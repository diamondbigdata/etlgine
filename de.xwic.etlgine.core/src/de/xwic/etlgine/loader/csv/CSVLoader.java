/**
 * 
 */
package de.xwic.etlgine.loader.csv;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import sun.nio.cs.StreamEncoder;
import au.com.bytecode.opencsv.CSVWriter;
import de.xwic.etlgine.AbstractLoader;
import de.xwic.etlgine.ETLException;
import de.xwic.etlgine.IColumn;
import de.xwic.etlgine.ILoader;
import de.xwic.etlgine.IProcessContext;
import de.xwic.etlgine.IRecord;

/**
 * Writes the data into a CSV file.
 * @author Lippisch
 */
public class CSVLoader extends AbstractLoader implements ILoader {

	private boolean containsHeader = true;
	private char separator = ',';
	private char quoteChar = '"';
	private String filename = null;
	
	private boolean zipOutput = false;
	private String zipFilename = null;
	private CSVWriter writer;
	
	private int colCount = 0;
	private IColumn[] exportCols = null;
	
	private String encoding = null;
	
	private ZipOutputStream zipOut = null;
	
	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * @return the containsHeader
	 */
	public boolean isContainsHeader() {
		return containsHeader;
	}

	/**
	 * @param containsHeader the containsHeader to set
	 */
	public void setContainsHeader(boolean containsHeader) {
		this.containsHeader = containsHeader;
	}

	/**
	 * @return the separator
	 */
	public char getSeparator() {
		return separator;
	}

	/**
	 * @param separator the separator to set
	 */
	public void setSeparator(char separator) {
		this.separator = separator;
	}

	/**
	 * @return the quoteChar
	 */
	public char getQuoteChar() {
		return quoteChar;
	}

	/**
	 * @param quoteChar the quoteChar to set
	 */
	public void setQuoteChar(char quoteChar) {
		this.quoteChar = quoteChar;
	}

	/* (non-Javadoc)
	 * @see de.xwic.etlgine.ILoader#initialize(de.xwic.etlgine.IETLContext)
	 */
	@Override
	public void initialize(IProcessContext processContext) throws ETLException {
		
		try {
			OutputStream out;
			if (zipOutput) {
				FileOutputStream osZipFile = new FileOutputStream(zipFilename);
				BufferedOutputStream bos = new BufferedOutputStream(osZipFile);
				zipOut = new ZipOutputStream(bos);
				
				ZipEntry entry = new ZipEntry(filename);
				zipOut.putNextEntry(entry);
				
				out = zipOut;
			} else {
				out = new BufferedOutputStream(new FileOutputStream(filename));
			}
			Writer w = new BufferedWriter(StreamEncoder.forOutputStreamWriter(out, filename, encoding));
			writer = new CSVWriter(w, separator, quoteChar);
		} catch (IOException e) {
			throw new ETLException("Error creating file " + filename + ": " + e, e);
		}

	}

	/* (non-Javadoc)
	 * @see de.xwic.etlgine.ILoader#onProcessFinished(de.xwic.etlgine.IETLContext)
	 */
	@Override
	public void onProcessFinished(IProcessContext processContext) throws ETLException {
		try {
			writer.flush();
			if (zipOut != null) {
				zipOut.closeEntry();
			}
			writer.close();
		} catch (IOException e) {
			throw new ETLException("Error closing writer: " + e, e);
		}
	}

	/* (non-Javadoc)
	 * @see de.xwic.etlgine.ILoader#preSourceProcessing(de.xwic.etlgine.IETLContext)
	 */
	@Override
	public void preSourceProcessing(IProcessContext processContext) {

		if (containsHeader) {
			List<IColumn> columns = processContext.getDataSet().getColumns();
			for (IColumn col : columns) {
				if (!col.isExclude()) {
					colCount++;
				}
			}
			String[] data = new String[colCount];
			exportCols = new IColumn[colCount];
			int i = 0;
			for (IColumn col : columns) {
				if (!col.isExclude()) {
					exportCols[i] = col;
					data[i++] = col.computeTargetName();
				}
			}
			writer.writeNext(data);
		}
		
	}

	/* (non-Javadoc)
	 * @see de.xwic.etlgine.ILoader#processRecord(de.xwic.etlgine.IETLContext, de.xwic.etlgine.IRecord)
	 */
	public void processRecord(IProcessContext processContext, IRecord record) throws ETLException {

		String[] data = new String[colCount];
		for (int i = 0; i < data.length; i++) {
			Object value = record.getData(exportCols[i]);
			data[i] = value != null ? value.toString() : "";
		}
		writer.writeNext(data);
		
	}

	/**
	 * @return the encoding
	 */
	public String getEncoding() {
		return encoding;
	}
	
	/**
	 * @param encoding the encoding to set
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * @return the zipOutput
	 */
	public boolean isZipOutput() {
		return zipOutput;
	}

	/**
	 * @param zipOutput the zipOutput to set
	 */
	public void setZipOutput(boolean zipOutput) {
		this.zipOutput = zipOutput;
	}

	/**
	 * @return the zipFilename
	 */
	public String getZipFilename() {
		return zipFilename;
	}

	/**
	 * @param zipFilename the zipFilename to set
	 */
	public void setZipFilename(String zipFilename) {
		this.zipFilename = zipFilename;
	}
}
