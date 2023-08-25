/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: CrossoverLowDensity.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	17/04/2015		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.operators.crossover;

import br.upe.jol.base.Scheme;
import br.upe.jol.base.Solution;
import br.upe.jol.metaheuristics.spea2.ISPEA2Solution;
import br.upe.jol.problems.simton.SimtonProblem;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo Araujo
 * @since 17/04/2015
 */
public class CrossoverLowDensity extends Crossover<Integer> {
	private static final long serialVersionUID = 12L;

	public CrossoverLowDensity(double crossoverPropability) {
		super(crossoverPropability);
	}

	@Override
	public Object execute(Solution<Integer>... parents) {
		SolutionONTD[] offSpring = new SolutionONTD[2];

		SolutionONTD offs1 = null;
		SolutionONTD offs2 = null;

		if (parents[0] instanceof ISPEA2Solution) {
			offs1 = new ISPEA2Solution(parents[0].getProblem(), parents[0].getDecisionVariables());
			offs2 = new ISPEA2Solution(parents[1].getProblem(), parents[1].getDecisionVariables());
		} else {
			offs1 = new SolutionONTD(parents[0].getProblem(), parents[0].getDecisionVariables());
			offs2 = new SolutionONTD(parents[1].getProblem(), parents[1].getDecisionVariables());
		}

		if (Math.random() < crossoverProbability) {
			Integer[] variables1 = parents[0].getDecisionVariables();
			Integer[] variables2 = parents[1].getDecisionVariables();

			for (int i = 0; i < variables1.length - 2; i++) {
				if (variables1[i] > 0 || variables2[i] > 0) {
					offs1.setValue(i, 3);	
				}
				if (i % 2 == 0) {
					offs2.setValue(i, parents[0].getDecisionVariables()[i]);	
				}
			}
			offs1.setValue(variables1.length - 1, parents[1].getDecisionVariables()[variables1.length - 1]);	
			offs2.setValue(variables1.length - 2, parents[0].getDecisionVariables()[variables1.length - 2]);
		}

		offSpring[0] = offs1;
		offSpring[1] = offs2;

		return offSpring;
	}

	@Override
	public Object execute(Scheme scheme, Solution<Integer>... parents) {
		SolutionONTD[] offSpring = new SolutionONTD[2];

		SolutionONTD offs1 = null;
		SolutionONTD offs2 = null;

		if (parents[0] instanceof ISPEA2Solution) {
			offs1 = new ISPEA2Solution(parents[0].getProblem(), parents[0].getDecisionVariables());
			offs2 = new ISPEA2Solution(parents[1].getProblem(), parents[1].getDecisionVariables());
		} else {
			offs1 = new SolutionONTD(parents[0].getProblem(), parents[0].getDecisionVariables());
			offs2 = new SolutionONTD(parents[1].getProblem(), parents[1].getDecisionVariables());
		}

		if (Math.random() < crossoverProbability) {
			Integer[] variables1 = parents[0].getDecisionVariables();
			Integer[] variables2 = parents[1].getDecisionVariables();

			for (int i = 0; i < variables1.length - 2; i++) {
				if (variables1[i] > 0 && variables2[i] > 0) {
					offs1.setValue(i, 3);	
				}
				if (variables1[i] != variables2[i]) {
					offs2.setValue(i, 3);	
				}
			}
			offs1.setValue(variables1.length - 1, parents[1].getDecisionVariables()[variables1.length - 1]);	
			offs2.setValue(variables1.length - 2, parents[0].getDecisionVariables()[variables1.length - 2]);
		}

		offSpring[0] = offs1;
		offSpring[1] = offs2;

		return offSpring;
	}

	public static void main(String[] args) {
		SimtonProblem ontd = new SimtonProblem(14, 2);
		SolutionONTD sol1 = null;

		Integer[] variables = null;
		variables = new Integer[ontd.getNumberOfVariables()];
		for (int j = 0; j < ontd.getNumberOfVariables(); j++) {
			if (Math.random() < 0.5) {
				variables[j] = (int) Math.round(Math.random() * ontd.getUpperLimit(j));
			} else {
				variables[j] = (int) Math.round(Math.random() * ontd.getUpperLimit(j));
				if (variables[j] == 0) {
					variables[j] = 1;
				}
			}
		}
		sol1 = new SolutionONTD(ontd, variables);

		variables = new Integer[ontd.getNumberOfVariables()];
		for (int j = 0; j < ontd.getNumberOfVariables(); j++) {
			double dRandom = Math.random();
			variables[j] = (int) (Math.round(dRandom * (ontd.getUpperLimit(j) - ontd.getLowerLimit(j))) + ontd
					.getLowerLimit(j));
		}
		SolutionONTD sol2 = new SolutionONTD(ontd, variables);

		ICrossover op = new ICrossover(1);

		SolutionONTD[] sol = (SolutionONTD[]) op.execute(sol1, sol2);

		System.out.println(sol[0]);
	}

	@Override
	public String getOpID() {
		return "C99";
	}

}
