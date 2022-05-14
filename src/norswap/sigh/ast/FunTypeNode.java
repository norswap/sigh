package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import java.util.List;

public final class FunTypeNode extends TypeNode
{
    public final List<TypeNode> parametersTypes;
    public final TypeNode returnType;

    @SuppressWarnings("unchecked")
    public FunTypeNode (Span span, Object parametersTypes, Object returnType) {
        super(span);
        this.parametersTypes = Util.cast(parametersTypes, List.class);
        this.returnType = returnType == null
            ? new SimpleTypeNode(new Span(span.start, span.start), "Void")
            : Util.cast(returnType, TypeNode.class);
    }

    @Override public String contents ()
    {
        String content = "FunTypeNode <";
        for(int i = 0; i < parametersTypes.size(); i++) {content += parametersTypes.get(i).toString() + ((i < parametersTypes.size() - 1) ? " " : "");}
        return content + " => " + returnType.toString() + ">";
    }
}
