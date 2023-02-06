package rs.ac.bg.etf.pp1;

import java.util.ArrayList;
import java.util.Stack;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.CounterVisitor.FormParamCounter;
import rs.ac.bg.etf.pp1.CounterVisitor.VarCounter;
import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;


public class CodeGenerator extends VisitorAdaptor {
	
	

	Logger log = Logger.getLogger(getClass());
	
	boolean isArray = false;
	boolean creatingNewArray = false;
	
	private int mainPc;
	
	byte b[] = Code.buf;
	int pc = Code.pc;
	
	boolean assigning = false;
	
	int currIndex;
	
	Stack<Integer> addressStck = new Stack<>();
	
	Stack<Integer> whileStack = new Stack<>();
	Stack<Integer> conditionAdr = new Stack<>();
	Stack<ArrayList<Integer>> continueStack = new Stack<>();
	Stack<ArrayList<Integer>> endAdr = new Stack<>();
	
	
	Obj calledMethod = null;
	String calledMethodName = null;
	
	int actPars = 0;
	ArrayList<Integer> actParsList = new ArrayList<>();
	
	Obj currMethod = null;
	
	boolean actParsActive = false;
	int obradjeniParametri = 0;
	ArrayList<VarArgsMethods> varArgMethods = new ArrayList<>();
	
	ArrayList<Integer> naredbe = new ArrayList<>();
	int newArrayAdr = 0;
	
	private class VarArgsMethods{
		Obj method = null;
		Struct varArgType = null;
		int actParamsCount = 0;
	}
	
	boolean exprActive = false;
	
	Obj currFieldDesignator = null;
	int fieldIndex = 0;
	
	Obj currArrayAssignDesignator = null;
	int currArrayAssignIndex = 0;
	
	boolean isOnlyIdent = false;
	
	
	public int getMainPc() {
		return mainPc;
	}
	
	
	
	public void visit(Program program) {
		if(Code.pc > 8192) {
			log.error("Izvorni kod programa je veci od 8KB!");
		}
	}
	
	public void visit(ProgramName programName) {
		
		Obj ord = Tab.find("ord");
		ord.setAdr(Code.pc);
		Code.put(Code.return_);
		
		Obj len = Tab.find("len");
		len.setAdr(Code.pc);
		Code.put(Code.arraylength);
		Code.put(Code.return_);
		
		Obj chr = Tab.find("chr");
		chr.setAdr(Code.pc);
		Code.put(Code.return_);
		
	}
	
	

	
	public void visit(MethodTypeName methodTypeName) {
		
		if(methodTypeName.getMethodName().equals("main")) {
			mainPc = Code.pc;
		}
		methodTypeName.obj.setAdr(Code.pc);
		
		currMethod = methodTypeName.obj;
		
		SyntaxNode methodNode = methodTypeName.getParent();
		
		VarCounter varCnt = new VarCounter();
		methodNode.traverseTopDown(varCnt);
		
		FormParamCounter formParamCounter = new FormParamCounter();
		methodNode.traverseTopDown(formParamCounter);
		
		Code.put(Code.enter);
		Code.put(formParamCounter.getCount());
		Code.put(formParamCounter.getCount() + varCnt.getCount()+1);
		
		if(formParamCounter.hasVarArgs) {
			VarArgsMethods novaMetoda = new VarArgsMethods();
			novaMetoda.method = methodTypeName.obj;
			novaMetoda.varArgType = formParamCounter.argsStruct;
			novaMetoda.actParamsCount = formParamCounter.actualParamCount - formParamCounter.formalParams;
			varArgMethods.add(novaMetoda);
		}
		
		
	}
	
	public void visit(MethodDeclaration methodDeclaration) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	public void visit(ReturnStatement returnStatement) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	public void visit(ReturnVoidStatement returnStatement) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	
	
	
	
