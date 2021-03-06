/*
 * de.xwic.etlgine.IColumn 
 */
package de.xwic.etlgine;


/**
 * @author lippisch
 */
public interface IColumn {

	public enum DataType {
		UNKNOWN,
		STRING,
		INT,
		LONG,
		DOUBLE,
		DATE,
		DATETIME,
		BOOLEAN,
		BIGDECIMAL
	}
	
	
	/**
	 * Remove the column name.
	 * @return
	 */
	public abstract String getName();

	/**
	 * Returns the source index.
	 * @return
	 */
	public abstract int getSourceIndex();

	/**
	 * Set the source index.
	 * @param sourceIndex
	 * @return
	 */
	public abstract void setSourceIndex(int sourceIndex);
	
	/**
	 * @return the exclude
	 */
	public boolean isExclude();

	/**
	 * If a column is excluded, the loader must ignore the column.
	 * @param exclude the exclude to set
	 */
	public void setExclude(boolean exclude);

	/**
	 * @return the targetName
	 */
	public String getTargetName();

	/**
	 * @param targetName the targetName to set
	 */
	public void setTargetName(String targetName);

	/**
	 * @return the typeHint
	 */
	public DataType getTypeHint();

	/**
	 * @param typeHint the typeHint to set
	 */
	public void setTypeHint(DataType typeHint);

	/**
	 * Returns the name for this column used by the loader.
	 * @return
	 */
	public String computeTargetName();

	/**
	 * @return the lengthHint
	 */
	public int getLengthHint();

	/**
	 * @param lengthHint the lengthHint to set
	 */
	public void setLengthHint(int lengthHint);

}
