/**
 * 
 */
package br.upe.jol.metaheuristics.paes;


import br.upe.jol.base.Algorithm;
import br.upe.jol.base.ArchiveGrid;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.base.Util;
import br.upe.jol.base.comparators.DominanceComparator;
import br.upe.jol.configuration.GeneralMOOParametersTO;
import br.upe.jol.configuration.HyperBoxMOOParametersTO;
import br.upe.jol.operators.mutation.IUniformMutation;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * @author Danilo
 * 
 */
public class IPAES extends Algorithm<Integer> {
	private static final long serialVersionUID = 1L;

	private int maxIterations = 2000 * 100;

	private int iteration = 0;

	private int archiveSize = 100;
	
	private int nDivs = 5;

	private SolutionSet<Integer> population;
	
	private ArchiveGrid<Integer> archive;
	
	private String pathFiles = "C:/Temp/results2/ontd";

	@Override
	public String toString() {
		return "PAES";
	}

	public IPAES(int archiveSize, int maxIterations, Problem<Integer> problem) {
		this.archiveSize = archiveSize;
		this.maxIterations = maxIterations;
		this.problem = problem;
	}
	
	@Override
	public SolutionSet<Integer> execute() {
		return this.execute(this.population, 1);
	}

	@Override
	public SolutionSet<Integer> execute(SolutionSet<Integer> ss, int lastGeneration) {
		population = ss;
		DominanceComparator<Integer> dominance = new DominanceComparator<Integer>();
		this.iteration = archiveSize * lastGeneration;
		this.archive = new ArchiveGrid<Integer>(archiveSize, nDivs, 
				problem.getNumberOfObjectives());
		
		Solution<Integer> parent = getInitialSolution();
	
		problem.evaluate(parent);
		problem.evaluateConstraints(parent);
		this.archive.add(parent);
		
		Solution<Integer> offspring = null;
		IUniformMutation mutation = new IUniformMutation(1);
		
		while (nextIteration()) {
			offspring = (SolutionONTD)parent.clone(); 
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
		    if ((iteration/archiveSize) % Util.GRAV_EXP_STEP == 0){
		    	Util.LOGGER.info(">>>>>>>>>>>>>>>>>>> PAES, geração " + (iteration/50) + " <<<<<<<<<<<<<<<<<<<<<<<");
				gravarResultados(iteration/archiveSize);
			}
			iteration++;
		}
		return archive;
	}

	/**
	 * Grava os resultados em arquivos
	 *
	 * @param iteration Número da iteração
	 */
	private void gravarResultados(int iteration) {
		System.out.println("Gravando resultados em " + pathFiles + "_paes_" + archive.getCapacity() + "_" + nDivs + "_" + itf.format(iteration));
		archive.printObjectivesToFile(pathFiles + "_paes_" + archiveSize + "_" + nDivs + "_" + itf.format(iteration) + "_pf.txt");
		archive.printVariablesToFile(pathFiles + "_paes_" + archiveSize + "_" + nDivs + "_" + itf.format(iteration) + "_var.txt");
	}

	private Solution<Integer> getInitialSolution() {
		if(this.population == null){
			this.population = new SolutionSet<Integer>(1);
		}
		Integer[] variables = new Integer[problem.getNumberOfVariables()];
		Solution<Integer> solution = null;
		variables = new Integer[problem.getNumberOfVariables()];
		for (int j = 0; j < problem.getNumberOfVariables(); j++) {
			variables[j] = (int) (Math.round(Math.random() 
					* (problem.getUpperLimit(j) - problem.getLowerLimit(j))) + problem.getLowerLimit(j));	
		}
		solution = new SolutionONTD(problem, variables);
		solution.evaluateObjectives();
		problem.evaluateConstraints(solution);
		return solution;
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
	  public Solution<Integer> test(Solution<Integer> solution, 
	                       Solution<Integer> mutatedSolution, 
	                       ArchiveGrid<Integer> archive){  
	    
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

	/**
	 * Método acessor para obter o valor do atributo maxIterations.
	 * @return O valor de maxIterations
	 */
	public int getMaxIterations() {
		return maxIterations;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo maxIterations.
	 * @param maxIterations O novo valor de maxIterations
	 */
	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	/**
	 * Método acessor para obter o valor do atributo iteration.
	 * @return O valor de iteration
	 */
	public int getIteration() {
		return iteration;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo iteration.
	 * @param iteration O novo valor de iteration
	 */
	public void setIteration(int iteration) {
		this.iteration = iteration;
	}

	/**
	 * Método acessor para obter o valor do atributo archiveSize.
	 * @return O valor de archiveSize
	 */
	public int getArchiveSize() {
		return archiveSize;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo archiveSize.
	 * @param archiveSize O novo valor de archiveSize
	 */
	public void setArchiveSize(int archiveSize) {
		this.archiveSize = archiveSize;
	}

	/**
	 * Método acessor para obter o valor do atributo nDivs.
	 * @return O valor de nDivs
	 */
	public int getnDivs() {
		return nDivs;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo nDivs.
	 * @param nDivs O novo valor de nDivs
	 */
	public void setnDivs(int nDivs) {
		this.nDivs = nDivs;
	}

	/**
	 * Método acessor para obter o valor do atributo population.
	 * @return O valor de population
	 */
	public SolutionSet<Integer> getPopulation() {
		return population;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo population.
	 * @param population O novo valor de population
	 */
	public void setPopulation(SolutionSet<Integer> population) {
		this.population = population;
	}

	/**
	 * Método acessor para obter o valor do atributo archive.
	 * @return O valor de archive
	 */
	public ArchiveGrid<Integer> getArchive() {
		return archive;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo archive.
	 * @param archive O novo valor de archive
	 */
	public void setArchive(ArchiveGrid<Integer> archive) {
		this.archive = archive;
	}

	/**
	 * Método acessor para obter o valor do atributo pathFiles.
	 * @return O valor de pathFiles
	 */
	public String getPathFiles() {
		return pathFiles;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo pathFiles.
	 * @param pathFiles O novo valor de pathFiles
	 */
	public void setPathFiles(String pathFiles) {
		this.pathFiles = pathFiles;
	}
}
