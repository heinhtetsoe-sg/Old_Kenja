// kanji=漢字
/*
 * $Id: bd4477f328a5bbc1be455e22aafa81315e17cfb7 $
 *
 * 作成日: 2010/05/07 18:19:09 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2007-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJObjectAbs;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD655B {

    private static final String FORM_NAME = "KNJD655B.frm";
    private static final String FORM2_NAME = "KNJD655B_2.frm";

    private static final Log log = LogFactory.getLog(KNJD655B.class);

    private KNJObjectAbs knjobj;        //編集用クラス

    Param _param;
    /**
     * KNJD.classから呼ばれる処理
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
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

            _param = createParam(request);

            _param.load(db2);

            knjobj = new KNJEditString();

            boolean hasData = false;

            final String frmName;
            if (_param.isCollege()) {
            	frmName = FORM2_NAME;
            } else {
            	frmName = FORM_NAME;
            }
            svf.VrSetForm(frmName, 1);
            log.debug("印刷するフォーム:" + frmName);

            for (int i = 0; i < _param._groupList.length; i++) {
                if (printMain(db2, svf, _param._groupList[i])) hasData = true;
            }
            if (!hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } finally {
            close(db2, svf);
        }
    }

    /** 印刷処理メイン */
    private boolean printMain(
            final DB2UDB db2,
            final Vrw32alp svf,
            final String hrclass
    ) throws Exception {
        boolean rtnflg = false;

        //ヘッダ部
        printHeader(svf);
        printHrName(db2, svf, hrclass);
        //1.クラス毎の生徒数
        printStudentCnt(db2, svf, hrclass);
        //2.成績不完備生徒
        printStudentFukanbi(db2, svf, hrclass);
        //3.平均点
        printAvg(db2, svf, hrclass);
        //4.成績上位者
        printRank(db2, svf, hrclass);
        //5.科目別欠点人数
        printSubclassFail(db2, svf, hrclass);
        //6.多くの欠点を有する生徒
        printStudentFail(db2, svf, hrclass);
        //7.指導全般にわたって注意を必要とする生徒
        printStudentSidou(db2, svf, hrclass);
        //8.備考
        printRecordDocument(db2, svf, hrclass);

        svf.VrEndPage();
        rtnflg = true;

        return rtnflg;
    }

    private void printHeader(final Vrw32alp svf) throws SQLException {
        svf.VrsOut("NENDO"      , _param._gengou);
        svf.VrsOut("SEMESTER"   , _param._semesterName);
        //学年末の場合は、テスト名は出力しない。
        if (!_param.isGakunenmatu()) {
            svf.VrsOut("TESTNAME"   , _param._testName);
        }
        svf.VrsOut("ymd1"       , _param._loginDate);

        final String[][] jobNames;
        if (_param.isCollege()) {
            jobNames = new String[][] {{"理事長", ""}, {"教学顧問", ""}, {"中高", "校長"}, {"高校", "教頭"}, {"中学", "教頭"}, {"教務", "主任"}, {"学年", "主任"}};
        } else {
            jobNames = new String[][] {{"中学", "校長"}, {"高校", "校長"}, {"中学", "教頭"}, {"高校", "教頭"}, {"教務", "主任"}, {"コース・", "学年主任"}};
        }
        for (int i = 0; i < jobNames.length; i++) {
            for (int j = 0; j < jobNames[i].length; j++) {
            	if ("".equals(jobNames[i][1])) {
        			//真ん中のフォームにだけ出力
            		if (j == 0) {
                        svf.VrsOutn("JOB_NAME", i + 1, jobNames[i][j]);
            		}
            	} else {
                    svf.VrsOutn("JOB_NAME" + String.valueOf(j + 1) , i + 1, jobNames[i][j]);
            	}
            }
        }
    }

    private void printHrName(final DB2UDB db2, final Vrw32alp svf, final String hrclass) throws SQLException {
        final String sql = getHrNameSql(hrclass);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String hrName = rs.getString("HR_NAME");
                String staffName = rs.getString("STAFFNAME1");
                String staffName2 = rs.getString("STAFFNAME2");
                String staffName3 = rs.getString("STAFFNAME3");
                if (null != staffName2) {
                    staffName = staffName + ", " + staffName2;
                }
                if (null != staffName3) {
                    staffName = staffName + ", " + staffName3;
                }
                svf.VrsOut("HR_NAME"  , hrName);
                svf.VrsOut("STAFFNAME", staffName);
            }
        } catch (final Exception ex) {
            log.error("クラス・担任名の取得でエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void printStudentCnt(final DB2UDB db2, final Vrw32alp svf, final String hrclass) throws SQLException {
        final String sql = getStudentCntSql(hrclass);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String sex = rs.getString("SEX");
                String regdCnt = rs.getString("REGD_CNT");
                String examCnt = rs.getString("EXAM_CNT");

                svf.VrsOut("REGD1_" + sex  , regdCnt);
                svf.VrsOut("EXAMINEE1_" + sex  , examCnt);
            }
        } catch (final Exception ex) {
            log.error("クラス生徒数の取得でエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void printStudentFukanbi(final DB2UDB db2, final Vrw32alp svf, final String hrclass) throws SQLException {
        final String sql = getStudentFukanbiSql(hrclass);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            int gyo = 0;
            while (rs.next()) {
                gyo++;
                if (5 < gyo) break;

                svf.VrsOutn("NAME2"     , gyo, rs.getString("NAME"));
                svf.VrsOutn("REASON2"   , gyo, rs.getString("REMARK1"));
            }
        } catch (final Exception ex) {
            log.error("不完備生徒の取得でエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void printAvg(final DB2UDB db2, final Vrw32alp svf, final String hrclass) throws SQLException {
        final String sql;
        if (_param.isKansan()) {
            sql = getAvgKansanSql(hrclass);
        } else {
            sql = getAvgSql(hrclass);
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            int cnt = 0;
            boolean printGrade = false;
            boolean printHrclass = false;
            while (rs.next()) {
                if (_param.isKansan()) {
                    final String div = rs.getString("DIV");
                    final String avg = null == rs.getString("AVG") ? null : new BigDecimal(rs.getString("AVG")).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    if (!printGrade && "GRADE_AVG".equals(div)) {
                        svf.VrsOut("GRADE_AVE3"        ,  avg);
                    }
                    if (!printHrclass && "HR_AVG".equals(div)) {
                        svf.VrsOut("CLASS_AVE3"        ,  avg);
                    }
                    if ("COURSE_AVG".equals(div)) {
                        cnt++;
                        if (2 < cnt) continue;

                        String no = String.valueOf(cnt);
                        svf.VrsOut("COURSE"       + no ,  rs.getString("NAME"));
                        svf.VrsOut("COURSE_AVE3_" + no ,  avg);
                    }
                } else {
                    cnt++;
                    if (2 < cnt) break;

                    String no = String.valueOf(cnt);
                    svf.VrsOut("COURSE"       + no ,  rs.getString("COURSECODENAME"));
                    svf.VrsOut("COURSE_AVE3_" + no ,  rs.getString("COURSE_AVG"));
                    svf.VrsOut("GRADE_AVE3"        ,  rs.getString("GRADE_AVG"));
                    svf.VrsOut("CLASS_AVE3"        ,  rs.getString("CLASS_AVG"));
                }
            }
        } catch (final Exception ex) {
            log.error("平均点の取得でエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void printRank(final DB2UDB db2, final Vrw32alp svf, final String hrclass) throws SQLException {
        final String sql = getRankSql(hrclass);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            svf.VrsOut("GRADE_RANK_NAME4"   ,  _param.getRankDivName());
            svf.VrsOut("AVERAGE_NAME4"      ,  _param.getRankKijunName());
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            int cnt = 0;
            while (rs.next()) {
                cnt++;
                if (8 < cnt) break;

                int gyo = (4 < cnt) ? (cnt - 4) : cnt;
                String no = (4 < cnt) ? "2" : "1";
                svf.VrsOutn("GRADE_RANK4_" + no ,  gyo,  rs.getString("GRADE_RANK"));
                svf.VrsOutn("CLASS_RANK4_" + no ,  gyo,  rs.getString("CLASS_RANK"));
                svf.VrsOutn("NAME4_"       + no ,  gyo,  rs.getString("NAME"));
                svf.VrsOutn("AVERAGE4_"    + no ,  gyo,  rs.getString("AVERAGE"));
            }
        } catch (final Exception ex) {
            log.error("成績上位者の取得でエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void printSubclassFail(final DB2UDB db2, final Vrw32alp svf, final String hrclass) throws SQLException {
        final String sql = getSubclassFailSql(hrclass);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            svf.VrsOut("FAIL_SCORE5"   ,  String.valueOf(_param._badScore));
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            int cnt = 0;
            while (rs.next()) {
                cnt++;
                if (15 < cnt) break;

                int gyo = (10 < cnt) ? (cnt - 10) : (5 < cnt) ? (cnt - 5) : cnt;
                String no = (10 < cnt) ? "3" : (5 < cnt) ? "2" : "1";
                svf.VrsOutn("SUBCLASS5_" + no ,  gyo,  rs.getString("SUBCLASSNAME"));
                svf.VrsOutn("FAIL5_"     + no ,  gyo,  rs.getString("CNT"));
            }
        } catch (final Exception ex) {
            log.error("科目別欠点人数の取得でエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void printStudentFail(final DB2UDB db2, final Vrw32alp svf, final String hrclass) throws SQLException {
        final String sql = getStudentFailSql(hrclass);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            svf.VrsOut("FAIL_SCORE6"    ,  String.valueOf(_param._badScore));
            svf.VrsOut("FAIL_SUBCLASS6" ,  String.valueOf(_param._badSubCnt));
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            String seq = "";
            String subName = "";
            String schName = "";
            String tmpSchno = "";
            int gyo = 0;
            while (rs.next()) {
                if (!tmpSchno.equals(rs.getString("SCHREGNO")) && !tmpSchno.equals("")) {
                    String no = 35 < subName.length() ? "_1" : "";
                    svf.VrsOutn("NAME6"         ,  gyo + 1,  schName);
                    svf.VrsOutn("SUBCLASS6" + no,  gyo + 1,  subName);

                    gyo++;
                    seq = "";
                    subName = "";
                }
                schName = rs.getString("NAME");
                tmpSchno = rs.getString("SCHREGNO");
                subName = subName + seq + rs.getString("SUBCLASSNAME");
                seq = "、";
            }
            if (!tmpSchno.equals("")) {
                String no = 35 < subName.length() ? "_1" : "";
                svf.VrsOutn("NAME6"         ,  gyo + 1,  schName);
                svf.VrsOutn("SUBCLASS6" + no,  gyo + 1,  subName);
            }
        } catch (final Exception ex) {
            log.error("多くの欠点を有する生徒の取得でエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void printStudentSidou(final DB2UDB db2, final Vrw32alp svf, final String hrclass) throws SQLException {
        final String sql = getStudentSidouSql(hrclass);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            svf.VrsOut("ATTEND_TERM7" ,  _param.getAttendTerm());
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            int gyo = 0;
            while (rs.next()) {
                gyo++;
                if (5 < gyo) break;

                svf.VrsOutn("NAME7"    ,  gyo,  rs.getString("NAME"));
                svf.VrsOutn("REASON7_1",  gyo,  rs.getString("REMARK1"));

                printAttendSemes(db2, svf, gyo, rs.getString("GRADE"), rs.getString("HR_CLASS"), rs.getString("SCHREGNO"));
            }
        } catch (final Exception ex) {
            log.error("指導全般にわたって注意を必要とする生徒の取得でエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void printAttendSemes(final DB2UDB db2, final Vrw32alp svf, final int gyo, final String grade, final String hrClass, final String schregno) throws SQLException {
        final String sql = AttendAccumulate.getAttendSemesSql(
                                                _param._semesFlg,
                                                _param._defineSchoolCode,
                                                _param._knjSchoolMst,
                                                _param._year,
                                                "1",
                                                _param._semester,
                                                (String) _param._hasuuMap.get("attendSemesInState"),
                                                _param._periodInState,
                                                (String) _param._hasuuMap.get("befDayFrom"),
                                                (String) _param._hasuuMap.get("befDayTo"),
                                                (String) _param._hasuuMap.get("aftDayFrom"),
                                                (String) _param._hasuuMap.get("aftDayTo"),
                                                grade,
                                                hrClass,
                                                schregno,
                                                "SCHREGNO",
                                                _param._useCurriculumcd,
                                                _param._useVirus,
                                                _param._useKoudome);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                svf.VrsOutn("ABSENCE7" ,  gyo,  rs.getString("SICK"));
                svf.VrsOutn("LATE7"    ,  gyo,  rs.getString("LATE"));
                svf.VrsOutn("EARLY7"   ,  gyo,  rs.getString("EARLY"));
            }
        } catch (final Exception ex) {
            log.error("出席状況の取得でエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void printRecordDocument(final DB2UDB db2, final Vrw32alp svf, final String hrclass) throws SQLException {
        final String sql = getRecordDocumentSql(hrclass);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String[] remark = KNJ_EditEdit.get_token(rs.getString("FOOTNOTE"), 90, 7);
                if (null != remark) {
                    for (int i = 0; i < remark.length; i++) {
                        svf.VrsOutn("REMARK8",  i+1,  remark[i]);
                    }
                }
            }
        } catch (final Exception ex) {
            log.error("備考の取得でエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String getHrNameSql(final String hrclass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T2.HR_NAME, ");
        stb.append("     L1.STAFFNAME AS STAFFNAME1, ");
        stb.append("     L2.STAFFNAME AS STAFFNAME2, ");
        stb.append("     L3.STAFFNAME AS STAFFNAME3 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT T2 ");
        stb.append("     LEFT JOIN STAFF_MST L1 ON L1.STAFFCD = T2.TR_CD1 ");
        stb.append("     LEFT JOIN STAFF_MST L2 ON L2.STAFFCD = T2.TR_CD2 ");
        stb.append("     LEFT JOIN STAFF_MST L3 ON L3.STAFFCD = T2.TR_CD3 ");
        stb.append(" WHERE ");
        stb.append("     T2.YEAR = '" + _param._year + "' AND ");
        stb.append("     T2.SEMESTER = '" + _param.getSchregSemester() + "' AND ");
        stb.append("     T2.GRADE || T2.HR_CLASS = '" + hrclass + "' ");
        return stb.toString();
    }

    private String getStudentCntSql(final String hrclass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHNO_CNT AS ( ");
        stb.append("     SELECT ");
        stb.append("         value(W2.SEX,'3') as SEX, ");
        stb.append("         count(W1.SCHREGNO) as REGD_CNT, ");
        stb.append("         count(L1.CLASS_RANK) as EXAM_CNT ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT W1 ");
        stb.append("         INNER JOIN SCHREG_BASE_MST W2 ON W2.SCHREGNO = W1.SCHREGNO ");
        stb.append("         LEFT JOIN RECORD_RANK_DAT L1 ");
        stb.append("               ON  L1.YEAR = '" + _param._year + "' ");
        stb.append("               AND L1.SEMESTER = '" + _param._semester + "' ");
        stb.append("               AND L1.TESTKINDCD || L1.TESTITEMCD = '" + _param._testcd + "' ");
        stb.append("               AND L1.SUBCLASSCD = '999999' ");
        stb.append("               AND L1.SCHREGNO = W1.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("         W1.YEAR = '" + _param._year + "' AND ");
        stb.append("         W1.SEMESTER = '" + _param.getSchregSemester() + "' AND ");
        stb.append("         W1.GRADE || W1.HR_CLASS = '" + hrclass + "' ");
        stb.append("     GROUP BY ");
        stb.append("         GROUPING SETS((W1.HR_CLASS,W2.SEX),W1.HR_CLASS) ");
        stb.append("     ) ");

        stb.append(" SELECT ");
        stb.append("     T1.SEX, ");
        stb.append("     T1.REGD_CNT, ");
        stb.append("     T1.EXAM_CNT ");
        stb.append(" FROM ");
        stb.append("     SCHNO_CNT T1 ");
        stb.append(" ORDER BY ");
        stb.append("     T1.SEX ");
        return stb.toString();
    }

    private String getStudentFukanbiSql(final String hrclass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_SCHNO AS ( ");
        stb.append("     SELECT ");
        stb.append("         W1.SCHREGNO, ");
        stb.append("         W1.ATTENDNO, ");
        stb.append("         W2.NAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT W1 ");
        stb.append("         INNER JOIN SCHREG_BASE_MST W2 ON W2.SCHREGNO = W1.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("         W1.YEAR = '" + _param._year + "' AND ");
        stb.append("         W1.SEMESTER = '" + _param.getSchregSemester() + "' AND ");
        stb.append("         W1.GRADE || W1.HR_CLASS = '" + hrclass + "' ");
        stb.append("     ) ");
        stb.append(" , T_SCORE AS ( ");
        stb.append("     SELECT ");
        stb.append("         T2.SCHREGNO, ");
        stb.append("         COUNT(T2.SUBCLASSCD) AS SUB_CNT ");
        stb.append("     FROM ");
        stb.append("         T_SCHNO T1, ");
        stb.append("         RECORD_SCORE_DAT T2 ");
        stb.append("     WHERE ");
        stb.append("           T2.YEAR = '" + _param._year + "' ");
        stb.append("       AND T2.SEMESTER = '" + _param._semester + "' ");
        stb.append("       AND T2.TESTKINDCD || T2.TESTITEMCD = '" + _param._testcd + "' ");
        stb.append("       AND T2.SCHREGNO = T1.SCHREGNO ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("       AND T2.CLASSCD <> '90' ");
        } else {
            stb.append("       AND SUBSTR(T2.SUBCLASSCD, 1, 2) <> '90' ");
        }
        if ("9900".equals(_param._testcd)) {
            stb.append("   AND T2.SCORE_DIV = '00' ");
            stb.append("   AND T2.VALUE IS NULL ");
        } else {
            stb.append("   AND T2.SCORE_DIV = '01' ");
            stb.append("   AND T2.SCORE IS NULL ");
        }
        stb.append("     GROUP BY ");
        stb.append("         T2.SCHREGNO ");
        stb.append("     ) ");

        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T2.SUB_CNT, ");
        stb.append("     T4.REMARK1 ");
        stb.append(" FROM ");
        stb.append("     T_SCHNO T1 ");
        stb.append("     INNER JOIN T_SCORE T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("      LEFT JOIN HEXAM_RECORD_REMARK_DAT T4 ");
        stb.append("             ON T4.YEAR = '" + _param._year + "' ");
        stb.append("            AND T4.SEMESTER = '" + _param._semester + "' ");
        stb.append("            AND T4.TESTKINDCD || T4.TESTITEMCD = '" + _param._testcd + "' ");
        stb.append("            AND T4.REMARK_DIV='5' ");
        stb.append("            AND T4.SCHREGNO = T1.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("     T2.SUB_CNT DESC, ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    private String getAvgSql(final String hrclass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_SCHREG AS ( ");
        stb.append("     SELECT ");
        stb.append("         W1.GRADE, ");
        stb.append("         W1.HR_CLASS, ");
        stb.append("         W1.COURSECD, ");
        stb.append("         W1.MAJORCD, ");
        stb.append("         W1.COURSECODE ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT W1 ");
        stb.append("         INNER JOIN SCHREG_BASE_MST W2 ON W2.SCHREGNO = W1.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("         W1.YEAR = '" + _param._year + "' AND ");
        stb.append("         W1.SEMESTER = '" + _param.getSchregSemester() + "' AND ");
        stb.append("         W1.GRADE || W1.HR_CLASS = '" + hrclass + "' ");
        stb.append("     GROUP BY ");
        stb.append("         W1.GRADE, ");
        stb.append("         W1.HR_CLASS, ");
        stb.append("         W1.COURSECD, ");
        stb.append("         W1.MAJORCD, ");
        stb.append("         W1.COURSECODE ");
        stb.append("     ) ");

        stb.append(" SELECT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.COURSECODE, ");
        stb.append("     M3.COURSECODENAME, ");
        if (_param.isKansan()) {
            stb.append("     decimal(round((float(sum(L1.SCORE))/sum(L1.COUNT))*10,0)/10,5,1) AS GRADE_AVG, ");
            stb.append("     decimal(round((float(sum(L2.SCORE))/sum(L2.COUNT))*10,0)/10,5,1) AS CLASS_AVG, ");
            stb.append("     decimal(round((float(sum(L3.SCORE))/sum(L3.COUNT))*10,0)/10,5,1) AS COURSE_AVG ");
        } else {
            stb.append("     decimal(round(float(L1.AVG)*10,0)/10,5,1) AS GRADE_AVG, ");
            stb.append("     decimal(round(float(L2.AVG)*10,0)/10,5,1) AS CLASS_AVG, ");
            stb.append("     decimal(round(float(L3.AVG)*10,0)/10,5,1) AS COURSE_AVG ");
        }
        stb.append(" FROM ");
        stb.append("     T_SCHREG T1 ");
        stb.append("     LEFT JOIN RECORD_AVERAGE_DAT L1 ");
        stb.append("             ON L1.YEAR = '" + _param._year + "' ");
        stb.append("            AND L1.SEMESTER = '" + _param._semester + "' ");
        stb.append("            AND L1.TESTKINDCD || L1.TESTITEMCD = '" + _param._testcd + "' ");
        if (_param.isKansan()) {
            stb.append("            AND L1.SUBCLASSCD not in ('333333','555555','999999') ");
            stb.append("            AND L1.SUBCLASSCD not like '%0000' ");
        } else {
            stb.append("            AND L1.SUBCLASSCD = '999999' ");
        }
        stb.append("            AND L1.GRADE = T1.GRADE ");
        stb.append("            AND L1.AVG_DIV = '1' ");
        stb.append("            AND L1.HR_CLASS = '000' ");
        stb.append("            AND L1.COURSECD = '0' ");
        stb.append("            AND L1.MAJORCD = '000' ");
        stb.append("            AND L1.COURSECODE = '0000' ");
        stb.append("     LEFT JOIN RECORD_AVERAGE_DAT L2 ");
        stb.append("             ON L2.YEAR = '" + _param._year + "' ");
        stb.append("            AND L2.SEMESTER = '" + _param._semester + "' ");
        stb.append("            AND L2.TESTKINDCD || L2.TESTITEMCD = '" + _param._testcd + "' ");
        if (_param.isKansan()) {
            stb.append("            AND L2.SUBCLASSCD not in ('333333','555555','999999') ");
            stb.append("            AND L2.SUBCLASSCD not like '%0000' ");
        } else {
            stb.append("            AND L2.SUBCLASSCD = '999999' ");
        }
        stb.append("            AND L2.GRADE = T1.GRADE ");
        stb.append("            AND L2.AVG_DIV = '2' ");
        stb.append("            AND L2.HR_CLASS = T1.HR_CLASS ");
        stb.append("            AND L2.COURSECD = '0' ");
        stb.append("            AND L2.MAJORCD = '000' ");
        stb.append("            AND L2.COURSECODE = '0000' ");
        stb.append("     LEFT JOIN RECORD_AVERAGE_DAT L3 ");
        stb.append("             ON L3.YEAR = '" + _param._year + "' ");
        stb.append("            AND L3.SEMESTER = '" + _param._semester + "' ");
        stb.append("            AND L3.TESTKINDCD || L3.TESTITEMCD = '" + _param._testcd + "' ");
        if (_param.isKansan()) {
            stb.append("            AND L3.SUBCLASSCD not in ('333333','555555','999999') ");
            stb.append("            AND L3.SUBCLASSCD not like '%0000' ");
        } else {
            stb.append("            AND L3.SUBCLASSCD = '999999' ");
        }
        stb.append("            AND L3.GRADE = T1.GRADE ");
        stb.append("            AND L3.AVG_DIV = '3' ");
        stb.append("            AND L3.HR_CLASS = '000' ");
        stb.append("            AND L3.COURSECD = T1.COURSECD ");
        stb.append("            AND L3.MAJORCD = T1.MAJORCD ");
        stb.append("            AND L3.COURSECODE = T1.COURSECODE ");
        stb.append("     LEFT JOIN COURSECODE_MST M3 ON M3.COURSECODE = T1.COURSECODE ");
        if (_param.isKansan()) {
            stb.append(" GROUP BY ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T1.COURSECODE, ");
            stb.append("     M3.COURSECODENAME ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.COURSECODE ");
        return stb.toString();
    }

    public String getAvgKansanSql(final String hrclass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_SCHREG AS (  ");
        stb.append("      SELECT  ");
        stb.append("          W1.SCHREGNO, ");
        stb.append("          W1.ATTENDNO, ");
        stb.append("          W1.GRADE, ");
        stb.append("          W1.HR_CLASS, ");
        stb.append("          W1.COURSECD, ");
        stb.append("          W1.MAJORCD, ");
        stb.append("          W1.COURSECODE ");
        stb.append("      FROM  ");
        stb.append("          SCHREG_REGD_DAT W1  ");
        stb.append("          INNER JOIN SCHREG_BASE_MST W2 ON W2.SCHREGNO = W1.SCHREGNO  ");
        stb.append("      WHERE  ");
        stb.append("          W1.YEAR = '" + _param._year + "' AND  ");
        stb.append("          W1.SEMESTER = '" + _param.getSchregSemester() + "' AND  ");
        stb.append("          W1.GRADE = '" + _param._grade + "' ");
        stb.append(" ) , SCHREGNO_SUM AS ( ");
        stb.append("      SELECT  ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.COURSECD, ");
        stb.append("         T1.MAJORCD, ");
        stb.append("         T1.COURSECODE, ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T2.SCORE, ");
        stb.append("         T2.AVG ");
        stb.append("      FROM  ");
        stb.append("          T_SCHREG T1 ");
        stb.append("          INNER JOIN RECORD_RANK_DAT T2  ");
        stb.append("                  ON T2.YEAR = '" + _param._year + "'  ");
        stb.append("                 AND T2.SEMESTER = '" + _param._semester + "'  ");
        stb.append("                 AND T2.TESTKINDCD || T2.TESTITEMCD = '" + _param._testcd + "'  ");
        stb.append("                 AND T2.SUBCLASSCD = '999999' ");
        stb.append("                 AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("         T2.GRADE_RANK IS NOT NULL ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("      'GRADE_AVG' AS DIV, ");
        stb.append("      T1.GRADE, ");
        stb.append("      '000' AS HR_CLASS, ");
        stb.append("      '0' AS COURSECD, ");
        stb.append("      '000' AS MAJORCD, ");
        stb.append("      '0000' AS COURSECODE, ");
        stb.append("      CAST(NULL AS VARCHAR(1)) AS NAME, ");
        stb.append("      AVG(T1.AVG) AS AVG ");
        stb.append(" FROM SCHREGNO_SUM T1 ");
        stb.append(" GROUP BY ");
        stb.append("     T1.GRADE ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("      'HR_AVG' AS DIV, ");
        stb.append("      T1.GRADE, ");
        stb.append("      T1.HR_CLASS, ");
        stb.append("      '0' AS COURSECD, ");
        stb.append("      '000' AS MAJORCD, ");
        stb.append("      '0000' AS COURSECODE, ");
        stb.append("      CAST(NULL AS VARCHAR(1)) AS NAME, ");
        stb.append("      AVG(T1.AVG) AS AVG ");
        stb.append(" FROM SCHREGNO_SUM T1 ");
        stb.append(" WHERE T1.GRADE || T1.HR_CLASS = '" + hrclass + "' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("      'COURSE_AVG' AS DIV, ");
        stb.append("      T1.GRADE, ");
        stb.append("      '000' AS HR_CLASS, ");
        stb.append("      T1.COURSECD, ");
        stb.append("      T1.MAJORCD, ");
        stb.append("      T1.COURSECODE, ");
        stb.append("      L1.COURSECODENAME AS NAME, ");
        stb.append("      AVG(T1.AVG) AS AVG ");
        stb.append(" FROM SCHREGNO_SUM T1 ");
        stb.append(" LEFT JOIN COURSECODE_MST L1 ON L1.COURSECODE= T1.COURSECODE ");
        stb.append(" WHERE EXISTS ( ");
        stb.append("        SELECT 'X' ");
        stb.append("        FROM T_SCHREG L1 ");
        stb.append("        WHERE L1.GRADE || L1.HR_CLASS = '" + hrclass + "' ");
        stb.append("          AND L1.COURSECD = T1.COURSECD ");
        stb.append("          AND L1.MAJORCD = T1.MAJORCD ");
        stb.append("          AND L1.COURSECODE = T1.COURSECODE ");
        stb.append(" ) ");
        stb.append(" GROUP BY ");
        stb.append("      T1.GRADE, ");
        stb.append("      T1.COURSECD, ");
        stb.append("      T1.MAJORCD, ");
        stb.append("      T1.COURSECODE, ");
        stb.append("      L1.COURSECODENAME ");
        stb.append(" ORDER BY ");
        stb.append("      GRADE, ");
        stb.append("      HR_CLASS, ");
        stb.append("      COURSECD, ");
        stb.append("      MAJORCD, ");
        stb.append("      COURSECODE ");
        return stb.toString();
    }

    private String getRankSql(final String hrclass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_SCHREG AS ( ");
        stb.append("     SELECT ");
        stb.append("         W1.SCHREGNO, ");
        stb.append("         W1.ATTENDNO, ");
        stb.append("         W2.NAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT W1 ");
        stb.append("         INNER JOIN SCHREG_BASE_MST W2 ON W2.SCHREGNO = W1.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("         W1.YEAR = '" + _param._year + "' AND ");
        stb.append("         W1.SEMESTER = '" + _param.getSchregSemester() + "' AND ");
        stb.append("         W1.GRADE || W1.HR_CLASS = '" + hrclass + "' ");
        stb.append("     ) ");

        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     L1." + _param.getClassRank() + " AS CLASS_RANK, ");
        stb.append("     L1." + _param.getGradeCourseRank() + " AS GRADE_RANK, ");
        if ("1".equals(_param._rankKijun)) {
            stb.append(" L1.SCORE AS AVERAGE ");
        } else {
            stb.append(" decimal(round(float(L1.AVG)*10,0)/10,5,1) AS AVERAGE ");
        }
        stb.append(" FROM ");
        stb.append("     T_SCHREG T1 ");
        stb.append("     INNER JOIN RECORD_RANK_DAT L1 ");
        stb.append("             ON L1.YEAR = '" + _param._year + "' ");
        stb.append("            AND L1.SEMESTER = '" + _param._semester + "' ");
        stb.append("            AND L1.TESTKINDCD || L1.TESTITEMCD = '" + _param._testcd + "' ");
        stb.append("            AND L1.SUBCLASSCD = '999999' ");
        stb.append("            AND L1.SCHREGNO = T1.SCHREGNO ");
        stb.append("            AND L1." + _param.getClassRank() + " IS NOT NULL ");
        stb.append("            AND L1." + _param.getClassRank() + " <= " + _param._rankNo + " ");
        stb.append(" ORDER BY ");
        stb.append("     L1." + _param.getClassRank() + ", ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    private String getSubclassFailSql(final String hrclass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_SCHREG AS ( ");
        stb.append("     SELECT ");
        stb.append("         W1.SCHREGNO, ");
        stb.append("         W1.GRADE, ");
        stb.append("         W1.ATTENDNO, ");
        stb.append("         W2.NAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT W1 ");
        stb.append("         INNER JOIN SCHREG_BASE_MST W2 ON W2.SCHREGNO = W1.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("         W1.YEAR = '" + _param._year + "' AND ");
        stb.append("         W1.SEMESTER = '" + _param.getSchregSemester() + "' AND ");
        stb.append("         W1.GRADE || W1.HR_CLASS = '" + hrclass + "' ");
        stb.append("     ) ");

        stb.append(" SELECT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || ");
        }
        stb.append("     L1.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     L2.SUBCLASSNAME, ");
        stb.append("     COUNT(T1.SCHREGNO) AS CNT ");
        stb.append(" FROM ");
        stb.append("     T_SCHREG T1 ");
        stb.append("     INNER JOIN RECORD_RANK_DAT L1 ");
        stb.append("             ON L1.YEAR = '" + _param._year + "' ");
        stb.append("            AND L1.SEMESTER = '" + _param._semester + "' ");
        stb.append("            AND L1.TESTKINDCD || L1.TESTITEMCD = '" + _param._testcd + "' ");
        stb.append("            AND L1.SCHREGNO = T1.SCHREGNO ");
        stb.append("            AND L1.SCORE < " + _param._badScore + " ");
        stb.append("     INNER JOIN SUBCLASS_MST L2 ON ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || ");
        }
        stb.append("         L2.SUBCLASSCD=");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || ");
        }
        stb.append("         L1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     NOT EXISTS( ");
        stb.append("         SELECT 'X' ");
        stb.append("         FROM   SUBCLASS_WEIGHTING_GRADES_DAT W1 ");
        stb.append("         WHERE  W1.YEAR = '" + _param._year + "' ");
        stb.append("           AND  W1.GRADE = T1.GRADE ");
        stb.append("           AND  ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         W1.COMBINED_CLASSCD || '-' || W1.COMBINED_SCHOOL_KIND || '-' || W1.COMBINED_CURRICULUM_CD || '-' || ");
        }
        stb.append("                W1.COMBINED_SUBCLASSCD = ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || ");
        }
        stb.append("                L1.SUBCLASSCD ");
        stb.append("         ) ");
        stb.append(" GROUP BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || ");
        }
        stb.append("     L1.SUBCLASSCD, ");
        stb.append("     L2.SUBCLASSNAME ");
        stb.append(" ORDER BY ");
        stb.append("     CNT DESC, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || ");
        }
        stb.append("     L1.SUBCLASSCD ");
        return stb.toString();
    }

    private String getStudentFailSql(final String hrclass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_SCHREG AS ( ");
        stb.append("     SELECT ");
        stb.append("         W1.SCHREGNO, ");
        stb.append("         W1.GRADE, ");
        stb.append("         W1.ATTENDNO, ");
        stb.append("         W2.NAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT W1 ");
        stb.append("         INNER JOIN SCHREG_BASE_MST W2 ON W2.SCHREGNO = W1.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("         W1.YEAR = '" + _param._year + "' AND ");
        stb.append("         W1.SEMESTER = '" + _param.getSchregSemester() + "' AND ");
        stb.append("         W1.GRADE || W1.HR_CLASS = '" + hrclass + "' ");
        stb.append("     ) ");
        stb.append(" , T_CNT AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         COUNT(L1.SUBCLASSCD) AS CNT ");
        stb.append("     FROM ");
        stb.append("         T_SCHREG T1 ");
        stb.append("         INNER JOIN RECORD_RANK_DAT L1 ");
        stb.append("                 ON L1.YEAR = '" + _param._year + "' ");
        stb.append("                AND L1.SEMESTER = '" + _param._semester + "' ");
        stb.append("                AND L1.TESTKINDCD || L1.TESTITEMCD = '" + _param._testcd + "' ");
        stb.append("                AND L1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                AND L1.SCORE < " + _param._badScore + " ");
        stb.append("         INNER JOIN SUBCLASS_MST L2 ON ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || ");
        }
        stb.append("             L2.SUBCLASSCD = ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || ");
        }
        stb.append("             L1.SUBCLASSCD ");
        stb.append("     WHERE ");
        stb.append("         NOT EXISTS( ");
        stb.append("             SELECT 'X' ");
        stb.append("             FROM   SUBCLASS_WEIGHTING_GRADES_DAT W1 ");
        stb.append("             WHERE  W1.YEAR = '" + _param._year + "' ");
        stb.append("               AND  W1.GRADE = T1.GRADE ");
        stb.append("               AND  ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         W1.COMBINED_CLASSCD || '-' || W1.COMBINED_SCHOOL_KIND || '-' || W1.COMBINED_CURRICULUM_CD || '-' || ");
        }
        stb.append("                W1.COMBINED_SUBCLASSCD = ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || ");
        }
        stb.append("                L1.SUBCLASSCD ");
        stb.append("             ) ");
        stb.append("     GROUP BY ");
        stb.append("         T1.SCHREGNO ");
        stb.append("     ) ");

        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T2.CNT, ");
        stb.append("     L1.SUBCLASSCD, ");
        stb.append("     L2.SUBCLASSNAME ");
        stb.append(" FROM ");
        stb.append("     T_SCHREG T1 ");
        stb.append("     INNER JOIN T_CNT T2 ");
        stb.append("             ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("            AND T2.CNT >= " + _param._badSubCnt + " ");
        stb.append("     INNER JOIN RECORD_RANK_DAT L1 ");
        stb.append("             ON L1.YEAR = '" + _param._year + "' ");
        stb.append("            AND L1.SEMESTER = '" + _param._semester + "' ");
        stb.append("            AND L1.TESTKINDCD || L1.TESTITEMCD = '" + _param._testcd + "' ");
        stb.append("            AND L1.SCHREGNO = T1.SCHREGNO ");
        stb.append("            AND L1.SCORE < " + _param._badScore + " ");
        stb.append("     INNER JOIN SUBCLASS_MST L2 ON ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || ");
        }
        stb.append("             L2.SUBCLASSCD = ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || ");
        }
        stb.append("             L1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     NOT EXISTS( ");
        stb.append("         SELECT 'X' ");
        stb.append("         FROM   SUBCLASS_WEIGHTING_GRADES_DAT W1 ");
        stb.append("         WHERE  W1.YEAR = '" + _param._year + "' ");
        stb.append("           AND  W1.GRADE = T1.GRADE ");
        stb.append("           AND  ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         W1.COMBINED_CLASSCD || '-' || W1.COMBINED_SCHOOL_KIND || '-' || W1.COMBINED_CURRICULUM_CD || '-' || ");
        }
        stb.append("                W1.COMBINED_SUBCLASSCD = ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || ");
        }
        stb.append("                L1.SUBCLASSCD ");
        stb.append("         ) ");
        stb.append(" ORDER BY ");
        stb.append("     T2.CNT DESC, ");
        stb.append("     T1.ATTENDNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || ");
        }
        stb.append("     L1.SUBCLASSCD ");
        return stb.toString();
    }

    private String getStudentSidouSql(final String hrclass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_SCHREG AS ( ");
        stb.append("     SELECT ");
        stb.append("         W1.SCHREGNO, ");
        stb.append("         W1.GRADE, ");
        stb.append("         W1.HR_CLASS, ");
        stb.append("         W1.ATTENDNO, ");
        stb.append("         W2.NAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT W1 ");
        stb.append("         INNER JOIN SCHREG_BASE_MST W2 ON W2.SCHREGNO = W1.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("         W1.YEAR = '" + _param._year + "' AND ");
        stb.append("         W1.SEMESTER = '" + _param.getSchregSemester() + "' AND ");
        stb.append("         W1.GRADE || W1.HR_CLASS = '" + hrclass + "' ");
        stb.append("     ) ");

        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T4.REMARK1 ");
        stb.append(" FROM ");
        stb.append("     T_SCHREG T1 ");
        stb.append("     INNER JOIN HEXAM_RECORD_REMARK_DAT T4 ");
        stb.append("             ON T4.YEAR = '" + _param._year + "' ");
        stb.append("            AND T4.SEMESTER = '" + _param._semester + "' ");
        stb.append("            AND T4.TESTKINDCD || T4.TESTITEMCD = '" + _param._testcd + "' ");
        stb.append("            AND T4.REMARK_DIV = '6' ");
        stb.append("            AND T4.SCHREGNO = T1.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    private String getRecordDocumentSql(final String hrclass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     GRADE, ");
        stb.append("     FOOTNOTE ");
        stb.append(" FROM ");
        stb.append("     RECORD_DOCUMENT_KIND_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' AND ");
        stb.append("     SEMESTER = '" + _param._semester + "' AND ");
        stb.append("     TESTKINDCD || TESTITEMCD = '" + _param._testcd + "' AND ");
        stb.append("     GRADE || HR_CLASS = '" + hrclass + "' AND ");
        stb.append("     COURSECD = '0' AND ");
        stb.append("     MAJORCD = '000' AND ");
        stb.append("     COURSECODE = '0000' AND ");
        stb.append("     SUBCLASSCD = '000000' AND ");
        stb.append("     KIND_DIV = '3' AND ");
        stb.append("     FOOTNOTE IS NOT NULL ");
        return stb.toString();
    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void close(final DB2UDB db2, final Vrw32alp svf) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
        if (null != svf) {
            svf.VrQuit();
        }
    }

    private Param createParam(final HttpServletRequest request) {
        log.fatal("$Revision: 75599 $ $Date: 2020-07-22 11:05:02 +0900 (水, 22 7 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String grade = request.getParameter("GRADE");
        final String groupList[] = request.getParameterValues("CLASS_SELECTED");
        final String loginDate = request.getParameter("LOGIN_DATE");
        final String badScore = request.getParameter("BAD_SCORE");
        final String badSubCnt = request.getParameter("BAD_SUBCNT");
        final String rankNo = request.getParameter("RANK_NO");
        final String rankKijun = request.getParameter("RANK_KIJUN");
        final String rankDiv = request.getParameter("RANK_DIV");

        final String testcd = request.getParameter("TESTCD");
        final String sDate = request.getParameter("SDATE");
        final String eDate = request.getParameter("EDATE");
        final String kansan = request.getParameter("KANSAN");
        final String useCurriclumcd = request.getParameter("useCurriculumcd");
        final String useVirus = request.getParameter("useVirus");
        final String useKoudome = request.getParameter("useKoudome");

        final Param param = new Param(
                year,
                semester,
                grade,
                groupList,
                loginDate,

                testcd,
                sDate.replace('/', '-'),
                eDate.replace('/', '-'),
                rankKijun,
                rankDiv,
                kansan,

                Integer.parseInt(badScore),
                Integer.parseInt(badSubCnt),
                Integer.parseInt(rankNo),
                useCurriclumcd,
                useVirus,
                useKoudome
        );
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _gengou;
        private final String _semester;
        private final String _grade;
        private final String _loginDate;
        private final String[] _groupList;
        private final int _badScore;
        private final int _badSubCnt;
        private final int _rankNo;

        private String _semesterName;
        private String _semesterSdate;
        private String _semesterEdate;
        private String _semesterMax;
        private String _schoolKind;

        private final String _testcd;
        private String _testName;
        private final String _rankKijun;
        private final String _rankDiv;
        private final String _kansan;

        private String _z010SchoolCode;
        private final String _sDate; //出欠集計開始日付
        private final String _eDate; //出欠集計終了日付
        private KNJSchoolMst _knjSchoolMst;
        private KNJDefineSchool _defineSchoolCode;
        private Map _attendSemesMap;
        private Map _hasuuMap;
        private boolean _semesFlg;
        private String _periodInState;
        KNJDefineCode _defineCode;
        private String _useCurriculumcd;
        private String _useVirus;
        private String _useKoudome;

        public Param(
                final String year,
                final String semester,
                final String grade,
                final String[] groupList,
                final String loginDate,

                final String testcd,
                final String sDate,
                final String eDate,
                final String rankKijun,
                final String rankDiv,
                final String kansan,

                final int badScore,
                final int badSubCnt,
                final int rankNo,
                final String useCurriculumcd,
                final String useVirus,
                final String useKoudome
        ) {
            _year = year;
            _semester = semester;
            _grade = grade;
            _groupList = groupList;
            _loginDate = KNJ_EditDate.h_format_JP(loginDate);
            final String gengou = nao_package.KenjaProperties.gengou(Integer.parseInt(year));
            _gengou = gengou + "年度";
            _badScore = badScore;
            _badSubCnt = badSubCnt;
            _rankNo = rankNo;

            _testcd = testcd;
            _rankKijun = rankKijun;
            _rankDiv = rankDiv;
            _kansan = kansan;

            _sDate = sDate;
            _eDate = eDate;
            _useCurriculumcd = useCurriculumcd;
            _useVirus = useVirus;
            _useKoudome = useKoudome;
        }

        private boolean isGakunenmatu() {
            return "9".equals(_semester);
        }

        private boolean isKansan() {
            return null != _kansan;
        }

        private String getAttendTerm() {
            return KNJ_EditDate.h_format_JP(_sDate) + " \uFF5E " + KNJ_EditDate.h_format_JP(_eDate);
        }

        private String getRankKijunName() {
            return "1".equals(_rankKijun) ? "総合点" : "平均点";
        }

        private String getRankDivName() {
            return "1".equals(_rankDiv) ? "学年" : "コース";
        }

        private String getClassRank() {
            return "1".equals(_rankKijun) ? "CLASS_RANK" : "CLASS_AVG_RANK";
        }

        private String getGradeRank() {
            return "1".equals(_rankKijun) ? "GRADE_RANK" : "GRADE_AVG_RANK";
        }

        private String getCourseRank() {
            return "1".equals(_rankKijun) ? "COURSE_RANK" : "COURSE_AVG_RANK";
        }

        private String getGradeCourseRank() {
            return "1".equals(_rankDiv) ? getGradeRank() : getCourseRank();
        }

        private void load(final DB2UDB db2) {
            _z010SchoolCode = getSchoolCode(db2);
            createSemesterName(db2);
            createSchregSeme(db2);
            createTestName(db2);
            createAttend(db2);
            setSchoolKind(db2);
        }

        private void createAttend(final DB2UDB db2) {
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
                _defineSchoolCode = new KNJDefineSchool();
                _defineSchoolCode.defineCode(db2, _year);
                _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, null, _year);
                _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap, _sDate, _eDate);
                _semesFlg = ((Boolean) _hasuuMap.get("semesFlg")).booleanValue();
                _periodInState = AttendAccumulate.getPeiodValue(db2, _defineCode, _year, "1", _semester);
            } catch (final Exception ex) {
                log.error("出欠共通の取得でエラー:", ex);
            } finally {
                db2.commit();
            }
        }

        private void createSemesterName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = "SELECT SEMESTER,SEMESTERNAME,SDATE,EDATE FROM SEMESTER_MST " +
                               "WHERE YEAR='" + _year + "' AND SEMESTER='" + _semester + "'";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _semesterName = rs.getString("SEMESTERNAME");
                    _semesterSdate = rs.getString("SDATE");
                    _semesterEdate = rs.getString("EDATE");
                }
            } catch (final Exception ex) {
                log.error("学期名の取得でエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("学期名:" + _semesterName);
        }

        private void createSchregSeme(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = "SELECT max(SEMESTER) as SEMESTER FROM SCHREG_REGD_HDAT " +
                               "WHERE YEAR='" + _year + "' AND GRADE='" + _grade + "'";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _semesterMax = rs.getString("SEMESTER");
                }
            } catch (final Exception ex) {
                log.error("MAX学期の取得でエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("MAX学期:" + _semesterMax);
        }

        private void setSchoolKind(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT " +
                               "WHERE YEAR='" + _year + "' AND GRADE='" + _grade + "'";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _schoolKind = rs.getString("SCHOOL_KIND");
                }
            } catch (final Exception ex) {
                log.error("校種取得でエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String getSchregSemester() {
            return ("9".equals(_semester)) ? _semesterMax : _semester;
        }

        private void createTestName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = getTestNameSql();
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _testName = rs.getString("TESTITEMNAME");
                }
            } catch (final Exception ex) {
                log.error("テスト名の取得でエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("テスト名:" + _testName);
        }

        private String getTestNameSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TESTITEMNAME ");
            stb.append(" FROM ");
            stb.append("     TESTITEM_MST_COUNTFLG_NEW ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _year + "' AND ");
            stb.append("     SEMESTER = '" + _semester + "' AND ");
            stb.append("     TESTKINDCD || TESTITEMCD = '" + _testcd + "' ");
            return stb.toString();
        }


        private String getSchoolCode(DB2UDB db2) {
            String schoolCode = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   CASE WHEN NAME1 = 'CHIBEN' THEN NAME2 ELSE NULL END AS SCHOOLCODE ");
                stb.append(" FROM ");
                stb.append("   NAME_MST T1 ");
                stb.append(" WHERE ");
                stb.append("   T1.NAMECD1 = 'Z010' ");
                stb.append("   AND T1.NAMECD2 = '00' ");

                PreparedStatement ps = db2.prepareStatement(stb.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    schoolCode = rs.getString("SCHOOLCODE");
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("getSchoolCode Exception", e);
            }
            return schoolCode;
        }

        boolean isGojo() {
            return "30290053001".equals(_z010SchoolCode) || isCollege();
        }

        boolean isWakayama() {
            return "30300049001".equals(_z010SchoolCode);
        }

        boolean isCollege() {
            return "30290086001".equals(_z010SchoolCode);
        }
    }
}
