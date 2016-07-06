#!/usr/bin/env bash

#Sample Stream, Streams cant be Curled'
#search(TestCollection, q="inStock:true", rows="100", fl="id,*", sort="id desc")

#Start Solr
sudo /opt/solr/bin/solr start -c

#Create Solr Collection and add configuration to ZK
sudo /opt/solr/bin/solr create -c TestCollection -d /opt/solr/server/solr/configsets/sample_techproducts_configs/conf/

#Index All example docs
sudo /opt/solr/bin/post -c TestCollection /opt/solr/example/exampledocs/*

#Create Solr Collection and add configuration to ZK
sudo /opt/solr/bin/solr create -c LocationCollection -d /opt/solr/server/solr/configsets/sample_techproducts_configs/conf/

#Index All example docs
sudo /opt/solr/bin/post -c LocationCollection /vagrant/locationJoin.csv

#Preview SQL
curl --data-urlencode 'stmt=SELECT manu as mfr, price as retail FROM TestCollection LIMIT 20' http://localhost:8983/solr/TestCollection/sql?aggregationMode=facet

#Streaming
curl --data-urlencode 'expr=search(TestCollection, q="inStock:true", rows="100", fl="id,*", sort="id desc")' -XPOST http://localhost:8983/solr/TestCollection/stream

#Streaming Join
curl --data-urlencode 'expr=innerJoin(search(LocationCollection, q=*:*, fl="id,address_t,city_t,state_t,publisher_t,printYear_i", sort="id asc"), search(TestCollection, q=cat:book, fl="author,id,name", sort="id asc"), on="id")' -XPOST http://localhost:8983/solr/TestCollection/stream

#Create Solr Collection as Update Collection
sudo /opt/solr/bin/solr create -c UpdateCollection -d /opt/solr/server/solr/configsets/sample_techproducts_configs/conf/

#Streaming Update
curl --data-urlencode 'expr=update(UpdateCollection, batchSize=500, search(TestCollection, q=cat:book,fl="id,name,author,price,price_c,inStock",sort="author asc"))' -XPOST http://localhost:8983/solr/TestCollection/stream

#Commit data
curl http://localhost:8983/solr/UpdateCollection/update?commit=true
