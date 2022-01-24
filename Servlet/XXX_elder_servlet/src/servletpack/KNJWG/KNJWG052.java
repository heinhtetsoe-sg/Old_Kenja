// kanji=漢字
/*
 * $Id: 89c07a04fd50dab5ee18cb4a2e7af9b3c7014cf2 $
 *
 * 作成日: 2007/03/13
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJWG;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [XXXX管理] 修了・卒業証明書（中高一貫）
 *
 *  2007/03/13 nakamoto・新規作成
 */

public class KNJWG052 {

    private static final Log log = LogFactory.getLog(KNJWG052.class);

    private String schno[];
    private boolean nonedata;

    private static final String FORM_FILE  = "KNJWG010_9.frm";  // 修了
    private static final String FORM_FILE2 = "KNJWG010_1.frm";  // 卒業

    private String nendo;
    private String kisaidate;
    private String graduatemonth;
    private boolean seirekiFlg;
    private String certifKindcd;

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

    protected String setSvfForm(final Map paramap) { return (((String) paramap.get("KIND")).equals("1")) ? FORM_FILE : FORM_FILE2; }

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
        ret = svf.VrSetForm(setSvfForm(paramap), 4);

        // 証明書種類コードの設定
        try {
            String tmpkind   = (String) paramap.get("KIND");    // 印刷種別 1:修了 2:卒業
            String tmpOutput = (String) paramap.get("OUTPUT");  // 印刷指定 1:クラス 2:個人
            String tmpCmbCls = (String) paramap.get("CMBCLASS");// コンボ '03' '03002'
            String tmpGrade  = (tmpOutput.equals("1")) ? tmpCmbCls : tmpCmbCls.substring(0, 2);
            if (tmpkind.equals("1")) {
                certifKindcd = "015";
            } else {
                certifKindcd = "001";
                String sql = "SELECT NAMESPARE2 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2='00' AND NAMESPARE2 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while( rs.next() ){
                    if (tmpGrade.equals("03")) certifKindcd = "022";
                }
                db2.commit();
                rs.close();
                ps.close();
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }

        // 西暦または和暦の出力フラグ
        try {
            seirekiFlg = false; //和暦
            String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while( rs.next() ){
                if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
            }
            db2.commit();
            rs.close();
            ps.close();
        } catch (Exception e) {
             log.error("Exception", e);
        }

