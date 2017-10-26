package com.zoe;

public class NotAnIntentException extends Exception {

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
