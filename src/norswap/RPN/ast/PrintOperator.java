package norswap.RPN.ast;

public class PrintOperator extends UnaryOperator {
    public PrintOperator(NodeRPN operand) {
        super("print", operand);
    }

    public int value() {
        return operand.value();
    }
}
