package servletpack.KNJL;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２３Ｃ＞  各種合格者名簿
 *
 *  2008/11/05 RTS 作成日
 **/

public class KNJL323C {

    private static final Log log = LogFactory.getLog(KNJL323C.class);
    private String[] titles = {"合格者名簿","補欠合格者名簿","移行合格者名簿", null};
    private Param _param;
    
    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
     
        //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }
        //  パラメータの取得
        _param = new Param(db2, request);

        //  print設定
        response.setContentType("application/pdf");
        
        //  svf設定
        int ret = svf.VrInit();                             //クラスの初期化
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定

        //  ＳＶＦ作成処理
        PreparedStatement ps1 = null;
        PreparedStatement psTotalPage = null;
        boolean nonedata = false;                               //該当データなしフラグ

        //SQL作成
        try {
            for (int j=0; j<_param._selected.length; j++) {
                if (_param._selected[j]==-1) continue;
                int outputSelect = _param._selected[j];
                
                for (int m = 0; m < _param._printPassArray.length; m++) {
                    String printPassSelect = _param._printPassArray[m];
                    if (printPassSelect == null) continue;
                    log.debug("printPassSelect="+printPassSelect);

                    for (int i=0; i<_param._testdivArray.length; i++) {
                        String testDiv = _param._testdivArray[i];
                        
                        String[] shDivPrint = null;
                        if ("9".equals(_param._testdiv)) {
                            if (_param.isGojo() && "1".equals(_param._applicantdiv) || _param.isCollege() && "2".equals(_param._applicantdiv)) {
                                //入試区分毎
                                shDivPrint = new String[]{"9"};
                            } else if (_param.isGojo() || _param.isWakayama() && "2".equals(_param._applicantdiv) && "3".equals(testDiv)) {
                                shDivPrint = new String[]{"1","2"};
                            } else {
                                shDivPrint = new String[]{"1"};
                            }
                        } else {
                            if (_param.isGojo() && "1".equals(_param._applicantdiv) || _param.isCollege() && "2".equals(_param._applicantdiv)) {
                                shDivPrint = new String[]{"9"};
                            } else {
                                shDivPrint = new String[]{_param._shdiv};
                            }
                        }
                        
                        for(int h=0; h<shDivPrint.length; h++) {
                            String shDiv = shDivPrint[h];
                            
                            String[] selSubDivPrint = null;
                            if (_param.isGojo() && "2".equals(_param._applicantdiv) && "7".equals(testDiv)) {
                                selSubDivPrint = new String[]{"7","6","9"}; //7:数学 6:社会
                            } else {
                                selSubDivPrint = new String[]{"9"};
                            }
                            
                            for(int k=0; k<selSubDivPrint.length; k++) {
                                String selSubDiv = selSubDivPrint[k];
                                
                                String sql;
                                sql = preStat1(testDiv, shDiv, selSubDiv, outputSelect, printPassSelect);
                                log.debug("preStat1 sql="+sql);
                                ps1 = db2.prepareStatement(sql);        //受験者一覧preparestatement
                                
                                sql = preStatTotalPage(testDiv, shDiv, selSubDiv, outputSelect, printPassSelect);
                                log.debug("preStateTotalPage sql="+sql);
                                psTotalPage = db2.prepareStatement(sql);        //総ページ数preparestatement
                                
                                //SVF出力
                                if (setSvfMain(db2, svf, ps1, psTotalPage, testDiv, shDiv, selSubDiv, outputSelect, printPassSelect)) nonedata = true;  //帳票出力のメソッド
                            }
                        }
                    }
                }
            }
        } catch( Exception ex ) {
            log.error("DB2 prepareStatement set error!");
        }

        //  該当データ無し
        if( !nonedata ){
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndPage();
        }

