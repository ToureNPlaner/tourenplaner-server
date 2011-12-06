/**
 * $$\\ToureNPlaner\\$$
 */
package computecore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import config.ConfigManager;
import database.DatabaseManager;

import algorithms.Algorithm;
import algorithms.ComputeException;

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
	private boolean isPrivate;
	private ThreadMXBean tmxb;
	private DatabaseManager dbm = null;

	/**
	 * Constructs a new ComputeThread using the given AlgorithmManager and
	 * RequestQueue
	 * 
	 * @param am
	 * @param rq
	 * @throws SQLException Thrown only in private mode, and only if 
	 * 				database connection could not be established.
	 */
	public ComputeThread(AlgorithmManager am, BlockingQueue<ComputeRequest> rq) 
			throws SQLException {
		alm = am;
		reqQueue = rq;
		ConfigManager cm = ConfigManager.getInstance();
		isPrivate = cm.getEntryBool("private", false);
		tmxb = ManagementFactory.getThreadMXBean();
		if (isPrivate) {
			this.dbm = new DatabaseManager(cm.getEntryString("dburi",
					"jdbc:mysql://localhost:3306/"), cm.getEntryString(
					"dbname", "tourenplaner"), cm.getEntryString("dbuser",
					"tnpuser"), cm.getEntryString("dbpw",
					"toureNPlaner"));
		}
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

		while (!Thread.interrupted()) {
			long cpuTime = 0;
			boolean tmxbSupport = true;
			int requestID = -1;
			ByteArrayOutputStream baOutputStream = null;
			
			try {
				work = reqQueue.take();
				alg = alm.getAlgByURLSuffix(work.getAlgorithmURLSuffix());
				if (alg != null) {
					try {
						
						if (isPrivate) {
							requestID = work.getRequestID();
							try {
								cpuTime = tmxb.getCurrentThreadCpuTime();
							} catch (UnsupportedOperationException e) {
								cpuTime = System.nanoTime();
								tmxbSupport = false;
							}
							
							alg.compute(work);
						
							if (tmxbSupport) {
								cpuTime =- tmxb.getCurrentThreadCpuTime();
							} else {
								cpuTime =- System.nanoTime();
							}
						} else {
							alg.compute(work);
						}
						
						try {
							baOutputStream = work.getResponder().writeComputeResult(work,
									HttpResponseStatus.OK);
						} catch (IOException e) {
							if (isPrivate) {
								try {
									// TODO change failDescription to user friendly message?
									dbm.updateRequestWithComputeResult(
											requestID, 
											null, //jsonResponse
											false, //isPending
											0, //costs
											cpuTime, 
											true, //hasFailed
											"IOException: " + e.getMessage()); //failDescription
								} catch (SQLException sqlE) {
									System.err.println("Could not log IOException into DB " +
											"within ComputeThread: " + sqlE.getMessage());
									sqlE.printStackTrace();
								}
								
							}
							throw e;
						}
						if (isPrivate) {
							
							// TODO get algorithm specific costs
							try {
								// baOutputStream is not null because else
								// writeComputeResult would throw an
								// IOException
								dbm.updateRequestWithComputeResult(
										requestID, 
										baOutputStream.toByteArray(), //jsonResponse
										false, //isPending
										0, //costs
										cpuTime, 
										false, //hasFailed
										null); //failDescription
							} catch (SQLException sqlE) {
								System.err.println("Could not log ComputeResult into DB " +
										"within ComputeThread: " + sqlE.getMessage());
								sqlE.printStackTrace();
							} 
						}
					} catch (ComputeException e) {
						System.err.println("There was a ComputeException: "
								+ e.getMessage());
						work.getResponder().writeErrorMessage("ECOMPUTE",
								e.getMessage(), "",
								HttpResponseStatus.PROCESSING);
						if (isPrivate) {
							try {
								// TODO change failDescription to user friendly message?
								dbm.updateRequestWithComputeResult(
										requestID, 
										null, //jsonResponse
										false, //isPending
										0, //costs
										cpuTime, 
										true, //hasFailed
										"ComputeException: " + e.getMessage()); //failDescription
							} catch (SQLException sqlE) {
								System.err.println("Could not log ComputeException into DB " +
										"within ComputeThread: " + sqlE.getMessage());
								sqlE.printStackTrace();
							}
							
						}
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
