package br.upe.jol.experiments;

import static br.upe.jol.base.Util.LOGGER;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import jsc.datastructures.PairedData;
import jsc.onesample.WilcoxonTest;
import jsc.tests.H1;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.metrics.Coverage;
import br.upe.jol.metrics.Hypervolume;
import br.upe.jol.metrics.MaximumSpread;
import br.upe.jol.metrics.Spacing;
import br.upe.jol.problems.simton.SimtonProblem;
import br.upe.jol.problems.simton.SimonProblem_3Obj;

public class MetricsFromVarFile {
	private static final String dirGrav = "D:\\Temp\\";
	private static SimtonProblem problem = new SimtonProblem(14, 2);
	private static SimonProblem_3Obj problem3o = new SimonProblem_3Obj(14, 3);
	private static Hypervolume<Double> hv = new Hypervolume<Double>();
	private static MaximumSpread<Double> ms = new MaximumSpread<Double>();
	private static Spacing<Double> spacing = new Spacing<Double>();
	private static Coverage<Double> cov = new Coverage<Double>();
	private static NumberFormat nf = NumberFormat.getInstance();
	private static final NumberFormat nfi = NumberFormat.getInstance();
	private static final double alfaDefault = 0.1;
	private static final double medianCSDefault = 0.5;
	
	private static int getIndiceMelhorCS(List<SolutionSet<Double>> solucoes, Coverage<Double> cov){
		int idMelhor = 0;
		//descobrir a melhor solução com relação ao cs
		double[] csValues = new double[solucoes.size()];
		int iSol1 = 0;
		for (SolutionSet<Double> sol : solucoes){
			cov.setParetoKnown(sol);
			int iSol2 = 0;
			for (SolutionSet<Double> sol1 : solucoes){
				if (iSol1 != iSol2){
					csValues[iSol1] += cov.getValue(sol1);
				}
				iSol2++;
			}
			iSol1++;
		}
		for (int m = 0; m < csValues.length; m++){
			if (csValues[m] > csValues[idMelhor]){
				idMelhor = m;
			}
		}
		return idMelhor;
	}
	static{
		nf.setMaximumFractionDigits(5);
		nf.setMinimumFractionDigits(5);
		nfi.setMaximumFractionDigits(0);
		nfi.setMinimumFractionDigits(0);
		nfi.setMinimumIntegerDigits(2);
	}
	
	public static void main_nsgaii(String[] args) {
		List<SolutionSet<Double>> solucoes = new Vector<SolutionSet<Double>>();
		int qtdeExps = 4;
		int qteExec = 10;
		int idRef = 3;
		for (int i = 0; i < qtdeExps; i++){
			solucoes.add(new SolutionSet<Double>());	
		}
		
		double[][] hvValues = new double[qtdeExps][qteExec];
		double[][] msValues = new double[qtdeExps][qteExec];
		double[][] spacingValues = new double[qtdeExps][qteExec];
		double[][] cs1Values = new double[qtdeExps-1][qteExec];
		double[][] cs2Values = new double[qtdeExps-1][qteExec];
		
		for (int i = 1; i <= qteExec; i++) {
			solucoes.get(0).readIntVariablesFromFile("D:\\Temp\\results_cluster\\nsgaii_" + i + "\\._nsgaii_var_990.txt", problem);
			readObjectivesFromFile(solucoes.get(0), "D:\\Temp\\results_cluster\\nsgaii_" + i + "\\._nsgaii_pf_990.txt");
			solucoes.get(1).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil2\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_006_0990_var.txt", problem);
			readObjectivesFromFile(solucoes.get(1), "H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil2\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_006_0990_pf.txt");
			solucoes.get(2).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil3\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_003_0990_var.txt", problem);
			readObjectivesFromFile(solucoes.get(2), "H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil3\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_003_0990_pf.txt");
			solucoes.get(3).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil4\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_001_0990_var.txt", problem);
			readObjectivesFromFile(solucoes.get(3), "H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil4\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_001_0990_pf.txt");
			
			for (int s = 0; s < hvValues.length; s++){
				hvValues[s][i-1] = hv.getValue(solucoes.get(s));
				msValues[s][i-1] = ms.getValue(solucoes.get(s));
				spacingValues[s][i-1] = spacing.getValue(solucoes.get(s));
			}
			
			cov.setParetoKnown(solucoes.get(idRef));
			for (int s = 0; s < cs1Values.length; s++){
				if (s < idRef){
					cs1Values[s][i-1] = cov.getValue(solucoes.get(s));	
				}else if (s >= idRef){
					cs1Values[s][i-1] = cov.getValue(solucoes.get(s+1));	
				}
			}
			
			for (int s = 0; s < cs2Values.length; s++){
				if (s < idRef){
					cov.setParetoKnown(solucoes.get(s));
				}else if (s >= idRef){
					cov.setParetoKnown(solucoes.get(s+1));
				}
				cs2Values[s][i-1] = cov.getValue(solucoes.get(idRef));
			}
			
		}
		buildResultsMOOMetrics("NSGAII", hvValues, msValues, spacingValues, cs1Values, cs2Values, idRef);
	}
	
	private static final void buildResultsMOOMetrics(String algoritmo, double[][] hvValues, double[][] msValues, double[][] spacingValues, double[][] cs1Values, double[][] cs2Values, int idRef){
		builldWilcoxonLatexTable("Teste de Wilcoxon para o indicador HV na Avaliação Paramétrica do " + algoritmo, "hv_" + algoritmo.toLowerCase(), algoritmo, hvValues, alfaDefault, true);
		builldWilcoxonLatexTable("Teste de Wilcoxon para o indicador MS na Avaliação Paramétrica do " + algoritmo, "ms_" + algoritmo.toLowerCase(), algoritmo, msValues, alfaDefault, true);
		builldWilcoxonLatexTable("Teste de Wilcoxon para o indicador Spacing na Avaliação Paramétrica do " + algoritmo, "spacing_" + algoritmo.toLowerCase(), algoritmo, spacingValues, alfaDefault, false);
		builldWilcoxonLatexTableCS("Teste de Wilcoxon para o indicador CS na Avaliação Paramétrica do " + algoritmo, "cs1_" + algoritmo.toLowerCase(), algoritmo, cs1Values, cs2Values, alfaDefault, medianCSDefault, idRef);
		
		showResultsConsole(hvValues, msValues, spacingValues, cs1Values, cs2Values, idRef, algoritmo);
	}
	
