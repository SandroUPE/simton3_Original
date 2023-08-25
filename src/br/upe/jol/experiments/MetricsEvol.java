package br.upe.jol.experiments;

import static br.upe.jol.base.Util.LOGGER;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;

import br.upe.jol.base.Algorithm;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.metrics.Hypervolume;
import br.upe.jol.problems.simton.SimtonProblem;

public class MetricsEvol {
	private static SimtonProblem problem = new SimtonProblem(14, 2);
	private static final NumberFormat nfi = NumberFormat.getInstance();
	private static final NumberFormat nfi1 = NumberFormat.getInstance();
	
	static{
		nfi.setMaximumFractionDigits(0);
		nfi.setMinimumFractionDigits(0);
		nfi.setMinimumIntegerDigits(2);
		nfi1.setMaximumFractionDigits(0);
		nfi1.setMinimumFractionDigits(0);
		nfi1.setMinimumIntegerDigits(4);
		nfi1.setGroupingUsed(false);
	}

	public static void main_paes(String[] args) {
		Hypervolume<Double> hv = new Hypervolume<Double>();
		SimtonProblem problem = new SimtonProblem(14, 2);
		SolutionSet<Double> ssAlgo1 = new SolutionSet<Double>();
		SolutionSet<Double> ssAlgo2 = new SolutionSet<Double>();
		SolutionSet<Double> ssAlgo3 = new SolutionSet<Double>();
		SolutionSet<Double> ssAlgo4 = new SolutionSet<Double>();
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(5);
		nf.setMinimumFractionDigits(5);
		int qtde = 10;
		double sumHv[] = new double[5];
		for (int j = 10; j <= 1000; j++) {
			if (j % 10 != 0){
				continue;
			}
			sumHv = new double[5];
			for (int i = 1; i <= qtde; i++) {
				ssAlgo3.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\paes\\perfil1\\r" + nfi.format(i) + "\\_paes_50_2_" + nfi1.format(j) + "_var.txt", problem);
				readObjectivesFromFile(ssAlgo3, "H:\\Mestrado\\Experimentos\\2objetivos\\paes\\perfil1\\r" + nfi.format(i) + "\\_paes_50_2_" + nfi1.format(j) + "_pf.txt");
				sumHv[0] += hv.getValue(ssAlgo3);
				ssAlgo1.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\paes\\perfil2\\r" + nfi.format(i) + "\\_paes_50_5_" + nfi1.format(j) + "_var.txt", problem);
				readObjectivesFromFile(ssAlgo1, "H:\\Mestrado\\Experimentos\\2objetivos\\paes\\perfil2\\r" + nfi.format(i) + "\\_paes_50_5_" + nfi1.format(j) + "_pf.txt");
				sumHv[1] += hv.getValue(ssAlgo1);
				ssAlgo2.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\paes\\perfil3\\r" + nfi.format(i) + "\\._paes_50_8_" + Algorithm.itf.format(j).replace(".", ",") + "_var.txt", problem);
				readObjectivesFromFile(ssAlgo2, "H:\\Mestrado\\Experimentos\\2objetivos\\paes\\perfil3\\r" + nfi.format(i) + "\\._paes_50_8_" + Algorithm.itf.format(j).replace(".", ",") + "_pf.txt");
				sumHv[2] += hv.getValue(ssAlgo2);
				ssAlgo4.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\paes\\perfil4\\r" + nfi.format(i) + "\\._paes_100_5_" + Algorithm.itf.format(j).replace(".", ",") + "_var.txt", problem);
				readObjectivesFromFile(ssAlgo4, "H:\\Mestrado\\Experimentos\\2objetivos\\paes\\perfil4\\r" + nfi.format(i) + "\\._paes_100_5_" + Algorithm.itf.format(j).replace(".", ",") + "_pf.txt");
				sumHv[3] += hv.getValue(ssAlgo4);
			}
			System.out.print(j + "\t");
			System.out.print(nf.format(sumHv[0]/qtde) + "\t");
			System.out.print(nf.format(sumHv[1]/qtde) + "\t");
			System.out.print(nf.format(sumHv[2]/qtde) + "\t");
			System.out.print(nf.format(sumHv[3]/qtde) + "\t");
			System.out.print(nf.format(sumHv[4]/qtde) + "\t");
			System.out.println();
		}
	}
	
