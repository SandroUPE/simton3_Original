/**
 * ssNSGAII.java
 * @author Antonio J. Nebro
 * @version 1.0  
 */
package br.upe.jol.metaheuristics.nsgaii;

import br.upe.jol.base.Algorithm;
import br.upe.jol.base.DistanceCalculator;
import br.upe.jol.base.Problem;
import br.upe.jol.base.RankUtil;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.base.comparators.CrowdingComparator;
import br.upe.jol.base.comparators.DominanceComparator;
import br.upe.jol.base.comparators.IObserver;
import br.upe.jol.configuration.GeneralMOOParametersTO;
import br.upe.jol.operators.crossover.Crossover;
import br.upe.jol.operators.crossover.ICrossover;
import br.upe.jol.operators.mutation.IUniformMutation;
import br.upe.jol.operators.mutation.Mutation;
import br.upe.jol.operators.selection.BinaryTournament;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * This class implements a steady-state version of NSGA-II.
 */
public class ssNSGAII extends Algorithm<Integer> {
	private static final long serialVersionUID = 1L;

	private int maxIterations = 100;

	private int iteration = 0;

	private int populationSize = 30;

	private SolutionSet<Integer> population;

	private double mutationProbability = 0.10;

	private double crossoverProbability = 0.9;

	private String pathFiles = "C:/Temp/results/";

	@Override
	public String toString() {
		return "NSGA II";
	}

	public ssNSGAII(int populationSize, int maxIterations, Problem<Integer> problem) {
		this.populationSize = populationSize;
		this.population = new SolutionSet<Integer>();
		this.maxIterations = maxIterations;
		this.problem = problem;
	}

	@Override
	public SolutionSet<Integer> execute(SolutionSet<Integer> ss, int lastGeneration) {
		this.iteration = lastGeneration;
		population.getSolutionsList().addAll(ss.getSolutionsList());
		SolutionSet<Integer> offspringPopulation = new SolutionSet<Integer>();
		SolutionSet<Integer> union = new SolutionSet<Integer>();

		Crossover<Integer> crossover = new ICrossover(this.crossoverProbability);
		Mutation<Integer> mutation = new IUniformMutation(this.mutationProbability);
		BinaryTournament<Integer> selection = new BinaryTournament<Integer>(new DominanceComparator<Integer>());
		DistanceCalculator distance = DistanceCalculator.getInstance();
		RankUtil<Integer> ranking = RankUtil.getIntegerInstance();
		SolutionSet<Integer>[] rankedSolutions;

		// Generations ...
		while (iteration < maxIterations) {

			// Create the offSpring solutionSet
			offspringPopulation = new SolutionSet(populationSize);
			SolutionONTD[] parents = new SolutionONTD[2];

			// obtain parents
			parents[0] = (SolutionONTD) selection.execute(population);
			parents[1] = (SolutionONTD) selection.execute(population);

			// crossover
			SolutionONTD[] offSpring = (SolutionONTD[]) crossover.execute(parents);

			// mutation
			mutation.execute(offSpring[0]);

			// evaluation
			problem.evaluateConstraints(offSpring[0]);
			problem.evaluate(offSpring[0]);

			// insert child into the offspring population
			offspringPopulation.add(offSpring[0]);

			iteration++;

			// Create the solutionSet union of solutionSet and offSpring
			union = ((SolutionSet) population).union(offspringPopulation);

			// Ranking the union
			rankedSolutions = ranking.getRankedSolutions(union);

			int remain = populationSize;
			int index = 0;
			SolutionSet front = null;
			population.clear();

			// Obtain the next front
			front = rankedSolutions[index];

			while ((remain > 0) && (remain >= front.size())) {
				// Assign crowding distance to individuals
				distance.crowdingDistanceAssignment(front, problem.getNumberOfObjectives());
				// Add the individuals of this front
				for (int k = 0; k < front.size(); k++) {
					population.add(front.get(k));
				} // for

				// Decrement remain
				remain = remain - front.size();

				// Obtain the next front
				index++;
				if (remain > 0) {
					front = rankedSolutions[index];
				} // if
			} // while

			// Remain is less than front(index).size, insert only the best one
			if (remain > 0) { // front contains individuals to insert
				distance.iCrowdingDistanceAssignment(front, problem.getNumberOfObjectives());
				front.sort(new CrowdingComparator());
				for (int k = 0; k < remain; k++) {
					population.add(front.get(k));
				} // for

				remain = 0;
			}
			if (iteration % 25 == 0){
				population.printObjectivesToFile(pathFiles + "_ssnsgaii_pf_" + (iteration/25) + ".txt");
				population.printVariablesToFile(pathFiles + "_ssnsgaii_var_" + (iteration/25) + ".txt");	
			}
			
		}

		// Return as output parameter the required evaluations
		rankedSolutions = ranking.getRankedSolutions(population);
		return rankedSolutions[0];
	}

