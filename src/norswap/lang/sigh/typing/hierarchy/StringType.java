package norswap.lang.sigh.typing.hierarchy;

public final class StringType extends Type {
    public static final StringType INSTANCE = new StringType();
    private StringType() {}

    @Override public String name() {
        return "String";
    }
}
