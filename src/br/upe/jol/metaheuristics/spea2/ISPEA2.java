/**
 * 
 */
package br.upe.jol.metaheuristics.spea2;

import br.upe.jol.base.Algorithm;
import br.upe.jol.base.Operator;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.base.Spea2Fitness;
import br.upe.jol.base.Util;
import br.upe.jol.base.comparators.DominanceComparator;
import br.upe.jol.configuration.ArchiveMOOParametersTO;
import br.upe.jol.configuration.GeneralMOOParametersTO;
import br.upe.jol.operators.crossover.Crossover;
import br.upe.jol.operators.crossover.ICrossover;
import br.upe.jol.operators.initializers.SWDistanceInitializer;
import br.upe.jol.operators.mutation.IUniformMutation;
import br.upe.jol.operators.mutation.Mutation;
import br.upe.jol.operators.selection.BinaryTournament;
import br.upe.jol.problems.simton.SimtonProblem;

/**
 * .
 * 
 * @author Danilo Araújo
 */
public class ISPEA2 extends Algorithm<Integer> {
	private static final long serialVersionUID = 1L;

	private int numberOfGenerations;
	private Problem<Integer> problem;
	SolutionSet<Integer> archive = null;
	SolutionSet<Integer> population = null;
	private int populationSize = 50;
	private int archiveSize = 50;
	SolutionSet<Integer> union = null;
	int i;
	private int generation;
	private double mutationProbability = 1 / 93.0;
	private double crossoverProbability = 1;
	private DominanceComparator<Integer> comparator = new DominanceComparator<Integer>();

	private String pathFiles = "C:/Temp/results2/ontd";
	/**
	 * /* Defines the number of tournaments for creating the mating pool
	 */
	public static final int TOURNAMENTS_ROUNDS = 1;

	public void setParameters(GeneralMOOParametersTO parameters) {
		if (!(parameters instanceof ArchiveMOOParametersTO)) {
			throw new RuntimeException("Parametros incorretos");
		}

		this.mutationProbability = parameters.getMutationProbability();
		this.crossoverProbability = parameters.getCrossoverProbability();

		this.archive = new SolutionSet<Integer>(((ArchiveMOOParametersTO) parameters).getArchiveSize());
		this.populationSize = parameters.getPopulationSize();
	}

	public double getMutationProbability() {
		return mutationProbability;
	}

	public void setMutationProbability(double mutationProbability) {
		this.mutationProbability = mutationProbability;
	}

	public double getCrossoverProbability() {
		return crossoverProbability;
	}

	public void setCrossoverProbability(double crossoverProbability) {
		this.crossoverProbability = crossoverProbability;
	}

	public ISPEA2(int populationSize, int archiveSize, int numberOfGenerations, Problem<Integer> problem) {
		this.populationSize = populationSize;
		this.archiveSize = archiveSize;
		this.numberOfGenerations = numberOfGenerations;
		this.problem = problem;
		this.archive = new SolutionSet<Integer>(archiveSize);
		this.population = new SolutionSet<Integer>(populationSize);
		this.union = new SolutionSet<Integer>(populationSize + archiveSize);
		this.mutationProbability = 1 / 93.0;
		this.crossoverProbability = 1;
	}

	@Override
	public String toString() {
		return "SPEA 2";
	}

	@SuppressWarnings("unchecked")
	public SolutionSet<Integer> execute(SolutionSet<Integer> ss, int lastGeneration) {
		Crossover<Integer> crossover = new ICrossover(this.crossoverProbability);
		Mutation<Integer> mutation = new IUniformMutation(this.mutationProbability);
		// initialization
		archive.getSolutionsList().addAll(ss.getSolutionsList());
		generation = lastGeneration;

		gravarResultados(0, crossover, mutation);

		// Mutation mutation = new
		// BarabasiAlbertMutation(this.mutationProbability);
		BinaryTournament<Integer> tournament = new BinaryTournament<Integer>(comparator);
		SolutionSet<Integer> offSpringSolutionSet;
		while (generation <= this.numberOfGenerations) {
//			if (generation % 200 == 0) {
//				((SimtonProblem) problem).setSimulate(true);
//			} else {
//				((SimtonProblem) problem).setSimulate(false);
//			}
			Util.LOGGER.info(">>>>>>>>>>>>>>>>>>> SPEA2, geração " + generation + " <<<<<<<<<<<<<<<<<<<<<<<");
			this.union = this.population.union(archive);

			Spea2Fitness spea = new Spea2Fitness(union);
			spea.fitnessAssign();
			archive = spea.environmentalSelection(archiveSize);

			SolutionSet<Integer> invalid = new SolutionSet<>();
			for (Solution<Integer> sol : archive.getSolutionsList()) {
				if ((generation < 250 && sol.getObjective(0) > 1) || (generation >= 250 && sol.getObjective(0) > 0.1)) {
					invalid.add(sol);
				}
			}
			archive.getSolutionsList().removeAll(invalid.getSolutionsList());

			System.out.println("EA = " + archive.size());
			// Create a new offspringPopulation
			offSpringSolutionSet = new SolutionSet(populationSize);
			Solution[] parents = new Solution[2];
			int qtdeAdded = 0;
			int qtdeEvaluated = 0;
			while (qtdeEvaluated < populationSize) {
				int j = 0;
				do {
					j++;
					parents[0] = (Solution) tournament.execute(archive);
				} while (j < TOURNAMENTS_ROUNDS); // do-while
				int k = 0;
				do {
					k++;
					parents[1] = (Solution) tournament.execute(archive);
				} while (k < TOURNAMENTS_ROUNDS); // do-while

				// make the crossover
				Solution[] offSpring = (Solution[]) crossover.execute(parents);

				mutation.execute(offSpring[0]);

				problem.evaluate(offSpring[0]);
				problem.evaluateConstraints(offSpring[0]);

				if ((generation < 250 && offSpring[0].getObjective(0) < 1)
						|| (generation >= 250 && offSpring[0].getObjective(0) < 0.1)) {
					// if ((generation < 0.05 * this.numberOfGenerations)
					// || (generation < 0.1 * this.numberOfGenerations &&
					// offSpring[0].getObjective(0) < 0.2)
					// || (offSpring[0].getObjective(0) < 0.1)) {
					offSpringSolutionSet.add(offSpring[0]);
					qtdeAdded++;
					// }
				}
				qtdeEvaluated++;
			} // while
			generation++;
			if (generation % Util.GRAV_EXP_STEP == 0) {
				gravarResultados(generation, crossover, mutation);
			}
			// End Create a offSpring solutionSet
			System.out.println("P = " + archive.size());
			population = offSpringSolutionSet;

		} // while

		return this.archive;
	}

