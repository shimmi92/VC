/**
 **	Scanner.java                        
 **/

package VC.Scanner;
import java.*;

import VC.ErrorReporter;

public final class Scanner { 

  private SourceFile sourceFile;
  private boolean debug;

  private ErrorReporter errorReporter;
  private StringBuffer currentSpelling;
  private char currentChar;
  private SourcePosition sourcePos;
  private int lineNum;
  private int startTok;
  private int endTok;
  private int currPos;
  private int floatFlag = 0; 
  private int eflag = 0;
  private int escapeFlag = 0;
  private int errorFlag = 0;
  private int errorMessage = 0;
  private int divFlag = 0;
// =========================================================

  public Scanner(SourceFile source, ErrorReporter reporter) {
    sourceFile = source;
    errorReporter = reporter;
    currentChar = sourceFile.getNextChar();
    debug = false;
    
    // you may initialise your counters for line and column numbers here
    lineNum = 1;
    startTok = 1;
    endTok = 1;
    currPos = 1;
  }

  public void enableDebugging() {
    debug = true;
  }

  // accept gets the next character from the source program.

  private void accept() {

    currentChar = sourceFile.getNextChar();

  // you may save the lexeme of the current token incrementally here
  // you may also increment your line and column counters here
  }

  // inspectChar returns the n-th character after currentChar
  // in the input stream. 
  //
  // If there are fewer than nthChar characters between currentChar 
  // and the end of file marker, SourceFile.eof is returned.
  // 
  // Both currentChar and the current position in the input stream
  // are *not* changed. Therefore, a subsequent call to accept()
  // will always return the next char after currentChar.

  private char inspectChar(int nthChar) {
    return sourceFile.inspectChar(nthChar);
  }

