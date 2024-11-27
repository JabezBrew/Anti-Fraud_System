package antifraud.errors;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(reason = "Unprocessable Content", code = org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY)
public class UnprocessableContentException extends RuntimeException{
    public UnprocessableContentException(String message) {
        super(message);
    }
}
