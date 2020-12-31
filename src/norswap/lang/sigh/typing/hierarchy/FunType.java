package norswap.lang.sigh.typing.hierarchy;

import norswap.utils.NArrays;
import java.util.Arrays;

public final class FunType extends Type
{
    public final Type return_type;
    public final Type[] param_types;

    public FunType (Type return_type, Type... param_types) {
        this.return_type = return_type;
        this.param_types = param_types;
    }

    @Override public String name() {
        String[] params = NArrays.map(param_types, new String[0], Type::name);
        return String.format("(%s) -> %s", String.join(",", params), return_type);
    }

    @Override public boolean equals (Object o) {
        if (this == o) return true;
        if (!(o instanceof FunType)) return false;
        FunType other = (FunType) o;

        return return_type.equals(other.return_type)
                && Arrays.equals(param_types, other.param_types);
    }

    @Override public int hashCode () {
        return 31 * return_type.hashCode() + Arrays.hashCode(param_types);
    }
}
