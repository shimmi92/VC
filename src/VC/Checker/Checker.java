/**
 * Checker.java   
 * Sat Apr 26 18:23:13 EST 2014
 **/

package VC.Checker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

import VC.ASTs.*;
import VC.Scanner.SourcePosition;
import VC.Scanner.Token;
import VC.ErrorReporter;
import VC.StdEnvironment;

public final class Checker implements Visitor {

  



private String errMesg[] = {
    "*0: main function is missing",                            
    "*1: return type of main is not int",                    

    // defined occurrences of identifiers
    // for global, local and parameters
    "*2: identifier redeclared",                            
    "*3: identifier declared void",                         
    "*4: identifier declared void[]",                      

    // applied occurrences of identifiers
    "*5: identifier undeclared",                          

    // assignments
    "*6: incompatible type for =",                       
    "*7: invalid lvalue in assignment",                 

     // types for expressions 
    "*8: incompatible type for return",                
    "*9: incompatible type for this binary operator", 
    "*10: incompatible type for this unary operator",

     // scalars
     "*11: attempt to use an array/fuction as a scalar", 

     // arrays
     "*12: attempt to use a scalar/function as an array",
     "*13: wrong type for element in array initialiser",
     "*14: invalid initialiser: array initialiser for scalar",   
     "*15: invalid initialiser: scalar initialiser for array",  
     "*16: excess elements in array initialiser",              
     "*17: array subscript is not an integer",                
     "*18: array size missing",                              

     // functions
     "*19: attempt to reference a scalar/array as a function",

     // conditional expressions in if, for and while
    "*20: if conditional is not boolean",                    
    "*21: for conditional is not boolean",                  
    "*22: while conditional is not boolean",               

    // break and continue
    "*23: break must be in a while/for",                  
    "*24: continue must be in a while/for",              

    // parameters 
    "*25: too many actual parameters",                  
    "*26: too few actual parameters",                  
    "*27: wrong type for actual parameter",           

    // reserved for errors that I may have missed (J. Xue)
    "*28: misc 1",
    "*29: misc 2",

    // the following two checks are optional 
    "*30: statement(s) not reached",     
    "*31: missing return statement",    
  };

  
  
  ArrayList<String> functions = new ArrayList<String>();
  ArrayList<String> scalars = new ArrayList<String>();
  ArrayList<String> arrayArgTypes = new ArrayList<String>();
  ArrayList<String> arraySize = new ArrayList<String>();
  
  ArrayList paraTypes = new ArrayList();
  ArrayList argTypes = new ArrayList();
  HashMap<String, ArrayList> functionDecls = new HashMap<String, ArrayList>();
  
  int arrayFlag = 0;
  int argsExist = 0;
  
  String arrayTypeValue = null;
  int noArraySize = 0;
  int bcFlag = 0;
  
  boolean mainCall = false;
  boolean mainType = false;
  boolean arrayExpr = false;
  boolean callE = false;
  private SymbolTable idTable;
  private static SourcePosition dummyPos = new SourcePosition();
  private ErrorReporter reporter;

  // Checks whether the source program, represented by its AST, 
  // satisfies the language's scope rules and type rules.
  // Also decorates the AST as follows:
  //  (1) Each applied occurrence of an identifier is linked to
  //      the corresponding declaration of that identifier.
  //  (2) Each expression and variable is decorated by its type.

  public Checker (ErrorReporter reporter) {
    this.reporter = reporter;
    this.idTable = new SymbolTable ();
    establishStdEnvironment();
  }

  public void check(AST ast) {
    ast.visit(this, null);
  }


  // auxiliary methods

  private void declareVariable(Ident ident, Decl decl) {
    IdEntry entry = idTable.retrieveOneLevel(ident.spelling);

    if (entry == null) {
      ; // no problem
    } else
      reporter.reportError3(errMesg[2] + ": %", ident.spelling, ident.position);
    idTable.insert(ident.spelling, decl);
  }


  // Programs

  public Object visitProgram(Program ast, Object o) {
    ast.FL.visit(this, null);
    if(mainCall == false){
    	reporter.reportError3(errMesg[0] + ": %", ast.FL.toString(), ast.FL.position);
    }
    else {
    	if(mainType == false){
    		reporter.reportError3(errMesg[1] + ": %", ast.FL.toString(), ast.FL.position);
    	}
    }
    
    return null;
  }
 
