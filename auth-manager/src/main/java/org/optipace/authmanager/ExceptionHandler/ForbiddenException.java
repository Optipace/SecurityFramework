package org.optipace.authmanager.ExceptionHandler;

public class ForbiddenException  extends  RuntimeException{

    public ForbiddenException(String message){
        super(message);
    }
}
