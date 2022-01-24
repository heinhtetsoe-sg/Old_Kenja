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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２８Ｃ＞  合否別一覧表
 *
 *  2008/11/05 RTS 作成日
 **/

public class KNJL328C {

    private static final Log log = LogFactory.getLog(KNJL328C.class);
    private String[] _testDivPrint;
    private boolean _seirekiFlg;
    private String _z010SchoolCode;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[5];

        //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                        //年度
            param[1] = request.getParameter("APPLICANTDIV");                //入試制度
            param[2] = request.getParameter("TESTDIV");                      //入試区分
            param[3] = request.getParameter("LOGIN_DATE");                  // ログイン日付
            param[4] = request.getParameter("PRINT_TYPE");                  //出力範囲ラジオボタン 1:合格者 2:受験者 3:志願者全員
        } catch( Exception ex ) {
            log.error("parameter error!");
        }

        //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }

        _z010SchoolCode = getSchoolCode(db2);
        // '全て'
        if ("9".equals(param[2])) {
            if("1".equals(param[1])) _testDivPrint = new String[]{"1","2"};
            if("2".equals(param[1])) {
                if (isGojo()) {
                    _testDivPrint = new String[]{"3","4","5","7"};
                } else {
                    _testDivPrint = new String[]{"3","4","5"};
                }
            }
        } else {
            _testDivPrint = new String[]{param[2]};
        }
        _seirekiFlg = getSeirekiFlg(db2);

        //  print設定
        response.setContentType("application/pdf");

        //  svf設定
        svf.VrInit();                             //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定


        //  ＳＶＦ作成処理
        PreparedStatement ps1 = null;
        PreparedStatement psTotalPage = null;
        boolean nonedata = false;                               //該当データなしフラグ
        for(int i=0 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);

        String year=param[0];
        String applicantDiv = param[1];
        String printType = param[4];


        //SQL作成
        try {
            for (int i=0; i<_testDivPrint.length; i++) {
                String testDiv = _testDivPrint[i];

                String sql;
                sql = preStat1(year, applicantDiv, testDiv, printType);
                log.debug("preStat1 sql="+sql);
                ps1 = db2.prepareStatement(sql);        //受験者一覧preparestatement

                sql = preStatTotalPage(year, applicantDiv, testDiv, printType);
                log.debug("preStateTotalPage sql="+sql);
                psTotalPage = db2.prepareStatement(sql);        //総ページ数preparestatement

                //SVF出力
                if (setSvfMain(db2, svf, param, ps1, psTotalPage, testDiv)) nonedata = true;  //帳票出力のメソッド
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
        preStatClose(ps1,psTotalPage);       //preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる
    }//doGetの括り

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
        } catch(SQLException ex) {
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

    private String getSchoolName(DB2UDB db2, String year, String applicantDiv) {
        String certifKindCd = null;
        if ("1".equals(applicantDiv)) certifKindCd = "105";
        if ("2".equals(applicantDiv)) certifKindCd = "106";
        if (certifKindCd == null) return null;

        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT SCHOOL_NAME ");
        sql.append(" FROM CERTIF_SCHOOL_DAT ");
        sql.append(" WHERE YEAR = '"+year+"' AND CERTIF_KINDCD = '"+certifKindCd+"' ");
        String name = null;
        try{
            PreparedStatement ps = db2.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
               name = rs.getString("SCHOOL_NAME");
            }
        } catch(SQLException ex) {
            log.debug(ex);
        }

        return name;
    }

    /** 表紙をセット **/
    private void setCover(
            DB2UDB db2,
            Vrw32alp svf,
            String param[],
            String testDiv)
    {
        String title = "【合否別一覧表】";
        svf.VrSetForm("KNJLCVR001C.frm", 4);
        String year = _seirekiFlg ? param[0]+"年度":
            KNJ_EditDate.h_format_JP_N(param[0]+"-01-01")+"度";
        svf.VrsOut("NENDO", year);
        svf.VrsOut("APPLICANTDIV", getNameMst(db2, "L003", param[1]));// 画面から入試制度
        svf.VrsOut("TESTDIV",      getNameMst(db2, "L004", testDiv));// 画面から入試区分
        svf.VrsOut("SCHOOLNAME", getSchoolName(db2, param[0], param[1]));// 学校名
        svf.VrsOut("TITLE", title);
        svf.VrsOut("NOTE" ,"");
        svf.VrEndRecord();//レコードを出力
    }

    /** 事前処理 **/
    private void setHeader(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        String testDiv
    ) {
        String formId = isCollege() ? "KNJL328C_C.frm" : isGojo() ? "KNJL328C_G.frm" : "KNJL328C.frm";
        svf.VrSetForm(formId, 4);
        String year = _seirekiFlg ? param[0]+"年度":
            KNJ_EditDate.h_format_JP_N(param[0]+"-01-01")+"度";
        svf.VrsOut("NENDO", year);
        svf.VrsOut("APPLICANTDIV", getNameMst(db2, "L003", param[1]));// 画面から入試制度
        svf.VrsOut("TESTDIV", getNameMst(db2, "L004", testDiv));// 画面から入試区分
        svf.VrsOut("SCHOOLNAME", getSchoolName(db2, param[0], param[1]));// 学校名

        String date = "";
        if (null != param[3]) {
            date =  _seirekiFlg ?
                    param[3].substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(param[3]):
                        KNJ_EditDate.h_format_JP( param[3] ) ;
        }
        svf.VrsOut( "DATE", date);

        String fsItem = null;
        if ("1".equals(param[1])) {
            fsItem = "出身小学校";
        } else if ("2".equals(param[1])) {
            fsItem = "出身中学校";
        }
        svf.VrsOut("FS_ITEM", fsItem);

        //  ＳＶＦ属性変更--->改ページ
        svf.VrAttribute("TESTDIV","FF=1");
    }



    /**
     *  svf print 印刷処理
     *            入試区分が指定されていれば( => param[2] !== "9" )１回の処理
     *            入試区分が複数の場合は全ての入試区分を舐める
     */

    private boolean setSvfMain(
            DB2UDB db2,
            Vrw32alp svf,
            String param[],
            PreparedStatement ps1,
            PreparedStatement psTotalpage,
            String testDiv
        ) {
            boolean nonedata = false;

            try {
                    if (setSvfout(db2, svf, param, ps1, psTotalpage, testDiv)) nonedata = true; //帳票出力のメソッド
                    db2.commit();
            } catch( Exception ex ) {
                log.error("setSvfMain set error!");
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
            log.error("setTotalPage set error! ="+ex);
        }

    }

    /**帳票出力（受験者一覧をセット）**/
    private boolean setSvfout(
        DB2UDB db2,
        Vrw32alp svf,
        String[] param,
        PreparedStatement ps1,
        PreparedStatement psTotalpage,
        String testDiv
    ) {
        boolean nonedata = false;
        int reccnt_man      = 0;    //男レコード数カウント用
        int reccnt_woman    = 0;    //女レコード数カウント用
        int reccnt = 0;             //合計レコード数
        ResultSet rs = null;
        try {
            rs = ps1.executeQuery();

            int pagecnt = 1;            //現在ページ数
            int gyo = 1;                //現在ページ数の判断用（行）
            while (rs.next()) {
                if (nonedata == false) {
                    setCover(db2, svf, param, testDiv);
                    setHeader(db2, svf, param, testDiv);
                    setTotalPage(db2, svf, psTotalpage); //総ページ数メソッド
                    nonedata = true;
                }

                //レコードを出力
                if (reccnt > 0) svf.VrEndRecord();
                //５０行超えたとき、または、入試区分ブレイクの場合、ページ数カウント
                if ((gyo % 50==1)) {
                    //ヘッダ
                    svf.VrsOut("PAGE", String.valueOf(pagecnt));      //現在ページ数
                    gyo = 1;
                    pagecnt++;
                }
                //明細
                svf.VrsOut("NUMBER", rs.getString("SEQNO"));       //連番
                svf.VrsOut("EXAMNO", rs.getString("EXAMNO"));       //受験番号
                svf.VrsOut(setformatArea("NAME", 10, rs.getString("NAME")), rs.getString("NAME")); // 名前
                if (isCollege()) {
                    svf.VrsOut("APPLYDIV", "1".equals(rs.getString("SHDIV")) || "3".equals(rs.getString("SHDIV")) || "4".equals(rs.getString("SHDIV")) || "5".equals(rs.getString("SHDIV")) || "6".equals(rs.getString("SHDIV")) || "7".equals(rs.getString("SHDIV")) || "8".equals(rs.getString("SHDIV")) ? "○" : "");
                } else if (isGojo()) {
                    svf.VrsOut("APPLYDIV", "1".equals(rs.getString("SHDIV")) ? "○" : "6".equals(rs.getString("SHDIV")) ? "Ⅰ" : "7".equals(rs.getString("SHDIV")) ? "Ⅱ" : "8".equals(rs.getString("SHDIV")) ? "Ⅲ" : "");
                } else {
                    svf.VrsOut("APPLYDIV", !"2".equals(rs.getString("SHDIV")) ? "○" : ""); // 専願なら○
                }
                svf.VrsOut("SEX", rs.getString("SEX_NAME"));     //性別
                svf.VrsOut("JUDGE", rs.getString("JUDGEMENT_NAME")); // 判定
                svf.VrsOut(setformatArea("FINSCHOOL", 13, nvlT(rs.getString("FINSCHOOL_NAME"))), nvlT(rs.getString("FINSCHOOL_NAME"))); //出身学校名

                //レコード数カウント
                reccnt++;
                if (rs.getString("SEX") != null) {
                    if (rs.getString("SEX").equals("1")) reccnt_man++;
                    if (rs.getString("SEX").equals("2")) reccnt_woman++;
                }

                // 保護者のADDR_DATA
                svf.VrsOut(setformatArea("GUARD_NAME", 10, rs.getString("GNAME")), rs.getString("GNAME"));
                svf.VrsOut("ZIPCODE"  ,rs.getString("ZIPCD"));
                svf.VrsOut("PREF"  ,rs.getString("PREF_NAME"));
                String address = (rs.getString("ADDRESS1")!=null) ? rs.getString("ADDRESS1") + " "  : "";
                address += (rs.getString("ADDRESS2")!=null) ? rs.getString("ADDRESS2")  : "";
                if (address.length() <= 25) {
                    svf.VrsOut("ADDRESS1", address);
                } else if (address.length() <= 35) {
                    svf.VrsOut("ADDRESS2", address);
                } else {
                    svf.VrsOut("ADDRESS3", address);
                }
                svf.VrsOut("TELNO"  ,rs.getString("TELNO"));

                String shiftDesire = ("1".equals(rs.getString("SHIFT_DESIRE_FLG"))) ? "○" : "";
                svf.VrsOut("SHIFT", shiftDesire); // 移行希望

                // 前期/後期出願
                if (null != rs.getString("OTHER_TESTDIV_NAME")) {
                    String otherTestdivName = rs.getString("OTHER_TESTDIV_NAME") + " ";
                    String recomExamno = null != rs.getString("RECOM_EXAMNO") ? rs.getString("RECOM_EXAMNO") : "";
                    svf.VrsOut("APPLICATION", otherTestdivName + recomExamno);
                }

                svfVrsOutFormat(svf, "REMARK"  , 20, rs.getString("REMARK1"), rs.getString("REMARK2"));// 備考

                //現在ページ数判断用
                gyo++;
            }
        } catch (Exception ex) {
            log.error("setSvfout set error!", ex);
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        //最終レコードを出力
        if (nonedata) {
            //最終ページに男女合計を出力
            svf.VrsOut("NOTE" ,"男"+String.valueOf(reccnt_man)+"名,女"+String.valueOf(reccnt_woman)+"名,合計"+String.valueOf(reccnt)+"名");
            svf.VrEndRecord();//レコードを出力
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

    /**合否別一覧を取得**/
    private String preStat1(String year,String applicantDiv, String testDiv, String printType)
    {
        StringBuffer stb = new StringBuffer();
    //  パラメータ（なし）
        try {
            stb.append(" SELECT ");
            stb.append("     row_number() over(ORDER BY T1.EXAMNO) AS SEQNO, ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     T1.NAME, ");
            stb.append("     T1.NAME_KANA, ");
            stb.append("     T1.SEX, ");
            stb.append("     T5.ABBV1 AS SEX_NAME, ");
            stb.append("     T1.JUDGEMENT_NAME, ");
            stb.append("     T1.FS_NAME, ");
            stb.append("     T7.NAME1 AS RITU_NAME, ");
            stb.append("     T6.FINSCHOOL_DISTCD AS FINSCHOOLCD, ");
            stb.append("     VALUE(T6.FINSCHOOL_NAME_ABBV, T6.FINSCHOOL_NAME) AS FINSCHOOL_NAME, ");
            stb.append("     T9.GNAME, ");
            stb.append("     T9.ZIPCD, ");
            stb.append("     T9.ADDRESS1, ");
            stb.append("     T9.ADDRESS2, ");
            stb.append("     T9.TELNO, ");
            stb.append("     T10.PREF_NAME, ");
            stb.append("     T1.REMARK1, ");
            stb.append("     T1.REMARK2, ");
            stb.append("     T1.RECOM_EXAMNO, ");
            stb.append("     T13.ABBV1 AS OTHER_TESTDIV_NAME, ");
            stb.append("     T1.SHIFT_DESIRE_FLG, ");
            stb.append("     T1.SHDIV ");
            stb.append(" FROM ");
            stb.append("     (SELECT ");
            stb.append("         W1.ENTEXAMYEAR, ");
            stb.append("         W1.TESTDIV, ");
            stb.append("         W1.EXAMNO, ");
            stb.append("         W1.RECOM_EXAMNO, ");
            stb.append("         W1.NAME, ");
            stb.append("         W1.NAME_KANA, ");
            stb.append("         W1.SEX, ");
            stb.append("         W2.JUDGEDIV, ");
            stb.append("         W3.NAME1 AS JUDGEMENT_NAME, ");
            stb.append("         W1.FS_NAME, ");
            stb.append("         W1.FS_CD, ");
            stb.append("         W1.REMARK1, ");
            stb.append("         W1.REMARK2, ");
            stb.append("         W1.SHIFT_DESIRE_FLG, ");
            stb.append("         W1.SHDIV ");
            stb.append("     FROM ");
            stb.append("         ENTEXAM_APPLICANTBASE_DAT W1 ");
            stb.append("         LEFT JOIN ENTEXAM_RECEPT_DAT W2 ON ");
            stb.append("             W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append("             W2.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append("             W2.TESTDIV = W1.TESTDIV AND ");
            stb.append("             W2.EXAMNO = W1.EXAMNO ");
            stb.append("         LEFT JOIN NAME_MST W3 ON ");
            stb.append("             W3.NAMECD1 = 'L013' AND ");
            stb.append("             W3.NAMECD2 = W2.JUDGEDIV ");
            stb.append("     WHERE ");
            stb.append("         W1.ENTEXAMYEAR='"+year+"' AND ");
            stb.append("         W1.APPLICANTDIV='"+applicantDiv+"' AND ");
            stb.append("         W1.TESTDIV = '"+testDiv+"' ");
            if ("1".equals(printType)) { //合格者全員
                stb.append("     AND W1.JUDGEMENT = '1' ");
            }
            if ("2".equals(printType)) { //受験者全員
                stb.append("     AND W2.EXAMNO IS NOT NULL ");
            }
            if ("3".equals(printType)) { //志願者全員
            }
            stb.append("     ) T1 ");
            stb.append("       LEFT JOIN NAME_MST T5 ON ");
            stb.append("                   T5.NAMECD1='Z002' AND ");
            stb.append("                   T5.NAMECD2=T1.SEX ");
            stb.append("       LEFT JOIN FINSCHOOL_MST T6 ON ");
            stb.append("                   T6.FINSCHOOLCD = T1.FS_CD ");
            stb.append("       LEFT JOIN NAME_MST T7 ON ");
            stb.append("                   T7.NAMECD1 = 'L001' AND ");
            stb.append("                   T7.NAMECD2 = T6.FINSCHOOL_DISTCD ");
            stb.append("       LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T9 ON ");
            stb.append("                   T9.ENTEXAMYEAR = T1.ENTEXAMYEAR AND ");
            stb.append("                   T9.EXAMNO = T1.EXAMNO ");
            stb.append("       LEFT JOIN PREF_MST T10 ON ");
            stb.append("                   T9.PREF_CD = T10.PREF_CD ");
            stb.append("       LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T12 ON ");
            stb.append("                   T12.ENTEXAMYEAR=T1.ENTEXAMYEAR AND ");
            stb.append("                   T12.EXAMNO=T1.RECOM_EXAMNO AND ");
            stb.append("                   T12.APPLICANTDIV = '1' AND ");
            stb.append("                   T12.TESTDIV <> T1.TESTDIV ");
            stb.append("       LEFT JOIN NAME_MST T13 ON ");
            stb.append("                   T13.NAMECD1 = 'L004' AND ");
            stb.append("                   T13.NAMECD2 = T12.TESTDIV ");
            stb.append(" ORDER BY ");
            stb.append("     T1.EXAMNO ");


        } catch( Exception e ){
            log.error("preStat1 error!"+e);
        }
        return stb.toString();

    }//preStat1()の括り



    /**総ページ数を取得**/
    private String preStatTotalPage(String year,String applicantDiv, String testDiv, String printType)
    {
        StringBuffer stb = new StringBuffer();
    //  パラメータ（なし）
        try {
            stb.append("SELECT ");
            stb.append("    SUM(T1.TEST_CNT) TOTAL_PAGE  ");
            stb.append("FROM ");
            stb.append("    (SELECT CASE WHEN MOD(COUNT(*),50) > 0 THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END TEST_CNT  ");
            stb.append("     FROM   ENTEXAM_APPLICANTBASE_DAT W1  ");
            stb.append("         LEFT JOIN ENTEXAM_RECEPT_DAT W2 ON ");
            stb.append("             W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append("             W2.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append("             W2.TESTDIV = W1.TESTDIV AND ");
            stb.append("             W2.EXAMNO = W1.EXAMNO ");
            stb.append("     WHERE  W1.ENTEXAMYEAR='"+year+"'  ");
            stb.append("            AND W1.APPLICANTDIV='"+applicantDiv+"' ");
            stb.append("            AND W1.TESTDIV='"+testDiv+"' ");
            if ("1".equals(printType)) { //合格者全員
                stb.append("        AND W1.JUDGEMENT = '1' ");
            }
            if ("2".equals(printType)) { //受験者全員
                stb.append("        AND W2.EXAMNO IS NOT NULL ");
            }
            if ("3".equals(printType)) { //志願者全員
            }
            stb.append("     ) T1  ");
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
}//クラスの括り
