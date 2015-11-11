package com.avalon.coe.clientAPI;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.util.ArrayList;
import java.util.List;

/**
 * handles getting and returning Metadata about the area
 * a doctor is in.
 */
public class AreaMetadata {

  private Client client;
  private List<String> states = new ArrayList<>(50);
  private List<String> zipCodes = new ArrayList<>();
  private final String indexName;
  private final String typeName;

  public AreaMetadata(Client c, String indexName, String typeName){
    this.client = c;
    this.indexName = indexName;
    this.typeName = typeName;
    getStatesESQuery();
    getZipCodesESQuery();
  }

  //Get all States
  //{"query":{"bool":{"filter":{"exists":{"field":"state"}}}},"aggs":{"states":{"terms":{"field":"state","size":0}}}}
  private void getStatesESQuery() {
    SearchResponse sr = this.client.prepareSearch(this.indexName)
            .setTypes(typeName)
            .setQuery(QueryBuilders.boolQuery().filter(new ExistsQueryBuilder("state")))
            .addAggregation(AggregationBuilders.terms("states").field("state").size(0))
            .execute()
            .actionGet();

    Terms agg1 = sr.getAggregations().get("states");
    for (Terms.Bucket b :agg1.getBuckets()) {
      states.add(b.getKeyAsString());
    }
  }

  //Get All zip codes
  //{"query":{"bool":{"filter":{"exists":{"field":"zip"}}}},"aggs":{"zips":{"terms":{"field":"zip","size":0}}}}
  private void getZipCodesESQuery(){
    SearchResponse sr = this.client.prepareSearch(this.indexName)
            .setTypes(typeName)
            .setQuery(QueryBuilders.boolQuery().filter(new ExistsQueryBuilder("zip")))
            .addAggregation(AggregationBuilders.terms("zips").field("zip").size(0))
            .execute()
            .actionGet();

    Terms agg1 = sr.getAggregations().get("zips");
    for (Terms.Bucket b :agg1.getBuckets()) {
      zipCodes.add(b.getKeyAsString());
    }
  }

  //Get All Cities of a State
  //{"query":{"bool":{"filter":{"bool":{"must":[{"exists":{"field":"city"}},{"term":{"state":"in"}}]}}}},"aggs":{"cities":{"terms":{"field":"city","size":0}}}}
  public List<String> getCities(String state){
    List<String> cities = new ArrayList<>();

    SearchResponse sr = this.client.prepareSearch(this.indexName)
            .setTypes(typeName)
            .setQuery(QueryBuilders.boolQuery().filter(
                    QueryBuilders.boolQuery()
                            .must(new ExistsQueryBuilder("city"))
                            .must(new TermQueryBuilder("state", state))))
            .addAggregation(AggregationBuilders.terms("cities").field("city").size(0))
            .execute()
            .actionGet();

    Terms agg1 = sr.getAggregations().get("cities");
    for (Terms.Bucket b :agg1.getBuckets()) {
      cities.add(b.getKeyAsString());
    }
    return cities;
  }

  public List<String> getStates(){
    return this.states;
  }

  public List<String> getZipCodes() {
    return this.zipCodes;
  }
}
