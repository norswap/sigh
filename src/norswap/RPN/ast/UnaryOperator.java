package norswap.RPN.ast;

public abstract class UnaryOperator extends OperatorNode
{
    public final NodeRPN operand;

    public UnaryOperator(String name, NodeRPN operand)
    {
        super(name);
        this.operand = operand;
    }

    public abstract int value();
}