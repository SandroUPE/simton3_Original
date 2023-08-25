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
 * Arquivo: IUniformMutation1.java
 * ****************************************************************************
 * Histórico de revisões
 * CR		Nome				Data		Descrição
 * ****************************************************************************
 * 064813-P	Danilo Araújo		11/03/2011		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.operators.mutation;

import br.upe.jol.base.Scheme;
import br.upe.jol.base.Solution;

/**
 * TODO Descrição do tipo
 * 
 * @author Danilo Araújo
 * @since 11/03/2011
 */
public class IUniformMutation1 extends IUniformMutation {
	public IUniformMutation1(double mutationProbability) {
		super(mutationProbability);
	}

	private static final long serialVersionUID = 23L;

	@Override
	public Object execute(Solution<Integer>... object) {
		Solution<Integer> solution = (Solution<Integer>) object[0];

		for (int var = 0; var < solution.getDecisionVariables().length; var++) {
			if (Math.random() < mutationProbability) {
				int iValue = (int)solution.getProblem().getLowerLimit(var);
				if (Math.random() > 0.5){					
					if (var < solution.getDecisionVariables().length - 2){
						iValue = 7;
					}else{
						iValue = (int) (Math.round(Math.random()
								* (solution.getProblem().getUpperLimit(var) - solution
										.getProblem().getLowerLimit(var))) + solution
								.getProblem().getLowerLimit(var));

						if (iValue < solution.getProblem().getLowerLimit(var))
							iValue = (int) solution.getProblem().getLowerLimit(var);
						else if (iValue > solution.getProblem().getUpperLimit(var))
							iValue = (int) solution.getProblem().getUpperLimit(var);
	
					}
				}
				
				solution.setValue(var, iValue);
			}
		}
		return solution;
	}
	
	@Override
	public Object execute(Scheme scheme, Solution<Integer>... object) {
		Solution<Integer> solution = (Solution<Integer>) object[0];

		for (int var = 0; var < solution.getDecisionVariables().length; var++) {
			if (!scheme.getValor()[var].equals(Scheme.STR_CORINGA)) {
				solution.setValue(var, Integer.valueOf(scheme.getValor()[var]));
			}else if (Math.random() < mutationProbability) {
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
		return "M9";
	}
}
