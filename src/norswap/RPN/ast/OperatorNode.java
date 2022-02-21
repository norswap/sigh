package norswap.RPN.ast;


import norswap.autumn.positions.Span;

public abstract class OperatorNode extends NodeRPN {
    public final Operators name;

    public OperatorNode(Span span, Operators name) {
        super(span);
        this.name = name;
    }
}
