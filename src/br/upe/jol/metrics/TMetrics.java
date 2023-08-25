package br.upe.jol.metrics;

public enum TMetrics {
	HIPERVOLUME(" HYPERVOLUME"), 
	SPREAD(" MAX. SPREAD"), 
	SPACING(" SPACING"), 
	COVERAGE(" COVERAGE");
	
	private TMetrics(String name){
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}

	private String name;
}