	private static void builldWilcoxonLatexTable(String titulo, String tituloTab, String prefAlgo, double[][] experimento, double significanciaMin, boolean max){
		WilcoxonTest test = null;
		H1 h1 = H1.LESS_THAN;
		if (max){
			h1 = H1.GREATER_THAN;
		}
		System.out.println();
		System.out.println("\\begin{table}[!ht]");
		System.out.println("\\caption{" + titulo + ".}");
		System.out.println("\\label{Table:" + tituloTab + "}");
		System.out.println("\\centering");
		System.out.println("\\begin{scriptsize}");
		System.out.print("\\begin{tabular}{l");
		for (int i = 0; i < experimento.length; i++){
			System.out.print("c");
		}
		System.out.println("}");
		System.out.println("\\hline");
		for (int i = 0; i < experimento.length; i++){
			System.out.print("& " + prefAlgo + " " + (i+1));
		}
		System.out.println("\\\\\\hline");
		for (int i = 0; i < experimento.length; i++){
			System.out.print(prefAlgo + " " + (i+1) + " ");
			for (int j = 0; j < experimento.length; j++){
				if (i == j){
					System.out.print(" & ");
					continue;
				}
				test = new WilcoxonTest(new PairedData(experimento[i], experimento[j]), h1);
				if (test.getSP() < significanciaMin){
					System.out.print(" & $\\blacktriangle$ ");
				}else {
					test = new WilcoxonTest(new PairedData(experimento[j], experimento[i]), h1);
					if (test.getSP() < significanciaMin){
						System.out.print(" & $\\triangledown$ ");
					}else {
						System.out.print(" & - ");
					}
				}
			}	
			System.out.println("\\\\");
		}
		System.out.println("\\hline");
		System.out.println("\\end{tabular}");
		System.out.println("\\end{scriptsize}");
		System.out.println("\\end{table}");
		System.out.println();
	}
	
	private static void builldWilcoxonLatexTableCS(String titulo, String tituloTab, String prefAlgo, double[][] experimento1, double[][] experimento2, double significanciaMin, double median, int idRef){
		WilcoxonTest test = null;
		H1 h1 = H1.GREATER_THAN;
		System.out.println();
		System.out.println("\\begin{table}[h]");
		System.out.println("\\caption{" + titulo + ".}");
		System.out.println("\\label{Table:" + tituloTab + "}");
		System.out.println("\\centering");
		System.out.println("\\begin{scriptsize}");
		System.out.print("\\begin{tabular}{l");
		for (int i = 0; i < experimento1.length; i++){
			System.out.print("c");
		}
		System.out.println("}");
		System.out.println("\\hline");
		for (int i = 0; i < experimento1.length; i++){
			if (i >= idRef){
				System.out.print("& " + prefAlgo + " " + (i+2));
			}else{
				System.out.print("& " + prefAlgo + " " + (i+1));	
			}
		}
		System.out.println("\\\\\\hline");
		System.out.print(" CS(" + prefAlgo + " " + (idRef + 1) + ", *)");
		for (int i = 0; i < experimento1.length; i++){
			test = new WilcoxonTest(experimento1[i], median, h1);
			if (test.getSP() < significanciaMin){
				System.out.print(" & $\\blacktriangle$ ");
			}else {
				System.out.print(" & $\\triangledown$ ");
			}
		}
		h1 = H1.LESS_THAN;
		System.out.println("\\\\\\hline");
		System.out.print(" CS(*, " + prefAlgo  + " " + (idRef + 1) + ")");
		for (int i = 0; i < experimento2.length; i++){
			test = new WilcoxonTest(experimento2[i], median, h1);
			if (test.getSP() < significanciaMin){
				System.out.print(" & $\\blacktriangle$ ");
			}else {
				System.out.print(" & $\\triangledown$ ");
			}
		}
		
		System.out.println("\\\\\\hline");
		System.out.println("\\end{tabular}");
		System.out.println("\\end{scriptsize}");
		System.out.println("\\end{table}");
		System.out.println();
	}
	
	
	public static void main_nsgaii_pref(String[] args) {
		List<SolutionSet<Double>> solucoes = new Vector<SolutionSet<Double>>();
		solucoes.add(new SolutionSet<Double>());
		solucoes.add(new SolutionSet<Double>());
		
		double[][] hvValues = new double[2][10];
		double[][] msValues = new double[2][10];
		double[][] spacingValues = new double[2][10];
		
		String strHv = "";
		String strMS = "";
		String strSpacing = "";
		String strC1 = "";
		String strC2 = "";
		int qteExp = 10;
		for (int i = 1; i <= qteExp; i++) {
			solucoes.get(0).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\pref\\r" + nfi.format(i) + "\\._nsgaii_C2_M3_50_1.00_0.03_0,990_var.txt", problem);
			readObjectivesFromFile(solucoes.get(0), "H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\pref\\r" + nfi.format(i) + "\\._nsgaii_C2_M3_50_1.00_0.03_0,990_pf.txt");
			solucoes.get(1).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\pref1\\r" + nfi.format(i) + "\\_nsgaii1_C2_M3_50_100_003_0990_var.txt", problem);
			readObjectivesFromFile(solucoes.get(1), "H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\pref1\\r" + nfi.format(i) + "\\_nsgaii1_C2_M3_50_100_003_0990_pf.txt");
			
			hvValues[0][i-1] = hv.getValue(solucoes.get(0));
			hvValues[1][i-1] = hv.getValue(solucoes.get(1));
			msValues[0][i-1] = ms.getValue(solucoes.get(0));
			msValues[1][i-1] = ms.getValue(solucoes.get(1));
			spacingValues[0][i-1] = spacing.getValue(solucoes.get(0));
			spacingValues[1][i-1] = spacing.getValue(solucoes.get(1));
			
			strHv += getStrHV(solucoes, 2);
			strMS += getStrMS(solucoes, 2);
			strSpacing += getStrSpacing(solucoes, 2);
			
			
			cov.setParetoKnown(solucoes.get(1));
			strC1 += nf.format(cov.getValue(solucoes.get(0))) + "\n";
			cov.setParetoKnown(solucoes.get(0));
			strC2 += nf.format(cov.getValue(solucoes.get(1))) + "\n";
		}
		
		WilcoxonTest test = new WilcoxonTest(new PairedData(hvValues[0], hvValues[1]), H1.GREATER_THAN);
		System.out.println(test.approxSP() + "-" + test.exactSP());
		test = new WilcoxonTest(new PairedData(msValues[0], msValues[1]), H1.GREATER_THAN);
		System.out.println(test.approxSP() + "-" + test.exactSP());
		test = new WilcoxonTest(new PairedData(spacingValues[0], spacingValues[1]), H1.LESS_THAN);
		System.out.println(test.approxSP() + "-" + test.exactSP());
		
		showResultsConsole(strHv, strMS, strSpacing, strC1, strC2, 1, "NSGAII");
	}
	
