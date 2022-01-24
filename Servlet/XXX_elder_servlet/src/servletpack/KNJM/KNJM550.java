// kanji=漢字
/*
 * $Id: 9941b46c0e00924ec565fad3c83654a0cbe4ce4c $
 *
 * 作成日: 2009/09/30 9:13:59 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 9941b46c0e00924ec565fad3c83654a0cbe4ce4c $
 */
public class KNJM550 {

    private static final Log log = LogFactory.getLog("KNJM550.class");
    private static final String FORM_NAME1 = "KNJM550_1.frm";
    private static final String FORM_NAME2 = "KNJM550_2.frm";

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
        final List stuedentData = getStuedentData(db2);
        int pageCnt = 1;
        pageCnt = printHead(svf, FORM_NAME1, pageCnt);
        int lineCnt = 1;
        int retuCnt = 1;
        boolean isFirstPage = true;
        for (final Iterator itStudent = stuedentData.iterator(); itStudent.hasNext();) {
            final Student student = (Student) itStudent.next();
            if (isFirstPage && lineCnt > 30) {
                if (retuCnt == 2) {
                    svf.VrEndPage();
                    pageCnt = printHead(svf, FORM_NAME2, pageCnt);
                    retuCnt = 1;
                    lineCnt = 1;
                    isFirstPage = false;
                } else {
                    retuCnt++;
                    lineCnt = 1;
                }
            }
            if (lineCnt > 45) {
                if (retuCnt == 2) {
                    svf.VrEndPage();
                    pageCnt = printHead(svf, FORM_NAME2, pageCnt);
                    retuCnt = 1;
                    lineCnt = 1;
                } else {
                    retuCnt++;
                    lineCnt = 1;
                }
            }
            //生徒印字
            printStudent(svf, student, retuCnt, lineCnt);
            lineCnt++;
            _hasData = true;
        }
        if (lineCnt > 1) {
            svf.VrEndPage();
        }
    }

    private int printHead(final Vrw32alp svf, final String formName, final int pageCnt) {
        svf.VrSetForm(formName, 1);
        svf.VrsOut("DATE", _param._ctrlDate);
        svf.VrsOut("NENDO", _param._year + "年");
        svf.VrsOut("TERM", _param._semesterName);
        svf.VrsOut("PAGE", pageCnt + "ページ");
        final String mainSearch = _param._mainSearch.equals("1") ? "AND" : "OR";
        svf.VrsOut("CONDITION", mainSearch);

        final String kamoku = "科目：";
        int joukenCnt = 1;
        if (pageCnt == 1) {
            printJouken(svf, kamoku, joukenCnt);
        }
        return pageCnt + 1;
    }

    private void printJouken(final Vrw32alp svf, final String kamoku, int joukenCnt) {
        for (final Iterator itPanel = _param._panelList.iterator(); itPanel.hasNext();) {
            final PanelData panelData = (PanelData) itPanel.next();
            svf.VrsOutn("CONDITION_NO", joukenCnt, "条件" + joukenCnt);
            svf.VrsOutn("CLASS", joukenCnt, kamoku);
            svf.VrsOutn("CLASS_NAME", joukenCnt, panelData._subclassName);
            final String subSearch = panelData._searchs.equals("1") ? "AND" : "OR";
            svf.VrsOutn("SUB_CONDITION", joukenCnt, "(" + subSearch + "条件で検索)");

            //レポート
            svf.VrsOutn("REPORT", joukenCnt, "レポート提出：");
            if (null != panelData._report && !panelData._report.equals("")) {
                if (panelData._reports.equals("1")) {
                    svf.VrsOutn("REPORT_SUBMIT", joukenCnt, panelData._report + "回まで提出済み");
                } else if (panelData._reports.equals("2")) {
                    svf.VrsOutn("REPORT_SUBMIT", joukenCnt, panelData._report + "回まで合格");
                } else {
                    svf.VrsOutn("REPORT_SUBMIT", joukenCnt, panelData._report + "回までに不合格・未提出あり");
                }
            }
            //スクーリング
            svf.VrsOutn("SCHOOLING", joukenCnt, "スクーリング出席回数：");
            if (null != panelData._schooling && !panelData._schooling.equals("")) {
                if (panelData._schoolings.equals("1")) {
                    svf.VrsOutn("SCHOOLING_COUNT", joukenCnt, panelData._schooling + "回に等しい");
                } else if (panelData._schoolings.equals("2")) {
                    svf.VrsOutn("SCHOOLING_COUNT", joukenCnt, panelData._schooling + "回以上");
                } else {
                    svf.VrsOutn("SCHOOLING_COUNT", joukenCnt, panelData._schooling + "回以下");
                }
            }
            //考査得点
            final String setTestName = "KNJM550M".equals(_param._prgId) ? "試験得点：" : "考査得点：";
            svf.VrsOutn("TEST", joukenCnt, setTestName);
            if (null != panelData._score && !panelData._score.equals("")) {
                if (panelData._scores.equals("1")) {
                    svf.VrsOutn("TEST_SCORE", joukenCnt, panelData._score + "点に等しい");
                } else if (panelData._scores.equals("2")) {
                    svf.VrsOutn("TEST_SCORE", joukenCnt, panelData._score + "点以上");
                } else {
                    svf.VrsOutn("TEST_SCORE", joukenCnt, panelData._score + "点以下");
                }
            }
            //評価・評定
            final String setEvaluation = "KNJM550M".equals(_param._prgId) ? "学年成績：" : "評価/評定：";
            svf.VrsOutn("EVALUATION", joukenCnt, setEvaluation);
            if (null != panelData._hyoutei && !panelData._hyoutei.equals("")) {
                if (panelData._hyouteis.equals("1")) {
                    svf.VrsOutn("EVALUATION_RESULT", joukenCnt, panelData._hyoutei + "に等しい");
                } else if (panelData._hyouteis.equals("2")) {
                    svf.VrsOutn("EVALUATION_RESULT", joukenCnt, panelData._hyoutei + "以上");
                } else {
                    svf.VrsOutn("EVALUATION_RESULT", joukenCnt, panelData._hyoutei + "以下");
                }
            }
            joukenCnt++;
        }
    }

    private void printStudent(final Vrw32alp svf, final Student student, final int retuCnt, final int lineCnt) {
        svf.VrsOutn("SCHREGNO" + retuCnt, lineCnt, student._schregno);
        svf.VrsOutn("HR_NAME" + retuCnt, lineCnt, student._hrName);
        svf.VrsOutn("NO" + retuCnt, lineCnt, student._attendno);
        svf.VrsOutn("NAME" + retuCnt, lineCnt, student._name);
    }

    private List getStuedentData(final DB2UDB db2) throws SQLException {
        final List rtnList = new ArrayList();
        ResultSet rs = null;
        try {
            for (int i = 0; i < _param._schregNos.length; i++) {
                String schregNo = _param._schregNos[i];
                if ("KNJM550M".equals(_param._prgId) && "3".equals(_param._typeDiv)) {
                    final String[] schArray = StringUtils.split(schregNo, "-");
                    schregNo = schArray[1];
                }
                final String sql = getStudentInfo(schregNo);
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String semester = rs.getString("SEMESTER");
                    final String schregno = rs.getString("SCHREGNO");
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");
                    final String hrAbbv = rs.getString("HR_NAMEABBV");
                    final String attendno = rs.getString("ATTENDNO");
                    final String name = rs.getString("NAME");
                    final String nameKana = rs.getString("NAME_KANA");
                    final Student student = new Student(year, semester, schregno, grade, hrClass, hrName, hrAbbv, attendno, name, nameKana);
                    rtnList.add(student);
                }
            }
        } finally {
            DbUtils.close(rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getStudentInfo(final String schregNo) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T3.HR_NAME, ");
        stb.append("     T3.HR_NAMEABBV, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T2.NAME_KANA ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("          AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("          AND T3.GRADE = T1.GRADE ");
        stb.append("          AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.SCHREGNO = '" + schregNo + "' ");

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
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class Student {
        final String _year;
        final String _semester;
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrAbbv;
        final String _attendno;
        final String _name;
        final String _nameKana;

        public Student(
                final String year,
                final String semester,
                final String schregno,
                final String grade,
                final String hrClass,
                final String hrName,
                final String hrAbbv,
                final String attendno,
                final String name,
                final String nameKana
        ) {
            _year = year;
            _semester = semester;
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrAbbv = hrAbbv;
            _attendno = attendno;
            _name = name;
            _nameKana = nameKana;
       }
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _semesterName;
        final String _ctrlDate;
        final String _mainSearch;
        final String _panelCnt;
        final String[] _schregNos;
        final String _prgId;
        final String _typeDiv;
        final List _panelList = new ArrayList();
        final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _semesterName = getSemeSterName(db2);
            _ctrlDate = request.getParameter("CTRL_DATE");
            _mainSearch = request.getParameter("MAIN_SEARCH");
            _panelCnt = request.getParameter("PANEL_CNT");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _prgId = request.getParameter("PRGID");
            _typeDiv = request.getParameter("TYPE_DIV");

            for (int i = 1; i <= Integer.parseInt(_panelCnt); i++) {
                final String chairSubclass = request.getParameter("SUBCLASS" + i);
                if (null != chairSubclass && !chairSubclass.equals("")) {
                    final String subclass = chairSubclass.substring(chairSubclass.indexOf('-') + 1);
                    final String searchs = request.getParameter("SEARCH_S" + i);
                    final String report = request.getParameter("REPORT" + i);
                    final String reports = request.getParameter("REPORT_S" + i);
                    final String schooling = request.getParameter("SCHOOLING" + i);
                    final String schoolings = request.getParameter("SCHOOLING_S" + i);
                    final String score = request.getParameter("SCORE" + i);
                    final String scores = request.getParameter("SCORE_S" + i);
                    final String hyoutei = request.getParameter("HYOUTEI" + i);
                    final String hyouteis = request.getParameter("HYOUTEI_S" + i);
                    final PanelData panelData = new PanelData(db2,
                                                              subclass,
                                                              searchs,
                                                              report,
                                                              reports,
                                                              schooling,
                                                              schoolings,
                                                              score,
                                                              scores,
                                                              hyoutei,
                                                              hyouteis,
                                                              _useCurriculumcd);
                    _panelList.add(panelData);
                }
            }

            _schregNos = null != request.getParameterValues("category_selected") ? request.getParameterValues("category_selected") : request.getParameterValues("category_name");
        }

        private String getSemeSterName(final DB2UDB db2) throws SQLException {
            String ret = "";
            final String sql = "SELECT * FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "'";
            ResultSet rs = null;
            try {
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    ret = rs.getString("SEMESTERNAME");
                }
            } finally {
                DbUtils.close(rs);
                db2.commit();
            }
            return ret;
        }

    }

    private class PanelData {
        final String _subclass;
        String _subclassName;
        final String _searchs;
        final String _report;
        final String _reports;
        final String _schooling;
        final String _schoolings;
        final String _score;
        final String _scores;
        final String _hyoutei;
        final String _hyouteis;

        public PanelData(
                final DB2UDB db2,
                final String subclass,
                final String searchs,
                final String report,
                final String reports,
                final String schooling,
                final String schoolings,
                final String score,
                final String scores,
                final String hyoutei,
                final String hyouteis,
                final String useCurriculumcd
        ) throws SQLException {
            _subclass = subclass;
            String sql = "SELECT * FROM SUBCLASS_MST WHERE SUBCLASSCD = '" + _subclass + "'";
            if ("1".equals(useCurriculumcd)) {
                sql = "SELECT * FROM SUBCLASS_MST WHERE CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '" + _subclass + "'";
            }
            ResultSet rs = null;
            try {
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    _subclassName = rs.getString("SUBCLASSNAME");
                }
            } finally {
                DbUtils.close(rs);
                db2.commit();
            }
            _searchs = searchs;
            _report = report;
            _reports = reports;
            _schooling = schooling;
            _schoolings = schoolings;
            _score = score;
            _scores = scores;
            _hyoutei = hyoutei;
            _hyouteis = hyouteis;
        }

    }
}

// eof
