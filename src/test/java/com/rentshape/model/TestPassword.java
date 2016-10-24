package com.rentshape.model;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by matt on 10/23/16.
 */
public class TestPassword {
    @Test
    public void passwordWorks(){
        Password p = new Password("one");
        Assert.assertTrue(p.isPassword("one"));
        Assert.assertFalse(p.isPassword("two"));

        Password p2 = new Password("one");

        Assert.assertNotEquals(p.getSalt(), p2.getSalt());
        Assert.assertNotEquals(p.getHash(), p2.getHash());
    }
}
