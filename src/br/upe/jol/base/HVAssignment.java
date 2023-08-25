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
 * Arquivo: HVAssignment.java
 * ****************************************************************************
 * Histórico de revisões
 * CR		Nome				Data		Descrição
 * ****************************************************************************
 * 064813-P	Danilo Araújo		05/12/2010		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.base;

import br.upe.jol.metrics.Hypervolume;

/**
 * TODO Descrição do tipo
 * 
 * @author Danilo Araújo
 * @since 05/12/2010
 */
public class HVAssignment {
	private static final HVAssignment instance = new HVAssignment();

	private HVAssignment() {
	}

	/**
	 * Seta o valor de crowding distances para todas as soluções do conjunto
	 * <code>SolutionSet</code>.
	 * 
	 * @param solutionSet
	 *            O conjuneto de soluções <code>SolutionSet</code>.
	 * @param nObjs
	 *            Número de objetivos.
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
