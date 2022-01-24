// kanji=漢字
/*
 * $Id: KnjSchregAttendrecDat2.java 56574 2017-10-22 11:21:06Z maeshiro $
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
 * <KnjSchregAttendrecDat>を作る。
 * @author takaesu
 * @version $Id: KnjSchregAttendrecDat2.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class KnjSchregAttendrecDat2 extends AbstractKnj {
    /*pkg*/static final Log log = LogFactory.getLog(KnjSchregAttendrecDat2.class);

    public KnjSchregAttendrecDat2() {
        super();
    }

    /** {@inheritDoc} */
    String getTitle() { return "学籍出欠記録データ"; }

    //TODO: 休学日数を決定する
    void migrate() throws SQLException {
        deleteAttendRec();
    }

    /**
     * 
     */
    private void deleteAttendRec() throws SQLException {
        final StringBuffer stb = new StringBuffer();
        stb.append(" DELETE FROM ");
        stb.append("     SCHREG_ATTENDREC_DAT ");
        stb.append(" WHERE ");
        stb.append("     VALUE(CLASSDAYS, 0) = 0 ");
        stb.append("     AND VALUE(OFFDAYS, 0) = 0 ");
        stb.append("     AND VALUE(ABSENT, 0) = 0 ");
        stb.append("     AND VALUE(SUSPEND, 0) = 0 ");
        stb.append("     AND VALUE(MOURNING, 0) = 0 ");
        stb.append("     AND VALUE(ABROAD, 0) = 0 ");
        stb.append("     AND VALUE(REQUIREPRESENT, 0) = 0 ");
        stb.append("     AND VALUE(SICK, 0) = 0 ");
        stb.append("     AND VALUE(ACCIDENTNOTICE, 0) = 0 ");
        stb.append("     AND VALUE(NOACCIDENTNOTICE, 0) = 0 ");
        stb.append("     AND VALUE(PRESENT, 0) = 0 ");

        log.debug(stb);
        try {
            _db2.stmt.executeUpdate(stb.toString());
            _db2.commit();
        } catch (SQLException e) {
            log.error("SQLException = " + stb.toString(), e);
        }

    }

}
// eof

