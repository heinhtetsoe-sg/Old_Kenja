package servletpack.KNJL;

import java.io.IOException;
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

import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３０２Ｃ＞  受験者名簿
 *
 *  2008/11/04 RTS 作成日
 **/

public class KNJL302C {

    private static final Log log = LogFactory.getLog(KNJL302C.class);
    private String[] _testDivPrint;
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
        log.fatal("$Revision: 65206 $ $Date: 2019-01-22 17:21:04 +0900 (火, 22 1 2019) $"); // CVSキーワードの取り扱いに注意
        _param = new Param(db2, request);

        // '全て'
        if ("9".equals(_param._testDiv)) {
            if("1".equals(_param._applicantDiv)) {
                if (_param.isCollege()) {
                    _testDivPrint = new String[]{"1","2"};
                } else if (_param.isGojo()) {
                    _testDivPrint = new String[]{"1","7","2"};
                } else {
                    _testDivPrint = new String[]{"1","2"};
                }
            }
            if("2".equals(_param._applicantDiv)) {
                if (_param.isGojo()) {
                    _testDivPrint = new String[]{"3","4","5","8"};
                } else {
                    _testDivPrint = new String[]{"3","4","5"};
                }
            }
        } else {
            _testDivPrint = new String[]{_param._testDiv};
        }

        //  print設定
        response.setContentType("application/pdf");

        //  svf設定
        svf.VrInit();                             //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定

        //  ＳＶＦ作成処理
        PreparedStatement psTestdiv = null;
        PreparedStatement ps1 = null;
        PreparedStatement psTotalPage = null;
        boolean nonedata = false;                               //該当データなしフラグ

        //SQL作成
        try {
            for(int i=0; i<_testDivPrint.length; i++) {
                String testDiv = _testDivPrint[i];
                String[] shDivPrintTemp = null;
                if ("9".equals(_param._testDiv)) {
                    if (_param.isCollege()) {
                        //入試区分毎
                        shDivPrintTemp = new String[]{"9"};
                    } else if (_param.isGojo() && "1".equals(_param._applicantDiv) && "1".equals(testDiv)) {
                        shDivPrintTemp = new String[]{"9","6","7","8","2"};
                    } else if (_param.isGojo() && "1".equals(_param._applicantDiv) && "2".equals(testDiv)) {
                        shDivPrintTemp = new String[]{"9","6","7","8","2"};
                    } else if (_param.isGojo() && "1".equals(_param._applicantDiv) && "7".equals(testDiv)) {
                        shDivPrintTemp = new String[]{"9"};
                    } else if (_param.isGojo() || _param.isWakayama() && "2".equals(_param._applicantDiv) && "3".equals(testDiv)) {
                        shDivPrintTemp = new String[]{"1","2"};
                    } else {
                        shDivPrintTemp = new String[]{"1"};
                    }
                } else {
                    if (_param.isCollege()) {
                        shDivPrintTemp = new String[]{"9"};
                    } else if (_param.isGojo() && "1".equals(_param._applicantDiv) && "1".equals(testDiv)) {
                        shDivPrintTemp = new String[]{"9","6","7","8","2"};
                    } else if (_param.isGojo() && "1".equals(_param._applicantDiv) && "2".equals(testDiv)) {
                        shDivPrintTemp = new String[]{"9","6","7","8","2"};
                    } else if (_param.isGojo() && "1".equals(_param._applicantDiv) && "7".equals(testDiv)) {
                        shDivPrintTemp = new String[]{"9"};
                    } else {
                        shDivPrintTemp = new String[]{_param._shDiv};
                    }
                }

                for(int j=0; j<shDivPrintTemp.length; j++) {
                    String shDiv = shDivPrintTemp[j];

                    String[] selSubDivPrint = null;
                    if (_param.isGojo() && "2".equals(_param._applicantDiv) && "7".equals(testDiv)) {
                        selSubDivPrint = new String[]{"7","6"}; //7:数学 6:社会
                    } else {
                        selSubDivPrint = new String[]{"9"};
                    }

                    for(int k=0; k<selSubDivPrint.length; k++) {
                        String selSubDiv = selSubDivPrint[k];

                        String sql;
                        sql = preStat1(testDiv, shDiv, selSubDiv);
                        log.debug("preStat1 sql="+sql);
                        ps1 = db2.prepareStatement(sql);        //受験者一覧preparestatement

                        sql = preStatTotalPage(testDiv, shDiv, selSubDiv);
                        log.debug("preStateTotalPage sql="+sql);
                        psTotalPage = db2.prepareStatement(sql);        //総ページ数preparestatement

                        //SVF出力
                        if (setSvfMain(db2, svf, ps1, psTotalPage, testDiv, shDiv, selSubDiv)) nonedata = true;  //帳票出力のメソッド
                    }
                }
            }
        } catch( Exception ex ) {
            log.error("DB2 prepareStatement set error!",ex);
        }

