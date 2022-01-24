// kanji=漢字
/*
 * $Id: c15cecd97f7300675baa015e135ea2df3979a02c $
 *
 * 作成日: 2009/10/21 10:40:44 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJE.KNJE370A.Param;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: c15cecd97f7300675baa015e135ea2df3979a02c $
 */
public class KNJE370A_1 {

    private static final Log log = LogFactory.getLog("KNJE370A_1.class");

    private boolean _hasData;

    Param _param;
    DB2UDB _db2;
    Vrw32alp _svf;

    /**
     * コンストラクタ。
     * @param param
     * @param db2
     * @param svf
     */
    public KNJE370A_1(
            final Param param,
            final DB2UDB db2,
            final Vrw32alp svf
    ) {
        _param = param;
        _db2 = db2;
        _svf = svf;
    }

    public boolean printMain() throws SQLException {
        boolean hasData = false;
        final List printSingakus = getPrintSingaku();
        _svf.VrSetForm("KNJE370A.frm", 1);
        String befSchregNo = "";
        int fieldCnt = 1;
        for (final Iterator it = printSingakus.iterator(); it.hasNext();) {
            final Singaku singaku = (Singaku) it.next();
            if (!hasData) {
                setTitle(singaku);
            }
            if (!befSchregNo.equals(singaku._schregno) && hasData) {
                _svf.VrEndPage();
                setTitle(singaku);
                fieldCnt = 1;
            } else if (fieldCnt > 12) {
                _svf.VrEndPage();
                setTitle(singaku);
                fieldCnt = 1;
            }

            //明細(「全てnullまたは空」以外は出力)
            if (!((null == singaku._schoolGroupName  || "".equals(singaku._schoolGroupName))
                && (null == singaku._statName  || "".equals(singaku._statName))
                && (null == singaku._facultyName  || "".equals(singaku._facultyName))
                && (null == singaku._departmentName  || "".equals(singaku._departmentName))
                && (null == singaku._decisionName || "".equals(singaku._decisionName))
                && (null == singaku._planstatName || "".equals(singaku._planstatName)))) {
                _svf.VrsOutn("PUBPRIV_KIND", fieldCnt, singaku._schoolGroupName);
                _svf.VrsOutn(getFieldName(singaku._statName, "SCHOOL_NAME1", "SCHOOL_NAME2", 15), fieldCnt, singaku._statName);
                _svf.VrsOutn("FACULTY", fieldCnt, singaku._facultyName);
                _svf.VrsOutn("MAJORCD", fieldCnt, singaku._departmentName);
                _svf.VrsOutn("EXAM_METHOD", fieldCnt, singaku._howtoexamName);
                _svf.VrsOutn("RESULT2", fieldCnt, singaku._decisionName);
                _svf.VrsOutn("COURSE_AHEAD", fieldCnt, singaku._planstatName);
                fieldCnt++;
            }

            hasData = true;
            befSchregNo = singaku._schregno;
        }
        if (hasData) {
            //ヘッダ
            _svf.VrEndPage();
        }
        return hasData;
    }

