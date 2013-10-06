package com.qbix.tkinfo.activities.misc;

public class NFCException extends RuntimeException{

	public NFCException(String message){
		super(message);
	}
	public NFCException(String message,Throwable throwable){
		super(message, throwable);
	}
}
