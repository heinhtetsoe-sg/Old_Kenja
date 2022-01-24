// kanji=漢字
/*
 * $Id: Database.java 56593 2017-10-22 14:18:00Z maeshiro $
 */
package nao_package.db;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * データベース接続。
 * Database.java JDBCによるデータベースへの接続と操作を行うための抽象クラスで、
 * ドライバ名称、接続用URL、ユーザー名、パスワードはサブクラスの コンストラクタ(DB2UDB.java参照)にて設定する。ただし
 * ユーザー名、パスワードについては<b>open(String user, String pass)</b> メソッドでも指定することができる。
 * @author takaesu
 */
public abstract class Database {
    /** JDBCドライバクラス名称 */
    protected String driver;

    /** データベースインスタンスの接続先を表すURL */
    protected String url;

    protected String dbName;

    /** インスタンス・オーナーのユーザー名(デフォルト値) */
    protected String user;

    /** インスタンス・オーナーのパスワード(デフォルト値) */
    protected String pass;

    /** データベースの接続を表すオブジェクト */
    public Connection conn = null;

    /** SQLステートメントを実行するためのオブジェクト */
    public Statement stmt = null;

    /** SQLステートメント(SELECT)の実行結果を表すオブジェクト */
    private ResultSet rs = null;

    /** デバッグモード */
    public boolean debug = false;

	/**
     * データベースをオープンし、Connectionオブジェクトを取得する。 
     * 接続時のユーザー名、パスワードはサブクラスのコンストラクタで
     * 指定したデフォルト値を使用する。
     * @return なし
     */
    public void open() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
    	
    	boolean hasConnection = false;
    	try {
			conn = getConnection(dbName);
			if (conn != null) {
			    hasConnection = hasConnection(conn);

			    if (!hasConnection) { // コンテキストが存在しJDBCリソースが用意されている(Connectionが取得できる)がDBに接続できない場合
	                System.out.println("DB接続が切断されている？ => もう一度接続を試みる :" + dbName);
	                // close(); // 接続が確立していないのにクローズしようとするとSQL例外発生 (ClosedConnectionException)
	                conn = getConnection(dbName);
	                hasConnection = hasConnection(conn);
			    }
			}
    	} catch (NamingException e) {
			System.out.println(e.getExplanation());
			e.printStackTrace();
    	}
    	
    	if (!hasConnection) {
    		open(user, pass);
    	}
    }
    
    private Connection getConnection(final String dbName) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, NamingException {
        String lookname = "jdbc/" + dbName.toLowerCase();
        System.out.println("lookup:" + lookname);
        // DataSourceを獲得
        Context initCtx = new InitialContext();
        DataSource ds0 = (DataSource) initCtx.lookup(lookname);
        System.out.println("DataSource:" + ds0.toString());
        // コネクションを獲得
        Connection conn = ds0.getConnection();
        return conn;
    }
    
    private static boolean hasConnection(final Connection conn) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean occurException = false;
        try {
            ps = conn.prepareStatement("values(current date) ");
            rs = ps.executeQuery();
        } catch (SQLException ex) {
            occurException = true;
        } finally {
            if (null != rs) try { rs.close(); } catch (Exception e) {}
            if (null != ps) try { ps.close(); } catch (Exception e) {}
        }
        return !occurException;
    }

    /**
     * データベースをオープンし、Connectionオブジェクトを取得する。
     * @param user インスタンス・オーナーのユーザー名
     * @param pass インスタンス・オーナーのパスワード
     * @return なし
     */
    public void open(String user, String pass) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        // JDBCドライバをDriverManagerに登録する
        DriverManager.registerDriver((Driver) Class.forName(driver).newInstance());

        // データベースに接続し、Connectionオブジェクトを取得する
        conn = DriverManager.getConnection(url, user, pass);

        // ---------------------------------------
        // 自動コミットモードはなしにしまっせー
        // ---------------------------------------
        conn.setAutoCommit(false); // 自動コミットモードをOFF。デフォルトはON
    }

	/**

     * SQL文(SELECT文)を実行する。
     * @param sql SQL文(SELECTのみ)
     * @return なし
     */
    public void query(String sql) throws SQLException {
        // データベースに接続されていなかったり、SQL文が無いときは終了
        if (conn == null || sql == null)
            return;

        // ResultSetオブジェクトをクローズする
        if (rs != null)
            rs.close();

        // Statementオブジェクトをクローズする
        if (stmt != null)
            stmt.close();

        // Statementオブジェクトを取得する
        stmt = conn.createStatement();

        // SQL文によりデータベースを照会(検索)してResultSetオブジェクトを取得する
        rs = stmt.executeQuery(sql);

        // takaesu
        if (debug == true) {
            System.out.println("QUERY:" + sql);
        }
    }

    /**
     * 実行結果が収められたResultSetオブジェクトを返す。
     * @return <b>query()</b>メソッドで照会(検索)を行った結果のオブジェクト
     */
    public ResultSet getResultSet() {
        // データベースの照会結果を保持するResultSetオブジェクトを返す
        return rs;
    }

    /**
     * データベースのクローズに伴い、ResultSet,Statement,Connection オブジェクトをクローズする。
     * @return なし
     */
    public void close() {
        try {
            // ResultSetオブジェクトをクローズする
            if (rs != null)
                rs.close();

            // Statementオブジェクトをクローズする
            if (stmt != null)
                stmt.close();

            // Connectionオブジェクトをクローズする
            if (conn != null)
                conn.close();
        } catch (SQLException ex) {
            // データベースへのアクセス時に何らかのエラーが発生した場合はメッセージを表示する
            System.out.println(">>>" + ex.getMessage());
        }
    }

    // takaesu
    // クラス名は ed が付くけど、メソッド名は prepare で ed が付かない
    public PreparedStatement prepareStatement(String s) throws SQLException {
        return conn.prepareStatement(s);
    }

    /*
     * * コミットの発行
     */
    public void commit() {
        try {
            conn.commit();
        } catch (Exception e) {
            System.out.println(">Connection#commit()>" + e);
        }
    }

    /**
     * @deprecated 標準出力を使っているので非推奨
     */
    public void rollback() {
        try {
            conn.rollback();
        } catch (Exception e) {
            System.out.println(">Connection#commit()>" + e);
        }
        System.out.println("rollbackしました。");
    }

    /**
     * ＩＮＳＥＲＴやＵＰＤＡＴＥ文で使用
     */
    public int executeUpdate(String s) throws SQLException {
        int sts = 0;
        try {
            sts = stmt.executeUpdate(s);
        } catch (Exception e) {
            System.out.println(">>>" + e + "<<<");
            e.printStackTrace();
            if (e instanceof SQLException)
                System.err.println("SQLException: " + e.getMessage() + ":" + ((SQLException) e).getSQLState());
        }
        return sts;
    }
}
