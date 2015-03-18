#!/usr/bin/env bash

echo "hello"

sudo yum clean all

sudo echo never > /sys/kernel/mm/redhat_transparent_hugepage/defrag
sudo echo never > /sys/kernel/mm/redhat_transparent_hugepage/enabled

sudo echo "echo never > /sys/kernel/mm/redhat_transparent_hugepage/defrag" >> /etc/rc.local
sudo echo "echo never > /sys/kernel/mm/redhat_transparent_hugepage/enabled" >> /etc/rc.local

sudo yum -y install wget ntp java-1.7.0-openjdk java-1.7.0-openjdk-devel

sudo service iptables stop
sudo chkconfig iptables off
sudo service ntpd start
sudo chkconfig ntpd on

sudo sed -i 's/^SELINUX=.*/SELINUX=disabled/g' /etc/sysconfig/selinux
sudo setenforce 0

ssh-keygen -t rsa -N "" -f ~/.ssh/id_rsa

# Setup Solr
if [ -f /vagrant/solr-5.0.0.tgz ]; then
	cp /vagrant/solr-5.0.0.tgz /opt/
else
	wget http://apache.spinellicreations.com/lucene/solr/5.0.0/solr-5.0.0.tgz
	sudo mv solr-5.0.0.tgz /opt/
fi
cd /opt/
sudo tar zxvf solr-5.0.0.tgz
mv /opt/solr-5.0.0 /opt/solr

cd /opt/solr/

cp /vagrant/solr-in-action.jar server/resources/

cp -r /vagrant/ch15/jts/WEB-INF/ server/webapps/
cd server/webapps/
jar -uf solr.war WEB-INF/lib/jts.jar
cd /opt/solr/
rm -r server/webapps/WEB-INF/

bin/solr start -c

sleep 10

echo "setup complete"
