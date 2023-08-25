package br.upe.jol.operators.initializers;

import java.util.HashMap;
import java.util.Map;

import br.cns.models.ErdosRenyiM;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.SolutionONTD;

public class RandomNetworkInitializer extends Initializer<Integer> {
	private ErdosRenyiM gp;

	private int numNodes;

	private double minDensity;

	private double maxDensity;

	private int ampLabel;

	public RandomNetworkInitializer() {
		this(14, 1.0 / (14 - 1), 0.40, 3);
	}

	public RandomNetworkInitializer(int numNodes, double minDensity, double maxDensity, int ampLabel) {
		this.numNodes = numNodes;
		this.minDensity = minDensity;
		this.maxDensity = maxDensity;
		this.ampLabel = ampLabel;
		gp = new ErdosRenyiM((minDensity + maxDensity) / 2, numNodes);
	}

	@Override
	public SolutionSet<Integer> execute(Problem<Integer> problem, int size) {
		SolutionSet<Integer> population = new SolutionSet<>(size);
		Integer[][] adjacencyMatrix = new Integer[numNodes][numNodes];
		Integer[] variables = null;
		Solution<Integer> solution = null;
		int numMaxLinks = 0;
		int lastIndex = problem.getNumberOfVariables() - 1;
		double density = maxDensity;
		double stepDensity = (maxDensity - minDensity) / size;

		Map<Integer, DecisionVariablesCombination> mapa = new HashMap<Integer, DecisionVariablesCombination>();
		int counter = 0;
		while (mapa.size() < size) {
			l1: for (int w = 40; w >= 4; w -= 1) {
				for (int oxc = 5; oxc >= 1; oxc--) {
					mapa.put(counter, new DecisionVariablesCombination(w, oxc, density));
					density -= stepDensity;
					counter++;
					if (mapa.size() > size) {
						break l1;
					}
				}
			}
		}
		for (int i = 0; i < size; i++) {
			numMaxLinks = (int) (mapa.get(i).getDensity() * (numNodes * (numNodes - 1)) / 2);
			gp.setM(numMaxLinks);
			adjacencyMatrix = gp.transform(adjacencyMatrix);
			variables = new Integer[problem.getNumberOfVariables()];
			int index = 0;
			for (int j = 0; j < adjacencyMatrix.length; j++) {
				for (int k = j + 1; k < adjacencyMatrix.length; k++) {
					variables[index] = adjacencyMatrix[j][k] * ampLabel;
					index++;
				}
			}
			variables[lastIndex - 1] = mapa.get(i).getOxc();
			variables[lastIndex] = mapa.get(i).getW();
			solution = new SolutionONTD(problem, variables);
			problem.evaluate(solution);
			if (solution.getObjective(0) < 1) {
				population.add(solution);
			}
		}
		return population;
	}

	public String getOpID() {
		return "rnd";
	}
}
