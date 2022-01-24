/*
 * 作成日: 2020/09/15
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJL402I {

    private static final Log log = LogFactory.getLog(KNJL402I.class);

    private boolean _hasData;

    private int MAX_LINE = 10;

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final List schoolList = getList(db2);
        for (Iterator iterator = schoolList.iterator(); iterator.hasNext();) {
            final School school = (School) iterator.next();

            //メイン
            printSvfMain(db2, svf, school);
            svf.VrEndPage();
        }
    }

    private void printSvfMain(final DB2UDB db2, final Vrw32alp svf, final School school) {
        final String form = "KNJL402I.frm";
        svf.VrSetForm(form , 4);

        int lineCnt = 1;
        int pageCnt = 1;

        //明細部以外を印字
        setTitle(svf, school);

        //明細部
        for (Iterator itStudent = school._studentList.iterator(); itStudent.hasNext();) {
            final Student student = (Student) itStudent.next();

            // 改ページの制御
            if (lineCnt > MAX_LINE) {
                svf.VrsOut("PAGE", "（" + pageCnt + "/" + school._maxPageCnt + "）"); //頁
                svf.VrEndRecord();
                svf.VrEndPage();
                svf.VrSetForm(form , 4);
                lineCnt = 1;
                pageCnt++;
                //明細部以外を印字
                setTitle(svf, school);
            }

            final String examno = student._examno.substring(student._examno.length() - 4);
            svf.VrsOut("EXAM_NO", examno); //受験番号
            svf.VrsOut("NAME", student._name); //氏名
            svf.VrsOut("JUDGE", student._judge); //合否
            svf.VrEndRecord();
            _hasData = true;
            lineCnt++;
        }
        svf.VrsOut("PAGE", "（" + pageCnt + "/" + school._maxPageCnt + "）"); //頁
        svf.VrEndRecord();
    }



    private void setTitle(final Vrw32alp svf, final School school) {
        svf.VrsOut("DATE", _param._outputDate); //日付
        svf.VrsOut("TITLE", _param._documentTitle); //タイトル

        svf.VrsOut("FINSCHOOL_NAME", school._finschool_name + "長"); //出身学校

        svf.VrsOut("SCHOOL_NAME", _param._certifSchoolSchoolName); //学校名
        svf.VrsOut("PRINCIPAL_NAME", _param._certifSchoolPrincipalName); //校長名

        VrsOutnRenban(svf, "NOTE1", KNJ_EditEdit.get_token(_param._documentText, 76, 12)); //文言

        svf.VrsOut("EXAM_DIV", _param._testDivName); //入試方式
        svf.VrsOut("CAPACITY", _param._capacity); //募集定員
        svf.VrsOut("APPLICANT", school._applicant); //志願者数
    }

    protected void VrsOutnRenban(final Vrw32alp svf, final String field, final String[] value) {
        if (null != value) {
            for (int i = 0 ; i < value.length; i++) {
                svf.VrsOutn(field, i + 1, value[i]);
            }
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        final String sql = getSchoolSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String entexamyear = StringUtils.defaultString(rs.getString("ENTEXAMYEAR"));
                final String applicantdiv = StringUtils.defaultString(rs.getString("APPLICANTDIV"));
                final String testdiv = StringUtils.defaultString(rs.getString("TESTDIV"));
                final String fs_cd = StringUtils.defaultString(rs.getString("FS_CD"));
                final String finschool_name = StringUtils.defaultString(rs.getString("FINSCHOOL_NAME"));
                final String applicant = StringUtils.defaultString(rs.getString("APPLICANT"));
                final School school = new School(entexamyear, applicantdiv, testdiv, fs_cd, finschool_name, applicant);
               school.setStudent(db2);
                retList.add(school);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSchoolSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH APPLICANTBASE AS( ");
        stb.append("     SELECT ");
        stb.append("         BASE.ENTEXAMYEAR, ");
        stb.append("         BASE.APPLICANTDIV, ");
        stb.append("         BASE.TESTDIV, ");
        stb.append("         BASE.FS_CD, ");
        stb.append("         COUNT(*) AS APPLICANT");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("         INNER JOIN ENTEXAM_RECEPT_DAT T2 ");
        stb.append("                 ON T2.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("                AND T2.APPLICANTDIV = BASE.APPLICANTDIV  ");
        stb.append("                AND T2.TESTDIV      = '"+ _param._testDiv +"' ");
        stb.append("                AND T2.EXAM_TYPE    = '1' ");
        stb.append("                AND T2.EXAMNO       = BASE.EXAMNO ");
        if (!"".equals(_param._judgeMent)) {
            stb.append("            AND T2.JUDGEDIV = '" + _param._judgeMent + "' ");
        }
        stb.append("     WHERE ");
        stb.append("             BASE.ENTEXAMYEAR  = '" + _param._entexamyear + "' ");
        stb.append("         AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("         AND BASE.FS_CD IS NOT NULL ");
        stb.append("     GROUP BY ");
        stb.append("         BASE.ENTEXAMYEAR, ");
        stb.append("         BASE.APPLICANTDIV, ");
        stb.append("         BASE.TESTDIV, ");
        stb.append("         BASE.FS_CD ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     BASE.ENTEXAMYEAR, ");
        stb.append("     BASE.APPLICANTDIV, ");
        stb.append("     BASE.TESTDIV, ");
        stb.append("     BASE.FS_CD, ");
        stb.append("     FS.FINSCHOOL_NAME, ");
        stb.append("     BASE.APPLICANT ");
        stb.append(" FROM ");
        stb.append("     APPLICANTBASE BASE ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FS ");
        stb.append("            ON BASE.FS_CD = FS.FINSCHOOLCD ");
        stb.append(" ORDER BY ");
        stb.append("     BASE.FS_CD ");
        return stb.toString();
    }

    private class School {
        String _entexamyear;
        String _applicantdiv;
        String _testdiv;
        String _fs_cd;
        String _finschool_name;
        String _applicant;
        int _maxPageCnt;
        List _studentList = new ArrayList();

        private School(
                final String entexamyear,
                final String applicantdiv,
                final String testdiv,
                final String fs_cd,
                final String finschool_name,
                final String applicant
        ) {
            _entexamyear = entexamyear;
            _applicantdiv = applicantdiv;
            _testdiv = testdiv;
            _fs_cd = fs_cd;
            _finschool_name = finschool_name;
            _applicant = applicant;
        }

        private void setStudent(final DB2UDB db2) {
            final String sql = getStudnetSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                int lineCnt = 1;
                _maxPageCnt = 1;
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String fs_cd = StringUtils.defaultString(rs.getString("FS_CD"));
                    final String examno = StringUtils.defaultString(rs.getString("EXAMNO"));
                    final String name = StringUtils.defaultString(rs.getString("NAME"));
                    final String judgement = StringUtils.defaultString(rs.getString("JUDGEMENT"));
                    final String judge = StringUtils.defaultString(rs.getString("JUDGE"));
                    final Student student = new Student(fs_cd, examno, name, judgement, judge);
                    _studentList.add(student);

                    //学校毎の頁数を保持
                    if (lineCnt > MAX_LINE) {
                        _maxPageCnt++;
                        lineCnt = 1;
                    }
                    lineCnt++;
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private String getStudnetSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  ");
            stb.append("   T1.FS_CD, ");
            stb.append("   T1.EXAMNO, ");
            stb.append("   T1.NAME, ");
            stb.append("   T1.JUDGEMENT, ");
            stb.append("   L013.NAME1 AS JUDGE ");
            stb.append(" FROM ");
            stb.append("   ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("   INNER JOIN ENTEXAM_RECEPT_DAT T2 ");
            stb.append("           ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
            stb.append("          AND T2.APPLICANTDIV = T1.APPLICANTDIV  ");
            stb.append("          AND T2.TESTDIV      = '"+ _testdiv +"' ");
            stb.append("          AND T2.EXAM_TYPE    = '1' ");
            stb.append("          AND T2.EXAMNO       = T1.EXAMNO ");
            if (!"".equals(_param._judgeMent)) {
                stb.append("      AND T2.JUDGEDIV = '" + _param._judgeMent + "' ");
            }
            stb.append("   LEFT JOIN ENTEXAM_SETTING_MST L013 ");
            stb.append("          ON L013.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
            stb.append("         AND L013.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("         AND L013.SETTING_CD   = 'L013' ");
            stb.append("         AND L013.SEQ          = T1.JUDGEMENT ");
            stb.append(" WHERE ");
            stb.append("       T1.ENTEXAMYEAR  = '"+ _entexamyear +"' ");
            stb.append("   AND T1.APPLICANTDIV = '"+ _applicantdiv +"' ");
            stb.append("   AND T1.FS_CD        = '"+ _fs_cd +"' ");
            stb.append(" ORDER BY ");
            stb.append("   T1.FS_CD, ");
            stb.append("   T1.EXAMNO ");

            return stb.toString();
        }
    }

    private static class Student {
        final String _fs_cd;
        final String _examno;
        final String _name;
        final String _judgement;
        final String _judge;

        private Student(
                final String fs_cd,
                final String examno,
                final String name,
                final String judgement,
                final String judge
        ) {
            _fs_cd = fs_cd;
            _examno = examno;
            _name = name;
            _judgement = judgement;
            _judge = judge;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 77052 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _loginYear;
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _preamble;
        private final String _judge; //抽出区分 1:全員 2:合格者のみ 3:不合格者のみ

        private final String _outputDate;

        private final String _judgeMent;
        private final String _documentTitle;
        private final String _documentText;

        private String _testDivName;
        private String _capacity;

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear = request.getParameter("LOGIN_YEAR");
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _preamble = request.getParameter("PREAMBLE");
            _judge = request.getParameter("JUDGE");

            //作成日時
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            final String date = sdf.format(new Date());
            final String[] outoutDate = date.split("/");
            _outputDate = outoutDate[0] + "年" + outoutDate[1] + "月" + outoutDate[2] + "日";

            _judgeMent = ("2".equals(_judge)) ? "1" : ("3".equals(_judge)) ? "2" : "";
            _documentTitle = getDocument(db2, "TITLE");
            _documentText = getDocument(db2, "TEXT");

            getTestDivMst(db2);

            setCertifSchoolDat(db2);
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final String certifKindcd = "105";
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT * FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _loginYear + "' AND CERTIF_KINDCD = '"+ certifKindcd +"' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _certifSchoolSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK7"));
            _certifSchoolJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _certifSchoolPrincipalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
        }

        private String getTestDivMst(final DB2UDB db2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   * ");
            stb.append(" FROM ");
            stb.append("   ENTEXAM_TESTDIV_MST ");
            stb.append(" WHERE ");
            stb.append("       ENTEXAMYEAR  = '"+ _entexamyear +"' ");
            stb.append("   AND APPLICANTDIV = '"+ _applicantDiv +"' ");
            stb.append("   AND TESTDIV      = '"+ _testDiv +"' ");

            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _testDivName = rs.getString("TESTDIV_ABBV");
                    _capacity = rs.getString("CAPACITY");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private String getDocument(final DB2UDB db2, final String field) {
            String rtnStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   * ");
            stb.append(" FROM ");
            stb.append("   DOCUMENT_MST ");
            stb.append(" WHERE ");
            stb.append("   DOCUMENTCD = '"+ _preamble +"' ");

            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnStr = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtnStr;
        }

    }
}

// eof

