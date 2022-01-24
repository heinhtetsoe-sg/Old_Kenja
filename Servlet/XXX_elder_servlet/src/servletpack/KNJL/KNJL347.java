// kanji=漢字
/*
 * $Id: 350c2904df916e4e15a82f4badc15bcb89fea9c5 $
 *
 * 作成日: 2013/11/19 11:19:04 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2013 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 350c2904df916e4e15a82f4badc15bcb89fea9c5 $
 */
public class KNJL347 {

    private static final Log log = LogFactory.getLog("KNJL347.class");

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
        svf.VrSetForm(_param.getFormName(), 1);
        final List printList = getPrintList(db2);
        for (final Iterator iter = printList.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();
            svf.VrsOut("EXAM_NO1", student._examno);
            svf.VrsOut("NAME1_1", student._name);
            svf.VrsOut("SCHOOL_NAME1", "大宮開成中学校　" + _param._testDivName+"("+KNJ_EditDate.h_format_JP_MD(_param._testDay)+")得点状況について、");
            svf.VrsOut("EXAM_NAME1", "下記のとおりお知らせいたします。");
            svf.VrsOut("LOW_PASS_NAME1", _param._lowPassName1);
            for (final Iterator itKamoku = _param._kamokuMap.keySet().iterator(); itKamoku.hasNext();) {
                final String nameCd2 = (String) itKamoku.next();
                final TestKamoku testKamoku = (TestKamoku) _param._kamokuMap.get(nameCd2);
                svf.VrsOut("CLASS_NAME" + nameCd2, testKamoku._name);
                svf.VrsOut("CLASS_NAME" + nameCd2 + "_2", testKamoku._name);
                if (null != testKamoku._avarageDat) {
                    if ("S".equals(testKamoku._kamokuCd)) {
                        svf.VrsOutn("PASS_SCORE5", 6, testKamoku._avarageDat._saitei);
                    } else if ("T".equals(testKamoku._kamokuCd)) {
                        svf.VrsOut("TOTAL_SP_PASS1", testKamoku._avarageDat._saitei);
                    } else {
                        svf.VrsOutn("PASS_SCORE" + nameCd2, 1, testKamoku._avarageDat._dansi);
                        svf.VrsOutn("PASS_SCORE" + nameCd2, 2, testKamoku._avarageDat._josi);
                        svf.VrsOutn("PASS_SCORE" + nameCd2, 3, testKamoku._avarageDat._goukei);
                        svf.VrsOutn("PASS_SCORE" + nameCd2, 4, testKamoku._avarageDat._saikou);
                        if ("A".equals(testKamoku._kamokuCd)) {
                            svf.VrsOutn("PASS_SCORE" + nameCd2, 5, testKamoku._avarageDat._saitei);
                        } else {
                            String setSaitei = testKamoku._avarageDat._saitei;
                            if (_param._saiteiNoDisp) {
                                setSaitei = "-";
                            }
                            svf.VrsOutn("PASS_SCORE" + nameCd2, 5, setSaitei);
                        }
                    }
                }
            }
            for (final Iterator itSub = student._scoreMap.keySet().iterator(); itSub.hasNext();) {
                final String kamoku = (String) itSub.next();
                final String score = (String) student._scoreMap.get(kamoku);
                final String key = "A".equals(kamoku) ? "5" : kamoku;
                final TestKamoku testKamoku = (TestKamoku) _param._kamokuMap.get(key);
                if (null == score) continue;
                final String setField = testKamoku.getScoreField(Integer.parseInt(score));
                svf.VrsOut(setField, "A".equals(kamoku) ? "☆" : "★");//得点ゾーン
                svf.VrsOut("SCORE" + key, score);//得点
            }
            _hasData = true;
            svf.VrEndPage();
        }
    }

    private List getPrintList(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        final String studenSql = getStudentSql();
        PreparedStatement psStudent = null;
        ResultSet rsStudent = null;
        try {
            psStudent = db2.prepareStatement(studenSql);
            rsStudent = psStudent.executeQuery();
            while (rsStudent.next()) {
                final Student student = new Student(db2, rsStudent.getString("EXAMNO"), rsStudent.getString("RECEPTNO"), rsStudent.getString("NAME"), rsStudent.getString("NAME_KANA"));
                retList.add(student);
            }
        } finally {
            DbUtils.closeQuietly(null, psStudent, rsStudent);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T1.EXAMNO, ");
        stb.append("   T1.RECEPTNO, ");
        stb.append("   L1.NAME, ");
        stb.append("   L1.NAME_KANA ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_RECEPT_DAT T1 ");
        stb.append("   LEFT JOIN ENTEXAM_APPLICANTBASE_DAT L1 ON T1.ENTEXAMYEAR = L1.ENTEXAMYEAR ");
        stb.append("        AND T1.EXAMNO = L1.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("   T1.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("   AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("   AND T1.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("   AND T1.EXAM_TYPE = '2' ");
        if (null != _param._examNo && !"".equals(_param._examNo)) {
            stb.append("   AND T1.EXAMNO = '" + _param._examNo + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("   T1.EXAMNO ");
        return stb.toString();
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
        log.fatal("$Revision: 71097 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _examNo;
        private final String _targetDiv; //1:得点ゾーン、2:得点
        private String _testDivName;
        private String _testDay;
        private String _classDiv;
        private String _lowPassName1;
        private final boolean _saiteiNoDisp;
        private final Map _kamokuMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _examNo = request.getParameter("EXAMNO");
            _targetDiv = request.getParameter("TARGET_DIV");
            setTestDivMst(db2);
            _saiteiNoDisp = "1".equals(request.getParameter("SAITEI_NODISP"));
            _kamokuMap = getKamokuMap(db2);
        }

        //入試区分マスタ
        private void setTestDivMst(final DB2UDB db2) throws SQLException {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_TESTDIV_MST ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR = '" + _year + "' ");
            stb.append("     AND TESTDIV = '" + _testDiv + "' ");
            PreparedStatement psTestDiv = null;
            ResultSet rsTestDiv = null;
            try {
                final String sql = stb.toString();
                psTestDiv = db2.prepareStatement(sql);
                rsTestDiv = psTestDiv.executeQuery();
                while (rsTestDiv.next()) {
                    _testDivName = rsTestDiv.getString("NAME");
                    _testDay = rsTestDiv.getString("TESTDAY");
                    _classDiv = rsTestDiv.getString("CLASSDIV");
                }
            } finally {
                DbUtils.closeQuietly(null, psTestDiv, rsTestDiv);
                db2.commit();
            }
            if ("1".equals(_classDiv)) {
                _lowPassName1 = "アップ合格　最低点";
            } else if ("2".equals(_classDiv)) {
                _lowPassName1 = "スライド合格　最低点";
            } else {
                _lowPassName1 = "特別進学クラス合格　最低点";
            }
        }

        private Map getKamokuMap(final DB2UDB db2) throws SQLException {
            final Map retMap = new TreeMap();
            final String kamokuSql = getKamokuSql();
            PreparedStatement psKamoku = null;
            ResultSet rsKamoku = null;
            try {
                psKamoku = db2.prepareStatement(kamokuSql);
                rsKamoku = psKamoku.executeQuery();
                while (rsKamoku.next()) {
                    TestKamoku kamoku = new TestKamoku(rsKamoku.getString("NAME1"), rsKamoku.getString("KAMOKU"));
                    retMap.put(rsKamoku.getString("NAMECD2"), kamoku);
                }
            } finally {
                DbUtils.closeQuietly(null, psKamoku, rsKamoku);
                db2.commit();
            }

            for (final Iterator iter = retMap.keySet().iterator(); iter.hasNext();) {
                final String kamokuCd = (String) iter.next();
                final TestKamoku kamoku = (TestKamoku) retMap.get(kamokuCd);

                final String avgSql = getJudgeAvgSql(kamoku._kamokuCd);
                PreparedStatement psAvg = null;
                ResultSet rsAvg = null;
                try {
                    psAvg = db2.prepareStatement(avgSql);
                    rsAvg = psAvg.executeQuery();
                    while (rsAvg.next()) {
                        final String dansi = rsAvg.getString("AVARAGE_MEN");
                        final String josi = rsAvg.getString("AVARAGE_WOMEN");
                        final String goukei = rsAvg.getString("AVARAGE_TOTAL");
                        final String saikou = rsAvg.getString("MAX_SCORE");
                        final String saitei = rsAvg.getString("MIN_SCORE");
                        kamoku._avarageDat = new EntexamJudgeAvarageDat(dansi, josi, goukei, saikou, saitei);
                    }
                } finally {
                    DbUtils.closeQuietly(null, psAvg, rsAvg);
                    db2.commit();
                }
            }
            return retMap;
        }

        private String getKamokuSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAKE_SONOTA(NAMECD2, KAMOKU, NAME1) AS ( ");
            stb.append(" VALUES('5', 'A', '4教科総合') ");
            stb.append(" UNION ");
            stb.append(" VALUES('6', 'S', '最低点') ");
            stb.append(" UNION ");
            stb.append(" VALUES('7', 'T', '特待合格') ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     NAMECD2, ");
            stb.append("     NAMECD2 AS KAMOKU, ");
            stb.append("     NAME1 ");
            stb.append(" FROM ");
            stb.append("     NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     NAMECD1 = 'L009' ");
            stb.append("     AND INT(NAMECD2) <= 4 ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     NAMECD2, ");
            stb.append("     KAMOKU, ");
            stb.append("     NAME1 ");
            stb.append(" FROM ");
            stb.append("     MAKE_SONOTA ");
            stb.append(" ORDER BY ");
            stb.append("     NAMECD2 ");
            return stb.toString();
        }

        private String getJudgeAvgSql(final String kamokuCd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T1.* ");
            stb.append(" FROM ");
            stb.append("   ENTEXAM_JUDGE_AVARAGE_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("   T1.ENTEXAMYEAR = '" + _year + "' ");
            stb.append("   AND T1.APPLICANTDIV = '" + _applicantDiv + "' ");
            stb.append("   AND T1.TESTDIV = '" + _testDiv + "' ");
            stb.append("   AND T1.EXAM_TYPE = '2' ");
            stb.append("   AND T1.TESTSUBCLASSCD = '" + kamokuCd + "' ");
            return stb.toString();
        }

        //フォーム
        private String getFormName() {
            String retStrng = "";
            if ("2".equals(_targetDiv)) {
                retStrng = "KNJL347_3.frm";
            } else if ("1".equals(_classDiv)) {
                retStrng = "KNJL347_1.frm";
            } else if ("2".equals(_classDiv)) {
                retStrng = "KNJL347_1.frm";
            } else {
                retStrng = "KNJL347_2.frm";
            }
            return retStrng;
        }

    }

    /** 生徒 */
    private class Student {
        private final String _examno;
        private final String _receptno;
        private final String _name;
        private final String _nameKana;
        private final Map _scoreMap;

        Student(final DB2UDB db2,
                final String examno,
                final String receptno,
                final String name,
                final String nameKana
        ) throws SQLException {
            _examno = examno;
            _receptno = receptno;
            _name = name;
            _nameKana = nameKana;
            _scoreMap = getScoreMap(db2);
        }

        private Map getScoreMap(final DB2UDB db2) throws SQLException {
            final Map retMap = new HashMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_SCORE_SPARE_DAT ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR = '" + _param._year + "' ");
            stb.append("     AND APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND TESTDIV = '" + _param._testDiv + "' ");
            stb.append("     AND EXAM_TYPE = '2' ");
            stb.append("     AND RECEPTNO = '" + _receptno + "' ");
            stb.append("     AND SEQ = '001' ");
            stb.append(" ORDER BY ");
            stb.append("     TESTSUBCLASSCD ");
            PreparedStatement psScore = null;
            ResultSet rsScore = null;
            try {
                psScore = db2.prepareStatement(stb.toString());
                rsScore = psScore.executeQuery();
                while (rsScore.next()) {
                    retMap.put(rsScore.getString("TESTSUBCLASSCD"), rsScore.getString("SCORE1"));
                }
            } finally {
                DbUtils.closeQuietly(null, psScore, rsScore);
                db2.commit();
            }
            return retMap;
        }

    }

    /** 科目 */
    private class TestKamoku {
        private final String _name;
        private final String _kamokuCd;
        private EntexamJudgeAvarageDat _avarageDat;
        private Map _scoreMap = new HashMap();

        TestKamoku(final String name, final String kamokuCd) {
            _name = name;
            _kamokuCd = kamokuCd;
            int setCnt = 1;
            if ("1".equals(kamokuCd) || "2".equals(kamokuCd)) {
                _scoreMap.put("SCORE" + kamokuCd + "_" + setCnt++, new ScoreHani(80, 100));
                _scoreMap.put("SCORE" + kamokuCd + "_" + setCnt++, new ScoreHani(70, 79));
                _scoreMap.put("SCORE" + kamokuCd + "_" + setCnt++, new ScoreHani(60, 69));
                _scoreMap.put("SCORE" + kamokuCd + "_" + setCnt++, new ScoreHani(50, 59));
                _scoreMap.put("SCORE" + kamokuCd + "_" + setCnt++, new ScoreHani(40, 49));
                _scoreMap.put("SCORE" + kamokuCd + "_" + setCnt++, new ScoreHani(0, 39));
            } else if ("3".equals(kamokuCd) || "4".equals(kamokuCd)) {
                _scoreMap.put("SCORE" + kamokuCd + "_" + setCnt++, new ScoreHani(40, 50));
                _scoreMap.put("SCORE" + kamokuCd + "_" + setCnt++, new ScoreHani(35, 39));
                _scoreMap.put("SCORE" + kamokuCd + "_" + setCnt++, new ScoreHani(30, 34));
                _scoreMap.put("SCORE" + kamokuCd + "_" + setCnt++, new ScoreHani(25, 29));
                _scoreMap.put("SCORE" + kamokuCd + "_" + setCnt++, new ScoreHani(20, 24));
                _scoreMap.put("SCORE" + kamokuCd + "_" + setCnt++, new ScoreHani(0, 19));
            } else {
                _scoreMap.put("SCORE5_" + setCnt++, new ScoreHani(241, 999));
                _scoreMap.put("SCORE5_" + setCnt++, new ScoreHani(221, 240));
                _scoreMap.put("SCORE5_" + setCnt++, new ScoreHani(201, 220));
                _scoreMap.put("SCORE5_" + setCnt++, new ScoreHani(181, 200));
                _scoreMap.put("SCORE5_" + setCnt++, new ScoreHani(161, 180));
                _scoreMap.put("SCORE5_" + setCnt++, new ScoreHani(141, 160));
                _scoreMap.put("SCORE5_" + setCnt++, new ScoreHani(121, 140));
                _scoreMap.put("SCORE5_" + setCnt++, new ScoreHani(0, 120));
            }
        }

        private String getScoreField (final int score) {
            for (final Iterator iter = _scoreMap.keySet().iterator(); iter.hasNext();) {
                final String fieldName = (String) iter.next();
                final ScoreHani hani = (ScoreHani) _scoreMap.get(fieldName);
                if (hani._hiScore >= score && hani._lowScore <= score) {
                    return fieldName;
                }
            }
            return "";
        }
    }

    /** 得点範囲 */
    private class ScoreHani {
        private final int _lowScore;
        private final int _hiScore;

        ScoreHani(final int lowScore, final int hiScore) {
            _lowScore = lowScore;
            _hiScore = hiScore;
        }

    }

    /** 最高点、最低点、平均点 */
    private class EntexamJudgeAvarageDat {
        private final String _dansi;
        private final String _josi;
        private final String _goukei;
        private final String _saikou;
        private final String _saitei;

        EntexamJudgeAvarageDat(
                final String dansi,
                final String josi,
                final String goukei,
                final String saikou,
                final String saitei
        ) throws SQLException {
            _dansi = dansi;
            _josi = josi;
            _goukei = goukei;
            _saikou = saikou;
            _saitei = saitei;
        }

    }
}

// eof
