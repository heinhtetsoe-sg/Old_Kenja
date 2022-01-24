// kanji=漢字
/*
 * $Id: dbbd3e7f223dfc0705ecc73ddb62aa3c67938288 $
 *
 * 作成日: 2009/10/21 8:52:18 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 *
 * @author nakamoto
 * @version $Id: dbbd3e7f223dfc0705ecc73ddb62aa3c67938288 $
 */
public class KNJE366B {

	private static final Log log = LogFactory.getLog("KNJE366B.class");
	private static final String KISOTSU = "ZZZZZ";

	private boolean _hasData;

	Param _param;

	/**
	 * @param request
	 *            リクエスト
	 * @param response
	 *            レスポンス
	 */
	public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

		final Vrw32alp svf = new Vrw32alp();
		DB2UDB db2 = null;
		try {
			init(response, svf);

			db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
			db2.open();

			_param = createParam(db2, request);
			log.debug("年組：" + _param._classSelectedIn);

			_hasData = false;

			_hasData = printMain(db2, svf, _hasData);

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

	private void init(final HttpServletResponse response, final Vrw32alp svf) throws IOException {
		response.setContentType("application/pdf");

		svf.VrInit();
		svf.VrSetSpoolFileStream(response.getOutputStream());
	}

	private boolean printMain(final DB2UDB db2, final Vrw32alp svf, boolean hasData) throws SQLException {
		final List printGouhi = getPrintGouhi(db2);
		final List printMoshi = getPrintMoshi(db2);

		if (printGouhi == null || printGouhi.size() == 0) {
			return hasData;
		}

		// ヘッダ
		if (_param._isPrintGouhi) {
			svf.VrSetForm("KNJE366B_1.frm", 4);
		} else {
			svf.VrSetForm("KNJE366B_2.frm", 4);
			svf.VrsOut("MOCK_NAME", _param._moshiName); // 模試名称
			for (int cnt = 0; cnt < _param._moshiKamoku.size(); cnt++) {
				svf.VrsOut("MOCK_SUBCLASSNAME" + (cnt + 1), _param._moshiKamoku.get(cnt).toString()); // 科目名
			}
		}
		svf.VrsOut("NENDO", _param.changePrintYear(db2, _param._year)); // 年度
		svf.VrsOut("DATE", _param.changePrintDate(db2, _param._ctrlDate)); // 出力日

		int fieldCnt = 1;
		for (final Iterator it = printGouhi.iterator(); it.hasNext();) {
			final Gouhi gouhi = (Gouhi) it.next();
			if (fieldCnt > 50) {
				svf.VrEndPage();
				fieldCnt = 1;
			}

			// 合否
			svf.VrsOut("GRPCD", gouhi._stat_Cd); // グループコード
			svf.VrsOut("NO", String.valueOf(fieldCnt)); // 連番
			svf.VrsOut("SCHOOL_NAME", gouhi._stat_Name); // 大学名
			svf.VrsOut("FACULTY", gouhi._facultyname); // 学部名
			svf.VrsOut("MAJORCD", gouhi._departmentname); // 学科名
			svf.VrsOut("EXAM_METHOD", gouhi._howtoexam_Name); // 受験方式
			svf.VrsOut("RESULT2", gouhi._decision_Name); // 合否
			svf.VrsOut("COURSE_AHEAD", gouhi._planstat_Name); // 進路状況
			svf.VrsOut("HR_NAME", gouhi._hr_Name_Attendno); // 年組番
			svf.VrsOut("NAME_SHOW", gouhi._name); // 氏名

			//模試
			if (!(_param._isPrintGouhi)) {
				svf.VrsOut("SCORE1_1", gouhi._rank1M_E == null ? "" : gouhi._rank1M_E); // 1学期中間評価
				svf.VrsOut("SCORE1_2", gouhi._rank1M_R == null ? "" : gouhi._rank1M_R); // 1学期中間評定
				svf.VrsOut("SCORE1_3", gouhi._rank1E_E == null ? "" : gouhi._rank1E_E); // 1学期期末評価
				svf.VrsOut("SCORE1_4", gouhi._rank1E_R == null ? "" : gouhi._rank1E_R); // 1学期期末評定
				svf.VrsOut("SCORE2_1", gouhi._rank2M_E == null ? "" : gouhi._rank2M_E); // 2学期中間評価
				svf.VrsOut("SCORE2_2", gouhi._rank2M_R == null ? "" : gouhi._rank2M_R); // 2学期中間評定
				svf.VrsOut("SCORE2_3", gouhi._rank2E_E == null ? "" : gouhi._rank2E_E); // 2学期期末評価
				svf.VrsOut("SCORE2_4", gouhi._rank2E_R == null ? "" : gouhi._rank2E_R); // 2学期期末評定
				svf.VrsOut("SCORE9_1", gouhi._rank3E_E == null ? "" : gouhi._rank3E_E); // 学年末評価
				svf.VrsOut("SCORE9_2", gouhi._rank3E_R == null ? "" : gouhi._rank3E_R); // 学年末評定
				svf.VrsOut("TOTAL_SCORE", gouhi._rank9E_E == null ? "" : gouhi._rank9E_E); // 総合成績
				svf.VrsOut("GRADE_RANK", gouhi._rank9E_R == null ? "" : gouhi._rank9E_R); // 学年順位


				int column = 1;
				for (int cnt = 0; cnt < printMoshi.size(); cnt++) {
					final Moshi moshi = (Moshi) printMoshi.get(cnt);
					if (moshi._schregno.equals(gouhi._schregno)) {
						svf.VrsOut("MOCK" + column  + "_1", moshi._score == null ? "" : moshi._score); // スコア
						svf.VrsOut("MOCK" + column  + "_2", moshi._gtz == null ? "" : moshi._gtz); // GTZ
						column++;
					} else {
						column = 1;
					}
				}
		}

		// hasData = true;
		fieldCnt++;
		svf.VrEndRecord();
	}
	// if (hasData) {
	svf.VrEndPage();
	// }
	return hasData = true;

	}

	private String getGouhiSql() {
		final StringBuffer stb = new StringBuffer();
		stb.append(" WITH SCHREG AS ( ");
		stb.append("     SELECT ");
		stb.append("         T1.SCHREGNO ");
		stb.append("       , T1.GRADE ");
		stb.append("       , T1.HR_CLASS ");
		stb.append("       , T1.ATTENDNO ");
		stb.append("       , S2.HR_NAME ");
		stb.append("       , S1.NAME ");
		stb.append("       , S1.NAME_KANA ");
		stb.append("       , S1.SEX ");
		stb.append("     FROM ");
		stb.append("         SCHREG_REGD_DAT T1 ");
		stb.append("         INNER JOIN SCHREG_BASE_MST S1 ON S1.SCHREGNO = T1.SCHREGNO ");
		stb.append("         INNER JOIN SCHREG_REGD_HDAT S2 ");
		stb.append("             ON S2.YEAR = T1.YEAR ");
		stb.append("             AND S2.SEMESTER = T1.SEMESTER ");
		stb.append("             AND S2.GRADE    = T1.GRADE ");
		stb.append("             AND S2.HR_CLASS = T1.HR_CLASS ");
		stb.append("     WHERE ");
		stb.append("         T1.YEAR     = '" + _param._year + "' ");
		stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
		stb.append(" AND T1.GRADE || T1.HR_CLASS IN " + _param._classSelectedIn);
		stb.append(" UNION ");
		stb.append(" SELECT DISTINCT ");
		stb.append("     T1.SCHREGNO ");
		stb.append("   , 'ZZ-' || VALUE(FISCALYEAR(CASE WHEN S2.GRD_DATE IS NOT NULL THEN S2.GRD_DATE ELSE S1.GRD_DATE END), '') || '-' || VALUE(S2.GRD_GRADE, '') AS GRADE ");
		stb.append("   , 'ZZZ-' || VALUE(S2.GRD_HR_CLASS,'') AS HR_CLASS ");
		stb.append("   , 'ZZZ-' || VALUE(S2.GRD_ATTENDNO,'') AS ATTENDNO ");
		stb.append("   , '既卒生' AS HR_NAME ");
		stb.append("   , S1.NAME ");
		stb.append("   , S1.NAME_KANA ");
		stb.append("   , S1.SEX ");
		stb.append(" FROM ");
		stb.append("     AFT_GRAD_COURSE_DAT T1 ");
		stb.append("     INNER JOIN SCHREG_BASE_MST S1 ON S1.SCHREGNO = T1.SCHREGNO ");
		stb.append("     LEFT JOIN GRD_BASE_MST S2 ON S2.SCHREGNO = T1.SCHREGNO ");
		stb.append(" WHERE ");
		stb.append("     T1.YEAR = '" + _param._year + "' ");
		stb.append(" AND NOT EXISTS(SELECT ");
		stb.append("                     'X' ");
		stb.append("                 FROM ");
		stb.append("                     SCHREG_REGD_DAT E1 ");
		stb.append("                 WHERE ");
		stb.append("                     E1.YEAR     = T1.YEAR ");
		stb.append("                 AND E1.SEMESTER = '" + _param._semester + "' ");
		stb.append("                 AND E1.SCHREGNO = T1.SCHREGNO ");
		stb.append("             ) ");
		stb.append(" AND ('ZZ' || 'ZZZ') IN " + _param._classSelectedIn);
		stb.append(" ) ");
		if (!_param._isPrintGouhi) {
			stb.append(", RANK1M_E AS ( ");
			stb.append(" SELECT ");
			stb.append("     T1.SCHREGNO ");
			stb.append("   , ROUND(T1.AVG,0) AS AVG ");
			stb.append(" FROM ");
			stb.append("     RECORD_RANK_SDIV_DAT T1 ");
			stb.append(" WHERE ");
			stb.append("     T1.YEAR     = '" + _param._year + "'");
			stb.append(" AND T1.SEMESTER = '1' ");
			stb.append(" AND T1.TESTKINDCD = '01' ");
			stb.append(" AND T1.TESTITEMCD = '01' ");
			stb.append(" AND T1.SCORE_DIV = '08' ");
			stb.append(" AND T1.CLASSCD = '99' ");
			stb.append(" AND T1.SCHOOL_KIND = 'H' ");
			stb.append(" AND T1.CURRICULUM_CD = '99' ");
			stb.append(" AND T1.SUBCLASSCD = '999999') ");

			stb.append(", RANK1M_R AS ( ");
			stb.append(" SELECT ");
			stb.append("     T1.SCHREGNO ");
			stb.append("   , ROUND(T1.AVG,0) AS AVG ");
			stb.append(" FROM ");
			stb.append("     RECORD_RANK_SDIV_DAT T1 ");
			stb.append(" WHERE ");
			stb.append("     T1.YEAR     = '" + _param._year + "'");
			stb.append(" AND T1.SEMESTER = '1' ");
			stb.append(" AND T1.TESTKINDCD = '01' ");
			stb.append(" AND T1.TESTITEMCD = '01' ");
			stb.append(" AND T1.SCORE_DIV = '09' ");
			stb.append(" AND T1.CLASSCD = '99' ");
			stb.append(" AND T1.SCHOOL_KIND = 'H' ");
			stb.append(" AND T1.CURRICULUM_CD = '99' ");
			stb.append(" AND T1.SUBCLASSCD = '999999') ");

			stb.append(", RANK1E_E AS ( ");
			stb.append(" SELECT ");
			stb.append("     T1.SCHREGNO ");
			stb.append("   , ROUND(T1.AVG,0) AS AVG ");
			stb.append(" FROM ");
			stb.append("     RECORD_RANK_SDIV_DAT T1 ");
			stb.append(" WHERE ");
			stb.append("     T1.YEAR     = '" + _param._year + "'");
			stb.append(" AND T1.SEMESTER = '1' ");
			stb.append(" AND T1.TESTKINDCD = '02' ");
			stb.append(" AND T1.TESTITEMCD = '01' ");
			stb.append(" AND T1.SCORE_DIV = '08' ");
			stb.append(" AND T1.CLASSCD = '99' ");
			stb.append(" AND T1.SCHOOL_KIND = 'H' ");
			stb.append(" AND T1.CURRICULUM_CD = '99' ");
			stb.append(" AND T1.SUBCLASSCD = '999999') ");

			stb.append(", RANK1E_R AS ( ");
			stb.append(" SELECT ");
			stb.append("     T1.SCHREGNO ");
			stb.append("   , ROUND(T1.AVG,0) AS AVG ");
			stb.append(" FROM ");
			stb.append("     RECORD_RANK_SDIV_DAT T1 ");
			stb.append(" WHERE ");
			stb.append("     T1.YEAR     = '" + _param._year + "'");
			stb.append(" AND T1.SEMESTER = '1' ");
			stb.append(" AND T1.TESTKINDCD = '02' ");
			stb.append(" AND T1.TESTITEMCD = '01' ");
			stb.append(" AND T1.SCORE_DIV = '09' ");
			stb.append(" AND T1.CLASSCD = '99' ");
			stb.append(" AND T1.SCHOOL_KIND = 'H' ");
			stb.append(" AND T1.CURRICULUM_CD = '99' ");
			stb.append(" AND T1.SUBCLASSCD = '999999') ");

			stb.append(", RANK2M_E AS ( ");
			stb.append(" SELECT ");
			stb.append("     T1.SCHREGNO ");
			stb.append("   , ROUND(T1.AVG,0) AS AVG ");
			stb.append(" FROM ");
			stb.append("     RECORD_RANK_SDIV_DAT T1 ");
			stb.append(" WHERE ");
			stb.append("     T1.YEAR     = '" + _param._year + "'");
			stb.append(" AND T1.SEMESTER = '2' ");
			stb.append(" AND T1.TESTKINDCD = '01' ");
			stb.append(" AND T1.TESTITEMCD = '01' ");
			stb.append(" AND T1.SCORE_DIV = '08' ");
			stb.append(" AND T1.CLASSCD = '99' ");
			stb.append(" AND T1.SCHOOL_KIND = 'H' ");
			stb.append(" AND T1.CURRICULUM_CD = '99' ");
			stb.append(" AND T1.SUBCLASSCD = '999999') ");

			stb.append(", RANK2M_R AS ( ");
			stb.append(" SELECT ");
			stb.append("     T1.SCHREGNO ");
			stb.append("   , ROUND(T1.AVG,0) AS AVG ");
			stb.append(" FROM ");
			stb.append("     RECORD_RANK_SDIV_DAT T1 ");
			stb.append(" WHERE ");
			stb.append("     T1.YEAR     = '" + _param._year + "'");
			stb.append(" AND T1.SEMESTER = '2' ");
			stb.append(" AND T1.TESTKINDCD = '01' ");
			stb.append(" AND T1.TESTITEMCD = '01' ");
			stb.append(" AND T1.SCORE_DIV = '09' ");
			stb.append(" AND T1.CLASSCD = '99' ");
			stb.append(" AND T1.SCHOOL_KIND = 'H' ");
			stb.append(" AND T1.CURRICULUM_CD = '99' ");
			stb.append(" AND T1.SUBCLASSCD = '999999') ");

			stb.append(", RANK2E_E AS ( ");
			stb.append(" SELECT ");
			stb.append("     T1.SCHREGNO ");
			stb.append("   , ROUND(T1.AVG,0) AS AVG ");
			stb.append(" FROM ");
			stb.append("     RECORD_RANK_SDIV_DAT T1 ");
			stb.append(" WHERE ");
			stb.append("     T1.YEAR     = '" + _param._year + "'");
			stb.append(" AND T1.SEMESTER = '2' ");
			stb.append(" AND T1.TESTKINDCD = '02' ");
			stb.append(" AND T1.TESTITEMCD = '01' ");
			stb.append(" AND T1.SCORE_DIV = '08' ");
			stb.append(" AND T1.CLASSCD = '99' ");
			stb.append(" AND T1.SCHOOL_KIND = 'H' ");
			stb.append(" AND T1.CURRICULUM_CD = '99' ");
			stb.append(" AND T1.SUBCLASSCD = '999999') ");

			stb.append(", RANK2E_R AS ( ");
			stb.append(" SELECT ");
			stb.append("     T1.SCHREGNO ");
			stb.append("   , ROUND(T1.AVG,0) AS AVG ");
			stb.append(" FROM ");
			stb.append("     RECORD_RANK_SDIV_DAT T1 ");
			stb.append(" WHERE ");
			stb.append("     T1.YEAR     = '" + _param._year + "'");
			stb.append(" AND T1.SEMESTER = '2' ");
			stb.append(" AND T1.TESTKINDCD = '02' ");
			stb.append(" AND T1.TESTITEMCD = '01' ");
			stb.append(" AND T1.SCORE_DIV = '09' ");
			stb.append(" AND T1.CLASSCD = '99' ");
			stb.append(" AND T1.SCHOOL_KIND = 'H' ");
			stb.append(" AND T1.CURRICULUM_CD = '99' ");
			stb.append(" AND T1.SUBCLASSCD = '999999') ");

			stb.append(", RANK3E_E AS ( ");
			stb.append(" SELECT ");
			stb.append("     T1.SCHREGNO ");
			stb.append("   , ROUND(T1.AVG,0) AS AVG ");
			stb.append(" FROM ");
			stb.append("     RECORD_RANK_SDIV_DAT T1 ");
			stb.append(" WHERE ");
			stb.append("     T1.YEAR     = '" + _param._year + "'");
			stb.append(" AND T1.SEMESTER = '3' ");
			stb.append(" AND T1.TESTKINDCD = '02' ");
			stb.append(" AND T1.TESTITEMCD = '01' ");
			stb.append(" AND T1.SCORE_DIV = '08' ");
			stb.append(" AND T1.CLASSCD = '99' ");
			stb.append(" AND T1.SCHOOL_KIND = 'H' ");
			stb.append(" AND T1.CURRICULUM_CD = '99' ");
			stb.append(" AND T1.SUBCLASSCD = '999999') ");

			stb.append(", RANK3E_R AS ( ");
			stb.append(" SELECT ");
			stb.append("     T1.SCHREGNO ");
			stb.append("   , ROUND(T1.AVG,0) AS AVG ");
			stb.append(" FROM ");
			stb.append("     RECORD_RANK_SDIV_DAT T1 ");
			stb.append(" WHERE ");
			stb.append("     T1.YEAR     = '" + _param._year + "'");
			stb.append(" AND T1.SEMESTER = '3' ");
			stb.append(" AND T1.TESTKINDCD = '02' ");
			stb.append(" AND T1.TESTITEMCD = '01' ");
			stb.append(" AND T1.SCORE_DIV = '09' ");
			stb.append(" AND T1.CLASSCD = '99' ");
			stb.append(" AND T1.SCHOOL_KIND = 'H' ");
			stb.append(" AND T1.CURRICULUM_CD = '99' ");
			stb.append(" AND T1.SUBCLASSCD = '999999') ");

			stb.append(", RANK9E_E AS ( ");
			stb.append(" SELECT ");
			stb.append("     T1.SCHREGNO ");
			stb.append("   , ROUND(T1.AVG,0) AS AVG ");
			stb.append(" FROM ");
			stb.append("     RECORD_RANK_SDIV_DAT T1 ");
			stb.append(" WHERE ");
			stb.append("     T1.YEAR     = '" + _param._year + "'");
			stb.append(" AND T1.SEMESTER = '9' ");
			stb.append(" AND T1.TESTKINDCD = '99' ");
			stb.append(" AND T1.TESTITEMCD = '00' ");
			stb.append(" AND T1.SCORE_DIV = '08' ");
			stb.append(" AND T1.CLASSCD = '99' ");
			stb.append(" AND T1.SCHOOL_KIND = 'H' ");
			stb.append(" AND T1.CURRICULUM_CD = '99' ");
			stb.append(" AND T1.SUBCLASSCD = '999999') ");

			stb.append(", RANK9E_R AS ( ");
			stb.append(" SELECT ");
			stb.append("     T1.SCHREGNO ");
			stb.append("   , T1.GRADE_AVG_RANK ");
			stb.append(" FROM ");
			stb.append("     RECORD_RANK_SDIV_DAT T1 ");
			stb.append(" WHERE ");
			stb.append("     T1.YEAR     = '" + _param._year + "'");
			stb.append(" AND T1.SEMESTER = '9' ");
			stb.append(" AND T1.TESTKINDCD = '99' ");
			stb.append(" AND T1.TESTITEMCD = '00' ");
			stb.append(" AND T1.SCORE_DIV = '09' ");
			stb.append(" AND T1.CLASSCD = '99' ");
			stb.append(" AND T1.SCHOOL_KIND = 'H' ");
			stb.append(" AND T1.CURRICULUM_CD = '99' ");
			stb.append(" AND T1.SUBCLASSCD = '999999') ");
		}

		stb.append(" SELECT ");
		stb.append("     T1.SEQ ");
		stb.append("   , T1.SCHREGNO ");
		stb.append("   , I1.GRADE ");
		stb.append("   , I1.HR_CLASS ");
		stb.append("   , I1.ATTENDNO ");
		stb.append("   , I1.HR_NAME ");
		stb.append("   , I1.HR_NAME || '-' || I1.ATTENDNO HR_NAME_ATTENDNO ");
		stb.append("   , I1.NAME ");
		stb.append("   , T1.SENKOU_KIND ");
		stb.append("   , T1.STAT_CD ");
		stb.append("   , L1.SCHOOL_NAME_SHOW1 AS STAT_NAME ");
		stb.append("   , T1.SCHOOL_GROUP ");
		stb.append("   , E012.NAME1 AS SCHOOL_GROUP_NAME ");
		stb.append("   , T1.FACULTYCD ");
		stb.append("   , L2.FACULTYNAME ");
		stb.append("   , T1.DEPARTMENTCD ");
		stb.append("   , L3.DEPARTMENTNAME ");
		stb.append("   , T1.HOWTOEXAM ");
		stb.append("   , E002.NAME1 AS HOWTOEXAM_NAME ");
		stb.append("   , T1.DECISION ");
		stb.append("   , E005.NAME1 AS DECISION_NAME ");
		stb.append("   , T1.PLANSTAT ");
		stb.append("   , E006.NAME1 AS PLANSTAT_NAME ");
		stb.append("   , AFT_GRAD_D.REMARK9 AS EXAMNO ");

		if (!_param._isPrintGouhi) {
			stb.append("   , RANK1M_E.AVG AS RANK1M_E ");
			stb.append("   , RANK1M_R.AVG AS RANK1M_R ");
			stb.append("   , RANK1E_E.AVG AS RANK1E_E ");
			stb.append("   , RANK1E_R.AVG AS RANK1E_R ");
			stb.append("   , RANK2M_E.AVG AS RANK2M_E ");
			stb.append("   , RANK2M_R.AVG AS RANK2M_R ");
			stb.append("   , RANK2E_E.AVG AS RANK2E_E ");
			stb.append("   , RANK2E_R.AVG AS RANK2E_R ");
			stb.append("   , RANK3E_E.AVG AS RANK3E_E ");
			stb.append("   , RANK3E_R.AVG AS RANK3E_R ");
			stb.append("   , RANK9E_E.AVG AS RANK9E_E ");
			stb.append("   , RANK9E_R.GRADE_AVG_RANK AS RANK9E_R ");
		}

		stb.append(" FROM ");
		stb.append("     AFT_GRAD_COURSE_DAT T1 ");
		stb.append("     INNER JOIN SCHREG I1 ON I1.SCHREGNO = T1.SCHREGNO ");
		stb.append("     INNER JOIN COLLEGE_MST L1 ON L1.SCHOOL_CD = T1.STAT_CD ");
		stb.append("     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT AFT_GRAD_D ");
		stb.append("         ON T1.YEAR = AFT_GRAD_D.YEAR ");
		stb.append("         AND T1.SEQ = AFT_GRAD_D.SEQ ");
		stb.append("         AND AFT_GRAD_D.DETAIL_SEQ = 1 ");
		stb.append("     LEFT JOIN COLLEGE_FACULTY_MST L2 ");
		stb.append("         ON L2.SCHOOL_CD = T1.STAT_CD ");
		stb.append("         AND L2.FACULTYCD = T1.FACULTYCD ");
		stb.append("     LEFT JOIN COLLEGE_DEPARTMENT_MST L3 ");
		stb.append("         ON L3.SCHOOL_CD = T1.STAT_CD ");
		stb.append("         AND L3.FACULTYCD = T1.FACULTYCD ");
		stb.append("         AND L3.DEPARTMENTCD = T1.DEPARTMENTCD ");
		stb.append("     LEFT JOIN PREF_MST L4 ON L4.PREF_CD = T1.PREF_CD ");
		stb.append("     LEFT JOIN NAME_MST E002 ON E002.NAMECD1 = 'E002' AND E002.NAMECD2 = T1.HOWTOEXAM ");
		stb.append("     LEFT JOIN NAME_MST E005 ON E005.NAMECD1 = 'E005' AND E005.NAMECD2 = T1.DECISION ");
		stb.append("     LEFT JOIN NAME_MST E006 ON E006.NAMECD1 = 'E006' AND E006.NAMECD2 = T1.PLANSTAT ");
		stb.append("     LEFT JOIN NAME_MST E012 ON E012.NAMECD1 = 'E012' AND E012.NAMECD2 = T1.SCHOOL_GROUP ");

		if (!_param._isPrintGouhi) {
			stb.append("     LEFT JOIN RANK1M_E ON RANK1M_E.SCHREGNO = T1.SCHREGNO ");
			stb.append("     LEFT JOIN RANK1M_R ON RANK1M_R.SCHREGNO = T1.SCHREGNO ");
			stb.append("     LEFT JOIN RANK1E_E ON RANK1E_E.SCHREGNO = T1.SCHREGNO ");
			stb.append("     LEFT JOIN RANK1E_R ON RANK1E_R.SCHREGNO = T1.SCHREGNO ");
			stb.append("     LEFT JOIN RANK2M_E ON RANK2M_E.SCHREGNO = T1.SCHREGNO ");
			stb.append("     LEFT JOIN RANK2M_R ON RANK2M_R.SCHREGNO = T1.SCHREGNO ");
			stb.append("     LEFT JOIN RANK2E_E ON RANK2E_E.SCHREGNO = T1.SCHREGNO ");
			stb.append("     LEFT JOIN RANK2E_R ON RANK2E_R.SCHREGNO = T1.SCHREGNO ");
			stb.append("     LEFT JOIN RANK3E_E ON RANK3E_E.SCHREGNO = T1.SCHREGNO ");
			stb.append("     LEFT JOIN RANK3E_R ON RANK3E_R.SCHREGNO = T1.SCHREGNO ");
			stb.append("     LEFT JOIN RANK9E_E ON RANK9E_E.SCHREGNO = T1.SCHREGNO ");
			stb.append("     LEFT JOIN RANK9E_R ON RANK9E_R.SCHREGNO = T1.SCHREGNO ");
		}

		stb.append(" WHERE ");
		stb.append("         T1.YEAR         = '" + _param._year + "' ");
		stb.append("     AND T1.SENKOU_KIND  = '0' ");

		if ("MIX".equals(_param._gouhiCd2)){
			stb.append("     AND T1.DECISION IN ('1','2')");
		}else if("E005".equals(_param._gouhiCd1)) {
			stb.append("     AND T1.DECISION IN ('" + _param._gouhiCd2 + "')");
		}else if("E006".equals(_param._gouhiCd1)) {
			stb.append("     AND T1.PLANSTAT IN ('" + _param._gouhiCd2 + "')");
		}
		stb.append("     AND T1.SCHOOL_GROUP IN " + _param._typeSelectedIn);

		stb.append(" ORDER BY ");
		stb.append("     T1.STAT_CD ");
		stb.append("   , T1.FACULTYCD ");
		stb.append("   , T1.DEPARTMENTCD ");
		stb.append("   , I1.GRADE ");
		stb.append("   , I1.HR_CLASS ");
		stb.append("   , I1.ATTENDNO ");
		stb.append("   , T1.SCHREGNO ");
		stb.append("   , T1.SEQ ");

		return stb.toString();
	}

	private void closeDb(final DB2UDB db2) {
		if (null != db2) {
			db2.commit();
			db2.close();
		}
	}

	private List getPrintMoshi(final DB2UDB db2) throws SQLException {
		final List rtnList = new ArrayList();
		PreparedStatement ps = null;
		ResultSet rs = null;
		final String moshiSql = getMoshiSql();
		log.debug(" sql = " + moshiSql);
		try {
			ps = db2.prepareStatement(moshiSql);
			rs = ps.executeQuery();
			while (rs.next()) {
				final String year = rs.getString("YEAR");
				final String mockcd = rs.getString("MOCKCD");
				final String schregno = rs.getString("SCHREGNO");
				final String mock_Subclass_Cd = rs.getString("MOCK_SUBCLASS_CD");
				final String subclass_Abbv = rs.getString("SUBCLASS_ABBV");
				final String mockname1 = rs.getString("MOCKNAME1");
				final String score = rs.getString("SCORE");
				final String gtz = rs.getString("GTZ");

				final Moshi moshi = new Moshi(year, mockcd, schregno, mock_Subclass_Cd, subclass_Abbv, mockname1, score,
						gtz);
				rtnList.add(moshi);
			}
		} finally {
			DbUtils.closeQuietly(null, ps, rs);
			db2.commit();
		}
		return rtnList;
	}

	private String getMoshiSql() {

		final StringBuffer stb = new StringBuffer();
		stb.append(" SELECT ");
		stb.append("     T5.YEAR, ");
		stb.append("     T2.MOCKCD, ");
		stb.append("     T1.SCHREGNO, ");
		stb.append("     T2.MOCK_SUBCLASS_CD, ");
		stb.append("     T3.SUBCLASS_ABBV, ");
		stb.append("     T4.MOCKNAME1, ");
		stb.append("     T2.SCORE, ");
		stb.append("     T2.GTZ ");
		stb.append(" FROM ");
		stb.append("     SCHREG_BASE_MST T1 ");
		stb.append(" INNER JOIN ");
		stb.append("     mock_rank_range_dat T2 ");
		stb.append(" ON T1.SCHREGNO = T2.SCHREGNO AND T2.RANK_RANGE = '2' AND T2.RANK_DIV = '02' AND T2.MOCKDIV = '1'  ");
		stb.append(" INNER JOIN mock_subclass_mst T3 ");
		stb.append(" ON T2.MOCK_SUBCLASS_CD = T3.MOCK_SUBCLASS_CD  ");
		stb.append(" INNER JOIN ");
		stb.append("     MOCK_MST T4 ");
		stb.append(" ON T2.MOCKCD = T4.MOCKCD AND T4.COMPANYCD = " + _param._companycd);
		stb.append(" INNER JOIN ");
		stb.append("     SCHREG_REGD_DAT T5 ");
		stb.append(" ON T1.SCHREGNO = T5.SCHREGNO ");
		stb.append(" WHERE ");
		stb.append("     T5.GRADE || T5.HR_CLASS IN " + _param._classSelectedIn + " " + " and T5.semester = "
				+ _param._semester + " and T5.year = " + _param._year + " AND T2.MOCKCD = '" + _param._mockCd + "'");
		stb.append(" order by T1.SCHREGNO,T3.MOCK_SUBCLASS_CD ");

		return stb.toString();
	}

	private class Moshi {
		final String _year;
		final String _mockcd;
		final String _schregno;
		final String _mock_Subclass_Cd;
		final String _subclass_Abbv;
		final String _mockname1;
		final String _score;
		final String _gtz;

		Moshi(final String year, final String mockcd, final String schregno, final String mock_Subclass_Cd,
				final String subclass_Abbv, final String mockname1, final String score, final String gtz

		) {
			_year = year;
			_mockcd = mockcd;
			_schregno = schregno;
			_mock_Subclass_Cd = mock_Subclass_Cd;
			_subclass_Abbv = subclass_Abbv;
			_mockname1 = mockname1;
			_score = score;
			_gtz = gtz;

		}

	}


	private List getPrintGouhi(final DB2UDB db2) throws SQLException {
		final List rtnList = new ArrayList();
		PreparedStatement ps = null;
		ResultSet rs = null;
		final String gouhiSql = getGouhiSql();
		try {
			ps = db2.prepareStatement(gouhiSql);
			rs = ps.executeQuery();
			while (rs.next()) {


				final String seq = rs.getString("SEQ");
				final String schregno = rs.getString("SCHREGNO");
				final String grade = rs.getString("GRADE");
				final String hr_Class = rs.getString("HR_CLASS");
				final String attendno = rs.getString("ATTENDNO");
				final String hr_Name = rs.getString("HR_NAME");
				final String hr_Name_Attendno = rs.getString("HR_NAME_ATTENDNO");
				final String name = rs.getString("NAME");
				final String senkou_Kind = rs.getString("SENKOU_KIND");
				final String stat_Cd = rs.getString("STAT_CD");
				final String stat_Name = rs.getString("STAT_NAME");
				final String school_Group = rs.getString("SCHOOL_GROUP");
				final String school_Group_Name = rs.getString("SCHOOL_GROUP_NAME");
				final String facultycd = rs.getString("FACULTYCD");
				final String facultyname = rs.getString("FACULTYNAME");
				final String departmentcd = rs.getString("DEPARTMENTCD");
				final String departmentname = rs.getString("DEPARTMENTNAME");
				final String howtoexam = rs.getString("HOWTOEXAM");
				final String howtoexam_Name = rs.getString("HOWTOEXAM_NAME");
				final String decision = rs.getString("DECISION");
				final String decision_Name = rs.getString("DECISION_NAME");
				final String planstat = rs.getString("PLANSTAT");
				final String planstat_Name = rs.getString("PLANSTAT_NAME");
				final String examno = rs.getString("EXAMNO");


				 String rank1M_E = "";
				 String rank1M_R = "";
				 String rank1E_E = "";
				 String rank1E_R = "";
				 String rank2M_E = "";
				 String rank2M_R = "";
				 String rank2E_E = "";
				 String rank2E_R = "";
				 String rank3E_E = "";
				 String rank3E_R = "";
				 String rank9E_E = "";
				 String rank9E_R = "";

				if (!_param._isPrintGouhi) {
					 rank1M_E = (rs.getString("RANK1M_E") == null ? "" : rs.getString("RANK1M_E").replaceAll("\\..*",""));
					 rank1M_R = rs.getString("RANK1M_R");
					 rank1E_E = (rs.getString("RANK1E_E") == null ? "" : rs.getString("RANK1E_E").replaceAll("\\..*",""));
					 rank1E_R = rs.getString("RANK1E_R");
					 rank2M_E = (rs.getString("RANK2M_E") == null ? "" : rs.getString("RANK2M_E").replaceAll("\\..*",""));
					 rank2M_R = rs.getString("RANK2M_R");
					 rank2E_E = (rs.getString("RANK2E_E") == null ? "" : rs.getString("RANK2E_E").replaceAll("\\..*",""));
					 rank2E_R = rs.getString("RANK2E_R");
					 rank3E_E = (rs.getString("RANK3E_E") == null ? "" : rs.getString("RANK3E_E").replaceAll("\\..*",""));
					 rank3E_R = rs.getString("RANK3E_R");
					 rank9E_E = (rs.getString("RANK9E_E") == null ? "" : rs.getString("RANK9E_E") .replaceAll("\\..*",""));
					 rank9E_R = rs.getString("RANK9E_R");

				}
				final Gouhi gouhi = new Gouhi(seq, schregno, grade, hr_Class, attendno, hr_Name, hr_Name_Attendno, name, senkou_Kind, stat_Cd, stat_Name, school_Group, school_Group_Name, facultycd, facultyname, departmentcd, departmentname, howtoexam, howtoexam_Name, decision, decision_Name, planstat, planstat_Name, examno, rank1M_E, rank1M_R, rank1E_E, rank1E_R, rank2M_E, rank2M_R, rank2E_E, rank2E_R, rank3E_E, rank3E_R, rank9E_E,rank9E_R
						);


				rtnList.add(gouhi);
			}
		} finally {
			DbUtils.closeQuietly(null, ps, rs);
			db2.commit();
		}
		return rtnList;

	}


	private class Gouhi {
		final String _seq;
		final String _schregno;
		final String _grade;
		final String _hr_Class;
		final String _attendno;
		final String _hr_Name;
		final String _hr_Name_Attendno;
		final String _name;
		final String _senkou_Kind;
		final String _stat_Cd;
		final String _stat_Name;
		final String _school_Group;
		final String _school_Group_Name;
		final String _facultycd;
		final String _facultyname;
		final String _departmentcd;
		final String _departmentname;
		final String _howtoexam;
		final String _howtoexam_Name;
		final String _decision;
		final String _decision_Name;
		final String _planstat;
		final String _planstat_Name;
		final String _examno;
		final String _rank1M_E;
		final String _rank1M_R;
		final String _rank1E_E;
		final String _rank1E_R;
		final String _rank2M_E;
		final String _rank2M_R;
		final String _rank2E_E;
		final String _rank2E_R;
		final String _rank3E_E;
		final String _rank3E_R;
		final String _rank9E_E;
		final String _rank9E_R;


		Gouhi(final String seq, final String schregno, final String grade, final String hr_Class, final String attendno, final String hr_Name, final String hr_Name_Attendno, final String name, final String senkou_Kind, final String stat_Cd, final String stat_Name, final String school_Group, final String school_Group_Name, final String facultycd, final String facultyname, final String departmentcd, final String departmentname, final String howtoexam, final String howtoexam_Name, final String decision, final String decision_Name, final String planstat, final String planstat_Name, final String examno, final String rank1M_E, final String rank1M_R, final String rank1E_E, final String rank1E_R, final String rank2M_E, final String rank2M_R, final String rank2E_E, final String rank2E_R, final String rank3E_E, final String rank3E_R, final String rank9E_E, final String rank9E_R
) {
		    _seq = seq;
		    _schregno = schregno;
		    _grade = grade;
		    _hr_Class = hr_Class;
		    _attendno = attendno;
		    _hr_Name = hr_Name;
		    _hr_Name_Attendno  = hr_Name_Attendno;
		    _name = name;
		    _senkou_Kind = senkou_Kind;
		    _stat_Cd = stat_Cd;
		    _stat_Name = stat_Name;
		    _school_Group = school_Group;
		    _school_Group_Name = school_Group_Name;
		    _facultycd = facultycd;
		    _facultyname = facultyname;
		    _departmentcd = departmentcd;
		    _departmentname = departmentname;
		    _howtoexam = howtoexam;
		    _howtoexam_Name = howtoexam_Name;
		    _decision = decision;
		    _decision_Name = decision_Name;
		    _planstat = planstat;
		    _planstat_Name = planstat_Name;
		    _examno = examno;
		    _rank1M_E = rank1M_E;
		    _rank1M_R = rank1M_R;
		    _rank1E_E = rank1E_E;
		    _rank1E_R = rank1E_R;
		    _rank2M_E = rank2M_E;
		    _rank2M_R = rank2M_R;
		    _rank2E_E = rank2E_E;
		    _rank2E_R = rank2E_R;
		    _rank3E_E = rank3E_E;
		    _rank3E_R = rank3E_R;
		    _rank9E_E = rank9E_E;
		    _rank9E_R = rank9E_R;

		}

	}

	private String getSougouSql() {
		final StringBuffer stb = new StringBuffer();
		stb.append(" SELECT ");
		stb.append("     SCHREGNO, ");
		stb.append("     SEMESTER, ");
		stb.append("     TESTKINDCD, ");
		stb.append("     TESTITEMCD, ");
		stb.append("     SCORE_DIV, ");
		stb.append("     ROUND(AVG,0) AS AVG, ");
		stb.append("     GRADE_AVG_RANK ");
		stb.append("     FROM ");
		stb.append("         RECORD_RANK_SDIV_DAT T1 ");
		stb.append("     WHERE ");
		stb.append("         YEAR = '" + _param._year + "' AND ");
		stb.append("         SCHOOL_KIND  = 'H' AND ");
		stb.append("         CLASSCD = '99' AND ");
		stb.append("         SUBCLASSCD = '999999' AND ");
		stb.append("         SCORE_DIV IN ('08','09') ");
		stb.append(" ORDER BY ");
		stb.append("     SCHREGNO, ");
		stb.append("     SEMESTER, ");
		stb.append("     TESTKINDCD, ");
		stb.append("     TESTITEMCD, ");
		stb.append("     SCORE_DIV     ");

		return stb.toString();
	}

	/** パラメータ取得処理 */
	private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
		final Param param = new Param(db2, request);
		log.fatal("$Revision: 70043 $");
		KNJServletUtils.debugParam(request, log);
		return param;
	}

