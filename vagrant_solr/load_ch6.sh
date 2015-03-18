#!/usr/bin/env bash

#CH 6
cp -r server/solr/configsets/basic_configs/conf/ /tmp/
cp /vagrant/ch6/wdfftypes.txt /tmp/conf/
cp /vagrant/schema6.xml /tmp/conf/schema.xml
bin/solr create_collection -c tweets -d /tmp/conf/ -n tweets
bin/post -c tweets /vagrant/ch6/tweets.xml
