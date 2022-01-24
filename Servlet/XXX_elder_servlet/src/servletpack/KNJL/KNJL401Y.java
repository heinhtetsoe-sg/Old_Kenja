/*
 * $Id: 782723ef3ed1a54a01324ad65b683612330b9991 $
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
 *                  ＜ＫＮＪＬ４０１Ｙ＞  グループ別志願者一覧
 **/
public class KNJL401Y {
    
    private static final Log log = LogFactory.getLog(KNJL401Y.class);
    
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
        
        final String formname = "KNJL401Y.frm";
        
        final List applicantListAll = getApplicants(db2);
        final int linePerPage = 40;
        
        final List pageList = getPageList(applicantListAll, linePerPage);

        for (int pi = 0; pi < pageList.size(); pi++) {
            final List applicantList = (List) pageList.get(pi);

            svf.VrSetForm(formname, 1);
            
//            svf.VrsOut("NENDO", ); // 年度
            svf.VrsOut("DATE", _param._dateString); //
            svf.VrsOut("TITLE", _param._entexamyear + "年度" + StringUtils.defaultString(_param._applicantDivName) + "入学試験グループ別志願者一覧表"); // 
            svf.VrsOut("SUBTITLE", "（" + StringUtils.defaultString(_param._tetDivName) + "）"); // サブタイトル
//            svf.VrsOut("RECOMMEND", null); // 推薦
//            svf.VrsOut("COURSE", null); // コース

            for (int li = 0; li < applicantList.size(); li++) {
                final Applicant applicant = (Applicant) applicantList.get(li);
                final int line = li + 1;

                final String groupCd = null != applicant._examHallCd && applicant._examHallCd.length() > 2 ? applicant._examHallCd.substring(applicant._examHallCd.length() - 2) : applicant._examHallCd;
                svf.VrsOut("GROUP_NAME", StringUtils.defaultString(groupCd) + ":" + StringUtils.defaultString(applicant._examHallName)); // グループ名

                svf.VrsOutn("SEATNO", line, String.valueOf(applicant._no)); // 座席番号
                svf.VrsOutn("EXAMNO", line, applicant._examno); // 受験番号
                final int nameKeta = getMS932count(applicant._name);
                final int nameKanaKeta = getMS932count(applicant._nameKana);
                svf.VrsOutn("NAME" + (nameKeta > 30 ? "3" : nameKeta > 20 ? "2" : "1"), line, applicant._name); // 氏名1
                svf.VrsOutn("NAME_KANA" + (nameKanaKeta > 30 ? "3" : nameKanaKeta > 20 ? "2" : "1"), line, applicant._nameKana); // 氏名1
                svf.VrsOutn("SEX", line, applicant._sexName); // 性別
                final int fsNameKeta = getMS932count(applicant._fsName);
                svf.VrsOutn("FINSCHOOL_ABBV" + (fsNameKeta > 24 ? "3_1" : fsNameKeta > 20 ? "2" : "1"), line, applicant._fsName); // 出身学校略称
                svf.VrsOutn("BIRTHDAY", line, null != applicant._birthday ? applicant._birthday.replace('-', '.') : ""); // 生年月日
                //svf.VrsOutn("REMARK", line, null); // 備考
            }
            _hasData = true;
            svf.VrEndPage();
        }
    }
    
    private List getPageList(final List applicantList, final int countPerPage) {
        String oldExamHallCd = null;
        final List pageList = new ArrayList();
        List current = null;
        int no = 1;
        for (final Iterator it = applicantList.iterator(); it.hasNext();) {
            final Applicant applicant = (Applicant) it.next();
            
            final boolean diff = null != oldExamHallCd && !oldExamHallCd.equals(applicant._examHallCd);
            if (null == current || diff || current.size() >= countPerPage) {
                if (diff) {
                   no = 1; 
                }
                current = new ArrayList();
                pageList.add(current);
            }
            current.add(applicant);
            applicant._no = no;
            oldExamHallCd = applicant._examHallCd;
            no += 1;
        }
        return pageList;
    }

    private List getApplicants(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List applicants = new ArrayList();
        try {
            final String sql = getApplicantSql();
            log.info(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String examHallCd = rs.getString("EXAMHALLCD");
                final String examHallName = rs.getString("EXAMHALL_NAME");
                final String examno = rs.getString("EXAMNO");
                final String name     = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String birthday = rs.getString("BIRTHDAY");
                final String sexName = rs.getString("SEX_NAME");
                final String fsName = rs.getString("FS_NAME");
                
                final Applicant applicant = new Applicant(examHallCd, examHallName, examno, name, nameKana, birthday, sexName, fsName);
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
    
    private static class Applicant {
        int _no;
        final String _examHallCd;
        final String _examHallName;
        final String _examno;
        final String _name;
        final String _nameKana;
        final String _birthday;
        final String _sexName;
        final String _fsName;
        public Applicant(
                final String examHallCd,
                final String examHallName,
                final String examno,
                final String name,
                final String nameKana,
                final String birthday,
                final String sexName,
                final String fsName) {
            _examHallCd = examHallCd;
            _examHallName = examHallName;
            _examno = examno;
            _name = name;
            _nameKana = nameKana;
            _birthday = birthday;
            _sexName = sexName;
            _fsName = fsName;
        }
    }

    private String getApplicantSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH ENTEXAM_HALL_EXAMNO AS ( ");
        stb.append("  SELECT ");
        stb.append("      T1.TESTDIV, ");
        stb.append("      T1.EXAMHALLCD, ");
        stb.append("      T1.EXAMHALL_NAME, ");
        stb.append("      T3.EXAMNO ");
        stb.append("  FROM ");
        stb.append("      ENTEXAM_HALL_YDAT T1 ");
        stb.append("      INNER JOIN ENTEXAM_HALL_LIST_YDAT HLY ON HLY.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("          AND HLY.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("          AND HLY.TESTDIV = T1.TESTDIV ");
        stb.append("          AND HLY.EXAM_TYPE = T1.EXAM_TYPE ");
        stb.append("          AND HLY.EXAMHALLCD = T1.EXAMHALLCD ");
        stb.append("      INNER JOIN ENTEXAM_RECEPT_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("          AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("          AND T3.TESTDIV = T1.TESTDIV ");
        stb.append("          AND T3.EXAM_TYPE = T1.EXAM_TYPE ");
        stb.append("          AND T3.RECEPTNO = HLY.RECEPTNO ");
        stb.append("  WHERE ");
        stb.append("      T1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("      AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("      AND T1.TESTDIV = '" + _param._testDiv + "' ");
        stb.append(") ");
        stb.append(" SELECT ");
        stb.append("     HALL.EXAMHALLCD, ");
        stb.append("     HALL.EXAMHALL_NAME, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.NAME_KANA, ");
        stb.append("     T1.BIRTHDAY, ");
        stb.append("     T1.FS_NAME, ");
        stb.append("     NM1.NAME2 AS SEX_NAME ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     INNER JOIN ENTEXAM_HALL_EXAMNO HALL ON HALL.EXAMNO = T1.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST NM1 ON NM1.NAMECD1 = 'Z002' AND NM1.NAMECD2 = T1.SEX ");
        stb.append("  WHERE ");
        stb.append("      T1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("      AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("     HALL.EXAMHALLCD, T1.EXAMNO ");
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
        final String _entexamyear;
        final String _applicantDiv;
        final String _applicantDivName;
        final String _testDiv;
        final String _dateString;
        final String _tetDivName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _applicantDivName = getApplicantdivName(db2);
            _testDiv = request.getParameter("TESTDIV");
            _tetDivName = getTestDivName(db2);
            _dateString = new SimpleDateFormat("yyyy年M月d日").format(Date.valueOf(request.getParameter("LOGIN_DATE")));
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
