package com.rentshape.model;

import java.sql.Connection;

/**
 * Created by matt on 10/26/16.
 */
public abstract class DbModel {
    static Connection conn;
    public static void setConnection(Connection conn){
        DbModel.conn = conn;
    }
    public static void validateConnection(){
        if (null == conn){
            throw new RuntimeException("Connection is null, initialize connection before saving");
        }
    }

    /**
     * Convert value to a string for DB insert/update commands
     * Converts string to "string" (w/ quotes)
     * Converts int to string w/o quotes
     * @param value
     * @return
     */
    public static String stringify(Object value){
        if (value instanceof String) {
            return "\"" + value + "\"";
        } else if (value instanceof Integer){
            return Integer.toString((Integer) value);
        } else {
            throw new IllegalArgumentException("Unable to stringify " + value + " because it is a " + value.getClass().getCanonicalName());
        }
    }
}
