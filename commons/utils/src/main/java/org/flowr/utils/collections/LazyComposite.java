/*******************************************************************************
 * Copyright (c) 2008 flowr.org - all rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License (EPL) v1.0. The EPL is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: flowr.org - initial API and implementation
 ******************************************************************************/
package org.flowr.utils.collections;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author krausesv
 * @param <T> the elements value type.
 * 
 */
public class LazyComposite<T> extends Composite<T> {

   private boolean loaded = false;
   private ReadWriteLock childAccessLock = new ReentrantReadWriteLock();

   /**
    * constructor
    * 
    * @param parent
    * @param object
    */
   public LazyComposite(Composite< ? > parent, T object) {
      super(parent, object);
   }

   @Override
   public List<Composite< ? >> getChildren() {
      ensureLoaded();
      return super.getChildren();
   }

   @Override
   public boolean hasChildren() {
      ensureLoaded();
      return super.hasChildren();
   }

   private void ensureLoaded() {
      childAccessLock.writeLock().lock();
      try {
         if (!loaded) {
            initializeChildren();
            loaded = true;
         }
      }
      finally {
         childAccessLock.writeLock().unlock();
      }
   }

   protected synchronized void releaseChilldren() {
      childAccessLock.writeLock().lock();
      try {
         getChildrenList().clear();
         loaded = false;
      }
      finally {
         childAccessLock.writeLock().unlock();
      }
   }

   /**
    * the hook method to initialize this composites children on first children access
    */
   protected void initializeChildren() {}

   @Override
   public String toString() {
      StringBuilder b = new StringBuilder();
      b.append("Composite [ #children=");
      if (loaded) {
         b.append(getChildren().size());
      } else {
         b.append("lazy: not initialized");
      }
      b.append(", isRoot=").append(getParent() == null);
      b.append(", object=").append(getObject());
      b.append("]");
      return b.toString();
   }

}
