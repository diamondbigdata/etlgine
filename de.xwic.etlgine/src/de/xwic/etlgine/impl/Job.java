/**
 * 
 */
package de.xwic.etlgine.impl;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.File;
import java.util.Date;

import de.xwic.etlgine.ETLException;
import de.xwic.etlgine.ETLgine;
import de.xwic.etlgine.IContext;
import de.xwic.etlgine.IJob;
import de.xwic.etlgine.IProcessChain;
import de.xwic.etlgine.ITrigger;

/**
 * @author Developer
 *
 */
public class Job implements IJob {

	private IProcessChain processChain = null;
	private ITrigger trigger = null;
	private Date lastRun = null;
	private String name = null;
	private boolean executing = false;
	private String chainScriptName = null; 
	
	/**
	 * @param name
	 */
	public Job(String name) {
		super();
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see de.xwic.etlgine.IJob#execute()
	 */
	public synchronized void execute(IContext context) throws ETLException {
		
		if (executing) {
			throw new ETLException("The job is already beeing executed.");
		}
		executing = true;
		try {
			if (processChain == null) {
				if (chainScriptName == null) {
					throw new ETLException("No processChain or chainScriptName given.");
				}
				loadChainFromScript(context);
			}
			processChain.start();
		} finally {
			executing = false;
			lastRun = new Date();
			processChain = null;
		}
	}

	/**
	 * @throws ETLException 
	 * 
	 */
	private void loadChainFromScript(IContext context) throws ETLException {
		
		processChain = ETLgine.createProcessChain(context, chainScriptName);
		
		Binding binding = new Binding();
		binding.setVariable("job", this);
		binding.setVariable("processChain", processChain);
		
		GroovyShell shell = new GroovyShell(binding);
		
		File jobPath = new File(context.getProperty(IContext.PROPERTY_SCRIPTPATH, "."));
		if (!jobPath.exists()) {
			throw new ETLException("The job path " + jobPath.getAbsolutePath() + " does not exist.");
		}
		File file = new File(jobPath, chainScriptName);
		if (!file.exists()) {
			throw new ETLException("The script file " + file.getAbsolutePath() + " does not exist.");
		}
		
		try {
			shell.evaluate(file);
		} catch (Exception e) {
			throw new ETLException("Error evaluating script '" + file.getName() + "':" + e, e);
		}
		
	}

	/* (non-Javadoc)
	 * @see de.xwic.etlgine.IJob#isExecuting()
	 */
	public boolean isExecuting() {
		return executing;
	}
	
	/**
	 * @return the processChain
	 */
	public IProcessChain getProcessChain() {
		return processChain;
	}

	/**
	 * @param processChain the processChain to set
	 */
	public void setProcessChain(IProcessChain processChain) {
		this.processChain = processChain;
	}

	/**
	 * @return the trigger
	 */
	
	public ITrigger getTrigger() {
		return trigger;
	}
	/**
	 * @param trigger the trigger to set
	 */
	public void setTrigger(ITrigger trigger) {
		this.trigger = trigger;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the lastRun
	 */
	public Date getLastRun() {
		return lastRun;
	}

	/**
	 * @return the chainScriptName
	 */
	public String getChainScriptName() {
		return chainScriptName;
	}

	/**
	 * @param chainScriptName the chainScriptName to set
	 */
	public void setChainScriptName(String chainScriptName) {
		this.chainScriptName = chainScriptName;
	}
	
}