	public static void main_nsgaii_sum(String[] args) {
		List<SolutionSet<Double>> solucoes = new Vector<SolutionSet<Double>>();
		int qtdeExps = 2;
		int qteExec = 8;
		int idRef = 1;
		for (int i = 0; i < qtdeExps; i++){
			solucoes.add(new SolutionSet<Double>());	
		}
		
		double[][] hvValues = new double[qtdeExps][qteExec];
		double[][] msValues = new double[qtdeExps][qteExec];
		double[][] spacingValues = new double[qtdeExps][qteExec];
		double[][] cs1Values = new double[qtdeExps-1][qteExec];
		double[][] cs2Values = new double[qtdeExps-1][qteExec];
		
		for (int i = 1; i <= qteExec; i++) {
			solucoes.get(0).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\sum\\r" + nfi.format(i) + "\\._nsgaii_C1_M8_50_1.00_0.03_0,990_var.txt", problem);
			readObjectivesFromFile(solucoes.get(0), "H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\sum\\r" + nfi.format(i) + "\\._nsgaii_C1_M8_50_1.00_0.03_0,990_pf.txt");
			solucoes.get(1).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil3\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_003_0990_var.txt", problem);
			readObjectivesFromFile(solucoes.get(1), "H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil3\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_003_0990_pf.txt");
			for (int s = 0; s < hvValues.length; s++){
				hvValues[s][i-1] = hv.getValue(solucoes.get(s));
				msValues[s][i-1] = ms.getValue(solucoes.get(s));
				spacingValues[s][i-1] = spacing.getValue(solucoes.get(s));
			}
			
			cov.setParetoKnown(solucoes.get(idRef));
			for (int s = 0; s < cs1Values.length; s++){
				if (s < idRef){
					cs1Values[s][i-1] = cov.getValue(solucoes.get(s));	
				}else if (s >= idRef){
					cs1Values[s][i-1] = cov.getValue(solucoes.get(s+1));	
				}
			}
			
			for (int s = 0; s < cs2Values.length; s++){
				if (s < idRef){
					cov.setParetoKnown(solucoes.get(s));
				}else if (s >= idRef){
					cov.setParetoKnown(solucoes.get(s+1));
				}
				cs2Values[s][i-1] = cov.getValue(solucoes.get(idRef));
			}
		}
		buildResultsMOOMetrics("NSGAII_SUM", hvValues, msValues, spacingValues, cs1Values, cs2Values, idRef);
	}
	
	public static void main_nsgaii_bg(String[] args) {
		List<SolutionSet<Double>> solucoes = new Vector<SolutionSet<Double>>();
		int qtdeExps = 2;
		int qteExec = 6;
		int idRef = 0;
		for (int i = 0; i < qtdeExps; i++){
			solucoes.add(new SolutionSet<Double>());	
		}
		
		double[][] hvValues = new double[qtdeExps][qteExec];
		double[][] msValues = new double[qtdeExps][qteExec];
		double[][] spacingValues = new double[qtdeExps][qteExec];
		double[][] cs1Values = new double[qtdeExps-1][qteExec];
		double[][] cs2Values = new double[qtdeExps-1][qteExec];
		
		for (int i = 1; i <= qteExec; i++) {
			solucoes.get(0).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\bg\\r" + nfi.format(i) + "\\._nsgaii_C3_M1_50_1.00_0.03_0,990_var.txt", problem);
			readObjectivesFromFile(solucoes.get(0), "H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\bg\\r" + nfi.format(i) + "\\._nsgaii_C3_M1_50_1.00_0.03_0,990_pf.txt");
			solucoes.get(1).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil3\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_003_0990_var.txt", problem);
			readObjectivesFromFile(solucoes.get(1), "H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil3\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_003_0990_pf.txt");
			for (int s = 0; s < hvValues.length; s++){
				hvValues[s][i-1] = hv.getValue(solucoes.get(s));
				msValues[s][i-1] = ms.getValue(solucoes.get(s));
				spacingValues[s][i-1] = spacing.getValue(solucoes.get(s));
			}
			
			cov.setParetoKnown(solucoes.get(idRef));
			for (int s = 0; s < cs1Values.length; s++){
				if (s < idRef){
					cs1Values[s][i-1] = cov.getValue(solucoes.get(s));	
				}else if (s >= idRef){
					cs1Values[s][i-1] = cov.getValue(solucoes.get(s+1));	
				}
			}
			
			for (int s = 0; s < cs2Values.length; s++){
				if (s < idRef){
					cov.setParetoKnown(solucoes.get(s));
				}else if (s >= idRef){
					cov.setParetoKnown(solucoes.get(s+1));
				}
				cs2Values[s][i-1] = cov.getValue(solucoes.get(idRef));
			}
		}
		buildResultsMOOMetrics("NSGAII_BG", hvValues, msValues, spacingValues, cs1Values, cs2Values, idRef);
	}
	
	public static void main_nsgaii_cross(String[] args) {
		List<SolutionSet<Double>> solucoes = new Vector<SolutionSet<Double>>();
		int qtdeExps = 3;
		int qteExec = 7;
		int idRef = 2;
		for (int i = 0; i < qtdeExps; i++){
			solucoes.add(new SolutionSet<Double>());	
		}
		
		double[][] hvValues = new double[qtdeExps][qteExec];
		double[][] msValues = new double[qtdeExps][qteExec];
		double[][] spacingValues = new double[qtdeExps][qteExec];
		double[][] cs1Values = new double[qtdeExps-1][qteExec];
		double[][] cs2Values = new double[qtdeExps-1][qteExec];
		
		for (int i = 1; i <= qteExec; i++) {
			solucoes.get(0).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\c45\\r" + nfi.format(i) + "\\._nsgaii_C1_M3_50_1.00_0.03_0,990_var.txt", problem);
			readObjectivesFromFile(solucoes.get(0), "H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\c45\\r" + nfi.format(i) + "\\._nsgaii_C1_M3_50_1.00_0.03_0,990_pf.txt");
			solucoes.get(1).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\c31\\r" + nfi.format(i) + "\\_nsgaii_C1_M3_50_100_003_0990_var.txt", problem);
			readObjectivesFromFile(solucoes.get(1), "H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\c31\\r" + nfi.format(i) + "\\_nsgaii_C1_M3_50_100_003_0990_pf.txt");
			solucoes.get(2).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil3\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_003_0990_var.txt", problem);
			readObjectivesFromFile(solucoes.get(2), "H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil3\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_003_0990_pf.txt");
			for (int s = 0; s < hvValues.length; s++){
				hvValues[s][i-1] = hv.getValue(solucoes.get(s));
				msValues[s][i-1] = ms.getValue(solucoes.get(s));
				spacingValues[s][i-1] = spacing.getValue(solucoes.get(s));
			}
			
			cov.setParetoKnown(solucoes.get(idRef));
			for (int s = 0; s < cs1Values.length; s++){
				if (s < idRef){
					cs1Values[s][i-1] = cov.getValue(solucoes.get(s));	
				}else if (s >= idRef){
					cs1Values[s][i-1] = cov.getValue(solucoes.get(s+1));	
				}
			}
			
			for (int s = 0; s < cs2Values.length; s++){
				if (s < idRef){
					cov.setParetoKnown(solucoes.get(s));
				}else if (s >= idRef){
					cov.setParetoKnown(solucoes.get(s+1));
				}
				cs2Values[s][i-1] = cov.getValue(solucoes.get(idRef));
			}
		}
		buildResultsMOOMetrics("NSGAII_CRUZ", hvValues, msValues, spacingValues, cs1Values, cs2Values, idRef);
	}
	
