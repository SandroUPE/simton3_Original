package br.upe.jol.experiments;

import java.awt.Color;
import java.awt.Graphics;
import java.text.NumberFormat;

public class ParetoCanvasPointSprite {
	
	private static final long serialVersionUID = 1L;
	
	private Integer[] network;
	
	private int[] center;
	
	private static final int N_NODES = 14;
	
	private double scale = 3;
	
	private boolean selected = false;
	
	private int[] posCenterZoom;
	
	private int border;
	
	private double capex;
	
	private double pb;
	
	private int idSymbol;
	
	private static final NumberFormat pbFormatter = NumberFormat.getInstance(); 
	
	private static final NumberFormat capexFormatter = NumberFormat.getInstance();
	
	private static final Color COR_0 = new Color(79, 129, 189);
	
	private static final Color COR_1 = new Color(192, 80, 77);
	
	private static final Color COR_2 = new Color(155, 187, 89);
	
	private static final Color COR_3 = new Color(158, 136, 184);
	
	private static final Color COR_4 = new Color(40, 40, 40);
	
	private static final Color COR_5 = new Color(185, 223, 233);
	
	static{
		pbFormatter.setMaximumFractionDigits(5);
		pbFormatter.setMinimumFractionDigits(5);
		capexFormatter.setMaximumFractionDigits(2);
		capexFormatter.setMinimumFractionDigits(2);
	}
	
	public ParetoCanvasPointSprite(){
		super();
	}
	
	public ParetoCanvasPointSprite(int[] center, double scale, boolean selected, int border, int idSymbol){
		this.center = center;
		this.scale = scale;
		this.selected = selected;
		this.border = border;
		this.idSymbol = idSymbol;
	}

	public boolean contains(int x, int y) {
		if (x < center[0] - (int) Math.round(2 * scale) || x > center[0] + (int) Math.round(2 * scale)){
			return false;
		}
		if (y < center[1] - (int) Math.round(2 * scale) || y > center[1] + (int) Math.round(2 * scale)){
			return false;
		}
		return true;
	}
	
	/**
	 * Desenha o símbolo 0
	 *
	 * @param g Contexto gráfico
	 * @param expandido Indica se desenha no tamanho normal ou expandido (destaque)
	 */
	public void drawSymbol0(Graphics g, boolean expandido) {
		g.setColor(COR_0);
		if (expandido) {
			g.fillPolygon(new int[]{center[0] - (int) Math.round(4 * scale), center[0], center[0] + (int) Math.round(4 * scale), 
					center[0]}, 
					new int[]{center[1], center[1] + (int) Math.round(4 * scale), center[1],
					center[1] - (int) Math.round(4 * scale)}, 4);
		} else {
			g.fillPolygon(new int[]{center[0] - (int) Math.round(2 * scale), center[0], center[0] + (int) Math.round(2 * scale), 
					center[0]}, 
					new int[]{center[1], center[1] + (int) Math.round(2 * scale), center[1],
					center[1] - (int) Math.round(2 * scale)}, 4);
		}
	}
	
	/**
	 * Desenha o símbolo 1
	 *
	 * @param g Contexto gráfico
	 * @param expandido Indica se desenha no tamanho normal ou expandido (destaque)
	 */
	public void drawSymbol1(Graphics g, boolean expandido) {
		g.setColor(COR_1);
		if (expandido) {
			g.fillRect(center[0] - (int) Math.round(3 * scale), center[1] - (int) Math.round(3 * scale), (int) Math
					.round(6 * scale), (int) Math.round(6 * scale));
		} else {
			g.fillRect(center[0] - (int) Math.round(2 * scale), center[1] - (int) Math.round(2 * scale), (int) Math
					.round(4 * scale), (int) Math.round(4 * scale));
		}
	}
	
	/**
	 * Desenha o símbolo 2
	 *
	 * @param g Contexto gráfico
	 * @param expandido Indica se desenha no tamanho normal ou expandido (destaque)
	 */
	public void drawSymbol2(Graphics g, boolean expandido) {
		g.setColor(COR_2);
		if (expandido) {
			g.fillPolygon(new int[]{center[0] - (int) Math.round(4 * scale), center[0] + (int) Math.round(4 * scale), 
					center[0]}, 
					new int[]{center[1] + (int) Math.round(4 * scale), center[1] + (int) Math.round(4 * scale), 
					center[1] - (int) Math.round(3 * scale)}, 3);
		} else {
			g.fillPolygon(new int[]{center[0] - (int) Math.round(2 * scale), center[0] + (int) Math.round(2 * scale), 
					center[0]}, 
					new int[]{center[1] + (int) Math.round(2 * scale), center[1] + (int) Math.round(2 * scale), 
					center[1] - (int) Math.round(2 * scale)}, 3);
		}
	}
	
