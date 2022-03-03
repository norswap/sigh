package norswap.sigh.bytecode;

import norswap.sigh.ast.*;
import norswap.sigh.interpreter.Constructor;
import norswap.sigh.scopes.Scope;
import norswap.sigh.scopes.SyntheticDeclarationNode;
import norswap.sigh.types.*;
import norswap.uranium.Reactor;
import norswap.utils.Vanilla;
import norswap.utils.data.wrappers.Pair;
import norswap.utils.visitors.ValuedVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static norswap.sigh.ast.BinaryOperator.*;
import static norswap.sigh.bytecode.AsmUtils.*;
import static norswap.sigh.bytecode.TypeUtils.fieldDescriptor;
import static norswap.sigh.bytecode.TypeUtils.methodDescriptor;
import static norswap.sigh.bytecode.TypeUtils.*;
import static org.objectweb.asm.Opcodes.*;

/**
 * <h2>Limitations</h2>
 * <ul>
 *     <li>The compiled code currently doesn't support closures (using variables in functions that
 *     are declared in some surroudning scopes outside the function).</li>
 * </ul>
 *
 * <h2>Data Representation</h2>
 * <ul>
 *     <li>{@code Int}, {@code Float}, {@code Bool}: {@code long}, {@code double}, {@code boolean}
 *     (which is manipulated as an integer by JVM instructions, but still has its own representation
 *     in type descriptors)</li>
 *     <li>{@code String}: {@link String}</li>
 *     <li>{@code null}: {@link norswap.sigh.bytecode.Null#INSTANCE}</li>
 *     <li>Arrays: proper array type for the representation of the component. For multi-dimension
 *     arrays, object arrays (e.g. {@code Object[][]}).</li>
 *     <li>Structs: A Java class encoding the structure, in the default package and using the
 *     structure's name.</li>
 *     <li>TODO: Functions: a {@link MethodHandle} to the method that implements the function in
 *     bytecode.</li>
 *     <li>Types: the {@link Class} object for the type representation. {@code Type} itself (the
 *     type of types) is represented by {@code Class.class}.</li>
 * </ul>
 *
 * <h2>Useful Links</h2>
 * <ul>
 *     <li>https://en.wikipedia.org/wiki/Java_bytecode_instruction_listings</li>
 *     <li>https://gist.github.com/ssfang/c28b5d1cfe276e3edf0955ea81c313b7</li>
 *     <li>https://javap.yawk.at</li>
 *     <li>https://asm.ow2.io/asm4-guide.pdf</li>
 * </ul>
 */
public class BytecodeCompiler
{
    // TODO: test with the interpreter tests (merge)
    //       test getting actual return value from run
    // TODO: reference resolution test
    // TODO: check that a string variable is equal to itself
    // TODO: test with existing example source files
    // TODO: inner methods should have name mangling
    // TODO: complete documentation (null, runtime, ...)
    // TODO: check (type) utils for ASM & GeneratorAdapter
    // TODO: prune util classes?
    // TODO: add a top type, and make print take it and convert
    //       (further: is operator, casts, flow casts)
    // TODO: function objects
    // TODO: simplify with https://asm.ow2.io/javadoc/org/objectweb/asm/commons/GeneratorAdapter.html ?

    // ---------------------------------------------------------------------------------------------

    private final ValuedVisitor<SighNode, Object> visitor = new ValuedVisitor<>();
    private final Reactor reactor;

    // ---------------------------------------------------------------------------------------------

