package com.rentshape;

import com.rentshape.exceptions.DatabaseException;
import com.rentshape.exceptions.DuplicateUserException;
import com.rentshape.model.*;
import spark.ModelAndView;
import spark.Request;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;

public class WebApp {

    private static String DB_CONNECTION = "mysql://localhost:3306/";
    private static String DB_NAME = "rentshape";
    private static int THIRTY_DAYS = 2592000;
    private static final String NAME = "__name";

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
            data.put(NAME, user.getEmail());
            List<Map<String, Object>> apps = Application.fromUser(user);
            if (apps.size() > 0) {
                data.put("apps", apps);
            }
            return new ModelAndView(data, "userhome.hbs");
        }, new HandlebarsTemplateEngine()); // user home page

        // get("/user/:id/verify/:code"); // email or txt verification code goes here

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

        get("/application/:appid", (req, res) -> {
            User user = loggedInUser(req);
            if (null == user){
                res.redirect("/");
                return null;
            }
            int id = Integer.parseInt(req.params(":appid"));
            Map<String, Object> app = Application.fromId(id);
            if (null == app || !app.get(Application.USERUUID).equals(user.getUuid())){
                res.redirect("/user/home");
                return null;
            }
            app.put(NAME, user.getEmail());
            return new ModelAndView(app, "app.hbs");
        }, new HandlebarsTemplateEngine());

        post("/application/:appid", (req, res) -> {
            User user = loggedInUser(req);
            if (null == user){
                res.redirect("/");
                return null;
            }
            int id = Integer.parseInt(req.params(":appid"));
            Map<String, Object> app = Application.fromId(id);
            Application.update(app, req.queryMap().toMap());
            Application.save(app);
            res.redirect(req.pathInfo().toString());
            return null;
        }); // modify application

        get("/del_application/:appid", (req, res) -> {
            User user = loggedInUser(req);
            if (null == user){
                res.redirect("/");
                return null;
            }
            int id = Integer.parseInt(req.params(":appid"));
            Map<String, Object> app = Application.fromId(id);
            if (null == app || !app.get(Application.USERUUID).equals(user.getUuid())){
                res.redirect("/user/home");
                return null;
            }
            app.put(NAME, user.getEmail());
            return new ModelAndView(app, "delete_confirm.hbs");
        }, new HandlebarsTemplateEngine());

        post("/del_application/:appid", (req, res) -> {
            User user = loggedInUser(req);
            if (null == user){
                res.redirect("/");
                return null;
            }
            int id = Integer.parseInt(req.params(":appid"));
            Map<String, Object> app = Application.fromId(id);
            if (null == app || !app.get(Application.USERUUID).equals(user.getUuid())){
                res.redirect("/user/home");
                return null;
            }
            Application.delete(id);
            res.redirect("/user/home");
            return null;
        });

        Map<String, AppPart> models = new HashMap<>();
        models.put("bank", new Bank());


        for (String model : models.keySet()){
            post("/" + model + "/new", (req, res) -> {

            });
            get("/" + model + "/:id", (req, res) -> {

            });
            post("/" + model + "/:id", (req, res) -> {

            });
            
            post("/del_" + model + "/:id", (req, res) -> {
                User user = loggedInUser(req);
                if (null == user){
                    res.redirect("/");
                    return null;
                }
                int id = Integer.parseInt(req.params(":id"));
                Map<String, Object> record = models.get(model).fromId(id);
                if (! (null == record)){
                    int appId = (int) record.get(models.get(model).APPLICATION_ID);
                    Map<String, Object> app = Application.fromId(appId);
                    if (! (null == app)){
                        User appUser = (User) app.get(Application.USER);
                        if (appUser.getUuid().equals(user.getUuid())){
                            models.get(model).delete(id);
                            res.redirect("/application/:"+appId);
                        } else {
                            res.redirect("/user/home");
                        }
                    } else {
                        res.redirect("/user/home");
                    }
                } else {
                    res.redirect("/user/home");
                }
                return null;
            });
        }

/*
        get("/link"); //index of users links (with buttons to delete?)
        get("/link/:uuid"); // view a specified application via link
        get("/link/new"); // form to create a new link (application ID, link name)
        post("/link"); // create a new link (for posts here)
*/

        get("/error", (req, res) -> {
            User user = loggedInUser(req);
            Map<String, String> data = new HashMap<>();
            if (null != user){
                data.put(NAME, user.getEmail());
            }
            return new ModelAndView(data, "error.hbs");
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
