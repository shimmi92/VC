/*
 * Parser.java            
 *
 * This parser for a subset of the VC language is intended to 
 *  demonstrate how to create the AST nodes, including (among others): 
 *  [1] a list (of statements)
 *  [2] a function
 *  [3] a statement (which is an expression statement), 
 *  [4] a unary expression
 *  [5] a binary expression
 *  [6] terminals (identifiers, integer literals and operators)
 *
 * In addition, it also demonstrates how to use the two methods start 
 * and finish to determine the position information for the start and 
 * end of a construct (known as a phrase) corresponding an AST node.
 *
 * NOTE THAT THE POSITION INFORMATION WILL NOT BE MARKED. HOWEVER, IT CAN BE
 * USEFUL TO DEBUG YOUR IMPLEMENTATION.
 *
 * (07-April-2014)


program       -> func-decl
func-decl     -> type identifier "(" ")" compound-stmt
type          -> void
identifier    -> ID
// statements
compound-stmt -> "{" stmt* "}" 
stmt          -> expr-stmt
expr-stmt     -> expr? ";"
// expressions 
expr                -> additive-expr
additive-expr       -> multiplicative-expr
                    |  additive-expr "+" multiplicative-expr
                    |  additive-expr "-" multiplicative-expr
multiplicative-expr -> unary-expr
	            |  multiplicative-expr "*" unary-expr
	            |  multiplicative-expr "/" unary-expr
unary-expr          -> "-" unary-expr
		    |  primary-expr

primary-expr        -> identifier
 		    |  INTLITERAL
		    | "(" expr ")"
 */

package VC.Parser;

//import VC.Recogniser.SyntaxError;
import VC.Scanner.Scanner;
import VC.Scanner.SourcePosition;
import VC.Scanner.Token;
import VC.ErrorReporter;
import VC.ASTs.*;

public class Parser {

  private Scanner scanner;
  private ErrorReporter errorReporter;
  private Token currentToken;
  private SourcePosition previousTokenPosition;
  private SourcePosition dummyPos = new SourcePosition();

  Type prevType = null;
  Type tAST = null;
 Type prevTypel = null;
 Type tASTl = null;
  int varCalledFromProgram;
  public Parser (Scanner lexer, ErrorReporter reporter) {
    scanner = lexer;
    errorReporter = reporter;
    
    previousTokenPosition = new SourcePosition();
    currentToken = scanner.getToken();
  }

// match checks to see f the current token matches tokenExpected.
// If so, fetches the next token.
// If not, reports a syntactic error.

  void match(int tokenExpected) throws SyntaxError {
    if (currentToken.kind == tokenExpected) {
      previousTokenPosition = currentToken.position;
      currentToken = scanner.getToken();
    } else {
      syntacticError("\"%\" expected here", Token.spell(tokenExpected));
    }
  }

  void accept() {
    previousTokenPosition = currentToken.position;
    currentToken = scanner.getToken();
  }

  void syntacticError(String messageTemplate, String tokenQuoted) throws SyntaxError {
    SourcePosition pos = currentToken.position;
    errorReporter.numErrors++;
    errorReporter.reportError3(messageTemplate, tokenQuoted, pos);
   // throw(new SyntaxError());
  }

// start records the position of the start of a phrase.
// This is defined to be the position of the first
// character of the first token of the phrase.

  void start(SourcePosition position) {
    position.lineStart = currentToken.position.lineStart;
    position.charStart = currentToken.position.charStart;
  }

// finish records the position of the end of a phrase.
// This is defined to be the position of the last
// character of the last token of the phrase.

  void finish(SourcePosition position) {
    position.lineFinish = previousTokenPosition.lineFinish;
    position.charFinish = previousTokenPosition.charFinish;
  }

  void copyStart(SourcePosition from, SourcePosition to) {
    to.lineStart = from.lineStart;
    to.charStart = from.charStart;
  }

// ========================== PROGRAMS ========================

  public Program parseProgram() {

    Program programAST = null;
    
    SourcePosition programPos = new SourcePosition();
    start(programPos);

    try {
    
     
      List dlAST = parseDeclList();
      finish(programPos);
      programAST = new Program(dlAST, programPos); 
      if (currentToken.kind != Token.EOF) {
    	//programAST = new Program(new EmptyDeclList(dummyPos),programPos);
        syntacticError("\"%\" unknown type", currentToken.spelling);
      }
      //ystem.out.println(errorReporter.numErrors);
    }
    
    catch (SyntaxError s) { return null;     
    }
    return programAST;
  }

// ========================== DECLARATIONS ========================

