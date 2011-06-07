/** Copyright 2011 Fabian Steeg, University of Cologne, http://github.com/spinfo */

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/** Edit distance with recursion, memoization, and dynamic programming. */
public class EditDistance {

  /** Test the correctness of the different implementations. */

  @Test
  public void correctness() {
    runResultTest(new RecursiveEditDistance());
    runResultTest(new MemoizedEditDistance());
    runResultTest(new DynamicProgrammingEditDistance());
  }

  /** Test the performance of the different implementations. */

  @Test
  public void performance() {
    runPerformanceTest(new DynamicProgrammingEditDistance());
    runPerformanceTest(new MemoizedEditDistance());
    runPerformanceTest(new RecursiveEditDistance());
  }

  /** Edit distance interface: number of operations to change s1 into s2. */

  interface Edit {
    int distance(String s1, String s2);
  }

  /** Implementation based on simple recursion. */

  class RecursiveEditDistance implements Edit {
    private String s1;
    private String s2;

    @Override
    public int distance(final String s1, final String s2) {
      this.s1 = s1;
      this.s2 = s2;
      /* Overall problem: D(i,j) for i = |S1| and j = |S2|, i.e: */
      return distance(s1.length(), s2.length());
    }

    /* Distance of the first i chars in s1 to the first j chars in s2 */
    protected int distance(final int i, final int j) {
      /* Uncomment to see redundant sub-solution computation: */
      // System.out.println(String.format("Checking pair: %s, %s", i, j));
      /* "Base Condition": d(0,j) is j and d(i,0) is i */
      if (i == 0) {
        return j;
      }
      if (j == 0) {
        return i;
      }
      /* "Recurrence Relation" */
      if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
        return distance(i - 1, j - 1);
      }
      /* For each edit x: three recursive descents, i.e. exp. runtime: O(3^x) */
      int del = distance(i - 1, j) + 1;
      int ins = distance(i, j - 1) + 1;
      int rep = distance(i - 1, j - 1) + 1;
      return Math.min(del, Math.min(ins, rep));
    }
  }

  /** Implementation based on memoized recursion. */

  class MemoizedEditDistance extends RecursiveEditDistance {
    private Map<String, Integer> map = new HashMap<String, Integer>();

    @Override
    public int distance(final String s1, final String s2) {
      map.clear(); // forget memoized solution for new pair of strings
      return super.distance(s1, s2);
    }

    @Override
    protected int distance(final int i, final int j) {
      String pair = i + ", " + j;
      /* Only if we have not seen the pair before, we delegate to superclass: */
      if (!map.containsKey(pair)) {
        map.put(pair, super.distance(i, j));
      }
      return map.get(pair); // return the memoized sub-solution
    }
  }

  /** Implementation based on dynamic programming. */

  class DynamicProgrammingEditDistance implements Edit {
    @Override
    public int distance(final String s1, final String s2) {
      /* We fill the table once, i.e. linear runtime: O(i + 1 + j + 1) */
      int[][] table = new int[s1.length() + 1][s2.length() + 1];
      for (int i = 0; i < table.length; i++) {
        for (int j = 0; j < table[i].length; j++) {
          /* "Base Condition": d(0,j) is j and d(i,0) is i */
          if (i == 0) {
            table[i][j] = j;
          } else if (j == 0) {
            table[i][j] = i;
          } else {
            int del = table[i - 1][j] + 1;
            int ins = table[i][j - 1] + 1;
            int rep = table[i - 1][j - 1]
                + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1);
            table[i][j] = Math.min(del, Math.min(ins, rep));
          }
        }
      }
      /*
       * After having started "bottom" at 0,0, at the end we are "up" (at the
       * position indicating die distance of the full strings, at the lower
       * right corner of the table) and have our result: D(i, j):
       */
      return table[s1.length()][s2.length()];
    }
  }

  private void runResultTest(final Edit distance) {
    assertEquals(2, distance.distance("ehe", "reh"));
    assertEquals(2, distance.distance("eber", "leder"));
    assertEquals(0, distance.distance("ehe", "ehe"));
    assertEquals(0, distance.distance("", ""));
    assertEquals(1, distance.distance("ehe", "eher"));
    assertEquals(2, distance.distance("he", ""));
    assertEquals(2, distance.distance("", "he"));
    assertEquals(0, distance.distance("rechtschaffen", "rechtschaffen"));
  }

  private void runPerformanceTest(final Edit distance) {
    System.out.print("Running performance test for: "
        + distance.getClass().getSimpleName() + "...");
    long start = System.currentTimeMillis();
    for (int i = 0; i < 50; i++) {
      distance.distance("nacktschnecke", "rechtschaffen");
    }
    System.out.println(String.format(" %s ms.", System.currentTimeMillis()
        - start)); // typical result: 3, 200, 60000 ms. for rec., memo., dp
  }

}