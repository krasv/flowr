/*******************************************************************************
 * Copyright (c) 2008 flowr.org - all rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License (EPL) v1.0. The EPL is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: flowr.org - initial API and implementation
 ******************************************************************************/

package org.flowr.utils.collections;

import org.flowr.utils.ITransformer;

/**
 * {@link ITransformer} implementation
 * 
 * @author krausesv
 * 
 * @param <From>
 * @param <To>
 */
public abstract class TransformerAdapter<From, To> implements
		ITransformer<From, To> {

	private Class<?> fromClass;
	private Class<?> toClass;

	public TransformerAdapter(Class<?> fromClass, Class<?> toClass) {
		this.fromClass = fromClass;
		this.toClass = toClass;
	}

	@Override
	public Class<?> fromClass() {
		return fromClass;

	}

	@Override
	public Class<?> toClass() {
		return toClass;
	}

	/**
	 * creates a String to Enum transformer
	 * 
	 * @param <E>
	 *            the supported EnumType
	 * @param enumType
	 *            the concrete enum type
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static <E extends Enum> ITransformer<String, E> string2EnumTransformer(
			final Class<E> enumType) {
		return new TransformerAdapter<String, E>(String.class, enumType) {

			@SuppressWarnings("unchecked")
			@Override
			public E transform(String from) {
				try {
					return from == null ? null : (E) Enum.valueOf(enumType,
							from);
				} catch (IllegalArgumentException e) {
					return null;
				}
			}
		};
	}

}
