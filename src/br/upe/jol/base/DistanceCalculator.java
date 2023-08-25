package br.upe.jol.base;

import javax.management.JMException;

import br.upe.jol.base.comparators.ObjectiveSolutionComparator;

/**
 * Esta classe foi adaptada a partir da classe Distance do framework JMetal.
 * Implementa m�todos utilit�rios para c�lculo de dist�ncia e � constru�da no
 * padr�o Singleton.
 * 
 * @author Danilo Ara�jo
 */
public class DistanceCalculator {
	private static final DistanceCalculator instance = new DistanceCalculator();

	private DistanceCalculator() {
	}

	/**
	 * Retorna a matriz com as dist�ncias entre solu��es de um
	 * <code>SolutionSet</code>.
	 * 
	 * @param solutionSet
	 *            O conjunto de solu��es <code>SolutionSet</code>.
	 * @return uma matriz de dist�ncias.
	 */
	public double[][] getDistanceMatrix(SolutionSet<Double> solutionSet) {
		Solution<Double> solutionI, solutionJ;

		double[][] distance = new double[solutionSet.size()][solutionSet.size()];
		for (int i = 0; i < solutionSet.size(); i++) {
			distance[i][i] = 0.0;
			solutionI = solutionSet.get(i);
			for (int j = i + 1; j < solutionSet.size(); j++) {
				solutionJ = solutionSet.get(j);
				distance[i][j] = this.getDistanceBetweenObjectives(solutionI, solutionJ);
				distance[j][i] = distance[i][j];
			}
		}

		return distance;
	}

	/**
	 * Retorna a dist�ncia m�nima de um objeto <code>Solution</code> at� um
	 * <code>SolutionSet</code> no espa�o de objetivos.
	 * 
	 * @param solution
	 *            O objeto <code>Solution</code>.
	 * @param solutionSet
	 *            O objeto <code>SolutionSet</code>.
	 * @return A dist�ncia m�nima da solu��o ao conjunto indicado.
	 * @throws JMException
	 */
	public double getDistanceToSolutionSet(Solution<Double> solution, SolutionSet<Double> solutionSet)
			throws JMException {
		double distance = Double.MAX_VALUE;

		// encontra a dist�ncia m�nima com rela��o � popula��o
		for (int i = 0; i < solutionSet.size(); i++) {
			double aux = this.getDistanceBetweenObjectives(solution, solutionSet.get(i));
			if (aux < distance)
				distance = aux;
		}

		return distance;
	}

	/**
	 * Retorna a dist�ncia m�nima de um objeto <code>Solution</code> at� um
	 * <code>SolutionSet</code> no espa�o de vari�veis.
	 * 
	 * @param solution
	 *            O objeto <code>Solution</code>.
	 * @param solutionSet
	 *            O objeto <code>SolutionSet</code>.
	 * @return A dist�ncia m�nima da solu��o ao conjunto indicado.
	 * @throws JMException
	 */
	public double getDistanceToSolutionSetSolutionSpace(Solution<Double> solution, SolutionSet<Double> solutionSet)
			throws JMException {
		double distance = Double.MAX_VALUE;

		// encontra a dist�ncia m�nima com rela��o � popula��o
		for (int i = 0; i < solutionSet.size(); i++) {
			double aux = this.getDistanceBetweenSolutions(solution, solutionSet.get(i));
			if (aux < distance)
				distance = aux;
		}

		return distance;
	}

	/**
	 * Retorna a dist�ncia entre duas solu��es no espa�o de vari�veis.
	 * 
	 * @param solutionI
	 *            O primeiro objeto <code>Solution</code>.
	 * @param solutionJ
	 *            O segundo objeto <code>Solution</code>.
	 * @return a dist�ncia entre duas solu��es no espa�o de objetivos.
	 */
	public double getDistanceBetweenSolutions(Solution<Double> solutionI, Solution<Double> solutionJ)
			throws JMException {
		Double[] decisionVariableI = (Double[]) solutionI.getDecisionVariables();
		Double[] decisionVariableJ = (Double[]) solutionJ.getDecisionVariables();

		double diff;
		double distance = 0.0;
		// -> Calcula a dist�ncia euclidiana
		for (int i = 0; i < decisionVariableI.length; i++) {
			diff = ((Double[]) decisionVariableI)[i] - ((Double[]) decisionVariableJ)[i];
			distance += Math.pow(diff, 2.0);
		}

		return Math.sqrt(distance);
	}

	/**
	 * Retorna a dist�ncia entre duas solu��es no espa�o de objetivos.
	 * 
	 * @param solutionI
	 *            O primeiro objeto <code>Solution</code>.
	 * @param solutionJ
	 *            O segundo objeto <code>Solution</code>.
	 * @return a dist�ncia entre duas solu��es no espa�o de objetivos.
	 */
	public double getDistanceBetweenObjectives(Solution<Double> solutionI, Solution<Double> solutionJ) {
		double diff;
		double distance = 0.0;
		// -> Calcula a dist�ncia euclidiana
		for (int nObj = 0; nObj < solutionI.numberOfObjectives(); nObj++) {
			diff = solutionI.getObjective(nObj) - solutionJ.getObjective(nObj);
			distance += Math.pow(diff, 2.0);
		}

		return Math.sqrt(distance);
	}

