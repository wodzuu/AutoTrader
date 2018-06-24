import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*

val DEFAULT_DATE_FORMAT = SimpleDateFormat( "yyyy-MM-dd HH:mm:ss ZZZ")
val NEW_YORK_TIMEZONE = TimeZone.getTimeZone("America/New_York")!!
val OBJECT_MAPPER = jacksonObjectMapper()

fun calendarAgo(offset: Duration): Calendar {
    val calendar = Calendar.getInstance(NEW_YORK_TIMEZONE)
    calendar.add(Calendar.SECOND, -offset.get(ChronoUnit.SECONDS).toInt())
    return calendar
}

fun initGlobals() {
    OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    DEFAULT_DATE_FORMAT.timeZone = NEW_YORK_TIMEZONE
}