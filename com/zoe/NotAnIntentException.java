package com.zoe;

public class NotAnIntentException extends Exception {

	private static final long serialVersionUID = 9010631L; //This is supposed to spell GULUC3M, but there is no number like M...
	
	public NotAnIntentException(){super();}
	public NotAnIntentException(String message){
		super("The message recieved was not an intent.\n"+message);
	}
	public NotAnIntentException(String message, Throwable cause) { 
		super("The message recieved was not an intent.\n"+message, cause); 
	}
	public NotAnIntentException(Throwable cause) { 
		super(cause); 
	}
}
