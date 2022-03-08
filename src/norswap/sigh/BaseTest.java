package norswap.sigh;

import norswap.autumn.Autumn;
import norswap.autumn.ParseOptions;
import norswap.autumn.ParseResult;
import norswap.autumn.positions.LineMap;
import norswap.autumn.positions.LineMapString;
import norswap.sigh.ast.SighNode;
import norswap.sigh.interpreter.Interpreter;
import norswap.uranium.Reactor;
import norswap.utils.IO;
import norswap.utils.visitors.Walker;
import java.nio.file.Paths;

import static norswap.utils.Util.cast;

public final class BaseTest
{

    public static void main(String[] args) {

        // Reading file
        String file = "hello.si";
        String path = Paths.get("examples/", file).toAbsolutePath().toString();
        String src = IO.slurp(path);

        BaseGrammar grammar = new BaseGrammar();
        ParseOptions options = ParseOptions.builder().recordCallStack(true).get();
        ParseResult result = Autumn.parse(grammar.root, src, options);
        LineMap lineMap = new LineMapString(path, src);
        System.out.println("[PARSING] "+result.toString(lineMap, false));

        if (!result.fullMatch)
            return;

        //walk(result, lineMap);
    }

    public static void walk(ParseResult result, LineMap lineMap) {

        // Walking the code
        SighNode tree = cast(result.topValue());
        Reactor reactor = new Reactor();
        Walker<SighNode> walker = SemanticAnalysis.createWalker(reactor);
        walker.walk(tree);
        reactor.run();

        // Handling errors
        if (!reactor.errors().isEmpty()) {
            System.out.println(reactor.reportErrors(it ->
                it.toString() + " (" + ((SighNode) it).span.startString(lineMap) + ")"));

            // Alternatively, print the whole tree:
            // System.out.println(
            //     AttributeTreeFormatter.formatWalkFields(tree, reactor, SighNode.class));
            return;
        }

        // Interpreting
        //interpret(result, reactor);
    }

    public static void interpret(ParseResult result, Reactor reactor) {

        // Getting tree
        SighNode tree = cast(result.topValue());

        // Interpreting
        Interpreter interpreter = new Interpreter(reactor);
        interpreter.interpret(tree);
        System.out.println();
        System.out.println("[INTERPRETER] Successfully ran the code.");
    }
}
