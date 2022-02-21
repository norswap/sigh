package norswap.RPN.ast;

import norswap.autumn.positions.Span;

public class UnaryOperator extends OperatorNode
{
    public final NodeRPN operand;

    public UnaryOperator(Span span, Operators name)
    {
        super(span, name);
        this.operand = null;
    }

    public String contents()
    {
        return name.toString();
    }
}