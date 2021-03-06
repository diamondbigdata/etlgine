/**
 * 
 */
package de.xwic.etlgine.server;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.xwic.etlgine.IJob;
import de.xwic.etlgine.server.ServerContext.EventType;

/**
 * 
 * @author lippisch
 */
public class JobQueue implements Runnable {
	
	public static final String QUEUE_THREAD_PREFIX = "jobQueue-";
	private static final int SLEEP_TIME = 2 * 1000;
	
	private static final Log log = LogFactory.getLog(JobQueue.class);
	
	private Queue<IJob> queue = new ConcurrentLinkedQueue<IJob>();

	private final String name;
	private ThreadGroup threadGroup;
	private Thread myThread;
	private boolean exitFlag = false;
	private IJob activeJob = null;
	private final ServerContext context; 
	
	/**
	 * Constructor.
	 * @param name
	 */
	public JobQueue(ServerContext context, String name) {
		this.context = context;
		this.name = name;
		
		threadGroup = new ThreadGroup(QUEUE_THREAD_PREFIX + name);
		myThread = new Thread(threadGroup, this, QUEUE_THREAD_PREFIX + name);
		myThread.start();
	}
	
	/**
	 * Add a job to the queue. If the queue is empty, the job is immidiately processed.
	 * @param job
	 */
	public void addJob(IJob job) {
		if (!isJobEnqueued(job)) {
			job.notifyEnqueued();
			log.debug("Adding job " + job.getName() + " to queue " + name);
			queue.add(job);
			//FLI: Do not interrupt the thread - some operations might stop immediately if the
			//thread is interrupted. (i.e. FileChannel operations)
			//myThread.interrupt();
		} else {
			throw new IllegalStateException("The specified job (" + job.getName() + ") is already queued for processing.");
		}
	}
	
	/**
	 * Checks if job is enqueued.
	 * @param job
	 * @return
	 */
	public boolean isJobEnqueued(IJob job) {
		return queue.contains(job);
	}

	/**
	 * Returns true if the queue is empty.
	 * @return
	 */
	public boolean isEmpty() {
		return queue.isEmpty();
	}

	/**
	 * Exits the queue. If a job is currently being processed, the
	 * queue is terminated after the job has finished.
	 */
	public void stopQueue() {
		log.info("Stopping queue " + name);
		exitFlag = true;
		if (activeJob == null) {
			myThread.interrupt();
		}
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
	
		while (!exitFlag) {
			
			activeJob = queue.poll();
			if (activeJob != null) {
				try {
					log.info("[Queue " + name +"]: Executing Job " + activeJob.getName());
					context.fireEvent(EventType.JOB_EXECUTION_START, new ServerContextEvent(this, activeJob));
					activeJob.execute(context);
					log.info("[Queue " + name +"]: Job " + activeJob.getName() + " finished execution.");
				} catch (Throwable t) {
					log.error("Error executing job " + activeJob.getName() + " in queue " + name, t);
				}
				context.fireEvent(EventType.JOB_EXECUTION_END, new ServerContextEvent(this, activeJob, activeJob.getState()));
				activeJob = null;
			} else {
				try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e) {
					// nothing unexpected...
				}
			}
			 
		}
		
	}

	/**
	 * @return the activeJob
	 */
	public IJob getActiveJob() {
		return activeJob;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the size of the queue.
	 * @return
	 */
	public int getSize() {
		return queue.size();
	}

	/**
	 * @return the myThread
	 */
	public Thread getThread() {
		return myThread;
	}
	
	
}
