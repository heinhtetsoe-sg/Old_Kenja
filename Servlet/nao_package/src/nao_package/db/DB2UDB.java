package nao_package.db;

/**
 * データベース接続。
 * @author takaesu
 * @version $Id: DB2UDB.java 56593 2017-10-22 14:18:00Z maeshiro $
 */
public class DB2UDB extends Database {
    /** アプリケーション用 */
    public static String TYPE2 = "COM.ibm.db2.jdbc.app.DB2Driver";

    /** アプレット用 */
    public static String TYPE3 = "COM.ibm.db2.jdbc.net.DB2Driver";

    /**
     * 引数つきのコンストラクタ。各パラメータを設定する。
     * @param dbName データベース・インスタンス名称
     * @param usr インスタンス・オーナーのユーザー名
     * @param pas インスタンス・オーナーのパスワード
     * @param mode TYPE2 OR TYPE3
     */
    public DB2UDB(String dbName, String usr, String pas, String type) {
    	this.dbName = dbName;
        if (isDB2V8()) {
            driver = "com.ibm.db2.jcc.DB2Driver";   // TYPE4
            if (TYPE2.equals(type)) {
                url = "jdbc:db2:" + dbName;
            } else {
                url = "jdbc:db2:" + db2v8type4(dbName);
            }
        } else {
            driver = type;
            url = "jdbc:db2:" + dbName;
        }
        user = usr;
        pass = pas;
        System.out.println("url=" + url);
    }

    /**
     * コンストラクタ。
     * サーブレット向け
     */
    public DB2UDB() {
        this("gakumudb", "db2inst1", "db2inst1", DB2UDB.TYPE2);
    }

    private static Boolean ISDB2V8 = null;

    private static synchronized boolean isDB2V8() {
        if (null == ISDB2V8) {
            String property = null;
            try {
                property = System.getProperty("nao_package.db.DB2UDB.isDB2V8");
            } catch (final RuntimeException e) {
                System.err.println(e.getMessage());
            }
            System.out.println("システムプロパティの値 nao_package.db.DB2UDB.isDB2V8=" + property);
            if (null == property) {
                ISDB2V8 = Boolean.FALSE;
                try {
                    Class.forName("com.ibm.db2.jcc.DB2Driver");
                    ISDB2V8 = Boolean.TRUE;
                } catch (final Throwable e) {
                    ;
                }
            } else {
                ISDB2V8 = property.equalsIgnoreCase("true") ? Boolean.TRUE : Boolean.FALSE;
            }

            System.out.println("ISDB2V8=" + ISDB2V8);
        }

        return ISDB2V8.booleanValue();
    }

    private static String db2v8type4(final String url) {
        // "//host/dbname" を "//host:50000/dbname" に置換する
        if (null != url && url.startsWith("//")) {
            final int pos = url.indexOf('/', 2);
            if (-1 == pos) {
                return url;
            }
            final String host = url.substring(0, pos);
            final String dbname = url.substring(pos, url.length());
            return host + ":50000" + dbname;
        } else {
            return url;
        }
    }
}
