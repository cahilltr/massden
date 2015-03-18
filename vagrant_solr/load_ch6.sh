#!/usr/bin/env bash

#CH 6
sudo cp -r /opt/solr/server/solr/configsets/basic_configs/conf/ /tmp/
sudo cp /vagrant/ch6/wdfftypes.txt /tmp/conf/
sudo cp /vagrant/schema6.xml /tmp/conf/schema.xml
sudo sh /opt/solr/bin/solr create_collection -c tweets -d /tmp/conf/ -n tweets
sudo sh /opt/solr/bin/post -c tweets /vagrant/ch6/tweets.xml
