import time
import pandas

def changeTime(value):
    s = time.strptime(value, "%d-%b-%y")
    # yyyy-MM-dd hh:mm:ss - Format for SQL DATE
    d = time.strftime('%Y-%m-%d %I:%M:%S', s)
    return d

w = "5-Jan-09"
print changeTime(w)

dataFile = raw_input("Path to CSV: ")
newFile = raw_input("New CSV Path: ")

dataDF = pandas.read_csv(dataFile)

dataDF['date'] = dataDF['date'].apply(changeTime)

dataDF.to_csv(path_or_buf=newFile, index=False)
