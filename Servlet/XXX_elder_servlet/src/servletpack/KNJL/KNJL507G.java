/*
 * $Id: 630673566cec8844b4738e01a5d241194110fc49 $
 *
 * 作成日: 2018/12/21
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
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL507G {

    private static final Log log = LogFactory.getLog(KNJL507G.class);

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

        if("1".equals(_param._printDiv)) {
            useForm = "KNJL507G_1.frm"; //特待生合格通知書
            sucFormFlg = true;
        }else {
            useForm = "KNJL507G_2.frm"; //特待生選考結果通知書
            sucFormFlg = false;
        }

        String outNendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度";
        String date = KNJ_EditDate.h_format_JP(db2,_param._printDate);
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
                final String scholarshipName = StringUtils.defaultString(rs.getString("SCHOLARSHIP_NAME"));
                final String courseName = rs.getString("COURSE_NAME");
                final String courseCd  = rs.getString("COURSECD");
                final String tokutaiCd = rs.getString("TOKUTAICD");

                svf.VrSetForm(useForm, 1);

                //共通
                svf.VrsOut("NENDO", outNendo); //年度
                svf.VrsOut("DATE", date); //日付
                svf.VrsOut("EXAM_NO", examno); //受験番号
                final String nameField = getMS932Bytecount(name) > 30 ? "3" : getMS932Bytecount(name) > 20 ? "2" : "1";
                svf.VrsOut("NAME" + nameField, name); //氏名
                svf.VrsOut("CORP_NAME", (String) _param._certifSchoolMap.get("CORP_NAME")); //法人名
                svf.VrsOut("SCHOOL_NAME", (String) _param._certifSchoolMap.get("SCHOOL_NAME")); //学校名
                svf.VrsOut("JOB_NAME", (String) _param._certifSchoolMap.get("JOB_NAME")); //役職名
                svf.VrsOut("STAFF_NAME", (String) _param._certifSchoolMap.get("PRINCIPAL_NAME")); //校長名

                if(sucFormFlg) {
                    //特待生合格通知書
                    svf.VrsOut("COURSE_NAME", courseName); //コース名
                    svf.VrsOut("SCHOLARSHIP_NAME", scholarshipName); //特待名称

                    final String setMenjo = (String) _param._himokuMap.get(tokutaiCd + courseCd) + " 免除";
                    svf.VrsOut("EXEMPT_NAME", "【" + outNendo + setMenjo + "】"); //免除名称
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
    	return KNJ_EditEdit.getMS932ByteLength(str);
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VBASE.EXAMNO, ");
        stb.append("     VBASE.NAME, ");
        stb.append("     VALUE(L025.NAME2, L025.NAME1) AS SCHOLARSHIP_NAME, ");
        stb.append("     value(MAJOR.MAJORABBV, '') AS COURSE_NAME, ");
        stb.append("     value(L036_C.NAMECD2, '') AS COURSECD, ");
        stb.append("     value(L036_T.NAMECD2, '') AS TOKUTAICD ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        //特待希望の生徒が対象
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S029 ");
        stb.append( "          ON S029.ENTEXAMYEAR  = VBASE.ENTEXAMYEAR ");
        stb.append("          AND S029.APPLICANTDIV = VBASE.APPLICANTDIV ");
        stb.append("          AND S029.EXAMNO       = VBASE.EXAMNO ");
        stb.append("          AND S029.SEQ          = '029' ");
        stb.append("          AND S029.REMARK1      = '1' ");
        stb.append("     LEFT JOIN NAME_MST L025 ");
        stb.append("          ON L025.NAMECD2  = VBASE.JUDGE_KIND ");
        stb.append("          AND L025.NAMECD1 = 'L025' ");
        stb.append("     LEFT JOIN V_NAME_MST L036_C ON L036_C.YEAR    = VBASE.ENTEXAMYEAR ");
        stb.append("                                AND L036_C.NAMECD1 = 'L036' ");
        stb.append("                                AND L036_C.ABBV2   = SUBSTR(VBASE.SUC_COURSECODE, 3, 1) ");
        stb.append("     LEFT JOIN V_NAME_MST L036_T ON L036_T.YEAR    = VBASE.ENTEXAMYEAR ");
        stb.append("                                AND L036_T.NAMECD1 = 'L036' ");
        stb.append("                                AND L036_T.ABBV3   = VBASE.JUDGE_KIND ");
        stb.append("     LEFT JOIN V_NAME_MST L013 ON L013.YEAR    = VBASE.ENTEXAMYEAR ");
        stb.append("                              AND L013.NAMECD1 = 'L013' ");
        stb.append("                              AND L013.NAMECD2 = VBASE.JUDGEMENT ");
        stb.append("     LEFT JOIN MAJOR_MST MAJOR ON VBASE.SUC_COURSECD = MAJOR.COURSECD ");
        stb.append("                              AND VBASE.SUC_MAJORCD  = MAJOR.MAJORCD ");
        stb.append(" WHERE ");
        stb.append("         VBASE.ENTEXAMYEAR  = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND VBASE.TESTDIV      = '" + _param._testDiv + "' ");
        stb.append("     AND L013.NAMESPARE1    = '1' "); // 合格
        if ("1".equals(_param._printDiv) && "2".equals(_param._passDiv)) {
            if(!"".equals(_param._passExamno)) {
                stb.append("           AND VBASE.EXAMNO >= '" + _param._passExamno + "' ");
            }
            if(!"".equals(_param._passExamnoTo)) {
                stb.append("           AND VBASE.EXAMNO <= '" + _param._passExamnoTo + "' ");
            }
        }
        if ("2".equals(_param._printDiv) && "2".equals(_param._unpassDiv)) {
            if(!"".equals(_param._unpassExamno)) {
                stb.append("           AND VBASE.EXAMNO >= '" + _param._unpassExamno + "' ");
            }
            if(!"".equals(_param._unpassExamnoTo)) {
                stb.append("           AND VBASE.EXAMNO <= '" + _param._unpassExamnoTo + "' ");
            }
        }
        if ("1".equals(_param._printDiv)) {
            stb.append("     AND L025.NAMESPARE1 = '1' "); // 特待合格者
        }
        if ("2".equals(_param._printDiv)) {
            stb.append("     AND ( L025.NAMESPARE1 <> '1' OR L025.NAMESPARE1 IS NULL ) ");
        }
        stb.append(" ORDER BY ");
        stb.append("     VBASE.EXAMNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 67867 $");
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
        private final Map _himokuMap;


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
            _himokuMap = getHimokuMap(db2, _entexamyear, _applicantDiv);

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

        private Map getHimokuMap(final DB2UDB db2, final String year, final String applicantdiv) {
            final Map rtnMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     TKOUTAI.EXEMPTION_CD AS TOKUTAICD, ");
                stb.append("     T1.EXEMPTION_CD AS COURESECD, ");
                stb.append("     ITEM.REMARK6 ");
                stb.append(" FROM ");
                stb.append("     ENTEXAM_PAYMENT_EXEMPTION_MST T1 ");
                stb.append("     LEFT JOIN ENTEXAM_PAYMENT_EXEMPTION_MST TKOUTAI ON TKOUTAI.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
                stb.append("                                                    AND TKOUTAI.APPLICANTDIV = T1.APPLICANTDIV ");
                stb.append("                                                    AND TKOUTAI.DIV          = T1.DIV ");
                stb.append("                                                    AND TKOUTAI.KIND_CD      = '1' "); // 固定
                stb.append("                                                    AND TKOUTAI.ITEM_CD      = T1.ITEM_CD ");
                stb.append("     LEFT JOIN ENTEXAM_PAYMENT_ITEM_MST ITEM ON ITEM.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
                stb.append("                                            AND ITEM.APPLICANTDIV = T1.APPLICANTDIV ");
                stb.append("                                            AND ITEM.DIV          = T1.DIV ");
                stb.append("                                            AND ITEM.ITEM_CD      = T1.ITEM_CD ");
                stb.append(" WHERE ");
                stb.append("         T1.ENTEXAMYEAR  = '"+ year +"' ");
                stb.append("     AND T1.APPLICANTDIV = '"+ applicantdiv +"' ");
                stb.append("     AND T1.DIV          = '0' "); // 固定
                stb.append("     AND T1.KIND_CD      = '2' "); // 固定
                stb.append(" ORDER BY ");
                stb.append("     TKOUTAI.EXEMPTION_CD, ");
                stb.append("     T1.EXEMPTION_CD ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String setKey = rs.getString("TOKUTAICD") + rs.getString("COURESECD");
                    if (null == rtnMap.get(setKey)) {
                        rtnMap.put(setKey, " " + rs.getString("REMARK6"));
                    } else {
                        final String setStr = (String) rtnMap.get(setKey);
                        rtnMap.put(setKey, setStr + "・" + rs.getString("REMARK6"));
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtnMap;
        }

    }
}

// eof

