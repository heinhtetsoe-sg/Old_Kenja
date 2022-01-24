// kanji=漢字
/*
 * $Id: 1b66f25c5ebca836c1044b5d5fcee42c7b502575 $
 *
 * 作成日: 2005/06/16 11:40:00 - JST
 * 作成者: yamashiro
 *
 * Copyright(C) 2005-2007 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJD;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Semester;

/**
 *
 *  学校教育システム 賢者 [成績管理]  成績未入力講座一覧
 *
 *  2005/06/16 yamashiro
 *  2006/01/31 yamashiro Modify
 *    ○成績入力済みの条件は SCH_CHR_TESTのEXECUTED='1'
 *  @version $Id: 1b66f25c5ebca836c1044b5d5fcee42c7b502575 $
 */

public class KNJD041 {

    private static final Log log = LogFactory.getLog(KNJD041.class);
    private PrintWriter _outstrm;
    private String _printname;               //プリンタ名


    /**
     *
     *  KNJD.classから最初に起動されるクラス
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception IO例外
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス
        Param param;
        boolean nonedata = false;

        try {
            // パラメータの取得
            param = getParam(request);

            // print svf設定
            setSvfInit(response, svf);

            // ＤＢ接続
            db2 = setDb(request);
            if (openDb(db2)) {
                log.error("db open error");
                return;
            }

            // 印刷処理
            nonedata = printSvf(request, db2, svf, param);

            // 終了処理
            closeSvf(svf, nonedata);
        } finally {
            closeDb(db2);
            if (null != svf) {
                svf.close();
            }
            if (null != _outstrm) {
                _outstrm.close();
            }
        }

    }


    /**
     *  印刷処理
     */
    boolean printSvf(
            final HttpServletRequest request,
            final DB2UDB db2,
            final Vrw32alp svf,
            final Param param
    ) throws Exception {
        boolean nonedata = false;                                   //該当データなしフラグ
        final PreparedStatement[] arrps = new PreparedStatement[2];

        param.setRecordValue();                     //対象となる成績の項目(FIELD ON TABLE)
        setHead(db2, svf, param);                             //見出し出力のメソッド
        final String sqlPrestatementChair = prestatementChair(param);
        log.debug(" prestatementChair sql = " + sqlPrestatementChair);
        arrps[0] = db2.prepareStatement(sqlPrestatementChair);
        arrps[1] = db2.prepareStatement(prestatementHrclassname(param));
        if (printsvfDetail(db2, svf, param, arrps)) {
            nonedata = true;
        }

        return nonedata;
    }

    /**
     *  SVF-FORM 見出し設定
     */
    void setHead(final DB2UDB db2, final Vrw32alp svf, final Param param) {
        svf.VrSetForm("KNJD041.frm", 4);
        svf.VrsOut("NENDO",        nao_package.KenjaProperties.gengou(Integer.parseInt(param._year)) + "年度"); //年度
        svf.VrsOut("TESTNAME",     param.setTitle1(db2));
        svf.VrsOut("INFORMATION",  param.setTitle2());
        svf.VrsOut("ENFORCEMENT",  "実施日");

        //作成日(現在処理日)の取得
        final KNJ_Control control = new KNJ_Control();
        final KNJ_Control.ReturnVal returnval = control.Control(db2);
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(returnval.val3));             //作成日

