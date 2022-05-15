package norswap.sigh.interpreter;

import norswap.sigh.SighGrammar;
import norswap.sigh.ast.*;
import norswap.sigh.scopes.DeclarationKind;
import norswap.sigh.scopes.RootScope;
import norswap.sigh.scopes.Scope;
import norswap.sigh.scopes.SyntheticDeclarationNode;
import norswap.sigh.types.ArrayType;
import norswap.sigh.types.FloatType;
import norswap.sigh.types.IntType;
import norswap.sigh.types.StringType;
import norswap.sigh.types.Type;
import norswap.uranium.Reactor;
import norswap.utils.Util;
import norswap.utils.exceptions.Exceptions;
import norswap.utils.exceptions.NoStackException;
import norswap.utils.visitors.ValuedVisitor;
import java.util.*;

import static norswap.utils.Util.cast;
import static norswap.utils.Vanilla.coIterate;
import static norswap.utils.Vanilla.map;

/**
 * Implements a simple but inefficient interpreter for Sigh.
 *
 * <h2>Limitations</h2>
 * <ul>
 *     <li>The compiled code currently doesn't support closures (using variables in functions that
 *     are declared in some surroudning scopes outside the function). The top scope is supported.
 *     </li>
 * </ul>
 *
 * <p>Runtime value representation:
 * <ul>
 *     <li>{@code Int}, {@code Float}, {@code Bool}: {@link Long}, {@link Double}, {@link Boolean}</li>
 *     <li>{@code String}: {@link String}</li>
 *     <li>{@code null}: {@link Null#INSTANCE}</li>
 *     <li>Arrays: {@code Object[]}</li>
 *     <li>Structs: {@code HashMap<String, Object>}</li>
 *     <li>Functions: the corresponding {@link DeclarationNode} ({@link FunDeclarationNode} or
 *     {@link SyntheticDeclarationNode}), excepted structure constructors, which are
 *     represented by {@link Constructor}</li>
 *     <li>Types: the corresponding {@link StructDeclarationNode}</li>
 * </ul>
 */
public final class Interpreter
{
    // ---------------------------------------------------------------------------------------------

    private final ValuedVisitor<SighNode, Object> visitor = new ValuedVisitor<>();
    private final Reactor reactor;
    private ScopeStorage storage = null;
    private RootScope rootScope;
    private ScopeStorage rootStorage;
    private Object obj;
    // ---------------------------------------------------------------------------------------------

    public Interpreter (Reactor reactor) {
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
        visitor.register(ArrayDeclarationNode.class,     this::arrayDecl);
        // no need to visitor other declarations! (use fallback)

        // statements
        visitor.register(ExpressionStatementNode.class,  this::expressionStmt);
        visitor.register(IfNode.class,                   this::ifStmt);
        visitor.register(WhileNode.class,                this::whileStmt);
        visitor.register(ReturnNode.class,               this::returnStmt);

        visitor.registerFallback(node -> null);
    }

    // ---------------------------------------------------------------------------------------------

    public Object interpret (SighNode root) {
        try {
            return run(root);
        } catch (PassthroughException e) {
            throw Exceptions.runtime(e.getCause());
        }
    }

    // ---------------------------------------------------------------------------------------------

