/**
 * 
 */
package de.xwic.etlgine.server.admin.datapool;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.jwic.base.IControlContainer;
import de.jwic.controls.Button;
import de.jwic.controls.ErrorWarning;
import de.jwic.controls.ToolBar;
import de.jwic.controls.ToolBarGroup;
import de.jwic.events.SelectionEvent;
import de.jwic.events.SelectionListener;
import de.xwic.cube.ICube;
import de.xwic.cube.IDataPool;
import de.xwic.cube.IDimension;
import de.xwic.cube.StorageException;
import de.xwic.cube.util.JDBCSerializerUtil;
import de.xwic.cube.webui.controls.DimensionElementSelector;
import de.xwic.etlgine.cube.CubeHandler;
import de.xwic.etlgine.jdbc.JDBCUtil;
import de.xwic.etlgine.server.ETLgineServer;
import de.xwic.etlgine.server.ServerContext;
import de.xwic.etlgine.server.admin.BaseContentContainer;
import de.xwic.etlgine.server.admin.ImageLibrary;
import de.xwic.etlgine.server.admin.StackedContentContainer;

/**
 * @author Developer
 *
 */
public class DPDetailsControl extends BaseContentContainer {

	private final String dataPoolManagerKey;
	private IDataPool dataPool;

	private Map<IDimension, DimensionElementSelector> selectorMap = new HashMap<IDimension, DimensionElementSelector>();
	private Button btSave;
	private Button btMapping;
	private String syncTableConnectionName;
	private ServerContext context;
	
	private ErrorWarning errInfo;
	private CubeHandler cubeHandler;
	
	private CubeDownloadControl cubeDownload;
	
	/**
	 * @param container
	 * @param name
	 */
	public DPDetailsControl(IControlContainer container, String name, String dataPoolManagerKey) {
		super(container, name);
		this.dataPoolManagerKey = dataPoolManagerKey;
		
		context = ETLgineServer.getInstance().getServerContext();
		cubeHandler = CubeHandler.getCubeHandler(context);
		
		setTitle("DataPool Details (" + dataPoolManagerKey + ")");
		
		errInfo = new ErrorWarning(this, "errInfo");
		
		ToolBar abar = new ToolBar(this, "actionBar");
		ToolBarGroup group = abar.addGroup();
		Button btReturn = group.addButton();
		btReturn.setIconEnabled(ImageLibrary.IMAGE_RETURN);
		btReturn.setTitle("Return");
		btReturn.addSelectionListener(new SelectionListener() {
			public void objectSelected(SelectionEvent event) {
				close();
			}
		});

		btSave = group.addButton();
		btSave.setIconEnabled(ImageLibrary.IMAGE_DATABASE_SAVE);
		btSave.setTitle("Save to InitTable");
		btSave.addSelectionListener(new SelectionListener() {
			public void objectSelected(SelectionEvent event) {
				onSaveToInit();
			}
		});
		
		btMapping = group.addButton();
		btMapping.setIconEnabled(ImageLibrary.IMAGE_TABLE_RELATIONSHIP);
		btMapping.setTitle("Edit Mappings");
		btMapping.addSelectionListener(new SelectionListener() {
			public void objectSelected(SelectionEvent event) {
				onEditMappings();
			}
		});

		Button btSavePool = group.addButton();
		btSavePool.setTitle("Save Datapool");
		btSavePool.setIconEnabled(ImageLibrary.IMAGE_DATABASE_SAVE);
		btSavePool.addSelectionListener(new SelectionListener() {
			/* (non-Javadoc)
			 * @see de.jwic.events.SelectionListener#objectSelected(de.jwic.events.SelectionEvent)
			 */
			public void objectSelected(SelectionEvent event) {
				onSavePool();
			}
		});

		Button btXlsTest = group.addButton();
		btXlsTest.setTitle("Test XLS Template");
		btXlsTest.setIconEnabled(ImageLibrary.IMAGE_PAGE_EXCEL);
		btXlsTest.addSelectionListener(new SelectionListener() {
			/* (non-Javadoc)
			 * @see de.jwic.events.SelectionListener#objectSelected(de.jwic.events.SelectionEvent)
			 */
			public void objectSelected(SelectionEvent event) {
				onXlsTest();
			}
		});

		
		cubeDownload = new CubeDownloadControl(this, "cubeDownload");
		
		loadDataPoolInfo();
		
	}
	
	/**
	 * 
	 */
	protected void onXlsTest() {

		StackedContentContainer sc = (StackedContentContainer)getContainer();
		XlsTemplateTestControl xlsTest = new XlsTemplateTestControl(sc, null, dataPool);
		sc.setCurrentControlName(xlsTest.getName());		
		
		
	}
	
