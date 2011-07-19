/**
 * $$\\ToureNPlaner\\$$
 */
package algorithms;

import java.util.ArrayList;
import java.util.List;

import computecore.ComputeRequest;
import computecore.ComputeResult;

/**
 * @author Peter Vollmer
 * 
 */
public class Bubblesort implements Algorithm {

	private ComputeResult res = null;

	private List<Long> sortArray;
	private ComputeRequest request;

	@Override
	public void run() {

		boolean unsorted = true;
		long temp;
		while (unsorted) {
			unsorted = false;
			for (int i = 0; i < sortArray.size() - 1; i++)
				if (sortArray.get(i) > sortArray.get(i + 1)) {
					temp = sortArray.get(i);
					sortArray.set(i, sortArray.get(i + 1));
					sortArray.set(i + 1, temp);
					unsorted = true;
				}
		}
		res = request.getResultObject();
		res.put("SortedArray", sortArray);

	}

	@Override
	public String getName() {
		return "Bubblesort";
	}

	@Override
	public String getShorthand() {
		return "bsort";
	}

	@Override
	public void setComputeRequest(ComputeRequest req) {
		request = req;
		List<Long> request = ((List<Long>) req.get("Numbers"));
		sortArray = new ArrayList<Long>();
		for (Long numList : request) {
			sortArray.add(numList);
		}
	}

	@Override
	public ComputeResult getComputeResult() {
		return res;
	}

}
