/*
 * $Id: 18a862d32ad90c1693facb903fc7d3c1b7da06ff $
 *
 * 作成日: 2010/11/01
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
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ４０９Ｙ＞  面接記入用紙
 **/
public class KNJL409Y {
    
    private static final Log log = LogFactory.getLog(KNJL409Y.class);
    
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
    
    private static int getMS932count(final String str) {
        int count = 0;
        try {
            if (null != str) {
                count = str.getBytes("MS932").length;
            }
        } catch (Exception e) {
            log.error(e);
        }
        return count;
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final int maxLine = 20;
        final String form;
        if ("KNJL251Y".equals(_param._prgid)) {
            form = "1".equals(_param._output) ? "KNJL251Y_2.frm" : "KNJL251Y.frm";
        } else {
            form = "1".equals(_param._output) ? "KNJL409Y_2.frm" : "KNJL409Y.frm";
        }
        
        final String title = _param._entexamYear + "年度" + StringUtils.defaultString(_param._applicantDivName) + "入学試験　面接記入用紙";
        final String subtitle = "（" + StringUtils.defaultString(_param._testDivName) + " -" + ("1".equals(_param._output) ? "本人用" : "2".equals(_param._output) ? "保護者用" : "") + "-）";
        
        final List applicantAllList = getApplicantList(db2);
        final List pageList = getPageList(applicantAllList, maxLine);

        for (int pi = 0; pi < pageList.size(); pi++) {
            final List applicantList = (List) pageList.get(pi);
            svf.VrSetForm(form, 1);
            
            svf.VrsOut("DATE", formatDate(_param._loginDate)); // 日付
            svf.VrsOut("TITLE", title); // タイトル
            svf.VrsOut("SUBTITLE", subtitle); // サブタイトル
            
            for (int i = 0; i < applicantList.size(); i++) {
                final int line = i + 1;
                
                final Applicant appl = (Applicant) applicantList.get(i);

                svf.VrsOutn("NO", line, String.valueOf(pi * maxLine + i + 1)); // 番号
                svf.VrsOutn("EXAM_NO", line, appl._examno); // 受験番号
                
                svf.VrsOutn("SEX", line, appl._sexname);
                
                svf.VrsOutn("KANA1_" + (getMS932count(appl._nameKana) > 24 ? "2" : "1"), line, appl._nameKana); // 氏名1
                final int nameKeta = getMS932count(appl._name);
                svf.VrsOutn("NAME1" + (nameKeta > 24 ? "_3" : nameKeta > 20 ? "_2" : ""), line, appl._name); // 氏名1

                svf.VrsOutn("KANA2_" + (getMS932count(appl._gkana) > 24 ? "2" : "1"), line, appl._gkana); // 氏名1
                final int gnameKeta = getMS932count(appl._gname);
                svf.VrsOutn("NAME2" + (gnameKeta > 24 ? "_3" : gnameKeta > 20 ? "_2" : ""), line, appl._gname); // 氏名1

                svf.VrsOutn("SEX", line, appl._sexname); // 性別
                int fsCnt = getMS932count(appl._fsName);
                final String fsSoeji = 36 < fsCnt ? "4_1" : 28 < fsCnt ? "3_1" : 14 < fsCnt ? "2_1" : "1";
                svf.VrsOutn("FINSCHOOL_NAME" + fsSoeji, line, appl._fsName);
                
                if ("KNJL251Y".equals(_param._prgid)) {
                    if ("1".equals(_param._output)) {
                        svf.VrsOutn("VAL", line, appl._interviewValueName); // 面接
                    }
                    svf.VrsOutn("INTERVIEW", line, ("1".equals(_param._output) ? appl._interviewRemark : "2".equals(_param._output) ? appl._interviewRemark3 : "")); // 面接
                }

//                svf.VrsOutn("REMARK1", line, null); // 備考 リンクフィールド
//                svf.VrsOutn("REMARK2", line, null); // 備考
            }
            
            svf.VrEndPage();
            _hasData = true;
        }
    }
    
