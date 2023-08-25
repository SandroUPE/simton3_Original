package br.upe.jol.metaheuristics.pesaii;

import br.upe.jol.base.Algorithm;
import br.upe.jol.base.ArchiveGrid;
import br.upe.jol.base.Operator;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.base.Util;
import br.upe.jol.base.comparators.IObserver;
import br.upe.jol.configuration.GeneralMOOParametersTO;
import br.upe.jol.configuration.HyperBoxMOOParametersTO;
import br.upe.jol.operators.crossover.Crossover;
import br.upe.jol.operators.crossover.ICrossover;
import br.upe.jol.operators.mutation.IUniformMutation;
import br.upe.jol.operators.mutation.Mutation;
import br.upe.jol.operators.selection.EPESAIISelection;
import br.upe.jol.problems.simton.SolutionONTD;

public class IPesaII extends Algorithm<Integer> {
	private static final long serialVersionUID = 1L;

	private int maxIterations = 10000;

	private int iteration = 0;

	private int populationSize = 50;

	private int archiveMaxSize = 50;

	private SolutionSet<Integer> population;

	private ArchiveGrid<Integer> archive;

	private int numberOfDivisions = 5;

	private double crossoverProbability = 1;

	private double mutationProbability = 0.03;
	
	private String pathFiles = "C:/temp/results/ontd";

	@Override
	public String toString() {
		return "PESA II";
	}

	public IPesaII(int populationSize, int archiveSize, int maxIterations,
			Problem<Integer> problem) {
		this.populationSize = populationSize;
		this.maxIterations = maxIterations;
		this.problem = problem;
		this.population = new SolutionSet<Integer>();
		this.archiveMaxSize = archiveSize;
	}

	@Override
	public SolutionSet<Integer> execute() {
		this.initializePopulation();
		return this.execute(this.population, 1);
	}

	public SolutionSet<Integer> execute(SolutionSet<Integer> ss,
			int lastGeneration) {
		this.population = ss;
		this.archive = new ArchiveGrid<Integer>(this.archiveMaxSize,
				this.numberOfDivisions, this.problem.getNumberOfObjectives());

		Crossover crossover = new ICrossover(this.crossoverProbability);
		EPESAIISelection<Integer> selection = new EPESAIISelection<Integer>();
		Mutation mutation = new IUniformMutation(mutationProbability);

		this.iteration = lastGeneration;

		while (this.nextIteration()) {
			this.updateArchive();

			this.population.clear();

			SolutionONTD[] parents = new SolutionONTD[2];
			do {
				parents[0] = (SolutionONTD) selection.execute(archive);
				parents[1] = (SolutionONTD) selection.execute(archive);

				SolutionONTD[] result = (SolutionONTD[]) crossover
						.execute(parents);
				SolutionONTD offspring = result[0];

				offspring = (SolutionONTD) mutation.execute(offspring);
				problem.evaluateConstraints(offspring);
				problem.evaluate(offspring);
//				if ((this.iteration < 0.1 * maxIterations) || (offspring.getObjective(0) < 0.01)){
					this.population.add(offspring);
//				}

			} while (this.population.size() < this.populationSize);

			this.iteration++;
			if (iteration % Util.GRAV_EXP_STEP == 0){
				gravarResultados(iteration, crossover, mutation);
			}
		}

		return this.archive;
	}

	/**
	 * Grava os resultados em arquivos
	 *
	 * @param iteration Número da iteração
	 */
	private void gravarResultados(int iteration, Operator<Integer> crossover, Operator<Integer> mutation) {
		System.out.println("Gravando resultados em " + pathFiles + "_pesaii_" + populationSize + "_"+  nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_" + itf.format(iteration));
		archive.printObjectivesToFile(pathFiles + "_pesaii_" + numberOfDivisions + "_" + crossover.getOpID() + "_" + mutation.getOpID() + "_" + populationSize + "_"
				+  nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_"+iteration + "_pf.txt");
		archive.printVariablesToFile(pathFiles + "_pesaii_" + numberOfDivisions + "_" + crossover.getOpID() + "_" + mutation.getOpID() + "_" + populationSize + "_"
				+  nf.format(crossoverProbability) + "_" + nf.format(mutationProbability) + "_"+iteration+"_var.txt");
	}

	private void updateArchive() {
		for (int i = 0; i < this.populationSize; i++) {
			this.archive.add(population.get(i));
		}
	}

	private void initializePopulation() {
		Integer[] variables = null;
		Solution<Integer> solution = null;
		for (int i = 0; i < populationSize; i++) {
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
			problem.evaluateConstraints(solution);
			problem.evaluate(solution);
			population.add(solution);
		}
		if (observer != null) {
			((IObserver) observer).setISolutionSet(population);
		}
	}

	// private synchronized void updateObserver() {
	// if(observer == null){
	// observer = new Observer();
	// observer.setSolutionSet(archive);
	// }else{
	// observer.setIteration(iteration);
	// observer.setSolutionSet(archive);
	// try {
	// Thread.sleep(10);
	// } catch (InterruptedException e) {
	// }
	// }
	// }

	private boolean nextIteration() {
		return iteration <= maxIterations;
	}

	public double getCrossoverProbability() {
		return crossoverProbability;
	}

	public void setCrossoverProbability(double crossoverProbability) {
		this.crossoverProbability = crossoverProbability;
	}

	public double getMutationProbability() {
		return mutationProbability;
	}

	public void setMutationProbability(double mutationProbability) {
		this.mutationProbability = mutationProbability;
	}

	public int getArchiveSize() {
		return archiveMaxSize;
	}

	public void setArchiveSize(int archiveSize) {
		this.archiveMaxSize = archiveSize;
	}

	@Override
	public void setParameters(GeneralMOOParametersTO parameters) {
		if (!(parameters instanceof HyperBoxMOOParametersTO)) {
			throw new RuntimeException("Parametros incorretos");
		}

		this.mutationProbability = parameters.getMutationProbability();
		this.crossoverProbability = parameters.getCrossoverProbability();
		this.archiveMaxSize = ((HyperBoxMOOParametersTO) parameters)
				.getArchiveSize();
		this.populationSize = parameters.getPopulationSize();
		this.numberOfDivisions = ((HyperBoxMOOParametersTO) parameters)
				.getBisections();
	}

	/**
	 * Método acessor para obter o valor do atributo pathFiles.
	 * @return O valor de pathFiles
	 */
	public String getPathFiles() {
		return pathFiles;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo pathFiles.
	 * @param pathFiles O novo valor de pathFiles
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
	 * @return the archiveMaxSize
	 */
	public int getArchiveMaxSize() {
		return archiveMaxSize;
	}

	/**
	 * @param archiveMaxSize the archiveMaxSize to set
	 */
	public void setArchiveMaxSize(int archiveMaxSize) {
		this.archiveMaxSize = archiveMaxSize;
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
	 * @return the archive
	 */
	public ArchiveGrid<Integer> getArchive() {
		return archive;
	}

	/**
	 * @param archive the archive to set
	 */
	public void setArchive(ArchiveGrid<Integer> archive) {
		this.archive = archive;
	}

	/**
	 * @return the numberOfDivisions
	 */
	public int getNumberOfDivisions() {
		return numberOfDivisions;
	}

	/**
	 * @param numberOfDivisions the numberOfDivisions to set
	 */
	public void setNumberOfDivisions(int numberOfDivisions) {
		this.numberOfDivisions = numberOfDivisions;
	}
}
