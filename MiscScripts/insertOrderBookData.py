import pandas as pd
from pandas import DataFrame
import phoenixdb
#Data from https://lobsterdata.com/info/DataSamples.php

messageHeader = ["TIME","TYPE","ORDERID","SIZE","PRICE","DIRECTION"]
orderBookHeader = ["ASK_PRICE_1","ASK_SIZE_1","BID_PRICE_1","BID_SIZE_1","ASK_PRICE_2","ASK_SIZE_2","BID_PRICE_2","BID_SIZE_2","ASK_PRICE_3","ASK_SIZE_3","BID_PRICE_3","BID_SIZE_3","ASK_PRICE_4","ASK_SIZE_4","BID_PRICE_4","BID_SIZE_4","ASK_PRICE_5","ASK_SIZE_5","BID_PRICE_5","BID_SIZE_5","ASK_PRICE_6","ASK_SIZE_6","BID_PRICE_6","BID_SIZE_6","ASK_PRICE_7","ASK_SIZE_7","BID_PRICE_7","BID_SIZE_7","ASK_PRICE_8","ASK_SIZE_8","BID_PRICE_8","BID_SIZE_8","ASK_PRICE_9","ASK_SIZE_9","BID_PRICE_9","BID_SIZE_9","ASK_PRICE_10","ASK_SIZE_10","BID_PRICE_10","BID_SIZE_10"]

database_url = 'http://localhost:8765/'
conn = phoenixdb.connect(database_url, autocommit=True)

counter = 0

def insertRow(x):
    global counter
    cur = conn.cursor()
    command = "UPSERT INTO ORDER_BOOK (TIME,TYPE,ORDERID,SIZE,PRICE,DIRECTION,ASK_PRICE_1,ASK_SIZE_1,BID_PRICE_1,BID_SIZE_1,ASK_PRICE_2,ASK_SIZE_2,BID_PRICE_2,BID_SIZE_2,ASK_PRICE_3,ASK_SIZE_3,BID_PRICE_3,BID_SIZE_3,ASK_PRICE_4,ASK_SIZE_4,BID_PRICE_4,BID_SIZE_4,ASK_PRICE_5,ASK_SIZE_5,BID_PRICE_5,BID_SIZE_5,ASK_PRICE_6,ASK_SIZE_6,BID_PRICE_6,BID_SIZE_6,ASK_PRICE_7,ASK_SIZE_7,BID_PRICE_7,BID_SIZE_7,ASK_PRICE_8,ASK_SIZE_8,BID_PRICE_8,BID_SIZE_8,ASK_PRICE_9,ASK_SIZE_9,BID_PRICE_9,BID_SIZE_9,ASK_PRICE_10,ASK_SIZE_10,BID_PRICE_10,BID_SIZE_10) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)" % (x['TIME'],x['TYPE'],x['ORDERID'],x['SIZE'],x['PRICE'],x['DIRECTION'],x['ASK_PRICE_1'],x['ASK_SIZE_1'],x['BID_PRICE_1'],x['BID_SIZE_1'],x['ASK_PRICE_2'],x['ASK_SIZE_2'],x['BID_PRICE_2'],x['BID_SIZE_2'],x['ASK_PRICE_3'],x['ASK_SIZE_3'],x['BID_PRICE_3'],x['BID_SIZE_3'],x['ASK_PRICE_4'],x['ASK_SIZE_4'],x['BID_PRICE_4'],x['BID_SIZE_4'],x['ASK_PRICE_5'],x['ASK_SIZE_5'],x['BID_PRICE_5'],x['BID_SIZE_5'],x['ASK_PRICE_6'],x['ASK_SIZE_6'],x['BID_PRICE_6'],x['BID_SIZE_6'],x['ASK_PRICE_7'],x['ASK_SIZE_7'],x['BID_PRICE_7'],x['BID_SIZE_7'],x['ASK_PRICE_8'],x['ASK_SIZE_8'],x['BID_PRICE_8'],x['BID_SIZE_8'],x['ASK_PRICE_9'],x['ASK_SIZE_9'],x['BID_PRICE_9'],x['BID_SIZE_9'],x['ASK_PRICE_10'],x['ASK_SIZE_10'],x['BID_PRICE_10'],x['BID_SIZE_10'])
    cur.execute(command)
    counter += 1
    print counter

messageCSV = raw_input("Message CSV: ")
orderbookCSV = raw_input("Order Book CSV: ")

messageDf = pd.read_csv(messageCSV,names=messageHeader)
orderbookCSV = pd.read_csv(orderbookCSV,names=orderBookHeader)

combinedDF = pd.merge(messageDf, orderbookCSV, how='outer', left_index=True, right_index=True)
print len(combinedDF)
combinedDF.apply(insertRow,axis=1)

