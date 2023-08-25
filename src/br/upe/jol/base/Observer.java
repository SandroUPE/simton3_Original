package br.upe.jol.base;

/**
 * Classe para ser usada buffer para atualização de dados da execução de um
 * algoritmo de otimização multi-objetivos.
 * 
 * @author Danilo Araújo
 * @since 05/07/2010
 */
public class Observer {
	private SolutionSet<Double> solutionSet;
	
	private String title;
	
	private int iteration;
	
	private int nFitnessAval;
	
	public Observer(String title) {
		this.title = title;
	}
	
	public Observer() {
	}

	public void setObjective(int solutionIndex, int objectiveIndex, double value) {
		solutionSet.get(solutionIndex).setObjective(objectiveIndex, value);
	}

	public void setVariableValue(int solutionIndex, int variableIndex,
			double value) {
		solutionSet.get(solutionIndex).setValue(variableIndex, value);
	}

	public SolutionSet<Double> getSolutionSet() {
		return solutionSet;
	}

	public void setSolutionSet(SolutionSet<Double> solutionSet) {
		this.solutionSet = solutionSet;
	}

	public int getIteration() {
		return iteration;
	}

	public void setIteration(int iteration) {
		this.iteration = iteration;
	}

	public int getnFitnessAval() {
		return nFitnessAval;
	}

	public void setnFitnessAval(int nFitnessAval) {
		this.nFitnessAval = nFitnessAval;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
