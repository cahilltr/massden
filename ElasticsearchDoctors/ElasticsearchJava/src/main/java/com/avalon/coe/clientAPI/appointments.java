package com.avalon.coe.clientAPI;

import com.avalon.coe.simulation.Appointment;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.index.IndexAction;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.support.format.ValueParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Api to handle appointments
 */
public class appointments {

  public static boolean insertAppointment(Client client, String indexName, String appointmentTypeName,
                                          String doctorID, Appointment a) {
    IndexAction indexAction = IndexAction.INSTANCE;
    IndexRequestBuilder indexRequestBuilder = new IndexRequestBuilder(client, indexAction);
    indexRequestBuilder.setParent(doctorID)
            .setIndex(indexName)
            .setType(appointmentTypeName)
            .setId(Long.toString(System.currentTimeMillis()))
            .setSource("cancelled", Boolean.toString(a.isCancelled))
            .setSource("notes", a.notes)
            .setSource("dateTime", a.date);

    ActionFuture<IndexResponse> af = client.index(indexRequestBuilder.request());
    IndexResponse response = af.actionGet();
    return response.isCreated();
  }
//  get all doctors with an appointment
//  {"query":{"has_child":{"type":"appointment","query":{"match_all":{}}}}}}

  //Get all appointments for Doctor
  //TODO Test
  //{"query":{"terms":{"_parent":["231029"]}}}
  public static List<Appointment> getAppointmentsForDoctor(Client c, String indexName, String appointmentTypeName,
                                                           String doctorTypeName, String doctorID) {
    SearchResponse sr = c.prepareSearch(indexName)
            .setQuery(QueryBuilders.termQuery("_parent",doctorID))
            .execute()
            .actionGet();

    SearchHit[] searchHits = sr.getHits().getHits();
    List<Appointment> appts = new ArrayList<>();
    for (SearchHit s : searchHits) {
      Appointment appointment = new Appointment();
      for (String name : s.getFields().keySet()) {
        if (name.equals("cancelled")) {
          appointment.isCancelled = Boolean.parseBoolean(s.getFields().get(name).toString());
        } else if (name.equals("notes")) {
          appointment.notes = s.getFields().get(name).toString();
        } else if (name.equals("dateTime")) {
          //TODO handle date time
        }
      }
      appts.add(appointment);
    }
    return appts;
  }

}
