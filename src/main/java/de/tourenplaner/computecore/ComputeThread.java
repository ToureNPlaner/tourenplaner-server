/*
 * Copyright 2012 ToureNPlaner
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.tourenplaner.computecore;

import de.tourenplaner.algorithms.Algorithm;
import de.tourenplaner.algorithms.ComputeException;
import de.tourenplaner.computeserver.ErrorMessage;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A ComputeThread computes the results of ComputeRequests it gets from the
 * queue of it's associated ComputeCore using Algorithms known to it's
 * AlgorithmManager
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class ComputeThread extends Thread {

    private static Logger log = Logger.getLogger("de.tourenplaner.computecore");

    private final AlgorithmManager alm;
    private final BlockingQueue<ComputeRequest> reqQueue;

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
        this.setDaemon(true);
    }

    /**
     * Runs computations taking new ones from the Queue
     */
    @Override
    public void run() {
        ComputeRequest work;
        Algorithm alg;

        while (!Thread.interrupted()) {

            try {
                work = reqQueue.take();

                // check needed if availability of algorithms changes
                alg = alm.getAlgByURLSuffix(work.getRequestData().getAlgorithmURLSuffix());
                if (alg != null) {
                    try {

                        alg.compute(work);
                        log.finer("Algorithm " + work.getRequestData().getAlgorithmURLSuffix() + " successfully computed.");

                        // IOException will be handled as EINTERNAL
                        work.getResponder().writeComputeResult(work, HttpResponseStatus.OK);

                    } catch (ComputeException e) {
                        log.log(Level.WARNING, "There was a ComputeException", e);
                        String errorMessage = work.getResponder().writeAndReturnErrorMessage(ErrorMessage.ECOMPUTE, e.getMessage());
                    } catch (Exception e) {
                        log.log(Level.WARNING, "Internal server exception (caused by algorithm or result writing)", e);
                        // Don't give too much info to client as we probably got a programming mistake
                        work.getResponder().writeErrorMessage(ErrorMessage.EINTERNAL_UNSPECIFIED);
                    }
                } else {
                    log.warning("Unsupported algorithm " + work.getRequestData().getAlgorithmURLSuffix() + " requested");
                    work.getResponder().writeErrorMessage(ErrorMessage.EUNKNOWNALG);
                }

            } catch (InterruptedException e) {
                log.warning("ComputeThread interrupted");
                return;
            } catch (Exception e) {
                log.log(Level.WARNING, "An exception occurred, keep on going", e);
            }
        }
    }
}
