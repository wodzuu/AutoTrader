import io.reactivex.rxkotlin.subscribeBy
import java.time.Duration

fun main(args: Array<String>) {
    initGlobals()

    var best = 0.0
    for (profitToSell in 1..150) {
        for (priceDropToBuy in 1..150) {
            val config = AlgorithmConfiguration(profitToSell / 10.0, priceDropToBuy / 10.0)
            val result = testConfig(config)
            if (result > best) {
                best = result
                println("Best: $config -> $best")
            }
        }
    }
    println("Best: $best")


    //println("Best: $config -> ${testConfig(config)}")

//    Stock("TTWO")
//            .toObservable(Interval.ONE_HOUR, start)
//            .scan(Pair<MarketQuote, TradeState>(MarketQuote(calendarAgo(Duration.ofDays(10*365)), 0.0, 0), ShortTradeState(-1.0, config, wallet))) { acc, v -> Pair(v, acc.second.next(v)) }
//            .subscribeBy(
//                    onNext = { println("$it -> ${it.second.wallet.balance+it.second.wallet.holdings*it.first.close}") },
//                    onError = { it.printStackTrace() },
//                    onComplete = { println("Done!") }
//            )
}

fun testConfig(config: AlgorithmConfiguration): Double {
    //println(" - testing $config")
    val start = calendarAgo(Duration.ofDays(365))
    val wallet = Wallet(1000.0, 0, Nordnet)

    var best: Double = 0.0
    Stock("TTWO")
            .toObservable(Interval.ONE_DAY, start)
            .scan(Pair<MarketQuote, TradeState>(MarketQuote(calendarAgo(Duration.ofDays(10 * 365)), 0.0, 0.0, 0), ShortTradeState(-1.0, config, wallet))) { acc, v -> Pair(v, acc.second.next(v)) }
            .subscribeBy(
                    onNext = { best = it.second.wallet.balance + it.second.wallet.holdings * it.first.close },
                    onError = { it.printStackTrace() }
            )
    return best
}