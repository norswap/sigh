package norswap.sigh.ast;

import norswap.autumn.positions.Span;

import java.util.List;

public class ClassDeclarationNode extends DeclarationNode {
    
    public String name;
    public List<SighNode> body;
    public String parent;

    public ClassDeclarationNode (Span span, String name, String parent, List<SighNode> body) {
        super(span);
        this.name = name;
        this.parent = parent; 
        this.body = body;
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
