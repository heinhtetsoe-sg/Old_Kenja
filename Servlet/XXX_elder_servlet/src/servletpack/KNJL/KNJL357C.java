package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;


/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 *      ＜ＫＮＪＬ３５７Ｃ＞
 *      ＜１＞  注文書
 *
 *    2008/11/19 takara 作成日
 *
 **/

public class KNJL357C {
    private static final Log log = LogFactory.getLog(KNJL357C.class);

    boolean nonedata = true;            //対象となるデータがあるかのフラグ(『true』の時は中身が空の意)
    Param _param;


    /**
     * メインの流れ
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
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
        _param.loadItemValue(db2);

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



    /**
     * 
     * svf出力
     * @param db2
     * @param svf
     */
    private void setSvfMain(final DB2UDB db2, final Vrw32alp svf) {
        PreparedStatement sql = null;
        String nendo = getSakuseibi();
        String school_div;
        if (_param._applicantdiv.equals("1")) {
            school_div = "中";
        } else {
            school_div = "高";
        }
        try {
            sql = db2.prepareStatement(get_sql());
            ResultSet result = sql.executeQuery();
            while(result.next()) {
                final String testdiv    = result.getString("TESTDIV");
                final String sexName    = result.getString("SEX_NAME");
                final String sex        = null != result.getString("SEX") ? result.getString("SEX") : "1";  //ないとは思うけど、念のため。
                final String examno     = result.getString("EXAMNO");
                final String name       = result.getString("NAME");
                final String schoolname       = result.getString("SCHOOL_NAME");

                final String form;
                if (_param.isCollege()) {
                    form = "KNJL357C_" + sex + "C.frm";
                } else if (_param.isGojo()) {
                    form = "KNJL357C_" + sex + "G.frm";
                } else {
                    form = "KNJL357C_" + sex + ".frm";
                }
                svf.VrSetForm(form, 1);

                svf.VrsOut("TESTDIV"    , testdiv);
                svf.VrsOut("SEX"        , sexName);
                svf.VrsOut("NENDO"      , nendo);
                svf.VrsOut("SCHOOL_DIV" , school_div);
                svf.VrsOut("EXAMNO"     , examno);
                svf.VrsOut("NAME"       , name);
                svf.VrsOut("TOTAL"      , _param.getValue("99", sex));
                svf.VrsOut("SCHOOLNAME" , schoolname);
                
                for (int i = 1; i <= 10; i++) {
                    svf.VrsOut("TESTDIV2_" + i , testdiv);
                    svf.VrsOut("SCHOOL_DIV2_" + i , school_div);
                    svf.VrsOut(_param.getItemNameField(i) , _param.getName(i));
                    svf.VrsOut(_param.getItemValueField(i) , _param.getValue(i, sex));
                }

                svf.VrEndPage();
                nonedata = false;
            }
        } catch (Exception e) {
            log.debug("prepareStatementでエラー", e);
        }
    }

