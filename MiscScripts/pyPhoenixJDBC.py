import jaydebeapi
from pandas import DataFrame
conn = jaydebeapi.connect('org.apache.phoenix.jdbc.PhoenixDriver', ['jdbc:phoenix:localhost:2181:/hbase-unsecure', '', ''], '/usr/hdp/current/phoenix-client/phoenix-client.jar')
curs = conn.cursor()
curs.execute('select * from WEB_STAT;')
rows = curs.fetchall()
df = DataFrame(rows, columns=zip(*curs.description)[0])
