package com.rentshape.model;

import com.rentshape.exceptions.DatabaseException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by matt on 10/26/16.
 */
public class Application extends DbModel {
    public static final int MAX_STRING = 250;

    public final static String ID = "id";
    public final static String LASTNAME = "last_name";
    public final static String FIRSTNAME = "first_name";
    public final static String MIDNAME = "middle_name";
    public final static String SSN = "ssn";
    public final static String OTHERNAMES = "other_names";
    public final static String WORKPHONE = "work_phone";
    public final static String HOMEPHONE = "home_phone";
    public final static String CELLPHONE = "cell_phone";
    public final static String DOB = "dob";
    public final static String EMAIL = "email";
    public final static String IDTYPE = "id_type";
    public final static String IDNUM = "id_number";
    public final static String IDISS = "id_issuer";
    public final static String IDEXP = "id_experation";
    public final static List<String> STRING_FIELDS = Arrays.asList(
            LASTNAME, FIRSTNAME, MIDNAME, SSN, OTHERNAMES,
            WORKPHONE, HOMEPHONE, CELLPHONE, DOB, EMAIL,
            IDTYPE, IDNUM, IDISS, IDEXP
    );
    public final static List<String> INT_FIELDS = Arrays.asList(ID);

    public final static String USER = "user";
    public final static String USERUUID = "user_uuid";

    public final static String BANKS = "_banks";

    public static List<Map<String, Object>> fromUser(User user) throws DatabaseException {
        String uuid = user.getUuid();
        List<Map<String, Object>> apps = Application.fromField(USERUUID, uuid);
        return apps;
    }

    public static Map<String, Object> fromId(int id) throws DatabaseException {
        List<Map<String, Object>> apps = Application.fromField(ID, id);
        if (apps.size() > 0){
            return apps.get(0);
        } else {
            return null; //TODO use optional?
        }

    }

