#MiscScripts contains scripts that accomplish something

##addyearfromfilenametoendofline.sh
Takes a year from the file name and adds the year to the end of each line.  The "-i" of sed is the flag to edit the file in place. "BASH_REMATCH[1]" is the the first subpattern assigned from the regex.

##dateToSQLDate.py
Reads a csv from https://archive.ics.uci.edu/ml/datasets/ISTANBUL+STOCK+EXCHANGE and changes the dates via Pandas Dataframes.

##hdpSandboxHBaseStart.sh
Uses Ambari's REST API to stop Oozie, Atlas, and Hive and starts HBase.  Only tested on HDP 2.3 Sandbox.

##pyPhoenixJDBC.py
Simple connection script for Python to Phoenix. Code from https://gist.github.com/randerzander/3fd189409cab970156b3.

##pythonUpgradeScript.sh
Script for upgrading to Python 2.7 while keeping Python 2.6 in place.
