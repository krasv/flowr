/*******************************************************************************
 * Copyright (c) 2008 flowr.org - all rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License (EPL) v1.0. The EPL is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: flowr.org - initial API and implementation
 ******************************************************************************/
package org.flowr.utils;

import java.util.Arrays;

/**
 * helper class providing various string manipulation and inspection methods.
 *
 * @author krausesv
 *
 */
public final class StringUtils {

   private StringUtils() {}

   /**
    * gets the substring of the given text reduced by the given leading part.
    *
    * @param text
    * @param leadingPart
    * @return the reduced substring, if the text starts with the given leading part; otherwise the incoming, unmodified
    *         text.
    */
   public static String subString(String text, String leadingPart) {
      if (text.startsWith(leadingPart)) {
         return text.substring(leadingPart.length());
      } else {
         return text;
      }
   }

   /**
    * gets the substring of the given text reduced by the number of leading and tailing characters.
    *
    * @param text the String to reduce
    * @param lead number of characters to remove at the leading end
    * @param tail number of characters to remove at the tailing end
    * @return the reduced substring, reduced by <code>lead</code> chars at the begin and <code>tail</code> chars at the
    *         end.
    * @throws StringIndexOutOfBoundsException
    */
   public static String subString(String text, int lead, int tail) {
      return text.substring(lead, text.length() - tail);
   }

   /**
    * gets the lead part of the given text <b>before</b> the given separator.
    *
    * @param text
    * @param separator
    * @return the leading part, if the separator exists within the given text; otherwise the incoming, unmodified text
    */
   public static String lead(String text, String separator) {
      int pos = text.indexOf(separator);
      return pos != -1 ? text.substring(0, pos) : text;
   }

   /**
    * gets the tail part of the given text <b>after</b> the given separator.
    *
    * @param text
    * @param separator
    * @return the tailing part, if the separator exists within the given text; otherwise the incoming, unmodified text.
    */
   public static String tail(String text, String separator) {
      int pos = text.lastIndexOf(separator);
      return pos != -1 ? text.substring(pos + 1) : text;
   }

   /**
    * separates the given string at the given separator, but skips empty leading and/or tailing parts.
    *
    * @param s
    * @param separator
    * @return
    */
   public static String[] split(String s, String separator) {
      if (s.startsWith(separator)) {
         s = subString(s, separator);
      }
      if (s.endsWith(separator)) {
         s = tail(s, separator);
      }
      return s.split(separator);
   }

   /**
    * determines, if the given string is empty, but accepts blanks as valid characters.
    *
    * @param s
    * @return
    */
   public static boolean isEmpty(String s) {
      return s == null || s.length() == 0;
   }

   /**
    * determines, if the given string is empty, but ignores leading and tailing blanks.
    *
    * @param s
    * @return
    */
   public static boolean isEmptyTrim(String s) {
      return s == null || s.trim().length() == 0;
   }

   /**
    * determines, if the given strings are equal
    *
    * @param value
    * @param attribute
    * @return
    */
   public static boolean equals(String s1, String s2) {
      if (s1 == null && s2 == null) return true;
      if (s1 != null && s2 != null) {
         return s1.equals(s2);
      } else {
         return false;
      }
   }

   /**
    * Creates a string repeating the given character <code>c</code> <code>count</code> times.
    *
    * @param c char to repeat
    * @param count number of repetitions
    */
   public static String fill(char c, int count) {
      if (count <= 0) return null;
      char[] cArray = new char[count];
      Arrays.fill(cArray, c);
      return new String(cArray);
   }

   /**
    * Joins a list of string to a new one using the given separator
    *
    * @param array
    * @param separator
    * @return
    */
   public static String join(String[] array, String separator) {
      StringBuffer buffer = new StringBuffer();
      for (int i = 0; i < array.length;) {
         buffer.append(array[i]);
         if (++i < array.length) buffer.append(separator);
      }
      return new String(buffer);
   }

   /**
    * Make the first {@link Character} of a given {@link String} upper case
    *
    * @param string the {@link String} to convert
    * @return copied {@link String} with the first Character upper case
    */
   public static String firstToUpperCase(String string) {
      String result = string;
      if (string != null && !string.isEmpty()) {
         char first = string.charAt(0);
         first = Character.toUpperCase(first);
         result = Character.toString(first);
         if (string.length() > 1) {
            return result + string.substring(1);
         }
      }
      return result;
   }

   /**
    * Make the first {@link Character} of a given {@link String} lower case
    *
    * @param string the {@link String} to convert
    * @return copied {@link String} with the first Character lower case
    */
   public static String firstToLowerCase(String string) {
      String result = string;
      if (string != null && !string.isEmpty()) {
         char first = string.charAt(0);
         first = Character.toLowerCase(first);
         result = Character.toString(first);
         if (string.length() > 1) {
            return result + string.substring(1);
         }
      }
      return result;
   }
}
