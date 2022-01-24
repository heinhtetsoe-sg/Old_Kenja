/*
 * $Id: ec4525317f66ad4fb760fa60e82207e233576fc8 $
 *
 * 作成日: 2014/02/01
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJC;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJC035E {

    private static final Log log = LogFactory.getLog(KNJC035E.class);

    private boolean _hasData;

    private Param _param;

    private static final String KEY_CD = "CD";
    private static final String KEY_ITEM = "ITEM";

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
        final int maxCol = 13;
        final List list = Attend.getAttendList(db2, _param);

        svf.VrSetForm("KNJC035E.frm", 4);
        
        svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate));
        svf.VrsOut("SEMESTER", _param._semesterName);

        svf.VrsOut("SUBCLASS", _param._subclasscd + ":" + StringUtils.defaultString(_param._subclassname));
        svf.VrsOut("CHAIR", _param._chaircd + ":" +  StringUtils.defaultString(_param._chairname));

        for (int line = 1; line <= list.size(); line++) {
            final Attend att = (Attend) list.get(line - 1);
            svf.VrsOutn("ATTENDNO", line, att._hrAttendno);
            if ("1".equals(_param._use_SchregNo_hyoji)) {
                svf.VrsOutn("SCHREGNO", line, att._schregno);
                svf.VrsOutn("NAME2", line, att._name);
            } else  {
                svf.VrsOutn("NAME", line, att._name);
            }
        }

        int col = 0;
        printSvfRecord(svf, "締め日", Attend.SHIMEBI, list);
        svf.VrEndRecord();
        col += 1;
        printSvfRecord(svf, "授業時数", Attend.LESSON, list);
        svf.VrEndRecord();
        col += 1;
        for (final Iterator it = Attend.getAttendNameList().iterator(); it.hasNext();) {
            final Map map = (Map) it.next();
            final String item = (String) map.get(KEY_ITEM);
            final String cd = (String) map.get(KEY_CD);
            if (!_param._nameMst.keySet().contains(cd) && !Attend.MLESSON.equals(cd)) { // 名称マスタ年度データに登録されている勤怠のみ表示
                continue;
            }
            printSvfRecord(svf, item, cd, list);
            svf.VrEndRecord();
            col += 1;
        }
        
        for (int i = 0, last = maxCol - (col % maxCol); i < last; i++) {
            svf.VrsOut("GRP" + ((i == last - 1) ? "2" : "1"), "G");
            if (i == last / 2) {
                svf.VrsOut("ITEM2_" + ((i == last - 1) ? "2" : "1"), "備考");
            }
            svf.VrEndRecord();
        }
        _hasData = true;
    }
    
    private void printSvfRecord(final Vrw32alp svf, final String item0, final String cd, final List list) {
        final String item;
//        if (Attend.C001_2.equals(cd)) {
//            final String repl = null != _param._nameMst.get(Attend.C001_25) ? "法止" : "";
//            item = StringUtils.replace(item0, "%s", repl);
        if (-1 != item0.indexOf("%")) {
            final String repl = StringUtils.defaultString((String) _param._nameMst.get(cd));
            item = StringUtils.replace(item0, "%s", repl);
        } else {
            item = item0;
        }
        svf.VrsOut("ITEM1" + (null != item && item.length() > 5 ? "_2" : "_1"), item);
        final String field = "APPOINTED_DAY";
        for (int line = 1; line <= list.size(); line++) {
            final Attend att = (Attend) list.get(line - 1);
            svf.VrsOutn(field, line, getAttendZeroHyoji(att.getValue(cd)));
        }
    }

    //プロパティ「use_Attend_zero_hyoji」= '1'のとき、データの通りにゼロ、NULLを表示
    //それ以外のとき、ゼロは表示しない
    private String getAttendZeroHyoji(final String val) {
        if ("1".equals(_param._use_Attend_zero_hyoji)) return val;
        if ("0".equals(val) || "0.0".equals(val)) return "";
        return val;
    }

    private static class Attend {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _hrAttendno;
        final String _name;
        final String _semester;
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _appointedDay;
        final String _lesson;
        final String _mlesson;
        final String _offdays;
        final String _absent;
        final String _suspend;
        final String _mourning;
        final String _abroad;
        final String _sick;
        final String _notice;
        final String _nonotice;
        final String _nurseoff;
        final String _late;
        final String _early;
        final String _virus;
        final String _koudome;

        Attend(
            final String schregno,
            final String grade,
            final String hrClass,
            final String attendno,
            final String hrAttendno,
            final String name,
            final String month,
            final String semester,
            final String classcd,
            final String schoolKind,
            final String curriculumCd,
            final String subclasscd,
            final String appointedDay,
            final String lesson,
            final String mlesson,
            final String offdays,
            final String absent,
            final String suspend,
            final String mourning,
            final String abroad,
            final String sick,
            final String notice,
            final String nonotice,
            final String nurseoff,
            final String late,
            final String early,
            final String virus,
            final String koudome
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendno = attendno;
            _hrAttendno = hrAttendno;
            _name = name;
            _semester = semester;
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _appointedDay = appointedDay;
            _lesson = lesson;
            _mlesson = mlesson;
            _offdays = offdays;
            _absent = absent;
            _suspend = suspend;
            _mourning = mourning;
            _abroad = abroad;
            _sick = sick;
            _notice = notice;
            _nonotice = nonotice;
            _nurseoff = nurseoff;
            _late = late;
            _early = early;
            _virus = virus;
            _koudome = koudome;
        }

        public static List getAttendList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO_SHOW");
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String attendno = rs.getString("ATTENDNO");
                    final String hrAttendno = rs.getString("HR_ATTENDNO");
                    final String name = rs.getString("NAME");
                    final String month = rs.getString("MONTH");
                    final String semester = rs.getString("SEMESTER");
                    final String classcd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String appointedDay = rs.getString("APPOINTED_DAY");
                    final String lesson = rs.getString("LESSON");
                    final String mlesson = rs.getString("MLESSON");
                    final String offdays = rs.getString("OFFDAYS");
                    final String absent = rs.getString("ABSENT");
                    final String suspend = rs.getString("SUSPEND");
                    final String mourning = rs.getString("MOURNING");
                    final String abroad = rs.getString("ABROAD");
                    final String sick = rs.getString("SICK");
                    final String notice = rs.getString("NOTICE");
                    final String nonotice = rs.getString("NONOTICE");
                    final String nurseoff = rs.getString("NURSEOFF");
                    final String late = rs.getString("LATE");
                    final String early = rs.getString("EARLY");
                    final String virus = "true".equals(param._useVirus) ? rs.getString("VIRUS") : null;
                    final String koudome = "true".equals(param._useKoudome) ? rs.getString("KOUDOME") : null;
                    final Attend attend = new Attend(schregno, grade, hrClass, attendno, hrAttendno, name, month, semester, classcd, schoolKind, curriculumCd, subclasscd, appointedDay, lesson, mlesson, offdays, absent, suspend, mourning, abroad, sick, notice, nonotice, nurseoff, late, early, virus, koudome);
                    list.add(attend);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCH_INFO AS ( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T1.ATTENDNO, ");
            stb.append("         T3.HR_NAMEABBV || '-' || T1.ATTENDNO AS HR_ATTENDNO, ");
            stb.append("         T2.NAME ");
            stb.append("     FROM ");
            stb.append("         SCHREG_REGD_DAT T1, ");
            stb.append("         SCHREG_BASE_MST T2, ");
            stb.append("         SCHREG_REGD_HDAT T3, ");
            stb.append("         CHAIR_STD_DAT T4 ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR     = T3.YEAR AND ");
            stb.append("         T1.YEAR     = T4.YEAR AND ");
            stb.append("         T1.YEAR     = '" + param._ctrlYear + "' AND ");
            stb.append("         T1.SEMESTER = T3.SEMESTER AND ");
            stb.append("         T1.SEMESTER = T4.SEMESTER AND ");
            stb.append("         T1.SEMESTER = '" + param._semester + "' AND ");
            stb.append("         T1.SCHREGNO = T2.SCHREGNO AND ");
            stb.append("         T1.SCHREGNO = T4.SCHREGNO AND ");
            stb.append("         T1.GRADE    = T3.GRADE AND ");
            stb.append("         T1.HR_CLASS = T3.HR_CLASS AND ");
            stb.append("         T4.CHAIRCD  = '" + param._chaircd + "' ");
            if (null != param._month) {
                stb.append("         AND MONTH(T4.APPDATE) <= " + param._imonth + " + CASE WHEN " + param._imonth + " < 4 THEN 12 ELSE 0 END AND ");
                stb.append("         " + param._imonth + " <= MONTH(T4.APPENDDATE) + CASE WHEN MONTH(T4.APPENDDATE) < 4 THEN 12 ELSE 0 END ");
                stb.append("         AND CASE WHEN " + param._imonth + " < 4 THEN " + param._imonth + "+12 ELSE " + param._imonth + " END ");
                stb.append("         BETWEEN ");
                stb.append("             CASE WHEN MONTH(T4.APPDATE) < 4 THEN MONTH(T4.APPDATE)+12 ELSE MONTH(T4.APPDATE) END ");
                stb.append("         AND ");
                stb.append("             CASE WHEN MONTH(T4.APPENDDATE) < 4 THEN MONTH(T4.APPENDDATE)+12 ELSE MONTH(T4.APPENDDATE) END ");
            }
            stb.append(" ) ");

            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO AS SCHREGNO_SHOW, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T1.HR_ATTENDNO, ");
            stb.append("     T1.NAME, ");
            stb.append("     VALUE(T2.LESSON,0) ");
            stb.append("         - VALUE(T2.ABROAD, 0)");
            if (!"1".equals(param._knjSchoolMst._subOffDays)) {
                stb.append("         - VALUE(T2.OFFDAYS, 0) ");
            }
            if (!"1".equals(param._knjSchoolMst._subSuspend)) {
                stb.append("         - VALUE(T2.SUSPEND, 0) ");
            }
            if (!"1".equals(param._knjSchoolMst._subMourning)) {
                stb.append("         - VALUE(T2.MOURNING, 0) ");
            }
            if ("true".equals(param._useVirus) && !"1".equals(param._knjSchoolMst._subVirus)) {
                stb.append("         - VALUE(T2.VIRUS, 0) ");
            }
            if ("true".equals(param._useKoudome) && !"1".equals(param._knjSchoolMst._subKoudome)) {
                stb.append("         - VALUE(T2.KOUDOME, 0) ");
            }
            stb.append("     AS MLESSON, ");
            stb.append("     T2.* ");
            stb.append(" FROM ");
            stb.append("     SCH_INFO T1 ");
            stb.append("     LEFT JOIN ATTEND_SUBCLASS_DAT T2 ON T1.SCHREGNO     = T2.SCHREGNO AND ");
            stb.append("         T2.YEAR         = '" + param._ctrlYear + "' AND  ");
            stb.append("         T2.SEMESTER     = '" + param._semester + "' AND ");
            stb.append("         T2.MONTH        = '" + param._month + "' AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' ||  T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD   = '" + param._subclasscd + "' ");
            } else {
                stb.append("     T2.CLASSCD      = '" + param._subclasscd.substring(0, 2) + "' AND ");
                stb.append("     T2.SUBCLASSCD   = '" + param._subclasscd + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T1.ATTENDNO, ");
            stb.append("         T1.SCHREGNO ");

            return stb.toString();
        }
        
        private static final String SHIMEBI = "SHIMEBI";
        private static final String LESSON = "LESSON";
        private static final String A004_2 = "A004_2";
        private static final String A004_1 = "A004_1";
        private static final String C001_1 = "C001_1";
        private static final String C001_2 = "C001_2";
        private static final String C001_25 = "C001_25";
        private static final String C001_19 = "C001_19";
        private static final String C001_3 = "C001_3";
        private static final String MLESSON = "MLESSON";
        private static final String C001_4 = "C001_4";
        private static final String C001_5 = "C001_5";
        private static final String C001_6 = "C001_6";
        private static final String C001_14 = "C001_14";
        private static final String C001_15 = "C001_15";
        private static final String C001_16 = "C001_16";

        private Map _attendMap = null;
        
        public String getValue(final String cd) {
            if (null == _attendMap) {
                final Map m = new HashMap();
                m.put(SHIMEBI, _appointedDay);
                m.put(LESSON, _lesson);
                m.put(A004_2, _offdays);
                m.put(A004_1, _abroad);
                m.put(C001_1, _absent);
                m.put(C001_2, _suspend);
                m.put(C001_25, _koudome);
                m.put(C001_19, _virus);
                m.put(C001_3, _mourning);
                m.put(MLESSON, _mlesson);
                m.put(C001_4, _sick);
                m.put(C001_5, _notice);
                m.put(C001_6, _nonotice);
                m.put(C001_14, _nurseoff);
                m.put(C001_15, _late);
                m.put(C001_16, _early);
                _attendMap = m;
            }
            return (String) _attendMap.get(cd);
        }

        private static List getAttendNameList() {
            final List rtn = new ArrayList();
            rtn.add(getMap(new String[]{KEY_CD, A004_2,  KEY_ITEM, "休学時数"}));
            rtn.add(getMap(new String[]{KEY_CD, A004_1,  KEY_ITEM, "留学時数"}));
            rtn.add(getMap(new String[]{KEY_CD, C001_1,  KEY_ITEM, "公欠時数"}));
            rtn.add(getMap(new String[]{KEY_CD, C001_2,  KEY_ITEM, "%s"}));
            rtn.add(getMap(new String[]{KEY_CD, C001_25, KEY_ITEM, "%s"}));
            rtn.add(getMap(new String[]{KEY_CD, C001_19, KEY_ITEM, "%s"}));
            rtn.add(getMap(new String[]{KEY_CD, C001_3,  KEY_ITEM, "忌引時数"}));
            rtn.add(getMap(new String[]{KEY_CD, MLESSON,  KEY_ITEM, "出席すべき時数"}));
            rtn.add(getMap(new String[]{KEY_CD, C001_4,  KEY_ITEM, "病欠"}));
            rtn.add(getMap(new String[]{KEY_CD, C001_5,  KEY_ITEM, "事故欠"}));
            rtn.add(getMap(new String[]{KEY_CD, C001_6,  KEY_ITEM, "欠席"}));
            rtn.add(getMap(new String[]{KEY_CD, C001_14, KEY_ITEM, "保健室欠課"}));
            rtn.add(getMap(new String[]{KEY_CD, C001_15, KEY_ITEM, "遅刻"}));
            rtn.add(getMap(new String[]{KEY_CD, C001_16, KEY_ITEM, "早退"}));
            return rtn;
        }
        
        private static Map getMap(final String[] array) {
            final Map rtn = new HashMap();
            for (int i = 0; i < array.length; i+= 2) {
                rtn.put(array[i], array[i + 1]);
            }
            return rtn;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 59907 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _semester;
        final String _subclasscd;
        final String _subclassname;
        final String _chaircd;
        final String _chairname;
        final String _monthcd;
        final String _semesterName;
        final String _month;
        final Integer _imonth;
        final String _appointedDay;
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;
        final String _use_Attend_zero_hyoji;
        final String _use_SchregNo_hyoji;
        final Map _nameMst;
        final String _useSchool_KindField;
        final String _SCHOOLCD;
        final String _SCHOOLKIND;
        private KNJSchoolMst _knjSchoolMst;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _subclasscd = StringUtils.defaultString(request.getParameter("SUBCLASSCD"));
            _chaircd = StringUtils.defaultString(request.getParameter("CHAIRCD"));
            _monthcd = request.getParameter("MONTHCD");
            if (null != _monthcd && StringUtils.split(_monthcd, "-").length > 1) {
                _semester = StringUtils.split(_monthcd, "-")[1];
                _month = StringUtils.split(_monthcd, "-")[0];
                _semesterName = getSemestername(db2, _month, _semester);
                _imonth = Integer.valueOf(_month);
            } else {
                _semester = null;
                _month = null;
                _semesterName = null;
                _imonth = null;
            }
            _appointedDay = request.getParameter("APPOINTED_DAY");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _subclassname = getSubclassname(db2);
            _chairname = getChairName(db2);
            _nameMst = new HashMap();
            _nameMst.putAll(getNameMst(db2, "A004"));
            _nameMst.putAll(getNameMst(db2, "C001"));
            _use_Attend_zero_hyoji = request.getParameter("use_Attend_zero_hyoji");
            _use_SchregNo_hyoji = request.getParameter("use_SchregNo_hyoji");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD = request.getParameter("SCHOOLCD");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _knjSchoolMst = new KNJSchoolMst(db2, _ctrlYear, setParamMap());
        }

        private Map setParamMap() {
            final Map retMap = new HashMap();
            if ("1".equals(_useSchool_KindField)) {
                retMap.put("SCHOOLCD", _SCHOOLCD);
                retMap.put("SCHOOL_KIND", _SCHOOLKIND);
            }
            return retMap;
        }


        /**
         * @param db2
         * @param subclassCd
         * @return
         */
        private String getSubclassname(final DB2UDB db2) throws SQLException {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("     SUBCLASSNAME ");
            sql.append(" FROM ");
            sql.append("     SUBCLASS_MST ");
            sql.append(" WHERE ");
            if ("1".equals(_useCurriculumcd)) {
                sql.append("     CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' || ");
            }
            sql.append("     SUBCLASSCD   = '" + _subclasscd + "' ");
            
            String rtn = "";
            PreparedStatement ps = db2.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                rtn = rs.getString("SUBCLASSNAME");
            }
            DbUtils.closeQuietly(null, ps, rs);
            return rtn;
        }

        /**
         * @param db2
         * @param chairCd
         * @return
         */
        private String getChairName(final DB2UDB db2) throws SQLException {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("     CHAIRNAME ");
            sql.append(" FROM ");
            sql.append("     CHAIR_DAT ");
            sql.append(" WHERE ");
            sql.append("     YEAR = '" + _ctrlYear + "' ");
            sql.append("     AND SEMESTER = '" + _ctrlSemester + "' ");
            sql.append("     AND CHAIRCD = '" + _chaircd + "' ");
            
            String rtn = "";
            PreparedStatement ps = db2.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                rtn = rs.getString("CHAIRNAME");
            }
            DbUtils.closeQuietly(null, ps, rs);
            return rtn;
        }

        private String getSemestername(final DB2UDB db2, final String month, final String semester) throws SQLException {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("     T1.NAMECD2, T1.NAME1, T1.NAMESPARE1, T2.SEMESTERNAME ");
            sql.append(" FROM ");
            sql.append("     NAME_MST T1, SEMESTER_MST T2");
            sql.append(" WHERE ");
            sql.append("     T1.NAMECD1 = 'Z005' ");
            sql.append("     AND T1.NAMECD2 = '" + month + "' ");
            sql.append("     AND T2.YEAR  = '" + _ctrlYear + "' ");
            sql.append("     AND T2.SEMESTER  = '" + semester + "' ");
            sql.append(" ORDER BY ");
            sql.append("     NAMESPARE1 ");
            
            String rtn = null;
            PreparedStatement ps = db2.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                rtn = StringUtils.defaultString(rs.getString("SEMESTERNAME")) + " " + StringUtils.defaultString(rs.getString("NAME1"));
            }
            DbUtils.closeQuietly(null, ps, rs);
            return rtn;
        }

        private Map getNameMst(final DB2UDB db2, final String namecd1) throws SQLException {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("     T1.NAMECD1 || '_' || T1.NAMECD2 AS CD, T1.NAME1 ");
            sql.append(" FROM ");
            sql.append("     V_NAME_MST T1 ");
            sql.append(" WHERE ");
            sql.append("     T1.YEAR = '" + _ctrlYear + "' ");
            sql.append("     AND T1.NAMECD1 = '" + namecd1 + "' ");
            sql.append(" ORDER BY ");
            sql.append("     NAMESPARE1, NAMECD2 ");
            
            final Map rtn = new HashMap();
            PreparedStatement ps = db2.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rtn.put(rs.getString(KEY_CD), rs.getString("NAME1"));
            }
            DbUtils.closeQuietly(null, ps, rs);
            return rtn;
        }
    }
}

// eof
