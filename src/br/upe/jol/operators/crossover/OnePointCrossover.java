package br.upe.jol.operators.crossover;

import java.util.Random;

import br.upe.jol.base.Solution;
import br.upe.jol.metaheuristics.dwga.HVCASolution;
import br.upe.jol.metaheuristics.spea2.SPEA2Solution;


public class OnePointCrossover extends Crossover<Double> {

	public OnePointCrossover(double crossoverPropability) {
		super(crossoverPropability);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Object execute(Solution<Double>... parents) {
		Solution<Double>[] offSpring = new Solution[2];
		
		Solution<Double> offs1 = null;
		Solution<Double> offs2 = null;
		
		if(parents[0] instanceof SPEA2Solution){
			 offs1 = new SPEA2Solution(parents[0].getProblem(), parents[0].getDecisionVariables());
			 offs2 = new SPEA2Solution(parents[1].getProblem(), parents[1].getDecisionVariables());
		}else if(parents[0] instanceof HVCASolution){
			 offs1 = new HVCASolution(parents[0].getProblem(), parents[0].getDecisionVariables());
			 offs2 = new HVCASolution(parents[1].getProblem(), parents[1].getDecisionVariables());
		}else{
			 offs1 = new Solution<Double>(parents[0].getProblem(), parents[0].getDecisionVariables());
			 offs2 = new Solution<Double>(parents[1].getProblem(), parents[1].getDecisionVariables());
		}
			
		Double[] variables1 = parents[0].getDecisionVariables();
		Double[] variables2 = parents[1].getDecisionVariables();
		
		int r =  new Random().nextInt(variables1.length-1);
		
		for(int i=0; i<variables1.length; i++){
				if(i < r){
					offs1.setValue(i, variables2[i]);
					offs2.setValue(i, variables1[i]);
				}	
		}
		
		offSpring[0] = offs1;
		offSpring[1] = offs2;
		
		return offSpring ;
	}

	@Override
	public String getOpID() {
		return "C6";
	}

}
