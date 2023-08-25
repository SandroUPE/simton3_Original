package br.upe.jol.base;

import br.upe.jol.metaheuristics.TAlgorithm;
import br.upe.jol.metrics.Hypervolume;
import br.upe.jol.metrics.MaximumSpread;
import br.upe.jol.metrics.Spacing;

public class Metricas<T> {
	
	private int nRuns;
	
	private Hypervolume<T> hyper;
	private double[] hyperVolumes;
	private int indexHyper;
	
	private Spacing<T> spacing;
	private double[] spacings;
	private int indexSpa;
	
	private MaximumSpread<T> maximum;
 	private double[] maximumSpreads;
	private int indexMS;
	
	private TAlgorithm tAlgorithm;
	
	public Metricas(int nRuns){
		this.nRuns = nRuns;
		this.hyperVolumes = new double[nRuns];
		this.spacings = new double[nRuns];
		this.maximumSpreads = new double[nRuns];
	}
	
	/**
	 * Método que calcula as métricas necessárias.
	 * @param solutionSet
	 */
	public void calcularMetricas(SolutionSet<T> solutionSet){
		this.calcularHyper(solutionSet);
		this.calcularMS(solutionSet);
		this.calcularSpacing(solutionSet);
	}

	private void calcularHyper(SolutionSet<T> solutionSet){
		if(hyper == null){
			hyper = new Hypervolume<T>();
			indexHyper = 0;
		}
		
		
		hyperVolumes[indexHyper++] = hyper.getValue(solutionSet);
	}
	
	private void calcularSpacing(SolutionSet<T> solutionSet){
		if(spacing == null){
			spacing = new Spacing<T>();
			indexSpa = 0;
		}
		
		spacings[indexSpa] = spacing.getValue(solutionSet);
		indexSpa++;
	}
	
	private void calcularMS(SolutionSet<T> solutionSet){
		if(maximum == null){
			maximum = new MaximumSpread<T>();
			indexMS = 0;
		}
		
		maximumSpreads[indexSpa] = maximum.getValue(solutionSet);
		indexMS++;
	}
	
	public double[] getHyperVolumes() {
		double[] aux = new double[indexHyper];
		for(int i=0; i<indexHyper; i++){
				aux[i] = hyperVolumes[i];
		}
		
		return aux;
	}

	public void setHyperVolumes(double[] hyperVolumes) {
		this.hyperVolumes = hyperVolumes;
	}

	public double[] getSpacings() {
		double[] aux = new double[indexSpa];
		for(int i=0; i<indexSpa; i++){
				aux[i] = spacings[i];
		}
		
		return aux;
	}

	public void setSpacings(double[] spacings) {
		this.spacings = spacings;
	}

	public double[] getMaximumSpreads() {
		double[] aux = new double[indexMS];
		for(int i=0; i<indexMS; i++){
				aux[i] = maximumSpreads[i];
		}
		
		return aux;
	}

	public void setMaximumSpreads(double[] maximumSpreads) {
		this.maximumSpreads = maximumSpreads;
	}

	public TAlgorithm gettAlgorithm() {
		return tAlgorithm;
	}

	public void settAlgorithm(TAlgorithm tAlgorithm) {
		this.tAlgorithm = tAlgorithm;
	}
	
}
