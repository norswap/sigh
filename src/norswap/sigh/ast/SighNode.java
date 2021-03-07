package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.uranium.Attribute;

public abstract class SighNode
{
    // ---------------------------------------------------------------------------------------------

    public final Span span;

    protected SighNode (Span span) {
        this.span = span;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns an attribute with the given name on this node.
     */
    public final Attribute attr (String name) {
        return new Attribute(this, name);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * The size that the string returned by {@link #toString} should not exceed.
     */
    public static int TO_STRING_CUTOFF = 40;

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a <b>brief</b> overview of the content of the node, suitable to be printed
     * in a single line.
     */
    public abstract String contents();

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a brief string description of the node, whose size does not exceed {@link
     * #TO_STRING_CUTOFF}.
     */
    @Override public final String toString ()
    {
        String klass = getClass().getSimpleName().replace("Node", "");
        String contents = contents();
        return klass.length() + contents.length() + 2 <= TO_STRING_CUTOFF
            ? String.format("%s(%s)", klass, contents)
            : klass + "(...)";
    }

    // ---------------------------------------------------------------------------------------------

    int contentsBudget () {
        return TO_STRING_CUTOFF - getClass().getSimpleName().length() - 2;
        // 2 == "()".length() - "Node".length
    }

    // ---------------------------------------------------------------------------------------------
}
