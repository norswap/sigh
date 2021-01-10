package norswap.sigh.types;

public final class ArrayType extends Type
{
    public final Type componentType;

    public ArrayType (Type componentType) {
        this.componentType = componentType;
    }

    @Override public String name() {
        return componentType.toString() + "[]";
    }

    @Override public boolean equals (Object o) {
        return this == o || o instanceof ArrayType && componentType.equals(o);
    }

    @Override public int hashCode () {
        return componentType.hashCode();
    }
}
