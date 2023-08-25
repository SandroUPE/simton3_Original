/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: ISPEA2Mutation.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	17/10/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.metaheuristics.spea2;

import java.util.ArrayList;

import br.cns.metrics.AlgebraicConnectivity;
import br.cns.models.BarabasiDensity;
import br.cns.models.Toroid;
import br.cns.models.WattsStrogatzDensity;
import br.upe.jol.base.Algorithm;
import br.upe.jol.base.MultithreadSolutionSet;
import br.upe.jol.base.Operator;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.base.Spea2Fitness;
import br.upe.jol.base.Util;
import br.upe.jol.base.comparators.DominanceComparator;
import br.upe.jol.base.comparators.IObserver;
import br.upe.jol.configuration.ArchiveMOOParametersTO;
import br.upe.jol.configuration.GeneralMOOParametersTO;
import br.upe.jol.operators.mutation.BarabasiAlbertMutation;
import br.upe.jol.operators.mutation.Mutation;
import br.upe.jol.operators.selection.BinaryTournament;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo
 * @since 17/10/2013
 */
public class ISPEA2Mutation extends Algorithm<Integer> {
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

	public ISPEA2Mutation(int populationSize, int archiveSize, int numberOfGenerations, Problem<Integer> problem) {
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
		// initialization
		archive.getSolutionsList().addAll(ss.getSolutionsList());
		generation = lastGeneration;
		Mutation mutation = new BarabasiAlbertMutation(this.mutationProbability);
		// Mutation mutation = new
		// WattsStrogatzMutation(this.mutationProbability);
		BinaryTournament<Integer> tournament = new BinaryTournament<Integer>(comparator);
		SolutionSet<Integer> offSpringSolutionSet;
		while (generation <= this.numberOfGenerations) {
			Util.LOGGER.info(">>>>>>>>>>>>>>>>>>> SPEA2, geração " + generation + " <<<<<<<<<<<<<<<<<<<<<<<");
			this.union = this.population.union(archive);

			Spea2Fitness spea = new Spea2Fitness(union);
			spea.fitnessAssign();
			archive = spea.environmentalSelection(archiveSize);
			System.out.println("EA = " + archive.size());
			// Create a new offspringPopulation
			offSpringSolutionSet = new SolutionSet(populationSize);
			Solution[] parents = new Solution[2];
			MultithreadSolutionSet mss = new MultithreadSolutionSet(populationSize);
			int qtdeAdded = 0;
			while (qtdeAdded < populationSize) {
				int j = 0;
				do {
					j++;
					parents[0] = (Solution) tournament.execute(archive);
				} while (j < TOURNAMENTS_ROUNDS); // do-while

				// make the crossover
				ISPEA2Solution offSpring = new ISPEA2Solution(parents[0].getProblem(),
						(Integer[]) parents[0].getDecisionVariables());
				mutation.execute(offSpring);
				mss.evaluate((SolutionONTD) offSpring);
				if ((generation < 0.1 * this.numberOfGenerations) || (offSpring.getObjective(0) < 0.1)) {
					if (offSpring.getNumberOfViolatedConstraint() == 0) {
						qtdeAdded++;
					}
				}
			} // while
			SolutionSet<Integer> ssAux = mss.getSolutionSet();
			for (Solution<Integer> sol : ssAux.getSolutionsList()) {
				offSpringSolutionSet.add(sol);
			}
			generation++;
			if (generation % Util.GRAV_EXP_STEP == 0) {
				gravarResultados(generation, mutation);
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
	private void gravarResultados(int iteration, Operator<Integer> mutation) {
		System.out.println("Gravando resultados em " + pathFiles + "_spea2_" + populationSize + "_"
				+ nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_" + itf.format(iteration));
		archive.printObjectivesToFile(pathFiles + "_spea2_" + populationSize + "_" + archive.getCapacity() + "_"
				+ nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_" + itf.format(iteration)
				+ "_pf.txt");
		archive.printVariablesToFile(pathFiles + "_spea2_" + populationSize + "_" + archive.getCapacity() + "_"
				+ nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_" + itf.format(iteration)
				+ "_var.txt");
	}

	@Override
	public SolutionSet<Integer> execute() {
		initializePopulationBA();
		return execute(population, 1);
	}

	private static final WattsStrogatzDensity gp = new WattsStrogatzDensity(0.24, 1, 0.2, true);

	private static final BarabasiDensity ba = new BarabasiDensity(0.2, 0.8);

	private static final Toroid toroid = new Toroid(0.2, 14);

	private void initializePopulationWS() {
		Integer[] variables = null;
		Solution<Integer> solution = null;
		MultithreadSolutionSet mss = new MultithreadSolutionSet(populationSize);
		Integer[][] adjacencyMatrix = new Integer[14][14];
		for (int i = 0; i < populationSize; i++) {
			gp.setD(Math.random() * 0.38 + 0.02);
			if (gp.getD() > 4.0 / 13) {
				gp.setK(2);
			} else {
				gp.setK(1);
			}
			adjacencyMatrix = gp.transform(adjacencyMatrix);
			variables = new Integer[problem.getNumberOfVariables()];
			int index = 0;
			for (int j = 0; j < adjacencyMatrix.length; j++) {
				for (int k = j + 1; k < adjacencyMatrix.length; k++) {
					variables[index] = adjacencyMatrix[j][k] * 3;
					index++;
				}
			}
			variables[91] = (int) (Math.round(Math.random() * (problem.getUpperLimit(91) - problem.getLowerLimit(91))) + problem
					.getLowerLimit(91));
			variables[92] = (int) (Math.round(Math.random() * (problem.getUpperLimit(92) - problem.getLowerLimit(92))) + problem
					.getLowerLimit(92));
			solution = new ISPEA2Solution(problem, variables);
			mss.evaluate((SolutionONTD) solution);
		}
		SolutionSet<Integer> ss = mss.getSolutionSet();
		for (Solution<Integer> sol : ss.getSolutionsList()) {
			population.add(sol);
		}
		if (observer != null) {
			((IObserver) observer).setISolutionSet(population);
		}
	}

	private void initializePopulationBA() {
		int numNodes = 14;
		Integer[] variables = null;
		Solution<Integer> solution = null;
		MultithreadSolutionSet mss = new MultithreadSolutionSet(populationSize);
		Integer[][] adjacencyMatrix = new Integer[numNodes][numNodes];
		double minDensity = 2.0 / (numNodes - 1);
		double maxDensity = 0.40;
		for (int i = 0; i < populationSize; i++) {
			ba.setDensity(Math.random() * (maxDensity - minDensity) + minDensity);
			adjacencyMatrix = ba.transform(adjacencyMatrix);
			// while
			// (AlgebraicConnectivity.getInstance().calculate(adjacencyMatrix)
			// <= 0) {
			// adjacencyMatrix = ba.transform(adjacencyMatrix);
			// }
			variables = new Integer[problem.getNumberOfVariables()];
			int index = 0;
			for (int j = 0; j < adjacencyMatrix.length; j++) {
				for (int k = j + 1; k < adjacencyMatrix.length; k++) {
					variables[index] = adjacencyMatrix[j][k] * 3;
					index++;
				}
			}
			variables[91] = (int) (Math.round(Math.random() * (problem.getUpperLimit(91) - problem.getLowerLimit(91))) + problem
					.getLowerLimit(91));
			variables[92] = (int) (Math.round(Math.random() * (problem.getUpperLimit(92) - problem.getLowerLimit(92))) + problem
					.getLowerLimit(92));
			solution = new ISPEA2Solution(problem, variables);
			mss.evaluate((SolutionONTD) solution);
		}
		SolutionSet<Integer> ss = mss.getSolutionSet();
		for (Solution<Integer> sol : ss.getSolutionsList()) {
			population.add(sol);
		}
		if (observer != null) {
			((IObserver) observer).setISolutionSet(population);
		}
	}

	private void initializePopulationToroid() {
		int numNodes = 14;
		Integer[] variables = null;
		Solution<Integer> solution = null;
		MultithreadSolutionSet mss = new MultithreadSolutionSet(populationSize);
		Integer[][] adjacencyMatrix = new Integer[numNodes][numNodes];
		double minDensity = 2.0 / (numNodes - 1);
		double maxDensity = 0.40;
		for (int i = 0; i < populationSize; i++) {
			toroid.changeDensity(Math.random() * (maxDensity - minDensity) + minDensity);
			adjacencyMatrix = ba.transform(adjacencyMatrix);
			while (AlgebraicConnectivity.getInstance().calculate(adjacencyMatrix) <= 0) {
				adjacencyMatrix = ba.transform(adjacencyMatrix);
			}
			variables = new Integer[problem.getNumberOfVariables()];
			int index = 0;
			for (int j = 0; j < adjacencyMatrix.length; j++) {
				for (int k = j + 1; k < adjacencyMatrix.length; k++) {
					variables[index] = adjacencyMatrix[j][k] * 3;
					index++;
				}
			}
			variables[91] = (int) (Math.round(Math.random() * (problem.getUpperLimit(91) - problem.getLowerLimit(91))) + problem
					.getLowerLimit(91));
			variables[92] = (int) (Math.round(Math.random() * (problem.getUpperLimit(92) - problem.getLowerLimit(92))) + problem
					.getLowerLimit(92));
			solution = new ISPEA2Solution(problem, variables);
			mss.evaluate((SolutionONTD) solution);
		}
		SolutionSet<Integer> ss = mss.getSolutionSet();
		for (Solution<Integer> sol : ss.getSolutionsList()) {
			population.add(sol);
		}
		if (observer != null) {
			((IObserver) observer).setISolutionSet(population);
		}
	}

	private void initializePopulationRnd() {
		Integer[] variables = null;
		Solution<Integer> solution = null;
		MultithreadSolutionSet mss = new MultithreadSolutionSet(populationSize);
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
			mss.evaluate((SolutionONTD) solution);
		}
		SolutionSet<Integer> ss = mss.getSolutionSet();
		for (Solution<Integer> sol : ss.getSolutionsList()) {
			population.add(sol);
		}
		if (observer != null) {
			((IObserver) observer).setISolutionSet(population);
		}
	}

	private void environmentalSelection(int countNonDominated) {
		this.archive.clear();

		if (countNonDominated == this.archive.getCapacity()) {
			for (int i = 0; i < this.union.size(); i++) {
				if (this.union.get(i).getFitness() < 1)
					this.archive.add(union.get(i));

				if (this.archive.size() == this.archive.getCapacity())
					break;
			}
		} else if (countNonDominated < this.archive.getCapacity()) {
			this.union.sort(comparator);

			for (int i = 0; i < this.union.size(); i++) {
				this.archive.add(union.get(i));

				if (this.archive.size() == this.archive.getCapacity())
					break;
			}
			// truncation
		} else {
			int count = countNonDominated - this.archive.getCapacity();
			SolutionSet<Integer> aux = new SolutionSet<Integer>(countNonDominated);
			ArrayList<Solution<Integer>> list = new ArrayList<Solution<Integer>>();

			for (int i = 0; i < this.union.size(); i++) {
				Solution<Integer> solution = this.union.get(i);
				if (solution.getFitness() < 1) {
					list.add(solution);
				}
			}
			aux.setSolutionsList(list);

			while (count > 0) {
				int forRemoval = this.getForRemoval(aux);
				aux.remove(forRemoval);
				count--;
			}

			for (int i = 0; i < this.union.size(); i++) {
				ISPEA2Solution solution = (ISPEA2Solution) aux.get(i);
				this.archive.add(solution);
				if (this.archive.size() == this.archive.getCapacity())
					break;
			}
		}

	}

	private int getForRemoval(SolutionSet<Integer> solutionSet) {
		double[][] distances = solutionSet.writeDistancesToMatrix();
		double minDistance = distances[0][0];
		int forRemoval = 0;

		for (int i = 1; i < distances[0].length; i++) {
			if (distances[i][0] < minDistance) {
				minDistance = distances[i][0];
				forRemoval = i;
			} else if (distances[i][0] == minDistance) {
				int j = 0;
				while (j < distances[i].length - 1 && distances[i][j] == distances[forRemoval][j]) {
					j++;
				}

				if (distances[i][j] < distances[forRemoval][j]) {
					forRemoval = i;
				}
			}

		}

		return forRemoval;
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
