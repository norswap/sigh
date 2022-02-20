package norswap.RPN.ast;

public abstract class BinaryOperator extends OperatorNode
{
    public final NodeRPN op1;
    public final NodeRPN op2;

    public BinaryOperator(String name, NodeRPN op1, NodeRPN op2)
    {
        super(name);
        this.op1 = op1;
        this.op2 = op2;
    }

    public abstract int value();
}
