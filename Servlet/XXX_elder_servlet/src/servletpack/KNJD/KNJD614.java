// kanji=漢字
/*
 * $Id: 1c9b4b84486c367c683caa58bc6a001edf95c9c9 $
 *
 * 作成日: 2006/05/08 21:10:29 - JST
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJC.KNJDivideAttendDate;
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理] 成績一覧 (単位制　東京都版)
 *  2006/05/02 yamashiro 東京都版を複写して作成
 *  @version $Id: 1c9b4b84486c367c683caa58bc6a001edf95c9c9 $
 */
public class KNJD614 {
    private static final Log log = LogFactory.getLog(KNJD614.class);

    private static final int MAX_COLUMN = 19;
    private static final int MAX_LINE = 25;

    private static final String SUBJECT_U = "89";  // 教科コード
    private static final String SUBJECT_T = "90";  // 総合的な学習の時間

    private static final String FORM_FILE = "KNJD614.frm";
    private static final String FROM_TO_MARK = "\uFF5E";
    private static final DecimalFormat TOTAL_DEC_FMT = new DecimalFormat("0.0");

    private Common _common;  //成績別処理のクラス

    private Param _param;
    private Form _form;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        _form = new Form();
        try {
            _form.svfInit(response, request);
        } catch (final IOException e) {
            log.fatal("SVFの初期化に失敗!");
            return;
        }

        boolean hasData = false;
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(request, db2);

            _common = createCommon(db2);

            _form.printHeader();

            for (int i = 0; i < _param._hrClasses.length; i++) {
                final HRInfo hrInfo = new HRInfo(_param._hrClasses[i]);
                hrInfo.loadHRClassStaff(db2, _param._hrClasses[i]);
                hrInfo.loadStudents(db2);
                hrInfo.loadScoreDetail(db2);

                hrInfo._ranking = hrInfo.createRanking();
                log.debug("RANK:" + hrInfo._ranking);

                boolean hasDataWrk = false;
                _form.setHead1(hrInfo._staffName, hrInfo._hrName);

                int line = 0;
                for (final Iterator it = hrInfo._students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                
                    final int used = student.print(line);
                    _form.svf.VrEndRecord();
                    line += used;
                    if (0 < used) {
                        hasDataWrk = true;
                    }
                }

                if (hasDataWrk) {
                    if (0 != line % MAX_LINE) {
                        // ページの途中で、年組が終わった場合。
                        final int n = MAX_LINE - (line % MAX_LINE);
                
                        // ページ末までの、残り行数分を、改行(つまり、改ページ)
                        for (int i1 = 0; i1 < n; i1++) {
                            _form.svf.VrEndRecord();
                        }
                    }
                    hasData = true;
                }
            }
        } catch (final Exception e) {
            log.error("メイン処理でエラー", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            _form.closeSvf(hasData);
        }
    }

    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 56595 $ $Date"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        final Param param = new Param(request, db2);
        return param;
    }

    /**
     *   １月〜３月の「学期＋月」の処理 （２学期制における集計処理に対応）
     *   2006/01/27 Build NO004
     *   TODO: "003" ⇒ "103" なぜ？ by takaesu
     */
    private static String retSemesterMonthValue(final String strx) {
        String rtn = null;
        try {
            final String aaa = strx.substring(1, strx.length());
            if (Integer.parseInt(aaa) < 4) {
                final int bbb = Integer.parseInt(strx.substring(0, 1));
                rtn = String.valueOf(bbb + 1) + "" + aaa;
            } else {
                rtn = strx;
            }
        } catch (final NumberFormatException e) {
            log.error("retSemesterMonthValue!", e);
        }
        log.debug("retSemesterMonthValue=" + rtn);
        return rtn;
    }

