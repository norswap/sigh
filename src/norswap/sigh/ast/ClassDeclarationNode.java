package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import java.util.List;

public class ClassDeclarationNode extends DeclarationNode{
    public final String name;
    public final List<FieldDeclarationNode> fields;

    @SuppressWarnings("unchecked")
    public ClassDeclarationNode (Span span, Object name, Object fields) {
        super(span);
        this.name = Util.cast(name, String.class);
        this.fields = Util.cast(fields, List.class);
    }

    @Override public String name () {
        return name;
    }

    @Override public String contents () {
        return "class " + name;
    }

    @Override public String declaredThing () {
        return "class";
    }
}
