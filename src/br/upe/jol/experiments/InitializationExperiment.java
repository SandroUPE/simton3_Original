/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: InitializationExperiment.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	26/12/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.experiments;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Vector;

import br.cns.Geolocation;
import br.cns.model.GmlData;
import br.cns.persistence.GmlDao;
import br.upe.jol.base.RankUtil;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.metrics.Coverage;
import br.upe.jol.metrics.Hypervolume;
import br.upe.jol.metrics.MaximumSpread;
import br.upe.jol.metrics.Spacing;
import br.upe.jol.operators.initializers.Initializer;
import br.upe.jol.operators.initializers.WaxmanInitializer;
import br.upe.jol.problems.simton.GmlSimton;

/**
 * 
 * @author Danilo
 * @since 26/12/2013
 */
public class InitializationExperiment {
	public static void main(String[] args) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(4);
		nf.setMaximumFractionDigits(4);
		int nRuns = 11;
		Spacing<Integer> spacing = new Spacing<Integer>();
		MaximumSpread<Integer> ms = new MaximumSpread<Integer>();
		Hypervolume<Integer> hyper = new Hypervolume<Integer>();
		Coverage<Integer> cov = new Coverage<Integer>();
		RankUtil<Integer> ranking = RankUtil.getIntegerInstance();
		
		// SimtonProblemNonUniformHub problem = new
		// SimtonProblemNonUniformHub(numNodes, 2);
//		SimtonProblem problem = new SimtonProblem(numNodes, 2, 200);
		GmlDao dao = new GmlDao();
		String strNet = "Noel";
//		GmlData data = dao.loadGmlData("C:/doutorado/datasets/internet topology/self-generated/Sprint.gml");
		GmlData data = dao.loadGmlData("C:/doutorado/datasets/internet topology/" + strNet + ".gml");
		int numNodes = data.getNodes().size();
		GmlSimton problem = new GmlSimton(numNodes, 2, data, 100);
		
		int size = 100;
		double minDensity = 1.0 / (numNodes - 1);
		double maxDensity = 0.15;
//		double minDensity = 0.25;
//		double maxDensity = 0.60;
		int ampLabel = 3;
//		Double[][] traffic = CallSchedulerNonUniformHub.TRAFFICMATRIX;
		Double[][] distances = problem.getDistanceMatrix();
		Geolocation[] locations = new Geolocation[numNodes];
		for (int i = 0; i < locations.length; i++) {
			locations[i] = new Geolocation(data.getNodes().get(i).getLatitude(), data.getNodes().get(i).getLongitude());
//			locations[i] = new Geolocation(SimtonProblem.NODES_POSITIONS[i][0], SimtonProblem.NODES_POSITIONS[i][1]);
		}

//		double maxDistance = 0;
//		double maxTraffic = 0;
//		for (int i = 0; i < distances.length; i++) {
//			for (int j = i + 1; j < distances.length; j++) {
//				if (maxDistance < distances[i][j]) {
//					maxDistance = distances[i][j];
//				}
//				if (maxTraffic < traffic[i][j]) {
//					maxTraffic = traffic[i][j];
//				}
//			}
//		}
//		for (int i = 0; i < distances.length; i++) {
//			distances[i][i] = 0.0;
//			traffic[i][i] = 0.0;
//			for (int j = i + 1; j < distances.length; j++) {
//				distances[i][j] = .8 * ((maxDistance - distances[i][j]) / maxDistance);
//				distances[j][i] = .8 * ((maxDistance - distances[i][j]) / maxDistance);
//				traffic[i][j] = 0.99 * (traffic[i][j] / maxTraffic);
//				traffic[j][i] = 0.99 * (traffic[i][j] / maxTraffic);
//			}
//		}

		List<Initializer<Integer>> algorithms = new Vector<>();
		// algorithms.add(new HybridInitializer(numNodes, minDensity,
		// maxDensity, traffic, distances, ampLabel, 0));
		// algorithms.add(new SWUniformDensityTrafficInitializer(numNodes,
		// minDensity, maxDensity, traffic, ampLabel, "swtud"));
//		algorithms.add(new SWDistanceInitializer(numNodes, minDensity, maxDensity, traffic, ampLabel, "swt"));
//		algorithms
//				.add(new PowerLawCustomInitializer(numNodes, minDensity, maxDensity, traffic, distances, ampLabel, 0));
//		algorithms.add(new WSInitializer(numNodes, minDensity, maxDensity, 0.15, ampLabel));
//		algorithms.add(new BANetworkInitializer(numNodes, minDensity, maxDensity, 1.00, ampLabel));
//		algorithms.add(new RandomNetworkInitializer(numNodes, minDensity, maxDensity, ampLabel));
//		algorithms.add(new RandomIsdaInitializer());
//		algorithms.add(new OriginalRandomInitializer());
		// algorithms.add(new SWDistanceInitializer(numNodes, minDensity,
		// maxDensity, distances, ampLabel, "swd"));
		algorithms.add(new WaxmanInitializer(numNodes, minDensity, maxDensity, locations, ampLabel, "wxm")); 
//		algorithms.add(new GabrielInitializer(numNodes, minDensity, maxDensity, locations, ampLabel, "gbr")); 
//		algorithms.add(new GeometricInitializer(numNodes, minDensity, maxDensity, locations, ampLabel, "geo")); 

		SolutionSet<Integer> popRnd = null;
		StringBuffer metrics = new StringBuffer();
		
		File pDir = new File("results/ini_" + strNet);
		if (!pDir.exists()) {
			pDir.mkdirs();
		}
		for (Initializer<Integer> alg : algorithms) { 
			metrics = new StringBuffer();
			for (int i = 0; i < nRuns; i++) {
				popRnd = ranking.getRankedSolutions(alg.execute(problem, size))[0];

				popRnd.printVariablesToFile("results/ini_" + strNet + "/" + alg.getOpID() + "-" + i + "-var.txt");
				popRnd.printObjectivesToFile("results/ini_" + strNet + "/" + alg.getOpID() + "-" + i + "-pf.txt");

				metrics.append(String.format("%.6f %.6f %.6f %d \n", hyper.getValue(popRnd), ms.getValue(popRnd),
						spacing.getValue(popRnd), popRnd.size()));
			}
			printMetrics(metrics, "results/ini_" + strNet + "/" + alg.getOpID() + "-metrics.txt");
		}
	}

	public static void printMetrics(StringBuffer content, String path) {
		try {
			FileWriter fw = new FileWriter(new File(path));
			fw.write(content.toString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
