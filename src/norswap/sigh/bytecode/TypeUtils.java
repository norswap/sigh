package norswap.sigh.bytecode;

import norswap.sigh.types.*;
import java.lang.reflect.Array;

/**
 * TODO
 */
public final class TypeUtils {
    private TypeUtils() {}

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the slash-separated binary type name for the runtime representation of the given
     * structure.
     */
    public static String structBinaryName (StructType type) {
        return type.name();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a java {@link Class} used for the runtime representation of the given Sigh {@link
     * Type}.
     *
     * <p>This is fragile when structure types are involved, since they have no compile-time {@link
     * Class} representation ({@code Object.class} is returned).
     */
    static Class<?> javaClass (Type type)
    {
        if (type instanceof IntType)
            return long.class;
        else if (type instanceof BoolType)
            return boolean.class;
        else if (type instanceof FloatType)
            return double.class;
        else if (type instanceof VoidType)
            return void.class;
        else if (type instanceof StringType)
            return String.class;
        else if (type instanceof NullType)
            return Null.class;
        else if (type instanceof ArrayType)
            return javaArrayClass(((ArrayType) type).componentType);
        else if (type instanceof TypeType)
            return Type.class;
        else if (type instanceof FunType)
            throw new UnsupportedOperationException(); // TODO
        else if (type instanceof StructType)
            return Object.class; // the proper class type is not available at compile time
        else
            throw new Error("unreachable");
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a java {@link Class} used for the runtime representation of the array whose
     * components is the given Sigh {@link Type}.
     *
     * <p>This is fragile when structure types are involved, since they have no compile-time {@link
     * Class} representation ({@code Object[].class} is returned).
     */
    public static Class<?> javaArrayClass (Type type)
    {
        if (type instanceof IntType)
            return long[].class;
        else if (type instanceof BoolType)
            return boolean[].class;
        else if (type instanceof FloatType)
            return double[].class;
        else if (type instanceof VoidType)
            return Object[].class; // can't have non-empty arrays of void
        else if (type instanceof StringType)
            return String[].class;
        else if (type instanceof NullType)
            return Null[].class;
        else if (type instanceof ArrayType)
            return Array.newInstance(javaClass(((ArrayType) type).componentType)).getClass();
        else if (type instanceof TypeType)
            return Type[].class;
        else if (type instanceof FunType)
            throw new UnsupportedOperationException(); // TODO
        else if (type instanceof StructType)
            return Object[].class; // the proper class type is not available at compile time
        else
            throw new Error("unreachable");
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the JVM field descriptor the runtime representation of the given Sigh {@link Type}.
     */
    public static String fieldDescriptor (Type type)
    {
        if (type instanceof IntType)
            return "J"; // long
        else if (type instanceof BoolType)
            return "Z"; // boolean
        else if (type instanceof FloatType)
            return "D"; // double
        else if (type instanceof VoidType)
            return "V"; // void
        else if (type instanceof StringType)
            return "Ljava/lang/String;";
        else if (type instanceof NullType)
            return "Lnorswap/sigh/bytecode/Null;";
        else if (type instanceof ArrayType)
            return "[" + fieldDescriptor(((ArrayType) type).componentType);
        else if (type instanceof TypeType)
            return "Lnorswap/sigh/types/Type;";
        else if (type instanceof FunType)
            throw new UnsupportedOperationException(); // TODO
        else if (type instanceof StructType)
            return "L" + structBinaryName((StructType) type) + ";";
        else
            throw new Error("unreachable");
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the ASM {@link org.objectweb.asm.Type} object for the runtime representation of the
     * given Sigh {@link Type}.
     */
    public static org.objectweb.asm.Type asmType (Type type) {
        return org.objectweb.asm.Type.getType(fieldDescriptor(type));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the JVM method descriptor for the method signature corresponding to the given
     * Sigh return type and parameter type.
     */
    public static String methodDescriptor (Type returnType, Type... paramTypes)
    {
        StringBuilder b = new StringBuilder("(");
        for (Type paramType: paramTypes)
            b.append(fieldDescriptor(paramType));
        b.append(")");
        b.append(fieldDescriptor(returnType));
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the JVM method descriptor for the method signature corresponding to the given
     * Sigh {@link FunType function type}.
     */
    public static String methodDescriptor (FunType funType) {
        return methodDescriptor(funType.returnType, funType.paramTypes);
    }

    // ---------------------------------------------------------------------------------------------
}
