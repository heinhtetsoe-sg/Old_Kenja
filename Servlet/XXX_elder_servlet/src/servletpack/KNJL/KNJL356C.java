package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;


/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 *      ＜ＫＮＪＬ３５６Ｈ＞
 *      ＜１＞  合格者数一覧
 *      ＜２＞  入学者数一覧
 *
 *    2008/11/19 takara 作成日
 *
 **/

public class KNJL356C {
    private static final Log log = LogFactory.getLog(KNJL356C.class);

    boolean nonedata = true;            //対象となるデータがあるかのフラグ(『true』の時は中身が空の意)
    Param _param;


/****************************************************************************************************************/
/****************************************************************************************************************/
/**********************************                                   *******************************************/
/**********************************            処理のメイン           *******************************************/
/**********************************                                   *******************************************/
/****************************************************************************************************************/
/****************************************************************************************************************/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        Vrw32alp svf     = new Vrw32alp();   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2       = null;             //Databaseクラスを継承したクラス

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
        _param = new Param(request, db2);

        //■■■SVF出力
        setSvfMain(db2, svf); //帳票出力のメソッド

        //■■■該当データ無し(nonedate=falseで該当データなし)
        if( nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }

        //■■■終了処理
        svf.VrQuit();
        db2.commit();
        db2.close();       //DBを閉じる
        outstrm.close();   //ストリームを閉じる
    }


