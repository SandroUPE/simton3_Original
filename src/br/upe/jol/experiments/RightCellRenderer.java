/**
 * 
 */
package br.upe.jol.experiments;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * .
 * 
 * @author Danilo Araújo
 */
public class RightCellRenderer extends DefaultTableCellRenderer implements
		TableCellRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		double valor = 0;
		JLabel renderedLabel = (JLabel) super.getTableCellRendererComponent(
				table, value, isSelected, hasFocus, row, column);
		renderedLabel.setHorizontalAlignment(RIGHT);

		String strValor = value == null ? "" : value.toString();
		if (strValor.trim().length() != 0) {
			try {
				valor = Util.decimalFormat.parse(strValor).doubleValue();
			} catch (Exception e) {
			}
			if (valor < 0) {
				renderedLabel.setForeground(Color.RED);
			} else {
				renderedLabel.setForeground(Color.BLACK);
			}

		}

		return renderedLabel;
	}

}
