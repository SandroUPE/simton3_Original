/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: Moea_Topology_MainNU.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	13/06/2015		Vers�o inicial
 * ****************************************************************************
 */
package br.upe.jol.problems.surrogateWRON;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import br.cns.Geolocation;
import br.cns.GravityModel;
import br.cns.model.GmlData;
import br.cns.persistence.GmlDao;
import br.upe.jol.base.Problem;
import br.upe.jol.metaheuristics.MOEA_Topology;
import br.upe.jol.operators.initializers.SWDistanceInitializer;
import br.upe.jol.problems.simon.entity.CallSchedulerNonUniformHub;
import br.upe.jol.problems.simton.GmlSimton;

/**
 * 
 * @author Danilo Araujo
 * @since 13/06/2015
 */
public class Moea_Topology_MainNU {
	public static void main(String[] args) {
		Moea_Topology_MainNU mainClass = new Moea_Topology_MainNU();
		 String network = "hse_medianet_hessen_germany";// 50
//		String network = "ftlg_regional_usa";// 300
		// String network = "arnes";//300
		int load = 50;
		boolean csm = true;

		if (args.length > 0) {
			network = args[0];
			load = Integer.parseInt(args[1]);
			if ("true".equals(args[2])) {
				csm = true;
			}
		}

		mainClass.simulate(network, load, csm);
	}

	public void simulate(String network, int load, boolean csm) {
		String strNet = network + ".gml";
		String strNetS = network + "_u" + load + "e";
		String basePath = "results_nonuniform/";

		GmlDao dao = new GmlDao();
		GmlData data = dao.loadGmlData(basePath + strNet);
		int numNodes = data.getNodes().size();
		Double[][] traffic = createGravityTraffic(basePath, numNodes, strNetS, data);
		CallSchedulerNonUniformHub.TRAFFICMATRIX = traffic;
		Geolocation[] locations = new Geolocation[numNodes];
		File newDir = new File(basePath + "/" + strNetS + "/");
		newDir.mkdir();
		for (int i = 0; i < locations.length; i++) {
			locations[i] = new Geolocation(data.getNodes().get(i).getLatitude(), data.getNodes().get(i).getLongitude());
		}

		GmlSimton problem = new GmlSimton(numNodes, 2, data, load);
		
//		EaCsmNU problem = new EaCsmNU(numNodes, 2, data, load);
		problem.setPathFiles(basePath + strNetS + "/");
		for (int i = 0; i < 11; i++) {
			MOEA_Topology moea = new MOEA_Topology(50, 1000, problem);
			moea.setIni(new SWDistanceInitializer(numNodes, 0.06, 0.4, traffic, 3, "swt"));
			newDir = new File(basePath + strNetS + "/" + i + "/");
			newDir.mkdir();
			moea.setPathFiles(basePath + strNetS + "/" + i + "/");

			moea.execute();
		}
	}

	private static Double[][] createGravityTraffic(String basePath, int max, String strNet, GmlData data) {
		GravityModel gm = new GravityModel(data);
		Double[][] traffic = gm.getTrafficMatrix();

		StringBuffer sbTrafficMatrix = new StringBuffer();
		for (int i = 0; i < max; i++) {
			for (int j = 0; j < max; j++) {
				sbTrafficMatrix.append(String.format("%.4f ", traffic[i][j]));
			}
			sbTrafficMatrix.append("\n");
		}
		try {
			FileWriter fw = new FileWriter(basePath + "results/" + strNet + "_tm.txt");
			fw.write(sbTrafficMatrix.toString());
			fw.close();
		} catch (IOException e) {
		}
		CallSchedulerNonUniformHub.TRAFFICMATRIX = traffic;
		return traffic;
	}

	public static Double[][] createUniformMatrix(int numNodes) {
		Double[][] traffic = new Double[numNodes][numNodes];

		for (int i = 0; i < numNodes; i++) {
			for (int j = i + 1; j < numNodes; j++) {
				traffic[i][j] = 1.0;
				traffic[j][i] = 1.0;
			}
		}

		return traffic;
	}
}