/***
 * *
 * * Recogniser.java            
 * *
 ***/

/* At this stage, this parser accepts a subset of VC defined	by
 * the following grammar. 
 *
 * You need to modify the supplied parsing methods (if necessary) and 
 * add the missing ones to obtain a parser for the VC language.
 *
 * (23-*-March-*-2014)

program       -> func-decl

// declaration

func-decl     -> void identifier "(" ")" compound-stmt

identifier    -> ID

// statements 
compound-stmt -> "{" stmt* "}" 
stmt          -> continue-stmt
    	      |  expr-stmt
continue-stmt -> continue ";"
expr-stmt     -> expr? ";"

// expressions 
expr                -> assignment-expr
assignment-expr     -> additive-expr
additive-expr       -> multiplicative-expr
                    |  additive-expr "+" multiplicative-expr
multiplicative-expr -> unary-expr
	            |  multiplicative-expr "*" unary-expr
unary-expr          -> "-" unary-expr
		    |  primary-expr

primary-expr        -> identifier
 		    |  INTLITERAL
		    | "(" expr ")"
*/

package VC.Recogniser;

import VC.Scanner.Scanner;
import VC.Scanner.SourcePosition;
import VC.Scanner.Token;
import VC.ErrorReporter;

public class Recogniser {

  private Scanner scanner;
  private ErrorReporter errorReporter;
  private Token currentToken;
  
  int varCalledFromProgram = 0;

  public Recogniser (Scanner lexer, ErrorReporter reporter) {
    scanner = lexer;
    errorReporter = reporter;

    currentToken = scanner.getToken();
  }

// match checks to see f the current token matches tokenExpected.
// If so, fetches the next token.
// If not, reports a syntactic error.

  void match(int tokenExpected) throws SyntaxError {
    if (currentToken.kind == tokenExpected) {
      currentToken = scanner.getToken();
    } else {
  
      syntacticError("\"%\" expected here", Token.spell(tokenExpected));
    }
  }

 // accepts the current token and fetches the next
  void accept() {
    currentToken = scanner.getToken();
  }

  void syntacticError(String messageTemplate, String tokenQuoted) throws SyntaxError {
    SourcePosition pos = currentToken.position;
    errorReporter.numErrors++;
    errorReporter.reportError2(messageTemplate, pos);
    throw(new SyntaxError());
  }


// ========================== PROGRAMS ========================

  public void parseProgram() {

    try {
    	parseDecl();
      if (currentToken.kind != Token.EOF) {
    
        syntacticError("\"%\" wrong result type for a function", currentToken.spelling);
      }
    }
    catch (SyntaxError s) {  }
  }

// ========================== DECLARATIONS ========================
  
  void parseDecl() throws SyntaxError {

	  while(currentToken.kind == Token.VOID || currentToken.kind == Token.BOOLEAN || currentToken.kind == Token.INT
	    	     || currentToken.kind == Token.FLOAT){
	    		  accept();
	    		    if(currentToken.kind == Token.ID){
		            parseIdent();
	    		    if(currentToken.kind == Token.LPAREN){
	    		    	parseFuncDecl();
	    		    	
	    		    }else{
	    		    	varCalledFromProgram=1;
	    		    	parseVarDecl();
	                } 	                
	       }
	  }	
  }
  void parseFuncDecl() throws SyntaxError {
   
    parseParaList();
    parseCompoundStmt();
  }
  void parseVarDecl() throws SyntaxError {

	  parseInitDeclaratorList();
	  
	  match(Token.SEMICOLON);
  }
  void parseInitDeclaratorList() throws SyntaxError {
	
	  parseInitDeclarator();
	  while(currentToken.kind == Token.COMMA){
		
		  accept();
		  parseInitDeclarator();
	  }
	  
  }
  void parseInitDeclarator() throws SyntaxError {

	  parseDeclarator();
	  if(currentToken.kind == Token.EQ){
		  acceptOperator();
		  parseInitialiser();
	  }
  }
  void parseDeclarator() throws SyntaxError {
	
	  if(varCalledFromProgram!=1){
	  parseIdent();
	  }
	  varCalledFromProgram=0;
	  if(currentToken.kind == Token.LBRACKET){
		
		  accept();
		  while(currentToken.kind == Token.INTLITERAL){
		     parseIntLiteral(); // dont need accept here, this function moves on
		  }
		  match(Token.RBRACKET);
	  }
	  	  
  }
  void parseInitialiser() throws SyntaxError {
	 
	  switch (currentToken.kind) {
      case Token.LCURLY:
        {
          accept();
          parseExpr();
          while(currentToken.kind == Token.COMMA){
        	  accept();
        	  parseExpr();
        	 
          }
          match(Token.RCURLY);
          
        }
        break;
      
      default:
        parsePrimaryExpr();
        break;
       
    }
  }
// ======================= STATEMENTS ==============================


