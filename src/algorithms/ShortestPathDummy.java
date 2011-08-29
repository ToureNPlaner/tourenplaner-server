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
	
	public ShortestPathDummy() {
		route = new ArrayList<List<Float>>(5);
		
		List<Float> temp;
		route = new ArrayList<List<Float>>(5);

		temp = new ArrayList<Float>(2);
		temp.add(0, 48.781773F);
		temp.add(1, 9.175818F);
		route.add(temp);
		
		temp = new ArrayList<Float>(2);
		temp.add(0, 48.782043F);
		temp.add(1, 9.175794F);
		route.add(temp);
		
		temp = new ArrayList<Float>(2);
		temp.add(0, 48.782295F);
		temp.add(1, 9.175640F);
		route.add(temp);
		
		temp = new ArrayList<Float>(2);
		temp.add(0, 48.782352F);
		temp.add(1, 9.175596F);
		route.add(temp);
		
		temp = new ArrayList<Float>(2);
		temp.add(0, 48.783245F);
		temp.add(1, 9.174920F);
		route.add(temp);
		
		temp = new ArrayList<Float>(2);
		temp.add(0, 48.783611F);
		temp.add(1, 9.176027F);
		route.add(temp);
		
		temp = new ArrayList<Float>(2);
		temp.add(0, 48.784061F);
		temp.add(1, 9.177478F);
		route.add(temp);
		
		temp = new ArrayList<Float>(2);
		temp.add(0, 48.784279F);
		temp.add(1, 9.178200F);
		route.add(temp);
		
		temp = new ArrayList<Float>(2);
		temp.add(0, 48.784256F);
		temp.add(1, 9.178476F);
		route.add(temp);
		
		temp = new ArrayList<Float>(2);
		temp.add(0, 48.784222F);
		temp.add(1, 9.178576F);
		route.add(temp);
		
		temp = new ArrayList<Float>(2);
		temp.add(0, 48.784187F);
		temp.add(1,  9.178683F);
		route.add(temp);
		
		temp = new ArrayList<Float>(2);
		temp.add(0, 48.784172F);
		temp.add(1, 9.178791F);
		route.add(temp);
		
		temp = new ArrayList<Float>(2);
		temp.add(0, 48.784172F);
		temp.add(1, 9.178791F);
		route.add(temp);

		temp = new ArrayList<Float>(2);
		temp.add(0, 48.784168F);
		temp.add(1,  9.178823F);
		route.add(temp);

		temp = new ArrayList<Float>(2);
		temp.add(0, 48.784168F);
		temp.add(1, 9.178838F);
		route.add(temp);

		temp = new ArrayList<Float>(2);
		temp.add(0, 48.784142F);
		temp.add(1, 9.178848F);
		route.add(temp);

		temp = new ArrayList<Float>(2);
		temp.add(0, 48.784107F);
		temp.add(1, 9.178863F);
		route.add(temp);

		temp = new ArrayList<Float>(2);
		temp.add(0, 48.784061F);
		temp.add(1, 9.178906F);
		route.add(temp);
		
	}
	
	@Override
	public void run() {
		res = req.getResultObject();
		res.put("Route", route);
	}


	@Override
	public void setRequest(ComputeRequest req) {
		this.req =	req;
	}

	@Override
	public ComputeResult getResult() {		
		return res;
	}

}
