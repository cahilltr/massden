#!/usr/bin/env bash

#CH 16

#Distance Relevancy
sudo sh /opt/solr/bin/solr create_collection -c distance-relevancy -d /vagrant/ch16/cores/distance-relevancy/conf/ -n distance-relevancy
sudo sh /opt/solr/bin/post -c distance-relevancy /vagrant/ch16/documents/distance-relevancy.xml

sleep 1

#Jobs
sudo sh /opt/solr/bin/solr create_collection -c jobs -d /vagrant/ch16/cores/jobs/conf/ -n jobs
sudo sh /opt/solr/bin/post -c jobs /vagrant/ch16/documents/jobs.csv

sleep 1

#news relevancy
sudo sh /opt/solr/bin/solr create_collection -c news-relevancy -d /vagrant/ch16/cores/news-relevancy/conf/ -n news-relevancy
sudo sh /opt/solr/bin/post -c news-relevancy /vagrant/ch16/documents/news-relevancy.xml

sleep 1

#No Title Boost
sudo sh /opt/solr/bin/solr create_collection -c no-title-boost -d /vagrant/ch16/cores/no-title-boost/conf/ -n no-title-boost
sudo sh /opt/solr/bin/post -c no-title-boost /vagrant/ch16/documents/no-title-boost.xml

sleep 1

#Title Boost
sudo sh /opt/solr/bin/solr create_collection -c title-boost -d /vagrant/ch16/cores/title-boost/conf/ -n title-boost
sudo sh /opt/solr/bin/post -c title-boost /vagrant/ch16/documents/title-boost.xml
