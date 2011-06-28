package spinfo;
/** Copyright 2011 Fabian Steeg, University of Cologne, http://github.com/spinfo */
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

/**
 * Basic indexing and preprocessing with regular expressions. Requires file
 * "pg100.txt", The Complete Works of William Shakespeare
 * (http://www.gutenberg.org/ebooks/100.txt.utf8)
 */
public class Index {

  /* Before indexing, we need to determine what elements to index. */

  private static final Preprocessor PREPROCESSOR = new Preprocessor();

  @Test
  public void tokenization() {
    assertEquals(asList("hello", "world"), process("hello, world!"));
    assertEquals(asList("123", "test"), process("test 123, 123 test, test"));
    assertEquals(asList("0221-123123", "test"), process("0221-123123, test"));
    assertEquals(asList("123", "köln", "test"), process("test - köln - 123"));
  }

  private List<String> process(String string) {
    // some wrapping for the tests (compare with sorted list)
    return new ArrayList<String>(new TreeSet<String>(
        PREPROCESSOR.tokenize(string)));
  }

  @Test
  public void patterns() {
    assertTrue("0221-470".matches(SpecialCase.COMPOUND.regex));
    assertTrue(!"Meine Nummer: 0221-470.".matches(SpecialCase.COMPOUND.regex));
    assertTrue(!"4711".matches(SpecialCase.COMPOUND.regex));
    assertTrue(!"Daimler-Benz".matches(SpecialCase.COMPOUND.regex));
    assertTrue("8.04".matches(SpecialCase.COMPOUND.regex));
    assertTrue("15:10".matches(SpecialCase.COMPOUND.regex));
    assertTrue("3,50".matches(SpecialCase.COMPOUND.regex));
    assertTrue("fabian.steeg@uni-koeln.de".matches(SpecialCase.EMAIL.regex));
    assertTrue("fsteeg@spinfo.uni-koeln.de".matches(SpecialCase.EMAIL.regex));
    assertTrue(!"fabian@home".matches(SpecialCase.EMAIL.regex));
  }

  /*
   * Available patterns for extraction. Uses enum instead of constants to
   * iterate over all patterns in constructor of Preprocessor.
   */
  enum SpecialCase {
    /* Phone (0221-4701751), versions (8.04), money (3,50) and time (15:15) */
    COMPOUND("\\d+[-.,:]\\d+"),
    /* Simple numbers */
    NUMBER("\\d+"),
    /* Some simple email adresses */
    EMAIL("[^@\\s]+@.+?\\.(de|com|eu|org|net)");

    String regex;

    SpecialCase(final String regularExpression) {
      this.regex = regularExpression;
    }
  }

  /**
   * A preprocessor based on regular expressions: first extracts custom
   * patterns, then splits on a given delimiter.
   */
  static class Preprocessor {
    /* Unicode-aware "non-letter" delimiter, ASCII version is \\W */
    private static final String UNICODE_AWARE_DELIMITER = "[^\\p{L}]";
    private List<SpecialCase> specialCases = new ArrayList<SpecialCase>();
    private String delimiter;

    public Preprocessor() {
      delimiter = UNICODE_AWARE_DELIMITER;
      for (SpecialCase p : SpecialCase.values()) {
        specialCases.add(p);
      }
    }

    public List<String> tokenize(final String input) {
      String text = input.toLowerCase();
      List<String> result = new ArrayList<String>();
      text = extractSpecialCases(text, result);
      tokenizeStandard(text, result);
      return result;
    }

    private String extractSpecialCases(String text, List<String> result) {
      for (SpecialCase p : specialCases) {
        Pattern pattern = Pattern.compile(p.regex);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
          String group = matcher.group();
          result.add(group); // add special case
          text = text.replace(group, ""); // don't treat group as regex
        }
      }
      return text;
    }

