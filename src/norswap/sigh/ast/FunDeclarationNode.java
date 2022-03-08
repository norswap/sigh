package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import java.util.List;

public class FunDeclarationNode extends DeclarationNode
{
    public final String name;
    public final List<ParameterNode> parameters;
    public final TypeNode returnType;
    public final ExpressionNode returnValue;
    public final BlockNode block;

    @SuppressWarnings("unchecked")
    public FunDeclarationNode
            (Span span, Object name, Object parameters, Object returnType, Object returnValue, Object block) {
        super(span);
        this.name = Util.cast(name, String.class);
        this.parameters = Util.cast(parameters, List.class);
        this.returnType = returnType == null
            ? new SimpleTypeNode(new Span(span.start, span.start), "Void")
            : Util.cast(returnType, TypeNode.class);
        
        this.returnValue = returnValue == null
                ? null
                : Util.cast(returnValue, ExpressionNode.class);
        
        this.block = block == null
                        ? null
                        : Util.cast(block, BlockNode.class);
    }

    @Override public String name () {
        return name;
    }

    @Override public String contents () {
        return "fun " + name;
    }

    @Override public String declaredThing () {
        return "function";
    }
}
