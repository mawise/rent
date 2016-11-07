package com.rentshape;

import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.HashMap;

import static spark.Spark.*;

/**
 * Created by matt on 11/7/16.
 */
public class TestApp {
    public static void main(String[] args) {
        get("/form", (req, res) -> {
            return new ModelAndView(new HashMap<>(), "test_form.hbs");
        }, new HandlebarsTemplateEngine());

        post("/form", (req, res) -> {
            String data = req.queryMap().toMap().toString();
            return data;
        });
    }
}
