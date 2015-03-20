#!/usr/bin/env bash
#CH10

sudo sh /opt/solr/bin/solr create_collection -c solrpedia -d /vagrant/ch10/cores/solrpedia/conf/ -n solrpedia
curl http://localhost:8983/solr/solrpedia/dataimport?command=full-import

sleep 1

sudo sh /opt/solr/bin/solr create_collection -c solrpedia_instant -d /vagrant/ch10/cores/solrpedia_instant/conf/ -n solrpedia_instant
sudo sh /opt/solr/bin/post -c solrpedia_instant /vagrant/ch10/documents/solrpedia_instant.json
