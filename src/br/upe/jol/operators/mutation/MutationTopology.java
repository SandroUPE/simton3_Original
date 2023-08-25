/*
 * *****************************************************************************
 * Copyright (c) 2005
 * Propriedade do Laborat�rio de P&D da Unicap-Itautec
 * Todos os direitos reservados, com base nas leis brasileiras de 
 * copyright
 * Este software � confidencial e de propriedade intelectual do
 * Laborat�rio de P&D da Unicap-Itautec
 * ****************************************************************************
 * Projeto: SIAC - Sistema Itautec de Automa��o Comercial
 * Arquivo: MutationTopology.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * CR		Nome				Data		Descri��o
 * ****************************************************************************
 * 064813-P	Danilo Ara�jo		29/11/2010		Vers�o inicial
 * ****************************************************************************
 */
package br.upe.jol.operators.mutation;

import br.upe.jol.base.Solution;

/**
 * TODO Descri��o do tipo
 * 
 * @author Danilo Ara�jo
 * @since 29/11/2010
 */
public class MutationTopology extends Mutation<Integer> {
	public MutationTopology(double mutationProbability) {
		super(mutationProbability);
	}

	private static final long serialVersionUID = 21L;

	@Override
	public Object execute(Solution<Integer>... object) {
		Solution<Integer> solution = (Solution<Integer>) object[0];
		//na topologia � feita muta��o considerando a probabilidade
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
		return solution;
	}
	
	@Override
	public String getOpID() {
		return "M6";
	}
}
