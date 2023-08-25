/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: MOEA_Topology_Antigo.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	20/08/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.metaheuristics;

import br.upe.jol.base.Algorithm;
import br.upe.jol.base.DistanceCalculator;
import br.upe.jol.base.Operator;
import br.upe.jol.base.Problem;
import br.upe.jol.base.RankUtil;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.base.Util;
import br.upe.jol.base.comparators.CrowdingComparator;
import br.upe.jol.base.comparators.DominanceComparator;
import br.upe.jol.configuration.GeneralMOOParametersTO;
import br.upe.jol.operators.crossover.Crossover;
import br.upe.jol.operators.crossover.ICrossover;
import br.upe.jol.operators.initializers.RandomIsdaInitializer;
import br.upe.jol.operators.mutation.IUniformMutation;
import br.upe.jol.operators.mutation.Mutation;
import br.upe.jol.operators.selection.BinaryTournament;
import br.upe.jol.operators.selection.Selection;
import br.upe.jol.problems.simton.SimtonProblem;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo
 * @since 20/08/2014
 */
public class MOEA_Topology_Antigo extends Algorithm<Integer> {
	private static final long serialVersionUID = 1L;

	private int maxIterations = 10000;

	private int iteration = 0;

	private int populationSize = 50;

	private SolutionSet<Integer> population;

	private double mutationProbability = 0.06;

	private double crossoverProbability = 1.0;

	private String pathFiles = null;

	public static final Double[][] traffic = new Double[14][14];

	static {
		for (int i = 0; i < traffic.length; i++) {
			for (int j = 0; j < traffic.length; j++) {
				traffic[i][j] = 0.50;
			}
		}
	}

	private RandomIsdaInitializer iniIsda = new RandomIsdaInitializer();

	@Override
	public String toString() {
		return "topological";
	}

	public MOEA_Topology_Antigo() {
		super();
	}

	public MOEA_Topology_Antigo(int populationSize, int maxIterations, Problem<Integer> problem) {
		this.populationSize = populationSize;
		this.population = new SolutionSet<Integer>();
		this.maxIterations = maxIterations;
		this.problem = problem;
	}

	@Override
	public SolutionSet<Integer> execute() {
		population = iniIsda.execute(problem, populationSize);
		return execute(population, 0);
	}

	public SolutionSet<Integer> execute(SolutionSet<Integer> ss, int iterationIni) {
		this.iteration = iterationIni;
		population.getSolutionsList().addAll(ss.getSolutionsList());
		Crossover<Integer> crossover = new ICrossover(crossoverProbability);
		BinaryTournament<Integer> selection = new BinaryTournament<Integer>(new DominanceComparator<Integer>());
		Mutation<Integer> mutation = new IUniformMutation(this.mutationProbability);
		gravarResultados(0, mutation);
		SolutionSet<Integer> offsprings = new SolutionSet<Integer>();
		SolutionSet<Integer> union = new SolutionSet<Integer>();
		int frontIndex = 0;
		DistanceCalculator distance = DistanceCalculator.getInstance();
		RankUtil<Integer> ranking = RankUtil.getIntegerInstance();
		SolutionSet<Integer>[] rankedSolutions;
		((SimtonProblem) problem).setSimulate(true);
		while (nextIteration()) {
			Util.LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>> geração " + iteration + " <<<<<<<<<<<<<<<<<<<<<<<<<");
			offsprings.getSolutionsList().clear();
			generateOffspringPopulation(offsprings, crossover, mutation, selection);

			union.getSolutionsList().clear();
			union.getSolutionsList().addAll(population.getSolutionsList());
			union.getSolutionsList().addAll(offsprings.getSolutionsList());

			rankedSolutions = ranking.getRankedSolutions(union);
			population.getSolutionsList().clear();
			frontIndex = 0;
			SolutionSet<Integer> front = null;
			while (population.size() < populationSize && frontIndex < rankedSolutions.length) {
				front = rankedSolutions[frontIndex];
				distance.iCrowdingDistanceAssignment(front, problem.getNumberOfObjectives());
				front.sort(new CrowdingComparator());
				for (Solution<Integer> solution : front.getSolutionsList()) {
					// if (solution.getObjective(0) < 1 && ((iteration < 200) ||
					// solution.getObjective(0) < 0.1)) {
					if (solution.getObjective(0) < 1) {
						population.getSolutionsList().add(solution);
					}
					if (population.size() >= populationSize) {
						break;
					}
				}
				frontIndex++;
			}
			if (iteration % Util.GRAV_EXP_STEP == 0) {
				gravarResultados(iteration, mutation);
			}
			iteration++;
		}
		return population;
	}

