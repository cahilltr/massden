#!/usr/bin/env bash

#Custom Functions
sudo sh /opt/solr/bin/solr create_collection -c customfunction -d /vagrant/ch15/cores/customfunction/conf/ -n customfunction
sudo sh /opt/solr/bin/post -c customfunction /vagrant/ch15/documents/customfunction.xml

sleep 1

#Distance Facet
sudo sh /opt/solr/bin/solr create_collection -c distancefacet -d /vagrant/ch15/cores/distancefacet/conf/ -n distancefacet

sleep 1

#Geospatial
sudo sh /opt/solr/bin/solr create_collection -c geospatial -d /vagrant/ch15/cores/geospatial/conf/ -n geospatial
sudo sh /opt/solr/bin/post -c geospatial /vagrant/ch15/documents/geospatial.xml

sleep 1

#JTS Geospatial
cp -r /vagrant/ch15/cores/geospatial/conf /tmp/
rm /tmp/conf/schema.xml
mv /tmp/conf/jts_schema.xml /tmp/conf/schema.xml
sudo sh /opt/solr/bin/solr create_collection -c geospatial_jts -d /tmp/conf/ -n geospatial_jts
sudo sh /opt/solr/bin/post -c geospatial_jts /vagrant/ch15/documents/geospatial.xml

sleep 1

#join restaurants
sudo sh /opt/solr/bin/solr create_collection -c join_restaurants -d /vagrant/ch15/cores/join/join_restaurants/conf/ -n join_restaurants
sudo sh /opt/solr/bin/post -c join_restaurants /vagrant/ch15/documents/join_restaurants.xml

sleep 1

#Join user actions
sudo sh /opt/solr/bin/solr create_collection -c join_useractions -d /vagrant/ch15/cores/join/join_useractions/conf/ -n join_useractions
sudo sh /opt/solr/bin/post -c join_useractions /vagrant/ch15/documents/join_useractions.xml

sleep 1

#news
sudo sh /opt/solr/bin/solr create_collection -c news -d /vagrant/ch15/cores/news/conf/ -n news
sudo sh /opt/solr/bin/post -c news /vagrant/ch15/documents/news.xml

sleep 1

#Pivot Faceting
sudo sh /opt/solr/bin/solr create_collection -c pivotfaceting -d /vagrant/ch15/cores/pivotfaceting/conf/ -n pivotfaceting
sudo sh /opt/solr/bin/post -c pivotfaceting /vagrant/ch15/documents/pivotfaceting.xml

sleep 1

#Sales Tax
sudo sh /opt/solr/bin/solr create_collection -c salestax -d /vagrant/ch15/cores/salestax/conf/ -n salestax
sudo sh /opt/solr/bin/post -c salestax /vagrant/ch15/documents/salestax.xml
