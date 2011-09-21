/**
 * $$\\ToureNPlaner\\$$
 */
package computecore;

import java.util.concurrent.BlockingQueue;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import algorithms.Algorithm;

/**
 * A ComputeThread computes the results of ComputeRequests it gets from the
 * queue of it's associated ComputeCore using Algorithms known to it's
 * AlgorithmManager
 * 
 * @author Niklas Schnelle, Peter Vollmer
 * 
 */
public class ComputeThread extends Thread {

	private final AlgorithmManager alm;
	private final BlockingQueue<ComputeRequest> reqQueue;

	/**
	 * Constructs a new ComputeThread using the given AlgorithmManager and
	 * RequestQueue
	 * 
	 * @param am
	 * @param rq
	 */
	public ComputeThread(AlgorithmManager am, BlockingQueue<ComputeRequest> rq) {
		alm = am;
		reqQueue = rq;
		this.setDaemon(true);
	}

	/**
	 * Runs computations taking new ones from the Queue
	 * 
	 */
	@Override
	public void run() {
		ComputeRequest work;
		Algorithm alg;
		ComputeResult res;

		while (!Thread.interrupted()) {
			try {
				work = reqQueue.take();
				alg = alm.getAlgByURLSuffix(work.getAlgorithmURLSuffix());
				if (alg != null) {
					alg.setRequest(work);
					alg.run();
					res = alg.getResult();
					if (res != null) {
						work.getResponder().writeJSON(res,
								HttpResponseStatus.OK);
					} else {
						System.err.println("Compute Thread couldn't process: "
								+ work);
					}
				} else {
					System.err.println("Unsupported algorithm "
							+ work.getAlgorithmURLSuffix() + " requested");
					work.getResponder().writeErrorMessage("EUNKNOWNALG",
							"An unknown algorithm was requested", null,
							HttpResponseStatus.NOT_FOUND);
				}

			} catch (InterruptedException e) {
				System.err.println("ComputeThread interrupted");
				return;
			} catch (Exception e) {
				System.err.println("Exception in ComputeThread: "
						+ e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
