package br.upe.jol.experiments;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.jibble.epsgraphics.EpsGraphics2D;

import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;

public class ParetoCanvas extends Canvas implements MouseMotionListener, MouseListener {
	private static final long serialVersionUID = 1L;
	
	private Problem<Integer> problem;
	
	private List<ParetoCanvasPointSprite> sprites = new Vector<ParetoCanvasPointSprite>();
	
	private SolutionSet<Double> solutionSet = new SolutionSet<Double>();
	
	private List<ParetoCanvasPointSprite> sprites2 = new Vector<ParetoCanvasPointSprite>();
	
	private SolutionSet<Double> solutionSet2 = new SolutionSet<Double>();
	
	private List<ParetoCanvasPointSprite> sprites3 = new Vector<ParetoCanvasPointSprite>();
	
	private SolutionSet<Double> solutionSet3 = new SolutionSet<Double>();
	
	private List<ParetoCanvasPointSprite> sprites4 = new Vector<ParetoCanvasPointSprite>();
	
	private SolutionSet<Double> solutionSet4 = new SolutionSet<Double>();
	
	private List<ParetoCanvasPointSprite> sprites5 = new Vector<ParetoCanvasPointSprite>();
	
	private SolutionSet<Double> solutionSet5 = new SolutionSet<Double>();
	
	private ParetoCanvasPointSprite selected;
	
	private SimulONDApplet parent;
	
	private boolean manterSel = false;

	private int[] center;

	private double scale = 1;

	private int width;

	private int height;

	private double lowerLimit[];

	private double upperLimit[];
	
	private Image img;
	
	private int bordas = 50;
	
	private double scaleFont = 1.0;
	
	private boolean escalaLogaritmicaX = true;
	
	private boolean escalaLogaritmicaY = false;
	
	private boolean ajustarDados = true;
	
	private static final NumberFormat formatter = NumberFormat.getInstance(Locale.ENGLISH);
	
	private static final int N_CASAS = 5;

	private static double FATOR_AJUSTE_LOG =  Math.pow(10, N_CASAS);
	
	private static final double LOG10SCALE = 1/Math.log(10);
	
	public static String TITULO_EIXO_X = "BLOCKING PROBABILITY";
	
	public static String TITULO_EIXO_Y = "COST";
	
	private static final Color COR_LINHA_EIXO = new Color(194, 194, 194);
	
	private ParetoCanvasPointSprite figuraLegenda0;
	
	private ParetoCanvasPointSprite figuraLegenda1;
	
	private ParetoCanvasPointSprite figuraLegenda2;
	
	private ParetoCanvasPointSprite figuraLegenda3;
	
	private ParetoCanvasPointSprite figuraLegenda4;
	
	public static double log10(double val) { return Math.log(val) * LOG10SCALE; }
    public static double exp10(double val) { return Math.exp(val / LOG10SCALE); }
    public static float flog10(double val) { return (float)log10(val); }
    public static float fexp10(double val) { return (float)exp10(val); }