	/**
	 * 
	 */
	protected void onSavePool() {

		try {
			dataPool.save();
			errInfo.showWarning("DataPool saved.");
		} catch (StorageException e) {
			errInfo.showError(e);
		}
		
	}

	/**
	 * 
	 */
	protected void onEditMappings() {
		
		StackedContentContainer sc = (StackedContentContainer)getContainer();
		DPMappingControl dpMapEditor = new DPMappingControl(sc, null, dataPoolManagerKey, syncTableConnectionName);
		sc.setCurrentControlName(dpMapEditor.getName());		
		
	}

	/**
	 * 
	 */
	protected void onSaveToInit() {
		
		try {
			Connection connection = JDBCUtil.openConnection(context, syncTableConnectionName);
			try {
				JDBCSerializerUtil.storeMeasures(connection, dataPool, "XCUBE_MEASURES");
				
				JDBCSerializerUtil.storeDimensions(connection, dataPool, "XCUBE_DIMENSIONS", "XCUBE_DIMENSION_ELEMENTS");
				
				errInfo.showWarning("Database Updated");
			} finally {
				connection.close();
			}
		} catch (Exception e) {
			log.error("Error saving to sync table.", e);
			errInfo.showError(e);
		}
		
	}

	/**
	 * 
	 */
	public void actionDimEdit(String dimKey) {
		
		StackedContentContainer sc = (StackedContentContainer)getContainer();
		DimensionEditorControl dimEditor = new DimensionEditorControl(sc, null, dataPool.getDimension(dimKey));
		sc.setCurrentControlName(dimEditor.getName());		
		
	}
	
	/**
	 * Export the Cube with leafs only.
	 * @param cubeKey
	 */
	public void actionExportCube(String cubeKey) {
		
		ICube cube = dataPool.getCube(cubeKey);
		cubeDownload.startDownload(cube, true);
		
	}
	
	/**
	 * View the cube details.
	 * @param cubeKey
	 */
	public void actionViewCube(String cubeKey) {
		
		ICube cube = dataPool.getCube(cubeKey);
		StackedContentContainer sc = (StackedContentContainer)getContainer();
		CubeDetailsControl control = new CubeDetailsControl(sc, null, cube);
		sc.setCurrentControlName(control.getName());		

	}

	/**
	 * Export the cube including all cells.
	 * @param cubeKey
	 */
	public void actionExportFullCube(String cubeKey) {
		
		ICube cube = dataPool.getCube(cubeKey);
		cubeDownload.startDownload(cube, false);
		
	}

	/**
	 * 
	 */
	private void loadDataPoolInfo() {
		
		syncTableConnectionName = context.getProperty(dataPoolManagerKey + ".datapool.syncTables.connection");
		
		try {
			dataPool = cubeHandler.openDataPool(dataPoolManagerKey);
			for (IDimension dim : dataPool.getDimensions()) {
				DimensionElementSelector dsc = new DimensionElementSelector(this, null, dim);
				dsc.setWidth(248);
				selectorMap.put(dim, dsc);
			}
		} catch (Exception e) {
			log.error("Error loading datapool", e);
		}
		
		btSave.setEnabled(syncTableConnectionName != null);
		btMapping.setEnabled(syncTableConnectionName != null);
	}

	/**
	 * Returns the name of the Selector.
	 * @param dim
	 * @return
	 */
	public String getSelectorName(IDimension dim) {
		return selectorMap.get(dim).getName();
	}
	
	/**
	 * 
	 */
	protected void close() {
		destroy();
	}

	/**
	 * @return the dataPoolManagerKey
	 */
	public String getDataPoolManagerKey() {
		return dataPoolManagerKey;
	}

	/**
	 * @return the dataPool
	 */
	public IDataPool getDataPool() {
		return dataPool;
	}

	/**
	 * Returns a sorted list of dimensions.
	 * @return
	 */
	public List<IDimension> getDimensions() {
		
		List<IDimension> dimList = new ArrayList<IDimension>();
		dimList.addAll(dataPool.getDimensions());
		Collections.sort(dimList, new Comparator<IDimension> () {
			@Override
			public int compare(IDimension o1, IDimension o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});
		return dimList;
		
	}

	/**
	 * Returns a sorted list of dimensions.
	 * @return
	 */
	public List<ICube> getCubes() {
		
		List<ICube> cubeList = new ArrayList<ICube>();
		cubeList.addAll(dataPool.getCubes());
		Collections.sort(cubeList, new Comparator<ICube> () {
			@Override
			public int compare(ICube o1, ICube o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});
		return cubeList;
		
	}

	
}
