// kanji=漢字
/*
 * $Id: 145fadf423fd4b69235bb3aa753c16ab0387e896 $
 *
 * 作成日: 2004/10/08 13:22:00 - JST
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJI;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *
 *  学校教育システム 賢者 [卒業生管理]
 *
 *                  ＜ＫＮＪＩ０９２Ｋ＞  卒業生台帳
 *
 *  2004/10/08
 *  2004/12/08 yamashiro 名簿の年・組の見出しを算用数字に変更
 *  2004/12/18 yamashiro 証書番号をテーブルから取得する場合、後ブランクを削除する
 *                       卒業生フラグを必ず条件とする => 処理年度とcontrol_mstの年度の比較をしない
 *  2004/12/21 yamashiro 西暦生年月日において元号が出力される不具合を修正
 *                       複数組出力した際、組の最終頁が出力されない不具合を修正
 *  2005/10/11 yamashiro 指示画面の卒業見込みがチェックされておれば今学期の3年生で除籍区分がnullの生徒を出力
 *  2005/10/13 yamashiro 出席番号の表記を追加(残?16)。  組・担任名の表記を大きくするに伴う修正(残?17)
 *                       生年月日において'元年'が出力されない不具合を修正
 *  2005/10/17 yamashiro MAJOR_MSTのリンクを修正
 */

public class KNJI092K {

    private static final Log log = LogFactory.getLog(KNJI092K.class);

    private Param param;
    private boolean nonedata;
    // 04/12/18  private boolean boothisyear;

    private java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy年M月d日");
    private Calendar cal = new GregorianCalendar();

    private static String arraykansuuji[] = {"〇","一","二","三","四","五","六","七","八","九","十"};
    private Map hmm = new HashMap();    //組アルファベットの変換用

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス

        // print svf設定
        response.setContentType("application/pdf");
        svf.VrInit();                                           //クラスの初期化
        try {
            svf.VrSetSpoolFileStream(response.getOutputStream());       //PDFファイル名の設定
        } catch( java.io.IOException ex ) {
            log.info("db new error:", ex);
        }

        // ＤＢ接続
        db2 = setDb(request);
        if( openDb(db2) ){
            log.error("db open error");
            return;
        }
        param = new Param(request);

        // 印刷処理
        printSvf(db2, svf);

