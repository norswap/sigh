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

    public rule MULT = word("*").push($ -> new MultOperator((NodeRPN)$.$[0], (NodeRPN)$.$[1]));
    public rule ADD = word("+").push($ -> new AddOperator((NodeRPN)$.$[0], (NodeRPN)$.$[1]));
    public rule POP = word("pop").push($ -> 
    {
        if ($.$.length == 1)
            return new PopOperator(null, (NodeRPN)$.$[0]);
        else
            return new PopOperator((NodeRPN)$.$[0], (NodeRPN)$.$[1]);
    });    
    public rule PRINT = word("print").push($ -> new PrintOperator((NodeRPN)$.$[0]));

    public rule number =
        seq(opt('-'), choice('0', digit.at_least(1)));

    public rule integer =
        number
        .push($ -> new IntegerNode($.str()))
        .word();

    public rule operator = choice(MULT, ADD, POP, PRINT);

    public rule expression = lazy(()-> seq(choice(integer, operator), this.expression));
 
    public rule root = expression;

    @Override public rule root () {
        return root;
    }
}
