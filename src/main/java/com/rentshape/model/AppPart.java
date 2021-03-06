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
 *
 * These are repeated elements in an application.  Banks, and cars and the like.
 */
public abstract class AppPart extends DbModel {
    public final static String ID = "id";

    public final static String APPLICATION_ID = "application_id";

    private final static int MAX_STRING = 250;

    public static final Map<String, AppPart> models = new HashMap<>();
    static {
        models.put("banks", new Bank());
    }

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
        return newFromApp((int) app.get(Application.ID));
    }
    public Map<String, Object> newFromApp(int appId) {
        Map<String, Object> record = new HashMap<>();
        record.put(APPLICATION_ID, appId);
        return record;
    }

    public Map<String, Object> fromId(int id) throws DatabaseException {
        List<Map<String, Object>> records = fromField(ID, id);
        if (records.size() > 0){
            return records.get(0);
        } else {
            return null;
        }
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

    public static boolean belongsToUser(Map<String, Object> record, User user) throws DatabaseException {
        if (null == user) {
            return false;
        }
        if (null == record.get(AppPart.APPLICATION_ID)) {
            return false;
        }
        int appId = (int) record.get(AppPart.APPLICATION_ID);
        Map<String, Object> app = Application.fromId(appId);
        if (null == app) {
            return false;
        }
        User recordUser = (User) app.get(Application.USER);
        if (null == recordUser) {
            return false;
        }
        return recordUser.getUuid().equals(user.getUuid());
    }

    public List<Map<String, Object>> fromAppForm(Map<String, String[]> formData, int appId){
        String key = tableName();
        List<Map<String, Object>> modelList = new ArrayList<>();
        for (int i=0; i<10; i++){ // max 10 entries
            String id = key + "-" + i;
            if (null != formData.get(id)){ // hidden marker for existance
                Map<String, Object> model = this.newFromApp(appId);
                for (String stringField : stringFields()){
                    String stringKey = id+"-"+stringField;
                    String[] values = formData.get(stringKey);
                    if (null != values && values.length > 0) {
                        model.put(stringField, values[0]);
                    }
                }
                for (String intField : intFields()){
                    if (intField.equals(ID)){ // I know, its funny because there are no other fields.
                        continue; //never update the ID.
                    }
                    String intKey = id+"-"+intField;
                    String[] values = formData.get(intKey);
                    if (null != values && values.length > 0) {
                        model.put(intField, Integer.parseInt(values[0])); // TODO catch NumberFormatException?
                    }
                }
                modelList.add(model);
            }
        }
        return modelList;
    }

}
