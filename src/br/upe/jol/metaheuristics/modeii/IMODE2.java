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
 * Arquivo: IMODE2.java
 * ****************************************************************************
 * Histórico de revisões
 * CR		Nome				Data		Descrição
 * ****************************************************************************
 * 064813-P	Danilo Araújo		07/02/2011		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.metaheuristics.modeii;

import java.util.Random;

import br.upe.jol.base.Algorithm;
import br.upe.jol.base.MultithreadSolutionSet;
import br.upe.jol.base.Operator;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.base.Util;
import br.upe.jol.configuration.ArchiveMOOParametersTO;
import br.upe.jol.configuration.GeneralMOOParametersTO;
import br.upe.jol.operators.crossover.MODEIICrossover;
import br.upe.jol.problems.simton.SimonProblemDouble3Obj;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * Classe que implementa o algoritmo MODE no espaço contínuo.
 * 
 * @author Danilo Araújo
 * @since 07/02/2011
 */
public class IMODE2 extends Algorithm<Integer> {
	private static final long serialVersionUID = 1L;
	private int populationSize = 50;
	private int archiveSize = 50;

	private SolutionSet<Double> population;
	private SolutionSet<Double> archive;

	private int iteration, maxIterations;
	private double crossoverProbability = 0.3;

	private String pathFiles = "C:/Temp/ontd";

	public IMODE2(int populationSize, int archiveSize, int maxIterations, Problem<Integer> problem) {
		this.populationSize = populationSize;
		this.archiveSize = archiveSize;

		this.population = new SolutionSet<Double>(this.populationSize);
		this.archive = new SolutionSet<Double>(this.archiveSize);

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
		SolutionSet<Integer> ss = new SolutionSet<Integer>();
		SolutionSet<Double> solutionSetDouble = executeDouble();
		
		for (Solution<Double> solutionDouble : solutionSetDouble.getSolutionsList()){
			
			Integer[] variables = new Integer[problem.getNumberOfVariables()];
			for (int var = 0; var < solutionDouble.getDecisionVariables().length; var++){
				variables[var] = (int)Math.round(solutionDouble.getDecisionVariables()[var]);
				if (variables[var] < problem.getLowerLimit(var)){
					variables[var] = (int)problem.getLowerLimit(var);
				}else if (variables[var] > problem.getUpperLimit(var)){
					variables[var] = (int)problem.getUpperLimit(var);
				} 
			}
			Solution<Integer> solutionInt = new SolutionONTD(problem, variables);
			
			for (int i = 0; i < problem.getNumberOfObjectives(); i++){
				solutionInt.setObjective(i, solutionDouble.getObjective(i));	
			}
			
			solutionInt.setOverallConstraintViolation(solutionDouble.getOverallConstraintViolation());
			solutionInt.setNumberOfViolatedConstraint(solutionDouble.getNumberOfViolatedConstraint());
			
			ss.add(solutionInt);
		}
		
		return ss;
	}

	@Override
	public SolutionSet<Integer> execute(SolutionSet<Integer> ss, int lastGeneration) {
		return null;
	}

	public SolutionSet<Double> executeDouble() {
		this.initializePopulation(); // Step 1
		return this.executeDouble(population, 1);
	}

	public SolutionSet<Double> executeDouble(SolutionSet<Double> ss, int lastGeneration) {
		this.population = ss;
		this.iteration = lastGeneration;

		MODEIICrossover crossover = new MODEIICrossover(this.crossoverProbability);

		while (nextIteration()) { // Step 3
			Util.LOGGER.info(">>>>>>>>>>>>>>>>>>> MODEII, geração " + iteration + " <<<<<<<<<<<<<<<<<<<<<<<");

			generateOffsprings(crossover);
			if (iteration % Util.GRAV_EXP_STEP == 0){
				gravarResultados(iteration, crossover);
			}
			// Step 3.5
			this.iteration++;
		}

		return this.archive;
	}
	
