package norswap.sigh.ast.base;

import norswap.autumn.positions.Span;
import norswap.sigh.ast.DeclarationNode;
import norswap.sigh.types.Type;
import norswap.utils.Util;

public final class TemplateTypeDeclarationNode extends DeclarationNode
{
    public final String name;
    public Type value;

    public TemplateTypeDeclarationNode(Span span, Object name) {
        super(span);
        this.name = Util.cast(name, String.class);
        this.value = null;
    }

    @Override public String contents () {
        return name;
    }

    /**
     * Returns the declared identifier name.
     */
    @Override public String name() {
        return name;
    }

    /**
     * Return the name of the thing declared (e.g. "function").
     */
    @Override public String declaredThing() {
        return "Template parameter";
    }
}
