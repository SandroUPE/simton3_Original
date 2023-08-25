/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: ExperimentoEstratificacao.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	02/08/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.dataset.experiments;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import br.grna.PadraoTreinamento;
import br.grna.bp.RedeNeural;

/**
 * 
 * @author Danilo
 * @since 02/08/2013
 */
public class ExperimentoEstratificacao {
	private static PadraoTreinamento[][] conjuntoPadroes;
	/**
	 * 	
	 */
	private static final int NUM_PATTERNS_CLASS = 8000;

	public static void main(String[] args) {
		String sufixo = "results/physical/faixa_pb_0/mlp_emq_physical";

		for (int i = 8; i < 30; i++) {
			treinar(sufixo + "_" + i + "_" + 15 + ".txt", 15);
		}
		
//		gravarErroMedClassNovo();
	}

	private static void gravarErroMedClassNovo() {
		String sufixo = "results/physical/faixa_pb";
		// sufixo = "results/backup_20130802/mlp_emq2";
		StringBuffer content = new StringBuffer();
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(8);
		nf.setMinimumFractionDigits(8);
		String nomeRede;

		double[][] intervalos = new double[][] { { 0.0, 0.1 }, { 0.1, 0.2 }, { 0.2, 0.3 }, { 0.3, 0.4 }, { 0.4, 0.5 },
				{ 0.5, 0.6 }, { 0.6, 0.7 }, { 0.7, 0.8 }, { 0.8, 0.9 }, { 0.9, 1.0 } };

		PadraoTreinamento[][][] padroes = new PadraoTreinamento[intervalos.length][][];

		for (int i = 0; i < intervalos.length; i++) {
			padroes[i] = prepararPadroes(intervalos[i][0], intervalos[i][1]);
		}

		for (int c = 1; c < 10; c++) {
			for (int l = 0; l < 29; l++) {
				nomeRede = sufixo + "_" + c + "\\mlp_emq_physical_" + l + "_" + 15 + ".txt";
				System.out.println(nomeRede);

				RedeNeural rede = new RedeNeural(11, 1, true);
				rede.carregarRede(nomeRede);
				content.append(nf.format(rede.getMedia(padroes[c][2], false))).append(" ");
			}
			content.append("\n");
		}

		try {
			FileWriter fw = new FileWriter("results/physical/erros-consol-class-estrat.txt");
			fw.write(content.toString());
			fw.close();
		} catch (IOException e) {
		}
	}

	/**
	 * @param nomeRede
	 * @return
	 */
	private static double calculateErroMedioClassNovo(String nomeRede, PadraoTreinamento[] padroes) {
		RedeNeural rede = new RedeNeural(11, 1, true);
		rede.carregarRede(nomeRede);
		return rede.getMedia(padroes, false);
	}

	private static PadraoTreinamento[][] prepararPadroes(double min, double max) {
		int numPadroesClasse = NUM_PATTERNS_CLASS;
		List<PadraoTreinamento> padroes = new Vector<>();

		List<PadraoTreinamento> padroesTreinamento = new Vector<>();

		RedeNeural rede = new RedeNeural(11, 1, 15, true);
		lerDados(padroesTreinamento, rede, Integer.MAX_VALUE, false);

		for (PadraoTreinamento padrao : padroesTreinamento) {
			if (padrao.getSaida()[0] >= min && padrao.getSaida()[0] < max) {
				padroes.add(padrao);
			}
		}
		int menor = padroes.size();
		if (menor > numPadroesClasse) {
			menor = numPadroesClasse;
		}
		menor /= 2;
		PadraoTreinamento[][] conjuntoPadroes = new PadraoTreinamento[3][];
		popularConjuntoDadosSimples(padroes, menor, conjuntoPadroes);

		return conjuntoPadroes;
	}

	/**
	 * 
	 */
	private static void treinar(String strRede, int neuronios) {
		int numPadroesClasse = NUM_PATTERNS_CLASS;
		List<PadraoTreinamento> padroes3 = new Vector<>();

		List<PadraoTreinamento> padroesTreinamento = new Vector<>();

		RedeNeural rede = new RedeNeural(11, 1, neuronios, true);
		lerDados(padroesTreinamento, rede, Integer.MAX_VALUE, false);

		for (PadraoTreinamento padrao : padroesTreinamento) {
			if (padrao.getSaida()[0] > 0.0 && padrao.getSaida()[0] <= 0.01) {
				padroes3.add(padrao);
			}
			// if (padrao.getSaida()[0] > 0.75) {
			// padroes4.add(padrao);
			// } else if (padrao.getSaida()[0] > 0.50) {
			// padroes1.add(padrao);
			// } else if (padrao.getSaida()[0] > 0.25 && padrao.getSaida()[0] <
			// 0.50) {
			// padroes2.add(padrao);
			// } else {
			// padroes3.add(padrao);
			// }
			// if (padrao.getSaida()[0] > 0.10) {
			// padroes4.add(padrao);
			// } else if (padrao.getSaida()[0] > 0.01) {
			// padroes1.add(padrao);
			// } else if (padrao.getSaida()[0] > 0.001 && padrao.getSaida()[0] <
			// 0.01) {
			// padroes2.add(padrao);
			// } else {
			// padroes3.add(padrao);
			// }
		}
		// int menor = padroes4.size();
		// if (padroes3.size() < menor) {
		// menor = padroes3.size();
		// }
		// if (padroes2.size() < menor) {
		// menor = padroes2.size();
		// }
		// if (padroes1.size() < menor) {
		// menor = padroes1.size();
		// }
		// if (menor > numPadroesClasse) {
		// menor = numPadroesClasse;
		// }
		int menor = padroes3.size();
		if (menor > numPadroesClasse) {
			menor = numPadroesClasse;
		}
		menor /= 2;
		if (conjuntoPadroes == null){
			conjuntoPadroes = new PadraoTreinamento[3][];
			popularConjuntoDadosSimples(padroes3, menor, conjuntoPadroes);	
		}
		rede.treinar(conjuntoPadroes[0], conjuntoPadroes[1], conjuntoPadroes[2], conjuntoPadroes[0].length * 50,
				0.000001, 0.05, 0.01, strRede);
		rede.salvarRede(strRede);
	}

