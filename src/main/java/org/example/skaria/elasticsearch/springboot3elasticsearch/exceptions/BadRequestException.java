package org.example.skaria.elasticsearch.springboot3elasticsearch.exceptions;

public class BadRequestException extends RuntimeException{

    public BadRequestException(String message){
        super(message);
    }

}
