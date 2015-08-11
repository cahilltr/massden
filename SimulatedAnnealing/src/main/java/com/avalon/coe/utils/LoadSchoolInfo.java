package com.avalon.coe.utils;

import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Created by cahillt on 7/21/15.
 * Loads School info from 2 files, 1 with schools and addresses
 * and 1 with schools and sports/class size
 * Also gets lat and long for school.
 */
public class LoadSchoolInfo {

  public static Map<String, SchoolInfo> getSchoolAddresses(String schoolAddress, String schoolSports) throws IOException {

    BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(schoolAddress)));
    Map<String, SchoolInfo> schoolAndAddress = new HashMap<>();
    String line = null;
    String currentSchool = null;
    String currentAddress = null;
    while((line = in.readLine()) != null) {
      if (line.contains("ID")) {
        currentSchool = line.split("\\s{2,}")[0];
      } else if (line.contains("IN")) {
        currentAddress = line.trim();
        currentAddress = currentAddress.replaceAll("\\s{2,}", " ");
        currentAddress = currentAddress.replaceAll(",", ";");
      }

      if (currentAddress != null && currentSchool != null) {
        schoolAndAddress.put(currentSchool, new SchoolInfo(currentSchool,currentAddress));
      }
    }

    Pattern replace = Pattern.compile("(.*?)\\s+((?:\\d{0,1}A\\s*|—\\s*){8})");
    BufferedReader sportsIn = new BufferedReader(new InputStreamReader(new FileInputStream(schoolSports)));
    boolean isFirstLine = true;
    while((line = sportsIn.readLine()) != null) {
//      Base BBB GBB FB BSoc GSoc SB VB
      if (!isFirstLine) {
        Matcher matcher = replace.matcher(line);
        if (matcher.matches()) {
          String school = matcher.group(1);
          if (schoolAndAddress.containsKey(school)) {
            String[] sports = matcher.group(2).split(" ");
            schoolAndAddress.get(school).setBase(sports[0]);
            schoolAndAddress.get(school).setBBB(sports[1]);
            schoolAndAddress.get(school).setGBB(sports[2]);
            schoolAndAddress.get(school).setFB(sports[3]);
            schoolAndAddress.get(school).setBSoc(sports[4]);
            schoolAndAddress.get(school).setGSoc(sports[5]);
            schoolAndAddress.get(school).setSB(sports[6]);
            schoolAndAddress.get(school).setVB(sports[7]);
          } else {
            System.out.println("NOMATCH: " + school);
          }
        }
      } else {
        isFirstLine = false;
      }
    }

    for (Map.Entry<String, SchoolInfo> entry : schoolAndAddress.entrySet()) {
      String address = entry.getValue().getAddress();
      String latAndLong = getLongitudeLatitude(address);
      String[] latlong = latAndLong.split(",");
      entry.getValue().setLat(latlong[0]);
      entry.getValue().setLong(latlong[1]);
    }

    return schoolAndAddress;
  }


  private static final String GEOCODE_REQUEST_URL = "http://maps.googleapis.com/maps/api/geocode/xml?sensor=false&";
  private static HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
//http://maps.googleapis.com/maps/api/geocode/xml?sensor=false&address=6422+E.+State+Road+218+Walton,+IN+46994
  public static String getLongitudeLatitude(String address) {

    String strLatitude = null;
    String strLongtitude = null;
    try {
      StringBuilder urlBuilder = new StringBuilder(GEOCODE_REQUEST_URL);
      if (StringUtils.isNotBlank(address)) {
        urlBuilder.append("&address=").append(URLEncoder.encode(address, "UTF-8"));
      }

      final GetMethod getMethod = new GetMethod(urlBuilder.toString());
      try {
        httpClient.executeMethod(getMethod);
        Reader reader = new InputStreamReader(getMethod.getResponseBodyAsStream(), getMethod.getResponseCharSet());

        int data = reader.read();
        char[] buffer = new char[1024];
        Writer writer = new StringWriter();
        while ((data = reader.read(buffer)) != -1) {
          writer.write(buffer, 0, data);
        }

        String result = writer.toString();
//        System.out.println(result.toString());

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader("<" + writer.toString().trim()));
        Document doc = db.parse(is);
        strLatitude = getXpathValue(doc, "//GeocodeResponse/result/geometry/location/lat/text()");
        strLongtitude = getXpathValue(doc,"//GeocodeResponse/result/geometry/location/lng/text()");
      } finally {
        getMethod.releaseConnection();
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println(address);
    }
    return strLatitude + "," + strLongtitude;
  }

  private static String getXpathValue(Document doc, String strXpath) throws XPathExpressionException {
    XPath xPath = XPathFactory.newInstance().newXPath();
    XPathExpression expr = xPath.compile(strXpath);
    String resultData = null;
    Object result4 = expr.evaluate(doc, XPathConstants.NODESET);
    NodeList nodes = (NodeList) result4;
    for (int i = 0; i < nodes.getLength(); i++) {
      resultData = nodes.item(i).getNodeValue();
    }
    return resultData;
  }

  public static Map<String, SchoolInfo> loadMap(String path) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
    Map<String, SchoolInfo> schoolInfo = new HashMap<>();
    String line;
    while((line = in.readLine()) != null) {
      String[] splits = line.split(",");
      if (splits.length == 12) {
        SchoolInfo schoolInfo1 = new SchoolInfo(splits[0], splits[1].replace(";", ","));
        //Base BBB GBB FB BSoc GSoc SB VB
        schoolInfo1.setBase(splits[2]);
        schoolInfo1.setBBB(splits[3]);
        schoolInfo1.setGBB(splits[4]);
        schoolInfo1.setFB(splits[5]);
        schoolInfo1.setBSoc(splits[6]);
        schoolInfo1.setGSoc(splits[7]);
        schoolInfo1.setSB(splits[8]);
        schoolInfo1.setVB(splits[9]);
        schoolInfo1.setLat(splits[10]);
        schoolInfo1.setLong(splits[11]);
        schoolInfo.put(splits[0], schoolInfo1);
      }
    }
    return schoolInfo;
  }
}