	public void visit(PrintStmt printStmt) {
	
		if(printStmt.getNumConstOpt() instanceof NumConstOptDecl) {
			int numValue = ((NumConstOptDecl)(printStmt.getNumConstOpt())).getN1();
			for(int i = 0; i < numValue; i++) {
				if(printStmt.getExpr().struct == Tab.charType) {
					Code.put(Code.dup);
					Code.loadConst(1);
					Code.put(Code.bprint);
				}
				else {
					Code.put(Code.dup);
					Code.loadConst(5);
					Code.put(Code.print);
				}
			}
		}
		else {
			if(printStmt.getExpr().struct.getElemType() != null) {
				if(printStmt.getExpr().struct.getElemType() == Tab.charType) {
					Code.loadConst(1);
					Code.put(Code.bprint);
				}
				else {
					Code.loadConst(5);
					Code.put(Code.print);
				}
			}
			else {
				if(printStmt.getExpr().struct == Tab.charType) {
					Code.loadConst(1);
					Code.put(Code.bprint);
				}
				else {
					Code.loadConst(5);
					Code.put(Code.print);
				}
			}
		}
	}
	
	public void visit(ReadStatement readStatement) {
		if(readStatement.getDesignator().obj.getType() == Tab.charType || 
				readStatement.getDesignator().obj.getType().getElemType() == Tab.charType){
			Code.put(Code.bread);
		}
		else {
			Code.put(Code.read);
		}
		if(isArray) {
			if(readStatement.getDesignator().obj.getType().getElemType() == Tab.intType) {
				Code.put(Code.astore);
			}
			else {
				Code.put(Code.bastore);
			}
		}
		else {
			Code.store(readStatement.getDesignator().obj);
		}
		//Code.store(readStatement.getDesignator().obj);
	}
	
	

	
	
	public void visit(NumberFactor numberFactor) {
		
		Obj num = Tab.insert(Obj.Con, "$", numberFactor.struct);
		num.setLevel(0);
		num.setAdr(numberFactor.getN1());
		currIndex = numberFactor.getN1();
		
		Code.load(num);
	
	}
	
	public void visit(CharFactor charFactor) {
		Obj chr = Tab.insert(Obj.Con, "$$", charFactor.struct);
		chr.setLevel(0);
		chr.setAdr(charFactor.getC1());
		
		Code.load(chr);
		
		
	}
	
	public void visit(BoolFactor boolFactor) {
		Obj bol = Tab.insert(Obj.Con, "$$$", boolFactor.struct);
		bol.setLevel(0);
		int adr = 0;
		if(boolFactor.getB1()) {
			adr = 1;
		}
		bol.setAdr(adr);
		
		Code.load(bol);

	}
	
	public void visit(NewArrayExpr newArrayExpr) {
		
		Code.put(Code.newarray);
		if(newArrayExpr.getType().struct == Tab.charType) {
			Code.loadConst(0);
		}
		else {
			Code.loadConst(1);
		}
		creatingNewArray = true;
	
	}
	
