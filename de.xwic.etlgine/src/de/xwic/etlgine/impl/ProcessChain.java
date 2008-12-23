/**
 * 
 */
package de.xwic.etlgine.impl;

import java.util.ArrayList;
import java.util.List;

import de.xwic.etlgine.ETLException;
import de.xwic.etlgine.IGlobalContext;
import de.xwic.etlgine.IMonitor;
import de.xwic.etlgine.IProcess;
import de.xwic.etlgine.IProcessChain;

/**
 * A chain of processes.
 * @author Florian Lippisch
 */
public class ProcessChain implements IProcessChain {

	private final String name;
	private IMonitor monitor;
	private IGlobalContext globalContext = new GlobalContext();

	private List<IProcess> processList = new ArrayList<IProcess>();
	
	public ProcessChain(String name) {
		this.name = name;
		
	}
	
	/* (non-Javadoc)
	 * @see de.xwic.etlgine.IProcessChain#addProcess(de.xwic.etlgine.IProcess)
	 */
	public IProcess createProcess(String name) {
		IProcess process = new Process(name);
		processList.add(process);
		return process;
	}

	/* (non-Javadoc)
	 * @see de.xwic.etlgine.IProcessChain#getGlobalContext()
	 */
	public IGlobalContext getGlobalContext() {
		return globalContext;
	}

	/* (non-Javadoc)
	 * @see de.xwic.etlgine.IProcessChain#getMonitor()
	 */
	public IMonitor getMonitor() {
		return monitor;
	}

	/* (non-Javadoc)
	 * @see de.xwic.etlgine.IProcessChain#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see de.xwic.etlgine.IProcessChain#setMonitor(de.xwic.etlgine.IMonitor)
	 */
	public void setMonitor(IMonitor monitor) {
		this.monitor = monitor;
	}

	/* (non-Javadoc)
	 * @see de.xwic.etlgine.IProcessChain#start()
	 */
	public void start() throws ETLException {
		
		for (IProcess process : processList) {
			process.start();
		}

	}

}
