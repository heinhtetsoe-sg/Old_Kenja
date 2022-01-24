/*
 * $Id: e1c61d7b3bb5237a8b36db7599f0b16c574e8f3e $
 *
 * 作成日: 2018/06/04
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJM505D {

    private static final Log log = LogFactory.getLog(KNJM505D.class);

    private static final String HYOUTEI_TESTCD = "990009"; //クラス指定
    private static final String CLASS_SHITEI = "1"; //クラス指定
    private static final String KOJIN_SHITEI = "2"; //個人指定

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
        final List printList = getSchList(db2);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = getRecordScoreHistDatSql();
            ps = db2.prepareStatement(sql);

            for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
                final SchregData schData = (SchregData) iterator.next();

                svf.VrSetForm("KNJM505D.frm", 4);

                svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear)) + "年度　" + _param._semeName + "　成績表");     // タイトル
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._ctrlDate));     // 作成日付

                svf.VrsOut("SCHREGNO", schData._schregNo);     // 学籍番号
                final String nameField = KNJ_EditEdit.getMS932ByteLength(schData._name) > 30 ? "3": KNJ_EditEdit.getMS932ByteLength(schData._name) > 20 ? "2": "1";
                svf.VrsOut("NAME" + nameField, schData._name); // 氏名
                final String kanaField = KNJ_EditEdit.getMS932ByteLength(schData._kana) > 30 ? "3": KNJ_EditEdit.getMS932ByteLength(schData._kana) > 20 ? "2": "1";
                svf.VrsOut("KANA" + kanaField, schData._kana); // かな

                svf.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名
                svf.VrsOut("PRINCIPAL_NAME", _param._jobName + "　" + _param._principalName); // 校長
                svf.VrsOut("TUTOR", schData._staffName); // チューター

                int totalGetCredit = 0;
                ps.setString(1, schData._schregNo);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subClassName       = rs.getString("SUBCLASSNAME");
                    final String staffName          = StringUtils.defaultString(rs.getString("STAFFNAME"));
                    final String credit             = StringUtils.defaultString(rs.getString("CREDITS"));
                    final String schoolingLimitCnt  = StringUtils.defaultString(rs.getString("SCHOOLING_LIMIT_CNT"));
                    final String schoolingCnt       = StringUtils.defaultString(rs.getString("SCHOOLING_CNT"));
                    final String repoLimitCnt       = StringUtils.defaultString(rs.getString("REPO_LIMIT_CNT"));
                    final String reportCnt          = StringUtils.defaultString(rs.getString("REPORT_CNT"));
                    final String value              = StringUtils.defaultString(rs.getString("VALUE"));
                    final String getCredit          = StringUtils.defaultString(rs.getString("GET_CREDIT"));
                    totalGetCredit += "".equals(getCredit) ? 0: Integer.parseInt(getCredit);

                    final String subField = KNJ_EditEdit.getMS932ByteLength(subClassName) > 20 ? "2": "1";
                    svf.VrsOut("SUBCLASS_NAME" + subField, subClassName); // 開講科目名
                    svf.VrsOut("TR_NAME", staffName);           // 担当
                    svf.VrsOut("CREDIT", credit);               // 単位数
                    svf.VrsOut("SCHOOLING", schoolingLimitCnt); // 必要スクーリング回数
                    svf.VrsOut("INTERVIEW", schoolingCnt);      // 面接指導出席時間
                    svf.VrsOut("REPORT", repoLimitCnt);         // レポート枚数
                    svf.VrsOut("CORRECT", reportCnt);           // 添削指導提出枚数
                    svf.VrsOut("VALUE", value);                 // 評定
                    svf.VrsOut("GET_CREDIT", getCredit);        // 認定単位

                    svf.VrEndRecord();
                }
                svf.VrsOut("TOTAL_GET_CREDIT", String.valueOf(totalGetCredit)); // 合計修得単位
                svf.VrEndRecord();

                _hasData = true;
            }

        } catch (Exception ex) {
            log.error("setSvfout set error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getRecordScoreHistDatSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH CHAIR_STF AS ( ");
        stb.append("         SELECT ");
        stb.append("             CHAIRCD, ");
        stb.append("             MIN(STAFFCD) AS STAFFCD ");
        stb.append("         FROM ");
        stb.append("             CHAIR_STF_DAT ");
        stb.append("         WHERE ");
        stb.append("                 YEAR      = '" + _param._ctrlYear + "' ");
        stb.append("             AND SEMESTER  = '" + _param._semester + "' ");
        stb.append("             AND CHARGEDIV = 1 "); //正担任
        stb.append("         GROUP BY ");
        stb.append("             CHAIRCD ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     STDD.SCHREGNO, ");
        stb.append("     STDD.CHAIRCD, ");
        stb.append("     SUBC.SUBCLASSNAME, ");
        stb.append("     STAF.STAFFNAME, ");
        stb.append("     CRDT.CREDITS, ");
        stb.append("     CRRS.SCHOOLING_LIMIT_CNT, ");
        stb.append("     ATND.SCHOOLING_CNT, ");
        stb.append("     CRRS.REPO_LIMIT_CNT, ");
        stb.append("     REPO.REPORT_CNT, ");
        stb.append("     RCRD.VALUE, ");
        stb.append("     RCRD.GET_CREDIT ");
        stb.append(" FROM ");
        stb.append("     (SELECT DISTINCT YEAR, SEMESTER, CHAIRCD, SCHREGNO FROM CHAIR_STD_DAT) STDD ");
        stb.append("     INNER JOIN CHAIR_DAT CHIR ON STDD.YEAR     = CHIR.YEAR ");
        stb.append("                              AND STDD.SEMESTER = CHIR.SEMESTER ");
        stb.append("                              AND STDD.CHAIRCD  = CHIR.CHAIRCD ");
        stb.append("     INNER JOIN SUBCLASS_MST SUBC ON CHIR.CLASSCD       = SUBC.CLASSCD ");
        stb.append("                                 AND CHIR.SCHOOL_KIND   = SUBC.SCHOOL_KIND ");
        stb.append("                                 AND CHIR.CURRICULUM_CD = SUBC.CURRICULUM_CD ");
        stb.append("                                 AND CHIR.SUBCLASSCD    = SUBC.SUBCLASSCD ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR     = STDD.YEAR ");
        stb.append("                                    AND REGD.SEMESTER = STDD.SEMESTER ");
        stb.append("                                    AND REGD.SCHREGNO = STDD.SCHREGNO ");
        stb.append("     LEFT JOIN CREDIT_MST CRDT ON CRDT.YEAR          = STDD.YEAR ");
        stb.append("                              AND CRDT.COURSECD      = REGD.COURSECD ");
        stb.append("                              AND CRDT.MAJORCD       = REGD.MAJORCD ");
        stb.append("                              AND CRDT.GRADE         = REGD.GRADE ");
        stb.append("                              AND CRDT.COURSECODE    = REGD.COURSECODE ");
        stb.append("                              AND CRDT.CLASSCD       = CHIR.CLASSCD ");
        stb.append("                              AND CRDT.SCHOOL_KIND   = CHIR.SCHOOL_KIND ");
        stb.append("                              AND CRDT.CURRICULUM_CD = CHIR.CURRICULUM_CD ");
        stb.append("                              AND CRDT.SUBCLASSCD    = CHIR.SUBCLASSCD ");
        stb.append("     LEFT JOIN CHAIR_STF CSTF ON CSTF.CHAIRCD = STDD.CHAIRCD ");
        stb.append("     LEFT JOIN STAFF_MST STAF ON STAF.STAFFCD = CSTF.STAFFCD ");
        stb.append("     LEFT JOIN CHAIR_CORRES_SEMES_DAT CRRS ON CRRS.YEAR          = STDD.YEAR ");
        stb.append("                                          AND CRRS.SEMESTER      = STDD.SEMESTER ");
        stb.append("                                          AND CRRS.CHAIRCD       = STDD.CHAIRCD ");
        stb.append("                                          AND CRRS.CLASSCD       = CHIR.CLASSCD ");
        stb.append("                                          AND CRRS.SCHOOL_KIND   = CHIR.SCHOOL_KIND ");
        stb.append("                                          AND CRRS.CURRICULUM_CD = CHIR.CURRICULUM_CD ");
        stb.append("                                          AND CRRS.SUBCLASSCD    = CHIR.SUBCLASSCD ");
        stb.append("     LEFT JOIN SCH_ATTEND_SEMES_DAT ATND ON ATND.YEAR     = STDD.YEAR ");
        stb.append("                                        AND ATND.SEMESTER = STDD.SEMESTER ");
        stb.append("                                        AND ATND.SCHREGNO = STDD.SCHREGNO ");
        stb.append("                                        AND ATND.CHAIRCD  = STDD.CHAIRCD ");
        stb.append("     LEFT JOIN REP_PRESENT_SEMES_DAT REPO ON REPO.YEAR          = STDD.YEAR ");
        stb.append("                                         AND REPO.SEMESTER      = STDD.SEMESTER ");
        stb.append("                                         AND REPO.CLASSCD       = CHIR.CLASSCD ");
        stb.append("                                         AND REPO.SCHOOL_KIND   = CHIR.SCHOOL_KIND ");
        stb.append("                                         AND REPO.CURRICULUM_CD = CHIR.CURRICULUM_CD ");
        stb.append("                                         AND REPO.SUBCLASSCD    = CHIR.SUBCLASSCD ");
        stb.append("                                         AND REPO.SCHREGNO      = STDD.SCHREGNO ");
        stb.append("     LEFT JOIN V_RECORD_SCORE_HIST_DAT RCRD ON RCRD.YEAR          = STDD.YEAR ");
        stb.append("                                           AND RCRD.SEMESTER      = STDD.SEMESTER ");
        stb.append("                                           AND RCRD.TESTKINDCD || RCRD.TESTITEMCD || RCRD.SCORE_DIV = '" + HYOUTEI_TESTCD + "' ");
        stb.append("                                           AND RCRD.CLASSCD       = CHIR.CLASSCD ");
        stb.append("                                           AND RCRD.SCHOOL_KIND   = CHIR.SCHOOL_KIND ");
        stb.append("                                           AND RCRD.CURRICULUM_CD = CHIR.CURRICULUM_CD ");
        stb.append("                                           AND RCRD.SUBCLASSCD    = CHIR.SUBCLASSCD ");
        stb.append("                                           AND RCRD.SCHREGNO      = STDD.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("         STDD.YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("     AND STDD.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND SUBC.CLASSCD <= '90' ");
        stb.append("     AND STDD.SCHREGNO = ? ");
        stb.append(" ORDER BY ");
        stb.append("     SUBC.SHOWORDER, ");
        stb.append("     SUBC.SUBCLASSCD ");

        return stb.toString();
    }

    private List getSchList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSchListSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregNo   = rs.getString("SCHREGNO");
                final String name       = rs.getString("NAME");
                final String kana       = rs.getString("NAME_KANA");
                final String staffName  = rs.getString("STAFFNAME");

                final SchregData schregData = new SchregData(schregNo, name, kana, staffName);
                retList.add(schregData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSchListSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     STAF.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     INNER JOIN  SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR     = REGD.YEAR ");
        stb.append("                                    AND HDAT.SEMESTER = REGD.SEMESTER ");
        stb.append("                                    AND HDAT.GRADE    = REGD.GRADE ");
        stb.append("                                    AND HDAT.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     LEFT JOIN STAFF_MST STAF ON STAF.STAFFCD     = HDAT.TR_CD1 ");
        stb.append(" WHERE ");
        stb.append("         REGD.YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        if (CLASS_SHITEI.equals(_param._choice)) {
            stb.append("     AND REGD.GRADE || REGD.HR_CLASS IN " + _param._selectedIn + " ");
            stb.append(" ORDER BY ");
            stb.append("     REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO ");
        } else if (KOJIN_SHITEI.equals(_param._choice)) {
            stb.append("     AND REGD.SCHREGNO IN " + _param._selectedIn + " ");
            stb.append(" ORDER BY ");
            stb.append("     REGD.SCHREGNO ");
        }

        return stb.toString();
    }

    private class SchregData {
        final String _schregNo;
        final String _name;
        final String _kana;
        final String _staffName;

        public SchregData(
                final String schregNo,
                final String name,
                final String kana,
                final String staffName
        ) {
            _schregNo  = schregNo;
            _name      = name;
            _kana      = kana;
            _staffName = staffName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65896 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _ctrlYear;
        private final String _semester;
        private final String _semeName;
        private final String _choice;
        private final String _selectedIn;
        private final String _ctrlDate;
        private final String _schoolName;
        private final String _jobName;
        private final String _principalName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear       = request.getParameter("CTRL_YEAR");
            _semester       = request.getParameter("SEMESTER");
            _semeName       = getSemeName(db2, _ctrlYear, _semester);
            _choice         = request.getParameter("CHOICE");
            _ctrlDate       = request.getParameter("LOGIN_DATE");
            _schoolName     = getCertifSchoolDat(db2, _ctrlYear, "SCHOOL_NAME");
            _jobName        = getCertifSchoolDat(db2, _ctrlYear, "JOB_NAME");
            _principalName  = getCertifSchoolDat(db2, _ctrlYear, "PRINCIPAL_NAME");

            final String[] selected = request.getParameterValues("CATEGORY_SELECTED");
            _selectedIn = getSelectedIn(selected);
        }

        private String getSemeName(final DB2UDB db2, final String year, final String semester) {
            String retSemeName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    retSemeName = rs.getString("SEMESTERNAME");
                }
            } catch (SQLException ex) {
                log.debug("getSemesterMst exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSemeName;
        }

        private String getCertifSchoolDat(final DB2UDB db2, final String year, final String fieldName) {
            String retFieldName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT "+ fieldName +" FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + year + "' AND CERTIF_KINDCD = '104' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    retFieldName = StringUtils.trim(rs.getString(fieldName));
                }
            } catch (SQLException ex) {
                log.debug("getCertifSchoolDat exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retFieldName;
        }

        private String getSelectedIn(final String[] selected) {
            StringBuffer stb = new StringBuffer();
            stb.append("(");
            for (int i = 0; i < selected.length; i++) {
                if (0 < i) stb.append(",");
                stb.append("'" + selected[i] + "'");
            }
            stb.append(")");
            return stb.toString();
        }

    }
}

// eof
