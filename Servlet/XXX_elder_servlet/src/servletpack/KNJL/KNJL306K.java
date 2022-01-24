// kanji=漢字
/*
 * $Id: 689f0bbe6bd5a42424181e89588d6b99a8d53624 $
 *
 * 作成日: 2006/11/16
 * 作成者: m-yama
 *
 * Copyright(C) 2006-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 *
 * 2006/12/05 m-yama   NO001 塾/学校受付番号でリンクしていた箇所を受付番号でリンクするよう修正
 */
package servletpack.KNJL;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *   学校教育システム 賢者 [入試]  入試事前相談データ重複チェックリスト2
 *
 *  2006/11/16 m-yama 作成日
 *  @version $Id: 689f0bbe6bd5a42424181e89588d6b99a8d53624 $
 *
 */
public class KNJL306K {

    private static final Log log = LogFactory.getLog(KNJL306K.class);

    final Map _hmparam = new HashMap();

    boolean nonedata = false;
    private String setacce[];           //セットデータ配列
    private String setschl[];           //セットデータ配列
    private String setpri[];            //セットデータ配列
    private String setdate[];           //セットデータ配列
    private String setname[];           //セットデータ配列
    private String setkana[];           //セットデータ配列
    private String setsex[];            //セットデータ配列
    private String setfsmj[];           //セットデータ配列
    private String setfsjd[];           //セットデータ配列
    private String setpsmj[];           //セットデータ配列
    private String setpsjd[];           //セットデータ配列
    private String setvalu[];           //セットデータ配列

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        final Vrw32alp svf = new Vrw32alp();    //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        final PrintWriter outstrm = new PrintWriter(response.getOutputStream());

