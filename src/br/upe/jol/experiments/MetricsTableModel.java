/**
 * 
 */
package br.upe.jol.experiments;

import javax.swing.table.AbstractTableModel;

import br.upe.jol.metaheuristics.TAlgorithm;
import br.upe.jol.metrics.TMetrics;

/**
 * .
 * 
 * @author Danilo Araújo
 */
public class MetricsTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	private String[] columnNames = { "Métrica", 
			TAlgorithm.NSGAII.toString(), 
			TAlgorithm.SPEA2.toString(),
			TAlgorithm.PESAII.toString(),
			TAlgorithm.PAES.toString(),
			TAlgorithm.MOPSO.toString(),
			TAlgorithm.MOPSOCDR.toString(),
			TAlgorithm.DNPSO.toString(),
			TAlgorithm.CSSMOPSO.toString()};
	private Object[][] data = { 
			{ TMetrics.HIPERVOLUME, Util.decimalFormat.format(0) , Util.decimalFormat.format(0) , Util.decimalFormat.format(0) , Util.decimalFormat.format(0) , Util.decimalFormat.format(0) , Util.decimalFormat.format(0) , Util.decimalFormat.format(0) , Util.decimalFormat.format(0) },
			{ TMetrics.COVERAGE, Util.decimalFormat.format(0), Util.decimalFormat.format(0) , Util.decimalFormat.format(0) , Util.decimalFormat.format(0) , Util.decimalFormat.format(0) , Util.decimalFormat.format(0) , Util.decimalFormat.format(0) , Util.decimalFormat.format(0)}, 
			{ TMetrics.SPACING, Util.decimalFormat.format(0), Util.decimalFormat.format(0) , Util.decimalFormat.format(0) , Util.decimalFormat.format(0) , Util.decimalFormat.format(0) , Util.decimalFormat.format(0) , Util.decimalFormat.format(0) , Util.decimalFormat.format(0)},
			{ TMetrics.SPREAD, Util.decimalFormat.format(0), Util.decimalFormat.format(0) , Util.decimalFormat.format(0) , Util.decimalFormat.format(0) , Util.decimalFormat.format(0) , Util.decimalFormat.format(0) , Util.decimalFormat.format(0) , Util.decimalFormat.format(0) }};

	public String getColumnName(int col) {
		return columnNames[col].toString();
	}

	public boolean isCellEditable(int row, int col) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return data.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int row, int col) {
		return data[row][col];
	}

	public void setValueAt(Object value, int row, int col) {
		data[row][col] = value;
		fireTableCellUpdated(row, col);
	}

	/**
	 * Método acessor para obter o valor de columnNames
	 *
	 * @return O valor de columnNames
	 */
	public String[] getColumnNames() {
		return columnNames;
	}

}
