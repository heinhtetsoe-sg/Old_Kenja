/*
 * $Id: 9baf00369dd48d3f6a619124e038fa51874b4697 $
 *
 * 作成日: 2013/10/10
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３５０Ｎ＞  入学試験受験者入試台帳
 **/
public class KNJL350N {

    private static final Log log = LogFactory.getLog(KNJL350N.class);

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
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }
    
    private static int getMS932ByteLength(final String s) {
        int rtn = 0;
        if (null != s) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch (Exception e) {
                log.debug("exception!", e);
            }
        }
        return rtn;
    }
    
    public void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final String form = "KNJL350N.frm";
        final int maxLine = 25;

        final List allApplicantList = Applicant.getApplicantList(db2, _param);
        
        final List pageList = getPageList(allApplicantList, maxLine);
        
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List applicantList = (List) pageList.get(pi);
            
            svf.VrSetForm(form, 1);
            
            svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度"); // 年度
            svf.VrsOut("PAGE1", String.valueOf(pi + 1)); // ページ分子
            svf.VrsOut("PAGE2", String.valueOf(pageList.size())); // ページ分母
            svf.VrsOut("TITLE", "　高等学校"); // 
            svf.VrsOut("SUBTITLE", _param._testdivAbbv1); // サブタイトル
            svf.VrsOut("DATE", _param._dateStr); // 印刷日時
