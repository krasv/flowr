/*******************************************************************************
 * Copyright (c) 2008 flowr.org - all rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License (EPL) v1.0. The EPL is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: flowr.org - initial API and implementation
 ******************************************************************************/
package org.flowr.utils.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.flowr.utils.IFilter;

/**
 * @author krausesv
 * 
 * @param <T>
 *            node type
 * 
 */
public class Composite<T> implements Iterable<Composite<?>> {

	private T object;

	private Composite<?> parent;

	private List<Composite<?>> children = new ArrayList<Composite<?>>();
	
	public Composite(Composite<?> parent, T object) {
		this.parent = parent;
		this.object = object;
		if (parent != null) {
			parent.add(this);
		}
	}

	public Object getAdapter(Class<?> adapter) {
		if (adapter == null)
			throw new NullPointerException("adapter must not be null");

		T o = getObject();
		if (o != null && adapter.isAssignableFrom(o.getClass())) {
			return o;
		}
		return null;
	}

	/**
	 * gets the value object of the composite
	 * 
	 * @return
	 */
	public T getObject() {
		return object;
	}

	/**
	 * Changes the internal object. No notification is done!
	 * 
	 * @param o
	 */
	public void setObject(T o) {
		this.object = o;
	}

	/**
	 * gets the parent element
	 * 
	 * @return
	 */
	public Composite<?> getParent() {
		return parent;
	}

	private void add(Composite<?> child) {
		this.children.add(child);
		childAdded(child);
	}

	/**
	 * hook method to get notified, when a child has been added
	 * 
	 * @param child
	 */
	protected void childAdded(Composite<?> child) {
	};

	/**
	 * determines if the composite has children composite elements
	 * 
	 * @return
	 */
	public boolean hasChildren() {
		return !children.isEmpty();
	}

	/**
	 * gets the children composite list
	 * 
	 * @return array of children composite elements. must not be null, may be
	 *         empty
	 */
	public List<Composite<?>> getChildren() {
		return Collections.unmodifiableList(children);
	}
	
	public <CT extends Composite<?>> List<CT> getTypedChildren(Class<CT> childType) {
		List<CT> resultList = new ArrayList<CT>();
		for (Composite<?> child : getChildren()) {
			if( childType.isInstance(child)) {
				@SuppressWarnings("unchecked")
				CT childCT = (CT) child;
				resultList.add(childCT);
			}
		}
		return resultList;
	}

	/**
	 * gets the internal used list of children. Attention that modifying this
	 * list will not trigger change event notification
	 */
	protected List<Composite<?>> getChildrenList() {
		return children;
	}

