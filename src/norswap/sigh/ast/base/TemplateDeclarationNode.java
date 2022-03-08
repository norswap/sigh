package norswap.sigh.ast.base;

import norswap.autumn.positions.Span;
import norswap.sigh.ast.*;
import norswap.utils.Util;
import javax.swing.plaf.nimbus.State;
import java.util.ArrayList;
import java.util.List;

public class TemplateDeclarationNode extends StatementNode
{
    public final List<String> parameterIdentifiers;
    public final String returnIdentifier;

    @SuppressWarnings("unchecked")
    public TemplateDeclarationNode (Span span, Object identifiers_raw) {
        super(span);

        // Casting
        List<String> identifiers = Util.cast(identifiers_raw, List.class);

        // Checking if multiple identifiers
        if (identifiers.size() > 1) {
            this.returnIdentifier = Util.cast(identifiers.get(0), String.class);
            this.parameterIdentifiers = identifiers.subList(1, identifiers.size()-1);
        } else {
            this.parameterIdentifiers = identifiers;
            this.returnIdentifier = parameterIdentifiers.get(0);
        }

        return;
    }

    @Override public String contents () {
        return "template " + "todo";
    }
}
