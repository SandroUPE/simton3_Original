/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: EvaluateFixedEletronetRecife.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	23/03/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.experiments;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import br.cns.TMetric;
import br.cns.experiments.ComplexNetwork;
import br.cns.experiments.nodePositions.CircularNetwork;
import br.cns.model.GmlData;
import br.cns.model.GmlEdge;
import br.cns.models.BarabasiDensity;
import br.cns.models.GenerativeProcedure;
import br.cns.models.TModel;
import br.cns.models.WattsStrogatzDensity;
import br.cns.persistence.GmlDao;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.GmlSimton;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo
 * @since 23/03/2014
 */
public class EvaluateFixedEletronetRecife {
	public static void main(String[] args) {
		evaluateFixedNetwork();
	}

	public static void evaluateFixedNetwork() {
		int n = 93;
		SimpleDateFormat df = new SimpleDateFormat("YYYY.dd.MM.HH.mm");
		GmlDao dao = new GmlDao();
		GmlData gmlData = dao.loadGmlData("C:/Users/Danilo/workspace_phd/Maps/gml/rec_metroweb.gml");
		GmlSimton problem = new GmlSimton(n, 2, gmlData, 55);
		int numRedes = 15;
		List<TMetric> metrics = TMetric.getDefaults();
		// inicio novo exemplo b
		// Integer[] variables = { 3, 3, 0, 0, 3, 0, 0, 0, 0, 3, 0, 3, 0, 0, 0,
		// 0, 0, 0, 3, 0, 3, 4, 40 };
		Integer[] variables = new Integer[(n * (n - 1)) / 2 + 2];
		variables[variables.length - 1] = 40;
		variables[variables.length - 2] = 4;
		int count = 0;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				variables[count] = 0;
				for (GmlEdge edge : gmlData.getEdges()) {
					if ((edge.getSource().getId() == i && edge.getTarget().getId() == j)
							|| (edge.getSource().getId() == j && edge.getTarget().getId() == i)) {
						variables[count] = 3;
						break;
					}
				}
				count++;
			}
		}

		SolutionONTD sol = new SolutionONTD(problem, variables);
//		problem.evaluate(sol);

		double[] pb = new double[numRedes];
		double density = 0.024;
		int k = getK(density, n);
		GenerativeProcedure gp = new WattsStrogatzDensity(0.05, k, density, true);
		ComplexNetwork cn = null;
//		System.out.println("WS");
		double avgPb = 0;
		SolutionSet<Integer> ss = new SolutionSet<>(numRedes);
//		for (int i = 0; i < numRedes; i++) {
//			cn = new ComplexNetwork(0, gp.transform(new Integer[n][n]), CircularNetwork.getInstance()
//					.createNodePositions(n), TModel.WATTS_STROGATZ, metrics);
//			System.out.println("d = " + cn.getMetricValues().get(TMetric.DENSITY));
//			count = 0;
//			for (int j = 0; j < n; j++) {
//				for (int l = j + 1; l < n; l++) {
//					variables[count] = cn.getAdjacencyMatrix()[j][l] * 3;
//					count++;
//				}
//			}
//			sol = new SolutionONTD(problem, variables);
//			problem.evaluate(sol);
//			ss.add(sol);
//			pb[i] = sol.getObjective(0);
//			avgPb += pb[i];
//		}
//		System.out.println("PB MED = " + avgPb / numRedes);
//		avgPb = 0;
//		ss.printObjectivesToFile("ws-pf-" + df.format(new Date()) + ".txt");
//		ss.printVariablesToFile("ws-var-" + df.format(new Date()) + ".txt");
		ss = new SolutionSet<>(numRedes);
		System.out.println("BA");
		gp = new BarabasiDensity(density, 1.0);
		for (int i = 0; i < numRedes; i++) {
			cn = new ComplexNetwork(0, gp.transform(new Integer[n][n]), CircularNetwork.getInstance()
					.createNodePositions(n), TModel.BARABASI, metrics);
			System.out.println("d = " + cn.getMetricValues().get(TMetric.DENSITY));
			count = 0;
			for (int j = 0; j < n; j++) {
				for (int l = j + 1; l < n; l++) {
					variables[count] = cn.getAdjacencyMatrix()[j][l] * 3;
					count++;
				}
			}
			sol = new SolutionONTD(problem, variables);
			problem.evaluate(sol);
			ss.add(sol);
			pb[i] = sol.getObjective(0);
			avgPb += pb[i];
		}
		System.out.println("PB MED = " + avgPb / numRedes);
		ss.printObjectivesToFile("ba-pf-" + df.format(new Date()) + ".txt");
		ss.printVariablesToFile("ba-var-" + df.format(new Date()) + ".txt");
	}

	private static int getK(double density, int n) {
		int k = Math.max((int) Math.floor((density * (n - 1)) / 2.0) + 1, 1);
		double deltaK = Math.abs(density - (2.0 * k) / (n - 1));
		double deltaKp1 = Math.abs(density - (2.0 * (k + 1)) / (n - 1));
		double deltakm1 = deltaK;
		if (k > 1) {
			deltakm1 = Math.abs(density - (2.0 * (k - 1)) / (n - 1));
		}
		if (deltaKp1 < deltaK) {
			if (deltakm1 < deltaKp1) {
				k = k - 1;
			} else {
				k = k + 1;
			}
		} else if (deltakm1 < deltaK) {
			k = k - 1;
		}

		return k;
	}

}
