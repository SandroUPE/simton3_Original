/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: VariableDataset.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	09/03/2015		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.dataset;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import br.cns.TMetric;
import br.cns.experiments.ComplexNetwork;
import br.cns.models.TModel;
import br.grna.PadraoTreinamento;
import br.grna.PbComparator;
import br.grna.bp.RedeNeural;
import br.upe.jol.base.Problem;
import br.upe.jol.problems.simton.GmlSimton;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo Araujo
 * @since 09/03/2015
 */
public class VariableDataset {
	protected boolean validation = true;

	protected String datasetPath;

	protected Double[][] distances;

	protected Problem<Integer> problem;

	protected RedeNeural redeNeural;

	protected int numMaxSamples;

	protected List<PadraoTreinamento> padroesTreinamento;

	protected List<PadraoTreinamento> padroesTeste;

	protected List<PadraoTreinamento> padroesValidacao;

	protected Map<Integer, List<PadraoTreinamento>> mapPadroesTreinamento = new HashMap<Integer, List<PadraoTreinamento>>();

	protected Map<Integer, List<PadraoTreinamento>> mapPadroesTeste = new HashMap<Integer, List<PadraoTreinamento>>();

	protected Map<Integer, List<PadraoTreinamento>> mapPadroesValidacao = new HashMap<Integer, List<PadraoTreinamento>>();

	protected double[] minValues = new double[11];

	protected double[] maxValues = new double[11];

	protected List<OpticalNetDesignVariable> variables = null;

	private int numNodes = 0;

	private static final NumberFormat nf = NumberFormat.getInstance();

	public static double[][] SWITCHES_COSTS_AND_LABELS = new double[][] { { 0, 0.5, 0.75, 1, 1.5, 2.0 }, // costs
			{ 0, 30, 33, 35, 38, 40 } }; // isolation factor in dB

	public VariableDataset(String datasetPath, RedeNeural redeNeural, List<OpticalNetDesignVariable> variables, int numMaxSamples,
			int numNodes) {
		this.datasetPath = datasetPath;
		this.redeNeural = redeNeural;
		this.variables = variables;
		this.numMaxSamples = numMaxSamples;
		this.numNodes = numNodes;
	}

	public int getIndexOxc(double isolationFactor) {
		for (int i = 0; i < SWITCHES_COSTS_AND_LABELS[1].length; i++) {
			if (SWITCHES_COSTS_AND_LABELS[1][i] == isolationFactor) {
				return i;
			}
		}
		return 0;
	}

	public VariableDataset(String datasetPath, RedeNeural redeNeural, int numMaxSamples) {
		this.datasetPath = datasetPath;
		this.redeNeural = redeNeural;
		this.variables = new Vector<>();
		this.numMaxSamples = numMaxSamples;

		variables.add(OpticalNetDesignVariable.W);
		variables.add(OpticalNetDesignVariable.OXC);
		variables.add(OpticalNetDesignVariable.DENSITY);
		variables.add(OpticalNetDesignVariable.NC);
		variables.add(OpticalNetDesignVariable.AVERAGE_PATH_LENGTH_PHYSICAL);
		variables.add(OpticalNetDesignVariable.ENTROPY_DFT);
	}

	private int getIndexPB(double pb, double[][] intervals) {
		for (int i = 0; i < intervals.length; i++) {
			if (pb >= intervals[i][0] && pb <= intervals[i][1]) {
				return i;
			}
		}
		return -1;
	}

