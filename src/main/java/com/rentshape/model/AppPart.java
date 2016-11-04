package com.rentshape.model;

import com.rentshape.exceptions.DatabaseException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by matt on 11/2/16.
 */
public abstract class AppPart extends DbModel {
    public final static String ID = "id";

    public final static String APPLICATION_ID = "application_id";

    private final static int MAX_STRING = 250;

    public AppPart(){}

    abstract String tableName();
    abstract List<String> stringFields();
    abstract List<String> intFields();

    public Map<String, Object> newFromApp(Map<String, Object> app){
        if (null == app.get(Application.ID)
                || null == app.get(Application.USER)
                || ! (app.get(Application.USER) instanceof User)) {
            throw new IllegalArgumentException("Cannot create a record without a valid application");
        }
        Map<String, Object> record = new HashMap<>();
        record.put(APPLICATION_ID, app.get(Application.ID));
        return record;
    }

    public List<Map<String, Object>> fromApp(Map<String, Object> app) throws DatabaseException {
        int id = (int) app.get(Application.ID);
        return fromAppId(id);
    }
    public List<Map<String, Object>> fromAppId(int id) throws DatabaseException {
        return fromField(APPLICATION_ID, id);
    }
    private List<Map<String, Object>> fromField(String field, Object value) throws DatabaseException {
        validateConnection();
        String query = "select * from " + tableName() + " where " + field + " = ?";
        List<Map<String, Object>> recordList = new ArrayList<>();
        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            if (value instanceof String) {
                stmt.setString(1, (String) value);
            } else if (value instanceof Integer){
                stmt.setInt(1, (int) value);
            } else {
                throw new IllegalArgumentException("Cannot get record by field " + field + " with " + value + " " + value.getClass().getCanonicalName());
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                Map<String, Object> record = new HashMap<>();
                for (String stringField : stringFields()){
                    record.put(stringField, rs.getString(stringField));
                }
                for (String intField : intFields()) {
                    record.put(intField, rs.getInt(intField));
                }
                int appId = rs.getInt(APPLICATION_ID);
                record.put(APPLICATION_ID, appId);
                int recordId = rs.getInt(ID);
                record.put(ID, recordId);
                recordList.add(record);
            }
            return recordList;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseException("querying the database", e);
        }
    }

    private List<String> fieldsToSave(Map<String, Object> app){
        List<String> fields = new ArrayList<>();
        for (String field : stringFields()){
            if (app.keySet().contains(field)){
                fields.add(field);
            }
        }
        for (String field : intFields()){
            if (app.keySet().contains(field)){
                fields.add(field);
            }
        }
        return fields;
    }

    private void addFieldsToSqlStatement(PreparedStatement stmt, List<String> fields, Map<String, Object> record) throws SQLException {
        for (int i = 0; i < fields.size(); i++) {
            String field = fields.get(i);
            if (field.equals(APPLICATION_ID)) {
                stmt.setInt(i + 1, (int) record.get(APPLICATION_ID));
            } else if (stringFields().contains(field)) {
                String value = (String) record.get(field);
                if (null != value){
                    value = value.substring(0, Math.min(value.length(), MAX_STRING));
                }
                stmt.setString(i + 1, value);
            } else if (intFields().contains(field)) {
                stmt.setInt(i + 1, (int) record.get(field));
            } else {
                throw new IllegalArgumentException("Cannot add field " + field + " not a recognized type");
            }
        }
    }

    /** create new record on the db */
    public void create(Map<String, Object> record) throws DatabaseException {
        if (null == record.get(APPLICATION_ID)){
            throw new IllegalArgumentException("Cannot create a record without belonging to an application");
        }
        List<String> fields = fieldsToSave(record);
        fields.add(APPLICATION_ID);
        StringBuilder createStatement = new StringBuilder();
        createStatement.append("INSERT into "+tableName()+" (");
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
            addFieldsToSqlStatement(stmt, fields, record);
            stmt.execute();
        } catch (SQLException e) {
            throw new DatabaseException("creating your record", e);
        }
    }

    /** save changes to existing record to the db */
    public void save(Map<String, Object> record) throws DatabaseException {
        if (null == record.get(APPLICATION_ID)){
            throw new IllegalArgumentException("Cannot save a record without belonging to an application");
        }
        if (null == record.get(ID)){
            throw new IllegalArgumentException("Cannot save an record that has no ID, must create it first");
        }
        List<String> fields = fieldsToSave(record);
        // don't add application ID because we'll never reassign it
        StringBuilder createStatement = new StringBuilder();
        createStatement.append("UPDATE "+tableName()+" set ");
        createStatement.append(fields.stream().map(f -> f + " = ?").collect(Collectors.joining(",")));
        createStatement.append(" where id = ?;");

        try {
            PreparedStatement stmt = conn.prepareStatement(createStatement.toString());
            addFieldsToSqlStatement(stmt, fields, record);
            stmt.setInt(fields.size()+1, (int) record.get(ID)); // for the where clause
            stmt.execute();
        } catch (SQLException e) {
            throw new DatabaseException("saving your record", e);
        }
    }

    public void update(Map<String, Object> app, Map<String, String[]> formData){
        for (String stringField : stringFields()){
            String[] values = formData.get(stringField);
            if (null != values && values.length > 0) {
                app.put(stringField, values[0]);
            }
        }
        for (String intField : intFields()){
            if (intField.equals(ID)){ // I know, its funny because there are no other fields.
                continue; //never update the ID.
            }
            String[] values = formData.get(intField);
            if (null != values && values.length > 0) {
                app.put(intField, Integer.parseInt(values[0])); // TODO catch NumberFormatException?
            }
        }
    }

    public void delete(Map<String, Object> record) throws DatabaseException {
        if (record.containsKey(ID) && record.containsKey(APPLICATION_ID)){
            delete((int) record.get(ID));
        } else {
            throw new IllegalArgumentException("Cannot delete, not a valid record: " + record.toString());
        }
    }
    public void delete(int id) throws DatabaseException {
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("DELETE FROM "+tableName()+" WHERE id = " + id);
        } catch (SQLException e) {
            throw new DatabaseException("Deleting your record", e);
        }

    }

}
