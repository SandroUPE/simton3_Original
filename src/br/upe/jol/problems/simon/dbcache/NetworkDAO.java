/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: NetworkDAO.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	23/03/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.problems.simon.dbcache;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import br.cns.TMetric;
import br.cns.experiments.ComplexNetwork;
import br.upe.jol.problems.simton.SimtonProblem;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo
 * @since 23/03/2013
 */
public class NetworkDAO {
	private static final NetworkDAO instance = new NetworkDAO();
	// TODO: essa infra deveria estar em uma classe utilitária para conexão com
	// banco de dados e/ou arquivo xml :P
	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DATABASE_URL = "jdbc:mysql://localhost/bons";
	private static final String DATABASE_USER = "root";
	private static final String DATABASE_PSW = "1234";

	private Connection connection = null;
	private PreparedStatement statement = null;
	
	private PreparedStatement statementCons = null;

	/**
	 * Método acessor para obter o valor de instance
	 * 
	 * @return O valor de instance
	 */
	public static NetworkDAO getInstance() {
		return instance;
	}

	private NetworkDAO() {
		try {
			Class.forName(JDBC_DRIVER);
			connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PSW);

			statement = connection.prepareStatement("INSERT INTO network (adjacencyMatrix, wavelengths, oxc, cost, blockingProbability, algebraicConnectivity, naturalConnectivity, density, averageDegree, averagePathLength, clusteringCoefficient, diameter, entropy, ampsAdjacencyMatrix, dtGeneration, hash) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");

			statementCons = connection.prepareStatement("SELECT idNetwork from network WHERE hash = ?;");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void saveNetwork(SimtonProblem ontd, SolutionONTD solution) {
		ComplexNetwork cn = ontd.createComplexNetwork(solution);
		
		StringBuffer am = new StringBuffer();
		StringBuffer aam = new StringBuffer();
		StringBuffer hash = new StringBuffer();
		int count = 0;
		int idNetwork = 0;
		for (Integer value : solution.getDecisionVariables()){
			if (count < solution.getDecisionVariables().length - 2){
				if (value != 0){
					am.append(1).append(";");	
				}else{
					am.append(0).append(";");
				}
				aam.append(value).append(";");
			}
			hash.append(value).append(";");
			count++;
		}
		
		try {
			statementCons.setString(1, hash.toString());
			
			ResultSet rs = statementCons.executeQuery();
			
			if (rs.next()){
				idNetwork = rs.getInt(1);
			}
			
			if (idNetwork > 0){
				System.out.println("Rede existente!");
				return;
			}
			
			statement.setString(1, am.toString()); //adjacencyMatrix
			statement.setShort(2, solution.getDecisionVariables()[solution.getDecisionVariables().length-1].shortValue()); // wavelengths
			statement.setShort(3, solution.getDecisionVariables()[solution.getDecisionVariables().length-2].shortValue()); // oxc
			statement.setDouble(4, solution.getObjective(1)); //custo
			statement.setDouble(5, solution.getObjective(0)); //BP
			
			statement.setDouble(6, cn.getMetricValues().get(TMetric.ALGEBRAIC_CONNECTIVITY)); 
			statement.setDouble(7, cn.getMetricValues().get(TMetric.NATURAL_CONNECTIVITY));
			statement.setDouble(8, cn.getMetricValues().get(TMetric.DENSITY));
			statement.setDouble(9, cn.getMetricValues().get(TMetric.AVERAGE_DEGREE));
			statement.setDouble(10, cn.getMetricValues().get(TMetric.AVERAGE_PATH_LENGTH));
			statement.setDouble(11, cn.getMetricValues().get(TMetric.CLUSTERING_COEFFICIENT));
			statement.setDouble(12, cn.getMetricValues().get(TMetric.DIAMETER));
			statement.setDouble(13, cn.getMetricValues().get(TMetric.ENTROPY));

			statement.setString(14, aam.toString()); //adjacencyMatrix
			
			statement.setDate(15, new Date(System.currentTimeMillis()));
			
			statement.setString(16, hash.toString()); //adjacencyMatrix
			
			statement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void close(){
		try {
			statement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
