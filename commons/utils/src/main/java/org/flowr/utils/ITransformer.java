/*******************************************************************************
 * Copyright (c) 2008 flowr.org - all rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License (EPL) v1.0. The EPL is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: flowr.org - initial API and implementation
 ******************************************************************************/
package org.flowr.utils;

/**
 * defines the ability to transform a value from type <i>From</i> into type <i>To</i>.
 *
 * @author krausesv
 *
 * @param <From>
 * @param <To>
 */
public interface ITransformer<From, To> {

   To transform(From from);

   Class< ? > fromClass();

   Class< ? > toClass();
}
