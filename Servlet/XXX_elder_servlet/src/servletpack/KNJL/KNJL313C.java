package servletpack.KNJL;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Calendar;

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
 *                  ＜ＫＮＪＬ３１３Ｃ＞  志願者データチェックリスト
 *
 *  2009/01/07 RTS 作成日
 **/

public class KNJL313C {

    private static final Log log = LogFactory.getLog(KNJL313C.class);
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
        log.fatal("$Revision: 64327 $"); // CVSキーワードの取り扱いに注意
        _param = new Param(db2, request);
        
        // '全て'
        if ("9".equals(_param._testDiv)) {
            if("1".equals(_param._applicantDiv)) _testDivPrint = new String[]{"1","2"};
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
                String sql;
                sql = preStat1(testDiv);
                log.debug("preStat1 sql="+sql);
                ps1 = db2.prepareStatement(sql);        //受験者一覧preparestatement
                
                sql = preStatTotalPage(testDiv);
                log.debug("preStateTotalPage sql="+sql);
                psTotalPage = db2.prepareStatement(sql);        //総ページ数preparestatement
                
                //SVF出力
                if (setSvfMain(db2, svf, ps1, psTotalPage, testDiv)) nonedata = true;  //帳票出力のメソッド
            }
        } catch( Exception ex ) {
            log.error("DB2 prepareStatement set error!");
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

    /** 事前処理 **/
    private void setHeader(
        DB2UDB db2,
        Vrw32alp svf,
        String testDiv
    ) {
        if (_param.isCollege()) {
            svf.VrSetForm("1".equals(_param._applicantDiv) && "1".equals(testDiv) ? "KNJL313C_C_2.frm" : "KNJL313C_C.frm", 4);
        } else {
            svf.VrSetForm(_param.isGojo() ? "KNJL313C_G.frm" : "KNJL313C.frm", 4);
        }
        svf.VrsOut("NENDO", _param.getNendo());
        svf.VrsOut("APPLICANTDIV", _param.getNameMst(db2, "L003", _param._applicantDiv));// 画面から入試制度
        svf.VrsOut("TESTDIV", _param.getNameMst(db2, "L004", testDiv));// 画面から入試区分
        svf.VrsOut("SCHOOLNAME", _param.getSchoolName(db2));// 学校名

        svf.VrsOut( "DATE", _param.getLoginDateString() + _param.getTimeString());

        String fsItem = null;
        if ("1".equals(_param._applicantDiv)) {
            fsItem = "出身小学校";
        } else if ("2".equals(_param._applicantDiv)) {
            fsItem = "出身中学校";
        }
        svf.VrsOut("FS_ITEM", fsItem);

        //  ＳＶＦ属性変更--->改ページ
        svf.VrAttribute("TESTDIV","FF=1");
    }



    /**
     *  svf print 印刷処理
     *            入試区分が指定されていれば( => _param._testDiv !== "9" )１回の処理
     *            入試区分が複数の場合は全ての入試区分を舐める
     */

    private boolean setSvfMain(
            DB2UDB db2,
            Vrw32alp svf,
            PreparedStatement ps1,
            PreparedStatement psTotalpage,
            String testDiv
    ) {
            boolean nonedata = false;

            try {
                if (setSvfout(db2, svf, ps1, psTotalpage, testDiv)) nonedata = true; //帳票出力のメソッド
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
            log.error("setTotalPage set error!"+ex);
        }

    }

    /**帳票出力（受験者一覧をセット）**/
    private boolean setSvfout(
        DB2UDB db2,
        Vrw32alp svf,
        PreparedStatement ps1,
        PreparedStatement psTotalpage,
        String testDiv
    ) {
        boolean nonedata = false;
        try {
            int reccnt_man      = 0;    //男レコード数カウント用
            int reccnt_woman    = 0;    //女レコード数カウント用
            int reccnt = 0;             //合計レコード数
            int pagecnt = 1;            //現在ページ数
            int gyo = 1;                //現在ページ数の判断用（行）
            ResultSet rs = ps1.executeQuery();

            while( rs.next() ){
                if (nonedata == false) {
                    setHeader(db2, svf, testDiv);
                    setTotalPage(db2, svf, psTotalpage); //総ページ数メソッド
                    nonedata = true;
                }
                
                //３０行超えたとき、または、入試区分ブレイクの場合、ページ数カウント
                if (gyo % 30==1) {
                    //ヘッダ
                    svf.VrsOut("PAGE", String.valueOf(pagecnt));      //現在ページ数
                    gyo = 1;
                    pagecnt++;
                }

                //明細
                svf.VrsOut("EXAMNO", rs.getString("EXAMNO")); // 受験番号
                svfVrsOutFormat(svf, "NAME", 12, rs.getString("NAME"), ""); // 名前
                svfVrsOutFormat(svf, "KANA", 15, rs.getString("NAME_KANA"), ""); // ふりがな
                //専併区分
                if (_param.isCollege()) {
                    svf.VrsOut("SLIDE_FLG", "1".equals(rs.getString("SLIDE_FLG")) ? "○" : "");
                    svf.VrsOut("SELECT_SUBCLASS_DIV7", "7".equals(rs.getString("SELECT_SUBCLASS_DIV")) ? "○" : "");
                    svf.VrsOut("SELECT_SUBCLASS_DIV6", "6".equals(rs.getString("SELECT_SUBCLASS_DIV")) ? "○" : "");
                    svf.VrsOut("DORMITORY", "1".equals(rs.getString("DORMITORY_FLG")) ? "○" : "");
                    if ("1".equals(_param._applicantDiv)) {
                        svf.VrsOut("APPLYDIV", "1".equals(rs.getString("SHDIV")) || "6".equals(rs.getString("SHDIV")) || "7".equals(rs.getString("SHDIV")) || "8".equals(rs.getString("SHDIV")) ? "○" : "");
                        svf.VrsOut("HOPE_COURSE1", "6".equals(rs.getString("SHDIV")) || "9".equals(rs.getString("SHDIV")) ? "G" : "7".equals(rs.getString("SHDIV")) || "A".equals(rs.getString("SHDIV")) ? "S" : "8".equals(rs.getString("SHDIV")) || "B".equals(rs.getString("SHDIV")) ? "S/G" : "");
                        if ("1".equals(testDiv)) {
                            svf.VrsOut("SH_DIV", "1".equals(rs.getString("SUB_ORDER")) ? "Ⅰ型" : "2".equals(rs.getString("SUB_ORDER")) ? "Ⅱ型" : "");
                        }
                    } else {
                        svf.VrsOut("APPLYDIV", "3".equals(rs.getString("SHDIV")) || "4".equals(rs.getString("SHDIV")) || "5".equals(rs.getString("SHDIV")) ? "○" : "");
                        svf.VrsOut("HOPE_COURSE1", "3".equals(rs.getString("SHDIV")) ? "EA" : "4".equals(rs.getString("SHDIV")) ? "ES" : "5".equals(rs.getString("SHDIV")) ? "EA/ES" : "");
                    }
                } else if (_param.isGojo()) {
                    svf.VrsOut("SLIDE_FLG", "");
                    svf.VrsOut("SELECT_SUBCLASS_DIV7", "8".equals(testDiv) &&  "1".equals(rs.getString("SPORTS_FLG")) ? "○" : "");
                    svf.VrsOut("SELECT_SUBCLASS_DIV6", "8".equals(testDiv) && !"1".equals(rs.getString("SPORTS_FLG")) ? "○" : "");
                    svf.VrsOut("DORMITORY", "1".equals(rs.getString("DORMITORY_FLG")) ? "○" : "");
                    svf.VrsOut("APPLYDIV", "1".equals(rs.getString("SHDIV")) ? "○" : "6".equals(rs.getString("SHDIV")) ? "Ⅰ" : "7".equals(rs.getString("SHDIV")) ? "Ⅱ" : "8".equals(rs.getString("SHDIV")) ? "Ⅲ" : "");
                } else {
                    svf.VrsOut("APPLYDIV", rs.getString("SHDIV_NAME"));
                }
                svf.VrsOut("SEX", rs.getString("SEX_NAME")); // 性別
                svf.VrsOut("BIRTHDAY", rs.getString("BIRTHDAY")); // 生年月日
                svf.VrsOut("ZIPCODE"  ,rs.getString("ZIPCD")); // 郵便番号
                svf.VrsOut("PREF"  ,rs.getString("PREF_NAME")); // 出身都道府県名
                svfVrsOutFormat(svf, "ADDRESS1_"  ,18, rs.getString("ADDRESS1"), "");// 住所1
                svfVrsOutFormat(svf, "ADDRESS2_"  ,18, rs.getString("ADDRESS2"), "");// 住所2
                if (_param.isGojo()) {
                    String juku = rs.getString("PRISCHOOL_NAME"); // 塾名
                    svf.VrsOut("JUKU" + (getMS932ByteCount(juku) > 34 ? "3" : getMS932ByteCount(juku) > 20 ? "2" : ""), juku);
                } else {
                    svf.VrsOut("EDBOARD", rs.getString("EDBOARDABBV")); // 教育委員会
                    svf.VrsOut("TELNO"  ,rs.getString("TELNO")); // 電話番号
                    svf.VrsOut("FS_AREA", rs.getString("FS_AREA_NAME")); // 学校所在地
                    svf.VrsOut("FS_NATPUBPRIDIV", rs.getString("FS_NATPUBPRINAME")); //学校区分
                }
                svfVrsOutFormat(svf, "FINSCHOOL"  ,13, nvlT(rs.getString("FINSCHOOL_NAME")), ""); // 学校名

                svfVrsOutFormat(svf, "GUARD_NAME"  ,12, rs.getString("GNAME"), ""); // 保護者名
                svfVrsOutFormat(svf, "GUARD_KANA"  ,15, rs.getString("GKANA"), ""); // 保護者ふりがな
                svf.VrsOut("RELATIONSHIP", rs.getString("RELATIONSHIP_NAME")); // 保護者続柄
                svf.VrsOut("GUARD_TELNO"  ,rs.getString("GTELNO")); // 保護者電話番号
                StringBuffer remark = new StringBuffer();
                final String shiftDesireFlg = rs.getString("SHIFT_DESIRE_FLG");
                if (null != rs.getString("OTHER_TESTDIV_NAME") || (!"".equals(shiftDesireFlg) && null != shiftDesireFlg)) {
                    String kibou = _param.isGojo() ? "カレッジ併願" : "移行希望";
                    if (_param.isCollege()) {
                        kibou = "1".equals(shiftDesireFlg) ? "五併" : "2".equals(shiftDesireFlg) ? "和併" : "3".equals(shiftDesireFlg) ? "五併/和併" : "";
                    }
                    String otherTestdivName = null != rs.getString("OTHER_TESTDIV_NAME") ? rs.getString("OTHER_TESTDIV_NAME") + " " : "";
                    String shiftDesire = (!"".equals(shiftDesireFlg) && null != shiftDesireFlg) ? kibou + " " : "";
                    String recomExamno = null != rs.getString("RECOM_EXAMNO") ? rs.getString("RECOM_EXAMNO") + " " : ""; 
                    remark.append(otherTestdivName);
                    remark.append(recomExamno);
                    remark.append(shiftDesire);
                }
                if (null != rs.getString("REMARK1"))            remark.append(rs.getString("REMARK1"));
                svfVrsOutFormat(svf, "REMARK"  ,12, remark.toString(), rs.getString("REMARK2"));// 備考
                svf.VrEndRecord(); //レコードを出力

                //現在ページ数判断用
                gyo++;

                //レコード数カウント
                reccnt++;
                if (rs.getString("SEX") != null) {
                    if (rs.getString("SEX").equals("1")) reccnt_man++;
                    if (rs.getString("SEX").equals("2")) reccnt_woman++;
                }
            }
            //最終レコードを出力
            if (nonedata) {
                //最終ページに男女合計を出力
                svf.VrsOut("NOTE" ,"男"+String.valueOf(reccnt_man)+"名,女"+String.valueOf(reccnt_woman)+"名,合計"+String.valueOf(reccnt)+"名");
                svf.VrEndRecord();//レコードを出力
                setSvfInt(svf);         //ブランクセット
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setSvfout set error!"+ex);
        }
        return nonedata;
    }
    
    private int getMS932ByteCount(final String str) {
        if (null == str) return 0;
        int ret = 0;
        try {
            ret = str.getBytes("MS932").length;
        } catch (Exception e) {
            log.error("exception!", e);
        }
        return ret;
    }


    /**対象者一覧を取得**/ 
    private String preStat1(String testDiv)
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" SELECT  ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     T1.NAME, ");
            stb.append("     T1.NAME_KANA, ");
            stb.append("     CASE WHEN T1.TESTDIV1 IS NOT NULL ");
            stb.append("          THEN N1.ABBV1 || '/' || N2.ABBV1 "); // 第２志望あり
            stb.append("          ELSE N1.ABBV1 "); // 第１志望のみ
            stb.append("     END AS TESTDIV_ABBV, ");
            stb.append("     T1.TESTDIV1, ");
            stb.append("     N1.ABBV1 AS TESTDIV_ABBV1, ");
            stb.append("     N2.ABBV1 AS TESTDIV_ABBV2, ");
            stb.append("     T1.SEX, ");
            stb.append("     T3.ABBV1 AS SEX_NAME, ");
            stb.append("     T1.SHDIV, ");
            stb.append("     L1.NAME1 AS SHDIV_NAME, ");
            stb.append("     T1.SUB_ORDER, ");
            stb.append("     T9.ABBV1 || T1.BIRTH_Y || T1.BIRTH_M || T1.BIRTH_D AS BIRTHDAY, ");
            stb.append("     T2.ZIPCD, ");
            stb.append("     T4.PREF_NAME, ");
            stb.append("     T2.ADDRESS1, ");
            stb.append("     T2.ADDRESS2, ");
            stb.append("     L2.EDBOARDABBV, ");
            stb.append("     T2.TELNO, ");
            stb.append("     T6.NAME1 AS FS_AREA_NAME, ");
            stb.append("     T7.NAME1 AS FS_NATPUBPRINAME, ");
            stb.append("     VALUE(T5.FINSCHOOL_NAME_ABBV, T5.FINSCHOOL_NAME) AS FINSCHOOL_NAME, ");
            stb.append("     P1.PRISCHOOL_NAME, ");
            stb.append("     T2.GKANA, ");
            stb.append("     T2.GNAME, ");
            stb.append("     T2.RELATIONSHIP, ");
            stb.append("     T8.NAME1 AS RELATIONSHIP_NAME, ");
            stb.append("     T2.GTELNO, ");
            stb.append("     T1.REMARK1, ");
            stb.append("     T1.REMARK2, ");
            stb.append("     T1.RECOM_EXAMNO, ");
            stb.append("     T11.ABBV1 AS OTHER_TESTDIV_NAME, ");
            stb.append("     T1.SPORTS_FLG, ");
            stb.append("     T1.SLIDE_FLG, ");
            stb.append("     T1.SELECT_SUBCLASS_DIV, ");
            stb.append("     T1.DORMITORY_FLG, ");
            stb.append("     T1.SHIFT_DESIRE_FLG ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T2 ON ");
            stb.append("         T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND ");
            stb.append("         T1.EXAMNO = T2.EXAMNO ");
            stb.append("     LEFT JOIN NAME_MST T3 ON ");
            stb.append("         T3.NAMECD1 = 'Z002' AND ");
            stb.append("         T3.NAMECD2 = T1.SEX ");
            stb.append("     LEFT JOIN PREF_MST T4 ON ");
            stb.append("         T4.PREF_CD = T2.PREF_CD ");
            stb.append("     LEFT JOIN FINSCHOOL_MST T5 ON ");
            stb.append("         T5.FINSCHOOLCD = T1.FS_CD ");
            stb.append("     LEFT JOIN NAME_MST T6 ON ");
            stb.append("         T6.NAMECD1 = 'Z003' AND ");
            stb.append("         T6.NAMECD2 = T5.DISTRICTCD ");
            stb.append("     LEFT JOIN NAME_MST T7 ON ");
            stb.append("         T7.NAMECD1 = 'L001' AND ");
            stb.append("         T7.NAMECD2 = T5.FINSCHOOL_DISTCD ");
            stb.append("     LEFT JOIN NAME_MST T8 ON ");
            stb.append("         T8.NAMECD1 = 'H201' AND ");
            stb.append("         T8.NAMECD2 = T2.RELATIONSHIP ");
            stb.append("     LEFT JOIN NAME_MST T9 ON ");
            stb.append("         T9.NAMECD1 = 'L007' AND ");
            stb.append("         T9.NAMECD2 = T1.ERACD ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T10 ON ");
            stb.append("         T10.ENTEXAMYEAR=T1.ENTEXAMYEAR AND ");
            stb.append("         T10.EXAMNO=T1.RECOM_EXAMNO AND ");
            stb.append("         T10.APPLICANTDIV = '1' AND ");
            stb.append("         T10.TESTDIV <> T1.TESTDIV ");
            stb.append("     LEFT JOIN NAME_MST T11 ON ");
            stb.append("         T11.NAMECD1 = 'L004' AND ");
            stb.append("         T11.NAMECD2 = T10.TESTDIV ");
            stb.append("     LEFT JOIN NAME_MST L1 ON ");
            stb.append("         L1.NAMECD1 = 'L006' AND ");
            stb.append("         L1.NAMECD2 = T1.SHDIV ");
            stb.append("     LEFT JOIN EDBOARD_MST L2 ON ");
            stb.append("         L2.EDBOARDCD = T2.EDBOARDCD ");
            stb.append("     LEFT JOIN PRISCHOOL_MST P1 ON ");
            stb.append("         P1.PRISCHOOLCD = T1.PRISCHOOLCD ");
            stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L004' AND N1.NAMECD2 = T1.TESTDIV ");
            stb.append("     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'L004' AND N2.NAMECD2 = T1.TESTDIV1 ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '"+_param._year+"' AND ");
            stb.append("     T1.APPLICANTDIV = '"+_param._applicantDiv+"' AND ");
            stb.append("     T1.TESTDIV = '"+testDiv+"' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.EXAMNO ");
            
        } catch( Exception e ){
            log.error("preStat1 error!");
        }
        return stb.toString();

    }//preStat1()の括り



    /**総ページ数を取得**/
    private String preStatTotalPage(String testDiv)
    {
        StringBuffer stb = new StringBuffer();
    //  パラメータ（なし）
        try {
            stb.append("SELECT ");
            stb.append("    SUM(T1.TEST_CNT) TOTAL_PAGE  ");
            stb.append("FROM ");
            stb.append("    (SELECT CASE WHEN MOD(COUNT(*),30) > 0 THEN COUNT(*)/30 + 1 ELSE COUNT(*)/30 END TEST_CNT  ");
            stb.append("     FROM   ENTEXAM_APPLICANTBASE_DAT W1");
            stb.append("     WHERE  W1.ENTEXAMYEAR='"+_param._year+"'  ");
            stb.append("            AND W1.APPLICANTDIV='"+_param._applicantDiv+"' ");
            stb.append("            AND W1.TESTDIV= '"+testDiv+"' ");
            stb.append("     ) T1  ");
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
            if (ps != null) ps.close();
            if (ps1 != null) ps1.close();
            if (ps2 != null) ps2.close();
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
        final String _loginDate;

        final boolean _seirekiFlg;
        final String _z010SchoolCode;

        Param(DB2UDB db2, HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _loginDate = request.getParameter("LOGIN_DATE");
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
                            KNJ_EditDate.h_format_JP(dateFormat) ;
            }
            return "";
        }

        String getTimeString() {
            if (null != _loginDate) {
                Calendar cal = Calendar.getInstance();
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);
                DecimalFormat df = new DecimalFormat("00");
                return df.format(hour) + "時" + df.format(minute) + "分現在";
            }
            return "";
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
