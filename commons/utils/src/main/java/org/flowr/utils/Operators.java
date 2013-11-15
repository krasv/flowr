/*******************************************************************************
 * Copyright (c) 2008 flowr.org - all rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License (EPL) v1.0. The EPL is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: flowr.org - initial API and implementation
 ******************************************************************************/
package org.flowr.utils;

/**
 * @author SvenKrause
 *
 */
public abstract class Operators {

	/**
	 * a strategy defines the allowed parameter matching.
	 *
	 * @param <T>
	 *        the parameter type the strategy supports
	 */
	public abstract static class CompareStrategy<WHERE, WHAT> {
		abstract boolean matches(WHERE where, WHAT what);

		/**
		 * dedicates, if the parameter <code>what</code> is matching at least on of the parameters <code>where</code>.
		 *
		 * @param what
		 *        the element to compare
		 * @param where
		 *        the elements array the <code>what</code> parameter is to compare with
		 * @return <code>true</code>, if the parameter <code>what</code> matches one of the parameters <code>where</code>
		 */
		public boolean in(WHAT what, WHERE... where) {
			for (WHERE w : where) {
				if (matches(w, what)) { return true; }
			}
			return false;
		}

		/**
		 * dedicates, if the parameter <code>what</code> is matching none of the parameters <code>where</code>.
		 *
		 * @param what
		 *        the element to compare
		 * @param where
		 *        the elements array the <code>what</code> parameter is to compare with
		 * @return <code>true</code>, if the parameter <code>what</code> matches none of the parameters <code>where</code>
		 */
		public boolean notIn(WHAT what, WHERE... where) {
			return !in(what, where);
		}

	}


	/**
	 * strategy that compares the elements by equality.
	 */
	public static final CompareStrategy< ? super Object, ? super Object> EQUALS = new CompareStrategy< Object, Object>() {

		@Override
		boolean matches(Object a, Object b) {
			return JavaUtils.equals(a, b);
		}
	};

	/**
	 * strategy that compares the elements by instance.
	 */
	public static final CompareStrategy< ? super Object, ? super Object> SAME = new CompareStrategy< Object, Object>() {
		@Override
		public boolean matches(Object a, Object b) {
			return a == b;
		}
	};

	/**
	 * strategy that compares the elements by instanceof operator
	 */
	public static final CompareStrategy<Class< ? >, ? super Object> INSTANCEOF = new CompareStrategy<Class< ? >, Object>() {

		@Override
		boolean matches(Class< ? > where, Object what) {
			return where != null && where.isInstance(what);
		}

	};

	protected Operators() {};

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		Object a = new String("a");
		Object[] b = { new String("a"), "b", "c" };
		System.out.println(Operators.EQUALS.in("", ""));
		System.out.println(Operators.EQUALS.in(a, b));
		System.out.println(Operators.SAME.in(a, b));
		System.out.println(Operators.INSTANCEOF.in(new Object(), String.class));
	}

}
