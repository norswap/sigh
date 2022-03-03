package norswap.sigh;

import norswap.autumn.Autumn;
import norswap.autumn.ParseOptions;
import norswap.autumn.ParseResult;
import norswap.sigh.ast.SighNode;
import norswap.sigh.ast.RootNode;
import norswap.sigh.interpreter.Interpreter;
import norswap.uranium.AttributeTreeFormatter;
import norswap.uranium.Reactor;
import norswap.uranium.SemanticError;
import norswap.utils.visitors.ReflectiveFieldWalker;
import norswap.utils.visitors.Walker;
import java.text.ParseException;
import java.util.Set;

import static norswap.utils.visitors.WalkVisitType.POST_VISIT;
import static norswap.utils.visitors.WalkVisitType.PRE_VISIT;

public final class SighRunner
{
    // ---------------------------------------------------------------------------------------------

    private final SighGrammar grammar = new SighGrammar();

    private final ParseOptions parseOptions = ParseOptions.builder()
            .wellFormednessCheck(false)
            .get();

    // ---------------------------------------------------------------------------------------------

    public Object run(String input) {
        Reactor reactor = new Reactor();
        Walker<SighNode> walker = SemanticAnalysis.createWalker(reactor);
        Interpreter interpreter = new Interpreter(reactor);

        ParseResult result = Autumn.parse(grammar.root, input, parseOptions);
        if (!result.fullMatch) {
            // TODO improve
            throw new RuntimeException(new ParseException(result.toString(), result.errorOffset));
        }

        Object top = result.topValue();
        if (!(top instanceof RootNode)) {
            throw new Error("parsing produced unexpected value: " + top);
        }

        RootNode root = result.topValue();
        walker.walk(root);
        reactor.run();
        Set<SemanticError> errors = reactor.errors();

        if (!errors.isEmpty()) {
            // TODO improve
            for (SemanticError error: errors) {
                System.err.println(error);
            }
            String tree = AttributeTreeFormatter.format(root, reactor,
                    new ReflectiveFieldWalker<>(SighNode.class, PRE_VISIT, POST_VISIT));
            System.err.println(tree);
            throw new AssertionError("semantic errors");
        }

        return interpreter.interpret(root);
    }

    // ---------------------------------------------------------------------------------------------
}
