/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: DatasetCorrelationExperiment.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	14/11/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.dataset.experiments;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import br.grna.PadraoTreinamento;
import br.grna.PbComparator;
import br.grna.bp.RedeNeural;
import br.upe.jol.dataset.Dataset;

/**
 * 
 * @author Danilo
 * @since 14/11/2013
 */
public class DatasetCorrelationExperiment extends Dataset {
	/**
	 * Construtor da classe.
	 * 
	 * @param datasetPath
	 * @param intervals
	 * @param redeNeural
	 * @param samples
	 */
	public DatasetCorrelationExperiment(String datasetPath, double[][] intervals, RedeNeural redeNeural, int samples) {
		super(datasetPath, intervals, redeNeural, samples);
	}

	public void populate() {
		List<PadraoTreinamento> padroes = new Vector<>();
		String linha = null;
		String[] values = null;
		NumberFormat nf = NumberFormat.getInstance();
		int[] samplesCounter = new int[intervals.length];
		double pb = 0;
		double w = 0;
		double oxc = 0;
		double algebraicConectivity = 0;
		double naturalConectivity = 0;
		double averagePathLength = 0;
		double averagePathLengthPhysical = 0;
		double clusteringCoefficient = 0;
		double diameter = 0;
		double diameterPhysical = 0;
		double entropyDFT = 0;
		double nfPond = 0;
		double entropy = 0;
		double density = 0;
		double cost = 0;
		double averageDegree = 0;
		double pdft = 0;
		double sr = 0;
		double sat = 0;
		int count = 0;
		File dirBase = new File(datasetPath);
		File subDir = null;
		FileReader fr = null;
		LineNumberReader lnr = null;
		Set<String> redesProcessadas = new HashSet<>();
		String hash = "";
		int repetidos = 0;
		int distintos = 0;

		for (int classe = 0; classe < intervals.length; classe++) {
			mapPadroesTeste.put(classe, new Vector<PadraoTreinamento>());
			mapPadroesTreinamento.put(classe, new Vector<PadraoTreinamento>());
			mapPadroesValidacao.put(classe, new Vector<PadraoTreinamento>());
		}

		lblExt: for (String dir : dirBase.list()) {
			subDir = new File(dirBase.getAbsolutePath() + File.separator + dir);
			if (subDir.isDirectory()) {
				for (String arq : subDir.list()) {
					try {
						fr = new FileReader(new File(subDir.getAbsolutePath() + File.separator + arq));
						lnr = new LineNumberReader(fr);
						linha = lnr.readLine();
						while (linha != null) {
							values = linha.split(";");
							cost = nf.parse(values[0]).doubleValue();
							pb = nf.parse(values[1]).doubleValue();

							w = nf.parse(values[2]).doubleValue();
							oxc = nf.parse(values[3]).doubleValue();
							algebraicConectivity = nf.parse(values[4]).doubleValue();
							naturalConectivity = nf.parse(values[5]).doubleValue();
							density = nf.parse(values[6]).doubleValue(); // density
							averageDegree = nf.parse(values[7]).doubleValue(); // averageDegree
							averagePathLength = nf.parse(values[8]).doubleValue();
							clusteringCoefficient = nf.parse(values[9]).doubleValue();
							diameter = nf.parse(values[10]).doubleValue();
							entropy = nf.parse(values[11]).doubleValue();
							diameterPhysical = nf.parse(values[12]).doubleValue();
							averagePathLengthPhysical = nf.parse(values[13]).doubleValue();
							nfPond = nf.parse(values[15]).doubleValue();
							entropyDFT = nf.parse(values[14]).doubleValue();
							sr = nf.parse(values[16]).doubleValue();
							pdft = nf.parse(values[17]).doubleValue();
							sat = nf.parse(values[18]).doubleValue();
							String[] strLC = values[21].split(" ");
							List<Double> orderedCloseness = new Vector<>();
							for (String lcv : strLC) {
								orderedCloseness.add(Integer.parseInt(lcv) * 1.0);
							}
							Collections.sort(orderedCloseness);
							double maxLc = orderedCloseness.get(orderedCloseness.size() - 1);
							double minLc = orderedCloseness.get(orderedCloseness.size() - 1);
							double countLc = 0;
							for (int i = 0; i < orderedCloseness.size(); i++) {
								if (orderedCloseness.get(i) > 0) {
									countLc++;
									if (orderedCloseness.get(i) < minLc) {
										minLc = orderedCloseness.get(i);
									}
								}
							}

							hash = values[19] + ";" + w + ";" + oxc;
							if (!redesProcessadas.contains(hash)) {
								distintos++;
								// remover outliers
								if (algebraicConectivity > 0 && diameter > 0 && diameter < (Integer.MAX_VALUE - 1)
										&& cost < 20141 && pb < 1) {
									for (int classe = 0; classe < intervals.length; classe++) {
										if (pb > intervals[classe][0] && pb <= intervals[classe][1]) {
											if (samplesCounter[classe] < samples) {
												samplesCounter[classe]++;
//												padroes.add(new PadraoTreinamento(new double[] { w, oxc,
//														clusteringCoefficient, density, entropyDFT, nfPond,
//														algebraicConectivity, naturalConectivity, averageDegree,
//														averagePathLength, diameter, entropy, diameterPhysical,
//														averagePathLengthPhysical, sr, sat, (maxLc - minLc)/countLc }, new double[] { pb }));
												padroes.add(new PadraoTreinamento(new double[] { w, oxc,
														clusteringCoefficient, density, entropyDFT,
														algebraicConectivity, naturalConectivity, averageDegree,
														averagePathLength, diameter, entropy, diameterPhysical,
														averagePathLengthPhysical, sr, (maxLc - minLc)/countLc }, new double[] { pb }));
												count++;
												if (count > samples * intervals.length) {
													break lblExt;
												}
											}
										}
									}
								}
								redesProcessadas.add(hash);
							} else {
								repetidos++;
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
		// for (int i = 0; i < samplesCounter.length; i++) {
		// System.out.println(i + " - " + samplesCounter[i]);
		// }
		// normalizar
		minValues = new double[padroes.get(0).getEntrada().length];
		maxValues = new double[padroes.get(0).getEntrada().length];
		Arrays.fill(minValues, Double.MAX_VALUE);
		Arrays.fill(maxValues, Double.MIN_VALUE);
		for (PadraoTreinamento padrao : padroes) {
			for (int i = 0; i < padrao.getEntrada().length; i++) {
				if (padrao.getEntrada()[i] < minValues[i]) {
					minValues[i] = padrao.getEntrada()[i];
				}
				if (padrao.getEntrada()[i] > maxValues[i]) {
					maxValues[i] = padrao.getEntrada()[i];
				}
			}
		}
		if (redeNeural != null) {
			redeNeural.setMinValues(minValues);
			redeNeural.setMaxValues(maxValues);
		}
		for (PadraoTreinamento padrao : padroes) {
			for (int i = 0; i < padrao.getEntrada().length; i++) {
				padrao.setValorEntrada(i, (padrao.getEntrada()[i] - minValues[i]) / (maxValues[i] - minValues[i]));
			}
		}
		Collections.sort(padroes, new PbComparator());
		padroesTreinamento = new Vector<>();
		padroesTeste = new Vector<>();
		padroesValidacao = new Vector<>();
		for (int i = 0; i < padroes.size() - padroes.size() % 4; i += 4) {
			padroesTreinamento.add(padroes.get(i));
			if (validation) {
				padroesValidacao.add(padroes.get(i + 1));
			} else {
				padroesTreinamento.add(padroes.get(i + 1));
			}
			padroesTeste.add(padroes.get(i + 2));
			padroesTreinamento.add(padroes.get(i + 3));
			for (int classe = 0; classe < intervals.length; classe++) {
				if (padroes.get(i).getSaida()[0] > intervals[classe][0]
						&& padroes.get(i).getSaida()[0] <= intervals[classe][1]) {
					mapPadroesTreinamento.get(classe).add(padroes.get(i));
				}
				if (padroes.get(i + 1).getSaida()[0] > intervals[classe][0]
						&& padroes.get(i + 1).getSaida()[0] <= intervals[classe][1]) {
					if (validation) {
						mapPadroesValidacao.get(classe).add(padroes.get(i + 1));
					} else {
						mapPadroesTreinamento.get(classe).add(padroes.get(i + 1));
					}
				}
				if (padroes.get(i + 2).getSaida()[0] > intervals[classe][0]
						&& padroes.get(i + 2).getSaida()[0] <= intervals[classe][1]) {
					mapPadroesTeste.get(classe).add(padroes.get(i + 2));
				}
				if (padroes.get(i + 3).getSaida()[0] > intervals[classe][0]
						&& padroes.get(i + 3).getSaida()[0] <= intervals[classe][1]) {
					mapPadroesTreinamento.get(classe).add(padroes.get(i + 3));
				}
			}
		}
	}
}
