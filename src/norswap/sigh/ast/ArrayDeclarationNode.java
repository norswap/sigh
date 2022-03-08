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
    public final Object[] thisArray;

    public ArrayDeclarationNode (Span span, Object name, Object type, Object initializer) {
        super(span);
        this.name = Util.cast(name, String.class);
        this.type = Util.cast(type, TypeNode.class);
        this.initializer = Util.cast(initializer, List.class);
        thisArray=createArray(0);
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
        String contents=type.contents();
        String t=contents.replace("[]","");
        int size=Integer.parseInt(((IntLiteralNode)initializer.get(index)).contents());

        if(index==initializer.size()-1){
            if(t.equals("Int") || t.equals("Long")){
                Long[] array= new Long[size];
                Arrays.fill(array,new Long(0));
                return array;
            }else if(t.equals("Float") || t.equals("Double")){
                Double[] array= new Double[size];
                Arrays.fill(array,new Double(0));
                return array;
            }else{
                return new Object[size];
            }
        }
        Object[] array=new Object[size];
        for(int i=0;i<size;i++){
            array[i]=createArray(index+1);
        }
        return array;

    }
}

