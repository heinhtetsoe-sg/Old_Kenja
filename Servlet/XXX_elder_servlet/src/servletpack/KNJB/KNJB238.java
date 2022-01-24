package servletpack.KNJB;

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

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;


/**
 * クラス別希望状況一覧
 */
public class KNJB238 {

    private static final Log log = LogFactory.getLog(KNJB238.class);
    private boolean _hasData;
    private Param _param;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        DB2UDB db2 = null;
        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

        } catch (final Exception e) {
            log.error("Exception:", e);
            return;
        }

        _param = createParam(db2, request);

        Vrw32alp svf = null;
        try {
            svf = new Vrw32alp();

            if (svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            for (int i = 0; i < _param._categorySelected.length; i++) {

                // 生徒データを取得
                final List kouzaList = createKouzaInfoData(db2, _param._categorySelected[i]);
                if (printMain(svf, kouzaList)) { // 生徒出力のメソッド
                    _hasData = true;
                }
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            final int ret = svf.VrQuit();
            log.info("===> VrQuit():" + ret);
            if (null != _param._attendPs) {
            	DbUtils.closeQuietly(_param._attendPs);
            }
            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }
    }

    /**
     * 生徒の出力
     */
    private boolean printMain(final Vrw32alp svf, final List kouzaList) throws Exception {
        boolean hasData = false;
        for (final Iterator it = kouzaList.iterator(); it.hasNext();) {
            final Kouza kouza = (Kouza) it.next();
            if (kouza._studentList.size() > 0) {
                if (printStudent(svf, kouza)) {
                    hasData = true;
                }
            }
        }
        return  hasData;
    }

    private boolean printStudent(final Vrw32alp svf, final Kouza kouza) {
        boolean hasData = false;
        svf.VrSetForm("KNJB238.frm", 4);

        svf.VrsOut("TITLE", "講座別　出欠状況集計一覧");
        svf.VrsOut("NENDO", _param._year + "年度");
        svf.VrsOut("PRINT_DATE", _param._ctrlDate == null ? null : _param._ctrlDate.replace('-', '/'));
        svf.VrsOut("CHAIRCD", kouza._chaircd);
        svf.VrsOut("CHAIR_NAME", kouza._chairname);
        svf.VrsOut("PERIOD", _param.getDate(_param._dateFrom) + " \uFF5E " + _param.getDate(_param._dateTo));
        String staffM = "";
        String staffS = "";
        String seqM = "";
        String seqS = "";
        for (final Iterator it = kouza._staffList.iterator(); it.hasNext();) {
            final Staff staff = (Staff) it.next();
            if ("1".equals(staff._chargediv)) {
                staffM += seqM + staff._staffname;
                seqM = ",";
            } else {
                staffS += seqS + staff._staffname;
                seqS = ",";
            }
        }
        final String fieldLenM = (30 < getMS932ByteLength(staffM)) ? "_2" : "";
        final String fieldLenS = (30 < getMS932ByteLength(staffS)) ? "_2" : "";
        svf.VrsOut("STAFF_NAME1" + fieldLenM, staffM);
        svf.VrsOut("STAFF_NAME2"  + fieldLenS, staffS);
        final int kensuu = kouza._studentList.size();
        svf.VrsOut("ATTEND_NUM", "受講者数：　" + String.valueOf(kensuu) + "名");

        int no = 0;
        int creditsMax = 0;
        int lessonMax = 0;
        for (final Iterator it = kouza._studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            no++;
            svf.VrsOut("NO", String.valueOf(no));
            svf.VrsOut("SCHREG_NO", student._schregno);
            svf.VrsOut("NAME" + ((30 < getMS932ByteLength(student._name)) ? "2" : "1"), student._name);
            svf.VrsOut("SEX", student._sexname);
            final String attendno = (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)) : "";
            svf.VrsOut("ATTEND_NO", student._hrNameAbbv + "-" + attendno);

            final int credits = (NumberUtils.isDigits(student._credits)) ? Integer.parseInt(student._credits) : 0;
            if (creditsMax < credits) creditsMax = credits;
            svf.VrsOut("CREDIT", (0 < creditsMax) ? String.valueOf(creditsMax) : "");

            for (final Iterator itAttend = student._attendList.iterator(); itAttend.hasNext();) {
                final Attend attend = (Attend) itAttend.next();
                final int lesson = (NumberUtils.isDigits(attend._lesson)) ? Integer.parseInt(attend._lesson) : 0;
                log.debug("lesson="+lesson);
                if (lessonMax < lesson) lessonMax = lesson;
                svf.VrsOut("KEKKA", attend._sick2);
                svf.VrsOut("KETUJI", attend._sick1);
                svf.VrsOut("LATE", attend._late);
                svf.VrsOut("EARLY", attend._early);
                svf.VrsOut("KOUKETU", attend._absent);
                final int suspend = (NumberUtils.isDigits(attend._suspend)) ? Integer.parseInt(attend._suspend) : 0;
                final int mourning = (NumberUtils.isDigits(attend._mourning)) ? Integer.parseInt(attend._mourning) : 0;
                svf.VrsOut("SUSPEND", String.valueOf(suspend + mourning));
            }
            svf.VrsOut("EXEC_TIME", (0 < lessonMax) ? String.valueOf(lessonMax) : "");

            svf.VrEndRecord();
            hasData = true;
        }

        return hasData;
    }

    private static int getMS932ByteLength(final String str) {
        int len = 0;
        if (null != str) {
            try {
                len = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return len;
    }

    private List createKouzaInfoData(final DB2UDB db2, final String chaircd) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtnList = new ArrayList();
        try {
            final String sql = getKouzaInfoSql(chaircd);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Kouza kouzaInfo = new Kouza(
                        rs.getString("SUBCLASSCD"),
                        rs.getString("SUBCLASSNAME"),
                        rs.getString("CHAIRCD"),
                        rs.getString("CHAIRNAME"),
                        rs.getString("TAKESEMES_NAME")
                        );
                rtnList.add(kouzaInfo);
                kouzaInfo._staffList = kouzaInfo.createStaffInfoData(db2, kouzaInfo._chaircd);
                kouzaInfo._studentList = kouzaInfo.createStudentInfoData(db2, kouzaInfo._chaircd);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getKouzaInfoSql(final String chaircd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.CHAIRNAME, ");
        stb.append("     T1.TAKESEMES, ");
        stb.append("     CASE WHEN T1.TAKESEMES = '0' THEN '通年' ELSE L1.SEMESTERNAME END AS TAKESEMES_NAME, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append(" T1.SUBCLASSCD AS SUBCLASSCD, ");
        }
        stb.append("     T2.SUBCLASSNAME ");
        stb.append(" FROM ");
        stb.append("     CHAIR_DAT T1 ");
        stb.append("     INNER JOIN SUBCLASS_MST T2 ");
        stb.append("             ON  T2.SUBCLASSCD = T1.SUBCLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("     LEFT JOIN SEMESTER_MST L1 ON L1.YEAR = T1.YEAR AND L1.SEMESTER = T1.TAKESEMES ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.CHAIRCD = '" + chaircd + "' ");
        return stb.toString();
    }

    /** 講座データクラス */
    private class Kouza {
        final String _subclasscd;
        final String _subclassname;
        final String _chaircd;
        final String _chairname;
        final String _takesemesName;
        List _staffList = new ArrayList();
        List _studentList = new ArrayList();

        Kouza(
                final String subclasscd,
                final String subclassname,
                final String chaircd,
                final String chairname,
                final String takesemesName
        ) {
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _chaircd = chaircd;
            _chairname = chairname;
            _takesemesName = takesemesName;
        }

        private List createStaffInfoData(final DB2UDB db2, final String chaircd) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List rtnList = new ArrayList();
            try {
                final String sql = getStaffInfoSql(chaircd);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Staff staffInfo = new Staff(
                            rs.getString("STAFFCD"),
                            rs.getString("STAFFNAME"),
                            rs.getString("CHARGEDIV")
                            );
                    rtnList.add(staffInfo);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnList;
        }

        /**
         * 生徒データ取得処理
         * @param db2           ＤＢ接続オブジェクト
         * @return              帳票出力対象データリスト
         * @throws Exception
         */
        private List<Student> createStudentInfoData(final DB2UDB db2, final String chaircd) throws SQLException {
            final List<Student> rtnList = new ArrayList<Student>();
            final String sql = getStudentInfoSql(chaircd);
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                final Student studentInfo = new Student(
                        KnjDbUtils.getString(row, "SCHREGNO"),
                        KnjDbUtils.getString(row, "NAME"),
                        KnjDbUtils.getString(row, "SEX"),
                        KnjDbUtils.getString(row, "SEX_NAME"),
                        KnjDbUtils.getString(row, "HR_NAME"),
                        KnjDbUtils.getString(row, "HR_NAMEABBV"),
                        KnjDbUtils.getString(row, "ATTENDNO"),
                        KnjDbUtils.getString(row, "CREDITS")
                );
                rtnList.add(studentInfo);
                studentInfo._attendList = studentInfo.createAttendInfoData(db2, studentInfo._schregno);
            }
            return rtnList;
        }
    }

    private String getStaffInfoSql(final String chaircd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.STAFFCD, ");
        stb.append("     T1.CHARGEDIV, ");
        stb.append("     T2.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     CHAIR_STF_DAT T1 ");
        stb.append("     INNER JOIN STAFF_MST T2 ON T2.STAFFCD = T1.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.CHAIRCD = '" + chaircd + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T2.STAFFCD ");
        return stb.toString();
    }

    /** 講座職員データクラス */
    private class Staff {
        final String _staffcd;
        final String _staffname;
        final String _chargediv;

        Staff(
                final String staffcd,
                final String staffname,
                final String chargediv
        ) {
            _staffcd = staffcd;
            _staffname = staffname;
            _chargediv = chargediv;
        }
    }

    private String getStudentInfoSql(final String chaircd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T3.NAME, ");
        stb.append("     T3.SEX, ");
        stb.append("     N1.ABBV1 AS SEX_NAME, ");
        stb.append("     T5.HR_NAME, ");
        stb.append("     T5.HR_NAMEABBV, ");
        stb.append("     T4.ATTENDNO, ");
        stb.append("     L1.CREDITS ");
        stb.append(" FROM ");
        stb.append("     CHAIR_STD_DAT T1 ");
        stb.append("     INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = T3.SEX ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T4 ");
        stb.append("             ON  T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("             AND T4.YEAR = T1.YEAR ");
        stb.append("             AND T4.SEMESTER = T1.SEMESTER ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T5 ");
        stb.append("             ON  T5.YEAR = T4.YEAR ");
        stb.append("             AND T5.SEMESTER = T4.SEMESTER ");
        stb.append("             AND T5.GRADE = T4.GRADE ");
        stb.append("             AND T5.HR_CLASS = T4.HR_CLASS ");
        stb.append("     LEFT JOIN CREDIT_MST L1  ");
        stb.append("             ON  L1.YEAR = T4.YEAR ");
        stb.append("             AND L1.GRADE = T4.GRADE ");
        stb.append("             AND L1.COURSECD = T4.COURSECD ");
        stb.append("             AND L1.MAJORCD = T4.MAJORCD ");
        stb.append("             AND L1.COURSECODE = T4.COURSECODE ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         AND L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD = '" + _param._subclasscd + "' ");
        } else {
            stb.append("         AND L1.SUBCLASSCD = '" + _param._subclasscd + "' ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.CHAIRCD = '" + chaircd + "' ");
        stb.append("     AND (( ");
        stb.append("     CASE WHEN DATE('" + _param._dateFrom + "') BETWEEN T2.SDATE AND T2.EDATE ");
        stb.append("          THEN DATE('" + _param._dateFrom + "') ");
        stb.append("          ELSE T2.SDATE ");
        stb.append("     END ");
        stb.append("     BETWEEN T1.APPDATE AND T1.APPENDDATE) OR ( ");
        stb.append("     CASE WHEN DATE('" + _param._dateTo + "') BETWEEN T2.SDATE AND T2.EDATE ");
        stb.append("          THEN DATE('" + _param._dateTo + "') ");
        stb.append("          ELSE T2.EDATE ");
        stb.append("     END ");
        stb.append("     BETWEEN T1.APPDATE AND T1.APPENDDATE)) ");
        stb.append(" GROUP BY ");
        stb.append("     T4.GRADE, ");
        stb.append("     T4.HR_CLASS, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T3.NAME, ");
        stb.append("     T3.SEX, ");
        stb.append("     N1.ABBV1, ");
        stb.append("     T5.HR_NAME, ");
        stb.append("     T5.HR_NAMEABBV, ");
        stb.append("     T4.ATTENDNO, ");
        stb.append("     L1.CREDITS ");
        stb.append(" ORDER BY ");
        stb.append("     T4.GRADE, ");
        stb.append("     T4.HR_CLASS, ");
        stb.append("     T4.ATTENDNO ");
        return stb.toString();
    }

    /** 生徒データクラス */
    private class Student {
        final String _schregno;
        final String _name;
        final String _sex;
        final String _sexname;
        final String _hrName;
        final String _hrNameAbbv;
        final String _attendno;
        final String _credits;
        List _attendList = new ArrayList();

        Student(
                final String schregno,
                final String name,
                final String sex,
                final String sexname,
                final String hrName,
                final String hrNameAbbv,
                final String attendno,
                final String credits
        ) {
            _schregno = schregno;
            _name = name;
            _sex = sex;
            _sexname = sexname;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
            _attendno = attendno;
            _credits = credits;
        }

        private List createAttendInfoData(final DB2UDB db2, final String schregno) throws SQLException {
            final List rtnList = new ArrayList();
            try {
            	if (null == _param._attendPs) {
            		final String sql = AttendAccumulate.getAttendSubclassSql(
            				((Boolean) _param._hasuuMap.get("semesFlg")).booleanValue(),
            				_param._definecode,
            				_param._knjSchoolMst,
            				_param._year,
            				_param.SSEMESTER,
            				_param._semester,
            				(String) _param._hasuuMap.get("attendSemesInState"),
            				_param._periodInState,
            				(String) _param._hasuuMap.get("befDayFrom"),
            				(String) _param._hasuuMap.get("befDayTo"),
            				(String) _param._hasuuMap.get("aftDayFrom"),
            				(String) _param._hasuuMap.get("aftDayTo"),
            				null,
            				null,
            				"?",
            				_param._attendParamMap
            				);
            		_param._attendPs = db2.prepareStatement(sql);
            	}
                for (final Map<String, String> row : KnjDbUtils.query(db2, _param._attendPs, new Object[] {schregno})) {
                    if (!"9".equals(KnjDbUtils.getString(row, "SEMESTER")) || !_param._subclasscd.equals(KnjDbUtils.getString(row, "SUBCLASSCD"))) {
                        continue;
                    }
                    final Attend attendInfo = new Attend(
                            KnjDbUtils.getString(row, "LESSON"),
                            KnjDbUtils.getString(row, "SICK2"),
                            KnjDbUtils.getString(row, "SICK1"),
                            KnjDbUtils.getString(row, "LATE"),
                            KnjDbUtils.getString(row, "EARLY"),
                            KnjDbUtils.getString(row, "ABSENT"),
                            KnjDbUtils.getString(row, "SUSPEND"),
                            KnjDbUtils.getString(row, "MOURNING")
                    );
                    rtnList.add(attendInfo);
                }
            } catch (Exception e) {
            	log.error("exception!", e);
            }
            return rtnList;
        }
    }

    /** 出欠クラス */
    private class Attend {
        final String _lesson;
        final String _sick2;
        final String _sick1;
        final String _late;
        final String _early;
        final String _absent;
        final String _suspend;
        final String _mourning;

        Attend(
                final String lesson,
                final String sick2,
                final String sick1,
                final String late,
                final String early,
                final String absent,
                final String suspend,
                final String mourning
        ) {
            _lesson = lesson;
            _sick2 = sick2;
            _sick1 = sick1;
            _late = late;
            _early = early;
            _absent = absent;
            _suspend = suspend;
            _mourning = mourning;
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) {
        log.fatal("$Revision: 71426 $ $Date: 2019-12-24 21:36:44 +0900 (火, 24 12 2019) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(db2, request);
    }

    private static class Param {

        final String _year;
        final String _semester;
        final String _subclasscd;
        final String[] _categorySelected; //学年・組
        final String _dateFrom;
        final String _dateTo;
        final String _ctrlDate;
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;

        final String SSEMESTER = "1";
        private KNJSchoolMst _knjSchoolMst;
        private KNJDefineCode _definecode;
        private String _periodInState;
        private Map _attendSemesMap;
        private Map _hasuuMap;
        private PreparedStatement _attendPs;
        private final Map _attendParamMap;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year      = request.getParameter("CTRL_YEAR");
            _semester  = request.getParameter("SEMESTER");
            _subclasscd  = request.getParameter("SUBCLASSCD");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _dateFrom = request.getParameter("DATE_FROM") == null ? null : request.getParameter("DATE_FROM").replace('/', '-');
            _dateTo = request.getParameter("DATE_TO") == null ? null : request.getParameter("DATE_TO").replace('/', '-');
            _ctrlDate = request.getParameter("CTRL_DATE");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
                _definecode = setClasscode0(db2);
                final String z010Name1 = setZ010Name1(db2);
                _periodInState = AttendAccumulate.getPeiodValue(db2, _definecode, _year, SSEMESTER, _semester);
                _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, _year);
                _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap, _dateFrom, _dateTo);
                //log.debug(_attendSemesMap);
            } catch (Exception e) {
                log.debug("loadAttendSemesArgument exception", e);
            }
            
    		_attendParamMap = new HashMap();
    		_attendParamMap.put("HttpServletRequest", request);
    		_attendParamMap.put("DB2UDB", db2);
        }

        private String getDate(final String date) {
            if (date == null) {
                return null;
            }
            return KNJ_EditDate.h_format_S(date, "yyyy") + "年" + KNJ_EditDate.setDateFormat2(KNJ_EditDate.h_format_JP_MD(date));
        }

        private KNJDefineCode setClasscode0(final DB2UDB db2) {
            KNJDefineCode definecode = new KNJDefineCode();
            try {
                definecode.defineCode(db2, _year);         //各学校における定数等設定
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!", ex);
            }
            return definecode;
        }

        private String setZ010Name1(DB2UDB db2) {
            String name1 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    name1 = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
        }
    }

}// クラスの括り
