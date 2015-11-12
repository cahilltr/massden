#!/usr/bin/env bash

#Stop Oozie
curl -k -u admin:admin -H "X-Requested-By:ambari" -i -X PUT -d '{"ServiceInfo":{"state":"INSTALLED"}}' http://127.0.0.1:8080/api/v1/clusters/Sandbox/services/OOZIE/

#Stop Atlas
curl -k -u admin:admin -H "X-Requested-By:ambari" -i -X PUT -d '{"ServiceInfo":{"state":"INSTALLED"}}' http://127.0.0.1:8080/api/v1/clusters/Sandbox/services/ATLAS/

#Stop Hive
curl -k -u admin:admin -H "X-Requested-By:ambari" -i -X PUT -d '{"ServiceInfo":{"state":"INSTALLED"}}' http://127.0.0.1:8080/api/v1/clusters/Sandbox/services/HIVE/

#Start HBase TODO Make Me start HBase
curl -u admin:admin -i -H 'X-Requested-By: ambari' -X PUT -d '{"RequestInfo": {"context" :"Turn off maintenance mode for HBase"}, "Body": {"ServiceInfo": {"maintenance_state": "OFF"}}}' http://localhost:8080/api/v1/clusters/Sandbox/services/HBASE

curl -u admin:admin -i -H 'X-Requested-By: ambari' -X PUT -d '{"RequestInfo": {"context" :"Start HBASE via REST"}, "Body": {"ServiceInfo": {"state": "STARTED"}}}' http://localhost:8080/api/v1/clusters/Sandbox/services/HBASE
