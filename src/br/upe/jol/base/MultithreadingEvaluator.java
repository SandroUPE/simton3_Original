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
 * Arquivo: MultithreadingEvaluator.java
 * ****************************************************************************
 * Histórico de revisões
 * CR		Nome				Data		Descrição
 * ****************************************************************************
 * 064813-P	Danilo Araújo		20/02/2011		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.base;

import br.upe.jol.problems.simton.SimtonProblem;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * TODO Descrição do tipo
 * 
 * @author Danilo Araújo
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
