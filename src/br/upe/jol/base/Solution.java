package br.upe.jol.base;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * @author Danilo
 * 
 * @param <T>
 */
public class Solution<T> implements Serializable, Comparable<Solution<T>>, Cloneable {
	private static final long serialVersionUID = 1L;

	protected Problem<T> problem;

	protected T[] variable;

	protected double[] objective;

	protected int numberOfObjectives;

	protected double fitness;

	protected int rank;

	protected int location;

	protected double overallConstraintViolation;

	protected int numberOfViolatedConstraints;

	protected double crowdingDistance;

	protected double diSpacing;

	protected double wDistance;

	protected double angle;

	protected double dAngle;

	private int id;

	private boolean accurateEvaluation;

	@Override
	public Solution<T> clone() {
		Solution<T> clone = new Solution<T>();
		clone.setProblem(this.problem);
		clone.setCrowdingDistance(this.crowdingDistance);
		clone.variable = this.variable.clone();
		clone.setFitness(this.fitness);
		clone.objective = this.objective.clone();
		clone.numberOfObjectives = this.numberOfObjectives;

		return clone;
	}

	public Solution() {
		problem = null;
		overallConstraintViolation = 0.0;
		numberOfViolatedConstraints = 0;
		variable = null;
		objective = null;
	}

	public void setValue(int i, T value) {
		variable[i] = value;
		// this.evaluateObjectives();
	}

	public Problem<T> getProblem() {
		return problem;
	}

	public void setProblem(Problem<T> problem) {
		this.problem = problem;
	}

	public Solution(int numberOfObjectives) {
		this.numberOfObjectives = numberOfObjectives;
		objective = new double[numberOfObjectives];
	}

	public Solution(Problem<T> problem) throws ClassNotFoundException {
		this.problem = problem;
		numberOfObjectives = problem.getNumberOfObjectives();
		objective = new double[numberOfObjectives];

		fitness = 0.0;
		crowdingDistance = 0.0;

		variable = null;
	}

	public Solution<T> getNewSolution(Problem<T> problem) throws ClassNotFoundException {
		return new Solution<T>(problem);
	}

	public Solution(Problem<T> problem, T[] variables) {
		this.problem = problem;
		numberOfObjectives = problem.getNumberOfObjectives();
		objective = new double[numberOfObjectives];

		fitness = 0.0;
		crowdingDistance = 0.0;

		this.variable = variables.clone();

		// this.evaluateObjectives();
	}

	public void evaluateObjectives() {
		this.problem.evaluate(this);
	}

	public Solution(Solution<T> solution) {
		problem = solution.problem;

		numberOfObjectives = solution.numberOfObjectives();
		objective = new double[numberOfObjectives];
		for (int i = 0; i < objective.length; i++) {
			objective[i] = solution.getObjective(i);
		}

		variable = solution.getDecisionVariables();
		overallConstraintViolation = solution.getOverallConstraintViolation();
		numberOfViolatedConstraints = solution.getNumberOfViolatedConstraint();
		crowdingDistance = solution.getCrowdingDistance();
		fitness = solution.getFitness();
	}

	public void setCrowdingDistance(double distance) {
		crowdingDistance = distance;
	}

