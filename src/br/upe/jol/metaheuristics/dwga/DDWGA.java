package br.upe.jol.metaheuristics.dwga;

import java.util.Collections;
import java.util.Vector;

import br.upe.jol.base.Algorithm;
import br.upe.jol.base.Operator;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.base.Util;
import br.upe.jol.base.comparators.FitnessComparator;
import br.upe.jol.configuration.GeneralMOOParametersTO;
import br.upe.jol.metaheuristics.spea2.SPEA2Solution;
import br.upe.jol.operators.crossover.Crossover;
import br.upe.jol.operators.crossover.OnePointCrossover;
import br.upe.jol.operators.mutation.Mutation;
import br.upe.jol.operators.mutation.UniformMutation;
import br.upe.jol.operators.selection.BinaryTournament;
import br.upe.jol.operators.selection.Selection;

public class DDWGA extends Algorithm<Double> {
	private static final long serialVersionUID = 1L;
	
	private int maxIterations = 1000;
	
	private int iteration = 0;
	
	private int populationSize = 50;
		
	private SolutionSet<Double> population;

	private double mutationProbability = 0.03;

	private double crossoverProbability = 1;
	
	private String pathFiles = "C:/Temp/results/";
	
	@Override
	public String toString() {
		return "MOVA";
	}
	
	public DDWGA(){
		super();
	}
	
	public DDWGA(int populationSize, int maxIterations, Problem<Double> problem){
		this.populationSize = populationSize;
		this.population = new SolutionSet<Double>();
		this.maxIterations = maxIterations;
		this.problem = problem;
	}
	
	@Override
	public SolutionSet<Double> execute() {
		initializePopulation();
		return execute(population, 0);
	}
	
	public SolutionSet<Double> execute(SolutionSet<Double> ss, int iteration) {
		this.iteration = iteration;
		Solution<Double> solAux = new Solution<Double>();
		population.getSolutionsList().addAll(ss.getSolutionsList());
		SolutionSet<Double> offsprings = new SolutionSet<Double>();
		SolutionSet<Double> union = new SolutionSet<Double>();
		Crossover<Double> crossover = new OnePointCrossover(this.crossoverProbability);
		Mutation<Double> mutation = new UniformMutation(this.mutationProbability);
		FitnessComparator<Double> comparator = new FitnessComparator<Double>();
		BinaryTournament<Double> selection = new BinaryTournament<Double>(comparator);

		Vector<Double> dist = new Vector<Double>();
		double c1 = 0;
		double c2 = 0;
		double a = 0;
		double b = 0;
		double c = 0;
		for (Solution<Double> solution : union.getSolutionsList()){
			c1 = 1 - solution.getObjective(0);
			c2 = 1 - solution.getObjective(1);
			a = Math.sqrt((c1 * c1 + c2 * c2));
			for (Solution<Double> solution1 : union.getSolutionsList()){
				if (!solution1.equals(solution)) {
					c1 = solution1.getObjective(0) - solution.getObjective(0);
					c2 = solution1.getObjective(1) - solution.getObjective(1);
					b = Math.sqrt((c1 * c1 + c2 * c2));
					c1 = 1 - solution1.getObjective(0);
					c2 = 1 - solution1.getObjective(1);
					c = Math.sqrt((c1 * c1 + c2 * c2));
					dist.add(getArea(a, b, c));
				}
			}	
			Collections.sort(dist);
			
			solution.setFitness(dist.get(0) + dist.get(1));
		}		
		
		while (nextIteration()){
			Util.LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>> DWGA, geração " + this.iteration + " <<<<<<<<<<<<<<<<<<<<<<<<<");
			offsprings.getSolutionsList().clear();
			generateOffspringPopulation(offsprings, crossover, mutation, selection);
			
			union.getSolutionsList().clear();
			union.getSolutionsList().addAll(population.getSolutionsList());
			union.getSolutionsList().addAll(offsprings.getSolutionsList());
			
			population.getSolutionsList().clear();
			
			dist = new Vector<Double>();
			for (Solution<Double> solution : union.getSolutionsList()){
				c1 = 1 - solution.getObjective(0);
				c2 = 1 - solution.getObjective(1);
				a = Math.sqrt((c1 * c1 + c2 * c2));
				for (Solution<Double> solution1 : union.getSolutionsList()){
					if (!solution1.equals(solution)) {
						c1 = solution1.getObjective(0) - solution.getObjective(0);
						c2 = solution1.getObjective(1) - solution.getObjective(1);
						b = Math.sqrt((c1 * c1 + c2 * c2));
						c1 = 1 - solution1.getObjective(0);
						c2 = 1 - solution1.getObjective(1);
						c = Math.sqrt((c1 * c1 + c2 * c2));
						dist.add(getArea(a, b, c));
					}
				}	
				Collections.sort(dist);
				
//				solution.setFitness(.01 * (1.0/(solution.getObjective(0) + solution.getObjective(1)) + a) + 
//						dist.get(0) + dist.get(1) + (dist.get(dist.size()-1) + dist.get(dist.size()-2))/50.0);
				
				solution.setFitness(dist.get(0) + dist.get(1));
			}			
			
			Collections.sort(union.getSolutionsList(), comparator);
			for (Solution<Double> solution : union.getSolutionsList()){
				population.getSolutionsList().add(solution.clone());
				if (population.size() >= populationSize){
					break;
				}
			}
			if (this.iteration % 10 == 0){
				gravarResultados(this.iteration, crossover, mutation);	
			}
			
			this.iteration++;
			
		}
		return population;
	}
	
