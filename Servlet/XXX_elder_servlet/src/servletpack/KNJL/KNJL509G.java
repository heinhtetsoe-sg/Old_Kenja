/*
 * $Id: fe4741d14bebfdeaad974089849d892d934b15ba $
 *
 * 作成日: 2019/01/08
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJL509G {

    private static final Log log = LogFactory.getLog(KNJL509G.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        String date = KNJ_EditDate.h_format_JP(db2,_param._printDate);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String sucMajor =  StringUtils.defaultString(rs.getString("SUC_MAJOR"));
                final String sucCoursecode =  StringUtils.defaultString(rs.getString("SUC_COURSECODE"));
                final boolean isDoyou = "1".equals(rs.getString("IS_DOYOU")); // 土曜コース
                String coursename = null;
                String majorname = null;
                final int spaceIdx = sucMajor.indexOf("　");
				if (0 <= spaceIdx) {
                	coursename = sucMajor.substring(0, spaceIdx); // スペースより前が学科名
                	if (spaceIdx < sucMajor.length()) {
                		majorname = sucMajor.substring(spaceIdx + 1); // スペースより後がコース名
                	}
                }
                svf.VrSetForm("KNJL509G.frm", 1); //入学許可証
                if (null != _param._schoollogoImagePath) {
                    svf.VrsOut("SCHOOL_LOGO", _param._schoollogoImagePath);
                }
                svf.VrsOut("EXAM_NO", schregno); //受験番号
                final String nameField = getMS932Bytecount(name) > 30 ? "3" : getMS932Bytecount(name) > 20 ? "2" : "1";
                svf.VrsOut("NAME" + nameField, name); //氏名
                svf.VrsOut("DEPARTMENT", coursename); //学科
                if (isDoyou) {
                    svf.VrsOut("COURSE", majorname); //コース
                } else {
                    svf.VrsOut("COURSE", StringUtils.defaultString(majorname) + "　　" + StringUtils.defaultString(sucCoursecode)); //コース
                }
                svf.VrsOut("DATE", date); //日付
                svf.VrsOut("CORP_NAME", (String) _param._certifSchoolMap.get("CORP_NAME")); //法人名
                svf.VrsOut("SCHOOL_NAME", (String) _param._certifSchoolMap.get("SCHOOL_NAME")); //学校名
                svf.VrsOut("JOB_NAME", (String) _param._certifSchoolMap.get("JOB_NAME")); //役職名
                svf.VrsOut("STAFF_NAME", (String) _param._certifSchoolMap.get("PRINCIPAL_NAME")); //校長名
                svf.VrEndPage();

                _hasData = true;
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
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
        stb.append("     BD026.REMARK1 AS SCHREGNO, ");
        stb.append("     VBASE.EXAMNO, ");
        stb.append("     VBASE.NAME, ");
        stb.append("     COURSE.COURSENAME AS SUC_COURSE, ");
        stb.append("     MAJOR.MAJORNAME2 AS SUC_MAJOR, ");
        stb.append("     COURSECODE.COURSECODEABBV1 AS SUC_COURSECODE, ");
        stb.append("     CASE WHEN SUBSTR(VBASE.SUC_COURSECODE, 3, 1) = '1' THEN 1 END AS IS_DOYOU ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        //合格者が対象
        stb.append("     INNER JOIN NAME_MST L013 ");
        stb.append("           ON L013.NAMECD2 = VBASE.JUDGEMENT ");
        stb.append("          AND L013.NAMECD1 = 'L013' ");
        stb.append("          AND L013.NAMESPARE1 = '1' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD026 ");
        stb.append("           ON BD026.ENTEXAMYEAR  = VBASE.ENTEXAMYEAR ");
        stb.append("          AND BD026.APPLICANTDIV = VBASE.APPLICANTDIV ");
        stb.append("          AND BD026.EXAMNO       = VBASE.EXAMNO ");
        stb.append("          AND BD026.SEQ          = '026' ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST SUC_COURSE ");
        stb.append("           ON SUC_COURSE.ENTEXAMYEAR  = VBASE.ENTEXAMYEAR ");
        stb.append("          AND SUC_COURSE.APPLICANTDIV = VBASE.APPLICANTDIV ");
        stb.append("          AND SUC_COURSE.TESTDIV      = VBASE.TESTDIV ");
        stb.append("          AND SUC_COURSE.COURSECD     = VBASE.SUC_COURSECD ");
        stb.append("          AND SUC_COURSE.MAJORCD      = VBASE.SUC_MAJORCD ");
        stb.append("          AND SUC_COURSE.EXAMCOURSECD = VBASE.SUC_COURSECODE ");
        stb.append("    LEFT JOIN COURSE_MST COURSE ");
        stb.append("           ON COURSE.COURSECD = SUC_COURSE.ENTER_COURSECD ");
        stb.append("    LEFT JOIN MAJOR_MST MAJOR ");
        stb.append("           ON MAJOR.COURSECD = SUC_COURSE.ENTER_COURSECD ");
        stb.append("          AND MAJOR.MAJORCD  = SUC_COURSE.ENTER_MAJORCD ");
        stb.append("    LEFT JOIN COURSECODE_MST COURSECODE ");
        stb.append("           ON COURSECODE.COURSECODE = SUC_COURSE.ENTER_COURSECODE ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        if ("2".equals(_param._printDiv)) {
            stb.append("     AND VBASE.EXAMNO = '" + _param._passExamno + "' ");
        }
        stb.append("     AND VBASE.PROCEDUREDIV = '1' ");
        stb.append(" ORDER BY ");
        stb.append("     VBASE.EXAMNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65592 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _entexamyear;
        private final String _printDate;
        private final String _printDiv;
        private final String _passExamno;
        private final String _documentroot;
        private String _schoollogoImagePath;
        private final Map _certifSchoolMap;


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("YEAR");
            _printDate = request.getParameter("PRINT_DATE");
            _printDiv = request.getParameter("PRINT_DIV");
            _passExamno = StringUtils.defaultString(request.getParameter("PASS_EXAMNO"));
            _documentroot = request.getParameter("DOCUMENTROOT");
            final String path = _documentroot + "/" + getImagePath(db2) + "/SCHOOLLOGO_H.jpg";
            if (new File(path).exists()) {
            	_schoollogoImagePath = path;
            } else {
            	log.warn("file not found : " + path);
            }
            _certifSchoolMap = getCertifScholl(db2);
        }

        private String getImagePath(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' "));
        }

        private Map getCertifScholl(final DB2UDB db2) {
            final Map rtnMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamyear + "' AND CERTIF_KINDCD = '106' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnMap.put("CORP_NAME", rs.getString("REMARK6"));
                    rtnMap.put("SCHOOL_NAME", rs.getString("SCHOOL_NAME"));
                    rtnMap.put("JOB_NAME", rs.getString("JOB_NAME"));
                    rtnMap.put("PRINCIPAL_NAME", rs.getString("PRINCIPAL_NAME"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtnMap;
        }

    }
}

// eof