  List parseDeclList() throws SyntaxError {
	  
	
    List dlAST = new EmptyDeclList(dummyPos);
    Decl f = null;
    Decl v = null;
   
    SourcePosition funcPos = new SourcePosition();
    start(funcPos);

    if(currentToken.kind == Token.VOID || currentToken.kind == Token.BOOLEAN || currentToken.kind == Token.INT
   	     || currentToken.kind == Token.FLOAT || currentToken.kind == Token.COMMA){
		  //accept();
    	if(currentToken.kind == Token.COMMA){
    		accept();
    		
    		tAST = prevType;
    		//accept();
    	}else{
    	
    			 tAST = parseType();
    			 prevType = tAST;
    	}
		  if(currentToken.kind == Token.ID){
          Ident i =parseIdent();
		    if(currentToken.kind == Token.LPAREN){
		    	f = parseFuncDecl(tAST,i);
		    	finish(funcPos);
		    	 dlAST = parseDeclList();
		    	dlAST = new DeclList(f,dlAST,funcPos);
		    }else{
		    	varCalledFromProgram=1;
		    	
		    	SourcePosition vardPos = new SourcePosition();
		        copyStart(funcPos, vardPos);
		    	v = parseVarDecl(tAST,i,1);
		    	
		        finish(vardPos);
		        dlAST = parseDeclList();
		    	dlAST = new DeclList(v,dlAST,vardPos);
             } 	                
          }
		
     }	
   
    
    return dlAST;
  }
  List parseLocVar() throws SyntaxError{
	  List dlAST = new EmptyDeclList(dummyPos);
	    Decl f = null;
	    Decl v = null;
	 
	    SourcePosition funcPos = new SourcePosition();
	    start(funcPos);
	  
	    if(currentToken.kind == Token.VOID || currentToken.kind == Token.BOOLEAN || currentToken.kind == Token.INT
	      	     || currentToken.kind == Token.FLOAT || currentToken.kind == Token.COMMA){
	   		  //accept();
	       	if(currentToken.kind == Token.COMMA){
	       		accept();
	       		
	       		tASTl = prevTypel;
	       		//accept();
	       	} else{
	           prevTypel = tASTl = parseType();
	       	}
	   		  
	        Ident i =parseIdent();
	   		  
	   		v = parseVarDecl(tASTl,i,2);
	   		    	
	   		     
	   		 dlAST = parseLocVar();
	   		 
	         dlAST = new DeclList(v,dlAST,dummyPos);
	         
	        } 	                
	             
	        
		return dlAST;
	  
	  
  }

  Decl parseFuncDecl(Type t,Ident i) throws SyntaxError {
   	 Decl fAST = null;
   	 SourcePosition funcPos = new SourcePosition();
	   start(funcPos);
   	 
	  Type tf =  null;
			  tf = t;
   	  Ident ifunc = null;
   			 ifunc = i;
   	  List fplAST = new EmptyParaList(dummyPos);
   	  fplAST = parseParaList();
      Stmt cAST = new EmptyCompStmt(dummyPos);
      cAST = parseCompoundStmt();
      finish(funcPos);
      if(fplAST instanceof EmptyParaList){
    	  fplAST = new EmptyParaList(dummyPos);
      }
      if(cAST instanceof EmptyCompStmt){
    	  cAST = new EmptyCompStmt(dummyPos);
      }
      fAST = new FuncDecl(tf, ifunc, fplAST, cAST, funcPos);
      
    return fAST;
  }
  Decl parseVarDecl(Type t,Ident i, int flag) throws SyntaxError{
	  
	  Decl gAST = null;
	  SourcePosition varPos = new SourcePosition();
	   start(varPos);
	   
	  Type tv = null;
			tv =  t;
	  Ident iv = null;
	       iv = i;
	  Expr e = new EmptyExpr(dummyPos);
	  int fl = flag;
	 // accept();
	  if(currentToken.kind == Token.SEMICOLON){
		  e = new EmptyExpr(dummyPos);
	  }
	  else{ 
		  if(currentToken.kind == Token.LBRACKET){
			  accept();	
			  if(currentToken.kind == Token.RBRACKET){
				  e = new EmptyExpr(dummyPos);
				  accept();
			  }
			  else{
			  e = parsePrimaryExpr();
			      accept();
			  }
			  tv = new ArrayType(tv,e,varPos);
			  e = new EmptyExpr(dummyPos);
		
		  }
		
		  if(currentToken.kind == Token.EQ){
				 accept();
		     e = parseInitialiser();
		     
		   }
	  }
	  
	 
	  finish(varPos);
	  if(currentToken.kind == Token.COMMA){
		 
	  }
	  else{
		  match(Token.SEMICOLON);
	  }
	 
	  if(flag == 1){
		  
	      gAST = new GlobalVarDecl(tv,iv,e,varPos);
	  }
	  if(flag == 2){
		  gAST = new LocalVarDecl(tv,iv,e,varPos);
	  }
	
	return gAST;
	  
  }
  
  Expr parseInitDeclaratorList() throws SyntaxError {
	 
	  List pidlAST = null;
	  Expr pAST = null;
	  SourcePosition pidPos = new SourcePosition();
	  start(pidPos);
	  pAST = parseInitDeclarator();
	  finish(pidPos);

	  //pidlAST = new ExprList(pAST, new EmptyExprList(dummyPos),pidPos);
	  while(currentToken.kind == Token.COMMA){		
		  accept();
		  Expr pidAST = parseInitDeclarator();
		  //need the copy position here
		  pidlAST = new ExprList(pidAST, pidlAST,pidPos);
	  }
	  
	
	//  pAST = new InitExpr(pidlAST,pidPos);
	
	  return pAST;
	  
  }
  Expr parseInitDeclarator() throws SyntaxError {
	  SourcePosition initdecPos = new SourcePosition();
	   start(initdecPos);
	  
      Expr pidAST = null;
      
	  pidAST = parseDeclarator();
	  
	  if(currentToken.kind == Token.EQ){
		  Operator op = acceptOperator();
		  if(pidAST == null){
			  pidAST = parseInitialiser();
		
		  }
		  else{
		     Expr pid2AST = parseInitialiser();
		     finish(initdecPos);
		     pidAST = new AssignExpr(pidAST,pid2AST,initdecPos);
		  }
	  }
	  if(pidAST instanceof EmptyExpr){
		  pidAST = new EmptyExpr(dummyPos);
	  }
    
	  return pidAST;
  }
  
