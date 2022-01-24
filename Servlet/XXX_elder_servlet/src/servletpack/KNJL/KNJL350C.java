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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３５０Ｃ＞  入学者一覧
 *
 *  2008/11/06 RTS 作成日
 **/

public class KNJL350C {

    private static final Log log = LogFactory.getLog(KNJL350C.class);
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
        
        //  print設定
        response.setContentType("application/pdf");

        //  svf設定
        int ret = svf.VrInit();                             //クラスの初期化
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetSpoolFileStream(response.getOutputStream());  //PDFファイル名の設定
        
        //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }
        _param = new Param(db2, request);

        // '全て'
        if ("9".equals(_param._testDiv)) {
            if("1".equals(_param._applicantDiv)) _testDivPrint = new String[]{"1","2"};
            if("2".equals(_param._applicantDiv)) {
                if (_param.isGojo()) {
                    _testDivPrint = new String[]{"3","4","5","7"}; 
                } else {
                    _testDivPrint = new String[]{"3","4","5"}; 
                }
            }
        } else {
            _testDivPrint = new String[]{_param._testDiv};
        }

        //  ＳＶＦ作成処理
        PreparedStatement psTestdiv = null;
        PreparedStatement ps1 = null;
        PreparedStatement psTotalPage = null;
        boolean nonedata = false;                               //該当データなしフラグ
        
        //SQL作成
        for(int i=0; i<_testDivPrint.length; i++) {
            String testDiv = _testDivPrint[i];
            
            try {
                for(int j=0; j<_param._edBoardCd.length; j++) {
                    String sql;
                    sql = preStatDistrictCD(testDiv, _param._edBoardCd[j]);
                    log.debug("preStatDistrictCd sql="+sql);
                    PreparedStatement ps = db2.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery();
                    
                    while (true) {
                        boolean res = rs.next();
                        if (!res) break;
                        String districtCd = rs.getString("DISTRICTCD");
                        String districtName = rs.getString("DISTRICTNAME");
                        String totalPage = rs.getString("TOTAL_PAGE");
                        log.debug("districtCd="+districtCd);
                        if (null == districtCd) continue; 
                        sql = preStat1(testDiv, districtCd);
                        log.debug("preStat1 sql="+sql);
                        ps1 = db2.prepareStatement(sql);        //受験者一覧preparestatement

                        //SVF出力
                        if (setSvfMain(db2, svf, ps1, totalPage, testDiv, districtName)) nonedata = true;  //帳票出力のメソッド
                    }
                    rs.close();
                }
            } catch( Exception ex ) {
                log.error("DB2 prepareStatement set error!"+ex);
            }
        }
    

    //  該当データ無し
        if( !nonedata ){
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndPage();
        }

