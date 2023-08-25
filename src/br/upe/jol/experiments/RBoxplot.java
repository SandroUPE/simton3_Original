/**
 * RBoxplot.java
 *
 * @author Antonio J. Nebro
 * @author Juan J. Durillo
 *
 * @version 1.1
 */

package br.upe.jol.experiments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class RBoxplot {
	/**
	 * This script produces R scripts for generating eps files containing
	 * boxplots of the results previosly obtained. The boxplots will be arranged
	 * in a grid of rows x cols. As the number of problems in the experiment can
	 * be too high, the @param problems includes a list of the problems to be
	 * plotted.
	 * 
	 * @param rows
	 * @param cols
	 * @param problems
	 *            List of problem to plot
	 * @param prefix
	 *            Prefix to be added to the names of the R scripts
	 * @throws java.io.FileNotFoundException
	 * @throws java.io.IOException
	 */
	public static void generateScripts(int rows, int cols, String[] problems, String prefix, boolean notch,
			Experiment experiment) throws FileNotFoundException, IOException {
		// STEP 1. Creating R output directory

		String rDirectory = "R";
		rDirectory = experiment.experimentBaseDirectory_ + "/" + rDirectory;
		System.out.println("R    : " + rDirectory);
		File rOutput;
		rOutput = new File(rDirectory);
		if (!rOutput.exists()) {
			new File(rDirectory).mkdirs();
			System.out.println("Creating " + rDirectory + " directory");
		}

		for (int indicator = 0; indicator < experiment.indicatorList_.length; indicator++) {
			System.out.println("Indicator: " + experiment.indicatorList_[indicator]);
			String rFile = rDirectory + "/" + prefix + "." + experiment.indicatorList_[indicator] + ".Boxplot.R";

			FileWriter os = new FileWriter(rFile, false);
			os.write("postscript(\"" + prefix + "." + experiment.indicatorList_[indicator]
					+ ".Boxplot.eps\", horizontal=FALSE, onefile=FALSE, height=8, width=12, pointsize=10)" + "\n");
			// os.write("resultDirectory<-\"../data/" + experimentName_ +"\"" +
			// "\n");
			os.write("resultDirectory<-\"../data/" + "\"" + "\n");
			os.write("qIndicator <- function(indicator, problem)" + "\n");
			os.write("{" + "\n");

			for (int i = 0; i < experiment.algorithmNameList_.length; i++) {
				os.write("file" + experiment.algorithmNameList_[i] + "<-paste(resultDirectory, \""
						+ experiment.algorithmNameList_[i] + "\", sep=\"/\")" + "\n");
				os.write("file" + experiment.algorithmNameList_[i] + "<-paste(file" + experiment.algorithmNameList_[i]
						+ ", " + "problem, sep=\"/\")" + "\n");
				os.write("file" + experiment.algorithmNameList_[i] + "<-paste(file" + experiment.algorithmNameList_[i]
						+ ", " + "indicator, sep=\"/\")" + "\n");
				os.write(experiment.algorithmNameList_[i] + "<-scan(" + "file" + experiment.algorithmNameList_[i] + ")"
						+ "\n");
				os.write("\n");
			} // for

			os.write("algs<-c(");
			for (int i = 0; i < experiment.algorithmNameList_.length - 1; i++) {
				os.write("\"" + experiment.algorithmNameList_[i] + "\",");
			} // for
			os.write("\"" + experiment.algorithmNameList_[experiment.algorithmNameList_.length - 1] + "\")" + "\n");

			os.write("boxplot(");
			for (int i = 0; i < experiment.algorithmNameList_.length; i++) {
				os.write(experiment.algorithmNameList_[i] + ",");
			} // for
			if (notch) {
				os.write("names=algs, notch = TRUE)" + "\n");
			} else {
				os.write("names=algs, notch = FALSE)" + "\n");
			}
			os.write("titulo <-paste(indicator, problem, sep=\":\")" + "\n");
			os.write("title(main=titulo)" + "\n");

			os.write("}" + "\n");

			os.write("par(mfrow=c(" + rows + "," + cols + "))" + "\n");

			os.write("indicator<-\"" + experiment.indicatorList_[indicator] + "\"" + "\n");

			for (int i = 0; i < problems.length; i++) {
				os.write("qIndicator(indicator, \"" + problems[i] + "\")" + "\n");
			}

			os.close();
		} // for
	} // generateRBoxplotScripts

	public static void main(String[] args) {
		String prefixo = "H:\\Mestrado\\Experimentos\\2objetivos\\spea2\\perfil";
		Experiment exp = new Experiment();
		exp.paretoFrontFile_ = new String[] { prefixo + "1\\._nsgaii_C2_M3_50_1.00_0.03_1,000_pf.txt",
				prefixo + "2\\._nsgaii_C2_M3_50_1,00_0,06_1.000_pf.txt",
				prefixo + "3\\._nsgaii_C2_M3_50_1.00_0.03_1,000_pf.txt",
				prefixo + "4\\._nsgaii_C2_M3_50_1,00_0,01_1.000_pf.txt" };
		exp.indicatorList_ = new String[] { "HYPERVOLUME", "MS", "SPACING" };
		exp.algorithmNameList_ = new String[] { "NSGAII 1", "NSGAII 2", "NSGAII 3", "NSGAII 4" };
		exp.latexDirectory_ = "C:\\Temp\\";
		exp.experimentBaseDirectory_ = "C:\\Temp\\";
		exp.indicatorMinimize_ = new HashMap<String, Boolean>();
		exp.indicatorMinimize_.put("HYPERVOLUME", true);
		exp.indicatorMinimize_.put("MS", true);
		exp.indicatorMinimize_.put("SPACING", false);
		try {
			RBoxplot.generateScripts(3, 4, new String[] { "Projeto Redes" }, "", true, exp);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}