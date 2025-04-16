package by.nurbolat.exchangerate.exceptions;

public class DuplicateKeyValueExceptions extends Exception {

    private final String message;

    public DuplicateKeyValueExceptions(String message) {
        super(message);
        this.message = "Duplicate Key! Exchange Currency already exist";
    }

    public String getMessage(){
        return this.message;
    }
}
