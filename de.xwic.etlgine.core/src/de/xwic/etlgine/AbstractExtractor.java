/*
 * de.xwic.etlgine.impl.AbstractLoader 
 */
package de.xwic.etlgine;


/**
 * @author lippisch
 */
public abstract class AbstractExtractor implements IExtractor {

	protected IProcessContext context = null;

	/* (non-Javadoc)
	 * @see de.xwic.etlgine.ILoader#initialize(de.xwic.etlgine.IETLContext)
	 */
	public void initialize(IProcessContext processContext) throws ETLException {
		this.context = processContext;
	}

	/* (non-Javadoc)
	 * @see de.xwic.etlgine.IProcessParticipant#preSourceOpening(de.xwic.etlgine.IProcessContext)
	 */
	public void preSourceOpening(IProcessContext processContext)
			throws ETLException {
		
	}
	
	/* (non-Javadoc)
	 * @see de.xwic.etlgine.IETLProcessParticipant#onProcessFinished(de.xwic.etlgine.IETLContext)
	 */
	public void onProcessFinished(IProcessContext processContext) throws ETLException {
		
	}
	/* (non-Javadoc)
	 * @see de.xwic.etlgine.IETLProcessParticipant#postSourceProcessing(de.xwic.etlgine.IETLContext)
	 */
	public void postSourceProcessing(IProcessContext processContext) throws ETLException {
		
	}
	/* (non-Javadoc)
	 * @see de.xwic.etlgine.IETLProcessParticipant#preSourceProcessing(de.xwic.etlgine.IETLContext)
	 */
	public void preSourceProcessing(IProcessContext processContext) throws ETLException {
		
	}
}