	public static void main_spea2(String[] args) {
		Hypervolume<Double> hv = new Hypervolume<Double>();
		SimtonProblem problem = new SimtonProblem(14, 2);
		SolutionSet<Double> ssSPEA2 = new SolutionSet<Double>();
		SolutionSet<Double> ssPESAII = new SolutionSet<Double>();
		SolutionSet<Double> ssPAES = new SolutionSet<Double>();
		SolutionSet<Double> ssNSGAII = new SolutionSet<Double>();
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(5);
		nf.setMinimumFractionDigits(5);
		int qtde = 10;
		double sumHv[] = new double[5];
		for (int j = 10; j <= 1000; j++) {
			if ((j) % (10) != 0){
				continue;
			}
			sumHv = new double[4];
			for (int i = 1; i <= qtde; i++) {
				
				ssPAES.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\spea2\\perfil1\\r" + nfi.format(i) + "\\._spea2_50_100_0.90_0.10_" + Algorithm.itf.format(j).replace(".", ",") + "_var.txt", problem);
				readObjectivesFromFile(ssPAES, "H:\\Mestrado\\Experimentos\\2objetivos\\spea2\\perfil1\\r" + nfi.format(i) + "\\._spea2_50_100_0.90_0.10_" + Algorithm.itf.format(j).replace(".", ",") + "_pf.txt");
				sumHv[0] += hv.getValue(ssPAES);
				ssSPEA2.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\spea2\\perfil2\\r" + nfi.format(i) + "\\._spea2_50_100_1.00_0.06_" + Algorithm.itf.format(j).replace(".", ",") + "_var.txt", problem);
				readObjectivesFromFile(ssSPEA2, "H:\\Mestrado\\Experimentos\\2objetivos\\spea2\\perfil2\\r" + nfi.format(i) + "\\._spea2_50_100_1.00_0.06_" + Algorithm.itf.format(j).replace(".", ",") + "_pf.txt");
				sumHv[1] += hv.getValue(ssSPEA2);
				ssPESAII.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\spea2\\perfil3\\r" + nfi.format(i) + "\\._spea2_50_100_1.00_0.03_" + Algorithm.itf.format(j).replace(".", ",") + "_var.txt", problem);
				readObjectivesFromFile(ssPESAII, "H:\\Mestrado\\Experimentos\\2objetivos\\spea2\\perfil3\\r" + nfi.format(i) + "\\._spea2_50_100_1.00_0.03_" + Algorithm.itf.format(j).replace(".", ",") + "_pf.txt");
				sumHv[2] += hv.getValue(ssPESAII);
				ssNSGAII.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\spea2\\perfil4\\r" + nfi.format(i) + "\\_spea2_50_100_100_001_" + nfi1.format(j) + "_var.txt", problem);
				readObjectivesFromFile(ssNSGAII, "H:\\Mestrado\\Experimentos\\2objetivos\\spea2\\perfil4\\r" + nfi.format(i) + "\\_spea2_50_100_100_001_" + nfi1.format(j) + "_pf.txt");
				sumHv[3] += hv.getValue(ssNSGAII);
			}
			System.out.print(j + "\t");
			System.out.print(nf.format(sumHv[0]/qtde) + "\t");
			System.out.print(nf.format(sumHv[1]/qtde) + "\t");
			System.out.print(nf.format(sumHv[2]/qtde) + "\t");
			System.out.print(nf.format(sumHv[3]/qtde) + "\t");
			System.out.println();
		}
	}
	
