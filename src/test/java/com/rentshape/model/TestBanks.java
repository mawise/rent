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
 * Created by matt on 11/3/16.
 */
public class TestBanks {
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
    public void readAndWrite() throws DatabaseException {
        Bank bankModel = new Bank();


        // reset banks
        List<Map<String, Object>> oldBanks = bankModel.fromAppId(2);
        for (Map<String, Object> bank : oldBanks){
            bankModel.delete(bank);
        }

        User user = User.fromEmail("matt1@matt.com");
        Map<String, Object> app = Application.fromUser(user).get(0);
        Map<String, Object> bank = bankModel.newFromApp(app);
        bank.put(Bank.NAME, "Fargo");
        bankModel.create(bank);

        Map<String, Object> dbBank = bankModel.fromAppId(2).get(0);
        Assert.assertEquals(dbBank.get(Bank.NAME), "Fargo");

        dbBank.put(Bank.ACCOUNT_NUMBER, "123");
        bankModel.save(dbBank);

        Map<String, Object> newDbBank = bankModel.fromApp(app).get(0);
        Assert.assertEquals(newDbBank.get(Bank.ACCOUNT_NUMBER), "123");
    }
}
