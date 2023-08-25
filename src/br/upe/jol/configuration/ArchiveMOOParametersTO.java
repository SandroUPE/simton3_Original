package br.upe.jol.configuration;

public class ArchiveMOOParametersTO extends GeneralMOOParametersTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Size of external archive
	 */
	private int archiveSize;
	
	
	public ArchiveMOOParametersTO() {
		super();
		this.archiveSize = 70;
	}

	public ArchiveMOOParametersTO(int populationSize, int archiveSize,
			double crossoverProbility, double mutationProbability) {
		super(populationSize, crossoverProbility, mutationProbability);
		this.archiveSize = archiveSize;
	}

	/**
	 * Método acessor para obter o valor do atributo archiveSize.
	 * @return O valor de archiveSize
	 */
	public int getArchiveSize() {
		return archiveSize;
	}

	
	/**
	 * Metodo acessor para alterar o valor do atributo archiveSize.
	 * @param populationSize O novo valor de archiveSize
	 */
	public void setArchiveSize(int archiveSize) {
		this.archiveSize = archiveSize;
	}	

}
