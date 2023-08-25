/**
 * 
 */
package br.upe.jol.metaheuristics.mopso;

import br.upe.jol.base.Algorithm;
import br.upe.jol.base.DistanceCalculator;
import br.upe.jol.base.Problem;
import br.upe.jol.base.RankUtil;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.base.comparators.CrowdingComparator;
import br.upe.jol.configuration.GeneralMOOParametersTO;
import br.upe.jol.operators.crossover.Crossover;
import br.upe.jol.operators.crossover.OnePointCrossover;
import br.upe.jol.operators.mutation.Mutation;
import br.upe.jol.operators.mutation.UniformMutation;

/**
 * @author Danilo
 *
 */
public class MOPSO extends Algorithm<Double> {
	private static final long serialVersionUID = 1L;
	
	private int maxIterations = 100;
	
	private int iteration = 0;
	
	private int populationSize = 30;
	
	private SolutionSet<Double> population;
	
	@Override
	public String toString() {
		return "MOPSO";
	}
	
	public MOPSO(int populationSize, int maxIterations, Problem<Double> problem){
		this.populationSize = populationSize;
		this.maxIterations = maxIterations;
		this.problem = problem;
	}
	
	
	@Override
	public SolutionSet<Double> execute() {
		population = new SolutionSet<Double>();
		SolutionSet<Double> offsprings = new SolutionSet<Double>();
		SolutionSet<Double> union = new SolutionSet<Double>();
		int frontIndex = 0;
		Crossover<Double> crossover = new OnePointCrossover(0.9);
		Mutation<Double> mutation = new UniformMutation(0.06);
		DistanceCalculator distance = DistanceCalculator.getInstance();
		initializePopulation();
		RankUtil<Double> ranking = RankUtil.getDoubleInstance();
		SolutionSet<Double>[] rankedSolutions;
		while (nextIteration()){
			System.out.println(iteration);
			offsprings.getSolutionsList().clear();
			generateOffspringPopulation(offsprings, crossover, mutation);
			
			union.getSolutionsList().clear();
			union.getSolutionsList().addAll(population.getSolutionsList());
			union.getSolutionsList().addAll(offsprings.getSolutionsList());
			
			rankedSolutions = ranking.getRankedSolutions(union);
			population.getSolutionsList().clear();
			frontIndex = 0;
			SolutionSet<Double> front = null;
			while (population.size() < populationSize/2){
				front = rankedSolutions[frontIndex];
				distance.crowdingDistanceAssignment(front, problem.getNumberOfObjectives());
				front.sort(new CrowdingComparator());
				for (Solution<Double> solution : front.getSolutionsList()){
					population.getSolutionsList().add(solution);
					if (population.size() >= populationSize/2){
						break;
					}
				}
				frontIndex++;
			}
			iteration++;
		}
		return population;
	}

	private void generateOffspringPopulation(SolutionSet<Double> offsprings,
			Crossover<Double> crossover, Mutation<Double> mutation) {
		Solution<Double> offspring1;
		Solution<Double> offspring2;
		for (int i = 0; i < populationSize/2; i++){
			offspring1 = population.getRandom();
			offspring2 = population.getRandom();
			
			Solution<Double>[] vector =  (Solution<Double>[]) crossover.execute(offspring1, offspring2);
			
			offspring1 = vector[0];
			offspring2 = vector[1];
			
			mutation.execute(offspring1);
			mutation.execute(offspring2);
			
			offsprings.add(offspring1);
			offsprings.add(offspring2);
		}
	}

	private void initializePopulation() {
		Double[] variables = null;
		Solution<Double> solution = null;
		for (int i = 0; i < populationSize; i++){
			variables = new Double[problem.getNumberOfVariables()];
			for (int j = 0; j < problem.getNumberOfVariables(); j++){
				variables[j] = Math.random() * problem.getUpperLimit(j) + problem.getLowerLimit(j);
			}
			solution = new Solution<Double>(problem, variables);
			population.add(solution);
		}
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
		return iteration < maxIterations; 
	}

	@Override
	public void setParameters(GeneralMOOParametersTO parameters) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SolutionSet<Double> execute(SolutionSet<Double> ss,
			int lastGeneration) {
		// TODO Auto-generated method stub
		return null;
	}
}
