/**
 * 
 */
package br.upe.jol.problems.simon.dbcache;

import java.util.Date;

import br.upe.jol.problems.simon.entity.NetworkBP;
import br.upe.jol.problems.simon.entity.NetworkCost;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * .
 * 
 * @author Danilo Araújo
 */
public class CacheSimulRedeTO {
	protected String key;

	protected NetworkCost custo;
	
	protected NetworkBP bp;

	protected Date dtHrCadastro;

	protected Date dtUltAcesso;

	protected int qtdeAcessos;

	protected double custoTotal;

	protected double probBloqueio;

	public CacheSimulRedeTO(SolutionONTD solution) {
		this.key = getKey(solution);
		dtHrCadastro = new Date();
		dtUltAcesso = new Date();
		qtdeAcessos = 0;
		custo = solution.getNetwork().getCost();
		bp = solution.getNetwork().getBp();
		custoTotal = solution.getObjective(1);
		probBloqueio = solution.getObjective(0);
	}

	public static String getKey(SolutionONTD solution) {
		StringBuffer sb = new StringBuffer();

		for (Integer gene : solution.getDecisionVariables()) {
			sb.append(gene);
		}

		return sb.toString();
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the dtHrCadastro
	 */
	public Date getDtHrCadastro() {
		return dtHrCadastro;
	}

	/**
	 * @param dtHrCadastro
	 *            the dtHrCadastro to set
	 */
	public void setDtHrCadastro(Date dtHrCadastro) {
		this.dtHrCadastro = dtHrCadastro;
	}

	/**
	 * @return the dtUltAcesso
	 */
	public Date getDtUltAcesso() {
		return dtUltAcesso;
	}

	/**
	 * @param dtUltAcesso
	 *            the dtUltAcesso to set
	 */
	public void setDtUltAcesso(Date dtUltAcesso) {
		this.dtUltAcesso = dtUltAcesso;
	}

	/**
	 * @return the qtdeAcessos
	 */
	public int getQtdeAcessos() {
		return qtdeAcessos;
	}

	/**
	 * @param qtdeAcessos
	 *            the qtdeAcessos to set
	 */
	public void setQtdeAcessos(int qtdeAcessos) {
		this.qtdeAcessos = qtdeAcessos;
	}

	/**
	 * @return the custoTotal
	 */
	public double getCustoTotal() {
		return custoTotal;
	}

	/**
	 * @param custoTotal
	 *            the custoTotal to set
	 */
	public synchronized void setCustoTotal(double custoTotal) {
		this.custoTotal = custoTotal;
	}

	/**
	 * @return the probBloqueio
	 */
	public double getProbBloqueio() {
		return probBloqueio;
	}

	/**
	 * @param probBloqueio
	 *            the probBloqueio to set
	 */
	public synchronized void setProbBloqueio(double probBloqueio) {
		this.probBloqueio = probBloqueio;
	}

	/**
	 * Método acessor para obter o valor do atributo custo.
	 * @return O valor de custo
	 */
	public NetworkCost getCusto() {
		return custo;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo custo.
	 * @param custo O novo valor de custo
	 */
	public void setCusto(NetworkCost custo) {
		this.custo = custo;
	}

	/**
	 * Método acessor para obter o valor do atributo bp.
	 * @return O valor de bp
	 */
	public NetworkBP getBp() {
		return bp;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo bp.
	 * @param bp O novo valor de bp
	 */
	public void setBp(NetworkBP bp) {
		this.bp = bp;
	}
}
