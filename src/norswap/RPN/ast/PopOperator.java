package norswap.RPN.ast;

public class PopOperator extends BinaryOperator
{
    public PopOperator(NodeRPN op1, NodeRPN op2)
    {
        super("pop", op1, op2);
    }

    public int value()
    {
        return op1.value();
    }
}