    private static List<Map<String, Object>> fromField(String field, Object value) throws DatabaseException {
        validateConnection();
        String query = "select * from applications where "+field+" = ?";
        List<Map<String, Object>> appList = new ArrayList<>();
        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            if (value instanceof String) {
                stmt.setString(1, (String) value);
            } else if (value instanceof Integer){
                stmt.setInt(1, (int) value);
            } else {
                throw new IllegalArgumentException("Cannot get application by field " + field + " with " + value + " " + value.getClass().getCanonicalName());
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                Map<String, Object> app = new HashMap<>();
                for (String stringField : STRING_FIELDS){
                    app.put(stringField, rs.getString(stringField));
                }
                for (String intField : INT_FIELDS) {
                    app.put(intField, rs.getInt(intField));
                }
                String userUuid = rs.getString(USERUUID);
                app.put(USERUUID, userUuid);
                User user = User.fromUuid(userUuid);
                app.put(USER, user);

                for (AppPart model : AppPart.models.values()){
                    List<Map<String, Object>> records = model.fromAppId((int) app.get(ID));
                    app.put(model.tableName(), records);
                }

                appList.add(app);
            }
            return appList;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseException("querying the database", e);
        }
    }

    // Constructors
    private Application(){}
    Map<String, Object> newApplication(){
        return new HashMap<>();
    }
    public static Map<String, Object> newApplication(User user){
        Map<String, Object> app = new HashMap<>();
        return setUser(app, user);
    }

    public static Map<String, Object> setString(Map<String, Object> app, String field, String value){
        if (STRING_FIELDS.contains(field)){
            app.put(field, value);
        } else {
            throw new IllegalArgumentException(field + " is not a valid string field for Applications");
        }
        return app;
    }
    public static Map<String, Object> setInt(Map<String, Object> app, String field, Integer value){
        if (INT_FIELDS.contains(field)){
            app.put(field, value);
        } else {
            throw new IllegalArgumentException(field + " is not a valid integer field for Applications");
        }
        return app;
    }
    public static Map<String, Object> setUser(Map<String, Object> app, User user){
        app.put(USER, user);
        return app;
    }
    public static User getUser(Map<String, Object> app){
        return (User) app.get(USER);
    }

    private static List<String> fieldsToSave(Map<String, Object> app){
        List<String> fields = new ArrayList<>();
        for (String field : STRING_FIELDS){
            if (app.keySet().contains(field)){
                fields.add(field);
            }
        }
        for (String field : INT_FIELDS){
            if (app.keySet().contains(field)){
                fields.add(field);
            }
        }
        return fields;
    }

    public static void create(Map<String, Object> app) throws DatabaseException {
        if (null == app.get(USER)){
            throw new IllegalArgumentException("Cannot create an application without the application belonging to a user");
        }
        List<String> fields = fieldsToSave(app);
        fields.add(USERUUID);
        StringBuilder createStatement = new StringBuilder();
        createStatement.append("INSERT into applications (");
        createStatement.append(fields.stream().collect(Collectors.joining(",")));
        createStatement.append(") VALUES(");
        StringJoiner blanks = new StringJoiner(",");
        for (int i=0; i<fields.size(); i++){
            blanks.add("?");
        }
        createStatement.append(blanks.toString());
        createStatement.append(");");

        try {
            PreparedStatement stmt = conn.prepareStatement(createStatement.toString());
            for (int i = 0; i < fields.size(); i++) {
                String field = fields.get(i);
                if (field.equals(USERUUID)) {
                    stmt.setString(i + 1, ((User) app.get(USER)).getUuid());
                } else if (STRING_FIELDS.contains(field)) {
                    String value = (String) app.get(field);
                    if (null != value){
                        value = value.substring(0, Math.min(value.length(), MAX_STRING));
                    }
                    stmt.setString(i + 1, value);
                } else if (INT_FIELDS.contains(field)) {
                    stmt.setInt(i + 1, (int) app.get(field));
                } else {
                    throw new IllegalArgumentException("Cannot add field " + field + " not a recognized type");
                }
            }
            stmt.execute();
        } catch (SQLException e) {
            throw new DatabaseException("creating your application", e);
        }
    }

    public static void save(Map<String, Object> app) throws DatabaseException {
        if (null == app.get(USER)){
            throw new IllegalArgumentException("Cannot create an application without the application belonging to a user");
        }
        if (null == app.get(ID)){
            throw new IllegalArgumentException("Cannot save an application that has no ID, must create it first");
        }
        List<String> fields = fieldsToSave(app);
        StringBuilder createStatement = new StringBuilder();
        createStatement.append("UPDATE applications set ");
        createStatement.append(fields.stream().map(f -> f + " = ?").collect(Collectors.joining(",")));
        createStatement.append(" where id = ?;");

        try {
            PreparedStatement stmt = conn.prepareStatement(createStatement.toString());
            for (int i = 0; i < fields.size(); i++) {
                String field = fields.get(i);
                if (field.equals(USERUUID)) {
                    stmt.setString(i + 1, ((User) app.get(USER)).getUuid());
                } else if (STRING_FIELDS.contains(field)) {
                    String value = (String) app.get(field);
                    if (null != value){
                        value = value.substring(0, Math.min(value.length(), MAX_STRING));
                    }
                    stmt.setString(i + 1, value);
                } else if (INT_FIELDS.contains(field)) {
                    stmt.setInt(i + 1, (int) app.get(field));
                } else {
                    throw new IllegalArgumentException("Cannot add field " + field + " not a recognized type");
                }
            }
            stmt.setInt(fields.size()+1, (int) app.get(ID)); // for the where clause
            stmt.execute();
        } catch (SQLException e) {
            throw new DatabaseException("saving your application", e);
        }

        for (AppPart model : AppPart.models.values()){
            String key = model.tableName();
            List<Map<String, Object>> modelObjects = (List<Map<String, Object>>) app.get(key);
            String delete = "DELETE from " + key + " where " + model.APPLICATION_ID + " = " + app.get(Application.ID);
            try {
                PreparedStatement stmt = conn.prepareStatement(delete);
                stmt.execute();
            } catch (SQLException e) {
                throw new DatabaseException("deleting old records", e);
            }
            for (Map<String, Object> record : modelObjects){
                model.create(record); // writes to DB
            }
        }
    }

    public static void update(Map<String, Object> app, Map<String, String[]> formData){
        int appId = (int) app.get(ID);
        for (String stringField : STRING_FIELDS){
            String[] values = formData.get(stringField);
            if (null != values && values.length > 0) {
                app.put(stringField, values[0]);
            }
        }
        for (String intField : INT_FIELDS){
            if (intField.equals(ID)){ // I know, its funny because there are no other fields.
                continue; //never update the ID.
            }
            String[] values = formData.get(intField);
            if (null != values && values.length > 0) {
                app.put(intField, Integer.parseInt(values[0])); // TODO catch NumberFormatException?
            }
        }
        for (AppPart model : AppPart.models.values()){
            String key = model.tableName();
            List<Map<String, Object>> modelObjects = model.fromAppForm(formData, appId);
            app.put(key, modelObjects);
        }
    }

    public static void delete(int id) throws DatabaseException {
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("DELETE FROM applications WHERE id = " + id);
        } catch (SQLException e) {
            throw new DatabaseException("Deleting your application", e);
        }

    }
}
