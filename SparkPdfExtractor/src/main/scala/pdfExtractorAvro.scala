package fr.aphp.wind

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.deploy.SparkHadoopUtil
import org.apache.hadoop.fs.{FileSystem, Path, LocatedFileStatus, RemoteIterator}
import java.net.URI
import fr.aphp.wind.pdfextractor
//FROM http://www.bigdatatidbits.cc/2015/01/how-to-load-some-avro-data-into-spark.html
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.mapreduce.AvroKeyInputFormat
import org.apache.avro.mapred.AvroKey
import org.apache.hadoop.io.NullWritable
import org.apache.avro.mapred.AvroInputFormat
import org.apache.avro.mapred.AvroWrapper
import org.apache.avro.generic.GenericRecord
import org.apache.avro.mapred.{AvroInputFormat, AvroWrapper}
import org.apache.hadoop.io.NullWritable
import java.nio.ByteBuffer
//For HDFS MERGE
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs._

/**
 * Extract text from pdf.
 *
 * Usage: PdfExtractor [input_path, output_path, numberPartition]
 */
object PdfExtractorAvro {

  @transient lazy val tt = new fr.aphp.wind.pdfextractor.PdfExtractor()

  def main(args: Array[String]) {
    val sparkConf = new SparkConf().setAppName("Pdf Extractor AVRO")
    val sc = new SparkContext(sparkConf)

    // The Avro records get converted to Spark types, filtered, and
    // then written back out to csv record
    val avro_path = args(0)
    val output_path = args(1)
    val output_file = args(2)
    val numberPartition = args(3)

    val avroRDD = sc.hadoopFile[AvroWrapper[GenericRecord], NullWritable, AvroInputFormat[GenericRecord]](avro_path, minPartitions=numberPartition.toInt)

    avroRDD.map( row => {
      tt.extractAndWrite( 
          row._1.datum.get(1).asInstanceOf[java.nio.ByteBuffer] 
        , row._1.datum.get(0).toString 
        , row._1.datum.get(2).asInstanceOf[Long] 
        , row._1.datum.get(3).asInstanceOf[Double]
        )
    }).saveAsTextFile(output_path)

    merge(output_path, output_file)
    sc.stop()
  }

  //cf: https://dzone.com/articles/spark-write-csv-file
  def merge(srcPath: String, dstPath: String): Unit =  {
    val hadoopConfig = new Configuration()
    val hdfs = FileSystem.get(hadoopConfig)
    FileUtil.copyMerge(hdfs, new Path(srcPath), hdfs, new Path(dstPath), false, hadoopConfig, null)

  }

}
// scalastyle:on println

