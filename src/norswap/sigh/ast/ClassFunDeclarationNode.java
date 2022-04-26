package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import java.util.List;

public class ClassFunDeclarationNode extends DeclarationNode {

    public final String name;
    public final List<ParameterNode> parameters;


    @SuppressWarnings("unchecked")
    public ClassFunDeclarationNode
        (Span span, Object name, Object parameters, Object returnType, Object block) {
        super(span);
        this.name = Util.cast(name, String.class);
        this.parameters = Util.cast(parameters, List.class);

    }

    @Override public String name () {
        return name;
    }

    @Override public String contents () {
        return "Class fun " + name;
    }

    @Override public String declaredThing () {
        return "Class function";
    }
}
