/*
 * *****************************************************************************
 * Projeto: JOL - Java Optimization Lab
 * Arquivo: ConfigurationTO.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	26/07/2010	Vers�o Inicial
 * ****************************************************************************
 */
package br.upe.jol.configuration;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import br.upe.jol.metaheuristics.TAlgorithm;
import br.upe.jol.problems.TProblems;

/**
 * @author Danilo Ara�jo
 *
 */
public class ConfigurationTO implements Serializable{
	private static final long serialVersionUID = 1L;

	private int nRuns;
	
	private List<TAlgorithm> selectedAlgorithms;
	
	private TProblems selectedProblem;
	
	private GeneralMOOParametersTO nsgaiiConfig;
	
	private ArchiveMOOParametersTO spea2Config;
	
	private HyperBoxMOOParametersTO pesaiiConfig;
	
	private HyperBoxMOOParametersTO paesConfig;
	
	private GeneralMOOParametersTO moPsoConfig;
	
	private GeneralMOOParametersTO moPsoCdrConfig;
	
	private GeneralMOOParametersTO cssMoPsoConfig;
	
	private GeneralMOOParametersTO dnPsoConfig;
	
	public ConfigurationTO(){
		nRuns = 30;
	
		selectedAlgorithms = new Vector<TAlgorithm>();
		selectedAlgorithms.add(TAlgorithm.values()[0]);
		
		selectedProblem = TProblems.values()[0];
		
		nsgaiiConfig = new GeneralMOOParametersTO();
		spea2Config = new ArchiveMOOParametersTO();
		pesaiiConfig = new HyperBoxMOOParametersTO();
		paesConfig = new HyperBoxMOOParametersTO();
		moPsoConfig = new GeneralMOOParametersTO();
		moPsoCdrConfig = new GeneralMOOParametersTO();
		cssMoPsoConfig = new GeneralMOOParametersTO();
		dnPsoConfig = new GeneralMOOParametersTO();
	}
	
	public GeneralMOOParametersTO getParameters(TAlgorithm tAlgorithm){
		GeneralMOOParametersTO ret = null;
		switch (tAlgorithm) {
			case NSGAII:
				ret = this.nsgaiiConfig;
				break;
			case SPEA2:
				ret = this.spea2Config;
				break;
			case CSSMOPSO:
				ret = this.cssMoPsoConfig;
				break;
			case DNPSO:
				ret = this.dnPsoConfig;
				break;
			case MOPSO:
				ret = this.moPsoConfig;
				break;
			case MOPSOCDR:
				ret = this.moPsoCdrConfig;
				break;
			case PAES:
				ret = this.paesConfig;
				break;
			case PESAII:
				ret = this.pesaiiConfig;
				break;
			case MODEII:
				ret = this.spea2Config;
				break;
			default:
				ret = this.nsgaiiConfig;
				break;
		}
	
	return ret;
	}