	/**
	 * Grava os resultados em arquivos
	 * 
	 * @param iteration
	 *            Número da iteração
	 */
	private void gravarResultados(int iteration, Operator<Integer> crossover, Operator<Integer> mutation) {
		System.out.println("Gravando resultados em " + pathFiles + "_spea2_" + populationSize + "_"
				+ nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_" + itf.format(iteration));
		archive.printObjectivesToFile(pathFiles + "_spea2_" + 50 + "_" + 100 + "_" + nf.format(crossoverProbability)
				+ "_" + nf.format(mutationProbability) + "_" + itf.format(iteration) + "_pf.txt");
		archive.printVariablesToFile(pathFiles + "_spea2_" + 50 + "_" + 100 + "_" + nf.format(crossoverProbability)
				+ "_" + nf.format(mutationProbability) + "_" + itf.format(iteration) + "_var.txt");
	}

	@Override
	public SolutionSet<Integer> execute() {
		Double[][] m = new Double[14][14];
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m.length; j++) {
				m[i][j] = 1.0;
			}
		}
//		population = new SWDistanceInitializer(m.length, 0.08, 0.6, m, 3, "swt").execute(problem, 50);
		 initializePopulationRnd();
		return execute(population, 1);
	}

	private void initializePopulationRnd() {
		Integer[] variables = null;
		Solution<Integer> solution = null;
		for (int i = 0; i < populationSize; i++) {
			variables = new Integer[problem.getNumberOfVariables()];
			for (int j = 0; j < problem.getNumberOfVariables(); j++) {
				if (Math.random() > 0.5) {
					variables[j] = (int) (Math.round(Math.random()
							* (problem.getUpperLimit(j) - problem.getLowerLimit(j))) + problem.getLowerLimit(j));
				} else {
					variables[j] = (int) problem.getLowerLimit(j);
				}
			}
			solution = new ISPEA2Solution(problem, variables);
			problem.evaluate(solution);
			problem.evaluateConstraints(solution);
			population.add(solution);
		}
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
	 * @return the numberOfGenerations
	 */
	public int getNumberOfGenerations() {
		return numberOfGenerations;
	}

	/**
	 * @param numberOfGenerations
	 *            the numberOfGenerations to set
	 */
	public void setNumberOfGenerations(int numberOfGenerations) {
		this.numberOfGenerations = numberOfGenerations;
	}

	/**
	 * @return the problem
	 */
	public Problem<Integer> getProblem() {
		return problem;
	}

	/**
	 * @param problem
	 *            the problem to set
	 */
	public void setProblem(Problem<Integer> problem) {
		this.problem = problem;
	}

	/**
	 * @return the archive
	 */
	public SolutionSet<Integer> getArchive() {
		return archive;
	}

	/**
	 * @param archive
	 *            the archive to set
	 */
	public void setArchive(SolutionSet<Integer> archive) {
		this.archive = archive;
	}

	/**
	 * @return the population
	 */
	public SolutionSet<Integer> getPopulation() {
		return population;
	}

	/**
	 * @param population
	 *            the population to set
	 */
	public void setPopulation(SolutionSet<Integer> population) {
		this.population = population;
	}

	/**
	 * @return the populationSize
	 */
	public int getPopulationSize() {
		return populationSize;
	}

	/**
	 * @param populationSize
	 *            the populationSize to set
	 */
	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}

	/**
	 * @return the union
	 */
	public SolutionSet<Integer> getUnion() {
		return union;
	}

	/**
	 * @param union
	 *            the union to set
	 */
	public void setUnion(SolutionSet<Integer> union) {
		this.union = union;
	}

	/**
	 * @return the i
	 */
	public int getI() {
		return i;
	}

	/**
	 * @param i
	 *            the i to set
	 */
	public void setI(int i) {
		this.i = i;
	}

	/**
	 * @return the generation
	 */
	public int getGeneration() {
		return generation;
	}

	/**
	 * @param generation
	 *            the generation to set
	 */
	public void setGeneration(int generation) {
		this.generation = generation;
	}

}
