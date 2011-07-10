/**
 * ToureNPlaner
 */
package computecore;

import java.util.HashMap;

import org.jboss.netty.channel.Channel;

/**
 * @author Niklas Schnelle
 *
 */
public class ComputeResult extends HashMap<String, Object>{
	

	protected ComputeResult(){}

	
	protected ComputeResult(int capacity){
		super(capacity);
	}

}
