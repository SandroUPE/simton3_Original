package br.upe.jol.metrics;

import br.upe.jol.base.Metric;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;

public class Coverage<T> extends Metric<T> {

	private SolutionSet<T> paretoKnown;
	
	public void setParetoKnown(SolutionSet<T> paretoKnown){
		this.paretoKnown = paretoKnown;
	}
	
	@Override
	public double getValue(SolutionSet<T> solutionSet) {
		double sum = 0;
		
		for(int i=0; i<solutionSet.size(); i++){
			Solution<T> solution = solutionSet.get(i);
			for(int j=0; j<paretoKnown.size(); j++){
				//se alguma uma solucao do pareto teorico domina a da solucao encontrada
				if( paretoKnown.get(j).dominates(solution) ){
					sum += 1;
					break;
				}
			}
		}
		
		
		//se retorno = 0 -> nenhuma solucao eh dominada por outra do pareto teorico
		//se retorno = 1 -> todas as solucoes sao dominadas pelo pareto teorico
		return sum/solutionSet.size();
	}

}
