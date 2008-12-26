/**
 * 
 */
package de.xwic.etlgine;

import java.io.FileNotFoundException;

/**
 * Adds one or more IProcess into a chain, executing each process
 * after the other, if no error condition is set. 
 * 
 * @author Developer
 */
public interface IProcessChain {

	/**
	 * Returns the global context.
	 * @return
	 */
	public IGlobalContext getGlobalContext();
	
	/**
	 * Returns the name of the chain.
	 * @return
	 */
	public String getName();
	
	/**
	 * Add a process. 
	 * @param process
	 */
	public IProcess createProcess(String name);
	
	/**
	 * Start the process chain.
	 * @throws ETLException
	 */
	public void start() throws ETLException;
	
	/**
	 * @return the monitor
	 */
	public IMonitor getMonitor();

	/**
	 * @param monitor the monitor to set
	 */
	public void setMonitor(IMonitor monitor);

	/**
	 * Create a new process from a script file.
	 * @param string
	 * @throws FileNotFoundException 
	 * @throws ETLException 
	 */
	public IProcess createProcessFromScript(String name, String filename) throws FileNotFoundException, ETLException;


}
