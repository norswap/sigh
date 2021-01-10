package norswap.sigh.ast;

import norswap.autumn.positions.Span;

public abstract class TypeNode extends Node {
    public TypeNode (Span span) {
        super(span);
    }
}
