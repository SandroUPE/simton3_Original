package br.upe.jol.operators.initializers;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.SimtonProblem;
import br.upe.jol.problems.simton.SolutionONTD;

public class NetworkInitializer extends  Initializer<Integer> {

	@Override
	public SolutionSet<Integer> execute(Problem<Integer> problem, int size) {
		boolean stop = false;
		SolutionSet<Integer> ss = new SolutionSet<Integer>(size);
		Integer[] variables = null;
		Solution<Integer> solution = null;
		for (int i = 0; i < size; i++){
			stop = false;
			variables = new Integer[problem.getNumberOfVariables()];
			for (int n = 0; n < problem.getNumberOfVariables(); n++){
				variables[n] = 0;
			}
			int numNodes = 14;
			HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
			List<Integer> possibleNodes = new Vector<Integer>();
			for (int n = 0; n < numNodes; n++){
				possibleNodes.add(n);
				map.put(n, 0);
			}
			while (!stop){
				int n1 = (int)(Math.random() * (possibleNodes.size()-1));
				int n2 = (int)(Math.random() * (possibleNodes.size()-1));
				if (n1 != n2){
					int indice = indiceVetor_mpr(n1, n2);
					if (variables[indice] == 0){
						variables[indice] = 7;
						map.put(n1, map.get(n1) + 1);
						map.put(n2, map.get(n2) + 1);	
					}	
				}
				if (map.get(n1) >= 2){
					possibleNodes.remove(n1);
				}
				if (map.get(n2) >= 2){
					possibleNodes.remove(n2);
				}
				if (possibleNodes.size() < 2){
					stop = true;
				}
			}
			solution = new SolutionONTD(problem, variables);
			problem.evaluateConstraints(solution);
			problem.evaluate(solution);
			ss.add(solution);
		}
		return ss;
	}

	private int indiceVetor_mpr(int j, int i) {
		return (j + 13 * i - i * (i + 1) / 2);
	}
	public static void main(String[] args) {
		NetworkInitializer ni = new NetworkInitializer();
		SolutionSet<Integer> ss = ni.execute(new SimtonProblem(14, 2), 5);
		
		System.out.println(ss.toString());
	}
}
