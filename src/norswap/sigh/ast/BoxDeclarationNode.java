package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import java.util.List;

public class BoxDeclarationNode extends DeclarationNode
{
    public final String name;
    public final List<AttributeDeclarationNode> attributes;
    public final List<MethodDeclarationNode> methods;

    @SuppressWarnings("unchecked")
    public BoxDeclarationNode (Span span, Object name, Object attributes, Object methods) {
        super(span);
        this.name = Util.cast(name, String.class);
        this.attributes = Util.cast(attributes, List.class);
        this.methods = Util.cast(methods, List.class);
    }

    @Override public String name () {
        return name;
    }

    @Override public String contents () {
        return "box " + name;
    }

    @Override public String declaredThing () {
        return "box";
    }
}
