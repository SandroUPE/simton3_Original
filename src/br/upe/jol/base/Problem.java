package br.upe.jol.base;

import java.io.Serializable;

/**
 * @author Danilo
 * 
 * @param <T>
 */
public abstract class Problem<T> implements Serializable {
	private static final long serialVersionUID = 1L;

	private final static int DEFAULT_PRECISSION = 16;

	protected int numberOfVariables;

	protected int numberOfObjectives;

	protected int numberOfConstraints;

	protected String problemName;

	protected double[] lowerLimit;

	protected double[] upperLimit;

	protected double[] lowerLimitObjective;

	protected double[] upperLimitObjective;

	protected int[] precision;

	protected int[] length;

	// CRUCIAL FOR VARIATION OF THE TOTAL DESIGN
	protected int[] staticVariables;

	// CRUCIAL FOR VARIATION OF THE TOTAL DESIGN
	protected boolean[] permission;

	public boolean[] getPermission() {
		return permission;
	}

	public void setPermission(boolean[] permission) {
		this.permission = permission;
	}

	public boolean[] getLogScale() {
		boolean[] defaultValue = new boolean[numberOfObjectives];

		return defaultValue;
	}

	/**
	 * @return Valor diferencial para cálculo do hypervolume, quando uma região
	 *         é conhecida por ser inalcançável
	 */
	public double getHypervolumeDif() {
		return 0;
	}

	public Problem(int numberOfVariables, int numberOfObjectives, String problemName) {
		this.numberOfVariables = numberOfVariables;
		this.numberOfObjectives = numberOfObjectives;
		this.problemName = problemName;
		this.upperLimit = new double[this.numberOfVariables];
		this.lowerLimit = new double[this.numberOfVariables];
		this.upperLimitObjective = new double[this.numberOfObjectives];
		this.lowerLimitObjective = new double[this.numberOfObjectives];
		this.staticVariables = new int[numberOfVariables];
	}

	public int getNumberOfVariables() {
		return numberOfVariables;
	}

	public void setNumberOfVariables(int numberOfVariables) {
		this.numberOfVariables = numberOfVariables;
	}

	public int getNumberOfObjectives() {
		return numberOfObjectives;
	}

	public double getLowerLimit(int i) {
		return lowerLimit[i];
	}

	public double getUpperLimit(int i) {
		return upperLimit[i];
	}

	public void evaluate(Solution<T> solution) {
		evaluate(solution, true);
	}
	
	public void evaluate(Solution<T> solution, double iteration) {
	}

	public int getNumberOfConstraints() {
		return numberOfConstraints;
	}

	public abstract void evaluateConstraints(Solution<T> solution);

	public void evaluate(Solution<T> solution, boolean simulate) {

	}

	public int getPrecision(int var) {
		return precision[var];
	}

	public int[] getPrecision() {
		return precision;
	}

	public void setPrecision(int[] precision) {
		this.precision = precision;
	}

	public int getLength(int var) {
		if (length == null)
			return DEFAULT_PRECISSION;
		return length[var];
	}

	public String getName() {
		return problemName;
	}

	public int getNumberOfBits() {
		int result = 0;
		for (int var = 0; var < numberOfVariables; var++) {
			result += getLength(var);
		}
		return result;
	}

	/**
	 * Método acessor para obter o valor do atributo problemName.
	 * 
	 * @return O valor de problemName
	 */
	public String getProblemName() {
		return problemName;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo problemName.
	 * 
	 * @param problemName
	 *            O novo valor de problemName
	 */
	public void setProblemName(String problemName) {
		this.problemName = problemName;
	}

	/**
	 * Método acessor para obter o valor do atributo lowerLimit.
	 * 
	 * @return O valor de lowerLimit
	 */
	public double[] getLowerLimit() {
		return lowerLimit;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo lowerLimit.
	 * 
	 * @param lowerLimit
	 *            O novo valor de lowerLimit
	 */
	public void setLowerLimit(double[] lowerLimit) {
		this.lowerLimit = lowerLimit;
	}

	/**
	 * Método acessor para obter o valor do atributo upperLimit.
	 * 
	 * @return O valor de upperLimit
	 */
	public double[] getUpperLimit() {
		return upperLimit;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo upperLimit.
	 * 
	 * @param upperLimit
	 *            O novo valor de upperLimit
	 */
	public void setUpperLimit(double[] upperLimit) {
		this.upperLimit = upperLimit;
	}

	/**
	 * Método acessor para obter o valor do atributo lowerLimitObjective.
	 * 
	 * @return O valor de lowerLimitObjective
	 */
	public double[] getLowerLimitObjective() {
		return lowerLimitObjective;
	}

	/**
	 * Método acessor para obter o valor do atributo upperLimitObjective.
	 * 
	 * @return O valor de upperLimitObjective
	 */
	public double[] getUpperLimitObjective() {
		return upperLimitObjective;
	}

	/**
	 * Método acessor para obter o valor do atributo length.
	 * 
	 * @return O valor de length
	 */
	public int[] getLength() {
		return length;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo length.
	 * 
	 * @param length
	 *            O novo valor de length
	 */
	public void setLength(int[] length) {
		this.length = length;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo numberOfObjectives.
	 * 
	 * @param numberOfObjectives
	 *            O novo valor de numberOfObjectives
	 */
	public void setNumberOfObjectives(int numberOfObjectives) {
		this.numberOfObjectives = numberOfObjectives;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo numberOfConstraints.
	 * 
	 * @param numberOfConstraints
	 *            O novo valor de numberOfConstraints
	 */
	public void setNumberOfConstraints(int numberOfConstraints) {
		this.numberOfConstraints = numberOfConstraints;
	}

	public int[] getStaticVariables() {
		return staticVariables;
	}

	public void setStaticVariables(int[] staticVariables) {
		this.staticVariables = staticVariables;
	}

	public String drawTopologies(Solution<T> solution) {
		return null;
	}

}
