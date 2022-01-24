/*
 * $Id: 216e5381cbd0d0d2c4a485ecdb6a128ea0fba26d $
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

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ４５５Ｙ＞  入学者名簿
 **/
public class KNJL455Y {

    private static final Log log = LogFactory.getLog(KNJL455Y.class);

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
        }
        return count;
    }
    
    private static List getPageList(final List list, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Applicant o = (Applicant) it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final String form = "KNJL455Y.frm";
        
        final List applicantAllList = getApplicantList(db2);
        final int maxLine = 50;
        final List pageList = getPageList(applicantAllList, maxLine);
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List applicantList = (List) pageList.get(pi);
            svf.VrSetForm(form, 1);

            svf.VrsOut("NENDO",            _param._entexamYear + "年度");
            svf.VrsOut("PAGE",             String.valueOf(pi + 1));
            svf.VrsOut("TOTAL_PAGE",       String.valueOf(pageList.size()));
            svf.VrsOut("DATE",             _param._dateString);
            svf.VrsOut("TITLE",            _param._applicantDivName + "入学試験　入学者名簿");
//            svf.VrsOut("SCHOOLNAME", null); // 学校名
            
            for (int i = 0; i < applicantList.size(); i++) {
                final Applicant applicant = (Applicant) applicantList.get(i);
                final int line = i + 1;

                final String addr = StringUtils.defaultString(applicant._address1) + StringUtils.defaultString(applicant._address2);
                // フィールド名注意
                svf.VrsOutn("NUMBER", line, String.valueOf(pi * maxLine + i + 1));
                svf.VrsOutn("EXAMNO", line, applicant._examno);
                svf.VrsOutn("NAME" + (30 < getMS932count(applicant._name) ? "3" : 20 < getMS932count(applicant._name) ? "2" : "1"), line, applicant._name);
                svf.VrsOutn("KANA" + (30 < getMS932count(applicant._nameKana) ? "3" : 20 < getMS932count(applicant._nameKana) ? "2" : "1"), line, applicant._nameKana);
                svf.VrsOutn("SEX", line, applicant._sexname);
                svf.VrsOutn("FINSCHOOL" + (40 < getMS932count(applicant._fsName) ? "3" : 26 < getMS932count(applicant._fsName) ? "2" : "1"), line, applicant._fsName);
                svf.VrsOutn("GUARD_NAME" + (20 < getMS932count(applicant._gname) ? "2" : "1"), line, applicant._gname);
                svf.VrsOutn("ZIPCODE", line, applicant._zipcd);
                svf.VrsOutn("ADDRESS" + (50 < getMS932count(addr) ? "2" : "1"), line, addr);
                svf.VrsOutn("TELNO", line, applicant._telno);
                
//                svf.VrsOutn("REMARK1", line, null); // 備考
//                svf.VrsOutn("REMARK2", line, null); // 備考
            }
            
            if (pi == pageList.size() - 1) {
                final Map countSexMap = new HashMap();
                for (int i = 0; i < applicantAllList.size(); i++) {
                    final Applicant appl = (Applicant) applicantAllList.get(i);
                    if (null == appl._sex) {
                        continue;
                    }
                    if (null == countSexMap.get(appl._sex)) {
                        countSexMap.put(appl._sex, new ArrayList());
                    }
                    ((List) countSexMap.get(appl._sex)).add(appl);
                }
                
                final StringBuffer note = new StringBuffer();
                int total = 0;
                String comma = "";
                for (final Iterator it = _param._sexNameMap.keySet().iterator(); it.hasNext();) {
                    final String sex = (String) it.next();
                    final List countList = (List) countSexMap.get(sex);
                    final int count = null == countList ? 0 : countList.size();
                    final String abbv1 = (String) _param._sexNameMap.get(sex);
                    note.append(comma).append(StringUtils.defaultString(abbv1)).append(count).append("名");
                    comma = "、";
                    total += count;
                }
                note.append(comma).append("合計").append(total).append("名");
                
                svf.VrsOut("NOTE", note.toString()); // 備考
            }
            
            svf.VrEndPage();
            _hasData = true;
        }
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
                final String nameKana = rs.getString("NAME_KANA");
                final String sex = rs.getString("SEX");
                final String sexname = rs.getString("SEXNAME");
                final String fsName = rs.getString("FS_NAME");
                final String gname = rs.getString("GNAME");
                final String zipcd = rs.getString("ZIPCD");
                final String address1 = rs.getString("ADDRESS1");
                final String address2 = rs.getString("ADDRESS2");
                final String telno = rs.getString("TELNO");
                
                Applicant applicant = new Applicant(examno, name, nameKana, sex, sexname, fsName, gname, zipcd, address1, address2, telno);
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
        stb.append("     T1.NAME_KANA, ");
        stb.append("     T1.SEX, ");
        stb.append("     NMZ002.NAME2 AS SEXNAME, ");
        stb.append("     T1.FS_NAME, ");
        stb.append("     T3.GNAME, ");
        stb.append("     T3.ZIPCD, ");
        stb.append("     T3.ADDRESS1, ");
        stb.append("     T3.ADDRESS2, ");
        stb.append("     T3.TELNO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T3.EXAMNO = T1.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = T1.SEX ");
        stb.append("     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' AND NML013.NAMECD2 = T1.JUDGEMENT ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND NML013.NAMESPARE1 = '1' ");
        stb.append("     AND T1.PROCEDUREDIV = '1' ");
        stb.append("     AND T1.ENTDIV = '1' ");
        stb.append(" ORDER BY ");
        if ("1".equals(_param._output)) {
            stb.append("     T1.NAME_KANA, ");
        }
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
        final String _examno;
        final String _name;
        final String _nameKana;
        final String _sex;
        final String _sexname;
        final String _fsName;
        final String _gname;
        final String _zipcd;
        final String _address1;
        final String _address2;
        final String _telno;
        public Applicant(
                final String examno,
                final String name,
                final String nameKana,
                final String sex,
                final String sexname,
                final String fsName,
                final String gname,
                final String zipcd,
                final String address1,
                final String address2,
                final String telno) {
            _examno = examno;
            _name = name;
            _nameKana = nameKana;
            _sex = sex;
            _sexname = sexname;
            _fsName = fsName;
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
        final String _applicantDivName;
        final String _dateString;
        final String _output; // 1:あいうえお 2:受験番号
        final Map _sexNameMap;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _applicantDivName = getApplicantdivName(db2);
            _sexNameMap = getSexName(db2);
            _dateString = new SimpleDateFormat("yyyy年M月d日").format(Date.valueOf(request.getParameter("LOGIN_DATE")));
            _output = request.getParameter("OUTPUT");
        }
        
        private Map getSexName(DB2UDB db2) {
            Map rtn = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAMECD2, ABBV1 FROM NAME_MST WHERE NAMECD1 = 'Z002' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("NAMECD2"), rs.getString("ABBV1"));
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
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
            final String namecd1 = "L004";
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
    }
}

// eof

