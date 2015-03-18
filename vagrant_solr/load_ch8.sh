#!/usr/bin/env bash

#CH 8
bin/solr create_collection -c restaurants -d /vagrant/ch8/cores/restaurants/ -n restaurants
bin/post -c restaurants /vagrant/ch8/documents/restaurants.json
