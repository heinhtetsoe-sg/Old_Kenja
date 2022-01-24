// kanji=漢字
/*
 * $Id: KnjRecCreditAdmits2.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2008/08/15 15:46:35 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.withus.icass.migration;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * REC_CREDIT_ADMITS を作る。
 * @author takaesu
 * @version $Id: KnjRecCreditAdmits2.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjRecCreditAdmits2 extends AbstractKnj{
    /*pkg*/static final Log log = LogFactory.getLog(KnjRecCreditAdmits2.class);
    private static final String KYOKA91 = "91";

    public KnjRecCreditAdmits2() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "単位認定結果　TEST_SCOREセット"; }


    void migrate() throws SQLException {

        final String sql = ""
                            +" UPDATE "
                            +"     REC_CREDIT_ADMITS t1 "
                            +" SET "
                            +"     (t1.TEST_SCORE) =   "
                            +"     (SELECT "
                            +"         VALUE(MAX(t2.SCORE), CAST(null AS SMALLINT)) "
                            +"      FROM "
                            +"         REC_TEST_DAT t2 "
                            +"      WHERE "
                            +"         t1.YEAR = t2.YEAR "
                            +"         AND t1.YEAR = t2.YEAR "
                            +"         AND t1.CLASSCD = t2.CLASSCD "
                            +"         AND t1.CURRICULUM_CD = t2.CURRICULUM_CD "
                            +"         AND t1.SUBCLASSCD = t2.SUBCLASSCD "
                            +"         AND t1.SCHREGNO = t2.SCHREGNO "
                            +"     ) ";

        try {
            _db2.stmt.executeUpdate(sql);
            _db2.commit();
        } catch (SQLException e) {
            log.error("SQLException = " + sql, e);
        }

    }

}
// eof

