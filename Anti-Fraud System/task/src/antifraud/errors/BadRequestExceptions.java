package antifraud.errors;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = org.springframework.http.HttpStatus.BAD_REQUEST)
public class BadRequestExceptions extends RuntimeException{
    public BadRequestExceptions(String message) {
        super(message);
    }
}