  private int nextToken() {
  // Tokens: separators, operators, literals, identifiers and keyworods
  //for letters
	  
	if(divFlag ==1)  {
		startTok = currPos-1;
        currentSpelling.append('/');
        endTok = currPos-1;
        divFlag = 0;
        return Token.DIV;
	}
	  
	//for string literals
	if(currentChar == '"'){
		startTok = currPos;
		accept();
		currPos++; 
		while(currentChar != '"' ){			
	       if(currentChar == '\n'){
	    	   accept();
	    	   errorFlag = 1;
	    	   errorMessage = 1;
	    	   endTok = currPos-1;
	    	   return Token.STRINGLITERAL; 

	       }
	       currentSpelling.append(currentChar);   
	       if(currentChar == '\\'){
			   escapeFlag =1;
		   }		
			accept();
			if(escapeFlag == 1){
				int length;
				if(currentChar == 'r'){
			        length = currentSpelling.length();
				    currentSpelling.deleteCharAt(length-1);
			   	    currentSpelling.append('\r');   
				}
				else if(currentChar == 'n'){
			   	    length = currentSpelling.length();
					currentSpelling.deleteCharAt(length-1);
					currentSpelling.append('\n');   
				}
				else if(currentChar == 'b'){
					length = currentSpelling.length();
					currentSpelling.deleteCharAt(length-1);
					currentSpelling.append('\b');   
				} 
				else if(currentChar == 't'){
				    length = currentSpelling.length();
				    currentSpelling.deleteCharAt(length-1);
				    currentSpelling.append('\t');   
			    }
			    else if(currentChar == 'f'){
		   		    length = currentSpelling.length();
				    currentSpelling.deleteCharAt(length-1);
				    currentSpelling.append('\f');   
		        }
			    else if(currentChar == '\\'){
		  		    length = currentSpelling.length();
				    currentSpelling.deleteCharAt(length-1);
			   	    currentSpelling.append('\\');   
				}
			    else if(currentChar == '\''){
					length = currentSpelling.length();
					currentSpelling.deleteCharAt(length-1);
					currentSpelling.append('\'');   
				}
			    else if(currentChar == '\"'){
			   	   length = currentSpelling.length();
				   currentSpelling.deleteCharAt(length-1);
				   currentSpelling.append('\"');   
			   }
			   else{
				   errorFlag =1;
			       errorMessage = 2;
				   currentSpelling.append(currentChar);
			   }			   
				   escapeFlag = 0;
				   currPos++;
				   accept();
	       }

			currPos++;
		}
		endTok = currPos;
		accept();
	    currPos++;
		return Token.STRINGLITERAL;
	}
	
	//for intliterals and float literals
    if(Character.isDigit(currentChar) || currentChar == '.'){
	   if(currentChar == '.' && (Character.isDigit(inspectChar(1)))){
	       floatFlag =1;
	   }
	   if(currentChar == '.' && !Character.isDigit(inspectChar(1))){
	       currentSpelling.append(currentChar);
		   accept();
		   startTok=endTok=currPos;
		   return Token.ERROR;
	   }
	   startTok = currPos;
	   currentSpelling.append(currentChar);
	   currPos++;
	   accept();
	
	   while((Character.isDigit(currentChar)) || currentChar == '.' || currentChar == 'e'|| currentChar == 'E'){
	       if(Character.isDigit(currentChar)){
			   currentSpelling.append(currentChar);
			   currPos++;
			   accept();
		   }
		   if(currentChar == '.' ){
			   currentSpelling.append(currentChar);
			   currPos++;
			   accept();
			   floatFlag =1;
		   }
		   if(currentChar == 'e' || currentChar == 'E'){
			   floatFlag =1;
			   if(inspectChar(1) == '+' || inspectChar(1) == '-'){
				   if(Character.isDigit(inspectChar(2))){
					   currentSpelling.append(currentChar);
					   currPos++;
					   accept();
					   currentSpelling.append(currentChar);
					   currPos++;
					   accept();
					   currentSpelling.append(currentChar);
					   currPos++;
					   accept();
					   endTok = currPos-1;
					   floatFlag=0;
					   eflag = 0;
					   return Token.FLOATLITERAL;
				   }
			   }
			   else if(Character.isDigit(inspectChar(1))){
				   currentSpelling.append(currentChar);
				   currPos++;
				   accept();
				   currentSpelling.append(currentChar);
				   currPos++;
				   accept();
				   endTok = currPos-1;
				   floatFlag=0;
				   eflag = 0;
				   return Token.FLOATLITERAL;
			   }
			
			   currentChar ='e';
			   endTok = currPos-1;
			   floatFlag=0;
			   eflag = 0;
			   return Token.FLOATLITERAL;					
		   }
		
	   }
	   if(floatFlag ==1){
		   endTok = currPos-1;
		   floatFlag=0;
		   eflag = 0;
		   return Token.FLOATLITERAL;
	   }
	   endTok = currPos-1;
	   return Token.INTLITERAL;	
    }
	
	// for keywords or identifiers
    if(Character.isLetter(currentChar) || currentChar == '_'){
        currentSpelling.append(currentChar);
        startTok = currPos;
        accept();
        currPos++;
        while(Character.isLetter(currentChar) || Character.isDigit(currentChar) || currentChar == '_'){
    	    currentSpelling.append(currentChar);
    	    accept();
    	    currPos++;
       }
       endTok = currPos-1;
       String check = currentSpelling.toString();
       if(check.equals("while")){
    	   return Token.WHILE;
       }
       else if(check.equals("if")){    	  
    	   return Token.IF;
       }
       else if(check.equals("int")){    	   
    	   return Token.INT;
       }
       else if(check.equals("else")){
    	   return Token.ELSE;    	  
       }
       else if(check.equals("continue")){
    	   return Token.CONTINUE;    	   
       }
       else if(check.equals("break")){
    	   return Token.BREAK;
       }
       else if(check.equals("return")){
    	   return Token.RETURN;   	   
       }
       else if(check.equals("boolean")){
    	   return Token.BOOLEAN;   	   
       }
       else if(check.equals("for")){
    	   return Token.FOR;    	   
       }
       else if(check.equals("void")){
    	   return Token.VOID;    	   
       }
       else if(check.equals("float")){
    	   return Token.FLOAT;   	   
       }
       else if(check.equals("true")){
    	   return Token.BOOLEANLITERAL;
       }
       else if(check.equals("false")){
    	   return Token.BOOLEANLITERAL;
       }
   	   return Token.ID;
       
    }
        
    //brackets
    if(currentChar == '('){
       startTok = currPos;
       currPos++;
       currentSpelling.append(currentChar);     
   	   accept();
   	   endTok = currPos-1;
   	   return Token.LPAREN;
    }
    if(currentChar == ')'){
    	startTok = currPos;
       currPos++;
       currentSpelling.append(currentChar);
       accept();
       endTok = currPos-1;
       return Token.RPAREN;
    }
    if(currentChar == '{'){
    	startTok = currPos;
       currPos++;
       currentSpelling.append(currentChar);
       accept();
       endTok = currPos-1;
       return Token.LCURLY;
    }
    if(currentChar == '}'){
    	startTok = currPos;
       currPos++;
       currentSpelling.append(currentChar);
       accept();
       endTok = currPos-1;
       return Token.RCURLY;
    }
    if(currentChar == ']'){
    	startTok = currPos;
         currPos++;
         currentSpelling.append(currentChar);
         accept();
         endTok = currPos-1;
         return Token.RBRACKET;
    }
    if(currentChar == '['){
    	startTok = currPos;
        currPos++;
        currentSpelling.append(currentChar);
        accept();
        endTok = currPos-1;
        return Token.LBRACKET;
    } 
    if(currentChar == ','){
    	startTok = currPos;
        currPos++;
        currentSpelling.append(currentChar);
        accept();
        endTok = currPos-1;
        return Token.COMMA;
    }    
    //operators
    if(currentChar == '+'){
    	startTok = currPos;
        currPos++;
        currentSpelling.append(currentChar);
        accept();
        endTok = currPos-1;
        return Token.PLUS;
    } 
    if(currentChar == '-'){
    	startTok = currPos;
        currPos++;
        currentSpelling.append(currentChar);
        accept();
        endTok = currPos-1;
        return Token.MINUS;
    }
    if(currentChar == '*'){
    	startTok = currPos;
        currPos++;
        currentSpelling.append(currentChar);
        accept();
        endTok = currPos-1;
        return Token.MULT;
    }
    if(currentChar == '/'){
    	startTok = currPos;
        currPos++;
        currentSpelling.append(currentChar);
        accept();
        endTok = currPos-1;
        divFlag = 0;
        return Token.DIV;
    }
    if(currentChar == '!'){
    	startTok = currPos;
        currPos++;
        currentSpelling.append(currentChar);
        accept();
        
        if(currentChar == '='){
        	currPos++;
        	currentSpelling.append(currentChar);
            accept();
            endTok = currPos-1;
            return Token.NOTEQ;
        }
        else{		
        	endTok = currPos-1;
        	return Token.NOT;	
        }        
    }
    if(currentChar == '='){   	
    	startTok = currPos;
    	currPos++;
    	currentSpelling.append(currentChar);
    	accept();    	
    	if(currentChar == '='){
    	   currPos++;
    	   currentSpelling.append(currentChar);
    	   accept();
    	   endTok = currPos-1;
    	   return Token.EQEQ;
    	}
    	else{ 
    	   endTok = startTok;
   		   return Token.EQ; 		
    	}
    }
    if(currentChar == '<'){
    	startTok = currPos;
    	currPos++;
    	currentSpelling.append(currentChar);
    	accept();
    	if(currentChar == '='){
    		currPos++;
     	    currentSpelling.append(currentChar);
     	    accept();
     	    endTok = currPos-1;
     	    return Token.LTEQ;
    	}
    	else{
    		endTok = currPos-1;
    		return Token.LT;    		
    	}
    }
    if(currentChar == '>'){
    	startTok = currPos;
    	currPos++;
    	currentSpelling.append(currentChar);
    	accept();
    	if(currentChar == '='){
    		currPos++;
     	    currentSpelling.append(currentChar);
     	    accept();
     	    endTok = currPos-1;
     	    return Token.GTEQ;
    	}
    	else{
    		endTok = currPos-1;
    		return Token.GT;
    		
    	}
    }
    if(currentChar == ';'){
    	startTok = currPos;
    	currPos++;
    	currentSpelling.append(currentChar);
    	accept();
    	endTok = currPos-1;
    	return Token.SEMICOLON;
    }
    if(currentChar == '&' && inspectChar(1) == '&'){
    	startTok = currPos;
    	currPos++;
    	currentSpelling.append(currentChar);
    	accept(); 
    	currPos++;
    	currentSpelling.append(currentChar);
    	accept();
    	endTok = currPos-1;
    	return Token.ANDAND;
    }
    if(currentChar == '|' && inspectChar(1) == '|'){
    	startTok = currPos;
    	currPos++;
    	currentSpelling.append(currentChar);
    	accept();    	
    	currPos++;
    	currentSpelling.append(currentChar);
    	accept();
    	endTok = currPos-1;
    	return Token.OROR;
    }
    
    if(currentChar == SourceFile.eof){
		currentSpelling.append(Token.spell(Token.EOF));
        startTok=1;
        endTok=1;
		return Token.EOF;
	   
	 } 
     currentSpelling.append(currentChar);
	 accept(); 
	 currPos++;
	 startTok=endTok = currPos-1;
	 return Token.ERROR;
 }


