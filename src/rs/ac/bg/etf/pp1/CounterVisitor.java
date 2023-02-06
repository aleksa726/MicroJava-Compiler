package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.concepts.Struct;

public class CounterVisitor extends VisitorAdaptor{

	protected int count;
	
	public int getCount() {
		return count;
	}
	
	public static class FormParamCounter extends CounterVisitor{
		
		Logger log = Logger.getLogger(getClass());
		
		public boolean hasVarArgs = false;
		public Struct argsStruct ;
		public int actualParamCount = 0;
		public int formalParams = 0;
		
		public void visit(SingleFormalParamDecl formalParam) {
			count++;
			formalParams++;
		}
		
		public void visit(FormalParamListDecl formalParam) {
			count++;
			formalParams++;
		}
		
		public void visit(VarArgsDecl varArgsDecl) {
			count+=1;
			this.hasVarArgs = true;
			this.argsStruct = varArgsDecl.getType().struct;
		}
		
		
		
		
		public void visit(ActualParams actualParams) {
			this.actualParamCount++;
		}
		
		public void visit(ActualParam actualParam) {
			this.actualParamCount++;
		}
		
		
	}
	
public static class VarCounter extends CounterVisitor{
		
		public void visit(SingleVarDeclaration varDeclaration) {
			count++;
		}
		
		public void visit(VarDeclarationList varDeclarationList) {
			count++;
		}
		
	}
	
	
}
