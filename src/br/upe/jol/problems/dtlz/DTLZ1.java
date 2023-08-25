package br.upe.jol.problems.dtlz;

import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;

/**
 * Esta classe foi adaptada a partir da classe DTLZ1 do framework JMetal.
 * 
 * @author Erick Barboza
 */
public class DTLZ1 extends Problem<Double> {

	public DTLZ1(int numberOfVariables, int numberOfObjectives) {
		super(numberOfVariables, numberOfObjectives, "DTLZ1");
		
		for(int i=0; i<super.numberOfVariables; i++){
			super.upperLimit[i] = 1.0;
			super.lowerLimit[i] = 0.0;
		}
		for(int i=0; i< numberOfObjectives; i++){
			upperLimitObjective[i] = 1.0;
			lowerLimitObjective[i] = 0.0;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	public void evaluate(Solution<Double> solution) {
		 Double[] gen  = solution.getDecisionVariables();
         
	    double [] x = new double[numberOfVariables];
	    double [] f = new double[numberOfObjectives];
	    int k = numberOfVariables - numberOfObjectives + 1;
	        
	    for (int i = 0; i < numberOfVariables; i++){
	      x[i] = gen[i];
	    }
	        
	    double g = 0.0;
	    for (int i = numberOfVariables - k; i < numberOfVariables; i++){
	      g += (x[i] - 0.5)*(x[i] - 0.5) - Math.cos(20.0 * Math.PI * ( x[i] - 0.5));
	    }
	        
	    g = 100 * (k + g);        
	    for (int i = 0; i < numberOfObjectives; i++){
	      f[i] = (1.0 + g) * 0.5;
	    }
	        
	    for (int i = 0; i < numberOfObjectives; i++){
	      for (int j = 0; j < numberOfObjectives - (i + 1); j++)            
	        f[i] *= x[j];               
	        if (i != 0){
	          int aux = numberOfObjectives - (i + 1);
	          f[i] *= 1 - x[aux];
	        } //if
	    }//for
	        
	    for (int i = 0; i < numberOfObjectives; i++){
	      solution.setObjective(i,f[i]);        
	    }
	}

	@Override
	public void evaluateConstraints(Solution<Double> solution) {
		// TODO Auto-generated method stub
		
	}

}
