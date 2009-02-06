/*
 * de.xwic.etlgine.loader.cube.CubeLoader 
 */
package de.xwic.etlgine.loader.cube;

import de.xwic.cube.ICube;
import de.xwic.cube.IDataPool;
import de.xwic.cube.IDimension;
import de.xwic.cube.IDimensionElement;
import de.xwic.cube.IMeasure;
import de.xwic.cube.Key;
import de.xwic.cube.StorageException;
import de.xwic.etlgine.AbstractLoader;
import de.xwic.etlgine.ETLException;
import de.xwic.etlgine.IProcessContext;
import de.xwic.etlgine.IRecord;

/**
 * Loads the data into a cube.
 * @author lippisch
 */
public class CubeLoader extends AbstractLoader {

	private IDataPoolProvider dataPoolProvider;
	private DataPoolInitializer dataPoolInitializer = null;
	private IDataPool dataPool;
	protected ICube cube;
	
	private String targetCubeKey = null;
	protected ICubeDataMapper dataMapper = null;
	
	private boolean saveDataPoolOnFinish = false;
	private boolean clearCubeBeforeStart = false;
	
	/**
	 * Constructor.
	 * @param dataPoolProvider
	 */
	public CubeLoader(IDataPoolProvider dataPoolProvider) {
		this.dataPoolProvider = dataPoolProvider;
	}
	
	/* (non-Javadoc)
	 * @see de.xwic.etlgine.impl.AbstractLoader#initialize(de.xwic.etlgine.IETLContext)
	 */
	@Override
	public void initialize(IProcessContext processContext) throws ETLException {
		super.initialize(processContext);
		dataPool = dataPoolProvider.getDataPool(processContext);
		if (targetCubeKey == null) {
			throw new ETLException("The target cube key is not specified.");
		}
		if (dataMapper == null) {
			throw new ETLException("No ICubeDataMapper specified.");
		}
		if (dataPoolInitializer != null) {
			try {
				dataPoolInitializer.verify(dataPool);
			} catch (Exception e) {
				throw new ETLException("Error verifying DataPool integerity.", e);
			}
		}
		
		if (!dataPool.containsCube(targetCubeKey)) {
			throw new ETLException("The DataPool does not contain a cube with the key " + targetCubeKey + ".");
		}
		cube = dataPool.getCube(targetCubeKey);
		dataMapper.initialize(processContext, cube);
		
		if (isClearCubeBeforeStart()) {
			dataMapper.clearCube(cube);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see de.xwic.etlgine.ILoader#processRecord(de.xwic.etlgine.IETLContext, de.xwic.etlgine.IRecord)
	 */
	public void processRecord(IProcessContext processContext, IRecord record) throws ETLException {

		if (dataMapper.accept(record)) {
		
			Key key = cube.createKey("");
			int idx = 0;
			for (IDimension dim : cube.getDimensions()) {
				
				IDimensionElement element = dataMapper.getElement(dim, record);
				if (element == null) {
					// invalid data
					return;
				}
				key.setDimensionElement(idx++, element);
				
			}
			
			for (IMeasure measure : dataMapper.getMeasures()) {
				Double value = dataMapper.getValue(measure, record);
				if (value != null) {
					cube.addCellValue(key, measure, value);
				}
			}
		}
		
	}
	
	/* (non-Javadoc)
	 * @see de.xwic.etlgine.AbstractLoader#onProcessFinished(de.xwic.etlgine.IProcessContext)
	 */
	@Override
	public void onProcessFinished(IProcessContext processContext) throws ETLException {
		super.onProcessFinished(processContext);
		if (isSaveDataPoolOnFinish()) {
			try {
				processContext.getMonitor().logInfo("Storing DataPool...");
				dataPool.save();
				processContext.getMonitor().logInfo("Storing DataPool finished...");
			} catch (StorageException e) {
				throw new ETLException("Error saving dataPool");
			}
		}
	}

	/**
	 * @return the targetCubeKey
	 */
	public String getTargetCubeKey() {
		return targetCubeKey;
	}

	/**
	 * @param targetCubeKey the targetCubeKey to set
	 */
	public void setTargetCubeKey(String targetCubeKey) {
		this.targetCubeKey = targetCubeKey;
	}

	/**
	 * @return the dataPoolInitializer
	 */
	public DataPoolInitializer getDataPoolInitializer() {
		return dataPoolInitializer;
	}

	/**
	 * @param dataPoolInitializer the dataPoolInitializer to set
	 */
	public void setDataPoolInitializer(DataPoolInitializer dataPoolInitializer) {
		this.dataPoolInitializer = dataPoolInitializer;
	}

	/**
	 * @return the dataMapper
	 */
	public ICubeDataMapper getDataMapper() {
		return dataMapper;
	}

	/**
	 * @param dataMapper the dataMapper to set
	 */
	public void setDataMapper(ICubeDataMapper dataMapper) {
		this.dataMapper = dataMapper;
	}

	/**
	 * @return the saveDataPoolOnFinish
	 */
	public boolean isSaveDataPoolOnFinish() {
		return saveDataPoolOnFinish;
	}

	/**
	 * @param saveDataPoolOnFinish the saveDataPoolOnFinish to set
	 */
	public void setSaveDataPoolOnFinish(boolean saveDataPoolOnFinish) {
		this.saveDataPoolOnFinish = saveDataPoolOnFinish;
	}

	/**
	 * @return the clearCubeBeforeStart
	 */
	public boolean isClearCubeBeforeStart() {
		return clearCubeBeforeStart;
	}

	/**
	 * @param clearCubeBeforeStart the clearCubeBeforeStart to set
	 */
	public void setClearCubeBeforeStart(boolean clearCubeBeforeStart) {
		this.clearCubeBeforeStart = clearCubeBeforeStart;
	}
	
}
