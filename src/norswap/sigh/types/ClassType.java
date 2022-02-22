package norswap.sigh.types;

import java.util.ArrayList;

import norswap.sigh.ast.*;

public class ClassType extends Type
{

    public final ClassDeclarationNode decl;

    public ClassType (ClassDeclarationNode node)
    {
        this.decl = node;
    }

    @Override public String name() {
        return decl.name;
    }
    
}
