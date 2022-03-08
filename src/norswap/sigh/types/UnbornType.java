package norswap.sigh.types;

public final class UnbornType extends Type
{
    public final Type componentType;

    public UnbornType (Type componentType) {
        this.componentType = componentType;
    }

    @Override public String name() {
        return "Unborn<" + componentType.toString() + ">";
    }

    @Override public boolean equals (Object o) {
        return this == o || o instanceof UnbornType && componentType.equals(o);
    }
}