  void parseCompoundStmt() throws SyntaxError {
	
    match(Token.LCURLY);
  
    while(currentToken.kind != Token.RCURLY){
    	if(currentToken.kind == Token.VOID || currentToken.kind == Token.BOOLEAN || currentToken.kind == Token.INT
	    	     || currentToken.kind == Token.FLOAT){
    		               accept();
                          parseVarDecl();
    	}
    	else{
        parseStmtList();
    	}
    }
    accept();
  }

 // Here, a new nontermial has been introduced to define { stmt } *
  void parseStmtList() throws SyntaxError {
	
    while (currentToken.kind != Token.RCURLY) 
      parseStmt();
  }

  void parseStmt() throws SyntaxError {
	
    switch (currentToken.kind) {
    case Token.LCURLY:
    	parseCompoundStmt();
    	break;
    case Token.CONTINUE:
      parseContinueStmt();
      break;
    case Token.BREAK:
      parseBreakStmt();
      break;
    case Token.IF:
    	parseIfStmt();
    	break;
    case Token.WHILE:
    	parseWhileStmt();
    	break;
    case Token.RETURN:
    	parseReturnStmt();
    	break;
    case Token.FOR:
    	parseForStmt();
    	break;
    default:
      parseExprStmt();
      break;

    }
  }

  void parseContinueStmt() throws SyntaxError {
	
    match(Token.CONTINUE);
    match(Token.SEMICOLON);

  }
  void parseBreakStmt() throws SyntaxError{
	
	  match(Token.BREAK);
	  match(Token.SEMICOLON);
  }
  void parseIfStmt() throws SyntaxError{
	
	  match(Token.IF);
	  match(Token.LPAREN);
	  parseExpr();
	  match(Token.RPAREN);
	  parseStmt();
	  if(currentToken.kind == Token.ELSE){
		  accept();
		  parseStmt();
		
	  }
	  
  }
  void parseForStmt() throws SyntaxError{
	 
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
		        parseExpr();
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
		        parseExpr();
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
		        parseExpr();
		       match(Token.RPAREN);
	  } else{
		  match(Token.RPAREN);
	  }
	  parseStmt();
  }
  void parseWhileStmt() throws SyntaxError{
	
	  match(Token.WHILE);
	  match(Token.LPAREN);
	  parseExpr();
	  match(Token.RPAREN);
	  parseStmt();
  }
  void parseReturnStmt() throws SyntaxError{
	 
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
		        parseExpr();
		        match(Token.SEMICOLON);
	  } else {
		      match(Token.SEMICOLON);
	  }
  }
  void parseExprStmt() throws SyntaxError {
	 
    while (currentToken.kind == Token.ID
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


        parseExpr();
    
    } 
       match(Token.SEMICOLON);
       
  
      return;
  }


// ======================= IDENTIFIERS ======================

 // Call parseIdent rather than match(Token.ID). 
 // In Assignment 3, an Identifier node will be constructed in here.


  void parseIdent() throws SyntaxError {
	
    if (currentToken.kind == Token.ID) {
      currentToken = scanner.getToken();
    } else {
     
      syntacticError("identifier expected here", "");
    }
  }

// ======================= OPERATORS ======================

 // Call acceptOperator rather than accept(). 
 // In Assignment 3, an Operator Node will be constructed in here.

  void acceptOperator() throws SyntaxError {
	
    currentToken = scanner.getToken();
  }


