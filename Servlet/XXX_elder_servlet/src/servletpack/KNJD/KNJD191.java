// kanji=漢字
/*
 * $Id: 89b71e9fe06e7d3e0b580fb71c755f8abbe6c381 $
 *
 * 作成日: 2007/02/27
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJD;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;

/**
 *  学校教育システム 賢者 [成績管理] 考査得点一覧（自修館）
 *
 *  2007/02/27 nakamoto・新規作成
 */

public class KNJD191 {

    private static final Log log = LogFactory.getLog(KNJD191.class);

    private String schno[];
    private boolean nonedata;

    private static final String FORM_FILE = "KNJD191.frm";

    private String nendo;
    private String testname;

    /**
     *  KNJD.classから最初に起動されるクラス
     */
    public void svf_out(
            HttpServletRequest request,
            HttpServletResponse response)
    throws ServletException, IOException {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                  // Databaseクラスを継承したクラス
        
        // パラメータの取得
        final Map paramap = getParam(request);
        // print svf設定
        sd.setSvfInit(request, response, svf);
        // ＤＢ接続
        db2 = sd.setDb(request);
        if( sd.openDb(db2) ){
            log.error("db open error");
            return;
        }
        // 印刷処理
        printSvf(db2,svf,paramap);
        // 終了処理
        sd.closeSvf(svf, nonedata);
        sd.closeDb(db2);
    }

    /**
     *  印刷処理
     */
    private void printSvf(
            DB2UDB db2,
            Vrw32alp svf,
            Map paramap
    ) {
        setHead(db2,svf,paramap);         //見出し項目
        printSvfMain(db2,svf,paramap);        //SVF-FORM出力処理
    }

    protected String setSvfForm(final Map paramap) { return FORM_FILE; }
    
    /** 
     *  SVF-FORMセット＆見出し項目 
     */
    private void setHead(
            DB2UDB db2,
            Vrw32alp svf,
            Map paramap
    ) {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetForm(setSvfForm(paramap), 1);

        nendo = nao_package.KenjaProperties.gengou(Integer.parseInt((String)paramap.get("YEAR"))) + "年度";

        // テスト種別名を出力
        final String sql = " SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW"
                         + " WHERE YEAR = '" + (String)paramap.get("YEAR") + "'"
                         + "   AND SEMESTER = '" + (String)paramap.get("SEMESTER") + "'"
                         + "   AND TESTKINDCD||TESTITEMCD = '" + (String)paramap.get("TESTCD") + "'";
        try {
            db2.query(sql);
            final ResultSet rs = db2.getResultSet();
            if (rs.next() && rs.getString("TESTITEMNAME") != null) {
                testname = rs.getString("TESTITEMNAME");
            }
            db2.commit();
            rs.close();
        } catch (SQLException e) {
             log.error("SQLException", e);
        } catch (Exception e) {
             log.error("Exception", e);
        }
    }

    /** 
     *  SVF-FORM メイン出力処理 
     */
    private void printSvfMain(
            DB2UDB db2,
            Vrw32alp svf,
            Map paramap
    ) {
        //定義
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        PreparedStatement ps = null;
        ResultSet rs = null;

        //RecordSet作成
        try {
            String sql = sqlSchregno(paramap); //学籍データ
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            int linex = 0;
            String hr_class = "999";
            while( rs.next() ){
                //１ページ出力
                if (linex == 6 || (!hr_class.equals("999") && !hr_class.equals(rs.getString("HR_CLASS")))) {
                    ret = svf.VrsOut("NENDO",  nendo);
                    ret = svf.VrsOut("TESTNAME",  testname);
                    ret = svf.VrEndPage();
                    linex = 0;
                }
                //生徒出力処理
                printSvfOutSchregno(linex,svf,rs,paramap);
                //成績出力処理
                printSvfOutAttend(linex,db2,svf,rs.getString("SCHREGNO"),paramap);
                printSvfOutRecord(linex,db2,svf,rs.getString("SCHREGNO"),paramap);
                hr_class = rs.getString("HR_CLASS");
                linex++;
            }
            //最終ページ出力
            if (nonedata) {
                ret = svf.VrsOut("NENDO",  nendo);
                ret = svf.VrsOut("TESTNAME",  testname);
                ret = svf.VrEndPage();
            }
        } catch( Exception ex ) { log.error("printSvfMain read error! ", ex);  }
    }

