// generated with ast extension for cup
// version 0.8
// 21/5/2022 22:52:9


package rs.ac.bg.etf.pp1.ast;

public class FunctionCall extends BaseExp {

    private MethodCallDesignator MethodCallDesignator;
    private ActParsList ActParsList;

    public FunctionCall (MethodCallDesignator MethodCallDesignator, ActParsList ActParsList) {
        this.MethodCallDesignator=MethodCallDesignator;
        if(MethodCallDesignator!=null) MethodCallDesignator.setParent(this);
        this.ActParsList=ActParsList;
        if(ActParsList!=null) ActParsList.setParent(this);
    }

    public MethodCallDesignator getMethodCallDesignator() {
        return MethodCallDesignator;
    }

    public void setMethodCallDesignator(MethodCallDesignator MethodCallDesignator) {
        this.MethodCallDesignator=MethodCallDesignator;
    }

    public ActParsList getActParsList() {
        return ActParsList;
    }

    public void setActParsList(ActParsList ActParsList) {
        this.ActParsList=ActParsList;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(MethodCallDesignator!=null) MethodCallDesignator.accept(visitor);
        if(ActParsList!=null) ActParsList.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(MethodCallDesignator!=null) MethodCallDesignator.traverseTopDown(visitor);
        if(ActParsList!=null) ActParsList.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(MethodCallDesignator!=null) MethodCallDesignator.traverseBottomUp(visitor);
        if(ActParsList!=null) ActParsList.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("FunctionCall(\n");

        if(MethodCallDesignator!=null)
            buffer.append(MethodCallDesignator.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(ActParsList!=null)
            buffer.append(ActParsList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [FunctionCall]");
        return buffer.toString();
    }
}
