package servletpack.KNJA;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Grade_Hrclass;
import servletpack.KNJZ.detail.KNJ_Semester;

/**
 * 学校教育システム 賢者 [学籍管理] ＜ＫＮＪＡ１７１＞ 生徒基本データ(生徒名簿)
 * 2005/01/28 nakamoto 作成（東京都）
 * 2005/12/18 m-yama NO001 SCHREG_BASE_DAT、SCHREG_ADDRESS_DAT修正に伴う修正
 * 2008/03/19 nakasone リファクタリング。
 *                     性別欄・備考欄の追加。フォーム(性別無し・性別有り)の選択機能追加。
 *                     電話番号・急用電話番号・性別の表示・非表示機能の追加。
 *                     タイトル年度・作成日・生年月日の西暦・和暦表示の選択機能追加。
 */

public class KNJA171M extends HttpServlet {

    KNJ_EditDate editdate = new KNJ_EditDate(); // 和暦変換取得クラスのインスタンス作成

    private static final String FORM_FILE1 = "KNJA171M.frm";
    private static final int PAGE_MAX_LINE = 25;
    private boolean _hasData;
    private Form _form;
    private Vrw32alp _svf;
    private DB2UDB db2;

    private DecimalFormat _df = new DecimalFormat("##");

    Param _param;

    private static final Log log = LogFactory.getLog(KNJA171M.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws Exception {

        dumpParam(request);

        _param = new Param(db2, request);
        _form = new Form(FORM_FILE1, response);

        db2 = null;

        // パラメータの取得
        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            if (openDb(db2)) {
                return;
            }

            _param.load(db2);
            _hasData = printMain(db2);

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            _form.closeSvf();
            closeDb(db2);
        }
    }

    /** 印刷処理メイン
     * @param db2   ＤＢ接続オブジェクト
     * @return
     * @throws Exception
     */
    private boolean printMain(final DB2UDB db2) throws Exception {

        boolean retflg = false;

        KNJ_Semester semester = new KNJ_Semester(); // クラスのインスタンス作成
        KNJ_Semester.ReturnVal returnval = semester.Semester(db2, _param._year, _param._semester);
        String serchdate1 = returnval.val2; // 学期開始日
        String serchdate2 = returnval.val3; // 学期終了日

        if (_param._kaiPageOff) {
            // 生徒データ取得
            final List student = createGradeClass(db2, "", "", _param.schregNo, serchdate1, serchdate2);
            // 帳票出力のメソッド
            if (outPutPrint(student, "", "")) {
                retflg = true;
            }
        } else {
            for (int i = 0; i < _param._gradeHrclass.length; i++) {
                String serchGrade = _param._gradeHrclass[i].substring(0, 2); //学年
                String serchHrClass = _param._gradeHrclass[i].substring(2); //組

                // 生徒データ取得
                final List student = createGradeClass(db2, serchGrade, serchHrClass, _param.schregNo, serchdate1, serchdate2);
                // 帳票出力のメソッド
                if (outPutPrint(student, serchGrade, serchHrClass)) {
                    retflg = true;
                }
            }
        }

        return retflg;
    }

    /**
     * 帳票出力処理
     * @param student           帳票出力生徒データ
     * @return
     * @throws Exception
     */
    private boolean outPutPrint(final List studentList, String serchGrade, String serchHrClass)   throws Exception {

        boolean dataflg = false;       // 対象データ存在フラグ
        int gyo = 1;                    // 現在ページ数の判断用（行）
        int reccnt = 0;                 // 合計レコード数
        String keyGrade = "";
        String keyHrClass = "";
        String keySchregFront4 = "";

        _form._svf.VrAttribute( "HR_NAME", "FF=1");  // 自動改ページ

        for (Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            //ページMAX行(50行) 又は 組コードブレイクの場合
            final String changeYear = student._schregno.substring(0, 4);
            if (_param._kaiPageOff) {
                if (gyo > PAGE_MAX_LINE || (!"".equals(keySchregFront4) && !keySchregFront4.equals(changeYear))) {
                    _form._svf.VrEndPage();
                    gyo = 1;
                    _param.pagecnt++;
                }
            } else {
                if ((gyo > PAGE_MAX_LINE) || !"".equals(keyGrade) && !"".equals(keyHrClass) && !keyGrade.equals(student._grade) && !keyHrClass.equals(student._hrClass)) {
                    _form._svf.VrEndPage();
                    gyo = 1;
                    _param.pagecnt++;
                }
            }

            // ヘッダ設定
            pringHead(serchGrade, serchHrClass);
            // 明細設定
            printMeisai(student, gyo);

            keyGrade = student._grade;
            keyHrClass = student._hrClass;
            keySchregFront4 = changeYear;
            ++reccnt;
            ++gyo;
            dataflg = true;
        }

        // 最終レコードを出力
        if (dataflg) {
            _form._svf.VrEndPage();
        }

        return dataflg;
    }

