package br.upe.jol.problems.dtlz;

import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;

/**
 * Esta classe foi adaptada a partir da classe DTLZ5 do framework JMetal.
 * 
 * @author Erick Barboza
 */
public class DTLZ5 extends Problem<Double> {

	public DTLZ5(int numberOfVariables, int numberOfObjectives) {
		super(numberOfVariables, numberOfObjectives, "DTLZ5");
		
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
	    double [] theta = new double[numberOfObjectives-1];
	    double g = 0.0;
	    int k = numberOfVariables - numberOfObjectives + 1;
	                              
	    for (int i = 0; i < numberOfVariables; i++){
	      x[i] = gen[i];
	    }
	                
	    for (int i = numberOfVariables - k; i < numberOfVariables; i++){
	      g += (x[i] - 0.5)*(x[i] - 0.5);        
	    }
	        
	    double t = java.lang.Math.PI  / (4.0 * (1.0 + g)); 
	    
	    theta[0] = x[0] * java.lang.Math.PI / 2.0;  
	    for (int i = 1; i < (numberOfObjectives-1); i++){
	      theta[i] = t * (1.0 + 2.0 * g * x[i]);		
	    }
	        
	    for (int i = 0; i < numberOfObjectives; i++){
	      f[i] = 1.0 + g;
	    }
	        
	    for (int i = 0; i < numberOfObjectives; i++){
	      for (int j = 0; j < numberOfObjectives - (i + 1); j++)            
	        f[i] *= java.lang.Math.cos(theta[j]);                
	        if (i != 0){
	          int aux = numberOfObjectives - (i + 1);
	          f[i] *= java.lang.Math.sin(theta[aux]);
	        } // if
	    } //for
	        
	    for (int i = 0; i < numberOfObjectives; i++){
	      solution.setObjective(i,f[i]);     
	    }
	   }


	@Override
	public void evaluateConstraints(Solution<Double> solution) {
		// TODO Auto-generated method stub
		
	}

}
