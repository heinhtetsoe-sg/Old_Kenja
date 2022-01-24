/*
 * $Id: 0db66cb7c50998ab92a36b2ca4f596b6e32916da $
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２６Ｂ＞  各種通知書（校長）
 **/
public class KNJL326B {

    private static final Log log = LogFactory.getLog(KNJL326B.class);

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
    
    public void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final List list = Finschool.load(db2, _param);
        final String form = "KNJL326B.frm";
        final int maxLine = 34;
        
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Finschool finschool = (Finschool) it.next();
            
            final List pageList = getPageList(finschool._applicantList, maxLine);
            
            for (int pi = 0; pi < pageList.size(); pi++) {
                final List applicantList = (List) pageList.get(pi);

                svf.VrSetForm(form, 1);
                
                svf.VrsOut("FINSCHOOL_NAME", StringUtils.defaultString(finschool._finschoolName)); // 出身集学校名
                // svf.VrsOut("FINSCHOOL_PRINC", "校長　" + StringUtils.defaultString(finschool._princname) + "　殿); // 出身学校校長名
                svf.VrsOut("FINSCHOOL_PRINC", "校長　殿"); // 出身学校校長名
                svf.VrsOut("KIND", "1".equals(_param._testdiv) ? "推薦" : "志願"); // 入試制度
                svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度" + (_param._testdivName1) + "結果について（通知）"); // タイトル
                svf.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名
                svf.VrsOut("PRINCIPAL_NAME", _param._principalName); // 校長名
                svf.VrsOut("DATE", _param._dateStr); // 日付
                
                for (int j = 0; j < applicantList.size(); j++) {
                    final Applicant appl = (Applicant) applicantList.get(j);
                    final int line = j + 1;
                    svf.VrsOutn("EXAM_NO", line, appl._examno); // 受験番号
                    final int nameByteLen = getMS932ByteLength(appl._name);
                    svf.VrsOutn("NAME" + (nameByteLen <= 20 ? "1" : nameByteLen <= 30 ? "2" : "3"), line, appl._name); // 氏名
                    svf.VrsOutn("JUDGE", line, appl._judge); // 判定結果
                    final String courseName;
                    if ("1".equals(appl._tanganKirikaeSingaku)) {
                        courseName = "単願切換" + StringUtils.defaultString(appl._tankiriSingakuCourseName);
                    } else {
                        courseName = StringUtils.defaultString(appl._examcourseName) + (null != appl._examcourseName && "1".equals(appl._receptDetailRemark4) ? "*" : "");
                    }
                    svf.VrsOutn("PASS_COURSE", line, courseName); // 合格区分
                }
                svf.VrEndPage();
            }
            _hasData = true;
        }
    }
    
    private static int getMS932ByteLength(final String s) {
        int rtn = 0;
        if (null != s) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return rtn;
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
    
    private static class Finschool {
        final String _fsCd;
        final String _finschoolName;
        final String _princname;
        final List _applicantList;

        Finschool(
            final String fsCd,
            final String finschoolName,
            final String princname
        ) {
            _fsCd = fsCd;
            _finschoolName = finschoolName;
            _princname = princname;
            _applicantList = new ArrayList();
        }

        private static Finschool getFinschool(final List list, final String fsCd) {
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Finschool fs = (Finschool) it.next();
                if ((null == fs._fsCd && null == fsCd) || fs._fsCd.equals(fsCd)) {
                    return fs;
                }
            }
            return null;
        }
        
        public static List load(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String fsCd = rs.getString("FS_CD");
                    if (null == getFinschool(list, fsCd)) {
                        final String finschoolName = rs.getString("FINSCHOOL_NAME");
                        final String princname = rs.getString("PRINCNAME");
                        final Finschool finschool = new Finschool(fsCd, finschoolName, princname);
                        list.add(finschool);
                    }
                    
                    final Finschool finschool = getFinschool(list, fsCd);
                    final String examno = rs.getString("EXAMNO");
                    final String name = rs.getString("NAME");
                    final String judge = rs.getString("JUDGE");
                    final String tanganKirikaeSingaku = rs.getString("TANGAN_KIRIKAE_SINGAKU");
                    final String examcourseName = rs.getString("EXAMCOURSE_NAME");
                    final String tankiriSingakuCourseName = rs.getString("TANKIRI_SINGAKU_COURSE_NAME");
                    final String receptDetailRemark4 = rs.getString("RECEPT_DETAIL_REMARK4");
                    final Applicant applicant = new Applicant(examno, name, judge, tanganKirikaeSingaku, examcourseName, tankiriSingakuCourseName, receptDetailRemark4);
                    finschool._applicantList.add(applicant);
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
            stb.append(" SELECT ");
            stb.append("     BASE.FS_CD, ");
            stb.append("     FIN.FINSCHOOL_NAME, ");
            stb.append("     FIN.PRINCNAME, ");
            stb.append("     BASE.EXAMNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     CASE WHEN RDETAIL1.REMARK3 IS NULL AND RDETAIL2.REMARK4 IS NOT NULL THEN '1' END AS TANGAN_KIRIKAE_SINGAKU,  ");
            stb.append("     CASE WHEN NML013.NAMESPARE1 = '1' THEN '合格' ");
            stb.append("          WHEN RDETAIL1.REMARK3 IS NULL AND RDETAIL2.REMARK4 IS NOT NULL THEN '合格'  ");
            stb.append("          ELSE '不合格' ");
            stb.append("     END AS JUDGE, ");
            stb.append("     CRS1.EXAMCOURSE_NAME AS EXAMCOURSE_NAME, ");
            stb.append("     CRS2.EXAMCOURSE_NAME AS TANKIRI_SINGAKU_COURSE_NAME, ");
            stb.append("     RDETAIL2.REMARK4 AS RECEPT_DETAIL_REMARK4 ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND RECEPT.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND RECEPT.EXAMNO = BASE.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RDETAIL1 ON RDETAIL1.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
            stb.append("         AND RDETAIL1.APPLICANTDIV = RECEPT.APPLICANTDIV ");
            stb.append("         AND RDETAIL1.TESTDIV = RECEPT.TESTDIV ");
            stb.append("         AND RDETAIL1.EXAM_TYPE = RECEPT.EXAM_TYPE ");
            stb.append("         AND RDETAIL1.RECEPTNO = RECEPT.RECEPTNO ");
            stb.append("         AND RDETAIL1.SEQ = '001' ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RDETAIL2 ON RDETAIL2.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
            stb.append("         AND RDETAIL2.APPLICANTDIV = RECEPT.APPLICANTDIV ");
            stb.append("         AND RDETAIL2.TESTDIV = RECEPT.TESTDIV ");
            stb.append("         AND RDETAIL2.EXAM_TYPE = RECEPT.EXAM_TYPE ");
            stb.append("         AND RDETAIL2.RECEPTNO = RECEPT.RECEPTNO ");
            stb.append("         AND RDETAIL2.SEQ = '002' ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST CRS1 ON CRS1.ENTEXAMYEAR = RDETAIL1.ENTEXAMYEAR ");
            stb.append("         AND CRS1.APPLICANTDIV = RDETAIL1.APPLICANTDIV ");
            stb.append("         AND CRS1.TESTDIV = RDETAIL1.TESTDIV ");
            stb.append("         AND CRS1.COURSECD = RDETAIL1.REMARK1 ");
            stb.append("         AND CRS1.MAJORCD = RDETAIL1.REMARK2 ");
            stb.append("         AND CRS1.EXAMCOURSECD = RDETAIL1.REMARK3 ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST CRS2 ON CRS2.ENTEXAMYEAR = RDETAIL2.ENTEXAMYEAR ");
            stb.append("         AND CRS2.APPLICANTDIV = RDETAIL2.APPLICANTDIV ");
            stb.append("         AND CRS2.TESTDIV = RDETAIL2.TESTDIV ");
            stb.append("         AND CRS2.COURSECD = RDETAIL2.REMARK1 ");
            stb.append("         AND CRS2.MAJORCD = RDETAIL2.REMARK2 ");
            stb.append("         AND CRS2.EXAMCOURSECD = RDETAIL2.REMARK3 ");
            stb.append("     LEFT JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD = BASE.FS_CD ");
            stb.append("     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' ");
            stb.append("         AND NML013.NAMECD2 = BASE.JUDGEMENT ");
            stb.append(" WHERE ");
            stb.append("     BASE.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND BASE.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND BASE.TESTDIV = '" + param._testdiv + "' ");
            if ("1".equals(param._specialReasonDiv)) {
                stb.append("     AND BASE.SPECIAL_REASON_DIV IS NOT NULL ");
            }
            if ("2".equals(param._output)) {
                stb.append("     AND BASE.FS_CD = '" + param._schoolcd + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     BASE.FS_CD, ");
            stb.append("     BASE.EXAMNO ");
            return stb.toString();
        }
    }
    
    private static class Applicant {
        final String _examno;
        final String _name;
        final String _judge;
        final String _tanganKirikaeSingaku;
        final String _examcourseName;
        final String _receptDetailRemark4;
        final String _tankiriSingakuCourseName;

        Applicant(
            final String examno,
            final String name,
            final String judge,
            final String tanganKirikaeSingaku,
            final String examcourseName,
            final String tankiriSingakuCourseName,
            final String receptDetailRemark4
        ) {
            _examno = examno;
            _name = name;
            _judge = judge;
            _tanganKirikaeSingaku = tanganKirikaeSingaku;
            _examcourseName = examcourseName;
            _tankiriSingakuCourseName = tankiriSingakuCourseName;
            _receptDetailRemark4 = receptDetailRemark4;
        }
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _date;
        final String _output;
        final String _schoolcd;
        final String _documentroot;
        final String _specialReasonDiv;

        final String _testdivName1;
        final String _testdivAbbv1;
        final String _dateStr;
        final String _schoolName;
        final String _principalName;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _date = request.getParameter("TSUCHI_DATE");
            _dateStr = null == _date ? null : getDateStr(_date.replace('/', '-'));
            _output = request.getParameter("OUTPUT");
            _schoolcd = request.getParameter("SCHOOLCD");
            _documentroot = request.getParameter("DOCUMENTROOT");
            _specialReasonDiv = request.getParameter("SPECIAL_REASON_DIV");
            
            _testdivName1 = getNameMst(db2, "NAME1", "L004", _testdiv);
            _testdivAbbv1 = getNameMst(db2, "ABBV1", "L004", _testdiv);
            _schoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
            _principalName = getCertifSchoolDat(db2, "PRINCIPAL_NAME");
        }
        
        private String getDateStr(final String date) {
            if (null == date) {
                return null;
            }
            return KNJ_EditDate.h_format_JP(date);
        }

        private String getExamCourseName(final DB2UDB db2, final String field, final String examcoursecd) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                StringBuffer sql = new StringBuffer();
                sql.append(" SELECT " + field + " ");
                sql.append(" FROM ENTEXAM_COURSE_MST ");
                sql.append(" WHERE ENTEXAMYEAR = '" + _entexamyear + "' ");
                sql.append("   AND APPLICANTDIV = '" + _applicantdiv + "' ");
                sql.append("   AND TESTDIV = '" + _testdiv + "' ");
                sql.append("   AND EXAMCOURSECD = '" + examcoursecd + "' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
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
        
        private String getCertifSchoolDat(final DB2UDB db2, final String field) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                StringBuffer sql = new StringBuffer();
                sql.append(" SELECT " + field + " ");
                sql.append(" FROM CERTIF_SCHOOL_DAT ");
                sql.append(" WHERE YEAR = '" + _entexamyear + "' ");
                sql.append("   AND CERTIF_KINDCD = '106' ");
                ps = db2.prepareStatement(sql.toString());
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

