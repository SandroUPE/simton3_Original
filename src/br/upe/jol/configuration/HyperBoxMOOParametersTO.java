package br.upe.jol.configuration;

public class HyperBoxMOOParametersTO extends ArchiveMOOParametersTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Number of bi-divisions in the objective space
	 */
	private int bisections;

	public HyperBoxMOOParametersTO() {
		super();
		this.bisections = 9;
	}

	public HyperBoxMOOParametersTO(int populationSize, int archiveSize, int bisections,
			double crossoverProbility, double mutationProbability) {
		super(populationSize, archiveSize, crossoverProbility, mutationProbability);
		this.bisections = bisections;
	}

	public int getBisections() {
		return bisections;
	}

	public void setBisections(int bisections) {
		this.bisections = bisections;
	}	
	
}
