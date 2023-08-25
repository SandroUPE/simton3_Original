package br.upe.jol.operators.crossover;

import br.upe.jol.base.Scheme;
import br.upe.jol.base.Solution;
import br.upe.jol.metaheuristics.spea2.ISPEA2Solution;
import br.upe.jol.problems.simton.SimtonProblem;
import br.upe.jol.problems.simton.SolutionONTD;

public class ICrossoverTopology extends Crossover<Integer> {
	private static final long serialVersionUID = 12L;
	
	public ICrossoverTopology(double crossoverPropability) {
		super(crossoverPropability);
	}

	@Override
	public Object execute(Solution<Integer>... parents) {
		SolutionONTD[] offSpring = new SolutionONTD[2];
		
		SolutionONTD offs1 = null;
		SolutionONTD offs2 = null;
		
		if(parents[0] instanceof ISPEA2Solution){
			 offs1 = new ISPEA2Solution(parents[0].getProblem(), parents[0].getDecisionVariables());
			 offs2 = new ISPEA2Solution(parents[1].getProblem(), parents[1].getDecisionVariables());
		}else{
			 offs1 = new SolutionONTD(parents[0].getProblem(), parents[0].getDecisionVariables());
			 offs2 = new SolutionONTD(parents[1].getProblem(), parents[1].getDecisionVariables());
		}
		
		if (Math.random() < crossoverProbability){
			Integer[] variables1 = parents[0].getDecisionVariables();
			Integer[] variables2 = parents[1].getDecisionVariables();
			
			for(int i=0; i<variables1.length-2; i++){
				if (Math.random() > 0.5) {
					offs1.setValue(i, variables2[i]);
					offs2.setValue(i, variables1[i]);
				}	
			}	
		}
		
		offSpring[0] = offs1;
		offSpring[1] = offs2;
		
		return offSpring ;
	}

	@Override
	public Object execute(Scheme scheme, Solution<Integer>... parents) {
		SolutionONTD[] offSpring = new SolutionONTD[2];
		
		SolutionONTD offs1 = null;
		SolutionONTD offs2 = null;
		
		if(parents[0] instanceof ISPEA2Solution){
			 offs1 = new ISPEA2Solution(parents[0].getProblem(), parents[0].getDecisionVariables());
			 offs2 = new ISPEA2Solution(parents[1].getProblem(), parents[1].getDecisionVariables());
		}else{
			 offs1 = new SolutionONTD(parents[0].getProblem(), parents[0].getDecisionVariables());
			 offs2 = new SolutionONTD(parents[1].getProblem(), parents[1].getDecisionVariables());
		}
		
		if (Math.random() < crossoverProbability){
			Integer[] variables1 = parents[0].getDecisionVariables();
			Integer[] variables2 = parents[1].getDecisionVariables();
			
			for(int i=0; i<variables1.length; i++){
				if (!scheme.getValor()[i].equals(Scheme.STR_CORINGA) && Math.random() > 0.5) {
					offs1.setValue(i, variables2[i]);
					offs2.setValue(i, variables1[i]);
				}	
			}	
		}
		
		offSpring[0] = offs1;
		offSpring[1] = offs2;
		
		return offSpring ;
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
		SolutionONTD sol2 = new SolutionONTD(ontd, variables);
		
		ICrossover op = new ICrossover(1);
		
		SolutionONTD[] sol = (SolutionONTD[])op.execute(sol1, sol2);
		
		System.out.println(sol[0]);
	}

	@Override
	public String getOpID() {
		return "C3";
	}

}
