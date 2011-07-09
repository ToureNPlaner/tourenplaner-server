/**
 * ToureNPlaner
 */
package computecore;

import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.concurrent.BlockingQueue;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.util.CharsetUtil;
import org.json.simple.JSONValue;

import tourenplaner.server.prototype.event.ResultResponder;

import algorithms.Algorithm;
import algorithms.ComputeRequest;
import algorithms.ComputeResult;

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
				    	ResultResponder.writeResponse(res);
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
			}
		}
	}
}
