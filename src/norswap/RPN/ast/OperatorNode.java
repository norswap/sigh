package norswap.RPN.ast;

public abstract class OperatorNode implements NodeRPN {
    public final String name;

    public OperatorNode(String name) {
        this.name = name;
    }
    public abstract int value();
}
