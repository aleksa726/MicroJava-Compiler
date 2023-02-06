// generated with ast extension for cup
// version 0.8
// 21/5/2022 22:52:9


package rs.ac.bg.etf.pp1.ast;

public class DoWhileStatement extends Statement {

    private DoStatement DoStatement;

    public DoWhileStatement (DoStatement DoStatement) {
        this.DoStatement=DoStatement;
        if(DoStatement!=null) DoStatement.setParent(this);
    }

    public DoStatement getDoStatement() {
        return DoStatement;
    }

    public void setDoStatement(DoStatement DoStatement) {
        this.DoStatement=DoStatement;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(DoStatement!=null) DoStatement.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(DoStatement!=null) DoStatement.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(DoStatement!=null) DoStatement.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("DoWhileStatement(\n");

        if(DoStatement!=null)
            buffer.append(DoStatement.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [DoWhileStatement]");
        return buffer.toString();
    }
}
