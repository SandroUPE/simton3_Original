/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: LoadGenerator.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	26/03/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.experiments;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;

import br.upe.jol.problems.simton.SimtonProblem;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo
 * @since 26/03/2014
 */
public class LoadGenerator {
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
		for (int r = 1; r < 8; r++) {
			File dir = new File("C:/doutorado/experimentos/nfsnet_model2/dataset_200e/r0" + r);

			char[] buffer = null;
			File file = null;
			String[] linhas = null;
			String[] strNetworkParts = null;
			SimtonProblem problem = new SimtonProblem(14, 2, 200);
			StringBuffer sbNew = new StringBuffer();
			NumberFormat nf = NumberFormat.getInstance();
			for (String fileName : dir.list()) {
				file = new File(dir, fileName);
				try {
					FileReader fr = new FileReader(file);
					buffer = new char[(int) file.length()];
					fr.read(buffer);
					linhas = new String(buffer).split("\n");
					// sbNew = new StringBuffer();
					for (String strNetwork : linhas) {
						if (strNetwork.trim().equals("")) {
							continue;
						}
						// System.out.println(fileName + ": " + numNets);
						strNetworkParts = strNetwork.split(";");

						Integer[] variables = new Integer[93];
						int count = 0;
						for (String value : strNetworkParts[20].split(" ")) {
							variables[count] = Integer.parseInt(value);
							count++;
						}
						variables[count] = 5;
						count++;
						variables[count] = Integer.parseInt(strNetworkParts[2]);

						SolutionONTD sol = new SolutionONTD(problem, variables);
						System.out.println(strNetwork);
						problem.evaluate(sol);
						int pos = 0;
						for (String s : strNetworkParts) {
							if (pos == 1) {
								sbNew.append(nf.format(sol.getObjective(0))).append(";");
							} else if (pos == 3) {
								sbNew.append("40,0").append(";");
							} else {
								sbNew.append(s).append(";");
							}
							pos++;
						}
						sbNew.append("\n");
					}

					fr.close();

				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					FileWriter fw = new FileWriter(new File(
							"C:/doutorado/experimentos/nfsnet_model2/dataset_200e/r09/", fileName));
					fw.write(sbNew.toString());
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}
}
