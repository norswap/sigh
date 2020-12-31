package norswap.lang.sigh;

import norswap.autumn.DSL;
import norswap.lang.sigh.ast.*;

import static norswap.lang.sigh.ast.BinaryOperator.*;
import static norswap.lang.sigh.ast.UnaryOperator.NOT;

public class SighGrammar extends DSL
{
    // ==== LEXICAL ===========================================================

    public rule line_comment
        = seq("//", seq(not("\n"), any).at_least(0));

    public rule ws_item = choice(
        set(" \t\n\r"),
        line_comment);

    { ws = ws_item.at_least(0); }


    public rule STAR            = word("*")             .token();
    public rule SLASH           = word("/")             .token();
    public rule PERCENT         = word("%")             .token();
    public rule PLUS            = word("+")             .token();
    public rule MINUS           = word("-")             .token();
    public rule LBRACE          = word("{")             .token();
    public rule RBRACE          = word("}")             .token();
    public rule LPAREN          = word("(")             .token();
    public rule RPAREN          = word(")")             .token();
    public rule LSQUARE         = word("[")             .token();
    public rule RSQUARE         = word("]")             .token();
    public rule COLON           = word(":")             .token();
    public rule EQUALS_EQUALS   = word("==")            .token();
    public rule EQUALS          = word("=")             .token();
    public rule BANG_EQUAL      = word("!=")            .token();
    public rule LANGLE_EQUAL    = word("<=")            .token();
    public rule RANGLE_EQUAL    = word(">=")            .token();
    public rule LANGLE          = word("<")             .token();
    public rule RANGLE          = word(">")             .token();
    public rule AMP_AMP         = word("&&")            .token();
    public rule BAR_BAR         = word("||")            .token();
    public rule BANG            = word("!")             .token();
    public rule DOT             = word(".")             .token();
    public rule DOLLAR          = word("$")             .token();
    public rule COMMA           = word(",")             .token();

    public rule _var            = word("var")           .token();
    public rule _fun            = word("fun")           .token();
    public rule _struct         = word("struct")        .token();
    public rule _if             = word("if")            .token();
    public rule _else           = word("else")          .token();
    public rule _while          = word("while")         .token();
    public rule _return         = word("return")        .token();

    public rule number =
        seq(opt('-'), choice('0', digit.at_least(1)));

    public rule integer =
        number
        .push($ -> new IntLiteralNode($.span(), Long.parseLong($.str())))
        .word()
        .token();

    public rule floating =
        seq(number, '.', digit.at_least(1))
        .push($ -> new FloatLiteralNode($.span(), Double.parseDouble($.str())))
        .word()
        .token();

    public rule string_char = choice(
        seq(set('"', '\\').not(), any),
        seq('\\', set("\\nrt")));

    public rule string_content =
        string_char.at_least(0)
        .push($ -> $.str());

    public rule string =
        seq('"', string_content, '"')
        .push($ -> new StringLiteralNode($.span(), $.$[0]))
        .word()
        .token();

    public rule identifier =
        seq(choice(alpha, '_'), choice(alphanum, '_').at_least(0))
        .push($ -> $.str())
        .word()
        .token();
    
    // ==== SYNTACTIC =========================================================
    
    public rule reference =
        identifier
        .push($ -> new ReferenceNode($.span(), $.$[0]));

    public rule constructor =
        seq(DOLLAR, reference)
        .push($ -> new ConstructorNode($.span(), $.$[0]));
    
    public rule simple_type =
        identifier
        .push($ -> new SimpleTypeNode($.span(), $.$[0]));

    public rule paren_expression = lazy(() ->
        seq(LPAREN, this.expression, RPAREN)
        .push($ -> new ParenthesizedNode($.span(), $.$[0])));

    public rule expressions = lazy(() ->
        this.expression.sep(0, COMMA)
        .as_list(ExpressionNode.class));

    public rule array =
        seq(LSQUARE, expressions, RSQUARE)
        .push($ -> new ArrayLiteralNode($.span(), $.$[0]));

    public rule basic_expression = choice(
        constructor,
        reference,
        integer,
        floating,
        string,
        paren_expression,
        array);

    public rule function_args =
        seq(LPAREN, expressions, RPAREN);

    public rule suffix_expression = left_expression()
        .left(basic_expression)
        .suffix(seq(DOT, identifier),
            $ -> new FieldAccessNode($.span(), $.$[0], $.$[1]))
        .suffix(seq(LSQUARE, lazy(() -> this.expression), RSQUARE),
            $ -> new ArrayAccessNode($.span(), $.$[0], $.$[1]))
        .suffix(function_args,
            $ -> new FunCallNode($.span(), $.$[0], $.$[1])).get();

    public rule prefix_expression = right_expression()
        .operand(suffix_expression)
        .prefix(BANG.as_val(NOT),
            $ -> new UnaryExpressionNode($.span(), $.$[0], $.$[1])).get();

