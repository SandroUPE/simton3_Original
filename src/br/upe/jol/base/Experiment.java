package br.upe.jol.base;

import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import br.upe.jol.metaheuristics.TAlgorithm;
import br.upe.jol.problems.TProblems;

/**
 * @author drba
 *
 */
public class Experiment implements Serializable{
	private static final long serialVersionUID = 1L;

	private TProblems problem;
	
	private TAlgorithm algorithm;
	
	private int nRuns;
	
	private List<Double> hypervolumeList;
	
	private List<Double> spacingList;
	
	private List<Double> maxSpreadList;
	
	private List<SolutionSet<Double>> runs = new ArrayList<SolutionSet<Double>>();
	
	private Date dtHrStart;
	
	private Date dtHrEnd;

	/**
	 * Método acessor para obter o valor do atributo problem.
	 * @return O valor de problem
	 */
	public TProblems getProblem() {
		return problem;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo problem.
	 * @param problem O novo valor de problem
	 */
	public void setProblem(TProblems problem) {
		this.problem = problem;
	}

	/**
	 * Método acessor para obter o valor do atributo algorithm.
	 * @return O valor de algorithm
	 */
	public TAlgorithm getAlgorithm() {
		return algorithm;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo algorithm.
	 * @param algorithm O novo valor de algorithm
	 */
	public void setAlgorithm(TAlgorithm algorithm) {
		this.algorithm = algorithm;
	}

	/**
	 * Método acessor para obter o valor do atributo nRuns.
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
	 * Método acessor para obter o valor do atributo runs.
	 * @return O valor de runs
	 */
	public List<SolutionSet<Double>> getRuns() {
		return runs;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo runs.
	 * @param runs O novo valor de runs
	 */
	public void setRuns(List<SolutionSet<Double>> runs) {
		this.runs = runs;
	}

	/**
	 * Método acessor para obter o valor do atributo dtHrStart.
	 * @return O valor de dtHrStart
	 */
	public Date getDtHrStart() {
		return dtHrStart;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo dtHrStart.
	 * @param dtHrStart O novo valor de dtHrStart
	 */
	public void setDtHrStart(Date dtHrStart) {
		this.dtHrStart = dtHrStart;
	}

	/**
	 * Método acessor para obter o valor do atributo dtHrEnd.
	 * @return O valor de dtHrEnd
	 */
	public Date getDtHrEnd() {
		return dtHrEnd;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo dtHrEnd.
	 * @param dtHrEnd O novo valor de dtHrEnd
	 */
	public void setDtHrEnd(Date dtHrEnd) {
		this.dtHrEnd = dtHrEnd;
	}

	/**
	 * Método acessor para obter o valor do atributo serialversionuid.
	 * @return O valor de serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * Método acessor para obter o valor do atributo hypervolumeList.
	 * @return O valor de hypervolumeList
	 */
	public List<Double> getHypervolumeList() {
		return hypervolumeList;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo hypervolumeList.
	 * @param hypervolumeList O novo valor de hypervolumeList
	 */
	public void setHypervolumeList(List<Double> hypervolumeList) {
		this.hypervolumeList = hypervolumeList;
	}

	/**
	 * Método acessor para obter o valor do atributo spacingList.
	 * @return O valor de spacingList
	 */
	public List<Double> getSpacingList() {
		return spacingList;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo spacingList.
	 * @param spacingList O novo valor de spacingList
	 */
	public void setSpacingList(List<Double> spacingList) {
		this.spacingList = spacingList;
	}

	/**
	 * Método acessor para obter o valor do atributo maxSpreadList.
	 * @return O valor de maxSpreadList
	 */
	public List<Double> getMaxSpreadList() {
		return maxSpreadList;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo maxSpreadList.
	 * @param maxSpreadList O novo valor de maxSpreadList
	 */
	public void setMaxSpreadList(List<Double> maxSpreadList) {
		this.maxSpreadList = maxSpreadList;
	}
}
