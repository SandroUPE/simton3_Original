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
public class ZDT6 extends Problem<Double> {
	
	public ZDT6(int numberOfVariables, int numberOfObjectives) {
		super(numberOfVariables, numberOfObjectives, "ZDT6");
		
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
		double f1 = 1 - ( Math.exp((-4)*variables[0])* Math.pow(Math.sin(6*Math.PI*variables[0]), 6) );
		solution.setObjective(0, f1);
		
		double sum = 0;
		int n = numberOfVariables ;
		
		for(int i=1; i<n; i++){
			sum += variables[i];
		}
		
		double g = 1 + (9* Math.pow(sum/9.0, 0.25));
		double h = 1 - ((f1/g)*(f1/g));
		
		solution.setObjective(1, g*h);
	}

	@Override
	public void evaluateConstraints(Solution<Double> solution) {
		Double[] variables = solution.getDecisionVariables();
		
		for(int i=0; i<variables.length; i++){
			if(variables[i]<super.getLowerLimit(i))
				variables[i] = super.getLowerLimit(i);
			else if(variables[i]>super.getUpperLimit(i))
				variables[i] = super.getUpperLimit(i);
			
		}
	}

}