  // Statements

  public Object visitCompoundStmt(CompoundStmt ast, Object o) {
 //   idTable.openScope();
    Type t = null;
    ast.DL.visit(this, ast);
    
    t = (Type) ast.SL.visit(this, ast);

    
  //  idTable.closeScope();
    if(t!=null){
    	
        return t;
    }
    else{
    	return null;
    	
    }
  }

  public Object visitStmtList(StmtList ast, Object o) {
	 Type t =null;

    t = (Type) ast.S.visit(this, o);
    if (ast.S instanceof ReturnStmt && ast.SL instanceof StmtList){
      reporter.reportError3(errMesg[30], "", ast.SL.position);
    }
    if(!(ast.SL instanceof EmptyStmtList )){
       t = (Type) ast.SL.visit(this, o);
    }
    
    return t;
  }


  public Object visitExprStmt(ExprStmt ast, Object o) {
    ast.E.visit(this, o);
    return null;
  }

  public Object visitEmptyStmt(EmptyStmt ast, Object o) {
    return null;
  }

  public Object visitEmptyStmtList(EmptyStmtList ast, Object o) {
    return null;
  }

  // Expressions

  // Returns the Type denoting the type of the expression. Does
  // not use the given object.


  public Object visitEmptyExpr(EmptyExpr ast, Object o) {
    ast.type = StdEnvironment.errorType;
    return ast.type;
  }

  public Object visitBooleanExpr(BooleanExpr ast, Object o) {
    ast.type = StdEnvironment.booleanType;
    return ast.type;
  }

  public Object visitIntExpr(IntExpr ast, Object o) {
	if(arrayFlag == 1){
		arraySize.add(ast.IL.spelling);
	}
	
    ast.type = StdEnvironment.intType;
    return ast.type;
  }

  public Object visitFloatExpr(FloatExpr ast, Object o) {
    ast.type = StdEnvironment.floatType;
    return ast.type;
  }

  public Object visitStringExpr(StringExpr ast, Object o) {
    ast.type = StdEnvironment.stringType;
    return ast.type;
  }

  public Object visitVarExpr(VarExpr ast, Object o) {
    ast.type = (Type) ast.V.visit(this, null);
    return ast.type;
  }

  // Declarations

  // Always returns null. Does not use the given object.

  public Object visitFuncDecl(FuncDecl ast, Object o) {
	  
	 
	  Type t = null;
	    
	    //clear the array of paradecls
	    paraTypes.clear();
	    if(ast.I.spelling.equals("main")){
	    
	    	mainCall = true;
	    }
	    if(ast.T.isIntType()){
	    	mainType = true;
	    }
	    
	    declareVariable(ast.I, ast);

	   idTable.openScope();
       ast.PL.visit(this, ast);
     // idTable.closeScope();
       t = (Type) ast.S.visit(this, ast);
       idTable.closeScope();
       if(t == null && !(ast.T instanceof VoidType)){
    	   reporter.reportError3(errMesg[31], "", ast.I.position);
       }
       else if(ast.T.assignable(t) != true && !(ast.T instanceof VoidType)){
    	   reporter.reportError3(errMesg[8], "", ast.I.position);
       }

       
       ArrayList a = new ArrayList();
       a.addAll(paraTypes);
       functionDecls.put(ast.I.spelling, a);

    return null;
  }
 
  public Object visitDeclList(DeclList ast, Object o) {
	    ast.D.visit(this, null);
    ast.DL.visit(this, null);
    return null;
  }

  public Object visitEmptyDeclList(EmptyDeclList ast, Object o) {
    return null;
  }