	public static void main_spea2(String[] args) {
		List<SolutionSet<Double>> solucoes = new Vector<SolutionSet<Double>>();
		int qtdeExps = 4;
		int qteExec = 10;
		int idRef = 3;
		for (int i = 0; i < qtdeExps; i++){
			solucoes.add(new SolutionSet<Double>());	
		}
		
		double[][] hvValues = new double[qtdeExps][qteExec];
		double[][] msValues = new double[qtdeExps][qteExec];
		double[][] spacingValues = new double[qtdeExps][qteExec];
		double[][] cs1Values = new double[qtdeExps-1][qteExec];
		double[][] cs2Values = new double[qtdeExps-1][qteExec];
		
		for (int i = 1; i <= qteExec; i++) {
			solucoes.get(0).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\spea2\\perfil1\\r" + nfi.format(i) + "\\._spea2_50_100_0.90_0.10_1,000_var.txt", problem);
			readObjectivesFromFile(solucoes.get(0), "H:\\Mestrado\\Experimentos\\2objetivos\\spea2\\perfil1\\r" + nfi.format(i) + "\\._spea2_50_100_0.90_0.10_1,000_pf.txt");
			solucoes.get(1).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\spea2\\perfil2\\r" + nfi.format(i) + "\\._spea2_50_100_1.00_0.06_1,000_var.txt", problem);
			readObjectivesFromFile(solucoes.get(1), "H:\\Mestrado\\Experimentos\\2objetivos\\spea2\\perfil2\\r" + nfi.format(i) + "\\._spea2_50_100_1.00_0.06_1,000_pf.txt");
			solucoes.get(2).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\spea2\\perfil3\\r" + nfi.format(i) + "\\._spea2_50_100_1.00_0.03_1,000_var.txt", problem);
			readObjectivesFromFile(solucoes.get(2), "H:\\Mestrado\\Experimentos\\2objetivos\\spea2\\perfil3\\r" + nfi.format(i) + "\\._spea2_50_100_1.00_0.03_1,000_pf.txt");
			solucoes.get(3).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\spea2\\perfil4\\r" + nfi.format(i) + "\\_spea2_50_100_100_001_1000_var.txt", problem);
			readObjectivesFromFile(solucoes.get(3), "H:\\Mestrado\\Experimentos\\2objetivos\\spea2\\perfil4\\r" + nfi.format(i) + "\\_spea2_50_100_100_001_1000_pf.txt");

			for (int s = 0; s < hvValues.length; s++){
				hvValues[s][i-1] = hv.getValue(solucoes.get(s));
				msValues[s][i-1] = ms.getValue(solucoes.get(s));
				spacingValues[s][i-1] = spacing.getValue(solucoes.get(s));
			}
			
			cov.setParetoKnown(solucoes.get(idRef));
			for (int s = 0; s < cs1Values.length; s++){
				if (s < idRef){
					cs1Values[s][i-1] = cov.getValue(solucoes.get(s));	
				}else if (s >= idRef){
					cs1Values[s][i-1] = cov.getValue(solucoes.get(s+1));	
				}
			}
			
			for (int s = 0; s < cs2Values.length; s++){
				if (s < idRef){
					cov.setParetoKnown(solucoes.get(s));
				}else if (s >= idRef){
					cov.setParetoKnown(solucoes.get(s+1));
				}
				cs2Values[s][i-1] = cov.getValue(solucoes.get(idRef));
			}
		}
		buildResultsMOOMetrics("SPEA2", hvValues, msValues, spacingValues, cs1Values, cs2Values, idRef);
	}
	
	public static void main_mode(String[] args) {
		List<SolutionSet<Double>> solucoes = new Vector<SolutionSet<Double>>();
		int qtdeExps = 5;
		int qteExec = 10;
		int idRef = 3;
		for (int i = 0; i < qtdeExps; i++){
			solucoes.add(new SolutionSet<Double>());	
		}
		
		double[][] hvValues = new double[qtdeExps][qteExec];
		double[][] msValues = new double[qtdeExps][qteExec];
		double[][] spacingValues = new double[qtdeExps][qteExec];
		double[][] cs1Values = new double[qtdeExps-1][qteExec];
		double[][] cs2Values = new double[qtdeExps-1][qteExec];
		
		for (int i = 1; i <= qteExec; i++) {
			solucoes.get(0).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\mode\\perfil1\\r" + nfi.format(i) + "\\._mode_50_0.90_1,000_var.txt", problem);
			readObjectivesFromFile(solucoes.get(0), "H:\\Mestrado\\Experimentos\\2objetivos\\mode\\perfil1\\r" + nfi.format(i) + "\\._mode_50_0.90_1,000_pf.txt");
			solucoes.get(1).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\mode\\perfil2\\r" + nfi.format(i) + "\\._mode_50_0.60_1,000_var.txt", problem);
			readObjectivesFromFile(solucoes.get(1), "H:\\Mestrado\\Experimentos\\2objetivos\\mode\\perfil2\\r" + nfi.format(i) + "\\._mode_50_0.60_1,000_pf.txt");
			solucoes.get(2).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\mode\\perfil3\\r" + nfi.format(i) + "\\._mode_50_0.30_1,000_var.txt", problem);
			readObjectivesFromFile(solucoes.get(2), "H:\\Mestrado\\Experimentos\\2objetivos\\mode\\perfil3\\r" + nfi.format(i) + "\\._mode_50_0.30_1,000_pf.txt");
			solucoes.get(3).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\mode\\perfil4\\r" + nfi.format(i) + "\\._mode_100_0.30_1,000_var.txt", problem);
			readObjectivesFromFile(solucoes.get(3), "H:\\Mestrado\\Experimentos\\2objetivos\\mode\\perfil4\\r" + nfi.format(i) + "\\._mode_100_0.30_1,000_pf.txt");
			solucoes.get(4).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\mode\\perfil5\\r" + nfi.format(i) + "\\._mode_50_0.30_1,000_var.txt", problem);
			readObjectivesFromFile(solucoes.get(4), "H:\\Mestrado\\Experimentos\\2objetivos\\mode\\perfil5\\r" + nfi.format(i) + "\\._mode_50_0.30_1,000_pf.txt");
			for (int s = 0; s < hvValues.length; s++){
				hvValues[s][i-1] = hv.getValue(solucoes.get(s));
				msValues[s][i-1] = ms.getValue(solucoes.get(s));
				spacingValues[s][i-1] = spacing.getValue(solucoes.get(s));
			}
			
			cov.setParetoKnown(solucoes.get(idRef));
			for (int s = 0; s < cs1Values.length; s++){
				if (s < idRef){
					cs1Values[s][i-1] = cov.getValue(solucoes.get(s));	
				}else if (s >= idRef){
					cs1Values[s][i-1] = cov.getValue(solucoes.get(s+1));	
				}
			}
			
			for (int s = 0; s < cs2Values.length; s++){
				if (s < idRef){
					cov.setParetoKnown(solucoes.get(s));
				}else if (s >= idRef){
					cov.setParetoKnown(solucoes.get(s+1));
				}
				cs2Values[s][i-1] = cov.getValue(solucoes.get(idRef));
			}
		}
		buildResultsMOOMetrics("MODE", hvValues, msValues, spacingValues, cs1Values, cs2Values, idRef);
	}
	
