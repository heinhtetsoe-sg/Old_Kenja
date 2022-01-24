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
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJB103B {

    private static final Log log = LogFactory.getLog(KNJB103B.class);

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

    //受験教室表
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final Map testInfoMap = getTestInfo(db2); //試験情報取得
        if (testInfoMap.isEmpty()) return;

        final int maxRow = 30; //最終行
        final int maxCol = 5;  //最終列
        int col = 1; //印字列

        final String form = "KNJB103B.frm";
        svf.VrSetForm(form, 1);

        for (Iterator iterator = testInfoMap.keySet().iterator(); iterator.hasNext();) {
            printTitle(svf); //明細部以外を印字
            final String key = (String) iterator.next();
            final Map printMap = (Map)testInfoMap.get(key);

            int row = 1; //印字行
            final String date = KNJ_EditDate.h_format_JP_MD(key);
            final String youbi = KNJ_EditDate.h_format_W(key);
            svf.VrsOut("DAY" + col, date + "(" + youbi + ")");  //日付

            //講座毎のループ
            for (Iterator ite = printMap.keySet().iterator(); ite.hasNext();) {
                final String keyTest = (String) ite.next();
                final TestInfo testInfo = (TestInfo) printMap.get(keyTest);

                //改ページ処理
                if (row > maxRow) {
                    row = 1;
                    col++;
                    if (col <= maxCol) {
                        svf.VrsOut("DAY" + col, date + "(" + youbi + ")");  //日付
                    }
                }
                if (col > maxCol) {
                    col = 1;
                    row = 1;
                    svf.VrEndPage();
                    printTitle(svf);
                    svf.VrsOut("DAY" + col, date + "(" + youbi + ")");  //日付
                }

                svf.VrsOutn("PERIOD_NAME" + col, row, testInfo._abbv1);  //時限
                final int keta = KNJ_EditEdit.getMS932ByteLength(testInfo._subclassname);
                final String field = keta <= 22 ? "1" : "2";
                svf.VrsOutn("SUBCLASS_NAME" + col + "_" + field, row, testInfo._subclassname);  //科目名
                svf.VrsOutn("HR_NAME" + col, row, testInfo._remark2);  //クラス
                svf.VrsOutn("FACILITY_NAME" + col, row, testInfo._facilityabbv);  //教室

                _hasData = true;

                row++;
            }
            col++;
        }
        svf.VrEndPage();
    }

    private void printTitle(final Vrw32alp svf) {
        svf.VrsOut("TITLE", _param._gradeName + " " + _param._year + "年度 " + _param._semesterName + _param._testName + " 時間割（使用教室一覧）");  //タイトル
        svf.VrsOut("PRINT_DATE", "出力日：" + _param._date.replace("-", "/"));  //出力日
    }

    private class TestInfo {
        final String _executedate;
        final String _periodcd;
        final String _chaircd;
        final String _testkindcd;
        final String _testitemcd;
        final String _year;
        final String _semester;
        final String _subclassname;
        final String _remark2;
        final String _faccd;
        final String _facilityabbv;
        final String _abbv1;

        public TestInfo(final String executedate, final String periodcd, final String chaircd, final String testkindcd,
                final String testitemcd, final String year, final String semester, final String subclassname,
                final String remark2, final String faccd, final String facilityabbv, final String abbv1) {
            _executedate = executedate;
            _periodcd = periodcd;
            _chaircd = chaircd;
            _testkindcd = testkindcd;
            _testitemcd = testitemcd;
            _year = year;
            _semester = semester;
            _subclassname = subclassname;
            _remark2 = remark2;
            _faccd = faccd;
            _facilityabbv = facilityabbv;
            _abbv1 = abbv1;
        }
    }

    private Map getTestInfo(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map addMap = null;
        final Map retMap = new LinkedMap();
        final String sql = setTestInfoSql();
        log.debug(sql);

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String executedate = rs.getString("EXECUTEDATE");
                final String periodcd = rs.getString("PERIODCD");
                final String chaircd = rs.getString("CHAIRCD");
                final String testkindcd = rs.getString("TESTKINDCD");
                final String testitemcd = rs.getString("TESTITEMCD");
                final String year = rs.getString("YEAR");
                final String semester = rs.getString("SEMESTER");
                final String subclassname = rs.getString("SUBCLASSNAME");
                final String remark2 = rs.getString("REMARK2");
                final String faccd = rs.getString("FACCD");
                final String facilityabbv = rs.getString("FACILITYABBV");
                final String abbv1 = rs.getString("ABBV1");

                if(!retMap.containsKey(executedate)) {
                    addMap = new LinkedMap();
                    retMap.put(executedate, addMap);
                } else {
                    addMap = (Map)retMap.get(executedate);
                }

                final String key = periodcd + "_" + chaircd;

                if(!addMap.containsKey(key)) {
                    final TestInfo wk = new TestInfo(executedate, periodcd, chaircd, testkindcd, testitemcd, year, semester, subclassname, remark2, faccd, facilityabbv, abbv1);
                    addMap.put(key, wk);
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
        stb.append("     TEST.EXECUTEDATE, ");
        stb.append("     TEST.PERIODCD, ");
        stb.append("     TEST.CHAIRCD, ");
        stb.append("     TEST.TESTKINDCD, ");
        stb.append("     TEST.TESTITEMCD, ");
        stb.append("     TEST.YEAR, ");
        stb.append("     TEST.SEMESTER, ");
        stb.append("     SUB.SUBCLASSNAME, ");
        stb.append("     DT004.REMARK2, ");
        stb.append("     FAC.FACCD, ");
        stb.append("     FMST.FACILITYABBV, ");
        stb.append("     NAME.ABBV1 ");
        stb.append(" FROM ");
        stb.append("     SCH_CHR_TEST TEST ");
        stb.append(" INNER JOIN ");
        stb.append("     CHAIR_STD_DAT STD ");
        stb.append("      ON STD.YEAR = TEST.YEAR ");
        stb.append("     AND STD.SEMESTER = TEST.SEMESTER ");
        stb.append("     AND STD.CHAIRCD = TEST.CHAIRCD ");
        stb.append(" INNER JOIN ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("      ON REGD.YEAR = STD.YEAR ");
        stb.append("     AND REGD.SEMESTER = STD.SEMESTER ");
        stb.append("     AND REGD.GRADE = '" + _param._grade + "' ");
        stb.append("     AND REGD.SCHREGNO = STD.SCHREGNO ");
        stb.append(" INNER JOIN ");
        stb.append("     CHAIR_DAT CHAIR ");
        stb.append("      ON CHAIR.YEAR = TEST.YEAR ");
        stb.append("     AND CHAIR.SEMESTER = TEST.SEMESTER ");
        stb.append("     AND CHAIR.CHAIRCD = TEST .CHAIRCD ");
        stb.append(" LEFT JOIN ");
        stb.append("     SUBCLASS_MST SUB ");
        stb.append("      ON SUB.CLASSCD = CHAIR.CLASSCD ");
        stb.append("     AND SUB.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
        stb.append("     AND SUB.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
        stb.append("     AND SUB.SUBCLASSCD = CHAIR.SUBCLASSCD ");
        stb.append(" LEFT JOIN ");
        stb.append("     CHAIR_DETAIL_DAT DT004 ");
        stb.append("      ON DT004.YEAR = CHAIR.YEAR ");
        stb.append("     AND DT004.SEMESTER = CHAIR.SEMESTER ");
        stb.append("     AND DT004.CHAIRCD = CHAIR.CHAIRCD ");
        stb.append("     AND DT004.SEQ = '004' ");
        stb.append(" LEFT JOIN ");
        stb.append("     SCH_FAC_DAT FAC ");
        stb.append("      ON FAC.EXECUTEDATE = TEST.EXECUTEDATE ");
        stb.append("      AND FAC.PERIODCD = TEST.PERIODCD ");
        stb.append("      AND FAC.CHAIRCD = TEST.CHAIRCD ");
        stb.append(" LEFT JOIN ");
        stb.append("     FACILITY_MST FMST ");
        stb.append("     ON FMST.FACCD = FAC.FACCD ");
        stb.append(" LEFT JOIN ");
        stb.append("     V_NAME_MST NAME ");
        stb.append("      ON NAME.YEAR = TEST.YEAR ");
        stb.append("     AND NAME.NAMECD1 = 'B001' ");
        stb.append("     AND NAME.NAMECD2 = TEST.PERIODCD ");
        stb.append(" WHERE ");
        stb.append("     TEST.YEAR = '" + _param._year + "' AND ");
        stb.append("     TEST.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("     TEST.TESTKINDCD || TEST.TESTITEMCD = '" + _param._testCd.substring(0,4) + "' ");
        if(_param._check) { //スモールクラス設定があるものだけ
            stb.append("     AND DT004.REMARK2 IS NOT NULL ");
        }
        stb.append(" ORDER BY ");
        stb.append("     TEST.EXECUTEDATE, ");
        stb.append("     TEST.PERIODCD, ");
        stb.append("     TEST.CHAIRCD ");

        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _semester;
        final String _testCd;
        final String _year;
        final String _grade;
        final String _date;
        final String _gradeName;
        final String _semesterName;
        final String _testName;
        final boolean _check;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _grade = request.getParameter("GRADE");
            _semester = request.getParameter("SEMESTER");
            _testCd = request.getParameter("TESTCD");
            _date = request.getParameter("CTRL_DATE");
            _check = "1".equals(request.getParameter("CHECK")); //スモールクラスのみフラグ

            _gradeName = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' AND SCHOOL_KIND = 'H' "));
            _semesterName = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' "));
            _testName = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testCd + "' "));

        }
    }
}

// eof
