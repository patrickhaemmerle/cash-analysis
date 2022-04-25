package ch.haemmi.cash.analysis

import java.util.*
import java.util.concurrent.TimeUnit


fun main() {
    val webScraper = WebScraper()
    val stockList = (
            webScraper.fetchStockList(StockList.CH) +
                    webScraper.fetchStockList(StockList.STOXX600) +
                    webScraper.fetchStockList(StockList.NASDAQ_100) +
                    webScraper.fetchStockList(StockList.SP_500) +
                    webScraper.fetchStockList(StockList.DJIA) +
                    webScraper.fetchStockList(StockList.DAX) +
                    webScraper.fetchStockList(StockList.CAC) +
                    webScraper.fetchStockList(StockList.SBF_250)
            )
        .distinctBy { it.id }

    println("Bullish Stocks")
    println("========================================")
    stockList
        .filter { it.monitorValue == MonitorValue.POSITIVE }
        .filter { it.getDetails().chanceValue == ChanceValue.FOUR }
        .filter { it.getDetails().valuation == Valuation.STRONGLY_UNDERVALUED }
        .distinctBy { it.getDetails().symbol }
        .forEach { printStock(it) }

    println()
    println("Bearish Stocks")
    println("========================================")
    stockList
        .filter { it.monitorValue == MonitorValue.NEGATIVE }
        .filter { it.getDetails().chanceValue == ChanceValue.ZERO }
        .filter { it.getDetails().valuation == Valuation.STRONGLY_OVERVALUED }
        .distinctBy { it.getDetails().symbol }
        .forEach { printStock(it) }
}

fun printStock(stock: Stock) {
    val out = StringBuffer()
    out.append(if (isRecent(stock.getDetails().chanceUpdate) || isRecent(stock.getDetails().monitorUpdate)) "* " else "  ")
    out.append(stock.getDetails().symbol)
    out.append(" - ")
    out.append(stock.getDetails().name)
    out.append(" - ")
    out.append(BASE_URL + stock.id)
    println(out)
}

fun isRecent(date: Date?): Boolean {
    if (date == null) {
        return false
    }
    val diffInMillis: Long = Date().time - date.time
    return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS) < 4
}