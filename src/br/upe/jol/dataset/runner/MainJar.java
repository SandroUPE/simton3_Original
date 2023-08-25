/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: MainJar.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	10/08/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.dataset.runner;

import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;

import br.grna.bp.RedeNeural;
import br.upe.jol.dataset.Dataset;

/**
 * 
 * @author Danilo
 * @since 10/08/2013
 */
public class MainJar {
	private static final int NUM_PATTERNS_CLASS = 8000;

	// private static double[][] DEFAULT_INTERVALS = new double[][] { { 0.0, 0.1
	// }, { 0.1, 0.2 }, { 0.2, 0.3 },
	// { 0.3, 0.4 }, { 0.4, 0.5 }, { 0.5, 0.6 }, { 0.6, 0.7 }, { 0.7, 0.8 }, {
	// 0.8, 0.9 }, { 0.9, 1.0 } };

	private static double[][] DEFAULT_INTERVALS = new double[][] { { 0.1, 0.15 }, { 0.15, 0.2 } };

	private static String DEFAULT_DS_PATH = "C:\\doutorado\\experimentos\\nfsnet_model2";

	private static String DEFAULT_RNA_PATH = "results/physical/compl_11nodes/";

	private static final NumberFormat nf = NumberFormat.getInstance();

	public static void main(String[] args) {
		nf.setMinimumFractionDigits(8);
		nf.setMaximumFractionDigits(8);
		RedeNeural rede = new RedeNeural(11, 1, 15, true);
		StringBuffer eTeste = new StringBuffer();
		Dataset ds = new Dataset(DEFAULT_DS_PATH, DEFAULT_INTERVALS, rede, NUM_PATTERNS_CLASS);
		ds.populate();

		for (int i = 2; i < 20; i++) {
			rede = new RedeNeural(11, 1, 15, true);
			ds.setRnaInputRanges(rede);
			rede.treinar(ds.getPadroesTreinamento(), ds.getPadroesValidacao(), ds.getPadroesTreinamento().size() * 50,
					0.000001, 0.05, 0.01, DEFAULT_RNA_PATH + "mlp_01_03_" + i + ".dat");
			rede.salvarRede(DEFAULT_RNA_PATH);

			eTeste.append(i + " " + nf.format(rede.getMedia(ds.getPadroesTeste(), false))).append("\n");
		}

		try {
			FileWriter fw = new FileWriter(DEFAULT_RNA_PATH + "erro.txt");
			fw.write(eTeste.toString());
			fw.close();
		} catch (IOException e) {
		}
	}
}
