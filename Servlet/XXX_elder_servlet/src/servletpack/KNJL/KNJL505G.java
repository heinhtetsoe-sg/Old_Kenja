/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 9c5048fea1bb9ebc8d8f502080b2c8363c5e295c $
 *
 * 作成日: 2019/01/28
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
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
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL505G {

    private static final Log log = LogFactory.getLog(KNJL505G.class);

    private boolean _hasData;
    private final String ALL = "all";

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
        svf.VrSetForm("KNJL505G.frm", 1);

        final String nendo = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear+"/04/01");
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final int maxLine = 50;
            int lineCnt = 1;
            int renBan = 1;
            String befTestDiv = "";
            while (rs.next()) {
                final String testDiv = rs.getString("TESTDIV");
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String sex = rs.getString("SEX");
                final String gName = rs.getString("GNAME");
                final String judgement = StringUtils.defaultString(rs.getString("JUDGEMENT"));
                final String judgeName = rs.getString("JUDGE_NAME");
                final String sucCourse = rs.getString("SUC_COURSE");
                final String course1 = rs.getString("COURSE1");
                final String course2 = rs.getString("COURSE2");
                final String shName = rs.getString("SH_NAME");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String grdYear = StringUtils.defaultString(rs.getString("REMARK6"));
                final String grddivname = StringUtils.defaultString(rs.getString("GRDDIVNAME"));
                final String majorname = StringUtils.defaultString(rs.getString("MAJORNAME"));
                final String kyoudaiName = StringUtils.defaultString(rs.getString("REMARK1"));
                final String relation = StringUtils.defaultString(rs.getString("RELATION"));

                if (lineCnt > maxLine) {
                    svf.VrEndPage();
                    lineCnt = 1;
                }
                if (!"".equals(befTestDiv) && !befTestDiv.equals(testDiv)) {
                    svf.VrEndPage();
                    lineCnt = 1;
                    renBan = 1;
                }
                setTitle(db2, svf, nendo, testDiv);

                svf.VrsOutn("NO", lineCnt, String.valueOf(renBan));
                if ("1".equals(judgement)) {
                    svf.VrsOutn("JUDGE1", lineCnt, judgeName);
                } else if ("2".equals(judgement)) {
                    svf.VrsOutn("JUDGE2", lineCnt, judgeName);
                } else if ("3".equals(judgement)) {
                    svf.VrsOutn("ATTEND", lineCnt, judgeName);
                }
                svf.VrsOutn("EXAM_NO", lineCnt, examno);
                final String nameField = getMS932Bytecount(name) > 30 ? "2" : "1";
                svf.VrsOutn("NAME" + nameField, lineCnt, name);
                final String kanaField = getMS932Bytecount(nameKana) > 30 ? "2" : "1";
                svf.VrsOutn("KANA" + kanaField, lineCnt, nameKana);
                svf.VrsOutn("SEX", lineCnt, sex);
                svf.VrsOutn("GURD_NAME", lineCnt, gName);
                svf.VrsOutn("PASS_COURSE", lineCnt, sucCourse);
                svf.VrsOutn("HOPE1", lineCnt, course1);
                svf.VrsOutn("HOPE2", lineCnt, course2);
                svf.VrsOutn("SDIV", lineCnt, shName);
                final String schoolField = getMS932Bytecount(finschoolName) > 20 ? "2" : "1";
                svf.VrsOutn("FINSCHOOL_NAME" + schoolField, lineCnt, finschoolName);

                final String setRemark = getRemarkData(grdYear, grddivname, majorname, kyoudaiName, relation);
                svf.VrsOutn("REMARK", lineCnt, setRemark);

                lineCnt++;
                renBan++;
                befTestDiv = testDiv;
                _hasData = true;
            }

            if (_hasData) {
                svf.VrEndPage();
            }
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String getRemarkData(
            final String remark6,
            final String grddivname,
            final String majorname,
            final String remark1,
            final String relation
    ) {
        String retStr = "";
        String setSep = "";
        if (!"".equals(remark6)) {
            retStr += setSep + remark6 + "年度";
            setSep = " ";
        }
        if (!"".equals(grddivname)) {
            retStr += setSep + grddivname;
            setSep = " ";
        }
        if (!"".equals(majorname)) {
            retStr += setSep + majorname;
            setSep = " ";
        }
        if (!"".equals(remark1)) {
            retStr += setSep + remark1;
            setSep = " ";
        }
        if (!"".equals(relation)) {
            retStr += setSep + relation;
            setSep = " ";
        }
        return retStr;
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final String nendo, final String testDiv) {
        svf.VrsOut("TITLE", nendo + "度　" + _param._applicantdivName + "　" + StringUtils.defaultString((String)_param._testdivMap.get(testDiv)) + "　親・兄弟姉妹優遇生の合格者一覧表");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2,_param._loginDate));
    }

    private static int getMS932Bytecount(String str) {
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

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VBASE.TESTDIV, ");
        stb.append("     VBASE.EXAMNO, ");
        stb.append("     VBASE.NAME, ");
        stb.append("     VBASE.NAME_KANA, ");
        stb.append("     VBASE.BIRTHDAY, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     VALUE(VBASE.JUDGEMENT, '') AS JUDGEMENT, ");
        stb.append("     L013.NAME1 AS JUDGE_NAME, ");
        stb.append("     ADDR.GNAME, ");
        stb.append("     VBASE.SUC_COURSECD || VBASE.SUC_MAJORCD || VBASE.SUC_COURSECODE AS CMC, ");
        stb.append("     VMAJOR.MAJORNAME || VCOURSE.COURSECODENAME AS SUC_COURSE, ");
        stb.append("     COURSE1.EXAMCOURSE_NAME AS COURSE1, ");
        stb.append("     COURSE2.EXAMCOURSE_NAME AS COURSE2, ");
        stb.append("     VBASE.SHDIV, ");
        stb.append("     L006.NAME1 AS SH_NAME, ");
        stb.append("     VBASE.FS_CD, ");
        stb.append("     FINSCHOOL.FINSCHOOL_NAME, ");
        stb.append("     BD018.REMARK6, ");
        stb.append("     BD018.REMARK7, ");
        stb.append("     L038.NAME1 AS GRDDIVNAME, ");
        stb.append("     BD018.REMARK5, ");
        stb.append("     MAJOR.MAJORNAME, ");
        stb.append("     BD018.REMARK1, ");
        stb.append("     BD018.REMARK3, ");
        stb.append("     H201.NAME1 AS RELATION ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND VBASE.SEX = Z002.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L006 ON L006.NAMECD1 = 'L006' ");
        stb.append("          AND VBASE.SHDIV = L006.NAMECD2 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON VBASE.FS_CD = FINSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ON VBASE.ENTEXAMYEAR = ADDR.ENTEXAMYEAR ");
        stb.append("          AND VBASE.EXAMNO = ADDR.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST L013 ON L013.NAMECD1 = 'L013' ");
        stb.append("          AND VBASE.JUDGEMENT = L013.NAMECD2 ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE1 ON VBASE.ENTEXAMYEAR = COURSE1.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = COURSE1.APPLICANTDIV ");
        stb.append("          AND VBASE.TESTDIV = COURSE1.TESTDIV ");
        stb.append("          AND VBASE.DAI1_COURSECD = COURSE1.COURSECD ");
        stb.append("          AND VBASE.DAI1_MAJORCD = COURSE1.MAJORCD ");
        stb.append("          AND VBASE.DAI1_COURSECODE = COURSE1.EXAMCOURSECD ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE2 ON VBASE.ENTEXAMYEAR = COURSE2.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = COURSE2.APPLICANTDIV ");
        stb.append("          AND VBASE.TESTDIV = COURSE2.TESTDIV ");
        stb.append("          AND VBASE.DAI2_COURSECD = COURSE2.COURSECD ");
        stb.append("          AND VBASE.DAI2_MAJORCD = COURSE2.MAJORCD ");
        stb.append("          AND VBASE.DAI2_COURSECODE = COURSE2.EXAMCOURSECD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD018 ON VBASE.ENTEXAMYEAR = BD018.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = BD018.APPLICANTDIV ");
        stb.append("          AND VBASE.EXAMNO = BD018.EXAMNO ");
        stb.append("          AND BD018.SEQ = '018' ");
        stb.append("     LEFT JOIN NAME_MST H201 ON H201.NAMECD1 = 'H201' ");
        stb.append("          AND BD018.REMARK3 = H201.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L038 ON L038.NAMECD1 = 'L038' ");
        stb.append("          AND BD018.REMARK7 = L038.NAMECD2 ");
        stb.append("     LEFT JOIN MAJOR_MST MAJOR ON BD018.REMARK5 = MAJOR.MAJORCD ");
        stb.append("     LEFT JOIN V_COURSE_MAJOR_MST VMAJOR ON VBASE.ENTEXAMYEAR = VMAJOR.YEAR ");
        stb.append("          AND VBASE.SUC_COURSECD = VMAJOR.COURSECD ");
        stb.append("          AND VBASE.SUC_MAJORCD = VMAJOR.MAJORCD ");
        stb.append("     LEFT JOIN V_COURSECODE_MST VCOURSE ON VBASE.ENTEXAMYEAR = VCOURSE.YEAR ");
        stb.append("          AND VBASE.SUC_COURSECODE = VCOURSE.COURSECODE ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if (!ALL.equals(_param._testDiv)) {
            stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
        }
        stb.append("     AND (BD018.REMARK6 IS NOT NULL OR ");
        stb.append("          BD018.REMARK7 IS NOT NULL OR ");
        stb.append("          BD018.REMARK5 IS NOT NULL OR ");
        stb.append("          BD018.REMARK1 IS NOT NULL OR ");
        stb.append("          BD018.REMARK3 IS NOT NULL) ");
        //合格者のみ
        stb.append("     AND VBASE.JUDGEMENT = '1' ");
        stb.append(" ORDER BY ");
        stb.append("     VBASE.TESTDIV, ");
        stb.append("     VBASE.EXAMNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65395 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _loginDate;
        final String _applicantdivName;
        final Map _testdivMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _loginDate = request.getParameter("LOGIN_DATE");
            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantDiv);
            _testdivMap = getNameMstMap(db2, "NAME1", "L004");
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

        private static Map getNameMstMap(final DB2UDB db2, final String field, final String namecd1) {
            final Map retMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT NAMECD2, " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retMap.put(rs.getString("NAMECD2"), rs.getString(field));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return retMap;
        }

    }
}

// eof
