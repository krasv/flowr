/**
 * 
 */
package org.flowr.utils.collections;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author skrause
 * 
 */
public class CompositeTest {

	private static class StringComposite extends Composite<String> {
		public StringComposite(Composite<?> parent, String object) {
			super(parent, object);
		}
	}

	/**
	 * Test method for
	 * {@link org.flowr.utils.collections.Composite#Composite(org.flowr.utils.collections.Composite, java.lang.Object)}
	 * .
	 */
	@Test
	public void testComposite() throws Exception {
		Composite<String> root = new Composite<String>(null, "Root");
		Assert.assertNotNull(root);
	}

	/**
	 * Test method for
	 * {@link org.flowr.utils.collections.Composite#getAdapter(java.lang.Class)}
	 * .
	 */
	@Test
	public void testGetAdapter() throws Exception {
		String sData = "String";
		Composite<String> c = new Composite<String>(null, sData);
		String adapter = c.getAdapter(String.class);
		Assert.assertEquals(sData, adapter);
		Assert.assertSame(sData, adapter);
	}

	/**
	 * Test method for
	 * {@link org.flowr.utils.collections.Composite#hasChildren()}.
	 */
	@Test
	public void testHasChildren() throws Exception {
		Composite<String> root = new Composite<String>(null, "Root");
		Composite<String> c1 = new Composite<String>(root, "C1");
		Assert.assertTrue(root.hasChildren());
		Assert.assertFalse(c1.hasChildren());
	}

	/**
	 * Test method for
	 * {@link org.flowr.utils.collections.Composite#getChildren()}.
	 */
	@Test
	public void testGetChildren() throws Exception {
		Composite<String> root = new Composite<String>(null, "Root");
		Composite<String> c1 = new Composite<String>(root, "C1");
		Composite<String> c2 = new Composite<String>(root, "C2");
		List<Composite<?>> children = root.getChildren();
		Assert.assertTrue(children.contains(c1));
		Assert.assertTrue(children.contains(c2));
	}

	/**
	 * Test method for
	 * {@link org.flowr.utils.collections.Composite#getAllChildren()}.
	 */
	@Test
	public void testGetAllChildren() throws Exception {
		Composite<String> root = new Composite<String>(null, "Root");
		Composite<String> c1 = new Composite<String>(root, "C1");
		Composite<String> c2 = new Composite<String>(root, "C2");
		Composite<String> c11 = new Composite<String>(c1, "C11");
		Composite<String> c12 = new Composite<String>(c1, "C12");
		Composite<String> c21 = new Composite<String>(c2, "C21");
		Composite<String> c22 = new Composite<String>(c2, "C22");
		List<Composite<?>> children = root.getAllChildren();
		Assert.assertTrue(children.contains(c1));
		Assert.assertTrue(children.contains(c2));
		Assert.assertTrue(children.contains(c11));
		Assert.assertTrue(children.contains(c12));
		Assert.assertTrue(children.contains(c21));
		Assert.assertTrue(children.contains(c22));
	}

	/**
	 * Test method for
	 * {@link org.flowr.utils.collections.Composite#getTypedChildren(java.lang.Class)}
	 * .
	 */
	@Test
	public void testGetTypedChildren() throws Exception {

		class LongComposite extends Composite<Long> {
			public LongComposite(Composite<?> parent, Long object) {
				super(parent, object);
			}
		}
		StringComposite root = new StringComposite(null, "Root");
		StringComposite c1 = new StringComposite(root, "C1");
		LongComposite c2 = new LongComposite(root, 01l);
		List<StringComposite> allTypedChildren = root.getTypedChildren(StringComposite.class);
		Assert.assertTrue(allTypedChildren.contains(c1));
		Assert.assertFalse(allTypedChildren.contains(c2));
	}

