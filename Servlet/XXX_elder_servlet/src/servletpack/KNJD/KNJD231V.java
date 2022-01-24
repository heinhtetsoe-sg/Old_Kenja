/*
 * $Id: 5605bf57c2d0e48bd63b632b888fa14a76a35f36 $
 *
 * 作成日: 2014/09/24
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 成績不振者成績訂正処理
 * @version $Id: 5605bf57c2d0e48bd63b632b888fa14a76a35f36 $
 */
public class KNJD231V {

    private static final Log log = LogFactory.getLog(KNJD231V.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final int maxLine = 50;
        svf.VrSetForm("KNJD231V.frm", 1);
		final List pageList = getPageList(getList(db2) ,maxLine);

		for (int pi = 0; pi < pageList.size(); pi++) {
		    final List list = (List) pageList.get(pi);

	        // タイトル
	        if ("1".equals(_param._printDiv)) {
	            svf.VrsOut("TITLE", StringUtils.defaultString(_param._gradeName) + " " + StringUtils.defaultString(_param._semesterName) + " 追認定対象者一覧表"); // タイトル
            } else if ("2".equals(_param._printDiv)) {
                svf.VrsOut("TITLE", StringUtils.defaultString(_param._gradeName) + " " + StringUtils.defaultString(_param._semesterName) + " 追認定結果一覧表"); // タイトル
            }
	        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate)); // 印刷日
	        svf.VrsOut("PAGE", String.valueOf(pi + 1) + "頁"); // ページ

