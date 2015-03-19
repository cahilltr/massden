Vagrant Solr Sandbox
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
* rootpw

#### Updating /etc/hosts
* When running a vagrant up / destroy, Vagrant may ask for a password to edit the /etc/hosts file. This password is the user password of the host machine.

## Solr Sandbox
The Solr sandbox is a Solr 5.0, 1 node SolrCloud instance with the ability to run the examples from Solr In Action (http://www.manning.com/grainger/?a_aid=1&a_bid=39472865).  It is set up to load a chapter of examples from the Solr In Action book at a time.

### Loading a Chapter
To load a chapter into Solr, you must first ssh into the sandbox.
#### Vagrant SSH
```bash
vagrant ssh
```
You will then be SSH'd into the Solr Sandbox as user vagrant.  In the home directory a list of load scripts will be availible.  To load a chapter, simply run the correct script.
#### Loading a Chapter
```bash
sh load_ch10.sh
```

### Starting Solr after a Halt
To restart solr after a halt, run the start_solr.sh script.
```bash
sh start_solr.sh
```

##Notes
I borrowed/stole/etc a good portion of the documentation, Vagrantfile, and setup.sh from risdenk.  I took the chapter configuration and data from https://github.com/treygrainger/solr-in-action.  The configurations required some editing to work with Solr 5.0/SolrCloud.  The custom Jar that is used through out the book needed edited as well to update the code from Solr 4.7 to Solr 5.0
