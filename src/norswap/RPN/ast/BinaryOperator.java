package norswap.RPN.ast;

import norswap.autumn.positions.Span;
public class BinaryOperator extends OperatorNode
{
    public final NodeRPN op1;
    public final NodeRPN op2;

    public BinaryOperator(Span span, Operators name)
    {
        super(span, name);
        this.op1 = null;
        this.op2 = null;
    }

    public String contents(){
        return name.toString();
    };
}
