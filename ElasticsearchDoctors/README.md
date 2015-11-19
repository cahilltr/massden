#Elasticsearch Doctors
This project uses Elasticsearch to index a list of doctors (National Downloadable File) and use the Parent-Child structure provided to create appointements for each doctor.  Indexing is done via the Python API.  Search and adding/removing appointments is done via the Java API.  There is a first draft of a simulation to simulate load and create appointments.  Also, work is being done to create a nice API to access data and index appointments.  When the API is complete, the simulation will be rewritten.

##Resources
https://data.medicare.gov/data/physician-compare - National Downloadable File
https://elasticsearch-py.readthedocs.org/en/master/ - Elasticsearch Python
https://www.elastic.co/guide/en/elasticsearch/client/python-api/current/index.html - Elasticsearch Python
