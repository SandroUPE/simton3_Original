package br.upe.jol.base;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import br.upe.jol.configuration.GeneralMOOParametersTO;

/**
 * @author Danilo
 * 
 * @param <T>
 */
public abstract class Algorithm<T> implements Serializable {
	private static final long serialVersionUID = 1L;

	protected static NumberFormat nf = NumberFormat.getInstance();

	public static NumberFormat itf = NumberFormat.getInstance();

	protected Map<String, Operator<T>> operators;

	protected Problem<T> problem;

	public abstract SolutionSet<T> execute();

	public abstract SolutionSet<T> execute(SolutionSet<T> ss, int lastGeneration);

	protected Observer observer;

	static {
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		itf.setMaximumFractionDigits(0);
		itf.setMinimumIntegerDigits(4);
	}

	public void addOperator(String name, Operator<T> operator) {
		if (operators == null) {
			operators = new HashMap<String, Operator<T>>();
		}
		operators.put(name, operator);
	}

	public Operator<T> getOperator(String name) {
		return operators.get(name);
	}

	public Map<String, Operator<T>> getOperators() {
		return operators;
	}

	public void setOperators(Map<String, Operator<T>> operators) {
		this.operators = operators;
	}

	public Problem<T> getProblem() {
		return problem;
	}

	public void setProblem(Problem<T> problem) {
		this.problem = problem;
	}

	public Observer getObserver() {
		return observer;
	}

	public void setObserver(Observer observer) {
		this.observer = observer;
	}

	public abstract void setParameters(GeneralMOOParametersTO parameters);

	public static void main(String[] args) {
		Scanner console = new Scanner(System.in);
		String nome = console.nextLine();
		System.out.println("Este programa foi  criado por " + nome);
	}
}
