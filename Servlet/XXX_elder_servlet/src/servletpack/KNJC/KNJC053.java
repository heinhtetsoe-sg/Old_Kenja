// kanji=漢字
/*
 * $Id: d4a7bae59cfa81f4501bd5141b8b370edc49a3d1 $
 *
 * 作成日:
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJC;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/*
 *
 *  学校教育システム 賢者 [出欠管理]  出席簿（科目別）
 *
 *  2004/07/07・新様式としてKNJC050とは別途作成
 *  2004/09/14 yamashiro・SQL文の不具合を修正->ペナルティ欠課選択の場合SQLの不具合が起きる
 *  2004/09/28 yamashiro・単位がnullの場合の不具合を修正
 *                      ・校時名称が'SHR'の場合は校時を出力しない-->10/06:数字以外は出力しないへ変更
 *  2004/09/29 yamashiro・SHRやLHRの場合総ページ数が０になる不具合を修正
 *                      ・遅刻コードに'7'を追加
 *  2004/10/06 yamashiro・出欠データ出力において時間割データにないデータは出力しない
 *  2004/10/28 yamashiro・'〜'出力の不具合を修正
 *  2004/11/15 yamashiro・累計処理は時間割データとリンク(学籍番号・日付・校時)したデータを集計する
 *  2005/03/28 yamashiro・新様式として作成
 *  2005/04/28 yamashiro・プログラムを共通化する => KNJZ/detail/KNJDefineCode.classを参照
 *                      ・時間割講座データ集計フラグを使用する場合と使用しない場合の処理を行う
 *                      ・時間割講座データ集計フラグが集計しないとなっている箇所を網掛け表示
 *  2005/06/03 yamashiro・集計表および明細表を科目単位で出力
 *
 */

public class KNJC053 {

    private static final Log log = LogFactory.getLog(KNJC053.class);
    private boolean hasdata;
    private static String FROM_TO_MARK = "\uFF5E";

    private static final int KANSAN0_NASI = 0;                       // 換算0：無し
    private static final int KANSAN1_SEISU_GAKKI = 1;                // 換算1：学期ごと整数
    private static final int KANSAN2_SEISU_NENKAN = 2;               // 換算2：年間で整数
    private static final int KANSAN3_SYOSU_GAKKI = 3;                // 換算3：学期ごと小数
    private static final int KANSAN4_SYOSU_NENKAN = 4;               // 換算4：年間で小数
    private static final int KANSAN5_SEISU_NENKAN_AMARIKURIAGE = 5;  // 換算5：年間で整数（余り繰り上げ）

    private static final int SEMES_ALL = 9;

    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        log.fatal("$Revision: 73381 $ $Date: 2020-03-30 12:08:11 +0900 (月, 30 3 2020) $"); // CVSキーワードの取り扱いに注意

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス

        try {
            // print svf設定
            response.setContentType("application/pdf");
            svf.VrInit();                                         //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());     //PDFファイル名の設定

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
            db2.open();

            // パラメータの取得
            KNJServletUtils.debugParam(request, log);
            Param param = new Param(request, db2);

            printSvf(db2, svf, param);

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            // 終了処理
            if (!hasdata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
            try {
                db2.close();
            } catch (Exception ex) {
                log.error("db close error!", ex);
            }//try-cathの括り
        }

    }   //doGetの括り


    /**
     *
     *  印刷処理
     *
     */
    private void printSvf(final DB2UDB db2, final Vrw32alp svf, final Param param) {
        //集計表
        if (param._output4 != null) {
            final CALC o = new CALC(param);
            log.info("集計票");
            o.printSvf(db2, svf);
            if (o.hasdata) {
                hasdata = true;
            }
        }

        //明細票
        if (param._output5 != null) {
            final LIST o = new LIST(param);
            log.info("明細票");
            o.printSvf(db2, svf);
            if (o.hasdata) {
                hasdata = true;
            }
        }
    }

    private static class Param {

        final String _year;
        final String _semester;
        /** 受講コード */
        final String _attendClassCd;
        final String _classCd;
        final String _subclassCd;
        final String _groupCd;

        /** 印刷範囲開始 */
        final String _dateFrom;
        /** 印刷範囲終了 */
        final String _dateTo;

        String _ctrlDateString;
        String _chairName;
        String _subclassName;
        String _staffName;
        String _subclassAbbv;
        String _schoolKind;
        String _curriculumCd;
        String _semestername;

        /** 適用開始日付 */
        final String _appDate;

        /** "注意" or "超過" */
        final String _tyuiTyoka;

        /** 遅刻・早退は欠課換算前の値を表示するか */
        final String _chikokuHyoujiFlg;

        /** テスト項目マスタテーブル */
        final String _useTestCountflg;

        /** 集計表出力 */
        final String _output4;
        /** 明細票出力 */
        final String _output5;

        /** 注意週数 */
        String _warnSemester;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;

        /** 校種を使用するか */
        final String _useSchool_KindField;

        /** 氏名欄に学籍番号の表示/非表示 1:表示 それ以外:非表示 */
        final String _use_SchregNo_hyoji;

        /** 明細で生徒の名簿適用日付内の時間割のみを対象とする */
        final String _knjc053useMeisaiExecutedate;

        final KNJSchoolMst _knjSchoolMst;

        final KNJDefineSchool _definecode;     //各学校における定数等設定
        final int _semesdiv;
        private boolean _hasCreditMstAbsenceWarn; //"CREDIT_MST"の"ABSENCE_WARN"フィールドが存在しないＤＢに対応する為、tableのfield存在チェックを追加
        final String _absenceDiv = "2";
        final Map _attendParamMap;
        final List<Semester> _semesterList;

        final boolean _isOutputDebugAll;
        final boolean _isOutputDebug;

        final String _nendo;
        final String _dateFromFormat;
        final String _dateToFormat;
        final SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd");
        final SimpleDateFormat sdf3 = new SimpleDateFormat("yy/MM/dd");

