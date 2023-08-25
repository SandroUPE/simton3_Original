package br.upe.jol.metaheuristics.nsgaii;

import java.text.NumberFormat;
import java.util.Collections;

import br.upe.jol.base.Algorithm;
import br.upe.jol.base.DistanceCalculator;
import br.upe.jol.base.Problem;
import br.upe.jol.base.RankUtil;
import br.upe.jol.base.Scheme;
import br.upe.jol.base.SchemeFrequency;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.base.Util;
import br.upe.jol.base.comparators.CrowdingComparator;
import br.upe.jol.base.comparators.DominanceComparator;
import br.upe.jol.base.comparators.IObserver;
import br.upe.jol.configuration.GeneralMOOParametersTO;
import br.upe.jol.operators.crossover.Crossover;
import br.upe.jol.operators.crossover.ICrossover;
import br.upe.jol.operators.mutation.IUniformMutation;
import br.upe.jol.operators.mutation.Mutation;
import br.upe.jol.operators.selection.BinaryTournament;
import br.upe.jol.operators.selection.Selection;
import br.upe.jol.problems.simton.SolutionONTD;

public class SNSGAII extends Algorithm<Integer> {
	private static final long serialVersionUID = 1L;
	
	private int maxIterations = 1000;
	
	private int iteration = 0;
	
	private int populationSize = 50;
		
	private SolutionSet<Integer> population;

	private double mutationProbability = 0.03;

	private double crossoverProbability = 1;
	
	protected static final double FREQ_MIN_ESQUEMA = 0.85;
	
	private String pathFiles = "C:/Temp/results/";
	
	private static NumberFormat nf = NumberFormat.getInstance();

	protected Scheme scheme;
	
	protected SchemeFrequency[] schemesFrequency;
	
	static{
		nf.setMaximumFractionDigits(4);
		nf.setMinimumFractionDigits(4);
	}
	
	@Override
	public String toString() {
		return "NSGA II";
	}
	
	public SNSGAII(){
		super();
	}
	
	public SNSGAII(int populationSize, int maxIterations, Problem<Integer> problem){
		this.populationSize = populationSize;
		this.population = new SolutionSet<Integer>();
		this.maxIterations = maxIterations;
		this.problem = problem;
		scheme = new Scheme(problem.getNumberOfVariables());
	}

	private void atualizarEsquemas() {
		double frequency;
		int maxFrequencyIndex;
		for (int i = 0; i < schemesFrequency.length; i++) {
			maxFrequencyIndex = -1;
			for (int g = 0; g < schemesFrequency[i].getListValues().size(); g++) {
				frequency = Collections.frequency(schemesFrequency[i].getListValues(), schemesFrequency[i].getListValues().get(g))/(1.0 * schemesFrequency[i].getListValues().size());
				if (frequency > FREQ_MIN_ESQUEMA){
					maxFrequencyIndex = g;
				}
			}
			if (maxFrequencyIndex != -1){
				scheme.getValor()[i] = schemesFrequency[i].getListValues().get(maxFrequencyIndex);	
			}
		}
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
		Crossover<Integer> crossover = new ICrossover(this.crossoverProbability);
		Mutation<Integer> mutation = new IUniformMutation(this.mutationProbability);
		BinaryTournament<Integer> selection = new BinaryTournament<Integer>(new DominanceComparator<Integer>());
		DistanceCalculator distance = DistanceCalculator.getInstance();
		RankUtil<Integer> ranking = RankUtil.getIntegerInstance();
		SolutionSet<Integer>[] rankedSolutions;
		schemesFrequency = new SchemeFrequency[problem.getNumberOfVariables()];
		for (int i = 0; i < schemesFrequency.length; i++) {
			schemesFrequency[i] = new SchemeFrequency();
		} 
		while (nextIteration()){
			for (int j = 0; j < schemesFrequency.length; j++) {
				schemesFrequency[j].getListValues().clear();
			} 
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
			System.out.println("Gravando resultados em " + pathFiles + "_snsgaii_pf_" + iteration + ".txt");
			population.printObjectivesToFile(pathFiles + "_NSGAII_PF_" + iteration + ".txt");
			System.out.println("Gravando resultados em " + pathFiles + "_snsgaii_var_" + iteration + ".txt");
			population.printVariablesToFile(pathFiles + "_NSGAII_VAR_" + iteration + ".txt");
//			updateObserver();
			iteration++;
		}
		return population;
	}

	private void generateOffspringPopulation(SolutionSet<Integer> offsprings,
			Crossover<Integer> crossover, Mutation<Integer> mutation, Selection<Integer> sel) {
		SolutionONTD offspring1;
		SolutionONTD offspring2;
		for (int i = 0; i < populationSize/2; i++){
			offspring1 = (SolutionONTD)sel.execute(population);
			offspring2 = (SolutionONTD)sel.execute(population);
			
			SolutionONTD[] vector =  (SolutionONTD[]) crossover.execute(scheme, offspring1, offspring2);
			
			offspring1 = vector[0];
			offspring2 = vector[1];
			
			mutation.execute(scheme, offspring1);
			mutation.execute(scheme, offspring2);
			
			problem.evaluateConstraints(offspring1);
			problem.evaluate(offspring1);
			problem.evaluateConstraints(offspring2);
			problem.evaluate(offspring2);
			
			offsprings.add(offspring1);
			offsprings.add(offspring2);
			for (int j = 0; j < problem.getNumberOfVariables(); j++){
				schemesFrequency[j].getListValues().add(String.valueOf((offspring1.getDecisionVariables()[j])));	
				schemesFrequency[j].getListValues().add(String.valueOf((offspring2.getDecisionVariables()[j])));	
			}
		}
		atualizarEsquemas();
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

	private synchronized void updateObserver() {
		if (observer != null){
			((IObserver)observer).setISolutionSet(population);
			observer.setIteration(iteration);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
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
