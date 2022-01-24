// kanji=漢字
/*
 * $Id: 2008832bb2ac623a7acbc1257b5058757dbe8d94 $
 *
 * 作成日: 2009/10/26 16:52:07 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.IOException;
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

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 2008832bb2ac623a7acbc1257b5058757dbe8d94 $
 */
public class KNJA033 {

    private static final Log log = LogFactory.getLog("KNJA033.class");

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
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        if (_param._printSinkyu || _param._printSotugyo) {
            for (int i = 0; i < _param._hrClass.length; i++) {
                final String hrClass = _param._hrClass[i];
                if (null == hrClass) break;
                log.fatal("クラス=" + hrClass);
                printClass(db2, svf, hrClass);
            }
        } else {
            printRyunen(db2, svf, null);
        }
    }

    private void printClass(final DB2UDB db2, final Vrw32alp svf, final String hrClass) throws SQLException {
        final List printStudents = getPrintStudent(db2, hrClass);
        svf.VrSetForm(_param.getFormId(), 4);
        int cntMan = 0;
        int cntWoman = 0;
        int cntTotal = 0;
        int cntRecord = 0;
        for (final Iterator it = printStudents.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            //１行出力
            if (0 < cntRecord) svf.VrEndRecord();
            //ヘッダ
            svf.VrsOut("NENDO", _param._printYear);
            svf.VrsOut("SELECT", _param.getTitle());
            svf.VrsOut("DATE", _param._printDate);
            svf.VrsOut("HR_NAME", student._hrName);
            svf.VrsOut("TR_CD1", student._staffName);
            //明細
            svf.VrsOut("ATTENDNO", student.getAttendNo());
            svf.VrsOut("SCHREGNO", student._schregno);
            svf.VrsOut("NAME", student._name);
            svf.VrsOut("SEX", student._sexName);
            svf.VrsOut("CREDIT", student._compCredit);
            svf.VrsOut("GET_CREDIT", student._getCredit);
            //カウント
            if ("1".equals(student._sex)) cntMan++;
            if ("2".equals(student._sex)) cntWoman++;
            cntTotal = cntMan + cntWoman;
            cntRecord++;
        }
        if (0 < cntRecord) {
            //最終行出力
            svf.VrsOut("BOY"  , String.valueOf(cntMan));
            svf.VrsOut("GIRL" , String.valueOf(cntWoman));
            svf.VrsOut("TOTAL", String.valueOf(cntTotal));
            svf.VrEndRecord();
            _hasData = true;
        }
    }

    private void printRyunen(final DB2UDB db2, final Vrw32alp svf, final String hrClass) throws SQLException {
        final List printStudents = getPrintStudent(db2, hrClass);
        svf.VrSetForm(_param.getFormId(), 4);
        int cntRecord = 0;
        for (final Iterator it = printStudents.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            //１行出力
            if (0 < cntRecord) svf.VrEndRecord();
            //ヘッダ
            svf.VrsOut("NENDO", _param._printYear);
            svf.VrsOut("DATE", _param._printDate);
            //明細
            svf.VrsOut("ATTENDNO", student._hrName + student.getAttendNoBan());
            svf.VrsOut("SCHREGNO", student._schregno);
            svf.VrsOut("NAME", student._name);
            svf.VrsOut("SEX", student._sexName);
            svf.VrsOut("CREDIT", student._compCredit);
            svf.VrsOut("GET_CREDIT", student._getCredit);
            //カウント
            cntRecord++;
        }
        if (0 < cntRecord) {
            //最終行出力
            svf.VrEndRecord();
            _hasData = true;
        }
    }

    private List getPrintStudent(final DB2UDB db2, final String hrClass) throws SQLException {
        final List rtnList = new ArrayList();
        PreparedStatement psSt = null;
        ResultSet rsSt = null;
        final String studentSql = getStudentSql(hrClass);
        try {
            psSt = db2.prepareStatement(studentSql);
            rsSt = psSt.executeQuery();
            while (rsSt.next()) {
                final String schregno = rsSt.getString("SCHREGNO");
                final String grade = rsSt.getString("GRADE");
                final String hrName = rsSt.getString("HR_NAME");
                final String staffName = rsSt.getString("STAFFNAME");
                final String attendno = rsSt.getString("ATTENDNO");
                final String name = rsSt.getString("NAME");
                final String sex = rsSt.getString("SEX");
                final String sexName = rsSt.getString("SEX_NAME");
                final Student student = new Student(
                        schregno,
                        grade,
                        hrName,
                        staffName,
                        attendno,
                        name,
                        sex,
                        sexName);
                rtnList.add(student);
            }
        } finally {
            DbUtils.closeQuietly(null, psSt, rsSt);
            db2.commit();
        }
        try {
            for (final Iterator it = rtnList.iterator(); it.hasNext();) {
                Student student = (Student) it.next();
                student._compCredit = student.setCompCredit(db2, student._schregno);
                student._getCredit = student.setGetCredit(db2, student._schregno);
            }
        } finally {
            db2.commit();
        }
        return rtnList;
    }

    private String getStudentSql(final String hrClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T2.HR_NAME, ");
        stb.append("     L1.STAFFNAME, ");
        stb.append("     T3.NAME, ");
        stb.append("     T3.SEX, ");
        stb.append("     Z002.ABBV1 AS SEX_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("                                   AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("                                   AND T2.GRADE = T1.GRADE ");
        stb.append("                                   AND T2.HR_CLASS = T1.HR_CLASS ");
        stb.append("     LEFT JOIN STAFF_MST L1 ON L1.STAFFCD = T2.TR_CD1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("                                  AND value(T3.GRD_DIV,'0') NOT IN ('2','3') ");
        if (_param._printSotugyo) {
            stb.append("                              AND value(T3.GRD_DIV,'0') = '1' ");
        }
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("                            AND Z002.NAMECD2 = T3.SEX ");
        if (_param._printSinkyu || _param._printRyunen) {
            stb.append("     INNER JOIN CLASS_FORMATION_DAT T4 ON T4.SCHREGNO = T1.SCHREGNO ");
            stb.append("                                      AND T4.YEAR = '" + _param._nextYear + "' ");
            stb.append("                                      AND T4.SEMESTER = '1' ");
            if (_param._printSinkyu) {
                stb.append("                                  AND value(T4.REMAINGRADE_FLG,'0') <> '1' ");
            }
            if (_param._printRyunen) {
                stb.append("                                  AND value(T4.REMAINGRADE_FLG,'0') = '1' ");
            }
        }
        if ("1".equals(_param._use_prg_schoolkind)) {
            if (!StringUtils.isBlank(_param._selectSchoolKind)) {
                stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ");
            }
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
            stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ");
            stb.append("   AND GDAT.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        if (_param._printSinkyu || _param._printSotugyo) {
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + hrClass + "' ");
        }
        if ("1".equals(_param._use_prg_schoolkind)) {
            if (!StringUtils.isBlank(_param._selectSchoolKind)) {
                stb.append("        AND GDAT.SCHOOL_KIND IN " + _param._selectSchoolKindSql + "  ");
            }
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    private class Student {
        final String _schregno;
        final String _grade;
        final String _hrName;
        final String _staffName;
        final String _attendno;
        final String _name;
        final String _sex;
        final String _sexName;
        String _compCredit;
        String _getCredit;

        Student(final String schregno,
                final String grade,
                final String hrName,
                final String staffName,
                final String attendno,
                final String name,
                final String sex,
                final String sexName
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrName = hrName;
            _staffName = staffName;
            _attendno = attendno;
            _name = name;
            _sex = sex;
            _sexName = sexName;
        }

        /**
         * {@inheritDoc}
         */
        private String getAttendNo() {
            if (null == _attendno) return "";
            return String.valueOf(Integer.parseInt(_attendno));
        }

        /**
         * {@inheritDoc}
         */
        private String getAttendNoBan() {
            if (null == _attendno) return "  番";
            return String.valueOf(Integer.parseInt(_attendno)) + "番";
        }

        private String setCompCredit(final DB2UDB db2, final String schregno) throws SQLException {
            String credit = "0";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = getCompCreditSql(schregno);
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    credit = rs.getString("COMP_CREDIT");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return credit;
        }

        private String getCompCreditSql(final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH T_STUDYREC AS ( ");
            stb.append("     SELECT ");
            stb.append("         SCHREGNO, ");
            stb.append("         SUM(VALUE(COMP_CREDIT, 0)) AS STUDYREC_CREDIT ");
            stb.append("     FROM ");
            stb.append("         SCHREG_STUDYREC_DAT ");
            stb.append("     WHERE ");
            stb.append("         YEAR < '" + _param._year + "' AND ");
            stb.append("         SCHREGNO = '" + schregno + "' ");
            stb.append("     GROUP BY ");
            stb.append("         SCHREGNO ");
            stb.append("     ) ");
            stb.append(" , T_RECORD AS ( ");
            stb.append("     SELECT ");
            stb.append("         SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ");
            }
            stb.append("         SUBCLASSCD, ");
            stb.append("         VALUE(SUM(RECORD_CREDIT), 0) AS RECORD_CREDIT ");
            stb.append("     FROM ( ");
            stb.append("     SELECT ");
            stb.append("         SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ");
            }
            stb.append("         SUBCLASSCD, ");
            stb.append("         COMP_CREDIT AS RECORD_CREDIT ");
            stb.append("     FROM ");
            if (_param.isUseRecordScoreDat()) {
                stb.append("     RECORD_SCORE_DAT T1 ");
            } else {
                stb.append("     RECORD_DAT T1 ");
            }
            stb.append("     WHERE ");
            stb.append("         YEAR = '" + _param._year + "' ");
            if (("1".equals(_param._useCurriculumcd))) {
                stb.append("         AND (CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD) NOT IN (SELECT DISTINCT ");
                stb.append("                                    ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD ");
                stb.append("                                FROM ");
                stb.append("                                    SUBCLASS_REPLACE_COMBINED_DAT ");
                stb.append("                                WHERE ");
                stb.append("                                    REPLACECD = '1' ");
                stb.append("                                    AND YEAR = '" + _param._year + "' ");
                stb.append("                               ) ");
            } else {
                stb.append("         AND SUBCLASSCD NOT IN (SELECT DISTINCT ");
                stb.append("                                    ATTEND_SUBCLASSCD ");
                stb.append("                                FROM ");
                stb.append("                                    SUBCLASS_REPLACE_COMBINED_DAT ");
                stb.append("                                WHERE ");
                stb.append("                                    REPLACECD = '1' ");
                stb.append("                                    AND YEAR = '" + _param._year + "' ");
                stb.append("                               ) ");
            }
            stb.append("         AND SCHREGNO = '" + schregno + "' ");
            if (_param.isUseRecordScoreDat()) {
                stb.append("     AND SEMESTER = '9' ");
                stb.append("     AND TESTKINDCD = '99' ");
                stb.append("     AND TESTITEMCD = '00' ");
                if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(_param._useTestCountflg) || "TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV".equals(_param._useTestCountflg)) {
                    stb.append("     AND SCORE_DIV = '09' ");
                } else {
                    stb.append("     AND SCORE_DIV = '00' ");
                }
            }
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("         SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ");
            }
            stb.append("         SUBCLASSCD, ");
            stb.append("     T1.CREDITS AS RECORD_CREDIT ");
            stb.append(" FROM ");
            stb.append("     SCHREG_QUALIFIED_DAT T1 ");
            stb.append(" WHERE T1.YEAR = '" + _param._year + "' ");
            stb.append("         AND SCHREGNO = '" + schregno + "' ");
            stb.append("     ) T1 ");
            stb.append("  GROUP BY  ");
            stb.append("         SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ");
            }
            stb.append("         SUBCLASSCD ");
            stb.append(" ) ");
            
            stb.append(" , T_LOGIN_YEAR AS ( ");
            stb.append("     SELECT ");
            stb.append("         SCHREGNO, VALUE(T1.COMP_CREDIT, 0) AS CREDIT ");
            stb.append("     FROM ");
            stb.append("         SCHREG_STUDYREC_DAT T1 ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + _param._year + "' ");
            stb.append("         AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append("     UNION ALL ");
            stb.append("     SELECT ");
            stb.append("         SCHREGNO, VALUE(T1.RECORD_CREDIT, 0) AS CREDIT ");
            stb.append("     FROM ");
            stb.append("         T_RECORD T1 ");
            stb.append("     WHERE ");
            stb.append("         NOT EXISTS ( ");
            stb.append("           SELECT 'X' FROM SCHREG_STUDYREC_DAT I1 WHERE ");
            stb.append("         I1.YEAR = '" + _param._year + "' AND ");
            stb.append("         I1.SCHREGNO = '" + schregno + "' AND ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         I1.CLASSCD = T1.CLASSCD AND ");
                stb.append("         I1.SCHOOL_KIND = T1.SCHOOL_KIND AND ");
                stb.append("         I1.CURRICULUM_CD = T1.CURRICULUM_CD AND ");
            }
            stb.append("         I1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         ) ");

            stb.append("  ) ");


            stb.append(" SELECT ");
            stb.append("     VALUE((SELECT STUDYREC_CREDIT FROM T_STUDYREC), 0) ");
            stb.append("   + VALUE((SELECT SUM(CREDIT) FROM T_LOGIN_YEAR), 0) AS COMP_CREDIT ");
            stb.append(" FROM ");
            stb.append("     (SELECT DISTINCT SCHREGNO FROM T_STUDYREC");
            stb.append("      UNION ");
            stb.append("      SELECT DISTINCT SCHREGNO FROM T_LOGIN_YEAR");
            stb.append("     ) T1 ");
            return stb.toString();
        }

        private String setGetCredit(final DB2UDB db2, final String schregno) throws SQLException {
            String credit = "0";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = getGetCreditSql(schregno);
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    credit = rs.getString("GET_CREDIT");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return credit;
        }

        private String getGetCreditSql(final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SCHREGNO, ");
            stb.append("     SUM(VALUE(GET_CREDIT, 0)) + SUM(VALUE(ADD_CREDIT, 0)) AS GET_CREDIT ");
            stb.append(" FROM ");
            stb.append("     SCHREG_STUDYREC_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR <= '" + _param._year + "' AND ");
            stb.append("     SCHREGNO = '" + schregno + "' ");
            stb.append(" GROUP BY ");
            stb.append("     SCHREGNO ");
            return stb.toString();
        }

    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 66691 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final boolean _printSinkyu; // クラス別進級生一覧を出力するか
        private final boolean _printSotugyo;// クラス別卒業生一覧を出力するか
        private final boolean _printRyunen; // 留年生一覧を出力するか
        private String[] _hrClass;
        private final String _nextYear;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _printDate; //作成日
        private final String _printYear; //年度
        private final String _useCurriculumcd;
        private boolean _isSeireki;
        /** 名称マスタ（学校等） */
        private String _z010Name1; //学校
        private String _z010NameSpare1; //record_score_dat使用フラグ
        private String _curriculumCd = null;
        final String _useSchool_KindField;
        final String _SCHOOLKIND;
        final String _useTestCountflg;
        final String _use_prg_schoolkind;
        final String _selectSchoolKind;
        private String _selectSchoolKindSql;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("THIS_YEAR");
            _semester = request.getParameter("MAX_SEMESTER");
            String output = request.getParameter("OUTPUT"); // 1:クラス別進級生一覧, 2:クラス別卒業生一覧, 3:留年生一覧
            _printSinkyu  = "1".equals(output);
            _printSotugyo = "2".equals(output);
            _printRyunen  = "3".equals(output);
            if (_printSinkyu || _printSotugyo) {
                _hrClass = request.getParameterValues("category_selected");
            }
            _nextYear = request.getParameter("NEXT_YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            setSeirekiFlg(db2);
            _printYear = printYear(_year);
            _printDate = printDate(_ctrlDate);
            setNameMst(db2);
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _useTestCountflg = request.getParameter("useTestCountflg");
            _selectSchoolKind = request.getParameter("selectSchoolKind");
            if (!StringUtils.isBlank(_selectSchoolKind)) {
                StringBuffer stb = new StringBuffer("('");
                final String[] split = StringUtils.split(_selectSchoolKind, ":");
                if (null != split) {
                    for (int i = 0; i < split.length; i++) {
                        stb.append(split[i] + "', '");
                    }
                }
                _selectSchoolKindSql = stb.append("')").toString();
            }
        }

        private void setSeirekiFlg(final DB2UDB db2) {
            try {
                _isSeireki = false;
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while( rs.next() ){
                    if (rs.getString("NAME1").equals("2")) _isSeireki = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
        }

        private String printDate(final String date) {
            if (null == date) {
                return "";
            }
            if (_isSeireki) {
                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
            } else {
                return KNJ_EditDate.h_format_JP(date);
            }
        }

        private String printYear(final String year) {
            if (null == year) {
                return "";
            }
            if (_isSeireki) {
                return year + "年度";
            } else {
                return nao_package.KenjaProperties.gengou(Integer.parseInt(year)) + "年度";
            }
        }

        private void setNameMst(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlNameMst("Z010", "00"));
                rs = ps.executeQuery();
                if (rs.next()) {
                    _z010Name1 = rs.getString("NAME1");
                    _z010NameSpare1 = rs.getString("NAMESPARE1");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("学校=" + _z010Name1 + "、成績テーブル=" + getRecordTable());
        }

        private String sqlNameMst(final String namecd1, final String namecd2) {
            final String sql = " SELECT "
                + "     * "
                + " FROM "
                + "     NAME_MST "
                + " WHERE "
                + "         NAMECD1 = '" + namecd1 + "' "
                + "     AND NAMECD2 = '" + namecd2 + "'";
            return sql;
        }

        private String getRecordTable() {
            return isUseRecordScoreDat() ? "RECORD_SCORE_DAT" : "RECORD_DAT";
        }

        /**
         * record_score_dat使用か?。
         * @return is not nullならtrue
         */
        private boolean isUseRecordScoreDat() {
            return _z010NameSpare1 != null;
        }

        private String getFormId() {
            if (_printRyunen) return "KNJA033_2.frm";
            return "KNJA033_1.frm";
        }

        private String getTitle() {
            return (_printSinkyu) ? "進級生" : "卒業生";
        }
    }
}

// eof
