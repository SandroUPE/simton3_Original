/**
 * 
 */
package br.upe.jol.metaheuristics.spea2;

import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;

/**
 * @author Danilo
 *
 */
public class SPEA2Solution extends Solution<Double> {
	private static final long serialVersionUID = 1L;

	private int kDistance;
	
	public int getkDistance() {
		return kDistance;
	}
	
	private void evaluatekDistance(SolutionSet<Double> union){
		this.kDistance = (int) ((Math.sqrt(union.getMaxSize())));
	}

	public void setkDistance(int kDistance) {
		this.kDistance = kDistance;
	}
	
	public SPEA2Solution(){
		super();
	}
	
	public SPEA2Solution(Problem<Double> problem, Double[] variables) {
		super(problem, variables);
	}
	
	public void evaluateFitness(SolutionSet<Double> union){
		double fitness = this.raw(union) + this.density(union);
		this.setFitness(fitness);
	}
	
	private double raw(SolutionSet<Double> union){
		int count = 0;
		for(int i=0; i<union.size(); i++){
			if(union.get(i).dominates(this))
				count += ((SPEA2Solution) union.get(i)).strenght(union);
		}
		
		return count;
	}
	
	private double strenght(SolutionSet<Double> union){
		int count = 0;
		for(int i=0; i<union.size(); i++){
				if(this.dominates(union.get(i)))
					count++;
		}
		return count;
	}
	
	private double density(SolutionSet<Double> union){
		this.evaluatekDistance(union);
		double[] distancias = this.distanceOrdered(union);
		
		return 1.0/(distancias[this.kDistance-1]+2);
	}
}
