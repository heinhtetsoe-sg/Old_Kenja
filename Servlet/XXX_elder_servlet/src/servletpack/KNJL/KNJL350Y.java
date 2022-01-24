/*
 * $Id: 394034bbfd643948888a074893347295690b524b $
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３５０Ｙ＞  入試台帳
 **/
public class KNJL350Y {

    private static final Log log = LogFactory.getLog(KNJL350Y.class);

    private boolean _hasData;

    Param _param;
    
    private final int MAX_LINE = 25;
    private Map _payDates;
    private Set _paidExamnos;

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
            
            _payDates = new TreeMap();
            _paidExamnos = new TreeSet();
            
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
    
    private List getPaidMoneyList(String payDate) {
        if (!_payDates.containsKey(payDate)) {
            _payDates.put(payDate, new ArrayList());
        }
        return (List) _payDates.get(payDate);
    }
    
    private int getMS932count(String str) {
        int count = 0;
        try {
            count = str.getBytes("MS932").length;
        } catch (Exception e) {
            log.error(e);
        }
        return count;
    }
    
    private int getMaxPage(final List applicants, final int MAX_LINE) {
        final int p = applicants.size() / MAX_LINE;
        return (applicants.size() % MAX_LINE == 0 ? p : p + 1);
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final String frm = "1".equals(_param._applicantDiv) ? "KNJL350Y_1.frm" : "KNJL350Y_2.frm";
        svf.VrSetForm(frm, 4);
        
        final List applicants = getApplicants(db2);
        try {
            final int maxPage = getMaxPage(applicants, MAX_LINE);
            int page = 1;
            int line = 1;
            int countTokusinChallenge = 0;
            for (int i = 0; i < applicants.size(); i++) {
                final Applicant applicant = (Applicant) applicants.get(i);
                
                svf.VrsOut("NENDO",    _param._entexamYear + "年度");
                svf.VrsOut("DATE",     _param._dateString);
                svf.VrsOut("TITLE",    _param._title);
                svf.VrsOut("SUBTITLE", _param._subTitle);
                svf.VrsOut("PAGE1",    String.valueOf(page));
                svf.VrsOut("PAGE2",    String.valueOf(maxPage));
                if (applicant._isNotFirst) {
                    svf.VrsOut("REEXAM", applicant._isTokusinChallenge ? "チ" : "再");
                    if (applicant._isTokusinChallenge) countTokusinChallenge++;
                }
                final int nameLen = getMS932count(applicant._name);
                final int nameKanaLen = getMS932count(applicant._nameKana);
                final String nameField = "NAME" + (30 < nameLen ? "3" : 20 < nameLen ? "2" : "1");
                final String nameKanafield = "NAME_KANA" + (50 < nameKanaLen ? "3" : 20 < nameKanaLen ? "2" : "1");
                
                final String no = String.valueOf(i+ 1);
                applicant._no = no;
                svf.VrsOut("NO",               no);
                svf.VrsOut("EXAMNO",           applicant._examno);
                svf.VrsOut(nameField,          applicant._name);
                svf.VrsOut(nameKanafield,      applicant._nameKana);
                svf.VrsOut("SEX",              applicant._sex);
                svf.VrsOut("FINSCHOOL_ABBV",   applicant._finschoolNameAbbv);
                svf.VrsOut("DESIRE",           applicant._shName);
                svf.VrsOut("EXAMCOURSE_NAME1", applicant._examcourseAbbv);
                final String passCourseName = ("3".equals(applicant._judgediv) || "5".equals(applicant._judgediv) && applicant._examcourseName2 != null) ? applicant._examcourseName2 : "1".equals(applicant._namespare1) ? applicant._examcourseName1 : "";
                svf.VrsOut("PASSCOURSE_NAME1", passCourseName);
                
                if ("1".equals(_param._applicantDiv)) {
                    svf.VrsOut("JUDGE",            applicant._judgedivAbbv2); // 1文字
                } else {
                    svf.VrsOut("JUDGE",            applicant._judgedivAbbv1); // 2文字
                }
                if ("2".equals(applicant._procedureDiv)) {
                    svf.VrsOut("JUDGE",            applicant._procedureDivName);
                }
                if ("2".equals(applicant._entDiv)) {
                    svf.VrsOut("JUDGE",            applicant._entDivName);
                }
                svf.VrsOut("SPECIAL",          applicant._judgeKindname);
                if (null != applicant._procedureDate) {
                    svf.VrsOut("PAYMENT_DAY",      KNJ_EditDate.h_format_JP_MD(applicant._procedureDate));
                }
                svf.VrsOut("MONEY",            applicant._payMoney);
                svf.VrsOut("SH_SCHOOL",        applicant._shSchoolAbbv);
                svf.VrsOut("DORMITORY",        applicant._dormitory);
                
                if (!_paidExamnos.contains(applicant._examno)) {
                    if (null != applicant._payMoney) {
                        if (null != applicant._procedureDate) {
                            getPaidMoneyList(applicant._procedureDate).add(applicant);
                        }
                        _paidExamnos.add(applicant._examno);
                    }
                }
                if (i == applicants.size() - 1) {
                    printSvfFooter(svf, countTokusinChallenge);
                }
                svf.VrEndRecord();
                
                _hasData = true;
                line += 1;
                if (MAX_LINE < line) {
                    page += 1;
                    line = 1;
                }
            }
//            log.debug(_payDates);
            
        } catch (Exception ex) {
            log.error("Exception:", ex);
        }
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
                final String sex = rs.getString("SEXNAME");
                final String finschoolNameAbbv = rs.getString("FINSCHOOL_NAME_ABBV");
                final String shName = rs.getString("SHNAME");
                final String judgediv = rs.getString("JUDGEDIV");
                final String namespare1 = rs.getString("NAMESPARE1");
                final String judgedivAbbv1 = rs.getString("JUDGEDIV_ABBV1");
                final String judgedivAbbv2 = rs.getString("JUDGEDIV_ABBV2");
                final String judgeKindname = rs.getString("JUDGE_KINDNAME");
                final String desireDiv = rs.getString("DESIREDIV");
                final String examcourseName1 = rs.getString("EXAMCOURSE_NAME1");
                final String examcourseName2 = rs.getString("EXAMCOURSE_NAME2");
                final String examcourseAbbv1 = rs.getString("EXAMCOURSE_ABBV1");
                final String examcourseAbbv2 = rs.getString("EXAMCOURSE_ABBV2");
                final String examcourseAbbv = (null == examcourseAbbv1 ? "" : examcourseAbbv1) + (null == examcourseAbbv2 ? "" : "・" + examcourseAbbv2);
                final String shSchoolAbbv = rs.getString("SHSCHOOL_NAME_ABBV");
                final String dormitory = "1".equals(rs.getString("DORMITORY_FLG")) ? "有" : "";
                final String payMoney = rs.getString("PAY_MONEY");
                final String procedureDate = rs.getString("PROCEDUREDATE");
                final String procedureDiv = rs.getString("PROCEDUREDIV");
                final String procedureDivName = rs.getString("PROCEDUREDIVNAME");
                final String entDiv = rs.getString("ENTDIV");
                final String entDivName = rs.getString("ENTDIVNAME");
                final boolean isNotFirst = "1".equals(rs.getString("IS_NOT_FIRST"));
                final boolean isTokusinChallenge = "1".equals(rs.getString("IS_TOKUSIN_CHALLENGE"));
                
                final Applicant applicant = new Applicant(testdiv, name, nameKana, examno, sex, finschoolNameAbbv, shName, judgediv, namespare1,
                        judgedivAbbv1, judgedivAbbv2, judgeKindname, desireDiv, examcourseName1, examcourseName2, examcourseAbbv,
                        shSchoolAbbv, dormitory, payMoney, procedureDate,
                        procedureDiv, procedureDivName, entDiv, entDivName, isNotFirst, isTokusinChallenge);
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

    private void printSvfFooter(final Vrw32alp svf, int countTokusinChallenge) {
        final String OTHER_DAYS = "9999-12-31";
        final int MAX_MONEY_LINE = 2;
        int idx = 1;
        final List otherDayMoneyList = new ArrayList();
        for (final Iterator it = _payDates.keySet().iterator(); it.hasNext();) {
            final String payDate = (String) it.next();
            if (idx <= MAX_MONEY_LINE) {
                idx += 1;
                continue;
            }
            final List paidMoneyList = getPaidMoneyList(payDate); // 日付の数がMAX_MONEY_LINE以上なら"上記以外"にまとめる
            if (0 != paidMoneyList.size()) {
                otherDayMoneyList.addAll(paidMoneyList);
            }
            it.remove();
            idx += 1;
        }
        if (0 != otherDayMoneyList.size()) {
            _payDates.put(OTHER_DAYS, otherDayMoneyList); // 日付の数がMAX_MONEY_LINE以上なら"上記以外"を表示する
        }
        idx = 1;
        int applCount = 0;                              // 合計人数
        BigInteger totalTotalMoney = BigInteger.ZERO;   // 合計金額
        for (final Iterator it = _payDates.keySet().iterator(); it.hasNext();) {
            final String payDate = (String) it.next();
            final List paidMoneyList = getPaidMoneyList(payDate);
            applCount += paidMoneyList.size();
            BigInteger totalMoney = BigInteger.ZERO;
            for (final Iterator itm = paidMoneyList.iterator(); itm.hasNext();) {
                final Applicant applicant = (Applicant) itm.next();
                final BigInteger money = new BigInteger(applicant._payMoney);
                totalMoney = totalMoney.add(money);
            }
            svf.VrsOutn("CONFIRM_DAY", idx, OTHER_DAYS.equals(payDate) ? "上記以外" : KNJ_EditDate.h_format_JP_MD(payDate));
            svf.VrsOutn("TOTAL_MONEY", idx, totalMoney.toString());
            svf.VrsOutn("NUM", idx, String.valueOf(paidMoneyList.size()));
            totalTotalMoney = totalTotalMoney.add(totalMoney);
            idx += 1;
        }
        // 合計表示
        svf.VrsOutn("CONFIRM_DAY", idx, "計");
        svf.VrsOutn("TOTAL_MONEY", idx, totalTotalMoney.toString());
        svf.VrsOutn("NUM", idx, String.valueOf(applCount));
        // 合計表示（左下）
        if ("2".equals(_param._applicantDiv) && "3".equals(_param._testDiv)) {
            svf.VrsOut("C_CONFIRM_DAY", "特進再チャレンジ");
            svf.VrsOut("C_TOTAL_MONEY", String.valueOf(countTokusinChallenge * 5000));
            svf.VrsOut("C_NUM", String.valueOf(countTokusinChallenge));
        }
    }
    
    private String getApplicantSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH PASS_GAKUTOKU_IPPAN AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT T1 ");
        stb.append("     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' AND NML013.NAMECD2 = T1.JUDGEDIV ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '2' ");
        stb.append("     AND T1.TESTDIV IN ('1','3') ");
        stb.append("     AND NML013.NAMESPARE1 = '1' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.EXAMNO ");
        stb.append(" HAVING ");
        stb.append("     2 = COUNT(*) ");
        stb.append(" ), MAIN AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     T5.NAME, ");
        stb.append("     T5.NAME_KANA, ");
        stb.append("     NMZ002.NAME2 AS SEXNAME, ");
        stb.append("     T7.FINSCHOOL_NAME_ABBV, ");
        stb.append("     NML006.NAME1 AS SHNAME, ");
        stb.append("     T1.JUDGEDIV, ");
        stb.append("     NML013.NAMESPARE1, ");
        stb.append("     NML013.ABBV1 AS JUDGEDIV_ABBV1, ");
        stb.append("     NML013.ABBV2 AS JUDGEDIV_ABBV2, ");
        stb.append("     NML025.NAME1 AS JUDGE_KINDNAME, ");
        stb.append("     T3.DESIREDIV, ");
        stb.append("     T4.EXAMCOURSE_NAME AS EXAMCOURSE_NAME1, ");
        stb.append("     T4.EXAMCOURSE_ABBV AS EXAMCOURSE_ABBV1, ");
        stb.append("     CASE WHEN T5.TESTDIV = '" + _param._testDiv + "' AND VALUE(T5.SLIDE_FLG, '') = '1' THEN T9.EXAMCOURSE_NAME END AS EXAMCOURSE_NAME2, ");
        stb.append("     CASE WHEN T5.TESTDIV = '" + _param._testDiv + "' AND VALUE(T5.SLIDE_FLG, '') = '1' THEN T9.EXAMCOURSE_ABBV END AS EXAMCOURSE_ABBV2, ");
        stb.append("     T6.FINSCHOOL_NAME_ABBV AS SHSCHOOL_NAME_ABBV, ");
        stb.append("     T5.DORMITORY_FLG, ");
        stb.append("     T5.SELECT_SUBCLASS_DIV AS IS_TOKUSIN_CHALLENGE, ");
        //学特入試で合格後、一般入試で合格した受験者の入金情報は、学特入試の出力では表示しない。
        if ("2".equals(_param._applicantDiv) && "1".equals(_param._testDiv)) {
            stb.append("     CASE WHEN NML013.NAMESPARE1 = '1' AND PASS_GI.EXAMNO IS NULL THEN T5.PAY_MONEY END AS PAY_MONEY, ");
            stb.append("     CASE WHEN NML013.NAMESPARE1 = '1' AND PASS_GI.EXAMNO IS NULL THEN T5.PROCEDUREDATE END AS PROCEDUREDATE, ");
        } else {
            stb.append("     CASE WHEN NML013.NAMESPARE1 = '1' THEN T5.PAY_MONEY END AS PAY_MONEY, ");
            stb.append("     CASE WHEN NML013.NAMESPARE1 = '1' THEN T5.PROCEDUREDATE END AS PROCEDUREDATE, ");
        }
        stb.append("     T5.PROCEDUREDIV, ");
        stb.append("     NML011.NAME1 AS PROCEDUREDIVNAME, ");
        stb.append("     T5.ENTDIV, ");
        stb.append("     NML012.NAME1 AS ENTDIVNAME ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT T1 ");
        stb.append("     LEFT JOIN PASS_GAKUTOKU_IPPAN PASS_GI ON PASS_GI.EXAMNO = T1.EXAMNO ");
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
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T4 ON T4.ENTEXAMYEAR = T3.ENTEXAMYEAR ");
        stb.append("         AND T4.APPLICANTDIV = T3.APPLICANTDIV ");
        stb.append("         AND T4.TESTDIV = T3.TESTDIV ");
        stb.append("         AND T4.COURSECD = T3.COURSECD ");
        stb.append("         AND T4.MAJORCD = T3.MAJORCD ");
        stb.append("         AND T4.EXAMCOURSECD = T3.EXAMCOURSECD ");
        stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST T8 ON T8.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("         AND T8.APPLICANTDIV = T2.APPLICANTDIV ");
        stb.append("         AND T8.TESTDIV = T2.TESTDIV ");
        stb.append("         AND T8.DESIREDIV = T2.DESIREDIV ");
        stb.append("         AND T8.WISHNO = '2' ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T9 ON T9.ENTEXAMYEAR = T8.ENTEXAMYEAR ");
        stb.append("         AND T9.APPLICANTDIV = T8.APPLICANTDIV ");
        stb.append("         AND T9.TESTDIV = T8.TESTDIV ");
        stb.append("         AND T9.COURSECD = T8.COURSECD ");
        stb.append("         AND T9.MAJORCD = T8.MAJORCD ");
        stb.append("         AND T9.EXAMCOURSECD = T8.EXAMCOURSECD ");
        stb.append("     LEFT JOIN FINHIGHSCHOOL_MST T6 ON T6.FINSCHOOLCD = T5.SH_SCHOOLCD ");
        stb.append("     LEFT JOIN FINSCHOOL_MST T7 ON T7.FINSCHOOLCD = T5.FS_CD ");
        stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = T5.SEX ");
        stb.append("     LEFT JOIN NAME_MST NML006 ON NML006.NAMECD1 = 'L006' AND NML006.NAMECD2 = T2.SHDIV ");
        stb.append("     LEFT JOIN NAME_MST NML011 ON NML011.NAMECD1 = 'L011' AND NML011.NAMECD2 = T5.PROCEDUREDIV ");
        stb.append("     LEFT JOIN NAME_MST NML012 ON NML012.NAMECD1 = 'L012' AND NML012.NAMECD2 = T5.ENTDIV ");
        stb.append("     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' AND NML013.NAMECD2 = T1.JUDGEDIV ");
        stb.append("     LEFT JOIN NAME_MST NML025 ON NML025.NAMECD1 = 'L025' AND NML025.NAMECD2 = T5.JUDGE_KIND ");
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
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }
    