        //学期名称の取得
        final KNJ_Semester semester = new KNJ_Semester();
        final KNJ_Semester.ReturnVal returnval2 = semester.Semester(db2, param._year, param._semester);
        svf.VrsOut("TERM",  returnval2.val1);                                      //学期名称

    }

    /**
     *  SVF-FORM  印刷処理
     */
    boolean printsvfDetail(
            final DB2UDB db2,
            final Vrw32alp svf,
            final Param param,
            final PreparedStatement[] arrps
    ) throws SQLException, NumberFormatException {
        boolean nonedata = false;
        final ResultSet rs = arrps[0].executeQuery();
        boolean amikake = false;

        while (rs.next()) {
            svf.VrsOut("COURSE",      rs.getString("CLASSNAME"));     //教科名
            svf.VrsOut("STAFFCD",     rs.getString("STAFFCD"));       //教科担当者コード
            svf.VrsOut("STAFFNAME",   rs.getString("STAFFNAME"));     //教科担当者名
            svf.VrsOut("CHAIRCD",     rs.getString("CHAIRCD"));       //講座コード

            amikake = (Integer.parseInt(rs.getString("ELECTDIV")) != 0) ? true : false;
            if (amikake) {
                svf.VrAttribute("SUBJECT", "Paint=(2,70,1),Bold=1");
            }
            svf.VrsOut("SUBJECT"     , rs.getString("SUBCLASSNAME")); //科目名
            if (amikake) {
                svf.VrAttribute("SUBJECT", "Paint=(0,0,0),Bold=0");
            }

            if (param._test.length() == 4  &&  param._test.substring(2, 4).equals("01")) {
                if (rs.getString("OPERATION_DATE") != null) {
                    svf.VrsOut("PERIOD1", KNJ_EditDate.h_format_thi(rs.getString("OPERATION_DATE"), 1));  //実施日
                }
            }
            //更新日で判断して処理日を出力
            if (rs.getString("EXECUTED").equals("1") && rs.getString("UP_DATE") != null) {
                svf.VrsOut("RECORD_DATE",  KNJ_EditDate.h_format_thi(rs.getString("UP_DATE"), 1));
                StringBuffer stb = new StringBuffer();
                stb.append(rs.getString("UP_TIME"));
                stb.setCharAt(2, ':');
                stb.setCharAt(5, ':');
                svf.VrsOut("RECORD_TIME", stb.toString());
            } else {
                svf.VrsOut("RECORD_DATE", "未入力");
            }

            printsvfHclassname(svf, rs.getString("CHAIRCD"), arrps[1]);    //受講ＨＲ編集

            svf.VrEndRecord();
            nonedata = true;
        }
        rs.close();

        return nonedata;
    }



    /**
     *   SVF-FORM 印刷処理 受講ＨＲ名を編集して出力
     *
     */
    private void printsvfHclassname(
            final Vrw32alp svf,
            final String chaircd,
            final PreparedStatement ps
    ) throws SQLException {
        int pp = 1;
        ps.setString(pp++, chaircd);  //講座コード
        final ResultSet rs = ps.executeQuery();
        String strx = null;
        boolean first = false;

        while (rs.next()) {
            if (!first) {
                strx = rs.getString("HR_NAMEABBV");
                first = true;
            } else {
                strx = strx + "," + rs.getString("HR_NAMEABBV");
            }
        }
        rs.close();
        if (first && strx != null) {
            svf.VrsOut("CLASS", strx);
        }
    }



    /**
     *   PrepareStatement作成
     *      テスト得点処理講座明細
     */
    //  CSOFF: MethodLength
    //  CSOFF: ExecutableStatementCount
    String prestatementChair(final Param param) {
        final StringBuffer stb = new StringBuffer();

        //講座の表
        stb.append("WITH CHAIR_A AS(");
        if (param._recordDiv == Param.RECORD_DIV_VALUE || param._recordDiv == Param.RECORD_DIV_SCORE && param._useRecordChkfinDat) {
            //テスト評価の場合
            stb.append("SELECT  T1.YEAR, MAX(T2.SEMESTER) AS SEMESTER, T1.CHAIRCD, MAX(EXECUTEDATE) AS OPERATION_DATE, VALUE(T1.EXECUTED,'0') AS EXECUTED,");
            stb.append("        MAX(DATE(T1.UPDATED)) AS UP_DATE, MAX(TIME(T1.UPDATED)) AS UP_TIME");
            stb.append(" FROM   RECORD_CHKFIN_DAT T1 ");
            stb.append("        ,CHAIR_DAT T2 ");
            stb.append(" WHERE  T1.YEAR = '" +  param._year + "' AND ");
            stb.append("        T1.SEMESTER = '" +  param._semester + "' AND ");
            stb.append("        T1.TESTKINDCD || T1.TESTITEMCD = '" +  param._test + "' AND ");
            stb.append("        T2.YEAR = '" +  param._year + "' AND ");
            if ("9".equals(param._semester)) {
                stb.append("    EXISTS (SELECT 'X' FROM CHAIR_DAT ");
                stb.append("            WHERE YEAR = '" +  param._year + "' AND CHAIRCD = T1.CHAIRCD ");
                stb.append("            HAVING MAX(SEMESTER) = T2.SEMESTER) AND ");
            } else {
                stb.append("    T2.SEMESTER = '" +  param._semester + "' AND ");
            }
            stb.append("        T2.CHAIRCD = T1.CHAIRCD AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        T2.CLASSCD || '-' || T2.SCHOOL_KIND IN " + param._classCd + " AND ");
            } else {
                stb.append("        SUBSTR(T2.SUBCLASSCD,1,2) IN " + param._classCd + " AND ");
            }
            stb.append("        T1.RECORD_DIV = '" + param._recordDiv + "' ");
        } else {
            //テスト得点の場合
            stb.append("SELECT  T1.YEAR, T1.SEMESTER, T1.CHAIRCD, MAX(EXECUTEDATE) AS OPERATION_DATE,VALUE(T1.EXECUTED,'0') AS EXECUTED,");
            stb.append("        MAX(DATE(T1.UPDATED)) AS UP_DATE, MAX(TIME(T1.UPDATED)) AS UP_TIME");
            stb.append(" FROM   SCH_CHR_TEST T1 ");
            stb.append("        ,CHAIR_DAT T2 ");
            stb.append(" WHERE  T1.YEAR = '" +  param._year + "' AND ");
            stb.append("        T1.SEMESTER = '" +  param._semester + "' AND ");
            stb.append("        T1.TESTKINDCD || T1.TESTITEMCD = '" +  param._test + "' AND ");
            stb.append("        T2.YEAR = '" +  param._year + "' AND ");
            stb.append("        T2.SEMESTER = '" +  param._semester + "' AND ");
            stb.append("        T2.CHAIRCD = T1.CHAIRCD AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        T2.CLASSCD || '-' || T2.SCHOOL_KIND IN " + param._classCd + " ");
            } else {
                stb.append("        SUBSTR(T2.SUBCLASSCD,1,2) IN " + param._classCd + " ");
            }
        }
        if (param._output.equals("1")) {
            stb.append("AND VALUE(T1.EXECUTED,'0') <> '1' ");   //06/01/31ADD
        }
        stb.append("GROUP BY T1.YEAR, T1.SEMESTER, T1.CHAIRCD,T1.EXECUTED ");
        stb.append(") ");

        //メインの表
        stb.append("SELECT  T1.YEAR, T1.SEMESTER, T1.CLASSCD, T1.CLASSNAME, T1.GROUPCD, T1.GROUPABBV, ");
        stb.append("    T1.SUBCLASSCD, T1.SUBCLASSNAME, T1.CHAIRCD, T1.ELECTDIV, ");
        stb.append("    T2.STAFFCD, T2.STAFFNAME, ");
        stb.append("    T4.OPERATION_DATE, ");
        stb.append("    T4.UP_DATE, T4.UP_TIME, T4.EXECUTED ");
        stb.append(" FROM    CHAIR_A T4 ");

        //講座の表
        stb.append("    INNER JOIN(");
        stb.append("         SELECT  W1.YEAR, W1.SEMESTER, SUBSTR(W1.SUBCLASSCD,1,2) AS CLASSCD, W2.CLASSNAME, W1.GROUPCD, W4.GROUPABBV, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("    W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
        }
        stb.append("                 W1.SUBCLASSCD AS SUBCLASSCD, W3.SUBCLASSNAME, W1.CHAIRCD, VALUE(W3.ELECTDIV,'0') AS ELECTDIV ");
        stb.append("         FROM    CHAIR_DAT W1 ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("                 INNER JOIN CLASS_MST W2 ON W2.CLASSCD = W1.CLASSCD ");
            stb.append("                     AND W2.SCHOOL_KIND = W1.SCHOOL_KIND ");
            stb.append("                 INNER JOIN SUBCLASS_MST W3 ON W3.SUBCLASSCD = W1.SUBCLASSCD ");
            stb.append("                     AND W3.CLASSCD = W1.CLASSCD ");
            stb.append("                     AND W3.SCHOOL_KIND = W1.SCHOOL_KIND ");
            stb.append("                     AND W3.CURRICULUM_CD = W1.CURRICULUM_CD ");
        } else  {
            stb.append("                 INNER JOIN CLASS_MST W2 ON W2.CLASSCD = SUBSTR(W1.SUBCLASSCD,1,2) ");
            stb.append("                 INNER JOIN SUBCLASS_MST W3 ON W3.SUBCLASSCD = W1.SUBCLASSCD ");
        }
        stb.append("                 LEFT JOIN V_ELECTCLASS_MST W4 ON W4.YEAR = W1.YEAR AND W4.GROUPCD = W1.GROUPCD ");
        stb.append("         WHERE   W1.YEAR = '" + param._year + "' AND ");
        if (!"9".equals(param._semester)) {
            stb.append("             W1.SEMESTER = '" + param._semester + "' AND ");
        }
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("                 W1.CLASSCD || '-' || W1.SCHOOL_KIND IN " + param._classCd);
        } else {
            stb.append("                 SUBSTR(W1.SUBCLASSCD,1,2) IN " + param._classCd);
        }
        stb.append("         )T1 ON T1.CHAIRCD = T4.CHAIRCD AND T1.YEAR = T4.YEAR ");
        stb.append("          AND T1.SEMESTER = T4.SEMESTER");
        //講座担任の表
        stb.append("    INNER JOIN (");
        stb.append("         SELECT  W1.YEAR, W1.SEMESTER, W1.CHAIRCD,W1.STAFFCD,W2.STAFFNAME ");
        stb.append("         FROM    CHAIR_STF_DAT W1 ");
        stb.append("                 INNER JOIN STAFF_MST W2 ON W2.STAFFCD = W1.STAFFCD ");
        stb.append("         WHERE   W1.YEAR = '" + param._year + "' ");
        if (!"9".equals(param._semester)) {
            stb.append("         AND W1.SEMESTER = '" + param._semester + "' ");
        }
        stb.append("         )T2 ON T2.CHAIRCD = T4.CHAIRCD AND T2.YEAR = T4.YEAR");
        stb.append("          AND T2.SEMESTER = T4.SEMESTER ");

        //未入力のみの場合
        //if( Integer.parseInt( param[3] ) == 1 )
        //    stb.append("WHERE T3.UPDATED IS NULL ");

        stb.append("ORDER BY T1.CLASSCD,T2.STAFFCD,T1.GROUPCD,T1.SUBCLASSCD,T1.CHAIRCD");

        return stb.toString();
    }
    //  CSON: ExecutableStatementCount
    //  CSON: MethodLength

    /**
     *   PrepareStatement作成 受講ＨＲの表
     *
     */
    String prestatementHrclassname(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT  CHAIRCD, TRGTGRADE, TRGTCLASS, HR_NAMEABBV");
        stb.append(" FROM (  SELECT  K2.CHAIRCD, K1.TRGTGRADE, K1.TRGTCLASS");
        stb.append("         FROM    CHAIR_CLS_DAT K1,CHAIR_DAT K2");
        stb.append("         WHERE   K1.YEAR = '" +  param._year + "' AND");
        if (!"9".equals(param._semester)) {
            stb.append("                 K1.SEMESTER = '" +  param._semester + "' AND");
        }
        stb.append("                 K2.YEAR = '" +  param._year + "' AND");
        stb.append("                 K2.SEMESTER = '" +  param._semester + "' AND");
        stb.append("                 (K1.CHAIRCD = '0000000' OR K1.CHAIRCD = K2.CHAIRCD) AND");
        stb.append("                 K1.GROUPCD = K2.GROUPCD");
        stb.append("      )W1,");
        stb.append("      SCHREG_REGD_HDAT W2");
        stb.append(" WHERE W1.CHAIRCD = ? AND");
        stb.append("       W1.TRGTGRADE = W2.GRADE AND");
        stb.append("       W1.TRGTCLASS = W2.HR_CLASS AND");
        stb.append("       W2.YEAR = '" +  param._year + "'");
        if (!"9".equals(param._semester)) {
            stb.append("       AND W2.SEMESTER = '" +  param._semester + "'");
        }
        stb.append(" ORDER BY TRGTGRADE, TRGTCLASS");

        return stb.toString();
    }


    /**
     *  get parameter doGet()パラメータ受け取り
     */
    private Param getParam(final HttpServletRequest request) {
        Enumeration paramEnumeration = request.getParameterNames();
        while (paramEnumeration.hasMoreElements()) {
            String parameter = (String) paramEnumeration.nextElement();
            log.debug("[" + parameter + "] = " + request.getParameter(parameter));
        }

        if (request.getParameter("PRINTNAME") != null) {
            _printname = request.getParameter("PRINTNAME"); //プリンタ名
        }
        final String year = request.getParameter("YEAR");            //年度
        final String semester = request.getParameter("GAKKI");           //学期
        final String test = request.getParameter("TEST");            //テスト種別
        final String output =  (request.getParameter("OUTPUT") != null) ?
                request.getParameter("OUTPUT") : "";  // 1=>未入力のみ 2=>全て
        final String[] classCds = request.getParameterValues("CLASS_SELECTED");    // 学籍番号
        final String countFlg = request.getParameter("COUNTFLG");
        final int recordDiv = (request.getParameter("RECORD_DIV") != null) ?
                Integer.parseInt(request.getParameter("RECORD_DIV")) : Param.RECORD_DIV_SCORE; // 素点/評価 種別
        final String useCurriculumcd = request.getParameter("useCurriculumcd");
        final boolean useRecordChkfinDat = "1".equals(request.getParameter("useRecordChkfinDat"));

        Param param = new Param(year,
                semester,
                test,
                output,
                classCds,
                countFlg,
                recordDiv,
                useCurriculumcd,
                useRecordChkfinDat);

        return param;
    }


    /** print設定 */
    private void setSvfInit(final HttpServletResponse response, final Vrw32alp svf) throws IOException {

        _outstrm = new PrintWriter(response.getOutputStream());
        if (_printname != null) {
            response.setContentType("text/html");
        } else {
            response.setContentType("application/pdf");
        }

        int ret = svf.VrInit();                             //クラスの初期化

        if (_printname != null) {
            ret = svf.VrSetPrinter("", _printname);          //プリンタ名の設定
            if (ret < 0) {
                log.info("printname ret = " + ret);
            }
        } else {
            ret = svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定
        }
    }


    /** svf close */
    private void closeSvf(final Vrw32alp svf, final boolean nonedata) {
        int ret = 0;
        if (_printname != null) {
            _outstrm.println("<HTML>");
            _outstrm.println("<HEAD>");
            _outstrm.println("<META http-equiv=\"Content-Type\" content=\"text/html; charset=euc-jp\">");
            _outstrm.println("</HEAD>");
            _outstrm.println("<BODY>");
            if (!nonedata) {
                _outstrm.println("<H1>対象データはありません。</h1>");
            } else {
                _outstrm.println("<H1>印刷しました。</h1>");
            }
            _outstrm.println("</BODY>");
            _outstrm.println("</HTML>");
        } else if (!nonedata) {
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndPage();
        }
        ret = svf.VrQuit();
        if (ret == 0) {
            log.info("===> VrQuit():" + ret);
        }
        _outstrm.close();            //ストリームを閉じる
    }


    /** DB set */
    private DB2UDB setDb(final HttpServletRequest request) throws ServletException, IOException {
        DB2UDB db2 = null;
        db2 = new DB2UDB(request.getParameter("DBNAME") , "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
        return db2;
    }

    /** DB open */
    private boolean openDb(final DB2UDB db2) throws Exception {
        db2.open();

        return false;
    }

    /** DB close */
    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private class Param {
        final String _year;
        final String _semester;
        final String _test;
        final String _output;
        final String _classCd;
        String _recordValue;
        final String _countFlg;
        final int _recordDiv;
        
        final static int RECORD_DIV_SCORE = 1; // 素点
        final static int RECORD_DIV_VALUE = 2; // 評価
        
        final String _useCurriculumcd;
        final boolean _useRecordChkfinDat;
        
        Param(final String year,
                final String semester,
                final String test,
                final String output,
                final String[] classCd,
                final String countFlg,
                final int recordVal,
                final String useCurriculumcd,
                final boolean useRecordChkfinDat) {
            _year = year;
            _semester = semester;
            _test = test;
            _output = output;
            _classCd = setClasscd(classCd);
            _countFlg = countFlg;
            _recordDiv = recordVal;
            _useCurriculumcd = useCurriculumcd;
            _useRecordChkfinDat = useRecordChkfinDat;
        }

        /**
         *  対象教科コードをＳＱＬ用に編集
         */
        private String setClasscd(final String[] classcd) {
            StringBuffer stb = new StringBuffer();
            stb.append("(");
            for (int i = 0; i < classcd.length; i++) {
                if (0 < i) {
                    stb.append(",");
                }
                stb.append("'").append(classcd[i]).append("'");
            }
            stb.append(")");
            return stb.toString();
        }
        
        /**
         *  TITLE設定
         */
        String setTitle1(final DB2UDB db2) {
            String retval = "";
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     " + _countFlg + " T1 ");
            stb.append(" WHERE ");
            stb.append("     YEAR='" + _year + "' ");
            stb.append("     AND TESTKINDCD || TESTITEMCD = '" + _test + "' ");
            if ("TESTITEM_MST_COUNTFLG_NEW".equals(_countFlg)) {
                stb.append("     AND SEMESTER = '" + _semester + "' ");
            }
            try {
                PreparedStatement ps = db2.prepareStatement(stb.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    retval = (rs.getString("TESTITEMNAME") != null) ? rs.getString("TESTITEMNAME") : "";
                }
                rs.close();
                ps.close();
                db2.commit();
            }catch (SQLException ex) {
                log.debug("SQLException",ex);
            }

            return retval;
        }

        /**
         *  TITLE設定
         */
        String setTitle2() {
            return (Integer.parseInt(_output) == 1) ? "未入力" : "全て";
        }
        
        /**
         *  対象成績設定
         */
        void setRecordValue() {
            String recordValue = null;
            if (!_semester.equals("9")) {
                recordValue = (_test.equals("0101")) ? "SEM" + _semester + "_INTR_SCORE"
                        : (_test.equals("0201")) ? "SEM" + _semester + "_TERM_SCORE"
                                : (_test.equals("0100")) ? "SEM" + _semester + "_INTR_VALUE"
                                        : (_test.equals("0200")) ? "SEM" + _semester + "_TERM_VALUE"
                                                : "SEM" + _semester + "_VALUE";
            } else {
                recordValue = "GRAD_VALUE";
            }

            _recordValue = recordValue;
        }
    }
}
