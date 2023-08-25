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
 * Arquivo: HVComparator.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * CR		Nome				Data		Descri��o
 * ****************************************************************************
 * 064813-P	Danilo Ara�jo		05/12/2010		Vers�o inicial
 * ****************************************************************************
 */
package br.upe.jol.base.comparators;

import java.util.Comparator;

import br.upe.jol.base.Solution;

/**
 * TODO Descri��o do tipo
 * 
 * @author Danilo Ara�jo
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
