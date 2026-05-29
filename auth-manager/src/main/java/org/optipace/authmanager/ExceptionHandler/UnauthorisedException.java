package org.optipace.authmanager.ExceptionHandler;

public class UnauthorisedException extends  RuntimeException{

    public UnauthorisedException(String message){
        super(message);
    }
}
