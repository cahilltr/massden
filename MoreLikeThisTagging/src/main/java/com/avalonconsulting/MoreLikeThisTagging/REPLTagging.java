package com.avalonconsulting.MoreLikeThisTagging;

import org.apache.solr.client.solrj.SolrServerException;

import java.io.BufferedInputStream;
import java.util.List;
import java.util.Scanner;

/**
 * Created by cahillt on 5/9/16.
 * REPL for assigning Tags
 */
public class REPLTagging {

  public static void main(String[] args) throws SolrServerException {
    Scanner reader = new Scanner(new BufferedInputStream(System.in));  // Reading from System.in

    TagDocuments tagDocuments = new TagDocuments();

    while (true) {
      System.out.println("Enter a String: ");

      String content = reader.nextLine();
      System.out.println(content);

      List<TagDocuments.ScoreTag> tags = tagDocuments.getTags(content);

      for (TagDocuments.ScoreTag scoreTag : tags) {
        System.out.println(scoreTag.toString() + " : " + scoreTag.getScore());
      }

      System.out.println();
    }

  }

}
