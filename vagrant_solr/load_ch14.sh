#!/usr/bin/env bash

#CH 14
bin/solr create_collection -c field-per-language -d /vagrant/ch14/cores/field-per-language/conf/ -n field-per-language
bin/post -c field-per-language /vagrant/ch14/documents/field-per-language.xml

sleep 1

bin/solr create_collection -c english -d /vagrant/ch14/cores/core-per-language/english/conf -n english
bin/post -c english /vagrant/ch14/documents/english.xml

sleep 1

bin/solr create_collection -c french -d /vagrant/ch14/cores/core-per-language/french/conf -n french
bin/post -c french /vagrant/ch14/documents/french.xml

sleep 1

bin/solr create_collection -c spanish -d /vagrant/ch14/cores/core-per-language/spanish/conf -n spanish
bin/post -c spanish /vagrant/ch14/documents/spanish.xml

bin/solr create_collection -c aggregator -d /vagrant/ch14/cores/core-per-language/aggregator/conf/ -n aggregator

sleep 1

bin/solr create_collection -c multi-language-field -d /vagrant/ch14/cores/multi-language-field/conf/ -n multi-language-field
bin/post -c multi-language-field /vagrant/ch14/documents/multi-language-field.xml

sleep 1

bin/solr create_collection -c multi-langid -d /vagrant/ch14/cores/multi-langid/conf -n multi-langid
bin/post -c multi-langid /vagrant/ch14/documents/multi-langid.xml

sleep 1

bin/solr create_collection -c langid -d /vagrant/ch14/cores/langid/conf -n langid
bin/post -c langid /vagrant/ch14/documents/langid.xml

sleep 1

bin/solr create_collection -c langid2 -d /vagrant/ch14/cores/langid2/conf -n langid2
bin/post -c langid2 /vagrant/ch14/documents/langid.xml
