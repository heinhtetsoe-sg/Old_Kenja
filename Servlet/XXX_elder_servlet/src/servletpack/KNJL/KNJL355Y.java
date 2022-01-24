/*
 * $Id: 20cd89cef4533713285ab76e619970aecbb38d6e $
 *
 * 作成日: 2010/11/12
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３５５Ｙ＞  入学者名簿
 **/
public class KNJL355Y {

    private static final Log log = LogFactory.getLog(KNJL355Y.class);

    private boolean _hasData;

    Param _param;

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
        }
        return count;
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL355Y.frm", 4);
        
        List applicantList = getApplicantList(db2);
        final int maxLine = 50;
        final int maxPage = getMaxPage(applicantList, maxLine);
        int line = 0;
        int seq = 0;
        int page = 1;
        for (Iterator it = applicantList.iterator(); it.hasNext();) {
            final Applicant applicant = (Applicant) it.next();
            line += 1;
            seq += 1;
            if (maxLine < line) {
                page += 1;
                line -= maxLine;
            }
            svf.VrsOut("NENDO",            _param._entexamYear + "年度");
            svf.VrsOut("PAGE",             String.valueOf(page));
            svf.VrsOut("TOTAL_PAGE",       String.valueOf(maxPage));
            svf.VrsOut("DATE",             _param._dateString);
            svf.VrsOut("TITLE",            _param._title + "入学試験　" + ("2".equals(_param._output) ? "入学者" : "合格者") + "名簿");
            svf.VrsOut("SCHOOLNAME",       _param._schoolName);
            
            final int namelen = getMS932count(applicant._name);
            final int finschoollen = getMS932count(applicant._finschoolName);
            final int gnamelen = getMS932count(applicant._gname);
            final String addr = (null == applicant._address1 ? "" : applicant._address1) + (null == applicant._address2 ? "" : applicant._address2);
            final int addrlen = getMS932count(addr);
            final String nameField = "NAME" + (20 < namelen ? "2" : "1");
            final String finschoolField = "FINSCHOOL" + (26 < finschoollen ? "2" : "1");
            final String gnameField = "GUARD_NAME" + (20 < gnamelen ? "2" : "1");
            final String addrField = "ADDRESS" + (50 < addrlen ? "2" : "1");
            
            // フィールド名注意
            svf.VrsOut("NUMBER", String.valueOf(seq));
            svf.VrsOut("EXAMNO", applicant._examno);
            svf.VrsOut(nameField, applicant._name);
            svf.VrsOut("SEX", applicant._sexname);
            svf.VrsOut(finschoolField, applicant._finschoolName);
            svf.VrsOut(gnameField, applicant._gname);
            svf.VrsOut("ZIPCODE", applicant._zipcd);
            svf.VrsOut(addrField, addr);
            svf.VrsOut("TELNO", applicant._telno);
            
            svf.VrEndRecord();
            _hasData = true;
        }
    }
    
    private int getMaxPage(final List applicants, final int MAX_LINE) {
        final int p = applicants.size() / MAX_LINE;
        return (applicants.size() % MAX_LINE == 0 ? p : p + 1);
    }

    private List getApplicantList(DB2UDB db2) {
        final List applicantList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String sexname = rs.getString("SEXNAME");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String gname = rs.getString("GNAME");
                final String zipcd = rs.getString("ZIPCD");
                final String address1 = rs.getString("ADDRESS1");
                final String address2 = rs.getString("ADDRESS2");
                final String telno = rs.getString("TELNO");
                
                Applicant applicant = new Applicant(examno, name, sexname, finschoolName, gname, zipcd, address1, address2, telno);
                applicantList.add(applicant);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return applicantList;
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT  ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     NMZ002.NAME2 AS SEXNAME, ");
        stb.append("     T2.FINSCHOOL_NAME, ");
        stb.append("     T3.GNAME, ");
        stb.append("     T3.ZIPCD, ");
        stb.append("     T3.ADDRESS1, ");
        stb.append("     T3.ADDRESS2, ");
        stb.append("     T3.TELNO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST T2 ON T2.FINSCHOOLCD = T1.FS_CD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T3.EXAMNO = T1.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = T1.SEX ");
        stb.append("     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' AND NML013.NAMECD2 = T1.JUDGEMENT ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND NML013.NAMESPARE1 = '1' ");
        if ("2".equals(_param._output))  { // 入学者
            stb.append("     AND T1.PROCEDUREDIV = '1' ");
            stb.append("     AND T1.ENTDIV = '1' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.EXAMNO ");
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
        final String _examno;
        final String _name;
        final String _sexname;
        final String _finschoolName;
        final String _gname;
        final String _zipcd;
        final String _address1;
        final String _address2;
        final String _telno;
        public Applicant(
                final String examno,
                final String name,
                final String sexname,
                final String finschoolName,
                final String gname,
                final String zipcd,
                final String address1,
                final String address2,
                final String telno) {
            _examno = examno;
            _name = name;
            _sexname = sexname;
            _finschoolName = finschoolName;
            _gname = gname;
            _zipcd = zipcd;
            _address1 = address1;
            _address2 = address2;
            _telno = telno;
        }
    }

    /** パラメータクラス */
    private class Param {
        final String _entexamYear;
        final String _applicantDiv;
        final String _testDiv;
        final String _title;
        final Map _testdivNames;
        final String _dateString;
        final String _schoolName;
        final String _output;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _title = getApplicantdivName(db2);
            _testdivNames = getTestDivNames(db2);
            _dateString = new SimpleDateFormat("yyyy年M月d日").format(Date.valueOf(request.getParameter("LOGIN_DATE")));
            _schoolName = getSchoolName(db2);
            _output = request.getParameter("OUTPUT");
        }
        
        private String getApplicantdivName(DB2UDB db2) {
            String applicantdivName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT VALUE(NAME1, '') FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '" + _applicantDiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  applicantdivName = rs.getString("NAME1"); 
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return applicantdivName;
        }
        
        private Map getTestDivNames(DB2UDB db2) {
            final Map testDivNames = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String namecd1 = "1".equals(_applicantDiv) ? "L024" : "L004";
            try {
                ps = db2.prepareStatement(" SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' ");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1") && null != rs.getString("NAMECD2")) {
                    final String testDiv = rs.getString("NAMECD2");
                    final String testDivName = rs.getString("NAME1");
                    testDivNames.put(testDiv, testDivName);
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testDivNames;
        }
        
        private String getSchoolName(DB2UDB db2) {
            String schoolName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String cd = "1".equals(_applicantDiv) ? "105" : "106";
                final String sql = " SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamYear + "' AND CERTIF_KINDCD = '" + cd + "' ";
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    schoolName = rs.getString("SCHOOL_NAME");
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolName;
        }
    }
}

// eof

