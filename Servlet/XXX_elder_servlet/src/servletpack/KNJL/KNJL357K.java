// kanji=漢字
/*
 * $Id: 77aa2152dff40613f3ee04d911bce09686f4f014 $
 *
 * 作成日: 2006/01/04 11:25:40 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３５７Ｋ＞  前・後期重複受験者　平均点比較表
 *
 *  2006/01/04 m-yama 作成日
 *  2006/01/17  NO001 m-yama    SQLでの合否区分の判定にVALUEを使用する。
 *  2006/10/24  NO002 m-yama    条件に生年月日を追加。
 * @author m-yama
 * @version $Id: 77aa2152dff40613f3ee04d911bce09686f4f014 $
 */

public class KNJL357K {


    private static final Log log = LogFactory.getLog(KNJL357K.class);

    StringBuffer zenkiKamokuBoy = new StringBuffer();
    StringBuffer zenkiKamokuGil = new StringBuffer();
    StringBuffer zenkiKamoku    = new StringBuffer();
    StringBuffer zenki4KamokuBoy = new StringBuffer();
    StringBuffer zenki4KamokuGil = new StringBuffer();
    StringBuffer zenki4Kamoku    = new StringBuffer();
    StringBuffer zenkiSyakaiBoy = new StringBuffer();
    StringBuffer zenkiSyakaiGil = new StringBuffer();
    StringBuffer zenkiSyakai    = new StringBuffer();
    StringBuffer zenkiRikaBoy = new StringBuffer();
    StringBuffer zenkiRikaGil = new StringBuffer();
    StringBuffer zenkiRika    = new StringBuffer();
    StringBuffer zenkiAracaltBoy = new StringBuffer();
    StringBuffer zenkiAracaltGil = new StringBuffer();
    StringBuffer zenkiAracalt    = new StringBuffer();

    StringBuffer koukiKamokuBoy = new StringBuffer();
    StringBuffer koukiKamokuGil = new StringBuffer();
    StringBuffer koukiKamoku    = new StringBuffer();
    StringBuffer kouki4KamokuBoy = new StringBuffer();
    StringBuffer kouki4KamokuGil = new StringBuffer();
    StringBuffer kouki4Kamoku    = new StringBuffer();
    StringBuffer koukiSyakaiBoy = new StringBuffer();
    StringBuffer koukiSyakaiGil = new StringBuffer();
    StringBuffer koukiSyakai    = new StringBuffer();
    StringBuffer koukiRikaBoy = new StringBuffer();
    StringBuffer koukiRikaGil = new StringBuffer();
    StringBuffer koukiRika    = new StringBuffer();
    StringBuffer koukiAracaltBoy = new StringBuffer();
    StringBuffer koukiAracaltGil = new StringBuffer();
    StringBuffer koukiAracalt    = new StringBuffer();

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[12];

    //  パラメータの取得
        try {
            param[0]  = request.getParameter("YEAR");           //次年度
            param[1]  = request.getParameter("JHFLG");          //中学/高校フラグ 1:中学,2:高校
            param[11] = request.getParameter("SPECIAL_REASON_DIV"); // 特別理由
        } catch( Exception ex ) {
            log.warn("parameter error!",ex);
        }

    //  print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

    //  svf設定
        svf.VrInit();                           //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());           //PDFファイル名の設定

    //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!",ex);
            return;
        }


    //  ＳＶＦ作成処理
        boolean nonedata = false;                               //該当データなしフラグ

        getHeaderData(db2,svf,param);                           //ヘッダーデータ抽出メソッド

for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);

        //SVF出力

        if( printMain(db2,svf,param) ) nonedata = true;

