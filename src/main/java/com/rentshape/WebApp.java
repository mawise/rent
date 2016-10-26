package com.rentshape;

import com.rentshape.model.User;
import spark.ModelAndView;
import spark.Request;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class WebApp {

    private static String DB_CONNECTION = "mysql://localhost:3306/";
    private static String DB_NAME = "rentshape";

    public static void main(String[] args) throws SQLException {

        staticFileLocation("public");

        Connection sqlConn = DriverManager.getConnection(
                "jdbc:" + DB_CONNECTION + DB_NAME,
                "root",
                "password");
        User.setConnection(sqlConn);

        get("/hello", (req, res) -> "Hello World");

        /** Sign up */
        get("/", (req, res) -> {
            User user = loggedInUser(req);
            if (null != user){
                res.redirect("/user/home");
                return null;
            } else {
                return new ModelAndView(new HashMap<>(), "signup.hbs");
            }
        }, new HandlebarsTemplateEngine());

        post("/user/signin", (req, res) -> {
            String email = req.queryParams("email");
            String pass = req.queryParams("pass");
            User user = User.fromEmail(email);
            if (user != null && user.isPassword(pass)){
                user.resetToken();
                user.save();
                String token = user.getToken();
                res.cookie("token", token);
                res.redirect("/user/home");
                return null;
            }
            return new ModelAndView(new HashMap<>(), "signin.hbs");
        }, new HandlebarsTemplateEngine());

        post("/user/new", (req, res) -> {
            String email = req.queryParams("email");
            String pass = req.queryParams("pass");
            User user = new User(email, pass);
            user.create();
            String token = user.getToken();
            res.cookie("token", token);
            res.redirect("/user/home");
            return null;
        }); // form posts here, creates users

        post("/logout", (req, res) -> {
            User user = loggedInUser(req);
            if (null != user){
                user.resetToken();
                user.save();
            }
            res.redirect("/");
            return null;
        });

        get("/user/home", (req, res) -> {
            User user = loggedInUser(req);
            if (null == user){
                res.redirect("/");
                return null;
            }
            Map<String, String> data = new HashMap<>();
            data.put("email", user.getEmail());
            return new ModelAndView(data, "userhome.hbs");
        }, new HandlebarsTemplateEngine()); // user home page
        /*
        post("/user/:id"); // modify profile
        get("/user/:id/edit"); // edit profile form -> posts to /user/:id
        get("/user/:id/verify/:code"); // email or txt verification code goes here
*/

/*
        get("/application"); // index of a users apps
        get("/application/:id"); // view application,
        get("/application/:id/edit"); // edit form
        post("/application/:id"); // modify application
        post("/application/new"); // create empty app, then redirect to edit form

        get("/link"); //index of users links (with buttons to delete?)
        get("/link/:uuid"); // view a specified application via link
        get("/link/new"); // form to create a new link (application ID, link name)
        post("/link"); // create a new link (for posts here)
*/
    }

    public static User loggedInUser(Request req){
        try {
            String token = req.cookie("token");
            User user = User.fromToken(token);
            return user;
        } catch (Exception e){
            return null;
        }
    }
}