	/**
	 * Seta o valor de crowding distances para todas as solu��es do conjunto
	 * <code>SolutionSet</code>.
	 * 
	 * @param solutionSet
	 *            O conjuneto de solu��es <code>SolutionSet</code>.
	 * @param nObjs
	 *            N�mero de objetivos.
	 */
	public void crowdingDistanceAssignment(SolutionSet<Double> solutionSet, int nObjs) {
		int size = solutionSet.size();

		if (size == 0)
			return;

		if (size == 1) {
			solutionSet.get(0).setCrowdingDistance(Double.POSITIVE_INFINITY);
			return;
		}

		if (size == 2) {
			solutionSet.get(0).setCrowdingDistance(Double.POSITIVE_INFINITY);
			solutionSet.get(1).setCrowdingDistance(Double.POSITIVE_INFINITY);
			return;
		}

		SolutionSet<Double> front = new SolutionSet(size);
		for (int i = 0; i < size; i++) {
			front.add(solutionSet.get(i));
		}

		for (int i = 0; i < size; i++)
			front.get(i).setCrowdingDistance(0.0);

		double objetiveMaxn;
		double objetiveMinn;
		double distance;

		for (int i = 0; i < nObjs; i++) {
			// Ordena a popula��o pelo objetivo n
			front.sort(new ObjectiveSolutionComparator(i));
			objetiveMinn = front.get(0).getObjective(i);
			objetiveMaxn = front.get(front.size() - 1).getObjective(i);

			// Seta crowding distance
			front.get(0).setCrowdingDistance(Double.POSITIVE_INFINITY);
			front.get(size - 1).setCrowdingDistance(Double.POSITIVE_INFINITY);

			for (int j = 1; j < size - 1; j++) {
				distance = front.get(j + 1).getObjective(i) - front.get(j - 1).getObjective(i);
				distance = distance / (objetiveMaxn - objetiveMinn);
				distance += front.get(j).getCrowdingDistance();
				front.get(j).setCrowdingDistance(distance);
			}
		}
	}

	/**
	 * Seta o valor de crowding distances para todas as solu��es do conjunto
	 * <code>SolutionSet</code>.
	 * 
	 * @param solutionSet
	 *            O conjuneto de solu��es <code>SolutionSet</code>.
	 * @param nObjs
	 *            N�mero de objetivos.
	 */
	public void iCrowdingDistanceAssignment(SolutionSet<Integer> solutionSet,
			int nObjs) {
		int size = solutionSet.size();

		if (size == 0)
			return;

		if (size == 1) {
			solutionSet.get(0).setCrowdingDistance(Double.POSITIVE_INFINITY);
			return;
		}

		if (size == 2) {
			solutionSet.get(0).setCrowdingDistance(Double.POSITIVE_INFINITY);
			solutionSet.get(1).setCrowdingDistance(Double.POSITIVE_INFINITY);
			return;
		}

		SolutionSet<Integer> front = new SolutionSet(size);
		for (int i = 0; i < size; i++) {
			front.add(solutionSet.get(i));
		}

		for (int i = 0; i < size; i++)
			front.get(i).setCrowdingDistance(0.0);

		double objetiveMaxn;
		double objetiveMinn;
		double distance;
		boolean[] logscale = front.get(0).getProblem().getLogScale();
		for (int i = 0; i < nObjs; i++) {
			// Ordena a popula��o pelo objetivo n
			front.sort(new ObjectiveSolutionComparator(i));
			objetiveMinn = front.get(0).getObjective(i);
			objetiveMaxn = front.get(front.size() - 1).getObjective(i);

			// Seta crowding distance
			front.get(0).setCrowdingDistance(Double.POSITIVE_INFINITY);
			front.get(size - 1).setCrowdingDistance(Double.POSITIVE_INFINITY);
			// Testar dist�ncia de malahanobis
			if (logscale[i]) {
				for (int j = 1; j < size - 1; j++) {
					distance = Math.log10(front.get(j + 1).getObjective(i))
							- Math.log10(front.get(j - 1).getObjective(i));
					distance = distance / (objetiveMaxn - objetiveMinn);
					distance += front.get(j).getCrowdingDistance();
					front.get(j).setCrowdingDistance(distance);
				}
			} else {
				for (int j = 1; j < size - 1; j++) {
					distance = front.get(j + 1).getObjective(i)
							- front.get(j - 1).getObjective(i);
					distance = distance / (objetiveMaxn - objetiveMinn);
					distance += front.get(j).getCrowdingDistance();
					front.get(j).setCrowdingDistance(distance);
				}	
			}
		}
	}

	public static void main(String[] args) {
		String alfabeto = "ABCDEFGHIJKLMNOPQRSTUVWYXZ";
		String chave = "UAGVXCDFHIZJKLMWNOPEQBRSTY";

		int qtde[] = new int[alfabeto.length()];

		String textoClaro = "A RESPOSTA PARA QUALQUER PROBLEMA ESTA NOS LIVROS. ALUNO BOM SERA ALGUEM NA VIDA. O SEGREDO DO SUCESSO E A EDUCACAO E DEDICACAO. ALIADO A ISSO, IDEIAS EM INVENTAR E INOVAR SOAM BEM.";
		String textoCifrado = "";

		for (int i = 0; i < textoClaro.length(); i++) {
			for (int j = 0; j < alfabeto.length(); j++) {
				if (textoClaro.charAt(i) == alfabeto.charAt(j)) {
					textoCifrado += chave.charAt(j);
					qtde[j]++;
					break;
				}
			}
		}
		System.out.println(textoCifrado);

		for (int j = 0; j < alfabeto.length(); j++) {
			System.out.printf("%s --> %.4f\n", alfabeto.charAt(j) + "", (qtde[j] * 1.0) / textoClaro.length());
		}
	}

	public static DistanceCalculator getInstance() {
		return instance;
	}
}
