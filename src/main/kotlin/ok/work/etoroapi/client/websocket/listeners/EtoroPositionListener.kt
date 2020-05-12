package ok.work.etoroapi.client.websocket.listeners

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.lightstreamer.client.ItemUpdate
import ok.work.etoroapi.client.EtoroPosition
import ok.work.etoroapi.transactions.Transaction
import ok.work.etoroapi.transactions.TransactionPool
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class EtoroPositionListener : EtoroListener() {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var transactionPool: TransactionPool

    val mapper: ObjectMapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
            .configure(DeserializationFeature.EAGER_DESERIALIZER_FETCH, true)


    override fun onItemUpdate(itemUpdate: ItemUpdate) {
        val itemUpdateJsonStr = mapper.writeValueAsString(itemUpdate)
        logger.warn("onItemUpdate: $itemUpdateJsonStr")
        // println(itemUpdate.getValue(1))

        val transactionJson = JSONObject(JSONObject(itemUpdate.getValue(1)).getString("Content"))
        val requestToken = transactionJson.getString("RequestToken")

        if (transactionJson.has("ErrorMessageCode")) {
            val errorcode = transactionJson.getInt("ErrorMessageCode")
            val errormessage = if (transactionJson.has("NotificationParams")) transactionJson.getJSONObject("NotificationParams") else null
            transactionPool.addToPool(Transaction(requestToken, null, errorcode, mapper.readValue(errormessage.toString()), LocalDateTime.now()))
        } else {
            val position: EtoroPosition = mapper.readValue(transactionJson.getJSONObject("Position").toString())
            transactionPool.addToPool(Transaction(requestToken, position, 0, null, LocalDateTime.now()))
        }
    }
}
