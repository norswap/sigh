package norswap.sigh.ast;

import norswap.autumn.positions.Span;

public abstract class DeclarationNode extends StatementNode
{
    public DeclarationNode (Span span) {
        super(span);
    }

    /**
     * Returns the declared identifier name.
     */
    public abstract String name();

    /**
     * Return the name of the thing declared (e.g. "function").
     */
    public abstract String declaredThing();
}
