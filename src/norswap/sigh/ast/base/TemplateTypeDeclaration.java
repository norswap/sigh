package norswap.sigh.ast.base;

import norswap.autumn.positions.Span;
import norswap.sigh.ast.DeclarationNode;
import norswap.sigh.ast.SimpleTypeNode;
import norswap.utils.Util;

public final class TemplateTypeDeclaration extends DeclarationNode
{
    public final String name;
    public SimpleTypeNode value;

    public TemplateTypeDeclaration (Span span, Object name) {
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