  public Object visitGlobalVarDecl(GlobalVarDecl ast, Object o) {
	  declareVariable(ast.I, ast);
	  Type t = null;
	  IdEntry entry = null;
		Decl check = null;

		arrayArgTypes.clear();
		t = (Type) ast.T.visit(this, ast);
	    if(t == null){
	    	return null;
	    }
		if(t.isArrayType()){
			
		    
			     if (((ArrayType) ast.T).T.isVoidType()){
			        reporter.reportError3(errMesg[4] + ": %", ast.I.spelling, ast.I.position);
			      }
			
			     check = idTable.retrieve(ast.I.spelling);
			   
			if(check instanceof FuncDecl || check.T.isFloatType() || check.T.isIntType()){
				
				reporter.reportError3(errMesg[12] + ": %", ast.I.spelling, ast.I.position);
			}
			
		}
		

		Type e = (Type) ast.E.visit(this, ast);
		if(!(ast.E instanceof EmptyExpr)&& !ast.T.isArrayType()){
			if(!ast.T.assignable(e)){
		
				reporter.reportError3(errMesg[6] + ": %", ast.E.toString(), ast.E.getPosition());
			}
	
		}

	if(arrayFlag == 1){
	  if(ast.E instanceof EmptyExpr){
		 
		  if(noArraySize == 1) {
			  reporter.reportError3(errMesg[18] + ": %", ast.E.toString(), ast.E.position);

		  }
		  
		
	  }	 
	  else{
			if(!(ast.E instanceof InitExpr)){
		  reporter.reportError3(errMesg[15] + ": %", ast.I.spelling, ast.I.position);
	      }
			else{
				  ListIterator l = arrayArgTypes.listIterator();
				  
				  while(l.hasNext()){
					  String s = (String) l.next();
					  if(!s.equals(arrayTypeValue)){
						  reporter.reportError3(errMesg[13] + ": %", ast.E.toString(), ast.E.position);
					  }
				  }
				  //for checing the initialised size and number of items initialised is the same
				  if(noArraySize == 0){
					    String s = String.valueOf(arrayArgTypes.size());
					  String s2 = arraySize.get(0);
				
					  if(!s.equals(s2)){
						  reporter.reportError3(errMesg[16] + ": %", ast.E.toString(), ast.E.position);
				
					  }
		  
				  }
				}
	  }
	}
	

	//for correct declatations of scalars
	if(arrayFlag == 0){
      	if(ast.E instanceof InitExpr){
     		reporter.reportError3(errMesg[14] + ": %", ast.E.toString(), ast.E.position);
	    }
      	if (ast.T.isVoidType()) {
            reporter.reportError3(errMesg[3] + ": %", ast.I.spelling, ast.I.position);
          }
	}
     arrayFlag=0;
    return null;
  }

  
  public Object visitLocalVarDecl(LocalVarDecl ast, Object o) {
    declareVariable(ast.I, ast);
	  Type t = null;
	  IdEntry entry = null;
		Decl check = null;

	    arrayArgTypes.clear();
		t = (Type) ast.T.visit(this, ast);
	    if(t == null){
	    	return null;
	    }
		if(t.isArrayType()){
			
			 if (((ArrayType) ast.T).T.isVoidType()){
			        reporter.reportError3(errMesg[4] + ": %", ast.I.spelling, ast.I.position);
			      }

			check = idTable.retrieve(ast.I.spelling);
			//check = entry.attr;
			if(check instanceof FuncDecl || check.T.isFloatType() || check.T.isIntType()){
				
				reporter.reportError3(errMesg[12] + ": %", ast.I.spelling, ast.I.position);
			}
			
		}
		Type e = (Type) ast.E.visit(this, ast);
		if(!(ast.E instanceof EmptyExpr) && !ast.T.isArrayType()){
			if(!ast.T.assignable(e)){
		
				reporter.reportError3(errMesg[6] + ": %", ast.E.toString(), ast.E.getPosition());
			}
	
		}
	
	if(arrayFlag == 1){
		
		
	  if(ast.E instanceof EmptyExpr){
	
		  if(noArraySize == 1) {
			  reporter.reportError3(errMesg[18] + ": %", ast.E.toString(), ast.E.position);

		  }
		  
		
	       }	
	  
	  
	    else{
			if(!(ast.E instanceof InitExpr)){
		  reporter.reportError3(errMesg[15] + ": %", ast.I.spelling, ast.I.position);
	      }
			else{
				  ListIterator l = arrayArgTypes.listIterator();
				  
				  while(l.hasNext()){
					  String s = (String) l.next();
					  if(!s.equals(arrayTypeValue)){
						  reporter.reportError3(errMesg[13] + ": %", ast.E.toString(), ast.E.position);
					  }
				  }
				  //for checing the initialised size and number of items initialised is the same
				  if(noArraySize == 0){
					    String s = String.valueOf(arrayArgTypes.size());
					  String s2 = arraySize.get(0);
				
					  if(!s.equals(s2)){
						  reporter.reportError3(errMesg[16] + ": %", ast.E.toString(), ast.E.position);
				
					  }
		  
				  }
				}
	  }
	}
	

	//for correct declatations of scalars
	if(arrayFlag == 0){
      	if(ast.E instanceof InitExpr){
     		reporter.reportError3(errMesg[14] + ": %", ast.E.toString(), ast.E.position);
	    }
      	if (ast.T.isVoidType()) {
            reporter.reportError3(errMesg[3] + ": %", ast.I.spelling, ast.I.position);
          }
      	
	}
     arrayFlag=0;
    return null;
  }

