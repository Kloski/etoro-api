package ok.work.etoroapi.client

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import ok.work.etoroapi.client.browser.EtoroMetadataService
import ok.work.etoroapi.model.*
import ok.work.etoroapi.transactions.Transaction
import ok.work.etoroapi.transactions.TransactionPool
import ok.work.etoroapi.watchlist.EtoroAsset
import ok.work.etoroapi.watchlist.EtoroFullAsset
import ok.work.etoroapi.watchlist.Image
import ok.work.etoroapi.watchlist.Watchlist
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse


data class ViewContext(val ClientViewRate: Double)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EtoroPosition(
        val PositionID: String?,
        val InstrumentID: String,
        val IsBuy: Boolean,
        val Leverage: Int,
        val StopLossRate: Double,
        val TakeProfitRate: Double,
        val IsTslEnabled: Boolean,
        val View_MaxPositionUnits: Int,
        val View_Units: Double,
        val View_openByUnits: Boolean?,
        val Amount: Double,
        val ViewRateContext: ViewContext?,
        val OpenDateTime: String?,
        val IsDiscounted: Boolean?,
        val OpenRate: Double?,
        val CloseRate: Double?,
        val NetProfit: Double?,
        val CloseDateTime: String?
)

data class EtoroPositionForOpen(
        val PositionID: String?, val InstrumentID: String, val IsBuy: Boolean, val Leverage: Int,
        val StopLossRate: Double, val TakeProfitRate: Double, val IsTslEnabled: Boolean,
        val View_MaxPositionUnits: Int, val View_Units: Double, val View_openByUnits: Boolean?,
        val Amount: Double, val ViewRateContext: ViewContext?, val OpenDateTime: String?, val IsDiscounted: Boolean?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EtoroPositionForUpdate(
        val PositionID: String,
        val StopLossRate: Double?,
        val TakeProfitRate: Double?,
        val IsTslEnabled: Boolean?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EtoroMirrors(val Invested: Double, val NetProfit: Double, val PendingForClosure: Boolean, val MirrorID: Long, val Value: Double, val ParentCID: Long, val ParentUsername: String)

data class AssetInfoRequest(val instrumentIds: Array<String>)

@Component
class EtoroHttpClient {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var userContext: UserContext

    @Autowired
    private lateinit var watchlist: Watchlist

    @Autowired
    private lateinit var transactionPool: TransactionPool

    @Autowired
    private lateinit var metadataService: EtoroMetadataService

    private val client = HttpClient.newHttpClient()

    var okHttpClient = OkHttpClient()

    var cachedInstruments: ArrayList<EtoroFullAsset> = arrayListOf()

    var cachedAssetInfoMap: MutableMap<String, JSONObject> = mutableMapOf()


    fun getInstruments(): List<EtoroFullAsset> {
        val req = HttpRequest.newBuilder()
                .uri(URI("https://api.etorostatic.com/sapi/instrumentsmetadata/V1.1/instruments/bulk?bulkNumber=1&cv=77286b759effc7a624555e466cfb7c86_48a07d20d16ee784216c9eed65623d62&totalBulks=1"))
                .GET()
                .build()
        if (cachedInstruments.isEmpty()) {
            val response = client.send(req, HttpResponse.BodyHandlers.ofString()).body()
            val jsonArray: JSONArray = JSONObject(response).getJSONArray("InstrumentDisplayDatas")
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val images = item.getJSONArray("Images")
                val imageList: ArrayList<Image> = arrayListOf()
                for (j in 0 until images.length()) {
                    val imageData = images.getJSONObject(j)
                    if (!imageData.has("Uri")) {
                        continue;
                    }
                    val image = Image(imageData.getInt("Width"), imageData.getInt("Height"), imageData.getString("Uri"))
                    imageList.add(image)
                }
                var id: String
                try {
                    id = item.getString("InstrumentID")
                } catch (e: Exception) {
                    id = item.getInt("InstrumentID").toString()
                }
                val asset = EtoroFullAsset(
                        id,
                        item.getString("SymbolFull"),
                        item.getString("InstrumentDisplayName"),
                        imageList.toList()
                )
                cachedInstruments.add(asset)
            }
        }
        return cachedInstruments.toList()
    }

    fun getPersonDetail(mode: TradingMode, cid: String): UserDetail {
        val req = prepareRequest("sapi/rankings/cid/${cid}/rankings/?Period=OneYearAgo&client_request_id=${userContext.requestId}",
            userContext.exchangeToken, mode, metadataService.getMetadata())
                .GET()
                .build()

        val response = client.send(req, HttpResponse.BodyHandlers.ofString())
        val jsonObject = JSONObject(response.body())
        val personData = jsonObject.getJSONObject("Data").toString()

        val mapper = jacksonObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
        val result: UserDetail = mapper.readValue(personData)
        return result
    }

    fun getPersonHistory(mode: TradingMode, cid: String): YearMonthPerformance {
        val req = prepareRequest("sapi/userstats/gain/cid/${cid}/history?IncludeSimulation=true&&client_request_id=${userContext.requestId}",
                userContext.exchangeToken, mode, metadataService.getMetadata())
                .GET()
                .build()

        val jsonStr = client.send(req, HttpResponse.BodyHandlers.ofString()).body()

        val mapper = jacksonObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
        val result: YearMonthPerformance = mapper.readValue(jsonStr)
        return result
    }

    fun getPersonDailyChartData(mode: TradingMode, userName: String, period: String?): DailyChartData {
        val req = prepareRequest("sapi/userstats/CopySim/Username/${userName}/${if (period != null) period else "OneYearAgo"}?callback=angular.callbacks._1&client_request_id=${userContext.requestId}",
                userContext.exchangeToken, mode, metadataService.getMetadata())
                .GET()
                .build()

        val jsonStr = client.send(req, HttpResponse.BodyHandlers.ofString()).body()
        val substringAfter = jsonStr.substringAfter('{')
        val substringBeforeLast = substringAfter.substringBeforeLast('}')
        val jsonObject = JSONObject("{" + substringBeforeLast + "}")
        val chartData = jsonObject.getJSONObject("simulation");
        var periodName: String? = chartData.keys().next()


        val mapper = jacksonObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
        val result: DailyChartData = mapper.readValue(chartData.get(periodName).toString())
        return result
    }

    fun getPersonData(mode: TradingMode, cid: String): List<EtoroMirrors> {
        val req = prepareRequest("sapi/trade-data-real/live/public/portfolios?cid=${cid}&client_request_id=${userContext.requestId}",
                userContext.exchangeToken, mode, metadataService.getMetadata())
                .GET()
                .build()

        val jsonObject = JSONObject(client.send(req, HttpResponse.BodyHandlers.ofString()).body())
        val copyPersons = jsonObject.getJSONArray("AggregatedMirrors").toString()
        val aggregatedPositions = jsonObject.getJSONArray("AggregatedPositions").toString()

        val mapper = jacksonObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
        val result: List<EtoroMirrors> = mapper.readValue(copyPersons)
        return result
    }

    /**
     * Copy user detail data - not so useful
     */
    fun getPersonDataData(mode: TradingMode, cidList: String): List<EtoroPosition> {
        val req = prepareRequest("api/logininfo/v1.1/aggregatedInfo?avatar=false&cidList=%5B${cidList}%5D&client_request_id=${userContext.requestId}&realcid=true",
                userContext.exchangeToken, mode, metadataService.getMetadata())
                .GET()
                .build()

        val response = JSONObject(client.send(req, HttpResponse.BodyHandlers.ofString()).body())
                .toString()

        try {
            throw Exception("Hi There!")
        } catch (e: Exception) {
            logger.error("Test fail", e)
        } finally {
            // optional finally block
        }

        return emptyList()
    }

    fun getPositions(mode: TradingMode): List<EtoroPosition> {
        val req = prepareRequest(
                "api/logininfo/v1.1/logindata?" +
                        "client_request_id=${userContext.requestId}&conditionIncludeDisplayableInstruments=false&conditionIncludeMarkets=false&conditionIncludeMetadata=false&conditionIncludeMirrorValidation=false",
                userContext.exchangeToken, mode, metadataService.getMetadata()
        )
                .GET()
                .build()

        val response = JSONObject(client.send(req, HttpResponse.BodyHandlers.ofString()).body())
                .getJSONObject("AggregatedResult")
                .getJSONObject("ApiResponses")
                .getJSONObject("PrivatePortfolio")
                .getJSONObject("Content")
                .getJSONObject("ClientPortfolio")
                .getJSONArray("Positions")
                .toString()

        val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)

        val positions: List<EtoroPosition> = mapper.readValue(response)
        return positions.map {
            val instrumentId = it.InstrumentID
            val assetInfo = getAssetInfo(instrumentId, mode)
            if (watchlist.getById(instrumentId) != null) {
                if (it.IsBuy) {
                    val price = watchlist.getPrice(
                            instrumentId,
                            PositionType.SELL,
                            assetInfo.getBoolean("AllowDiscountedRates")
                    )
                    it.copy(NetProfit = (price - it.OpenRate!!) * it.Leverage * it.Amount / it.OpenRate)
                } else {
                    val price =
                            watchlist.getPrice(instrumentId, PositionType.BUY, assetInfo.getBoolean("AllowDiscountedRates"))
                    it.copy(NetProfit = (it.OpenRate!! - price) * it.Leverage * it.Amount / it.OpenRate)
                }
            } else {
                it
            }
        }
    }

    fun getLoginData(mode: String): JSONObject {
        val request = prepareRequest(
            "api/logininfo/v1.1/logindata?" +
                    "client_request_id=${userContext.requestId}&conditionIncludeDisplayableInstruments=false&conditionIncludeMarkets=false&conditionIncludeMetadata=false&conditionIncludeMirrorValidation=false",
            userContext.exchangeToken, ofString(mode), metadataService.getMetadata()
        )
            .GET()
            .build()
        return JSONObject(client.send(request, HttpResponse.BodyHandlers.ofString()).body())
    }

    fun getMirrors(mode: String): List<Mirror> {
        val loginData = getLoginData(mode)
        val mirrors = loginData.getJSONObject("AggregatedResult")
            .getJSONObject("ApiResponses")
            .getJSONObject("PrivatePortfolio")
            .getJSONObject("Content")
            .getJSONObject("ClientPortfolio")
            .getJSONArray("Mirrors")
        val hasUserData =  loginData.getJSONObject("AggregatedResult")
            .getJSONObject("ApiResponses")
            .getJSONObject("MirrorsUserData")
            .getInt("StatusCode") == 200
        if (hasUserData) {
            var userData = loginData.getJSONObject("AggregatedResult")
                .getJSONObject("ApiResponses")
                .getJSONObject("MirrorsUserData")
                .getJSONObject("Content")
                .getJSONArray("users")
            for (i in 0 until mirrors.length()) {
                val mirror = mirrors.getJSONObject(i)
                val user = userData.find {
                    it is JSONObject && it.getInt("realCID") == mirror.getInt("ParentCID")
                }
                if (user is JSONObject) {
                    mirror.put("User", user)
                }
            }
        }
        val json = mirrors.toString()
        val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
        return mapper.readValue(json)
    }

    fun getMirrorPositions(mode: String, mirror_id: String? = null): List<EtoroPosition> {
        val mirrorsData = getLoginData(mode)
            .getJSONObject("AggregatedResult")
            .getJSONObject("ApiResponses")
            .getJSONObject("PrivatePortfolio")
            .getJSONObject("Content")
            .getJSONObject("ClientPortfolio")
            .getJSONArray("Mirrors")
        if (mirror_id != null) {
            val mirror = mirrorsData.find { it is JSONObject && it.getInt("MirrorID") == mirror_id.toInt() }
            if (mirror is JSONObject) {
                val positionsJSON = mirror.getJSONArray("Positions").toString()
                val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
                return mapper.readValue(positionsJSON)
            }
            return listOf()
        } else {
            var allPositions: List<EtoroPosition> = listOf()
            for (i in 0 until mirrorsData.length()) {
                val mirrorData = mirrorsData.getJSONObject(i)
                val positionsJSON = mirrorData.getJSONArray("Positions").toString()
                val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
                val mirrorPositions: List<EtoroPosition> = mapper.readValue(positionsJSON)
                allPositions = allPositions + mirrorPositions
            }
            return allPositions
        }
    }

    fun getMirroredInstrumentIds(mode: String): List<String> {
        return getMirrorPositions(mode).map { it.InstrumentID }.distinct().sorted()
    }

    fun getHistoryPositions(
            limit: String = "100",
            page: String = "1",
            StartTime: String = "",
            mode: TradingMode
    ): List<EtoroPosition> {
        val req = prepareRequest(
                "sapi/trade-data-${mode.name.toLowerCase()}/history/private/credit/flat?ItemsPerPage=$limit&PageNumber=$page&StartTime=$StartTime",
                userContext.exchangeToken, mode, metadataService.getMetadata()
        )
                .GET()
                .build()

        val response = JSONObject(client.send(req, HttpResponse.BodyHandlers.ofString()).body())
                .getJSONArray("HistoryPositions")
                .toString()

        val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
        return mapper.readValue(response)
    }

    fun getInstrumentIDs(): List<EtoroAsset> {
        val req = HttpRequest.newBuilder()
                .uri(URI("https://api.etorostatic.com/sapi/instrumentsmetadata/V1.1/instruments?cv=1c85198476a3b802326706d0c583e99b_beb3f4faa55c3a46ed44fc6d763db563"))
                .GET()
                .build()

        val response =
                JSONObject(client.send(req, HttpResponse.BodyHandlers.ofString()).body()).get("InstrumentDisplayDatas")
                        .toString()
        val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        return mapper.readValue(response)
    }

    fun openPosition(position: Position, mode: TradingMode): Transaction {
        val type = position.type.equals(PositionType.BUY)
        val instrumentId = position.instrumentId ?: watchlist.getInstrumentIdByName(position.name ?: "")
        val assetInfo = getAssetInfo(instrumentId, mode)
        val price = watchlist.getPrice(instrumentId, position.type, assetInfo.getBoolean("AllowDiscountedRates"))
        val leverages = assetInfo.getJSONArray("Leverages")
        val minPositionAmount = assetInfo.getInt("MinPositionAmount")
        val minPositionAmountAbsolute = assetInfo.getInt("MinPositionAmountAbsolute")

/*        if (watchlist.isMarketOpen(instrumentId)) {*/
        if (!leverages.contains(position.leverage)) {
            throw RuntimeException("x${position.leverage} is not permitted. You can use $leverages")
        }
        if (minPositionAmount > position.leverage * position.amount || position.amount < minPositionAmountAbsolute) {
            throw RuntimeException("You cannot open less than minimum position amount $$minPositionAmount, and minimum absolute amount $$minPositionAmountAbsolute")
        }


        if (position.type == PositionType.SELL) {
            when {
                position.stopLossAmountRate > 0.0 -> position.stopLossRate =
                        price + (price * position.stopLossAmountRate / 100) / position.leverage
                position.stopLossRate > 0.0 -> position.stopLossRate = price + (price * position.stopLossRate / 100)
                position.stopLoss > 0.0 -> position.stopLossRate = position.stopLoss
                else -> {
                    val maxSL = assetInfo.getInt("MaxStopLossPercentage")
                    position.stopLossRate = price + (price * maxSL / 100)
                }
            }

            when {
                position.takeProfitAmountRate > 0.0 -> position.takeProfitRate =
                        price - (price * position.takeProfitAmountRate / 100) / position.leverage
                position.takeProfitRate > 0.0 -> position.takeProfitRate =
                        price - (price * position.takeProfitRate / 100)
                position.takeProfit > 0.0 -> position.takeProfitRate = position.takeProfit
                else -> {
                    position.takeProfitRate = price - (price * 50 / 100)
                }
            }
        } else if (position.type == PositionType.BUY) {
            when {
                position.stopLossAmountRate > 0.0 -> position.stopLossRate =
                        price - (price * position.stopLossAmountRate / 100) / position.leverage
                position.stopLossRate > 0.0 -> position.stopLossRate = price - (price * position.stopLossRate / 100)
                position.stopLoss > 0.0 -> position.stopLossRate = position.stopLoss
                else -> {
                    val maxSL = assetInfo.getInt("MaxStopLossPercentage")
                    position.stopLossRate = price - (price * maxSL / 100)
                }
            }
            when {
                position.takeProfitAmountRate > 0.0 -> position.takeProfitRate =
                        price + (price * position.takeProfitAmountRate / 100) / position.leverage
                position.takeProfitRate > 0.0 -> position.takeProfitRate =
                        price + (price * position.takeProfitRate / 100)
                position.takeProfit > 0.0 -> position.takeProfitRate = position.takeProfit
                else -> {
                    position.takeProfitRate = price + (price * 50 / 100)
                }
            }

        }
        position.takeProfitRate = position.takeProfitRate.round(assetInfo.getInt("Precision"))
        position.stopLossRate = position.stopLossRate.round(assetInfo.getInt("Precision"))

        val positionRequestBody = EtoroPositionForOpen(
                null,
                instrumentId,
                type,
                position.leverage,
                position.stopLossRate,
                position.takeProfitRate,
                position.tsl,
                assetInfo.getInt("MaxPositionUnits"),
                0.01,
                false,
                position.amount,
                ViewContext(price),
                null,
                assetInfo.getBoolean("AllowDiscountedRates")
        )

        val req = prepareRequest(
                "sapi/trade-${mode.name.toLowerCase()}/positions?client_request_id=${userContext.requestId}",
                userContext.exchangeToken,
                mode,
                metadataService.getMetadata()
        )
                .POST(HttpRequest.BodyPublishers.ofString(JSONObject(positionRequestBody).toString()))
                .build()
        val body = client.send(req, HttpResponse.BodyHandlers.ofString()).body()

        val transactionId = JSONObject(body).getString("Token")
        return transactionPool.getFromPool(transactionId) ?: Transaction(transactionId, null, null, null, null)
    }

    fun updatePosition(positionRequestBody: EtoroPositionForUpdate, mode: TradingMode): Transaction {
        val id = positionRequestBody.PositionID
        val requestBody = JSONObject(positionRequestBody)

        val req = prepareRequest(
                "sapi/trade-${mode.name.toLowerCase()}/positions/$id?client_request_id=${userContext.requestId}",
                userContext.exchangeToken,
                mode,
                metadataService.getMetadata()
        )
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build()

        val body = client.send(req, HttpResponse.BodyHandlers.ofString()).body()
        val transactionId = JSONObject(body).getString("Token")
        return transactionPool.getFromPool(transactionId) ?: Transaction(transactionId, null, null, null, null)
    }

    fun deletePosition(id: String, mode: TradingMode) {
        val req = prepareOkRequest(
                "sapi/trade-${mode.name.toLowerCase()}/positions/$id?PositionID=$id&client_request_id=${userContext.requestId}",
                userContext.exchangeToken,
                mode,
                metadataService.getMetadata()
        )
        req.delete("{}".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()))

        val code = okHttpClient.newCall(req.build()).execute().code

        if (code != 200) {
            throw RuntimeException("Failed close positionID $id")
        }
    }

    fun watchMirroredAssets(mode: String): Int {
        val mirroredAssets = getMirroredInstrumentIds(mode)
        for (id in mirroredAssets) {
            if (watchlist.getById(id) == null) {
                watchlist.addAssetToWatchlistById(id)
            }
        }
        return mirroredAssets.size
    }

    fun getAssetInfo(id: String, mode: TradingMode): JSONObject {
        val body = AssetInfoRequest(arrayOf(id))
        val req = prepareRequest(
                "sapi/trade-real/instruments/private/index?client_request_id=${userContext.requestId}",
                userContext.exchangeToken, mode, metadataService.getMetadata()
        )
                .POST(HttpRequest.BodyPublishers.ofString(JSONObject(body).toString()))
                .build()
        return JSONObject(client.send(req, HttpResponse.BodyHandlers.ofString()).body()).getJSONArray("Instruments")
                .getJSONObject(0)
    }

    fun getCash(mode: TradingMode): Double {
        val req = prepareRequest(
                "api/logininfo/v1.1/logindata?" +
                        "client_request_id=${userContext.requestId}&conditionIncludeDisplayableInstruments=false&conditionIncludeMarkets=false&conditionIncludeMetadata=false&conditionIncludeMirrorValidation=false",
                userContext.exchangeToken, mode, metadataService.getMetadata()
        )
                .GET()
                .build()

        return JSONObject(client.send(req, HttpResponse.BodyHandlers.ofString()).body())
                .getJSONObject("AggregatedResult")
                .getJSONObject("ApiResponses")
                .getJSONObject("PrivatePortfolio")
                .getJSONObject("Content")
                .getJSONObject("ClientPortfolio")
                .getDouble("Credit")
    }

}
