package br.upe.jol.metaheuristics.modeii;

import java.util.Random;

import br.upe.jol.base.Algorithm;
import br.upe.jol.base.Observer;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.configuration.ArchiveMOOParametersTO;
import br.upe.jol.configuration.GeneralMOOParametersTO;
import br.upe.jol.operators.crossover.Crossover;
import br.upe.jol.operators.crossover.MODEIICrossover;

public class MODEII extends Algorithm<Double> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int populationSize = 50;
	private int archiveSize = 100;
	
	private SolutionSet<Double> population;
	private SolutionSet<Double> archive;
	
	private int iteration, maxIterations;
	private double crossoverProbability = 0.3;
	
	public MODEII(int populationSize, int archiveSize, int maxIterations, Problem<Double> problem){
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
	public SolutionSet<Double> execute() {
		this.initializePopulation(); //Step 1
		return this.execute(population, 1);
	}

	@Override
	public SolutionSet<Double> execute(SolutionSet<Double> ss,
			int lastGeneration) {
		this.population = ss;
		this.iteration = lastGeneration;
		
		Solution<Double> mutated, offspring, solution = null;
		Crossover<Double> crossover = new MODEIICrossover(this.crossoverProbability);
		
		while(nextIteration()){	//Step 3
			
			for(int i=0; i<this.population.size(); i++){
		
				//Step 3.1
				mutated = this.mutation(i);
				problem.evaluateConstraints(mutated);
				problem.evaluate(mutated);
				solution = population.get(i);
				//Step 3.2
				offspring = (Solution<Double>)crossover.execute(
						solution , mutated);	
						
				//Step 3.3
				problem.evaluateConstraints(offspring);
				problem.evaluate(offspring);
				
				if(solution.dominates(offspring)){
					continue;
				}else if(offspring.dominates(solution)){
					this.population.update(i, offspring);
					this.updateArchive(offspring);
				}else if(this.isLessCrowded(offspring, solution)){
					this.population.update(i, offspring);
				}
				
			}
			
			updateObserver();
			//Step 3.5
			this.iteration++;
		}
		
		
		return this.archive;
	}

	/**
	 * Atualiza o arquivo externo
	 * @param offspring
	 */
	private void updateArchive(Solution<Double> offspring) {
		
		for(int i=0; i<this.archive.size(); i++){
			Solution<Double> s = this.archive.get(i);
			//é dominado por alguma solução do arquivo = descartado
			if(s.dominates(offspring)){
				return;
			}
		}
		
		for(int i=0; i<this.archive.size(); i++){
			Solution<Double> s = this.archive.get(i);
			//domina alguma solução do arquivo
			if(offspring.dominates(s)){
				//elimina solução dominada
				this.archive.remove(i);
				--i;
			}
		}
			
		//Step 3.4
		if(this.archive.size() == this.archive.getCapacity()){
			//truncamento
			int toRemoval = this.getForRemoval(archive);
			this.archive.remove(toRemoval);
		}
		
		//adiciona nova solução
		this.archive.add(offspring);
		
	}
	
	/**
	 * Retorna a solução que está no local mais "lotado"
	 * @param solutionSet
	 * @return
	 */
	private int getForRemoval(SolutionSet<Double> solutionSet){
		//calcula a matriz de distancia entre todas as solucoes do arquivo externo
		double[][] distances = solutionSet.writeDistancesToMatrix();
		double minDistance = this.harmonicAvarageDistance(distances[0]);
		int forRemoval = 0;
		
		for(int i=1; i<distances[0].length; i++){
			double auxDistance = harmonicAvarageDistance(distances[i]);
			if(auxDistance < minDistance){
				minDistance = auxDistance;
				forRemoval = i;
			}
		}
		
		return forRemoval;
	}
	
	/**
	 * Retorna se <code>solution1</code> esta em um local menos "povoado" 
	 * @param solution1
	 * @param solution2
	 * @return
	 */
	private boolean isLessCrowded(Solution<Double> solution1, Solution<Double> solution2){
		double[] distances1 = solution1.distanceOrderedDif(archive);
		double[] distances2 = solution2.distanceOrderedDif(archive);
		
		double d1 = this.harmonicAvarageDistance(distances1);
		double d2 = this.harmonicAvarageDistance(distances2);
		
		return d1 > d2;
	}
	
	/**
	 * Calcula a Harmonic Avarage Distance, descrita no paper
	 * @param distances
	 * @return
	 */
	private double harmonicAvarageDistance(double[] distances){
		double sum = 0;
		int k = (int) Math.sqrt(population.size() + archive.size() -1);
		for(int i=0; i<k && i<distances.length-1; i++){
			if(distances[i] == 0){
				sum += 0;
			}else{
				sum += 1/distances[i];
			}
		}
		
		return k / sum;
	}

	/**
	 * "Mutação" descrita no paper
	 * @param indexSolution
	 * @return
	 */
	private Solution<Double> mutation(int indexSolution){
		//pega Xbest
		Solution<Double> retorno = this.archive.getRandom();
		//calcula f -> [0,2]
		//double f = (new Random().nextDouble()) * 2;
		double f = 0.3;
		//calcular os r1, r2, r3, r4. Diferentes do indexSolution e diferentes entre eles
		int[] rs = this.getRs(indexSolution);
		
		Solution<Double> solution1 = this.population.get(rs[0]);
		Solution<Double> solution2 = this.population.get(rs[1]);
		
		//F(Xr1 - Xr2)
		solution1 = this.multi(f, this.sub(solution1, solution2));
		
		Solution<Double> solution3 = this.population.get(rs[2]);
		Solution<Double> solution4 = this.population.get(rs[3]);
		
		//F(Xr3 - Xr4)
		solution3 = this.multi(f, this.sub(solution3, solution4));
		
		//Xbest + F(Xr1 - Xr2) + F(Xr3 - Xr4)
		return this.add(retorno, this.add(solution1, solution3));
	}
	
	/**
	 * Multiplicação de vetores
	 * @param scala
	 * @param solution
	 * @return
	 */
	private Solution<Double> multi(double scala, Solution<Double> solution){
		Double[] var = solution.getDecisionVariables();
		Double[] retorno = new Double[var.length];
		
		for(int i=0; i<var.length; i++){
			retorno[i] = scala *(var[i]); 
		}
		
		return new Solution<Double>(solution.getProblem(), retorno);
	} 
	
	/**
	 * Subtração de vetores
	 * @param solution1
	 * @param solution2
	 * @return
	 */
	private Solution<Double> sub(Solution<Double> solution1, Solution<Double> solution2){
		Double[] var1 = solution1.getDecisionVariables();
		Double[] var2 = solution2.getDecisionVariables();
		Double[] retorno = new Double[var1.length];
		
		for(int i=0; i<var1.length; i++){
			retorno[i] = var1[i] - var2[i]; 
		}
		
		return new Solution<Double>(solution1.getProblem(), retorno);
	}
	
	/**
	 * Soma de vetores
	 * @param solution1
	 * @param solution2
	 * @return
	 */
	private Solution<Double> add(Solution<Double> solution1, Solution<Double> solution2){
		Double[] var1 = solution1.getDecisionVariables();
		Double[] var2 = solution2.getDecisionVariables();
		Double[] retorno = new Double[var1.length];
		
		for(int i=0; i<var1.length; i++){
			retorno[i] = var1[i] + var2[i]; 
		}
		
		return new Solution<Double>(solution1.getProblem(), retorno);
	}
	
	/**
	 * Retorna os indices como definido no paper
	 * @param indexSolution
	 * @return
	 */
	private int[] getRs(int indexSolution){
		int r1, r2, r3, r4;
		
		do{
			r1 = new Random().nextInt(this.population.size());
		}while(r1 == indexSolution);
		
		do{
			r2 = new Random().nextInt(this.population.size());
		}while(r2 == r1 || r2 == indexSolution);
		
		do{
			r3 = new Random().nextInt(this.population.size());
		}while(r3 == r1 || r3 == r2 || r3 == indexSolution);
		
		do{
			r4 = new Random().nextInt(this.population.size());
		}while(r4 == r1 || r4 == r2 || r4 == r3 || r4 == indexSolution);
		
		int[] retorno = {r1, r2, r3, r4};
		return retorno;
	}
	
	private boolean nextIteration() {
		return iteration <= maxIterations;
	}

	@Override
	public void setParameters(GeneralMOOParametersTO parameters) {
		if(!(parameters instanceof ArchiveMOOParametersTO)){
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
		for (int i = 0; i < populationSize; i++){
			variables = new Double[problem.getNumberOfVariables()];
			for (int j = 0; j < problem.getNumberOfVariables(); j++){
				variables[j] = Math.random() * problem.getUpperLimit(j) + problem.getLowerLimit(j);
			}
			solution = new Solution<Double>(problem, variables);
			problem.evaluateConstraints(solution);
			solution.evaluateObjectives();
			population.add(solution);
			if(this.archive.getCapacity() > this.archive.size()){
				archive.add(solution);//Step 2
			}
		}
		if (observer != null){
			observer.setSolutionSet(archive);
		}
	}

	private synchronized void updateObserver() {
		if(observer == null){
			observer = new Observer();
			observer.setSolutionSet(archive);
		}
		
		if (observer != null){
			observer.setIteration(iteration);
			observer.setSolutionSet(archive);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
	}

	public double getCrossoverProbability() {
		return crossoverProbability;
	}

	public void setCrossoverProbability(double crossoverProbability) {
		this.crossoverProbability = crossoverProbability;
	}
	
}
