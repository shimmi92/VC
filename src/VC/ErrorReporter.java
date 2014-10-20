/*
 * ErrorReporter.java     
 */

package VC;

import VC.Scanner.SourcePosition;

public class ErrorReporter {

  public int numErrors;

  public ErrorReporter() {
    numErrors = 0;
  }

  public void reportError(int message, SourcePosition pos) {
    System.out.print ("ERROR: ");
    System.out.print(pos.lineStart + "(" + pos.charStart + ").." +
                     pos.lineFinish+ "(" + pos.charFinish + "): ");

    if(message == 1){
    	System.out.println("Unterminated string.");
         
    }
    if(message == 2){
    	System.out.println("Illegal escape chracter.");
    }
    if(message == 3){
    	System.out.println("Unterminated comment.");
    }
    numErrors++;
  }
  public void reportError(String message, SourcePosition pos) {
	  
  }
  public void reportError2(String messageTemplate, SourcePosition pos){
	  numErrors++;
	  System.out.println("ERROR: "+ pos+ ": "+ messageTemplate );
  }
  public void reportError3(String messageTemplate, String tokenQuoted, SourcePosition pos){
	  numErrors++;
	  System.out.println("ERROR: "+ pos+ ": "+ messageTemplate );
  }
  public void reportRestriction(String message) {
	  numErrors++;
    System.out.println("RESTRICTION: " + message);
  }
}
