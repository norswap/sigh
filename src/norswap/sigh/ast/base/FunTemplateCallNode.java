package norswap.sigh.ast.base;

import norswap.autumn.positions.Span;
import norswap.sigh.ast.ExpressionNode;
import norswap.sigh.ast.FunCallNode;
import norswap.utils.Util;
import java.util.ArrayList;
import java.util.List;

public final class FunTemplateCallNode extends FunCallNode
{
    public List<ExpressionNode> template_arguments;

    @SuppressWarnings("unchecked")
    public FunTemplateCallNode(Span span, Object function, Object arguments, Object template_arguments) {
        super(span, function, arguments);

        // Setting up template arguments
        this.template_arguments = (template_arguments == null) ? new ArrayList<>() : Util.cast(template_arguments, List.class);
    }

    @Override public String contents ()
    {
        String args = arguments.size() == 0 ? "()" : "(...)";
        return function.contents() + args;
    }
}
