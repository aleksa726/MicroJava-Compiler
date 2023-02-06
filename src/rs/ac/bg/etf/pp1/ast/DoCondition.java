// generated with ast extension for cup
// version 0.8
// 21/5/2022 22:52:9


package rs.ac.bg.etf.pp1.ast;

public class DoCondition implements SyntaxNode {

    private SyntaxNode parent;
    private int line;
    private DoCond DoCond;

    public DoCondition (DoCond DoCond) {
        this.DoCond=DoCond;
        if(DoCond!=null) DoCond.setParent(this);
    }

    public DoCond getDoCond() {
        return DoCond;
    }

    public void setDoCond(DoCond DoCond) {
        this.DoCond=DoCond;
    }

    public SyntaxNode getParent() {
        return parent;
    }

    public void setParent(SyntaxNode parent) {
        this.parent=parent;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line=line;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(DoCond!=null) DoCond.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(DoCond!=null) DoCond.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(DoCond!=null) DoCond.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("DoCondition(\n");

        if(DoCond!=null)
            buffer.append(DoCond.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [DoCondition]");
        return buffer.toString();
    }
}
