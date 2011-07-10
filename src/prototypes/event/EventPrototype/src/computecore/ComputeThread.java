/**
 * ToureNPlaner
 */
package computecore;


import java.util.concurrent.BlockingQueue;



import tourenplaner.server.prototype.event.ResultResponder;

import algorithms.Algorithm;

/**
 * @author Niklas Schnelle
 *
 */
public class ComputeThread extends Thread {
	
	private AlgorithmManager alm;
	private BlockingQueue<ComputeRequest> reqQueue;
	
	public ComputeThread(AlgorithmManager am, BlockingQueue<ComputeRequest>rq){
		alm = am;
		reqQueue = rq;
		this.setDaemon(true);
	}
	

	
	public void run(){
		ComputeRequest work;
		Algorithm alg;
		ComputeResult res;
		
		
		while(!Thread.interrupted()){
			try {
				work = reqQueue.take();
			    alg = alm.getAlgByShortname(work.getAlgorithmShort());
			    if(alg != null){
			    	alg.setComputeRequest(work);
			    	alg.run();
			    	res = alg.getComputeResult();
				    if(res !=  null){
				    	work.getResponder().writeResponse(res);
				    } else {
				    	System.err.println("Compute Thread couldn't process: "+work);
				    }
			    } else {
			    	System.err.println("Unsupported algorithm "+work.getAlgorithmShort()+" requested");
			    }
			    
			    
			} catch (InterruptedException e) {
				System.err.println("ComputeThread interrupted");
				return;
			} catch (Exception e){
				System.err.println("Exception in ComputeThread: "+e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
