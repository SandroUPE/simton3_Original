/**
 * 
 */
package br.upe.jol.problems.zdt;

import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;

/**
 * @author Danilo
 *
 */
public class ZDT1 extends Problem<Double> {
	
	public ZDT1(int numberOfVariables, int numberOfObjectives) {
		super(numberOfVariables, numberOfObjectives, "ZDT1");
		
		for(int i=0; i<super.numberOfVariables; i++){
			super.upperLimit[i] = 1.0;
			super.lowerLimit[i] = 0.0;
		}
		for(int i=0; i< numberOfObjectives; i++){
			upperLimitObjective[i] = 1.0;
			lowerLimitObjective[i] = 0.0;
		}
	}

	
	private static final long serialVersionUID = 1L;
	
	@Override
	public void evaluate(Solution<Double> solution) {
		Double[] variables = solution.getDecisionVariables();
		solution.setObjective(0, variables[0]);
		
		double sum = 0;
		int n = numberOfVariables ;
		for(int i=1; i<n; i++){
			sum += variables[i];
		}
		
		double g = 1 + ((9.0/(n-1))*sum);
		double h = 1 - Math.sqrt(variables[0]/g);
	
		
		solution.setObjective(1, h);
	}

	@Override
	public void evaluateConstraints(Solution<Double> solution) {
		Double[] variables = solution.getDecisionVariables();
		
		for(int i=0; i<variables.length; i++){
			if(variables[i]<super.getLowerLimit(0))
				variables[i] = super.getLowerLimit(0);
			else if(variables[i]>super.getUpperLimit(0))
				variables[i] = super.getUpperLimit(0);
		}
	}

}
