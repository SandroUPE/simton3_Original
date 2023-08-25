package br.upe.jol.metaheuristics.nsgaii;

import static br.upe.jol.base.Util.LOGGER;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

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
import br.upe.jol.base.comparators.IObserver;
import br.upe.jol.configuration.GeneralMOOParametersTO;
import br.upe.jol.operators.crossover.Crossover;
import br.upe.jol.operators.crossover.ONTDCrossover;
import br.upe.jol.operators.mutation.IFashionMutation;
import br.upe.jol.operators.mutation.Mutation;
import br.upe.jol.operators.selection.BinaryTournament;
import br.upe.jol.operators.selection.Selection;
import br.upe.jol.problems.simon.entity.BlockUtil;
import br.upe.jol.problems.simton.RegSimonProblem;
import br.upe.jol.problems.simton.SolutionONTD;

public class INSGAII_trunk extends Algorithm<Integer> {
	private static final long serialVersionUID = 1L;
	
	private int maxIterations = 2000;
	
	private int iteration = 0;
	
	private int populationSize = 50;
		
	private SolutionSet<Integer> population;

	private double mutationProbability = 0.4;

	private double crossoverProbability = 1;
	
	private String pathFiles = ".";
	
	private BlockUtil block;
	
	@Override
	public String toString() {
		return "NSGA II";
	}
	
	public INSGAII_trunk(){
		super();
	}
	
	public INSGAII_trunk(int populationSize, int maxIterations, Problem<Integer> problem){
		this.populationSize = populationSize;
		this.population = new SolutionSet<Integer>();
		this.maxIterations = maxIterations;
		this.problem = problem;
		block = BlockUtil.getInstance();
	}
	
	@Override
	public SolutionSet<Integer> execute() {
		initializePopulation();
		return execute(population, 0);
	}
	
	public SolutionSet<Integer> execute(SolutionSet<Integer> ss, int iterationIni) {
		this.iteration = iterationIni;
		population.getSolutionsList().addAll(ss.getSolutionsList());
		SolutionSet<Integer> offsprings = new SolutionSet<Integer>();
		SolutionSet<Integer> union = new SolutionSet<Integer>();
		int frontIndex = 0;
		Crossover<Integer> crossover = new ONTDCrossover(this.crossoverProbability);
		Mutation<Integer> mutation = new IFashionMutation(this.mutationProbability);
		BinaryTournament<Integer> selection = new BinaryTournament<Integer>(new DominanceComparator<Integer>());
		DistanceCalculator distance = DistanceCalculator.getInstance();
		RankUtil<Integer> ranking = RankUtil.getIntegerInstance();
		SolutionSet<Integer>[] rankedSolutions;
		while (nextIteration()){
			Util.LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>> NSGAII, geraï¿½ï¿½o " + iteration + " <<<<<<<<<<<<<<<<<<<<<<<<<");
			offsprings.getSolutionsList().clear();
			generateOffspringPopulation(offsprings, crossover, mutation, selection);
			
			union.getSolutionsList().clear();
			union.getSolutionsList().addAll(population.getSolutionsList());
			union.getSolutionsList().addAll(offsprings.getSolutionsList());
			
			rankedSolutions = ranking.getRankedSolutions(union);
			population.getSolutionsList().clear();
			frontIndex = 0;
			SolutionSet<Integer> front = null;
			ArrayList<Solution> temp = new ArrayList<Solution>();
			
			while (population.size() < populationSize && frontIndex < rankedSolutions.length){
				front = rankedSolutions[frontIndex];
				distance.iCrowdingDistanceAssignment(front, problem.getNumberOfObjectives());
				front.sort(new CrowdingComparator());
				for (Solution<Integer> solution : front.getSolutionsList()){
					//if ((iteration < 0.1 * maxIterations || solution.getObjective(0) < 0.1)){
						population.getSolutionsList().add(solution);
					/*}else if(solution.getObjective(0) >= 0.1){
						temp.add(solution);
					}*/
					if (population.size() >= populationSize){
						break;
					}
				}
				frontIndex++;
			}
			
			/*usado para evitar população pequena
			int index = 0;
			while(population.size() < this.populationSize){
				population.getSolutionsList().add(temp.get(index));
				index++;
			}*/
			
			gravarResultados(iteration, crossover, mutation);
//			updateObserver();
			iteration++;
		}
		return population;
	}

