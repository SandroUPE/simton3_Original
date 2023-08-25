package br.upe.jol.operators.mutation;

import br.upe.jol.base.Solution;

public class UniformMutation extends Mutation<Double> {
	public UniformMutation(double mutationProbability) {
		super(mutationProbability);
		this.perturbation = 0.8;
	}

	private static final long serialVersionUID = 1L;

	private double perturbation;

	@Override
	public Object execute(Solution<Double>... object) {
		Solution<Double> solution = (Solution<Double>) object[0];

		for (int var = 0; var < solution.getDecisionVariables().length; var++) {
			if (Math.random() < mutationProbability) {
				double rand = Math.random();
				double tmp = (rand - 0.5) * perturbation;

				tmp += solution.getDecisionVariables()[var];

				if (tmp < solution.getProblem().getLowerLimit(var))
					tmp = solution.getProblem().getLowerLimit(var);
				else if (tmp > solution.getProblem().getUpperLimit(var))
					tmp = solution.getProblem().getUpperLimit(var);

				solution.setValue(var, tmp);
			}
		}
		return solution;
	}

	@Override
	public String getOpID() {
		return "M4";
	}

}
