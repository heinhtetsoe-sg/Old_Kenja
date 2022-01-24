// kanji=漢字
/*
 * $Id: KnjAnotherSchoolGetcreditsDat2.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/11/10 15:46:35 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ANOTHER_SCHOOL_GETCREDITS_DATを作る。
 * @author m-yama
 * @version $Id: KnjAnotherSchoolGetcreditsDat2.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjAnotherSchoolGetcreditsDat2 extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjAnotherSchoolGetcreditsDat2.class);

    public static final DecimalFormat _subClassCd99Format = new DecimalFormat("990000");

    public KnjAnotherSchoolGetcreditsDat2() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "自校外修得単位データ"; }

    void migrate() throws SQLException {
        updateAnother();
    }

    /**
     * 計上済みの売上計画の計画年度と年月を、計上日に変更する。
     */
    private void updateAnother() throws SQLException {

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.*, ");
        stb.append("     RANK() OVER(PARTITION BY T1.APPLICANTNO ");
        stb.append("                 ORDER BY T1.APPLICANTNO, T1.SUBCLASSNAME) + 100 AS KAMOKU_CODE ");
        stb.append(" FROM ");
        stb.append("     ANOTHER_SCHOOL_GETCREDITS_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.SUBCLASSCD LIKE '99%' ");
        stb.append("     AND VALUE(T1.VALUATION, 0) > 1 ");
        stb.append(" ORDER BY ");
        stb.append("     T1.APPLICANTNO ");

        log.debug(stb);

        // SQL実行
        final List result;
        try {
            result = (List) _runner.query(_db2.conn, stb.toString(), _handler);
        } catch (final SQLException e) {
            log.error("ICASSデータ取込みでエラー", e);
            throw e;
        }

        String befAppno = "";
        for (final Iterator iter = result.iterator(); iter.hasNext();) {
            final Map map = (Map) iter.next();
            final String appno = (String) map.get("APPLICANTNO");
            String delSql = getDelSql(map);
            if (!befAppno.equals(appno)) {
                try {
                    _db2.stmt.executeUpdate(delSql);
                } catch (SQLException e) {
                    log.error("SQLException = " + delSql, e);
                }
            }

            String updSql = getUpdSql(map);
            try {
                _db2.stmt.executeUpdate(updSql);
            } catch (SQLException e) {
                log.error("SQLException = " + updSql, e);
            }

            befAppno = appno;

        }

    }

    private String getDelSql(final Map map) {

        final StringBuffer stb = new StringBuffer();

        stb.append(" DELETE FROM ANOTHER_SCHOOL_GETCREDITS_DAT ");
        stb.append(" WHERE ");
        stb.append("     APPLICANTNO = '" + map.get("APPLICANTNO") + "' ");
        stb.append("     AND SUBCLASSCD LIKE '99%' ");
        stb.append("     AND VALUE(VALUATION, 0) > 1 ");

        return stb.toString();
    }

    private String getUpdSql(final Map map) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" INSERT INTO ANOTHER_SCHOOL_GETCREDITS_DAT ");
        stb.append(" VALUES( ");
        stb.append(" " + getInsertVal((String) map.get("YEAR")) + ", ");
        stb.append(" " + getInsertVal((String) map.get("GET_DIV")) + ", ");
        stb.append(" " + getInsertVal((String) map.get("APPLICANTNO")) + ", ");
        stb.append(" " + getInsertVal((String) map.get("GET_METHOD")) + ", ");
        stb.append(" " + getInsertVal((String) map.get("CLASSCD")) + ", ");
        stb.append(" " + getInsertVal((String) map.get("CURRICULUM_CD")) + ", ");
        stb.append(" " + getInsertVal(_subClassCd99Format.format((Number) map.get("KAMOKU_CODE"))) + ", ");
        stb.append(" " + getInsertVal((String) map.get("CREDIT_CURRICULUM_CD")) + ", ");
        stb.append(" " + getInsertVal((String) map.get("CREDIT_ADMITSCD")) + ", ");
        stb.append(" " + getInsertVal((String) map.get("SUBCLASSNAME")) + ", ");
        stb.append(" " + getInsertVal((String) map.get("SUBCLASSABBV")) + ", ");
        stb.append(" " + getInsertVal((Integer) map.get("GET_CREDIT")) + ", ");
        stb.append(" " + getInsertVal((Integer) map.get("VALUATION")) + ", ");
        stb.append(" " + getInsertVal((String) map.get("FORMER_REG_SCHOOLCD")) + ", ");
        stb.append(" " + getInsertVal((String) map.get("GET_DATE")) + ", ");
        stb.append(" " + getInsertVal((String) map.get("REMARK")) + ", ");
        stb.append(" '" + Param.REGISTERCD + "', ");
        stb.append(" current timestamp ");
        stb.append(" ) ");

        return stb.toString();
    }
}
// eof