	public double getCrowdingDistance() {
		return crowdingDistance;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	public double getFitness() {
		return fitness;
	}

	public void setObjective(int i, double value) {
		objective[i] = value;
	}

	public double getObjective(int i) {
		return objective[i];
	}

	public double[] getAllObjectives() {
		return objective;
	}

	public int numberOfObjectives() {
		if (objective == null)
			return 0;
		else
			return numberOfObjectives;
	}

	public int numberOfVariables() {
		return problem.getNumberOfVariables();
	}

	public String toString() {
		String aux = "";
		DecimalFormat casas = new DecimalFormat("0.00000");
		for (int i = 0; i < this.numberOfObjectives; i++)
			aux = aux + casas.format(this.getObjective(i)) + " ";

		return aux;
	}

	public T[] getDecisionVariables() {
		return variable;
	}

	public void setDecisionVariables(T[] variables) {
		variable = variables;
	}

	public void setOverallConstraintViolation(double value) {
		this.overallConstraintViolation = value;
	}

	public double getOverallConstraintViolation() {
		return this.overallConstraintViolation;
	}

	public void setNumberOfViolatedConstraint(int value) {
		this.numberOfViolatedConstraints = value;
	}

	public int getNumberOfViolatedConstraint() {
		return this.numberOfViolatedConstraints;
	}

	public double getAggregativeValue() {
		double value = 0.0;
		for (int i = 0; i < numberOfObjectives(); i++) {
			value += getObjective(i);
		}
		return value;
	}

	private static final double STEP = 0.0005;

	public boolean dominates1(Solution<T> solution) {
		boolean condition1 = true;
		System.out.println(this.getOverallConstraintViolation());
		System.out.println(solution.getOverallConstraintViolation());
		if (this.getOverallConstraintViolation() != solution.getOverallConstraintViolation()
				&& (this.getOverallConstraintViolation() < 0) || (solution.getOverallConstraintViolation() < 0)) {
			return getOverallConstraintViolation() > solution.getOverallConstraintViolation();
		}

		for (int i = 0; i < this.numberOfObjectives(); i++) {
			if (this.objective[i] > solution.getObjective(i) + problem.getUpperLimitObjective()[i] * STEP) {
				condition1 = false;
				break;
			}
		}

		if (condition1) {
			for (int i = 0; i < this.numberOfObjectives(); i++) {
				if (this.objective[i] + problem.getUpperLimitObjective()[i] * STEP < solution.getObjective(i))
					return true;
			}
		}

		return false;
	}

	public boolean dominates(Solution<T> solution) {
		boolean condition1 = true;
		if (this.getOverallConstraintViolation() != solution.getOverallConstraintViolation()) {
			return getOverallConstraintViolation() > solution.getOverallConstraintViolation();
		}

		for (int i = 0; i < this.numberOfObjectives(); i++) {
			if (this.objective[i] > solution.getObjective(i)) {
				condition1 = false;
				break;
			}
		}

		if (condition1) {
			for (int i = 0; i < this.numberOfObjectives(); i++) {
				if (this.objective[i] != solution.getObjective(i))
					return true;
			}
		}

		return false;
	}

	private double distEuclideana(Solution<T> solution) {
		double soma = 0;
		for (int i = 0; i < this.numberOfObjectives(); i++) {
			soma += (this.getObjective(i) - solution.getObjective(i))
					* (this.getObjective(i) - solution.getObjective(i));
		}

		return Math.sqrt(soma);
	}

	public double[] distanceOrdered(SolutionSet<T> solutionSet) {
		double[] distancias = new double[solutionSet.size() - 1];
		boolean isSame = false;

		for (int j = 0; j < solutionSet.size(); j++) {
			Solution<T> solution = solutionSet.get(j);
			if (!this.equals(solution) && !isSame) {
				distancias[j] = distEuclideana(solution);
			} else if (isSame) {
				distancias[j - 1] = distEuclideana(solution);
			} else {
				isSame = true;
			}
		}

		// ordenar de forma crescente
		Arrays.sort(distancias);
		return distancias;
	}

	public double[] distanceOrderedDif(SolutionSet<T> solutionSet) {
		double[] distancias = new double[solutionSet.size() - 1];

		for (int j = 0; j < solutionSet.size() - 1; j++) {
			Solution<T> solution = solutionSet.get(j);
			distancias[j] = distEuclideana(solution);
		}

		// ordenar de forma crescente
		Arrays.sort(distancias);
		return distancias;
	}

	@Override
	public int compareTo(Solution<T> o) {
		return fitness > o.getFitness() ? -1 : 1;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	// Used in Spacing metric
	public double getDiSpacing() {
		return diSpacing;
	}

	public void evaluateDi(SolutionSet<T> solutionSet) {
		double minDi = Double.MAX_VALUE;
		double auxDi = 0;
		double lengthObjective = 1;
		
		boolean[] logScale = problem.getLogScale();

		if (problem.getUpperLimitObjective() != null) {
			for (Solution<T> s : solutionSet.solutionsList) {
				if (s.equals(this))
					continue;

				auxDi = 0;
				for (int i = 0; i < this.numberOfObjectives; i++) {
					if (logScale[i]) {
						lengthObjective = 5;
						auxDi += Math.abs(Math.log10(this.getObjective(i)) / lengthObjective - Math.log10(s.getObjective(i)) / lengthObjective);
					} else {
						lengthObjective = problem.getUpperLimitObjective()[i] - problem.getLowerLimitObjective()[i];
						auxDi += Math.abs(this.getObjective(i) / lengthObjective - s.getObjective(i) / lengthObjective);	
					}
				}

				if (auxDi < minDi) {
					minDi = auxDi;
				}
			}
		} else {
			for (Solution<T> s : solutionSet.solutionsList) {
				if (s.equals(this))
					continue;

				auxDi = 0;
				for (int i = 0; i < this.numberOfObjectives; i++) {
					if (logScale[i]) {
						auxDi += Math.abs(Math.log10(this.getObjective(i)) - Math.log10(s.getObjective(i)));
					} else {
						auxDi += Math.abs(this.getObjective(i) - s.getObjective(i));	
					}
				}

				if (auxDi < minDi) {
					minDi = auxDi;
				}
			}
		}

		this.setDiSpacing(minDi);
	}

	public void setDiSpacing(double di) {
		this.diSpacing = di;
	}

	// Finish Used in Spacing

	/**
	 * Mï¿½todo acessor para obter o valor de location
	 * 
	 * @return O valor de location
	 */
	public int getLocation() {
		return location;
	}

	/**
	 * Mï¿½todo acessor para setar o valor de location
	 * 
	 * @param location
	 *            O valor de location
	 */
	public void setLocation(int location) {
		this.location = location;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(variable);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Solution other = (Solution) obj;
		if (!Arrays.equals(variable, other.variable))
			return false;
		return true;
	}

	/**
	 * Método acessor para obter o valor do atributo wDistance.
	 * 
	 * @return Retorna o atributo wDistance.
	 */
	public double getwDistance() {
		return wDistance;
	}

	/**
	 * Método acessor para modificar o valor do atributo wDistance.
	 * 
	 * @param wDistance
	 *            O valor de wDistance para setar.
	 */
	public void setwDistance(double wDistance) {
		this.wDistance = wDistance;
	}

	/**
	 * Método acessor para obter o valor do atributo angle.
	 * 
	 * @return Retorna o atributo angle.
	 */
	public double getAngle() {
		return angle;
	}

	/**
	 * Método acessor para modificar o valor do atributo angle.
	 * 
	 * @param angle
	 *            O valor de angle para setar.
	 */
	public void setAngle(double angle) {
		this.angle = angle;
	}

	/**
	 * Método acessor para obter o valor do atributo dAngle.
	 * 
	 * @return Retorna o atributo dAngle.
	 */
	public double getdAngle() {
		return dAngle;
	}

	/**
	 * Método acessor para modificar o valor do atributo dAngle.
	 * 
	 * @param dAngle
	 *            O valor de dAngle para setar.
	 */
	public void setdAngle(double dAngle) {
		this.dAngle = dAngle;
	}

	/**
	 * @return o valor do atributo id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Altera o valor do atributo id
	 * 
	 * @param id
	 *            O valor para setar em id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return o valor do atributo accurateEvaluation
	 */
	public boolean isAccurateEvaluation() {
		return accurateEvaluation;
	}

	/**
	 * Altera o valor do atributo accurateEvaluation
	 * 
	 * @param accurateEvaluation
	 *            O valor para setar em accurateEvaluation
	 */
	public void setAccurateEvaluation(boolean accurateEvaluation) {
		this.accurateEvaluation = accurateEvaluation;
	}

}
