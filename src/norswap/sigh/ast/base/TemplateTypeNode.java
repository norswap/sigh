package norswap.sigh.ast.base;

import norswap.autumn.positions.Span;
import norswap.sigh.ast.SimpleTypeNode;
import norswap.sigh.ast.TypeNode;
import norswap.sigh.types.Type;
import norswap.utils.Util;

public final class TemplateTypeNode extends TypeNode
{
    public final String name;

    public TemplateTypeNode (Span span, Object name) {
        super(span);
        this.name = Util.cast(name, String.class);
    }

    @Override
    public Type getType () {
        return null;
    }

    /**
     * Used in order to copy the attributes of another parameter node
     * @param node
     */
    public TemplateTypeNode(SimpleTypeNode node) {
        super(node.span);
        this.name = node.name;
    }

    @Override public String contents () {
        return name;
    }
}
