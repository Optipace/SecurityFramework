package com.optipace.ExceptionHandler;

public class AlreadyExistException extends  RuntimeException{
    public AlreadyExistException(String message){
        super(message);
    }
}
