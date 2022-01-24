// kanji=漢字
/*
 * $Id: 349ce476cb8c320589b5fbcc986cfc097c316566 $
 *
 * 作成日: 2008/05/20 15:56:08 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 349ce476cb8c320589b5fbcc986cfc097c316566 $
 */
public class KNJD041S {

    private static final Log log = LogFactory.getLog(KNJD041S.class);

    Param _param;

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
        setHead(svf);
        int cnt = 0;
        String befClass = "";
        final List printDataList = getPrintDataList(db2);
        for (final Iterator iter = printDataList.iterator(); iter.hasNext();) {
            final PrintData printData = (PrintData) iter.next();

            if (hasData && !befClass.equals(printData._classCd)) {
                svf.VrSetForm("KNJD041S.frm", 4);
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

    private void setHead(final Vrw32alp svf) {
        svf.VrSetForm("KNJD041S.frm", 4);
    }

    private void setPrintOut(final Vrw32alp svf, final PrintData printData, final int fieldNo) {
        log.debug(printData);
        svf.VrsOut("NENDO", _param.changePrintYear());
        svf.VrsOut("TERM", _param._semesterName);
        svf.VrsOut("COURSE", printData._className);
        svf.VrsOut("TESTNAME", _param._testName);
        final String information = _param._output.equals("1") ? "未入力のみ" : "全て";
        svf.VrsOut("INFORMATION", information);
        svf.VrsOut("DATE", _param.changePrintDate(_param._date));
        svf.VrsOut("ENFORCEMENT", "実施日");

        svf.VrsOut("STAFFCD", printData._staffCd);
        if (null != printData._staffName) {
            final String stfField = (20 < printData._staffName.getBytes().length) ? "2" : "";
            svf.VrsOut("STAFFNAME" + stfField, printData._staffName);
        }
        if (printData._electDiv.equals("1")) {
            svf.VrAttribute("SUBJECT2", "Paint=(2,70,2),Bold=1");
        }
        if (null != printData._subclassName) {
            final String stfField = (16 < printData._subclassName.getBytes().length) ? "2" : "";
            svf.VrsOut("SUBJECT" + stfField, printData._subclassName);
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
        ResultSet rs = null;
        try {
            final String sql = getPrintDataSql();
            log.debug(sql);
            db2.query(sql);
            rs = db2.getResultSet();
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
                    printData = new PrintData(rs.getString("CHAIRCD"),
                            rs.getString("CHAIRNAME"),
                            rs.getString("CLASSCD"),
                            rs.getString("CLASSNAME"),
                            rs.getString("SUBCLASSCD"),
                            rs.getString("SUBCLASSNAME"),
                            rs.getString("ELECTDIV"),
                            rs.getString("STAFFCD"),
                            rs.getString("STAFFNAME"),
                            sep + (null == rs.getString("CHAIRCLASS") ? "" : rs.getString("CHAIRCLASS")),
                            rs.getString("EXECUTEDATE"),
                            rs.getString("EXECUTED"),
                            rs.getString("MAXDATE"),
                            rs.getString("MAXTIME")
                            );
                    sep = ",";
                } else {
                    printData.setChairClass(sep + rs.getString("CHAIRCLASS"));
                }
                befStaffChairCd = checkCd;
            }
            if (printData != null) {
                rtnList.add(printData);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rs);
        }
        return rtnList;
    }

    /**
     * @return
     */
    private String getPrintDataSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH SCH_CHR AS ( ");
        if (_param._recordDiv == Param.RECORD_DIV_VALUE || _param._recordDiv == Param.RECORD_DIV_SCORE && _param._useRecordChkfinDat) {
            //テスト評価の場合
            stb.append("SELECT ");
            stb.append("        T1.TESTKINDCD || T1.TESTITEMCD AS TESTCD, ");
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
            stb.append("        T3.SUBCLASSNAME, ");
            stb.append("        T3.ELECTDIV, ");
            stb.append("        DATE(MAX(T5.UPDATED)) AS MAXDATE, ");
            stb.append("        TIME(MAX(T5.UPDATED)) AS MAXTIME ");
            stb.append(" FROM ");
            stb.append("        RECORD_CHKFIN_DAT T1 ");
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
            stb.append("             AND T1.TESTKINDCD || T1.TESTITEMCD = T5.TESTKINDCD || T5.TESTITEMCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("            AND T1.CLASSCD = T5.CLASSCD ");
                stb.append("            AND T1.SCHOOL_KIND = T5.SCHOOL_KIND ");
                stb.append("            AND T1.CURRICULUM_CD = T5.CURRICULUM_CD ");
            }
            stb.append("             AND T1.SUBCLASSCD = T5.SUBCLASSCD ");
            stb.append(" WHERE  T1.YEAR = '" +  _param._year + "' ");
            stb.append("        AND T1.SEMESTER = '" +  _param._semester + "' ");
            if ("9".equals(_param._semester)) {
                stb.append("    AND EXISTS (SELECT 'X' FROM CHAIR_DAT ");
                stb.append("            WHERE YEAR = '" +  _param._year + "' AND CHAIRCD = T1.CHAIRCD ");
                stb.append("            HAVING MAX(SEMESTER) = T2.SEMESTER )");
            } else {
                stb.append("             AND T1.SEMESTER = T2.SEMESTER ");
            }
            stb.append("        AND T1.TESTKINDCD || T1.TESTITEMCD = '" +  _param._kindItem + "' ");
            if ("9".equals(_param._semester)) {
                stb.append("    AND EXISTS (SELECT 'X' FROM CHAIR_DAT ");
                stb.append("            WHERE YEAR = '" +  _param._year + "' AND CHAIRCD = T1.CHAIRCD ");
                stb.append("            HAVING T2.SEMESTER = MAX(SEMESTER)) ");
            } else {
                stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
            }
            stb.append("        AND T2.CHAIRCD = T1.CHAIRCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("        AND T2.CLASSCD || '-' || T2.SCHOOL_KIND IN " + _param._selectInstate + " ");
            } else {
                stb.append("        AND substr(T2.SUBCLASSCD, 1, 2) IN " + _param._selectInstate + " ");
            }
            stb.append("        AND T1.RECORD_DIV = '" + _param._recordDiv + "' ");
            if (_param._output.equals("1")) {
                stb.append("        AND value(T1.EXECUTED, '0') = '0' ");
            }
            stb.append(" GROUP BY ");
            stb.append("     T1.TESTKINDCD || T1.TESTITEMCD, ");
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
            stb.append("     T3.ELECTDIV ");
            stb.append(" ) ");
        } else {
            stb.append(" SELECT ");
            stb.append("     T1.TESTKINDCD || T1.TESTITEMCD AS TESTCD, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.EXECUTEDATE, ");
            stb.append("     T1.EXECUTED, ");
            stb.append("     T1.CHAIRCD, ");
            stb.append("     L1.CHAIRNAME, ");
            stb.append("     L1.GROUPCD, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("        L1.CLASSCD AS CLASSCD, ");
            } else {
                stb.append("        substr(L1.SUBCLASSCD, 1, 2) AS CLASSCD, ");
            }
            stb.append("     L3.CLASSNAME, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || ");
            }
            stb.append("     L1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     L2.SUBCLASSNAME, ");
            stb.append("     L2.ELECTDIV, ");
            stb.append("     DATE(MAX(REC.UPDATED)) AS MAXDATE, ");
            stb.append("     TIME(MAX(REC.UPDATED)) AS MAXTIME ");
            stb.append(" FROM ");
            stb.append("     SCH_CHR_TEST T1 ");
            stb.append("     INNER JOIN CHAIR_DAT L1 ON T1.YEAR = L1.YEAR ");
            stb.append("          AND T1.SEMESTER = L1.SEMESTER ");
            stb.append("          AND T1.CHAIRCD = L1.CHAIRCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     INNER JOIN SUBCLASS_MST L2 ON L1.SUBCLASSCD = L2.SUBCLASSCD ");
                stb.append("         AND L1.CLASSCD = L2.CLASSCD ");
                stb.append("         AND L1.SCHOOL_KIND = L2.SCHOOL_KIND ");
                stb.append("         AND L1.CURRICULUM_CD = L2.CURRICULUM_CD ");
                stb.append("     INNER JOIN CLASS_MST L3 ON L1.CLASSCD = L3.CLASSCD ");
                stb.append("         AND L1.SCHOOL_KIND = L3.SCHOOL_KIND ");
            } else {
                stb.append("     INNER JOIN SUBCLASS_MST L2 ON L1.SUBCLASSCD = L2.SUBCLASSCD ");
                stb.append("     INNER JOIN CLASS_MST L3 ON substr(L1.SUBCLASSCD, 1, 2) = L3.CLASSCD ");
            }
            stb.append("     LEFT JOIN RECORD_SCORE_DAT REC ON L1.YEAR = REC.YEAR ");
            stb.append("          AND L1.SEMESTER = REC.SEMESTER ");
            stb.append("          AND T1.TESTKINDCD || T1.TESTITEMCD = REC.TESTKINDCD || REC.TESTITEMCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("            AND L1.CLASSCD = REC.CLASSCD ");
                stb.append("            AND L1.SCHOOL_KIND = REC.SCHOOL_KIND ");
                stb.append("            AND L1.CURRICULUM_CD = REC.CURRICULUM_CD ");
            }
            stb.append("          AND L1.SUBCLASSCD = REC.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.TESTKINDCD || T1.TESTITEMCD = '" + _param._kindItem + "' ");
            if (_param._output.equals("1")) {
                stb.append("     AND VALUE(T1.EXECUTED, '0') = '0' ");
            }
            stb.append("     AND T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     AND L1.CLASSCD || '-' || L1.SCHOOL_KIND IN " + _param._selectInstate + " ");
            } else {
                stb.append("     AND substr(L1.SUBCLASSCD, 1, 2) IN " + _param._selectInstate + " ");
            }
            stb.append(" GROUP BY ");
            stb.append("     T1.TESTKINDCD || T1.TESTITEMCD, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.EXECUTEDATE, ");
            stb.append("     T1.EXECUTED, ");
            stb.append("     T1.CHAIRCD, ");
            stb.append("     L1.CHAIRNAME, ");
            stb.append("     L1.GROUPCD, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("        L1.CLASSCD, ");
            } else {
                stb.append("        substr(L1.SUBCLASSCD, 1, 2) AS CLASSCD, ");
            }
            stb.append("     L3.CLASSNAME, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("    L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || ");
            }
            stb.append("     L1.SUBCLASSCD, ");
            stb.append("     L2.SUBCLASSNAME, ");
            stb.append("     L2.ELECTDIV ");
            stb.append(" ) ");
        }
        stb.append(" SELECT ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.CLASSNAME, ");
        stb.append("     STF.STAFFCD, ");
        stb.append("     L1.STAFFNAME, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.SUBCLASSNAME, ");
        stb.append("     VALUE(T1.ELECTDIV, '0') AS ELECTDIV, ");
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
        stb.append("     T1.MAXTIME ");
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
        final Param param = new Param(db2, request);
        dumpParam(request, param);
        return param;
    }

    /** パラメータダンプ */
    private void dumpParam(final HttpServletRequest request, final Param param) {
        log.fatal("$Revision: 56595 $"); // CVSキーワードの取り扱いに注意
        if (log.isDebugEnabled()) {
            final Enumeration enums = request.getParameterNames();
            while (enums.hasMoreElements()) {
                final String name = (String) enums.nextElement();
                final String[] values = request.getParameterValues(name);
                log.debug("parameter:name=" + name + ", value=[" + StringUtils.join(values, ',') + "]");
            }
        }
    }

    private class Param {
        final String _year;
        final String _ctrSemester;
        final String _kindItem;
        final String _testName;
        final String _semester;
        final String _semesterName;
        final String _output;
        final String[] _selectData;
        final String _selectInstate;
        final String _date;
        String _z010 = "";
        String _z012 = "";
        final boolean _isSeireki;
        final int _recordDiv;
        final boolean _useRecordChkfinDat;

        final static int RECORD_DIV_SCORE = 1; // 素点
        final static int RECORD_DIV_VALUE = 2; // 評価
        
        final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _ctrSemester = request.getParameter("CTRL_SEMESTER");
            _kindItem = request.getParameter("TESTCD");
            _semester = request.getParameter("SEMESTER");
            _testName = getTestName(db2);
            _semesterName = getSemesterName(db2);
            _output = request.getParameter("OUTPUT");
            _selectData = request.getParameterValues("CLASS_SELECTED");  //学籍番号または学年-組
            _selectInstate = getInstate(_selectData);

            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            returnval = getinfo.Control(db2);
            _date = returnval.val3;

            _recordDiv = (request.getParameter("RECORD_DIV") != null) ? Integer.parseInt(request.getParameter("RECORD_DIV")) 
                    : RECORD_DIV_SCORE; // 素点/評価 種別
            _useRecordChkfinDat = "1".equals(request.getParameter("useRecordChkfinDat"));

            _z010 = setNameMst(db2, "Z010", "00");
            _z012 = setNameMst(db2, "Z012", "01");
            _isSeireki = _z012.equals("2") ? true : false;
            
            _useCurriculumcd = request.getParameter("useCurriculumcd");
        }

        private String getTestName(final DB2UDB db2) throws SQLException {
            String rtn = "";
            ResultSet rs = null;
            try {
                final String sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW WHERE YEAR = '" + _year + "'" +
                        " AND SEMESTER = '" + _semester + "' " +
                        " AND TESTKINDCD || TESTITEMCD = '" + _kindItem + "' ";
                db2.query(sql);
                rs = db2.getResultSet();
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
            ResultSet rs = null;
            try {
                final String sql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ";
                db2.query(sql);
                rs = db2.getResultSet();
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
            db2.query(getNameMst(_year, namecd1, namecd2));
            ResultSet rs = db2.getResultSet();
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
                return _param._year + "年度";
            } else {
                return nao_package.KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度";
            }
        }
    }
}
 // KNJD041S

// eof
