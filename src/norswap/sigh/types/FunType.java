package norswap.sigh.types;

import norswap.utils.NArrays;
import java.util.Arrays;

public final class FunType extends Type
{
    public final Type returnType;
    public final Type[] paramTypes;

    public FunType (Type returnType, Type... paramTypes) {
        this.returnType = returnType;
        this.paramTypes = paramTypes;
    }

    @Override public String name() {
        String[] params = NArrays.map(paramTypes, new String[0], Type::name);
        return String.format("(%s) -> %s", String.join(",", params), returnType);
    }

    @Override public boolean equals (Object o) {
        if (this == o) return true;
        if (!(o instanceof FunType)) return false;
        FunType other = (FunType) o;

        return returnType.equals(other.returnType)
                && Arrays.equals(paramTypes, other.paramTypes);
    }

    @Override public int hashCode () {
        return 31 * returnType.hashCode() + Arrays.hashCode(paramTypes);
    }
}
