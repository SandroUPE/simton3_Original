/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: CetegoryBlockingExperiment.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	27/12/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.experiments;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;

import br.cns.experiments.ComplexNetwork;
import br.cns.transformations.LinkClosenessSequence;
import br.upe.jol.problems.simton.SimtonProblem;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo
 * @since 27/12/2013
 */
public class CetegoryBlockingExperiment {
	public static double[][] SWITCHES_COSTS_AND_LABELS = new double[][] { { 0, 0.5, 0.75, 1, 1.5, 2.0 }, // costs
			{ 0, 30, 33, 35, 38, 40 } }; // isolation factor in dB

	public static int getIndexOxc(double isolationFactor) {
		for (int i = 0; i < SWITCHES_COSTS_AND_LABELS[1].length; i++) {
			if (SWITCHES_COSTS_AND_LABELS[1][i] == isolationFactor) {
				return i;
			}
		}
		return 0;
	}

	public static void main(String[] args) {
		StringBuffer sbNew = new StringBuffer();
		int countFlags = 0;
		lblExt: for (int r = 1; r <= 8; r++) {
			File dir = new File("C:/doutorado/experimentos/nfsnet_model2/dataset_200_epsilon/r0" + r);
			char[] buffer = null;
			File file = null;
			String[] linhas = null;
			String[] strNetworkParts = null;
			SimtonProblem problem = new SimtonProblem(14, 2, 200);
			
			int numNets = 0;
			NumberFormat nf = NumberFormat.getInstance();
			for (String fileName : dir.list()) {
				sbNew = new StringBuffer();
				file = new File(dir, fileName);
				try {
					FileReader fr = new FileReader(file);
					buffer = new char[(int) file.length()];
					fr.read(buffer);
					linhas = new String(buffer).split("\n");
					for (String strNetwork : linhas) {
						numNets++;
						System.out.println(fileName + ": " + numNets);
						strNetworkParts = strNetwork.split(";");
						double pbOriginal = nf.parse(strNetworkParts[1]).doubleValue();
						Integer[] variables = new Integer[93];
						int count = 0;
						for (String value : strNetworkParts[20].split(" ")) {
							variables[count] = Integer.parseInt(value);
							count++;
						}
						try {
							variables[count] = getIndexOxc(nf.parse(strNetworkParts[3]).doubleValue());
						} catch (ParseException e) {
						}
						count++;
						variables[count] = Integer.parseInt(strNetworkParts[2]);
						boolean flag = false;
						SolutionONTD sol = null;
						if (variables[variables.length - 2] == 5 || variables[variables.length - 2] == 0) {
							variables[variables.length - 2] = 5;
							sol = new SolutionONTD(problem, variables);
							problem.evaluate(sol);
							flag = true;
							countFlags++;
//							if (countFlags > 400) {
//								break lblExt;
//							}
						}

						//
						// String blockingInfo =
						// String.format("%.4f %.4f %.4f %.4f %.4f",
						// problem.getProfile().getBp().getBer(),
						// problem.getProfile().getBp().getDispersion(),
						// problem.getProfile().getBp().getLambda(),
						// problem.getProfile().getBp().getMeanDist(),
						// problem.getProfile().getBp().getPmd());
						//
						// sbNew.append(strNetwork).append(";").append(blockingInfo).append("\n");
						// strNetworkParts[3] = ((int)
						// SWITCHES_COSTS_AND_LABELS[1][Integer.parseInt(strNetworkParts[3])])
						// + "";
						int c = 0;
//						if (flag) {
							for (String s : strNetworkParts) {
								if (c == 1 && flag) {
									sbNew.append(nf.format(sol.getObjective(0))).append(";");
								} else if (c == 3 && flag) {
									sbNew.append("40,0").append(";");
								} else {
									sbNew.append(s).append(";");
								}

								c++;
							}
//							ComplexNetwork cn = problem.createComplexNetworkDistance(sol);
//
//							Double[][] linkCloseness = LinkClosenessSequence.getInstance().transform(cn.getDistances());
//							StringBuffer lc = new StringBuffer();
//							for (int i = 0; i < 14; i++) {
//								for (int j = i + 1; j < 14; j++) {
//									lc.append(linkCloseness[i][j].intValue()).append(" ");
//								}
//							}

//							sbNew.append(lc);
							sbNew.append("\n");

//						}
					}

					fr.close();
				} catch (Exception e) {
					e.printStackTrace();
				}


				try {
					FileWriter fw = new FileWriter(new File("C:/doutorado/experimentos/nfsnet_model2/ds_200_epsilon/r0" + r,
							fileName));
					fw.write(sbNew.toString());
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
