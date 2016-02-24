Spark MLLib
=======
This Repo is for work/play with Spark MLLib's data.

Data
-------
The data come from [NFL Savant](http://nflsavant.com/about.php). There are 2 files to be used: 
1) [Players CSV](http://nflsavant.com/dump/players_2013-12-12.csv)
2) [Combine CSV](http://nflsavant.com/dump/combine.csv?year=2015)

The Combine CSV needs to be cleansed. Names that include "Jr" have an extra comma and need to be removed.

External Libraries
---------
To turn the CSV's into Dataframes I used the [spark-csv library](https://github.com/databricks/spark-csv).

DecisionTreeFootball
-----------
This creates a Decision Tree to classify if the a player will be a "Long Term" player in the NFL using combine stats.

KMeansFootball
---------
WIP - Currently does KMeans but is not done.  I am planning on doing anomaly detection with KMeans to find anomalies ("Long Term" NFLer's).

LinearRegressionFootball
---------
This creates a Linear Regression Model to classify if the a player will be a "Long Term" player in the NFL using combine stats.  Currently, this does not do a good job, as "Long Term" players are very rare, which results in the model always classifying as a "Short Term" player.

LinearSVMFootball
---------
This creates a Linear SVM Model to classify if the a player will be a "Long Term" player in the NFL using combine stats.  Similar to LinearRegressionFootball, this does not do a good job, as "Long Term" players are very rare, which results in the model always classifying as a "Short Term" player.

RandomForestFootball
----------
This creates a Random Forest to classify if the a player will be a "Long Term" player in the NFL using combine stats. This generally has about double the error rate as the single decision tree.

TODO
--------
Work with the models to fine tune the data and work on other models to determine "Long Term" players

Notes
--------
The combine data is not the best; lots of fields are missing such as "hands", "arms", "twentyyd", etc.  This reduces the effects of any machine learning.  There is also a lack of data, which reduces the ability of the models to be trained.  Another problem is my lack of knowledge about Data Science and machine learning.