  void skipSpaceAndComments() {
     if(currentChar == '\n'){
     	endTok=startTok = currPos = 1;
     	lineNum++;
        accept();
        if(currentChar == '\n'){
           skipSpaceAndComments();
        }
     }
    //for tabs
    if(currentChar == '\t'){
    	while(currPos % 8 != 0){
    	    currPos++;
    	}
    	currPos++;
    	accept();
    }
    //for spaces
    if(currentChar == ' '){
    	currPos++;
    	accept();
    	skipSpaceAndComments();
    }   
    //for comments
    else if(currentChar == '/'){
    	currPos++;
    	accept();
    	if(currentChar == '/'){
    		currPos++;
    		accept();
    		while(currentChar != '\n'){
    			accept();  			
    		}
    		if(currentChar == '\n'){
    	     	endTok=startTok = currPos = 1;
    	     	skipSpaceAndComments();   	     
    	     }   		
       	}
    	else if(currentChar == '*'){
    		currPos++;
    		accept();
    		while(true){
    			if(currentChar == '/'){
    		      	accept();
    			    skipSpaceAndComments();
    			    return;
    		    }
    			
    			if(currentChar == '*' && inspectChar(1) == '/'){
    				accept();
    				accept();
    				skipSpaceAndComments();
    		      return;
    			}
    				 			
    			if(currentChar == '\n'){
            		skipSpaceAndComments();
            		
                }
    			if(currentChar == SourceFile.eof){
    				errorFlag =1;
    				errorMessage = 3;
    				return;
    			}
    			accept();
    		}
    	}
    	else{
    		divFlag = 1;    		
    		return;
    	}
     }    
  }

  public Token getToken() {
    Token tok;
    int kind;
    // skip white space and comments
   skipSpaceAndComments();

   currentSpelling = new StringBuffer("");  
   // You must record the position of the current token somehow
   
   kind = nextToken();
   
   sourcePos = new SourcePosition(lineNum,startTok,endTok);
   
   tok = new Token(kind, currentSpelling.toString(), sourcePos);
   if(errorFlag == 1){
	   if(errorMessage == 1){
		   lineNum++;
		   currPos=1;
	   }
	   errorReporter.reportError(errorMessage,sourcePos);
	   errorFlag = 0;
   }   
   // * do not remove these three lines
   if (debug)
     System.out.println(tok);
   return tok;
   }

}
