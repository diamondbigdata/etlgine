/*
 * de.xwic.etlgine.impl.Record 
 */
package de.xwic.etlgine.impl;

import java.util.HashMap;
import java.util.Map;

import de.xwic.etlgine.ETLException;
import de.xwic.etlgine.IColumn;
import de.xwic.etlgine.IDataSet;
import de.xwic.etlgine.IRecord;

/**
 * @author lippisch
 */
public class Record implements IRecord {

	protected final IDataSet dataSet;
	protected boolean invalid = false;
	protected boolean skip = false;
	protected String invalidReason = null;
	protected Map<IColumn, Object> data = new HashMap<IColumn, Object>();
	
	/**
	 * @param dataSet
	 */
	public Record(IDataSet dataSet) {
		this.dataSet = dataSet;
	}

	/**
	 * Set a value.
	 * @param column
	 * @param value
	 */
	public void setData(IColumn column, Object value) {
		data.put(column, value);
	}
	
	/**
	 * Change the data.
	 * @param columnName
	 * @param value
	 * @throws ETLException
	 */
	public void setData(String columnName, Object value) throws ETLException {
		IColumn column = dataSet.getColumn(columnName);
		setData(column, value);
	}
	
	/**
	 * Returns the data with the specified column name.
	 * @param columnName
	 * @return
	 * @throws ETLException
	 */
	public Object getData(String columnName) throws ETLException {
		IColumn column = dataSet.getColumn(columnName);
		return getData(column);
	}
	
	/**
	 * Returns the data in the specified column.
	 * @param column
	 * @return
	 * @throws ETLException
	 */
	public Object getData(IColumn column) throws ETLException {
		return data.get(column);
	}

	/**
	 * Mark the record as invalid.
	 * @param reason
	 */
	public void markInvalid(String reason) {
		setInvalid(true);
		setInvalidReason(reason);
	}
	
	/**
	 * @return the invalid
	 */
	public boolean isInvalid() {
		return invalid;
	}

	/**
	 * @param invalid the invalid to set
	 */
	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	/**
	 * @return the invalidReason
	 */
	public String getInvalidReason() {
		return invalidReason;
	}

	/**
	 * @param invalidReason the invalidReason to set
	 */
	public void setInvalidReason(String invalidReason) {
		this.invalidReason = invalidReason;
	}

	/* (non-Javadoc)
	 * @see de.xwic.etlgine.IRecord#getDataAsString(java.lang.String)
	 */
	public String getDataAsString(String columnName) throws ETLException {
		IColumn column = dataSet.getColumn(columnName);
		return getDataAsString(column);
	}

	/* (non-Javadoc)
	 * @see de.xwic.etlgine.IRecord#getDataAsString(de.xwic.etlgine.IColumn)
	 */
	public String getDataAsString(IColumn column) throws ETLException {
		Object val = getData(column);
		return val != null ? val.toString() : null;
	}
	
	/* (non-Javadoc)
	 * @see de.xwic.etlgine.IRecord#getDataAsDouble(java.lang.String)
	 */
	public Double getDataAsDouble(String columnName) throws ETLException {
		IColumn column = dataSet.getColumn(columnName);
		return getDataAsDouble(column);
	}
	/* (non-Javadoc)
	 * @see de.xwic.etlgine.IRecord#getDataAsDouble(de.xwic.etlgine.IColumn)
	 */
	public Double getDataAsDouble(IColumn column) throws ETLException {
		Object val = getData(column);
		if (val instanceof Double) {
			return (Double)val;
		} else if (val instanceof String) {
			String s = (String)val;
			if (s.length() == 0) {
				return null;
			}
			return Double.parseDouble(s);
		}
		return null;
		
	}

	/**
	 * @return the skip
	 */
	public boolean isSkip() {
		return skip;
	}

	/**
	 * @param skip the skip to set
	 */
	public void setSkip(boolean skip) {
		this.skip = skip;
	}
	
}
