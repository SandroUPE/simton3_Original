/*
 * *****************************************************************************
 * Copyright (c) 2005
 * Propriedade do Laboratório de P&D da Unicap-Itautec
 * Todos os direitos reservados, com base nas leis brasileiras de 
 * copyright
 * Este software é confidencial e de propriedade intelectual do
 * Laboratório de P&D da Unicap-Itautec
 * ****************************************************************************
 * Projeto: SIAC - Sistema Itautec de Automação Comercial
 * Arquivo: IMixMutation.java
 * ****************************************************************************
 * Histórico de revisões
 * CR		Nome				Data		Descrição
 * ****************************************************************************
 * 064813-P	Danilo Araújo		27/11/2010		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.operators.mutation;

import br.upe.jol.base.Solution;
import br.upe.jol.problems.simton.SimtonProblem;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * TODO Descrição do tipo
 * 
 * @author Danilo Araújo
 * @since 27/11/2010
 */
public class IMixMutation extends Mutation<Integer> {
	public IMixMutation(double mutationProbability) {
		super(mutationProbability);
	}

	private static final long serialVersionUID = 21L;

	@Override
	public Object execute(Solution<Integer>... object) {
		Solution<Integer> solution = (Solution<Integer>) object[0];
		//na topologia é feita mutação considerando a probabilidade
		for (int var = 0; var < solution.getDecisionVariables().length-2; var++) {
			if (Math.random() < mutationProbability) {
				int iValue = (int) (Math.round(Math.random()
						* (solution.getProblem().getUpperLimit(var) - solution
								.getProblem().getLowerLimit(var))) + solution
						.getProblem().getLowerLimit(var));

				if (iValue < solution.getProblem().getLowerLimit(var))
					iValue = (int) solution.getProblem().getLowerLimit(var);
				else if (iValue > solution.getProblem().getUpperLimit(var))
					iValue = (int) solution.getProblem().getUpperLimit(var);

				solution.setValue(var, iValue);
			}
		}
		//no oxc e comp onda é feita busca local
		for (int var = solution.getDecisionVariables().length-2; var < solution.getDecisionVariables().length; var++) {
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
		
		IMixMutation op = new IMixMutation(1);
		
		SolutionONTD sol = (SolutionONTD)op.execute(sol1);
		
	}

	@Override
	public String getOpID() {
		return "M1";
	}
}
