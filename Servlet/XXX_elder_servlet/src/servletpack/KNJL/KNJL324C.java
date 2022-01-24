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

import servletpack.KNJZ.detail.KNJ_EditDate;


/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 *      ＜ＫＮＪＬ３２７Ｈ＞
 *      ＜１＞  合格者発表資料（掲示用）
 *
 *    2008/11/21 takara 作成日
 *
 **/

public class KNJL324C {
    private static final Log log = LogFactory.getLog(KNJL324C.class);
    Param _param;

    /**
     * メインの処理
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        Vrw32alp svf     = new Vrw32alp();   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2       = null;             //Databaseクラスを継承したクラス
        boolean nonedata = true;            //対象となるデータがあるかのフラグ(『true』の時は中身が空の意)

        //■■■print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        //■■■svf設定
        svf.VrInit();    //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定

        //■■■ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }
        _param = new Param(db2, request);

        svf.VrSetForm("KNJL324C.frm", 1);
        //■■■SVF出力
        for (int i=0; i<_param._testdiv.length; i++) {
            nonedata &= setSvfMain(db2, svf, _param._testdiv[i], nonedata); //帳票出力のメソッド
        }

        //■■■該当データ無し(nonedate=falseで該当データなし)
        log.debug("nonedata="+nonedata);
        if( nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
        }
        svf.VrEndPage();

        //■■■終了処理
        svf.VrQuit();
        db2.commit();
        db2.close();       //DBを閉じる
        outstrm.close();   //ストリームを閉じる
    }

    /**
     * フォームの作成
     * @param db2
     * @param svf
     */
    private boolean setSvfMain(DB2UDB db2, Vrw32alp svf, String testdiv,boolean nonedataBefore) {
        boolean nonedata = true;
        final String nendo = getSakuseibi(db2);
        String examno = null;
        String applicantdiv = null;
        String testdivname = null;
        String new_testdiv = null;
        String old_testdiv = "";
        String new_judgediv = null;
        String old_judgediv = "";
        boolean kaigyou_flg = true;
        boolean hajimedake  = true;
        PreparedStatement sql_main = null;
        try {
            final String schoolname = get_school_name(db2);

            sql_main = db2.prepareStatement(get_sql_main(testdiv));
            ResultSet result_main = sql_main.executeQuery();
            int retu_count = 1;
            int gyo_count = 0;
            
            String judgement = "";
            if ("1".equals(_param._printType)) {
                judgement = "合格";
            } else if ("2".equals(_param._printType)) {
                judgement = "補欠合格";
            } else if ("3".equals(_param._printType)) {
                judgement = "移行合格";
            }
            
            while(result_main.next()) {
                if (hajimedake) {
                    if (nonedataBefore == false) {
                        svf.VrEndPage();
                    }
                    old_testdiv = result_main.getString("TESTDIV");
                    old_judgediv = result_main.getString("JUDGEDIV");
                    hajimedake = false;
                }
                gyo_count++;
                if (gyo_count > 10) {
                    gyo_count = 1;
                    retu_count++;
                    if (retu_count > 5) {
                        svf.VrEndPage();
                        kaigyou_flg = false;
                        retu_count = 1;
                    }
                }
                examno  = result_main.getString("EXAMNO");
                applicantdiv  = result_main.getString("APPLICANT");
                final String shdivName = _param.isGojo() ? "" : result_main.getString("SHDIVNAME");
                testdivname  = result_main.getString("TESTDIVNAME") + shdivName;
                new_testdiv = result_main.getString("TESTDIV");
                new_judgediv = result_main.getString("JUDGEDIV");
                if ("1".equals(_param._printType)) {
                    if (_param.isCollege()) {
                        judgement = "1".equals(new_judgediv) ? "合格" : "8".equals(new_judgediv) ? "Ｓ選抜クラス合格" : "9".equals(new_judgediv) ? "総合選抜クラス合格" : "A".equals(new_judgediv) ? "ＥＭＡコース合格" : "B".equals(new_judgediv) ? "ＥＭＳコース合格" : "";
                    } else if (_param.isGojo()) {
                        judgement = "1".equals(new_judgediv) ? "合格" : "8".equals(new_judgediv) ? "Ｓ特別選抜クラス合格" : "9".equals(new_judgediv) ? "ＡＢ総合選抜クラス合格" : "";
                    } else {
                        judgement = "1".equals(new_judgediv) ? "合格" : "8".equals(new_judgediv) ? "Ｓ選抜クラス合格" : "9".equals(new_judgediv) ? "総合選抜クラス合格" : "";
                    }
                }
                if (!old_testdiv.equals(new_testdiv) && kaigyou_flg || !old_judgediv.equals(new_judgediv)) {
                    svf.VrEndPage();
                    retu_count = 1;
                    gyo_count = 1;
                }
                svf.VrsOutn("EXAMNO" + retu_count, gyo_count, examno);
                svf.VrsOut("APPLICANT", applicantdiv);
                svf.VrsOut("TESTDIV", testdivname);
                svf.VrsOut("SCHOOLNAME", schoolname);
                svf.VrsOut("NENDO", nendo);
                svf.VrsOut("JUDGEMENT", judgement);

                old_testdiv = new_testdiv;
                old_judgediv = new_judgediv;
                kaigyou_flg = true;
                nonedata = false;
            }
        } catch (Exception e) {
            log.debug("prepareStatementでエラー" + e);
        }
        return nonedata;
    }


