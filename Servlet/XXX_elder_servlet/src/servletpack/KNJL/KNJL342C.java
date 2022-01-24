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
 *                  ＜ＫＮＪＬ３４２Ｃ＞  入学予定者名簿
 *
 *  2008/11/05 RTS 作成日
 **/

public class KNJL342C {

    private static final Log log = LogFactory.getLog(KNJL342C.class);
    private boolean _seirekiFlg;
    private String[] testDivPrint;
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
            param[4] = request.getParameter("OUTPUT");                      // 追加合格者のみ
        } catch( Exception ex ) {
            log.error("parameter error!");
        }

        //  print設定
        response.setContentType("application/pdf");

        //  svf設定
        int ret = svf.VrInit();                             //クラスの初期化
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定

        //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }
        _seirekiFlg = getSeirekiFlg(db2);
        _z010SchoolCode = getSchoolCode(db2);

        // '全て'
        if ("9".equals(param[2])) {
            if("1".equals(param[1])) testDivPrint = new String[]{"0"};
            if("2".equals(param[1])) {
                if (isGojo()) {
                    testDivPrint = new String[]{"3","4","5","7"};
                } else {
                    testDivPrint = new String[]{"3","4","5"};
                }
            }
        } else if ("0".equals(param[2])) {
            if("1".equals(param[1])) testDivPrint = new String[]{"1","2","7"};
            if("2".equals(param[1])) testDivPrint = new String[]{param[2]};
        } else {
            if("1".equals(param[1])) testDivPrint = new String[]{param[2]};
            if("2".equals(param[1])) testDivPrint = new String[]{param[2]};
        }

        //  ＳＶＦ作成処理
        PreparedStatement ps1 = null;
        boolean nonedata = false;                               //該当データなしフラグ
        for(int i=0 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);

        String year=param[0];
        String applicantDiv = param[1];
        String output = param[4];

        //SQL作成
        try {
                for (int i=0; i<testDivPrint.length; i++) {
                    String testDiv = testDivPrint[i];

                    String sql;
                    sql = preStat1(year, applicantDiv, testDiv, output);
                    log.debug("preStat1 sql="+sql);
                    ps1 = db2.prepareStatement(sql);        //受験者一覧preparestatement

                    //SVF出力
                    if (setSvfMain(db2, svf, param, ps1, testDiv)) nonedata = true;  //帳票出力のメソッド
                }
        } catch( Exception ex ) {
            log.error("DB2 prepareStatement set error!",ex);
        }

        //  該当データ無し
        if( !nonedata ){
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndPage();
        }

        //  終了処理
        ret = svf.VrQuit();
        preStatClose(ps1);       //preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる
    }//doGetの括り

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
        } catch(SQLException ex) {
            log.debug(ex);
        }

        return name;
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

    /** 事前処理 **/
    private void setHeader(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        String testDiv
    ) {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetForm("KNJL342C.frm", 4);
        String year = _seirekiFlg ? param[0]+"年度":
            KNJ_EditDate.h_format_JP_N(param[0]+"-01-01")+"度";
        ret = svf.VrsOut("NENDO", year);
        ret = svf.VrsOut("APPLICANTDIV", getNameMst(db2, "L003", param[1]));// 画面から入試制度
        if (!"1".equals(param[1])) {
            // 入試制度が中学のときは入試区分名称を表示しない。
            ret = svf.VrsOut("TESTDIV", getNameMst(db2, "L004", testDiv));// 画面から入試区分
        }
        ret = svf.VrsOut("SCHOOLNAME", getSchoolName(db2, param[0], param[1]));// 学校名
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
            String param[],
            PreparedStatement ps1,
            String testDiv
        ) {
            boolean nonedata = false;

            try {
                    if (setSvfout(db2, svf, param, ps1, testDiv)) nonedata = true; //帳票出力のメソッド
                    db2.commit();
            } catch( Exception ex ) {
                log.error("setSvfMain set error!"+ex);
            }
            return nonedata;
        }

    /**帳票出力（受験者一覧をセット）**/
    private boolean setSvfout(
        DB2UDB db2,
        Vrw32alp svf,
        String[] param,
        PreparedStatement ps1,
        String testDiv
    ) {
        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            int reccnt_man      = 0;    //男レコード数カウント用
            int reccnt_woman    = 0;    //女レコード数カウント用
            int reccnt = 0;             //合計レコード数
            int pagecnt = 1;            //現在ページ数
            int gyo = 1;                //現在ページ数の判断用（行）
            ResultSet rs = ps1.executeQuery();

            while( rs.next() ){
                if (nonedata == false) {
                    setHeader(db2, svf, param, testDiv);
                }
                if (pagecnt!=1) ret = svf.VrEndRecord(); //レコードを出力
                //25行超えたとき、ページ数カウント
                if (gyo % 25 == 1) {
                    //ヘッダ
                    ret = svf.VrsOut("PAGE", String.valueOf(pagecnt));      //現在ページ数
                    gyo = 1;
                    pagecnt++;
                }
                //明細

                ret = svf.VrsOut("NUMBER", rs.getString("SEQNO"));       //連番
                ret = svf.VrsOut("EXAMNO", rs.getString("EXAMNO"));       //受験番号
                ret = svfVrsOutFormat(svf, "NAME", 10, rs.getString("NAME"), ""); // 名前
                ret = svf.VrsOut("SEX", rs.getString("SEX_NAME"));     //性別
                //ret = svfVrsOutFormat(svf, "REMARK", 10, rs.getString("REMARK1"), rs.getString("REMARK2")); // 備考は印字しない
                //レコード数カウント
                reccnt++;
                if (rs.getString("SEX") != null) {
                    if (rs.getString("SEX").equals("1")) reccnt_man++;
                    if (rs.getString("SEX").equals("2")) reccnt_woman++;
                }
                //log.debug(rs.getString("SEQNO")+","+rs.getString("EXAMNO"));

                //現在ページ数判断用
                gyo++;

                nonedata = true;
            }
            rs.close();
            db2.commit();
            //最終レコードを出力
            if (nonedata) {
                //最終ページに男女合計を出力
                ret = svf.VrsOut("NOTE" ,"男"+String.valueOf(reccnt_man)+"名,女"+String.valueOf(reccnt_woman)+"名,合計"+String.valueOf(reccnt)+"名");
                ret = svf.VrEndRecord();//レコードを出力
                setSvfInt(svf);         //ブランクセット
            }
        } catch( Exception ex ) {
            log.error("setSvfout set error! ",ex);
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


    /**入学予定者一覧を取得**/
    private String preStat1(String year,String applicantDiv, String testDiv, String output)
    {
        StringBuffer stb = new StringBuffer();
    //  パラメータ（なし）
        try {
            stb.append(" SELECT ");
            stb.append("     row_number() over() AS SEQNO, ");
            stb.append("     W1.EXAMNO, ");
            stb.append("     W1.NAME, ");
            stb.append("     W1.NAME_KANA, ");
            stb.append("     W1.SEX, ");
            stb.append("     T5.ABBV1 AS SEX_NAME, ");
            stb.append("     W1.REMARK1, ");
            stb.append("     W1.REMARK2 ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT W1 ");
            stb.append("     LEFT JOIN NAME_MST T5 ON ");
            stb.append("                     T5.NAMECD1='Z002' AND ");
            stb.append("                     T5.NAMECD2=W1.SEX ");
            stb.append(" WHERE ");
            stb.append("     W1.ENTEXAMYEAR='"+year+"' ");
            stb.append("     AND W1.APPLICANTDIV='"+applicantDiv+"' ");
            // 特併合格者は、特進文系コースの合格者と一緒に出力する。
            if ("7".equals(testDiv)) {
                stb.append("     AND (W1.TESTDIV='"+testDiv+"' AND W1.JUDGEMENT='1' OR W1.TESTDIV='3' AND W1.JUDGEMENT='7') ");
            } else if ("0".equals(testDiv)) {
                stb.append("     AND (W1.TESTDIV='1' OR W1.TESTDIV='2' OR W1.TESTDIV='6' OR W1.TESTDIV='7') ");
                stb.append("     AND W1.JUDGEMENT = '1' ");
            } else {
                stb.append("     AND W1.TESTDIV='"+testDiv+"' ");
                stb.append("     AND W1.JUDGEMENT = '1' ");
            }
            if ("1".equals(output)) {
                stb.append("     AND W1.SPECIAL_MEASURES IS NOT NULL "); // 追加合格者のみを対象
            }
            stb.append("     AND W1.PROCEDUREDIV = '1' ");
            stb.append("     AND W1.ENTDIV= '1' ");
            stb.append(" ORDER BY ");
            stb.append("     W1.EXAMNO ");

        } catch( Exception e ){
            log.error("preStat1 error!"+e);
        }
        return stb.toString();

    }//preStat1()の括り

    /**PrepareStatement close**/
    private void preStatClose(
        PreparedStatement ps1
    ) {
        try {
            ps1.close();
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
