package org.flowr.utils;

import org.junit.Assert;
import org.junit.Test;

public class PasswordStoreTest {
    
    @Test
    public void testStore() throws Exception {
        PasswordStore store = new PasswordStore("myPrivateKey");
        Assert.assertNotNull(store);
        
        String encrypted = store.encrypt("password");
        Assert.assertNotNull(encrypted);
        Assert.assertEquals("password", store.decryptPassword(encrypted));
    }

}
