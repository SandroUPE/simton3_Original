/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: MOEA_MainNU.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	13/06/2015		Versão inicial
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
import br.grna.bp.RedeNeural;
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
public class MOEA_MainNU {
	public static void main(String[] args) {
		MOEA_MainNU mainClass = new MOEA_MainNU();
		// String network = "hse_medianet_hessen_germany";// 150
		String network = "ftlg_regional_usa";// 300
		// String network = "arnes";//300
		int load = 300;

		if (args.length > 0) {
			network = args[0];
			load = Integer.parseInt(args[1]);
		}

		mainClass.simulate(network, load);
	}

	public void simulate(String network, int load) {
		String strNet = network + ".gml";
		String strNetS = network + "_u" + load + "e";
		String basePath = "results/";

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

		Problem<Integer> problem = null;

		problem = new GmlSimton(numNodes, 2, data, load);
		((GmlSimton) problem).setPathFiles(basePath + strNetS + "/");
		for (int i = 0; i < 11; i++) {
			MOEA_Topology moea = new MOEA_Topology(50, 1000, problem);
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