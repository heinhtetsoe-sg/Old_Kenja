/*
 * $Id: 85db86d5f850b176fa5be9e89ea2b34e1e3403de $
 *
 * 作成日: 2010/11/09
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.io.File;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ４２６Ｙ＞  各種通知書
 **/
public class KNJL426Y {

    private static final Log log = LogFactory.getLog(KNJL426Y.class);

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
    
    private int getMS932count(String str) {
        int count = 0;
        try {
            count = str.getBytes("MS932").length;
        } catch (Exception e) {
            log.error(e);
        }
        return count;
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final List applicants = getApplicants(db2);
        
        if ("1".equals(_param._form)) { // 通知
            String date = "";
            if (!StringUtils.isBlank(_param._ndate)) {
                date = new SimpleDateFormat("yyyy年M月d日").format(Date.valueOf(_param._ndate));
            }

            String simeDate = "";
            if (!StringUtils.isBlank(_param._simeDate)) {
                final SimpleDateFormat sdf2 = new SimpleDateFormat("M月d日");
                final Date date1 = Date.valueOf(_param._simeDate.replace('/', '-'));
                final Calendar cal = Calendar.getInstance();
                cal.setTime(date1);
                final int week = cal.get(Calendar.DAY_OF_WEEK);
                simeDate = sdf2.format(date1) + "（" + (0 <= week ? new String[]{null, "日", "月", "火", "水", "木", "金", "土"}[week] : "") + "）";
            }

            for (final Iterator it = applicants.iterator(); it.hasNext(); ) {
                final Applicant appl = (Applicant) it.next();
                
                String form = null;
                if ("1".equals(appl._namespare1)) {
                    form = "KNJL426Y_1.frm"; // 合格通知
                } else if ("2".equals(appl._judgediv)) {
                    if ("4".equals(_param._output1)) {
                        form = "?"; // 再受験
                    } else {
                        form = "KNJL426Y_2.frm"; // 不合格通知
                    }
                }
                if (null == form) {
                    continue;
                }
                
                svf.VrSetForm(form, 1);
                
                svf.VrsOut("DATE", date); // 証明日付
                svf.VrsOut("EXAMNO", toZenkaku(appl._examno)); // 受験番号
                svf.VrsOut("NAME", appl._name); // 氏名
                if ("1".equals(_param._check_inei) && null != _param._schoolStampFile) {
                    svf.VrsOut("SCHOOLSTAMP", _param._schoolStampFile.getPath()); // 
                }
                svf.VrsOut("STAFFNAME", StringUtils.defaultString(_param._certifSchoolJobName) + " " + StringUtils.defaultString(_param._certifSchoolPrincipalName)); // 職員名
                svf.VrsOut("NENDO", _param._entexamYear + "年度"); // 年度
                svf.VrsOut("TESTDIV", _param._testdivName); // 入試区分
                svf.VrsOut("SCHOOLNAME", _param._certifSchoolSchoolName); // 学校名
                
//                if ("1".equals(appl._namespare1)) {
//                    
//                    svf.VrsOut("TEXT1", null); // 入試区分
//                    svf.VrsOut("TEXT2", null); // 本文2
//
//                } else if ("2".equals(appl._judgediv)) {
//
//                    svf.VrsOut("TEXT1", null); // 入試区分
//                    svf.VrsOut("TEXT2", null); // 本文2
//                    svf.VrsOut("TEXT3", null); // 本文3
//                }

                svf.VrEndPage();

                _hasData = true;
            }
        } else if ("2".equals(_param._form)) { // ラベル
            
            final String form = "KNJL426Y_3.frm";
            svf.VrSetForm(form, 1);
            
            final int maxLine = 5;
            final int maxCol = 2;
            int line = 1;
            int col = 1;

            for (final Iterator it = applicants.iterator(); it.hasNext(); ) {
                final Applicant appl = (Applicant) it.next();

                if (col > maxCol) {
                    line += 1;
                    if (line > maxLine) {
                        svf.VrEndPage();
                        svf.VrSetForm(form, 1);
                        line = 1;
                    }
                    col = 1;
                }
                
                final String scol = String.valueOf(col);
                svf.VrsOutn("ZIPCODE" + scol, line, appl._zipcd); // 郵便番号
                
                final int addr1Keta = getMS932count(appl._address1);
                final int addr2Keta = getMS932count(appl._address2);
                String addrField = "";
                if (addr1Keta > 40 || addr2Keta > 40) {
                    addrField = "_4";
                } else if (addr1Keta > 30 || addr2Keta > 30) {
                    addrField = "_3";
                } else if (addr1Keta > 26 || addr2Keta > 26) {
                    addrField = "_2";
                } else {
                    addrField = "";
                }
                svf.VrsOutn("ADDRESS1_" + scol + addrField, line, appl._address1); // 住所
                svf.VrsOutn("ADDRESS2_" + scol + addrField, line, appl._address2); // 住所

                svf.VrsOutn("EXAM_NO" + (col == 2 ? "2" : ""), line, appl._examno); // 受験番号
                
                final String printName = StringUtils.defaultString(appl._name) + "　様";
                final int nameKeta = getMS932count(printName);
                svf.VrsOutn("NAME" + scol + "_" + (nameKeta > 18 ? "3" : nameKeta > 14 ? "2" : "1"), line, printName); // 氏名

                col += 1;
                _hasData = true;
            }

            if (_hasData) {
                svf.VrEndPage();
            }
        }
    }
    