    /**
     * 『年度』を返す
     * @param db2
     * @return
     */
    private String getSakuseibi() {
        String date;
        if (null != _param._yearDate) {
            if (_param._seirekiFlg) { // 西暦か和暦はフラグで判断
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

    /**
     * 
     * sqlを作る
     * @return
     */
    private String get_sql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     E1.NAME,E1.EXAMNO,N1.NAME1 AS TESTDIV,N2.NAME3 AS SEX_NAME, E1.SEX, C1.SCHOOL_NAME ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT E1 ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT E2 ON E2.ENTEXAMYEAR  = E1.ENTEXAMYEAR ");
        stb.append("                                    AND E2.APPLICANTDIV = E1.APPLICANTDIV ");
        stb.append("                                    AND E2.TESTDIV      = E1.TESTDIV ");
        stb.append("                                    AND E2.EXAMNO       = E1.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L004' ");
        stb.append("                          AND N1.NAMECD2 = E1.TESTDIV ");
        stb.append("     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'Z002' ");
        stb.append("                          AND N2.NAMECD2 = E1.SEX ");
        stb.append("     LEFT JOIN CERTIF_SCHOOL_DAT C1 ON ");
        if (_param._applicantdiv.equals("1")) {
            stb.append("                               C1.CERTIF_KINDCD = '105'  ");
        } else {
            stb.append("                               C1.CERTIF_KINDCD = '106'  ");
        }
        stb.append("                                   AND C1.YEAR = E1.ENTEXAMYEAR ");
        stb.append(" WHERE ");
        stb.append("         E1.ENTEXAMYEAR  = '"+ _param._year +"' ");
        stb.append("     AND E1.APPLICANTDIV = '"+ _param._applicantdiv +"' ");
        stb.append("     AND E1.TESTDIV in "+ SQLUtils.whereIn(true, _param._testdiv) +" ");
        stb.append("     AND E1.SEX = '"+ _param._sex +"' ");
        if (_param._print_type.equals("1")) {
            stb.append("     AND E1.JUDGEMENT    = '1' ");
        } else if (_param._print_type.equals("4")) { //志願者全員
        } else {
            stb.append("     AND E2.ENTEXAMYEAR  = E1.ENTEXAMYEAR ");
            stb.append("     AND E2.APPLICANTDIV = E1.APPLICANTDIV ");
            stb.append("     AND E2.TESTDIV      = E1.TESTDIV ");
            stb.append("     AND E2.EXAMNO       = E1.EXAMNO ");
        }
        if (_param._print_type.equals("3")) {
            stb.append("     AND E2.EXAMNO       = '"+ _param._examno +"' ");
            if (null != _param._goukakusha) {
                stb.append(" AND E1.JUDGEMENT = '1' ");
            }
        }
//        // 特併合格者は、特進文系コースの合格者と一緒に出力する。
//        if (("1".equals(_param._print_type) || "3".equals(_param._print_type) && null != _param._goukakusha) && _param._isTestdiv7) {
//            stb.append(" UNION ALL ");
//            stb.append(" SELECT ");
//            stb.append("     E1.NAME,E1.EXAMNO,N1.NAME1 AS TESTDIV,N2.NAME3 AS SEX_NAME, E1.SEX, C1.SCHOOL_NAME ");
//            stb.append(" FROM ");
//            stb.append("     ENTEXAM_APPLICANTBASE_DAT E1 ");
//            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT E2 ON E2.ENTEXAMYEAR  = E1.ENTEXAMYEAR ");
//            stb.append("                                    AND E2.APPLICANTDIV = E1.APPLICANTDIV ");
//            stb.append("                                    AND E2.TESTDIV      = E1.TESTDIV ");
//            stb.append("                                    AND E2.EXAMNO       = E1.EXAMNO ");
//            stb.append("     LEFT JOIN V_NAME_MST N1 ON N1.YEAR    = E1.ENTEXAMYEAR ");
//            stb.append("                            AND N1.NAMECD1 = 'L004' ");
//            stb.append("                            AND N1.NAMECD2 = '7' ");
//            stb.append("     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'Z002' ");
//            stb.append("                          AND N2.NAMECD2 = E1.SEX ");
//            stb.append("     LEFT JOIN CERTIF_SCHOOL_DAT C1 ON ");
//            if (_param._applicantdiv.equals("1")) {
//                stb.append("                               C1.CERTIF_KINDCD = '105'  ");
//            } else {
//                stb.append("                               C1.CERTIF_KINDCD = '106'  ");
//            }
//            stb.append("                                   AND C1.YEAR = E1.ENTEXAMYEAR ");
//            stb.append(" WHERE ");
//            stb.append("         E1.ENTEXAMYEAR  = '"+ _param._year +"' ");
//            stb.append("     AND E1.APPLICANTDIV = '"+ _param._applicantdiv +"' ");
//            stb.append("     AND E1.SEX = '"+ _param._sex +"' ");
//            stb.append("     AND E1.TESTDIV = '3' ");
//            stb.append("     AND E1.JUDGEMENT    = '7' ");
//            if (_param._print_type.equals("3")) {
//                stb.append("     AND E2.EXAMNO       = '"+ _param._examno +"' ");
//            }
//        }
        stb.append(" ORDER BY EXAMNO ");

        log.debug("SQL = " + stb.toString());
        return stb.toString();
    }



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
        private final String _examno;
        private final String _goukakusha;
        private final String _sex;
        private final boolean _seirekiFlg;
//        private boolean _isTestdiv7;
        
        private final DecimalFormat _valueFormat;
        private final Map _hankakuToZenkaku;;
        private final Map _itemNameFields;
        private final Map _itemValueFields;
        private final String _z010SchoolCode;
        private Map _itemName = Collections.EMPTY_MAP;
        private Map _itemValueMan = Collections.EMPTY_MAP;
        private Map _itemValueWoman = Collections.EMPTY_MAP;

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
            _print_type   = request.getParameter("PRINT_TYPE");
            _examno       = request.getParameter("EXAMNO");
            _goukakusha = request.getParameter("GOUKAKUSHA");
            _sex          = request.getParameter("SEX");
            _seirekiFlg   = getSeirekiFlg(db2);
            
            _itemValueFields = new HashMap();
            _itemNameFields = new HashMap();
            for (int i = 1; i <= 10; i++) {
                final String itemCd = String.valueOf(i);
                final String lr = i % 2 == 1 ? "L" : "R";
                final String n = String.valueOf((i + 1) / 2);
                _itemValueFields.put(itemCd, lr + "_PRICE" + n);
                _itemNameFields.put(itemCd, lr + "_NAME" + n);
            }

            
            _hankakuToZenkaku = new HashMap();
            _hankakuToZenkaku.put("0", "０");
            _hankakuToZenkaku.put("1", "１");
            _hankakuToZenkaku.put("2", "２");
            _hankakuToZenkaku.put("3", "３");
            _hankakuToZenkaku.put("4", "４");
            _hankakuToZenkaku.put("5", "５");
            _hankakuToZenkaku.put("6", "６");
            _hankakuToZenkaku.put("7", "７");
            _hankakuToZenkaku.put("8", "８");
            _hankakuToZenkaku.put("9", "９");
            _hankakuToZenkaku.put(",", "，");
            
            _valueFormat = new DecimalFormat();
            _valueFormat.setGroupingUsed(true);
            final DecimalFormatSymbols dfs = new DecimalFormatSymbols();
            dfs.setDecimalSeparator(',');
            _valueFormat.setDecimalFormatSymbols(dfs);
        }
        
        public String getValue(String key, String sex) {
            final String value;
            if ("2".equals(sex)) {
                value = (String) _itemValueWoman.get(key);
            } else {
                value = (String) _itemValueMan.get(key);
            }
            return value;
        }
        
        public String getName(int i) {
            return (String) _itemName.get(i < 10 ? String.valueOf("0" + i) : String.valueOf(i));
        }
        
        public String getValue(int i, String sex) {
            return getValue(i < 10 ? String.valueOf("0" + i) : String.valueOf(i), sex);
        }
        
        public String getItemNameField(int i) {
            return (String) _param._itemNameFields.get(String.valueOf(i));
        }
        
        public String getItemValueField(int i) {
            return (String) _param._itemValueFields.get(String.valueOf(i));
        }
        
        public String valueFormat(final Long number) {
            try {
                final StringBuffer sb = new StringBuffer();
                final String formatted = _valueFormat.format(number);
                if (null != formatted) {
                    for (int i = 0; i < formatted.length(); i++) {
                        sb.append((String) _hankakuToZenkaku.get(String.valueOf(formatted.charAt(i))));
                    }
                }
                return sb.append("円").toString();
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return "";
        }
        
        public void loadItemValue(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _itemName = new HashMap();
            _itemValueMan = new HashMap();
            _itemValueWoman = new HashMap();
            long sumValueMan = 0;
            long sumValueWoman = 0;
            try {
                final String sql = getItemSql();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    
                    final Long lvalueMan = parseLong(rs.getString("MONEY_BOY"));
                    final Long lvalueWoman = parseLong(rs.getString("MONEY_GIRL"));
                    
                    final String itemCd = rs.getString("ITEMCD");
                    if (!StringUtils.isNumeric(itemCd) || Integer.parseInt(itemCd) <= 0 || Integer.parseInt(itemCd) > 10) {
                        continue;
                    }
                    _itemName.put(itemCd, rs.getString("ITEMNAME"));
                    if (null != lvalueMan) {
                        sumValueMan += lvalueMan.longValue();
                        _itemValueMan.put(itemCd, valueFormat(lvalueMan));
                    }
                    if (null != lvalueWoman) {
                        sumValueWoman += lvalueWoman.longValue();
                        _itemValueWoman.put(itemCd, valueFormat(lvalueWoman));
                    }
                }
                
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if (0 != sumValueMan) {
                _itemValueMan.put("99", valueFormat(new Long(sumValueMan)));
            }
            if (0 != sumValueWoman) {
                _itemValueWoman.put("99", valueFormat(new Long(sumValueWoman)));
            }
        }
        
        private Long parseLong(String value) {
            if (null == value || "".equals(value)) {
                return null;
            }
            try {
                return Long.valueOf(value);
            } catch (Exception e) {
                log.error("exception! value = " + value, e);
                return null;
            }
        }
        
        /*■■■■■   西暦表示にするのかのフラグ   ■■■■■*/
        private boolean getSeirekiFlg(DB2UDB db2) {
            boolean seirekiFlg = false;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            }
            return seirekiFlg;
        }
        
        private String getItemSql() {
            final StringBuffer sb = new StringBuffer();
            sb.append(" SELECT ");
            sb.append("     ITEMCD, ");
            sb.append("     ITEMNAME, ");
            sb.append("     MONEY_BOY, ");
            sb.append("     MONEY_GIRL ");
            sb.append(" FROM ");
            sb.append("     ENTEXAM_COMMODITY_MST ");
            sb.append(" WHERE ");
            sb.append("     ENTEXAMYEAR = '" + _year + "' ");
            return sb.toString();
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
