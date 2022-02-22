package norswap.sigh.types;

import java.util.ArrayList;

import norswap.sigh.ast.ClassDeclarationNode;
import norswap.sigh.ast.FunDeclarationNode;
import norswap.sigh.ast.StructDeclarationNode;
import norswap.sigh.ast.TypeNode;
import norswap.sigh.ast.VarDeclarationNode;

public class ClassType extends Type
{
    // Define an inner class
    public class Pair<T1, T2> {
        public T1 first;
        public T2 second;

        public Pair(T1 first, T2 second) {
            this.first = first;
            this.second = second;
        }
    }

    public final String name;
    public final String parent;
    public final ArrayList<FunDeclarationNode> funs;
    public final ArrayList<VarDeclarationNode> vars;
    public final ArrayList<StructDeclarationNode> structs;

    public ClassType (ClassDeclarationNode node)
    {
        this.name = node.name;
        this.parent = node.parent;
        this.funs = node.funs;
        this.vars = node.vars;
        this.structs = node.structs;
    }

    @Override public String name() {
        return name;
    }
    
}
