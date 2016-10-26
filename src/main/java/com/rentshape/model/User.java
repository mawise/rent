package com.rentshape.model;

import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import com.rentshape.exceptions.DatabaseException;
import com.rentshape.exceptions.DuplicateUserException;

import java.sql.*;
import java.util.Arrays;
import java.util.UUID;

public class User {

    static Connection conn;
    public static void setConnection(Connection conn){
        User.conn = conn;
    }
    public static void validateConnection(){
        if (null == User.conn){
            throw new RuntimeException("Connection is null, initialize connection before saving");
        }
    }

    public void validateUser(){
        for (Object obj : Arrays.asList(uuid, email, token, password_hash, password_salt)) {
            if (null == obj){
                throw new RuntimeException("User not valid");
            }
        }
    }



    public static User fromToken(String token) throws DatabaseException {
        return fromField("token", token);
    }

    public static User fromEmail(String email) throws DatabaseException {
        return fromField("email", email);
    }

    public static User fromUuid(String uuid) throws DatabaseException {
        return fromField("uuid", uuid);
    }

    private static User fromField(String field, String value) throws DatabaseException {
        validateConnection();
        String query = "select * from users where "+field+" = ?";
        try {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, value);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()){
                User user = new User();
                user.setToken(rs.getString("token"));
                user.setUuid(rs.getString("uuid"));
                user.setEmail(rs.getString("email"));
                user.password_hash = rs.getBytes("password_hash");
                user.password_salt = rs.getBytes("password_salt");
                return user;
            } else {
                return null; // TODO: use optional
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseException("querying the database", e);
        }
    }

    /**
     * add a new user to DB.  Assumes uuid not already in DB.
     */
    public void create() throws DatabaseException, DuplicateUserException {
        validateConnection();
        validateUser();
        String insertStatement = "INSERT INTO users (uuid, email, token, password_salt, password_hash) VALUES (?,?,?,?,?);";
        try {
            PreparedStatement stmt = conn.prepareStatement(insertStatement);
            stmt.setString(1, uuid);
            stmt.setString(2, email);
            stmt.setString(3, token);
            stmt.setBytes(4, password_salt);
            stmt.setBytes(5, password_hash);
            stmt.execute();
        } catch (MySQLIntegrityConstraintViolationException dupe){
            throw new DuplicateUserException(email);
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")){
                throw new DuplicateUserException(email);
            } else {
                throw new DatabaseException("creating your account", e);
            }
        }
    }

    /**
     * save existing user to DB.  Updates record with uuid.
     */
    public void save(){
        validateConnection();
        validateUser();
        String updateStatement = "UPDATE users set email = ?, token = ?, password_salt = ?, password_hash = ? where uuid = ?;";
        try {
            PreparedStatement stmt = conn.prepareStatement(updateStatement);
            stmt.setString(1, email);
            stmt.setString(2, token);
            stmt.setBytes(3, password_salt);
            stmt.setBytes(4, password_hash);
            stmt.setString(5, uuid);
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save to DB", e);
        }
    }

    private String uuid;
    private String email;
    private String token;
    private byte[] password_salt;
    private byte[] password_hash;

    public User() {
        token = UUID.randomUUID().toString();
        uuid = UUID.randomUUID().toString();
    }

    public User(String email, String password){
        this();
        this.email = email;
        setPassword(password);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void resetToken() {
        token = UUID.randomUUID().toString();
    }

    public byte[] getPassword_salt() {
        return password_salt;
    }

    public byte[] getPassword_hash() {
        return password_hash;
    }

    public void setPassword(String password){
        Password p = new Password(password);
        this.password_hash = p.getHash();
        this.password_salt = p.getSalt();
    }

    public boolean isPassword(String password){
        return new Password(password_salt, password_hash).isPassword(password);
    }
}