	/** パラメータクラス */
	public class Param {
		final String _ctrlYear;
		final String _ctrlSemester;
		final String _year;
		final String _semester;
		final String _ctrlDate;
		final String _classSelectedIn;
		final String _typeSelectedIn;
		final String[] _typeSelected;
		final String[] _selecttypedata;
		String _mockCd;
		String _companycd;

		final boolean _isPrintGouhi;// 合否ならtrue、進路ならfalse
		final String _senkouKind;// 合否なら'0'、進路なら'1'
		final String _dataDiv; // 1:クラス指定 2:個人指定
		final List _moshiKamoku = new ArrayList(); // 模試科目名称
		final int _typeSelMaxCnt; // 種別(設置区分)の最大選択件数

		String _gouhiCd1;// 「E005 or E006」
		String _gouhiCd2;
		String _gouhiCd3;
		String _gouhiName;
		String _gouhiName2;
		String _kubunName;
		String _moshiName;

		private boolean _isSeireki;
		boolean _isKisotsu = false;

		Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
			_ctrlYear = request.getParameter("CTRL_YEAR");
			_ctrlSemester = request.getParameter("CTRL_SEMESTER");
			_year = request.getParameter("YEAR");
			_semester = request.getParameter("SEMESTER");
			_ctrlDate = request.getParameter("CTRL_DATE");
			_selecttypedata = request.getParameterValues("selecttypedata");

