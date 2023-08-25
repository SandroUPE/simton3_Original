/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: DatasetOutliers.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	04/12/2013		Versão inicial
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
 * @since 04/12/2013
 */
public class DatasetOutliers extends Dataset {
	private RedeNeural rnaRef = null;

	/**
	 * Construtor da classe.
	 * 
	 * @param datasetPath
	 * @param intervals
	 * @param redeNeural
	 * @param samples
	 */
	public DatasetOutliers(String datasetPath, double[][] intervals, RedeNeural redeNeural, int samples) {
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
		PadraoTreinamento pt1 = null;

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
												PadraoTreinamento pt = new PadraoTreinamento(new double[] { w, oxc,
														density, entropyDFT, naturalConectivity,
														averagePathLengthPhysical }, new double[] { pb });
												double erro = rnaRef.calcularErroPadrao(true, pt);
												double out = 0;

												// fuzzificação depende da faixa
												// de PB
												// 1) outlier -> se o valor do erro
												// do padrão é > Q3 + 1,5 x IQR
												// 2) não confiável -> se o valor
												// do erro do padrão é > Q3
												// 3) passível de análise
												// intervalar -> se o valor do
												// erro do padrão está entre Q1
												// e Q3
												// 4) confiável -> se o valor do
												// erro do padrão é abaixo de Q1

												if (pb >= 0.6) {
													if (erro <= 0.000001215) {
														out = 0.0; // muito confiável
													} else if (erro <= 0.00002114 && erro > 0.000001215) {
														out = 0.25;  // confiável
													} else if (erro <= 0.000015615 && erro > 0.00002114) {
														out = 0.50;  // passível de análise intervalar
													} else if (erro <= 0.000037215 && erro > 0.000015615) {
														out = 0.75; // não confiável
													} else if (erro > 0.000037215) {
														out = 1.0; // outlier
													}
												} else if (pb >= 0.5 && pb < 0.6) {
													if (erro <= 0.000015995) {
														out = 0.0; // muito confiável
													} else if (erro <= 0.00007775 && erro > 0.000015995) {
														out = 0.25;  // confiável
													} else if (erro <= 0.00025619 && erro > 0.00007775) {
														out = 0.50;  // passível de análise intervalar
													} else if (erro <= 0.000616483 && erro > 0.00025619) {
														out = 0.75; // não confiável
													} else if (erro > 0.000616483) {
														out = 1.0; // outlier
													}
												} else if (pb >= 0.4 && pb < 0.5) {
													if (erro <= 0.00002419) {
														out = 0.0; // muito confiável
													} else if (erro <= 0.00014244 && erro > 0.00002419) {
														out = 0.25;  // confiável
													} else if (erro <= 0.00037213 && erro > 0.00014244) {
														out = 0.50;  // passível de análise intervalar
													} else if (erro <= 0.000616483 && erro > 0.00037213) {
														out = 0.75; // não confiável
													} else if (erro > 0.00089404) {
														out = 1.0; // outlier
													}
												} else if (pb >= 0.3 && pb < 0.4) {
													if (erro <= 0.000039255) {
														out = 0.0; // muito confiável
													} else if (erro <= 0.00017913 && erro > 0.000039255) {
														out = 0.25;  // confiável
													} else if (erro <= 0.000540785 && erro > 0.00017913) {
														out = 0.50;  // passível de análise intervalar
													} else if (erro <= 0.00129308 && erro > 0.000540785) {
														out = 0.75; // não confiável
													} else if (erro > 0.00129308) {
														out = 1.0; // outlier
													}
												} else if (pb >= 0.2 && pb < 0.3) {
													if (erro <= 0.0000634) {
														out = 0.0; // muito confiável
													} else if (erro <= 0.00025200 && erro > 0.0000634) {
														out = 0.25;  // confiável
													} else if (erro <= 0.00078128 && erro > 0.00025200) {
														out = 0.50;  // passível de análise intervalar
													} else if (erro <= 0.0018581 && erro > 0.00078128) {
														out = 0.75; // não confiável
													} else if (erro > 0.0018581) {
														out = 1.0; // outlier
													}
												} else if (pb >= 0.1 && pb < 0.2) {
													if (erro <= 0.00004949) {
														out = 0.0; // muito confiável
													} else if (erro <= 0.00023082 && erro > 0.0000634) {
														out = 0.25;  // confiável
													} else if (erro <= 0.000738445 && erro > 0.00023082) {
														out = 0.50;  // passível de análise intervalar
													} else if (erro <= 0.001771878 && erro > 0.000738445) {
														out = 0.75; // não confiável
													} else if (erro > 0.001771878) {
														out = 1.0; // outlier
													}
												} else if (pb >= 0.01 && pb < 0.1) {
													if (erro <= 0.000031245) {
														out = 0.0; // muito confiável
													} else if (erro <= 0.00014516 && erro > 0.000031245) {
														out = 0.25;  // confiável
													} else if (erro <= 0.00045101 && erro > 0.00014516) {
														out = 0.50;  // passível de análise intervalar
													} else if (erro <= 0.001080658 && erro > 0.00045101) {
														out = 0.75; // não confiável
													} else if (erro > 0.001080658) {
														out = 1.0; // outlier
													}
												} else if (pb < 0.01) {
													if (erro <= 0.000001265) {
														out = 0.0; // muito confiável
													} else if (erro <= 0.00000625 && erro > 0.000001265) {
														out = 0.25;  // confiável
													} else if (erro <= 0.000026425 && erro > 0.00000625) {
														out = 0.50;  // passível de análise intervalar
													} else if (erro <= 0.000064165 && erro > 0.000026425) {
														out = 0.75; // não confiável
													} else if (erro > 0.000064165) {
														out = 1.0; // outlier
													}
												}

												pt1 = new PadraoTreinamento(new double[] { w, oxc, density, entropyDFT,
														naturalConectivity, averagePathLengthPhysical, pb },
														new double[] { out });
												System.out.printf("Erro = %.8f; pb = %.8f; CL = %.2f \n ", erro, pb,
														out);
												padroes.add(pt1);
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
		System.out.println("Distintos = " + distintos);
		System.out.println("Repetidos = " + repetidos);
		for (int i = 0; i < samplesCounter.length; i++) {
			System.out.println(i + " - " + samplesCounter[i]);
		}
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

	/**
	 * @return o valor do atributo rnaRef
	 */
	public RedeNeural getRnaRef() {
		return rnaRef;
	}

	/**
	 * Altera o valor do atributo rnaRef
	 * 
	 * @param rnaRef
	 *            O valor para setar em rnaRef
	 */
	public void setRnaRef(RedeNeural rnaRef) {
		this.rnaRef = rnaRef;
	}

}
