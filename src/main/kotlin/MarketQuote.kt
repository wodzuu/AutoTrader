import java.util.*

data class MarketQuote(val timestamp: Calendar, val close: Double, val open: Double, val volume: Long) {
    override fun toString(): String {
        return "${DEFAULT_DATE_FORMAT.format(timestamp.time)} close=$close volume=$volume"
    }
}