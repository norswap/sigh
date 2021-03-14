package norswap.sigh.interpreter;

/**
 * Simple wrapper for exceptions thrown by the interpreter for the benefit of the user of the
 * interpreter (i.e. to mark exceptions that aren't caused by a bug in the interpreter, but
 * are thrown on purpose).
 */
final class PassthroughException extends RuntimeException {
    public PassthroughException (Throwable cause) {
        super(cause);
    }
}
