/*
 * $Id: 2b08c506f6cd3859ed0ffd68d9ff7880f3655530 $
 *
 * 作成日: 2013/05/13
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 体位疾病以上報告 KNJF301P
 */
public class KNJF301P {

    private static final Log log = LogFactory.getLog(KNJF301P.class);

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

            printMain(db2, svf);
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJF301P.frm", 4);
		final List list = getList(db2);
		String beforeGrade = null;
		for (int line = 0; line < list.size(); line++) {
			final MedexamDiseaseAddition1 mda1 = (MedexamDiseaseAddition1) list.get(line);

			if (beforeGrade != null && !beforeGrade.equals(mda1._grade)) {
	            svf.VrEndRecord();
			}

			svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度　" + "体位疾病異常報告票"); // タイトル
			svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate)); // 作成日
            String tot = "";
			if ("99".equals(mda1._grade)) {
                tot = "TOTAL_";
			} else {
                svf.VrsOut("GRADE_NAME", mda1._gradeName1); // 学年名
			}

            final String i = "1".equals(mda1._sex) ? "1" : "2".equals(mda1._sex) ? "2" : "9".equals(mda1._sex) ? "3" : null;
			svf.VrsOut(tot + "NUT" + i + "_1", mda1._nutritioncd01); // 栄養状態
			svf.VrsOut(tot + "NUT" + i + "_2", mda1._nutritioncd02); // 栄養状態
			svf.VrsOut(tot + "NUT" + i + "_3", mda1._nutritioncd03); // 栄養状態
			svf.VrsOut(tot + "SPINAL" + i + "_1", mda1._spineribcd01); // 脊柱
			svf.VrsOut(tot + "SPINAL" + i + "_2", mda1._spineribcd02); // 脊柱
			svf.VrsOut(tot + "SPINAL" + i + "_3", mda1._spineribcd03); // 脊柱
			svf.VrsOut(tot + "SPINAL" + i + "_4", mda1._spineribcd99); // 脊柱
			svf.VrsOut(tot + "SKIN" + i + "_1", mda1._skindiseasecd01); // 皮膚
			svf.VrsOut(tot + "SKIN" + i + "_2", mda1._skindiseasecd02); // 皮膚
			svf.VrsOut(tot + "SKIN" + i + "_3", mda1._skindiseasecd03); // 皮膚
			svf.VrsOut(tot + "SKIN" + i + "_4", mda1._skindiseasecd99); // 皮膚
			svf.VrsOut(tot + "OTHER" + i + "_1", mda1._otherdiseasecd01); // その他疾患
			svf.VrsOut(tot + "OTHER" + i + "_2", mda1._otherdiseasecd02); // その他疾患
			svf.VrsOut(tot + "OTHER" + i + "_3", mda1._otherdiseasecd03); // その他疾患
			svf.VrsOut(tot + "OTHER" + i + "_4", mda1._otherdiseasecd04); // その他疾患
			svf.VrsOut(tot + "OTHER" + i + "_5", mda1._otherdiseasecd05); // その他疾患
			svf.VrsOut(tot + "OTHER" + i + "_6", mda1._otherdiseasecd99); // その他疾患
			beforeGrade = mda1._grade;
			_hasData = true;
		}
		if (null != beforeGrade) {
		    svf.VrEndRecord();
		}
    }

    private class MedexamDiseaseAddition1 {
        final String _grade;
        final String _gradeName1;
        final String _sex;
        final String _sexname;
        final String _nutritioncd01;
        final String _nutritioncd02;
        final String _nutritioncd03;
        final String _spineribcd01;
        final String _spineribcd02;
        final String _spineribcd03;
        final String _spineribcd99;
        final String _skindiseasecd01;
        final String _skindiseasecd02;
        final String _skindiseasecd03;
        final String _skindiseasecd99;
        final String _otherdiseasecd01;
        final String _otherdiseasecd02;
        final String _otherdiseasecd03;
        final String _otherdiseasecd04;
        final String _otherdiseasecd05;
        final String _otherdiseasecd99;

        MedexamDiseaseAddition1(
            final String grade,
            final String gradeName1,
            final String sex,
            final String sexname,
            final String nutritioncd01,
            final String nutritioncd02,
            final String nutritioncd03,
            final String spineribcd01,
            final String spineribcd02,
            final String spineribcd03,
            final String spineribcd99,
            final String skindiseasecd01,
            final String skindiseasecd02,
            final String skindiseasecd03,
            final String skindiseasecd99,
            final String otherdiseasecd01,
            final String otherdiseasecd02,
            final String otherdiseasecd03,
            final String otherdiseasecd04,
            final String otherdiseasecd05,
            final String otherdiseasecd99
        ) {
            _grade = grade;
            _gradeName1 = gradeName1;
            _sex = sex;
            _sexname = sexname;
            _nutritioncd01 = nutritioncd01;
            _nutritioncd02 = nutritioncd02;
            _nutritioncd03 = nutritioncd03;
            _spineribcd01 = spineribcd01;
            _spineribcd02 = spineribcd02;
            _spineribcd03 = spineribcd03;
            _spineribcd99 = spineribcd99;
            _skindiseasecd01 = skindiseasecd01;
            _skindiseasecd02 = skindiseasecd02;
            _skindiseasecd03 = skindiseasecd03;
            _skindiseasecd99 = skindiseasecd99;
            _otherdiseasecd01 = otherdiseasecd01;
            _otherdiseasecd02 = otherdiseasecd02;
            _otherdiseasecd03 = otherdiseasecd03;
            _otherdiseasecd04 = otherdiseasecd04;
            _otherdiseasecd05 = otherdiseasecd05;
            _otherdiseasecd99 = otherdiseasecd99;
        }
    }

    public List getList(final DB2UDB db2) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String gradeName1 = rs.getString("GRADE_NAME1");
                final String sex = rs.getString("SEX");
                final String sexname = rs.getString("SEXNAME");
                final String nutritioncd01 = rs.getString("NUTRITIONCD01");
                final String nutritioncd02 = rs.getString("NUTRITIONCD02");
                final String nutritioncd03 = rs.getString("NUTRITIONCD03");
                final String spineribcd01 = rs.getString("SPINERIBCD01");
                final String spineribcd02 = rs.getString("SPINERIBCD02");
                final String spineribcd03 = rs.getString("SPINERIBCD03");
                final String spineribcd99 = rs.getString("SPINERIBCD99");
                final String skindiseasecd01 = rs.getString("SKINDISEASECD01");
                final String skindiseasecd02 = rs.getString("SKINDISEASECD02");
                final String skindiseasecd03 = rs.getString("SKINDISEASECD03");
                final String skindiseasecd99 = rs.getString("SKINDISEASECD99");
                final String otherdiseasecd01 = rs.getString("OTHERDISEASECD01");
                final String otherdiseasecd02 = rs.getString("OTHERDISEASECD02");
                final String otherdiseasecd03 = rs.getString("OTHERDISEASECD03");
                final String otherdiseasecd04 = rs.getString("OTHERDISEASECD04");
                final String otherdiseasecd05 = rs.getString("OTHERDISEASECD05");
                final String otherdiseasecd99 = rs.getString("OTHERDISEASECD99");
                final MedexamDiseaseAddition1 medexamdiseaseaddition4 = new MedexamDiseaseAddition1(grade, gradeName1, sex, sexname,
                        nutritioncd01, nutritioncd02, nutritioncd03, spineribcd01, spineribcd02, spineribcd03, spineribcd99, skindiseasecd01, skindiseasecd02, skindiseasecd03, skindiseasecd99, otherdiseasecd01, otherdiseasecd02, otherdiseasecd03, otherdiseasecd04, otherdiseasecd05, otherdiseasecd99);
                list.add(medexamdiseaseaddition4);
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
        stb.append(" WITH REGD_GDAT(GRADE, GRADE_NAME1) AS ( ");
        stb.append("     SELECT T1.GRADE, T1.GRADE_NAME1 ");
        stb.append("     FROM SCHREG_REGD_GDAT T1 ");
        stb.append("     WHERE YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     UNION ");
        stb.append("     VALUES('99', '合計') ");
        stb.append(" ) SELECT ");
        stb.append("     T2.GRADE, ");
        stb.append("     T2.GRADE_NAME1, ");
        stb.append("     T1.SEX, ");
        stb.append("     T3.ABBV1 AS SEXNAME, ");
        stb.append("     T1.NUTRITIONCD01, ");
        stb.append("     T1.NUTRITIONCD02, ");
        stb.append("     T1.NUTRITIONCD03, ");
        stb.append("     T1.SPINERIBCD01, ");
        stb.append("     T1.SPINERIBCD02, ");
        stb.append("     T1.SPINERIBCD03, ");
        stb.append("     T1.SPINERIBCD99, ");
        stb.append("     T1.SKINDISEASECD01, ");
        stb.append("     T1.SKINDISEASECD02, ");
        stb.append("     T1.SKINDISEASECD03, ");
        stb.append("     T1.SKINDISEASECD99, ");
        stb.append("     T1.OTHERDISEASECD01, ");
        stb.append("     T1.OTHERDISEASECD02, ");
        stb.append("     T1.OTHERDISEASECD03, ");
        stb.append("     T1.OTHERDISEASECD04, ");
        stb.append("     T1.OTHERDISEASECD05, ");
        stb.append("     T1.OTHERDISEASECD99 ");
        stb.append(" FROM REGD_GDAT T2 ");
        if (!"".equals(_param._fixedData)) {
            stb.append(" LEFT JOIN MEDEXAM_DISEASE_ADDITION1_FIXED_DAT T1 ON T2.GRADE = T1.GRADE ");
        } else {
            stb.append(" LEFT JOIN MEDEXAM_DISEASE_ADDITION1_DAT T1 ON T2.GRADE = T1.GRADE ");
        }
        stb.append(" LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'Z002' ");
        stb.append("     AND T3.NAMECD2 = T1.SEX ");
        stb.append(" WHERE ");
        stb.append("     T1.EDBOARD_SCHOOLCD = '" + _param._edboardSchoolcd + "' ");
        stb.append("     AND T1.YEAR = '" + _param._ctrlYear + "' ");
        if (!"".equals(_param._fixedData)) {
            stb.append("     AND T1.FIXED_DATE = '" + _param._fixedData + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.YEAR, T1.GRADE, T1.SEX ");

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
        final String _ctrlYear;
        final String _ctrlDate;
        final String _executeDate;
        final String _fixedData;
        final String _edboardSchoolcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _executeDate = request.getParameter("EXECUTE_DATE").replace('/', '-');
            _ctrlDate = request.getParameter("CTRL_DATE").replace('/', '-');
            _fixedData = request.getParameter("FIXED_DATA").replace('/', '-');
            KNJSchoolMst schoolMst = new KNJSchoolMst(db2, _ctrlYear);
            _edboardSchoolcd = schoolMst._kyoikuIinkaiSchoolcd;
        }
    }
}

// eof
