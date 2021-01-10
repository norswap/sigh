package norswap.sigh;

import norswap.autumn.Autumn;
import norswap.autumn.ParseOptions;
import norswap.autumn.ParseResult;
import norswap.autumn.positions.LineMapString;
import norswap.sigh.ast.Node;
import norswap.uranium.AttributeTreeFormatter;
import norswap.uranium.Reactor;
import norswap.utils.IO;
import norswap.utils.visitors.ReflectiveFieldWalker;
import norswap.utils.visitors.Walker;

import java.nio.file.Paths;

import static norswap.utils.Util.cast;
import static norswap.utils.visitors.WalkVisitType.*;

public final class Test
{
    public static void main (String[] args) {
//         String file = "fizzbuzz.si";
        String file = "kitchensink.si";
        String path = Paths.get("examples/", file).toAbsolutePath().toString();
        String src = IO.slurp(path);
        SighGrammar grammar = new SighGrammar();
        ParseOptions options = ParseOptions.builder().record_call_stack(true).get();
        ParseResult result = Autumn.parse(grammar.root, src, options);
        System.out.println(result.toString(new LineMapString(src), false, path));

        if (!result.full_match)
            return;

        Node root = cast(result.value_stack.peek());
        Reactor reactor = new Reactor();
        Walker<Node> walker = SemanticAnalysis.createWalker(reactor);
        walker.walk(root);
        reactor.run();

        System.out.println(AttributeTreeFormatter.format(root, reactor,
            new ReflectiveFieldWalker<>(Node.class, PRE_VISIT, POST_VISIT)));
    }
}