        try {
            //  print設定
            response.setContentType("application/pdf");

            //  svf設定
            svf.VrInit(); //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定

            //  ＤＢ接続
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            //  パラメータの取得
            setParam(request, db2);
            //  SQLパラメータ作成
            setParamSql();
            //ヘッダデータ設定
            setHead(db2);
            for (final Iterator it = _hmparam.keySet().iterator(); it.hasNext();) {
                final String key = (String) it.next();
                log.debug(key + " = " + _hmparam.get(key));
            }

            //SVF出力
            printMain(db2, svf);

            //  該当データ無し
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "");
                svf.VrEndPage();
            }
        } finally {
            if (null != svf) {
                svf.VrQuit();
            }
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            if (null != outstrm) {
                outstrm.close();
            }
        }

    }

    /** パラメータ設定 */
    private void setParam(
            final HttpServletRequest request,
            final DB2UDB db2
    ) throws Exception {

        _hmparam.put("YEAR", request.getParameter("YEAR"));         //年度
        _hmparam.put("TESTDIV", request.getParameter("TESTDIV"));   //試験区分
        _hmparam.put("JHFLG", request.getParameter("JHFLG"));       //中高区分
        _hmparam.put("OUTPUT", request.getParameter("OUTPUT"));     //帳票種別
    }


    private void setParamSql() {
        if (_hmparam.get("OUTPUT").equals("on")) {
            //漢字氏名○、かな氏名×
            setSubtitle("○", "×", "−");
            setWhereSql("VALUE(NAME,'') AS NAME1", "VALUE(NAME,'')", "T1.NAME1",
                    "VALUE(T2.NAME2,'') = VALUE(T1.NAME1,'') AND VALUE(T2.KANA2,'') != VALUE(L1.NAME_KANA,'')",
                    "VALUE(T2.NAME2,'') = VALUE(T1.NAME1,'')");

        } else if (_hmparam.get("OUTPUT").equals("no")) {
            //漢字氏名×、かな氏名○
            setSubtitle("×", "○", "−");
            setWhereSql("VALUE(NAME_KANA,'') AS KANA1", "VALUE(NAME_KANA,'')", "T1.KANA1", 
                    "VALUE(T2.KANA2,'') = VALUE(T1.KANA1,'') AND VALUE(T2.NAME2,'') != VALUE(L1.NAME,'')",
                    "VALUE(T2.KANA2,'') = VALUE(T1.KANA1,'')");

        } else if (_hmparam.get("OUTPUT").equals("oo")) {
            setSubtitle("○", "○", "−");
            //漢字氏名○、かな氏名○
            setWhereSql("VALUE(NAME,'') AS NAME1,VALUE(NAME_KANA,'') AS KANA1", "VALUE(NAME,''),VALUE(NAME_KANA,'')",
                    "T1.NAME1, T1.KANA1",
                    "VALUE(T2.NAME2,'') = VALUE(T1.NAME1,'') AND VALUE(T2.KANA2,'') = VALUE(T1.KANA1,'')",
                    "VALUE(T2.NAME2,'') = VALUE(T1.NAME1,'') AND VALUE(T2.KANA2,'') = VALUE(T1.KANA1,'')");

        } else if (_hmparam.get("OUTPUT").equals("ooo")) {
            //漢字氏名○、かな氏名○、出身学校○
            setSubtitle("○", "○", "○");
            setWhereSql("VALUE(NAME,'') AS NAME1,VALUE(NAME_KANA,'') AS KANA1,VALUE(FS_CD,'') AS FS_CD1",
                    "VALUE(NAME,''),VALUE(NAME_KANA,''),VALUE(FS_CD,'')", "T1.NAME1, T1.KANA1, T1.FS_CD1",
                    "VALUE(T2.NAME2,'') = VALUE(T1.NAME1,'') AND VALUE(T2.KANA2,'') = VALUE(T1.KANA1,'') AND VALUE(T2.FS_CD2,'') = VALUE(T1.FS_CD1,'')",
                    "VALUE(T2.NAME2,'') = VALUE(T1.NAME1,'') AND VALUE(T2.KANA2,'') = VALUE(T1.KANA1,'') AND VALUE(T2.FS_CD2,'') = VALUE(T1.FS_CD1,'')");

        } else if (_hmparam.get("OUTPUT").equals("oon")) {
            //漢字氏名○、かな氏名○、出身学校×
            setSubtitle("○", "○", "×");
            setWhereSql("VALUE(NAME,'') AS NAME1,VALUE(NAME_KANA,'') AS KANA1", "VALUE(NAME,''),VALUE(NAME_KANA,'')",
                    "T1.NAME1, T1.KANA1",
                    "VALUE(T2.NAME2,'') = VALUE(T1.NAME1,'') AND VALUE(T2.KANA2,'') = VALUE(T1.KANA1,'') AND VALUE(T2.FS_CD2,'') != VALUE(L1.FS_CD,'')",
                    "VALUE(T2.NAME2,'') = VALUE(T1.NAME1,'') AND VALUE(T2.KANA2,'') = VALUE(T1.KANA1,'')");

        } else if (_hmparam.get("OUTPUT").equals("ono")) {
            //漢字氏名○、かな氏名×、出身学校○
            setSubtitle("○", "×", "○");
            setWhereSql("VALUE(NAME,'') AS NAME1,VALUE(FS_CD,'') AS FS_CD1", "VALUE(NAME,''),VALUE(FS_CD,'')",
                    "T1.NAME1, T1.FS_CD1",
                    "VALUE(T2.NAME2,'') = VALUE(T1.NAME1,'') AND VALUE(T2.KANA2,'') != VALUE(L1.NAME_KANA,'') AND VALUE(T2.FS_CD2,'') = VALUE(T1.FS_CD1,'')",
                    "VALUE(T2.NAME2,'') = VALUE(T1.NAME1,'') AND VALUE(T2.FS_CD2,'') = VALUE(T1.FS_CD1,'')");

        } else if (_hmparam.get("OUTPUT").equals("onn")) {
            //漢字氏名○、かな氏名×、出身学校×
            setSubtitle("○", "×", "×");
            setWhereSql("VALUE(NAME,'') AS NAME1", "VALUE(NAME,'')", "T1.NAME1",
                    "VALUE(T2.NAME2,'') = VALUE(T1.NAME1,'') AND VALUE(T2.KANA2,'') != VALUE(L1.NAME_KANA,'') AND VALUE(T2.FS_CD2,'') != VALUE(L1.FS_CD,'')",
                    "VALUE(T2.NAME2,'') = VALUE(T1.NAME1,'')");

        } else if (_hmparam.get("OUTPUT").equals("noo")) {
            //漢字氏名×、かな氏名○、出身学校○
            setSubtitle("×", "○", "○");
            setWhereSql("VALUE(NAME_KANA,'') AS KANA1,VALUE(FS_CD,'') AS FS_CD1", "VALUE(NAME_KANA,''),VALUE(FS_CD,'')",
                    "T1.KANA1, T1.FS_CD1",
                    "VALUE(T2.NAME2,'') != VALUE(L1.NAME,'') AND VALUE(T2.KANA2,'') = VALUE(T1.KANA1,'') AND VALUE(T2.FS_CD2,'') = VALUE(T1.FS_CD1,'')",
                    "VALUE(T2.KANA2,'') = VALUE(T1.KANA1,'') AND VALUE(T2.FS_CD2,'') = VALUE(T1.FS_CD1,'')");

        } else if (_hmparam.get("OUTPUT").equals("non")) {
            //漢字氏名×、かな氏名○、出身学校×
            setSubtitle("×", "○", "×");
            setWhereSql("VALUE(NAME_KANA,'') AS KANA1", "VALUE(NAME_KANA,'')", "T1.KANA1",
                    "VALUE(T2.NAME2,'') != VALUE(L1.NAME,'') AND VALUE(T2.KANA2,'') = VALUE(T1.KANA1,'') AND VALUE(T2.FS_CD2,'') != VALUE(L1.FS_CD,'')",
                    "VALUE(T2.KANA2,'') = VALUE(T1.KANA1,'')");

        } else if (_hmparam.get("OUTPUT").equals("nno")) {
            //漢字氏名×、かな氏名×、出身学校○
            setSubtitle("×", "×", "○");
            setWhereSql("VALUE(FS_CD,'') AS FS_CD1", "VALUE(FS_CD,'')", "T1.FS_CD1",
                    "VALUE(T2.NAME2,'') != VALUE(L1.NAME,'') AND VALUE(T2.KANA2,'') != VALUE(L1.NAME_KANA,'') AND VALUE(T2.FS_CD2,'') = VALUE(T1.FS_CD1,'')",
                    "VALUE(T2.FS_CD2,'') = VALUE(T1.FS_CD1,'')");
        }
    }

    //サブタイトル設定
    private void setSubtitle(final String simei, final String kanasimei, final String school) {
        _hmparam.put("SIMEI", simei);
        _hmparam.put("KANASIMEI", kanasimei);
        _hmparam.put("SCHOOL", school);
    }

    //抽出条件設定
    private void setWhereSql(
            final String main_t_select,
            final String main_t_group,
            final String select_t_select,
            final String select_t_where,
            final String select_join
    ) {
        _hmparam.put("MAIN_T_SELECT", main_t_select);
        _hmparam.put("MAIN_T_GROUP", main_t_group);
        _hmparam.put("SELECT_T_SELECT", select_t_select);
        _hmparam.put("SELECT_T_WHERE", select_t_where);
        _hmparam.put("SELECT_JOIN", select_join);
    }

    /** ヘッダデータ設定 **/
    private void setHead(final DB2UDB db2) throws Exception
    {
        //タイトル年度
        _hmparam.put("NENDO", nao_package.KenjaProperties.gengou(Integer.parseInt((String) _hmparam.get("YEAR"))) + "年度");
        //  学校名
        if (_hmparam.get("JHFLG").equals("1")) {
            _hmparam.put("SCHOOLNAME", "中学校");
        } else {
            _hmparam.put("SCHOOLNAME", "高等学校");
        }

        //  作成日(現在処理日)の取得
        final String sql = "VALUES RTRIM(CHAR(DATE(SYSDATE()))),RTRIM(CHAR(HOUR(SYSDATE()))),RTRIM(CHAR(MINUTE(SYSDATE())))";
        db2.query(sql);
        final ResultSet daters = db2.getResultSet();
        try {
            final String[] arr_ctrl_date = new String[3];
            int number = 0;
            while( daters.next() ){
                arr_ctrl_date[number] = daters.getString(1);
                number++;
            }
            _hmparam.put("DATE", KNJ_EditDate.h_format_JP(arr_ctrl_date[0])+arr_ctrl_date[1]+"時"+arr_ctrl_date[2]+"分"+" 現在");
        } finally {
            DbUtils.close(daters);
        }

        //  試験区分の取得
        if (!_hmparam.get("TESTDIV").equals("99")){
            final String tDivSql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '" + _hmparam.get("TESTDIV") + "'";
            db2.query(tDivSql);
            final ResultSet tDivrs = db2.getResultSet();
            try {
                while( tDivrs.next() ){
                    _hmparam.put("TESTDIVNAME", tDivrs.getString("NAME1"));
                }
            } finally {
                DbUtils.close(tDivrs);
            }
        } else {
            _hmparam.put("TESTDIVNAME", "");
        }

    }

    /** 印刷メイン **/
    private void printMain(
            DB2UDB db2,
            Vrw32alp svf
    ) throws Exception {
        svf.VrSetForm("KNJL306.frm", 4);
        int gyo = 1;
        PreparedStatement mainps = null;
        mainps = db2.prepareStatement(mainSql());
        final ResultSet mainrs = mainps.executeQuery();
        try {
            while( mainrs.next() ){
                //51行目は空行印字
                if (gyo > 50) {
                    svf.VrAttribute("TOTAL_MEMBER","Meido=100");
                    svf.VrsOut("TOTAL_MEMBER"       , String.valueOf("空"));    //空行
                    svf.VrEndRecord();
                    gyo = 1;
                }
                //ヘッダ印字
                printHeader(svf);
                //明細印字
                printdata(svf, mainrs);
                nonedata = true;
                gyo++;
            }

        } finally {
            DbUtils.close(mainrs);
            DbUtils.close(mainps);
        }

    }

    /** ヘッダーデータをセット */
    private void printHeader(final Vrw32alp svf)
    {
        svf.VrsOut("NENDO", (String) _hmparam.get("NENDO"));
        svf.VrsOut("SCHOOLDIV", (String) _hmparam.get("SCHOOLNAME"));
        svf.VrsOut("TESTDIV", (String) _hmparam.get("TESTDIVNAME"));
        svf.VrsOut("SIMEI", (String) _hmparam.get("SIMEI"));
        svf.VrsOut("KANASIMEI", (String) _hmparam.get("KANASIMEI"));
        svf.VrsOut("SCHOOL", (String) _hmparam.get("SCHOOL"));
        svf.VrsOut("DATE", (String) _hmparam.get("DATE"));
    }

    /** 明細データ出力 */
    private void printdata(final Vrw32alp svf, final ResultSet rs) throws SQLException
    {
        String len1 = "0";
        String len2 = "0";
        //明細出力
        svf.VrsOut("NUMBER", rs.getString("LINE_NUMBER"));
        svf.VrsOut("ACCEPTNO", rs.getString("ACCEPTNO1"));
        svf.VrsOut("FINSCHOOLNAME", rs.getString("FINSCHOOL_NAME"));
        svf.VrsOut("PRISCHOOLNAME", rs.getString("PRISCHOOL_NAME"));
        svf.VrsOut("UPDATE", rs.getString("CREATE_DATE"));
        //氏名出力フィールド設定
        if (null != rs.getString("NAME1")) {
            len1 = (10 < (rs.getString("NAME1")).length()) ? "2" : "1" ;
        } else {
            len1 = "1" ;
        }
        svf.VrsOut("NAME"+len1, rs.getString("NAME1"));
        //かな氏名出力フィールド設定
        if (null != rs.getString("KANA1")) {
            len2 = (10 < (rs.getString("KANA1")).length()) ? "2" : "1" ;
        } else {
            len2 = "1" ;
        }
        svf.VrsOut("KANA"+len2, rs.getString("KANA1"));
        svf.VrsOut("SEX", rs.getString("SEX"));
        svf.VrsOut("ORG_MAJOR1", rs.getString("FSJUDG"));
        svf.VrsOut("ORG_MAJOR2", rs.getString("PSJUDG"));
        svf.VrEndRecord();

    }

    /** メインSQL作成 **/
    private String mainSql()
    {
        final String judgeSql = priorJudgmentSql();
        StringBuffer stb = new StringBuffer();
        try {
            //学校データ
            stb.append("WITH FSDATA AS ( ");
            stb.append("SELECT ");
            stb.append("    ACCEPTNO,DATADIV,SHDIV,WISHNO, ");
            stb.append("    n2.ABBV1 AS SHNAME,n1.NAME1 AS JUDG,EXAMCOURSE_MARK AS MAJOR ");
            stb.append("FROM ");
            stb.append("    ENTEXAM_CONSULTATION_DAT t1 ");
            stb.append("    LEFT JOIN NAME_MST n1 ON n1.NAMECD2 = JUDGEMENT ");
            stb.append("    AND n1.NAMECD1 = 'L002' ");
            stb.append("    LEFT JOIN NAME_MST n2 ON n2.NAMECD2 = SHDIV ");
            stb.append("    AND n2.NAMECD1 = 'L006' ");
            stb.append("    LEFT JOIN ENTEXAM_COURSE_MST t2 ON t2.COURSECD = t1.COURSECD ");
            stb.append("    AND t2.ENTEXAMYEAR = '").append(_hmparam.get("YEAR")).append("' ");
            stb.append("    AND t2.MAJORCD = t1.MAJORCD ");
            stb.append("    AND t2.EXAMCOURSECD = t1.EXAMCOURSECD ");
            stb.append("WHERE ");
            stb.append("    t1.ENTEXAMYEAR = '").append(_hmparam.get("YEAR")).append("' ");
            if (!_hmparam.get("TESTDIV").equals("99")){
                stb.append("    AND t1.TESTDIV = '").append(_hmparam.get("TESTDIV")).append("' ");
            }
            stb.append("    AND t1.DATADIV = '1' ");
            //塾データ
            stb.append("),PSDATA AS ( ");
            stb.append("SELECT ");
            stb.append("    ACCEPTNO,DATADIV,SHDIV,WISHNO, ");
            stb.append("    n2.ABBV1 AS SHNAME,n1.NAME1 AS JUDG,EXAMCOURSE_MARK AS MAJOR ");
            stb.append("FROM ");
            stb.append("    ENTEXAM_CONSULTATION_DAT t1 ");
            stb.append("    LEFT JOIN NAME_MST n1 ON n1.NAMECD2 = JUDGEMENT ");
            stb.append("    AND n1.NAMECD1 = 'L002' ");
            stb.append("    LEFT JOIN NAME_MST n2 ON n2.NAMECD2 = SHDIV ");
            stb.append("    AND n2.NAMECD1 = 'L006' ");
            stb.append("    LEFT JOIN ENTEXAM_COURSE_MST t2 ON t2.COURSECD = t1.COURSECD ");
            stb.append("    AND t2.ENTEXAMYEAR = '").append(_hmparam.get("YEAR")).append("' ");
            stb.append("    AND t2.MAJORCD = t1.MAJORCD ");
            stb.append("    AND t2.EXAMCOURSECD = t1.EXAMCOURSECD ");
            stb.append("WHERE ");
            stb.append("    t1.ENTEXAMYEAR = '").append(_hmparam.get("YEAR")).append("' ");
            if (!_hmparam.get("TESTDIV").equals("99")){
                stb.append("    AND t1.TESTDIV = '").append(_hmparam.get("TESTDIV")).append("' ");
            }
            stb.append("    AND t1.DATADIV = '2' ");
            //重複メイン候補
            stb.append("), MAIN_T AS (SELECT ");
            stb.append("    COUNT(*) AS CNT, ");
            stb.append("    ENTEXAMYEAR, ");
            stb.append("    TESTDIV, ");
            stb.append("    MIN(ACCEPTNO) AS ACCEPTNO1, ");
            stb.append("    ").append(_hmparam.get("MAIN_T_SELECT")).append(" ");
            stb.append("FROM ");
            stb.append("    ENTEXAM_CONSULTATION_HDAT ");
            stb.append("WHERE ");
            stb.append("    ENTEXAMYEAR = '").append(_hmparam.get("YEAR")).append("' AND ");
            if (!_hmparam.get("TESTDIV").equals("99")) {
                stb.append("    TESTDIV = '").append(_hmparam.get("TESTDIV")).append("' ");
            }
            stb.append("GROUP BY ");
            stb.append("    ENTEXAMYEAR, ");
            stb.append("    TESTDIV, ");
            stb.append("    ").append(_hmparam.get("MAIN_T_GROUP")).append(" ");
            stb.append("HAVING ");
            stb.append("    COUNT(*) > 1 ");
            //重複候補データ
            stb.append("), JOIN_T AS (SELECT ");
            stb.append("    T1.ACCEPTNO AS ACCEPTNO2, ");
            stb.append("    VALUE(T1.NAME,'') AS NAME2, ");
            stb.append("    VALUE(T1.NAME_KANA,'') AS KANA2, ");
            stb.append("    CASE WHEN T1.SEX = '2' THEN '*' ELSE '' END AS SEX, ");
            stb.append("    T1.CREATE_DATE,");
            stb.append("    VALUE(T1.PS_ACCEPTNO,'') AS PS_ACCEPTNO2, ");
            stb.append("    VALUE(T1.FS_ACCEPTNO,'') AS FS_ACCEPTNO2, ");
            stb.append("    VALUE(T1.FS_CD,'') AS FS_CD2, ");
            stb.append("    L1.FINSCHOOL_NAME, ");
            stb.append("    L2.PRISCHOOL_NAME ");
            stb.append("FROM ");
            stb.append("    ENTEXAM_CONSULTATION_HDAT T1 ");
            stb.append("    LEFT JOIN FINSCHOOL_MST L1 ON L1.FINSCHOOLCD = T1.FS_CD ");
            stb.append("    LEFT JOIN PRISCHOOL_MST L2 ON L2.PRISCHOOLCD = T1.PS_CD ");
            stb.append("WHERE ");
            stb.append("    T1.ENTEXAMYEAR = '").append(_hmparam.get("YEAR")).append("' AND ");
            if (!_hmparam.get("TESTDIV").equals("99")) {
                stb.append("    T1.TESTDIV = '").append(_hmparam.get("TESTDIV")).append("' AND ");
            }
            stb.append("    T1.ACCEPTNO NOT IN (SELECT ");
            stb.append("                            T2.ACCEPTNO1 ");
            stb.append("                        FROM ");
            stb.append("                            MAIN_T T2 ");
            stb.append("                        ) ");
            //重複メイン
            stb.append("), SELECT_T AS (SELECT ");
            stb.append("    T1.ENTEXAMYEAR, ");
            stb.append("    T1.TESTDIV, ");
            stb.append("    MIN(T1.ACCEPTNO1) AS ACCEPTNO1, ");
            stb.append("    ").append(_hmparam.get("SELECT_T_SELECT")).append(" ");
            stb.append("FROM ");
            stb.append("    MAIN_T T1 ");
            stb.append("    LEFT JOIN ENTEXAM_CONSULTATION_HDAT L1 ON L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND L1.TESTDIV = T1.TESTDIV ");
            stb.append("         AND L1.ACCEPTNO = T1.ACCEPTNO1 ");
            stb.append("    LEFT JOIN JOIN_T T2 ON ").append(_hmparam.get("SELECT_JOIN")).append(" ");
            stb.append("WHERE ");
            stb.append("    ").append(_hmparam.get("SELECT_T_WHERE")).append(" ");
            stb.append("GROUP BY ");
            stb.append("    T1.ENTEXAMYEAR, ");
            stb.append("    T1.TESTDIV, ");
            stb.append("    ").append(_hmparam.get("SELECT_T_SELECT")).append(" ");
            //重複メインにソート順位を付加
            stb.append("), SELECT_T_NUM AS ( ");
            stb.append("SELECT ");
            stb.append("    ROW_NUMBER() OVER (ORDER BY ACCEPTNO1) AS LINE_NUMBER, ");
            stb.append("    T1.ACCEPTNO1, ");
            stb.append("    L2.FINSCHOOL_NAME, ");
            stb.append("    L3.PRISCHOOL_NAME, ");
            stb.append("    L1.CREATE_DATE, ");
            stb.append("    L1.NAME AS NAME1, ");
            stb.append("    L1.NAME_KANA AS KANA1, ");
            stb.append("    CASE WHEN L1.SEX = '2' THEN '*' ELSE '' END AS SEX, ");
            stb.append("    L1.PS_ACCEPTNO AS PS_ACCEPTNO1, ");
            stb.append("    L1.FS_ACCEPTNO AS FS_ACCEPTNO1, ");
            stb.append("    ").append(judgeSql).append(" ");
            stb.append("FROM ");
            stb.append("    SELECT_T T1 ");
            stb.append("    LEFT JOIN ENTEXAM_CONSULTATION_HDAT L1 ON L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND L1.TESTDIV = T1.TESTDIV ");
            stb.append("         AND L1.ACCEPTNO = T1.ACCEPTNO1 ");
            stb.append("    LEFT JOIN FINSCHOOL_MST L2 ON L2.FINSCHOOLCD = L1.FS_CD ");
            stb.append("    LEFT JOIN PRISCHOOL_MST L3 ON L3.PRISCHOOLCD = L1.PS_CD ");
            stb.append("    LEFT JOIN FSDATA f1 ON f1.ACCEPTNO = L1.ACCEPTNO AND f1.WISHNO = '1'"); //NO001
            stb.append("    LEFT JOIN FSDATA f2 ON f2.ACCEPTNO = L1.ACCEPTNO AND f2.WISHNO = '2'"); //NO001
            stb.append("    LEFT JOIN FSDATA f3 ON f3.ACCEPTNO = L1.ACCEPTNO AND f3.WISHNO = '3'"); //NO001
            stb.append("    LEFT JOIN FSDATA f4 ON f4.ACCEPTNO = L1.ACCEPTNO AND f4.WISHNO = '4'"); //NO001
            stb.append("    LEFT JOIN PSDATA p1 ON p1.ACCEPTNO = L1.ACCEPTNO AND p1.WISHNO = '1'"); //NO001
            stb.append("    LEFT JOIN PSDATA p2 ON p2.ACCEPTNO = L1.ACCEPTNO AND p2.WISHNO = '2'"); //NO001
            stb.append("    LEFT JOIN PSDATA p3 ON p3.ACCEPTNO = L1.ACCEPTNO AND p3.WISHNO = '3'"); //NO001
            stb.append("    LEFT JOIN PSDATA p4 ON p4.ACCEPTNO = L1.ACCEPTNO AND p4.WISHNO = '4'"); //NO001
            stb.append("ORDER BY ");
            stb.append("    ACCEPTNO1 ");
            stb.append(") ");
            //メインデータ
            stb.append("SELECT ");
            stb.append("    * ");
            stb.append("FROM ");
            stb.append("    SELECT_T_NUM ");
            stb.append("UNION ALL ");
            stb.append("SELECT ");
            stb.append("    L2.LINE_NUMBER, ");
            stb.append("    T2.ACCEPTNO2 AS ACCEPTNO1, ");
            stb.append("    T2.FINSCHOOL_NAME, ");
            stb.append("    T2.PRISCHOOL_NAME, ");
            stb.append("    T2.CREATE_DATE, ");
            stb.append("    T2.NAME2 AS NAME1, ");
            stb.append("    T2.KANA2 AS KANA1, ");
            stb.append("    T2.SEX, ");
            stb.append("    T2.PS_ACCEPTNO2 AS PS_ACCEPTNO1, ");
            stb.append("    T2.FS_ACCEPTNO2 AS FS_ACCEPTNO1, ");
            stb.append("    ").append(judgeSql).append(" ");
            stb.append("FROM ");
            stb.append("    SELECT_T T1 ");
            stb.append("    LEFT JOIN ENTEXAM_CONSULTATION_HDAT L1 ON L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND L1.TESTDIV = T1.TESTDIV ");
            stb.append("         AND L1.ACCEPTNO = T1.ACCEPTNO1 ");
            stb.append("    LEFT JOIN SELECT_T_NUM L2 ON L2.ACCEPTNO1 = T1.ACCEPTNO1 ");
            stb.append("    LEFT JOIN JOIN_T T2 ON ").append(_hmparam.get("SELECT_JOIN")).append(" ");
            stb.append("    LEFT JOIN FSDATA f1 ON f1.ACCEPTNO = T2.ACCEPTNO2 AND f1.WISHNO = '1'"); //NO001
            stb.append("    LEFT JOIN FSDATA f2 ON f2.ACCEPTNO = T2.ACCEPTNO2 AND f2.WISHNO = '2'"); //NO001
            stb.append("    LEFT JOIN FSDATA f3 ON f3.ACCEPTNO = T2.ACCEPTNO2 AND f3.WISHNO = '3'"); //NO001
            stb.append("    LEFT JOIN FSDATA f4 ON f4.ACCEPTNO = T2.ACCEPTNO2 AND f4.WISHNO = '4'"); //NO001
            stb.append("    LEFT JOIN PSDATA p1 ON p1.ACCEPTNO = T2.ACCEPTNO2 AND p1.WISHNO = '1'"); //NO001
            stb.append("    LEFT JOIN PSDATA p2 ON p2.ACCEPTNO = T2.ACCEPTNO2 AND p2.WISHNO = '2'"); //NO001
            stb.append("    LEFT JOIN PSDATA p3 ON p3.ACCEPTNO = T2.ACCEPTNO2 AND p3.WISHNO = '3'"); //NO001
            stb.append("    LEFT JOIN PSDATA p4 ON p4.ACCEPTNO = T2.ACCEPTNO2 AND p4.WISHNO = '4'"); //NO001
            stb.append("WHERE ");
            stb.append("    ").append(_hmparam.get("SELECT_T_WHERE")).append(" ");
            stb.append("ORDER BY ");
            stb.append("    LINE_NUMBER, ");
            stb.append("    ACCEPTNO1 ");

log.debug(stb);
        } catch( Exception e ){
            log.error("Pre_Stat1 error!");
        }
        return stb.toString();

    }

    //事前判定データ
    private String priorJudgmentSql()
    {
        StringBuffer stb = new StringBuffer();
        stb.append("    CASE WHEN f1.MAJOR IS NULL THEN ' ' ELSE value(f1.MAJOR,'') END || ");
        if (_hmparam.get("JHFLG").equals("2")){
            stb.append("    CASE WHEN f1.SHNAME IS NULL THEN ' ' ELSE value(f1.SHNAME,'') END || ");
        }
        stb.append("    CASE WHEN f1.JUDG IS NULL THEN ' ・' ELSE value(f1.JUDG,'') || '・' END || ");
        stb.append("    CASE WHEN f2.MAJOR IS NULL THEN ' ' ELSE value(f2.MAJOR,'') END || ");
        if (_hmparam.get("JHFLG").equals("2")){
            stb.append("    CASE WHEN f2.SHNAME IS NULL THEN ' ' ELSE value(f2.SHNAME,'') END || ");
        }
        stb.append("    CASE WHEN f2.JUDG IS NULL THEN ' ・' ELSE value(f2.JUDG,'') || '・' END || ");
        stb.append("    CASE WHEN f3.MAJOR IS NULL THEN ' ' ELSE value(f3.MAJOR,'') END || ");
        if (_hmparam.get("JHFLG").equals("2")){
            stb.append("    CASE WHEN f3.SHNAME IS NULL THEN ' ' ELSE value(f3.SHNAME,'') END || ");
        }
        stb.append("    CASE WHEN f3.JUDG IS NULL THEN ' ・' ELSE value(f3.JUDG,'') || '・' END || ");
        stb.append("    CASE WHEN f4.MAJOR IS NULL THEN ' ' ELSE value(f4.MAJOR,'') END || ");
        if (_hmparam.get("JHFLG").equals("2")){
            stb.append("    CASE WHEN f4.SHNAME IS NULL THEN ' ' ELSE value(f4.SHNAME,'') END || ");
        }
        stb.append("    CASE WHEN f4.JUDG IS NULL THEN ' ' ELSE value(f4.JUDG,'') END FSJUDG, ");
        stb.append("    CASE WHEN p1.MAJOR IS NULL THEN ' ' ELSE value(p1.MAJOR,'') END || ");
        if (_hmparam.get("JHFLG").equals("2")){
            stb.append("    CASE WHEN p1.SHNAME IS NULL THEN ' ' ELSE value(p1.SHNAME,'') END || ");
        }
        stb.append("    CASE WHEN p1.JUDG IS NULL THEN ' ・' ELSE value(p1.JUDG,'') || '・' END || ");
        stb.append("    CASE WHEN p2.MAJOR IS NULL THEN ' ' ELSE value(p2.MAJOR,'') END || ");
        if (_hmparam.get("JHFLG").equals("2")){
            stb.append("    CASE WHEN p2.SHNAME IS NULL THEN ' ' ELSE value(p2.SHNAME,'') END || ");
        }
        stb.append("    CASE WHEN p2.JUDG IS NULL THEN ' ・' ELSE value(p2.JUDG,'') || '・' END || ");
        stb.append("    CASE WHEN p3.MAJOR IS NULL THEN ' ' ELSE value(p3.MAJOR,'') END || ");
        if (_hmparam.get("JHFLG").equals("2")){
            stb.append("    CASE WHEN p3.SHNAME IS NULL THEN ' ' ELSE value(p3.SHNAME,'') END || ");
        }
        stb.append("    CASE WHEN p3.JUDG IS NULL THEN ' ・' ELSE value(p3.JUDG,'') || '・' END || ");
        stb.append("    CASE WHEN p4.MAJOR IS NULL THEN ' ' ELSE value(p4.MAJOR,'') END || ");
        if (_hmparam.get("JHFLG").equals("2")){
            stb.append("    CASE WHEN p4.SHNAME IS NULL THEN ' ' ELSE value(p4.SHNAME,'') END || ");
        }
        stb.append("    CASE WHEN p4.JUDG IS NULL THEN ' ' ELSE value(p4.JUDG,'') END PSJUDG ");

        return stb.toString();
    }
}//クラスの括り
