/*
 * $Id: c05b14a5341bdb9728f97e682391de229fccbc20 $
 *
 * 作成日: 2013/10/10
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３００Ｒ＞  会場ラベル用紙
 **/
public class KNJL300R {

    private static final Log log = LogFactory.getLog(KNJL300R.class);

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

    private static int getMS932ByteLength(final String s) {
        return KNJ_EditEdit.getMS932ByteLength(s);
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T2.EXAMNO, ");
        stb.append("     T2.RECEPTNO, ");
        stb.append("     BASE.NAME ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_HALL_YDAT T1 ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T2.TESTDIV = T1.TESTDIV ");
        stb.append("         AND T2.RECEPTNO BETWEEN T1.S_RECEPTNO AND T1.E_RECEPTNO ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("         AND BASE.APPLICANTDIV = T2.APPLICANTDIV ");
        stb.append("         AND BASE.EXAMNO       = T2.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND T1.TESTDIV = '" + _param._testdiv + "' ");
        stb.append("     AND T1.EXAMHALLCD IN " + SQLUtils.whereIn(true, _param._category_name)  + " ");
        if (null != _param._noinfSt) {
            stb.append("     AND BASE.EXAMNO >= '" + _param._noinfSt + "' ");
        }
        if (null != _param._noinfEd) {
            stb.append("     AND BASE.EXAMNO <= '" + _param._noinfEd + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.EXAMHALLCD, ");
        if ("2".equals(_param._applicantdiv)) {
            stb.append("     T2.RECEPTNO, ");
        }
        stb.append("     BASE.EXAMNO ");
        return stb.toString();
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List examnoList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
            // log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                if ("2".equals(_param._applicantdiv)) {
                    examnoList.add(rs.getString("RECEPTNO"));
                } else {
                    examnoList.add(rs.getString("EXAMNO"));
                }
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        if (examnoList.isEmpty()) {
            return;
        }

        final File imageFile = _param.getImageFile(db2);

        svf.VrSetForm("KNJL300R.frm", 4);
        final int maxCol = 2;
        for (int i = 0; i < (_param._porow - 1) * maxCol + (_param._pocol - 1); i++) {
            svf.VrsOut("DUMMY", "1"); // 空欄用
            svf.VrEndRecord();
        }
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度";
        final String title = nendo + "　" + StringUtils.defaultString(_param._testdivName);
        final String titleField = getMS932ByteLength(title) > 30 ? "TITLE2" : "TITLE1";
        for (final Iterator it = examnoList.iterator(); it.hasNext();){
            final String examno = (String) it.next();
            svf.VrsOut(titleField, title); // タイトル
            svf.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名
            svf.VrsOut("EXAM_NO", examno); // 受験番号
            if (null != imageFile) {
                svf.VrsOut("SCHOOL_LOGO", imageFile.getPath()); // 受験番号
            }
            svf.VrEndRecord();
        }
        _hasData = true;
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 64559 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _date;
        final String[] _category_name;
        final String _noinfSt;
        final String _noinfEd;
        final int _porow;
        final int _pocol;

        final String _applicantdivName;
        final String _testdivName;
        final String _dateStr;
        final String _schoolName;
        final String _documentroot;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _date = request.getParameter("CTRL_DATE");
            _dateStr = getDateStr(_date);
            _category_name = request.getParameterValues("category_name");
            for (int i = 0; i < _category_name.length; i++) {
                _category_name[i] = _category_name[i].substring(0, _category_name[i].indexOf('-'));
            }
            _documentroot = request.getParameter("DOCUMENTROOT");
            _noinfSt = _category_name.length == 1 && !StringUtils.isBlank(request.getParameter("NOINF_ST")) ? request.getParameter("NOINF_ST") : null;
            _noinfEd = _category_name.length== 1 && !StringUtils.isBlank(request.getParameter("NOINF_ED")) ? request.getParameter("NOINF_ED") : null;
            _porow = _category_name.length == 1 && NumberUtils.isDigits(request.getParameter("POROW")) ? Integer.parseInt(request.getParameter("POROW")) : 1;
            _pocol = _category_name.length == 1 && NumberUtils.isDigits(request.getParameter("POCOL")) ? Integer.parseInt(request.getParameter("POCOL")) : 1;

            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantdiv);
            if ("2".equals(_applicantdiv)) {
                _testdivName = StringUtils.defaultString(getNameMst(db2, "ABBV1", "L024", _testdiv)) + StringUtils.defaultString(getNameMst(db2, "ABBV2", "L024", _testdiv));
            } else {
                _testdivName = StringUtils.defaultString(getNameMst(db2, "ABBV1", "L004", _testdiv)) + "入学試験";
            }
            _schoolName = getCertifSchoolDat(db2, "SCHOOL_NAME", _applicantdiv);
        }

        private String getDateStr(final String date) {
            if (null == date) {
                return null;
            }
            return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
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
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getCertifSchoolDat(final DB2UDB db2, final String field, final String applicantDiv) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                StringBuffer sql = new StringBuffer();
                sql.append(" SELECT " + field + " ");
                sql.append(" FROM CERTIF_SCHOOL_DAT ");
                sql.append(" WHERE YEAR = '" + _entexamyear + "' ");
                if ("2".equals(applicantDiv)) {
                    sql.append("   AND CERTIF_KINDCD = '105' ");
                } else {
                    sql.append("   AND CERTIF_KINDCD = '106' ");
                }
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private File getImageFile(final DB2UDB db2) {
            if (null == _documentroot) {
                return null;
            }
            String imagepath = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST WHERE CTRL_NO = '01' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    imagepath = rs.getString("IMAGEPATH");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            if (null == imagepath) {
                return null;
            }
            final File file = new File(_documentroot + "/" + imagepath + "/SCHOOLLOGO.jpg");
            log.fatal(" file = " + file.getAbsolutePath() + ", exists? = " + file.exists());
            if (!file.exists()) {
                return null;
            }
            return file;
        }
    }
}

// eof

