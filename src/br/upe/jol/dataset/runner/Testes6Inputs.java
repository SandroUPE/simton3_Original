/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: Testes5Inputs.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	03/11/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.dataset.runner;

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
import java.util.Vector;

import br.grna.AvaliadorErroPercentual;
import br.grna.PadraoTreinamento;
import br.grna.bp.RedeNeural;
import br.upe.jol.dataset.Dataset;

/**
 * 
 * @author Danilo
 * @since 03/11/2013
 */
public class Testes6Inputs {
	/**
	 * 
	 */
	private static final int NUM_PATTERNS_CLASS = 2500;

	private static Dataset dataset;

	private static List<String> linhasPadroes = new Vector<>();

	public static void main(String[] args) {
		// gravarErroMed();
		gravarErroMedClassNovo();
	}

	public static void findExample() {
		List<PadraoTreinamento> padroesTreinamento = new Vector<>();
		double bpSimton = 0;
		double bpRede1 = 0;
		double bpRede2 = 0;
		AvaliadorErroPercentual ae = new AvaliadorErroPercentual();
		RedeNeural rede1 = new RedeNeural(6, 1, true);
		rede1.carregarRede("mlp_emq2_" + 24 + "_" + 15 + ".txt");
		boolean achou1 = false;
		boolean achou2 = false;
		lerDados(padroesTreinamento, rede1, 200000, false);
		double minBP = 0.1;
		RedeNeural rede2 = null;
		int i = 0;
		while (!achou1 || !achou2) {
			i = 0;
			lblExt: for (PadraoTreinamento padrao : padroesTreinamento) {
				if (padrao.getSaida()[0] > 0.5) {
					i++;
					continue;
				}
				for (int j = 0; j < 28; j++) {
					rede1 = new RedeNeural(8, 1, true);
					rede1.carregarRede("mlp_emq2_" + j + "_" + 15 + ".txt");
					for (int k = 0; k < 28; k++) {
						// System.out.println("Rede1 = " + j + "; Rede2 = " +
						// k);
						rede2 = new RedeNeural(8, 1, true);
						rede2.carregarRede("mlp_emq2_" + k + "_" + 20 + ".txt");
						bpSimton = padrao.getSaida()[0];
						bpRede1 = rede1.obterSaida(padrao.getEntrada(), true);
						bpRede2 = rede2.obterSaida(padrao.getEntrada(), true);

						if (bpSimton > .25 && bpSimton < 0.5 && ae.calcular(bpSimton, bpRede1) < 0.1
								&& ae.calcular(bpSimton, bpRede2) < 0.1 && !achou1) {
							System.out.println("Exemplo 1");
							System.out.println(linhasPadroes.get(i));
							System.out.printf("BPsimton = %.8f; BPrede1 = %.8f; BPrede2 = %.8f \n", bpSimton, bpRede1,
									bpRede2);
							System.out.printf("Rede 1 = %d; Rede 2 = %d\n", j, k);
							achou1 = true;
						}

						if (bpSimton < minBP && ae.calcular(bpSimton, bpRede1) < 0.2
								&& ae.calcular(bpSimton, bpRede2) > 0.25 && !achou2) {
							System.out.println("Exemplo 2");
							System.out.println(linhasPadroes.get(i));
							System.out.printf("BPsimton = %.8f; BPrede1 = %.8f; BPrede2 = %.8f \n", bpSimton, bpRede1,
									bpRede2);
							System.out.printf("Rede 1 = %d; Rede 2 = %d\n", j, k);
							achou2 = true;
						}
						if (achou1 && achou2) {
							break lblExt;
						}
					}
				}
				i++;
			}
			minBP += 0.01;
		}

	}

	/**
	 * 
	 */
	private static void gravarErroMed() {
		String sufixo = "_emq2";
		StringBuffer content = new StringBuffer();
		double erroMed = 0;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(8);
		nf.setMinimumFractionDigits(8);

		String inicio = "results/compl_6nodes/";
		for (int l = 0; l < 20; l++) {
			String nomeRede = inicio + "mlp" + sufixo + "_" + l + "_0" + 5 + ".txt";
			erroMed = calculateErroMedio(nomeRede);
			content.append(nf.format(erroMed)).append(" ");

			nomeRede = inicio + "mlp" + sufixo + "_" + l + "_" + 10 + ".txt";
			erroMed = calculateErroMedio(nomeRede);
			content.append(nf.format(erroMed)).append(" ");

			nomeRede = inicio + "mlp" + sufixo + "_" + l + "_" + 15 + ".txt";
			erroMed = calculateErroMedio(nomeRede);
			content.append(nf.format(erroMed)).append(" ");

			nomeRede = inicio + "mlp" + sufixo + "_" + l + "_" + 20 + ".txt";
			erroMed = calculateErroMedio(nomeRede);
			content.append(nf.format(erroMed)).append(" ");

			nomeRede = "mlp_emq_log_estratificado" + "_" + l + "_" + 10 + ".txt";
			erroMed = calculateErroMedio(nomeRede);
			content.append(nf.format(erroMed)).append(" ");

			content.append("\n");
		}

		try {
			FileWriter fw = new FileWriter("results/compl_6nodes/erros-consol-1.txt");
			fw.write(content.toString());
			fw.close();
		} catch (IOException e) {
		}
	}

