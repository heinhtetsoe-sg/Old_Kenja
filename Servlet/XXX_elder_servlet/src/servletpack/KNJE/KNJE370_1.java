// kanji=漢字
/*
 * $Id: 0338024be8dd1c171df8898c1ffc9f1a2e3ff3b4 $
 *
 * 作成日: 2009/10/21 10:40:44 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJE.KNJE370.Param;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 0338024be8dd1c171df8898c1ffc9f1a2e3ff3b4 $
 */
public class KNJE370_1 {

    private static final Log log = LogFactory.getLog("KNJE370_1.class");

//    private boolean _hasData;

    Param _param;
    DB2UDB _db2;
    Vrw32alp _svf;

    /**
     * コンストラクタ。
     * @param param
     * @param db2
     * @param svf
     */
    public KNJE370_1(
            final Param param,
            final DB2UDB db2,
            final Vrw32alp svf
    ) {
        _param = param;
        _db2 = db2;
        _svf = svf;
    }

    public boolean printMain(final DB2UDB db2) throws SQLException {
        boolean hasData = false;
        final List printSingakus = getPrintSingaku();
        _svf.VrSetForm("KNJE370_1.frm", 1);
        String befSchregNo = "";
        int fieldCnt = 1;
        for (final Iterator it = printSingakus.iterator(); it.hasNext();) {
            final Singaku singaku = (Singaku) it.next();
            if ("1".equals(_param._kaipage) && !befSchregNo.equals(singaku._schregno) && hasData) {
                _svf.VrEndPage();
                fieldCnt = 1;
            } else if (fieldCnt > 25) {
                _svf.VrEndPage();
                fieldCnt = 1;
            }
            //ヘッダ
            _svf.VrsOut("NENDO", _param.changePrintYear(db2, _param._year));
            _svf.VrsOut("SEL_PUBPRIV_KIND", _param._kubunName);
            _svf.VrsOut("RESULT1", _param._gouhiName);
            _svf.VrsOut("DATE", _param.changePrintDate(db2, _param._ctrlDate));
            //明細
            _svf.VrsOutn("SCHREGNO", fieldCnt, singaku._schregno);
            final String nameField = (KNJ_EditEdit.getMS932ByteLength(singaku._hrName) > 15) ? "2": "";
            _svf.VrsOutn("HR_NAME" + nameField, fieldCnt, singaku._hrName + singaku.getAttendNo());
            _svf.VrsOutn("NAME_SHOW", fieldCnt, singaku._name);
            _svf.VrsOutn("PUBPRIV_KIND", fieldCnt, singaku._schoolGroupName);
            _svf.VrsOutn("LOCATION", fieldCnt, singaku._prefName);
            _svf.VrsOutn(getFieldName(singaku._statName, "SCHOOL_NAME1", "SCHOOL_NAME2", 30), fieldCnt, singaku._statName);
            _svf.VrsOutn("FACULTY", fieldCnt, singaku._facultyName);
            _svf.VrsOutn(getFieldName(singaku._departmentName, getFieldName(singaku._departmentName, "MAJORCD", "MAJORCD2", 12), "MAJORCD3_1", 18), fieldCnt, singaku._departmentName);
            _svf.VrsOutn(getFieldName(singaku._howtoexamName, getFieldName(singaku._howtoexamName, "EXAM_METHOD", "EXAM_METHOD2", 12), "EXAM_METHOD3_1", 18), fieldCnt, singaku._howtoexamName);
            _svf.VrsOutn("RESULT2", fieldCnt, singaku._decisionName);
            _svf.VrsOutn("EXAM_NO", fieldCnt, singaku._examNo);
            _svf.VrsOutn("COURSE_AHEAD", fieldCnt, singaku._planstatName);
            _svf.VrsOutn("SCHEDULE", fieldCnt, singaku._programName);
            _svf.VrsOutn("REMARK", fieldCnt, singaku._formName);
            _svf.VrsOutn("ANONYMOUS", fieldCnt, singaku._chkMark);

            hasData = true;
            befSchregNo = singaku._schregno;
            fieldCnt++;
        }
        if (hasData) {
            _svf.VrEndPage();
        }
        return hasData;
    }

    /**
     * 文字数によるフォームフィールド名を取得
     * @param str：データ
     * @param field1：フィールド１（小さい方）
     * @param field2：フィールド２（大きい方）
     * @param len：フィールド１の文字数
     */
    private String getFieldName(final String str, final String field1, final String field2, final int len) {
        if (null == str) return field1;
        return len < KNJ_EditEdit.getMS932ByteLength(str) ? field2 : field1;
    }

