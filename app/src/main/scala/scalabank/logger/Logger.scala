package scalabank.logger

import scalabank.utils.TimeFormatter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.collection.mutable.ListBuffer

object Logger:
    type Event = String
    private val savedEventsList = ListBuffer[Event]()
    private val prefixFormatter = PrefixFormatter()
    def log(event: Event): Unit = println(event)
    def save(event: Event): Unit = savedEventsList += prefixFormatter.getStandardPrefixFormatter + event
    def getSize(): Int = savedEventsList.size
    def reset(): Unit = savedEventsList.clear()
    def logAll(): Unit =
        savedEventsList.foreach(e => Logger.log(e))
        Logger.reset()