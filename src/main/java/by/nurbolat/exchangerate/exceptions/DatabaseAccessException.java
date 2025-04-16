package by.nurbolat.exchangerate.exceptions;

public class DatabaseAccessException extends Exception{
    private final String message;

    public DatabaseAccessException(String message) {
        super(message);
        this.message = "Database is not available";
    }

    public String getMessage(){
        return this.message;
    }
}
