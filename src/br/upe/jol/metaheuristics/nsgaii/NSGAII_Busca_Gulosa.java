/*
 * *****************************************************************************
 * Copyright (c) 2005
 * Propriedade do Laboratório de P&D da Unicap-Itautec
 * Todos os direitos reservados, com base nas leis brasileiras de 
 * copyright
 * Este software é confidencial e de propriedade intelectual do
 * Laboratório de P&D da Unicap-Itautec
 * ****************************************************************************
 * Projeto: SIAC - Sistema Itautec de Automação Comercial
 * Arquivo: NSGAII_Busca_Gulosa.java
 * ****************************************************************************
 * Histórico de revisões
 * CR		Nome				Data		Descrição
 * ****************************************************************************
 * 064813-P	Danilo Araújo		29/11/2010		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.metaheuristics.nsgaii;

import br.upe.jol.base.Algorithm;
import br.upe.jol.base.DistanceCalculator;
import br.upe.jol.base.MultithreadSolutionSet;
import br.upe.jol.base.Operator;
import br.upe.jol.base.Problem;
import br.upe.jol.base.RankUtil;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.base.Util;
import br.upe.jol.base.comparators.CrowdingComparator;
import br.upe.jol.base.comparators.DominanceComparator;
import br.upe.jol.base.comparators.IObserver;
import br.upe.jol.configuration.GeneralMOOParametersTO;
import br.upe.jol.operators.crossover.Crossover;
import br.upe.jol.operators.crossover.ICrossoverTopology;
import br.upe.jol.operators.mutation.IMixMutation;
import br.upe.jol.operators.mutation.Mutation;
import br.upe.jol.operators.selection.BinaryTournament;
import br.upe.jol.operators.selection.Selection;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * TODO Descrição do tipo
 * 
 * @author Danilo Araújo
 * @since 29/11/2010
 */
public class NSGAII_Busca_Gulosa  extends Algorithm<Integer> {
	private static final long serialVersionUID = 1L;
	
	private int maxIterations = 1000;
	
	private int iteration = 0;
	
	private int populationSize = 50;
		
	private SolutionSet<Integer> population;

	private double mutationProbability = 0.06;

	private double percentBuscaGulosa = 0.1;

	private double crossoverProbability = 1;
	
	private String pathFiles = "C:/Temp/results/";
	
	@Override
	public String toString() {
		return "NSGA II";
	}
	
	public NSGAII_Busca_Gulosa(){
		super();
	}
	
	public NSGAII_Busca_Gulosa(int populationSize, int maxIterations, Problem<Integer> problem){
		this.populationSize = populationSize;
		this.population = new SolutionSet<Integer>();
		this.maxIterations = maxIterations;
		this.problem = problem;
	}
	
	@Override
	public SolutionSet<Integer> execute() {
		initializePopulation();
		return execute(population, 0);
	}
	
	public SolutionSet<Integer> execute(SolutionSet<Integer> ss, int iteration) {
		this.iteration = iteration;
		population.getSolutionsList().addAll(ss.getSolutionsList());
		SolutionSet<Integer> offsprings = new SolutionSet<Integer>();
		SolutionSet<Integer> union = new SolutionSet<Integer>();
		int frontIndex = 0;
		Crossover<Integer> crossover = new ICrossoverTopology(this.crossoverProbability);
		Mutation<Integer> mutation = new IMixMutation(this.mutationProbability);
		BinaryTournament<Integer> selection = new BinaryTournament<Integer>(new DominanceComparator<Integer>());
		DistanceCalculator distance = DistanceCalculator.getInstance();
		RankUtil<Integer> ranking = RankUtil.getIntegerInstance();
		SolutionSet<Integer>[] rankedSolutions;
		while (nextIteration()){
			Util.LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>> NSGAII, geração " + iteration + " <<<<<<<<<<<<<<<<<<<<<<<<<");
			offsprings.getSolutionsList().clear();
			generateOffspringPopulation(offsprings, crossover, mutation, selection);
			
			union.getSolutionsList().clear();
			union.getSolutionsList().addAll(population.getSolutionsList());
			union.getSolutionsList().addAll(offsprings.getSolutionsList());
			
			rankedSolutions = ranking.getRankedSolutions(union);
			population.getSolutionsList().clear();
			frontIndex = 0;
			SolutionSet<Integer> front = null;
			while (population.size() < populationSize && frontIndex < rankedSolutions.length){
				front = rankedSolutions[frontIndex];
				distance.iCrowdingDistanceAssignment(front, problem.getNumberOfObjectives());
				front.sort(new CrowdingComparator());
				for (Solution<Integer> solution : front.getSolutionsList()){
					population.getSolutionsList().add(solution);
					if (population.size() >= populationSize){
						break;
					}
				}
				frontIndex++;
			}
			if (iteration % Util.GRAV_EXP_STEP == 0){
				gravarResultados(iteration, crossover, mutation);
			}
			iteration++;
		}
		return population;
	}

