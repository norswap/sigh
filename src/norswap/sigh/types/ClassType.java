package norswap.sigh.types;

import java.util.HashMap;

public class ClassType extends Type
{

    public final String name;
    private final HashMap<String, Type> fields;

    public ClassType (String name)
    {
        this.name = name;
        this.fields = new HashMap<>();
    }

    public boolean addKeys(String name, Type type) {
        if (!fields.containsKey(name)) {
            fields.put(name, type);
            return true;
        } else {
            return false;
        }
    }

    public String name () {
        return name;
    }

    
}
