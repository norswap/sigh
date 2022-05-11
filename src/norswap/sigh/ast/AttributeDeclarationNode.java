package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public class AttributeDeclarationNode extends DeclarationNode
{
    public final String name;
    public final TypeNode type;
    public final ExpressionNode initializer;

    public AttributeDeclarationNode (Span span, Object name, Object type) {
        super(span);
        this.name = Util.cast(name, String.class);
        this.type = Util.cast(type, TypeNode.class);
        // TODO put the default type for every types
        if (type.equals(new SimpleTypeNode(null, "Int"))) {
            this.initializer = new IntLiteralNode(null, 0);
        } else if (type.equals(new SimpleTypeNode(null, "Float"))) {
            this.initializer = new FloatLiteralNode(null, 0);
        } else if (type.equals(new SimpleTypeNode(null, "Bool"))) {
            this.initializer = new IntLiteralNode(null, 0);
        } else if (type.equals(new SimpleTypeNode(null, "String"))) {
            this.initializer = new StringLiteralNode(null, "");
        } else {
            this.initializer = null;
        }
    }

    @Override public String name () {
        return name;
    }

    @Override public String contents () {
        return "attr " + name;
    }

    @Override public String declaredThing () {
        return "attribute";
    }
}
