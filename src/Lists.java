/** Copyright 2011 Fabian Steeg, University of Cologne, http://github.com/spinfo */
import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

/** Lists: elementary data structures. */
public class Lists {

	/** Low-level, non-OOP list implementation simulating a tuple/record/struct. */
	@Test
	public void tuple() {
		/* Independent nodes: */
		Object[] first = new Object[2];
		Object[] second = new Object[2];
		Object[] third = new Object[2];
		/* Containing values: */
		first[0] = "first";
		second[0] = "second";
		third[0] = "third";
		/* Linked with pointers: */
		first[1] = second;
		second[1] = third;
		/* Can be traversed: */
		System.out.println("List traversal: ");
		Object[] current = first;
		while (current != null) {
			System.out.println(current[0]);
			current = (Object[]) current[1];
		}
	}

	/** OOP implementation of a queue, a FIFO list (first in, first out). */
	@Test
	public void queue() {
		Queue queue = new Queue();
		/* Enqueue at end: */
		queue.enqueue("first");
		queue.enqueue("second");
		queue.enqueue("third");
		/* Iterate: */
		System.out.println("Queue traversal: ");
		Node current = queue.first;
		while (current != null) {
			System.out.println(current.value);
			current = current.next;
		}
		/* Dequeue from front: */
		assertEquals("first", queue.dequeue());
		assertEquals("second", queue.dequeue());
		assertEquals("third", queue.dequeue());
		assertEquals(null, queue.dequeue());
	}

	/** A list element: wraps a value and a reference to the next element. */
	class Node {
		Object value;
		Node next;

		Node(Object value) {
			this.value = value;
		}
	}

	/** The queue class enforces the restricted FIFO access. */
	class Queue /**/implements Iterable<Object> /* like implements List *//**/{

		private Node first;
		private Node last;

		/** Add an object in constant time. */
		public void enqueue(Object value) {
			Node n = new Node(value);
			if (first == null) {
				first = n;
				last = first;
			} else {
				last.next = n;
				last = n;
			}
		}

		/** Get an object in constant time. */
		public Object dequeue() {
			if (first == null)
				return null;
			Object result = first.value;
			first = first.next;
			return result;
		}

		/**/
		@Override
		public Iterator<Object> iterator() {
			return new NodeIterator(first);
		}
		/**/
	}

	/** OOP implementation of a stack, a LIFO list (last in, first out). */
	@Test
	public void stack() {
		Stack stack = new Stack();
		/* Push on top, i.e. from front: */
		stack.push("first");
		stack.push("second");
		stack.push("third");
		/* Iterate: */
		System.out.println("Stack traversal: ");
		Node current = stack.first;
		while (current != null) {
			System.out.println(current.value);
			current = current.next;
		}
		/* Pop from top, i.e. from front: */
		assertEquals("third", stack.pop());
		assertEquals("second", stack.pop());
		assertEquals("first", stack.pop());
		assertEquals(null, stack.pop());
	}

	/** The stack class enforces the restricted LIFO access. */
	class Stack /**/implements Iterable<Object> /* like implements List *//**/{

		private Node first;

		/** Add an object in constant time. */
		public void push(Object value) {
			Node n = new Node(value);
			if (first == null) {
				first = n;
			} else {
				n.next = first;
				first = n;
			}
		}

		/** Get an object in constant time. */
		public Object pop() {
			if (first == null)
				return null;
			Object result = first.value;
			first = first.next;
			return result;
		}

		/**/
		@Override
		public Iterator<Object> iterator() {
			return new NodeIterator(first);
		}
		/**/
	}

	/** Common to both: linear order, both are iterable. */
	@Test
	public void list() {
		Queue list = new Queue();
		list.enqueue("first");
		list.enqueue("second");
		list.enqueue("third");
		System.out.println("Iterate using Iterator: ");
		Iterator<?> iterator = list.iterator();
		while (iterator.hasNext()) {
			System.out.println(iterator.next());
		}
		System.out.println("Iterate using Iterable: ");
		for (Object o : list) {
			System.out.println(o);
		}
	}

	/** A sequence can be traversed. */
	interface List {
		Iterator<?> iterator();
	}

	/** Representation of the stateful traversal. */
	interface SimpleIterator {
		Object next();

		boolean hasNext();
	}

	/** Iterator implementation based on linked nodes. */
	class NodeIterator implements Iterator<Object> /* like SimpleIterator */{
		private Node current;

		public NodeIterator(Node first) {
			if (first != null)
				current = first;
			else
				throw new IllegalArgumentException();
		}

		@Override
		public Object next() {
			if (current.value == null) {
				throw new NoSuchElementException();
			}
			Object next = current.value;
			current = current.next;
			return next;
		}

		@Override
		public boolean hasNext() {
			return current != null;
		}

		@Override
		public void remove() {
			throw new IllegalStateException("Not implemented");
		}

	}

	@Before
	public void line() {
		System.out.println();
	}

}
