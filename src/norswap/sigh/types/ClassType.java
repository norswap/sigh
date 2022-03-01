package norswap.sigh.types;

import java.util.ArrayList;
import java.util.HashMap;

import norswap.sigh.ast.*;

public class ClassType extends Type
{

    public final String name;
    private HashMap<String, DeclarationNode> fields;

    public ClassType (HashMap<String, DeclarationNode> fields, String name)
    {
        this.fields = fields;
        this.name = name;
    }

    public HashMap<String, DeclarationNode> getFields () {
        return fields;
    }

    public String name () {
        return name;
    }

    
}
