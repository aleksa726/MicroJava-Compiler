package rs.ac.bg.etf.pp1;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;
import rs.etf.pp1.symboltable.visitors.DumpSymbolTableVisitor;

public class SemanticAnalyzer extends VisitorAdaptor {
	
	int varDeclCount = 0;
	int globalVarsCount = 0;
	int localVarsCount = 0;
	
	
	Struct boolType = new Struct(Struct.Bool);
	
	boolean errorDetected = false;
	boolean returnFound = false;
	boolean mainMethod = false;
	boolean validType = false;
	
	Struct currentVarType;
	Struct currentMethodType;
	// indeksiranje ide od 1, a 0 je za promenljive, a -1 za varArgs
	int currMethodFpPos = 0;
	Struct currentConstType;
	Struct currAssingDesignatorType;
	
	Obj currMethod = null;
	Obj currDesignator = null;
	Obj currClass = null;
	Obj currRecord = null;
	String currClassName = null;
	
	int doStatementCount = 0;
	
	ArrayList<Obj> assignDesignators = new ArrayList<>();
	boolean assingOp = true;
	
	ArrayList<Struct> tipoviArgumenata = new ArrayList<>();
    int actParamCount = 0;
    
    ArrayList<Obj> recordi = new ArrayList<>();
	
	
	
	Logger log = Logger.getLogger(getClass());
	
