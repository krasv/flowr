/*******************************************************************************
 * Copyright (c) 2008 flowr.org - all rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License (EPL) v1.0. The EPL is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: flowr.org - initial API and implementation
 ******************************************************************************/
package org.flowr.utils.logging;

import java.text.MessageFormat;

import org.flowr.utils.collections.Composite;


/**
 * tracing helper class for dedicated method invocation protocol collection similar to TimeMeasurement by flowR.
 *
 * @author SvenKrause
 *
 */
public final class ExecutionStack {

	private static ThreadLocal< Handle > threadLocal = new ThreadLocal< Handle >();

	private ExecutionStack() {}

	/**
	 * stack element handle
	 */
	@SuppressWarnings("nls")
	public static final class Handle extends Composite< String > {

		private final Object[] args;

		private Handle(Composite< ? > parent, String object, Object... args) {
			super(parent, object);
			this.args = args;
		}

		/**
		 * declaring the execution segment completed
		 */
		public void done() {
			Handle parent = (Handle) getParent();
			if (parent != null) {
				threadLocal.set(parent);
			} else {
				threadLocal.remove();
			}
		}

		/**
		 * gets the execution trace as formated text tree.
		 *
		 * @return
		 */

		public String asQualifiedTree() {
			StringBuilder b = new StringBuilder();
			for (CompositeIterator< Composite< ? >> it = elements(0); it.hasNext();) {
				Handle h = (Handle) it.next();
				int level = h.getPath().length - 1;
				if (h.args != null && h.args.length > 0) {
					String fmt = MessageFormat.format(h.getObject(), h.args);
					String dump = MessageFormat.format("{0} {1}", lead(level), fmt);
					b.append(dump);
				} else {
					String dump = MessageFormat.format("{0} {1}", lead(level), h.getObject());
					b.append(dump);
				}
				b.append("\n");
			}
			return b.toString();
		}

		private static String lead(int count) {
			StringBuilder b = new StringBuilder();
			for (int i = 0; i < count; i++) {
				b.append("  ");
			}
			return b.toString();
		}

	}

	/**
	 * begins a new execution block.
	 *
	 * @param label
	 *        the blocks label pattern
	 * @param args
	 *        the label pattern arguments.
	 * @return the segement handle.
	 */
	public static Handle start(String label, Object... args) {
		Handle h = new Handle(threadLocal.get(), label, args);
		threadLocal.set(h);
		return threadLocal.get();
	}
}
