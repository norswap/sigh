package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.sigh.types.BoolType;
import norswap.sigh.types.FloatType;
import norswap.sigh.types.IntType;
import norswap.sigh.types.StringType;
import norswap.sigh.types.Type;
import norswap.utils.Util;

public final class SimpleTypeNode extends TypeNode
{
    public final String name;

    public SimpleTypeNode (Span span, Object name) {
        super(span);
        this.name = Util.cast(name, String.class);
    }

    @Override
    public Type getType() {

        switch (name) {
            case "Bool": return BoolType.INSTANCE;
            case "Float": return FloatType.INSTANCE;
            case "Int": return IntType.INSTANCE;
            case "String": return StringType.INSTANCE;
        }

        return null;
    }

    @Override public String contents () {
        return name;
    }
}