    private void tokenizeStandard(String text, List<String> result) {
      List<String> list = Arrays.asList(text.split(delimiter));
      for (String s : list)
        if (s.trim().length() > 0) // filter empty strings
          result.add(s.trim());
    }

  }

  /* Once we can preprocess our corpus, we can build an index and search it: */

  private static final InvertedIndex INDEX = buildIndex();

  /** Test searching the corpus for a single term. */
  @Test
  public final void testSearch() throws MalformedURLException, IOException {
    long start = System.currentTimeMillis();
    String query = "Brutus";
    Set<Integer> list = INDEX.search(query);
    System.out.printf("Result for '%s': %s, took %s ms.\n", query, list,
        (System.currentTimeMillis() - start));
    Assert.assertTrue("Search should find a single term", list.size() > 0);
  }

  /** Test searching the corpus for multiple search terms. */
  @Test
  public final void testMulti() throws MalformedURLException, IOException {
    long start = System.currentTimeMillis();
    String query = "Brutus Caesar"; // = Brutus AND Caesar
    Set<Integer> list = INDEX.search(query);
    System.out.printf("Result for '%s': %s, took %s ms.\n", query, list,
        (System.currentTimeMillis() - start));
    Assert.assertTrue("Search should find multiple terms", list.size() > 0);
  }

  static class InvertedIndex {

    private Map<String, SortedSet<Integer>> index = new HashMap<String, SortedSet<Integer>>();

    public InvertedIndex(final List<String> corpus) {
      index = index(corpus);
    }

    private Map<String, SortedSet<Integer>> index(final List<String> works) {
      Map<String, SortedSet<Integer>> index = new HashMap<String, SortedSet<Integer>>();
      // for each document, and each of its token, add it to the index
      for (int i = 0; i < works.size(); i++) {
        List<String> tokens = PREPROCESSOR.tokenize(works.get(i));
        for (String token : tokens) {
          SortedSet<Integer> postings = index.get(token);
          if (postings == null) { // first time
            postings = new TreeSet<Integer>();
            index.put(token, postings);
          }
          postings.add(i); // document i contains token
        }
      }
      return index;
    }

    public Set<Integer> search(final String query) {
      /* We treat all entries as AND-linked... */
      List<String> queries = PREPROCESSOR.tokenize(query);
      /* We get the results for each query term: */
      List<SortedSet<Integer>> allPostings = new ArrayList<SortedSet<Integer>>();
      for (String q : queries) {
        SortedSet<Integer> postings = index.get(q);
        if (postings != null)
          allPostings.add(postings);
      }
      /* For efficient intersection computation: sort lists by length */
      sortByLength(allPostings);
      /* Intersection of postings for all query terms is our result: */
      return intersectionOf(allPostings);
    }

    private void sortByLength(List<SortedSet<Integer>> all) {
      Collections.sort(all, new Comparator<SortedSet<Integer>>() {
        public int compare(final SortedSet<Integer> o1,
            final SortedSet<Integer> o2) {
          return Integer.valueOf(o1.size()).compareTo(o2.size());
        }
      });
    }

    private Set<Integer> intersectionOf(List<SortedSet<Integer>> all) {
      /* The result set is the intersection of the first list with all others: */
      SortedSet<Integer> result = all.get(0);
      for (SortedSet<Integer> set : all.subList(1, all.size())) {
        result = intersection(result.iterator(), set.iterator());
      }
      return result;
    }

  }

  /* Implementation and tests for the intersection algorithm: */

  @Test
  public void intersection() {
    /* Test intersection computation for AND-queries: */
    TreeSet<Integer> PL1 = new TreeSet<Integer>(Arrays.asList(4, 3, 2, 1));
    TreeSet<Integer> PL2 = new TreeSet<Integer>(Arrays.asList(2, 4, 6, 8));
    Assert.assertEquals(Arrays.asList(2, 4), new ArrayList<Integer>(
        intersection(PL1.iterator(), PL2.iterator())));
  }

  public static SortedSet<Integer> intersection(final Iterator<Integer> i1,
      final Iterator<Integer> i2) {
    SortedSet<Integer> result = new TreeSet<Integer>();
    Integer p1 = next(i1);
    Integer p2 = next(i2);
    while (p1 != null && p2 != null) {
      if (p1.equals(p2)) {
        result.add(p1);
        p1 = next(i1);
        p2 = next(i2);
      } else if (p1 < p2)
        p1 = next(i1);
      else
        p2 = next(i2);
    }
    return result;
  }

  /* A little oddity to stay close to Manning et al. 2008, p. 11: */
  private static Integer next(final Iterator<Integer> i1) {
    return i1.hasNext() ? i1.next() : null;
  }

  /* Utilities: load data, build index: */

  private static InvertedIndex buildIndex() {
    List<String> corpus = corpus();
    long start = System.currentTimeMillis();
    System.out.printf("Building index for %s texts... ", corpus.size());
    InvertedIndex invertedIndex = new InvertedIndex(corpus);
    System.out
        .printf("done, took %s ms.\n", System.currentTimeMillis() - start);
    return invertedIndex;
  }

  private static List<String> corpus() {
    try {
      Scanner s = new Scanner(new File("pg100.txt"), "UTF-8");
      StringBuilder builder = new StringBuilder();
      while (s.hasNextLine()) {
        builder.append(s.nextLine()).append("\n");
      }
      /* Each work is delimited by a line ending with a year: */
      return Arrays.asList(builder.toString().split("1[56][0-9]{2}\n"));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return Collections.emptyList();
  }

}