CREATE USER 'ambari'@'%' IDENTIFIED BY 'hadoop';

GRANT ALL PRIVILEGES ON *.* TO 'ambari'@'%';

CREATE USER 'ambari'@'localhost' IDENTIFIED BY 'hadoop';

GRANT ALL PRIVILEGES ON *.* TO 'ambari'@'localhost';

CREATE USER 'ambari'@'master.cluster' IDENTIFIED BY 'hadoop';

GRANT ALL PRIVILEGES ON *.* TO 'ambari'@'master.cluster';

FLUSH PRIVILEGES;

CREATE USER ‘hive’@’localhost’ IDENTIFIED BY ‘hadoop’;

GRANT ALL PRIVILEGES ON *.* TO 'hive'@'localhost';

CREATE USER ‘hive’@’%’ IDENTIFIED BY ‘hadoop’;

GRANT ALL PRIVILEGES ON *.* TO 'hive'@'%';

CREATE USER 'hive'@'node1.cluster'IDENTIFIED BY 'hadoop';

GRANT ALL PRIVILEGES ON *.* TO 'hive'@'node1.cluster';

FLUSH PRIVILEGES;



CREATE USER 'oozie'@'%' IDENTIFIED BY 'hadoop';

GRANT ALL PRIVILEGES ON *.* TO 'oozie'@'%';

FLUSH PRIVILEGES;

CREATE DATABASE oozie;