//    /**
//     * HR組の学籍番号を取得するSQL
//     */
//    private String sqlHrclassStdList() {
//        final StringBuffer stb = new StringBuffer();
//
//        stb.append("SELECT  W1.SCHREGNO ");
//        stb.append("FROM    SCHREG_REGD_DAT W1 ");
//        stb.append("INNER  JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
//        stb.append("WHERE   W1.YEAR = '" + _param._year + "' ");
//        stb.append(    "AND W1.SEMESTER = '" + _param.specialSemester() + "' ");
//        stb.append(    "AND W1.GRADE||W1.HR_CLASS = ? ");
//        stb.append("ORDER BY W1.ATTENDNO"); // TODO: Student#compareTo を使って効率化を図りたい
//
//        return stb.toString();
//    }

    private Common createCommon(final DB2UDB db2) {
        final Common common;
        if ("0101".equals(_param._testKindCd)) {
            // 中間
            common = new CommonInter(_param._creditDrop, _param._testKindCd, _param._semester, _param._semesterName, _param._testName);
        } else if ("0201".equals(_param._testKindCd) || "0202".equals(_param._testKindCd)) {
            // 期末1 or 期末2
            common = new CommonTerm(_param._creditDrop, _param._testKindCd, _param._semester, _param._semesterName, _param._testName);
        } else if (_param.semesterGakunenMatu()) {
            // 学年
            common = new CommonGrade(_param._creditDrop, _param._testKindCd, _param._semester, _param._semesterName, _param._testName);
        } else {
            // 学期
            common = new CommonGakki(_param._creditDrop, _param._testKindCd, _param._semester, _param._semesterName, _param._testName);
        }
        log.fatal(common);

        common.loadAverage(db2);
        return common;
    }

    private static double round10(final int a, final int b) {
        return Math.round(a * 10.0 / b) / 10.0;
    }

    //--- 内部クラス -------------------------------------------------------
    private abstract class Common {
        String _fieldName;
        String _fieldName2;
        String _fieldChaircd;
        private final Map _averageMap;
        protected final boolean _creditDrop;

        protected final String _semesterName;
        protected final String _semester;
        protected final String _testKindCd;

        protected String _testName;

        abstract void loadAverage(DB2UDB db2);
        abstract ScoreValue getScoreValue(ScoreDetail d);
        abstract boolean isPrintAverage();
        abstract boolean doPrintMark();

        public Common(
                final boolean creditDrop,
                final String testKindCd,
                final String semester,
                final String semesterName,
                final String testName
        ) {
            _averageMap = new HashMap();
            _creditDrop = creditDrop;
            _testKindCd = testKindCd;
            _semester = semester;
            _semesterName = semesterName;
            _testName = testName;
        }

        public void addAverage(
                final String chaircd,
                final Double avgScore
        ) {
            _averageMap.put(chaircd,avgScore);
        }
        
        public Double getAverage(final String chaircd) {
            return (Double)_averageMap.get(chaircd);
        }

        boolean hasCredit() {
            // 履修単位数/修得単位数なし
            return false;
        }

        abstract void setHead2(MyVrw32alp svf);

        /** {@inheritDoc} */
        public String toString() {
            return getClass().getName() + " : semesterName=" + _semesterName + ", testName=" + _testName;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 中間試験成績の処理クラス
     */
    private class CommonInter extends Common {

        CommonInter(
                final boolean creditDrop,
                final String testKindCd,
                final String semester,
                final String semesterName,
                final String testName
        ) {
            super(creditDrop, testKindCd, semester, semesterName, testName);
            _fieldName  = "SEM" + _semester + "_INTR_SCORE";
            _fieldName2 = "SEM" + _semester + "_INTR";
            _fieldChaircd = "SEM" + _semester + "_INTR_CHAIRCD";
        }

        /**
         *  中間試験成績 ページ見出し
         */
        void setHead2(final MyVrw32alp svf) {
            svf.VrsOut("TITLE", "");    // 成績名称
            svf.VrsOut("TEST", _testName);  // 成績名称
            svf.VrsOut("TERM", _semesterName); // 学期名称

            // 詳細の凡例
            svf.VrsOut("DETAIL1_1", "素点");  // 科目名の下左
            svf.VrsOut("DETAIL1_2", "平均点"); // 科目名の下右

            // 合計欄
            svf.VrsOut("T_TOTAL", "総合点");
            svf.VrsOut("T_AVERAGE", "平均点");
        }

        ScoreValue getScoreValue(final ScoreDetail d) {
            return d._score;
        }

        boolean isPrintAverage() {
            return true;
        }

        void loadAverage(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlSubclassAverage(_fieldChaircd, _fieldName);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String chaircd = rs.getString("CHAIRCD");
                    final Double avgScore = (Double) rs.getObject("AVG_SCORE");

                    if (null != chaircd && null != avgScore) {
                        addAverage(chaircd, avgScore);
                    }
                }
            } catch (final Exception e) {
                log.error("平均の算出にてエラー", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
        
        
        /*
         *  PrepareStatement作成 --> 科目別平均の表
         */
        private String sqlSubclassAverage(final String fieldChaircd, final String fieldName) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT  ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(         "W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append(         "W3.SUBCLASSCD AS SUBCLASSCD, W2.CHAIRCD");
            stb.append("        ,ROUND(AVG(FLOAT(W3.SCORE))*10,0)/10 AS AVG_SCORE");
            stb.append(" FROM    RECORD_RANK_DAT W3 ");
            stb.append(        " LEFT JOIN RECORD_SCORE_DAT W2 ON W2.YEAR = W3.YEAR ");
            stb.append(                                     "AND W2.SEMESTER = W3.SEMESTER ");
            stb.append(                                     "AND W2.TESTKINDCD = W3.TESTKINDCD ");
            stb.append(                                     "AND W2.TESTITEMCD = W3.TESTITEMCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(                                     "AND W2.CLASSCD = W3.CLASSCD ");
                stb.append(                                     "AND W2.SCHOOL_KIND = W3.SCHOOL_KIND ");
                stb.append(                                     "AND W2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            }
            stb.append(                                     "AND W2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append(                                     "AND W2.SCHREGNO = W3.SCHREGNO ");
            stb.append(" WHERE   W3.YEAR = '" + _param._year + "' AND ");
            stb.append(        " W3.SEMESTER = '" + _param._semester + "' AND ");
            stb.append(        " W3.TESTKINDCD || W3.TESTITEMCD = '" + _param._testKindCd + "' ");
            stb.append(" GROUP BY ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(         "W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append(        " W3.SUBCLASSCD, W2.CHAIRCD");
            stb.append(" HAVING W2.CHAIRCD IS NOT NULL");

            return stb.toString();
        }

        boolean doPrintMark() {
            return false;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 期末試験成績の処理クラス
     */
    private class CommonTerm extends CommonInter {
        CommonTerm(
                final boolean creditDrop,
                final String testKindCd,
                final String semester,
                final String semesterName,
                final String testName
        ) {
            super(creditDrop, testKindCd, semester, semesterName, testName);
            if (_testKindCd.equals("0201")) {
                _fieldName  = "SEM" + _semester + "_TERM_SCORE";
                _fieldName2 = "SEM" + _semester + "_TERM";
                _fieldChaircd = "SEM" + _semester + "_TERM_CHAIRCD";

                _testName = "期末";
            } else {
                _fieldName  = "SEM" + _semester + "_TERM2_SCORE";
                _fieldName2 = "SEM" + _semester + "_TERM2";
                _fieldChaircd = "SEM" + _semester + "_TERM2_CHAIRCD";

                _testName = "期末２";
            }
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 学年成績の処理クラス
     */
    private class CommonGrade extends Common {
        public CommonGrade(
                final boolean creditDrop,
                final String testKindCd,
                final String semester,
                final String semesterName,
                final String testName
        ) {
            super(creditDrop, testKindCd, semester, semesterName, testName);
            _fieldName = "GRAD_VALUE";
            _fieldName2 = null;
            _fieldChaircd = "SEM1_INTR_CHAIRCD"; // ダミーでフィールドをセットしておく
        }

        boolean hasCredit() {
            // 履修単位数/修得単位数あり
            return true;
        }

        /**
         *  学年成績 ページ見出し
         */
        void setHead2(final MyVrw32alp svf) {
            svf.VrsOut("TITLE", "（評定）");    //成績名称
            svf.VrsOut("TEST", "");  // 成績名称
            svf.VrsOut("TERM", ""); // 学期名称

            // 詳細の凡例
            svf.VrsOut("DETAIL1_1", "評定");  // 科目名の下左
            svf.VrsOut("DETAIL1_2", "単位数"); // 科目名の下右

            // 合計欄
            svf.VrsOut("T_TOTAL", "評定合計");
            svf.VrsOut("T_AVERAGE", "評定平均");
        }

        ScoreValue getScoreValue(final ScoreDetail d) {
            return d._patternAssess;
        }

        boolean isPrintAverage() {
            return false;
        }

        void loadAverage(final DB2UDB db2) {}

        boolean doPrintMark() {
            return _creditDrop;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 学期成績の処理クラス
     */
    private class CommonGakki extends CommonGrade {
        public CommonGakki(
                final boolean creditDrop,
                final String testKindCd,
                final String semester,
                final String semesterName,
                final String testName
        ) {
            super(creditDrop, testKindCd, semester, semesterName, testName);
            _fieldName = "SEM" + _semester + "_VALUE";
            _fieldName2 = null;
            _fieldChaircd = "SEM" + _semester + "_INTR_CHAIRCD"; // ダミーでフィールドをセットしておく
        }

        /**
         *  学期成績 ページ見出し
         */
        void setHead2(final MyVrw32alp svf) {
            svf.VrsOut("TITLE", ""); // 成績名称
            svf.VrsOut("TEST", "");  // 成績名称
            svf.VrsOut("TERM", _semesterName); // 学期名称

            // 詳細の凡例
            svf.VrsOut("DETAIL1_1", "評価");  // 科目名の下左
            svf.VrsOut("DETAIL1_2", "単位数"); // 科目名の下右

            // 合計欄
            svf.VrsOut("T_TOTAL", "評価合計");
            svf.VrsOut("T_AVERAGE", "評価平均");
        }

        boolean hasCredit() {
            // 履修単位数/修得単位数なし
            return false;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private static class MyVrw32alp extends Vrw32alp {
        public int VrsOut(String field, String data) {
            if (null == field || null == data) {
                return 0;
            }
            return super.VrsOut(field, data);
        }

        public int VrsOutn(String field, int gyo, String data) {
            if (null == field || null == data) {
                return 0;
            }
            return super.VrsOutn(field, gyo, data);
        }

        public void doSvfOutNonZero(
                final String str1,
                final String str2
        ) {
            if (null == str1 || null == str2) {
                return;
            }
            if (str2.equals("0")) {
                return;
            }

            VrsOut(str1, str2);
        }

        public void doSvfOutNonZero(
                final String str,
                final int val
        ) {
            if (null == str || 0 == val) {
                return;
            }

            VrsOut(str, String.valueOf(val));
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private class HRInfo implements Comparable {
        private final String _code;
        private String _staffName;
        private String _hrName;

        private final List _students = new LinkedList();

        private List _ranking;

        HRInfo(
                final String code
        ) {
            _code = code;
        }
        
        private Student getStudent(final String schregno) {
            if (schregno == null) {
                return null;
            }
            for (final Iterator it = _students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (schregno.equals(student._code)) {
                    return student;
                }
            }
            return null;
        }

        private void loadHRClassStaff(final DB2UDB db2, final String hrClass) {
            final KNJ_Get_Info.ReturnVal returnval = _param._getinfo.Hrclass_Staff(
                    db2,
                    _param._year,
                    _param._semester,
                    hrClass,
                    ""
            );
            _staffName = returnval.val3;
            _hrName = returnval.val1;
        }

        private String sqlHrclassStdList() {
            final String sql;
            sql = "SELECT"
                + "  schregno,"
                + "  attendno,"
                + "  name,"
                + "  ent_date,"
                + "  ent_div,"
                + "  grd_date,"
                + "  grd_div"
                + " FROM v_schreg_info"
                + " WHERE year='" + _param._year + "'"
                + " AND semester='" + _param.specialSemester() + "'"
                + " AND grade || hr_class = ?"
                + " ORDER BY attendno"
                ;
            return sql;
        }

        private void loadStudents(final DB2UDB db2) throws Exception {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sqlHrclassStdList());
                ps.setString(1, _code);

                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("schregno");
                    final String attendno = rs.getString("attendno");
                    final String name = rs.getString("name");
                    final Date entDate = rs.getDate("ent_date");
                    final String entDiv = rs.getString("ent_div");
                    final Date grdDate = rs.getDate("grd_date");
                    final String grdDiv = rs.getString("grd_div");
                    final Student student = new Student(schregno, attendno, name, this, entDate, entDiv, grdDate, grdDiv);
                    _students.add(student);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }

//            loadStudentsInfo(db2);
            loadStudentsScoreInfo(db2);
            loadTransfer(db2);
            loadAttend(db2);
        }

        private void loadTransfer(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            for (final Iterator it = _students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                final String sql = sqlTrans(student);
                try {
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String code = rs.getString("transfercd");
                        final String sDate = rs.getString("transfer_sdate");
                        if (null != code) {
                            student._trsDiv = code;
                            student._trsDate = sDate;
                        }
                    }
                } catch (final SQLException e) {
                    log.error("異動情報の取得でエラー", e);
                    throw e;
                }
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        private String sqlTrans(final Student student) {
            final String sql;
            sql = "SELECT t1.transfercd, t1.transfer_sdate"
                + " FROM schreg_transfer_dat t1, semester_mst t2"
                + " WHERE t2.year='" + _param._year + "'"
                + " AND t2.semester='" + _param._semester + "'"
                + " AND t1.schregno='" + student._code + "'"
                + " AND t2.edate BETWEEN t1.transfer_sdate AND t1.transfer_edate"
                ;
            return sql;
        }

//        private void loadStudentsInfo(
//                final DB2UDB db2
//        ) throws Exception {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//
//            try {
//                ps = db2.prepareStatement(sqlStdNameInfo());
//
//                for (final Iterator it = _students.iterator(); it.hasNext();) {
//                    final Student student = (Student) it.next();
//
//                    ps.setString(1, student.getCode());
//                    rs = ps.executeQuery();
//                    if (rs.next()) {
//                        final String transInfo = createTransInfo(rs);
//
//                        student.setInfo(
//                                rs.getString("ATTENDNO"),
//                                rs.getString("NAME"),
//                                transInfo
//                        );
//                    }
//                }
//            } finally {
//                db2.commit();
//                DbUtils.closeQuietly(null, ps, rs);
//            }
//        }
        
//        /**
//         * 任意の生徒の学籍情報を取得するSQL
//         *   SEMESTER_MSTは'指示画面指定学期'で検索 => 学年末'9'有り
//         *   SCHREG_REGD_DATは'指示画面指定学期'で検索 => 学年末はSCHREG_REGD_HDATの最大学期
//         */
//        private String sqlStdNameInfo() {
//            final StringBuffer stb = new StringBuffer();
//
//            stb.append("SELECT  W1.SCHREGNO, W1.ATTENDNO, W3.NAME, W6.HR_NAME, ");
//            stb.append(        "CASE WHEN W4.GRD_DATE IS NOT NULL THEN W4.GRD_DATE ELSE W4.ENT_DATE END AS KBN_DATE1, ");
//            stb.append(        "CASE WHEN W4.GRD_DATE IS NOT NULL THEN (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1 = 'A003' AND S1.NAMECD2 = W4.GRD_DIV) ");
//            stb.append(             "ELSE (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1 = 'A002' AND S1.NAMECD2 = W4.ENT_DIV) END AS KBN_NAME1, ");
//            stb.append(        "W5.TRANSFER_SDATE AS KBN_DATE2,");
//            stb.append(        "(SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A004' AND S1.NAMECD2 = W5.TRANSFERCD) AS KBN_NAME2 ");
//            stb.append("FROM    SCHREG_REGD_DAT W1 ");
//            stb.append("INNER  JOIN SCHREG_REGD_HDAT  W6 ON W6.YEAR = '" + _param._year + "' AND W6.SEMESTER = W1.SEMESTER AND W6.GRADE = W1.GRADE AND W6.HR_CLASS = W1.HR_CLASS ");
//            stb.append("INNER  JOIN SEMESTER_MST    W2 ON W2.YEAR = '" + _param._year + "' AND W2.SEMESTER = '" + _param._semester + "' ");
//            stb.append("INNER  JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
//            stb.append("LEFT   JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
//            stb.append(                              "AND ((W4.GRD_DIV IN('2','3') AND W4.GRD_DATE < W2.EDATE) ");
//            stb.append(                                "OR (W4.ENT_DIV IN('4','5') AND W4.ENT_DATE > W2.SDATE)) ");
//            stb.append("LEFT   JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
//            stb.append(                                  "AND (W5.TRANSFERCD IN ('1','2') AND W2.EDATE BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE) ");
//            stb.append("WHERE   W1.YEAR = '" + _param._year + "' ");
//            stb.append(    "AND W1.SCHREGNO = ? ");
//            stb.append("AND W1.SEMESTER = '" + _param.specialSemester() + "' ");
//
//            return stb.toString();
//        }

//        private String createTransInfo(final ResultSet rs) {
//            try {
//                final String d1 = rs.getString("KBN_DATE1");
//                if (null != d1) {
//                    final String n1 = rs.getString("KBN_NAME1");
//                    return KNJ_EditDate.h_format_JP(d1) + n1;
//                }
//
//                final String d2 = rs.getString("KBN_DATE2");
//                if (null != d2) {
//                    final String n2 = rs.getString("KBN_NAME2");
//                    return KNJ_EditDate.h_format_JP(d2) + n2;
//                }
//            } catch (final SQLException e) {
//                 log.error("SQLException", e);
//            }
//            return "";
//        }

        private void loadStudentsScoreInfo(
                final DB2UDB db2
        ) throws Exception {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlStdTotalRank());
                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._code);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        student.setScoreInfo(
                                rs.getString("TOTAL"),
                                rs.getString("AVERAGE"),
                                rs.getString("RANK")
                        );
                    }
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
        
        /**
         * SQL 任意の生徒の順位を取得するSQL
         */
        private String sqlStdTotalRank() {
            final StringBuffer stb = new StringBuffer();

            stb.append("SELECT  W3.SCHREGNO ");
            stb.append(       ",W3.SCORE AS TOTAL ");
            stb.append(       ",DECIMAL(ROUND(FLOAT(W3.AVG)*10,0)/10,5,1) AS AVERAGE ");
            stb.append(       "," + _param._rankFieldName + " AS RANK ");
            stb.append(  "FROM  RECORD_RANK_DAT W3 ");
            stb.append( "WHERE  W3.YEAR = '" + _param._year + "' ");
            stb.append(   "AND  W3.SEMESTER = '" + _param._semester + "' ");
            stb.append(   "AND  W3.TESTKINDCD || W3.TESTITEMCD = '" + _param._testKindCd + "' ");
            stb.append(   "AND  W3.SUBCLASSCD = '999999' ");
            stb.append(   "AND  W3.SCHREGNO = ? ");

            return stb.toString();
        }

        private void loadAttend(final DB2UDB db2) throws Exception {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final Map paramMap = new HashMap();
                paramMap.put("useCurriculumcd", _param._useCurriculumcd);
                paramMap.put("useVirus", _param._useVirus);
                paramMap.put("useKoudome", _param._useKoudome);
                paramMap.put("DB2UDB", db2);

                final String attendSemesSql = AttendAccumulate.getAttendSemesSql(
                        _param._semesFlg,
                        _param._definecode,
                        _param._knjSchoolMst,
                        _param._year,
                        _param.SSEMESTER,
                        _param.specialSemester(),
                        (String) _param._hasuuMap.get("attendSemesInState"),
                        _param._periodInState,
                        (String) _param._hasuuMap.get("befDayFrom"),
                        (String) _param._hasuuMap.get("befDayTo"),
                        (String) _param._hasuuMap.get("aftDayFrom"),
                        (String) _param._hasuuMap.get("aftDayTo"),
                        (String) _param._grade,
                        _code.substring(2),
                        null,
                        "SEMESTER",
                        paramMap
                );
                ps = db2.prepareStatement(attendSemesSql);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    if (!_param._semester.equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    final Student student = getStudent(rs.getString("SCHREGNO"));
                    if (null == student) {
                        continue;
                    }
                    final AttendInfo attendInfo = new AttendInfo(
                            rs.getInt("LESSON"),
                            rs.getInt("MLESSON"),
                            rs.getInt("SUSPEND"),
                            rs.getInt("MOURNING"),
                            rs.getInt("SICK"),
                            rs.getInt("PRESENT"),
                            rs.getInt("LATE"),
                            rs.getInt("EARLY"),
                            rs.getInt("TRANSFER_DATE")
                    );
                    student._attendInfo = attendInfo;
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private void loadScoreDetail(final DB2UDB db2) throws Exception {
            PreparedStatement ps = null;
            ResultSet rs = null;

            final Map _subClasses = new HashMap();
            try {
                ps = db2.prepareStatement(sqlStdSubclassDetail());

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    int i = 1;
                    ps.setString(i++, student._code);

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        // if (null != rs.getString("ABSENT1")) log.info("schregno="+rs.getString("SCHREGNO") +"subclasscd="+rs.getString("SUBCLASSCD") +"absent1="+rs.getString("ABSENT1"));
                        final ScoreDetail detail = createScoreDetail(rs, _subClasses);
                        student._scoreDetails.add(detail);
                    }
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            _param.relateSubclass(_subClasses);
            //
            for (final Iterator it = _students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                student.relateScoreDetails(_subClasses, _param._replaceCombined);
            }
            
            try {
                final Map paramMap = new HashMap();
                paramMap.put("absenceDiv", "1");
                paramMap.put("useCurriculumcd", _param._useCurriculumcd);
                paramMap.put("useVirus", _param._useVirus);
                paramMap.put("useKoudome", _param._useKoudome);
                paramMap.put("DB2UDB", db2);

                final String sql = AttendAccumulate.getAttendSubclassSql(
                        _param._semesFlg,
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
                        _code.substring(0, 2),
                        _code.substring(2),
                        null,
                        paramMap
                        );
                log.debug(" attend sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    if (!"9".equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    final Student student = getStudent(rs.getString("SCHREGNO"));
                    if (student == null) {
                        continue;
                    }
                    
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    
                    ScoreDetail scoreDetail = null;
                    for (final Iterator it = student._scoreDetails.iterator(); it.hasNext();) {
                        final ScoreDetail scoreDetail1 = (ScoreDetail) it.next();
                        if (scoreDetail1._subClass._code.equals(rs.getString("SUBCLASSCD"))) {
                            scoreDetail = scoreDetail1;
                            break;
                        }
                    }
                    if (null == scoreDetail) {
//                        SubClass subClass = null;
//                        for (final Iterator it = _subclasses.keySet().iterator(); it.hasNext();) {
//                            final String subclasscd = (String) it.next();
//                            if (subclasscd.substring(1).equals(rs.getString("SUBCLASSCD"))) {
//                                subClass = (SubClass) _subclasses.get(subclasscd);
//                                scoreDetail = new ScoreDetail(subClass, null, null, null, null, null, null, null, null);
//                                student._scoreDetails.put(subclasscd, scoreDetail);
//                                break;
//                            }
//                        }
                        if (null == scoreDetail) {
//                            log.fatal(" no detail " + student._schregno + ", " + rs.getString("SUBCLASSCD"));
                            continue;
                        }
                    }

                    final String classCd = subclassCd == null || "".equals(subclassCd) ? "" : subclassCd.substring(0, 2);
                    if (classCd.compareTo(KNJDefineSchool.subject_U) <= 0 || classCd.equals(KNJDefineSchool.subject_T)) {
                        scoreDetail._jisu = Integer.valueOf(rs.getString("LESSON"));
                        if (scoreDetail.isRelateTo()) {
                            scoreDetail._absent = Double.valueOf(rs.getString("REPLACED_SICK"));
                        } else {
                            scoreDetail._absent = Double.valueOf(rs.getString("SICK2"));
                        }
                        final BigDecimal absenceHigh = new BigDecimal(StringUtils.defaultString(rs.getString("ABSENCE_HIGH"), "99"));
//                        final String absenceWarn = rs.getString("ABSENCE_WARN" + ("1".equals(_param._warnSemester) ? "" : _param._warnSemester));
//                        final String credits = rs.getString("CREDITS");
//                        if (_param._useAbsenceWarn && null != credits && null != absenceWarn) {
//                            scoreDetail._absenceHigh = scoreDetail._absenceHigh.subtract(new BigDecimal(Integer.parseInt(credits) * Integer.parseInt(absenceWarn)));
//                        }
                        scoreDetail._isOver = judgeOver(scoreDetail._absent, absenceHigh);
                        scoreDetail._enableCredit = enableCredit(scoreDetail._onRecordComp, scoreDetail._replaceMoto, scoreDetail._isOver, scoreDetail._compUncondition);
                    }
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        /**
         *  PrepareStatement作成 --> 成績・評定・欠課データの表
         *  2005/06/20 Modify ペナルティ欠課の算出式を修正
         */
        private String sqlStdSubclassDetail() {
            final StringBuffer stb = new StringBuffer();

            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append("SCHNO_A AS(");
            stb.append(     "SELECT  W1.SCHREGNO,W1.SEMESTER ");
            stb.append(            ",W1.GRADE,W1.COURSECD, W1.MAJORCD, W1.COURSECODE, W1.HR_CLASS ");  //NO010
            stb.append(     "FROM    SCHREG_REGD_DAT W1 ");
            if (_param.semesterGakunenMatu()) {
                stb.append(     "WHERE   W1.YEAR = '" + _param._year + "' ");
                stb.append(     "AND W1.SEMESTER = '" + _param._semeFlg + "' ");
            } else {
                stb.append(     "INNER   JOIN SEMESTER_MST W2 ON W2.YEAR = '" + _param._year + "' AND W2.SEMESTER = '" + _param._semester + "' ");
                stb.append(     "WHERE   W1.YEAR = '" + _param._year + "' ");
                stb.append(     "AND W1.SEMESTER = '" + _param._semester + "' ");
                stb.append(     "AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append(     "     WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append(         "AND W1.SCHREGNO = ? ");
            stb.append(     ") ");

            //対象講座の表
            stb.append(",CHAIR_A AS(");
            stb.append(     "SELECT W1.SCHREGNO, W2.CHAIRCD, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(         "W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || ");
            }
            stb.append(            "W2.SUBCLASSCD AS SUBCLASSCD, W2.SEMESTER, W1.APPDATE, W1.APPENDDATE ");
            stb.append(     "FROM   CHAIR_STD_DAT W1, CHAIR_DAT W2 ");
            stb.append(     "WHERE  W1.YEAR = '" + _param._year + "' ");
            stb.append(        "AND W2.YEAR = '" + _param._year + "' ");
            stb.append(        "AND W1.SEMESTER = W2.SEMESTER ");

            stb.append(    "AND W1.SEMESTER <= '" + _param._semester + "' ");
            stb.append(    "AND W2.SEMESTER <= '" + _param._semester + "' ");

            stb.append(        "AND W1.CHAIRCD = W2.CHAIRCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(        "AND (W2.CLASSCD <= '" + SUBJECT_U + "' OR W2.CLASSCD = '" + SUBJECT_T + "') ");
            } else {
                stb.append(        "AND (SUBSTR(W2.SUBCLASSCD,1,2) <= '" + SUBJECT_U + "' OR SUBSTR(W2.SUBCLASSCD,1,2) = '" + SUBJECT_T + "') ");
            }
            stb.append(        "AND EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO)");
            stb.append(     ")");

            //NO010
            stb.append(",CREDITS_A AS(");
            stb.append(    "SELECT  SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(         "CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
            stb.append(            "SUBCLASSCD AS SUBCLASSCD, CREDITS, COMP_UNCONDITION_FLG ");
            stb.append(    "FROM    CREDIT_MST T1, SCHNO_A T2 ");
            stb.append(    "WHERE   T1.YEAR = '" + _param._year + "' ");
            stb.append(        "AND T1.GRADE = T2.GRADE ");
            stb.append(        "AND T1.COURSECD = T2.COURSECD ");
            stb.append(        "AND T1.MAJORCD = T2.MAJORCD ");
            stb.append(        "AND T1.COURSECODE = T2.COURSECODE ");
            stb.append(") ");

            //成績データの表（通常科目）
            stb.append(",RECORD_REC AS(");
            stb.append(    "SELECT  W3.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(         "W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append(            "W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append(           ",W2.COMP_CREDIT ,W2.GET_CREDIT ,W2.CHAIRCD ");
            if (!_param._testKindCd.equals("9900")) {
                //中間・期末成績  NO024 Modify
                // fieldname:SEM?_XXXX_SCORE / fieldname2:SEM?_XXXX
                stb.append(       ",CASE WHEN W3.SCORE IS NOT NULL THEN RTRIM(CHAR(W3.SCORE)) ");
                stb.append(             "ELSE NULL END AS SCORE ");
                stb.append(       ",CASE WHEN W3.SCHREGNO IS NULL THEN RTRIM(CHAR(W3.SCORE)) ");
                stb.append(             "ELSE NULL END AS PATTERN_ASSESS ");
            } else {
                // 学年成績/学期成績
                stb.append(       ",CASE WHEN W3.SCHREGNO IS NULL THEN RTRIM(CHAR(W3.SCORE)) ");
                stb.append(             "ELSE NULL END AS SCORE ");
                stb.append(       ",CASE WHEN W3.SCORE IS NOT NULL THEN RTRIM(CHAR(W3.SCORE)) ");
                stb.append(             "ELSE NULL END AS PATTERN_ASSESS ");
            }
            stb.append(    "FROM    RECORD_RANK_DAT W3 ");
            stb.append(            "LEFT JOIN RECORD_SCORE_DAT W2 ON W2.YEAR = W3.YEAR ");
            stb.append(                                         "AND W2.SEMESTER = W3.SEMESTER ");
            stb.append(                                         "AND W2.TESTKINDCD = W3.TESTKINDCD ");
            stb.append(                                         "AND W2.TESTITEMCD = W3.TESTITEMCD ");
            stb.append(                                         "AND W2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append(                                         "AND W2.SCHREGNO = W3.SCHREGNO ");
            stb.append(    "WHERE   W3.YEAR = '" + _param._year + "' AND ");
            stb.append(            "W3.SEMESTER = '" + _param._semester + "' AND ");
            stb.append(            "W3.TESTKINDCD || W3.TESTITEMCD = '" + _param._testKindCd + "' AND ");
            stb.append(            "EXISTS(SELECT  'X' FROM SCHNO_A W1 WHERE W3.SCHREGNO = W1.SCHREGNO) ");
            stb.append(     ") ");

            //NO010
            stb.append(",CREDITS_UNCONDITION AS(");
            stb.append(    "SELECT  SCHREGNO, SUBCLASSCD, CREDITS ");
            stb.append(    "FROM    CREDITS_A T1 ");
            stb.append(    "WHERE   VALUE(T1.COMP_UNCONDITION_FLG,'0') = '1' ");
            stb.append(        "AND NOT EXISTS(SELECT 'X' FROM RECORD_REC T3 WHERE T3.SUBCLASSCD = T1.SUBCLASSCD AND T3.SCHREGNO = T1.SCHREGNO) ");
            stb.append(") ");

            //評価読替前科目の表 NO008 Build
            stb.append(",REPLACE_REC_MOTO AS(");
            stb.append(        "SELECT  ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(         "ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append(                 "ATTEND_SUBCLASSCD AS SUBCLASSCD ");
            stb.append(        "FROM    SUBCLASS_REPLACE_COMBINED_DAT T1 ");
            stb.append(        "WHERE   YEAR ='" + _param._year + "' AND REPLACECD = '1' ");
            stb.append(            "AND EXISTS(SELECT 'X' FROM CHAIR_A T2 WHERE T2.SUBCLASSCD = ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(         "T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append(                        "T1.ATTEND_SUBCLASSCD ");  //NO0017
            if (!_param.semesterGakunenMatu()) {
                stb.append("AND T2.SEMESTER = '" + _param._semester + "' ");
            }
            stb.append(")");
            stb.append(        "GROUP BY ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(         "T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append(                  "ATTEND_SUBCLASSCD ");
            stb.append(     ") ");
            //評価読替後科目の表 NO008 Build
            stb.append(",REPLACE_REC_SAKI AS(");
            stb.append(        "SELECT  ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(         "T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append(                "COMBINED_SUBCLASSCD AS SUBCLASSCD ");
            stb.append(        "FROM    SUBCLASS_REPLACE_COMBINED_DAT T1 ");
            stb.append(        "WHERE   YEAR ='" + _param._year + "' AND REPLACECD = '1' ");
            stb.append(            "AND EXISTS(SELECT 'X' FROM CHAIR_A T2 WHERE T2.SUBCLASSCD = ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(         "T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append(                        "T1.ATTEND_SUBCLASSCD ");  //NO0017
            if (!_param.semesterGakunenMatu()) {
                stb.append("AND T2.SEMESTER = '" + _param._semester + "' ");
            }
            stb.append(")");
            stb.append(        "GROUP BY ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(         "T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append(                  "COMBINED_SUBCLASSCD ");
            stb.append(     ") ");

            //評定読替え科目評定の表
//            if (_param.semesterGakunenMatu()) {
                stb.append(",REPLACE_REC AS(");
                stb.append(     "SELECT SCHREGNO, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(         "W2.COMBINED_CLASSCD || '-' || W2.COMBINED_SCHOOL_KIND || '-' || W2.COMBINED_CURRICULUM_CD || '-' || ");
                }
                stb.append(            "W2.COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(         "W2.ATTEND_CLASSCD || '-' || W2.ATTEND_SCHOOL_KIND || '-' || W2.ATTEND_CURRICULUM_CD || '-' || ");
                }
                stb.append(            "W2.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, ");
                stb.append(            "SCORE, ");
                stb.append(            "PATTERN_ASSESS ");
                stb.append(            ",COMP_CREDIT,GET_CREDIT ");  //NO010
                stb.append(     "FROM   RECORD_REC W1, SUBCLASS_REPLACE_COMBINED_DAT W2 ");
                stb.append(     "WHERE  W1.SUBCLASSCD = ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(         "W2.ATTEND_CLASSCD || '-' || W2.ATTEND_SCHOOL_KIND || '-' || W2.ATTEND_CURRICULUM_CD || '-' || ");
                }
                stb.append(            "W2.ATTEND_SUBCLASSCD AND ");
                stb.append(            "W2.YEAR='" + _param._year + "' AND W2.REPLACECD='1' ");  //05/05/22
                stb.append(     ") ");

                stb.append(",REPLACE_REC_ATTEND AS(");
                stb.append(     "SELECT SCHREGNO, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(         "W2.COMBINED_CLASSCD || '-' || W2.COMBINED_SCHOOL_KIND || '-' || W2.COMBINED_CURRICULUM_CD || '-' || ");
                }
                stb.append(           "W2.COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(         "W2.ATTEND_CLASSCD || '-' || W2.ATTEND_SCHOOL_KIND || '-' || W2.ATTEND_CURRICULUM_CD || '-' || ");
                }
                stb.append(           "W2.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
                stb.append(     "FROM   RECORD_REC W1, SUBCLASS_REPLACE_COMBINED_DAT W2 ");
                stb.append(     "WHERE  W1.SUBCLASSCD = ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append(         "W2.ATTEND_CLASSCD || '-' || W2.ATTEND_SCHOOL_KIND || '-' || W2.ATTEND_CURRICULUM_CD || '-' || ");
                }
                stb.append(            "W2.ATTEND_SUBCLASSCD AND ");
                stb.append(            "EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO) AND ");
                stb.append(            "W2.YEAR='" + _param._year + "' AND W2.REPLACECD='1' ");
                stb.append(     ") ");
//             }

            //科目別平均
            stb.append(",RECORD_AVERAGE AS(");
            stb.append(    "SELECT  T2.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(         "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(            "T1.SUBCLASSCD AS SUBCLASSCD, DECIMAL(ROUND(FLOAT(T1.AVG)*10,0)/10,5,1) AS AVG ");
            stb.append(    "FROM    RECORD_AVERAGE_DAT T1, SCHNO_A T2 ");
            stb.append(    "WHERE   T1.YEAR = '" + _param._year + "' ");
            stb.append(        "AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append(        "AND T1.TESTKINDCD || T1.TESTITEMCD = '" + _param._testKindCd + "' ");
            stb.append(        "AND T1.AVG_DIV = '" + _param._avgDiv + "' ");
            stb.append(        "AND T1.GRADE = T2.GRADE ");
            if (_param._outputRank.equals("1")) {
                stb.append(    "AND T1.HR_CLASS = T2.HR_CLASS ");
            }
            if (_param._outputRank.equals("3")) {
                stb.append(    "AND T1.COURSECD = T2.COURSECD ");
                stb.append(    "AND T1.MAJORCD = T2.MAJORCD ");
                stb.append(    "AND T1.COURSECODE = T2.COURSECODE ");
            }
            stb.append(") ");

            //メイン表
            stb.append(" SELECT  T1.SUBCLASSCD,T1.SCHREGNO ");
            stb.append(        ",T3.SCORE ");
                                    //教科コード'90'も同様に評定をそのまま出力 NO015
            stb.append(        ",T3.PATTERN_ASSESS ");
            stb.append(        ",REPLACEMOTO ");
            stb.append(        ",CASE WHEN VALUE(T6.COMP_UNCONDITION_FLG,'0') = '1' AND T3.COMP_CREDIT IS NULL THEN T6.CREDITS ELSE T3.COMP_CREDIT END AS COMP_CREDIT ");  //NO0015  NO0018
            stb.append(        ",CASE WHEN VALUE(T6.COMP_UNCONDITION_FLG,'0') = '1' AND T3.GET_CREDIT IS NULL THEN T6.CREDITS ELSE T3.GET_CREDIT END AS GET_CREDIT ");  //NO0015  NO0018
            stb.append(        ",VALUE(T6.COMP_UNCONDITION_FLG,'0')AS COMP_UNCONDITION_FLG ");  //NO015
            stb.append(        ",T3.COMP_CREDIT AS ON_RECORD_COMP "); //NO0015 NO0018
            stb.append(        ",T7.SUBCLASSABBV AS SUBCLASSNAME ");
            stb.append(        ",T6.CREDITS ");
            stb.append(        ",T3.CHAIRCD ");
            stb.append(        ",CASE WHEN T3.SCHREGNO IS NOT NULL AND T3.SUBCLASSCD IS NOT NULL THEN T8.AVG ELSE NULL END AS AVG ");

            //対象生徒・講座の表
            stb.append("FROM(");
            stb.append(     "SELECT  W1.SCHREGNO,W2.SUBCLASSCD ");
            stb.append(     "FROM    CHAIR_STD_DAT W1, CHAIR_A W2, SCHNO_A W3 ");
            stb.append(     "WHERE   W1.YEAR = '" + _param._year + "' AND ");
            stb.append(             "W1.CHAIRCD = W2.CHAIRCD AND ");
            stb.append(             "W1.SEMESTER = W2.SEMESTER AND ");
            stb.append(             "W1.SCHREGNO = W3.SCHREGNO ");

            if (!_param.semesterGakunenMatu()) {
                stb.append(     "AND W2.SEMESTER = '" + _param._semester + "' ");
            }

            stb.append(     "GROUP BY W1.SCHREGNO,W2.SUBCLASSCD ");

//            if (_param.semesterGakunenMatu()) {
//                stb.append( "UNION   SELECT SCHREGNO,SUBCLASSCD ");
//                stb.append( "FROM    REPLACE_REC_ATTEND ");
//                stb.append( "GROUP BY SCHREGNO,SUBCLASSCD ");
//            }
//            //NO010
//            stb.append(     "UNION ");
//            stb.append(     "SELECT  SCHREGNO, SUBCLASSCD ");
//            stb.append(     "FROM    CREDITS_UNCONDITION S1 ");

            stb.append(")T1 ");

            //成績の表
            stb.append(  "LEFT JOIN(");
            //成績の表（通常科目）
            if (_param.semesterGakunenMatu()) {
                stb.append(     "SELECT W3.SCHREGNO, W3.SUBCLASSCD, W3.SCORE, W3.PATTERN_ASSESS, ");  //NO010
                stb.append(            "(SELECT  COUNT(*) ");
                stb.append(             "FROM    REPLACE_REC S1 ");
                stb.append(             "WHERE   S1.SCHREGNO = W3.SCHREGNO AND ");
                stb.append(                     "S1.ATTEND_SUBCLASSCD = W3.SUBCLASSCD ");
                stb.append(             "GROUP BY ATTEND_SUBCLASSCD) AS REPLACEMOTO ");
                stb.append(             ",COMP_CREDIT,GET_CREDIT ");  //NO010
                stb.append(             ",CHAIRCD ");
                stb.append(     "FROM   RECORD_REC W3 ");
                stb.append(     "WHERE  NOT EXISTS( SELECT 'X' FROM REPLACE_REC S2 WHERE W3.SUBCLASSCD = S2.SUBCLASSCD) ");
            } else {
                stb.append(     "SELECT W3.SCHREGNO, W3.SUBCLASSCD, W3.SCORE, W3.PATTERN_ASSESS, ");  //NO010
                stb.append(            "0 AS REPLACEMOTO ");
                stb.append(           ",COMP_CREDIT,GET_CREDIT ");  //NO010
                stb.append(           ",CHAIRCD ");
                stb.append(     "FROM   RECORD_REC W3 ");
            }
            if (_param.semesterGakunenMatu()) {
                                //評定読替科目 成績の表 NO006
                stb.append(     "UNION ALL ");
                stb.append(     "SELECT  W3.SCHREGNO, W3.SUBCLASSCD, ");
                stb.append(             "W3.SCORE AS SCORE, ");
                stb.append(             "W3.PATTERN_ASSESS, ");
                stb.append(             "-1 AS REPLACEMOTO ");
                stb.append(             ",COMP_CREDIT,GET_CREDIT ");  //NO010
                stb.append(             ",CASE WHEN VALUE(CHAIRCD,'') = '' THEN NULL ELSE CHAIRCD END AS CHAIRCD ");
                stb.append(     "FROM   RECORD_REC W3 ");
                stb.append(     "WHERE  EXISTS( SELECT 'X' FROM REPLACE_REC S2 WHERE W3.SUBCLASSCD = S2.SUBCLASSCD) ");
            }
            stb.append(     ")T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD AND T3.SCHREGNO = T1.SCHREGNO ");

            stb.append("LEFT JOIN CREDITS_A T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.SUBCLASSCD = T1.SUBCLASSCD ");  //NO010
            stb.append("LEFT JOIN SUBCLASS_MST T7 ON ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(         "T7.CLASSCD || '-' || T7.SCHOOL_KIND || '-' || T7.CURRICULUM_CD || '-' || ");
            }
            stb.append(                              "T7.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("LEFT JOIN RECORD_AVERAGE T8 ON T8.SCHREGNO = T1.SCHREGNO AND T8.SUBCLASSCD = T1.SUBCLASSCD ");

            stb.append("ORDER BY T1.SUBCLASSCD, T1.SCHREGNO");

            return stb.toString();
        }
        
        private SubClass getSubClass(final String code, final String abbv, final Map subClasses) {
            if (subClasses.containsKey(code)) {
                return (SubClass) subClasses.get(code);
            }
            final SubClass subClass = new SubClass(code, abbv);
            subClasses.put(code, subClass);
            return subClass;
        }

        private ScoreDetail createScoreDetail(final ResultSet rs, final Map subClasses) throws SQLException {
            // 科目
            final String subClassCd        = rs.getString("SUBCLASSCD");
            final SubClass subClass        = getSubClass(subClassCd, rs.getString("SUBCLASSNAME"), subClasses);

            // 左下
            final ScoreValue score         = createScoreValue(subClassCd, rs.getString("SCORE"));
            final ScoreValue patternAssess = createScoreValue(subClassCd, rs.getString("PATTERN_ASSESS"));

            // 履修単位
            final Integer compCredit       = (Integer) rs.getObject("COMP_CREDIT");
            // 修得単位
            final Integer getCredit        = (Integer) rs.getObject("GET_CREDIT");
            // [履修単位数/修得単位数]が有効か? 
            final Integer onRecordComp     = (Integer) rs.getObject("ON_RECORD_COMP");
            final Integer replaceMoto      = (Integer) rs.getObject("REPLACEMOTO");
            final boolean compUncondition  = "1".equals(rs.getString("COMP_UNCONDITION_FLG"));

            // 単位数。CREDIT_MST.CREDITS。下段の真中
            final Integer credits          = (Integer) rs.getObject("CREDITS");
            // 科目別平均。下段の真中
            final String subAvg            = rs.getString("AVG");

            final String chairCd           = rs.getString("CHAIRCD");

            final ScoreDetail detail = new ScoreDetail(
                    subClass,
                    score,
                    patternAssess,
                    compCredit,
                    getCredit,
                    onRecordComp,
                    replaceMoto,
                    compUncondition,
                    credits,
                    subAvg,
                    chairCd
            );
            return detail;
        }
        
        private ScoreValue createScoreValue(final String subClassCd, final String strScore) {
            if (null == strScore) {
                return null;
            }

            if (_common instanceof CommonGrade) {
                String code = subClassCd.substring(0, 2);
                // 「総合的な学習の時間」は固定で対象とする
                if (SUBJECT_T.equals(code)) {
                    return null;
                }
                if ("1".equals(_param._useCurriculumcd) && "1".equals(_param._useClassDetailDat) && 4 == StringUtils.split(subClassCd, "-").length) {
                    final String[] split = StringUtils.split(subClassCd, "-");
                    code = split[0] + "-" + split[1];
                }
                if (_param._disableValueCd.contains(code)) {
                    return null;
                }
            }
            return new ScoreValue(strScore);
        }

        /**
         * [履修単位数/修得単位数]が有効か?
         * @param onRecordComp
         * @param replaceMoto
         * @param isOver
         * @param compUnconditionFlg 単位マスタ.無条件フラグ。 true=無条件に単位マスタからの単位を表示する
         * @return [履修単位数/修得単位数]が有効ならtrue
         */
        private boolean enableCredit(final Integer onRecordComp, final Integer replaceMoto, final boolean isOver, final boolean compUnconditionFlg) {
            if (null == onRecordComp && compUnconditionFlg) {
                if (isOver) {
                    return false;
                }
            }

            if (null != replaceMoto && replaceMoto.intValue() >= 1) {
                return false;
            }
            return true;
        }

        private boolean judgeOver(final Double absent, final BigDecimal absenceHigh) {
            if (null == absent || null == absenceHigh) {
                return false;
            }
            if (0.1 > absent.floatValue() || 0 == absenceHigh.intValue()) {
                return false;
            }
            if (absenceHigh.floatValue() < absent.floatValue()) {
                return true;
            }
            return false;
        }

        private List createRanking() {
            final List list = new LinkedList();
            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                student._total = new Total(student);
                final Total total = student._total;
                if (0 < total._count) {
                    list.add(total);
                }
            }

            Collections.sort(list);
            return list;
        }

        int rank(final Student student) {
            final Total total = student._total;
            if (0 >= total._count) {
                return -1;
            }

            return 1 + _ranking.indexOf(total);
        }

        public int compareTo(final Object o) {
            if (!(o instanceof HRInfo)) return -1;
            final HRInfo that = (HRInfo) o;
            return _code.compareTo(that._code);
        }

        public String toString() {
            return _hrName + "[" + _staffName + "]";
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private class Student implements Comparable {
        private final String _code;
        private final HRInfo _hrInfo;

        private String _attendNo;
        private String _name;

        private String _scoreSum;
        private String _scoreAvg;
        private String _scoreRank;

        private final Date _entDate;
        private final String _entDiv;
        private final Date _grdDate;
        private final String _grdDiv;
        private String _trsDate;
        private String _trsDiv;

        private String _transInfo;
        private AttendInfo _attendInfo;

        private final List _scoreDetails = new LinkedList();
        private Total _total;

        Student(
                final String code,
                final String attendNo,
                final String name,
                final HRInfo hrInfo,
                final Date entDate,
                final String entDiv,
                final Date grdDate,
                final String grdDiv
        ) {
            _code = code;
            _attendNo = attendNo;
            _name = name;

            _entDate = entDate;
            _entDiv = entDiv;
            _grdDate = grdDate;
            _grdDiv = grdDiv;

            _hrInfo = hrInfo;
        }

//        void setInfo(
//                final String attendNo,
//                final String name,
//                final String tansInfo
//        ) {
//            _attendNo = attendNo;
//            _name = name;
//            _transInfo = tansInfo;
//        }
//
        void setScoreInfo(
                final String scoreSum,
                final String scoreAvg,
                final String scoreRank
        ) {
            _scoreSum = scoreSum;
            _scoreAvg = scoreAvg;
            _scoreRank = scoreRank;
        }

        public int compareTo(final Object o) {
            if (!(o instanceof Student)) {
                return -1;
            }
            final Student that = (Student) o;
            int rtn;
            rtn = _hrInfo.compareTo(that._hrInfo);
            if (0 != rtn) {
                return rtn;
            }
            rtn = _attendNo.compareTo(that._attendNo);
            return rtn;
        }

        int rank() {
            return _hrInfo.rank(this);
        }

        public String toString() {
            return _attendNo + ":" + _name;
        }

        int print(final int line) {
            final int usingLines = _scoreDetails.isEmpty() ? 1 : (int) Math.ceil((double) _scoreDetails.size() / MAX_COLUMN);
            final int yLine = line % MAX_LINE;
            int white = 0;
            if (0 != yLine) {
                final int y = yLine + usingLines;
                if (MAX_LINE < y) {
                    final int n = MAX_LINE - yLine;
                    white = n;

                    // ページ末までの、残り行数分を、改行(つまり、改ページ)
                    for (int i = 0; i < n; i++) {
                        _form.svf.VrEndRecord();
                    }
                }
            }

            final String transInfo = getTransInfo();
            _form.printStudent(_name, _attendNo, transInfo);

            _form.printStudentTotal(_scoreSum, _scoreAvg, _scoreRank);

            _form.printTotal(_total);
            _form.printAttendInfo(_attendInfo);

            int column = 0;
            for (final Iterator it = _scoreDetails.iterator(); it.hasNext();) {
                if (0 != column && 0 == column % MAX_COLUMN) {
                    // 一人で、二行以上を使う場合
                    _form.svf.VrEndRecord();
                }
                final ScoreDetail detail = (ScoreDetail) it.next();
                _form.printDetail(detail, column);
                column++;
            }
            return white + usingLines;
        }

        private String getTransInfo() {
            final String rtn;
            if (enableTrs()) {
                final Map map = (Map) _param._map.get("A004");
                rtn = KNJ_EditDate.h_format_JP(_trsDate) + map.get(_trsDiv);
            } else if (enableGrd()) {
                final Map map = (Map) _param._map.get("A003");
                final String hoge = _grdDate.toString();
                rtn = KNJ_EditDate.h_format_JP(hoge) + map.get(_grdDiv);
            } else if (enableEnt()) {
                final Map map = (Map) _param._map.get("A002");
                final String hoge = _entDate.toString();
                rtn = KNJ_EditDate.h_format_JP(hoge) + map.get(_entDiv);
            } else {
                return null;
            }

            log.debug("備考に関する情報。日付(区分)" + toString());
            log.debug("\t入学:" + _entDate + "(" + _entDiv + ")");
            log.debug("\t卒業:" + _grdDate + "(" + _grdDiv + ")");
            log.debug("\t異動:" + _trsDate + "(" + _trsDiv + ")");
            return rtn;
        }

        /**
         * 入学データは有効か?
         * @return 有効ならtrue
         */
        private boolean enableEnt() {
            if (null == _entDate) {
                return false;
            }
            if (!"4".equals(_entDiv) && !"5".equals(_entDiv)) {
                return false;
            }

            final Semester semes = (Semester) _param._semesterMap.get(_param._semester);
            final Date aaa = semes._sDate;    // 指定学期開始日
            if (_entDate.compareTo(aaa) < 0) { // _entDate < aaa
                return false;
            }

            return true;
        }

        /**
         * 卒業データは有効か?
         * @return 有効ならtrue
         */
        private boolean enableGrd() {
            if (null == _grdDate) {
                return false;
            }
            if (!"2".equals(_grdDiv) && !"3".equals(_grdDiv)) {
                return false;
            }

            final Semester semes = (Semester) _param._semesterMap.get(_param._semester);
            final Date aaa = semes._eDate;    // 指定学期終了日
            if (_grdDate.compareTo(aaa) > 0) { // _grdDate > aaa
                return false;
            }

            return true;
        }

        /**
         * 異動データは有効か?
         * @return 有効ならtrue
         */
        private boolean enableTrs() {
            if (null == _trsDate) {
                return false;
            }
            if (!"1".equals(_trsDiv) && !"2".equals(_trsDiv)) {
                return false;
            }
            return true;
        }

        void relateScoreDetails(final Map subClasses, final Set replaceCombined) {
            for (final Iterator it = replaceCombined.iterator(); it.hasNext();) {
                final ReplaceCombined aaa = (ReplaceCombined) it.next();

                // 先も元も存在するか？
                final ScoreDetail saki = find(aaa._combined);
                final ScoreDetail moto = find(aaa._attend);
                if (null == saki || null == moto) {
                    log.warn("合併先、元のいづれかがnull:" + saki + ", " + moto);
                    continue;
                }

                // 合併先から見た元への関連付け
                saki._attendScoreDetails.add(moto);
                saki._fixed = aaa._fixed;

                // 合併元から見た先への関連付け
                moto._combined = saki;
            }
        }

        private ScoreDetail find(final String subClassCd) {
            for (final Iterator it = _scoreDetails.iterator(); it.hasNext();) {
                final ScoreDetail sd = (ScoreDetail) it.next();
                if (subClassCd.equals(sd._subClass._code)) {
                    return sd;
                }
            }
            return null;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private class AttendInfo {
        private final int _lesson;
        private final int _mLesson;
        private final int _suspend;
        private final int _mourning;
        private final int _absent;
        private final int _present;
        private final int _late;
        private final int _early;
        private final int _transDays;

        private AttendInfo(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int absent,
                final int present,
                final int late,
                final int early,
                final int transDays
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
            _transDays = transDays;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private class SubClass {
        private final String _code;
        private final String _abbv;

        /** 合併先 */
        private SubClass _combined;
        /** 合併元 */
        private Set _attendSubClasses = new HashSet();
        /** 単位固定/加算フラグ(合併先科目の単位数取得方法)。<br>true:固定  false:加算 */
        private boolean _fixed;

        SubClass(final String code, final String abbv) {
            _code = code;
            _abbv = abbv;
        }

        public String toString() {
            return _code + ":" + _abbv;
        }

        public boolean equals(final Object obj) {
            if (!(obj instanceof SubClass)) {
                return false;
            }
            final SubClass that = (SubClass) obj;
            return _code.equals(that._code);
        }

        public int hashCode() {
            return _code.hashCode();
        }

        /**
         * 合併先か?
         */
        boolean isRelateTo() {
            // 「合併元を持っている」という事は「先」である。
            return _attendSubClasses.size() > 0;
        }

        /**
         * 合併元か?
         */
        boolean isRelateFrom() {
            // 「合併先を持っている」という事は「元」である。
            return null != _combined;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private static class ScoreValue {
        private final String _strScore;
        private final boolean _isInt;
        private int _val;

        ScoreValue(final String strScore) {
            _strScore = strScore;
            _isInt = !StringUtils.isEmpty(_strScore) && StringUtils.isNumeric(_strScore);
            if (_isInt) {
                _val = Integer.parseInt(_strScore);
            }
        }

        static ScoreValue create(final String strScore) {
            if (null == strScore) {
                return null;
            }
            return new ScoreValue(strScore);
        }

        String getScore() { return _strScore; }
        boolean hasIntValue() { return _isInt; }
        int getScoreAsInt() { return _val; }
    }

    //--- 内部クラス -------------------------------------------------------
    private class ScoreDetail {
        private final SubClass _subClass;
        /** 欠課 */
        private Double _absent;
        /** 総時数 */
        private Integer _jisu;

        /** 中間・期末の時の下左 */
        private final ScoreValue _score;
        /** 学年・学期の時の下左 */
        private final ScoreValue _patternAssess;

        /** 履修単位数 */
        private final Integer _compCredit;
        /** 修得単位数 */
        private final Integer _getCredit;

        private final Integer _onRecordComp;
        private final Integer _replaceMoto;
        private final boolean _compUncondition;

        /** [履修単位数/修得単位数]が有効か? */
        private boolean _enableCredit;

        /** 単位数 */
        private final Integer _credits;
        /** 科目別平均 */
        private final String _subAvg;
        /** 欠課時数越えか? */
        private boolean _isOver;
        private final String _chaircd;

        /** 合併先 */
        private ScoreDetail _combined;
        /** 合併元 */
        private Set _attendScoreDetails = new HashSet();
        /** 単位固定/加算フラグ(合併先科目の単位数取得方法)。<br>true:固定  false:加算 */
        private boolean _fixed;

        ScoreDetail(
                final SubClass subClass,
                final ScoreValue score,
                final ScoreValue patternAssess,
                final Integer compCredit,
                final Integer getCredit,
                final Integer onRecordComp,
                final Integer replaceMoto,
                final boolean compUncondition,
                final Integer credits,
                final String subAvg,
                final String chaircd
        ) {
            _subClass = subClass;

            _score = score;
            _patternAssess = patternAssess;

            _compCredit = compCredit;
            _getCredit = getCredit;
            _onRecordComp = onRecordComp;
            _replaceMoto = replaceMoto;
            _compUncondition = compUncondition;
            _credits = credits;
            _subAvg = subAvg;
            _chaircd = chaircd;
        }

        Integer getCompCredit() {
            return _enableCredit ? _compCredit : null;
        }

        Integer getGetCredit() {
            return _enableCredit ? _getCredit : null;
        }

        /**
         * 合併先か?
         */
        boolean isRelateTo() {
            // 「合併元を持っている」という事は「先」である。
            return _attendScoreDetails.size() > 0;
        }

        /**
         * 合併元か?
         */
        boolean isRelateFrom() {
            // 「合併先を持っている」という事は「元」である。
            return null != _combined;
        }

        String printJisu() {
            if (isRelateTo()) {
                if (isTypeFix()) {
                    int total = 0;
                    for (final Iterator it = _attendScoreDetails.iterator(); it.hasNext();) {
                        final ScoreDetail sd = (ScoreDetail) it.next();
                        total += sd._jisu.intValue();
                    }
                    return String.valueOf(total);
                } else {
                    return "";
                }
            }

            return _jisu.toString();
        }

        String getCredits() {
            if (null == _credits) {
                return null;
            }

            if (isRelateFrom()) {
                return "(" + _credits.toString() + ")";
            }

            if (isRelateTo() && isTypeAdd()) {
                int total = 0;
                for (final Iterator it = _attendScoreDetails.iterator(); it.hasNext();) {
                    final ScoreDetail sd = (ScoreDetail) it.next();
                    total += sd._credits.intValue();
                }
                return String.valueOf(total);
            }

            return _credits.toString();
        }

        String getRecDatCredits() {
            if (null == _getCredit) {
                return null;
            }

            if (isRelateFrom()) {
                return "(" + _getCredit.toString() + ")";
            }

            return _getCredit.toString();
        }

        /**
         * 単位固定/加算フラグは固定タイプか?
         * @return 固定タイプなら true
         */
        public boolean isTypeFix() {
            return _fixed;
        }

        /**
         * 単位固定/加算フラグは加算タイプか?
         * @return 加算タイプなら true
         */
        public boolean isTypeAdd() {
            return !_fixed;
        }

        public String toString() {
            return _subClass.toString();
        }

        public boolean enableSubClass() {
            final String classcd = _subClass._code.substring(0, 2);
            return classcd.compareTo(SUBJECT_U) <= 0;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private class Total implements Comparable {
        private final Student _stundet;

        private int _total;
        private int _count;
        private BigDecimal _avgBigDecimal;

        /** 履修単位数 */
        private int _compCredit;
        /** 修得単位数 */
        private int _getCredit;

        Total(final Student student) {
            _stundet = student;
            compute();
        }

        private void compute() {
            final Common common = _common;

            int total = 0;
            int count = 0;

            int compCredit = 0;
            int getCredit = 0;

            for (final Iterator it = _stundet._scoreDetails.iterator(); it.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) it.next();

                if (common instanceof CommonGrade && detail.isRelateFrom() && null != detail._patternAssess) {
                    continue;
                }

                final ScoreValue scoreValue = common.getScoreValue(detail);
                if (enable(scoreValue) && detail.enableSubClass()) {
                    total += scoreValue.getScoreAsInt();
                    count++;
                }

                final Integer c = detail.getCompCredit();
                if (null != c) {
                    compCredit += c.intValue();
                }

                final Integer g = detail.getGetCredit();
                if (null != g) {
                    getCredit += g.intValue();
                }
            }

            _total = total;
            _count = count;
            if (0 < count) {
                final float avg = (float) round10(total, count);
                _avgBigDecimal = new BigDecimal(TOTAL_DEC_FMT.format(avg).toString());
            }
            _compCredit = compCredit;
            _getCredit = getCredit;
        }

        private boolean enable(final ScoreValue scoreValue) {
            return null != scoreValue && scoreValue.hasIntValue();
        }

        public int compareTo(final Object o) {
            if (!(o instanceof Total)) {
                return -1;
            }
            final Total that = (Total) o;

            return that._avgBigDecimal.compareTo(this._avgBigDecimal);
        }

        public boolean equals(final Object o) {
            if (!(o instanceof Total)) {
                return false;
            }
            final Total that = (Total) o;
            return that._avgBigDecimal.equals(this._avgBigDecimal);
        }

        public String toString() {
            return _avgBigDecimal.toString();
        }
    }

    private static class Param {
        private final String _year;

        /** 学期 */
        private final String _semester;
        /** LOG-IN時の学期（現在学期） */
        private final String _semeFlg;

        private final String _grade;
        private final String _testKindCd;
        /** 出欠集計日付 */
        private final String _date;
        /** 単位保留マークを付けるか? */
        private final boolean _creditDrop;
        /** 年組 */
        private final String[] _hrClasses;
        /** 総合順位出力フラグ */
        private final String _outputRank;
        /** 総合順位出力項目名 */
        private String _rankName;
        private String _rankFieldName;
        /** 科目別平均：区分 */
        private String _avgDiv;

        /** 最終集計日の翌日 */
        private String _divideAttendDate;
        /** ??? */
        private String _semesMonth;
        private String _semesterName;
        private String _semesterDateS;
        private String _yearDateS;
        private String _testName;

        /** 評定を無いものとして扱う教科コード */
        private Set _disableValueCd = new HashSet();

        /** 科目合併設定データ */
        private Set _replaceCombined = new HashSet();

        /** 名称マスタ */
        private Map _map = new HashMap();

        /** 学期 */
        private Map _semesterMap = new HashMap();   // TAKAESU: 整理せよ!

        /** ランクに平均の値を使用するか? */
        private boolean _outputKijunIsAvg;
        
        /** 教育課程コードを使用するか */
        private final String _useCurriculumcd;
        private final String _useClassDetailDat;
        private final String _useVirus;
        private final String _useKoudome;

        /** 出欠状況取得引数 */
        private String _periodInState;
        private Map _attendSemesMap;
        private Map _hasuuMap;
        private boolean _semesFlg;
        private String _sDate;
        private final String SSEMESTER = "1";

        private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス
        private KNJSchoolMst _knjSchoolMst;
        private DecimalFormat _absentFmt;
        private KNJ_Get_Info _getinfo = new KNJ_Get_Info();
        
        public Param(final HttpServletRequest request, final DB2UDB db2) {
            _testKindCd = request.getParameter("TESTKINDCD");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _semeFlg = request.getParameter("SEME_FLG");
            _grade = request.getParameter("GRADE");
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _hrClasses = request.getParameterValues("CLASS_SELECTED");
            _outputRank = request.getParameter("OUTPUT_RANK");
            final String outputKijun = request.getParameter("OUTPUT_KIJUN");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");

            _creditDrop = request.getParameter("OUTPUT4") != null;
            _outputKijunIsAvg = "2".equals(outputKijun);

            _definecode = createDefineCode(db2);
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            
            final KNJDivideAttendDate obj = new KNJDivideAttendDate();
            obj.getDivideAttendDate(db2, _year, _semester, _date);
            _divideAttendDate = obj.date;
            _semesMonth = retSemesterMonthValue(obj.month);

            setSemesters(db2);

            // 年度の開始日
            final KNJ_Get_Info.ReturnVal returnval1 = _getinfo.Semester(db2, _year, "9");
            _yearDateS = returnval1.val2;

            // 評定を無いものとして扱う教科コードを設定する
            setDisableValueCd(db2);

            // 科目合併設定データを読み込む
            setReplaceCombined(db2);

            // テスト名称を設定する
            setTestName(db2);

            // 総合順位出力：項目名を設定する
            setRankName();

            setNameMst(db2);

            loadAttendSemesArgument(db2);
        }
        
        private KNJDefineSchool createDefineCode(final DB2UDB db2) {
            final KNJDefineSchool definecode = new KNJDefineSchool();

            // 各学校における定数等設定
            definecode.defineCode(db2, _year);
            log.debug("semesdiv=" + definecode.semesdiv + "   absent_cov=" + definecode.absent_cov + "   absent_cov_late=" + definecode.absent_cov_late);

            switch (definecode.absent_cov) {
                case 0:
                case 1:
                case 2:
                    _absentFmt = new DecimalFormat("0");
                    break;
                default:
                    _absentFmt = new DecimalFormat("0.0");
            }

            return definecode;
        }

        public void setSemesters(final DB2UDB db2) {
            ResultSet rs = null;
            try {
                db2.query("SELECT * FROM semester_mst WHERE year='" + _year + "'");
                rs = db2.getResultSet();
                while (rs.next()) {
                    final String code = rs.getString("semester");
                    final String name = rs.getString("semestername");
                    final Date sDate = rs.getDate("sdate");
                    final Date eDate = rs.getDate("edate");

                    final Semester semester = new Semester(code, name, sDate, eDate);
                    _semesterMap.put(code, semester);

                    if (_semester.equals(code)) {
                        _semesterName = name;
                        _semesterDateS = sDate.toString();
                    }
                }
            } catch (final SQLException e) {
                log.error("学期マスタの読込みでエラー", e);
            }
            db2.commit();
            DbUtils.closeQuietly(null, null, rs);

            if (null == _semesterDateS) {
                _semesterDateS = _year + "-04-01";
            }
        }

        public void setNameMst(final DB2UDB db2) {
            final String[] namecd1 = {
                    "A002",
                    "A003",
                    "A004",
            };
            ResultSet rs = null;
            try {
                for (int i = 0; i < namecd1.length; i++) {
                    final Map map = new HashMap();

                    db2.query("SELECT namecd2, name1 FROM v_name_mst WHERE year='" + _year + "' AND namecd1='" + namecd1[i] + "'");
                    rs = db2.getResultSet();
                    while (rs.next()) {
                        final String namecd2 = rs.getString("namecd2");
                        final String name1 = rs.getString("name1");
                        map.put(namecd2, name1);
                    }
                    
                    _map.put(namecd1[i], map);
                }
            } catch (final SQLException e) {
                log.error("名称マスタの読込みでエラー", e);
            }
            db2.commit();
            DbUtils.closeQuietly(null, null, rs);
        }

        /**
         * 科目合併設定データを読み込む
         */
        public void setReplaceCombined(final DB2UDB db2) {
            ResultSet rs = null;
            try {
                final String sql = sqlReplaceCombined();
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    final String combined = rs.getString("combined_subclasscd");
                    final String attend = rs.getString("attend_subclasscd");
                    final String calculateFlg = rs.getString("calculate_credit_flg");
                    final boolean fixed = "1".equals(calculateFlg) ? true : false;
                    _replaceCombined.add(new ReplaceCombined(combined, attend, fixed));
                }
            } catch (SQLException e) {
                log.error("評定を無いものとして扱う教科コードの取得エラー", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, null, rs);
            }
            log.info("科目合併設定の件数=" + _replaceCombined.size());
        }

        private String sqlReplaceCombined() {
            String sql;
            sql = "SELECT";
            if ("1".equals(_useCurriculumcd)) {
                sql += " combined_classcd || '-' || combined_school_kind || '-' || combined_curriculum_cd || '-' || ";
            }
            sql += " combined_subclasscd AS combined_subclasscd,";
            if ("1".equals(_useCurriculumcd)) {
                sql += " attend_classcd || '-' || attend_school_kind || '-' || attend_curriculum_cd || '-' || ";
            }
            sql += " attend_subclasscd AS attend_subclasscd,"
                + " calculate_credit_flg"
                + " FROM subclass_replace_combined_dat"
                + " WHERE YEAR='" + _year + "'"
                ;
            return sql;
        }

        /**
         * 科目の合併情報を関連付ける。
         * @param subClasses
         */
        public void relateSubclass(final Map map) {
            final Set removeRC = new HashSet();

            for (final Iterator it = _replaceCombined.iterator(); it.hasNext();) {
                final ReplaceCombined aaa = (ReplaceCombined) it.next();

                // 先も元も存在するか？
                final SubClass saki = (SubClass) map.get(aaa._combined);
                final SubClass moto = (SubClass) map.get(aaa._attend);
                if (null == saki || null == moto) {
                    log.warn("合併先、元のいづれかがnullなので無効:" + saki + ", " + moto);
                    removeRC.add(aaa);
                    continue;
                }

                // 合併先から見た元への関連付け
                saki._attendSubClasses.add(moto);
                saki._fixed = aaa._fixed;

                // 合併元から見た先への関連付け
                moto._combined = saki;
            }
            _replaceCombined.removeAll(removeRC);
        }

        public String getNendo() {
            final int year = Integer.parseInt(_year);
            return nao_package.KenjaProperties.gengou(year);
        }

        public boolean semesterGakunenMatu() {
            return _semester.equals("9");
        }

        public String specialSemester() {
            if (semesterGakunenMatu()) {
                return _semeFlg;
            } else {
                return _semester;
            }
        }

        /**
         * 評定を無いものとして扱う教科コードを設定する。
         */
        void setDisableValueCd(final DB2UDB db2) {
            // 名称マスタから取得する
            ResultSet rs = null;
            try {
                final String sql;
                if ("1".equals(_useCurriculumcd) && "1".equals(_useClassDetailDat)) {
                    sql = "SELECT classcd || '-' || school_kind AS namecd2 FROM class_detail_dat WHERE year='" + _year+ "' AND class_seq='003' ";
                } else {
                    sql = "SELECT namecd2 FROM v_name_mst WHERE year='" + _year+ "' AND namecd1='D008' AND namecd2 IS NOT NULL";
                }
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    _disableValueCd.add(rs.getString("namecd2"));
                }
            } catch (SQLException e) {
                log.error("評定を無いものとして扱う教科コードの取得エラー", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, null, rs);
            }

            log.debug("評定無し扱い教科コード=" + _disableValueCd);
        }

        /**
         * テスト名称を設定する。
         */
        void setTestName(final DB2UDB db2) {
            ResultSet rs = null;
            try {
                final String sql = "SELECT TESTITEMNAME "
                                 +   "FROM TESTITEM_MST_COUNTFLG_NEW "
                                 +  "WHERE YEAR = '" + _year + "' "
                                 +    "AND SEMESTER = '" + _semester + "' "
                                 +    "AND TESTKINDCD || TESTITEMCD = '" + _testKindCd + "' ";
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    _testName = rs.getString("TESTITEMNAME");
                }
            } catch (SQLException e) {
                log.error("テスト名称の取得エラー", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, null, rs);
            }
            log.debug("テスト名称=" + _testName);
        }

        /**
         * 総合順位出力：項目名を設定する。
         */
        public void setRankName() {
            if (_outputRank.equals("1")) {
                _rankName = "学級順位";
                if (_outputKijunIsAvg) {
                    _rankFieldName = "CLASS_AVG_RANK";
                } else {
                    _rankFieldName = "CLASS_RANK";
                }
                _avgDiv = "2";
            } else if (_outputRank.equals("2")) {
                _rankName = "学年順位";
                if (_outputKijunIsAvg) {
                    _rankFieldName = "GRADE_AVG_RANK";
                } else {
                    _rankFieldName = "GRADE_RANK";
                }
                _avgDiv = "1";
            } else {
                _rankName = "コース順位";
                if (_outputKijunIsAvg) {
                    _rankFieldName = "COURSE_AVG_RANK";
                } else {
                    _rankFieldName = "COURSE_RANK";
                }
                _avgDiv = "3";
            }
            log.debug("順位名称=" + _rankName);
        }
        
        private KNJDefineCode setClasscode0(final DB2UDB db2, final String year) {
            KNJDefineCode definecode = null;
            try {
                definecode = new KNJDefineCode();
                definecode.defineCode(db2, year);         //各学校における定数等設定
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!", ex);
            }
            return definecode;
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
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

        public void loadAttendSemesArgument(DB2UDB db2) {
            try {
                loadSemester(db2, _year);
                // 出欠の情報
                final KNJDefineCode definecode0 = setClasscode0(db2, _year);
                final String z010Name1 = setZ010Name1(db2);
                _periodInState = AttendAccumulate.getPeiodValue(db2, definecode0, _year, SSEMESTER, specialSemester());
                _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, _year);
                _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap, _sDate, _date); // _sDate: 年度開始日, _date: LOGIN_DATE
                _semesFlg = ((Boolean) _hasuuMap.get("semesFlg")).booleanValue();
                
                log.debug(" attendSemesMap = " + _attendSemesMap);
                log.debug(" hasuuMap = " + _hasuuMap);
                log.debug(" semesFlg = " + _semesFlg);
            } catch (Exception e) {
                log.debug("loadAttendSemesArgument exception", e);
            }
        }

        /**
         * 年度の開始日を取得する 
         */
        private void loadSemester(final DB2UDB db2, String year) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
            final List list = new ArrayList();
            try {
                ps = db2.prepareStatement(sqlSemester(year));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String name = rs.getString("SEMESTERNAME");
                    map.put(semester, name);

                    final String sDate = rs.getString("SDATE");
                    list.add(sDate);
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            if (!list.isEmpty()) {
                _sDate = (String) list.get(0);
            }
            log.debug("年度の開始日=" + _sDate);
        }
        
        private String sqlSemester(String year) {
            final String sql;
            sql = "select"
                + "   SEMESTER,"
                + "   SEMESTERNAME,"
                + "   SDATE"
                + " from"
                + "   SEMESTER_MST"
                + " where"
                + "   YEAR='" + year + "'"
                + " order by SEMESTER"
            ;
            return sql;
        }    
    }

    private class Form {
        final MyVrw32alp svf = new MyVrw32alp();

        MyVrw32alp svfInit(
                final HttpServletResponse response,
                final HttpServletRequest request
        ) throws IOException {
            svf.VrInit();

            response.setContentType("application/pdf");
            svf.VrSetSpoolFileStream(response.getOutputStream());
            svf.VrSetForm(FORM_FILE, 4);

            return svf;
        }

        void setHead1(final String staffName, final String hrName) {
            svf.VrsOut("STAFFNAME", staffName);  // 担任名
            svf.VrsOut("CLASSNAME", hrName);  // 組名称
        }

        void printStudent(String name, String attendNo, String remark) {
            svf.VrsOut("NAME",     name);    //氏名
            svf.VrsOut("ATTENDNO", attendNo);    //出席番号
            svf.VrsOut("REMARK",   remark); //備考
        }

        void printStudentTotal(String scoreSum, String scoreAvg, String scoreRank) {
            svf.VrsOut("TOTAL",     scoreSum);  //合計
            svf.VrsOut("AVERAGE",   scoreAvg);  //平均
            svf.VrsOut("RANK",      scoreRank); //順位
        }

        void printHeader() {
            svf.VrsOut("NENDO", _param.getNendo() + "年度");

            // 作成日(現在処理日)・出欠集計範囲の出力 05/05/22Modify
            try {
                final Date date = new Date();
        
                final SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
                final String nowNendo = nao_package.KenjaProperties.gengou(Integer.parseInt(sdfY.format(date)));
        
                final SimpleDateFormat sdf = new SimpleDateFormat("年M月d日H時m分");
                final String nowDate = sdf.format(date);
        
                svf.VrsOut("DATE", nowNendo + nowDate);
            } catch (final NumberFormatException e) {
                log.warn("処理日の算出にて変換エラー", e);
            }

            final String date_E = KNJ_EditDate.h_format_JP(_param._date);

            // 出欠集計範囲(欠課数の集計範囲)
            svf.VrsOut("DATE2", KNJ_EditDate.h_format_JP(_param._yearDateS) + FROM_TO_MARK + date_E);

            // 一覧表枠外の文言
            svf.VrAttribute("NOTE1", "Paint=(1,70,1),Bold=1");
            svf.VrsOut("NOTE1", " ");
            svf.VrsOut("NOTE2", "：欠課時数超過者");

            // 「出欠の記録」の日付範囲
            final String fromDate = KNJ_EditDate.h_format_JP(_param._semesterDateS);
            svf.VrsOut("DATE3", fromDate + FROM_TO_MARK + date_E);

            // 対象校時および名称取得  05/06/15
            _common.setHead2(svf);

            // 固定個所(詳細の凡例)
            svf.VrsOut("DETAIL1", "科目名");
            svf.VrsOut("DETAIL2", "総時数");
            svf.VrsOut("DETAIL2_1", "欠課");

            // 総合順位出力：項目名
            svf.VrsOut("T_RANK", _param._rankName);
        }

        void printTotal(final Total total) {
//            if (0 < total._count) {
//                svf.VrsOut("TOTAL", String.valueOf(total._total));  // 総合点
//                svf.VrsOut("AVERAGE", total._avgBigDecimal.toString());  // 平均点
//            }
//
//            final int rank = total._stundet.rank();
//            if (1 <= rank) {
//                svf.VrsOut("RANK", String.valueOf(rank));  // 順位
//            }

            final Common common = _common;
            if (common.hasCredit()) {
                svf.VrsOut("R_CREDIT", String.valueOf(total._compCredit));    // 履修単位数
                svf.VrsOut("C_CREDIT", String.valueOf(total._getCredit)); //修得単位数
            }
        }

        void printAttendInfo(final AttendInfo info) {
            svf.doSvfOutNonZero("LESSON",  info._lesson);       // 授業日数
            svf.doSvfOutNonZero("PRESENT", info._mLesson);      // 出席すべき日数
            svf.doSvfOutNonZero("SUSPEND", info._suspend);      // 出席停止
            svf.doSvfOutNonZero("KIBIKI",  info._mourning);     // 忌引
            svf.doSvfOutNonZero("ABSENCE", info._absent);       // 欠席日数
            svf.doSvfOutNonZero("ATTEND",  info._present);      // 出席日数
            svf.doSvfOutNonZero("LATE",    info._late);         // 遅刻回数
            svf.doSvfOutNonZero("LEAVE",   info._early);        // 早退回数
            svf.doSvfOutNonZero("ABROAD",  info._transDays);    // 留学等実績
        }

        void printDetail(final ScoreDetail detail, final int column) {
            final int j = (column % MAX_COLUMN) + 1;

            // 科目名
            svf.VrsOut("SUBJECT" + j, detail._subClass._abbv);
        
            // 総時数
            if (null != detail._jisu) {
                svf.VrsOut("TOTALLESSON" + j, detail.printJisu());
            }
        
            // 成績(素点/評定/評価)
            final ScoreValue grading = _common.getScoreValue(detail);
            if (null != grading) {
                if (_common.doPrintMark() && 1 == grading.getScoreAsInt()) {
                    svf.VrsOut("GRADING" + j, "*" + grading.getScore());
                } else {
                    svf.VrsOut("GRADING" + j, grading.getScore());
                }
            }
        
            if (_common instanceof CommonInter) {
                // 中間・期末は「平均」
                if (null != detail._subAvg) {
                    svf.VrsOut("CREDIT" + j, detail._subAvg);
                }
            } else if (_common instanceof CommonGakki) {
                // 学期は「単位マスタの単位数」
                svf.doSvfOutNonZero("CREDIT" + j, detail.getCredits());
            } else {
                // 学年は「成績データの単位数」
                svf.doSvfOutNonZero("CREDIT" + j, detail.getRecDatCredits());
            }

            // 欠課
            if (detail.isRelateTo() && detail.isTypeAdd()) {
                return;
            }
            if (null != detail._absent) {
                final int value = (int) Math.round(detail._absent.doubleValue() * 10.0);
                if (0 != value) {
                    if (detail._isOver) {
                        svf.VrAttribute("KEKKA" + j, "Paint=(2,70,1),Bold=1");
                    }
                    svf.VrsOut("KEKKA" + j, _param._absentFmt.format(detail._absent.floatValue()));
                    if (detail._isOver) {
                        svf.VrAttribute("KEKKA" + j, "Paint=(0,0,0),Bold=0");   //網掛けクリア
                    }
                }
            }
        }

        void closeSvf(final boolean hasData) {
            if (null == svf) {
                return;
            }
            if (!hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            final int ret = svf.VrQuit();
            log.info("===> VrQuit():" + ret);
        }
    }

    private static class ReplaceCombined {
        /** 合併先科目コード */
        private final String _combined;

        /** 合併元科目コード */
        private final String _attend;

        /** 単位固定/加算フラグ(合併先科目の単位数取得方法)。<br>true:固定  false:加算 */
        private final boolean _fixed;

        public ReplaceCombined(final String combined, final String attend, final boolean fixed) {
            _combined = combined;
            _attend = attend;
            _fixed = fixed;
        }
    }

    private static class Semester {
        private final String _code;
        private final String _name;
        private final Date _sDate;
        private final Date _eDate;

        public Semester(
                final String code,
                final String name,
                final Date sDate,
                final Date eDate
        ) {
            _code = code;
            _name = name;
            _sDate = sDate;
            _eDate = eDate;
        }

        public String toString() {
            return _code + "/" + _name + "/" + _sDate + "/" + _eDate;
        }
    }
}