        public Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");            //年度
            _semester = request.getParameter("SEMESTER");        //学期
            _attendClassCd = request.getParameter("ATTENDCLASSCD");   //受講コード
            _classCd = request.getParameter("CLASSCD");         //教科コード
            _subclassCd = request.getParameter("SUBCLASSCD");      //科目コード
            _groupCd = request.getParameter("GROUPCD");         //群コード
            //  日付型を変換
            _dateFrom = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));    //印刷範囲開始
            _dateTo = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE2"));   //印刷範囲終了
            //if( request.getParameter("OUTPUT3")!=null )param[15] = "on";      //遅刻を欠課に換算 null:無
            _appDate = request.getParameter("APPDATE"); //適用開始日付
            _tyuiTyoka = request.getParameter("TYUI_TYOUKA"); // "注意" or "超過"
            _chikokuHyoujiFlg = request.getParameter("chikokuHyoujiFlg"); // 遅刻・早退は欠課換算前の値を表示するか
            _useTestCountflg = request.getParameter("useTestCountflg"); // テスト項目マスタテーブル

            _output4 = request.getParameter("OUTPUT4");
            _output5 = request.getParameter("OUTPUT5");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _use_SchregNo_hyoji = request.getParameter("use_SchregNo_hyoji");
            _knjc053useMeisaiExecutedate = request.getParameter("knjc053useMeisaiExecutedate");

            KNJSchoolMst knjSchoolMst = null;
            try {
                final Map paramMap = new HashMap();
                if ("1".equals(_useSchool_KindField)) {
                    paramMap.put("SCHOOL_KIND", StringUtils.split(_subclassCd, "-")[3]);
                }
                knjSchoolMst = new KNJSchoolMst(db2, _year, paramMap);
            } catch (Exception e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _knjSchoolMst = knjSchoolMst;

            //教科コード・学校区分・学期制等定数取得
            KNJDefineSchool definecode = null;
            try {
                definecode = new KNJDefineSchool();
                definecode.defineCode(db2, _year);         //各学校における定数等設定
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!", ex);
            }
            _definecode = definecode;
            _semesdiv = _definecode.semesdiv;

            setWarnSemester(db2);

            //  作成日(現在処理日)
            _ctrlDateString = KNJ_EditDate.h_format_JP(db2, getControlDate(db2));

            //  講座名、科目名
            setChairInfo(db2);

            //  担任名
            setChairStaff(db2);

            _hasCreditMstAbsenceWarn = KnjDbUtils.setTableColumnCheck(db2, "CREDIT_MST", "ABSENCE_WARN");

            _attendParamMap = new HashMap();
            _attendParamMap.put("absenceDiv", _absenceDiv);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("DB2UDB", db2);

            _semesterList = getSemesterList(db2);
            final Semester seme = getSemester(_semester);
            if (null != seme) {
                _semestername = seme._semestername;
            }

            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
            _dateFromFormat = KNJ_EditDate.h_format_JP(db2, _dateFrom);
            _dateToFormat = KNJ_EditDate.h_format_JP(db2, _dateTo);
            final String[] outputDebug = StringUtils.split(getDbPrginfoProperties(db2, "outputDebug"));
            _isOutputDebugAll = ArrayUtils.contains(outputDebug, "all");
            _isOutputDebug = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "1");
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJC053' AND NAME = '" + propName + "' "));
        }

        /**
         * 遅刻・早退は欠課換算前の値を表示するか
         * @return 遅刻・早退は欠課換算前の値を表示するならtrue、そうでなければfalse
         */
        private boolean isPrintRawLateEarly() {
            return "1".equals(_chikokuHyoujiFlg);
        }

        /**
         * 出欠は整数で表示するか
         * @return 出欠は整数で表示するならtrue、そうでなければfalse
         */
        private boolean isAttendPrintInt() {
            final int ac = getAbsentConv();
            return ac != KANSAN3_SYOSU_GAKKI && ac != KANSAN4_SYOSU_NENKAN;
        }

        /**
         * 出欠換算は学期ごとか
         * @return 出欠換算は学期ごとならtrue、そうでなければfalse
         */
        private boolean isConvOnSemester() {
            final int ac = getAbsentConv();
            return ac != KANSAN2_SEISU_NENKAN && ac != KANSAN4_SYOSU_NENKAN && ac != KANSAN5_SEISU_NENKAN_AMARIKURIAGE;
        }

        /**
         * 欠課を表示するか
         * @param semester 学期
         * @return 欠課を表示するか
         */
        private boolean isPrintSemesKekka(int semester) {
            final int ac = getAbsentConv();
            final boolean isPrintKekka;
            if (SEMES_ALL == semester) {
                isPrintKekka = ac != KANSAN0_NASI; // 総欠課数は換算無し以外で表示する
            } else {
                isPrintKekka = !(ac == KANSAN0_NASI || ac == KANSAN2_SEISU_NENKAN || ac == KANSAN4_SYOSU_NENKAN || ac == KANSAN5_SEISU_NENKAN_AMARIKURIAGE);
            }
            return isPrintKekka;
        }

        private int getAbsentConv() {
            return _definecode == null ? 0 : _definecode.absent_cov;
        }

        private void setWarnSemester(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT T1.YEAR, T1.SEMESTER, T1.SDATE, T1.EDATE, T2.SEMESTER AS NEXT_SEMESTER, T2.SDATE AS NEXT_SDATE ");
            stb.append(" FROM SEMESTER_MST T1 ");
            stb.append(" LEFT JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND INT(T2.SEMESTER) = INT(T1.SEMESTER) + 1 ");
            stb.append(" WHERE T1.YEAR = '" + _year + "' ");
            stb.append("     AND T1.SEMESTER <> '9' ");
            stb.append("     AND (('" + _dateTo + "' BETWEEN T1.SDATE AND T1.EDATE) ");
            stb.append("          OR (T1.EDATE < '" + _dateTo + "' AND '" + _dateTo + "' < VALUE(T2.SDATE, '9999-12-30'))) ");
            stb.append(" ORDER BY T1.SEMESTER ");
            _warnSemester = KnjDbUtils.getString(KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb.toString())), "SEMESTER");
        }


        /**
         *  講座名、科目名を取得するメソッド
         */
        private void setChairInfo(final DB2UDB db2) {

            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT CHAIRNAME,SUBCLASSNAME,SUBCLASSABBV ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(       ",W1.SCHOOL_KIND ");
                stb.append(       ",W1.CURRICULUM_CD ");
            }
            stb.append("FROM CHAIR_DAT W1,SUBCLASS_MST W2 ");
            stb.append("WHERE  W1.YEAR='" + _year + "' AND W1.SEMESTER='" + _semester + "' AND ");
            stb.append(       "W1.CHAIRCD='" + _attendClassCd + "' ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(       "AND W1.CLASSCD=W2.CLASSCD ");
                stb.append(       "AND W1.SCHOOL_KIND=W2.SCHOOL_KIND ");
                stb.append(       "AND W1.CURRICULUM_CD=W2.CURRICULUM_CD ");
            }
            stb.append(       "AND W1.SUBCLASSCD=W2.SUBCLASSCD");

            final Map<String, String> row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb.toString()));

            _chairName = KnjDbUtils.getString(row, "CHAIRNAME");
            _subclassName = KnjDbUtils.getString(row, "SUBCLASSNAME");
            _subclassAbbv = KnjDbUtils.getString(row, "SUBCLASSABBV");
            if ("1".equals(_useCurriculumcd)) {
                _schoolKind = KnjDbUtils.getString(row, "SCHOOL_KIND");
                _curriculumCd = KnjDbUtils.getString(row, "CURRICULUM_CD");
            }
        }

        /**
         *  講座担任名を取得するメソッド
         */
        private void setChairStaff(final DB2UDB db2) {

            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT  STAFFNAME, STAFFNAME_SHOW, STAFFNAME_KANA, STAFFNAME_ENG ");
            stb.append("FROM    CHAIR_STF_DAT W1, STAFF_MST W2 ");
            stb.append("WHERE   W1.YEAR = '" + _year + "' ");
            stb.append(    "AND W1.SEMESTER = '" + _semester + "' ");
            stb.append(    "AND W1.CHAIRCD = '" + _attendClassCd + "' ");
            stb.append(    "AND W1.STAFFCD = W2.STAFFCD ");
            stb.append(    "AND W1.CHARGEDIV = 1 ");
            stb.append("ORDER BY W1.STAFFCD");
            //氏名
            _staffName = KnjDbUtils.getString(KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb.toString())), "STAFFNAME");
        }

        private String getControlDate(final DB2UDB db2) {
            //処理日付
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT CTRL_DATE FROM CONTROL_MST WHERE CTRL_NO = '01' "));
        }

        private Semester getSemester(final String semester) {
            for (final Semester seme : _semesterList) {
                if (semester.equals(seme._semester)) {
                    return seme;
                }
            }
            return null;
        }

        /** ＤＢより該当年度全学期情報を取得するメソッド **/
        private List<Semester> getSemesterList(final DB2UDB db2) {
            final List<Semester> semesterList = new ArrayList();
            final String sql = "SELECT * FROM SEMESTER_MST WHERE YEAR = '" + _year + "' ORDER BY SEMESTER";
            for (final Map row : KnjDbUtils.query(db2, sql)) {
                final String seme = KnjDbUtils.getString(row, "SEMESTER");
                final Semester semester = new Semester(seme, KnjDbUtils.getString(row, "SEMESTERNAME"), KnjDbUtils.getString(row, "SDATE"), KnjDbUtils.getString(row, "EDATE"), String.valueOf(SEMES_ALL).equals(seme) ? "1" : seme);
                semesterList.add(semester);
            }
            return semesterList;
        }

        private boolean isHoutei() {
            if ("3".equals(_knjSchoolMst._jugyouJisuFlg)) {
                return "9".equals(_semester) || null != _knjSchoolMst._semesterDiv && _knjSchoolMst._semesterDiv.equals(_semester);
            }
            return !"2".equals(_knjSchoolMst._jugyouJisuFlg);
        }
    }

    private static class Range {
        final String _semester;
        final String _sdate;
        final String _edate;
        final String _sSemester;
        public Range(final String semester, final String sdate, final String edate, final String sSemester) {
            _semester = semester;
            _sdate = sdate;
            _edate = edate;
            _sSemester = sSemester;
        }
        public String toString() {
            return "Range(" + _semester + ", " + _sdate + ", " + _edate + ")";
        }
    }

    private static class Semester extends Range {
        final String _semestername;
        public Semester(final String semester, final String semestername, final String sdate, final String edate, final String sSemester) {
            super(semester, sdate, edate, sSemester);
            _semestername = semestername;
        }
    }

    /**
     *   １月〜３月の「学期＋月」の処理 （２学期制における集計処理に対応）
     */
    private static String retSemesterMonthValue(final String strx) {
        String str = null;
        try {
            if (Integer.parseInt(strx.substring(1, strx.length())) < 4) {
                str = String.valueOf(Integer.parseInt(strx.substring(0, 1)) + 1) + "" + strx.substring(1, strx.length());
            } else {
                str = strx;
            }
        } catch (Exception ex) {
            log.error("retSemesterMonthValue!", ex);
        }
        log.debug("retSemesterMonthValue=" + str);
        return str;
    }

    private static Calendar toCal(final String date) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        final Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(sdf.parse(date));
        } catch (Exception ex) {
            log.error("toCalendar error!", ex);
        }
        return cal;
    }

    private static String toDate(final Calendar cal) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }

    private static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList<B>());
        }
        return map.get(key1);
    }

    /**
     *  PrepareStatement作成  生徒情報
     **/
    private static String setStatementSchInfo(final Param param) {
        final StringBuffer stb = new StringBuffer();

        stb.append("WITH SCHNO_A AS(");
        stb.append("    SELECT  SCHREGNO,MAX(SEMESTER) AS SEMESTER ");
        stb.append("    FROM    CHAIR_STD_DAT ");
        stb.append("    WHERE   YEAR = '" + param._year + "' ");
        stb.append("        AND SEMESTER <= '" + param._semester + "' ");
        stb.append("        AND CHAIRCD = '" + param._attendClassCd + "' ");
        stb.append("        AND '" + param._appDate + "' BETWEEN APPDATE AND APPENDDATE ");
        stb.append("        GROUP BY SCHREGNO ");
        stb.append(") ");

        stb.append(",TRANSFER_A AS(");
        stb.append("    SELECT  SCHREGNO,MAX(TRANSFER_SDATE) AS TRANSFER_SDATE ");
        stb.append("    FROM    SCHREG_TRANSFER_DAT S1,SEMESTER_MST S3 ");
        stb.append("    WHERE   TRANSFERCD IN('1','2') ");
        stb.append("        AND EXISTS(SELECT 'X' FROM SCHNO_A S2 WHERE S2.SCHREGNO = S1.SCHREGNO) ");
        stb.append("        AND FISCALYEAR(S1.TRANSFER_SDATE) = '" + param._year + "' ");
        stb.append("        AND S1.TRANSFER_SDATE < S3.EDATE ");
        stb.append("        AND S3.YEAR = '" + param._year + "' ");
        stb.append("        AND S3.SEMESTER <= '" + param._semester + "' ");
        stb.append("    GROUP BY SCHREGNO ");
        stb.append(") ");

        stb.append(",TRANSFER_B AS(");
        stb.append("    SELECT  SCHREGNO, TRANSFER_SDATE, TRANSFER_EDATE, TRANSFERCD ");
        stb.append("    FROM    SCHREG_TRANSFER_DAT S1 ");
        stb.append("    WHERE   EXISTS(SELECT 'X' FROM TRANSFER_A S2 WHERE S1.SCHREGNO = S2.SCHREGNO ");
        stb.append("                                                   AND S1.TRANSFER_SDATE = S2.TRANSFER_SDATE) ");
        stb.append(") ");

//        stb.append(",SCHREG_OFFDAYS AS(");
//        stb.append("    SELECT  T2.SCHREGNO, COUNT(*) AS OFFDAYS_COUNT ");
//        stb.append("    FROM    SCH_CHR_DAT T1 ");
//        stb.append("   INNER JOIN CHAIR_STD_DAT T2 ON T1.YEAR = T2.YEAR ");
//        stb.append("    AND T1.SEMESTER = T2.SEMESTER ");
//        stb.append("    AND T1.CHAIRCD = T2.CHAIRCD ");
//        stb.append("    AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
//        stb.append("   INNER JOIN TRANSFER_B S1 ON T1.EXECUTEDATE BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE ");
//        stb.append("    AND T2.SCHREGNO = S1.SCHREGNO ");
//        stb.append("    AND S1.TRANSFERCD = '2' ");
//        stb.append("   WHERE ");
//        stb.append("    T1.YEAR = '" + param._year +"' ");
//        stb.append("    AND T1.SEMESTER = '" + param._semester +"' ");
//        stb.append("    AND T1.CHAIRCD = '" + param._attendClassCd +"' ");
//        stb.append("    GROUP BY T2.SCHREGNO ");
//        stb.append(") ");

        stb.append("SELECT  REGD.SCHREGNO, ");
        stb.append("        BASE.NAME, ");
        stb.append("        REGDH.HR_NAMEABBV || '-' || CHAR(INT(REGD.ATTENDNO)) AS ATTENDNO, ");
        stb.append("        REGD.GRADE, ");
        stb.append("        REGD.HR_CLASS, ");
        stb.append("        REGD.ATTENDNO AS ATTENDNO1, ");
        stb.append("        REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO AS ATTENDNO2, ");
        stb.append("        CASE WHEN W4.GRD_DATE IS NOT NULL THEN '1' ELSE '0' END AS KBN_DIV1, ");
        stb.append("        CASE WHEN W4.GRD_DATE IS NOT NULL THEN W4.GRD_DATE ELSE W4.ENT_DATE END AS KBN_DATE1, ");
        stb.append("        CASE WHEN W4.GRD_DATE IS NOT NULL THEN (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A003' AND S1.NAMECD2 = W4.GRD_DIV) ");
        stb.append("             ELSE (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A002' AND S1.NAMECD2 = W4.ENT_DIV) END AS KBN_NAME1, ");
        stb.append("        W5.TRANSFERCD, ");
        stb.append("        TRSEMES.SEMESTER AS KBN_DATE2_SSEMESTER, ");
        stb.append("        TRSEMEE.SEMESTER AS KBN_DATE2_ESEMESTER, ");
        stb.append("        W5.TRANSFER_SDATE AS KBN_DATE2, ");
        stb.append("        W5.TRANSFER_EDATE AS KBN_DATE2E,");
        stb.append("        (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A004' AND S1.NAMECD2 = W5.TRANSFERCD) AS KBN_NAME2 ");
//        stb.append("        ,VALUE(W8.OFFDAYS_COUNT,0) AS OFFDAYS_COUNT ");
        stb.append("FROM    SCHNO_A W6 ");
        stb.append("INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = '" + param._year + "' AND REGD.SEMESTER = W6.SEMESTER AND REGD.SCHREGNO = W6.SCHREGNO ");
        stb.append("INNER JOIN V_SEMESTER_GRADE_MST    W2 ON W2.YEAR = '" + param._year + "' AND W2.SEMESTER = REGD.SEMESTER AND W2.GRADE = REGD.GRADE ");
        stb.append("INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("INNER JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR AND REGDH.SEMESTER = REGD.SEMESTER ");
        stb.append("                              AND REGDH.GRADE = REGD.GRADE AND REGDH.HR_CLASS = REGD.HR_CLASS ");
        stb.append("LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = REGD.SCHREGNO ");
        stb.append("                               AND ((W4.GRD_DIV IN('2','3')) ");
        stb.append("                                 OR (W4.ENT_DIV IN('4','5'))) ");
        stb.append("LEFT JOIN TRANSFER_B W5 ON W5.SCHREGNO = REGD.SCHREGNO ");
        stb.append("LEFT JOIN SEMESTER_MST TRSEMES ON TRSEMES.YEAR = '" + param._year + "' AND TRSEMES.SEMESTER <> '9' AND W5.TRANSFER_SDATE BETWEEN TRSEMES.SDATE AND TRSEMES.EDATE ");
        stb.append("LEFT JOIN SEMESTER_MST TRSEMEE ON TRSEMEE.YEAR = '" + param._year + "' AND TRSEMEE.SEMESTER <> '9' AND W5.TRANSFER_EDATE BETWEEN TRSEMEE.SDATE AND TRSEMEE.EDATE ");
//        stb.append("LEFT JOIN SCHREG_OFFDAYS W8 ON W8.SCHREGNO = REGD.SCHREGNO ");
        stb.append("ORDER BY REGD.GRADE, REGD.HR_CLASS, REGD.ATTENDNO ");

        return stb.toString();
    }


    /**
     *  PrepareStatement作成 該当講座の生徒数・単位の表
     */
    private static String prestateSchnumAndCredits(final Param param) {

        final StringBuffer stb = new StringBuffer();

        //対象講座を抽出
        stb.append("WITH CHAIR_A AS(");
        stb.append("   SELECT  CHAIRCD, SCHREGNO ");
        stb.append("   FROM    CHAIR_STD_DAT T1 ");
        stb.append("   WHERE   YEAR = '" + param._year + "' AND ");

                               //指定講座の生徒を対象とする
        stb.append("           EXISTS(SELECT  SCHREGNO ");
        stb.append("                  FROM    CHAIR_STD_DAT T2 ");
        stb.append("                  WHERE   YEAR = T1.YEAR AND ");
        stb.append("                          t2.semester = t1.semester and ");
        stb.append("                          CHAIRCD = '" + param._attendClassCd + "' AND ");
        stb.append("                          (( APPDATE    BETWEEN '" + param._dateFrom + "' AND '" + param._dateTo + "' )OR ");
        stb.append("                           ( APPENDDATE BETWEEN '" + param._dateFrom + "' AND '" + param._dateTo + "' )OR ");
        stb.append("                           ( '" + param._dateFrom + "' BETWEEN APPDATE AND APPENDDATE )OR ");
        stb.append("                           ( '" + param._dateTo + "' BETWEEN APPDATE AND APPENDDATE )) AND ");
        stb.append("                          T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("                  GROUP BY SCHREGNO) AND ");

                               //指定講座の科目を対象とする
        stb.append("           EXISTS(SELECT  'X' ");
        stb.append("                  FROM    CHAIR_DAT T2 ");
        stb.append("                  WHERE   T2.YEAR = T1.YEAR AND ");
        stb.append("                          T2.CHAIRCD = T1.CHAIRCD AND ");
        stb.append("                          EXISTS(SELECT  'X' ");
        stb.append("                                 FROM    CHAIR_DAT T3 ");
        stb.append("                                 WHERE   T3.YEAR = T2.YEAR AND ");
        stb.append("                                         t3.semester = t2.semester and ");
        stb.append("                                         T3.CHAIRCD = '" + param._attendClassCd + "' ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("       AND T3.CLASSCD = T2.CLASSCD ");
            stb.append("       AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("       AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ");
        }
        stb.append("                                         AND T3.SUBCLASSCD = T2.SUBCLASSCD )) ");
        stb.append("   GROUP BY CHAIRCD, SCHREGNO) ");

        //メイン表
        stb.append("SELECT  (SELECT COUNT(DISTINCT SCHREGNO) FROM CHAIR_A) AS COUNT, ");
        stb.append("        (SELECT COUNT(DISTINCT CHAIRCD)  FROM CHAIR_A) AS CHAIR_COUNT, ");
        stb.append("        T3.CREDITS, ");
        stb.append("        T1.SCHREGNO, ");
        stb.append("        T1.CHAIRCD, ");
        if (param.isHoutei()) {
            stb.append("     VALUE(T3.ABSENCE_HIGH, 0) ");
            if ("1".equals(param._tyuiTyoka)) {
                stb.append("      - VALUE(T3.ABSENCE_WARN_RISHU_SEM" + param._warnSemester + ", 0) ");
            }
            stb.append("       AS ABSENCE_HIGH, ");
            stb.append("     VALUE(T3.GET_ABSENCE_HIGH, 0) ");
            if ("1".equals(param._tyuiTyoka)) {
                stb.append("      - VALUE(T3.ABSENCE_WARN_SHUTOKU_SEM" + param._warnSemester + ", 0) ");
            }
            stb.append("       AS GET_ABSENCE_HIGH ");
        } else {
            stb.append(" VALUE(T4.COMP_ABSENCE_HIGH, 0) AS ABSENCE_HIGH, ");
            stb.append(" VALUE(T4.GET_ABSENCE_HIGH, 0) AS GET_ABSENCE_HIGH ");
        }
        if (param._hasCreditMstAbsenceWarn) {
            stb.append("    ,ABSENCE_WARN AS ABSENCE_WARN ");
        }
        stb.append("FROM CHAIR_A T1 ");
        stb.append("        INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = '" + param._year + "' AND ");
        stb.append("                                         T2.SEMESTER = '" + param._semester + "' AND ");
        stb.append("                                         T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("        LEFT JOIN V_CREDIT_MST T3 ON T3.YEAR = '" + param._year + "' AND ");
        stb.append("                                   T3.GRADE = T2.GRADE AND ");
        stb.append("                                   T3.COURSECD = T2.COURSECD AND ");
        stb.append("                                   T3.MAJORCD = T2.MAJORCD AND ");
        stb.append("                                   T3.COURSECODE = T2.COURSECODE AND ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("       T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
        }
        stb.append("                                        T3.SUBCLASSCD = '" + param._subclassCd + "' ");
        if (!param.isHoutei()) {
            stb.append("        LEFT JOIN SCHREG_ABSENCE_HIGH_DAT T4 ON T4.YEAR = '" + param._year + "' ");
            stb.append("                                    AND T4.DIV = '" + param._absenceDiv + "' ");
            stb.append("                                    AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("   T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || ");
            }
            stb.append("                                        T4.SUBCLASSCD = '" + param._subclassCd + "' ");
            stb.append("                                    AND T4.SCHREGNO = T1.SCHREGNO ");
        }
        return stb.toString();
    }


    /*
    *
    *  学校教育システム 賢者 [出欠管理]  出席簿（科目別）
    */
    private static class CALC {

       private int subclasscredit;             //科目単位数

       private final Map<String, BigDecimal> _schregAbsencehigh;          //履修欠時数上限値
       private final Map<String, BigDecimal> _schregAbsenceGetHigh;       //修得欠時数上限値

       private static final String AMIKAKE_ATTR1 = "Paint=(1,60,1),Bold=1";
       private static final String AMIKAKE_ATTR2 = "Paint=(1,80,1),Bold=1";

       boolean hasdata;

       int LINE = 50;
       int COLUMN = 64;
       final Param _param;

       /**
        *  コンストラクター
        */
       public CALC(final Param param) {
           _param = param;
           _schregAbsencehigh = new HashMap();
           _schregAbsenceGetHigh = new HashMap();
       }

       /**
        *  svf print 印刷処理
        */
       public void printSvf(final DB2UDB db2, final Vrw32alp svf) {
           //  ＳＶＦフォームの設定->学期制による
           final String form;
           if (2 < _param._semesdiv) {
               form = "KNJC053_3.frm";   //３学期制用 50行
           } else {
               form = "KNJC053_2.frm";   //２学期制用 50行
           }
           svf.VrSetForm(form, 1);

           //  全学期
           final Iterator<Semester> it = _param._semesterList.iterator();
           for (int i = 1; i <= _param._semesdiv && it.hasNext(); i++) {
               final Semester semester = it.next();
               svf.VrsOut("SEMESTER" + i, semester._semestername);
           }
           svf.VrsOut("SEMESTER" + (_param._semesdiv + 1), "1 " + FROM_TO_MARK + " " + _param._semesdiv + "学期累計");
           svf.VrsOut("RANGE", "年度初め " + FROM_TO_MARK + " " + _param._dateToFormat);

           final String comment = "1".equals(_param._tyuiTyoka) ? "注意" : "超過";
           svf.VrAttribute("NOTE5", AMIKAKE_ATTR1);
           svf.VrsOut("NOTE5", "　");
           svf.VrsOut("NOTE6", "　：未履修" + comment);
           svf.VrAttribute("NOTE7", AMIKAKE_ATTR2);
           svf.VrsOut("NOTE7", "　");
           svf.VrsOut("NOTE8", "　：未修得" + comment);

           boolean note3Output = false;
           if ("1".equals(_param._knjSchoolMst._subOffDays)) {
               svf.VrsOut("NOTE3", "※備考欄の休学の（）内の数字は、休学欠課数を表す。");
               note3Output = true;
           }
           final String footerText = getFooterText(_param._knjSchoolMst);
           if (null != footerText) {
               svf.VrsOut(!note3Output ? "NOTE3" : "NOTE4", footerText);
           }

           //  総ページ数
           printTotalPage(db2, svf);

           final List<Range> rangeList = getDateRangeList();
           if (_param._isOutputDebug) {
               log.info(" rangeList = " + rangeList);
           }

           // １ページに入る範囲の生徒単位で処理を行う
           for (final Map.Entry<Integer, Map<String, Student>> e : getStudentPageMap(db2).entrySet()) {
               final Map<String, Student> hm1 = e.getValue();

               setAttendance(db2, hm1, rangeList);

               printStudent(svf, hm1);      //出欠集計の取得と出力のメソッド
               svf.VrEndPage();
               hasdata = true;
           }
       }

       private Map<Integer, Map<String, Student>> getStudentPageMap(final DB2UDB db2) {
           final Map<Integer, Map<String, Student>> pMap = new TreeMap();
           //  生徒名等ResultSet作成
           final String sql = setStatementSchInfo(_param);
           //生徒情報

           //  生徒名等SVF出力
           Integer page = new Integer(1);
           int line = 0;
           for (final Map row : KnjDbUtils.query(db2, sql)) {
               line += 1;
               final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
               final Student student = new Student(schregno, line, KnjDbUtils.getString(row, "GRADE"), KnjDbUtils.getString(row, "HR_CLASS"), KnjDbUtils.getString(row, "ATTENDNO1"));
               if (NumberUtils.isDigits(KnjDbUtils.getString(row, "GRADE"))) {
                   student._gradeInt = KnjDbUtils.getInt(row, "GRADE", new Integer(0)).intValue();
               }
               student._rsMap = row;
               if (null == pMap.get(page)) {
                   pMap.put(page, new HashMap());
               }
               pMap.get(page).put(schregno, student);     //行番号に学籍番号を付ける
               if (line == LINE) {
                   page = new Integer(page.intValue() + 1);
                   line = 0;
               }
           }
           return pMap;
       }

       /**
        *   備考編集
        */
       private String retSchTransferInfo(final Param param, final Student student) {
           String str = null;
           try {
               // 異動情報の優先順位は 「留学・休学 > 退学・転学 > 転入・編入」 とする
               final String trainsfercd = student._rsMap.get("TRANSFERCD");
               final String kbnDate2 = student._rsMap.get("KBN_DATE2");
               final String kbnDate2e = student._rsMap.get("KBN_DATE2E");
               final String kbnName2 = StringUtils.defaultString(student._rsMap.get("KBN_NAME2"));

               final String kbnName1 = StringUtils.defaultString(student._rsMap.get("KBN_NAME1"));
               final String kbnDate1 = student._rsMap.get("KBN_DATE1");

               final String kbnDate2sSemester = student._rsMap.get("KBN_DATE2_SSEMESTER");
               final String kbnDate2eSemester = student._rsMap.get("KBN_DATE2_ESEMESTER");

               if (kbnDate2 != null) {
                   final Calendar cala = toCal(param._dateTo);  //印刷範囲TO
                   final Calendar calb = toCal(kbnDate2);
                   final Calendar calc = toCal(kbnDate2e);
                   if (!calb.after(cala)) {
                       str = param.sdf2.format(calb.getTime()) + FROM_TO_MARK + param.sdf2.format(calc.getTime()) + kbnName2;
                       if ("1".equals(param._knjSchoolMst._subOffDays) && "2".equals(trainsfercd)) {
                           final String offdaysCount = getSchregOffdays(param, student, kbnDate2sSemester, kbnDate2eSemester);
                           str += "（" + offdaysCount + "）"; // 休学が欠課に含まれるとき備考に休学時数を表示する
                       }
                   }
               } else if (kbnDate1 != null) {
                   final Calendar cala = toCal(param._dateTo);  //印刷範囲TO
                   final Calendar calb = toCal(kbnDate1);
                   if (!calb.after(cala)) {
                       str = param.sdf3.format(calb.getTime()) + kbnName1;
                   }
               }
           } catch (Exception ex) {
               log.error("setSchTransferInfo error!", ex);
           }
           return str;
       }

       /**
        *  svf print 出欠集計取得
        */
       private String getSchregOffdays(final Param param, final Student student, final String kbnDate2sSemester, final String kbnDate2eSemester) {

           String offdays = "";
           for (final Semester semester : param._semesterList) {
                                                //学期コード
               if (String.valueOf(SEMES_ALL).equals(semester._semester)) {
                   break;
               }
               if (toCal(param._dateTo).before(toCal(semester._sdate))) {
                   break;   //印刷終了日が学期開始日の前なら以降の学期は出力しない！
               }
               if (null != kbnDate2sSemester && Integer.parseInt(kbnDate2sSemester) > Integer.parseInt(semester._semester)) {
                   continue;
               }
               if (null != kbnDate2eSemester && Integer.parseInt(kbnDate2eSemester) < Integer.parseInt(semester._semester)) {
                   continue;
               }
               final Attendance ate = student._attendance.get(semester._semester);
               if (null != ate) {
                   if (ate._offdays > 0) {
                       offdays = String.valueOf((NumberUtils.isDigits(offdays) ? Integer.parseInt(offdays) : 0) + (int) ate._offdays);
                   }
               }
           }
           return offdays;
       }

       private List<Range> getDateRangeList() {
           final List<Range> rtn = new ArrayList();
           for (final Semester semester : _param._semesterList) {
               if (toCal(_param._dateTo).before(toCal(semester._sdate))) {
                   continue;   //印刷終了日が学期開始日の前なら以降の学期は出力しない！
               }
               final String edate;      //学期終了日
               if (toCal(_param._dateTo).before(toCal(semester._edate))) {         //印刷終了日が学期終了日の前なら集計期間は印刷終了日までとする！
                   edate = _param._dateTo;
               } else {
                   edate = semester._edate;
               }
               final Range range = new Range(semester._semester, semester._sdate, edate, semester._sSemester);
               rtn.add(range);
           }
           return rtn;
       }

       /**
        *  svf print 出欠集計取得と印刷
        */
       private void printStudent(final Vrw32alp svf, final Map<String, Student> hm1) {
           //  ＳＶＦフィールドを初期化
           final int lastLine = LINE + 1;
           for (int seme = 1; seme <= 3; seme++) {
               for (int line = 1 ; line < lastLine; line++) {
                   svf.VrsOutn("TOTAL_ABSENCE" + seme    ,line ,"");   //欠席
                   svf.VrsOutn("TOTAL_LATE" + seme       ,line ,"");   //遅刻
                   svf.VrsOutn("TOTAL_LEAVE" + seme      ,line ,"");   //早退
                   svf.VrsOutn("TOTAL_TIME" + seme       ,line ,"");   //時数
               }
           }

           svf.VrsOut("nendo",      _param._nendo);
           svf.VrsOut("scope_day",  _param._dateFromFormat + " " + FROM_TO_MARK + " " + _param._dateToFormat); //印刷範囲
           svf.VrsOut("TODAY",      _param._ctrlDateString);
           svf.VrsOut("STAFFNAME",  _param._staffName);
           svf.VrsOut("class",      _param._chairName);
           svf.VrsOut("CLASSNAME",  _param._subclassName);

           //一覧表枠外の文言
           svf.VrAttribute("NOTE1",  AMIKAKE_ATTR2);
           svf.VrsOut("NOTE1",  " ");
           svf.VrsOut("NOTE2",  "：欠課時数オーバの者");

           if (_param.isPrintSemesKekka(1)) {
               svf.VrsOut("ABSENTNAME", "欠課時数");
           }
           if (_param.isPrintSemesKekka(SEMES_ALL)) {
               svf.VrsOut("ABSENTNAME9", "欠課時数");
           }

           for (int schno = 1; schno <= LINE; schno++) {
               final Student student = getStudent(hm1, schno);
               if (null != student) {
                   //生徒名等出力のメソッド
                   if (null != student._rsMap) {
                       if ("1".equals(_param._use_SchregNo_hyoji)) {
                           svf.VrsOutn("SCHREGNO", schno, student._schregno);     //学籍番号
                           svf.VrsOutn("HR_NAME2",   schno, student._rsMap.get("ATTENDNO")); //学年組出席番号
                           svf.VrsOutn("NAME_SHOW2", schno, student._rsMap.get("NAME"));     //生徒名
                       } else  {
                           svf.VrsOutn("HR_NAME",   schno, student._rsMap.get("ATTENDNO")); //学年組出席番号
                           svf.VrsOutn("NAME_SHOW", schno, student._rsMap.get("NAME"));     //生徒名
                       }

                       //備考の編集＆出力
                       final String str = retSchTransferInfo(_param, student);
                       if (str != null) {
                           svf.VrsOutn("REMARK", schno, str);   //備考
                       }
                   }
               }
           }

           final Map<String, Attendance> semesStudentTotalAttendance = new HashMap();

           for (final String schregno : hm1.keySet()) {
               final Student student = hm1.get(schregno);
               if (null == student) {
                   continue;
               }

               student._attendance9 = new Attendance(String.valueOf(SEMES_ALL), student._schregno);
               for (final Map.Entry<String, Attendance> e : student._attendance.entrySet()) {
                   final String semester = e.getKey();
                   final Attendance ate = e.getValue();
                   if (String.valueOf(SEMES_ALL).equals(semester)) {
                       continue;
                   }

                   //学期集計の出力
                   svfVrsOutnSum(svf, "TOTAL_ABSENCE" + semester, student._line, getSumstr(_param, ate._kesseki));    //欠席
                   svfVrsOutnSum(svf, "TOTAL_LATE"    + semester, student._line, getBdSumstr(_param, ate._srcLate));    //遅刻
                   svfVrsOutnSum(svf, "TOTAL_LEAVE"   + semester, student._line, getBdSumstr(_param, ate._srcEarly));  //早退

                   if (_param.isPrintSemesKekka(Integer.parseInt(semester))) {
                       svfVrsOutnSum(svf, "TOTAL_TIME" + semester, student._line, getSumstr(_param, ate._srcKekka));
                   }
                   svfVrsOutnSum(svf, "TOTAL_JUJITIME" + semester, student._line, ate._lesson == 0.0 ? "" : String.valueOf((int) ate._lesson));

                   //生徒別累計処理
                   student._attendance9.add(_param, ate);
                   if (!semesStudentTotalAttendance.containsKey(semester)) {
                       semesStudentTotalAttendance.put(semester, new Attendance(student._schregno, semester));
                   }
                   final Attendance semesTotal = semesStudentTotalAttendance.get(semester);

                   //学期別累計処理
                   semesTotal.add(_param, ate);
               }
           }

           final int semeline = _param._semesdiv + 1;
           for (int i = 0; i < LINE && i < hm1.size(); i++) {
               final Student student = getStudent(hm1, i + 1);
               if (null == student || (null == student._attendance.get(String.valueOf(SEMES_ALL)) && null == student._attendance9)) {
                   continue;
               }
               final Attendance c = null == student._attendance9 ? new Attendance(student._schregno, String.valueOf(SEMES_ALL)) : student._attendance9.calc(student._schregno, _param);
               if (_param._isOutputDebug) {
                   log.info(" schregno " + student._schregno + " ( " + student._grade + ", " + student._hrClass + ", " + student._attendno + "), kekka9 = " + c._srcKekka);
               }

               svfVrsOutnSum(svf, "TOTAL_ABSENCE" + semeline, student._line, getSumstr(_param, c._kesseki));  //欠席

               svfVrsOutnSum(svf, "TOTAL_LATE"   + semeline, student._line, getBdSumstr(_param, c._srcLate)); //遅刻
               svfVrsOutnSum(svf, "TOTAL_LEAVE"  + semeline, student._line, getBdSumstr(_param, c._srcEarly)); //早退
               //時数
               svfVrsOutnSum(svf, "TOTAL_JUJITIME" + semeline, student._line, c._lesson == 0.0 ? "" : String.valueOf((int) c._lesson));

               if (!semesStudentTotalAttendance.containsKey(String.valueOf(SEMES_ALL))) {
                   semesStudentTotalAttendance.put(String.valueOf(SEMES_ALL), new Attendance("HR", String.valueOf(SEMES_ALL)));
               }
               final Attendance semes9Total = semesStudentTotalAttendance.get(String.valueOf(SEMES_ALL));

               //absent_cov==2 通年で換算
               //definecode.useabsencehigh == true : 単位マスターの欠課時数上限値を使用
               if (_param.isPrintSemesKekka(SEMES_ALL)) {
                   if (_param._definecode.useabsencehigh) { // 近大以外
                       //単位マスターの欠課時数上限値を使用する場合
                       boolean amikake = false;

                       Attendance accumu9 = student._attendance.get(String.valueOf(SEMES_ALL));
                       if (null == accumu9) {
                           accumu9 = new Attendance(student._schregno, String.valueOf(SEMES_ALL));
                       }

                       final String attr = getKekkaover(_param, student._schregno, accumu9._srcKekka);
                       if (null != attr) {
                           svf.VrAttributen("TOTAL_TIME" + semeline, student._line, attr);
                           amikake = true;
                       }

                       svfVrsOutnSum(svf, "TOTAL_TIME" + semeline, student._line, getSumstr(_param, accumu9._srcKekka));
                       if (amikake) {
                           svf.VrAttributen("TOTAL_TIME" + semeline, student._line, "Paint=(0,0,0),Bold=0");
                           amikake = false;
                       }
                       semes9Total.add(_param, accumu9);
                   } else {
                       //欠課時数上限値を定数とする場合
                       if (student != null && 0 != student._gradeInt) {
                           svfVrsOutnSum(svf, "TOTAL_TIME" + semeline, student._line, getKekkaoverTeisu(_param, c._srcKekka, student._gradeInt));  //欠課
                       }
                       semes9Total.add(_param, c);
                   }
               }
           }

           // 最下段累計出力処理
           for (final String semester : semesStudentTotalAttendance.keySet()) {
               final Attendance att = semesStudentTotalAttendance.get(semester);
               final int j = SEMES_ALL == Integer.parseInt(semester) ? _param._semesdiv + 1 : Integer.parseInt(semester);
               svfVrsOutnSum(svf, "TOTAL_ABSENCE"  + j, lastLine, getSumstr(_param, att._kesseki));        //欠席
               svfVrsOutnSum(svf, "TOTAL_LATE"     + j, lastLine, getBdSumstr(_param, att._srcLate));        //遅刻
               svfVrsOutnSum(svf, "TOTAL_LEAVE"    + j, lastLine, getBdSumstr(_param, att._srcEarly));        //早退
               svfVrsOutnSum(svf, "TOTAL_JUJITIME" + j, lastLine, att._lesson == 0.0 ? "" : String.valueOf((int) att._lesson));        //時数
               if (_param.isPrintSemesKekka(Integer.parseInt(semester))) {
                   svfVrsOutnSum(svf, "TOTAL_TIME" + j, lastLine, getSumstr(_param, att._srcKekka));        //欠課
               }
           }
       }

       private void setAttendance(final DB2UDB db2, final Map<String, Student> hm1, final List<Range> ranges) {

           final Set<String> gradeSet = new HashSet<String>();
           final Set<String> hrClassSet = new HashSet<String>();
           for (final String schregno : hm1.keySet()) {
               final Student student = hm1.get(schregno);
               if (null == student) {
                   continue;
               }
               if (null != student._grade) {
                   gradeSet.add(student._grade);
               }
               if (null != student._hrClass) {
                   hrClassSet.add(student._hrClass);
               }
           }
           log.info(" grade = " + gradeSet + ", hrClass = " + hrClassSet);

           for (final Range range : ranges) {

               log.debug(" set attendance semester =  " + range._semester);

               _param._attendParamMap.put("subclasscd", _param._subclassCd);
               _param._attendParamMap.put("sSemester", range._sSemester);
               if (gradeSet.size() == 1) {
                   _param._attendParamMap.put("grade", gradeSet.iterator().next());
               } else {
                   _param._attendParamMap.remove("grade");
               }
               if (hrClassSet.size() == 1) {
                   _param._attendParamMap.put("hrClass", hrClassSet.iterator().next());
               } else {
                   _param._attendParamMap.remove("hrClass");
               }
               boolean exeStudent = false;
               if (gradeSet.size() > 1 || hrClassSet.size() > 1) {
                   _param._attendParamMap.put("schregno", "?");
                   exeStudent = true;
               }
               final String sql = AttendAccumulate.getAttendSubclassSql(_param._year,
                       range._semester,
                       range._sdate,
                       _param._dateTo,
                       _param._attendParamMap);

               if (_param._isOutputDebug) {
                   for (final Iterator pit = _param._attendParamMap.entrySet().iterator(); pit.hasNext();) {
                       final Map.Entry e = (Map.Entry) pit.next();
                       log.info(" attend param " + e.getKey() + " = " + e.getValue());
                   }
               }

               try {
                   if (exeStudent) {
                       PreparedStatement ps = null;
                       try {
                           ps = db2.prepareStatement(sql);  //出欠集計
                           for (final String schregno : hm1.keySet()) {
                               final Student student = hm1.get(schregno);
                               if (null == student) {
                                   continue;
                               }

                               if (_param._isOutputDebug) {
                                   log.info(" calc " + range._semester + " schregno = " + schregno);
                               }

                               for (final Map row : KnjDbUtils.query(db2, ps, new Object[] {schregno})) {
                                   setStudentAttendance(row, hm1, _param, range);
                               }
                           }
                       } catch (Exception e) {
                           log.error("exception!", e);
                       } finally {
                           DbUtils.closeQuietly(ps);
                           db2.commit();
                       }
                   } else {

                       if (_param._isOutputDebug) {
                           log.info(" calc " + range._semester);
                       }

                       for (final Map row : KnjDbUtils.query(db2, sql)) {
                           setStudentAttendance(row, hm1, _param, range);
                       }
                   }

               } catch (Exception ex) {
                   log.warn("ResultSet read error!", ex);
               }
           }
       }

       private void setStudentAttendance(final Map row, final Map<String, Student> hm1, final Param param, final Range range) {
           if (!String.valueOf(SEMES_ALL).equals(KnjDbUtils.getString(row, "SEMESTER"))) {
               return;
           }
           final Student student = hm1.get(KnjDbUtils.getString(row, "SCHREGNO"));
           if (null == student) {
               return;
           }


           final int sick1 = Integer.parseInt(KnjDbUtils.getString(row, "SICK1"));
           final BigDecimal late = param.isPrintRawLateEarly() ? KnjDbUtils.getBigDecimal(row, "LATE", null) : KnjDbUtils.getBigDecimal(row, "LATE2", null);
           final BigDecimal early = param.isPrintRawLateEarly() ? KnjDbUtils.getBigDecimal(row, "EARLY", null) : KnjDbUtils.getBigDecimal(row, "EARLY2", null);
           final String sick2Str = KnjDbUtils.getString(row, "SICK2");
           final int jisu = Integer.parseInt(KnjDbUtils.getString(row, "MLESSON"));
           final int offdays = Integer.parseInt(KnjDbUtils.getString(row, "OFFDAYS"));
           final Attendance ate = new Attendance(student._schregno, range._semester);
           ate._kesseki = sick1;
           ate._srcLate = late;
           ate._srcEarly = early;
           if (param.isAttendPrintInt()) {
               ate._srcKekka = Integer.parseInt(sick2Str);    //欠課
           } else {
               ate._srcKekka = Double.parseDouble(sick2Str);    //欠課
           }
           ate._lesson = jisu;
           ate._offdays = offdays;
           student._attendance.put(range._semester, ate);
       }

       private int svfVrsOutnSum(final Vrw32alp svf, final String field1, final int n, final String sum) {
           if (null == sum || sum.length() == 0) {
               return 0;
           }
           final String field = 4 < sum.length() ? field1 + "_2" : field1;
           return svf.VrsOutn(field, n, sum);
       }

       /**
        * 指定行の生徒を得る
        * @param schregnoLineMap 生徒と行のマップ
        * @param line 行
        * @return 指定行の生徒
        */
       private Student getStudent(final Map<String, Student> schregnoLineMap, final int line) {
           for (final String schregno : schregnoLineMap.keySet()) {
               final Student student = schregnoLineMap.get(schregno);
               if (student._line == line) {
                   return student;
               }
           }
           return null;
       }

       /**
        *  欠課時数超過チェック
        *    subclasscredit : 科目の単位数
        *    absencehigh    : 欠課時数上限値
        *    absencewarn    : 欠時数チェック週数 05/07/29Build J^o^L => ^(~oo~)^ => J+o+L
        *    引数について kekka : 生徒別欠課時数累計
        *
        *    2005/04/28 Modify 単位または欠課時数上限値が０の場合’＊’を付けない
        *    2005/07/29 Modify 欠課時数超過の計算を修正 => '*'ではなく網掛けに変更
        *                      欠課時数注意者を'*'で表記追加
        */
       private String getKekkaover(final Param param, final String schregno, final double kekka) {

           final BigDecimal absenceHighBd = _schregAbsencehigh.get(schregno);
           final BigDecimal absenceGetHighBd = _schregAbsenceGetHigh.get(schregno);
           final double absenceHigh = (absenceHighBd != null) ? absenceHighBd.doubleValue() : 0;
           final double absenceGetHigh = (absenceGetHighBd != null) ? absenceGetHighBd.doubleValue() : 0;

           try {
               if (kekka != 0) {
                   if (0 == absenceHigh || 0 < absenceHigh  &&  absenceHigh < kekka) {
                       return AMIKAKE_ATTR1;
                   } else if (0 < absenceGetHigh  &&  absenceGetHigh < kekka) {
                       return AMIKAKE_ATTR2;
                   } else if (0 < absenceHigh  &&  absenceHigh < kekka) {
                       return "Paint=(2,70,1),Bold=1";
                   }
               }
           } catch (Exception ex) {
               log.error("error! ", ex);
           }
           //log.debug("kekka="+ kekka + "  str="+str);
           return null;
       }


       /**
        *  欠課時数超過チェック => 欠課時数上限値を使用しない場合
        *    subclasscredit : 科目の単位数
        *    absencehigh    : 欠課時数上限値
        *    引数について kekka : 生徒別欠課時数累計
        *                 g     : 生徒別学年
        */
       private String getKekkaoverTeisu(final Param param, final double kekka, final int g) {
           String str = null;
           try {
               if (kekka == 0) {
                   str = "";
               } else {
                   if (0 < subclasscredit  &&
                       subclasscredit * param._definecode.absencehighgrade[g-1][0] * Integer.parseInt(param._semester) / param._definecode.absencehighgrade[g-1][1] < kekka ) {
                       str = "*" + getSumstr(param, kekka);
                   } else {
                       str = getSumstr(param, kekka);
                   }
               }
           } catch (Exception ex) {
               log.error("error! ", ex);
           }
           return str;
       }


       /**
        *  遅刻および早退を欠課へ換算する処理
        *  absent_cov_late     : 換算する遅刻および早退の数単位
        */
       private static double countonyear(final Param param, final double srcLate, final double srcEarly, final double srcKekka) {
           double kekka = 0;
//           try {
//               if (param.isAttendPrintInt()) {
//                   kekka = (int) (srcLate + srcEarly) / param._definecode.absent_cov_late;
//               } else {
//                   kekka = (srcLate + srcEarly) / param._definecode.absent_cov_late;
//               }
//
//               if (param._definecode.absent_cov == KANSAN5_SEISU_NENKAN_AMARIKURIAGE) {
//                   kekka += getKuriage(param, (int) (srcLate + srcEarly));
//               }
//           } catch (Exception ex) {
//               log.error("error! ", ex);
//           }
           kekka += srcKekka;
           return kekka;
       }


       /**
        *  ペナルティー欠課換算後の遅刻算出（換算数を差し引く）
        */
       private static double lateonyear(final Param param, final double srcLate, final double srcEarly) {
           double late = 0;
           final int j = param._definecode.absent_cov_late;
           try {
               final int lateearly = (int) (srcLate + srcEarly);
               if (param._definecode.absent_cov == KANSAN5_SEISU_NENKAN_AMARIKURIAGE) {
                   final int amari = 0 != getKuriage(param, lateearly) ? Integer.parseInt(param._knjSchoolMst._amariKuriage) : 0;
                   if( srcLate - (lateearly / j * j + amari) < 0) {
                       late = 0;
                   } else {
                       late = srcLate - (lateearly / j * j + amari);
                   }
               } else if (param._definecode.absent_cov == KANSAN4_SYOSU_NENKAN) {
                   late = 0; // すべて欠課へ換算
               } else if( srcLate - lateearly / j * j < 0) {
                   late = 0;
               } else {
                   late = srcLate - lateearly / j * j;
               }
           } catch (Exception ex) {
               log.error("error! ", ex);
           }
           return late;
       }


       /**
        *  ペナルティー欠課換算後の早退算出（換算数を差し引く）
        */
       private static double earlyonyear(final Param param, final double srcLate, final double srcEarly) {
           double early = 0;
           final int j = param._definecode.absent_cov_late;
           try {
               final int lateearly = (int) (srcLate + srcEarly);
               if (param._definecode.absent_cov == KANSAN5_SEISU_NENKAN_AMARIKURIAGE) {
                   final int amari = 0 != getKuriage(param, lateearly) ? Integer.parseInt(param._knjSchoolMst._amariKuriage) : 0;
                   if( srcLate - (lateearly / j * j  + amari) < 0) {
                       early = srcEarly + ( srcLate - (lateearly / j * j  + amari));
                   } else {
                       early = srcEarly;
                   }
               } else if (param._definecode.absent_cov == KANSAN4_SYOSU_NENKAN) {
                   early = 0; // すべて欠課へ換算
               } else if( srcLate - lateearly / j * j < 0) {
                   early = srcEarly + ( srcLate - lateearly / j * j );
               } else {
                   early = srcEarly;
               }
           } catch (Exception ex) {
               log.error("error! ", ex);
           }
           return early;
       }


       /**
        *  ペナルティー欠課換算後の遅刻算出（換算数を差し引く）
        *  2005/06/18
        */
       private static int getKuriage(final Param param, final int lateEarly) {
           try {
               final int _absentCovLate = Integer.parseInt(param._knjSchoolMst._absentCovLate);
               final int _amariKuriage = Integer.parseInt(param._knjSchoolMst._amariKuriage);
               return lateEarly % _absentCovLate >= _amariKuriage ? 1 : 0;
           } catch (Exception e) {
               log.error("Exception:", e);
           }
           return 0;
       }

       /**
        *  出欠累計の編集 ゼロは否出力
        */
       private String getSumstr(final Param param, final double sum) {
           if (sum <= 0.0) {
               return "";
           }

           final String rtn;
           if (param.isAttendPrintInt()) {
               rtn = String.valueOf((int) sum);
           } else {
               rtn = new BigDecimal(sum).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
           }
           return rtn;

       }//String getSumstr()の括り

       /**
        *  出欠累計の編集 ゼロは否出力
        */
       private String getBdSumstr(final Param param, final BigDecimal bdsum) {
           if (null == bdsum || bdsum.doubleValue() <= 0.0) {
               return "";
           }

           final String rtn;
           if (param.isAttendPrintInt()) {
               rtn = bdsum.setScale(0).toString();
           } else {
               rtn = bdsum.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
           }
           return rtn;

       }//String getSumstr()の括り

       private static String getFooterText(final KNJSchoolMst knjSchoolMst) {
           StringBuffer text = new StringBuffer("※欠課時数に");
           String comma = "";
           if ("1".equals(knjSchoolMst._subOffDays)) {
               text.append(comma + "休学");
               comma = "、";
           }
           if ("1".equals(knjSchoolMst._subAbsent)) {
               text.append(comma + "公欠");
               comma = "、";
           }
           if ("1".equals(knjSchoolMst._subSuspend)) {
               text.append(comma + "出停");
               comma = "、";
           }
           if ("1".equals(knjSchoolMst._subMourning)) {
               text.append(comma + "忌引");
               comma = "、";
           }
           if ("1".equals(knjSchoolMst._subVirus)) {
               text.append(comma + "出停（伝染病）");
               comma = "、";
           }
           text.append("を含む。");

           if ("".equals(comma)) {
               return null;
           }
           return text.toString();
       }

       private void printTotalPage(final DB2UDB db2, final Vrw32alp svf) {

           int c_sch = 0;                      //生徒数

           Integer creditMax = null;
           //生徒数取得
           final String sqlCredits = prestateSchnumAndCredits(_param);
           if (_param._isOutputDebug) {
               log.info(" credits sql = " + sqlCredits);
           }
           //生徒情報
           for (final Map row : KnjDbUtils.query(db2, sqlCredits)) {
               c_sch = KnjDbUtils.getInt(row, "COUNT", new Integer(0)).intValue();           //生徒数
               if (KnjDbUtils.getString(row, "CREDITS") != null) {
                   if (null == creditMax || creditMax.intValue() < KnjDbUtils.getInt(row, "CREDITS", new Integer(0)).intValue()) {
                       creditMax = Integer.valueOf(KnjDbUtils.getString(row, "CREDITS"));
                   }
                   if (KnjDbUtils.getString(row, "ABSENCE_HIGH") != null) {
                       final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                       _schregAbsencehigh.put(schregno, KnjDbUtils.getBigDecimal(row, "ABSENCE_HIGH", null));
                       _schregAbsenceGetHigh.put(schregno, KnjDbUtils.getBigDecimal(row, "GET_ABSENCE_HIGH", null));
                   }
               }
           }

           if (null != creditMax) {
               subclasscredit = creditMax.intValue();
           }
           if (0 < subclasscredit) {
               svf.VrsOut("CREDIT", String.valueOf(subclasscredit));   //単位
           }

           final int[] c_chr = new int[_param._semesdiv + 1]; //学期別授業時数のインスタンス変数

           //時数取得
           for (int ia = 0; ia < c_chr.length; ia++) {
               c_chr[ia] = 0;
           }

           //  科目日付、校時
           final StringBuffer stb = new StringBuffer();
           stb.append("SELECT  SEMESTER, COUNT(*) AS COUNT ");
           stb.append("    FROM    SCH_CHR_DAT W1 ");
           stb.append("WHERE   YEAR = '" + _param._year + "' ");
           stb.append("    AND SEMESTER <= '" + _param._semester + "' ");
           stb.append("    AND CHAIRCD = '" + _param._attendClassCd + "' ");
           stb.append("    AND EXECUTEDATE >= '" + _param._dateFrom + "' AND EXECUTEDATE <= '" + _param._dateTo + "' ");
           stb.append("GROUP BY SEMESTER");

           final String sql = stb.toString();
           //科目情報
           for (final Map row : KnjDbUtils.query(db2, sql)) {
               c_chr[KnjDbUtils.getInt(row, "SEMESTER", new Integer(9)).intValue() - 1] = KnjDbUtils.getInt(row, "COUNT", new Integer(0)).intValue();
           }
           for (int ia = 0; ia < c_chr.length - 1; ia++) {
               c_chr[_param._semesdiv] += c_chr[ia];
           }
           //総ページ数
           final int page = (c_chr[_param._semesdiv] / COLUMN + (c_chr[_param._semesdiv] % COLUMN > 0 ? 1 : 0)) * (c_sch / LINE + (c_sch % LINE > 0 ? 1 : 0));
           svf.VrlOut("TOTAL_PAGE", page);
       }

       private static class Student {
           final String _schregno;
           final int _line;
           final Map<String, Attendance> _attendance = new TreeMap();
           Attendance _attendance9;
           final String _grade;
           final String _hrClass;
           final String _attendno;
           int _gradeInt;
           Map<String, String> _rsMap;
           Student(final String schregno, final int line, final String grade, final String hrClass, final String attendno) {
               _schregno = schregno;
               _line = line;
               _grade = grade;
               _hrClass = hrClass;
               _attendno = attendno;
           }
       }

       private static class Attendance {
           // [0:欠席 1:遅刻 2:早退 3:欠課時数 4:時数]
           final String _schregno;
           final String _semester;
           double _kesseki;
           BigDecimal _srcLate;
           BigDecimal _srcEarly;
           double _srcKekka;
           double _lesson;
           double _offdays;
           Attendance(final String schregno, final String semester) {
               _schregno = schregno;
               _semester = semester;
           }
           public void add(final Param param, final Attendance attendance) {
//        	   if (param._isOutputDebug) {
//        		   if (attendance._srcKekka > 0.0) {
//        			   log.info(" " + _schregno + " add kekka " + _srcKekka + " + " + attendance._srcKekka + " ( " + _semester + ") => " + String.valueOf(_srcKekka + attendance._srcKekka));
//        		   }
//        	   }
               _kesseki += attendance._kesseki;
               if (null == _srcLate) {
                   _srcLate = attendance._srcLate;
               } else if (null != attendance._srcLate) {
                   _srcLate = _srcLate.add(attendance._srcLate);
               }
               if (null == _srcEarly) {
                   _srcEarly = attendance._srcEarly;
               } else if (null != attendance._srcEarly) {
                   _srcEarly = _srcEarly.add(attendance._srcEarly);
               }
               _srcKekka += attendance._srcKekka;
               _lesson += attendance._lesson;
               _offdays += attendance._offdays;
           }

           public Attendance calc(final String schregno, final Param param) {
               final Attendance c = new Attendance(schregno, null);
               c.add(param, this);

               if (param.isConvOnSemester() || param.isPrintRawLateEarly()) {
                   c._srcLate = _srcLate;
                   c._srcEarly = _srcEarly;
               } else{
                   c._srcLate = new BigDecimal(lateonyear(param, null == _srcLate ? 0.0 : _srcLate.doubleValue(), null == _srcEarly ? 0.0 : _srcEarly.doubleValue()));
                   c._srcEarly = new BigDecimal(earlyonyear(param, null == _srcLate ? 0.0 : _srcLate.doubleValue(), null == _srcEarly ? 0.0 : _srcEarly.doubleValue()));
               }
               c._srcKekka = param.isConvOnSemester() ? _srcKekka : countonyear(param, null == _srcLate ? 0.0 : _srcLate.doubleValue(), null == _srcEarly ? 0.0 : _srcEarly.doubleValue(), _srcKekka); // 欠課

               return c;
           }
       }
   }

   /*
   *
   *  学校教育システム 賢者 [出欠管理]  出席簿（科目別）
   * 作成日: 2005/03/28
   * 作成者: yamasiro
   *
   *  2005/03/28 yamashiro・明細出力を新様式として作成
   *  2005/04/15 yamashiro・’〜’をUNICODEで出力
   *  2005/04/28 yamashiro・時間割講座データ集計フラグが集計しないとなっている箇所を網掛け表示
   *  2005/06/03 yamashiro・集計表および明細表を科目単位で出力
   *  2005/10/22 yamashiro 印刷範囲日付と現在学期が異なる場合集計が出力されない不具合を修正
   *  2006/01/26 yamashiro・備考欄に異動情報を表示する --NO003 (05/12/05の仕様が再決定された）
   *                        ・異動情報は、該当日以降のページの備考欄に表示する
   *                      ・異動日以降の出席簿は「−」を重ね打ちする。但し、欠席等印が印字している場合のみ印字する。
   *  2006/02/09 yamashiro・異動情報は期間がある場合は期間を出力する --NO006
   *
   */
    private static class LIST {

        boolean hasdata;

        int MAX_LINE = 50;
        int COLUMN = 64;

        private final Param _param;

        LIST(final Param param) {
            _param = param;
        }

        /* svf print 印刷処理 */
        public void printSvf(final DB2UDB db2, final Vrw32alp svf) {

            //  ＳＶＦフォームの設定->学期制による
            final String form;
            if (2 < _param._semesdiv) {
                form = "KNJC053_1.frm";   //３学期制用 50行
            } else {
                form = "KNJC053_1.frm";   //２学期制用 50行
            }
            svf.VrSetForm(form, 4);   //２学期制用 50行

            svf.VrsOut("nendo",      _param._nendo);
            svf.VrsOut("scope_day",  _param._dateFromFormat + " " + FROM_TO_MARK + " " + _param._dateToFormat); //印刷範囲
            svf.VrsOut("TODAY",      _param._ctrlDateString);
            svf.VrsOut("STAFFNAME",  _param._staffName);
            svf.VrsOut("class",      _param._chairName);
            svf.VrsOut("CLASSNAME",  _param._subclassName);

            //  学期名称
            svf.VrsOut("SEMESTER", _param._semestername);

            final String subclassDatePeriodSql = getSubclassDatePeriodSql(_param);
            if (_param._isOutputDebug) {
                log.info(" subclass date period sql = " + subclassDatePeriodSql);
            }
            final List<Map<String, String>> executedateKeyMapList = KnjDbUtils.query(db2, subclassDatePeriodSql);
            if (_param._isOutputDebug) {
                log.info(" subclass date period size = " + executedateKeyMapList.size());
            }

            setTotalPage(svf, db2, executedateKeyMapList.size());

            final Map<Integer, PrintPage> pageMap = getPrintPageMap(db2);

            for (final Integer page : pageMap.keySet()) {
                final PrintPage ppage = pageMap.get(page);

                final Map<String, List<Attendance>> attendanceListMap = getAttendanceListMap(db2, ppage);

                if (printPage(svf, ppage, executedateKeyMapList, attendanceListMap)) {
                    hasdata = true;  //科目、出欠の取得と出力のメソッド
                }
            }
        }

        /**
         *  PrepareStatement作成 明細表の日付・校時の表
         */
        private String getSubclassDatePeriodSql(final Param param) {

            final StringBuffer stb = new StringBuffer();
            stb.append("WITH CHAIR_A AS(");
            stb.append("   SELECT ");
            if ("1".equals(param._knjc053useMeisaiExecutedate)) {
                stb.append("       CHAIRCD ");
                stb.append("     , APPDATE ");
                stb.append("     , APPENDDATE ");
            } else {
                stb.append("       CHAIRCD ");
            }
            stb.append("   FROM    CHAIR_STD_DAT T1 ");
            stb.append("   WHERE   YEAR = '" + param._year + "' AND ");
            stb.append("           SEMESTER <= '" + param._semester + "' ");

            //指定講座の生徒を対象とする
            stb.append("           AND T1.SCHREGNO IN (SELECT  SCHREGNO ");
            stb.append("                  FROM    CHAIR_STD_DAT T2 ");
            stb.append("                  WHERE   YEAR = '" + param._year + "' AND ");
            stb.append("                          SEMESTER <= '" + param._semester + "' AND ");
            stb.append("                          CHAIRCD = '" + param._attendClassCd + "' AND ");
            stb.append("                          (( APPDATE    BETWEEN '" + param._dateFrom + "' AND '" + param._dateTo + "'  )OR ");
            stb.append("                           ( APPENDDATE BETWEEN '" + param._dateFrom + "' AND '" + param._dateTo + "' )OR ");
            stb.append("                           ( '" + param._dateFrom + "' BETWEEN APPDATE AND APPENDDATE )OR ");
            stb.append("                           ( '" + param._dateTo + "' BETWEEN APPDATE AND APPENDDATE )) ");
            stb.append("                  GROUP BY SCHREGNO) ");

            //指定講座の科目を対象とする
            stb.append("           AND (T1.YEAR, T1.SEMESTER, T1.CHAIRCD, T1.SCHREGNO) IN (SELECT T2.YEAR, T2.SEMESTER, T2.CHAIRCD, T3.SCHREGNO ");
            stb.append("                  FROM    CHAIR_DAT T2 ");
            stb.append("                  INNER JOIN CHAIR_STD_DAT T3 ON T3.YEAR = T2.YEAR AND ");
            stb.append("                         T3.SEMESTER = T2.SEMESTER AND ");
            stb.append("                         T3.CHAIRCD = T2.CHAIRCD AND ");
            stb.append("                       ((t3.APPDATE    BETWEEN '" + param._dateFrom + "' AND '" + param._dateTo + "' ) OR ");
            stb.append("                        (t3.APPENDDATE BETWEEN '" + param._dateFrom + "' AND '" + param._dateTo + "' ) OR ");
            stb.append("                        ('" + param._dateFrom + "' BETWEEN t3.APPDATE AND t3.APPENDDATE ) OR ");
            stb.append("                        ('" + param._dateTo + "' BETWEEN t3.APPDATE AND t3.APPENDDATE )) ");
            stb.append("                  WHERE   T2.YEAR = '" + param._year + "' ");
            stb.append("                          AND T2.SEMESTER <= '" + param._semester + "' ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("                          AND (CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD) IN (SELECT CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ");
                stb.append("                                 FROM    CHAIR_DAT T3 ");
                stb.append("                                 WHERE   T3.YEAR = '" + param._year + "' AND ");
                stb.append("                                         T3.SEMESTER <= '" + param._semester + "' AND ");
                stb.append("                                         T3.CHAIRCD = '" + param._attendClassCd + "' ");
                stb.append("                                         ) ");
            } else {
                stb.append("                          AND SUBCLASSCD IN (SELECT SUBCLASSCD ");
                stb.append("                                 FROM    CHAIR_DAT T3 ");
                stb.append("                                 WHERE   T3.YEAR = '" + param._year + "' AND ");
                stb.append("                                         T3.SEMESTER <= '" + param._semester + "' AND ");
                stb.append("                                         T3.CHAIRCD = '" + param._attendClassCd + "' ");
                stb.append("                                         ) ");
            }
            stb.append("                  GROUP BY T2.YEAR, T2.SEMESTER, T2.CHAIRCD, T3.SCHREGNO) ");
            stb.append(" ) ");

            //メイン表
            stb.append("SELECT  T1.EXECUTEDATE, ");
            stb.append("        T1.PERIODCD, ");
            stb.append("        L1.NAME1 AS PERIODNAME ");
            stb.append("FROM    SCH_CHR_DAT T1 ");
            stb.append("LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'B001' AND L1.NAMECD2 = T1.PERIODCD ");
            if ("1".equals(param._knjc053useMeisaiExecutedate)) {
                stb.append("        INNER JOIN (SELECT CHAIRCD, APPDATE, APPENDDATE FROM CHAIR_A) T2 ON T2.CHAIRCD = T1.CHAIRCD ");
                stb.append("            AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            } else {
                stb.append("        INNER JOIN (SELECT CHAIRCD FROM CHAIR_A) T2 ON T2.CHAIRCD = T1.CHAIRCD ");
            }
            stb.append("WHERE   T1.EXECUTEDATE BETWEEN '" + param._dateFrom + "' AND '" + param._dateTo + "' ");
            stb.append("GROUP BY T1.EXECUTEDATE, T1.PERIODCD, L1.NAME1 ");
            stb.append("ORDER BY T1.EXECUTEDATE, T1.PERIODCD ");
            return stb.toString();
        }

        /**
         *  PrepareStatement作成
         *    時間割講座データ集計フラグの条件を追加 => '0'は集計しない
         */
        private String getAttendSql(final String attendnoFrom, final String attendnoTo) {

        //  出欠データ
            final StringBuffer stb = new StringBuffer();
            //対象学籍の表
            stb.append("WITH SCHNO_A AS(");
            stb.append("SELECT  W1.GRADE, W1.HR_CLASS, W1.ATTENDNO, W1.SCHREGNO ");
            stb.append("FROM    SCHREG_REGD_DAT W1 ");
            stb.append("WHERE   W1.YEAR = '" + _param._year + "' ");
            stb.append("    AND W1.GRADE||W1.HR_CLASS||W1.ATTENDNO BETWEEN '" + attendnoFrom + "' AND '" + attendnoTo + "' ");
            stb.append("    AND W1.SEMESTER = (SELECT  MAX(W2.SEMESTER) ");
            stb.append("                        FROM   SCHREG_REGD_DAT W2 ");
            stb.append("                        WHERE  W2.YEAR = W1.YEAR ");
            stb.append("                           AND W2.SEMESTER <= '" + _param._semester + "' ");
            stb.append("                           AND W2.SCHREGNO = W1.SCHREGNO) ");
            stb.append(") ");
            //対象講座コードの表
            stb.append(",CHAIR_B AS(");
            stb.append("   SELECT  T1.CHAIRCD ");
            stb.append("   FROM    CHAIR_STD_DAT T1 ");
            stb.append("   WHERE   T1.YEAR = '" + _param._year + "' AND ");
            stb.append("           T1.SEMESTER <= '" + _param._semester + "' ");
            //指定講座の生徒を対象とする
            stb.append("           AND SCHREGNO IN (SELECT  T2.SCHREGNO ");
            stb.append("                  FROM    CHAIR_STD_DAT T2 ");
            stb.append("                  WHERE   T2.YEAR = '" + _param._year + "' AND ");
            stb.append("                          T2.SEMESTER <= '" + _param._semester + "' AND ");
            stb.append("                          T2.CHAIRCD = '" + _param._attendClassCd + "' AND ");
            stb.append("                          (( T2.APPDATE    BETWEEN '" + _param._dateFrom + "' AND '" + _param._dateTo + "' )OR ");
            stb.append("                           ( T2.APPENDDATE BETWEEN '" + _param._dateFrom + "' AND '" + _param._dateTo + "' )OR ");
            stb.append("                           ( '" + _param._dateFrom + "' BETWEEN T2.APPDATE AND T2.APPENDDATE )OR ");
            stb.append("                           ( '" + _param._dateTo + "' BETWEEN T2.APPDATE AND T2.APPENDDATE )) ");
            stb.append("                  GROUP BY T2.SCHREGNO) ");
            //指定講座の科目を対象とする
            stb.append("           AND (T1.YEAR, T1.SEMESTER, T1.CHAIRCD, T1.SCHREGNO) IN (SELECT T2.YEAR, T2.SEMESTER, T2.CHAIRCD, T3.SCHREGNO ");
            stb.append("                  FROM    CHAIR_DAT T2 ");
            stb.append("                  INNER JOIN CHAIR_STD_DAT T3 ON T3.YEAR = T2.YEAR AND ");
            stb.append("                         T3.SEMESTER = T2.SEMESTER AND ");
            stb.append("                         T3.CHAIRCD = T2.CHAIRCD AND ");
            stb.append("                       ((t3.APPDATE    BETWEEN '" + _param._dateFrom + "' AND '" + _param._dateTo + "' ) OR ");
            stb.append("                        (t3.APPENDDATE BETWEEN '" + _param._dateFrom + "' AND '" + _param._dateTo + "' ) OR ");
            stb.append("                        ('" + _param._dateFrom + "' BETWEEN t3.APPDATE AND t3.APPENDDATE ) OR ");
            stb.append("                        ('" + _param._dateTo + "' BETWEEN t3.APPDATE AND t3.APPENDDATE )) ");
            stb.append("                  WHERE   T2.YEAR = '" + _param._year + "' AND ");
            stb.append("                          T2.SEMESTER <= '" + _param._semester + "' ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("                          AND (T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, T2.SUBCLASSCD) IN (SELECT T3.CLASSCD, T3.SCHOOL_KIND, T3.CURRICULUM_CD, T3.SUBCLASSCD ");
                stb.append("                                 FROM    CHAIR_DAT T3 ");
                stb.append("                                 WHERE   T3.YEAR = '" + _param._year + "' AND ");
                stb.append("                                         T3.SEMESTER <= '" + _param._semester + "' AND ");
                stb.append("                                         T3.CHAIRCD = '" + _param._attendClassCd + "') ");
            } else {
                stb.append("                          AND T2.SUBCLASSCD IN (SELECT T3.SUBCLASSCD ");
                stb.append("                                 FROM    CHAIR_DAT T3 ");
                stb.append("                                 WHERE   T3.YEAR = '" + _param._year + "' AND ");
                stb.append("                                         T3.SEMESTER <= '" + _param._semester + "' AND ");
                stb.append("                                         T3.CHAIRCD = '" + _param._attendClassCd + "') ");
            }
            stb.append("                  ) ");
            stb.append("                  GROUP BY CHAIRCD ");
            stb.append(") ");
            //テストの集計フラグ
            stb.append(",TEST_COUNTFLG AS (" );
            stb.append("     SELECT ");
            stb.append("         T1.EXECUTEDATE, ");
            stb.append("         T1.PERIODCD, ");
            stb.append("         T1.CHAIRCD, ");
            stb.append("         '2' AS DATADIV, ");
            stb.append("         T2.COUNTFLG ");
            stb.append("     FROM ");
            stb.append("         SCH_CHR_TEST T1, ");
            if ("TESTITEM_MST_COUNTFLG".equals(_param._useTestCountflg)) {
                stb.append("         TESTITEM_MST_COUNTFLG T2 ");
                stb.append("     WHERE ");
                stb.append("         T2.YEAR       = T1.YEAR ");
            } else if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(_param._useTestCountflg)) {
                stb.append("         TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ");
                stb.append("     WHERE ");
                stb.append("         T2.YEAR       = T1.YEAR ");
                stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
                stb.append("         AND T2.SCORE_DIV  = '01' ");
            } else {
                stb.append("         TESTITEM_MST_COUNTFLG_NEW T2 ");
                stb.append("     WHERE ");
                stb.append("         T2.YEAR       = T1.YEAR ");
                stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
            }
            stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
            stb.append(") ");
            //対象講座生徒の表
            stb.append(",CHAIR_STD_A AS(");
            stb.append("SELECT  STD.SCHREGNO, STD.CHAIRCD, STD.APPDATE, STD.APPENDDATE, STD.SEMESTER ");
            stb.append("FROM    CHAIR_STD_DAT STD ");
            stb.append("WHERE   STD.YEAR = '" + _param._year + "' ");
            stb.append("    AND STD.SEMESTER <= '" + _param._semester + "' ");
            stb.append("    AND (( STD.APPDATE    BETWEEN '" + _param._dateFrom + "' AND '" + _param._dateTo + "' ) ");
            stb.append("      OR ( STD.APPENDDATE BETWEEN '" + _param._dateFrom + "' AND '" + _param._dateTo + "' ) ");
            stb.append("      OR ( '" + _param._dateFrom + "' BETWEEN STD.APPDATE AND STD.APPENDDATE ) ");
            stb.append("      OR ( '" + _param._dateTo + "' BETWEEN STD.APPDATE AND STD.APPENDDATE )) ");
            stb.append("    AND STD.CHAIRCD IN (SELECT W2.CHAIRCD FROM CHAIR_B W2) ");
            stb.append("    AND STD.SCHREGNO IN (SELECT W2.SCHREGNO FROM SCHNO_A W2) ");
            stb.append(") ");
            //時間割データとリンクした出欠データの表
            stb.append(",ATTEND_A AS(");
            stb.append("SELECT  STD.SCHREGNO, SCHE.CHAIRCD, SCHE.EXECUTEDATE, SCHE.PERIODCD, L1.DI_CD, SCHE.DATADIV ");
            stb.append("FROM ");
            stb.append("    SCH_CHR_DAT SCHE ");
            stb.append("    INNER JOIN CHAIR_STD_A STD ON SCHE.EXECUTEDATE BETWEEN STD.APPDATE AND STD.APPENDDATE ");
            stb.append("        AND STD.CHAIRCD = SCHE.CHAIRCD ");
            stb.append("    INNER JOIN ATTEND_DAT W2 ON W2.ATTENDDATE = SCHE.EXECUTEDATE ");
            stb.append("        AND W2.PERIODCD = SCHE.PERIODCD ");
            stb.append("        AND STD.SCHREGNO = W2.SCHREGNO ");
            stb.append("    LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = W2.YEAR AND L1.DI_CD = W2.DI_CD ");
            stb.append("WHERE ");
            stb.append("    SCHE.EXECUTEDATE BETWEEN '" + _param._dateFrom + "' AND '" + _param._dateTo + "' ");
            stb.append("    AND STD.SEMESTER = SCHE.SEMESTER ");
            stb.append(") ");

            //メイン表
            stb.append("SELECT  T2.GRADE || T2.HR_CLASS || T2.ATTENDNO AS ATTENDNO2, ");
            stb.append("        T1.SCHREGNO, ");
            stb.append("        T1.EXECUTEDATE AS ATTENDDATE, ");
            stb.append("        T1.PERIODCD, ");
            stb.append("        T1.DI_CD, ");
            stb.append("        NMC001.ABBV1 AS DI_NAME, ");
            stb.append("        CASE WHEN W5.SCHREGNO IS NOT NULL THEN 1 WHEN W6.SCHREGNO IS NOT NULL THEN 1 WHEN W7.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS LEAVE,");
            if (_param._definecode.useschchrcountflg) {
                stb.append("    CASE WHEN VALUE(T1.DATADIV, '0') = '2' THEN VALUE(T4.COUNTFLG, '1') ");
                stb.append("         WHEN VALUE(T1.DATADIV, '0') IN ('0', '1') THEN VALUE(T3.COUNTFLG, '1') ");
                stb.append("         ELSE '1' END AS COUNTFLG ");
            }
            stb.append("FROM    ATTEND_A T1 ");
            stb.append("INNER JOIN SCHNO_A T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            if (_param._definecode.useschchrcountflg) {
                stb.append("LEFT JOIN SCH_CHR_COUNTFLG T3 ON T3.EXECUTEDATE = T1.EXECUTEDATE ");
                stb.append("                             AND T3.PERIODCD = T1.PERIODCD AND T3.GRADE = T2.GRADE ");
                stb.append("                             AND T3.HR_CLASS = T2.HR_CLASS AND T3.CHAIRCD = T1.CHAIRCD ");
                stb.append("                             AND T1.DATADIV IN ('0', '1') ");
                stb.append("LEFT JOIN TEST_COUNTFLG T4 ON T4.EXECUTEDATE = T1.EXECUTEDATE ");
                stb.append("                             AND T4.PERIODCD = T1.PERIODCD AND T4.CHAIRCD = T1.CHAIRCD ");
                stb.append("                             AND T4.DATADIV = T1.DATADIV ");
            }
            stb.append(" LEFT JOIN SCHREG_BASE_MST W5");
            stb.append("  ON W5.SCHREGNO = T1.SCHREGNO AND W5.GRD_DIV IN('2','3') AND T1.EXECUTEDATE > W5.GRD_DATE");
            stb.append(" LEFT JOIN SCHREG_BASE_MST W6");
            stb.append("  ON W6.SCHREGNO = T1.SCHREGNO AND W6.ENT_DIV IN('4','5') AND T1.EXECUTEDATE < W6.ENT_DATE");
            stb.append(" LEFT JOIN SCHREG_TRANSFER_DAT W7");
            stb.append("  ON W7.SCHREGNO = T1.SCHREGNO AND TRANSFERCD IN('1','2') AND T1.EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE");
            stb.append(" LEFT JOIN NAME_MST NMC001 ON NMC001.NAMECD1 = 'C001' ");
            stb.append("  AND NMC001.NAMECD2 = T1.DI_CD ");

            stb.append(" ORDER BY T1.EXECUTEDATE, T1.PERIODCD ");
            return stb.toString();
        }

        private Map<Integer, PrintPage> getPrintPageMap(final DB2UDB db2) {
            final Map<Integer, PrintPage> pageMap = new TreeMap();

            //生徒情報
            final String sql = setStatementSchInfo(_param);

            Integer page = new Integer(1);
            //  生徒名等SVF出力
            int line = 0;
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                line += 1;
                if (null == pageMap.get(page)) {
                    pageMap.put(page, new PrintPage(page));
                }
                final PrintPage p = pageMap.get(page);

                final Student student = new Student(KnjDbUtils.getString(row, "SCHREGNO"), line);
                p.hm1.put(student.schregno, student);
                student.attendno = KnjDbUtils.getString(row, "ATTENDNO");
                student.name = KnjDbUtils.getString(row, "NAME");
                student.schInfoMap = getSchInfo(row);
                if (line == 1) {
                    p.attendnoFrom = KnjDbUtils.getString(row, "ATTENDNO2");    //開始生徒
                }
                p.attendnoTo = KnjDbUtils.getString(row, "ATTENDNO2");                  //終了生徒
                if (line == MAX_LINE) {
                    line = 0;
                    page = new Integer(page.intValue() + 1);
                }
            }
            return pageMap;
        }

        private static class Student {
            final String schregno;
            final int line;
            String name;
            String attendno;
            HashMap<String, String> schInfoMap;
            Student(final String schregno, final int line) {
                this.schregno = schregno;
                this.line = line;
            }
        }

        private static class PrintPage {
            final Integer _page;
            final Map<String, Student> hm1 = new HashMap();
            String attendnoFrom;
            String attendnoTo;
            PrintPage(final Integer page) {
                _page = page;
            }
        }

        private static class Attendance {
            final Student _student;
            final String _countflg;
            final String _diName;
            final String _leave;
            public Attendance(final Student student, final String countflg, final String diName, final String leave) {
                _student = student;
                _countflg = countflg;
                _diName = diName;
                _leave = leave;
            }
        }

        /**
         *  備考の保管処理
         */
        private HashMap<String, String> getSchInfo(final Map row) {
            // 異動情報の優先順位は 「留学・休学 > 退学・転学 > 転入・編入」 とする
            final String kbnDate2 = KnjDbUtils.getString(row, "KBN_DATE2");
            final String kbnDate2e = KnjDbUtils.getString(row, "KBN_DATE2E");
            final String kbnName2 = KnjDbUtils.getString(row, "KBN_NAME2");
            final String kbnDate1 = KnjDbUtils.getString(row, "KBN_DATE1");
            final String kbnName1 = KnjDbUtils.getString(row, "KBN_NAME1");
            final String kbnDiv1 = KnjDbUtils.getString(row, "KBN_DIV1");

            String str = null;
            if (kbnDate2 == null || kbnDate1 == null) {
                return null;
            } else if (kbnDate2 != null) {
                str = _param.sdf2.format(toCal(kbnDate2).getTime()) + FROM_TO_MARK + _param.sdf2.format(toCal(kbnDate2e).getTime()) + StringUtils.defaultString(kbnName2);
            } else if (kbnDate1 != null) {
                str = _param.sdf3.format(toCal(kbnDate1).getTime()) + StringUtils.defaultString(kbnName1);
            }

            final HashMap<String, String> tmap = new HashMap();
            tmap.put("INFO", str);

            // 異動情報の優先順位は 「留学・休学 > 退学・転学 > 転入・編入」 とする
            if (kbnDate2 != null) {
                tmap.put("SDATE", kbnDate2);
                tmap.put("EDATE", kbnDate2e);
            } else if (kbnDate1 != null) {
                if ("1".equals(kbnDiv1)) {
                    tmap.put("EDATE", kbnDate1);
                } else {
                    tmap.put("SDATE", kbnDate1);
                }
            }
            return tmap;
        }

        /**
         *  科目、出欠の取得と出力
         *    '時間割講座データ集計フラグが集計しないとなっている箇所を網掛け表示'に対応
         */
        private boolean printPage(final Vrw32alp svf, final PrintPage ppage, final List<Map<String, String>> executedateKeyMapList, final Map<String, List<Attendance>> attendanceListMap) {
            boolean hasdata = false;

            //  日付等SVF出力
            int lcount = 0;
            for (final Map<String, String> keyMap : executedateKeyMapList) {

                final String executedate = keyMap.get("EXECUTEDATE");
                final String periodcd = keyMap.get("PERIODCD");

                //日付等出力のメソッド
                svf.VrsOut("day_item_mm", KNJ_EditDate.h_format_S(executedate,"M"));
                svf.VrsOut("day_item_dd", KNJ_EditDate.h_format_S(executedate,"d"));
                svf.VrsOut("day_item_week", KNJ_EditDate.h_format_W(executedate));
                if (keyMap.get("PERIODNAME") != null) {
                    svf.VrsOut("day_item_hh", keyMap.get("PERIODNAME"));
                }

                printStudent(svf, executedate, ppage);

                final List<Attendance> attendanceList = attendanceListMap.get(executedate + periodcd);
                if (null != attendanceList) {
                    for (final Attendance att : attendanceList) {
                        pritnAttendance(svf, att);            //出欠データ出力のメソッド
                    }
                }

                if (0 == lcount % COLUMN) {
                    // 欄外コメント出力
                    svf.VrAttribute("MARK1",  "Paint=(2,70,1),Bold=1");
                    svf.VrsOut("MARK1",  "  " );
                    svf.VrsOut("MARK2",  "＝" );
                }

                svf.VrEndRecord();
                lcount++;
                hasdata = true;
            }
            if (hasdata) {          //最終ページに累計を出力
                for (; lcount % COLUMN > 0; lcount++) {
                    svf.VrEndRecord();
                }
                svf.VrPrint();
                for(int i = 1; i < MAX_LINE + 1; i++) {
                    if ("1".equals(_param._use_SchregNo_hyoji)) {
                        svf.VrsOutn("SCHREGNO", i, "");       //学籍番号
                        svf.VrsOutn("HR_NAME2",   i, "");       //学年組出席番号
                        svf.VrsOutn("NAME_SHOW2", i, "");       //生徒名
                    } else  {
                        svf.VrsOutn("HR_NAME",   i, "");       //学年組出席番号
                        svf.VrsOutn("NAME_SHOW", i, "");       //生徒名
                    }
                    svf.VrsOutn("attend", i, "");
                    svf.VrsOutn("NOTE", i, "");
                }
            }
            return hasdata;
        }//Set_Detail_2()の括り

        private Map<String, List<Attendance>> getAttendanceListMap(final DB2UDB db2, final PrintPage ppage) {

            final Map<String, List<Attendance>> attendanceListMap = new HashMap();

            //  出欠データ
            final String sql = getAttendSql(ppage.attendnoFrom, ppage.attendnoTo);
            if (_param._isOutputDebug) {
                log.info(" attend sql = " + sql);
            }

            for (final Map row : KnjDbUtils.query(db2, sql)) {
                final String attenddate = KnjDbUtils.getString(row, "ATTENDDATE");
                final String periodcd = KnjDbUtils.getString(row, "PERIODCD");

                final Student student = ppage.hm1.get(KnjDbUtils.getString(row, "SCHREGNO"));
                if (null == student) {
                    continue;
                }
                final Attendance att = new Attendance(student, KnjDbUtils.getString(row, "COUNTFLG"), KnjDbUtils.getString(row, "DI_NAME"), KnjDbUtils.getString(row, "LEAVE"));
                getMappedList(attendanceListMap, attenddate + periodcd).add(att);
            }
            return attendanceListMap;
        }

        private void printStudent(final Vrw32alp svf, final String executedate, final PrintPage ppage) {
            for (final String schregno : ppage.hm1.keySet()) {
                boolean biko = false;
                final Student student = ppage.hm1.get(schregno);
                if ("1".equals(_param._use_SchregNo_hyoji)) {
                    svf.VrsOutn("SCHREGNO", student.line, student.schregno); // 学籍番号
                    svf.VrsOutn("HR_NAME2", student.line, student.attendno); // 学年組出席番号
                    svf.VrsOutn("NAME_SHOW2", student.line, student.name); // 生徒名
                } else  {
                    svf.VrsOutn("HR_NAME", student.line, student.attendno); // 学年組出席番号
                    svf.VrsOutn("NAME_SHOW", student.line, student.name); // 生徒名
                }
                if (null != student.schInfoMap) {
                    // 備考の出力処理
                    final HashMap<String, String> m = student.schInfoMap;
                    if (m.containsKey("EDATE") && !m.containsKey("SDATE")) {
                        final Calendar cala = toCal(executedate); //時間割日
                        final Calendar calc = toCal(m.get("EDATE"));
                        if (!cala.before(calc)) {
                            biko = true;
                        }
                    } else if(m.containsKey("SDATE") && !m.containsKey("EDATE")) {
                        final Calendar cala = toCal(executedate); //時間割日
                        final Calendar calc = toCal(m.get("SDATE"));
                        if(!cala.before(calc)) {
                            biko = true;
                        }
                    } else if (m.containsKey("SDATE") && m.containsKey("EDATE")) {
                        final Calendar cala = toCal(executedate); //時間割日
                        final Calendar calb = toCal(m.get("SDATE"));
                        final Calendar calc = toCal(m.get("EDATE"));
                        if (!cala.after(calc) && !cala.before(calb)) {
                            biko = true;
                        }
                    }
                    if (biko) {
                        svf.VrsOutn("NOTE", student.line, (String) (m.get("INFO")));  //備考
                    }
                }
            }
        }

        /**
         *  出欠データ出力
         *    '時間割講座データ集計フラグが集計しないとなっている箇所を網掛け表示'に対応
         */
        private void pritnAttendance(final Vrw32alp svf, final Attendance a) {
            boolean amikake = false;
            //時間割講座データ集計フラグを使用する場合の網掛け設定
            if (_param._definecode.useschchrcountflg) {
                amikake = (Integer.parseInt(a._countflg) == 0) ? true : false;
                if (amikake) {
                    svf.VrAttributen("attend", a._student.line, "Paint=(2,70,1),Bold=1");
                }
            }
            //出欠記号
            if (a._diName != null) {
                svf.VrsOutn("attend", a._student.line, a._diName);
                // 取り消し線
                if (a._leave != null && Integer.parseInt(a._leave) == 1) {
                    svf.VrsOutn("CLEAR_attend", a._student.line, "＝" );
                }
            }
            //網掛け設定解除
            if (_param._definecode.useschchrcountflg) {
                if (amikake) {
                    svf.VrAttributen("attend", a._student.line, "Paint=(0,0,0),Bold=0");
                }
            }
        }

        private void setTotalPage(final Vrw32alp svf, final DB2UDB db2, final int scheduleCount) {

            Integer creditsMax = null;
            int c_sch = 0;                      //生徒数
            //生徒数取得
            final String sql = prestateSchnumAndCredits(_param);

            for (final Map row : KnjDbUtils.query(db2, sql)) {
                c_sch = KnjDbUtils.getInt(row, "COUNT", new Integer(0)).intValue();           //生徒数
                if (KnjDbUtils.getString(row, "CREDITS") != null) {
                    if (creditsMax == null || creditsMax.intValue() < KnjDbUtils.getInt(row, "CREDITS", new Integer(0)).intValue()) {
                        creditsMax = Integer.valueOf(KnjDbUtils.getString(row, "CREDITS"));
                    }
                }
            }

            final StringBuffer creditsStr = new StringBuffer(" ( ");
            if (null != creditsMax) {
                creditsStr.append(creditsMax).append(" 単位");
            }
            creditsStr.append(" )");

            svf.VrsOut("CLASSNAME", _param._subclassName + creditsStr.toString());

            log.debug(" scheduleCount = " + scheduleCount);
            final int schedulePage = scheduleCount / COLUMN + (scheduleCount % COLUMN  > 0 ? 1 : 0);
            final int studentPagePerChairPage = c_sch / MAX_LINE + (c_sch % MAX_LINE > 0 ? 1 : 0);
            final int page = schedulePage * studentPagePerChairPage;                     //総ページ数

            svf.VrlOut("TOTAL_PAGE", page);
        }
    }//クラスの括り

}//クラスの括り