  Expr parseDeclarator() throws SyntaxError {
	  SourcePosition decPos = new SourcePosition();
	  start(decPos);
	  
	  Ident i = null;
	 
	  Expr exprAST = null;
	  
	  Var simVAST = null;
	  
	 
	    
	  if(varCalledFromProgram!=1){
		  i = parseIdent();
		  finish(decPos);
		  simVAST = new SimpleVar(i, decPos);
	      exprAST = new VarExpr(simVAST, decPos);
	   	      
	  }
	  varCalledFromProgram=0;
	  if(currentToken.kind == Token.LBRACKET){
		
		  accept();
		  while(currentToken.kind == Token.INTLITERAL){
		     //parseIntLiteral(); // dont need accept here, this function moves on	
			  //need copy position thing here
		     IntLiteral ilAST = parseIntLiteral();		
		     Expr ie = new IntExpr(ilAST,decPos);
		     exprAST = new ArrayExpr(simVAST,ie,decPos);
		    
		     
		  }
		  match(Token.RBRACKET);
	  }
	  if(exprAST == null){
		  exprAST = new EmptyExpr(dummyPos);
	  }
        
	  return exprAST;		     
  }
  Expr parseInitialiser() throws SyntaxError {
	  Expr iAST = null;
	  
	  List ilAST = null;
	  
	  SourcePosition initPos = new SourcePosition();
	    start(initPos);
	    
	  switch (currentToken.kind) {
      case Token.LCURLY:
        {
          accept();
          iAST=parseExpr();
          ilAST = new ExprList(iAST, new EmptyExprList(dummyPos),initPos);
          while(currentToken.kind == Token.COMMA){
        	  accept();
        	  iAST = parsePrimaryExpr();
        	  finish(initPos);
        	  ilAST = new ExprList(iAST, ilAST,initPos);
        	 
          }
          iAST = new InitExpr(ilAST,initPos);
          match(Token.RCURLY);
          
        }
        break;
      
      default:
        iAST = parsePrimaryExpr();
        
        break;
       
    }
	//finish(initPos);
	//iAST = new InitExpr(ilAST,initPos);
	return iAST;
	 
	 
  }

//  ======================== TYPES ==========================

  Type parseType() throws SyntaxError {
    Type typeAST = null;

    SourcePosition typePos = new SourcePosition();
    start(typePos);

    
   
    if(currentToken.kind == Token.VOID){
    	typeAST = new VoidType(typePos);
    }
    if(currentToken.kind == Token.BOOLEAN){
    	typeAST = new BooleanType(typePos);
    }
    if(currentToken.kind == Token.INT){
    	typeAST = new IntType(typePos);
    }
    if(currentToken.kind == Token.FLOAT){
    	typeAST = new FloatType(typePos);
    }
    accept();
    finish(typePos);

    return typeAST;
    }

// ======================= STATEMENTS ==============================

  Stmt parseCompoundStmt() throws SyntaxError {
    Stmt cAST = new EmptyCompStmt(dummyPos); 
    Decl lvAST = null;
    List slAST = new EmptyStmtList(dummyPos);
    List d = new EmptyDeclList(dummyPos);
    SourcePosition stmtPos = new SourcePosition();
    start(stmtPos);

    match(Token.LCURLY);
    
    while(currentToken.kind != Token.RCURLY){
         		    	  
        d = parseLocVar();
        slAST = parseStmtList();
       	
    }
    accept();
    
    finish(stmtPos);
    if(d instanceof EmptyDeclList && slAST instanceof EmptyStmtList ){
    	cAST = new EmptyCompStmt(dummyPos);
    }
    else{
    cAST = new CompoundStmt(d,slAST,stmtPos);
    }
    return cAST;
  }

  List parseStmtList() throws SyntaxError {
		List l = new EmptyStmtList(dummyPos);
	    Stmt s = null;
	    SourcePosition stmtlistPos = new SourcePosition();
	    start(stmtlistPos);
	    
	    while (currentToken.kind != Token.RCURLY){
	      s = parseStmt();
	      l = parseStmtList();
	      l = new StmtList(s,l,stmtlistPos);
	    }
	    if(s instanceof EmptyStmt){
	    	l = new EmptyStmtList(dummyPos);
	    }
	    
	    return l;
 }
  
  
  Stmt parseStmt() throws SyntaxError {
		
	    Stmt s = new EmptyStmt(dummyPos);
	    
	    switch (currentToken.kind) {
	    case Token.LCURLY:
	    	s = parseCompoundStmt();
	    	break;
	    case Token.CONTINUE:
	        s = parseContinueStmt();
	      break;
	    case Token.BREAK:
	        s = parseBreakStmt();
	      break;
	    case Token.IF:
	    	s = parseIfStmt();
	    	break;
	    case Token.WHILE:
	    	s = parseWhileStmt();
	    	break;
	    case Token.RETURN:
	    	s = parseReturnStmt();
	    	break;
	    case Token.FOR:
	    	s = parseForStmt();
	    	break;
	    default:
	      s = parseExprStmt();
	      break;

	    }
	    if(s instanceof EmptyStmt){
	    	s = new EmptyStmt(dummyPos);
	    }
	    
	    return s;
  }
  