    //  終了処理
        svf.VrQuit();
        preStatClose(psTestdiv,ps1,psTotalPage);       //preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる
    }//doGetの括り
    
    /** 表紙をセット **/
    private void setCover(
            DB2UDB db2,
            Vrw32alp svf,
            String testDiv,
            String districtName)
    {
        String title = "【入学者一覧表】（" + districtName + "在住者）";
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetForm("KNJLCVR001C.frm", 4);
        ret = svf.VrsOut("NENDO", _param.getNendo());
        ret = svf.VrsOut("APPLICANTDIV", _param.getNameMst(db2, "L003", _param._applicantDiv));// 画面から入試制度
        ret = svf.VrsOut("TESTDIV",      _param.getNameMst(db2, "L004", testDiv));// 画面から入試区分
        ret = svf.VrsOut("SCHOOLNAME", _param.getSchoolName(db2));// 学校名
        ret = svf.VrsOut("TITLE", title);
        ret = svf.VrsOut("NOTE" ,"");
        ret = svf.VrEndRecord();//レコードを出力
    }
    
    /** 事前処理 **/
    private void setHeader(
        DB2UDB db2,
        Vrw32alp svf,
        String testDiv,
        String districtName
    ) {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetForm("KNJL350C.frm", 4);
        ret = svf.VrsOut("NENDO", _param.getNendo());
        ret = svf.VrsOut("APPLICANTDIV", _param.getNameMst(db2, "L003", _param._applicantDiv));// 画面から入試制度
        if (!"9".equals(testDiv)) {
            ret = svf.VrsOut("TESTDIV", _param.getNameMst(db2, "L004", testDiv));// 画面から入試区分
        }
        ret = svf.VrsOut("SCHOOLNAME", _param.getSchoolName(db2));// 学校名
        log.debug("HEADER: APPLICANTDIV="+_param.getNameMst(db2, "L003", _param._applicantDiv));
        log.debug("HEADER: TESTDIV     ="+_param.getNameMst(db2, "L004", testDiv));
        ret = svf.VrsOut("DISTRICT", districtName);
        ret = svf.VrsOut( "DATE", _param.getLoginDateString());

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
            String totalPage,
            String testDiv,
            String districtName
    ) {
            boolean nonedata = false;

            try {
                if (setSvfout(db2, svf, ps1, totalPage, testDiv, districtName)) nonedata = true; //帳票出力のメソッド
                db2.commit();
            } catch( Exception ex) {
                log.error("setSvfMain set error!"+ex);
            }
            return nonedata;
        }

    /**帳票出力（受験者一覧をセット）**/
    private boolean setSvfout(
        DB2UDB db2,
        Vrw32alp svf,
        PreparedStatement ps1,
        String totalPage,
        String testDiv,
        String districtName
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
            String oldDistirctCd = "-";
            int districtCdNo = 1;

            while( rs.next() ){
                if (nonedata == false) {
                    setCover(db2, svf, testDiv, districtName);
                    setHeader(db2, svf, testDiv, districtName);
                    svf.VrsOut("TOTAL_PAGE", totalPage);                //総ページ数メソッド
                    nonedata = true;
                }
                
                // 地区コードが変わったら改ページ
                if(rs.getString("DISTRICTCD") != null && !oldDistirctCd.equals(rs.getString("DISTRICTCD"))) {
                    if (districtCdNo != 1) {
                        for (int g=gyo; g<=50; g++)
                            ret = svf.VrEndRecord();
                        gyo = 1;
                    }
                    log.debug(" districtCdNo="+districtCdNo+" ,"+rs.getString("DISTRICTCD"));
                    districtCdNo ++;
                }
                oldDistirctCd = rs.getString("DISTRICTCD");
                
                //５０行超えたとき、ページ数カウント
                if (gyo % 50==1) {
                    //ヘッダ
                    ret = svf.VrsOut("PAGE", String.valueOf(pagecnt));      //現在ページ数
                    pagecnt++;
                }

                //明細
                ret = svf.VrsOut("NUMBER", rs.getString("SEQNO"));       //連番
                ret = svf.VrsOut("EXAMNO", rs.getString("EXAMNO"));       //受験番号
                ret = svfVrsOutFormat(svf, "NAME", 10, rs.getString("NAME"), ""); //名前
                ret = svf.VrsOut("SEX", rs.getString("SEX_NAME"));     //性別
                ret = svfVrsOutFormat(svf, "FINSCHOOL", 13, nvlT(rs.getString("FINSCHOOL_NAME")), ""); // 学校名
                ret = svfVrsOutFormat(svf, "GUARD_NAME", 10, rs.getString("GNAME"), ""); // 保護者名
                ret = svf.VrsOut("ZIPCODE", rs.getString("ZIPCD")); // 保護者郵便番号
                ret = svf.VrsOut("PREF", rs.getString("PREF_NAME")); // 保護者県別
                ret = svfVrsOutFormat(svf, "ADDRESS", 25, rs.getString("ADDRESS1"), rs.getString("ADDRESS2")); // 住所
                ret = svf.VrsOut("TELNO", rs.getString("TELNO")); // 電話番号
                StringBuffer remark = new StringBuffer();
                if (null != rs.getString("OTHER_TESTDIV_NAME") || "1".equals(rs.getString("SHIFT_DESIRE_FLG"))) {
                    String otherTestdivName = null != rs.getString("OTHER_TESTDIV_NAME") ? rs.getString("OTHER_TESTDIV_NAME") + " " : "";
                    String kibou = _param.isGojo() ? "カレッジ併願" : "移行希望";
                    String shiftDesire = ("1".equals(rs.getString("SHIFT_DESIRE_FLG"))) ? kibou + " " : "";
                    String recomExamno = null != rs.getString("RECOM_EXAMNO") ? rs.getString("RECOM_EXAMNO") + " " : ""; 
                    remark.append(otherTestdivName);
                    remark.append(recomExamno);
                    remark.append(shiftDesire);
                }
                remark.append(null != rs.getString("REMARK1") ? rs.getString("REMARK1") : "");
                ret = svfVrsOutFormat(svf, "REMARK"  ,17, remark.toString(), rs.getString("REMARK2"));// 備考
                ret = svf.VrEndRecord(); //レコードを出力
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


    /**入学者一覧を取得**/ 
    private String preStat1(String testDiv, String districtCd)
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" SELECT ");
            stb.append("     row_number() over() AS SEQNO, ");
            stb.append("     T6.EDBOARDCD, ");
            stb.append("     T6.DISTRICTCD, ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     T1.NAME, ");
            stb.append("     T1.NAME_KANA, ");
            stb.append("     T1.SEX, ");
            stb.append("     T5.ABBV1 AS SEX_NAME, ");
            stb.append("     T1.FS_NAME, ");
            stb.append("     T7.NAME1 AS RITU_NAME, ");
            stb.append("     T6.FINSCHOOLCD, ");
            stb.append("     T6.FINSCHOOL_DISTCD, ");
            stb.append("     T6.FINSCHOOL_NAME, ");
            stb.append("     T9.GNAME, ");
            stb.append("     T9.ZIPCD, ");
            stb.append("     T11.PREF_NAME AS PREF_NAME, ");
            stb.append("     T9.ADDRESS1, ");
            stb.append("     T9.ADDRESS2, ");
            stb.append("     T9.TELNO, ");
            stb.append("     T1.REMARK1, ");
            stb.append("     T1.REMARK2, ");
            stb.append("     T1.RECOM_EXAMNO, ");
            stb.append("     T13.ABBV1 AS OTHER_TESTDIV_NAME, ");
            stb.append("     T1.SHIFT_DESIRE_FLG ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
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
            stb.append("     LEFT JOIN PREF_MST T11 ON ");
            stb.append("                 T11.PREF_CD = T9.PREF_CD ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T12 ON ");
            stb.append("                 T12.ENTEXAMYEAR=T1.ENTEXAMYEAR AND ");
            stb.append("                 T12.EXAMNO=T1.RECOM_EXAMNO AND ");
            stb.append("                 T12.APPLICANTDIV = '1' AND ");
            stb.append("                 T12.TESTDIV <> T1.TESTDIV ");
            stb.append("     LEFT JOIN NAME_MST T13 ON ");
            stb.append("                 T13.NAMECD1 = 'L004' AND ");
            stb.append("                 T13.NAMECD2 = T12.TESTDIV ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR='"+_param._year+"' AND ");
            stb.append("     T1.APPLICANTDIV='"+_param._applicantDiv+"' AND ");
            if (!"9".equals(testDiv)) {
                stb.append("     T1.TESTDIV='"+testDiv+"' AND ");
            }
            stb.append("     T1.PROCEDUREDIV='1' AND ");
            stb.append("     T1.ENTDIV='1' AND ");
            stb.append("     T1.JUDGEMENT in ('1','7') AND ");
            stb.append("     T6.EDBOARDCD in "+SQLUtils.whereIn(true, _param._edBoardCd)+" AND ");
            stb.append("     T6.DISTRICTCD = '"+districtCd+"' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.EXAMNO ");

        } catch( Exception e ){
            log.error("preStat1 error!");
        }
        return stb.toString();

    }//preStat1()の括り

    private String preStatDistrictCD(String testDiv, String edboardCd)
    {
        StringBuffer stb = new StringBuffer();
    //  パラメータ（なし）
        try {
            stb.append(" SELECT ");
            stb.append("     T6.DISTRICTCD, ");
            stb.append("     T7.NAME1 AS DISTRICTNAME, ");
            stb.append("     (CASE WHEN MOD(COUNT(*),50) > 0 THEN COUNT(*)/50 + 1");
            stb.append("      ELSE COUNT(*)/50 END) AS TOTAL_PAGE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT W1 ");
            stb.append("     LEFT JOIN FINSCHOOL_MST T6 ON ");
            stb.append("         T6.FINSCHOOLCD = W1.FS_CD ");
            stb.append("     LEFT JOIN NAME_MST T7 ON ");
            stb.append("         T7.NAMECD1 = 'Z003' AND ");
            stb.append("         T7.NAMECD2 = T6.DISTRICTCD ");
            stb.append("     WHERE ");
            stb.append("         W1.ENTEXAMYEAR='"+_param._year+"' AND ");
            stb.append("         W1.APPLICANTDIV='"+_param._applicantDiv+"' AND ");
            if (!"9".equals(testDiv)) {
                stb.append("         W1.TESTDIV='"+testDiv+"' AND ");
            }
            stb.append("         W1.PROCEDUREDIV='1' AND ");
            stb.append("         W1.ENTDIV='1' AND ");
            stb.append("         W1.JUDGEMENT in ('1','7') AND ");
            stb.append("         T6.EDBOARDCD = '"+edboardCd+"' ");
            stb.append(" GROUP BY ");
            stb.append("     T6.DISTRICTCD, ");
            stb.append("     T7.NAME1 ");
        } catch( Exception e ){
            log.error("preStatDistrictCD error!");
        }
        return stb.toString();

    }//preStat3()の括り

    /**PrepareStatement close**/
    private void preStatClose(
        PreparedStatement ps,
        PreparedStatement ps1,
        PreparedStatement ps2
    ) {
        try {
            if(ps!=null) ps.close();
            if(ps1!=null) ps1.close();
            if(ps2!=null) ps2.close();
        } catch( Exception e ){
            log.error("preStatClose error!"+e);
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
        final String[] _edBoardCd;

        private boolean _seirekiFlg;
        private final String _z010SchoolCode;

        Param(DB2UDB db2, HttpServletRequest request) {
            _year = request.getParameter("YEAR");                        //年度
            _applicantDiv = request.getParameter("APPLICANTDIV");                //入試制度
            _testDiv = request.getParameter("TESTDIV");                      //入試区分
            _loginDate = request.getParameter("LOGIN_DATE");                  // ログイン日付
            _edBoardCd = request.getParameterValues("CATEGORY_SELECTED");
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
            return "30290053001".equals(_z010SchoolCode);
        }
        
        boolean isWakayama() {
            return "30300049001".equals(_z010SchoolCode);
        }
    }
}//クラスの括り
