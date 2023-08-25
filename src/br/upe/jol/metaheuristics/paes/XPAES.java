/**
 * 
 */
package br.upe.jol.metaheuristics.paes;

import br.upe.jol.base.Algorithm;
import br.upe.jol.base.DistanceCalculator;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.base.comparators.CrowdingComparator;
import br.upe.jol.base.comparators.DominanceComparator;
import br.upe.jol.configuration.GeneralMOOParametersTO;
import br.upe.jol.operators.mutation.Mutation;
import br.upe.jol.operators.mutation.UniformMutation;

/**
 * .
 * 
 * @author Danilo Ara�jo
 */
public class XPAES extends Algorithm<Double> {
	private static final long serialVersionUID = 1L;
	
	private int maxIterations = 1000;
	
	private int iteration = 0;
	
	private int populationSize = 50;
	
	private SolutionSet<Double> population;
	
	@Override
	public String toString() {
		return "PAES";
	}
	
	public XPAES(int populationSize, int maxIterations, Problem<Double> problem){
		this.populationSize = populationSize;
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
		this.iteration = lastGeneration;
		Solution<Double> offspring = getInitialSolution();
		Mutation<Double> mutation = new UniformMutation(1);
		DistanceCalculator distance = DistanceCalculator.getInstance();
		DominanceComparator<Double> dominanceComparator = new DominanceComparator<Double>();
		CrowdingComparator<Double> crowdingComparator = new CrowdingComparator<Double>();
		if (observer != null){
			observer.setSolutionSet(population);
		}
		while (nextIteration()){
			offspring = (Solution<Double>)mutation.execute(getInitialSolution());
			
			population.add(offspring);
			
			if (population.size() > populationSize){
				distance.crowdingDistanceAssignment(population, problem.getNumberOfObjectives());
				population.sort(dominanceComparator);
				if (!population.get(populationSize-1).dominates(population.get(populationSize-2))){
					population.sort(crowdingComparator);
				}
				population.getSolutionsList().remove(populationSize); 
			}
			updateObserver();
			iteration++;
		}
		return population;
	}

	private Solution<Double> getInitialSolution() {
		if(this.population == null){
			this.population = new SolutionSet<Double>(populationSize);
		}
		Double[] variables = new Double[problem.getNumberOfVariables()];
		Solution<Double> solution = null;
		variables = new Double[problem.getNumberOfVariables()];
		for (int j = 0; j < problem.getNumberOfVariables(); j++) {
			variables[j] = Math.random() * problem.getUpperLimit(j)
					+ problem.getLowerLimit(j);
		}
		solution = new Solution<Double>(problem, variables);
		return solution;
	}

	private void updateObserver() {
		if (observer != null){
			int index = 0;
			for (Solution<Double> solution : population.getSolutionsList()) {
				for (int i = 0; i < solution.numberOfVariables(); i++){
					observer.setVariableValue(index, i, solution.getDecisionVariables()[i]);
				}
				for (int i = 0; i < solution.numberOfObjectives(); i++){
					observer.setObjective(index, i, solution.getObjective(i));
				}
				index++;
			}
			try {
				Thread.sleep(50);
				Thread.yield();
			} catch (InterruptedException e) {
			}
			observer.setIteration(iteration);
		}
	}

	private boolean nextIteration(){
		return iteration <= maxIterations; 
	}

	@Override
	public void setParameters(GeneralMOOParametersTO parameters) {
		// TODO Auto-generated method stub
		
	}
}