	public static void main(String[] args) {
//		SolutionSet<Double> sol = new SolutionSet<Double>();
//		SolutionSet<Double> sol1 = new SolutionSet<Double>();
//		
//		for (int i = 1; i <= 10; i++) {
//			sol = new SolutionSet<Double>();
//			sol1 = new SolutionSet<Double>();
//			sol.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\pref1\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_003_0990_var.txt", problem);
//			readObjectivesFromFile(sol, "H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\pref1\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_003_0990_pf.txt");
//			for (Solution<Double> solution : sol.getSolutionsList()){
//				if (solution.getObjective(0) <= 0.1){
//					sol1.add(solution);
//				}
//			}
//			sol1.printVariablesToFile("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\pref1\\r" + nfi.format(i) + "\\_nsgaii1_C2_M3_50_100_003_0990_var.txt");
//			sol1.printObjectivesToFile("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\pref1\\r" + nfi.format(i) + "\\_nsgaii1_C2_M3_50_100_003_0990_pf.txt");
//		}
		
//		String expBase = "H:\\Mestrado\\Experimentos\\2objetivos\\";
//		String dirExp = expBase + "nsgaii\\c31\\r06\\";
//		File file = new File(dirExp);
//		
//		for (String strArq : file.list()){
//			File arquivoExp = new File(dirExp + strArq);
//			String novoNome = strArq;
//			novoNome = novoNome.replace(".", "<REM>");
//			novoNome = novoNome.replace(",", "<REM>");
//			novoNome = novoNome.replace("<REM>", "");
//			novoNome = novoNome.replace("txt", ".txt");
//			System.out.println(strArq);
//			System.out.println(novoNome);
//			arquivoExp.renameTo(new File(dirExp + novoNome));
//		}
//		main_spea2(args);
//		main_pesaii(args);
//		main_nsgaii(args);
//		main_nsgaii_pref(args);
//		main_nsgaii_cross(args);
//		main_paes(args);
//		main_mode(args);
//		main_nsgaii_sum(args);
//		main_nsgaii_bg(args);
		main_3obj(args);
	}
	
	public static void main_pesaii(String[] args) {
		List<SolutionSet<Double>> solucoes = new Vector<SolutionSet<Double>>();
		int qtdeExps = 5;
		int qteExec = 10;
		int idRef = 2;
		for (int i = 0; i < qtdeExps; i++){
			solucoes.add(new SolutionSet<Double>());	
		}
		
		double[][] hvValues = new double[qtdeExps][qteExec];
		double[][] msValues = new double[qtdeExps][qteExec];
		double[][] spacingValues = new double[qtdeExps][qteExec];
		double[][] cs1Values = new double[qtdeExps-1][qteExec];
		double[][] cs2Values = new double[qtdeExps-1][qteExec];
		
		for (int i = 1; i <= qteExec; i++) {
			solucoes.get(0).readIntVariablesFromFile("D:\\Temp\\results_cluster\\pesaii_" + i + "\\._pesaii_var_1000.txt", problem);
			readObjectivesFromFile(solucoes.get(0), "D:\\Temp\\results_cluster\\pesaii_" + i + "\\._pesaii_pf_1000.txt");
			solucoes.get(1).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\pesaii\\perfil2\\r" + nfi.format(i) + "\\_pesaii_2_C2_M3_50_100_006_1000_var.txt", problem);
			readObjectivesFromFile(solucoes.get(1), "H:\\Mestrado\\Experimentos\\2objetivos\\pesaii\\perfil2\\r" + nfi.format(i) + "\\_pesaii_2_C2_M3_50_100_006_1000_pf.txt");
			solucoes.get(2).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\pesaii\\perfil3\\r" + nfi.format(i) + "\\_pesaii_5_C2_M3_50_100_003_1000_var.txt", problem);
			readObjectivesFromFile(solucoes.get(2), "H:\\Mestrado\\Experimentos\\2objetivos\\pesaii\\perfil3\\r" + nfi.format(i) + "\\_pesaii_5_C2_M3_50_100_003_1000_pf.txt");
			solucoes.get(3).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\pesaii\\perfil4\\r" + nfi.format(i) + "\\_pesaii_5_C2_M3_50_100_001_1000_var.txt", problem);
			readObjectivesFromFile(solucoes.get(3), "H:\\Mestrado\\Experimentos\\2objetivos\\pesaii\\perfil4\\r" + nfi.format(i) + "\\_pesaii_5_C2_M3_50_100_001_1000_pf.txt");
			solucoes.get(4).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\pesaii\\perfil5\\r" + nfi.format(i) + "\\._pesaii_8_C2_M3_50_1.00_0.03_1000_var.txt", problem);
			readObjectivesFromFile(solucoes.get(4), "H:\\Mestrado\\Experimentos\\2objetivos\\pesaii\\perfil5\\r" + nfi.format(i) + "\\._pesaii_8_C2_M3_50_1.00_0.03_1000_pf.txt");
			for (int s = 0; s < hvValues.length; s++){
				hvValues[s][i-1] = hv.getValue(solucoes.get(s));
				msValues[s][i-1] = ms.getValue(solucoes.get(s));
				spacingValues[s][i-1] = spacing.getValue(solucoes.get(s));
			}
			
			cov.setParetoKnown(solucoes.get(idRef));
			for (int s = 0; s < cs1Values.length; s++){
				if (s < idRef){
					cs1Values[s][i-1] = cov.getValue(solucoes.get(s));	
				}else if (s >= idRef){
					cs1Values[s][i-1] = cov.getValue(solucoes.get(s+1));	
				}
			}
			
			for (int s = 0; s < cs2Values.length; s++){
				if (s < idRef){
					cov.setParetoKnown(solucoes.get(s));
				}else if (s >= idRef){
					cov.setParetoKnown(solucoes.get(s+1));
				}
				cs2Values[s][i-1] = cov.getValue(solucoes.get(idRef));
			}
		}
		buildResultsMOOMetrics("PESAII", hvValues, msValues, spacingValues, cs1Values, cs2Values, idRef);

	}
	
