package br.upe.jol.operators.mutation;

import br.upe.jol.base.Solution;
import br.upe.jol.problems.simton.SimtonProblem;
import br.upe.jol.problems.simton.SolutionONTD;

public class IPertubMutation extends Mutation<Integer> {
	
	public IPertubMutation(double mutationProbability) {
		super(mutationProbability);
	}

	private static final long serialVersionUID = 22L;

	@Override
	public Object execute(Solution<Integer>... object) {
		Solution<Integer> solution = (Solution<Integer>) object[0];

		for (int var = 0; var < solution.getDecisionVariables().length; var++) {
			if (Math.random() < mutationProbability) {
				int iValue = 1;
				if (Math.random()>0.5){
					iValue = -1;
				}
				iValue += solution.getDecisionVariables()[var];

				if (iValue < solution.getProblem().getLowerLimit(var))
					iValue = (int) solution.getProblem().getLowerLimit(var);
				else if (iValue > solution.getProblem().getUpperLimit(var))
					iValue = (int) solution.getProblem().getUpperLimit(var);

				solution.setValue(var, iValue);
			}
		}
		return solution;
	}
	
	public static void main(String[] args) {
		SimtonProblem ontd = new SimtonProblem(14, 2);
		SolutionONTD sol1 = null;
		
		Integer[] variables = null;
		variables = new Integer[ontd.getNumberOfVariables()];
		for (int j = 0; j < ontd.getNumberOfVariables(); j++) {
			if (Math.random() < 0.5){
				variables[j] = (int) Math.round(Math.random()
						* ontd.getUpperLimit(j));	
			}else{
				variables[j] = (int)Math.round(Math.random()
						* ontd.getUpperLimit(j));
				if (variables[j] == 0){
					variables[j] = 1;
				}
			}
		}
		sol1 = new SolutionONTD(ontd, variables);
		
		variables = new Integer[ontd.getNumberOfVariables()];
		for (int j = 0; j < ontd.getNumberOfVariables(); j++) {
			double dRandom = Math.random();
			variables[j] = (int) (Math.round(dRandom 
					* (ontd.getUpperLimit(j) - ontd.getLowerLimit(j))) + ontd.getLowerLimit(j));	
		}
		
		IPertubMutation op = new IPertubMutation(1);
		
		SolutionONTD sol = (SolutionONTD)op.execute(sol1);
		
	}

	@Override
	public String getOpID() {
		return "M2";
	}
}
