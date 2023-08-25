package br.upe.jol.problems.simton;

import br.upe.jol.problems.simon.rwa.InterferenceYenSimulator;

public class SimtonInterferenceProblem extends SimtonProblem {
	public SimtonInterferenceProblem(int numberOfNodes_ppr,
			int numberOfObjectives) {
		super(numberOfNodes_ppr, numberOfObjectives);
		simulator = new InterferenceYenSimulator();
	}
	
	public SimtonInterferenceProblem(int numberOfNodes_ppr,
			int numberOfObjectives, int calls) {
		super(numberOfNodes_ppr, numberOfObjectives, calls);
		simulator = new InterferenceYenSimulator();
	}

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		SimtonInterferenceProblem p = new SimtonInterferenceProblem(14, 2);
		Integer[][] nsfnetorigem = new Integer[][] { 
				{ 0, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0 },
				{ 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, 
				{ 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0 }, 
				{ 0, 0, 0, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0 },
				{ 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1 },
				{ 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0 }, //
				{ 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0 }, //
				{ 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 1, 0 },//
				{ 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0 },//
				{ 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0 },//
				{ 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 1 },
				{ 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 0 } };
		Integer[] variables = new Integer[] { 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 1, 4,
				40 };


		SolutionONTD solution = new SolutionONTD(p, variables);

		SimtonInterferenceProblem problem = new SimtonInterferenceProblem(14, 2, 200);
		
		for (int i = 0; i < 1; i++) {
			problem.evaluate(solution);
		}

	}
}
