AVROTOOLS
==========

BUILD
----

- `mvn install`

USE
---

1. put files into a directory
2. transform the files into an avro file on hdfs: `hadoop jar PdfAvro-0.0.1-SNAPSHOT.jar  fr.aphp.wind.PdfAvro.PdfMultipleToAvroFile.java myLocalFolder/ myHdfsPath/myAvroFile`




