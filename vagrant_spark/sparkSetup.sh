#!/usr/bin/env bash

echo "hello"

sudo yum clean all

sudo echo never > /sys/kernel/mm/redhat_transparent_hugepage/defrag
sudo echo never > /sys/kernel/mm/redhat_transparent_hugepage/enabled

sudo echo "echo never > /sys/kernel/mm/redhat_transparent_hugepage/defrag" >> /etc/rc.local
sudo echo "echo never > /sys/kernel/mm/redhat_transparent_hugepage/enabled" >> /etc/rc.local

sudo yum -y install wget ntp java-1.8.0-openjdk java-1.8.0-openjdk-devel

sudo service iptables stop
sudo chkconfig iptables off
sudo service ntpd start
sudo chkconfig ntpd on

sudo sed -i 's/^SELINUX=.*/SELINUX=disabled/g' /etc/sysconfig/selinux
sudo setenforce 0

ssh-keygen -t rsa -N "" -f ~/.ssh/id_rsa

# Setup Spark
if [ -f /vagrant/spark-1.3.0-bin-hadoop2.4.tgz ]; then
	cp /vagrant/spark-1.3.0-bin-hadoop2.4.tgz /opt/
else
	wget http://mirror.sdunix.com/apache/spark/spark-1.3.0/spark-1.3.0-bin-hadoop2.4.tgz
	sudo mv spark-1.3.0-bin-hadoop2.4.tgz /opt/
fi

cd /opt/
sudo tar zxvf spark-1.3.0-bin-hadoop2.4.tgz
mv /opt/spark-1.3.0-bin-hadoop2.4 /opt/spark
sudo rm /opt/spark-1.3.0-bin-hadoop2.4.tgz

sleep 5

echo "setup complete"
