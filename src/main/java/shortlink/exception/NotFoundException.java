package shortlink.exception;

/*
* Not found Exception
* */
public class NotFoundException extends RuntimeException{

    public NotFoundException(String message) {
        super(message);
    }
}
