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
 * Arquivo: HVCA.java
 * ****************************************************************************
 * Histórico de revisões
 * CR		Nome				Data		Descrição
 * ****************************************************************************
 * 064813-P	Danilo Araújo		05/05/2011		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.metaheuristics.dwga;

import java.util.List;
import java.util.Vector;

import br.upe.jol.base.Algorithm;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.configuration.GeneralMOOParametersTO;
import br.upe.jol.operators.crossover.Crossover;
import br.upe.jol.operators.crossover.OnePointCrossover;
import br.upe.jol.operators.mutation.Mutation;
import br.upe.jol.operators.mutation.UniformMutation;
import br.upe.jol.operators.selection.BinaryTournament;

/**
 * TODO Descrição do tipo
 * 
 * @author Danilo Araújo
 * @since 05/05/2011
 */
public class HVCA extends Algorithm<Double> {
	private static final long serialVersionUID = 1L;
	
	private int maxIterations = 500;
	
	private int iteration = 0;
	
	private int populationSize = 10;
	
	private int numGroups = 5;
	
	private double minContrib;
	
	private double maxContrib;
	
	private List<SolutionSet<Double>> populations = new Vector<SolutionSet<Double>>();
		
	private double mutationProbability = 0.03;

	private double crossoverProbability = 1;
	
	private double sumHv = 0;
	@Override
	public SolutionSet<Double> execute() {
		initializePopulation();
		HVComparator<Double> comparator = new HVComparator<Double>();
		HVComparatorGroup<Double> comparatorGroup = new HVComparatorGroup<Double>();
		BinaryTournament<Double> selection = new BinaryTournament<Double>(comparator);
		BinaryTournament<Double> inverseSelection = new BinaryTournament<Double>(comparator);
		BinaryTournamentGroup<Double> selectionGroup = new BinaryTournamentGroup<Double>(comparatorGroup);
		BinaryTournamentGroup<Double> inverseSelectionGroup = new BinaryTournamentGroup<Double>(new InverseHVGroup<Double>());
		SolutionSet<Double> offsprings;
		Crossover<Double> crossover = new OnePointCrossover(this.crossoverProbability);
		Mutation<Double> mutation = new UniformMutation(this.mutationProbability);
		Solution<Double> offspring1;
		Solution<Double> offspring2;
		double idealContrib = 1/populationSize;
		List<SolutionSet<Double>> populations1 = null;
		for (int i = 0; i < maxIterations; i++){
			offsprings = new SolutionSet<Double>();
			double hvGroup = 0;
			double angleGroup = 0;
			populations1 = new Vector<SolutionSet<Double>>();
			for (int j = 0; j < populationSize/2; j++){
				SolutionSet<Double> population = (SolutionSet<Double>)selectionGroup.execute(populations);
				
				offspring1 = (Solution<Double>)selection.execute(population);
				offspring2 = (Solution<Double>)selection.execute(population);
				
				Solution<Double>[] vector =  (Solution<Double>[]) crossover.execute(offspring1, offspring2);
				
				offspring1 = vector[0];
				offspring2 = vector[1];
				
				mutation.execute(offspring1);
				mutation.execute(offspring2);
				offspring1.evaluateObjectives();
				offspring2.evaluateObjectives();
				offsprings.add(offspring1);
				offsprings.add(offspring2);
				hvGroup += ((HVCASolution)offspring1).getHvContributionIndividual();
				angleGroup += ((HVCASolution)offspring1).getHvContributionIndividual();
				hvGroup += ((HVCASolution)offspring2).getHvContributionIndividual();
				angleGroup += ((HVCASolution)offspring2).getHvContributionIndividual();
			}
			for (int j = 0; j < populationSize; j++){
				((HVCASolution)offsprings.get(j)).setHvContributionGroup(Math.abs(hvGroup/populationSize - ((HVCASolution)offsprings.get(j)).getHvContributionIndividual()));
				((HVCASolution)offsprings.get(j)).setVarAngleGroup(Math.abs(getMeanAngle(populationSize) - ((HVCASolution)offsprings.get(j)).getAngleIndividual()));
			}
			offsprings.setDifAngleSolutions(Math.abs(getMeanAngle(populationSize) - angleGroup/populationSize));
			offsprings.setDifHVSolutions(hvGroup);
			
			populations.add(offsprings);
			for (int j = 0; j < numGroups+1; j++){
				populations.get(j).setDifHVSolutions(Math.abs(idealContrib * sumHv/(numGroups + 1) - populations.get(j).getDifHVSolutions()/populationSize));
			}
 			populations.remove(inverseSelectionGroup.execute(populations));
			
		}
		
		SolutionSet<Double> population = new SolutionSet<Double>();
		
		for (int i = 0; i < numGroups; i++){
			population.getSolutionsList().addAll(populations.get(i).getSolutionsList());
		}
		
		return population;
	}

	@Override
	public SolutionSet<Double> execute(SolutionSet<Double> ss, int lastGeneration) {
		return null;
	}

	@Override
	public void setParameters(GeneralMOOParametersTO parameters) {
		
	}

	private void initializePopulation() {
		double idealContrib = 1/populationSize;
		sumHv = 0;
		
		for (int i = 0; i < numGroups; i++){
			populations.add(new SolutionSet<Double>());
			sumHv += inicializePopulationGroup(populations.get(i), idealContrib);
		}
		for (int i = 0; i < numGroups; i++){
			populations.get(i).setDifHVSolutions(Math.abs(idealContrib * sumHv/numGroups - populations.get(i).getDifHVSolutions()/populationSize));
		}
	}

	/**
	 * TODO Descrição do método
	 *
	 */
	private double inicializePopulationGroup(SolutionSet<Double> population, double idealContrib) {
		Double[] variables;
		HVCASolution solution;
		double hvGroup = 0;
		double angleGroup = 0;
		for (int i = 0; i < populationSize; i++){
			variables = new Double[problem.getNumberOfVariables()];
			for (int j = 0; j < problem.getNumberOfVariables(); j++){
				variables[j] = Math.random() * problem.getUpperLimit(j) + problem.getLowerLimit(j);
			}
			solution = new HVCASolution(problem, variables);
			solution.evaluateObjectives();
			population.add(solution);
			hvGroup += ((HVCASolution)population.get(i)).getHvContributionIndividual();
			angleGroup += ((HVCASolution)population.get(i)).getHvContributionIndividual();
			
		}
		for (int i = 0; i < populationSize; i++){
			((HVCASolution)population.get(i)).setHvContributionGroup(Math.abs(hvGroup/populationSize - ((HVCASolution)population.get(i)).getHvContributionIndividual()));
			((HVCASolution)population.get(i)).setVarAngleGroup(Math.abs(getMeanAngle(populationSize) - ((HVCASolution)population.get(i)).getAngleIndividual()));
		}
		population.setDifAngleSolutions(Math.abs(getMeanAngle(populationSize) - angleGroup/populationSize));
		population.setDifHVSolutions(hvGroup);
		return hvGroup;
	}
	
	private static double getMeanAngle(int numDiv){
		double sumAngle = 0;
		double angle = 0;
		
		for (int i = 0; i < numDiv; i++){
			angle += (2 * Math.PI)/numDiv;
			sumAngle += angle;
		}
		return sumAngle/numDiv;
	}

}