	@Override
	public void setParameters(GeneralMOOParametersTO parameters) {
		this.mutationProbability = parameters.getMutationProbability();
		this.crossoverProbability = parameters.getCrossoverProbability();
		this.populationSize = parameters.getPopulationSize();
	}

	private void initializePopulation() {
		Integer[] variables = null;
		Solution<Integer> solution = null;
		for (int i = 0; i < populationSize; i++) {
			variables = new Integer[problem.getNumberOfVariables()];
			for (int j = 0; j < problem.getNumberOfVariables(); j++) {
				variables[j] = (int) (Math.round(Math.random() * (problem.getUpperLimit(j) - problem.getLowerLimit(j))) + problem
						.getLowerLimit(j));
			}
			solution = new SolutionONTD(problem, variables);
			problem.evaluateConstraints(solution);
			problem.evaluate(solution);
			population.add(solution);
		}
		if (observer != null) {
			((IObserver) observer).setISolutionSet(population);
		}
	}

	@Override
	public SolutionSet<Integer> execute() {
		initializePopulation();
		return execute(population, 0);
	}

	/**
	 * Método acessor para obter o valor do atributo maxIterations.
	 * 
	 * @return O valor de maxIterations
	 */
	public int getMaxIterations() {
		return maxIterations;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo maxIterations.
	 * 
	 * @param maxIterations
	 *            O novo valor de maxIterations
	 */
	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	/**
	 * Método acessor para obter o valor do atributo iteration.
	 * 
	 * @return O valor de iteration
	 */
	public int getIteration() {
		return iteration;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo iteration.
	 * 
	 * @param iteration
	 *            O novo valor de iteration
	 */
	public void setIteration(int iteration) {
		this.iteration = iteration;
	}

	/**
	 * Método acessor para obter o valor do atributo populationSize.
	 * 
	 * @return O valor de populationSize
	 */
	public int getPopulationSize() {
		return populationSize;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo populationSize.
	 * 
	 * @param populationSize
	 *            O novo valor de populationSize
	 */
	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}

	/**
	 * Método acessor para obter o valor do atributo population.
	 * 
	 * @return O valor de population
	 */
	public SolutionSet<Integer> getPopulation() {
		return population;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo population.
	 * 
	 * @param population
	 *            O novo valor de population
	 */
	public void setPopulation(SolutionSet<Integer> population) {
		this.population = population;
	}

	/**
	 * Método acessor para obter o valor do atributo mutationProbability.
	 * 
	 * @return O valor de mutationProbability
	 */
	public double getMutationProbability() {
		return mutationProbability;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo mutationProbability.
	 * 
	 * @param mutationProbability
	 *            O novo valor de mutationProbability
	 */
	public void setMutationProbability(double mutationProbability) {
		this.mutationProbability = mutationProbability;
	}

	/**
	 * Método acessor para obter o valor do atributo crossoverProbability.
	 * 
	 * @return O valor de crossoverProbability
	 */
	public double getCrossoverProbability() {
		return crossoverProbability;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo crossoverProbability.
	 * 
	 * @param crossoverProbability
	 *            O novo valor de crossoverProbability
	 */
	public void setCrossoverProbability(double crossoverProbability) {
		this.crossoverProbability = crossoverProbability;
	}

	/**
	 * Método acessor para obter o valor do atributo pathFiles.
	 * 
	 * @return O valor de pathFiles
	 */
	public String getPathFiles() {
		return pathFiles;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo pathFiles.
	 * 
	 * @param pathFiles
	 *            O novo valor de pathFiles
	 */
	public void setPathFiles(String pathFiles) {
		this.pathFiles = pathFiles;
	}

	/**
	 * Método acessor para obter o valor do atributo problem.
	 * 
	 * @return O valor de problem
	 */
	public Problem<Integer> getproblem() {
		return problem;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo problem.
	 * 
	 * @param problem
	 *            O novo valor de problem
	 */
	public void setProblem(Problem<Integer> problem) {
		this.problem = problem;
	}

	/**
	 * Método acessor para obter o valor do atributo serialversionuid.
	 * 
	 * @return O valor de serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
} // NSGA-II
