package com.optipace.ExceptionHandler;

public class UnauthorisedException extends RuntimeException{

    UnauthorisedException(String message){
        super(message);
    }
}
