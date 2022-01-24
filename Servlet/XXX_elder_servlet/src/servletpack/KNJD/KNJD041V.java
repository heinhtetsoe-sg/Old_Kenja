// kanji=漢字
/*
 * $Id: 00b8118f6014798cd5eed1e1aeb0d38de76363d5 $
 *
 * 作成日: 2008/05/20 15:56:08 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 00b8118f6014798cd5eed1e1aeb0d38de76363d5 $
 */
public class KNJD041V {

    private static final Log log = LogFactory.getLog(KNJD041V.class);

    private Param _param;

    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定
        DB2UDB db2 = null; //Databaseクラスを継承したクラス
        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        boolean hasData = false;
        try {

            // print svf設定
            sd.setSvfInit(request, response, svf);
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            //SVF出力
            hasData = printMain(response, db2, svf);

        } finally {
            sd.closeSvf(svf, hasData);
            sd.closeDb(db2);
        }

    }// doGetの括り

    private boolean printMain(final HttpServletResponse response, final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        boolean hasData = false;
        svf.VrSetForm("KNJD041V.frm", 4);
        int cnt = 0;
        String befClass = "";
        final List printDataList = getPrintDataList(db2);
        for (final Iterator iter = printDataList.iterator(); iter.hasNext();) {
            final PrintData printData = (PrintData) iter.next();

            if (hasData && !befClass.equals(printData._classCd)) {
                svf.VrSetForm("KNJD041V.frm", 4);
                cnt = 0;
            }

            cnt++;
            setPrintOut(svf, printData, cnt);
            svf.VrEndRecord();

            befClass = printData._classCd;
            hasData = true;
        }
        if (cnt > 0) {
            svf.VrEndRecord();
        }
        return hasData;
    }

    private void setPrintOut(final Vrw32alp svf, final PrintData printData, final int fieldNo) {
        log.debug(printData);
        svf.VrsOut("NENDO", _param.changePrintYear());
        svf.VrsOut("TERM", _param._semesterName);
        svf.VrsOut("COURSE", printData._classCd + "-" + printData._schoolkind + ":" + printData._className);
        svf.VrsOut("TESTNAME", _param._testName);
        final String information = _param._output.equals("1") ? "未入力のみ" : "全て";
        svf.VrsOut("INFORMATION", information);
        svf.VrsOut("DATE", _param.changePrintDate(_param._date));
        svf.VrsOut("ENFORCEMENT", "実施日");
        svf.VrsOut("SUBJECT_NAME_HEADER", ("2".equals(_param._outputcol) ? "講座名" : "科  目"));

        svf.VrsOut("STAFFCD", printData._staffCd);
        if (null != printData._staffName) {
            final String stfField = (20 < printData._staffName.getBytes().length) ? "2" : "";
            svf.VrsOut("STAFFNAME" + stfField, printData._staffName);
        }
        if (null != printData._subclassName) {
            final String subField = (16 < printData._subclassName.getBytes().length) ? "2" : "";
            if (printData._electDiv.equals("1")) {
                svf.VrAttribute("SUBJECT" + subField, "Paint=(2,70,2),Bold=1");
            }
            if ("2".equals(_param._outputcol)) {
                svf.VrsOut("SUBJECT" + subField, printData._chairName);
            } else {
                svf.VrsOut("SUBJECT" + subField, printData._subclassName);
            }
        }
        svf.VrsOut("CHAIRCD", printData._chairCd);
        svf.VrsOut("CLASS", printData._chairClass.toString());
        if (null != printData._executeDate) {
            svf.VrsOut("PERIOD1", printData._executeDate.replace('-', '/'));
        }
        if (null != printData._exeDate && printData._executed.equals("1")) {
            svf.VrsOut("RECORD_DATE", printData._exeDate.replace('-', '/') + " " + printData._exeTime);
        } else {
            svf.VrsOut("RECORD_DATE", "未入力");
        }
    }

    private List getPrintDataList(final DB2UDB db2) throws SQLException {
        final List rtnList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getPrintDataSql();
            log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            String befStaffChairCd = "";
            PrintData printData = null;
            String sep = "";
            while (rs.next()) {
                final String checkCd = rs.getString("STAFFCD") + rs.getString("CHAIRCD") + rs.getString("SUBCLASSCD");
                if (!befStaffChairCd.equals(checkCd)) {
                    if (!befStaffChairCd.equals("")) {
                        rtnList.add(printData);
                        sep = "";
                    }
                    final String maxdate, maxtime;
                    if (null == rs.getString("MAXDATE")) {
                        maxdate = rs.getString("MAXDATE2");
                        maxtime = rs.getString("MAXTIME2");
                    } else {
                        maxdate = rs.getString("MAXDATE");
                        maxtime = rs.getString("MAXTIME");
                    }
                    printData = new PrintData(rs.getString("CHAIRCD"),
                            rs.getString("CHAIRNAME"),
                            rs.getString("CLASSCD"),
                            rs.getString("CLASSNAME"),
                            rs.getString("SUBCLASSCD"),
                            rs.getString("SUBCLASSNAME"),
                            rs.getString("ELECTDIV"),
                            rs.getString("STAFFCD"),
                            rs.getString("STAFFNAME"),
                            (null == rs.getString("CHAIRCLASS") ? "" : sep + rs.getString("CHAIRCLASS")),
                            rs.getString("SCHOOL_KIND"),
                            rs.getString("EXECUTEDATE"),
                            rs.getString("EXECUTED"),
                            maxdate,
                            maxtime
                            );
                    //先頭データのCHAIRCLASSがNULLだった場合を考慮。
                    sep = ("".equals(sep) && null == rs.getString("CHAIRCLASS") ? "" : ",");
                } else {
                    printData.setChairClass((null == rs.getString("CHAIRCLASS") ? "" : sep + rs.getString("CHAIRCLASS")));
                }
                befStaffChairCd = checkCd;
            }
            if (printData != null) {
                rtnList.add(printData);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtnList;
    }

    /**
     * @return
     */
    private String getPrintDataSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH SCH_CHR AS ( ");
        stb.append("SELECT ");
        stb.append("        T1.TESTKINDCD || T1.TESTITEMCD || T1.RECORD_DIV AS TESTCD, ");
        stb.append("        T1.YEAR, ");
        if ("9".equals(_param._semester)) {
            stb.append("        MAX(T2.SEMESTER) AS SEMESTER, ");
        } else {
            stb.append("        T1.SEMESTER, ");
        }
        stb.append("        T1.EXECUTEDATE, ");
        stb.append("        value(T1.EXECUTED,'0') AS EXECUTED,");
        stb.append("        T1.CHAIRCD, ");
        stb.append("        T2.CHAIRNAME, ");
        stb.append("        T2.GROUPCD, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        T2.CLASSCD AS CLASSCD, ");
        } else {
            stb.append("        substr(T2.SUBCLASSCD, 1, 2) AS CLASSCD, ");
        }
        stb.append("        T4.CLASSNAME, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("    T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
        }
        stb.append("        T2.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("        T2.SCHOOL_KIND, ");
        stb.append("        T3.SUBCLASSNAME, ");
        stb.append("        T3.ELECTDIV, ");
        stb.append("        DATE(MAX(T5.UPDATED)) AS MAXDATE, ");
        stb.append("        TIME(MAX(T5.UPDATED)) AS MAXTIME, ");
        stb.append("        DATE(T1.UPDATED) AS MAXDATE2, ");
        stb.append("        TIME(T1.UPDATED) AS MAXTIME2 ");
        stb.append(" FROM ");
        stb.append("        RECORD_CHKFIN_SDIV_DAT T1 ");
        stb.append("        INNER JOIN CHAIR_DAT T2 ON ");
        stb.append("             T2.YEAR = T1.YEAR ");

        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        INNER JOIN SUBCLASS_MST T3 ON T2.SUBCLASSCD = T3.SUBCLASSCD ");
            stb.append("            AND T2.CLASSCD = T3.CLASSCD ");
            stb.append("            AND T2.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append("            AND T2.CURRICULUM_CD = T3.CURRICULUM_CD ");
            stb.append("        INNER JOIN CLASS_MST T4 ON T4.CLASSCD = T3.CLASSCD ");
            stb.append("            AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ");
        } else  {
            stb.append("        INNER JOIN SUBCLASS_MST T3 ON T2.SUBCLASSCD = T3.SUBCLASSCD ");
            stb.append("        INNER JOIN CLASS_MST T4 ON T4.CLASSCD = substr(T3.SUBCLASSCD, 1, 2) ");
        }
        stb.append("        LEFT JOIN RECORD_SCORE_DAT T5 ON T1.YEAR = T5.YEAR ");
        stb.append("             AND T1.SEMESTER = T5.SEMESTER ");
        stb.append("             AND T1.TESTKINDCD || T1.TESTITEMCD || T1.RECORD_DIV = T5.TESTKINDCD || T5.TESTITEMCD || T5.SCORE_DIV ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("            AND T1.CLASSCD = T5.CLASSCD ");
            stb.append("            AND T1.SCHOOL_KIND = T5.SCHOOL_KIND ");
            stb.append("            AND T1.CURRICULUM_CD = T5.CURRICULUM_CD ");
        }
        stb.append("             AND T1.SUBCLASSCD = T5.SUBCLASSCD ");
        stb.append(" WHERE  T1.YEAR = '" +  _param._year + "' ");
        stb.append("        AND T1.SEMESTER = '" +  _param._semester + "' ");
        stb.append("        AND T1.TESTKINDCD || T1.TESTITEMCD || T1.RECORD_DIV = '" +  _param._kindItem + "' ");
        if (_param._output.equals("1")) {
            stb.append("        AND value(T1.EXECUTED, '0') = '0' ");
        }
        if ("9".equals(_param._semester)) {
            stb.append("    AND EXISTS (SELECT 'X' FROM CHAIR_DAT W1 ");
            stb.append("                WHERE W1.YEAR = '" +  _param._year + "' AND W1.CHAIRCD = T1.CHAIRCD ");
            stb.append("                HAVING T2.SEMESTER = MAX(W1.SEMESTER)) ");
        } else {
            stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
        }
        stb.append("        AND T2.CHAIRCD = T1.CHAIRCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        AND T2.CLASSCD || '-' || T2.SCHOOL_KIND IN " + _param._selectInstate + " ");
        } else {
            stb.append("        AND substr(T2.SUBCLASSCD, 1, 2) IN " + _param._selectInstate + " ");
        }
        stb.append(" GROUP BY ");
        stb.append("     T1.TESTKINDCD || T1.TESTITEMCD || T1.RECORD_DIV, ");
        stb.append("     T2.SCHOOL_KIND, ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.EXECUTEDATE, ");
        stb.append("     T1.EXECUTED, ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T2.CHAIRNAME, ");
        stb.append("     T2.GROUPCD, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("        T2.CLASSCD, ");
        } else {
            stb.append("        substr(T2.SUBCLASSCD, 1, 2), ");
        }
        stb.append("     T4.CLASSNAME, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("    T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
        }
        stb.append("     T2.SUBCLASSCD, ");
        stb.append("     T3.SUBCLASSNAME, ");
        stb.append("     T3.ELECTDIV, ");
        stb.append("     DATE(T1.UPDATED), ");
        stb.append("     TIME(T1.UPDATED) ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.CLASSNAME, ");
        stb.append("     STF.STAFFCD, ");
        stb.append("     L1.STAFFNAME, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.SUBCLASSNAME, ");
        stb.append("     VALUE(T1.ELECTDIV, '0') AS ELECTDIV, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.CHAIRNAME, ");
        stb.append("     T1.GROUPCD, ");
        stb.append("     T1.EXECUTEDATE, ");
        stb.append("     VALUE(T1.EXECUTED, '0') AS EXECUTED, ");
        stb.append("     CASE WHEN T1.GROUPCD = '0000' ");
        stb.append("          THEN CLSNM1.HR_NAMEABBV ");
        stb.append("          ELSE CLSNM2.HR_NAMEABBV ");
        stb.append("     END AS CHAIRCLASS, ");
        stb.append("     T1.MAXDATE, ");
        stb.append("     T1.MAXTIME, ");
        stb.append("     T1.MAXDATE2, ");
        stb.append("     T1.MAXTIME2 ");
        stb.append(" FROM ");
        stb.append("     SCH_CHR T1 ");
        stb.append("     LEFT JOIN CHAIR_STF_DAT STF ON T1.YEAR = STF.YEAR ");
        stb.append("          AND T1.SEMESTER = STF.SEMESTER ");
        stb.append("          AND T1.CHAIRCD = STF.CHAIRCD ");
        stb.append("     LEFT JOIN STAFF_MST L1 ON STF.STAFFCD = L1.STAFFCD ");
        stb.append("     LEFT JOIN CHAIR_CLS_DAT CLS1 ON T1.YEAR = CLS1.YEAR ");
        stb.append("          AND T1.SEMESTER = CLS1.SEMESTER ");
        stb.append("          AND T1.CHAIRCD = CLS1.CHAIRCD ");
        stb.append("          AND T1.GROUPCD = '0000' ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT CLSNM1 ON CLS1.YEAR = CLSNM1.YEAR ");
        stb.append("          AND CLS1.SEMESTER = CLSNM1.SEMESTER ");
        stb.append("          AND CLS1.TRGTGRADE = CLSNM1.GRADE ");
        stb.append("          AND CLS1.TRGTCLASS = CLSNM1.HR_CLASS ");
        stb.append("     LEFT JOIN CHAIR_CLS_DAT CLS2 ON T1.YEAR = CLS2.YEAR ");
        stb.append("          AND T1.SEMESTER = CLS2.SEMESTER ");
        stb.append("          AND T1.GROUPCD <> '0000' ");
        stb.append("          AND T1.GROUPCD = CLS2.GROUPCD ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT CLSNM2 ON CLS2.YEAR = CLSNM2.YEAR ");
        stb.append("          AND CLS2.SEMESTER = CLSNM2.SEMESTER ");
        stb.append("          AND CLS2.TRGTGRADE = CLSNM2.GRADE ");
        stb.append("          AND CLS2.TRGTCLASS = CLSNM2.HR_CLASS ");
        stb.append(" ORDER BY ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     STF.STAFFCD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.EXECUTEDATE ");
        return stb.toString();
    }

    private class PrintData {
        final String _chairCd;
        final String _chairName;
        final String _classCd;
        final String _className;
        final String _subclassCd;
        final String _subclassName;
        final String _electDiv;
        final String _staffCd;
        final String _staffName;
        final StringBuffer _chairClass = new StringBuffer();
        final String _schoolkind;
        final String _executeDate;
        final String _executed;
        final String _exeDate;
        final String _exeTime;

        private PrintData(
                final String chairCd,
                final String chairName,
                final String classCd,
                final String className,
                final String subclassCd,
                final String subclassName,
                final String electDiv,
                final String staffCd,
                final String staffName,
                final String chairClass,
                final String schoolkind,
                final String executeDate,
                final String executed,
                final String exeDate,
                final String exeTime
        ) {
            _chairCd = chairCd;
            _chairName = chairName;
            _classCd = classCd;
            _className = className;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _electDiv = electDiv;
            _staffCd = staffCd;
            _staffName = staffName;
            _chairClass.append(chairClass);
            _schoolkind = schoolkind;
            _executeDate = executeDate;
            _executed = executed;
            _exeDate = exeDate;
            _exeTime = exeTime;
        }

        private void setChairClass(final String chairClass) {
            _chairClass.append(chairClass);
        }

        public String toString() {
            return "担当：" + _staffCd + " : " + _staffName
                    + " 口座：" + _classCd + " : "  + _className
                    + " 科目：" + _subclassCd + " : "  + _subclassName
                    + " クラス：" + _chairClass.toString();
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 68257 $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    private static class Param {
        final String _year;
        final String _ctrSemester;
        final String _kindItem;
        final String _testName;
        final String _semester;
        final String _semesterName;
        final String _output;
        final String _outputcol;
        final String[] _selectData;
        final String _selectInstate;
        final String _date;
        final String _major;
        final String _use_school_detail_gcm_dat;
        final String _useSchool_KindField;
        final String SCHOOLKIND;
        final String SCHOOLCD;
        String _z010 = "";
        String _z012 = "";
        final boolean _isSeireki;

        final static int RECORD_DIV_SCORE = 1; // 素点
        final static int RECORD_DIV_VALUE = 2; // 評価

        final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _ctrSemester = request.getParameter("CTRL_SEMESTER");
            _kindItem = request.getParameter("TESTCD");
            _semester = request.getParameter("SEMESTER");
            _major = request.getParameter("MAJOR");
            _use_school_detail_gcm_dat = request.getParameter("use_school_detail_gcm_dat");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            SCHOOLKIND = request.getParameter("SCHOOLKIND");
            SCHOOLCD = request.getParameter("SCHOOLCD");
            _testName = getTestName(db2);
            _semesterName = getSemesterName(db2);
            _output = request.getParameter("OUTPUT");
            _outputcol = request.getParameter("OUTPUTCOL");
            _selectData = request.getParameterValues("CLASS_SELECTED");
            _selectInstate = getInstate(_selectData);

            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            returnval = getinfo.Control(db2);
            _date = returnval.val3;

            _z010 = setNameMst(db2, "Z010", "00");
            _z012 = setNameMst(db2, "Z012", "01");
            _isSeireki = _z012.equals("2") ? true : false;

            _useCurriculumcd = request.getParameter("useCurriculumcd");
        }

        private String getTestName(final DB2UDB db2) throws SQLException {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql;
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV WHERE YEAR = '" + _year + "'" +
                            " AND SEMESTER = '" + _semester + "' " +
                            " AND GRADE = '00' " +
                            " AND COURSECD || '-' || MAJORCD = '" + _major + "' " +
                            " AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _kindItem + "' ";
                    if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(SCHOOLKIND)) {
                        sql += " AND SCHOOLCD = '" + SCHOOLCD + "' " +
                               " AND SCHOOL_KIND = '" + SCHOOLKIND + "' ";
                    }
                } else {
                    sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV WHERE YEAR = '" + _year + "'" +
                            " AND SEMESTER = '" + _semester + "' " +
                            " AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _kindItem + "' ";
                }
                log.debug(" testname sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("TESTITEMNAME");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
            return rtn;
        }

        private String getSemesterName(final DB2UDB db2) throws SQLException {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SEMESTERNAME");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
            return rtn;
        }

        /**
         * @param selectData
         * @return
         */
        private String getInstate(final String[] selectData) {
            String sep = "";
            final StringBuffer stb = new StringBuffer();
            stb.append("('");
            for (int i = 0; i < selectData.length; i++) {
                stb.append(sep + selectData[i]);
                sep = "','";
            }
            stb.append("')");

            return stb.toString();
        }

        private String setNameMst(final DB2UDB db2, final String namecd1, final String namecd2) throws SQLException {
            String rtnSt = "";
            final String sql = getNameMst(_year, namecd1, namecd2);
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            try {
                while (rs.next()) {
                    rtnSt = rs.getString("NAME1");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
            return rtnSt;
        }

        private String getNameMst(final String year, final String namecd1, final String namecd2) {
            final String rtnSql = " SELECT "
                                + "     * "
                                + " FROM "
                                + "     V_NAME_MST "
                                + " WHERE "
                                + "     YEAR = '" + year + "' "
                                + "     AND NAMECD1 = '" + namecd1 + "' "
                                + "     AND NAMECD2 = '" + namecd2 + "'";
            return rtnSql;
        }

        private String changePrintDate(final String date) {
            if (_isSeireki) {
                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
            } else {
                return KNJ_EditDate.h_format_JP(date);
            }
        }

        private String changePrintYear() {
            if (_isSeireki) {
                return _year + "年度";
            } else {
                return KenjaProperties.gengou(Integer.parseInt(_year)) + "年度";
            }
        }
    }
}
 // KNJD041V

// eof
