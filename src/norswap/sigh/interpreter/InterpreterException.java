package norswap.sigh.interpreter;

/**
 * Simple wrapper for exceptions thrown while running the interpreter.
 */
public final class InterpreterException extends RuntimeException {
    public InterpreterException (String message, Throwable cause) {
        super(message, cause);
    }
}
