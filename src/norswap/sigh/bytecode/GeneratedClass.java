package norswap.sigh.bytecode;

/**
 * A class generated by the bytecode compiler.
 */
public final class GeneratedClass
{
    // ---------------------------------------------------------------------------------------------

    private final String slashBinaryName;
    private final byte[] bytes;

    // ---------------------------------------------------------------------------------------------

    /**
     * Create a generated class from a (slash-separated) binary name and the bytecode array.
     */
    public GeneratedClass (String slashBinaryName, byte[] bytes) {
        this.slashBinaryName = slashBinaryName;
        this.bytes = bytes;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Return the bytecode array for this class.
     */
    public byte[] bytes() {
        return bytes;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Load this class in the current JVM using the given class loader and return the corresponding
     * {@link Class} object.
     */
    public Class<?> load (ByteArrayClassLoader loader) {
        return loader.defineClass(slashBinaryName, bytes);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the dot-separated binary name of the class.
     */
    public String binaryName() {
        return slashBinaryName.replace('/', '.');
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the slash-separated binary name of the class.
     */
    public String slashBinaryName() {
        return slashBinaryName;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the simple name (i.e. without the package part) of the class.
     */
    public String name() {
        int lastDot = slashBinaryName.lastIndexOf('.');
        return lastDot < 0
            ? slashBinaryName
            : slashBinaryName.substring(lastDot + 1);
    }

    // ---------------------------------------------------------------------------------------------
}
