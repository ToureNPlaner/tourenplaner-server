/**
 * ToureNPlaner
 * 
 * Knapsack Test Algorithm
 * 
 */
package algorithms;

import java.util.ArrayList;
import java.util.List;

import computecore.ComputeRequest;
import computecore.ComputeResult;

/**
 * @author Niklas Schnelle
 *
 */
public class Knapsack implements Algorithm {
	
	private int[] values=null;
	private int[] weights=null;
	private int numElements;
	private ComputeResult res=null;
	
	private long rucksackSize;
	private int maxValue = 0;
	private ComputeRequest request;
	
	private static final Smallsack inftySack = new Knapsack.Smallsack();
	
	private static class Smallsack{
		Smallsack(){
			weight=Integer.MAX_VALUE;
			infty=true;
			from_up=false;
		}
		Smallsack(int nw){
			weight =nw;
			infty=false;
			from_up=false;
			}
		Smallsack(int plusweight, Smallsack subsack){
			if(!subsack.infty){
				weight=plusweight+subsack.weight;
				infty=false;
				from_up=true;
			} else {
				weight=Integer.MAX_VALUE;
				infty=true;
				from_up=false;
			}
		}

		public static Smallsack minSack(Smallsack left, Smallsack right){
			if(left.infty){
				return right;
			} else if(right.infty){
				return left;
			} else if(left.weight > right.weight){
				return right;
			} else {
				return left;
			}		
		}
		public int weight = Integer.MAX_VALUE;
		public boolean infty = true;
		public boolean from_up = false;
	}
	
	/*
	 * {
	 * }
	 */
	
	@Override
	public void run() {

		//+1 to account for the zero row
		Smallsack[][] dyn = new Smallsack[numElements+1][maxValue+1];
				
		//fill the first row with zeroes
		Smallsack zeroSack = new Smallsack(0);
		for (int index=0; index < dyn.length; index++){
			dyn[index][0] = zeroSack; 
		}
		
		//fill the first column with infinity (not the first element)
		for (int index=1; index < dyn[0].length; index++){
			dyn[0][index] = inftySack; 
		}
		
		//Fill the Sackmatrix
		Smallsack temp1Sack = null;
		Smallsack temp2Sack = null;
		for(int vals=1; vals < dyn[0].length; vals++){
			for(int nums=1; nums < dyn.length; nums++){
				temp1Sack = dyn[nums-1][vals];
				//Make sure this came from up we'll get a new object from left
				if(temp1Sack.from_up){
					temp1Sack = new Smallsack(temp1Sack.weight);
				}
				temp2Sack = ((vals-values[nums-1]) >= 0) ? 
						new Smallsack(weights[nums-1], dyn[nums-1][vals-values[nums-1]]):
							inftySack;
				
				temp1Sack=Smallsack.minSack(temp1Sack, temp2Sack);
				
				dyn[nums][vals]=temp1Sack;
				
			}
		}
		
		//printMat(dyn);
		//Do the backtracking
		//First find the right-downmost element that is valid
		Smallsack sack = null;
		int vals = dyn[0].length;
		while(sack == null || sack.infty == true || sack.weight > rucksackSize){
			vals--;
			sack = dyn[dyn.length-1][vals];
		}
		
		res = request.getResultObject();
		res.put("BestWeight", new Integer(sack.weight));
		res.put("BestValue", new Integer(vals));
		
		// Now save what to pack as array of 
		ArrayList<Integer> items = new ArrayList<Integer>();
		
		int nums = dyn.length-1;
		while(nums>0){
			if(sack.from_up){
				//System.out.println("("+sack.weight+") PACK: "+(nums)+
				//		" with (weight, value): ("+weights[nums-1]+", "+values[nums-1]+")");
				items.add(nums);
				vals=vals-values[nums-1];
				nums--;
				sack = dyn[nums][vals];
			} else {
				nums--;
				sack = dyn[nums][vals];                   
			}
		}
		
		res.put("Items", items);
		
	}

	@Override
	public String getName() {
		return "Knapsack";
	}

	@Override
	public String getShorthand() {
		return "ks";
	}

	@Override
	public void setComputeRequest(ComputeRequest req) {
		request = req;
		rucksackSize = ((java.lang.Number)req.get("KnapsackSize")).longValue();
		List<List<java.lang.Number>> request = ((List<List<java.lang.Number>>) req.get("ValuesAndWeights"));
		values = new int[request.size()];
		weights = new int[request.size()];
		
		int index = 0;
		for(List<java.lang.Number> numList: request){
			
			values[index] = numList.get(0).intValue();
			maxValue += values[index];
			weights[index] = numList.get(1).intValue();
			index++;
		}
		
		numElements = index;
	}



	@Override
	public ComputeResult getComputeResult() {
		return res;
	}




}
