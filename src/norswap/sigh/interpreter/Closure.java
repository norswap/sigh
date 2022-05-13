package norswap.sigh.interpreter;

import norswap.sigh.ast.FunDeclarationNode;

public class Closure
{
    public final FunDeclarationNode funNode;

    public final ScopeStorage storage;

    public Closure (FunDeclarationNode funNode_, ScopeStorage storage_) {
        this.funNode = funNode_;
        this.storage = storage_;
    }
}