	        for (int j = 0; j < list.size(); j++) {
	            final PrintData data = (PrintData) list.get(j);
	            final int line = j + 1;
	            svf.VrsOutn("HR_CLASS", line, StringUtils.defaultString(data._hrName) + "-" + data.getPrintAttendno()); // 年組
                String nameNo = "";
                if ("1".equals(_param._use_SchregNo_hyoji)) {
                    svf.VrsOutn("SCHREGNO", line, data._schregno); // 学籍番号
                    nameNo = "_2"; // 学籍番号表示用の氏名フィールド
                }
	            final int namelen = getMS932ByteLength(data._name);
	            svf.VrsOutn("NAME" + (namelen > 30 ? "3" : namelen > 20 ? "2" : "1") + nameNo, line, data._name); // 氏名
                final int subclassnamelen = getMS932ByteLength(data._subclassname);
                svf.VrsOutn("SUBLECT" + (subclassnamelen > 30 ? "3" : subclassnamelen > 20 ? "2" : "1"), line, data._subclassname); // 科目名
	            svf.VrsOutn("HOPE_CREDIT", line, data._credits); // 見込単位数
	            svf.VrsOutn("DIV2", line, data._score); // 評定
	            svf.VrsOutn("COMP_CREDIT", line, data._compCredit); // 履修単位数
	            svf.VrsOutn("GET_CREDIT", line, data._getCredit); // 修得単位数
	            svf.VrsOutn("KETUJI", line, data._ketujisu); // 欠時数
	            svf.VrsOutn("LATE_EARLY", line, data._tikokuSoutai); // 遅刻早退
	            svf.VrsOutn("KEKKA", line, data._noticeLate); // 欠課時数
	            svf.VrsOutn("PRESENT", line, data._lesson); // 出席すべき日数
	        }
	        _hasData = true;
	        svf.VrEndPage();
		}
    }

    private static class PrintData {
        final String _hrName;
        final String _hrClass;
        final String _attendno;
        final String _schregno;
        final String _grade;
        final String _name;
        final String _subclassname;
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _score;
        final String _credits;
        final String _compCredit;
        final String _getCredit;
        final String _valuation;
        final String _stComp;
        final String _stGetAdd;
        final String _ketujisu;
        final String _tikokuSoutai;
        final String _noticeLate;
        final String _lesson;
        final String _compAbsenceHigh;
        final String _getAbsenceHigh;

        PrintData(
            final String hrName,
            final String hrClass,
            final String attendno,
            final String schregno,
            final String grade,
            final String name,
            final String subclassname,
            final String classcd,
            final String schoolKind,
            final String curriculumCd,
            final String subclasscd,
            final String score,
            final String credits,
            final String compCredit,
            final String getCredit,
            final String valuation,
            final String stComp,
            final String stGetAdd,
            final String ketujisu,
            final String tikokuSoutai,
            final String noticeLate,
            final String lesson,
            final String compAbsenceHigh,
            final String getAbsenceHigh
        ) {
            _hrName = hrName;
            _hrClass = hrClass;
            _attendno = attendno;
            _schregno = schregno;
            _grade = grade;
            _name = name;
            _subclassname = subclassname;
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _score = score;
            _credits = credits;
            _compCredit = compCredit;
            _getCredit = getCredit;
            _valuation = valuation;
            _stComp = stComp;
            _stGetAdd = stGetAdd;
            _ketujisu = ketujisu;
            _tikokuSoutai = tikokuSoutai;
            _noticeLate = noticeLate;
            _lesson = lesson;
            _compAbsenceHigh = compAbsenceHigh;
            _getAbsenceHigh = getAbsenceHigh;
        }

        public String getPrintAttendno() {
            if (!NumberUtils.isDigits(_attendno)) {
                return "";
            }
            final DecimalFormat df = new DecimalFormat("00");
            return df.format(Integer.parseInt(_attendno));
        }
    }

    private List getList(final DB2UDB db2) throws SQLException {
        String attend_sdate = "";
        String attend_seme = "";
        final List attend_month = new ArrayList();
        final String attendDateQuery = Knjd231vQuery.getAttendDate(_param);
        final PreparedStatement psa = db2.prepareStatement(attendDateQuery);
        final ResultSet rsa = psa.executeQuery();
        while (rsa.next()){
            final String tmp_attend_sdate = rsa.getString("MAX_YEAR") + "-" + rsa.getString("MONTH") + "-" + rsa.getString("MAX_APP");
            if (_param._date.compareTo(tmp_attend_sdate) < 0) break;
            attend_month.add(rsa.getString("MONTH"));
            attend_sdate = tmp_attend_sdate;
            attend_seme = rsa.getString("SEMESTER");
        }
        if ("".equals(attend_sdate)) {
            final String query2 = "SELECT SDATE FROM V_SEMESTER_GRADE_MST WHERE YEAR='" + _param._ctrlYear + "' AND SEMESTER='1' AND GRADE = '" + _param._grade + "' ";
            attend_sdate = Knjd231vQuery.getOne(db2, query2);   //学期開始日
        } else {
            final String query2 = "VALUES Add_days(date('" + attend_sdate + "'), 1)";
            attend_sdate = Knjd231vQuery.getOne(db2, query2);   //次の日
        }

        PreparedStatement ps = null;
        ResultSet rs = null;

        final List list = new ArrayList();
        try {
            final String query1 = Knjd231vQuery.selectListQuery(_param, attend_seme, attend_month, attend_sdate, _param._knjSchoolMst);
            log.debug(" sql =" + query1);
            ps = db2.prepareStatement(query1);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String hrName = rs.getString("HR_NAME");
                final String hrClass = rs.getString("HR_CLASS");
                final String attendno = rs.getString("ATTENDNO");
                final String schregno = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String name = rs.getString("NAME");
                final String subclassname = rs.getString("SUBCLASSNAME");
                final String classcd = rs.getString("CLASSCD");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subclasscd = rs.getString("SUBCLASSCD");
                final String score = rs.getString("SCORE");
                final String credits = rs.getString("CREDITS");
                final String compCredit = rs.getString("COMP_CREDIT");
                final String getCredit = rs.getString("GET_CREDIT");
                final String valuation = rs.getString("VALUATION");
                final String stComp = rs.getString("ST_COMP");
                final String stGetAdd = rs.getString("ST_GET_ADD");
                final String ketujisu = rs.getString("KETUJISU");
                final String tikokuSoutai = rs.getString("TIKOKU_SOUTAI");
                final String noticeLate = rs.getString("NOTICE_LATE");
                final String lesson = rs.getString("LESSON");
                final String compAbsenceHigh = rs.getString("COMP_ABSENCE_HIGH");
                final String getAbsenceHigh = rs.getString("GET_ABSENCE_HIGH");
                final PrintData printdata = new PrintData(hrName, hrClass, attendno, schregno, grade, name, subclassname, classcd, schoolKind, curriculumCd, subclasscd, score, credits, compCredit, getCredit, valuation, stComp, stGetAdd, ketujisu, tikokuSoutai, noticeLate, lesson, compAbsenceHigh, getAbsenceHigh);
                list.add(printdata);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private static List getPageList(final List list, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private static int getMS932ByteLength(final String s) {
        int rtn = 0;
        if (null != s) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return rtn;
    }

    private static class Knjd231vQuery {


        //出欠集計開始日付などを取得
        private static String getAttendDate(final Param param) {
            final String semester = ("9".equals(param._gakki2)) ? param._ctrlSemester : param._gakki2;
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT SEMESTER ");
            stb.append("      ,MAX(CASE WHEN MONTH BETWEEN '01' AND '03' THEN RTRIM(CHAR(INT(YEAR)+1)) ELSE YEAR END) AS MAX_YEAR ");
            stb.append("      ,MONTH ");
            stb.append("      ,MAX(APPOINTED_DAY) AS MAX_APP ");
            stb.append("FROM   ATTEND_SEMES_DAT ");
            stb.append("WHERE  YEAR='" + param._ctrlYear + "' AND SEMESTER <= '" + semester + "' ");
            stb.append("GROUP BY SEMESTER,MONTH ");
            stb.append("ORDER BY 2,3,1 ");

            return stb.toString();
        }

        //学期取得
        private static String getSelectSeme(final Param param) {
            String query = "";
            query +=  " SELECT DISTINCT ";
            query +=  "     SEMESTERNAME AS LABEL,";
            query +=  "     SEMESTER AS VALUE ";
            query +=  " FROM ";
            query +=  "     V_SEMESTER_GRADE_MST ";
            query +=  " WHERE ";
            query +=  "     YEAR = '" + param._ctrlYear + "' AND ";
            query +=  "     SEMESTER = '9' ";
            query +=  "     AND GRADE = '" + param._grade + "' ";
            query +=  " ORDER BY ";
            query +=  "     SEMESTER";

            return query;
        }

        //学年取得
        private static String getSelectGrade(final Param param) {
            String query = "";
            query +=  " SELECT ";
            query +=  "     GRADE_NAME1 AS LABEL, ";
            query +=  "     GRADE AS VALUE ";
            query +=  " FROM ";
            query +=  "     SCHREG_REGD_GDAT ";
            query +=  " WHERE ";
            query +=  "     YEAR = '" + param._ctrlYear + "' AND ";
            query +=  "     SCHOOL_KIND IN ('H', 'J') ";
            query +=  "     AND GRADE = '" + param._grade + "'";
            query +=  " ORDER BY ";
            query +=  "     INT(GRADE) ";

            return query;
        }

    /**************************************************************************************************/
    /**************************************************************************************************/
    /**************************************************************************************************/
        //CSV作成(成績不振者)
        private static String selectListQuery(final Param param, final String attend_seme, final List month, final String attend_sdate, final KNJSchoolMst knjSchoolMst) {
            final String semester = ("9".equals(param._gakki2)) ? param._ctrlSemester : param._gakki2;
            final String date = param._date;
            final String absent_cov      = knjSchoolMst._absentCov;
            final String absent_cov_late = knjSchoolMst._absentCovLate;
            final String amari_kuriage   = knjSchoolMst._amariKuriage;
            final String ctrl_year = param._ctrlYear;

            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHNO AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.COURSECD, ");
            stb.append("         T1.MAJORCD, ");
            stb.append("         T1.COURSECODE, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T1.ATTENDNO, ");
            stb.append("         T3.HR_NAME, ");
            stb.append("         T2.NAME ");
            stb.append("     FROM ");
            stb.append("         SCHREG_REGD_DAT T1");
            stb.append("     INNER JOIN ");
            stb.append("         SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("     INNER JOIN ");
            stb.append("         SCHREG_REGD_HDAT T3 ON T1.YEAR = T3.YEAR ");
            stb.append("                            AND T1.SEMESTER = T3.SEMESTER ");
            stb.append("                            AND T1.GRADE = T3.GRADE ");
            stb.append("                            AND T1.HR_CLASS = T3.HR_CLASS ");
            stb.append("     WHERE ");
            stb.append("             T1.YEAR     = '" + param._ctrlYear + "' ");
            stb.append("         AND T1.SEMESTER = '" + semester + "' ");
            stb.append("         AND VALUE(T2.GRD_DATE, '9999-12-31') > '" + date + "' ");
            if (!"99".equals(param._grade)) {
                stb.append("     AND T1.GRADE    = '" + param._grade + "' ");
            }
            stb.append(" ) ");

            stb.append(" ,ATTEND_SUBCLASS AS ( ");
            stb.append("     SELECT ");
            stb.append("         * ");
            stb.append("     FROM ");
            stb.append("         ATTEND_SUBCLASS_DAT ");
            stb.append("     WHERE ");
            stb.append("             YEAR = '" + ctrl_year + "' ");
            stb.append("         AND SEMESTER <= '" + attend_seme + "' ");
            stb.append("         AND MONTH IN ('" +  implode(month, "','")  + "') ");
            stb.append(" ) ");

            stb.append(" ,SCHEDULE AS ( ");
            stb.append("     SELECT ");
            stb.append("         EXECUTEDATE, ");
            stb.append("         PERIODCD, ");
            stb.append("         CHAIRCD ");
            stb.append("     FROM ");
            stb.append("         SCH_CHR_DAT ");
            stb.append("     WHERE ");
            stb.append("             EXECUTEDATE BETWEEN DATE('" + attend_sdate + "') AND DATE('" + date + "') ");
            stb.append("         AND PERIODCD != '0' ");
            stb.append("     GROUP BY ");
            stb.append("         EXECUTEDATE, ");
            stb.append("         PERIODCD, ");
            stb.append("         CHAIRCD ");
            stb.append(" ) ");

            stb.append(" ,T_attend_dat AS ( ");
            stb.append("     SELECT ");
            stb.append("         W1.SCHREGNO, ");
            stb.append("         W1.ATTENDDATE, ");
            stb.append("         W1.PERIODCD, ");
            stb.append("         CASE WHEN L1.ATSUB_REPL_DI_CD IS NOT NULL THEN L1.ATSUB_REPL_DI_CD ELSE L1.REP_DI_CD END AS DI_CD, ");
            stb.append("         L1.MULTIPLY ");
            stb.append("     FROM ");
            stb.append("         ATTEND_DAT W1 ");
            stb.append("         LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param._ctrlYear + "' AND L1.DI_CD = W1.DI_CD ");
            stb.append("     WHERE ");
            stb.append("             W1.ATTENDDATE BETWEEN DATE('" + attend_sdate + "') AND DATE('" + date + "') ");
            stb.append("         AND NOT EXISTS( SELECT ");
            stb.append("                             'X' ");
            stb.append("                         FROM ");
            stb.append("                             SCHREG_TRANSFER_DAT T7 ");
            stb.append("                         WHERE ");
            stb.append("                                 T7.SCHREGNO = W1.SCHREGNO ");
            stb.append("                             AND T7.TRANSFERCD IN('1','2') ");
            stb.append("                             AND W1.ATTENDDATE BETWEEN T7.TRANSFER_SDATE AND T7.TRANSFER_EDATE ) ");
            stb.append(" ) ");

            stb.append(" ,T_attend AS ( ");
            stb.append("     SELECT ");
            stb.append("         S1.SCHREGNO, ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     S1.CLASSCD, ");
                stb.append("     S1.SCHOOL_KIND, ");
                stb.append("     S1.CURRICULUM_CD, ");
            }
            stb.append("         S1.SUBCLASSCD, ");
            stb.append("         S1.SEMESTER, ");
            stb.append("         COUNT(S3.SCHREGNO)  AS OFFDAYS, ");
            stb.append("         SUM(CASE S2.DI_CD WHEN  '1' THEN 1 WHEN '8'  THEN 1 ELSE 0 END)  AS  ABSENT, ");
            stb.append("         SUM(CASE S2.DI_CD WHEN  '2' THEN 1 WHEN '9'  THEN 1 ELSE 0 END)  AS  SUSPEND, ");
            stb.append("         SUM(CASE S2.DI_CD WHEN  '3' THEN 1 WHEN '10' THEN 1 ELSE 0 END)  AS  MOURNING, ");
            stb.append("         SUM(CASE S2.DI_CD WHEN  '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END)  AS  SICK, ");
            stb.append("         SUM(CASE S2.DI_CD WHEN  '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END)  AS  NOTICE, ");
            stb.append("         SUM(CASE S2.DI_CD WHEN  '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END)  AS  NONOTICE, ");
            stb.append("         SUM(CASE S2.DI_CD WHEN '14' THEN 1 ELSE 0 END)  AS  NURSEOFF, ");
            stb.append("         SUM(CASE WHEN S2.DI_CD IN('15','23','24') THEN SMALLINT(VALUE(S2.MULTIPLY, '1')) ELSE 0 END)  AS  LATE,  ");
            stb.append("         SUM(CASE S2.DI_CD WHEN '16' THEN 1 ELSE 0 END)  AS  EARLY, ");
            stb.append("         SUM(CASE S2.DI_CD WHEN '19' THEN 1 WHEN '20' THEN 1 ELSE 0 END)  AS  VIRUS, ");
            stb.append("         SUM(CASE S2.DI_CD WHEN '25' THEN 1 WHEN '26' THEN 1 ELSE 0 END)  AS  KOUDOME ");
            stb.append("     FROM ");
            stb.append("         (SELECT ");
            stb.append("             T2.SCHREGNO, ");
            stb.append("             T1.EXECUTEDATE, ");
            stb.append("             T1.PERIODCD, ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         T3.CLASSCD, ");
                stb.append("         T3.SCHOOL_KIND, ");
                stb.append("         T3.CURRICULUM_CD, ");
            }
            stb.append("             T3.SUBCLASSCD, ");
            stb.append("             T2.SEMESTER ");
            stb.append("         FROM ");
            stb.append("             SCHEDULE T1, ");
            stb.append("             CHAIR_STD_DAT T2, ");
            stb.append("             CHAIR_DAT T3, ");
            stb.append("             SCHNO T4 ");
            stb.append("         WHERE ");
            stb.append("                 T1.CHAIRCD  = T3.CHAIRCD ");
            stb.append("             AND T3.YEAR     = '" + ctrl_year + "' ");
            stb.append("             AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            stb.append("             AND T2.YEAR     = '" + ctrl_year + "' ");
            stb.append("             AND T2.SEMESTER = T3.SEMESTER ");
            stb.append("             AND T2.CHAIRCD  = T1.CHAIRCD ");
            stb.append("             AND T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("             AND NOT EXISTS( SELECT ");
            stb.append("                                 'X' ");
            stb.append("                             FROM ");
            stb.append("                                 SCH_CHR_COUNTFLG T5 ");
            stb.append("                             WHERE ");
            stb.append("                                 T5.EXECUTEDATE  = T1.EXECUTEDATE AND ");
            stb.append("                                 T5.PERIODCD     = T1.PERIODCD AND ");
            stb.append("                                 T5.CHAIRCD      = T1.CHAIRCD AND ");
            stb.append("                                 T5.GRADE        = T4.GRADE AND ");
            stb.append("                                 T5.HR_CLASS     = T4.HR_CLASS AND ");
            stb.append("                                 T5.COUNTFLG     = '0' ");
            stb.append("                           ) ");
            stb.append("             AND NOT EXISTS( SELECT ");
            stb.append("                                 'X' ");
            stb.append("                             FROM ");
            stb.append("                                 SCHREG_BASE_MST T6 ");
            stb.append("                             WHERE ");
            stb.append("                                 T6.SCHREGNO = T4.SCHREGNO AND ");
            stb.append("                                 ((T6.GRD_DIV IN('1','2','3') AND T6.GRD_DATE < T1.EXECUTEDATE) OR ");
            stb.append("                                  (T6.ENT_DIV IN('4','5')     AND T6.ENT_DATE > T1.EXECUTEDATE)) ");
            stb.append("                           ) ");
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT L4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param._ctrlYear + "' AND L1.DI_CD = L4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       L4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND L4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND L4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
            stb.append("                  ) ");
            // 勤怠コード'28'は時間割にカウントしない
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param._ctrlYear + "' AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '28' ");
            stb.append("                  ) ");
            stb.append("         GROUP BY ");
            stb.append("             T2.SCHREGNO, ");
            stb.append("             T1.EXECUTEDATE, ");
            stb.append("             T1.PERIODCD, ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         T3.CLASSCD, ");
                stb.append("         T3.SCHOOL_KIND, ");
                stb.append("         T3.CURRICULUM_CD, ");
            }
            stb.append("             T3.SUBCLASSCD, ");
            stb.append("             T2.SEMESTER)S1 ");
            stb.append("     LEFT JOIN T_attend_dat        S2 ON  S2.SCHREGNO   = S1.SCHREGNO ");
            stb.append("                                      AND S2.ATTENDDATE = S1.EXECUTEDATE ");
            stb.append("                                      AND S2.PERIODCD   = S1.PERIODCD ");
            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT S3 ON  S3.SCHREGNO   = S1.SCHREGNO ");
            stb.append("                                      AND S3.TRANSFERCD = '2' ");
            stb.append("                                      AND S1.EXECUTEDATE BETWEEN S3.TRANSFER_SDATE AND S3.TRANSFER_EDATE ");
            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT S4 ON  S4.SCHREGNO   = S1.SCHREGNO ");
            stb.append("                                      AND S4.TRANSFERCD = '1' ");
            stb.append("                                      AND S1.EXECUTEDATE BETWEEN S4.TRANSFER_SDATE AND S4.TRANSFER_EDATE ");
            stb.append("     GROUP BY ");
            stb.append("         S1.SCHREGNO, ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     S1.CLASSCD, ");
                stb.append("     S1.SCHOOL_KIND, ");
                stb.append("     S1.CURRICULUM_CD, ");
            }
            stb.append("         S1.SUBCLASSCD, ");
            stb.append("         S1.SEMESTER ");
            stb.append(" ) ");

            stb.append(" ,ATTEND_SUM AS ( ");
            stb.append("     SELECT ");
            stb.append("         W1.SCHREGNO, ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     W1.CLASSCD, ");
                stb.append("     W1.SCHOOL_KIND, ");
                stb.append("     W1.CURRICULUM_CD, ");
            }
            stb.append("         W1.SUBCLASSCD, ");
            stb.append("         W1.SEMESTER, ");
            stb.append("         SUM(VALUE(W1.SICK,0) + VALUE(W1.NURSEOFF,0) ");
            if ("1".equals(knjSchoolMst._subAbsent)) {
                stb.append(           "+ VALUE(W1.ABSENT,0)");
            }
            if ("1".equals(knjSchoolMst._subSuspend)) {
                stb.append(           "+ VALUE(W1.SUSPEND,0)");
            }
            if ("1".equals(knjSchoolMst._subMourning)) {
                stb.append(           "+ VALUE(W1.MOURNING,0)");
            }
            if ("1".equals(knjSchoolMst._subOffDays)) {
                stb.append(           "+ VALUE(W1.OFFDAYS,0)");
            }
            if ("1".equals(knjSchoolMst._subVirus)) {
                stb.append(           "+ VALUE(W1.VIRUS,0)");
            }
            if ("1".equals(knjSchoolMst._subKoudome)) {
                stb.append(           "+ VALUE(W1.KOUDOME,0)");
            }
            stb.append("            ) AS SICK, ");
            stb.append("         SUM(VALUE(W1.NOTICE,0))     NOTICE, ");
            stb.append("         SUM(VALUE(W1.NONOTICE,0))   NONOTICE, ");
            stb.append("         SUM(VALUE(W1.LATE,0))       LATE, ");
            stb.append("         SUM(VALUE(W1.EARLY,0))      EARLY ");
            stb.append("     FROM ");
            stb.append("         ATTEND_SUBCLASS W1, ");
            stb.append("         SCHNO W0 ");
            stb.append("     WHERE ");
            stb.append("         W1.SCHREGNO = W0.SCHREGNO ");
            stb.append("     GROUP BY ");
            stb.append("         W1.SCHREGNO, ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     W1.CLASSCD, ");
                stb.append("     W1.SCHOOL_KIND, ");
                stb.append("     W1.CURRICULUM_CD, ");
            }
            stb.append("         W1.SUBCLASSCD, ");
            stb.append("         W1.SEMESTER ");
            stb.append("     UNION ALL ");
            stb.append("     SELECT ");
            stb.append("         W2.SCHREGNO, ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     W2.CLASSCD, ");
                stb.append("     W2.SCHOOL_KIND, ");
                stb.append("     W2.CURRICULUM_CD, ");
            }
            stb.append("         W2.SUBCLASSCD, ");
            stb.append("         W2.SEMESTER, ");
            stb.append("         VALUE(W2.SICK,0) + VALUE(W2.NURSEOFF,0) ");
            if ("1".equals(knjSchoolMst._subAbsent)) {
                stb.append("          + VALUE(W2.ABSENT,0)");
            }
            if ("1".equals(knjSchoolMst._subSuspend)) {
                stb.append("          + VALUE(W2.SUSPEND,0)");
            }
            if ("1".equals(knjSchoolMst._subMourning)) {
                stb.append("          + VALUE(W2.MOURNING,0)");
            }
            if ("1".equals(knjSchoolMst._subOffDays)) {
                stb.append("          + VALUE(W2.OFFDAYS,0)");
            }
            if ("1".equals(knjSchoolMst._subVirus)) {
                stb.append("          + VALUE(W2.VIRUS,0)");
            }
            if ("1".equals(knjSchoolMst._subKoudome)) {
                stb.append("          + VALUE(W2.KOUDOME,0)");
            }
            stb.append("                AS SICK, ");
            stb.append("         VALUE(W2.NOTICE,0)      NOTICE, ");
            stb.append("         VALUE(W2.NONOTICE,0)    NONOTICE, ");
            stb.append("         VALUE(W2.LATE,0)        LATE, ");
            stb.append("         VALUE(W2.EARLY,0)       EARLY ");
            stb.append("     FROM ");
            stb.append("         T_attend W2 ");
            stb.append(" ) ");

            //学期毎に清算
            stb.append(" ,ATTEND_SUM2 AS ( ");
            stb.append("     SELECT ");
            stb.append("         SCHREGNO, ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     CLASSCD, ");
                stb.append("     SCHOOL_KIND, ");
                stb.append("     CURRICULUM_CD, ");
            }
            stb.append("         SUBCLASSCD, ");
            stb.append("         SEMESTER, ");
            stb.append("         SUM(SICK)       SICK, ");
            stb.append("         SUM(NOTICE)     NOTICE, ");
            stb.append("         SUM(NONOTICE)   NONOTICE, ");
            stb.append("         SUM(LATE)       LATE, ");
            stb.append("         SUM(EARLY)      EARLY, ");
            if (("3".equals(absent_cov)) && (NumberUtils.isDigits(absent_cov_late) && Integer.parseInt(absent_cov_late) != 0)) {
                stb.append("      MOD((sum(LATE) + sum(EARLY)), " + absent_cov + ") AS TIKOKU_SOUTAI, ");
                stb.append("      decimal((float(sum(LATE) + sum(EARLY)) / " + absent_cov + ") + (sum(NOTICE) + sum(NONOTICE) + sum(SICK)),4,1) as NOTICE_LATE, ");
            } else if (("1".equals(absent_cov)) && (NumberUtils.isDigits(absent_cov_late) && Integer.parseInt(absent_cov_late) != 0)) {
                stb.append("      MOD((sum(LATE) + sum(EARLY)), " + absent_cov + ") AS TIKOKU_SOUTAI, ");
                stb.append("      ((sum(LATE) + sum(EARLY)) / " + absent_cov + ") + (sum(NOTICE) + sum(NONOTICE) + sum(SICK)) as NOTICE_LATE, ");
            } else {
                stb.append("      (SUM(LATE) + SUM(EARLY)) AS TIKOKU_SOUTAI, ");
                stb.append("      sum(NOTICE) + sum(NONOTICE) + sum(SICK) as NOTICE_LATE, ");
            }
            stb.append("         SUM(LATE) + SUM(EARLY) + SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE2, ");
            stb.append("         SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE3 ");
            stb.append("     FROM ");
            stb.append("         ATTEND_SUM ");
            stb.append("     GROUP BY ");
            stb.append("         SCHREGNO, ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     CLASSCD, ");
                stb.append("     SCHOOL_KIND, ");
                stb.append("     CURRICULUM_CD, ");
            }
            stb.append("         SUBCLASSCD, ");
            stb.append("         SEMESTER ");
            stb.append(" ) ");

            //年間で清算
            stb.append(" ,ATTEND_SUM3 AS ( ");
            stb.append("     SELECT ");
            stb.append("         SCHREGNO, ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     CLASSCD, ");
                stb.append("     SCHOOL_KIND, ");
                stb.append("     CURRICULUM_CD, ");
            }
            stb.append("         SUBCLASSCD, ");
            stb.append("         SUM(SICK)       SICK, ");
            stb.append("         SUM(NOTICE)     NOTICE, ");
            stb.append("         SUM(NONOTICE)   NONOTICE, ");
            stb.append("         SUM(LATE)       LATE, ");
            stb.append("         SUM(EARLY)      EARLY, ");
            stb.append("         SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS KETUJISU, ");
            if (("1".equals(absent_cov) || "3".equals(absent_cov)) && (NumberUtils.isDigits(absent_cov_late) && Integer.parseInt(absent_cov_late) != 0)) {
                stb.append("     SUM(TIKOKU_SOUTAI) TIKOKU_SOUTAI, "); //遅刻早退
                stb.append("     SUM(NOTICE_LATE) NOTICE_LATE, "); //欠課
            } else {
                if (("4".equals(absent_cov)) && (NumberUtils.isDigits(absent_cov_late) && Integer.parseInt(absent_cov_late) != 0)) {
                    stb.append("      MOD((SUM(LATE) + SUM(EARLY)), " + absent_cov_late + ") AS TIKOKU_SOUTAI, "); //遅刻早退
                    stb.append("      DECIMAL((FLOAT(SUM(LATE) + SUM(EARLY)) / " + absent_cov_late + ") + (SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK)),4,1) AS NOTICE_LATE, ");
                } else if (("2".equals(absent_cov)) && (NumberUtils.isDigits(absent_cov_late) && Integer.parseInt(absent_cov_late) != 0)) {
                    stb.append("      MOD((SUM(LATE) + SUM(EARLY)), " + absent_cov_late + ") AS TIKOKU_SOUTAI, "); //遅刻早退
                    stb.append("      ((SUM(LATE) + SUM(EARLY)) / " + absent_cov_late + ") + (SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK)) AS NOTICE_LATE, ");
                } else if (("5".equals(absent_cov)) && (NumberUtils.isDigits(absent_cov_late) && Integer.parseInt(absent_cov_late) != 0)) {
                    stb.append("      CASE WHEN MOD((SUM(LATE) + SUM(EARLY)) , " + absent_cov_late + ") >= " + amari_kuriage + " ");
                    stb.append("           THEN MOD((SUM(LATE) + SUM(EARLY)) , " + absent_cov_late + ") -  " + amari_kuriage + " ");
                    stb.append("           ELSE MOD((SUM(LATE) + SUM(EARLY)) , " + absent_cov_late + ") ");
                    stb.append("      END AS TIKOKU_SOUTAI, "); //遅刻早退
                    stb.append("      ((SUM(LATE) + SUM(EARLY)) / " + absent_cov_late + ") + (SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK)) + (CASE WHEN MOD((SUM(LATE) + SUM(EARLY)) , " + absent_cov + ") >= " + amari_kuriage + " ");
                    stb.append("                                                                                                          THEN 1 ");
                    stb.append("                                                                                                          ELSE 0 ");
                    stb.append("                                                                                                     END) AS NOTICE_LATE, ");
                } else {
                    stb.append("      SUM(LATE) + SUM(EARLY) AS TIKOKU_SOUTAI, ");
                    stb.append("      SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE, ");
                }
            }

            stb.append("         SUM(LATE) + SUM(EARLY) AS TIKOKU_SOUTAI1, ");
            stb.append("         SUM(LATE) + SUM(EARLY) + SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE2, ");
            stb.append("         SUM(NOTICE) + SUM(NONOTICE) + SUM(SICK) AS NOTICE_LATE3 ");
            stb.append("     FROM ");
            stb.append("         ATTEND_SUM2 ");
            stb.append("     WHERE ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     CLASSCD || '-' || ");
                stb.append("     SCHOOL_KIND || '-' || ");
                stb.append("     CURRICULUM_CD || '-' || ");
            }
            stb.append("         SUBCLASSCD NOT IN ( ");
            stb.append("                             SELECT ");
            if("9".equals(param._gakki2)) {
                //教育課程対応
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("                         ATTEND_CLASSCD || '-' || ");
                    stb.append("                         ATTEND_SCHOOL_KIND || '-' || ");
                    stb.append("                         ATTEND_CURRICULUM_CD || '-' || ");
                }
                stb.append("                             ATTEND_SUBCLASSCD ");
            } else {
                //教育課程対応
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("                         COMBINED_CLASSCD || '-' || ");
                    stb.append("                         COMBINED_SCHOOL_KIND || '-' || ");
                    stb.append("                         COMBINED_CURRICULUM_CD || '-' || ");
                }
                stb.append("                             COMBINED_SUBCLASSCD ");
            }
            stb.append("                             FROM ");
            stb.append("                                 SUBCLASS_REPLACE_COMBINED_DAT ");
            stb.append("                             WHERE ");
            stb.append("                                 YEAR = '" + ctrl_year + "' ");
            stb.append("                             ) ");
            stb.append("     GROUP BY ");
            stb.append("         SCHREGNO, ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     CLASSCD, ");
                stb.append("     SCHOOL_KIND, ");
                stb.append("     CURRICULUM_CD, ");
            }
            stb.append("         SUBCLASSCD ");

            //評定とか拾ってくる
            stb.append(" ), RECORD_SCORE AS( ");

            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            }
            stb.append("         T1.SUBCLASSCD, ");
            stb.append("         T3.SUBCLASSNAME, ");
            stb.append("         T1.SCORE, ");
            stb.append("         T4.CREDITS, ");
            stb.append("         T1.COMP_CREDIT, ");
            stb.append("         T1.GET_CREDIT, ");
            stb.append("         DECIMAL(ROUND(FLOAT(T5.AVG)*10,0)/10,5,1) AS VALUATION ");
            stb.append("     FROM ");
            stb.append("         RECORD_SCORE_DAT T1 ");
            stb.append("     INNER JOIN SCHNO           T2 ON  T2.SCHREGNO   = T1.SCHREGNO ");
            stb.append("     LEFT  JOIN SUBCLASS_MST    T3 ON  T3.SUBCLASSCD = T1.SUBCLASSCD ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("                               AND T3.CLASSCD = T1.CLASSCD ");
                stb.append("                               AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("                               AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("     LEFT  JOIN CREDIT_MST      T4 ON  T4.YEAR       = T1.YEAR ");
            stb.append("                                   AND T4.COURSECD   = T2.COURSECD ");
            stb.append("                                   AND T4.MAJORCD    = T2.MAJORCD ");
            stb.append("                                   AND T4.GRADE      = T2.GRADE ");
            stb.append("                                   AND T4.COURSECODE = T2.COURSECODE ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("                               AND T4.CLASSCD = T1.CLASSCD ");
                stb.append("                               AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("                               AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("                                   AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     LEFT  JOIN RECORD_RANK_SDIV_DAT T5 ON T5.YEAR   = T1.YEAR ");
            stb.append("                                   AND T5.SEMESTER   = '" + param._gakki2 + "' ");
            stb.append("                                   AND T5.TESTKINDCD = '99' ");
            stb.append("                                   AND T5.TESTITEMCD = '00' ");
            stb.append("                                   AND T5.SCORE_DIV  = '09' ");
            stb.append("                                   AND T5.SUBCLASSCD = '999999' ");
            stb.append("                                   AND T5.SCHREGNO   = T1.SCHREGNO ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR         = '" + ctrl_year + "' AND ");
            stb.append("         T1.SEMESTER     = '" + param._gakki2 + "' AND ");
            stb.append("         T1.TESTKINDCD   = '99' AND ");
            stb.append("         T1.TESTITEMCD   = '00' AND ");
            stb.append("         T1.SCORE_DIV    = '09' AND ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || '-' || ");
                stb.append("     T1.SCHOOL_KIND || '-' || ");
                stb.append("     T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("         T1.SUBCLASSCD NOT IN ( ");
            stb.append("                             SELECT ");
            if("9".equals(param._gakki2)) {
                //教育課程対応
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("                         ATTEND_CLASSCD || '-' || ");
                    stb.append("                         ATTEND_SCHOOL_KIND || '-' || ");
                    stb.append("                         ATTEND_CURRICULUM_CD || '-' || ");
                }
                stb.append("                             ATTEND_SUBCLASSCD ");
            } else {
                //教育課程対応
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("                         COMBINED_CLASSCD || '-' || ");
                    stb.append("                         COMBINED_SCHOOL_KIND || '-' || ");
                    stb.append("                         COMBINED_CURRICULUM_CD || '-' || ");
                }
                stb.append("                             COMBINED_SUBCLASSCD ");
            }
            stb.append("                             FROM ");
            stb.append("                                 SUBCLASS_REPLACE_COMBINED_DAT ");
            stb.append("                             WHERE ");
            stb.append("                                 YEAR = '" + ctrl_year + "' ");
            stb.append("                             ) ");
            //仮評定は除く
            stb.append("         AND NOT EXISTS(SELECT ");
            stb.append("                            'X' ");
            stb.append("                        FROM ");
            stb.append("                            RECORD_PROV_FLG_DAT P1 ");
            stb.append("                        WHERE ");
            stb.append("                                P1.YEAR = '" + ctrl_year + "' ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("                        AND P1.CLASSCD = T1.CLASSCD ");
                stb.append("                        AND P1.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("                        AND P1.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("                            AND P1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("                            AND P1.SCHREGNO = T1.SCHREGNO ");
            stb.append("                            AND P1.PROV_FLG = '1' "); //1:仮評定フラグ
            stb.append("                       ) ");

            //履修単位数・修得単位数 累計
            stb.append(" ), STUDYREC AS ( ");

            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     SUM(VALUE(T1.COMP_CREDIT, 0)) AS ST_COMP, ");
            stb.append("     SUM(VALUE(T1.GET_CREDIT, 0)) + SUM(VALUE(T1.ADD_CREDIT, 0)) AS ST_GET_ADD ");
            stb.append(" FROM ");
            stb.append("     SCHREG_STUDYREC_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR < '" + ctrl_year + "' ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SCHREGNO ");

            //授業時数を取得
            stb.append(" ), SUB_LESSON AS ( ");
            stb.append( getLessonSql(ctrl_year, attend_seme, month, attend_sdate, date, knjSchoolMst, param));

            /******************************/
            /* 教科・科目 or 総合的な時間 */
            /******************************/
                //対象の生徒を取得
                stb.append(" ), SUB_MAIN AS ( ");

                if ("1".equals(param._seisekiHusin2) || "1".equals(param._seisekiHusin3)) {
                    stb.append(" SELECT ");
                    stb.append("     T1.SCHREGNO, ");
                    //教育課程対応
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append(" T2.CLASSCD, ");
                        stb.append(" T2.SCHOOL_KIND, ");
                        stb.append(" T2.CURRICULUM_CD, ");
                    }
                    stb.append("     T2.SUBCLASSCD ");
                    if ("1".equals(knjSchoolMst._jugyouJisuFlg)) { //1：法定時数 2：実時数
                        stb.append(" FROM ");
                        stb.append("     SCHNO T1 ");
                        stb.append(" INNER JOIN ");
                        stb.append("     ATTEND_SUM3 T2 ON T2.SCHREGNO = T1.SCHREGNO ");
                        stb.append(" INNER JOIN ");
                        stb.append("     RECORD_SCORE T3 ON  T3.SCHREGNO   = T2.SCHREGNO ");
                        //教育課程対応
                        if ("1".equals(param._useCurriculumcd)) {
                            stb.append("                 AND T3.CLASSCD = T2.CLASSCD ");
                            stb.append("                 AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
                            stb.append("                 AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ");
                        }
                        stb.append("                     AND T3.SUBCLASSCD = T2.SUBCLASSCD ");
                        stb.append(" LEFT  JOIN ");
                        stb.append("     CREDIT_MST L1 ON  L1.YEAR       = '" + ctrl_year + "' ");
                        stb.append("                   AND L1.COURSECD   = T1.COURSECD ");
                        stb.append("                   AND L1.MAJORCD    = T1.MAJORCD ");
                        stb.append("                   AND L1.GRADE      = T1.GRADE ");
                        stb.append("                   AND L1.COURSECODE = T1.COURSECODE ");
                        //教育課程対応
                        if ("1".equals(param._useCurriculumcd)) {
                            stb.append("               AND L1.CLASSCD = T2.CLASSCD ");
                            stb.append("               AND L1.SCHOOL_KIND = T2.SCHOOL_KIND ");
                            stb.append("               AND L1.CURRICULUM_CD = T2.CURRICULUM_CD ");
                        }
                        stb.append("                   AND L1.SUBCLASSCD = T2.SUBCLASSCD ");
                    } else {
                        stb.append(" FROM ");
                        stb.append("     SCHNO T1 ");
                        stb.append(" INNER JOIN ");
                        stb.append("     ATTEND_SUM3 T2 ON T1.SCHREGNO = T2.SCHREGNO ");
                        stb.append(" INNER JOIN ");
                        stb.append("     RECORD_SCORE T3 ON  T3.SCHREGNO   = T2.SCHREGNO ");
                        //教育課程対応
                        if ("1".equals(param._useCurriculumcd)) {
                            stb.append("                 AND T3.CLASSCD = T2.CLASSCD ");
                            stb.append("                 AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
                            stb.append("                 AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ");
                        }
                        stb.append("                     AND T3.SUBCLASSCD = T2.SUBCLASSCD ");
                        stb.append(" LEFT  JOIN ");
                        stb.append("     SCHREG_ABSENCE_HIGH_DAT L1 ON  L1.YEAR       = '" + ctrl_year + "' ");
                        stb.append("                                AND L1.SCHREGNO   = T1.SCHREGNO ");
                        //教育課程対応
                        if ("1".equals(param._useCurriculumcd)) {
                            stb.append("                            AND L1.CLASSCD = T2.CLASSCD ");
                            stb.append("                            AND L1.SCHOOL_KIND = T2.SCHOOL_KIND ");
                            stb.append("                            AND L1.CURRICULUM_CD = T2.CURRICULUM_CD ");
                        }
                        stb.append("                                AND L1.SUBCLASSCD = T2.SUBCLASSCD ");
                        stb.append("                                AND L1.DIV        = '2' ");
                    }

                    if ("1".equals(param._seisekiHusin2) && "1".equals(param._seisekiHusin3)) {
                        stb.append(" WHERE ");
                        stb.append("     T2.NOTICE_LATE > L1.GET_ABSENCE_HIGH ");
                    } else if ("1".equals(param._seisekiHusin2)) {
                        stb.append(" WHERE ");
                        stb.append("     T2.NOTICE_LATE >  L1.GET_ABSENCE_HIGH AND ");
                        if ("1".equals(knjSchoolMst._jugyouJisuFlg)) { //1：法定時数 2：実時数
                            stb.append("     T2.NOTICE_LATE <= L1.ABSENCE_HIGH ");
                        } else {
                            stb.append("     T2.NOTICE_LATE <= L1.COMP_ABSENCE_HIGH ");
                        }
                    } else if ("1".equals(param._seisekiHusin3)) {
                        stb.append(" WHERE ");
                        if ("1".equals(knjSchoolMst._jugyouJisuFlg)) { //1：法定時数 2：実時数
                            stb.append("     T2.NOTICE_LATE > L1.ABSENCE_HIGH ");
                        } else {
                            stb.append("     T2.NOTICE_LATE > L1.COMP_ABSENCE_HIGH ");
                        }
                    }
                }

                if (("1".equals(param._seisekiHusin2) || "1".equals(param._seisekiHusin3)) && "1".equals(param._seisekiHusin1)) {
                    stb.append(" UNION ");
                }

                if ("1".equals(param._seisekiHusin1)) {
                    stb.append(" SELECT ");
                    stb.append("     T1.SCHREGNO, ");
                    //教育課程対応
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append(" T3.CLASSCD, ");
                        stb.append(" T3.SCHOOL_KIND, ");
                        stb.append(" T3.CURRICULUM_CD, ");
                    }
                    stb.append("     T3.SUBCLASSCD ");
                    stb.append(" FROM ");
                    stb.append("     SCHNO T1 ");
                    stb.append(" INNER JOIN ");
                    stb.append("     RECORD_SCORE T3 ON T3.SCHREGNO = T1.SCHREGNO ");

                    if ("1".equals(param._seisekiHusin1)) {
                        stb.append(" WHERE ");
                        if (NumberUtils.isDigits(param._seisekiHusinHyouteiFrom) && NumberUtils.isDigits(param._seisekiHusinHyouteiTo)) {
                            stb.append("    (T3.SCORE  >= " + param._seisekiHusinHyouteiFrom + " ");
                            stb.append(" AND T3.SCORE  <= " + param._seisekiHusinHyouteiTo + ") ");
                        } else if (NumberUtils.isDigits(param._seisekiHusinHyouteiTo)) {
                            stb.append("    (T3.SCORE  <= " + param._seisekiHusinHyouteiTo + " ");
                            stb.append(" OR T3.SCORE IS NULL) ");
                        } else {
                            stb.append(" T3.SCORE IS NULL ");
                        }
                    }
                }


                //メイン
                stb.append(" ) ");

            if (!"2".equals(param._printDiv)) {

                stb.append(" SELECT ");
                stb.append("     T1.HR_NAME, ");
                stb.append("     T1.HR_CLASS, ");
                stb.append("     T1.ATTENDNO, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T1.GRADE, ");
                stb.append("     T1.NAME, ");
                stb.append("     L2.SUBCLASSNAME, ");
                //教育課程対応
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(" L2.CLASSCD, ");
                    stb.append(" L2.SCHOOL_KIND, ");
                    stb.append(" L2.CURRICULUM_CD, ");
                }
                stb.append("     L2.SUBCLASSCD, ");
                stb.append("     T3.SCORE, ");
                stb.append("     T3.CREDITS, ");
                stb.append("     T3.COMP_CREDIT, ");
                stb.append("     T3.GET_CREDIT, ");
                stb.append("     T3.VALUATION, ");
                stb.append("     T4.ST_COMP, ");
                stb.append("     T4.ST_GET_ADD, ");
                stb.append("     T2.KETUJISU, "); //欠次数
                //遅刻表示フラグ対応
                if ("1".equals(param._chikokuHyoujiFlg)) {
                    stb.append("     T2.TIKOKU_SOUTAI1 AS TIKOKU_SOUTAI, "); //遅刻・早退
                } else {
                    stb.append("     T2.TIKOKU_SOUTAI, "); //遅刻・早退
                }
                stb.append("     T2.NOTICE_LATE, "); //欠課
                stb.append("     T5.MLESSON AS LESSON, "); //LESSON:授業時数、MLESSON:出席すべき時数
                if ("1".equals(knjSchoolMst._jugyouJisuFlg)) { //1：法定時数 2：実時数
                    stb.append("     L1.ABSENCE_HIGH     AS COMP_ABSENCE_HIGH, ");
                    stb.append("     L1.GET_ABSENCE_HIGH AS GET_ABSENCE_HIGH ");
                    stb.append(" FROM ");
                    stb.append("     SCHNO T1 ");
                    stb.append(" INNER JOIN ");
                    stb.append("     SUB_MAIN        M1 ON M1.SCHREGNO    = T1.SCHREGNO ");
                    stb.append(" LEFT JOIN ");
                    stb.append("     SUB_LESSON      T5 ON T5.SCHREGNO    = M1.SCHREGNO ");
                    //教育課程対応
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("                   AND T5.CLASSCD  = M1.CLASSCD ");
                        stb.append("                   AND T5.SCHOOL_KIND  = M1.SCHOOL_KIND ");
                        stb.append("                   AND T5.CURRICULUM_CD  = M1.CURRICULUM_CD ");
                    }
                    stb.append("                       AND T5.SUBCLASSCD  = M1.SUBCLASSCD ");
                    stb.append(" LEFT JOIN ");
                    stb.append("     ATTEND_SUM3     T2 ON T2.SCHREGNO    = M1.SCHREGNO ");
                    //教育課程対応
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("                   AND T2.CLASSCD  = M1.CLASSCD ");
                        stb.append("                   AND T2.SCHOOL_KIND  = M1.SCHOOL_KIND ");
                        stb.append("                   AND T2.CURRICULUM_CD  = M1.CURRICULUM_CD ");
                    }
                    stb.append("                       AND T2.SUBCLASSCD  = M1.SUBCLASSCD ");
                    stb.append(" LEFT JOIN ");
                    stb.append("     RECORD_SCORE    T3 ON  T3.SCHREGNO   = M1.SCHREGNO ");
                    //教育課程対応
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("                    AND T3.CLASSCD  = M1.CLASSCD ");
                        stb.append("                    AND T3.SCHOOL_KIND  = M1.SCHOOL_KIND ");
                        stb.append("                    AND T3.CURRICULUM_CD  = M1.CURRICULUM_CD ");
                    }
                    stb.append("                        AND T3.SUBCLASSCD = M1.SUBCLASSCD ");
                    stb.append(" LEFT  JOIN STUDYREC T4 ON T4.SCHREGNO    = T1.SCHREGNO ");
                    stb.append(" LEFT  JOIN ");
                    stb.append("     CREDIT_MST L1 ON  L1.YEAR       = '" + ctrl_year + "' ");
                    stb.append("                   AND L1.COURSECD   = T1.COURSECD ");
                    stb.append("                   AND L1.MAJORCD    = T1.MAJORCD ");
                    stb.append("                   AND L1.GRADE      = T1.GRADE ");
                    stb.append("                   AND L1.COURSECODE = T1.COURSECODE ");
                    //教育課程対応
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("               AND L1.CLASSCD  = M1.CLASSCD ");
                        stb.append("               AND L1.SCHOOL_KIND  = M1.SCHOOL_KIND ");
                        stb.append("               AND L1.CURRICULUM_CD  = M1.CURRICULUM_CD ");
                    }
                    stb.append("                   AND L1.SUBCLASSCD = M1.SUBCLASSCD ");
                    stb.append(" LEFT  JOIN ");
                    stb.append("     SUBCLASS_MST L2 ON  L2.SUBCLASSCD = M1.SUBCLASSCD ");
                    //教育課程対応
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("                 AND L2.CLASSCD  = M1.CLASSCD ");
                        stb.append("                 AND L2.SCHOOL_KIND  = M1.SCHOOL_KIND ");
                        stb.append("                 AND L2.CURRICULUM_CD  = M1.CURRICULUM_CD ");
                    }
                } else {
                    stb.append("     L1.COMP_ABSENCE_HIGH AS COMP_ABSENCE_HIGH, ");
                    stb.append("     L1.GET_ABSENCE_HIGH  AS GET_ABSENCE_HIGH ");
                    stb.append(" FROM ");
                    stb.append("     SCHNO T1 ");
                    stb.append(" INNER JOIN ");
                    stb.append("     SUB_MAIN    M1 ON M1.SCHREGNO = T1.SCHREGNO ");
                    stb.append(" LEFT JOIN ");
                    stb.append("     SUB_LESSON      T5 ON T5.SCHREGNO    = M1.SCHREGNO ");
                    //教育課程対応
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("                   AND T5.CLASSCD  = M1.CLASSCD ");
                        stb.append("                   AND T5.SCHOOL_KIND  = M1.SCHOOL_KIND ");
                        stb.append("                   AND T5.CURRICULUM_CD  = M1.CURRICULUM_CD ");
                    }
                    stb.append("                       AND T5.SUBCLASSCD  = M1.SUBCLASSCD ");
                    stb.append(" LEFT JOIN ");
                    stb.append("     ATTEND_SUM3     T2 ON T2.SCHREGNO    = M1.SCHREGNO ");
                    //教育課程対応
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("                   AND T2.CLASSCD  = M1.CLASSCD ");
                        stb.append("                   AND T2.SCHOOL_KIND  = M1.SCHOOL_KIND ");
                        stb.append("                   AND T2.CURRICULUM_CD  = M1.CURRICULUM_CD ");
                    }
                    stb.append("                       AND T2.SUBCLASSCD  = M1.SUBCLASSCD ");
                    stb.append(" LEFT JOIN ");
                    stb.append("     RECORD_SCORE    T3 ON  T3.SCHREGNO   = M1.SCHREGNO ");
                    //教育課程対応
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("                    AND T3.CLASSCD  = M1.CLASSCD ");
                        stb.append("                    AND T3.SCHOOL_KIND  = M1.SCHOOL_KIND ");
                        stb.append("                    AND T3.CURRICULUM_CD  = M1.CURRICULUM_CD ");
                    }
                    stb.append("                        AND T3.SUBCLASSCD = M1.SUBCLASSCD ");
                    stb.append(" LEFT  JOIN STUDYREC T4 ON  T4.SCHREGNO   = T1.SCHREGNO ");
                    stb.append(" LEFT  JOIN ");
                    stb.append("     SCHREG_ABSENCE_HIGH_DAT L1 ON  L1.YEAR       = '" + ctrl_year + "' ");
                    stb.append("                                AND L1.SCHREGNO   = M1.SCHREGNO ");
                    //教育課程対応
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("                            AND L1.CLASSCD  = M1.CLASSCD ");
                        stb.append("                            AND L1.SCHOOL_KIND  = M1.SCHOOL_KIND ");
                        stb.append("                            AND L1.CURRICULUM_CD  = M1.CURRICULUM_CD ");
                    }
                    stb.append("                                AND L1.SUBCLASSCD = M1.SUBCLASSCD ");
                    stb.append("                                AND L1.DIV        = '2' ");
                    stb.append(" LEFT  JOIN ");
                    stb.append("     SUBCLASS_MST L2 ON  L2.SUBCLASSCD = M1.SUBCLASSCD ");
                    //教育課程対応
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("                 AND L2.CLASSCD  = M1.CLASSCD ");
                        stb.append("                 AND L2.SCHOOL_KIND  = M1.SCHOOL_KIND ");
                        stb.append("                 AND L2.CURRICULUM_CD  = M1.CURRICULUM_CD ");
                    }
                }
                if ("1".equals(param._kyoukaSougou1) || "1".equals(param._kyoukaSougou2)) {
                    stb.append(" WHERE ");
                }
                if ("1".equals(param._kyoukaSougou1) && "1".equals(param._kyoukaSougou2)) {
                    stb.append("     SUBSTR(M1.SUBCLASSCD,1,2) <= '90' ");
                } else if ("1".equals(param._kyoukaSougou1)) {
                    stb.append("     SUBSTR(M1.SUBCLASSCD,1,2) < '90' ");
                } else if ("1".equals(param._kyoukaSougou2)) {
                    stb.append("     SUBSTR(M1.SUBCLASSCD,1,2) = '90' ");
                }
                //科目
                if (param._subclasscd != null && !"".equals(param._subclasscd)) {
                    if ("1".equals(param._kyoukaSougou1) || "1".equals(param._kyoukaSougou2)) {
                        stb.append(" AND ");
                    } else {
                        stb.append(" WHERE ");
                    }
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append(" L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || L2.SUBCLASSCD = '" + param._subclasscd + "' ");
                    } else {
                        stb.append(" L2.SUBCLASSCD = '" + param._subclasscd + "' ");
                    }
                }

            }

            //RECORD_SCORE_BEF_AFT_DAT・・・更新前後データ参照
            if ("1".equals(param._printDiv) || "2".equals(param._printDiv)) {

                if ("1".equals(param._printDiv)) {
                    stb.append(" UNION ");
                }
                stb.append(" SELECT ");
                stb.append("     T1.HR_NAME, ");
                stb.append("     T1.HR_CLASS, ");
                stb.append("     T1.ATTENDNO, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T1.GRADE, ");
                stb.append("     T1.NAME, ");
                stb.append("     L2.SUBCLASSNAME, ");
                //教育課程対応
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append(" L2.CLASSCD, ");
                    stb.append(" L2.SCHOOL_KIND, ");
                    stb.append(" L2.CURRICULUM_CD, ");
                }
                stb.append("     L2.SUBCLASSCD, ");
                if ("1".equals(param._printDiv)) {
                    stb.append("     M1.BEF_SCORE AS SCORE, ");
                    stb.append("     T3.CREDITS, ");
                    stb.append("     M1.BEF_COMP_CREDIT AS COMP_CREDIT, ");
                    stb.append("     M1.BEF_GET_CREDIT AS GET_CREDIT, ");
                } else if ("2".equals(param._printDiv)) {
                    stb.append("     M1.AFT_SCORE AS SCORE, ");
                    stb.append("     T3.CREDITS, ");
                    stb.append("     M1.AFT_COMP_CREDIT AS COMP_CREDIT, ");
                    stb.append("     M1.AFT_GET_CREDIT AS GET_CREDIT, ");
                }
                stb.append("     T3.VALUATION, ");
                stb.append("     T4.ST_COMP, ");
                stb.append("     T4.ST_GET_ADD, ");
                stb.append("     T2.KETUJISU, "); //欠次数
                //遅刻表示フラグ対応
                if ("1".equals(param._chikokuHyoujiFlg)) {
                    stb.append("     T2.TIKOKU_SOUTAI1 AS TIKOKU_SOUTAI, "); //遅刻・早退
                } else {
                    stb.append("     T2.TIKOKU_SOUTAI, "); //遅刻・早退
                }
                stb.append("     T2.NOTICE_LATE, "); //欠課
                stb.append("     T5.MLESSON AS LESSON, "); //LESSON:授業時数、MLESSON:出席すべき時数
                if ("1".equals(knjSchoolMst._jugyouJisuFlg)) { //1：法定時数 2：実時数
                    stb.append("     L1.ABSENCE_HIGH     AS COMP_ABSENCE_HIGH, ");
                    stb.append("     L1.GET_ABSENCE_HIGH AS GET_ABSENCE_HIGH ");
                    stb.append(" FROM ");
                    stb.append("     SCHNO T1 ");
                    stb.append(" INNER JOIN RECORD_SCORE_BEF_AFT_DAT M1 ");
                    stb.append("        ON  M1.YEAR = '" + ctrl_year + "' ");
                    stb.append("        AND M1.SEMESTER = '9' ");
                    stb.append("        AND M1.TESTKINDCD = '99' ");
                    stb.append("        AND M1.TESTITEMCD = '00' ");
                    stb.append("        AND M1.SCORE_DIV = '09' ");
                    stb.append("        AND M1.SCHREGNO = T1.SCHREGNO ");
                    stb.append(" LEFT JOIN ");
                    stb.append("     SUB_LESSON      T5 ON T5.SCHREGNO    = M1.SCHREGNO ");
                    //教育課程対応
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("                   AND T5.CLASSCD  = M1.CLASSCD ");
                        stb.append("                   AND T5.SCHOOL_KIND  = M1.SCHOOL_KIND ");
                        stb.append("                   AND T5.CURRICULUM_CD  = M1.CURRICULUM_CD ");
                    }
                    stb.append("                       AND T5.SUBCLASSCD  = M1.SUBCLASSCD ");
                    stb.append(" LEFT JOIN ");
                    stb.append("     ATTEND_SUM3     T2 ON T2.SCHREGNO    = M1.SCHREGNO ");
                    //教育課程対応
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("                   AND T2.CLASSCD  = M1.CLASSCD ");
                        stb.append("                   AND T2.SCHOOL_KIND  = M1.SCHOOL_KIND ");
                        stb.append("                   AND T2.CURRICULUM_CD  = M1.CURRICULUM_CD ");
                    }
                    stb.append("                       AND T2.SUBCLASSCD  = M1.SUBCLASSCD ");
                    stb.append(" LEFT JOIN ");
                    stb.append("     RECORD_SCORE    T3 ON  T3.SCHREGNO   = M1.SCHREGNO ");
                    //教育課程対応
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("                    AND T3.CLASSCD  = M1.CLASSCD ");
                        stb.append("                    AND T3.SCHOOL_KIND  = M1.SCHOOL_KIND ");
                        stb.append("                    AND T3.CURRICULUM_CD  = M1.CURRICULUM_CD ");
                    }
                    stb.append("                        AND T3.SUBCLASSCD = M1.SUBCLASSCD ");
                    stb.append(" LEFT  JOIN STUDYREC T4 ON T4.SCHREGNO    = T1.SCHREGNO ");
                    stb.append(" LEFT  JOIN ");
                    stb.append("     CREDIT_MST L1 ON  L1.YEAR       = '" + ctrl_year + "' ");
                    stb.append("                   AND L1.COURSECD   = T1.COURSECD ");
                    stb.append("                   AND L1.MAJORCD    = T1.MAJORCD ");
                    stb.append("                   AND L1.GRADE      = T1.GRADE ");
                    stb.append("                   AND L1.COURSECODE = T1.COURSECODE ");
                    //教育課程対応
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("               AND L1.CLASSCD  = M1.CLASSCD ");
                        stb.append("               AND L1.SCHOOL_KIND  = M1.SCHOOL_KIND ");
                        stb.append("               AND L1.CURRICULUM_CD  = M1.CURRICULUM_CD ");
                    }
                    stb.append("                   AND L1.SUBCLASSCD = M1.SUBCLASSCD ");
                    stb.append(" LEFT  JOIN ");
                    stb.append("     SUBCLASS_MST L2 ON  L2.SUBCLASSCD = M1.SUBCLASSCD ");
                    //教育課程対応
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("                 AND L2.CLASSCD  = M1.CLASSCD ");
                        stb.append("                 AND L2.SCHOOL_KIND  = M1.SCHOOL_KIND ");
                        stb.append("                 AND L2.CURRICULUM_CD  = M1.CURRICULUM_CD ");
                    }
                } else {
                    stb.append("     L1.COMP_ABSENCE_HIGH AS COMP_ABSENCE_HIGH, ");
                    stb.append("     L1.GET_ABSENCE_HIGH  AS GET_ABSENCE_HIGH ");
                    stb.append(" FROM ");
                    stb.append("     SCHNO T1 ");
                    stb.append(" INNER JOIN RECORD_SCORE_BEF_AFT_DAT M1 ");
                    stb.append("        ON  M1.YEAR = '" + ctrl_year + "' ");
                    stb.append("        AND M1.SEMESTER = '9' ");
                    stb.append("        AND M1.TESTKINDCD = '99' ");
                    stb.append("        AND M1.TESTITEMCD = '00' ");
                    stb.append("        AND M1.SCORE_DIV = '09' ");
                    stb.append("        AND M1.SCHREGNO = T1.SCHREGNO ");
                    stb.append(" LEFT JOIN ");
                    stb.append("     SUB_LESSON      T5 ON T5.SCHREGNO    = M1.SCHREGNO ");
                    //教育課程対応
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("                   AND T5.CLASSCD  = M1.CLASSCD ");
                        stb.append("                   AND T5.SCHOOL_KIND  = M1.SCHOOL_KIND ");
                        stb.append("                   AND T5.CURRICULUM_CD  = M1.CURRICULUM_CD ");
                    }
                    stb.append("                       AND T5.SUBCLASSCD  = M1.SUBCLASSCD ");
                    stb.append(" LEFT JOIN ");
                    stb.append("     ATTEND_SUM3     T2 ON T2.SCHREGNO    = M1.SCHREGNO ");
                    //教育課程対応
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("                   AND T2.CLASSCD  = M1.CLASSCD ");
                        stb.append("                   AND T2.SCHOOL_KIND  = M1.SCHOOL_KIND ");
                        stb.append("                   AND T2.CURRICULUM_CD  = M1.CURRICULUM_CD ");
                    }
                    stb.append("                       AND T2.SUBCLASSCD  = M1.SUBCLASSCD ");
                    stb.append(" LEFT JOIN ");
                    stb.append("     RECORD_SCORE    T3 ON  T3.SCHREGNO   = M1.SCHREGNO ");
                    //教育課程対応
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("                    AND T3.CLASSCD  = M1.CLASSCD ");
                        stb.append("                    AND T3.SCHOOL_KIND  = M1.SCHOOL_KIND ");
                        stb.append("                    AND T3.CURRICULUM_CD  = M1.CURRICULUM_CD ");
                    }
                    stb.append("                        AND T3.SUBCLASSCD = M1.SUBCLASSCD ");
                    stb.append(" LEFT  JOIN STUDYREC T4 ON  T4.SCHREGNO   = T1.SCHREGNO ");
                    stb.append(" LEFT  JOIN ");
                    stb.append("     SCHREG_ABSENCE_HIGH_DAT L1 ON  L1.YEAR       = '" + ctrl_year + "' ");
                    stb.append("                                AND L1.SCHREGNO   = M1.SCHREGNO ");
                    //教育課程対応
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("                            AND L1.CLASSCD  = M1.CLASSCD ");
                        stb.append("                            AND L1.SCHOOL_KIND  = M1.SCHOOL_KIND ");
                        stb.append("                            AND L1.CURRICULUM_CD  = M1.CURRICULUM_CD ");
                    }
                    stb.append("                                AND L1.SUBCLASSCD = M1.SUBCLASSCD ");
                    stb.append("                                AND L1.DIV        = '2' ");
                    stb.append(" LEFT  JOIN ");
                    stb.append("     SUBCLASS_MST L2 ON  L2.SUBCLASSCD = M1.SUBCLASSCD ");
                    //教育課程対応
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("                 AND L2.CLASSCD  = M1.CLASSCD ");
                        stb.append("                 AND L2.SCHOOL_KIND  = M1.SCHOOL_KIND ");
                        stb.append("                 AND L2.CURRICULUM_CD  = M1.CURRICULUM_CD ");
                    }
                }
                if ("1".equals(param._kyoukaSougou1) || "1".equals(param._kyoukaSougou2)) {
                    stb.append(" WHERE ");
                }
                if ("1".equals(param._kyoukaSougou1) && "1".equals(param._kyoukaSougou2)) {
                    stb.append("     SUBSTR(M1.SUBCLASSCD,1,2) <= '90' ");
                } else if ("1".equals(param._kyoukaSougou1)) {
                    stb.append("     SUBSTR(M1.SUBCLASSCD,1,2) < '90' ");
                } else if ("1".equals(param._kyoukaSougou2)) {
                    stb.append("     SUBSTR(M1.SUBCLASSCD,1,2) = '90' ");
                }
                //科目
                if (param._subclasscd != null && !"".equals(param._subclasscd)) {
                    if ("1".equals(param._kyoukaSougou1) || "1".equals(param._kyoukaSougou2)) {
                        stb.append(" AND ");
                    } else {
                        stb.append(" WHERE ");
                    }
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append(" L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || L2.SUBCLASSCD = '" + param._subclasscd + "' ");
                    } else {
                        stb.append(" L2.SUBCLASSCD = '" + param._subclasscd + "' ");
                    }
                }

            }

            stb.append(" ORDER BY ");
            stb.append("     GRADE, ");
            stb.append("     HR_CLASS, ");
            stb.append("     ATTENDNO, ");
            stb.append("     SCHREGNO, ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" CLASSCD, ");
                stb.append(" SCHOOL_KIND, ");
                stb.append(" CURRICULUM_CD, ");
            }
            stb.append("     SUBCLASSCD ");

            return stb.toString();
        }

        //授業時数(LESSON) 出席すべき時数(MLESSON)
        private static String getLessonSql(final String ctrl_year, final String attend_seme, final List month, final String attend_sdate, final String date, final KNJSchoolMst knjSchoolMst, final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" TT1.CLASSCD, ");
                stb.append(" TT1.SCHOOL_KIND, ");
                stb.append(" TT1.CURRICULUM_CD, ");
            }
            stb.append("     TT1.SUBCLASSCD, ");
            stb.append("     TT1.SCHREGNO, ");
            stb.append("     SUM(VALUE(TT1.LESSON,0)) AS LESSON, ");
            stb.append("     SUM(VALUE(TT1.MLESSON,0)) AS MLESSON ");
            stb.append(" FROM ");
            stb.append("     ( ");
            //集計テーブル参照
            stb.append("     SELECT ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     CLASSCD, ");
                stb.append("     SCHOOL_KIND, ");
                stb.append("     CURRICULUM_CD, ");
            }
            stb.append("         SUBCLASSCD, ");
            stb.append("         SCHREGNO, ");
            if ("1".equals(knjSchoolMst._subOffDays)) {
                stb.append("     SUM(VALUE(LESSON,0) - VALUE(ABROAD,0)) AS LESSON, ");
                stb.append("     SUM(VALUE(LESSON,0) - VALUE(ABROAD,0) ");
                if (!"1".equals(knjSchoolMst._subSuspend)) {
                    stb.append(           " - VALUE(SUSPEND,0) ");
                }
                if (!"1".equals(knjSchoolMst._subMourning)) {
                    stb.append(           " - VALUE(MOURNING,0) ");
                }
                if (!"1".equals(knjSchoolMst._subVirus) && "true".equals(param._useVirus)) {
                    stb.append(           " - VALUE(VIRUS,0) ");
                }
                if (!"1".equals(knjSchoolMst._subKoudome) && "true".equals(param._useKoudome)) {
                    stb.append(           " - VALUE(KOUDOME,0) ");
                }
                stb.append(" ) AS MLESSON ");
            } else {
                stb.append("     SUM(VALUE(LESSON,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0)) AS LESSON, ");
                stb.append("     SUM(VALUE(LESSON,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ");
                if (!"1".equals(knjSchoolMst._subSuspend)) {
                    stb.append(           " - VALUE(SUSPEND,0) ");
                }
                if (!"1".equals(knjSchoolMst._subMourning)) {
                    stb.append(           " - VALUE(MOURNING,0) ");
                }
                if (!"1".equals(knjSchoolMst._subVirus) && "true".equals(param._useVirus)) {
                    stb.append(           " - VALUE(VIRUS,0) ");
                }
                if (!"1".equals(knjSchoolMst._subKoudome) && "true".equals(param._useKoudome)) {
                    stb.append(           " - VALUE(KOUDOME,0) ");
                }
                stb.append(" ) AS MLESSON ");
            }
            stb.append("     FROM ");
            stb.append("         ATTEND_SUBCLASS_DAT ");
            stb.append("     WHERE ");
            stb.append("         YEAR = '" + ctrl_year + "' ");
            stb.append("         AND SEMESTER <= '" + attend_seme + "' ");
            stb.append("         AND MONTH IN ('" +  implode(month, "','")  + "') ");
            stb.append("     GROUP BY ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     CLASSCD, ");
                stb.append("     SCHOOL_KIND, ");
                stb.append("     CURRICULUM_CD, ");
            }
            stb.append("         SUBCLASSCD, ");
            stb.append("         SCHREGNO ");
            stb.append("     UNION ALL ");
            //時間割テーブル参照
            stb.append("     SELECT ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     TBL.CLASSCD, ");
                stb.append("     TBL.SCHOOL_KIND, ");
                stb.append("     TBL.CURRICULUM_CD, ");
            }
            stb.append("         TBL.SUBCLASSCD, ");
            stb.append("         TBL.SCHREGNO, ");
            stb.append("         SUM(LESSON) AS LESSON, ");
            stb.append("         SUM(LESSON ");
            if (!"1".equals(knjSchoolMst._subSuspend)) {
                stb.append(           " - SUSPEND");
            }
            if (!"1".equals(knjSchoolMst._subMourning)) {
                stb.append(           " - MOURNING");
            }
            if (!"1".equals(knjSchoolMst._subVirus) && "true".equals(param._useVirus)) {
                stb.append(           " - VIRUS");
            }
            if (!"1".equals(knjSchoolMst._subKoudome) && "true".equals(param._useKoudome)) {
                stb.append(           " - KOUDOME");
            }
            stb.append("         ) AS MLESSON ");
            stb.append("     FROM ");
            stb.append("         ( ");
            stb.append("         SELECT ");
            stb.append("             T2.SCHREGNO, ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         T4.CLASSCD, ");
                stb.append("         T4.SCHOOL_KIND, ");
                stb.append("         T4.CURRICULUM_CD, ");
            }
            stb.append("             T4.SUBCLASSCD, ");
            stb.append("             T1.EXECUTEDATE, ");
            stb.append("             T1.PERIODCD, ");
            stb.append("             1 AS LESSON, ");
            stb.append("             (CASE WHEN VALUE(L1.DI_CD, '0') IN ( '2',  '9') THEN 1 ELSE 0 END) AS SUSPEND, ");
            stb.append("             (CASE WHEN VALUE(L1.DI_CD, '0') IN ( '3', '10') THEN 1 ELSE 0 END) AS MOURNING, ");
            stb.append("             (CASE WHEN VALUE(L1.DI_CD, '0') IN ('19', '20') THEN 1 ELSE 0 END) AS VIRUS, ");
            stb.append("             (CASE WHEN VALUE(L1.DI_CD, '0') IN ('25', '26') THEN 1 ELSE 0 END) AS KOUDOME ");
            stb.append("         FROM ");
            stb.append("             SCH_CHR_DAT     T1 ");
            stb.append("         INNER JOIN ");
            stb.append("             CHAIR_STD_DAT   T2 ON  T2.YEAR     = T1.YEAR ");
            stb.append("                                AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("                                AND T2.CHAIRCD  = T1.CHAIRCD ");
            stb.append("         INNER JOIN ");
            stb.append("             SCHREG_REGD_DAT T3 ON  T3.SCHREGNO = T2.SCHREGNO ");
            stb.append("                                AND T3.YEAR     = T1.YEAR ");
            stb.append("                                AND T3.SEMESTER = T1.SEMESTER ");
            stb.append("         INNER JOIN ");
            stb.append("             CHAIR_DAT       T4 ON  T4.YEAR     = T1.YEAR ");
            stb.append("                                AND T4.SEMESTER = T1.SEMESTER ");
            stb.append("                                AND T4.CHAIRCD  = T1.CHAIRCD ");
            stb.append("         LEFT JOIN ");
            stb.append("             ATTEND_DAT      L1 ON  L1.PERIODCD   = T1.PERIODCD ");
            stb.append("                                AND L1.CHAIRCD    = T1.CHAIRCD ");
            stb.append("                                AND L1.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                                AND L1.SCHREGNO   = T2.SCHREGNO ");
            stb.append("         WHERE T1.YEAR     = '" + ctrl_year + "' ");
            stb.append("            AND T1.EXECUTEDATE BETWEEN DATE('" + attend_sdate + "') AND DATE('" + date + "') ");
            stb.append("            AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            stb.append("            AND NOT EXISTS(SELECT 'X' FROM SCH_CHR_COUNTFLG E1 ");
            stb.append("                            WHERE E1.EXECUTEDATE   = T1.EXECUTEDATE AND ");
            stb.append("                                  E1.PERIODCD      = T1.PERIODCD AND ");
            stb.append("                                  E1.CHAIRCD       = T1.CHAIRCD AND ");
            stb.append("                                  E1.GRADE         = T3.GRADE AND ");
            stb.append("                                  E1.HR_CLASS      = T3.HR_CLASS AND ");
            stb.append("                                  E1.COUNTFLG      = '0') ");
            stb.append("            AND NOT EXISTS(SELECT 'X' FROM SCHREG_BASE_MST E2 ");
            stb.append("                            WHERE E2.SCHREGNO = T2.SCHREGNO AND ");
            stb.append("                                  ((E2.GRD_DIV IN('1','2','3') AND E2.GRD_DATE < T1.EXECUTEDATE) OR ");
            stb.append("                                   (E2.ENT_DIV IN('4','5')     AND E2.ENT_DATE > T1.EXECUTEDATE))) ");
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT L4 ");
            stb.append("                   LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param._ctrlYear + "' AND L1.DI_CD = L4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       L4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND L4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND L4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
            stb.append("                  ) ");
            // 勤怠コード'28'は時間割にカウントしない
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                   LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param._ctrlYear + "' AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '28' ");
            stb.append("                  ) ");
            stb.append("            AND NOT EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT E3 ");
            stb.append("                            WHERE E3.SCHREGNO = T2.SCHREGNO AND ");
            if ("1".equals(knjSchoolMst._subOffDays)) {
                stb.append("                              E3.TRANSFERCD IN('1') AND ");
            } else {
                stb.append("                              E3.TRANSFERCD IN('1','2') AND ");
            }
            stb.append("                                  T1.EXECUTEDATE BETWEEN E3.TRANSFER_SDATE AND E3.TRANSFER_EDATE) ");
            stb.append("         GROUP BY T2.SCHREGNO, ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         T4.CLASSCD, ");
                stb.append("         T4.SCHOOL_KIND, ");
                stb.append("         T4.CURRICULUM_CD, ");
            }
            stb.append("             T4.SUBCLASSCD, T1.EXECUTEDATE, T1.PERIODCD, L1.DI_CD ");
            stb.append("         ) TBL ");
            stb.append("     GROUP BY TBL.SCHREGNO, ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     TBL.CLASSCD, ");
                stb.append("     TBL.SCHOOL_KIND, ");
                stb.append("     TBL.CURRICULUM_CD, ");
            }
            stb.append("         TBL.SUBCLASSCD ");
            stb.append("     ) TT1 ");
            stb.append(" GROUP BY TT1.SCHREGNO, ");
            //教育課程対応
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" TT1.CLASSCD, ");
                stb.append(" TT1.SCHOOL_KIND, ");
                stb.append(" TT1.CURRICULUM_CD, ");
            }
            stb.append("     TT1.SUBCLASSCD ");
            return stb.toString();
        }

        private static String implode(final List month, final String glue) {
            if (null == month) {
                return null;
            }
            String c = "";
            final StringBuffer stb = new StringBuffer();
            for (final Iterator it = month.iterator(); it.hasNext();) {
                final String m = (String) it.next();
                stb.append(c).append(m);
                c = glue;
            }
            return stb.toString();
        }

        private static String getOne(final DB2UDB db2, final String query) throws SQLException {
            final PreparedStatement ps = db2.prepareStatement(query);
            final ResultSet rs = ps.executeQuery();
            try {
                if (rs.next()) {
                    return rs.getString(1);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return null;
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
        final String _grade;
        final String _gakki2;
        final String _date;
        final String _seisekiHusin1;
        final String _seisekiHusinHyouteiFrom;
        final String _seisekiHusinHyouteiTo;
        final String _kyoukaSougou1;
        final String _kyoukaSougou2;
        final String _seisekiHusin2;
        final String _seisekiHusin3;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;
        final String _chikokuHyoujiFlg;
        final String _subclasscd;
        final String _printDiv; //1:訂正前(画面＋BEF) 2:訂正後(AFT) 3:未訂正(画面)
        /** 氏名欄に学籍番号の表示/非表示 1:表示 それ以外:非表示 */
        final String _use_SchregNo_hyoji;

        final KNJSchoolMst _knjSchoolMst;
        final String _gradeName;
        final String _semesterName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _grade = request.getParameter("GRADE");
            _gakki2 = request.getParameter("GAKKI2");
            _date = null != request.getParameter("DATE") ? StringUtils.replace(request.getParameter("DATE"), "/", "-"): request.getParameter("DATE");
            _seisekiHusin1 = request.getParameter("SEISEKI_HUSIN1");
            _seisekiHusinHyouteiFrom = request.getParameter("SEISEKI_HUSIN_HYOUTEI_FROM");
            _seisekiHusinHyouteiTo = request.getParameter("SEISEKI_HUSIN_HYOUTEI_TO");
            _kyoukaSougou1 = request.getParameter("KYOUKA_SOUGOU1");
            _kyoukaSougou2 = request.getParameter("KYOUKA_SOUGOU2");
            _seisekiHusin2 = request.getParameter("SEISEKI_HUSIN2");
            _seisekiHusin3 = request.getParameter("SEISEKI_HUSIN3");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _chikokuHyoujiFlg = request.getParameter("chikokuHyoujiFlg");
            _subclasscd = request.getParameter("SUBCLASSCD");
            _printDiv = request.getParameter("PRINT_DIV");
            _use_SchregNo_hyoji = request.getParameter("use_SchregNo_hyoji");

            _knjSchoolMst = new KNJSchoolMst(db2, _ctrlYear);
            _gradeName = "99".equals(_grade) ? "" : Knjd231vQuery.getOne(db2, Knjd231vQuery.getSelectGrade(this));
            _semesterName = Knjd231vQuery.getOne(db2, Knjd231vQuery.getSelectSeme(this));
        }
    }
}

// eof

