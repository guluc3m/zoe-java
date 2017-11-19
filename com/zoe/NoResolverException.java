package com.zoe;

public class NoResolverException extends Exception{

	private static final long serialVersionUID = 9010632L; //This is supposed to spell GULUC3M, but there is no number like M...
	
	public NoResolverException(){super();}
	public NoResolverException(String message){
		super("There is no resolver to resolve this intent.\n"+message);
	}
	public NoResolverException(String message, Throwable cause) { 
		super("There is no resolver to resolve this intent.\n"+message, cause); 
	}
	public NoResolverException(Throwable cause) { 
		super(cause); 
	}
}
