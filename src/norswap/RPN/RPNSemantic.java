package norswap.RPN;

import norswap.RPN.ast.*;
import norswap.uranium.Reactor;
import norswap.uranium.SemanticError;
import norswap.utils.visitors.ReflectiveFieldWalker;
import norswap.utils.visitors.Walker;

import static norswap.utils.visitors.WalkVisitType.POST_VISIT;
import static norswap.utils.visitors.WalkVisitType.PRE_VISIT;


public class RPNSemantic {
        private final Reactor reactor;

        private int stackSize = 0;

        private RPNSemantic(Reactor reactor) {
                this.reactor = reactor;
        }

        public static Walker<NodeRPN> createWalker(Reactor reactor) {
            ReflectiveFieldWalker<NodeRPN> walker = new ReflectiveFieldWalker<>(NodeRPN.class, PRE_VISIT, POST_VISIT);
            RPNSemantic semantic = new RPNSemantic(reactor);

            walker.register(IntegerNode.class, PRE_VISIT, semantic::integer);
            walker.register(UnaryOperator.class, PRE_VISIT, semantic::unary_operator);
            walker.register(BinaryOperator.class, PRE_VISIT, semantic::binary_operator);
            walker.register(MainNode.class, PRE_VISIT, node -> {});

            walker.registerFallback(POST_VISIT, node -> {});

            return walker;
        }

        private void integer(IntegerNode node) {
            stackSize++;
        }

        private void unary_operator(UnaryOperator node) {
            if (node.name == Operators.PRINT) {
                if (stackSize == 0) {
                    reactor.error(new SemanticError("You cannot print an empty stack.", null, node));
                }
                return;
            } else if (node.name == Operators.POP) {
                if (stackSize == 0) {
                    reactor.error(new SemanticError("You cannot POP an empty stack.", null, node));
                } else if (stackSize == 1) {
                    System.out.println("WARNING : You're performing a POP operation on a stack of size 1. This is probably not what you want.");
                }
                stackSize--;
            }
        }

        private void binary_operator(BinaryOperator node) {
            if (stackSize < 2) {
                reactor.error(new SemanticError("You need at least two elements on your stack to perform a " + node.name, null, node));
            }
            stackSize--;
        }


}