			final String[] classSelected = request.getParameterValues("CLASS_SELECTED");

			_classSelectedIn = getClassSelectedIn(classSelected);

			String outDiv = request.getParameter("OUT_DIV");
			_isPrintGouhi = "1".equals(outDiv);
			_senkouKind = "0";

			_dataDiv = "1";
			String[] gouhi = StringUtils.split(request.getParameter("GOUHI"), "-");
			_gouhiCd1 = gouhi[0];
			_gouhiCd2 = gouhi[1];
			if ("MIX".equals(_gouhiCd2)) {
				_gouhiCd3 = gouhi[2];
				_gouhiName = getGouhiKubunName2(db2, _gouhiCd1, _gouhiCd3, "NAME1");
				_gouhiName2 = getGouhiKubunName2(db2, _gouhiCd1, _gouhiCd3, "NAME2");
			} else {
				_gouhiCd3 = "";
				_gouhiName = getGouhiKubunName(db2, _gouhiCd1, _gouhiCd2, "NAME1");
				_gouhiName2 = getGouhiKubunName(db2, _gouhiCd1, _gouhiCd2, "NAME2");
			}
			_typeSelMaxCnt = Integer.parseInt(request.getParameter("SELECTDATA_TYPE_CNT"));


			_typeSelected = request.getParameterValues("SELECTTYPEDATA");
			final String[] typeSelected = request.getParameterValues("SELECTTYPEDATA");
			String[] ar = typeSelected[0].toString().split(",");
			_typeSelectedIn = getTypeSelectedIn(ar);