	/**
	 * Grava os resultados em arquivos
	 *
	 * @param iteration Nï¿½mero da iteraï¿½ï¿½o
	 */
	private void gravarResultados(int iteration, Operator<Integer> crossover, Operator<Integer> mutation) {
		System.out.println("Gravando resultados em " + pathFiles + "_nsgaii_" + populationSize + "_"+  nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_" + itf.format(iteration));
		population.printObjectivesToFile(pathFiles + "_nsgaii_" + crossover.getOpID() + "_" + mutation.getOpID() + "_" + populationSize + "_"
				+  nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_" + itf.format(iteration) + "_pf.txt");
		population.printVariablesToFile(pathFiles + "_nsgaii_" + crossover.getOpID() + "_" + mutation.getOpID() + "_" + populationSize + "_"
				+  nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_" + itf.format(iteration) + "_var.txt");
		/*population.printObjectivesToFile(pathFiles + "_nsgaii_" + crossover.getOpID() + "_" + mutation.getOpID() + "_" + populationSize + "_"
				+  nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_pf.txt");
		population.printVariablesToFile(pathFiles + "_nsgaii_" + crossover.getOpID() + "_" + mutation.getOpID() + "_" + populationSize + "_"
				+  nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_var.txt");*/
		block.printBlocks(iteration);
		this.printTopologies(pathFiles + "_nsgaii_" + crossover.getOpID() + "_" + mutation.getOpID() + "_" + populationSize + "_"
				+  nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_" + itf.format(iteration) + "_topologies.txt");
		
		population.printMetrics(pathFiles, iteration);
	}

	
	private void printTopologies(String path){
		try {
			FileOutputStream fos = new FileOutputStream(path);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			BufferedWriter bw = new BufferedWriter(osw);
			
			for (int i = 0; i < this.population.size(); i++) {
				Solution<Integer> solution = population.get(i);
				bw.write(solution.toString());
				bw.write("\t W = " + solution.getDecisionVariables()[solution.getDecisionVariables().length-1]);
				bw.write(" E = " + solution.getDecisionVariables()[solution.getDecisionVariables().length-2]);
				bw.newLine();
				bw.write( this.problem.drawTopologies(population.get(i)));
				bw.newLine();
				bw.write("***************************************************************");
				bw.write("***************************************************************");
				bw.newLine();
			}

			bw.close();
		} catch (IOException e) {
			LOGGER.severe("Error acceding to the file");
			e.printStackTrace();
		}
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
			offsprings.add(offspring1);
			problem.evaluateConstraints(offspring2);
			problem.evaluate(offspring2);
			offsprings.add(offspring2);
		}
	}

	private void initializePopulation() {
		Integer[] variables = null;
		Solution<Integer> solution = null;
		for (int i = 0; i < populationSize; i++){
			variables = new Integer[problem.getNumberOfVariables()];
			boolean[] permission = problem.getPermission();
			int[] staticVar = problem.getStaticVariables();
			
			//Calculando o valor para lambda
			int posicaoLambda = problem.getNumberOfVariables()-1;
			if(permission[posicaoLambda] && staticVar[posicaoLambda] == 0){
				variables[posicaoLambda] = (int) (Math.round(Math.random()
						* (problem.getUpperLimit(posicaoLambda) - problem
								.getLowerLimit(posicaoLambda))) + problem
						.getLowerLimit(posicaoLambda));
			}
			
			//Calculando o valor para epislon
			int posicaoEpislon = problem.getNumberOfVariables()-2;
			if(permission[posicaoEpislon] && staticVar[posicaoEpislon] == 0){
				variables[posicaoEpislon] = (int) (Math.round(Math.random()
						* (problem.getUpperLimit(posicaoEpislon) - problem
								.getLowerLimit(posicaoEpislon))) + problem
						.getLowerLimit(posicaoEpislon));
			}
			
			int numberOfNodes = ((RegSimonProblem)problem).NUMBER_OF_NODES;
			
			int inicioReg = (numberOfNodes*(numberOfNodes-1))/2;
			
			//Calculando os links
			for (int j = 0; j < inicioReg; j++){
				//se for um link possivel e for um link q ainda n existe
				if (permission[j] && staticVar[j] == 0) {
					if (Math.random() > 0.5) {
						variables[j] = (int) (Math.round(Math.random()
								* (problem.getUpperLimit(j) - problem
										.getLowerLimit(j))) + problem
								.getLowerLimit(j));
					} else {
						variables[j] = (int) problem.getLowerLimit(j);
					}
				//se o link ja existe se mantem
				}else{
					variables[j] = staticVar[j];
				}
			}
			
			
			//Calculando a quantidade de regeneradores
			int[] maxReg = ((RegSimonProblem)problem).numberMaxOfRegenerator(variables);
			
			for (int j = inicioReg; j < posicaoEpislon; j++){
				//se for um link possivel e for um link q ainda n existe
				if (Math.random() > 0.5) {
					variables[j] = (int) (Math.round(Math.random() * (maxReg[j - inicioReg] - 1)) + 1);
				} else {
					variables[j] = 0;
				}
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
	 * Mï¿½todo acessor para obter o valor de pathFiles
	 * @return O valor de pathFiles
	 */
	public String getPathFiles() {
		return pathFiles;
	}

	/**
	 * Mï¿½todo acessor para setar o valor de pathFiles
	 * @param pathFiles O valor de pathFiles
	 */
	public void setPathFiles(String pathFiles) {
		this.pathFiles = pathFiles;
		block.path = pathFiles;
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
