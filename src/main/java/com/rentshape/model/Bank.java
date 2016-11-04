package com.rentshape.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by matt on 11/3/16.
 */
public class Bank extends AppPart {
    public static final String NAME = "name";
    public static final String ADDRESS = "address";
    public static final String ACCOUNT_NUMBER = "account_number";

    public static final List<String> STRINGFIELDS = Arrays.asList(NAME, ADDRESS, ACCOUNT_NUMBER);
    public static final List<String> INTFIELDS = new ArrayList<>();

    public Bank(){
        super();
    };

    @Override
    String tableName() {
        return "banks";
    }

    @Override
    List<String> stringFields() {
        return STRINGFIELDS;
    }

    @Override
    List<String> intFields() {
        return INTFIELDS;
    }
}
