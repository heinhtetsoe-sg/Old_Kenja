// kanji=漢字
/*
 * $Id: KnjSchregStudyrecDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO: <KnjSchregStudyrecDat>を作る。
 * @author takaesu
 * @version $Id: KnjSchregStudyrecDat.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjSchregStudyrecDat extends AbstractKnj implements IKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjSchregStudyrecDat.class);

    public KnjSchregStudyrecDat() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "学籍学習記録データ"; }

    void migrate() throws SQLException {
        // TODO: データが多いので分けて移行する
        String[] whereArray = {
                " WHERE T1.YEAR = '2005' and t1.schregno  < '05300000' ",
                " WHERE T1.YEAR = '2005' and t1.schregno >= '05300000' ", 
                " WHERE T1.YEAR = '2006' and t1.schregno  < '05300000' ",
                " WHERE T1.YEAR = '2006' and t1.schregno >= '05300000' ", 
                " WHERE T1.YEAR = '2007' and t1.schregno  < '06300000' ",
                " WHERE T1.YEAR = '2007' and t1.schregno >= '06300000' ", 
        };
        for (int i=0; i<whereArray.length; i++) {
            String where = whereArray[i];
            migrate(where);
        }
    }
    
    void migrate(String where) throws SQLException {
        final String sql;
        sql = " SELECT " + 
        "     '0' AS SCHOOLCD, " +
        "     T1.YEAR, " + 
        "     T1.CLASSCD, " + 
        "     T1.CURRICULUM_CD, " + 
        "     T1.SUBCLASSCD, " + 
        "     T1.SCHREGNO, " + 
        "     (CASE WHEN T1.CLASSCD = '11' " +
        "          THEN NULL " +
        "          ELSE MAX(T1.GRAD_VALUE) " +
        "      END) AS GRAD_VALUE, " + 
        "     SUM(T1.GET_CREDIT) AS GET_CREDIT, " +
        "     VALUE(L1.ANNUAL, '00') AS ANNUAL, " +
        "     MAX(L2.CREDITS) AS CREDITS " + 
        " FROM REC_CREDIT_ADMITS T1 " + 
        "     LEFT JOIN SCHREG_REGD_DAT L1 ON " + 
        "         T1.YEAR = L1.YEAR AND " + 
        "         T1.SCHREGNO = L1.SCHREGNO " + 
        "     LEFT JOIN SUBCLASS_DETAILS_MST L2 ON " + 
        "         T1.YEAR = L2.YEAR AND " + 
        "         T1.CLASSCD = L2.CLASSCD AND " + 
        "         T1.CURRICULUM_CD = L2.CURRICULUM_CD AND " + 
        "         T1.SUBCLASSCD = L2.SUBCLASSCD " + 
        where +
        " GROUP BY " +
        "     T1.YEAR, T1.CLASSCD, T1.CURRICULUM_CD, " +
        "     T1.SUBCLASSCD, T1.SCHREGNO, L1.ANNUAL " +
        " ORDER BY " +
        "     T1.YEAR, T1.SCHREGNO, L1.ANNUAL, T1.CLASSCD, T1.SUBCLASSCD, T1.CURRICULUM_CD "

        ; 

        log.debug("sql=" + sql);
        
        // SQL実行
        final List result;
        try {
            result = _runner.mapListQuery(sql);
        } catch (final SQLException e) {
            log.error("ICASSデータ取込みでエラー", e);
            throw e;
        }
        log.debug("データ件数=" + result.size());

        _runner.listToKnj(result, "schreg_studyrec_dat", this);
    }

    public Object[] mapToArray(final Map map) {
        final Object[] rtn = {
                map.get("SCHOOLCD"),
                map.get("YEAR"),
                map.get("SCHREGNO"),
                map.get("ANNUAL"),
                map.get("CLASSCD"),
                map.get("SUBCLASSCD"),
                map.get("CURRICULUM_CD"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                map.get("GRAD_VALUE"),
                map.get("GET_CREDIT"),
                null,
                map.get("CREDITS"),
                null,
                Param.REGISTERCD,
        };
        //log.debug(year+","+schregno+","+annual+","+classCd+","+subclassCd+","+curriculumCd+","+valuation+","+getCredit+","+compCredit);
        return rtn;
    }
}
// eof

