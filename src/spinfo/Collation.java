/** Copyright 2011 Fabian Steeg, University of Cologne, http://github.com/spinfo */

package spinfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.text.CollationKey;
import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;

/** Lexicographic sorting and collation in Java. */
public class Collation {

  List<String> words = Arrays.asList("Very", "Über", "very", "ultra", "über");

  @Test
  public void basicProblem() {
    /*
     * The basic problem: Java's default sorting for strings (based on the
     * character's Unicode position) is insufficient for lexicographic ordering:
     */
    Collections.sort(words);
    /*
     * Because it sorts all upper case letters before all lowercase letters and
     * sorts all letters with diacritics behind all standard letters:
     */
    assertEquals(Arrays.asList("Very", "ultra", "very", "Über", "über"), words);
    /* Which is not what we would expect (upper, lower, diacritics together): */
    assertFalse(Arrays.asList("ultra", "Über", "über", "Very", "very").equals(
        words));
  }

  @Test
  public void basicSolution() {
    /* Idea: map the chars to their correct position, and sort by that mapping: */
    final Map<Character, Integer> collationKeys = new HashMap<Character, Integer>();
    collationKeys.put('U', 1); // or lower-level, with array: char['U'] = 1;
    collationKeys.put('u', 2);
    collationKeys.put('\u00dc', 3); // Ü
    collationKeys.put('\u00fc', 4); // ü
    collationKeys.put('V', 5);
    collationKeys.put('v', 6);
    /* We pass a custom sorting strategy to the sort method: */
    Collections.sort(words, new Comparator<String>() {
      @Override
      public int compare(String s1, String s2) {
        /* For this sample, we only look at the first letter: */
        Character c1 = s1.charAt(0);
        Character c2 = s2.charAt(0);
        /* We don't compare the chars, but their collation keys: */
        return collationKeys.get(c1).compareTo(collationKeys.get(c2));
      }
    });
    /* For our specific case, this results in a somewhat correct order: */
    assertEquals(Arrays.asList("ultra", "Über", "über", "Very", "very"), words);
  }

  @Test
  public void collator() {
    /* Java contains region-specific collation rules, via Collator: */
    final Collator collator = Collator.getInstance(Locale.GERMAN);
    Collections.sort(words, new Comparator<String>() {
      @Override
      public int compare(String s1, String s2) {
        return collator.compare(s1, s2);
      }
    });
    /* Which gets the details right, e.g. sort umlauts like their standards: */
    assertEquals(Arrays.asList("über", "Über", "ultra", "very", "Very"), words);
  }

  @Test
  public void comparable() {
    /*
     * If we control the objects sorted (unlike strings), and the sorting does
     * not depend on something external to the objects (unlike above, where we
     * sort chars by their keys), we can define the order inside our objects:
     */
    List<Word> words = Arrays.asList(new Word("Very"), new Word("ultra"),
        new Word("über"), new Word("Super"));
    /* From the usage side, it now looks like the default sorting just works: */
    Collections.sort(words);
    assertEquals(Arrays.asList(new Word("Super"), new Word("über"), new Word(
        "ultra"), new Word("Very")), words);
  }

  static class Word implements Comparable<Word> {

    private String val;
    private Collator collator = Collator.getInstance(); // uses system locale
    private CollationKey key;

    public Word(String val) {
      this.val = val;
      this.key = collator.getCollationKey(val); // precompute the key
    }

    @Override
    public int compareTo(Word that) {
      // return this.val.compareTo(that.val); // naive, not sufficient
      /* Instead of comparing the vals, we can pass them to the collator: */
      // return collator.compare(this.val, that.val); // always computes keys
      /* To improve performance, we precompute the keys, and compare these: */
      return this.key.compareTo(that.key);
    }

    /* Java standard method implementations below, needed for testing here: */

    @Override
    public String toString() {
      return val;
    }

    @Override
    public boolean equals(Object that) {
      return that instanceof Word && ((Word) that).val.equals(this.val);
    }

    @Override
    public int hashCode() {
      return val.hashCode(); // mandatory if equals, consistent with equals
    }
  }

  @Test
  public void customRules() throws ParseException {
    List<String> w = Arrays.asList("Löss", "Lee", "Luv", "Löß");
    /* Default collator: ß after ss */
    sortWithCollator(w, Collator.getInstance(Locale.GERMAN)); // default german
    assertEquals(Arrays.asList("Lee", "Löss", "Löß", "Luv"), w);
    /* Custom requirement: sort ß before ss (old German spelling rules) */
    String defaultRules = ((RuleBasedCollator) RuleBasedCollator
        .getInstance(Locale.GERMAN)).getRules();
    String customRules = "ß < ss"; // additional custom rule, replaces default
    final Collator collator = new RuleBasedCollator(defaultRules + customRules);
    sortWithCollator(w, collator);
    assertEquals(Arrays.asList("Lee", "Löß", "Löss", "Luv"), w);
  }

  private void sortWithCollator(List<String> words, final Collator collator) {
    Collections.sort(words, new Comparator<String>() {
      @Override
      public int compare(String s1, String s2) {
        return collator.compare(s1, s2);
      }
    });
  }

}