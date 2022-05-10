package norswap.sigh.bytecode;

import norswap.utils.exceptions.NoStackException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Represents the result of compiling a single source unit (~ a source file, but it could
 * be programatically generated instead of being read form a file).
 *
 * <p>This result is a set of {@link GeneratedClass} corresponding to the emitted JVM classes.
 */
public final class CompilationResult
{
    // ---------------------------------------------------------------------------------------------

    /**
     * The generated class coresponding to the compiled source unit.
     */
    public final GeneratedClass mainClass;

    // ---------------------------------------------------------------------------------------------

    /**
     * The generated classes corresponding to structures defined in the source unit.
     */
    public final List<GeneratedClass> structures;

    // ---------------------------------------------------------------------------------------------

    public CompilationResult (GeneratedClass mainClass, List<GeneratedClass> structures) {
        this.mainClass = mainClass;
        this.structures = structures;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Load this class in the current JVM using the given class loader and return the
     * {@link Class} object for {@link #mainClass}.
     */
    public Class<?> load (ByteArrayClassLoader loader) {
        structures.forEach(it -> it.load(loader));
        return mainClass.load(loader);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Load the generated class in the current JVM (using {@link ByteArrayClassLoader#INSTANCE} and
     * return the {@link Class} object for {@link #mainClass}.
     */
    public Class<?> load() {
        return load(ByteArrayClassLoader.INSTANCE);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Call the {@code run} method of the given class, passing it the given arguments.
     *
     * <p>Unlike the {@code main} method, the {@code run} method is able to return a value!
     * The {@code main} method just calls the {@code run} method and ignores its return value.
     */
    public static Object callRun (Class<?> mainClass, String... args) {
        try {
            // Object cast: the string array is one argument in the vararg!
            return mainClass.getMethod("run", String[].class).invoke(null, (Object) args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new NoStackException(e);
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Call the {@code run} method of the given class, see {@link #callRun}.
     */
    public static void callRun (Class<?> mainClass) {
        callMain(mainClass, new String[0]);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Call the {@code main} method of the given class, passing it the given arguments.
     *
     * <p>The {@code main} method just calls the {@code run} method, ignoring its return value.
     */
    public static void callMain (Class<?> mainClass, String... args) {
        try {
            // Object cast: the string array is one argument in the vararg!
            mainClass.getMethod("main", String[].class).invoke(null, (Object) args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new NoStackException(e);
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Call the {@code main} method of the given class, see {@link #callMain}.
     */
    public static void callMain (Class<?> mainClass) {
        callMain(mainClass, new String[0]);
    }

    // ---------------------------------------------------------------------------------------------
}
