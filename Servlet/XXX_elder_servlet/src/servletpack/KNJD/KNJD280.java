// kanji=漢字
/*
 * $Id: ccb34d7ccb69598943e953509189008bd42ba3f7 $
 *
 * 作成日: 2005/05/08
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJD;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;


/*
 *
 *  学校教育システム 賢者 [成績管理] 単位履修修得表
 *
 *  2005/05/08 yamashiro・新規作成
 *  2005/10/21 nakamoto ・年組番号と作成日を追加。教科名表示をグループサプレスに変更。---NO001
 *  2006/02/01 yamasihro・2学期制の場合、1月〜3月の出欠集計データのソート順による集計の不具合を修正 --NO004
 *  2006/03/01 yamashiro・--NO004修正時の不具合を修正 DB2の型変換の使用方法に間違いがあった --NO007
 *  2006/03/27 yamashiro・○履修済科目は「学習記録データ」より出力  --NO002
 *                        ○履修済以外は該当生徒の年度、学年、コースにより「単位マスタ」から出力  --NO002
 *                        ○必修科目の「○」は、履修済科目のみ出力  --NO002
 *                        ○学外単位等は「学習記録データ」にあれば、そのまま出力  --NO002
 *  2006/11/21 nakamoto ・履修科目出力の対応 --- NO003
 *                        ○ログイン年度まで出力する
 *                        ○ログイン年度の履修科目：出力する   → 講座名簿から出力
 *                                              　：出力しない → 学習記録データから出力
 *                        ○「０年次単位」は、０年度０年次を出力する
 *                        ○同一科目は、全て合計して出力する
 *                        ○読替元科目は、表示しない
 *  2006/11/22 nakamoto ・履修科目出力の対応 --- NO003
 *                        ○教科コード’91’以上は出力しない。
 *                        ○ログイン年度の履修科目を出力する場合、欄外（左下）に「※は今年度の履修科目です。」と表示する
 */

public class KNJD280 {

    private static final Log log = LogFactory.getLog(KNJD280.class);

    private boolean _hasData;

    /**
     *
     *  KNJD.classから最初に起動されるクラス
     *
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();    //帳票におけるＳＶＦおよびＤＢ２の設定
        Vrw32alp svf = new Vrw32alp();      //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                  //Databaseクラスを継承したクラス

        // print svf設定
        sd.setSvfInit(request, response, svf);
        // ＤＢ接続
        db2 = sd.setDb(request);
        if (sd.openDb(db2)) {
            log.error("db open error");
            return;
        }
        // パラメータの取得
        final Param param = createParam(request, db2);

        // 印刷処理
        printSvf(db2, svf, param);
        // 終了処理
        sd.closeSvf(svf, _hasData);
        sd.closeDb(db2);
    }


    /**
     *
     *  印刷処理
     *
     */
    private void printSvf( DB2UDB db2, Vrw32alp svf, final Param param) {

        final List<Student> studentList = Student.getStudentList(db2, param);

        for (final Student student : studentList) {
            if (param._isOutputDebug) {
                log.info(" print " + student._schregno);
            }
            printStudent(db2, svf, param, student);
        }
    }

