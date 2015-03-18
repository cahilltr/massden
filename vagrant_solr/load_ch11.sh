#!/usr/bin/env bash

#CH 11
bin/solr create_collection -c ecommerce -d /vagrant/ch11/cores/ecommerce/conf/ -n ecommerce
bin/post -c ecommerce /vagrant/ch11/documents/ecommerce.xml
