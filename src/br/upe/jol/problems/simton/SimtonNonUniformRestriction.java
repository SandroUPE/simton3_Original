/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: SimtonNonUniformRestriction.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	15/01/2014		Vers�o inicial
 * ****************************************************************************
 */
package br.upe.jol.problems.simton;

/**
 * 
 * @author Danilo
 * @since 15/01/2014
 */
public class SimtonNonUniformRestriction extends SimtonProblemNonUniformHub {
	/**
	 * Construtor da classe.
	 * @param numberOfNodes_ppr
	 * @param numberOfObjectives
	 */
	public SimtonNonUniformRestriction(int numberOfNodes_ppr, int numberOfObjectives) {
		super(numberOfNodes_ppr, numberOfObjectives);
		upperLimitObjective[0] = 0.1;
		upperLimitObjective[1] = 20141.7028;
		lowerLimitObjective[0] = 0;
		lowerLimitObjective[1] = 2100;
	}

	public static void main(String[] args) {
		evaluateFixedNetwork();
	}
	
	public static void evaluateFixedNetwork(){
		SimtonProblemNonUniformHub problemNonUniform = new SimtonProblemNonUniformHub(14, 2);
		problemNonUniform.setSimulate(true);
		SimtonProblem problem = new SimtonProblem(14, 2);
		problem.setSimulate(true);
		Integer[] variables = {
				 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7,
				 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0, 7,
				 0, 7, 0, 7, 0, 7, 0, 7, 0, 7, 0,
				 7, 0, 7, 0, 7, 0, 7, 0, 7, 0,
				 7, 0, 7, 0, 7, 0, 7, 0, 7,
				 7, 0, 7, 0, 7, 0, 7, 0,
				 7, 0, 7, 0, 7, 0, 7,
				 0, 7, 0, 7, 0, 7,
				 0, 7, 0, 7, 0,
				 7, 0, 7, 0,
				 0, 7, 0,
				 7, 0,
				 7,
				 5, 40 };
		
		SolutionONTD sol = new SolutionONTD(problem, variables);
		problem.evaluate(sol);
		
//		sol = new SolutionONTD(problemNonUniform, variables);
//		problemNonUniform.evaluate(sol);
	}

}