    private void setTitle(final Singaku singaku) {
        _svf.VrsOut("TITLE", "合否・進学確認連絡票");
        _svf.VrsOut("DATE", _param.changePrintDate(_param._ctrlDate));
        //年度組番
        log.debug(singaku._hrName + singaku.getAttendNo());
        if (!"既卒".equals(singaku._gradHrName)) {
            _svf.VrsOut("HR_NAME", singaku._hrName + singaku.getAttendNo());
        } else {
            _svf.VrsOut("HR_NAME2", singaku._hrName + singaku.getAttendNo());
        }
        //氏名
        _svf.VrsOut("NAME", singaku._name);
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
        return len < str.length() ? field2 : field1;
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
                final String attendno = rs.getString("ATTENDNO");
                final String gradHrName = rs.getString("G_HR_NAME");
                final String hrName = StringUtils.defaultString(rs.getString("HR_NAME"));
                final String name = rs.getString("NAME");
                final String statName = rs.getString("STAT_NAME");
                final String schoolGroupName = rs.getString("SCHOOL_GROUP_NAME");
                final String facultyName = rs.getString("FACULTYNAME");
                final String departmentName = rs.getString("DEPARTMENTNAME");
                final String howtoexamName = rs.getString("HOWTOEXAM_NAME");
                final String decisionName = rs.getString("DECISION_NAME");
                final String planstatName = rs.getString("PLANSTAT_NAME");
                final Singaku singaku = new Singaku(
                        schregno,
                        attendno,
                        gradHrName,
                        hrName,
                        name,
                        statName,
                        schoolGroupName,
                        facultyName,
                        departmentName,
                        howtoexamName,
                        decisionName,
                        planstatName
                        );
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
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEQ, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     '' AS GRADE, ");
        stb.append("     G_BASE.GRD_HR_CLASS AS HR_CLASS, ");
        stb.append("     G_BASE.GRD_ATTENDNO AS ATTENDNO, ");
        stb.append("     '2' AS SORT_DIV, ");
        stb.append("     '既卒' AS G_HR_NAME, ");
        stb.append("     (CASE WHEN G_BASE.GRD_TERM IS NULL THEN '' ELSE G_BASE.GRD_TERM END) || '期卒' || (CASE WHEN G_HDAT.HR_NAME IS NULL THEN '' ELSE G_HDAT.HR_NAME END) AS HR_NAME, ");
        stb.append("     I1.NAME, ");
        stb.append("     T1.STAT_CD, ");
        stb.append("     L1.SCHOOL_NAME as STAT_NAME, ");
        stb.append("     E012.NAME1 as SCHOOL_GROUP_NAME, ");
        stb.append("     L2.FACULTYNAME, ");
        stb.append("     L3.DEPARTMENTNAME, ");
        stb.append("     E002.NAME1 as HOWTOEXAM_NAME, ");
        stb.append("     E005.NAME1 as DECISION_NAME, ");
        stb.append("     AFT_GRAD_D.REMARK9 AS EXAMNO, ");
        stb.append("     E006.NAME1 as PLANSTAT_NAME ");
        stb.append(" FROM ");
        stb.append("     AFT_GRAD_COURSE_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST I1 ON I1.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN (SELECT GBDAT.*, CASE WHEN MONTH(GBDAT.GRD_DATE) <= 3 THEN YEAR(GBDAT.GRD_DATE)-1 ELSE YEAR(GBDAT.GRD_DATE) END AS GRD_YEAR FROM GRD_BASE_MST GBDAT) AS G_BASE ON T1.SCHREGNO = G_BASE.SCHREGNO ");
        stb.append("     LEFT JOIN GRD_REGD_HDAT G_HDAT ON G_BASE.GRD_YEAR = INTEGER(G_HDAT.YEAR) ");
        stb.append("                                   AND G_BASE.GRD_SEMESTER = G_HDAT.SEMESTER");
        stb.append("                                   AND G_BASE.GRD_GRADE    = G_HDAT.GRADE");
        stb.append("                                   AND G_BASE.GRD_HR_CLASS = G_HDAT.HR_CLASS");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT I2 ON I2.SCHREGNO = T1.SCHREGNO AND I2.YEAR = '" + _param._year + "' AND I2.SEMESTER = '" + _param._semester + "' ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT I3 ON I3.YEAR = I2.YEAR AND I3.SEMESTER = I2.SEMESTER AND I3.GRADE = I2.GRADE AND I3.HR_CLASS = I2.HR_CLASS ");
        stb.append("     LEFT JOIN COLLEGE_MST L1 ON L1.SCHOOL_CD = T1.STAT_CD ");
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
        stb.append(" WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND I2.SCHREGNO IS NULL ");
        stb.append("     AND T1.SENKOU_KIND = '0' ");
        if ("2".equals(_param._dataDiv)) {
            stb.append("     AND T1.SCHREGNO IN " + _param._classSelectedIn + " ");
            stb.append("     AND NOT EXISTS(SELECT ");
            stb.append("             'X' ");
            stb.append("          FROM ");
            stb.append("             SCHREG_REGD_DAT E1 ");
            stb.append("        WHERE ");
            stb.append("              T1.YEAR     = E1.YEAR AND ");
            stb.append("              E1.SEMESTER = '1' AND ");
            stb.append("              T1.SCHREGNO = E1.SCHREGNO ");
            stb.append("     ) ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.SCHREGNO ASC ");

        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     I2.SEQ, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     '1' AS SORT_DIV, ");
        stb.append("     '' AS G_HR_NAME, ");
        stb.append("     I3.HR_NAME, ");
        stb.append("     I1.NAME, ");
        stb.append("     I2.STAT_CD, ");
        stb.append("     L1.SCHOOL_NAME as STAT_NAME, ");
        stb.append("     E012.NAME1 as SCHOOL_GROUP_NAME, ");
        stb.append("     L2.FACULTYNAME, ");
        stb.append("     L3.DEPARTMENTNAME, ");
        stb.append("     E002.NAME1 as HOWTOEXAM_NAME, ");
        stb.append("     E005.NAME1 as DECISION_NAME, ");
        stb.append("     AFT_GRAD_D.REMARK9 AS EXAMNO, ");
        stb.append("     E006.NAME1 as PLANSTAT_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST I1 ON I1.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN AFT_GRAD_COURSE_DAT I2 ON T1.SCHREGNO = I2.SCHREGNO AND I2.YEAR = '" + _param._year + "' ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT I3 ON I3.YEAR = T1.YEAR AND I3.SEMESTER = T1.SEMESTER AND I3.GRADE = T1.GRADE AND I3.HR_CLASS = T1.HR_CLASS ");
        stb.append("     LEFT JOIN COLLEGE_MST L1 ON L1.SCHOOL_CD = I2.STAT_CD ");
        stb.append("     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT AFT_GRAD_D ON I2.YEAR = AFT_GRAD_D.YEAR ");
        stb.append("          AND I2.SEQ = AFT_GRAD_D.SEQ ");
        stb.append("          AND AFT_GRAD_D.DETAIL_SEQ = 1 ");
        stb.append("     LEFT JOIN COLLEGE_FACULTY_MST L2 ON L2.SCHOOL_CD = I2.STAT_CD AND L2.FACULTYCD = I2.FACULTYCD ");
        stb.append("     LEFT JOIN COLLEGE_DEPARTMENT_MST L3 ON L3.SCHOOL_CD = I2.STAT_CD AND L3.FACULTYCD = I2.FACULTYCD AND L3.DEPARTMENTCD = I2.DEPARTMENTCD ");
        stb.append("     LEFT JOIN EXAM_CALENDAR_PROGRAM ECP ON ECP.YEAR = I2.YEAR AND ECP.SCHOOL_CD = I2.STAT_CD AND ECP.FACULTYCD = I2.FACULTYCD AND ECP.DEPARTMENTCD = I2.DEPARTMENTCD ");
        stb.append("         AND ECP.ADVERTISE_DIV = AFT_GRAD_D.REMARK1 AND ECP.PROGRAM_CD = AFT_GRAD_D.REMARK2 ");
        stb.append("     LEFT JOIN EXAM_CALENDAR_FORM ECF ON ECF.YEAR = I2.YEAR AND ECF.SCHOOL_CD = I2.STAT_CD AND ECF.FACULTYCD = I2.FACULTYCD AND ECF.DEPARTMENTCD = I2.DEPARTMENTCD ");
        stb.append("         AND ECF.ADVERTISE_DIV = AFT_GRAD_D.REMARK1 AND ECF.PROGRAM_CD = AFT_GRAD_D.REMARK2 AND ECF.FORM_CD = AFT_GRAD_D.REMARK3 ");
        stb.append("     LEFT JOIN NAME_MST E002 ON E002.NAMECD1 = 'E002' AND E002.NAMECD2 = I2.HOWTOEXAM ");
        stb.append("     LEFT JOIN NAME_MST E005 ON E005.NAMECD1 = 'E005' AND E005.NAMECD2 = I2.DECISION ");
        stb.append("     LEFT JOIN NAME_MST E006 ON E006.NAMECD1 = 'E006' AND E006.NAMECD2 = I2.PLANSTAT ");
        stb.append("     LEFT JOIN NAME_MST E012 ON E012.NAMECD1 = 'E012' AND E012.NAMECD2 = I2.SCHOOL_GROUP ");
        stb.append(" WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
        if ("1".equals(_param._dataDiv)) {
            stb.append("     AND T1.GRADE || T1.HR_CLASS IN " + _param._classSelectedIn + " ");
        } else {
            stb.append("     AND T1.SCHREGNO IN " + _param._classSelectedIn + " ");
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
        stb.append("     SCHREGNO, ");
        stb.append("     STAT_CD, ");
        stb.append("     YEAR, ");
        stb.append("     SEQ ");

        return stb.toString();
    }

    private class Singaku {
        final String _schregno;
        final String _attendno;
        final String _gradHrName;
        final String _hrName;
        final String _name;
        final String _statName;
        final String _schoolGroupName;
        final String _facultyName;
        final String _departmentName;
        final String _howtoexamName;
        final String _decisionName;
        final String _planstatName;

        Singaku(final String schregno,
                final String attendno,
                final String gradHrName,
                final String hrName,
                final String name,
                final String statName,
                final String schoolGroupName,
                final String facultyName,
                final String departmentName,
                final String howtoexamName,
                final String decisionName,
                final String planstatName
        ) {
            _schregno = schregno;
            _attendno = attendno;
            _gradHrName = gradHrName;
            _hrName = hrName;
            _name = name;
            _statName = statName;
            _schoolGroupName = schoolGroupName;
            _facultyName = facultyName;
            _departmentName = departmentName;
            _howtoexamName = howtoexamName;
            _decisionName = decisionName;
            _planstatName = planstatName;
        }

        /**
         * {@inheritDoc}
         */
        public String getAttendNo() {
//            if ("既卒".equals(_gradHrName)) {
//                return "";
//            }
            if (null == _attendno || "".equals(_attendno)) {
            	return "  番";
            }
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