	public static void main_pesaii(String[] args) {
		Hypervolume<Double> hv = new Hypervolume<Double>();
		SimtonProblem problem = new SimtonProblem(14, 2);
		SolutionSet<Double> ssAlgo1 = new SolutionSet<Double>();
		SolutionSet<Double> ssAlgo2 = new SolutionSet<Double>();
		SolutionSet<Double> ssAlgo3 = new SolutionSet<Double>();
		SolutionSet<Double> ssAlgo4 = new SolutionSet<Double>();
		SolutionSet<Double> ssAlgo5 = new SolutionSet<Double>();
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(5);
		nf.setMinimumFractionDigits(5);
		int qtde = 10;
		double sumHv[] = new double[5];
		for (int j = 10; j <= 1000; j++) {
			if (j % 10 != 0){
				continue;
			}
			sumHv = new double[5];
			for (int i = 1; i <= qtde; i++) {
				
				ssAlgo3.readIntVariablesFromFile("D:\\Temp\\results_cluster\\pesaii_" + i + "\\._pesaii_var_" + j + ".txt", problem);
				readObjectivesFromFile(ssAlgo3, "D:\\Temp\\results_cluster\\pesaii_" + i + "\\._pesaii_pf_" + j + ".txt");
				sumHv[0] += hv.getValue(ssAlgo3);
				ssAlgo1.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\pesaii\\perfil2\\r" + nfi.format(i) + "\\_pesaii_2_C2_M3_50_100_006_" + j + "_var.txt", problem);
				readObjectivesFromFile(ssAlgo1, "H:\\Mestrado\\Experimentos\\2objetivos\\pesaii\\perfil2\\r" + nfi.format(i) + "\\_pesaii_2_C2_M3_50_100_006_" + j + "_pf.txt");
				sumHv[1] += hv.getValue(ssAlgo1);
				ssAlgo2.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\pesaii\\perfil3\\r" + nfi.format(i) + "\\_pesaii_5_C2_M3_50_100_003_" + j + "_var.txt", problem);
				readObjectivesFromFile(ssAlgo2, "H:\\Mestrado\\Experimentos\\2objetivos\\pesaii\\perfil3\\r" + nfi.format(i) + "\\_pesaii_5_C2_M3_50_100_003_" + j + "_pf.txt");
				sumHv[2] += hv.getValue(ssAlgo2);
				ssAlgo4.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\pesaii\\perfil4\\r" + nfi.format(i) + "\\_pesaii_5_C2_M3_50_100_001_" + j + "_var.txt", problem);
				readObjectivesFromFile(ssAlgo4, "H:\\Mestrado\\Experimentos\\2objetivos\\pesaii\\perfil4\\r" + nfi.format(i) + "\\_pesaii_5_C2_M3_50_100_001_" + j + "_pf.txt");
				sumHv[3] += hv.getValue(ssAlgo4);
				ssAlgo5.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\pesaii\\perfil5\\r" + nfi.format(i) + "\\._pesaii_8_C2_M3_50_1.00_0.03_" + j + "_var.txt", problem);
				readObjectivesFromFile(ssAlgo5, "H:\\Mestrado\\Experimentos\\2objetivos\\pesaii\\perfil5\\r" + nfi.format(i) + "\\._pesaii_8_C2_M3_50_1.00_0.03_" + j + "_pf.txt");
				sumHv[4] += hv.getValue(ssAlgo5);
			}
			System.out.print(j + "\t");
			System.out.print(nf.format(sumHv[0]/qtde) + "\t");
			System.out.print(nf.format(sumHv[1]/qtde) + "\t");
			System.out.print(nf.format(sumHv[2]/qtde) + "\t");
			System.out.print(nf.format(sumHv[3]/qtde) + "\t");
			System.out.print(nf.format(sumHv[4]/qtde) + "\t");
			System.out.println();
		}
	}
	
