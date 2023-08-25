package br.upe.jol.base;

import static br.upe.jol.base.Util.LOGGER;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ForkJoinPool;

import br.upe.jol.metrics.Hypervolume;
import br.upe.jol.metrics.MaximumSpread;
import br.upe.jol.metrics.Spacing;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * @author Danilo
 * 
 * @param <T>
 */
public class SolutionSet<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	protected List<Solution<T>> solutionsList;

	private int capacity = 0;

	private double meanDiSpacing;

	private double difAngleSolutions;

	private double difHVSolutions;
	
	public SolutionSet() {
		solutionsList = new ArrayList<Solution<T>>();
	}

	public SolutionSet(int maximumSize) {
		solutionsList = new ArrayList<Solution<T>>(capacity);
		capacity = maximumSize;
	}

	public SolutionSet(String path, Problem<T> problem) throws ClassNotFoundException {
		solutionsList = new ArrayList<Solution<T>>();
		this.readObjectivesFromFile(path, problem);
	}
	
	/**
	 * Evaluate all solutions. Fork/join is used to improve performance.
	 */
	public void evaluate() {
		long t0 = System.currentTimeMillis();
		MulticoreEvaluator<T> mce = new MulticoreEvaluator<T>(solutionsList, 0, this.size(), this.size()/8);
		ForkJoinPool pool = new ForkJoinPool();
		pool.invoke(mce);
		Util.LOGGER.info("Avaliações concluídas em " + (System.currentTimeMillis() - t0)/60000.0 + " min.");
	}

	public boolean add(Solution<T> solution) {
		/*
		 * if (solutionsList.size() == capacity) {
		 * LOGGER.severe("The population is full");
		 * LOGGER.severe("Capacity is : " + capacity);
		 * LOGGER.severe("\t Size is: " + this.size()); return false; }
		 */

		solutionsList.add(solution);
		return true;
	}

	public Solution<T> get(int i) {
		if (i >= solutionsList.size()) {
			throw new IndexOutOfBoundsException("Index out of Bound " + i);
		}
		return solutionsList.get(i);
	}

	public Solution<T> getLast() {
		return solutionsList.get(this.size() - 1);
	}

	public void update(int i, Solution<T> element) {
		if (i >= solutionsList.size()) {
			throw new IndexOutOfBoundsException("Index out of Bound " + i);
		}
		solutionsList.remove(i);
		solutionsList.add(i, element);
	}

	public Solution<T> getRandom() {
		if (this.size() == 0) {
			return null;
		} else if (this.size() == 1) {
			return solutionsList.get(0);
		}
		int i = new Random().nextInt(this.size() - 1);
		return solutionsList.get(i);
	}

	public int getMaxSize() {
		return capacity;
	}

	public void sort(Comparator<Solution<T>> comparator) {
		if (comparator == null) {
			LOGGER.severe("No criterium for compare exist");
			return;
		}
		Collections.sort(solutionsList, comparator);
	}

	public int size() {
		return solutionsList.size();
	}

	public void printObjectivesToFile(String path) {
		try {
			FileOutputStream fos = new FileOutputStream(path);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			BufferedWriter bw = new BufferedWriter(osw);

			for (int i = 0; i < solutionsList.size(); i++) {
				bw.write(solutionsList.get(i).toString());
				bw.newLine();
			}

			bw.close();
		} catch (IOException e) {
			LOGGER.severe("Error acceding to the file");
			e.printStackTrace();
		}
	}

	public void readObjectivesFromFile(String path, Problem<T> problem) {
		NumberFormat nf = NumberFormat.getInstance();
		
		try {
			FileInputStream fis = new FileInputStream(path);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);

			while (br.ready()) {
				String[] objectives = br.readLine().split(" ");
				SolutionONTD solution = null;
				solution = new SolutionONTD((Problem<Integer>) problem, new Integer[problem.numberOfVariables]);
				for (int i = 0; i < problem.numberOfObjectives; i++){
					try {
						solution.setObjective(i, Double.parseDouble(objectives[i]));
					} catch (NumberFormatException e) {
						//para os casos em que o arquivo de experimentos está em PT-BR
						try {
							solution.setObjective(i, nf.parse(objectives[i]).doubleValue());
						} catch (ParseException e1) {
							e1.printStackTrace();
						}
					}
				}

				this.solutionsList.add((Solution<T>) solution);
			}

			br.close();
		} catch (IOException e) {
			LOGGER.severe("Error acceding to the file");
			e.printStackTrace();
		}
	}

	public void readExistingObjectivesFromFile(String path, Problem<T> problem) {
		NumberFormat nf = NumberFormat.getInstance();
		try {
			FileInputStream fis = new FileInputStream(path);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			int sol = 0;
			while (br.ready()) {
				String[] objectives = br.readLine().split(" ");
				for (int i = 0; i < problem.numberOfObjectives; i++){
					try {
						this.solutionsList.get(sol).setObjective(i, Double.parseDouble(objectives[i]));
					} catch (NumberFormatException e) {
						//para os casos em que o arquivo de experimentos está em PT-BR
						try {
							this.solutionsList.get(sol).setObjective(i, nf.parse(objectives[i]).doubleValue());
						} catch (ParseException e1) {
							e1.printStackTrace();
						}
					}
				}
				sol++;
			}

			br.close();
		} catch (IOException e) {
			LOGGER.severe("Error acceding to the file");
			e.printStackTrace();
		}
	}

	public void readDoubleVariablesFromFile(String path) {
		solutionsList = new Vector<Solution<T>>();
		Solution<Double> solution;
		int i;
		try {
			FileReader fis = new FileReader(path);
			LineNumberReader lnr = new LineNumberReader(fis);
			String line = lnr.readLine();
			while (line != null) {
				solution = new Solution<Double>();
				i = 0;
				for (String s : line.split("\t")) {
					solution.setValue(i, Double.parseDouble(s));
					i++;
				}
				line = lnr.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readIntVariablesFromFile(String path, Problem<Integer> problem) {
		solutionsList = new Vector<Solution<T>>();
		SolutionONTD solution;
		String[] linha;
		Integer[] variables;
		try {
			FileReader fis = new FileReader(path);
			LineNumberReader lnr = new LineNumberReader(fis);
			String line = lnr.readLine();
			while (line != null) {
				linha = line.split("\t");
				variables = new Integer[linha.length];
				for (int i = 0; i < linha.length; i++) {
					variables[i] = (int) (Double.parseDouble(linha[i]));
				}
				line = lnr.readLine();
				solution = new SolutionONTD(problem, variables);
				solutionsList.add((Solution<T>) solution);
			}
			lnr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void readIntVariablesFromFileCN(String path, Problem<Integer> problem) {
		NumberFormat nf = NumberFormat.getNumberInstance();
		solutionsList = new Vector<Solution<T>>();
		SolutionONTD solution;
		String[] linha;
		Integer[] variables;
		try {
			FileReader fis = new FileReader(path);
			LineNumberReader lnr = new LineNumberReader(fis);
			String line = lnr.readLine();
			double pb = 0;
			while (line != null) {
				linha = line.split(";");
				pb = nf.parse(linha[0]).doubleValue();
				variables = new Integer[93];
				variables[variables.length -1] = (int) (nf.parse(linha[1]).doubleValue());
				variables[variables.length -2] = (int) (nf.parse(linha[2]).doubleValue());
				linha = linha[11].split(" ");
				for (int i = 0; i < linha.length; i++) {
					variables[i] = (int) (nf.parse(linha[i]).doubleValue());
				}
				
				line = lnr.readLine();
				solution = new SolutionONTD(problem, variables);
				solution.setObjective(0, pb);
				solutionsList.add((Solution<T>) solution);
			}
			lnr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void readExistingIntVariablesFromFile(String path, Problem<Integer> problem) {
		String[] linha;
		int sol = 0;
		try {
			FileReader fis = new FileReader(path);
			LineNumberReader lnr = new LineNumberReader(fis);
			String line = lnr.readLine();
			while (line != null) {
				linha = line.split("\t");
				for (int i = 0; i < linha.length; i++) {
					((SolutionONTD) solutionsList.get(sol)).setValue(i, (int) Double.parseDouble(linha[i]));
					;
				}
				sol++;
				line = lnr.readLine();
			}
			lnr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void printVariablesToFile(String path) {
		try {
			FileOutputStream fos = new FileOutputStream(path);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			BufferedWriter bw = new BufferedWriter(osw);

			int numberOfVariables = solutionsList.get(0).getDecisionVariables().length;
			for (int i = 0; i < solutionsList.size(); i++) {
				for (int j = 0; j < numberOfVariables; j++)
					bw.write(solutionsList.get(i).getDecisionVariables()[j].toString() + "\t");
				bw.newLine();
			}

			bw.close();
		} catch (IOException e) {
			LOGGER.severe("Error acceding to the file");
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		int numberOfVariables = solutionsList.get(0).getDecisionVariables().length;
		for (int i = 0; i < solutionsList.size(); i++) {
			for (int j = 0; j < numberOfVariables; j++)
				sb.append(solutionsList.get(i).getDecisionVariables()[j].toString() + "\t");
			sb.append('\n');
		}
		return sb.toString();
	}

	public void clear() {
		solutionsList.clear();
	}

	public void remove(int i) {
		if (i > solutionsList.size() - 1) {
			LOGGER.severe("Size is: " + this.size());
		}
		solutionsList.remove(i);
	}

	public void remove(Solution<T> solution) {
		for (int i = 0; i < solutionsList.size(); i++) {
			if (solutionsList.get(i).equals(solution)) {
				this.remove(i);
				break;
			}

			if (i == solutionsList.size() - 1) {
				System.out.println("Nada.");
			}

		}
	}

	public SolutionSet<T> union(SolutionSet<T> solutionSet) {
		int newSize = this.size() + solutionSet.size();
		if (newSize < capacity)
			newSize = capacity;

		SolutionSet<T> union = new SolutionSet<T>(newSize);
		for (int i = 0; i < this.size(); i++) {
			union.add(this.get(i));
		}

		for (int i = this.size(); i < (this.size() + solutionSet.size()); i++) {
			union.add(solutionSet.get(i - this.size()));
		}

		return union;
	}

	public void replace(int position, Solution<T> solution) {
		if (position > this.solutionsList.size()) {
			solutionsList.add(solution);
		}
		solutionsList.remove(position);
		solutionsList.add(position, solution);
	}

	public double[][] writeObjectivesToMatrix() {
		if (this.size() == 0) {
			return null;
		}
		boolean[] logscale = get(0).getProblem().getLogScale();
		double[][] objectives;
		objectives = new double[size()][get(0).numberOfObjectives()];
		for (int i = 0; i < size(); i++) {
			for (int j = 0; j < get(0).numberOfObjectives(); j++) {
				if (logscale[j]) {
					objectives[i][j] = Math.log10(get(i).getObjective(j));
				} else {
					objectives[i][j] = get(i).getObjective(j);	
				}
			}
		}
		return objectives;
	}

	public double[][] writeDistancesToMatrix() {
		if (this.size() == 0) {
			return null;
		}
		double[][] distances;
		distances = new double[size()][size() - 1];
		for (int i = 0; i < size(); i++) {
			Solution<T> solution1 = this.get(i);
			distances[i] = solution1.distanceOrdered(this);
		}
		return distances;
	}

	public boolean contains(Solution<T> solution) {
		return this.solutionsList.contains(solution);
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public List<Solution<T>> getSolutionsList() {
		return solutionsList;
	}

	public void setSolutionsList(List<Solution<T>> solutionsList) {
		this.solutionsList = solutionsList;
	}

	public double getMeanDiSpacing() {
		return meanDiSpacing;
	}

	public void setMeanDiSpacing(double meanDiSpacing) {
		this.meanDiSpacing = meanDiSpacing;
	}

	public void evaluateMeanDiSpacing() {
		double sum = 0;

		for (int i = 0; i < this.size(); i++) {
			this.get(i).evaluateDi(this);
			sum += this.get(i).getDiSpacing();
		}

		double s = (double) this.size();
		this.setMeanDiSpacing(sum / s);
	}

	/**
	 * Método acessor para obter o valor do atributo difAngleSolutions.
	 * 
	 * @return Retorna o atributo difAngleSolutions.
	 */
	public double getDifAngleSolutions() {
		return difAngleSolutions;
	}

	/**
	 * Método acessor para modificar o valor do atributo difAngleSolutions.
	 * 
	 * @param difAngleSolutions
	 *            O valor de difAngleSolutions para setar.
	 */
	public void setDifAngleSolutions(double difAngleSolutions) {
		this.difAngleSolutions = difAngleSolutions;
	}

	/**
	 * Método acessor para obter o valor do atributo difHVSolutions.
	 * 
	 * @return Retorna o atributo difHVSolutions.
	 */
	public double getDifHVSolutions() {
		return difHVSolutions;
	}

	/**
	 * Método acessor para modificar o valor do atributo difHVSolutions.
	 * 
	 * @param difHVSolutions
	 *            O valor de difHVSolutions para setar.
	 */
	public void setDifHVSolutions(double difHVSolutions) {
		this.difHVSolutions = difHVSolutions;
	}

	public void printMetrics(String path, int generation) {
		try {
			// Limpa arquivo
			if (generation == 0) {
				FileOutputStream fos = new FileOutputStream(path + "Metrics.txt");
				OutputStreamWriter osw = new OutputStreamWriter(fos);
				BufferedWriter bw = new BufferedWriter(osw);

				bw.write("HYPERVOLUME\t\tSPACING\t\tMAX SPREAD");

				bw.close();
			}

			FileOutputStream fos = new FileOutputStream(path + "Metrics.txt", true);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			BufferedWriter bw = new BufferedWriter(osw);

			Hypervolume<T> hv = new Hypervolume<T>();
			MaximumSpread<T> ms = new MaximumSpread<T>();
			Spacing<T> spacing = new Spacing<T>();

			bw.newLine();
			bw.write("" + hv.getValue(this));
			bw.write("\t\t" + spacing.getValue(this));
			bw.write("\t\t" + ms.getValue(this));

			bw.close();
		} catch (IOException e) {
			LOGGER.severe("Error acceding to the file");
			e.printStackTrace();
		}
	}
}
