package com.avalon.coe.simulation;

import com.avalon.coe.clientAPI.AreaMetadata;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Simulation Driver Class will create threads and query/insert into Elasticsearch
 */
public class SimulationDriver {

  public static void main(String[] args) throws UnknownHostException {

    /* TODO 1. query to find all possible zip codes, states, cities, etc - DONE
     * TODO 2. simulate adding/removing appointments from ES
     */

    try (Client client = TransportClient.builder().build()
            .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300))) {
      AreaMetadata am = new AreaMetadata(client, "doctors", "doctor");
      List<String> states = am.getStates();

      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.DATE, 100);

      List<Thread> threads = new ArrayList<>();
      for (String state : states) {
        List<String> cities = am.getCities(state);
        AppointmentSimulator as = new AppointmentSimulator(new Date(), calendar.getTime());
        StateSimulatedLoadThread sslt = new StateSimulatedLoadThread(client, state, cities, "doctors", "doctor",
                "appointment", as);
        sslt.run();
        threads.add(sslt);
      }

      for (Thread t : threads) {
        try {
          t.join();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

    }
  }
}