    /**
     *
     *  SVF-FORM メイン出力処理
     *
     */
    private void printStudent(final DB2UDB db2, final Vrw32alp svf, final Param param, final Student student) {
        final String formname;
        if ("1".equals(param._knjd280UseForm2)) {
            formname = "KNJD280_2.frm";
        } else {
            formname = "KNJD280.frm";
        }
        svf.VrSetForm(formname, 4);

        if (!student._schregRow.isEmpty()) {
            svf.VrsOut("COURSE",    StringUtils.defaultString(KnjDbUtils.getString(student._schregRow, "MAJORNAME")));
            svf.VrsOut("SCHREGNO",  KnjDbUtils.getString(student._schregRow, "SCHREGNO"));
            svf.VrsOut("NAME",      StringUtils.defaultString(KnjDbUtils.getString(student._schregRow, "NAME")));
            svf.VrsOut("BIRTHDAY",  KNJ_EditDate.h_format_JP(db2, KnjDbUtils.getString(student._schregRow, "BIRTHDAY")));
            if (KnjDbUtils.getString(student._schregRow, "HR_NAME") != null) {
                svf.VrsOut("HR_CLASS",  KnjDbUtils.getString(student._schregRow, "HR_NAME") + " " + String.valueOf(KnjDbUtils.getInt(student._schregRow, "ATTENDNO", null)) + "番");
            }
            if (param._date != null) {
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, param._date));
            }
            if (param._subChk != null) {
                svf.VrsOut("MARK", "※は今年度の履修科目です。");
            }
            _hasData = true;
        }

        printSvfOutMeisai(svf, param, student);
    }

    /**
     *   ＨＲ成績生徒別明細を出力 => VrEndRecord()
     */
    private void printSvfOutMeisai(final Vrw32alp svf, final Param param, final Student student) {

        final int maxline = 45;
        int linex = 0;                                      //１ページ当り出力行数
        final int[] totalcredits = {0,0,0,0,0,0,0,0};               //修得単位数計

        for (final List<Map<String, String>> subclassRows : student._subclassRowsList) {

            final Map<String, String> row0 = subclassRows.get(0);
            final int col = ( linex / maxline == 0) ? 1 : ((linex / maxline) % 2 == 0) ? 1 : 2;

            svf.VrsOut("CLASS"    + col,  KnjDbUtils.getString(row0, "CLASSNAME"));         //教科名
            final String subclassname = KnjDbUtils.getString(row0, "SUBCLASSNAME");
            if (param._isOutputDebug) {
                log.info("  subclass " + KnjDbUtils.getString(row0, "SUBCLASSCD") + " " + subclassname);
            }
            if ("1".equals(param._knjd280UseForm2)) {
                final int subclassnameKeta = KNJ_EditEdit.getMS932ByteLength(subclassname);
                svf.VrsOut("SUBCLASS" + col + (subclassnameKeta <= 19 ? "" : subclassnameKeta <= 22 ? "_2" : "_3"),  subclassname);      //科目名
            } else {
                svf.VrsOut("SUBCLASS" + col,  subclassname);      //科目名
            }
            svf.VrsOut("R_CREDIT" + col,  KnjDbUtils.getString(row0, "SUBCLASS_CREDITS"));  //登録単位数
            svf.VrsOut("SUBJECT"  + col,  ( KnjDbUtils.getString(row0, "ELECTDIV") != null  &&  Integer.parseInt( KnjDbUtils.getString(row0, "ELECTDIV")) == 0) ? "○" : "");    //必修科目

            int subclasscredits = 0;
            //明細の出力
            // 年次の単位を出力
            for (final Map<String, String> row : subclassRows) {
                int retvalue = 0;
                final String credits = KnjDbUtils.getString(row, "CREDITS");
                final String recordScoreCredits = KnjDbUtils.getString(row, "RECORD_SCORE_CREDIT");
                if ((credits != null || recordScoreCredits != null) && KnjDbUtils.getString(row, "ANNUAL") != null) {
                    final int annual = Integer.parseInt(KnjDbUtils.getString(row, "ANNUAL"));
                    //log.debug("annual="+annual+"   i="+i);
                    if (0 <= annual  &&  annual <= 6) {
                        if (NumberUtils.isNumber(recordScoreCredits)) {
                            svf.VrsOut("CREDIT" + annual + "_" + col, recordScoreCredits);
                            retvalue = Integer.parseInt(recordScoreCredits);
                            totalcredits[annual] += Integer.parseInt(recordScoreCredits);    //年次計
                        } else if (Integer.parseInt(credits) < 0) {
                            svf.VrsOut("CREDIT" + annual + "_" + col, "※");
                        } else {
                            svf.VrsOut("CREDIT" + annual + "_" + col, credits);
                            retvalue = Integer.parseInt(credits);
                            totalcredits[annual] += Integer.parseInt(credits);    //年次計
                        }
                    }
                }
                subclasscredits += retvalue;
            }

            // 科目別修得単位数を出力
            if (0 < subclasscredits) {
                svf.VrsOut("F_CREDIT" + col, String.valueOf(subclasscredits));        //修得単位数
                totalcredits[totalcredits.length - 1] += subclasscredits; //総計
            }

            svf.VrEndRecord();
            _hasData = true;
            linex++;
        }

        // 修得単位数計の行を出力
        final int k = linex % (maxline * 2); // 現ページの出力済行数
        for (int j = k; j < maxline * 2 - 1 ; j++) {
            //log.debug("k="+k+"  j="+j+"  linex="+linex);
            if (linex % (maxline * 2) == 0  ||  linex % (maxline * 2) < maxline) {
                svf.VrAttribute("RECORD1", "Print=1");
                svf.VrAttribute("CLASS1", "Meido=100");
                svf.VrsOut("CLASS1",  String.valueOf(linex));         //教科名
            } else {
                svf.VrAttribute("RECORD2", "Print=1");
                svf.VrAttribute("CLASS2", "Meido=100");
                svf.VrsOut("CLASS2", String.valueOf(linex));         //教科名
            }
            svf.VrEndRecord();
            linex++;
        }

        for (int ti = 0; ti < totalcredits.length - 1; ti++) {
            svf.VrsOut("TOTAL_CREDIT" + ti, String.valueOf(totalcredits[ti]));     //修得単位数
        }
        svf.VrsOut("F_TOTAL_CREDIT", String.valueOf(totalcredits[totalcredits.length - 1]));     //修得単位数
        svf.VrEndRecord();
    }

    private static class Student {
        final String _schregno;
        Map<String, String> _schregRow = Collections.emptyMap();
        List<List<Map<String, String>>> _subclassRowsList = Collections.emptyList();

        Student(final String schregno) {
            _schregno = schregno;
        }

        private static List<Student> getStudentList(DB2UDB db2, final Param param) {
            final List<Student> studentList = new ArrayList<Student>();
            PreparedStatement ps1 = null, ps2 = null;
            try {
                final String sqlSchreg = prestatementSchreg( param);
                ps1 = db2.prepareStatement( sqlSchreg);
                final String sqlSeiseki = prestatementRecord( param);
                ps2 = db2.prepareStatement( sqlSeiseki);

                if (param._isOutputDebug) {
                    log.info(" sql seiseki = " + sqlSeiseki);
                    log.info(" schregno = " + ArrayUtils.toString(param._categorySelected));
                }

                for (final String schregno : param._categorySelected) {
                    final Student student = new Student(schregno);

                    student._schregRow = KnjDbUtils.firstRow(KnjDbUtils.query(db2, ps1, new Object[] {schregno}));

                    final List<Map<String, String>> seisekiList = KnjDbUtils.query(db2, ps2, new Object[] { student._schregno });

                    final List<List<Map<String, String>>> subclassRowsList = new ArrayList<List<Map<String, String>>>();

                    {
                        List<Map<String, String>> currentList = null;
                        String subclasscd = null;
                        for (final Map<String, String> row : seisekiList) {
                            if (null == subclasscd || !subclasscd.equals(KnjDbUtils.getString(row, "SUBCLASSCD"))) {
                                currentList = new ArrayList<Map<String, String>>();
                                subclassRowsList.add(currentList);
                            }
                            currentList.add(row);
                            subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                        }
                    }
                    student._subclassRowsList = subclassRowsList;

                    studentList.add(student);
                }
            } catch (Exception ex) {
                log.error("error! ",ex);
            } finally {
                DbUtils.closeQuietly(ps1);
                DbUtils.closeQuietly(ps2);
            }
            return studentList;
        }

        /**
        *
        *   SQLStatement作成 生徒学籍情報を取得
        *
        */
       private static String prestatementSchreg(final Param param) {

           final StringBuffer stb = new StringBuffer();
           stb.append("SELECT  T1.SCHREGNO, MAJORNAME, NAME, BIRTHDAY ");
           stb.append(        ",T4.HR_NAME ,ATTENDNO ");
           stb.append("FROM    SCHREG_REGD_DAT T1 ");
           stb.append(        "INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
           stb.append(        "LEFT JOIN MAJOR_MST T3 ON T3.MAJORCD = T1.MAJORCD ");
           stb.append(        "LEFT JOIN SCHREG_REGD_HDAT T4 ON T4.YEAR = T1.YEAR ");
           stb.append(                                     "AND T4.SEMESTER = T1.SEMESTER ");
           stb.append(                                     "AND T4.GRADE = T1.GRADE ");
           stb.append(                                     "AND T4.HR_CLASS = T1.HR_CLASS ");
           stb.append("WHERE   T1.SCHREGNO = ? AND T1.YEAR = '" + param._year + "' AND ");
           stb.append(        "T1.SEMESTER = (SELECT  MAX(SEMESTER) ");
           stb.append(                       "FROM    SCHREG_REGD_DAT T2 ");
           stb.append(                       "WHERE   T2.YEAR = T1.YEAR AND ");
           stb.append(                               "T2.SCHREGNO = T1.SCHREGNO) ");
           return stb.toString();
       }

       /**
        *   SQLStatement作成 学習記録データ
        *   NO002 Modify
        */
       private static String prestatementRecord(final Param param) {
           final StringBuffer stb = new StringBuffer();
           stb.append("WITH ");
           //学籍の表
           stb.append("SCHNO_A AS(");
           stb.append(   "SELECT  T1.SCHREGNO, T1.YEAR, T1.GRADE, T1.HR_CLASS, T1.ANNUAL, ");
           stb.append(           "T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
           stb.append(   "FROM    SCHREG_REGD_DAT T1 ");
           stb.append(   "WHERE   T1.SCHREGNO = ? ");
           stb.append(       "AND T1.YEAR <= '" + param._year + "' ");
           stb.append(       "AND T1.SEMESTER = (SELECT  MAX(SEMESTER) ");
           stb.append(                          "FROM    SCHREG_REGD_DAT T2 ");
           stb.append(                          "WHERE   T2.YEAR = T1.YEAR ");
           stb.append(                              "AND T2.SCHREGNO = T1.SCHREGNO) ");
           stb.append(   ") ");

           //該当する単位マスターの表
           stb.append(",SUBCLASS_CREDIT AS(");
           stb.append(   "SELECT  REGD.YEAR, REGD.ANNUAL, ");
           if ("1".equals(param._useCurriculumcd)) {
               stb.append(           "CRED.CLASSCD, ");
               stb.append(           "CRED.SCHOOL_KIND, ");
               stb.append(           "CRED.CURRICULUM_CD, ");
           }
           stb.append(           "CRED.SUBCLASSCD, ");
           stb.append(           "CRED.CREDITS ");
           stb.append(   "FROM    CREDIT_MST CRED ");
           stb.append(          ",SCHNO_A REGD ");
           stb.append(          ",SCHREG_STUDYREC_DAT T2 ");
           stb.append(   "WHERE   CRED.YEAR = REGD.YEAR ");
           stb.append(       "AND CRED.GRADE = REGD.GRADE ");
           stb.append(       "AND CRED.COURSECD = REGD.COURSECD ");
           stb.append(       "AND CRED.MAJORCD = REGD.MAJORCD ");
           stb.append(       "AND CRED.COURSECODE = REGD.COURSECODE ");
           stb.append(       "AND CRED.YEAR = T2.YEAR ");
           stb.append(       "AND REGD.SCHREGNO = T2.SCHREGNO ");
           if ("1".equals(param._useCurriculumcd)) {
               stb.append(       "AND CRED.CLASSCD = T2.CLASSCD ");
               stb.append(       "AND CRED.SCHOOL_KIND = T2.SCHOOL_KIND ");
               stb.append(       "AND CRED.CURRICULUM_CD = T2.CURRICULUM_CD ");
           }
           stb.append(       "AND CRED.SUBCLASSCD = T2.SUBCLASSCD ");
           stb.append(   ") ");

           //単位数合計の表
           stb.append(",SUBCLASS_CREDIT_TOTAL AS(");
           stb.append(   "SELECT  SUBCLASSCD, ");
           if ("1".equals(param._useCurriculumcd)) {
               stb.append(           "CLASSCD, ");
               stb.append(           "SCHOOL_KIND, ");
               stb.append(           "CURRICULUM_CD, ");
           }
           stb.append(" SUM(CREDITS) AS CREDITS "); // STUDYRECの単位マスタ単位合計
           stb.append(   "FROM    SUBCLASS_CREDIT ");
           stb.append(   "GROUP BY SUBCLASSCD ");
           if ("1".equals(param._useCurriculumcd)) {
               stb.append(           ", CLASSCD ");
               stb.append(           ", SCHOOL_KIND ");
               stb.append(           ", CURRICULUM_CD ");
           }
           stb.append(   ") ");

           //今年度の履修科目（講座名簿に登録されている科目）の表
           stb.append(",SUBCLASS_STD AS(");
           stb.append(   "SELECT  DISTINCT ");
           stb.append(           "STD.SCHREGNO, ");
           stb.append(           "REGD.ANNUAL, ");
           stb.append(           "CHR.SUBCLASSCD ");
           if ("1".equals(param._useCurriculumcd)) {
               stb.append(           ", CHR.CLASSCD ");
               stb.append(           ", CHR.SCHOOL_KIND ");
               stb.append(           ", CHR.CURRICULUM_CD ");
           }
           stb.append("   FROM    CHAIR_STD_DAT STD ");
           stb.append("           INNER JOIN SCHNO_A REGD ");
           stb.append("            ON STD.YEAR = REGD.YEAR ");
           stb.append("           AND STD.SCHREGNO = REGD.SCHREGNO ");
           stb.append("           INNER JOIN CHAIR_DAT CHR ");
           stb.append("            ON STD.YEAR = CHR.YEAR ");
           stb.append("           AND STD.SEMESTER = CHR.SEMESTER ");
           stb.append("           AND STD.CHAIRCD = CHR.CHAIRCD ");
           stb.append("   WHERE   STD.YEAR = '" + param._year + "' ");
           stb.append("       AND SUBSTR(CHR.SUBCLASSCD,1,2) <= '90' ");
           stb.append("       AND NOT EXISTS(SELECT 'X' FROM SUBCLASS_REPLACE_DAT R1 ");
           stb.append("                       WHERE R1.YEAR = STD.YEAR ");
           stb.append("                         AND R1.ANNUAL = REGD.GRADE ");
           if ("1".equals(param._useCurriculumcd)) {
               stb.append("                         AND R1.ATTEND_CLASSCD = CHR.CLASSCD ");
               stb.append("                         AND R1.ATTEND_SCHOOL_KIND = CHR.SCHOOL_KIND ");
               stb.append("                         AND R1.ATTEND_CURRICULUM_CD = CHR.CURRICULUM_CD ");
           }
           stb.append("                         AND R1.ATTEND_SUBCLASSCD = CHR.SUBCLASSCD) ");
           stb.append(   ") ");

           //履修済み科目の表
           stb.append(",STUDYREC AS(");
           stb.append(   "SELECT ");
           stb.append(           "'0' AS SCHOOLCD, "); // 本校
           stb.append(           "T1.ANNUAL, ");
           stb.append(           "T1.CLASSCD, ");
           if ("1".equals(param._useCurriculumcd)) {
               stb.append(           "T1.SCHOOL_KIND, ");
               stb.append(           "T1.CURRICULUM_CD, ");
           }
           stb.append(           "T1.SUBCLASSCD, ");
           stb.append(           "min(T1.CLASSABBV) AS CLASSNAME, ");
           if ("1".equals(param._knjd280UseForm2)) {
               stb.append(           "min(T1.SUBCLASSNAME) AS SUBCLASSNAME, ");
           } else {
               stb.append(           "min(T1.SUBCLASSABBV) AS SUBCLASSNAME, ");
           }
           stb.append(           "sum(T1.GET_CREDIT) AS GET_CREDIT, ");
           stb.append(           "sum(T1.ADD_CREDIT) AS ADD_CREDIT ");
           stb.append(   "FROM    SCHREG_STUDYREC_DAT T1 ");
           stb.append(          " INNER JOIN SCHNO_A REGD ");
           stb.append(          "     ON REGD.SCHREGNO = T1.SCHREGNO ");
           stb.append(          "    AND REGD.YEAR = '" + param._year + "' ");
           stb.append(   "WHERE   smallint(T1.ANNUAL) > 0 ");
           stb.append(     "AND   SUBSTR(T1.SUBCLASSCD,1,2) <= '90' ");
           stb.append(     "AND   NOT EXISTS(SELECT 'X' FROM SUBCLASS_REPLACE_DAT R1 ");
           stb.append(                      " WHERE R1.YEAR = T1.YEAR ");
           stb.append(                        " AND R1.ANNUAL = REGD.GRADE ");
           if ("1".equals(param._useCurriculumcd)) {
               stb.append(                        " AND R1.ATTEND_CLASSCD = T1.CLASSCD ");
               stb.append(                        " AND R1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
               stb.append(                        " AND R1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
           }
           stb.append(                        " AND R1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ");
           if (param._subChk != null) {
               //履修科目
               stb.append( "AND T1.YEAR <> '" + param._year + "' ");
           }
           stb.append(   "GROUP BY T1.SCHREGNO, T1.ANNUAL, T1.CLASSCD, T1.SUBCLASSCD ");
           if ("1".equals(param._useCurriculumcd)) {
               stb.append(           ", T1.SCHOOL_KIND ");
               stb.append(           ", T1.CURRICULUM_CD ");
           }
           stb.append("UNION ");
           stb.append(   "SELECT ");
           stb.append(           "'1' AS SCHOOLCD, "); // 前籍校
           stb.append(           "'0' AS ANNUAL, ");
           stb.append(           "T1.CLASSCD, ");
           if ("1".equals(param._useCurriculumcd)) {
               stb.append(           "T1.SCHOOL_KIND, ");
               stb.append(           "T1.CURRICULUM_CD, ");
           }
           stb.append(           "T1.SUBCLASSCD, ");
           stb.append(           "min(T1.CLASSABBV) AS CLASSNAME, ");
           if ("1".equals(param._knjd280UseForm2)) {
               stb.append(           "min(T1.SUBCLASSNAME) AS SUBCLASSNAME, ");
           } else {
               stb.append(           "min(T1.SUBCLASSABBV) AS SUBCLASSNAME, ");
           }
           stb.append(           "sum(T1.GET_CREDIT) AS GET_CREDIT, ");
           stb.append(           "sum(T1.ADD_CREDIT) AS ADD_CREDIT ");
           stb.append(   "FROM    SCHREG_STUDYREC_DAT T1 ");
           stb.append(          " INNER JOIN SCHNO_A REGD ");
           stb.append(          "     ON REGD.SCHREGNO = T1.SCHREGNO ");
           stb.append(          "    AND REGD.YEAR = '" + param._year + "' ");
           stb.append(   "WHERE   (smallint(T1.YEAR) = 0 AND smallint(T1.ANNUAL) = 0 OR T1.SCHOOLCD = '1') ");
           stb.append(     "AND   SUBSTR(T1.SUBCLASSCD,1,2) <= '90' ");
           stb.append(     "AND   NOT EXISTS(SELECT 'X' FROM SUBCLASS_REPLACE_DAT R1 ");
           stb.append(                      " WHERE R1.YEAR = T1.YEAR ");
           stb.append(                        " AND R1.ANNUAL = REGD.GRADE ");
           if ("1".equals(param._useCurriculumcd)) {
               stb.append(                        " AND R1.ATTEND_CLASSCD = T1.CLASSCD ");
               stb.append(                        " AND R1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
               stb.append(                        " AND R1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
           }
           stb.append(                        " AND R1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ");
           stb.append(   "GROUP BY T1.SCHREGNO, T1.ANNUAL, T1.CLASSCD, T1.SUBCLASSCD ");
           if ("1".equals(param._useCurriculumcd)) {
               stb.append(           ", T1.SCHOOL_KIND ");
               stb.append(           ", T1.CURRICULUM_CD ");
           }
           stb.append(   ") ");

           //メイン表
           stb.append(",STUDYREC2 AS(");
           stb.append("SELECT  ");
           stb.append("        T1.SCHOOLCD, ");
           stb.append("        T1.ANNUAL, T1.CLASSCD, ");
           if ("1".equals(param._useCurriculumcd)) {
               stb.append(           "T1.SCHOOL_KIND, ");
               stb.append(           "T1.CURRICULUM_CD, ");
               stb.append(           "T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD, ");
           } else {
               stb.append(           "T1.SUBCLASSCD, ");
           }
           stb.append(        "VALUE(SCM.ELECTDIV,'0') AS ELECTDIV, ");
           stb.append(        "VALUE(T1.CLASSNAME, ");
           if (param._hasANOTHER_CLASS_MST) {
               stb.append(        "      ACLM.CLASSABBV, ");
           }
           stb.append(        "      CLM.CLASSABBV) AS CLASSNAME, ");
           stb.append(        "VALUE(T1.SUBCLASSNAME, ");
           if (param._hasANOTHER_CLASS_MST) {
               if ("1".equals(param._knjd280UseForm2)) {
                   stb.append(        "ASCM.SUBCLASSNAME, ");
               } else {
                   stb.append(        "ASCM.SUBCLASSABBV, ");
               }
           }
           if ("1".equals(param._knjd280UseForm2)) {
               stb.append(        "SCM.SUBCLASSNAME ");
           } else {
               stb.append(        "SCM.SUBCLASSABBV ");
           }
           stb.append(        ") AS SUBCLASSNAME, ");
           stb.append(        "CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.ADD_CREDIT,0) + VALUE(T1.GET_CREDIT,0) ");
           stb.append(             "ELSE T1.GET_CREDIT END AS CREDITS, ");
           stb.append(        "TOT.CREDITS AS SUBCLASS_CREDITS, ");
           stb.append(        "CAST(NULL AS SMALLINT) AS RECORD_SCORE_CREDIT ");
           stb.append("FROM    STUDYREC T1 ");
           stb.append("LEFT JOIN CLASS_MST CLM ON CLM.CLASSCD = T1.CLASSCD ");
           if ("1".equals(param._useCurriculumcd)) {
               stb.append(           " AND CLM.SCHOOL_KIND = T1.SCHOOL_KIND ");
           }
           stb.append("LEFT JOIN SUBCLASS_MST SCM ON SCM.SUBCLASSCD = T1.SUBCLASSCD ");
           if ("1".equals(param._useCurriculumcd)) {
               stb.append(           " AND SCM.CLASSCD = T1.CLASSCD ");
               stb.append(           " AND SCM.SCHOOL_KIND = T1.SCHOOL_KIND ");
               stb.append(           " AND SCM.CURRICULUM_CD = T1.CURRICULUM_CD ");
           }
           if (param._hasANOTHER_CLASS_MST) {
               stb.append("LEFT JOIN ANOTHER_CLASS_MST ACLM ON ACLM.CLASSCD = T1.CLASSCD ");
               stb.append(           " AND ACLM.SCHOOL_KIND = T1.SCHOOL_KIND ");
               stb.append(           " AND T1.SCHOOLCD = '1' ");
           }
           if (param._hasANOTHER_SUBCLASS_MST) {
               stb.append("LEFT JOIN ANOTHER_SUBCLASS_MST ASCM ON ASCM.SUBCLASSCD = T1.SUBCLASSCD ");
               stb.append(           " AND ASCM.CLASSCD = T1.CLASSCD ");
               stb.append(           " AND ASCM.SCHOOL_KIND = T1.SCHOOL_KIND ");
               stb.append(           " AND ASCM.CURRICULUM_CD = T1.CURRICULUM_CD ");
               stb.append(           " AND T1.SCHOOLCD = '1' ");
           }
           stb.append("LEFT JOIN SUBCLASS_CREDIT_TOTAL TOT ON TOT.SUBCLASSCD = T1.SUBCLASSCD ");
           stb.append(           " AND T1.SCHOOLCD = '0' ");
           if ("1".equals(param._useCurriculumcd)) {
               stb.append(           " AND TOT.CLASSCD = T1.CLASSCD ");
               stb.append(           " AND TOT.SCHOOL_KIND = T1.SCHOOL_KIND ");
               stb.append(           " AND TOT.CURRICULUM_CD = T1.CURRICULUM_CD ");
           }
           stb.append(") ");

           stb.append(",RISHU AS(");
           stb.append("SELECT  ");
           stb.append("        '0' AS SCHOOLCD, ");
           stb.append("        T1.ANNUAL, SUBSTR(T1.SUBCLASSCD,1,2) AS CLASSCD, ");
           if ("1".equals(param._useCurriculumcd)) {
               stb.append(           "T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD, ");
           } else {
               stb.append(           "T1.SUBCLASSCD, ");
           }
           stb.append(        "VALUE(SCM.ELECTDIV,'0') AS ELECTDIV, ");

           stb.append(        "CLM.CLASSABBV AS CLASSNAME, ");
           if ("1".equals(param._knjd280UseForm2)) {
               stb.append(        "SCM.SUBCLASSNAME AS SUBCLASSNAME, ");
           } else {
               stb.append(        "SCM.SUBCLASSABBV AS SUBCLASSNAME, ");
           }
           stb.append(        "CASE WHEN T1.SUBCLASSCD IS NULL THEN NULL ELSE -1 END AS CREDITS, ");
           stb.append(        "TOT.CREDITS AS SUBCLASS_CREDITS, ");
           if ("1".equals(param._knjd280PrintRecordScoreCredit)) {
               stb.append(        "RSD.GET_CREDIT AS RECORD_SCORE_CREDIT ");
           } else {
               stb.append(        "CAST(NULL AS SMALLINT) AS RECORD_SCORE_CREDIT ");
           }
           stb.append("FROM    SUBCLASS_STD T1 ");
           stb.append("LEFT JOIN CLASS_MST CLM ON CLM.CLASSCD = SUBSTR(T1.SUBCLASSCD,1,2) ");
           if ("1".equals(param._useCurriculumcd)) {
               stb.append(        " AND CLM.SCHOOL_KIND = T1.SCHOOL_KIND ");
           }
           stb.append("LEFT JOIN SUBCLASS_MST SCM ON SCM.SUBCLASSCD = T1.SUBCLASSCD ");
           if ("1".equals(param._useCurriculumcd)) {
               stb.append(        " AND SCM.CLASSCD = T1.CLASSCD ");
               stb.append(        " AND SCM.SCHOOL_KIND = T1.SCHOOL_KIND ");
               stb.append(        " AND SCM.CURRICULUM_CD = T1.CURRICULUM_CD ");
           }
           stb.append("LEFT JOIN SUBCLASS_CREDIT_TOTAL TOT ON TOT.SUBCLASSCD = T1.SUBCLASSCD ");
           if ("1".equals(param._useCurriculumcd)) {
               stb.append(        " AND TOT.CLASSCD = T1.CLASSCD ");
               stb.append(        " AND TOT.SCHOOL_KIND = T1.SCHOOL_KIND ");
               stb.append(        " AND TOT.CURRICULUM_CD = T1.CURRICULUM_CD ");
           }
           if ("1".equals(param._knjd280PrintRecordScoreCredit)) {
               stb.append("LEFT JOIN V_RECORD_SCORE_HIST_DAT RSD ");
               stb.append(   "  ON RSD.YEAR = '" + param._year + "' ");
               stb.append(   " AND RSD.SEMESTER = '9' ");
               stb.append(   " AND RSD.TESTKINDCD = '99' ");
               stb.append(   " AND RSD.TESTITEMCD = '00' ");
               stb.append(   " AND RSD.SCORE_DIV = '09' ");
               stb.append(   " AND RSD.CLASSCD = T1.CLASSCD ");
               stb.append(   " AND RSD.SCHOOL_KIND = T1.SCHOOL_KIND ");
               stb.append(   " AND RSD.CURRICULUM_CD = T1.CURRICULUM_CD ");
               stb.append(   " AND RSD.SUBCLASSCD = T1.SUBCLASSCD ");
               stb.append(   " AND RSD.SCHREGNO = T1.SCHREGNO ");
           }
           stb.append(") ");

           // 資格
           stb.append(",QUALIFIED AS(");
           stb.append(" SELECT ");
           stb.append("     T1.YEAR, ");
           stb.append("     CASE WHEN T1.CONDITION_DIV = '2' THEN '1' ELSE '0' END AS SCHOOLCD, ");
           stb.append("     CASE WHEN T1.CONDITION_DIV = '2' THEN '0' ELSE VALUE(REGD.ANNUAL, '0') END AS ANNUAL, "); // 年次
           if ("1".equals(param._useCurriculumcd)) {
               stb.append("     T1.CLASSCD, ");
           } else {
               stb.append("     substr(T1.SUBCLASSCD, 1, 2) AS CLASSCD, ");
           }
           if ("1".equals(param._useCurriculumcd)) {
               stb.append(           "T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD, ");
           } else {
               stb.append(           "T1.SUBCLASSCD, ");
           }
           stb.append("     VALUE(SCM.ELECTDIV,'0') AS ELECTDIV, ");
           stb.append("     CLM.CLASSABBV AS CLASSNAME, ");
           if ("1".equals(param._knjd280UseForm2)) {
               stb.append(        "SCM.SUBCLASSNAME ");
           } else {
               stb.append(        "SCM.SUBCLASSABBV ");
           }
           stb.append(        " AS SUBCLASSNAME, ");
           stb.append("     CAST(NULL AS SMALLINT) AS CREDITS, ");
           stb.append("     CAST(NULL AS SMALLINT) AS SUBCLASS_CREDITS, ");
           stb.append("     SUM(T1.CREDITS) AS RECORD_SCORE_CREDIT ");
           stb.append(" FROM ");
           stb.append("     SCHREG_QUALIFIED_DAT T1 ");
           stb.append("     LEFT JOIN SCHNO_A REGD ");
           stb.append("         ON REGD.SCHREGNO = T1.SCHREGNO ");
           stb.append("        AND REGD.YEAR = T1.YEAR ");
           stb.append("     INNER JOIN CLASS_MST CLM ON CLM.CLASSCD = T1.CLASSCD ");
           if ("1".equals(param._useCurriculumcd)) {
               stb.append(           " AND CLM.SCHOOL_KIND = T1.SCHOOL_KIND ");
           }
           stb.append("     INNER JOIN SUBCLASS_MST SCM ON SCM.SUBCLASSCD = T1.SUBCLASSCD ");
           if ("1".equals(param._useCurriculumcd)) {
               stb.append(           " AND SCM.CLASSCD = T1.CLASSCD ");
               stb.append(           " AND SCM.SCHOOL_KIND = T1.SCHOOL_KIND ");
               stb.append(           " AND SCM.CURRICULUM_CD = T1.CURRICULUM_CD ");
           }
           stb.append(" WHERE ");
           stb.append("     T1.CONDITION_DIV IN ('1', '2', '3') "); // 1:増加単位認定、2:学校外認定(他校履修)、3:高等学校卒業程度認定単位
           stb.append("     AND T1.CREDITS IS NOT NULL ");
           stb.append("     AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCHNO_A) ");
           stb.append(" GROUP BY ");
           stb.append("     T1.YEAR, ");
           stb.append("     CASE WHEN CONDITION_DIV = '2' THEN '1' ELSE '0' END, ");
           stb.append("     CASE WHEN CONDITION_DIV = '2' THEN '0' ELSE VALUE(REGD.ANNUAL, '0') END, "); // 年次
           if ("1".equals(param._useCurriculumcd)) {
               stb.append("     T1.CLASSCD, ");
           } else {
               stb.append("     substr(T1.SUBCLASSCD, 1, 2), ");
           }
           if ("1".equals(param._useCurriculumcd)) {
               stb.append(           "T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD, ");
           } else {
               stb.append(           "T1.SUBCLASSCD, ");
           }
           stb.append("     VALUE(SCM.ELECTDIV,'0'), ");
           stb.append("     CLM.CLASSABBV, ");
           if ("1".equals(param._knjd280UseForm2)) {
               stb.append("        SCM.SUBCLASSNAME ");
           } else {
               stb.append("        SCM.SUBCLASSABBV ");
           }
           stb.append(") ");
           stb.append("SELECT ");
           stb.append("        T1.SCHOOLCD, ");
           stb.append("        T1.ANNUAL, T1.CLASSCD, ");
           stb.append("        T1.SUBCLASSCD, ");
           stb.append("        T1.ELECTDIV, ");
           stb.append("        T1.CLASSNAME, ");
           stb.append("        T1.SUBCLASSNAME, ");
           stb.append("        MAX(T1.CREDITS) AS CREDITS, ");
           stb.append("        SUM(T1.SUBCLASS_CREDITS) AS SUBCLASS_CREDITS, ");
           stb.append("        SUM(T1.RECORD_SCORE_CREDIT) AS RECORD_SCORE_CREDIT ");
           stb.append(" FROM (");
           stb.append("    SELECT ");
           stb.append("        T1.SCHOOLCD, ");
           stb.append("        T1.ANNUAL, T1.CLASSCD, ");
           stb.append("        T1.SUBCLASSCD, ");
           stb.append("        T1.ELECTDIV, ");
           stb.append("        T1.CLASSNAME, ");
           stb.append("        T1.SUBCLASSNAME, ");
           stb.append("        T1.CREDITS, ");
           stb.append("        T1.SUBCLASS_CREDITS, ");
           stb.append("        T1.RECORD_SCORE_CREDIT ");
           stb.append("    FROM STUDYREC2 T1 ");
           if (param._subChk != null) {
               stb.append("WHERE   NOT EXISTS(SELECT 'X' FROM SUBCLASS_STD T2 ");
               stb.append(                   " WHERE T2.ANNUAL = T1.ANNUAL AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
               if ("1".equals(param._useCurriculumcd)) {
                   stb.append(           " AND T2.CLASSCD = T1.CLASSCD ");
                   stb.append(           " AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                   stb.append(           " AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
               }
               stb.append(" ) ");
           }
           stb.append("    UNION ALL ");
           stb.append("    SELECT ");
           stb.append("        T1.SCHOOLCD, ");
           stb.append("        T1.ANNUAL, T1.CLASSCD, ");
           stb.append("        T1.SUBCLASSCD, ");
           stb.append("        T1.ELECTDIV, ");
           stb.append("        T1.CLASSNAME, ");
           stb.append("        T1.SUBCLASSNAME, ");
           stb.append("        T1.CREDITS, ");
           stb.append("        T1.SUBCLASS_CREDITS, ");
           stb.append("        T1.RECORD_SCORE_CREDIT ");
           stb.append(" FROM QUALIFIED T1 ");
           if (param._subChk != null) {
               //履修科目
               stb.append("  WHERE T1.YEAR <> '" + param._year + "' ");
           }
           stb.append(" ) T1 ");
           stb.append(" GROUP BY ");
           stb.append("        T1.SCHOOLCD, ");
           stb.append("        T1.ANNUAL, T1.CLASSCD, ");
           stb.append("        T1.SUBCLASSCD, ");
           stb.append("        T1.ELECTDIV, ");
           stb.append("        T1.CLASSNAME, ");
           stb.append("        T1.SUBCLASSNAME ");
           //履修科目
           if (param._subChk != null) {
               stb.append("UNION ");
               stb.append("SELECT ");
               stb.append("        T1.SCHOOLCD, ");
               stb.append("        T1.ANNUAL, T1.CLASSCD, ");
               stb.append("        T1.SUBCLASSCD, ");
               stb.append("        T1.ELECTDIV, ");
               stb.append("        T1.CLASSNAME, ");
               stb.append("        T1.SUBCLASSNAME, ");
               stb.append("        MAX(T1.CREDITS) AS CREDITS, ");
               stb.append("        SUM(T1.SUBCLASS_CREDITS) AS SUBCLASS_CREDITS, ");
               stb.append("        SUM(T1.RECORD_SCORE_CREDIT) AS RECORD_SCORE_CREDIT ");
               stb.append(" FROM (");
               stb.append("SELECT  ");
               stb.append("      T1.SCHOOLCD, ");
               stb.append("      T1.ANNUAL, ");
               stb.append("      T1.CLASSCD, ");
               stb.append("      T1.SUBCLASSCD, ");
               stb.append("      T1.ELECTDIV, ");
               stb.append("      T1.CLASSNAME, ");
               stb.append("      T1.SUBCLASSNAME, ");
               stb.append("      T1.CREDITS, ");
               stb.append("      T1.SUBCLASS_CREDITS, ");
               stb.append("      T1.RECORD_SCORE_CREDIT ");
               stb.append("FROM    RISHU T1 ");
               stb.append(" UNION ALL ");
               stb.append("SELECT ");
               stb.append("        T1.SCHOOLCD, ");
               stb.append("        T1.ANNUAL, T1.CLASSCD, ");
               stb.append("        T1.SUBCLASSCD, ");
               stb.append("        T1.ELECTDIV, ");
               stb.append("        T1.CLASSNAME, ");
               stb.append("        T1.SUBCLASSNAME, ");
               stb.append("        T1.CREDITS, ");
               stb.append("        T1.SUBCLASS_CREDITS, ");
               stb.append("        T1.RECORD_SCORE_CREDIT ");
               stb.append(" FROM QUALIFIED T1 ");
               stb.append( " WHERE T1.YEAR = '" + param._year + "' ");
               stb.append(" ) T1 ");
               stb.append(" GROUP BY ");
               stb.append("        T1.SCHOOLCD, ");
               stb.append("        T1.ANNUAL, T1.CLASSCD, ");
               stb.append("        T1.SUBCLASSCD, ");
               stb.append("        T1.ELECTDIV, ");
               stb.append("        T1.CLASSNAME, ");
               stb.append("        T1.SUBCLASSNAME ");
           }
           stb.append("ORDER BY CLASSCD, ELECTDIV, SUBCLASSCD, ANNUAL ");
           return stb.toString();
       }
    }

    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        KNJServletUtils.debugParam(request, log);
        return new Param(request, db2);
    }

    private static class Param {
        final String _year;
        final String _gakki;
        final String _gradeHrClass;
        final String[] _categorySelected;
        final String _date;
        final String _subChk;
        final String _useCurriculumcd;
        final String _knjd280UseForm2;
        final String _knjd280PrintRecordScoreCredit;
        final boolean _hasANOTHER_CLASS_MST;
        final boolean _hasANOTHER_SUBCLASS_MST;
        final boolean _isOutputDebug;
        private KNJDefineSchool _definecode;       //各学校における定数等設定

        private Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");  //年度
            _gakki = request.getParameter("GAKKI");  //現在学期
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");  //学年・組
            _categorySelected = request.getParameterValues("category_selected");       //学籍番号;
            _date = request.getParameter("DATE");   //作成日
            _subChk = request.getParameter("SUB_CHK"); //履修科目 on:出力する null:出力しない
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _knjd280UseForm2 = request.getParameter("knjd280UseForm2");
            _knjd280PrintRecordScoreCredit = request.getParameter("knjd280PrintRecordScoreCredit");
            _hasANOTHER_CLASS_MST = KnjDbUtils.setTableColumnCheck(db2, "ANOTHER_CLASS_MST", null);
            _hasANOTHER_SUBCLASS_MST = KnjDbUtils.setTableColumnCheck(db2, "ANOTHER_SUBCLASS_MST", null);

            try {
                _definecode = new KNJDefineSchool();
                _definecode.defineCode(db2, _year);      //各学校における定数等設定
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!", ex);
            }
            _isOutputDebug = "1".equals(KnjDbUtils.getDbPrginfoProperties(db2, "KNJD280", "outputDebug"));
        }
    }
}