			if (_isPrintGouhi) {
				if (_typeSelMaxCnt == _typeSelected.length) {
					// 全てとして扱う。
					_kubunName = "全て";
				} else {
					_kubunName = "";
					String delimstr = "";
					for (int ii = 0; ii < _typeSelected.length; ii++) {
						if (ii >= 2) {
							continue;
						}
						String kubun = _typeSelected[ii];
						final String kubunCd1 = kubun.substring(0, 4);
						final String kubunCd2 = kubun.substring(5);
						_kubunName += delimstr + getGouhiKubunName(db2, kubunCd1, kubunCd2, "NAME1");
						if (_typeSelected.length > 2 && ii + 1 >= 2) {
							_kubunName += delimstr + "他";
						}
						delimstr = "、";
					}
				}
			} else {
				_mockCd = request.getParameter("MOCKCD");
				_companycd = request.getParameter("COMPANYCD");
				setMoshiKamoku(db2);
				setMoshiName(db2);
			}
			setSeirekiFlg(db2);
		}

		private String getClassSelectedIn(final String[] classSelected) {
			StringBuffer stb = new StringBuffer();
			stb.append("(");
			for (int i = 0; i < classSelected.length; i++) {
				if (0 < i)
					stb.append(",");
				stb.append("'" + classSelected[i].replace("-", "") + "'");
				if (KISOTSU.equals(classSelected[i])) {
					_isKisotsu = true;
				}
			}
			stb.append(")");
			return stb.toString();
		}

