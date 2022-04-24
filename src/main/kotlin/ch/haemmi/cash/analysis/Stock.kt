package ch.haemmi.cash.analysis

import java.util.*

class Stock(
    val valor: String,
    val id: String,
    val monitorValue: MonitorValue?,
    val webScraper: WebScraper
) {
    private var details: StockDetails? = null

    fun getDetails(): StockDetails {
        fun fetchDetails(): StockDetails {
            details = webScraper.fetchStockDetails(this)
            return details as StockDetails
        }
        return details ?: fetchDetails()
    }
}

data class StockDetails(
    val symbol: String,
    val name: String,
    val chanceValue: ChanceValue?,
    val valuation: Valuation?,
    val chanceUpdate: Date?,
    val monitorUpdate: Date?
)

enum class MonitorValue(val value: Int) {
    NEGATIVE(-2),
    RATHER_NEGATIVE(-1),
    NEUTRAL(0),
    RATHER_POSITIVE(1),
    POSITIVE(2)
}

enum class ChanceValue(val value: Int) {
    ZERO(0),
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4)
}

enum class Valuation() {
    STRONGLY_UNDERVALUED,
    UNDERVALUED,
    FAIR,
    OVERVALUED,
    STRONGLY_OVERVALUED
}