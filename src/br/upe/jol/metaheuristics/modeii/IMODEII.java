package br.upe.jol.metaheuristics.modeii;

import java.util.Random;

import br.upe.jol.base.Algorithm;
import br.upe.jol.base.Operator;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.base.Util;
import br.upe.jol.base.comparators.IObserver;
import br.upe.jol.configuration.ArchiveMOOParametersTO;
import br.upe.jol.configuration.GeneralMOOParametersTO;
import br.upe.jol.metaheuristics.spea2.ISPEA2Solution;
import br.upe.jol.operators.crossover.IMODEIICrossover;

public class IMODEII extends Algorithm<Integer> {
	private static final long serialVersionUID = 1L;
	private int populationSize = 50;
	private int archiveSize = 50;

	private SolutionSet<Integer> population;
	private SolutionSet<Integer> archive;

	private int iteration, maxIterations;
	private double crossoverProbability = 0.9;

	private String pathFiles = "C:/Temp/ontd";

	public IMODEII(int populationSize, int archiveSize, int maxIterations, Problem<Integer> problem) {
		this.populationSize = populationSize;
		this.archiveSize = archiveSize;

		this.population = new SolutionSet<Integer>(this.populationSize);
		this.archive = new SolutionSet<Integer>(this.archiveSize);

		this.iteration = 0;
		this.maxIterations = maxIterations;

		this.problem = problem;
	}

	@Override
	public String toString() {
		return "MODEII";
	}

	@Override
	public SolutionSet<Integer> execute() {
		this.initializePopulation(); // Step 1
		return this.execute(population, 1);
	}

	@Override
	public SolutionSet<Integer> execute(SolutionSet<Integer> ss, int lastGeneration) {
		this.population = ss;
		this.iteration = lastGeneration;

		Solution<Integer> mutated, offspring, solution = null;
		IMODEIICrossover crossover = new IMODEIICrossover(this.crossoverProbability);

		while (nextIteration()) { // Step 3
			Util.LOGGER.info(">>>>>>>>>>>>>>>>>>> MODEII, gera��o " + iteration + " <<<<<<<<<<<<<<<<<<<<<<<");

			for (int i = 0; i < this.population.size(); i++) {
				// Step 3.1
				mutated = this.mutation(i);
				solution = population.get(i);
				// Step 3.2
				offspring = (Solution<Integer>) crossover.execute(solution, mutated);
		
				// Step 3.3
				problem.evaluateConstraints(offspring);
				problem.evaluate(offspring);

				if (solution.dominates(offspring)) {
					continue;
				} else if (offspring.dominates(solution)) {
					this.population.update(i, offspring);
					this.updateArchive(offspring);
				} else if (this.isLessCrowded(offspring, solution)) {
					this.population.update(i, offspring);
				}

			}
			if (iteration % Util.GRAV_EXP_STEP == 0){
				gravarResultados(iteration, crossover);
			}

			// Step 3.5
			this.iteration++;
		}

		return this.archive;
	}

	/**
	 * Grava os resultados em arquivos
	 *
	 * @param iteration N�mero da itera��o
	 */
	private void gravarResultados(int iteration, Operator<Integer> crossover) {
		System.out.println("Gravando resultados em " + pathFiles + "_mode_" + populationSize + "_"+  nf.format(crossoverProbability) + "_" + itf.format(iteration));
		population.printObjectivesToFile(pathFiles + "_mode_" + populationSize + "_"+  nf.format(crossoverProbability) +  "_" + itf.format(iteration) + "_pf.txt");
		population.printVariablesToFile(pathFiles + "_mode_" + populationSize + "_"+  nf.format(crossoverProbability) + "_" + itf.format(iteration) + "_var.txt");
	}

	/**
	 * Atualiza o arquivo externo
	 * 
	 * @param offspring
	 */
	private void updateArchive(Solution<Integer> offspring) {

		for(int i=0; i<this.archive.size(); i++){
			Solution<Integer> s = this.archive.get(i);
			//é dominado por alguma solução do arquivo
			if(s.dominates(offspring)){
				return;
			}
		}
		
		for(int i=0; i<this.archive.size(); i++){
			Solution<Integer> s = this.archive.get(i);
			//domina alguma solução do arquivo
			if(offspring.dominates(s)){
				//elimina solução dominada
				this.archive.remove(i);
				--i;
			}
		}
		// Step 3.4
		if (this.archive.size() == this.archive.getCapacity()) {
			// truncamento
			int toRemoval = this.getForRemoval(archive);
			this.archive.remove(toRemoval);
		}

		// adiciona nova solução
		this.archive.add(offspring);

	}