    private static class Applicant {
        String _no;
        final String _testdiv;
        final String _name;
        final String _nameKana;
        final String _examno;
        final String _sex;
        final String _finschoolNameAbbv;
        final String _shName;
        final String _judgediv;
        final String _namespare1;
        final String _judgedivAbbv1;
        final String _judgedivAbbv2;
        final String _judgeKindname;
        final String _desireDiv;
        final String _examcourseName1;
        final String _examcourseName2;
        final String _examcourseAbbv;
        final String _shSchoolAbbv;
        final String _dormitory;
        final String _payMoney;
        final String _procedureDate;
        final String _procedureDiv;
        final String _procedureDivName;
        final String _entDiv;
        final String _entDivName;
        final boolean _isNotFirst; // trueなら再受験者
        final boolean _isTokusinChallenge; // trueなら特進再チャレンジ受験者
        
        public Applicant(
                String testdiv,
                String name,
                String nameKana,
                String examno,
                String sex,
                String finschoolNameAbbv,
                String shName,
                String judgediv,
                String namespare1,
                String judgedivAbbv1,
                String judgedivAbbv2,
                String judgeKindname,
                String desireDiv,
                String examcourseName1,
                String examcourseName2,
                String examcourseAbbv,
                String shSchoolAbbv,
                String dormitory,
                String payMoney,
                String procedureDate,
                String procedureDiv,
                String procedureDivName,
                String entDiv,
                String entDivName,
                boolean isNotFirst,
                boolean isTokusinChallenge) {
            _testdiv = testdiv;
            _name = name;
            _nameKana = nameKana;
            _examno = examno;
            _sex = sex;
            _finschoolNameAbbv = finschoolNameAbbv;
            _shName = shName;
            _judgediv = judgediv;
            _namespare1 = namespare1;
            _judgedivAbbv1 = judgedivAbbv1;
            _judgedivAbbv2 = judgedivAbbv2;
            _judgeKindname = judgeKindname;
            _desireDiv = desireDiv;
            _examcourseName1 = examcourseName1;
            _examcourseName2 = examcourseName2;
            _examcourseAbbv = examcourseAbbv;
            _shSchoolAbbv = shSchoolAbbv;
            _dormitory = dormitory;
            _payMoney = payMoney;
            _procedureDate = procedureDate;
            _procedureDiv = procedureDiv;
            _procedureDivName = procedureDivName;
            _entDiv = entDiv;
            _entDivName = entDivName;
            _isNotFirst = isNotFirst;
            _isTokusinChallenge = isTokusinChallenge;
        }
    }
    
    
    /** パラメータクラス */
    private class Param {
        final String _entexamYear;
        final String _applicantDiv;
        final String _testDiv;
        final String _title;
        final String _subTitle;
        final String _dateString;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _title = getSchoolName(db2);
            _subTitle = getTestDivName(db2);
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日");
            final Calendar cal = Calendar.getInstance();
            final String loginDate = request.getParameter("LOGIN_DATE");
            _dateString = sdf.format(Date.valueOf(loginDate)) + cal.get(Calendar.HOUR_OF_DAY) + "時" + cal.get(Calendar.MINUTE) + "分";
        }
        
        private String getSchoolName(DB2UDB db2) {
            String schoolName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '" + _applicantDiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  schoolName = rs.getString("NAME1"); 
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolName;
        }
        
        private String getTestDivName(DB2UDB db2) {
            String testDivName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String namecd1 = "1".equals(_applicantDiv) ? "L024" : "L004";
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + _testDiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  testDivName = rs.getString("NAME1"); 
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testDivName;
        }
    }
}

// eof
