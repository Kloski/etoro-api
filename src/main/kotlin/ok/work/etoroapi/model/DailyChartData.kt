package ok.work.etoroapi.model

data class DailyChartData(
        val assets: Any?,
        val auditTrail: List<Any>,
        val chart: List<Chart>,
        val period: String,
        val stats: Stats
)

data class Chart(
        val credit: Double,
        val equity: Double,
        val investment: Double,
        val pnL: Double,
        val timestamp: String,
        val totalDividends: Double
)

data class Stats(
        val dailyDrawDown: Double,
        val dailyDrawDownStart: String,
        val gain: Double,
        val peakToValleyDrawDown: Double,
        val peakToValleyEnd: String,
        val peakToValleyStart: String,
        val standardDeviation: Double,
        val weeklyDrawDown: Double,
        val weeklyDrawDownStart: String
)
