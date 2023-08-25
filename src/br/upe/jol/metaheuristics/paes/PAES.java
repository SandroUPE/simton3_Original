/**
 * 
 */
package br.upe.jol.metaheuristics.paes;


import br.upe.jol.base.Algorithm;
import br.upe.jol.base.ArchiveGrid;
import br.upe.jol.base.Grid;
import br.upe.jol.base.Observer;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.base.comparators.DominanceComparator;
import br.upe.jol.configuration.GeneralMOOParametersTO;
import br.upe.jol.configuration.HyperBoxMOOParametersTO;
import br.upe.jol.operators.mutation.Mutation;
import br.upe.jol.operators.mutation.UniformMutation;

/**
 * @author Danilo
 * 
 */
public class PAES extends Algorithm<Double> {
	private static final long serialVersionUID = 1L;

	private int maxIterations = 1000;

	private int iteration = 0;

	private int archiveSize = 50;
	
	private int nDivs = 2;

	private SolutionSet<Double> population;
	
	private ArchiveGrid<Double> archive;

	@Override
	public String toString() {
		return "PAES";
	}

	public PAES(int archiveSize, int maxIterations, Problem<Double> problem) {
		this.archiveSize = archiveSize;
		this.maxIterations = maxIterations;
		this.problem = problem;
	}
	
	@Override
	public SolutionSet<Double> execute() {
		return this.execute(this.population, 1);
	}

	@Override
	public SolutionSet<Double> execute(SolutionSet<Double> ss, int lastGeneration) {
		population = ss;
		DominanceComparator<Double> dominance = new DominanceComparator<Double>();
		this.iteration = lastGeneration;
		this.archive = new ArchiveGrid<Double>(archiveSize, nDivs, 
				problem.getNumberOfObjectives());
		
		Solution<Double> parent = getInitialSolution();
	
		problem.evaluate(parent);
		problem.evaluateConstraints(parent);
		this.archive.add(parent);
		
		Solution<Double> offspring = null;
		Mutation<Double> mutation = new UniformMutation(1);
		
		if (observer != null) {
			observer.setSolutionSet(archive);
		}
		
		while (nextIteration()) {
			offspring = parent.clone(); 
			mutation.execute(offspring);
			problem.evaluate(offspring);
			problem.evaluateConstraints(offspring);
			
			int flag = dominance.compare(parent,offspring);            
            
		    if (flag == 1) { //If mutate solution dominate                  
		        parent = offspring.clone();                
		        archive.add(offspring);                
		    } else if (flag == 0) { //If none dominate the other                               
		        if (archive.add(offspring)) {                    
		        	parent = test(parent,offspring,archive);
		        }                               
		    }     
			updateObserver();
			iteration++;
		}
		return archive;
	}

	private Solution<Double> getInitialSolution() {
		if(this.population == null){
			this.population = new SolutionSet<Double>(1);
		}
		Double[] variables = new Double[problem.getNumberOfVariables()];
		Solution<Double> solution = null;
		variables = new Double[problem.getNumberOfVariables()];
		for (int j = 0; j < problem.getNumberOfVariables(); j++) {
			variables[j] = Math.random() * problem.getUpperLimit(j)
					+ problem.getLowerLimit(j);
		}
		solution = new Solution<Double>(problem, variables);
		solution.evaluateObjectives();
		problem.evaluateConstraints(solution);
		return solution;
	}


	private synchronized void updateObserver() {
		if(observer == null){
			observer = new Observer();
			observer.setSolutionSet(archive);
		}else{
			observer.setIteration(iteration);
			observer.setSolutionSet(archive);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
	}


	private boolean nextIteration() {
		return iteration <= maxIterations;
	}

	@Override
	public void setParameters(GeneralMOOParametersTO parameters) {
		if(!(parameters instanceof HyperBoxMOOParametersTO)){
			throw new RuntimeException("Parametro Incorreto");
		}
		this.archiveSize = parameters.getPopulationSize();
		this.nDivs = ((HyperBoxMOOParametersTO)parameters).getBisections();
	}
	
	 /**
	   * Tests two solutions to determine which one becomes be the guide of PAES
	   * algorithm
	   * @param solution The actual guide of PAES
	   * @param mutatedSolution A candidate guide
	   */
	  public Solution<Double> test(Solution<Double> solution, 
	                       Solution<Double> mutatedSolution, 
	                       ArchiveGrid<Double> archive){  
	    
	    int originalLocation = archive.getGrid().location(solution);
	    int mutatedLocation  = archive.getGrid().location(mutatedSolution); 

	    if (originalLocation == -1) {
	      return mutatedSolution.clone();
	    }
	    
	    if (mutatedLocation == -1) {
	      return solution.clone();
	    }
	        
	    if (archive.getGrid().getLocationDensity(mutatedLocation) < 
	        archive.getGrid().getLocationDensity(originalLocation)) {
	      return mutatedSolution.clone();
	    }
	    
	    return solution.clone();          
	  } // test
}
