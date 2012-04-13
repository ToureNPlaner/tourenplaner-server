/**
 * $$\\ToureNPlaner\\$$
 */
package de.tourenplaner.computecore;

import de.tourenplaner.algorithms.Algorithm;
import de.tourenplaner.algorithms.ComputeException;
import de.tourenplaner.config.ConfigManager;
import de.tourenplaner.database.DatabaseManager;
import de.tourenplaner.server.ErrorMessage;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.CharsetUtil;

import java.io.ByteArrayOutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A ComputeThread computes the results of ComputeRequests it gets from the
 * queue of it's associated ComputeCore using Algorithms known to it's
 * AlgorithmManager
 *
 * @author Niklas Schnelle, Peter Vollmer, Sascha Meusel
 */
public class ComputeThread extends Thread {

    private static Logger log = Logger.getLogger("de.tourenplaner.computecore");

    private final AlgorithmManager alm;
    private final BlockingQueue<ComputeRequest> reqQueue;
    private boolean isPrivate;
    private ThreadMXBean threadMXBean;
    private DatabaseManager dbm;
    private double costPerMillisecond;

    /**
     * Constructs a new ComputeThread using the given AlgorithmManager and
     * RequestQueue
     *
     * @param am AlgorithmManager
     * @param rq BlockingQueue&lt;ComputeRequest&gt;
     */
    public ComputeThread(AlgorithmManager am, BlockingQueue<ComputeRequest> rq) {
        alm = am;
        reqQueue = rq;
        ConfigManager cm = ConfigManager.getInstance();
        isPrivate = cm.getEntryBool("private", false);
        threadMXBean = ManagementFactory.getThreadMXBean();
        if (isPrivate) {
            int costPerTimeUnit = cm.getEntryInt("costpertimeunit", 10);
            if (costPerTimeUnit < 0) {
                costPerTimeUnit = 0;
            }
            // size of time unit is in milliseconds
            int timeUnitSize = cm.getEntryInt("timeunitsize", 1000);
            if (timeUnitSize <= 0) {
                timeUnitSize = 1;
            }
            costPerMillisecond = ((double) costPerTimeUnit) / ((double) timeUnitSize);

            this.dbm = new DatabaseManager();
        }
        this.setDaemon(true);
    }

    /**
     * Runs computations taking new ones from the Queue
     */
    @Override
    public void run() {
        ComputeRequest work;
        Algorithm alg;

        checkThreadMXBeanSupport();

        while (!Thread.interrupted()) {
            long cpuTime;
            int requestID = -1;
            ByteArrayOutputStream baOutputStream;

            try {
                work = reqQueue.take();

                // workIsPrivate should only be true if ComputeThread is private,
                // else there is no database connection available
                boolean workIsPrivate = this.isPrivate && work.isPrivate();

                // check needed if availability of algorithms changes
                alg = alm.getAlgByURLSuffix(work.getAlgorithmURLSuffix());
                if (alg != null) {
                    try {

                        // if server is not private, work.getRequestID() should return -1
                        requestID = work.getRequestID();

                        cpuTime = startTimeMeasurement(workIsPrivate);

                        alg.compute(work);

                        cpuTime = finishTimeMeasurement(cpuTime, workIsPrivate);

                        log.finer("Algorithm " + work.getAlgorithmURLSuffix() + " successfully computed.");

                        // IOException will be handled as EINTERNAL
                        baOutputStream = work.getResponder().writeComputeResult(work,
                                HttpResponseStatus.OK);

                        if (workIsPrivate) {

                            try {
                                int cost = (int) Math.ceil(((double) cpuTime) * costPerMillisecond);

                                // baOutputStream is not null because else writeComputeResult
                                // would throw an IOException
                                dbm.updateRequestWithComputeResult(requestID,
                                        baOutputStream.toByteArray(), cost, cpuTime);
                            } catch (SQLException sqlE) {
                                log.log(Level.WARNING, "Could not log ComputeResult into DB", sqlE);
                            }
                        }
                    } catch (ComputeException e) {
                        log.log(Level.WARNING, "There was a ComputeException", e);
                        String errorMessage = work.getResponder().writeAndReturnErrorMessage(
                                ErrorMessage.ECOMPUTE, e.getMessage());

                        writeErrorIntoDatabase(requestID, errorMessage, "ECOMPUTE", workIsPrivate);
                    } catch (Exception e) {
                        log.log(Level.WARNING, "Internal server exception (caused by algorithm or result writing)", e);
                        // Don't give too much info to client as we probably got a programming mistake
                        String errorMessage = work.getResponder().writeAndReturnErrorMessage(
                                ErrorMessage.EINTERNAL_UNSPECIFIED);

                        writeErrorIntoDatabase(requestID, errorMessage, "EINTERNAL", workIsPrivate);
                    }
                } else {
                    log.warning("Unsupported algorithm " + work.getAlgorithmURLSuffix() + " requested");
                    String errorMessage = work.getResponder().writeAndReturnErrorMessage(ErrorMessage.EUNKNOWNALG);

                    writeErrorIntoDatabase(requestID, errorMessage, "UNKNOWNALG", workIsPrivate);
                }

            } catch (InterruptedException e) {
                log.warning("ComputeThread interrupted");
                return;
            } catch (Exception e) {
                log.log(Level.WARNING, "An exception occurred, keep on going", e);
            }
        }
    }

    private void writeErrorIntoDatabase(int requestID, String errorMessage, String errorName, boolean workIsPrivate) {
        if (workIsPrivate) {
            try {
                // TODO change failDescription to user friendly message?
                dbm.updateRequestAsFailed(requestID, errorMessage.getBytes(CharsetUtil.UTF_8));
            } catch (SQLException sqlE) {
                log.log(Level.WARNING, "Could not log " + errorName + " into DB ", sqlE);
            }
        }
    }

    private long startTimeMeasurement(boolean workIsPrivate) {
        long cpuTime = 0;
        if (workIsPrivate) {
            cpuTime = threadMXBean != null ? threadMXBean.getCurrentThreadCpuTime() : System.nanoTime();
        }
        return cpuTime;
    }

    private long finishTimeMeasurement(long cpuStartTime, boolean workIsPrivate) {
        if (workIsPrivate) {
            cpuStartTime =
                    threadMXBean != null ?
                            threadMXBean.getCurrentThreadCpuTime() - cpuStartTime :
                            System.nanoTime() - cpuStartTime;
            // convert to milliseconds
            cpuStartTime = Math.round(cpuStartTime / 1000000);
            if (cpuStartTime == 0) {
                cpuStartTime = 1;
            }
        }
        return cpuStartTime;
    }

    /**
     * Checks if thread CPU time measurements works correctly. If not, this method will set this.threadMXBean = null
     */
    private void checkThreadMXBeanSupport() {
        if (threadMXBean != null) {
            try {
                if (threadMXBean.getCurrentThreadCpuTime() < 0) {
                    threadMXBean = null;
                }
            } catch (UnsupportedOperationException e) {
                threadMXBean = null;
            }
        }
    }
}
