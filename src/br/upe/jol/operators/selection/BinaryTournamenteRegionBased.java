package br.upe.jol.operators.selection;

import java.util.Comparator;

import br.upe.jol.base.HyperCube;

public class BinaryTournamenteRegionBased extends RegionBasedSelection {
	private static final long serialVersionUID = 1L;

	public BinaryTournamenteRegionBased(Comparator<HyperCube> comparator){
		super.setComparator(comparator);
	}
	
	@Override
	public Object execute(HyperCube... object) {
		HyperCube hyperBox1, hyperBox2;
		/*solution1 = object[(int) (Math.random() * (object.length - 1))];
		solution2 = object[(int) (Math.random() * (object.length - 1))];*/
		
		hyperBox1 = object[0];
		hyperBox2 = object[1];

		int flag = comparator.compare(hyperBox1, hyperBox2);
		if (flag == 1)
			return hyperBox1;
		else if (flag == -1)
			return hyperBox2;
		else if (Math.random() < 0.5)
			return hyperBox1;
		else
			return hyperBox2;
	}
	
	
}
