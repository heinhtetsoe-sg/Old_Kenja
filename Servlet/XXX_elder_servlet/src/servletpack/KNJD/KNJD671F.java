/*
 * $Id: 335775ba884ab067eee0bfdef266218c5abea9d9 $
 *
 * 作成日: 2015/12/22
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJD671F {

    private static final Log log = LogFactory.getLog(KNJD671F.class);

    private boolean _hasData;
    private static final String ALL3KA = "333333";
    private static final String ALL5KA = "555555";
    private static final String ALL9KA = "999999";
    private static String arraykansuuji[] = {"〇","一","二","三","四","五","六","七","八","九","十"};

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
        for (final Iterator subit = _param._subclassList.iterator(); subit.hasNext();) {
            final Subclass subclass = (Subclass) subit.next();

            if (_param._isYokoForm) {

                final List list = getList(db2, subclass);
                final List pageList = getPageList(list, 15);
                if (pageList.size() == 0) {
                    continue;
                }
                final List page1List = (List) pageList.get(0);
                final int countPerPage = Math.min(15, Math.max(10, page1List.size()));
                final String fieldDiv = String.valueOf(countPerPage - 9); // 10: "1", 11: "2", 12: "3", ... 15: "6"
                
                for (int i = 0; i < pageList.size(); i++) {
                    final List studentList = (List) pageList.get(i);

                    svf.VrSetForm("KNJD671F_3.frm", 4);
                    if (!_param._isMusashinohigashi) {
                        String setTitle = _param._gradeName;
                        setTitle += _param._testName + "テスト";
                        svf.VrsOut("TITLE", setTitle);
                    }
                    
                    svf.VrsOut("FOOTER", StringUtils.defaultString(_param._gradeName) + " " + StringUtils.defaultString(subclass._subclassname));

                    for (int j = 0; j < studentList.size(); j++) {
                        final PrintStuedent printStuedent = (PrintStuedent) studentList.get(j);
                        
                        svf.VrsOut("RANK" + fieldDiv, StringUtils.defaultString(printStuedent._majorRank) + "位");
                        svf.VrsOut("HR" + fieldDiv, printStuedent._hrClassName1);
                        
                        final int nameKeta = getMS932ByteLength(printStuedent._name);
                        final String ketaField;
                        if (nameKeta > 24) {
                            ketaField = "4";
                        } else if (nameKeta > 16) {
                            ketaField = "3";
                        } else if (nameKeta > 12) {
                            ketaField = "2";
                        } else {
                            ketaField = "1";
                        }
                        svf.VrsOut("NAME" + fieldDiv + "_" + ketaField, printStuedent._name);
                        svf.VrsOut("SCORE" + fieldDiv, printStuedent._score);
                        svf.VrEndRecord();
                        _hasData = true;
                    }
                }
                
            } else {
                
                svf.VrSetForm("KNJD671F_1.frm", 1);
                String setTitle = _param._gradeName;
                //setTitle += _param._semesterName;
                setTitle += _param._testName + "テスト";
                svf.VrsOut("TITLE", setTitle);
                svf.VrsOut("SUB_TITLE", "【" + subclass._classname + "】");
                
                final List list = getList(db2, subclass);
                int lineCnt = 1;
                final int maxLineCnt = 10;
                for (Iterator ItPrint = list.iterator(); ItPrint.hasNext();) {
                    PrintStuedent printStuedent = (PrintStuedent) ItPrint.next();
                    if (maxLineCnt < lineCnt) {
                        svf.VrEndPage();
                        svf.VrSetForm("KNJD671F_2.frm", 1);
                        lineCnt = 1;
                    }
                    final int printLine = maxLineCnt - lineCnt + 1;
                    
                    final String rankKanji = convertKansuuji(printStuedent._majorRank);
                    if (rankKanji.length() > 2) {
                        svf.VrsOutn("RANK1_2", printLine, rankKanji);
                    } else {
                        svf.VrsOutn("RANK1_1", printLine, rankKanji);
                    }
                    
                    if (null != printStuedent._name) {
                        if (printStuedent._name.length() > 16) {
                            svf.VrsOutn("NAME1_4", printLine, printStuedent._name);
                        } else if (printStuedent._name.length() > 11) {
                            svf.VrsOutn("NAME1_3", printLine, printStuedent._name);
                        } else if (printStuedent._name.length() > 8) {
                            svf.VrsOutn("NAME1_2", printLine, printStuedent._name);
                        } else {
                            svf.VrsOutn("NAME1_1", printLine, printStuedent._name);
                        }
                    }
                    
                    final String scoreKanji = convertKansuuji(printStuedent._score);
                    svf.VrsOutn("SCORE1", printLine, scoreKanji);
                    lineCnt++;
                    _hasData = true;
                }
                if (_hasData) {
                    svf.VrEndPage();
                }
            }
        }
    }
    /**
     *  svf print 数字を漢数字へ変換(文字単位)
     */
    private String convertKansuuji(final String suuji) {
        final StringBuffer stb = new StringBuffer();
        for (int i = 0; i < suuji.length(); i++) {
            final String n = suuji.substring(i, i + 1);
            final String cov;
            cov = arraykansuuji[Integer.parseInt(n)];
            stb.append(cov);
        }
        return stb.toString();
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
    	return KNJ_EditEdit.getMS932ByteLength(str);
    }
    
    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private List getList(final DB2UDB db2, final Subclass subclass) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = scoreSql(subclass);
            log.info(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno   = rs.getString("SCHREGNO");
                final String hrName     = rs.getString("HR_NAME");
                final String hrNameabbv = rs.getString("HR_NAMEABBV");
                final String hrClassName1 = rs.getString("HR_CLASS_NAME1");
                final String attendno   = rs.getString("ATTENDNO");
                final String name       = rs.getString("NAME");
                final String majorRank  = rs.getString("MAJOR_RANK");
                final String score      = rs.getString("SCORE");
                final String handi      = rs.getString("HANDI");

                final PrintStuedent printStuedent = new PrintStuedent(schregno, hrName, hrNameabbv, hrClassName1, attendno, name, majorRank, score, handi);
                retList.add(printStuedent);
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String scoreSql(final Subclass subclass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     HDAT.HR_NAME, ");
        stb.append("     HDAT.HR_NAMEABBV, ");
        stb.append("     HDAT.HR_CLASS_NAME1, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     RANK.MAJOR_RANK, ");
        stb.append("     RANK.SCORE, ");
        stb.append("     HANDI.NAME1 AS HANDI ");
        stb.append(" FROM ");
        if ("1".equals(_param._hrClassType)) {
            stb.append("     SCHREG_REGD_HDAT HDAT ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON HDAT.YEAR = REGD.YEAR ");
        } else {
            stb.append("     SCHREG_REGD_FI_HDAT HDAT ");
            stb.append("     LEFT JOIN SCHREG_REGD_FI_DAT REGD ON HDAT.YEAR = REGD.YEAR ");
        }
        stb.append("          AND HDAT.SEMESTER = REGD.SEMESTER ");
        stb.append("          AND HDAT.GRADE = REGD.GRADE ");
        stb.append("          AND HDAT.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        if ("1".equals(_param._hrClassType)) {
            stb.append("     INNER JOIN RECORD_RANK_SDIV_DAT RANK ON HDAT.YEAR = RANK.YEAR ");
        } else {
            stb.append("     INNER JOIN RECORD_RANK_FI_SDIV_DAT RANK ON HDAT.YEAR = RANK.YEAR ");
        }
        stb.append("           AND RANK.SEMESTER = '" + _param._semester + "' ");
        stb.append("           AND RANK.TESTKINDCD || RANK.TESTITEMCD || RANK.SCORE_DIV = '" + _param._testcd + "' ");
        if (!ALL3KA.equals(subclass._subclasscd) && !ALL5KA.equals(subclass._subclasscd) && !ALL9KA.equals(subclass._subclasscd)) {
            stb.append("           AND RANK.CLASSCD || RANK.SCHOOL_KIND || RANK.CURRICULUM_CD || RANK.SUBCLASSCD = '" + subclass._classcd + subclass._schoolKind + subclass._curriculum_cd + subclass._subclasscd + "' ");
        } else {
            stb.append("           AND RANK.SUBCLASSCD = '" + subclass._subclasscd + "' ");
        }
        stb.append("           AND REGD.SCHREGNO = RANK.SCHREGNO ");
        stb.append("           AND RANK.MAJOR_RANK <= " + _param._rank + " ");
        stb.append("     LEFT JOIN NAME_MST HANDI ON HANDI.NAMECD1 = 'A025' ");
        stb.append("          AND BASE.HANDICAP = HANDI.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     HDAT.YEAR = '" + _param._year + "' ");
        stb.append("     AND HDAT.SEMESTER = '" + _param._regdSeme + "' ");
        if (!"1".equals(_param._hrClassType)) {
        	stb.append("     AND HDAT.RECORD_DIV = '1' ");
        }
        stb.append("     AND HDAT.GRADE = '" + _param._grade + "' ");
        stb.append(" ORDER BY ");
        stb.append("     RANK.MAJOR_RANK, ");
        stb.append("     RANK.SCORE, ");
        stb.append("     HDAT.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
    	log.fatal("$Revision: 60776 $");
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _semester;
        private final String _grade;
        private final String _schoolKind;
        private final String _testcd;
        private final String[] _categorySelected;
        private final String _rank;
        private final String _year;
        private final String _ctrlSeme;
        private final String _regdSeme;
        private final String _loginDate;
        private final String _prgid;
        private final String _printLogStaffcd;
        private final String _semesterName;
        private final String _gradeName;
        private final String _testName;
        private final String _knjd671F_FORM_J;
        private final String _knjd671F_FORM_H;
        private final String _hrClassType;
        private final boolean _isYokoForm;
        private final List _subclassList;
        /** 武蔵野東ならtrue */
        final boolean _isMusashinohigashi;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester           = request.getParameter("SEMESTER");
            _grade              = request.getParameter("GRADE");
            _testcd             = request.getParameter("TESTCD");
            _categorySelected   = request.getParameterValues("CATEGORY_SELECTED");
            _rank               = null != request.getParameter("RANK") && !"".equals(request.getParameter("RANK")) ? request.getParameter("RANK") : "9999";
            _year               = request.getParameter("YEAR");
            _ctrlSeme           = request.getParameter("CTRL_SEME");
            _regdSeme           = "9".equals(_semester) ? _ctrlSeme : _semester;
            _loginDate          = request.getParameter("LOGIN_DATE");
            _prgid              = request.getParameter("PRGID");
            _printLogStaffcd    = request.getParameter("PRINT_LOG_STAFFCD");
            _knjd671F_FORM_J    = request.getParameter("knjd671F_FORM_J");
            _knjd671F_FORM_H    = request.getParameter("knjd671F_FORM_H");
            _hrClassType        = request.getParameter("HR_CLASS_TYPE");
            _semesterName = getSemesterName1(db2);
            _gradeName = getGradeName(db2);
            _testName = getTestName(db2);
            _schoolKind = getSchoolKind(db2);
            _subclassList = getSubclassList(db2);
            _isYokoForm = "H".equals(_schoolKind) && "1".equals(_knjd671F_FORM_H) || "J".equals(_schoolKind) && "1".equals(_knjd671F_FORM_J);
            final String z010 = getZ010(db2);
            _isMusashinohigashi = "musashinohigashi".equals(z010);
        }
        
        private String getZ010(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getSchoolKind(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String retstr = "";
            try {
                final String sql = "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    retstr = rs.getString("SCHOOL_KIND");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retstr;
        }

        private String getSemesterName1(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String retstr = "";
            try {
                final String sql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ORDER BY SEMESTER ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    retstr = rs.getString("SEMESTERNAME");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retstr;
        }

        private String getGradeName(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String retstr = "";
            try {
                final String sql = "SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    retstr = rs.getString("GRADE_NAME1");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retstr;
        }

        private String getTestName(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String retstr = "";
            try {
                final String sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testcd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    retstr = rs.getString("TESTITEMNAME");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retstr;
        }

        private List getSubclassList(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            final List retList = new ArrayList();
            try {
                String sql = "";
                for (int j = 0; j < _categorySelected.length; j++) {
                    final String subclassCd = _categorySelected[j].substring(7);
                    if (ALL3KA.equals(subclassCd) || ALL5KA.equals(subclassCd) || ALL9KA.equals(subclassCd)) {
                        final String classcd        = "";
                        final String schoolKind     = "";
                        final String curriculumcd   = "";
                        final String subclasscd     = subclassCd;
                        final String subclassname   = ALL3KA.equals(subclassCd) ? "3科合計" : ALL5KA.equals(subclassCd) ? "5科合計" : "総合計";
                        final String subclassabbv   = ALL3KA.equals(subclassCd) ? "3科合計" : ALL5KA.equals(subclassCd) ? "5科合計" : "総合計";
                        final Subclass subclass = new Subclass(classcd, schoolKind, subclassname, curriculumcd, subclasscd, subclassname, subclassabbv);
                        retList.add(subclass);
                    } else {
                        sql = "SELECT T1.*, T2.CLASSNAME FROM SUBCLASS_MST T1 INNER JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND WHERE T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + _categorySelected[j] + "' ";
                        ps = db2.prepareStatement(sql);
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            final String classcd        = rs.getString("CLASSCD");
                            final String schoolKind     = rs.getString("SCHOOL_KIND");
                            final String classname      = rs.getString("CLASSNAME");
                            final String curriculumcd   = rs.getString("CURRICULUM_CD");
                            final String subclasscd     = rs.getString("SUBCLASSCD");
                            final String subclassname   = rs.getString("SUBCLASSNAME");
                            final String subclassabbv   = rs.getString("SUBCLASSABBV");
                            final Subclass subclass = new Subclass(classcd, schoolKind, classname, curriculumcd, subclasscd, subclassname, subclassabbv);
                            retList.add(subclass);
                        }
                    }
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return retList;
        }

    }

    private class Subclass {
        final String _classcd;
        final String _schoolKind;
        final String _classname;
        final String _curriculum_cd;
        final String _subclasscd;
        final String _subclassname;
        final String _subclassabbv;

        public Subclass(
                final String classcd,
                final String schoolKind,
                final String classname,
                final String curriculumcd,
                final String subclasscd,
                final String subclassname,
                final String subclassabbv
        ) {
            _classcd        = classcd;
            _schoolKind     = schoolKind;
            _classname      = classname;
            _curriculum_cd  = curriculumcd;
            _subclasscd     = subclasscd;
            _subclassname   = subclassname;
            _subclassabbv   = subclassabbv;
        }
    }

    private class PrintStuedent {
        final String _schregno;
        final String _hrName;
        final String _hrNameabbv;
        final String _hrClassName1;
        final String _attendno;
        final String _name;
        final String _majorRank;
        final String _score;
        final String _handi;

        public PrintStuedent(
                final String schregno,
                final String hrName,
                final String hrNameabbv,
                final String hrClassName1,
                final String attendno,
                final String name,
                final String majorRank,
                final String score,
                final String handi
        ) {
            _schregno   = schregno;
            _hrName     = hrName;
            _hrNameabbv = hrNameabbv;
            _hrClassName1 = hrClassName1;
            _attendno   = attendno;
            _name       = name;
            _majorRank  = majorRank;
            _score      = score;
            _handi      = handi;
        }
    }
}

// eof