  Stmt parseContinueStmt() throws SyntaxError {
		Stmt s = null;
		
	    SourcePosition constmtPos = new SourcePosition();
		start(constmtPos);
		
	    match(Token.CONTINUE);
	    match(Token.SEMICOLON);
	    
	    finish(constmtPos);
	    s = new ContinueStmt(constmtPos);
	    
	    return s;
  }
  
  Stmt parseBreakStmt() throws SyntaxError{
	  Stmt s = null;
		
	  SourcePosition breakstmtPos = new SourcePosition();
      start(breakstmtPos);
      
	  match(Token.BREAK);
	  match(Token.SEMICOLON);
	  
	  finish(breakstmtPos);
	  s = new BreakStmt(breakstmtPos);
	  
	  return s;
  }
  
  Stmt parseIfStmt() throws SyntaxError{
	  Stmt s = null;
	  Stmt s2 = null;
	  Expr e = null;
	  Stmt i = null;
	  
	  SourcePosition ifstmtPos = new SourcePosition();
      start(ifstmtPos);
      
	  match(Token.IF);
	  match(Token.LPAREN);
	  e = parseExpr();
	  match(Token.RPAREN);
	  s = parseStmt();
	  
	  
	  
	  if(currentToken.kind == Token.ELSE){
		  accept();
		  s2 =parseStmt();
		
	  }

	  if(e instanceof EmptyExpr ){
		  e = new EmptyExpr(dummyPos);
	  }
	  if(s instanceof EmptyStmt){
		  s = new EmptyStmt(dummyPos);
	  }
	  if(s2 instanceof EmptyStmt){
		  s2 = new EmptyStmt(dummyPos);
	  }
	  finish(ifstmtPos);
	  if(s2 == null){
	      i = new IfStmt(e,s,ifstmtPos);
	  }
	  else{
		  i = new IfStmt(e,s,s2,ifstmtPos);
	  }
	  return i;
	  
  }
  
  Stmt parseForStmt() throws SyntaxError{
      Stmt f = null;
      Stmt s = null;
      Expr e1 = null;
      Expr e2 = null;
      Expr e3 = null;
	  
      SourcePosition forstmtPos = new SourcePosition();
      start(forstmtPos);
      
	  match(Token.FOR);
	  match(Token.LPAREN);
	  
	  if (currentToken.kind == Token.ID
		        || currentToken.kind == Token.INTLITERAL
		        || currentToken.kind == Token.MINUS
		        || currentToken.kind == Token.PLUS       
		        || currentToken.kind == Token.LPAREN
		        || currentToken.kind == Token.BOOLEANLITERAL
		        || currentToken.kind == Token.FLOATLITERAL
		        || currentToken.kind == Token.STRINGLITERAL
		        || currentToken.kind == Token.EQ
		        || currentToken.kind == Token.OROR
		        || currentToken.kind == Token.ANDAND
		        || currentToken.kind == Token.EQEQ
		        || currentToken.kind == Token.NOTEQ
		        || currentToken.kind == Token.LT
		        || currentToken.kind == Token.LTEQ
		        || currentToken.kind == Token.GT
		        || currentToken.kind == Token.GTEQ
		        || currentToken.kind == Token.MULT
		        || currentToken.kind == Token.DIV
		        || currentToken.kind == Token.NOT) {
		        e1 = parseExpr();
		        match(Token.SEMICOLON);
	  } else {
		      match(Token.SEMICOLON);
      }
	  if (currentToken.kind == Token.ID
		        || currentToken.kind == Token.INTLITERAL
		        || currentToken.kind == Token.MINUS
		        || currentToken.kind == Token.PLUS       
		        || currentToken.kind == Token.LPAREN
		        || currentToken.kind == Token.BOOLEANLITERAL
		        || currentToken.kind == Token.FLOATLITERAL
		        || currentToken.kind == Token.STRINGLITERAL
		        || currentToken.kind == Token.EQ
		        || currentToken.kind == Token.OROR
		        || currentToken.kind == Token.ANDAND
		        || currentToken.kind == Token.EQEQ
		        || currentToken.kind == Token.NOTEQ
		        || currentToken.kind == Token.LT
		        || currentToken.kind == Token.LTEQ
		        || currentToken.kind == Token.GT
		        || currentToken.kind == Token.GTEQ
		        || currentToken.kind == Token.MULT
		        || currentToken.kind == Token.DIV
		        || currentToken.kind == Token.NOT) {
		        e2 = parseExpr();
		        match(Token.SEMICOLON);
	  } else {
		      match(Token.SEMICOLON);
	  }
	  if (currentToken.kind == Token.ID
		        || currentToken.kind == Token.INTLITERAL
		        || currentToken.kind == Token.MINUS
		        || currentToken.kind == Token.PLUS       
		        || currentToken.kind == Token.LPAREN
		        || currentToken.kind == Token.BOOLEANLITERAL
		        || currentToken.kind == Token.FLOATLITERAL
		        || currentToken.kind == Token.STRINGLITERAL
		        || currentToken.kind == Token.EQ
		        || currentToken.kind == Token.OROR
		        || currentToken.kind == Token.ANDAND
		        || currentToken.kind == Token.EQEQ
		        || currentToken.kind == Token.NOTEQ
		        || currentToken.kind == Token.LT
		        || currentToken.kind == Token.LTEQ
		        || currentToken.kind == Token.GT
		        || currentToken.kind == Token.GTEQ
		        || currentToken.kind == Token.MULT
		        || currentToken.kind == Token.DIV
		        || currentToken.kind == Token.NOT) {
		        e3 = parseExpr();
		       match(Token.RPAREN);
	  } else{
		  match(Token.RPAREN);
	  }
	  s = parseStmt();
	  
	  if(e1 == null ){
		  e1 = new EmptyExpr(dummyPos);
	  }
	  if(e2 == null ){
		  e2 = new EmptyExpr(dummyPos);
	  }
	  if(e3 == null ){
		  e3 = new EmptyExpr(dummyPos);
	  }
	  if(s instanceof EmptyStmt){
		  s = new EmptyStmt(dummyPos);
	  }
      finish(forstmtPos);
      
      f = new ForStmt(e1,e2,e3,s,forstmtPos);
      
      return f;
	  
  }
  
