#export SPARK_CLASSPATH=/usr/hdp/current/phoenix-client/lib/*; pyspark

from pyspark.sql import SQLContext
from pandas import DataFrame, Series
import pandas
sqlContext = SQLContext(sc)

df = sqlContext.load(source="org.apache.phoenix.spark", zKUrl="localhost:2181:/hbase-unsecure", table="doctors")
pandas_df = df.toPandas()
