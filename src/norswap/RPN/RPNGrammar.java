package norswap.RPN;

import norswap.autumn.Grammar;

import norswap.RPN.ast.*;

@SuppressWarnings("Convert2MethodRef")
public class RPNGrammar extends Grammar
{
    // ==== LEXICAL ===========================================================

    public rule line_comment =
        seq("//", seq(not("\n"), any).at_least(0));

    public rule ws_item = choice(
        set(" \t\n\r;"),
        line_comment);

    {
        ws = ws_item.at_least(0);
    }

    public rule MULT = word("*").as_val(Operators.MULT);
    public rule ADD = word("+").as_val(Operators.ADD);
    public rule POP = word("pop").as_val(Operators.POP);
    public rule PRINT = word("print").as_val(Operators.PRINT);

    public rule binary_operator = choice(MULT, ADD).push(ctx -> new BinaryOperator(ctx.span(), ctx.$0())).word();
    public rule unary_operator = choice(PRINT, POP).push(ctx -> new UnaryOperator(ctx.span(), ctx.$0())).word();

    public rule number =
        seq(opt('-'), choice('0', digit.at_least(1))).word();

    public rule integer =
        number
        .push(ctx -> new IntegerNode(ctx.span(),ctx.str()))
        .word();

    public rule expression = choice(integer, unary_operator, binary_operator).at_least(0).as_list(NodeRPN.class);

    public rule root = expression.push(ctx -> new MainNode(ctx.span(), ctx.$0()));

    @Override public rule root () {
        return root;
    }
}