  // Parameters

 // Always returns null. Does not use the given object.

  public Object visitParaList(ParaList ast, Object o) {
    ast.P.visit(this, null);
    ast.PL.visit(this, null);
    return null;
  }

  public Object visitParaDecl(ParaDecl ast, Object o) {
	  
	
    declareVariable(ast.I, ast);
    if(ast.T.isArrayType()){
    	paraTypes.add(1);
    }
    else{
    
    paraTypes.add(ast.T);
    }
    
    if (ast.T.isVoidType()) {
      reporter.reportError3(errMesg[3] + ": %", ast.I.spelling, ast.I.position);
    } else if (ast.T.isArrayType()) {
     if (((ArrayType) ast.T).T.isVoidType())
        reporter.reportError3(errMesg[4] + ": %", ast.I.spelling, ast.I.position);
    }
    
    
    return null;
  }

  public Object visitEmptyParaList(EmptyParaList ast, Object o) {
    return null;
  }

  // Arguments

  // Your visitor methods for arguments go here

  // Types 

  // Returns the type predefined in the standard environment. 

  public Object visitErrorType(ErrorType ast, Object o) {
    return StdEnvironment.errorType;
  }

  public Object visitBooleanType(BooleanType ast, Object o) {
    return StdEnvironment.booleanType;
  }

  public Object visitIntType(IntType ast, Object o) {
    return StdEnvironment.intType;
  }

  public Object visitFloatType(FloatType ast, Object o) {
    return StdEnvironment.floatType;
  }

  public Object visitStringType(StringType ast, Object o) {
    return StdEnvironment.stringType;
  }

  public Object visitVoidType(VoidType ast, Object o) {
	  //might have problems here
	  //reporter.reportError3(errMesg[3], "", ast.position);
    return StdEnvironment.voidType;
  }
  public Object visitArrayType(ArrayType ast, Object o) {
	     arrayFlag = 1;
	    if(ast.E instanceof EmptyExpr){
	    	//reporter.reportError3(errMesg[18] + ": %", ast.E.toString(), ast.E.position);

	    	noArraySize = 1;

	    }
	    
	    else if(!(ast.E instanceof IntExpr)){
			  reporter.reportError3(errMesg[17] + ": %", ast.E.toString(), ast.E.position);
             return null;
	    } 
	    
	    ast.E.visit(this, ast);
		if(ast.T.isBooleanType()){
			arrayTypeValue = "boolean";
		}
		if(ast.T.isFloatType()){
			arrayTypeValue = "float";
		}
		if(ast.T.isIntType()){
	
			arrayTypeValue = "int";
		}
		if(ast.T.isStringType()){
			arrayTypeValue = "String";
		}
		//arrayTypeValue = ast.T;
	
		//if(ast.T.isVoidType()){
			//reporter.reportError3(errMesg[3], "", ast.T.position);
		// }
	    
	    
		return ast;
  }

  // Literals, Identifiers and Operators

  public Object visitIdent(Ident I, Object o) {
    Decl binding = idTable.retrieve(I.spelling);
    if (binding != null){
      I.decl = binding;
      return binding;
    }
    else {
    	reporter.reportError3(errMesg[5] + ": %", I.spelling, I.position);
        return null;
    }
    
  }

