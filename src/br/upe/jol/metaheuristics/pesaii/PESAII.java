/**
 * 
 */
package br.upe.jol.metaheuristics.pesaii;

import br.upe.jol.base.Algorithm;
import br.upe.jol.base.ArchiveGrid;
import br.upe.jol.base.Observer;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.configuration.GeneralMOOParametersTO;
import br.upe.jol.configuration.HyperBoxMOOParametersTO;
import br.upe.jol.operators.crossover.OnePointCrossover;
import br.upe.jol.operators.mutation.UniformMutation;
import br.upe.jol.operators.selection.EPESAIISelection;

/**
 * @author Erick
 *
 */
public class PESAII extends Algorithm<Double> {
	private static final long serialVersionUID = 1L;
	
	private int maxIterations = 100;
	
	private int iteration = 0;
	
	private int populationSize;
	
	private int archiveMaxSize;
	
	private SolutionSet<Double> population;
	
	private ArchiveGrid<Double> archive;
	
	private int numberOfDivisions;
	
	private double crossoverProbability = 0.9;
	
	private double mutationProbability = 0.1;
	
	@Override
	public String toString() {
		return "PESA II";
	}
	
	public PESAII(int populationSize, int archiveSize, int maxIterations, Problem<Double> problem){
		this.populationSize = populationSize;
		this.maxIterations = maxIterations;
		this.problem = problem;
		this.archiveMaxSize = archiveSize;
	}
	
	@Override
	public SolutionSet<Double> execute() {
		this.initializePopulation();
		return this.execute(this.population, 1);
	}
	
	public SolutionSet<Double> execute(SolutionSet<Double> ss, int lastGeneration) {
		this.population = ss;
		this.archive = new ArchiveGrid<Double>(this.archiveMaxSize, this.numberOfDivisions, this.problem.getNumberOfObjectives());
		
		OnePointCrossover crossover = new OnePointCrossover(this.crossoverProbability); 
		EPESAIISelection<Double> selection = new EPESAIISelection<Double>();
		UniformMutation mutation = new UniformMutation(mutationProbability);
		
		this.iteration = lastGeneration;
		
		while(this.nextIteration()){
			this.updateArchive();
			
			this.population.clear();
			
			 Solution<Double> [] parents = new Solution[2];
			do{		
				parents[0] = selection.execute(archive);
				parents[1] = selection.execute(archive);
				
				Solution<Double>[] result =  (Solution<Double> []) crossover.execute(parents);
				Solution<Double> offspring = result[0];
				
				offspring = (Solution<Double>) mutation.execute(offspring);
				
				this.problem.evaluateConstraints(offspring);
				offspring.evaluateObjectives();
				this.population.add(offspring);
				
				
				/*if(Math.random() < (1-this.crossoverProbability) 
						&& this.population.size()<this.populationSize){
					Solution<Double> solution1 = selection.execute(archive);	
					solution1 = (Solution<Double>) mutation.execute(solution1);
					this.problem.evaluateConstraints(solution1);
					solution1.evaluateObjectives();
					this.population.add(solution1);
				}*/
			}while(this.population.size() < this.populationSize);
			
			updateObserver();
			this.iteration++;
		}
		
		return this.archive;
	}

	private void updateArchive() {
		for(int i=0; i<this.populationSize; i++){
			this.archive.add(population.get(i));
		}
	}
	
	private void initializePopulation() {
		this.population = new SolutionSet<Double>(populationSize);
		Double[] variables = null;
		Solution<Double> solution = null;
		for (int i = 0; i < populationSize; i++){
			variables = new Double[problem.getNumberOfVariables()];
			for (int j = 0; j < problem.getNumberOfVariables(); j++){
				variables[j] = Math.random() * problem.getUpperLimit(j) + problem.getLowerLimit(j);
			}
			solution = new Solution<Double>(problem, variables);
			solution.evaluateObjectives();
			this.population.add(solution);
		}
		if (observer != null){
			observer.setSolutionSet(archive);
		}
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

	private boolean nextIteration(){
		return iteration <= maxIterations; 
	}

	public double getCrossoverProbability() {
		return crossoverProbability;
	}

	public void setCrossoverProbability(double crossoverProbability) {
		this.crossoverProbability = crossoverProbability;
	}

	public double getMutationProbability() {
		return mutationProbability;
	}

	public void setMutationProbability(double mutationProbability) {
		this.mutationProbability = mutationProbability;
	}

	public int getArchiveSize() {
		return archiveMaxSize;
	}

	public void setArchiveSize(int archiveSize) {
		this.archiveMaxSize = archiveSize;
	}

	@Override
	public void setParameters(GeneralMOOParametersTO parameters) {
		if(!(parameters instanceof HyperBoxMOOParametersTO)){
			throw new RuntimeException("Parametros incorretos");
		}
		
		this.mutationProbability = parameters.getMutationProbability();
		this.crossoverProbability = parameters.getCrossoverProbability();
		this.archiveMaxSize = ((HyperBoxMOOParametersTO) parameters).getArchiveSize();
		this.populationSize = parameters.getPopulationSize();
		this.numberOfDivisions = ((HyperBoxMOOParametersTO) parameters).getBisections();
	}
}
