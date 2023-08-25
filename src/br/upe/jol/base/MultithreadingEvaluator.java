/*
 * *****************************************************************************
 * Copyright (c) 2005
 * Propriedade do Laborat�rio de P&D da Unicap-Itautec
 * Todos os direitos reservados, com base nas leis brasileiras de 
 * copyright
 * Este software � confidencial e de propriedade intelectual do
 * Laborat�rio de P&D da Unicap-Itautec
 * ****************************************************************************
 * Projeto: SIAC - Sistema Itautec de Automa��o Comercial
 * Arquivo: MultithreadingEvaluator.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * CR		Nome				Data		Descri��o
 * ****************************************************************************
 * 064813-P	Danilo Ara�jo		20/02/2011		Vers�o inicial
 * ****************************************************************************
 */
package br.upe.jol.base;

import br.upe.jol.problems.simton.SimtonProblem;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * TODO Descri��o do tipo
 * 
 * @author Danilo Ara�jo
 * @since 20/02/2011
 */
public class MultithreadingEvaluator implements Runnable{
	private SolutionONTD solution;
	private boolean evaluated;
	private MultithreadSolutionSet parent;
	
	public MultithreadingEvaluator(SolutionONTD solution, MultithreadSolutionSet parent) {
		this.solution = solution;
		this.parent = parent;
	}
	
	public void run() {
		Problem<Integer> problem = new SimtonProblem(14, 2);
		
		problem.evaluateConstraints(solution);
		problem.evaluate(solution);
		parent.addEvaluated(solution);
	}
	
}
