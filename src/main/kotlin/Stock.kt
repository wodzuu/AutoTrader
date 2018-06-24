import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.rxkotlin.toObservable
import org.apache.commons.io.IOUtils
import java.net.URL
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Stock(val symbol: String) {
    data class Quote(val open: Array<Double?>?, val close: Array<Double?>?, val volume: Array<Long?>?)

    data class Indicators(val quote: Array<Quote>)

    data class Result(val timestamp: Array<Long>?, val indicators: Indicators) {
        val quotes: List<MarketQuote> by lazy {
            if (timestamp != null) {
                timestamp.withIndex()
                        .filter { indicators.quote[0].close!![it.index] != null }
                        .filter { indicators.quote[0].open!![it.index] != null }
                        .filter { indicators.quote[0].volume!![it.index] != null }
                        .filter { indicators.quote[0].volume!![it.index] != 0L }
                        .map {
                            try {
                                var timestamp = Calendar.getInstance(NEW_YORK_TIMEZONE)
                                timestamp.timeInMillis = it.value * 1000
                                MarketQuote(timestamp, indicators.quote[0].close!![it.index]!!, indicators.quote[0].open!![it.index]!!, indicators.quote[0].volume!![it.index]!!)
                            } catch (e: NullPointerException) {
                                throw e
                            }
                        }
            } else emptyList()
        }
    }

    data class ChartData(val result: Array<Result>)

    data class YahooResponse(val chart: ChartData)

// Don't cache!
    companion object {
        var cache: ChartData? = null
    }


    fun loadChartData(interval: Interval, from: Calendar, to: Calendar): ChartData {
        if (cache != null) return cache!!

        //println("Loading data from ${DEFAULT_DATE_FORMAT.format(from.time)} to ${DEFAULT_DATE_FORMAT.format(to.time)}")
        val url = "https://l1-query.finance.yahoo.com/v7/finance/chart/$symbol?period2=${(to.timeInMillis / 1000)}&period1=${(from.getTimeInMillis() / 1000)}&interval=${interval.key}&indicators=quote&includeTimestamps=true&includePrePost=true&events=div%7Csplit%7Cearn&corsDomain=finance.yahoo.com"
        //println(url)
        var text = URL(url).openStream().use {
            IOUtils.toString(it, StandardCharsets.UTF_8)
        }
        //println(text)
        try {
            var chart = OBJECT_MAPPER.readValue(text, YahooResponse::class.java).chart
            cache = chart
            return chart
        } catch (e: Exception) {
            throw e
        }

    }

    fun toObservable(interval: Interval, since: Calendar): Observable<MarketQuote> {
        return loadAndPublish(interval, since).toObservable()
//        return Observable.create { subscriber ->
//            var lastTimestamp = loadAndPublish(interval, since, since, subscriber)
//            Executors.newScheduledThreadPool(1).scheduleAtFixedRate({
//
//                val back2h =  calendarAgo(Duration.ofHours(2))
//
//                lastTimestamp = loadAndPublish(interval, back2h, lastTimestamp, subscriber)
//            }, 1, 1, TimeUnit.SECONDS)
//        }
    }

    private fun loadAndPublish(interval: Interval, from: Calendar): List<MarketQuote> {
        try {
            val now = calendarAgo(Duration.ofHours(0))

            //println("Loading...")
            val chartData = loadChartData(interval, from, now) //TODO no need to load since "since" all the time. Can push it forward a bit
            return chartData.result[0].quotes
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun loadAndPublish(interval: Interval, from: Calendar, lastTimestamp: Calendar, subscriber: ObservableEmitter<MarketQuote>): Calendar {
        try {
            val now = calendarAgo(Duration.ofHours(0))

            //println("Loading...")
            val chartData = loadChartData(interval, from, now) //TODO no need to load since "since" all the time. Can push it forward a bit
            chartData.result[0].quotes
                    .filter {
                        it.timestamp.after(lastTimestamp)
                    }
                    .forEach {
                        //println(it.timestamp)
                        subscriber.onNext(it)
                    }
            return chartData.result[0].quotes.lastOrNull()?.timestamp ?: lastTimestamp
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}