	/**
	 * Grava os resultados em arquivos
	 * 
	 * @param iteration
	 *            Número da iteração
	 */
	private void gravarResultados(int iteration, Operator<Integer> mutation) {
		System.out.println("Gravando resultados em " + pathFiles + "_top_" + populationSize + "_"
				+ nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_" + itf.format(iteration));
		population.printObjectivesToFile(pathFiles + "_top_" + mutation.getOpID() + "_" + populationSize + "_"
				+ nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_" + itf.format(iteration)
				+ "_pf.txt");
		population.printVariablesToFile(pathFiles + "_top_" + mutation.getOpID() + "_" + populationSize + "_"
				+ nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_" + itf.format(iteration)
				+ "_var.txt");
		population.printObjectivesToFile(pathFiles + "_top_" + mutation.getOpID() + "_" + populationSize + "_"
				+ nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_pf.txt");
		population.printVariablesToFile(pathFiles + "_top_" + mutation.getOpID() + "_" + populationSize + "_"
				+ nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_var.txt");
	}

	private void generateOffspringPopulation(SolutionSet<Integer> offsprings, Crossover<Integer> crossover,
			Mutation<Integer> mutation, Selection<Integer> sel) {
		SolutionONTD offspring1;
		SolutionONTD offspring2;
		for (int i = 0; i < populationSize / 2; i++) {
			offspring1 = (SolutionONTD) sel.execute(population);
			offspring2 = (SolutionONTD) sel.execute(population);

			SolutionONTD[] vector = (SolutionONTD[]) crossover.execute(offspring1, offspring2);

			offspring1 = vector[0];
			offspring2 = vector[1];

			offspring1 = (SolutionONTD) mutation.execute(offspring1);
			offspring2 = (SolutionONTD) mutation.execute(offspring2);

			problem.evaluate(offspring1);
			problem.evaluate(offspring2);

			offsprings.add(offspring1);
			offsprings.add(offspring2);
		}
	}

	private boolean nextIteration() {
		return iteration <= maxIterations;
	}

	@Override
	public void setParameters(GeneralMOOParametersTO parameters) {
		this.mutationProbability = parameters.getMutationProbability();
		this.crossoverProbability = parameters.getCrossoverProbability();
		this.populationSize = parameters.getPopulationSize();
	}

	/**
	 * Método acessor para obter o valor de pathFiles
	 * 
	 * @return O valor de pathFiles
	 */
	public String getPathFiles() {
		return pathFiles;
	}

	/**
	 * Método acessor para setar o valor de pathFiles
	 * 
	 * @param pathFiles
	 *            O valor de pathFiles
	 */
	public void setPathFiles(String pathFiles) {
		this.pathFiles = pathFiles;
	}

	/**
	 * @return the maxIterations
	 */
	public int getMaxIterations() {
		return maxIterations;
	}

	/**
	 * @param maxIterations
	 *            the maxIterations to set
	 */
	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	/**
	 * @return the iteration
	 */
	public int getIteration() {
		return iteration;
	}

	/**
	 * @param iteration
	 *            the iteration to set
	 */
	public void setIteration(int iteration) {
		this.iteration = iteration;
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
	 * @return the mutationProbability
	 */
	public double getMutationProbability() {
		return mutationProbability;
	}

	/**
	 * @param mutationProbability
	 *            the mutationProbability to set
	 */
	public void setMutationProbability(double mutationProbability) {
		this.mutationProbability = mutationProbability;
	}

	/**
	 * @return the crossoverProbability
	 */
	public double getCrossoverProbability() {
		return crossoverProbability;
	}

	/**
	 * @param crossoverProbability
	 *            the crossoverProbability to set
	 */
	public void setCrossoverProbability(double crossoverProbability) {
		this.crossoverProbability = crossoverProbability;
	}
}
