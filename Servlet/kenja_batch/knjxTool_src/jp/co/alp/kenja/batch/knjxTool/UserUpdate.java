// kanji=漢字
/*
 * $Id: UserUpdate.java 56574 2017-10-22 11:21:06Z maeshiro $
 *
 * 作成日: 2017/05/30 14:23:11 - JST
 * 作成者: m-yamashiro
 *
 * Copyright(C) 2017-2021 ALP Co.,Ltd. All rights reserved.
 */
package jp.co.alp.kenja.batch.knjxTool;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.Database;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * クラス情報テーブル。
 *
 * @author m-yamashiro
 * @version $Id: UserUpdate.java 56574 2017-10-22 11:21:06Z maeshiro $
 */
public class UserUpdate {
    /* pkg */static final Log log = LogFactory.getLog(UserUpdate.class);

    protected final Param _param;

    public UserUpdate(final Param param, final Database knj, final String title) throws SQLException {
        _param = param;

        log.info("★" + title);
        saveData(knj);
    }

    private void saveData(final Database knj) throws SQLException {
        final String deleteSql = getDeleteSql();
        knj.executeUpdate(deleteSql);
        knj.commit();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getUserData();
            ps = knj.prepareStatement(sql);
            rs = ps.executeQuery();
            int count = 0;
            String sep1 = "";
            String sep2 = "";
            String sep3 = "";
            String setYear = "";
            String setUserId = "";
            String setBumonCd = "";
            String setClassCd = "";
            String setGradeCd = "";
            while (rs.next()) {
                final String year = rs.getString("YEAR");
                final String userId = rs.getString("USERID");
                final String trgtGrade = rs.getString("TRGTGRADE");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String classCd = rs.getString("CLASSCD");
                final String groupCd = rs.getString("GROUPCD");
                if (count > 0 && (!year.equals(setYear) || !userId.equals(setUserId))) {
                    final String insertSql = getInsertSql(setYear, setUserId, setBumonCd, setClassCd, setGradeCd);
                    knj.executeUpdate(insertSql);
                    knj.commit();
                    setYear = "";
                    setUserId = "";
                    setBumonCd = "";
                    setClassCd = "";
                    setGradeCd = "";
                    sep1 = "";
                    sep2 = "";
                    sep3 = "";
                }
                setYear = year;
                setUserId = userId;

                if (null != schoolKind && !"".equals(schoolKind)) {
                    String bumoncd = "";
                    if("P".equals(schoolKind)){
                        bumoncd = "1";
                    }
                    if("J".equals(schoolKind)){
                        bumoncd = "2";
                    }
                    if("H".equals(schoolKind)){
                        bumoncd = "3";
                    }
                    if(setBumonCd.indexOf(bumoncd) < 0) {
                        setBumonCd = setBumonCd + sep1 + bumoncd;
                        sep1 = ",";
                    }
                }

                if (null != trgtGrade && !"".equals(trgtGrade)) {
                    if (setGradeCd.indexOf(trgtGrade) < 0) {
                        setGradeCd = setGradeCd + sep2 + trgtGrade;
                        sep2 = ",";
                    }
                }

                if (null != classCd && !"".equals(classCd)) {
                    if (setClassCd.indexOf(classCd) < 0){
                        setClassCd = setClassCd + sep3 + classCd;
                        sep3 = ",";
                    }
                }

                //教務と教科主任は属してる部門のすべての学年が見れる
                if (null != groupCd && ("0004".equals(groupCd) || "0021".equals(groupCd))) {
                    String sepG = "";
                    String wrkG = "";
                    if (setBumonCd.indexOf("1") > -1) {
                        wrkG = "1,2,3,4,5,6";
                        sepG = ",";
                    }
                    if(setBumonCd.indexOf("2") > -1) {
                        wrkG += sepG;
                        wrkG += "7,8,9";
                        sepG  = ",";
                    }
                    if(setBumonCd.indexOf("3") > -1 || setBumonCd.indexOf("4") > -1) {
                        wrkG += sepG;
                        wrkG += "10,11,12";
                    }
                    if(!"".equals(wrkG)){
                        setGradeCd = wrkG;
                    }
                }

                if ("0003".equals(groupCd) || "0012".equals(groupCd)) {
                    setBumonCd = "1,2,3,4";
                    setGradeCd = "1,2,3,4,5,6,7,8,9,10,11,12";
                }
                count++;
            }
            if (count > 0) {
                final String insertSql = getInsertSql(setYear, setUserId, setBumonCd, setClassCd, setGradeCd);
                knj.executeUpdate(insertSql);
                knj.commit();
            }
            rs.close();
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String getDeleteSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" DELETE FROM ");
        stb.append("     TOOL_USER_MST ");

