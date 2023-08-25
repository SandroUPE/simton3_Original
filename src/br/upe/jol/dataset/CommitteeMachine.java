/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: CommitteeMachine.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	25/12/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.dataset;

import java.text.NumberFormat;

import br.grna.bp.RedeNeural;

/**
 * 
 * @author Danilo
 * @since 25/12/2013
 */
public class CommitteeMachine {
	private RedeNeural boosterRna;

	private RedeNeural[] expertsRna;

	private int inputs;

	private int hidden;

	private int output;

	private static final NumberFormat nf = NumberFormat.getInstance();

	public CommitteeMachine(String pathBoosterRna, String[] experts, int inputs, int hidden, int output) {
		this.inputs = inputs;
		this.hidden = hidden;
		this.output = output;

		boosterRna = getRedeNeural(pathBoosterRna);
		expertsRna = new RedeNeural[experts.length];

		for (int i = 0; i < experts.length; i++) {
			expertsRna[i] = getRedeNeural(experts[i]);
		}
	}

	public double getOutputByMeanSet(double[] input) {
		double output = 0;

		for (RedeNeural rede : expertsRna) {
			output += rede.obterSaida(input);
		}

		return output / expertsRna.length;
	}

	public double getOutputByMedianSet(double[] input) {
		double output = boosterRna.obterSaida(input);
		int expertIndex = 0;
		if (output < 0.01) {
			expertIndex = 0;
		} else if (output < 0.1) {
			expertIndex = 1;
		} else if (output < 0.2) {
			expertIndex = 2;
		} else if (output < 0.3) {
			expertIndex = 3;
		} else if (output < 0.4) {
			expertIndex = 4;
		} else if (output < 0.5) {
			expertIndex = 5;
		} else if (output < 0.6) {
			expertIndex = 6;
		} else if (output < 0.7) {
			expertIndex = 7;
		} else if (output < 0.8) {
			expertIndex = 8;
		} else if (output < 0.9) {
			expertIndex = 9;
		} else {
			expertIndex = 10;
		}

		return expertsRna[expertIndex].obterSaida(input);

	}

	public double getOutputByBoosting(double[] input) {
		double output = boosterRna.obterSaida(input);
		int expertIndex = 0;
		if (output < 0.01) {
			expertIndex = 0;
		} else if (output < 0.1) {
			expertIndex = 1;
		} else if (output < 0.2) {
			expertIndex = 2;
		} else if (output < 0.3) {
			expertIndex = 3;
		} else if (output < 0.4) {
			expertIndex = 4;
		} else if (output < 0.5) {
			expertIndex = 5;
		} else if (output < 0.6) {
			expertIndex = 6;
		} else if (output < 0.7) {
			expertIndex = 7;
		} else if (output < 0.8) {
			expertIndex = 8;
		} else if (output < 0.9) {
			expertIndex = 9;
		} else {
			expertIndex = 10;
		}

		return expertsRna[expertIndex].obterSaida(input);
	}

	public double getOutputByBoostingAndMean(double[] input) {
		double output = boosterRna.obterSaida(input);
		int expertIndex = 0;
		int expert2Index = 0;
		int expert3Index = 0;
		
		if (output < 0.01) {
			expertIndex = 0;
		} else if (output < 0.1) {
			expertIndex = 1;
		} else if (output < 0.2) {
			expertIndex = 2;
		} else if (output < 0.3) {
			expertIndex = 3;
		} else if (output < 0.4) {
			expertIndex = 4;
		} else if (output < 0.5) {
			expertIndex = 5;
		} else if (output < 0.6) {
			expertIndex = 6;
		} else if (output < 0.7) {
			expertIndex = 7;
		} else if (output < 0.8) {
			expertIndex = 8;
		} else if (output < 0.9) {
			expertIndex = 9;
		} else {
			expertIndex = 10;
		}
		
		if (expertIndex == 0) {
			expert2Index = 1;
			expert3Index = 2;
		} else if (expertIndex == 10) {
			expert2Index = 9;
			expert3Index = 8;
		} else {
			expert2Index = expertIndex - 1;
			expert3Index = expertIndex + 1;
		}
		
		double w = 0.15;

		output = (1 - 2 * w) * expertsRna[expertIndex].obterSaida(input) + w * expertsRna[expert2Index].obterSaida(input)
				+ w * expertsRna[expert3Index].obterSaida(input);
		return output;
	}

	public RedeNeural getRedeNeural(String pathRna) {
		RedeNeural rna = new RedeNeural(inputs, output, hidden, true);
		rna.carregarRede(pathRna);
		return rna;

	}

	/**
	 * @return o valor do atributo boosterRna
	 */
	public RedeNeural getBoosterRna() {
		return boosterRna;
	}

	/**
	 * Altera o valor do atributo boosterRna
	 * 
	 * @param boosterRna
	 *            O valor para setar em boosterRna
	 */
	public void setBoosterRna(RedeNeural boosterRna) {
		this.boosterRna = boosterRna;
	}

	/**
	 * @return o valor do atributo expertsRna
	 */
	public RedeNeural[] getExpertsRna() {
		return expertsRna;
	}

	/**
	 * Altera o valor do atributo expertsRna
	 * 
	 * @param expertsRna
	 *            O valor para setar em expertsRna
	 */
	public void setExpertsRna(RedeNeural[] expertsRna) {
		this.expertsRna = expertsRna;
	}
}
