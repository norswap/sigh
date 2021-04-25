package norswap.sigh.bytecode;

/**
 * A class loader with the ability to load class from bytecode arrays.
 */
public final class ByteArrayClassLoader extends ClassLoader
{
    // ---------------------------------------------------------------------------------------------

    static {
        registerAsParallelCapable();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Default reusable instance of the class loader.
     */
    public static final ByteArrayClassLoader INSTANCE = new ByteArrayClassLoader();

    // ---------------------------------------------------------------------------------------------

    /**
     * Given a class' (dot-separated) binary name and the bytecode array, load the class
     * and return the corresponding {@link Class} object.
     */
    public Class<?> defineClass (String binaryName, byte[] bytecode) {
        return defineClass(binaryName, bytecode, 0, bytecode.length);
    }

    // ---------------------------------------------------------------------------------------------
}