    /**
     * 学校名を返す
     */
    private String get_school_name(DB2UDB db2) throws SQLException {
        PreparedStatement sql_get_schoolname;
        sql_get_schoolname = db2.prepareStatement(get_schoolname());
        ResultSet result_schoolname = sql_get_schoolname.executeQuery();
        ;
        if (result_schoolname.next()) {
            final String schoolname = result_schoolname.getString("SCHOOL_NAME");
            return schoolname;
        }
        return null;
    }

    /**
     * 『年度』を返す
     * @param db2
     * @return
     */
    private String getSakuseibi(DB2UDB db2) {
        // 西暦か和暦はフラグで判断
        boolean _seirekiFlg = getSeirekiFlg(db2);
        String date;
        if (null != _param._year) {
            if (_seirekiFlg) {
                //2008年3月3日の形
                date = _param._year.toString().substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(_param._year);
            } else {
                //平成14年度の形
                date = KNJ_EditDate.h_format_JP_N(_param._year+"-01-01") + "度";
            }
            return date;
        }
        return null;
    }
    /*■■■■■   西暦表示にするのかのフラグ   ■■■■■*/
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


    /**
     * SQL文を返す
     * @return
     */
    private String get_schoolname() {
        StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     SCHOOL_NAME ");
        stb.append(" FROM ");
        stb.append("     CERTIF_SCHOOL_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '"+ _param._year +"' AND ");
        if (_param._applicantdiv.equals("1")) {
            stb.append("     CERTIF_KINDCD = '105' ");//中学は105
        } else {
            stb.append("     CERTIF_KINDCD = '106' ");//高校は106
        }

        log.debug("get_schoolname SQL = " + stb.toString());
        return stb.toString();
    }


    private String get_sql_main(String testdiv) {
        StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.JUDGEDIV, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     N3.NAME1 AS TESTDIVNAME, ");
        if (_param.isGojo()) {
            stb.append("     '（' || N4.NAME1 || '）' AS SHDIVNAME, ");
        } else {
            stb.append("     case when T1.TESTDIV = '3' then '（' || N4.NAME1 || '）' else '' end AS SHDIVNAME, ");
        }
        stb.append("     N2.NAME1 AS APPLICANT ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT T1   ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ");
        stb.append("             ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("            AND T2.EXAMNO       = T1.EXAMNO ");
        if (!_param.isGojo() || !_param.isCollege() && !"1".equals(_param._applicantdiv)) {
            stb.append("            AND T2.SHDIV        = '" + _param._shdiv + "' ");
        }
        stb.append(" LEFT JOIN NAME_MST N2 ON N2.NAMECD1    = 'L003' ");
        stb.append("                         AND N2.NAMECD2 = T1.APPLICANTDIV ");
        stb.append(" LEFT JOIN NAME_MST N3 ON N3.NAMECD1    = 'L004' ");
        stb.append("                         AND N3.NAMECD2 = T1.TESTDIV ");
        stb.append(" LEFT JOIN NAME_MST N4 ON N4.NAMECD1    = 'L006' ");
        stb.append("                         AND N4.NAMECD2 = T2.SHDIV ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '"+ _param._year +"' AND ");
        stb.append("     T1.APPLICANTDIV = '"+ _param._applicantdiv +"' AND ");
        stb.append("     T1.TESTDIV = '"+ testdiv +"' AND ");
        if ("1".equals(_param._printType)) { // 合格者
            if (_param.isCollege()) {
                stb.append("     T1.JUDGEDIV in ('1','8','9','A','B') ");
            } else if (_param.isGojo()) {
                stb.append("     T1.JUDGEDIV in ('1','8','9') ");
            } else {
                stb.append("     T1.JUDGEDIV in ('1','8','9') ");
            }
        }
        if ("2".equals(_param._printType)) { stb.append("     T1.JUDGEDIV = '3' "); } // 補欠合格者
        if ("3".equals(_param._printType)) { stb.append("     T1.JUDGEDIV = '4' "); } // 移行合格者
        stb.append(" ORDER BY ");
        stb.append("     T1.TESTDIV, T1.JUDGEDIV, T1.EXAMNO ");

        log.debug("main SQL = " + stb.toString());
        return stb.toString();
    }

    /**
     * パラメーターを受け取るクラス
     */
    private class Param {
        private final String _prgid;
        private final String _dbname;
        private final String _year;
        private final String _login_date;
        private final String _applicantdiv;
        private final String[] _testdiv;
        private final String _printType;
        private final String _shdiv;
        private final String _z010SchoolCode;

        Param(
                final DB2UDB db2,
                final HttpServletRequest request
        ) {
            _prgid        = request.getParameter("PRGID");
            _dbname       = request.getParameter("DBNAME");
            _year         = request.getParameter("YEAR");
            _login_date   = request.getParameter("LOGIN_DATE");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _shdiv = request.getParameter("SHDIV");
            _z010SchoolCode = getSchoolCode(db2);
            String temp_testdiv      = request.getParameter("TESTDIV");
            if ("9".equals(temp_testdiv)) {
                if ("1".equals(_applicantdiv)) {
                    _testdiv = new String[]{"1","2"};
                } else if ("2".equals(_applicantdiv)) {
                    if (isGojo()) {
                        _testdiv = new String[]{"3","4","5","7"}; 
                    } else {
                        _testdiv = new String[]{"3","4","5"}; 
                    }
                } else {
                    _testdiv = null;
                }
            } else {
                _testdiv = new String[]{temp_testdiv};
            } 
            _printType = request.getParameter("JUDGEMENT");
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
