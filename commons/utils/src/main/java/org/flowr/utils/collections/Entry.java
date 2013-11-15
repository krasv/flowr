/*******************************************************************************
 * Copyright (c) 2008 flowr.org - all rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License (EPL) v1.0. The EPL is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: flowR - initial API and implementation
 ******************************************************************************/

package org.flowr.utils.collections;

/**
 * plain Map.Entry implementation
 *
 * @author <a href="mailto:skrause@flowr.org">sven.krause</a>
 *
 * @param <K> key type
 * @param <V> value type
 *
 */
public class Entry<K, V> implements java.util.Map.Entry<K, V> {

   private K key;
   private V value;
   private Object[] items;

   public Entry(K key, V value, Object... items) {
      this.key = key;
      this.value = value;
      this.items = items;
   }

   @Override
   public K getKey() {
      return key;
   }

   @Override
   public V getValue() {
      return value;
   }

   /**
    * gets the items
    *
    * @return the items
    */
   public Object[] getItems() {
      return items;
   }

   public Object getItem(int index) {
      if (index >= 0 && index < items.length) {
         return items[index];
      }
      return null;
   }

   public V setValue(V value) {
      V oldValue = value;
      this.value = value;
      return oldValue;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((key == null) ? 0 : key.hashCode());
      result = prime * result + ((value == null) ? 0 : value.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (!(obj instanceof Entry< ? , ? >)) return false;
      Entry< ? , ? > other = (Entry< ? , ? >) obj;
      if (key == null) {
         if (other.key != null) return false;
      } else if (!key.equals(other.key)) return false;
      if (value == null) {
         if (other.value != null) return false;
      } else if (!value.equals(other.value)) return false;
      return true;
   };

}
