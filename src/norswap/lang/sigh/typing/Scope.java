package norswap.lang.sigh.typing;

import norswap.lang.sigh.ast.DeclarationNode;
import norswap.lang.sigh.ast.Node;
import java.util.HashMap;

public class Scope
{
    public final Node node;
    public final Scope parent;

    private final HashMap<String, DeclarationNode> declarations = new HashMap<>();

    public Scope (Node node, Scope parent) {
        this.node = node;
        this.parent = parent;
    }

    public void declare (String identifier, DeclarationNode node) {
        declarations.put(identifier, node);
    }

    public DeclarationNode lookup (String name)
    {
        DeclarationNode declaration = declarations.get(name);
        return declaration != null
                ? declaration
                : parent != null
                    ? parent.lookup(name)
                    : null;
    }

    @Override public String toString() {
        return "Scope " + declarations.toString();
    }
}
