build: buildpdfbox buildavro buildspark

buildpdfbox:
	(cd PdfboxPojo && mvn install) &&\
	cp PdfboxPojo/target/wind-pdf-extractor-1.0-SNAPSHOT-jar-with-dependencies.jar SparkPdfExtractor/lib/

buildavro:
	(cd PdfAvro && mvn install) &&\
	cp PdfAvro/target/PdfAvro-0.0.1-SNAPSHOT.jar SparkPdfExtractor/lib/

buildspark:
	(cd SparkPdfExtractor && sbt publish-local)
