/*******************************************************************************
 * Copyright (c) 2008 flowr.org - all rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License (EPL) v1.0. The EPL is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: flowr.org - initial API and implementation
 ******************************************************************************/
package org.flowr.utils;

/**
 * defines the ability to decide, if a given object matches specific conditions.
 * 
 * @author krausesv
 * 
 * @param <T>
 */
public interface IFilter<T> {

   boolean accept(T object);

   /**
    * logical combination of IFilters.
    * 
    * @author krausesv
    * 
    */
   enum Junction {

      /**
       * true == all filters accepts
       */
      And {

         @Override
         public <T> IFilter<T> filter(final IFilter<T>... filters) {
            return new IFilter<T>() {

               @Override
               public boolean accept(T object) {
                  for (IFilter<T> filter : filters) {
                     if (filter != null && !filter.accept(object)) {
                        return false;
                     }
                  }
                  return true;
               }
            };
         }

      },

      /**
       * true == at least one filter accepts
       */
      Or {

         @Override
         public <T> IFilter<T> filter(final IFilter<T>... filters) {
            return new IFilter<T>() {

               @Override
               public boolean accept(T object) {
                  for (IFilter<T> filter : filters) {
                     if (filter == null || filter.accept(object)) {
                        return true;
                     }
                  }
                  return false;
               }
            };
         }
      },

      /**
       * true == if all filters deny or accept
       */
      Xor {

         @Override
         public <T> IFilter<T> filter(final IFilter<T>... filters) {
            return new IFilter<T>() {

               public boolean accept(T object) {
                  boolean first = false;
                  boolean performed = false;
                  for (IFilter<T> filter : filters) {
                     if (filter != null) {
                        if (!performed) {
                           performed = true;
                           first = filter.accept(object);
                        } else {
                           if (first != filter.accept(object)) {
                              return false;
                           }
                        }
                     }
                  }
                  return true;
               }
            };
         }
      };

      /**
       * join the given filters.
       * 
       * @param <T>
       * @param filters
       */
      public abstract <T> IFilter<T> filter(IFilter<T>... filters);
   }
}
