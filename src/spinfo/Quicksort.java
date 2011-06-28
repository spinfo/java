package spinfo;
/** Copyright 2011 Fabian Steeg, University of Cologne, http://github.com/spinfo */
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Test;

/** Quicksort: a state of the art comparison-based sorting algorithm. */
public class Quicksort {

  /* For complexity discussion, see also insertion sort in SortSearch.java */
  /* For divide-and-conquer approach, see binary search in SortSearch.java */

  @Test
  public void sort() {
    List<Integer> vals = new ArrayList<Integer>(Arrays.asList(91, 23, 88, 93,
        20, 37));
    List<Integer> sort = sort(vals);
    assertEquals(Arrays.asList(20, 23, 37, 88, 91, 93), sort);
    System.out.println("Sorted: " + sort);
  }

  private List<Integer> sort(List<Integer> vals) {
    sort(vals, 0, vals.size() - 1); // start with full list
    return vals; // in-place algorithm, modifies vals
  }

  private void sort(List<Integer> vals, int left, int right) {
    if (left < right) { // if a section exists
      int p = /**/randomPartition/**/(vals, left, right); // divide at p
      sort(vals, left, p - 1); // conquer left of p
      sort(vals, p + 1, right); // conquer right of p
    }
  }

  private int partition(List<Integer> vals, int left, int right) {
    int x = vals.get(right); // pivot element (here: last element)
    int i = left - 1; // left .. i is <= x
    /* loop invariant: vals left of i are <= x, vals right of i are > x */
    for (int j = left; j <= right - 1; j++) { // i + 1 .. j - 1 is > x
      if (vals.get(j) <= x) { // current value belongs to first partition
        i++; // grow first partition, <= x
        swap(vals, i, j); // swap smaller value to first partition
      }
    }
    swap(vals, i + 1, right); // swap pivot to end of smaller partition
    return i + 1; // return pivot position
  }

  private void swap(List<Integer> vals, int a, int b) {
    int buf = vals.get(a); // buffer for the first val
    vals.set(a, vals.get(b)); // overwrite the first val with the second val
    vals.set(b, buf); // restore the first val at the position of the second val
  }

  /* Randomize pivot for improved average runtime for all inputs */

  Random r = new Random();

  private int randomPartition(List<Integer> vals, int left, int right) {
    int i = left + r.nextInt(right - left + 1); // map 0..x to left..right
    swap(vals, right, i); // place random val at end, will be pivot
    return partition(vals, left, right);
  }

}