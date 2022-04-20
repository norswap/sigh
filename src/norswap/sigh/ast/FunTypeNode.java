package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import java.util.List;

public final class FunTypeNode extends TypeNode
{
    public final TypeNode returnType;
    public final List<TypeNode> parametersTypes;

    @SuppressWarnings("unchecked")
    public FunTypeNode (Span span, Object returnType, Object parametersTypes) {
        super(span);
        this.returnType = returnType == null
            ? new SimpleTypeNode(new Span(span.start, span.start), "Void")
            : Util.cast(returnType, TypeNode.class);
        this.parametersTypes = Util.cast(parametersTypes, List.class);
    }

    @Override public String contents ()
    {
        String content = "FunTypeNode <";
        for(int i = 0; i < parametersTypes.size(); i++) {content += parametersTypes.get(i).toString() + ((i < parametersTypes.size() - 1) ? " " : "");}
        return content + " => " + returnType.toString() + ">";
    }
}
