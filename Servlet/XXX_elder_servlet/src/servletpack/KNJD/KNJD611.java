// kanji=漢字
/*
 * $Id: f8561f6af5da73214c192089fad47000948e0323 $
 *
 * 作成日: 2007/05/14
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJC.KNJC053_BASE;
import servletpack.KNJC.KNJDivideAttendDate;
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.Vrw32alpWrap;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 成績一覧表（成績判定会議用）を印刷します。
 * 成績処理改訂版。
 * 成績判定会議用をBASEとし、従来の成績一覧表の成績処理改訂版の印刷処理を行います。
 * @author nakamoto
 * @version $Id: f8561f6af5da73214c192089fad47000948e0323 $
 */
public class KNJD611 {
    private static final Log log = LogFactory.getLog(KNJD611.class);

    private static final int MAX_COLUMN = 18;
    private static final int MAX_LINE = 50;
    private static final String FROM_TO_MARK = "\uFF5E";

    private String FORM_FILE;
    private KNJD065_COMMON _knjdobj;  //成績別処理のクラス
    private Manager _manager;
    private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス
    private KNJSchoolMst _knjSchoolMst;

    private KNJ_Get_Info _getinfo;
    
    private final DecimalFormat DEC_FMT1 = new DecimalFormat("0.0");
    private final DecimalFormat DEC_FMT2 = new DecimalFormat("0");

    // 出欠集計共通端数計算メソッド用引数
    private String _periodInState;
    private Map _attendSemesMap;
    private Map _hasuuMap;
    private boolean _semesFlg;
    private String _sDate;
    private final String SSEMESTER = "1";

    /**
     *  KNJD.classから最初に起動されます。
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        log.fatal("$Revision: 59214 $ $Date: 2018-03-20 10:59:45 +0900 (火, 20 3 2018) $"); // CVSキーワードの取り扱いに注意
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定

        DB2UDB db2 = null;
        Vrw32alpWrap svf = null;
        boolean hasData = false;
        try {
            // パラメータの取得
            final Param paramap = createParamap(request);

            // ＤＢ接続
            db2 = sd.setDb(request);
            if (sd.openDb(db2)) {
                log.error("db open error! ");
                return;
            }
            _definecode = createDefineCode(db2, paramap);
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, (String) paramap._year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }

            init(db2, paramap);
            loadAttendSemesArgument(db2, paramap);

            svf = new Vrw32alpWrap();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            svf.setKurikaeshiNum(MAX_COLUMN);
            svf.setFieldNum(MAX_LINE);
            sd.setSvfInit(request, response, svf);

            setHeader(paramap);
            final String[] hrclass = request.getParameterValues("CLASS_SELECTED");  //印刷対象HR組
            for (int h = 0; h < hrclass.length; h++) {
                paramap._hrClass = hrclass[h];  //HR組

                if (paramap._isOutputCourse) {
                    final List courses = createCourses(db2, paramap);
                    log.debug("コース数=" + courses.size());
                    
                    for (final Iterator it = courses.iterator(); it.hasNext();) {
                        final Course course = (Course) it.next();
                        paramap._courseCd = course._coursecd;
                        
                        final HRInfo hrInfo = query(db2, paramap, hrclass[h]);
                        
                        // 印刷処理
                        if (printMain(svf, paramap, hrInfo)) {
                            hasData = true;
                        }
                    }
                } else {
                    final HRInfo hrInfo = query(db2, paramap, hrclass[h]);
                    
                    // 印刷処理
                    if (printMain(svf, paramap, hrInfo)) {
                        hasData = true;
                    }
                }
            }
        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (null != db2) {
                sd.closeDb(db2);
            }
            if (null != svf) {
                sd.closeSvf(svf, hasData);
            }
        }
    }

    private List createCourses(final DB2UDB db2, final Param paramap) throws SQLException {
        final List rtn = new ArrayList();
        final String sql = sqlCourses(paramap);

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String hrclass = rs.getString("HR_CLASS");
                final String coursecd = rs.getString("COURSECD");
                final String name = rs.getString("COURSECODENAME");

                final Course course = new Course(
                        grade,
                        hrclass,
                        coursecd,
                        name
                );

                rtn.add(course);
            }
        } catch (final Exception ex) {
            log.error("コースのロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    private String sqlCourses(final Param paramap) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     W1.GRADE, ");
        stb.append("     W1.HR_CLASS, ");
        stb.append("     W1.COURSECD || W1.MAJORCD || W1.COURSECODE as COURSECD, ");
        stb.append("     L1.COURSECODENAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT W1 ");
        stb.append("     LEFT JOIN COURSECODE_MST L1 ON L1.COURSECODE=W1.COURSECODE ");
        stb.append(" WHERE ");
        stb.append("     W1.YEAR = '" + paramap._year + "' AND ");
        if (!paramap._semester.equals("9")) {
            stb.append(" W1.SEMESTER = '" + paramap._semester + "' AND ");
        } else {
            stb.append(" W1.SEMESTER = '" + paramap._semeFlg + "' AND ");
        }
        stb.append("     W1.GRADE || W1.HR_CLASS = '" + paramap._hrClass + "' ");
        stb.append(" GROUP BY ");
        stb.append("     W1.GRADE, ");
        stb.append("     W1.HR_CLASS, ");
        stb.append("     W1.COURSECD, ");
        stb.append("     W1.MAJORCD, ");
        stb.append("     W1.COURSECODE, ");
        stb.append("     L1.COURSECODENAME ");

        return stb.toString();
    }

    private class Course {
        private final String _grade;
        private final String _hrclass;
        private final String _coursecd;
        private final String _name;

        Course(
                final String grade,
                final String hrclass,
                final String coursecd,
                final String name
        ) {
            _grade = grade;
            _hrclass = hrclass;
            _coursecd = coursecd;
            _name = name;
        }

        public String toString() {
            return _coursecd + ":" + _name;
        }
    }

    private void init(
            final DB2UDB db2,
            final Param paramap
    ) {
        _getinfo = new KNJ_Get_Info();

        getParam2(db2, paramap);  //Map要素設定

        try {
            _knjdobj = createKnjd065Obj(db2,paramap);  //成績別処理クラスを設定するメソッド
        } catch (Exception e) {
            log.error("Exception", e);
        }
        log.fatal(_knjdobj);
        _manager = new Manager(_knjdobj);
    }

    private HRInfo query(
            final DB2UDB db2,
            final Param paramap,
            final String hrclass
    ) throws Exception {
        final HRInfo hrInfo = getHRInfo(paramap);
        hrInfo.load(db2, paramap);
        return hrInfo;
    }

    /**
     *  get parameter doGet()パラメータ受け取り
     *       2005/01/05 最終更新日付を追加(param[15])
     *       2005/05/22 学年・組を配列で受け取る
     *       2007/05/14 総合順位出力を追加
     */
    private Param createParamap(final HttpServletRequest request) {
        if (log.isDebugEnabled()) {
            final Enumeration enums = request.getParameterNames();
            while (enums.hasMoreElements()) {
                final String name = (String) enums.nextElement();
                final String[] values = request.getParameterValues(name);
                log.debug("parameter:name=" + name + ", value=[" + StringUtils.join(values, ',') + "]");
            }
        }
        final Param paramap = new Param(request);
        return paramap;
    }

    /**
     *  成績別処理クラス設定
     */
    private KNJD065_COMMON createKnjd065Obj(
            final DB2UDB db2,
            final Param paramap
    ) throws Exception {
        final String prgid = (String) paramap._prgId;
        if ("KNJD065".equals(prgid)) {
            FORM_FILE = "KNJD065.frm";
            // 成績会議判定資料　学期
            if (!paramap._semester.equals("9")) { return new KNJD065_GAKKI(db2,paramap); }
            // 成績会議判定資料　学年
            return  new KNJD065_GRADE(db2,paramap);
        }

//        FORM_FILE = "KNJD062.frm";
        FORM_FILE = "KNJD611.frm";
        final String testKindCd = (String) paramap._testKindCd;

        // 成績一覧表(KNJD062) 中間
        if ("0101".equals(testKindCd)) { return new KNJD062A_INTER(db2,paramap); }

        // 成績一覧表(KNJD062) 期末1 or 期末2
        if ("0201".equals(testKindCd) || "0202".equals(testKindCd)) { return new KNJD062A_TERM(db2,paramap); }

        // 成績一覧表(KNJD062) 学期
        if (!paramap._semester.equals("9")) { return new KNJD062A_GAKKI(db2,paramap); }

        // 成績一覧表(KNJD062) 学年
        return  new KNJD062A_GRADE(db2,paramap);
    }

    /*
     *  クラス内で使用する定数設定
     */
    private KNJDefineSchool createDefineCode(
            final DB2UDB db2,
            final Param paramap
    ) {
        final KNJDefineSchool definecode = new KNJDefineSchool();

        definecode.defineCode(db2, (String) paramap._year);         //各学校における定数等設定
        log.debug("semesdiv=" + definecode.semesdiv + "   absent_cov=" + definecode.absent_cov + "   absent_cov_late=" + definecode.absent_cov_late);

        return definecode;
    }

    /**
     * 学級ごとの印刷。
     * @param svf
     * @param paramap
     * @param hrInfo
     * @return
     */
    private boolean printMain(
            final Vrw32alpWrap svf,
            final Param paramap,
            final HRInfo hrInfo
    ) {
        boolean hasData = false;
        
        int first = -1;  // 生徒リストのインデックス
        int last = 0;  // 生徒リストのインデックス
        int page = 0;  // ページ
        for (final Iterator it = hrInfo.getStudents().iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            if (-1 == first) { first = hrInfo.getStudents().indexOf(student); }
            last = hrInfo.getStudents().indexOf(student);

            int nowpage = (int)Math.floor((double)student._gnum / (double)MAX_LINE);
            if (page != nowpage) {
                page = nowpage;
                List list = hrInfo.getStudents().subList(first, last);
                first = last;

                if (printSub(svf, paramap, hrInfo, list)) hasData = true;
            }
        }
        if (0 < last) {
            List list = hrInfo.getStudents().subList(first, last + 1);

            if (printSub(svf, paramap, hrInfo, list)) hasData = true;
        }
        
        return hasData;
    }
    
    /**
     * 学級ごと、生徒MAX行ごとの印刷。
     * @param svf
     * @param paramap
     * @param hrInfo：年組
     * @param stulist：List hrInfo._studentsのsublist
     * @return
     */
    private boolean printSub(
            final Vrw32alpWrap svf,
            final Param paramap,
            final HRInfo hrInfo,
            final List stulist
    ) {
        boolean hasData = false;

        int line = 0;  // 科目の列番号
        int num = 0;  // 前期課程・英語の科目数
        final int subclassesnum = hrInfo.getSubclasses().size();

        final Collection subClasses = hrInfo.getSubclasses().values();
        for (final Iterator it = subClasses.iterator(); it.hasNext();) {
            if (0 == line % MAX_COLUMN) {
                printSvfSetHead(svf, hrInfo, paramap);  // ヘッダー出力項目を設定および印字
                printStudentsName(svf, stulist);  // 生徒名等を印字
            }
            if (subclassesnum == line + 1 + num) { 
                printStudentsTotal(svf, stulist);
                hrInfo.print(svf, hrInfo);
            }  // 生徒別総合成績および出欠を印字

            final SubClass subclass = (SubClass) it.next();
            final int used = printSubclasses(svf, subclass, line, stulist);
            line += used;
            if (0 < used) { hasData = true; }
            if (0 == used) { num++; }
        }

        return hasData;
    }

    /**
     * 生徒の氏名・備考を印字
     * @param svf
     * @param hrInfo
     * @param stulist：List hrInfo._studentsのsublist
     */
    private void printStudentsName(
            final Vrw32alpWrap svf, 
            final List stulist
    ) {
        for (final Iterator it = stulist.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            student.printOnPage(svf);
        }
    }

    /**
     * 生徒別最終出力の処理
     * @param svf
     * @param stulist：List hrInfo._studentsのsublist
     */
    private void printStudentsTotal(
            final Vrw32alpWrap svf, 
            final List stulist
    ) {
        for (final Iterator it = stulist.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            student.printOnLastpage(svf);
        }
    }

    /**
     * 該当科目名および科目別成績等を印字する処理
     * @param svf
     * @param subclass
     * @param line：科目の列番号
     * @param stulist：List hrInfo._studentsのsublist
     * @return
     */
    private int printSubclasses(
            final Vrw32alpWrap svf,
            final SubClass subclass,
            final int line,
            final List stulist
    ) {
        subclass.print(svf, line);  // 該当科目名等を印字
        printStudentsSubclasses(svf, subclass, line, stulist);  // 生徒別該当科目成績を印字する処理
        if (!subclass._kaiwadiv) svf.VrEndRecord();
        return (subclass._kaiwadiv) ? 0 : 1;
    }
    
    /**
     * 該当科目の生徒別成績を印字する処理
     * @param svf
     * @param subclass
     * @param line：科目の列番号
     * @param stulist：List hrInfo._studentsのsublist
     */
    private void printStudentsSubclasses(
            final Vrw32alpWrap svf, 
            final SubClass subclass,
            final int line,
            final List stulist
    ) {
        for (final Iterator it = stulist.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            student.printScoreDetail(svf, subclass, line);
        }        
    }

    /**
     *  パラメータセット 2005/01/29
     *      param[15]:attend_semes_datの最終集計日の翌日をセット
     *      param[16]:attend_semes_datの最終集計学期＋月をセット
     *  2005/02/20 Modify getDivideAttendDateクラスより取得
     */
    private void getParam2(
            final DB2UDB db2,
            final Param paramap
    ) {
        final KNJDivideAttendDate obj = new KNJDivideAttendDate();
        obj.getDivideAttendDate(db2, (String) paramap._year, (String) paramap._semester, (String) paramap._date);
        paramap.DIVIDEATTENDDATE = obj.date;  //最終集計日の翌日
        paramap.DIVIDEATTENDMONTH = obj.month;  //最終集計学期＋月
        paramap.SEMES_MONTH = KNJC053_BASE.retSemesterMonthValue((String) paramap.DIVIDEATTENDMONTH);

        //  学期名称、範囲の取得
        final KNJ_Get_Info.ReturnVal returnval = _getinfo.Semester(db2, (String) paramap._year, (String) paramap._semester);
        paramap.SEMESTERNAME = returnval.val1;  //学期名称

        // 学期期間FROM
        if (null == returnval.val2) {
            paramap.SEMESTERDATE_S = (String) paramap._year + "-04-01";
        } else {
            paramap.SEMESTERDATE_S = returnval.val2;
        }

        // 年度の開始日
        final KNJ_Get_Info.ReturnVal returnval1 = _getinfo.Semester(db2, (String) paramap._year, "9");
        paramap.YEARDATE_S = returnval1.val2;

        // テスト名称
        final String testitemname = getTestName(db2, (String) paramap._year, (String) paramap._semester, (String) paramap._testKindCd);
        paramap.TESTITEMNAME = testitemname;
    }

    private String getTestName(
            final DB2UDB db2,
            final String year,
            final String semester,
            final String testcd
    ) {
        String testitemname = "";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = "SELECT TESTITEMNAME "
                             +   "FROM TESTITEM_MST_COUNTFLG_NEW "
                             +  "WHERE YEAR = '" + year + "' "
                             +    "AND SEMESTER = '" + semester + "' "
                             +    "AND TESTKINDCD || TESTITEMCD = '" + testcd + "' ";
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                testitemname = rs.getString("TESTITEMNAME");
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return testitemname;
    }

    /**
     * 見出し項目を設定します。
     * @param paramap
     */
    private void setHeader(final Param paramap) {

        // 年度
        final int year = Integer.parseInt((String) paramap._year);
        paramap.NENDO = nao_package.KenjaProperties.gengou(year) + "年度";

        // 作成日
        final StringBuffer stb = new StringBuffer();
        final Date date = new Date();
        final SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
        stb.append(nao_package.KenjaProperties.gengou(Integer.parseInt(sdfY.format(date))));
        final SimpleDateFormat sdf = new SimpleDateFormat("年M月d日H時m分");
        stb.append(sdf.format(date));
        paramap.NOW = stb.toString();

        // 出欠集計範囲(欠課数の集計範囲)
        final String date_E = KNJ_EditDate.h_format_JP((String) paramap._date);
        paramap.TERM_KEKKA = KNJ_EditDate.h_format_JP((String) paramap.YEARDATE_S) + FROM_TO_MARK + date_E;

        //「出欠の記録」の日付範囲
        final String fromDate = KNJ_EditDate.h_format_JP((String) paramap.SEMESTERDATE_S);
        paramap.TERM_ATTEND = fromDate + FROM_TO_MARK + date_E;
    }
    
