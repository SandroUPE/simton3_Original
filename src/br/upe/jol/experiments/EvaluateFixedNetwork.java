/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: EvaluateFixedNNetwork.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	17/02/2014		Vers�o inicial
 * ****************************************************************************
 */
package br.upe.jol.experiments;

import br.upe.jol.problems.simton.SimtonProblem;
import br.upe.jol.problems.simton.SimtonProblemNonUniformHub;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo
 * @since 17/02/2014
 */
public class EvaluateFixedNetwork {
	public static void main(String[] args) {
		evaluateFixedNetwork();
	}

	public static void evaluateFixedNetwork() {
		SimtonProblem problem = new SimtonProblem(14, 2, 200);
		problem.setSimulate(true);

		// nsfnet original
//		Integer[] variables = { 3, 3, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0,
//				0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 3, 0, 0, 0, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 3, 0,
//				0, 3, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 3, 0, 3, 3, 0, 3, 3, 0, 3, 5, 40 };

		// nsfnet anel
//		 Integer[] variables = {
//		 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//		 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//		 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//		 3, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//		 3, 0, 0, 0, 0, 0, 0, 0, 0,
//		 3, 0, 0, 0, 0, 0, 0, 0,
//		 3, 0, 0, 0, 0, 0, 0,
//		 3, 0, 0, 0, 0, 0,
//		 3, 0, 0, 0, 0,
//		 3, 0, 0, 0,
//		 3, 0, 0,
//		 3, 0,
//		 3,
//		 5, 40 };
		 
		Integer[] variables = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 3, 0, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 3, 3, 3, 0, 0, 0, 3, 3, 3, 3, 0, 3, 0, 0, 0, 0, 3, 0, 3, 3, 3, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 3, 0, 3, 3, 0, 3, 3, 0, 0, 0, 3, 3, 3, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 38};

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 5; i++) {
			SolutionONTD sol = new SolutionONTD(problem, variables);
			problem.evaluate(sol);
			sb.append(String.format("%.5f\n", sol.getObjective(0)));
		}
		System.out.println(sb.toString());
		// problem = new SimtonProblem(14, 2, 80);
		// problem.evaluate(sol);
		// double[] bp1 = new double[8];
		// double[] bp2 = new double[8];
		// int i = 0;
		// for (int oxc = 1; oxc <
		// SimtonProblem.SWITCHES_COSTS_AND_LABELS[0].length; oxc++) {
		// sol.getDecisionVariables()[variables.length-2] = oxc;
		// problem = new SimtonProblem(14, 2, 100);
		// problem.evaluate(sol);
		// i++;
		// }

		// i = 0;
		// for (int networkLoad = 80; networkLoad <= 220; networkLoad += 20) {
		// System.out.println("LOAD = " + networkLoad);
		// problem = new SimtonProblem(14, 2, networkLoad);
		// problem.evaluate(sol);
		// bp1[i] = sol.getObjective(0);
		// i++;
		// }
		// i = 0;
		// variables[variables.length-1] = 20;
		// sol = new SolutionONTD(problem, variables);
		// for (int networkLoad = 80; networkLoad <= 220; networkLoad += 20) {
		// System.out.println("LOAD = " + networkLoad);
		// problem = new SimtonProblem(14, 2, networkLoad);
		// problem.evaluate(sol);
		// bp2[i] = sol.getObjective(0);
		// i++;
		// }
	}

	// Integer[] variables = {
	// 3, 3, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0,
	// 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	// 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0,
	// 3, 0, 0, 0, 0, 0, 3, 0, 0, 0,
	// 3, 3, 0, 0, 0, 0, 0, 0, 0,
	// 0, 0, 0, 3, 0, 0, 0, 3,
	// 3, 0, 3, 0, 0, 0, 0,
	// 3, 0, 0, 0, 0, 0,
	// 3, 0, 3, 3, 3,
	// 0, 0, 0, 0,
	// 3, 3, 0,
	// 3, 3,
	// 3,
	// 4, 40 };
	// Integer[] variables = { 9, 1, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	// 0, 3, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0,
	// 0, 0, 0, 0, 0, 0, 0, 3, 0, 3, 0, 8, 0, 3, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0,
	// 0, 0, 6, 0, 0, 5, 0, 0, 0, 0,
	// 3, 0, 0, 5, 0, 0, 6, 3, 0, 4, 0, 0, 0, 2, 0, 2, 1, 4, 3, 0, 0, 0, 1, 0,
	// 0, 7, 0, 1, 4, 40 };

	// Integer[] variables = {
	// 3, 3, 3, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0,
	// 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0,
	// 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	// 3, 0, 3, 0, 3, 0, 3, 0, 0, 0,
	// 0, 0, 0, 0, 3, 0, 0, 0, 0,
	// 3, 0, 0, 3, 0, 0, 0, 0,
	// 3, 0, 0, 3, 0, 0, 3,
	// 3, 0, 3, 0, 0, 0,
	// 3, 0, 3, 3, 3,
	// 3, 0, 0, 0,
	// 3, 0, 0,
	// 3, 0,
	// 3,
	// 4, 40 };

	// inicio da rede a do exemplo original

	// Integer[] variables = {
	// 3, 3, 3, 0, 3, 3, 0, 0, 0, 0, 0, 0, 0,
	// 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	// 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	// 3, 0, 3, 0, 3, 0, 0, 0, 0, 0,
	// 0, 0, 0, 0, 3, 0, 0, 0, 0,
	// 3, 0, 0, 3, 0, 0, 0, 0,
	// 3, 0, 0, 3, 0, 0, 3,
	// 3, 0, 3, 0, 0, 0,
	// 3, 0, 3, 3, 3,
	// 3, 0, 0, 0,
	// 3, 0, 0,
	// 3, 0,
	// 3,
	// 4, 40 };

	// fim da rede a do exemplo original

	// inicio novo exemplo a
	// Integer[] variables = {
	// 3, 3, 3, 0, 3, 3, 0, 0, 0, 0, 0, 0, 0,
	// 3, 0, 0, 0, 0, 3, 0, 0, 0, 0, 3, 0,
	// 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	// 3, 0, 3, 0, 3, 0, 0, 0, 0, 0,
	// 0, 0, 0, 0, 3, 0, 0, 0, 0,
	// 3, 0, 0, 3, 0, 0, 0, 0,
	// 3, 0, 0, 3, 0, 0, 3,
	// 3, 0, 3, 0, 0, 0,
	// 3, 0, 3, 3, 3,
	// 3, 0, 0, 0,
	// 3, 0, 0,
	// 3, 0,
	// 3,
	// 4, 40 };

	//
	// Integer[] variables = {
	// 3, 3, 3, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0,
	// 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	// 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	// 3, 0, 3, 0, 3, 0, 0, 0, 0, 0,
	// 0, 0, 0, 0, 3, 0, 0, 0, 0,
	// 3, 0, 0, 3, 0, 0, 0, 0,
	// 3, 0, 0, 3, 0, 0, 3,
	// 3, 0, 3, 0, 0, 0,
	// 3, 3, 3, 3, 3,
	// 3, 0, 0, 0,
	// 3, 0, 0,
	// 3, 0,
	// 3,
	// 4, 40 };
}
