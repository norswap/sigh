package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.sigh.types.Type;

public abstract class TypeNode extends SighNode {
    public TypeNode (Span span) {
        super(span);
    }

    public abstract Type getType ();
}
