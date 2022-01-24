// kanji=漢字
/*
 * $Id: bd50256c644c47629ce400d2b0599b9b0dabd041 $
 *
 * 作成日: 2006/07/06 21:02:05 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ.detail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 学校値。
 * 学校固有のインスタンスを生成し、自身とする。
 * @author takaesu
 * @version $Id: bd50256c644c47629ce400d2b0599b9b0dabd041 $
 */
public class KNJDefineSchool implements KNJDefineCodeImp {
    private static final Log log = LogFactory.getLog(KNJDefineSchool.class);
    private static final String PROPERTIES = "KNJDefineSchool.properties";
    private static final String FQCN_KEY = "defineSchool";

    private KNJDefineSchool _impl;

    public String schoolmark;
    public int svfline;
    public int svfline2;
    public boolean usechairname;
    public String schooldiv;
    public int semesdiv;
    public int absent_cov;
    public int absent_cov_late;
    public boolean useschchrcountflg;
    public boolean usefromtoperiod;
    public boolean useabsencehigh;
    public int absencehighgrade[][];

    /**
     * コンストラクタ。
     * プロパティファイルを読込む
     */
    public KNJDefineSchool() {
        super();

        final Map properties = load(PROPERTIES);
        log.debug("プロパティファイル=" + properties);

        final String fqcn = (String) properties.get(FQCN_KEY);
        log.debug("fqcn=[" + fqcn + "]");
        if (null != fqcn) {
            invoke(fqcn);
        }
    }

    /**
     * コンストラクタ。
     * プロパティファイルを読込まない
     * @param dummy ダミー
     */
    protected KNJDefineSchool(final Object dummy) {
        super();
    }

    private void invoke(final String fqcn) {
        final Class clazz;
        try {
            clazz = Class.forName(fqcn);
        } catch (final ClassNotFoundException e) {
            log.error(fqcn + ":" + e.getClass().getName());
            return;
        }

        final Object instance;
        try {
            instance = clazz.newInstance();
        } catch (final InstantiationException e) {
            log.error(fqcn + ":" + e.getClass().getName());
            return;
        } catch (final IllegalAccessException e) {
            log.error(fqcn + ":" + e.getClass().getName());
            return;
        }

        _impl = (KNJDefineSchool) instance;
        setFields(_impl);
        ((DefineSchoolHolder) _impl).setParent(this);
    }

    private void setFields(final KNJDefineSchool instance) {
        if (null == instance) {
            return;
        }

        // 全てのフィールドに値を設定する
        this.schoolmark = instance.schoolmark;
        this.svfline = instance.svfline;
        this.svfline2 = instance.svfline2;
        this.usechairname = instance.usechairname;
        this.schooldiv = instance.schooldiv;
        this.semesdiv = instance.semesdiv;
        this.absent_cov = instance.absent_cov;
        this.absent_cov_late = instance.absent_cov_late;
        this.useschchrcountflg = instance.useschchrcountflg;
        this.usefromtoperiod = instance.usefromtoperiod;
        this.useabsencehigh = instance.useabsencehigh;
        this.absencehighgrade = instance.absencehighgrade;
    }

    private Map load(final String filename) {
        InputStream is = null;
        try {
            is = this.getClass().getClassLoader().getResourceAsStream(filename);
            if (null != is) {
                final Properties prop = new Properties();
                try {
                    prop.load(is);
                    log.debug("prop.size()=" + prop.size());
                    return prop;
                } catch (final IOException e) {
                    log.error("load", e);
                }
            }
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (final IOException e) {
                    log.error("close", e);
                }
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    public void Get_ClassCode(final DB2UDB db2) {
        if (null != _impl) {
            _impl.Get_ClassCode(db2);
        }
    }

    /** {@inheritDoc} */
    public void setSchoolCode(final DB2UDB db2, final String year) {
        if (null != _impl) {
            _impl.setSchoolCode(db2, year);
        }
    }

    /**
     *  各学校における定数等設定を実行
     */
    public void defineCode( DB2UDB db2, String year )
    {
        Get_ClassCode( db2 );           //学校区分および科目表示の設定値を取得
        setSchoolCode( db2, year );     //学校設定値を取得
    }
    
    /**
     * <pre>
     * 該当ＤＢにおけるテーブルとフィールドの存在チェックをします。
     * 引数のfieldがnullの場合、テーブルの存在チェックをします。
     * </pre>
     * @param db2
     * @param table テーブル名
     * @param field フィールド名
     * @return テーブルまたはフィールドが存在すればTrueを戻します。
     */
    public boolean hasTableHasField (
            final DB2UDB db2,
            final String table,
            final String field
    ) {
        String str;
        if (null == field) {
            str = "SELECT NAME FROM SYSIBM.SYSCOLUMNS WHERE TBNAME = '" + table + "'";
        } else {
            str = "SELECT NAME FROM SYSIBM.SYSCOLUMNS WHERE TBNAME = '" + table + "' AND NAME = '" + field + "'";
        }
        try {
            db2.query(str);
            ResultSet rs = db2.getResultSet();
            if (rs.next()) { return true; }
            rs.close();
        } catch (SQLException e) {
            log.error("SQLException", e);
        }
        return false;
    }

    //========================================================================

    protected interface DefineSchoolHolder {
        /**
         * 学校値の最上位インスタンスを設定する。
         * @param kds 学校値の最上位インスタンス
         */
        void setParent(KNJDefineSchool kds);
    } // DefineSchoolHolder
}
