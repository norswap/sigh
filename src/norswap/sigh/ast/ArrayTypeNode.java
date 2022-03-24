package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;

public final class ArrayTypeNode extends TypeNode
{
    public TypeNode componentType;
    public List dimensions;

    public ArrayTypeNode (Span span, Object componentType, Object dims) {
        super(span);
        this.componentType = Util.cast(componentType, TypeNode.class);
        if(!(dims instanceof List))
            throw new IllegalArgumentException("Illegal declaration of array");
        this.dimensions=Util.cast(dims,List.class);
        init();
    }

    @Override public String contents() {
        return componentType.contents() + "[]";
    }


    private void init(){
        if(dimensions.size()<=1) return;
        List subDimensions=dimensions.subList(1,dimensions.size());
        ArrayTypeNode subArray=new ArrayTypeNode(this.span,componentType,subDimensions);
        componentType=subArray;
    }
}
