import os
import pandas as pd
from pandas import DataFrame, Series

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

usedCols = ['Last Name', 'First Name', 'Middle Name', 'Suffix', 'Gender', 'Credential', 'Medical school name',
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

columnFamily = {"Last Name": "personal", "First Name": "personal", "Middle Name": "personal",
            "Suffix": "personal",
            "Gender": "personal", "Credential": "medical", "Medical school name": "medical",
            "Graduation year": "medical",
            "Primary specialty": "medical", "Secondary specialty 1": "medical",
            "Secondary specialty 2": "medical",
            "Secondary specialty 3": "medical", "Secondary specialty 4": "medical",
            "All secondary specialties": "medical",
            "Line 1 Street Address": "personal", "City": "personal", "State": "personal", "Zip Code": "personal"}

#Doctors tables has 2 column Families: personal and medical
#put 't1', 'r1', 'c1', 'value'
def putRow(x, tableName):
    rk = x['NPI']
    for col in usedCols:
        CF = columnFamily[col]
        columnName = colsDict[col]
        value = x[col]
    command = "echo \"put '%s','%s','%s:%s','%s'\" | hbase shell" % (tableName,rk,CF,columnName, value)
    os.system(command)

dataFile = raw_input("Path to CSV: ")
tableName = raw_input("Table Name: ")

dataDF = pd.read_csv(dataFile)
dataDF.drop(removeableCols, axis=1, inplace=True)
dataDF.apply(lambda x: putRow(x, tableName), axis=1)
