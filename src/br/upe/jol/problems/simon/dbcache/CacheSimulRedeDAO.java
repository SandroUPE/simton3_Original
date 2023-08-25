package br.upe.jol.problems.simon.dbcache;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CacheSimulRedeDAO {
	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DATABASE_URL = "jdbc:mysql://localhost/projeto_redes";
	private static final String DATABASE_USER = "root";
	private static final String DATABASE_PSW = "#1234#";
	private static Connection connection = null;
	private static PreparedStatement statementInserirCache = null;
	private static PreparedStatement statementAtualizarCache = null;
	private static PreparedStatement statementLerCache = null;

	public static synchronized void getCacheValue(CacheSimulRedeTO cache) {
		ResultSet rs = null;
		try {
			inicializarObjetosDB();

			statementLerCache.setString(1, cache.getKey().trim());

			rs = statementLerCache.executeQuery();

			if (rs.next()) {
				cache.custo.setLambda(rs.getInt(3) / 10000.0);
				cache.custo.setOpticalSwitch(rs.getInt(2) / 10000.0);
				cache.custo.setAmplifier(rs.getInt(4) / 10000.0);
				cache.custo.setdCFFiber(rs.getInt(5) / 10000.0);
				cache.custo.setsSMFFiber(rs.getInt(7) / 10000.0);
				cache.custo.setDeployment(rs.getInt(8) / 10000.0);
				cache.custoTotal = rs.getInt(1) / 10000.0;
				cache.probBloqueio = rs.getInt(6) / 10000.0;
				cache.dtHrCadastro = rs.getDate(9);
				cache.dtUltAcesso = rs.getDate(10);
				cache.qtdeAcessos = rs.getInt(11);

				statementAtualizarCache.setDate(1, new Date(new java.util.Date().getTime()));
				statementAtualizarCache.setInt(2, cache.qtdeAcessos + 1);
				statementAtualizarCache.setString(3, cache.getKey().trim());
				statementAtualizarCache.execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void inicializarObjetosDB() throws ClassNotFoundException, SQLException {
		if (connection == null) {
			Class.forName(JDBC_DRIVER);
			connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PSW);
		}
		if (statementLerCache == null) {
			statementLerCache = connection
					.prepareStatement("SELECT vlrCustoTotal, vlrCustoSwitch, vlrCustoLambda, vlrCustoAmp, vlrCustoDCFFibra, vlrProbBloqueio,  vlrCustoSSMFFibra, vlrCustoDeployment, dtHrCriacao, dtHrUltAcesso, qtdeAcessos FROM cachesimulrede WHERE hashCode = ?");
		}
		if (statementInserirCache == null) {
			statementInserirCache = connection
					.prepareStatement("INSERT INTO cachesimulrede (hashCode, vlrCustoTotal, vlrCustoSwitch, vlrCustoLambda, vlrCustoAmp, vlrCustoDCFFibra, vlrProbBloqueio,  vlrCustoSSMFFibra, vlrCustoDeployment, dtHrCriacao, dtHrUltAcesso, qtdeAcessos) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");
		}
		if (statementAtualizarCache == null) {
			statementAtualizarCache = connection
					.prepareStatement("UPDATE cachesimulrede SET dtHrUltAcesso = ?, qtdeAcessos = ? WHERE hashCode = ? ");
		}
	}

	public static synchronized void salvarCacheSolution(CacheSimulRedeTO cache) {
		try {
			statementInserirCache.setString(1, cache.getKey().trim());
			statementInserirCache.setInt(2, (int) (cache.custoTotal * 10000));
			statementInserirCache.setInt(3, (int) (cache.custo.getOpticalSwitch() * 10000));
			statementInserirCache.setInt(4, (int) (cache.custo.getLambda() * 10000));
			statementInserirCache.setInt(5, (int) (cache.custo.getAmplifier() * 10000));
			statementInserirCache.setInt(6, (int) (cache.custo.getdCFFiber() * 10000));
			statementInserirCache.setInt(7, (int) (cache.probBloqueio * 10000));
			statementInserirCache.setInt(8, (int) (cache.custo.getsSMFFiber() * 10000));
			statementInserirCache.setInt(9, (int) (cache.custo.getDeployment() * 10000));
			statementInserirCache.setDate(10, new Date(new java.util.Date().getTime()));
			statementInserirCache.setDate(11, new Date(new java.util.Date().getTime()));
			statementInserirCache.setInt(12, 1);
			statementInserirCache.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
