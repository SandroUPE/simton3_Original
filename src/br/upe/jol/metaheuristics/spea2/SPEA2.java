package br.upe.jol.metaheuristics.spea2;

import java.util.ArrayList;

import br.upe.jol.base.Algorithm;
import br.upe.jol.base.Observer;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.base.comparators.DominanceComparator;
import br.upe.jol.base.comparators.FitnessComparator;
import br.upe.jol.configuration.ArchiveMOOParametersTO;
import br.upe.jol.configuration.GeneralMOOParametersTO;
import br.upe.jol.operators.crossover.OnePointCrossover;
import br.upe.jol.operators.mutation.UniformMutation;
import br.upe.jol.operators.selection.BinaryTournament;

/**
 * @author Erick
 *
 */
public class SPEA2 extends Algorithm<Double> {
	private static final long serialVersionUID = 1L;

	private int numberOfGenerations;
	SolutionSet<Double> archive = null;
	private int archiveSize;
	SolutionSet<Double> population = null;
	private int populationSize;
	SolutionSet<Double> union = null;
	int i;
	private int generation;
	private double mutationProbability;
	private double crossoverProbability;
	
	public void setParameters(GeneralMOOParametersTO parameters){
		if(!(parameters instanceof ArchiveMOOParametersTO)){
			throw new RuntimeException("Parametros incorretos");
		}
		
		this.mutationProbability = parameters.getMutationProbability();
		this.crossoverProbability = parameters.getCrossoverProbability();
		this.archiveSize = ((ArchiveMOOParametersTO) parameters).getArchiveSize();
		this.populationSize = parameters.getPopulationSize();
		this.population = new SolutionSet<Double>(this.populationSize);
		this.archive = new SolutionSet<Double>(this.archiveSize);
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
	
	public SPEA2(int populationSize, int archiveSize, int numberOfGenerations, Problem<Double> problem){
		this.numberOfGenerations = numberOfGenerations;
		this.problem = problem;
		this.archiveSize = archiveSize;
		this.archive = new SolutionSet<Double>(this.archiveSize);
		this.populationSize = populationSize;
		this.population = new SolutionSet<Double>(this.populationSize);
		this.union = new SolutionSet<Double>(this.populationSize+this.archiveSize);
		this.mutationProbability = 0.06;
		this.crossoverProbability = 0.9;
	}
	
	@Override
	public String toString() {
		return "SPEA 2";
	}
	
	@Override
	public SolutionSet<Double> execute() {
		this.initializePopulation();
		return this.execute(this.population, 1);
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public SolutionSet<Double> execute(SolutionSet<Double> ss, int lastGeneration) {
		//initialization
		this.population = ss;
		
		generation = lastGeneration;
		int countNonDominated;
		
		OnePointCrossover crossover = new OnePointCrossover(this.crossoverProbability);
		UniformMutation mutation = new UniformMutation(this.mutationProbability);
		BinaryTournament<Double> tournament = new BinaryTournament<Double>(new DominanceComparator<Double>());

		SPEA2Solution sel1, sel2 = null;
		SPEA2Solution parent1, parent2 = null;

		
		while(generation <= this.numberOfGenerations){
//			System.out.println("SPEA2 = " + generation);
			this.union = this.population.union(archive);
			
			countNonDominated = 0;
			int i;
			for(i=0; i<union.getCapacity(); i++){
				SPEA2Solution solution = (SPEA2Solution) union.get(i);
				solution.evaluateFitness(union);
				union.update(i, solution);
				if(union.get(i).getFitness() < 1)
					countNonDominated++;
			}
			this.environmentalSelection(countNonDominated);
		
			if(generation < this.numberOfGenerations){				
				this.population.clear();
				
				while(this.population.size() < this.population.getCapacity()){
					//selection
					sel1 = (SPEA2Solution) this.archive.getRandom();
					sel2 = (SPEA2Solution) this.archive.getRandom();
					parent1 = (SPEA2Solution) tournament.execute(sel1, sel2);
					
					sel1 = (SPEA2Solution) this.archive.getRandom();
					sel2 = (SPEA2Solution) this.archive.getRandom();
					parent2 = (SPEA2Solution) tournament.execute(sel1, sel2);

					Solution<Double>[] vector =  (Solution<Double>[]) crossover.execute(parent1, parent2);
					SPEA2Solution result1 =  (SPEA2Solution) vector[0];
					SPEA2Solution result2 =  (SPEA2Solution) vector[1];
					
					//mutation
					mutation.execute(result1);
					mutation.execute(result2);
					
					//constrains evaluation
					this.problem.evaluateConstraints(result1);
					result1.evaluateObjectives();
					this.population.add(result1);
					
					if(this.population.size() == this.population.getCapacity())
						break;
					
					this.problem.evaluateConstraints(result2);
					result2.evaluateObjectives();
					this.population.add(result2);
				}
			}
	
			updateObserver();
			generation += 1;
		}
		
		return this.archive;
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
		if (observer != null){
			observer.setSolutionSet(archive);
		}
	}
	
	private void environmentalSelection(int countNonDominated){
		this.archive.clear();
		
		if(countNonDominated == this.archive.getCapacity()){
			for(int i=0; i<this.union.size(); i++){
				if(this.union.get(i).getFitness() < 1)
					this.archive.add(union.get(i));
				
				if(this.archive.size() == this.archive.getCapacity())
					break;
			}
		}else if(countNonDominated < this.archive.getCapacity()){
			FitnessComparator<Double> comparator = new FitnessComparator<Double>();
			this.union.sort(comparator);
			
			for(int i=0; i<this.union.size(); i++){
					this.archive.add(union.get(i));
					
					if(this.archive.size() == this.archive.getCapacity())
						break;
			}
		//truncation
		}else{
			int count = countNonDominated-this.archive.getCapacity();
			SolutionSet<Double> aux = new SolutionSet<Double>(countNonDominated);
			ArrayList<Solution<Double>> list = new ArrayList<Solution<Double>>();
			
			for(int i=0; i<this.union.size(); i++){
				Solution<Double> solution = this.union.get(i);
				if(solution.getFitness() < 1){
					list.add(solution);
				}
			}
			aux.setSolutionsList(list);
			
			while( count > 0 ){
				int forRemoval = this.getForRemoval(aux);
				aux.remove(forRemoval);
				count--;
			}
			
			for(int i=0; i<this.union.size(); i++){
				SPEA2Solution solution =  (SPEA2Solution) aux.get(i);
				this.archive.add(solution);
				if(this.archive.size() == this.archive.getCapacity())
					break;
			}
		}
		
	}
	
	private int getForRemoval(SolutionSet<Double> solutionSet){
		double[][] distances = solutionSet.writeDistancesToMatrix();
		double minDistance = distances[0][0];
		int forRemoval = 0;
		
		for(int i=1; i<distances[0].length; i++){
			if(distances[i][0] < minDistance){
				minDistance = distances[i][0];
				forRemoval = i;
			}
			else if( distances[i][0] == minDistance ){
				int j = 0;
				while( j < distances[i].length-1  && distances[i][j] == distances[forRemoval][j]){
					j++;
				}
				
				if( distances[i][j] < distances[forRemoval][j]){
					forRemoval = i;
				}
			}
			
		}
		
		return forRemoval;
	}

	private synchronized void updateObserver() {
		if(observer == null){
			observer = new Observer();
			observer.setSolutionSet(archive);
		}
		
		if (observer != null){
			observer.setIteration(generation);
			observer.setSolutionSet(archive);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
	}
	
}
