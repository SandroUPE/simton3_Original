/**
 * 
 */
package br.upe.jol.operators.crossover;

import br.upe.jol.base.Solution;

/**
 * @author Danilo
 * 
 */
public class SBXCrossover extends Crossover<Double> {
	public SBXCrossover(double crossoverPropability) {
		super(crossoverPropability);
	}

	private static final long serialVersionUID = 1L;

	protected static final double EPS = 1.0e-14;

	public double eta_c = 20.0;

	@Override
	public Object execute(Solution<Double>... parents) {
		Solution<Double>[] offSpring = new Solution[2];

		offSpring[0] = parents[0];
		offSpring[1] = parents[1];

		int i;
		double rand;
		double y1, y2, yL, yu;
		double c1, c2;
		double alpha, beta, betaq;
		double valueX1, valueX2;
		Solution<Double> x1 = parents[0];
		Solution<Double> x2 = parents[1];
		Solution<Double> offs1 = offSpring[0];
		Solution<Double> offs2 = offSpring[1];

		int numberOfVariables = x1.getDecisionVariables().length;

		if (Math.random() <= crossoverProbability) {
			for (i = 0; i < numberOfVariables; i++) {
				valueX1 = x1.getDecisionVariables()[i];
				valueX2 = x2.getDecisionVariables()[i];
				if (Math.random() <= 0.5) {
					if (Math.abs(valueX1 - valueX2) > EPS) {

						if (valueX1 < valueX2) {
							y1 = valueX1;
							y2 = valueX2;
						} else {
							y1 = valueX2;
							y2 = valueX1;
						} 

						yL = x1.getProblem().getLowerLimit(i);
						yu = x1.getProblem().getUpperLimit(i);
						rand = Math.random();
						beta = 1.0 + (2.0 * (y1 - yL) / (y2 - y1));
						alpha = 2.0 - Math.pow(beta, -(eta_c + 1.0));

						if (rand <= (1.0 / alpha)) {
							betaq = Math.pow((rand * alpha),
									(1.0 / (eta_c + 1.0)));
						} else {
							betaq = Math.pow((1.0 / (2.0 - rand
									* alpha)), (1.0 / (eta_c + 1.0)));
						} 

						c1 = 0.5 * ((y1 + y2) - betaq * (y2 - y1));
						beta = 1.0 + (2.0 * (yu - y2) / (y2 - y1));
						alpha = 2.0 - Math.pow(beta, -(eta_c + 1.0));

						if (rand <= (1.0 / alpha)) {
							betaq = Math.pow((rand * alpha),
									(1.0 / (eta_c + 1.0)));
						} else {
							betaq = Math.pow((1.0 / (2.0 - rand * alpha)), (1.0 / (eta_c + 1.0)));
						}

						c2 = 0.5 * ((y1 + y2) + betaq * (y2 - y1));

						if (c1 < yL)
							c1 = yL;

						if (c2 < yL)
							c2 = yL;

						if (c1 > yu)
							c1 = yu;

						if (c2 > yu)
							c2 = yu;

						if (Math.random() <= 0.5) {
							offs1.setValue(i, c2);
							offs2.setValue(i, c1);
						} else {
							offs1.setValue(i, c1);
							offs2.setValue(i, c2);
						}
					} else {
						offs1.setValue(i, valueX1);
						offs2.setValue(i, valueX2);
					}
				} else {
					offs1.setValue(i, valueX2);
					offs2.setValue(i, valueX1);
				}
			}
		}
		
		offSpring[0] = offs1;
		offSpring[1] = offs2;
		
		return offSpring;
	}

	@Override
	public String getOpID() {
		return "C7";
	}

}
