package antifraud.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class IpAddressOrCardConflictException extends RuntimeException{
    public IpAddressOrCardConflictException(String message) {
        super(message);
    }
}