	public static void main_2o(String[] args) {
		List<SolutionSet<Double>> solucoes = new Vector<SolutionSet<Double>>();
		int qtdeExps = 5;
		int qteExec = 9;
		int idRef = 0;
		for (int i = 0; i < qtdeExps; i++){
			solucoes.add(new SolutionSet<Double>());	
		}
		
		double[][] hvValues = new double[qtdeExps][qteExec];
		double[][] msValues = new double[qtdeExps][qteExec];
		double[][] spacingValues = new double[qtdeExps][qteExec];
		double[][] cs1Values = new double[qtdeExps-1][qteExec];
		double[][] cs2Values = new double[qtdeExps-1][qteExec];
		
		for (int i = 1; i <= qteExec; i++) {
			solucoes.get(0).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil4\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_001_0990_var.txt", problem);
			readObjectivesFromFile(solucoes.get(0), "H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil4\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_001_0990_pf.txt");
			solucoes.get(1).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\spea2\\perfil4\\r" + nfi.format(i) + "\\_spea2_50_100_100_001_1000_var.txt", problem);
			readObjectivesFromFile(solucoes.get(1), "H:\\Mestrado\\Experimentos\\2objetivos\\spea2\\perfil4\\r" + nfi.format(i) + "\\_spea2_50_100_100_001_1000_pf.txt");
			solucoes.get(2).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\pesaii\\perfil3\\r" + nfi.format(i) + "\\_pesaii_5_C2_M3_50_100_003_1000_var.txt", problem);
			readObjectivesFromFile(solucoes.get(2), "H:\\Mestrado\\Experimentos\\2objetivos\\pesaii\\perfil3\\r" + nfi.format(i) + "\\_pesaii_5_C2_M3_50_100_003_1000_pf.txt");
			solucoes.get(3).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\paes\\perfil4\\r" + nfi.format(i) + "\\._paes_100_5_1,000_var.txt", problem);
			readObjectivesFromFile(solucoes.get(3), "H:\\Mestrado\\Experimentos\\2objetivos\\paes\\perfil4\\r" + nfi.format(i) + "\\._paes_100_5_1,000_pf.txt");
			solucoes.get(4).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\mode\\perfil4\\r0" + i + "\\._mode_100_0.30_1,000_var.txt", problem);
			readObjectivesFromFile(solucoes.get(4), "H:\\Mestrado\\Experimentos\\2objetivos\\mode\\perfil4\\r0" + i + "\\._mode_100_0.30_1,000_pf.txt");
			for (int s = 0; s < hvValues.length; s++){
				hvValues[s][i-1] = hv.getValue(solucoes.get(s));
				msValues[s][i-1] = ms.getValue(solucoes.get(s));
				spacingValues[s][i-1] = spacing.getValue(solucoes.get(s));
			}
			
			cov.setParetoKnown(solucoes.get(idRef));
			for (int s = 0; s < cs1Values.length; s++){
				if (s < idRef){
					cs1Values[s][i-1] = cov.getValue(solucoes.get(s));	
				}else if (s >= idRef){
					cs1Values[s][i-1] = cov.getValue(solucoes.get(s+1));	
				}
			}
			
			for (int s = 0; s < cs2Values.length; s++){
				if (s < idRef){
					cov.setParetoKnown(solucoes.get(s));
				}else if (s >= idRef){
					cov.setParetoKnown(solucoes.get(s+1));
				}
				cs2Values[s][i-1] = cov.getValue(solucoes.get(idRef));
			}
		}
		buildResultsMOOMetrics("Geral_2o", hvValues, msValues, spacingValues, cs1Values, cs2Values, idRef);

	}
	
	public static void mainTempo(String[] args) {
		List<SolutionSet<Double>> solucoes = new Vector<SolutionSet<Double>>();
		int qtdeExps = 5;
		int qteExec = 10;
		for (int i = 0; i < qtdeExps; i++){
			solucoes.add(new SolutionSet<Double>());	
		}
		
		double[][] hvTempo = new double[qtdeExps][qteExec];
		for (int i = 1; i <= qteExec; i++) {
			File fileFim = new File("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil3\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_003_0990_var.txt");
			File fileIni = new File("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil3\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_003_0010_var.txt");
			hvTempo[0][i-1] = fileFim.lastModified() - fileIni.lastModified();
			fileFim = new File("H:\\Mestrado\\Experimentos\\2objetivos\\spea2\\perfil4\\r" + nfi.format(i) + "\\_spea2_50_100_100_001_1000_var.txt");
			fileIni = new File("H:\\Mestrado\\Experimentos\\2objetivos\\spea2\\perfil4\\r" + nfi.format(i) + "\\_spea2_50_100_100_001_0010_var.txt");
			hvTempo[1][i-1] = fileFim.lastModified() - fileIni.lastModified();
			fileFim = new File("H:\\Mestrado\\Experimentos\\2objetivos\\pesaii\\perfil3\\r" + nfi.format(i) + "\\_pesaii_5_C2_M3_50_100_003_1000_var.txt");
			fileIni = new File("H:\\Mestrado\\Experimentos\\2objetivos\\pesaii\\perfil3\\r" + nfi.format(i) + "\\_pesaii_5_C2_M3_50_100_003_10_var.txt");
			hvTempo[2][i-1] = fileFim.lastModified() - fileIni.lastModified();
			fileFim = new File("H:\\Mestrado\\Experimentos\\2objetivos\\paes\\perfil4\\r" + nfi.format(i) + "\\._paes_100_5_1,000_var.txt");
			fileIni = new File("H:\\Mestrado\\Experimentos\\2objetivos\\paes\\perfil4\\r" + nfi.format(i) + "\\._paes_100_5_0,010_var.txt");
			hvTempo[3][i-1] = fileFim.lastModified() - fileIni.lastModified();
			fileFim = new File("H:\\Mestrado\\Experimentos\\2objetivos\\mode\\perfil4\\r0" + i + "\\._mode_100_0.30_1,000_var.txt");
			fileIni = new File("H:\\Mestrado\\Experimentos\\2objetivos\\mode\\perfil4\\r0" + i + "\\._mode_100_0.30_0,010_var.txt");
			hvTempo[4][i-1] = fileFim.lastModified() - fileIni.lastModified();
			
		}
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		System.out.println("NSGAII\tSPEA2\tPESAII\tPAES\tMODE");
		for (int i = 0; i < hvTempo[0].length; i++){
			for (int j = 0; j < hvTempo.length; j++){
				if (hvTempo[j][i]/(1000*60*60) < 40){
					System.out.print(nf.format(hvTempo[j][i]/(1000*60*60)) + "\t");	
				}else{
					System.out.print("\t");
				}
			}	
			System.out.println();
		}
		builldWilcoxonLatexTable("Teste de Wilcoxon para o tempo de execução dos algoritmos", "tempo_exec", "Tempo ", hvTempo, alfaDefault, false);
//		buildResultsMOOMetrics("Geral_2o", hvTempo, hvTempo, hvTempo, hvTempo, hvTempo, idRef);

	}
	
