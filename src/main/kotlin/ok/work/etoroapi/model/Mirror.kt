package ok.work.etoroapi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Mirror(
    val MirrorID: Int,
    val InitialInvestment: Double,
    val DepositSummary: Double,
    val WithdrawalSummary: Double,
    val AvailableAmount: Double,
    val ClosedPositionsNetProfit: Double,
    val StopLossAmount: Double,
    val StopLossPercentage: Double,
    val CopyExistingPositions: Boolean,
    val IsPaused: Boolean,
    val PendingForClosure: Boolean,
    val StartedCopyDate: String,
    val User: User
)

data class MirrorWithPositions(
    @JsonProperty("MirrorID")
    val mirrorID: Long,

    @JsonProperty("CID")
    val cid: Long,

    @JsonProperty("ParentCID")
    val parentCID: Long,

    @JsonProperty("StopLossPercentage")
    val stopLossPercentage: Double,

    @JsonProperty("IsPaused")
    val isPaused: Boolean,

    @JsonProperty("CopyExistingPositions")
    val copyExistingPositions: Boolean,

    @JsonProperty("AvailableAmount")
    val availableAmount: Double,

    @JsonProperty("StopLossAmount")
    val stopLossAmount: Double,

    @JsonProperty("InitialInvestment")
    val initialInvestment: Double,

    @JsonProperty("DepositSummary")
    val depositSummary: Double,

    @JsonProperty("WithdrawalSummary")
    val withdrawalSummary: Double,

    @JsonProperty("Positions")
    val positions: List<MirrorPosition>,

    @JsonProperty("EntryOrders")
    val entryOrders: List<Any?>,

    @JsonProperty("ExitOrders")
    val exitOrders: List<Any?>,

    @JsonProperty("ParentUsername")
    val parentUsername: String,

    @JsonProperty("ClosedPositionsNetProfit")
    val closedPositionsNetProfit: Double,

    @JsonProperty("StartedCopyDate")
    val startedCopyDate: String,

    @JsonProperty("PendingForClosure")
    val pendingForClosure: Boolean,

    @JsonProperty("ParentMirrors")
    val parentMirrors: List<Any?>
)

data class MirrorPosition(
    @JsonProperty("PositionID")
    val positionID: Long,

    @JsonProperty("CID")
    val cid: Long,

    @JsonProperty("OpenDateTime")
    val openDateTime: String,

    @JsonProperty("OpenRate")
    val openRate: Double,

    @JsonProperty("InstrumentID")
    val instrumentID: Long,

    @JsonProperty("IsBuy")
    val isBuy: Boolean,

    @JsonProperty("TakeProfitRate")
    val takeProfitRate: Double,

    @JsonProperty("StopLossRate")
    val stopLossRate: Double,

    @JsonProperty("MirrorID")
    val mirrorID: Long,

    @JsonProperty("ParentPositionID")
    val parentPositionID: Long,

    @JsonProperty("Amount")
    val amount: Double,

    @JsonProperty("Leverage")
    val leverage: Long,

    @JsonProperty("OrderID")
    val orderID: Long,

    @JsonProperty("Units")
    val units: Double,

    @JsonProperty("TotalFees")
    val totalFees: Double,

    @JsonProperty("InitialAmountInDollars")
    val initialAmountInDollars: Double,

    @JsonProperty("IsTslEnabled")
    val isTslEnabled: Boolean,

    @JsonProperty("StopLossVersion")
    val stopLossVersion: Long,

    @JsonProperty("IsSettled")
    val isSettled: Boolean,

    @JsonProperty("RedeemStatusID")
    val redeemStatusID: Long,

    @JsonProperty("InitialUnits")
    val initialUnits: Double,

    @JsonProperty("IsPartiallyAltered")
    val isPartiallyAltered: Boolean,

    @JsonProperty("UnitsBaseValueDollars")
    val unitsBaseValueDollars: Double,

    @JsonProperty("IsDiscounted")
    val isDiscounted: Boolean
)