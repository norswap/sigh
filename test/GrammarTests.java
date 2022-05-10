import norswap.autumn.AutumnTestFixture;
import norswap.sigh.SighGrammar;
import norswap.sigh.ast.*;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static norswap.sigh.ast.BinaryOperator.*;

public class GrammarTests extends AutumnTestFixture {
    // ---------------------------------------------------------------------------------------------

    private final SighGrammar grammar = new SighGrammar();
    private final Class<?> grammarClass = grammar.getClass();

    // ---------------------------------------------------------------------------------------------

    private static IntLiteralNode intlit (long i) {
        return new IntLiteralNode(null, i);
    }

    private static FloatLiteralNode floatlit (double d) {
        return new FloatLiteralNode(null, d);
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testLiteralsAndUnary () {
        rule = grammar.expression;

        successExpect("42", intlit(42));
        successExpect("42.0", floatlit(42d));
        successExpect("\"hello\"", new StringLiteralNode(null, "hello"));
        successExpect("(42)", new ParenthesizedNode(null, intlit(42)));
        successExpect("[1, 2, 3]", new ArrayLiteralNode(null, asList(intlit(1), intlit(2), intlit(3))));
        successExpect("true", new ReferenceNode(null, "true"));
        successExpect("false", new ReferenceNode(null, "false"));
        successExpect("null", new ReferenceNode(null, "null"));
        successExpect("!false", new UnaryExpressionNode(null, UnaryOperator.NOT, new ReferenceNode(null, "false")));
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testNumericBinary () {
        successExpect("1 + 2", new BinaryExpressionNode(null, intlit(1), ADD, intlit(2)));
        successExpect("2 - 1", new BinaryExpressionNode(null, intlit(2), SUBTRACT,  intlit(1)));
        successExpect("2 * 3", new BinaryExpressionNode(null, intlit(2), MULTIPLY, intlit(3)));
        successExpect("2 / 3", new BinaryExpressionNode(null, intlit(2), DIVIDE, intlit(3)));
        successExpect("2 % 3", new BinaryExpressionNode(null, intlit(2), REMAINDER, intlit(3)));

        successExpect("1.0 + 2.0", new BinaryExpressionNode(null, floatlit(1), ADD, floatlit(2)));
        successExpect("2.0 - 1.0", new BinaryExpressionNode(null, floatlit(2), SUBTRACT, floatlit(1)));
        successExpect("2.0 * 3.0", new BinaryExpressionNode(null, floatlit(2), MULTIPLY, floatlit(3)));
        successExpect("2.0 / 3.0", new BinaryExpressionNode(null, floatlit(2), DIVIDE, floatlit(3)));
        successExpect("2.0 % 3.0", new BinaryExpressionNode(null, floatlit(2), REMAINDER, floatlit(3)));

        successExpect("2 * (4-1) * 4.0 / 6 % (2+1)", new BinaryExpressionNode(null,
            new BinaryExpressionNode(null,
                new BinaryExpressionNode(null,
                    new BinaryExpressionNode(null,
                        intlit(2),
                        MULTIPLY,
                        new ParenthesizedNode(null, new BinaryExpressionNode(null,
                            intlit(4),
                            SUBTRACT,
                            intlit(1)))),
                    MULTIPLY,
                    floatlit(4d)),
                DIVIDE,
                intlit(6)),
            REMAINDER,
            new ParenthesizedNode(null, new BinaryExpressionNode(null,
                intlit(2),
                ADD,
                intlit(1)))));
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testArrayStructAccess () {
        rule = grammar.expression;
        successExpect("[1][0]", new ArrayAccessNode(null,
            new ArrayLiteralNode(null, asList(intlit(1))), intlit(0)));
        successExpect("[1].length", new FieldAccessNode(null,
            new ArrayLiteralNode(null, asList(intlit(1))), "length"));
        successExpect("p.x", new FieldAccessNode(null, new ReferenceNode(null, "p"), "x"));
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testDeclarations() {
        rule = grammar.statement;

        successExpect("var x: Int = 1", new VarDeclarationNode(null,
            "x", new SimpleTypeNode(null, "Int"), intlit(1)));

        successExpect("struct P {}", new StructDeclarationNode(null, "P", asList()));

        successExpect("struct P { var x: Int; var y: Int }",
            new StructDeclarationNode(null, "P", asList(
                new FieldDeclarationNode(null, "x", new SimpleTypeNode(null, "Int")),
                new FieldDeclarationNode(null, "y", new SimpleTypeNode(null, "Int")))));

        successExpect("fun f (x: Int): Int { return 1 }",
            new FunDeclarationNode(null, "f",
                asList(new ParameterNode(null, "x", new SimpleTypeNode(null, "Int"))),
                new SimpleTypeNode(null, "Int"),
                new BlockNode(null, asList(new ReturnNode(null, intlit(1))))));

        // New test by group 10 to try a no parameter function
        successExpect("fun f (): Int { return 1 }",
            new FunDeclarationNode(null, "f", asList(),
                new SimpleTypeNode(null, "Int"),
                new BlockNode(null, asList(new ReturnNode(null, intlit(1))))));
        // New test by group 10 to try a return String function
        successExpect("fun f (): String { return \"Read carefully the output\" }",
            new FunDeclarationNode(null, "f", asList(),
                new SimpleTypeNode(null, "String"),
                new BlockNode(null, asList(new ReturnNode(null,
                    new StringLiteralNode(null, "Read carefully the output"))))));
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testStatements() {
        rule = grammar.statement;

        successExpect("return", new ReturnNode(null, null));
        successExpect("return 1", new ReturnNode(null, intlit(1)));
        successExpect("print(1)", new ExpressionStatementNode(null,
            new FunCallNode(null, new ReferenceNode(null, "print"), asList(intlit(1)))));
        successExpect("{ return }", new BlockNode(null, asList(new ReturnNode(null, null))));


        successExpect("if true return 1 else return 2", new IfNode(null, new ReferenceNode(null, "true"),
            new ReturnNode(null, intlit(1)),
            new ReturnNode(null, intlit(2))));

        successExpect("if false return 1 else if true return 2 else return 3 ",
            new IfNode(null, new ReferenceNode(null, "false"),
                new ReturnNode(null, intlit(1)),
                new IfNode(null, new ReferenceNode(null, "true"),
                    new ReturnNode(null, intlit(2)),
                    new ReturnNode(null, intlit(3)))));

        successExpect("while 1 < 2 { return } ", new WhileNode(null,
            new BinaryExpressionNode(null, intlit(1), LOWER, intlit(2)),
            new BlockNode(null, asList(new ReturnNode(null, null)))));
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testClassDeclarations() {
        rule = grammar.statement;

        String input1 = "" +
            "class Car {\n" +
            "    var brand: String\n" +
            "    var max_speed: Int\n" +
            "    var test: Bool\n" +
            "\n" +
            "    meth set_max_speed(speed: Int) {\n" +
            "        max_speed = speed\n" +
            "    }\n" +
            "\n" +
            "    meth get_max_speed(): Int {\n" +
            "        return max_speed\n" +
            "    }\n" +
            "\n" +
            "    meth set_brand(name: String) {\n" +
            "        brand = name\n" +
            "    }\n" +
            "\n" +
            "    meth get_brand(): String {\n" +
            "        return brand\n" +
            "    }\n" +
            "}";

        SimpleTypeNode string = new SimpleTypeNode(null, "String");
        SimpleTypeNode integer = new SimpleTypeNode(null, "Int");
        SimpleTypeNode bool = new SimpleTypeNode(null, "Bool");
        SimpleTypeNode none = new SimpleTypeNode(null, "null");

        successExpect(input1,
            new ClassDeclarationNode(null, "Car", asList(
                new FieldDeclarationNode(null, "brand", string),
                    new FieldDeclarationNode(null, "max_speed", integer),
                    new FieldDeclarationNode(null, "test", bool)
            ), asList(
                new ClassFunDeclarationNode(null, "set_max_speed",
                    asList(new ParameterNode(null, "speed", integer)), none,
                    new BlockNode(null, new AssignmentNode(null,
                        new FieldDeclarationNode(null, "brand", string),
                        new ParameterNode(null, "speed", integer))))
            )));

        // Test if the function is well parsed in the grammar
        successExpect("class Car { fun f (x: Int): Int { return 1 } }",
            new ClassDeclarationNode(null, "Car", asList(), asList(
                new FunDeclarationNode(null, "f",
                    asList(new ParameterNode(null, "x", new SimpleTypeNode(null, "Int"))),
                    new SimpleTypeNode(null, "Int"),
                    new BlockNode(null, asList(new ReturnNode(null, intlit(1)))))
        )));

        // Test only with functions
        successExpect("class Car { fun brand (): String { return \"Ferrari\" } fun speed (): Int { return 350 } }",
            new ClassDeclarationNode(null, "Car", asList(), asList(
                new FunDeclarationNode(null, "brand", asList(),
                    new SimpleTypeNode(null, "String"),
                    new BlockNode(null, asList(new ReturnNode(null, new StringLiteralNode(null, "Ferrari"))))),
                new FunDeclarationNode(null, "speed", asList(),
                    new SimpleTypeNode(null, "Int"),
                    new BlockNode(null, asList(new ReturnNode(null, intlit(350)))))
        )));

        // Test only with attributes
        successExpect("class Car { var brand: String var max_speed: Int }",
            new ClassDeclarationNode(null, "Car", asList(
                new FieldDeclarationNode(null, "brand", new SimpleTypeNode(null, "String")),
                new FieldDeclarationNode(null, "max_speed", new SimpleTypeNode(null, "Int"))
            ), asList()));

        // Test with attributes and functions, but no use of the attributes in the function
        successExpect("class Car { var brand: String var max_speed: Int " +
            "fun brand (): String { return \"Ferrari\" } fun speed (): Int { return 350 } }",
            new ClassDeclarationNode(null, "Car", asList(
                new FieldDeclarationNode(null, "brand", new SimpleTypeNode(null, "String")),
                new FieldDeclarationNode(null, "max_speed", new SimpleTypeNode(null, "Int"))
            ), asList(
                new FunDeclarationNode(null, "brand", asList(),
                    new SimpleTypeNode(null, "String"),
                    new BlockNode(null, asList(new ReturnNode(null, new StringLiteralNode(null, "Ferrari"))))),
                new FunDeclarationNode(null, "speed", asList(),
                    new SimpleTypeNode(null, "Int"),
                    new BlockNode(null, asList(new ReturnNode(null, intlit(350)))))
            )));

    }
}
