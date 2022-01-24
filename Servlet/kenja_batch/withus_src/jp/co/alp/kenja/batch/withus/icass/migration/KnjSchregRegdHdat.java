// kanji=漢字
/*
 * $Id: KnjSchregRegdHdat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SCHREG_REGD_HDATを作る。
 * @author takaesu
 * @version $Id: KnjSchregRegdHdat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjSchregRegdHdat extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjSchregRegdHdat.class);

    public static final String ICASS_TABLE = "CLASS";
    public static final String ICASS_TABLE2 = "CLASS_TANNIN";

    public static final DecimalFormat _hrClassFormat = new DecimalFormat("000");
    public static final DecimalFormat _trCdFormat = new DecimalFormat("00000000");

    public KnjSchregRegdHdat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "学籍在籍ヘッダデータ"; }

    void migrate() throws SQLException {
        final List list = loadIcass();
        log.debug(ICASS_TABLE + "と" + ICASS_TABLE2 + "データ件数=" + list.size());

        try {
            saveKnj(list);
            _db2.commit();
        } catch (final SQLException e) {
            _db2.conn.rollback();
            log.fatal("更新処理中にエラー! rollback した。");
            throw e;
        }

        final List kenjaList = loadKenja();
        log.debug("賢者データ件数=" + kenjaList.size());
        try {
            saveKnj(kenjaList);
        } catch (final SQLException e) {
            _db2.conn.rollback();
            log.fatal("更新処理中にエラー! rollback した。");
            throw e;
        }
    }

    private List loadIcass() throws SQLException {
        final List rtn = new ArrayList();

        // SQL実行
        final List result;
        try {
            final String sql = getSql();
            result = (List) _runner.query(_db2.conn, sql, _handler);//TODO: データ量が多いので溢れてしまう。対策を考えろ!
        } catch (final SQLException e) {
            log.error("ICASSデータ取込みでエラー", e);
            throw e;
        }

        // 結果の処理
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();

            final int syear = ((Number) map.get("SYEAR")).intValue();
            final Number checkEyear = (Number) map.get("EYEAR");
            final int eyear = null == checkEyear ? syear : ((Number) map.get("EYEAR")).intValue();

            for (int yearCnt = syear; yearCnt <= eyear; yearCnt++) {
                final String semester = "1";
                final String grade = (String) map.get("GAKUSHU_KYOTEN_CODE");
                final String hrClass = _hrClassFormat.format(Integer.parseInt((String) map.get("CLASS_CODE")));
                final String key = new String(String.valueOf(yearCnt) + semester + grade + hrClass);

                SchregRegdHdat schregRegdHdat = null;
                for(Iterator ite=rtn.iterator(); ite.hasNext();) {
                    SchregRegdHdat temp = (SchregRegdHdat) ite.next();
                    if (key.equals(temp.getKey())) {
                        schregRegdHdat = temp;
                        break;
                    }
                }

                if (schregRegdHdat == null) {
                    schregRegdHdat = new SchregRegdHdat(map, yearCnt);
                    rtn.add(schregRegdHdat);
                } else {
                    schregRegdHdat.setTrCd(map);
                }
            }
        }
        return rtn;
    }

    /** SCHREG_REGD_DATにあってHDATにないクラスを作成する。*/
    private List loadKenja() throws SQLException {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MK AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     NOT EXISTS( ");
        stb.append("         SELECT ");
        stb.append("             'x' ");
        stb.append("         FROM ");
        stb.append("             SCHREG_REGD_HDAT T2 ");
        stb.append("         WHERE ");
        stb.append("             T1.YEAR = T2.YEAR ");
        stb.append("             AND T1.GRADE = T2.GRADE ");
        stb.append("             AND T1.HR_CLASS = T2.HR_CLASS ");
        stb.append("     ) ");
        stb.append(" GROUP BY ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     VALUE(L1.CLASS_NAME,'組不明') AS CLASS_NAME, ");
        stb.append("     VALUE(L1.CLASS_R_NAME,'組不明') AS CLASS_R_NAME ");
        stb.append(" FROM ");
        stb.append("     MK T1 ");
        stb.append(" LEFT JOIN CLASS L1 ON INT(L1.GAKUSHU_KYOTEN_CODE) = INT(T1.GRADE) ");
        stb.append("                   AND INT(L1.CLASS_CODE)          = INT(T1.HR_CLASS) ");

        // SQL実行
        final List result;
        try {
            final String sql = stb.toString();
            result = (List) _runner.query(_db2.conn, sql, _handler);//TODO: データ量が多いので溢れてしまう。対策を考えろ!
        } catch (final SQLException e) {
            log.error("ICASSデータ取込みでエラー", e);
            throw e;
        }

        final List upList = new ArrayList();
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();
            final String year = (String) map.get("YEAR");
            final String grade = (String) map.get("GRADE");
            final String hrClass = (String) map.get("HR_CLASS");
            final String hrName = (String) map.get("CLASS_NAME");
            final String hrNameabbv = (String) map.get("CLASS_R_NAME");
            SchregRegdHdat schregRegdHdat = new SchregRegdHdat(year, grade, hrClass, hrName, hrNameabbv);
            upList.add(schregRegdHdat);
        }

        return upList;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.GAKUSHU_KYOTEN_CODE, ");
        stb.append("     T1.CLASS_CODE, ");
        stb.append("     L1.CLASS_NAME, ");
        stb.append("     L1.CLASS_R_NAME, ");
        stb.append("     CASE WHEN MONTH(T1.CLASS_TANNIN_KIKAN_F) < 4 ");
        stb.append("          THEN YEAR(T1.CLASS_TANNIN_KIKAN_F) - 1 ");
        stb.append("          ELSE YEAR(T1.CLASS_TANNIN_KIKAN_F) ");
        stb.append("     END AS SYEAR, ");
        stb.append("     CASE WHEN MONTH(T1.CLASS_TANNIN_KIKAN_T) < 4 ");
        stb.append("          THEN YEAR(T1.CLASS_TANNIN_KIKAN_T) - 1 ");
        stb.append("          ELSE YEAR(T1.CLASS_TANNIN_KIKAN_T) ");
        stb.append("     END AS EYEAR, ");
        stb.append("     T1.GAKKO_KANKEISHA_NO ");
        stb.append(" FROM ");
        stb.append("     CLASS_TANNIN T1 ");
        stb.append("     LEFT JOIN CLASS L1 ON T1.GAKUSHU_KYOTEN_CODE = L1.GAKUSHU_KYOTEN_CODE ");
        stb.append("          AND T1.CLASS_CODE = L1.CLASS_CODE ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GAKUSHU_KYOTEN_CODE, ");
        stb.append("     T1.CLASS_CODE, ");
        stb.append("     SYEAR, ");
        stb.append("     EYEAR ");

        log.debug("sql=" + stb.toString());

        return stb.toString();
    }

    private class SchregRegdHdat {
        final String _year;
        final String _semester;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrNameabbv;
        final String _hrFaccd;
        String _trCd1;
        String _trCd2;
        String _trCd3;
        final String _subtrCd1;
        final String _subtrCd2;
        final String _subtrCd3;
        final Integer _classweeks;
        final Integer _classdays;

        public SchregRegdHdat(final Map map, final int year) {
            _year = String.valueOf(year);
            _semester = "1";
            _grade = (String) map.get("GAKUSHU_KYOTEN_CODE");
            final String hrClass = (String) map.get("CLASS_CODE");
            _hrClass = _hrClassFormat.format(Integer.parseInt(hrClass));
            _hrName = (String) map.get("CLASS_NAME");
            _hrNameabbv = (String) map.get("CLASS_R_NAME");
            _hrFaccd = "";  // TODU:未定
            final String trCd = (String) map.get("GAKKO_KANKEISHA_NO");
            _trCd1 = _trCdFormat.format(Integer.parseInt(trCd));
            _trCd2 = "";
            _trCd3 = "";
            _subtrCd1 = "";  // TODU:未定
            _subtrCd2 = "";  // TODU:未定
            _subtrCd3 = "";  // TODU:未定
            _classweeks = null; // TODU:未定
            _classdays = null;  // TODU:未定
        }

        public SchregRegdHdat(final String year,
                               final String grade,
                               final String hrClass,
                               final String hrName,
                               final String hrNameabbv) {
            _year = year;
            _semester = "1";
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrNameabbv = hrNameabbv;
            _hrFaccd = "";  // TODU:未定
            _trCd1 = "";
            _trCd2 = "";
            _trCd3 = "";
            _subtrCd1 = "";  // TODU:未定
            _subtrCd2 = "";  // TODU:未定
            _subtrCd3 = "";  // TODU:未定
            _classweeks = null; // TODU:未定
            _classdays = null;  // TODU:未定
        }

        public String getKey() {
            return _year + _semester + _grade + _hrClass;
        }
        
        public void setTrCd(Map map) {
            String trCd = _trCdFormat.format(Integer.parseInt((String) map.get("GAKKO_KANKEISHA_NO")));
            if ("".equals(_trCd1)) {
                _trCd1 = trCd;     
            } else if ("".equals(_trCd2)) {
                _trCd2 = trCd;
            } else if ("".equals(_trCd3)) {
                _trCd3 = trCd;
            }
        }
        
    }

    /*
     * [db2inst1@withus db2inst1]$ db2 describe table SCHREG_REGD_HDAT

        列名                           スキーマ  タイプ名           長さ    位取り NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        YEAR                           SYSIBM    VARCHAR                   4     0 いいえ
        SEMESTER                       SYSIBM    VARCHAR                   1     0 いいえ
        GRADE                          SYSIBM    VARCHAR                   3     0 いいえ
        HR_CLASS                       SYSIBM    VARCHAR                   3     0 いいえ
        HR_NAME                        SYSIBM    VARCHAR                  45     0 はい
        HR_NAMEABBV                    SYSIBM    VARCHAR                  15     0 はい
        HR_FACCD                       SYSIBM    VARCHAR                   4     0 はい
        TR_CD1                         SYSIBM    VARCHAR                   8     0 はい
        TR_CD2                         SYSIBM    VARCHAR                   8     0 はい
        TR_CD3                         SYSIBM    VARCHAR                   8     0 はい
        SUBTR_CD1                      SYSIBM    VARCHAR                   8     0 はい
        SUBTR_CD2                      SYSIBM    VARCHAR                   8     0 はい
        SUBTR_CD3                      SYSIBM    VARCHAR                   8     0 はい
        CLASSWEEKS                     SYSIBM    SMALLINT                  2     0 はい
        CLASSDAYS                      SYSIBM    SMALLINT                  2     0 はい
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
        UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
        
          17 レコードが選択されました。
     */
    private void saveKnj(final List list) throws SQLException {
        int totalCount = 0;
        ResultSet rs = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final SchregRegdHdat schregRegdHdat = (SchregRegdHdat) it.next();
            final String insSql = getInsertSql(schregRegdHdat);
            try{
                _db2.stmt.executeUpdate(insSql);
            } catch(SQLException e) {
                log.error("SQLException: " + e.getMessage()+ ":" + ((SQLException)e).getSQLState());
                throw e;
            }
            totalCount++;
        }
        DbUtils.closeQuietly(rs);
        log.warn("挿入件数=" + totalCount);
    }

    private String getInsertSql(final SchregRegdHdat schregRegdHdat) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" INSERT INTO SCHREG_REGD_HDAT ");
        stb.append(" VALUES( ");
        stb.append(" " + getInsertVal(schregRegdHdat._year) + ", ");
        stb.append(" " + getInsertVal(schregRegdHdat._semester) + ", ");
        stb.append(" " + getInsertVal(schregRegdHdat._grade) + ", ");
        stb.append(" " + getInsertVal(schregRegdHdat._hrClass) + ", ");
        stb.append(" " + getInsertVal(schregRegdHdat._hrName) + ", ");
        stb.append(" " + getInsertVal(schregRegdHdat._hrNameabbv) + ", ");
        stb.append(" " + getInsertVal(schregRegdHdat._hrFaccd) + ", ");
        stb.append(" " + getInsertVal(schregRegdHdat._trCd1) + ", ");
        stb.append(" " + getInsertVal(schregRegdHdat._trCd2) + ", ");
        stb.append(" " + getInsertVal(schregRegdHdat._trCd3) + ", ");
        stb.append(" " + getInsertVal(schregRegdHdat._subtrCd1) + ", ");
        stb.append(" " + getInsertVal(schregRegdHdat._subtrCd2) + ", ");
        stb.append(" " + getInsertVal(schregRegdHdat._subtrCd3) + ", ");
        stb.append(" " + getInsertVal(schregRegdHdat._classweeks) + ", ");
        stb.append(" " + getInsertVal(schregRegdHdat._classdays) + ", ");
        stb.append(" '" + Param.REGISTERCD + "', ");
        stb.append(" current timestamp ");
        stb.append(" ) ");

        return stb.toString();
    }
}
// eof

