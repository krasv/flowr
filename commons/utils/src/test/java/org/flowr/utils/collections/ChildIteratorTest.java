/**
 * i.S.X. Software GmbH & Co KG.
 */
package org.flowr.utils.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author skrause
 *
 */
public class ChildIteratorTest {
	
	@Test
	public void testIterator() throws Exception {
		Composite<String> root = new Composite<String>(null, "root");
		Composite<String> node1 = new Composite<String>(root, "Node1");
		Composite<String> node1_1 = new Composite<String>(node1, "Node1_1");
		Composite<String> node2 = new Composite<String>(root, "Node2");
		Composite<String> node2_1 = new Composite<String>(node2, "Node2_1");
		
		@SuppressWarnings("unchecked")
		Iterator<Composite<String>> it = ChildIterator.createIterator(new ChildIterator.IChildProvider<Composite<String>>() {

			@Override
			public Iterable<Composite<String>> getChildren(Composite<String> container) {
				List<Composite<String>> result = new ArrayList<Composite<String>>();
				for (Composite<?> composite : container.getChildren()) {
					result.add((Composite<String>) composite);
				}
				return result;
			}
		}, node1, node2);
		
		List<Composite<String>> expected = new ArrayList<Composite<String>>();
		expected.add(node1);
		expected.add(node1_1);
		expected.add(node2);
		expected.add(node2_1);

		while(it.hasNext()) {
			expected.remove(it.next());
		}
		
		Assert.assertTrue("to less children returned " + expected.toString(), expected.isEmpty());
		
	}
}
