/*
 * *****************************************************************************
 * Copyright (c) 2005
 * Propriedade do Laboratório de P&D da Unicap-Itautec
 * Todos os direitos reservados, com base nas leis brasileiras de 
 * copyright
 * Este software é confidencial e de propriedade intelectual do
 * Laboratório de P&D da Unicap-Itautec
 * ****************************************************************************
 * Projeto: SIAC - Sistema Itautec de Automação Comercial
 * Arquivo: NFSNetSprite.java
 * ****************************************************************************
 * Histórico de revisões
 * CR		Nome				Data		Descrição
 * ****************************************************************************
 * 064813-P	Danilo Araújo		12/12/2010		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.experiments;

import static br.upe.jol.problems.simton.SimtonProblem.NODES_POSITIONS;
import static br.upe.jol.problems.simton.SimtonProblem.SWITCHES_COSTS_AND_LABELS;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.NumberFormat;
import java.util.Locale;

import org.jibble.epsgraphics.EpsGraphics2D;

/**
 * TODO Descrição do tipo
 * 
 * @author Danilo Araújo
 * @since 12/12/2010
 */
public class NFSNetSprite extends Canvas {
	private static final long serialVersionUID = 1L;
	
	private Integer[] network;
	
	private static final int N_NODES = 14;
	
	private double scale = 3;
	
	private boolean selected = false;
	
	private int[] posCenterZoom;
	
	private int border;
	
	private double capex;
	
	private double pb;
	
	private int idSymbol;
	
	private static final NumberFormat pbFormatter = NumberFormat.getInstance(); 
	
	private static final NumberFormat capexFormatter = NumberFormat.getInstance(Locale.ENGLISH);
	
	static{
		pbFormatter.setMaximumFractionDigits(5);
		pbFormatter.setMinimumFractionDigits(5);
		capexFormatter.setMaximumFractionDigits(2);
		capexFormatter.setMinimumFractionDigits(2);
	}
	
	public NFSNetSprite() {
		super();
	}

	@Override
	public void update(Graphics g) {
		paint(g);
	}
	
	@Override
	public void paint(Graphics g) {
		draw(g);
	}
	
	/**
	 * Exporta o conteúdo para EPS
	 *
	 * @return String com o conteúdo em EPS
	 */
	public String getEPSString() {
		Graphics2D g = new EpsGraphics2D();
		// Set resolution to 300 dpi (0.24 = 72/300)
//		g.scale(0.24, 0.24);

		paint(g);

		return g.toString();
	}
	
