/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: DatasetNewFormat.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	16/12/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.dataset;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import ufpe.networkdb.ws.Edge;
import ufpe.networkdb.ws.NetRepServicesProxy;
import ufpe.networkdb.ws.Network;
import ufpe.networkdb.ws.NetworkAttribute;
import ufpe.networkdb.ws.Node;
import br.cns.util.Statistics;
import br.grna.PadraoTreinamento;
import br.grna.PbComparator;
import br.grna.bp.RedeNeural;

/**
 * 
 * @author Danilo
 * @since 16/12/2013
 */
public class DatasetNewFormat extends Dataset {
	

	/**
	 * Construtor da classe.
	 * @param datasetPath
	 * @param intervals
	 * @param redeNeural
	 * @param samples
	 */
	public DatasetNewFormat(String datasetPath, double[][] intervals, RedeNeural redeNeural, int samples, List<OpticalNetDesignVariable> v) {
		super(datasetPath, intervals, redeNeural, samples, v);
	}
	
	/**
	 * Construtor da classe.
	 * @param datasetPath
	 * @param intervals
	 * @param redeNeural
	 * @param samples
	 */
	public DatasetNewFormat(String datasetPath, double[][] intervals, RedeNeural redeNeural, int samples) {
		super(datasetPath, intervals, redeNeural, samples);
	}

	private static final NumberFormat nf = NumberFormat.getInstance();

	private NetRepServicesProxy dao = new NetRepServicesProxy();

	private static final double[][] AMPLIFIERS_COSTS_AND_LABELS = new double[][] {
			{ 0, 0.75, 1.5, 2.25, 0.5, 1, 1.5, 0.25, 0.5, 0.75 }, // cost
			// index
			{ 0, 13, 16, 19, 13, 16, 19, 13, 16, 19 }, // amplifier saturation
			// power
			{ 0, 5, 5, 5, 7, 7, 7, 9, 9, 9 } // amplifier noise figure
	};

