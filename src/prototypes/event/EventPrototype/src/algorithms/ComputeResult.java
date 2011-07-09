/**
 * ToureNPlaner
 */
package algorithms;

import java.util.HashMap;

import org.jboss.netty.channel.Channel;

/**
 * @author Niklas Schnelle
 *
 */
public class ComputeResult extends HashMap<String, Object>{
	
	private Channel replyChannel;
	private boolean keepAlive;
	
	private ComputeResult(){}
	/**
	 * Creates a new ComputeResult taking basic connection data from the given
	 * ComputeRequest
	 * @param req
	 */
	public ComputeResult(ComputeRequest req){
		super();
		replyChannel = req.getReplyChannel();
		keepAlive = req.isKeepAlive();
	}
	
	public ComputeResult(int capacity){
		super(capacity);
	}

	public boolean isKeepAlive(){
		return keepAlive;
	}
	
	public Channel getReplyChannel() {
		return replyChannel;
	}
}
