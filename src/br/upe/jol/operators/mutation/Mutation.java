package br.upe.jol.operators.mutation;

import br.upe.jol.base.Operator;

public abstract class Mutation<T> extends Operator<T> {
	private static final long serialVersionUID = 1L;

	protected double mutationProbability;

	public Mutation(double mutationProbability){
		this.mutationProbability = mutationProbability;
	}

	public double getMutationProbability() {
		return mutationProbability;
	}

	public void setMutationProbability(double mutationProbability) {
		this.mutationProbability = mutationProbability;
	}
}
