package main.java.br.usp.icmc.scc0204.javacafe.exceptions;

public class EmptyOrderException extends Exception {
    public EmptyOrderException(String message) {
        super(message);
    }
}