	public static void main_2o(String[] args) {
		Hypervolume<Double> hv = new Hypervolume<Double>();
		SimtonProblem problem = new SimtonProblem(14, 2);
		SolutionSet<Double> ssAlgo1 = new SolutionSet<Double>();
		SolutionSet<Double> ssAlgo2 = new SolutionSet<Double>();
		SolutionSet<Double> ssAlgo3 = new SolutionSet<Double>();
		SolutionSet<Double> ssAlgo4 = new SolutionSet<Double>();
		SolutionSet<Double> ssAlgo5 = new SolutionSet<Double>();
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(5);
		nf.setMinimumFractionDigits(5);
		int qtde = 9;
		double sumHv[] = new double[5];
		for (int j = 10; j <= 1000; j++) {
			if (j % 10 != 0){
				continue;
			}
			sumHv = new double[5];
			for (int i = 1; i <= qtde; i++) {
				ssAlgo3.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil4\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_001_" + nfi1.format(j) + "_var.txt", problem);
				readObjectivesFromFile(ssAlgo3, "H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil4\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_001_" + nfi1.format(j) + "_pf.txt");
				sumHv[0] += hv.getValue(ssAlgo3);
				ssAlgo1.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\spea2\\perfil4\\r" + nfi.format(i) + "\\_spea2_50_100_100_001_" + nfi1.format(j) + "_var.txt", problem);
				readObjectivesFromFile(ssAlgo1, "H:\\Mestrado\\Experimentos\\2objetivos\\spea2\\perfil4\\r" + nfi.format(i) + "\\_spea2_50_100_100_001_" + nfi1.format(j) + "_pf.txt");
				sumHv[1] += hv.getValue(ssAlgo1);
				ssAlgo2.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\pesaii\\perfil3\\r" + nfi.format(i) + "\\_pesaii_5_C2_M3_50_100_003_" + j + "_var.txt", problem);
				readObjectivesFromFile(ssAlgo2, "H:\\Mestrado\\Experimentos\\2objetivos\\pesaii\\perfil3\\r" + nfi.format(i) + "\\_pesaii_5_C2_M3_50_100_003_" + j + "_pf.txt");
				sumHv[2] += hv.getValue(ssAlgo2);
				ssAlgo4.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\paes\\perfil4\\r" + nfi.format(i) + "\\._paes_100_5_" + Algorithm.itf.format(j).replace(".", ",") + "_var.txt", problem);
				readObjectivesFromFile(ssAlgo4, "H:\\Mestrado\\Experimentos\\2objetivos\\paes\\perfil4\\r" + nfi.format(i) + "\\._paes_100_5_" + Algorithm.itf.format(j).replace(".", ",") + "_pf.txt");
				sumHv[3] += hv.getValue(ssAlgo4);
				ssAlgo5.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\mode\\perfil4\\r0" + i + "\\._mode_100_0.30_" + Algorithm.itf.format(j).replace(".", ",") + "_var.txt", problem);
				readObjectivesFromFile(ssAlgo5, "H:\\Mestrado\\Experimentos\\2objetivos\\mode\\perfil4\\r0" + i + "\\._mode_100_0.30_" + Algorithm.itf.format(j).replace(".", ",") + "_pf.txt");
				sumHv[4] += hv.getValue(ssAlgo5);
			}
			System.out.print(j + "\t");
			System.out.print(nf.format(sumHv[0]/qtde) + "\t");
			System.out.print(nf.format(sumHv[1]/qtde) + "\t");
			System.out.print(nf.format(sumHv[2]/qtde) + "\t");
			System.out.print(nf.format(sumHv[3]/qtde) + "\t");
			System.out.print(nf.format(sumHv[4]/qtde) + "\t");
			System.out.println();
		}
	}
	
