package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import java.util.ArrayList;
import java.util.List;

public class StructDeclarationNode extends DeclarationNode
{
    public final String name;

    public final List<FieldDeclarationNode> fields;
    public final List<FunDeclarationNode> fun;

    @SuppressWarnings("unchecked")
    public StructDeclarationNode (Span span, Object name, Object field,Object fun) {
        super(span);
        this.name = Util.cast(name, String.class);

        this.fields = Util.cast(field, List.class);
        this.fun = Util.cast(fun, List.class);

    }

    @Override public String name () {
        return name;
    }

    @Override public String contents () {
        return "struct " + name;
    }

    @Override public String declaredThing () {
        return "struct";
    }
}
