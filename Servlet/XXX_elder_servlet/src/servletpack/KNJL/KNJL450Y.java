/*
 * $Id: b0789b599a09c7288b53029a52363ac5f241d12f $
 *
 * 作成日: 2010/11/09
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.math.BigInteger;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ４５０Ｙ＞  入試台帳
 **/
public class KNJL450Y {

    private static final Log log = LogFactory.getLog(KNJL450Y.class);

    private boolean _hasData;

    private Param _param;
    
    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");
            
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());
            
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
            
            _param = createParam(db2, request);
            
            _hasData = false;
            
            printMain(db2, svf);
            
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }
    
    private static List getMappedList(final Map map, final String key) {
        if (!map.containsKey(key)) {
            map.put(key, new ArrayList());
        }
        return (List) map.get(key);
    }
    
    private static int getMS932count(String str) {
    	return KNJ_EditEdit.getMS932ByteLength(str);
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final String frm = "KNJL450Y.frm";
        svf.VrSetForm(frm, 1);
        
        final int MAX_LINE = 25;

        final List<Applicant> applicantAllList = getApplicants(db2);
        
        final List<List<Applicant>> pageList = getPageList(applicantAllList, MAX_LINE);
        
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List<Applicant> applicantList = pageList.get(pi);

            svf.VrsOut("NENDO",    _param._entexamYear + "年度");
            svf.VrsOut("DATE",     _param._dateString);
            svf.VrsOut("TITLE",    _param._applicantDivName);
            svf.VrsOut("SUBTITLE", _param._testDivName);
            svf.VrsOut("PAGE1",    String.valueOf(pi + 1));
            svf.VrsOut("PAGE2",    String.valueOf(pageList.size()));
            
            for (int i = 0; i < applicantList.size(); i++) {
                final Applicant appl = applicantList.get(i);
                final int line = i + 1;
                
                if (appl._isNotFirst) {
                    svf.VrsOutn("REEXAM", line, "再");
                }
                final int nameLen = getMS932count(appl._name);
                final int nameKanaLen = getMS932count(appl._nameKana);
                final String nameField = "NAME" + (30 < nameLen ? "3" : 20 < nameLen ? "2" : "1");
                final String nameKanafield = "NAME_KANA" + (30 < nameKanaLen ? "3" : 20 < nameKanaLen ? "2" : "1");
                
                final String no = String.valueOf(MAX_LINE * pi + line);
                appl._no = no;
                svf.VrsOutn("NO", line,               no);
                svf.VrsOutn("EXAMNO", line,           appl._examno);
                svf.VrsOutn(nameField, line,          appl._name);
                svf.VrsOutn(nameKanafield, line,      appl._nameKana);
                svf.VrsOutn("SEX", line,              appl._sexName);
                int fsCnt = getMS932count(appl._fsName);
                final String fsSoeji = 36 < fsCnt ? "4_1" : 28 < fsCnt ? "3_1" : 14 < fsCnt ? "2_1" : "1";
                svf.VrsOutn("FINSCHOOL_NAME" + fsSoeji, line, appl._fsName);
                
                if ("2".equals(appl._procedureDiv)) {
                    svf.VrsOutn("JUDGE", line,            appl._procedureDivName);
                } else if ("2".equals(appl._entDiv)) {
                    svf.VrsOutn("JUDGE", line,            appl._entDivName);
                } else {
                    svf.VrsOutn("JUDGE", line,            appl._judgedivAbbv2); // 1文字
                }
                if ("1".equals(appl._judgediv)) {
                    if (null != appl._procedureDate) {
                        svf.VrsOutn("PAYMENT_DAY", line,      KNJ_EditDate.h_format_JP_MD(appl._procedureDate));
                    }
                    svf.VrsOutn("MONEY", line,            appl._payMoney);
                }
                
//                svf.VrsOutn("REMARK", line, null); // 備考
                
                _hasData = true;
            }
            
            if (pi == pageList.size() - 1) {
                final String OTHER_DATE = "9999-12-31";
                final Map<String, List<String>> payDatesMoneyListMap = new TreeMap<String, List<String>>();
                final Set paidExamnos = new TreeSet();
                
                for (int i = 0; i < applicantAllList.size(); i++) {
                    final Applicant appl = applicantAllList.get(i);

                    if (!paidExamnos.contains(appl._examno) && "1".equals(appl._namespare1)) {
                        if (null != appl._payMoney) {
                            final String date = StringUtils.defaultString(appl._procedureDate, OTHER_DATE);
                            getMappedList(payDatesMoneyListMap, date).add(appl._payMoney);
                        }
                        paidExamnos.add(appl._examno);
                    }
                }

                printFooter(svf, payDatesMoneyListMap, OTHER_DATE);
            }

            svf.VrEndPage();
        }
    }
    
    private static String sum(final List<String> list) {
        BigInteger sum = BigInteger.ZERO;
        for (final String n : list) {
            sum = sum.add(new BigInteger(n));
        }
        return sum.toString();
    }

    public void printFooter(final Vrw32alp svf, final Map<String, List<String>> payDates, final String OTHER_DATE) {
        final int MAX_MONEY_LINE = 2;
        int idx;
        idx = 1;
        final List<String> others = new ArrayList<String>();
        for (final Iterator<String> it = payDates.keySet().iterator(); it.hasNext();) {
            final String payDate = it.next();
            if (idx <= MAX_MONEY_LINE) {
            } else {
                // 日付の数がMAX_MONEY_LINE以上なら"上記以外"(ダミーOTHER_DATE)にまとめる
                others.addAll(getMappedList(payDates, payDate));
                it.remove();
            }
            idx += 1;
        }
        payDates.put(OTHER_DATE, others);
        idx = 1;
        final List<String> paidMoneyAllList = new ArrayList();
        for (final String payDate : payDates.keySet()) {
            final List<String> paidMoneyList = getMappedList(payDates, payDate);
            paidMoneyAllList.addAll(paidMoneyList);
            svf.VrsOutn("CONFIRM_DAY", idx, OTHER_DATE.equals(payDate) ? "上記以外" : KNJ_EditDate.h_format_JP_MD(payDate));
            svf.VrsOutn("TOTAL_MONEY", idx, sum(paidMoneyList));
            svf.VrsOutn("NUM", idx, String.valueOf(paidMoneyList.size()));
            idx += 1;
        }
        // 合計表示
        svf.VrsOutn("CONFIRM_DAY", idx, "計");
        svf.VrsOutn("TOTAL_MONEY", idx, sum(paidMoneyAllList));
        svf.VrsOutn("NUM", idx, String.valueOf(paidMoneyAllList.size()));
    }

    private List getApplicants(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        final List applicants = new ArrayList();
        try {
            final String sql = getApplicantSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                
                final String testdiv = rs.getString("TESTDIV");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String examno = rs.getString("EXAMNO");
                final String sexName = rs.getString("SEXNAME");
                final String fsName = rs.getString("FS_NAME");
                final String judgediv = rs.getString("JUDGEDIV");
                final String namespare1 = rs.getString("NAMESPARE1");
                final String judgedivAbbv1 = rs.getString("JUDGEDIV_ABBV1");
                final String judgedivAbbv2 = rs.getString("JUDGEDIV_ABBV2");
                final String payMoney = rs.getString("PAY_MONEY");
                final String procedureDate = rs.getString("PROCEDUREDATE");
                final String procedureDiv = rs.getString("PROCEDUREDIV");
                final String procedureDivName = rs.getString("PROCEDUREDIVNAME");
                final String entDiv = rs.getString("ENTDIV");
                final String entDivName = rs.getString("ENTDIVNAME");
                final boolean isNotFirst = "1".equals(rs.getString("IS_NOT_FIRST"));
                
                final Applicant applicant = new Applicant(testdiv, name, nameKana, examno, sexName, fsName, judgediv, namespare1,
                        judgedivAbbv1, judgedivAbbv2,
                        payMoney, procedureDate,
                        procedureDiv, procedureDivName, entDiv, entDivName, isNotFirst);
                applicants.add(applicant);
            }
            
        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return applicants;
    }

    private String getApplicantSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAIN AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     T5.NAME, ");
        stb.append("     T5.NAME_KANA, ");
        stb.append("     NMZ002.NAME2 AS SEXNAME, ");
        stb.append("     T5.FS_NAME, ");
        stb.append("     T1.JUDGEDIV, ");
        stb.append("     NML013.NAMESPARE1, ");
        stb.append("     NML013.ABBV1 AS JUDGEDIV_ABBV1, ");
        stb.append("     NML013.ABBV2 AS JUDGEDIV_ABBV2, ");
        stb.append("     T3.DESIREDIV, ");
        stb.append("     CASE WHEN NML013.NAMESPARE1 = '1' THEN T5.PAY_MONEY END AS PAY_MONEY, ");
        stb.append("     CASE WHEN NML013.NAMESPARE1 = '1' THEN T5.PROCEDUREDATE END AS PROCEDUREDATE, ");
        stb.append("     T5.PROCEDUREDIV, ");
        stb.append("     NML011.NAME1 AS PROCEDUREDIVNAME, ");
        stb.append("     T5.ENTDIV, ");
        stb.append("     NML012.NAME1 AS ENTDIVNAME ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T5 ON T5.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T5.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T5.EXAMNO = T1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTDESIRE_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T2.TESTDIV = T1.TESTDIV ");
        stb.append("         AND T2.EXAMNO = T1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST T3 ON T3.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("         AND T3.APPLICANTDIV = T2.APPLICANTDIV ");
        stb.append("         AND T3.TESTDIV = T2.TESTDIV ");
        stb.append("         AND T3.DESIREDIV = T2.DESIREDIV ");
        stb.append("         AND T3.WISHNO = '1' ");
        stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = T5.SEX ");
        stb.append("     LEFT JOIN NAME_MST NML011 ON NML011.NAMECD1 = 'L011' AND NML011.NAMECD2 = T5.PROCEDUREDIV ");
        stb.append("     LEFT JOIN NAME_MST NML012 ON NML012.NAMECD1 = 'L012' AND NML012.NAMECD2 = T5.ENTDIV ");
        stb.append("     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' AND NML013.NAMECD2 = T1.JUDGEDIV ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV <= '" + _param._testDiv + "' ");
        stb.append("     AND T1.EXAM_TYPE = '1' ");
        stb.append(" ), BEFORE AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("   T2.EXAMNO ");
        stb.append(" FROM ");
        stb.append("   MAIN T2 ");
        stb.append(" WHERE ");
        stb.append("   T2.TESTDIV < '" + _param._testDiv + "' ");
        stb.append("   AND EXISTS (SELECT 'X' FROM MAIN ");
        stb.append("                  WHERE EXAMNO = T2.EXAMNO AND TESTDIV = '" + _param._testDiv + "') ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T1.* ");
        stb.append("   ,CASE WHEN T2.EXAMNO IS NOT NULL THEN 1 ELSE 0 END AS IS_NOT_FIRST ");
        stb.append(" FROM ");
        stb.append("   MAIN T1 ");
        stb.append("   LEFT JOIN BEFORE T2 ON T2.EXAMNO = T1.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("   T1.TESTDIV = '" + _param._testDiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("     EXAMNO, TESTDIV ");

        return stb.toString();
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71458 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }
    
    private static <T> List<List<T>> getPageList(final List<T> list, final int count) {
        final List<List<T>> rtn = new ArrayList<List<T>>();
        List<T> current = null;
        for (final T o : list) {
            if (null == current || current.size() >= count) {
                current = new ArrayList<T>();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }
    
    private static class Applicant {
        String _no;
        final String _testdiv;
        final String _name;
        final String _nameKana;
        final String _examno;
        final String _sexName;
        final String _fsName;
        final String _judgediv;
        final String _namespare1;
        final String _judgedivAbbv1;
        final String _judgedivAbbv2;
        final String _payMoney;
        final String _procedureDate;
        final String _procedureDiv;
        final String _procedureDivName;
        final String _entDiv;
        final String _entDivName;
        final boolean _isNotFirst; // trueなら再受験者
        
        public Applicant(
                String testdiv,
                String name,
                String nameKana,
                String examno,
                String sexName,
                String fsName,
                String judgediv,
                String namespare1,
                String judgedivAbbv1,
                String judgedivAbbv2,
                String payMoney,
                String procedureDate,
                String procedureDiv,
                String procedureDivName,
                String entDiv,
                String entDivName,
                boolean isNotFirst) {
            _testdiv = testdiv;
            _name = name;
            _nameKana = nameKana;
            _examno = examno;
            _sexName = sexName;
            _fsName = fsName;
            _judgediv = judgediv;
            _namespare1 = namespare1;
            _judgedivAbbv1 = judgedivAbbv1;
            _judgedivAbbv2 = judgedivAbbv2;
            _payMoney = payMoney;
            _procedureDate = procedureDate;
            _procedureDiv = procedureDiv;
            _procedureDivName = procedureDivName;
            _entDiv = entDiv;
            _entDivName = entDivName;
            _isNotFirst = isNotFirst;
        }
    }
    
    
    /** パラメータクラス */
    private static class Param {
        final String _entexamYear;
        final String _applicantDiv;
        final String _testDiv;
        final String _applicantDivName;
        final String _testDivName;
        final String _dateString;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _applicantDivName = getApplicantDivName(db2);
            _testDivName = getTestDivName(db2);
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日");
            final Calendar cal = Calendar.getInstance();
            final String loginDate = request.getParameter("LOGIN_DATE");
            _dateString = sdf.format(Date.valueOf(loginDate)) + cal.get(Calendar.HOUR_OF_DAY) + "時" + cal.get(Calendar.MINUTE) + "分";
        }
        
        private String getApplicantDivName(DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '" + _applicantDiv + "'"));
        }
        
        private String getTestDivName(DB2UDB db2) {
            final String namecd1 = "L004";
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + _testDiv + "'"));
        }
    }
}

// eof
