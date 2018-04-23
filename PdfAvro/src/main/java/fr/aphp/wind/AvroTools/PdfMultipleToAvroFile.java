package fr.aphp.wind.AvroTools;


import org.apache.avro.Schema;
import org.apache.avro.file.*;
import org.apache.avro.generic.*;
import org.apache.avro.io.DatumWriter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;

import java.io.*;
import java.nio.ByteBuffer;

public class PdfMultipleToAvroFile {

  public static final String FILENAME = "filename";
  public static final String CONTENTS = "contents";
  public static final String MTIME = "modiftime";
  public static final String SIZE = "filesize";
  private static final String SCHEMA_JSON = 
          "{\"type\": \"record\", \"name\": \"SmallFilesToAvroFile\", "
          + "\"fields\": ["
          + "{\"name\":\"" + FILENAME
          + "\", \"type\":\"string\"},"
          + "{\"name\":\"" + CONTENTS
          + "\", \"type\":\"bytes\"},"
          + "{\"name\":\"" + MTIME
          + "\", \"type\":\"long\"},"
          + "{\"name\":\"" + SIZE
          + "\", \"type\":\"double\"}"
          + "]}";
  @SuppressWarnings("deprecation")
public static final Schema SCHEMA = Schema.parse(SCHEMA_JSON);

  @SuppressWarnings("resource")
public static void writeToAvro(File srcPath, OutputStream outputStream)
          throws IOException {
	DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<GenericRecord>(SCHEMA);  
    DataFileWriter<GenericRecord> writer = new DataFileWriter<GenericRecord>(datumWriter)
                .setSyncInterval(100);                
    writer.setCodec(CodecFactory.snappyCodec());   
    writer.create(SCHEMA, outputStream);           
    for (Object obj : FileUtils.listFiles(srcPath, null, false)) {
      File file = (File) obj;
      String filename = file.getAbsolutePath();
	  Long lastModified = file.lastModified();
	  double pdfLength = file.length();
      byte[] content = FileUtils.readFileToByteArray(file);
      GenericRecord record = new GenericData.Record(SCHEMA);  
      record.put(FILENAME, filename);                   
      record.put(CONTENTS, ByteBuffer.wrap(content));
      record.put(MTIME, lastModified);         
      record.put(SIZE, pdfLength);         
      writer.append(record);                                  
    }

    IOUtils.cleanup(null, writer);
    IOUtils.cleanup(null, outputStream);
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    FileSystem fs = FileSystem.get(conf);

    File inputDir = new File(args[0]);
    Path outFile = new Path(args[1]);

    OutputStream os = fs.create(outFile);
    writeToAvro(inputDir, os);
  }
}
