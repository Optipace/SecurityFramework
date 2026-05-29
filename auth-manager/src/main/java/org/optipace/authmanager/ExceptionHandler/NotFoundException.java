package org.optipace.authmanager.ExceptionHandler;

public class NotFoundException extends RuntimeException{


    public NotFoundException(String message){
        super(message);
    }
}