    private static String toZenkaku(final String s) {
        if (null == s) {
            return s;
        }
        final StringBuffer stb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if ('0' <= c && c <= '9') {
                stb.append((char) (c - '0' + '０'));
            } else {
                stb.append(c);
            }
        }
        return stb.toString();
    }

    private List getApplicants(final DB2UDB db2) {
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        final List applicants = new ArrayList();
        try {
            final String sql = getApplicantSql();
            //log.info(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String judgediv = rs.getString("JUDGEDIV");
                final String judgedivName = rs.getString("JUDGEDIV_NAME");
                final String namespare1 = rs.getString("NAMESPARE1");
                
                final Applicant applicant = new Applicant(examno, name, judgediv, judgedivName, namespare1);
                applicant._zipcd = rs.getString("ZIPCD");
                applicant._address1 = rs.getString("ADDRESS1");
                applicant._address2 = rs.getString("ADDRESS2");
                
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
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO ");
        stb.append("     ,T3.NAME ");
        stb.append("     ,T1.JUDGEDIV ");
        stb.append("     ,NML013.NAMESPARE1 ");
        stb.append("     ,NML013.NAME1 AS JUDGEDIV_NAME ");
        stb.append("     ,ADDR.ZIPCD ");
        stb.append("     ,ADDR.ADDRESS1 ");
        stb.append("     ,ADDR.ADDRESS2 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT T1 ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T3.EXAMNO = T1.EXAMNO ");
        stb.append("     INNER JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' AND NML013.NAMECD2 = T3.JUDGEMENT ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ON ADDR.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND ADDR.EXAMNO = T1.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND T1.EXAM_TYPE = '1' ");
        if ("1".equals(_param._output1)) {
            stb.append("     AND T3.JUDGEMENT IN ('1', '2', '3') ");
        } else if ("2".equals(_param._output1)) {
            stb.append("     AND NML013.NAMESPARE1 = '1' ");
        } else if ("3".equals(_param._output1)) {
            stb.append("     AND T3.JUDGEMENT = '2' ");
        } else if ("4".equals(_param._output1)) {
            stb.append("     AND T3.JUDGEMENT = '2' ");
        } else {
            // 
            stb.append("     AND 0 <> 0 ");
        }
        if (null != _param._examno) {
            stb.append("     AND T1.EXAMNO = '" + _param._examno + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.EXAMNO ");
        return stb.toString();
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }
    
    private static class Applicant {
        final String _name;
        final String _examno;
        final String _judgediv;
        final String _judgedivname;
        final String _namespare1;
        String _zipcd;
        String _address1;
        String _address2;
        public Applicant(
                final String examno, 
                final String name,
                final String judgediv,
                final String judgedivName,
                final String namespare1) {
            _name = name;
            _examno = examno;
            _judgediv = judgediv;
            _judgedivname = judgedivName;
            _namespare1 = namespare1;
        }
    }
    
    
    /** パラメータクラス */
    private static class Param {
        final String _entexamYear;
        final String _applicantDiv;
        final String _testDiv;
        final String _form; // 1:通知 2:ラベル
        final String _ndate; // 通知日付
        final String _simeDate; // 締め切り日付
        final String _output1;      // 出力範囲 1:全て 2:合格者 3:不合格者 4:再受験
        final String _check_inei;

        final File _schoolStampFile;
        final String _applicantDivName;
        final String _testdivName;
        final String _certifSchoolSchoolName;
        final String _certifSchoolJobName;
        final String _certifSchoolPrincipalName;
        final String _examno;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _form = request.getParameter("FORM");
            _ndate = request.getParameter("NDATE").replace('/', '-');
            _simeDate = request.getParameter("SIME_DATE");
            _applicantDivName = getApplicantdivName(db2);
            _testdivName = getTestDivName(db2);
            _certifSchoolSchoolName = getCertifSchool(db2, "SCHOOL_NAME");
            _certifSchoolJobName = getCertifSchool(db2, "JOB_NAME");
            _certifSchoolPrincipalName = getCertifSchool(db2, "PRINCIPAL_NAME");
            _schoolStampFile = getSchoolStamp(db2, request.getParameter("DOCUMENTROOT"));
            _output1 = request.getParameter("OUTPUT1");
            _examno = "2".equals(request.getParameter("OUTPUT2")) ? request.getParameter("EXAMNO1") : null;           // 指定：受験番号
            _check_inei = request.getParameter("CHECK_INEI");
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
        
        private String getCertifSchool(DB2UDB db2, final String field) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String cd = "106";
                final String sql = " SELECT " + field + " FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamYear + "' AND CERTIF_KINDCD = '" + cd + "' ";
//                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = null == rs.getString(field) ? "" : rs.getString(field);
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
        
        private File getSchoolStamp(final DB2UDB db2, final String documentRoot) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String imagepath = "";
            String extension = "";
            try {
                ps = db2.prepareStatement(" SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST WHERE CTRL_NO = '01' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    imagepath = rs.getString("IMAGEPATH");
                    extension = rs.getString("EXTENSION");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            final String jh = "P";
            final File schoolStampFile = new File(documentRoot + "/" + imagepath + "/SCHOOLSTAMP" + jh + "." + extension);
            log.info(" stamp file = " + schoolStampFile.toString() + " / exists? " + schoolStampFile.exists());
            return schoolStampFile.exists() ? schoolStampFile : null;
        }
    }
}

// eof
