/*
 * $Id: d727292ae8367cb5a0f260cfd9ccebb334c0372a $
 *
 * 作成日: 2013/05/07
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * KNJF310P 歯科検診結果一覧
 * @version $Id: d727292ae8367cb5a0f260cfd9ccebb334c0372a $
 */
public class KNJF310P {

    private static final Log log = LogFactory.getLog(KNJF310P.class);

    private static final String PRGID_KNJF310 = "KNJF310";
    private static final String PRGID_KNJF310P = "KNJF310P";
    
    private boolean _hasData;

    private Param _param;

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
	        response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain2(db2, svf);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain2(final DB2UDB db2, final Vrw32alp svf) {
		final List list = getMedexamDiseaseAddition4List(db2);
		if (list.isEmpty()) {
		    return;
		}
        
		svf.VrSetForm("KNJF310P.frm", 4);

        String gradeFlg = "";
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final MedexamDiseaseAddition5 mda5 = (MedexamDiseaseAddition5) it.next();

            if (!gradeFlg.equals(mda5._grade)) {
                if (!"".equals(gradeFlg)) {
                    svf.VrEndRecord();
                }
                gradeFlg = mda5._grade;
            }

            svf.VrsOut("TITLE", _param._nendo + "　歯科保健結果報告票");
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); // 日付

            if (!"99".equals(mda5._grade)) {
                svf.VrsOut("GRADE_NAME", mda5._gradeName);
            }

            String totalFlg = ("99".equals(mda5._grade)) ? "TOTAL_" : "";
            String sexFlg = ("9".equals(mda5._sex)) ? "3" : mda5._sex;

            svf.VrsOut(totalFlg + "EXAMNUM"         + sexFlg, mda5._remark101);
            svf.VrsOut(totalFlg + "NO_CAVITY"       + sexFlg, mda5._remark102);
            svf.VrsOut(totalFlg + "RES_EX_CAV"      + sexFlg, mda5._remark103);
            svf.VrsOut(totalFlg + "RES_TREATEX_CAV" + sexFlg, mda5._remark104);
            svf.VrsOut(totalFlg + "RES_COMPEX_CAV"  + sexFlg, mda5._remark105);
            svf.VrsOut(totalFlg + "RES_CA_BABY"     + sexFlg, mda5._remark106);
            svf.VrsOut(totalFlg + "RES_OBS"         + sexFlg, mda5._remark107);
            svf.VrsOut(totalFlg + "RES_LOST"        + sexFlg, mda5._remark108);
            svf.VrsOut(totalFlg + "ROW_NO_PROB"     + sexFlg, mda5._remark201);
            svf.VrsOut(totalFlg + "ROW_OBS"         + sexFlg, mda5._remark202);
            svf.VrsOut(totalFlg + "ROW_DIAG"        + sexFlg, mda5._remark203);
            svf.VrsOut(totalFlg + "JAW_NO_PROB"     + sexFlg, mda5._remark204);
            svf.VrsOut(totalFlg + "JAW_OBS"         + sexFlg, mda5._remark205);
            svf.VrsOut(totalFlg + "JAW_DIAG"        + sexFlg, mda5._remark206);
            svf.VrsOut(totalFlg + "PLAQ_NO"         + sexFlg, mda5._remark207);
            svf.VrsOut(totalFlg + "PLAQ_LITTLE"     + sexFlg, mda5._remark208);
            svf.VrsOut(totalFlg + "PLAQ_MORE"       + sexFlg, mda5._remark209);
            svf.VrsOut(totalFlg + "GUM_NO_PROB"     + sexFlg, mda5._remark210);
            svf.VrsOut(totalFlg + "GUM_OBS"         + sexFlg, mda5._remark211);
            svf.VrsOut(totalFlg + "GUM_DIAG"        + sexFlg, mda5._remark212);

