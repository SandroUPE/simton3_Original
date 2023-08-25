package br.upe.jol.base;

import java.io.Serializable;

/**
 * @author Erick
 *
 * @param <T>
 */
public abstract class OperatorRegionBased<T> implements Serializable {
	private static final long serialVersionUID = 1L;

	abstract public Object execute(HyperCube...object);

} 