//            svf.VrsOut("C_CONFIRM_DAY", null); // 特進再チャレンジ
//            svf.VrsOut("C_TOTAL_MONEY", null); // 合計金額（特進再チャレンジ）
//            svf.VrsOut("C_NUM", null); // 人数（特進再チャレンジ）
            
            for (int j = 0; j < applicantList.size(); j++) {
                final Applicant appl = (Applicant) applicantList.get(j);
                final int line = j + 1;
                //svf.VrsOutn("REEXAM", line, null); // 再受験
                svf.VrsOutn("NO", line, appl._printN); // 番号
                svf.VrsOutn("EXAMNO", line, appl._examno); // 受験番号
                final int nameKeta = getMS932ByteLength(appl._name);
                svf.VrsOutn("NAME" + (nameKeta <= 20 ? "1" : nameKeta <= 30 ? "2" : "3"), line, appl._name); // 氏名
                final int nameKanaKeta = getMS932ByteLength(appl._nameKana);
                svf.VrsOutn("NAME_KANA" + (nameKanaKeta <= 20 ? "1" : nameKanaKeta <= 50 ? "2" : "3"), line, appl._nameKana); // かな氏名
                svf.VrsOutn("SEX", line, appl._sexName); // 性別
                svf.VrsOutn("FINSCHOOL_ABBV", line, appl._finschoolNameAbbv); // 出身学校略称
                final int examcourseNameKeta = getMS932ByteLength(appl._examcourseName);
                svf.VrsOutn("EXAMCOURSE_NAME" + (examcourseNameKeta <= 10 ? "1" : examcourseNameKeta <= 16 ? "2" : "3"), line, appl._examcourseName); // 志望コース1
                final int sucExamcourseNameKeta = getMS932ByteLength(appl._sucExamcourseName);
                svf.VrsOutn("PASSCOURSE_NAME" + (sucExamcourseNameKeta <= 10 ? "1" : sucExamcourseNameKeta <= 16 ? "2" : "3"), line, appl._sucExamcourseName); // 志望コース1
                svf.VrsOutn("JUDGE" + (getMS932ByteLength(appl._judgedivName) <= 4 ? "1" : "2"), line, appl._judgedivName); // 合否
                final int remark8NameKeta = getMS932ByteLength(appl._remark8Name);
                svf.VrsOutn("SPECIAL" + (remark8NameKeta < 6 ? "1" : ""), line, appl._remark8Name); // 奨学生
                svf.VrsOutn("PAYMENT_DAY", line, KNJ_EditDate.h_format_JP_MD(appl._proceduredate)); // 入金日
                svf.VrsOutn("MONEY", line, appl._payMoney); // 金額
                //svf.VrsOutn("REMARK", line, null); // 備考
            }

            if (pi == pageList.size() - 1) {
                printTotalMoney(svf, allApplicantList);
            }

            svf.VrEndPage();
            _hasData = true;
        }
        
    }

    private void printTotalMoney(final Vrw32alp svf, final List applicantList) {
        final Map dateMoneyListMap = new TreeMap();
        for (int ai = 0; ai < applicantList.size(); ai++) {
            final Applicant appl = (Applicant) applicantList.get(ai);
            if (null != appl._payMoney) {
                final String date = StringUtils.defaultString(appl._proceduredate, "9999-12-31");
                if (null == dateMoneyListMap.get(date)) {
                    dateMoneyListMap.put(date, new ArrayList());
                }
                ((List) dateMoneyListMap.get(date)).add(appl._payMoney);
            }
        }

        int count = 1;
        final List otherDateMoneyList = new ArrayList();
        final List totalMoneyList = new ArrayList();
        for (final Iterator it = dateMoneyListMap.entrySet().iterator(); it.hasNext();) {
            final Map.Entry e = (Map.Entry) it.next();
            final String date = (String) e.getKey();
            final List moneyList = (List) e.getValue();
            totalMoneyList.addAll(moneyList);
            if (count >= 3) {
                otherDateMoneyList.addAll(moneyList);
            } else {
                final String sum = sum(moneyList);
                if (null != sum) {
                    if (!"9999-12-31".equals(date)) {
                        svf.VrsOutn("CONFIRM_DAY", count, KNJ_EditDate.h_format_JP_MD(date)); // 入金確認日
                    }
                    svf.VrsOutn("TOTAL_MONEY", count, sum); // 合計金額
                    svf.VrsOutn("NUM", count, String.valueOf(moneyList.size())); // 人数
                    count += 1;
                }
            }
        }
        if (otherDateMoneyList.size() > 0) {
            final String sum = sum(otherDateMoneyList);
            if (null != sum) {
                svf.VrsOutn("CONFIRM_DAY", count, "上記以外"); // 入金確認日
                svf.VrsOutn("TOTAL_MONEY", count, sum); // 合計金額
                svf.VrsOutn("NUM", count, String.valueOf(otherDateMoneyList.size())); // 人数
                count += 1;
            }
        }

        final String sum = sum(totalMoneyList);
        if (null != sum) {
            svf.VrsOutn("CONFIRM_DAY", count, "計"); // 入金確認日
            svf.VrsOutn("TOTAL_MONEY", count, sum); // 合計金額
            svf.VrsOutn("NUM", count, String.valueOf(totalMoneyList.size())); // 人数
            count += 1;
        }
    }
    
    private String sum(final List moneyList) {
        Long sum = null;
        for (final Iterator it = moneyList.iterator(); it.hasNext();) {
            final String money = (String) it.next();
            if (NumberUtils.isDigits(money)) {
                if (null == sum) {
                    sum = new Long(0);
                }
                sum = new Long(sum.longValue() + Long.parseLong(money));
            }
        }
        return null == sum ? null : sum.toString();
    }

    private static List getPageList(final List list, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        int n = 1;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Applicant o = (Applicant) it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
            o._printN = String.valueOf(n);
            n += 1;
        }
        return rtn;
    }
    
    private static class Applicant {
        String _printN;
        final String _examno;
        final String _name;
        final String _nameKana;
        final String _sex;
        final String _sexName;
        final String _finschoolNameAbbv;
        final String _examcourseName;
        final String _sucExamcourseName;
        final String _judgedivName;
        final String _remark8Name;
        final String _proceduredate;
        final String _payMoney;

        Applicant(
            final String examno,
            final String name,
            final String nameKana,
            final String sex,
            final String sexName,
            final String finschoolName,
            final String examcourseName,
            final String sucExamcourseName,
            final String judgedivName,
            final String remark8Name,
            final String proceduredate,
            final String payMoney
        ) {
            _examno = examno;
            _name = name;
            _nameKana = nameKana;
            _sex = sex;
            _sexName = sexName;
            _finschoolNameAbbv = finschoolName;
            _examcourseName = examcourseName;
            _sucExamcourseName = sucExamcourseName;
            _judgedivName = judgedivName;
            _remark8Name = remark8Name;
            _proceduredate = proceduredate;
            _payMoney = payMoney;
        }

        public static List getApplicantList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String examno = rs.getString("EXAMNO");
                    final String name = rs.getString("NAME");
                    final String nameKana = rs.getString("NAME_KANA");
                    final String sex = rs.getString("SEX");
                    final String sexName = rs.getString("SEX_NAME");
                    final String finschoolNameAbbv = rs.getString("FINSCHOOL_NAME_ABBV");
                    final String examcourseName = rs.getString("EXAMCOURSE_NAME");
                    final String sucExamcourseName = rs.getString("SUC_EXAMCOURSE_NAME");
                    final String judgedivName = rs.getString("JUDGEDIV_NAME");
                    final String remark8Name = rs.getString("REMARK8_NAME");
                    final String proceduredate = rs.getString("PROCEDUREDATE");
                    final String payMoney = rs.getString("PAY_MONEY");
                    
                    final Applicant applicant = new Applicant(examno, name, nameKana, sex, sexName, finschoolNameAbbv, examcourseName, sucExamcourseName, judgedivName, remark8Name, proceduredate, payMoney);
                    list.add(applicant);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  ");
            stb.append("     BASE.EXAMNO,  ");
            stb.append("     BASE.NAME,  ");
            stb.append("     BASE.NAME_KANA,  ");
            stb.append("     BASE.SEX,  ");
            stb.append("     NMZ002.NAME2 AS SEX_NAME,  ");
            stb.append("     FIN.FINSCHOOL_NAME_ABBV,  ");
            stb.append("     CRS1.EXAMCOURSE_NAME,  ");
            stb.append("     CRS2.EXAMCOURSE_NAME AS SUC_EXAMCOURSE_NAME,  ");
            stb.append("     CASE WHEN BASE.PROCEDUREDIV = '2' THEN NML011.NAME1  ");
            stb.append("          WHEN BASE.ENTDIV = '2' THEN NML012.NAME1  ");
            stb.append("          ELSE NML013.NAME1  ");
            stb.append("     END AS JUDGEDIV_NAME,  ");
            stb.append("     NML025.NAME1 AS REMARK8_NAME,  ");
            stb.append("     BASE.PROCEDUREDATE,  ");
            stb.append("     BASE.PAY_MONEY  ");
            stb.append("  FROM ENTEXAM_APPLICANTBASE_DAT BASE  ");
            stb.append("  INNER JOIN ENTEXAM_RECEPT_DAT RCPT ON BASE.ENTEXAMYEAR = RCPT.ENTEXAMYEAR  ");
            stb.append("      AND BASE.APPLICANTDIV = RCPT.APPLICANTDIV  ");
            stb.append("      AND BASE.TESTDIV = RCPT.TESTDIV  ");
            stb.append("      AND RCPT.EXAM_TYPE = '1'  ");
            stb.append("      AND BASE.EXAMNO = RCPT.EXAMNO  ");
            stb.append("  LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL1 ON BDETAIL1.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("      AND BDETAIL1.EXAMNO = BASE.EXAMNO  ");
            stb.append("      AND BDETAIL1.SEQ = '001'   ");
            stb.append("  LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL9 ON BDETAIL9.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("      AND BDETAIL9.EXAMNO = BASE.EXAMNO  ");
            stb.append("      AND BDETAIL9.SEQ = '009'   ");
            stb.append("  LEFT JOIN ENTEXAM_COURSE_MST CRS1 ON CRS1.ENTEXAMYEAR = BASE.ENTEXAMYEAR   ");
            stb.append("      AND CRS1.APPLICANTDIV = BASE.APPLICANTDIV  ");
            stb.append("      AND CRS1.TESTDIV = BASE.TESTDIV  ");
            stb.append("      AND CRS1.COURSECD = BDETAIL1.REMARK8  ");
            stb.append("      AND CRS1.MAJORCD = BDETAIL1.REMARK9  ");
            stb.append("      AND CRS1.EXAMCOURSECD = BDETAIL1.REMARK10   ");
            stb.append("  LEFT JOIN ENTEXAM_COURSE_MST CRS2 ON CRS2.ENTEXAMYEAR = BASE.ENTEXAMYEAR   ");
            stb.append("      AND CRS2.APPLICANTDIV = BASE.APPLICANTDIV  ");
            stb.append("      AND CRS2.TESTDIV = BASE.TESTDIV  ");
            stb.append("      AND CRS2.COURSECD = BASE.SUC_COURSECD  ");
            stb.append("      AND CRS2.MAJORCD = BASE.SUC_MAJORCD  ");
            stb.append("      AND CRS2.EXAMCOURSECD = BASE.SUC_COURSECODE  ");
            stb.append("  LEFT JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD = BASE.FS_CD  ");
            stb.append("  LEFT JOIN NAME_MST NMZ002 ON 'Z002' = NMZ002.NAMECD1  ");
            stb.append("      AND BASE.SEX = NMZ002.NAMECD2  ");
            stb.append("  LEFT JOIN NAME_MST NML013 ON 'L013' = NML013.NAMECD1  ");
            stb.append("      AND BASE.JUDGEMENT = NML013.NAMECD2  ");
            stb.append("  LEFT JOIN NAME_MST NML011 ON 'L011' = NML011.NAMECD1  ");
            stb.append("      AND BASE.PROCEDUREDIV = NML011.NAMECD2  ");
            stb.append("  LEFT JOIN NAME_MST NML012 ON 'L012' = NML012.NAMECD1  ");
            stb.append("      AND BASE.ENTDIV = NML012.NAMECD2  ");
            stb.append("  LEFT JOIN NAME_MST NML025 ON 'L025' = NML025.NAMECD1  ");
            stb.append("      AND BDETAIL9.REMARK8 = NML025.NAMECD2  ");
            stb.append(" WHERE  ");
            stb.append("     BASE.ENTEXAMYEAR = '" + param._entexamyear + "'  ");
            stb.append("     AND BASE.APPLICANTDIV = '" + param._applicantdiv + "'   ");
            stb.append("     AND BASE.TESTDIV = '" + param._testdiv + "'  ");
            stb.append("     AND VALUE(NML013.NAMESPARE1, '') = '1'  ");
            stb.append("  ORDER BY  ");
            stb.append("     BASE.EXAMNO  ");
            return stb.toString();
        }
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _date;
        
        final String _applicantdivAbbv1;
        final String _testdivAbbv1;
        final String _dateStr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _date = request.getParameter("CTRL_DATE").replace('/', '-');
            _dateStr = getDateStr(_date);
            _applicantdivAbbv1 = getNameMst(db2, "ABBV1", "L003", _applicantdiv);
            _testdivAbbv1 = getNameMst(db2, "ABBV1", "L004", _testdiv);
        }
        

        private String getDateStr(final String date) {
            final Calendar cal = Calendar.getInstance();
            final DecimalFormat df = new DecimalFormat("00");
            final int hour = cal.get(Calendar.HOUR_OF_DAY);
            final int minute = cal.get(Calendar.MINUTE);
            return KNJ_EditDate.h_format_JP(date) + "　" + df.format(hour) + "時" + df.format(minute) + "分現在";
        }

        private static String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}

// eof

