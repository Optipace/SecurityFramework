package org.optipace.authmanager.ExceptionHandler;


import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {



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
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse>  handleBadRequest(BadRequestException ex){

        log.error("Bad Request exception", ex);
        ErrorResponse err = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage()
        );
        return new ResponseEntity<>(err,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorisedException.class)
    public ResponseEntity<ErrorResponse>  handleUnauthorisation(UnauthorisedException ex){
        log.error("Unauthorised exception", ex);
        ErrorResponse err = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage()
        );
        return new ResponseEntity<>(err,HttpStatus.UNAUTHORIZED);
    }


    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse>  handleForbidden(ForbiddenException ex){
        log.error("forbidden exception", ex);
        ErrorResponse err = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage()
        );
        return new ResponseEntity<>(err,HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse>  handleNotFound(NotFoundException ex){
        log.error("Not found exception", ex);
        ErrorResponse err = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage()
        );
        return new ResponseEntity<>(err,HttpStatus.NOT_FOUND);
    }


}
