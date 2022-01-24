/*
 * $Id: f479d4d2f7183f0ba4666861221f28569498dffe $
 *
 * 作成日: 2010/11/01
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ４２１Ｙ＞  合否判定資料
 **/
public class KNJL421Y {
    
    private static final Log log = LogFactory.getLog(KNJL421Y.class);
    
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
    
    private static int getMS932count(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }
    
    private static String formatDate(final String date) {
        if (null == date) {
            return null;
        }
        return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date) ;
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final int MAX_LINE = 20;
        final String form = "KNJL421Y.frm";
        
        final List applicantAllList = Applicant.getApplicantList(db2, _param);
        
        final List pageList = getPageList(applicantAllList, MAX_LINE);

        final List subclasscdList = new ArrayList(_param._subclassCdNameMap.keySet());
        
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List applicantList = (List) pageList.get(pi);
            svf.VrSetForm(form, 1);
            
            svf.VrsOut("NENDO", _param._entexamYear + "年度"); // 年度
            svf.VrsOut("TITLE", StringUtils.defaultString(_param._applicantDivName)); // 
            svf.VrsOut("SUBTITLE", _param._testDivName); // サブタイトル
            svf.VrsOut("PAGE1", String.valueOf(pi + 1)); // ページ分子
            svf.VrsOut("PAGE2", String.valueOf(pageList.size())); // ページ分母
            svf.VrsOut("DATE", formatDate(_param._loginDate)); // 印刷日時
            for (int i = 0; i < subclasscdList.size(); i++) {
                svf.VrsOut("SUBJECT" + String.valueOf(i + 1), (String) _param._subclassCdNameMap.get(subclasscdList.get(i))); // 科目
                
                if (_param._amikakeSubclasscdList.contains(String.valueOf(subclasscdList.get(i)))) {
                    svf.VrAttribute("SUBJECT" + String.valueOf(i + 1), "Paint=(1,70,2),Bold=1"); // 科目
                }
            }

            for (int j = 0; j < applicantList.size(); j++) {
                final Applicant appl = (Applicant) applicantList.get(j);
                final int line = j + 1;

                svf.VrsOutn("NO", line, String.valueOf(pi * MAX_LINE + line)); // 番号
                svf.VrsOutn("EXAMNO", line, appl._examno); // 受験番号

                final int groupNameKeta = getMS932count(appl._examhallName);
                if (groupNameKeta > 8 * 1) {
                    final String[] token;
                    final String field;
                    if (groupNameKeta > 8 * 2) {
                        token = KNJ_EditEdit.get_token(appl._examhallName, 8, 3);
                        field = "GROUP_NAME3_";
                    } else {
                        token = KNJ_EditEdit.get_token(appl._examhallName, 8, 2);
                        field = "GROUP_NAME2_";
                    }
                    if (null != token) {
                        for (int i = 0; i < token.length; i++) {
                            svf.VrsOutn(field + String.valueOf(i + 1), line, token[i]); // グループ名
                        }
                    }
                } else {
                    svf.VrsOutn("GROUP_NAME1", line, appl._examhallName); // グループ名
                }
                svf.VrsOutn("GROUPNO", line, appl._examhallcdReceptnoOrder); // グループ内番号
                
                final int nameKeta = getMS932count(appl._name);
                svf.VrsOutn("NAME" + (nameKeta > 30 ? "3" : nameKeta > 20 ? "2" : nameKeta > 14 ? "1" : "0"), line, appl._name); // 氏名1

                svf.VrsOutn("SEX", line, appl._sexname); // 性別
                svf.VrsOutn("BIRTHDAY", line, null == appl._birthday ? null : appl._birthday.replace('-', '.')); // 生年月日
                
                for (int i = 0; i < subclasscdList.size(); i++) {
                    final String score = (String) appl._scoreMap.get(subclasscdList.get(i).toString());
                    final String attendFlg = (String) appl._attendFlgMap.get(subclasscdList.get(i).toString());
                    svf.VrsOutn("SCORE" + String.valueOf(i + 1), line, "0".equals(attendFlg) ? "*" : score); // 点数
                }

                svf.VrsOutn("SUBTOTAL", line, "1".equals(_param._output) ? appl._total2 : "2".equals(_param._output) ? appl._total1 : null); // 小計
                svf.VrsOutn("VAL", line, appl._interviewValue2Namespare1); // 評価
                svf.VrsOutn("TOTAL", line, "1".equals(_param._output) ? appl._total4 : "2".equals(_param._output) ? appl._total3 : null); // 合計

                final String[] tokenIntvRemark2 = KNJ_EditEdit.get_token(appl._interviewRemark2, 34, 3);
                if (null != tokenIntvRemark2) {
                    for (int i = 0; i < tokenIntvRemark2.length; i++) {
                        svf.VrsOutn("REMARK2_" + String.valueOf(i + 1), line, tokenIntvRemark2[i]); // 行動観察備考
                    }
                }

                svf.VrsOutn("LAST_TOTAL", line, appl._interviewValueName1); //
                
                final String[] tokenIntvRemark = KNJ_EditEdit.get_token(appl._interviewRemark, 34, 3);
                if (null != tokenIntvRemark) {
                    for (int i = 0; i < tokenIntvRemark.length; i++) {
                        svf.VrsOutn("REMARK3_" + String.valueOf(i + 1), line, tokenIntvRemark[i]); // 行動観察備考
                    }
                }

                svf.VrsOutn("JUDGE", line, appl._judgedivabbv2); // 合否
            }
            
