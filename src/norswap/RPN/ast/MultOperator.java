package norswap.RPN.ast;

public class MultOperator extends BinaryOperator
{
    public MultOperator(NodeRPN op1, NodeRPN op2)
    {
        super("*", op1, op2);
    }

    public int value()
    {
        return op1.value() * op2.value();
    }
}
