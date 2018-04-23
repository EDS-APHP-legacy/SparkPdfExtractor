GOAL
-----

- pdfs are serialized into AVRO
- AVRO si distributed as a spark RDD in X partitions
- each partition is collected and stored as a csv part
- csv are then merged, and compressed
- archive goes back to application serveur that load the postgresql table

PERFORMANCES
------------

- 50 Million of pdf of 3 pages average were transformed and dumped to text for 2 hours of runtime

BUILD
-----

- `make build`


USE (yarn)
----------------
1. transform the pdf to avro (see PdfAvro folder)
1. push 2 jars on the spark computer cluster
1. spark-submit --jars wind-pdf-extractor-1.0-SNAPSHOT-jar-with-dependencies.jar --driver-java-options "-Dlog4j.configuration=file:log4jmaster" --conf "spark.executor.extraJavaOptions=-Dlog4j.configuration=file:log4jslave" --num-executors 120  --executor-cores 1  --master yarn  pdfextractor_2.11-0.1.0-SNAPSHOT.jar inputAvroHdfsFolder/ outputCsvHdfsFolder/ 400`
1. it is crucial to put only one executor core


CONFIGURATION
------------

- ulimit -n 64000 (default is 1024, way too low)


READING
--------

- [problem is hdfs does not manage well many little files](http://hadooptutorial.info/merging-small-files-into-sequencefile/)
- [avro is a better choice](http://hadooptutorial.info/merging-small-files-into-avro-file/)
- then the idea :
	1. put the pdf as bytes into an avro file
	1. [transform the avro into a spark RDD](https://stackoverflow.com/questions/23944615/how-can-i-load-avros-in-spark-using-the-schema-on-board-the-avro-files)
	1. [run pdfbox with the bytes using a ByteArrayInputStream](https://stackoverflow.com/questions/29123436/how-to-sign-an-inputstream-from-a-pdf-file-with-pdfbox-2-0-0) 
	1. [append the result as an avro file into hdfs](http://www.landoop.com/blog/2017/05/fast-avro-write/) [OR](http://www.landoop.com/blog/2017/05/fast-avro-write/) 
