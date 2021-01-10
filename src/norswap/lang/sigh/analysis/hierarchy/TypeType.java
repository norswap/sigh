package norswap.lang.sigh.analysis.hierarchy;

public final class TypeType extends Type {
    public static final TypeType INSTANCE = new TypeType();
    private TypeType() {}

    @Override public String name() {
        return "Type";
    }
}