            svf.VrEndPage();
            _hasData = true;
        }
    }
    
    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }
    
    private static class Applicant {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _examno;
        final String _receptno;
        final String _name;
        final String _sexname;
        final String _judgediv;
        final String _judgedivabbv2;
        String _birthday;
        String _interviewValue;
        String _interviewValueName1;
        String _interviewValueNamespare1;
        String _interviewRemark;
        String _interviewValue2;
        String _interviewValue2Namespare1;
        String _interviewRemark2;
        String _total1;
        String _total2;
        String _total3;
        String _total4;
        String _examhallName;
        String _examhallcdReceptnoOrder;
        final Map _scoreMap = new HashMap();
        final Map _attendFlgMap = new HashMap();

        Applicant(
            final String entexamyear,
            final String applicantdiv,
            final String testdiv,
            final String examno,
            final String receptno,
            final String name,
            final String sexname,
            final String judgediv,
            final String judgedivabbv2
        ) {
            _entexamyear = entexamyear;
            _applicantdiv = applicantdiv;
            _testdiv = testdiv;
            _examno = examno;
            _receptno = receptno;
            _name = name;
            _sexname = sexname;
            _judgediv = judgediv;
            _judgedivabbv2 = judgedivabbv2;
        }

        public static List getApplicantList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map receptnoMap = new HashMap();
            try {
                final String sql = sql(param);
                //log.info(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String entexamyear = rs.getString("ENTEXAMYEAR");
                    final String applicantdiv = rs.getString("APPLICANTDIV");
                    final String testdiv = rs.getString("TESTDIV");
                    final String examno = rs.getString("EXAMNO");
                    final String receptno = rs.getString("RECEPTNO");
                    final String name = rs.getString("NAME");
                    final String sexname = rs.getString("SEXNAME");
                    final String judgediv = rs.getString("JUDGEDIV");
                    final String judgedivabbv2 = rs.getString("JUDGEDIVABBV2");
                    final Applicant applicant = new Applicant(entexamyear, applicantdiv, testdiv, examno, receptno, name, sexname, judgediv, judgedivabbv2);
                    applicant._birthday = rs.getString("BIRTHDAY");
                    applicant._interviewValue = rs.getString("INTERVIEW_VALUE");
                    applicant._interviewValueName1 = rs.getString("INTERVIEW_VALUE_NAME1");
                    applicant._interviewValueNamespare1 = rs.getString("INTERVIEW_VALUE_NAMESPARE1");
                    applicant._interviewRemark = rs.getString("INTERVIEW_REMARK");
                    applicant._interviewValue2 = rs.getString("INTERVIEW_VALUE2");
                    applicant._interviewValue2Namespare1 = rs.getString("INTERVIEW_VALUE2_NAMESPARE1");
                    applicant._interviewRemark2 = rs.getString("INTERVIEW_REMARK2");
                    applicant._total1 = rs.getString("TOTAL1");
                    applicant._total2 = rs.getString("TOTAL2");
                    applicant._total3 = rs.getString("TOTAL3");
                    applicant._total4 = rs.getString("TOTAL4");
                    applicant._examhallName = rs.getString("EXAMHALL_NAME");
                    applicant._examhallcdReceptnoOrder = rs.getString("EXAMHALLCD_RECEPTNO_ORDER");
                    
                    list.add(applicant);
                    receptnoMap.put(receptno, applicant);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            try {
                final String sql = getScoreSql(param);
//                 log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    final String testsubclasscd = rs.getString("TESTSUBCLASSCD");
                    final String score = rs.getString("SCORE");
                    final String attendFlg = rs.getString("ATTEND_FLG");
                    Applicant applicant = (Applicant) receptnoMap.get(rs.getString("RECEPTNO"));
                    if (null == applicant) {
                        continue;
                    }
                    applicant._scoreMap.put(testsubclasscd, score);
                    applicant._attendFlgMap.put(testsubclasscd, attendFlg);
                }
            } catch (SQLException ex) {
                log.error("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return list;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  ");
            stb.append("     T1.ENTEXAMYEAR,  ");
            stb.append("     T1.APPLICANTDIV,  ");
            stb.append("     T2.TESTDIV,  ");
            stb.append("     T1.EXAMNO,  ");
            stb.append("     T2.RECEPTNO,  ");
            stb.append("     T1.NAME,  ");
            stb.append("     T1.BIRTHDAY, ");
            stb.append("     NMZ002.NAME2 AS SEXNAME,  ");
            stb.append("     T2.JUDGEDIV,  ");
            stb.append("     NML013.ABBV2 AS JUDGEDIVABBV2,  ");
            stb.append("     INTV.INTERVIEW_VALUE, ");
            stb.append("     NML027.NAME1 AS INTERVIEW_VALUE_NAME1, ");
            stb.append("     NML027.NAMESPARE1 AS INTERVIEW_VALUE_NAMESPARE1, ");
            stb.append("     INTV.INTERVIEW_REMARK, ");
            stb.append("     INTV.INTERVIEW_VALUE2,  ");
            stb.append("     NML030.NAMESPARE1 AS INTERVIEW_VALUE2_NAMESPARE1, ");
            stb.append("     INTV.INTERVIEW_REMARK2,  ");
            stb.append("     T2.TOTAL1, ");
            stb.append("     T2.TOTAL2, ");
            stb.append("     T2.TOTAL3, ");
            stb.append("     T2.TOTAL4, ");
            stb.append("     HY.EXAMHALL_NAME, ");
            stb.append("     ROW_NUMBER() OVER(PARTITION BY HY.EXAMHALLCD ORDER BY T2.RECEPTNO) AS EXAMHALLCD_RECEPTNO_ORDER ");
            stb.append(" FROM  ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT T1  ");
            stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR  ");
            stb.append("         AND T2.APPLICANTDIV = T1.APPLICANTDIV  ");
            stb.append("         AND T2.EXAMNO = T1.EXAMNO  ");
            stb.append("         AND T2.EXAM_TYPE = '1'  ");
            stb.append("     LEFT JOIN ENTEXAM_HALL_LIST_YDAT HLY ON T1.ENTEXAMYEAR = HLY.ENTEXAMYEAR ");
            stb.append("        AND T2.APPLICANTDIV = HLY.APPLICANTDIV ");
            stb.append("        AND T2.TESTDIV = HLY.TESTDIV ");
            stb.append("        AND T2.EXAM_TYPE = HLY.EXAM_TYPE ");
            stb.append("        AND T2.RECEPTNO = HLY.RECEPTNO ");
            stb.append("     LEFT JOIN ENTEXAM_HALL_YDAT HY ON HLY.ENTEXAMYEAR = HY.ENTEXAMYEAR ");
            stb.append("        AND HLY.APPLICANTDIV = HY.APPLICANTDIV ");
            stb.append("        AND HLY.TESTDIV = HY.TESTDIV ");
            stb.append("        AND HLY.EXAMHALLCD = HY.EXAMHALLCD ");
            stb.append("        AND HLY.EXAM_TYPE = HY.EXAM_TYPE ");
            stb.append("     LEFT JOIN ENTEXAM_INTERVIEW_DAT INTV ON INTV.ENTEXAMYEAR = T1.ENTEXAMYEAR  ");
            stb.append("         AND INTV.APPLICANTDIV = T1.APPLICANTDIV  ");
            stb.append("         AND INTV.TESTDIV = T2.TESTDIV  ");
            stb.append("         AND INTV.EXAMNO = T1.EXAMNO  ");
            stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = T1.SEX  ");
            stb.append("     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' AND NML013.NAMECD2 = T2.JUDGEDIV  ");
            stb.append("     LEFT JOIN NAME_MST NML027 ON NML027.NAMECD1 = 'L027' AND NML027.NAMECD2 = INTV.INTERVIEW_VALUE ");
            stb.append("     LEFT JOIN NAME_MST NML030 ON NML030.NAMECD1 = 'L030' AND NML030.NAMECD2 = INTV.INTERVIEW_VALUE2 ");
            stb.append(" WHERE  ");
            stb.append("     T1.ENTEXAMYEAR = '" + param._entexamYear + "'  ");
            stb.append("     AND T1.APPLICANTDIV = '" + param._applicantDiv + "'  ");
            stb.append("     AND T2.TESTDIV = '" + param._testDiv + "'  ");
            stb.append(" ORDER BY  ");
            if ("2".equals(param._output_sort)) {
                stb.append("     T1.EXAMNO  ");
            } else {
                if ("2".equals(param._output)) {
                    stb.append("     VALUE(T2.TOTAL3, -1) DESC,  ");                    
                    stb.append("     VALUE(T2.TOTAL1, -1) DESC,  ");                    
                } else {
                    stb.append("     VALUE(T2.TOTAL4, -1) DESC,  ");                    
                    stb.append("     VALUE(T2.TOTAL2, -1) DESC,  ");                    
                }
                stb.append("     T1.EXAMNO  "); 
            }
            return stb.toString();
        }
        
        private static String getScoreSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T3.RECEPTNO, ");
            stb.append("     T3.TESTSUBCLASSCD, ");
            stb.append("     T3.SCORE, ");
            stb.append("     T3.ATTEND_FLG ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_SCORE_DAT T3 ");
            stb.append(" WHERE ");
            stb.append("     T3.ENTEXAMYEAR = '" + param._entexamYear + "' ");
            stb.append("     AND T3.APPLICANTDIV = '" + param._applicantDiv + "' ");
            stb.append("     AND T3.TESTDIV = '" + param._testDiv + "' ");
            stb.append("     AND T3.EXAM_TYPE = '1' ");
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
    private class Param {
        final String _entexamYear;
        final String _applicantDiv;
        final String _testDiv;
        final String _output; // 傾斜配点出力 1:する 2:しない
        final String _output_sort; // 出力順 1:成績順 2:受験番号順
        final String _applicantDivName;
        final String _testDivName;
        final String _loginDate;
        final String _dateString;
        final Map _subclassCdNameMap;
        final List _amikakeSubclasscdList;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _output = request.getParameter("OUTPUT");
            _output_sort = request.getParameter("OUTPUT_SORT");
            _applicantDivName = getApplicantDivName(db2);
            _testDivName = getTestDivName(db2);
            _subclassCdNameMap = getSubclassnameMap(db2);
            final Calendar cal = Calendar.getInstance();
            _loginDate = request.getParameter("LOGIN_DATE");
            _dateString = new SimpleDateFormat("yyyy年M月d日").format(Date.valueOf(_loginDate)) + cal.get(Calendar.HOUR_OF_DAY) + "時" + cal.get(Calendar.MINUTE) + "分";
            
            if ("1".equals(_output)) {
                _amikakeSubclasscdList = getAmikakeSubclassCdList(db2);
            } else {
                _amikakeSubclasscdList = Collections.EMPTY_LIST;
            }
        }
        
        private List getAmikakeSubclassCdList(DB2UDB db2) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "";
                sql += " SELECT TESTSUBCLASSCD FROM ENTEXAM_PERFECT_MST ";
                sql += " WHERE ENTEXAMYEAR = '" + _entexamYear + "' ";
                sql += "   AND APPLICANTDIV = '" + _applicantDiv + "' ";
                sql += "   AND TESTDIV = '" + _testDiv + "' ";
                sql += "   AND COURSECD = '0' ";
                sql += "   AND MAJORCD = '000' ";
                sql += "   AND EXAMCOURSECD = '0000' ";
                sql += "   AND VALUE(RATE,100) <> 100 ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                  list.add(rs.getString("TESTSUBCLASSCD")); 
                }
            } catch (SQLException e) {
                log.fatal("exceptioN!", e);
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        
        private Map getSubclassnameMap(DB2UDB db2) {
            Map rtn = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _entexamYear + "' AND NAMECD1 = 'L009' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (!NumberUtils.isDigits(rs.getString("NAMECD2"))) {
                        continue;
                    }
                    rtn.put(Integer.valueOf(rs.getString("NAMECD2")), rs.getString("NAME1"));
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
        
        private String getApplicantDivName(DB2UDB db2) {
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
                log.fatal("exceptioN!", e);
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolName;
        }
        
        private String getTestDivName(DB2UDB db2) {
            String testDivName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String namecd1 = "L004";
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + _testDiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  testDivName = rs.getString("NAME1"); 
                }
            } catch (SQLException e) {
                log.fatal("exceptioN!", e);
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testDivName;
        }
    }
}

// eof