	/**
	 * gets the next sibling of this composite, if the composite has a parent.
	 * 
	 * @return the next child of this composites parent
	 */
	public Composite<?> nextSibling() {
		if (parent == null) {
			return null;
		}
		try {
			ListIterator<Composite<?>> it = parent.children
					.listIterator(parent.children.indexOf(this));
			it.next();
			return it.next();
		} catch (IndexOutOfBoundsException e) {
			return null;
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	/**
	 * gets the previous sibling of this composite, if the composite has a
	 * parent.
	 * 
	 * @return the previous child of this composites parent
	 */
	public Composite<?> previousSibling() {
		if (parent == null) {
			return null;
		}
		try {
			ListIterator<Composite<?>> it = parent.children
					.listIterator(parent.children.indexOf(this));
			return it.previous();
		} catch (IndexOutOfBoundsException e) {
			return null;
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	/**
	 * gets the first child of this composite
	 * 
	 * @return
	 */
	public Composite<?> firstChild() {
		if (children != null && children.size() > 0) {
			return children.get(0);
		}
		return null;
	}

	/**
	 * gets the last child of this composite
	 * 
	 * @return
	 */
	public Composite<?> lastChild() {
		if (children != null && children.size() > 0) {
			return children.get(children.size() - 1);
		}
		return null;
	}

	/**
	 * determines the topmost composite element of the parent chain
	 * 
	 * @return
	 */
	public Composite<?> getRoot() {
		if (getParent() != null) {
			return getParent().getRoot();
		} else {
			return this;
		}
	}

	/**
	 * gets the path from this composite element to the root element
	 * 
	 * @return
	 */
	public Composite<?>[] getPath() {
		List<Composite<?>> l = new ArrayList<Composite<?>>();
		Composite<?> c = this;
		while (c != null) {
			l.add(0, c);
			c = c.getParent();
		}
		return l.toArray(new Composite[l.size()]);
	}

	/**
	 * gets an (read only) iteration through all elements of this composite and
	 * all childrens elements walking down the first child and its children and
	 * than the next child and so on. The first element of the iteration is this
	 * composite itself
	 * 
	 * @return
	 */
	public CompositeIterator<Composite<?>> elements(int level) {
		return new CompositeIteratorImpl(level);
	}

	public Iterator<Composite<?>> iterator() {
		return elements(0);
	}

	public <IT> Iterable<IT> asSingleTypeIterable() {
		final Iterator<Composite<?>> it = this.iterator();
		return new Iterable<IT>() {

			@Override
			public Iterator<IT> iterator() {
				return new Iterator<IT>() {

					@Override
					public boolean hasNext() {
						return it.hasNext();
					}

					@SuppressWarnings("unchecked")
					@Override
					public IT next() {
						return (IT) it.next();
					}

					@Override
					public void remove() {
						it.remove();
					}
				};
			}
		};
	}

	public static interface CompositeIterator<T> extends Iterator<T> {

		int level();
	}

	private class CompositeIteratorImpl implements
			CompositeIterator<Composite<?>> {

		/**
		 * @return the level
		 */
		public int level() {
			return nextLevel;
		}

		private final int level;

		private int currentChild;

		private CompositeIterator<Composite<?>> childIterator;

		private Composite<?> nextElement;

		private int nextLevel;

		CompositeIteratorImpl(int level) {
			this.level = level;
			nextElement = Composite.this;
			nextLevel = level;
			currentChild = -1;
			childIterator = null;
		}

		private Composite<?> determineNext() {
			Composite<?> retValue = this.nextElement;
			if (nextElement == Composite.this) {
				List<Composite<?>> childrenList = getChildren();
				if (childrenList != null && childrenList.size() > 0) {
					currentChild = 0;
					childIterator = childrenList.get(currentChild).elements(
							level + 1);
					nextElement = childrenList.get(currentChild);
					// skip next result, since it the current child again
					nextElement = childIterator.next();
					nextLevel = ((CompositeIteratorImpl) childIterator).level;
				} else {
					currentChild = -1;
					childIterator = null;
					nextElement = null;
					nextLevel = level;
				}
			} else if (childIterator != null && childIterator.hasNext()) {
				nextLevel = ((CompositeIteratorImpl) childIterator).level();
				nextElement = childIterator.next();
			} else if (currentChild != -1) {
				List<Composite<?>> childrenList = getChildren();
				if (currentChild < childrenList.size() - 1) {
					currentChild++;
					childIterator = childrenList.get(currentChild).elements(
							level + 1);
					nextElement = childrenList.get(currentChild);
					// skip next result, since it the current child again
					nextElement = childIterator.next();
					nextLevel = ((CompositeIteratorImpl) childIterator).level;
				} else {
					currentChild = -1;
					childIterator = null;
					nextElement = null;
					nextLevel = level;
				}
			}
			return retValue;
		}

		public boolean hasNext() {
			return nextElement != null;
		}

		public Composite<?> next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			return determineNext();
		}

		public void remove() {
			throw new UnsupportedOperationException("read only iteration"); //$NON-NLS-1$
		}

	}

	@Override
	@SuppressWarnings("nls")
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("Composite [ #children=");
		b.append(children.size());
		b.append(", isRoot=").append(getParent() == null);
		b.append(", object=").append(getObject());
		b.append("]");
		return b.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		T o = getObject();
		result = prime * result + ((o == null) ? 0 : o.hashCode());
		return result;
	}

}