	/**
	 * M�todo acessor para obter o valor do atributo nRuns.
	 * @return O valor de nRuns
	 */
	public int getnRuns() {
		return nRuns;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo nRuns.
	 * @param nRuns O novo valor de nRuns
	 */
	public void setnRuns(int nRuns) {
		this.nRuns = nRuns;
	}

	/**
	 * M�todo acessor para obter o valor do atributo selectedAlgorithms.
	 * @return O valor de selectedAlgorithms
	 */
	public List<TAlgorithm> getSelectedAlgorithms() {
		return selectedAlgorithms;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo selectedAlgorithms.
	 * @param selectedAlgorithms O novo valor de selectedAlgorithms
	 */
	public void setSelectedAlgorithms(List<TAlgorithm> selectedAlgorithms) {
		this.selectedAlgorithms = selectedAlgorithms;
	}

	/**
	 * M�todo acessor para obter o valor do atributo selectedProblem.
	 * @return O valor de selectedProblem
	 */
	public TProblems getSelectedProblem() {
		return selectedProblem;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo selectedProblem.
	 * @param selectedProblem O novo valor de selectedProblem
	 */
	public void setSelectedProblem(TProblems selectedProblem) {
		this.selectedProblem = selectedProblem;
	}

	/**
	 * M�todo acessor para obter o valor do atributo nsgaiiConfig.
	 * @return O valor de nsgaiiConfig
	 */
	public GeneralMOOParametersTO getNSGAIIConfig() {
		return nsgaiiConfig;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo nsgaiiConfig.
	 * @param nsgaiiConfig O novo valor de nsgaiiConfig
	 */
	public void setNsgaiiConfig(GeneralMOOParametersTO nsgaiiConfig) {
		this.nsgaiiConfig = nsgaiiConfig;
	}

	/**
	 * M�todo acessor para obter o valor do atributo spea2Config.
	 * @return O valor de spea2Config
	 */
	public GeneralMOOParametersTO getSpea2Config() {
		return spea2Config;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo spea2Config.
	 * @param spea2Config O novo valor de spea2Config
	 */
	public void setSpea2Config(ArchiveMOOParametersTO spea2Config) {
		this.spea2Config = spea2Config;
	}

	/**
	 * M�todo acessor para obter o valor do atributo pesaiiConfig.
	 * @return O valor de pesaiiConfig
	 */
	public GeneralMOOParametersTO getPESAIIConfig() {
		return pesaiiConfig;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo pesaiiConfig.
	 * @param pesaiiConfig O novo valor de pesaiiConfig
	 */
	public void setPesaiiConfig(HyperBoxMOOParametersTO pesaiiConfig) {
		this.pesaiiConfig = pesaiiConfig;
	}

	/**
	 * M�todo acessor para obter o valor do atributo paesiiConfig.
	 * @return O valor de paesiiConfig
	 */
	public GeneralMOOParametersTO getPaesiiConfig() {
		return paesConfig;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo paesiiConfig.
	 * @param paesiiConfig O novo valor de paesiiConfig
	 */
	public void setPaesiiConfig(HyperBoxMOOParametersTO paesiiConfig) {
		this.paesConfig = paesiiConfig;
	}

	/**
	 * M�todo acessor para obter o valor do atributo moPsoConfig.
	 * @return O valor de moPsoConfig
	 */
	public GeneralMOOParametersTO getMoPsoConfig() {
		return moPsoConfig;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo moPsoConfig.
	 * @param moPsoConfig O novo valor de moPsoConfig
	 */
	public void setMoPsoConfig(GeneralMOOParametersTO moPsoConfig) {
		this.moPsoConfig = moPsoConfig;
	}

	/**
	 * M�todo acessor para obter o valor do atributo moPsoCdrConfig.
	 * @return O valor de moPsoCdrConfig
	 */
	public GeneralMOOParametersTO getMoPsoCdrConfig() {
		return moPsoCdrConfig;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo moPsoCdrConfig.
	 * @param moPsoCdrConfig O novo valor de moPsoCdrConfig
	 */
	public void setMoPsoCdrConfig(GeneralMOOParametersTO moPsoCdrConfig) {
		this.moPsoCdrConfig = moPsoCdrConfig;
	}

	/**
	 * M�todo acessor para obter o valor do atributo cssMoPsoConfig.
	 * @return O valor de cssMoPsoConfig
	 */
	public GeneralMOOParametersTO getCssMoPsoConfig() {
		return cssMoPsoConfig;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo cssMoPsoConfig.
	 * @param cssMoPsoConfig O novo valor de cssMoPsoConfig
	 */
	public void setCssMoPsoConfig(GeneralMOOParametersTO cssMoPsoConfig) {
		this.cssMoPsoConfig = cssMoPsoConfig;
	}

	/**
	 * M�todo acessor para obter o valor do atributo dnPsoConfig.
	 * @return O valor de dnPsoConfig
	 */
	public GeneralMOOParametersTO getDnPsoConfig() {
		return dnPsoConfig;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo dnPsoConfig.
	 * @param dnPsoConfig O novo valor de dnPsoConfig
	 */
	public void setDnPsoConfig(GeneralMOOParametersTO dnPsoConfig) {
		this.dnPsoConfig = dnPsoConfig;
	}
	
	
}
