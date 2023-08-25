package br.upe.jol.operators.mutation;

import br.upe.jol.base.Scheme;
import br.upe.jol.base.Solution;

public class IFashionMutation extends Mutation<Integer> {
	public IFashionMutation(double mutationProbability) {
		super(mutationProbability);
	}

	private static final long serialVersionUID = 23L;
	
	private int indiceVetor(int numberOfNodes, int j, int i) {
		return (j + (numberOfNodes - 1) * i - i * (i + 1) / 2)-1;
	}
	
	private int[] getPar(int numberOfNodes, int index){
		int[] ret = new int[2];
		
		for (int i = 0; i < numberOfNodes; i++){
			for (int j = i+1; j < numberOfNodes; j++){
				if ((j + (numberOfNodes - 1) * i - i * (i + 1) / 2)-1 == index){
					ret[0] = i;
					ret[1] = j;
					break;
				}
			}	
		}
		
		return ret; 
	}

	@Override
	public Object execute(Solution<Integer>... object) {
		Solution<Integer> solution = (Solution<Integer>) object[0];
		int numberOfNodes = 14;
		int numTopologia = solution.getDecisionVariables().length - numberOfNodes - 2;
		if (Math.random() < mutationProbability) {
			if (Math.random() < .25){
				//trocar o nó destino de um enlace já estabelecido
				int index = (int) Math.round(Math.random() * (solution.getDecisionVariables().length - numberOfNodes - 2));
				int idAmp = solution.getDecisionVariables()[index];
				while (idAmp == 0){
					index = (int) Math.round(Math.random() * (solution.getDecisionVariables().length - numberOfNodes - 2));
					idAmp = solution.getDecisionVariables()[index];
				}
				int[] nodes = getPar(numberOfNodes, index);
				int novoDestino = (int) Math.round(Math.random() * numberOfNodes);
				int novoIndex = indiceVetor(numberOfNodes, nodes[0], novoDestino);
				while (novoDestino == nodes[0] || solution.getDecisionVariables()[novoIndex] != 0){
					novoDestino = (int) Math.round(Math.random() * numberOfNodes);
					novoIndex = indiceVetor(numberOfNodes, nodes[0], novoDestino);
				}
				solution.setValue(novoIndex, idAmp);
				solution.setValue(index, 0);
			}
			if (Math.random() < .25){
				//remover enlace
				int index = (int) Math.round(Math.random() * (solution.getDecisionVariables().length - numberOfNodes - 2));
				int idAmp = solution.getDecisionVariables()[index];
				while (idAmp == 0){
					index = (int) Math.round(Math.random() * (solution.getDecisionVariables().length - numberOfNodes - 2));
					idAmp = solution.getDecisionVariables()[index];
				}
				solution.setValue(index, 0);
			}
			if (Math.random() < .25){
				//incluir enlace
				int index = (int) Math.round(Math.random() * (solution.getDecisionVariables().length - numberOfNodes - 2));
				int idAmp = solution.getDecisionVariables()[index];
				while (idAmp != 0){
					index = (int) Math.round(Math.random() * (solution.getDecisionVariables().length - numberOfNodes - 2));
					idAmp = solution.getDecisionVariables()[index];
				}
				solution.setValue(index, 3);
			}
			if (Math.random() < .25){
				//inclusão/remoção de regenerador (troca de função)
				int index = (int) Math.round(Math.random() * (solution.getDecisionVariables().length - numTopologia)) + numTopologia;
				if (solution.getDecisionVariables()[index] == 0){
					solution.setValue(index, (int) Math.round(Math.random() * 100));
				}else{
					solution.setValue(index, 0);
				}
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
			} else if (Math.random() < mutationProbability) {
				int iValue = (int) (Math.round(Math.random()
						* (solution.getProblem().getUpperLimit(var) - solution.getProblem().getLowerLimit(var))) + solution
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
		return "M3";
	}
}