		private String getTypeSelectedIn(final String[] typeSelected) {
			StringBuffer stb = new StringBuffer();
			stb.append("(");
			for (int i = 0; i < typeSelected.length; i++) {
				if (0 < i)
					stb.append(",");
				final String tmp = typeSelected[i].toString().replace("E012-", "");
				stb.append("'" + tmp + "'");
				if (KISOTSU.equals(typeSelected[i])) {
					_isKisotsu = true;
				}
			}
			stb.append(")");
			return stb.toString();
		}

		private String getGouhiKubunName(final DB2UDB db2, String cd1, String cd2, final String field) {
			if ("ALL".equals(cd2)) {
				return "全て";
			}
			String rtnName = "";
			try {
				String sql = "SELECT " + field + " FROM NAME_MST WHERE NAMECD1='" + cd1 + "' AND NAMECD2='" + cd2
						+ "' ";
				PreparedStatement ps = db2.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					rtnName = rs.getString(field);
				}
				ps.close();
				rs.close();
			} catch (Exception e) {
				log.error("Exception", e);
			} finally {
				db2.commit();
			}
			return rtnName;
		}

		private String getGouhiKubunName2(final DB2UDB db2, String cd1, String namespare, final String field) {
			String rtnName = "";
			String sep = "";
			try {
				String sql = "SELECT " + field + " FROM NAME_MST WHERE NAMECD1='" + cd1 + "' AND NAMESPARE2='"
						+ namespare + "' ";
				PreparedStatement ps = db2.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					final String name = StringUtils.defaultString(rs.getString(field), "");
					if (!"".equals(name)) {
						rtnName = rtnName + sep + name;
						sep = "・";
					}
				}
				ps.close();
				rs.close();
			} catch (Exception e) {
				log.error("Exception", e);
			} finally {
				db2.commit();
			}
			return rtnName;
		}

		private void setSeirekiFlg(final DB2UDB db2) {
			try {
				_isSeireki = false;
				String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
				PreparedStatement ps = db2.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					if (rs.getString("NAME1").equals("2"))
						_isSeireki = true; // 西暦
				}
				ps.close();
				rs.close();
			} catch (Exception e) {
				log.error("Exception", e);
			} finally {
				db2.commit();
			}
		}

		public String changePrintDate(final DB2UDB db2, final String date) {
			if (null == date) {
				return "";
			}
			if (_isSeireki) {
				return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
			} else {
				return KNJ_EditDate.h_format_JP(db2, date);
			}
		}

		public String changePrintYear(final DB2UDB db2, final String year) {
			if (null == year) {
				return "";
			}
			if (_isSeireki) {
				return year + "年度";
			} else {
				return KNJ_EditDate.gengou(db2, Integer.parseInt(year)) + "年度";
			}
		}

		public boolean isNamecdE012(final String cd1) {
			return "E012".equals(cd1);
		}

		public boolean isNamecdE005(final String cd1) {
			return "E005".equals(cd1);
		}

		public boolean isNamecdE006(final String cd1) {
			return "E006".equals(cd1);
		}

		// 模試名称
		private void setMoshiName(final DB2UDB db2) throws SQLException {

			final StringBuffer stb = new StringBuffer();
			stb.append(" SELECT ");
			stb.append("     KYOUZAINAME ");
			stb.append(" FROM ");
			stb.append("     MOCK_CSV_BENE_SCORE_HDAT ");
			stb.append(" WHERE ");
			stb.append("     YEAR = " + _ctrlYear + " AND ");
			stb.append("     KYOUZAICD = '06' AND ");
			stb.append("     MOCKCD = " + _mockCd + " AND ");
			stb.append("     KYOUZAINAME  IS NOT NULL ");

			PreparedStatement ps = null;
			ResultSet rs = null;

			log.debug(" sql = " + stb.toString());
			try {
				ps = db2.prepareStatement(stb.toString());
				rs = ps.executeQuery();
				while (rs.next()) {
					_moshiName = rs.getString("KYOUZAINAME");
				}
			} catch (Exception e) {
				log.error("Exception", e);
			} finally {
				DbUtils.closeQuietly(null, ps, rs);
				db2.commit();
			}
		}

		private void setMoshiKamoku(final DB2UDB db2) throws SQLException {
			final StringBuffer stb = new StringBuffer();
			stb.append(" SELECT ");
			stb.append("     MIN(T4.MOCKNAME1) AS MOCKNAME, ");
			stb.append("     T3.SUBCLASS_ABBV, ");
			stb.append("     MIN(T3.MOCK_SUBCLASS_CD) AS MOCK_SUBCLASS_CD ");
			stb.append(" FROM ");
			stb.append(
					"     SCHREG_REGD_DAT T1  INNER JOIN      mock_rank_range_dat T2  ON T1.SCHREGNO = T2.SCHREGNO AND T2.RANK_RANGE = '1'  INNER JOIN mock_subclass_mst T3  ON T2.MOCK_SUBCLASS_CD = T3.MOCK_SUBCLASS_CD   INNER JOIN      MOCK_MST T4  ON T2.MOCKCD = T4.MOCKCD");
			stb.append(" WHERE ");
			stb.append("     T1.GRADE || T1.HR_CLASS IN " + _classSelectedIn + " AND ");
			stb.append("     T1.semester = " + _ctrlSemester + " AND ");
			stb.append("     T1.year = " + _ctrlYear + " AND ");
			stb.append("     T2.MOCKCD = " + _mockCd + " AND ");
			stb.append("     T4.COMPANYCD = " + _companycd);
			stb.append(" GROUP BY ");
			stb.append("     T3.SUBCLASS_ABBV ");
			stb.append(" ORDER BY ");
			stb.append("     MOCK_SUBCLASS_CD ");

			PreparedStatement ps = null;
			ResultSet rs = null;

			log.debug(" sql = " + stb.toString());
			try {
				ps = db2.prepareStatement(stb.toString());
				rs = ps.executeQuery();
				while (rs.next()) {
					_moshiKamoku.add(rs.getString("SUBCLASS_ABBV"));
				}
			} catch (Exception e) {
				log.error("Exception", e);
			} finally {
				DbUtils.closeQuietly(null, ps, rs);
				db2.commit();
			}
		}
	}
}

// eof
