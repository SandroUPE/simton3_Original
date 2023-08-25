/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: ComitteeMachineExperiment.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	25/12/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.dataset;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.grna.PadraoTreinamento;
import br.grna.bp.RedeNeural;

/**
 * 
 * @author Danilo
 * @since 25/12/2013
 */
public class ComitteeMachineExperiment {
	private static final int NUM_PATTERNS_CLASS = 2000;

	public static void main(String[] args) {
		gravarErroMedClassIndividual();
	}

	private static void gravarErroMedClassIndividual() {
		String[] experts = new String[11];
		String booster = "results/lm/mlp_lm_6i_wnf_0_10.txt";
		for (int i = 0; i < experts.length; i++) {
			experts[i] = "results/mlp/mlp_6_c" + (i+1) + "_0_10.txt";
		}
		CommitteeMachine cm = new CommitteeMachine(booster, experts, 6, 10, 1);
		StringBuffer content = new StringBuffer();
		double[][] erroMed;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(8);
		nf.setMinimumFractionDigits(8);

		erroMed = calculateErroBoostingAndMean(cm);

		for (double[] vErro : erroMed) {
			for (double erro : vErro) {
				content.append(nf.format(erro)).append(" ");
			}
			content.append("\n");
		}

		try {
			// FileWriter fw = new
			// FileWriter("results/compl_6nodes/erros-indivudual-class-estrat.txt");
			FileWriter fw = new FileWriter("results/mlp/erros-cm-class-estrat-boosting-and-mean.txt");
			fw.write(content.toString());
			fw.close();
		} catch (IOException e) {
		}
	}

	/**
	 * @param nomeRede
	 * @return
	 */
	private static double[][] calculateErroMeanSet(CommitteeMachine cm) {
		Dataset dataset = buildPatterns(NUM_PATTERNS_CLASS, cm.getBoosterRna());

		double[][] erros = new double[dataset.getMapPadroesTeste().get(1).size()][dataset.getIntervals().length];
		int j = 0;
		for (int i = 0; i < dataset.getIntervals().length; i++) {
			j = 0;
			for (PadraoTreinamento pt : dataset.getMapPadroesTeste().get(i)) {
				erros[j][i] = Math.pow(pt.getSaida()[0] - cm.getOutputByMeanSet(pt.getEntrada()), 2);
				System.out.println(pt.getSaida()[0] + " ; " + cm.getOutputByMeanSet(pt.getEntrada()));
				j++;
			}
		}

		return erros;
	}

	/**
	 * @param nomeRede
	 * @return
	 */
	private static double[][] calculateErroBoosting(CommitteeMachine cm) {
		Dataset dataset = buildPatterns(NUM_PATTERNS_CLASS, cm.getBoosterRna());

		double[][] erros = new double[dataset.getMapPadroesTeste().get(1).size()][dataset.getIntervals().length];
		int j = 0;
		for (int i = 0; i < dataset.getIntervals().length; i++) {
			j = 0;
			for (PadraoTreinamento pt : dataset.getMapPadroesTeste().get(i)) {
				erros[j][i] = Math.pow(pt.getSaida()[0] - cm.getOutputByBoosting(pt.getEntrada()), 2);
				System.out.println(pt.getSaida()[0] + " ; " + cm.getOutputByBoosting(pt.getEntrada()));
				j++;
			}
		}

		return erros;
	}

	/**
	 * @param nomeRede
	 * @return
	 */
	private static double[][] calculateErroBoostingAndMean(CommitteeMachine cm) {
		Dataset dataset = buildPatterns(NUM_PATTERNS_CLASS, cm.getBoosterRna());

		double[][] erros = new double[dataset.getMapPadroesTeste().get(1).size()][dataset.getIntervals().length];
		int j = 0;
		for (int i = 0; i < dataset.getIntervals().length; i++) {
			j = 0;
			for (PadraoTreinamento pt : dataset.getMapPadroesTeste().get(i)) {
				erros[j][i] = Math.pow(pt.getSaida()[0] - cm.getOutputByBoostingAndMean(pt.getEntrada()), 2);
				System.out.println(pt.getSaida()[0] + " ; " + cm.getOutputByBoostingAndMean(pt.getEntrada()));
				j++;
			}
		}

		return erros;
	}


	/**
	 * @param nomeRede
	 * @param numPadroesClasse
	 * @param rede
	 * @return
	 */
	private static Dataset buildPatterns(int numPadroesClasse, RedeNeural rede) {
		double[][] intervalos = new double[][] { { 0.20, 0.21 }, { 0.21, 0.22 }, { 0.22, 0.23 }, { 0.23, 0.24 },
				{ 0.24, 0.25 }, { 0.25, 0.26 }, { 0.26, 0.27 }, { 0.27, 0.28 }, { 0.28, 0.29 }, { 0.29, 0.3 } };
		Dataset ds = new Dataset("C:\\doutorado\\experimentos\\nfsnet_model2", intervalos, rede, NUM_PATTERNS_CLASS);
		ds.populate();

		return ds;
	}

