package br.upe.jol.base.comparators;

import br.upe.jol.base.Observer;
import br.upe.jol.base.SolutionSet;

public class IObserver extends Observer {
	private SolutionSet<Integer> iSolutionSet;
	
	public void setObjective(int solutionIndex, int objectiveIndex, int value) {
		iSolutionSet.get(solutionIndex).setObjective(objectiveIndex, value);
	}

	public void setVariableValue(int solutionIndex, int variableIndex,
			int value) {
		iSolutionSet.get(solutionIndex).setValue(variableIndex, value);
	}

	public SolutionSet<Integer> getISolutionSet() {
		return iSolutionSet;
	}

	public void setISolutionSet(SolutionSet<Integer> solutionSet) {
		this.iSolutionSet = solutionSet;
	}
}