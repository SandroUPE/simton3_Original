package br.upe.jol.base;

import static br.upe.jol.base.Util.LOGGER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * @author Erick
 * @version 2.0
 * @since 22/08/10
 * @param <T>
 */
public class HyperCubeGrid<T> {
	/**
	 * The number of objectives
	 */
	private int objectives;
	
	/**
	 * The upper limits for each objective
	 */
	private double[] upperLimits;
	
	/**
	 * The lower limits for each objective
	 */
	private double[] lowerLimits;
	
	/**
	 * The number of divison of objective space
	 */
	private int numberOfDivisions;
	
	/**
	 * The size of division of each objective
	 */
	private double[][] divisions;
	
	/**
	 * The list of hyperboxs
	 */
	private List<HyperCube> hypercubeList; 
	
	public HyperCubeGrid(int objectives, int numberOfDivisions){
		this.objectives = objectives;
		this.numberOfDivisions = numberOfDivisions;
		this.lowerLimits = new double[this.objectives];
		this.upperLimits = new double[this.objectives];
		this.divisions = new double[this.objectives][this.numberOfDivisions];
		this.hypercubeList = new ArrayList<HyperCube>();
		this.updateLimits();
	}
	
	/**
	 * Initializing the objectives limits of the grid 
	 *
	 */
	private void updateLimits() {
		 //Init the lower and upper limits 
	    for (int obj = 0; obj < objectives; obj++){
	      //Set the lower limits to the max real
	      lowerLimits[obj] = Double.MAX_VALUE;
	      //Set the upper limits to the min real
	      upperLimits[obj] = Double.MIN_VALUE;
	    } // for
	}
	
	/**
	 * Update the objectives limits based in <code>solution</code>
	 * @param solution
	 */
	private void updateLimits(Solution<T> solution) {	   
      for (int obj = 0; obj < objectives; obj++) {
        if (solution.getObjective(obj) < lowerLimits[obj]) {
          lowerLimits[obj] = solution.getObjective(obj);
        }
        if (solution.getObjective(obj) > upperLimits[obj]) {
          upperLimits[obj] = solution.getObjective(obj);
        }
      } // for
	}
	
	/**
	 * Split the objective space in hypercube
	 */
	private void splitHyperboxs(Solution<T> solution) {
		this.updateLimits(solution);
		
		//calculate the size of hypercube
		double[] sizeHypercube = new double[this.objectives];
		for(int i=0; i<this.objectives; i++){
			sizeHypercube[i] = (upperLimits[i])/((double) this.numberOfDivisions);
		}
		
		//calculate the points where the division will occur
		double[] lowLimitTemp = this.lowerLimits;
		for(int i=0; i<objectives; i++){
			for(int j=0; j<numberOfDivisions; j++){
				this.divisions[i][j] = lowLimitTemp[i] + sizeHypercube[i];
				lowLimitTemp[i] = this.divisions[i][j];
			}
		}
		
		/*make the hyperbox and add to the list
		for(int i=0; i<this.objectives; i++){
			for(int j=i+1; j<this.objectives; j++){
				for(int k=0; k<this.divisions[i].length; k++){
					for(int l=0; l<this.divisions[j].length; l++){
						double[] objectivesLimits = {divisions[i][k], divisions[j][l]};
						HyperBox<T> hyperbox = new HyperBox<T>(objectivesLimits);
						this.hyperboxList.add(hyperbox);
					}
				}
			}
		}*/
	}
	
	
	/**
	 * Add <code>solution</code> into <code>hyperBox</code>
	 * @param hyperBox
	 * @param indexSolution Index of the solution in <code>archive</code>
	 */
	public void addSolution(int indexSolution, EArchiveGrid<T> archive){
		Solution<T> solution = archive.get(indexSolution);
		this.splitHyperboxs(solution);
		HyperCube hypercube = this.getHyperCube(solution);
		boolean add = false;
		for(HyperCube hb : hypercubeList){
			if(hb.equals(hypercube)){
				hb.addSolution(indexSolution);
				add = true;
				break;
			}
		}
		
		if(!add){
			hypercube.addSolution(indexSolution);
			this.hypercubeList.add(hypercube);
		}
	}
	
	public void removeSolution(int indexSolution, EArchiveGrid<T> archive){
		Solution<T> solution = archive.get(indexSolution);
		HyperCube hypercube = this.getHyperCube(solution);
		HyperCube hc = null;
		for(int i=0; i<hypercubeList.size(); i++){
			hc = this.hypercubeList.get(i);
			if(hc.equals(hypercube)){
				hc.removeSolution(indexSolution);
				if(hc.size() == 0){
					this.hypercubeList.remove(i);
				}
				break;
			}
		}
	}
	
	/**
	 * Get the HyperBox where the <code>solution</code> will be
	 * @param solution
	 * @return hyperBox
	 */
	public HyperCube getHyperCube(Solution<T> solution){
		int objectives = solution.numberOfObjectives();
		double[] solutionsObjectives = solution.getAllObjectives();
		double[] objectivesLimits = new double[this.objectives];
	
		for(int i=0; i<objectives; i++){
			for(int k=0; k<divisions[i].length; k++){
				if(solutionsObjectives[i] < divisions[i][k]){
					objectivesLimits[i] = divisions[i][k];
					break;
				}
			}
		}
		
		return new HyperCube(objectivesLimits);
	}
	
	public HyperCube getMostCrowded(){
		int index = 0;
		int maxSize = Integer.MIN_VALUE;
		
		for(int i=0; i<this.size(); i++){
			if(this.get(i).size() > maxSize){
				maxSize = this.get(i).size();
				index = i;
			}
		}
		
		return this.get(index);
	}
	
	/*/**
	 * Get the HyperBox where the <code>solution</code> will be
	 * @param solution
	 * @return hyperBox
	 *
	private HyperBox<T> getHyperBox(Solution<T> solution){
		double[] solutionsObjectives = solution.getAllObjectives();
		double[] objectivesLimits = new double[this.objectives];
	
		for(int i=0; i<this.objectives; i++){
			for(int k=0; k<divisions[i].length; k++){
				if(solutionsObjectives[i] < divisions[i][k]){
					objectivesLimits[i] = divisions[i][k];
					break;
				}
			}
		}
		
		return new HyperBox<T>(objectivesLimits);
	}*/
	
	public int size(){
		return this.hypercubeList.size();
	}
	
	public HyperCube get(int i) {
		if (i >= hypercubeList.size()) {
			throw new IndexOutOfBoundsException("Index out of Bound " + i);
		}
		return hypercubeList.get(i);
	}
	
	public HyperCube getLast(){
		return hypercubeList.get(this.size()-1);
	}
	
	public double[][] getDivisions() {
		return divisions;
	}

	public void setDivisions(double[][] divisions) {
		this.divisions = divisions;
	}

	public void sort(Comparator<HyperCube> comparator) {
		if (comparator == null) {
			LOGGER.severe("No criterium for compare exist");
			return;
		}
		Collections.sort(hypercubeList, comparator);
	}
	
	/**
	 * Get a HyperBox randomly 
	 * @return HyperBox
	 */
	public HyperCube getRandom(){
		if(this.size() == 1){
			return this.hypercubeList.get(0);
		}
		
		int i = new Random().nextInt(this.size()-1);
		
		return hypercubeList.get(i);
	}
	
	public void clear(){
		this.hypercubeList.clear();
	}
	
	public String toString() {
		return this.hypercubeList.toString();
	}
	
}
