package com.rentshape.exceptions;

/**
 * Created by matt on 10/26/16.
 */
public class DuplicateUserException extends Exception {
    public DuplicateUserException(String email){
        super(email + " already exists, please login with that account or use another email");
    }
}