    private List getPrintSingaku() throws SQLException {
        final List rtnList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String singakuSql = getSingakuSql();
        log.debug(" sql = " + singakuSql);
        try {
            ps = _db2.prepareStatement(singakuSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String attendno = rs.getString("ATTENDNO");
                final String gradHrName = rs.getString("G_HR_NAME");
                final String hrName = StringUtils.defaultString(rs.getString("HR_NAME"));
                final String name = rs.getString("NAME");
                final String senkouKind = rs.getString("SENKOU_KIND");
                final String statCd = rs.getString("STAT_CD");
                final String schoolGroup = rs.getString("SCHOOL_GROUP");
                final String facultyCd = rs.getString("FACULTYCD");
                final String departmentCd = rs.getString("DEPARTMENTCD");
                final String prefCd = rs.getString("PREF_CD");
                final String howtoexam = rs.getString("HOWTOEXAM");
                final String decision = rs.getString("DECISION");
                final String planstat = rs.getString("PLANSTAT");
                final String statName = rs.getString("STAT_NAME");
                final String schoolGroupName = rs.getString("SCHOOL_GROUP_NAME");
                final String facultyName = rs.getString("FACULTYNAME");
                final String departmentName = rs.getString("DEPARTMENTNAME");
                final String prefName = rs.getString("PREF_NAME");
                final String formName = rs.getString("FORM_NAME");
                final String programName = rs.getString("PROGRAM_NAME");
                final String howtoexamName = rs.getString("HOWTOEXAM_NAME");
                final String decisionName = rs.getString("DECISION_NAME");
                final String planstatName = rs.getString("PLANSTAT_NAME");
                final String examNo = rs.getString("EXAMNO");
                final String chkMark = rs.getString("CHKMARK");
                final Singaku singaku = new Singaku(
                        schregno,
                        grade,
                        hrClass,
                        attendno,
                        gradHrName,
                        hrName,
                        name,
                        senkouKind,
                        statCd,
                        schoolGroup,
                        facultyCd,
                        departmentCd,
                        prefCd,
                        howtoexam,
                        decision,
                        planstat,
                        statName,
                        schoolGroupName,
                        facultyName,
                        departmentName,
                        prefName,
                        formName,
                        programName,
                        howtoexamName,
                        decisionName,
                        planstatName,
                        examNo,
                        chkMark);
                rtnList.add(singaku);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            _db2.commit();
        }
        return rtnList;
    }

    private String getSingakuSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH EXAM_CALENDAR_FORM AS ( ");
        stb.append(" SELECT ");
        stb.append("     YEAR, SCHOOL_CD, FACULTYCD, DEPARTMENTCD, ADVERTISE_DIV, PROGRAM_CD, FORM_CD, MAX(PROGRAM_NAME) AS PROGRAM_NAME, MAX(FORM_NAME) AS FORM_NAME ");
        stb.append(" FROM ");
        stb.append("     COLLEGE_EXAM_CALENDAR T1 ");
        stb.append(" GROUP BY ");
        stb.append("     YEAR, SCHOOL_CD, FACULTYCD, DEPARTMENTCD, ADVERTISE_DIV, PROGRAM_CD, FORM_CD  ");
        stb.append(" ), EXAM_CALENDAR_PROGRAM AS ( ");
        stb.append(" SELECT ");
        stb.append("     YEAR, SCHOOL_CD, FACULTYCD, DEPARTMENTCD, ADVERTISE_DIV, PROGRAM_CD, MAX(PROGRAM_NAME) AS PROGRAM_NAME ");
        stb.append(" FROM ");
        stb.append("     EXAM_CALENDAR_FORM T1 ");
        stb.append(" GROUP BY ");
        stb.append("     YEAR, SCHOOL_CD, FACULTYCD, DEPARTMENTCD, ADVERTISE_DIV, PROGRAM_CD  ");
        stb.append(" ), KISOTSU AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SEQ, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     '' AS GRADE, ");
        stb.append("     '' AS HR_CLASS, ");
        stb.append("     '' AS ATTENDNO, ");
        stb.append("     '2' AS SORT_DIV, ");
        stb.append("     '既卒' AS G_HR_NAME, ");
        stb.append("     VALUE(FISCALYEAR(G_BASE.GRD_DATE),'') || '年度卒' || G_HDAT.HR_NAME AS HR_NAME, ");
        stb.append("     I1.NAME, ");
        stb.append("     T1.SENKOU_KIND, ");
        stb.append("     T1.STAT_CD, ");
        stb.append("     L1.SCHOOL_NAME as STAT_NAME, ");
        stb.append("     T1.SCHOOL_GROUP, ");
        stb.append("     E012.NAME1 as SCHOOL_GROUP_NAME, ");
        stb.append("     T1.FACULTYCD, ");
        stb.append("     L2.FACULTYNAME, ");
        stb.append("     T1.DEPARTMENTCD, ");
        stb.append("     L3.DEPARTMENTNAME, ");
        stb.append("     T1.PREF_CD, ");
        stb.append("     L4.PREF_NAME, ");
        stb.append("     ECF.FORM_NAME, ");
        stb.append("     ECP.PROGRAM_NAME, ");
        stb.append("     T1.HOWTOEXAM, ");
        stb.append("     E002.NAME1 as HOWTOEXAM_NAME, ");
        stb.append("     T1.DECISION, ");
        stb.append("     E005.NAME1 as DECISION_NAME, ");
        stb.append("     T1.PLANSTAT, ");
        stb.append("     AFT_GRAD_D.REMARK9 AS EXAMNO, ");
        stb.append("     E006.NAME1 as PLANSTAT_NAME, ");
        stb.append("     CASE WHEN AFT_GRAD_D5.REMARK1 = '1' THEN '〇' ELSE '' END AS CHKMARK ");
        stb.append(" FROM ");
        stb.append("     AFT_GRAD_COURSE_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST I1 ON I1.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN GRD_BASE_MST G_BASE ON T1.SCHREGNO = G_BASE.SCHREGNO ");
        stb.append("     LEFT JOIN GRD_REGD_HDAT G_HDAT ON T1.YEAR = G_HDAT.YEAR ");
        stb.append("                                   AND G_BASE.GRD_SEMESTER = G_HDAT.SEMESTER");
        stb.append("                                   AND G_BASE.GRD_GRADE    = G_HDAT.GRADE");
        stb.append("                                   AND G_BASE.GRD_HR_CLASS = G_HDAT.HR_CLASS");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT I2 ON I2.SCHREGNO = T1.SCHREGNO AND I2.YEAR = '" + _param._year + "' AND I2.SEMESTER = '" + _param._semester + "' ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT I3 ON I3.YEAR = I2.YEAR AND I3.SEMESTER = I2.SEMESTER AND I3.GRADE = I2.GRADE AND I3.HR_CLASS = I2.HR_CLASS ");
        stb.append("     INNER JOIN COLLEGE_MST L1 ON L1.SCHOOL_CD = T1.STAT_CD ");
        stb.append("     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT AFT_GRAD_D ON T1.YEAR = AFT_GRAD_D.YEAR ");
        stb.append("          AND T1.SEQ = AFT_GRAD_D.SEQ ");
        stb.append("          AND AFT_GRAD_D.DETAIL_SEQ = 1 ");
        stb.append("     LEFT JOIN COLLEGE_FACULTY_MST L2 ON L2.SCHOOL_CD = T1.STAT_CD AND L2.FACULTYCD = T1.FACULTYCD ");
        stb.append("     LEFT JOIN COLLEGE_DEPARTMENT_MST L3 ON L3.SCHOOL_CD = T1.STAT_CD AND L3.FACULTYCD = T1.FACULTYCD AND L3.DEPARTMENTCD = T1.DEPARTMENTCD ");
        stb.append("     LEFT JOIN EXAM_CALENDAR_PROGRAM ECP ON ECP.YEAR = T1.YEAR AND ECP.SCHOOL_CD = T1.STAT_CD AND ECP.FACULTYCD = T1.FACULTYCD AND ECP.DEPARTMENTCD = T1.DEPARTMENTCD ");
        stb.append("         AND ECP.ADVERTISE_DIV = AFT_GRAD_D.REMARK1 AND ECP.PROGRAM_CD = AFT_GRAD_D.REMARK2 ");
        stb.append("     LEFT JOIN EXAM_CALENDAR_FORM ECF ON ECF.YEAR = T1.YEAR AND ECF.SCHOOL_CD = T1.STAT_CD AND ECF.FACULTYCD = T1.FACULTYCD AND ECF.DEPARTMENTCD = T1.DEPARTMENTCD ");
        stb.append("         AND ECF.ADVERTISE_DIV = AFT_GRAD_D.REMARK1 AND ECF.PROGRAM_CD = AFT_GRAD_D.REMARK2 AND ECF.FORM_CD = AFT_GRAD_D.REMARK3 ");
        stb.append("     LEFT JOIN PREF_MST L4 ON L4.PREF_CD = T1.PREF_CD ");
        stb.append("     LEFT JOIN NAME_MST E002 ON E002.NAMECD1 = 'E002' AND E002.NAMECD2 = T1.HOWTOEXAM ");
        stb.append("     LEFT JOIN NAME_MST E005 ON E005.NAMECD1 = 'E005' AND E005.NAMECD2 = T1.DECISION ");
        stb.append("     LEFT JOIN NAME_MST E006 ON E006.NAMECD1 = 'E006' AND E006.NAMECD2 = T1.PLANSTAT ");
        stb.append("     LEFT JOIN NAME_MST E012 ON E012.NAMECD1 = 'E012' AND E012.NAMECD2 = T1.SCHOOL_GROUP ");
        stb.append("     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT AFT_GRAD_D5 ON AFT_GRAD_D5.YEAR = T1.YEAR ");
        stb.append("      AND AFT_GRAD_D5.SEQ = T1.SEQ ");
        stb.append("      AND AFT_GRAD_D5.DETAIL_SEQ = 5");
        if ("MIX".equals(_param._gouhiCd2)) {
            stb.append("     LEFT JOIN NAME_MST NM ");
            stb.append("           ON NM.NAMECD1    = '" + _param._gouhiCd1 + "' ");
            stb.append("          AND NM.NAMESPARE2 = '" + _param._gouhiCd3 + "' ");
        }
        stb.append(" WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SENKOU_KIND = '" + _param._senkouKind + "' ");
        stb.append("     AND I2.SCHREGNO IS NULL ");

        if (_param._typeSelMaxCnt > _param._typeSelected.length && _param._typeSelected.length > 0) {
            stb.append(" AND ( ");
            String qconnectStr = "";
            for (int ii = 0;ii < _param._typeSelected.length; ii++) {
                final String kubunCd1 = _param._typeSelected[ii].substring(0,4);
                final String kubunCd2 = _param._typeSelected[ii].substring(5);
                if (_param.isNamecdE012(kubunCd1)) {
                    stb.append(qconnectStr + " T1.SCHOOL_GROUP = '" + kubunCd2 + "' ");
                }
                qconnectStr = " OR ";
            }
            stb.append(" ) ");
        }
        if (_param.isNamecdE005(_param._gouhiCd1)) {
            if ("MIX".equals(_param._gouhiCd2)) {
                stb.append("     AND T1.DECISION = NM.NAMECD2 ");
            } else {
                stb.append("     AND T1.DECISION = '" + _param._gouhiCd2 + "' ");
            }
        }
        if (_param.isNamecdE006(_param._gouhiCd1)) {
            stb.append("     AND T1.PLANSTAT = '" + _param._gouhiCd2 + "' ");
        }
        if ("2".equals(_param._dataDiv)) {
            stb.append("     AND T1.SCHREGNO IN " + _param._classSelectedIn + " ");
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SEQ, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     I2.GRADE, ");
        stb.append("     I2.HR_CLASS, ");
        stb.append("     I2.ATTENDNO, ");
        stb.append("     '1' AS SORT_DIV, ");
        stb.append("     '' AS G_HR_NAME, ");
        stb.append("     I3.HR_NAME, ");
        stb.append("     I1.NAME, ");
        stb.append("     T1.SENKOU_KIND, ");
        stb.append("     T1.STAT_CD, ");
        stb.append("     L1.SCHOOL_NAME as STAT_NAME, ");
        stb.append("     T1.SCHOOL_GROUP, ");
        stb.append("     E012.NAME1 as SCHOOL_GROUP_NAME, ");
        stb.append("     T1.FACULTYCD, ");
        stb.append("     L2.FACULTYNAME, ");
        stb.append("     T1.DEPARTMENTCD, ");
        stb.append("     L3.DEPARTMENTNAME, ");
        stb.append("     T1.PREF_CD, ");
        stb.append("     L4.PREF_NAME, ");
        stb.append("     ECF.FORM_NAME, ");
        stb.append("     ECP.PROGRAM_NAME, ");
        stb.append("     T1.HOWTOEXAM, ");
        stb.append("     E002.NAME1 as HOWTOEXAM_NAME, ");
        stb.append("     T1.DECISION, ");
        stb.append("     E005.NAME1 as DECISION_NAME, ");
        stb.append("     T1.PLANSTAT, ");
        stb.append("     AFT_GRAD_D.REMARK9 AS EXAMNO, ");
        stb.append("     E006.NAME1 as PLANSTAT_NAME, ");
        stb.append("     CASE WHEN AFT_GRAD_D5.REMARK1 = '1' THEN '〇' ELSE '' END AS CHKMARK ");
        stb.append(" FROM ");
        stb.append("     AFT_GRAD_COURSE_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST I1 ON I1.SCHREGNO = T1.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT I2 ON I2.SCHREGNO = T1.SCHREGNO AND I2.YEAR = '" + _param._year + "' AND I2.SEMESTER = '" + _param._semester + "' ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT I3 ON I3.YEAR = I2.YEAR AND I3.SEMESTER = I2.SEMESTER AND I3.GRADE = I2.GRADE AND I3.HR_CLASS = I2.HR_CLASS ");
        stb.append("     INNER JOIN COLLEGE_MST L1 ON L1.SCHOOL_CD = T1.STAT_CD ");
        stb.append("     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT AFT_GRAD_D ON T1.YEAR = AFT_GRAD_D.YEAR ");
        stb.append("          AND T1.SEQ = AFT_GRAD_D.SEQ ");
        stb.append("          AND AFT_GRAD_D.DETAIL_SEQ = 1 ");
        stb.append("     LEFT JOIN COLLEGE_FACULTY_MST L2 ON L2.SCHOOL_CD = T1.STAT_CD AND L2.FACULTYCD = T1.FACULTYCD ");
        stb.append("     LEFT JOIN COLLEGE_DEPARTMENT_MST L3 ON L3.SCHOOL_CD = T1.STAT_CD AND L3.FACULTYCD = T1.FACULTYCD AND L3.DEPARTMENTCD = T1.DEPARTMENTCD ");
        stb.append("     LEFT JOIN EXAM_CALENDAR_PROGRAM ECP ON ECP.YEAR = T1.YEAR AND ECP.SCHOOL_CD = T1.STAT_CD AND ECP.FACULTYCD = T1.FACULTYCD AND ECP.DEPARTMENTCD = T1.DEPARTMENTCD ");
        stb.append("         AND ECP.ADVERTISE_DIV = AFT_GRAD_D.REMARK1 AND ECP.PROGRAM_CD = AFT_GRAD_D.REMARK2 ");
        stb.append("     LEFT JOIN EXAM_CALENDAR_FORM ECF ON ECF.YEAR = T1.YEAR AND ECF.SCHOOL_CD = T1.STAT_CD AND ECF.FACULTYCD = T1.FACULTYCD AND ECF.DEPARTMENTCD = T1.DEPARTMENTCD ");
        stb.append("         AND ECF.ADVERTISE_DIV = AFT_GRAD_D.REMARK1 AND ECF.PROGRAM_CD = AFT_GRAD_D.REMARK2 AND ECF.FORM_CD = AFT_GRAD_D.REMARK3 ");
        stb.append("     LEFT JOIN PREF_MST L4 ON L4.PREF_CD = T1.PREF_CD ");
        stb.append("     LEFT JOIN NAME_MST E002 ON E002.NAMECD1 = 'E002' AND E002.NAMECD2 = T1.HOWTOEXAM ");
        stb.append("     LEFT JOIN NAME_MST E005 ON E005.NAMECD1 = 'E005' AND E005.NAMECD2 = T1.DECISION ");
        stb.append("     LEFT JOIN NAME_MST E006 ON E006.NAMECD1 = 'E006' AND E006.NAMECD2 = T1.PLANSTAT ");
        stb.append("     LEFT JOIN NAME_MST E012 ON E012.NAMECD1 = 'E012' AND E012.NAMECD2 = T1.SCHOOL_GROUP ");
        stb.append("     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT AFT_GRAD_D5 ON AFT_GRAD_D5.YEAR = T1.YEAR ");
        stb.append("          AND AFT_GRAD_D5.SEQ = T1.SEQ ");
        stb.append("          AND AFT_GRAD_D5.DETAIL_SEQ = 5 ");
        if ("MIX".equals(_param._gouhiCd2)) {
            stb.append("     LEFT JOIN NAME_MST NM ");
            stb.append("           ON NM.NAMECD1    = '" + _param._gouhiCd1 + "' ");
            stb.append("          AND NM.NAMESPARE2 = '" + _param._gouhiCd3 + "' ");
        }
        stb.append(" WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SENKOU_KIND = '" + _param._senkouKind + "' ");
        if (_param._typeSelMaxCnt > _param._typeSelected.length && _param._typeSelected.length > 0) {
            stb.append(" AND ( ");
            String qconnectStr = "";
            for (int ii = 0;ii < _param._typeSelected.length; ii++) {
                final String kubunCd1 = _param._typeSelected[ii].substring(0,4);
                final String kubunCd2 = _param._typeSelected[ii].substring(5);
                if (_param.isNamecdE012(kubunCd1)) {
                    stb.append(qconnectStr + " T1.SCHOOL_GROUP = '" + kubunCd2 + "' ");
                }
                qconnectStr = " OR ";
            }
            stb.append(" ) ");
        }
        if (_param.isNamecdE005(_param._gouhiCd1)) {
            if ("MIX".equals(_param._gouhiCd2)) {
                stb.append("     AND T1.DECISION = NM.NAMECD2 ");
            } else {
                stb.append("     AND T1.DECISION = '" + _param._gouhiCd2 + "' ");
            }
        }
        if (_param.isNamecdE006(_param._gouhiCd1)) {
            stb.append("     AND T1.PLANSTAT = '" + _param._gouhiCd2 + "' ");
        }
        if ("1".equals(_param._dataDiv)) {
            stb.append("     AND I2.GRADE || I2.HR_CLASS IN " + _param._classSelectedIn + " ");
        } else {
            stb.append("     AND I2.SCHREGNO IN " + _param._classSelectedIn + " ");
        }
        if (_param._isKisotsu || "2".equals(_param._dataDiv)) {
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     KISOTSU ");
        }
        stb.append(" ORDER BY ");
        stb.append("     SORT_DIV, ");
        stb.append("     GRADE, ");
        stb.append("     HR_CLASS, ");
        stb.append("     ATTENDNO, ");
        stb.append("     SEQ ");

        return stb.toString();
    }

    private class Singaku {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _gradHrName;
        final String _hrName;
        final String _name;
        final String _senkouKind;
        final String _statCd;
        final String _schoolGroup;
        final String _facultyCd;
        final String _departmentCd;
        final String _prefCd;
        final String _howtoexam;
        final String _decision;
        final String _planstat;
        final String _statName;
        final String _schoolGroupName;
        final String _facultyName;
        final String _departmentName;
        final String _prefName;
        final String _formName;
        final String _programName;
        final String _howtoexamName;
        final String _decisionName;
        final String _planstatName;
        final String _examNo;
        final String _chkMark;

        Singaku(final String schregno,
                final String grade,
                final String hrClass,
                final String attendno,
                final String gradHrName,
                final String hrName,
                final String name,
                final String senkouKind,
                final String statCd,
                final String schoolGroup,
                final String facultyCd,
                final String departmentCd,
                final String prefCd,
                final String howtoexam,
                final String decision,
                final String planstat,
                final String statName,
                final String schoolGroupName,
                final String facultyName,
                final String departmentName,
                final String prefName,
                final String formName,
                final String programName,
                final String howtoexamName,
                final String decisionName,
                final String planstatName,
                final String examNo,
                final String chkMark
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendno = attendno;
            _gradHrName = gradHrName;
            _hrName = hrName;
            _name = name;
            _senkouKind = senkouKind;
            _statCd = statCd;
            _schoolGroup = schoolGroup;
            _facultyCd = facultyCd;
            _departmentCd = departmentCd;
            _prefCd = prefCd;
            _howtoexam = howtoexam;
            _decision = decision;
            _planstat = planstat;
            _statName = statName;
            _schoolGroupName = schoolGroupName;
            _facultyName = facultyName;
            _departmentName = departmentName;
            _prefName = prefName;
            _formName = formName;
            _programName = programName;
            _howtoexamName = howtoexamName;
            _decisionName = decisionName;
            _planstatName = planstatName;
            _examNo = examNo;
            _chkMark = chkMark;
        }

        /**
         * {@inheritDoc}
         */
        public String getAttendNo() {
            if ("既卒".equals(_gradHrName)) {
                return "";
            }
            if (null == _attendno || "".equals(_attendno)) return "  番";
            return String.valueOf(Integer.parseInt(_attendno)) + "番";
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return _schregno + " = " + _name;
        }
    }
}

// eof
