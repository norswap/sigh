package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ArrayDeclarationNode extends DeclarationNode
{
    public final String name;
    public final TypeNode type;
    public final List initializer;

    public ArrayDeclarationNode (Span span, Object name, Object type, Object initializer) {
        super(span);
        this.name = Util.cast(name, String.class);
        this.type = Util.cast(type, TypeNode.class);
        this.initializer = Util.cast(initializer, List.class);
    }

    @Override public String name () {
        return name;
    }

    @Override public String contents () {
        return "var " + name;
    }

    @Override public String declaredThing () {
        return "variable";
    }

    public Object[] createArray(int index){
        int size=Integer.parseInt(((IntLiteralNode)initializer.get(index)).contents());
        Object[] array=new Object[size];
        if(index==initializer.size()-1)return array;
        for(Object i:array){
            i=createArray(index+1);
        }
        return array;

    }
}

