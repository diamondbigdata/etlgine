/**
 * 
 */
package de.xwic.etlgine.server.admin.datapool;

import java.io.BufferedReader;
import java.io.StringReader;

import de.jwic.base.IControlContainer;
import de.jwic.controls.Button;
import de.jwic.controls.ErrorWarning;
import de.jwic.controls.InputBox;
import de.jwic.controls.Label;
import de.jwic.controls.ToolBar;
import de.jwic.controls.ToolBarGroup;
import de.jwic.controls.tableviewer.TableColumn;
import de.jwic.controls.tableviewer.TableModel;
import de.jwic.controls.tableviewer.TableViewer;
import de.jwic.events.ElementSelectedEvent;
import de.jwic.events.ElementSelectedListener;
import de.jwic.events.SelectionEvent;
import de.jwic.events.SelectionListener;
import de.xwic.cube.IDimension;
import de.xwic.cube.IDimensionElement;
import de.xwic.etlgine.server.admin.BaseContentContainer;
import de.xwic.etlgine.server.admin.ImageLibrary;

/**
 * @author Developer
 *
 */
public class DimensionEditorControl extends BaseContentContainer {

	private final IDimension dimension;
	private TableViewer table;
	private TableModel tableModel;
	
	private Label lblParent;
	private InputBox inpKey, inpTitle, inpWeight, inpMassInput;
	private Button btUpdate, btDelete, btMoveUp, btMoveDown, btMassInput;
	
	private IDimensionElement editedElement = null;
	private boolean insertMode = false;
	private boolean insertChild = false;

	private ErrorWarning errInfo;
	private Button btSeal;
	
	/**
	 * @param container
	 * @param name
	 */
	public DimensionEditorControl(IControlContainer container, String name, IDimension dimension) {
		super(container, name);
		this.dimension = dimension;
		
		setTitle("Dimension Editor (" + dimension.getKey() + ")");
		
		errInfo = new ErrorWarning(this, "errInfo");
				
		createActionBar();
		setupTable();
		setupEditor();
		
		setEditorEnabled(false);
		
	}

	/**
	 * @param b
	 */
	private void setEditorEnabled(boolean enabled) {
		
		inpKey.setEnabled(enabled);
		inpTitle.setEnabled(enabled);
		inpWeight.setEnabled(enabled);
		btUpdate.setEnabled(enabled);
		btDelete.setEnabled(enabled);
		btMoveUp.setEnabled(enabled && !insertMode && editedElement.getIndex() > 0);
		btMoveDown.setEnabled(enabled && !insertMode && editedElement.getIndex() + 1 < editedElement.getParent().getDimensionElements().size());
		
		btMassInput.setEnabled(enabled && insertMode);
	}

	/**
	 * 
	 */
	private void setupEditor() {
		
		lblParent = new Label(this, "lblParent");
				
		inpKey = new InputBox(this, "inpKey");
		inpKey.setWidth(200);
		
		inpTitle = new InputBox(this, "inpTitle");
		inpTitle.setWidth(200);
		
		inpWeight = new InputBox(this, "inpWeight");
		inpWeight.setWidth(60);
		
		inpMassInput = new InputBox(this, "inpMassInsert");
		inpMassInput.setWidth(450);
		inpMassInput.setHeight(100);
		inpMassInput.setMultiLine(true);
		
		btMassInput = new Button(this, "btMassInsert");
		btMassInput.setTitle("Insert All");
		btMassInput.addSelectionListener(new SelectionListener() {
			@Override
			public void objectSelected(SelectionEvent event) {
				onMassInput();
			}
		});
		
		btUpdate = new Button(this, "btUpdate");
		btUpdate.setTitle("Update");
		btUpdate.addSelectionListener(new SelectionListener() {
			/* (non-Javadoc)
			 * @see de.jwic.events.SelectionListener#objectSelected(de.jwic.events.SelectionEvent)
			 */
			public void objectSelected(SelectionEvent event) {
				onUpdate();				
			}
		});
		
		btDelete = new Button(this, "btDelete");
		btDelete.setTitle("Delete");
		btDelete.setConfirmMsg("Do you really want to delete this dimension?");
		btDelete.addSelectionListener(new SelectionListener() {
			/* (non-Javadoc)
			 * @see de.jwic.events.SelectionListener#objectSelected(de.jwic.events.SelectionEvent)
			 */
			public void objectSelected(SelectionEvent event) {
				onDelete();
			}
		});
		
		btMoveUp = new Button(this, "btMoveUp");
		btMoveUp.setTitle("Move Up");
		btMoveUp.addSelectionListener(new SelectionListener() {
			/* (non-Javadoc)
			 * @see de.jwic.events.SelectionListener#objectSelected(de.jwic.events.SelectionEvent)
			 */
			public void objectSelected(SelectionEvent event) {
				onMoveUp();
			} 
		});
		
		btMoveDown = new Button(this, "btMoveDown");
		btMoveDown.setTitle("Move Down");
		btMoveDown.addSelectionListener(new SelectionListener() {
			/* (non-Javadoc)
			 * @see de.jwic.events.SelectionListener#objectSelected(de.jwic.events.SelectionEvent)
			 */
			public void objectSelected(SelectionEvent event) {
				onMoveDown();				
			}
		});
		
	}

