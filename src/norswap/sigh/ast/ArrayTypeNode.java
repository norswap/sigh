package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import java.util.ArrayList;
import java.util.List;

public final class ArrayTypeNode extends TypeNode
{
    public final TypeNode componentType;
    public List dimensions=new ArrayList();

    public ArrayTypeNode (Span span, Object componentType, Object dims) {
        super(span);
        this.componentType = Util.cast(componentType, TypeNode.class);
        this.dimensions=Util.cast(dims,List.class);
        //dimension(this.componentType);
    }

    @Override public String contents() {
        return componentType.contents() + "[]";
    }

    private void dimension(TypeNode compo){
        String[] comp=compo.contents().split(",");
        dimensions.add(comp.length);
        if(compo instanceof ArrayTypeNode){
            dimension(((ArrayTypeNode)compo).componentType);
        }
    }
}
