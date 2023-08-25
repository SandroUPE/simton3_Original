package br.upe.jol.operators.crossover;

import java.util.Random;

import br.upe.jol.base.Solution;
import br.upe.jol.problems.simton.SolutionONTD;

public class IMODEIICrossover extends Crossover<Integer> {

private double CR;
	
	public IMODEIICrossover(double CR) {
		super(1);
		this.CR = CR;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Object execute(Solution<Integer>... parents) {		
		SolutionONTD offspring = new SolutionONTD(parents[0].getProblem(),
				parents[0].getDecisionVariables());
			
		Integer[] variables1 = parents[0].getDecisionVariables();
		Integer[] variables2 = parents[1].getDecisionVariables();
		
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
		return "C4";
	}

}