	/**
	 * Retorna a solução que está no local mais "lotado"
	 * 
	 * @param solutionSet
	 * @return
	 */
	private int getForRemoval(SolutionSet<Integer> solutionSet) {
		double[][] distances = solutionSet.writeDistancesToMatrix();
		double minDistance = this.harmonicAvarageDistance(distances[0]);
		int forRemoval = 0;

		for (int i = 1; i < distances[0].length; i++) {
			double auxDistance = harmonicAvarageDistance(distances[i]);
			if (auxDistance < minDistance) {
				minDistance = auxDistance;
				forRemoval = i;
			}
		}

		return forRemoval;
	}

	/**
	 * Retorna se <code>solution1</code> esta em um local menos "povoado"
	 * 
	 * @param solution1
	 * @param solution2
	 * @return
	 */
	private boolean isLessCrowded(Solution<Integer> solution1, Solution<Integer> solution2) {
		double[] distances1 = solution1.distanceOrderedDif(archive);
		double[] distances2 = solution2.distanceOrderedDif(archive);

		double d1 = this.harmonicAvarageDistance(distances1);
		double d2 = this.harmonicAvarageDistance(distances2);

		return d1 > d2;
	}

	/**
	 * Calcula a Harmonic Avarage Distance, descrita no paper
	 * 
	 * @param distances
	 * @return
	 */
	private double harmonicAvarageDistance(double[] distances) {
		double sum = 0;
		int k = (int) Math.sqrt(population.size() + archive.size() - 1);
		for (int i = 0; i < k && i < distances.length - 1; i++) {
			if (distances[i] == 0) {
				sum += 0;
			} else {
				sum += 1 / distances[i];
			}
		}

		return k / sum;
	}

	/**
	 * "Mutação" descrita no paper
	 * 
	 * @param indexSolution
	 * @return
	 */
	private Solution<Integer> mutation(int indexSolution) {
		// pega Xbest
		Solution<Integer> retorno = this.archive.getRandom();
		// calcula f -> [0,2]
		int f = new Random().nextInt(2);
		// calcular os r1, r2, r3, r4. Diferentes do indexSolution e diferentes
		// entre eles
		int[] rs = this.getRs(indexSolution);

		Solution<Integer> solution1 = this.population.get(rs[0]);
		Solution<Integer> solution2 = this.population.get(rs[1]);

		// F(Xr1 - Xr2)
		solution1 = this.multi(f, this.sub(solution1, solution2));

		Solution<Integer> solution3 = this.population.get(rs[2]);
		Solution<Integer> solution4 = this.population.get(rs[3]);

		// F(Xr3 - Xr4)
		solution3 = this.multi(f, this.sub(solution3, solution4));

		// Xbest + F(Xr1 - Xr2) + F(Xr3 - Xr4)
		return this.add(retorno, this.add(solution1, solution3));
	}

	/**
	 * Multiplicação de vetores
	 * 
	 * @param scala
	 * @param solution
	 * @return
	 */
	private Solution<Integer> multi(int scala, Solution<Integer> solution) {
		Integer[] var = solution.getDecisionVariables();
		Integer[] retorno = new Integer[var.length];

		for (int i = 0; i < var.length; i++) {
			retorno[i] = scala * (var[i]);
			if (retorno[i] > problem.getUpperLimit(i)){
				retorno[i] = (int)problem.getUpperLimit(i);
			}else if (retorno[i] < problem.getLowerLimit(i)){
				retorno[i] = (int)problem.getLowerLimit(i);
			}
		}

		return new Solution<Integer>(solution.getProblem(), retorno);
	}

