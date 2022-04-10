package norswap.sigh.ast;

import com.sun.org.apache.bcel.internal.generic.SIPUSH;
import norswap.autumn.positions.Span;
import norswap.sigh.ast.base.TemplateTypeNode;
import norswap.utils.Util;
import org.graalvm.compiler.lir.LIRInstruction.Temp;
import java.util.ArrayList;
import java.util.List;

public class FunDeclarationNode extends DeclarationNode
{
    public final String name;
    public final List<ParameterNode> parameters;
    public final TypeNode returnType;
    public final BlockNode block;
    public final List<String> templateParameters;

    @SuppressWarnings("unchecked")
    public FunDeclarationNode
            (Span span, Object name, Object parameters, Object returnType, Object block) {
        super(span);

        this.name = Util.cast(name, String.class);
        this.parameters = Util.cast(parameters, List.class);
        this.returnType = returnType == null
            ? new SimpleTypeNode(new Span(span.start, span.start), "Void")
            : Util.cast(returnType, TypeNode.class);
        this.block = Util.cast(block, BlockNode.class);
        this.templateParameters = new ArrayList<>();

        return;
    }

    @SuppressWarnings("unchecked")
    public FunDeclarationNode
        (Span span, Object name, Object parameters, Object returnType, Object block, Object templateParameters) {
        super(span);

        // Casting to template parameters list
        List<String> templateParametersList = Util.cast(templateParameters, List.class);

        this.name = Util.cast(name, String.class);

        // Checking parameters if they have to be recast to template parameter type
        List<ParameterNode> parametersList = Util.cast(parameters, List.class);

        int parameterIndex = 0;
        for (ParameterNode parameterNode : parametersList) {

            // Checking if parameter type is a template parameter
            // TODO handle array return types?
            if (parameterNode.type instanceof SimpleTypeNode) {
                SimpleTypeNode typeNode = Util.cast(parameterNode.type, SimpleTypeNode.class);

                // Preparing new template parameter
                ParameterNode newTemplateParameter = new ParameterNode(parameterNode.span, parameterNode.name, new TemplateTypeNode(typeNode));

                // Updating parameter as template parameter
                parametersList.set(parameterIndex, newTemplateParameter);
            }

            parameterIndex++;
        }
        this.parameters = Util.cast(parameters, List.class);

        // Checking if return type is a template parameter
        // TODO handle array return types?
        if (returnType == null) {
            this.returnType = new SimpleTypeNode(new Span(span.start, span.start), "Void");
        } else if (returnType instanceof TemplateTypeNode) { // Corner case when providing a template type nod
            this.returnType = Util.cast(returnType, TemplateTypeNode.class);
        } else {
            // Casting to simple type node
            SimpleTypeNode returnTypeNode = Util.cast(returnType, SimpleTypeNode.class);

            // Checking if return type is actually a template type
            if (templateParameters != null && templateParametersList.contains(returnTypeNode.name)) {
                this.returnType = new TemplateTypeNode(null, returnTypeNode.name);
            } else {
                this.returnType = Util.cast(returnType, TypeNode.class);
            }
        }

        this.block = Util.cast(block, BlockNode.class);
        this.templateParameters = Util.cast(templateParameters, List.class);

        return;
    }

    @Override public String name () {
        return name;
    }

    @Override public String contents () {
        return "fun " + name;
    }

    @Override public String declaredThing () {
        return "function";
    }
}
