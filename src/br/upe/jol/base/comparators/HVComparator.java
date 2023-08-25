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
package br.upe.jol.base.comparators;

import java.util.Comparator;

import br.upe.jol.base.Solution;

/**
 * TODO Descrição do tipo
 * 
 * @author Danilo Araújo
 * @since 05/12/2010
 */
public class HVComparator<T> implements Comparator<Solution<T>> {

	public int compare(Solution<T> solution1, Solution<T> solution2) {
		if (solution1 == null) {
			return 1;
		} else if (solution2 == null) {
			return -1;
		}

		double distance1 = solution1.getCrowdingDistance();
		double distance2 = solution2.getCrowdingDistance();
		if (distance1 > distance2) {
			return -1;
		} else if (distance1 < distance2) {
			return 1;
		}

		return 0;
	}
}
