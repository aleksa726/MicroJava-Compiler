// generated with ast extension for cup
// version 0.8
// 21/5/2022 22:52:9


package rs.ac.bg.etf.pp1.ast;

public class AssignError extends AssignDesignator {

    public AssignError () {
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("AssignError(\n");

        buffer.append(tab);
        buffer.append(") [AssignError]");
        return buffer.toString();
    }
}
