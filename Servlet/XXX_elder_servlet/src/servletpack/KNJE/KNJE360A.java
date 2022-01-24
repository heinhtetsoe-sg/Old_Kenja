// kanji=漢字
/*
 * $Id: 9cb103fd73e0b5889bcba261a242cb508a3d616e $
 *
 * 作成日: 2009/10/21 8:52:18 - JST
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 9cb103fd73e0b5889bcba261a242cb508a3d616e $
 */
public class KNJE360A {

    private static final Log log = LogFactory.getLog("KNJE360A.class");
    private static final String KISOTSU = "ZZZZZ";

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

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List printSingakus = Singaku.getPrintSingaku(db2, _param);
        final List pageList = getPageList(printSingakus, 25);
        for (int pi = 0; pi < pageList.size(); pi++) {

            svf.VrSetForm("KNJE360A.frm", 1);

            svf.VrsOut("NENDO", _param.changePrintYear(_param._ctrlYear));
            svf.VrsOut("DATE", _param.changePrintDate(_param._ctrlDate));

            final List singakuList = (List) pageList.get(pi);
            for (int i = 0; i < singakuList.size(); i++) {
                final Singaku singaku = (Singaku) singakuList.get(i);
                final int linet = i + 1;
                svf.VrsOutn("SCHREGNO", linet, singaku._schregno);
                final String nameField = (KNJ_EditEdit.getMS932ByteLength(singaku._hrName) > 15) ? "2": "";
                svf.VrsOutn("HR_NAME" + nameField, linet, StringUtils.defaultString(singaku._hrName) + singaku.getAttendNo());
                svf.VrsOutn("NAME_SHOW", linet, singaku._name);
                svf.VrsOutn("PUBPRIV_KIND", linet, singaku._schoolGroupName);
                svf.VrsOutn("LOCATION", linet, singaku._prefName);
                svf.VrsOutn(getFieldName(singaku._statName, "SCHOOL_NAME1", "SCHOOL_NAME2", 30), linet, singaku._statName);
                svf.VrsOutn("FACULTY", linet, singaku._facultyName);
                svf.VrsOutn("MAJORCD", linet, singaku._departmentName);
                svf.VrsOutn("EXAM_METHOD", linet, singaku._howtoexamName);
                svf.VrsOutn("RESULT2", linet, singaku._decisionName);
                svf.VrsOutn("EXAM_NO", linet, singaku._examNo);
                svf.VrsOutn("COURSE_AHEAD", linet, singaku._planstatName);
                svf.VrsOutn("SCHEDULE", linet, singaku._programName);
                svf.VrsOutn("REMARK", linet, singaku._formName);
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }

    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
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
        return KNJ_EditEdit.getMS932ByteLength(str) > len ? field2 : field1;
    }

    private static class Singaku {
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
                final String examNo
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
        }

        /**
         * {@inheritDoc}
         */
        public String getAttendNo() {
            if ("既卒".equals(_gradHrName)) {
                return "";
            }
            if (StringUtils.isBlank(_attendno)) return "";
            return String.valueOf(Integer.parseInt(_attendno)) + "番";
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return _schregno + " = " + _name;
        }