	private synchronized void generateOffsprings(MODEIICrossover crossover){
		MultithreadSolutionSet mss = new MultithreadSolutionSet(populationSize);
		Solution<Double> mutated, offspring, solution = null;
		SolutionONTD solutionInt = null;
		for (int i = 0; i < populationSize; i++) {
			mutated = this.mutation(i);
			solution = population.get(i);
			offspring = (Solution<Double>) crossover.execute(solution, mutated);
			
			Integer[] variables = new Integer[problem.getNumberOfVariables()];
			for (int var = 0; var < offspring.getDecisionVariables().length; var++){
				variables[var] = (int)Math.round(offspring.getDecisionVariables()[var]);
				if (variables[var] < problem.getLowerLimit(var)){
					variables[var] = (int)problem.getLowerLimit(var);
				}else if (variables[var] > problem.getUpperLimit(var)){
					variables[var] = (int)problem.getUpperLimit(var);
				} 
			}
			solutionInt = new SolutionONTD(problem, variables);
			
			mss.evaluate(solutionInt);
		}
		SolutionSet<Integer> ss = mss.getSolutionSet();
		for (int i = 0; i < populationSize; i++) {
			solution = population.get(i);
			solutionInt = (SolutionONTD)ss.get(i);
			
			Double[] variables = new Double[problem.getNumberOfVariables()];
			for (int var = 0; var < solutionInt.getDecisionVariables().length; var++){
				variables[var] = solutionInt.getDecisionVariables()[var].doubleValue();
			}
			offspring = new Solution<Double>(SimonProblemDouble3Obj.problemDouble, variables);
			for (int k = 0; k < problem.getNumberOfObjectives(); k++){
				offspring.setObjective(k, solutionInt.getObjective(k));	
			}
			
			offspring.setOverallConstraintViolation(solutionInt.getOverallConstraintViolation());
			offspring.setNumberOfViolatedConstraint(solutionInt.getNumberOfViolatedConstraint());
			
			if (solution.dominates(offspring)) {
				continue;
			} else if (offspring.dominates(solution)) {
				this.population.update(i, offspring);
				this.updateArchive(offspring);
			} else if (this.isLessCrowded(offspring, solution)) {
				this.population.update(i, offspring);
				this.updateArchive(offspring);
			}
		}
	}

	/**
	 * Grava os resultados em arquivos
	 *
	 * @param iteration Número da iteração
	 */
	private void gravarResultados(int iteration, Operator<Double> crossover) {
		System.out.println("Gravando resultados em " + pathFiles + "_mode_" + populationSize + "_"+  nf.format(crossoverProbability) + "_" + itf.format(iteration));
		archive.printObjectivesToFile(pathFiles + "_mode_" + archiveSize + "_"+  nf.format(crossoverProbability) +  "_" + itf.format(iteration) + "_pf.txt");
		archive.printVariablesToFile(pathFiles + "_mode_" + archiveSize + "_"+  nf.format(crossoverProbability) + "_" + itf.format(iteration) + "_var.txt");
	}

