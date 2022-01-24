/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 *
 * 作成日: 2021/02/10
 * 作成者: ishimine
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJB;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJB103D {

    private static final Log log = LogFactory.getLog(KNJB103D.class);

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

    //試験封筒ラベル印刷
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final Map testInfoMap = getTestInfo(db2); //試験情報取得
        if (testInfoMap.isEmpty()) return;

        setStudentList(db2, testInfoMap); //受験者取得

        final String form = "KNJB103D.frm";
        final int maxGyo = 45;
        for (Iterator iterator = testInfoMap.keySet().iterator(); iterator.hasNext();) {
            final String key = (String) iterator.next();
            final TestInfo testInfo = (TestInfo) testInfoMap.get(key);

            //留学者カウント
            int ryugaku = 0; //留学者数
            for (Student stu : testInfo.studentList) {
                if (stu._transfercd != null) {
                    ryugaku++;
                }
            }

            svf.VrSetForm(form , 1);
            printTitle(svf, testInfo, ryugaku);

            int no  = 1; //連番
            int gyo = 1; //印字行
            for (Student student : testInfo.studentList) {
                if (gyo > maxGyo) {
                    svf.VrEndPage();
                    printTitle(svf, testInfo, ryugaku);
                    gyo = 1;
                }

                svf.VrsOutn("NO", gyo, String.valueOf(no));  //連番
                svf.VrsOutn("HR_NAME", gyo, student._hrNameAbbv);  //HR
                svf.VrsOutn("ATTEND_NO", gyo, String.valueOf(Integer.parseInt(student._attendno)));  //出席番号

                final int keta = KNJ_EditEdit.getMS932ByteLength(student._name);
                final String field = keta <= 20 ? "1" : keta <= 30 ? "2" : "3";
                svf.VrsOutn("NAME" + field, gyo, student._name);  //氏名
                no++;
                gyo++;
            }

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printTitle(final Vrw32alp svf, final TestInfo testInfo, final int ryugaku) {
        svf.VrsOut("PRINT_DATE", "出力日：" + _param._date.replace("-", "/"));  //出力日

        if (testInfo._due_Date != null) {
            final String dDate = KNJ_EditDate.h_format_JP_MD(testInfo._due_Date);
            svf.VrsOut("LIMIT",  " " + dDate + " " + testInfo._due_Time + "　時");  //成績締切
        }

        final String date = KNJ_EditDate.h_format_SeirekiJP(testInfo._executedate);
        final String youbi = KNJ_EditDate.h_format_W(testInfo._executedate);

        svf.VrsOut("DATE", date + "(" + youbi + ")");  //日時
        svf.VrsOut("PERIOD", testInfo._period);  //時限

        svf.VrsOut("SUBCLASS_NAME" + getField(testInfo._subclassname), testInfo._subclassname);  //科目名
        svf.VrsOut("TR_NAME1_" + getField(testInfo._stfname1), testInfo._stfname1);  //出題者

        final String stf2Name = testInfo._stfname1.equals(testInfo._stfname2) ? "同上" : testInfo._stfname2;
        svf.VrsOut("TR_NAME2_" + getField(stf2Name), stf2Name);  //返却先

        svf.VrsOut("CHAIR_NAME", testInfo._chairname);  //クラス
        svf.VrsOut("SELECT", testInfo._remark2);  //選択
        svf.VrsOut("KIND", testInfo._remark3);  //種別
        svf.VrsOut("FACILITY_NAME", testInfo._facilityabbv);  //教室
        svf.VrsOut("MANAGER_NAME1_" + getField(testInfo._stfname3), testInfo._stfname3);  //監督者
        svf.VrsOut("Q_PAPERS", testInfo._q_Papers);  //問題用紙
        svf.VrsOut("Q_BOTH_DIV", testInfo._q_Both_Div);  //問題用紙 両面印刷
        svf.VrsOut("A_PAPERS", testInfo._a_Papers);  //解答用紙
        svf.VrsOut("A_BOTH_DIV", testInfo._a_Both_Div);  //解答用紙 両面印刷

        final int zaiseki = testInfo.studentList.size();
        svf.VrsOut("ENROLL_NUM1", String.valueOf(zaiseki));  //在籍者数
        svf.VrsOut("ENROLL_NUM2", String.valueOf(ryugaku));  //留学者数

        final int examNum = zaiseki - ryugaku;
        svf.VrsOut("EXAM_NUM1", String.valueOf(examNum));  //受験者数
        svf.VrsOut("EXAM_NUM2", "2");  //固定
        final int busuu = examNum + 2;
        svf.VrsOut("EXAM_NUM3", String.valueOf(busuu));  //部数(受験者数＋２)

        if (testInfo._remark != null) {
            final String remark[] = KNJ_EditEdit.get_token(testInfo._remark, 40, 3);
            for (int cnt = 0; cnt < 3; cnt++) {
                if (remark[cnt] != null) {
                    svf.VrsOut("REMARK" + (cnt + 1), remark[cnt]); //備考
                }
            }
        }
    }

    private String getField(final String str) {
        final int keta = KNJ_EditEdit.getMS932ByteLength(str);
        return keta <= 30 ? "1" : "2";
    }

    private void setStudentList(final DB2UDB db2, final Map testInfoMap) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        TestInfo testInfo = null;
        final String sql = getStudentSql();
        log.debug(" sql =" + sql);

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String chaircd = rs.getString("CHAIRCD");
                final String schregno = rs.getString("SCHREGNO");
                final String hr_Class = rs.getString("HR_CLASS");
                final String attendno = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                final String transfercd = rs.getString("TRANSFERCD");
                final String hrNameAbbv = rs.getString("HR_NAMEABBV");
                final Student student = new Student(schregno, hr_Class, attendno, name, transfercd, hrNameAbbv);

                final String executedate = rs.getString("EXECUTEDATE");
                final String periodcd = rs.getString("PERIODCD");
                final String key = chaircd + "-" + executedate + "-" + periodcd;

                if (testInfo == null) {
                    testInfo = (TestInfo)testInfoMap.get(key);
                }
                else if (!key.equals(testInfo._key) && testInfoMap.containsKey(key)) {
                    testInfo = (TestInfo)testInfoMap.get(key);
                }
                testInfo.studentList.add(student);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT");
        stb.append("     STD.YEAR, ");
        stb.append("     STD.SEMESTER, ");
        stb.append("     STD.CHAIRCD, ");
        stb.append("     STD.SCHREGNO, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     TRANS.TRANSFER_SDATE, ");
        stb.append("     TRANS.TRANSFER_EDATE, ");
        stb.append("     TRANS.TRANSFERCD, ");
        stb.append("     REGDH.HR_NAMEABBV, ");
        stb.append("     CTEST.EXECUTEDATE, ");
        stb.append("     CTEST.PERIODCD ");
        stb.append(" FROM ");
        stb.append("     CHAIR_STD_DAT STD ");
        stb.append(" INNER JOIN ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("      ON REGD.YEAR = STD.YEAR ");
        stb.append("     AND REGD.SCHREGNO = STD.SCHREGNO ");
        stb.append("     AND REGD.SEMESTER = STD.SEMESTER ");
        stb.append("     AND REGD.GRADE = '" + _param._grade + "' ");
        stb.append(" INNER JOIN ");
        stb.append("     SCHREG_BASE_MST BASE ");
        stb.append("      ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append(" INNER JOIN ");
        stb.append("     SCH_CHR_TEST CTEST");
        stb.append("      ON CTEST.CHAIRCD = STD.CHAIRCD ");
        stb.append("     AND CTEST.YEAR = STD.YEAR  ");
        stb.append("     AND CTEST.SEMESTER = STD.SEMESTER ");
        stb.append("     AND CTEST.TESTKINDCD || CTEST.TESTITEMCD = '" + _param._testCd.substring(0, 4) + "' ");
        stb.append(" LEFT JOIN ");
        stb.append("     SCHREG_TRANSFER_DAT TRANS ");
        stb.append("      ON TRANS.SCHREGNO = STD.SCHREGNO ");
        stb.append("     AND TRANS.TRANSFERCD = '1' "); //留学者
        stb.append("     AND TRANS.TRANSFER_SDATE <= CTEST.EXECUTEDATE ");
        stb.append("     AND ((TRANS.TRANSFER_EDATE >= CTEST.EXECUTEDATE) OR (TRANS.TRANSFER_EDATE IS NULL))");
        stb.append(" LEFT JOIN ");
        stb.append("     SCHREG_REGD_HDAT REGDH ");
        stb.append("      ON REGDH.YEAR = REGD.YEAR ");
        stb.append("     AND REGDH.SEMESTER = REGD.SEMESTER ");
        stb.append("     AND REGDH.GRADE = REGD.GRADE ");
        stb.append("     AND REGDH.HR_CLASS = REGD.HR_CLASS ");
        stb.append(" WHERE ");
        stb.append("     STD.YEAR = '" + _param._year + "' AND ");
        stb.append("     STD.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("     STD.CHAIRCD IN " +  SQLUtils.whereIn(true, _param._categorySelected));
        stb.append(" ORDER BY ");
        stb.append("     STD.CHAIRCD, CTEST.EXECUTEDATE, CTEST.PERIODCD, REGD.HR_CLASS, REGD.ATTENDNO ");

        return stb.toString();
    }

    private class Student {
        final String _schregno;
        final String _hr_Class;
        final String _attendno;
        final String _name;
        final String _transfercd;
        final String _hrNameAbbv;

        public Student (final String schregno, final String hr_Class, final String attendno, final String name, final String transfercd, final String hrNameAbbv) {
            _schregno = schregno;
            _hr_Class = hr_Class;
            _attendno = attendno;
            _name = name;
            _transfercd = transfercd;
            _hrNameAbbv = hrNameAbbv;
        }
    }

    private Map getTestInfo(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final Map retMap = new LinkedMap();
        final String sql = setTestInfoSql();
        log.debug(sql);

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String year = rs.getString("YEAR");
                final String semester = rs.getString("SEMESTER");
                final String chaircd = rs.getString("CHAIRCD");
                final String classcd = rs.getString("CLASSCD");
                final String school_Kind = rs.getString("SCHOOL_KIND");
                final String curriculum_Cd = rs.getString("CURRICULUM_CD");
                final String subclasscd = rs.getString("SUBCLASSCD");
                final String chairname = rs.getString("CHAIRNAME");
                final String executedate = rs.getString("EXECUTEDATE");
                final String period = rs.getString("PERIOD");
                final String subclassname = rs.getString("SUBCLASSNAME");
                final String remark2 = rs.getString("REMARK2");
                final String remark3 = rs.getString("REMARK3");
                final String stfname1 = StringUtils.defaultString(rs.getString("STFNAME1"));
                final String stfname2 = StringUtils.defaultString(rs.getString("STFNAME2"));
                final String stfname3 = StringUtils.defaultString(rs.getString("STFNAME3"));
                final String staffcd = rs.getString("STAFFCD");
                final String due_Date = StringUtils.defaultString(rs.getString("DUE_DATE"));
                final String due_Time = StringUtils.defaultString(rs.getString("DUE_TIME"));
                final String q_Papers = rs.getString("Q_PAPERS");
                final String a_Papers = rs.getString("A_PAPERS");
                final String q_Both_Div = rs.getString("Q_BOTH_DIV");
                final String a_Both_Div = rs.getString("A_BOTH_DIV");
                final String remark = rs.getString("REMARK");
                final String facilityabbv = rs.getString("FACILITYABBV");

                final String periodCd = rs.getString("PERIODCD");
                final String key = chaircd + "-" + executedate + "-" + periodCd;

                if (!retMap.containsKey(key)) {
                    final TestInfo wk = new TestInfo(key, year, semester, chaircd, classcd, school_Kind, curriculum_Cd,
                            subclasscd, chairname, executedate, period, subclassname, remark2, remark3, stfname1,
                            stfname2, stfname3, staffcd, due_Date, due_Time, q_Papers, a_Papers, q_Both_Div, a_Both_Div,
                            remark, facilityabbv);
                    retMap.put(key, wk);
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
        return retMap;
    }

    private String setTestInfoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     CHAIR.YEAR, ");
        stb.append("     CHAIR.SEMESTER, ");
        stb.append("     CHAIR.CHAIRCD, ");
        stb.append("     CHAIR.CLASSCD, ");
        stb.append("     CHAIR.SCHOOL_KIND, ");
        stb.append("     CHAIR.CURRICULUM_CD, ");
        stb.append("     CHAIR.SUBCLASSCD, ");
        stb.append("     CHAIR.CHAIRNAME, ");
        stb.append("     CTEST.EXECUTEDATE, ");
        stb.append("     CTEST.PERIODCD, ");
        stb.append("     NAMEB001.NAME1 AS PERIOD, ");
        stb.append("     SUB.SUBCLASSNAME, ");
        stb.append("     CD004.REMARK2, ");
        stb.append("     CD004.REMARK3, ");
        stb.append("     STF1.STAFFNAME AS STFNAME1, ");
        stb.append("     STF2.STAFFNAME AS STFNAME2, ");
        stb.append("     STF3.STAFFNAME AS STFNAME3, ");
        stb.append("     SSTF.STAFFCD, ");
        stb.append("     LABEL.DUE_DATE, ");
        stb.append("     LABEL.DUE_TIME, ");
        stb.append("     LABEL.Q_PAPERS, ");
        stb.append("     LABEL.A_PAPERS, ");
        stb.append("     CASE WHEN LABEL.Q_BOTH_DIV = '1' THEN 'あり' ELSE 'なし' END AS Q_BOTH_DIV, ");
        stb.append("     CASE WHEN LABEL.A_BOTH_DIV = '1' THEN 'あり' ELSE 'なし' END AS A_BOTH_DIV, ");
        stb.append("     LABEL.REMARK, ");
        stb.append("     FAC.FACILITYABBV ");
        stb.append(" FROM ");
        stb.append("     CHAIR_DAT CHAIR ");
        stb.append(" INNER JOIN ");
        stb.append("     SCH_CHR_TEST CTEST ");
        stb.append("      ON CTEST.CHAIRCD = CHAIR.CHAIRCD ");
        stb.append("     AND CTEST.YEAR = CHAIR.YEAR ");
        stb.append("     AND CTEST.SEMESTER = CHAIR.SEMESTER ");
        stb.append("     AND CTEST.TESTKINDCD || CTEST.TESTITEMCD = '" + _param._testCd.substring(0, 4) + "' ");
        stb.append(" LEFT JOIN ");
        stb.append("     SUBCLASS_MST SUB ");
        stb.append("      ON SUB.CLASSCD = CHAIR.CLASSCD ");
        stb.append("     AND SUB.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
        stb.append("     AND SUB.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
        stb.append("     AND SUB.SUBCLASSCD = CHAIR.SUBCLASSCD ");
        stb.append(" LEFT JOIN ");
        stb.append("     CHAIR_DETAIL_DAT CD004 ");
        stb.append("      ON CD004.YEAR = CHAIR.YEAR ");
        stb.append("     AND CD004.SEMESTER = CHAIR.SEMESTER ");
        stb.append("     AND CD004.CHAIRCD = CHAIR.CHAIRCD ");
        stb.append("     AND CD004.SEQ = '004' ");
        stb.append(" LEFT JOIN ");
        stb.append("     CHAIR_STF_DAT CSTF ");
        stb.append("      ON CSTF.YEAR = CHAIR.YEAR ");
        stb.append("     AND CSTF.SEMESTER = CHAIR.SEMESTER ");
        stb.append("     AND CSTF.CHAIRCD = CHAIR.CHAIRCD ");
        stb.append("     AND CSTF.CHARGEDIV = '1' ");
        stb.append(" LEFT JOIN ");
        stb.append("     STAFF_MST STF1 ");
        stb.append("      ON STF1.STAFFCD = CSTF.STAFFCD ");
        stb.append(" LEFT JOIN ");
        stb.append("     TESTITEM_LABEL_MST LABEL ");
        stb.append("      ON LABEL.YEAR = CHAIR.YEAR ");
        stb.append("     AND LABEL.SEMESTER = CHAIR.SEMESTER ");
        stb.append("     AND LABEL.GRADE = '" + _param._grade + "' ");
        stb.append("     AND LABEL.TESTKINDCD || LABEL.TESTITEMCD || LABEL.SCORE_DIV = '" + _param._testCd + "' ");
        stb.append("     AND LABEL.CLASSCD = CHAIR.CLASSCD ");
        stb.append("     AND LABEL.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
        stb.append("     AND LABEL.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
        stb.append("     AND LABEL.SUBCLASSCD = CHAIR.SUBCLASSCD ");
        stb.append("     AND LABEL.CHAIRCD = CHAIR.CHAIRCD ");
        stb.append(" LEFT JOIN ");
        stb.append("     STAFF_MST STF2 ");
        stb.append("      ON STF2.STAFFCD = LABEL.RETURN_STAFFCD ");
        stb.append(" LEFT JOIN ");
        stb.append("     SCH_STF_DAT SSTF ");
        stb.append("      ON SSTF.EXECUTEDATE = CTEST.EXECUTEDATE ");
        stb.append("     AND SSTF.PERIODCD = CTEST.PERIODCD ");
        stb.append("     AND SSTF.CHAIRCD = CTEST.CHAIRCD ");
        stb.append(" LEFT JOIN ");
        stb.append("     STAFF_MST STF3 ");
        stb.append("      ON STF3.STAFFCD = SSTF.STAFFCD ");
        stb.append(" LEFT JOIN ");
        stb.append("     V_NAME_MST NAMEB001 ");
        stb.append("      ON NAMEB001.YEAR = CHAIR.YEAR ");
        stb.append("     AND NAMEB001.NAMECD1 = 'B001' ");
        stb.append("     AND NAMEB001.NAMECD2 = CTEST.PERIODCD ");
        stb.append(" LEFT JOIN ");
        stb.append("     (SELECT ");
        stb.append("         FAC.EXECUTEDATE, ");
        stb.append("         FAC.PERIODCD, ");
        stb.append("         FAC.CHAIRCD, ");
        stb.append("         LISTAGG(MST.FACILITYABBV, ' ') AS FACILITYABBV ");
        stb.append("     FROM ");
        stb.append("         SCH_FAC_DAT FAC ");
        stb.append("     LEFT JOIN ");
        stb.append("         FACILITY_MST MST ");
        stb.append("          ON MST.FACCD = FAC.FACCD ");
        stb.append("     WHERE ");
        stb.append("         FAC.CHAIRCD IN " +  SQLUtils.whereIn(true, _param._categorySelected));
        stb.append("     GROUP BY ");
        stb.append("         EXECUTEDATE, PERIODCD, CHAIRCD ");
        stb.append("     ) AS FAC ");
        stb.append("     ON FAC.EXECUTEDATE = CTEST.EXECUTEDATE ");
        stb.append("    AND FAC.PERIODCD = CTEST.PERIODCD ");
        stb.append("    AND FAC.CHAIRCD = CTEST.CHAIRCD   ");
        stb.append(" WHERE ");
        stb.append("     CHAIR.YEAR = '" + _param._year + "' AND ");
        stb.append("     CHAIR.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("     CHAIR.CHAIRCD IN " +  SQLUtils.whereIn(true, _param._categorySelected));
        stb.append(" ORDER BY ");
        stb.append("     CHAIR.CHAIRCD, CTEST.EXECUTEDATE, CTEST.PERIODCD ");

        return stb.toString();
    }

    private class TestInfo {
        final String _key;
        final String _year;
        final String _semester;
        final String _chaircd;
        final String _classcd;
        final String _school_Kind;
        final String _curriculum_Cd;
        final String _subclasscd;
        final String _chairname;
        final String _executedate;
        final String _period;
        final String _subclassname;
        final String _remark2;
        final String _remark3;
        final String _stfname1;
        final String _stfname2;
        final String _stfname3;
        final String _staffcd;
        final String _due_Date;
        final String _due_Time;
        final String _q_Papers;
        final String _a_Papers;
        final String _q_Both_Div;
        final String _a_Both_Div;
        final String _remark;
        final String _facilityabbv;
        final List<Student> studentList = new ArrayList();

        public TestInfo(final String key, final String year, final String semester, final String chaircd, final String classcd,
                final String school_Kind, final String curriculum_Cd, final String subclasscd, final String chairname,
                final String executedate, final String period, final String subclassname, final String remark2,
                final String remark3, final String stfname1, final String stfname2, final String stfname3,
                final String staffcd, final String due_Date, final String due_Time, final String q_Papers,
                final String a_Papers, final String q_Both_Div, final String a_Both_Div, final String remark,
                final String facilityabbv) {
            _key = key;
            _year = year;
            _semester = semester;
            _chaircd = chaircd;
            _classcd = classcd;
            _school_Kind = school_Kind;
            _curriculum_Cd = curriculum_Cd;
            _subclasscd = subclasscd;
            _chairname = chairname;
            _executedate = executedate;
            _period = period;
            _subclassname = subclassname;
            _remark2 = remark2;
            _remark3 = remark3;
            _stfname1 = stfname1;
            _stfname2 = stfname2;
            _stfname3 = stfname3;
            _staffcd = staffcd;
            _due_Date = due_Date;
            _due_Time = due_Time;
            _q_Papers = q_Papers;
            _a_Papers = a_Papers;
            _q_Both_Div = q_Both_Div;
            _a_Both_Div = a_Both_Div;
            _remark = remark;
            _facilityabbv = facilityabbv;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String[] _categorySelected;
        final String _semester;
        final String _testCd;
        final String _year;
        final String _grade;
        final String _date;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _year = request.getParameter("CTRL_YEAR");
            _grade = request.getParameter("GRADE");
            _semester = request.getParameter("SEMESTER");
            _testCd = request.getParameter("TESTCD");
            _date = request.getParameter("CTRL_DATE");
        }
    }
}

// eof
