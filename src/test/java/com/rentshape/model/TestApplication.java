package com.rentshape.model;

import com.rentshape.exceptions.DatabaseException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by matt on 10/23/16.
 */
public class TestApplication {
    private static String DB_CONNECTION = "mysql://localhost:3306/";
    private static String DB_NAME = "rentshape";

    @BeforeClass
    public void connect() throws SQLException {
        Connection sqlConn = DriverManager.getConnection(
                "jdbc:" + DB_CONNECTION + DB_NAME,
                "root",
                "password");
        DbModel.setConnection(sqlConn);
    }

    @Test
    public void applicationsWriteToDb() throws SQLException, DatabaseException {

        User user = User.fromEmail("matt1@matt.com");
        Map<String, Object> app = Application.newApplication(user);
        Application.setString(app, Application.EMAIL, "matt1@matt.com");
        Application.create(app);


    }

    @Test
    public void applicationsGetFromDb() throws DatabaseException {
        User user = User.fromEmail("matt1@matt.com");
        Assert.assertNotNull(user.getUuid());
        List<Map<String, Object>> apps = Application.fromUser(user);
        Assert.assertEquals(apps.size(), 1);
        Map<String, Object> app = apps.get(0);
        Assert.assertEquals(app.get(Application.EMAIL), "matt1@matt.com");
        Assert.assertEquals(app.get(Application.USERUUID), user.getUuid());

        String phone = "123-544-1234";
        Application.setString(app, Application.CELLPHONE, phone);
        Application.save(app);

        apps = Application.fromUser(user);
        Assert.assertEquals(apps.size(), 1);
        app = apps.get(0);
        Assert.assertEquals(app.get(Application.CELLPHONE), phone);
    }
}