    public void atualizarExperimento(SolutionSet<Double> solutions, int[] center, double scale, int width, int height, int id) {
    	switch (id) {
		case 0:
	    	solutionSet = solutions;
			break;
		case 1:
	    	solutionSet2 = solutions;
			break;
		case 2:
	    	solutionSet3 = solutions;
			break;	
		case 3:
	    	solutionSet4 = solutions;
			break;	
		case 4:
	    	solutionSet5 = solutions;
			break;			
		default:
			break;
		}
		formatter.setMinimumFractionDigits(2);
		formatter.setMaximumFractionDigits(5);
		this.center = center;
		this.scale = scale;
		this.width = width;
		this.height = height;
		ParetoCanvasPointSprite sprite = null;
		if (ajustarDados){
			lowerLimit = new double[problem.getNumberOfObjectives()];
			upperLimit = new double[lowerLimit.length];
		}else{
			lowerLimit = problem.getLowerLimitObjective();
			upperLimit = problem.getUpperLimitObjective();	
		}
		
		if (ajustarDados){
			for (int i = 0; i < upperLimit.length; i++) {
				upperLimit[i] = Double.MIN_VALUE;
				lowerLimit[i] = Double.MAX_VALUE;
			}
			for (Solution<Double> sol : solutionSet.getSolutionsList()) {
				for (int i = 0; i < upperLimit.length; i++) {
					if (sol.getObjective(i) > upperLimit[i]) {
						upperLimit[i] = sol.getObjective(i);
					}
					if (sol.getObjective(i) < lowerLimit[i]) {
						lowerLimit[i] = sol.getObjective(i);
					}
				}
			}
			for (Solution<Double> sol : solutionSet2.getSolutionsList()) {
				for (int i = 0; i < upperLimit.length; i++) {
					if (sol.getObjective(i) > upperLimit[i]) {
						upperLimit[i] = sol.getObjective(i);
					}
					if (sol.getObjective(i) < lowerLimit[i]) {
						lowerLimit[i] = sol.getObjective(i);
					}
				}
			}
			for (Solution<Double> sol : solutionSet3.getSolutionsList()) {
				for (int i = 0; i < upperLimit.length; i++) {
					if (sol.getObjective(i) > upperLimit[i]) {
						upperLimit[i] = sol.getObjective(i);
					}
					if (sol.getObjective(i) < lowerLimit[i]) {
						lowerLimit[i] = sol.getObjective(i);
					}
				}
			}
			for (Solution<Double> sol : solutionSet4.getSolutionsList()) {
				for (int i = 0; i < upperLimit.length; i++) {
					if (sol.getObjective(i) > upperLimit[i]) {
						upperLimit[i] = sol.getObjective(i);
					}
					if (sol.getObjective(i) < lowerLimit[i]) {
						lowerLimit[i] = sol.getObjective(i);
					}
				}
			}
			for (Solution<Double> sol : solutionSet5.getSolutionsList()) {
				for (int i = 0; i < upperLimit.length; i++) {
					if (sol.getObjective(i) > upperLimit[i]) {
						upperLimit[i] = sol.getObjective(i);
					}
					if (sol.getObjective(i) < lowerLimit[i]) {
						lowerLimit[i] = sol.getObjective(i);
					}
				}
			}
			upperLimit[1] += 80;
		}
		if (escalaLogaritmicaX){
			upperLimit[0] = flog10(FATOR_AJUSTE_LOG * 1);
			lowerLimit[0] = flog10(FATOR_AJUSTE_LOG * Math.pow(10, -N_CASAS));
		}
		if (escalaLogaritmicaY){
			upperLimit[1] = flog10(upperLimit[1]);
			lowerLimit[1] = flog10(lowerLimit[1]);
		}
		double scaleNets = 2.3;
        double rangeX = upperLimit[0] - lowerLimit[0];
        double pixels_per_log_unit = (width - 2 * bordas) / rangeX;

        int idSymbol = id;
        switch (id) {
		case 0:
			sprites.clear();
			for (Solution<Double> sol : solutions.getSolutionsList()) {
				sprite = new ParetoCanvasPointSprite();
				criarPonto(width, height, sprite, scaleNets, pixels_per_log_unit, idSymbol, sol);
				sprites.add(sprite);
			}
			break;
		case 1:
			sprites2.clear();
			for (Solution<Double> sol : solutionSet2.getSolutionsList()) {
				sprite = new ParetoCanvasPointSprite();
				criarPonto(width, height, sprite, scaleNets, pixels_per_log_unit, idSymbol, sol);
				sprites2.add(sprite);
			}
			break;
		case 2:
			sprites3.clear();
			for (Solution<Double> sol : solutionSet3.getSolutionsList()) {
				sprite = new ParetoCanvasPointSprite();
				criarPonto(width, height, sprite, scaleNets, pixels_per_log_unit, idSymbol, sol);
				sprites3.add(sprite);
			}
			break;	
		case 3:
			sprites4.clear();
			for (Solution<Double> sol : solutionSet4.getSolutionsList()) {
				sprite = new ParetoCanvasPointSprite();
				criarPonto(width, height, sprite, scaleNets, pixels_per_log_unit, idSymbol, sol);
				sprites4.add(sprite);
			}
			break;	
		case 4:
			sprites5.clear();
			for (Solution<Double> sol : solutionSet5.getSolutionsList()) {
				sprite = new ParetoCanvasPointSprite();
				criarPonto(width, height, sprite, scaleNets, pixels_per_log_unit, idSymbol, sol);
				sprites5.add(sprite);
			}
			break;			
		default:
			break;
		}
		
		img = createImage( width, height );
		selected = null;
		manterSel = false;
		repaint();
	}
    
