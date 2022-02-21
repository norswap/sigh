package norswap.RPN;

import norswap.autumn.Autumn;
import norswap.autumn.ParseOptions;
import norswap.autumn.ParseResult;
import norswap.autumn.positions.LineMap;
import norswap.autumn.positions.LineMapString;
import norswap.RPN.ast.*;
import norswap.RPN.interpreter.Interpreter;
import norswap.utils.IO;
import java.nio.file.Paths;
import norswap.uranium.Reactor;
import norswap.RPN.ast.NodeRPN;
import norswap.utils.visitors.Walker;

import static norswap.utils.Util.cast;

public final class Test
{
    public static void main (String[] args) {
//         String file = "fizzbuzz.si";
        String file = "example.rpn";
        String path = Paths.get("src/norswap/RPN/examples/", file).toAbsolutePath().toString();
        String src = IO.slurp(path);
        RPNGrammar grammar = new RPNGrammar();
        ParseOptions options = ParseOptions.builder().recordCallStack(true).get();
        ParseResult result = Autumn.parse(grammar.root, src, options);
        LineMap lineMap = new LineMapString(path, src);
        System.out.println(result.toString(lineMap, false));

        if (!result.fullMatch)
            return;

        MainNode tree = cast(result.topValue());
        Reactor reactor = new Reactor();
         Walker<NodeRPN> walker = RPNSemantic.createWalker(reactor);
         walker.walk(tree);
         reactor.run();

         if (!reactor.errors().isEmpty()) {
             System.out.println(reactor.reportErrors(it ->
                 it.toString() + " (" + ((NodeRPN) it).span.startString(lineMap) + ")"));

             // Alternatively, print the whole tree:
             // System.out.println(
             //     AttributeTreeFormatter.formatWalkFields(tree, reactor, SighNode.class));
             return;
         }

         Interpreter interpreter = new Interpreter(tree.content);
         interpreter.run();
        System.out.println("success");

    }
}