    public BytecodeCompiler (Reactor reactor) {
        this.reactor = reactor;

        // expressions
        visitor.register(IntLiteralNode.class,           this::intLiteral);
        visitor.register(FloatLiteralNode.class,         this::floatLiteral);
        visitor.register(StringLiteralNode.class,        this::stringLiteral);
        visitor.register(ReferenceNode.class,            this::reference);
        visitor.register(ConstructorNode.class,          this::constructor);
        visitor.register(ArrayLiteralNode.class,         this::arrayLiteral);
        visitor.register(ParenthesizedNode.class,        this::parenthesized);
        visitor.register(FieldAccessNode.class,          this::fieldAccess);
        visitor.register(ArrayAccessNode.class,          this::arrayAccess);
        visitor.register(FunCallNode.class,              this::funCall);
        visitor.register(UnaryExpressionNode.class,      this::unaryExpression);
        visitor.register(BinaryExpressionNode.class,     this::binaryExpression);
        visitor.register(AssignmentNode.class,           this::assignment);

        // statement groups & declarations
        visitor.register(RootNode.class,                 this::root);
        visitor.register(BlockNode.class,                this::block);
        visitor.register(VarDeclarationNode.class,       this::varDecl);
        visitor.register(FieldDeclarationNode.class,     this::fieldDecl);
        visitor.register(ParameterNode.class,            this::parameter);
        visitor.register(FunDeclarationNode.class,       this::funDecl);
        visitor.register(StructDeclarationNode.class,    this::structDecl);

        // statements
        visitor.register(ExpressionStatementNode.class,  this::expressionStmt);
        visitor.register(IfNode.class,                   this::ifStmt);
        visitor.register(WhileNode.class,                this::whileStmt);
        visitor.register(ReturnNode.class,               this::returnStmt);
    }

    // ---------------------------------------------------------------------------------------------

    /* Slash-separated binary class name for the class containing the emitted bytecode for the source
     * unit. */
    private String containerName;

    /* Class visitor for the class containing the emitted bytecode for the source unit. */
    private ClassWriter container;

    /* Class writer for the class representing the struct currently being emitted. */
    private ClassWriter struct;

    /** The list of (class name, class writer) pairs for the classes representing the structures
     * defined in the source unit. */
    ArrayList<Pair<String, ClassWriter>> structs = new ArrayList<>();

    /* MethodVisitor for current method. */
    private MethodVisitor method;

    /** Maps variables in a scope to a variable index. */
    private final HashMap<Pair<Scope, String>, Integer> variables = new HashMap<>();

    /** Counter used to number variables in {@link #variables}. */
    private int variableCounter = 0;

    /** Whether we are in top-level code. */
    private boolean topLevel;

    // ---------------------------------------------------------------------------------------------

    /**
     * Compile the given source unit (given as its root AST node) into a class whose (dot-separated)
     * binary name is {@code binaryName}.
     */
    public CompilationResult compile (String binaryName, SighNode root)
    {
        this.containerName = binaryName.replace('.', '/');
        run(root);
        GeneratedClass mainClass = new GeneratedClass(containerName, container.toByteArray());
        List<GeneratedClass> structClasses = structs.stream()
            .map(it -> new GeneratedClass(it.a, it.b.toByteArray()))
            .collect(Collectors.toList());

        return new CompilationResult(mainClass, structClasses);
    }

    // ---------------------------------------------------------------------------------------------

    private Object run (SighNode node) {
        return visitor.apply(node);
    }

    // ---------------------------------------------------------------------------------------------

