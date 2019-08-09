package com.avalonconsulting.MoreLikeThisTagging;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import java.util.*;

/**
 * Created by cahillt on 5/6/16.
 * Tagging Documents
 */
public class TagDocuments {

  private final CloudSolrServer cloudSolrServer = new CloudSolrServer("localhost:9983");

  SolrServer server = new HttpSolrServer("http://localhost:8983/solr/stackoverflow");

  public TagDocuments() {
    cloudSolrServer.setDefaultCollection("stackoverflow");
  }


  public List<ScoreTag> getTags(String content) throws SolrServerException {
//    cloudSolrServer.connect();
    SolrQuery query = new SolrQuery();
    query.set("fq", "postTypeId:1")
            .set("start", 0)
            .set("rows", 10)
            .set("fl", "*,score")
            .set("mlt.interestingTerms", "details");

    query.setQuery(content);

    System.out.println(query.getQuery());

    QueryResponse response = server.query(query);

    System.out.println(response.getRequestUrl());

    SolrDocumentList documents = response.getResults();
    Map<String, Integer> countMap = new HashMap<>(documents.size() * 2);
    int size = documents.size();
    for (int i=0; i < size; i++) {
      Collection<Object> tags = documents.get(i).getFieldValues("tags");
      for (Object o: tags) {
        String tag = o.toString();
        if (countMap.containsKey(tag)) {
          int count = countMap.get(tag) + 1;
          countMap.put(tag,  count);
        } else {
          countMap.put(tag,  1);
        }
      }
    }

    Queue<ScoreTag> scoreTags = new PriorityQueue<>(countMap.size());
    for (String tag : countMap.keySet()) {
      ScoreTag scoreTag = new ScoreTag(tag, countMap.get(tag));
      scoreTags.add(scoreTag);
    }


    List<ScoreTag> tags = new ArrayList<>(3);
    for (int i = 0; i < 3; i++) {
      tags.add(scoreTags.poll());
    }
    return tags;
  }

  /** Hold a tag returned by the Solr query, it's score and the
   *  relative probability of it appearing in relation to the
   *  other results in the set.
   *
   */
  public static class ScoreTag implements Comparable {
    private String tag;
    private int count;
    private double score;

    public ScoreTag(String tag, int score) {
      this.tag = tag;
      this.count = score;
    }

    public String getTag() {
      return tag;
    }

    public void setTag(String tag) {
      this.tag = tag;
    }

    public int getCount() {
      return count;
    }

    public void setCount(int score) {
      this.count = score;
    }

    public double getScore() {
      return score;
    }

    public void setScore(double prob) {
      this.score = prob;
    }

    @Override
    public String toString() {
      return tag + " " + count + " " + score;
    }

    @Override
    public int compareTo(Object o) {
      ScoreTag scoreTag = (ScoreTag) o;

      if (scoreTag.getTag() == null && this.getTag() == null) {
        return 0;
      } else if (scoreTag.getTag() == null && this.getTag() != null) {
        return 1;
      } else if (scoreTag.getTag() != null && this.getTag() == null) {
        return -1;
      } else if (scoreTag.getScore() > this.getScore()) {
        return -1;
      } else if (scoreTag.getScore() < this.getScore()) {
        return 1;
      }
      return 0;
    }
  }


}
