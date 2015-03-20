#!/usr/bin/env bash

sudo sh /opt/solr/bin/solr create_collection -c solr-examples -d /opt/solr/example/example-DIH/solr/solr/conf/ -n solr-examples
sudo sh /opt/solr/bin/post -c solr-examples /opt/solr/example/exampledocs/books.csv
sudo sh /opt/solr/bin/post -c solr-examples /opt/solr/example/exampledocs/books.json
sudo sh /opt/solr/bin/post -c solr-examples /opt/solr/example/exampledocs/*.xml