    /** 帳票ヘッダ設定処理
     * @param serchGrade    学年
     * @param serchHrClass  組
     * @throws Exception
     */
    private void pringHead(String serchGrade, String serchHrClass)   throws Exception {

        // 作成日(現在処理日)の取得
        KNJ_Control control = new KNJ_Control(); // クラスのインスタンス作成
        KNJ_Control.ReturnVal returnval = control.Control(db2);
        if(_param.seirekiOutHantei){
            // 西暦出力
            _form._svf.VrsOut("YMD", fomatSakuseiDate(returnval.val3, "yyyy-MM-dd", "yyyy'年'M'月'd'日'"));
        } else {
            // 和暦出力
            _form._svf.VrsOut("YMD", KNJ_EditDate.h_format_JP(returnval.val3));
        }

        if (!_param._kaiPageOff) {
            // 組名称及び担任名の取得
            KNJ_Grade_Hrclass hrclass_staff = new KNJ_Grade_Hrclass(); // クラスのインスタンス作成
            KNJ_Grade_Hrclass.ReturnVal returnvalStaff = hrclass_staff.Hrclass_Staff(db2, _param._year, _param._semester, serchGrade, serchHrClass);
            _form._svf.VrsOut("HR_NAME", returnvalStaff.val1); // 組名称
            _form._svf.VrsOut("tannin_mei", returnvalStaff.val3); // 担任名
        }
        // タイトル年度
        if(_param.seirekiOutHantei){
            // 西暦出力
            _form._svf.VrsOut("nendo", _param._year + "年度");
        } else {
            // 和暦出力
            _form._svf.VrsOut("nendo", nao_package.KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度");
        }
        if ("1".equals(_param._rishuuCheck)) {
            _form._svf.VrsOut("SUBTITLE", "(履修登録者のみ)");
        }
//        _form._svf.VrsOut("ITEM", _param.getHogosyaTitle());
    }

    /** 帳票明細設定処理
     * @throws Exception
     */
    private void printMeisai(final Student student, final int gyo)   throws Exception {

        _form._svf.VrlOutn("gakkyuubango", gyo, student._attendno);
        _form._svf.VrsOutn("gakusekibango", gyo, student._schregno);
        _form._svf.VrsOutn("seito_kana", gyo, student._seitoKana);
        _form._svf.VrsOutn("NAME", gyo, student._seitoKanji);
        // 誕生日出力
        if(null != student._age && !student._age.equals("")){
            _form._svf.VrsOutn("AGE", gyo, student._age);
        }
        if(null != student._birthday){
            _form._svf.VrsOutn("BIRTHDAY", gyo, student.getFormatBirthDay());
        }
        // 性別出力
        _form._svf.VrsOutn("sex", gyo, student._sex);

        _form._svf.VrsOutn("ZIPCD", gyo, student._zipcd1);
        _form._svf.VrsOutn("seito_jyusho", gyo, student._address1);
        _form._svf.VrsOutn("seito_jyusho2", gyo, student._address2);
        // 電話番号出力
        _form._svf.VrsOutn("PHONE1", gyo, student._telno1);
        // 急用電話番号出力
        _form._svf.VrsOutn("PHONE2", gyo, student._telno2);
        _form._svf.VrsOutn("hogosha_kanji", gyo, student._totalCredit);
//        _form._svf.VrsOutn("syussinko", gyo, student._jName);

    }

    // ======================================================================
    /** 生徒データ取得処理
     * @param db2           ＤＢ接続オブジェクト
     * @param serchGrade    学年
     * @param serchHrClass  組
     * @param serchdate1    学期開始日
     * @param serchdate2    学期終了日
     * @throws SQLException
     */
    private List createGradeClass(final DB2UDB db2,
                                 String serchGrade,
                                 String serchHrClass,
                                 String schregNo,
                                 String serchdate1,
                                 String serchdate2) throws SQLException {

        final List rtnList = new ArrayList();
        final String sql = getGradeClassSql(serchGrade, serchHrClass, schregNo, serchdate1, serchdate2);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final Student student = new Student(
                        rs.getString("YEAR"),
                        rs.getString("GRADE"),
                        rs.getString("HR_CLASS"),
                        rs.getString("SEMESTER"),
                        rs.getString("SCHREGNO"),
                        rs.getInt("ATTENDNO"),
                        rs.getString("SEITO_KANJI"),
                        rs.getString("SEITO_KANA"),
                        rs.getString("AGE"),
                        rs.getString("ZIPCD1"),
                        rs.getString("ADDRESS1"),
                        rs.getString("ADDRESS2"),
                        rs.getString("TELNO1"),
                        rs.getString("GUARD_NAME"),
                        rs.getString("TELNO2"),
                        rs.getString("J_NAME"),
                        rs.getString("SEX"),
                        rs.getString("BIRTHDAY")
                );
                rtnList.add(student);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }

        PreparedStatement ps1 = null;
        ResultSet rs1 = null;
        // 累計単位をセット
        try {
            final String sqlTotalCredit = getTotalCreditSql(serchGrade, serchHrClass);
            //log.debug(" totalCreditSql =" + sqlTotalCredit);
            ps1 = db2.prepareStatement(sqlTotalCredit);
            rs1 = ps1.executeQuery();

            while (rs1.next()) {
                final Student student = getStudent(rtnList, rs1.getString("SCHREGNO"));
                if (null == student) {
                    continue;
                }
                final int sateiTanni = NumberUtils.isNumber(rs1.getString("SATEI_TANNI")) ? Integer.parseInt(rs1.getString("SATEI_TANNI")) : 0;
                final int totalCredit = NumberUtils.isNumber(rs1.getString("TOTAL_CREDIT")) ? Integer.parseInt(rs1.getString("TOTAL_CREDIT")) : 0;
                if (null != rs1.getString("SATEI_TANNI") || null != rs1.getString("TOTAL_CREDIT")) {
                    student._totalCredit = String.valueOf(sateiTanni + totalCredit);
                }
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps1, rs1);
            db2.commit();
        }
        return rtnList;
    }