	/**
	 * Test method for
	 * {@link org.flowr.utils.collections.Composite#getAllTypedChildren(java.lang.Class)}
	 * .
	 */
	@Test
	public void testGetAllTypedChildren() throws Exception {

		class LongComposite extends Composite<Long> {
			public LongComposite(Composite<?> parent, Long object) {
				super(parent, object);
			}
		}
		StringComposite root = new StringComposite(null, "Root");
		StringComposite c1 = new StringComposite(root, "C1");
		StringComposite c2 = new StringComposite(root, "C2");
		LongComposite c11 = new LongComposite(c1, 011l);
		LongComposite c12 = new LongComposite(c1, 012l);
		LongComposite c21 = new LongComposite(c2, 021l);
		LongComposite c22 = new LongComposite(c2, 022l);
		List<StringComposite> allTypedChildren = root.getAllTypedChildren(StringComposite.class);
		Assert.assertTrue(allTypedChildren.contains(c1));
		Assert.assertTrue(allTypedChildren.contains(c2));
		Assert.assertFalse(allTypedChildren.contains(c11));
		Assert.assertFalse(allTypedChildren.contains(c12));
		Assert.assertFalse(allTypedChildren.contains(c21));
		Assert.assertFalse(allTypedChildren.contains(c22));
	}

	/**
	 * Test method for {@link org.flowr.utils.collections.Composite#getRoot()}.
	 */
	@Test
	public void testGetRoot() throws Exception {
		Composite<String> root = new Composite<String>(null, "Root");
		Composite<String> c1 = new Composite<String>(root, "C1");
		Composite<String> c11 = new Composite<String>(c1, "C11");
		Composite<String> c12 = new Composite<String>(c1, "C12");
		Composite<String> c2 = new Composite<String>(root, "C1");
		Composite<String> c21 = new Composite<String>(c2, "C21");
		Composite<String> c22 = new Composite<String>(c2, "C22");

		Assert.assertEquals(root, root.getRoot());
		Assert.assertEquals(root, c1.getRoot());
		Assert.assertEquals(root, c11.getRoot());
		Assert.assertEquals(root, c12.getRoot());
		Assert.assertEquals(root, c2.getRoot());
		Assert.assertEquals(root, c21.getRoot());
		Assert.assertEquals(root, c22.getRoot());
	}

	/**
	 * Test method for
	 * {@link org.flowr.utils.collections.Composite#sortChildren(java.util.Comparator)}
	 * .
	 */
	@Test
	public void testSortChildren() throws Exception {
		StringComposite root = new StringComposite(null, "root");
		StringComposite a1 = new StringComposite(root, "a1");
		StringComposite a2 = new StringComposite(root, "a2");
		StringComposite c1 = new StringComposite(root, "c1");
		StringComposite c2 = new StringComposite(root, "c2");
		StringComposite b1 = new StringComposite(root, "b1");
		StringComposite b2 = new StringComposite(root, "b2");

		root.sortChildren(new Comparator<Composite<?>>() {

			@Override
			public int compare(Composite<?> o1, Composite<?> o2) {
				StringComposite c1 = (StringComposite) o1;
				StringComposite c2 = (StringComposite) o2;
				return c1.getObject().compareTo(c2.getObject());
			}
		});
		Iterator<Composite<?>> it = root.getChildren().iterator();
		Assert.assertEquals(a1, it.next());
		Assert.assertEquals(a2, it.next());
		Assert.assertEquals(b1, it.next());
		Assert.assertEquals(b2, it.next());
		Assert.assertEquals(c1, it.next());
		Assert.assertEquals(c2, it.next());

	}

	/**
	 * Test method for
	 * {@link org.flowr.utils.collections.Composite#sortAllChildren(java.util.Comparator)}
	 * .
	 */
	@Test
	public void testSortAllChildren() throws Exception {

		StringComposite root = new StringComposite(null, "root");
		StringComposite a2 = new StringComposite(root, "a2");
		StringComposite a22 = new StringComposite(a2, "a22");
		StringComposite a21 = new StringComposite(a2, "a21");
		StringComposite a1 = new StringComposite(root, "a1");
		StringComposite a12 = new StringComposite(a1, "a12");
		StringComposite a11 = new StringComposite(a1, "a11");
		
		root.sortAllChildren(new Comparator<Composite<?>>() {

			@Override
			public int compare(Composite<?> o1, Composite<?> o2) {
				StringComposite c1 = (StringComposite) o1;
				StringComposite c2 = (StringComposite) o2;
				return c1.getObject().compareTo(c2.getObject());
			}
		});
		Iterator<Composite<?>> it = root.getAllChildren().iterator();
		Assert.assertEquals(root, it.next());
		Assert.assertEquals(a1, it.next());
		Assert.assertEquals(a11, it.next());
		Assert.assertEquals(a12, it.next());
		Assert.assertEquals(a2, it.next());
		Assert.assertEquals(a21, it.next());
		Assert.assertEquals(a22, it.next());
	}

}
