package norswap.sigh.interpreter;

/**
 * Uninstantiable class (only inhabited by {@code null}, used to mark nodes that should not evaluate
 * to a value in the interpreter - typically statements.
 */
public final class Void {
    private Void() {}
}