    /** 学年・組単位の生徒データ抽出ＳＱＬ生成処理
     * @param serchGrade    学年
     * @param serchHrClass  組
     * @param serchdate1    学期開始日
     * @param serchdate2    学期終了日
     * @return
     */
    private String getGradeClassSql(String serchGrade, String serchHrClass, String schregNo, String serchdate1, String serchdate2){
        StringBuffer stb = new StringBuffer();
        stb.append("  SELECT DISTINCT");
        stb.append("   DB1.YEAR,");
        stb.append("   DB1.GRADE,");
        stb.append("   DB1.HR_CLASS,");
        stb.append("   DB1.SEMESTER,");
        stb.append("   SUBSTR(DB1.SCHREGNO, 1, 4),");
        stb.append("   SUBSTR(DB1.SCHREGNO, 5, 4),");
        stb.append("   DB1.SCHREGNO,");
        stb.append("   DB1.ATTENDNO,");
        stb.append("   DB2.NAME AS SEITO_KANJI,");
        stb.append("   VALUE(DB2.NAME_KANA,'') AS SEITO_KANA, ");
        stb.append("   CASE WHEN DB2.BIRTHDAY IS NOT NULL THEN YEAR('" + _param._year + "-04-01' - DB2.BIRTHDAY) END AS AGE, ");
        if ("1".equals(_param._taishousha)) {
            stb.append("   VALUE(DB4.GUARD_ZIPCD,'') AS ZIPCD1,");
            stb.append("   VALUE(DB4.GUARD_ADDR1,'') AS ADDRESS1,");
            stb.append("   value(DB4.GUARD_ADDR2,'') AS ADDRESS2,");
            stb.append("   VALUE(DB4.GUARD_TELNO,'') AS TELNO1,");
            stb.append("   ''                        AS TELNO2,");
            stb.append("   VALUE(DB4.GUARD_NAME,'') AS GUARD_NAME,");
        } else if ("2".equals(_param._taishousha)) {
            stb.append("   VALUE(DB4.GUARANTOR_ZIPCD,'') AS ZIPCD1,");
            stb.append("   VALUE(DB4.GUARANTOR_ADDR1,'') AS ADDRESS1,");
            stb.append("   value(DB4.GUARANTOR_ADDR2,'') AS ADDRESS2,");
            stb.append("   VALUE(DB4.GUARANTOR_TELNO,'') AS TELNO1,");
            stb.append("   ''                        AS TELNO2,");
            stb.append("   VALUE(DB4.GUARANTOR_NAME,'') AS GUARD_NAME,");
        } else {
            stb.append("   VALUE(DB4.SEND_ZIPCD,'') AS ZIPCD1,");
            stb.append("   VALUE(DB4.SEND_ADDR1,'') AS ADDRESS1,");
            stb.append("   value(DB4.SEND_ADDR2,'') AS ADDRESS2,");
            stb.append("   VALUE(DB4.SEND_TELNO,'') AS TELNO1,");
            stb.append("   VALUE(DB4.SEND_TELNO2,'') AS TELNO2,");
            stb.append("   VALUE(DB4.SEND_NAME,'') AS GUARD_NAME,");
        }
        stb.append("   VALUE(DB6.FINSCHOOL_NAME,'') AS J_NAME,");
        stb.append("    VALUE(DB8.ABBV1,'') AS SEX,");
        stb.append("   DB2.BIRTHDAY ");
        stb.append(" FROM");
        stb.append("   SCHREG_REGD_DAT DB1");
        stb.append(" INNER JOIN SCHREG_BASE_MST DB2 ON");
        stb.append("   DB1.SCHREGNO = DB2.SCHREGNO");
        stb.append(" INNER JOIN SCHREG_REGD_HDAT DB7 ON");
        stb.append("   DB1.YEAR = DB7.YEAR");
        stb.append("   AND DB1.SEMESTER = DB7.SEMESTER");
        stb.append("   AND DB1.GRADE = DB7.GRADE");
        stb.append("   AND DB1.HR_CLASS = DB7.HR_CLASS");
        if ("1".equals(_param._taishousha) || "2".equals(_param._taishousha)) {
            stb.append(" LEFT  JOIN GUARDIAN_DAT DB4 ON");
            stb.append("   DB2.SCHREGNO = DB4.SCHREGNO");
        } else {
            stb.append(" LEFT  JOIN SCHREG_SEND_ADDRESS_DAT DB4 ON");
            stb.append("   DB2.SCHREGNO = DB4.SCHREGNO");
            final String div = "3".equals(_param._taishousha) ? "1" : "2";
            stb.append("   AND DB4.DIV = '" + div + "'");
        }
        if ("1".equals(_param._rishuuCheck)) {
            stb.append(" INNER JOIN SUBCLASS_STD_SELECT_DAT SUB_STD ON");
            stb.append("   DB1.YEAR = SUB_STD.YEAR");
            stb.append("   AND DB1.SEMESTER = SUB_STD.SEMESTER");
            stb.append("   AND DB1.SCHREGNO = SUB_STD.SCHREGNO");
        }
        stb.append(" LEFT  JOIN FINSCHOOL_MST DB6 ON");
        stb.append("   DB2.FINSCHOOLCD = DB6.FINSCHOOLCD");
        stb.append(" LEFT JOIN NAME_MST DB8 ON");
        stb.append("   DB8.NAMECD1='Z002' AND DB8.NAMECD2=DB2.SEX");
        stb.append(" LEFT JOIN (");
        stb.append("   SELECT");
        stb.append("     SCHREGNO,");
        stb.append("     ZIPCD,");
        stb.append("     TELNO,");
        stb.append("     ADDR1,");
        stb.append("     ADDR2");
        stb.append("   FROM");
        stb.append("     SCHREG_ADDRESS_DAT W1");
        stb.append("   WHERE");
        stb.append("     (W1.SCHREGNO,W1.ISSUEDATE) IN (");
        stb.append("       SELECT SCHREGNO,");
        stb.append("              MAX(ISSUEDATE)");
        stb.append("       FROM   SCHREG_ADDRESS_DAT W2");
        stb.append("       WHERE  W2.ISSUEDATE <= '" + serchdate2 + "'");
        stb.append("         AND (W2.EXPIREDATE IS NULL " + " OR W2.EXPIREDATE >= '" + serchdate1 + "')");
        stb.append("       GROUP BY SCHREGNO )");
        stb.append(" )DB3 ON DB3.SCHREGNO = DB1.SCHREGNO");
        stb.append(" WHERE");
        stb.append("       DB1.YEAR = '" + _param._year + "'");
        stb.append("   AND DB1.SEMESTER = '" + _param._semester + "'");
        if (_param._kaiPageOff && "1".equals(_param._choice)) {
            stb.append("   AND DB1.GRADE || DB1.HR_CLASS IN " + _param._kaiPageOffGradeHr + " ");
        } else {
            stb.append("   AND DB1.GRADE = '" + serchGrade + "'");
            stb.append("   AND DB1.HR_CLASS = '" + serchHrClass + "'");
        }
        if (!StringUtils.isBlank(schregNo)) {
            stb.append("   AND DB1.SCHREGNO IN " + schregNo + " ");
        }
        if (_param._kaiPageOff) {
            stb.append(" ORDER BY substr(DB1.SCHREGNO, 1, 4) DESC, substr(DB1.SCHREGNO, 5, 4) ");
        } else {
            stb.append(" ORDER BY DB1.GRADE, DB1.HR_CLASS, DB1.ATTENDNO");
        }

        log.debug("学年・組単位の帳票出力データ抽出SQL=" + stb.toString());

        return stb.toString();

    }

