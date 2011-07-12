/**
 * ToureNPlaner
 */
package computecore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * 
 * @author Niklas Schnelle
 *
 */
public class ComputeCore {
	
	private List<Thread> threads;
	private BlockingQueue<ComputeRequest> reqQueue;
	
	
	public ComputeCore(int numThreads, int queueLength){
		threads = new ArrayList<Thread>(numThreads);
		reqQueue = new LinkedBlockingQueue<ComputeRequest>(queueLength);
		AlgorithmRegistry algRegistry = AlgorithmRegistry.getInstance();
		ComputeThread curr;
		
		for(int i=0; i < numThreads; i++){
			curr = new ComputeThread(algRegistry.createAlgorithmManager(), reqQueue);
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
	
}