	protected void onMassInput() {
		
		String text = inpMassInput.getText();
		if (text.isEmpty()) {
			errInfo.showError("No data in the text field...");
		}
		
		BufferedReader reader = new BufferedReader(new StringReader(text));

		try {
			int count = 0;
			int existed = 0;
			String line;
			while ((line = reader.readLine()) != null) {
				String key;
				String title = null;
				int idx = line.indexOf('|'); 
				if (idx != -1) {
					key = line.substring(0, idx).trim();
					title = line.substring(idx + 1).trim();
				} else {
					key = line.trim();
				}
				
				if (key.indexOf('/') != -1) {
					if (title == null) {
						title = key;
					}
					key = key.replace('/', '_');
				}
				
				if (!key.isEmpty()) {
					if (!editedElement.containsDimensionElement(key)) {
						IDimensionElement elm ;
						if (insertChild) {
							elm = editedElement.createDimensionElement(key);
						} else {
							elm = editedElement.getParent().createDimensionElement(key);
						}
						if (title != null) {
							elm.setTitle(title.length() == 0 ? null : title);
						}
						count++;
					} else {
						existed++;
					}
				}
			}
			errInfo.showWarning("Created " + count + " elements. (" + existed + " elements did already exist.)");
			tableModel.clearSelection();
			table.setRequireRedraw(true);

		} catch (Exception e) {
			errInfo.showError(e);
		}

		
	}

	/**
	 * 
	 */
	protected void onMoveDown() {
		
		editedElement.getParent().reindex(editedElement, editedElement.getIndex() + 1);
		table.setRequireRedraw(true);
		setEditorEnabled(true); // refresh buttons
		
	}

	/**
	 * 
	 */
	protected void onMoveUp() {
		
		if (editedElement.getIndex() > 0) {
			editedElement.getParent().reindex(editedElement, editedElement.getIndex() - 1);
			table.setRequireRedraw(true);
			setEditorEnabled(true); // refresh buttons
		}
		
	}

	/**
	 * 
	 */
	protected void onDelete() {
		
		if (!insertMode) {
			editedElement.remove();
			tableModel.clearSelection();
			table.setRequireRedraw(true);
		}
		
	}

	/**
	 * 
	 */
	protected void onUpdate() {
		
		if (!insertMode) {
			String title = inpTitle.getText().trim();
			editedElement.setTitle(title.length() == 0 ? null : title);
			editedElement.setWeight(Double.parseDouble(inpWeight.getText()));
			updateEditor(null);
		} else {
			String key = inpKey.getText().trim();
			String title = inpTitle.getText().trim();
			try {
				IDimensionElement elm ;
				if (insertChild) {
					elm = editedElement.createDimensionElement(key);
				} else {
					elm = editedElement.getParent().createDimensionElement(key);
				}
				elm.setTitle(title.length() == 0 ? null : title);
				elm.setWeight(Double.parseDouble(inpWeight.getText()));
				
				inpKey.setText("");
				inpKey.forceFocus();
				inpTitle.setText("");
				inpWeight.setText("1.0");
		
			} catch (Exception e) {
				errInfo.showError(e);
			}
			
		}
		tableModel.clearSelection();
		table.setRequireRedraw(true);
	}

	/**
	 * @param object
	 */
	private void updateEditor(IDimensionElement element) {
		
		insertMode = false;
		editedElement = element;
		
		if (element != null) {
			IDimensionElement parent = element.getParent();
			if(parent == null){
				lblParent.setText("");
			}else{
				lblParent.setText(parent.getPath());
			}
			inpKey.setText(element.getKey());
			inpTitle.setText(element.getTitle() != null ? element.getTitle() : "");
			inpWeight.setText(Double.toString(element.getWeight()));
			setEditorEnabled(true);
			inpKey.setEnabled(false);
		} else {
			lblParent.setText("");
			inpKey.setText("");
			inpTitle.setText("");
			inpWeight.setText("");
			setEditorEnabled(false);
		}
		

	}

	/**
	 * 
	 */
	private void setupTable() {
		
		table = new TableViewer(this, "table");
		
		table.setContentProvider(new DimensionContentProvider(dimension));
		table.setTableLabelProvider(new DimensionEditorLabelProvider());
		table.setWidth(949);
		table.setHeight(300);
		table.setResizeableColumns(true);
		table.setScrollable(true);
		table.setShowStatusBar(false);
		table.setExpandableColumn(0);
		
		tableModel = table.getModel();
		tableModel.setSelectionMode(TableModel.SELECTION_SINGLE);
		tableModel.addColumn(new TableColumn("Key", 300, "key"));
		tableModel.addColumn(new TableColumn("Title", 240, "title"));
		tableModel.addColumn(new TableColumn("Weight", 150, "weight"));
		tableModel.addElementSelectedListener(new ElementSelectedListener() {
			public void elementSelected(ElementSelectedEvent event) {
				if(tableModel.getSelection().size()>0)
					onSelection((String)event.getElement());
			
			}
		});

		
	}