    public void incrementarFontSize(){
    	if (scaleFont < 5){
    		scaleFont += 0.05;
    	}
    	repaint();
    }
    
    public void decrementarFontSize(){
    	if (scaleFont > 1){
    		scaleFont -= 0.05;
    	}
    	repaint();
    }
	
	private void criarPonto(int width, int height, ParetoCanvasPointSprite sprite, double scaleNets, double pixels_per_log_unit,
			int idSymbol, Solution<Double> sol) {
		int xCenter;
		int yCenter;
		if (escalaLogaritmicaX){
			xCenter = (int)((flog10(FATOR_AJUSTE_LOG * sol.getObjective(0)) - lowerLimit[0]) * pixels_per_log_unit + .5) + bordas;
		}else{
			xCenter = (int) Math.round((sol.getObjective(0) / upperLimit[0]) * (width - 2 * bordas)) + bordas;
		}
		if (escalaLogaritmicaY){
			yCenter = height - (int) Math.round((log10(sol.getObjective(1)))  / upperLimit[1] * (height - 2 * bordas)) - bordas;
		}else{
			yCenter = height - (int) Math.round((sol.getObjective(1) / upperLimit[1]) * (height - 2 * bordas)) - bordas;
		}
		sprite.setPosCenterZoom(new int[]{(int)(bordas + 100 * scaleNets),(int) (height - bordas - bordas/2 - scaleNets * 50)});
		sprite.setCenter(new int[] {xCenter, yCenter});
		sprite.setNetwork(sol.getDecisionVariables());
		sprite.setBorder(bordas);
		sprite.setScale(scaleNets);
		sprite.setIdSymbol(idSymbol);
		sprite.setCapex(sol.getObjective(1));
		sprite.setPb(sol.getObjective(0));
	}

	public ParetoCanvas(SolutionSet<Double> solutions, int[] center, double scale, int width, int height, SimulONDApplet parent) {
		this.parent = parent;
		this.problem = parent.getProblem();
    	solutionSet = solutions;
		formatter.setMinimumFractionDigits(2);
		formatter.setMaximumFractionDigits(5);
		this.center = center;
		this.scale = scale;
		this.width = width;
		this.height = height;
		atualizarExperimento(solutions, center, scale, width, height, 0);
		img = createImage( width, height );
		double scaleLegenda = 1.2;
		figuraLegenda0 = new ParetoCanvasPointSprite(center, scaleLegenda, true, bordas, 0);
		figuraLegenda1 = new ParetoCanvasPointSprite(center, scaleLegenda, true, bordas, 1);
		figuraLegenda2 = new ParetoCanvasPointSprite(center, scaleLegenda, true, bordas, 2);
		figuraLegenda3 = new ParetoCanvasPointSprite(center, scaleLegenda, true, bordas, 3);
		figuraLegenda4 = new ParetoCanvasPointSprite(center, scaleLegenda, true, bordas, 4);
		addMouseMotionListener(this);
		addMouseListener(this);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseMoved(e.getX(), e.getY());
	}

	public void mouseMoved(int x, int y) {
		if (!manterSel){
			if (selected != null){
				selected.setSelected(false);
				selected = null;
			}
			for (ParetoCanvasPointSprite sprite : sprites) {
				if (sprite.contains(x, y)) {
					sprite.setSelected(true);
					selected = sprite;
					break;
				} 
			}
			for (ParetoCanvasPointSprite sprite : sprites2) {
				if (sprite.contains(x, y)) {
					sprite.setSelected(true);
					selected = sprite;
					break;
				} 
			}
			for (ParetoCanvasPointSprite sprite : sprites3) {
				if (sprite.contains(x, y)) {
					sprite.setSelected(true);
					selected = sprite;
					break;
				} 
			}
			for (ParetoCanvasPointSprite sprite : sprites4) {
				if (sprite.contains(x, y)) {
					sprite.setSelected(true);
					selected = sprite;
					break;
				} 
			}
			for (ParetoCanvasPointSprite sprite : sprites5) {
				if (sprite.contains(x, y)) {
					sprite.setSelected(true);
					selected = sprite;
					break;
				} 
			}
			if (selected != null){
				parent.atualizarTopologia(selected.getNetwork(), selected.getPb(), selected.getCapex());
			}
		}
		repaint();
	}

