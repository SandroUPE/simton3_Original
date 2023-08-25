package br.upe.jol.operators.selection;

import java.util.Comparator;

import br.upe.jol.base.HyperCube;
import br.upe.jol.base.OperatorRegionBased;

public abstract class RegionBasedSelection extends OperatorRegionBased<HyperCube> {
	private static final long serialVersionUID = 1L;
	
	protected Comparator<HyperCube> comparator;

	public Comparator<HyperCube> getComparator() {
		return comparator;
	}

	public void setComparator(Comparator<HyperCube> comparator) {
		this.comparator = comparator;
	}
}
