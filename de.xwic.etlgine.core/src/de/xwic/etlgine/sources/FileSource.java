/*
 * de.xwic.etlgine.sources.FileSource 
 */
package de.xwic.etlgine.sources;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.xwic.etlgine.ISource;

/**
 * @author lippisch
 */
public class FileSource implements ISource {

	protected String filename = null;
	protected File file = null;
	protected boolean optional = false;
	protected String encoding = null;
	
	/**
	 * Default Constructor.
	 */
	public FileSource() {
		
	}
	
	/**
	 * Construct a new fileSource for the specified filename.
	 * @param filename
	 */
	public FileSource(String filename) {
		setFilename(filename);
	}
	
	/**
	 * Construct a new FileSource from a file.
	 * @param file
	 */
	public FileSource(File file) {
		setFile(file);
	}
	
	/* (non-Javadoc)
	 * @see de.xwic.etlgine.ISource#getName()
	 */
	public String getName() {
		return filename;
	}

	/* (non-Javadoc)
	 * @see de.xwic.etlgine.ISource#isAvailable()
	 */
	public boolean isAvailable() {
		return file.exists() && !file.isDirectory() && file.length() > 0;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Sets the filename by calling setFile(new File(filename)).
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		try {
			filename = new File(filename).getCanonicalPath();
		} catch (IOException e) {}
		setFile(new File(filename));
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		this.file = file;
		if (file != null) {
			filename = file.getAbsolutePath();
		} else {
			filename = null;
		}
	}

	/**
	 * @return the optional
	 */
	public boolean isOptional() {
		return optional;
	}

	/**
	 * @param optional the optional to set
	 */
	public void setOptional(boolean optional) {
		this.optional = optional;
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
	 * @return new InputStream for file.
	 * @throws IOException
	 */
	public InputStream getInputStream() throws IOException {
		return new FileInputStream(file);
	}
}
