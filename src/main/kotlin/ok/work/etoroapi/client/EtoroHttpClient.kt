package ok.work.etoroapi.client

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import ok.work.etoroapi.client.cookies.EtoroMetadataService
import ok.work.etoroapi.model.*
import ok.work.etoroapi.transactions.Transaction
import ok.work.etoroapi.transactions.TransactionPool
import ok.work.etoroapi.watchlist.EtoroAsset
import ok.work.etoroapi.watchlist.Watchlist
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*

data class ViewContext(val ClientViewRate: Double)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EtoroPosition(val PositionID: String?, val InstrumentID: String, val IsBuy: Boolean, val Leverage: Int,
                         val StopLossRate: Double, val TakeProfitRate: Double, val IsTslEnabled: Boolean,
                         val View_MaxPositionUnits: Int, val View_Units: Double, val View_openByUnits: Boolean?,
                         val Amount: Int, val ViewRateContext: ViewContext?, val OpenDateTime: String?, val isDiscounted: Boolean?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EtoroMirrors(val Invested: Double, val NetProfit: Double, val PendingForClosure: Boolean, val MirrorID: Long, val Value: Double, val ParentCID: Long, val ParentUsername: String)

data class AssetInfoRequest(val instrumentIds: Array<String>)

data class AssetInfo(val InstrumentID: Int, val AllowDiscountedRates: Boolean)

data class AssetInfoResponse(val Instruments: Array<AssetInfo>)

@Component
class EtoroHttpClient {

    @Autowired
    private lateinit var authorizationContext: AuthorizationContext

    @Autowired
    private lateinit var watchlist: Watchlist

    @Autowired
    private lateinit var transactionPool: TransactionPool

    @Autowired
    private lateinit var metadataService: EtoroMetadataService

    private val client = HttpClient.newHttpClient()

    fun getPersonDetail(mode: TradingMode, cid: String): UserDetail {
        val req = prepareRequest("sapi/rankings/cid/${cid}/rankings/?Period=OneYearAgo&&client_request_id=${authorizationContext.requestId}",
                authorizationContext.exchangeToken, mode, metadataService.getMetadata())
                .GET()
                .build()

        val jsonObject = JSONObject(client.send(req, HttpResponse.BodyHandlers.ofString()).body())
        val personData = jsonObject.getJSONObject("Data").toString()

        val mapper = jacksonObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
        val result: UserDetail = mapper.readValue(personData)
        return result
    }

    fun getPersonHistory(mode: TradingMode, cid: String): YearMonthPerformance {
        val req = prepareRequest("sapi/userstats/gain/cid/${cid}/history?IncludeSimulation=true&&client_request_id=${authorizationContext.requestId}",
                authorizationContext.exchangeToken, mode, metadataService.getMetadata())
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
        val req = prepareRequest("sapi/userstats/CopySim/Username/${userName}/${if (period != null) period else "OneYearAgo"}?callback=angular.callbacks._1&client_request_id=${authorizationContext.requestId}",
                authorizationContext.exchangeToken, mode, metadataService.getMetadata())
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
        val req = prepareRequest("sapi/trade-data-real/live/public/portfolios?cid=${cid}&client_request_id=${authorizationContext.requestId}",
                authorizationContext.exchangeToken, mode, metadataService.getMetadata())
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
        val req = prepareRequest("api/logininfo/v1.1/aggregatedInfo?avatar=false&cidList=%5B${cidList}%5D&client_request_id=${authorizationContext.requestId}&realcid=true",
                authorizationContext.exchangeToken, mode, metadataService.getMetadata())
                .GET()
                .build()

        val response = JSONObject(client.send(req, HttpResponse.BodyHandlers.ofString()).body())
                .toString()

        return Collections.emptyList()
    }

    fun getPositions(mode: TradingMode): List<EtoroPosition> {
        val req = prepareRequest("api/logininfo/v1.1/logindata?" +
                "client_request_id=${authorizationContext.requestId}&conditionIncludeDisplayableInstruments=false&conditionIncludeMarkets=false&conditionIncludeMetadata=false&conditionIncludeMirrorValidation=false",
                authorizationContext.exchangeToken, mode, metadataService.getMetadata())
                .GET()
                .build()

        val logindataJsonObject = JSONObject(client.send(req, HttpResponse.BodyHandlers.ofString()).body())
        val response = logindataJsonObject
                .getJSONObject("AggregatedResult")
                .getJSONObject("ApiResponses")
                .getJSONObject("PrivatePortfolio")
                .getJSONObject("Content")
                .getJSONObject("ClientPortfolio")
                .getJSONArray("Positions")
                .toString()

        val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
        return mapper.readValue(response)
    }

    fun getInstrumentIDs(): List<EtoroAsset> {
        val req = HttpRequest.newBuilder().uri(URI("https://api.etorostatic.com/sapi/instrumentsmetadata/V1.1/instruments?cv=1c85198476a3b802326706d0c583e99b_beb3f4faa55c3a46ed44fc6d763db563"))
                .GET()
                .build()

        val response = JSONObject(client.send(req, HttpResponse.BodyHandlers.ofString()).body()).get("InstrumentDisplayDatas").toString()
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

        if (watchlist.isMarketOpen(instrumentId)) {
            if (!leverages.contains(position.leverage)) {
                throw RuntimeException("x${position.leverage} is not permitted. You can use $leverages")
            }
            if (minPositionAmount > position.leverage * position.amount || position.amount < minPositionAmountAbsolute) {
                throw RuntimeException("You cannot open less than minimum position amount $$minPositionAmount, and minimum absolute amount $$minPositionAmountAbsolute")
            }
            if (position.type == PositionType.SELL) {
                if (position.stopLossRate == 0.0) {
                    val maxSL = assetInfo.getInt("MaxStopLossPercentage")
                    position.stopLossRate = price + (price * maxSL / 100)
                }
                if (position.takeProfitRate == 0.0) {
                    position.takeProfitRate = (price * 50 / 100)
                }
            }
            val positionRequestBody = EtoroPosition(null, instrumentId, type, position.leverage, position.stopLossRate, position.takeProfitRate, false, assetInfo.getInt("MaxPositionUnits"),
                    0.01, false, position.amount, ViewContext(price), null, assetInfo.getBoolean("AllowDiscountedRates"))

            val req = prepareRequest("sapi/trade-${mode.name.toLowerCase()}/positions?client_request_id=${authorizationContext.requestId}", authorizationContext.exchangeToken, mode, metadataService.getMetadata())
                    .POST(HttpRequest.BodyPublishers.ofString(JSONObject(positionRequestBody).toString()))
                    .build()

            val transactionId = JSONObject(client.send(req, HttpResponse.BodyHandlers.ofString()).body()).getString("Token")
            return transactionPool.getFromPool(transactionId) ?: Transaction(transactionId, null, null, null, null)
        }
        throw RuntimeException("Market ${position.instrumentId} is closed.")

    }

    fun closePosition(id: String, mode: TradingMode) {
        val req = prepareRequest("sapi/trade-${mode.name.toLowerCase()}/positions/$id?PositionID=$id&client_request_id=${authorizationContext.requestId}",
                authorizationContext.exchangeToken, mode, metadataService.getMetadata())
                .DELETE()
                .build()

        val code = client.send(req, HttpResponse.BodyHandlers.ofString()).statusCode()

        if (code != 200) {
            throw RuntimeException("Failed close positionID $id")
        }
    }

    fun getAssetInfo(id: String, mode: TradingMode): JSONObject {
        val body = AssetInfoRequest(arrayOf(id))
        val req = prepareRequest("sapi/trade-real/instruments/private/index?client_request_id=${authorizationContext.requestId}",
                authorizationContext.exchangeToken, mode, metadataService.getMetadata())
                .POST(HttpRequest.BodyPublishers.ofString(JSONObject(body).toString()))
                .build()

        return JSONObject(client.send(req, HttpResponse.BodyHandlers.ofString()).body()).getJSONArray("Instruments").getJSONObject(0)
    }

}
