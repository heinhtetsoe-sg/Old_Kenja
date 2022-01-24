/*
 * $Id: 34647d0526a3019a1145c2ca42b6d9001b6fe2b0 $
 *
 * 作成日: 2010/11/09
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ４２５Ｙ＞  判定結果一覧
 **/
public class KNJL425Y {

    private static final Log log = LogFactory.getLog(KNJL425Y.class);

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
        try {
            count = str.getBytes("MS932").length;
        } catch (Exception e) {
            log.error(e);
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
        
        final String form = "KNJL425Y.frm";
        
        final int maxLine = 51;
        final Map printDivApplicantListMap = Applicant.getPrintDivApplicantListMap(db2, _param);

        boolean addBlank = false;
        final List printLineAllList = new ArrayList();

        addBlank = setPrintDiv(printLineAllList, (List) printDivApplicantListMap.get("1"), "合格者", addBlank);
        addBlank = setPrintDiv(printLineAllList, (List) printDivApplicantListMap.get("2"), "不合格者", addBlank);
        addBlank = setPrintDiv(printLineAllList, (List) printDivApplicantListMap.get("3"), "欠席", addBlank);
        
        final List pageList = getPageList(printLineAllList, maxLine);
        
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List printLineList = (List) pageList.get(pi);
            
            svf.VrSetForm(form, 4);
            
            svf.VrsOut("NENDO", _param._entexamYear + "年度"); // 年度
            svf.VrsOut("DATE", formatDate(_param._loginDate)); // 
            svf.VrsOut("TITLE", _param._applicantDivName); // 
            svf.VrsOut("SUBTITLE", "（" + StringUtils.defaultString(_param._testDivName) + "）"); // サブタイトル
            svf.VrsOut("PAGE1", String.valueOf(pi + 1)); // ページ分子
            svf.VrsOut("PAGE2", String.valueOf(pageList.size())); // ページ分母

            for (int j = 0; j < printLineList.size(); j++) {
                final Object line = printLineList.get(j);
                
                if (line instanceof Applicant) {
                    final Applicant appl = (Applicant) line;
                    svf.VrsOut("SEATNO", appl._no); // 座席番号
                    svf.VrsOut("JUDGE", appl._judgedivAbbv); // 判定
                    svf.VrsOut("EXAMNO", appl._examno); // 受験番号
                    final int nameKeta = getMS932count(appl._name);
                    svf.VrsOut("NAME" + (nameKeta > 30 ? "3" : nameKeta > 20 ? "2" : "1"), appl._name); // 氏名
                    final int nameKanaKeta = getMS932count(appl._nameKana);
                    svf.VrsOut("NAME_KANA" + (nameKanaKeta > 30 ? "3" : nameKanaKeta > 20 ? "2" : "1"), appl._nameKana); // かな氏名
                    svf.VrsOut("SEX", appl._sexname); // 性別
                    final int fsNameKeta = getMS932count(appl._fsName);
                    svf.VrsOut("FINSCHOOL_ABBV" + (fsNameKeta > 36 ? "4" : fsNameKeta > 24 ? "3" : fsNameKeta > 20 ? "2" : ""), appl._fsName); // 出身学校略称
                    //svf.VrsOut("REMARK", null); // 備考
                    svf.VrEndRecord();

                } else if ("BLANK".equals(line)) {
                    // 空行
                    svf.VrsOut("COURSE", "DUMMY"); // コース
                    svf.VrAttribute("COURSE", "X=10000"); // コース
                    svf.VrEndRecord();

                } else if ("HEADER".equals(line)) {
                    // 項目のヘッダ
                    svf.VrsOut("HEADER_SEATNO", "No."); // ヘッダNO
                    svf.VrsOut("HEADER_JUDGE", "判定"); // ヘッダ判定
                    svf.VrsOut("HEADER_EXAMNO", "受験番号"); // ヘッダ受験番号
                    svf.VrsOut("HEADER_NAME1", "氏名"); // ヘッダ氏名
                    svf.VrsOut("HEADER_NAME_KANA1", "氏名かな"); // ヘッダかな
                    svf.VrsOut("HEADER_SEX", "性別"); // ヘッダ性別
                    svf.VrsOut("HEADER_FINSCHOOL_ABBV", "出身幼稚園等"); // ヘッダ出身学校名
                    svf.VrsOut("HEADER_REMARK", "備考"); // ヘッダ備考
                    svf.VrEndRecord();

                } else if (line instanceof String) {
                    // 表示ブロックのタイトル
                    svf.VrsOut("COURSE", (String) line); // コース
                    //svf.VrsOut("COURSE_HEADER", null); // コースヘッダ表示用
                    svf.VrEndRecord();

                }
                
                _hasData = true;
            }
        }
    }

    private boolean setPrintDiv(final List printLineList, final List applicantList, final String title, final boolean addBlank) {
        if (null == applicantList) {
            return addBlank;
        }
        if (addBlank) {
            printLineList.add("BLANK");
        }

        printLineList.add(title + "（" + String.valueOf(applicantList.size()) + "名）");
        printLineList.add("HEADER");
        printLineList.addAll(applicantList);

        return true;
    }

    private static List getPageList(final List list, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }
    
    private static class Applicant {
        String _no;
        final String _examno;
        final String _name;
        final String _nameKana;
        final String _fsName;
        final String _sexname;
        final String _judgediv;
        final String _namespare1;
        final String _judgedivName;
        final String _judgedivAbbv;

        Applicant(
            final String examno,
            final String name,
            final String nameKana,
            final String fsName,
            final String sexname,
            final String judgediv,
            final String namespare1,
            final String judgedivName,
            final String judgedivAbbv
        ) {
            _examno = examno;
            _name = name;
            _nameKana = nameKana;
            _fsName = fsName;
            _sexname = sexname;
            _judgediv = judgediv;
            _namespare1 = namespare1;
            _judgedivName = judgedivName;
            _judgedivAbbv = judgedivAbbv;
        }
        
        public String toString() {
            return "Applicant(" + _examno + ":" + _name + ")";
        }

        public static Map getPrintDivApplicantListMap(final DB2UDB db2, final Param param) {
            final Map printDivApplicantListMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String oldPrintDiv = null;
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int no = 0;
                while (rs.next()) {
                    final String printDiv = rs.getString("PRINT_DIV");
                    if (null == oldPrintDiv || !oldPrintDiv.equals(printDiv)) {
                        no = 0;
                    }
                    no += 1;
                    
                    final String examno = rs.getString("EXAMNO");
                    final String name = rs.getString("NAME");
                    final String nameKana = rs.getString("NAME_KANA");
                    final String fsName = rs.getString("FS_NAME");
                    final String sexname = rs.getString("SEXNAME");
                    final String judgediv = rs.getString("JUDGEDIV");
                    final String namespare1 = rs.getString("NAMESPARE1");
                    final String judgedivName = rs.getString("JUDGEDIV_NAME");
                    final String judgedivAbbv = rs.getString("JUDGEDIV_ABBV");
                    final Applicant applicant = new Applicant(examno, name, nameKana, fsName, sexname, judgediv, namespare1, judgedivName, judgedivAbbv);
                    if (null == printDivApplicantListMap.get(printDiv)) {
                        printDivApplicantListMap.put(printDiv, new ArrayList());
                    }
                    ((List) printDivApplicantListMap.get(printDiv)).add(applicant);
                    applicant._no = String.valueOf(no);
                    
                    oldPrintDiv = printDiv;
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return printDivApplicantListMap;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  ");
            stb.append("      CASE WHEN RCPT.JUDGEDIV IN ('1')      THEN 1 ");
            stb.append("           WHEN RCPT.JUDGEDIV IN ('2', '3') THEN 2 ");
            stb.append("           WHEN RCPT.JUDGEDIV IN ('4')      THEN 3 ");
            stb.append("           ELSE 999 ");
            stb.append("      END AS PRINT_DIV, ");
            stb.append("      BASE.EXAMNO  ");
            stb.append("      ,BASE.NAME  ");
            stb.append("      ,BASE.NAME_KANA  ");
            stb.append("      ,BASE.FS_NAME  ");
            stb.append("      ,NMZ002.NAME2 AS SEXNAME  ");
            stb.append("      ,RCPT.JUDGEDIV  ");
            stb.append("      ,NML013.NAMESPARE1  ");
            stb.append("      ,NML013.NAME2 AS JUDGEDIV_NAME  ");
            stb.append("      ,NML013.ABBV1 AS JUDGEDIV_ABBV  ");
            stb.append("  FROM  ");
            stb.append("      ENTEXAM_APPLICANTBASE_DAT BASE  ");
            stb.append("      INNER JOIN ENTEXAM_RECEPT_DAT RCPT ON RCPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("          AND RCPT.APPLICANTDIV = BASE.APPLICANTDIV  ");
            stb.append("          AND RCPT.EXAM_TYPE = '1'  ");
            stb.append("          AND RCPT.EXAMNO = BASE.EXAMNO  ");
            stb.append("      LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' AND NML013.NAMECD2 = RCPT.JUDGEDIV  ");
            stb.append("      LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = BASE.SEX  ");
            stb.append("  WHERE  ");
            stb.append("      BASE.ENTEXAMYEAR = '" + param._entexamYear + "'  ");
            stb.append("      AND BASE.APPLICANTDIV = '" + param._applicantDiv + "'  ");
            stb.append("      AND RCPT.TESTDIV = '" + param._testDiv + "'  ");
            stb.append("  ORDER BY  ");
            stb.append("      CASE WHEN RCPT.JUDGEDIV IN ('1')      THEN 1 ");
            stb.append("           WHEN RCPT.JUDGEDIV IN ('2', '3') THEN 2 ");
            stb.append("           WHEN RCPT.JUDGEDIV IN ('4')      THEN 3 ");
            stb.append("           ELSE 999 ");
            stb.append("      END, ");
            stb.append("      BASE.EXAMNO  ");
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
        final String _applicantDivName;
        final String _testDivName;
        final String _loginDate;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");

            _applicantDivName = getApplicantdivName(db2);
            _testDivName = getTestDivName(db2);
            _loginDate = request.getParameter("LOGIN_DATE");
        }
        
        private String getApplicantdivName(DB2UDB db2) {
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
            final String namecd1 = "L004";
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

