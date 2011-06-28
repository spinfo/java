/** Copyright 2011 Fabian Steeg, University of Cologne, http://github.com/spinfo */
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

/** Sorting and searching: elementary algorithms. */
public class SortSearch {

	/* First elementary algorithmic problem: sorting */

	@Test
	public void insertionSort() {
		List<Integer> vals = new ArrayList<Integer>(Arrays.asList(91, 23, 88,
				93, 20, 37));
		List<Integer> sort = sort(vals);
		assertEquals(Arrays.asList(20, 23, 37, 88, 91, 93), sort);
		System.out.println("Sorted: " + sort);
	}

	private List<Integer> sort(List<Integer> vals) {
		/* loop invariant: now sorted elements, originally in vals[0..i-1] */
		List<Integer> sorted = new LinkedList<Integer>(vals.subList(0, 1));
		/* loop invariant init: contains first element of vals */
		for (int i = 1; i < vals.size(); i++) {
			int next = vals.get(i);
			int index = index(sorted, next);
			System.out.println(sorted + " <- " + next + " @ " + index);
			/* loop invariant maintenance: add one, sorted */
			sorted.add(index, next);
		}
		/* loop invariant termination: terminates when i==vals.size, full list */
		return sorted;
		/*
		 * run time depends on 1) input size, 2) how sorted input is -- best
		 * case: no inner loop, O(n) (here: reverse sorted), worst case: full
		 * inner loop, O(n^2) (here: already sorted).
		 */
	}

	private int index(List<Integer> res, int next) {
		int i = 0;
		while (i < res.size() && next > res.get(i)) {
			i++;
		}
		return i;
	}

	/* Second elementary algorithmic problem: searching */

	@Test
	public void linearSearch() {
		List<Integer> vals = Arrays.asList(20, 23, 37, 88, 91, 93);
		assertEquals(0, linearSearch(20, vals));
		assertEquals(1, linearSearch(23, vals));
		assertEquals(2, linearSearch(37, vals));
		assertEquals(3, linearSearch(88, vals));
		assertEquals(4, linearSearch(91, vals));
		assertEquals(5, linearSearch(93, vals));
		assertEquals(-1, linearSearch(100, vals));
	}

	private int linearSearch(int i, List<Integer> vals) {
		for (int j = 0; j < vals.size(); j++) {
			if (vals.get(j) == i)
				return j;
		}
		return -1;
	}

	/* A general algorithmic strategy: divide and conquer */

	private int factorial(int i) {
		/* factorial(0) == 1; factorial(5) == 1 * 2 * 3 * 4 * 5 */
		return i == 0 ? 1 : i * factorial(i - 1);
		/*
		 * We don't iterate but devide the problem into subproblems, and solve
		 * the overall problem by combining subsolutions: i * factorial(i-1)
		 */
	}

	@Test
	public void factorial() {
		assertEquals(1, factorial(0));
		assertEquals(1, factorial(1));
		assertEquals(2, factorial(2));
		assertEquals(120, factorial(5));
		assertEquals(3628800, factorial(10));
	}

	@Test(expected = StackOverflowError.class)
	public void factorialOverflow() {
		factorial(10000);
	}

	/* Recursive, divide-and-conquer binary search implementation */

	@Test
	public void binarySearchFound() {
		List<Integer> vals = Arrays.asList(20, 23, 37, 88, 91, 93);
		assertEquals(0, binarySearch(20, vals));
		assertEquals(1, binarySearch(23, vals));
		assertEquals(2, binarySearch(37, vals));
		assertEquals(3, binarySearch(88, vals));
		assertEquals(4, binarySearch(91, vals));
		assertEquals(5, binarySearch(93, vals));
		assertEquals(6,
				binarySearch(95, Arrays.asList(20, 23, 37, 88, 91, 93, 95)));
	}

	@Test
	public void binarySearchNotFound() {
		assertEquals(-1,
				binarySearch(100, Arrays.asList(20, 23, 37, 88, 91, 93)));
		assertEquals(-1,
				binarySearch(100, Arrays.asList(20, 23, 37, 88, 91, 93, 95)));
	}

	private int binarySearch(int i, List<Integer> vals) {
		return binarySearch(i, vals, 0, vals.size() - 1);
	}

	private int binarySearch(int i, List<Integer> vals, int left, int right) {
		if (left > right) {
			return -1;
		}
		int mid = left + (right - left) / 2;
		Integer val = vals.get(mid);
		if (val == i)
			return mid;
		if (val > i) { /* i in [left..m] */
			return binarySearch(i, vals, left, mid - 1);
		} else { /* i in [m..right] */
			return binarySearch(i, vals, mid + 1, right);
		}
	}

	/* Sort (and search) any type of objects with same principle: Comparable */

	@Test
	public void comparable() {
		List<Book> books = Arrays.asList(
		/**/
		new Book("Buddenbrooks", "Mann"),
		/**/
		new Book("Werther", "Goethe"),
		/**/
		new Book("Faust", "Goethe"));
		/* Sort first by author, second by title */
		List<Book> sort = sortBook(books);
		assertEquals(Arrays.asList(
		/**/
		new Book("Faust", "Goethe"),
		/**/
		new Book("Werther", "Goethe"),
		/**/
		new Book("Buddenbrooks", "Mann")), sort);
		System.out.println("Sorted: " + sort);
	}

	static class Book implements Comparable<Book> {

		private String title;
		private String author;

		public Book(String title, String author) {
			this.title = title;
			this.author = author;
		}

		@Override
		public int compareTo(Book that) { /* Define how to compare books */
			if (this.author.compareTo(that.author) == 0) {
				return this.title.compareTo(that.title); // second by title
			}
			return this.author.compareTo(that.author); // first by author
		}

		@Override
		public String toString() {
			return author + ": " + title; /* for useful output */
		}

		@Override
		public boolean equals(Object that) { /* for comparison in unit test */
			return that instanceof Book
					&& ((Book) that).author.equals(this.author)
					&& ((Book) that).title.equals(this.title);
		}

		@Override
		public int hashCode() { // use same values as in equals
			int result = 17;
			result = 31 * result + author.hashCode();
			result = 31 * result + title.hashCode();
			return result;
		}

	}

	/* changed here: Book instead of Integer (else as above) */
	private List<Book> sortBook(List<Book> vals) {
		List<Book> sorted = new LinkedList<Book>(vals.subList(0, 1));
		for (int i = 1; i < vals.size(); i++) {
			Book next = vals.get(i);
			int index = indexBook(sorted, next);
			System.out.println(sorted + " <- " + next + " @ " + index);
			sorted.add(index, next);
		}
		return sorted;
	}

	private int indexBook(List<Book> res, Book next) {
		int i = 0;
		/* changed here: use compareTo instead of == (else as above) */
		while (i < res.size() && next.compareTo(res.get(i)) > 0) {
			i++;
		}
		return i;
	}
}
