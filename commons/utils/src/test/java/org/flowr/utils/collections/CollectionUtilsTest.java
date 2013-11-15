package org.flowr.utils.collections;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.flowr.utils.IFilter;
import org.flowr.utils.Operators;
import org.flowr.utils.collections.CollectionUtils.IContainerHandler;
import org.junit.Assert;
import org.junit.Test;

public class CollectionUtilsTest {
    
    @Test
    public void filterList() {
        List<String> data = Arrays.asList("Xantippe", "Qualle", "Auto", "Apfel", "Brücke", "Baum", "Boot", "Dreifach", "Dutzend");
        class F implements IFilter<String> {

            @Override
            public boolean accept(String s) {
                return s.startsWith("A");
            }
            
        };
        System.out.println(CollectionUtils.filter(data, new F()));
    }
    
    @Test
    public void testTreeMapNullKey() {
        Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                if(o1 == null) {
                    return o2 == null ? 0 : 1;
                }
                if(o2 == null) return -1;
                return o1.compareTo(o2);
            }
        });
        map.put(null, "NULL");
        Assert.assertNotNull(map.get(null));
    }

    @Test
    public void testGroupByKey() {
        List<String> data = Arrays.asList("Xantippe", "Qualle", "Auto", "Apfel", "Brücke", "Baum", "Boot", "Dreifach", "Dutzend");
        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.PRIMARY);
        CollectionUtils.GroupedComparator<Category, String> groupedComparator = new CollectionUtils.GroupedComparator<Category, String>() {
            
            @Override
            public int compare(Category key, String v1, String v2) {
                if(key != null && key.type == Category.Type.FirstLetter) {
                    return collator.compare(v1, v2);
                } else {
                    return 0 - collator.compare(v1, v2);
                }
            }
        };
        Comparator<Category> cc = new Comparator<Category>() {
            
            @Override
            public int compare(Category o1, Category o2) {
                return o1 == o2 ? 0 : o1 == null ? 1 : o2 == null ? -1 : o1.compareTo(o2);
            }
        };
        Map<Category, List<String>> map = CollectionUtils.groupByKey(data,
                new CollectionUtils.GroupCriteria<Category, String>() {

                    @SuppressWarnings("unchecked")
                    @Override
                    public Category[] getGroups(String value) {
                        if(Operators.EQUALS.in(value, "Xantippe", "Qualle")) {
                            return new Category[] { null, Category.firstLetter(value) , Category.length(value) };
                        } else {
                            return new Category[] { Category.firstLetter(value) , Category.length(value) };
                        }
                    }
                }, groupedComparator, cc);
        for (Category cat : map.keySet()) {
            System.out.println(cat + " --> " + map.get(cat));
        }
    }
    
    private static class Category implements Comparable<Category> {
        private static enum Type {
            FirstLetter, Length
        }
        
        public static Category firstLetter(String value) {
            return new Category(value.substring(0, 1), Category.Type.FirstLetter);
        }

        public static Category length(String value) {
            return new Category("" + value.length(), Category.Type.Length);
        }

        private final String value;

        private final Type type;

        public Category(String value, Type type) {
            super();
            this.value = value;
            this.type = type;
        }
        
        @Override
        public String toString() {
            return /*type.name().substring(0,1) + ":" + */  value;
        }

        @Override
        public int compareTo(Category o) {
            if(o == null) return 1;
            if(o.type == type) {
                return value.compareTo(o.value);
            } else {
                return type.ordinal() - o.type.ordinal();
            }
        }

    }
    
    @Test
    public void testFilterWithHandler() {
        IFilter<Node> filter = new IFilter<Node>(){

            @Override
            public boolean accept(Node object) {
                return object instanceof Leaf && ((Leaf)object).data.startsWith("A");
            }};
        Leaf la11 = new Leaf("A1_1");
        Leaf la21 = new Leaf("A2_1");
        Leaf lb11 = new Leaf("B1_1");
        Container c11 = new Container("c11", la11, la21, lb11);
        Container c1 = new Container("c1", c11);
        Leaf la12 = new Leaf("A1_2");
        Leaf la22 = new Leaf("A2_2");
        Leaf lb12 = new Leaf("B1_2");
        Container c2 = new Container("c2", la12, la22, lb12);
        Leaf la13 = new Leaf("A1_3");
        List<Node> list = Arrays.asList(c1, c2, la13);
        CompositeNodeHandler handler = new CompositeNodeHandler();
        
        List<Node> resultList = CollectionUtils.filter(list, filter, handler);
        
        System.out.println(resultList);
        Assert.assertTrue(resultList.contains(c1));
        Assert.assertTrue(resultList.contains(c2));
        Assert.assertTrue(resultList.contains(la13));
        List<Node> newC1Children = getNode(Container.class, c1, resultList).children;
        Assert.assertTrue(newC1Children.contains(c11));
        List<Node> newC11Children = getNode(Container.class, c11, newC1Children).children;
        Assert.assertTrue(getNode(Node.class, la11, newC11Children) != null);
        Assert.assertTrue(getNode(Node.class, la21, newC11Children) != null);
        Assert.assertTrue(getNode(Node.class, lb11, newC11Children) == null);
    }
    
    @SuppressWarnings("unchecked")
    private static <T extends Node> T getNode(Class<T> type, Node master, List<Node> nodes) {
        for (Node node : nodes) {
            if(node.getData().equals(master.getData()) && type.isInstance(node)) return (T) node;
        }
        return null;
    }
    
    class CompositeNodeHandler implements IContainerHandler<Node> {

        @Override
        public Node createFilteredContainer(Node t, List<Node> list) {
            return new Container(t.getData(), list);
        }

        @Override
        public List<Node> getChildren(Node t) {
            return ((Container)t).children;
        }

        @Override
        public boolean isContainer(Node t) {
            return t instanceof Container;
        }}
    
    private abstract class Node {
        abstract String getData();
    }
    
    private class Container extends Node {
        private final List<Node> children;
        private final String id;
        
        @Override
        public String getData() {
            return id;
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Container other = (Container) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (id == null) {
                if (other.id != null)
                    return false;
            } else if (!id.equals(other.id))
                return false;
            return true;
        }


        public Container(String id, Node ...children) {
            this.id = id;
            this.children = new ArrayList<Node>();
            for (Node node : children) {
                this.children.add(node);
            }
        }
        public Container(String id, List<Node> children) {
            this.id = id;
            this.children = children;
        }

        @Override
        public String toString() {
            return "Container [children=" + children + "]";
        }



        private CollectionUtilsTest getOuterType() {
            return CollectionUtilsTest.this;
        }
    }
    
    private class Leaf extends Node{
        String data;
        @Override
        public String getData() {
            return data;
        }
        public Leaf(String data) {
            this.data = data;
        }
        @Override
        public String toString() {
            return "Leaf [data=" + data + "]";
        }
    }
    

}
