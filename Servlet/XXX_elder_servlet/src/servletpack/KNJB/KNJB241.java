package servletpack.KNJB;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import servletpack.KNJZ.detail.dao.AttendAccumulate;


/**
 * クラス別希望状況一覧
 */
public class KNJB241 {

    private static final Log log = LogFactory.getLog(KNJB241.class);
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
            if (kouza._studentList.size() > 0 && kouza._semeMonthList.size() > 0) {
                if (printStudent(svf, kouza)) {
                    hasData = true;
                }
            }
        }
        return  hasData;
    }
    
    private boolean printStudent(final Vrw32alp svf, final Kouza kouza) {
        boolean hasData = false;

        svf.VrSetForm("KNJB241.frm", 4);
        
        svf.VrsOut("TITLE", _param._semestername + "　出欠集計一覧表");
        svf.VrsOut("PRINT_DATE", _param._ctrlDate == null ? null : _param._ctrlDate.replace('-', '/'));
        svf.VrsOut("CHAIRCD", kouza._chaircd);
        svf.VrsOut("CHAIRNAME", kouza._chairname);
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
        svf.VrsOut("MAIN_STAFF", staffM);
        svf.VrsOut("SUB_STAFF", staffS);
        final int kensuu = kouza._studentList.size();
        svf.VrsOut("SCH_CNT", String.valueOf(kensuu));

        int no = 0;
        for (final Iterator it = kouza._studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            no++;
            svf.VrsOut("NO", String.valueOf(no));
            svf.VrsOut("SCHREGNO", student._schregno);
            svf.VrsOut("NAME" + ((30 < getMS932ByteLength(student._name)) ? "_2" : ""), student._name);
            svf.VrsOut("SEX", student._sexname);
            final String attendno = (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)) : "";
            svf.VrsOut("ATTENDNO", student._hrNameAbbv + "-" + attendno);
            int len = 0;
            final SemeMonth studentTotal = new SemeMonth("TOTAL", "", "計", "0", "0", "0");
            final Set totalMonthSet = new HashSet();
            for (final Iterator it2 = kouza._semeMonthList.iterator(); it2.hasNext();) {
                final SemeMonth semeMonth = (SemeMonth) it2.next();
                len++;
                svf.VrsOut("SEMESTER" + len, semeMonth._semestername);
                svf.VrsOut("MONTH" + len, semeMonth._month + "月");
                svf.VrsOut("MI" + len, semeMonth._miCnt);
                svf.VrsOut("JISSI" + len, semeMonth._jissiCnt);
                svf.VrsOut("KYUKOU" + len, semeMonth._kyukouCnt);
                
                if (!totalMonthSet.contains(semeMonth._semester + semeMonth._month)) {
                    studentTotal._semestername = semeMonth._semestername;
                    studentTotal._miCnt = add(studentTotal._miCnt, semeMonth._miCnt);
                    studentTotal._jissiCnt = add(studentTotal._jissiCnt, semeMonth._jissiCnt);
                    studentTotal._kyukouCnt = add(studentTotal._kyukouCnt, semeMonth._kyukouCnt);
                    totalMonthSet.add(semeMonth._semester + semeMonth._month);
                }
                
                if (semeMonth._attendMap.containsKey(student._schregno)) {
                    final Attend attend = (Attend) semeMonth._attendMap.get(student._schregno);
                    svf.VrsOut("KEKKA" + len, attend._sick1);
                    svf.VrsOut("LATE" + len, attend._late);
                    svf.VrsOut("EARLY" + len, attend._early);
                    svf.VrsOut("ABSENT" + len, attend._absent);
                    final int suspend = (NumberUtils.isDigits(attend._suspend)) ? Integer.parseInt(attend._suspend) : 0;
                    final int mourning = (NumberUtils.isDigits(attend._mourning)) ? Integer.parseInt(attend._mourning) : 0;
                    svf.VrsOut("SUSPEND" + len, String.valueOf(suspend + mourning));
                    
                    if (null == studentTotal._attendMap.get(student._schregno)) {
                        studentTotal._attendMap.put(student._schregno, new Attend(null, null, null, null, null, null, null, null));
                    }
                    final Attend attendTotal = (Attend) studentTotal._attendMap.get(student._schregno);
                    attendTotal._lesson = add(attendTotal._lesson, attend._lesson);
                    attendTotal._sick2 = add(attendTotal._sick2, attend._sick2);
                    attendTotal._sick1 = add(attendTotal._sick1, attend._sick1);
                    attendTotal._late = add(attendTotal._late, attend._late);
                    attendTotal._early = add(attendTotal._early, attend._early);
                    attendTotal._absent = add(attendTotal._absent, attend._absent);
                    attendTotal._suspend = add(attendTotal._suspend, attend._suspend);
                    attendTotal._mourning = add(attendTotal._mourning, attend._mourning);
                }
            }
            
            if (!totalMonthSet.isEmpty()) {
                len = 7;
                svf.VrsOut("SEMESTER" + len, studentTotal._semestername);
                svf.VrsOut("MONTH" + len, "計");
                svf.VrsOut("MI" + len, studentTotal._miCnt);
                svf.VrsOut("JISSI" + len, studentTotal._jissiCnt);
                svf.VrsOut("KYUKOU" + len, studentTotal._kyukouCnt);

                final Attend attendTotal = (Attend) studentTotal._attendMap.get(student._schregno);
                if (null != attendTotal) {
                    svf.VrsOut("KEKKA" + len, attendTotal._sick1);
                    svf.VrsOut("LATE" + len, attendTotal._late);
                    svf.VrsOut("EARLY" + len, attendTotal._early);
                    svf.VrsOut("ABSENT" + len, attendTotal._absent);
                    final int suspend = (NumberUtils.isDigits(attendTotal._suspend)) ? Integer.parseInt(attendTotal._suspend) : 0;
                    final int mourning = (NumberUtils.isDigits(attendTotal._mourning)) ? Integer.parseInt(attendTotal._mourning) : 0;
                    svf.VrsOut("SUSPEND" + len, String.valueOf(suspend + mourning));
                }
            }
            svf.VrEndRecord();
            hasData = true;
        }
        
        if (hasData) {
            int len = 0;
            Attend totalTotal = null;
            for (final Iterator it2 = kouza._semeMonthList.iterator(); it2.hasNext();) {
                final SemeMonth semeMonth = (SemeMonth) it2.next();
                len++;
                if (semeMonth._attendMap.containsKey("TOTAL")) {
                    final Attend total = (Attend) semeMonth._attendMap.get("TOTAL");
                    svf.VrsOut("TOTAL_KEKKA" + len, total._sick1);
                    svf.VrsOut("TOTAL_LATE" + len, total._late);
                    svf.VrsOut("TOTAL_EARLY" + len, total._early);
                    svf.VrsOut("TOTAL_ABSENT" + len, total._absent);
                    final int suspend = (NumberUtils.isDigits(total._suspend)) ? Integer.parseInt(total._suspend) : 0;
                    final int mourning = (NumberUtils.isDigits(total._mourning)) ? Integer.parseInt(total._mourning) : 0;
                    svf.VrsOut("TOTAL_SUSPEND" + len, String.valueOf(suspend + mourning));
                    
                    if (null == totalTotal) {
                        totalTotal = new Attend(null, null, null, null, null, null, null, null);
                    }
                    
                    totalTotal._lesson = add(totalTotal._lesson, total._lesson);
                    totalTotal._sick2 = add(totalTotal._sick2, total._sick2);
                    totalTotal._sick1 = add(totalTotal._sick1, total._sick1);
                    totalTotal._late = add(totalTotal._late, total._late);
                    totalTotal._early = add(totalTotal._early, total._early);
                    totalTotal._absent = add(totalTotal._absent, total._absent);
                    totalTotal._suspend = add(totalTotal._suspend, total._suspend);
                    totalTotal._mourning = add(totalTotal._mourning, total._mourning);
                }
            }
            if (null != totalTotal) {
                len = 7;
                svf.VrsOut("TOTAL_KEKKA" + len, totalTotal._sick1);
                svf.VrsOut("TOTAL_LATE" + len, totalTotal._late);
                svf.VrsOut("TOTAL_EARLY" + len, totalTotal._early);
                svf.VrsOut("TOTAL_ABSENT" + len, totalTotal._absent);
                final int suspend = (NumberUtils.isDigits(totalTotal._suspend)) ? Integer.parseInt(totalTotal._suspend) : 0;
                final int mourning = (NumberUtils.isDigits(totalTotal._mourning)) ? Integer.parseInt(totalTotal._mourning) : 0;
                svf.VrsOut("TOTAL_SUSPEND" + len, String.valueOf(suspend + mourning));
            }
            svf.VrEndRecord();
        }

        return hasData;
    }
    
    private static String add(final String num1, final String num2) {
        if (!NumberUtils.isNumber(num1)) { return num2; }
        if (!NumberUtils.isNumber(num2)) { return num1; }
        return new BigDecimal(num1).add(new BigDecimal(num2)).toString();
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
                kouzaInfo._semeMonthList = kouzaInfo.createSemeMonthInfoData(db2, kouzaInfo._chaircd, kouzaInfo._studentList);
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
        List _semeMonthList = new ArrayList();
        
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
        private List createStudentInfoData(final DB2UDB db2, final String chaircd) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List rtnList = new ArrayList();
            try {
                final String sql = getStudentInfoSql(chaircd);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student studentInfo = new Student(
                            rs.getString("SCHREGNO"),
                            rs.getString("NAME"),
                            rs.getString("SEX"),
                            rs.getString("SEX_NAME"),
                            rs.getString("HR_NAME"),
                            rs.getString("HR_NAMEABBV"),
                            rs.getString("ATTENDNO"),
                            rs.getString("CREDITS")
                    );
                    rtnList.add(studentInfo);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnList;
        }
        
        private List createSemeMonthInfoData(final DB2UDB db2, final String chaircd, final List studentList) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List rtnList = new ArrayList();
            try {
                final String sql = getSemeMonthInfoSql(chaircd);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final SemeMonth semeMonthInfo = new SemeMonth(
                            rs.getString("SEMESTER"),
                            rs.getString("SEMESTERNAME"),
                            rs.getString("MONTH"),
                            rs.getString("CNT_JISSI"),
                            rs.getString("CNT_KYUKOU"),
                            rs.getString("CNT_MI")
                            );
                    rtnList.add(semeMonthInfo);
                    semeMonthInfo._attendMap = semeMonthInfo.createAttendInfoData(db2, semeMonthInfo.getSemMonthInState(), studentList);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
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
    }
    
    private String getSemeMonthInfoSql(final String chaircd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     S1.SEMESTERNAME, ");
        stb.append("     MONTH(T1.EXECUTEDATE) AS MONTH, ");
        stb.append("     CASE WHEN MONTH(T1.EXECUTEDATE) <= 3 THEN MONTH(T1.EXECUTEDATE) + 12 ELSE MONTH(T1.EXECUTEDATE) END AS MONTH_ORDER, ");
        stb.append("     COUNT(CASE WHEN T1.EXECUTEDIV = '1' THEN '1' END) AS CNT_JISSI, ");
        stb.append("     COUNT(CASE WHEN T1.EXECUTEDIV = '2' THEN '2' END) AS CNT_KYUKOU, ");
        stb.append("     COUNT(CASE WHEN T1.EXECUTEDIV = '1' OR T1.EXECUTEDIV = '2' THEN NULL ELSE '0' END) AS CNT_MI ");
        stb.append(" FROM ");
        stb.append("     SCH_CHR_DAT T1 ");
        stb.append("     LEFT JOIN SEMESTER_MST S1 ON S1.YEAR = T1.YEAR AND S1.SEMESTER = T1.SEMESTER ");
        stb.append(" WHERE ");
        stb.append("     T1.EXECUTEDATE BETWEEN DATE('" + _param._dateFrom + "') AND DATE('" + _param._dateTo + "') ");
        stb.append("     AND T1.CHAIRCD = '" + chaircd + "' ");
        stb.append("     AND T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     S1.SEMESTERNAME, ");
        stb.append("     MONTH(T1.EXECUTEDATE), ");
        stb.append("     CASE WHEN MONTH(T1.EXECUTEDATE) <= 3 THEN MONTH(T1.EXECUTEDATE) + 12 ELSE MONTH(T1.EXECUTEDATE) END ");
        stb.append(" ORDER BY ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     MONTH_ORDER ");
        return stb.toString();
    }
    
    /** 学期・月クラス */
    private class SemeMonth {
        final String _semester;
        String _semestername;
        final String _month;
        String _jissiCnt;
        String _kyukouCnt;
        String _miCnt;
        Map _attendMap = new HashMap();
        
        SemeMonth(
                final String semester,
                final String semestername,
                final String month,
                final String jissiCnt,
                final String kyukouCnt,
                final String miCnt
        ) {
            _semester = semester;
            _semestername = semestername;
            _month = month;
            _jissiCnt = jissiCnt;
            _kyukouCnt = kyukouCnt;
            _miCnt = miCnt;
        }
        
        private String getSemMonthInState() {
            final String month2 = _month.length() == 1 ? "0" + _month : _month;
            final String semMonthInState = "('" + _semester + month2 + "')";
            return semMonthInState;
        }

        private Map createAttendInfoData(final DB2UDB db2, final String semMonthInState, final List studentList) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map rtnMap = new HashMap();
            final Map paramMap = new HashMap();
            paramMap.put("useCurriculumcd", _param._useCurriculumcd);
            paramMap.put("useVirus", _param._useVirus);
            paramMap.put("useKoudome", _param._useKoudome);
            try {
                final String sql = AttendAccumulate.getAttendSubclassSql(
                        true,
                        _param._definecode,
                        _param._knjSchoolMst,
                        _param._year,
                        _param.SSEMESTER,
                        _param._semester,
                        semMonthInState,
                        _param._periodInState,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "?",
                        paramMap
                );
                ps = db2.prepareStatement(sql);

                int cnt = 0;
                int lesson = 0, sick2 = 0, sick1 = 0, late = 0, early = 0, absent = 0, suspend = 0, mourning = 0;
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        if (!"9".equals(rs.getString("SEMESTER")) || !_param._subclasscd.equals(rs.getString("SUBCLASSCD"))) {
                            continue;
                        }
                        final Attend attendInfo = new Attend(
                                rs.getString("LESSON"),
                                rs.getString("SICK2"),
                                rs.getString("SICK1"),
                                rs.getString("LATE"),
                                rs.getString("EARLY"),
                                rs.getString("ABSENT"),
                                rs.getString("SUSPEND"),
                                rs.getString("MOURNING")
                        );
                        rtnMap.put(student._schregno, attendInfo);

                        lesson += Integer.parseInt(attendInfo._lesson);
                        sick2 += Integer.parseInt(attendInfo._sick2);
                        sick1 += Integer.parseInt(attendInfo._sick1);
                        late += Integer.parseInt(attendInfo._late);
                        early += Integer.parseInt(attendInfo._early);
                        absent += Integer.parseInt(attendInfo._absent);
                        suspend += Integer.parseInt(attendInfo._suspend);
                        mourning += Integer.parseInt(attendInfo._mourning);
                        
                        cnt++;
                    }
                }
                if (cnt > 0) {
                    final Attend total = new Attend(String.valueOf(lesson), String.valueOf(sick2), String.valueOf(sick1), String.valueOf(late), String.valueOf(early), String.valueOf(absent), String.valueOf(suspend), String.valueOf(mourning));
                    rtnMap.put("TOTAL", total);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnMap;
        }
    }
    
    /** 出欠クラス */
    private class Attend {
        String _lesson;
        String _sick2;
        String _sick1;
        String _late;
        String _early;
        String _absent;
        String _suspend;
        String _mourning;
        
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
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(db2, request);
    }

    private static class Param {

        final String _year;
        final String _semester;
        private String _semestername;
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
                _periodInState = AttendAccumulate.getPeiodValue(db2, _definecode, _year, SSEMESTER, _semester);
                _semestername = setSemesterName(db2);
            } catch (Exception e) {
                log.debug("loadAttendSemesArgument exception", e);
            }
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

        private String setSemesterName(DB2UDB db2) {
            String name = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    name = rs.getString("SEMESTERNAME");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name;
        }
    }
    
}// クラスの括り