log.debug("nonedata="+nonedata);

    //  該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }

    //  終了処理
        svf.VrQuit();
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り


    /**ヘッダーデータを抽出*/
    private void getHeaderData(DB2UDB db2,Vrw32alp svf,String param[]){

        //年度
        try {
            param[2] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";
        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

    }//getHeaderData()の括り

    public void setInfluenceName(DB2UDB db2, Vrw32alp svf, String[] param) {
        PreparedStatement ps;
        ResultSet rs;
        try {
            ps = db2.prepareStatement("SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2 = '" + param[11] + "' ");
            rs = ps.executeQuery();
            while (rs.next()) {
                String name2 = rs.getString("NAME2");
                svf.VrsOut("VIRUS", name2);
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            log.error(e);
        } finally {
            db2.commit();
        }
    }
    
    
    /**印刷処理メイン*/
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;

        try {
            svf.VrSetForm("KNJL357.frm", 1);
            if( printMeisai(db2,svf,param) ) nonedata = true;
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }

        return nonedata;

    }//printMain()の括り

    /**明細データ印刷処理*/
    private boolean printMeisai(DB2UDB db2,Vrw32alp svf,String param[])
    {
        boolean nonedata = false;
        int examboy  = 0;
        int examgirl = 0;
        int zenkiSubclassFlg = 0;
        int koukiSubclassFlg = 0;
        String comma = "";
        try {
            db2.query(passMeisai(param));
            ResultSet rs = db2.getResultSet();
            makeSqlInProcess(" ( ", false);
            while( rs.next() ){
                //後期の性別で集計する。
                if (rs.getString("KOUKI_SEX").equals("2")){
                    examgirl++;
                }else {
                    examboy++;
                }
                zenkiSubclassFlg = rs.getInt("ZENKI_KOKUGO_FLG")+rs.getInt("ZENKI_SANSUU_FLG")+rs.getInt("ZENKI_SYAKAI_FLG")+rs.getInt("ZENKI_RIKA_FLG");
                koukiSubclassFlg = rs.getInt("KOUKI_KOKUGO_FLG")+rs.getInt("KOUKI_SANSUU_FLG")+rs.getInt("KOUKI_SYAKAI_FLG")+rs.getInt("KOUKI_RIKA_FLG");
                makeSqlInSentence(rs,comma,zenkiSubclassFlg,koukiSubclassFlg);
                comma = ",";
                nonedata = true;
            }
            makeSqlInProcess(" ) ", true);
            //ヘッダデータをセット
            printHead(db2,svf,param,examboy,examgirl);

            //科目データをセット(前期)
            for (int i = 1;i < 5;i++){
                printSubclass(db2,svf,param,i,"1");
            }

            //4科目データをセット(前期)
            printTotal(db2,svf,param,"1","","('1','2','3','4')",1,zenki4KamokuBoy.toString(),zenki4KamokuGil.toString(),zenki4Kamoku.toString());

            //社会型データをセット(前期)
            printTotal(db2,svf,param,"1","","('1','2','3')",2,zenkiSyakaiBoy.toString(),zenkiSyakaiGil.toString(),zenkiSyakai.toString());

            //理科型データをセット(前期)
            printTotal(db2,svf,param,"1","","('1','2','4')",3,zenkiRikaBoy.toString(),zenkiRikaGil.toString(),zenkiRika.toString());

            //アラカルト型データをセット(前期)
            printTotal(db2,svf,param,"1","1","('1','2','4')",4,zenkiRikaBoy.toString(),zenkiRikaGil.toString(),zenkiRika.toString());

            //科目データをセット(後期)
            for (int i = 1;i < 5;i++){
                printSubclass(db2,svf,param,i,"2");
            }

            //4科目データをセット(後期)
            printTotal(db2,svf,param,"2","","('1','2','3','4')",1,kouki4KamokuBoy.toString(),kouki4KamokuGil.toString(),kouki4Kamoku.toString());

            //社会型データをセット(後期)
            printTotal(db2,svf,param,"2","","('1','2','3')",2,koukiSyakaiBoy.toString(),koukiSyakaiGil.toString(),koukiSyakai.toString());

            //理科型データをセット(後期)
            printTotal(db2,svf,param,"2","","('1','2','4')",3,koukiRikaBoy.toString(),koukiRikaGil.toString(),koukiRika.toString());

            //アラカルト型データをセット(後期)
            printTotal(db2,svf,param,"2","1","('1','2','4')",4,koukiRikaBoy.toString(),koukiRikaGil.toString(),koukiRika.toString());

            svf.VrEndPage();

            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMeisai read error!",ex);
        }
        return nonedata;

    }//printMeisai()の括り

    private String getSetData(final String checkData, final String setData, final boolean endFlg) {

        String rtnSt = setData;
        if (endFlg && checkData.equals(" ( ")) {
            rtnSt = " '') ";
        }

        return rtnSt;
    }

    /**IN分の作成(先頭文)*/
    private void makeSqlInProcess(String setData, final boolean endFlg)
    {
        try {
            zenkiKamokuBoy.append(getSetData(zenkiKamokuBoy.toString(), setData, endFlg));
            zenkiKamokuGil.append(getSetData(zenkiKamokuGil.toString(), setData, endFlg));
            zenkiKamoku.append(getSetData(zenkiKamoku.toString(), setData, endFlg));
            zenki4KamokuBoy.append(getSetData(zenki4KamokuBoy.toString(), setData, endFlg));
            zenki4KamokuGil.append(getSetData(zenki4KamokuGil.toString(), setData, endFlg));
            zenki4Kamoku.append(getSetData(zenki4Kamoku.toString(), setData, endFlg));
            zenkiSyakaiBoy.append(getSetData(zenkiSyakaiBoy.toString(), setData, endFlg));
            zenkiSyakaiGil.append(getSetData(zenkiSyakaiGil.toString(), setData, endFlg));
            zenkiSyakai.append(getSetData(zenkiSyakai.toString(), setData, endFlg));
            zenkiRikaBoy.append(getSetData(zenkiRikaBoy.toString(), setData, endFlg));
            zenkiRikaGil.append(getSetData(zenkiRikaGil.toString(), setData, endFlg));
            zenkiRika.append(getSetData(zenkiRika.toString(), setData, endFlg));
            zenkiAracaltBoy.append(getSetData(zenkiAracaltBoy.toString(), setData, endFlg));
            zenkiAracaltGil.append(getSetData(zenkiAracaltGil.toString(), setData, endFlg));
            zenkiAracalt.append(getSetData(zenkiAracalt.toString(), setData, endFlg));

            koukiKamokuBoy.append(getSetData(koukiKamokuBoy.toString(), setData, endFlg));
            koukiKamokuGil.append(getSetData(koukiKamokuGil.toString(), setData, endFlg));
            koukiKamoku.append(getSetData(koukiKamoku.toString(), setData, endFlg));
            kouki4KamokuBoy.append(getSetData(kouki4KamokuBoy.toString(), setData, endFlg));
            kouki4KamokuGil.append(getSetData(kouki4KamokuGil.toString(), setData, endFlg));
            kouki4Kamoku.append(getSetData(kouki4Kamoku.toString(), setData, endFlg));
            koukiSyakaiBoy.append(getSetData(koukiSyakaiBoy.toString(), setData, endFlg));
            koukiSyakaiGil.append(getSetData(koukiSyakaiGil.toString(), setData, endFlg));
            koukiSyakai.append(getSetData(koukiSyakai.toString(), setData, endFlg));
            koukiRikaBoy.append(getSetData(koukiRikaBoy.toString(), setData, endFlg));
            koukiRikaGil.append(getSetData(koukiRikaGil.toString(), setData, endFlg));
            koukiRika.append(getSetData(koukiRika.toString(), setData, endFlg));
            koukiAracaltBoy.append(getSetData(koukiAracaltBoy.toString(), setData, endFlg));
            koukiAracaltGil.append(getSetData(koukiAracaltGil.toString(), setData, endFlg));
            koukiAracalt.append(getSetData(koukiAracalt.toString(), setData, endFlg));

        } catch( Exception ex ) {
            log.warn("makeSqlInProcess read error!",ex);
        }
    }//makeSqlInProcess()の括り

    /**ヘッダデータをセット*/
    private void makeSqlInSentence(ResultSet rs,String comma,int zenkiSubclassFlg,int koukiSubclassFlg)
    {
        String comma2 = "";
        try {

            zenkiKamoku.append(setSqlInSentenceZenki(rs,comma,zenkiSubclassFlg,9));
            comma2 = setComma(zenki4Kamoku.toString());
            zenki4Kamoku.append(setSqlInSentenceZenki(rs,comma2,zenkiSubclassFlg,1111));
            comma2 = setComma(zenkiSyakai.toString());
            zenkiSyakai.append(setSqlInSentenceZenki(rs,comma2,zenkiSubclassFlg,1110));
            if (rs.getString("ZENKI_RIKA_FLG").equals("1")){
                comma2 = setComma(zenkiRika.toString());
                zenkiRika.append(setSqlInSentenceZenki(rs,comma2,zenkiSubclassFlg,1101));
            }
            comma2 = setComma(zenkiAracalt.toString());
            zenkiAracalt.append(setSqlInSentenceZenki(rs,comma2,zenkiSubclassFlg,1101));

            if (rs.getString("ZENKI_SEX").equals("2")){
                comma2 = setComma(zenkiKamokuGil.toString());
                zenkiKamokuGil.append(setSqlInSentenceZenki(rs,comma2,zenkiSubclassFlg,9));
                comma2 = setComma(zenki4KamokuGil.toString());
                zenki4KamokuGil.append(setSqlInSentenceZenki(rs,comma2,zenkiSubclassFlg,1111));
                comma2 = setComma(zenkiSyakaiGil.toString());
                zenkiSyakaiGil.append(setSqlInSentenceZenki(rs,comma2,zenkiSubclassFlg,1110));
                if (rs.getString("ZENKI_RIKA_FLG").equals("1")){
                    comma2 = setComma(zenkiRikaGil.toString());
                    zenkiRikaGil.append(setSqlInSentenceZenki(rs,comma2,zenkiSubclassFlg,1101));
                }
                comma2 = setComma(zenkiAracaltGil.toString());
                zenkiAracaltGil.append(setSqlInSentenceZenki(rs,comma2,zenkiSubclassFlg,1110));
            }else {
                comma2 = setComma(zenkiKamokuBoy.toString());
                zenkiKamokuBoy.append(setSqlInSentenceZenki(rs,comma2,zenkiSubclassFlg,9));
                comma2 = setComma(zenki4KamokuBoy.toString());
                zenki4KamokuBoy.append(setSqlInSentenceZenki(rs,comma2,zenkiSubclassFlg,1111));
                comma2 = setComma(zenkiSyakaiBoy.toString());
                zenkiSyakaiBoy.append(setSqlInSentenceZenki(rs,comma2,zenkiSubclassFlg,1110));
                if (rs.getString("ZENKI_RIKA_FLG").equals("1")){
                    comma2 = setComma(zenkiRikaBoy.toString());
                    zenkiRikaBoy.append(setSqlInSentenceZenki(rs,comma2,zenkiSubclassFlg,1101));
                }
                comma2 = setComma(zenkiAracaltBoy.toString());
                zenkiAracaltBoy.append(setSqlInSentenceZenki(rs,comma2,zenkiSubclassFlg,1110));
            }


            koukiKamoku.append(setSqlInSentenceKouki(rs,comma,koukiSubclassFlg,9));
            comma2 = setComma(kouki4Kamoku.toString());
            kouki4Kamoku.append(setSqlInSentenceKouki(rs,comma2,koukiSubclassFlg,1111));
            comma2 = setComma(koukiSyakai.toString());
            koukiSyakai.append(setSqlInSentenceKouki(rs,comma2,koukiSubclassFlg,1110));
            if (rs.getString("KOUKI_RIKA_FLG").equals("1")){
                comma2 = setComma(koukiRika.toString());
                koukiRika.append(setSqlInSentenceKouki(rs,comma2,koukiSubclassFlg,1101));
            }
            comma2 = setComma(koukiAracalt.toString());
            koukiAracalt.append(setSqlInSentenceKouki(rs,comma2,koukiSubclassFlg,1101));

            if (rs.getString("KOUKI_SEX").equals("2")){
                comma2 = setComma(koukiKamokuGil.toString());
                koukiKamokuGil.append(setSqlInSentenceKouki(rs,comma2,koukiSubclassFlg,9));
                comma2 = setComma(kouki4KamokuGil.toString());
                kouki4KamokuGil.append(setSqlInSentenceKouki(rs,comma2,koukiSubclassFlg,1111));
                comma2 = setComma(koukiSyakaiGil.toString());
                koukiSyakaiGil.append(setSqlInSentenceKouki(rs,comma2,koukiSubclassFlg,1110));
                if (rs.getString("KOUKI_RIKA_FLG").equals("1")){
                    comma2 = setComma(koukiRikaGil.toString());
                    koukiRikaGil.append(setSqlInSentenceKouki(rs,comma2,koukiSubclassFlg,1101));
                }
                comma2 = setComma(koukiAracaltGil.toString());
                koukiAracaltGil.append(setSqlInSentenceKouki(rs,comma2,koukiSubclassFlg,1110));
            }else {
                comma2 = setComma(koukiKamokuBoy.toString());
                koukiKamokuBoy.append(setSqlInSentenceKouki(rs,comma2,koukiSubclassFlg,9));
                comma2 = setComma(kouki4KamokuBoy.toString());
                kouki4KamokuBoy.append(setSqlInSentenceKouki(rs,comma2,koukiSubclassFlg,1111));
                comma2 = setComma(koukiSyakaiBoy.toString());
                koukiSyakaiBoy.append(setSqlInSentenceKouki(rs,comma2,koukiSubclassFlg,1110));
                if (rs.getString("KOUKI_RIKA_FLG").equals("1")){
                    comma2 = setComma(koukiRikaBoy.toString());
                    koukiRikaBoy.append(setSqlInSentenceKouki(rs,comma2,koukiSubclassFlg,1101));
                }
                comma2 = setComma(koukiAracaltBoy.toString());
                koukiAracaltBoy.append(setSqlInSentenceKouki(rs,comma2,koukiSubclassFlg,1110));
            }

        } catch( Exception ex ) {
            log.warn("makeSqlInSentence read error!",ex);
        }

    }//makeSqlInSentence()の括り

    /**SQL作成前期*/
    private String setComma(String commaData)
    {
        String setVal = "";
        try {
            if (commaData.equals(" ( ")){
                setVal = "";
            }else {
                setVal = ",";
            }
        } catch( Exception ex ) {
            log.warn("setComma read error!",ex);
        }

        return setVal;

    }//setComma()の括り

    /**SQL作成前期*/
    private String setSqlInSentenceZenki(ResultSet rs,String comma,int subclassFlg,int flgVal)
    {
        StringBuffer sqlSb = new StringBuffer();
        try {
            if (flgVal == 9){
                sqlSb.append(comma);
                sqlSb.append("'").append(rs.getString("ZENKI_EXAM")).append("'");
            }else {
                if (subclassFlg >= flgVal){
                    sqlSb.append(comma);
                    sqlSb.append("'").append(rs.getString("ZENKI_EXAM")).append("'");
                }
            }
        } catch( Exception ex ) {
            log.warn("setSqlInSentenceZenki read error!",ex);
        }

        return sqlSb.toString();

    }//setSqlInSentenceZenki()の括り

    /**SQL作成後期*/
    private String setSqlInSentenceKouki(ResultSet rs,String comma,int subclassFlg,int flgVal)
    {
        StringBuffer sqlSb = new StringBuffer();
        try {
            if (flgVal == 9){
                sqlSb.append(comma);
                sqlSb.append("'").append(rs.getString("KOUKI_EXAM")).append("'");
            }else {
                if (subclassFlg >= flgVal){
                    sqlSb.append(comma);
                    sqlSb.append("'").append(rs.getString("KOUKI_EXAM")).append("'");
                }
            }
        } catch( Exception ex ) {
            log.warn("setSqlInSentenceKouki read error!",ex);
        }

        return sqlSb.toString();

    }//setSqlInSentenceKouki()の括り

    /**ヘッダデータをセット*/
    private void printHead(DB2UDB db2,Vrw32alp svf,String param[],int examboy,int examgirl)
    {
        try {

            svf.VrsOut("NENDO"          , param[2] );
            svf.VrsOut("TOTAL"          , String.valueOf(examboy+examgirl) );
            svf.VrsOut("BOY"                , String.valueOf(examboy) );
            svf.VrsOut("GIRL"               , String.valueOf(examgirl) );
            setInfluenceName(db2, svf, param);
        } catch( Exception ex ) {
            log.warn("printHead read error!",ex);
        }

    }//printHead()の括り

    /**科目データをセット*/
    private void printSubclass(DB2UDB db2,Vrw32alp svf,String param[],int subclassCd,String zenkoukiFlg)
    {
        int gyo = 1;
        try {
            db2.query(sqlSubclassMeisai(param,subclassCd,zenkoukiFlg));
            ResultSet rs = db2.getResultSet();
            while( rs.next() ){
                if (zenkoukiFlg.equals("1")){
                    svf.VrsOutn("SUBCLASS_AVE1_"+rs.getString("TESTSUBCLASSCD") ,gyo    ,rs.getString("SUBCLASSAVG") );
                }else {
                    svf.VrsOutn("SUBCLASS_AVE2_"+rs.getString("TESTSUBCLASSCD") ,gyo    ,rs.getString("SUBCLASSAVG") );
                }
                gyo++;
            }

            rs.close();
            db2.commit();

        } catch( Exception ex ) {
            log.warn("printSubclass read error!",ex);
        }

    }//printSubclass()の括り

    /**科目データをセット*/
    private void printTotal(DB2UDB db2,Vrw32alp svf,String param[],String zenkoukiFlg,String aracaltFlg,
                            String subclassCd,int fieldNo,String sqlInBoy,String sqlInGil,String sqlIn)
    {
        int gyo = 1;
        try {
            if (aracaltFlg.equals("")){
                db2.query(sqlTotal(param,subclassCd,zenkoukiFlg,sqlInBoy,sqlInGil,sqlIn));
            }else {
                db2.query(sqlAracalt(param,"('1','2')","('3','4')",zenkoukiFlg,sqlInBoy,sqlInGil,sqlIn));
            }
            ResultSet rs = db2.getResultSet();
            while( rs.next() ){
                if (zenkoukiFlg.equals("1")){
                    svf.VrsOutn("TOTAL_AVE1_"+fieldNo   ,gyo    ,rs.getString("SUBCLASSAVG") );
                    if (null != rs.getString("SUBCLASSAVG")) {
                        if (fieldNo == 1){
                            svf.VrsOutn("AVERAGE1_"+fieldNo ,gyo    ,Double.toString(Double.parseDouble(Long.toString((Math.round(Double.parseDouble(rs.getString("SUBCLASSAVG"))*10/4)*10)/10))/10));
                        }else {
                            svf.VrsOutn("AVERAGE1_"+fieldNo ,gyo    ,Double.toString(Double.parseDouble(Long.toString((Math.round(Double.parseDouble(rs.getString("SUBCLASSAVG"))*10/3)*10)/10))/10));
                        }
                    }
                }else {
                    svf.VrsOutn("TOTAL_AVE2_"+fieldNo   ,gyo    ,rs.getString("SUBCLASSAVG") );
                    if (null != rs.getString("SUBCLASSAVG")) {
                        if (fieldNo == 1){
                            svf.VrsOutn("AVERAGE2_"+fieldNo ,gyo    ,Double.toString(Double.parseDouble(Long.toString((Math.round(Double.parseDouble(rs.getString("SUBCLASSAVG"))*10/4)*10)/10))/10));
                        }else {
                            svf.VrsOutn("AVERAGE2_"+fieldNo ,gyo    ,Double.toString(Double.parseDouble(Long.toString((Math.round(Double.parseDouble(rs.getString("SUBCLASSAVG"))*10/3)*10)/10))/10));
                        }
                    }
                }
                gyo++;
            }

            rs.close();
            db2.commit();

        } catch( Exception ex ) {
            log.warn("printTotal read error!",ex);
        }

    }//printTotal()の括り

    /**
     *  対象データを抽出
     *
     */
    private String passMeisai(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH EXAM_ZENKI AS ( ");
            stb.append("SELECT ");
            stb.append("    * ");
            stb.append("FROM ");
            stb.append("    ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("WHERE ");
            stb.append("    ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[11])) {
                stb.append("    AND SPECIAL_REASON_DIV = '" + param[11] + "' ");
            }
            stb.append("    AND TESTDIV = '1' ");
            stb.append("    AND EXAMNO NOT BETWEEN '3000' AND '3999' ");
            stb.append("    AND VALUE(JUDGEMENT,'88') NOT IN ('8') ");   //NO001
            stb.append("), EXAM_KOUKI AS ( ");
            stb.append("SELECT ");
            stb.append("    * ");
            stb.append("FROM ");
            stb.append("    ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("WHERE ");
            stb.append("    ENTEXAMYEAR = '"+param[0]+"' ");
            if (!"9".equals(param[11])) {
                stb.append("    AND SPECIAL_REASON_DIV = '" + param[11] + "' ");
            }
            stb.append("    AND TESTDIV = '2' ");
            stb.append("    AND EXAMNO NOT BETWEEN '3000' AND '3999' ");
            stb.append("    AND VALUE(JUDGEMENT,'88') NOT IN ('8') ");   //NO001
            stb.append(") ");
            stb.append("SELECT ");
            stb.append("        t1.EXAMNO ZENKI_EXAM, ");
            stb.append("        VALUE(t1.SEX, '0') ZENKI_SEX, ");
            stb.append("        t3.A_SCORE ZENKI_KOKUGO, ");
            stb.append("        t4.A_SCORE ZENKI_SANSUU, ");
            stb.append("        t5.A_SCORE ZENKI_SYAKAI, ");
            stb.append("        t6.A_SCORE ZENKI_RIKA, ");
            stb.append("        CASE WHEN t3.A_SCORE IS NOT NULL THEN '1000' ELSE '0000' END ZENKI_KOKUGO_FLG, ");
            stb.append("        CASE WHEN t4.A_SCORE IS NOT NULL THEN '100' ELSE '000' END ZENKI_SANSUU_FLG, ");
            stb.append("        CASE WHEN t5.A_SCORE IS NOT NULL THEN '10' ELSE '00' END ZENKI_SYAKAI_FLG, ");
            stb.append("        CASE WHEN t6.A_SCORE IS NOT NULL THEN '1' ELSE '0' END ZENKI_RIKA_FLG, ");
            stb.append("        t2.EXAMNO KOUKI_EXAM, ");
            stb.append("        VALUE(t2.SEX, '0') KOUKI_SEX, ");
            stb.append("        t7.A_SCORE KOUKI_KOKUGO, ");
            stb.append("        t8.A_SCORE KOUKI_SANSUU, ");
            stb.append("        t9.A_SCORE KOUKI_SYAKAI, ");
            stb.append("        t10.A_SCORE KOUKI_RIKA, ");
            stb.append("        CASE WHEN t7.A_SCORE IS NOT NULL THEN '1000' ELSE '0000' END KOUKI_KOKUGO_FLG, ");
            stb.append("        CASE WHEN t8.A_SCORE IS NOT NULL THEN '100' ELSE '000' END KOUKI_SANSUU_FLG, ");
            stb.append("        CASE WHEN t9.A_SCORE IS NOT NULL THEN '10' ELSE '00' END KOUKI_SYAKAI_FLG, ");
            stb.append("        CASE WHEN t10.A_SCORE IS NOT NULL THEN '1' ELSE '0' END KOUKI_RIKA_FLG ");
            stb.append("FROM ");
            stb.append("    EXAM_ZENKI t1 ");
            stb.append("    LEFT JOIN ENTEXAM_SCORE_DAT t3 ON t3.ENTEXAMYEAR = t1.ENTEXAMYEAR ");
            stb.append("    AND t3.TESTDIV = t1.TESTDIV ");
            stb.append("    AND t3.EXAMNO = t1.EXAMNO ");
            stb.append("    AND t3.TESTSUBCLASSCD = '1' ");
            stb.append("    LEFT JOIN ENTEXAM_SCORE_DAT t4 ON t4.ENTEXAMYEAR = t1.ENTEXAMYEAR ");
            stb.append("    AND t4.TESTDIV = t1.TESTDIV ");
            stb.append("    AND t4.EXAMNO = t1.EXAMNO ");
            stb.append("    AND t4.TESTSUBCLASSCD = '2' ");
            stb.append("    LEFT JOIN ENTEXAM_SCORE_DAT t5 ON t5.ENTEXAMYEAR = t1.ENTEXAMYEAR ");
            stb.append("    AND t5.TESTDIV = t1.TESTDIV ");
            stb.append("    AND t5.EXAMNO = t1.EXAMNO ");
            stb.append("    AND t5.TESTSUBCLASSCD = '3' ");
            stb.append("    LEFT JOIN ENTEXAM_SCORE_DAT t6 ON t6.ENTEXAMYEAR = t1.ENTEXAMYEAR ");
            stb.append("    AND t6.TESTDIV = t1.TESTDIV ");
            stb.append("    AND t6.EXAMNO = t1.EXAMNO ");
            stb.append("    AND t6.TESTSUBCLASSCD = '4', ");
            stb.append("    EXAM_KOUKI t2 ");
            stb.append("    LEFT JOIN ENTEXAM_SCORE_DAT t7 ON t7.ENTEXAMYEAR = t2.ENTEXAMYEAR ");
            stb.append("    AND t7.TESTDIV = t2.TESTDIV ");
            stb.append("    AND t7.EXAMNO = t2.EXAMNO ");
            stb.append("    AND t7.TESTSUBCLASSCD = '1' ");
            stb.append("    LEFT JOIN ENTEXAM_SCORE_DAT t8 ON t8.ENTEXAMYEAR = t2.ENTEXAMYEAR ");
            stb.append("    AND t8.TESTDIV = t2.TESTDIV ");
            stb.append("    AND t8.EXAMNO = t2.EXAMNO ");
            stb.append("    AND t8.TESTSUBCLASSCD = '2' ");
            stb.append("    LEFT JOIN ENTEXAM_SCORE_DAT t9 ON t9.ENTEXAMYEAR = t2.ENTEXAMYEAR ");
            stb.append("    AND t9.TESTDIV = t2.TESTDIV ");
            stb.append("    AND t9.EXAMNO = t2.EXAMNO ");
            stb.append("    AND t9.TESTSUBCLASSCD = '3' ");
            stb.append("    LEFT JOIN ENTEXAM_SCORE_DAT t10 ON t10.ENTEXAMYEAR = t2.ENTEXAMYEAR ");
            stb.append("    AND t10.TESTDIV = t2.TESTDIV ");
            stb.append("    AND t10.EXAMNO = t2.EXAMNO ");
            stb.append("    AND t10.TESTSUBCLASSCD = '4' ");
            stb.append("WHERE ");
            stb.append("    t1.NAME = t2.NAME ");
            stb.append("    AND t1.NAME_KANA = t2.NAME_KANA ");
            stb.append("    AND t1.BIRTHDAY = t2.BIRTHDAY ");   //NO002

//log.debug(stb);
        } catch( Exception e ){
            log.warn("statementMeisai error!",e);
        }
        return stb.toString();

    }//statementMeisai()の括り

    /**
     *  科目毎データを抽出
     *
     */
    private String sqlSubclassMeisai(String param[],int subclassCd,String zenkoukiFlg)
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT ");
            stb.append("    '1' AS SORT, ");
            stb.append("    TESTSUBCLASSCD, ");
            if (zenkoukiFlg.equals("2") && (subclassCd == 3 || subclassCd == 4)){
                stb.append("    DECIMAL(ROUND(AVG(FLOAT(B_SCORE))*10,0)/10,5,1) AS SUBCLASSAVG ");
            }else {
                stb.append("    DECIMAL(ROUND(AVG(FLOAT(A_SCORE))*10,0)/10,5,1) AS SUBCLASSAVG ");
            }
            stb.append("FROM ");
            stb.append("    ENTEXAM_SCORE_DAT ");
            stb.append("WHERE ");
            stb.append("    ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    AND TESTDIV = '"+zenkoukiFlg+"' ");
            if (zenkoukiFlg.equals("1")){
                stb.append("    AND EXAMNO IN "+zenkiKamokuBoy+" ");
            }else {
                stb.append("    AND EXAMNO IN "+koukiKamokuBoy+" ");
            }
            stb.append("    AND TESTSUBCLASSCD = '"+subclassCd+"' ");
            stb.append("GROUP BY ");
            stb.append("    TESTSUBCLASSCD ");
            stb.append("UNION ");
            stb.append("SELECT ");
            stb.append("    '2' AS SORT, ");
            stb.append("    TESTSUBCLASSCD, ");
            if (zenkoukiFlg.equals("2") && (subclassCd == 3 || subclassCd == 4)){
                stb.append("    DECIMAL(ROUND(AVG(FLOAT(B_SCORE))*10,0)/10,5,1) AS SUBCLASSAVG ");
            }else {
                stb.append("    DECIMAL(ROUND(AVG(FLOAT(A_SCORE))*10,0)/10,5,1) AS SUBCLASSAVG ");
            }
            stb.append("FROM ");
            stb.append("    ENTEXAM_SCORE_DAT ");
            stb.append("WHERE ");
            stb.append("    ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    AND TESTDIV = '"+zenkoukiFlg+"' ");
            if (zenkoukiFlg.equals("1")){
                stb.append("    AND EXAMNO IN "+zenkiKamokuGil+" ");
            }else {
                stb.append("    AND EXAMNO IN "+koukiKamokuGil+" ");
            }
            stb.append("    AND TESTSUBCLASSCD = '"+subclassCd+"' ");
            stb.append("GROUP BY ");
            stb.append("    TESTSUBCLASSCD ");
            stb.append("UNION ");
            stb.append("SELECT ");
            stb.append("    '3' AS SORT, ");
            stb.append("    TESTSUBCLASSCD, ");
            if (zenkoukiFlg.equals("2") && (subclassCd == 3 || subclassCd == 4)){
                stb.append("    DECIMAL(ROUND(AVG(FLOAT(B_SCORE))*10,0)/10,5,1) AS SUBCLASSAVG ");
            }else {
                stb.append("    DECIMAL(ROUND(AVG(FLOAT(A_SCORE))*10,0)/10,5,1) AS SUBCLASSAVG ");
            }
            stb.append("FROM ");
            stb.append("    ENTEXAM_SCORE_DAT ");
            stb.append("WHERE ");
            stb.append("    ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    AND TESTDIV = '"+zenkoukiFlg+"' ");
            if (zenkoukiFlg.equals("1")){
                stb.append("    AND EXAMNO IN "+zenkiKamoku+" ");
            }else {
                stb.append("    AND EXAMNO IN "+koukiKamoku+" ");
            }
            stb.append("    AND TESTSUBCLASSCD = '"+subclassCd+"' ");
            stb.append("GROUP BY ");
            stb.append("    TESTSUBCLASSCD ");
            stb.append("ORDER BY ");
            stb.append("    SORT ");

//log.debug(stb);
        } catch( Exception e ){
            log.warn("sqlSubclassMeisai error!",e);
        }
        return stb.toString();

    }//sqlSubclassMeisai()の括り

    /**
     *  科目型データを抽出
     *
     */
    private String sqlTotal(String param[],String subclassCd,String zenkoukiFlg,
                            String sqlInBoy,String sqlInGil,String sqlIn)
    {
        StringBuffer stb = new StringBuffer();
        try {

            stb.append("WITH TOTALSUB AS ( ");
            stb.append("SELECT ");
            stb.append("    EXAMNO, ");
            stb.append("    '1' AS FLG, ");
            if (zenkoukiFlg.equals("2")){
                stb.append("    SUM(B_SCORE) AS TOTAL ");
            }else {
                stb.append("    SUM(A_SCORE) AS TOTAL ");
            }
            stb.append("FROM ");
            stb.append("    ENTEXAM_SCORE_DAT ");
            stb.append("WHERE ");
            stb.append("    ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    AND TESTDIV = '"+zenkoukiFlg+"' ");
            stb.append("    AND EXAMNO IN "+sqlInBoy+" ");
            stb.append("    AND TESTSUBCLASSCD IN "+subclassCd+" ");
            stb.append("GROUP BY ");
            stb.append("    EXAMNO ");
            stb.append("UNION ");
            stb.append("SELECT ");
            stb.append("    EXAMNO, ");
            stb.append("    '2' AS FLG, ");
            if (zenkoukiFlg.equals("2")){
                stb.append("    SUM(B_SCORE) AS TOTAL ");
            }else {
                stb.append("    SUM(A_SCORE) AS TOTAL ");
            }
            stb.append("FROM ");
            stb.append("    ENTEXAM_SCORE_DAT ");
            stb.append("WHERE ");
            stb.append("    ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    AND TESTDIV = '"+zenkoukiFlg+"' ");
            stb.append("    AND EXAMNO IN "+sqlInGil+" ");
            stb.append("    AND TESTSUBCLASSCD IN "+subclassCd+" ");
            stb.append("GROUP BY ");
            stb.append("    EXAMNO ");
            stb.append("UNION ");
            stb.append("SELECT ");
            stb.append("    EXAMNO, ");
            stb.append("    '3' AS FLG, ");
            if (zenkoukiFlg.equals("2")){
                stb.append("    SUM(B_SCORE) AS TOTAL ");
            }else {
                stb.append("    SUM(A_SCORE) AS TOTAL ");
            }
            stb.append("FROM ");
            stb.append("    ENTEXAM_SCORE_DAT ");
            stb.append("WHERE ");
            stb.append("    ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    AND TESTDIV = '"+zenkoukiFlg+"' ");
            stb.append("    AND EXAMNO IN "+sqlIn+" ");
            stb.append("    AND TESTSUBCLASSCD IN "+subclassCd+" ");
            stb.append("GROUP BY ");
            stb.append("    EXAMNO ");

            stb.append("), TOTAL AS ( ");
            stb.append("SELECT ");
            stb.append("    FLG, ");
            stb.append("    SUM(TOTAL) AS TOTAL ");
            stb.append("FROM ");
            stb.append("    TOTALSUB ");
            stb.append("GROUP BY ");
            stb.append("    FLG ");

            stb.append("), MAIN AS ( ");
            stb.append("SELECT ");
            stb.append("    '1' AS SORT, ");
            stb.append("    DECIMAL(ROUND(AVG(FLOAT(TOTAL))*10,0)/10,5,1) AS SUBCLASSAVG ");
            stb.append("FROM ");
            stb.append("    TOTALSUB ");
            stb.append("WHERE ");
            stb.append("    FLG = '1'");
            stb.append("UNION ");
            stb.append("SELECT ");
            stb.append("    '2' AS SORT, ");
            stb.append("    DECIMAL(ROUND(AVG(FLOAT(TOTAL))*10,0)/10,5,1) AS SUBCLASSAVG ");
            stb.append("FROM ");
            stb.append("    TOTALSUB ");
            stb.append("WHERE ");
            stb.append("    FLG = '2'");
            stb.append("UNION ");
            stb.append("SELECT ");
            stb.append("    '3' AS SORT, ");
            stb.append("    DECIMAL(ROUND(AVG(FLOAT(TOTAL))*10,0)/10,5,1) AS SUBCLASSAVG ");
            stb.append("FROM ");
            stb.append("    TOTALSUB ");
            stb.append("WHERE ");
            stb.append("    FLG = '3'");
            stb.append(") ");

            stb.append("SELECT ");
            stb.append("    SORT, ");
            stb.append("    TOTAL, ");
            stb.append("    SUBCLASSAVG ");
            stb.append("FROM ");
            stb.append("    MAIN ");
            stb.append("    LEFT JOIN TOTAL ON FLG = SORT ");
            stb.append("ORDER BY ");
            stb.append("    SORT ");

//log.debug(stb);
        } catch( Exception e ){
            log.warn("sqlTotal error!",e);
        }
        return stb.toString();

    }//sqlTotal()の括り

    /**
     *  アラカルト型データを抽出
     *
     */
    private String sqlAracalt(String param[],String subclassCd,String araclatCd,String zenkoukiFlg,
                            String sqlInBoy,String sqlInGil,String sqlIn)
    {
        StringBuffer stb = new StringBuffer();
        try {

            stb.append("WITH SCORE1 AS ( ");
            stb.append("SELECT ");
            stb.append("    EXAMNO, ");
            stb.append("    '1' AS FLG, ");
            if (zenkoukiFlg.equals("2")){
                stb.append("    MAX(B_SCORE) AS SCORE ");
            }else {
                stb.append("    MAX(A_SCORE) AS SCORE ");
            }
            stb.append("FROM ");
            stb.append("    ENTEXAM_SCORE_DAT ");
            stb.append("WHERE ");
            stb.append("    ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    AND TESTDIV = '"+zenkoukiFlg+"' ");
            stb.append("    AND EXAMNO IN "+sqlInBoy+" ");
            stb.append("    AND TESTSUBCLASSCD IN "+araclatCd+" ");
            stb.append("GROUP BY ");
            stb.append("    EXAMNO ");
            stb.append("UNION ");
            stb.append("SELECT ");
            stb.append("    EXAMNO, ");
            stb.append("    '1' AS FLG, ");
            if (zenkoukiFlg.equals("2")){
                stb.append("    SUM(B_SCORE) AS SCORE ");
            }else {
                stb.append("    SUM(A_SCORE) AS SCORE ");
            }
            stb.append("FROM ");
            stb.append("    ENTEXAM_SCORE_DAT ");
            stb.append("WHERE ");
            stb.append("    ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    AND TESTDIV = '"+zenkoukiFlg+"' ");
            stb.append("    AND EXAMNO IN "+sqlInBoy+" ");
            stb.append("    AND TESTSUBCLASSCD IN "+subclassCd+" ");
            stb.append("GROUP BY ");
            stb.append("    EXAMNO ");

            stb.append("), SCORE2 AS ( ");
            stb.append("SELECT ");
            stb.append("    EXAMNO, ");
            stb.append("    '2' AS FLG, ");
            if (zenkoukiFlg.equals("2")){
                stb.append("    MAX(B_SCORE) AS SCORE ");
            }else {
                stb.append("    MAX(A_SCORE) AS SCORE ");
            }
            stb.append("FROM ");
            stb.append("    ENTEXAM_SCORE_DAT ");
            stb.append("WHERE ");
            stb.append("    ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    AND TESTDIV = '"+zenkoukiFlg+"' ");
            stb.append("    AND EXAMNO IN "+sqlInGil+" ");
            stb.append("    AND TESTSUBCLASSCD IN "+araclatCd+" ");
            stb.append("GROUP BY ");
            stb.append("    EXAMNO ");
            stb.append("UNION ");
            stb.append("SELECT ");
            stb.append("    EXAMNO, ");
            stb.append("    '2' AS FLG, ");
            if (zenkoukiFlg.equals("2")){
                stb.append("    SUM(B_SCORE) AS SCORE ");
            }else {
                stb.append("    SUM(A_SCORE) AS SCORE ");
            }
            stb.append("FROM ");
            stb.append("    ENTEXAM_SCORE_DAT ");
            stb.append("WHERE ");
            stb.append("    ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    AND TESTDIV = '"+zenkoukiFlg+"' ");
            stb.append("    AND EXAMNO IN "+sqlInGil+" ");
            stb.append("    AND TESTSUBCLASSCD IN "+subclassCd+" ");
            stb.append("GROUP BY ");
            stb.append("    EXAMNO ");

            stb.append("), SCORE3 AS ( ");
            stb.append("SELECT ");
            stb.append("    EXAMNO, ");
            stb.append("    '3' AS FLG, ");
            if (zenkoukiFlg.equals("2")){
                stb.append("    MAX(B_SCORE) AS SCORE ");
            }else {
                stb.append("    MAX(A_SCORE) AS SCORE ");
            }
            stb.append("FROM ");
            stb.append("    ENTEXAM_SCORE_DAT ");
            stb.append("WHERE ");
            stb.append("    ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    AND TESTDIV = '"+zenkoukiFlg+"' ");
            stb.append("    AND EXAMNO IN "+sqlIn+" ");
            stb.append("    AND TESTSUBCLASSCD IN "+araclatCd+" ");
            stb.append("GROUP BY ");
            stb.append("    EXAMNO ");
            stb.append("UNION ");
            stb.append("SELECT ");
            stb.append("    EXAMNO, ");
            stb.append("    '3' AS FLG, ");
            if (zenkoukiFlg.equals("2")){
                stb.append("    SUM(B_SCORE) AS SCORE ");
            }else {
                stb.append("    SUM(A_SCORE) AS SCORE ");
            }
            stb.append("FROM ");
            stb.append("    ENTEXAM_SCORE_DAT ");
            stb.append("WHERE ");
            stb.append("    ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("    AND TESTDIV = '"+zenkoukiFlg+"' ");
            stb.append("    AND EXAMNO IN "+sqlIn+" ");
            stb.append("    AND TESTSUBCLASSCD IN "+subclassCd+" ");
            stb.append("GROUP BY ");
            stb.append("    EXAMNO ");

            stb.append("), TOTALSUB AS ( ");
            stb.append("SELECT ");
            stb.append("    EXAMNO, ");
            stb.append("    FLG, ");
            stb.append("    SUM(SCORE) AS TOTAL ");
            stb.append("FROM ");
            stb.append("    SCORE1 ");
            stb.append("GROUP BY ");
            stb.append("    EXAMNO, ");
            stb.append("    FLG ");
            stb.append("UNION ");
            stb.append("SELECT ");
            stb.append("    EXAMNO, ");
            stb.append("    FLG, ");
            stb.append("    SUM(SCORE) AS TOTAL ");
            stb.append("FROM ");
            stb.append("    SCORE2 ");
            stb.append("GROUP BY ");
            stb.append("    EXAMNO, ");
            stb.append("    FLG ");
            stb.append("UNION ");
            stb.append("SELECT ");
            stb.append("    EXAMNO, ");
            stb.append("    FLG, ");
            stb.append("    SUM(SCORE) AS TOTAL ");
            stb.append("FROM ");
            stb.append("    SCORE3 ");
            stb.append("GROUP BY ");
            stb.append("    EXAMNO, ");
            stb.append("    FLG ");

            stb.append("), TOTAL AS ( ");
            stb.append("SELECT ");
            stb.append("    FLG, ");
            stb.append("    SUM(TOTAL) AS TOTAL ");
            stb.append("FROM ");
            stb.append("    TOTALSUB ");
            stb.append("GROUP BY ");
            stb.append("    FLG ");

            stb.append("), MAIN AS ( ");
            stb.append("SELECT ");
            stb.append("    '1' AS SORT, ");
            stb.append("    DECIMAL(ROUND(AVG(FLOAT(TOTAL))*10,0)/10,5,1) AS SUBCLASSAVG ");
            stb.append("FROM ");
            stb.append("    TOTALSUB ");
            stb.append("WHERE ");
            stb.append("    FLG = '1'");
            stb.append("UNION ");
            stb.append("SELECT ");
            stb.append("    '2' AS SORT, ");
            stb.append("    DECIMAL(ROUND(AVG(FLOAT(TOTAL))*10,0)/10,5,1) AS SUBCLASSAVG ");
            stb.append("FROM ");
            stb.append("    TOTALSUB ");
            stb.append("WHERE ");
            stb.append("    FLG = '2'");
            stb.append("UNION ");
            stb.append("SELECT ");
            stb.append("    '3' AS SORT, ");
            stb.append("    DECIMAL(ROUND(AVG(FLOAT(TOTAL))*10,0)/10,5,1) AS SUBCLASSAVG ");
            stb.append("FROM ");
            stb.append("    TOTALSUB ");
            stb.append("WHERE ");
            stb.append("    FLG = '3'");
            stb.append(") ");

            stb.append("SELECT ");
            stb.append("    SORT, ");
            stb.append("    TOTAL, ");
            stb.append("    SUBCLASSAVG ");
            stb.append("FROM ");
            stb.append("    MAIN ");
            stb.append("    LEFT JOIN TOTAL ON FLG = SORT ");
            stb.append("ORDER BY ");
            stb.append("    SORT ");

//log.debug(stb);
        } catch( Exception e ){
            log.warn("sqlAracalt error!",e);
        }
        return stb.toString();

    }//sqlAracalt()の括り

}//クラスの括り
