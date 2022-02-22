package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import java.util.ArrayList;

public class ClassDeclarationNode extends DeclarationNode {
    
    public String name;
    public ArrayList<FunDeclarationNode> funs;
    public ArrayList<VarDeclarationNode> vars;
    public ArrayList<StructDeclarationNode> structs;
    public String parent;

    public ClassDeclarationNode (Span span, String name, String parent, BlockNode body) {
        super(span);
        this.name = name;
        this.parent = parent; 
        this.funs = new ArrayList<>();
        this.vars = new ArrayList<>();
        this.structs = new ArrayList<>();
        for (SighNode node : body.statements) {
            if (node instanceof FunDeclarationNode) {
                funs.add((FunDeclarationNode) node);
            } else if (node instanceof VarDeclarationNode) {
                vars.add((VarDeclarationNode) node);
            } else if (node instanceof StructDeclarationNode) {
                structs.add((StructDeclarationNode) node);
            }
        }
    }

    public String contents() {
        return name;
    }

    public String name() {
        return name;
    }

    public String declaredThing() {
        return "class";
    }

}
