package norswap.lang.sigh.analysis;

import norswap.lang.sigh.ast.DeclarationNode;
import norswap.lang.sigh.ast.Node;
import norswap.lang.sigh.interpreter.Frame;
import java.util.HashMap;

/**
 * Represent a lexical scope in which declarations occurs.
 *
 * <p>Such a scope is "instantiated" at runtime as a {@link Frame}.
 */
public class Scope
{
    /**
     * The AST node that introduces this scope.
     */
    public final Node node;

    /**
     * The parent of this scope, which is the inermost lexically enclosing scope.
     */
    public final Scope parent;

    private final HashMap<String, DeclarationNode> declarations = new HashMap<>();

    public Scope (Node node, Scope parent) {
        this.node = node;
        this.parent = parent;
    }

    /**
     * Adds a new declaration to this scope.
     */
    public void declare (String identifier, DeclarationNode node) {
        declarations.put(identifier, node);
    }

    /**
     * Looks up the name in the scope and its parents, returning a context comprising the
     * found declaration and the scope in which it occurs, or null if not found.
     */
    public DeclarationContext lookup (String name)
    {
        DeclarationNode declaration = declarations.get(name);
        return declaration != null
                ? new DeclarationContext(this, declaration)
                : parent != null
                    ? parent.lookup(name)
                    : null;
    }

    @Override public String toString() {
        return "Scope " + declarations.toString();
    }
}