	/**
	 * Atualiza o arquivo externo
	 * 
	 * @param offspring
	 */
	private void updateArchive(Solution<Double> offspring) {

		for(int i=0; i<this.archive.size(); i++){
			Solution<Double> s = this.archive.get(i);
			//Ã© dominado por alguma soluÃ§Ã£o do arquivo
			if(s.dominates(offspring)){
				return;
			}
		}
		
		for(int i=0; i<this.archive.size(); i++){
			Solution<Double> s = this.archive.get(i);
			//domina alguma soluÃ§Ã£o do arquivo
			if(offspring.dominates(s)){
				//elimina soluÃ§Ã£o dominada
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

		// adiciona nova soluÃ§Ã£o
		this.archive.add(offspring);

	}

	/**
	 * Retorna a soluÃ§Ã£o que estÃ¡ no local mais "lotado"
	 * 
	 * @param solutionSet
	 * @return
	 */
	private int getForRemoval(SolutionSet<Double> solutionSet) {
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
	private boolean isLessCrowded(Solution<Double> solution1, Solution<Double> solution2) {
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
			if (distances[i] != 0) {
				sum += 1.0 / distances[i];
			}
		}

		return k / sum;
	}

	/**
	 * "MutaÃ§Ã£o" descrita no paper
	 * 
	 * @param indexSolution
	 * @return
	 */
	private synchronized Solution<Double> mutation(int indexSolution) {
		// pega Xbest
		Solution<Double> retorno = this.archive.getRandom();
		//calcula f -> [0,2]
		double f = Math.random() * 2;
//		double f = 0.3;
		//calcular os r1, r2, r3, r4. Diferentes do indexSolution e diferentes entre eles
		int[] rs = this.getRs(indexSolution);
		
		Solution<Double> solution1 = this.population.get(rs[0]);
		Solution<Double> solution2 = this.population.get(rs[1]);
		
		//F(Xr1 - Xr2)
		try {
			solution1 = this.multi(f, this.sub(solution1, solution2));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Solution<Double> solution3 = this.population.get(rs[2]);
		Solution<Double> solution4 = this.population.get(rs[3]);
		
		//F(Xr3 - Xr4)
		solution3 = this.multi(f, this.sub(solution3, solution4));
		
		//Xbest + F(Xr1 - Xr2) + F(Xr3 - Xr4)
		return this.add(retorno, this.add(solution1, solution3));
	}

	/**
	 * MultiplicaÃ§Ã£o de vetores
	 * 
	 * @param scala
	 * @param solution
	 * @return
	 */
	private Solution<Double> multi(double scala, Solution<Double> solution){
		Double[] var = solution.getDecisionVariables();
		Double[] retorno = new Double[var.length];
		
		for(int i=0; i<var.length; i++){
			retorno[i] = scala *(var[i]); 
			if (retorno[i] > problem.getUpperLimit(i)){
				retorno[i] = problem.getUpperLimit(i);
			}else if (retorno[i] < problem.getLowerLimit(i)){
				retorno[i] = problem.getLowerLimit(i);
			}
		}
		
		return new Solution<Double>(solution.getProblem(), retorno);
	} 

	/**
	 * SubtraÃ§Ã£o de vetores
	 * 
	 * @param solution1
	 * @param solution2
	 * @return
	 */
	private Solution<Double> sub(Solution<Double> solution1, Solution<Double> solution2) {
		Double[] var1 = solution1.getDecisionVariables();
		Double[] var2 = solution2.getDecisionVariables();
		Double[] retorno = new Double[var1.length];

		for (int i = 0; i < var1.length; i++) {
			try {
				retorno[i] = var1[i] - var2[i];
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (retorno[i] > problem.getUpperLimit(i)){
				retorno[i] = problem.getUpperLimit(i);
			}else if (retorno[i] < problem.getLowerLimit(i)){
				retorno[i] = problem.getLowerLimit(i);
			}
		}

		return new Solution<Double>(solution1.getProblem(), retorno);
	}

	/**
	 * Soma de vetores
	 * 
	 * @param solution1
	 * @param solution2
	 * @return
	 */
	private Solution<Double> add(Solution<Double> solution1, Solution<Double> solution2) {
		Double[] var1 = solution1.getDecisionVariables();
		Double[] var2 = solution2.getDecisionVariables();
		Double[] retorno = new Double[var1.length];

		for (int i = 0; i < var1.length; i++) {
			retorno[i] = var1[i] + var2[i];
			if (retorno[i] > problem.getUpperLimit(i)){
				retorno[i] = problem.getUpperLimit(i);
			}else if (retorno[i] < problem.getLowerLimit(i)){
				retorno[i] = problem.getLowerLimit(i);
			}
		}

		return new Solution<Double>(solution1.getProblem(), retorno);
	}

	/**
	 * Retorna os indices como definido no paper
	 * 
	 * @param indexSolution
	 * @return
	 */
	private int[] getRs(int indexSolution) {
		int r1, r2, r3, r4;
		Random random = new Random();

		do {
			r1 = random.nextInt(this.population.size());
		} while (r1 == indexSolution);

		do {
			r2 = random.nextInt(this.population.size());
		} while (r2 == r1 || r2 == indexSolution);

		do {
			r3 = random.nextInt(this.population.size());
		} while (r3 == r1 || r3 == r2 || r3 == indexSolution);

		do {
			r4 = random.nextInt(this.population.size());
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
		this.population = new SolutionSet<Double>(this.populationSize);
		this.archive = new SolutionSet<Double>(this.archiveSize);
	}

	/**
	 * Initializing the population
	 */
	private void initializePopulation() {
		Double[] variables = null;
		Solution<Double> solution = null;
		for (int i = 0; i < populationSize; i++) {
			variables = new Double[problem.getNumberOfVariables()];
			
			Integer[] variablesInt = new Integer[problem.getNumberOfVariables()];
			for (int var = 0; var < problem.getNumberOfVariables(); var++){
				if (Math.random() > 0.5){
					variablesInt[var] = (int) (Math.round(Math.random() 
							* (problem.getUpperLimit(var) - problem.getLowerLimit(var))) + problem.getLowerLimit(var));
				}else{
					variablesInt[var] = (int)problem.getLowerLimit(var);	
				}
				if (variablesInt[var] < problem.getLowerLimit(var)){
					variablesInt[var] = (int)problem.getLowerLimit(var);
				}else if (variablesInt[var] > problem.getUpperLimit(var)){
					variablesInt[var] = (int)problem.getUpperLimit(var);
				}
				variables[var] = variablesInt[var].doubleValue();
			}
			solution = new Solution<Double>(SimonProblemDouble3Obj.problemDouble, variables);
			Solution<Integer> solutionInt = new SolutionONTD(problem, variablesInt);
			problem.evaluateConstraints(solutionInt);
			problem.evaluate(solutionInt);
			
			for (int k = 0; k < problem.getNumberOfObjectives(); k++){
				solution.setObjective(k, solutionInt.getObjective(k));
			}
			
			solution.setOverallConstraintViolation(solutionInt.getOverallConstraintViolation());
			solution.setNumberOfViolatedConstraint(solutionInt.getNumberOfViolatedConstraint());
			
			population.add(solution);
			if(this.archive.getCapacity() > this.archive.size()){
				archive.add(solution);//Step 2
			}
		}
	}

	/**
	 * Mï¿½todo acessor para obter o valor do atributo pathFiles.
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
	 * @return the archive
	 */
	public SolutionSet<Double> getArchive() {
		return archive;
	}

	/**
	 * @param archive the archive to set
	 */
	public void setArchive(SolutionSet<Double> archive) {
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
