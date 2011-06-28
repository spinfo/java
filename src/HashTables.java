/** Copyright 2011 Fabian Steeg, University of Cologne, http://github.com/spinfo */
import junit.framework.Assert;

import org.junit.Test;

/** Hash Tables: a useful, efficient data structure. */
public class HashTables {

  /* The basic idea: a direct-address table */

  @Test
  public void direct() {
    DirectAddressTable t = new DirectAddressTable();
    Person tom = new Person("tom");
    Person jim = new Person("jim");
    t.put(50, tom); // e.g. student ID = 50
    t.put(75, jim); // e.g. student ID = 75
    Assert.assertEquals(tom, t.get(50));
    Assert.assertEquals(jim, t.get(75));
  }

  static class DirectAddressTable {
    Object[] table = new Object[100]; // lots of space wasted

    public void put(int key, Object person) { // only numeric key supported
      table[key] = person;
    }

    public Object get(int key) {
      return table[key];
    }
  }

  static class Person {

    private String name;

    public Person(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  /* A simple hash table using chaining for collisions: */

  @Test
  public void hashed() {
    HashTable t = new HashTable();
    Person tom = new Person("tom");
    Person jim = new Person("jim");
    Person joe = new Person("joe");
    t.put(50, tom); // e.g. student ID = 50
    t.put(75, jim); // e.g. student ID = 75
    t.put(85, joe); // e.g. student ID = 85, hashes to same as 75 here
    Assert.assertEquals(tom, t.get(50));
    Assert.assertEquals(jim, t.get(75));
    Assert.assertEquals(joe, t.get(85));
  }

  static class HashTable {

    Element[] table = new Element[10]; // scale down

    static class Element {
      Element next;
      Object key; // key can be of any type
      Object value;

      public Element(Object key, Object value) {
        this.key = key;
        this.value = value;
      }
    }

    public void put(Object key, Object value) {
      Element newElement = new Element(key, value);
      int slot = hash(key);
      Element e = table[slot];
      table[slot] = newElement; // place new element in table
      /* Handle previous element in the slot with a different key: */
      if (e != null && !e.key.equals(newElement.key)) {
        newElement.next = e; // add new in front
      }
    }

    public Object get(Object key) {
      Element e = table[hash(key)];
      if (e == null) // no value in slot
        return null;
      /* Find element with correct key in list: */
      while (!(e.key.equals(key)) && e.next != null) {
        e = e.next;
      }
      return e.key.equals(key) ? e.value : null;
    }

    private int hash(Object key) {
      // simple demo hash: map key to table length
      if (key instanceof Integer) {
        return ((Integer) key) % table.length;
      }
      if (key instanceof String) {
        return ((String) key).length() % table.length;
      }
      return key.hashCode() % table.length;
    }
  }

  /* Hashing in practice: equality for custom objects */

  @Test
  public void equality() {
    Student s1 = new Student(5, "John", "Doe");
    Student s2 = new Student(8, "Jim", "Jones");
    Student s3 = new Student(8, "Jim", "Jones");
    Student s4 = new Student(5, "John", "Doe");
    /* hashCode has to be implemented consistent with equals: */
    Assert.assertEquals(s1, s4);
    Assert.assertEquals(s2, s3);
    Assert.assertEquals(s1.hashCode(), s4.hashCode());
    Assert.assertEquals(s2.hashCode(), s3.hashCode());
    Assert.assertFalse(s1.equals(s2));
    Assert.assertFalse(s1.hashCode() == s2.hashCode());
  }

  static class Student {
    int id;
    String first;
    String last;

    public Student(int id, String first, String last) {
      this.id = id;
      this.first = first;
      this.last = last;
    }

    @Override
    public String toString() {
      return String.format("%s %s (%s)", first, last, id);
    }

    @Override
    public int hashCode() { // use same values as in equals
      int result = 17;
      result = 31 * result + id;
      result = 31 * result + first.hashCode();
      result = 31 * result + last.hashCode();
      return result;
    }

    @Override
    public boolean equals(Object that) { // use same values as in hashCode
      return (that instanceof Student) && ((Student) that).id == this.id
          && ((Student) that).first.equals(this.first)
          && ((Student) that).last.equals(this.last);
    }
  }

  /* Hash table sample usage: counting words */

  @Test
  public void usage() {
    String text = "hi there hi everybody hi there again";
    HashTable t = count(text);
    Assert.assertEquals(3, t.get("hi"));
    Assert.assertEquals(2, t.get("there"));
    Assert.assertEquals(1, t.get("everybody"));
  }

  private HashTable count(String text) {
    HashTable t = new HashTable();
    String[] words = text.split(" ");
    for (String w : words) {
      Integer v = (Integer) t.get(w);
      if (v == null) // first occurrence
        v = 0;
      t.put(w, v + 1); // count up
    }
    return t;
  }
}