            _hasData = true;
        }
        if (!"".equals(gradeFlg)) {
            svf.VrEndRecord();
        }
    }
    
    
    private class MedexamDiseaseAddition5 {
        final String _grade;
        final String _sex;
        final String _gradeName;
        final String _sexName;
        final String _remark101;
        final String _remark102;
        final String _remark103;
        final String _remark104;
        final String _remark105;
        final String _remark106;
        final String _remark107;
        final String _remark108;
        final String _remark201;
        final String _remark202;
        final String _remark203;
        final String _remark204;
        final String _remark205;
        final String _remark206;
        final String _remark207;
        final String _remark208;
        final String _remark209;
        final String _remark210;
        final String _remark211;
        final String _remark212;

        MedexamDiseaseAddition5(
                final String grade,
                final String sex,
                final String gradeName,
                final String sexName,
                final String remark101,
                final String remark102,
                final String remark103,
                final String remark104,
                final String remark105,
                final String remark106,
                final String remark107,
                final String remark108,
                final String remark201,
                final String remark202,
                final String remark203,
                final String remark204,
                final String remark205,
                final String remark206,
                final String remark207,
                final String remark208,
                final String remark209,
                final String remark210,
                final String remark211,
                final String remark212
        ) {
            _grade = grade;
            _sex = sex;
            _gradeName = gradeName;
            _sexName = sexName;
            _remark101 = remark101;
            _remark102 = remark102;
            _remark103 = remark103;
            _remark104 = remark104;
            _remark105 = remark105;
            _remark106 = remark106;
            _remark107 = remark107;
            _remark108 = remark108;
            _remark201 = remark201;
            _remark202 = remark202;
            _remark203 = remark203;
            _remark204 = remark204;
            _remark205 = remark205;
            _remark206 = remark206;
            _remark207 = remark207;
            _remark208 = remark208;
            _remark209 = remark209;
            _remark210 = remark210;
            _remark211 = remark211;
            _remark212 = remark212;
        }
    }

    private List getMedexamDiseaseAddition4List(final DB2UDB db2) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final MedexamDiseaseAddition5 mda5 = new MedexamDiseaseAddition5(
                        rs.getString("GRADE"),
                        rs.getString("SEX"),
                        rs.getString("GRADE_NAME"),
                        rs.getString("SEX_NAME"),
                        rs.getString("REMARK101"),
                        rs.getString("REMARK102"),
                        rs.getString("REMARK103"),
                        rs.getString("REMARK104"),
                        rs.getString("REMARK105"),
                        rs.getString("REMARK106"),
                        rs.getString("REMARK107"),
                        rs.getString("REMARK108"),
                        rs.getString("REMARK201"),
                        rs.getString("REMARK202"),
                        rs.getString("REMARK203"),
                        rs.getString("REMARK204"),
                        rs.getString("REMARK205"),
                        rs.getString("REMARK206"),
                        rs.getString("REMARK207"),
                        rs.getString("REMARK208"),
                        rs.getString("REMARK209"),
                        rs.getString("REMARK210"),
                        rs.getString("REMARK211"),
                        rs.getString("REMARK212"));
                list.add(mda5);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    public String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_GRADE (GRADE, GRADE_NAME) AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.GRADE, ");
        stb.append("         T3.GRADE_NAME1 ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT T1 ");
        stb.append("         INNER JOIN SCHREG_BASE_MST T2 ");
        stb.append("             ON  T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         INNER JOIN SCHREG_REGD_GDAT T3 ");
        stb.append("             ON  T3.YEAR     = T1.YEAR ");
        stb.append("             AND T3.GRADE    = T1.GRADE ");
        stb.append("     WHERE ");
        stb.append("             T1.YEAR     = '" + _param._year + "' ");
        stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     GROUP BY ");
        stb.append("         T1.GRADE, ");
        stb.append("         T3.GRADE_NAME1 ");
        stb.append("     UNION ALL ");
        stb.append("     VALUES('99', '合計') ");
        stb.append("     ) ");
        stb.append(" , T_SEX (SEX, SEX_NAME) AS ( ");
        stb.append("     SELECT ");
        stb.append("         NAMECD2, ");
        stb.append("         ABBV1 ");
        stb.append("     FROM ");
        stb.append("         NAME_MST ");
        stb.append("     WHERE ");
        stb.append("         NAMECD1 = 'Z002' ");
        stb.append("     UNION ALL ");
        stb.append("     VALUES('9', '合計') ");
        stb.append("     ) ");
        stb.append(" , T_GRADE_SEX AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.*, ");
        stb.append("         T2.* ");
        stb.append("     FROM ");
        stb.append("         T_GRADE T1, ");
        stb.append("         T_SEX T2 ");
        stb.append("     WHERE ");
        stb.append("         T1.GRADE = '99' OR T2.SEX != '9' ");
        stb.append("     ) ");
        stb.append(" , T_ADDITION AS ( ");
        stb.append("     SELECT ");
        stb.append("         GRADE, ");
        stb.append("         SEX, ");
        stb.append("         SUM(CASE WHEN REMARK_DIV = '101' THEN COUNT ELSE 0 END) AS REMARK101, ");
        stb.append("         SUM(CASE WHEN REMARK_DIV = '102' THEN COUNT ELSE 0 END) AS REMARK102, ");
        stb.append("         SUM(CASE WHEN REMARK_DIV = '103' THEN COUNT ELSE 0 END) AS REMARK103, ");
        stb.append("         SUM(CASE WHEN REMARK_DIV = '104' THEN COUNT ELSE 0 END) AS REMARK104, ");
        stb.append("         SUM(CASE WHEN REMARK_DIV = '105' THEN COUNT ELSE 0 END) AS REMARK105, ");
        stb.append("         SUM(CASE WHEN REMARK_DIV = '106' THEN COUNT ELSE 0 END) AS REMARK106, ");
        stb.append("         SUM(CASE WHEN REMARK_DIV = '107' THEN COUNT ELSE 0 END) AS REMARK107, ");
        stb.append("         SUM(CASE WHEN REMARK_DIV = '108' THEN COUNT ELSE 0 END) AS REMARK108, ");
        stb.append("         SUM(CASE WHEN REMARK_DIV = '201' THEN COUNT ELSE 0 END) AS REMARK201, ");
        stb.append("         SUM(CASE WHEN REMARK_DIV = '202' THEN COUNT ELSE 0 END) AS REMARK202, ");
        stb.append("         SUM(CASE WHEN REMARK_DIV = '203' THEN COUNT ELSE 0 END) AS REMARK203, ");
        stb.append("         SUM(CASE WHEN REMARK_DIV = '204' THEN COUNT ELSE 0 END) AS REMARK204, ");
        stb.append("         SUM(CASE WHEN REMARK_DIV = '205' THEN COUNT ELSE 0 END) AS REMARK205, ");
        stb.append("         SUM(CASE WHEN REMARK_DIV = '206' THEN COUNT ELSE 0 END) AS REMARK206, ");
        stb.append("         SUM(CASE WHEN REMARK_DIV = '207' THEN COUNT ELSE 0 END) AS REMARK207, ");
        stb.append("         SUM(CASE WHEN REMARK_DIV = '208' THEN COUNT ELSE 0 END) AS REMARK208, ");
        stb.append("         SUM(CASE WHEN REMARK_DIV = '209' THEN COUNT ELSE 0 END) AS REMARK209, ");
        stb.append("         SUM(CASE WHEN REMARK_DIV = '210' THEN COUNT ELSE 0 END) AS REMARK210, ");
        stb.append("         SUM(CASE WHEN REMARK_DIV = '211' THEN COUNT ELSE 0 END) AS REMARK211, ");
        stb.append("         SUM(CASE WHEN REMARK_DIV = '212' THEN COUNT ELSE 0 END) AS REMARK212 ");
        stb.append("     FROM ");
        stb.append("         MEDEXAM_DISEASE_ADDITION5_DAT ");
        stb.append("     WHERE ");
        stb.append("         EDBOARD_SCHOOLCD = '" + _param._knjSchoolMst._kyoikuIinkaiSchoolcd + "' AND ");
        stb.append("         SHORI_CD = '01' AND ");
        stb.append("         YEAR = '" + _param._year + "' ");
        stb.append("     GROUP BY ");
        stb.append("         GRADE, ");
        stb.append("         SEX ");
        stb.append("     ) ");

        stb.append(" SELECT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.SEX, ");
        stb.append("     T1.GRADE_NAME, ");
        stb.append("     T1.SEX_NAME, ");
        stb.append("     L1.REMARK101, ");
        stb.append("     L1.REMARK102, ");
        stb.append("     L1.REMARK103, ");
        stb.append("     L1.REMARK104, ");
        stb.append("     L1.REMARK105, ");
        stb.append("     L1.REMARK106, ");
        stb.append("     L1.REMARK107, ");
        stb.append("     L1.REMARK108, ");
        stb.append("     L1.REMARK201, ");
        stb.append("     L1.REMARK202, ");
        stb.append("     L1.REMARK203, ");
        stb.append("     L1.REMARK204, ");
        stb.append("     L1.REMARK205, ");
        stb.append("     L1.REMARK206, ");
        stb.append("     L1.REMARK207, ");
        stb.append("     L1.REMARK208, ");
        stb.append("     L1.REMARK209, ");
        stb.append("     L1.REMARK210, ");
        stb.append("     L1.REMARK211, ");
        stb.append("     L1.REMARK212 ");
        stb.append(" FROM ");
        stb.append("     T_GRADE_SEX T1 ");
        stb.append("     LEFT JOIN T_ADDITION L1 ON L1.GRADE = T1.GRADE AND L1.SEX = T1.SEX ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.SEX ");
        return stb.toString();
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
        final String _year;
        final String _semester;
        final String _loginDate;
        final String _prgid;
        final KNJSchoolMst _knjSchoolMst;
        final String _nendo;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _prgid = request.getParameter("PRGID");
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _loginDate = request.getParameter("CTRL_DATE");
            _knjSchoolMst = new KNJSchoolMst(db2, _year);
            final String gengou = nao_package.KenjaProperties.gengou(Integer.parseInt(_year));
            _nendo = gengou + "年度";
        }
    }
}

// eof

