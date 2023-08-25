package br.upe.jol.operators.crossover;

import java.util.Random;

import br.upe.jol.base.Solution;

public class MODEIICrossover extends Crossover<Double> {

	private double CR;
	
	public MODEIICrossover(double CR) {
		super(1);
		this.CR = CR;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Object execute(Solution<Double>... parents) {		
		Solution<Double> offspring = new Solution<Double>(parents[0].getProblem(),
				parents[0].getDecisionVariables());
			
		Double[] variables1 = parents[0].getDecisionVariables();
		Double[] variables2 = parents[1].getDecisionVariables();
		
		double r;
		int iRand = new Random().nextInt(variables1.length-1);
		
		for(int i=0; i<variables1.length; i++){
			r =  Math.random();
			if(r <= CR || i == iRand){
				offspring.setValue(i, variables2[i]);
			}	
		}
		
		return offspring ;
	}

	@Override
	public String getOpID() {
		return "C5";
	}

}
