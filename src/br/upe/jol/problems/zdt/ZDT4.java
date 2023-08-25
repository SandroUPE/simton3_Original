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
public class ZDT4 extends Problem<Double> {
	
	public ZDT4(int numberOfVariables, int numberOfObjectives) {
		super(numberOfVariables, numberOfObjectives, "ZDT4");
		
		for(int i=0; i<super.numberOfVariables; i++){
			if(i==0){
				super.upperLimit[i] = 1.0;
				super.lowerLimit[i] = 0.0;
			}else{
				super.upperLimit[i] = 5.0;
				super.lowerLimit[i] = -5.0;
			}
		}
	}

	
	private static final long serialVersionUID = 1L;
	
	@Override
	public void evaluate(Solution<Double> solution) {
		Double[] variables = solution.getDecisionVariables();
		double f1 = variables[0];
		solution.setObjective(0, f1);
		
		double sum = 0;
		double temp = 0;
		int n = numberOfVariables ;
		for(int i=1; i<n; i++){
			temp = variables[i]*variables[i];
			temp -= 10 * Math.cos(4 * Math.PI * variables[i]);
			sum += temp;
		}
		
		double g = 1 + (10*(n-1)) + sum;
		double h = 1 - Math.sqrt(f1/g);
		
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
