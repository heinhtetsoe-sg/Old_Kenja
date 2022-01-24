/*
 * $Id: 74dfdbeb9d82dcee1d40be4ba45908cb1747a6bd $
 *
 * 作成日: 2015/12/22
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJD670F {

    private static final Log log = LogFactory.getLog(KNJD670F.class);

    private boolean _hasData;
    private static final String ALL3KA = "333333";
    private static final String ALL5KA = "555555";
    private static final String ALL9KA = "999999";

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
        for (final Iterator itsub = _param._subclassList.iterator(); itsub.hasNext();) {
            final Subclass subclass = (Subclass) itsub.next();

            final List stdalllist0 = PrintStudent.getList(db2, _param, subclass);
            final Map chairnameMap = new HashMap();
            final List stdgrplist = new ArrayList();
            if ("1".equals(_param._chairPage)) {
                final Map chaircdStudentListMap = new TreeMap();
                for (final Iterator it = stdalllist0.iterator(); it.hasNext();) {
                    final PrintStudent s = (PrintStudent) it.next();
                    if (null != s._chaircd) {
                        getMappedList(chaircdStudentListMap, s._chaircd).add(s);
                        chairnameMap.put(s._chaircd, s._chairname);
                    }
                }
                stdgrplist.addAll(new ArrayList(chaircdStudentListMap.values()));
            } else {
                stdgrplist.add(stdalllist0);
            }
            for (final Iterator it = stdgrplist.iterator(); it.hasNext();) {
                final List stdalllist = (List) it.next();
                
                if ("J".equals(_param._schoolKind)) {
                    svf.VrSetForm("KNJD670F_1.frm", 1);
                    String setTitle = _param._isSeireki ? _param._year + "年度" : KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度";
                    setTitle += _param._gradeName;
                    setTitle += _param._testName;
                    setTitle += subclass._subclassname;
                    svf.VrsOut("TITLE", setTitle);
                } else {
                    svf.VrSetForm("KNJD670F_2.frm", 1);
                    svf.VrsOut("NENDO", _param._isSeireki ? _param._year + "年度" : KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度");
                    svf.VrsOut("GRADE", _param._gradeName);
                    if ("1".equals(_param._chairPage)) {
                        svf.VrsOut("TITLE", "講座別学年順位表");
                    } else {
                        svf.VrsOut("TITLE", "科目別学年順位表");
                    }
                    svf.VrsOut("SUBCLASS_NAME", subclass._subclassname);
                    if ("1".equals(_param._chairPage) && stdalllist.size() > 0) {
                        svf.VrsOut("TEST_NAME", ((PrintStudent) stdalllist.get(0))._chairname);
                    }
                }
                if ("H".equals(_param._schoolKind)) {
                    final List kenjouScoreList = new ArrayList();
                    final List allScoreList = new ArrayList();
                    for (final Iterator ItPrint = stdalllist.iterator(); ItPrint.hasNext();) {
                        final PrintStudent s = (PrintStudent) ItPrint.next();
                        if (NumberUtils.isDigits(s._score)) {
                            allScoreList.add(new BigDecimal(s._score));
                            if ("001".equals(s._handicap)) {
                                kenjouScoreList.add(new BigDecimal(s._score));
                            }
                        }
                    }
                    svf.VrsOut("AVERAGE2_1", avgString(kenjouScoreList));
                    svf.VrsOut("AVERAGE2_2", avgString(allScoreList));
//              } else {
//                svf.VrsOut("AVERAGE2_1", subclass._kenjouAvg);
//                svf.VrsOut("AVERAGE2_2", subclass._allAvg);
                }
                svf.VrsOut("DATE", _param._isSeireki ? KNJ_EditDate.h_format_SeirekiJP(_param._loginDate) : KNJ_EditDate.h_format_JP(db2, _param._loginDate));
                final int maxLineCnt = "J".equals(_param._schoolKind) ? 40 : 50;
                final List retuAllList = getPageList(stdalllist, maxLineCnt);
                final List pageList = getPageList(retuAllList, 2);
                
                for (int pi = 0; pi < pageList.size(); pi++) {
                    final List retuList = (List) pageList.get(pi);
                    
                    for (int retuCntIdx = 0; retuCntIdx < retuList.size(); retuCntIdx++) {
                        final int retuCnt = retuCntIdx + 1;
                        final List stdlist = (List) retuList.get(retuCntIdx);
                        for (int lineCntIdx = 0; lineCntIdx < stdlist.size(); lineCntIdx++) {
                            final PrintStudent printStudent = (PrintStudent) stdlist.get(lineCntIdx);
                            
                            final int lineCnt = lineCntIdx + 1;
                            if ("1".equals(_param._chairPage)) {
                                svf.VrsOutn("RANK" + retuCnt, lineCnt, printStudent._chairGradeRank);
                            } else {
                                svf.VrsOutn("RANK" + retuCnt, lineCnt, printStudent._gradeRank);
                            }
                            svf.VrsOutn("HR_NAME" + retuCnt, lineCnt, printStudent._hrNameabbv);
                            svf.VrsOutn("NO" + retuCnt, lineCnt, printStudent._attendno);
                            svf.VrsOutn("CHAIR_NAME" + retuCnt, lineCnt, printStudent._chairabbv);
                            final int namelen = KNJ_EditEdit.getMS932ByteLength(printStudent._name);
                            if (namelen > 30) {
                                svf.VrsOutn("NAME" + retuCnt + "_3", lineCnt, printStudent._name);
                            } else if (namelen > 20) {
                                svf.VrsOutn("NAME" + retuCnt + "_2", lineCnt, printStudent._name);
                            } else {
                                svf.VrsOutn("NAME" + retuCnt + "_1", lineCnt, printStudent._name);
                            }
                            
                            svf.VrsOutn("SCORE" + retuCnt, lineCnt, printStudent._score);
                            svf.VrsOutn("BELONG" + retuCnt, lineCnt, printStudent._handi);
                        }
                    }
                    _hasData = true;
                    svf.VrEndPage();
                }
            }
        }
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
    
    private static List getMappedList(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private static String avgString(final List scoreList) {
        if (scoreList.size() == 0) {
            return null;
        }
        BigDecimal sum = new BigDecimal(0);
        for (final Iterator it = scoreList.iterator(); it.hasNext();) {
            final BigDecimal score = (BigDecimal) it.next();
            sum = sum.add(score);
        }
        return sum.divide(new BigDecimal(scoreList.size()), 1, BigDecimal.ROUND_HALF_UP).toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 60776 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _semester;
        final String _grade;
        final String _schoolKind;
        final String _testcd;
        final String[] _categorySelected;
        final String _rank;
        final String _year;
        final String _ctrlSeme;
        final String _regdSeme;
        final String _loginDate;
        final String _prgid;
        final String _printLogStaffcd;
        final String _semesterName;
        final String _gradeName;
        final String _testName;
        final String _chairPage;
        final String _hrClassType;
        final boolean _isSeireki;
        private final List _subclassList;

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
            _chairPage          = request.getParameter("CHAIR_PAGE");
            _hrClassType        = request.getParameter("HR_CLASS_TYPE");
            _semesterName = getSemesterName1(db2);
            _gradeName = getGradeName(db2);
            _testName = getTestName(db2);
            _schoolKind = getSchoolKind(db2);
            _isSeireki = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' ")));
            if ("1".equals(_chairPage)) {
                final String[] subclasscd = {request.getParameter("SUBCLASSCD")};
                _subclassList = getSubclassList(db2, subclasscd);
            } else {
                _subclassList = getSubclassList(db2, _categorySelected);
            }
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
                final String sql = "SELECT VALUE(GRADE_NAME1, '') AS GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";
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
                final String sql = "SELECT VALUE(TESTITEMNAME, '') AS TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testcd + "' ";
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

        private List getSubclassList(final DB2UDB db2, final String[] subclasscdArray) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            final List retList = new ArrayList();
            try {
                String sql = "";
                for (int j = 0; j < subclasscdArray.length; j++) {
                    final String subclassCd = subclasscdArray[j].substring(7);
                    if (ALL3KA.equals(subclassCd) || ALL5KA.equals(subclassCd) || ALL9KA.equals(subclassCd)) {
                        final String classcd        = "";
                        final String schoolKind     = "";
                        final String curriculumcd   = "";
                        final String subclasscd     = subclassCd;
                        final String subclassname   = ALL3KA.equals(subclassCd) ? "3科合計" : ALL5KA.equals(subclassCd) ? "5科合計" : "総合計";
                        final String subclassabbv   = ALL3KA.equals(subclassCd) ? "3科合計" : ALL5KA.equals(subclassCd) ? "5科合計" : "総合計";
                        final Subclass subclass = new Subclass(classcd, schoolKind, curriculumcd, subclasscd, subclassname, subclassabbv);
                        retList.add(subclass);
                    } else {
                        sql = "SELECT * FROM SUBCLASS_MST WHERE CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + subclasscdArray[j] + "' ";
                        ps = db2.prepareStatement(sql);
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            final String classcd        = rs.getString("CLASSCD");
                            final String schoolKind     = rs.getString("SCHOOL_KIND");
                            final String curriculumcd   = rs.getString("CURRICULUM_CD");
                            final String subclasscd     = rs.getString("SUBCLASSCD");
                            final String subclassname   = rs.getString("SUBCLASSNAME");
                            final String subclassabbv   = rs.getString("SUBCLASSABBV");
                            final Subclass subclass = new Subclass(classcd, schoolKind, curriculumcd, subclasscd, subclassname, subclassabbv);
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

    private static class Subclass {
        final String _classcd;
        final String _schoolKind;
        final String _curriculum_cd;
        final String _subclasscd;
        final String _subclassname;
        final String _subclassabbv;

        public Subclass(
                final String classcd,
                final String schoolKind,
                final String curriculumcd,
                final String subclasscd,
                final String subclassname,
                final String subclassabbv
        ) {
            _classcd        = classcd;
            _schoolKind     = schoolKind;
            _curriculum_cd  = curriculumcd;
            _subclasscd     = subclasscd;
            _subclassname   = subclassname;
            _subclassabbv   = subclassabbv;
        }
    }

    private static class PrintStudent {
        final String _schregno;
        final String _hrName;
        final String _hrNameabbv;
        final String _attendno;
        final String _name;
        final String _chairname;
        final String _chairabbv;
        final String _chaircd;
        final String _gradeRank;
        final String _chairGradeRank;
        final String _score;
        final String _handicap;
        final String _handi;

        public PrintStudent(
                final String schregno,
                final String hrName,
                final String hrNameabbv,
                final String attendno,
                final String name,
                final String chairname,
                final String chairabbv,
                final String chaircd,
                final String gradeRank,
                final String chairGradeRank,
                final String score,
                final String handicap,
                final String handi
        ) {
            _schregno   = schregno;
            _hrName     = hrName;
            _hrNameabbv = hrNameabbv;
            _attendno   = attendno;
            _name       = name;
            _chairname  = chairname;
            _chairabbv  = chairabbv;
            _chaircd    = chaircd;
            _gradeRank  = gradeRank;
            _chairGradeRank = chairGradeRank;
            _score      = score;
            _handicap   = handicap;
            _handi      = handi;
        }
        
        private static List getList(final DB2UDB db2, final Param param, final Subclass subclass) {
            final List retList = new ArrayList();
            final Map schregnoStudentMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = scoreSql(param, subclass);
                log.info(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String schregno   = rs.getString("SCHREGNO");
                    if ("J".equals(param._schoolKind) && null != schregnoStudentMap.get(schregno)) {
                    	continue;
                    }
                    final String hrName     = rs.getString("HR_NAME");
                    final String hrNameabbv = rs.getString("HR_NAMEABBV");
                    final String attendno   = rs.getString("ATTENDNO");
                    final String name       = rs.getString("NAME");
                    String chairname = null;
                    String chairabbv = null;
                    String chaircd = null;
                    if ("J".equals(param._schoolKind) || "1".equals(param._chairPage)) {
                        chairname  = rs.getString("CHAIRNAME");
                        chairabbv  = rs.getString("CHAIRABBV");
                        chaircd    = rs.getString("CHAIRCD");
                    }
                    final String gradeRank  = rs.getString("GRADE_RANK");
                    final String score      = rs.getString("SCORE");
                    final String chairGradeRank = rs.getString("CHAIR_GRADE_RANK");
                    final String handicap   = StringUtils.defaultString(rs.getString("HANDICAP"), "001");
                    final String handi      = rs.getString("HANDI");

                    final PrintStudent printStuedent = new PrintStudent(schregno, hrName, hrNameabbv, attendno, name, chairname, chairabbv, chaircd, gradeRank, chairGradeRank, score, handicap, handi);
                    retList.add(printStuedent);
                    schregnoStudentMap.put(schregno, printStuedent);
                }

            } catch (SQLException ex) {
                log.error("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retList;
        }

        private static String scoreSql(final Param param, final Subclass subclass) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH CHAIR_STD AS (");
            stb.append("     SELECT CHAIR.YEAR, CHAIR.SEMESTER, CHAIR.CLASSCD, CHAIR.SCHOOL_KIND, CHAIR.CURRICULUM_CD, CHAIR.SUBCLASSCD, STD.SCHREGNO, CHAIR.CHAIRNAME, CHAIR.CHAIRABBV, CHAIR.CHAIRCD ");
            stb.append("     FROM CHAIR_STD_DAT STD ");
            stb.append("     INNER JOIN CHAIR_DAT CHAIR ON STD.YEAR = CHAIR.YEAR ");
            stb.append("          AND STD.SEMESTER = CHAIR.SEMESTER ");
            stb.append("          AND STD.CHAIRCD = CHAIR.CHAIRCD ");
            stb.append("     WHERE STD.YEAR = '" + param._year + "' ");
            stb.append(" ) ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     HDAT.HR_CLASS, ");
            stb.append("     HDAT.HR_NAME, ");
            stb.append("     HDAT.HR_NAMEABBV, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     BASE.NAME, ");
            if ("J".equals(param._schoolKind) || "1".equals(param._chairPage)) {
                stb.append("     CHAIR.CHAIRNAME, ");
                stb.append("     CHAIR.CHAIRABBV, ");
                stb.append("     CHAIR.CHAIRCD, ");
            }
            stb.append("     RANK.GRADE_RANK, ");
            stb.append("     RANK.SCORE, ");
            if ("1".equals(param._chairPage)) {
                stb.append("     CHAIR_RANK.GRADE_RANK AS CHAIR_GRADE_RANK, ");
            } else {
                stb.append("     CAST(NULL AS INT) AS CHAIR_GRADE_RANK, ");
            }
            stb.append("     BASE.HANDICAP, ");
            stb.append("     HANDI.NAME1 AS HANDI ");
            stb.append(" FROM ");
            if ("1".equals(param._hrClassType)) {
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
            if ("1".equals(param._hrClassType)) {
                stb.append("     INNER JOIN RECORD_RANK_SDIV_DAT RANK ON HDAT.YEAR = RANK.YEAR ");
            } else {
                stb.append("     INNER JOIN RECORD_RANK_FI_SDIV_DAT RANK ON HDAT.YEAR = RANK.YEAR ");
            }
            stb.append("           AND RANK.SEMESTER = '" + param._semester + "' ");
            stb.append("           AND RANK.TESTKINDCD || RANK.TESTITEMCD || RANK.SCORE_DIV = '" + param._testcd + "' ");
            if (ALL3KA.equals(subclass._subclasscd) || ALL5KA.equals(subclass._subclasscd) || ALL9KA.equals(subclass._subclasscd)) {
                stb.append("           AND RANK.SUBCLASSCD = '" + subclass._subclasscd + "' ");
            } else {
                stb.append("           AND RANK.CLASSCD || RANK.SCHOOL_KIND || RANK.CURRICULUM_CD || RANK.SUBCLASSCD = '" + subclass._classcd + subclass._schoolKind + subclass._curriculum_cd + subclass._subclasscd + "' ");
            }
            stb.append("           AND REGD.SCHREGNO = RANK.SCHREGNO ");
            if ("J".equals(param._schoolKind) || "1".equals(param._chairPage)) {
                stb.append("     LEFT JOIN CHAIR_STD CHAIR ON HDAT.YEAR = CHAIR.YEAR ");
                stb.append("          AND HDAT.SEMESTER = CHAIR.SEMESTER ");
                stb.append("          AND REGD.SCHREGNO = CHAIR.SCHREGNO ");
                stb.append("          AND RANK.CLASSCD = CHAIR.CLASSCD ");
                stb.append("          AND RANK.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
                stb.append("          AND RANK.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
                stb.append("          AND RANK.SUBCLASSCD = CHAIR.SUBCLASSCD ");
            }
            if ("1".equals(param._chairPage)) {
                if ("1".equals(param._hrClassType)) {
                    stb.append("     INNER JOIN RECORD_RANK_CHAIR_SDIV_DAT CHAIR_RANK ON HDAT.YEAR = CHAIR_RANK.YEAR ");
                } else {
                    stb.append("     INNER JOIN RECORD_RANK_CHAIR_FI_SDIV_DAT CHAIR_RANK ON HDAT.YEAR = CHAIR_RANK.YEAR ");
                }
                stb.append("           AND CHAIR_RANK.SEMESTER = '" + param._semester + "' ");
                stb.append("           AND CHAIR_RANK.TESTKINDCD || CHAIR_RANK.TESTITEMCD || CHAIR_RANK.SCORE_DIV = '" + param._testcd + "' ");
                if (ALL3KA.equals(subclass._subclasscd) || ALL5KA.equals(subclass._subclasscd) || ALL9KA.equals(subclass._subclasscd)) {
                    stb.append("           AND CHAIR_RANK.SUBCLASSCD = '" + subclass._subclasscd + "' ");
                } else {
                    stb.append("           AND CHAIR_RANK.CLASSCD || CHAIR_RANK.SCHOOL_KIND || CHAIR_RANK.CURRICULUM_CD || CHAIR_RANK.SUBCLASSCD = '" + subclass._classcd + subclass._schoolKind + subclass._curriculum_cd + subclass._subclasscd + "' ");
                }
                stb.append("           AND CHAIR.CHAIRCD = CHAIR_RANK.CHAIRCD ");
                stb.append("           AND REGD.SCHREGNO = CHAIR_RANK.SCHREGNO ");
            }
            stb.append("     LEFT JOIN NAME_MST HANDI ON HANDI.NAMECD1 = 'A025' ");
            stb.append("          AND BASE.HANDICAP = HANDI.NAMECD2 ");
            stb.append(" WHERE ");
            stb.append("     HDAT.YEAR = '" + param._year + "' ");
            stb.append("     AND HDAT.SEMESTER = '" + param._regdSeme + "' ");
//            stb.append("     AND HDAT.RECORD_DIV = '1' ");
            stb.append("     AND HDAT.GRADE = '" + param._grade + "' ");
            if ("1".equals(param._chairPage)) {
                stb.append("           AND CHAIR.CHAIRCD IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
                stb.append("           AND CHAIR_RANK.GRADE_RANK <= " + param._rank + " ");
            } else {
                stb.append("           AND RANK.GRADE_RANK <= " + param._rank + " ");
            }
            stb.append(" ORDER BY ");
            stb.append("     RANK.GRADE_RANK, ");
            stb.append("     RANK.SCORE, ");
            if ("J".equals(param._schoolKind) || "1".equals(param._chairPage)) {
                stb.append("     CHAIR.CHAIRCD, ");
            }
            stb.append("     HDAT.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO ");
            return stb.toString();
        }
    }
}

// eof

