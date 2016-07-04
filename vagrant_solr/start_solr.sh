#!/usr/bin/env bash

#Sample Stream, Streams cant be Curled'
#search(TestCollection, q="inStock:true", rows="100", fl="id,*", sort="id desc")

#Start Solr
sudo /opt/solr/bin/solr start -c

#Create Solr Collection and add configuration to ZK
sudo /opt/solr/bin/solr create -c TestCollection -d /opt/solr/server/solr/configsets/sample_techproducts_configs/conf/

#Index All example docs
sudo /opt/solr/bin/post -c TestCollection /opt/solr/example/exampledocs/*

#Preview SQL
curl --data-urlencode 'stmt=SELECT manu as mfr, price as retail FROM TestCollection LIMIT 20' http://localhost:8983/solr/TestCollection/sql?aggregationMode=facet

#Streaming
curl --data-urlencode 'expr=search(TestCollection, q="inStock:true", rows="100", fl="id,*", sort="id desc")' -XPOST http://localhost:8983/solr/TestCollection/stream
