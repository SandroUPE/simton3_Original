package br.upe.jol.problems.surrogateWRON;
/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: RevalidateSolutionSet.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	04/01/2014		Versão inicial
 * ****************************************************************************
 */


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
 * @author Danilo
 * @since 04/01/2014
 */
public class RevalidateSolutionSet {
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
//		String strNet = "hse_medianet_hessen_germany"; //150
//		String strNet = "ftlg_regional_usa";//200
		String strNet = "arnes";//200
		int load = 300; 
		String base1 = "results/";
		String basePath = "results/" + strNet + "_u" + load + "e/0/";
		GmlDao dao = new GmlDao();
		GmlData data = dao.loadGmlData(base1 + strNet + ".gml");
		int numNodes = data.getNodes().size();
//		Double[][] traffic = createGravityTraffic(basePath, numNodes, strNet, data);
		Double[][] traffic = createUniformMatrix(numNodes);
		CallSchedulerNonUniformHub.TRAFFICMATRIX = traffic;
		Geolocation[] locations = new Geolocation[numNodes];
		for (int i = 0; i < locations.length; i++) {
			locations[i] = new Geolocation(data.getNodes().get(i).getLatitude(), data.getNodes().get(i).getLongitude());
		}
		GmlSimton problem = new GmlSimton(numNodes, 2, data, load);
		String iteration = "0.035";
		SolutionSet<Integer> ss = new SolutionSet<>(50);
		String inFile = basePath + "_top_M3_50_1,00_0,06_" + iteration + "_var.txt";
		String inPfFile = basePath + "_top_M3_50_1,00_0,06_" + iteration + "_pf.txt";
		String outVarFile = basePath + "_top_M3_50_1,00_0,06_" + iteration + "_var_rev.txt";
		String outPfFile = basePath + "_top_M3_50_1,00_0,06_" + iteration + "_pf_rev.txt";
		
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
	
	public static void main4(String[] args) {
		SimtonProblem problem = new SimtonProblem(14, 2); 
		SolutionSet<Integer> ss = new SolutionSet<>(50);
		problem.setSimulate(true);
//		String basePath = "C:/doutorado/submissões/elsarticle/eaai_2014/experiments/isda_amp_unico_non_uniform/2/";
//		String basePath = "C:/doutorado/submissões/elsarticle/eaai_2014/experiments/surrogate1/9/";
		String basePath = "C:/workspace_phd/Icton15/results/arnes_u100e/0/";
		String inFile = basePath + "_top_M3_50_1,00_0,06_1.000_var.txt";
		String outVarFile = basePath + "_top_M3_50_1,00_0,06_1.000_var_rev.txt";
		String outPfFile = basePath + "_top_M3_50_1,00_0,06_1.000_pf_rev.txt";
		
		ss.readIntVariablesFromFile(inFile, problem);
		
		for (Solution<Integer> solution : ss.getSolutionsList()) {
			problem.evaluate(solution);
		}
		
		ss.printObjectivesToFile(outPfFile);
		ss.printVariablesToFile(outVarFile);
	}
	
	public static void main1(String[] args) {
		Problem problem = MOEA_GabrielBased.DEFAULT_PROBLEM;
		SolutionSet<Integer> ss = new SolutionSet<>(50);
		String basePath = MOEA_GabrielBased.basePath + "/moea_isda_start_40e/0/";
		String inFile = basePath + "_top_M3_50_1,00_0,06_1.000_var.txt";
		String outVarFile = basePath + "_top_M3_50_1,00_0,06_1.000_var_rev2.txt";
		String outPfFile = basePath + "_top_M3_50_1,00_0,06_1.000_pf_rev2.txt";
		int evaluations = 3;
		ss.readIntVariablesFromFile(inFile, problem);
		double pbm = 0;
		
		for (Solution<Integer> solution : ss.getSolutionsList()) {
			pbm = 0;
			for (int i = 0; i < evaluations; i++) {
				problem.evaluate(solution);
				pbm += solution.getObjective(0); 
			}
			pbm /= evaluations;
			solution.setObjective(0, pbm);
		}
		
		RankUtil<Integer> ranking = RankUtil.getIntegerInstance();
		ss = ranking.getRankedSolutions(ss)[0];
		
		ss.printObjectivesToFile(outPfFile);
		ss.printVariablesToFile(outVarFile);
	}
	
	public static void main2(String[] args) {
		Problem problem = MOEA_GabrielBased.DEFAULT_PROBLEM;
		SolutionSet<Integer> ss = new SolutionSet<>(50);
		String basePath = MOEA_GabrielBased.basePath + "/results_new/0t_l40_IowaStatewideFiberMap.gml/";
		String inFile = basePath + "tg_pgeo_IowaStatewideFiberMap.gml-0-var.txt";
		String outVarFile = basePath + "tg_pgeo_IowaStatewideFiberMap.gml-0-var_rev2.txt";
		String outPfFile = basePath + "tg_pgeo_IowaStatewideFiberMap.gml-0-pf_rev2.txt";
		int evaluations = 3;
		ss.readIntVariablesFromFile(inFile, problem);
		double pbm = 0;
		
		for (Solution<Integer> solution : ss.getSolutionsList()) {
			pbm = 0;
			for (int i = 0; i < evaluations; i++) {
				problem.evaluate(solution);
				pbm += solution.getObjective(0); 
			}
			pbm /= evaluations;
			solution.setObjective(0, pbm);
		}
		
		RankUtil<Integer> ranking = RankUtil.getIntegerInstance();
		ss = ranking.getRankedSolutions(ss)[0];
		
		ss.printObjectivesToFile(outPfFile);
		ss.printVariablesToFile(outVarFile);
	}
}