	public void draw(Graphics g) {
		g.setFont(new Font("Aral", Font.PLAIN, 14));
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);	
		g.setColor(Color.white);
		g.fillRect(0, 0, (int) Math.round(166 * scale), (int) Math.round(100 * scale));
		g.setColor(Color.black);
		g.drawRect(0, 0, (int) Math.round(166 * scale), (int) Math.round(100 * scale));
		g.drawString("W = " + network[network.length - 1] + "; OXC = "
				+ capexFormatter.format(SWITCHES_COSTS_AND_LABELS[0][network[network.length - 2]]) + " m.u.; -"
				+ capexFormatter.format(SWITCHES_COSTS_AND_LABELS[1][network[network.length - 2]]) + " dB",
				5, posCenterZoom[1] + (int) Math.round(55 * scale));
		g.drawString("BP = " + pbFormatter.format(pb) + "; COST = " + capexFormatter.format(capex) + " u.m.",
				5, 15);
		for (int i = 0; i < 14; i++) {
			for (int j = i + 1; j < 14; j++) {
				if (network[getIndice(i, j)] != 0) {
					g.drawLine((int) Math.round(NODES_POSITIONS[i][0] * scale) + posCenterZoom[0], (int) Math
							.round(NODES_POSITIONS[i][1] * scale)
							+ posCenterZoom[1], (int) Math.round(NODES_POSITIONS[j][0] * scale) + posCenterZoom[0], (int) Math
							.round(NODES_POSITIONS[j][1] * scale)
							+ posCenterZoom[1]);
					g.setColor(Color.white);
					g.fillOval((int) Math.round(NODES_POSITIONS[i][0] * scale - 2 * scale) + posCenterZoom[0], (int) Math
							.round(NODES_POSITIONS[i][1] * scale - 2 * scale)
							+ posCenterZoom[1], (int) Math.round(4 * scale), (int) Math.round(4 * scale));
					g.fillOval((int) Math.round(NODES_POSITIONS[j][0] * scale - 2 * scale) + posCenterZoom[0], (int) Math
							.round(NODES_POSITIONS[j][1] * scale - 2 * scale)
							+ posCenterZoom[1], (int) Math.round(4 * scale), (int) Math.round(4 * scale));
					g.setColor(Color.black);
					g.drawOval((int) Math.round(NODES_POSITIONS[i][0] * scale - 2 * scale) + posCenterZoom[0], (int) Math
							.round(NODES_POSITIONS[i][1] * scale - 2 * scale)
							+ posCenterZoom[1], (int) Math.round(4 * scale), (int) Math.round(4 * scale));
					g.drawOval((int) Math.round(NODES_POSITIONS[j][0] * scale - 2 * scale) + posCenterZoom[0], (int) Math
							.round(NODES_POSITIONS[j][1] * scale - 2 * scale)
							+ posCenterZoom[1], (int) Math.round(4 * scale), (int) Math.round(4 * scale));
				}
			}
		}	
	}
	
	private int getIndice(int j, int i) {
		return (j + (N_NODES - 1) * i - i * (i + 1) / 2);
	}

	/**
	 * Método acessor para obter o valor do atributo network.
	 *
	 * @return Retorna o atributo network.
	 */
	public Integer[] getNetwork() {
		return network;
	}

	/**
	 * Método acessor para modificar o valor do atributo network.
	 *
	 * @param network O valor de network para setar.
	 */
	public void setNetwork(Object network) {
		this.network = (Integer[])network;
	}

	/**
	 * Método acessor para obter o valor do atributo scale.
	 *
	 * @return Retorna o atributo scale.
	 */
	public double getScale() {
		return scale;
	}

	/**
	 * Método acessor para modificar o valor do atributo scale.
	 *
	 * @param scale O valor de scale para setar.
	 */
	public void setScale(double scale) {
		this.scale = scale;
	}

	/**
	 * Método acessor para obter o valor do atributo serialversionuid.
	 *
	 * @return Retorna o atributo serialversionuid.
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * Método acessor para obter o valor do atributo nNodes.
	 *
	 * @return Retorna o atributo nNodes.
	 */
	public static int getnNodes() {
		return N_NODES;
	}

	/**
	 * Método acessor para obter o valor do atributo selected.
	 *
	 * @return Retorna o atributo selected.
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Método acessor para modificar o valor do atributo selected.
	 *
	 * @param selected O valor de selected para setar.
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * Método acessor para modificar o valor do atributo network.
	 *
	 * @param network O valor de network para setar.
	 */
	public void setNetwork(Integer[] network) {
		this.network = network;
	}

	/**
	 * Método acessor para obter o valor do atributo posCenterZoom.
	 *
	 * @return Retorna o atributo posCenterZoom.
	 */
	public int[] getPosCenterZoom() {
		return posCenterZoom;
	}

	/**
	 * Método acessor para modificar o valor do atributo posCenterZoom.
	 *
	 * @param posCenterZoom O valor de posCenterZoom para setar.
	 */
	public void setPosCenterZoom(int[] posCenterZoom) {
		this.posCenterZoom = posCenterZoom;
	}

	/**
	 * Método acessor para obter o valor do atributo border.
	 *
	 * @return Retorna o atributo border.
	 */
	public int getBorder() {
		return border;
	}

	/**
	 * Método acessor para modificar o valor do atributo border.
	 *
	 * @param border O valor de border para setar.
	 */
	public void setBorder(int border) {
		this.border = border;
	}

	/**
	 * Método acessor para obter o valor do atributo capex.
	 *
	 * @return Retorna o atributo capex.
	 */
	public double getCapex() {
		return capex;
	}

	/**
	 * Método acessor para modificar o valor do atributo capex.
	 *
	 * @param capex O valor de capex para setar.
	 */
	public void setCapex(double capex) {
		this.capex = capex;
	}

	/**
	 * Método acessor para obter o valor do atributo pb.
	 *
	 * @return Retorna o atributo pb.
	 */
	public double getPb() {
		return pb;
	}

	/**
	 * Método acessor para modificar o valor do atributo pb.
	 *
	 * @param pb O valor de pb para setar.
	 */
	public void setPb(double pb) {
		this.pb = pb;
	}

	/**
	 * Método acessor para obter o valor do atributo idSymbol.
	 *
	 * @return Retorna o atributo idSymbol.
	 */
	public int getIdSymbol() {
		return idSymbol;
	}

	/**
	 * Método acessor para modificar o valor do atributo idSymbol.
	 *
	 * @param idSymbol O valor de idSymbol para setar.
	 */
	public void setIdSymbol(int idSymbol) {
		this.idSymbol = idSymbol;
	}
}
