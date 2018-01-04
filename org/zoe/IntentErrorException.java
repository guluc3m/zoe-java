package org.zoe;

public class IntentErrorException extends Exception {
	
	private static final long serialVersionUID = 9010633L; //This is supposed to spell GULUC3M, but there is no number like M...
	
	public IntentErrorException(){super();}
	public IntentErrorException(String message){
		super("Error in intent resolution.\n"+message);
	}
	public IntentErrorException(String message, Throwable cause) { 
		super("Error in intent resolution.\n"+message, cause); 
	}
	public IntentErrorException(Throwable cause) { 
		super(cause); 
	}
}
