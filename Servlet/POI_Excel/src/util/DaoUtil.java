package util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;

//データベース操作
// open close 以外は呼出し元でSQLExceptionを処理します
public class DaoUtil {
	private String jdbcLookup = "";
	private DataSource ds0 = null;
	private Connection conn = null; // データベースの接続を表すオブジェクト
	private Statement stmt = null; // SQLステートメントを実行するためのオブジェクト
	private ResultSet rs = null; // SQLステートメント(SELECT)の実行結果を表すオブジェクト
	private boolean sqllogging; // sql文をロギング
	private String errorMessage = "";
	Log log;

	// コンストラクタ
	// パラメータ：sql文をロギング
	public DaoUtil(boolean sqllogging, Log log) {
		this.sqllogging = sqllogging;
		this.log = log;
	}

	// データベースをオープンし、Connectionオブジェクトを取得する。
	public Statement open(String JDBC_LOOKUP) {
		stmt = null;
		if (jdbcLookup.length() == 0) {
			jdbcLookup = JDBC_LOOKUP;
		} else { // リトライ時の再オープン
			try {
				TimeUnit.SECONDS.sleep(3); // ３秒遅延
			} catch (InterruptedException e) {
				// TODO 自動生成された catch ブロック
				StackTraceLogging(e);
			}
		}
		// クローズを確認
		close();
		// オープン
		try {
			// DataSourceを獲得
			Context initCtx = new InitialContext();
			// DataSource ds0 = (DataSource)initCtx.lookup("jdbc/seiho");
			// //WebSphere用です。
			// DataSource ds0 =
			// (DataSource)initCtx.lookup("java:comp/env/jdbc/seiho");
			// //Tomcat用です。
			ds0 = (DataSource) initCtx.lookup(jdbcLookup);
			System.out.println("lookup:jdbc/seiho " + ds0.toString());
			// コネクションを獲得
			conn = ds0.getConnection();
			// 自動コミットモード
			// conn.setAutoCommit(false); // 自動コミットモードをOFF。デフォルトはON
			// Statementオブジェクトを取得する
			stmt = conn.createStatement();
		} catch (NamingException e) {
			StackTraceLogging(e);
		} catch (SQLException e) {
			StackTraceLogging(e);
		}
		return stmt;
	}

	/*****************************************************************
	 * SQL文(SELECTやUPDATE文)を実行する。
	 *
	 * @param sql
	 *            SQL文(SELECTのみ)
	 * @return なし
	 *****************************************************************/
	public ResultSet query(String sql) {
		this.errorMessage = "";
		if (sqllogging == true) {
			log.info(sql);
		}
		// データベースに接続されていなかったり、SQL文が無いときは終了
		if (conn == null || sql == null || stmt == null)
			return null;
		boolean retry = false;
		try {
			// ResultSetオブジェクトをクローズする
			if (rs != null) {
				rs.close();
				rs = null;
			}
			// SQL文によりデータベースを照会(検索)してResultSetオブジェクトを取得する
			rs = stmt.executeQuery(sql);
		} catch (SQLException e) {
			retry = sqlExceptionLogging(e);
			rs = null;
		}
		if (retry == true) { // データベース接続が切れていたのでリトライする
			if (open("") != null) {
				try {
					rs = stmt.executeQuery(sql);
					this.errorMessage = "";
				} catch (SQLException e) {
					StackTraceLogging(e);
					rs = null;
				}
			}
		}
		return rs;
	}

	// query結果取出し
	public boolean Next() throws SQLException {
			return rs != null ? rs.next() : false;
	}

	// query結果取出しを閉じる
	public void rsClose() throws SQLException {
		// ResultSetオブジェクトをクローズする
		if (rs != null) {
				rs.close();
			rs = null;
		}
	}

	// INSERTやDELETE文で使用
	public int executeUpdate(String sql) {
		this.errorMessage = "";
		if (sqllogging == true) {
			log.info(sql);
		}
		int sts = 0;
		// データベースに接続されていなかったり、SQL文が無いときは終了
		if (conn == null || sql == null || stmt == null)
			return 0;
		boolean retry = false;
		try {
			sts = stmt.executeUpdate(sql);
		} catch (SQLException e) {
			retry = sqlExceptionLogging(e);
		}
		if (retry == true) { // データベース接続が切れていたのでリトライする
			if (open("") != null) {
				try {
					sts = stmt.executeUpdate(sql);
				} catch (SQLException e) {
					StackTraceLogging(e);
				}
			}
		}

		return sts;
	}

	/*****************************************************************
	 * データベースのクローズに伴い、ResultSet,Statement,Connection オブジェクトをクローズする。
	 *
	 * @return なし
	 *****************************************************************/
	public void close() {
		// ResultSetオブジェクトをクローズする
		try {
			if (rs != null) {
				rs.close();
				rs = null;
			}
			// Statementオブジェクトをクローズする
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
			// Connectionオブジェクトをクローズする
			if (conn != null) {
				conn.close();
				conn = null;
			}
		} catch (SQLException e) {
			StackTraceLogging(e);
		} finally {
			ds0 = null;
		}
	}

	/*
	 * * コミットの発行
	 */
	public void commit() {
		try {
			conn.commit();
		} catch (SQLException e) {
			StackTraceLogging(e);
		}

	}

	// rollback
	public void rollback() {
		try {
			conn.rollback();
			log.warn("rollbackしました。");
		} catch (SQLException e) {
			StackTraceLogging(e);
		}

	}

	// 例外のスタックトレースをロギング
	public void StackTraceLogging(Exception ex) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		pw.flush();
		String trace = sw.toString();
		log.error(trace);
		//this.errorMessage = ex.getMessage();
		this.errorMessage = ex.toString();
	}

	// sql例外処理
	public boolean sqlExceptionLogging(SQLException e) {
		StackTraceLogging(e);
		int errorCode = e.getErrorCode();
		String sqlState = e.getSQLState();
		if ((errorCode == -4499) && (sqlState.equals("08001"))) {
			return true; // データベース接続が切れている
		} else {
			return false;
		}
	}

	//例外の情報を取出す
	public String getErrTrace() {
		return this.errorMessage;
	}
}
