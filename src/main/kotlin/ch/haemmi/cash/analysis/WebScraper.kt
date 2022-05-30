package ch.haemmi.cash.analysis

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat

const val BASE_URL = "https://www.cash.ch/"

enum class StockList(val url: String) {
    STOXX600(BASE_URL + "kurse/aktien/europa/stoxx-600"),
    NASDAQ_100(BASE_URL + "kurse/aktien/usa-und-kanada/nasdaq-100"),
    SP_500(BASE_URL + "kurse/aktien/usa-und-kanada/sp500"),
    DJIA(BASE_URL + "kurse/aktien/usa-und-kanada/djia"),
}

class WebScraper {
    fun fetchStockList(stockList: StockList): List<Stock> {
        val doc = Jsoup.connect(stockList.url).get()
        return doc.select("#main-list-fragment > tbody > tr")
            .map {
                Stock(
                    it.attr("data-valor"),
                    it.select(":nth-child(2)").attr("data-raw"),
                    this.evaluateMonitorValueFromHtml(it),
                    this
                )
            }
            .toList()
    }

    fun fetchStockDetails(stock: Stock): StockDetails {
        val detailsPage = Jsoup.connect(BASE_URL + stock.id).get()
        val monitorPage = Jsoup.connect(BASE_URL + stock.id + "/monitor").get()
        val monitorPageHtml = monitorPage.html()
        val title = detailsPage.select("title")
        val name = if (title.size > 0) title[0].text().substringBefore("-").trim() else "n/a"
        val symbol = detailsPage.select("span[class=valor-symbol]")[0].text().substringAfter(":").substringAfter(":").trim()

        val formatter = SimpleDateFormat("dd.MM.yyyy")
        val monitorUpdateRegex = "Die Aktie ist seit dem (\\d{2}\\.\\d{2}\\.\\d{4}) als".toRegex()
        val monitorUpdateString = monitorUpdateRegex.find(monitorPageHtml)?.groupValues?.get(1)
        val monitorUpdate = if (monitorUpdateString != null) formatter.parse(monitorUpdateString) else null
        val chanceUpdateRegex = "Das Chancenprofil ist seit dem (\\d{2}\\.\\d{2}\\.\\d{4})".toRegex()
        val chanceUpdateString = chanceUpdateRegex.find(monitorPageHtml)?.groupValues?.get(1)
        val chanceUpdate = if (chanceUpdateString != null) formatter.parse(chanceUpdateString) else null
        return StockDetails(
            symbol = symbol,
            name = name,
            chanceValue = evaluateChanceValueFromHtml(detailsPage),
            valuation = evaluateValuationFromHtml(detailsPage),
            chanceUpdate = chanceUpdate,
            monitorUpdate = monitorUpdate
        )
    }

    private fun evaluateValuationFromHtml(doc: Document): Valuation? {
        val html = doc.html()
        if (html.contains("Stark unterbewertet")) {
            return Valuation.STRONGLY_UNDERVALUED
        }
        if (html.contains("Leicht unterbewertet")) {
            return Valuation.UNDERVALUED
        }
        if (html.contains("Fairer Preis")) {
            return Valuation.FAIR
        }
        if (html.contains("Leicht überbewertet")) {
            return Valuation.OVERVALUED
        }
        if (html.contains("Stark überbewertet")) {
            return Valuation.STRONGLY_OVERVALUED
        }
        return null
    }

    private fun evaluateMonitorValueFromHtml(it: Element): MonitorValue? {
        return try {
            when (it.select("[data-field=GLEVAL_SCRE]").attr("data-raw").toInt()) {
                -2 -> MonitorValue.NEGATIVE
                -1 -> MonitorValue.RATHER_NEGATIVE
                0 -> MonitorValue.NEUTRAL
                1 -> MonitorValue.RATHER_POSITIVE
                2 -> MonitorValue.POSITIVE
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun evaluateChanceValueFromHtml(doc: Document): ChanceValue? {
        try {
            val hasStars = doc.select("td[class='thescreener-irst']").size > 0
            val chanceValue =
                if (hasStars) doc.select("td[class='thescreener-irst'] > span[class='icon-star-full']").size
                else null

            return when (chanceValue) {
                0 -> ChanceValue.ZERO
                1 -> ChanceValue.ONE
                2 -> ChanceValue.TWO
                3 -> ChanceValue.THREE
                4 -> ChanceValue.FOUR
                else -> null
            }
        } catch (e: Exception) {
            return null
        }
    }
}