  Stmt parseWhileStmt() throws SyntaxError{
      Stmt s = null;
      Expr e = null;
      Stmt w = null;
      SourcePosition wstmtPos = new SourcePosition();
      start(wstmtPos);
      
	  match(Token.WHILE);
	  match(Token.LPAREN);
	  e = parseExpr();
	  match(Token.RPAREN);
	  s = parseStmt();
	  
	  if(e instanceof EmptyExpr){
		 e = new EmptyExpr(dummyPos);
	  }
	  if(s instanceof EmptyStmt){
		 s = new EmptyStmt(dummyPos);
	  }
	  finish(wstmtPos);
	  w = new WhileStmt(e,s,wstmtPos);
	  
	  return w;
	 
	  
  }
  
  Stmt parseReturnStmt() throws SyntaxError{
	  
	  Expr e = null;
	  Stmt r = null;
	  
	  SourcePosition rstmtPos = new SourcePosition();
      start(rstmtPos);
	  
	  match(Token.RETURN);
	  if (currentToken.kind == Token.ID
		        || currentToken.kind == Token.INTLITERAL
		        || currentToken.kind == Token.MINUS
		        || currentToken.kind == Token.PLUS       
		        || currentToken.kind == Token.LPAREN
		        || currentToken.kind == Token.BOOLEANLITERAL
		        || currentToken.kind == Token.FLOATLITERAL
		        || currentToken.kind == Token.STRINGLITERAL
		        || currentToken.kind == Token.EQ
		        || currentToken.kind == Token.OROR
		        || currentToken.kind == Token.ANDAND
		        || currentToken.kind == Token.EQEQ
		        || currentToken.kind == Token.NOTEQ
		        || currentToken.kind == Token.LT
		        || currentToken.kind == Token.LTEQ
		        || currentToken.kind == Token.GT
		        || currentToken.kind == Token.GTEQ
		        || currentToken.kind == Token.MULT
		        || currentToken.kind == Token.DIV
		        || currentToken.kind == Token.NOT) {
		        e = parseExpr();
		        match(Token.SEMICOLON);
	  } else {
		      match(Token.SEMICOLON);
	  }
	  
	  if(e instanceof EmptyExpr){
		  e = new EmptyExpr(dummyPos);
	  }
	  finish(rstmtPos);
	  
	  r = new ReturnStmt(e,rstmtPos);
	  
	  return r;
  }
  
  
  Stmt parseExprStmt() throws SyntaxError {
		Expr e = new EmptyExpr(dummyPos);
		Stmt s = new EmptyStmt(dummyPos);
		SourcePosition estmtPos = new SourcePosition();
	      start(estmtPos);
		
	    if (currentToken.kind == Token.ID
	        || currentToken.kind == Token.INTLITERAL
	        || currentToken.kind == Token.MINUS
	        || currentToken.kind == Token.PLUS       
	        || currentToken.kind == Token.LPAREN
	        || currentToken.kind == Token.BOOLEANLITERAL
	        || currentToken.kind == Token.FLOATLITERAL
	        || currentToken.kind == Token.STRINGLITERAL
	        || currentToken.kind == Token.EQ
	        || currentToken.kind == Token.OROR
	        || currentToken.kind == Token.ANDAND
	        || currentToken.kind == Token.EQEQ
	        || currentToken.kind == Token.NOTEQ
	        || currentToken.kind == Token.LT
	        || currentToken.kind == Token.LTEQ
	        || currentToken.kind == Token.GT
	        || currentToken.kind == Token.GTEQ
	        || currentToken.kind == Token.MULT
	        || currentToken.kind == Token.DIV
	        || currentToken.kind == Token.NOT) {


	        e = parseExpr();
	        
	    
	    } 
	       match(Token.SEMICOLON);
	       
	       if(e instanceof EmptyExpr){
	    	   e = new EmptyExpr(dummyPos);
	       }
	       
	       finish(estmtPos);
	       s = new ExprStmt(e,estmtPos);
	  
	      return s;
	  }
  


