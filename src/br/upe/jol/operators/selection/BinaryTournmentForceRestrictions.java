package br.upe.jol.operators.selection;

import java.util.Comparator;

import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;

public class BinaryTournmentForceRestrictions<T> extends Selection<T> {
	private static final long serialVersionUID = 1L;

	public BinaryTournmentForceRestrictions(Comparator<Solution<T>> comparator){
		super.setComparator(comparator);
	}
	
	@Override
	public Object execute(Solution<T>... object) {
		Solution<T> solution1, solution2;
		
		solution1 = object[(int)Math.ceil(Math.random()*object.length)-1];
		solution2 = object[(int)Math.ceil(Math.random()*object.length)-1];
		
		while (solution1.getOverallConstraintViolation() > 0){
			solution1 = object[(int)Math.ceil(Math.random()*object.length)-1];
		}
		
		while (solution2.getOverallConstraintViolation() > 0){
			solution2 = object[(int)Math.ceil(Math.random()*object.length)-1];
		}

		int flag = comparator.compare(solution1, solution2);
		if (flag == -1)
			return solution1;
		else if (flag == 1)
			return solution2;
		else if (Math.random() < 0.5)
			return solution1;
		else
			return solution2;
	}
	
	@Override
	public Object execute(SolutionSet<T> object) {
		Solution<T> solution1, solution2;
		
		solution1 = object.getRandom();;
		solution2 = object.getRandom();;

		int flag = comparator.compare(solution1, solution2);
		if (flag == -1)
			return solution1;
		else if (flag == 1)
			return solution2;
		else if (Math.random() < 0.5)
			return solution1;
		else
			return solution2;
	}

	@Override
	public String getOpID() {
		return "S1";
	}
}