    public rule mult_op = choice(
        STAR        .as_val(MULTIPLY),
        SLASH       .as_val(DIVIDE),
        PERCENT     .as_val(REMAINDER));

    public rule add_op = choice(
        PLUS        .as_val(ADD),
        MINUS       .as_val(SUBTRACT));

    public rule cmp_op = choice(
        EQUALS_EQUALS.as_val(EQUALITY),
        BANG_EQUAL  .as_val(NOT_EQUALS),
        LANGLE_EQUAL.as_val(LOWER_EQUAL),
        RANGLE_EQUAL.as_val(GREATER_EQUAL),
        LANGLE      .as_val(LOWER),
        RANGLE      .as_val(GREATER));

    public rule mult_expr = left_expression()
        .operand(prefix_expression)
        .infix(mult_op,
            $ -> new BinaryExpressionNode($.span(), $.$[0], $.$[1], $.$[2])).get();

    public rule add_expr = left_expression()
        .operand(mult_expr)
        .infix(add_op,
            $ -> new BinaryExpressionNode($.span(), $.$[0], $.$[1], $.$[2])).get();

    public rule order_expr = left_expression()
        .operand(add_expr)
        .infix(cmp_op,
            $ -> new BinaryExpressionNode($.span(), $.$[0], $.$[1], $.$[2])).get();

    public rule and_expression = left_expression()
        .operand(order_expr)
        .infix(AMP_AMP.as_val(AND),
            $ -> new BinaryExpressionNode($.span(), $.$[0], $.$[1], $.$[2])).get();

    public rule or_expression = left_expression()
        .operand(and_expression)
        .infix(BAR_BAR.as_val(OR),
            $ -> new BinaryExpressionNode($.span(), $.$[0], $.$[1], $.$[2])).get();

    public rule assignment_expression = right_expression()
        .operand(or_expression)
        .infix(EQUALS.as_val(ASSIGN),
            $ -> new BinaryExpressionNode($.span(), $.$[0], $.$[1], $.$[2])).get();

    public rule expression =
        seq(assignment_expression);

    // Only function calls and assignments are allowed, but parsing accepts everything.
    // Invalid expressions are rejected during semantic analysis.
    public rule expression_stmt =
        expression
        .push($ -> new ExpressionStatementNode($.span(), $.$[0]));

    public rule array_type = left_expression()
        .left(simple_type)
        .suffix(seq(LSQUARE, RSQUARE),
            $ -> new ArrayTypeNode($.span(), $.$[0])).get();

    public rule type =
        seq(array_type);

    public rule statement = lazy(() -> choice(
        this.block,
        this.var_decl,
        this.fun_decl,
        this.struct_decl,
        this.if_stmt,
        this.while_stmt,
        this.return_stmt,
        this.expression_stmt));

    public rule statements =
        statement.at_least(0)
        .as_list(StatementNode.class);

    public rule block =
        seq(LBRACE, statements, RBRACE)
        .push($ -> new BlockNode($.span(), $.$[0]));

    public rule var_decl =
        seq(_var, identifier, COLON, type, EQUALS, expression)
        .push($ -> new VarDeclarationNode($.span(), $.$[0], $.$[1], $.$[2]));

    public rule parameter =
        seq(identifier, COLON, type)
        .push($ -> new ParameterNode($.span(), $.$[0], $.$[1]));

    public rule parameters =
        parameter.sep(0, COMMA)
        .as_list(ParameterNode.class);

    public rule maybe_return_type =
        seq(COLON, type).or_push_null();

    public rule fun_decl =
        seq(_fun, identifier, LPAREN, parameters, RPAREN, maybe_return_type, block)
        .push($ -> new FunDeclarationNode($.span(), $.$[0], $.$[1], $.$[2], $.$[3]));

    public rule field_decl =
        seq(_var, identifier, COLON, type)
        .push($ -> new FieldDeclarationNode($.span(), $.$[0], $.$[1]));

    public rule struct_body =
        seq(LBRACE, field_decl.at_least(0).as_list(DeclarationNode.class), RBRACE);

    public rule struct_decl =
        seq(_struct, identifier, struct_body)
        .push($ -> new StructDeclarationNode($.span(), $.$[0], $.$[1]));

    public rule if_stmt =
        seq(_if, expression, statement, seq(_else, statement).or_push_null())
        .push($ -> new IfNode($.span(), $.$[0], $.$[1], $.$[2]));

    public rule while_stmt =
        seq(_while, expression, statement)
        .push($ -> new WhileNode($.span(), $.$[0], $.$[1]));

    public rule return_stmt =
        seq(_return, expression.or_push_null())
        .push($ -> new ReturnNode($.span(), $.$[0]));

    public rule root =
        seq(ws, statement.at_least(1))
        .as_list(StatementNode.class)
        .push($ -> new RootNode($.span(), $.$[0]));

    { make_rule_names(); }
}
