package norswap.sigh.interpreter;

import norswap.sigh.scopes.RootScope;
import norswap.sigh.scopes.Scope;
import java.util.HashMap;

/**
 * The concrete instantiation of a {@link Scope} at runtime.
 *
 * <p>The scope itself cannot be used directly, as many "copies" of the scope will be needed
 * at runtime (for instance, one for each function invocation), sometimes at the same type,
 * in the presence of recursion.
 */
public final class ScopeStorage
{
    // ---------------------------------------------------------------------------------------------

    public final Scope scope;
    public final ScopeStorage parent;

    // ---------------------------------------------------------------------------------------------

    private final HashMap<String, Object> values = new HashMap<>();

    // ---------------------------------------------------------------------------------------------

    ScopeStorage (Scope scope, ScopeStorage parent) {
        this.scope = scope;
        this.parent = parent;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the value with the given name, defined in the given scope (determined by semantic
     * analysis), which should be this scope or one of its ancestors.
     */
    Object get (Scope scope, String name)
    {
        if (scope == this.scope)
            return values.get(name);
        else if (parent != null)
            return parent.get(scope, name);
        else
            throw new Error("[implementation bug] could not lookup name: " + name);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Sets the value with the given name, defined in the given scope (determined by semantic
     * analysis), which should be this scope or one of its ancestors.
     */
    void set (Scope scope, String name, Object value)
    {
        if (scope == this.scope)
            values.put(name, value);
        else
            parent.set(scope, name, value);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * To be called on the root frame to initialize its variables.
     */
    void initRoot(RootScope root)
    {
        set(root, root._true  .name(), true);
        set(root, root._false .name(), false);
        set(root, root._null  .name(), Null.INSTANCE);

        // Function and types are not assigned values in frames - instead they derive
        // their runtime value values from the corresponding DeclarationNode.
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toString() {
        return "ScopeStorage " + values.toString();
    }

    // ---------------------------------------------------------------------------------------------
}