/****************************************************************************************************************/
/****************************************************************************************************************/
/**********************************                                   *******************************************/
/**********************************           フォームに出力          *******************************************/
/**********************************                                   *******************************************/
/****************************************************************************************************************/
/****************************************************************************************************************/
    private void setSvfMain(DB2UDB db2, Vrw32alp svf) {
        PreparedStatement sql = null;
        String nendo = getSakuseibi(db2);
        String testdivName;
        String sexName;
        String shdivName;
        String shdiv;
        String sex;
        String nyugakuMoney;
        String seifukuMoney;
        String name;
        String examno;
        String schoolname;
        String schooldiv;
        if (_param._applicantdiv.equals("1")) {
            schooldiv = "中";
        } else {
            schooldiv = "高";
        }
        try {
            sql = db2.prepareStatement(get_sql());
            ResultSet result = sql.executeQuery();
            while(result.next()) {
                name       = result.getString("NAME");
                examno     = result.getString("EXAMNO");
                schoolname = result.getString("SCHOOL_NAME");
                testdivName = result.getString("TESTDIV_NAME");
                sexName     = result.getString("SEX_NAME");
                shdivName   = result.getString("SHDIV_NAME");
                shdiv       = result.getString("SHDIV");
                sex         = result.getString("SEX");
                nyugakuMoney = result.getString("BANKTRANSFERMONEY");
                seifukuMoney = "1".equals(sex) ? result.getString("TOTAL_MONEY_BOY") : result.getString("TOTAL_MONEY_GIRL");

                //1人2枚
                for (int page = 1; page <= 2; page++) {
                    final String form;
                    if (_param.isGojo()) {
//                        if ("2".equals(_param._applicantdiv) && "2".equals(shdiv) && page == 1) {
//                            form = "KNJL356C_G.frm";
//                        } else {
                            form = "KNJL356C_2G.frm";
//                        }
                    } else if (_param.isWakayama()) {
                        form = "KNJL356C.frm";
                    } else {
                        form = "KNJL356C.frm";
                    }
                    svf.VrSetForm(form, 1);

                    List flist = new ArrayList();
                    flist.add("ERA_NAME");
                    flist.add("ERA_NAME2");
                    putGengou2(db2, svf, flist);

                    svf.VrsOut("NENDO"      , nendo);
                    svf.VrsOut("SCHOOL_DIV" , schooldiv);
                    svf.VrsOut("TESTDIV"    , testdivName);
                    svf.VrsOut("SEX"        , sexName);
                    if ("2".equals(_param._applicantdiv)) {
                        svf.VrsOut("SHDIV"      , shdivName);
                    }
                    svf.VrsOut("PRICENAME"  , page == 1 ? "入　学　金" : "制定学用品代");
                    svf.VrsOut("PRICE"      , page == 1 ? nyugakuMoney : seifukuMoney);
                    svf.VrsOut("NAME1"       , String.valueOf(name));
                    svf.VrsOut("EXAMNO1"     , String.valueOf(examno));
                    svf.VrsOut("SCHOOLNAME1" , String.valueOf(schoolname));
                    svf.VrsOut("NAME2"       , String.valueOf(name));
                    svf.VrsOut("EXAMNO2"     , String.valueOf(examno));
                    svf.VrsOut("SCHOOLNAME2" , String.valueOf(schoolname));

                    svf.VrEndPage();
                    nonedata = false;
                }
            }
        } catch (Exception e) {
            log.debug("prepareStatementでエラー" + e);
        }
    }

    private void putGengou2(final DB2UDB db2, final Vrw32alp svf, final List fieldList) {
        //元号(記入項目用)
        String[] dwk;
        if (_param._login_date.indexOf('/') >= 0) {
            dwk = StringUtils.split(_param._login_date, '/');
        } else if (_param._login_date.indexOf('-') >= 0) {
            dwk = StringUtils.split(_param._login_date, '-');
        } else {
            //ありえないので、固定値で設定。
            dwk = new String[1];
        }
        if (dwk.length >= 3) {
            final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(dwk[0]), Integer.parseInt(dwk[1]), Integer.parseInt(dwk[2]));
            for (final Iterator it = fieldList.iterator(); it.hasNext();) {
                final String setFieldStr = (String) it.next();
                svf.VrsOut(setFieldStr, gengou);
            }
        }
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
        if (null != _param._yearDate) {
            if (_seirekiFlg) {
                //2008年の形
                date = _param._year.toString().substring(2, 4);
            } else {
                //平成14年度の形
                date = KNJ_EditDate.h_format_JP_N(_param._yearDate);
                date = date.substring(2,4);
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

/****************************************************************************************************************/
/****************************************************************************************************************/
/**********************************                                   *******************************************/
/**********************************         sqlを作る                 *******************************************/
/**********************************                                   *******************************************/
/****************************************************************************************************************/
/****************************************************************************************************************/
    private String get_sql() {
        StringBuffer stb = new StringBuffer();

        stb.append(" WITH T_BANK AS ( ");
        stb.append("     SELECT ");
        stb.append("         '"+ _param._year +"' AS ENTEXAMYEAR, ");
        stb.append("         BANKTRANSFERMONEY ");
        stb.append("     FROM ");
        stb.append("         SCHOOL_BANK_MST ");
        stb.append("     WHERE ");
        if (_param._applicantdiv.equals("1")) {
            stb.append("         BANKTRANSFERCD = '1010' ");
        } else {
            stb.append("         BANKTRANSFERCD = '2010' ");
        }
        stb.append("     ) ");
        stb.append(" , T_ITEM AS ( ");
        stb.append("     SELECT ");
        stb.append("         ENTEXAMYEAR, ");
        stb.append("         SUM(MONEY_BOY) AS TOTAL_MONEY_BOY, ");
        stb.append("         SUM(MONEY_GIRL) AS TOTAL_MONEY_GIRL ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_COMMODITY_MST ");
        stb.append("     WHERE ");
        stb.append("         ENTEXAMYEAR = '"+ _param._year +"' ");
        stb.append("     GROUP BY ");
        stb.append("         ENTEXAMYEAR ");
        stb.append("     ) ");
        stb.append(" SELECT ");
        stb.append("     E1.NAME,E1.EXAMNO,C1.SCHOOL_NAME ");
        stb.append("    ,E1.TESTDIV,N1.NAME1 AS TESTDIV_NAME,E1.SEX,N2.NAME3 AS SEX_NAME,E1.SHDIV,N3.NAME1 AS SHDIV_NAME ");
        stb.append("    ,BANK.BANKTRANSFERMONEY ");
        stb.append("    ,ITEM.TOTAL_MONEY_BOY ");
        stb.append("    ,ITEM.TOTAL_MONEY_GIRL ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT E1  ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT E2 ON E2.ENTEXAMYEAR  = E1.ENTEXAMYEAR ");
        stb.append("                                    AND E2.APPLICANTDIV = E1.APPLICANTDIV ");
        stb.append("                                    AND E2.TESTDIV      = E1.TESTDIV ");
        stb.append("                                    AND E2.EXAMNO       = E1.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L004' ");
        stb.append("                          AND N1.NAMECD2 = E1.TESTDIV ");
        stb.append("     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'Z002' ");
        stb.append("                          AND N2.NAMECD2 = E1.SEX ");
        stb.append("     LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'L006' ");
        stb.append("                          AND N3.NAMECD2 = E1.SHDIV ");
        stb.append("     LEFT JOIN T_BANK BANK  ON BANK.ENTEXAMYEAR = E1.ENTEXAMYEAR ");
        stb.append("     LEFT JOIN T_ITEM ITEM  ON ITEM.ENTEXAMYEAR = E1.ENTEXAMYEAR ");
        stb.append("     LEFT JOIN CERTIF_SCHOOL_DAT C1  ON C1.YEAR          = E1.ENTEXAMYEAR ");
        if (_param._applicantdiv.equals("1")) {
            stb.append("                                AND C1.CERTIF_KINDCD = '105'  ");
        } else {
            stb.append("                                AND C1.CERTIF_KINDCD = '106'  ");
        }
        stb.append(" WHERE ");
        stb.append("         E1.ENTEXAMYEAR = '"+ _param._year +"' ");
        stb.append("     AND E1.APPLICANTDIV = '"+ _param._applicantdiv +"' ");
        stb.append("     AND E1.TESTDIV in "+ SQLUtils.whereIn(true, _param._testdiv) +" ");
        if (_param._print_type.equals("1")) {
            stb.append(" AND E1.JUDGEMENT = '1' ");
        } else if (_param._print_type.equals("5")) { //追加合格者全員
            stb.append(" AND E1.JUDGEMENT = '1' ");
            stb.append(" AND E1.SPECIAL_MEASURES is not null ");
        } else if (_param._print_type.equals("4")) { //志願者全員
        } else {
            stb.append(" AND E1.ENTEXAMYEAR  = E2.ENTEXAMYEAR ");
            stb.append(" AND E1.EXAMNO       = E2.EXAMNO ");
            stb.append(" AND E1.APPLICANTDIV = E2.APPLICANTDIV ");
            stb.append(" AND E1.TESTDIV      = E2.TESTDIV ");
        }
        if (_param._print_type.equals("3")) {
            stb.append(" AND E2.EXAMNO = '"+ _param._examno +"' ");
            if (null != _param._goukakusha) {
                stb.append(" AND E1.JUDGEMENT = '1' ");
            }
        }
//        // 特併合格者は、特進文系コースの合格者と一緒に出力する。
//        if (("1".equals(_param._print_type) || "3".equals(_param._print_type) && null != _param._goukakusha) && _param._isTestdiv7) {
//            stb.append(" UNION ALL ");
//            stb.append(" SELECT ");
//            stb.append("     E1.NAME,E1.EXAMNO,C1.SCHOOL_NAME ");
//            stb.append("    ,'7' AS TESTDIV,N1.NAME1 AS TESTDIV_NAME,E1.SEX,N2.NAME3 AS SEX_NAME,E1.SHDIV,N3.NAME1 AS SHDIV_NAME ");
//            stb.append("    ,BANK.BANKTRANSFERMONEY ");
//            stb.append("    ,ITEM.TOTAL_MONEY_BOY ");
//            stb.append("    ,ITEM.TOTAL_MONEY_GIRL ");
//            stb.append(" FROM ");
//            stb.append("     ENTEXAM_APPLICANTBASE_DAT E1  ");
//            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT E2 ON E2.ENTEXAMYEAR  = E1.ENTEXAMYEAR ");
//            stb.append("                                    AND E2.APPLICANTDIV = E1.APPLICANTDIV ");
//            stb.append("                                    AND E2.TESTDIV      = E1.TESTDIV ");
//            stb.append("                                    AND E2.EXAMNO       = E1.EXAMNO ");
//            stb.append("     LEFT JOIN V_NAME_MST N1 ON N1.YEAR    = E1.ENTEXAMYEAR ");
//            stb.append("                            AND N1.NAMECD1 = 'L004' ");
//            stb.append("                            AND N1.NAMECD2 = '7' ");
//            stb.append("     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'Z002' ");
//            stb.append("                          AND N2.NAMECD2 = E1.SEX ");
//            stb.append("     LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'L006' ");
//            stb.append("                          AND N3.NAMECD2 = E1.SHDIV ");
//            stb.append("     LEFT JOIN T_BANK BANK  ON BANK.ENTEXAMYEAR = E1.ENTEXAMYEAR ");
//            stb.append("     LEFT JOIN T_ITEM ITEM  ON ITEM.ENTEXAMYEAR = E1.ENTEXAMYEAR ");
//            stb.append("     LEFT JOIN CERTIF_SCHOOL_DAT C1  ON C1.YEAR          = E1.ENTEXAMYEAR ");
//            if (_param._applicantdiv.equals("1")) {
//                stb.append("                                AND C1.CERTIF_KINDCD = '105'  ");
//            } else {
//                stb.append("                                AND C1.CERTIF_KINDCD = '106'  ");
//            }
//            stb.append(" WHERE ");
//            stb.append("         E1.ENTEXAMYEAR = '"+ _param._year +"' ");
//            stb.append("     AND E1.APPLICANTDIV = '"+ _param._applicantdiv +"' ");
//            stb.append("     AND E1.TESTDIV = '3' ");
//            stb.append("     AND E1.JUDGEMENT = '7' ");
//            if (_param._print_type.equals("3")) {
//                stb.append(" AND E2.EXAMNO = '"+ _param._examno +"' ");
//            }
//        }
        stb.append(" ORDER BY EXAMNO ");

        log.debug("SQL = " + stb.toString());
        return stb.toString();
    }


/****************************************************************************************************************/
/****************************************************************************************************************/
/**********************************                                   *******************************************/
/**********************************         paramを受け取る           *******************************************/
/**********************************                                   *******************************************/
/****************************************************************************************************************/
/****************************************************************************************************************/
    /** パラメータクラス */
    private class Param {
        private final String _prgid;
        private final String _dbname;
        private final String _year;
        private final String _yearDate;
        private final String _login_date;
        private final String _applicantdiv;
        private final String[] _testdiv;
        private final String _print_type;
        private final String _check_examno;
        private final String _examno;
        private final String _goukakusha;
        private final String _z010SchoolCode;
//        private boolean _isTestdiv7;

        Param(
                final HttpServletRequest request,
                final DB2UDB db2
        ) {
            _prgid        = request.getParameter("PRGID");
            _dbname       = request.getParameter("DBNAME");
            _year         = request.getParameter("YEAR");
            _yearDate = null != _year ? _year + "/01/01" : null;
            _login_date   = request.getParameter("LOGIN_DATE");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            String temp_testDiv = request.getParameter("TESTDIV");
            _print_type   = request.getParameter("PRINT_TYPE");
            _check_examno = request.getParameter("CHECK_EXAMNO");
            _examno       = request.getParameter("EXAMNO");
            _goukakusha = request.getParameter("GOUKAKUSHA");
            _z010SchoolCode = getSchoolCode(db2);
//            _isTestdiv7 = false;
            if ("9".equals(temp_testDiv)) {
                if("1".equals(_applicantdiv)) {
                    _testdiv = new String[]{"1","2"};
                } else if("2".equals(_applicantdiv)) {
                    if (isGojo()) {
                        _testdiv = new String[]{"3","4","5","7"};
//                        _isTestdiv7 = true;
                    } else {
                        _testdiv = new String[]{"3","4","5"};
                    }
                } else {
                    _testdiv = null;
                }
            } else {
//                if ("7".equals(temp_testDiv)) _isTestdiv7 = true;
                _testdiv = new String[]{temp_testDiv};
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
            return "30290053001".equals(_z010SchoolCode);
        }

        boolean isWakayama() {
            return "30300049001".equals(_z010SchoolCode);
        }
    }
}//クラスの括り