	/**
	 * 
	 */
	private static void gravarErroMedClass(int net) {
		String sufixo = "_emq2";
		StringBuffer content = new StringBuffer();
		double[] erroMed;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(8);
		nf.setMinimumFractionDigits(8);
		String nomeRede;
		for (int l = 0; l < 30; l++) {
			nomeRede = "mlp" + sufixo + "_" + l + "_" + net + ".txt";
			erroMed = calculateErroMedioClass(nomeRede);
			content.append(nf.format(erroMed[0])).append(" ");
			content.append(nf.format(erroMed[1])).append(" ");
			content.append(nf.format(erroMed[2])).append(" ");
			content.append(nf.format(erroMed[3])).append(" ");
			content.append("\n");
		}

		try {
			FileWriter fw = new FileWriter("results/compl_6nodes/erros-consol-class-" + net + ".txt");
			fw.write(content.toString());
			fw.close();
		} catch (IOException e) {
		}
	}

	private static void gravarErroMedClassNovo() {
		String sufixo = "results/lm/mlp_emq_log_estratificado";
//		sufixo = "results/lm/mlp_lm_c2";
		sufixo = "results/mlp/mlp_6_wc_0_10.txt";
		StringBuffer content = new StringBuffer();
		double[] erroMed;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(8);
		nf.setMinimumFractionDigits(8);
		String nomeRede;
		for (int l = 0; l <= 9; l++) {
//			nomeRede = sufixo + "_" + l + "_10.txt";
			nomeRede = sufixo;
			System.out.println(nomeRede);
			erroMed = calculateErroMedioClassNovo(nomeRede);
			for (double erro : erroMed) {
				content.append(nf.format(erro)).append(" ");
			}
			content.append("\n");
		}

		try {
			FileWriter fw = new FileWriter("results/lm/erros-consol-class-estrat.txt");
//			FileWriter fw = new FileWriter("results/compl_6nodes/erros-consol-class-estrat.txt");
			fw.write(content.toString());
			fw.close();
		} catch (IOException e) {
		}
	}

	/**
	 * @param nomeRede
	 * @return
	 */
	private static double calculateErroMedio(String nomeRede) {
		double erroMed;
		int numPadroesClasse = 4000;
		RedeNeural rede = new RedeNeural(5, 1, true);
		rede.carregarRede(nomeRede);
		if (dataset == null) {
			dataset = buildPatterns(nomeRede, numPadroesClasse, rede);
		}

		erroMed = rede.getMedia(dataset.getPadroesTeste(), false);
		return erroMed;
	}

	/**
	 * @param nomeRede
	 * @return
	 */
	private static double[] calculateErroMedioClass(String nomeRede) {
		int numPadroesClasse = 4000;
		RedeNeural rede = new RedeNeural(5, 1, true);
		rede.carregarRede(nomeRede);
		if (dataset == null) {
			dataset = buildPatterns(nomeRede, numPadroesClasse, rede);
		}

		return new double[] { rede.getMedia(dataset.getMapPadroesTeste().get(0), false),
				rede.getMedia(dataset.getMapPadroesTeste().get(1), false),
				rede.getMedia(dataset.getMapPadroesTeste().get(2), false),
				rede.getMedia(dataset.getMapPadroesTeste().get(3), false) };
	}

	/**
	 * @param nomeRede
	 * @return
	 */
	private static double[] calculateErroMedioClassNovo(String nomeRede) {
		int numPadroesClasse = 2500;
		RedeNeural rede = new RedeNeural(7, 1, true);
		rede.carregarRede(nomeRede);
		if (dataset == null) {
			dataset = buildPatterns(nomeRede, numPadroesClasse, rede);
		}
		double[] erros = new double[dataset.getIntervals().length];
		for (int i = 0; i < erros.length; i++) {
			erros[i] = rede.getMedia(dataset.getMapPadroesTeste().get(i), false);
		}
		return erros;
	}