        return stb.toString();
    }

    private String getUserData() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT DISTINCT ");
        stb.append("     T5.YEAR, ");
        stb.append("     T1.USERID, ");
        stb.append("     LTRIM(T3.TRGTGRADE,'0') AS TRGTGRADE, ");
        stb.append("     T4.SCHOOL_KIND, ");
        stb.append("     T4.CLASSCD, ");
        stb.append("     T6.GROUPCD ");
        stb.append(" FROM ");
        stb.append("     STAFF_MST T0 ");
        stb.append("     LEFT JOIN STAFF_YDAT T5 on T0.STAFFCD = T5.STAFFCD ");
        stb.append("     LEFT JOIN USER_MST T1 ON T0.STAFFCD = T1.STAFFCD  ");
        stb.append("     LEFT JOIN CHAIR_STF_DAT T2 on T0.STAFFCD = T2.STAFFCD and T5.YEAR = T2.YEAR ");
        stb.append("     LEFT JOIN CHAIR_CLS_DAT T3 on T2.YEAR = T3.YEAR and T2.SEMESTER = T3.SEMESTER and T2.CHAIRCD = T3.CHAIRCD  ");
        stb.append("     LEFT JOIN CHAIR_DAT T4 on T2.YEAR = T4.YEAR and T2.SEMESTER = T4.SEMESTER and T2.CHAIRCD = T4.CHAIRCD and T1.SCHOOL_KIND = T4.SCHOOL_KIND ");
        stb.append("     LEFT JOIN USERGROUP_DAT T6 on T1.STAFFCD = T6.STAFFCD and T1.SCHOOLCD = T6.SCHOOLCD and T1.SCHOOL_KIND = T6.SCHOOL_KIND and T5.YEAR = T6.YEAR and T6.GROUPCD in('0003','0004','0012','0021') ");
        stb.append(" WHERE ");
        stb.append("     T5.YEAR IS NOT NULL AND ");
        stb.append("     T5.STAFFCD IS NOT NULL AND ");
        stb.append("     T1.USERID IS NOT NULL AND ");
        stb.append("     (T3.TRGTGRADE IS NOT NULL OR T4.SCHOOL_KIND IS NOT NULL OR T4.CLASSCD IS NOT NULL OR T6.GROUPCD IS NOT NULL) AND ");
        stb.append("     (T1.INVALID_FLG = '0' OR INVALID_FLG IS NULL) ");
        stb.append(" ORDER BY ");
        stb.append("     YEAR, ");
        stb.append("     USERID, ");
        stb.append("     SCHOOL_KIND, ");
        stb.append("     TRGTGRADE, ");
        stb.append("     CLASSCD ");

        return stb.toString();
    }

    private String getInsertSql(final String setYear, final String setUserId, final String setBumonCd, final String setClassCd, final String setGradeCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" INSERT INTO TOOL_USER_MST  ");
        stb.append(" VALUES('" + setYear + "', '" + setUserId + "', '" + setBumonCd + "', '" + setClassCd + "', '" + setGradeCd + "') ");

        return stb.toString();
    }
} // UserUpdate

// eof
