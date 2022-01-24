/*
 * $Id: eb4b646488c1621bcdade510076dea191eba69f8 $
 *
 * 作成日: 2018/10/31
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

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

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJE154 {

    private static final Log log = LogFactory.getLog(KNJE154.class);

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
        final Map classSchregMap = getSchregInfo(_param, db2); //クラス別生徒リスト: Map(hr_class, Map(schregno, student))
        if (_param._schregcnt == 0) {
        	return;
        }
        int idx = 0;

        //final Map spActList → _spActMap //特別活動リスト
        //評定、及び特別活動成績は、student内_scoreMap,_spActScoreMapに格納。

        //ページ単位(クラス別)に列単位(科目別)に行(生徒)を処理していく
        for (final Iterator it = classSchregMap.keySet().iterator(); it.hasNext();) {
            String hrClass = (String)it.next();
            Map studentMap = (Map)classSchregMap.get(hrClass);
            svf.VrSetForm("KNJE154.frm", 4);
            setTitle(_param, db2, svf, hrClass);

            //左端の番号、生徒の名称等のredord以外の箇所をを出力
            int stucntwk = 1;
            for (final Iterator its = studentMap.keySet().iterator(); its.hasNext();) {
                String schregno = (String)its.next();
                Student stuInfo = (Student)studentMap.get(schregno);
                svf.VrsOutn("NUMBER", stucntwk, stuInfo._attendNo);
                svf.VrsOut("NAME"+stucntwk, stuInfo._name);
                stucntwk++;
            }

            Map subclassMap = getSubclassInfo(_param, db2, idx++); //科目リスト
            if (subclassMap.size() == 0) {
                return;
            }

            //1列毎(科目毎)に処理
            for (final Iterator its = subclassMap.keySet().iterator(); its.hasNext();) {
                String subclassCd = (String)its.next();
                SubclassData scwk = (SubclassData)subclassMap.get(subclassCd);
                //科目名称
                svf.VrsOut("CLASS_NAME", scwk._subclassName);
                //生徒単位に評定を出力する
                int scorecnt = 1;
                for (final Iterator itstu = studentMap.keySet().iterator(); itstu.hasNext();) {
                    String schregno = (String)itstu.next();
                    Student studentwk = (Student)studentMap.get(schregno);
                    if (!studentwk._scoreMap.containsKey(scwk._subclassCd)) {
                        scorecnt++;
                        continue;
                    }
                    ScoreInfo si = (ScoreInfo)studentwk._scoreMap.get(scwk._subclassCd);
                    svf.VrsOut("SCORE"+scorecnt, si._valuation);
                    scorecnt++;
                }
                svf.VrEndRecord();
            }

            //特別活動を1列毎に処理
            int spcnt = 1;
            for (final Iterator its = _param._spActMap.keySet().iterator(); its.hasNext();) {
                String ncd2 = (String)its.next();
                String spActName = (String)_param._spActMap.get(ncd2);
                svf.VrsOut("ITEM"+spcnt, spActName);

                //生徒単位に評定を出力する
                stucntwk = 1;
                for (final Iterator itstu = studentMap.keySet().iterator(); itstu.hasNext();) {
                    String schregno = (String)itstu.next();
                    Student studentwk = (Student)studentMap.get(schregno);
                    if (!studentwk._spActScoreMap.containsKey(ncd2)) {
                        stucntwk++;
                        continue;
                    }
                    String rec = (String)studentwk._spActScoreMap.get(ncd2);
                    svf.VrsOut("SP_ACT"+ spcnt + "_" + stucntwk, rec);
                }
                stucntwk++;
                spcnt++;
                svf.VrEndRecord();
            }
            svf.VrEndPage();
            _hasData = true;
        }

    }

    private static void setTitle(final Param param, final DB2UDB db2, final Vrw32alp svf, final String hrclass) {
        final String lessonCnt = getTotalLessonDay(param, db2, hrclass); //年間授業日数

        svf.VrsOut("lesson", "年間授業日数 " + lessonCnt + " 日");
        svf.VrsOut("year2", KNJ_EditDate.gengou(db2, Integer.parseInt(param._ctrlYear)) + "年度");
        svf.VrsOut("TITLE", "成績一覧表");
        final HrInfo hrclassInfo = (HrInfo) param._hrClassNameMap.get(hrclass);
        svf.VrsOut("HR_NAME",  hrclassInfo._hrClassName);
        svf.VrsOut("teacher", hrclassInfo._teacherName);
    }

    private static String getTotalLessonDay(final Param param, final DB2UDB db2, final String hrclass) {
        String retStr = "";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getTotalLessonDaySql(param, hrclass);
            log.debug("totallesson sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                retStr = rs.getString("LESSON");
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retStr;
    }

    private static String getTotalLessonDaySql(final Param param, String hrclass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" select ");
        stb.append("  T1.GRADE, ");
        stb.append("  SUM(CASE WHEN T2.LESSON IS NOT NULL THEN T2.LESSON ELSE T1.LESSON END) AS LESSON ");
        stb.append(" from ");
        stb.append("  ATTEND_LESSON_MST T1 ");
        stb.append("  LEFT JOIN ATTEND_SEMES_LESSON_DAT T2 ");
        stb.append("    ON T2.YEAR = T1.YEAR ");
        stb.append("   AND T2.MONTH = T1.MONTH ");
        stb.append("   AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("   AND T2.GRADE = T1.GRADE ");
        stb.append("   AND T2.GRADE || T2.HR_CLASS = '" + hrclass + "' ");
        stb.append(" WHERE ");
        stb.append("  T1.YEAR = '" + param._ctrlYear + "' ");
        stb.append("  AND T1.GRADE = '" + param._grade + "' ");
        stb.append("  AND T1.COURSECD = '0' "); //固定
        stb.append("  AND T1.MAJORCD = '000' "); //固定
        stb.append(" GROUP BY ");
        stb.append("  T1.GRADE ");
        return stb.toString();
    }

    private static Map getSchregInfo(final Param param, final DB2UDB db2) {
        final Map retMap = new TreeMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSchregInfoSql(param);
            log.debug(" schreg sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            param._schregcnt = 0;
            while (rs.next()) {
                Student addwk = new Student(rs.getString("SCHREGNO"), rs.getString("ATTENDNO"),rs.getString("NAME"));
                final Map addMap;
                if (retMap.containsKey(rs.getString("HR_CLASS"))) {
                    addMap = (Map)retMap.get(rs.getString("HR_CLASS"));
                } else {
                    addMap = new TreeMap();
                }
                addMap.put(rs.getString("SCHREGNO"), addwk);
                retMap.put(rs.getString("HR_CLASS"), addMap);
                param._schregcnt++;
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        getSchregScore(param, db2, retMap);
        getSchregSpActScore(param, db2, retMap);
        return retMap;
    }

    private static String getSchregInfoSql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     (REGD.GRADE || REGD.HR_CLASS) AS HR_CLASS, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
        stb.append("     AND REGD.GRADE || REGD.HR_CLASS IN " + param._categorySelectedIn + " ");
        stb.append(" ORDER BY ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     REGD.SCHREGNO ");

        return stb.toString();
    }

    private static Map getSubclassInfo(final Param param, final DB2UDB db2, final int idx) {
        final Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSubclassInfoSql(param,idx);
            log.debug(" subclass sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String subclassName = rs.getString("SUBCLASSNAME");
                final String subclassAbbv = rs.getString("SUBCLASSABBV");
                final String classCd;
                final String className;
                final String classAbbv;
                if ("1".equals(param._useCurriculumcd)) {
                    classCd = rs.getString("CLASSCD");
                    className = rs.getString("CLASSNAME");
                    classAbbv = rs.getString("CLASSABBV");
                } else {
                    classCd = "";
                    className = "";
                    classAbbv = "";
                }
                final boolean mojiHyouka = "1".equals(rs.getString("ELECTDIV")) ? true : false;
                SubclassData addwk = new SubclassData(subclassCd, StringUtils.defaultString(subclassName, ""),
                		                               StringUtils.defaultString(subclassAbbv, ""), classCd,
                		                               StringUtils.defaultString(className, ""), StringUtils.defaultString(classAbbv, ""),
                		                               mojiHyouka);
                retMap.put(subclassCd, addwk);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private static String getSubclassInfoSql(final Param param, final int idx) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append("     T1.SUBCLASSCD, ");
        }
        stb.append("     L2.SUBCLASSNAME, ");
        stb.append("     L2.SUBCLASSABBV, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD, ");
            stb.append("     L3.CLASSNAME, ");
            stb.append("     L3.CLASSABBV, ");
        }
        stb.append("     L3.ELECTDIV ");
        stb.append(" FROM ");
        stb.append("     SCHREG_STUDYREC_DAT T1 ");
        stb.append("     LEFT JOIN SUBCLASS_MST L2 ON L2.SUBCLASSCD = T1.SUBCLASSCD ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("          AND L2.CLASSCD = T1.CLASSCD ");
            stb.append("          AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("          AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("     LEFT JOIN CLASS_MST L3 ON L3.CLASSCD = T1.CLASSCD ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("          AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ");
        }
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.SEMESTER = '" + param._semester + "' ");
        stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
        //画面で指定している物なので、useprgチェック不要。
        stb.append("     AND T1.SCHOOL_KIND   = '" + param._schoolKind + "' ");
        stb.append("     AND EXISTS (SELECT 'X' ");
        stb.append("                 FROM SCHREG_REGD_GDAT T4 ");
        stb.append("                 WHERE ");
        stb.append("      T1.YEAR = T4.YEAR AND ");
        stb.append("      T4.SCHOOL_KIND = T1.SCHOOL_KIND AND ");
        stb.append("      T4.GRADE = '" + param._grade + "' ");
        stb.append("     ) ");
        stb.append("     AND T2.GRADE || T2.HR_CLASS = '" + param._categorySelected[idx] + "' ");
        stb.append(" GROUP BY ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("   T1.CLASSCD , T1.SCHOOL_KIND , T1.CURRICULUM_CD , T1.SUBCLASSCD, ");
        } else {
            stb.append("     T1.SUBCLASSCD, ");
        }
        stb.append("     L2.SUBCLASSNAME, ");
        stb.append("     L2.SUBCLASSABBV, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("     L3.CLASSNAME, ");
            stb.append("     L3.CLASSABBV, ");
        }
        stb.append(" L3.ELECTDIV, ");
        stb.append(" L3.SHOWORDER4 ");
        stb.append(" ORDER BY ");
        stb.append("     VALUE(L3.ELECTDIV, '0'), ");
        stb.append("     L3.SHOWORDER4, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
        }
        stb.append("     T1.SUBCLASSCD ");

        return stb.toString();
    }

    private static Map getSchregSpActScore(final Param param, final DB2UDB db2, Map schregMap) {
        final Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSchregSpActScoreSql(param);
            log.debug(" spact sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                if (!schregMap.containsKey(rs.getString("HR_CLASS"))) {
                    continue;
                }
                Map findMap = (Map)schregMap.get(rs.getString("HR_CLASS"));
                if (!findMap.containsKey(rs.getString("SCHREGNO"))) {
                    continue;
                }
                Student student = (Student)findMap.get(rs.getString("SCHREGNO"));
                student._spActScoreMap.put(rs.getString("CODE"), rs.getString("RECORD"));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private static String getSchregSpActScoreSql(final Param param) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     (T2.GRADE || T2.HR_CLASS) AS HR_CLASS, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.CODE, ");
        stb.append("     T1.RECORD ");
        stb.append(" FROM ");
        stb.append("     BEHAVIOR_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ");
        stb.append("       ON T2.YEAR = T1.YEAR ");
        stb.append("      AND T2.SEMESTER = '" + param._semester + "' ");
        stb.append("      AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("      AND T2.ANNUAL = T1.ANNUAL ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + param._ctrlYear + "'  ");
        stb.append("     AND T1.DIV = '4' ");

        return stb.toString();
    }

    private static void getSchregScore(final Param param, final DB2UDB db2, Map schregMap) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSchregScoreSql(param);
            log.debug(" score sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                if (!schregMap.containsKey(rs.getString("HR_CLASS"))) {
                    continue;
                }
                Map findMap = (Map)schregMap.get(rs.getString("HR_CLASS"));
                if (!findMap.containsKey(rs.getString("SCHREGNO"))) {
                    continue;
                }
                Student student = (Student)findMap.get(rs.getString("SCHREGNO"));
                ScoreInfo addwk = new ScoreInfo(rs.getString("CLASSCD"), rs.getString("SUBCLASSCD"),rs.getString("VALUATION"));
                student._scoreMap.put(rs.getString("SUBCLASSCD"), addwk);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private static String getSchregScoreSql(final Param param) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("   T2.GRADE || T2.HR_CLASS AS HR_CLASS, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.CLASSCD, ");
        stb.append("   (T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD )AS SUBCLASSCD, ");
        stb.append("   SUM(T1.VALUATION) AS VALUATION ");
        stb.append(" FROM ");
        stb.append("   SCHREG_STUDYREC_DAT T1 ");
        stb.append("   INNER JOIN SCHREG_REGD_DAT T2 ");
        stb.append("      ON T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.SEMESTER = '" + param._semester + "' ");
        stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + param._ctrlYear + "' ");
        stb.append("   AND T1.SCHOOL_KIND = '" + param._schoolKind + "' ");
        stb.append("   AND T2.GRADE || T2.HR_CLASS IN " + param._categorySelectedIn + " ");
        stb.append(" GROUP BY ");
        stb.append("   T2.GRADE, T2.HR_CLASS, ");
        stb.append("   T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, ");
        stb.append("   T1.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("   T2.HR_CLASS, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.CLASSCD, ");
        stb.append("   T1.CURRICULUM_CD, ");
        stb.append("   T1.SUBCLASSCD ");

        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65504 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private static class Student {
        final String _schregNo;
        final String _attendNo;
        final String _name;
        Map _scoreMap;
        Map _spActScoreMap;
        public Student(
                final String schregNo,
                final String attendNo,
                final String name
                ) {
                _schregNo = schregNo;
                _attendNo = attendNo;
                _name = name;
                _spActScoreMap = new TreeMap();
                _scoreMap = new TreeMap();
        }
    }

    private static class HrInfo {
        private final String _hrClass;
        private final String _hrClassName;
        private final String _teacherName;
        HrInfo(final String hrClass, final String hrClassName, final String teacherName) {
            _hrClass = hrClass;
            _hrClassName = hrClassName;
            _teacherName = teacherName;
        }
    }
    private static class Semester {
        private final String _semester;
        private final String _name;
        private final String _sDate;
        private final String _eDate;

        public Semester(
                final String code,
                final String name,
                final String sDate,
                final String eDate
        ) {
            _semester = code;
            _name = name;
            _sDate = sDate;
            _eDate = eDate;
        }

        public String toString() {
            return _semester + "/" + _name + "/" + _sDate + "/" + _eDate;
        }
    }

    private static class SubclassData {
        final String _subclassCd;
        final String _subclassName;
        final String _subclassAbbv;
        final String _classCd;
        final String _className;
        final String _classAbbv;
        final boolean _mojiHyouka;
        //final List _viewList = new ArrayList();

        SubclassData (
                final String subclassCd,
                final String subclassName,
                final String subclassAbbv,
                final String classCd,
                final String className,
                final String classAbbv,
                final boolean mojiHyouka
        ) {
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _subclassAbbv = subclassAbbv;
            _classCd = classCd;
            _className = className;
            _classAbbv = classAbbv;
            _mojiHyouka = mojiHyouka;
        }


        public String toString() {
            return "SubclassData(" + _subclassCd + ")";
        }
    }

    private static class ScoreInfo {
        private final String _classCd;
        private final String _subclassCd;
        private final String _valuation;
        ScoreInfo(final String classCd, final String subclassCd, final String valuation) {
            _classCd = classCd;
            _subclassCd = subclassCd;
            _valuation = valuation;
        }
    }

    /** パラメータクラス */
    private class Param {
        private final String _ctrlYear;
        private final String[] _categorySelected;
        private final String _categorySelectedIn;
        private final String _semester;
        private final String _grade;
        private final String _useCurriculumcd;
        private final String _schoolKind;

        private final String _gradeCd;
        private Map _semesterMap = new HashMap();
        private Map _hrClassNameMap;
        private Map _spActMap;
        private long _schregcnt;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _useCurriculumcd = request.getParameter("useCurriculumcd");

            _categorySelected = request.getParameterValues("CLASS_SELECTED");
            _categorySelectedIn = getCategorySelectedIn(_categorySelected);

            loadSemester(db2, _ctrlYear);
            _gradeCd = String.valueOf(Integer.parseInt(getSchregRegdGdat(db2, "GRADE_CD")));
            _hrClassNameMap = getHRClassName(db2);
            _schoolKind = getSchregRegdGdat(db2, "SCHOOL_KIND");
            _spActMap = setD034NameMap(db2);
            _schregcnt = 0;
        }

        private String getCategorySelectedIn(final String[] categorySelected) {
            StringBuffer stb = new StringBuffer();
            stb.append("(");
            for (int i = 0; i < categorySelected.length; i++) {
                if (0 < i) stb.append(",");
                stb.append("'" + categorySelected[i] + "'");
            }
            stb.append(")");
            return stb.toString();
        }

        //semesterMapを設定する
        private String loadSemester(final DB2UDB db2, String year) {
            String retstr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List list = new ArrayList();
            try {
                ps = db2.prepareStatement(sqlSemester(year));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semesterCd = rs.getString("SEMESTER");
                    final String name = rs.getString("SEMESTERNAME");
                    final String sDate = rs.getString("SDATE");
                    final String eDate = rs.getString("EDATE");
                    final Semester semester = new Semester(semesterCd, name, sDate, eDate);
                    _semesterMap.put(semesterCd, semester);
                    if (!"9".equals(semesterCd)) {
                        retstr = semesterCd;
                    }

                    list.add(sDate);
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return retstr;
        }

        private String sqlSemester(String year) {
            final String sql;
            sql = "select"
                + "   * "
                + " from"
                + "   SEMESTER_MST"
                + " where"
                + "   YEAR= '" + year + "'"
                + " order by SEMESTER"
            ;
            return sql;
        }
        private String getSchregRegdGdat(final DB2UDB db2, final String field) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "SELECT " + field + " FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _ctrlYear + "' AND GRADE = '" + _grade + "' ";
                log.debug(" gdat sql = "+ sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (SQLException ex) {
                log.error("SCHREG_REGD_GDAT exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
        private Map getHRClassName(final DB2UDB db2) {
            Map rtnMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "SELECT T1.GRADE || T1.HR_CLASS AS HR_CLASS, HR_NAME, T2.STAFFNAME FROM SCHREG_REGD_HDAT T1 "
                             + "LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.TR_CD1 "
                             + " WHERE T1.YEAR = '" + _ctrlYear + "' "
                             + " AND T1.SEMESTER = '" + _semester + "' "
                             + " AND T1.GRADE || T1.HR_CLASS IN " + _categorySelectedIn + " ";
                log.debug("hrclassname sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while(rs.next()) {
                    HrInfo addwk = new HrInfo(rs.getString("HR_CLASS"), rs.getString("HR_NAME"), rs.getString("STAFFNAME"));
                    rtnMap.put(rs.getString("HR_CLASS"), addwk);
                }
            } catch (SQLException ex) {
                log.error("SCHREG_REGD_GDAT exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnMap;
        }
        private Map setD034NameMap(DB2UDB db2) {
            Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE NAMECD1 = 'D034' AND YEAR = '" + _ctrlYear + "' ";
                log.debug("D034 sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retMap.put(rs.getString("NAMECD2"), rs.getString("NAME1"));
                }
            } catch (SQLException ex) {
                log.error("getD034 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

    }
}

// eof
