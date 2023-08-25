/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: Dataset.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	09/08/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.dataset;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import br.cns.TMetric;
import br.cns.experiments.ComplexNetwork;
import br.cns.metrics.PhysicalAveragePathLengthSD;
import br.cns.models.TModel;
import br.grna.PadraoTreinamento;
import br.grna.PbComparator;
import br.grna.bp.RedeNeural;
import br.upe.jol.base.Problem;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo
 * @since 09/08/2013
 */
public class Dataset {
	protected boolean validation = true;

	protected String datasetPath;

	protected double[][] intervals;

	protected Double[][] distances;

	protected Problem<Integer> problem;

	protected RedeNeural redeNeural;

	protected int samples;

	protected List<PadraoTreinamento> padroesTreinamento;

	protected List<PadraoTreinamento> padroesTeste;

	protected List<PadraoTreinamento> padroesValidacao;

	protected Map<Integer, List<PadraoTreinamento>> mapPadroesTreinamento = new HashMap<Integer, List<PadraoTreinamento>>();

	protected Map<Integer, List<PadraoTreinamento>> mapPadroesTeste = new HashMap<Integer, List<PadraoTreinamento>>();

	protected Map<Integer, List<PadraoTreinamento>> mapPadroesValidacao = new HashMap<Integer, List<PadraoTreinamento>>();

	protected double[] minValues = new double[11];

	protected double[] maxValues = new double[11];

	protected List<OpticalNetDesignVariable> variables = null;

	private static final NumberFormat nf = NumberFormat.getInstance();

	private double networkLoad;

	public static double[][] SWITCHES_COSTS_AND_LABELS = new double[][] { { 0, 0.5, 0.75, 1, 1.5, 2.0 }, // costs
			{ 0, 30, 33, 35, 38, 40 } }; // isolation factor in dB

	public Dataset(String datasetPath, double[][] intervals, RedeNeural redeNeural, int samples,
			List<OpticalNetDesignVariable> variables) {
		this.datasetPath = datasetPath;
		this.intervals = intervals;
		this.redeNeural = redeNeural;
		this.samples = samples;
		this.variables = variables;
	}

	public int getIndexOxc(double isolationFactor) {
		for (int i = 0; i < SWITCHES_COSTS_AND_LABELS[1].length; i++) {
			if (SWITCHES_COSTS_AND_LABELS[1][i] == isolationFactor) {
				return i;
			}
		}
		return 0;
	}

	public Dataset(String datasetPath, double[][] intervals, RedeNeural redeNeural, int samples) {
		this.datasetPath = datasetPath;
		this.intervals = intervals;
		this.redeNeural = redeNeural;
		this.samples = samples;
		this.variables = new Vector<>();

		variables.add(OpticalNetDesignVariable.W);
		variables.add(OpticalNetDesignVariable.OXC);
		variables.add(OpticalNetDesignVariable.DENSITY);
		variables.add(OpticalNetDesignVariable.NC);
		variables.add(OpticalNetDesignVariable.AVERAGE_PATH_LENGTH_PHYSICAL);
		variables.add(OpticalNetDesignVariable.ENTROPY_DFT);
	}