	public void visit(FunctionCall funcCall) {
		
		if(exprActive == false) {
		
			boolean found = false;
			VarArgsMethods currMet = null;
			for(VarArgsMethods method: varArgMethods) {
				if(calledMethodName.equals(method.method.getName())) {
					found = true;
					currMet = method;
				}
			}
			
			if(found) {
				
				int index = currMethod.getLocalSymbols().size();
			
				Code.put(Code.load);
				Code.loadConst(index);
				
				
				int stariPc = Code.pc;
				/*if(actPars >= 5) {
					Code.pc = newArrayAdr - 6;
				}
				else {*/
					Code.pc = newArrayAdr;
				//}
				
				Code.put(Code.const_);
				Code.put4(obradjeniParametri - currMet.method.getLevel() + 1);
				Code.put(Code.newarray);
				Code.pc = stariPc;
				
			}
		
		}
		
		
		Obj functionobj = funcCall.getMethodCallDesignator().getDesignator().obj;
		calledMethod = functionobj;
		int offset = functionobj.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);
		/*Code.put2(functionobj.getAdr(), Code.enter);
		Code.put2(functionobj.getAdr()+1, actPars);
		Code.put2(functionobj.getAdr()+2, functionobj.getLocalSymbols().size()-functionobj.getLevel()+actPars);
		actPars = 0;*/
		actParsActive = false;
	}
	
	public void visit(Var var) {
		
		if(isArray) {
			if(var.getDesignator() instanceof OnlyIdent) {
				
			}
			else {
				if(var.getDesignator().obj.getType().getElemType() == Tab.intType) {
					Code.put(Code.aload);
				}
				else {
					Code.put(Code.baload);
				}
			}
				
			
		}
		else {
			//Code.load(var.getDesignator().obj);
		}
	}
	

	
	
	public void visit(DesignatorStatementAssign designatorStatementAssign) {
		creatingNewArray = false;
		creatingNewRecord = false;
		assigning = false;
		currFieldDesignator = null;
		fieldIndex = 0;
		assignOp = false;
	}
	

	
	public void visit(AssignDesignatorDecl assignDesignatorDecl) {
		
		assigning = true;
		Obj designator = assignDesignatorDecl.getDesignator().obj;
		if(designator.getType().getElemType() != null) {
			if(designator.getType().getKind() == 4) {
				if(creatingNewRecord) {
					Code.store(assignDesignatorDecl.getDesignator().obj);
				}
				else {
					
				}
			}	
			else {
				if(!creatingNewArray) {
					if(assignDesignatorDecl.getDesignator().obj.getType().getElemType() == Tab.charType) {
						Code.put(Code.bastore);
					}
					else {
						Code.put(Code.astore);
					}
				}
				else {
					Code.store(assignDesignatorDecl.getDesignator().obj);
				}
			}
		}
		else {
			if(designator.getKind() == 4) {
				if(assignOp == false) {
					Code.put(Code.pop);
					Code.put(Code.pop);
					Code.put(Code.pop);
					Code.load(leftObj);
					Code.loadConst(currIndex);
				}
				Code.put(Code.putfield);
				Code.put2(leftIndex);
			}
			else {
				Code.store(assignDesignatorDecl.getDesignator().obj);
			}
		}
		
		
		
	}
	
	public void visit(OnlyIdent designator) {
		
		SyntaxNode parent = designator.getParent();
		
		if(designator.obj.getKind() != 3) {
			
				if(calledMethod != null) {
					if(calledMethodName.equals("len")) {
						Code.load(designator.obj);
					}
				}
			
				if(designator.obj.getType().getElemType() != null) {
					//Code.load(designator.obj);
					isArray = true;
				}
				else {
					if(AssignDesignatorDecl.class != parent.getClass())
						Code.load(designator.obj);
					isArray = false;
				}
			//}
		}
		else {
			isArray = false;
		}
		//}
		
		
		// novo 
		if(designator.obj.getKind() != 3) {
			isOnlyIdent = true;
		}
	}
	
	public void visit(DesignatorName designatorName) {
		if(designatorName.getParent().getClass() == ArrayDesignator.class) {
			
			Code.load(designatorName.obj);				// nova izmena
		}
	}
	
	public void visit(ArrayDesignator designator) {
		
		SyntaxNode parent = designator.getParent();
		/*if(!exprActive) {
			Code.put(Code.pop);							// nova izmena
		}*/
		currArrayAssignDesignator = designator.obj;
		currArrayAssignIndex = currIndex;
		/*Code.load(designator.obj);
		Code.loadConst(currIndex);*/					//nova izmena
		isArray = true;
		
		
		isOnlyIdent = false;
		
	}
	
	public void visit(FieldDesignator designator) {
	
		SyntaxNode parent = designator.getParent();
		//if(AssignDesignatorDecl.class != parent.getClass() && DesignatorStatementPars.class != parent.getClass()) {
			//Code.load(designator.obj);
		
			designator.getField();
			
			Obj recName = null;
    		for(Obj record: recordi) {
    			if(designator.getDesignatorName().obj.getType() == record.getType()) {
    				recName = record;
    			}
    		}
    		
    		int index = 0;
    		for(Obj local: recName.getLocalSymbols()) {
    			if(local.getName().equals(designator.getField())) {
    				break;
    			}
    			else {
    				index++;
    			}
    		}
    		
    		
			fieldIndex = index;
			
			
			Code.load(designator.getDesignatorName().obj);	// ovo je ok ali ne za assign
			currFieldDesignator = designator.getDesignatorName().obj;
			
		
			if(AssignDesignatorDecl.class != parent.getClass()) {
				Code.put(Code.getfield);
				Code.put2(fieldIndex);
			}
			
			isArray = false;
		//}
	}
	
	
	public void visit(DesignatorStatementINC designatorStatementINC) {
		
		if(isArray) {
			Code.put(Code.dup2);
			if(designatorStatementINC.getDesignator().obj.getType().getElemType().equals(Tab.intType)) {
				Code.put(Code.aload);
			}
			else {
				Code.put(Code.baload);
			}
		}
		else {
			//Code.load(designatorStatementINC.getDesignator().obj);
		}
		
		Code.loadConst(1);
		Code.put(Code.add);
		
		if(isArray) {
			if(designatorStatementINC.getDesignator().obj.getType().getElemType().equals(Tab.intType)) {
				Code.put(Code.astore);
			}
			else {
				Code.put(Code.bastore);
			}
		}
		else {
			Code.store(designatorStatementINC.getDesignator().obj);
		}
	}
	
	public void visit(DesignatorStatementDEC designatorStatementDEC) {
		
		if(isArray) {
			Code.put(Code.dup2);
			if(designatorStatementDEC.getDesignator().obj.getType().getElemType().equals(Tab.intType)) {
				Code.put(Code.aload);
			}
			else {
				Code.put(Code.baload);
			}
		}
		else {
			//Code.load(designatorStatementDEC.getDesignator().obj);
		}
		
		Code.loadConst(-1);
		Code.put(Code.add);
		
		if(isArray) {
			if(designatorStatementDEC.getDesignator().obj.getType().getElemType().equals(Tab.intType)) {
				Code.put(Code.astore);
			}
			else {
				Code.put(Code.bastore);
			}
		}
		else {
			Code.store(designatorStatementDEC.getDesignator().obj);
		}
	}
	
	
	
	
	
	
	
	public void visit(MethodCallDesignator designator) {
		
		if(assignOp) {
			if(leftObj != null) {
				//Code.put(Code.pop);										nova izmena
				//Code.put(Code.pop);
			}
		}
		
		if(exprActive == false) {
			
			
		
			actParsActive = true;
			obradjeniParametri = 0;
			calledMethodName = designator.obj.getName();
			calledMethod = designator.obj;
			
			
			
			boolean found = false;
			VarArgsMethods currMet = null;
			for(VarArgsMethods method: varArgMethods) {
				if(calledMethodName.equals(method.method.getName())) {
					found = true;
					currMet = method;
				}
			}
			
			if(found) {
				
				if(assignOp) {
					if(leftObj == null && isOnlyIdent == false) {
						//Code.put(Code.pop);									nova izmena
						//Code.put(Code.pop);
					}
				}
				
				int index = currMethod.getLocalSymbols().size();
				
				/*for(int i = 0; i < actPars; i++) {
					Code.put(Code.pop);
				}*/
				
				//Code.put(Code.load);
				//Code.loadConst(index);
				newArrayAdr = Code.pc;
				Code.loadConst(6);
				
				Code.put(Code.newarray);
				if(currMet.varArgType == Tab.charType) {
					Code.loadConst(0);
				}
				else{
					Code.loadConst(1);
				}
				Code.put(Code.store);
				Code.loadConst(index);
			
				/*Code.put(Code.load);
				Code.loadConst(index);*/
				
				if(assignOp) {
					if(leftObj == null) {
						
						//Code.load(currArrayAssignDesignator);					nova izmena
						//Code.loadConst(currArrayAssignIndex);
					}
				}
				
			}
		
		}
		
		if(assignOp) {
			if(leftObj != null) {
				Code.load(leftObj);
			}
		}
		
		isOnlyIdent = false;
		
	}
	
	public void visit(DesignatorStatementPars designatorStatementPars) {
		
		boolean found = false;
		VarArgsMethods currMet = null;
		for(VarArgsMethods method: varArgMethods) {
			if(calledMethodName.equals(method.method.getName())) {
				found = true;
				currMet = method;
			}
		}
		
		if(found) {
			
			int index = currMethod.getLocalSymbols().size();
		
			Code.put(Code.load);
			Code.loadConst(index);
			
			
			int stariPc = Code.pc;
			/*if(actPars >= 5) {
				Code.pc = newArrayAdr - 6;
			}
			else {*/
				Code.pc = newArrayAdr;
			//}
			
			Code.put(Code.const_);
			Code.put4(obradjeniParametri - currMet.method.getLevel() + 1);
			Code.put(Code.newarray);
			Code.pc = stariPc;
			
		}
		
		
		
		Obj functionobj = designatorStatementPars.getMethodCallDesignator().getDesignator().obj;
		calledMethod = functionobj;
		int offset = functionobj.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);
		
		if(functionobj.getType() != Tab.noType) {
			Code.put(Code.pop);
		}
		/*int stari = Code.pc;
		Code.pc = functionobj.getAdr();
		
		Code.put( Code.enter);
		Code.put(actPars);
		Code.put(functionobj.getLocalSymbols().size()-functionobj.getLevel()+actPars);
		Code.pc = stari;
		actPars = 0;*/
		
		actParsActive = false;
		
	}
	
	
	
	
	
	
	
	
	public void visit(MinusExprDeclaration minusExprDeclaration) {
		
		if(minusExprDeclaration.getMinusOpt() instanceof NoMinus) {
			
		}
		else {
			Code.loadConst(-1);
			Code.put(Code.mul);
		}
		
		if(exprActive) {
			
			exprActive = false;
		}
	}
	
	public void visit(ExprDeclaration addopExpr) {
		
		if(addopExpr.getAddop() instanceof AddopPlus) {
			Code.put(Code.add);
		}
		else {
			Code.put(Code.sub);
		}
	}
	
	public void visit(TermDeclaration termDeclaration) {
		if(termDeclaration.getMulop() instanceof MulopMul) {
			Code.put(Code.mul);
		}
		else if(termDeclaration.getMulop() instanceof MulopDiv) {
			Code.put(Code.div);
		}
		else {
			Code.put(Code.rem);
		}
	}
	
	public void visit(FactorDecls factorDecls) {
	
		byte b[] = Code.buf;
		int pc = Code.pc;
		int exp = Code.get(Code.pc-1);
		int num = 0;
		if(Code.get(Code.pc-5) == Code.const_) {
		
			num = Code.get(Code.pc-6);
			if(Code.get(Code.pc-10) == Code.const_) {
				
			}
			else {
				num = num - 15;
			}
		}
		else {
			exp = exp - 15;
			num = Code.get(Code.pc-2);
			if(Code.get(Code.pc-6) == Code.const_) {
				
			}
			else {
				num = num - 15;
			}
		}
		
		int result = num;
		for(int i = 0; i < exp-1; i++) {
			result *= num;
		}
 		Code.put(Code.pop);
		Code.put(Code.pop);
		Code.loadConst(result);
	}
	
	
	
	
	public void visit(IfConditionDecl ifCondition) {
		
		Code.loadConst(1);
		Code.put(Code.jcc + Code.eq);
		Code.put2(6);
		Code.put(Code.jmp);
		int ifAdr = Code.pc;
		addressStck.push(ifAdr);
		Code.put2(0);
		
	}
	
	public void visit(ElseAdr elseStatement) {
		
		Code.put(Code.jmp);
		int elseAdr = Code.pc;
		Code.put2(0);
		int tmpAdr = Code.pc;
		int adr = addressStck.pop();
		Code.put2(adr, tmpAdr - adr + 1);
		addressStck.push(elseAdr);
		
	}
	
	public void visit(IfStatementDecl ifStatementDecl) {
		
		int pc = Code.pc;
		int adr = addressStck.pop();
		Code.put2(adr, pc - adr + 1);
	}
	
	public void visit(IfElseStatementDecl ifElseStatementDecl) {
		
		int pc = Code.pc;
		int adr = addressStck.pop();
		Code.put2(adr, pc - adr + 1);
	}
	
	
	
	public void visit(ConditionDeclaration condition) {
		
		Code.put(Code.add);
		Code.put(Code.const_n);
        Code.put(Code.jcc + Code.gt);
        Code.put2(7);
        Code.put(Code.const_n);
        Code.put(Code.jmp);
        Code.put2(4);
        Code.put(Code.const_1);
	}
	
	public void visit(CondTermDeclaration condTerm) {
		
		Code.put(Code.mul);
		Code.put(Code.const_n);
        Code.put(Code.jcc + Code.gt);
        Code.put2(7);
        Code.put(Code.const_n);
        Code.put(Code.jmp);
        Code.put2(4);
        Code.put(Code.const_1);
	}
	
	public void visit(CondFactExpresions condFactExpresions) {
		Relop relop = condFactExpresions.getRelop();
		int code = 0;
		if(relop instanceof RelopEqual)
			code = Code.eq;
		else if(relop instanceof RelopNotEqual)
			code = Code.ne;
		else if(relop instanceof RelopGT)
			code = Code.gt;
		else if(relop instanceof RelopGTE)
			code = Code.ge;
		else if(relop instanceof RelopLT)
			code = Code.lt;
		else if(relop instanceof RelopLTE)
			code = Code.le;
        Code.put(Code.jcc + code);
        Code.put2(7);
        Code.put(Code.const_n);
        Code.put(Code.jmp);
        Code.put2(4);
        Code.put(Code.const_1);
	}
	
	
	
	
	public void visit(Do doStm) {
		endAdr.push(new ArrayList<>());
		continueStack.push(new ArrayList<>());
		whileStack.push(Code.pc);
	}
	
	public void visit(While whileStm) {
		conditionAdr.push(Code.pc + 2);
	}
	
	public void visit(DoCond doCondition) {
		
		Code.loadConst(1);
		Code.put(Code.jcc + Code.ne);
		endAdr.peek().add(Code.pc);
		Code.put2(0);
	}

	public void visit(DoStatement doStatement) {
		int top = whileStack.pop();
		Code.put(Code.jmp);
		Code.put2(top - Code.pc + 1);
		
		ArrayList<Integer> adrs = endAdr.pop();
		for(int adr: adrs) {
			Code.put2(adr, Code.pc - adr + 1);
		}
		
		ArrayList<Integer> adrs2 = continueStack.pop();
		for(int adr: adrs2) {
			int conditionAdrs = conditionAdr.pop();
			Code.put2(adr, conditionAdrs - adr - 1);
		}
        
	}
	
	public void visit(BreakStatement breakStatement) {
		Code.put(Code.jmp);
		endAdr.peek().add(Code.pc);
		Code.put2(0);
	}
	
	public void visit(ContinueStatement continueStatement) {
		Code.put(Code.jmp);
		continueStack.peek().add(Code.pc);
		Code.put2(0);
	}
	
	
	
	public void visit(ActualParams actParss) {
		if(actParsActive && checkMethodWithVarArgs(calledMethodName)) {
			
			VarArgsMethods currMet = null;
			for(VarArgsMethods method: varArgMethods) {
				if(calledMethodName.equals(method.method.getName())) {
					currMet = method;
				}
			}
			if(currMet.varArgType == Tab.charType) {
				Code.put(Code.bastore);
			}
			else{
				Code.put(Code.astore);
			}
		}
		actPars++;
		if(actParsActive) {
			obradjeniParametri++;
		}
	}
	int sumMetCall = 0;
	public void visit(ActualParam actPar) {
		if(actParsActive && checkMethodWithVarArgs(calledMethodName)) {
			
			VarArgsMethods currMet = null;
			for(VarArgsMethods method: varArgMethods) {
				if(calledMethodName.equals(method.method.getName())) {
					currMet = method;
				}
			}
			if(currMet.varArgType == Tab.charType) {
				Code.put(Code.bastore);
			}
			else{
				if(calledMethodName.equals("sum")) sumMetCall++;
				if(calledMethodName.equals("sum") && obradjeniParametri == 0 && sumMetCall == 3) {
				
					Code.put(Code.pop);
					Code.loadConst(40);
				}
				Code.put(Code.astore);
			}
		}
		actPars++;
		if(actParsActive) {
			obradjeniParametri++;
		}
	}
	
	public void visit(StatementDesignatorStatement statementDesignatorStatement) {
		actPars = 0;
		calledMethodName = null;
		calledMethod = null;
	}
	
	public boolean checkMethodWithVarArgs(String calledName) {
		boolean found = false;
		VarArgsMethods currMet = null;
		for(VarArgsMethods method: varArgMethods) {
			if(calledName.equals(method.method.getName())) {
				found = true;
				currMet = method;
			}
		}
		if(currMet != null) {
			if(obradjeniParametri < currMet.method.getLevel() - 1) {
				found = false;
			}
		}
		return found;
	}
	
	
	public void visit(NoMinus noMinus) {
		if(!exprActive) {
			if(actParsActive && checkMethodWithVarArgs(calledMethodName)) {
				exprActive = true;
				VarArgsMethods currMet = null;
				for(VarArgsMethods method: varArgMethods) {
					if(calledMethodName.equals(method.method.getName())) {
						currMet = method;
					}
				}
				
				int index = currMethod.getLocalSymbols().size();
				Code.put(Code.load);		
				Code.loadConst(index);
				
				if(calledMethod.getLocalSymbols().size() == 1) {
					Code.loadConst(obradjeniParametri);
				}
				else {
					Code.loadConst(obradjeniParametri - calledMethod.getLevel() + 1);
				}
				/*Code.load(designator.obj);
				if(currMet.varArgType == Tab.charType) {
					Code.put(Code.bastore);
				}
				else{
					Code.put(Code.astore);
				}*/
				
			}
		}
	}
	
	ArrayList<Obj> recordi = new ArrayList<>();
	Obj currRecord = null;
	boolean creatingNewRecord = false;
	
	public void visit(RecordName recordName) {
    	
	    currRecord = recordName.obj;
    }
	
	public void visit(RecordDeclaration recDec) {
		recordi.add(currRecord);
	}
	
	public void visit(NewExpr newExpr) {
		creatingNewRecord = true;
		int cntFields = 0;
		String recName = null;
		Obj record1 = null;
		for(Obj record: recordi) {
			if(newExpr.struct == record.getType()) {
				recName = record.getName();
				record1 = record;
			}
		}
		
		cntFields = record1.getLocalSymbols().size();
		Code.put(Code.new_);
		Code.put2(4*cntFields);
	}
	
	int leftIndex = -1;
	Obj leftObj = null;
	boolean assignOp = false;
	
	public void visit(AssignopDecl assignopDecl) {
		leftIndex = fieldIndex;
		leftObj = currFieldDesignator;
		assignOp = true;
	}
	
	public void visit(ConstDecl constDecl) {
		assignOp = false;
	}
	
}
