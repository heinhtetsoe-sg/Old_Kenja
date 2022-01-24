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
 *                  ＜ＫＮＪＬ３５４Ｃ＞  割り印簿
 *
 *  2008/11/06 RTS 作成日
 **/

public class KNJL354C {

    private static final Log log = LogFactory.getLog(KNJL354C.class);
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
        String param[] = new String[6];

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
        
        //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                        //年度
            param[1] = request.getParameter("APPLICANTDIV");                //入試制度
            param[2] = request.getParameter("TESTDIV");                      //入試区分
            param[3] = request.getParameter("LOGIN_DATE");                  // ログイン日付
            param[4] = request.getParameter("PRINT_TYPE");                  // 対象出力条件
            param[5] = request.getParameter("SHDIV");                       //専併区分
        } catch( Exception ex ) {
            log.error("parameter error!");
        }

        // '全て'
        if ("9".equals(param[2])) {
            if("1".equals(param[1])) _testDivPrint = new String[]{"1","2","6"};
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

        //  print設定
        response.setContentType("application/pdf");

        //  svf設定
        int ret = svf.VrInit();                             //クラスの初期化
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定

        //  ＳＶＦ作成処理
        PreparedStatement psTestdiv = null;
        PreparedStatement ps1 = null;
        PreparedStatement psTotalPage = null;
        boolean nonedata = false;                               //該当データなしフラグ
        for(int i=0 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);

        String year=param[0];
        String applicantDiv = param[1];
        String printType = param[4];
        String shDiv = param[5];
        
        //SQL作成
        for(int i=0; i<_testDivPrint.length; i++) {
            String testDiv = _testDivPrint[i];
            try {
                String sql;
                sql = preStat1(year, applicantDiv, testDiv, printType, shDiv);
                log.debug("preStat1 sql="+sql);
                ps1 = db2.prepareStatement(sql);        //受験者一覧preparestatement

                sql = preStatTotalPage(year, applicantDiv, testDiv, printType, shDiv);
                log.debug("preStateTotalPage sql="+sql);
                psTotalPage = db2.prepareStatement(sql);        //総ページ数preparestatement
                
            } catch( Exception ex ) {
                log.error("DB2 prepareStatement set error!");
            }
            //SVF出力
            if (setSvfMain(db2, svf, param, ps1, psTotalPage, testDiv)) nonedata = true;  //帳票出力のメソッド
        }

    //  該当データ無し
        if( !nonedata ){
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndPage();
        }

    //  終了処理
        ret = svf.VrQuit();
        preStatClose(psTestdiv,ps1,psTotalPage);       //preparestatementを閉じる
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
                if ("2".equals(rs.getString("NAME1"))) seirekiFlg = true; //西暦
            }
            ps.close();
            rs.close();
        } catch (Exception e) {
            log.error("getSeirekiFlg Exception "+ e);
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

    /** 表紙をセット **/
    private void setCover(
            DB2UDB db2,
            Vrw32alp svf,
            String param[],
            String testDiv)
    {
        String title = "【割り印簿】";
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetForm("KNJLCVR001C.frm", 4);
        String year = _seirekiFlg ? param[0]+"年度":
            KNJ_EditDate.h_format_JP_N(param[0]+"-01-01")+"度";
        ret = svf.VrsOut("NENDO", year);
        ret = svf.VrsOut("APPLICANTDIV", getNameMst(db2, "L003", param[1]));// 画面から入試制度
        String shDivName = "";
        if (!isCollege() && "2".equals(param[1])) {
            shDivName = "（" + getNameMst(db2, "L006", param[5]) + "）";// 画面から専併区分
        }
        ret = svf.VrsOut("TESTDIV",      getNameMst(db2, "L004", testDiv) + shDivName);// 画面から入試区分
        ret = svf.VrsOut("SCHOOLNAME", getSchoolName(db2, param[0], param[1]));// 学校名
        ret = svf.VrsOut("TITLE", title);
        ret = svf.VrsOut("NOTE" ,"");
        ret = svf.VrEndRecord();//レコードを出力
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
        if (isWakayama()) {
            ret = svf.VrSetForm("KNJL354C_2.frm", 4); //判定欄有(和歌山用)
        } else {
            ret = svf.VrSetForm("KNJL354C.frm", 4);
        }

        String year = _seirekiFlg ? param[0]+"年度":
            KNJ_EditDate.h_format_JP_N(param[0]+"-01-01")+"度";
        ret = svf.VrsOut("NENDO", year);
        ret = svf.VrsOut("APPLICANTDIV", getNameMst(db2, "L003", param[1]));// 画面から入試制度
        String shDivName = "";
        if (!isCollege() && "2".equals(param[1])) {
            shDivName = "(" + getNameMst(db2, "L006", param[5]) + ")";// 画面から専併区分
        }
        ret = svf.VrsOut("TESTDIV", getNameMst(db2, "L004", testDiv) + shDivName);// 画面から入試区分
        ret = svf.VrsOut("SCHOOLNAME", getSchoolName(db2, param[0], param[1]));// 学校名
        log.debug("HEADER: APPLICANTDIV="+getNameMst(db2, "L003", param[1]));
        log.debug("HEADER: TESTDIV     ="+getNameMst(db2, "L004", testDiv));
        log.debug("HEADER: SHDIV       ="+shDivName);

        String fsItem = "";
        if ("1".equals(param[1])) {
            fsItem = "出身小学校";
        } else if ("2".equals(param[1])) {
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
            String param[],
            PreparedStatement ps1,
            PreparedStatement psTotalpage,
            String testDiv
    ) {
            boolean nonedata = false;

            try {
                if (setSvfout(db2, svf, param, ps1, psTotalpage, testDiv)) nonedata = true; //帳票出力のメソッド
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
        String[] param,
        PreparedStatement ps1,
        PreparedStatement psTotalpage,
        String testDiv
    ) {
        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            int reccnt_man      = 0;    //男レコード数カウント用
            int reccnt_woman    = 0;    //女レコード数カウント用
            int reccnt = 0;             //合計レコード数
            int gyo = 1;                //現在ページ数の判断用（行）
            ResultSet rs = ps1.executeQuery();

            while( rs.next() ){
                //レコードを出力
                if (nonedata == false) {
                    setCover(db2, svf, param, testDiv);
                    setHeader(db2, svf, param, testDiv);
                    setTotalPage(db2, svf, psTotalpage); //総ページ数メソッド
                    nonedata = true;
                }

                reccnt++;
                //レコード数カウント
                if (rs.getString("SEX") != null) {
                    if (rs.getString("SEX").equals("1")) reccnt_man++;
                    if (rs.getString("SEX").equals("2")) reccnt_woman++;
                }
                //明細
                ret = svf.VrsOut("NUMBER", String.valueOf(reccnt));       //連番
                ret = svf.VrsOut("EXAMNO", rs.getString("EXAMNO"));       //受験番号
                ret = svfVrsOutFormat(svf, "NAME"  ,10, rs.getString("NAME"), ""); //名前
                ret = svf.VrsOut("SEX", rs.getString("SEX_NAME"));     //性別
                ret = svf.VrsOut("PREF", rs.getString("PREF")); // 県別
                ret = svfVrsOutFormat(svf, "FINSCHOOL"  ,10, nvlT(rs.getString("FINSCHOOL_NAME")), ""); // 学校名
                ret = svf.VrsOut("JUDGE", rs.getString("JUDGEDIV_NAME")); // 判定
                ret = svf.VrEndRecord();
                //-------------------------------------------------------------
                //現在ページ数判断用
                gyo++;
                
            }
            //最終レコードを出力
            if (nonedata) {
                //最終ページに男女合計を出力
                ret = svf.VrsOut("NOTE" ,"男"+String.valueOf(reccnt_man)+"名,女"+String.valueOf(reccnt_woman)+"名,合計"+String.valueOf(reccnt)+"名");
                ret = svf.VrEndRecord();//レコードを出力
                setSvfInt(svf);         //ブランクセット
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setSvfout set error!"+ex);
        }
        return nonedata;
    }


    /**受験者/合格者一覧を取得**/ 
    private String preStat1(String year,String applicantDiv,String testDiv,String printType, String shDiv)
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" select distinct ");
            stb.append("     t1.EXAMNO, ");
            stb.append("     t1.NAME, ");
            stb.append("     t1.NAME_KANA, ");
            stb.append("     t1.SEX, ");
            stb.append("     t2.ABBV1 as SEX_NAME, ");
            stb.append("     VALUE(t3.FINSCHOOL_NAME_ABBV, t3.FINSCHOOL_NAME) AS FINSCHOOL_NAME, ");
            stb.append("     N1.NAME1 AS JUDGEDIV_NAME, ");
            stb.append("     t4.PREF ");
            stb.append(" from  ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT t1");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT t0 ON ");
            stb.append("        t0.ENTEXAMYEAR = t1.ENTEXAMYEAR AND ");
            stb.append("        t0.APPLICANTDIV = t1.APPLICANTDIV AND ");
            stb.append("        t0.TESTDIV = t1.TESTDIV AND ");
            stb.append("        t0.EXAMNO = t1.EXAMNO ");
            stb.append("     left join NAME_MST t2 on ");
            stb.append("         t2.NAMECD1 = 'Z002' and ");
            stb.append("         t2.NAMECD2 = t1.SEX ");
            stb.append("     left join FINSCHOOL_MST t3 on ");
            stb.append("         t1.FS_CD = t3.FINSCHOOLCD ");
            stb.append("     left join ZIPCD_MST t4 on ");
            stb.append("         t3.FINSCHOOL_ZIPCD = t4.NEW_ZIPCD ");
            stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L013' AND N1.NAMECD2 = t0.JUDGEDIV ");
            stb.append(" where ");
            stb.append("     t1.ENTEXAMYEAR='"+year+"' ");
            stb.append("     and t1.APPLICANTDIV='"+applicantDiv+"' ");
            stb.append("     and t1.TESTDIV='"+testDiv+"' ");
            if ("1".equals(printType)) {
                stb.append(" and t1.JUDGEMENT='1' ");
            }
            if ("2".equals(printType)) { //受験者全員
                stb.append(" and t0.EXAMNO IS NOT NULL ");
            }
            if ("3".equals(printType)) { //志願者全員
            }
            // 高校の場合、専併区分の条件追加
            if (!isCollege() && "2".equals(applicantDiv)) {
                stb.append("     and t1.SHDIV='"+shDiv+"' ");
            }
            stb.append(" order by ");
            stb.append("     t1.EXAMNO ");
            
        } catch( Exception e ){
            log.error("preStat1 error!"+e);
        }
        return stb.toString();

    }//preStat1()の括り



    /**総ページ数を取得**/
    private String preStatTotalPage(String year,String applicantDiv,String testDiv,String printType, String shDiv)
    {
        StringBuffer stb = new StringBuffer();
    //  パラメータ（なし）
        try {
            stb.append("SELECT ");
            stb.append("    SUM(T1.TEST_CNT) TOTAL_PAGE  ");
            stb.append("FROM ");
            stb.append("    (SELECT CASE WHEN MOD(COUNT(*),25) > 0 THEN COUNT(*)/25 + 1 ELSE COUNT(*)/25 END TEST_CNT  ");
            stb.append("     FROM ");
            stb.append("        ENTEXAM_APPLICANTBASE_DAT W1 ");
            stb.append("        LEFT JOIN ENTEXAM_RECEPT_DAT W0 ON ");
            stb.append("            W0.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append("            W0.APPLICANTDIV = W1.APPLICANTDIV AND ");
            stb.append("            W0.TESTDIV = W1.TESTDIV AND ");
            stb.append("            W0.EXAMNO = W1.EXAMNO ");
            stb.append("     WHERE  W1.ENTEXAMYEAR='"+year+"'  ");
            stb.append("            AND W1.APPLICANTDIV='"+applicantDiv+"' ");
            stb.append("            AND W1.TESTDIV= '"+testDiv+"' ");
            if ("1".equals(printType)) {
                stb.append("        AND W1.JUDGEMENT='1' ");
            }
            if ("2".equals(printType)) { //受験者全員
                stb.append("        AND W0.EXAMNO IS NOT NULL ");
            }
            if ("3".equals(printType)) { //志願者全員
            }
            // 高校の場合、専併区分の条件追加
            if (!isCollege() && "2".equals(applicantDiv)) {
                stb.append("        AND W1.SHDIV='"+shDiv+"' ");
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
    
    private int svfVrsOutFormat(Vrw32alp svf,String area_name, int len, String sval1, String sval2) {
        if (sval1 == null || "null".equals(sval1)) return 0;
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