	/**
	 * @param padroesTreinamento
	 * @param padroesValidacao
	 */
	private static void lerDados(List<PadraoTreinamento> padroesTreinamento, RedeNeural rede, int maxPadroes,
			boolean distancia) {
		String linha = null;
		String[] values = null;
		NumberFormat nf = NumberFormat.getInstance();

		double pb = 0;
		double w = 0;
		double oxc = 0;
		double algebraicConectivity = 0;
		double naturalConectivity = 0;
		double density = 0;
		double averageDegree = 0;
		double averagePathLength = 0;
		double averagePathLengthPhysical = 0;
		double entropyDFT = 0;
		double clusteringCoefficient = 0;
		double diameter = 0;
		double diameterPhysical = 0;
		double nfPond = 0;
		double entropy = 0;
		String hash = "";
		double cost = 0;
		Set<String> redesProcessadas = new HashSet<>();
		int count = 0;
		File dirBase = new File("C:\\doutorado\\experimentos\\nfsnet_model2");
		File subDir = null;
		FileReader fr = null;
		LineNumberReader lnr = null;
		int repetidas = 0;
		int nrepetidas = 0;
		lblExt: for (String dir : dirBase.list()) {
			subDir = new File(dirBase.getAbsolutePath() + File.separator + dir);
			if (subDir.isDirectory()) {
				for (String arq : subDir.list()) {
					try {
						fr = new FileReader(new File(subDir.getAbsolutePath() + File.separator + arq));
						lnr = new LineNumberReader(fr);
						linha = lnr.readLine();
						while (linha != null) {
							// System.out.println(linha);
							values = linha.split(";");
							cost = nf.parse(values[0]).doubleValue();
							pb = nf.parse(values[1]).doubleValue();

							w = nf.parse(values[2]).doubleValue();
							oxc = nf.parse(values[3]).doubleValue();
							algebraicConectivity = nf.parse(values[4]).doubleValue();
							naturalConectivity = nf.parse(values[5]).doubleValue();
							density = nf.parse(values[6]).doubleValue();
							averageDegree = nf.parse(values[7]).doubleValue();
							averagePathLength = nf.parse(values[8]).doubleValue();
							clusteringCoefficient = nf.parse(values[9]).doubleValue();
							diameter = nf.parse(values[10]).doubleValue();
							entropy = nf.parse(values[11]).doubleValue();
							diameterPhysical = nf.parse(values[12]).doubleValue();
							averagePathLengthPhysical = nf.parse(values[13]).doubleValue();
							entropyDFT = nf.parse(values[14]).doubleValue();
							nfPond = nf.parse(values[15]).doubleValue();
							hash = values[16] + ";" + w + ";" + oxc;
							if (!redesProcessadas.contains(hash)) {
								redesProcessadas.add(hash);
								nrepetidas++;
								// remover outliers
								if (algebraicConectivity > 0 && diameter > 0 && diameter < (Integer.MAX_VALUE - 1)
										&& cost < 20141 && pb < 1) {
									padroesTreinamento.add(new PadraoTreinamento(new double[] { w, oxc,
											clusteringCoefficient, density, entropyDFT, nfPond }, new double[] { pb }));
									count++;
									if (count > maxPadroes) {
										break lblExt;
									}
								}
							} else {
								repetidas++;
							}

							linha = lnr.readLine();
						}

						fr.close();
						lnr.close();

					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}
		}
		System.out.println("Repetidas = " + repetidas);
		System.out.println("Não Repetidas = " + nrepetidas);
		// normalizar
		double[] minValues = new double[11];
		double[] maxValues = new double[11];
		Arrays.fill(minValues, Double.MAX_VALUE);
		Arrays.fill(maxValues, Double.MIN_VALUE);
		rede.setMinValues(minValues);
		rede.setMaxValues(maxValues);
		for (PadraoTreinamento padrao : padroesTreinamento) {
			for (int i = 0; i < padrao.getEntrada().length; i++) {
				if (padrao.getEntrada()[i] < minValues[i]) {
					minValues[i] = padrao.getEntrada()[i];
				}
				if (padrao.getEntrada()[i] > maxValues[i]) {
					maxValues[i] = padrao.getEntrada()[i];
				}
			}
		}
		for (PadraoTreinamento padrao : padroesTreinamento) {
			for (int i = 0; i < padrao.getEntrada().length; i++) {
				padrao.setValorEntrada(i, (padrao.getEntrada()[i] - minValues[i]) / (maxValues[i] - minValues[i]));
			}
		}
	}

}
