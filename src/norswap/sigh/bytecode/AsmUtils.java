package norswap.sigh.bytecode;

import norswap.utils.exceptions.Exceptions;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.NoSuchElementException;

import static org.objectweb.asm.Opcodes.*;

/**
 * Utilities to help emitting JVM bytecode.
 */
public final class AsmUtils {
    private AsmUtils() {}

    // ---------------------------------------------------------------------------------------------

    /**
     * Alias for {@link Opcodes#IFEQ} that makes the purpose clearer when checking for a zero value
     * outside of the context of number comparison.
     */
    public final static int IF_ZERO = IFEQ;

    /**
     * Alias for {@link Opcodes#IFNE} that makes the purpose clearer when checking for a zero value
     * outside of the context of number comparison.
     */
    public final static int IF_NOT_ZERO = IFNE;

    // ---------------------------------------------------------------------------------------------

    /**
     * The binary name of class (or assimilated: interface, enum, ... - but not arrays or
     * primitives), which is the canonical name of the class ({@link Class#getName()}.
     *
     * <p>In particular this returns a form used in JVM descriptors, namely using slashes (/)
     * as separators instead of dots.
     *
     * <p>Defined in <a
     * href="https://docs.oracle.com/javase/specs/jvms/se16/html/jvms-4.html#jvms-4.2.1">JVMS
     * §4.2.1</a>
     */
    public static String slashBinaryName (Class<?> klass)
    {
        if (klass.isPrimitive() || klass.isArray())
            throw new IllegalArgumentException("expected class or interface, not primitive or array");
        return klass.getName().replace('.', '/');
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the field descriptor for {@code klass}, for use in JVM descriptors and signatures.
     * e.g. "I" for {@code int}, "Ljava/lang/String;" for {@code String}.
     *
     * <p>Defined in <a
     * href="https://docs.oracle.com/javase/specs/jvms/se16/html/jvms-4.html#jvms-4.3.2">JVMS
     * §4.3.2</a>.
     */
    public static String fieldDescriptor (Class<?> klass)
    {
        if (klass.isPrimitive())
            if (int.class == klass)
                return "I";
            else if (long.class == klass)
                return "J";
            else if (boolean.class == klass)
                return "Z";
            else if (byte.class == klass)
                return "B";
            else if (short.class == klass)
                return "S";
            else if (char.class == klass)
                return "C";
            else if (float.class == klass)
                return "F";
            else if (double.class == klass)
                return "D";
            else if (void.class == klass)
                return "V";
            else
                throw new Error("unreachable");
        else if (klass.isArray())
            return "[" + fieldDescriptor(klass.getComponentType());
        else
            return "L" + slashBinaryName(klass) + ";";
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the method descriptor for the class' method with the given parameters.
     *
     * <p>Defined in <a
     * href="https://docs.oracle.com/javase/specs/jvms/se16/html/jvms-4.html#jvms-4.3.3">JVMS
     * §4.3.3</a>.
     */
    public static String methodDescriptor (Class<?> klass, String method, Class<?>... parameters) {
        try {
            Method m = klass.getMethod(method, parameters);
            return methodDescriptor(m.getReturnType(), m.getParameterTypes());
        } catch (NoSuchMethodException e) {
            throw new NoSuchElementException(e.getMessage());
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Return a method descriptor using the given return type and parameter types.
     *
     * <p>Defined in <a
     * href="https://docs.oracle.com/javase/specs/jvms/se16/html/jvms-4.html#jvms-4.3.3">JVMS
     * §4.3.3</a>.
     */
    public static String methodDescriptor (Class<?> returnType, Class<?>... parameterTypes)
    {
        StringBuilder b = new StringBuilder();
        b.append('(');
        for (Class<?> paramType: parameterTypes)
            b.append(fieldDescriptor(paramType));
        b.append(')');
        return b.append(fieldDescriptor(returnType)).toString();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Emits a static invocation on the visitor for a method with the given name and parameter types
     * in the class.
     */
    public static void invokeStatic (
            MethodVisitor visitor, Class<?> klass, String method,
            Class<?>... parameterTypes) {

        visitor.visitMethodInsn(INVOKESTATIC, slashBinaryName(klass), method,
            methodDescriptor(klass, method, parameterTypes), false);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link Handle} for a the static method with the given Emits a static invocation on
     * the visitor for a method with the given name and parameter types in the class.
     */
    public static Handle staticHandle (Class<?> klass, String method, Class<?>... parameterTypes) {
        return new Handle(H_INVOKESTATIC, slashBinaryName(klass), method,
            methodDescriptor(klass, method, parameterTypes), false);
    }

    // ---------------------------------------------------------------------------------------------

    private static final HashMap<Object, Integer> CONSTANTS = new HashMap<>();
    static {
        CONSTANTS.put(-1, ICONST_M1);
        CONSTANTS.put(0, ICONST_0);
        CONSTANTS.put(1, ICONST_1);
        CONSTANTS.put(2, ICONST_2);
        CONSTANTS.put(3, ICONST_3);
        CONSTANTS.put(4, ICONST_4);
        CONSTANTS.put(5, ICONST_5);
        CONSTANTS.put(0L, LCONST_0);
        CONSTANTS.put(1L, LCONST_1);
        CONSTANTS.put(0f, FCONST_0);
        CONSTANTS.put(1f, FCONST_1);
        CONSTANTS.put(2f, FCONST_2);
        CONSTANTS.put(0d, DCONST_0);
        CONSTANTS.put(1d, DCONST_1);
        CONSTANTS.put(null, ACONST_NULL);
    }

    /**
     * Emits an instruction to load the given constant. If possible, emits a specialized
     * {@code $X$CONST_$Y$} instruction.
     */
    public static void loadConstant (MethodVisitor visitor, Object constant) {
        Integer opcode = CONSTANTS.get(constant);
        if (opcode != null) visitor.visitInsn(opcode);
        else visitor.visitLdcInsn(constant);
    }

    // ---------------------------------------------------------------------------------------------

    public static Type asmMethodType (Class<?> owner, String name, Class<?>... paramTypes) {
        Method m = Exceptions.suppress(() -> owner.getMethod(name, paramTypes));
        return Type.getType(m);
    }

    // ---------------------------------------------------------------------------------------------
}
