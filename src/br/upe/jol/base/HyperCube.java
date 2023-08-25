package br.upe.jol.base;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Erick
 * @version 1.0
 * @param <T>
 */
public class HyperCube{
	
	/**
	 * List with the index of solution into the Hypercube
	 */
	private List<Integer> indexSolutions;
	
	/**
	 * Limits of the Hypercube
	 */
	private double[] objectivesLimits;

	
	public HyperCube(double[] objectivesLimits){
		this.objectivesLimits = objectivesLimits;
		this.indexSolutions = new ArrayList<Integer>();
	}
	
	/**
	 * Dominance operator
	 * @param <code>hyperbox</code> Other hyperbox
	 * @return <code>true</code> This hyperpox dominates the other<br />
	 *  <code>false</code> The other hyperbox dominates this
	 */
	public boolean dominates(HyperCube hypercube){
		/*boolean condition1 = true;
		
		for(int i=0; i<this.objectivesLimits.length; i++){
			if(this.objectivesLimits[i] > hypercube.getObjectiveLimit(i)){
				condition1 = false;
				break;
			}
		}
		
		if(condition1){
			for(int i=0; i<this.objectivesLimits.length; i++){
				if(this.objectivesLimits[i]!=hypercube.getObjectiveLimit(i))
					return true;
			}
		}*/
		
		if(this.size()<hypercube.size()){
			return true;
		}
		
		return false;
	}
	
	public double getObjectiveLimit(int dimension){
		return this.objectivesLimits[dimension];
	}
	
	public void addSolution(int indexSolution){
		this.indexSolutions.add(indexSolution);
	}
	
	public void removeSolution(int indexSolution){
		for(int i=0; i<indexSolutions.size(); i++){
			if(indexSolutions.get(i) == indexSolution){
				this.indexSolutions.remove(i);
			}
		}
	}
	
	public boolean equals(HyperCube hypercube){
		for(int i=0; i<objectivesLimits.length; i++){
			if(hypercube.getObjectiveLimit(i) != objectivesLimits[i]){
				return false;
			}
		}
		return true;
	}
	
	public int getRandom(){
		if(this.size() == 0){
			return Integer.MAX_VALUE;
		}else if(this.size() == 1){
			return this.indexSolutions.get(0);
		}else{
			int i = new Random().nextInt(this.size()-1);
			return indexSolutions.get(i);
		}
	}
	
	public int size(){
		return this.indexSolutions.size();
	}
	
	public String toString() {
		String aux = "";
		DecimalFormat casas = new DecimalFormat("0.00000");
		for (int i = 0; i < this.objectivesLimits.length; i++)
			aux = aux + casas.format(this.objectivesLimits[i]) + " ";

		return aux;
	}
}