 //======================= EXPRESSIONS ======================
//fixing expressions now

  //fixing expressions now

  Expr parseExpr() throws SyntaxError {
    Expr exprAST = null;
    
    
    exprAST = parseAssignExpr();
    
    if(exprAST instanceof EmptyExpr){
    	exprAST = new EmptyExpr(dummyPos);
    }
    return exprAST;
  }
  
  
  Expr parseAssignExpr() throws SyntaxError {
	   Expr aeAST = null;
	  // Expr e = null;
	   SourcePosition assignPos = new SourcePosition();
	   start(assignPos);
	    
	   aeAST = parseCOE();
	   
       if(currentToken.kind == Token.EQ){
         	Operator op = acceptOperator();
 	        Expr ae2AST = parseAssignExpr();
 	        		//parseCOE();
 	        		//parseAssignExpr();
 	        		//
 	        SourcePosition assign2Pos = new SourcePosition();
 	        copyStart(assignPos, assign2Pos);
 	        
 	       // aeAST = parseAssignExpr();
 	        aeAST = new AssignExpr(aeAST,ae2AST,assign2Pos);
 	        
        }
       else{
    	   
    	//   aeAST = new AssignExpr(aeAST,new EmptyExpr(dummyPos),assignPos);
       }
       
       return aeAST;

  }

 
  Expr parseCOE() throws SyntaxError{
	  SourcePosition coePos = new SourcePosition();
	    start(coePos);
	  Expr coeAST = null;
	   
	 coeAST = parseCAE();
	
     while(currentToken.kind == Token.OROR){
     	  Operator op = acceptOperator();
     	  Expr coe2AST = parseCAE();
     			  
     			  //parseCOE();
     	 SourcePosition coe2Pos = new SourcePosition();
         copyStart(coePos, coe2Pos);
         finish(coe2Pos);
         coeAST = new BinaryExpr(coeAST,op,coe2AST,coe2Pos);
     }
    
     return coeAST;
   }
  
  Expr parseCAE() throws SyntaxError{
	  
	  SourcePosition caePos = new SourcePosition();
	  start(caePos);
	  Expr caeAST = null;

 	   caeAST = parseEqualityExpr();

 	  while(currentToken.kind == Token.ANDAND){
	    Operator op =  acceptOperator();
      	Expr cae2AST = parseEqualityExpr();
      			//parseCAE();
      			//
      	SourcePosition cae2Pos = new SourcePosition();
        copyStart(caePos, cae2Pos);
        finish(cae2Pos);
        caeAST = new BinaryExpr(caeAST,op,cae2AST,cae2Pos);
       }
       
       return caeAST;
       
  }
  Expr parseEqualityExpr() throws SyntaxError{
	  
	  SourcePosition eePos = new SourcePosition();
	  start(eePos);
	  Expr eeAST = null;

 	  eeAST = parseRelExpr();
 	  
 	 while(currentToken.kind == Token.EQEQ || currentToken.kind == Token.NOTEQ){
	     Operator op = acceptOperator();
	     Expr ee2AST = parseRelExpr();
	    		 //parseEqualityExpr();
	    		 //
	     SourcePosition ee2Pos = new SourcePosition();
	       copyStart(eePos, ee2Pos);
	        finish(ee2Pos); 
	      eeAST = new BinaryExpr(eeAST,op,ee2AST,ee2Pos);
	        
      }
 	 /*
      if(currentToken.kind == Token.NOTEQ){
	     Operator op = acceptOperator();
	     Expr ee2AST = parseEqualityExpr();
	    		// parseRelExpr();
	     SourcePosition ee2Pos = new SourcePosition();
	       copyStart(eePos, ee2Pos);
	        finish(ee2Pos); 
	      eeAST = new BinaryExpr(ee2AST,op,eeAST,ee2Pos);
	        
     }
      */
     return eeAST;
  }    
      
  Expr parseRelExpr() throws SyntaxError{
	  
	  
	  SourcePosition rPos = new SourcePosition();
	  start(rPos);
	  Expr rAST = null;
	  
	  
	  rAST = parseAdditiveExpr();
	  while(currentToken.kind == Token.LT || currentToken.kind == Token.LTEQ || currentToken.kind == Token.GT || currentToken.kind == Token.GTEQ){
		  Operator op = acceptOperator();
		  Expr r2AST = parseAdditiveExpr();
				  //parseRelExpr();
				//  
		  SourcePosition r2Pos = new SourcePosition();
	       copyStart(rPos, r2Pos);
	        finish(r2Pos);
		  rAST = new BinaryExpr(rAST,op,r2AST,r2Pos);
	  }
	 
      return rAST;
	  
  }  
  Expr parseAdditiveExpr() throws SyntaxError {
	  Expr aeAST = null;
	  SourcePosition aePos = new SourcePosition();
	  start(aePos);
	  
	  aeAST =  parseMultiplicativeExpr();
	  while(currentToken.kind == Token.PLUS || currentToken.kind == Token.MINUS) {
	    Operator op = acceptOperator();
	    Expr ae2AST = parseMultiplicativeExpr();
	    SourcePosition ae2Pos = new SourcePosition();
	    copyStart(aePos, ae2Pos);
	    finish(ae2Pos);
	   aeAST = new BinaryExpr(aeAST,op,ae2AST,ae2Pos);
	
	  }
	 
	  return aeAST;
}
  
  
  Expr parseMultiplicativeExpr() throws SyntaxError {
	  SourcePosition mePos = new SourcePosition();
	  start(mePos);
	  
	  Expr meAST = null;
	    meAST = parseUnaryExpr();
	    
	    if(currentToken.kind == Token.MULT || currentToken.kind == Token.DIV) {
	      Operator op = acceptOperator();
	      
	      Expr me2AST = parseUnaryExpr();
	    		
	      SourcePosition me2Pos = new SourcePosition();
		  copyStart(mePos, me2Pos);
		  finish(me2Pos);
          meAST = new BinaryExpr(meAST,op,me2AST,me2Pos);
	      
	      
	      
	    }

	    return meAST;
	 }

