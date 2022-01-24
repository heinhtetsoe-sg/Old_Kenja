package servletpack.KNJL;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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
 *                  ＜ＫＮＪＬ３４３Ｃ＞  入学予定者の関係書類の送付
 *
 *  2008/11/13 RTS 作成日
 **/

public class KNJL343C {

    private static final Log log = LogFactory.getLog(KNJL343C.class);
    Param _param ;
    
    private int GYO_MAX = 15;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        log.debug("$Id: ed1a55c28cac9b4f29d9a80ecdbbc99e25568336 $");

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
        _param = new Param(db2, request);
        
        //  print設定
        response.setContentType("application/pdf");

        //  svf設定
        int ret = svf.VrInit();                             //クラスの初期化
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定

        //  ＳＶＦ作成処理
        PreparedStatement ps1 = null;
        boolean nonedata = false;                               //該当データなしフラグ

        //SQL作成
        try {
            String sql = preStat1();
            log.debug("preStat1 sql="+sql);
            ps1 = db2.prepareStatement(sql);        //受験者一覧preparestatement

            //SVF出力
            if (setSvfMain(db2, svf, ps1)) nonedata = true;  //帳票出力のメソッド
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
        preStatClose(ps1);       //preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる
    }//doGetの括り

    private void setSchoolStatus(DB2UDB db2, Vrw32alp svf) {
        String certifKindCd = null;
        if ("1".equals(_param._applicantDiv)) certifKindCd = "111";
        if ("2".equals(_param._applicantDiv)) certifKindCd = "112";
        if (certifKindCd == null) return ;
        
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK1 AS ADDRESS, REMARK2 AS ZIPCD ");
        sql.append(" FROM CERTIF_SCHOOL_DAT ");
        sql.append(" WHERE YEAR = '"+_param._year+"' AND CERTIF_KINDCD = '"+certifKindCd+"' ");
        try{
            PreparedStatement ps = db2.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                int ret = 0;
                if (false && 0 != ret) { ret = 0; }
                ret = svf.VrsOut("SCHOOLNAME", rs.getString("SCHOOL_NAME"));
                ret = svf.VrsOut("JOBNAME", rs.getString("JOB_NAME"));
                ret = svf.VrsOut("STAFFNAME", rs.getString("PRINCIPAL_NAME"));
                ret = svf.VrsOut("ADDRESS", rs.getString("ADDRESS"));
                ret = svf.VrsOut("ZIPCD", rs.getString("ZIPCD"));
            }
        } catch(SQLException ex) {
            log.debug(ex);
        }
    }

    /** 事前処理 **/
    private void setHeader(
        DB2UDB db2,
        Vrw32alp svf
    ) {
        String form = null;
        if ("1".equals(_param._applicantDiv)) {
            if (_param.isCollege()) {
                form = "KNJL343C_1C.frm";
                GYO_MAX = 14;
            } else if (_param.isGojo()) {
                form = "KNJL343C_1G.frm";
            } else {
                form = "KNJL343C_1.frm";
            }
        } else if("2".equals(_param._applicantDiv)) {
            if (_param.isCollege()) {
                form = "KNJL343C_2C.frm";
                GYO_MAX = 14;
            } else if (_param.isGojo()) {
                form = "KNJL343C_2G.frm";
            } else {
                form = "KNJL343C_2.frm";
            }
        }
        svf.VrSetForm(form, 4);
        setSchoolStatus(db2, svf);

        svf.VrsOut( "DATE", _param.getDateString(_param._printDate));

        //  ＳＶＦ属性変更--->改ページ
        svf.VrAttribute("FS_CD","FF=1");
    }



    /**
     *  svf print 印刷処理
     */

    private boolean setSvfMain(
            DB2UDB db2,
            Vrw32alp svf,
            PreparedStatement ps1
        ) {
            boolean nonedata = false;

            try {
                if (setSvfout(db2, svf, ps1)) nonedata = true; //帳票出力のメソッド
                db2.commit();
            } catch( Exception ex ) {
                log.error("setSvfMain set error!"+ex);
                ex.printStackTrace();
            }
            return nonedata;
        }


    /**帳票出力**/
    private boolean setSvfout(
        DB2UDB db2,
        Vrw32alp svf,
        PreparedStatement ps1
    ) {
        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ResultSet rs = null;
        final List fsList = new ArrayList();
        try {
            rs = ps1.executeQuery();

            while( rs.next() ){
                final String fsCd = rs.getString("FS_CD");
                FS fs = getFs(fsCd, fsList);
                if (null == fs) {
                    fs = new FS(fsCd);
                    fs._finschoolZipcd = rs.getString("FINSCHOOL_ZIPCD");
                    fs._finschoolAddr1 = rs.getString("FINSCHOOL_ADDR1");
                    fs._finschoolAddr2 = rs.getString("FINSCHOOL_ADDR2");
                    fs._finschoolDistname = rs.getString("FINSCHOOL_DISTNAME");
                    fs._finschoolName = rs.getString("FINSCHOOL_NAME");
                    fsList.add(fs);
                }

                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String sexname = rs.getString("SEX_NAME");
                final Examinee e = new Examinee(examno, name, sexname);
                fs._examineeList.add(e);

                nonedata = true;
            }
        } catch( Exception ex ) {
            log.error("setSvfout set error! ="+ex);
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        
        try {
            for (final Iterator fsit = fsList.iterator(); fsit.hasNext();) {
                final FS fs = (FS) fsit.next();
                setHeader(db2, svf);
                
                int gyo = 0;                //現在ページ数の判断用（行）
                for (final Iterator eit = fs._examineeList.iterator(); eit.hasNext();) {
                    
                    ret = svf.VrsOut("F_ZIPCD", nvlT(fs._finschoolZipcd)); //出身学校郵便番号
                    String finSchoolAddress = nvlT(fs._finschoolAddr1) + ((fs._finschoolAddr2==null) ? "" : " " + fs._finschoolAddr2); 
                    ret = svf.VrsOut("F_ADDRESS", finSchoolAddress); //出身学校住所
                    ret = svf.VrsOut("DISTRICT", nvlT(fs._finschoolDistname));  //出身学校地区名
                    ret = svf.VrsOut("F_SCHOOLNAME", nvlT(fs._finschoolName)); //出身学校名

                    final Examinee e = (Examinee) eit.next();
                    //明細
                    gyo++;
                    ret = svf.VrsOut("NUMBER", String.valueOf(gyo));
                    ret = svf.VrsOut("EXAMNO", e._examno);       //受験番号
                    
                    if ("1".equals(_param._applicantDiv) && _param.isGojo() || _param.isCollege()) {
                        ret = svf.VrsOut("NAME1", e._name);
                    } else if (e._name.length() > 20) {
                        ret = svf.VrsOut("NAME3", e._name);
                    } else {
                        ret = svfVrsOutFormat(svf, "NAME", 10, e._name, null); // 名前
                    }
                    ret = svf.VrsOut("SEX", e._sexname);     //性別
                    ret = svf.VrEndRecord();
                    nonedata = true;                        

                    if (("1".equals(_param._applicantDiv) && _param.isGojo() || _param.isCollege()) && 0 != gyo && 0 == gyo % GYO_MAX) {
                        setHeader(db2, svf);
                    }
                }
                
                if ("1".equals(_param._applicantDiv) && _param.isGojo() || _param.isCollege()) {
                    svf.VrsOut("TOTAL", String.valueOf(gyo));
                    svf.VrEndRecord();
                }
            }

        } catch( Exception ex ) {
            log.error("setSvfout set error! ="+ex);
        }

        return nonedata;
    }
    
    private FS getFs(final String fsCd, final List fsList) {
        for (final Iterator it = fsList.iterator(); it.hasNext();) {
            final FS fs = (FS) it.next();
            if (null != fs._fsCd && fs._fsCd.equals(fsCd)) {
                return fs;
            }
        }
        return null;
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

    /**入学予定者を取得**/ 
    private String preStat1()
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" select ");
            stb.append("     t1.FS_CD, ");
            stb.append("     t2.FINSCHOOL_ZIPCD, ");
            stb.append("     t2.FINSCHOOL_ADDR1, ");
            stb.append("     t2.FINSCHOOL_ADDR2, ");
            stb.append("     t2.FINSCHOOL_NAME, ");
            stb.append("     t2.FINSCHOOL_DISTCD, ");
            stb.append("     t4.NAME1 AS FINSCHOOL_DISTNAME, ");
            stb.append("     t1.EXAMNO, ");
            stb.append("     t1.NAME, ");
            stb.append("     t1.SEX, ");
            stb.append("     t3.ABBV1 AS SEX_NAME ");
            stb.append(" from ENTEXAM_APPLICANTBASE_DAT t1 ");
            stb.append("     left join FINSCHOOL_MST t2 on ");
            stb.append("         t1.FS_CD = t2.FINSCHOOLCD ");
            stb.append("     left join NAME_MST t3 on ");
            stb.append("         t3.NAMECD1 = 'Z002' and ");
            stb.append("         t3.NAMECD2 = t1.SEX ");
            stb.append("     left join NAME_MST t4 on ");
            stb.append("         t4.NAMECD1 = 'L001' and ");
            stb.append("         t4.NAMECD2 = t2.FINSCHOOL_DISTCD ");
            stb.append(" where ");
            stb.append("     t1.JUDGEMENT in ('1','7') and ");
            stb.append("     t1.PROCEDUREDIV = '1' and ");
            stb.append("     t1.ENTDIV = '1' and ");
            stb.append("     t1.ENTEXAMYEAR = '"+_param._year+"' and ");
            stb.append("     t1.APPLICANTDIV = '"+_param._applicantDiv+"' ");
            stb.append(" order by ");
            stb.append("     t1.FS_CD, t1.EXAMNO ");
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
            if (ps1 != null) ps1.close();
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
    
    class FS {
        final String _fsCd;
        public String _finschoolName;
        public String _finschoolDistname;
        public String _finschoolAddr2;
        public String _finschoolAddr1;
        public String _finschoolZipcd;
        public List _examineeList = new ArrayList();
        public FS(final String fsCd) {
            _fsCd = fsCd;
        }
    }
    
    class Examinee {
        final String _examno;
        final String _name;
        final String _sexname;
        public Examinee(final String examno, final String name, final String sexname) {
            _examno = examno;
            _name = name;
            _sexname = sexname;
        }
        
    }

    class Param {
        
        final String _year;
        final String _applicantDiv;
        final String _loginDate;
        final String _printDate;
        final String _z010SchoolCode;

        private boolean _seirekiFlg;

        Param(DB2UDB db2, HttpServletRequest request) {
            _year = request.getParameter("YEAR");                        //年度
            _applicantDiv = request.getParameter("APPLICANTDIV");                //入試制度
            _loginDate = request.getParameter("LOGIN_DATE");                  // ログイン日付
            _printDate = request.getParameter("PRINT_DATE");
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