	/**
	 * Subtração de vetores
	 * 
	 * @param solution1
	 * @param solution2
	 * @return
	 */
	private Solution<Integer> sub(Solution<Integer> solution1, Solution<Integer> solution2) {
		Integer[] var1 = solution1.getDecisionVariables();
		Integer[] var2 = solution2.getDecisionVariables();
		Integer[] retorno = new Integer[var1.length];

		for (int i = 0; i < var1.length; i++) {
			retorno[i] = var1[i] - var2[i];
			if (retorno[i] > problem.getUpperLimit(i)){
				retorno[i] = (int)problem.getUpperLimit(i);
			}else if (retorno[i] < problem.getLowerLimit(i)){
				retorno[i] = (int)problem.getLowerLimit(i);
			}
		}

		return new Solution<Integer>(solution1.getProblem(), retorno);
	}

	/**
	 * Soma de vetores
	 * 
	 * @param solution1
	 * @param solution2
	 * @return
	 */
	private Solution<Integer> add(Solution<Integer> solution1, Solution<Integer> solution2) {
		Integer[] var1 = solution1.getDecisionVariables();
		Integer[] var2 = solution2.getDecisionVariables();
		Integer[] retorno = new Integer[var1.length];

		for (int i = 0; i < var1.length; i++) {
			retorno[i] = var1[i] + var2[i];
			if (retorno[i] > problem.getUpperLimit(i)){
				retorno[i] = (int)problem.getUpperLimit(i);
			}else if (retorno[i] < problem.getLowerLimit(i)){
				retorno[i] = (int)problem.getLowerLimit(i);
			}
		}

		return new Solution<Integer>(solution1.getProblem(), retorno);
	}

	/**
	 * Retorna os indices como definido no paper
	 * 
	 * @param indexSolution
	 * @return
	 */
	private int[] getRs(int indexSolution) {
		int r1, r2, r3, r4;

		do {
			r1 = new Random().nextInt(this.population.size());
		} while (r1 == indexSolution);

		do {
			r2 = new Random().nextInt(this.population.size());
		} while (r2 == r1 || r2 == indexSolution);

		do {
			r3 = new Random().nextInt(this.population.size());
		} while (r3 == r1 || r3 == r2 || r3 == indexSolution);

		do {
			r4 = new Random().nextInt(this.population.size());
		} while (r4 == r1 || r4 == r2 || r4 == r3 || r4 == indexSolution);

		int[] retorno = { r1, r2, r3, r4 };
		return retorno;
	}

	private boolean nextIteration() {
		return iteration <= maxIterations;
	}

	@Override
	public void setParameters(GeneralMOOParametersTO parameters) {
		if (!(parameters instanceof ArchiveMOOParametersTO)) {
			throw new RuntimeException("Parametros incorretos");
		}
		this.crossoverProbability = parameters.getCrossoverProbability();
		this.archiveSize = ((ArchiveMOOParametersTO) parameters).getArchiveSize();
		this.population = new SolutionSet<Integer>(this.populationSize);
		this.archive = new SolutionSet<Integer>(this.archiveSize);
	}

	/**
	 * Initializing the population
	 */
	private void initializePopulation() {
		Integer[] variables = null;
		Solution<Integer> solution = null;
		for (int i = 0; i < populationSize; i++) {
			variables = new Integer[problem.getNumberOfVariables()];
			for (int j = 0; j < problem.getNumberOfVariables(); j++) {
				variables[j] = (int) (Math.round(Math.random() * (problem.getUpperLimit(j) - problem.getLowerLimit(j))) + problem
						.getLowerLimit(j));
			}

			solution = new ISPEA2Solution(problem, variables);
			problem.evaluateConstraints(solution);
			problem.evaluate(solution);
			population.add(solution);
			if(this.archive.getCapacity() > this.archive.size()){
				archive.add(solution);//Step 2
			}
		}
		if (observer != null) {
			((IObserver) observer).setISolutionSet(population);
		}
	}

	/**
	 * M�todo acessor para obter o valor do atributo pathFiles.
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

	public double getCrossoverProbability() {
		return crossoverProbability;
	}

	public void setCrossoverProbability(double crossoverProbability) {
		this.crossoverProbability = crossoverProbability;
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
	 * @return the archiveSize
	 */
	public int getArchiveSize() {
		return archiveSize;
	}

	/**
	 * @param archiveSize the archiveSize to set
	 */
	public void setArchiveSize(int archiveSize) {
		this.archiveSize = archiveSize;
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
	public SolutionSet<Integer> getArchive() {
		return archive;
	}

	/**
	 * @param archive the archive to set
	 */
	public void setArchive(SolutionSet<Integer> archive) {
		this.archive = archive;
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
}