    /*
     *  ページ見出し・初期設定
     */
    private void printSvfSetHead(
            final Vrw32alpWrap svf,
            final HRInfo hrInfo,
            final Param paramap
    ) {
        svf.VrSetForm(FORM_FILE, 4);  //SVF-FORM設定
        _knjdobj.printHeader(svf, hrInfo, paramap);
        _knjdobj.printHeaderOther(svf,paramap);
    }

    /**
     * SQL HR組の学籍番号を取得するSQL
     */
    private String sqlHrclassStdList(final Param paramap) {
        final StringBuffer stb = new StringBuffer();

        stb.append("SELECT  W1.SCHREGNO,W1.ATTENDNO ");
        stb.append("FROM    SCHREG_REGD_DAT W1 ");
        stb.append("INNER  JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
        stb.append("WHERE   W1.YEAR = '" + paramap._year + "' ");
        if (!paramap._semester.equals("9")) {
            stb.append(    "AND W1.SEMESTER = '" + paramap._semester + "' ");
        } else {
            stb.append(    "AND W1.SEMESTER = '" + paramap._semeFlg + "' ");
        }
        stb.append(    "AND W1.GRADE||W1.HR_CLASS = '" + paramap._hrClass + "' ");
        if (paramap._isOutputCourse) {
            stb.append(    "AND W1.COURSECD || W1.MAJORCD || W1.COURSECODE = '" + paramap._courseCd + "' ");
        }
        stb.append("ORDER BY W1.ATTENDNO");

        return stb.toString();
    }

    /**
     * SQL 任意の生徒の学籍情報を取得するSQL
     *   SEMESTER_MSTは'指示画面指定学期'で検索 => 学年末'9'有り
     *   SCHREG_REGD_DATは'指示画面指定学期'で検索 => 学年末はSCHREG_REGD_HDATの最大学期
     */
    private String sqlStdNameInfo(final Param paramap) {
        final StringBuffer stb = new StringBuffer();

        stb.append("SELECT  W1.SCHREGNO, W1.ATTENDNO, W3.NAME, W6.HR_NAME, ");
        stb.append(        "CASE WHEN W4.GRD_DATE IS NOT NULL THEN W4.GRD_DATE ELSE W4.ENT_DATE END AS KBN_DATE1, ");
        stb.append(        "CASE WHEN W4.GRD_DATE IS NOT NULL THEN (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1 = 'A003' AND S1.NAMECD2 = W4.GRD_DIV) ");
        stb.append(             "ELSE (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1 = 'A002' AND S1.NAMECD2 = W4.ENT_DIV) END AS KBN_NAME1, ");
        stb.append(        "W5.TRANSFER_SDATE AS KBN_DATE2,");
        stb.append(        "(SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A004' AND S1.NAMECD2 = W5.TRANSFERCD) AS KBN_NAME2 ");
        stb.append("FROM    SCHREG_REGD_DAT W1 ");
        stb.append("INNER  JOIN SCHREG_REGD_HDAT  W6 ON W6.YEAR = '" + paramap._year + "' AND W6.SEMESTER = W1.SEMESTER AND W6.GRADE = W1.GRADE AND W6.HR_CLASS = W1.HR_CLASS ");
        stb.append("INNER  JOIN SEMESTER_MST    W2 ON W2.YEAR = '" + paramap._year + "' AND W2.SEMESTER = '" + paramap._semester + "' ");
        stb.append("INNER  JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
        stb.append("LEFT   JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
        stb.append(                              "AND ((W4.GRD_DIV IN('2','3') AND W4.GRD_DATE < W2.EDATE) ");
        stb.append(                                "OR (W4.ENT_DIV IN('4','5') AND W4.ENT_DATE > W2.SDATE)) ");
        stb.append("LEFT   JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
        stb.append(                                  "AND (W5.TRANSFERCD IN ('1','2') AND W2.EDATE BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE) ");
        stb.append("WHERE   W1.YEAR = '" + paramap._year + "' ");
        stb.append(    "AND W1.SCHREGNO = ? ");
        if (!paramap._semester.equals("9")) {
            stb.append("AND W1.SEMESTER = '" + paramap._semester + "' ");
        } else {
            stb.append("AND W1.SEMESTER = '" + paramap._semeFlg + "' ");
        }

        return stb.toString();
    }

    /*
     *  PrepareStatement作成 --> 科目別平均の表
     */
//    private String sqlSubclassAverage(final Param paramap) {
//        final StringBuffer stb = new StringBuffer();
//
//        stb.append(" SELECT  SUBCLASSCD");
//        stb.append("        ," + _knjdobj._fieldChaircd + " AS CHAIRCD");
//        stb.append("        ,ROUND(AVG(FLOAT(" + _knjdobj._fieldname + "))*10,0)/10 AS AVG_SCORE");
//        stb.append(" FROM RECORD_DAT W1");
//        stb.append(" WHERE YEAR = '" + paramap._year + "'");
//        stb.append(" GROUP BY SUBCLASSCD");
//        stb.append("," + _knjdobj._fieldChaircd);
//        stb.append(" HAVING " + _knjdobj._fieldChaircd +" IS NOT NULL");
//
//        return stb.toString();
//    }

    /**
     * SQL 任意の生徒の順位を取得するSQL
     */
    private String sqlStdTotalRank(final Param paramap) {
        final StringBuffer stb = new StringBuffer();

        stb.append("WITH ");

        //対象生徒の表 クラスの生徒
        stb.append(" SCHNO_A AS(");
        stb.append("     SELECT  W1.SCHREGNO,W1.SEMESTER ");
        stb.append("            ,W1.GRADE,W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
        stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
        stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
        stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
        stb.append("                  ELSE 0 END AS LEAVE ");
        stb.append("     FROM    SCHREG_REGD_DAT W1 ");
        stb.append("     INNER JOIN SEMESTER_MST W2 ON W2.YEAR = '" + paramap._year + "' AND W2.SEMESTER = W1.SEMESTER ");
        //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
        stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
        stb.append("                  AND W3.GRD_DIV IN('2','3') ");
        stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + paramap._date + "' THEN W2.EDATE ELSE '" + paramap._date + "' END ");
        //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
        stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
        stb.append("                  AND W4.ENT_DIV IN('4','5') ");
        stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + paramap._date + "' THEN W2.EDATE ELSE '" + paramap._date + "' END ");
        //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
        stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
        stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
        stb.append("                  AND CASE WHEN W2.EDATE < '" + paramap._date + "' THEN W2.EDATE ELSE '" + paramap._date + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
        
        stb.append("     WHERE   W1.YEAR = '" + paramap._year + "' ");
        if ("9".equals(paramap._semester)) {
            stb.append( "    AND W1.SEMESTER = '" + paramap._semeFlg + "' ");
        } else {
            stb.append("     AND W1.SEMESTER = '" + paramap._semester + "' ");
            stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
            stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
        }
        stb.append("         AND W1.SCHREGNO = ? ");
        stb.append(") ");

        //メイン表
        stb.append("SELECT  W3.SCHREGNO ");
        if (paramap._totalRank.equals("1")) stb.append(   ",CLASS_RANK  AS TOTAL_RANK ");
        if (paramap._totalRank.equals("2")) stb.append(   ",GRADE_RANK  AS TOTAL_RANK ");
        if (paramap._totalRank.equals("3")) stb.append(   ",COURSE_RANK AS TOTAL_RANK ");
        stb.append(  "FROM  RECORD_RANK_DAT W3 ");
        stb.append( "WHERE  W3.YEAR = '" + paramap._year + "' ");
        stb.append(   "AND  W3.SEMESTER = '" + paramap._semester + "' ");
        stb.append(   "AND  W3.TESTKINDCD || W3.TESTITEMCD = '" + paramap._testKindCd + "' ");
        stb.append(   "AND  W3.SUBCLASSCD = '999999' ");
        stb.append(   "AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
        stb.append(                "WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");

        return stb.toString();
    }

    /**
     *  PrepareStatement作成 --> 成績・評定・欠課データの表
     */
    private String sqlStdSubclassDetail(final Param paramap, final String hrClassCd) {
        final StringBuffer stb = new StringBuffer();

        stb.append("WITH ");

        //対象生徒の表 クラスの生徒
        stb.append(" SCHNO_A AS(");
        stb.append("     SELECT  W1.SCHREGNO,W1.SEMESTER ");
        stb.append("            ,W1.GRADE,W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
        stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
        stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
        stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
        stb.append("                  ELSE 0 END AS LEAVE ");
        stb.append("     FROM    SCHREG_REGD_DAT W1 ");
        stb.append("     INNER JOIN SEMESTER_MST W2 ON W2.YEAR = '" + paramap._year + "' AND W2.SEMESTER = W1.SEMESTER ");
        //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
        stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
        stb.append("                  AND W3.GRD_DIV IN('2','3') ");
        stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + paramap._date + "' THEN W2.EDATE ELSE '" + paramap._date + "' END ");
        //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
        stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
        stb.append("                  AND W4.ENT_DIV IN('4','5') ");
        stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + paramap._date + "' THEN W2.EDATE ELSE '" + paramap._date + "' END ");
        //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
        stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
        stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
        stb.append("                  AND CASE WHEN W2.EDATE < '" + paramap._date + "' THEN W2.EDATE ELSE '" + paramap._date + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
        
        stb.append("     WHERE   W1.YEAR = '" + paramap._year + "' ");
        if ("9".equals(paramap._semester)) {
            stb.append( "    AND W1.SEMESTER = '" + paramap._semeFlg + "' ");
        } else {
            stb.append("     AND W1.SEMESTER = '" + paramap._semester + "' ");
            stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
            stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
        }
        stb.append("         AND W1.GRADE || W1.HR_CLASS = '" + hrClassCd + "' ");
        stb.append(") ");

        //対象講座の表
        stb.append(",CHAIR_A AS(");
        stb.append(     "SELECT W1.SCHREGNO, W2.CHAIRCD, ");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(              "W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
            stb.append(              "W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || ");
        }
        stb.append(            "W2.SUBCLASSCD AS SUBCLASSCD, W2.SEMESTER, W1.APPDATE, W1.APPENDDATE ");
        stb.append(     "FROM   CHAIR_STD_DAT W1, CHAIR_DAT W2 ");
        stb.append(     "WHERE  W1.YEAR = '" + paramap._year + "' ");
        stb.append(        "AND W2.YEAR = '" + paramap._year + "' ");
        stb.append(        "AND W1.SEMESTER = W2.SEMESTER ");
        stb.append(        "AND W1.SEMESTER <= '" + paramap._semester + "' ");
        stb.append(        "AND W2.SEMESTER <= '" + paramap._semester + "' ");
        stb.append(        "AND W1.CHAIRCD = W2.CHAIRCD ");
        stb.append(        "AND (SUBSTR(W2.SUBCLASSCD,1,2) <= '" + KNJDefineSchool.subject_U + "' OR SUBSTR(W2.SUBCLASSCD,1,2) = '" + KNJDefineSchool.subject_T + "') ");
        stb.append(        "AND EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO)");
        stb.append(     ")");

        // テスト項目マスタの集計フラグ
        stb.append(" , TEST_COUNTFLG AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.EXECUTEDATE, ");
        stb.append("         T1.PERIODCD, ");
        stb.append("         T1.CHAIRCD, ");
        stb.append("         '2' AS DATADIV ");
        stb.append("     FROM ");
        stb.append("         SCH_CHR_TEST T1, ");
        stb.append("         TESTITEM_MST_COUNTFLG_NEW T2 ");
        stb.append("     WHERE ");
        stb.append("         T2.YEAR       = T1.YEAR ");
        stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
        stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
        stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
        stb.append("         AND T2.COUNTFLG   = '0' ");
        stb.append(" ) ");

        //欠課数の表
        stb.append(",ATTEND_A AS(");
                              //出欠データより集計
        stb.append(          "SELECT S1.SCHREGNO, SUBCLASSCD, SEMESTER,");
        stb.append(                 "COUNT(*) ");
        if (!"1".equals(_knjSchoolMst._subOffDays)) {
            stb.append(             "- COUNT(IS_OFFDAYS) ");
        }
        stb.append(                 "AS JISU, ");
        stb.append(                 "SUM(CASE WHEN DI_CD IN('4','5','6','14','11','12','13'");
        if ("1".equals(_knjSchoolMst._subAbsent)) {
            stb.append(                                     ",'1','8'");
        }
        if ("1".equals(_knjSchoolMst._subSuspend)) {
            stb.append(                                     ",'2','9'");
        }
        if ("1".equals(_knjSchoolMst._subMourning)) {
            stb.append(                                     ",'3','10'");
        }
        if ("1".equals(_knjSchoolMst._subVirus)) {
            stb.append(                                     ",'19','20'");
        }
        stb.append(                                       ") ");
        if ("1".equals(_knjSchoolMst._subOffDays)) {
            stb.append(                                   "OR (IS_OFFDAYS IS NOT NULL)");
        }
        stb.append(                 " THEN 1 ELSE 0 END)AS ABSENT1, ");        
        stb.append(                 "SUM(CASE WHEN DI_CD IN('15','16','23','24') THEN SMALLINT(VALUE(L1.ABBV2, '1')) ELSE 0 END)AS LATE_EARLY ");
        stb.append(          "FROM ( SELECT T2.SCHREGNO, T2.SUBCLASSCD, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV, T5.DI_CD, T3.SCHREGNO AS IS_OFFDAYS ");
        stb.append(                 "FROM   SCH_CHR_DAT T1 ");
        stb.append(                        "INNER JOIN CHAIR_A T2 ON T1.SEMESTER = T2.SEMESTER ");
        stb.append(                             "AND T1.CHAIRCD = T2.CHAIRCD ");
        stb.append(                        "LEFT JOIN SCHREG_TRANSFER_DAT T3 ON T3.TRANSFERCD = '2' ");
        stb.append(                             "AND T2.SCHREGNO = T3.SCHREGNO ");
        stb.append(                             "AND T1.EXECUTEDATE BETWEEN T3.TRANSFER_SDATE AND T3.TRANSFER_EDATE ");
        stb.append(                        "LEFT JOIN ATTEND_DAT T5 ON T2.SCHREGNO = T5.SCHREGNO ");
        stb.append(                             "AND T1.EXECUTEDATE = T5.ATTENDDATE ");
        stb.append(                             "AND T1.PERIODCD = T5.PERIODCD ");
        stb.append(                 "WHERE  T1.EXECUTEDATE BETWEEN '" + paramap.DIVIDEATTENDDATE + "' AND '" + paramap._date + "' ");
        stb.append(                    "AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
        stb.append(                    "AND T1.YEAR = '" + paramap._year + "' ");
        stb.append(                    "AND T1.SEMESTER <= '" + paramap._semester + "' ");
        stb.append(                    "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T4 ");  //NO025
        stb.append(                                   "WHERE   T4.SCHREGNO = T2.SCHREGNO ");  //NO025
        stb.append(                                       "AND (( ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE ) ");  //NO025
        stb.append(                                         "OR ( GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE )) ) ");  //NO025
        stb.append(                    "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");  //NO025
        stb.append(                                   "WHERE   T4.SCHREGNO = T2.SCHREGNO ");  //NO025
        stb.append(                                       "AND TRANSFERCD IN('1') AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ) ");  //NO025
        if (_definecode.useschchrcountflg) {
            stb.append(                "AND NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T4 ");
            stb.append(                               "WHERE   T4.EXECUTEDATE = T1.EXECUTEDATE AND ");
            stb.append(                                       "T4.PERIODCD = T1.PERIODCD AND ");
            stb.append(                                       "T4.CHAIRCD = T1.CHAIRCD AND ");
            stb.append(                                       "T1.DATADIV IN ('0', '1') AND ");
            stb.append(                                       "T4.GRADE||T4.HR_CLASS = '" + hrClassCd + "' AND ");
            stb.append(                                       "T4.COUNTFLG = '0') ");
            stb.append("                AND NOT EXISTS(SELECT 'X' FROM TEST_COUNTFLG TEST ");
            stb.append("                               WHERE ");
            stb.append("                                       TEST.EXECUTEDATE  = T1.EXECUTEDATE ");
            stb.append("                                       AND TEST.PERIODCD = T1.PERIODCD ");
            stb.append("                                       AND TEST.CHAIRCD  = T1.CHAIRCD ");
            stb.append("                                       AND TEST.DATADIV  = T1.DATADIV) ");
        }
        stb.append(                 "GROUP BY T2.SCHREGNO, T2.SUBCLASSCD, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV, T5.DI_CD, T3.SCHREGNO ");
        stb.append(               ")S1 ");
        stb.append(               "LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'C001' AND L1.NAMECD2 = S1.DI_CD ");
        stb.append(          "GROUP BY S1.SCHREGNO, SUBCLASSCD, SEMESTER ");

                              //月別科目別出欠集計データより欠課を取得
        stb.append(          "UNION ALL ");
        stb.append(          "SELECT  W1.SCHREGNO, ");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(              "W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
        }
        stb.append(                  "W1.SUBCLASSCD AS SUBCLASSCD, W1.SEMESTER, ");
        stb.append(                  "SUM(VALUE(LESSON,0) ");
        if (!"1".equals(_knjSchoolMst._subOffDays)) {
            stb.append(               " - VALUE(OFFDAYS,0)");
        }
        stb.append(                   " - VALUE(ABROAD,0) ) AS JISU, ");
        
        stb.append(                  "SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF,0)");
        if ("1".equals(_knjSchoolMst._subAbsent)) {
            stb.append(              "+ VALUE(ABSENT,0)");
        }
        if ("1".equals(_knjSchoolMst._subSuspend)) {
            stb.append(              "+ VALUE(SUSPEND,0)");
        }
        if ("1".equals(_knjSchoolMst._subMourning)) {
            stb.append(              "+ VALUE(MOURNING,0)");
        }
        if ("1".equals(_knjSchoolMst._subOffDays)) {
            stb.append(              "+ VALUE(OFFDAYS,0)");
        }
        if ("1".equals(_knjSchoolMst._subVirus)) {
            stb.append(              "+ VALUE(VIRUS,0)");
        }
        stb.append(                  ") AS ABSENT1, ");
        stb.append(                  "SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY ");
        stb.append(          "FROM    ATTEND_SUBCLASS_DAT W1, SCHNO_A W2 ");
        stb.append(          "WHERE   W1.YEAR = '" + paramap._year + "' AND ");
        stb.append(              "W1.SEMESTER <= '" + paramap._semester + "' AND ");

        stb.append(                  "(CASE WHEN INT(W1.MONTH) < 4 THEN RTRIM(CHAR(INT(W1.SEMESTER) + 1 )) ELSE W1.SEMESTER END )||W1.MONTH <= '" + paramap.SEMES_MONTH + "' AND ");   //--NO004 NO007
        stb.append(                  "W1.SCHREGNO = W2.SCHREGNO ");
        stb.append(          "GROUP BY W1.SCHREGNO, ");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(              "W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
        }
        stb.append(                  "W1.SUBCLASSCD, W1.SEMESTER ");
        stb.append(     ") ");

        //欠課数の表
        stb.append(",ATTEND_B AS(");
        stb.append(         "SELECT  SCHREGNO, SUBCLASSCD ");

        if (_definecode.absent_cov == 1 || _definecode.absent_cov == 3) {
        //遅刻・早退を学期で欠課換算する
            stb.append(           ", SUM(ABSENT1)AS ABSENT1 ");
            stb.append(     "      , SUM(JISU)AS JISU ");
            stb.append(     "FROM   (SELECT  SCHREGNO, SUBCLASSCD, SUM(JISU)AS JISU, ");
            if (_definecode.absent_cov == 1 || paramap._semester.equals("9")) {
                stb.append(                 "VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + _definecode.absent_cov_late + " AS ABSENT1 ");   //05/06/20Modify
            } else {
                stb.append(                 "FLOAT(DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(FLOAT(LATE_EARLY)),0) / " + _definecode.absent_cov_late + ",5,1)) AS ABSENT1 ");   //05/06/20Modify
            }
            stb.append(             "FROM    ATTEND_A ");
            stb.append(             "GROUP BY SCHREGNO, SUBCLASSCD, SEMESTER ");
            stb.append(             ")W1 ");
            stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
        } else if (_definecode.absent_cov == 2 || _definecode.absent_cov == 4) {
            //遅刻・早退を年間で欠課換算する
            if (_definecode.absent_cov == 2) {
                stb.append(       ", VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + _definecode.absent_cov_late + " AS ABSENT1 ");   //05/06/20Modify
            } else {
                stb.append(       ", FLOAT(DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(FLOAT(LATE_EARLY)),0) / " + _definecode.absent_cov_late + ",5,1)) AS ABSENT1 ");   //05/06/20Modify
            }
            stb.append(     "      , SUM(JISU)AS JISU ");
            stb.append(     "FROM    ATTEND_A ");
            stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
        } else {
            //遅刻・早退を欠課換算しない
            stb.append(     "      , SUM(ABSENT1)AS ABSENT1 ");
            stb.append(     "      , SUM(JISU)AS JISU ");
            stb.append(     "FROM    ATTEND_A W1 ");
            stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
        }
        stb.append(     ") ");

        //合併科目の欠課数の表
        stb.append(", ATTEND_C AS(");
//        stb.append("   SELECT  SUBCLASSCD,ABSENT1,JISU");
//        stb.append("   FROM    ATTEND_B T1");
//        stb.append("   WHERE   NOT EXISTS(SELECT 'X' FROM   SUBCLASS_REPLACE_COMBINED_DAT T2");
//        stb.append("                      WHERE  T2.YEAR = '" + paramap._year + "'");
//        stb.append("                         AND T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD)");
        stb.append("   SELECT  T1.SCHREGNO, ");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(             "T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' || ");
        }
        stb.append(             "T2.COMBINED_SUBCLASSCD AS SUBCLASSCD, SUM(T1.ABSENT1) AS ABSENT1, SUM(T1.JISU) AS JISU");
        stb.append("   FROM    ATTEND_B T1, SUBCLASS_REPLACE_COMBINED_DAT T2");
        stb.append("   WHERE   T2.YEAR = '" + paramap._year + "'");
        stb.append("       AND ");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(              "T2.ATTEND_CLASSCD || '-' || T2.ATTEND_SCHOOL_KIND || '-' || T2.ATTEND_CURRICULUM_CD || '-' || ");
        }
        stb.append(             "T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD");
        stb.append("   GROUP BY T1.SCHREGNO, ");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(             "T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' || ");
        }
        stb.append(             "T2.COMBINED_SUBCLASSCD");
        stb.append(" ) ");

        //NO010
        stb.append(",CREDITS_A AS(");
        stb.append(    "SELECT  SCHREGNO, ");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(              "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append(            "SUBCLASSCD AS SUBCLASSCD, CREDITS ");
        stb.append(    "FROM    CREDIT_MST T1, SCHNO_A T2 ");
        stb.append(    "WHERE   T1.YEAR = '" + paramap._year + "' ");
        stb.append(        "AND T1.GRADE = T2.GRADE ");
        stb.append(        "AND T1.COURSECD = T2.COURSECD ");
        stb.append(        "AND T1.MAJORCD = T2.MAJORCD ");
        stb.append(        "AND T1.COURSECODE = T2.COURSECODE ");
        stb.append(        "AND EXISTS(SELECT 'X' FROM CHAIR_A T3 WHERE T3.SUBCLASSCD = ");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(              "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append(                  "T1.SUBCLASSCD)");
        stb.append(") ");

        // 単位数の表
        stb.append(",CREDITS_B AS(");
        stb.append("    SELECT  T1.SUBCLASSCD, T1.CREDITS");
        stb.append("    FROM    CREDITS_A T1");
        stb.append("    WHERE   NOT EXISTS(SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT T2 ");
        stb.append("                       WHERE  T2.YEAR = '" + paramap._year + "' ");
        stb.append("                          AND T2.CALCULATE_CREDIT_FLG = '2'");
        stb.append("                          AND ");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(                          "T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' || ");
        }
        stb.append(                              "T2.COMBINED_SUBCLASSCD = T1.SUBCLASSCD)");
        stb.append("    UNION SELECT ");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(             "T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' || ");
        }
        stb.append(                 "COMBINED_SUBCLASSCD AS SUBCLASSCD, SUM(T1.CREDITS) AS CREDITS");
        stb.append("    FROM    CREDITS_A T1, SUBCLASS_REPLACE_COMBINED_DAT T2 ");
        stb.append("    WHERE   T2.YEAR = '" + paramap._year + "' ");
        stb.append("        AND T2.CALCULATE_CREDIT_FLG = '2'");
        stb.append("        AND ");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(              "T2.ATTEND_CLASSCD || '-' || T2.ATTEND_SCHOOL_KIND || '-' || T2.ATTEND_CURRICULUM_CD || '-' || ");
        }
        stb.append(            "T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD");
        stb.append("    GROUP BY ");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(             "T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' || ");
        }
        stb.append(             "COMBINED_SUBCLASSCD");
        stb.append(") ");       
        
        // 欠課数の表
        stb.append(",T_ABSENCE_HIGH AS(");
        stb.append(    "SELECT  SCHREGNO, ");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(              "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append(             "SUBCLASSCD AS SUBCLASSCD, ");
        if (_knjSchoolMst.isJitu()) {
            stb.append(        " VALUE(T1.COMP_ABSENCE_HIGH, 99) ");
        } else {
            stb.append(        " VALUE(T1.ABSENCE_HIGH, 99) ");
        }
        stb.append(    "AS ABSENCE_HIGH ");
        stb.append(    "FROM ");
        if (_knjSchoolMst.isJitu()) {
            stb.append(    "SCHREG_ABSENCE_HIGH_DAT T1 ");
            stb.append(    "WHERE T1.YEAR = '" + paramap._year + "' ");
            stb.append(      "AND T1.DIV = '1' ");
        } else {
            stb.append(    "CREDIT_MST T1 ");
            stb.append(      "INNER JOIN SCHNO_A T2 ON ");
            stb.append(          "T1.GRADE = T2.GRADE ");
            stb.append(          "AND T1.COURSECD = T2.COURSECD ");
            stb.append(          "AND T1.MAJORCD = T2.MAJORCD ");
            stb.append(          "AND T1.COURSECODE = T2.COURSECODE ");
            stb.append(    "WHERE T1.YEAR = '" + paramap._year + "' ");
            stb.append(          "AND EXISTS(SELECT 'X' FROM CHAIR_A T3 WHERE T3.SUBCLASSCD = ");
            if ("1".equals(paramap._useCurriculumcd)) {
                stb.append(              "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                  "T1.SUBCLASSCD)");
        }
        stb.append(") ");

        //成績席次データの表（素点・評点・評定）
        stb.append(",RECORD_REC AS(");
        stb.append(    "SELECT  W3.SCHREGNO, ");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(              "W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
        }
        stb.append(            "W3.SUBCLASSCD AS SUBCLASSCD ");
        if (paramap._testKindCd.equals("0101") || paramap._testKindCd.equals("0201") || paramap._testKindCd.equals("0202")) {
            //中間・期末成績
            stb.append(       ",CASE WHEN SCORE IS NOT NULL THEN RTRIM(CHAR(SCORE)) ");
            stb.append(             "ELSE NULL END AS SCORE ");
            stb.append(       ",'' AS PATTERN_ASSESS ");
        } else {
            //学期・学年成績
            stb.append(       ",'' AS SCORE ");
            stb.append(       ",CASE WHEN SCORE IS NOT NULL THEN RTRIM(CHAR(SCORE)) ");
            stb.append(             "ELSE NULL END AS PATTERN_ASSESS ");
        }
        stb.append(    "FROM    RECORD_RANK_DAT W3 ");
        stb.append(    "WHERE   W3.YEAR = '" + paramap._year + "' AND ");
        stb.append("            W3.SEMESTER = '" + paramap._semester + "' AND ");
        stb.append("            W3.TESTKINDCD || W3.TESTITEMCD = '" + paramap._testKindCd + "' AND ");
        stb.append(            "EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
        stb.append(                   "WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
        stb.append(     ") ");
        
        //成績データの表（総合点）
        stb.append(",RECORD_SCORE AS(");
        stb.append(    "SELECT  W3.SCHREGNO, ");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(              "W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
        }
        stb.append(            "W3.SUBCLASSCD AS SUBCLASSCD ");
        stb.append(           ",W3.COMP_CREDIT ,W3.GET_CREDIT ");
        stb.append(           ",CASE WHEN SCORE IS NOT NULL THEN RTRIM(CHAR(SCORE)) ELSE NULL END AS SCORE_SOU ");
        stb.append(    "FROM    RECORD_SCORE_DAT W3 ");
        stb.append(    "WHERE   W3.YEAR = '" + paramap._year + "' AND ");
        stb.append("            W3.SEMESTER = '" + paramap._semester + "' AND ");
        stb.append("            W3.TESTKINDCD || W3.TESTITEMCD = '" + paramap._testKindCd + "' AND ");
        stb.append("            W3.SCORE_DIV = '00' AND ");
        stb.append(            "EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
        stb.append(                   "WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
        stb.append(     ") ");

        //追試試験データの表
        stb.append(",SUPP_EXA AS(");
        stb.append(    "SELECT  W3.SCHREGNO, ");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(              "W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
        }
        stb.append(            "W3.SUBCLASSCD AS SUBCLASSCD ,SCORE_FLG ");
        stb.append(           ",CASE WHEN SCORE_PASS IS NOT NULL THEN RTRIM(CHAR(SCORE_PASS)) ");
        stb.append(                 "ELSE NULL END AS SCORE_PASS ");
        stb.append(    "FROM    SUPP_EXA_DAT W3 ");
        stb.append(    "WHERE   W3.YEAR = '" + paramap._year + "' AND ");
        stb.append("            W3.SEMESTER = '" + paramap._semester + "' AND ");
        stb.append("            W3.TESTKINDCD || W3.TESTITEMCD = '" + paramap._testKindCd + "' AND ");
        stb.append(            "EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
        stb.append(                   "WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
        stb.append(     ") ");

        //成績データの表（平常点・会話点）
        stb.append(",RECORD_SCORE2 AS(");
                    //基本
        stb.append(    "SELECT  W3.SCHREGNO, ");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(              "W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
        }
        stb.append(            "W3.SUBCLASSCD AS SUBCLASSCD ");
        stb.append(           ",CASE WHEN SCORE IS NOT NULL THEN RTRIM(CHAR(SCORE)) ELSE NULL END AS SCORE_HEI ");
        stb.append(           ",'' AS SCORE_KAI ");
        stb.append(    "FROM    RECORD_SCORE_DAT W3 ");
        stb.append(    "WHERE   W3.YEAR = '" + paramap._year + "' AND ");
        stb.append("            W3.SEMESTER = '" + paramap._semester + "' AND ");
        stb.append("            W3.TESTKINDCD || W3.TESTITEMCD = '" + paramap._testKindCd + "' AND ");
        stb.append("            W3.SCORE_DIV = '02' AND ");
        stb.append(            "W3.SUBCLASSCD not in (SELECT NAME1 FROM V_NAME_MST ");
        stb.append(                                   "WHERE YEAR = '" + paramap._year + "' AND NAMECD1 in ('D002')) AND ");
        stb.append(            "W3.SUBCLASSCD not in ('090100') AND ");
        stb.append(            "EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
        stb.append(                    "WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
                    //後期課程・英語数学
        stb.append(    "UNION ALL ");
        stb.append(    "SELECT  W3.SCHREGNO, ");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(              "W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
        }
        stb.append(            "W3.SUBCLASSCD AS SUBCLASSCD ");
        stb.append(           ",CASE WHEN SCORE IS NOT NULL THEN RTRIM(CHAR(SCORE)) ELSE NULL END AS SCORE_HEI ");
        stb.append(           ",'' AS SCORE_KAI ");
        stb.append(    "FROM    RECORD_SCORE_DAT W3 ");
        stb.append(    "WHERE   W3.YEAR = '" + paramap._year + "' AND ");
        stb.append("            W3.SEMESTER = '" + paramap._semester + "' AND ");
        stb.append("            W3.TESTKINDCD || W3.TESTITEMCD = '" + paramap._testKindCd + "' AND ");
        stb.append("            W3.SCORE_DIV = '03' AND ");
        stb.append(            "W3.SUBCLASSCD in (SELECT NAME1 FROM V_NAME_MST ");
        stb.append(                               "WHERE YEAR = '" + paramap._year + "' AND NAMECD1 = 'D002') AND ");
        stb.append(            "EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
        stb.append(                    "WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
                    //前期課程・英語
        stb.append(    "UNION ALL ");
        stb.append(    "SELECT  W3.SCHREGNO, ");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(              "W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
        }
        stb.append(            "W3.SUBCLASSCD AS SUBCLASSCD ");
        stb.append(           ",MAX(CASE WHEN SCORE_DIV = '02' ");
        stb.append(                     "THEN CASE WHEN SCORE IS NOT NULL THEN RTRIM(CHAR(SCORE)) ELSE NULL END ");
        stb.append(                     "ELSE NULL END) AS SCORE_HEI ");
        stb.append(           ",MAX(CASE WHEN SCORE_DIV = '03' ");
        stb.append(                     "THEN CASE WHEN SCORE IS NOT NULL THEN RTRIM(CHAR(SCORE)) ELSE NULL END ");
        stb.append(                     "ELSE NULL END) AS SCORE_KAI ");
        stb.append(    "FROM    RECORD_SCORE_DAT W3 ");
        stb.append(    "WHERE   W3.YEAR = '" + paramap._year + "' AND ");
        stb.append("            W3.SEMESTER = '" + paramap._semester + "' AND ");
        stb.append("            W3.TESTKINDCD || W3.TESTITEMCD = '" + paramap._testKindCd + "' AND ");
        stb.append("            W3.SCORE_DIV in ('02','03') AND ");
        stb.append(            "W3.SUBCLASSCD in ('090100') AND ");
        stb.append(            "EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
        stb.append(                    "WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
        stb.append(    "GROUP BY  W3.SCHREGNO, ");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(              "W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
        }
        stb.append(            "W3.SUBCLASSCD ");
        stb.append(     ") ");

        //メイン表
        stb.append(" SELECT  T1.SUBCLASSCD,T1.SCHREGNO ");
        stb.append("        ,CASE WHEN T34.SCORE_PASS IS NOT NULL THEN T34.SCORE_FLG ELSE NULL END AS SCORE_FLG ");
        stb.append("        ,T34.SCORE_PASS ");
        if (paramap._testKindCd.equals("0101") || paramap._testKindCd.equals("0201") || paramap._testKindCd.equals("0202")) {
            //中間・期末成績
            stb.append("    ,T3.SCORE ");
            stb.append("    ,T35.SCORE_HEI AS PATTERN_ASSESS ");
        } else {
            //学期・学年成績
            stb.append("    ,T33.SCORE_SOU AS SCORE ");
            stb.append("    ,T3.PATTERN_ASSESS ");
        }
        stb.append("        ,T35.SCORE_KAI ");
        stb.append("        ,T33.COMP_CREDIT ");
        stb.append("        ,T33.GET_CREDIT ");
        stb.append("        ,CASE WHEN T1.SUBCLASSCD = '090100' THEN '1' ELSE NULL END AS KAIWADIV ");
        stb.append("        ,FLOAT(T5.ABSENT1) AS ABSENT1 ");
        stb.append("        ,T5.JISU ");
        stb.append("        ,T12.ABSENCE_HIGH ");
        stb.append("        ,T11.CREDITS ");
        stb.append("        ,T7.SUBCLASSABBV AS SUBCLASSNAME ");
        stb.append("        ,T8.CLASSABBV AS CLASSNAME ");
        stb.append("        ,T7.ELECTDIV ");
        stb.append("        ,CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN -1");
        stb.append("              WHEN T10.SUBCLASSCD IS NOT NULL THEN 1 ELSE 0 END AS REPLACEMOTO ");
        stb.append("        ,VALUE(T10.PRINT_FLG,'0') AS PRINT_FLG");
        //対象生徒・講座の表
        stb.append(" FROM(");
        stb.append("     SELECT  W2.SCHREGNO,");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(              "W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
        }
        stb.append(             "W2.SUBCLASSCD");
        stb.append("     FROM    CHAIR_A W2");
        if (!paramap._semester.equals("9")) {
            stb.append(" WHERE   W2.SEMESTER = '" + paramap._semester + "'");
        }
        stb.append("     GROUP BY W2.SCHREGNO,");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(              "W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
        }
        stb.append(             "W2.SUBCLASSCD");
        stb.append(" )T1 ");
        //成績の表
        stb.append(" LEFT JOIN(");
        stb.append("   SELECT W3.SCHREGNO,W3.SUBCLASSCD,W3.SCORE,W3.PATTERN_ASSESS");
        stb.append("   FROM   RECORD_REC W3");
        stb.append(" )T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD AND T3.SCHREGNO = T1.SCHREGNO");
        stb.append(" LEFT JOIN(");
        stb.append("   SELECT W3.SCHREGNO,W3.SUBCLASSCD,COMP_CREDIT,GET_CREDIT,SCORE_SOU");
        stb.append("   FROM   RECORD_SCORE W3");
        stb.append(" )T33 ON T33.SUBCLASSCD = T1.SUBCLASSCD AND T33.SCHREGNO = T1.SCHREGNO");
        stb.append(" LEFT JOIN(");
        stb.append("   SELECT W3.SCHREGNO,W3.SUBCLASSCD,SCORE_FLG,SCORE_PASS");
        stb.append("   FROM   SUPP_EXA W3");
        stb.append(" )T34 ON T34.SUBCLASSCD = T1.SUBCLASSCD AND T34.SCHREGNO = T1.SCHREGNO");
        stb.append(" LEFT JOIN(");
        stb.append("   SELECT W3.SCHREGNO,W3.SUBCLASSCD,SCORE_HEI,SCORE_KAI");
        stb.append("   FROM   RECORD_SCORE2 W3");
        stb.append(" )T35 ON T35.SUBCLASSCD = T1.SUBCLASSCD AND T35.SCHREGNO = T1.SCHREGNO");
        //合併先科目の表
        stb.append("  LEFT JOIN(");
        stb.append("    SELECT ");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(              "COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
        }
        stb.append(            "COMBINED_SUBCLASSCD AS SUBCLASSCD");
        stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
        stb.append("    WHERE  YEAR = '" + paramap._year + "'");
        stb.append("    GROUP BY ");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(              "COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
        }
        stb.append(            "COMBINED_SUBCLASSCD");
        stb.append(" )T9 ON T9.SUBCLASSCD = T1.SUBCLASSCD");
        //合併元科目の表
        stb.append("  LEFT JOIN(");
        stb.append("    SELECT ");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(              "ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
        }
        stb.append(             "ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(PRINT_FLG2) AS PRINT_FLG");
        stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
        stb.append("    WHERE  YEAR = '" + paramap._year + "'");
        stb.append("    GROUP BY ");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(              "ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
        }
        stb.append(             "ATTEND_SUBCLASSCD");
        stb.append(" )T10 ON T10.SUBCLASSCD = T1.SUBCLASSCD");

        //欠課数の表
        stb.append(" LEFT JOIN(");
        stb.append("   SELECT SCHREGNO, SUBCLASSCD,ABSENT1,JISU");
        stb.append("   FROM   ATTEND_B W1");
        stb.append("   WHERE  NOT EXISTS(SELECT 'X' FROM ATTEND_C W2 WHERE W2.SUBCLASSCD = W1.SUBCLASSCD)");
        stb.append("   UNION SELECT SCHREGNO, SUBCLASSCD,ABSENT1,JISU");
        stb.append("   FROM   ATTEND_C W1");
        stb.append(" )T5 ON T5.SCHREGNO = T1.SCHREGNO AND T5.SUBCLASSCD = T1.SUBCLASSCD");

        stb.append(" LEFT JOIN CREDITS_A T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.SUBCLASSCD = T1.SUBCLASSCD");
        stb.append(" LEFT JOIN CREDITS_B T11 ON T11.SUBCLASSCD = T1.SUBCLASSCD");
        stb.append(" LEFT JOIN T_ABSENCE_HIGH T12 ON T12.SCHREGNO = T1.SCHREGNO AND T12.SUBCLASSCD = T1.SUBCLASSCD");
        stb.append(" LEFT JOIN SUBCLASS_MST T7 ON ");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(     "T7.CLASSCD || '-' || T7.SCHOOL_KIND || '-' || T7.CURRICULUM_CD || '-' || ");
        }
        stb.append(         "T7.SUBCLASSCD = T1.SUBCLASSCD");
        stb.append(" LEFT JOIN CLASS_MST T8 ON ");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(     " T8.CLASSCD || '-' || T8.SCHOOL_KIND = T1.CLASSCD || '-' || T1.SCHOOL_KIND ");
        } else {
            stb.append(     " T8.CLASSCD = LEFT(T1.SUBCLASSCD,2)");
        }

        stb.append(" ORDER BY T1.SUBCLASSCD, T1.SCHREGNO");

        return stb.toString();
    }

    /**
     * 前年度までの修得単位数計
     * @param paramap
     * @return
     */
    private String sqlStdPreviousCredits(final Param paramap) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT SUM(CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END) AS CREDIT");
        stb.append(" FROM   SCHREG_STUDYREC_DAT T1");
        stb.append(" WHERE  T1.SCHREGNO = ?");
        stb.append("    AND T1.YEAR < '" + paramap._year + "'");
        stb.append("    AND ((T1.SCHOOLCD = '0' AND (CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR CLASSCD = '" + KNJDefineSchool.subject_T + "'))");
        stb.append("      OR T1.SCHOOLCD != '0')");

        return stb.toString();
    }

    /**
     * 前年度までの未履修（必須科目）数
     * @param paramap
     * @return
     */
    private String sqlStdPreviousMirisyu(final Param paramap) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT COUNT(*) AS COUNT");
        stb.append(" FROM   SCHREG_STUDYREC_DAT T1");
        stb.append(" INNER JOIN SUBCLASS_MST T2 ON T1.SUBCLASSCD = T2.SUBCLASSCD");
        if ("1".equals(paramap._useCurriculumcd)) {
            stb.append(     " AND T2.CLASSCD = T2.CLASSCD ");
            stb.append(     " AND T2.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append(     " AND T2.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        stb.append(" WHERE  T1.SCHREGNO = ?");
        stb.append("    AND T1.YEAR < '" + paramap._year + "'");
        stb.append("    AND ((T1.SCHOOLCD = '0' AND (CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR CLASSCD = '" + KNJDefineSchool.subject_T + "'))");
        stb.append("      OR T1.SCHOOLCD != '0')");
        stb.append("    AND VALUE(T2.ELECTDIV,'0') <> '1'");
        stb.append("    AND VALUE(T1.COMP_CREDIT,0) = 0");

        return stb.toString();
    }

    /**
     * 今年度の資格認定単位数
     * @param paramap
     * @return
     */
    private String sqlStdQualifiedCredits(final Param paramap) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT SUM(T1.CREDITS) AS CREDITS");
        stb.append(" FROM SCHREG_QUALIFIED_DAT T1");
        stb.append(" WHERE  T1.SCHREGNO = ?");
        stb.append("    AND T1.YEAR < '" + paramap._year + "'");

        return stb.toString();
    }

    /**
     * 卒業認定単位数
     * @param paramap
     * @return
     */
    private String sqlGradCredits(final Param paramap) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT GRAD_CREDITS FROM SCHOOL_MST WHERE YEAR = '" + paramap._year + "'");
        return stb.toString();
    }
    
    private static double round10(final int a, final int b) {
        return Math.round(a * 10.0 / b) / 10.0;
    }

    private HRInfo getHRInfo(final Param paramap) throws Exception {
        return new HRInfo(_manager, ((String) paramap._hrClass));
    }
    
    protected String getRecordTableName(final String prgid) {
        return "KNJD062B".equals(prgid) ? "V_RECORD_SCORE_DAT" : "RECORD_DAT";
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
    
    private void loadAttendSemesArgument(DB2UDB db2, Param paramap) {
        
        try {
            loadSemester(db2, (String) paramap._year);
            // 出欠の情報
            final KNJDefineCode definecode0 = setClasscode0(db2, paramap._year);
            final String z010Name1 = setZ010Name1(db2);
            _periodInState = AttendAccumulate.getPeiodValue(db2, definecode0, (String) paramap._year, SSEMESTER, (String) paramap._semester);
            _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, (String) paramap._year);
            _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap,_sDate, (String) paramap._date); // _sDate: 年度開始日, _date: LOGIN_DATE
            _semesFlg = ((Boolean) _hasuuMap.get("semesFlg")).booleanValue();
            log.debug(" hasuuMap = " + _hasuuMap);
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

    //--- 内部クラス -------------------------------------------------------
    /**
     * 基準となる処理クラス
     */
    private abstract class KNJD065_COMMON {
        String _fieldname;
        String _fieldname2;
        String _svfFieldNameKekka;  // 欠課時数フィールド名は、欠課時数算定コードにより異なる。
        String _svfFieldNameKekka2;  // 欠課時数フィールド名（19列目）
        DecimalFormat _absentFmt;
//        String _fieldChaircd;
        protected final boolean _creditDrop;
        protected final String _semesterName;
        protected final String _semester;
        protected final String _testKindCd;
        protected String _testName;
        protected int _gradCredits;  // 卒業認定単位数
        protected String _item0Name;  // 明細項目名（19列目）
        protected String _item1Name;  // 明細項目名
        protected String _item2Name;  // 明細項目名
        protected String _item3Name;  // 明細項目名
        protected String _item4Name;  // 総合点欄項目名
        protected String _item5Name;  // 平均点欄項目名
        protected String _item7Name;  // 順位欄項目名

        public KNJD065_COMMON(
                final DB2UDB db2,
                final Param paramap
        ) throws Exception {
            _creditDrop = paramap._isCreditDrop;
            _testKindCd = (String) paramap._testKindCd;
            _semester = (String) paramap._semester;
            _semesterName = (String) paramap.SEMESTERNAME;
            _gradCredits = getGradCredits(db2, paramap);
            setSvfFieldNameKekka();
            setAbsentFmt();
            _item7Name = paramap._rankName;
        }

        String getSvfFieldNameKekka(final int div) {
            return (1 == div) ? _svfFieldNameKekka : _svfFieldNameKekka2;
        }

        void setSvfFieldNameKekka() {
            int absent_cov = _definecode.absent_cov;
            if (absent_cov == 3 || absent_cov == 4) {
                _svfFieldNameKekka = "kekka1_2_";
                _svfFieldNameKekka2 = "kekka2_2_";
            } else {
                _svfFieldNameKekka = "kekka1_1_";
                _svfFieldNameKekka2 = "kekka2_1_";
            }
        }

        DecimalFormat getAbsentFmt() {
            return _absentFmt;
        }

        void setAbsentFmt() {
            switch (_definecode.absent_cov) {
            case 0:
            case 1:
            case 2:
                _absentFmt = new DecimalFormat("0");    // TODO: グローバルインスタンスを使えないか?
                break;
            default:
                _absentFmt = new DecimalFormat("0.0");
            }
        }

        boolean doPrintCreditDrop() {
            return _creditDrop;
        }

        ScoreValue getTargetValue(final ScoreDetail d) {
            return d.getPatternAsses();
        }

        /**
         * (帳票種別によって異なる)ページ見出し・項目・欄外記述を印刷します。
         * @param svf
         * @param paramap
         */
        abstract void printHeaderOther(Vrw32alpWrap svf,Param paramap);

        /**
         * ページ見出し・項目・欄外記述を印刷します。
         * @param svf
         * @param hrInfo
         * @param paramap
         */
        void printHeader(
                final Vrw32alpWrap svf,
                final HRInfo hrInfo,
                final Param paramap
        ) {
            svf.VrsOut("year2", (String) paramap.NENDO);
            svf.VrsOut("ymd1", (String) paramap.NOW); // 作成日
            svf.VrsOut("DATE", (String) paramap.TERM_KEKKA);  // 欠課の集計範囲
            svf.VrsOut("DATE2", (String) paramap.TERM_ATTEND);  // 「出欠の記録」の集計範囲

            svf.VrsOut("teacher", hrInfo.getStaffName());  //担任名
            svf.VrsOut("HR_NAME", hrInfo.getHrName());  //組名称
            if (hasCompCredit()) {
                svf.VrsOut("credit20", hrInfo.getHrCompCredits());  // 履修単位
            }
            svf.VrsOut("lesson20", hrInfo.getHrMLesson());  // 授業日数

            for (int i = 1; i <= MAX_COLUMN; i++) {
                svf.VrsOutn("ITEM1",i, _item1Name);
                svf.VrsOutn("ITEM2",i, _item2Name);
                svf.VrsOutn("ITEM3",i, _item3Name);
            }
            svf.VrsOut("ITEM4", _item4Name);
            svf.VrsOut("ITEM5", _item5Name);
            svf.VrsOut("ITEM6", "成　　績");
            svf.VrsOut("ITEM7", _item7Name);
            // 成績項目名（19列目）
            svf.VrsOut("ITEM2_1", _item1Name);
            svf.VrsOut("ITEM2_2", _item2Name);
            svf.VrsOut("ITEM2_3", _item3Name);
            svf.VrsOut("ITEM2_4", _item0Name);

            // 一覧表枠外の文言
            svf.VrAttribute("NOTEMARK1",  "Paint=(1,70,1),Bold=1");
            svf.VrsOut("NOTEMARK1",  " " );
            svf.VrsOut("NOTE1",  "：欠課時数超過者" );
            if (paramap._testKindCd.equals("0101") || paramap._testKindCd.equals("0201") || paramap._testKindCd.equals("0202")) {
                svf.VrAttribute("NOTEMARK2",  "Paint=(1,50,1),Bold=1");
                svf.VrsOut("NOTEMARK2",  " " );
                svf.VrsOut("NOTE2",  "：追試・見込点、再試験点（/：追試、+：見込点、*：再試験）" );
            } else if (doPrintCreditDrop()) {
                svf.VrsOut("NOTEMARK2",  "*" );
                svf.VrsOut("NOTE2",  "：単位保留者" );
            }
log.debug("CREDIT_DROP = "+doPrintCreditDrop());
        }

        boolean hasCompCredit() {
            // 履修単位数/修得単位数なし
            return false;
        }
        
        boolean hasJudgementItem() {
            // 判定会議資料用の項目なし
            return false;
        }

        /** {@inheritDoc} */
        public String toString() {
            return getClass().getName() + " : semesterName=" + _semesterName + ", testName=" + _testName;
        }

        // 卒業認定単位数の取得
        private int getGradCredits(
                final DB2UDB db2,
                final Param paramap
        ) {
            int gradcredits = 0;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sqlGradCredits(paramap);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    gradcredits = rs.getInt("GRAD_CREDITS");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return gradcredits;
        }
        
        /**
         * @return 合併元科目の「印刷しない」フラグを使用する場合はTrueを戻します。
         */
        boolean enablePringFlg() {
            return false;
        }
        
        /**
         * @return 明細の"成績"欄に単位を印刷する場合はTrueを戻します。
         */
        boolean isPrintDetailTani() {
            return false;
        }

        /**
         * @return 学年末はTrueを戻します。
         */
        boolean isGakunenMatu() {
            return false;
        }

        protected void vrsOutMarkPrgid(Vrw32alpWrap svf, Param paramap) {
            svf.VrsOut("MARK", "/");
            svf.VrsOut("PRGID", (String) paramap._prgId);
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 学年成績の処理クラス
     */
    private class KNJD065_GRADE extends KNJD065_COMMON {
        public KNJD065_GRADE(
                final DB2UDB db2,
                final Param paramap
        ) throws Exception {
            super(db2, paramap);
            _fieldname = "GRAD_VALUE";
            _fieldname2 = null;
            _testName = "(評定)";
            _item1Name = "単位";
            _item2Name = "評定";
            _item3Name = "欠課";
            _item4Name = "評定合計";
            _item5Name = "評定平均";
        }

        void printHeaderOther(
                final Vrw32alpWrap svf,
                final Param paramap
        ) {
            svf.VrsOut("TITLE", _semesterName + "成績一覧表"); //成績名称
            svf.VrsOut("MARK1_2",  ((Float)paramap._assess).toString());
        }

        boolean hasCompCredit() {
            // 履修単位数/修得単位数あり
            return true;
        }
        
        boolean hasJudgementItem() {
            // 判定会議資料用の項目あり
            return true;
        }
        
        /**
         * @return 合併元科目の「印刷しない」フラグを使用する場合はTrueを戻します。
         */
        boolean enablePringFlg() {
            return false;
        }

        /**
         * @return 明細の"成績"欄に単位を印刷する場合はTrueを戻します。
         */
        boolean isPrintDetailTani() {
            return true;
        }

        /**
         * @return 学年末はTrueを戻します。
         */
        boolean isGakunenMatu() {
            return true;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 学期成績の処理クラス
     */
    private class KNJD065_GAKKI extends KNJD065_GRADE {
        public KNJD065_GAKKI(
                final DB2UDB db2,
                final Param paramap
        ) throws Exception {
            super(db2, paramap);
            _fieldname = "SEM" + _semester + "_VALUE";
            _fieldname2 = null;
            _item2Name = "評価";
            _item4Name = "評価合計";
            _item5Name = "評価平均";
        }

        boolean hasCompCredit() {
            // 履修単位数/修得単位数なし
            return true;
        }
        
        boolean hasJudgementItem() {
            // 判定会議資料用の項目あり
            return true;
        }

        /**
         * @return 学年末はTrueを戻します。
         */
        boolean isGakunenMatu() {
            return false;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 学年成績の処理クラス
     */
    private class KNJD062A_GRADE extends KNJD065_COMMON {
        public KNJD062A_GRADE(
                final DB2UDB db2,
                final Param paramap
        ) throws Exception {
            super(db2, paramap);
            _fieldname = "GRAD_VALUE";
            _fieldname2 = null;
            _testName = (String) paramap.TESTITEMNAME;
            _item0Name = "";
            _item1Name = "総合点";
            _item2Name = "評定";
            _item3Name = "欠課";
            _item4Name = "評定総合点";
            _item5Name = "評定平均点";
        }

        boolean hasCompCredit() {
            // 履修単位数/修得単位数あり
            return true;
        }

        void printHeaderOther(
                final Vrw32alpWrap svf,
                final Param paramap
        ) {
            svf.VrsOut("TITLE" , _testName + "　成績一覧表（学年評定）");
            vrsOutMarkPrgid(svf, paramap);
        }

        ScoreValue getTargetValue(final ScoreDetail d) {
            return d.getPatternAsses();
        }

        boolean isPrintAverage() {
            return false;
        }

        void loadAverage(final DB2UDB db2, final Param paramap) {}
        
        /**
         * @return 合併元科目の「印刷しない」フラグを使用する場合はTrueを戻します。
         */
        boolean enablePringFlg() {
            return false;
        }

        /**
         * @return 明細の"成績"欄に単位を印刷する場合はTrueを戻します。
         */
        boolean isPrintDetailTani() {
            return true;
        }

        /**
         * @return 学年末はTrueを戻します。
         */
        boolean isGakunenMatu() {
            return true;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 学期成績の処理クラス
     */
    private class KNJD062A_GAKKI extends KNJD062A_GRADE {
        public KNJD062A_GAKKI(
                final DB2UDB db2,
                final Param paramap
        ) throws Exception {
            super(db2, paramap);
            _fieldname = "SEM" + _semester + "_VALUE";
            _fieldname2 = null;
            _item2Name = "評点";
            _item4Name = "評価総合点";
            _item5Name = "評価平均点";
        }

        void printHeaderOther(
                final Vrw32alpWrap svf,
                final Param paramap
        ) {
            svf.VrsOut("TITLE" , _testName + "　成績一覧表（評点）");
            vrsOutMarkPrgid(svf, paramap);
        }

        boolean hasCompCredit() {
            // 履修単位数/修得単位数なし
            return false;
        }

        /**
         * @return 学年末はTrueを戻します。
         */
        boolean isGakunenMatu() {
            return false;
        }

        boolean doPrintCreditDrop() {
            return false;
        }
        
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 中間試験成績の処理クラス
     */
    private class KNJD062A_INTER extends KNJD065_COMMON {

        KNJD062A_INTER(
                final DB2UDB db2,
                final Param paramap
        ) throws Exception {
            super(db2, paramap);
            _fieldname  = "SEM" + _semester + "_INTR_SCORE";
            _fieldname2 = "SEM" + _semester + "_INTR";
            _testName = (String) paramap.TESTITEMNAME;
            _item0Name = "会話点";
            _item1Name = "素点";
            _item2Name = "平常点";
            _item3Name = "欠課";
            _item4Name = "総合点";
            _item5Name = "平均点";
        }

        void printHeaderOther(
                final Vrw32alpWrap svf,
                final Param paramap
        ) {
            svf.VrsOut("TITLE" , _testName + "　成績一覧表（素点）");

            vrsOutMarkPrgid(svf, paramap);
        }

        private String getTitle(final Param paramap) {
            final String one234;
            final String testKindCd = (String) paramap._testKindCd;

            if ("1".equals(_semester)) {
                one234 = testKindCd.startsWith("01") ? "１" : "２";
            } else {
                one234 = testKindCd.startsWith("01") ? "３" : "４";
            }

            return one234 + "学期期末";
        }

        ScoreValue getTargetValue(final ScoreDetail d) {
            return d.getScore();
        }

        boolean isPrintAverage() {
            return true;
        }

//        void loadAverage(final DB2UDB db2, final Param paramap) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//
//            try {
//                final String sql = sqlSubclassAverage(paramap);
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    final String chaircd = rs.getString("CHAIRCD");
//                    final Double avgScore = (Double) rs.getObject("AVG_SCORE");
//
//                    if (null != chaircd && null != avgScore) {
//                        addAverage(chaircd, avgScore);
//                    }
//                }
//            } catch (final Exception ex) {
//                log.error("error! ", ex);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//            }
//
//        }

        boolean doPrintCreditDrop() {
            return false;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 期末試験成績の処理クラス
     */
    private class KNJD062A_TERM extends KNJD062A_INTER {
        KNJD062A_TERM(
                final DB2UDB db2,
                final Param paramap
        ) throws Exception {
            super(db2, paramap);
            if (_testKindCd.equals("0201")) {
                _fieldname  = "SEM" + _semester + "_TERM_SCORE";
                _fieldname2 = "SEM" + _semester + "_TERM";
//                _testName = "期末";
            } else {
                _fieldname  = "SEM" + _semester + "_TERM2_SCORE";
                _fieldname2 = "SEM" + _semester + "_TERM2";
//                _testName = "期末２";
            }
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private class Manager {
        private final KNJD065_COMMON _knjdObj;

        Manager(final KNJD065_COMMON knjdobj) {
            super();
            _knjdObj = knjdobj;
        }

        KNJD065_COMMON getKnjDobj() {
            return _knjdObj;
        }

        /**
         * 科目クラスの取得（教科名・科目名・単位・授業時数）
         * @param rs 生徒別科目別明細
         * @return 科目のクラス
         */
        SubClass getSubClass(
                final ResultSet rs,
                Map subclasses
        ) {
            String subclasscode = null;
            int credit = 0;
            int jisu = 0;
            try {
                subclasscode = rs.getString("SUBCLASSCD");
                if (rs.getString("CREDITS") != null) { credit = rs.getInt("CREDITS"); }
                if (rs.getString("JISU") != null) { jisu = rs.getInt("JISU"); }
            } catch (SQLException e) {
                 log.error("SQLException", e);
            }
            //科目クラスのインスタンスを更新して返す
            if (subclasses.containsKey(subclasscode)) {
                SubClass subclass = (SubClass) subclasses.get(subclasscode);
                ReturnVal ret = setMaxMin(subclass._maxcredit, subclass._mincredit, credit);
                subclass._maxcredit = ret.val1;
                subclass._mincredit = ret.val2;               
//                if (0 != credit) {
//                    if (subclass._maxcredit < credit) subclass._maxcredit = credit;
//                    if (credit < subclass._mincredit || 0 == subclass._mincredit) subclass._mincredit = credit;
//                }
                if (0 != jisu && subclass._jisu < jisu) subclass._jisu = jisu;
                return subclass;
            }
            //科目クラスのインスタンスを作成して返す
            String classabbv = null;
            String subclassabbv = null;
            boolean electdiv = false;
            boolean kaiwadiv = false;
            try {
                classabbv = rs.getString("CLASSNAME");
                subclassabbv = rs.getString("SUBCLASSNAME");
                if (null != rs.getString("ELECTDIV") && rs.getString("ELECTDIV").equals("1")) electdiv = true;
                if (null != rs.getString("KAIWADIV") && rs.getString("KAIWADIV").equals("1")) kaiwadiv = true;
            } catch (SQLException e) {
                 log.error("SQLException", e);
            }
            final SubClass subClass = new SubClass(subclasscode, classabbv, subclassabbv, electdiv, kaiwadiv, credit, jisu);
            subclasses.put(subclasscode, subClass);
            return subClass;
        }

        ScoreDetail createScoreDetail(
                final ResultSet rs,
                Map subclasses
        ) throws SQLException {
            final ScoreDetail detail = new ScoreDetail(
                    this,
                    getSubClass(rs, subclasses),
                    (Double) rs.getObject("ABSENT1"),
                    (Integer) rs.getObject("JISU"),
                    ScoreValue.create(rs.getString("SCORE"),rs.getString("SUBCLASSCD")),
                    ScoreValue.create(rs.getString("PATTERN_ASSESS"),rs.getString("SUBCLASSCD")),
                    ScoreValue.create(rs.getString("SCORE_KAI"),rs.getString("SUBCLASSCD")),
                    (Integer) rs.getObject("REPLACEMOTO"),
                    (String) rs.getObject("PRINT_FLG"),
                    (String) rs.getObject("SCORE_FLG"),
                    (String) rs.getObject("SCORE_PASS"),
                    (Integer) rs.getObject("COMP_CREDIT"),
                    (Integer) rs.getObject("GET_CREDIT"),
                    (BigDecimal) rs.getObject("ABSENCE_HIGH"),
                    (Integer) rs.getObject("CREDITS")
//                    rs.getString("CHAIRCD")
            );
            return detail;
        }

        TransInfo createTransInfo(final ResultSet rs) {
            try {
                final String d1 = rs.getString("KBN_DATE1");
                if (null != d1) {
                    final String n1 = rs.getString("KBN_NAME1");
                    return new TransInfo(d1, n1);
                }

                final String d2 = rs.getString("KBN_DATE2");
                if (null != d2) {
                    final String n2 = rs.getString("KBN_NAME2");
                    return new TransInfo(d2, n2);
                }
            } catch (final SQLException e) {
                 log.error("SQLException", e);
            }
            return new TransInfo();
        }

        AttendInfo createAttendInfo(final ResultSet rs) throws SQLException {
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
            return attendInfo;
        }

        private ReturnVal setMaxMin(
                int maxInt,
                int minInt,
                int tergetInt
        ) {
            if (0 < tergetInt) {
                if (maxInt < tergetInt){ maxInt = tergetInt; }
                if (0 == minInt) {
                    minInt = tergetInt;
                } else {
                    if (minInt > tergetInt){ minInt = tergetInt; }
                }
            }
            return new ReturnVal(maxInt,minInt);
        }

    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<学級のクラス>>。
     */
    private class HRInfo implements Comparable {
        private final Manager _manager;
        private final String _code;
        private String _staffName;
        private String _hrName;
        private final List _students = new LinkedList();
        private final Map _subclasses = new TreeMap();
        private List _ranking;
        private BigDecimal _avgHrTotalScore;  // 総合点の学級平均
        private BigDecimal _avgHrAverageScore;  // 平均点の学級平均
        private BigDecimal _perHrPresent;  // 学級の出席率
        private BigDecimal _perHrAbsent;  // 学級の欠席率
        private String _HrCompCredits;  // 学級の履修単位数
        private String _HrMLesson;  // 学級の授業日数

        HRInfo(
                final Manager manager,
                final String code
        ) {
            _manager = manager;
            _code = code;
        }

        String getCode() { return _code; }

        void load(
                final DB2UDB db2,
                final Param paramap
        ) throws Exception {
            loadHRClassStaff(db2, paramap);
            loadStudents(db2, paramap);
            loadStudentsInfo(db2, paramap);
            loadAttend(db2, paramap);
            loadRank(db2, paramap);
            loadScoreDetail(db2, paramap);
            _ranking = createRanking();
            log.debug("RANK:" + _ranking);
            setSubclassAverage(this);        
            setHrTotal(this);  // 学級平均等の算出
            if (_manager._knjdObj.hasJudgementItem()) {
                loadPreviousCredits(db2, paramap);  // 前年度までの修得単位数取得
                loadPreviousMirisyu(db2, paramap);  // 前年度までの未履修（必須科目）数
                loadQualifiedCredits(db2, paramap);  // 今年度の資格認定単位数
                setGradeAttendCheckMark(paramap);  // 成績不振者/成績優良者/皆勤者/欠課時数超過 設定
            }
        }

        private void loadHRClassStaff(
                final DB2UDB db2,
                final Param paramap
        ) {
            final KNJ_Get_Info.ReturnVal returnval = _getinfo.Hrclass_Staff(
                    db2,
                    (String) paramap._year,
                    (String) paramap._semester,
                    (String) paramap._hrClass,
                    ""
            );
            _staffName = returnval.val3;
            _hrName = returnval.val1;
        }

        private void loadStudents(
                final DB2UDB db2,
                final Param paramap
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlHrclassStdList(paramap);
                ps = db2.prepareStatement(sql);

                rs = ps.executeQuery();
                int gnum = 0;
                while (rs.next()) {
                    if (paramap._isNoKetuban){
                        gnum++;
                    } else {
                        gnum = rs.getInt("ATTENDNO");
                    }
                    final Student student = new Student(rs.getString("SCHREGNO"), this, gnum);
                    _students.add(student);
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

        private void loadStudentsInfo(
                final DB2UDB db2,
                final Param paramap
        ) {
            PreparedStatement ps = null;

            try {
                final String sql = sqlStdNameInfo(paramap);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student.getCode());

                    ResultSet rs = null;
                    try {
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            final TransInfo transInfo = _manager.createTransInfo(rs);

                            student.setInfo(
                                    rs.getString("ATTENDNO"),
                                    rs.getString("NAME"),
                                    transInfo
                            );
                        }
                    } catch (SQLException e) {
                        log.error("SQLException", e);
                    } finally {
                        DbUtils.closeQuietly(rs);
                    }
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }
        
        private Student getStudent(final List students, final String schregno) {
            for (Iterator it = students.iterator(); it.hasNext();) {
                Student student = (Student) it.next();
                if (null != schregno && schregno.equals(student._code)) {
                    return student;
                }
            }
            return null;
        }

        private void loadAttend(
                final DB2UDB db2,
                final Param paramap
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final Map paramMap = new HashMap();
                paramMap.put("useCurriculumcd", paramap._useCurriculumcd);
                paramMap.put("useVirus", paramap._useVirus);
                paramMap.put("useKoudome", paramap._useKoudome);
                paramMap.put("DB2UDB", db2);

                String targetSemes = (String) paramap._semester;
                final String sql = AttendAccumulate.getAttendSemesSql(
                        _semesFlg,
                        _definecode,
                        _knjSchoolMst,
                        (String) paramap._year,
                        SSEMESTER,
                        targetSemes,
                        (String) _hasuuMap.get("attendSemesInState"),
                        _periodInState,
                        (String) _hasuuMap.get("befDayFrom"),
                        (String) _hasuuMap.get("befDayTo"),
                        (String) _hasuuMap.get("aftDayFrom"),
                        (String) _hasuuMap.get("aftDayTo"),
                        (String) paramap._grade,
                        ((String) paramap._hrClass.substring(2, 5)),
                        null,
                        "SEMESTER",
                        paramMap
                );
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final Student student = getStudent(_students, rs.getString("SCHREGNO"));
                    if (null == student) {
                        continue;
                    }
                    if (!targetSemes.equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    final AttendInfo attendInfo = _manager.createAttendInfo(rs);
                    student.setAttendInfo(attendInfo);
                }
                
//                for (final Iterator it = _students.iterator(); it.hasNext();) {
//                    final Student student = (Student) it.next();
//
//                    int i = 1;
//                    ps.setString(i++, student.getCode());
//                    log.debug(" student = " + student._attendNo + " : " + student._code + " : " + student._name);
//                    ResultSet rs = null;
//                    try {
//                        rs = ps.executeQuery();
//                        while (rs.next()) {
//                            if (!targetSemes.equals(rs.getString("SEMESTER"))) {
//                                continue;
//                            }
//                            final AttendInfo attendInfo = _manager.createAttendInfo(rs);
//                            student.setAttendInfo(attendInfo);
//                        }
//                    } catch (Exception e) {
//                        log.error("Exception", e);
//                    } finally {
//                        DbUtils.closeQuietly(rs);
//                    }
//                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void loadRank(
                final DB2UDB db2,
                final Param paramap
        ) {
            PreparedStatement ps = null;

            try {
                final String sql = sqlStdTotalRank(paramap);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student.getCode());

                    ResultSet rs = null;
                    try {
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            student.setRank(rs.getInt("TOTAL_RANK"));
                        }
                    } catch (SQLException e) {
                        log.error("SQLException", e);
                    } finally {
                        DbUtils.closeQuietly(rs);
                    }
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private void loadScoreDetail(
                final DB2UDB db2,
                final Param paramap
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            
            try {
                ps = db2.prepareStatement(sqlStdSubclassDetail(paramap, _code));
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (_manager._knjdObj.enablePringFlg() && "1".equals(rs.getString("PRINT_FLG"))) {
                        continue;
                    }
                    
                    final Student student = getStudent(_students, rs.getString("SCHREGNO"));
                    
                    if (null == student) {
                      continue;
                    }
                    final ScoreDetail scoreDetail = _manager.createScoreDetail(rs,_subclasses);
                    student.add(scoreDetail);
                }

//                ps = db2.prepareStatement(sqlStdSubclassDetail(paramap, _code));
//                for (final Iterator it = _students.iterator(); it.hasNext();) {
//                    final Student student = (Student) it.next();
//                    log.debug(" student = " + student._attendNo + " : " + student._code);
//                    
//                    int i = 1;
//                    ps.setString(i++, student.getCode());
//                    
//                    rs = ps.executeQuery();
//                    while (rs.next()) {
//                        if (_manager._knjdObj.enablePringFlg() && "1".equals(rs.getString("PRINT_FLG"))) {
//                            continue;
//                        }
//
//                        final ScoreDetail scoreDetail = _manager.createScoreDetail(rs,_subclasses);
//                        student.add(scoreDetail);
//                    }
//                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        // 前年度までの修得単位数計
        private void loadPreviousCredits(
                final DB2UDB db2,
                final Param paramap
        ) {
            PreparedStatement ps = null;

            try {
                final String sql = sqlStdPreviousCredits(paramap);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    int i = 1;
                    ps.setString(i++, student.getCode());

                    ResultSet rs = null;
                    try {
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            student._previousCredits = rs.getInt("CREDIT");
                        }
                    } catch (SQLException e) {
                        log.error("SQLException", e);
                    } finally {
                        DbUtils.closeQuietly(rs);
                    }
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        // 前年度までの未履修（必須科目）数
        private void loadPreviousMirisyu(
                final DB2UDB db2,
                final Param paramap
        ) {
            PreparedStatement ps = null;

            try {
                final String sql = sqlStdPreviousMirisyu(paramap);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    int i = 1;
                    ps.setString(i++, student.getCode());

                    ResultSet rs = null;
                    try {
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            student._previousMirisyu = rs.getInt("COUNT");
                        }
                    } catch (SQLException e) {
                        log.error("SQLException", e);
                    } finally {
                        DbUtils.closeQuietly(rs);
                    }
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        // 今年度の資格認定単位数
        private void loadQualifiedCredits(
                final DB2UDB db2,
                final Param paramap
        ) {
            PreparedStatement ps = null;

            try {
                final String sql = sqlStdQualifiedCredits(paramap);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    int i = 1;
                    ps.setString(i++, student.getCode());

                    ResultSet rs = null;
                    try {
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            student._qualifiedCredits = rs.getInt("CREDITS");
                        }
                    } catch (SQLException e) {
                        log.error("SQLException", e);
                    } finally {
                        DbUtils.closeQuietly(rs);
                    }
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }
        
        // 成績不振者/成績優良者/皆勤者/欠課時数超過 設定
        private void setGradeAttendCheckMark(Param paramap) {
            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                student._isGradeGood = student.isGradeGood(paramap);
                student._isGradePoor = student.isGradePoor();
                student._isAttendPerfect = student.isAttendPerfect();
                student._isKekkaOver = student.isKekkaOver();
            }
        }

        /**
         * Studentクラスの成績から科目別学級平均および合計を算出し、SubClassクラスのフィールドにセットする。
         * @param hrInfo
         */
        private void setSubclassAverage(
                final HRInfo hrInfo
        ) {
//            final KNJD065_COMMON knjdobj = _manager.getKnjDobj();
            final Map map = new HashMap();
            final Map map2 = new HashMap();
            final Map map3 = new HashMap();
            final Map map4 = new HashMap();

            for (final Iterator itS = hrInfo._students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                for (final Iterator itD = student.getScoreDetails().iterator(); itD.hasNext();) {
                    final ScoreDetail detail = (ScoreDetail) itD.next();
                    final ScoreValue val  = detail.getScore();
                    final ScoreValue val2 = detail.getPatternAsses();
                    final ScoreValue val4 = detail.getScoreKai();
//                    final ScoreValue val = knjdobj.getTargetValue(detail);
                    final SubClass subClass = detail.getSubClass();
                    if (null != val && val.hasIntValue()) {
                        int[] arr = (int[]) map.get(subClass);
                        if (null == arr) {
                            arr = new int[2];
                            map.put(subClass, arr);
                        }
                        arr[0] += val.getScoreAsInt();
                        arr[1]++;
                    }
                    if (null != val2 && val2.hasIntValue()) {
                        int[] arr2 = (int[]) map2.get(subClass);
                        if (null == arr2) {
                            arr2 = new int[2];
                            map2.put(subClass, arr2);
                        }
                        arr2[0] += val2.getScoreAsInt();
                        arr2[1]++;
                    }
                    if (null != val4 && val4.hasIntValue()) {
                        int[] arr4 = (int[]) map4.get(subClass);
                        if (null == arr4) {
                            arr4 = new int[2];
                            map4.put(subClass, arr4);
                        }
                        arr4[0] += val4.getScoreAsInt();
                        arr4[1]++;
                    }
                    final Double kekka = detail.getAbsent();
                    if (null == kekka) continue;
                    final int val3 = (int) kekka.doubleValue();
                    if (0 == val3) continue;
                    int[] arr3 = (int[]) map3.get(subClass);
                    if (null == arr3) {
                        arr3 = new int[2];
                        map3.put(subClass, arr3);
                    }
                    arr3[0] += val3;
                    arr3[1]++;
                }
            }

            for (final Iterator it = hrInfo.getSubclasses().values().iterator(); it.hasNext();) {
                final SubClass subclass = (SubClass) it.next();
                if (map.containsKey(subclass)) {
                    final int[] val = (int[]) map.get(subclass);
                    double d = 0;
                    if (0 != val[1]) {
                        d = round10(val[0], val[1]);
                        subclass._scoreaverage = DEC_FMT1.format(d);
                        subclass._scoretotal = String.valueOf(val[0]);
                        subclass._scoreCount = String.valueOf(val[1]);
                    }
                }
                if (map2.containsKey(subclass)) {
                    final int[] val = (int[]) map2.get(subclass);
                    double d = 0;
                    if (0 != val[1]) {
                        d = round10(val[0], val[1]);
                        subclass._scoreaverage2 = DEC_FMT1.format(d);
                        subclass._scoretotal2 = String.valueOf(val[0]);
                        subclass._scoreCount2 = String.valueOf(val[1]);
                    }
                }
                if (map3.containsKey(subclass)) {
                    final int[] val = (int[]) map3.get(subclass);
                    if (0 != val[1]) {
                        subclass._scoretotal3 = String.valueOf(val[0]);
                        subclass._scoreCount3 = String.valueOf(val[1]);
                    }
                }
                if (map4.containsKey(subclass)) {
                    final int[] val = (int[]) map4.get(subclass);
                    double d = 0;
                    if (0 != val[1]) {
                        d = round10(val[0], val[1]);
                        subclass._scoreaverage4 = DEC_FMT1.format(d);
                        subclass._scoretotal4 = String.valueOf(val[0]);
                        subclass._scoreCount4 = String.valueOf(val[1]);
                    }
                }
            }
        }
        
        /**
         * 学級平均の算出
         */
        private void setHrTotal(
                final HRInfo hrInfo
        ) {
            int totalT = 0;
            int countT = 0;
            double totalA = 0;
            int countA = 0;
            int mlesson = 0;
            int present = 0;
            int absent = 0;
            int[] arrc = {0,0};  // 履修単位
            int[] arrj = {0,0};  // 授業日数
            for (final Iterator itS = hrInfo._students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                final Total totalObj = student._total;
                if (null != totalObj) {
                    if (0 < totalObj._count) {
                        totalT += totalObj._total;
                        countT++;
                    }
//                    if (null != totalObj._avgBigDecimal) {
//                        totalA += totalObj._avgBigDecimal.doubleValue();
//                        countA++;
//                    }
//                    if (0< totalObj._avgcount) {
//                        totalA += totalObj._avgtotal;
//                        countA += totalObj._avgcount;
                        if (0< totalObj._count) {
                        totalA += totalObj._total;
                        countA += totalObj._count;
                    }
                }
                final AttendInfo attend = student._attendInfo;
                if (null != attend) {
                    mlesson += attend._mLesson;
                    present += attend._present;
                    absent += attend._absent;
                    ReturnVal ret = _manager.setMaxMin(arrj[0], arrj[1], attend._mLesson);
                    arrj[0] = ret.val1;
                    arrj[1] = ret.val2;
                }
                ReturnVal ret = _manager.setMaxMin(arrc[0], arrc[1], student._compCredit);
                arrc[0] = ret.val1;
                arrc[1] = ret.val2;
            }
            if (0 < countT) {
                final double avg = (float) totalT / (float) countT;
                _avgHrTotalScore = new BigDecimal(avg);
            }                
            if (0 < countA) {
                final double avg = (float) totalA / (float) countA;
                _avgHrAverageScore = new BigDecimal(avg);
            }
            if (0 < mlesson) {
                _perHrPresent = new BigDecimal((float) present / (float) mlesson * 100);
                _perHrAbsent = new BigDecimal((float) absent / (float) mlesson * 100);
            }
            if (0 < arrc[0]) {
                _HrCompCredits = arrc[0] + "単位";
            }
            if (0 < arrj[0]) {
                _HrMLesson = arrj[0] + "日";
            }
        }

        /**
         * 順位の算出
         */
        private List createRanking() {
            final List list = new LinkedList();
            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                student.compute();
                final Total total = student.getTotal();
                if (0 < total.getCount()) {
                    list.add(total);
                }
            }

            Collections.sort(list);
            return list;
        }

        List getStudents() { return _students; }
        Map getSubclasses() { return _subclasses; }
        String getHrName() { return _hrName; }
        String getStaffName() { return _staffName; }
        String getHrCompCredits() { return _HrCompCredits; }
        String getHrMLesson() { return _HrMLesson; }

        int rank(final Student student) {
            final Total total = student.getTotal();
            if (0 >= total.getCount()) {
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
            return getHrName() + "[" + getStaffName() + "]";
        }

        /**
         * 学級データの印字処理(学級平均)
         * @param svf
         */
        void print(
                final Vrw32alpWrap svf,
                HRInfo hrinfo
        ) {
            if (null != hrinfo._avgHrTotalScore) {
                svf.VrsOut("TOTAL53", DEC_FMT1.format(hrinfo._avgHrTotalScore.setScale(1,BigDecimal.ROUND_HALF_UP)));
            }
            if (null != hrinfo._avgHrAverageScore) {
                svf.VrsOut("AVERAGE53", DEC_FMT1.format(hrinfo._avgHrAverageScore.setScale(1,BigDecimal.ROUND_HALF_UP)));
            }
            if (null != hrinfo._perHrPresent) {
                svf.VrsOut("PER_ATTEND", DEC_FMT1.format(hrinfo._perHrPresent.setScale(1,BigDecimal.ROUND_HALF_UP)));
            }
            if (null != hrinfo._perHrAbsent) {
                svf.VrsOut("PER_ABSENCE", DEC_FMT1.format(hrinfo._perHrAbsent.setScale(1,BigDecimal.ROUND_HALF_UP)));
            }
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private class TransInfo {
        private final String _date;
        private final String _name;
        private final String _str;

        TransInfo() {
            this(null, null);
        }

        TransInfo(
                final String date,
                final String name
        ) {
            _date = date;
            _name = name;
            _str = toString(this);
        }

        public String toString() {
            return _str;
        }

        private String toString(final TransInfo info) {
            if (null == info._date && null == info._name) {
                return "";
            }

            final StringBuffer sb = new StringBuffer();
            if (null != info._date) {
                sb.append(KNJ_EditDate.h_format_JP(info._date));
            }
            if (null != info._name) {
                sb.append(info._name);
            }
            return sb.toString();
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒のクラス>>。
     */
    private class Student implements Comparable {
        private final int _gnum;  // 行番号
        private final String _code;  // 学籍番号
        private final HRInfo _hrInfo;
        private String _attendNo;
        private String _name;
        private TransInfo _transInfo;
        private AttendInfo _attendInfo;
        private int _rank;
        private final Map _scoreDetails = new HashMap();
        private Total _total;
        private int _compCredit;  // 今年度の履修単位数
        private int _getCredit;  // 今年度の修得単位数
        private int _qualifiedCredits;  // 今年度の認定単位数
        private int _previousCredits;  // 前年度までの修得単位数
        private int _previousMirisyu;  // 前年度までの未履修（必須科目）数
        private boolean _isGradeGood;  // 成績優良者
        private boolean _isGradePoor;  // 成績不振者
        private boolean _isAttendPerfect;  // 皆勤者
        private boolean _isKekkaOver;  // 欠課時数超過が1科目でもある者

        Student(final String code, final HRInfo hrInfo, final int gnum) {
            _gnum = gnum;
            _code = code;
            _hrInfo = hrInfo;
        }

        void setInfo(
                final String attendNo,
                final String name,
                final TransInfo tansInfo
        ) {
            _attendNo = attendNo;
            _name = name;
            _transInfo = tansInfo;
        }

        void setAttendInfo(final AttendInfo attendInfo) { _attendInfo = attendInfo; }

        AttendInfo getAttendInfo() { return _attendInfo; }

        void setRank(final int rank) { _rank = rank; }

        int getRank() { return _rank; }

        void add(final ScoreDetail scoreDetail) { _scoreDetails.put(scoreDetail._subClass._subclasscode, scoreDetail); }

        void compute() { _total = new Total(this); }

        Collection getScoreDetails() { return _scoreDetails.values(); }

        Total getTotal() { return _total; }

        String getCode() { return _code; }

        /**
         * 出席番号順にソートします。
         * {@inheritDoc}
         */
        public int compareTo(final Object o) {
            if (!(o instanceof Student)) return -1;
            final Student that = (Student) o;
            int rtn;
            rtn = _hrInfo.compareTo(that._hrInfo);
            if (0 != rtn) return rtn;
            rtn = _attendNo.compareTo(that._attendNo);
            return rtn;
        }

        int rank() {
            return _hrInfo.rank(this);
        }

        public String toString() {
            return _attendNo + ":" + _name;
        }

        /**
         * @return 成績優良者（評定平均が4.3以上）は true を戻します。
         */
        boolean isGradeGood(Param paramap) {
            final BigDecimal avg = getTotal().getAvgBigDecimal();
            if (null == avg) { return false; }
            Float ac = (Float) paramap._assess;
            if (ac.floatValue() <= avg.doubleValue()) { return true; }
            return false;
        }

        /**
         * @return 成績不振者（評定１が1つでもある）は true を戻します。
         */
        boolean isGradePoor() {
            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) itD.next();
                final ScoreValue val = detail._patternAssess;
                if (null == val) continue;
                if (!val.hasIntValue()) continue;
                if (val._val == 1){ return true; }
            }
            return false;
        }

        /**
         * @return 皆勤（欠席、遅刻、早退、欠課が０）なら true を戻します。
         */
        boolean isAttendPerfect() {
            if (null != _attendInfo && ! _attendInfo.isAttendPerfect()) { return false; }
            
            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) itD.next();
                final Double kekka = detail.getAbsent();
                if (null == kekka) continue;
                if (0 < kekka.doubleValue()){ return false; }
            }            
            return true;
        }

        /**
         * @return 欠課超過が1科目でもあるなら true を戻します。
         */
        boolean isKekkaOver() {
            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) itD.next();
                if (true == detail._isOver){ return true; }
            }            
            return false;
        }

        /**
         * ページ毎の印字処理(生徒名/マーク)
         * @param svf
         */
        void printOnPage(
                final Vrw32alpWrap svf
        ) {
            svf.doNumberingSvfOut((10 < _name.length()) ? "name2_" : "name1_", _gnum, _name);    // 氏名
            svf.doNumberingSvfOutn("NUMBER", _gnum, 0, DEC_FMT2.format(Integer.parseInt(_attendNo)));  // 出席番号
            svf.doNumberingSvfOut("REMARK", _gnum, _transInfo.toString());  // 備考
            if (_hrInfo._manager._knjdObj.hasJudgementItem()) { printMark(svf); }
        }

        /**
         * 科目別明細の印字処理
         * @param svf
         * @param subclass
         * @param line
         */
        void printScoreDetail(
                final Vrw32alpWrap svf,
                final SubClass subclass,
                final int line
        ) {
            if (_scoreDetails.containsKey(subclass._subclasscode)) {
              final ScoreDetail detail = (ScoreDetail) _scoreDetails.get(subclass._subclasscode);
              if (subclass._kaiwadiv) {
                  detail.printKaiwa(svf, _gnum);
              } else {
                  detail.print(svf, line, _gnum);
              }
            }
        }

        /**
         * 最後のページの印字処理（成績総合/出欠の記録）
         * @param svf
         */
        void printOnLastpage(
                final Vrw32alpWrap svf
        ) {
            if (null != _total) {
                _total.print(svf, _gnum);
            }
            if (null != _attendInfo) {
                _attendInfo.print(svf, _gnum);
            }
            if (_hrInfo._manager._knjdObj.hasCompCredit()) {
                //今年度履修単位数
                svf.doNumberingSvfOutNonZero("R_CREDIT", _gnum, String.valueOf(_compCredit));
                //今年度修得単位数
                svf.doNumberingSvfOutNonZero("C_CREDIT", _gnum, String.valueOf(_getCredit));
            }
            if (_hrInfo._manager._knjdObj.hasJudgementItem()) {
                printOtherCredits(svf);  // 前年度までの単位数を印字
            }
        }

        /**
         * 各単位数、未履修科目数を印字
         * @param svf
         */
        void printOtherCredits(
                final Vrw32alpWrap svf
        ) {
            // 今年度認定単位数
            svf.doNumberingSvfOutNonZero("A_CREDIT", _gnum, String.valueOf(_qualifiedCredits));
            // 前年度までの修得単位数
            svf.doNumberingSvfOutNonZero("PRE_C_CREDIT", _gnum, String.valueOf(_previousCredits));
            // 修得単位数計
            int t = _getCredit + _qualifiedCredits + _previousCredits;
            if (t != 0) {
                int g = _hrInfo._manager._knjdObj._gradCredits;  // 卒業認定単位数
                if (g != 0 && g <= t) {
                    svf.doNumberingSvfOut("TOTAL_C_CREDIT", _gnum, "@" + String.valueOf(t));
                } else {
                    svf.doNumberingSvfOut("TOTAL_C_CREDIT", _gnum, String.valueOf(t));
                }
            }
            // 前年度までの未履修科目数
            svf.doNumberingSvfOutNonZero("PRE_N_CREDIT", _gnum, String.valueOf(_previousMirisyu));
        }

        /**
         * 成績優良者、成績不振者、皆勤者、欠課時数超過有者のマークを印字
         * @param svf
         */
        void printMark(
                final Vrw32alpWrap svf
        ) {
            if (_isGradePoor) { svf.doNumberingSvfOutn("CHECK1", _gnum, 0, "★"); }
            else if (_isGradeGood) { svf.doNumberingSvfOutn("CHECK1", _gnum, 0, "☆"); } 
            if (_isAttendPerfect) { svf.doNumberingSvfOutn("CHECK2", _gnum, 0, "○"); }
            else if (_isKekkaOver) { svf.doNumberingSvfOutn("CHECK2", _gnum, 0, "●"); } 
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

        void print(final Vrw32alpWrap svf, int gnum) {
            svf.doNumberingSvfOutNonZero("PRESENT", gnum, _mLesson);      // 出席すべき日数
            svf.doNumberingSvfOutNonZero("SUSPEND", gnum, _suspend);      // 出席停止
            svf.doNumberingSvfOutNonZero("KIBIKI", gnum,  _mourning);     // 忌引
            svf.doNumberingSvfOutNonZero("ABSENCE", gnum, _absent);       // 欠席日数
            svf.doNumberingSvfOutNonZero("ATTEND", gnum,  _present);      // 出席日数
            svf.doNumberingSvfOutNonZero("TOTAL_LATE", gnum, _late);      // 遅刻回数
            svf.doNumberingSvfOutNonZero("LEAVE", gnum,   _early);        // 早退回数
            svf.doNumberingSvfOutNonZero("ABROAD", gnum,  _transDays);    // 留学等実績
        }

        /**
         * @return 皆勤（欠席、遅刻、早退が０）なら true を戻す。
         */
        boolean isAttendPerfect() {
            if (_absent == 0 && _late == 0 && _early == 0) { return true; }
            return false;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<科目のクラスです>>。
     */
    private class SubClass {
        private final String _classcode;
        private final String _classabbv;
        private final String _subclasscode;
        private final String _subclassabbv;
        private final boolean _electdiv; // 選択科目
        private final boolean _kaiwadiv; // 科目（前期課程・英語）
        private int _gnum;  // 行番号
        private int _maxcredit;  // 単位
        private int _mincredit;  // 単位
        private int _jisu;  // 授業時数
        private String _scoreaverage;  // 学級平均
        private String _scoretotal;  // 学級合計
        private String _scoreaverage2;  // 学級平均（２列目）
        private String _scoretotal2;  // 学級合計（２列目）
        private String _scoretotal3;  // 学級合計（３列目）
        private String _scoreaverage4;  // 学級平均（会話点）
        private String _scoretotal4;  // 学級合計（会話点）
        private String _scoreCount;  // 学級人数
        private String _scoreCount2;  // 学級人数
        private String _scoreCount3;  // 学級人数
        private String _scoreCount4;  // 学級人数

        SubClass(
                final String subclasscode, 
                final String classabbv, 
                final String subclassabbv,
                final boolean electdiv,
                final boolean kaiwadiv,
                final int credit,
                final int jisu
        ) {
            _classcode = subclasscode.substring(0, 2);
            _classabbv = classabbv;
            _subclasscode = subclasscode;
            _subclassabbv = subclassabbv;
            _electdiv = electdiv;
            _kaiwadiv = kaiwadiv;
            _maxcredit = credit;  // 単位
            _mincredit = credit;  // 単位
            _jisu = jisu;  // 授業時数
        }

//        public String toString() {
//            return _subclasscode + ":" + _abbv;
//        }

        public boolean equals(final Object obj) {
            if (!(obj instanceof SubClass)) return false;
            final SubClass that = (SubClass) obj;
            return _subclasscode.equals(that._subclasscode);
        }

        public int hashCode() {
            return _subclasscode.hashCode();
        }

        String getSubclasscode() { return _subclasscode; }
        String getAbbv() { return _subclassabbv; }
        
        /**
         * 科目項目(教科名・科目名・)を印刷します。
         * @param svf
         * @param line 科目の列番
         */
        void print(
                final Vrw32alpWrap svf, 
                int line
        ) {
            int i = ((line + 1) % MAX_COLUMN == 0)? MAX_COLUMN: (line + 1) % MAX_COLUMN;
if (log.isDebugEnabled()) { log.debug("subclassname="+_subclasscode + " " + _subclassabbv+"   line="+line + "  i="+i + "  kaiwadiv="+_kaiwadiv); }

            //前期課程・英語
            if (_kaiwadiv) {
                //教科名
                svf.VrsOut("course2", _classabbv);
                //科目名
                if (_electdiv) svf.VrAttribute("subject2", "Paint=(2,70,2),Bold=1");
                svf.VrsOut("subject2", _subclassabbv);
                if (_electdiv) svf.VrAttribute("subject2", "Paint=(0,0,0),Bold=0");
                //単位数
                if (0 != _maxcredit) {
                    if (_maxcredit == _mincredit) { svf.VrsOut("credit2", String.valueOf(_maxcredit)); }
                    else  { svf.VrsOut("credit2", String.valueOf(_mincredit) + " \uFF5E " + String.valueOf(_maxcredit)); }
                }
                //授業時数
                if (0 != _jisu) { svf.VrsOut("lesson2", String.valueOf(_jisu)); }
                //学級平均・合計
                if (null != _scoretotal) { svf.VrsOut("RATE_TOTAL2", _scoretotal); }
                if (null != _scoretotal2) { svf.VrsOut("LATE_TOTAL2", _scoretotal2); }
                if (null != _scoretotal3) { svf.VrsOut("KEKKA_TOTAL2", _scoretotal3); }
                if (null != _scoretotal4) { svf.VrsOut("SCORE_TOTAL2", _scoretotal4); }
                if (null != _scoreCount)  { svf.VrsOut("RATE_NUMBER2",  _scoreCount); }
                if (null != _scoreCount2) { svf.VrsOut("LATE_NUMBER2",  _scoreCount2); }
                if (null != _scoreCount3) { svf.VrsOut("KEKKA_NUMBER2", _scoreCount3); }
                if (null != _scoreCount4) { svf.VrsOut("SCORE_NUMBER2", _scoreCount4); }
                if (null != _scoreaverage) { svf.VrsOut("RATE_AVE2", _scoreaverage); }
                if (null != _scoreaverage2) { svf.VrsOut("LATE_AVE2", _scoreaverage2); }
                if (null != _scoreaverage4) { svf.VrsOut("SCORE_AVE2", _scoreaverage4); }
            } else {
                //教科名
                svf.VrsOut("course1", _classabbv);
                //科目名
                if (_electdiv) svf.VrAttributen("subject1", i, "Paint=(2,70,2),Bold=1");
                svf.VrsOutn("subject1", i, _subclassabbv);
                if (_electdiv) svf.VrAttributen("subject1", i, "Paint=(0,0,0),Bold=0");
                //単位数
                if (0 != _maxcredit) {
                    if (_maxcredit == _mincredit) { svf.VrsOutn("credit1", i, String.valueOf(_maxcredit)); }
                    else  { svf.VrsOutn("credit1", i, String.valueOf(_mincredit) + " \uFF5E " + String.valueOf(_maxcredit)); }
                }
                //授業時数
                if (0 != _jisu) { svf.VrsOutn("lesson1", i, String.valueOf(_jisu)); }
                //学級平均・合計
                if (null != _scoretotal) { svf.VrsOutn("RATE_TOTAL", i, _scoretotal); }
                if (null != _scoretotal2) { svf.VrsOutn("LATE_TOTAL", i, _scoretotal2); }
                if (null != _scoretotal3) { svf.VrsOutn("KEKKA_TOTAL", i, _scoretotal3); }
                if (null != _scoreCount)  { svf.VrsOutn("RATE_NUMBER",  i, _scoreCount); }
                if (null != _scoreCount2) { svf.VrsOutn("LATE_NUMBER",  i, _scoreCount2); }
                if (null != _scoreCount3) { svf.VrsOutn("KEKKA_NUMBER", i, _scoreCount3); }
                if (null != _scoreaverage) { svf.VrsOutn("RATE_AVE", i, _scoreaverage); }
                if (null != _scoreaverage2) { svf.VrsOutn("LATE_AVE", i, _scoreaverage2); }
            }
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<素点・評定データのクラスです>>。
     */
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
            if (null == strScore) return null;
            return new ScoreValue(strScore);
        }

        /**
         * 生徒別科目別の素点または評定のインスタンスを作成します。
         * @param strScore 素点または評定
         * @param classcd 教科コード
         * @return ScoreValue。'総合的な学習の時間'は'null'を戻します。
         */
        static ScoreValue create(
                final String strScore,
                final String classcd
        ) {
            if (null == strScore) return null;
            if (KNJDefineSchool.subject_T.equals(classcd.substring(0, 2))) return null;
            return new ScoreValue(strScore);
        }

        String getScore() { return _strScore; }
        boolean hasIntValue() { return _isInt; }
        int getScoreAsInt() { return _val; }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒別科目別データのクラスです>>。
     */
    private class ScoreDetail {
        private final Manager _manager;
        private final SubClass _subClass;
        private final Double _absent;
        private final Integer _jisu;
        private final ScoreValue _score;
        private final ScoreValue _patternAssess;
        private final ScoreValue _scoreKai;
        private final Integer _replacemoto;
        private final String _print_flg;
        private final String _score_flg;
        private final String _scorePass;
        private final Integer _compCredit;
        private final Integer _getCredit;
        private final BigDecimal _absenceHigh;
        private final Integer _credits;
        private final boolean _isOver;
//        private final String _chaircd;

        ScoreDetail(
                final Manager manager,
                final SubClass subClass,
                final Double absent,
                final Integer jisu,
                final ScoreValue score,
                final ScoreValue patternAssess,
                final ScoreValue scoreKai,
                final Integer replacemoto,
                final String print_flg,
                final String score_flg,
                final String scorePass,
                final Integer compCredit,
                final Integer getCredit,
                final BigDecimal absenceHigh,
                final Integer credits
//                final String chaircd
        ) {
            _manager = manager;
            _subClass = subClass;
            _absent = absent;
            _jisu = jisu;
            _score = score;
            _patternAssess = patternAssess;
            _scoreKai = scoreKai;
            _replacemoto = replacemoto;
            _print_flg = print_flg;
            _score_flg = score_flg;
            _scorePass = scorePass;
            _compCredit = compCredit;
            _getCredit = getCredit;
            _absenceHigh = absenceHigh;
            _credits = credits;
            _isOver = judgeOver(absent, absenceHigh);
//            _chaircd = chaircd;
        }

        /**
         * 欠課時数超過ならTrueを戻します。
         * @param absent 欠課時数
         * @param absenceHigh 超過対象欠課時数（CREDIT_MST）
         * @return
         */
        private boolean judgeOver(final Double absent, final BigDecimal absenceHigh) {
            if (null == absent || null == absenceHigh) {
                return false;
            }
            if (0.1 > absent.floatValue() || 0 == absenceHigh.intValue()) {
                return false;
            }
            if (absenceHigh.intValue() < absent.floatValue()) {
                return true;
            }
            return false;
        }

        ScoreValue getScore() { return _score; }
        ScoreValue getPatternAsses() { return _patternAssess; }
        ScoreValue getScoreKai() { return _scoreKai; }
        SubClass getSubClass() { return _subClass; }

        /**
         * @return 合併元科目はnullを、以外はcompCreditを戻します。
         */
        Integer getCompCredit() {
            return enableCredit() ? _compCredit : null;
        }

        /**
         * @return 合併元科目はnullを、以外はgetCreditを戻します。
         */
        Integer getGetCredit() {
            return enableCredit() ? _getCredit : null;
        }

        /**
         * @return 合併元科目はFalseを、以外はTrueを戻します。
         */
        boolean enableCredit() {
            if (null != _replacemoto && _replacemoto.intValue() >= 1) {
                return false;
            }
            return true;
        }

        /**
         * 生徒別科目別素点・評定・欠課時数等を印刷します。
         * @param svf
         * @param line 科目の列番
         * @param gnum 生徒の行番
         */
        void print(
                final Vrw32alpWrap svf,
                final int line,
                final int gnum
        ) {
            int column = line % MAX_COLUMN + 1;
            final KNJD065_COMMON knjdobj = _manager.getKnjDobj();

            // 素点・単位
//            if (_manager._knjdObj.isPrintDetailTani()) {
//                printTani(svf, gnum, column);
//            } else {
                printSoten(svf, gnum, column);
//            }
            
            // 成績
            if (null != _patternAssess) {
                if (knjdobj.doPrintCreditDrop() && 1 == _patternAssess.getScoreAsInt()) {
                    svf.doNumberingSvfOutn("late", gnum, column,  "*" + _patternAssess.getScore());
                } else {
                    svf.doNumberingSvfOutn("late", gnum, column, _patternAssess.getScore());
                }
            }

            // 欠課
            if (null != _absent) {
                final int value = (int) Math.round(_absent.doubleValue() * 10.0);
                final String field = _manager._knjdObj.getSvfFieldNameKekka(1);
                if (0 != value) {
                    if (_isOver) {
                        svf.doNumberingVrAttributen(field, gnum, column, "Paint=(2,70,1),Bold=1");
                    }
                    svf.doNumberingSvfOutn(field, gnum, column, _manager._knjdObj.getAbsentFmt().format(_absent.floatValue()));
                    if (_isOver) {
                        svf.doNumberingVrAttributen(field, gnum, column, "Paint=(0,0,0),Bold=0");   //網掛けクリア
                    }
                }
            }
        }

        /**
         * 生徒別科目別素点・評定・欠課時数等を印刷します。
         * 前期課程・英語の場合のみ印刷します。
         * @param svf
         * @param gnum 生徒の行番
         */
        void printKaiwa(
                final Vrw32alpWrap svf,
                final int gnum
        ) {
            final KNJD065_COMMON knjdobj = _manager.getKnjDobj();
            String gnum_s = String.valueOf(gnum);

            // 素点
            if (null != _score && null == _scorePass) {
                if (null != _score_flg) {
                    svf.VrAttribute("rate2_" + gnum_s, "Paint=(1,50,1),Bold=1");
                }
                final String mark = ("1".equals(_score_flg)) ? "/" : 
                                    ("2".equals(_score_flg)) ? "+" : 
                                    ("3".equals(_score_flg)) ? "*" : "";
                svf.VrsOut("rate2_" + gnum_s, (null != _score_flg) ? mark + _scorePass : mark + _score.getScore() );
                if (null != _score_flg) {
                    svf.VrAttribute("rate2_" + gnum_s, "Paint=(0,0,0),Bold=0");
                }
            }

            // 成績
            if (null != _patternAssess) {
                if (knjdobj.doPrintCreditDrop() && 1 == _patternAssess.getScoreAsInt()) {
                    svf.VrsOut("late2_" + gnum_s,  "*" + _patternAssess.getScore());
                } else {
                    svf.VrsOut("late2_" + gnum_s, _patternAssess.getScore());
                }
            }

            // 会話点
            if (null != _scoreKai) {
                svf.VrsOut("SCORE2_" + gnum_s, _scoreKai.getScore() );
            }

            // 欠課
            if (null != _absent) {
                final int value = (int) Math.round(_absent.doubleValue() * 10.0);
                final String field = _manager._knjdObj.getSvfFieldNameKekka(2);
                if (0 != value) {
                    if (_isOver) {
                        svf.VrAttribute(field + gnum_s, "Paint=(2,70,1),Bold=1");
                    }
                    svf.VrsOut(field + gnum_s, _manager._knjdObj.getAbsentFmt().format(_absent.floatValue()));
                    if (_isOver) {
                        svf.VrAttribute(field + gnum_s, "Paint=(0,0,0),Bold=0");   //網掛けクリア
                    }
                }
            }
        }

        /**
         * 素点を印刷します。
         * @param svf
         * @param gnum 印刷する行番号
         * @param column 印刷する列番号
         */
        private void printSoten(
                final Vrw32alpWrap svf, 
                final int gnum, 
                final int column
        ) {
            if (null == _score && null == _scorePass) { return; }
            if (null != _score_flg) {
                svf.doNumberingVrAttributen("rate", gnum, column, "Paint=(1,50,1),Bold=1");
            }
            final String mark = ("1".equals(_score_flg)) ? "/" : 
                                ("2".equals(_score_flg)) ? "+" : 
                                ("3".equals(_score_flg)) ? "*" : "";
            svf.doNumberingSvfOutn("rate", gnum, column, (null != _score_flg) ? mark + _scorePass : mark + _score.getScore() );
            if (null != _score_flg) {
                svf.doNumberingVrAttributen("rate", gnum, column, "Paint=(0,0,0),Bold=0");
            }
        }

        /**
         * 単位を印刷します。
         * @param svf
         * @param gnum 印刷する行番号
         * @param column 印刷する列番号
         */
        private void printTani(
            final Vrw32alpWrap svf, 
            final int gnum, 
            final int column
        ) {
            Integer credit;
            if (_manager._knjdObj.isGakunenMatu()) {
                credit = _getCredit;
            } else {
                credit = _credits;
            }
            if (null == credit) { return; }
            if (null != _replacemoto && 1 <= _replacemoto.intValue()) {
                svf.doNumberingSvfOutn("rate", gnum, column, "(" + credit.toString() + ")");
            } else {
                svf.doNumberingVrAttributen("rate", gnum, column, "Hensyu=3");
                svf.doNumberingSvfOutn("rate", gnum, column, credit.toString());
            }
        }

        
        /**
         * @return absent を戻します。
         */
        Double getAbsent() {
            return _absent;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒別総合成績データのクラスです>>。
     */
    private class Total implements Comparable {
        private Student _student;
        private int _total;  // 総合点
        private int _count;  // 件数（成績）
        private BigDecimal _avgBigDecimal;  // 平均点

        /**
         * コンストラクタ。
         * @param student
         */
        Total(final Student student) {
            _student = student;
            compute();
        }

        /**
         * @return
         */
        int getCount() { return _count; }

        /**
         * 生徒別総合点・件数・履修単位数・修得単位数を算出します。
         */
        private void compute() {
            final KNJD065_COMMON knjdObj = _student._hrInfo._manager._knjdObj;

            int total = 0;
            int count = 0;

            int compCredit = 0;
            int getCredit = 0;

            for (final Iterator it = _student.getScoreDetails().iterator(); it.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) it.next();

                final ScoreValue scoreValue = knjdObj.getTargetValue(detail);
                if (isAddTotal(scoreValue, detail._replacemoto, knjdObj)) {
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
                final double avg = (float) total / (float) count;
                _avgBigDecimal = new BigDecimal(avg);
            }
            if (0 < compCredit) { _student._compCredit = compCredit; }
            if (0 < getCredit) { _student._getCredit = getCredit; }
        }

        
        /**
         * @param scoreValue
         * @param replacemoto
         * @param knjdObj
         * @return 成績総合計に組み入れる場合Trueを戻します。
         */
        private boolean isAddTotal(
                final ScoreValue scoreValue,
                final Integer replacemoto,
                final KNJD065_COMMON knjdObj
        ) {
            if (null == scoreValue) { return false; }
            if (!scoreValue.hasIntValue()) { return false; }
            if (knjdObj.isGakunenMatu() && null != replacemoto && 0 < replacemoto.intValue()) { return false; }
            return true;
        }
        
        /**
         * 生徒別総合点・平均点・順位を印刷します。
         * @param svf
         * @param gnum 行番号(印字位置)
         */
        void print(
                final Vrw32alpWrap svf, 
                final int gnum
        ) {
            if (0 < _count) {
                svf.doNumberingSvfOut("TOTAL", gnum, String.valueOf(_total));  //総合点
                svf.doNumberingSvfOut("AVERAGE", gnum, DEC_FMT1.format(_avgBigDecimal.setScale(1,BigDecimal.ROUND_HALF_UP)));  //平均点
            }

            final int rank = _student.getRank();
            if (1 <= rank) {
                svf.doNumberingSvfOut("RANK", gnum, String.valueOf(rank));  //順位
            }
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(final Object o) {
            if (!(o instanceof Total)) return -1;
            final Total that = (Total) o;

            return that._avgBigDecimal.compareTo(this._avgBigDecimal);
        }

        /**
         * {@inheritDoc}
         */
        public boolean equals(final Object o) {
            if (!(o instanceof Total)) return false;
            final Total that = (Total) o;
            return that._avgBigDecimal.equals(this._avgBigDecimal);
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return _avgBigDecimal.toString();
        }

        /**
         * @return avgBigDecimal を戻します。
         */
        BigDecimal getAvgBigDecimal() {
            return _avgBigDecimal;
        }       
    }

    //--- 内部クラス -------------------------------------------------------
    static class ReturnVal {
        final int val1,val2;
        ReturnVal(int int1,int int2){
            this.val1 = int1;
            this.val2 = int2;
        }
    }
    
    private static class Param {
        final String _year;  //年度
        final String _semester;  //学期
        final String _semeFlg;  //LOG-IN時の学期（現在学期）
        final String _grade;  //学年
        final String _testKindCd;   //テスト・成績種別
        final String _date;  //出欠集計日付
        final String _totalRank;  //総合順位出力 1:学級 2:学年 3:コース
        final String _rankName; //総合順位出力の順位欄項目名
        final boolean _isCreditDrop;  //単位保留
        final boolean _isNoKetuban;  //欠番を詰める
        final boolean _isOutputCourse;  //同一クラスでのコース毎に改頁あり
        final Float _assess;
        final String _prgId;
        
        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;

        String DIVIDEATTENDDATE;
        String DIVIDEATTENDMONTH;
        String SEMES_MONTH;
        String SEMESTERNAME;  //学期名称
        String SEMESTERDATE_S; // 学期期間FROM
        String YEARDATE_S; // 年度の開始日
        String TESTITEMNAME;// テスト名称 
        String NENDO;
        String NOW;
        String TERM_KEKKA;
        String TERM_ATTEND;
        
        String _hrClass;
        String _courseCd;
        public Param(final HttpServletRequest request) {
            final String testKindCd = request.getParameter("TESTKINDCD");
            _totalRank = request.getParameter("OUTPUT_RANK");

            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _semeFlg = request.getParameter("SEME_FLG");
            _grade = request.getParameter("GRADE");
            _testKindCd = "0".equals(testKindCd) ? "0000" : testKindCd;
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _isCreditDrop = (request.getParameter("OUTPUT4") != null);
            _isNoKetuban = ( request.getParameter("OUTPUT5") != null );
            _isOutputCourse = (request.getParameter("OUTPUT_COURSE") != null);
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            
            if (_totalRank.equals("1")) {
                _rankName = "学級順位";
            } else if (_totalRank.equals("2")) {
                _rankName = "学年順位";
            } else {
                _rankName = "コース順位";
            }
            //成績優良者評定平均の基準値===>KNJD610：未使用
            if( request.getParameter("ASSESS") != null ) {
                _assess = new Float(request.getParameter("ASSESS1"));
            } else {
                _assess = new Float(4.3);
            }
            // 起動元のプログラムＩＤ
            _prgId = ( request.getParameter("PRGID") != null ) ? request.getParameter("PRGID") : null;
        }
    }
}