        private static List getPrintSingaku(final DB2UDB db2, final Param param) throws SQLException {
            final List rtnList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            //final String singakuSql = getSingakuSql();
            final String singakuSql = getKNJE360Asql(param);
            log.debug(" sql = " + singakuSql);
            try {
                ps = db2.prepareStatement(singakuSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String attendno = rs.getString("ATTENDNO");
                    final String gradHrName = rs.getString("G_HR_NAME");
                    final String hrName = rs.getString("HR_NAME");
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
                            examNo);
                    rtnList.add(singaku);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnList;
        }

        // knje360aQuery::getDataListに出力項目を追加
        private static String getKNJE360Asql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD_SCHINFO AS ( ");
            stb.append("     SELECT ");
            stb.append("         '9999' AS GRD_YEAR, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.HR_CLASS, ");
            stb.append("         T1.ATTENDNO, ");
            stb.append("         L2.HR_NAME, ");
            stb.append("         L1.NAME ");
            stb.append("     FROM ");
            stb.append("         SCHREG_REGD_DAT T1 ");
            stb.append("         LEFT JOIN SCHREG_BASE_MST L1 ");
            stb.append("                  ON T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("         LEFT JOIN SCHREG_REGD_HDAT L2 ");
            stb.append("                  ON T1.YEAR     = L2.YEAR ");
            stb.append("                 AND T1.SEMESTER = L2.SEMESTER ");
            stb.append("                 AND T1.GRADE    = L2.GRADE ");
            stb.append("                 AND T1.HR_CLASS = L2.HR_CLASS ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR     = '" + param._ctrlYear + "' AND ");
            stb.append("         T1.SEMESTER = '" + param._ctrlSemester + "' ");
            stb.append(" ), GRD_SCHINFO AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.GRD_YEAR, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.GRD_GRADE AS GRADE, ");
            stb.append("         T1.GRD_HR_CLASS AS HR_CLASS, ");
            stb.append("         T1.GRD_ATTENDNO AS ATTENDNO, ");
            stb.append("         T1.GRD_YEAR || '年度卒' || L1.HR_NAME AS HR_NAME, ");
            stb.append("         T1.NAME ");
            stb.append("     FROM ");
            stb.append("        (SELECT ");
            stb.append("             SCHREGNO, ");
            stb.append("             FISCALYEAR(GRD_DATE) AS GRD_YEAR, ");
            stb.append("             GRD_SEMESTER, ");
            stb.append("             GRD_GRADE, ");
            stb.append("             GRD_HR_CLASS, ");
            stb.append("             GRD_ATTENDNO, ");
            stb.append("             NAME ");
            stb.append("         FROM ");
            stb.append("             GRD_BASE_MST ) T1 ");
            stb.append("         LEFT JOIN GRD_REGD_HDAT L1 ");
            stb.append("                  ON T1.GRD_YEAR     = L1.YEAR ");
            stb.append("                 AND T1.GRD_SEMESTER = L1.SEMESTER ");
            stb.append("                 AND T1.GRD_GRADE    = L1.GRADE ");
            stb.append("                 AND T1.GRD_HR_CLASS = L1.HR_CLASS ");
            stb.append(" ), MAIN AS ( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         AFT.SEQ, ");
            stb.append("         AFT.SCHREGNO, ");
            stb.append("         AFT.DECISION, ");
            stb.append("         AFT.HOWTOEXAM, ");
            stb.append("         AFT.PLANSTAT, ");
            stb.append("         AFT.PREF_CD, ");
            stb.append("         AFT.SCHOOL_GROUP, ");
            stb.append("         AFT.SENKOU_KIND, ");
            stb.append("         AFT.STAT_CD, ");
            stb.append("         COLM.SCHOOL_CD, ");
            stb.append("         COLM.SCHOOL_NAME AS STAT_NAME, ");
            stb.append("         AFT.FACULTYCD, ");
            stb.append("         COLFM.FACULTYNAME, ");
            stb.append("         AFT.DEPARTMENTCD, ");
            stb.append("         COLDM.DEPARTMENTNAME, ");
            stb.append("         D1.REMARK1 AS ADVERTISE_DIV, ");
            stb.append("         NME044.NAME1 AS ADVERTISE_NAME, ");
            stb.append("         D1.REMARK2 AS PROGRAM_CD, ");
            stb.append("         C1.PROGRAM_NAME, ");
            stb.append("         D1.REMARK3 AS FORM_CD, ");
            stb.append("         C2.FORM_NAME, ");
            stb.append("         D1.REMARK4 AS L_CD, ");
            stb.append("         COLEXM.L_NAME, ");
            stb.append("         D1.REMARK5 AS S_CD, ");
            stb.append("         C3.S_NAME, ");
            stb.append("         AFT.STAT_DATE3, ");
            stb.append("         D1.REMARK9 AS EXAMNO ");
            stb.append("     FROM ");
            stb.append("         AFT_GRAD_COURSE_DAT AFT ");
            stb.append("         LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT D1 ON ");
            stb.append("                 AFT.YEAR         = D1.YEAR AND ");
            stb.append("                 AFT.SEQ          = D1.SEQ AND ");
            stb.append("                 D1.DETAIL_SEQ   = 1 ");
            stb.append("         LEFT JOIN COLLEGE_MST COLM ON ");
            stb.append("                 AFT.STAT_CD      = COLM.SCHOOL_CD ");
            stb.append("         LEFT JOIN COLLEGE_FACULTY_MST COLFM ON ");
            stb.append("                 AFT.STAT_CD      = COLFM.SCHOOL_CD AND ");
            stb.append("                 AFT.FACULTYCD    = COLFM.FACULTYCD ");
            stb.append("         LEFT JOIN COLLEGE_DEPARTMENT_MST COLDM ON ");
            stb.append("                 AFT.STAT_CD      = COLDM.SCHOOL_CD AND ");
            stb.append("                 AFT.FACULTYCD    = COLDM.FACULTYCD AND ");
            stb.append("                 AFT.DEPARTMENTCD = COLDM.DEPARTMENTCD ");
            stb.append("         LEFT JOIN PREF_MST L1 ON ");
            stb.append("                 L1.PREF_CD      = AFT.PREF_CD ");
            stb.append("         LEFT JOIN COLLEGE_EXAM_LDAT COLEXM ON ");
            stb.append("                 COLEXM.L_CD         = D1.REMARK4 ");
            stb.append("         LEFT JOIN NAME_MST NME044 ON ");
            stb.append("                 NME044.NAMECD1      = 'E044' AND ");
            stb.append("                 NME044.NAMECD2      = D1.REMARK1 ");
            stb.append("         LEFT JOIN COLLEGE_EXAM_CALENDAR C1 ON ");
            stb.append("                 C1.YEAR         = AFT.YEAR AND ");
            stb.append("                 C1.SCHOOL_CD    = AFT.STAT_CD AND ");
            stb.append("                 C1.FACULTYCD    = AFT.FACULTYCD AND ");
            stb.append("                 C1.DEPARTMENTCD = AFT.DEPARTMENTCD AND ");
            stb.append("                 C1.ADVERTISE_DIV = D1.REMARK1 AND ");
            stb.append("                 C1.PROGRAM_CD   = D1.REMARK2 ");
            stb.append("         LEFT JOIN COLLEGE_EXAM_CALENDAR C2 ON ");
            stb.append("                 C2.YEAR         = AFT.YEAR AND ");
            stb.append("                 C2.SCHOOL_CD    = AFT.STAT_CD AND ");
            stb.append("                 C2.FACULTYCD    = AFT.FACULTYCD AND ");
            stb.append("                 C2.DEPARTMENTCD = AFT.DEPARTMENTCD AND ");
            stb.append("                 C2.ADVERTISE_DIV = D1.REMARK1 AND ");
            stb.append("                 C2.PROGRAM_CD   = D1.REMARK2 AND ");
            stb.append("                 C2.FORM_CD      = D1.REMARK3 ");
            stb.append("         LEFT JOIN COLLEGE_EXAM_CALENDAR C3 ON ");
            stb.append("                 C3.YEAR         = AFT.YEAR AND ");
            stb.append("                 C3.SCHOOL_CD    = AFT.STAT_CD AND ");
            stb.append("                 C3.FACULTYCD    = AFT.FACULTYCD AND ");
            stb.append("                 C3.DEPARTMENTCD = AFT.DEPARTMENTCD AND ");
            stb.append("                 C3.ADVERTISE_DIV = D1.REMARK1 AND ");
            stb.append("                 C3.PROGRAM_CD   = D1.REMARK2 AND ");
            stb.append("                 C3.FORM_CD      = D1.REMARK3 AND ");
            stb.append("                 C3.L_CD1        = D1.REMARK4 AND ");
            stb.append("                 C3.S_CD         = D1.REMARK5 ");
            stb.append("     WHERE ");
            stb.append("         AFT.YEAR         = '" + param._ctrlYear + "' AND ");
            stb.append("         AFT.SENKOU_KIND  = '0' AND ");
//            stb.append("         AFT.STAT_CD IN ('".implode(explode(',', $model->schoolcd),"','")."') AND ");
            stb.append("         AFT.SEQ IN " + param._seqWhereIn + " AND ");
            stb.append("         AFT.SCHREGNO IS NOT NULL ");
            stb.append(" ) ");

            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.*, ");
            stb.append("     CASE WHEN L1.SCHREGNO IS NOT NULL THEN L1.GRD_YEAR ELSE L2.GRD_YEAR END AS GRD_YEAR, ");
            stb.append("     CASE WHEN L1.SCHREGNO IS NOT NULL THEN L1.GRADE ELSE L2.GRADE END AS GRADE, ");
            stb.append("     CASE WHEN L1.SCHREGNO IS NOT NULL THEN L1.HR_CLASS ELSE L2.HR_CLASS END AS HR_CLASS, ");
            stb.append("     CASE WHEN L1.SCHREGNO IS NOT NULL THEN L1.ATTENDNO ELSE L2.ATTENDNO END AS ATTENDNO, ");
            stb.append("     CASE WHEN L1.SCHREGNO IS NOT NULL THEN L1.HR_NAME ELSE L2.HR_NAME END AS HR_NAME, ");
            stb.append("     CASE WHEN L1.SCHREGNO IS NOT NULL THEN L1.NAME ELSE L2.NAME END AS NAME, ");
            stb.append("     CASE WHEN L1.SCHREGNO IS NOT NULL THEN '' ELSE '既卒' END AS G_HR_NAME ");
            stb.append("     , L4.PREF_NAME "); //SP
            stb.append("     , E002.NAME1 as HOWTOEXAM_NAME "); //SP
            stb.append("     , E005.NAME1 as DECISION_NAME "); //SP
            stb.append("     , E006.NAME1 as PLANSTAT_NAME "); //SP
            stb.append("     , E012.NAME1 as SCHOOL_GROUP_NAME "); //SP
            stb.append(" FROM ");
            stb.append("     MAIN T1 ");
            stb.append("     LEFT JOIN REGD_SCHINFO L1 ON T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("     LEFT JOIN GRD_SCHINFO L2 ON T1.SCHREGNO = L2.SCHREGNO ");
//            if ($model->data_select == "1" || $model->data_select == "2") {
//                stb.append(" WHERE ");
//                if ($model->data_select == "1") {
//                    stb.append("     T1.DECISION IN ('1', '2') ");
//                } else {
//                    stb.append("     T1.DECISION NOT IN ('1', '2') OR ");
//                    stb.append("     T1.DECISION IS NULL ");
//                }
//            }
            stb.append("     LEFT JOIN PREF_MST L4 ON L4.PREF_CD = T1.PREF_CD ");
            stb.append("     LEFT JOIN NAME_MST E002 ON E002.NAMECD1 = 'E002' AND E002.NAMECD2 = T1.HOWTOEXAM ");
            stb.append("     LEFT JOIN NAME_MST E005 ON E005.NAMECD1 = 'E005' AND E005.NAMECD2 = T1.DECISION ");
            stb.append("     LEFT JOIN NAME_MST E006 ON E006.NAMECD1 = 'E006' AND E006.NAMECD2 = T1.PLANSTAT ");
            stb.append("     LEFT JOIN NAME_MST E012 ON E012.NAMECD1 = 'E012' AND E012.NAMECD2 = T1.SCHOOL_GROUP ");

            stb.append(" ORDER BY ");
            stb.append("     T1.SCHOOL_CD, ");
            stb.append("     T1.FACULTYCD, ");
            stb.append("     T1.DEPARTMENTCD, ");
            stb.append("     T1.EXAMNO " + param._param_asc_or_desc + ", ");
            stb.append("     T1.ADVERTISE_DIV, ");
            stb.append("     T1.PROGRAM_CD, ");
            stb.append("     T1.FORM_CD, ");
            stb.append("     T1.S_CD, ");
            stb.append("     GRD_YEAR DESC, ");
            stb.append("     GRADE, ");
            stb.append("     HR_CLASS, ");
            stb.append("     ATTENDNO, ");
            stb.append("     T1.SEQ ");

            return stb.toString();
        }

//        private String getSingakuSql() {
//            final StringBuffer stb = new StringBuffer();
    //
//            stb.append(" WITH EXAM_CALENDAR_FORM AS ( ");
//            stb.append(" SELECT ");
//            stb.append("     YEAR, SCHOOL_CD, FACULTYCD, DEPARTMENTCD, ADVERTISE_DIV, PROGRAM_CD, FORM_CD, MAX(PROGRAM_NAME) AS PROGRAM_NAME, MAX(FORM_NAME) AS FORM_NAME ");
//            stb.append(" FROM ");
//            stb.append("     COLLEGE_EXAM_CALENDAR T1 ");
//            stb.append(" GROUP BY ");
//            stb.append("     YEAR, SCHOOL_CD, FACULTYCD, DEPARTMENTCD, ADVERTISE_DIV, PROGRAM_CD, FORM_CD  ");
//            stb.append(" ), EXAM_CALENDAR_PROGRAM AS ( ");
//            stb.append(" SELECT ");
//            stb.append("     YEAR, SCHOOL_CD, FACULTYCD, DEPARTMENTCD, ADVERTISE_DIV, PROGRAM_CD, MAX(PROGRAM_NAME) AS PROGRAM_NAME ");
//            stb.append(" FROM ");
//            stb.append("     EXAM_CALENDAR_FORM T1 ");
//            stb.append(" GROUP BY ");
//            stb.append("     YEAR, SCHOOL_CD, FACULTYCD, DEPARTMENTCD, ADVERTISE_DIV, PROGRAM_CD  ");
//            stb.append(" ) ");
//            stb.append(" SELECT ");
//            stb.append("     AFT.SEQ, ");
////            stb.append("     AFT.SCHREGNO, "); //SP
//            stb.append("     I2.GRADE, "); //S
//            stb.append("     I2.HR_CLASS, "); //S
////            stb.append("     I2.ATTENDNO, "); //SP
////            stb.append("     I3.HR_NAME, "); //SP
////            stb.append("     I1.NAME, "); //SP
//            stb.append("     AFT.SENKOU_KIND, "); //S
//            stb.append("     AFT.STAT_CD, "); //S
////            stb.append("     L1.SCHOOL_NAME as STAT_NAME, "); //SP
//            stb.append("     AFT.SCHOOL_GROUP, "); //S
////            stb.append("     E012.NAME1 as SCHOOL_GROUP_NAME, "); //SP
////            stb.append("     AFT.FACULTYCD, "); //S
////            stb.append("     L2.FACULTYNAME, "); //SP
////            stb.append("     AFT.DEPARTMENTCD, "); //S
////            stb.append("     L3.DEPARTMENTNAME, "); //SP
//            stb.append("     AFT.PREF_CD, "); //S
////            stb.append("     L4.PREF_NAME, "); //SP
////            stb.append("     ECF.FORM_NAME, "); //SP
////            stb.append("     ECP.PROGRAM_NAME, "); //SP
////            stb.append("     AFT.HOWTOEXAM, "); //S
////            stb.append("     E002.NAME1 as HOWTOEXAM_NAME, "); //SP
////            stb.append("     AFT.DECISION, "); //S
////            stb.append("     E005.NAME1 as DECISION_NAME, "); //SP
//            stb.append("     AFT.PLANSTAT, "); //S
////            stb.append("     AFT_GRAD_D.REMARK9 AS EXAMNO, "); //SP
////            stb.append("     E006.NAME1 as PLANSTAT_NAME "); //SP
//            stb.append(" FROM ");
//            stb.append("     AFT_GRAD_COURSE_DAT AFT ");
//            stb.append("     INNER JOIN SCHREG_BASE_MST I1 ON I1.SCHREGNO = AFT.SCHREGNO ");
//            stb.append("     INNER JOIN SCHREG_REGD_DAT I2 ON I2.SCHREGNO = AFT.SCHREGNO AND I2.YEAR = '" + _param._ctrlYear + "' AND I2.SEMESTER = '" + _param._ctrlSemester + "' ");
//            stb.append("     INNER JOIN SCHREG_REGD_HDAT I3 ON I3.YEAR = I2.YEAR AND I3.SEMESTER = I2.SEMESTER AND I3.GRADE = I2.GRADE AND I3.HR_CLASS = I2.HR_CLASS ");
////            stb.append("     INNER JOIN COLLEGE_MST L1 ON L1.SCHOOL_CD = AFT.STAT_CD ");
////            stb.append("     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT AFT_GRAD_D ON AFT.YEAR = AFT_GRAD_D.YEAR ");
////            stb.append("          AND AFT.SEQ = AFT_GRAD_D.SEQ ");
////            stb.append("          AND AFT_GRAD_D.DETAIL_SEQ = 1 ");
////            stb.append("     LEFT JOIN COLLEGE_FACULTY_MST L2 ON L2.SCHOOL_CD = AFT.STAT_CD AND L2.FACULTYCD = AFT.FACULTYCD ");
////            stb.append("     LEFT JOIN COLLEGE_DEPARTMENT_MST L3 ON L3.SCHOOL_CD = AFT.STAT_CD AND L3.FACULTYCD = AFT.FACULTYCD AND L3.DEPARTMENTCD = AFT.DEPARTMENTCD ");
//            stb.append("     LEFT JOIN EXAM_CALENDAR_PROGRAM ECP ON ECP.YEAR = AFT.YEAR AND ECP.SCHOOL_CD = AFT.STAT_CD AND ECP.FACULTYCD = AFT.FACULTYCD AND ECP.DEPARTMENTCD = AFT.DEPARTMENTCD ");
//            stb.append("         AND ECP.ADVERTISE_DIV = AFT_GRAD_D.REMARK1 AND ECP.PROGRAM_CD = AFT_GRAD_D.REMARK2 ");
//            stb.append("     LEFT JOIN EXAM_CALENDAR_FORM ECF ON ECF.YEAR = AFT.YEAR AND ECF.SCHOOL_CD = AFT.STAT_CD AND ECF.FACULTYCD = AFT.FACULTYCD AND ECF.DEPARTMENTCD = AFT.DEPARTMENTCD ");
//            stb.append("         AND ECF.ADVERTISE_DIV = AFT_GRAD_D.REMARK1 AND ECF.PROGRAM_CD = AFT_GRAD_D.REMARK2 AND ECF.FORM_CD = AFT_GRAD_D.REMARK3 ");
////            stb.append("     LEFT JOIN PREF_MST L4 ON L4.PREF_CD = AFT.PREF_CD ");
////            stb.append("     LEFT JOIN NAME_MST E002 ON E002.NAMECD1 = 'E002' AND E002.NAMECD2 = AFT.HOWTOEXAM ");
////            stb.append("     LEFT JOIN NAME_MST E005 ON E005.NAMECD1 = 'E005' AND E005.NAMECD2 = AFT.DECISION ");
////            stb.append("     LEFT JOIN NAME_MST E006 ON E006.NAMECD1 = 'E006' AND E006.NAMECD2 = AFT.PLANSTAT ");
////            stb.append("     LEFT JOIN NAME_MST E012 ON E012.NAMECD1 = 'E012' AND E012.NAMECD2 = AFT.SCHOOL_GROUP ");
//            stb.append(" WHERE ");
//            stb.append("         AFT.YEAR = '" + _param._ctrlYear + "' ");
//            stb.append("     AND AFT.SENKOU_KIND = '0' "); // 進学
//            stb.append("     AND AFT.SEQ IN " + _param._seqWhereIn + " ");
//            stb.append(" ORDER BY ");
//            stb.append("     GRADE, ");
//            stb.append("     HR_CLASS, ");
//            stb.append("     ATTENDNO, ");
//            stb.append("     SEQ ");
    //
//            return stb.toString();
//        }
    }

    /** パラメータ取得処理 */
    private static Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 57286 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    public static class Param {
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String[] _seqList;
        final StringBuffer _seqWhereIn;
        final String _param_asc_or_desc;
        private boolean _isSeireki;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _param_asc_or_desc = request.getParameter("param_asc_or_desc");
            _seqList = StringUtils.split(request.getParameter("SEQ_LIST"), ",");
            _seqWhereIn = new StringBuffer();
            _seqWhereIn.append("(");
            if (_seqList.length == 0) {
                _seqWhereIn.append("-1");
            } else {
                for (int i = 0; i < _seqList.length; i++) {
                    if (i != 0) {
                        _seqWhereIn.append(",");
                    }
                    _seqWhereIn.append(_seqList[i]);
                }
            }
            _seqWhereIn.append(")");

            setSeirekiFlg(db2);
        }

        private void setSeirekiFlg(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                _isSeireki = false;
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' AND NAME1 IS NOT NULL ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if ("2".equals(rs.getString("NAME1"))) _isSeireki = true; //西暦
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        public String changePrintDate(final String date) {
            if (null == date) {
                return "";
            }
            if (_isSeireki) {
                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
            } else {
                return KNJ_EditDate.h_format_JP(date);
            }
        }

        public String changePrintYear(final String year) {
            if (null == year) {
                return "";
            }
            if (_isSeireki) {
                return year + "年度";
            } else {
                return KenjaProperties.gengou(Integer.parseInt(year)) + "年度";
            }
        }
    }
}

// eof
