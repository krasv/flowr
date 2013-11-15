/*******************************************************************************
 * Copyright (c) 2008 flowr.org - all rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License (EPL) v1.0. The EPL is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: flowr.org - initial API and implementation
 ******************************************************************************/
package org.flowr.utils;

import java.lang.reflect.Field;
import java.util.Comparator;

/**
 * helper class to access java classes through various methods.
 * 
 * @author krausesv
 * 
 */
public final class JavaUtils {

    private JavaUtils() {
    }

    /**
     * null safe object equals
     * 
     * @param o1
     * @param o2
     * @return
     */
    public static boolean equals(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        } else if (o1 != null) {
            return o1.equals(o2);
        } else if (o2 != null) {
            return o2.equals(o1);
        }
        return false;
    }

    /**
     * Compares the given two objects by delegating the comparison to the objects, if at least one of the implements
     * {@link Comparable} and returns a {@link Object#toString()} comparison otherwise.
     * 
     * @param o1
     *            first object to compare
     * @param o2
     *            second object to compare
     * @return
     * @throws ClassCastException
     *             if the objects are not comparable.
     * @see Comparable#compareTo(Object)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static int compare(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 != null && o2 == null) {
            return 1;
        } else if (o1 == null && o2 != null) {
            return -1;
        }

        if (o1 instanceof Comparable) {
            Comparable c1 = (Comparable) o1;
            return c1.compareTo(o2);
        } else if (o2 instanceof Comparable) {
            Comparable c2 = (Comparable) o2;
            return -c2.compareTo(o1);
        }

        return o1.toString().compareTo(o2.toString());
    }

    /**
     * creates an Comparator instance delegating the compare invocation to the {@link #compare(Object, Object)} method.
     * 
     * @param <T>
     *            comparable type.
     * @return a {@link Comparator} instance
     */
    public static <T> Comparator<T> comparator() {
        return new Comparator<T>() {

            @Override
            public int compare(T o1, T o2) {
                return JavaUtils.compare(o1, o2);
            }
        };
    }

    private static Field locateField(Class<?> expectedType, Class<?> targetClass, String fieldName) {
        try {
            Field field = targetClass.getDeclaredField(fieldName);
            if (field != null) {
                field.setAccessible(true);
                if (expectedType.isAssignableFrom(field.getType())) {
                    return field;
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Class<T> expectedType, Object target, String fieldName) {
        try {
            Class<? extends Object> targetClass = target.getClass();
            while (targetClass != null) {
                Field field = locateField(expectedType, targetClass, fieldName);
                if (field != null) {
                    return (T) field.get(target);
                } else {
                    targetClass = targetClass.getSuperclass();
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T setFieldValue(Class<T> expectedType, Object target, String fieldName, T value) {
        try {
            Class<? extends Object> targetClass = target.getClass();
            while (targetClass != null) {
                Field field = locateField(expectedType, targetClass, fieldName);
                if (field != null) {
                    T oldValue = (T) field.get(target);
                    field.set(target, value);
                    return oldValue;
                } else {
                    targetClass = targetClass.getSuperclass();
                }
            } 
        } catch (Exception e) {
        }
        return null;
    }

}
