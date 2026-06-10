package com.library.application.exception;

public class GenreException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public GenreException(String message){
        super(message);
    }
    public GenreException(String message,Throwable cause){
        super(message,cause);
    }
}
