Spark MLLib
=======
This Repo is for work/play with Spark MLLib's data.

Data
-------
The data come from [NFL Savant](http://nflsavant.com/about.php). There are 2 files to be used. 
1) [Players CSV](http://nflsavant.com/dump/players_2013-12-12.csv)
2) [Combine CSV](http://nflsavant.com/dump/combine.csv?year=2015)

The Combine CSV needs to be cleansed. Names that include "Jr" have an extra comma and need to be removed.

External Libraries
---------
To turn the CSV's into Dataframes I used the [spark-csv library](https://github.com/databricks/spark-csv).

