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
 * Arquivo: HVAssignment.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * CR		Nome				Data		Descri��o
 * ****************************************************************************
 * 064813-P	Danilo Ara�jo		05/12/2010		Vers�o inicial
 * ****************************************************************************
 */
package br.upe.jol.base;

import br.upe.jol.metrics.Hypervolume;

/**
 * TODO Descri��o do tipo
 * 
 * @author Danilo Ara�jo
 * @since 05/12/2010
 */
public class HVAssignment {
	private static final HVAssignment instance = new HVAssignment();

	private HVAssignment() {
	}

	/**
	 * Seta o valor de crowding distances para todas as solu��es do conjunto
	 * <code>SolutionSet</code>.
	 * 
	 * @param solutionSet
	 *            O conjuneto de solu��es <code>SolutionSet</code>.
	 * @param nObjs
	 *            N�mero de objetivos.
	 */
	public void hvAssignment(SolutionSet<Double> solutionSet) {
		int size = solutionSet.size();
		
		Hypervolume<Double> hv = new Hypervolume<Double>();
		
		double hvTotal = hv.getValue(solutionSet);

		if (size == 0)
			return;

		if (size == 1) {
			solutionSet.get(0).setCrowdingDistance(hvTotal);
			return;
		}

		if (size == 2) {
			solutionSet.get(0).setCrowdingDistance(hvTotal);
			solutionSet.get(1).setCrowdingDistance(hvTotal);
			return;
		}

		SolutionSet<Double> front = new SolutionSet(size);
		for (int i = 0; i < size; i++) {
			front.add(solutionSet.get(i));
		}

		for (int i = 0; i < size; i++)
			solutionSet.get(i).setCrowdingDistance(0.0);

		for (Solution<Double> sol : solutionSet.getSolutionsList()){
			front.getSolutionsList().remove(sol);
			sol.setCrowdingDistance(hvTotal - hv.getValue(front));
			front.add(sol);
		}
	}

	public void iHvAssignment(SolutionSet<Integer> solutionSet) {
		int size = solutionSet.size();
		
		Hypervolume<Integer> hv = new Hypervolume<Integer>();
		
		double hvTotal = hv.getValue(solutionSet);

		if (size == 0)
			return;

		if (size == 1) {
			solutionSet.get(0).setCrowdingDistance(hvTotal);
			return;
		}

		if (size == 2) {
			solutionSet.get(0).setCrowdingDistance(hvTotal);
			solutionSet.get(1).setCrowdingDistance(hvTotal);
			return;
		}

		SolutionSet<Integer> front = new SolutionSet(size);
		for (int i = 0; i < size; i++) {
			front.add(solutionSet.get(i));
		}

		for (int i = 0; i < size; i++)
			solutionSet.get(i).setCrowdingDistance(0.0);

		for (Solution<Integer> sol : solutionSet.getSolutionsList()){
			front.getSolutionsList().remove(sol);
			sol.setCrowdingDistance(hvTotal - hv.getValue(front));
			front.add(sol);
		}
	}

	public static HVAssignment getInstance() {
		return instance;
	}
}
