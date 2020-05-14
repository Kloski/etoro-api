package ok.work.etoroapi.model

data class YearMonthPerformance(
        val customerId: Int?,
        val monthly: List<Monthly>?,
        val yearly: List<Yearly>?
)

data class Yearly(
        val gain: Double,
        val isSimulation: Boolean,
        val start: String
)

data class Monthly(
        val gain: Double,
        val isSimulation: Boolean,
        val start: String
)
