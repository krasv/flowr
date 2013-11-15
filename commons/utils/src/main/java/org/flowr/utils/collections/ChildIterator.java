/*******************************************************************************
 * Copyright (c) 2008 flowr.org - all rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License (EPL) v1.0. The EPL is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: flowr.org - initial API and implementation
 ******************************************************************************/
package org.flowr.utils.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author krausesv
 * 
 * @param <E> iteration element type
 * 
 */
public class ChildIterator<E> implements Iterator<E> {

   /**
    * defines the ability providing children for a given container.
    *
    * @author SvenKrause
    *
    * @param <T>
    */
   public static interface IChildProvider<E> {

      /**
       * determines the children iterable for the given container.
       * 
       * @param container the children's parent
       * @return an {@link Iterable} instance for the children. may not be null.
       */
      Iterable<E> getChildren(E container);
   }

   private IChildProvider<E> childrenProvider;
   private ChildIterator<E> childIterator;
   private Iterator<E> thisIterator;
   private E next;
   private boolean prepared = false;
   
   public static <E> Iterator<E> createIterator(IChildProvider<E> childrenProvider, E... elements) {
	   ArrayList<E> list = new ArrayList<E>();
	   list.addAll(Arrays.asList(elements));
	   return new ChildIterator<E>(childrenProvider, list);
   }

   /**
    * @param childrenProvider
    */
   public ChildIterator(IChildProvider<E> childrenProvider, Iterable<E> collection) {
      this.childrenProvider = childrenProvider;
      this.thisIterator = collection.iterator();
   }

   protected ChildIterator<E> newChildIterator(IChildProvider<E> childrenProvider, Iterable<E> iterable) {
      return new ChildIterator<E>(childrenProvider, iterable);
   }

   void determineNext() {
      prepared = true;
      if (childIterator == null) {
         if (thisIterator.hasNext()) {
            next = thisIterator.next();
            childIterator = newChildIterator(childrenProvider, childrenProvider.getChildren(next));
         } else {
            next = null;
         }
      } else {
         if (childIterator.hasNext()) {
            next = childIterator.next();
         } else {
            childIterator = null;
            determineNext();
         }
      }
   }

   @Override
   public E next() {
      if (!hasNext()) {
         throw new NoSuchElementException();
      }
      E element = next;
      determineNext();
      return element;
   }

   @Override
   public boolean hasNext() {
      if (!prepared) {
         determineNext();
      }
      return next != null;
   }

   @Override
   public void remove() {
      throw new UnsupportedOperationException("read only iterator");
   }

}