    private Object run (SighNode node) {
        try {
            return visitor.apply(node);
        } catch (InterpreterException | Return | PassthroughException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new InterpreterException("exception while executing " + node, e);
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Used to implement the control flow of the return statement.
     */
    private static class Return extends NoStackException {
        final Object value;
        private Return (Object value) {
            this.value = value;
        }
    }

    // ---------------------------------------------------------------------------------------------

    private <T> T get(SighNode node) {
        return cast(run(node));
    }

    // ---------------------------------------------------------------------------------------------

    private Long intLiteral (IntLiteralNode node) {
        return node.value;
    }

    private Double floatLiteral (FloatLiteralNode node) {
        return node.value;
    }

    private String stringLiteral (StringLiteralNode node) {
        return node.value;
    }

    // ---------------------------------------------------------------------------------------------

    private Object parenthesized (ParenthesizedNode node) {
        return get(node.expression);
    }

    // ---------------------------------------------------------------------------------------------

    private Object[] arrayLiteral (ArrayLiteralNode node) {
        return map(node.components, new Object[0], visitor);
    }

    // ---------------------------------------------------------------------------------------------

    private Object[] arrayOperate(Object lArray,Object rArray, int op){
        if(!(lArray instanceof Object[]) || !(rArray instanceof  Object[]))
            throw new InterpreterException("Operation between an array and a scalar not implemented yet", new Exception());
        Object[] leftArray=(Object[]) lArray;
        Object[] rightArray=(Object[]) rArray;
        if(leftArray.length!=rightArray.length){
            throw new InterpreterException("Try to operate on two arrays with different sizes",new Exception());
        }
        if(leftArray[0] instanceof Double ){
            if (rightArray[0] instanceof Double) {
                Double[] result = new Double[leftArray.length];
                for (int i = 0; i < leftArray.length; i++) {
                    switch (op){
                        case 0:
                            result[i] = (double) leftArray[i] + (double) rightArray[i];
                            break;
                        case 1:
                            result[i] = (double) leftArray[i] - (double) rightArray[i];
                            break;
                        case 2:
                            result[i] = (double) leftArray[i] * (double) rightArray[i];
                            break;
                        case 3:
                            if((double) rightArray[i]==0)
                                throw new ArithmeticException("Division by zero");
                            result[i] = (double) leftArray[i] / (double) rightArray[i];
                            break;
                        default:
                            result[i] = null;
                            break;
                    }
                }
                return result;
            }else{
                throw new InterpreterException("Try to operate on two arrays with different types: "+leftArray.getClass()+
                    " and "+rightArray.getClass(),new ArithmeticException());
            }
        }
        else if(leftArray[0] instanceof Long ){
            if (rightArray[0] instanceof Long) {
                Long[] result = new Long[leftArray.length];
                for (int i = 0; i < leftArray.length; i++) {
                    switch (op) {
                        case 0:
                            result[i] = (long) leftArray[i] + (long) rightArray[i];
                            break;
                        case 1:
                            result[i] = (long) leftArray[i] - (long) rightArray[i];
                            break;
                        case 2:
                            result[i] = (long) leftArray[i] * (long) rightArray[i];
                            break;
                        case 3:
                            if ((long) rightArray[i] == 0)
                                throw new ArithmeticException("Division by zero");
                            result[i] = (long) leftArray[i] / (long) rightArray[i];
                            break;
                        default:
                            result[i] = null;
                            break;
                    }
                }
                return result;
            }
            else{
                throw new InterpreterException("Try to operate on two arrays with different types: "+leftArray.getClass()+
                    " and "+rightArray.getClass(),new ArithmeticException());
            }
        }
        else if(leftArray[0] instanceof Object[]){
            if(rightArray[0] instanceof Object[]){
                Object[] result=new Object[leftArray.length];
                for(int i=0;i<leftArray.length;i++){
                    result[i]=arrayOperate(leftArray[i],rightArray[i],op);
                }
                return result;
            }
        }
        else if (leftArray[0] instanceof HashMap && rightArray[0] instanceof HashMap  ){
            Object[] result = new Object[leftArray.length];
            Object oldobj = obj;
            for (int i=0;i< leftArray.length;i++){
                Object decl = null;
                switch (op) {
                    case 0: decl = "plus"; break;
                    case 1: decl = "minus"; break;
                    case 2: decl = "mul"; break;
                    case 3: decl = "div"; break;

                }
                FunDeclarationNode b1 = (FunDeclarationNode) ((HashMap)leftArray[i]).get(decl);
                if(b1==null){
                    throw new InterpreterException("You must implement "+ decl+ " method",new Exception());
                }
                Object[] args = new Object[]{rightArray[i]};
                ScopeStorage oldStorage = storage;
                Scope scope = reactor.get(b1, "scope");
                storage = new ScopeStorage(scope, storage);
                coIterate(args, b1.parameters,
                    (arg, param) -> storage.set(scope, param.name, arg));

                obj = leftArray[i];
                try {
                    get(b1.block);
                } catch (Return r) {
                    result[i]= r.value;
                } finally {
                    storage = oldStorage;
                }
            }
            obj=oldobj;
            return result;
        }
        else{
            throw new InterpreterException("Operation not implemented",new Exception());
        }
        return null;
    }

    private Object matrixOperate(Object lArray, Object rArray, int op){
        if(!(lArray instanceof Object[]) || !(rArray instanceof  Object[]))
            throw new InterpreterException("Trying to operate on non-array type", new Exception());
        Object[] leftArray=(Object[]) lArray;
        Object[] rightArray=(Object[]) rArray;
        if(leftArray[0] instanceof Long && rightArray[0] instanceof Long){
            if(leftArray.length!=rightArray.length){
                throw new InterpreterException("Trying to operate on arrays with different dimensions", new Exception());
            }
            Long result=Long.valueOf(0);
            for(int i=0;i<leftArray.length;i++){
                result+=(Long)leftArray[i]*(Long)rightArray[i];
            }
            return result;
        }
        if(leftArray[0] instanceof Long && (rightArray[0] instanceof Object[]) ){
            if(rightArray.length>1 && ((Object[])rightArray).length!=leftArray.length){
                throw new InterpreterException("Trying to operate on arrays with different dimensions", new Exception());
            }
            Long result=Long.valueOf(0);
            for(int i=0;i<leftArray.length;i++){
                result+=(Long)leftArray[i]*(Long)((Object[])rightArray[i])[0];
            }
            return result;
        }
        if(!(leftArray[0] instanceof Object[])||!(rightArray[0] instanceof Object[])
            ||(((Object[])leftArray[0])[0] instanceof Object[]) ||(((Object[])rightArray[0])[0] instanceof Object[]))
            throw new InterpreterException("Trying to operate on non-array type", new Exception());

        switch (op) {
            case 0:
                if (((Object[]) leftArray[0])[0] instanceof Long && (((Object[]) rightArray[0])[0] instanceof Long)) {
                    Long[][] lA = new Long[leftArray.length][((Object[]) leftArray[0]).length];
                    Long[][] rA = new Long[rightArray.length][((Object[]) rightArray[0]).length];
                    for (int i = 0; i < lA.length; i++) {
                        Object[] line=(Object[])leftArray[i];
                        for(int j=0;j<line.length;j++){
                            lA[i][j]=(Long)line[j];
                        }

                    }
                    for (int i = 0; i < rA.length; i++) {
                        Object[] line=(Object[])rightArray[i];
                        for(int j=0;j<line.length;j++){
                            rA[i][j]=(Long)line[j];
                        }
                    }
                    if (lA[0].length != rA.length)
                    throw new InterpreterException("Trying to use @ operation on matrix with uncompatible sizes:" +
                        "[" + lA.length + ", " + lA[0].length + "] and [" + rA.length + ", " + rA[0].length + "]", new Exception());
                    Long[][] result = new Long[lA.length][rA[0].length];
                    for (int i = 0; i < lA.length; i++) {
                        for (int j = 0; j < rA.length; j++) {
                            result[i][j]=Long.valueOf(0);
                            for (int k = 0; k < lA[0].length; k++) {
                                result[i][j] += lA[i][k] * rA[k][j];
                            }
                        }
                    }
                    return result;

                }

                if (((Object[]) leftArray[0])[0] instanceof Long && (((Object[]) rightArray[0])[0] instanceof Long)) {
                    Double[][] lA = new Double[leftArray.length][];
                    Double[][] rA = new Double[rightArray.length][];
                    for (int i = 0; i < lA.length; i++) {
                        Object[] line=(Object[])leftArray[i];
                        for(int j=0;j<line.length;j++){
                            lA[i][j]=(Double)line[j];
                        }

                    }
                    for (int i = 0; i < rA.length; i++) {
                        Object[] line=(Object[])rightArray[i];
                        for(int j=0;j<line.length;j++){
                            rA[i][j]=(Double)line[j];
                        }
                    }
                    Double[][] result = new Double[lA.length][rA[0].length];

                    for (int i = 0; i < lA.length; i++) {
                        for (int j = 0; j < rA.length; j++) {
                            result[i][j]=Double.valueOf(0);
                            for(int k=0;k<lA[0].length;k++){
                                result[i][j]+=lA[i][k]*rA[k][j];
                            }
                        }
                    }
                    return result;
                }
                else
                    throw new InterpreterException("Operation @ not defined for this type",new Exception());
            default:
                return new Object[leftArray.length][rightArray.length];
        }
    }

    private Object arrayExpression( BinaryExpressionNode node){
        Type leftType  = reactor.get(node.left, "type");
        Type rightType = reactor.get(node.right, "type");

        Object left  = get(node.left);
        Object right = get(node.right);

        if(node.operator == BinaryOperator.ADD){
            Object[] leftArray=(Object[])left;
            Object[] rightArray=(Object[])right;
            return arrayOperate(leftArray,rightArray,0);
        }
        else if (node.operator==BinaryOperator.MULTIPLY){
            Object[] leftArray=(Object[])left;
            Object[] rightArray=(Object[])right;
            return arrayOperate(leftArray,rightArray,2);
        }
        else if (node.operator==BinaryOperator.SUBTRACT){
            Object[] leftArray=(Object[])left;
            Object[] rightArray=(Object[])right;
            return arrayOperate(leftArray,rightArray,1);
        }
        else if (node.operator==BinaryOperator.DIVIDE){
            Object[] leftArray=(Object[])left;
            Object[] rightArray=(Object[])right;
            return arrayOperate(leftArray,rightArray,3);
        }
        else if (node.operator==BinaryOperator.DOTPRODUCT){
            Object[] rightArray=(Object[])right;
            Object[] leftArray=(Object[])left;
            return matrixOperate(leftArray,rightArray,0);
        }
        switch (node.operator) {
            case EQUALITY:
                return  leftType.isPrimitive() ? left.equals(right) : left == right;
            case NOT_EQUALS:
                return  leftType.isPrimitive() ? !left.equals(right) : left != right;
        }

        throw new InterpreterException("Operation not implemented yet", new Exception());
    }

    private Object binaryExpression (BinaryExpressionNode node)
    {
        Type leftType  = reactor.get(node.left, "type");
        Type rightType = reactor.get(node.right, "type");

        // Cases where both operands should not be evaluated.
        switch (node.operator) {
            case OR:  return booleanOp(node, false);
            case AND: return booleanOp(node, true);
        }

        Object left  = get(node.left);
        Object right = get(node.right);

        if (node.operator == BinaryOperator.ADD
                && (leftType instanceof StringType || rightType instanceof StringType))
            return convertToString(left) + convertToString(right);
        else if (leftType instanceof ArrayType || rightType instanceof ArrayType) {
            return arrayExpression(node);
        }
        boolean floating = leftType instanceof FloatType || rightType instanceof FloatType;
        boolean numeric  = floating || leftType instanceof IntType;

        if (numeric)
            return numericOp(node, floating, (Number) left, (Number) right);

        switch (node.operator) {
            case EQUALITY:
                return  leftType.isPrimitive() ? left.equals(right) : left == right;
            case NOT_EQUALS:
                return  leftType.isPrimitive() ? !left.equals(right) : left != right;
        }

        throw new Error("should not reach here");
    }

    // ---------------------------------------------------------------------------------------------

    private boolean booleanOp (BinaryExpressionNode node, boolean isAnd)
    {
        boolean left = get(node.left);
        return isAnd
                ? left && (boolean) get(node.right)
                : left || (boolean) get(node.right);
    }

    // ---------------------------------------------------------------------------------------------

    private Object numericOp
            (BinaryExpressionNode node, boolean floating, Number left, Number right)
    {
        long ileft, iright;
        double fleft, fright;

        if (floating) {
            fleft  = left.doubleValue();
            fright = right.doubleValue();
            ileft = iright = 0;
        } else {
            ileft  = left.longValue();
            iright = right.longValue();
            fleft = fright = 0;
        }

        Object result;
        if (floating)
            switch (node.operator) {
                case MULTIPLY:      return fleft *  fright;
                case DIVIDE:        return fleft /  fright;
                case REMAINDER:     return fleft %  fright;
                case ADD:           return fleft +  fright;
                case SUBTRACT:      return fleft -  fright;
                case GREATER:       return fleft >  fright;
                case LOWER:         return fleft <  fright;
                case GREATER_EQUAL: return fleft >= fright;
                case LOWER_EQUAL:   return fleft <= fright;
                case EQUALITY:      return fleft == fright;
                case NOT_EQUALS:    return fleft != fright;
                default:
                    throw new Error("should not reach here");
            }
        else
            switch (node.operator) {
                case MULTIPLY:      return ileft *  iright;
                case DIVIDE:        return ileft /  iright;
                case REMAINDER:     return ileft %  iright;
                case ADD:           return ileft +  iright;
                case SUBTRACT:      return ileft -  iright;
                case GREATER:       return ileft >  iright;
                case LOWER:         return ileft <  iright;
                case GREATER_EQUAL: return ileft >= iright;
                case LOWER_EQUAL:   return ileft <= iright;
                case EQUALITY:      return ileft == iright;
                case NOT_EQUALS:    return ileft != iright;
                default:
                    throw new Error("should not reach here");
            }
    }

    // ---------------------------------------------------------------------------------------------

    public Object assignment (AssignmentNode node)
    {
        if (node.left instanceof ReferenceNode) {
            Scope scope = reactor.get(node.left, "scope");
            String name = ((ReferenceNode) node.left).name;
            Object rvalue = get(node.right);
            assign(scope, name, rvalue, reactor.get(node, "type"));
            return rvalue;
        }

        if (node.left instanceof ArrayAccessNode) {
            ArrayAccessNode arrayAccess = (ArrayAccessNode) node.left;
            Object[] array = getNonNullArray(arrayAccess.array);
            int index = getIndex(arrayAccess.index);
            try {
                return array[index] =(Object) get(node.right);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new PassthroughException(e);
            }
        }

        if (node.left instanceof FieldAccessNode) {
            FieldAccessNode fieldAccess = (FieldAccessNode) node.left;
            Object object = get(fieldAccess.stem);
            if (object == Null.INSTANCE)
                throw new PassthroughException(
                    new NullPointerException("accessing field of null object"));
            Map<String, Object> struct = cast(object);
            Object right = get(node.right);
            struct.put(fieldAccess.fieldName, right);
            return right;
        }

        throw new Error("should not reach here");
    }

    // ---------------------------------------------------------------------------------------------

    private int getIndex (ExpressionNode node)
    {
        long index = get(node);
        if (index < 0)
            throw new ArrayIndexOutOfBoundsException("Negative index: " + index);
        if (index >= Integer.MAX_VALUE - 1)
            throw new ArrayIndexOutOfBoundsException("Index exceeds max array index (2ˆ31 - 2): " + index);
        return (int) index;
    }

    // ---------------------------------------------------------------------------------------------

    private Object[] getNonNullArray (ExpressionNode node)
    {
        Object object = get(node);
        if (object == Null.INSTANCE)
            throw new PassthroughException(new NullPointerException("indexing null array"));
        return (Object[]) object;
    }

    // ---------------------------------------------------------------------------------------------

    private Object unaryExpression (UnaryExpressionNode node)
    {
        // there is only NOT
        assert node.operator == UnaryOperator.NOT;
        return ! (boolean) get(node.operand);
    }

    // ---------------------------------------------------------------------------------------------

    private Object arrayAccess (ArrayAccessNode node)
    {
        Object[] array = getNonNullArray(node.array);
        try {
            return array[getIndex(node.index)];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new PassthroughException(e);
        }
    }

    // ---------------------------------------------------------------------------------------------

    private Object root (RootNode node)
    {
        assert storage == null;
        rootScope = reactor.get(node, "scope");
        storage = rootStorage = new ScopeStorage(rootScope, null);
        storage.initRoot(rootScope);

        try {
            node.statements.forEach(this::run);
        } catch (Return r) {
            return r.value;
            // allow returning from the main script
        } finally {
            storage = null;
        }
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Void block (BlockNode node) {
        Scope scope = reactor.get(node, "scope");
        storage = new ScopeStorage(scope, storage);
        node.statements.forEach(this::run);
        storage = storage.parent;
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Constructor constructor (ConstructorNode node) {
        // guaranteed safe by semantic analysis
        return new Constructor(get(node.ref));
    }

    // ---------------------------------------------------------------------------------------------

    private Object expressionStmt(ExpressionStatementNode node) {
        get(node.expression);
        return null;  // discard value
    }

    // ---------------------------------------------------------------------------------------------
    private long average(Object[] tab){
        if(tab.length==0) return 0;
        long a=sum(tab);
        long n=count(tab);
        return a/n;
    }

    private long sum(Object[] tab){
        if(tab.length==0)return 0;
        long a=0;
        for(Object o:tab){
            if(o instanceof Object[]){
                a+=sum((Object[])o);
            }
            else a=a+(long)o;
        }
        return a;
    }

    private long count(Object[] tab){
        if(tab.length==0)return 0;
        long n=0;
        if(tab[0] instanceof Object[]){
            for(Object o:tab)
                n+=count((Object[])o);
        }
        else n+=tab.length;
        return n;
    }

    private long nDim(Object[] tab, long n){
        if(tab.length==0)return 1;
        if (tab[0] instanceof Object[]){
            return nDim((Object[])tab[0],n+1);
        }
        return n;
    }


    private Object fieldAccess (FieldAccessNode node)
    {

        Object stem = get(node.stem);
        if (stem == Null.INSTANCE)
            throw new PassthroughException(
                    new NullPointerException("accessing field of null object"));
        if(! (stem instanceof  Map)){
            String fieldName=node.fieldName;
            if(fieldName.equals("length"))
                return (long) ((Object[]) stem).length;
            else if(fieldName.equals("avg"))
                return (long) average((Object[]) stem);
            else if(fieldName.equals("count"))
                return (long) count((Object[]) stem);
            else if(fieldName.equals("sum"))
                return (long) sum((Object[]) stem);
            else if(fieldName.equals("nDim"))
                return (long) nDim((Object[]) stem,1);
        }
        if(stem instanceof HashMap){
            if (((HashMap)stem).get(node.fieldName) instanceof FunDeclarationNode){
                obj = stem;
            }
        }

        return Util.<Map<String, Object>>cast(stem).get(node.fieldName);
        /*return stem instanceof Map
                ? Util.<Map<String, Object>>cast(stem).get(node.fieldName)
                :(long) ((Object[]) stem).length;  // only field on arrays*/
    }


    // ---------------------------------------------------------------------------------------------

    private Object funCall (FunCallNode node)
    {
        Object decl = get(node.function);


        node.arguments.forEach(this::run);
        Object[] args = map(node.arguments, new Object[0], visitor);

        if (decl == Null.INSTANCE)
            throw new PassthroughException(new NullPointerException("calling a null function"));

        if (decl instanceof SyntheticDeclarationNode)
            return builtin(((SyntheticDeclarationNode) decl).name(), args);

        if (decl instanceof Constructor)
            return buildStruct(((Constructor) decl).declaration, args);

        ScopeStorage oldStorage = storage;
        Scope scope = reactor.get(decl, "scope");
        storage = new ScopeStorage(scope, storage);

        FunDeclarationNode funDecl = (FunDeclarationNode) decl;
        coIterate(args, funDecl.parameters,
                (arg, param) -> storage.set(scope, param.name, arg));

        try {
            get(funDecl.block);
        } catch (Return r) {
            return r.value;
        } finally {
            storage = oldStorage;
        }
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Object builtin (String name, Object[] args)
    {
        assert name.equals("print"); // only one at the moment
        String out = convertToString(args[0]);
        System.out.println(out);
        return out;
    }

    // ---------------------------------------------------------------------------------------------

    private String convertToString (Object arg)
    {
        if (arg == Null.INSTANCE)
            return "null";
        else if (arg instanceof Object[]) {

            if (((Object[]) arg)[0] instanceof HashMap) {
                Object oldobj = obj;
                Object[] result = new Object[((Object[])arg).length];
                for (int i = 0; i <((Object[])arg).length;i++){
                    FunDeclarationNode toprint = (FunDeclarationNode) ((HashMap)((Object[])arg)[i]).get("to_Number");
                    if(toprint==null){
                        throw new InterpreterException("You must implement to_Number method",new Exception());
                    }

                    ScopeStorage oldStorage = storage;
                    Scope scope = reactor.get(toprint, "scope");
                    storage = new ScopeStorage(scope, storage);


                    obj = ((Object[])arg)[i];
                    try {
                        get(toprint.block);
                    } catch (Return r) {
                        result[i]= r.value;
                    } finally {
                        storage = oldStorage;
                    }
                }
                obj=oldobj;
                return Arrays.deepToString(result);
            }

            return Arrays.deepToString((Object[]) arg);
        }
        else if (arg instanceof FunDeclarationNode)
            return ((FunDeclarationNode) arg).name;
        else if (arg instanceof StructDeclarationNode)
            return ((StructDeclarationNode) arg).name;
        else if (arg instanceof Constructor)
            return "$" + ((Constructor) arg).declaration.name;
        else

            return arg.toString();
    }

    // ---------------------------------------------------------------------------------------------

    private HashMap<String, Object> buildStruct (StructDeclarationNode node, Object[] args)
    {
        HashMap<String, Object> struct = new HashMap<>();
        for (int i = 0; i < node.fields.size(); ++i)
            struct.put(node.fields.get(i).name, args[i]);
        for (int i = 0; i < node.fun.size(); ++i)
            struct.put(node.fun.get(i).name, node.fun.get(i));
        return struct;
    }

    // ---------------------------------------------------------------------------------------------

    private Void ifStmt (IfNode node)
    {
        if (get(node.condition))
            get(node.trueStatement);
        else if (node.falseStatement != null)
            get(node.falseStatement);
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Void whileStmt (WhileNode node)
    {
        while (get(node.condition))
            get(node.body);
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private Object reference (ReferenceNode node)
    {
        Scope scope = reactor.get(node, "scope");
        DeclarationNode decl = reactor.get(node, "decl");

        if (decl instanceof VarDeclarationNode
        || decl instanceof ParameterNode
        || decl instanceof ArrayDeclarationNode
        || decl instanceof SyntheticDeclarationNode
                && ((SyntheticDeclarationNode) decl).kind() == DeclarationKind.VARIABLE)
            return scope == rootScope
                ? rootStorage.get(scope, node.name)
                : storage.get(scope, node.name);
        if (decl instanceof FieldDeclarationNode){
            return ((HashMap<?, ?>)obj).get(node.name);//((HashMap)rootStorage.get(scope, var.name)).get(node.name);
        }
        return decl; // structure or function
    }

    // ---------------------------------------------------------------------------------------------

    private Void returnStmt (ReturnNode node) {
        throw new Return(node.expression == null ? null : get(node.expression));
    }

    // ---------------------------------------------------------------------------------------------

    private Void varDecl (VarDeclarationNode node)
    {
        Scope scope = reactor.get(node, "scope");
        assign(scope, node.name, get(node.initializer), reactor.get(node, "type"));
        return null;
    }

    private Void arrayDecl (ArrayDeclarationNode node)
    {
        Scope scope = reactor.get(node, "scope");
        assign(scope, node.name, node.createArray(0), reactor.get(node, "type"));
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private void assign (Scope scope, String name, Object value, Type targetType)
    {
        if (value instanceof Long && targetType instanceof FloatType)
            value = ((Long) value).doubleValue();
        storage.set(scope, name, value);
    }

    // ---------------------------------------------------------------------------------------------
}
