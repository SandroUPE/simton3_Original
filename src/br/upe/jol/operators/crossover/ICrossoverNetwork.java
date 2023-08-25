/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: ICrossoverNetwork.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	17/10/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.operators.crossover;

import br.cns.transformations.DegreeMatrix;
import br.upe.jol.base.Scheme;
import br.upe.jol.base.Solution;
import br.upe.jol.metaheuristics.spea2.ISPEA2Solution;
import br.upe.jol.problems.simton.SimtonProblem;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo
 * @since 17/10/2013
 */
public class ICrossoverNetwork extends Crossover<Integer> {
	private static final long serialVersionUID = 11L;

	public ICrossoverNetwork(double crossoverPropability) {
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
			int numNodes = 14;
			Integer[][] am1 = new Integer[numNodes][numNodes];
			Integer[][] am2 = new Integer[numNodes][numNodes];
			int counter = 0;
			for (int i = 0; i < numNodes; i++) {
				am1[i][i] = 0;
				am2[i][i] = 0;
				for (int j = i + 1; j < numNodes; j++) {
					am1[i][j] = variables1[counter];
					am1[j][i] = am1[i][j];
					am2[i][j] = variables2[counter];
					am2[j][i] = am2[i][j];
					counter++;
				}
			}
			Integer[][] d1 = DegreeMatrix.getInstance().transform(am1);
			Integer[][] d2 = DegreeMatrix.getInstance().transform(am2);

			for (int pos = 0; pos < variables1.length; pos++) {
				if (Math.random() > 0.5) {
					if (pos >= variables1.length - 2) {
						offs1.setValue(pos, variables2[pos]);
						offs2.setValue(pos, variables1[pos]);
					} else {
						counter = 0;
						lblExt: for (int i = 0; i < numNodes; i++) {
							for (int j = i + 1; j < numNodes; j++) {
								if (counter == pos && d1[i][i] > 1 && d1[j][j] > 1 && d2[i][i] > 1 && d2[j][j] > 1) {
									offs1.setValue(pos, variables2[pos]);
									offs2.setValue(pos, variables1[pos]);
									break lblExt;
								}
								counter++;
							}
						}
					}
				}
			}
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

			for (int i = 0; i < variables1.length; i++) {
				if (!scheme.getValor()[i].equals(Scheme.STR_CORINGA)) {
					offs1.setValue(i, Integer.valueOf(scheme.getValor()[i]));
					offs2.setValue(i, Integer.valueOf(scheme.getValor()[i]));
				} else if (Math.random() > 0.5) {
					offs1.setValue(i, variables2[i]);
					offs2.setValue(i, variables1[i]);
				}
			}
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
		return "C9";
	}

}
