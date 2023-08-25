package br.upe.jol.metrics;

import br.upe.jol.base.Metric;
import br.upe.jol.base.SolutionSet;

public class Spacing<T> extends Metric<T> {

	@Override
	public double getValue(SolutionSet<T> solutionSet) {
		double sum = 0;
		double di = 0;
		double result = 0;
		solutionSet.evaluateMeanDiSpacing();
		double meanDi = solutionSet.getMeanDiSpacing();

		for (int i = 0; i < solutionSet.size(); i++) {
			di = solutionSet.get(i).getDiSpacing();

			sum += (meanDi - di) * (meanDi - di);
		}

		result = (1.0 / (solutionSet.size() - 1)) * sum;

		return Math.sqrt(result);
	}
}
