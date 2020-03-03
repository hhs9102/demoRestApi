package me.ham.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("exception")
public class ExceptionThrowController {
    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public @ResponseBody
    String throwException(){
        throw new RuntimeException("throw RuntimeException");
    }

    @GetMapping("/illegalArgumentException")
    public String throwExceptionIllegal(){
        throw new IllegalArgumentException("throw IllegalArgumentException");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> exceptionHandlerLocally(Exception e){
        System.out.println("EventController exceptionHandlerLocally is called::::::::::");
        String error = "내가 임의로 만든 에러 메시지";
        return buildResponseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, error, e));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> exceptionHandlerLocally(IllegalArgumentException e){
        System.out.println("EventController exceptionHandlerLocally is called::::::::::");
        String error = "IllegalArgumentException";
        return buildResponseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, error, e));
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}
