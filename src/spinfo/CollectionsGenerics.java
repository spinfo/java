/** Copyright 2011 Fabian Steeg, University of Cologne, http://github.com/spinfo */

package spinfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.Test;

/** Generic data structures and algorithms: Java generics and collections. */
public class CollectionsGenerics {

  /* Generics */

  @Test
  public void basicGenerics() {
    /*
     * A List can contain different types of elements. We specify the type as a
     * parameter for the List class, e.g. Integer:
     */
    List<Integer> ints = Arrays.asList(1, 2, 3, 4, 5); // auto-boxed
    assertTrue(ints.get(0) instanceof Integer);
    /* Or String: */
    List<String> strings = Arrays.asList("one", "two");
    assertTrue(strings.get(0) instanceof String);
    /* Since List can be used with different types, it's called a generic class. */
  }

  @Test
  public void genericMethods() {
    /* As method used above, but implemented below: */
    List<String> s = asList(new String[] { "one", "two" });
    /* Optional here: explicit type parameter: */
    s = CollectionsGenerics.<String> asList(new String[] { "one", "two" });
    assertEquals("one", s.get(0));
    assertEquals("two", s.get(1));
  }

  /* A generic method: has a type parameter T (inferred or explicit) */
  private static <T> List<T> asList(T[] ts) {
    List<T> result = new ArrayList<T>(); // choose List impl. on creation
    for (T t : ts)
      result.add(t);
    return result;
  }

  @Test
  public void genericClasses() {
    /* Like for List above, we can also use generics on our classes: */
    Tree<String> tree = new Tree<String>();
    tree.root = new Node<String>("value");
    assertEquals("value", tree.root.value);
    assertTrue(tree.root.value instanceof String);
  }

  static class Tree<T> { // T becomes concrete on creation, e.g. String,
                         // Integer, etc.
    Node<T> root;
  }

  static class Node<T> {
    T value;
    Node<T> left;
    Node<T> right;

    public Node(T value) {
      this.value = value;
    }
  }

  /* Collections */

  @Test
  public void collections() {
    List<String> list = new ArrayList<String>(); // refer by interface
    Collection<String> coll = list; // List is a Collection
    Iterable<String> iter = coll; // Collection is Iterable
    assertTrue(iter instanceof Iterable);
    assertTrue(iter instanceof Collection);
    assertTrue(iter instanceof List);
    assertTrue(iter instanceof ArrayList);
    /* The Collection Interface defines 4 kinds of methods: */
    coll.add("hi"); // 1. methods for adding elements (also addAll, ...)
    coll.remove("hi"); // 2. methods for removing elements (also removeAll, ...)
    coll.contains("hi"); // 3. methods for querying (also containsAll, ...)
    coll.toArray(new String[0]); // 4. methods for conversion (iterator, ...)
  }

  @Test
  public void sets() {
    Set<String> set = new HashSet<String>(); // no duplicates, no order
    set = new TreeSet<String>(); // change impl: no duplicates, sorted, tree
    assertTrue(set instanceof Collection);
    assertTrue(set instanceof Set);
    assertTrue(set instanceof TreeSet);
    assertTrue(set instanceof SortedSet); // additional interface
    set.add("hi"); // O(1) for HashSet, O(log n) for TreeSet
    assertTrue(set.contains("hi")); // O(1) for HashSet, O(log n) for TreeSet
    set.add("hi"); // add existing value, should not be added
    assertEquals(1, set.size()); // no duplicates
  }

  @Test
  public void lists() {
    List<String> list = new ArrayList<String>(); // array-based impl.
    list = new LinkedList<String>(); // linked list impl.
    assertTrue(list instanceof Collection);
    assertTrue(list instanceof List);
    assertTrue(list instanceof LinkedList);
    assertTrue(list instanceof Deque); // additional interface
    list.add("hi"); // O(1) for ArrayList and LinkedList (add at end)
    String s = list.get(0); // O(1) for ArrayList, O(n) for LinkedList
    list.remove(0); // O(1) for LinkedList (front), O(n) for ArrayList
    assertEquals("hi", s);
  }

  @Test
  public void maps() {
    Map<String, Integer> map = new HashMap<String, Integer>(); // hash table
    map = new TreeMap<String, Integer>(); // change impl: sorted keys, tree
    assertTrue(map instanceof Map);
    assertTrue(map instanceof TreeMap);
    assertTrue(map instanceof SortedMap); // additional interface
    map.put("hi", 5); // O(1) for HashMap, O(log n) for TreeMap
    int i = map.get("hi"); // O(1) for HashMap, O(log n) for TreeMap
    assertEquals(5, i);
  }

  @Test
  public void algorithms() {
    /* Generic methods for working with collections, e.g. sorting and searching: */
    List<Integer> vals = Arrays.asList(91, 23, 88, 93, 20, 37);
    Collections.sort(vals); // merge sort, O(n log n)
    assertEquals(Arrays.asList(20, 23, 37, 88, 91, 93), vals);
    assertEquals(2, Collections.binarySearch(vals, 37)); // binsearch, O(log n)
    assertEquals(5, Collections.binarySearch(vals, 93));
  }

  @Test
  public void wrappers() {
    /* We can convert collections by passing them to the constructor: */
    List<String> list = Arrays.asList("one", "one", "two", "two");
    assertEquals(4, list.size());
    /* Remove duplicates by wrapping the list in a set: */
    Set<String> set = new HashSet<String>(list);
    assertEquals(2, set.size());
  }
}