	public void populate() {
		nf.setMinimumFractionDigits(6);
		nf.setMaximumFractionDigits(6);
		List<PadraoTreinamento> padroes = new Vector<>();
		String linha = null;
		String[] values = null;
		NumberFormat nf = NumberFormat.getInstance(new Locale("PT", "BR"));
		double[] metricValues = new double[variables.size()];
		double pb;
		File dirBase = new File(datasetPath);
		FileReader fr = null;
		LineNumberReader lnr = null;
		double[][] intervalos = new double[][] { { 0.0, 0.001 }, { 0.001, 0.01 }, { 0.01, 0.10 }, { 0.10, 0.20 },
				{ 0.20, 0.30 }, { 0.30, 0.40 }, { 0.40, 0.50 }, { 0.5, 0.60 }, { 0.6, 0.70 }, { 0.70, 1.00 } };
		int maxClass = numMaxSamples / intervalos.length;
		int[] counters = new int[intervalos.length];

		for (int i = 0; i < counters.length; i++) {
			counters[i] = 0;
		}
		double emq = 0;
		int index = 0;
		int totalCounter = 0;
		StringBuffer sbErros = new StringBuffer();
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
						if (numNodes == 0) {
							metricValues = new double[variables.size()];
							for (int i = 0; i < variables.size(); i++) {
								metricValues[i] = nf.parse(values[variables.get(i).getIndex()].trim()).doubleValue();
							}
						} else {
							metricValues = new double[variables.size() + 1];
							for (int i = 0; i < variables.size(); i++) {
								metricValues[i] = nf.parse(values[variables.get(i).getIndex()].trim()).doubleValue();
							}
							metricValues[metricValues.length - 1] = numNodes;
						}

						pb = nf.parse(values[OpticalNetDesignVariable.BP.getIndex()].trim()).doubleValue();
						index = getIndexPB(pb, intervalos);

						if (counters[index] < maxClass) {
							PadraoTreinamento pt = new PadraoTreinamento(metricValues, new double[] { pb });

//							String[] strVar = values[16].split(" ");
//
//							Integer[] variables = new Integer[strVar.length + 2];
//
//							for (int i = 0; i < strVar.length; i++) {
//								variables[i] = Integer.parseInt(strVar[i]);
//							}
//							double oxcIF = nf.parse(values[Variable.OXC.getIndex()].trim()).doubleValue();;
//							variables[variables.length - 2] = 4;
//							for (int i = 0; i < GmlSimton.SWITCHES_COSTS_AND_LABELS[0].length; i++) {
//								if (oxcIF == GmlSimton.SWITCHES_COSTS_AND_LABELS[1][i]) {
//									variables[variables.length - 2] = i;
//									break;
//								}
//							}
//							variables[variables.length - 1] = Integer.parseInt(values[Variable.W.getIndex()].trim());
//							SolutionONTD sol = new SolutionONTD(problem, variables);
//							int load = Integer.parseInt(values[Variable.NETWORK_LOAD.getIndex()].trim());
//							if (load == 200) {
//								problem.evaluate(sol);
//								totalCounter++;
//								sbErros.append(String.format("%.6f %.6f \n", sol.getObjective(0), pb));
//								emq += (sol.getObjective(0) - pb) * (sol.getObjective(0) - pb);
//							}
							// System.out.println("PB1 = " + pb);
							// System.out.println("PB2 = " +
							// sol.getObjective(0));
							// double[] m1 =
							// ((GmlSimton)problem).calculateMetricsWithSimulation(sol,
							// 12);
							// double[] m1 =
							// ((GmlSimton)problem).calculateMetricsWithSimulation(sol,
							// 5);
							padroes.add(pt);
							counters[index]++;

							// for (double d : metricValues) {
							// System.out.printf("%.4f ", d);
							// }
							// System.out.println();
							// for (double d : m1) {
							// System.out.printf("%.4f ", d);
							// }
							// System.out.println();

							boolean parar = true;
							for (int i = 0; i < counters.length; i++) {
								if (counters[i] < maxClass) {
									parar = false;
									break;
								}
							}
							if (parar) {
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
		} catch (Throwable e) {
			e.printStackTrace();
		}
//		System.out.println(sbErros.toString());
//		try {
//			FileWriter fw = new FileWriter("/results/erros-simton.txt");
//			fw.write(sbErros.toString());
//			fw.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		 System.out.println();
//		 for (int c : counters) {
//		 System.out.print(c + " ");
//		 }
//		 System.out.println();
//		System.out.printf("EMQ SIMTON = %.8f\n", emq / totalCounter);
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
	 * @return o valor do atributo problem
	 */
	public Problem<Integer> getProblem() {
		return problem;
	}

	/**
	 * Altera o valor do atributo problem
	 * 
	 * @param problem
	 *            O valor para setar em problem
	 */
	public void setProblem(Problem<Integer> problem) {
		this.problem = problem;
	}
}