    /** 
     *   生徒出力処理
     */
    private void printSvfOutSchregno(
            int linex,
            Vrw32alp svf,
            ResultSet rs,
            Map paramap
    ) {
        try {
            int ret = 0;
            ret = svf.VrsOut("HR_NAME" + String.valueOf(linex + 1), rs.getString("HR_NAME") );  // 年組
            ret = svf.VrsOut("ATTENDNO" + String.valueOf(linex + 1), String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) );  // 出席番号
            ret = svf.VrsOut("NAME" + String.valueOf(linex + 1), rs.getString("NAME_SHOW") );  // 生徒名

            if(ret == 0)nonedata = true;
        } catch( Exception ex ){
            log.error("printSvfOutSchregno error!", ex );
        }
    }

    /** 
     *   成績出力処理（欠席者）
     */
    private void printSvfOutAttend(
            int linex,
            DB2UDB db2,
            Vrw32alp svf,
            String schregno,
            Map paramap
    ) {
        //定義
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = sqlScore(paramap); //成績得点データ
            ps = db2.prepareStatement(sql);
            ps.setString( 1, schregno );  //学籍番号
            rs = ps.executeQuery();
            while( rs.next() ){
                //欠席者出力処理（中学）
                printSvfOutScore(linex,svf,rs,paramap);
            }
        } catch( Exception ex ){
            log.error("printSvfOutAttend error!", ex );
        }
    }

    /** 
     *   欠席者出力処理（中学）
     */
    private void printSvfOutScore(
            int linex,
            Vrw32alp svf,
            ResultSet rs,
            Map paramap
    ) {
        try {
            int ret = 0;
            int len = 0;
            if ((rs.getString("CLASSCD")).equals("01")) len = 1;
            if ((rs.getString("CLASSCD")).equals("02")) len = 2;
            if ((rs.getString("CLASSCD")).equals("03")) len = 3;
            if ((rs.getString("CLASSCD")).equals("04")) len = 4;
            if ((rs.getString("CLASSCD")).equals("09")) len = 5;
            if (0 < len) {
                ret = svf.VrsOutn("SCORE" + String.valueOf(linex + 1), len, "欠" );
            } else {
                log.info("schregno="+rs.getString("SCHREGNO") + " subclasscd="+rs.getString("SUBCLASSCD"));
            }
            if (ret == 0) nonedata = true;
        } catch( Exception ex ){
            log.error("printSvfOutScore error!", ex );
        }
    }

    /** 
     *   成績出力処理
     */
    private void printSvfOutRecord(
            int linex,
            DB2UDB db2,
            Vrw32alp svf,
            String schregno,
            Map paramap
    ) {
        //定義
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = sqlRankAverage(paramap); //成績席次・平均データ
            ps = db2.prepareStatement(sql);
            ps.setString( 1, schregno );  //学籍番号
            rs = ps.executeQuery();
            while( rs.next() ){
                //席次・平均出力処理（中学）
                printSvfOutRankAverage(linex,svf,rs,paramap);
            }
        } catch( Exception ex ){
            log.error("printSvfOutRecord error!", ex );
        }
    }

    /** 
     *   席次・平均出力処理（中学）
     */
    private void printSvfOutRankAverage(
            int linex,
            Vrw32alp svf,
            ResultSet rs,
            Map paramap
    ) {
        try {
            int ret = 0;
            int len = 0;
            if ((rs.getString("CLASSCD")).equals("01")) len = 1;
            if ((rs.getString("CLASSCD")).equals("02")) len = 2;
            if ((rs.getString("CLASSCD")).equals("03")) len = 3;
            if ((rs.getString("CLASSCD")).equals("04")) len = 4;
            if ((rs.getString("CLASSCD")).equals("09")) len = 5;
            if ((rs.getString("SUBCLASSCD")).equals("555555")) len = 6;
            if (0 < len) {
                ret = svf.VrsOutn("SCORE" + String.valueOf(linex + 1), len, rs.getString("SCORE") );  // 得点
                final Double gradeAvg = KNJServletUtils.getDouble(rs, "GRADE_AVG");
                if (null != gradeAvg) {
                    final double roundAvg = KNJServletUtils.roundHalfUp(gradeAvg.doubleValue(), 1);
                    ret = svf.VrsOutn("AVERAGE" + String.valueOf(linex + 1), len, String.valueOf(roundAvg) );  // 学年平均点
                }
                ret = svf.VrsOutn("VALUE" + String.valueOf(linex + 1), len, rs.getString("GRADE_DEVIATION") );  // 学年偏差値
                ret = svf.VrsOutn("ORDER" + String.valueOf(linex + 1) + "_1", len, rs.getString("CLASS_RANK") );  // クラス順位
                ret = svf.VrsOutn("ORDER" + String.valueOf(linex + 1) + "_2", len, rs.getString("GRADE_RANK") );  // 学年順位               
            } else {
                log.info("schregno="+rs.getString("SCHREGNO") + " subclasscd="+rs.getString("SUBCLASSCD"));
            }

            if(ret == 0)nonedata = true;
        } catch( Exception ex ){
            log.error("printSvfOutRankAverage error!", ex );
        }
    }

    /* 
     *  SQLStatement作成 学籍データ
     */
    private String sqlSchregno(Map paramap) {

        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, ");
        stb.append("        T1.SCHREGNO, T2.HR_NAME, T3.NAME_SHOW ");
        stb.append(" FROM   SCHREG_REGD_DAT T1, ");
        stb.append("        SCHREG_REGD_HDAT T2, ");
        stb.append("        SCHREG_BASE_MST T3 ");
        stb.append(" WHERE  T1.YEAR = '" + (String) paramap.get("YEAR") + "' AND ");
        stb.append("        T1.SEMESTER = '" + (String) paramap.get("SEMESTER") + "' AND ");
        //カテゴリ区分 1:クラス 2:個人
        if (((String) paramap.get("CATEGORY_IS_CLASS")).equals("1")) {
            stb.append("    T1.GRADE || '-' || T1.HR_CLASS IN " + (String) paramap.get("SCHNOLIST") + " AND ");
        } else {
            stb.append("    T1.SCHREGNO IN " + (String) paramap.get("SCHNOLIST") + " AND ");
        }
        stb.append("        T2.YEAR = T1.YEAR AND ");
        stb.append("        T2.SEMESTER = T1.SEMESTER AND ");
        stb.append("        T2.GRADE = T1.GRADE AND ");
        stb.append("        T2.HR_CLASS = T1.HR_CLASS AND ");
        stb.append("        T3.SCHREGNO = T1.SCHREGNO ");
        stb.append(" ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
        return stb.toString();
    }

    /* 
     *  SQLStatement作成 成績得点データ
     */
    private String sqlScore(Map paramap) {
        StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_RANK AS ( ");
        stb.append("     SELECT SCHREGNO, SUBCLASSCD ");
        stb.append("     FROM   RECORD_SCORE_DAT ");
        stb.append("     WHERE  YEAR = '" + (String) paramap.get("YEAR") + "' AND ");
        stb.append("            SEMESTER = '" + (String) paramap.get("SEMESTER") + "' AND ");
        stb.append("            TESTKINDCD || TESTITEMCD = '" + (String)paramap.get("TESTCD") + "' AND ");
        stb.append("            SCHREGNO = ? ");
        stb.append("     GROUP BY SCHREGNO, SUBCLASSCD ");
        stb.append("     ) ");

        stb.append(" SELECT substr(T1.SUBCLASSCD,1,2) as CLASSCD, T1.SUBCLASSCD, T1.SCHREGNO ");
        stb.append(" FROM   T_RANK T1 ");
        stb.append(" ORDER BY T1.SUBCLASSCD ");
        return stb.toString();
    }

    /* 
     *  SQLStatement作成 成績席次・平均データ
     */
    private String sqlRankAverage(Map paramap) {

        StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_RANK AS ( ");
        stb.append("     SELECT SCHREGNO, SUBCLASSCD, SCORE, GRADE_DEVIATION, CLASS_RANK, GRADE_RANK ");
        stb.append("     FROM   RECORD_RANK_DAT ");
        stb.append("     WHERE  YEAR = '" + (String) paramap.get("YEAR") + "' AND ");
        stb.append("            SEMESTER = '" + (String) paramap.get("SEMESTER") + "' AND ");
        stb.append("            TESTKINDCD || TESTITEMCD = '" + (String)paramap.get("TESTCD") + "' AND ");
        stb.append("            SCHREGNO = ? ");
        stb.append("     ) ");
        stb.append(" ,T_AVERAGE AS ( ");
        stb.append("     SELECT SUBCLASSCD, AVG AS GRADE_AVG ");
        stb.append("     FROM   RECORD_AVERAGE_DAT ");
        stb.append("     WHERE  YEAR = '" + (String) paramap.get("YEAR") + "' AND ");
        stb.append("            SEMESTER = '" + (String) paramap.get("SEMESTER") + "' AND ");
        stb.append("            TESTKINDCD || TESTITEMCD = '" + (String)paramap.get("TESTCD") + "' AND ");
        stb.append("            AVG_DIV = '1' AND ");//学年
        stb.append("            GRADE = '" + (String)paramap.get("GRADE") + "' ");
        stb.append("     ) ");

        stb.append(" SELECT substr(T1.SUBCLASSCD,1,2) as CLASSCD, T1.SUBCLASSCD, T1.SCHREGNO, ");
        stb.append("        T1.SCORE, T2.GRADE_AVG, T1.GRADE_DEVIATION, T1.CLASS_RANK, T1.GRADE_RANK ");
        stb.append(" FROM   T_RANK T1 ");
        stb.append("        LEFT JOIN T_AVERAGE T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" ORDER BY T1.SUBCLASSCD ");
        return stb.toString();
    }

    /*
     *  対象生徒学籍番号編集(SQL用) 
     */
    private String Set_Schno(String schno[]) {

        StringBuffer stb = new StringBuffer();

        for (int ia=0; ia<schno.length; ia++) {
            if( ia==0 ) stb.append("('");
            else        stb.append("','");
            stb.append(schno[ia]);
        }
        stb.append("')");

        return stb.toString();
    }
    
    /* 
     *  get parameter doGet()パラメータ受け取り 
     */
    private Map getParam (final HttpServletRequest request) {

        if (log.isDebugEnabled()) {
            final Enumeration enums = request.getParameterNames();
            while (enums.hasMoreElements()) {
                final String name = (String) enums.nextElement();
                final String[] values = request.getParameterValues(name);
                log.debug("parameter:name=" + name + ", value=[" + StringUtils.join(values, ',') + "]");
            }
        }

        final Map paramap = new HashMap();
        paramap.put("YEAR", request.getParameter("YEAR"));  //年度
        paramap.put("SEMESTER", request.getParameter("SEMESTER"));  //学期
        paramap.put("GRADE", request.getParameter("GRADE"));  //学年
        paramap.put("TESTCD", request.getParameter("TESTCD"));  //テスト種別
        paramap.put("CATEGORY_IS_CLASS", request.getParameter("CATEGORY_IS_CLASS"));  //カテゴリ区分 1:クラス 2:個人

        schno = request.getParameterValues("CATEGORY_SELECTED");  //学籍番号または学年-組
        paramap.put("SCHNOLIST", Set_Schno(schno));  //学籍番号の編集(SQL用)

        return paramap;
    }


}
