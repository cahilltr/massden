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
sudo sed -i 's/^SELINUX=.*/SELINUX=disabled/g' /etc/selinux/config
sudo setenforce 0

sed -i 's/^127.0.0.1.*/127.0.0.1 localhost localhost.localdomain localhost4 localhost4.localdomain4/' /etc/hosts

ssh-keygen -t rsa -N "" -f ~/.ssh/id_rsa

cd /opt;

wget https://download.lucidworks.com/fusion-4.0.1/fusion-4.0.1.tar.gz

tar -xvf fusion-4.0.1.tar.gz

sudo reboot
