package norswap.lang.sigh.typing.hierarchy;

public final class VoidType extends Type {
    public static final VoidType INSTANCE = new VoidType();
    private VoidType() {}

    @Override public String name() {
        return "Void";
    }
}
