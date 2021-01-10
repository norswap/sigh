package norswap.sigh.scopes;

import norswap.sigh.ast.DeclarationNode;
import java.util.Locale;

/**
 * In Sigh's implementation, every reference must resolve to a {@link DeclarationNode}.
 * A {@code SyntheticDeclarationNode} is such a node for declarations that have not been
 * introduced by the user.
 *
 * <p>At present, all such declarations are unconditionally introduced in the {@link RootScope}.
 */
public final class SyntheticDeclarationNode extends DeclarationNode
{
    private final String name;
    private final DeclarationKind kind;

    public SyntheticDeclarationNode(String name, DeclarationKind kind) {
        super(null);
        this.name = name;
        this.kind = kind;
    }

    @Override public String name () {
        return name;
    }

    public DeclarationKind kind() {
        return kind;
    }

    @Override public String contents () {
        return name;
    }

    @Override public String declaredThing () {
        return "built-in " + kind.name().toLowerCase(Locale.ROOT);
    }
}
