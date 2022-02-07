package io.datakitchen.ide.json;

public class ParseException extends Exception{

    private final String message;
    private final int line;
    private final int charPosition;

    public ParseException(String message, int line, int charPosition){
        this.message = message;
        this.line = line;
        this.charPosition = charPosition;
    }

    public String getMessage() {
        return message;
    }

    public int getLine() {
        return line;
    }

    public int getCharPosition() {
        return charPosition;
    }
}