	public static void main_paes(String[] args) {
		List<SolutionSet<Double>> solucoes = new Vector<SolutionSet<Double>>();
		int qtdeExps = 4;
		int qteExec = 10;
		int idRef = 3;
		for (int i = 0; i < qtdeExps; i++){
			solucoes.add(new SolutionSet<Double>());	
		}
		
		double[][] hvValues = new double[qtdeExps][qteExec];
		double[][] msValues = new double[qtdeExps][qteExec];
		double[][] spacingValues = new double[qtdeExps][qteExec];
		double[][] cs1Values = new double[qtdeExps-1][qteExec];
		double[][] cs2Values = new double[qtdeExps-1][qteExec];
		
		for (int i = 1; i <= qteExec; i++) {
			solucoes.get(0).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\paes\\perfil1\\r" + nfi.format(i) + "\\_paes_50_2_1000_var.txt", problem);
			readObjectivesFromFile(solucoes.get(0), "H:\\Mestrado\\Experimentos\\2objetivos\\paes\\perfil1\\r" + nfi.format(i) + "\\_paes_50_2_1000_pf.txt");
			solucoes.get(1).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\paes\\perfil2\\r" + nfi.format(i) + "\\_paes_50_5_1000_var.txt", problem);
			readObjectivesFromFile(solucoes.get(1), "H:\\Mestrado\\Experimentos\\2objetivos\\paes\\perfil2\\r" + nfi.format(i) + "\\_paes_50_5_1000_pf.txt");
			solucoes.get(2).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\paes\\perfil3\\r" + nfi.format(i) + "\\._paes_50_8_1,000_var.txt", problem);
			readObjectivesFromFile(solucoes.get(2), "H:\\Mestrado\\Experimentos\\2objetivos\\paes\\perfil3\\r" + nfi.format(i) + "\\._paes_50_8_1,000_pf.txt");
			solucoes.get(3).readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\paes\\perfil4\\r" + nfi.format(i) + "\\._paes_100_5_1,000_var.txt", problem);
			readObjectivesFromFile(solucoes.get(3), "H:\\Mestrado\\Experimentos\\2objetivos\\paes\\perfil4\\r" + nfi.format(i) + "\\._paes_100_5_1,000_pf.txt");
			for (int s = 0; s < hvValues.length; s++){
				hvValues[s][i-1] = hv.getValue(solucoes.get(s));
				msValues[s][i-1] = ms.getValue(solucoes.get(s));
				spacingValues[s][i-1] = spacing.getValue(solucoes.get(s));
			}
			
			cov.setParetoKnown(solucoes.get(idRef));
			for (int s = 0; s < cs1Values.length; s++){
				if (s < idRef){
					cs1Values[s][i-1] = cov.getValue(solucoes.get(s));	
				}else if (s >= idRef){
					cs1Values[s][i-1] = cov.getValue(solucoes.get(s+1));	
				}
			}
			
			for (int s = 0; s < cs2Values.length; s++){
				if (s < idRef){
					cov.setParetoKnown(solucoes.get(s));
				}else if (s >= idRef){
					cov.setParetoKnown(solucoes.get(s+1));
				}
				cs2Values[s][i-1] = cov.getValue(solucoes.get(idRef));
			}
		}
		buildResultsMOOMetrics("PAES", hvValues, msValues, spacingValues, cs1Values, cs2Values, idRef);
	}

