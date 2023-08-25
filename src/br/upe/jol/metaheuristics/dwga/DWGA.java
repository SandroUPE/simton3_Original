package br.upe.jol.metaheuristics.dwga;

import java.util.Arrays;

import br.upe.jol.base.Algorithm;
import br.upe.jol.base.DWAssignment;
import br.upe.jol.base.Operator;
import br.upe.jol.base.Problem;
import br.upe.jol.base.RankUtil;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.base.Util;
import br.upe.jol.base.comparators.DWComparator;
import br.upe.jol.base.comparators.IObserver;
import br.upe.jol.configuration.GeneralMOOParametersTO;
import br.upe.jol.operators.crossover.Crossover;
import br.upe.jol.operators.crossover.ICrossover;
import br.upe.jol.operators.mutation.IUniformMutation;
import br.upe.jol.operators.mutation.Mutation;
import br.upe.jol.operators.selection.BinaryTournament;
import br.upe.jol.operators.selection.Selection;
import br.upe.jol.problems.simton.SolutionONTD;

public class DWGA extends Algorithm<Integer> {
	private static final long serialVersionUID = 1L;
	
	private int maxIterations = 1000;
	
	private int iteration = 0;
	
	private int populationSize = 50;
		
	private SolutionSet<Integer> population;

	private double mutationProbability = 0.03;

	private double crossoverProbability = 1;
	
	private String pathFiles = "C:/Temp/results/";
	
	@Override
	public String toString() {
		return "MOVA";
	}
	
	public DWGA(){
		super();
	}
	
	public DWGA(int populationSize, int maxIterations, Problem<Integer> problem){
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
		Solution<Integer> solAux = new Solution<Integer>();
		population.getSolutionsList().addAll(ss.getSolutionsList());
		SolutionSet<Integer> offsprings = new SolutionSet<Integer>();
		SolutionSet<Integer> union = new SolutionSet<Integer>();
		Crossover<Integer> crossover = new ICrossover(this.crossoverProbability);
		Mutation<Integer> mutation = new IUniformMutation(this.mutationProbability);
		DWAssignment distance = DWAssignment.getInstance();
		DWComparator<Integer> dwComparator = new DWComparator<Integer>();
		BinaryTournament<Integer> selection = new BinaryTournament<Integer>(dwComparator);
		RankUtil<Integer> ranking = RankUtil.getIntegerInstance();
		while (nextIteration()){
			Util.LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>> DWGA, geração " + this.iteration + " <<<<<<<<<<<<<<<<<<<<<<<<<");
			offsprings.getSolutionsList().clear();
			generateOffspringPopulation(offsprings, crossover, mutation, selection);
			
			union.getSolutionsList().clear();
			union.getSolutionsList().addAll(population.getSolutionsList());
			union.getSolutionsList().addAll(offsprings.getSolutionsList());
			
			population.getSolutionsList().clear();
			distance.iHvAssignment(union);
			if (this.iteration > maxIterations - 2){
				union = ranking.getRankedSolutions(union)[0];
			}else{
				union.sort(dwComparator);
				
			}
			for (Solution<Integer> solution : union.getSolutionsList()){
				if (!solAux.equals(solution) && !Arrays.equals(solAux.getAllObjectives(), solution.getAllObjectives())){
					population.getSolutionsList().add(solution.clone());
				}
				if (population.size() >= populationSize){
					break;
				}
				solAux = solution.clone();
			}
			gravarResultados(this.iteration, crossover, mutation);
			this.iteration++;
			dwComparator.setPercentIterations((this.iteration*1.0)/(maxIterations));
		}
		return population;
	}

	/**
	 * Grava os resultados em arquivos
	 *
	 * @param iteration Número da iteração
	 */
	private void gravarResultados(int iteration, Operator<Integer> crossover, Operator<Integer> mutation) {
		System.out.println("Gravando resultados em " + pathFiles + "_dwga_" + populationSize + "_"+  nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_" + itf.format(iteration));
		population.printObjectivesToFile(pathFiles + "_dwga_" + crossover.getOpID() + "_" + mutation.getOpID() + "_" + populationSize + "_"
				+  nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_" + itf.format(iteration) + "_pf.txt");
		population.printVariablesToFile(pathFiles + "_dwga_" + crossover.getOpID() + "_" + mutation.getOpID() + "_" + populationSize + "_"
				+  nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_" + itf.format(iteration) + "_var.txt");
	}

	private void generateOffspringPopulation(SolutionSet<Integer> offsprings,
			Crossover<Integer> crossover, Mutation<Integer> mutation, Selection<Integer> sel) {
		SolutionONTD offspring1;
		SolutionONTD offspring2;
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
			
			offsprings.add(offspring1);
			offsprings.add(offspring2);
		}
	}

	private void initializePopulation() {
		Integer[] variables = null;
		Solution<Integer> solution = null;
		for (int i = 0; i < populationSize; i++){
			variables = new Integer[problem.getNumberOfVariables()];
			for (int j = 0; j < problem.getNumberOfVariables(); j++){
				variables[j] = (int) (Math.round(Math.random() 
						* (problem.getUpperLimit(j) - problem.getLowerLimit(j))) + problem.getLowerLimit(j));	
			}
			solution = new SolutionONTD(problem, variables);
			problem.evaluateConstraints(solution);
			problem.evaluate(solution);
			population.add(solution);
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