	  Expr parseUnaryExpr() throws SyntaxError {
		  SourcePosition uePos = new SourcePosition();
		  start(uePos);
		  Expr uAST = null;
		  Operator op;       
          Expr u2AST;
		  
		  switch (currentToken.kind) {
	      case Token.MINUS:        
	           op = acceptOperator();       
	           u2AST = parseUnaryExpr(); 
	          finish(uePos);
	          uAST = new UnaryExpr(op,u2AST,uePos);
	          break;
	      case Token.PLUS:
	    	  op = acceptOperator();       
	          u2AST = parseUnaryExpr(); 
	          finish(uePos);
	          uAST = new UnaryExpr(op,u2AST,uePos);
	          break;
	     case Token.NOT:      
	    	 op = acceptOperator();       
	          u2AST = parseUnaryExpr(); 
	          finish(uePos);
	          uAST = new UnaryExpr(op,u2AST,uePos);
	          break;
	      default:
	        uAST = parsePrimaryExpr();
	               break;	       
	               
	       }
		  return uAST;
	  }

	  Expr parsePrimaryExpr() throws SyntaxError {
		  SourcePosition pePos = new SourcePosition();
		  start(pePos);
		  Expr peAST = new EmptyExpr(dummyPos);
		  Expr pe2AST = null;
		  switch (currentToken.kind) {

	      case Token.ID:
	        Ident i = parseIdent();
	        if(currentToken.kind == Token.LBRACKET){
	        	accept();
	        	pe2AST = parseExpr();
	        	match(Token.RBRACKET); 
	        	finish(pePos);
	        	Var v = new SimpleVar(i, pePos);
	        	peAST = new ArrayExpr(v,pe2AST,pePos);
	        }
	        else if(currentToken.kind == Token.LPAREN){
	        	List l =  parseArgList();
	        	finish(pePos);
	        	peAST = new CallExpr(i,l,pePos);
	        }
	        else{
	        	Var v = new SimpleVar(i, pePos);
	        	peAST = new VarExpr(v,pePos);
	        }
	        
	      break;

	      case Token.LPAREN:
	        {
	             accept();
	             peAST = parseExpr();
		         match(Token.RPAREN);
	        }
	        break;

	      case Token.INTLITERAL:
	    	  IntLiteral ilAST = parseIntLiteral();
	          finish(pePos);
	          peAST = new IntExpr(ilAST, pePos);
	        break;
	      case Token.FLOATLITERAL:
	          FloatLiteral f = parseFloatLiteral();
	          finish(pePos);
	          peAST = new FloatExpr(f,pePos);
	          
	          break;
	      case Token.BOOLEANLITERAL:
	          BooleanLiteral b = parseBooleanLiteral();
	          finish(pePos);
	          peAST = new BooleanExpr(b,pePos);
	          break;
	      case Token.STRINGLITERAL:
	          StringLiteral s = parseStringLiteral();
	          finish(pePos);
	          peAST = new StringExpr(s,pePos);
	          break;
	      
	      default:
	        syntacticError("illegal parimary expression", currentToken.spelling);
	       peAST = new EmptyExpr(dummyPos);
	    }
		  return peAST;
	  }

 



// ========================== ID, OPERATOR and LITERALS ========================

  Ident parseIdent() throws SyntaxError {

    Ident I = null; 

    if (currentToken.kind == Token.ID) {
      previousTokenPosition = currentToken.position;
      String spelling = currentToken.spelling;
      I = new Ident(spelling, previousTokenPosition);
      currentToken = scanner.getToken();
    } else 
      syntacticError("identifier expected here", "");
    return I;
  }

// acceptOperator parses an operator, and constructs a leaf AST for it

  Operator acceptOperator() throws SyntaxError {
    Operator O = null;

    previousTokenPosition = currentToken.position;
    String spelling = currentToken.spelling;
    O = new Operator(spelling, previousTokenPosition);
    currentToken = scanner.getToken();
    return O;
  }


  IntLiteral parseIntLiteral() throws SyntaxError {
    IntLiteral IL = null;

    if (currentToken.kind == Token.INTLITERAL) {
      String spelling = currentToken.spelling;
      accept();
      IL = new IntLiteral(spelling, previousTokenPosition);
    } else 
      syntacticError("integer literal expected here", "");
    return IL;
  }

