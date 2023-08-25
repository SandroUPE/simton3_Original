/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: RnaFromGmlExperiment.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	25/03/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.problems.simton;

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

/**
 * 
 * @author Danilo
 * @since 25/03/2014
 */
public class RnaFromGmlExperiment {
	public static void main(String[] args) {
		evaluateFixedNetwork();
	}

	public static void evaluateFixedNetwork() {
		int n = 13;
		SimpleDateFormat df = new SimpleDateFormat("YYYY.dd.MM.HH.mm");
		GmlDao dao = new GmlDao();
//		GmlData gmlData = dao.loadGmlData("C:/Users/Danilo/workspace_phd/Maps/gml/rec_metroweb.gml");
		GmlData gmlData = dao.loadGmlData("C:/doutorado/datasets/internet topology/Nsfnet.gml");
		GmlSimton problem = new GmlSimton(n, 2, gmlData, 100);
		int numRedes = 15;
		List<TMetric> metrics = TMetric.getDefaults();
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
		problem.evaluate(sol);

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
