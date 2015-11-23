import pandas as pd
from pandas import DataFrame, Series
import pandas.io.data as web
import jaydebeapi

stocks = ['SPY', 'AAPL', 'XOM', 'MRO', 'GOOG']

conn = jaydebeapi.connect('org.apache.phoenix.jdbc.PhoenixDriver', ['jdbc:phoenix:localhost:2181:/hbase-unsecure', '', ''], '/usr/hdp/current/phoenix-client/phoenix-client.jar')

def insertRow(x, stock):
    cur = conn.cursor()
    command = "UPSERT INTO STOCKS (SYMBOL, OPEN, HIGH, LOW, CLOSE, VOLUME, ADJ_CLOSE, RECORD_DATE) VALUES ('%s', %s, %s, %s, %s, %s, %s, '%s')" % (stock, x['Open'], x['High'], x['Low'], x['Close'], x['Volume'], x['Adj Close'], x.name)
    cur.execute(command)

for stock in stocks:
    data = web.get_data_yahoo(stock,'2006-01-01')
#    Cannot use to_sql because it is not supported
#    data.to_sql('STOCKS', conn, if_exists='append', index=True, index_label='RECORD_DATE')
    data.apply(lambda x: insertRow(x, stock), axis=1)
#    Must commit on the connection for upserts to work
    conn.commit()
