package ok.work.etoroapi.model

data class UserDetail(
        val AUMTier: Int?,
        val AUMTierDesc: Any?,
        val AUMTierV2: Int?,
        val ActiveWeeks: Int?,
        val ActiveWeeksPct: Double?,
        val AffiliateId: Int?,
        val AvgPosSize: Double?,
        val BaseLineCopiers: Int?,
        val Blocked: Boolean?,
        val BonusOnly: Boolean?,
        val CopiedTrades: Int?,
        val Copiers: Int?,
        val CopiersGain: Double?,
        val CopyBlock: Boolean?,
        val CopyInvestmentPct: Double?,
        val CopyTradesPct: Double?,
        val Country: String?,
        val CustomerId: Int?, // cid
        val DailyDD: Double?, // DailyDrawDown
        val DailyGain: Double?, // % daily change
        val DisplayFullName: Boolean?,
        val Exposure: Double?,
        val FirstActivity: String?, // since
        val FundType: Int?,
        val Gain: Double?, // % Gain +/-
        val HasAvatar: Boolean?,
        val HighLeveragePct: Double?,
        val InstrumentPct: Double?,
        val IsBronze: Boolean?,
        val IsFund: Boolean?,
        val IsSocialConnected: Boolean?,
        val IsTestAccount: Boolean?,
        val LastActivity: String?, //
        val LongPosPct: Double?,
        val LowLeveragePct: Double?,
        val MaxDailyRiskScore: Int?,
        val MaxMonthlyRiskScore: Int?,
        val MediumLeveragePct: Double?,
        val OptimalCopyPosSize: Double?,
        val PeakToValley: Double?,
        val PeakToValleyEnd: String?,
        val PeakToValleyStart: String?,
        val PopularInvestor: Boolean?,
        val ProfitableMonthsPct: Double?, // % in +
        val ProfitableWeeksPct: Double?, // % in +
        val RiskScore: Int?,
        val ThisWeekGain: Double?, // % this week change
        val TopTradedAssetClassId: Int?,
        val TopTradedInstrumentId: Int?,
        val Trades: Int?,
        val UserName: String?, //
        val Velocity: Double?,
        val Verified: Boolean?,
        val VirtualCopiers: Int?,
        val WeeklyDD: Double?, // WeeklyDrawDown
        val WeeksSinceRegistration: Int?,
        val WinRatio: Double
)