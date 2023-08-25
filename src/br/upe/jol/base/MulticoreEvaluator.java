/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: MulticoreEvaluator.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	02/12/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.base;

import java.util.List;
import java.util.concurrent.RecursiveAction;

/**
 * 
 * @author Danilo Araujo
 * @since 02/12/2014
 */
public class MulticoreEvaluator<T> extends RecursiveAction {

	private List<Solution<T>> solutionsList;

	private int start;

	private int length;
	
	private int th;

	/**
	 * Construtor da classe.
	 */
	public MulticoreEvaluator(List<Solution<T>> solutionsList, int start, int length, int th) {
		this.solutionsList = solutionsList;
		this.start = start;
		this.length = length;
		this.th = th;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.RecursiveAction#compute()
	 */
	@Override
	protected void compute() {
		if (length < th) {
			computeDirectly();
			return;
		}
		int split = length / 2;
		invokeAll(new MulticoreEvaluator<T>(solutionsList, start, split, th), new MulticoreEvaluator<T>(solutionsList,
				start + split, length - split, th));
	}

	protected void computeDirectly() {
		for (int i = start; i < start + length; i++) {
			solutionsList.get(i).evaluateObjectives();
		}
	}

}
