#!/usr/bin/env bash

#CH 8
sudo sh /opt/solr/bin/solr create_collection -c restaurants -d /vagrant/ch8/cores/restaurants/ -n restaurants
sudo sh /opt/solr/bin/post -c restaurants /vagrant/ch8/documents/restaurants.json
