package scalabank.logger

import java.io.PrintStream

/**
 * Trait defining the Logger functionality.
 */
trait Logger:
    /**
     * Disables the logger.
     */
    def disable(): Unit

    /**
     * Enables the logger.
     */
    def enable(): Unit

    /**
     * Checks if the logger is enabled.
     *
     * @return true if the logger is enabled, false otherwise.
     */
    def isEnabledNow: Boolean

    /**
     * Logs a message.
     *
     * @param string the message to be logged.
     */
    def log(string: String): Unit

    /**
     * Sets the output media to a file.
     *
     * @param fileName the name of the file to which logs will be written.
     */
    def setOutputMediaToFile(fileName: String): Unit

    /**
     * Sets the output media to the console.
     */
    def setOutputMediaToConsole(): Unit

    /**
     * Gets the prefix formatter.
     *
     * @return the current PrefixFormatter instance.
     */
    def getPrefixFormatter: PrefixFormatter

/**
 * Trait for classes that depend on a Logger (cake pattern).
 */
trait LoggerDependency:
    /**
     * The logger instance.
     */
    val logger: Logger

/**
 * Implementation of the Logger trait.
 */
class LoggerImpl extends Logger:
    private var isEnabled = true
    private var outputMedia: PrintStream = System.out
    private val prefixFormatter: PrefixFormatter = PrefixFormatter()
    
    override def disable(): Unit = isEnabled = false
    
    override def enable(): Unit = isEnabled = true
    
    override def isEnabledNow: Boolean = isEnabled

    override def log(string: String): Unit =
        if isEnabled then outputMedia.println(prefixFormatter.getPrefixWithCurrentTime + string)

    override def setOutputMediaToFile(fileName: String): Unit = outputMedia = PrintStream(fileName + ".txt")

    override def setOutputMediaToConsole(): Unit = outputMedia = System.out
    
    override def getPrefixFormatter: PrefixFormatter = prefixFormatter