  public Object visitBooleanLiteral(BooleanLiteral SL, Object o) {
    return StdEnvironment.booleanType;
  }

  public Object visitIntLiteral(IntLiteral IL, Object o) {
    return StdEnvironment.intType;
  }

  public Object visitFloatLiteral(FloatLiteral IL, Object o) {
    return StdEnvironment.floatType;
  }

  public Object visitStringLiteral(StringLiteral IL, Object o) {
    return StdEnvironment.stringType;
  }

  public Object visitOperator(Operator O, Object o) {
	
    return O;
  }

  // Creates a small AST to represent the "declaration" of each built-in
  // function, and enters it in the symbol table.

  private FuncDecl declareStdFunc (Type resultType, String id, List pl) {

    FuncDecl binding;

    binding = new FuncDecl(resultType, new Ident(id, dummyPos), pl, 
           new EmptyStmt(dummyPos), dummyPos);
    idTable.insert (id, binding);
    return binding;
  }

  // Creates small ASTs to represent "declarations" of all 
  // build-in functions.
  // Inserts these "declarations" into the symbol table.

  private final static Ident dummyI = new Ident("x", dummyPos);

  private void establishStdEnvironment () {

    // Define four primitive types
    // errorType is assigned to ill-typed expressions

    StdEnvironment.booleanType = new BooleanType(dummyPos);
    StdEnvironment.intType = new IntType(dummyPos);
    StdEnvironment.floatType = new FloatType(dummyPos);
    StdEnvironment.stringType = new StringType(dummyPos);
    StdEnvironment.voidType = new VoidType(dummyPos);
    StdEnvironment.errorType = new ErrorType(dummyPos);

    // enter into the declarations for built-in functions into the table

    StdEnvironment.getIntDecl = declareStdFunc( StdEnvironment.intType,
	"getInt", new EmptyParaList(dummyPos)); 
    StdEnvironment.putIntDecl = declareStdFunc( StdEnvironment.voidType,
	"putInt", new ParaList(
	new ParaDecl(StdEnvironment.intType, dummyI, dummyPos),
	new EmptyParaList(dummyPos), dummyPos)); 
    StdEnvironment.putIntLnDecl = declareStdFunc( StdEnvironment.voidType,
	"putIntLn", new ParaList(
	new ParaDecl(StdEnvironment.intType, dummyI, dummyPos),
	new EmptyParaList(dummyPos), dummyPos)); 
    StdEnvironment.getFloatDecl = declareStdFunc( StdEnvironment.floatType,
	"getFloat", new EmptyParaList(dummyPos)); 
    StdEnvironment.putFloatDecl = declareStdFunc( StdEnvironment.voidType,
	"putFloat", new ParaList(
	new ParaDecl(StdEnvironment.floatType, dummyI, dummyPos),
	new EmptyParaList(dummyPos), dummyPos)); 
    StdEnvironment.putFloatLnDecl = declareStdFunc( StdEnvironment.voidType,
	"putFloatLn", new ParaList(
	new ParaDecl(StdEnvironment.floatType, dummyI, dummyPos),
	new EmptyParaList(dummyPos), dummyPos)); 
    StdEnvironment.putBoolDecl = declareStdFunc( StdEnvironment.voidType,
	"putBool", new ParaList(
	new ParaDecl(StdEnvironment.booleanType, dummyI, dummyPos),
	new EmptyParaList(dummyPos), dummyPos)); 
    StdEnvironment.putBoolLnDecl = declareStdFunc( StdEnvironment.voidType,
	"putBoolLn", new ParaList(
	new ParaDecl(StdEnvironment.booleanType, dummyI, dummyPos),
	new EmptyParaList(dummyPos), dummyPos)); 

    StdEnvironment.putStringLnDecl = declareStdFunc( StdEnvironment.voidType,
	"putStringLn", new ParaList(
	new ParaDecl(StdEnvironment.stringType, dummyI, dummyPos),
	new EmptyParaList(dummyPos), dummyPos)); 

    StdEnvironment.putStringDecl = declareStdFunc( StdEnvironment.voidType,
	"putString", new ParaList(
	new ParaDecl(StdEnvironment.stringType, dummyI, dummyPos),
	new EmptyParaList(dummyPos), dummyPos)); 

    StdEnvironment.putLnDecl = declareStdFunc( StdEnvironment.voidType,
	"putLn", new EmptyParaList(dummyPos));

  }

@Override
public Object visitEmptyExprList(EmptyExprList ast, Object o) {
	
	return null;
}

@Override
public Object visitEmptyArgList(EmptyArgList ast, Object o) {

	return null;
}




@Override
public Object visitIfStmt(IfStmt ast, Object o) {
	Type t;
	Operator op;
	ast.S1.visit(this, ast);
	ast.S2.visit(this, ast);
	if((ast.E instanceof UnaryExpr)){
		t = (Type) ast.E.visit(this,ast);
		if(!t.isBooleanType()){
			reporter.reportError3(errMesg[20] + ": %", ast.E.toString(), ast.E.getPosition());

		}
	}
	else if(ast.E instanceof BinaryExpr){
		   op = (Operator) ast.E.visit(this, ast);
		   if(!op.spelling.equals("!=") && !op.spelling.equals("==") && !op.spelling.equals(">") && !op.spelling.equals("<") && !op.spelling.equals(">=") && !op.spelling.equals("<=")){
			   reporter.reportError3(errMesg[20] + ": %", ast.E.toString(), ast.E.getPosition());
			
			  

		   }
		   
		}
	
	else{
	if(!(ast.E instanceof BooleanExpr)){
		reporter.reportError3(errMesg[20] + ": %", ast.E.toString(), ast.E.getPosition());

	}
	}

	return null;
}

@Override
public Object visitWhileStmt(WhileStmt ast, Object o) {
	Type t;
	Operator op = null;

	if((ast.E instanceof UnaryExpr)){
		t = (Type) ast.E.visit(this, ast);
		if(!t.isBooleanType()){
			reporter.reportError3(errMesg[22] + ": %", ast.E.toString(), ast.E.getPosition());

		}
	}
	else if(ast.E instanceof BinaryExpr){
	   op = (Operator) ast.E.visit(this, ast);
	   if(!op.spelling.equals("!=") && !op.spelling.equals("==") && !op.spelling.equals(">") && !op.spelling.equals("<") && !op.spelling.equals(">=") && !op.spelling.equals("<=")){
		   reporter.reportError3(errMesg[22] + ": %", ast.E.toString(), ast.E.getPosition());
		

	   }
	   
	}
	else{
		if(!(ast.E instanceof BooleanExpr)){
			reporter.reportError3(errMesg[22] + ": %", ast.E.toString(), ast.E.getPosition());
	
		 }
	}
	bcFlag++;
	ast.S.visit(this, ast);
	bcFlag--;
	return null;
}

@Override
public Object visitForStmt(ForStmt ast, Object o) {
	ast.E1.visit(this,ast);
	ast.E2.visit(this, ast);
	Type t;
	Operator op = null;

	if((ast.E2 instanceof UnaryExpr)){
		t = (Type) ast.E2.visit(this, ast);
		if(!t.isBooleanType()){
			reporter.reportError3(errMesg[21] + ": %", ast.E2.toString(), ast.E2.getPosition());

		}
	}
	else if(ast.E2 instanceof BinaryExpr){
	   op = (Operator) ast.E2.visit(this, ast);
	   if(!op.spelling.equals("!=") && !op.spelling.equals("==") && !op.spelling.equals(">") && !op.spelling.equals("<") && !op.spelling.equals(">=") && !op.spelling.equals("<=")){
		   reporter.reportError3(errMesg[21] + ": %", ast.E2.toString(), ast.E2.getPosition());
		

	   }
	   
	}
	else{
		if(!(ast.E2 instanceof BooleanExpr)){
			reporter.reportError3(errMesg[21] + ": %", ast.E2.toString(), ast.E2.getPosition());
	
		 }
	}
	ast.E3.visit(this, ast);
	bcFlag++;
	ast.S.visit(this, ast);
	bcFlag--;
	return null;
}

@Override
public Object visitBreakStmt(BreakStmt ast, Object o) {
	if(bcFlag <= 0){
		
			reporter.reportError3(errMesg[23] + ": %", ast.toString(), ast.getPosition());
	}
	return null;
}

@Override
public Object visitContinueStmt(ContinueStmt ast, Object o) {
	if(bcFlag <= 0){
		
		reporter.reportError3(errMesg[24] + ": %", ast.toString(), ast.getPosition());
     }

	return null;
}

@Override
public Object visitReturnStmt(ReturnStmt ast, Object o) {
	Type t = null;
	t = (Type) ast.E.visit(this, ast);
	if(t.isIntType()){
		 Operator op = new Operator("i2f", dummyPos);
	     UnaryExpr eAST = new UnaryExpr(op, ast.E, dummyPos);
	     eAST.type = StdEnvironment.floatType;
	     ast.E = eAST;
	}
	return t;
}

@Override
public Object visitEmptyCompStmt(EmptyCompStmt ast, Object o) {
	
	return null;
}






@Override
public Object visitUnaryExpr(UnaryExpr ast, Object o) {
	Type t = null;
	ast.O.visit(this, ast);
	t = (Type) ast.E.visit(this, ast);
	if(t!= null){
	if(ast.O.spelling.equals("!")){
		if(!(t instanceof BooleanType)){
		    reporter.reportError3(errMesg[10] + ": %", ast.E.toString(), ast.E.getPosition());

		}
	}
	
	   return t;
	}
	else{
		return null;
	}
}

@Override
public Object visitBinaryExpr(BinaryExpr ast, Object o) {
	Type t1 = null;
	Type t2 = null;
	
	Operator bo = null;
//	if(ast.E1 instanceof CallExpr || ast.E2 instanceof CallExpr){
//	    reporter.reportError3(errMesg[11] + ": %", ast.E1.toString(), ast.E1.getPosition());
//	}
	
	t1 = (Type) ast.E1.visit(this, ast);
	bo = (Operator) ast.O.visit(this,ast);
	t2 = (Type) ast.E2.visit(this,ast);
	if(t1 != null && t2 != null){
	if(t1.assignable(t2) != true){
	    reporter.reportError3(errMesg[9] + ": %", ast.E1.toString(), ast.E1.getPosition());
        if(t1.isBooleanType() && t2.isBooleanType()){
        	ast.O.spelling = "i" + ast.O.spelling;
        }
        ast.O.spelling = "i" + ast.O.spelling; 
        if(t1.isFloatType() || t2.isFloatType()){
        	ast.O.spelling = "f" + ast.O.spelling;
        }
        
	    if(t1.isIntType() && t2.isFloatType()){
	    	 Operator op = new Operator("i2f", dummyPos);
	         UnaryExpr eAST = new UnaryExpr(op, ast.E1, dummyPos);
	         eAST.type = StdEnvironment.floatType;
	         ast.E1 = eAST;
	    }
	    if(t1.isFloatType() && t2.isIntType()){		
	   	 Operator op = new Operator("i2f", dummyPos);
	     UnaryExpr eAST = new UnaryExpr(op, ast.E2, dummyPos);
	     eAST.type = StdEnvironment.floatType;
	     ast.E2 = eAST;
	    	
	    }
	    
	  }
	}
	
    if(bo!=null){
     
	return bo;
    }
    else{
    	return null;
    }
}

@Override
public Object visitInitExpr(InitExpr ast, Object o) {
    ast.IL.visit(this, ast);
	return null;
}


@Override
public Object visitExprList(ExprList ast, Object o) {
	Type t = null;
	String st = null;
	t = (Type) ast.E.visit(this, ast);
	
	
	if(t!=null){
		if(t.isBooleanType()){
			st = "boolean";
		}
		if(t.isFloatType()){
			st = "float";
		}
		if(t.isIntType()){
			st = "int";
		}
		if(t.isStringType()){
			st = "String";
		}
		if(arrayFlag == 1){
			arrayArgTypes.add(st);
		}
		
	}
	if(!(ast.EL instanceof EmptyExprList )){
	   t=(Type) ast.EL.visit(this, ast);
	}
	return null;
}

@Override
public Object visitArrayExpr(ArrayExpr ast, Object o) {
	arrayExpr = true;
	ast.E.visit(this, ast);
    
	ast.V.visit(this,ast);
	arrayExpr = false;
	return null;
}

@Override
public Object visitCallExpr(CallExpr ast, Object o) {
	callE = true;
	ArrayList a = null;
	argTypes.clear();
	ast.I.visit(this,ast);

	Decl check = null;
	//IdEntry i = null;
	check = idTable.retrieve(ast.I.spelling);
	
	
	if(check != null){
		ast.AL.visit(this, ast);
	    //check = i.attr;
		if(!(check instanceof FuncDecl)){
			if((check.T.isIntType() || check.T.isFloatType()) || check.T instanceof ArrayType){ //find out what is scalar type
				reporter.reportError3(errMesg[19] + ": %", ast.I.toString(), ast.I.getPosition());
		
			}
		}
		
		a = functionDecls.get(ast.I.spelling);
	    
		if(a != null){
			
		if(argTypes.size() > a.size()){
			reporter.reportError3(errMesg[25] + ": %", ast.I.toString(), ast.I.getPosition());
		}
		
		if(a.size() > argTypes.size()){
			reporter.reportError3(errMesg[26] + ": %", ast.I.toString(), ast.I.getPosition());
		}
		
		if(argTypes.size() == a.size()){

			if(a.equals(argTypes) == false){
				reporter.reportError3(errMesg[27] + ": %", ast.I.toString(), ast.I.getPosition());
	
			}
			
		}
		}
	}
	callE=false;
	return null;
}

public Object visitAssignExpr(AssignExpr ast, Object o) {
	Type t2=null;
	Type t1 = null;
	if(ast.E1 instanceof CallExpr){
		reporter.reportError3(errMesg[11] + ": %", ast.E1.toString(), ast.E1.getPosition());
	}
	else if(ast.E1 instanceof VarExpr && !(ast.E2 instanceof BinaryExpr)){
		t1 = (Type) ast.E1.visit(this, ast);
		t2 = (Type) ast.E2.visit(this, ast);
		if(t1 != null && t2 != null){
			if(t1.assignable(t2) != true){
			    reporter.reportError3(errMesg[6] + ": %", ast.E1.toString(), ast.E1.getPosition());
			}
			if(t2.isIntType()){
				 Operator op = new Operator("i2f", dummyPos);
			     UnaryExpr eAST = new UnaryExpr(op, ast.E2, dummyPos);
			     eAST.type = StdEnvironment.floatType;
			     ast.E2 = eAST;
			}
			
			
		}
	}
	else if(ast.E1 instanceof VarExpr && (ast.E2 instanceof BinaryExpr)){
		;
	}
	else{
		reporter.reportError3(errMesg[7] + ": %", ast.E1.toString(), ast.E1.getPosition());
	}
	
	

	return null;
}

@Override
public Object visitArgList(ArgList ast, Object o) {
	ast.A.visit(this,ast);
	ast.AL.visit(this, ast);
	return null;
}

@Override
public Object visitArg(Arg ast, Object o) {
	Type t = null;
	
	t = (Type) ast.E.visit(this, ast);
	if(t.isArrayType()){
		argTypes.add(1);
	}
	else{
	argTypes.add(t);
	}
	
	if(t.isIntType()){
		 Operator op = new Operator("i2f", dummyPos);
	     UnaryExpr eAST = new UnaryExpr(op, ast.E, dummyPos);
	     eAST.type = StdEnvironment.floatType;
	     ast.E = eAST;
	}
	return null;
}

@Override
public Object visitSimpleVar(SimpleVar ast, Object o) {
	Decl d = null;
	Decl check = null;
	d = (Decl) ast.I.visit(this, ast);

	
	check = idTable.retrieve(ast.I.spelling);
	
    if(check!= null){
   
		
		if(arrayExpr == true){
			if(check instanceof FuncDecl || !check.T.isArrayType() ){
				reporter.reportError3(errMesg[12] + ": %", ast.I.spelling, ast.I.getPosition());
			}
		}else{
			if((check instanceof FuncDecl || check.T.isArrayType()) && callE == false){
				  
							reporter.reportError3(errMesg[11] + ": %", ast.I.spelling, ast.I.getPosition());

				
		}
			
		}
		
		    return d.T;
		
	    }
    
    return null;	
    
   
	
	
}


}
