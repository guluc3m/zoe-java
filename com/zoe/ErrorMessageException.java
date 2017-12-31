package com.zoe;

public class ErrorMessageException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public ErrorMessageException(){super();}
	public ErrorMessageException(String message){
		super("Error in intent resolution.\n"+message);
	}
	public ErrorMessageException(String message, Throwable cause) { 
		super("Error in intent resolution.\n"+message, cause); 
	}
	public ErrorMessageException(Throwable cause) { 
		super(cause); 
	}
}
