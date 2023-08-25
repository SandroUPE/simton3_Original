package br.upe.jol.metaheuristics.nsgaii;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.LineNumberReader;

public class EstabilityStudy {
	public static double[][] NODES_POSITIONS = new double[][] { { -85.692672, 14.728428 }, { -86.51664, -31.619772 },
		{ -69.213312, 34.606656 }, { -51.189012, 0.51498 }, { -29.044872, 3.810852 }, { 4.531824, 46.142208 },
		{ 0, 0 }, { 28.3239, 3.192876 }, { 56.13282, 1.853928 }, { 41.610384, 30.177828 },
		{ 43.464312, -6.282756 }, { 67.97736, -7.003728 }, { 74.15712, 2.677896 }, { 66.741408, 8.033688 } };
	
	public static void main2(String[] args) {
		double posX1;
		double posX2;
		double posY1;
		double posY2;
		for (int i = 0; i < 14; i++){
			for (int j = i+1; j < 14; j++){
				posX1 = NODES_POSITIONS[i][0];
				posY1 = NODES_POSITIONS[i][1];
				posX2 = NODES_POSITIONS[j][0];
				posY2 = NODES_POSITIONS[j][1];
				System.out.printf("d(%s, %s) = %.3f\n", i, j, Math.sqrt((posX1 - posX2) * (posX1 - posX2) + (posY1 - posY2) * (posY1 - posY2)));
//				System.out.printf("d(%s, %s)\n", i+1, j+1, Math.sqrt((posX1 - posX2) * (posX1 - posX2) + (posY1 - posY2) * (posY1 - posY2)));
			}
		}
	}
	
	public static void main(String[] args) {
		int qtde = 400;
		FileReader fr = null;
		LineNumberReader lnr = null;
		String[][] values = new String[qtde][93];
		String line = null;
		try {
			FileWriter fw = new FileWriter("C:\\Temp\\exp1\\probabilities_compl.txt");
			for (int i = 1; i <= qtde; i++){
				fr = new FileReader("C:\\Temp\\exp1\\probabilities_" + i + ".txt");
				lnr = new LineNumberReader(fr);
				
				line = lnr.readLine();
				values[i-1] = line.split(" ");
				
				lnr.close();
				fr.close();
			}
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < values.length; i++){
				for (int j = 0; j < values[i].length; j++){
					System.out.print(values[i][j] + " ");
					sb.append(values[i][j] + " ");
				}
				sb.append("\n");
				System.out.println();
			}
			fw.write(sb.toString());
			fw.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