	/**
	 * Desenha o símbolo 3
	 *
	 * @param g Contexto gráfico
	 * @param expandido Indica se desenha no tamanho normal ou expandido (destaque)
	 */
	public void drawSymbol3(Graphics g, boolean expandido) {
		g.setColor(COR_3);
		if (expandido) {
			g.drawLine(center[0] - (int) Math.round(3 * scale), center[1] - (int) Math.round(3 * scale), 
					center[0] + (int) Math.round(3 * scale), center[1] + (int) Math.round(3 * scale));
			g.drawLine(center[0] - (int) Math.round(3 * scale), center[1] + (int) Math.round(3 * scale), 
					center[0] + (int) Math.round(3 * scale), center[1] - (int) Math.round(3 * scale));
		} else {
			g.drawLine(center[0] - (int) Math.round(2 * scale), center[1] - (int) Math.round(2 * scale), 
					center[0] + (int) Math.round(2 * scale), center[1] + (int) Math.round(2 * scale));
			g.drawLine(center[0] - (int) Math.round(2 * scale), center[1] + (int) Math.round(2 * scale), 
					center[0] + (int) Math.round(2 * scale), center[1] - (int) Math.round(2 * scale));
		}
	}
	
	/**
	 * Desenha o símbolo 4
	 *
	 * @param g Contexto gráfico
	 * @param expandido Indica se desenha no tamanho normal ou expandido (destaque)
	 */
	public void drawSymbol4(Graphics g, boolean expandido) {
		g.setColor(COR_4);
		if (expandido) {
			g.drawLine(center[0] - (int) Math.round(4 * scale), center[1] - (int) Math.round(4 * scale), center[0]
					+ (int) Math.round(4 * scale), center[1] + (int) Math.round(4 * scale));
			g.drawLine(center[0] - (int) Math.round(4 * scale), center[1] + (int) Math.round(4 * scale), center[0]
					+ (int) Math.round(4 * scale), center[1] - (int) Math.round(4 * scale));
			g.drawLine(center[0], center[1] + (int) Math.round(4 * scale), center[0], center[1]
					- (int) Math.round(4 * scale));
		} else {
			g.drawLine(center[0] - (int) Math.round(2 * scale), center[1] - (int) Math.round(2 * scale), center[0]
					+ (int) Math.round(2 * scale), center[1] + (int) Math.round(2 * scale));
			g.drawLine(center[0] - (int) Math.round(2 * scale), center[1] + (int) Math.round(2 * scale), center[0]
					+ (int) Math.round(2 * scale), center[1] - (int) Math.round(2 * scale));
			g.drawLine(center[0], center[1] + (int) Math.round(2 * scale), center[0], center[1]
					- (int) Math.round(2 * scale));
		}
	}
	
	/**
	 * Desenha o símbolo 5
	 *
	 * @param g Contexto gráfico
	 * @param expandido Indica se desenha no tamanho normal ou expandido (destaque)
	 */
	public void drawSymbol5(Graphics g, boolean expandido) {
		g.setColor(COR_5);
		if (expandido) {
			g.fillRect(center[0] - (int) Math.round(3 * scale), center[1] - (int) Math.round(3 * scale), 
					(int) Math.round(6 * scale), (int) Math.round(6 * scale));
			g.setColor(Color.BLACK);
			g.drawRect(center[0] - (int) Math.round(3 * scale), center[1] - (int) Math.round(3 * scale), 
					(int) Math.round(6 * scale), (int) Math.round(6 * scale));
		} else {
			g.fillRect(center[0] - (int) Math.round(2 * scale), center[1] - (int) Math.round(2 * scale), 
					(int) Math.round(4 * scale), (int) Math.round(4 * scale));
			g.setColor(Color.BLACK);
			g.drawRect(center[0] - (int) Math.round(2 * scale), center[1] - (int) Math.round(2 * scale), 
					(int) Math.round(4 * scale), (int) Math.round(4 * scale));
		}
	}
	
	public void draw(Graphics g) {
		if (selected){
			switch (idSymbol) {
			case 0:
				drawSymbol0(g, true);
				break;
			case 1:
				drawSymbol1(g, true);
				break;
			case 2:
				drawSymbol2(g, true);
				break;
			case 3:
				drawSymbol3(g, true);
				break;
			case 4:
				drawSymbol4(g, true);
				break;
			default:
				break;
			}
		}else{
			switch (idSymbol) {
			case 0:
				drawSymbol0(g, false);
				break;
			case 1:
				drawSymbol1(g, false);
				break;
			case 2:
				drawSymbol2(g, false);
				break;
			case 3:
				drawSymbol3(g, false);
				break;
			case 4:
				drawSymbol4(g, false);
				break;
			default:
				break;
			}
		}
		
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
	 * Método acessor para obter o valor do atributo center.
	 *
	 * @return Retorna o atributo center.
	 */
	public int[] getCenter() {
		return center;
	}

	/**
	 * Método acessor para modificar o valor do atributo center.
	 *
	 * @param center O valor de center para setar.
	 */
	public void setCenter(int[] center) {
		this.center = center;
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
