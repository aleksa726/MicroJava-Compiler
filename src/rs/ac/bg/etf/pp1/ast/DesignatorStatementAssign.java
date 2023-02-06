// generated with ast extension for cup
// version 0.8
// 21/5/2022 22:52:9


package rs.ac.bg.etf.pp1.ast;

public class DesignatorStatementAssign extends DesignatorStatement {

    private AssignDesignator AssignDesignator;

    public DesignatorStatementAssign (AssignDesignator AssignDesignator) {
        this.AssignDesignator=AssignDesignator;
        if(AssignDesignator!=null) AssignDesignator.setParent(this);
    }

    public AssignDesignator getAssignDesignator() {
        return AssignDesignator;
    }

    public void setAssignDesignator(AssignDesignator AssignDesignator) {
        this.AssignDesignator=AssignDesignator;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(AssignDesignator!=null) AssignDesignator.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(AssignDesignator!=null) AssignDesignator.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(AssignDesignator!=null) AssignDesignator.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("DesignatorStatementAssign(\n");

        if(AssignDesignator!=null)
            buffer.append(AssignDesignator.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [DesignatorStatementAssign]");
        return buffer.toString();
    }
}
