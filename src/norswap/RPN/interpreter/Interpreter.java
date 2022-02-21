package norswap.RPN.interpreter;

import java.util.Stack;
import java.util.List;
import norswap.RPN.ast.*;

public class Interpreter {
    private Stack<Integer> stack;
    private List<NodeRPN> ast;

    public Interpreter(List<NodeRPN> ast) {
        this.ast = ast;
        this.stack = new Stack<Integer>();
    }

    public void run() {
        for (NodeRPN node: ast) {
            if (node instanceof IntegerNode) {
                stack.push(((IntegerNode) node).value);
            } else if (node instanceof BinaryOperator) {
                int right = stack.pop();
                int left = stack.pop();
                if (((BinaryOperator) node).name == Operators.ADD) {
                    stack.push(left + right);
                } else if (((BinaryOperator) node).name == Operators.MULT) {
                    stack.push(left * right);
                }
            } else if (node instanceof UnaryOperator) {
                int value = stack.pop();
                if (((UnaryOperator) node).name == Operators.PRINT) {
                    System.out.println(value);
                    stack.push(value);
                }
            }
        }
        int result = stack.pop();
        System.out.println("Result: " + result);
    }

}
