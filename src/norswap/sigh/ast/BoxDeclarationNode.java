package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import java.util.ArrayList;
import java.util.List;

public class BoxDeclarationNode extends DeclarationNode
{
    public final String name;
    public final List<AttributeDeclarationNode> attributes;
    public final List<MethodDeclarationNode> methods;

    @SuppressWarnings("unchecked")
    public BoxDeclarationNode (Span span, Object name, Object elements) {
        super(span);
        this.name = Util.cast(name, String.class);
        this.attributes = new ArrayList<>();
        this.methods    = new ArrayList<>();
        List<DeclarationNode> temp = Util.cast(elements, List.class);
        for (DeclarationNode declarationNode : temp) {
            if (declarationNode instanceof AttributeDeclarationNode) {
                this.attributes.add((AttributeDeclarationNode) declarationNode);
            } else if (declarationNode instanceof MethodDeclarationNode) {
                this.methods.add((MethodDeclarationNode) declarationNode);
            }
        }
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
