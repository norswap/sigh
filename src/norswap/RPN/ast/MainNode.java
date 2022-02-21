package norswap.RPN.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import java.util.List;



public class MainNode extends NodeRPN
{
    public final List<NodeRPN> content;

    public MainNode(Span span, Object nodes)
    {
        super(span);
        this.content = Util.cast(nodes, List.class);
    }

    public String contents()
    {
        return content.get(0).contents();
    }
}