        // 終了処理
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }
        svf.VrQuit();
        db2.commit();
        db2.close();

    }   //doGetの括り



    /**
     *  svf print 印刷処理 
     */
    private void printSvf(DB2UDB db2, Vrw32alp svf)
    {
        //setKansuuji();    //漢数字編集用配列を用意
        // boothisyear =  param._0.equals( getThisYear(db2) );     //処理年度が(CONTROL_MST)ならTRUE

        if (param._output1 != null) {
            try {
                svf.VrSetForm("KNJI092_1.frm", 1);
                printHyoushi(db2, svf);            //表紙を印刷
            } catch (Exception ex) {
                log.error("statementHyoushi error!", ex);
            }
        }

        if (param._output2 != null) {
            try {
                svf.VrSetForm("KNJI092_2.frm", 1);
                printMeibo(db2, svf);         //名簿を印刷
            } catch (Exception ex) {
                log.error("statementMeibo error!",ex);
            }
        }
    }


    /**
     *  svf print 表紙印刷
     */
    private void printHyoushi(final DB2UDB db2, final Vrw32alp svf)
    {
        PreparedStatement ps1 = null;
        ResultSet rs = null;
        try{
            final String sql = statementHyoushi();
//          log.debug(" sql = " + sql);
            ps1 = db2.prepareStatement(sql);
            rs = ps1.executeQuery();
            int count = 0;
            while (rs.next()) {
                if ("ZZZ".equals(rs.getString("HR_CLASS"))) {
                    printHyoushiSvf(svf, rs);
                } else {
                    printHyoushiSvf(svf, rs, ++count);
                }
                if (count == 30) {
                    svf.VrEndPage();
                    nonedata = true;
                    count = 0;
                }
            }
            if (count > 0) {
                svf.VrEndPage();
                nonedata = true;
            }
        } catch (Exception ex) {
            log.error("printHyoushi error!", ex);
        } finally{
            DbUtils.closeQuietly(null, ps1, rs);
            db2.commit();
        }
    }


    /**
     *  svf print 表紙.合計出力
     */
    private void printHyoushiSvf(final Vrw32alp svf, final ResultSet rs)
    {
        StringBuffer stb2 = new StringBuffer();
        try{

            if (rs.getString("CNT_SCH") != null) {
                svf.VrsOut( "TOTAL_NUMBER",     rs.getString("CNT_SCH")      );
            }
            if (rs.getString("CNT_SCH2") != null) {
                svf.VrsOut( "TOTAL_GIRL", "(" + rs.getString("CNT_SCH2") + ")");
            }
            if (rs.getString("MIN_GRD_NO") != null) {
                stb2.append("第").append(insertBlankMoji(new StringBuffer().append(convertKansuuji(rs.getString("MIN_GRD_NO"))))).append("号から");
                stb2.append("第").append(insertBlankMoji(new StringBuffer().append(convertKansuuji(rs.getString("MAX_GRD_NO"))))).append("号");
                svf.VrsOut( "CERTIF_NO" , stb2.toString());
            } else {
                svf.VrsOut( "CERTIF_NO" , "第      号から第      号" );
            }
        } catch(Exception ex) {
            log.error("printHyoushiSvf.total error!", ex);
        }
    }


    /**
     *  svf print 表紙.明細出力
     */
    private void printHyoushiSvf(final Vrw32alp svf, final ResultSet rs, final int count)
    {
        try{
            if(count == 1) {
                svf.VrsOut("NENDO" , getKansuujiWareki(nao_package.KenjaProperties.gengou(Integer.parseInt(param._year)) + "年度"));
            }
            int kurikaeshi = Math.abs( ( (count>15)? count-15 : count )-(15+1) );
            if (null != rs.getString("HR_CLASS_NAME1")) {
                svf.VrsOutn("HR_CLASS" + ((count > 15) ? "2" : "1"), kurikaeshi, rs.getString("HR_CLASS_NAME1"));
            } else if (isNumCheck(rs.getString("HR_CLASS"))) {
                svf.VrsOutn("HR_CLASS" + ((count > 15) ? "2" : "1"), kurikaeshi, String.valueOf(rs.getInt("HR_CLASS")));
            } else {
                svf.VrsOutn("HR_CLASS" + ((count > 15) ? "2" : "1"), kurikaeshi, rs.getString("HR_CLASS"));
            }
            if( rs.getString("STAFFNAME") != null )
                svf.VrsOutn( "NAME"      + ((count > 15) ? "2" : "1"), kurikaeshi, rs.getString("STAFFNAME") );
            if( rs.getString("CNT_SCH") != null )
                svf.VrsOutn( "NUMBER"    + ((count > 15) ? "2" : "1"), kurikaeshi, rs.getString("CNT_SCH")   );
            if( rs.getString("CNT_SCH2") != null )
                svf.VrsOutn( "GIRL"      + ((count > 15) ? "2" : "1"), kurikaeshi, "(" + rs.getString("CNT_SCH2") + ")"  );
        } catch (Exception ex) {
            log.error("printHyoushiSvf.total error!", ex);
        }
    }


    /**
     *  svf print 数字を漢数字へ変換(文字単位) 
     */
    private String convertKansuuji(final String suuji)
    {
        final StringBuffer stb = new StringBuffer();
        for (int i = 0; i < suuji.length(); i++) {
            if (Character.isDigit(suuji.charAt(i))) {
                stb.append( arraykansuuji[Integer.parseInt(suuji.substring(i, i+1))]);
            } else {
                if( hmm.get(suuji.substring(i,i+1)) == null ) {
                    stb.append( suuji.substring(i, i + 1));
                } else {
                    stb.append( (hmm.get(suuji.substring(i, i + 1))).toString());
                }
            }
        }
        return stb.toString();
    }


    /**
     *  svf print 数字を漢数字へ変換.百の位まで(数値単位) 
     */
    private String convertKansuuji(int suuji)
    {
        StringBuffer stb = new StringBuffer();
        int kurai = (String.valueOf(suuji)).length();
        if (kurai > 0) {
            if (Integer.parseInt((String.valueOf(suuji)).substring(kurai-1)) > 0) { 
                stb.append( arraykansuuji[Integer.parseInt((String.valueOf(suuji)).substring(kurai - 1))]);
            }
        }
        if (kurai >= 2) {
            stb.insert(0, "十");
//log.debug("suuji="+Integer.parseInt((String.valueOf(suuji)).substring(kurai-2,kurai-1)));
            if (Integer.parseInt((String.valueOf(suuji)).substring(kurai-2,kurai-1)) > 1) {
                stb.insert(0, arraykansuuji[Integer.parseInt((String.valueOf(suuji)).substring(kurai - 2, kurai - 1))]);
            }
        }
        if (kurai >= 3) {
            stb.insert(0, "百");
            if (Integer.parseInt((String.valueOf(suuji)).substring(kurai-3,kurai-2)) > 1) {
                stb.insert(0, arraykansuuji[Integer.parseInt((String.valueOf(suuji)).substring(kurai - 3, kurai - 2))]);
            }
        }
        return stb.toString();
    }


    /**
     *  svf print 名簿印刷処理
     */
    private void printMeibo(final DB2UDB db2, final Vrw32alp svf)
    {
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        ResultSet rs1 = null;
        param._6 = getGraduateDate(db2);    //卒業年月日
        crtAlphMap();
        try{
            ps1 = db2.prepareStatement(statementMeiboHeader());
            ps2 = db2.prepareStatement(statementMeiboMeisai());
            rs1 = ps1.executeQuery();

            while (rs1.next()) {
                /* クラス見出しのセット => 課程から卒業日まで一つの文字列に編集後、組をスペースに置き換え、縦で出力する。
                組は文字列を別にして横に出力する。その際、上述文字列の組位置を算出して出力する。
                */
                String meiboheader = getMeiboHeaderSec(svf, rs1.getString("MAJORNAME"), rs1.getString("STAFFNAME"),
                        rs1.getString("GRADE"), rs1.getString("GRADE_CD"),
                        rs1.getString("HR_CLASS_NAME1"), rs1.getString("HR_CLASS_NAME2"));  //クラス見出し
                //HR_CLASS2の出力位置設定
                int meiboheaderpoint = null == meiboheader ? 0 : Math.max(51 - meiboheader.length() + meiboheader.indexOf("*") + 1, 0);
                int idxAstar = meiboheader.indexOf("*");                            //SVF-FIELD HR_CLASS2 での組の位置を取得
                if (-1 != idxAstar) {
                    meiboheader = meiboheader.substring(0, idxAstar) + " " + meiboheader.substring(idxAstar + 1);  // '*'をスペースに置き換える
                }
                ps2.setString(1, rs1.getString("HR_CLASS"));  //組
                ResultSet rs2 = ps2.executeQuery();
                int count = 0;
                while (rs2.next()) {
                    printMeiboMeisaiSvf(svf, rs2, ++count, meiboheader, meiboheaderpoint, idxAstar); //生徒名簿を出力するメソッド
                    log.debug("count="+count+ " name="+rs2.getString("NAME"));
                    if (count == 12) {
                        svf.VrEndPage();
                        nonedata = true;
                        count = 0;
                    }
                }
                DbUtils.closeQuietly(rs2);
                if (count > 0) {
                    svf.VrEndPage();
                    svf.VrPrint();
                    nonedata = true;
                }
            }
        } catch (Exception ex) {
            log.error("printMeibo error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps1, rs1);
            DbUtils.closeQuietly(ps2);
            db2.commit();
        }
    }

    /**
     *   svf print 名簿.組見出し取得 
     *             2004/12/09Modify => 年、組を漢数字ではなく算用数字で出力
     */
    private String getMeiboHeaderSec(final Vrw32alp svf, final String majorname, final String staffname, 
            final String rsGrade, final String rsGradeCd, final String hrclassname1, final String hrclassname2)
    {
        final StringBuffer stb = new StringBuffer();
        try{
            if (majorname != null) {
                stb.append(majorname).append(" ");
            }
            
            if (StringUtils.isNumeric(rsGradeCd)) {
                stb.append("第").append(rsGradeCd).append("年");
            } else if (StringUtils.isNumeric(rsGrade)) {
                stb.append("第").append(rsGrade).append("年");
            }
            
            String hrName = null;
            if (hrclassname2 != null && false) {
                hrName = hrclassname2;
                log.debug(" SCHREG_REGD_HDAT.HR_NAME2から取得 : " + hrName);
            } else if (hrclassname1 != null) {
                final String name1 = hrclassname1;
                hrName = null == name1 ? null : (name1 + "組");
                log.debug(" SCHREG_REGD_HDAT.HR_NAME1から取得 : " + hrName);
            }
            
            if (hrName != null && !"".equals(hrName)) {
                stb.append(hrName).append(" ");
            } else {
                stb.append("*").append("組").append(" ");
            }
            log.debug(" hrName = " + stb.toString());
            
            if (staffname != null) {
                stb.append("担任名 ").append(staffname).append(" ");
            }
        } catch (Exception ex) {
            log.error("getMeiboHeader error!", ex);
        }
        return stb.toString();
    }

    /**
     *  svf print 今年度取得 
     */
    private String getThisYear(DB2UDB db2)
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String hyear = null;
        try{
            ps = db2.prepareStatement("SELECT CTRL_YEAR FROM CONTROL_MST WHERE CTRL_NO = '01' ");
            rs = ps.executeQuery();
            if (rs.next()) {
                hyear = rs.getString(1);
            }
        } catch (Exception ex) {
            log.error("getThisYear error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return null == hyear ? "" : hyear;            
    }


    /**
     *  svf print 卒業日付取得 
     */
    private String getGraduateDate(DB2UDB db2)
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String hdate = null;
        try{
            ps = db2.prepareStatement("SELECT GRADUATE_DATE FROM SCHOOL_MST WHERE YEAR = '" + param._year + "'");
            rs = ps.executeQuery();
            if (rs.next()) {
                cal.setTime(rs.getDate("GRADUATE_DATE"));
                hdate = getKansuujiWareki(nao_package.KenjaProperties.gengou(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE)));
            }
        } catch (Exception ex) {
            log.error("getGraduateDate error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return null == hdate ? "" : hdate;
    }


    /**
     *  svf print 日付を漢数字変換 
     */
    private String getKansuujiWareki(final String hdate)
    {
        final StringBuffer stb = new StringBuffer();
        try{
            boolean dflg = false;       //数値？
            int ia = 0;
            for (int i = 0; i < hdate.length(); i++) {
                if ((Character.isDigit(hdate.charAt(i)) && !dflg) || (!Character.isDigit(hdate.charAt(i)) && dflg)) {
                    if (i == 0) {
                        continue;
                    }
                    if (!dflg) {
                        stb.append(hdate.substring(ia,i));
                    } else {
                        stb.append(convertKansuuji(Integer.parseInt(hdate.substring(ia, i))));
                    }
                    ia = i;
                    dflg = Character.isDigit(hdate.charAt(i));
                }
            }
            if (ia > 0) {
                stb.append(hdate.substring(ia));
            }
        } catch (Exception ex) {
            log.error("getKansuujiWareki error!", ex);
        }
        return stb.toString();
    }


    /**
     *  svf print 名簿.明細 
     *            引数について int count : 出力済生徒数
     *                         String meiboheader : 課程から卒業日までのページ見出し
     *                         int meiboheaderpoint : ページ見出しにおける組の位置
     */
    private void printMeiboMeisaiSvf(final Vrw32alp svf, ResultSet rs, final int count,
            final String meiboheader,
            final int meiboheaderpoint,
            final int indexAster)
    {
        String arrstr[] = null;
        boolean langflg = false;
        final StringBuffer stb = new StringBuffer();
        try{
            if (count == 1) {
                svf.VrsOut("NENDO"    ,nao_package.KenjaProperties.gengou(Integer.parseInt(param._year)) + "年度");
                svf.VrsOut("HR_CLASS",  meiboheader);
                if (-1 != indexAster) {
                    svf.VrAttribute("HR_CLASS2", "Y=" + ( -360 + (67) * meiboheaderpoint));  // 組の出力位置を変更
                    if (isNumCheck(rs.getString("HR_CLASS"))) {
                        svf.VrsOut("HR_CLASS2", String.valueOf(rs.getInt("HR_CLASS")));
                    } else {
                        svf.VrsOut("HR_CLASS2", rs.getString("HR_CLASS"));
                    }
                }
                svf.VrsOut("GRADUATE",  "（" + param._6 + " 卒業" + "）");
            }

            final int kurikaeshi = Math.abs(count - (12 + 1));
            if (rs.getString("GRD_NO") != null) {
                svf.VrsOutn("CERTIFNO",kurikaeshi ,"第" + insertBlankMoji(stb.append(convertKansuuji(rs.getString("GRD_NO")))) + "号" );
            }
            svf.VrsOutn("KANA"    , kurikaeshi, rs.getString("NAME_KANA")  );
            svf.VrsOutn("ATTENDNO", kurikaeshi, String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))));

            int mojisuu = rs.getString("NAME") == null ? 0 : rs.getString("NAME").length();
            if (mojisuu > 0 && 20 >= mojisuu) {
                langflg = isJapaneaseUse((rs.getString("NAME")).charAt(0)); //最初の文字で縦・横を判断する！
                if (10 >= mojisuu) {
                    if (!langflg) {
                        svf.VrAttributen("NAME1", kurikaeshi, "Rotation=270");
                    }
                    svf.VrsOutn("NAME1", kurikaeshi, rs.getString("NAME"));
                } else {
                    if (!langflg) {
                        svf.VrAttributen( "NAME2", kurikaeshi, "Rotation=270");
                    }
                    if (!langflg) {
                        svf.VrAttributen( "NAME3", kurikaeshi, "Rotation=270");
                    }
                    svf.VrsOutn("NAME2", kurikaeshi, (rs.getString("NAME")).substring(0,10));
                    svf.VrsOutn("NAME3", kurikaeshi, (rs.getString("NAME")).substring(10));
                }
            }

            arrstr = arrayBirth(rs.getDate("BIRTHDAY"), ((rs.getString("BIRTHDIV") != null) ? 1 : 0));
            if (arrstr != null) {
                for (int i = 1; i < 8; i++ ) {
                    svf.VrsOutn( "BIRTHDAY" + i ,kurikaeshi ,"");
                }
                for (int i= (arrstr.length - 1), j = 7; i >= 0; i--, j--) {
                    svf.VrsOutn( "BIRTHDAY" + j, kurikaeshi, arrstr[i]);
                }
            }
        } catch (Exception ex) {
            log.error("printMeibo error!", ex);
        }
    }


    /* 文字列ブランク挿入 */
    String insertBlankMoji(final StringBuffer stb) {
        stb.insert(0,"      ");     //６文字に満たない場合はブランクを挿入！
        if (stb.length() > 6) {
            stb.delete(0, stb.length() - 6);
        }
        return stb.toString();
    }

    /* ひらがな、かたかな、漢字判定 */
    boolean isJapaneaseUse(char ch) {
        boolean hantei = false;
/* ***** 日本語特有の文字かどうかの判断は困難！
        hantei = java.lang.Character.UnicodeBlock.of(ch).equals(java.lang.Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
        if( !hantei )hantei = java.lang.Character.UnicodeBlock.of(ch).equals(java.lang.Character.UnicodeBlock.HIRAGANA);
        if( !hantei )hantei = java.lang.Character.UnicodeBlock.of(ch).equals(java.lang.Character.UnicodeBlock.KATAKANA);
        if( !hantei )hantei = java.lang.Character.UnicodeBlock.of(ch).equals(java.lang.Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A);
*******  アルファベットかどうかの判断において、一部のカタカナがアルファベットと認識される */
        if( !hantei )hantei = java.lang.Character.UnicodeBlock.of(ch).equals(java.lang.Character.UnicodeBlock.BASIC_LATIN);
        if( !hantei )hantei = java.lang.Character.UnicodeBlock.of(ch).equals(java.lang.Character.UnicodeBlock.LATIN_1_SUPPLEMENT);
        if( !hantei )hantei = java.lang.Character.UnicodeBlock.of(ch).equals(java.lang.Character.UnicodeBlock.LATIN_EXTENDED_A);
        if( !hantei )hantei = java.lang.Character.UnicodeBlock.of(ch).equals(java.lang.Character.UnicodeBlock.LATIN_EXTENDED_B);
        if( !hantei )hantei = java.lang.Character.UnicodeBlock.of(ch).equals(java.lang.Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS);

        return !hantei;
    }


    /**
     *  数値チェック
     *    項目が半角0-9か判断する
     *
     *  @param    strNum  入力パラメータ
     *  @return   boolean  有効 true 無効 false
     */         
    public boolean isNumCheck(String strInString) {
        //文字列の長さ分繰り返し
        int intChk;
        intChk = 0;
        for (int i = 0; i < strInString.length(); i++) {
            char c  =  strInString.charAt(i);
            char c1 =  '0';
            char c2 =  '9';
            if (c < c1 || c > c2) {
                intChk = intChk + 1;
            }
        }
        if (intChk == 0) {
            return true;
        } else {
            return false;
        }
    }

    /* svf print 年月日の編集 */
    private String arrayBirth(java.util.Date pdate, int condiv)[]
    {
        String[] arr_date = condiv == 0 ? new String[7] : new String[6];
        String hdate = null;
        try{
            if( condiv == 0){
                cal.setTime(pdate);
                hdate = nao_package.KenjaProperties.gengou(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DATE));
            } else{
                hdate = sdf.format(pdate);
            }
//log.debug("hdate="+hdate);
            boolean dflg = false;       //数値？
            int ia = 0;
            int ib = 0;
            for( int i=0 ; i<hdate.length() ; i++ ){
                //if( ( Character.isDigit(hdate.charAt(i)) && !dflg ) || ( !Character.isDigit(hdate.charAt(i)) && dflg ) ){
                //05/10/13Modify '元'年に対応
                boolean cflg = (hdate.charAt(i) == '元') || Character.isDigit(hdate.charAt(i));
                if ((cflg && !dflg) || (!cflg &&  dflg)) {
                    if( i > 0 )arr_date[ib++] = hdate.substring(ia,i);
                    ia = i;
                    //dflg = Character.isDigit(hdate.charAt(i));
                    dflg = cflg;   //10/05/13
                }
            }
            if (ia > 0) {
                arr_date[ib] = hdate.substring(ia);
            }
        } catch( Exception ex ){
            log.error("printMeibo error!",ex);
        }
        return arr_date;
    }

    /* DB set */
    private DB2UDB setDb(final HttpServletRequest request) throws ServletException, IOException
    {
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME") , "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
        } catch( Exception ex ){
            log.info("db new error:" + ex);
            if (db2 != null) {
                db2.close();
            }
        }
        return db2;
    }


    /* DB open */
    private boolean openDb(final DB2UDB db2)
    {
        try {
            db2.open();
        } catch (Exception ex) {
            log.error("db open error!", ex);
            return true;
        }//try-cathの括り

        return false;

    }//private boolean Open_db()

    /* 漢数字変換用のMAP作成 */
    private void crtAlphMap()
    {
        String obj1[] = {"J","P","Q","S","A","B","C","D"};
        String obj2[] = {"Ｊ","Ｐ","Ｑ","Ｓ","Ａ","Ｂ","Ｃ","Ｄ"};
        for(int i = 0; i < obj1.length; i++) hmm.put(obj1[i], obj2[i]);

    }//private void getf()


    /* DB STATEMENT 表紙 */
    private String statementHyoushi()
    {
        final StringBuffer stb = new StringBuffer();
        try{
            stb.append("WITH T_HRCLASS (HR_CLASS, HR_CLASS_NAME1, TR_CD1) AS ( ");
            stb.append("SELECT HR_CLASS, HR_CLASS_NAME1, TR_CD1 ");
            stb.append("FROM   SCHREG_REGD_HDAT ");
            stb.append("WHERE  YEAR = '" + param._year + "' AND SEMESTER='" + param._gakki + "' AND GRADE = '" + param._grade + "' ");
            stb.append("UNION  VALUES('ZZZ','',''))");
            
            stb.append("SELECT W1.HR_CLASS, W1.HR_CLASS_NAME1, STAFFNAME, CNT_SCH, CNT_SCH2,");
            stb.append(       "MIN_GRD_NO,");
            stb.append(       "MAX_GRD_NO ");
            stb.append("FROM   T_HRCLASS W1 ");
            stb.append("LEFT   JOIN STAFF_MST W2 ON W2.STAFFCD=W1.TR_CD1 ");
            
            stb.append("LEFT JOIN (");
            stb.append("SELECT S1.HR_CLASS, S2.HR_CLASS_NAME1, COUNT(S1.SCHREGNO) AS CNT_SCH,");
            stb.append(       "SUM(CASE S3.SEX WHEN '2' THEN 1 ELSE NULL END)AS CNT_SCH2,");
            stb.append(       "'' AS MIN_GRD_NO,'' AS MAX_GRD_NO ");
            stb.append("FROM   SCHREG_REGD_DAT S1, T_HRCLASS S2, SCHREG_BASE_MST S3 ");
            stb.append("WHERE  S1.YEAR = '" + param._year + "' AND S1.SEMESTER='" + param._gakki + "' AND S1.GRADE = '" + param._grade + "' AND ");
            stb.append(       "S1.SCHREGNO=S3.SCHREGNO AND S1.HR_CLASS=S2.HR_CLASS ");
            if (param._mikomi == null) {
                stb.append(   "AND VALUE(S3.GRD_DIV,'0')='1' ");
            } else {
                stb.append(   "AND S3.GRD_DIV IS NULL ");
            }
            stb.append("GROUP BY S1.HR_CLASS, S2.HR_CLASS_NAME1 ");
            stb.append("UNION ");
            stb.append("SELECT 'ZZZ' AS HR_CLASS, 'ZZZ' AS HR_CLASS_NAME1, COUNT(SCHREGNO) AS CNT_SCH,");
            stb.append(       "SUM(CASE SEX WHEN '2' THEN 1 ELSE NULL END) AS CNT_SCH2,");
            stb.append(       "MIN(GRD_NO)AS MIN_GRD_NO,");
            stb.append(       "MAX(GRD_NO)AS MAX_GRD_NO ");
            stb.append("FROM   (SELECT S1.SCHREGNO,SEX,");
            stb.append(              "(SUBSTR('      '||GRD_NO,LENGTH('      '||RTRIM(GRD_NO))-5,6)) AS GRD_NO ");
            stb.append(        "FROM   SCHREG_REGD_DAT S1,SCHREG_BASE_MST S3 ");
            stb.append(        "WHERE  S1.YEAR = '" + param._year + "' AND S1.SEMESTER = '" + param._gakki + "' AND S1.GRADE = '" + param._grade + "' AND ");
            stb.append(       "S1.SCHREGNO=S3.SCHREGNO ");
            if (param._mikomi == null) {
                stb.append(   "AND VALUE(S3.GRD_DIV,'0')='1' ");
            } else {
                stb.append(   "AND S3.GRD_DIV IS NULL ");
            }
            stb.append(")S1 )W3 ON W3.HR_CLASS=W1.HR_CLASS ");
            
            stb.append("ORDER BY W1.HR_CLASS ");
        } catch (Exception ex) {
            log.error("sql statement error!", ex);
        }
        return stb.toString();
    }


    /**
     *  DB STATEMENT 名簿.クラス見出し 
     */
    private String statementMeiboHeader()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT INT(W1.GRADE) AS GRADE, ");
        stb.append("       CASE WHEN W6.GRADE_CD IS NULL THEN NULL ELSE INT(W6.GRADE_CD) END AS GRADE_CD, ");
        stb.append("       W1.HR_CLASS, ");
        stb.append("       W1.HR_CLASS_NAME1, ");
        stb.append("       W1.HR_CLASS_NAME2, ");
        stb.append("       W2.STAFFNAME, ");
        stb.append("       W4.MAJORNAME ");
        stb.append("FROM   SCHREG_REGD_HDAT W1 ");
        stb.append("LEFT   JOIN STAFF_MST W2 ON W2.STAFFCD=W1.TR_CD1 ");
        stb.append("LEFT   JOIN (");
        stb.append(    "SELECT HR_CLASS, MAJORCD, COURSECD ");
        stb.append(    "FROM   SCHREG_REGD_DAT S1 ");
        stb.append(    "WHERE  YEAR = '" + param._year + "' AND SEMESTER = '" + param._gakki + "' AND GRADE = '" + param._grade + "' AND HR_CLASS IN " + param._gradehrclass + " AND ");
        stb.append(           "ATTENDNO = (SELECT MIN(ATTENDNO) FROM SCHREG_REGD_DAT S2 ");
        stb.append(                     "WHERE  S2.YEAR = S1.YEAR AND S2.SEMESTER = S1.SEMESTER AND ");
        stb.append(                            "S1.GRADE = S2.GRADE AND S1.HR_CLASS = S2.HR_CLASS AND S2.MAJORCD IS NOT NULL)");
        stb.append(    ") W3 ON W3.HR_CLASS = W1.HR_CLASS ");
        stb.append("LEFT   JOIN MAJOR_MST W4 ON W4.MAJORCD = W3.MAJORCD AND W4.COURSECD = W3.COURSECD ");
        stb.append("LEFT JOIN SCHREG_REGD_GDAT W6 ON W6.YEAR = W1.YEAR AND W6.GRADE = W1.GRADE ");
        stb.append("WHERE  W1.YEAR = '" + param._year + "' AND W1.SEMESTER = '" + param._gakki + "' AND W1.GRADE = '" + param._grade + "' AND W1.HR_CLASS IN " + param._gradehrclass + " ");
        stb.append("ORDER BY W1.HR_CLASS");
        return stb.toString();
    }


    /* DB STATEMENT 名簿.明細 */
    private String statementMeiboMeisai()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT RTRIM(S3.GRD_NO) AS GRD_NO, ");
        stb.append(       "NAME, ");
        stb.append(       "NAME_KANA, ");
        stb.append(       "BIRTHDAY, ");
        stb.append(       "HR_CLASS, ");
        stb.append(       "ATTENDNO, ");
        stb.append(       "(SELECT SCHREGNO FROM KIN_GRD_LEDGER_SETUP_DAT S2 WHERE S1.SCHREGNO = S2.SCHREGNO) AS BIRTHDIV ");
        stb.append("FROM   SCHREG_REGD_DAT S1, SCHREG_BASE_MST S3 ");
        stb.append("WHERE  S1.YEAR = '" + param._year + "' AND ");
        stb.append(       "S1.SEMESTER = '" + param._gakki + "' AND ");
        stb.append(       "S1.GRADE = '" + param._grade + "' AND ");
        stb.append(       "S1.HR_CLASS = ? AND S1.SCHREGNO = S3.SCHREGNO ");
        if (param._mikomi == null) {
            stb.append(  "AND VALUE(S3.GRD_DIV,'0') = '1' ");
        } else {
            stb.append(   "AND S3.GRD_DIV IS NULL ");
        }
        stb.append("ORDER BY S1.ATTENDNO");
        return stb.toString();
    }
    
    static class Param {
        final String _year;
        final String _gakki;
        final String _grade;
        final String _gradehrclass;
        final String _output1;
        final String _output2;
        String _6;
        final String _mikomi;
        Param(final HttpServletRequest request) {
            _year = request.getParameter("YEAR");                    //卒業年度
            _gakki = request.getParameter("GAKKI");           //学期

            String hrclass[] = request.getParameterValues("CLASS_SELECTED");        //対象クラス
            final StringBuffer stb = new StringBuffer();
            stb.append("(");
            String grade = null;
            for (int i = 0; i < hrclass.length; i++) {
                if (i > 0) {
                    stb.append(",");
                }
                grade = hrclass[i].substring(0,2);               //対象学年
                stb.append("'").append(hrclass[i].substring(2)).append("'");
            }
            stb.append(")");
            _gradehrclass = stb.toString();                                  //対象組(カンマで接続)
            if (grade == null) grade = "03";
            _grade = grade;

            _output1 = request.getParameter("OUTPUT1");                 //表紙印刷
            _output2 = request.getParameter("OUTPUT2");                 //名簿印刷
            _mikomi = request.getParameter("MIKOMI");                  //卒業見込み出力の選択
        }
    }

}//クラスの括り
