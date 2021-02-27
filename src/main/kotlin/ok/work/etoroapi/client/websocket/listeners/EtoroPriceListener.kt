package ok.work.etoroapi.client.websocket.listeners

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.lightstreamer.client.ItemUpdate
import com.lightstreamer.client.Subscription
import ok.work.etoroapi.client.websocket.subscriptionFields
import ok.work.etoroapi.watchlist.Watchlist
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
class EtoroPriceListener : EtoroListener() {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var watchlist: Watchlist

    @Autowired
    lateinit var simpMessagingTemplate: SimpMessagingTemplate

    override fun onListenEnd(subscription: Subscription) {
        val subscriptionJsonStr = jacksonObjectMapper().writeValueAsString(subscription)
        logger.warn("onListenEnd: $subscriptionJsonStr")
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onItemUpdate(itemUpdate: ItemUpdate) {
        val id = itemUpdate.itemName.replace("instrument:", "")

        if (watchlist.getById(id) !== null) {
            watchlist.updatePrice(id, itemUpdate.getValue(2), itemUpdate.getValue(3))
            // For some market (HKG50), the market status could be wrongly set. we assume market open when there is price update
/*            watchlist.updateMarketStatus(id, true)*/
            watchlist.updateMarketStatus(id, itemUpdate.getValue(4)!!.toBoolean())

            watchlist.updateDiscounted(id, itemUpdate.getValue(16)!!.toDouble(), itemUpdate.getValue(17)!!.toDouble())

            val log = StringBuilder()
            var update = HashMap<String, String>()
            for (i in 1..subscriptionFields.size) {
                update.put(subscriptionFields[i-1], itemUpdate.getValue(i))
                log.append("${itemUpdate.getValue(i)} | ")
            }
        logger.debug("onItemUpdate: $log.toString()")
            simpMessagingTemplate.convertAndSend("/api/price", update)
        }
    }
}


