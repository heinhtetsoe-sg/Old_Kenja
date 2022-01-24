/*
 * $Id: b28e03280ff0ead021c257e6112d52ae807fc984 $
 *
 * 作成日: 2016/10/12
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJM432W {

    private static final Log log = LogFactory.getLog(KNJM432W.class);

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
        svf.VrSetForm("KNJM432W.frm", 1);
        final List list = getList(db2);
        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
            PrintData printData = (PrintData) iterator.next();
            svf.VrsOut("TITLE", "単位修得通知書");
            svf.VrsOut("SYOSYO_NAME", (String) _param._certifSchoolMap.get("SYOSYO_NAME"));
            svf.VrsOut("CERTIF_NO", null != printData._certifNo ? printData._certifNo : "");
            svf.VrsOut("SYOSYO_NAME2", (String) _param._certifSchoolMap.get("SYOSYO_NAME2"));
            svf.VrsOut("NOTE", "本校通信制の課程において、下記の科目の単位を修得したことを通知する。");
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._date));
            svf.VrsOut("SCHREGNO", printData._schregNo);
            svf.VrsOut("HR_NAME", printData._hrName);
            svf.VrsOut("NAME", printData._name);
            svf.VrsOut("SCHOOLNAME", (String) _param._certifSchoolMap.get("SCHOOL_NAME"));
            svf.VrsOut("STAFFNAME", (String) _param._certifSchoolMap.get("PRINCIPAL_NAME"));
            svf.VrsOut("SUBCLASS_NAME1", printData._subclassName);
            svf.VrsOut("tani1", printData._credit);
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String hrName = rs.getString("HR_NAME");
                final String name = rs.getString("NAME");
                final String certifNo = rs.getString("CERTIF_NO");
                final String subclassName = rs.getString("SUBCLASSNAME");
                final String credit = rs.getString("CREDITS");

                final PrintData printData = new PrintData(schregNo, hrName, name, certifNo, subclassName, credit);
                retList.add(printData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     REG_H.HR_NAME, ");
        stb.append("     BASE.NAME, ");
        if ("1".equals(_param._certif_no_8keta) || "1".equals(_param._certifNoSyudou)) {
            stb.append("     T1.REMARK1 AS CERTIF_NO, ");
        } else {
            stb.append("     T1.CERTIF_NO, ");
        }
        stb.append("     SUB_T.SUBCLASSNAME, ");
        stb.append("     CREDIT.GET_CREDIT AS CREDITS ");
        stb.append(" FROM ");
        if ("1".equals(_param._certif_no_8keta) || "1".equals(_param._certifNoSyudou)) {
            stb.append("     CERTIF_DETAIL_EACHTYPE_SUBCLASS_DAT T1 ");
        } else {
            stb.append("     CERTIF_ISSUE_SUBCLASS_DAT T1 ");
        }
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT REG_D ON T1.YEAR = REG_D.YEAR ");
        stb.append("          AND REG_D.SEMESTER = '" + _param._semester + "' ");
        stb.append("          AND T1.SCHREGNO = REG_D.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REG_H ON T1.YEAR = REG_H.YEAR ");
        stb.append("          AND REG_D.SEMESTER = REG_H.SEMESTER ");
        stb.append("          AND REG_D.GRADE = REG_H.GRADE ");
        stb.append("          AND REG_D.HR_CLASS = REG_H.HR_CLASS ");
        stb.append("     LEFT JOIN SUBCLASS_MST SUB_T ON T1.CLASSCD = SUB_T.CLASSCD ");
        stb.append("          AND T1.SCHOOL_KIND = SUB_T.SCHOOL_KIND ");
        stb.append("          AND T1.CURRICULUM_CD = SUB_T.CURRICULUM_CD ");
        stb.append("          AND T1.SUBCLASSCD = SUB_T.SUBCLASSCD ");
        stb.append("     LEFT JOIN V_RECORD_SCORE_HIST_DAT CREDIT ON REG_D.YEAR = CREDIT.YEAR ");
        stb.append("          AND CREDIT.SEMESTER || '-' || CREDIT.TESTKINDCD || '-' || CREDIT.TESTITEMCD || '-' || CREDIT.SCORE_DIV = '9-99-00-09' ");
        stb.append("          AND T1.CLASSCD = CREDIT.CLASSCD ");
        stb.append("          AND T1.SCHOOL_KIND = CREDIT.SCHOOL_KIND ");
        stb.append("          AND T1.CURRICULUM_CD = CREDIT.CURRICULUM_CD ");
        stb.append("          AND T1.SUBCLASSCD = CREDIT.SUBCLASSCD ");
        stb.append("          AND T1.SCHREGNO = CREDIT.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SCHREGNO || '_' || T1.CERTIF_INDEX IN " + _param._taisyouInState + " ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class PrintData {
        final String _schregNo;
        final String _hrName;
        final String _name;
        final String _certifNo;
        final String _subclassName;
        final String _credit;

        public PrintData(
                final String schregNo,
                final String hrName,
                final String name,
                final String certifNo,
                final String subclassName,
                final String credit
        ) {
            _schregNo       = schregNo;
            _hrName         = hrName;
            _name           = name;
            _certifNo       = certifNo;
            _subclassName   = subclassName;
            _credit         = credit;
        }
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _date;
        private final String _order;
        private final String _selSub;
        private final String _certifNoSyudou;
        private final String _useSchool_KindField;
        private final String _useCurriculumcd;
        private final String _certif_no_8keta;
        private final String _printParam;
        private final String _taisyouInState;
        private final Map _certifSchoolMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _date = request.getParameter("CTRL_DATE");
            _order = request.getParameter("ORDER");
            _selSub = request.getParameter("SELSUB");
            _certifNoSyudou = request.getParameter("certifNoSyudou");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _certif_no_8keta = request.getParameter("certif_no_8keta");
            _printParam = request.getParameter("printParam");
            _taisyouInState = " ('" + StringUtils.replace(_printParam, ",", "','") + "')";

            _certifSchoolMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '134' ";
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _certifSchoolMap.put("CERTIF_NO", rs.getString("CERTIF_NO"));
                    _certifSchoolMap.put("SYOSYO_NAME", rs.getString("SYOSYO_NAME"));
                    _certifSchoolMap.put("SYOSYO_NAME2", rs.getString("SYOSYO_NAME2"));
                    _certifSchoolMap.put("SCHOOL_NAME", rs.getString("SCHOOL_NAME"));
                    _certifSchoolMap.put("PRINCIPAL_NAME", rs.getString("PRINCIPAL_NAME"));
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

    }
}

// eof

