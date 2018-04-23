package fr.aphp.wind.pdfextractor;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

public class PdfExtractor implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Configuration conf;
	private Double pdfLength;
	private Long lastModified;
	private StringBuilder bodySb;
	private StringBuilder footSb;
	private StringBuilder leftSb;
	private String lastModifiedFormated = "1515-01-01 00:00:00";
	private String id_doc = "INCONNU";

	public PdfExtractor() {
	}

	public String extractAndWrite(ByteBuffer pdfBuf, String pdfFilePath
			, Long lastModified, Double pdfLenght)
			throws Exception {
		// where pdf is a ByteBuffer from my Avro stream
		int size = pdfBuf.remaining();
		byte[] buf = new byte[size];
		pdfBuf.get(buf, 0, size);
		this.lastModified = lastModified;
		setLastModifiedString();
		this.pdfLength = pdfLenght;
		this.id_doc = pdfFilePath.replaceAll("\\..*$", "").replaceAll("^.*/", "");

		try {
			PDDocument document = PDDocument.load(buf);
			this.basicExtract(document);
			document.close();
		} catch (Exception e) {
			bodySb = new StringBuilder();
			leftSb = new StringBuilder();
			footSb = new StringBuilder();
		}
		return getCsv();
	}


	public void extractAndWrite(String pdfFilePath) throws InvalidPasswordException, IOException {
		File file = new File(pdfFilePath);

		lastModified = file.lastModified();
		pdfLength = (double) file.length();
		setLastModifiedString();

		// 1. CHARGEMENT DOC$
		try {
			PDDocument document = PDDocument.load(file);
				this.basicExtract(document);
			document.close();
		} catch (Exception e) {
			bodySb = new StringBuilder();
			leftSb = new StringBuilder();
			footSb = new StringBuilder();
		}

	}

	private void setLastModifiedString() {
		if (lastModified != null) {

			Date date = new Date(lastModified);
			SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			lastModifiedFormated = formatDate.format(date);
		}

	}

	private void basicExtract(PDDocument document) throws InvalidPasswordException, IOException {

		//
		// Rectangle(int x, int y, int width, int height)
		// Constructs a new Rectangle whose upper-left corner is specified as
		// (x,y)
		// and whose width and height are specified by the arguments of the same
		// name.
		//
		bodySb = new StringBuilder();
		leftSb = new StringBuilder();
		footSb = new StringBuilder();

		// 2. PREMIER STRIPPER
		PDFTextStripper pdfStripper = new PDFTextStripper();
		bodySb.append(pdfStripper.getText(document));
		

	}
	


	public String getString() {
		return String.format("%s<@>%s<@>%s<@>%s", bodySb.toString().replaceAll("<@>", ""),
				leftSb.toString().replaceAll("<@>", ""), pdfLength / 1024 / 1024, lastModified);
	}

	public String getCsv() {
		return String.format("%s;%s;%s;%s;%s;%s", this.id_doc, pdfLength / 1024 / 1024, lastModifiedFormated,
				prepareString(bodySb.toString()), prepareString(leftSb.toString()), prepareString(footSb.toString()));
	}

	public String prepareString(String str) {
		if (str.equals("")) {
			return "";
		} else {
			return "\"" + str
					.replaceAll("\"", "\"\"") // for quoting
					.replaceAll("(?m)^\\.$", "\"\"\\.\"\"") //because of postgresql parser
					+ "\"";
		}

	}
}
