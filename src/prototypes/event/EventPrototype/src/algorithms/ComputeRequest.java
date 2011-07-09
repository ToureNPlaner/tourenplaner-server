/**
 * ToureNPlaner
 * 
 */
package algorithms;

import java.util.Map;

import org.jboss.netty.channel.Channel;

/**
 * @author Niklas Schnelle
 *
 */
public class ComputeRequest {
	
	protected final Channel replyChannel;
	protected Map<String, Object> request;
	protected boolean keepAlive;
	private String algName;
	

	public ComputeRequest(Channel replyChannel, boolean keepAlive, String algName,Map<String, Object> request){
		this.replyChannel = replyChannel;
		this.algName = algName;
		this.request = request;
		this.keepAlive = keepAlive;
	}
	
	public String getAlgorithmShort(){
		return algName;		
	}
	
	public Channel getReplyChannel(){
		return replyChannel;
	}
	
	public boolean isKeepAlive(){
		return keepAlive;
	}
	
	public Object get(String index){
		return request.get(index);
	}
	
	
}
