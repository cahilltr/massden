import jaydebeapi
conn = jaydebeapi.connect('org.apache.phoenix.jdbc.PhoenixDriver', \
                           ['jdbc:phoenix:my_zk_server:2181:/hbase-unsecure', '', ''], \
                           '/usr/hdp/current/phoenix-client/phoenix-client.jar')
curs = conn.cursor()
curs.execute('select * from WEB_STAT limit 1')
curs.fetchall()
