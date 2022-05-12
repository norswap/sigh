package norswap.sigh.types;

import norswap.sigh.ast.base.TemplateTypeDeclarationNode;

public final class TemplateType extends Type
{
    public final TemplateTypeDeclarationNode node;

    public TemplateType (TemplateTypeDeclarationNode node) {
        this.node = node;
    }

    @Override public String name() {
        return node.name();
    }

    @Override public boolean equals (Object o) {
        return this == o;
    }

    @Override public int hashCode () {
        return node.hashCode();
    }

    public Type getTemplateTypeReference() {
        return this.node.value != null ? this.node.value : this;
    }
}