	private double getArea(double a, double b, double c){
		double p = (a + b + c)/2.0;
		double area = Math.sqrt(p * (p  - a) * (p - b) * (p - c));
		return area;
	}

	/**
	 * Grava os resultados em arquivos
	 *
	 * @param iteration Número da iteração
	 */
	private void gravarResultados(int iteration, Operator<Double> crossover, Operator<Double> mutation) {
		System.out.println("Gravando resultados em " + pathFiles + "_dwga_" + populationSize + "_"+  nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_" + itf.format(iteration));
		population.printObjectivesToFile(pathFiles + "_dwga_" + crossover.getOpID() + "_" + mutation.getOpID() + "_" + populationSize + "_"
				+  nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_pf.txt");
		population.printVariablesToFile(pathFiles + "_dwga_" + crossover.getOpID() + "_" + mutation.getOpID() + "_" + populationSize + "_"
				+  nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_var.txt");
	}

	private void generateOffspringPopulation(SolutionSet<Double> offsprings,
			Crossover<Double> crossover, Mutation<Double> mutation, Selection<Double> sel) {
		Solution<Double> offspring1;
		Solution<Double> offspring2;
		for (int i = 0; i < populationSize; i++){
			offspring1 = (Solution<Double>)population.getRandom();
			offspring2 = (Solution<Double>)population.getRandom();
			
			Solution<Double>[] vector =  (Solution<Double>[]) crossover.execute(offspring1, offspring2);
			
			offspring1 = vector[0];
			offspring2 = vector[1];
			
			mutation.execute(offspring1);
			mutation.execute(offspring2);
			
			problem.evaluateConstraints(offspring1);
			problem.evaluate(offspring1);
			problem.evaluateConstraints(offspring2);
			problem.evaluate(offspring2);
			
			offsprings.add((Solution<Double>)sel.execute(offspring1, offspring2));
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
			solution = new SPEA2Solution(problem, variables);
			solution.evaluateObjectives();
			population.add(solution);
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
	public SolutionSet<Double> getPopulation() {
		return population;
	}

	/**
	 * @param population the population to set
	 */
	public void setPopulation(SolutionSet<Double> population) {
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
