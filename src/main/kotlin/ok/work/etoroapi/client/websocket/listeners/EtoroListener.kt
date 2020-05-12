package ok.work.etoroapi.client.websocket.listeners

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.lightstreamer.client.ItemUpdate
import com.lightstreamer.client.Subscription
import com.lightstreamer.client.SubscriptionListener
import org.slf4j.LoggerFactory

open class EtoroListener : SubscriptionListener {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun onListenEnd(subscription: Subscription) {
        val subscriptionJsonStr = jacksonObjectMapper().writeValueAsString(subscription)
        logger.warn("onListenEnd: $subscriptionJsonStr")
    }

    override fun onItemUpdate(itemUpdate: ItemUpdate) {
        val jsonStr = jacksonObjectMapper().writeValueAsString(itemUpdate)
        logger.warn("onItemUpdate: $jsonStr")
    }

    override fun onSubscription() {
        logger.warn("onSubscription - subscribed")
    }

    override fun onEndOfSnapshot(itemName: String?, itemPos: Int) {
        logger.warn("onEndOfSnapshot: $itemName, itemPos: $itemPos")
    }

    override fun onItemLostUpdates(itemName: String?, itemPos: Int, lostUpdates: Int) {
        logger.warn("onItemLostUpdates: $itemName, itemPos: $itemPos, lostUpdates: $lostUpdates")
    }

    override fun onSubscriptionError(code: Int, message: String?) {
        logger.warn("onSubscriptionError: $message, code: $code")
    }

    override fun onClearSnapshot(itemName: String?, itemPos: Int) {
        logger.warn("onClearSnapshot: $itemName, itemPos: $itemPos")
    }

    override fun onCommandSecondLevelSubscriptionError(code: Int, message: String?, key: String?) {
        logger.warn("onCommandSecondLevelSubscriptionError: $message, code: $code, key: $key")
    }

    override fun onUnsubscription() {
        logger.warn("onUnsubscription")
    }

    override fun onCommandSecondLevelItemLostUpdates(lostUpdates: Int, key: String) {
        logger.warn("onCommandSecondLevelItemLostUpdates: $lostUpdates, key: $key")
    }

    override fun onListenStart(subscription: Subscription) {
        logger.warn("onListenStart: $subscription")
    }

}
