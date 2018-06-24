abstract class TradeState(open val wallet: Wallet) {
    abstract fun next(tick: MarketQuote): TradeState
}

data class LongTradeState(val buyPrice: Double, val config: AlgorithmConfiguration, override val wallet: Wallet) : TradeState(wallet) {
    override fun next(tick: MarketQuote): TradeState =
            if (tick.close > buyPrice + config.profitToSell) {
                //sell
                EnterShortTradeState(config, wallet.sell(tick.close))
            } else {
                LongTradeState(buyPrice, config, wallet)
            }
}

data class ShortTradeState(val localMaximum: Double, val config: AlgorithmConfiguration, override val wallet: Wallet) : TradeState(wallet) {
    override fun next(tick: MarketQuote): TradeState =
            if (tick.close < localMaximum - config.priceDropToBuy) {
                // buy
                EnterLongTradeState(config, wallet)
            } else {
                ShortTradeState(Math.max(tick.close, localMaximum),config,  wallet)
            }
}

data class EnterShortTradeState(val config: AlgorithmConfiguration, override val wallet: Wallet) : TradeState(wallet) {
    override fun next(tick: MarketQuote): TradeState = ShortTradeState(tick.close, config, wallet.sell(tick.open))
}

data class EnterLongTradeState(val config: AlgorithmConfiguration, override val wallet: Wallet) : TradeState(wallet) {
    override fun next(tick: MarketQuote): TradeState = LongTradeState(tick.close, config, wallet.buy(tick.open))
}

data class Wallet(val balance: Double, val holdings: Int, val broker: Broker, val operations: Int = 0){
    fun sell(price: Double): Wallet {
        val income = price*holdings
        return copy(balance = balance+income-(income*broker.commision), holdings = 0, operations = operations+1)
    }
    fun buy(price: Double): Wallet {
        val volume: Int = Math.floor(balance/price).toInt()
        val outcome = volume*price
        return copy(balance = balance - outcome - outcome*broker.commision, holdings = volume, operations = operations+1)
    }
}

abstract class Broker(val commision: Double)

object Nordnet:Broker(0.002)

data class AlgorithmConfiguration(val profitToSell: Double, val priceDropToBuy: Double)