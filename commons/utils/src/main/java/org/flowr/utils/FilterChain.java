/*******************************************************************************
 * Copyright (c) 2008 flowr.org - all rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License (EPL) v1.0. The EPL is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: flowR - initial API and implementation
 ******************************************************************************/

package org.flowr.utils;

/**
 * Convenience class allowing to concatenate filters by logical operations.
 * <p>
 * usage:
 *
 * <pre>
 *  List&lt;String&gt; data = ...
 *  IFilter&lt;String&gt; f1 = ...
 *  IFilter&lt;String&gt; f2 = ...
 *  IFilter&lt;String&gt; f3 = ...
 *  ... CollectionUtils.filter(data,  FilterChain.and(f1, f2).or(f3));
 * </pre>
 *
 * </p>
 *
 * @author <a href="mailto:skrause@flowr.org">sven.krause</a>
 * @param <T> filter type
 */
public class FilterChain<T> implements IFilter<T> {

   /**
    * defines the ability to concatenate two logical operations being used as filter chain operator.
    *
    * @author <a href="mailto:skrause@flowr.org">sven.krause</a>
    *
    */
   public static interface IChainOperator {

      /** determines the initial operator state */
      boolean init(boolean state);

      /** compares the given values */
      boolean evaluate(boolean value1, boolean value2);

      /** determines, if the given state might be affected by further evaluate operations */
      boolean cont(boolean state);
   }

   @Override
   public boolean accept(T t) {
      boolean result = this.chainOperator.init(filters[0] == null || filters[0].accept(t));
      for (int i = 1; i < filters.length; i++) {
         result = chainOperator.evaluate(result, filters[i] == null || filters[i].accept(t));
         if (!chainOperator.cont(result)) {
            break;
         }

      }
      return result;
   }

   private IChainOperator chainOperator;
   private IFilter< ? super T>[] filters;

   /**
    * Constructs a FilterChain using the given filters and chain operation
    *
    * @param chainOperator the concatenation operation for the filters
    * @param filters the filters to use
    */
   public FilterChain(IChainOperator chainOperator, IFilter< ? super T>... filters) {
      this.chainOperator = chainOperator;
      this.filters = filters;
   }

   /**
    * logical AND filter concatenation
    */
   public static <T> FilterChain<T> andChain(IFilter< ? super T>... filters) {
      return new FilterChain<T>(AND, filters);
   }

   /**
    * logical OR filter concatenation
    */
   public static <T> FilterChain<T> orChain(IFilter< ? super T>... filters) {
      return new FilterChain<T>(OR, filters);
   }

   public FilterChain<T> or(IFilter< ? super T>... filters) {
      return concatenate(OR, filters);
   }

   public FilterChain<T> and(IFilter< ? super T>... filters) {
      return concatenate(AND, filters);
   }

   @SuppressWarnings("unchecked")
   public FilterChain<T> concatenate(IChainOperator operator, IFilter< ? super T>... filters) {
      @SuppressWarnings("rawtypes")
      IFilter[] array = new IFilter[filters.length + 1];
      array[0] = (IFilter< ? super T>) this;
      System.arraycopy(filters, 0, array, 1, filters.length);
      return new FilterChain<T>(operator, array);
   }

   /**
    * logical XOR filter concatenation
    */
   public static <T> FilterChain<T> xor(IFilter< ? super T>... filters) {
      return new FilterChain<T>(new XOR(), filters);
   }

   /**
    * logical AND operation (v1 && v2 && ... vn) return true, if all values are true
    */
   public static final IChainOperator AND = new IChainOperator() {

      @Override
      public boolean cont(boolean state) {
         return state;
      }

      @Override
      public boolean evaluate(boolean value1, boolean value2) {
         return value1 && value2;
      }

      @Override
      public boolean init(boolean state) {
         return state;
      }
   };

   /**
    * logical OR operation (v1 || v2 || ... vn) return true, if at least one value is true
    */
   public static final IChainOperator OR = new IChainOperator() {

      @Override
      public boolean cont(boolean state) {
         return !state;
      }

      @Override
      public boolean evaluate(boolean value1, boolean value2) {
         return value1 || value2;
      }

      @Override
      public boolean init(boolean state) {
         return state;
      }
   };

   /**
    *
    * logical XOR operation (v1 ^ v2 ^ ... vn) return true, if v1 == v2 ... == vn
    */
   public static class XOR implements IChainOperator {

      private boolean value;

      @Override
      public boolean cont(boolean state) {
         return state;
      }

      @Override
      public boolean evaluate(boolean value1, boolean value2) {
         return value == value2;
      }

      @Override
      public boolean init(boolean state) {
         this.value = state;
         return true;
      }
   };

}