    private Object root (RootNode node)
    {
        container = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        container.visit(V1_8, ACC_PUBLIC, containerName, null, "java/lang/Object", null);

        // Top-level code belongs in the run method.
        method = container.visitMethod(ACC_PUBLIC | ACC_STATIC, "run",
            "([Ljava/lang/String;)Ljava/lang/Object;", null, null);
        method.visitCode();
        topLevel = true;
        node.statements.forEach(this::run);
        // append "return null;" since we do not know if top-level code always returns
        loadConstant(method, null);
        method.visitInsn(ARETURN);
        method.visitEnd();
        method.visitMaxs(-1, -1);
        container.visitEnd();

        // Traditional java main method to run standalone.
        // This just calls run, ignoring its return value.
        method = container.visitMethod(ACC_PUBLIC | ACC_STATIC, "main",
            "([Ljava/lang/String;)V", null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitMethodInsn(INVOKESTATIC, containerName,
            "run", "([Ljava/lang/String;)Ljava/lang/Object;", false);
        method.visitInsn(POP);
        method.visitInsn(RETURN); // explicitly necessary
        method.visitEnd();
        method.visitMaxs(-1, -1);
        container.visitEnd();
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Object funDecl (FunDeclarationNode node)
    {
        int surroundingVariableCounter = variableCounter;
        MethodVisitor surroundingMethod = method;
        boolean surroundingIsTopLevel = topLevel;

        variableCounter = 0;
        topLevel = false;
        node.parameters.forEach(this::run);

        String descriptor = methodDescriptor(reactor.get(node, "type"));
        method = container.visitMethod(ACC_PUBLIC | ACC_STATIC, node.name, descriptor, null, null);
        method.visitCode();
        run(node.block);

        // NOTE: The current semantic analysis check guarantee that there is we unconditionally
        // return. So we do not have to worry about instructions not followed by a return.
        // The only exception is for void methods - so we always add a return at the end in that
        // case. In the future, it might be good to check that nothing follows a return in semantic
        // analysis.
        if (descriptor.endsWith("V"))
            method.visitInsn(RETURN);

        method.visitEnd();
        method.visitMaxs(-1, -1);

        method = surroundingMethod;
        variableCounter = surroundingVariableCounter;
        topLevel = surroundingIsTopLevel;
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Object intLiteral (IntLiteralNode node) {
        method.visitLdcInsn(node.value);
        return null;
    }

    private Object floatLiteral (FloatLiteralNode node) {
        method.visitLdcInsn(node.value);
        return null;
    }

    private Object stringLiteral (StringLiteralNode node) {
        method.visitLdcInsn(node.value);
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Object parenthesized (ParenthesizedNode node) {
        return run(node.expression);
    }

    // ---------------------------------------------------------------------------------------------

    private Object arrayLiteral (ArrayLiteralNode node)
    {
        ArrayType type = reactor.get(node, "type");
        Type compType = type.componentType;

        method.visitLdcInsn(node.components.size());
        int storeOpcode = AASTORE;

        if (compType instanceof IntType) {
            method.visitIntInsn(NEWARRAY, T_LONG);
            storeOpcode = LASTORE;
        } else if (compType instanceof FloatType) {
            method.visitIntInsn(NEWARRAY, T_DOUBLE);
            storeOpcode = DASTORE;
        } else if (compType instanceof BoolType) {
            method.visitIntInsn(NEWARRAY, T_BOOLEAN);
            storeOpcode = IASTORE;
        } else if (compType instanceof StringType) {
            method.visitTypeInsn(ANEWARRAY, "java/lang/String");
        } else if (compType instanceof TypeType) {
            method.visitTypeInsn(ANEWARRAY, "norswap/sigh/types/TypeType");
        } else if (compType instanceof FunType) {
            throw new UnsupportedOperationException("TODO"); // TODO
        } else if (compType instanceof NullType) {
            method.visitTypeInsn(ANEWARRAY, "norswap/sigh/bytecode/Null");
        } else if (compType instanceof VoidType || compType instanceof ArrayType) {
            // Sigh does not have a syntax for multi-dimensional arrays, so use an array of
            // Object that we'll be able to cast to array themselves.
            method.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        } else if (compType instanceof StructType) {
            method.visitTypeInsn(ANEWARRAY, structBinaryName((StructType) compType));
        }

        int i = 0;
        for (ExpressionNode component: node.components) {
            method.visitInsn(DUP); // duplicate the array
            loadConstant(method, i++);
            run(component);
            method.visitInsn(storeOpcode);
        }

        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Object binaryExpression (BinaryExpressionNode node)
    {
        if (isShortCircuit(node.operator))
            return shortCircuit(node);

        run(node.left);

        Type left  = reactor.get(node.left, "type");
        Type right = reactor.get(node.right, "type");

        // promote long to double for mixed operations
        if (enablesPromotion(node.operator) && left instanceof IntType && right instanceof FloatType)
            method.visitInsn(L2D);

        // string concatenation: convert value to string
        if (node.operator == ADD && right instanceof StringType)
            convertToString(left);

        run(node.right);

        switch (node.operator) {
            case ADD:
                if (left instanceof StringType) {
                    convertToString(right);
                    invokeStatic(method, SighRuntime.class, "concat", String.class, String.class);
                } else if (right instanceof StringType) {
                    // left already converted to string in this case
                    invokeStatic(method, SighRuntime.class, "concat", String.class, String.class);
                } else {
                    numOperation(LADD, DADD, left, right);
                } break;

            case MULTIPLY:  numOperation(LMUL, DMUL, left, right); break;
            case DIVIDE:    numOperation(LDIV, DDIV, left, right); break;
            case REMAINDER: numOperation(LREM, DREM, left, right); break;
            case SUBTRACT:  numOperation(LSUB, DSUB, left, right); break;

            case EQUALITY:
                comparison(node.operator, IFEQ, IF_ICMPEQ, IF_ACMPEQ, left, right); break;
            case NOT_EQUALS:
                comparison(node.operator, IFNE, IF_ICMPNE, IF_ACMPNE, left, right); break;
            case GREATER:
                comparison(node.operator, IFGT, -1, -1, left, right); break;
            case LOWER:
                comparison(node.operator, IFLT, -1, -1, left, right); break;
            case GREATER_EQUAL:
                comparison(node.operator, IFGE, -1, -1, left, right); break;
            case LOWER_EQUAL:
                comparison(node.operator, IFLE, -1, -1, left, right); break;

            // default: throw an exception
        }

        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private boolean enablesPromotion (BinaryOperator op) {
        return isArithmetic(op) || isComparison(op) || isEquality(op);
    }

    private boolean isShortCircuit (BinaryOperator op) {
        return op == AND || op == OR;
    }

    private boolean isArithmetic (BinaryOperator op) {
        return op == ADD || op == MULTIPLY || op == SUBTRACT || op == DIVIDE || op == REMAINDER;
    }

    private boolean isComparison (BinaryOperator op) {
        return op == GREATER || op == GREATER_EQUAL || op == LOWER || op == LOWER_EQUAL;
    }

    private boolean isEquality (BinaryOperator op) {
        return op == EQUALITY || op == NOT_EQUALS;
    }

    // ---------------------------------------------------------------------------------------------

    private Object shortCircuit (BinaryExpressionNode node)
    {
        int opcode = node.operator == AND ? IFEQ /* if 0 */ : /* OR */ IFNE /* if 1 */;
        Label endLabel = new Label();
        run(node.left);
        method.visitInsn(DUP);
        method.visitJumpInsn(opcode, endLabel);
        method.visitInsn(POP);
        run(node.right);
        method.visitLabel(endLabel);
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    @SuppressWarnings("StatementWithEmptyBody")
    private void convertToString (Type type)
    {
        if (type instanceof StringType) {
            // skip
        } else if (type instanceof IntType) {
            invokeStatic(method, String.class, "valueOf", long.class);
        } else if (type instanceof FloatType) {
            invokeStatic(method, String.class, "valueOf", double.class);
        } else if (type instanceof BoolType) {
            invokeStatic(method, String.class, "valueOf", boolean.class);
        } else if (type instanceof NullType) {
            method.visitInsn(POP);
            method.visitLdcInsn("null");
        } else if (type instanceof ArrayType) {
            Type component = ((ArrayType) type).componentType;
            if (component.isPrimitive())
                invokeStatic(method, Arrays.class, "toString", javaArrayClass(component));
            else
                invokeStatic(method, Arrays.class, "deepToString", Object[].class);
        } else if (type instanceof TypeType) {
            // String.valueOf -> Type#toString -> Type#name
            invokeStatic(method, String.class, "valueOf", Object.class);
        } else if (type instanceof FunType) {
            throw new UnsupportedOperationException("TODO"); // TODO
        } else if (type instanceof StructType) {
            // String.valueOf -> Object#toString (or override)
            invokeStatic(method, String.class, "valueOf", Object.class);
        } else {
            throw new Error("unexpected type: " + type);
        }
    }

    // ---------------------------------------------------------------------------------------------

    private void numOperation(int longOpcode, int doubleOpcode, Type left, Type right)
    {
        if (left instanceof IntType && right instanceof IntType) {
            method.visitInsn(longOpcode);
        } else if (left instanceof FloatType && right instanceof FloatType) {
            method.visitInsn(doubleOpcode);
        } else if (left instanceof FloatType && right instanceof IntType) {
            method.visitInsn(L2D);
            method.visitInsn(doubleOpcode);
        } else if (left instanceof IntType && right instanceof FloatType) {
            // in this case, we've added a L2D instruction before the long operand beforehand
            method.visitInsn(doubleOpcode);
        } else {
            throw new Error("unexpected numeric operation type combination: " + left + ", " + right);
        }
    }

    // ---------------------------------------------------------------------------------------------

    private void comparison (
            BinaryOperator op,
            int doubleWidthOpcode, int boolOpcode, int objOpcode,
            Type left, Type right) {

        Label trueLabel = new Label();
        Label endLabel = new Label();

        if (left instanceof IntType && right instanceof IntType) {
            method.visitInsn(LCMP);
            method.visitJumpInsn(doubleWidthOpcode, trueLabel);
        } else if ((left instanceof FloatType || left instanceof IntType) && right instanceof FloatType) {
            // If left is an Int, we've added a L2D instruction before the long operand beforehand
            // Proper NaN handling: if NaN is involved, has to be false for all operations.
            int opcode = op == LOWER || op == LOWER_EQUAL ? DCMPG : DCMPL;
            method.visitInsn(opcode);
            method.visitJumpInsn(doubleWidthOpcode, trueLabel);
        } else if (left instanceof FloatType && right instanceof IntType) {
            method.visitInsn(L2D);
            // Proper NaN handling: if NaN is involved, has to be false for all operations.
            int opcode = op == LOWER || op == LOWER_EQUAL ? DCMPG : DCMPL;
            method.visitInsn(opcode);
            method.visitJumpInsn(doubleWidthOpcode, trueLabel);
        } else if (left instanceof BoolType && right instanceof BoolType) {
            method.visitJumpInsn(boolOpcode, trueLabel);
        } else {
            method.visitJumpInsn(objOpcode, trueLabel);
        }

        method.visitInsn(ICONST_0);
        method.visitJumpInsn(GOTO, endLabel);
        method.visitLabel(trueLabel);
        method.visitInsn(ICONST_1);
        method.visitLabel(endLabel);
    }

    // ---------------------------------------------------------------------------------------------

    private Object unaryExpression (UnaryExpressionNode node)
    {
        // there is only NOT
        assert node.operator == UnaryOperator.NOT;

        run(node.operand);
        Label falseLabel = new Label();
        Label endLabel = new Label();
        method.visitJumpInsn(IF_ZERO, falseLabel);
        method.visitInsn(ICONST_0);
        method.visitJumpInsn(GOTO, endLabel);
        method.visitLabel(falseLabel);
        method.visitInsn(ICONST_1);
        method.visitLabel(endLabel);
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Object arrayAccess (ArrayAccessNode node)
    {
        run(node.array);
        run(node.index);
        method.visitInsn(L2I); // indices must be 32-bit int
        method.visitInsn(nodeAsmType(node).getOpcode(IALOAD));
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Object funCall (FunCallNode node)
    {
        FunType funType = reactor.get(node.function, "type");

        // The function part can either be a reference, in which case we emit a call,
        // or a more complex expression, which will evaluate to a lambda.

        if (node.function instanceof ReferenceNode) {
            DeclarationNode decl = reactor.get(node.function, "decl");
            if (decl instanceof SyntheticDeclarationNode) {
                return builtin(funType, decl.name(), node.arguments);
            }
            else if (decl instanceof FunDeclarationNode) {
                runArguments(funType, node.arguments);
                method.visitMethodInsn(INVOKESTATIC, containerName,
                    decl.name(), methodDescriptor(funType), false);
            }
            else { // TODO
                throw new UnsupportedOperationException("variables or parameters containing a function value");
            }
        }
        else if (node.function instanceof ConstructorNode) {
            StructDeclarationNode decl = reactor.get(((ConstructorNode) node.function).ref, "decl");
            String binaryName = structBinaryName(reactor.get(decl, "declared"));
            method.visitTypeInsn(NEW, binaryName);
            method.visitInsn(DUP);
            runArguments(funType, node.arguments);
            String descriptor = methodDescriptor(VoidType.INSTANCE, funType.paramTypes);
            method.visitMethodInsn(INVOKESPECIAL, binaryName, "<init>", descriptor, false);
        }
        else
            throw new UnsupportedOperationException("complex expression evaluating to a function value");

        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Object builtin (FunType funType, String name, List<ExpressionNode> arguments)
    {
        assert name.equals("print"); // only one at the moment
        method.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
            "Ljava/io/PrintStream;");
        runArguments(funType, arguments);
        method.visitInsn(DUP_X1); // we return the printed string!
        method.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println",
            "(Ljava/lang/String;)V", false);
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Visit all argument nodes, adding implicit conversion based on the target parameter type
     * if needed.
     */
    private void runArguments (FunType funType, List<ExpressionNode> arguments)
    {
        Vanilla.forEachIndexed(arguments, (i, arg) -> {
            run(arg);
            implicitConversion(funType.paramTypes[i], reactor.get(arg, "type"));
        });
    }

    // ---------------------------------------------------------------------------------------------

    private Object expressionStmt (ExpressionStatementNode node) {
        run(node.expression);
        if (node.expression instanceof AssignmentNode)
            pop(reactor.get(node.expression, "type"));
        else if (node.expression instanceof FunCallNode) {
            Type type = reactor.get(node.expression, "type");
            if (!(type instanceof VoidType)) pop(type);
        }
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Object returnStmt (ReturnNode node) {
        if (node.expression == null) {
            if (topLevel) {
                loadConstant(method, null);
                method.visitInsn(ARETURN);
            } else {
                method.visitInsn(RETURN);
            }
            return null;
        }

        run(node.expression);

        if (topLevel) {
            Type type = reactor.get(node.expression, "type");
            if (type instanceof IntType)
                invokeStatic(method, Long.class, "valueOf", long.class);
            else if (type instanceof FloatType)
                invokeStatic(method, Double.class, "valueOf", double.class);
            else if (type instanceof BoolType)
                invokeStatic(method, Boolean.class, "valueOf", boolean.class);
            method.visitInsn(ARETURN);
        } else {
            method.visitInsn(nodeAsmType(node.expression).getOpcode(IRETURN));
        }

        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Object block (BlockNode node) {
        node.statements.forEach(this::run);
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Object ifStmt (IfNode node)
    {
        Label elseLabel = new Label();
        Label endLabel = new Label();
        boolean hasElse = node.falseStatement != null;
        run(node.condition);
        method.visitJumpInsn(IFEQ, hasElse ? elseLabel : endLabel);
        run(node.trueStatement);
        if (hasElse) {
            method.visitJumpInsn(GOTO, endLabel);
            method.visitLabel(elseLabel);
            run(node.falseStatement);
        }
        method.visitLabel(endLabel);
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Object whileStmt (WhileNode node)
    {
        Label startLabel = new Label();
        Label endLabel = new Label();
        method.visitLabel(startLabel);
        run(node.condition);
        method.visitJumpInsn(IFEQ, endLabel);
        run(node.body);
        method.visitJumpInsn(GOTO, startLabel);
        method.visitLabel(endLabel);
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Object reference (ReferenceNode node)
    {
        DeclarationNode decl = reactor.get(node, "decl");

        // TODO distinguish local variables from closures
        if (decl instanceof VarDeclarationNode || decl instanceof ParameterNode) {
            method.visitVarInsn(nodeAsmType(node).getOpcode(ILOAD), varIndex(node));
        }
        else if (decl instanceof StructDeclarationNode) {
            // NOTE: This is not used when the reference is part of a constructor call, the
            // resolution is handled in #funCall.
            org.objectweb.asm.Type asmType = asmType(reactor.get(decl, "declared"));
            method.visitLdcInsn(asmType); // class constant for emitted type
        }
        else if (decl instanceof FunDeclarationNode) {
            // NOTE: This is not used when the reference is part of a function call, the resolution
            // is handled in #funCall.

            // TODO proper handling/representation of function object
            //  For now I use a method handle. There is no way to use it in the language however.
            method.visitLdcInsn(new Handle(
                H_INVOKESTATIC, containerName, decl.name(),
                methodDescriptor(reactor.get(decl, "type")), false));
        }
        else if (decl instanceof SyntheticDeclarationNode) {
            switch (decl.name()) {
                case "Bool":
                    method.visitLdcInsn(org.objectweb.asm.Type.getType(boolean.class));
                    break;
                case "Int":
                    method.visitLdcInsn(org.objectweb.asm.Type.getType(long.class));
                    break;
                case "Float":
                    method.visitLdcInsn(org.objectweb.asm.Type.getType(double.class));
                    break;
                case "String":
                    method.visitLdcInsn(org.objectweb.asm.Type.getType(String.class));
                    break;
                case "Void":
                    method.visitLdcInsn(org.objectweb.asm.Type.getType(void.class));
                    break;
                case "Type":
                    method.visitLdcInsn(org.objectweb.asm.Type.getType(Class.class));
                    break;
                case "print":
                    // TODO cf FunDeclarationNode case above
                    method.visitLdcInsn(staticHandle(SighRuntime.class, "print", String.class));
                    break;
                case "true":  loadConstant(method, 1);      break;
                case "false": loadConstant(method, 0);      break;
                case "null":  loadConstant(method, null);   break;
                default: throw new Error("unreachable");
            }
        }

        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Object varDecl (VarDeclarationNode node)
    {
        org.objectweb.asm.Type type = nodeAsmType(node);
        int index = registerVariable(node, type);
        run(node.initializer);
        implicitConversion(node, node.initializer);
        method.visitVarInsn(type.getOpcode(ISTORE), index);
        // LATER: method.visitLocalVariable for debug information
        // https://stackoverflow.com/questions/28633731
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Object parameter (ParameterNode node) {
        registerVariable(node);
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    public Object assignment (AssignmentNode node)
    {
        if (node.left instanceof ReferenceNode) {
            ReferenceNode left = (ReferenceNode) node.left;
            run(node.right);
            Type type = implicitConversion(node, node.right);
            dup(type);
            method.visitVarInsn(nodeAsmType(node).getOpcode(ISTORE), varIndex(left));
        }
        else if (node.left instanceof ArrayAccessNode) {
            ArrayAccessNode left = (ArrayAccessNode) node.left;
            run(left.array);
            run(left.index);
            method.visitInsn(L2I);
            run(node.right);
            Type type = implicitConversion(node, node.right);
            dup_x2(type);
            method.visitInsn(nodeAsmType(node).getOpcode(IASTORE));
        }
        else if (node.left instanceof FieldAccessNode) {
            FieldAccessNode left = (FieldAccessNode) node.left;
            run(left.stem);
            run(node.right);
            Type type = implicitConversion(node, node.right);
            dup_x1(type);
            StructType structType = reactor.get(left.stem, "type");
            Type fieldType = reactor.get(node, "type");
            method.visitFieldInsn(PUTFIELD, structBinaryName(structType), left.fieldName,
                fieldDescriptor(fieldType));
        }
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Object structDecl (StructDeclarationNode node)
    {
        String binaryName = node.name;
        struct = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        struct.visit(V1_8, ACC_PUBLIC, binaryName, null, "java/lang/Object", null);
        node.fields.forEach(this::run);

        // generate constructor
        Type[] paramTypes =
            node.fields.stream().map(f -> (Type) reactor.get(f, "type")).toArray(Type[]::new);
        String descriptor = methodDescriptor(VoidType.INSTANCE, paramTypes);
        MethodVisitor init = struct.visitMethod(ACC_PUBLIC, "<init>", descriptor, null, null);
        init.visitCode();
        init.visitVarInsn(ALOAD, 0); // this
        init.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        int i = 1;
        for (FieldDeclarationNode field: node.fields) {
            init.visitVarInsn(ALOAD, 0);
            org.objectweb.asm.Type type = nodeAsmType(field);
            init.visitVarInsn(type.getOpcode(ILOAD), i);
            i += type.getSize();
            init.visitFieldInsn(PUTFIELD, binaryName, field.name, type.getDescriptor());
        }
        init.visitInsn(RETURN);
        init.visitMaxs(-1,-1);
        init.visitEnd();

        struct.visitEnd();
        structs.add(new Pair<>(binaryName, struct));
        struct = null;
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Object fieldDecl (FieldDeclarationNode node)
    {
        struct.visitField(ACC_PUBLIC, node.name, nodeFieldDescriptor(node), null, null);
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Object fieldAccess (FieldAccessNode node) {
        run(node.stem);
        String binaryName = asmType(reactor.get(node.stem, "type")).getClassName();
        method.visitFieldInsn(GETFIELD, binaryName, node.fieldName, nodeFieldDescriptor(node));
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Constructor constructor (ConstructorNode node) {
        // not needed - handled in funCall instead
        return null;
    }

    // =============================================================================================
    // region [Utilities]
    // =============================================================================================

    /**
     * Returns the {@link org.objectweb.asm.Type ASM Type} for the {@code type} attribute of the
     * given node.
     */
    private org.objectweb.asm.Type nodeAsmType (SighNode node) {
        return asmType(reactor.get(node, "type"));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Return the JVM field descriptor for the given node, which must have a {@code type} attribute.
     */
    private String nodeFieldDescriptor (SighNode node) {
        return fieldDescriptor(reactor.get(node, "type"));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Declares a variable introduce by the given declaration (which must be a {@link
     * VarDeclarationNode} or {@link ParameterNode}, and returns its index in its JVM method
     * scope.
     */
    private int registerVariable (DeclarationNode node) {
        return registerVariable(node, nodeAsmType(node));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * See {@link #registerVariable(DeclarationNode)}. Takes the ASM type of the declaration
     * to compute faster in case it is already available.
     */
    private int registerVariable (DeclarationNode node, org.objectweb.asm.Type type) {
        int index = variableCounter;
        variableCounter += type.getSize();
        variables.put(new Pair<>(reactor.get(node, "scope"), node.name()), index);
        return index;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the variable index for the given reference, which must be a reference to a variable.
     */
    private int varIndex (ReferenceNode node) {
        return variables.get(new Pair<>((Scope) reactor.get(node, "scope"), node.name));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Implicitly converts the value at the top of the stack (of type {@code right}) to the type
     * {@code left} if compatible, in which case {@code left} is returned. Otherwise returns {@code
     * right}.
     */
    private Type implicitConversion (Type left, Type right) {
        if (left instanceof FloatType && right instanceof IntType) {
            method.visitInsn(L2D);
            return left;
        }
        return right;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Like {@link #implicitConversion(Type, Type)}, using the type attributes of the passed nodes.
     */
    private Type implicitConversion (SighNode left, SighNode right) {
        return implicitConversion((Type) reactor.get(left,  "type"), reactor.get(right, "type"));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Calls the right dup instruction depending on {@code type}, the type of the value at the top
     * of the stack.
     */
    private void dup (Type type) {
        if (type instanceof FloatType || type instanceof IntType)
            method.visitInsn(DUP2);
        else
            method.visitInsn(DUP);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Calls the right dup_x2 ([x, y] => [y, x, y]) instruction depending on {@code type}, the type
     * of the value at the top of the stack.
     */
    private void dup_x1 (Type type) {
        if (type instanceof FloatType || type instanceof IntType)
            method.visitInsn(DUP2_X1);
        else
            method.visitInsn(DUP_X1);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Calls the right dup_x2 ([x, y, z] => [z, x, y, z]) instruction depending on {@code type}, the
     * type of the value at the top of the stack.
     */
    private void dup_x2 (Type type) {
        if (type instanceof FloatType || type instanceof IntType)
            method.visitInsn(DUP2_X2);
        else
            method.visitInsn(DUP_X2);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Calls the right pop instruction depending on {@code type}, the type of the value at the top
     * of the stack.
     */
    private void pop (Type type) {
        if (type instanceof FloatType || type instanceof IntType)
            method.visitInsn(POP2);
        else
            method.visitInsn(POP);
    }

    // endregion
    // ---------------------------------------------------------------------------------------------
}
