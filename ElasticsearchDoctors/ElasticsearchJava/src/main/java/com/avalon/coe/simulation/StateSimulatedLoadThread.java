package com.avalon.coe.simulation;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.index.IndexAction;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;

import java.util.List;
import java.util.Random;

/**
 * Thread for creating/cancelling appointments in a given state
 */
public class StateSimulatedLoadThread extends Thread implements Runnable  {

  private Client client;
  private String state;
  private List<String> cities;
  private AppointmentSimulator as;
  private Random random = new Random();
  private String indexName;
  private String doctorTypeName;
  private String appointmentTypeName;

  public StateSimulatedLoadThread(Client c, String state, List<String> cities, String indexName, String doctorTypeName,
                                  String appointmentTypeName, AppointmentSimulator as) {
    this.client = c;
    this.state = state;
    this.cities = cities;
    this.as = as;
    this.indexName = indexName;
    this.doctorTypeName = doctorTypeName;
    this.appointmentTypeName = appointmentTypeName;
  }

  @Override
  public void run() {
    int count = 10000;
    while (count != 0) {

//    Determine what City to search for
      String city = randomCity();

//    find doctor from selected random city
      String id = findDoctor(city);

//    schedule appointment
      if (createAppointment(this.as.createAppointment(), id)) {
        count--;
      }
      if (count % 100 == 0) {
        System.out.println(this.getId() + " " + count);
      }
    }
  }

  private boolean createAppointment(Appointment a, String doctorID) {
    IndexAction indexAction = IndexAction.INSTANCE;
    IndexRequestBuilder indexRequestBuilder = new IndexRequestBuilder(client, indexAction);
    indexRequestBuilder.setParent(doctorID)
            .setIndex(this.indexName)
            .setType(this.appointmentTypeName)
            .setId(Long.toString(System.currentTimeMillis()))
            .setSource("cancelled", Boolean.toString(a.isCancelled))
            .setSource("notes", a.notes)
            .setSource("dateTime", a.date);

    ActionFuture<IndexResponse> af = client.index(indexRequestBuilder.request());
    IndexResponse response = af.actionGet();
    return response.isCreated();
  }

  //Gets a random city from the list of cities.
  private String randomCity(){
    return this.cities.get(this.random.nextInt(cities.size()));
  }

  //Gets the ID of a doctor in a city
  //{"query":{"bool":{"must":[{"term":{"city":"auburn"}},{"term":{"state":"in"}}]}}}
  private String findDoctor(String city) {
    SearchResponse sr = this.client.prepareSearch(this.indexName)
            .setTypes(this.doctorTypeName)
            .setQuery(QueryBuilders.boolQuery()
                            .must(new TermQueryBuilder("city", city))
                            .must(new TermQueryBuilder("state", state)))
            .execute()
            .actionGet();

    SearchHit[] searchHits = sr.getHits().getHits();
    return searchHits[this.random.nextInt(searchHits.length)].getId();
  }
}