	/**
	 * 
	 */
	protected void onSelection(String path) {
		log.info("Selected: " + path);
		
		IDimensionElement element = dimension.parsePath(path);
		updateEditor(element);
		btUpdate.setTitle("Update");
	}

	/**
	 * 
	 */
	private void createActionBar() {
		
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
		
		Button btAdd = group.addButton();
		btAdd.setIconEnabled(ImageLibrary.IMAGE_ADD);
		btAdd.setTitle("Add Element");
		btAdd.addSelectionListener(new SelectionListener() {
			public void objectSelected(SelectionEvent event) {
				onAddElement();
			}
		});

		Button btAddChild = group.addButton();
		btAddChild.setIconEnabled(ImageLibrary.IMAGE_ADD);	
		btAddChild.setTitle("Add Child Element");
		btAddChild.addSelectionListener(new SelectionListener() {
			public void objectSelected(SelectionEvent event) {
				onAddChildElement();
			}
		});

		Button btSort = group.addButton();
		btSort.setIconEnabled(ImageLibrary.IMAGE_REFRESH);	
		btSort.setTitle("Sort Elements");
		btSort.addSelectionListener(new SelectionListener() {
			public void objectSelected(SelectionEvent event) {
				onSortElements();
			}
		});

		Button btDeleteAll = group.addButton();
		btDeleteAll.setIconEnabled(ImageLibrary.IMAGE_TABLE_DELETE);	
		btDeleteAll.setTitle("Delete All Elements");
		btDeleteAll.addSelectionListener(new SelectionListener() {
			public void objectSelected(SelectionEvent event) {
				onDeleteAllElements();
			}
		});
		btDeleteAll.setConfirmMsg("Do you really want to delete all elements in that dimension?");

		btSeal = group.addButton();
		btSeal.setIconEnabled(dimension.isSealed() ? ImageLibrary.IMAGE_LOCK : ImageLibrary.IMAGE_LOCK_OPEN);
		btSeal.setTitle(dimension.isSealed() ? "Unlock Sealed Dimension" : "Seal Dimension");
		btSeal.addSelectionListener(new SelectionListener() {
			/* (non-Javadoc)
			 * @see de.jwic.events.SelectionListener#objectSelected(de.jwic.events.SelectionEvent)
			 */
			@Override
			public void objectSelected(SelectionEvent event) {
				onSeal();
			}
		});
	}

	/**
	 * 
	 */
	protected void onSeal() {
		
		dimension.setSealed(!dimension.isSealed());
		btSeal.setIconEnabled(dimension.isSealed() ? ImageLibrary.IMAGE_LOCK : ImageLibrary.IMAGE_LOCK_OPEN);
		btSeal.setTitle(dimension.isSealed() ? "Unlock Sealed Dimension" : "Seal Dimension");
		
	}

	/**
	 * 
	 */
	protected void onSortElements() {
		
		if (editedElement != null) {
			editedElement.sortDimensionElements();
		} else {
			dimension.sortDimensionElements();
		}
		table.setRequireRedraw(true);
		
		
	}

	/**
	 * 
	 */
	protected void onDeleteAllElements() {
	
		dimension.removeDimensionElements();
		table.setRequireRedraw(true);
		
	}

	/**
	 * 
	 */
	protected void onAddChildElement() {

		if (editedElement == null) {
			editedElement = dimension;
		}
		
		insertMode = true;
		insertChild = true;
		
		lblParent.setText(editedElement.getPath());
		inpKey.setText("");
		inpKey.forceFocus();
		inpTitle.setText("");
		inpWeight.setText("1.0");
		setEditorEnabled(true);
		btDelete.setEnabled(false);
		btUpdate.setTitle("Insert");
		
	}

	/**
	 * 
	 */
	protected void onAddElement() {
		
		if (editedElement == null) {
			onAddChildElement();
			return;
		}
		
		insertMode = true;
		insertChild = false;
		
		lblParent.setText(editedElement.getParent().getPath());
		inpKey.setText("");
		inpKey.forceFocus();
		inpTitle.setText("");
		inpWeight.setText("1.0");
		
		setEditorEnabled(true);
		btDelete.setEnabled(false);
		btUpdate.setTitle("Insert");

		
	}

	/**
	 * 
	 */
	protected void close() {
		destroy();
		
	}

	/**
	 * @return the editedElement
	 */
	public IDimensionElement getEditedElement() {
		return editedElement;
	}
}
