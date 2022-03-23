package norswap.sigh.ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import java.util.ArrayList;
import java.util.List;

public class ArrayLiteralNode extends ExpressionNode
{
    public final List<ExpressionNode> components;
    public List<Integer> dimensions=new ArrayList<Integer>();

    @SuppressWarnings("unchecked")
    public ArrayLiteralNode (Span span, Object components) {
        super(span);
        this.components = Util.cast(components, List.class);
        dimension(this.components);
    }

    private void dimension(List compo){
        dimensions.add(compo.size());
        if(compo.size()>0 && compo.get(0) instanceof ArrayLiteralNode){
            dimension(((ArrayLiteralNode)compo.get(0)).components);
        }
    }

    @Override public String contents ()
    {
        if (components.size() == 0)
            return "[]";

        int budget = contentsBudget() - 2; // 2 == "[]".length()
        StringBuilder b = new StringBuilder("[");
        int i = 0;

        for (ExpressionNode it: components)
        {
            if (i > 0) b.append(", ");
            String contents = it.contents();
            budget -= 2 + contents.length();
            if (i == components.size() - 1) {
                if (budget < 0) break;
            } else {
                if (budget - ", ...".length() < 0) break;
            }
            b.append(contents);
            ++i;
        }

        if (i < components.size())
            b.append("...");

        return b.append(']').toString();
    }
}
