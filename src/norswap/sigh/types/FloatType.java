package norswap.sigh.types;

public final class FloatType extends Type
{
    public static final FloatType INSTANCE = new FloatType();
    private FloatType () {}

    @Override public boolean isPrimitive () {
        return true;
    }

    @Override public String name() {
        return "Float";
    }
}
