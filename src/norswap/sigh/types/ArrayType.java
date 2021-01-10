package norswap.sigh.types;

public final class ArrayType extends Type
{
    public final Type component_type;

    public ArrayType (Type component_type) {
        this.component_type = component_type;
    }

    @Override public String name() {
        return component_type.toString() + "[]";
    }

    @Override public boolean equals (Object o) {
        return this == o || o instanceof ArrayType && component_type.equals(o);
    }

    @Override public int hashCode () {
        return component_type.hashCode();
    }
}
