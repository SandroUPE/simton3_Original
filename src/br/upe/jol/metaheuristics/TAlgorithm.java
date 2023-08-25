package br.upe.jol.metaheuristics;

import java.io.File;

public enum TAlgorithm {
	NSGAII(" NSGA II	"), 
	SPEA2(" SPEA 2	"), 
	PAES(" PAES		"), 
	PESAII(" PESA II	"),
	MODEII(" MODE II	"), 
	MOPSO(" MOPSO	"), 
	CSSMOPSO(" CSS MOPSO	"), 
	MOPSOCDR(" MOPSO CDR	"), 
	DNPSO(" DNPSO	");
	
	public static void main(String[] args) {
		File file = new File("C:/doutorado/experimentos/nsfnet/isda/r02_novo");
		File arq = null;
		for (String strFile : file.list()) {
			arq = new File(file, strFile);
			if (arq.isFile()) {
				arq.renameTo(new File(file, strFile.replaceAll("._spea2", "2_spea2")));
			}
		}
	}
	
	private TAlgorithm(String name){
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}

	private String name;
}