	@Override
	public void update(Graphics g) {
		paint(g);
	}

	@Override
	public void paint(Graphics g) {
        double rangeX = upperLimit[0] - lowerLimit[0];
        double pixels_per_log_unit = (width - 2 * bordas) / rangeX;
        
		if (img == null){
			img = createImage(width, height);
		}
		Graphics g1 = img.getGraphics();
		((Graphics2D)g1).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g1.setFont(new Font ("Arial", Font.PLAIN , (int)(12 * scaleFont)));	
		g1.setColor(Color.white);
		g1.fillRect(center[0] - width / 2, center[1] - height / 2, width, height);
		g1.setColor(Color.black);
		g1.drawLine(bordas, bordas, bordas, height - bordas);
		g1.drawLine(bordas, height-bordas, width-bordas, height - bordas);
		int nDivs = 100;
		g1.setColor(COR_LINHA_EIXO);
		for (int i = 0; i <= nDivs; i++){
			
			g1.drawLine(bordas, height  - bordas - (int) Math.round((i*upperLimit[1]/nDivs)/ upperLimit[1] * (height - 2 * bordas)), 
					width - bordas, height - bordas - (int) Math.round((i*upperLimit[1]/nDivs)/ upperLimit[1] * (height - 2 * bordas)));

			if (escalaLogaritmicaX) {
				g1.drawLine(
						(int) ((flog10(FATOR_AJUSTE_LOG * Math.pow(10, -i / 2 / 10) * (i / 2 % 10)*.10) - lowerLimit[0])
								* pixels_per_log_unit + .5)
								+ bordas, bordas, (int) ((flog10(FATOR_AJUSTE_LOG * Math.pow(10, -i / 2 / 10)
								* (i / 2 % 10)*.10) - lowerLimit[0])
								* pixels_per_log_unit + .5)
								+ bordas, height - bordas);
			} else {
				g1.drawLine(bordas
						+ (int) Math.round((i * upperLimit[0] / nDivs) / upperLimit[0] * (width - 2 * bordas)), bordas,
						bordas + (int) Math.round((i * upperLimit[0] / nDivs) / upperLimit[0] * (width - 2 * bordas)),
						height - bordas);
			}
		}

		g1.setColor(Color.BLACK);
		for (int i = 0; i <= nDivs; i++){
			if (i % 10 == 0){
				g1.drawLine(bordas, height  - bordas - (int) Math.round((i*upperLimit[1]/nDivs)/ upperLimit[1] * (height - 2 * bordas)), 
						width - bordas, height - bordas - (int) Math.round((i*upperLimit[1]/nDivs)/ upperLimit[1] * (height - 2 * bordas)));
				g1.drawString(String.valueOf(Math.round(i*upperLimit[1]/nDivs)), width - bordas + 5, 
						height  - bordas - (int) Math.round((i*upperLimit[1]/nDivs)/ upperLimit[1] * (height - 2 * bordas)));

				if (escalaLogaritmicaX){
					g1.drawLine((int)((flog10(FATOR_AJUSTE_LOG * Math.pow(10, -i/10)) - lowerLimit[0]) * pixels_per_log_unit + .5) + bordas, 
							bordas, 
							(int)((flog10(FATOR_AJUSTE_LOG * Math.pow(10, -i/10)) - lowerLimit[0]) * pixels_per_log_unit + .5) + bordas, 
							height - bordas);
					g1.drawString(formatter.format(Math.pow(10, -i/10)), 
							(int)((flog10(FATOR_AJUSTE_LOG * Math.pow(10, -i/10)) - lowerLimit[0]) * pixels_per_log_unit + .5) + bordas, 
							height - bordas/2-10);
				}else{
					g1.drawLine(bordas + (int) Math.round((i*upperLimit[0]/nDivs)/ upperLimit[0] * (width - 2 * bordas)), bordas, 
							bordas + (int) Math.round((i*upperLimit[0]/nDivs)/ upperLimit[0] * (width - 2 * bordas)), height - bordas);
					g1.drawString(formatter.format(i*upperLimit[0]/nDivs), 
							bordas + (int) Math.round((i*upperLimit[0]/nDivs)/ upperLimit[0] * (width - 2 * bordas)), 
							height - bordas/2-10);	
				}
			}
		}
		int nExps = 0;
		if (!sprites.isEmpty()){
			for (ParetoCanvasPointSprite sprite : sprites) {
				sprite.draw(g1);
			}
			nExps++;
		}
		if (!sprites2.isEmpty()){
			for (ParetoCanvasPointSprite sprite : sprites2) {
				sprite.draw(g1);
			}
			nExps++;
		}
		if (!sprites3.isEmpty()){
			for (ParetoCanvasPointSprite sprite : sprites3) {
				sprite.draw(g1);
			}
			nExps++;
		}
		if (!sprites4.isEmpty()){
			for (ParetoCanvasPointSprite sprite : sprites4) {
				sprite.draw(g1);
			}
			nExps++;
		}
		if (!sprites5.isEmpty()){
			for (ParetoCanvasPointSprite sprite : sprites5) {
				sprite.draw(g1);
			}
			nExps++;
		}
		int tamanhoTexto = 80;
		if (nExps != 0){
			FontMetrics fm = g1.getFontMetrics();
			tamanhoTexto = fm.stringWidth(parent.getTxtTituloExperimento().getText());
			if (fm.stringWidth(parent.getTxtTituloExperimento2().getText()) > tamanhoTexto){
				tamanhoTexto = fm.stringWidth(parent.getTxtTituloExperimento2().getText());
			}
			if (fm.stringWidth(parent.getTxtTituloExperimento3().getText()) > tamanhoTexto){
				tamanhoTexto = fm.stringWidth(parent.getTxtTituloExperimento3().getText());
			}
			if (fm.stringWidth(parent.getTxtTituloExperimento4().getText()) > tamanhoTexto){
				tamanhoTexto = fm.stringWidth(parent.getTxtTituloExperimento4().getText());
			}
			if (fm.stringWidth(parent.getTxtTituloExperimento5().getText()) > tamanhoTexto){
				tamanhoTexto = fm.stringWidth(parent.getTxtTituloExperimento5().getText());
			}
			tamanhoTexto += 40;
			if (tamanhoTexto < 80){
				tamanhoTexto = 80;
			}
			g1.setColor(Color.white);
			g1.fillRect(center[0] + width / 2 - (tamanhoTexto + 60), bordas + 10, tamanhoTexto, 15 * nExps + 25);
			g1.setColor(Color.black);
			g1.drawRect(center[0] + width / 2 - (tamanhoTexto + 60), bordas + 10, tamanhoTexto, 15 * nExps + 25);
		}
		nExps = 0;
		if (!sprites.isEmpty()){
			nExps++;
			g1.drawString(parent.getTxtTituloExperimento().getText(), center[0] + width / 2 - (tamanhoTexto + 60) + 25, bordas + 20 + nExps * 15);	
			figuraLegenda0.setCenter(new int[]{center[0] + width / 2 - (tamanhoTexto + 60) + 15, bordas + 15 + nExps * 15});
			figuraLegenda0.draw(g1);
		}
		if (!sprites2.isEmpty()){
			nExps++;
			g1.setColor(Color.black);
			g1.drawString(parent.getTxtTituloExperimento2().getText(), center[0] + width / 2 - (tamanhoTexto + 60) + 25, bordas + 20 + nExps * 15);	
			figuraLegenda1.setCenter(new int[]{center[0] + width / 2 - (tamanhoTexto + 60) + 15, bordas + 15 + nExps * 15});
			figuraLegenda1.draw(g1);
		}
		if (!sprites3.isEmpty()){
			nExps++;
			g1.setColor(Color.black);
			g1.drawString(parent.getTxtTituloExperimento3().getText(), center[0] + width / 2 - (tamanhoTexto + 60) + 25, bordas + 20 + nExps * 15);	
			figuraLegenda2.setCenter(new int[]{center[0] + width / 2 - (tamanhoTexto + 60) + 15, bordas + 15 + nExps * 15});
			figuraLegenda2.draw(g1);
		}
		if (!sprites4.isEmpty()){
			nExps++;
			g1.setColor(Color.black);
			g1.drawString(parent.getTxtTituloExperimento4().getText(), center[0] + width / 2 - (tamanhoTexto + 60) + 25, bordas + 20 + nExps * 15);	
			figuraLegenda3.setCenter(new int[]{center[0] + width / 2 - (tamanhoTexto + 60) + 15, bordas + 15 + nExps * 15});
			figuraLegenda3.draw(g1);
		}
		if (!sprites5.isEmpty()){
			nExps++;
			g1.setColor(Color.black);
			g1.drawString(parent.getTxtTituloExperimento5().getText(), center[0] + width / 2 - (tamanhoTexto + 60) + 25, bordas + 20 + nExps * 15);	
			figuraLegenda4.setCenter(new int[]{center[0] + width / 2 - (tamanhoTexto + 60) + 15, bordas + 15 + nExps * 15});
			figuraLegenda4.draw(g1);
		}
		g1.setFont(new Font ("Arial", Font.BOLD , (int)(14 * scaleFont)));
		g1.setColor(Color.black);
		g1.drawString(TITULO_EIXO_X, center[0] - g1.getFontMetrics().stringWidth(TITULO_EIXO_X)/2, height - 15);
		int altura = g1.getFontMetrics().getHeight();
	    int posYIni = center[1] - altura * TITULO_EIXO_Y.length()/2;
	    int i = 0;
	    for (char c : TITULO_EIXO_Y.toCharArray()){
	    	g1.drawString(c+"", bordas/2 - 5, posYIni + i * altura);
	    	i++;
	    }
		g.drawImage( img, 0, 0, this );
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
	
	@Override
	public void mouseDragged(MouseEvent e) {
	}

	/**
	 * Método acessor para obter o valor do atributo sprites.
	 *
	 * @return Retorna o atributo sprites.
	 */
	public List<ParetoCanvasPointSprite> getSprites() {
		return sprites;
	}

	/**
	 * Método acessor para modificar o valor do atributo sprites.
	 *
	 * @param sprites O valor de sprites para setar.
	 */
	public void setSprites(List<ParetoCanvasPointSprite> sprites) {
		this.sprites = sprites;
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
	 * Método acessor para obter o valor do atributo width.
	 *
	 * @return Retorna o atributo width.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Método acessor para modificar o valor do atributo width.
	 *
	 * @param width O valor de width para setar.
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * Método acessor para obter o valor do atributo height.
	 *
	 * @return Retorna o atributo height.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Método acessor para modificar o valor do atributo height.
	 *
	 * @param height O valor de height para setar.
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * Método acessor para obter o valor do atributo lowerLimit.
	 *
	 * @return Retorna o atributo lowerLimit.
	 */
	public double[] getLowerLimit() {
		return lowerLimit;
	}

	/**
	 * Método acessor para modificar o valor do atributo lowerLimit.
	 *
	 * @param lowerLimit O valor de lowerLimit para setar.
	 */
	public void setLowerLimit(double[] lowerLimit) {
		this.lowerLimit = lowerLimit;
	}

	/**
	 * Método acessor para obter o valor do atributo upperLimit.
	 *
	 * @return Retorna o atributo upperLimit.
	 */
	public double[] getUpperLimit() {
		return upperLimit;
	}

	/**
	 * Método acessor para modificar o valor do atributo upperLimit.
	 *
	 * @param upperLimit O valor de upperLimit para setar.
	 */
	public void setUpperLimit(double[] upperLimit) {
		this.upperLimit = upperLimit;
	}

	/**
	 * Método acessor para obter o valor do atributo img.
	 *
	 * @return Retorna o atributo img.
	 */
	public Image getImg() {
		return img;
	}

	/**
	 * Método acessor para modificar o valor do atributo img.
	 *
	 * @param img O valor de img para setar.
	 */
	public void setImg(Image img) {
		this.img = img;
	}

	/**
	 * Método acessor para obter o valor do atributo bordas.
	 *
	 * @return Retorna o atributo bordas.
	 */
	public int getBordas() {
		return bordas;
	}

	/**
	 * Método acessor para modificar o valor do atributo bordas.
	 *
	 * @param bordas O valor de bordas para setar.
	 */
	public void setBordas(int bordas) {
		this.bordas = bordas;
	}

	/**
	 * Método acessor para obter o valor do atributo escalaLogaritmicaX.
	 *
	 * @return Retorna o atributo escalaLogaritmicaX.
	 */
	public boolean isEscalaLogaritmicaX() {
		return escalaLogaritmicaX;
	}

	/**
	 * Método acessor para modificar o valor do atributo escalaLogaritmicaX.
	 *
	 * @param escalaLogaritmicaX O valor de escalaLogaritmicaX para setar.
	 */
	public void setEscalaLogaritmicaX(boolean escalaLogaritmicaX) {
		this.escalaLogaritmicaX = escalaLogaritmicaX;
		atualizarExperimento(solutionSet, center, scale, width, height, 0);
		atualizarExperimento(solutionSet2, center, scale, width, height, 1);
		atualizarExperimento(solutionSet3, center, scale, width, height, 2);
		atualizarExperimento(solutionSet4, center, scale, width, height, 3);
		atualizarExperimento(solutionSet5, center, scale, width, height, 4);
		repaint();
	}

	/**
	 * Método acessor para obter o valor do atributo escalaLogaritmicaY.
	 *
	 * @return Retorna o atributo escalaLogaritmicaY.
	 */
	public boolean isEscalaLogaritmicaY() {
		return escalaLogaritmicaY;
	}

	/**
	 * Método acessor para modificar o valor do atributo escalaLogaritmicaY.
	 *
	 * @param escalaLogaritmicaY O valor de escalaLogaritmicaY para setar.
	 */
	public void setEscalaLogaritmicaY(boolean escalaLogaritmicaY) {
		this.escalaLogaritmicaY = escalaLogaritmicaY;
		atualizarExperimento(solutionSet, center, scale, width, height, 0);
		atualizarExperimento(solutionSet2, center, scale, width, height, 1);
		atualizarExperimento(solutionSet3, center, scale, width, height, 2);
		atualizarExperimento(solutionSet4, center, scale, width, height, 3);
		atualizarExperimento(solutionSet5, center, scale, width, height, 4);
		repaint();
	}

	/**
	 * Método acessor para obter o valor do atributo ajustarDados.
	 *
	 * @return Retorna o atributo ajustarDados.
	 */
	public boolean isAjustarDados() {
		return ajustarDados;
	}

	/**
	 * Método acessor para modificar o valor do atributo ajustarDados.
	 *
	 * @param ajustarDados O valor de ajustarDados para setar.
	 */
	public void setAjustarDados(boolean ajustarDados) {
		this.ajustarDados = ajustarDados;
		atualizarExperimento(solutionSet, center, scale, width, height, 0);
		atualizarExperimento(solutionSet2, center, scale, width, height, 1);
		atualizarExperimento(solutionSet3, center, scale, width, height, 2);
		atualizarExperimento(solutionSet4, center, scale, width, height, 3);
		atualizarExperimento(solutionSet5, center, scale, width, height, 4);
		repaint();
	}

	@Override
	public void mouseClicked(MouseEvent me) {
		if (manterSel){
			boolean clicouFora = true;
			for (ParetoCanvasPointSprite sprite : sprites) {
				if (sprite.contains(me.getX(), me.getY())) {
					clicouFora = false;
					break;
				} 
			}	
			for (ParetoCanvasPointSprite sprite : sprites2) {
				if (sprite.contains(me.getX(), me.getY())) {
					clicouFora = false;
					break;
				} 
			}	
			for (ParetoCanvasPointSprite sprite : sprites3) {
				if (sprite.contains(me.getX(), me.getY())) {
					clicouFora = false;
					break;
				} 
			}	
			for (ParetoCanvasPointSprite sprite : sprites4) {
				if (sprite.contains(me.getX(), me.getY())) {
					clicouFora = false;
					break;
				} 
			}	
			for (ParetoCanvasPointSprite sprite : sprites5) {
				if (sprite.contains(me.getX(), me.getY())) {
					clicouFora = false;
					break;
				} 
			}
			if (clicouFora){
				selected.setSelected(false);
				selected = null;
				manterSel = false;
			}
		}else{
			if (selected != null){
				manterSel = true;
			}else{
				manterSel = false;
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent me) {
	}

	@Override
	public void mouseExited(MouseEvent me) {
	}

	@Override
	public void mousePressed(MouseEvent me) {
	}

	@Override
	public void mouseReleased(MouseEvent v) {
	}
	/**
	 * Método acessor para obter o valor do atributo problem.
	 *
	 * @return Retorna o atributo problem.
	 */
	public Problem<Integer> getProblem() {
		return problem;
	}
	/**
	 * Método acessor para modificar o valor do atributo problem.
	 *
	 * @param problem O valor de problem para setar.
	 */
	public void setProblem(Problem<Integer> problem) {
		this.problem = problem;
	}
}
