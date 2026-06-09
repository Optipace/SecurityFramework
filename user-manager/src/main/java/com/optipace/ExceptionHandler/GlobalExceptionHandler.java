package com.optipace.ExceptionHandler;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

     @ExceptionHandler(UnauthorisedException.class)
    public ResponseEntity<ErrorResponse>  handleUnauthorised(UnauthorisedException e ){
         log.error("Unauthorized Exception", e);

         ErrorResponse response = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);

    }


    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse>  handleNotFound(UnauthorisedException e ){
        log.error("Not found Exception", e);

        ErrorResponse response = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {

        log.error("Unexpected application error", ex);

        ErrorResponse err = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error"
        );

        return new ResponseEntity<>(err,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<ErrorResponse>  handleAlreadyExist(AlreadyExistException e ){
        log.error("AlreadyExistException ", e);

        ErrorResponse response = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);

    }





    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse>  handleBadRequest(BadRequestException e ){
        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

    }
}

