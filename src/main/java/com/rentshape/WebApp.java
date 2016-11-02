package com.rentshape;

import com.rentshape.exceptions.DatabaseException;
import com.rentshape.exceptions.DuplicateUserException;
import com.rentshape.model.Application;
import com.rentshape.model.DbModel;
import com.rentshape.model.User;
import spark.ModelAndView;
import spark.Request;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;

public class WebApp {

    private static String DB_CONNECTION = "mysql://localhost:3306/";
    private static String DB_NAME = "rentshape";
    private static int THIRTY_DAYS = 2592000;

    public static void main(String[] args) throws SQLException {

        staticFileLocation("public");

        Connection sqlConn = DriverManager.getConnection(
                "jdbc:" + DB_CONNECTION + DB_NAME,
                "root",
                "password");
        DbModel.setConnection(sqlConn);

        get("/hello", (req, res) -> "Hello World");

        /** Home page with sign-up */
        get("/", (req, res) -> {
            User user = loggedInUser(req);
            if (null != user){
                res.redirect("/user/home");
                return null;
            } else {
                return new ModelAndView(new HashMap<>(), "signup.hbs");
            }
        }, new HandlebarsTemplateEngine());

        /** post the login form here */
        post("/", (req, res) -> {
            String email = req.queryParams("email");
            String pass = req.queryParams("pass");
            User user = User.fromEmail(email);
            if (user != null && user.isPassword(pass)){
                user.resetToken();
                user.save();
                String token = user.getToken();
                res.cookie("token", token, THIRTY_DAYS);
                res.redirect("/user/home");
                return null;
            }
            return new ModelAndView(new HashMap<>(), "signin.hbs");
        }, new HandlebarsTemplateEngine());

        /** post the registration form here */
        post("/user/new", (req, res) -> {
            String email = req.queryParams("email");
            String pass = req.queryParams("pass");
            User user = new User(email, pass);
            try {
                user.create();
            } catch (DuplicateUserException | DatabaseException e){
                Map<String, String> data = new HashMap<String, String>();
                data.put("error", e.getMessage());
                return new ModelAndView(data, "signup.hbs");
            }
            String token = user.getToken();
            res.cookie("token", token, THIRTY_DAYS);
            res.redirect("/user/home");
            return null;
        }, new HandlebarsTemplateEngine()); // form posts here, creates users

        /** post the logout button here */
        post("/logout", (req, res) -> {
            User user = loggedInUser(req);
            if (null != user){
                user.resetToken();
                user.save();
                for (String cookieKey : req.cookies().keySet()) {
                    res.removeCookie(cookieKey);
                }
            }
            res.redirect("/");
            return null;
        });

        /** home page for logged in user, index of applications */
        get("/user/home", (req, res) -> {
            User user = loggedInUser(req);
            if (null == user){
                res.redirect("/");
                return null;
            }
            Map<String, Object> data = new HashMap<>();
            data.put("email", user.getEmail());
            List<Map<String, Object>> apps = Application.fromUser(user);
            if (apps.size() > 0) {
                data.put("apps", apps);
            }
            return new ModelAndView(data, "userhome.hbs");
        }, new HandlebarsTemplateEngine()); // user home page

        // get("/user/:id/verify/:code"); // email or txt verification code goes here

        get("/application/:id", (req, res) -> {
            User user = loggedInUser(req);
            if (null == user){
                res.redirect("/");
                return null;
            }
            int id = Integer.parseInt(req.params(":id"));
            Map<String, Object> app = Application.fromId(id);
            if (null == app){
                System.out.println("App is null");
            }
            if (null == app || !app.get(Application.USERUUID).equals(user.getUuid())){
                System.out.println("app user uuid: " + app.get(Application.USERUUID) + " user: " + user.getUuid());

                res.redirect("/user/home");
                return null;
            }
            return new ModelAndView(app, "app.hbs");
        }, new HandlebarsTemplateEngine());

        post("/application/new", (req, res) -> {
            User user = loggedInUser(req);
            if (null == user){
                res.redirect("/");
                return null;
            }
            Map<String, Object> newApp = Application.newApplication(user);
            Application.create(newApp);
            res.redirect("/user/home");
            return null;
        }); // create empty app, then redirect to home page

/*
        get("/application"); // index of a users apps

        get("/application/:id/edit"); // edit form
        post("/application/:id"); // modify application


        get("/link"); //index of users links (with buttons to delete?)
        get("/link/:uuid"); // view a specified application via link
        get("/link/new"); // form to create a new link (application ID, link name)
        post("/link"); // create a new link (for posts here)
*/

        get("/error", (req, res) -> {
            return new ModelAndView(new HashMap<>(), "error.hbs");
        }, new HandlebarsTemplateEngine());


    //    get ("*", (req, res) -> {  // this causes static routes to be unavailable (like /css/*)
    //        res.redirect("/");
    //        return null;
    //    });

        exception(DatabaseException.class, (e, req, res) -> {
            e.printStackTrace();
            res.redirect("/error");
        });
    }

    public static User loggedInUser(Request req){
        try {
            String token = req.cookie("token");
            User user = User.fromToken(token);
            return user;
        } catch (Exception e) {
            return null;
        }
    }
}