  FloatLiteral parseFloatLiteral() throws SyntaxError {
    FloatLiteral FL = null;

    if (currentToken.kind == Token.FLOATLITERAL) {
      String spelling = currentToken.spelling;
      accept();
      FL = new FloatLiteral(spelling, previousTokenPosition);
    } else 
      syntacticError("float literal expected here", "");
    return FL;
  }

  BooleanLiteral parseBooleanLiteral() throws SyntaxError {
    BooleanLiteral BL = null;

    if (currentToken.kind == Token.BOOLEANLITERAL) {
      String spelling = currentToken.spelling;
      accept();
      BL = new BooleanLiteral(spelling, previousTokenPosition);
    } else 
      syntacticError("boolean literal expected here", "");
    return BL;
  }
  StringLiteral parseStringLiteral() throws SyntaxError {
	   StringLiteral SL = null;

	    if (currentToken.kind == Token.STRINGLITERAL) {
	      String spelling = currentToken.spelling;
	      accept();
	      SL = new StringLiteral(spelling, previousTokenPosition);
	    } else 
	      syntacticError("string literal expected here", "");
	    return SL;
	  }




//===========================Parameters=====================

List parseParaList() throws SyntaxError {
	  match(Token.LPAREN);
	  List l = null;
      if(currentToken.kind != Token.RPAREN){
		 	 l = parseProperParaList();
	  }
		 
	  accept();
	  if(l == null){
		  l = new EmptyParaList(dummyPos);
	  }
	  
	  return l;
	  
}
List parseProperParaList() throws SyntaxError {
	  List l = new EmptyParaList(dummyPos);
	  SourcePosition plpos = new SourcePosition();
	  start(plpos);
	  Decl p =parseParaDecl();
	  
	//  l = new DeclList(p,new EmptyDeclList(dummyPos),plpos);
	  if(currentToken.kind == Token.COMMA){
			  accept();
			  // have a copy position thing here
		      l = parseProperParaList();
		      l = new ParaList((ParaDecl) p,l,dummyPos);
	  }
	  else{
		  l = new ParaList((ParaDecl) p,new EmptyParaList(dummyPos),dummyPos);
	  }
	  if(p == null){
		  l = new EmptyParaList(dummyPos);
	  }
	  return l;
	
}
Decl parseParaDecl() throws SyntaxError {
	  ParaDecl p =  null;
	  Expr e = null;
	  Ident i = null;
	  Type t = null;
	  Var simVAST = null;
	  SourcePosition pdpos = new SourcePosition();
	  start(pdpos);
	 	  if(currentToken.kind == Token.VOID || currentToken.kind == Token.BOOLEAN || currentToken.kind == Token.INT
	    	     || currentToken.kind == Token.FLOAT){
	 		     t = parseType();
	    		 // accept();
	 		     i = parseIdent();
	 		  
	 				
	 		     if(currentToken.kind == Token.LBRACKET){
	 		    	 accept();	
	 				  if(currentToken.kind == Token.RBRACKET){
	 					  e = new EmptyExpr(dummyPos);
	 					  accept();
	 				  }
	 				  else{
	 				  e = parsePrimaryExpr();
	 				      accept();
	 				  }
	 				 
	 				  e = new EmptyExpr(dummyPos);
	 	 			
	 					  t = new ArrayType(t,e,dummyPos);
	 				
	 		      }
	 			 
	 			   p = new ParaDecl(t,i,dummyPos);
	  }
	  else{
				  syntacticError("\"%\" wrong starting", currentToken.spelling);
	  }
	 	  
	 	  
	  return p;
	  
}
List parseArgList() throws SyntaxError {
	  List alAST = new EmptyArgList(dummyPos);
	  match(Token.LPAREN);
	  
	  while(currentToken.kind != Token.RPAREN){
		  alAST=parseProperArgList();
	
	  }
	  
	  accept();
	  if(alAST instanceof EmptyArgList){
		  alAST = new EmptyArgList(dummyPos);
	  }
	  
	 return alAST;
}

List parseProperArgList() throws SyntaxError {
	  Arg aAST = null;
	  List palAST = null; 
	  
	  SourcePosition properargpos = new SourcePosition();
	  start(properargpos);
	  
	  aAST =  parseArg();
	  if(currentToken.kind == Token.COMMA){
		  accept();
		  palAST = parseProperArgList();	  
          finish(properargpos);
	      palAST = new ArgList (aAST,palAST,properargpos);
	  }
	  else{
		  	 
		// aAST = parseArg();
	//	 palAST = parseProperArgList();
		// SourcePosition properarg2pos = new SourcePosition();
		//  copyStart(properargpos, properarg2pos);
		//  finish(properarg2pos);
		 palAST = new ArgList (aAST,new EmptyArgList(dummyPos),properargpos);
	  }
	 
    return palAST;
}

Arg parseArg() throws SyntaxError {
	Arg aAST = null;
	Expr paAST = null;
	SourcePosition argpos = new SourcePosition();
	start(argpos);
	paAST = parseExpr();
	finish(argpos);
	aAST = new Arg(paAST,argpos);		
		
   return aAST;
}


}

