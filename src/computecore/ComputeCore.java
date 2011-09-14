/**
 * $$\\ToureNPlaner\\$$
 */
package computecore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * The ComputeCore keeps a thread pool of ComputeThreads and allows new
 * computations to be scheduled for execution by adding them to it's queue
 * 
 * @author Niklas Schnelle, Peter Vollmer
 *
 */
public class ComputeCore {
	
	private List<Thread> threads;
	private BlockingQueue<ComputeRequest> reqQueue;
	private AlgorithmRegistry registry;
	
	
	/**
	 * Constructs a new ComputeCore which uses numThreads threads in it's pool
	 * and has a waiting queue of length queueLength
	 * @param algRegistry
	 * @param numThreads
	 * @param queueLength
	 */
	public ComputeCore(AlgorithmRegistry algRegistry , int numThreads, int queueLength){
		threads = new ArrayList<Thread>(numThreads);
		reqQueue = new LinkedBlockingQueue<ComputeRequest>(queueLength);
		registry = algRegistry;
		
		ComputeThread curr;
		
		for(int i=0; i < numThreads; i++){
			curr = new ComputeThread(registry.getAlgorithmManager(), reqQueue);
			curr.start();
			threads.add(curr);
		}
		
	}
	
	
	
	/**
	 * Submits a request for computation, returns true if there is
	 * still space in the queue false otherwise
	 * 
	 * @param rq
	 * @return
	 */
	public boolean submit(ComputeRequest rq){
		return reqQueue.offer(rq);
	}
	
	/**
	 * Gets the AlgorithmRegistry used by this ComputeCore to create AlgorithmManagers for it's
	 * ComputeThreads 
	 * 
	 * @return the AlgorithmRegistry
	 */
	public AlgorithmRegistry getAlgorithmRegistry(){
		return registry;
	}
	
}
