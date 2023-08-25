/**
 * 
 */
package br.upe.jol.metaheuristics.spea2;

import br.upe.jol.base.Problem;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * .
 * 
 * @author Danilo Araújo
 */
public class ISPEA2Solution extends SolutionONTD {
	private static final long serialVersionUID = 1L;

	private int kDistance;
	
	public int getkDistance() {
		return kDistance;
	}
	
	private void evaluatekDistance(SolutionSet<Integer> union){
		this.kDistance = (int) ((Math.sqrt(union.getMaxSize())));
	}

	public void setkDistance(int kDistance) {
		this.kDistance = kDistance;
	}
	
	public ISPEA2Solution(){
		super();
	}
	
	public ISPEA2Solution(Problem<Integer> problem, Integer[] variables) {
		super(problem, variables);
	}
	
	public ISPEA2Solution(Problem<Integer> problem, Integer[] variables, int id) {
		super(problem, variables, id);		
	}
	
	public void evaluateFitness(SolutionSet<Integer> union){
		double fitness = this.raw(union) + this.density(union);
		this.setFitness(fitness);
	}
	
	private double raw(SolutionSet<Integer> union){
		int count = 0;
		for(int i=0; i<union.size(); i++){
			if(union.get(i).dominates(this))
				count += ((ISPEA2Solution) union.get(i)).strenght(union);
		}
		
		return count;
	}
	
	private double strenght(SolutionSet<Integer> union){
		int count = 0;
		for(int i=0; i<union.size(); i++){
				if(this.dominates(union.get(i)))
					count++;
		}
		return count;
	}
	
	private double density(SolutionSet<Integer> union){
		this.evaluatekDistance(union);
		double[] distancias = this.distanceOrdered(union);
		
		return 1.0/(distancias[this.kDistance-1]+2);
	}
}