        //  終了処理
        ret = svf.VrQuit();
        preStatClose(ps1,psTotalPage);       //preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる

    }//doGetの括り

    /** 事前処理 **/
    private void setHeader(
        DB2UDB db2,
        Vrw32alp svf,
        String testDiv,
        String shDiv,
        String selSubDiv,
        int outputSelect,
        String printPassSelect
    ) {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        if (_param.isCollege()) {
            ret = svf.VrSetForm("2".equals(_param._applicantdiv) ? "KNJL323C_1C.frm" : "KNJL323C_2C.frm", 4);
        } else if (_param.isGojo()) {
            ret = svf.VrSetForm("2".equals(_param._applicantdiv) ? "KNJL323C_1G.frm" : "KNJL323C_2G.frm", 4);
        } else {
            ret = svf.VrSetForm("KNJL323C.frm", 4);
        }
        ret = svf.VrsOut("NENDO", _param.getNendo());
        ret = svf.VrsOut("APPLICANTDIV", _param.getNameMst("1", db2, "L003", _param._applicantdiv));// 画面から入試制度
        String selsubdivText = "";
        if (_param.isGojo() && !"9".equals(selSubDiv)) {
            selsubdivText = ","+_param.getNameMst("1", db2, "L009", selSubDiv);
        }
        String shdivText = "";
        if (_param.isGojo() && !"9".equals(shDiv) || _param.isWakayama() && "2".equals(_param._applicantdiv) && "3".equals(testDiv)) { // 入試制度が高校かつ入試区分が編入コースのみ専願/併願を表示する
            shdivText = "("+_param.getNameMst("1", db2, "L006", shDiv)+selsubdivText+")";// 画面から専併区分
        }
        ret = svf.VrsOut("TESTDIV", _param.getNameMst("1", db2, "L004", testDiv)+shdivText);// 画面から入試区分
        ret = svf.VrsOut("SCHOOLNAME", _param.getSchoolName(db2));// 学校名
        
        if (outputSelect == 4) {
            ret = svf.VrsOut("TITLE", "【追加合格者名簿("+_param.getNameMst("2", db2, "L010", _param._specialMeasures)+")】");
        } else {
            String passType = "";
            if ("1".equals(printPassSelect)){ passType="(S合格/G合格)"; }
            if ("2".equals(printPassSelect)){ passType="(S合格)"; }
            if ("3".equals(printPassSelect)){ passType="(G合格)"; }
            ret = svf.VrsOut("TITLE", "【" + titles[outputSelect-1] + passType + "】");
        }
        
        ret = svf.VrsOut("DATE", _param.getLoginDateString());
        
        String fsItem = null;
        if ("1".equals(_param._applicantdiv)) {
            fsItem = "出身小学校";
        } else if ("2".equals(_param._applicantdiv)) {
            fsItem = "出身中学校";
        }
        ret = svf.VrsOut("FS_ITEM", fsItem);

        //  ＳＶＦ属性変更--->改ページ
        ret = svf.VrAttribute("TESTDIV","FF=1");
    }



    /**
     *  svf print 印刷処理
     *            入試区分が指定されていれば( => param[2] !== "9" )１回の処理
     *            入試区分が複数の場合は全ての入試区分を舐める
     */

    private boolean setSvfMain(
            DB2UDB db2,
            Vrw32alp svf,
            PreparedStatement ps1,
            PreparedStatement psTotalpage,
            String testDiv,
            String shDiv,
            String selSubDiv,
            int outputSelect,
            String printPassSelect
        ) {
            boolean nonedata = false;

            try {
                if (setSvfout(db2, svf, ps1, psTotalpage, testDiv, shDiv, selSubDiv, outputSelect, printPassSelect)) nonedata = true; //帳票出力のメソッド
                db2.commit();
            } catch( Exception ex ) {
                log.error("setSvfMain set error!"+ex);
            }
            return nonedata;
        }

    /** 表紙をセット **/
    private void setCover(
            DB2UDB db2,
            Vrw32alp svf,
            String testDiv,
            String shDiv,
            String selSubDiv,
            int outputSelect,
            String printPassSelect)
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetForm("KNJLCVR001C.frm", 4);
        ret = svf.VrsOut("NENDO", _param.getNendo());
        ret = svf.VrsOut("APPLICANTDIV", _param.getNameMst("1", db2, "L003", _param._applicantdiv));// 画面から入試制度
        String selsubdivText = "";
        if (_param.isGojo() && !"9".equals(selSubDiv)) {
            selsubdivText = ","+_param.getNameMst("1", db2, "L009", selSubDiv);
        }
        String shdivText = "";
        if (_param.isGojo() && !"9".equals(shDiv) || _param.isWakayama() && "2".equals(_param._applicantdiv) && "3".equals(testDiv)) { // 入試制度が高校かつ入試区分が編入コースのみ専願/併願を表示する
            shdivText = "("+_param.getNameMst("1", db2, "L006", shDiv)+selsubdivText+")";// 画面から専併区分
        }
        ret = svf.VrsOut("TESTDIV", _param.getNameMst("1", db2, "L004", testDiv)+shdivText);// 画面から入試区分
        ret = svf.VrsOut("SCHOOLNAME", _param.getSchoolName(db2));// 学校名
        if (outputSelect == 4) {
            ret = svf.VrsOut("TITLE", "【追加合格者名簿("+_param.getNameMst("2", db2, "L010", _param._specialMeasures)+")】");
        } else {
            String passType = "";
            if ("1".equals(printPassSelect)){ passType="(S合格/G合格)"; }
            if ("2".equals(printPassSelect)){ passType="(S合格)"; }
            if ("3".equals(printPassSelect)){ passType="(G合格)"; }
            ret = svf.VrsOut("TITLE", "【" + titles[outputSelect-1] + passType + "】");
        }
        ret = svf.VrsOut("NOTE" ,"note");
        ret = svf.VrEndRecord();//レコードを出力
    }

    /**総ページ数をセット**/
    private void setTotalPage(
        DB2UDB db2,
        Vrw32alp svf,
        PreparedStatement psTotalPage
    ) {
        try {
            ResultSet rs = psTotalPage.executeQuery();

            while( rs.next() ){
                if (rs.getString("TOTAL_PAGE") != null) 
                    svf.VrsOut("TOTAL_PAGE" ,rs.getString("TOTAL_PAGE"));
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setTotalPage set error! ="+ex);
        }

    }

    /**帳票出力（受験者一覧をセット）**/
    private boolean setSvfout(
        DB2UDB db2,
        Vrw32alp svf,
        PreparedStatement ps1,
        PreparedStatement psTotalpage,
        String testDiv,
        String shDiv,
        String selSubDiv,
        int outputSelect,
        String printPassSelect
    ) {
        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        int reccnt_man      = 0;    //男レコード数カウント用
        int reccnt_woman    = 0;    //女レコード数カウント用
        int reccnt = 0;             //合計レコード数
        try {
            ResultSet rs = ps1.executeQuery();

            int pagecnt = 1;            //現在ページ数
            int gyo = 1;                //現在ページ数の判断用（行）
            String testdiv = "0";       //現在ページ数の判断用（入試区分）
            
            while( rs.next() ){
                if (nonedata == false) {
                    setCover(db2, svf, testDiv, shDiv, selSubDiv, outputSelect, printPassSelect);
                    setHeader(db2, svf, testDiv, shDiv, selSubDiv, outputSelect, printPassSelect);
                    setTotalPage(db2, svf, psTotalpage); //総ページ数メソッド
                    nonedata = true;
                }
                
                //レコードを出力
                if (reccnt > 0) ret = svf.VrEndRecord();
                //５０行超えたとき、または、入試区分ブレイクの場合、ページ数カウント
                if ((gyo > 50) || (!testdiv.equals(rs.getString("TESTDIV")) && !testdiv.equals("0"))) {
                    gyo = 1;
                    pagecnt++;
                }
                //ヘッダ
                ret = svf.VrsOut("PAGE", String.valueOf(pagecnt));      //現在ページ数
                //明細
                ret = svf.VrsOut("NUMBER", String.valueOf(reccnt+1));  //連番
                ret = svf.VrsOut("EXAMNO", rs.getString("EXAMNO")); //受験番号
                ret = svf.VrsOut(setformatArea("NAME", 10, rs.getString("NAME")), rs.getString("NAME")); //名前
                ret = svf.VrsOut("SEX", rs.getString("SEX_NAME"));  //性別
                ret = svfVrsOutFormat(svf, "FINSCHOOL", 13, nvlT(rs.getString("FINSCHOOL_NAME")), ""); // 出身学校名
                if (_param.isWakayama()) {
                    svf.VrsOut("JUDGE", rs.getString("JUDGEDIV_NAME")); // 判定
                }
                //志望コース・合格コース
                if (_param.isCollege() && "2".equals(_param._applicantdiv)) {
                    svf.VrsOut("HOPE_COURSE", "3".equals(rs.getString("SHDIV")) ? "EA" : "4".equals(rs.getString("SHDIV")) ? "ES" : "5".equals(rs.getString("SHDIV")) ? "EA/ES" : "");
                    svf.VrsOut("PASS_COURSE", "A".equals(rs.getString("JUDGEDIV")) ? "EA" : "B".equals(rs.getString("JUDGEDIV")) ? "ES" : "");
                }

                //レコード数カウント
                reccnt++;
                if (rs.getString("SEX") != null) {
                    if (rs.getString("SEX").equals("1")) reccnt_man++;
                    if (rs.getString("SEX").equals("2")) reccnt_woman++;
                }

                // 保護者のADDR_DATA
                ret = svf.VrsOut(setformatArea("GUARD_NAME", 11, rs.getString("GNAME")), rs.getString("GNAME"));
                ret = svf.VrsOut("ZIPCODE"  ,rs.getString("ZIPCD"));

                ret = svf.VrsOut("TELNO"  ,rs.getString("TELNO"));
                
                String shiftDesire = ("1".equals(rs.getString("SHIFT_DESIRE_FLG"))) ? "○" : ""; 
                ret = svf.VrsOut("SHIFT", shiftDesire); // 移行希望

                if (!_param.isCollege() && _param.isGojo() && "1".equals(_param._applicantdiv)) {
                    // 判定
                    svf.VrsOut("JUDGE", rs.getString("JUDGEDIV_NAME"));
                } else {
                    // 前期/後期出願
                    if (null != rs.getString("OTHER_TESTDIV_NAME")) {
                        String otherTestdivName = rs.getString("OTHER_TESTDIV_NAME") + " ";
                        String recomExamno = null != rs.getString("RECOM_EXAMNO") ? rs.getString("RECOM_EXAMNO") : ""; 
                        ret = svf.VrsOut("APPLICATION", otherTestdivName + recomExamno);
                    }
                }

                String address = (rs.getString("ADDRESS1")!=null) ? rs.getString("ADDRESS1") + " "  : "";
                address += (rs.getString("ADDRESS2")!=null) ? rs.getString("ADDRESS2")  : "";
                if (_param.isGojo()) {
                    if (_param.isCollege()) {
                        svf.VrsOut("APPLYDIV", "1".equals(rs.getString("SHDIV")) || "3".equals(rs.getString("SHDIV")) || "4".equals(rs.getString("SHDIV")) || "5".equals(rs.getString("SHDIV")) || "6".equals(rs.getString("SHDIV")) || "7".equals(rs.getString("SHDIV")) || "8".equals(rs.getString("SHDIV")) ? "○" : "");
                        svf.VrsOut("SLIDE_FLG", "1".equals(rs.getString("SLIDE_FLG")) ? "○" : "");
                    } else {
                        svf.VrsOut("APPLYDIV", "1".equals(rs.getString("SHDIV")) ? "○" : "6".equals(rs.getString("SHDIV")) ? "Ⅰ" : "7".equals(rs.getString("SHDIV")) ? "Ⅱ" : "8".equals(rs.getString("SHDIV")) ? "Ⅲ" : "");
                        svf.VrsOut("SLIDE_FLG", "");
                    }
                    String heigan = null != rs.getString("SHSCHOOL_NAME") ? rs.getString("SHSCHOOL_NAME") : "";
                    svf.VrsOut("HEIGAN" + (heigan.length() > 17 ? "3" : heigan.length() > 10 ? "2" : "1"), heigan);
                    svf.VrsOut("SPORTS", "1".equals(rs.getString("SPORTS_FLG")) ? "○" : "");
                    svf.VrsOut("GENERAL_FLG", rs.getString("GENERAL_FLG_NAME"));
                    svf.VrsOut("DORMITORY", "1".equals(rs.getString("DORMITORY_FLG")) ? "○" : "");
                    if (null != rs.getString("ERA_ABBV")) {
                        svf.VrsOut("BIRTHDAY", rs.getString("ERA_ABBV")+rs.getString("BIRTH_Y")+rs.getString("BIRTH_M")+rs.getString("BIRTH_D"));
                    }
                    svf.VrsOut("PREF", rs.getString("PREF_NAME")); // 県別
                    String juku = null != rs.getString("PRISCHOOL_NAME") ? rs.getString("PRISCHOOL_NAME") : "";
                    svf.VrsOut("JUKU" + (juku.length() > 17 ? "3" : juku.length() > 10 ? "2" : ""), juku);
                    StringBuffer remark = new StringBuffer();
                    if ("1".equals(_param._applicantdiv) && "1".equals(rs.getString("DORMITORY_FLG"))) {
                        String dormitory = "1".equals(rs.getString("DORMITORY_FLG")) ? "入寮希望" + " " : "";
                        remark.append(dormitory);
                    }
                    if (null != rs.getString("REMARK1"))            remark.append(rs.getString("REMARK1"));
                    int mojisuu = "1".equals(_param._applicantdiv) ? 20 : 10;
                    svfVrsOutFormat(svf, "REMARK"  ,mojisuu, remark.toString(), rs.getString("REMARK2"));// 備考
                    if ("2".equals(_param._applicantdiv)) {
                        if (address.length() <= 17) {
                            ret = svf.VrsOut("ADDRESS1", address);
                        } else if (address.length() <= 30) {
                            ret = svf.VrsOut("ADDRESS2", address);
                        } else {
                            ret = svf.VrsOut("ADDRESS3", address);
                        }
                    } else {
                        if (address.length() <= 25) {
                            ret = svf.VrsOut("ADDRESS1", address);
                        } else if (address.length() <= 35) {
                            ret = svf.VrsOut("ADDRESS2", address);
                        } else {
                            ret = svf.VrsOut("ADDRESS3", address);
                        }
                    }
                } else {
                    svfVrsOutFormat(svf, "REMARK"  ,20, rs.getString("REMARK1"), rs.getString("REMARK2"));// 備考
                    if (address.length() <= 25) {
                        ret = svf.VrsOut("ADDRESS1", address);
                    } else {
                        ret = svf.VrsOut("ADDRESS2", address);
                    }
                }

                //現在ページ数判断用
                gyo++;
                testdiv = rs.getString("TESTDIV");

                nonedata = true;
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setSvfout set error! ="+ex);
        }
        //最終レコードを出力
        if (nonedata) {
            //最終ページに男女合計を出力
            ret = svf.VrsOut("NOTE" ,"男"+String.valueOf(reccnt_man)+"名,女"+String.valueOf(reccnt_woman)+"名,合計"+String.valueOf(reccnt)+"名");
            ret = svf.VrEndRecord();//レコードを出力
            setSvfInt(svf);         //ブランクセット
        }
        return nonedata;
    }

    private int svfVrsOutFormat(Vrw32alp svf,String area_name, int len, String sval1, String sval2) {
        if (sval1 == null || "null".equals(sval1)) sval1 = "";
        if (sval2 == null || "null".equals(sval2)) {
            sval2 = "";
        } else if ("ADDRESS".equals(area_name)){
            sval2 = " " + sval2;
        }
        return svf.VrsOut(setformatArea(area_name, len, sval1+sval2), sval1+sval2 );
    }

    /**合格者一覧を取得**/ 
    private String preStat1(String testDiv, String shDiv, String selSubDiv, int output, String printPassSelect)
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" SELECT ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     T1.NAME, ");
            stb.append("     T1.NAME_KANA, ");
            stb.append("     T1.SEX, ");
            stb.append("     T5.ABBV1 AS SEX_NAME, ");
            stb.append("     T1.SHDIV, ");
            stb.append("     T8.ABBV1 AS ERA_ABBV, ");
            stb.append("     T1.BIRTH_Y, ");
            stb.append("     T1.BIRTH_M, ");
            stb.append("     T1.BIRTH_D, ");
            stb.append("     T1.FS_NAME, ");
            stb.append("     T7.NAME1 AS RITU_NAME, ");
            stb.append("     T6.FINSCHOOL_DISTCD AS FINSCHOOLCD, ");
            if (_param.isWakayama()) {
                stb.append("     T6.FINSCHOOL_NAME_ABBV AS FINSCHOOL_NAME, ");
            } else {
                stb.append("     VALUE(T6.FINSCHOOL_NAME_ABBV, T6.FINSCHOOL_NAME) AS FINSCHOOL_NAME, ");
            }
            stb.append("     T9.ZIPCD, ");
            stb.append("     T10.PREF_NAME AS PREF_NAME, ");
            stb.append("     T9.ADDRESS1, ");
            stb.append("     T9.ADDRESS2, ");
            stb.append("     T9.TELNO, ");
            stb.append("     T1.JUDGEDIV, ");
            stb.append("     T1.JUDGEDIV_NAME, ");
            stb.append("     T9.GNAME, ");
            stb.append("     T1.REMARK1, ");
            stb.append("     T1.REMARK2, ");
            stb.append("     T1.RECOM_EXAMNO, ");
            stb.append("     T13.ABBV1 AS OTHER_TESTDIV_NAME, ");
            stb.append("     T1.SHIFT_DESIRE_FLG, ");
            stb.append("     T1.SLIDE_FLG, ");
            stb.append("     T1.SPORTS_FLG, ");
            stb.append("     T1.GENERAL_FLG, ");
            stb.append("     NM1.NAME1 AS GENERAL_FLG_NAME, ");
            stb.append("     FINH.FINSCHOOL_NAME AS SHSCHOOL_NAME, ");
            stb.append("     T1.DORMITORY_FLG, ");
            stb.append("     P1.PRISCHOOL_NAME ");
            stb.append(" FROM ");
            stb.append("     (SELECT ");
            stb.append("         W1.ENTEXAMYEAR, ");
            if (_param.isGojo() && "7".equals(testDiv) && "2".equals(shDiv) && ("7".equals(selSubDiv) || "9".equals(selSubDiv)) && output==1) {
                stb.append("         CASE WHEN W1.TESTDIV = '3' THEN '"+ testDiv +"' ELSE W1.TESTDIV END AS TESTDIV, ");
            } else {
                stb.append("         W1.TESTDIV, ");
            }
            stb.append("         W1.EXAMNO, ");
            stb.append("         W2.RECOM_EXAMNO, ");
            stb.append("         W2.NAME, ");
            stb.append("         W2.NAME_KANA, ");
            stb.append("         W2.SEX, ");
            stb.append("         W2.SHDIV, ");
            stb.append("         W2.ERACD, ");
            stb.append("         W2.BIRTH_Y, ");
            stb.append("         W2.BIRTH_M, ");
            stb.append("         W2.BIRTH_D, ");
            stb.append("         W2.FS_NAME, ");
            stb.append("         W2.FS_CD, ");
            stb.append("         W2.REMARK1, ");
            stb.append("         W2.REMARK2, ");
            stb.append("         W1.JUDGEDIV, ");
            stb.append("         W4.NAME1 AS JUDGEDIV_NAME, ");
            stb.append("         W2.SHIFT_DESIRE_FLG, ");
            stb.append("         W2.SLIDE_FLG, ");
            stb.append("         W2.SPORTS_FLG, ");
            stb.append("         W2.GENERAL_FLG, ");
            stb.append("         W2.SH_SCHOOLCD, ");
            stb.append("         W2.DORMITORY_FLG, ");
            stb.append("         W2.PRISCHOOLCD ");
            stb.append("     FROM ");
            stb.append("         ENTEXAM_RECEPT_DAT W1 ");
            stb.append("         INNER JOIN ENTEXAM_APPLICANTBASE_DAT W2 ON ");
            stb.append("             W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append("             W2.EXAMNO = W1.EXAMNO AND ");
            stb.append("             W2.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append("             W2.TESTDIV = W1.TESTDIV ");
            stb.append("         LEFT JOIN NAME_MST W4 ON ");
            stb.append("             W4.NAMECD1 = 'L013' AND ");
            stb.append("             W4.NAMECD2 = W1.JUDGEDIV ");
            stb.append("     WHERE ");
            stb.append("         W1.ENTEXAMYEAR='"+_param._year+"' AND ");
            stb.append("         W2.APPLICANTDIV='"+_param._applicantdiv+"' AND ");
            if (!"9".equals(shDiv)) {
                stb.append("         W2.SHDIV= '"+shDiv+"' AND ");
            }
            // 特併合格者は、特進文系コース併願数学の合格者と一緒に出力する。
            if (_param.isGojo() && "7".equals(testDiv) && "2".equals(shDiv) && "7".equals(selSubDiv) && output==1) {
                stb.append("        (W1.TESTDIV='"+testDiv+"' AND W2.SELECT_SUBCLASS_DIV= '"+selSubDiv+"' AND W1.JUDGEDIV='1' OR W1.TESTDIV='3' AND W1.JUDGEDIV='7') AND ");
            } else if (_param.isGojo() && "7".equals(testDiv) && "2".equals(shDiv) && "9".equals(selSubDiv) && output==1) {
                stb.append("        (W1.TESTDIV= '"+testDiv+"' AND W1.JUDGEDIV='1' OR W1.TESTDIV='3' AND W1.JUDGEDIV='7') AND ");
            } else {
                stb.append("         W1.TESTDIV = '"+testDiv+"' AND ");
                if (!"9".equals(selSubDiv)) {
                    stb.append("         W2.SELECT_SUBCLASS_DIV= '"+selSubDiv+"' AND ");
                }
                if (output==1) {
                    stb.append(" W1.JUDGEDIV in ('1','8','9','A','B') AND ");
                    if ("1".equals(printPassSelect)) {
                        stb.append("         W1.JUDGEDIV in ('8','9') AND ");
                    } else if ("2".equals(printPassSelect)) {
                        stb.append("         W1.JUDGEDIV in ('8') AND ");
                    } else if ("3".equals(printPassSelect)) {
                        stb.append("         W1.JUDGEDIV in ('9') AND ");
                    }
                } else if (output==2) {
                    stb.append(" W1.JUDGEDIV='3' AND ");
                } else if (output==3) {
                    stb.append(" W1.JUDGEDIV='4' AND ");
                } else if (output==4) {
                    stb.append(" W2.SPECIAL_MEASURES='"+_param._specialMeasures+"' AND ");
                }
            }
            stb.append("         W2.ENTEXAMYEAR=W1.ENTEXAMYEAR AND ");
            stb.append("         W2.EXAMNO=W1.EXAMNO AND ");
            stb.append("         W2.APPLICANTDIV=W1.APPLICANTDIV AND ");
            stb.append("         W2.TESTDIV=W1.TESTDIV ");
            stb.append("     ) T1 ");
            stb.append("     LEFT JOIN NAME_MST T5 ON ");
            stb.append("                 T5.NAMECD1='Z002' AND ");
            stb.append("                 T5.NAMECD2=T1.SEX ");
            stb.append("     LEFT JOIN FINSCHOOL_MST T6 ON ");
            stb.append("                 T6.FINSCHOOLCD = T1.FS_CD ");
            stb.append("     LEFT JOIN NAME_MST T7 ON ");
            stb.append("                 T7.NAMECD1 = 'L001' AND ");
            stb.append("                 T7.NAMECD2 = T6.FINSCHOOL_DISTCD ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T9 ON ");
            stb.append("                 T9.ENTEXAMYEAR = T1.ENTEXAMYEAR AND ");
            stb.append("                 T9.EXAMNO = T1.EXAMNO ");
            stb.append("     LEFT JOIN PREF_MST T10 ON ");
            stb.append("                 T10.PREF_CD = T9.PREF_CD ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T12 ON ");
            stb.append("                 T12.ENTEXAMYEAR=T1.ENTEXAMYEAR AND ");
            stb.append("                 T12.EXAMNO=T1.RECOM_EXAMNO AND ");
            stb.append("                 T12.APPLICANTDIV = '1' AND ");
            stb.append("                 T12.TESTDIV <> T1.TESTDIV ");            
            stb.append("     LEFT JOIN NAME_MST T13 ON ");
            stb.append("                 T13.NAMECD1 = 'L004' AND ");
            stb.append("                 T13.NAMECD2 = T12.TESTDIV ");
            stb.append("     LEFT JOIN PRISCHOOL_MST P1 ON ");
            stb.append("         P1.PRISCHOOLCD = T1.PRISCHOOLCD ");
            stb.append("     LEFT JOIN NAME_MST T8 ON ");
            stb.append("                 T8.NAMECD1 = 'L007' AND ");
            stb.append("                 T8.NAMECD2 = T1.ERACD ");
            stb.append("     LEFT JOIN FINHIGHSCHOOL_MST FINH ON ");
            stb.append("         FINH.FINSCHOOLCD = T1.SH_SCHOOLCD ");
            stb.append("     LEFT JOIN NAME_MST NM1 ON ");
            stb.append("                 NM1.NAMECD1 = 'L027' AND ");
            stb.append("                 NM1.NAMECD2 = T1.GENERAL_FLG ");
            stb.append(" ORDER BY ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.EXAMNO ");
            
        } catch( Exception e ){
            log.error("preStat1 error!");
        }
        return stb.toString();

    }//preStat1()の括り



    /**総ページ数を取得**/
    private String preStatTotalPage(String testDiv, String shDiv, String selSubDiv, int output, String printPassSelect)
    {
        StringBuffer stb = new StringBuffer();
    //  パラメータ（なし）
        try {
            stb.append("SELECT ");
            stb.append("    SUM(T1.TEST_CNT) TOTAL_PAGE  ");
            stb.append("FROM ");
            stb.append("    (SELECT CASE WHEN MOD(COUNT(*),50) > 0 THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END TEST_CNT  ");
            stb.append("     FROM   ENTEXAM_RECEPT_DAT W1, ENTEXAM_APPLICANTBASE_DAT W2  ");
            stb.append("     WHERE  W1.ENTEXAMYEAR='"+_param._year+"'  ");
            stb.append("            AND W1.APPLICANTDIV='"+_param._applicantdiv+"' ");
            if (!"9".equals(shDiv)) {
                stb.append("            AND W2.SHDIV= '"+shDiv+"' ");
            }
            // 特併合格者は、特進文系コース併願数学の合格者と一緒に出力する。
            if (_param.isGojo() && "7".equals(testDiv) && "2".equals(shDiv) && "7".equals(selSubDiv) && output==1) {
                stb.append("            AND (W1.TESTDIV='"+testDiv+"' AND W2.SELECT_SUBCLASS_DIV= '"+selSubDiv+"' AND W1.JUDGEDIV='1' OR W1.TESTDIV='3' AND W1.JUDGEDIV='7') ");
            } else {
                stb.append("            AND W1.TESTDIV='"+testDiv+"' ");
                if (!"9".equals(selSubDiv)) {
                    stb.append("            AND W2.SELECT_SUBCLASS_DIV= '"+selSubDiv+"' ");
                }
                if (output==1) {
                    stb.append("  AND W1.JUDGEDIV in ('1','8','9','A','B') ");
                    if ("1".equals(printPassSelect)) {
                        stb.append("         AND W1.JUDGEDIV in ('8','9') ");
                    } else if ("2".equals(printPassSelect)) {
                        stb.append("         AND W1.JUDGEDIV in ('8') ");
                    } else if ("3".equals(printPassSelect)) {
                        stb.append("         AND W1.JUDGEDIV in ('9') ");
                    }
                } else if (output==2) {
                    stb.append("  AND W1.JUDGEDIV='3' ");
                } else if (output==3) {
                    stb.append("  AND W1.JUDGEDIV='4' ");
                } else if (output==4) {
                    stb.append("  AND W2.SPECIAL_MEASURES='"+_param._specialMeasures+"' ");
                }
            }
            stb.append("            AND W2.ENTEXAMYEAR=W1.ENTEXAMYEAR ");
            stb.append("            AND W2.EXAMNO=W1.EXAMNO ");
            stb.append("            AND W2.APPLICANTDIV=W1.APPLICANTDIV ");
            stb.append("            AND W2.TESTDIV=W1.TESTDIV ");
            stb.append("    ) T1  ");
        } catch( Exception e ){
            log.error("preStatTotalPage error!");
        }
        return stb.toString();

    }//preStat2()の括り



    /**PrepareStatement close**/
    private void preStatClose(
        PreparedStatement ps1,
        PreparedStatement ps2
    ) {
        try {
            ps1.close();
            ps2.close();
        } catch( Exception e ){
            log.error("preStatClose error! ="+e);
        }
    }//preStatClose()の括り



    /**ブランクをセット**/
    private void setSvfInt(
        Vrw32alp svf
    ) {
        try {
            svf.VrsOut("NOTE"   ,"");
        } catch( Exception ex ) {
            log.error("setSvfInt set error!");
        }

    }
    
    /**
     * 半角数字を全角数字に変換する
     * @param s
     * @return
     */
    private String convZenkakuToHankaku(String s) {
        StringBuffer sb = new StringBuffer(s);
        for (int i = 0; i < s.length(); i++) {
          char c = s.charAt(i);
          if (c >= '0' && c <= '9') {
            sb.setCharAt(i, (char) (c - '0' + 0xff10));
          }
        }
        return sb.toString();
    }

    /**
     * 日付をフォーマットYYYY年MM月DD日に設定する
     * @param s
     * @return
     */
    private String formatSakuseiDate(String cnvDate) {

        String retDate = "";
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd"); 
            //文字列よりDate型へ変換
            Date date1 = format.parse(cnvDate); 
            // 年月日のフォーマットを指定
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy'年'MM'月'dd'日'");
            // Date型より文字列へ変換
            retDate = sdf1.format(date1);
        } catch( Exception e ){
            log.error("setHeader set error!");
        }
        return retDate;
    }

    /**
     * 帳票に設定する文字数が制限文字超の場合
     * 帳票設定エリアの変更を行う
     * @param area_name 帳票出力エリア
     * @param area_len      制限文字数
     * @param sval          値
     * @return
     */
    private String setformatArea(String area_name, int area_len, String sval) {

        String retAreaName = "";
        // 値がnullの場合はnullが返される
        if (sval == null) {
            return null;
        }
        // 設定値が制限文字超の場合、帳票設定エリアの変更を行う
        if(area_len >= sval.length()){
            retAreaName = area_name + "1";
        } else {
            retAreaName = area_name + "2";
        }
        return retAreaName;
    }

    /**
     * NULL値を""として返す。
     */
    private String nvlT(String val) {

        if (val == null) {
            return "";
        } else {
            return val;
        }
    }
    
    /**
     * パラメータを受け取るクラス
     **/
    private class Param {
        private final String _prgid;
        private final String _dbname;
        private final String _year;
        private final String _loginDate;
        private final String _applicantdiv;
        private final String _testdiv;
        private final String[] _testdivArray;
        private final int[] _selected;
        private final boolean _seirekiFlg;
        private final String _specialMeasures;
        private final String _shdiv;
        private final String _z010SchoolCode;
        private final String[] _printPassArray;
        Param(
                final DB2UDB db2,
                final HttpServletRequest request
        ) {
            _prgid        = request.getParameter("PRGID");
            _dbname       = request.getParameter("DBNAME");
            _year         = request.getParameter("YEAR");
            _loginDate   = request.getParameter("LOGIN_DATE");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _z010SchoolCode = getSchoolCode(db2);
            _testdiv = request.getParameter("TESTDIV");
            if ("9".equals(_testdiv)) {
                if("1".equals(_applicantdiv)) _testdivArray = new String[]{"1","2"};
                else if("2".equals(_applicantdiv)) {
                    if (isGojo()) {
                        _testdivArray = new String[]{"3","4","5","8"}; 
                    } else {
                        _testdivArray = new String[]{"3","4","5"}; 
                    }
                }
                else _testdivArray = null;
            } else {
                _testdivArray = new String[]{_testdiv};
            } 
            String s1 = request.getParameter("PRINT_TYPE1");                  // 出力条件
            String s2 = request.getParameter("PRINT_TYPE2");                  // 出力条件
            String s3 = request.getParameter("PRINT_TYPE3");                  // 出力条件
            String s4 = request.getParameter("PRINT_TYPE4");                  // 出力条件
            _selected = new int[] {
                    "1".equals(s1) ? 1 : -1,
                    "1".equals(s2) ? 2 : -1,
                    "1".equals(s3) ? 3 : -1,
                    "1".equals(s4) ? 4 : -1 };

            //和歌山中学の場合、指示画面からパラメータがくる（合格者名簿）
            String printPass1 = request.getParameter("PRINT_PASS1"); //S合格/G合格
            String printPass2 = request.getParameter("PRINT_PASS2"); //S合格
            String printPass3 = request.getParameter("PRINT_PASS3"); //G合格
            if ("1".equals(s1) && ("1".equals(printPass1) || "1".equals(printPass2) || "1".equals(printPass3))) {
                _printPassArray = new String[] {
                        "1".equals(printPass1) ? "1" : null,
                        "1".equals(printPass2) ? "2" : null,
                        "1".equals(printPass3) ? "3" : null };
            } else {
                _printPassArray = new String[]{"0"};
            }

            _seirekiFlg = getSeirekiFlg(db2);
            
            _specialMeasures = request.getParameter("SPECIAL_MEASURES");
            
            _shdiv = request.getParameter("SHDIV");

        }
        
        private String getSchoolName(DB2UDB db2) {
            String certifKindCd = null;
            if ("1".equals(_applicantdiv)) certifKindCd = "105";
            if ("2".equals(_applicantdiv)) certifKindCd = "106";
            if (certifKindCd == null) return "UNIDENTIFIED CERTIFKINDCD";
            
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME ");
            sql.append(" FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '"+_year+"' AND CERTIF_KINDCD = '"+certifKindCd+"' ");
            String name = null;
            try{
                PreparedStatement ps = db2.prepareStatement(sql.toString());
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                   name = rs.getString("SCHOOL_NAME");
                }
            } catch(Exception ex) {
                log.debug(ex);
            }
            
            return name;
        }
        
        private boolean getSeirekiFlg(DB2UDB db2) {
            boolean seirekiFlg = false;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                if ( rs.next() ) {
                    if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            }
            return seirekiFlg;
        }
        
        String getNendo() {
            return _seirekiFlg ? _year+"年度":
                KNJ_EditDate.h_format_JP_N(_year+"-01-01")+"度";
        }
        
        String getLoginDateString() {
            return getDateString(_loginDate);
        }

        String getDateString(String dateFormat) {
            if (null != dateFormat) {
                return _seirekiFlg ?
                        dateFormat.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(dateFormat):
                            KNJ_EditDate.h_format_JP(dateFormat ) ;        
            }
            return null;
        }
        
        private String getNameMst(String i, DB2UDB db2, String namecd1,String namecd2) {
            
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT NAME"+i+" ");
            sql.append(" FROM NAME_MST ");
            sql.append(" WHERE NAMECD1 = '"+namecd1+"' AND NAMECD2 = '"+namecd2+"' ");
            String name = null;
            try{
                PreparedStatement ps = db2.prepareStatement(sql.toString());
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                   name = rs.getString("NAME"+i+"");
                }
            } catch(SQLException ex) {
                log.debug(ex);
            }
            
            return name;
        }
        
        private String getSchoolCode(DB2UDB db2) {
            String schoolCode = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   CASE WHEN NAME1 = 'CHIBEN' THEN NAME2 ELSE NULL END AS SCHOOLCODE ");
                stb.append(" FROM ");
                stb.append("   NAME_MST T1 ");
                stb.append(" WHERE ");
                stb.append("   T1.NAMECD1 = 'Z010' ");
                stb.append("   AND T1.NAMECD2 = '00' ");
                
                PreparedStatement ps = db2.prepareStatement(stb.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    schoolCode = rs.getString("SCHOOLCODE");
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("getSchoolCode Exception", e);
            }
            return schoolCode;
        }
        
        boolean isGojo() {
            return "30290053001".equals(_z010SchoolCode) || isCollege();
        }
        
        boolean isWakayama() {
            return "30300049001".equals(_z010SchoolCode);
        }

        boolean isCollege() {
            return "30290086001".equals(_z010SchoolCode);
        }
    }
    
}//クラスの括り
