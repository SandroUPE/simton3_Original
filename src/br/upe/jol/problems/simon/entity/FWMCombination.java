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
 * Arquivo: FWMCombination.java
 * ****************************************************************************
 * Histórico de revisões
 * CR		Nome				Data		Descrição
 * ****************************************************************************
 * 064813-P	Danilo Araújo		29/04/2011		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.problems.simon.entity;


/**
 * Estrutura para encapsular 3 comprimentos de onda para avaliação de FWM.
 * 
 * @author Danilo Araújo
 * @since 29/04/2011
 */
public class FWMCombination {
	protected int i;

	protected int j;

	protected int k;

	public FWMCombination() {
	}

	public FWMCombination(int i, int j, int k) {
		super();
		this.i = i;
		this.j = j;
		this.k = k;
	}

	/**
	 * Método acessor para obter o valor do atributo i.
	 *
	 * @return Retorna o atributo i.
	 */
	public int getI() {
		return i;
	}

	/**
	 * Método acessor para modificar o valor do atributo i.
	 *
	 * @param i O valor de i para setar.
	 */
	public void setI(int i) {
		this.i = i;
	}

	/**
	 * Método acessor para obter o valor do atributo j.
	 *
	 * @return Retorna o atributo j.
	 */
	public int getJ() {
		return j;
	}

	/**
	 * Método acessor para modificar o valor do atributo j.
	 *
	 * @param j O valor de j para setar.
	 */
	public void setJ(int j) {
		this.j = j;
	}

	/**
	 * Método acessor para obter o valor do atributo k.
	 *
	 * @return Retorna o atributo k.
	 */
	public int getK() {
		return k;
	}

	/**
	 * Método acessor para modificar o valor do atributo k.
	 *
	 * @param k O valor de k para setar.
	 */
	public void setK(int k) {
		this.k = k;
	}

}
