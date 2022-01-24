/*
 * $Id: b78c852e34d1420b689e219843f93a89c83d34a6 $
 *
 * 作成日: 2018/12/20
 * 作成者: matsushima
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

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL506G {

    private static final Log log = LogFactory.getLog(KNJL506G.class);

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
        String useForm = "";
        boolean sucFormFlg = false;
        String[] nendo = KNJ_EditDate.tate_format4(db2,_param._entexamyear + "-12-31");
        String[] date = KNJ_EditDate.tate_format4(db2,KNJ_EditDate.H_Format_Haifun(_param._printDate));
        String date2 = KNJ_EditDate.h_format_JP(db2,_param._printDate);

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
                final String sucCourse = StringUtils.defaultString(rs.getString("SUC_COURSE"));
                final String course1 = StringUtils.defaultString(rs.getString("COURSE1"));

                if("1".equals(_param._printDiv)) {
                    useForm = "KNJL506G_1.frm"; //合格通知書
                    sucFormFlg = true;
                }else {
                    if(!"".equals(sucCourse)) {
                        useForm = "KNJL506G_2.frm"; //審査結果通知書 合格コースあり
                    }else {
                        useForm = "KNJL506G_3.frm"; //審査結果通知書 合格コースなし
                    }
                    sucFormFlg = false;
                }
                svf.VrSetForm(useForm, 1);

                //共通
                svf.VrsOut("EXAM_NO", examno); //受験番号
                svf.VrsOut("NENDO", sucFormFlg? nendo[1] : nendo[0] + nendo[1] + "年度"); //年度
                final String nameField = getMS932Bytecount(name) > 30 ? "3" : getMS932Bytecount(name) > 20 ? "2" : "1";
                svf.VrsOut("NAME" + nameField, name); //氏名

                if(sucFormFlg) {
                    //合格通知書
                    if(!"2".equals(_param._passDiv)) {
                        svf.VrsOut("COURSE_NAME", sucCourse); //コース名
                    }else {
                        svf.VrsOut("COURSE_NAME", course1); //コース名
                    }
                    svf.VrsOut("YEAR", date[1]); //年
                    svf.VrsOut("MONTH", date[2]); //月
                    svf.VrsOut("DAY", date[3]); //日
                }else {
                    //審査結果通知書
                    svf.VrsOut("DATE", date2); //日付
                    svf.VrsOut("CORP_NAME", (String) _param._certifSchoolMap.get("CORP_NAME")); //法人名
                    svf.VrsOut("SCHOOL_NAME", (String) _param._certifSchoolMap.get("SCHOOL_NAME")); //学校名
                    svf.VrsOut("JOB_NAME", (String) _param._certifSchoolMap.get("JOB_NAME")); //役職名
                    svf.VrsOut("STAFF_NAME", (String) _param._certifSchoolMap.get("PRINCIPAL_NAME")); //校長名
                }

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
        stb.append("     VBASE.EXAMNO, ");
        stb.append("     VBASE.NAME, ");
        stb.append("     value(MAJOR.MAJORABBV, '') AS SUC_COURSE, ");
        stb.append("     value(MAJOR1.MAJORABBV, '') AS COURSE1 ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     LEFT JOIN NAME_MST L013 ");
        stb.append("           ON L013.NAMECD2 = VBASE.JUDGEMENT ");
        stb.append("          AND L013.NAMECD1 = 'L013' ");
        stb.append("     LEFT JOIN MAJOR_MST MAJOR ON VBASE.SUC_COURSECD = MAJOR.COURSECD ");
        stb.append("                              AND VBASE.SUC_MAJORCD  = MAJOR.MAJORCD ");
        stb.append("     LEFT JOIN MAJOR_MST MAJOR1 ON VBASE.DAI1_COURSECD = MAJOR1.COURSECD ");
        stb.append("                               AND VBASE.DAI1_MAJORCD  = MAJOR1.MAJORCD ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
        if ("1".equals(_param._printDiv) && "3".equals(_param._passDiv)) {
            if(!"".equals(_param._passExamno)) {
                stb.append("           AND VBASE.EXAMNO >= '" + _param._passExamno + "' ");
            }
            if(!"".equals(_param._passExamnoTo)) {
                stb.append("           AND VBASE.EXAMNO <= '" + _param._passExamnoTo + "' ");
            }
        }
        if ("2".equals(_param._printDiv) && "3".equals(_param._unpassDiv)) {
            if(!"".equals(_param._unpassExamno)) {
                stb.append("           AND VBASE.EXAMNO >= '" + _param._unpassExamno + "' ");
            }
            if(!"".equals(_param._unpassExamnoTo)) {
                stb.append("           AND VBASE.EXAMNO <= '" + _param._unpassExamnoTo + "' ");
            }
        }
        if ("1".equals(_param._printDiv) && !"2".equals(_param._passDiv)) {
            stb.append("          AND L013.NAMESPARE1 = '1' ");
        }
        if ("2".equals(_param._printDiv) && !"2".equals(_param._unpassDiv)) {
            stb.append("          AND ( L013.NAMESPARE1 <> '1' OR L013.NAMESPARE1 IS NULL ) ");
        }
        stb.append(" ORDER BY ");
        stb.append("     VBASE.EXAMNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 67446 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _printDate;
        private final String _printDiv;
        private final String _passDiv;
        private final String _passExamno;
        private final String _passExamnoTo;
        private final String _unpassDiv;
        private final String _unpassExamno;
        private final String _unpassExamnoTo;
        private final Map _certifSchoolMap;


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _printDate = request.getParameter("PRINT_DATE");
            _printDiv = request.getParameter("PRINT_DIV");
            _passDiv = request.getParameter("PASS_DIV");
            _passExamno = StringUtils.defaultString(request.getParameter("PASS_EXAMNO"));
            _passExamnoTo = StringUtils.defaultString(request.getParameter("PASS_EXAMNO_TO"));
            _unpassDiv = request.getParameter("UNPASS_DIV");
            _unpassExamno = StringUtils.defaultString(request.getParameter("UNPASS_EXAMNO"));
            _unpassExamnoTo = StringUtils.defaultString(request.getParameter("UNPASS_EXAMNO_TO"));
            _certifSchoolMap = getCertifScholl(db2);

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

