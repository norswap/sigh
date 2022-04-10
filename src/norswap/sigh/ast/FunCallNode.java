package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import java.util.ArrayList;
import java.util.List;

public class FunCallNode extends ExpressionNode
{
    public final ExpressionNode function;
    public final List<ExpressionNode> arguments;
    public List<ExpressionNode> template_arguments;

    @SuppressWarnings("unchecked")
    public FunCallNode (Span span, Object function, Object arguments) {
        super(span);
        this.function = Util.cast(function, ExpressionNode.class);
        this.arguments = Util.cast(arguments, List.class);
    }

    @SuppressWarnings("unchecked")
    public FunCallNode(Span span, Object function, Object arguments, Object template_arguments) {
        super(span);

        // Setting up data
        this.function = Util.cast(function, ExpressionNode.class);
        this.arguments = Util.cast(arguments, List.class);

        // Setting up template arguments
        this.template_arguments = (template_arguments == null) ? new ArrayList<>() : Util.cast(template_arguments, List.class);
    }

    @Override public String contents ()
    {
        // TODO show up template arguments here
        String args = arguments.size() == 0 ? "()" : "(...)";
        return function.contents() + args;
    }
}
