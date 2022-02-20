package norswap.RPN.ast;

public class IntegerNode implements NodeRPN
{
    public final int value;

    public IntegerNode(String value)
    {
        this.value = Integer.parseInt(value);
    }

    public int value()
    {
        return value;
    }
}