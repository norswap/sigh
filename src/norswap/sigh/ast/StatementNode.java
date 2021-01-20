package norswap.sigh.ast;

import norswap.autumn.positions.Span;

public abstract class StatementNode extends SighNode {
    public StatementNode (Span span) {
        super(span);
    }
}