	public void populate() {
		// SimtonProblem problem = new SimtonProblem(14, 2);
		// problem.setSimulate(true);
		nf.setMinimumFractionDigits(6);
		nf.setMaximumFractionDigits(6);
		List<PadraoTreinamento> padroes = new Vector<>();
		String linha = null;
		String[] values = null;
		NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
		int[] samplesCounter = new int[intervals.length];
		int adds = 2;
		double[] metricValues = new double[variables.size() + adds];
		double pb;
		double ac;
		double diameter;
		double cost;
		int count = 0;
		File dirBase = new File(datasetPath);
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
		try {
			lblExt: for (String arq : dirBase.list()) {
				if (!arq.contains("dataset")) {
					continue;
				}
				try {
					fr = new FileReader(new File(dirBase.getAbsolutePath() + File.separator + arq));
					lnr = new LineNumberReader(fr);
					linha = lnr.readLine();
					while (linha != null) {
						values = linha.split(";");
						// é acrescentada CR e a carga
						metricValues = new double[variables.size() + adds];
						String[] strLC = values[17].split(" ");
						List<Double> orderedCloseness = new Vector<>();
						for (String lcv : strLC) {
							if (!lcv.trim().equals("")) {
								orderedCloseness.add(Double.parseDouble(lcv.trim()) * 1.0);
							}
						}
						Collections.sort(orderedCloseness);
						for (int i = 0; i < variables.size(); i++) {
							metricValues[i] = nf.parse(values[variables.get(i).getIndex()].trim()).doubleValue();
						}
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
						metricValues[metricValues.length - 2] = (maxLc - minLc) / countLc;
						metricValues[metricValues.length - 1] = networkLoad;

						pb = nf.parse(values[OpticalNetDesignVariable.BP.getIndex()].trim()).doubleValue();
						ac = nf.parse(values[OpticalNetDesignVariable.AC.getIndex()].trim()).doubleValue();
						double apl = nf.parse(values[OpticalNetDesignVariable.AVERAGE_PATH_LENGTH_PHYSICAL.getIndex()].trim()).doubleValue();
						
						diameter = nf.parse(values[OpticalNetDesignVariable.DIAMETER.getIndex()].trim()).doubleValue();
//						hash = values[16] + ";" + values[Variable.OXC.getIndex()] + ";" + values[Variable.W.getIndex()];
						hash = values[16] + ";" + values[OpticalNetDesignVariable.W.getIndex()];

						if (!redesProcessadas.contains(hash) && ((samples <= 40 && Math.random() > 0.5) || samples > 40)) {
							// remover outliers
							if (ac > 0 && diameter > 0 && diameter < (Integer.MAX_VALUE - 1) && pb < 1) {
								distintos++;
								for (int classe = 0; classe < intervals.length; classe++) {
									if (pb > intervals[classe][0] && pb <= intervals[classe][1]) {
										if (samplesCounter[classe] < samples) {
//											String[] am = values[16].split(" ");
//											Integer[] variables = new Integer[am.length + 2];
//											for (int i = 0; i < am.length; i++) { 
//												variables[i] = Integer.parseInt(am[i]);
//											}
//											variables[variables.length-1] = Integer.parseInt(values[Variable.W.getIndex()]);
//											variables[variables.length-2] = 4;
//												
//											SolutionONTD sol = new SolutionONTD(problem, variables);
//											
//											double sdapl = PhysicalAveragePathLengthSD.getInstance().calculate(createComplexNetworkDistance(sol, distances.length));
//											if (sdapl > 500){
//												System.out.println("Achou");
//											}
//												
//											metricValues[metricValues.length - 3] = sdapl;
											
											samplesCounter[classe]++;

											PadraoTreinamento pt = new PadraoTreinamento(metricValues,
													new double[] { pb });
											padroes.add(pt);
											count++;
											if (count > samples * intervals.length) {
												break lblExt;
											}
										}
									}
								}
								redesProcessadas.add(hash);
							}
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
		} catch (Throwable e) {
			e.printStackTrace();
		}
		System.out.println("Distintos = " + distintos + ". Repetidos = " + repetidos);
		for (int i = 0; i < samplesCounter.length; i++) {
			System.out.print(i + ": " + samplesCounter[i] + "; ");
		}
		System.out.println();
		// normalizar
//		setBounds(padroes);
//		for (PadraoTreinamento padrao : padroes) {
//			for (int i = 0; i < padrao.getEntrada().length; i++) {
//				if (minValues[i] == maxValues[i]) {
//					padrao.setValorEntrada(i, 1);
//				} else {
//					padrao.setValorEntrada(i, (padrao.getEntrada()[i] - minValues[i]) / (maxValues[i] - minValues[i]));
//				}
//			}
//		}
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
	
	public ComplexNetwork createComplexNetworkDistance(SolutionONTD solution, int numNodes) {
		List<TMetric> metrics = new ArrayList<TMetric>();
		metrics.add(TMetric.NATURAL_CONNECTIVITY);
		metrics.add(TMetric.ALGEBRAIC_CONNECTIVITY);
		metrics.add(TMetric.DENSITY);
		metrics.add(TMetric.AVERAGE_DEGREE);
		metrics.add(TMetric.AVERAGE_PATH_LENGTH);
		metrics.add(TMetric.PHYSICAL_AVERAGE_PATH_LENGTH);
		metrics.add(TMetric.CLUSTERING_COEFFICIENT);
		metrics.add(TMetric.DIAMETER);
		metrics.add(TMetric.PHYSICAL_DIAMETER);
		metrics.add(TMetric.ENTROPY);
		metrics.add(TMetric.DFT_LAPLACIAN_ENTROPY);
		metrics.add(TMetric.PHYSICAL_DFT_LAPLACIAN_ENTROPY);
		metrics.add(TMetric.SPECTRAL_RADIUS);
		metrics.add(TMetric.MAXIMUM_CLOSENESS);
		metrics.add(TMetric.PHYSICAL_DENSITY);

		Integer[][] matrix = new Integer[numNodes][numNodes];
		double value = 0;
		int counter = 0;
		Double[][] customDistances = new Double[numNodes][numNodes];
		for (int i = 0; i < matrix.length; i++) {
			matrix[i][i] = 0;
			customDistances[i][i] = 0.0;
			for (int j = i + 1; j < matrix.length; j++) {
				value = Integer.valueOf(solution.getDecisionVariables()[counter]);
				if (value != 0) {
					matrix[i][j] = 1;
					matrix[j][i] = 1;
					customDistances[i][j] = distances[i][j];
					customDistances[j][i] = distances[j][i];
				} else {
					matrix[i][j] = 0;
					matrix[j][i] = 0;
					customDistances[i][j] = 0.0;
					customDistances[j][i] = 0.0;
				}
				counter++;
			}
		}
		return new ComplexNetwork(0, matrix, new double[numNodes][numNodes], distances, customDistances, TModel.CUSTOM,
				metrics);
	}

	/**
	 * @param padroes
	 */
	private void setBounds(List<PadraoTreinamento> padroes) {
		minValues = new double[padroes.get(0).getEntrada().length];
		maxValues = new double[padroes.get(0).getEntrada().length];

		Arrays.fill(minValues, Double.MAX_VALUE);
		Arrays.fill(maxValues, Double.MIN_VALUE);
		for (int i = 0; i < minValues.length; i++) {
			for (PadraoTreinamento pt : padroes) {
				if (pt.getEntrada()[i] > maxValues[i]) {
					maxValues[i] = pt.getEntrada()[i];
				}
				if (pt.getEntrada()[i] < minValues[i]) {
					minValues[i] = pt.getEntrada()[i];
				}
			}
		}

//		minValues = new double[] { 4.0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 50.0 };
//		maxValues = new double[] { 40.0, 40, 1.0, 1.0, 5000.00, 3.0, 1.0, 10.0, 200.0 };

		if (redeNeural != null) {
			redeNeural.setMinValues(minValues);
			redeNeural.setMaxValues(maxValues);
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

	/**
	 * @return o valor do atributo validation
	 */
	public boolean isValidation() {
		return validation;
	}

	/**
	 * Altera o valor do atributo validation
	 * 
	 * @param validation
	 *            O valor para setar em validation
	 */
	public void setValidation(boolean validation) {
		this.validation = validation;
	}

	/**
	 * @return o valor do atributo networkLoad
	 */
	public double getNetworkLoad() {
		return networkLoad;
	}

	/**
	 * Altera o valor do atributo networkLoad
	 * 
	 * @param networkLoad
	 *            O valor para setar em networkLoad
	 */
	public void setNetworkLoad(double networkLoad) {
		this.networkLoad = networkLoad;
	}

	/**
	 * @return o valor do atributo distances
	 */
	public Double[][] getDistances() {
		return distances;
	}

	/**
	 * Altera o valor do atributo distances
	 * @param distances O valor para setar em distances
	 */
	public void setDistances(Double[][] distances) {
		this.distances = distances;
	}

	/**
	 * @return o valor do atributo problem
	 */
	public Problem<Integer> getProblem() {
		return problem;
	}

	/**
	 * Altera o valor do atributo problem
	 * @param problem O valor para setar em problem
	 */
	public void setProblem(Problem<Integer> problem) {
		this.problem = problem;
	}

}
