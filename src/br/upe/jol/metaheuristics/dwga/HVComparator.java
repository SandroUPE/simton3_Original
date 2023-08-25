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
 * Arquivo: HVComparator.java
 * ****************************************************************************
 * Histórico de revisões
 * CR		Nome				Data		Descrição
 * ****************************************************************************
 * 064813-P	Danilo Araújo		05/12/2010		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.metaheuristics.dwga;

import java.util.Comparator;

import br.upe.jol.base.Solution;

/**
 * TODO Descrição do tipo
 * 
 * @author Danilo Araújo
 * @since 05/12/2010
 */
public class HVComparator<T> implements Comparator<Solution<Double>> {

	public int compare(Solution<Double> solution1, Solution<Double> solution2) {
		if (solution1 == null) {
			return 1;
		} else if (solution2 == null) {
			return -1;
		}

		double distance1 = 1/(1+((HVCASolution)solution1).getHvContributionIndividual() + ((HVCASolution)solution1).getVarAngleGroup());
		double distance2 = 1/(1+((HVCASolution)solution2).getHvContributionIndividual() + ((HVCASolution)solution2).getVarAngleGroup());
		System.out.println(solution1.getObjective(0)+" - "+solution1.getObjective(1));
		System.out.println(solution2.getObjective(0)+" - "+solution2.getObjective(1));
		System.out.println("d1 = " + distance1);
		System.out.println("d2 = " + distance2);
		if (distance1 > distance2) {
			return -1;
		} else if (distance1 < distance2) {
			return 1;
		}

		return 0;
	}
}