	/**
	 * @param nomeRede
	 * @param numPadroesClasse
	 * @param rede
	 * @return
	 */
	private static Dataset buildPatterns(String nomeRede, int numPadroesClasse, RedeNeural rede) {
//		double[][] intervalos = new double[][] { { 0.0, 0.001 }, { 0.001, 0.01 }, { 0.01, 0.10 }, { 0.10, 0.20 },
//				{ 0.20, 0.30 }, { 0.30, 0.40 }, { 0.40, 0.50 }, { 0.5, 0.60 }, { 0.6, 0.70 }, { 0.70, 0.80 },
//				{ 0.80, 0.90 }, { 0.90, 1.00 } };
		double[][] intervalos = new double[][] { { 0.0, 0.01 }, { 0.01, 0.10 }, { 0.10, 0.20 }, { 0.20, 0.30 }, { 0.30, 0.40 }, { 0.40, 0.50 }, { 0.5, 0.60 }, { 0.6, 0.70 }, { 0.70, 0.80 }, { 0.80, 0.90 }, { 0.90, 1.00 } };
		RedeNeural rn = new RedeNeural(6, 1, 6, true);
		Dataset ds = new Dataset("C:\\doutorado\\experimentos\\nfsnet_model2", intervalos, rn, NUM_PATTERNS_CLASS);
		ds.populate();

		return ds;
	}

	/**
	 * 
	 */
	private static void gravarErroDet() {
		String sufixo = "_eqp";
		exibirErro("mlp" + sufixo + "_05.txt", false, "et" + sufixo + "1_05.txt");
		exibirErro("mlp" + sufixo + "_10.txt", false, "et" + sufixo + "1_10.txt");
		exibirErro("mlp" + sufixo + "_15.txt", false, "et" + sufixo + "1_15.txt");
		exibirErro("mlp" + sufixo + "_20.txt", false, "et" + sufixo + "1_20.txt");
		exibirErro("mlp" + sufixo + "_25.txt", false, "et" + sufixo + "1_25.txt");
	}

	/**
	 * 
	 */
	private static void exibirErro(String nomeRede, boolean distance, String arqErro) {
		int numPadroesClasse = NUM_PATTERNS_CLASS;
		List<PadraoTreinamento> padroes1 = new Vector<>();
		List<PadraoTreinamento> padroes2 = new Vector<>();
		List<PadraoTreinamento> padroes3 = new Vector<>();
		List<PadraoTreinamento> padroes4 = new Vector<>();

		List<PadraoTreinamento> padroesTreinamento = new Vector<>();

		RedeNeural rede = new RedeNeural(8, 1, true);
		rede.carregarRede(nomeRede);
		lerDados(padroesTreinamento, rede, 100000, distance);

		for (PadraoTreinamento padrao : padroesTreinamento) {
			if (padrao.getSaida()[0] > 0.75) {
				padroes4.add(padrao);
			} else if (padrao.getSaida()[0] < 0.25) {
				padroes1.add(padrao);
			} else if (padrao.getSaida()[0] > 0.25 && padrao.getSaida()[0] < 0.50) {
				padroes2.add(padrao);
			} else {
				padroes3.add(padrao);
			}
		}
		int menor = padroes4.size();
		if (padroes3.size() < menor) {
			menor = padroes3.size();
		}
		if (padroes2.size() < menor) {
			menor = padroes2.size();
		}
		if (padroes1.size() < menor) {
			menor = padroes1.size();
		}
		if (menor > numPadroesClasse) {
			menor = numPadroesClasse;
		}
		menor /= 2;
		PadraoTreinamento[] padroesTreinamentoClass = new PadraoTreinamento[menor * 4];
		int j = 0;
		int i = 0;
		for (; i < menor; i++) {
			padroesTreinamentoClass[j] = padroes1.get(i);
			padroesTreinamentoClass[j + 1] = padroes2.get(i);
			padroesTreinamentoClass[j + 2] = padroes3.get(i);
			padroesTreinamentoClass[j + 3] = padroes4.get(i);
			j += 4;
		}
		menor /= 2;
		PadraoTreinamento[] padroesValidacaoClass = new PadraoTreinamento[menor * 4];
		j = 0;
		for (; i < menor + padroesTreinamentoClass.length / 4; i++) {
			padroesValidacaoClass[j] = padroes1.get(i);
			padroesValidacaoClass[j + 1] = padroes2.get(i);
			padroesValidacaoClass[j + 2] = padroes3.get(i);
			padroesValidacaoClass[j + 3] = padroes4.get(i);
			j += 4;
		}
		PadraoTreinamento[] padroesTesteClass = new PadraoTreinamento[menor * 4];
		j = 0;
		for (; i < menor + padroesTreinamentoClass.length / 4 + padroesValidacaoClass.length / 4; i++) {
			padroesTesteClass[j] = padroes1.get(i);
			padroesTesteClass[j + 1] = padroes2.get(i);
			padroesTesteClass[j + 2] = padroes3.get(i);
			padroesTesteClass[j + 3] = padroes4.get(i);
			j += 4;
		}
		rede.gravarResultados(padroesTesteClass, true, arqErro);
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
