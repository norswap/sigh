package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class BornNode extends StatementNode
{
    public final String name;
    public final BlockNode block;

    public BornNode (Span span, Object name, Object block) {
        super(span);
        this.name = Util.cast(name, String.class);
        this.block = Util.cast(block, BlockNode.class);
    }

    @Override public String contents ()
    {
        return "Born" + name;
    }
}
