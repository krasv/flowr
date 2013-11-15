/*******************************************************************************
 * Copyright (c) 2008 flowr.org - all rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License (EPL) v1.0. The EPL is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: flowr.org - initial API and implementation
 ******************************************************************************/
package org.flowr.utils.collections;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.flowr.utils.IFilter;
import org.flowr.utils.ITransformer;

/**
 * utilities method collections
 *
 * @author krausesv
 *
 */
public final class ArrayUtils {

   private ArrayUtils() {}

   @SuppressWarnings("unchecked")
   public static <T> T[] filter(T[] array, IFilter< ? super T> filter) {
      if (filter == null || array == null) return array;
      List<T> l = new ArrayList<T>();
      for (T t : array) {
         if (filter.accept(t)) {
            l.add(t);
         }
      }
      return l.toArray((T[]) Array.newInstance(array.getClass().getComponentType(), l.size()));

   }

   @SuppressWarnings("unchecked")
   public static <From, To> To[] transform(From[] from, ITransformer<From, To> transformer) {
      To[] toArray = (To[]) Array.newInstance(transformer.toClass(), from.length);
      for (int i = 0; i < from.length; i++) {
         toArray[i] = from[i] != null ? transformer.transform(from[i]) : null;
      }
      return toArray;
   }

   public static <From, To> To[] transformSkipNull(From[] from, ITransformer<From, To> transformer) {
      return transformSkipNull(from, transformer, null);
   }

   @SuppressWarnings("unchecked")
   public static <From, To> To[] transformSkipNull(From[] from, ITransformer<From, To> transformer, IFilter<To> filter) {
      List<To> result = new ArrayList<To>();
      for (int i = 0; i < from.length; i++) {
         if (from[i] != null) {
            To transformed = transformer.transform(from[i]);
            if (transformed != null && (filter == null || filter.accept(transformed))) {
               boolean filtered = false;
               if (filter != null) {
                  filtered = !filter.accept(transformed);
               }
               if (!filtered) {
                  result.add(transformed);
               }
            }
         }
      }
      To[] resultArray = (To[]) Array.newInstance(transformer.toClass(), result.size());
      for (int i = 0; i < result.size(); i++) {
         resultArray[i] = result.get(i);
      }
      return resultArray;
   }

   @SuppressWarnings("unchecked")
   public static <From, To> To[] transform(Collection<From> c, ITransformer<From, To> transformer) {
      To[] toArray = (To[]) Array.newInstance(transformer.toClass(), c.size());
      int i = 0;
      for (From from : c) {
         toArray[i] = from != null ? transformer.transform(from) : null;
         i++;
      }
      return toArray;
   }

   @SuppressWarnings("unchecked")
   public static <From, To> To[] transformSkipNull(Collection<From> c, ITransformer<From, To> transformer) {
      List<To> list = new ArrayList<To>();
      for (From from : c) {
         if (from != null) {
            final To to = transformer.transform(from);
            if (to != null) {
               list.add(to);
            }
         }
      }
      To[] toArray = (To[]) Array.newInstance(transformer.toClass(), list.size());
      int i = 0;
      for (To to : list) {
         toArray[i++] = to;
      }
      return toArray;
   }

   @SuppressWarnings("unchecked")
   public static <T> T[] subArray(T[] array, int start, int length) {
      int arrayLength = (length - start) + 1;
      T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), arrayLength);
      System.arraycopy(array, start, newArray, 0, arrayLength);
      return newArray;
   }

}