	/**
	 * TODO Descrição do método
	 *
	 * @param strHv
	 * @param strMS
	 * @param strSpacing
	 * @param strC1
	 * @param strC2
	 * @param idMelhor
	 */
	private static void showResultsConsole(String strHv, String strMS, String strSpacing, String strC1, String strC2,
			int idMelhor, String alg) {
		File file = new File(dirGrav + alg);
		if (!file.exists()){
			file.mkdirs();
		}
		String linha1Csv = alg + " 1, " + alg + " 2, " + alg + " 3, " + alg + " 4 " + "\n";
		try {
			FileOutputStream fis = new FileOutputStream(new File(file, "hypervolume.txt"));
			DataOutputStream dos = new DataOutputStream(fis);
			
			System.out.println("============================================================");
			System.out.println("					MÉTRICAS " + alg);
			System.out.println("============================================================");
			System.out.println("============================================================");
			System.out.println("					 HYPERVOLUME");
			System.out.println("============================================================");		
			System.out.println(strHv);
			dos.writeChars(linha1Csv);
			dos.writeChars(strHv.replaceAll("\t", ","));
			dos.close();
			fis.close();
			
			fis = new FileOutputStream(new File(file, "ms.csv"));
			dos = new DataOutputStream(fis);
			System.out.println("============================================================");
			System.out.println("					MAXIMUM SPREAD");
			System.out.println("============================================================");
			System.out.println(strMS);
			dos.writeChars(linha1Csv);
			dos.writeChars(strMS.replaceAll("\t", ","));
			dos.close();
			fis.close();
			
			fis = new FileOutputStream(new File(file, "spacing.csv"));
			dos = new DataOutputStream(fis);
			System.out.println("============================================================");
			System.out.println("					SPACING");
			System.out.println("============================================================");
			System.out.println(strSpacing);
			dos.writeChars(linha1Csv);
			dos.writeChars(strSpacing.replaceAll("\t", ","));
			dos.close();
			fis.close();
			
			fis = new FileOutputStream(new File(file, "cs1.csv"));
			dos = new DataOutputStream(fis);
			System.out.println("============================================================");
			System.out.println("					CS( " + alg + (idMelhor+1) + ", *)");
			System.out.println("============================================================");
			System.out.println(strC1);
			dos.writeChars(linha1Csv);
			dos.writeChars(strC1.replaceAll("\t", ","));
			dos.close();
			fis.close();
			
			fis = new FileOutputStream(new File(file, "cs2.csv"));
			dos = new DataOutputStream(fis);
			System.out.println("============================================================");
			System.out.println("					CS(*, " + alg + (idMelhor+1) + ")");
			System.out.println("============================================================");
			System.out.println(strC2);
			dos.writeChars(strC2.replaceAll("\t", ","));
			dos.close();
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void showResultsConsole(double[][] hv, double[][] ms, double[][] spacing, double[][] cs1, double[][] cs2,
			int idMelhor, String alg) {
		System.out.println();
		System.out.println("RESULTADOS PARA " + alg);
		System.out.println("HV");
		for (int i = 0; i < hv.length; i++){
			System.out.print("\t" + alg + " " + (i + 1));
		}
		System.out.println();
		for (int i = 0; i < hv[0].length; i++){
			System.out.print((i + 1) + "\t");
			for (int j = 0; j < hv.length; j++){
				System.out.print(nf.format(hv[j][i]) + "\t");
			}
			System.out.println();
		}
		System.out.println();
		System.out.println("MS");
		for (int i = 0; i < ms.length; i++){
			System.out.print("\t" + alg + " " + (i + 1));
		}
		System.out.println();
		for (int i = 0; i < ms[0].length; i++){
			System.out.print((i + 1) + "\t");
			for (int j = 0; j < ms.length; j++){
				System.out.print(nf.format(ms[j][i]) + "\t");
			}
			System.out.println();
		}
		System.out.println();
		
		System.out.println("SPACING");
		for (int i = 0; i < spacing.length; i++){
			System.out.print("\t" + alg + " " + (i + 1));
		}
		System.out.println();
		for (int i = 0; i < spacing[0].length; i++){
			System.out.print((i + 1) + "\t");
			for (int j = 0; j < spacing.length; j++){
				System.out.print(nf.format(spacing[j][i]) + "\t");
			}
			System.out.println();
		}
		System.out.println();
		
		System.out.println("CS1");
		for (int i = 0; i < cs1.length; i++){
			System.out.print("\t" + alg + " " + (i + 1));
		}
		System.out.println();
		for (int i = 0; i < cs1[0].length; i++){
			System.out.print((i + 1) + "\t");
			for (int j = 0; j < cs1.length; j++){
				System.out.print(nf.format(cs1[j][i]) + "\t");
			}
			System.out.println();
		}
		System.out.println();
		
		System.out.println("CS2");
		for (int i = 0; i < cs2.length; i++){
			System.out.print("\t" + alg + " " + (i + 1));
		}
		System.out.println();
		for (int i = 0; i < cs2[0].length; i++){
			System.out.print((i + 1) + "\t");
			for (int j = 0; j < cs2.length; j++){
				System.out.print(nf.format(cs2[j][i]) + "\t");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public static void main_3obj(String[] args) {
		List<SolutionSet<Double>> solucoes = new Vector<SolutionSet<Double>>();
		int qtdeExps = 5;
		int qteExec = 5;
		int idRef = 0;
		for (int i = 0; i < qtdeExps; i++){
			solucoes.add(new SolutionSet<Double>());	
		}
		
		double[][] hvValues = new double[qtdeExps][qteExec];
		double[][] msValues = new double[qtdeExps][qteExec];
		double[][] spacingValues = new double[qtdeExps][qteExec];
		double[][] cs1Values = new double[qtdeExps-1][qteExec];
		double[][] cs2Values = new double[qtdeExps-1][qteExec];
		
		for (int i = 1; i <= qteExec; i++) {
			solucoes.get(0).readIntVariablesFromFile("H:\\3obj\\nsgaii\\r" + nfi.format(i) + "\\._nsgaii_C2_M3_50_1.00_0.03_9,990_var.txt", problem3o);
			readObjectivesFromFile(solucoes.get(0), "H:\\3obj\\nsgaii\\r" + nfi.format(i) + "\\._nsgaii_C2_M3_50_1.00_0.03_9,990_pf.txt");
			solucoes.get(1).readIntVariablesFromFile("H:\\3obj\\spea2\\r" + nfi.format(i) + "\\._spea2_50_100_1.00_0.01_10,000_var.txt", problem3o);
			readObjectivesFromFile(solucoes.get(1), "H:\\3obj\\spea2\\r" + nfi.format(i) + "\\._spea2_50_100_1.00_0.01_10,000_pf.txt");
			solucoes.get(2).readIntVariablesFromFile("H:\\3obj\\pesaii\\r" + nfi.format(i) + "\\._pesaii_5_C2_M3_50_1.00_0.03_10000_var.txt", problem3o);
			readObjectivesFromFile(solucoes.get(2), "H:\\3obj\\pesaii\\r" + nfi.format(i) + "\\._pesaii_5_C2_M3_50_1.00_0.03_10000_pf.txt");
			solucoes.get(3).readIntVariablesFromFile("H:\\3obj\\paes\\r" + nfi.format(i) + "\\._paes_50_5_0,010_var.txt", problem3o);
			readObjectivesFromFile(solucoes.get(3), "H:\\3obj\\paes\\r" + nfi.format(i) + "\\._paes_50_5_0,010_pf.txt");
			solucoes.get(4).readIntVariablesFromFile("H:\\3obj\\mode\\r0" + i + "\\._mode_50_0.30_10,000_var.txt", problem3o);
			readObjectivesFromFile(solucoes.get(4), "H:\\3obj\\mode\\r0" + i + "\\._mode_50_0.30_10,000_pf.txt");
			for (int s = 0; s < hvValues.length; s++){
				hvValues[s][i-1] = hv.getValue(solucoes.get(s));
				msValues[s][i-1] = ms.getValue(solucoes.get(s));
				spacingValues[s][i-1] = spacing.getValue(solucoes.get(s));
			}
			
			cov.setParetoKnown(solucoes.get(idRef));
			for (int s = 0; s < cs1Values.length; s++){
				if (s < idRef){
					cs1Values[s][i-1] = cov.getValue(solucoes.get(s));	
				}else if (s >= idRef){
					cs1Values[s][i-1] = cov.getValue(solucoes.get(s+1));	
				}
			}
			
			for (int s = 0; s < cs2Values.length; s++){
				if (s < idRef){
					cov.setParetoKnown(solucoes.get(s));
				}else if (s >= idRef){
					cov.setParetoKnown(solucoes.get(s+1));
				}
				cs2Values[s][i-1] = cov.getValue(solucoes.get(idRef));
			}
		}
		buildResultsMOOMetrics("Geral_2o", hvValues, msValues, spacingValues, cs1Values, cs2Values, idRef);
	}
	
	public static void readObjectivesFromFile(SolutionSet<Double> ss, String path) {
		try {
			FileInputStream fis = new FileInputStream(path);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			int j = 0;
			while (br.ready()) {
				String[] objectives = br.readLine().split(" ");
				for (int i = 0; i < objectives.length; i++)
					ss.getSolutionsList().get(j)
							.setObjective(i, Double.parseDouble(objectives[i].replaceAll(",", ".")));
				j++;
			}

			br.close();
		} catch (IOException e) {
			LOGGER.severe("Error acceding to the file");
			e.printStackTrace();
		}
	}

	public synchronized static void fixedFiles(String path, int n) {
		File file = new File(path);
		if (file.isDirectory()) {
			for (String filho : file.list()) {
				fixedFiles(path + File.separator + filho, n);
			}
		} else {
			SolutionSet<Double> ss = new SolutionSet<Double>();
			if (path.contains("pf")) {
				ss.readIntVariablesFromFile(path.replaceAll("pf", "var"), problem);
				readObjectivesFromFile(ss, path);
				ss.printObjectivesToFile(path);
			}
			if (!path.contains(n + File.separator + File.separator + "._")) {
				path = path.replaceAll(n + File.separator + File.separator + "_", n + File.separator + File.separator
						+ "._");
				file.renameTo(new File(path));
			}
		}
	}


	/**
	 * TODO Descrição do método
	 *
	 * @param solucoes
	 * @return
	 */
	private static String getStrSpacing(List<SolutionSet<Double>> solucoes, int qtde) {
		String strIndicador = "";
		for (int i = 0; i < qtde; i++){
			strIndicador += nf.format(spacing.getValue(solucoes.get(i))) + "\t";
		}
		strIndicador += "\n";
		return strIndicador;
	}

	/**
	 * TODO Descrição do método
	 *
	 * @param solucoes
	 * @return
	 */
	private static String getStrMS(List<SolutionSet<Double>> solucoes, int qtde) {
		String strIndicador = "";
		for (int i = 0; i < qtde; i++){
			strIndicador += nf.format(ms.getValue(solucoes.get(i))) + "\t";
		}
		strIndicador += "\n";
		return strIndicador;
	}

	/**
	 * TODO Descrição do método
	 *
	 * @param solucoes
	 * @return
	 */
	private static String getStrHV(List<SolutionSet<Double>> solucoes, int qtde) {
		String strHv = "";
		for (int i = 0; i < qtde; i++){
			strHv += nf.format(hv.getValue(solucoes.get(i))) + "\t";
		}
		strHv += "\n";
		return strHv;
	}

	/**
	 * TODO Descrição do método
	 *
	 * @param idsMelhor
	 * @return
	 */
	private static int getIdCS(List<Integer> idsMelhor) {
		int idMelhor = 0;
		for (int m = 0; m < 4; m++){
			if (Collections.frequency(idsMelhor, m) > Collections.frequency(idsMelhor, idMelhor)){
				idMelhor = m;
			}
		}
		return idMelhor;
	}
}
