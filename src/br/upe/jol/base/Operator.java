package br.upe.jol.base;

import java.io.Serializable;

/**
 * @author Danilo
 *
 * @param <T>
 */
public abstract class Operator<T> implements Serializable {
	private static final long serialVersionUID = 1L; 
	
	abstract public String getOpID();

	abstract public Object execute(Solution<T>...object);

	public Object execute(Scheme scheme, Solution<T>...object){
		return execute(object);
	}
} 