        //  該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }

        //  終了処理
        svf.VrQuit();
        preStatClose(psTestdiv,ps1,psTotalPage);       //preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる

    }//doGetの括り

    /** 表紙をセットする */
    private void setCover(
            DB2UDB db2,
            Vrw32alp svf,
            String testDiv,
            String shDiv,
            String selSubDiv)
    {
        String title = (_param._output && _param.isWakayama()) ? "【受験者名簿・前期Ｓ選抜合格者は除く】" : "【受験者名簿】";
        svf.VrSetForm("KNJLCVR001C.frm", 4);
        svf.VrsOut("NENDO", _param.getNendo());
        svf.VrsOut("APPLICANTDIV", _param.getNameMst(db2, "L003", _param._applicantDiv));// 画面から入試制度
        String selsubdibText = "";
        if (_param.isGojo() && !"9".equals(selSubDiv)) {
            selsubdibText = ","+_param.getNameMst(db2, "L009", selSubDiv);
        }
        String shdivText = "";
        if (_param.isGojo() && !"9".equals(shDiv) || _param.isWakayama() && "2".equals(_param._applicantDiv) && "3".equals(testDiv)) {
            shdivText = "("+_param.getNameMst(db2, "L006", shDiv)+selsubdibText+")";
        }
        svf.VrsOut("TESTDIV", _param.getNameMst(db2, "L004", testDiv)+shdivText);// 画面から入試区分
        svf.VrsOut("SCHOOLNAME", _param.getSchoolName(db2));// 学校名
        svf.VrsOut("TITLE", title);
        svf.VrsOut("NOTE" ,"");
        svf.VrEndRecord();//レコードを出力
    }

    /** 事前処理 **/
    private void setHeader(
        DB2UDB db2,
        Vrw32alp svf,
        String testDiv,
        String shDiv,
        String selSubDiv
    ) {
        if (_param.isCollege()) {
            svf.VrSetForm("2".equals(_param._applicantDiv) ? "KNJL302C_1C.frm" : "1".equals(_param._applicantDiv) && "1".equals(testDiv) ? "KNJL302C_2C_2.frm" : "KNJL302C_2C.frm", 4);
        } else if (_param.isGojo()) {
            svf.VrSetForm("2".equals(_param._applicantDiv) ? "KNJL302C_1G.frm" : "KNJL302C_2G.frm", 4);
        } else {
            svf.VrSetForm("2".equals(_param._applicantDiv) ? "KNJL302C_1.frm" : "KNJL302C_2.frm", 4);
        }
        svf.VrsOut("NENDO", _param.getNendo());
        svf.VrsOut("APPLICANTDIV", _param.getNameMst(db2, "L003", _param._applicantDiv));// 画面から入試制度
        String selsubdibText = "";
        if (_param.isGojo() && !"9".equals(selSubDiv)) {
            selsubdibText = ","+_param.getNameMst(db2, "L009", selSubDiv);
        }
        String shdivText = "";
        if (_param.isGojo() && !"9".equals(shDiv) || _param.isWakayama() && "2".equals(_param._applicantDiv) && "3".equals(testDiv)) {
            shdivText = "("+_param.getNameMst(db2, "L006", shDiv)+selsubdibText+")";
        }
        svf.VrsOut("TESTDIV", _param.getNameMst(db2, "L004", testDiv)+shdivText);// 画面から入試区分
        svf.VrsOut("SCHOOLNAME", _param.getSchoolName(db2));// 学校名
        svf.VrsOut( "DATE", _param.getLoginDateString());
        String fsItem = null;
        if ("1".equals(_param._applicantDiv)) {
            fsItem = "出身小学校";
        } else if ("2".equals(_param._applicantDiv)) {
            fsItem = "出身中学校";
        }
        svf.VrsOut("FS_ITEM", fsItem);
        if (_param._output && _param.isWakayama()) {
            svf.VrsOut("ADD_TITLE", "・前期Ｓ選抜合格者は除く");
        }

        //  ＳＶＦ属性変更--->改ページ
        svf.VrAttribute("TESTDIV","FF=1");
    }

    /**
     *  svf print 印刷処理
     *            入試区分が指定されていれば( => _param._testdiv !== "9" )１回の処理
     *            入試区分が複数の場合は全ての入試区分を舐める
     */

    private boolean setSvfMain(
            DB2UDB db2,
            Vrw32alp svf,
            PreparedStatement ps1,
            PreparedStatement psTotalpage,
            String testDiv,
            String shDiv,
            String selSubDiv
    ) {
            boolean nonedata = false;

            try {
                if (setSvfout(db2, svf, ps1, psTotalpage, testDiv, shDiv, selSubDiv)) nonedata = true; //帳票出力のメソッド
                db2.commit();
            } catch( Exception ex) {
                log.error("setSvfMain set error!"+ex);
            }
            return nonedata;
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
            log.error("setTotalPage set error!");
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
        String selSubDiv
    ) {
        boolean nonedata = false;
        try {
            int reccnt_man      = 0;    //男レコード数カウント用
            int reccnt_woman    = 0;    //女レコード数カウント用
            int reccnt = 0;             //合計レコード数
            int bususe1Count = 0;       //JR五条カウント用
            int bususe2Count = 0;       //林間田園都市駅カウント用
            int bususe3Count = 0;       //福神駅カウント用
            int pagecnt = 1;            //現在ページ数
            int gyo = 1;                //現在ページ数の判断用（行）
            ResultSet rs = ps1.executeQuery();

            while( rs.next() ){
                if (nonedata == false) {
                    setCover(db2, svf, testDiv, shDiv, selSubDiv);
                    setHeader(db2, svf, testDiv, shDiv, selSubDiv);
                    setTotalPage(db2, svf, psTotalpage); //総ページ数メソッド
                }
                //レコードを出力
                if (gyo != 1) {
                    svf.VrEndRecord();
                }
                //５０行超えたとき、または、入試区分ブレイクの場合、ページ数カウント
                if (gyo % 50==1) {
                    //ヘッダ
                    svf.VrsOut("PAGE", String.valueOf(pagecnt));      //現在ページ数
                    pagecnt++;
                }
                //明細
                svf.VrsOut("NUMBER", rs.getString("SEQNO"));       //連番
                svf.VrsOut("EXAMNO", rs.getString("EXAMNO"));       //受験番号
                svfVrsOutFormat(svf, "NAME"  ,10, rs.getString("NAME"), ""); //名前
                //志望コース
                if (_param.isCollege() && "1".equals(_param._applicantDiv)) {
                    svf.VrsOut("HOPE_COURSE", "6".equals(rs.getString("SHDIV")) || "9".equals(rs.getString("SHDIV")) ? "G" : "7".equals(rs.getString("SHDIV")) || "A".equals(rs.getString("SHDIV")) ? "S" : "8".equals(rs.getString("SHDIV")) || "B".equals(rs.getString("SHDIV")) ? "S/G" : "");
                    if ("1".equals(testDiv)) {
                        svf.VrsOut("SH_DIV", "1".equals(rs.getString("SUB_ORDER")) ? "Ⅰ型" : "2".equals(rs.getString("SUB_ORDER")) ? "Ⅱ型" : "");
                    }
                } else if (_param.isCollege() && "2".equals(_param._applicantDiv)) {
                    svf.VrsOut("HOPE_COURSE", "3".equals(rs.getString("SHDIV")) ? "EA" : "4".equals(rs.getString("SHDIV")) ? "ES" : "5".equals(rs.getString("SHDIV")) ? "EA/ES" : "");
                }
                svf.VrsOut("SEX", rs.getString("SEX_NAME"));     //性別
                svfVrsOutFormat(svf, "FINSCHOOL"  ,10, nvlT(rs.getString("FINSCHOOL_NAME")), ""); // 学校名
                if (_param._printType) {
                    svfVrsOutFormat(svf, "GUARD_NAME"  ,10, rs.getString("GNAME"), ""); // 保護者名
                }

                svf.VrsOut("ZIPCODE", rs.getString("ZIPCD")); // 郵便番号
                svf.VrsOut("TELNO",     rs.getString("TELNO")); // 電話番号

                if (_param.isGojo()) {
                    if (_param.isCollege()) {
                        svf.VrsOut("APPLYDIV", "1".equals(rs.getString("SHDIV")) || "3".equals(rs.getString("SHDIV")) || "4".equals(rs.getString("SHDIV")) || "5".equals(rs.getString("SHDIV")) || "6".equals(rs.getString("SHDIV")) || "7".equals(rs.getString("SHDIV")) || "8".equals(rs.getString("SHDIV")) ? "○" : "");
                    } else if (_param.isGojo()) {
                        svf.VrsOut("APPLYDIV", "1".equals(rs.getString("SHDIV")) ? "○" : "6".equals(rs.getString("SHDIV")) ? "Ⅰ" : "7".equals(rs.getString("SHDIV")) ? "Ⅱ" : "8".equals(rs.getString("SHDIV")) ? "Ⅲ" : "");
                    }
                    if ("1".equals(rs.getString("BUS_USE"))) {
                        if ("1".equals(rs.getString("STATIONDIV"))) { // 林間田園都市駅
                            svf.VrsOut("BUSUSE2", rs.getString("BUS_USER_COUNT"));
                            bususe2Count += rs.getInt("BUS_USER_COUNT");
                        } else if ("2".equals(rs.getString("STATIONDIV"))) { // 福神駅
                            svf.VrsOut("BUSUSE3", rs.getString("BUS_USER_COUNT"));
                            bususe3Count += rs.getInt("BUS_USER_COUNT");
                        } else if ("3".equals(rs.getString("STATIONDIV"))) { // JR五条駅
                            svf.VrsOut("BUSUSE1", rs.getString("BUS_USER_COUNT"));
                            bususe1Count += rs.getInt("BUS_USER_COUNT");
                        }
                    }
                    String juku = null != rs.getString("PRISCHOOL_NAME") ? rs.getString("PRISCHOOL_NAME") : "";
                    svf.VrsOut("JUKU" + (juku.length() > 17 ? "3" : juku.length() > 10 ? "2" : ""), juku);
                }
                //-------------------------------------------------------------
                final String shiftDesireFlg = rs.getString("SHIFT_DESIRE_FLG");
                if ("2".equals(_param._applicantDiv)) {
                    //和_1,五_1G
                    int len = _param.isGojo() ? 28 : 30;
                    svfVrsOutFormat(svf, "ADDRESS" ,len, rs.getString("ADDRESS1"), rs.getString("ADDRESS2")); // 住所
                    svf.VrsOut("PREF", rs.getString("PREF_NAME")); // 県別

                    if (null != rs.getString("ERA_ABBV"))
                        svf.VrsOut("BIRTHDAY", rs.getString("ERA_ABBV")+rs.getString("BIRTH_Y")+rs.getString("BIRTH_M")+rs.getString("BIRTH_D")); // 誕生日

                    StringBuffer remark = new StringBuffer();
                    if (null != rs.getString("OTHER_TESTDIV_NAME") || (!"".equals(shiftDesireFlg) && null != shiftDesireFlg)) {
                        String otherTestdivName = null != rs.getString("OTHER_TESTDIV_NAME") ? rs.getString("OTHER_TESTDIV_NAME") + " " : "";
                        String recomExamno = null != rs.getString("RECOM_EXAMNO") ? rs.getString("RECOM_EXAMNO") + " " : "";
                        remark.append(otherTestdivName);
                        remark.append(recomExamno);
                        if (_param.isGojo()) {
                            String shiftDesire = (!"".equals(shiftDesireFlg) && null != shiftDesireFlg) ? "○" : "";
                            if (_param.isCollege()) {
                                shiftDesire = "1".equals(shiftDesireFlg) ? "五併" : "2".equals(shiftDesireFlg) ? "和併" : "3".equals(shiftDesireFlg) ? "五併/和併" : "";
                            }
                            svf.VrsOut("SHIFT", shiftDesire); // カレッジ併願
                        } else {
                            String shiftDesire = (!"".equals(shiftDesireFlg) && null != shiftDesireFlg) ? "移行希望" + " " : "";
                            remark.append(shiftDesire); // 移行希望
                        }
                    }
                    if (_param.isCollege()) {
                        svf.VrsOut("SLIDE_FLG", "1".equals(rs.getString("SLIDE_FLG")) ? "○" : "");
                        svf.VrsOut("SPORTS", "1".equals(rs.getString("SPORTS_FLG")) ? "○" : "");
                        svf.VrsOut("GENERAL_FLG", rs.getString("GENERAL_FLG_NAME"));
                        svf.VrsOut("DORMITORY", "1".equals(rs.getString("DORMITORY_FLG")) ? "○" : "");
                    } else if (_param.isGojo()) {
                        svf.VrsOut("SLIDE_FLG", "");
                        svf.VrsOut("SPORTS", "1".equals(rs.getString("SPORTS_FLG")) ? "○" : "");
                        svf.VrsOut("GENERAL_FLG", rs.getString("GENERAL_FLG_NAME"));
                        svf.VrsOut("DORMITORY", "1".equals(rs.getString("DORMITORY_FLG")) ? "○" : "");
                    }
                    if (null != rs.getString("REMARK1"))            remark.append(rs.getString("REMARK1"));
                    svfVrsOutFormat(svf, "REMARK"  ,10, remark.toString(), rs.getString("REMARK2"));// 備考
                } else if ("1".equals(_param._applicantDiv)) { // 保護者情報出力
                    //和_2,五_2G
                    String address = (rs.getString("ADDRESS1")!=null) ? rs.getString("ADDRESS1") + " "  : "";
                    address += (rs.getString("ADDRESS2")!=null) ? rs.getString("ADDRESS2")  : "";
                    if (address.length() <= 25) {
                        svf.VrsOut("ADDRESS1", address);
                    } else if (address.length() <= 35) {
                        svf.VrsOut("ADDRESS2", address);
                    } else {
                        svf.VrsOut("ADDRESS3", address);
                    }

                    String shiftDesire = (!"".equals(shiftDesireFlg) && null != shiftDesireFlg) ? "○" : "";
                    if (_param.isCollege()) {
                        shiftDesire = "1".equals(shiftDesireFlg) ? "五併" : "2".equals(shiftDesireFlg) ? "和併" : "3".equals(shiftDesireFlg) ? "五併/和併" : "";
                    }
                    svf.VrsOut("SHIFT", shiftDesire); // 移行希望

                    // 前期/後期出願　A/B出願
                    if (_param.isCollege() || null != rs.getString("OTHER_TESTDIV_NAME")) {
                        String otherTestdivName = _param.isCollege() ? "" : rs.getString("OTHER_TESTDIV_NAME") + " ";
                        String recomExamno = null != rs.getString("RECOM_EXAMNO") ? rs.getString("RECOM_EXAMNO") : "";
                        svf.VrsOut("APPLICATION", otherTestdivName + recomExamno);
                    }

                    if (_param.isGojo()) {
                        svf.VrsOut("PRENO", rs.getString("PRE_RECEPTNO"));
                        StringBuffer remark = new StringBuffer();
                        if ("1".equals(rs.getString("SPORTS_FLG")) || "1".equals(rs.getString("DORMITORY_FLG"))) {
                            String sports = "1".equals(rs.getString("SPORTS_FLG")) ? "スポ希望" + " " : "";
                            String dormitory = "1".equals(rs.getString("DORMITORY_FLG")) ? "入寮希望" + " " : "";
                            remark.append(sports);
                            remark.append(dormitory);
                        }
                        if (null != rs.getString("REMARK1"))            remark.append(rs.getString("REMARK1"));
                        svfVrsOutFormat(svf, "REMARK"  ,20, remark.toString(), rs.getString("REMARK2"));// 備考
                    } else {
                        svfVrsOutFormat(svf, "REMARK"  ,20, rs.getString("REMARK1"), rs.getString("REMARK2"));// 備考
                    }
                }
                //現在ページ数判断用
                gyo++;
                nonedata = true;

                //レコード数カウント
                reccnt++;
                if (rs.getString("SEX") != null) {
                    if (rs.getString("SEX").equals("1")) reccnt_man++;
                    if (rs.getString("SEX").equals("2")) reccnt_woman++;
                }
            }
            //最終レコードを出力
            if (nonedata) {
                if (_param.isGojo()) {
                    svf.VrsOut("TOTAL_NAME", "計");
                    svf.VrsOut("TOTAL_BUSUSE1", String.valueOf(bususe1Count));
                    svf.VrsOut("TOTAL_BUSUSE2", String.valueOf(bususe2Count));
                    svf.VrsOut("TOTAL_BUSUSE3", String.valueOf(bususe3Count));
                }
                //最終ページに男女合計を出力
                svf.VrsOut("NOTE" ,"男"+String.valueOf(reccnt_man)+"名,女"+String.valueOf(reccnt_woman)+"名,合計"+String.valueOf(reccnt)+"名");
                svf.VrEndRecord();//レコードを出力
                setSvfInt(svf);         //ブランクセット
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setSvfout set error!",ex);
        }
        return nonedata;
    }


    /**受験者一覧を取得**/
    private String preStat1(String testDiv,String shDiv,String selSubDiv)
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" SELECT ");
            stb.append("     row_number() over(ORDER BY T1.EXAMNO) AS SEQNO, ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     T1.NAME, ");
            stb.append("     T1.SEX, ");
            stb.append("     T5.ABBV1 AS SEX_NAME, ");
            stb.append("     T1.SHDIV, ");
            stb.append("     T1.SUB_ORDER, ");
            stb.append("     T1.FS_NAME, ");
            stb.append("     T7.NAME1 AS RITU_NAME, ");
            stb.append("     T6.FINSCHOOL_DISTCD AS FINSCHOOLCD, ");
            stb.append("     VALUE(T6.FINSCHOOL_NAME_ABBV, T6.FINSCHOOL_NAME) AS FINSCHOOL_NAME, ");
            stb.append("     T8.ABBV1 AS ERA_ABBV, ");
            stb.append("     T1.BIRTH_Y, ");
            stb.append("     T1.BIRTH_M, ");
            stb.append("     T1.BIRTH_D, ");
            stb.append("     T9.ZIPCD, ");
            stb.append("     T10.PREF_NAME AS PREF_NAME, ");
            stb.append("     T9.ADDRESS1, ");
            stb.append("     T9.ADDRESS2, ");
            stb.append("     T9.TELNO, ");
            stb.append("     T9.GNAME, ");
            stb.append("     CASE WHEN T1.TESTDIV1 IS NOT NULL ");
            stb.append("          THEN N1.ABBV1 || '/' || N2.ABBV1 "); // 第２志望あり
            stb.append("          ELSE N1.ABBV1 "); // 第１志望のみ
            stb.append("     END AS TESTDIV_ABBV, ");
            stb.append("     T1.REMARK1, ");
            stb.append("     T1.REMARK2, ");
            stb.append("     T1.RECOM_EXAMNO, ");
            stb.append("     T13.ABBV1 AS OTHER_TESTDIV_NAME, ");
            stb.append("     T12.JUDGEMENT AS OTHER_JUDGEMENT, ");
            stb.append("     T1.SHIFT_DESIRE_FLG, ");
            stb.append("     T1.SPORTS_FLG, ");
            stb.append("     T1.DORMITORY_FLG, ");
            stb.append("     T1.SLIDE_FLG, ");
            stb.append("     T1.GENERAL_FLG, ");
            stb.append("     NM1.NAME1 AS GENERAL_FLG_NAME, ");
            stb.append("     T1.PRE_RECEPTNO, ");
            stb.append("     P1.PRISCHOOL_NAME, ");
            stb.append("     T1.BUS_USE, ");
            stb.append("     T1.STATIONDIV, ");
            stb.append("     T1.BUS_USER_COUNT ");
            stb.append(" FROM ");
            stb.append("     (SELECT ");
            stb.append("         W1.ENTEXAMYEAR, ");
            stb.append("         W1.TESTDIV, ");
            stb.append("         W1.EXAMNO, ");
            stb.append("         W1.RECOM_EXAMNO, ");
            stb.append("         W1.NAME, ");
            stb.append("         W1.NAME_KANA, ");
            stb.append("         W1.SEX, ");
            stb.append("         W1.SHDIV, ");
            stb.append("         W1.SUB_ORDER, ");
            stb.append("         W1.FS_NAME, ");
            stb.append("         W1.FS_CD, ");
            stb.append("         W1.ERACD, ");
            stb.append("         W1.BIRTH_Y, ");
            stb.append("         W1.BIRTH_M, ");
            stb.append("         W1.BIRTH_D, ");
            stb.append("         W1.SHIFT_DESIRE_FLG, ");
            stb.append("         W1.SPORTS_FLG, ");
            stb.append("         W1.DORMITORY_FLG, ");
            stb.append("         W1.SLIDE_FLG, ");
            stb.append("         W1.GENERAL_FLG, ");
            stb.append("         W1.PRE_RECEPTNO, ");
            stb.append("         W1.PRISCHOOLCD, ");
            stb.append("         W1.TESTDIV1, ");
            stb.append("         W1.REMARK1, ");
            stb.append("         W1.REMARK2, ");
            stb.append("         W1.BUS_USE, ");
            stb.append("         W1.STATIONDIV, ");
            stb.append("         W1.BUS_USER_COUNT ");
            stb.append("     FROM ");
            stb.append("         ENTEXAM_APPLICANTBASE_DAT W1 ");
            stb.append("     WHERE ");
            stb.append("         W1.ENTEXAMYEAR='"+_param._year+"' AND ");
            stb.append("         W1.APPLICANTDIV='"+_param._applicantDiv+"' AND ");
            stb.append("         W1.TESTDIV='"+testDiv+"' ");
            if (!"9".equals(shDiv)) {
                stb.append("         AND W1.SHDIV='"+shDiv+"' ");
            }
            if (!"9".equals(selSubDiv)) {
                stb.append("         AND W1.SELECT_SUBCLASS_DIV='"+selSubDiv+"' ");
            }
            stb.append("     ) T1 ");
            stb.append("     LEFT JOIN NAME_MST T5 ON ");
            stb.append("                 T5.NAMECD1='Z002' AND ");
            stb.append("                 T5.NAMECD2=T1.SEX ");
            stb.append("     LEFT JOIN FINSCHOOL_MST T6 ON ");
            stb.append("                 T6.FINSCHOOLCD = T1.FS_CD ");
            stb.append("     LEFT JOIN NAME_MST T7 ON ");
            stb.append("                 T7.NAMECD1 = 'L001' AND ");
            stb.append("                 T7.NAMECD2 = T6.FINSCHOOL_DISTCD ");
            stb.append("     LEFT JOIN NAME_MST T8 ON ");
            stb.append("                 T8.NAMECD1 = 'L007' AND ");
            stb.append("                 T8.NAMECD2 = T1.ERACD ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T9 ON ");
            stb.append("                 T9.ENTEXAMYEAR = T1.ENTEXAMYEAR AND ");
            stb.append("                 T9.EXAMNO = T1.EXAMNO ");
            stb.append("     LEFT JOIN PREF_MST T10 ON ");
            stb.append("                 T10.PREF_CD = T9.PREF_CD ");
            stb.append("     LEFT JOIN PREF_MST T11 ON ");
            stb.append("                 T11.PREF_CD = T9.GPREF_CD ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T12 ON ");
            stb.append("                 T12.ENTEXAMYEAR=T1.ENTEXAMYEAR AND ");
            stb.append("                 T12.EXAMNO=T1.RECOM_EXAMNO AND ");
            stb.append("                 T12.APPLICANTDIV = '1' AND ");
            stb.append("                 T12.TESTDIV <> T1.TESTDIV ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT R1 ON ");
            stb.append("                 R1.ENTEXAMYEAR=T1.ENTEXAMYEAR AND ");
            stb.append("                 R1.EXAMNO=T1.RECOM_EXAMNO AND ");
            stb.append("                 R1.APPLICANTDIV = '1' AND ");
            if (_param.isCollege()) {
                stb.append("                 R1.TESTDIV <> T1.TESTDIV ");
            } else {
                stb.append("                 R1.TESTDIV = '1' ");
            }
            stb.append("     LEFT JOIN NAME_MST T13 ON ");
            stb.append("                 T13.NAMECD1 = 'L004' AND ");
            stb.append("                 T13.NAMECD2 = T12.TESTDIV ");
            stb.append("     LEFT JOIN PRISCHOOL_MST P1 ON ");
            stb.append("         P1.PRISCHOOLCD = T1.PRISCHOOLCD ");
            stb.append("     LEFT JOIN NAME_MST NM1 ON ");
            stb.append("                 NM1.NAMECD1 = 'L027' AND ");
            stb.append("                 NM1.NAMECD2 = T1.GENERAL_FLG ");
            stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L004' AND N1.NAMECD2 = T1.TESTDIV ");
            stb.append("     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'L004' AND N2.NAMECD2 = T1.TESTDIV1 ");
            if (_param._output) {
                stb.append(" WHERE ");
                stb.append("     T12.JUDGEMENT IS NULL OR T12.JUDGEMENT != '1' ");
                if (_param.isCollege()) {
                    stb.append("     OR (VALUE(T12.TESTDIV,'') IN ('1','7') AND VALUE(T12.SHDIV,'') IN ('8','B') AND VALUE(R1.JUDGEDIV,'') = '9') ");
                } else if (_param.isGojo()) {
                    stb.append("     OR (VALUE(T12.TESTDIV,'') = '1' AND VALUE(T12.SHDIV,'') IN ('2','7') AND VALUE(R1.JUDGEDIV,'') = '9') ");
                } else {
                    stb.append("     OR (VALUE(T12.TESTDIV,'') = '1' AND VALUE(R1.JUDGEDIV,'') = '9') ");
                }
            }
            stb.append(" ORDER BY ");
            stb.append("     T1.EXAMNO ");
        } catch( Exception e ){
            log.error("preStat1 error!");
        }
        return stb.toString();

    }//preStat1()の括り



    /**総ページ数を取得**/
    private String preStatTotalPage(String testDiv,String shDiv,String selSubDiv)
    {
        StringBuffer stb = new StringBuffer();
    //  パラメータ（なし）
        try {
            stb.append("SELECT ");
            stb.append("    SUM(T1.TEST_CNT) TOTAL_PAGE  ");
            stb.append("FROM ");
            stb.append("    (SELECT CASE WHEN MOD(COUNT(*),50) > 0 THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END TEST_CNT  ");
            stb.append("     FROM   ENTEXAM_APPLICANTBASE_DAT W1  ");
            stb.append("     WHERE  W1.ENTEXAMYEAR='"+_param._year+"'  ");
            stb.append("            AND W1.APPLICANTDIV='"+_param._applicantDiv+"' ");
            stb.append("            AND W1.TESTDIV= '"+testDiv+"' ");
            if (!"9".equals(shDiv)) {
                stb.append("            AND W1.SHDIV='"+shDiv+"' ");
            }
            if (!"9".equals(selSubDiv)) {
                stb.append("            AND W1.SELECT_SUBCLASS_DIV='"+selSubDiv+"' ");
            }
            if (_param._output) {
                stb.append("        AND NOT EXISTS ( ");
                stb.append("            SELECT 'X' FROM ENTEXAM_APPLICANTBASE_DAT T12 ");
                stb.append("                            LEFT JOIN ENTEXAM_RECEPT_DAT R1 ON ");
                stb.append("                                        R1.ENTEXAMYEAR=W1.ENTEXAMYEAR AND ");
                stb.append("                                        R1.EXAMNO=W1.RECOM_EXAMNO AND ");
                stb.append("                                        R1.APPLICANTDIV = '1' AND ");
                if (_param.isCollege()) {
                    stb.append("                                    R1.TESTDIV <> W1.TESTDIV ");
                } else {
                    stb.append("                                    R1.TESTDIV = '1' ");
                }
                stb.append("            WHERE T12.ENTEXAMYEAR = W1.ENTEXAMYEAR ");
                stb.append("              AND T12.EXAMNO = W1.RECOM_EXAMNO ");
                stb.append("              AND T12.APPLICANTDIV = '1' ");
                stb.append("              AND T12.TESTDIV <> W1.TESTDIV ");
                stb.append("              AND T12.JUDGEMENT = '1' ");
                if (_param.isCollege()) {
                    stb.append("          AND NOT (VALUE(T12.TESTDIV,'') IN ('1','7') AND VALUE(T12.SHDIV,'') IN ('8','B') AND VALUE(R1.JUDGEDIV,'') = '9') ");
                } else if (_param.isGojo()) {
                    stb.append("          AND NOT (VALUE(T12.TESTDIV,'') = '1' AND VALUE(T12.SHDIV,'') IN ('2','7') AND VALUE(R1.JUDGEDIV,'') = '9') ");
                } else {
                    stb.append("          AND NOT (VALUE(T12.TESTDIV,'') = '1' AND VALUE(R1.JUDGEDIV,'') = '9') ");
                }
                stb.append("        ) ");
            }
            stb.append("     GROUP BY W1.TESTDIV ) T1  ");
        } catch( Exception e ){
            log.error("preStatTotalPage error!");
        }
        return stb.toString();

    }//preStat2()の括り



    /**PrepareStatement close**/
    private void preStatClose(
        PreparedStatement ps,
        PreparedStatement ps1,
        PreparedStatement ps2
    ) {
        try {
//            ps.close();
            ps1.close();
            ps2.close();
        } catch( Exception e ){
            log.error("preStatClose error!");
        }
    }//preStatClose()の括り



    /**ブランクをセット**/
    private void setSvfInt(
        Vrw32alp svf
    ) {
        try {
            svf.VrsOut("NOTE"   ,"note");
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

    private int svfVrsOutFormat(Vrw32alp svf,String area_name, int len, String sval1, String sval2) {
        if (sval1 == null || "null".equals(sval1)) sval1 = "";
        if (sval2 == null || "null".equals(sval2)) {
            sval2 = "";
        } else if ("ADDRESS".equals(area_name)){
            sval2 = " " + sval2;
        }
        return svf.VrsOut(setformatArea(area_name, len, sval1+sval2), sval1+sval2 );
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

    class Param {

        final String _year;
        final String _applicantDiv;
        final String _testDiv;
        final String _shDiv;
        final String _loginDate;
        final boolean _printType; //1:保護者出力ありか？
        final boolean _output; //合格者を除くか？

        final boolean _seirekiFlg;
        final String _z010SchoolCode;

        Param(DB2UDB db2, HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _shDiv = request.getParameter("SHDIV");
            _loginDate = request.getParameter("LOGIN_DATE");
            _printType = "1".equals(request.getParameter("PRINT_TYPE"));
            _output = "1".equals(request.getParameter("OUTPUT"));
            // 西暦使用フラグ
            _seirekiFlg = getSeirekiFlg(db2);
            _z010SchoolCode = getSchoolCode(db2);
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

        private String getNameMst(DB2UDB db2, String namecd1,String namecd2) {

            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT NAME1 ");
            sql.append(" FROM NAME_MST ");
            sql.append(" WHERE NAMECD1 = '"+namecd1+"' AND NAMECD2 = '"+namecd2+"' ");
            String name = null;
            try{
                PreparedStatement ps = db2.prepareStatement(sql.toString());
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                   name = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.debug(ex);
            }

            return name;
        }

        private String getSchoolName(DB2UDB db2) {
            String certifKindCd = null;
            if ("1".equals(_applicantDiv)) certifKindCd = "105";
            if ("2".equals(_applicantDiv)) certifKindCd = "106";
            if (certifKindCd == null) return null;

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
            } catch (SQLException ex) {
                log.error(ex);
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