	public void report_error(String message, SyntaxNode info) {
		errorDetected = true;
		
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" - na liniji ").append(line);
		log.error(msg.toString());
	}

	public void report_info(SyntaxNode info, Obj obj) {
		DumpSymbolTableVisitor dumpVisitor = new DumpSymbolTableVisitor();
		dumpVisitor.visitObjNode(obj);
		StringBuilder msg = new StringBuilder(""); 
		int line = (info == null) ? 0: info.getLine();
		if (line != 0) {
			msg.append ("Pretraga na liniji ").append(line).append(" (").append(obj.getName()).append("), nadjeno ").append(dumpVisitor.getOutput());
		}
		msg.deleteCharAt(msg.length()-1);
		log.info(msg.toString());
	}
	
	
	
   
    
    public void visit(ProgramName progName) {
    	Tab.insert(Obj.Type, "bool", boolType);
    	
    	Obj progNode = Tab.insert(Obj.Prog, progName.getProgName(), Tab.noType);
    	progName.obj = progNode;
    	report_info(progName, progNode);
    	Tab.openScope();
    }
    
    public void visit(Program program) {
    	Tab.chainLocalSymbols(program.getProgramName().obj);
    	Tab.closeScope();
    	if(!mainMethod) {
    		report_error("Program nema main metodu! ", program);
    	}
    	
    }
    
    
    
    public void visit(ClassName className) {
    	if(Tab.find(className.getClassName()) == Tab.noObj){
	    	Struct classStruct = new Struct(Struct.Class);
	    	classStruct.setElementType(Tab.noType);
	    	Obj classNode = Tab.insert(Obj.Type, className.getClassName(), classStruct);
	    	className.obj = classNode;
	    	currClass = classNode;
	    	currClassName = className.getClassName();
	    	Tab.openScope();
	    	report_info(className, classNode);
    	}
    	else {
    		report_error("Ime " + className.getClassName() + " je zauzeto!", className);
    	}
    }
    
    public void visit(ClassDecl classDecl) {
    	Tab.chainLocalSymbols(currClass);
    	Tab.closeScope();
    	report_info(classDecl, currClass);
    	currClass = null;
    	currClassName = null;
    }
    
    public void visit(ClassExtensionType classExtensionType) {
    	Struct parentClass = classExtensionType.getType().struct;
    
    	if(parentClass.getKind() != Struct.Class) {
    		report_error("Tip iz kog se izvodi nije klasa", classExtensionType);
    		classExtensionType.struct = Tab.noType;
    	}
    	else{
    		boolean ok = true;
    		for(Obj currObj: recordi) {
    			if(classExtensionType.getType().getTypeName().equals(currObj.getName())) {
    				ok = false;
    			}
    		}
    		if(ok) {
    			if(!classExtensionType.getType().getTypeName().equals(currClassName)) {
    				classExtensionType.struct = parentClass;
    			}
    			else {
    				report_error("Ne moze se izvesti sam iz sebe!" , classExtensionType);
    			}
    		}
    		else {
    			report_error("Ne moze se izvesti iz recorda!" , classExtensionType);
    		}
    	}
    }
    
    
    
    
    public void visit(RecordName recordName) {
    	if(Tab.find(recordName.getRecordName()) == Tab.noObj){
	    	Struct recortStruct = new Struct(Struct.Class);
	    	recortStruct.setElementType(Tab.noType);
	    	Obj recordNode = Tab.insert(Obj.Type, recordName.getRecordName(), recortStruct);
	    	recordName.obj = recordNode;
	    	currRecord = recordNode;
	    	Tab.openScope();
	    	report_info(recordName, recordNode);
    	}
    	else {
    		report_error("Ime " + recordName.getRecordName() + " je zauzeto!", recordName);
    	}
    }
    
    public void visit(RecordDeclaration recordDecl) {
    	Tab.chainLocalSymbols(recordDecl.getRecordName().obj);
    	Tab.closeScope();
    	report_info(recordDecl, currRecord);
    	recordi.add(currRecord);
    	currRecord = null;
    }
    
    
    
    
    
    public void visit(ConstDecl constDecl) {
    	currentConstType = null;
    }
    
    public void visit(ConstType constType) {
    	currentConstType = constType.getType().struct;
    }
    
    public void visit(NumConst numConst) {
    	if(currentConstType == Tab.intType) {
    		Obj tmp = Tab.find(numConst.getConstName());
    		if(tmp == Tab.noObj) {
    			int constVal = 0;
    			
    			
		    	Obj constNode = Tab.insert(Obj.Con, numConst.getConstName(), currentConstType);
		    	constNode.setAdr(numConst.getN2());
		    	
		    	numConst.obj = constNode;
		    	
		    	report_info(numConst, constNode);
    		}
    		else {
    			report_error("Ime " + numConst.getConstName() + " je zauzeto!", numConst);
    		}
    	}
    	else {
    		report_error("Tip konstante nije int! ", numConst);
    	}
    }
    
    public void visit(CharConst charConst) {
    	if(currentConstType == Tab.charType) {
    		Obj tmp = Tab.find(charConst.getConstName());
    		if(tmp == Tab.noObj) {
		    	Obj constNode = Tab.insert(Obj.Con, charConst.getConstName(), currentConstType);
		    	constNode.setAdr(charConst.getC2());
		    	charConst.obj = constNode;
		    	report_info(charConst, constNode);
    		}
    		else {
    			report_error("Ime " + charConst.getConstName() + " je zauzeto!", charConst);
    		}
    	}
    	else {
    		report_error("Tip konstante nije char! ", charConst);
    	}
    }
    
    public void visit(BoolConst boolConst) {
    	if(currentConstType == boolType) {
    		Obj tmp = Tab.find(boolConst.getConstName());
    		if(tmp == Tab.noObj) {
		    	Obj constNode = Tab.insert(Obj.Con, boolConst.getConstName(), currentConstType);
		    	if(boolConst.getB2()) {
		    		constNode.setAdr(1);
		    	}
		    	else {
		    		constNode.setAdr(0);
		    	}
		    	
		    	boolConst.obj = constNode;
		    	report_info(boolConst, constNode);
    		}
    		else {
    			report_error("Ime " + boolConst.getConstName() + " je zauzeto!", boolConst);
    		}
    	}
    	else {
    		report_error("Tip konstante nije bool! ", boolConst);
    	}
    }
    
    
    
    
    
    public void visit(VarDecl varDecl) {
    	currentVarType = null;
    }
    
    public void visit(VarType varType) {
    	currentVarType = varType.getType().struct;
    }
    
    public void visit(VarDeclarationDecl varDeclaration) {
    	if(validType) {
	    	Scope currentScope = Tab.currentScope;
			Obj res = currentScope.findSymbol(varDeclaration.getVarName());
			if(res != null) {
				report_error("Ime " + varDeclaration.getVarName() + " je zauzeto!", varDeclaration);
			} 
			else{
				varDeclCount++;
				if(currMethod == null) {
					if(globalVarsCount >= 65536) {
						log.error("Program ima vise od 65536 lokalnih promenljivih!" );
					}
					else globalVarsCount++;
				}
				else {
					if(localVarsCount >= 256) {
			    		log.error("Program ima vise od 256 lokalnih promenljivih!" );
			    	}
					else localVarsCount++;
				}
				if(currRecord == null) {
		    		Obj varNode = Tab.insert(Obj.Var, varDeclaration.getVarName(), currentVarType);
		    		varDeclaration.obj = varNode;
		    		report_info(varDeclaration, varNode);
		    		if(currMethod == null) {
		    			varNode.setLevel(0);
					}
		        	else {
		        		varNode.setLevel(1);
		        	}
				}
				else {
					Obj varNode = Tab.insert(Obj.Fld, varDeclaration.getVarName(), currentVarType);
		    		varDeclaration.obj = varNode;
		    		report_info(varDeclaration, varNode);
				}
			}
    	}
    	else {
    		report_error("Tip promenljive nije ispravan!", varDeclaration);
    	}
    }
    
    public void visit(ArrayDeclaration arrayDeclaration) {
    	if(validType) {
	    	Scope currentScope = Tab.currentScope;
			Obj res = currentScope.findSymbol(arrayDeclaration.getVarName());
			if(res != null) {
				report_error("Ime " + arrayDeclaration.getVarName() + " je zauzeto!", arrayDeclaration);
			} 
			else{
				varDeclCount++;
				if(currMethod == null) {
					if(globalVarsCount >= 65536) {
						log.error("Program ima vise od 65536 lokalnih promenljivih!" );
					}
					else globalVarsCount++;
				}
				else {
					if(localVarsCount >= 256) {
			    		log.error("Program ima vise od 256 lokalnih promenljivih!" );
			    	}
					else localVarsCount++;
				}
				Struct arrayStruct = new Struct(Struct.Array);
	        	arrayStruct.setElementType(currentVarType);
	        	if(currRecord == null) {
		        	Obj varArrNode = Tab.insert(Obj.Var, arrayDeclaration.getVarName(), arrayStruct);
		        	arrayDeclaration.obj = varArrNode;
		        	report_info(arrayDeclaration, varArrNode);
		        	if(currMethod == null) {
						varArrNode.setLevel(0);
					}
		        	else {
		        		varArrNode.setLevel(1);
		        	}
	        	}
	        	else {
	        		Obj varArrNode = Tab.insert(Obj.Fld, arrayDeclaration.getVarName(), arrayStruct);
		        	arrayDeclaration.obj = varArrNode;
		        	report_info(arrayDeclaration, varArrNode);
	        	}
			}
    	}
    	else {
    		report_error("Tip promenljive nije ispravan!", arrayDeclaration);
    	}
		
    }
    
    
    
    
    public void visit(Type type) {
    	Obj typeNode = Tab.find(type.getTypeName());
    	if(typeNode == Tab.noObj) {
    		report_error("Trazeni tip " + type.getTypeName() +" nije pronadjen u tabeli simbola!", type);
    		type.struct = Tab.noType;
    		validType = false;
    	}
    	else {
    		if(Obj.Type == typeNode.getKind()) {
    			type.struct = typeNode.getType();
    			validType = true;
    		}
    		else {
    			report_error("Ime " + type.getTypeName() +" ne predstavlja tip!", type);
    			type.struct = Tab.noType;
    			validType = false;
    		}
    	}
    }
    
    
    
    
    public void visit(MethodTypeName methodTypeName) {
    	if(validType) {
	    	Obj meth = Tab.find(methodTypeName.getMethodName());
	    	if(meth == Tab.noObj) {
	    		if(methodTypeName.getMethodName().equals("main")) {
	    			mainMethod = true;
	    		}
		    	currMethod = Tab.insert(Obj.Meth, methodTypeName.getMethodName(), currentMethodType);
		    	methodTypeName.obj = currMethod;
		    	currMethod.setLevel(0);
		    	Tab.openScope();
		    	report_info(methodTypeName, currMethod);
	    	}
	    	else {
	    		report_error("Ime " + methodTypeName.getMethodName() + " je vec zauzeto!", methodTypeName);
	    	}
    	}
    	else {
    		report_error("Povratni tip metode nije ispravan!", methodTypeName);
    	}

    }
    
    public void visit(MethodDeclaration methodDecl) {
    	if(currMethod != null) {
	    	if(!returnFound && currentMethodType != Tab.noType){
				report_error("Semanticka greska na liniji " + methodDecl.getLine() + ": funkcija " + currMethod.getName() + " nema return iskaz!", null);
	    	}
	    	Tab.chainLocalSymbols(currMethod);
	    	Tab.closeScope();
	    	returnFound = false;
	    	currMethod = null;
	    	currMethodFpPos = 0;
    	}
    }
    
    public void visit(MethodType methodType) {
    	currentMethodType = methodType.getType().struct;
    }
    
    public void visit(VoidType voidType) {
    	currentMethodType = Tab.noType;
    	validType = true;
    }
    
    
    
    
    
    public void visit(FormParsAndVarArgs formParsAndVarArgs) {
    	if(currMethod != null) {
    		currMethod.setLocals(Tab.currentScope.getLocals());
    	}
    }
    
    public void visit(FormalParamDeclaration formalParam) {
    	if(currMethod != null) {
    		if(!formalParam.getParamName().equals(currMethod.getName())) {
		    	Scope currentScope = Tab.currentScope;
				Obj res = currentScope.findSymbol(formalParam.getParamName());
				if(res != null) {
					report_error("Ime " + formalParam.getParamName() + " je zauzeto!", formalParam);
				} 
				else {
					currMethod.setLevel(currMethod.getLevel() + 1);
			    	Obj param = Tab.insert(Obj.Var, formalParam.getParamName(), formalParam.getType().struct);
			    	formalParam.obj = param;
			    	formalParam.obj.setFpPos(++currMethodFpPos);
			    	report_info(formalParam, param);
				}
    		}
    		else {
    			report_error("Ime " + formalParam.getParamName() + " je zauzeto!", formalParam);
    		}
    	}
    }
    
    public void visit(FormalParamArray formalParamArray) {
    	if(currMethod != null) {
    		if(!formalParamArray.getParamName().equals(currMethod.getName())) {
		    	Scope currentScope = Tab.currentScope;
				Obj res = currentScope.findSymbol(formalParamArray.getParamName());
				if(res != null) {
					report_error("Ime " + formalParamArray.getParamName() + " je zauzeto!", formalParamArray);
				} 
				else {
					currMethod.setLevel(currMethod.getLevel() + 1);
					Struct arrayStruct = new Struct(Struct.Array);
					arrayStruct.setElementType(formalParamArray.getType().struct);
			    	Obj param = Tab.insert(Obj.Var, formalParamArray.getParamName(), arrayStruct);
			    	formalParamArray.obj = param;
			    	formalParamArray.obj.setFpPos(++currMethodFpPos);
			    	report_info(formalParamArray, param);
				}
    		}
    		else {
    			report_error("Ime " + formalParamArray.getParamName() + " je zauzeto!", formalParamArray);
    		}
    	}
    }
    
    public void visit(VarArgsDecl varArgs) {
    	if(currMethod != null) {
    		if(!varArgs.getArgsName().equals(currMethod.getName())) {
		    	Scope currentScope = Tab.currentScope;
				Obj res = currentScope.findSymbol(varArgs.getArgsName());
				if(res != null) {
					report_error("Ime " + varArgs.getArgsName() + " je zauzeto!", varArgs);
				} 
				else {
					currMethod.setLevel(currMethod.getLevel() + 1);
					Struct arrayStruct = new Struct(Struct.Array);
					arrayStruct.setElementType(varArgs.getType().struct);
			    	Obj param = Tab.insert(Obj.Var, varArgs.getArgsName(), arrayStruct);
			    	varArgs.obj = param;
			    	varArgs.obj.setFpPos(-1);
			    	report_info(varArgs, param);
				}
    		}
    		else {
    			report_error("Ime " + varArgs.getArgsName() + " je zauzeto!", varArgs);
    		}
    	}
    }
    
    
    
   
    
    public void visit(Designator designator) {
    	designator.obj = currDesignator;
    	currDesignator = null;
    }
    
    public void visit(OnlyIdent designatorExpr) {
    	Obj obj = Tab.find(designatorExpr.getDesignatorName().getDesignatorName());
    	if(obj == Tab.noObj) {
    		report_error("Ime " + designatorExpr.getDesignatorName().getDesignatorName() +" nije deklarisano!", designatorExpr);
    	}
    	designatorExpr.obj = obj;
    	
    }
    
    public void visit(DesignatorName designatorName) {
    	Obj obj = Tab.find(designatorName.getDesignatorName());
    	if(obj == Tab.noObj) {
    		report_error("Ime " + designatorName.getDesignatorName() +" nije deklarisano!", designatorName);
    	}
    	else {
	    	designatorName.obj = obj;
	    	currDesignator = obj;
    	}
    }
    
    /*public void visit(DesignatorOptDecl designatorOptDecl) {
    	if(designatorOptDecl.getDesignatorOptions() instanceof ArrOption) {
    		
    	}
    }*/
    
    public void visit(ArrayDesignator arrOption) {
    	
    	Obj obj = Tab.find(arrOption.getDesignatorName().getDesignatorName());
    	if(obj == Tab.noObj) {
    		report_error("Ime " + arrOption.getDesignatorName().getDesignatorName() +" nije deklarisano!", arrOption);
    	}
    	arrOption.obj = obj;
    
    	Struct t = arrOption.getExpr().struct;
    	if(t == Tab.intType) {
    		
    		if(currDesignator == null) {
        		report_error("Nepostojeci simbol!", arrOption);
        	}
        	else {
        		//arrOption.obj = new Obj(Obj.Var, arrOption.getDesignatorName().getDesignatorName(), obj.getType().getElemType());
        	}
    	}
    	else {
    		if(t.getKind() == Struct.Array) {
    			if(t.getElemType().getKind() != Struct.Int) {
    				report_error("Nekompatibilni tipovi u izrazu za pristup clanu niza!", arrOption);
    			}
    		}
    		else {
    			report_error("Nekompatibilni tipovi u izrazu za pristup clanu niza!", arrOption);
    		}
    	}
    }
    
   
    
 
    public void visti(DesignatorStatementAssign designatorStatementAssign) {
    	currAssingDesignatorType = null;
    }
    
    public void visit(AssignDesignatorDecl assignDesignator) {
    	Obj designator = assignDesignator.getDesignator().obj;
    	Struct exp = assignDesignator.getExpr().struct;
    	if(designator.getKind() == Obj.Var || designator.getKind() == Obj.Fld || designator.getKind() == Obj.Elem) {
    		
    		if(!assignDesignator.getExpr().struct.assignableTo(designator.getType())) {
    			if(designator.getType().getKind() == Struct.Array) {
    				if(!assignDesignator.getExpr().struct.assignableTo(designator.getType().getElemType())) {
    					if(assignDesignator.getExpr().struct.getElemType() != null) {
    						if(assignDesignator.getExpr().struct.getElemType() == designator.getType().getElemType()) {
    							
    						}
    						else {
    							report_error("Tip designatora nije istog tipa kao expr", assignDesignator);
    						}
    					}
    				}
    			}
    			else {
    				if(exp.getKind() == Struct.Array) {
    					if(!assignDesignator.getExpr().struct.getElemType().assignableTo(designator.getType())) {
        					report_error("Tip designatora nije istog tipa kao expr", assignDesignator);
        				}
    				}
    				else{
    					report_error("Tip designatora nije istog tipa kao expr", assignDesignator);
    				}
    			}
    		}
    		/*if(assignDesignator.getExpr().struct.getKind() == Struct.Array) {
    			if(assignDesignator.getExpr().struct.getElemType().assignableTo(designator.getType())) {
    				currAssingDesignatorType = arrayStruct;
    			}
    			else {
    				report_error("Tip designatora nije istog tipa kao expr", assignDesignator);
    			}
    		}
    		else {
    			if(assignDesignator.getExpr().struct.getKind() == Struct.Class) {
    				
    				if(assignDesignator.getExpr().struct == designator.getType()) {
    					
    				}
    				else {
    					report_error("Tip designatora nije istog tipa kao expr", assignDesignator);
    				}
    				
    			}
    			else {
		    		if(assignDesignator.getExpr().struct.assignableTo(assignDesignator.getDesignator().obj.getType())) {
		    			currAssingDesignatorType = assignDesignator.getDesignator().obj.getType();
		    		}
		    		else {
		    			report_error("Tip designatora nije istog tipa kao expr", assignDesignator);
		    		}
    			}
    		}*/
    	}
    	else {
    		report_error("Designator nije promenljiva ili polje klase! ", assignDesignator);
    	}
    }
    
    public void visit(DesignatorStatementINC designatorStatementINC) {
    	Obj designator = designatorStatementINC.getDesignator().obj;
    	if(designator.getKind() == Obj.Var || designator.getKind() == Obj.Fld || designator.getKind() == Obj.Elem) {
    		if(designator.getType().compatibleWith(Tab.intType)) {
    			
    		}
    		else {
    			if(designator.getType().getKind() == Struct.Array) {
    				if(designator.getType().getElemType().compatibleWith(Tab.intType)) {
    	    			
    	    		}
    				else {
    					report_error("Tip designatora nije tipa Int", designatorStatementINC);
    				}
    			}
    			else {
    				report_error("Tip designatora nije tipa Int", designatorStatementINC);
    			}
    		}
    	}
    	else {
    		report_error("Designator nije promenljiva ili polje klase! ", designatorStatementINC);
    	}
    }
    
    public void visit(DesignatorStatementDEC designatorStatementDEC) {
    	Obj designator = designatorStatementDEC.getDesignator().obj;
    	if(designator.getKind() == Obj.Var || designator.getKind() == Obj.Fld || designator.getKind() == Obj.Elem) {
    		if(designator.getType().compatibleWith(Tab.intType)) {
    			
    		}
    		else {
    			if(designator.getType().getKind() == Struct.Array) {
    				if(designator.getType().getElemType().compatibleWith(Tab.intType)) {
    	    			
    	    		}
    				else {
    					report_error("Tip designatora nije tipa Int", designatorStatementDEC);
    				}
    			}
    			else {
    				report_error("Tip designatora nije tipa Int", designatorStatementDEC);
    			}
    		}
    	}
    	else {
    		report_error("Designator nije promenljiva ili polje klase! ", designatorStatementDEC);
    	}
    }
    
   
    
    
    public void visit(MethodCallDesignator methodCallDesignator) {
    	methodCallDesignator.obj = methodCallDesignator.getDesignator().obj;
    	
    }
    
   
    
    public void visit(ActualParam actualParam) {
    	tipoviArgumenata.add(actualParam.getExpr().struct);
    	actParamCount++;
    }
    
    public void visit(ActualParams actualParams) {
    	tipoviArgumenata.add(actualParams.getExpr().struct);
    	actParamCount++;
    }
    
    public void visit(DesignatorStatementPars designatorStatementPars) {
    	Obj designator = designatorStatementPars.getMethodCallDesignator().getDesignator().obj;
    	if(designator.getName().equals("len")) {
    		return;
    	}
    	Collection<Obj> formalniParametriLokalnePromenljive = designator.getLocalSymbols();
    	
    	
    	ArrayList<Obj> formalniParametri = new ArrayList<Obj>();
    	boolean imaVarArgs = false;
    	for(Obj parametar: formalniParametriLokalnePromenljive) {
    		if(parametar.getFpPos() != 0) {
    			if(parametar.getFpPos() == -1) {
    				imaVarArgs = true;
    			}
    			formalniParametri.add(parametar);
    		}
    	}
    	
    	if(designator.getKind() == Obj.Meth) {
    		
    		if(formalniParametri.size() == actParamCount && imaVarArgs == false) {
    			int index = 0;
    			boolean ok = true;
    			for(Obj parametar: formalniParametri) {
    				if(tipoviArgumenata.get(index).getElemType() != null) {
    					if(!parametar.getType().equals(tipoviArgumenata.get(index).getElemType())) {
    						if(parametar.getType().getElemType() != null) {
    							if(!parametar.getType().getElemType().equals(tipoviArgumenata.get(index).getElemType())) {
    								ok = false;
        							break;
    							}
    							else {
    								index++;
    							}
    						}
    						else {
    							ok = false;
    							break;
    						}
        				}
        				else {
        					index++;
        				}
    				}
    				else {
        				if(!parametar.getType().equals(tipoviArgumenata.get(index))) {
        					ok = false;
        					break;
        				}
        				else {
        					index++;
        				}
    				}
    			}
    			if(ok) {
    				report_info(designatorStatementPars, designator);
    			}
    			else {
    				report_error("Tipovi argumenata se ne poklapaju!" , designatorStatementPars);
    			}
    		}
    		else if(imaVarArgs) {
    			int index = 0;
    			boolean ok = true;
    			for(Struct tipArgumenta: tipoviArgumenata) {
    				if(tipArgumenta.getElemType() != null) {
    					if(tipArgumenta.getElemType().getKind() != formalniParametri.get(index).getKind()) {
	    					if(formalniParametri.get(index).getFpPos() == -1) {
	    						int kind = formalniParametri.get(index).getType().getElemType().getKind();
	    						if(tipArgumenta.getKind() != kind) {
	    							ok = false;
	        						break;
	    						}
	    					}
	    					else {
	    						ok = false;
	    						break;
	    					}
	    				}
	    				else {
	    					if(index < formalniParametri.size()-1) {
	    						index++;
	    					}
	    				}
    				}
    				else {
	    				if(tipArgumenta.getKind() != formalniParametri.get(index).getType().getKind()) {
	    					if(formalniParametri.get(index).getFpPos() == -1) {
	    						int kind = formalniParametri.get(index).getType().getElemType().getKind();
	    						if(tipArgumenta.getKind() != kind) {
	    							ok = false;
	        						break;
	    						}
	    					}
	    					else {
	    						ok = false;
	    						break;
	    					}
	    				}
	    				else {
	    					if(index < formalniParametri.size()-1) {
	    						index++;
	    					}
	    				}
    				}
    			}
    			if(ok) {
    				report_info(designatorStatementPars, designator);
    			}
    			else {
    				report_error("Tipovi argumenata se ne poklapaju!" , designatorStatementPars);
    			}
    		}
    		else {
    			report_error("Poziv metode nema odgovarajuci broj argumenata! ", designatorStatementPars);
    		}
    	}
    	else {
    		report_error("Designator " + designator.getName() + " nije metoda", designatorStatementPars);
    	}
    	actParamCount = 0;
    	tipoviArgumenata = new ArrayList<>();
    }
    
    
    
    
    public void visit(SingleTermDeclaration singleTerm) {
    	singleTerm.struct = singleTerm.getFactor().struct;
    }
    
    
    
    public void visit(MinusExprDeclaration minusExprDeclaration) {
    	Struct exprDecl = minusExprDeclaration.getExprDecl().struct;
    	Struct term = minusExprDeclaration.getTerm().struct;
    	if(exprDecl == null) {
    		if(term == null) {
    			report_error("Nekompatibilni tipovi u izrazu za sabiranje!", minusExprDeclaration);
    		}
    		else {
    			minusExprDeclaration.struct = term;
    		}
    	}
    	else {
    		if(exprDecl.compatibleWith(Tab.intType) && term.compatibleWith(Tab.intType)) {
    			minusExprDeclaration.struct = Tab.intType;
	    	}
	    	else {
	    		if(term.getElemType() != null) {
	    			if(term.getElemType().compatibleWith(Tab.intType)) {
	    				minusExprDeclaration.struct = Tab.intType;
	    			}
	    			else {
	    				report_error("Nekompatibilni tipovi u izrazu za sabiranje!", minusExprDeclaration);
	    				minusExprDeclaration.struct = Tab.noType;
	    			}
	    		}
	    		else {
	    			if(exprDecl.getElemType().compatibleWith(Tab.intType)) {
	    				minusExprDeclaration.struct = Tab.intType;
	    			}
	    			else {
	    				report_error("Nekompatibilni tipovi u izrazu za sabiranje!", minusExprDeclaration);
	    				minusExprDeclaration.struct = Tab.noType;
	    			}
	    			
	    		}
	    	}
    	}
    }
    
    public void visit(ExprDeclaration exprDeclaration) {
    	Struct expr = exprDeclaration.getExprDecl().struct;
    	Struct term = exprDeclaration.getTerm().struct;
    	if(expr == null) {
    		if(term == null) {
    			report_error("Nekompatibilni tipovi u izrazu za sabiranje!", exprDeclaration);
    		}
    		else {
    			exprDeclaration.struct = term;
    		}
    	}
    	else {
    		if(expr.compatibleWith(Tab.intType) && term.compatibleWith(Tab.intType)) {
    			exprDeclaration.struct = Tab.intType;
	    	}
	    	else {
	    		if(term.getElemType() != null) {
	    			if(term.getElemType().compatibleWith(Tab.intType)) {
	    				exprDeclaration.struct = Tab.intType;
	    			}
	    			else {
	    				report_error("Nekompatibilni tipovi u izrazu za sabiranje!", exprDeclaration);
	    				exprDeclaration.struct = Tab.noType;
	    			}
	    		}
	    		else {
	    			if(expr.getElemType().compatibleWith(Tab.intType)) {
	    				exprDeclaration.struct = Tab.intType;
	    			}
	    			else {
	    				report_error("Nekompatibilni tipovi u izrazu za sabiranje!", exprDeclaration);
	    				exprDeclaration.struct = Tab.noType;
	    			}
	    			
	    		}
	    	}
    	}
    }
   
   public void visit(TermDeclaration termDeclaration) {
    	Struct term = termDeclaration.getTerm().struct;
    	Struct factor = termDeclaration.getFactor().struct;
    	if(term == null) {
    		if(factor == null) {
    			report_error("Nekompatibilni tipovi u izrazu za sabiranje!", termDeclaration);
    		}
    		else {
    			termDeclaration.struct = factor;
    		}
    	}
    	else {
    		if(factor.compatibleWith(Tab.intType) && term.compatibleWith(Tab.intType)) {
    			termDeclaration.struct = Tab.intType;
	    	}
	    	else {
	    		boolean isFactorArr = false;
	    		boolean isTermArr = false;
	    		boolean factorArrValid = false;
	    		boolean termArrValid = false;
	    		if(factor.getKind() == Struct.Array) {
	    			if(factor.getElemType().compatibleWith(Tab.intType)) {
	    				factorArrValid = true;
	    			}
	    			isFactorArr = true;
	    		}
	    		if(term.getKind() == Struct.Array) {
	    			if(term.getElemType().compatibleWith(Tab.intType)) {
	    				termArrValid = true;
	    			}
	    			isTermArr = true;
	    		}
	    		
	    		if(isFactorArr) {
	    			if(isTermArr) {
	    				if(factorArrValid && termArrValid) {
	    					termDeclaration.struct = Tab.intType;
	    				}
	    				else {
	    					report_error("Nekompatibilni tipovi u izrazu za mnozenje!", termDeclaration);
		    	    		termDeclaration.struct = Tab.noType;
	    				}
	    			}
	    			else {
	    				if(term.compatibleWith(Tab.intType)) {
	    					termDeclaration.struct = Tab.intType;
	    				}
	    				else {
	    					report_error("Nekompatibilni tipovi u izrazu za mnozenje!", termDeclaration);
		    	    		termDeclaration.struct = Tab.noType;
	    				}
	    			}
	    		}
	    		else if(isTermArr) {
	    			if(isFactorArr) {
	    				if(factorArrValid && termArrValid) {
	    					termDeclaration.struct = Tab.intType;
	    				}
	    				else {
	    					report_error("Nekompatibilni tipovi u izrazu za mnozenje!", termDeclaration);
		    	    		termDeclaration.struct = Tab.noType;
	    				}
	    			}
	    			else {
	    				if(factor.compatibleWith(Tab.intType)) {
	    					termDeclaration.struct = Tab.intType;
	    				}
	    				else {
	    					report_error("Nekompatibilni tipovi u izrazu za mnozenje!", termDeclaration);
		    	    		termDeclaration.struct = Tab.noType;
	    				}
	    			}
	    		}
	    		else {
	    			report_error("Nekompatibilni tipovi u izrazu za mnozenje!", termDeclaration);
		    		termDeclaration.struct = Tab.noType;
	    		}
	    		
	    	}
    	}
    }
   
   public void visit(SingleFactorDecl singleFactor) {
	   singleFactor.struct = singleFactor.getBaseExp().struct;
   }
    
    public void visit(FactorDecls factorDecls) {
    	Struct factor = factorDecls.getFactor().struct;
    	Struct baseExp = factorDecls.getBaseExp().struct;
    	if(factor == null) {
    		if(baseExp == null) {
    			report_error("Nekompatibilni tipovi u izrazu za sabiranje!", factorDecls);
    		}
    		else {
    			factorDecls.struct = baseExp;
    		}
    	}
    	else {
	    	if(factor.compatibleWith(Tab.intType) && baseExp.compatibleWith(Tab.intType)) {
	    		factorDecls.struct = Tab.intType;
	    	}
	    	else {
	    		report_error("Nekompatibilni tipovi u izrazu za eksponent!", factorDecls);
	    		factorDecls.struct = Tab.noType;
	    	}
    	}
    }
    
    
    
    public void visit(FunctionCall functionCall) {
    	Obj func = functionCall.getMethodCallDesignator().getDesignator().obj;
    	if(func.getName().equals("len")) {
    		Obj designator = functionCall.getMethodCallDesignator().getDesignator().obj;
    		functionCall.struct = func.getType();
    		return;
    	}
    	if(Obj.Meth == func.getKind()) {
    		functionCall.struct = func.getType();
    		
    		Obj designator = functionCall.getMethodCallDesignator().getDesignator().obj;
        	Collection<Obj> formalniParametriLokalnePromenljive = designator.getLocalSymbols();
        	
        	
        	ArrayList<Obj> formalniParametri = new ArrayList<Obj>();
        	boolean imaVarArgs = false;
        	for(Obj parametar: formalniParametriLokalnePromenljive) {
        		if(parametar.getFpPos() != 0) {
        			if(parametar.getFpPos() == -1) {
        				imaVarArgs = true;
        			}
        			formalniParametri.add(parametar);
        		}
        	}
        	
        	if(designator.getKind() == Obj.Meth) {
        		
        		if(formalniParametri.size() == actParamCount && imaVarArgs == false) {
        			int index = 0;
        			boolean ok = true;
        			for(Obj parametar: formalniParametri) {
        				if(tipoviArgumenata.get(index).getElemType() != null) {
        					if(!parametar.getType().equals(tipoviArgumenata.get(index).getElemType())) {
	        					ok = false;
	        					break;
	        				}
	        				else {
	        					index++;
	        				}
        				}
        				else {
	        				if(!parametar.getType().equals(tipoviArgumenata.get(index))) {
	        					ok = false;
	        					break;
	        				}
	        				else {
	        					index++;
	        				}
        				}
        			}
        			if(ok) {
        				report_info(functionCall, designator);
        			}
        			else {
        				report_error("Tipovi argumenata se ne poklapaju!" , functionCall);
        			}
        		}
        		else if(imaVarArgs) {
        			int index = 0;
        			boolean ok = true;
        			for(Struct tipArgumenta: tipoviArgumenata) {
        				if(tipArgumenta.getElemType() != null) {
        					if(tipArgumenta.getElemType().getKind() != formalniParametri.get(index).getKind()) {
    	    					if(formalniParametri.get(index).getFpPos() == -1) {
    	    						int kind = formalniParametri.get(index).getType().getElemType().getKind();
    	    						if(tipArgumenta.getKind() != kind) {
    	    							ok = false;
    	        						break;
    	    						}
    	    					}
    	    					else {
    	    						ok = false;
    	    						break;
    	    					}
    	    				}
    	    				else {
    	    					if(index < formalniParametri.size()-1) {
    	    						index++;
    	    					}
    	    				}
        				}
        				else {
    	    				if(tipArgumenta.getKind() != formalniParametri.get(index).getType().getKind()) {
    	    					if(formalniParametri.get(index).getFpPos() == -1) {
    	    						int kind = formalniParametri.get(index).getType().getElemType().getKind();
    	    						if(tipArgumenta.getKind() != kind) {
    	    							ok = false;
    	        						break;
    	    						}
    	    					}
    	    					else {
    	    						ok = false;
    	    						break;
    	    					}
    	    				}
    	    				else {
    	    					if(index < formalniParametri.size()-1) {
    	    						index++;
    	    					}
    	    				}
        				}
        			}
        			if(ok) {
        				report_info(functionCall, designator);
        			}
        			else {
        				report_error("Tipovi argumenata se ne poklapaju!" , functionCall);
        			}
        		}
        		else {
        			if(functionCall.getMethodCallDesignator().getDesignator().obj.getName().equals("ord") || functionCall.getMethodCallDesignator().getDesignator().obj.getName().equals("chr")) {
        				
        			}
        			else {
        				report_error("Poziv metode nema odgovarajuci broj argumenata! ", functionCall);
        			}
        		}
        	}
        	else {
        		report_error("Designator " + designator.getName() + " nije metoda", functionCall);
        	}
        	actParamCount = 0;
        	tipoviArgumenata = new ArrayList<>();
    	}
    	else {
    		report_error("Ime " + functionCall.getMethodCallDesignator().getDesignator().obj.getName() +" nije funkcija!", functionCall);
    		functionCall.struct = Tab.noType;
    	}
    	
    	
    	
    }
    
    public void visit(NumberFactor numberFactor) {
    	numberFactor.struct = Tab.intType;
    }
    
    public void visit(CharFactor charFactor) {
    	charFactor.struct = Tab.charType;
    }
    
    public void visit(BoolFactor boolFactor) {
    	boolFactor.struct = boolType;
    }
    
    public void visit(Var var) {
    	var.struct = var.getDesignator().obj.getType();
    }
    
    public void visit(ExprFactor exprFactor) {
    	exprFactor.struct = exprFactor.getExpr().struct;
    }
    
    public void visit(NewArrayExpr newArrayExpr) {
    	Struct t = newArrayExpr.getExpr().struct;
    	if(t.compatibleWith(Tab.intType)) {
    		newArrayExpr.struct = new Struct(Struct.Array, newArrayExpr.getType().struct);
    	}
    	else {
    		report_error("Nekompatibilni tipovi u izrazu za kreiranje novog niza!", newArrayExpr);
    	}
    }
    
    public void visit(NewExpr newExpr) {
    	Struct s = newExpr.getType().struct;
    	if(s.getKind() == Struct.Class) {
    		newExpr.struct = s;
    		// proveriti da li je dobro
    	}
    	else {
    		report_error("Nije klasa!", newExpr);
    	}
    }
    
   
    
    
    
    
    
    public void visit(ReturnStatement returnSatement){
    	if(currMethod != null) {
	    	returnFound = true;
	    	if(!currentMethodType.compatibleWith(returnSatement.getExpr().struct)){
	    		if(returnSatement.getExpr().struct.getElemType() != null) {
	    			if(!currentMethodType.compatibleWith(returnSatement.getExpr().struct.getElemType())){
	    				report_error("Tip funkcije " + currMethod.getName() + " i povratne vrednosti se ne slazu!", returnSatement);
	    			}
	    		}
	    		else {
	    			report_error("Tip funkcije " + currMethod.getName() + " i povratne vrednosti se ne slazu!", returnSatement);
	    		}
	    	}
    	}
    }
    
    public void visit(ReturnVoidStatement returnVoidStatement) {
    	returnFound = true;
    	if(currentMethodType != Tab.noType) {
    		report_error("Tip funkcije " + currMethod.getName() + " i povratne vrednosti se ne slazu!", returnVoidStatement);
    	}
    }
    
    public void visit(Do doStm) {
    	doStatementCount++;
    }
    
    public void visit(DoWhileStatement doWhileStatement) {
    	doStatementCount--;
    }
    
    public void visit(BreakStatement breakStatement) {
    	if(doStatementCount == 0) {
    		report_error("Break se ne nalazi u do while petlji!", breakStatement);
    	}
    	else {
    		//report_info("Obradjuje se break", breakStatement);
    	}
    }
    
    public void visit(ContinueStatement continueStatement) {
    	if(doStatementCount == 0) {
    		report_error("Break se ne nalazi u do while petlji!", continueStatement);
    	}
    	else {
    		//report_info("Obradjuje se continue", continueStatement);
    	}
    }
    
    public void visit(ReadStatement readStatement) {
    	Obj designator = readStatement.getDesignator().obj;
    	if(designator.getKind() == Obj.Var || designator.getKind() == Obj.Fld || designator.getKind() == Obj.Elem) {
    		if(designator.getType().compatibleWith(Tab.intType) || designator.getType().compatibleWith(Tab.charType) || designator.getType().compatibleWith(boolType)) {
    			//report_info("Obradjuje se read", readStatement);
    		}
    		else {
    			if(designator.getType().getKind() == Struct.Array) {
    				if(designator.getType().getElemType().compatibleWith(Tab.intType) || designator.getType().getElemType().compatibleWith(Tab.charType) || designator.getType().getElemType().compatibleWith(boolType)) {
    	    			//report_info("Obradjuje se read", readStatement);
    	    		}
    				else {
    					report_error("Tip designatora nije tipa Int, Char ili Bool", readStatement);
    				}
    			}
    			else{
    				report_error("Tip designatora nije tipa Int, Char ili Bool", readStatement);
    			}
    		}
    	}
    	else {
    		report_error("Designator " + designator.getName() + " nije promenljiva ili polje klase! ", readStatement);
    	}
    }
    
    public void visit(PrintStmt print) {
		if(print.getExpr().struct == Tab.intType || print.getExpr().struct == Tab.charType || print.getExpr().struct == boolType) {
			//report_info("Obradjuje se print", print);
		}
		else {
			if(print.getExpr().struct.getKind() == Struct.Array) {
				if(print.getExpr().struct.getElemType() == Tab.intType || print.getExpr().struct.getElemType() == Tab.charType || print.getExpr().struct.getElemType() == boolType) {
					
				}
				else {
					report_error("Izraz u naredbi print nije odgovarajuceg tipa", print);
				}
			}
			else {
				report_error("Izraz u naredbi print nije odgovarajuceg tipa", print);
			}
		}	
	}
    
    
    
    
    public void visit(SingleConditionDeclaration singleCondition) {
    	Struct s = singleCondition.getCondTerm().struct;
    	
    	if(!s.equals(boolType)) {
    		report_error("Nije bool tip!" , singleCondition);
    		singleCondition.struct = Tab.noType;
    	}
    	else {
    		singleCondition.struct = s;
    	}
    	
    }
    
    public void visit(SingleCondTermDeclaration singleCondTerm) {
    	Struct s = singleCondTerm.getCondFact().struct;
    	
    	if(!s.equals(boolType)) {
    		report_error("Nije bool tip!" , singleCondTerm);
    		singleCondTerm.struct = Tab.noType;
    	}
    	else {
    		singleCondTerm.struct = s;
    	}
    	
    }
    
    public void visit(CondFactDeclaration condFact) {
    	Struct s = condFact.getExpr().struct;
    	
    	if(!s.equals(boolType)) {
    		report_error("Nije bool tip!" , condFact);
    		condFact.struct = Tab.noType;
    	}
    	else {
    		condFact.struct = s;
    	}
    	
    }
    
    public void visit(ConditionDeclaration conditionDeclaration) {
    	Struct e1 = conditionDeclaration.getCondition().struct;
    	Struct e2 = conditionDeclaration.getCondTerm().struct;
    	if(!e1.equals(boolType)) {
    		report_error("Izrazi nisu bool tipa ", conditionDeclaration);
    		conditionDeclaration.struct = Tab.noType;
    	}
    	else if (!e2.equals(boolType)) {
    		report_error("Izrazi nisu bool tipa ", conditionDeclaration);
    		conditionDeclaration.struct = Tab.noType;
    	}
    	else if(!e1.compatibleWith(e2)){
			report_error("Izrazi nisu kompatibilni ", conditionDeclaration);
			conditionDeclaration.struct = Tab.noType;
    	}
    	else {
    		conditionDeclaration.struct = boolType;
    	}
    }
    
    public void visit(CondTermDeclaration condTermDeclaration) {
    	Struct e1 = condTermDeclaration.getCondTerm().struct;
    	Struct e2 = condTermDeclaration.getCondFact().struct;
    	if(!e1.equals(boolType)) {
    		report_error("Izrazi nisu bool tipa ", condTermDeclaration);
    		condTermDeclaration.struct = Tab.noType;
    	}
    	else if (!e2.equals(boolType)) {
    		report_error("Izrazi nisu bool tipa ", condTermDeclaration);
    		condTermDeclaration.struct = Tab.noType;
    	}
    	else if(!e1.compatibleWith(e2)){
			report_error("Izrazi nisu kompatibilni ", condTermDeclaration);
			condTermDeclaration.struct = Tab.noType;
    	}
    	else {
    		condTermDeclaration.struct = boolType;
    	}
    }
    
    public void visit(CondFactExpresions condFactExpresions) {
    	Struct e1 = condFactExpresions.getExpr().struct;
    	Struct e2 = condFactExpresions.getExpr1().struct;
    	
    	if(e1.getKind() == Struct.Array && e2.getKind() == Struct.Array) {
    		Relop relop = condFactExpresions.getRelop();
            if ((relop instanceof RelopEqual) || (relop instanceof RelopNotEqual)) {
            	if(!e1.compatibleWith(e2)){
        			report_error("Izrazi nisu kompatibilni ", condFactExpresions);
        			condFactExpresions.struct = Tab.noType;
            	}
            	else {
            		condFactExpresions.struct = boolType;
            	}
            }
            else {
            	report_error("Nedozvoljena vrsta relacionih operacija za nizove! ", condFactExpresions);
                condFactExpresions.struct = Tab.noType;
                return;
            }
    	}
    	else if(e1.getKind() == Struct.Array && e2.getKind() != Struct.Array) {
    		if(e1.getElemType().compatibleWith(e2)) {
    			condFactExpresions.struct = boolType;
    		}
    		else {
    			report_error("Izrazi nisu kompatibilni ", condFactExpresions);
    			condFactExpresions.struct = Tab.noType;
    		}
    	}
    	else if(e1.getKind() != Struct.Array && e2.getKind() == Struct.Array) {
    		if(e2.getElemType().compatibleWith(e1)) {
    			condFactExpresions.struct = boolType;
    		}
    		else {
    			report_error("Izrazi nisu kompatibilni ", condFactExpresions);
    			condFactExpresions.struct = Tab.noType;
    		}
    	}
    	else {
	    	if(!e1.compatibleWith(e2)){
				report_error("Izrazi nisu kompatibilni ", condFactExpresions);
				condFactExpresions.struct = Tab.noType;
	    	}
	    	else {
	    		condFactExpresions.struct = boolType;
	    	}
    	}
    }
    
    
    public void visit(FieldDesignator fieldDesignator) {
    	Obj obj = Tab.find(fieldDesignator.getDesignatorName().getDesignatorName());
    	if(obj == Tab.noObj) {
    		report_error("Ime " + fieldDesignator.getDesignatorName().getDesignatorName() +" nije deklarisano!", fieldDesignator);
    	}
    	fieldDesignator.obj = obj;
    	if(obj.getType().getKind() != Struct.Class) {
    		report_error("Nije record! ", fieldDesignator);
    		fieldDesignator.obj = Tab.noObj;
    	}
    	else {
    		String recName = null;
    		for(Obj record: recordi) {
    			if(obj.getType() == record.getType()) {
    				recName = record.getName();
    			}
    		}
    		Obj record = Tab.find(recName);
    		boolean ima = false;
    		boolean tipOk = false;
    		Struct type = null;
    		for(Obj local: record.getLocalSymbols()) {
    			if(local.getName().equals(fieldDesignator.getField())) {
    				ima = true;
    				type = local.getType();
    			}
    		}
    		if(!ima) {
    			report_error("Record " + recName + " nema polje " + obj.getName()+ " !", fieldDesignator);
    		}
    		else {
    			fieldDesignator.obj = new Obj(Obj.Fld, fieldDesignator.getDesignatorName().getDesignatorName(), type);
    		}
    	}
    }
    
    
    
    public boolean passed(){
    	return !errorDetected;
    }
    
    
}