	public static void main_mode(String[] args) {
		Hypervolume<Double> hv = new Hypervolume<Double>();
		SimtonProblem problem = new SimtonProblem(14, 2);
		SolutionSet<Double> ssAlgo1 = new SolutionSet<Double>();
		SolutionSet<Double> ssAlgo2 = new SolutionSet<Double>();
		SolutionSet<Double> ssAlgo3 = new SolutionSet<Double>();
		SolutionSet<Double> ssAlgo4 = new SolutionSet<Double>();
		SolutionSet<Double> ssAlgo5 = new SolutionSet<Double>();
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(5);
		nf.setMinimumFractionDigits(5);
		int qtde = 10;
		double sumHv[] = new double[5];
		for (int j = 10; j <= 1000; j++) {
			if (j % 10 != 0){
				continue;
			}
			sumHv = new double[5];
			for (int i = 1; i <= qtde; i++) {
				
				ssAlgo3.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\mode\\perfil1\\r" + nfi.format(i) + "\\._mode_50_0.90_" + Algorithm.itf.format(j).replace(".", ",") + "_var.txt", problem);
				readObjectivesFromFile(ssAlgo3, "H:\\Mestrado\\Experimentos\\2objetivos\\mode\\perfil1\\r" + nfi.format(i) + "\\._mode_50_0.90_" + Algorithm.itf.format(j).replace(".", ",") + "_pf.txt");
				sumHv[0] += hv.getValue(ssAlgo3);
				ssAlgo1.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\mode\\perfil2\\r" + nfi.format(i) + "\\._mode_50_0.60_" + Algorithm.itf.format(j).replace(".", ",") + "_var.txt", problem);
				readObjectivesFromFile(ssAlgo1, "H:\\Mestrado\\Experimentos\\2objetivos\\mode\\perfil2\\r" + nfi.format(i) + "\\._mode_50_0.60_" + Algorithm.itf.format(j).replace(".", ",") + "_pf.txt");
				sumHv[1] += hv.getValue(ssAlgo1);
				ssAlgo2.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\mode\\perfil3\\r" + nfi.format(i) + "\\._mode_50_0.30_" + Algorithm.itf.format(j).replace(".", ",") + "_var.txt", problem);
				readObjectivesFromFile(ssAlgo2, "H:\\Mestrado\\Experimentos\\2objetivos\\mode\\perfil3\\r" + nfi.format(i) + "\\._mode_50_0.30_" + Algorithm.itf.format(j).replace(".", ",") + "_pf.txt");
				sumHv[2] += hv.getValue(ssAlgo2);
				ssAlgo4.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\mode\\perfil4\\r" + nfi.format(i) + "\\._mode_100_0.30_" + Algorithm.itf.format(j).replace(".", ",") + "_var.txt", problem);
				readObjectivesFromFile(ssAlgo4, "H:\\Mestrado\\Experimentos\\2objetivos\\mode\\perfil4\\r" + nfi.format(i) + "\\._mode_100_0.30_" + Algorithm.itf.format(j).replace(".", ",") + "_pf.txt");
				sumHv[3] += hv.getValue(ssAlgo4);
				ssAlgo5.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\mode\\perfil5\\r" + nfi.format(i) + "\\._mode_50_0.30_" + Algorithm.itf.format(j).replace(".", ",") + "_var.txt", problem);
				readObjectivesFromFile(ssAlgo5, "H:\\Mestrado\\Experimentos\\2objetivos\\mode\\perfil5\\r" + nfi.format(i) + "\\._mode_50_0.30_" + Algorithm.itf.format(j).replace(".", ",") + "_pf.txt");
				sumHv[4] += hv.getValue(ssAlgo5);
			}
			System.out.print(j + "\t");
			System.out.print(nf.format(sumHv[0]/qtde) + "\t");
			System.out.print(nf.format(sumHv[1]/qtde) + "\t");
			System.out.print(nf.format(sumHv[2]/qtde) + "\t");
			System.out.print(nf.format(sumHv[3]/qtde) + "\t");
			System.out.print(nf.format(sumHv[4]/qtde) + "\t");
			System.out.println();
		}
	}
	
	public static void main_NSGAII(String[] args) {
		Hypervolume<Double> hv = new Hypervolume<Double>();
		SimtonProblem problem = new SimtonProblem(14, 2);
		SolutionSet<Double> ssSPEA2 = new SolutionSet<Double>();
		SolutionSet<Double> ssPESAII = new SolutionSet<Double>();
		SolutionSet<Double> ssPAES = new SolutionSet<Double>();
		SolutionSet<Double> ssNSGAII = new SolutionSet<Double>();
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(5);
		nf.setMinimumFractionDigits(5);
		int qtde = 10;
		NumberFormat format = NumberFormat.getInstance();
		format.setMinimumIntegerDigits(4);
		format.setGroupingUsed(false);
		double sumHv[] = new double[5];
		for (int j = 10; j <= 990; j++) {
			if ((j) % (10) != 0){
				continue;
			}
			sumHv = new double[4];
			for (int i = 1; i <= qtde; i++) {
				ssPAES.readIntVariablesFromFile("D:\\Temp\\results_cluster\\nsgaii_" + i + "\\._nsgaii_var_" + j + ".txt", problem);
				readObjectivesFromFile(ssPAES, "D:\\Temp\\results_cluster\\nsgaii_" + i + "\\._nsgaii_pf_" + j + ".txt");
				sumHv[0] += hv.getValue(ssPAES);
				ssSPEA2.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil2\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_006_" + nfi1.format(j) + "_var.txt", problem);
				readObjectivesFromFile(ssSPEA2, "H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil2\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_006_" + nfi1.format(j) + "_pf.txt");
				sumHv[1] += hv.getValue(ssSPEA2);
				ssPESAII.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil3\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_003_" + nfi1.format(j) + "_var.txt", problem);
				readObjectivesFromFile(ssPESAII, "H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil3\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_003_" + nfi1.format(j) + "_pf.txt");
				sumHv[2] += hv.getValue(ssPESAII);
				ssNSGAII.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil4\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_001_" + nfi1.format(j) + "_var.txt", problem);
				readObjectivesFromFile(ssNSGAII, "H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil4\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_001_" + nfi1.format(j) + "_pf.txt");
				sumHv[3] += hv.getValue(ssNSGAII);
			}
			System.out.print(j + "\t");
			System.out.print(nf.format(sumHv[0]/qtde) + "\t");
			System.out.print(nf.format(sumHv[1]/qtde) + "\t");
			System.out.print(nf.format(sumHv[2]/qtde) + "\t");
			System.out.print(nf.format(sumHv[3]/qtde) + "\t");
			System.out.println();
		}
	}
	
