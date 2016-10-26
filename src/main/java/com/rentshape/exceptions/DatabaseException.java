package com.rentshape.exceptions;

/**
 * Created by matt on 10/26/16.
 */
public class DatabaseException extends Exception {
    public DatabaseException(String message, Exception e){
        super("We had a problem " + message + " please try again later", e);
    }
}
