package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.sigh.types.ArrayType;
import norswap.utils.Util;
import java.util.ArrayList;

public final class VarDeclarationNode extends DeclarationNode
{
    public final String name;
    public final TypeNode type;
    public final ExpressionNode initializer;

    public VarDeclarationNode (Span span, Object name, Object type, Object initializer) {
        super(span);
        this.name = Util.cast(name, String.class);
        this.type = Util.cast(type, TypeNode.class);
        this.initializer = Util.cast(initializer, ExpressionNode.class);
        if(initializer instanceof ArrayLiteralNode && type instanceof ArrayTypeNode){
            getLiteralDimension();
        }
    }

    @Override public String name () {
        return name;
    }

    @Override public String contents () {
        return "var " + name;
    }

    @Override public String declaredThing () {
        return "variable";
    }

    public void getLiteralDimension(){
        ((ArrayTypeNode)type).dimensions=new ArrayList(((ArrayLiteralNode)initializer).dimensions);
    }
}
