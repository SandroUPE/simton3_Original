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
 * Arquivo: MultithreadSolutionSet.java
 * ****************************************************************************
 * Histórico de revisões
 * CR		Nome				Data		Descrição
 * ****************************************************************************
 * 064813-P	Danilo Araújo		20/02/2011		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.base;

import br.upe.jol.problems.simton.SolutionONTD;

/**
 * TODO Descrição do tipo
 * 
 * @author Danilo Araújo
 * @since 20/02/2011
 */
public class MultithreadSolutionSet {
	private SolutionSet<Integer> solutionSet = new SolutionSet<Integer>();
	private int evaluatedCounter;
	private int solutionSetCounter;
	
	public MultithreadSolutionSet(int solutionSetCounter){
		this.solutionSetCounter = solutionSetCounter;
	}
	
	public synchronized void evaluate(SolutionONTD solution){
		MultithreadingEvaluator evaluator = new MultithreadingEvaluator(solution, this);
		new Thread(evaluator).start();
	}
	
	public synchronized void addEvaluated(SolutionONTD solution){
		solutionSet.add(solution);
		evaluatedCounter++;
		notifyAll();
	}
	
	public synchronized SolutionSet<Integer> getSolutionSet(){
		while (evaluatedCounter < solutionSetCounter){
			try {
				wait();
			} catch (InterruptedException e) { }
		}
		return solutionSet;
	}

	public synchronized int getEvaluatedCounter() {
		return evaluatedCounter;
	}
	
}
