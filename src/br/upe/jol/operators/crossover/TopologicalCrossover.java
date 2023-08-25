/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: TopologicalCrossover.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	18/01/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.operators.crossover;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import br.cns.transformations.DegreeMatrix;
import br.upe.jol.base.Scheme;
import br.upe.jol.base.Solution;
import br.upe.jol.problems.simton.SimtonProblem;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo
 * @since 18/01/2014
 */
public class TopologicalCrossover extends Crossover<Integer> {
	private static final long serialVersionUID = 12L;

	public TopologicalCrossover(double crossoverPropability) {
		super(crossoverPropability);
	}

	@Override
	public Object execute(Solution<Integer>... parents) {
		SolutionONTD[] offSpring = new SolutionONTD[2];

		SolutionONTD offs1 = null;
		SolutionONTD offs2 = null;

		offs1 = new SolutionONTD(parents[0].getProblem(), parents[0].getDecisionVariables());
		offs2 = new SolutionONTD(parents[1].getProblem(), parents[1].getDecisionVariables());

		int n = 14;

		if (Math.random() < crossoverProbability) {
			Integer[] variables1 = parents[0].getDecisionVariables();
			Integer[] variables2 = parents[1].getDecisionVariables();

			Integer[][] adjacencyMatrix1 = new Integer[n][n];
			Integer[][] adjacencyMatrix2 = new Integer[n][n];
			int index = 0;
			for (int i = 0; i < n; i++) {
				adjacencyMatrix1[i][i] = 0;
				for (int j = i + 1; j < n; j++) {
					if (variables1[index] == 0) {
						adjacencyMatrix1[i][j] = 0;
						adjacencyMatrix1[j][i] = 0;
					} else {
						adjacencyMatrix1[i][j] = 1;
						adjacencyMatrix1[j][i] = 1;
					}

					if (variables2[index] == 0) {
						adjacencyMatrix2[i][j] = 0;
						adjacencyMatrix2[j][i] = 0;
					} else {
						adjacencyMatrix2[i][j] = 1;
						adjacencyMatrix2[j][i] = 1;
					}

					index++;
				}
			}

			if (Math.random() > 0.5) {
				index = 0;
				for (int j = 0; j < adjacencyMatrix1.length; j++) {
					for (int k = j + 1; k < adjacencyMatrix1.length; k++) {
						if (adjacencyMatrix1[j][k]  + adjacencyMatrix2[j][k] == 1) {
							offs1.setValue(index, 3);
						} else {
							offs1.setValue(index, 0);
						}
						offs2.setValue(index, adjacencyMatrix2[j][k] * 3);
						index++;
					}
				}
			} else {
				index = 0;
				for (int j = 0; j < adjacencyMatrix1.length; j++) {
					for (int k = j + 1; k < adjacencyMatrix1.length; k++) {
						offs1.setValue(index, adjacencyMatrix1[j][k] * 3);
						if (adjacencyMatrix1[j][k]  + adjacencyMatrix2[j][k] == 1) {
							offs2.setValue(index, 3);
						} else {
							offs2.setValue(index, 0);
						}
						index++;
					}
				}
			}
			
			// if (Math.random() > 0.5) {
			offs1.setValue(variables1.length - 1, variables2[variables1.length - 1]);
			offs2.setValue(variables1.length - 1, variables1[variables1.length - 1]);

			offs1.setValue(variables1.length - 2, variables2[variables1.length - 2]);
			offs2.setValue(variables1.length - 2, variables1[variables1.length - 2]);
			// } else {
			// offs1.setValue(variables1.length-1,
			// variables1[variables1.length-1]);
			// offs2.setValue(variables1.length-1,
			// variables2[variables1.length-1]);
			//
			// offs1.setValue(variables1.length-2,
			// variables1[variables1.length-2]);
			// offs2.setValue(variables1.length-2,
			// variables2[variables1.length-2]);
			// }
		}
		System.out.println("Rodou 1...");
		offSpring[0] = offs1;
		offSpring[1] = offs2;

		return offSpring;
	}

	@Override
	public Object execute(Scheme scheme, Solution<Integer>... parents) {
		return null;
	}

	public static void main(String[] args) {
		SimtonProblem ontd = new SimtonProblem(14, 2);
		SolutionONTD sol1 = null;

		Integer[] variables = null;
		variables = new Integer[ontd.getNumberOfVariables()];
		Arrays.fill(variables, 1);
		sol1 = new SolutionONTD(ontd, variables);

		variables = new Integer[ontd.getNumberOfVariables()];
		Arrays.fill(variables, 2);
		SolutionONTD sol2 = new SolutionONTD(ontd, variables);

		TopologicalCrossover op = new TopologicalCrossover(1);

		SolutionONTD[] sol = (SolutionONTD[]) op.execute(sol1, sol2);

		System.out.println("Indivídulo 1:");
		for (int i = 0; i < variables.length; i++) {
			System.out.print(sol[0].getDecisionVariables()[i] + " ");
		}

		System.out.println("Indivídulo 2:");
		for (int i = 0; i < variables.length; i++) {
			System.out.print(sol[1].getDecisionVariables()[i] + " ");
		}
	}

	@Override
	public String getOpID() {
		return "C99";
	}

}