        // 日付を出力
        try {
            // 記載日付
            if (seirekiFlg) {
                kisaidate = getSeireki_yyyyMd((String)paramap.get("NOTICEDAY"), "yyyy年M月d日");
            } else {
                kisaidate = KNJ_EditDate.h_format_JP((String)paramap.get("NOTICEDAY"));
            }
            // 年度
            String warekinendo = nao_package.KenjaProperties.gengou(Integer.parseInt((String)paramap.get("YEAR")));
            nendo = warekinendo.substring(2);
            //印刷種別 1:修了 2:卒業
            if (((String) paramap.get("KIND")).equals("2")) {
                // 卒業年月
                if (seirekiFlg) {
                    graduatemonth = getSeireki_yyyyMd((String)paramap.get("GRADUATE_DATE"), "yyyy年M月");
                } else {
                    graduatemonth = KNJ_EditDate.h_format_JP_M((String)paramap.get("GRADUATE_DATE"));
                }
            }
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
            while( rs.next() ){
                //生徒出力処理
                printSvfOutSchregno(svf,rs,paramap);
            }
        } catch( Exception ex ) { log.error("printSvfMain read error! ", ex);  }
    }

    /** 
     *   生徒出力処理
     */
    private void printSvfOutSchregno(
            Vrw32alp svf,
            ResultSet rs,
            Map paramap
    ) {
        try {
            int ret = 0;

            // 生徒名
            if (rs.getString("NAME") != null  &&  12 < rs.getString("NAME").length()) {
                ret = svf.VrsOut("NAME2", rs.getString("NAME") );
            } else {
                ret = svf.VrsOut("NAME1", rs.getString("NAME") );
            }
            // 誕生日
            if (seirekiFlg) {
                ret = svf.VrsOut("BIRTHDAY", (rs.getString("BIRTHDAY") != null) ? getSeireki_yyyyMd(rs.getString("BIRTHDAY"), "yyyy年M月d日生") : "" );
            } else {
                ret = svf.VrsOut("BIRTHDAY", (rs.getString("BIRTHDAY") != null) ? KNJ_EditDate.h_format_JP_Bth(rs.getString("BIRTHDAY")) : "" );
            }
            //印刷種別 1:修了 2:卒業
            if (((String) paramap.get("KIND")).equals("1")) {
                ret = svf.VrsOut("COURSE", rs.getString("COURSENAME") );  // 課程名
            } else {
                ret = svf.VrsOut("GRADUATION", graduatemonth );  // 卒業年月
                ret = svf.VrsOut("SYOSYO_NAME", rs.getString("SYOSYO") );  // 証書名
                ret = svf.VrsOut("CERTIF_NO", rs.getString("CERTIF_NO") );  // 証書番号
                ret = svf.VrsOut("SYOSYO_NAME2", rs.getString("SYOSYO2") );  // 証書名2
            }
            ret = svf.VrsOut("SCHOOLNAME",  rs.getString("SCHOOL") );  // 学校名
            ret = svf.VrsOut("JOBNAME",  rs.getString("JOB") );  // 役職名
            ret = svf.VrsOut("STAFFNAME",  rs.getString("PRINCIPAL") );  // 校長名
            ret = svf.VrsOut("DATE",  kisaidate );  // 記載日付
            //句点出力
            if (paramap.get("POINT") != null) {
                ret = svf.VrsOut("POINT", "。" );
            }

            ret = svf.VrEndRecord();
            if(ret == 0)nonedata = true;
        } catch( Exception ex ){
            log.error("printSvfOutSchregno error!", ex );
        }
    }

    /* 
     *  SQLStatement作成 学籍データ
     */
    private String sqlSchregno(Map paramap) {

        StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHNO AS ( ");
        stb.append("     SELECT T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.YEAR, ");
        stb.append("            case when T2.GRD_DIV = '1' then T2.GRD_DATE end as GRD_DATE, ");
        stb.append("            T2.NAME, T2.BIRTHDAY, T1.COURSECD ");
        stb.append("     FROM   SCHREG_REGD_DAT T1, ");
        stb.append("            SCHREG_BASE_MST T2 ");
        stb.append("     WHERE  T1.YEAR = '" + (String) paramap.get("YEAR") + "' AND ");
        stb.append("            T1.SEMESTER = '" + (String) paramap.get("GAKKI") + "' AND ");
        //カテゴリ区分 1:クラス 2:個人
        if (((String) paramap.get("OUTPUT")).equals("1")) {
            stb.append("        T1.GRADE = '" + (String) paramap.get("CMBCLASS") + "' AND ");
            stb.append("        T1.GRADE || T1.HR_CLASS IN " + (String) paramap.get("SCHNOLIST") + " AND ");
        } else {
            stb.append("        T1.GRADE || T1.HR_CLASS = '" + (String) paramap.get("CMBCLASS") + "' AND ");
            stb.append("        T1.ATTENDNO IN " + (String) paramap.get("SCHNOLIST") + " AND ");
        }
        stb.append("            T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     ) ");
        stb.append(" , T_CERTIF AS ( ");
        stb.append("     SELECT YEAR, CERTIF_NO, ");
        stb.append("            CERTIF_KINDCD as KINDCD, ");
        stb.append("            SYOSYO_NAME as SYOSYO, ");
        stb.append("            SYOSYO_NAME2 as SYOSYO2, ");
        stb.append("            SCHOOL_NAME as SCHOOL, ");
        stb.append("            JOB_NAME as JOB, ");
        stb.append("            PRINCIPAL_NAME as PRINCIPAL ");
        stb.append("     FROM   CERTIF_SCHOOL_DAT ");
        stb.append("     WHERE  YEAR = '" + (String) paramap.get("YEAR") + "' AND ");
        //印刷種別 1:修了 2:卒業
        if (((String) paramap.get("KIND")).equals("1")) {
            stb.append("        CERTIF_KINDCD = '" + certifKindcd + "' ");
        } else {
            stb.append("        CERTIF_KINDCD = '" + certifKindcd + "' ");
        }
        stb.append("     ) ");
        //印刷種別 1:修了 2:卒業
        if (((String) paramap.get("KIND")).equals("2")) {
            stb.append(" , MAX_NO AS ( ");
            stb.append("     SELECT MAX(CERTIF_INDEX) AS MAX_INDEX,SCHREGNO ");
            stb.append("     FROM   CERTIF_ISSUE_DAT ");
            stb.append("     WHERE  YEAR = '" + (String) paramap.get("YEAR") + "' AND ");
            stb.append("            CERTIF_KINDCD = '" + certifKindcd + "' ");
            stb.append("     GROUP BY SCHREGNO ");
            stb.append("     ) ");
            stb.append(" , T_ISSUE AS ( ");
            stb.append("     SELECT CERTIF_INDEX,SCHREGNO,CERTIF_KINDCD,CERTIF_NO ");
            stb.append("     FROM   CERTIF_ISSUE_DAT T1 ");
            stb.append("     WHERE  YEAR = '" + (String) paramap.get("YEAR") + "' AND ");
            stb.append("            CERTIF_KINDCD = '" + certifKindcd + "' AND ");
            stb.append("            EXISTS(SELECT 'X' ");
            stb.append("                     FROM MAX_NO T2 ");
            stb.append("                    WHERE T2.MAX_INDEX = T1.CERTIF_INDEX AND ");
            stb.append("                          T2.SCHREGNO=T1.SCHREGNO) ");
            stb.append("     ) ");
        }

        stb.append(" SELECT T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.GRD_DATE, ");
        stb.append("        T1.NAME, T1.BIRTHDAY, T2.COURSENAME || '課程' AS COURSENAME, ");
        stb.append("        T3.KINDCD, T3.SYOSYO, T3.SYOSYO2, T3.SCHOOL, T3.JOB, T3.PRINCIPAL ");
        //印刷種別 1:修了 2:卒業
        if (((String) paramap.get("KIND")).equals("2")) {
            stb.append("   ,CASE WHEN T3.CERTIF_NO = '1' THEN NULL ELSE T4.CERTIF_NO END AS CERTIF_NO ");
        }
        stb.append(" FROM   SCHNO T1 ");
        stb.append("        LEFT JOIN COURSE_MST T2 ON T2.COURSECD = T1.COURSECD ");
        stb.append("        LEFT JOIN T_CERTIF T3 ON T3.YEAR = T1.YEAR ");
        //印刷種別 1:修了 2:卒業
        if (((String) paramap.get("KIND")).equals("2")) {
            stb.append("    LEFT JOIN T_ISSUE T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        }
        stb.append(" ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
        return stb.toString();
    }

    /*
     *  対象生徒番号編集(SQL用) 
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
        paramap.put("GAKKI", request.getParameter("GAKKI"));  //学期
        paramap.put("CMBCLASS", request.getParameter("CMBCLASS"));  //学年or学年・組
        paramap.put("KIND", request.getParameter("KIND"));  //印刷種別 1:修了 2:卒業
        paramap.put("OUTPUT", request.getParameter("OUTPUT"));  //カテゴリ区分 1:クラス 2:個人
        paramap.put("NOTICEDAY", request.getParameter("NOTICEDAY"));  //記載日付
        paramap.put("GRADUATE_DATE", request.getParameter("GRADUATE_DATE"));  //記載日付
        paramap.put("POINT", request.getParameter("POINT"));  //句点あり

        schno = request.getParameterValues("CLASS_SELECTED");  //出席番号または学年・組
        paramap.put("SCHNOLIST", Set_Schno(schno));  //番号の編集(SQL用)

        return paramap;
    }

    private String getSeireki_yyyyMd(String strx, String pattern){
        String hdate = new String();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            Date dat = new Date();
            try {
                sdf.applyPattern("yyyy-MM-dd");
                dat = sdf.parse( strx );
            } catch ( Exception e ) {
                try {
                    sdf.applyPattern("yyyy/MM/dd");
                    dat = sdf.parse( strx );
                } catch ( Exception e2 ) {
                    hdate = "";
                    return hdate;
                }
            }
            SimpleDateFormat sdfseireki = new SimpleDateFormat(pattern);
            hdate = sdfseireki.format(dat);
        } catch ( Exception e3 ) {
            hdate = "";
        }
        return hdate;
    }


}
