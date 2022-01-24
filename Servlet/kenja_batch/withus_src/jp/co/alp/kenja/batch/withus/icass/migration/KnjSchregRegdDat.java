// kanji=漢字
/*
 * $Id: KnjSchregRegdDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SCHREG_REGD_DATを作る。
 * @author takaesu
 * @version $Id: KnjSchregRegdDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjSchregRegdDat extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjSchregRegdDat.class);

    public static final String ICASS_TABLE = "SEITO_SHOZOKU";
    public static final DecimalFormat _hrClassFormat = new DecimalFormat("000");
    public static final DecimalFormat _attendNoFormat = new DecimalFormat("000");
    public static final DecimalFormat _courseCode = new DecimalFormat("000");
    public static final DecimalFormat _majorCd = new DecimalFormat("000");
    public static final DecimalFormat _annuaL = new DecimalFormat("00");
    public static final DecimalFormat _partnerCd = new DecimalFormat("000");

    public KnjSchregRegdDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "学籍在籍データ"; }

    void migrate() throws SQLException { 
        
        for (int jouken=0; jouken<2; jouken++) {
            // データが多いので2回に分割する
            // jouken==0 : 志願者連番が5000未満,  jouken==1 :志願者連番が5000以上 
            final Map schregRegdMap = loadIcass(jouken);
            log.debug(ICASS_TABLE + "データ件数=" + schregRegdMap.size());

            try {
                saveKnj(schregRegdMap);
            } catch (final SQLException e) {
                _db2.conn.rollback();
                log.fatal("更新処理中にエラー! rollback した。");
                throw e;
            }
        }
    }

    private Map loadIcass(int jouken) throws SQLException {

        // SQL実行
        final List result;
        try {
            final String sql = getSql(jouken);
            result = (List) _runner.query(_db2.conn, sql, _handler);//TODO: データ量が多いので溢れてしまう。対策を考えろ!
        } catch (final SQLException e) {
            log.error("ICASSデータ取込みでエラー", e);
            throw e;
        }

        // 結果の処理
        final Map schregRegdMap = new HashMap();
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();

            final String shiganshaRenban = (String) map.get("SHIGANSHA_RENBAN");

            SchregRegdDat schregRegdDat = null;
            if (!schregRegdMap.containsKey(shiganshaRenban)) {
                schregRegdDat = new SchregRegdDat(map);
            } else {
                schregRegdDat = (SchregRegdDat) schregRegdMap.get(shiganshaRenban);
                schregRegdDat.setRegdData(map);
            }
            schregRegdMap.put(shiganshaRenban, schregRegdDat);
        }
        return schregRegdMap;
    }

    private String getSql(int jouken) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH IDO_MAX AS (  ");
        stb.append(" SELECT  ");
        stb.append("     SHIGANSHA_RENBAN,  ");
        stb.append("     MAX(GAKUSEKI_JOTAI_KAISHI_NENGAPPI) AS NENGAPPI,  ");
        stb.append("     CASE WHEN MONTH(MAX(GAKUSEKI_JOTAI_KAISHI_NENGAPPI)) > 3  ");
        stb.append("          THEN YEAR(MAX(GAKUSEKI_JOTAI_KAISHI_NENGAPPI))  ");
        stb.append("          ELSE YEAR(MAX(GAKUSEKI_JOTAI_KAISHI_NENGAPPI)) - 1  ");
        stb.append("     END AS E_YEAR  ");
        stb.append(" FROM  ");
        stb.append("     SEITO_GAKUSEKI_IDO_RIREKI  ");
        stb.append(" WHERE  ");
        stb.append("     GAKUSEKI_JOTAI_CODE NOT IN ('1', '2', '4')  ");
        stb.append(" GROUP BY  ");
        stb.append("     SHIGANSHA_RENBAN  ");
        stb.append(" ), SEITO_INFO AS (  ");
        stb.append(" SELECT  ");
        stb.append("     T1.SHIGANSHA_RENBAN,  ");
        stb.append("     T1.NYUUGAKU_NENJI,  ");
        stb.append("     T1.ZAIGAKU_NENJI,  ");
        stb.append("     T1.NYUGAKU_KEITAI_CODE,  ");
        stb.append("     CASE WHEN L1.E_YEAR IS NOT NULL  ");
        stb.append("          THEN L1.E_YEAR  ");
        stb.append("          ELSE CASE WHEN VALUE(T1.SOTSUGYO_NENGAPPI, '') <> '' AND YEAR(T1.SOTSUGYO_NENGAPPI) < 2009  ");
        stb.append("                    THEN YEAR(T1.SOTSUGYO_NENGAPPI)  ");
        stb.append("                    ELSE 2008  ");
        stb.append("               END  ");
        stb.append("     END AS E_YEAR  ");
        stb.append(" FROM  ");
        stb.append("     SEITO T1  ");
        stb.append("     LEFT JOIN IDO_MAX L1 ON T1.SHIGANSHA_RENBAN = L1.SHIGANSHA_RENBAN  ");
        stb.append(" WHERE  ");
        stb.append("     VALUE(T1.SEITO_NO, '') <> ''  ");
        stb.append(" ), CODE_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     ROW_NUMBER() OVER() AS PARTNER_CD, ");
        stb.append("     GAKUSHU_KYOTEN_CODE ");
        stb.append(" FROM  ");
        stb.append("     GAKUSHU_KYOTEN ");
        stb.append(" WHERE  ");
        stb.append("     GAKUSHU_KYOTEN_CODE > '050'  ");
        stb.append(" GROUP BY ");
        stb.append("     GAKUSHU_KYOTEN_CODE,  ");
        stb.append("     GAKUSHU_KYOTEN_NAME,  ");
        stb.append("     YUBIN_NO,  ");
        stb.append("     TODOFUKEN_NO,  ");
        stb.append("     ADDRESS1,  ");
        stb.append("     ADDRESS2,  ");
        stb.append("     TEL_NO  ");
        stb.append(" ORDER BY ");
        stb.append("     GAKUSHU_KYOTEN_CODE ");
        stb.append(" ) ");
        stb.append(" SELECT  ");
        stb.append("     T1.SHIGANSHA_RENBAN,  ");
        stb.append("     T1.KATEI_CODE,  ");
        stb.append("     T1.GAKKA_CODE,  ");
        stb.append("     T1.SENKO_CODE,  ");
        stb.append("     T1.COURSE_CODE,  ");
        stb.append("     T1.SHOZOKU_KAISHI_NENGAPPI,  ");
        stb.append("     CASE WHEN MONTH(T1.SHOZOKU_KAISHI_NENGAPPI) > 3  ");
        stb.append("          THEN YEAR(T1.SHOZOKU_KAISHI_NENGAPPI)  ");
        stb.append("          ELSE YEAR(T1.SHOZOKU_KAISHI_NENGAPPI) - 1  ");
        stb.append("     END AS S_YEAR,  ");
        stb.append("     CASE WHEN VALUE(T1.SHOZOKU_SHURYO_NENGAPPI, '') <> ''  ");
        stb.append("          THEN CASE WHEN MONTH(T1.SHOZOKU_SHURYO_NENGAPPI) > 3  ");
        stb.append("                    THEN YEAR(T1.SHOZOKU_SHURYO_NENGAPPI)  ");
        stb.append("                    ELSE YEAR(T1.SHOZOKU_SHURYO_NENGAPPI) - 1  ");
        stb.append("               END  ");
        stb.append("          ELSE CASE WHEN L1.E_YEAR IS NOT NULL  ");
        stb.append("                    THEN L1.E_YEAR  ");
        stb.append("                    ELSE 2008  ");
        stb.append("               END  ");
        stb.append("     END AS E_YEAR,  ");
        stb.append("     T1.SHOZOKU_SHURYO_NENGAPPI,  ");
        stb.append("     L2.PARTNER_CD, ");
        stb.append("     T1.GAKUSHU_KYOTEN_CODE,  ");
        stb.append("     VALUE(T1.CLASS_CODE, '999') AS CLASS_CODE,  ");
        stb.append("     T1.SHUSSEKI_NO,  ");
        stb.append("     CASE WHEN L1.NYUGAKU_KEITAI_CODE = '9'  ");
        stb.append("          THEN '2'  ");
        stb.append("          ELSE '1'  ");
        stb.append("     END AS COURSE_DIV,  ");
        stb.append("     VALUE(L1.NYUUGAKU_NENJI, '1') AS NYUUGAKU_NENJI, ");
        stb.append("     VALUE(L1.ZAIGAKU_NENJI, '1') AS ZAIGAKU_NENJI ");
        stb.append(" FROM  ");
        stb.append("     SEITO_SHOZOKU T1  ");
        stb.append("     INNER JOIN SEITO_INFO L1 ON T1.SHIGANSHA_RENBAN = L1.SHIGANSHA_RENBAN  ");
        stb.append("     LEFT JOIN CODE_T L2 ON T1.GAKUSHU_KYOTEN_CODE = L2.GAKUSHU_KYOTEN_CODE ");
        stb.append(" WHERE ");
        if (jouken == 0) {
            stb.append("     T1.SHIGANSHA_RENBAN < '5000' "); 
        } else if (jouken == 1) {
            stb.append("     T1.SHIGANSHA_RENBAN >= '5000' "); 
        }
        stb.append(" ORDER BY  ");
        stb.append("     INT(T1.SHIGANSHA_RENBAN),  ");
        stb.append("     T1.SHOZOKU_KAISHI_NENGAPPI  ");
        log.debug("sql=" + stb.toString());

        return stb.toString();
    }

    private class SchregRegdDat {
        final String _shiganshaRenban;
        final String _schregno;
        final String _semester;
        final String _annual;
        final String _zaisekiAnnual;
        final String _attendNo;
        final String _attach1;
        final String _attach2;
        final String _seatRow;
        final String _seatCol;
        final String _courseDiv;
        Map _regdMap = new TreeMap();

        public SchregRegdDat(final Map map) {
            _shiganshaRenban = (String) map.get("SHIGANSHA_RENBAN");
            _schregno = _param.getSchregno(_shiganshaRenban);
            _semester = "1";
//            final String attendNo = (String) map.get("SHUSSEKI_NO");
//            _attendNo = null != attendNo ? _attendNoFormat.format(Integer.parseInt(attendNo)) : attendNo;
            _attendNo = "001";
            final String annual = (String) map.get("NYUUGAKU_NENJI");
            _annual = _annuaL.format(Integer.parseInt(annual));
            final String zaisekiAnnual = (String) map.get("ZAIGAKU_NENJI");
            _zaisekiAnnual = _annuaL.format(Integer.parseInt(zaisekiAnnual));
            _attach1 = "";    // TODO:未定
            _attach2 = "";    // TODO:未定
            _seatRow = "";    // TODO:未定
            _seatCol = "";    // TODO:未定
            _courseDiv = (String) map.get("COURSE_DIV");
            setRegdData(map);

        }

        public boolean isMaxData(final String year) {
            int maxYear = 0;
            for (final Iterator iter = _regdMap.keySet().iterator(); iter.hasNext();) {
                final String key = (String) iter.next();
                final int intYear = Integer.parseInt(key);
                if (maxYear < intYear) {
                    maxYear = intYear;
                }
            }
            return year.equals(String.valueOf(maxYear));
        }

        /**
         * @param map
         */
        public void setRegdData(Map map) {
            final int syear = ((Number) map.get("S_YEAR")).intValue();
            final int eyear = ((Number) map.get("E_YEAR")).intValue();
            for (int yearCnt = syear; yearCnt <= eyear; yearCnt++) {

                RegdData regdData = new RegdData(map, yearCnt);
                _regdMap.put(String.valueOf(yearCnt), regdData);

            }
        }
        
    }

    private class RegdData {
        final String _year;
        final String _grade;
        final String _hrClass;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final String _studentDiv;
        final String _partnercd;
        public RegdData(final Map map, final int yearCnt) {
            _year = String.valueOf(yearCnt);
            final String belonging = (String) map.get("GAKUSHU_KYOTEN_CODE");
            _grade = belonging;
            final String hrClass = (String) map.get("CLASS_CODE");
            _hrClass = null != hrClass ? _hrClassFormat.format(Integer.parseInt(hrClass)) : hrClass;
            final String courseCd = (String) map.get("KATEI_CODE");
            _coursecd = courseCd;
            final String majorcd = (String) map.get("GAKKA_CODE");
            _majorcd = _majorCd.format(Integer.parseInt(majorcd));
            final String courseMst = (courseCd + majorcd + (String) map.get("SENKO_CODE") + (String) map.get("COURSE_CODE"));
            _coursecode = _param.getCourseCode(belonging, courseMst);
            _studentDiv = _param.getStudentDiv(belonging, courseMst);
            final String partnercd;
            if (map.get("PARTNER_CD") != null) {
                partnercd = ((Long) map.get("PARTNER_CD")).toString();
                _partnercd = _partnerCd.format(Integer.parseInt(partnercd));
            } else {
                _partnercd = null;
            }
            
        }
    }
    /*
     * [db2inst1@withus db2inst1]$ db2 describe table SCHREG_REGD_DAT

        列名                           スキーマ  タイプ名           長さ    位取り NULL
        ------------------------------ --------- ------------------ -------- ----- ------
        SCHREGNO                       SYSIBM    VARCHAR                   8     0 いいえ
        YEAR                           SYSIBM    VARCHAR                   4     0 いいえ
        SEMESTER                       SYSIBM    VARCHAR                   1     0 いいえ
        GRADE                          SYSIBM    VARCHAR                   3     0 はい
        HR_CLASS                       SYSIBM    VARCHAR                   3     0 はい
        ATTENDNO                       SYSIBM    VARCHAR                   3     0 はい
        ANNUAL                         SYSIBM    VARCHAR                   2     0 はい
        ATTACH1                        SYSIBM    VARCHAR                   8     0 はい
        ATTACH2                        SYSIBM    VARCHAR                   8     0 はい
        PARTNER_CD                     SYSIBM    VARCHAR                   4     0 はい
        SEAT_ROW                       SYSIBM    VARCHAR                   2     0 はい
        SEAT_COL                       SYSIBM    VARCHAR                   2     0 はい
        COURSECD                       SYSIBM    VARCHAR                   1     0 はい
        MAJORCD                        SYSIBM    VARCHAR                   3     0 はい
        COURSECODE                     SYSIBM    VARCHAR                   4     0 はい
        COURSE_DIV                     SYSIBM    VARCHAR                   1     0 はい
        STUDENT_DIV                    SYSIBM    VARCHAR                   2     0 はい
        REGISTERCD                     SYSIBM    VARCHAR                   8     0 はい
        UPDATED                        SYSIBM    TIMESTAMP                10     0 はい
        
          19 レコードが選択されました。
     */
    private void saveKnj(final Map schregRegdMap) throws SQLException {
        int totalCount = 0;
        ResultSet rs = null;
        for (final Iterator it = schregRegdMap.keySet().iterator(); it.hasNext();) {
            final String shiganshaRenban = (String) it.next();
            final SchregRegdDat schregRegdDat = (SchregRegdDat) schregRegdMap.get(shiganshaRenban);
            int annual = Integer.parseInt(schregRegdDat._annual);
            for (final Iterator iter = schregRegdDat._regdMap.keySet().iterator(); iter.hasNext();) {
                final String key = (String) iter.next();
                final RegdData regdData = (RegdData) schregRegdDat._regdMap.get(key);
                if (annual > 3) {
                    annual = 3;
                }
                annual = schregRegdDat.isMaxData(key) ? Integer.parseInt(schregRegdDat._zaisekiAnnual) : annual;
                final String insSql = getInsertSql(schregRegdDat, regdData, _annuaL.format(annual));
                annual++;
                try{
                    _db2.stmt.executeUpdate(insSql);
                } catch(SQLException e) {
                    log.error("SQLException: " + e.getMessage()+ ":" + ((SQLException)e).getSQLState());
                    throw e;
                }
                totalCount++;
            }
        }
        DbUtils.closeQuietly(rs);
        log.warn("挿入件数=" + totalCount);
    }

    private String getInsertSql(final SchregRegdDat schregRegdDat, final RegdData regdData, final String annual) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" INSERT INTO SCHREG_REGD_DAT ");
        stb.append(" VALUES( ");
        stb.append(" " + getInsertVal(schregRegdDat._schregno) + ", ");
        stb.append(" " + getInsertVal(regdData._year) + ", ");
        stb.append(" " + getInsertVal(schregRegdDat._semester) + ", ");
        stb.append(" " + getInsertVal(regdData._grade) + ", ");
        stb.append(" " + getInsertVal(regdData._hrClass) + ", ");
        stb.append(" " + getInsertVal(schregRegdDat._attendNo) + ", ");
        stb.append(" " + getInsertVal(annual) + ", ");
        stb.append(" " + getInsertVal(schregRegdDat._attach1) + ", ");
        stb.append(" " + getInsertVal(schregRegdDat._attach2) + ", ");
        stb.append(" " + getInsertVal(regdData._partnercd) + ", ");
        stb.append(" " + getInsertVal(schregRegdDat._seatRow) + ", ");
        stb.append(" " + getInsertVal(schregRegdDat._seatCol) + ", ");
        stb.append(" " + getInsertVal(regdData._coursecd) + ", ");
        stb.append(" " + getInsertVal(regdData._majorcd) + ", ");
        stb.append(" " + getInsertVal(regdData._coursecode) + ", ");
        stb.append(" " + getInsertVal(schregRegdDat._courseDiv) + ", ");
        stb.append(" " + getInsertVal(regdData._studentDiv) + ", ");
        stb.append(" '" + Param.REGISTERCD + "', ");
        stb.append(" current timestamp ");
        stb.append(" ) ");

        return stb.toString();
    }
}
// eof

