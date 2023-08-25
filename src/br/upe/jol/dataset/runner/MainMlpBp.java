/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: Main6Inputs.java
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
import java.util.List;
import java.util.Vector;

import br.grna.PadraoTreinamento;
import br.grna.bp.RedeNeural;
import br.upe.jol.dataset.Dataset;
import br.upe.jol.dataset.OpticalNetDesignVariable;

/**
 * 
 * @author Danilo
 * @since 03/11/2013
 */
public class MainMlpBp {
	/**
	 * 
	 */
	private static final int NUM_PATTERNS_CLASS = 700;

	private static boolean isOutlier(double rna, double erro) {
		boolean outlier = false;
		if (rna < 0.6 && rna > 0.3 && erro > 0.95) {
			outlier = true;
		} else if (rna < 0.3 && rna > 0.1 && erro > 0.75) {
			outlier = true;
		} else if (rna <= 0.1 && erro > 0.5) {
			outlier = true;
		}
		return outlier;
	}

	public static void main(String[] args) {
		double[][] intervalos = new double[][] { { 0.0, 0.001 }, { 0.001, 0.01 }, { 0.01, 0.10 }, { 0.10, 0.20 },
				{ 0.20, 0.30 }, { 0.30, 0.40 }, { 0.40, 0.50 }, { 0.5, 0.60 }, { 0.6, 0.70 }, { 0.70, 0.80 },
				{ 0.80, 0.90 }, { 0.90, 1.00 } };
		int inputs = 8;
		int hidden = 17;
		RedeNeural rn = new RedeNeural(inputs, 1, hidden, false);

		List<OpticalNetDesignVariable> variables = new Vector<>();
		variables.add(OpticalNetDesignVariable.W);
		variables.add(OpticalNetDesignVariable.OXC);
		// variables.add(Variable.NC);
		// variables.add(Variable.AC);
		// variables.add(Variable.AVERAGE_PATH_LENGTH);
		variables.add(OpticalNetDesignVariable.CC);
		// variables.add(Variable.DIAMETER);
		// variables.add(Variable.ENTROPY);
		// variables.add(Variable.W);
		// variables.add(Variable.OXC);
		variables.add(OpticalNetDesignVariable.DENSITY);
		// variables.add(Variable.NC);
		variables.add(OpticalNetDesignVariable.AVERAGE_PATH_LENGTH_PHYSICAL);
		variables.add(OpticalNetDesignVariable.ENTROPY_DFT);

		Dataset ds = new Dataset("C:\\doutorado\\experimentos\\nfsnet_model2\\dataset_200_epsilon", intervalos, rn,
				NUM_PATTERNS_CLASS, variables);
		ds.setValidation(false);
		ds.setNetworkLoad(200);
		ds.populate();

		double[] min = rn.getMinValues();
		double[] max = rn.getMaxValues();

		List<PadraoTreinamento> tr = ds.getPadroesTreinamento();
		List<PadraoTreinamento> t = ds.getPadroesTeste();

		ds = new Dataset("C:\\doutorado\\experimentos\\nfsnet_model2\\dataset_100_epsilon", intervalos, rn,
				NUM_PATTERNS_CLASS, variables);
		ds.setNetworkLoad(100);
		ds.setValidation(false);
		ds.populate();

		tr.addAll(ds.getPadroesTreinamento());
		t.addAll(ds.getPadroesTeste());
		
		for (int i = 0; i < rn.getMinValues().length; i++) {
			if (rn.getMinValues()[i] < min[i]) {
				min[i] = rn.getMinValues()[i];
			}
			if (rn.getMaxValues()[i] > max[i]) {
				max[i] = rn.getMaxValues()[i];
			}
		}

		StringBuffer sb = new StringBuffer();
		String dirBase = "results/mlp/";
		double sumErro = 0;
		double[] erros = new double[30];
		String strRede;
		for (int i = 0; i < 31; i++) {
			rn = new RedeNeural(inputs, 1, hidden, false);
			rn.setMinValues(min);
			rn.setMaxValues(max);
			sumErro = 0;
			strRede = dirBase + "mlp_" + inputs + "_traffic_" + i + "_" + hidden + ".txt";
//			rn.carregarRede("results/mlp/mlp_8_traffic_0_17.txt");
			// System.out.println("Processando " + strRede);
			rn.treinar(tr, t, 500000, 2e-7, 0.02, 0.0, strRede, true);
			for (PadraoTreinamento pt : t) {
				sumErro += rn.calcularErroPadrao(false, pt);
			}
			erros[i] = sumErro / t.size();
			System.out.println(erros[i]);
		}

		int i = 0;
		for (PadraoTreinamento pt : t) {
			sb.append(i).append(" ").append(pt.getSaida()[0]).append(" \n");

			i++;
		}
		try {
			FileWriter fw = new FileWriter("erros-teste-single_class.txt");
			fw.write(sb.toString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void setBounds(List<PadraoTreinamento> padroes, List<PadraoTreinamento> padroes2, RedeNeural rn) {
		int length = padroes.get(0).getEntrada().length;
		double[] minValues = new double[length];
		double[] maxValues = new double[length];

		Arrays.fill(minValues, Double.MAX_VALUE);
		Arrays.fill(maxValues, Double.MIN_VALUE);
		for (PadraoTreinamento padrao : padroes) {
			for (int i = 0; i < length; i++) {
				if (padrao.getEntrada()[i] < minValues[i]) {
					minValues[i] = padrao.getEntrada()[i];
				}
				if (padrao.getEntrada()[i] > maxValues[i]) {
					maxValues[i] = padrao.getEntrada()[i];
				}
			}
		}
		for (PadraoTreinamento padrao : padroes2) {
			for (int i = 0; i < length; i++) {
				if (padrao.getEntrada()[i] < minValues[i]) {
					minValues[i] = padrao.getEntrada()[i];
				}
				if (padrao.getEntrada()[i] > maxValues[i]) {
					maxValues[i] = padrao.getEntrada()[i];
				}
			}
		}
		rn.setMinValues(minValues);
		rn.setMaxValues(maxValues);
	}

	public static void main1(String[] args) {
		double[][] intervalos = new double[][] { { 0.0, 0.25 }, { 0.25, 0.50 }, { 0.50, 0.75 }, { 0.75, 1.00 } };
		RedeNeural rn = new RedeNeural(5, 1, 5, true);
		Dataset ds = new Dataset("C:\\doutorado\\experimentos\\nfsnet_model2", intervalos, rn, NUM_PATTERNS_CLASS);

		ds.populate();

		List<PadraoTreinamento> tr = ds.getPadroesTreinamento();
		List<PadraoTreinamento> t = ds.getPadroesTeste();

		StringBuffer sb = new StringBuffer();
		String dirBase = "results/compl_6nodes/";
		double sumErro = 0;
		double[] erros = new double[13];
		for (int i = 0; i < 13; i++) {
			sumErro = 0;
			rn.carregarRede(dirBase + "mlp_single_class_" + i + ".dat");
			// rn.setAem(new AvaliadorErroPercentual());
			for (PadraoTreinamento pt : t) {
				sumErro += rn.calcularErroPadrao(false, pt);
			}
			erros[i] = sumErro / t.size();
			System.out.println(erros[i]);
		}

		int i = 0;
		for (PadraoTreinamento pt : t) {
			sb.append(i).append(" ").append(pt.getSaida()[0]).append(" \n");

			i++;
		}
		try {
			FileWriter fw = new FileWriter("erros-teste-single_class.txt");
			fw.write(sb.toString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 */
	private static void treinar(String strRede, int neuronios, double min, double max) {
		int numPadroesClasse = NUM_PATTERNS_CLASS;
		List<PadraoTreinamento> padroes1 = new Vector<>();
		List<PadraoTreinamento> padroes2 = new Vector<>();
		List<PadraoTreinamento> padroes3 = new Vector<>();
		List<PadraoTreinamento> padroes4 = new Vector<>();

		List<PadraoTreinamento> padroesTreinamento = new Vector<>();

		RedeNeural rede = new RedeNeural(11, 1, neuronios, true);
		lerDados(padroesTreinamento, rede, Integer.MAX_VALUE, false);

		for (PadraoTreinamento padrao : padroesTreinamento) {
			if (padrao.getSaida()[0] >= min && padrao.getSaida()[0] < max) {
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
		PadraoTreinamento[][] conjuntoPadroes = new PadraoTreinamento[3][];
		popularConjuntoDadosSimples(padroes3, menor, conjuntoPadroes);
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
		double entropyDFT = 0;
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
							entropyDFT = nf.parse(values[14]).doubleValue();
							nfPond = nf.parse(values[15]).doubleValue();

							// remover outliers
							if (algebraicConectivity > 0 && diameter > 0 && diameter < (Integer.MAX_VALUE - 1)
									&& cost < 20141 && pb < 1) {
								padroesTreinamento.add(new PadraoTreinamento(new double[] { w, oxc, naturalConectivity,
										clusteringCoefficient, diameter, entropyDFT }, new double[] { pb }));
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
