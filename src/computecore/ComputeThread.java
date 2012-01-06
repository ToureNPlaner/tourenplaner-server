/**
 * $$\\ToureNPlaner\\$$
 */
package computecore;

import algorithms.Algorithm;
import algorithms.ComputeException;
import config.ConfigManager;
import database.DatabaseManager;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

/**
 * A ComputeThread computes the results of ComputeRequests it gets from the
 * queue of it's associated ComputeCore using Algorithms known to it's
 * AlgorithmManager
 * 
 * @author Niklas Schnelle, Peter Vollmer, Sascha Meusel
 * 
 */
public class ComputeThread extends Thread {
    
    private static Logger log = Logger.getLogger("tourenplaner");

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
	public ComputeThread(AlgorithmManager am, BlockingQueue<ComputeRequest> rq) {
		alm = am;
		reqQueue = rq;
		ConfigManager cm = ConfigManager.getInstance();
		isPrivate = cm.getEntryBool("private", false);
		tmxb = ManagementFactory.getThreadMXBean();
		if (isPrivate) {
            try {
                this.dbm = new DatabaseManager(cm.getEntryString("dburi",
                        "jdbc:mysql://localhost:3306/"), cm.getEntryString(
                        "dbname", "tourenplaner"), cm.getEntryString("dbuser",
                        "tnpuser"), cm.getEntryString("dbpw",
                        "toureNPlaner"));
            } catch(SQLException e){
                log.severe("Couldn't establish database connection");
                System.exit(1);
            }

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
                isPrivate = work.isPrivate();
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
								cpuTime = tmxb.getCurrentThreadCpuTime() - cpuTime;
							} else {
								cpuTime = System.nanoTime() - cpuTime;
							}
							// convert to milliseconds
							cpuTime = cpuTime / 1000000;
							
						} else {
							alg.compute(work);
						}
						log.fine("Algorithm "+ work.getAlgorithmURLSuffix()
								+ " successfully computed.");
						
						try {
							baOutputStream = work.getResponder().writeComputeResult(work,
									HttpResponseStatus.OK);
							log.finest("Algorithm "+ work.getAlgorithmURLSuffix()
									+ " compute result successfully written into response.");
						} catch (IOException e) {
                            log.warning("There was an IOException: "
                                    + e.getMessage());
                            // TODO define error and write to protocol specification
                            String errorMessage = work.getResponder().writeAndReturnErrorMessage("ECOMPUTE",
                                    "The server could not send or store the compute result", "",
                                    HttpResponseStatus.INTERNAL_SERVER_ERROR);
							if (isPrivate) {
								try {
									// TODO change failDescription to user friendly message?
									dbm.updateRequestWithComputeResult(
											requestID,
                                            // TODO maybe a better method should be used to convert a string to a byte array
                                            errorMessage.getBytes(), //jsonResponse
											false, //isPending
											0, //costs
											cpuTime, 
											true, //hasFailed
											null); //failDescription
									log.finest("ComputeThread: Algorithm "+ work.getAlgorithmURLSuffix()
											+ " IOException successful written into database.");
								} catch (SQLException sqlE) {
									log.warning("Could not log IOException into DB "
											+ sqlE.getMessage());
								}
								
							}
							throw e;
						}
						if (isPrivate) {
							
							// TODO get algorithm specific costs
							try {
								log.fine("Algorithm "+ work.getAlgorithmURLSuffix()
										+ ": trying to write compute result into database, length of ByteArrayStream: " 
										+ baOutputStream.toByteArray().length);
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
								log.finest("ComputeThread: Algorithm "+ work.getAlgorithmURLSuffix()
										+ " compute result successful written into database.");
							} catch (SQLException sqlE) {
								log.warning("ComputeThread: Could not log ComputeResult into DB: "
										+ sqlE.getMessage());
							} 
						}
					} catch (ComputeException e) {
						log.warning("There was a ComputeException: "
								+ e.getMessage());
                        String errorMessage = work.getResponder().writeAndReturnErrorMessage("ECOMPUTE",
								e.getMessage(), "",
								HttpResponseStatus.PROCESSING); //TODO maybe wrong response status
						if (isPrivate) {
							try {
								// TODO change failDescription to user friendly message?
								dbm.updateRequestWithComputeResult(
										requestID,
                                        // TODO maybe a better method should be used to convert a string to a byte array
                                        errorMessage.getBytes(), //jsonResponse
										false, //isPending
										0, //costs
										cpuTime,
										true, //hasFailed
										null); //failDescription
							} catch (SQLException sqlE) {
								log.warning("ComputeThread: Could not log ComputeException into DB : "
										+ sqlE.getMessage());
							}
							
						}
					}
				} else {
					log.warning("Unsupported algorithm "
							+ work.getAlgorithmURLSuffix() + " requested");
					String errorMessage = work.getResponder().writeAndReturnErrorMessage("EUNKNOWNALG",
							"An unknown algorithm was requested", null,
							HttpResponseStatus.NOT_FOUND);
                    if (isPrivate) {
                        try {
                            // TODO change failDescription to user friendly message?
                            dbm.updateRequestWithComputeResult(
                                    requestID,
                                    // TODO maybe a better method should be used to convert a string to a byte array
                                    errorMessage.getBytes(), //jsonResponse
                                    false, //isPending
                                    0, //costs
                                    0, //cpuTime
                                    true, //hasFailed
                                    null); //failDescription
                        } catch (SQLException sqlE) {
                            log.warning("ComputeThread: Could not log EUNKNOWNALG into DB : "
                                    + sqlE.getMessage());
                        }

                    }
				}

			} catch (InterruptedException e) {
				log.warning("ComputeThread interrupted");
				return;
			} catch (Exception e) {
				log.warning("Exception in ComputeThread: "
						+ e.getMessage());
                // TODO: Remove DEBUG Only Code
				if (baOutputStream != null) {
					System.err.println("Exception in ComputeThread - debug information: Size of the ByteArrayStream: " 
							+ baOutputStream.size());
					if (baOutputStream.toString().length() > 200) {
						System.err.println("Exception in ComputeThread - debug information: Content of the ByteArrayStream: " 
								+ baOutputStream.toString().substring(0, 100) + "\n ### SKIP BYTES ### \n"
								+ baOutputStream.toString().substring(baOutputStream.toString().length() - 100));
					} else {
						System.err.println("ComputeThread - debug information: Content of the ByteArrayStream: " 
								+ baOutputStream.toString());
					}
				} else {
					System.err.println("ComputeThread: ByteArrayStream is null");
				}
				
				e.printStackTrace();
			}
		}
	}
}