    private Student getStudent(final List studentList, final String schregno) {
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (student._schregno.equals(schregno)) {
                return student;
            }
        }
        return null;
    }

    private String getTotalCreditSql(String serchGrade, String serchHrClass) {
        final StringBuffer stb = new StringBuffer();
        // 表示対象学籍番号（指定年度の履修科目登録がある生徒）
        stb.append(" WITH SCHREGNOS AS ( ");
        stb.append("     SELECT DISTINCT T1.SCHREGNO ");
        stb.append("     FROM SCHREG_REGD_DAT T1 ");
        stb.append("     WHERE T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
        if (_param._kaiPageOff && "1".equals(_param._choice)) {
            stb.append("   AND T1.GRADE || T1.HR_CLASS IN " + _param._kaiPageOffGradeHr + " ");
        } else {
            stb.append("         AND T1.GRADE = '" + serchGrade + "' ");
            stb.append("         AND T1.HR_CLASS = '" + serchHrClass + "' ");
        }
        // 成績（履修済み科目）の単位
        stb.append(" ), STUDYREC AS ( ");
        stb.append("     SELECT T1.YEAR, T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, ");
        stb.append("            SUM(VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0)) AS CREDIT ");
        stb.append("     FROM SCHREG_STUDYREC_DAT T1 ");
        stb.append("     INNER JOIN SCHREGNOS T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR < '" + _param._year + "' ");
        stb.append("         AND T1.CLASSCD <= '90' ");
        stb.append("     GROUP BY T1.YEAR, T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
        // 成績（今年度履修した科目）の単位
        stb.append(" ), RECORD AS ( ");
        stb.append("     SELECT T1.YEAR, T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD,  ");
        stb.append("            SUM(");
        stb.append("              CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0) ");
        stb.append("                   ELSE T1.GET_CREDIT ");
        stb.append("              END) AS CREDIT ");
        stb.append("     FROM RECORD_DAT T1 ");
        stb.append("     INNER JOIN SCHREGNOS T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.CLASSCD <= '90' ");
        stb.append("     GROUP BY T1.YEAR, T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
        // 成績（履修登録科目）の単位（次年度指定の場合、次年度 + 今年度。今年度指定の場合、今年度のみ。）
        stb.append(" ), SUBCLASS_STD_SELECT AS ( ");
        stb.append("     SELECT T1.YEAR, T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, SUM(T3.CREDITS) AS CREDIT ");
        stb.append("     FROM SUBCLASS_STD_SELECT_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T2.YEAR = '" + _param._year + "' ");
        stb.append("         AND T2.SEMESTER = '" + _param._semester + "' ");
        stb.append("     INNER JOIN CREDIT_MST T3 ON T3.YEAR = T2.YEAR ");
        stb.append("         AND T3.COURSECD = T2.COURSECD ");
        stb.append("         AND T3.MAJORCD = T2.MAJORCD ");
        stb.append("         AND T3.GRADE = T2.GRADE ");
        stb.append("         AND T3.COURSECODE = T2.COURSECODE ");
        stb.append("         AND T3.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T3.SUBCLASSCD = T1.SUBCLASSCD     ");
        stb.append("     INNER JOIN SCHREGNOS T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("     WHERE ");
        if (!_param._year.equals(_param._ctrlYear)) {
            stb.append("         (T1.YEAR = '" + _param._ctrlYear + "' AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
            stb.append("           OR T1.YEAR = '" + _param._year + "' AND T1.SEMESTER = '" + _param._semester + "') ");
        } else {
            stb.append("         (T1.YEAR = '" + _param._year + "' AND T1.SEMESTER = '" + _param._semester + "') ");
        }
        stb.append("         AND T1.CLASSCD <= '90' ");
        stb.append("     GROUP BY T1.YEAR, T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
        // 高認
        stb.append("   UNION ALL ");
        stb.append("     SELECT T1.YEAR, T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, VALUE(T3.CREDITS, 0) AS CREDIT ");
        stb.append("     FROM SCH_COMP_DETAIL_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T2.YEAR = T1.YEAR ");
        stb.append("     INNER JOIN CREDIT_MST T3 ON T3.YEAR = T2.YEAR ");
        stb.append("         AND T3.COURSECD = T2.COURSECD ");
        stb.append("         AND T3.MAJORCD = T2.MAJORCD ");
        stb.append("         AND T3.GRADE = T2.GRADE ");
        stb.append("         AND T3.COURSECODE = T2.COURSECODE ");
        stb.append("         AND T3.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T3.SUBCLASSCD = T1.SUBCLASSCD     ");
        stb.append("     INNER JOIN SCHREGNOS T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("     WHERE ");
        if (!_param._year.equals(_param._ctrlYear)) {
            stb.append("         (T1.YEAR = '" + _param._ctrlYear + "' AND T2.SEMESTER = '" + _param._ctrlSemester + "' ");
            stb.append("           OR T1.YEAR = '" + _param._year + "' AND T2.SEMESTER = '1') ");
        } else {
            stb.append("         (T1.YEAR = '" + _param._year + "' AND T2.SEMESTER = '" + _param._ctrlSemester + "') ");
        }
        stb.append("         AND T1.KOUNIN = '1' ");
        stb.append("         AND T1.CLASSCD <= '90' ");
        // 増単
        stb.append("   UNION ALL ");
        stb.append("     SELECT T1.YEAR, T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, VALUE(T1.ADD_CREDIT, 0) AS CREDIT ");
        stb.append("     FROM SCH_COMP_DETAIL_DAT T1 ");
        stb.append("     INNER JOIN SCHREGNOS T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("     WHERE ");
        if (!_param._year.equals(_param._ctrlYear)) {
            stb.append("         (T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("           OR T1.YEAR = '" + _param._year + "') ");
        } else {
            stb.append("         (T1.YEAR = '" + _param._year + "') ");
        }
        stb.append("         AND VALUE(T1.KOUNIN, '') <> '1' ");
        stb.append("         AND T1.CLASSCD <= '90' ");
        stb.append(" ), YEAR_SUBCLASS AS ( ");
        stb.append("     SELECT YEAR, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD FROM STUDYREC ");
        stb.append("     UNION ");
        stb.append("     SELECT YEAR, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD FROM RECORD ");
        stb.append("     UNION ");
        stb.append("     SELECT YEAR, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD FROM SUBCLASS_STD_SELECT ");
        // メイン表
        stb.append(" ), CREDIT_MAIN AS ( ");
        stb.append("     SELECT  ");
        stb.append("         T1.YEAR, T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, ");
        stb.append("         T2.CREDIT AS STUDYREC_CREDIT, ");
        stb.append("         T3.CREDIT AS RECORD_CREDIT, ");
        stb.append("         T4.CREDIT AS SUBCLASS_STD_SELECT_CREDIT, ");
        // 単位の優先順位 => SCHREGNO_STUDYREC_DAT, RECORD_DAT, SUBCLASS_STD_SELECT(CREDIT_MST)
        stb.append("         CASE WHEN T2.CREDIT IS NOT NULL THEN T2.CREDIT ");
        stb.append("              WHEN T3.CREDIT IS NOT NULL THEN T3.CREDIT ");
        stb.append("              ELSE T4.CREDIT ");
        stb.append("         END AS CREDIT ");
        stb.append("     FROM ");
        stb.append("         YEAR_SUBCLASS T1     ");
        stb.append("         LEFT JOIN STUDYREC T2 ON T2.YEAR = T1.YEAR AND T2.SCHREGNO = T1.SCHREGNO AND T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         LEFT JOIN RECORD T3 ON T3.YEAR = T1.YEAR AND T3.SCHREGNO = T1.SCHREGNO AND T3.CLASSCD = T1.CLASSCD AND T3.SCHOOL_KIND = T1.SCHOOL_KIND AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         LEFT JOIN SUBCLASS_STD_SELECT T4 ON T4.YEAR = T1.YEAR AND T4.SCHREGNO = T1.SCHREGNO AND T4.CLASSCD = T1.CLASSCD AND T4.SCHOOL_KIND = T1.SCHOOL_KIND AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" ), BASE_YEAR_DETAIL AS ( ");
        stb.append("     SELECT MAIN.SCHREGNO, SC_YD.BASE_REMARK1 AS GRD_YOTEI ");
        stb.append("     FROM SCHREGNOS MAIN ");
        stb.append("     INNER JOIN SCHREG_BASE_YEAR_DETAIL_MST SC_YD ON SC_YD.SCHREGNO = MAIN.SCHREGNO ");
        stb.append("          AND SC_YD.YEAR = '" + _param._year + "' ");
        stb.append("          AND SC_YD.BASE_SEQ = '001' ");
        stb.append(" ), BASE_DETAIL AS ( ");
        stb.append("     SELECT MAIN.SCHREGNO, SC_D.BASE_REMARK1 AS SATEI_TANNI ");
        stb.append("     FROM SCHREGNOS MAIN ");
        stb.append("     INNER JOIN SCHREG_BASE_DETAIL_MST SC_D ON SC_D.SCHREGNO = MAIN.SCHREGNO ");
        stb.append("          AND SC_D.BASE_SEQ = '004' ");
        stb.append("          AND SC_D.BASE_REMARK1 IS NOT NULL ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, L1.GRD_YOTEI, L2.TOTAL_CREDIT, L3.SATEI_TANNI ");
        stb.append(" FROM ");
        stb.append("    (SELECT SCHREGNO FROM BASE_YEAR_DETAIL ");
        stb.append("     UNION ");
        stb.append("     SELECT SCHREGNO FROM BASE_DETAIL ");
        stb.append("     UNION ");
        stb.append("     SELECT SCHREGNO FROM CREDIT_MAIN ");
        stb.append("    ) T1 ");
        stb.append("    LEFT JOIN BASE_YEAR_DETAIL L1 ON L1.SCHREGNO = T1.SCHREGNO  ");
        stb.append("    LEFT JOIN (SELECT SCHREGNO, SUM(CREDIT) AS TOTAL_CREDIT FROM CREDIT_MAIN ");
        stb.append("               GROUP BY SCHREGNO) L2 ON L2.SCHREGNO = T1.SCHREGNO ");
        stb.append("    LEFT JOIN BASE_DETAIL L3 ON L3.SCHREGNO = T1.SCHREGNO  ");
        stb.append(" ORDER BY ");
        stb.append("     T1.SCHREGNO ");
        log.debug(stb);
        return stb.toString();
    }

    /** 生徒クラス */
    private class Student {
        final String _year;
        final String _grade;
        final String _hrClass;
        final String _semester;
        final String _schregno;
        final int _attendno;
        final String _seitoKanji;
        final String _seitoKana;
        final String _age;
        final String _zipcd1;
        final String _address1;
        final String _address2;
        final String _telno1;
        final String _guardName;
        final String _telno2;
        final String _jName;
        final String _sex;
        final String _birthday;
        String _totalCredit;

        Student(
                final String year,
                final String grade,
                final String hrClass,
                final String semester,
                final String schregno,
                final int attendno,
                final String seitoKanji,
                final String seitoKana,
                final String age,
                final String zipcd1,
                final String address1,
                final String address2,
                final String telno1,
                final String guardName,
                final String telno2,
                final String jName,
                final String sex,
                final String birthday
        ) {
            _year = year;
            _grade = grade;
            _hrClass = hrClass;
            _semester = semester;
            _schregno = schregno;
            _attendno = attendno;
            _seitoKanji = seitoKanji;
            _seitoKana = seitoKana;
            _age = age;
            _zipcd1 = zipcd1;
            _address1 = address1;
            _address2 = address2;
            _telno1 = telno1;
            _guardName = guardName;
            _telno2 = telno2;
            _jName = jName;
            _sex = sex;
            _birthday = birthday;
        }

        public String getFormatBirthDay() {
            if (null == _birthday || _birthday.length() < 10 || StringUtils.split(_birthday, "-").length != 3) {
                return null;
            }
            final String[] split = StringUtils.split(_birthday, "-");
            final int year = Integer.parseInt(split[0]);
            final int month = Integer.parseInt(split[1]);
            final int day = Integer.parseInt(split[2]);
            String retVal[] = {"", ""};
            retVal[0] = StringUtils.defaultString(KNJ_EditDate.gengouAlphabetMark(db2, year, month, day));
            final String[] tate_format = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(db2, _birthday));
            if (null != tate_format && tate_format.length == 4) {
            	if ("元".equals(tate_format[1])) {
            		retVal[1] = "1";
            	} else if (NumberUtils.isDigits(tate_format[1])) {
            		retVal[1] = String.valueOf(tate_format[1]);
            	}
            }
            return retVal[0] + retVal[1] + "-" + _df.format(month) + "-" + _df.format(day);
        }
    }

    // ======================================================================
    /**
     * 名称マスタ。
     */
    private String getNameMstInfo(final DB2UDB db2)
        throws SQLException {

        String retSeirekiFlg = "";

        final String sql = sqlNameMst();
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                retSeirekiFlg = rs.getString("seirekiFlg");
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return retSeirekiFlg;
    }

    private String sqlNameMst() {
        String ret ="select"
                + "  VALUE(NAME1, '') as seirekiFlg"
                + " from"
                + "   NAME_MST"
                + " where"
                + "   NAMECD1 = 'Z012' and"
                + "   NAMECD2 = '00' "
                ;
        return ret;
    }

    /**
     * 日付を指定されたフォーマットに設定し文字列にして返す
     * @param s
     * @return
     */
    private String fomatSakuseiDate(String cnvDate, String sfmt, String chgfmt) {

        String retDate = "";
        try {
            DateFormat foramt = new SimpleDateFormat(sfmt);
            //文字列よりDate型へ変換
            Date date1 = foramt.parse(cnvDate);
            // 年月日のフォーマットを指定
            SimpleDateFormat sdf1 = new SimpleDateFormat(chgfmt);
            // Date型より文字列へ変換
            retDate = sdf1.format(date1);
        } catch( Exception e ){
            log.error("setHeader set error!");
        }
        return retDate;
    }
    private void dumpParam(final HttpServletRequest request) {
        log.fatal("$Revision: 66922 $ $Date: 2019-04-11 11:24:28 +0900 (木, 11 4 2019) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
    }

    /** パラメータクラス */
    private class Param {
        private final String _programid;
        private final String _year;
        private final String _semester;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _choice;
        private final String[] _gradeHrclass;
        private final String _taishousha;
        private final String _rishuuCheck;
        private final boolean _kaiPageOff;
        private String _kaiPageOffGradeHr;

        private String schregNo = "";
        private int pagecnt = 0;    // 現在ページ数
        private boolean seirekiOutHantei = false;


        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _programid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");

            _choice = request.getParameter("CHOICE");               // 1:クラス指定、2:個人指定
            _taishousha = request.getParameter("TAISHOUSHA");       // 出力住所1:保護者 2:負担者 3:その他１ 4:その他２
            _rishuuCheck = request.getParameter("RISHUUCHECK");     // 履修登録者を出力
            _kaiPageOff = "1".equals(request.getParameter("KAIPAGE_OFF"));

            // 学籍番号
            if(_choice.equals("1")){
                _gradeHrclass = request.getParameterValues("CATEGORY_SELECTED"); // 学年＋組または学籍番号
                _kaiPageOffGradeHr = "(";
                for (int ia = 0; ia < _gradeHrclass.length; ia++) {
                    if (_gradeHrclass[ia] == null)
                        break;
                    if (ia > 0) {
                        _kaiPageOffGradeHr = _kaiPageOffGradeHr + ",";
                    }
                    _kaiPageOffGradeHr = _kaiPageOffGradeHr + "'" + _gradeHrclass[ia] + "'";
                }
                _kaiPageOffGradeHr = _kaiPageOffGradeHr + ")";
            } else {
                _gradeHrclass = new String[] {request.getParameter("GRADE_HR_CLASS")}; // 学年＋組
                String c_select[] = request.getParameterValues("CATEGORY_SELECTED"); // 学籍番号SQL抽出用文字列
                schregNo = "(";
                for (int ia = 0; ia < c_select.length; ia++) {
                    if (c_select[ia] == null)
                        break;
                    if (ia > 0)
                        schregNo = schregNo + ",";
                    schregNo = schregNo + "'" + c_select[ia] + "'";
                }
                schregNo = schregNo + ")";
            }
        }

        public String getHogosyaTitle() {
            String retTitle = "";
            if ("1".equals(_taishousha)) {
                retTitle = "保護者";
            } else if ("2".equals(_taishousha)) {
                retTitle = "負担者";
            } else if ("3".equals(_taishousha)) {
                retTitle = "その他１";
            } else if ("4".equals(_taishousha)) {
                retTitle = "その他２";
            }
            return retTitle;
        }
        public void load(DB2UDB db2) throws SQLException {
            String seirekiHantei = getNameMstInfo(db2);
            if(seirekiHantei.equals("2")){
                seirekiOutHantei = true;
            }
            return;
        }

    }

    // ======================================================================
    private class Form {
        private Vrw32alp _svf;

        public Form(final String file,final HttpServletResponse response) throws IOException {
            _svf = new Vrw32alp();

            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            _svf.VrSetForm(file, 1);
        }

        private void closeSvf() {
            if (!_hasData) {
                _svf.VrSetForm("MES001.frm", 0);
                _svf.VrsOut("note", "note");
                _svf.VrEndPage();
            }

            final int ret = _svf.VrQuit();
            log.info("===> VrQuit():" + ret);
        }
    }


    private boolean openDb(final DB2UDB db2) {
        try {
            db2.open();
        } catch (final Exception ex) {
            log.error("db open error!", ex);
            return true;
        }
        return false;
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

}
