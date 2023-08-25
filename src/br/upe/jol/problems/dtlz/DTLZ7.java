package br.upe.jol.problems.dtlz;

import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;

/**
 * Esta classe foi adaptada a partir da classe DTLZ7 do framework JMetal.
 * 
 * @author Erick Barboza
 */
public class DTLZ7 extends Problem<Double> {

	public DTLZ7(int numberOfVariables, int numberOfObjectives) {
		super(numberOfVariables, numberOfObjectives, "DTLZ7");
		
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
	            
	    for (int i = 0; i < numberOfVariables; i++)
	      x[i] = gen[i];
	        
	    //Calculate g
	    double g = 0.0;
	    for (int i = this.numberOfVariables - k; i < numberOfVariables; i++)
	      g += x[i] ;
	        
	    g = 1 + (9.0 * g) / k;
	    //<-
	                
	    //Calculate the value of f1,f2,f3,...,fM-1 (take acount of vectors start at 0)
	    for (int i = 0; i < numberOfObjectives-1; i++)
	      f[i] = x[i];
	    //<-
	        
	    //->Calculate fM
	    double h = 0.0;
	    for (int i = 0; i < numberOfObjectives -1; i++)
	      h += (f[i]/(1.0 + g))*(1 + Math.sin(3.0 * Math.PI * f[i]));
	       
	    h = numberOfObjectives - h;
	       
	    f[numberOfObjectives-1] = (1 + g) * h;
	    //<-
	        
	    //-> Setting up the value of the objetives
	    for (int i = 0; i < numberOfObjectives; i++)
	      solution.setObjective(i,f[i]); 
	   }


	@Override
	public void evaluateConstraints(Solution<Double> solution) {
		// TODO Auto-generated method stub
		
	}

}
