package org.flowr.utils;

import org.junit.Assert;
import org.junit.Test;

public class OperatorsTest {
  
  
  @SuppressWarnings("unchecked")
  @Test
  public void testEquals() {
    Object a = new String("a");
    Object[] b = { new String("a"), "b", "c" };

    Assert.assertTrue(Operators.EQUALS.in("", ""));
    Assert.assertTrue(Operators.EQUALS.in(a, b));
    Assert.assertFalse(Operators.EQUALS.in("", b));
    
    Assert.assertFalse(Operators.SAME.in(a, b));
    Assert.assertTrue(Operators.SAME.in(a, new Object[] {a}));

    Assert.assertFalse(Operators.INSTANCEOF.in(new Object(), String.class));
    Assert.assertTrue(Operators.INSTANCEOF.in("", String.class));
  }

}
