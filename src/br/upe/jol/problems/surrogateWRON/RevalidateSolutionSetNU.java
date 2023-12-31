/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: RevalidateSolutionSetNU.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	13/06/2015		Vers�o inicial
 * ****************************************************************************
 */
package br.upe.jol.problems.surrogateWRON;

import java.io.FileWriter;
import java.io.IOException;

import br.cns.Geolocation;
import br.cns.GravityModel;
import br.cns.model.GmlData;
import br.cns.persistence.GmlDao;
import br.upe.jol.base.Problem;
import br.upe.jol.base.RankUtil;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.metaheuristics.MOEA_GabrielBased;
import br.upe.jol.problems.simon.entity.CallSchedulerNonUniformHub;
import br.upe.jol.problems.simton.GmlSimton;
import br.upe.jol.problems.simton.SimtonProblem;

/**
 * 
 * @author Danilo Araujo
 * @since 13/06/2015
 */
public class RevalidateSolutionSetNU {
	private static Double[][] createGravityTraffic(String basePath, int numNodes, String strNet, GmlData data) {
		GravityModel gm = new GravityModel(data);
		Double[][] traffic = gm.getTrafficMatrix();

		StringBuffer sbTrafficMatrix = new StringBuffer();
		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) {
				sbTrafficMatrix.append(String.format("%.6f ", traffic[i][j]));
			}
			sbTrafficMatrix.append("\n");
		}
		try {
			FileWriter fw = new FileWriter("results/" + strNet + "_tm.txt");
			fw.write(sbTrafficMatrix.toString());
			fw.close();
		} catch (IOException e) {
		}
		CallSchedulerNonUniformHub.TRAFFICMATRIX = traffic;
		return traffic;
	}
	
	public static void main(String[] args) {
//		String strNet = "hse_medianet_hessen_germany"; //50
//		String strNet = "ftlg_regional_usa";//200
		String strNet = "arnes";//120
		int load = 120; 
		String base1 = "results_nonuniform/";
		String basePath = "results_nonuniform/" + strNet + "_u" + load + "e/0/";
		GmlDao dao = new GmlDao();
		GmlData data = dao.loadGmlData(base1 + strNet + ".gml");
		int numNodes = data.getNodes().size();
		Double[][] traffic = createGravityTraffic(basePath, numNodes, strNet, data);
		CallSchedulerNonUniformHub.TRAFFICMATRIX = traffic;
		Geolocation[] locations = new Geolocation[numNodes];
		for (int i = 0; i < locations.length; i++) {
			locations[i] = new Geolocation(data.getNodes().get(i).getLatitude(), data.getNodes().get(i).getLongitude());
		}
		GmlSimton problem = new GmlSimton(numNodes, 2, data, load);
		
		SolutionSet<Integer> ss = new SolutionSet<>(50);
		String inFile = basePath + "_top_M3_50_1,00_0,06_1.000_var.txt";
		String inPfFile = basePath + "_top_M3_50_1,00_0,06_1.000_pf.txt";
		String outVarFile = basePath + "_top_M3_50_1,00_0,06_1.000_var_rev.txt";
		String outPfFile = basePath + "_top_M3_50_1,00_0,06_1.000_pf_rev.txt";
		
		ss.readIntVariablesFromFile(inFile, problem);
		ss.readExistingObjectivesFromFile(inPfFile, problem);
		
		SolutionSet<Integer> ss1 = new SolutionSet<>(50);
		for (Solution<Integer> sol : ss.getSolutionsList()) {
			ss1.add(sol.clone());
		}
		
		for (Solution<Integer> solution : ss1.getSolutionsList()) {
			problem.evaluate(solution);
		}
		
		ss1.printObjectivesToFile(outPfFile);
		ss1.printVariablesToFile(outVarFile);
	}
	
	public static Double[][] createUniformMatrix(int numNodes){
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
