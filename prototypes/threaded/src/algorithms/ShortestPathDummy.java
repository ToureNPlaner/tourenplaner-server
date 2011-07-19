/**
 * $$\\ToureNPlaner\\$$
 */

package algorithms;

import java.util.ArrayList;
import java.util.List;

import computecore.ComputeRequest;
import computecore.ComputeResult;

/**
 * @author Niklas Schnelle, Peter Vollmer
 *
 */
public class ShortestPathDummy implements Algorithm {

	private ComputeRequest req;
	private ComputeResult res;
	private ArrayList<List<Float>> route = new ArrayList<List<Float>>(5);
	private int dummyCounter;
	
	public ShortestPathDummy() {
		route = new ArrayList<List<Float>>(5);
		
		List<Float> temp;
		route = new ArrayList<List<Float>>(5);
		
		temp = new ArrayList<Float>(2);
		temp.add(0, 3.1427F);
		temp.add(1, 90.1487F);
		route.add(temp);
		
		temp = new ArrayList<Float>(2);
		temp.add(0, 28.1427F);
		temp.add(1, 20.1567F);
		route.add(temp);
		
		temp = new ArrayList<Float>(2);
		temp.add(0, 17.1978F);
		temp.add(1, 86.1487F);
		route.add(temp);
		
		temp = new ArrayList<Float>(2);
		temp.add(0, 42.5927F);
		temp.add(1, 129.4667F);
		route.add(temp);
		
		temp = new ArrayList<Float>(2);
		temp.add(0, 89.1427F);
		temp.add(1, 1.0847F);
		route.add(temp);
		
	}
	
	@Override
	public void run() {
		/*for(int i=0; i < Integer.MAX_VALUE; i++){
			dummyCounter++;
		}*/
	
		res = req.getResultObject();
		res.put("Route", route);
	}

	@Override
	public String getName() {
		return "ShortestPathDummy";
	}

	@Override
	public String getShorthand() {
		return "sp";
	}

	@Override
	public void setComputeRequest(ComputeRequest req) {
		this.req =	req;
	}

	@Override
	public ComputeResult getComputeResult() {		
		return res;
	}

}
