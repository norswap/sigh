package norswap.RPN.ast;

import norswap.autumn.positions.Span;

public class IntegerNode extends NodeRPN
{
    public final int value;

    public IntegerNode(Span span, String value)
    {
        super(span);
        this.value = Integer.parseInt(value);
    }

    public String contents()
    {
        return Integer.toString(value);
    }
}