    private static String formatDate(final String date) {
        if (null == date) {
            return null;
        }
        return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date) ;
    }

    private List getApplicantList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        final List applicantList= new ArrayList();
        try {
            final String sql = getApplicantSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                
                final String examno = rs.getString("EXAMNO");
                final String fsName = rs.getString("FS_NAME");
                final String sexname = rs.getString("SEXNAME");
                final String gname = rs.getString("GNAME");
                final String gkana = rs.getString("GKANA");
                
                final Applicant applicant = new Applicant(examno, fsName, sexname, gname, gkana);
                applicant._name = rs.getString("NAME");
                applicant._nameKana = rs.getString("NAME_KANA");
                applicant._interviewValue = rs.getString("INTERVIEW_VALUE");
                applicant._interviewValueName = rs.getString("INTERVIEW_VALUE_NAME");
                applicant._interviewRemark = rs.getString("INTERVIEW_REMARK");
                applicant._interviewRemark3 = rs.getString("INTERVIEW_REMARK3");
                applicantList.add(applicant);
            }
        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return applicantList;
    }
    
    private static class Applicant {
        final String _examno;
        String _name;
        String _nameKana;
        final String _fsName;
        final String _sexname;
        final String _gname;
        final String _gkana;
        String _interviewValue;
        String _interviewValueName;
        String _interviewRemark;
        String _interviewRemark3;
        Applicant(
                final String examno,
                final String finschoolName,
                final String sexname,
                final String gname,
                final String gkana
                ) {
             _examno = examno;
             _fsName = finschoolName;
             _sexname = sexname;
             _gname = gname;
             _gkana = gkana;
        }
    }
    
    private String getApplicantSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     T0.EXAMNO, ");
        stb.append("     T0.NAME, ");
        stb.append("     T0.NAME_KANA, ");
        stb.append("     T0.FS_NAME, ");
        stb.append("     NMZ002.NAME2 AS SEXNAME, ");
        stb.append("     ADDR.GNAME, ");
        stb.append("     ADDR.GKANA, ");
        stb.append("     INTV.INTERVIEW_VALUE, ");
        stb.append("     NML027.NAME1 AS INTERVIEW_VALUE_NAME, ");
        stb.append("     INTV.INTERVIEW_REMARK, ");
        stb.append("     INTV.INTERVIEW_REMARK3 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T0 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ON ADDR.ENTEXAMYEAR = T0.ENTEXAMYEAR ");
        stb.append("          AND ADDR.EXAMNO = T0.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = T0.SEX ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT RCPT ON RCPT.ENTEXAMYEAR = T0.ENTEXAMYEAR ");
        stb.append("          AND RCPT.APPLICANTDIV = T0.APPLICANTDIV ");
        stb.append("          AND RCPT.EXAM_TYPE = '1' ");
        stb.append("          AND RCPT.EXAMNO = T0.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_INTERVIEW_DAT INTV ON INTV.ENTEXAMYEAR = T0.ENTEXAMYEAR  ");
        stb.append("         AND INTV.APPLICANTDIV = T0.APPLICANTDIV  ");
        stb.append("         AND INTV.TESTDIV = RCPT.TESTDIV  ");
        stb.append("         AND INTV.EXAMNO = T0.EXAMNO  ");
        stb.append("     LEFT JOIN NAME_MST NML027 ON NML027.NAMECD1 = 'L027' AND NML027.NAMECD2 = INTV.INTERVIEW_VALUE ");
        stb.append(" WHERE ");
        stb.append("      T0.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("      AND T0.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("      AND RCPT.TESTDIV = '" + _param._testDiv + "' ");
        if (null != _param._receptnoFrom) {
            stb.append("        AND T0.EXAMNO >= '" + _param._receptnoFrom + "' ");
        }
        if (null != _param._receptnoTo) {
            stb.append("        AND T0.EXAMNO <= '" + _param._receptnoTo + "' ");
        }
        if (null != _param._hidReceptnoMin) {
            stb.append("        AND T0.EXAMNO >= '" + _param._hidReceptnoMin + "' ");
        }
        if (null != _param._hidReceptnoMax) {
            stb.append("        AND T0.EXAMNO <= '" + _param._hidReceptnoMax + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T0.EXAMNO ");
        return stb.toString();
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
        final String _receptnoFrom; // KNJL409Y
        final String _receptnoTo; // KNJL409Y
        final String _hidReceptno; // KNJL251Y
        final String _output; // 1:本人 2:保護者
        final String _loginDate;
        final String _applicantDivName;
        final String _testDivName;
        final String _prgid;
        String _hidReceptnoMin = null; // KNJL251Y
        String _hidReceptnoMax = null; // KNJL251Y

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _receptnoFrom = StringUtils.isBlank(request.getParameter("RECEPTNO_FROM")) ? null : request.getParameter("RECEPTNO_FROM");
            _receptnoTo = StringUtils.isBlank(request.getParameter("RECEPTNO_TO")) ? null : request.getParameter("RECEPTNO_TO");
            _hidReceptno = request.getParameter("HID_RECEPTNO");
            _output = request.getParameter("OUTPUT");
            _loginDate = request.getParameter("LOGIN_DATE");
            _applicantDivName = getApplicantDivName(db2);
            _testDivName = getTestDivName(db2, "NAME1");
            _prgid = request.getParameter("PRGID");
            setHidReceptnoMinMax();
        }
        
        private void setHidReceptnoMinMax() {
            final String[] arr = StringUtils.split(_hidReceptno, ",");
            if (null != arr && arr.length > 0) {
                log.info(" hid = " + ArrayUtils.toString(arr));
                final TreeMap no = new TreeMap();
                for (int i = 0; i < arr.length; i++) {
                    final String examno = arr[i];
                    if (NumberUtils.isDigits(examno)) {
                        no.put(Integer.valueOf(examno), examno);
                    }
                }
                if (!no.isEmpty()) {
                    _hidReceptnoMin = (String) no.get(no.firstKey());
                    _hidReceptnoMax = (String) no.get(no.lastKey());
                }
            }
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
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolName;
        }
        
        private String getTestDivName(DB2UDB db2, final String field) {
            String val = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String namecd1 = "L004";
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + _testDiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString(field)) {
                  val = rs.getString(field); 
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return val;
        }
    }
}

// eof