	/**
	 * Grava os resultados em arquivos
	 *
	 * @param iteration Número da iteração
	 */
	private void gravarResultados(int iteration, Operator<Integer> crossover, Operator<Integer> mutation) {
		System.out.println("Gravando resultados em " + pathFiles + "_nsgaii_" + populationSize + "_"+  nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_" + itf.format(iteration));
		population.printObjectivesToFile(pathFiles + "_nsgaii_" + crossover.getOpID() + "_" + mutation.getOpID() + "_" + populationSize + "_"
				+  nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_" + itf.format(iteration) + "_pf.txt");
		population.printVariablesToFile(pathFiles + "_nsgaii_" + crossover.getOpID() + "_" + mutation.getOpID() + "_" + populationSize + "_"
				+  nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_" + itf.format(iteration) + "_var.txt");
		population.printObjectivesToFile(pathFiles + "_nsgaii_" + crossover.getOpID() + "_" + mutation.getOpID() + "_" + populationSize + "_"
				+  nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_pf.txt");
		population.printVariablesToFile(pathFiles + "_nsgaii_" + crossover.getOpID() + "_" + mutation.getOpID() + "_" + populationSize + "_"
				+  nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_var.txt");
	}

	private void generateOffspringPopulation(SolutionSet<Integer> offsprings,
			Crossover<Integer> crossover, Mutation<Integer> mutation, Selection<Integer> sel) {
		SolutionONTD offspring1;
		SolutionONTD offspring2;
		
		SolutionONTD offspringBG1;
		SolutionONTD offspringBG2;
		for (int i = 0; i < populationSize/2; i++){
			offspring1 = (SolutionONTD)sel.execute(population);
			offspring2 = (SolutionONTD)sel.execute(population);
			
			SolutionONTD[] vector =  (SolutionONTD[]) crossover.execute(offspring1, offspring2);
			
			offspring1 = vector[0];
			offspring2 = vector[1];
			
			mutation.execute(offspring1);
			mutation.execute(offspring2);
			
			problem.evaluateConstraints(offspring1);
			problem.evaluate(offspring1);
			problem.evaluateConstraints(offspring2);
			problem.evaluate(offspring2);
			if (offsprings.size() > populationSize * (1 - percentBuscaGulosa)){
				offspringBG1 = offspring1.clone();
				offspringBG2 = offspring2.clone();
				int posW = offspringBG1.getDecisionVariables().length - 2;
				int posOXC = offspringBG1.getDecisionVariables().length - 1;
				int valor = (int)Math.round(Math.random() * (problem.getUpperLimit(posW) - offspringBG1.getDecisionVariables()[posW]) + offspringBG1.getDecisionVariables()[posW]);
				if (valor > problem.getUpperLimit(posW)){
					valor = (int)problem.getUpperLimit(posW);
				}else if (valor < problem.getLowerLimit(posW)){
					valor = (int)problem.getLowerLimit(posW);
				} 
				offspringBG1.setValue(posW, valor);
				valor = (int)Math.round(Math.random() * (problem.getUpperLimit(posOXC) - offspringBG1.getDecisionVariables()[posOXC]) + offspringBG1.getDecisionVariables()[posOXC]);
				if (valor > problem.getUpperLimit(posOXC)){
					valor = (int)problem.getUpperLimit(posOXC);
				}else if (valor < problem.getLowerLimit(posOXC)){
					valor = (int)problem.getLowerLimit(posOXC);
				} 
				offspringBG1.setValue(posOXC, valor);
				problem.evaluate(offspringBG1);
				if (!offspring1.dominates(offspringBG1)){
					offspring1 = offspringBG1;
				}
				valor = (int)Math.round(Math.random() * (problem.getUpperLimit(posW) - offspringBG2.getDecisionVariables()[posW]) + offspringBG2.getDecisionVariables()[posW]);
				if (valor > problem.getUpperLimit(posW)){
					valor = (int)problem.getUpperLimit(posW);
				}else if (valor < problem.getLowerLimit(posW)){
					valor = (int)problem.getLowerLimit(posW);
				} 
				valor = (int)Math.round(Math.random() * (problem.getUpperLimit(posOXC) - offspringBG2.getDecisionVariables()[posOXC]) + offspringBG2.getDecisionVariables()[posOXC]);
				if (valor > problem.getUpperLimit(posOXC)){
					valor = (int)problem.getUpperLimit(posOXC);
				}else if (valor < problem.getLowerLimit(posOXC)){
					valor = (int)problem.getLowerLimit(posOXC);
				} 
				offspringBG2.setValue(posOXC, valor);
				problem.evaluate(offspringBG2);
				if (!offspring2.dominates(offspringBG2)){
					offspring2 = offspringBG2;
				}
			}
			offsprings.add(offspring1);
			offsprings.add(offspring2);
			
		}
	}

	private void initializePopulation() {
		Integer[] variables = null;
		Solution<Integer> solution = null;
		MultithreadSolutionSet mss = new MultithreadSolutionSet(populationSize); 
		for (int i = 0; i < populationSize; i++){
			variables = new Integer[problem.getNumberOfVariables()];
			for (int j = 0; j < problem.getNumberOfVariables(); j++){
				if (Math.random() > 0.5){
					variables[j] = (int) (Math.round(Math.random() 
							* (problem.getUpperLimit(j) - problem.getLowerLimit(j))) + problem.getLowerLimit(j));
				}else{
					variables[j] = (int)problem.getLowerLimit(j);	
				}
			}
			solution = new SolutionONTD(problem, variables);
			mss.evaluate((SolutionONTD)solution);
		}
		SolutionSet<Integer> ss = mss.getSolutionSet();
		for (Solution<Integer> sol : ss.getSolutionsList()){
			population.add(sol);
		}
		if (observer != null){
			((IObserver)observer).setISolutionSet(population);
		}
	}

	private boolean nextIteration(){
		return iteration < maxIterations; 
	}

	@Override
	public void setParameters(GeneralMOOParametersTO parameters) {
		this.mutationProbability = parameters.getMutationProbability();
		this.crossoverProbability = parameters.getCrossoverProbability();
		this.populationSize = parameters.getPopulationSize();		
	}

	/**
	 * Método acessor para obter o valor de pathFiles
	 * @return O valor de pathFiles
	 */
	public String getPathFiles() {
		return pathFiles;
	}

	/**
	 * Método acessor para setar o valor de pathFiles
	 * @param pathFiles O valor de pathFiles
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
	 * @param maxIterations the maxIterations to set
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
	 * @param iteration the iteration to set
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
	 * @param populationSize the populationSize to set
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
	 * @param population the population to set
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
	 * @param mutationProbability the mutationProbability to set
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
	 * @param crossoverProbability the crossoverProbability to set
	 */
	public void setCrossoverProbability(double crossoverProbability) {
		this.crossoverProbability = crossoverProbability;
	}
}
