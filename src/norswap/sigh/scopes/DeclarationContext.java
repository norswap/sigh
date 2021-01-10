package norswap.sigh.scopes;

import norswap.sigh.ast.DeclarationNode;

/**
 * A pair of a {@link Scope} and a {@link DeclarationNode} declaring an entry in that scope.
 */
public final class DeclarationContext
{
    public final Scope scope;
    public final DeclarationNode declaration;

    public DeclarationContext(Scope scope, DeclarationNode declaration) {
        this.scope = scope;
        this.declaration = declaration;
    }
}
