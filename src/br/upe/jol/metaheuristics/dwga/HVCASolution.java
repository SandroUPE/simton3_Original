package br.upe.jol.metaheuristics.dwga;

import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;

public class HVCASolution extends Solution<Double> {

	private static final long serialVersionUID = 1L;
	
	private double hvContributionIndividual;
	
	private double hvContributionGroup;
	
	private double angleIndividual;
	
	private double varAngleGroup;
	
	public HVCASolution() {
		super();
	}

	public HVCASolution(int numberOfObjectives) {
		super(numberOfObjectives);
	}

	public HVCASolution(Problem<Double> problem, Double[] variables) {
		super(problem, variables);
	}

	public HVCASolution(Problem<Double> problem) throws ClassNotFoundException {
		super(problem);
	}

	public HVCASolution(Solution<Double> solution) {
		super(solution);
	}

	@Override
	public void evaluateObjectives() {
		super.evaluateObjectives();
		hvContributionIndividual = (problem.getUpperLimitObjective()[1] - objective[1]) * (problem.getUpperLimitObjective()[0] - objective[0]);
		angleIndividual = Math.atan((problem.getUpperLimitObjective()[1] - objective[1]) / (problem.getUpperLimitObjective()[0] - objective[0]));
	}
	/**
	 * Método acessor para obter o valor do atributo hvContributionIndividual.
	 *
	 * @return Retorna o atributo hvContributionIndividual.
	 */
	public double getHvContributionIndividual() {
		return hvContributionIndividual;
	}

	/**
	 * Método acessor para modificar o valor do atributo hvContributionIndividual.
	 *
	 * @param hvContributionIndividual O valor de hvContributionIndividual para setar.
	 */
	public void setHvContributionIndividual(double hvContributionIndividual) {
		this.hvContributionIndividual = hvContributionIndividual;
	}

	/**
	 * Método acessor para obter o valor do atributo hvContributionGroup.
	 *
	 * @return Retorna o atributo hvContributionGroup.
	 */
	public double getHvContributionGroup() {
		return hvContributionGroup;
	}

	/**
	 * Método acessor para modificar o valor do atributo hvContributionGroup.
	 *
	 * @param hvContributionGroup O valor de hvContributionGroup para setar.
	 */
	public void setHvContributionGroup(double hvContributionGroup) {
		this.hvContributionGroup = hvContributionGroup;
	}

	/**
	 * Método acessor para obter o valor do atributo serialversionuid.
	 *
	 * @return Retorna o atributo serialversionuid.
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * Método acessor para obter o valor do atributo angleIndividual.
	 *
	 * @return Retorna o atributo angleIndividual.
	 */
	public double getAngleIndividual() {
		return angleIndividual;
	}

	/**
	 * Método acessor para modificar o valor do atributo angleIndividual.
	 *
	 * @param angleIndividual O valor de angleIndividual para setar.
	 */
	public void setAngleIndividual(double angleIndividual) {
		this.angleIndividual = angleIndividual;
	}

	/**
	 * Método acessor para obter o valor do atributo varAngleGroup.
	 *
	 * @return Retorna o atributo varAngleGroup.
	 */
	public double getVarAngleGroup() {
		return varAngleGroup;
	}

	/**
	 * Método acessor para modificar o valor do atributo varAngleGroup.
	 *
	 * @param varAngleGroup O valor de varAngleGroup para setar.
	 */
	public void setVarAngleGroup(double varAngleGroup) {
		this.varAngleGroup = varAngleGroup;
	}
	
	

}
