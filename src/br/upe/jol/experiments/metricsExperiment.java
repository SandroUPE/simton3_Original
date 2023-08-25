package br.upe.jol.experiments;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import br.upe.jol.base.Algorithm;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.base.StatisticsUtil;
import br.upe.jol.metaheuristics.AlgorithmFactory;
import br.upe.jol.metaheuristics.TAlgorithm;
import br.upe.jol.metrics.Coverage;
import br.upe.jol.metrics.Hypervolume;
import br.upe.jol.metrics.MaximumSpread;
import br.upe.jol.metrics.Spacing;
import br.upe.jol.problems.TProblems;


public class metricsExperiment {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TProblems[] problems = {TProblems.ZDT1, TProblems.ZDT2, TProblems.ZDT3, TProblems.ZDT4, TProblems.ZDT6, 
				TProblems.DTLZ1, TProblems.DTLZ2, TProblems.DTLZ4, 
				TProblems.DTLZ5, TProblems.DTLZ6, TProblems.DTLZ7};
		AlgorithmFactory factory = AlgorithmFactory.getInstance();
		
		int execucoes = 30;
		int n = 0;
		boolean getPareto = false;
		
		double[] hypervolumes = new double[execucoes];
		double[] spacings = new double[execucoes];
		double[] coverages = new double[execucoes];
		double[] maxSpreads = new double[execucoes];
		
		Spacing<Double> spacing = new Spacing<Double>();
		MaximumSpread<Double> ms = new MaximumSpread<Double>();
		SolutionSet<Double> paretoKnown = null;
		Hypervolume<Double> hyper = new Hypervolume<Double>();
		Coverage<Double> cov = new Coverage<Double>();
		
		StatisticsUtil sUtil = StatisticsUtil.getInstance();
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(args[0]);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		OutputStreamWriter osw = new OutputStreamWriter(fos);
		BufferedWriter bw = new BufferedWriter(osw);
		
		
		for(int i=0; i<problems.length; i++){
			Algorithm<Double> algorithm = factory.getAlgorithm(TAlgorithm.MODEII, problems[i]);
			
			System.out.println(problems[i].toString());
			n = execucoes;
			getPareto = false;
			while(n>0){
				SolutionSet<Double> solutions1 = algorithm.execute();
			
				//solutions1.printObjectivesToFile("./results/ZDT6_artigo.txt");
				if(!getPareto){
					try {
						String path = problems[i].toString().trim();
						if(path.contains("LZ")){
							path += ".2D";
						}
						paretoKnown = new SolutionSet<Double>("./paretoFronts/" + path + 
								".pf",algorithm.getProblem());
						cov.setParetoKnown(paretoKnown);
						getPareto = true;
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
				
				spacings[execucoes-n] = spacing.getValue(solutions1);
				hypervolumes[execucoes-n] = hyper.getValue(solutions1);
				maxSpreads[execucoes-n] = ms.getValue(solutions1);
				coverages[execucoes-n] = cov.getValue(solutions1);
				
				--n;
			}
			
			try {
				bw.write("*************** "+ problems[i].toString() + " **********************");
			
				bw.newLine();
				
				bw.write("** Teórico **");
				bw.newLine();
				bw.write("Spacing = " + spacing.getValue(paretoKnown));
				bw.newLine();
				bw.write("Maximum Spread Pareto = " + ms.getValue(paretoKnown));
				bw.newLine();
				bw.write("Hypervolume Pareto = " + hyper.getValue(paretoKnown));
				
				bw.newLine();
				bw.newLine();
				
				bw.write("** Resultado **");
				bw.newLine();
				bw.write("Spacing = " + sUtil.getMedian(spacings));
				bw.newLine();
				bw.write("Maximum Spread = " + sUtil.getMedian(maxSpreads));
				bw.newLine();
				bw.write("Coverage = " + sUtil.getMedian(coverages));
				bw.newLine();
				bw.write("Hypervolume = " + sUtil.getMedian(hypervolumes));
				bw.newLine();
				bw.newLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
