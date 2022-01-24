package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *    学校教育システム 賢者 [入試管理] 振込依頼書
 *
 **/
public class KNJL360C {

    private static final Log log = LogFactory.getLog(KNJL360C.class);

    boolean nonedata = true;
    Param _param;

    /**
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {

        Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;

        final PrintWriter outstrm = new PrintWriter(response.getOutputStream());
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());

        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            log.debug(" $rev ");
            _param = new Param(request, db2);

            setSvfMain(db2, svf); //帳票出力のメソッド

        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
        } finally {
            db2.commit();
            db2.close();       //DBを閉じる
        }

        if (nonedata) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }
        svf.VrQuit();
        outstrm.close();   //ストリームを閉じる
    }

    private String formatDateJp(final String date, final int pattern) {
        if ("".equals(date) || null == date) {
            return "";
        }
        final int year = Integer.parseInt(date.substring(0, 4));
        final int month = Integer.parseInt(date.substring(5, 7));
        final int day = Integer.parseInt(date.substring(8, 10));
        final String gengo = KenjaProperties.gengou(year) + "年";
        final String monthStr = month >= 10 ? String.valueOf(month) : _param.toZenkaku(String.valueOf(month));
        final String dayStr = day >= 10 ? String.valueOf(day) : _param.toZenkaku(String.valueOf(day));
        final String dow = KNJ_EditDate.h_format_W(date);
        return (pattern == 1) ? monthStr + "月" + dayStr + "日" + "(" + dow + ")" : (pattern == 2) ? gengo + monthStr + "月" + dayStr + "日" : gengo + monthStr + "月" + dayStr + "日" + "(" + dow + ")";
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
     *
     * svf出力
     * @param db2
     * @param svf
     */
    private void setSvfMain(final DB2UDB db2, final Vrw32alp svf) {
        final String entDate = formatDateJp(_param._entDate, 1);
        final String strDate = formatDateJp(_param._strDate, _param.isCollege() ? 3 : 2);
        final String endDate = formatDateJp(_param._endDate, 2);
        final String printTime;
        if (Integer.parseInt(_param._printTime) > 12) {
            final int h = Integer.parseInt(_param._printTime) - 12;
            printTime = "午後" + (h >= 10 ? String.valueOf(h) : _param.toZenkaku(String.valueOf(h))) + "時";
        } else {
            final int h = Integer.parseInt(_param._printTime);
            printTime = "午前" + (h >= 10 ? String.valueOf(h) : _param.toZenkaku(String.valueOf(h))) + "時";
        }
        String strDate2 = "";
        String endDate2 = "";
        String printTime2 = "";
        if (_param.isGojo()) {
            if (null != _param._strDate2) {
                strDate2 = formatDateJp(_param._strDate2, _param.isCollege() ? 3 : 2);
            }
            if (null != _param._endDate2) {
                endDate2 = formatDateJp(_param._endDate2, 2);
            }
            if (null != _param._printTime2) {
                if (Integer.parseInt(_param._printTime2) > 12) {
                    final int h = Integer.parseInt(_param._printTime2) - 12;
                    printTime2 = "午後" + (h >= 10 ? String.valueOf(h) : _param.toZenkaku(String.valueOf(h))) + "時";
                } else {
                    final int h = Integer.parseInt(_param._printTime2);
                    printTime2 = "午前" + (h >= 10 ? String.valueOf(h) : _param.toZenkaku(String.valueOf(h))) + "時";
                }
            }
        }
        String form = "on".equals(_param._check) ? "KNJL360C_2.frm" : "KNJL360C.frm";
        if (_param.isCollege()) {
            form = _param._isTestdiv6 ? "KNJL360C_2C.frm" : "KNJL360C_C.frm";
        } else
        if (_param.isGojo()) {
            if ("on".equals(_param._check)) {
                form = "KNJL360C_2G.frm";
            } else {
                form = "KNJL360C_G.frm";
            }
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            ps = db2.prepareStatement(sql);
            log.debug("sql = " + sql);
            rs = ps.executeQuery();
            int count = 0;
            while (rs.next()) {
                svf.VrSetForm(form, 1);

                svf.VrsOut("NENDO", _param._nendo);
                svf.VrsOut("EXAMNO", rs.getString("EXAMNO"));
                svf.VrsOut("NAME", rs.getString("NAME"));
                svf.VrsOut("NAME_KANA", rs.getString("NAME_KANA"));
                svf.VrsOut("SEX", rs.getString("SEX_NAME"));
                svf.VrsOut("SCHOOLNAME", rs.getString("SCHOOL_NAME"));
                svf.VrsOut("SCHOOLNAME1_4", rs.getString("SCHOOL_NAME"));
                if (!"1".equals(_param._applicantdiv)) {
                    svf.VrsOut("TESTDIV", rs.getString("TESTDIVNAME"));
                    svf.VrsOut("SHDIV" , rs.getString("SHDIV_NAME"));
                }

                int gengou_setcnt = "on".equals(_param._check) ? 2 : 4;
                if (_param.isCollege() && !_param._isTestdiv6) {
                    gengou_setcnt = 4;
                }
                List flist = new ArrayList();
                if (gengou_setcnt == 4) {
                    flist.add("ERA_NAME");
                    flist.add("ERA_NAME2");
                    flist.add("ERA_NAME3");
                    flist.add("ERA_NAME4");
                } else {
                    flist.add("ERA_NAME");
                    flist.add("ERA_NAME2");
                }
            	putGengou2(db2, svf, flist);

                svf.VrsOut("PRICE1", rs.getString("BANK_MONEY1"));
                svf.VrsOut("PRICE2", _param.getValue(Param.KEY_TOTAL, rs.getString("SEX")));
                svf.VrsOut("RECEIPT_NAME1", rs.getString("RECEIPT_NAME1"));
                svf.VrsOut("RECEIPT_NAME2", rs.getString("RECEIPT_NAME2"));
                svf.VrsOut("RECEIPT_NAME_KANA1", rs.getString("RECEIPT_NAME_KANA1"));
                svf.VrsOut("RECEIPT_NAME_KANA2", rs.getString("RECEIPT_NAME_KANA2"));
                svf.VrsOut("DEPOSIT_KIND1", rs.getString("DEPOSIT_KIND1"));
                svf.VrsOut("DEPOSIT_KIND2", rs.getString("DEPOSIT_KIND2"));
                svf.VrsOut("ACCOUNT_NAME1", rs.getString("ACCOUNT_NAME1"));
                svf.VrsOut("ACCOUNT_NAME2", rs.getString("ACCOUNT_NAME2"));

                svf.VrsOut("BANK_NAME1", rs.getString("BANK_NAME1"));
                svf.VrsOut("BANK_NAME1_2", rs.getString("BANK_NAME1"));
                svf.VrsOut("BRANCH_NAME1", rs.getString("BRANCH_NAME1"));
                svf.VrsOut("BRANCH_NAME1_2", rs.getString("BRANCH_NAME1"));
                svf.VrsOut("BANK_NAME2", rs.getString("BANK_NAME2"));
                svf.VrsOut("BANK_NAME2_2", rs.getString("BANK_NAME2"));
                svf.VrsOut("BRANCH_NAME2", rs.getString("BRANCH_NAME2"));
                svf.VrsOut("BRANCH_NAME2_2", rs.getString("BRANCH_NAME2"));

                svf.VrsOut("PROC_DATE", entDate);
                svf.VrsOut("STR_DATE", strDate);
                svf.VrsOut("END_DATE", endDate);
                svf.VrsOut("PRINT_TIME", printTime);

                svf.VrsOut("TELNO", rs.getString("TELNO"));
                svf.VrsOut("ADDRESS1_1", rs.getString("ADDRESS1"));
                svf.VrsOut("ADDRESS1_2", rs.getString("ADDRESS2"));

                String period = "";
                if (_param.isCollege()) {
                    period = period + strDate + " " + printTime + "まで";
                } else {
                    if ("1".equals(rs.getString("APPLICANTDIV")) && ("1".equals(rs.getString("TESTDIV")) || "2".equals(rs.getString("TESTDIV")))) {
                        period = period + rs.getString("TESTDIVNAME") + "　";
                    }
                    period = period + strDate + "〜" + endDate + " " + printTime + "まで";
                }
                svf.VrsOut("PERIOD1_1", period);
                if (_param.isGojo()) {
                    String period2 = "";
                    if (_param.isCollege()) {
                        period2 = period2 + strDate2 + " " + printTime2 + "までに納入してください。";
                    } else {
                        if ("1".equals(rs.getString("APPLICANTDIV")) && ("1".equals(rs.getString("TESTDIV")) || "2".equals(rs.getString("TESTDIV")))) {
                            period2 = period2 + rs.getString("TESTDIVNAME") + "　";
                        }
                        period2 = period2 + strDate2 + "〜" + endDate2 + " " + printTime2 + "まで";
                    }
                    svf.VrsOut("PERIOD2_1", period2);
                }

                svf.VrEndPage();
                nonedata = false;
                count += 1;
            }
        } catch (final Exception e) {
            log.debug("prepareStatementでエラー", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     E1.NAME, ");
        stb.append("     E1.NAME_KANA, ");
        stb.append("     E1.EXAMNO, ");
        stb.append("     E1.APPLICANTDIV, ");
        stb.append("     E1.TESTDIV, ");
        stb.append("     N1.NAME1 AS TESTDIVNAME, ");
        stb.append("     N2.NAME3 AS SEX_NAME, ");
        stb.append("     N4.NAME1 AS SHDIV_NAME, ");
        stb.append("     E1.SEX, ");
        stb.append("     E3.TELNO, ");
        stb.append("     E3.ADDRESS1, ");
        stb.append("     E3.ADDRESS2, ");
        stb.append("     C1.SCHOOL_NAME, ");
        stb.append("     E5.BANKTRANSFERMONEY AS BANK_MONEY1, ");
        stb.append("     E5.BANKNAME AS BANK_NAME1, ");
        stb.append("     E5.BRANCHNAME AS BRANCH_NAME1, ");
        stb.append("     N5.ABBV1 AS DEPOSIT_KIND1, ");
        stb.append("     E5.ACCOUNTNAME AS RECEIPT_NAME1, ");
        stb.append("     E5.ACCOUNTNAME_KANA AS RECEIPT_NAME_KANA1, ");
        stb.append("     E5.ACCOUNTNO AS ACCOUNT_NAME1, ");
        stb.append("     E6.BANKNAME AS BANK_NAME2, ");
        stb.append("     E6.BRANCHNAME AS BRANCH_NAME2, ");
        stb.append("     N6.ABBV1 AS DEPOSIT_KIND2, ");
        stb.append("     E6.ACCOUNTNAME AS RECEIPT_NAME2, ");
        stb.append("     E6.ACCOUNTNAME_KANA AS RECEIPT_NAME_KANA2, ");
        stb.append("     E6.ACCOUNTNO AS ACCOUNT_NAME2 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT E1 ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT E2 ON E2.ENTEXAMYEAR  = E1.ENTEXAMYEAR ");
        stb.append("        AND E2.APPLICANTDIV = E1.APPLICANTDIV ");
        stb.append("        AND E2.TESTDIV = E1.TESTDIV ");
        stb.append("        AND E2.EXAMNO = E1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT E3 ON E3.ENTEXAMYEAR  = E1.ENTEXAMYEAR ");
        stb.append("        AND E3.EXAMNO = E1.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L004' AND N1.NAMECD2 = E1.TESTDIV ");
        stb.append("     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'Z002' AND N2.NAMECD2 = E1.SEX ");
        stb.append("     LEFT JOIN NAME_MST N4 ON N4.NAMECD1 = 'L006' AND N4.NAMECD2 = E1.SHDIV ");
        stb.append("     LEFT JOIN CERTIF_SCHOOL_DAT C1 ON C1.YEAR = E1.ENTEXAMYEAR ");
        if ("1".equals(_param._applicantdiv)) {
            stb.append("     AND C1.CERTIF_KINDCD = '105'  ");
        } else {
            stb.append("     AND C1.CERTIF_KINDCD = '106'  ");
        }
        stb.append("     LEFT JOIN SCHOOL_BANK_MST E5 ON ");
        if ("1".equals(_param._applicantdiv)) {
            stb.append("         E5.BANKTRANSFERCD = '1010' ");
        } else if (!_param.isGojo() || !"on".equals(_param._check)) {
            stb.append("         E5.BANKTRANSFERCD = '2010' ");
        } else {
            stb.append("         E5.BANKTRANSFERCD = '2011' ");
        }
        stb.append("     LEFT JOIN SCHOOL_BANK_MST E6 ON ");
        if ("1".equals(_param._applicantdiv)) {
            stb.append("         E6.BANKTRANSFERCD = '1020' ");
        } else {
            stb.append("         E6.BANKTRANSFERCD = '2020' ");
        }
        stb.append("     LEFT JOIN NAME_MST N5 ON N5.NAMECD1 = 'G203' AND N5.NAMECD2 = E5.DEPOSIT_ITEM ");
        stb.append("     LEFT JOIN NAME_MST N6 ON N6.NAMECD1 = 'G203' AND N6.NAMECD2 = E6.DEPOSIT_ITEM ");
        stb.append(" WHERE ");
        stb.append("         E1.ENTEXAMYEAR  = '"+ _param._year +"' ");
        stb.append("     AND E1.APPLICANTDIV = '"+ _param._applicantdiv +"' ");
        stb.append("     AND E1.TESTDIV in "+ SQLUtils.whereIn(true, _param._testdiv) +" ");
        stb.append("     AND E1.SEX = '"+ _param._sex +"' ");
        if ("1".equals(_param._print_type)) {
            stb.append("     AND E1.JUDGEMENT    = '1' ");
        } else if (_param._print_type.equals("4")) { //志願者全員
        } else {
            stb.append("     AND E2.ENTEXAMYEAR  = E1.ENTEXAMYEAR ");
            stb.append("     AND E2.APPLICANTDIV = E1.APPLICANTDIV ");
            stb.append("     AND E2.TESTDIV      = E1.TESTDIV ");
            stb.append("     AND E2.EXAMNO       = E1.EXAMNO ");
        }
        if ("3".equals(_param._print_type)) {
            stb.append("     AND E2.EXAMNO       = '"+ _param._examno +"' ");
            if (null != _param._goukakusha) {
                stb.append(" AND E1.JUDGEMENT = '1' ");
            }
        }
        if (_param.isGojo() && "2".equals(_param._applicantdiv)) {
            if ("on".equals(_param._check)) {
                stb.append("     AND E1.SHDIV = '2' ");
            } else {
                stb.append("     AND E1.SHDIV != '2' ");
            }
        }
        stb.append(" ORDER BY EXAMNO ");
        return stb.toString();
    }

    /** パラメータクラス */
    private static class Param {

        static final String KEY_TOTAL = "99";

        final String _prgid;
        final String _dbname;
        final String _year;
        final String _nendo;
        final String _login_date;
        final String _applicantdiv;
        final String[] _testdiv;
        final String _print_type;
        final String _examno;
        final String _goukakusha;
        final String _check;
        final String _sex;
        final boolean _seirekiFlg;
        private boolean _isTestdiv7;
        private boolean _isTestdiv6;

        final DecimalFormat _valueFormat;
        final Map _hankakuToZenkaku;;
        private Map _itemValueMan = Collections.EMPTY_MAP;
        private Map _itemValueWoman = Collections.EMPTY_MAP;

        final String _entDate;
        final String _strDate;
        final String _endDate;
        final String _printTime;
        String _strDate2;
        String _endDate2;
        String _printTime2;
        final String _z010SchoolCode;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _prgid = request.getParameter("PRGID");
            _dbname = request.getParameter("DBNAME");
            _year = request.getParameter("YEAR");
            _login_date = request.getParameter("LOGIN_DATE");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _z010SchoolCode = getSchoolCode(db2);
            final String temp_testDiv = request.getParameter("TESTDIV");
            _isTestdiv7 = false;
            _isTestdiv6 = false;
            if ("9".equals(temp_testDiv)) {
                if("1".equals(_applicantdiv)) {
                    _testdiv = new String[]{"1","2"};
                } else if("2".equals(_applicantdiv)) {
                    if (isGojo()) {
                        _testdiv = new String[]{"3","4","5","7"};
                        _isTestdiv7 = true;
                    } else {
                        _testdiv = new String[]{"3","4","5"};
                    }
                } else {
                    _testdiv = null;
                }
            } else {
                _testdiv = new String[]{temp_testDiv};
                if ("7".equals(temp_testDiv)) _isTestdiv7 = true;
                if ("6".equals(temp_testDiv)) _isTestdiv6 = true;
            }
            _print_type = request.getParameter("PRINT_TYPE");
            _examno = request.getParameter("EXAMNO");
            _goukakusha = request.getParameter("GOUKAKUSHA");
            _check = request.getParameter("CHECK"); // 入学金なし
            _sex = request.getParameter("SEX");

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

            _seirekiFlg = getSeirekiFlg(db2);
            _entDate = request.getParameter("ENT_DATE").replace('/', '-'); // 入学手続日
            _strDate = request.getParameter("STR_DATE").replace('/', '-'); // 手続開始日
            _endDate = getDateReplace(request.getParameter("END_DATE")); // 手続終了日
            _printTime = request.getParameter("PRINT_TIME");
            if (isGojo()) {
                //取扱期間(五条の制定学用品代)
                _strDate2 = getDateReplace(request.getParameter("STR_DATE2")); // 手続開始日
                _endDate2 = getDateReplace(request.getParameter("END_DATE2")); // 手続終了日
                _printTime2 = getTimeValue(request.getParameter("PRINT_TIME2"));
            }

            if (_seirekiFlg) {
                _nendo = _year + "年度";
            } else {
                final String yearDate = _year + "/01/01";
                _nendo = KNJ_EditDate.h_format_JP_N(yearDate) + "度";
            }
            loadItemValue(db2);
        }

        private String getDateReplace(final String date) {
            if ("".equals(date) || null == date) {
                return null;
            }
            return date.replace('/', '-');
        }

        private String getTimeValue(final String time) {
            if ("".equals(time) || null == time) {
                return null;
            }
            return time;
        }

        public String getValue(final String key, final String sex) {
            final String value;
            if ("2".equals(sex)) {
                value = (String) _itemValueWoman.get(key);
            } else {
                value = (String) _itemValueMan.get(key);
            }
            return value;
        }

        public String getValue(final int i, final String sex) {
            return getValue(i < 10 ? String.valueOf("0" + i) : String.valueOf(i), sex);
        }

        public String toZenkaku(final String num) {
            final StringBuffer sb = new StringBuffer();
            try {
                if (null != num) {
                    for (int i = 0; i < num.length(); i++) {
                        final String n = String.valueOf(num.charAt(i));
                        sb.append(null == (String) _hankakuToZenkaku.get(n) ? n : (String) _hankakuToZenkaku.get(n));
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return sb.toString();
        }

        public String valueFormat(final Long number) {
            return String.valueOf(number); //"¥" + toZenkaku(_valueFormat.format(number));
        }

        public void loadItemValue(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
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
                    if (null != lvalueMan) {
                        sumValueMan += lvalueMan.longValue();
//                        _itemValueMan.put(itemCd, valueFormat(lvalueMan));
                    }
                    if (null != lvalueWoman) {
                        sumValueWoman += lvalueWoman.longValue();
//                        _itemValueWoman.put(itemCd, valueFormat(lvalueWoman));
                    }
                }

            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if (0 != sumValueMan) {
                _itemValueMan.put(KEY_TOTAL, valueFormat(new Long(sumValueMan)));
            }
            if (0 != sumValueWoman) {
                _itemValueWoman.put(KEY_TOTAL, valueFormat(new Long(sumValueWoman)));
            }
        }

        private Long parseLong(final String value) {
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

        private boolean getSeirekiFlg(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            boolean seirekiFlg = false;
            try {
                final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if ("2".equals(rs.getString("NAME1"))) seirekiFlg = true; //西暦
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return seirekiFlg;
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
