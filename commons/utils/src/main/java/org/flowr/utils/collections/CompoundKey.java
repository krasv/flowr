/*******************************************************************************
 * Copyright (c) 2008 flowr.org - all rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License (EPL) v1.0. The EPL is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: flowr.org - initial API and implementation
 ******************************************************************************/
package org.flowr.utils.collections;

import java.util.Arrays;

/**
 * key proxy class based on a companion of multiple key elements.
 *
 * @author <a href="mailto:skrause@flowr.org">sven.krause</a>
 *
 * @param <T> key type
 */
public class CompoundKey<T> {

   private final T[] objects;

   /**
    * value constructor
    *
    * @param objects key participants
    */
   public CompoundKey(T... objects) {
      this.objects = objects;
   }

   /**
    * gets the key element at the specific index.
    *
    * @param index
    * @return
    */
   public T getKey(int index) {
      return objects != null && objects.length >= index + 1 ? objects[index] : null;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + Arrays.hashCode(objects);
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      CompoundKey< ? > other = (CompoundKey< ? >) obj;
      if (!Arrays.equals(objects, other.objects)) return false;
      return true;
   }

}
