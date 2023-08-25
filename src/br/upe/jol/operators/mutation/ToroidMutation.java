/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: ToroidMutation.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	16/10/2013		Vers�o inicial
 * ****************************************************************************
 */
package br.upe.jol.operators.mutation;

import br.cns.metrics.AlgebraicConnectivity;
import br.cns.models.Toroid;
import br.upe.jol.base.Scheme;
import br.upe.jol.base.Solution;

/**
 * 
 * @author Danilo
 * @since 16/10/2013
 */
public class ToroidMutation extends Mutation<Integer> {
	public ToroidMutation(double mutationProbability) {
		super(mutationProbability);
	}

	private static final Toroid gp = new Toroid(0.2, 14);

	private static final long serialVersionUID = 23L;

	@Override
	public Object execute(Solution<Integer>... object) {
		Solution<Integer> solution = (Solution<Integer>) object[0];
		int numNodes = 14;
		Integer[][] adjacencyMatrix = new Integer[numNodes][numNodes];
		
		double minDensity = 2.0/(numNodes-1);
		double maxDensity = 0.40;
		gp.changeDensity(Math.random() * (maxDensity - minDensity) + minDensity);
		adjacencyMatrix = gp.transform(adjacencyMatrix);
		while (AlgebraicConnectivity.getInstance().calculate(adjacencyMatrix) <= 0){
			adjacencyMatrix = gp.transform(adjacencyMatrix);
		}
		Integer[] variables = new Integer[solution.getDecisionVariables().length];
		int index = 0;
		for (int j = 0; j < adjacencyMatrix.length; j++) {
			for (int k = j + 1; k < adjacencyMatrix.length; k++) {
				variables[index] = adjacencyMatrix[j][k]*3;
				index++;
			}
		}
		int iValue = 0;
		if (Math.random() < 0.10) {
			for (int var = 0; var < solution.getDecisionVariables().length; var++) {
				if (var >= solution.getDecisionVariables().length - 2) {
					if (Math.random() < mutationProbability){
						iValue = (int) (Math.round(Math.random()
								* (solution.getProblem().getUpperLimit(var) - solution.getProblem().getLowerLimit(var))) + solution
								.getProblem().getLowerLimit(var));

						if (iValue < solution.getProblem().getLowerLimit(var))
							iValue = (int) solution.getProblem().getLowerLimit(var);
						else if (iValue > solution.getProblem().getUpperLimit(var))
							iValue = (int) solution.getProblem().getUpperLimit(var);
					} 
				} else {
					iValue = variables[var];
				}
				solution.setValue(var, iValue);
			}
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
}
