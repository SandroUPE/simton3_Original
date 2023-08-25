/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: ComplexNetworkMultipleRegression.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	13/04/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.experiments;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Set;

import br.cns.TMetric;
import br.cns.experiments.ComplexNetwork;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.SimtonProblem;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo
 * @since 13/04/2013
 */
public class ComplexNetworkMultipleRegression {
	private static Set<String> processadas = new HashSet<String>();

	public static void main(String[] args) {
		// exportOldFormat();
		exportNewFormat();
	}

	private static void exportNewFormat() {
		SimtonProblem problem = new SimtonProblem(14, 2);
		int seq = 0;

		String basePath = "C:/Users/Danilo/workspace_phd/simton3/results/dataset";
		String outPath = "C:/Users/Danilo/workspace_phd/simton3/results/dataset1";
		File baseFile = new File(basePath);
		SolutionSet<Integer> ss = null;
		StringBuffer contentArqResults = null;

		try {
			String quebra = "\n";
			for (String dir : baseFile.list()) {
				contentArqResults = new StringBuffer();
				ss = new SolutionSet<Integer>();
				ss.readIntVariablesFromFileCN(basePath + File.separator + dir, problem);
				for (Solution<Integer> sol : ss.getSolutionsList()) {
					problem.evaluate(sol);
					StringBuffer line = getLine(problem, (SolutionONTD) sol);
					if (line != null) {
						contentArqResults.append(line);
						contentArqResults.append(quebra);
					}
				}
				System.out.println(outPath + File.separator + "complenet_consol_" + seq + ".txt");
				gravarArquivo(outPath + File.separator + "complenet_consol_" + seq + ".txt", contentArqResults);
				seq++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private static void exportOldFormat() {
		SimtonProblem problem = new SimtonProblem(14, 2);
		int seq = 0;
		String[] algs = new String[] { "nsgaii", "spea2", "pesaii", "paes", "mode" };

		for (int a = 0; a < algs.length; a++) {
			for (int p = 1; p <= 4; p++) {
				String basePath = "C:\\BACKUP_PENDRIVE_AZUL\\Mestrado\\Experimentos\\2o\\" + algs[a] + "\\perfil" + p;
				String outPath = "C:\\doutorado\\experimentos\\nfsnet_model2";
				File baseFile = new File(basePath);
				File subDir = null;
				SolutionSet<Integer> ss = null;
				StringBuffer contentArqResults = null;

				try {
					String quebra = "\n";
					Set<String> processados = new HashSet<String>();
					for (String dir : baseFile.list()) {
						subDir = new File(basePath + File.separator + dir);
						if (subDir.isDirectory()) {
							contentArqResults = new StringBuffer();
							for (String arqExp : subDir.list()) {
								ss = new SolutionSet<Integer>();
								if (arqExp.contains("var.txt") && !processados.contains(arqExp)) {
									ss.readIntVariablesFromFile(basePath + File.separator + dir + File.separator
											+ arqExp, problem);
									processados.add(arqExp);
									arqExp = arqExp.replaceFirst("var.txt", "pf.txt");
									ss.readExistingObjectivesFromFile(basePath + File.separator + dir + File.separator
											+ arqExp, problem);
									processados.add(arqExp);
									for (Solution<Integer> sol : ss.getSolutionsList()) {
										StringBuffer line = getLine(problem, (SolutionONTD) sol);
										if (line != null) {
											contentArqResults.append(line);
											contentArqResults.append(quebra);
										}
									}
								}
								if (arqExp.contains("pf.txt") && !processados.contains(arqExp)) {
									ss.readObjectivesFromFile(
											basePath + File.separator + dir + File.separator + arqExp, problem);
									processados.add(arqExp);
									arqExp = arqExp.replaceFirst("pf.txt", "var.txt");
									ss.readExistingIntVariablesFromFile(basePath + File.separator + dir
											+ File.separator + arqExp, problem);
									processados.add(arqExp);
									for (Solution<Integer> sol : ss.getSolutionsList()) {
										StringBuffer line = getLine(problem, (SolutionONTD) sol);
										if (line != null) {
											contentArqResults.append(line);
											contentArqResults.append(quebra);
										}
									}
								}
							}
							System.out.println(outPath + File.separator + "complenet_consol_" + seq + ".txt");
							gravarArquivo(outPath + File.separator + "complenet_consol_" + seq + ".txt",
									contentArqResults);
							seq++;
							processados.clear();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void gravarArquivo(String path, StringBuffer content) {
		try {
			String contentFinal = content.toString();
			FileWriter fw = new FileWriter(path);
			fw.write(contentFinal);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static StringBuffer getLine(SimtonProblem ontd, SolutionONTD solution) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(6);
		nf.setMaximumFractionDigits(6);
		ComplexNetwork cn = ontd.createComplexNetworkDistance(solution);

		// ComplexNetwork cn = ontd.createComplexNetwork(solution);

		StringBuffer fileContent = new StringBuffer();
		String innerSeparator = " ";
		String outerSeparator = ";";
		StringBuffer am = new StringBuffer();
		StringBuffer aam = new StringBuffer();
		StringBuffer hash = new StringBuffer();
		int count = 0;
		for (Integer value : solution.getDecisionVariables()) {
			if (count < solution.getDecisionVariables().length - 2) {
				if (value != 0) {
					am.append(1).append(innerSeparator);
				} else {
					am.append(0).append(innerSeparator);
				}
				aam.append(value).append(innerSeparator);
			}
			hash.append(value).append(";");
			count++;
		}

		if (!processadas.contains(hash.toString())) {
			processadas.add(hash.toString());
		} else {
			return null;
		}
		fileContent.append(nf.format(solution.getObjective(1))); // custo
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(solution.getObjective(0))); // BP
		fileContent.append(outerSeparator);
		fileContent.append(solution.getDecisionVariables()[solution.getDecisionVariables().length - 1].shortValue()); // wavelengths
		fileContent.append(outerSeparator);
		fileContent.append(solution.getDecisionVariables()[solution.getDecisionVariables().length - 2].shortValue()); // oxc
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.ALGEBRAIC_CONNECTIVITY)));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.NATURAL_CONNECTIVITY)));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.DENSITY)));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.AVERAGE_DEGREE)));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.AVERAGE_PATH_LENGTH)));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.CLUSTERING_COEFFICIENT)));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.DIAMETER)));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.ENTROPY)));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.PHYSICAL_DIAMETER)));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.PHYSICAL_AVERAGE_PATH_LENGTH)));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.DFT_LAPLACIAN_ENTROPY)));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(normalizedNf(solution.getDecisionVariables(), cn.getDistances())));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.SPECTRAL_RADIUS)));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.PHYSICAL_DFT_LAPLACIAN_ENTROPY)));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(normalizedSat(solution.getDecisionVariables(), cn.getDistances())));
		fileContent.append(outerSeparator);
		fileContent.append(am.toString()); // adjacencyMatrix
		fileContent.append(outerSeparator);
		fileContent.append(aam.toString()); // adjacencyMatrix

		return fileContent;
	}

	public static double normalizedNf(Integer[] values, Double[][] distances) {
		double nf = 0;
		double sumDistance = 0;

		for (int i = 0; i < values.length - 2; i++) {
			if (values[i] != 0) {
				sumDistance += 1;
				nf += (getNf(values[i]) / 9.0);
			}
		}

		return nf / sumDistance;
	}

	public static double normalizedSat(Integer[] values, Double[][] distances) {
		double sat = 0;
		double sumDistance = 0;

		for (int i = 0; i < values.length - 2; i++) {
			if (values[i] != 0) {
				sumDistance += 1;
				sat += (getSat(values[i]) / 19.0);
			}
		}

		return sat / sumDistance;
	}

	private static int indiceVetor_mpr(int j, int i) {
		return (j + (13) * i - i * (i + 1) / 2);
	}

	private static double getDistance(int index, Double[][] distances) {
		for (int i = 0; i < 14; i++) {
			for (int j = i + 1; j < 14; j++) {
				if (indiceVetor_mpr(i, j) == index) {
					return distances[i][j];
				}
			}
		}
		return 0;
	}

	private static double getNf(int ampLabel) {
		if (ampLabel > 6) {
			return 9;
		} else if (ampLabel > 3) {
			return 7;
		} else if (ampLabel > 0) {
			return 5;
		}
		return 0;
	}

	private static double getSat(int ampLabel) {
		// { 0, 13, 16, 19, 13, 16, 19, 13, 16, 19 }, // amplifier saturation
		if (ampLabel == 1 || ampLabel == 4 || ampLabel == 7) {
			return 13;
		} else if (ampLabel == 2 || ampLabel == 5 || ampLabel == 8) {
			return 16;
		} else if (ampLabel == 3 || ampLabel == 6 || ampLabel == 9) {
			return 19;
		}
		return 0;
	}
}
