package br.upe.jol.metrics;

import br.upe.jol.base.Metric;
import br.upe.jol.base.Problem;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.base.comparators.ObjectiveSolutionComparator;

public class MaximumSpread<T> extends Metric<T> {
	// para 2 objetivos
	private static final double MAX_NORM = Math.sqrt(2);
	// para 3 objetivos
	private static final double MAX_NORM_3 = Math.sqrt(3);

	@Override
	public double getValue(SolutionSet<T> solutionSet) {
		return getValue(solutionSet, solutionSet.get(0).getProblem());
	}

	public double getValue(SolutionSet<T> solutionSet, Problem<T> problem) {
		ObjectiveSolutionComparator<T> comparator = new ObjectiveSolutionComparator<T>(0);
		solutionSet.sort(comparator);
		int numberOfObjectives = solutionSet.get(0).numberOfObjectives();
		double result = 0;
		double lengthObjective = 1;

		double[] extreme1 = solutionSet.get(0).getAllObjectives();
		double[] extreme2 = solutionSet.get(solutionSet.size() - 1).getAllObjectives();
		if (problem.getUpperLimitObjective() != null) {
			for (int i = 0; i < numberOfObjectives; i++) {
				lengthObjective = problem.getUpperLimitObjective()[i] - problem.getLowerLimitObjective()[i];
				result += (((extreme1[i] - extreme2[i]) / lengthObjective) * ((extreme1[i] - extreme2[i]) / lengthObjective))
						/ MAX_NORM;
			}
		} else {
			for (int i = 0; i < numberOfObjectives; i++) {
				result += (Math.log10(extreme1[i]) - Math.log10(extreme2[i]))
						* (Math.log10(extreme1[i]) - Math.log10(extreme2[i]));
			}
		}

		return Math.sqrt(result);
	}
}
