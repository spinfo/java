/** Copyright 2011 Fabian Steeg, University of Cologne, http://github.com/spinfo */

package spinfo;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.cyberneko.html.parsers.DOMParser;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

/** Basic web crawling, HTML processing, and concurrency. */
public class Crawling {

  /** Test simple content loading via URL. */
  @Test
  public void load() throws IOException {
    /*
     * Simple loading of content from a URL, using Guava (passing the charset
     * specified by the site), but the result is not very useful as it is (e.g.
     * for indexing):
     */
    System.out.println(Resources.toString(new URL("http://www.zeit.de/"),
        Charsets.UTF_8));
  }

  /** Test web site parsing. */
  @Test
  public void parse() throws SAXException, IOException {
    /* What we need is a structured processing of content and links: */
    WebDocument doc = Parser.parse("http://www.zeit.de/");
    String text = doc.text;
    Set<String> links = doc.links;
    assertTrue("Document content should exist", text.length() > 0);
    assertTrue("Outgoing links should exist", links.size() > 0);
    System.out.println("Text: " + text);
    System.out.println("Links: " + links);
  }

  /** A web document representation consisting of text and links. */
  static class WebDocument {
    String text;
    Set<String> links;
    URL url;

    WebDocument(String url, String text, Set<String> links)
        throws MalformedURLException {
      this.text = text;
      this.links = links;
      this.url = new URL(url);
    }
  }

  /** A parser that transforms a URL into a web document representation. */
  static class Parser {
    private static Set<String> links;
    private static StringBuilder builder;

    static WebDocument parse(String url) throws SAXException, IOException {
      /* We parse with NekoHTML, an error-correcting parser based on Xerces: */
      DOMParser parser = new DOMParser();
      parser.parse(url);
      builder = new StringBuilder();
      links = new HashSet<String>();
      /* We start at the first element: */
      process(parser.getDocument().getFirstChild());
      /* At the end we create our resulting document object: */
      return new WebDocument(url, builder.toString().trim(), links);
    }

    private static void process(Node node) throws MalformedURLException {
      /*
       * We get elements by their names. We could use instanceof, and e.g. test
       * if something is a HTMLParagraphElement, but this is less robust, since
       * e.g. XHTML documents are made of elements in a different namespace.
       */
      String elementName = node.getNodeName().toLowerCase().trim();
      /* We treat as content here only text within a p-tag: */
      if (elementName.equals("p")) {
        String text = node.getTextContent().trim();
        if (text.length() > 0) {
          builder.append(text).append("\n\n"); // make it a paragraph
        }
      } else if (elementName.equals("a")) {
        if (node.hasAttributes()) {
          /* If the a-tag has a href attribute with http, add it to the links: */
          Node href = node.getAttributes().getNamedItem("href");
          if (href != null && href.getNodeValue().trim().startsWith("http://")) {
            links.add(href.getNodeValue().trim());
          }
        }
      }
      /* Done with current node, recurse on same level (if there is more): */
      Node sibling = node.getNextSibling();
      if (sibling != null) {
        process(sibling);
      }
      /* Done with current level, recurse to next level (if there is more): */
      Node child = node.getFirstChild();
      if (child != null) {
        process(child);
      }
    }
  }

  /** Test the actual crawling, quick sample. */
  @Test
  public void crawl() throws InterruptedException {
    /* Now that we have a way to process a single web site, we can crawl: */
    List<String> seed = Arrays.asList("http://www.ub.uni-koeln.de/",
        "http://www.zeit.de");
    /* Process the seed only: */
    assertTrue(Crawler.crawl(seed, 0).size() == seed.size());
  }

  /** Test the actual crawling, long-running sample. */
  // @Test // (long-running task, comment in to run)
  public void crawlMore() throws InterruptedException {
    List<String> seed = Arrays.asList("http://www.ub.uni-koeln.de/",
        "http://www.zeit.de");
    /* Process seed and one level down: */
    int linksPerSite = 5; // estimation: > 5 links / site
    assertTrue(Crawler.crawl(seed, 1).size() > seed.size() * linksPerSite);
  }

  /** A simple crawler that processes the seed concurrently. */
  static class Crawler {
    public static List<WebDocument> crawl(List<String> seed, int depth)
        throws InterruptedException {
      /*
       * The result of crawling will be a list of web documents. To avoid
       * concurrent modification of the list, we use a synchronized wrapper:
       */
      List<WebDocument> result = Collections
          .synchronizedList(new ArrayList<WebDocument>());
      /*
       * We separate the unit of work (a Runnable) and the concurrent execution
       * (ExecutorService), cf. Effective Java, Second Edition, Chapter 10:
       */
      ExecutorService exec = Executors.newCachedThreadPool(); // newFixedThreadPool(1);
      for (String url : seed) {
        /* For every seed URL we create and execute a runnable: */
        exec.execute(new CrawlerRunnable(result, url, depth));
      }
      /* We passed all work to be done: */
      exec.shutdown();
      /* Now running in the background - we don't want to go on, but wait: */
      boolean done = exec.awaitTermination(5, TimeUnit.HOURS);
      /* Print some info on the result: */
      System.out.printf("Crawled %s docs, in time: %s\n", result.size(), done);
      return result;
    }
  }

  /** A crawler runnable that crawls from a given starting point. */
  static class CrawlerRunnable implements Runnable {
    private int depth;
    private String url;
    private List<WebDocument> result;

    public CrawlerRunnable(List<WebDocument> result, String url, int depth) {
      this.result = result;
      this.url = url;
      this.depth = depth;
    }

    @Override
    /* Top-level entry point (called by the executor service): */
    public void run() {
      try {
        crawl(url, 0); // start crawling, and catch all that can go wrong here
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (MalformedURLException e) {
        e.printStackTrace();
      } catch (SAXException e) {
        e.printStackTrace();
      } catch (IOException e) {
        System.out.println("Crawl error: " + e.getMessage()); // e.g. dead links
      }
    }

    /*
     * The recursive crawling method: parse current page, add result, and if
     * below the depth limit, call itself with the outgoing links of the page.
     */
    private void crawl(final String url, final int current)
        throws InterruptedException, SAXException, IOException {
      WebDocument doc = Parser.parse(url);
      result.add(doc);
      System.out.println("Crawled: " + url);
      Thread.sleep(300); // delay for politeness (no server request flood)
      if (current < depth) {
        for (String link : doc.links) {
          crawl(link, current + 1);
        }
      }
    }

  }
}
