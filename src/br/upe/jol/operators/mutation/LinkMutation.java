/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: LinkMutation.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	17/04/2015		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.operators.mutation;

import br.cns.transformations.DegreeMatrix;
import br.upe.jol.base.Scheme;
import br.upe.jol.base.Solution;
import br.upe.jol.problems.simton.SimtonProblem;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo Araujo
 * @since 17/04/2015
 */
public class LinkMutation extends Mutation<Integer> {
	private static final int N = 14;

	public LinkMutation(double mutationProbability) {
		super(mutationProbability);
	}

	private static final long serialVersionUID = 23L;

	public Integer[][] getAdjacencyMatrix(Solution<Integer> solution) {
		Integer[][] matrix = new Integer[N][N];
		int counter = 0;
		for (int i = 0; i < N; i++) {
			matrix[i][i] = 0;
			for (int j = i + 1; j < N; j++) {
				matrix[i][j] = solution.getDecisionVariables()[counter];
				matrix[j][i] = matrix[i][j];
				counter++;
			}
		}

		return matrix;
	}

	@Override
	public Object execute(Solution<Integer>... object) {
		Solution<Integer> solution = (Solution<Integer>) object[0];

		Integer[][] matrix = getAdjacencyMatrix(solution);
		Integer[][] degrees = DegreeMatrix.getInstance().transform(matrix);

		for (int i = 0; i < N; i++) {
			for (int j = i + 1; j < N; j++) {
				if (Math.random() > mutationProbability) {
					continue;
				}
				if (Math.random() > 0.7 && matrix[i][j] == 0) {
					matrix[i][j] = 1;
					matrix[j][i] = 1;
					degrees[i][i]++;
					degrees[j][j]++;
				} else if (matrix[i][j] == 1 && degrees[j][j] > 1 && degrees[i][i] > 1) {
					matrix[i][j] = 0;
					matrix[j][i] = 0;
					degrees[i][i]--;
					degrees[j][j]--;
				}
			}
		}
		int counter = 0;
		for (int i = 0; i < N; i++) {
			for (int j = i + 1; j < N; j++) {
				solution.setValue(counter, matrix[i][j]);
				counter++;
			}
		}
		if (Math.random() < 2 * mutationProbability) {
			int lambdas = solution.getDecisionVariables()[solution.getDecisionVariables().length-1];
			if (Math.random() > 0.5) {
				//aumentar lambda
				lambdas += 4;
				if (lambdas > 40) {
					lambdas = 40;
				}
			} else {
				//diminuir lambda
				lambdas -= 4;
				if (lambdas < 4) {
					lambdas = 4;
				}
			}
			solution.getDecisionVariables()[solution.getDecisionVariables().length-1] = lambdas;
		}
		if (Math.random() < 2 * mutationProbability) {
			int oxc = solution.getDecisionVariables()[solution.getDecisionVariables().length-2];
			if (Math.random() > 0.7) {
				//aumentar oxc
				oxc += 1;
				if (oxc > 4) {
					oxc = 4;
				}
			} else {
				//diminuir lambda
				oxc -= 1;
				if (oxc < 0) {
					oxc = 0;
				}
			}
			solution.getDecisionVariables()[solution.getDecisionVariables().length-2] = oxc;
		}
		return solution;
	}

	@Override
	public Object execute(Scheme scheme, Solution<Integer>... object) {
		Solution<Integer> solution = (Solution<Integer>) object[0];

		for (int var = 0; var < solution.getDecisionVariables().length; var++) {
			if (!scheme.getValor()[var].equals(Scheme.STR_CORINGA)) {
				solution.setValue(var, Integer.valueOf(scheme.getValor()[var]));
			} else if (Math.random() < mutationProbability) {
				int iValue = (int) (Math.round(Math.random()
						* (solution.getProblem().getUpperLimit(var) - solution.getProblem().getLowerLimit(var))) + solution
						.getProblem().getLowerLimit(var));

				if (iValue < solution.getProblem().getLowerLimit(var))
					iValue = (int) solution.getProblem().getLowerLimit(var);
				else if (iValue > solution.getProblem().getUpperLimit(var))
					iValue = (int) solution.getProblem().getUpperLimit(var);

				solution.setValue(var, iValue);
			}
		}
		return solution;
	}

	@Override
	public String getOpID() {
		return "M3";
	}

	public static void main(String[] args) {
		Integer[] variables = new Integer[] { 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 1, 4,
				40 };
		SimtonProblem problem = new SimtonProblem(14, 2, 100);
		int qtde = 100;
		SolutionONTD sol = new SolutionONTD(problem, variables);

		problem.evaluate(sol);

		LinkMutation lm = new LinkMutation(0.2);

		IUniformMutation um = new IUniformMutation(1.0 / 93);

		int dominates = 0;
		int isDominated = 0;
		int discarded = 0;
		int pbl = 0;
		int pbh = 0;
		for (int i = 0; i < qtde; i++) {
			problem = new SimtonProblem(14, 2, 100);
			SolutionONTD sol2 = (SolutionONTD) lm.execute(sol.clone());
//			 SolutionONTD sol2 = (SolutionONTD) um.execute(sol.clone());

			problem.evaluate(sol2);
			if (sol2.getObjective(0) < sol.getObjective(0)) {
				pbl++;
			} else {
				pbh++;
			}

			if (sol2.dominates(sol)) {
				dominates++;
			} else if (sol.dominates(sol2)) {
				isDominated++;
			}
			if (sol2.getObjective(0) == 1) {
				discarded++;
			}
		}
		System.out.println("Number of solutions with lower BP is " + pbl);
		System.out.println("Number of solutions with higher BP is " + pbh);
		System.out.println("Number of dominated solutions is " + isDominated);
		System.out.println("Number of solutions that are dominated is " + dominates);
		System.out.println("Number of non-dominated solutions is " + (qtde - isDominated - dominates));
		System.out.println("Number of discarded solutions is " + discarded);
	}
}