// ======================= EXPRESSIONS ======================

  void parseExpr() throws SyntaxError {
	    parseAssignExpr();
  }


  void parseAssignExpr() throws SyntaxError {
	     parseCOE();
    while(currentToken.kind == Token.EQ){
    	acceptOperator();
    	parseCOE();
    }

  }
  void parseCOE() throws SyntaxError{
		  parseCAE();
	  while(currentToken.kind == Token.OROR){
		  acceptOperator();
		  parseCAE();
	  }
  }
  void parseCAE() throws SyntaxError{
	 	  parseEqualityExpr();
	  while(currentToken.kind == Token.ANDAND){
		  acceptOperator();
		  parseEqualityExpr();
	  }
  }
  void parseEqualityExpr() throws SyntaxError{
	 	  parseRelExpr();
	  while(currentToken.kind == Token.EQEQ){
		  acceptOperator();
		  parseRelExpr();
	  }
	  while(currentToken.kind == Token.NOTEQ){
		  acceptOperator();
		  parseRelExpr();
	  }
  }
  void parseRelExpr() throws SyntaxError{
		  parseAdditiveExpr();
	  while(currentToken.kind == Token.LT){
		  acceptOperator();
		  parseAdditiveExpr();
	  }
	  while(currentToken.kind == Token.LTEQ){
		  acceptOperator();
		  parseAdditiveExpr();
	  }
	  while(currentToken.kind == Token.GT){
		  acceptOperator();
		  parseAdditiveExpr();
	  }
	  while(currentToken.kind == Token.GTEQ){
		  acceptOperator();
		  parseAdditiveExpr();
	  }
  }
  void parseAdditiveExpr() throws SyntaxError {
	    parseMultiplicativeExpr();
    while (currentToken.kind == Token.PLUS) {
      acceptOperator();
      parseMultiplicativeExpr();
    }
    while(currentToken.kind == Token.MINUS){
    	acceptOperator();
    	parseMultiplicativeExpr();
    }
  }

  void parseMultiplicativeExpr() throws SyntaxError {
		  
    parseUnaryExpr();
    
    while (currentToken.kind == Token.MULT) {
      acceptOperator();
      
      parseUnaryExpr();
    }
    while(currentToken.kind == Token.DIV){
      acceptOperator();
      
      parseUnaryExpr();
    }
    
     }

  void parseUnaryExpr() throws SyntaxError {
	     switch (currentToken.kind) {
      case Token.MINUS:        
          acceptOperator();       
          parseUnaryExpr();       
          break;
      case Token.PLUS:
    	  acceptOperator();
    	  parseUnaryExpr();
          break;
     case Token.NOT:      
    	  System.out.println(currentToken.kind);
    	  acceptOperator();    	  
    	    	  parseUnaryExpr();
          break;
      default:
        parsePrimaryExpr();
               break;
       
    }
  }

  void parsePrimaryExpr() throws SyntaxError {
	     switch (currentToken.kind) {

      case Token.ID:
        parseIdent();
        if(currentToken.kind == Token.LBRACKET){
        	accept();
        	parseExpr();
        	match(Token.RBRACKET);           	
        }
        if(currentToken.kind == Token.LPAREN){
        	        	        		parseArgList();
        	        	        }
                break;

      case Token.LPAREN:
        {
                 accept();
          parseExpr();
	  match(Token.RPAREN);
        }
        break;

      case Token.INTLITERAL:
        parseIntLiteral();
        break;
      case Token.FLOATLITERAL:
          parseFloatLiteral();
          break;
      case Token.BOOLEANLITERAL:
          parseBooleanLiteral();
          break;
      case Token.STRINGLITERAL:
          parseStringLiteral();
          break;
      
      default:
        syntacticError("illegal parimary expression", currentToken.spelling);
       
    }
  }

// ========================== LITERALS ========================

  // Call these methods rather than accept().  In Assignment 3, 
  // literal AST nodes will be constructed inside these methods. 

  void parseIntLiteral() throws SyntaxError {

    if (currentToken.kind == Token.INTLITERAL) {
      currentToken = scanner.getToken();
       } else 
      syntacticError("integer literal expected here", "");
  }

  void parseFloatLiteral() throws SyntaxError {
	    if (currentToken.kind == Token.FLOATLITERAL) {
      currentToken = scanner.getToken();
    } else 
      syntacticError("float literal expected here", "");
  }

  void parseBooleanLiteral() throws SyntaxError {
	    if (currentToken.kind == Token.BOOLEANLITERAL) {
      currentToken = scanner.getToken();
         } else 
      syntacticError("boolean literal expected here", "");
  }
  void parseStringLiteral() throws SyntaxError {
		    if (currentToken.kind == Token.STRINGLITERAL) {
	      currentToken = scanner.getToken();
	    } else 
	      syntacticError("String literal expected here", "");
	  }
//===========================Parameters=====================
  
  void parseParaList() throws SyntaxError {
		  match(Token.LPAREN);
		  while(currentToken.kind != Token.RPAREN){
		 				  parseProperParaList();
	  }
	  accept();
	  
  }
  void parseProperParaList() throws SyntaxError {
		  parseParaDecl();
	  while(currentToken.kind == Token.COMMA){
			  accept();
		  parseParaDecl();
	  }
	
  }
  void parseParaDecl() throws SyntaxError {
	 	  if(currentToken.kind == Token.VOID || currentToken.kind == Token.BOOLEAN || currentToken.kind == Token.INT
	    	     || currentToken.kind == Token.FLOAT){
	    		  accept();
                  parseDeclarator();	    		  
	  }
	  else{
				  syntacticError("\"%\" wrong starting", currentToken.spelling);
	  }
  }
  void parseArgList() throws SyntaxError {
	  match(Token.LPAREN);
	  while(currentToken.kind != Token.RPAREN){
		  parseProperArgList();
	  }
	  accept();
  }
  
  void parseProperArgList() throws SyntaxError {
		  parseArg();
	  while(currentToken.kind == Token.COMMA){
		  acceptOperator();
		  parseArg();
	  }
	   }
  void parseArg() throws SyntaxError {
		  parseExpr();
  }
}
