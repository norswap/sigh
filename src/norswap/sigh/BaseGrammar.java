package norswap.sigh;

import norswap.sigh.ast.RootNode;
import norswap.sigh.ast.StatementNode;
import norswap.sigh.ast.base.TemplateDeclarationNode;

public class BaseGrammar extends SighGrammar {

    // RESERVED
    public rule _template = reserved("template");

    // MISC
    public rule identifiers = identifier.sep(1, COMMA).as_list(String.class);

    // DECLARATIONS
    public rule template_decl =
        seq(_template, LANGLE, identifiers, RANGLE)
            .push($ -> new TemplateDeclarationNode($.span(), $.$[0]));

    // STATEMENTS
    public rule statement = lazy(() -> choice(
        this.block,
        this.var_decl,
        this.template_decl, // ADDED template decl
        this.fun_decl,
        this.struct_decl,
        this.if_stmt,
        this.while_stmt,
        this.return_stmt,
        this.expression_stmt
    ));

    // Redefining the root grammar
    public rule root =
        seq(ws, statement.at_least(1))
            .as_list(StatementNode.class)
            .push($ -> new RootNode($.span(), $.$[0]));

}
