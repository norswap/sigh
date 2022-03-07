package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.uranium.Attribute;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;

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

    private Field[] getFields() {
        return Arrays.stream(this.getClass().getFields())
            .filter(f -> Modifier.isPublic(f.getModifiers()))
            .toArray(Field[]::new);
    }

    // ---------------------------------------------------------------------------------------------

    // NOTE: hashCode and equals are implemented reflectively
    //    This is not ideal for performance, but these methods are currently only used in tests.
    //    Ideally we would replace all the AST classes by a small framework that generates code
    //    for them.

    @Override public int hashCode() {
        try {
            Field[] fields = getFields();
            int hash = 7;
            for (Field field: fields) {
                hash *= 31;
                Object value = field.get(this);
                if (value != null)
                    hash += value.hashCode();
            }
            return hash;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Two node are equals if they are of the same class and all of their public fields are equal,
     * excepted {@link #span} which is allowed to be different. If you require {@link #span} to be
     * identical, use {@link #equals(Object, boolean)}.
     *
     * <p>This method uses reflection to implement the comparison.
     */
    @Override public boolean equals (Object obj) {
        return equals(obj, true);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Two node are equals if they are of the same class and all of their public fields are equal,
     * excepted {@link #span} which is only checked if {@code ignoreSpan} is {@code false}.
     *
     * <p>This method uses reflection to implement the comparison.
     */
    public boolean equals (Object obj, boolean ignoreSpan) {
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;
        try {
            Field[] fields = getFields();
            for (Field field: fields) {
                if (ignoreSpan && field.getName().equals("span"))
                    continue;
                if (!Objects.equals(field.get(this), field.get(obj)))
                    return false;
            }
            return true;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------------------------------------------------------------------------------------
}
