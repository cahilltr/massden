from elasticsearch import Elasticsearch
import sys
import pandas
import json
import math
import threading
import string

removeableCols = ["Claims based hospital affiliation CCN 1", "Claims based hospital affiliation LBN 1",
                  "Claims based hospital affiliation CCN 2", "Claims based hospital affiliation LBN 2",
                  "Claims based hospital affiliation CCN 3", "Claims based hospital affiliation LBN 3",
                  "Claims based hospital affiliation CCN 4", "Claims based hospital affiliation LBN 4",
                  "Claims based hospital affiliation CCN 5", "Claims based hospital affiliation LBN 5",
                  "Professional accepts Medicare Assignment", "Participating in eRx", "Participating in PQRS",
                  "Participating in EHR", "Received PQRS Maintenance of Certification Program Incentive",
                  "Participated in Million Hearts", "Line 2 Street Address", "Marker of address line 2 suppression",
                  "Organization legal name", "Organization DBA name", "Group Practice PAC ID",
                  "Number of Group Practice members",
                  "PAC ID", "Professional Enrollment ID"]

usedCols = ['NPI', 'Last Name', 'First Name', 'Middle Name', 'Suffix', 'Gender', 'Credential', 'Medical school name',
            'Graduation year', 'Primary specialty', 'Secondary specialty 1', 'Secondary specialty 2',
            'Secondary specialty 3', 'Secondary specialty 4', 'All secondary specialties', 'Line 1 Street Address',
            'City', 'State', 'Zip Code']

colsDict = {"NPI": "npi", "Last Name": "last_name", "First Name": "first_name", "Middle Name": "middle_name",
            "Suffix": "suffix",
            "Gender": "gender", "Credential": "credentials", "Medical school name": "med_school",
            "Graduation year": "grad_year",
            "Primary specialty": "primary_spec", "Secondary specialty 1": "secondary_spec",
            "Secondary specialty 2": "tertiary_spec",
            "Secondary specialty 3": "quaternary_spec", "Secondary specialty 4": "quinary_spec",
            "All secondary specialties": "all_spec",
            "Line 1 Street Address": "street", "City": "city", "State": "state", "Zip Code": "zip"}


def createIndexAndAddMapping(es, body, indexName):
    if not es.indices.exists(index=indexName):
        print "Index does not exist"
        try:
            es.indices.create(index=indexName, ignore=400, body=body)
            return True
        except:
            e = sys.exc_info()[0]
            print e
            return False
    else:
        print "index exists"
        return False


def indexDataBulk(es, body):
    if not body:
        return False
    try:
        es.bulk(body)
    except:
        e = sys.exc_info()[0]
        print e
        return False
    return True


# handle csv and return a list of DFs
def handleCSV(csvFile):
    dataDF = pandas.read_csv(csvFile)
    dataDF.drop(removeableCols, axis=1, inplace=True)
    dfs = []
    step = 1000
    for i in range(step, len(dataDF) + step, step):
        tempDF = dataDF[i - step:i]
        dfs.append(tempDF)
    return dfs
    # return dataDF


# { "index": { "_index": "addr", "_type": "contact", "_id": 1 }}
# { "name": "Fyodor Dostoevsky", "country": "RU" }
# createJSON
# create index and index body from a row
def createJsonFromRow(row, indexName, type):
    data = {}
    for col in usedCols:
        if isinstance(row[col], (int, long, float, complex)):
            if not math.isnan(row[col]):
                if col == "Zip Code":
                    zipCode = int(str(row[col])[0:5])
                    data[colsDict[col]] = zipCode
                else:
                    data[colsDict[col]] = row[col]
        elif col == "Line 1 Street Address":
            street = row[col]
            data[colsDict[col]] = street
        elif col == "City":
            city = row[col]
            data[colsDict[col]] = city
        elif col == "State":
            state = row[col]
            data[colsDict[col]] = state
        else:
            data[colsDict[col]] = row[col]
    jsonBody = json.dumps(data)
    return '{ "index": { "_index": "%s", "_type": "%s", "_id": %s }}' % (
    indexName, type, row.name) + "\n" + jsonBody + "\n"


def indexBody(es, df, indexName, type):
    df['json'] = df.apply(lambda x: createJsonFromRow(x, indexName, type), axis=1)
    indexDataBulk(es, string.join(df['json'], "\n"))


try:
    dataFile = raw_input("Path to CSV: ")
    mapping = raw_input("Path to JSON mapping: ")
    indexName = raw_input("Name of Elasticsearch Index: ")
    typeName = raw_input("Name of Type: ")

    es = Elasticsearch()

    with open(mapping, "r") as myfile:
        mappingString = myfile.read()

    created = createIndexAndAddMapping(es, mappingString, indexName)
    if not created:
        raise

    dfList = handleCSV(dataFile)

    print len(dfList)
    threads = []
    for i in dfList:
        thread = threading.Thread(target=indexBody, args=(es, i, indexName, typeName))
        thread.start()
        threads.append(thread)

    print threading.active_count

    for t in threads:
        t.join()

except:
    exc_type, exc_obj, exc_tb = sys.exc_info()
    print (exc_type, exc_tb.tb_lineno)
