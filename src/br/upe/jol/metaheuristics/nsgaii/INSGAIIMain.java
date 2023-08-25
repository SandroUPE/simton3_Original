package br.upe.jol.metaheuristics.nsgaii;

import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.SimtonProblem;

public class INSGAIIMain {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimtonProblem problem = new SimtonProblem(14, 2);
		INSGAII nsgaii = new INSGAII(50, 10000, problem);
		nsgaii.setPathFiles("C:\\Temp_sony\\exp20130416\\");

//		SolutionSet<Integer> ss = new SolutionSet<Integer>();
//		ss.readIntVariablesFromFile("C:\\Temp_sony\\exp20130416\\_nsgaii_C2_M3_50_1,00_0,06_9.990_var.txt", problem);
//		for (Solution<Integer> sol : ss.getSolutionsList()){
//			problem.evaluate(sol);
//		}
//			
//		SolutionSet<Integer> solutions1 = nsgaii.execute(ss, 9995);

		
		SolutionSet<Integer> solutions1 = nsgaii.execute();

//		solutions1.printObjectivesToFile("C:\\Temp\\exp1\\ONTD.txt");
		
	}
	
	public static void main1(String[] args) {
		SimtonProblem ontd = new SimtonProblem(14, 2);
		INSGAII nsgaii = new INSGAII(20, 1005, ontd);
		SolutionSet<Integer> ssTotal = new SolutionSet<Integer>();
		ssTotal.setCapacity(250);
		nsgaii.setPathFiles("D:\\Temp\\results_cluster\\");
				
//		SolutionSet<Integer> ss = new SolutionSet<Integer>();
//		ss.readIntVariablesFromFile("D:\\Temp\\results_cluster\\nsgaii_1\\._NSGAII_VAR_1543.txt", ontd);
//		for (Solution<Integer> sol : ss.getSolutionsList()){
//			ontd.evaluate(sol);
//			ssTotal.getSolutionsList().add(sol);
//		}
		
//		SolutionSet<Integer> ss1 = new SolutionSet<Integer>();
//		ss1.readIntVariablesFromFile("D:\\Temp\\results_cluster\\nsgaii_2\\._NSGAII_VAR_1730.txt", ontd);
//		for (Solution<Integer> sol : ss1.getSolutionsList()){
//			ontd.evaluate(sol);
//			ssTotal.getSolutionsList().add(sol);
//		}
//		
//		SolutionSet<Integer> ss2 = new SolutionSet<Integer>();
//		ss2.readIntVariablesFromFile("D:\\Temp\\results_cluster\\nsgaii_3\\._NSGAII_VAR_2746.txt", ontd);
//		for (Solution<Integer> sol : ss2.getSolutionsList()){
//			ontd.evaluate(sol);
//			ssTotal.getSolutionsList().add(sol);
//		}
		
		SolutionSet<Integer> ss3 = new SolutionSet<Integer>();
		ss3.readIntVariablesFromFile("D:\\Temp\\results_cluster\\nsgaii_4\\._NSGAII_VAR_3125.txt", ontd);
		for (Solution<Integer> sol : ss3.getSolutionsList()){
			ontd.evaluate(sol);
			ssTotal.getSolutionsList().add(sol);
		}
		
//		SolutionSet<Integer> ss4 = new SolutionSet<Integer>();
//		ss4.readIntVariablesFromFile("D:\\Temp\\results_cluster\\nsgaii_5\\._NSGAII_VAR_1647.txt", ontd);
//		for (Solution<Integer> sol : ss4.getSolutionsList()){
//			ontd.evaluate(sol);
//			ssTotal.getSolutionsList().add(sol);
//		}
		
//		SolutionSet<Integer> solutions1 = nsgaii.execute();
		SolutionSet<Integer> solutions1 = nsgaii.execute(ssTotal, 1000);
		
		solutions1.printObjectivesToFile("./results/ONTD.txt");
		
	}
	
	public static void main5(String[] args) {
		SimtonProblem ontd = new SimtonProblem(14, 2);
		SolutionSet<Integer> ss3 = new SolutionSet<Integer>();
		ss3.readIntVariablesFromFile("C:\\Temp\\exp1\\_nsgaii_C2_M3_50_1,00_0,40_9.900_var.txt", ontd);
		SolutionSet<Integer> ssTotal = new SolutionSet<Integer>();
		for (Solution<Integer> sol : ss3.getSolutionsList()){
			ontd.evaluate(sol);
			ssTotal.getSolutionsList().add(sol);
		}
		ssTotal.printObjectivesToFile("C:\\Temp\\exp1\\avaliacao_pb.txt");
	}

}