	public static void main_NSGAII_10m(String[] args) {
		Hypervolume<Double> hv = new Hypervolume<Double>();
		SimtonProblem problem = new SimtonProblem(14, 2);
		SolutionSet<Double> ssSPEA2 = new SolutionSet<Double>();
		SolutionSet<Double> ssPESAII = new SolutionSet<Double>();
		SolutionSet<Double> ssPAES = new SolutionSet<Double>();
		SolutionSet<Double> ssNSGAII = new SolutionSet<Double>();
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(5);
		nf.setMinimumFractionDigits(5);
		int qtde = 1;
		NumberFormat format = NumberFormat.getInstance();
		format.setMinimumIntegerDigits(4);
		format.setGroupingUsed(false);
		double sumHv[] = new double[5];
		for (int j = 4900; j <= 10000; j++) {
			if ((j) % (50) != 0){
				continue;
			}
			sumHv = new double[4];
			for (int i = 1; i <= 1; i++) {
				ssPAES.readIntVariablesFromFile("D:\\results_c2_m3_003_100\\nsgaii_14000\\._nsgaii_C2_M3_50_1.00_0.03_" + Algorithm.itf.format(j).replace(".", ",") + "_var.txt", problem);
				readObjectivesFromFile(ssPAES, "D:\\results_c2_m3_003_100\\nsgaii_14000\\._nsgaii_C2_M3_50_1.00_0.03_" + Algorithm.itf.format(j).replace(".", ",") + "_pf.txt");
				sumHv[0] += hv.getValue(ssPAES);
			}
			System.out.print(j + "\t");
			System.out.print(nf.format(sumHv[0]/qtde) + "\t");
//			System.out.print(nf.format(sumHv[1]/qtde) + "\t");
//			System.out.print(nf.format(sumHv[2]/qtde) + "\t");
//			System.out.print(nf.format(sumHv[3]/qtde) + "\t");
			System.out.println();
		}
	}
	
	public static void main_nsgaii_sum(String[] args) {
		Hypervolume<Double> hv = new Hypervolume<Double>();
		SimtonProblem problem = new SimtonProblem(14, 2);
		SolutionSet<Double> ssSPEA2 = new SolutionSet<Double>();
		SolutionSet<Double> ssPAES = new SolutionSet<Double>();
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(5);
		nf.setMinimumFractionDigits(5);
		int qtde = 6;
		NumberFormat format = NumberFormat.getInstance();
		format.setMinimumIntegerDigits(4);
		format.setGroupingUsed(false);
		double sumHv[] = new double[5];
		for (int j = 10; j <= 990; j++) {
			if ((j) % (10) != 0){
				continue;
			}
			sumHv = new double[4];
			for (int i = 1; i <= qtde; i++) {
				ssPAES.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\sum\\r" + nfi.format(i) + "\\._nsgaii_C1_M8_50_1.00_0.03_" + Algorithm.itf.format(j).replace(".", ",") + "_var.txt", problem);
				readObjectivesFromFile(ssPAES, "H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\sum\\r" + nfi.format(i) + "\\._nsgaii_C1_M8_50_1.00_0.03_" + Algorithm.itf.format(j).replace(".", ",") + "_pf.txt");
				sumHv[0] += hv.getValue(ssPAES);
				ssSPEA2.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil3\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_003_" + nfi1.format(j) + "_var.txt", problem);
				readObjectivesFromFile(ssSPEA2, "H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil3\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_003_" + nfi1.format(j) + "_pf.txt");
				sumHv[1] += hv.getValue(ssSPEA2);
			}
			System.out.print(j + "\t");
			System.out.print(nf.format(sumHv[0]/qtde) + "\t");
			System.out.print(nf.format(sumHv[1]/qtde) + "\t");
			System.out.println();
		}
	}
	
