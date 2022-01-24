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
 *                  ＜ＫＮＪＬ３５９Ｃ＞  入学者一覧（生徒所在地の教育委員会用）
 *
 *  2008/11/06 RTS 作成日
 **/

public class KNJL359C {

    private static final Log log = LogFactory.getLog(KNJL359C.class);
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


        //  ＳＶＦ作成処理
        PreparedStatement psTestdiv = null;
        PreparedStatement ps1 = null;
        PreparedStatement psTotalPage = null;
        boolean nonedata = false;                               //該当データなしフラグ
        
        //SQL作成
    
        try {
            for(int j=0; j<_param._edBoardCd.length; j++) {
                String sql = preStatEdboard(_param._edBoardCd[j]);
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
//                    String edboardCd = rs.getString("EDBOARDCD");
//                    String edboardName = rs.getString("EDBOARDNAME");
                    String prefName = rs.getString("PREF_NAME");
                    String cityName = rs.getString("CITY_NAME");
                    if (null == prefName || "".equals(prefName)) continue;
                    if (null == cityName || "".equals(cityName)) continue;

                    String zipCnt = getZipCnt(db2, prefName, cityName);
                    String addrCnt = getAddrCnt(db2, prefName, cityName, "CNT");
                    String totalPage = getAddrCnt(db2, prefName, cityName, "TOTAL_PAGE");
                    if (null == zipCnt || "0".equals(zipCnt)) continue;
                    if (null == addrCnt || "0".equals(addrCnt)) continue;

                    String sql1 = preStatAddrList(prefName, cityName);
//                    log.debug("sql1 = " + sql1);
                    ps1 = db2.prepareStatement(sql1);

                    //SVF出力
                    if (setSvfMain(db2, svf, ps1, totalPage, cityName)) nonedata = true;
                }
                rs.close();
            }
        } catch( Exception ex ) {
            log.error("DB2 prepareStatement set error!"+ex);
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
            String cityName)
    {
        String title = "【入学者一覧表】（" + cityName + "在住者）";
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetForm("KNJLCVR001C.frm", 4);
        ret = svf.VrsOut("NENDO", _param.getNendo());
        ret = svf.VrsOut("APPLICANTDIV", _param.getNameMst(db2, "L003", _param._applicantDiv));// 画面から入試制度
        ret = svf.VrsOut("TESTDIV", "前期・後期");
        ret = svf.VrsOut("SCHOOLNAME", _param.getSchoolName(db2));// 学校名
        ret = svf.VrsOut("TITLE", title);
        ret = svf.VrsOut("NOTE" ,"");
        ret = svf.VrEndRecord();//レコードを出力
    }
    
    /** 事前処理 **/
    private void setHeader(
        DB2UDB db2,
        Vrw32alp svf,
        String cityName
    ) {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetForm("KNJL359C.frm", 4);
        ret = svf.VrsOut("NENDO", _param.getNendo());
        ret = svf.VrsOut("APPLICANTDIV", _param.getNameMst(db2, "L003", _param._applicantDiv));// 画面から入試制度
        ret = svf.VrsOut("TESTDIV", "前期・後期");
        ret = svf.VrsOut("SCHOOLNAME", _param.getSchoolName(db2));// 学校名
        ret = svf.VrsOut("DISTRICT", cityName + "在住者");
        ret = svf.VrsOut("DATE", _param.getLoginDateString());
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
            String cityName
    ) {
            boolean nonedata = false;

            try {
                if (setSvfout(db2, svf, ps1, totalPage, cityName)) nonedata = true; //帳票出力のメソッド
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
        String cityName
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
//            String oldDistirctCd = "-";
//            int edboardCdNo = 1;

            while( rs.next() ){
                if (nonedata == false) {
                    setCover(db2, svf, cityName);
                    setHeader(db2, svf, cityName);
                    svf.VrsOut("TOTAL_PAGE", totalPage);                //総ページ数メソッド
                    nonedata = true;
                }
                
                
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
                ret = svf.VrEndRecord(); //レコードを出力
                //現在ページ数判断用
                gyo++;
                
                //レコード数カウント
                reccnt++;
                if (rs.getString("SEX") != null) {
                    if ("1".equals(rs.getString("SEX"))) reccnt_man++;
                    if ("2".equals(rs.getString("SEX"))) reccnt_woman++;
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

    private String preStatEdboard(String edboardCd) {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" SELECT ");
            stb.append("     E1.EDBOARDCD, ");
            stb.append("     E1.EDBOARDNAME, ");
            stb.append("     P1.PREF_NAME, ");
            stb.append("     REPLACE(E1.EDBOARDNAME,'教育委員会','') AS CITY_NAME ");
            stb.append(" FROM ");
            stb.append("     EDBOARD_MST E1 ");
            stb.append("     LEFT JOIN PREF_MST P1 ON P1.PREF_CD = SUBSTR(E1.EDBOARDCD,2,2) ");
            stb.append(" WHERE ");
            stb.append("     E1.EDBOARDCD = '" + edboardCd + "' ");
        } catch( Exception e ){
            log.error("preStatEdboard error!");
        }
        return stb.toString();
    }

    private String getZipCnt(DB2UDB db2, String prefName, String cityName) {
        String cnt = null;
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     COUNT(*) AS CNT ");
            stb.append(" FROM ");
            stb.append("     ZIPCD_MST Z1 ");
            stb.append(" WHERE ");
            stb.append("     Z1.PREF = '" + prefName + "' ");
            stb.append("     AND Z1.CITY LIKE '%" + cityName + "%' ");

            PreparedStatement ps = db2.prepareStatement(stb.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                cnt = rs.getString("CNT");
            }
            ps.close();
            rs.close();
        } catch (Exception e) {
            log.error("getSchoolCode Exception", e);
        }
        return cnt;
    }

    private String getAddrCnt(DB2UDB db2, String prefName, String cityName, String div) {
        String cnt = null;
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            if ("TOTAL_PAGE".equals(div)) {
                stb.append("     (CASE WHEN MOD(COUNT(*),50) > 0 THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END) AS TOTAL_PAGE ");
            } else {
                stb.append("     COUNT(*) AS CNT ");
            }
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT B1 ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT A1 ON A1.ENTEXAMYEAR = B1.ENTEXAMYEAR AND A1.EXAMNO = B1.EXAMNO ");
            stb.append(" WHERE ");
            stb.append("     B1.ENTEXAMYEAR = '" + _param._year + "' ");
            stb.append("     AND B1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND B1.JUDGEMENT = '1' ");
            stb.append("     AND B1.PROCEDUREDIV = '1' ");
            stb.append("     AND B1.ENTDIV = '1' ");
            stb.append("     AND REPLACE(A1.ADDRESS1,'" + prefName + "','') LIKE '%" + cityName + "%' ");

            PreparedStatement ps = db2.prepareStatement(stb.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if ("TOTAL_PAGE".equals(div)) {
                    cnt = rs.getString("TOTAL_PAGE");
                } else {
                    cnt = rs.getString("CNT");
                }
            }
            ps.close();
            rs.close();
        } catch( Exception e ){
            log.error("getAddrCnt Exception", e);
        }
        return cnt;
    }

    private String preStatAddrList(String prefName, String cityName) {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" SELECT ");
            stb.append("     row_number() over() AS SEQNO, ");
            stb.append("     B1.EXAMNO, ");
            stb.append("     B1.NAME, ");
            stb.append("     B1.SEX, ");
            stb.append("     N1.ABBV1 AS SEX_NAME, ");
            stb.append("     B1.FS_CD, ");
            stb.append("     F1.FINSCHOOL_NAME, ");
            stb.append("     A1.GNAME, ");
            stb.append("     A1.ZIPCD, ");
            stb.append("     P1.PREF_NAME AS PREF_NAME, ");
            stb.append("     A1.ADDRESS1, ");
            stb.append("     A1.ADDRESS2, ");
            stb.append("     A1.TELNO ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT B1 ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT A1 ON A1.ENTEXAMYEAR = B1.ENTEXAMYEAR AND A1.EXAMNO = B1.EXAMNO ");
            stb.append("     LEFT JOIN PREF_MST P1 ON P1.PREF_CD = A1.PREF_CD ");
            stb.append("     LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = B1.FS_CD ");
            stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = B1.SEX ");
            stb.append(" WHERE ");
            stb.append("     B1.ENTEXAMYEAR = '" + _param._year + "' ");
            stb.append("     AND B1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND B1.JUDGEMENT = '1' ");
            stb.append("     AND B1.PROCEDUREDIV = '1' ");
            stb.append("     AND B1.ENTDIV = '1' ");
            stb.append("     AND REPLACE(A1.ADDRESS1,'" + prefName + "','') LIKE '%" + cityName + "%' ");
            stb.append(" ORDER BY ");
            stb.append("     B1.EXAMNO ");
        } catch( Exception e ){
            log.error("preStatAddrList error!");
        }
        return stb.toString();
    }

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
        final String _loginDate;
        final String[] _edBoardCd;

        private boolean _seirekiFlg;
        private final String _z010SchoolCode;

        Param(DB2UDB db2, HttpServletRequest request) {
            _year = request.getParameter("YEAR");                        //年度
            _applicantDiv = request.getParameter("APPLICANTDIV");                //入試制度
            _loginDate = request.getParameter("LOGIN_DATE");                  // ログイン日付
            _edBoardCd = request.getParameterValues("CATEGORY_SELECTED");
            _z010SchoolCode = getSchoolCode(db2);
            // 西暦使用フラグ
            _seirekiFlg = getSeirekiFlg(db2);
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
