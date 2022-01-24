// kanji=漢字
/*
 * $Id: GradUpdate.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2017/05/30 14:23:11 - JST
 * 作成者: m-yamashiro
 *
 * Copyright(C) 2017-2021 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.knjxTool;

import java.sql.SQLException;
import java.util.Iterator;

import jp.co.alp.kenja.batch.knjxTool.Param.NameMst;
import nao_package.db.Database;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * クラス情報テーブル。
 *
 * @author m-yamashiro
 * @version $Id: GradUpdate.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class GradUpdate {
    /* pkg */static final Log log = LogFactory.getLog(GradUpdate.class);

    protected final Param _param;

    public GradUpdate(final Param param, final Database knj, final String title) throws SQLException {
        _param = param;

        for (Iterator iterator = _param._a023List.iterator(); iterator.hasNext();) {
            final NameMst nameMst = (NameMst) iterator.next();
            log.info("★" + title + "　" + nameMst._abbv1);
            saveData(knj, nameMst);
        }
    }

    private void saveData(final Database knj, final NameMst nameMst) throws SQLException {
        int count = 0;
        String whereSql1 = " WHERE NENDO = '" + _param._year + "' ";
        String whereSql2 = " AND t1.YEAR = '" + _param._year + "' ";

        final String deleteSql = getDeleteSql(whereSql1);
        knj.executeUpdate(deleteSql);
        final String insertSql = getInsertSql(whereSql2);
        int cnt = knj.executeUpdate(insertSql);
        count += cnt;
        log.debug("データ数=" + count);
    }

    private String getDeleteSql(final String whereSql1) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" DELETE FROM ");
        stb.append("     TOOL_AFT_GRAD_DAT ");
        stb.append(whereSql1);

        return stb.toString();
    }

    private String getInsertSql(final String whereSql2) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" INSERT INTO TOOL_AFT_GRAD_DAT  ");
        stb.append(" SELECT  ");
        stb.append("     t1.YEAR, ");
        stb.append("     t1.SCHREGNO, ");
        stb.append("     t2.NAME as NAME_KANJI, ");
        stb.append("     t2.NAME_KANA, ");
        stb.append("     t1.STAT_CD, ");
        stb.append("     t3.SCHOOL_NAME, ");
        stb.append("     t1.FACULTYCD, ");
        stb.append("     t4.FACULTYNAME, ");
        stb.append("     t1.DEPARTMENTCD, ");
        stb.append("     t5.DEPARTMENTNAME, ");
        stb.append("     null as ANONYMOUS_FLG, ");
        stb.append("     case when t2.GRD_DIV = '1' then '2' ");
        stb.append("     else '1' end as ZAISEKI_FLG, ");
        stb.append("     CASE ");
        stb.append("     WHEN t1.UPDATED > t2.UPDATED ");
        stb.append("     THEN t1.UPDATED ");
        stb.append("     WHEN t2.UPDATED > t1.UPDATED ");
        stb.append("     THEN t2.UPDATED ");
        stb.append("     ELSE t1.UPDATED ");
        stb.append("     END AS UPDATED, ");
        stb.append("     t1.REGISTERCD, ");
        stb.append("     sysdate() as COMPDATETIME ");
        stb.append(" FROM ");
        stb.append("     AFT_GRAD_COURSE_DAT t1 ");
        stb.append("     left join SCHREG_BASE_MST t2 on t1.SCHREGNO = t2.SCHREGNO ");
        stb.append("     left join COLLEGE_MST t3 on t1.STAT_CD = t3.SCHOOL_CD ");
        stb.append("     left join COLLEGE_FACULTY_MST t4 on t1.STAT_CD = t4.SCHOOL_CD and t1.FACULTYCD = t4.FACULTYCD ");
        stb.append("     left join COLLEGE_DEPARTMENT_MST t5 on t1.STAT_CD = t5.SCHOOL_CD and t1.FACULTYCD = t5.FACULTYCD and t1.DEPARTMENTCD = t5.DEPARTMENTCD ");
        stb.append(" WHERE ");
        stb.append("     t1.STAT_CD IS NOT NULL AND t1.DECISION = '1' ");
        stb.append(whereSql2);
        stb.append(" ORDER BY SCHREGNO ");

        return stb.toString();
    }
} // GradUpdate

// eof
