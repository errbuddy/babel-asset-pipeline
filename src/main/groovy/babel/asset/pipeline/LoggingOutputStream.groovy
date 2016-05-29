package babel.asset.pipeline

import java.util.logging.Level
import java.util.logging.Logger

/**
 * mostly inspired by http://sysgears.com/articles/how-to-redirect-stdout-and-stderr-writing-to-a-log4j-appender/
 */
public class LoggingOutputStream extends OutputStream {

    /**
     * Default number of bytes in the buffer.
     */
    private static final int DEFAULT_BUFFER_LENGTH = 2048

    /**
     * Indicates stream state.
     */
    private boolean hasBeenClosed = false

    /**
     * Internal buffer where data is stored.
     */
    private StringBuffer stringBuffer

    /**
     * The logger to write to.
     */
    private Logger log

    /**
     * The log level.
     */
    private Level level

    /**
     * Creates the Logging instance to flush to the given logger.
     *
     * @param log the Logger to write to
     * @param level the log level
     * @throws IllegalArgumentException in case if one of arguments is  null.
     */
    public LoggingOutputStream(final Logger log, final Level level) throws IllegalArgumentException {
        if (log == null || level == null) {
            throw new IllegalArgumentException("Logger or log level must be not null")
        }
        this.log = log
        this.level = level
        stringBuffer = new StringBuffer()
    }

    /**
     * Writes the specified byte to this output stream.
     *
     * @param b the byte to write
     * @throws IOException if an I/O error occurs.
     */
    public void write(final int b) throws IOException {
        if (hasBeenClosed) {
            throw new IOException("The stream has been closed.")
        }
        // don't log nulls
        if (b == 0) {
            return
        }

        stringBuffer.append(new String((byte) (b & 0xff)))
        if (stringBuffer.toString().endsWith("\n")) {
            flush()
        }
    }

    /**
     * Flushes this output stream and forces any buffered output
     * bytes to be written out.
     */
    public void flush() {
        try {
        if (stringBuffer.size() > 1) {
            stringBuffer.deleteCharAt(stringBuffer.size() - 1)
            if (stringBuffer.size()) {
                log.log(level, stringBuffer.toString())
            }
        }
        } catch (ignore) {
            // due to the process starting and stopping we might run into concurrency issues here    
        } finally {
            stringBuffer = new StringBuffer()
        }
    }

    /**
     * Closes this output stream and releases any system resources
     * associated with this stream.
     */
    public void close() {
        flush()
        hasBeenClosed = true
    }
}