// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2011/08/03 11:20:55 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;


public class KNJF071 {

    private static final Log log = LogFactory.getLog("KNJF071.class");

    private boolean _hasData;

    Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List gradeAgeList = getGradeAgeList(db2);

        final List gradeMedexamList = getGradeMedexamList(db2, gradeAgeList);

        printMedexam(svf, gradeMedexamList);
    }

    private void printMedexam(final Vrw32alp svf, final List gradeMedexamList) {
        svf.VrSetForm(!_param._isKumamoto ? "KNJF071_2.frm" : "KNJF071.frm", 1);
        svf.VrsOut("NENDO"    , _param._nendo);
        svf.VrsOut("TODAY"    , _param._today);
        if (_param._isAgeNozoku) {
            svf.VrsOut("REMARK"   , "（注）異年令を除く");
        }
        for (final Iterator it = gradeMedexamList.iterator(); it.hasNext();) {
            final GradeMedexam gradeMedexam = (GradeMedexam) it.next();

            int lineG = Integer.parseInt(gradeMedexam._grade);
            int lineS = lineG * 3;
            if (lineG == 5 && "1".equals(gradeMedexam._sex)) svf.VrEndPage();
            if (lineG > 4) {
                lineG = lineG - 4;
                lineS = lineG * 3;
            }
            if ("1".equals(gradeMedexam._sex)) lineS = lineS - 2;
            if ("2".equals(gradeMedexam._sex)) lineS = lineS - 1;
            if (lineS > 0 && lineS <= 15) {
                //学年
                if (lineG != 5) {
                    svf.VrsOutn("GRADE",    lineG, String.valueOf(Integer.parseInt(gradeMedexam._grade)) + _param._gname);
                }
                //体位(平均)
                svf.VrsOutn("HEIGHT",       lineS, gradeMedexam._height);
                svf.VrsOutn("WEIGHT",       lineS, gradeMedexam._weight);
                svf.VrsOutn("SEATED_HEIGHT", lineS, gradeMedexam._sitheight);
                //BMI(%)
                svf.VrsOutn("BMI1",         lineS, gradeMedexam._bmi1p);
                svf.VrsOutn("BMI2",         lineS, gradeMedexam._bmi2p);
                svf.VrsOutn("BMI3",         lineS, gradeMedexam._bmi3p);
                svf.VrsOutn("BMI4",         lineS, gradeMedexam._bmi4p);
                svf.VrsOutn("BMI5",         lineS, gradeMedexam._bmi5p);
                if (!_param._isKumamoto) {
                    svf.VrsOutn("BMI6",         lineS, gradeMedexam._bmi6p);
                }
                //BMI(人数)
                svf.VrsOutn("BMISUM1",      lineS, gradeMedexam._bmi1);
                svf.VrsOutn("BMISUM2",      lineS, gradeMedexam._bmi2);
                svf.VrsOutn("BMISUM3",      lineS, gradeMedexam._bmi3);
                svf.VrsOutn("BMISUM4",      lineS, gradeMedexam._bmi4);
                svf.VrsOutn("BMISUM5",      lineS, gradeMedexam._bmi5);
                if (!_param._isKumamoto) {
                    svf.VrsOutn("BMISUM6",      lineS, gradeMedexam._bmi6);
                }
                svf.VrsOutn("BMI_TOTAL",    lineS, gradeMedexam._bmiTotal);
                //視力(%)
                svf.VrsOutn("EYE1",         lineS, gradeMedexam._eye1p);
                svf.VrsOutn("EYE2",         lineS, gradeMedexam._eye2p);
                svf.VrsOutn("EYE3",         lineS, gradeMedexam._eye3p);
                svf.VrsOutn("EYE4",         lineS, gradeMedexam._eye4p);
                //視力(人数)
                svf.VrsOutn("EYESUM1",      lineS, gradeMedexam._eye1);
                svf.VrsOutn("EYESUM2",      lineS, gradeMedexam._eye2);
                svf.VrsOutn("EYESUM3",      lineS, gradeMedexam._eye3);
                svf.VrsOutn("EYESUM4",      lineS, gradeMedexam._eye4);
                svf.VrsOutn("EYE_TOTAL",    lineS, gradeMedexam._eyeTotal);
                //歯科(%)
                svf.VrsOutn("TOOTH1",       lineS, gradeMedexam._tooth01p);
                svf.VrsOutn("TOOTH2",       lineS, gradeMedexam._tooth02p);
                svf.VrsOutn("TOOTH3",       lineS, gradeMedexam._tooth03p);
                svf.VrsOutn("TOOTH4",       lineS, gradeMedexam._tooth04p);
                svf.VrsOutn("TOOTH5",       lineS, gradeMedexam._tooth05p);
                svf.VrsOutn("TOOTH6",       lineS, gradeMedexam._tooth06p);
                svf.VrsOutn("TOOTH7",       lineS, gradeMedexam._tooth07p);
                svf.VrsOutn("TOOTH8",       lineS, gradeMedexam._tooth08p);
                svf.VrsOutn("TOOTH9",       lineS, gradeMedexam._tooth09p);
                svf.VrsOutn("TOOTH10",      lineS, gradeMedexam._tooth10p);
                svf.VrsOutn("TOOTH11",      lineS, gradeMedexam._tooth11p);
                svf.VrsOutn("TOOTH12",      lineS, gradeMedexam._tooth12p);
                svf.VrsOutn("TOOTH13",      lineS, gradeMedexam._tooth13p);
                svf.VrsOutn("TOOTH14",      lineS, gradeMedexam._tooth14p);
                svf.VrsOutn("TOOTH15",      lineS, gradeMedexam._tooth15p);
                //歯科(人数)
                svf.VrsOutn("TOOTHSUM1",    lineS, gradeMedexam._tooth01);
                svf.VrsOutn("TOOTHSUM2",    lineS, gradeMedexam._tooth02);
                svf.VrsOutn("TOOTHSUM3",    lineS, gradeMedexam._tooth03);
                svf.VrsOutn("TOOTHSUM4",    lineS, gradeMedexam._tooth04);
                svf.VrsOutn("TOOTHSUM5",    lineS, gradeMedexam._tooth05);
                svf.VrsOutn("TOOTHSUM6",    lineS, gradeMedexam._tooth06);
                svf.VrsOutn("TOOTHSUM7",    lineS, gradeMedexam._tooth07);
                svf.VrsOutn("TOOTHSUM8",    lineS, gradeMedexam._tooth08);
                svf.VrsOutn("TOOTHSUM9",    lineS, gradeMedexam._tooth09);
                svf.VrsOutn("TOOTHSUM10",   lineS, gradeMedexam._tooth10);
                svf.VrsOutn("TOOTHSUM11",   lineS, gradeMedexam._tooth11);
                svf.VrsOutn("TOOTHSUM12",   lineS, gradeMedexam._tooth12);
                svf.VrsOutn("TOOTHSUM13",   lineS, gradeMedexam._tooth13);
                svf.VrsOutn("TOOTHSUM14",   lineS, gradeMedexam._tooth14);
                svf.VrsOutn("TOOTHSUM15",   lineS, gradeMedexam._tooth15);
                svf.VrsOutn("TOOTH_TOTAL",  lineS, gradeMedexam._toothTotal);

                _hasData = true;
            }
        }
        if (_hasData) {
            svf.VrsOut("NENDO"    , _param._nendo);
            svf.VrsOut("TODAY"    , _param._today);
            if (_param._isAgeNozoku) {
                svf.VrsOut("REMARK"   , "（注）異年令を除く");
            }
            svf.VrEndPage();
        }
    }

    private List getGradeMedexamList(final DB2UDB db2, final List gradeAgeList) throws SQLException  {
        final List rtnList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getGradeMedexamSql(gradeAgeList);
            log.debug("getGradeMedexamSql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final GradeMedexam gradeMedexam = new GradeMedexam(rs.getString("GRADE"),
                                                                   rs.getString("SEX"),
                                                                   rs.getString("GRADE_NAME1"),

                                                                   rs.getString("HEIGHT"),
                                                                   rs.getString("WEIGHT"),
                                                                   rs.getString("SITHEIGHT"),
                                                                   rs.getString("BMI_TOTAL"),
                                                                   rs.getString("BMI1"),
                                                                   rs.getString("BMI2"),
                                                                   rs.getString("BMI3"),
                                                                   rs.getString("BMI4"),
                                                                   rs.getString("BMI5"),
                                                                   !_param._isKumamoto ? rs.getString("BMI6") : "",
                                                                   rs.getString("BMI1P"),
                                                                   rs.getString("BMI2P"),
                                                                   rs.getString("BMI3P"),
                                                                   rs.getString("BMI4P"),
                                                                   rs.getString("BMI5P"),
                                                                   !_param._isKumamoto ? rs.getString("BMI6P") : "",

                                                                   rs.getString("EYE_TOTAL"),
                                                                   rs.getString("EYE1"),
                                                                   rs.getString("EYE2"),
                                                                   rs.getString("EYE3"),
                                                                   rs.getString("EYE4"),
                                                                   rs.getString("EYE1P"),
                                                                   rs.getString("EYE2P"),
                                                                   rs.getString("EYE3P"),
                                                                   rs.getString("EYE4P"),

                                                                   rs.getString("TOOTH_TOTAL"),
                                                                   rs.getString("TOOTH01"),
                                                                   rs.getString("TOOTH02"),
                                                                   rs.getString("TOOTH03"),
                                                                   rs.getString("TOOTH04"),
                                                                   rs.getString("TOOTH05"),
                                                                   rs.getString("TOOTH06"),
                                                                   rs.getString("TOOTH07"),
                                                                   rs.getString("TOOTH08"),
                                                                   rs.getString("TOOTH09"),
                                                                   rs.getString("TOOTH10"),
                                                                   rs.getString("TOOTH11"),
                                                                   rs.getString("TOOTH12"),
                                                                   rs.getString("TOOTH13"),
                                                                   rs.getString("TOOTH14"),
                                                                   rs.getString("TOOTH15"),
                                                                   rs.getString("TOOTH01P"),
                                                                   rs.getString("TOOTH02P"),
                                                                   rs.getString("TOOTH03P"),
                                                                   rs.getString("TOOTH04P"),
                                                                   rs.getString("TOOTH05P"),
                                                                   rs.getString("TOOTH06P"),
                                                                   rs.getString("TOOTH07P"),
                                                                   rs.getString("TOOTH08P"),
                                                                   rs.getString("TOOTH09P"),
                                                                   rs.getString("TOOTH10P"),
                                                                   rs.getString("TOOTH11P"),
                                                                   rs.getString("TOOTH12P"),
                                                                   rs.getString("TOOTH13P"),
                                                                   rs.getString("TOOTH14P"),
                                                                   rs.getString("TOOTH15P"));
                rtnList.add(gradeMedexam);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtnList;
    }

    private String getGradeMedexamSql(final List gradeAgeList) {
        final StringBuffer stb = new StringBuffer();

        String rVisionMark = "R_VISION_MARK";
        String rBarevisionMark = "R_BAREVISION_MARK";
        String lVisionMark = "L_VISION_MARK";
        String lBarevisionMark = "L_BAREVISION_MARK";
        if ("1".equals(_param._VisionMarkSaveValueCd)) {
            rVisionMark = "RVM_F017.NAME1";
            rBarevisionMark = "RBVM_F017.NAME1";
            lVisionMark = "LVM_F017.NAME1";
            lBarevisionMark = "LBVM_F017.NAME1";
        }

        stb.append(" WITH GDAT(GRADE, GRADE_NAME1) AS ( ");
        stb.append("     SELECT ");
        stb.append("         GRADE, ");
        stb.append("         GRADE_NAME1 ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_GDAT ");
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._year + "' ");
        stb.append("     UNION ALL ");
        stb.append("     VALUES('9','合計') ");
        stb.append("     ) ");

        //異年令を除く
        if (_param._isAgeNozoku) {
            stb.append(" , T_AGE(GRADE, AGE) AS ( ");
            stb.append("     VALUES('99', 99) ");//ダミー

            for (final Iterator it = gradeAgeList.iterator(); it.hasNext();) {
                final GradeAge gradeAge = (GradeAge) it.next();

                stb.append("     UNION ALL VALUES('" + gradeAge._grade + "', " + gradeAge._age + ") ");
            }

            stb.append("     ) ");

            stb.append(" , NOT_AGE AS ( ");
            stb.append("     SELECT ");
            stb.append("         T2.SCHREGNO, ");
            stb.append("         T2.GRADE ");
            stb.append("     FROM ");
            stb.append("        (SELECT ");
            stb.append("             YEAR, ");
            stb.append("             SCHREGNO, ");
            stb.append("             MAX(GRADE) AS GRADE ");
            stb.append("         FROM ");
            stb.append("             SCHREG_REGD_DAT ");
            stb.append("         WHERE ");
            stb.append("             YEAR = '" + _param._year + "' ");
            stb.append("         GROUP BY ");
            stb.append("             YEAR, ");
            stb.append("             SCHREGNO ");
            stb.append("         ) T2 ");
            stb.append("         INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T2.SCHREGNO ");
            stb.append("         INNER JOIN T_AGE NM ON NM.GRADE = T2.GRADE ");
            stb.append("     WHERE ");
            stb.append("         T3.BIRTHDAY < DATE(RTRIM(CHAR(SMALLINT(T2.YEAR) - SMALLINT(NM.AGE) - 1)) || '-04-02') OR ");
            stb.append("         T3.BIRTHDAY > DATE(RTRIM(CHAR(SMALLINT(T2.YEAR) - SMALLINT(NM.AGE))) || '-04-01') ");
            stb.append("     ) ");
        }

        //肥満度
        if (!_param._isKumamoto) {
            stb.append(" , MAX_YEAR AS ( ");
            stb.append("   SELECT ");
            stb.append("       MAX(YEAR) AS YEAR ");
            stb.append("   FROM ");
            stb.append("       HEXAM_PHYSICAL_AVG_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR <= '" + _param._year + "' ");
            stb.append(" ), MIN_YEAR AS ( ");
            stb.append("   SELECT ");
            stb.append("       MIN(YEAR) AS YEAR ");
            stb.append("   FROM ");
            stb.append("       HEXAM_PHYSICAL_AVG_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR >= '" + _param._year + "' ");
            stb.append(" ), MAX_MIN_YEAR AS ( ");
            stb.append("   SELECT ");
            stb.append("       MIN(T1.YEAR) AS YEAR ");
            stb.append("   FROM ( ");
            stb.append("       SELECT YEAR FROM MAX_YEAR T1 ");
            stb.append("       UNION ");
            stb.append("       SELECT YEAR FROM MIN_YEAR T1 ");
            stb.append("   ) T1 ");
            stb.append(" ), HEXAM_PHYSICAL_AVG AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SEX, ");
            stb.append("     T1.NENREI_YEAR, ");
            stb.append("     T1.NENREI_MONTH, ");
            stb.append("     T1.STD_WEIGHT_KEISU_A, ");
            stb.append("     T1.STD_WEIGHT_KEISU_B ");
            stb.append(" FROM ");
            stb.append("    HEXAM_PHYSICAL_AVG_DAT T1 ");
            stb.append("    INNER JOIN MAX_MIN_YEAR T2 ON T2.YEAR = T1.YEAR ");
            stb.append(" ) ");
        }

        stb.append(" SELECT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.SEX, ");
        stb.append("     G1.GRADE_NAME1, ");

        stb.append("     HEIGHT, ");
        stb.append("     WEIGHT, ");
        stb.append("     SITHEIGHT, ");
        stb.append("     BMI_TOTAL, ");
        stb.append("     BMI1, ");
        stb.append("     BMI2, ");
        stb.append("     BMI3, ");
        stb.append("     BMI4, ");
        stb.append("     BMI5, ");
        if (!_param._isKumamoto) {
            stb.append("     BMI6, ");
        }
        stb.append("     CASE WHEN BMI_TOTAL > 0 ");
        stb.append("          THEN DECIMAL(ROUND(FLOAT(BMI1)/FLOAT(BMI_TOTAL)*100,1),5,1) ");
        stb.append("          ELSE 0 END AS BMI1P, ");
        stb.append("     CASE WHEN BMI_TOTAL > 0 ");
        stb.append("          THEN DECIMAL(ROUND(FLOAT(BMI2)/FLOAT(BMI_TOTAL)*100,1),5,1) ");
        stb.append("          ELSE 0 END AS BMI2P, ");
        stb.append("     CASE WHEN BMI_TOTAL > 0 ");
        stb.append("          THEN DECIMAL(ROUND(FLOAT(BMI3)/FLOAT(BMI_TOTAL)*100,1),5,1) ");
        stb.append("          ELSE 0 END AS BMI3P, ");
        stb.append("     CASE WHEN BMI_TOTAL > 0 ");
        stb.append("          THEN DECIMAL(ROUND(FLOAT(BMI4)/FLOAT(BMI_TOTAL)*100,1),5,1) ");
        stb.append("          ELSE 0 END AS BMI4P, ");
        stb.append("     CASE WHEN BMI_TOTAL > 0 ");
        stb.append("          THEN DECIMAL(ROUND(FLOAT(BMI5)/FLOAT(BMI_TOTAL)*100,1),5,1) ");
        stb.append("          ELSE 0 END AS BMI5P, ");
        if (!_param._isKumamoto) {
            stb.append("     CASE WHEN BMI_TOTAL > 0 ");
            stb.append("          THEN DECIMAL(ROUND(FLOAT(BMI6)/FLOAT(BMI_TOTAL)*100,1),5,1) ");
            stb.append("          ELSE 0 END AS BMI6P, ");
        }

        stb.append("     EYE_TOTAL, ");
        stb.append("     EYE1, ");
        stb.append("     EYE2, ");
        stb.append("     EYE3, ");
        stb.append("     EYE4, ");
        stb.append("     CASE WHEN EYE_TOTAL > 0 ");
        stb.append("          THEN DECIMAL(ROUND(FLOAT(EYE1)/FLOAT(EYE_TOTAL)*100,1),5,1) ");
        stb.append("          ELSE 0 END AS EYE1P, ");
        stb.append("     CASE WHEN EYE_TOTAL > 0 ");
        stb.append("          THEN DECIMAL(ROUND(FLOAT(EYE2)/FLOAT(EYE_TOTAL)*100,1),5,1) ");
        stb.append("          ELSE 0 END AS EYE2P, ");
        stb.append("     CASE WHEN EYE_TOTAL > 0 ");
        stb.append("          THEN DECIMAL(ROUND(FLOAT(EYE3)/FLOAT(EYE_TOTAL)*100,1),5,1) ");
        stb.append("          ELSE 0 END AS EYE3P, ");
        stb.append("     CASE WHEN EYE_TOTAL > 0 ");
        stb.append("          THEN DECIMAL(ROUND(FLOAT(EYE4)/FLOAT(EYE_TOTAL)*100,1),5,1) ");
        stb.append("          ELSE 0 END AS EYE4P, ");

        stb.append("     TOOTH_TOTAL, ");
        stb.append("     TOOTH01, ");
        stb.append("     TOOTH02, ");
        stb.append("     TOOTH03, ");
        stb.append("     TOOTH04, ");
        stb.append("     TOOTH05, ");
        stb.append("     TOOTH06, ");
        stb.append("     TOOTH07, ");
        stb.append("     TOOTH08, ");
        stb.append("     TOOTH09, ");
        stb.append("     TOOTH10, ");
        stb.append("     TOOTH11, ");
        stb.append("     TOOTH12, ");
        stb.append("     TOOTH13, ");
        stb.append("     TOOTH14, ");
        stb.append("     TOOTH15, ");
        stb.append("     CASE WHEN TOOTH_TOTAL=0 THEN 0 ELSE ");
        stb.append("          DECIMAL(ROUND(FLOAT(TOOTH01)/FLOAT(TOOTH_TOTAL)*100,1),5,1) END AS TOOTH01P, ");
        stb.append("     CASE WHEN TOOTH_TOTAL=0 THEN 0 ELSE ");
        stb.append("          DECIMAL(ROUND(FLOAT(TOOTH02)/FLOAT(TOOTH_TOTAL)*100,1),5,1) END AS TOOTH02P, ");
        stb.append("     CASE WHEN TOOTH_TOTAL=0 THEN 0 ELSE ");
        stb.append("          DECIMAL(ROUND(FLOAT(TOOTH03)/FLOAT(TOOTH_TOTAL)*100,1),5,1) END AS TOOTH03P, ");
        stb.append("     CASE WHEN TOOTH_TOTAL=0 THEN 0 ELSE ");
        stb.append("          DECIMAL(ROUND(FLOAT(TOOTH04)/FLOAT(TOOTH_TOTAL)*100,1),5,1) END AS TOOTH04P, ");
        stb.append("     CASE WHEN TOOTH_TOTAL=0 THEN 0 ELSE ");
        stb.append("          DECIMAL(ROUND(FLOAT(TOOTH05)/FLOAT(TOOTH_TOTAL)*100,1),5,1) END AS TOOTH05P, ");
        stb.append("     CASE WHEN TOOTH_TOTAL=0 THEN 0 ELSE ");
        stb.append("          DECIMAL(ROUND(FLOAT(TOOTH06)/FLOAT(TOOTH_TOTAL)*100,1),5,1) END AS TOOTH06P, ");
        stb.append("     CASE WHEN TOOTH_TOTAL=0 THEN 0 ELSE ");
        stb.append("          DECIMAL(ROUND(FLOAT(TOOTH07)/FLOAT(TOOTH_TOTAL)*100,1),5,1) END AS TOOTH07P, ");
        stb.append("     CASE WHEN TOOTH_TOTAL=0 THEN 0 ELSE ");
        stb.append("          DECIMAL(ROUND(FLOAT(TOOTH08)/FLOAT(TOOTH_TOTAL)*100,1),5,1) END AS TOOTH08P, ");
        stb.append("     CASE WHEN TOOTH_TOTAL=0 THEN 0 ELSE ");
        stb.append("          DECIMAL(ROUND(FLOAT(TOOTH09)/FLOAT(TOOTH_TOTAL)*100,1),5,1) END AS TOOTH09P, ");
        stb.append("     CASE WHEN TOOTH_TOTAL=0 THEN 0 ELSE ");
        stb.append("          DECIMAL(ROUND(FLOAT(TOOTH10)/FLOAT(TOOTH_TOTAL)*100,1),5,1) END AS TOOTH10P, ");
        stb.append("     CASE WHEN TOOTH_TOTAL=0 THEN 0 ELSE ");
        stb.append("          DECIMAL(ROUND(FLOAT(TOOTH11)/FLOAT(TOOTH_TOTAL)*100,1),5,1) END AS TOOTH11P, ");
        stb.append("     CASE WHEN TOOTH_TOTAL=0 THEN 0 ELSE ");
        stb.append("          DECIMAL(ROUND(FLOAT(TOOTH12)/FLOAT(TOOTH_TOTAL)*100,1),5,1) END AS TOOTH12P, ");
        stb.append("     CASE WHEN TOOTH_TOTAL=0 THEN 0 ELSE ");
        stb.append("          DECIMAL(ROUND(FLOAT(TOOTH13)/FLOAT(TOOTH_TOTAL)*100,1),5,1) END AS TOOTH13P, ");
        stb.append("     CASE WHEN TOOTH_TOTAL=0 THEN 0 ELSE ");
        stb.append("          DECIMAL(ROUND(FLOAT(TOOTH14)/FLOAT(TOOTH_TOTAL)*100,1),5,1) END AS TOOTH14P, ");
        stb.append("     CASE WHEN TOOTH_TOTAL=0 THEN 0 ELSE ");
        stb.append("          DECIMAL(ROUND(FLOAT(TOOTH15)/FLOAT(TOOTH_TOTAL)*100,1),5,1) END AS TOOTH15P ");

        stb.append(" FROM ");
        stb.append("     ( ");
        stb.append("         SELECT ");
        stb.append("             VALUE(T2.GRADE,'9') AS GRADE, ");
        stb.append("             VALUE(T3.SEX,'3') AS SEX, ");

        stb.append("             DECIMAL(ROUND(AVG(HEIGHT),1),5,1) AS HEIGHT, ");
        stb.append("             DECIMAL(ROUND(AVG(WEIGHT),1),5,1) AS WEIGHT, ");
        stb.append("             DECIMAL(ROUND(AVG(SITHEIGHT),1),5,1) AS SITHEIGHT, ");
        if (!_param._isKumamoto) {
            String bmi;
            bmi  = "(CASE WHEN WEIGHT IS NULL OR HEIGHT IS NULL ";
            bmi += " OR (HP_MAX2.STD_WEIGHT_KEISU_A IS NULL AND HP_MAX.STD_WEIGHT_KEISU_A IS NULL AND HP.STD_WEIGHT_KEISU_A IS NULL) ";
            bmi += " OR (HP_MAX2.STD_WEIGHT_KEISU_B IS NULL AND HP_MAX.STD_WEIGHT_KEISU_B IS NULL AND HP.STD_WEIGHT_KEISU_B IS NULL) THEN NULL ";
            bmi += "ELSE DECIMAL(ROUND((WEIGHT-(VALUE(HP.STD_WEIGHT_KEISU_A,HP_MAX.STD_WEIGHT_KEISU_A,HP_MAX2.STD_WEIGHT_KEISU_A)*HEIGHT-VALUE(HP.STD_WEIGHT_KEISU_B,HP_MAX.STD_WEIGHT_KEISU_B,HP_MAX2.STD_WEIGHT_KEISU_B)))/(VALUE(HP.STD_WEIGHT_KEISU_A,HP_MAX.STD_WEIGHT_KEISU_A,HP_MAX2.STD_WEIGHT_KEISU_A)*HEIGHT-VALUE(HP.STD_WEIGHT_KEISU_B,HP_MAX.STD_WEIGHT_KEISU_B,HP_MAX2.STD_WEIGHT_KEISU_B))*100,1),4,1) END) ";
            stb.append("             SUM(CASE WHEN                        " + bmi + " <= -30 THEN 1 ");
            stb.append("                      WHEN -30 <  " + bmi + " AND " + bmi + " <= -20 THEN 1 ");
            stb.append("                      WHEN -20 <  " + bmi + " AND " + bmi + " <   20 THEN 1 ");
            stb.append("                      WHEN  20 <= " + bmi + " AND " + bmi + " <   30 THEN 1 ");
            stb.append("                      WHEN  30 <= " + bmi + " AND " + bmi + " <   50 THEN 1 ");
            stb.append("                      WHEN  50 <= " + bmi + "                        THEN 1 ELSE 0 END) AS BMI_TOTAL, ");
            stb.append("             SUM(CASE WHEN                        " + bmi + " <= -30 THEN 1 ELSE 0 END) AS BMI1, ");
            stb.append("             SUM(CASE WHEN -30 <  " + bmi + " AND " + bmi + " <= -20 THEN 1 ELSE 0 END) AS BMI2, ");
            stb.append("             SUM(CASE WHEN -20 <  " + bmi + " AND " + bmi + " <   20 THEN 1 ELSE 0 END) AS BMI3, ");
            stb.append("             SUM(CASE WHEN  20 <= " + bmi + " AND " + bmi + " <   30 THEN 1 ELSE 0 END) AS BMI4, ");
            stb.append("             SUM(CASE WHEN  30 <= " + bmi + " AND " + bmi + " <   50 THEN 1 ELSE 0 END) AS BMI5, ");
            stb.append("             SUM(CASE WHEN  50 <= " + bmi + "                        THEN 1 ELSE 0 END) AS BMI6, ");
        } else {
            stb.append("             SUM(CASE WHEN  0 <= BMI AND BMI < 18 THEN 1 ");
            stb.append("                      WHEN 18 <= BMI AND BMI < 20 THEN 1 ");
            stb.append("                      WHEN 20 <= BMI AND BMI < 24 THEN 1 ");
            stb.append("                      WHEN 24 <= BMI AND BMI < 26 THEN 1 ");
            stb.append("                      WHEN 26 <= BMI              THEN 1 ELSE 0 END) AS BMI_TOTAL, ");
            stb.append("             SUM(CASE WHEN  0 <= BMI AND BMI < 18 THEN 1 ELSE 0 END) AS BMI1, ");
            stb.append("             SUM(CASE WHEN 18 <= BMI AND BMI < 20 THEN 1 ELSE 0 END) AS BMI2, ");
            stb.append("             SUM(CASE WHEN 20 <= BMI AND BMI < 24 THEN 1 ELSE 0 END) AS BMI3, ");
            stb.append("             SUM(CASE WHEN 24 <= BMI AND BMI < 26 THEN 1 ELSE 0 END) AS BMI4, ");
            stb.append("             SUM(CASE WHEN 26 <= BMI              THEN 1 ELSE 0 END) AS BMI5, ");
        }

        stb.append("             SUM(CASE WHEN VISION<>'X' THEN 1 ELSE 0 END) AS EYE_TOTAL, ");
        stb.append("             SUM(CASE WHEN VISION='A'  THEN 1 ELSE 0 END) AS EYE1, ");
        stb.append("             SUM(CASE WHEN VISION='B'  THEN 1 ELSE 0 END) AS EYE2, ");
        stb.append("             SUM(CASE WHEN VISION='C'  THEN 1 ELSE 0 END) AS EYE3, ");
        stb.append("             SUM(CASE WHEN VISION='D'  THEN 1 ELSE 0 END) AS EYE4, ");

        stb.append("             SUM(CASE WHEN REMAINADULTTOOTH=0 AND TREATEDADULTTOOTH=0 AND REMAINBABYTOOTH=0 AND TREATEDBABYTOOTH=0 THEN 1 ");
        stb.append("                      WHEN VALUE(REMAINADULTTOOTH,0)>0 OR VALUE(TREATEDADULTTOOTH,0)>0 OR VALUE(REMAINBABYTOOTH,0)>0 OR VALUE(TREATEDBABYTOOTH,0)>0 THEN 1 ");
        stb.append("                                                              ELSE 0 END)    AS TOOTH_TOTAL, ");
        stb.append("             SUM(CASE WHEN REMAINADULTTOOTH=0 AND TREATEDADULTTOOTH=0 AND REMAINBABYTOOTH=0 AND TREATEDBABYTOOTH=0 THEN 1 ");
        stb.append("                                                              ELSE 0 END)    AS TOOTH01, ");
        stb.append("             SUM(CASE WHEN VALUE(REMAINADULTTOOTH,0)>0 OR VALUE(TREATEDADULTTOOTH,0)>0 OR VALUE(REMAINBABYTOOTH,0)>0 OR VALUE(TREATEDBABYTOOTH,0)>0 THEN 1 ");
        stb.append("                                                              ELSE 0 END)    AS TOOTH02, ");
        stb.append("             SUM(CASE WHEN VALUE(REMAINADULTTOOTH,0)>0 OR VALUE(REMAINBABYTOOTH,0)>0 THEN 1 ");
        stb.append("                                                              ELSE 0 END)    AS TOOTH03, ");
        stb.append("             SUM(CASE WHEN VALUE(REMAINADULTTOOTH,0)>0  OR VALUE(REMAINBABYTOOTH,0)>0  THEN 0 ");
        stb.append("                      WHEN VALUE(TREATEDADULTTOOTH,0)>0 OR VALUE(TREATEDBABYTOOTH,0)>0 THEN 1 ");
        stb.append("                                                              ELSE 0 END)    AS TOOTH04, ");
        stb.append("             SUM(CASE WHEN VALUE(BRACK_BABYTOOTH,0)>0  THEN 1 ELSE 0 END)    AS TOOTH05, ");
        stb.append("             SUM(CASE WHEN VALUE(BRACK_ADULTTOOTH,0)>0 THEN 1 ELSE 0 END)    AS TOOTH06, ");
        stb.append("             SUM(CASE WHEN VALUE(LOSTADULTTOOTH,0)>0   THEN 1 ELSE 0 END)    AS TOOTH07, ");
        stb.append("             SUM(CASE WHEN JAWS_JOINTCD='02'  THEN 1 ELSE 0 END) AS TOOTH08, ");
        stb.append("             SUM(CASE WHEN JAWS_JOINTCD='03'  THEN 1 ELSE 0 END) AS TOOTH09, ");
        stb.append("             SUM(CASE WHEN JAWS_JOINTCD2='02' THEN 1 ELSE 0 END) AS TOOTH10, ");
        stb.append("             SUM(CASE WHEN JAWS_JOINTCD2='03' THEN 1 ELSE 0 END) AS TOOTH11, ");
        stb.append("             SUM(CASE WHEN PLAQUECD='02'      THEN 1 ELSE 0 END) AS TOOTH12, ");
        stb.append("             SUM(CASE WHEN PLAQUECD='03'      THEN 1 ELSE 0 END) AS TOOTH13, ");
        stb.append("             SUM(CASE WHEN GUMCD='02'         THEN 1 ELSE 0 END) AS TOOTH14, ");
        stb.append("             SUM(CASE WHEN GUMCD='03'         THEN 1 ELSE 0 END) AS TOOTH15 ");
        stb.append("         FROM ");
        stb.append("             ( ");
        stb.append("                 SELECT ");
        stb.append("                     W1.YEAR, ");
        stb.append("                     W1.SCHREGNO, ");
        stb.append("                     MAX(W1.GRADE) AS GRADE ");
        stb.append("                 FROM ");
        stb.append("                     SCHREG_REGD_DAT W1 ");
        stb.append("                 WHERE ");
        stb.append("                     W1.YEAR = '" + _param._year + "' ");
        stb.append("                     AND smallint(W1.GRADE) < 9 ");
        //異年令を除く
        if (_param._isAgeNozoku) {
            stb.append("                 AND NOT EXISTS(SELECT 'X' FROM NOT_AGE A1 WHERE A1.SCHREGNO = W1.SCHREGNO) ");
        }
        stb.append("                 GROUP BY ");
        stb.append("                     W1.YEAR, ");
        stb.append("                     W1.SCHREGNO ");
        stb.append("             ) T2 ");

        stb.append("             INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T2.SCHREGNO ");

        if (!_param._isKumamoto) {
            stb.append("         LEFT JOIN HEXAM_PHYSICAL_AVG HP ON HP.SEX=T3.SEX AND HP.NENREI_YEAR=YEAR(T2.YEAR || '-04-01' - T3.BIRTHDAY) AND HP.NENREI_MONTH=MONTH(T2.YEAR || '-04-01' - T3.BIRTHDAY)");
            stb.append("         LEFT JOIN ( SELECT I1.* FROM HEXAM_PHYSICAL_AVG I1 ");
            stb.append("                     INNER JOIN (SELECT SEX, NENREI_YEAR, MAX(NENREI_MONTH) AS MAX_NENREI_MONTH ");
            stb.append("                                 FROM HEXAM_PHYSICAL_AVG ");
            stb.append("                                 GROUP BY SEX, NENREI_YEAR");
            stb.append("                                ) I2 ON I2.SEX = I1.SEX ");
            stb.append("                                    AND I2.NENREI_YEAR = I1.NENREI_YEAR ");
            stb.append("                                    AND I2.MAX_NENREI_MONTH = I1.NENREI_MONTH ");
            stb.append("         ) HP_MAX ON HP_MAX.SEX = T3.SEX AND HP_MAX.NENREI_YEAR = YEAR(T2.YEAR || '-04-01' - T3.BIRTHDAY) ");
            stb.append("         LEFT JOIN ( SELECT I1.* FROM HEXAM_PHYSICAL_AVG I1 ");
            stb.append("                     INNER JOIN (SELECT SEX, MAX(NENREI_YEAR) AS MAX_NENREI_YEAR ");
            stb.append("                                 FROM HEXAM_PHYSICAL_AVG ");
            stb.append("                                 GROUP BY SEX");
            stb.append("                                ) I2 ON I2.SEX = I1.SEX ");
            stb.append("                                    AND I2.MAX_NENREI_YEAR = I1.NENREI_YEAR ");
            stb.append("                     INNER JOIN (SELECT SEX, NENREI_YEAR, MAX(NENREI_MONTH) AS MAX_NENREI_MONTH ");
            stb.append("                                 FROM HEXAM_PHYSICAL_AVG ");
            stb.append("                                 GROUP BY SEX, NENREI_YEAR");
            stb.append("                                ) I3 ON I3.SEX = I1.SEX ");
            stb.append("                                    AND I3.NENREI_YEAR = I1.NENREI_YEAR ");
            stb.append("                                    AND I3.MAX_NENREI_MONTH = I1.NENREI_MONTH ");
            stb.append("         ) HP_MAX2 ON HP_MAX2.SEX = T3.SEX ");
        }

        stb.append("             LEFT JOIN( ");
        stb.append("                 SELECT ");
        stb.append("                     SCHREGNO, ");
        stb.append("                     HEIGHT, ");
        stb.append("                     WEIGHT, ");
        stb.append("                     SITHEIGHT, ");
        stb.append("                     DECIMAL(ROUND(WEIGHT/HEIGHT/HEIGHT*10000,1),4,1) BMI ");
        stb.append("                 FROM ");
        stb.append("                     MEDEXAM_DET_DAT ");
        stb.append("                 WHERE ");
        stb.append("                     YEAR = '" + _param._year + "' ");
        stb.append("                     AND HEIGHT > 0 ");
        stb.append("                     AND WEIGHT > 0 ");
        stb.append("             ) BMI  ON BMI.SCHREGNO = T2.SCHREGNO ");

        stb.append("             LEFT JOIN( ");
        stb.append("                 SELECT ");
        stb.append("                     SCHREGNO, ");
        if (_param._isEyeMoji) {
            if (_param._isSeikatu) {
                stb.append("             CASE WHEN VALUE(" + rVisionMark + ", " + rBarevisionMark + ") IN ('A') AND VALUE(" + lVisionMark + ", " + lBarevisionMark + ") IN ('A') THEN 'A' ");
                stb.append("                  WHEN VALUE(" + rVisionMark + ", " + rBarevisionMark + ") IN ('A','B') AND VALUE(" + lVisionMark + ", " + lBarevisionMark + ") IN ('A','B') THEN 'B' ");
                stb.append("                  WHEN VALUE(" + rVisionMark + ", " + rBarevisionMark + ") IN ('A','B','C') AND VALUE(" + lVisionMark + ", " + lBarevisionMark + ") IN ('A','B','C') THEN 'C' ");
                stb.append("                  WHEN VALUE(" + rVisionMark + ", " + rBarevisionMark + ") IN ('A','B','C','D') AND VALUE(" + lVisionMark + ", " + lBarevisionMark + ") IN ('A','B','C','D') THEN 'D' ");
                stb.append("                  ELSE 'X' END ");
            } else
            if (_param._isRagan) {
                stb.append("             CASE WHEN " + rBarevisionMark + " IN ('A') AND " + lBarevisionMark + " IN ('A') THEN 'A' ");
                stb.append("                  WHEN " + rBarevisionMark + " IN ('A','B') AND " + lBarevisionMark + " IN ('A','B') THEN 'B' ");
                stb.append("                  WHEN " + rBarevisionMark + " IN ('A','B','C') AND " + lBarevisionMark + " IN ('A','B','C') THEN 'C' ");
                stb.append("                  WHEN " + rBarevisionMark + " IN ('A','B','C','D') AND " + lBarevisionMark + " IN ('A','B','C','D') THEN 'D' ");
                stb.append("                  ELSE 'X' END ");
            } else
            if (_param._isKyousei) {
                stb.append("             CASE WHEN " + rVisionMark + " IN ('A') AND " + lVisionMark + " IN ('A') THEN 'A' ");
                stb.append("                  WHEN " + rVisionMark + " IN ('A','B') AND " + lVisionMark + " IN ('A','B') THEN 'B' ");
                stb.append("                  WHEN " + rVisionMark + " IN ('A','B','C') AND " + lVisionMark + " IN ('A','B','C') THEN 'C' ");
                stb.append("                  WHEN " + rVisionMark + " IN ('A','B','C','D') AND " + lVisionMark + " IN ('A','B','C','D') THEN 'D' ");
                stb.append("                  ELSE 'X' END ");
            }
        } else {
            if (_param._isSeikatu) {
                stb.append("             CASE WHEN VALUE(R_VISION, R_BAREVISION) >= '1.0' AND VALUE(L_VISION, L_BAREVISION) >= '1.0' THEN 'A' ");
                stb.append("                  WHEN VALUE(R_VISION, R_BAREVISION) >= '0.7' AND VALUE(L_VISION, L_BAREVISION) >= '0.7' THEN 'B' ");
                stb.append("                  WHEN VALUE(R_VISION, R_BAREVISION) >= '0.3' AND VALUE(L_VISION, L_BAREVISION) >= '0.3' THEN 'C' ");
                stb.append("                  WHEN VALUE(R_VISION, R_BAREVISION) >  '0.0' AND VALUE(L_VISION, L_BAREVISION) >  '0.0' THEN 'D' ");
                stb.append("                  ELSE 'X' END ");
            } else
            if (_param._isRagan) {
                stb.append("             CASE WHEN R_BAREVISION >= '1.0' AND L_BAREVISION >= '1.0' THEN 'A' ");
                stb.append("                  WHEN R_BAREVISION >= '0.7' AND L_BAREVISION >= '0.7' THEN 'B' ");
                stb.append("                  WHEN R_BAREVISION >= '0.3' AND L_BAREVISION >= '0.3' THEN 'C' ");
                stb.append("                  WHEN R_BAREVISION >  '0.0' AND L_BAREVISION >  '0.0' THEN 'D' ");
                stb.append("                  ELSE 'X' END ");
            } else
            if (_param._isKyousei) {
                stb.append("             CASE WHEN R_VISION >= '1.0' AND L_VISION >= '1.0' THEN 'A' ");
                stb.append("                  WHEN R_VISION >= '0.7' AND L_VISION >= '0.7' THEN 'B' ");
                stb.append("                  WHEN R_VISION >= '0.3' AND L_VISION >= '0.3' THEN 'C' ");
                stb.append("                  WHEN R_VISION >  '0.0' AND L_VISION >  '0.0' THEN 'D' ");
                stb.append("                  ELSE 'X' END ");
            }
        }
        stb.append("                       AS VISION ");
        stb.append("                 FROM ");
        stb.append("                     MEDEXAM_DET_DAT ");
        if ("1".equals(_param._VisionMarkSaveValueCd)) {
            stb.append("                     LEFT JOIN NAME_MST RVM_F017 ON RVM_F017.NAMECD1 = 'F017' ");
            stb.append("                                                AND RVM_F017.NAMECD2 = R_VISION_MARK ");
            stb.append("                     LEFT JOIN NAME_MST RBVM_F017 ON RBVM_F017.NAMECD1 = 'F017' ");
            stb.append("                                                 AND RBVM_F017.NAMECD2 = R_BAREVISION_MARK ");
            stb.append("                     LEFT JOIN NAME_MST LVM_F017 ON LVM_F017.NAMECD1 = 'F017' ");
            stb.append("                                                AND LVM_F017.NAMECD2 = L_VISION_MARK ");
            stb.append("                     LEFT JOIN NAME_MST LBVM_F017 ON LBVM_F017.NAMECD1 = 'F017' ");
            stb.append("                                                 AND LBVM_F017.NAMECD2 = L_BAREVISION_MARK ");
        }
        stb.append("                 WHERE ");
        stb.append("                     YEAR = '" + _param._year + "' ");
        stb.append("                     AND ( ");
        if (_param._isEyeMoji) {
            stb.append("                     ((R_BAREVISION_MARK IS NOT NULL AND R_BAREVISION_MARK != '') OR (L_BAREVISION_MARK IS NOT NULL AND L_BAREVISION_MARK != '')) ");
            stb.append("                  OR ((R_VISION_MARK     IS NOT NULL AND R_VISION_MARK     != '') OR (L_VISION_MARK     IS NOT NULL AND L_VISION_MARK     != '')) ");
        } else {
            stb.append("                     ((R_BAREVISION      IS NOT NULL AND R_BAREVISION      != '') OR (L_BAREVISION      IS NOT NULL AND L_BAREVISION      != '')) ");
            stb.append("                  OR ((R_VISION          IS NOT NULL AND R_VISION          != '') OR (L_VISION          IS NOT NULL AND L_VISION          != '')) ");
        }
        stb.append("                         ) ");
        stb.append("             ) EYE  ON EYE.SCHREGNO = T2.SCHREGNO ");

        stb.append("             LEFT JOIN( ");
        stb.append("                 SELECT ");
        stb.append("                     SCHREGNO, ");
        stb.append("                     REMAINADULTTOOTH, ");
        stb.append("                     REMAINBABYTOOTH, ");
        stb.append("                     TREATEDADULTTOOTH, ");
        stb.append("                     TREATEDBABYTOOTH, ");
        stb.append("                     BRACK_ADULTTOOTH, ");
        stb.append("                     BRACK_BABYTOOTH, ");
        stb.append("                     LOSTADULTTOOTH, ");
        stb.append("                     JAWS_JOINTCD, ");
        stb.append("                     JAWS_JOINTCD2, ");
        stb.append("                     PLAQUECD, ");
        stb.append("                     GUMCD ");
        stb.append("                 FROM ");
        stb.append("                     MEDEXAM_TOOTH_DAT ");
        stb.append("                 WHERE ");
        stb.append("                         YEAR = '" + _param._year + "' ");
        stb.append("                     AND (ADULTTOOTH IS NOT NULL ");
        stb.append("                             OR REMAINADULTTOOTH IS NOT NULL ");
        stb.append("                             OR REMAINBABYTOOTH IS NOT NULL ");
        stb.append("                             OR TREATEDADULTTOOTH IS NOT NULL ");
        stb.append("                             OR TREATEDBABYTOOTH IS NOT NULL ");
        stb.append("                             OR BRACK_ADULTTOOTH IS NOT NULL ");
        stb.append("                             OR BRACK_BABYTOOTH IS NOT NULL ");
        stb.append("                             OR LOSTADULTTOOTH IS NOT NULL ");
        stb.append("                             OR JAWS_JOINTCD IS NOT NULL ");
        stb.append("                             OR JAWS_JOINTCD2 IS NOT NULL ");
        stb.append("                             OR PLAQUECD IS NOT NULL ");
        stb.append("                             OR GUMCD IS NOT NULL) ");
        stb.append("             ) TOOTH  ON TOOTH.SCHREGNO = T2.SCHREGNO ");

        stb.append("         GROUP BY ");
        stb.append("         GROUPING SETS ");
        stb.append("             ((T2.GRADE,T3.SEX),(T3.SEX),(T2.GRADE),()) ");
        stb.append("     ) T1 ");

        stb.append("     LEFT JOIN GDAT G1 ON G1.GRADE = T1.GRADE ");

        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.SEX ");

        return stb.toString();
    }

    private class GradeMedexam {
        private final String _grade;
        private final String _sex;
        private final String _gradeName;

        private final String _height;
        private final String _weight;
        private final String _sitheight;
        private final String _bmiTotal;
        private final String _bmi1;
        private final String _bmi2;
        private final String _bmi3;
        private final String _bmi4;
        private final String _bmi5;
        private final String _bmi6;
        private final String _bmi1p;
        private final String _bmi2p;
        private final String _bmi3p;
        private final String _bmi4p;
        private final String _bmi5p;
        private final String _bmi6p;

        private final String _eyeTotal;
        private final String _eye1;
        private final String _eye2;
        private final String _eye3;
        private final String _eye4;
        private final String _eye1p;
        private final String _eye2p;
        private final String _eye3p;
        private final String _eye4p;

        private final String _toothTotal;
        private final String _tooth01;
        private final String _tooth02;
        private final String _tooth03;
        private final String _tooth04;
        private final String _tooth05;
        private final String _tooth06;
        private final String _tooth07;
        private final String _tooth08;
        private final String _tooth09;
        private final String _tooth10;
        private final String _tooth11;
        private final String _tooth12;
        private final String _tooth13;
        private final String _tooth14;
        private final String _tooth15;
        private final String _tooth01p;
        private final String _tooth02p;
        private final String _tooth03p;
        private final String _tooth04p;
        private final String _tooth05p;
        private final String _tooth06p;
        private final String _tooth07p;
        private final String _tooth08p;
        private final String _tooth09p;
        private final String _tooth10p;
        private final String _tooth11p;
        private final String _tooth12p;
        private final String _tooth13p;
        private final String _tooth14p;
        private final String _tooth15p;

        public GradeMedexam(
                final String grade,
                final String sex,
                final String gradeName,

                final String height,
                final String weight,
                final String sitheight,
                final String bmiTotal,
                final String bmi1,
                final String bmi2,
                final String bmi3,
                final String bmi4,
                final String bmi5,
                final String bmi6,
                final String bmi1p,
                final String bmi2p,
                final String bmi3p,
                final String bmi4p,
                final String bmi5p,
                final String bmi6p,

                final String eyeTotal,
                final String eye1,
                final String eye2,
                final String eye3,
                final String eye4,
                final String eye1p,
                final String eye2p,
                final String eye3p,
                final String eye4p,

                final String toothTotal,
                final String tooth01,
                final String tooth02,
                final String tooth03,
                final String tooth04,
                final String tooth05,
                final String tooth06,
                final String tooth07,
                final String tooth08,
                final String tooth09,
                final String tooth10,
                final String tooth11,
                final String tooth12,
                final String tooth13,
                final String tooth14,
                final String tooth15,
                final String tooth01p,
                final String tooth02p,
                final String tooth03p,
                final String tooth04p,
                final String tooth05p,
                final String tooth06p,
                final String tooth07p,
                final String tooth08p,
                final String tooth09p,
                final String tooth10p,
                final String tooth11p,
                final String tooth12p,
                final String tooth13p,
                final String tooth14p,
                final String tooth15p
        ) {
            _grade = grade;
            _sex = sex;
            _gradeName = gradeName;

            _height = height;
            _weight = weight;
            _sitheight = sitheight;
            _bmiTotal = bmiTotal;
            _bmi1 = bmi1;
            _bmi2 = bmi2;
            _bmi3 = bmi3;
            _bmi4 = bmi4;
            _bmi5 = bmi5;
            _bmi6 = bmi6;
            _bmi1p = bmi1p;
            _bmi2p = bmi2p;
            _bmi3p = bmi3p;
            _bmi4p = bmi4p;
            _bmi5p = bmi5p;
            _bmi6p = bmi6p;

            _eyeTotal = eyeTotal;
            _eye1 = eye1;
            _eye2 = eye2;
            _eye3 = eye3;
            _eye4 = eye4;
            _eye1p = eye1p;
            _eye2p = eye2p;
            _eye3p = eye3p;
            _eye4p = eye4p;

            _toothTotal = toothTotal;
            _tooth01 = tooth01;
            _tooth02 = tooth02;
            _tooth03 = tooth03;
            _tooth04 = tooth04;
            _tooth05 = tooth05;
            _tooth06 = tooth06;
            _tooth07 = tooth07;
            _tooth08 = tooth08;
            _tooth09 = tooth09;
            _tooth10 = tooth10;
            _tooth11 = tooth11;
            _tooth12 = tooth12;
            _tooth13 = tooth13;
            _tooth14 = tooth14;
            _tooth15 = tooth15;
            _tooth01p = tooth01p;
            _tooth02p = tooth02p;
            _tooth03p = tooth03p;
            _tooth04p = tooth04p;
            _tooth05p = tooth05p;
            _tooth06p = tooth06p;
            _tooth07p = tooth07p;
            _tooth08p = tooth08p;
            _tooth09p = tooth09p;
            _tooth10p = tooth10p;
            _tooth11p = tooth11p;
            _tooth12p = tooth12p;
            _tooth13p = tooth13p;
            _tooth14p = tooth14p;
            _tooth15p = tooth15p;
        }
    }

    private List getGradeAgeList(final DB2UDB db2) throws SQLException  {
        final List rtnList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getGradeAgeSql();
            log.debug("getGradeAgeSql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            int age = 0;
            String preSchoolKind = "";
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String schoolKind = rs.getString("SCHOOL_KIND");

                if (!preSchoolKind.equals(schoolKind)) {
                    if ("P".equals(schoolKind)) age = 6;
                    if ("J".equals(schoolKind)) age = 12;
                    if ("H".equals(schoolKind)) age = 15;
                    preSchoolKind = schoolKind;
                }

                if (age == 0) continue;

                log.debug("grade = " + grade + ", age = " + age);
                final GradeAge gradeAge = new GradeAge(grade, String.valueOf(age));
                rtnList.add(gradeAge);

                age++;
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtnList;
    }

    private String getGradeAgeSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     GRADE, ");
        stb.append("     SCHOOL_KIND ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_GDAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        stb.append(" ORDER BY ");
        stb.append("     SCHOOL_KIND DESC, ");
        stb.append("     GRADE ");
        return stb.toString();
    }

    private class GradeAge {
        private final String _grade;
        private final String _age;

        public GradeAge(
                final String grade,
                final String age
        ) {
            _grade = grade;
            _age = age;
        }
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _isAgeNozoku;
        private final boolean _isEyeMoji;
        private final boolean _isSeikatu;
        private final boolean _isRagan;
        private final boolean _isKyousei;
        private final String _gname;
        private boolean _seirekiFlg;
        private final String _nendo;
        private final String _today;
        private final boolean _isKumamoto;
        private final String _VisionMarkSaveValueCd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _VisionMarkSaveValueCd = request.getParameter("VisionMark_SaveValueCd");
            //異年令を除くか？
            final String ageNozoku = request.getParameter("AGE_NOZOKU");
            _isAgeNozoku = null != ageNozoku;
            //視力(1:文字 2:数値)
            final String eyeMoji = request.getParameter("EYE_MOJI");
            _isEyeMoji = "1".equals(eyeMoji);
            //視力(1:生活 2:裸眼 3:矯正)
            final String eyeKubun = request.getParameter("EYE_KUBUN");
            _isSeikatu = "1".equals(eyeKubun);
            _isRagan = "2".equals(eyeKubun);
            _isKyousei = "3".equals(eyeKubun);
            _gname = getGradeName(db2);
            setSeirekiFlg(db2);
            _nendo = getPrintYear(db2, _year);
            _today = getPrintDate(db2, _ctrlDate);
            _isKumamoto = "kumamoto".equals(getNamemstZ010(db2));
        }

        private String getGradeName(final DB2UDB db2) throws SQLException {
            String rtn = "";
            final String sql = "SELECT SCHOOLDIV FROM SCHOOL_MST WHERE YEAR = '" + _year + "'";
            ResultSet rs = null;
            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    String schooldiv = rs.getString("SCHOOLDIV");
                    rtn = "0".equals(schooldiv) ? "学年" : "年次";
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private void setSeirekiFlg(final DB2UDB db2) {
            try {
                _seirekiFlg = false;
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while( rs.next() ){
                    if (rs.getString("NAME1").equals("2")) _seirekiFlg = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
        }

        private String getNamemstZ010(final DB2UDB db2) {
            String namemstZ010Name1 = "";
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    namemstZ010Name1 = rs.getString("NAME1");
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
            return namemstZ010Name1;
        }

        private String getPrintDate(final DB2UDB db2, final String date) {
            if (null != date) {
                if (_seirekiFlg) {
                    return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
                } else {
                    return KNJ_EditDate.h_format_JP(db2, date);
                }
            } else {
                return "";
            }
        }

        private String getPrintYear(final DB2UDB db2, final String year) {
            if (null == year) {
                return "";
            }
            if (_seirekiFlg) {
                return year + "年度";
            } else {
                return KNJ_EditDate.gengou(db2, Integer.parseInt(year)) + "年度";
            }
        }

    }
}

// eof
