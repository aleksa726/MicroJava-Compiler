// generated with ast extension for cup
// version 0.8
// 21/5/2022 22:52:9


package rs.ac.bg.etf.pp1.ast;

public class MethodDeclaration extends MethodDecl {

    private MethodTypeName MethodTypeName;
    private FormParsAndVarArgs FormParsAndVarArgs;
    private VarListOpt VarListOpt;
    private StatementList StatementList;

    public MethodDeclaration (MethodTypeName MethodTypeName, FormParsAndVarArgs FormParsAndVarArgs, VarListOpt VarListOpt, StatementList StatementList) {
        this.MethodTypeName=MethodTypeName;
        if(MethodTypeName!=null) MethodTypeName.setParent(this);
        this.FormParsAndVarArgs=FormParsAndVarArgs;
        if(FormParsAndVarArgs!=null) FormParsAndVarArgs.setParent(this);
        this.VarListOpt=VarListOpt;
        if(VarListOpt!=null) VarListOpt.setParent(this);
        this.StatementList=StatementList;
        if(StatementList!=null) StatementList.setParent(this);
    }

    public MethodTypeName getMethodTypeName() {
        return MethodTypeName;
    }

    public void setMethodTypeName(MethodTypeName MethodTypeName) {
        this.MethodTypeName=MethodTypeName;
    }

    public FormParsAndVarArgs getFormParsAndVarArgs() {
        return FormParsAndVarArgs;
    }

    public void setFormParsAndVarArgs(FormParsAndVarArgs FormParsAndVarArgs) {
        this.FormParsAndVarArgs=FormParsAndVarArgs;
    }

    public VarListOpt getVarListOpt() {
        return VarListOpt;
    }

    public void setVarListOpt(VarListOpt VarListOpt) {
        this.VarListOpt=VarListOpt;
    }

    public StatementList getStatementList() {
        return StatementList;
    }

    public void setStatementList(StatementList StatementList) {
        this.StatementList=StatementList;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(MethodTypeName!=null) MethodTypeName.accept(visitor);
        if(FormParsAndVarArgs!=null) FormParsAndVarArgs.accept(visitor);
        if(VarListOpt!=null) VarListOpt.accept(visitor);
        if(StatementList!=null) StatementList.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(MethodTypeName!=null) MethodTypeName.traverseTopDown(visitor);
        if(FormParsAndVarArgs!=null) FormParsAndVarArgs.traverseTopDown(visitor);
        if(VarListOpt!=null) VarListOpt.traverseTopDown(visitor);
        if(StatementList!=null) StatementList.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(MethodTypeName!=null) MethodTypeName.traverseBottomUp(visitor);
        if(FormParsAndVarArgs!=null) FormParsAndVarArgs.traverseBottomUp(visitor);
        if(VarListOpt!=null) VarListOpt.traverseBottomUp(visitor);
        if(StatementList!=null) StatementList.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MethodDeclaration(\n");

        if(MethodTypeName!=null)
            buffer.append(MethodTypeName.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(FormParsAndVarArgs!=null)
            buffer.append(FormParsAndVarArgs.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(VarListOpt!=null)
            buffer.append(VarListOpt.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(StatementList!=null)
            buffer.append(StatementList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MethodDeclaration]");
        return buffer.toString();
    }
}
