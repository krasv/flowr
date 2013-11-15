package org.flowr.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class JavaUtilsTest {

    @Test
    public void testGetFieldValue() {
        B b = new B();
        ValueType value = JavaUtils.getFieldValue(ValueType.class, b, "value");
        assertSame(ValueType.INSTANCE, value);
        ValueType staticValue = JavaUtils.getFieldValue(ValueType.class, new ValueType(), "INSTANCE");
        assertSame(ValueType.INSTANCE, staticValue);
        b.setData("test");
        assertEquals("test", b.getData());
        String oldValue = JavaUtils.setFieldValue(String.class, b, "data", "sample");
        assertEquals("test", oldValue);
        assertEquals("sample", b.getData());
    }
    
    public static class ValueType {
        private static ValueType INSTANCE = new ValueType();
    }
    
    public static class A {
        @SuppressWarnings("unused")
        private ValueType value = ValueType.INSTANCE; 
        private String data;
        public void setData(String data) {
            this.data = data;
        }
        
        public String getData() {
            return data;
        }
    }
    
    public static class B extends A {
    }
    

}