	public void populate() {
		nf.setMinimumFractionDigits(6);
		nf.setMaximumFractionDigits(6);
		List<PadraoTreinamento> padroes = new Vector<>();
		String linha = null;
		String[] values = null;
		NumberFormat nf = NumberFormat.getInstance();
		int[] samplesCounter = new int[intervals.length];
		double[] metricValues = new double[variables.size()];
		double pb;
		double ac;
		double diameter;
		double cost;
		int count = 0;
		File dirBase = new File(datasetPath);
		File subDir = null;
		FileReader fr = null;
		LineNumberReader lnr = null;
		Set<String> redesProcessadas = new HashSet<>();
		String hash = "";
		int repetidos = 0;
		int distintos = 0;
		String provider = "DANILO";
		String[] am = null;
		Integer label = 0;
		List<Double> lNfAmps = new Vector<>();
		double meanNf = 0;
		double sdNf = 0;
		List<Double> lSatAmps = new Vector<>();
		double meanSat = 0;
		double sdSat = 0;
		
		Statistics stats = new Statistics();

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

							lNfAmps.clear();
							lSatAmps.clear();
							am = values[11].split(" ");
							for (int i = 0; i < am.length; i++) {
								label = Integer.parseInt(am[i]);
								if (label != 0) {
									lNfAmps.add(AMPLIFIERS_COSTS_AND_LABELS[2][label]);
									lSatAmps.add(AMPLIFIERS_COSTS_AND_LABELS[1][label]);
								}
							}
							stats = new Statistics();
							stats.addRandomVariableValues(lNfAmps);
							stats.addRandomVariableValues(lSatAmps);
							meanNf = stats.getMean(0);
							sdNf = stats.getStandardDeviation(0);
							meanSat = stats.getMean(1);
							sdSat = stats.getStandardDeviation(1);
//							metricValues = new double[variables.size() + 4];
							metricValues = new double[variables.size()];
							
							for (int i = 0; i < variables.size(); i++) {
								metricValues[i] = nf.parse(values[i+1]).doubleValue();
							}
//							metricValues[metricValues.length - 4] = meanSat;
//							metricValues[metricValues.length - 3] = sdSat;
//							metricValues[metricValues.length - 2] = meanNf;
//							metricValues[metricValues.length - 1] = sdNf;

							pb = nf.parse(values[0]).doubleValue();
//							ac = nf.parse(values[Variable.AC.getIndex()]).doubleValue();
//							diameter = nf.parse(values[Variable.DIAMETER.getIndex()]).doubleValue();
//							cost = nf.parse(values[Variable.COST.getIndex()]).doubleValue();
							hash = values[11] + ";" + values[OpticalNetDesignVariable.OXC.getIndex()] + ";"
									+ values[OpticalNetDesignVariable.W.getIndex()];
							if (!redesProcessadas.contains(hash)) {
								distintos++;
								// remover outliers
//								if (ac > 0 && diameter > 0 && diameter < (Integer.MAX_VALUE - 1) && cost < 20141
//										&& pb < 1) {
									for (int classe = 0; classe < intervals.length; classe++) {
										if (pb > intervals[classe][0] && pb <= intervals[classe][1]) {
											if (samplesCounter[classe] < samples) {
												samplesCounter[classe]++;
												PadraoTreinamento pt = new PadraoTreinamento(metricValues,
														new double[] { pb });
												padroes.add(pt);
												// gravarBD(pt, values[19],
												// cost, provider, values[20]);
												count++;
												if (count > samples * intervals.length) {
													break lblExt;
												}
											}
										}
//									}
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
		minValues = new double[]{4.0, 0.0, 0.1428, 0.0,  50.0, 0.0,  13.0,  0.0,  5.0, 0.0 };
		maxValues = new double[]{40.0, 4.0, 1.0, 20.0,  300.0, 3.0,  19.0,  6.0,  9.0, 4.0 };
//		minValues = new double[padroes.get(0).getEntrada().length];
//		maxValues = new double[padroes.get(0).getEntrada().length];
//		Arrays.fill(minValues, Double.MAX_VALUE);
//		Arrays.fill(maxValues, Double.MIN_VALUE);
//		for (PadraoTreinamento padrao : padroes) {
//			for (int i = 0; i < padrao.getEntrada().length; i++) {
//				if (padrao.getEntrada()[i] < minValues[i]) {
//					minValues[i] = padrao.getEntrada()[i];
//				}
//				if (padrao.getEntrada()[i] > maxValues[i]) {
//					maxValues[i] = padrao.getEntrada()[i];
//				}
//			}
//		}
		if (redeNeural != null) {
			redeNeural.setMinValues(minValues);
			redeNeural.setMaxValues(maxValues);
		}
		for (PadraoTreinamento padrao : padroes) {
			for (int i = 0; i < padrao.getEntrada().length; i++) {
				if (minValues == maxValues) {
					padrao.setValorEntrada(i, 1);
				} else {
					padrao.setValorEntrada(i, (padrao.getEntrada()[i] - minValues[i]) / (maxValues[i] - minValues[i]));
				}
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

	public void setRnaInputRanges(RedeNeural rna) {
		redeNeural.setMinValues(minValues);
		redeNeural.setMaxValues(maxValues);
	}

	/**
	 * @return o valor do atributo padroesTreinamento
	 */
	public List<PadraoTreinamento> getPadroesTreinamento() {
		return padroesTreinamento;
	}

	/**
	 * @return o valor do atributo padroesTeste
	 */
	public List<PadraoTreinamento> getPadroesTeste() {
		return padroesTeste;
	}

	/**
	 * @return o valor do atributo mapPadroesTreinamento
	 */
	public Map<Integer, List<PadraoTreinamento>> getMapPadroesTreinamento() {
		return mapPadroesTreinamento;
	}

	/**
	 * Altera o valor do atributo mapPadroesTreinamento
	 * 
	 * @param mapPadroesTreinamento
	 *            O valor para setar em mapPadroesTreinamento
	 */
	public void setMapPadroesTreinamento(Map<Integer, List<PadraoTreinamento>> mapPadroesTreinamento) {
		this.mapPadroesTreinamento = mapPadroesTreinamento;
	}

	/**
	 * @return o valor do atributo mapPadroesTeste
	 */
	public Map<Integer, List<PadraoTreinamento>> getMapPadroesTeste() {
		return mapPadroesTeste;
	}

	/**
	 * Altera o valor do atributo mapPadroesTeste
	 * 
	 * @param mapPadroesTeste
	 *            O valor para setar em mapPadroesTeste
	 */
	public void setMapPadroesTeste(Map<Integer, List<PadraoTreinamento>> mapPadroesTeste) {
		this.mapPadroesTeste = mapPadroesTeste;
	}

	/**
	 * @return o valor do atributo intervals
	 */
	public double[][] getIntervals() {
		return intervals;
	}

	/**
	 * Altera o valor do atributo intervals
	 * 
	 * @param intervals
	 *            O valor para setar em intervals
	 */
	public void setIntervals(double[][] intervals) {
		this.intervals = intervals;
	}

	/**
	 * Altera o valor do atributo padroesTeste
	 * 
	 * @param padroesTeste
	 *            O valor para setar em padroesTeste
	 */
	public void setPadroesTeste(List<PadraoTreinamento> padroesTeste) {
		this.padroesTeste = padroesTeste;
	}

	/**
	 * @return o valor do atributo padroesValidacao
	 */
	public List<PadraoTreinamento> getPadroesValidacao() {
		return padroesValidacao;
	}

	/**
	 * Altera o valor do atributo padroesValidacao
	 * 
	 * @param padroesValidacao
	 *            O valor para setar em padroesValidacao
	 */
	public void setPadroesValidacao(List<PadraoTreinamento> padroesValidacao) {
		this.padroesValidacao = padroesValidacao;
	}

	/**
	 * @return o valor do atributo mapPadroesValidacao
	 */
	public Map<Integer, List<PadraoTreinamento>> getMapPadroesValidacao() {
		return mapPadroesValidacao;
	}

	/**
	 * Altera o valor do atributo mapPadroesValidacao
	 * 
	 * @param mapPadroesValidacao
	 *            O valor para setar em mapPadroesValidacao
	 */
	public void setMapPadroesValidacao(Map<Integer, List<PadraoTreinamento>> mapPadroesValidacao) {
		this.mapPadroesValidacao = mapPadroesValidacao;
	}

	/**
	 * Altera o valor do atributo padroesTreinamento
	 * 
	 * @param padroesTreinamento
	 *            O valor para setar em padroesTreinamento
	 */
	public void setPadroesTreinamento(List<PadraoTreinamento> padroesTreinamento) {
		this.padroesTreinamento = padroesTreinamento;
	}

}
