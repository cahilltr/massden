Vagrant Cluster
=============
### Setup
#### Install Vagrant
* http://www.vagrantup.com/downloads.html
* http://docs.vagrantup.com/v2/

#### Install Vagrant Plugins
```bash
vagrant plugin install vagrant-hostmanager
vagrant plugin install vagrant-cachier
```

#### Vagrant Up
```bash
vagrant up
```
#### Root Password
* vagrant

#### Updating /etc/hosts
* When running a vagrant up / destroy, Vagrant may ask for a password to edit the /etc/hosts file. This password is the user password of the host machine.


##Notes
I borrowed/stole/etc a good portion of the documentation, Vagrantfile, and setup.sh from risdenk