	public static void main(String[] args) {
		main_NSGAII_10m(args);
	}
	
	public static void main_nsgaii_cross(String[] args) {
		Hypervolume<Double> hv = new Hypervolume<Double>();
		SimtonProblem problem = new SimtonProblem(14, 2);
		SolutionSet<Double> ssSPEA2 = new SolutionSet<Double>();
		SolutionSet<Double> ssPESAII = new SolutionSet<Double>();
		SolutionSet<Double> ssPAES = new SolutionSet<Double>();
		SolutionSet<Double> ssNSGAII = new SolutionSet<Double>();
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(5);
		nf.setMinimumFractionDigits(5);
		int qtde = 6;
		NumberFormat format = NumberFormat.getInstance();
		format.setMinimumIntegerDigits(4);
		format.setGroupingUsed(false);
		double sumHv[] = new double[5];
		for (int j = 10; j <= 990; j++) {
			if ((j) % (10) != 0){
				continue;
			}
			sumHv = new double[4];
			for (int i = 1; i <= qtde; i++) {
				ssPAES.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\c45\\r" + nfi.format(i) + "\\._nsgaii_C1_M3_50_1.00_0.03_" + Algorithm.itf.format(j).replace(".", ",") + "_var.txt", problem);
				readObjectivesFromFile(ssPAES, "H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\c45\\r" + nfi.format(i) + "\\._nsgaii_C1_M3_50_1.00_0.03_" + Algorithm.itf.format(j).replace(".", ",") + "_pf.txt");
				sumHv[0] += hv.getValue(ssPAES);
				ssSPEA2.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil2\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_006_" + nfi1.format(j) + "_var.txt", problem);
				readObjectivesFromFile(ssSPEA2, "H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil2\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_006_" + nfi1.format(j) + "_pf.txt");
				sumHv[1] += hv.getValue(ssSPEA2);
				ssPESAII.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil3\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_003_" + nfi1.format(j) + "_var.txt", problem);
				readObjectivesFromFile(ssPESAII, "H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil3\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_003_" + nfi1.format(j) + "_pf.txt");
				sumHv[2] += hv.getValue(ssPESAII);
				ssNSGAII.readIntVariablesFromFile("H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil4\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_001_" + nfi1.format(j) + "_var.txt", problem);
				readObjectivesFromFile(ssNSGAII, "H:\\Mestrado\\Experimentos\\2objetivos\\nsgaii\\perfil4\\r" + nfi.format(i) + "\\_nsgaii_C2_M3_50_100_001_" + nfi1.format(j) + "_pf.txt");
				sumHv[3] += hv.getValue(ssNSGAII);
			}
			System.out.print(j + "\t");
			System.out.print(nf.format(sumHv[0]/qtde) + "\t");
			System.out.print(nf.format(sumHv[1]/qtde) + "\t");
			System.out.print(nf.format(sumHv[2]/qtde) + "\t");
			System.out.print(nf.format(sumHv[3]/qtde) + "\t");
			System.out.println();
		}
	}

	public static void readObjectivesFromFile(SolutionSet<Double> ss, String path) {
		try {
			FileInputStream fis = new FileInputStream(path);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			int j = 0;
			while (br.ready()) {
				String[] objectives = br.readLine().split(" ");
				for (int i = 0; i < 2; i++)
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
}
