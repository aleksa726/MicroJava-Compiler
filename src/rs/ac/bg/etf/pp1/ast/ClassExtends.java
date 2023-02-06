// generated with ast extension for cup
// version 0.8
// 21/5/2022 22:52:9


package rs.ac.bg.etf.pp1.ast;

public class ClassExtends extends ClassExtend {

    private ClassExtensionType ClassExtensionType;

    public ClassExtends (ClassExtensionType ClassExtensionType) {
        this.ClassExtensionType=ClassExtensionType;
        if(ClassExtensionType!=null) ClassExtensionType.setParent(this);
    }

    public ClassExtensionType getClassExtensionType() {
        return ClassExtensionType;
    }

    public void setClassExtensionType(ClassExtensionType ClassExtensionType) {
        this.ClassExtensionType=ClassExtensionType;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(ClassExtensionType!=null) ClassExtensionType.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(ClassExtensionType!=null) ClassExtensionType.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(ClassExtensionType!=null) ClassExtensionType.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ClassExtends(\n");

        if(ClassExtensionType!=null)
            buffer.append(ClassExtensionType.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ClassExtends]");
        return buffer.toString();
    }
}