	/**
	 * @param padroes1
	 * @param padroes2
	 * @param padroes3
	 * @param padroes4
	 * @param menor
	 * @param conjuntoPadroes
	 */
	private static void popularConjuntoDadosCompl(List<PadraoTreinamento> padroes1, List<PadraoTreinamento> padroes2,
			List<PadraoTreinamento> padroes3, List<PadraoTreinamento> padroes4, int menor,
			PadraoTreinamento[][] conjuntoPadroes) {
		conjuntoPadroes[0] = new PadraoTreinamento[menor * 4];
		int j = 0;
		int i = 0;
		for (; i < menor; i++) {
			conjuntoPadroes[0][j] = padroes1.get(i);
			conjuntoPadroes[0][j + 1] = padroes2.get(i);
			conjuntoPadroes[0][j + 2] = padroes3.get(i);
			conjuntoPadroes[0][j + 3] = padroes4.get(i);
			j += 4;
		}
		menor /= 2;
		conjuntoPadroes[1] = new PadraoTreinamento[menor * 4];
		j = 0;
		for (; i < menor + conjuntoPadroes[0].length / 4; i++) {
			conjuntoPadroes[1][j] = padroes1.get(i);
			conjuntoPadroes[1][j + 1] = padroes2.get(i);
			conjuntoPadroes[1][j + 2] = padroes3.get(i);
			conjuntoPadroes[1][j + 3] = padroes4.get(i);
			j += 4;
		}
		conjuntoPadroes[2] = new PadraoTreinamento[menor * 4];
		j = 0;
		for (; i < menor + conjuntoPadroes[0].length / 4 + conjuntoPadroes[1].length / 4; i++) {
			conjuntoPadroes[2][j] = padroes1.get(i);
			conjuntoPadroes[2][j + 1] = padroes2.get(i);
			conjuntoPadroes[2][j + 2] = padroes3.get(i);
			conjuntoPadroes[2][j + 3] = padroes4.get(i);
			j += 4;
		}
	}

	/**
	 * @param padroes1
	 * @param padroes2
	 * @param padroes
	 * @param padroes4
	 * @param menor
	 * @param conjuntoPadroes
	 */
	private static void popularConjuntoDadosSimples(List<PadraoTreinamento> padroes, int menor,
			PadraoTreinamento[][] conjuntoPadroes) {
		conjuntoPadroes[0] = new PadraoTreinamento[menor];
		int j = 0;
		int i = 0;
		for (; i < menor; i++) {
			conjuntoPadroes[0][i] = padroes.get(i);
		}
		menor /= 2;
		conjuntoPadroes[1] = new PadraoTreinamento[menor];
		j = 0;
		for (; i < menor + conjuntoPadroes[0].length; i++) {
			conjuntoPadroes[1][j] = padroes.get(i);
			j += 1;
		}
		conjuntoPadroes[2] = new PadraoTreinamento[menor];
		j = 0;
		for (; i < menor + conjuntoPadroes[0].length + conjuntoPadroes[1].length; i++) {
			conjuntoPadroes[2][j] = padroes.get(i);
			j += 1;
		}
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
		double clusteringCoefficient = 0;
		double diameter = 0;
		double diameterPhysical = 0;
		double nfPond = 0;
		double entropy = 0;
		double cost = 0;
		int count = 0;
		File dirBase = new File("C:\\doutorado\\experimentos\\nfsnet_model2");
		File subDir = null;
		FileReader fr = null;
		LineNumberReader lnr = null;
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
							nfPond = nf.parse(values[14]).doubleValue();

							// remover outliers
							if (algebraicConectivity > 0 && diameter > 0 && diameter < (Integer.MAX_VALUE - 1)
									&& cost < 20141 && pb < 1) {
								padroesTreinamento.add(new PadraoTreinamento(new double[] { w, oxc,
										algebraicConectivity, naturalConectivity, averagePathLength,
										clusteringCoefficient, diameter, entropy, diameterPhysical,
										averagePathLengthPhysical, nfPond }, new double[] { pb }));
								count++;
								if (count > maxPadroes) {
									break lblExt;
								}
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
		// System.out.println("Máx/min values:");
		// for (int i = 0; i < minValues.length; i++) {
		// System.out.printf("%.4f %.4f\n", minValues[i], maxValues[i]);
		// }
		// System.out.println();
	}
}
