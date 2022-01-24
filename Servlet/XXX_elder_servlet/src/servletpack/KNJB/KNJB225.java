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

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;


/**
 * クラス別希望状況一覧
 */
public class KNJB225 {

    private static final Log log = LogFactory.getLog(KNJB225.class);
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

            printMain(db2, svf);

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
    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        //予定
        if ("1".equals(_param._output)) {
            final List creditList = createCreditInfoData(db2);
            if (creditList.size() > 0) {
                printDataList(svf, creditList);
            }
        } else {
            final List studentCreditList = createStudentCreditInfoData(db2);
            if (studentCreditList.size() > 0) {
                String studentKeep = "";
                List studentList = new ArrayList();
                List curentStudentCreditList = new ArrayList();
                for (final Iterator it = studentCreditList.iterator(); it.hasNext();) {
                    final StudentCredit studentCredit = (StudentCredit) it.next();
                    if (!studentKeep.equals(studentCredit._schregno)) {
                        curentStudentCreditList = new ArrayList();
                        studentList.add(curentStudentCreditList);
                        studentKeep = studentCredit._schregno;
                    }
                    curentStudentCreditList.add(studentCredit);
                }
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final List creditList = (List) it.next();
                    printStudentList(svf, creditList);
                }
            }
        }
    }

    private void printDataList(final Vrw32alp svf, final List creditList) {
        svf.VrSetForm("KNJB225.frm", 4);

        svf.VrsOut("TITLE", _param._year + "年度　" + "通年" + "展開表");
        svf.VrsOut("GRADE_COURSE", _param._gradecoursename);

        int subCnt = 0;
        int creditsMax = 0;
        String groupKeep = "";
        Map dataMap = new HashMap();
        Map dataStartMap = new HashMap();
        Map headerMap = new HashMap();

        final int maxLen = 8;
        int no = 0;
        for (final Iterator it = creditList.iterator(); it.hasNext();) {
            final Credit credit = (Credit) it.next();
            final int credits = (NumberUtils.isDigits(credit._credits)) ? Integer.parseInt(credit._credits) : 0;
            if (credit._groupcd != null) {
                if (!groupKeep.equals(credit._groupcd)) {
                    groupKeep = credit._groupcd;
                    subCnt = 0;
                    no = no + creditsMax;
                    creditsMax = 0;
                }
                subCnt++;
                if (creditsMax < credits) creditsMax = credits;
                for (int len = 1; len <= credits; len++) {
                    no++;
                    final int line = (no % maxLen == 0) ? no / maxLen : no / maxLen + 1;
                    final int lenNo = (no % maxLen == 0) ? maxLen : no % maxLen;
                    log.debug(" creditNo=" + no + " line=" + line + " subclasscd=" + credit._subclasscd + " groupcd=" + credit._groupcd + " subCnt=" + subCnt + " lenNo=" + lenNo);
                    final String key = String.valueOf(line) + String.valueOf(subCnt) + String.valueOf(lenNo);
                    dataMap.put(key, credit);
                    if (len == 1) dataStartMap.put(key, credit);
                    final String key2 = String.valueOf(line);
                    if (!headerMap.containsKey(key2)) {
                        headerMap.put(key2, String.valueOf(subCnt));
                    } else {
                        final String val = (String) headerMap.get(key2);
                        if (Integer.parseInt(val) < subCnt) {
                            headerMap.put(key2, String.valueOf(subCnt));
                        }
                    }
                }
                no = no - credits;
            } else {
                subCnt = 1;
                for (int len = 1; len <= credits; len++) {
                    no++;
                    final int line = (no % maxLen == 0) ? no / maxLen : no / maxLen + 1;
                    final int lenNo = (no % maxLen == 0) ? maxLen : no % maxLen;
                    log.debug(" creditNo=" + no + " line=" + line + " subclasscd=" + credit._subclasscd + " groupcd=" + credit._groupcd + " subCnt=" + subCnt + " lenNo=" + lenNo);
                    final String key = String.valueOf(line) + String.valueOf(subCnt) + String.valueOf(lenNo);
                    dataMap.put(key, credit);
                    if (len == 1) dataStartMap.put(key, credit);
                    final String key2 = String.valueOf(line);
                    if (!headerMap.containsKey(key2)) {
                        headerMap.put(key2, String.valueOf(subCnt));
                    }
                }
            }
        }

        if (!headerMap.isEmpty()) {
            int creditNo = 0;
            final int size = headerMap.size();
            log.debug(" size=" + size);
            for (int line = 1; line <= size; line++) {
                final String key2 = String.valueOf(line);
                if (headerMap.containsKey(key2)) {
                    final String val2 = (String) headerMap.get(key2);
                    final int size2 = Integer.parseInt(val2);
                    log.debug(" size2=" + size2);
                    for (int gyo = 1; gyo <= size2; gyo++) {
                        if (gyo == 1) {
                            //空行(8列)
                            if (0 < creditNo) {
                                for (int len = 1; len <= maxLen; len++) {
                                    svf.VrsOut("KARA", String.valueOf(len));
                                    svf.VrEndRecord();
                                }
                            }
                            //No
                            for (int len = 1; len <= maxLen; len++) {
                                final String key = String.valueOf(line) + String.valueOf(gyo) + String.valueOf(len);
                                if (dataMap.containsKey(key)) {
                                    final Credit credit = (Credit) dataMap.get(key);
                                    //No
                                    creditNo++;
                                    svf.VrsOut("CREDIT_NO", String.valueOf(creditNo));
                                    svf.VrEndRecord();
                                    _hasData = true;
                                    log.debug(" line=" + line + " gyo=" + gyo + " len=" + len + " subclasscd=" + credit._subclasscd + " groupcd=" + credit._groupcd);
                                } else {
                                    //空列
                                    svf.VrsOut("KARA", String.valueOf(len));
                                    svf.VrEndRecord();
                                    log.debug(" line=" + line + " gyo=" + gyo + " len=" + len + " subclasscd=" + " groupcd=");
                                }
                            }
                        }
                        //科目
                        for (int len = 1; len <= maxLen; len++) {
                            final String key = String.valueOf(line) + String.valueOf(gyo) + String.valueOf(len);
                            if (dataMap.containsKey(key)) {
                                final Credit credit = (Credit) dataMap.get(key);
                                //科目
                                svf.VrsOut("SUBCLASSCD_SHIRO", credit._subclasscd);
                                if (dataStartMap.containsKey(key)) {
                                    svf.VrsOut("SUBCLASSCD", credit._subclasscd);
                                    svf.VrsOut("SUBCLASSNAME", credit._subclassname);
                                }
                                svf.VrEndRecord();
                                log.debug(" line=" + line + " gyo=" + gyo + " len=" + len + " subclasscd=" + credit._subclasscd + " groupcd=" + credit._groupcd);
                            } else {
                                //空列
                                svf.VrsOut("KARA", String.valueOf(len));
                                svf.VrEndRecord();
                                log.debug(" line=" + line + " gyo=" + gyo + " len=" + len + " subclasscd=" + " groupcd=");
                            }
                        }
                    }
                }
            }
        } else {
            log.debug(" headerMap isEmpty");
        }

    }

    private void printStudentList(final Vrw32alp svf, final List creditList) {
        svf.VrSetForm("KNJB225.frm", 4);

        svf.VrsOut("TITLE", _param._year + "年度　" + "通年" + "展開表");

        final int maxLen = 8;
        int no = 0;
        int lineKeep = 0;
        List lineList = new ArrayList();
        List curentLineCreditList = new ArrayList();
        for (final Iterator it = creditList.iterator(); it.hasNext();) {
            final StudentCredit credit = (StudentCredit) it.next();
            final int credits = (NumberUtils.isDigits(credit._credits)) ? Integer.parseInt(credit._credits) : 0;
            for (int len = 1; len <= credits; len++) {
                no++;
                final int line = (no % maxLen == 0) ? no / maxLen : no / maxLen + 1;
                if (lineKeep != line) {
                    curentLineCreditList = new ArrayList();
                    lineList.add(curentLineCreditList);
                    lineKeep = line;
                }
                curentLineCreditList.add(credit);
            }
        }

        int creditNo = 0;
        String cdKeep = "";
        for (final Iterator itLine = lineList.iterator(); itLine.hasNext();) {
            final List lineCreditList = (List) itLine.next();
            //空行(8列)
            if (0 < creditNo) {
                for (int len = 1; len <= maxLen; len++) {
                    svf.VrsOut("KARA", String.valueOf(len));
                    svf.VrEndRecord();
                    _hasData = true;
                }
            }
            //No
            for (final Iterator itCredit = lineCreditList.iterator(); itCredit.hasNext();) {
                final StudentCredit credit = (StudentCredit) itCredit.next();
                final String attendno = NumberUtils.isDigits(credit._attendno) ? String.valueOf(Integer.parseInt(credit._attendno)) : credit._attendno;
                svf.VrsOut("GRADE_COURSE", _param._gradecoursename + "　" + credit._hrName + attendno + "番　" + credit._schregno + "　" + credit._name);
                creditNo++;
                log.debug(creditNo + ":" + credit._subclasscd);
                svf.VrsOut("CREDIT_NO", String.valueOf(creditNo));
                svf.VrEndRecord();
                _hasData = true;
            }
            //No(空列)
            if (0 < creditNo % maxLen) {
                int startLen = creditNo % maxLen + 1;
                for (int len = startLen; len <= maxLen; len++) {
                    svf.VrsOut("KARA", String.valueOf(len));
                    svf.VrEndRecord();
                    _hasData = true;
                }
            }
            //科目
            for (final Iterator itCredit = lineCreditList.iterator(); itCredit.hasNext();) {
                final StudentCredit credit = (StudentCredit) itCredit.next();
                svf.VrsOut("SUBCLASSCD_SHIRO", credit._subclasscd);
                if (!cdKeep.equals(credit._subclasscd)) {
                    svf.VrsOut("SUBCLASSCD", credit._subclasscd);
                    svf.VrsOut("SUBCLASSNAME", credit._subclassname);
                    cdKeep = credit._subclasscd;
                }
                svf.VrEndRecord();
                _hasData = true;
            }
            //科目(空列)
            if (0 < creditNo % maxLen) {
                int startLen = creditNo % maxLen + 1;
                for (int len = startLen; len <= maxLen; len++) {
                    svf.VrsOut("KARA", String.valueOf(len));
                    svf.VrEndRecord();
                    _hasData = true;
                }
            }
        }

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

    private List createCreditInfoData(final DB2UDB db2) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtnList = new ArrayList();
        try {
            final String sql = getCreditInfoSql();
            log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Credit credit = new Credit(
                        rs.getString("SUBCLASSCD"),
                        rs.getString("SUBCLASSNAME"),
                        rs.getString("CREDITS"),
                        rs.getString("REQUIRE_FLG"),
                        rs.getString("REQUIRE_NAME"),
                        rs.getString("ELECTDIV"),
                        rs.getString("GROUPCD"),
                        rs.getString("GROUPDIV")
                        );
                rtnList.add(credit);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getCreditInfoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append("     T1.SUBCLASSCD AS SUBCLASSCD, ");
        }
        stb.append("     L1.SUBCLASSNAME, ");
        stb.append("     T1.CREDITS, ");
        stb.append("     T1.REQUIRE_FLG, ");
        stb.append("     N1.NAME1 AS REQUIRE_NAME, ");
        stb.append("     CASE WHEN T1.REQUIRE_FLG = '3' THEN '1' ELSE '0' END AS ELECTDIV, ");
        stb.append("     L2.GROUPCD, ");
        stb.append("     CASE WHEN L2.GROUPCD IS NOT NULL THEN '1' ELSE '0' END AS GROUPDIV ");
        stb.append(" FROM ");
        stb.append("     CREDIT_MST T1 ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z011' AND N1.NAMECD2 = T1.REQUIRE_FLG ");
        stb.append("     LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("             AND L1.CLASSCD = T1.CLASSCD ");
            stb.append("             AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("             AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("     LEFT JOIN SUBCLASS_COMP_SELECT_DAT L2 ");
        stb.append("             ON  L2.YEAR = T1.YEAR ");
        stb.append("             AND L2.GRADE = T1.GRADE ");
        stb.append("             AND L2.COURSECD = T1.COURSECD ");
        stb.append("             AND L2.MAJORCD = T1.MAJORCD ");
        stb.append("             AND L2.COURSECODE = T1.COURSECODE ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("             AND L2.CLASSCD = T1.CLASSCD ");
            stb.append("             AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("             AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("             AND L2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("             AND VALUE(T1.REQUIRE_FLG, '0') = '3' "); //選択
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.COURSECD = '" + _param._coursecd + "' ");
        stb.append("     AND T1.MAJORCD = '" + _param._majorcd + "' ");
        stb.append("     AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("     AND T1.COURSECODE = '" + _param._coursecode + "' ");
        stb.append("     AND T1.CREDITS IS NOT NULL ");
        stb.append(" ORDER BY ");
        stb.append("     ELECTDIV, ");
        stb.append("     GROUPDIV, ");
        stb.append("     L2.GROUPCD, ");
//        stb.append("     T1.CREDITS DESC, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
        }
        stb.append("     T1.SUBCLASSCD ");
        return stb.toString();
    }

    /** 単位マスタ */
    private class Credit {
        final String _subclasscd;
        final String _subclassname;
        final String _credits;
        final String _requireFlg;
        final String _requireName;
        final String _electdiv;
        final String _groupcd;
        final String _groupdiv;

        Credit(
                final String subclasscd,
                final String subclassname,
                final String credits,
                final String requireFlg,
                final String requireName,
                final String electdiv,
                final String groupcd,
                final String groupdiv
        ) {
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _credits = credits;
            _requireFlg = requireFlg;
            _requireName = requireName;
            _electdiv = electdiv;
            _groupcd = groupcd;
            _groupdiv = groupdiv;
        }
    }

    private List createStudentCreditInfoData(final DB2UDB db2) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtnList = new ArrayList();
        try {
            final String sql = getStudentCreditInfoSql();
            log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final StudentCredit studentCredit = new StudentCredit(
                        rs.getString("SCHREGNO"),
                        rs.getString("NAME"),
                        rs.getString("HR_NAME"),
                        rs.getString("ATTENDNO"),
                        rs.getString("SUBCLASSCD"),
                        rs.getString("SUBCLASSNAME"),
                        rs.getString("CREDITS")
                        );
                rtnList.add(studentCredit);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getStudentCreditInfoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T3.HR_NAME, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || L2.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append("     L2.SUBCLASSCD AS SUBCLASSCD, ");
        }
        stb.append("     L3.SUBCLASSNAME, ");
        stb.append("     L1.CREDITS, ");
        stb.append("     CASE WHEN L1.REQUIRE_FLG = '3' THEN '1' ELSE '0' END AS ELECTDIV ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T3 ");
        stb.append("             ON  T3.YEAR = T1.YEAR ");
        stb.append("             AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("             AND T3.GRADE = T1.GRADE ");
        stb.append("             AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append("     INNER JOIN SUBCLASS_STD_SELECT_DAT L2 ");
        stb.append("             ON  L2.YEAR = T1.YEAR ");
        stb.append("             AND L2.SEMESTER = T1.SEMESTER ");
        stb.append("             AND L2.RIREKI_CODE = '" + _param._rirekiCode + "' ");
        stb.append("             AND L2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     INNER JOIN CREDIT_MST L1 ");
        stb.append("             ON  L1.YEAR = T1.YEAR ");
        stb.append("             AND L1.GRADE = T1.GRADE ");
        stb.append("             AND L1.COURSECD = T1.COURSECD ");
        stb.append("             AND L1.MAJORCD = T1.MAJORCD ");
        stb.append("             AND L1.COURSECODE = T1.COURSECODE ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("             AND L1.CLASSCD = L2.CLASSCD ");
            stb.append("             AND L1.SCHOOL_KIND = L2.SCHOOL_KIND ");
            stb.append("             AND L1.CURRICULUM_CD = L2.CURRICULUM_CD ");
        }
        stb.append("             AND L1.SUBCLASSCD = L2.SUBCLASSCD ");
        stb.append("             AND L1.CREDITS IS NOT NULL ");
        stb.append("     LEFT JOIN SUBCLASS_MST L3 ON L3.SUBCLASSCD = L2.SUBCLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("             AND L3.CLASSCD = L2.CLASSCD ");
            stb.append("             AND L3.SCHOOL_KIND = L2.SCHOOL_KIND ");
            stb.append("             AND L3.CURRICULUM_CD = L2.CURRICULUM_CD ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("     AND T1.COURSECD = '" + _param._coursecd + "' ");
        stb.append("     AND T1.MAJORCD = '" + _param._majorcd + "' ");
        stb.append("     AND T1.COURSECODE = '" + _param._coursecode + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     ELECTDIV, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     L2.CLASSCD, ");
            stb.append("     L2.SCHOOL_KIND, ");
            stb.append("     L2.CURRICULUM_CD, ");
        }
        stb.append("     L2.SUBCLASSCD ");
        return stb.toString();
    }

    /** 生徒 */
    private class StudentCredit {
        final String _schregno;
        final String _name;
        final String _hrName;
        final String _attendno;
        final String _subclasscd;
        final String _subclassname;
        final String _credits;

        StudentCredit(
                final String schregno,
                final String name,
                final String hrName,
                final String attendno,
                final String subclasscd,
                final String subclassname,
                final String credits
        ) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _attendno = attendno;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _credits = credits;
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
        final String _grade;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        private String _gradecoursename;
        final String _output;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _useCurriculumcd;
        private final String _rirekiCode;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year      = request.getParameter("CTRL_YEAR");
            _semester  = request.getParameter("SEMESTER");
            _grade  = request.getParameter("GRADE");
            _coursecd  = request.getParameter("COURSECD");
            _majorcd  = request.getParameter("MAJORCD");
            _coursecode  = request.getParameter("COURSECODE");
            _output  = request.getParameter("OUTPUT");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _rirekiCode = request.getParameter("RIREKI_CODE");
            try {
                _semestername = setSemesterName(db2);
                _gradecoursename = setGradeCourseName(db2);
            } catch (Exception e) {
                log.debug("setSemesterName exception", e);
            }
        }

        private String getDate(final String date) {
            if (date == null) {
                return null;
            }
            return KNJ_EditDate.h_format_S(date, "yyyy") + "年" + KNJ_EditDate.setDateFormat2(KNJ_EditDate.h_format_JP_MD(date));
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

        private String setGradeCourseName(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String name = null;
            try {
                final String sql = getGradeCourseNameSql();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    name = rs.getString("GRADE_NAME1") + " " + rs.getString("COURSENAME") + rs.getString("MAJORNAME") + rs.getString("COURSECODENAME");
                }
            } catch (SQLException ex) {
                log.debug("setGradeCourseName exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name;
        }

        private String getGradeCourseNameSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T3.GRADE, ");
            stb.append("     T3.GRADE_NAME1, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T1.COURSENAME, ");
            stb.append("     T1.MAJORNAME, ");
            stb.append("     T2.COURSECODE, ");
            stb.append("     T2.COURSECODENAME ");
            stb.append(" FROM ");
            stb.append("     V_COURSE_MAJOR_MST T1, ");
            stb.append("     V_COURSECODE_MST T2, ");
            stb.append("     SCHREG_REGD_GDAT T3 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = T2.YEAR ");
            stb.append("     AND T1.YEAR = T3.YEAR ");
            stb.append("     AND T1.YEAR = '" + _year + "' ");
            stb.append("     AND T3.GRADE = '" + _grade + "' ");
            stb.append("     AND T1.COURSECD = '" + _coursecd + "' ");
            stb.append("     AND T1.MAJORCD = '" + _majorcd + "' ");
            stb.append("     AND T2.COURSECODE = '" + _coursecode + "' ");
            return stb.toString();
        }
    }

}// クラスの括り
