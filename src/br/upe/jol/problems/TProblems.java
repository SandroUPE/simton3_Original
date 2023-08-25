package br.upe.jol.problems;

public enum TProblems {
	PROJ_REDES_OPTICAS(" Projeto de Redes Ópticas"), 
	ZDT1(" ZDT1"), 
	ZDT2(" ZDT2"), 
	ZDT3(" ZDT3"), 
	ZDT4(" ZDT4"), 
	ZDT5(" ZDT5"), 
	ZDT6(" ZDT6"),
	DTLZ1(" DTLZ1"),
	DTLZ2(" DTLZ2"),
	DTLZ3(" DTLZ3"),
	DTLZ4(" DTLZ4"),
	DTLZ5(" DTLZ5"),
	DTLZ6(" DTLZ6"),
	DTLZ7(" DTLZ7"),
	DTLZ8(" DTLZ8"),
	DTLZ9(" DTLZ9"),
	MO_MST(" MO-MST");
	
	private TProblems(String name){
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}

	private String name;
}
