// kanji=漢字
/*
 * $Id: 70ccd67955692312130ecacbec99102d3e678fcd $
 *
 * 作成日: 2013/11/07 11:50:13 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2013 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.sql.PreparedStatement;
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

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 70ccd67955692312130ecacbec99102d3e678fcd $
 */
public class KNJB0220 {

    private static final Log log = LogFactory.getLog("KNJB0220.class");

    private boolean _hasData;
    private final String CONDITION_KISYU = "1";
    private final String CONDITION_HEI = "2";
    private final String CONDITION_HEI_KIN = "3";
    private final String CONDITION_KEIZOKU = "4";
    private final String CONDITION_KEIZOKU_KIN = "5";
    private final String CONDITION_TANNI = "6";
    private final String CONDITION_HITU = "7";

    private final String JOUKEN_ALL = "0";
    private final String JOUKEN_RISYU = "1";
    private final String JOUKEN_SETTEI = "2";
    private final String JOUKEN_HITURISYU = "3";

    Param _param;

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

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final Map printMap = getPrintMap(db2);
        for (final Iterator iter = printMap.keySet().iterator(); iter.hasNext();) {
            final String key = (String) iter.next();
            final List printDataList = (List) printMap.get(key);
            if (JOUKEN_RISYU.equals(key)) {
                printRisyu(svf, printDataList, key);
            } else if (JOUKEN_SETTEI.equals(key)) {
                printSentaku(svf, printDataList, key);
            } else if (JOUKEN_HITURISYU.equals(key)) {
                printHituRisyu(svf, printDataList, key);
            }
        }
    }

    private void printRisyu(final Vrw32alp svf, final List printDataList, final String key) {
        String befDiv = "";
        for (final Iterator itData = printDataList.iterator(); itData.hasNext();) {
            final SubclassCompGroupData compData = (SubclassCompGroupData) itData.next();
            if (!befDiv.equals(compData._conditionDiv)) {
                setHead(svf, key);
                String setConditionTitle = "設定単位修得条件";
                if (CONDITION_KISYU.equals(compData._conditionDiv)) {
                    setConditionTitle = "設定既修条件";
                } else if (CONDITION_HEI.equals(compData._conditionDiv)) {
                    setConditionTitle = "設定併修条件";
                } else if (CONDITION_HEI_KIN.equals(compData._conditionDiv)) {
                    setConditionTitle = "設定併修禁止条件";
                } else if (CONDITION_KEIZOKU.equals(compData._conditionDiv)) {
                    setConditionTitle = "設定継続履修条件";
                } else if (CONDITION_KEIZOKU_KIN.equals(compData._conditionDiv)) {
                    setConditionTitle = "設定継続履修禁止条件";
                }
                svf.VrsOut("CONDITION_TITLE", setConditionTitle);
            }
            svf.VrsOut("SUBCLASSCD", compData._groupCd);
            svf.VrsOut("SUBCLASSNAME", compData._groupName);
            svf.VrsOut("GRADE", compData._gradeName);
            svf.VrsOut("SUBCLASSCD_MASK", compData._groupCd);
            svf.VrsOut("GRADE_MASK", compData._grade);
            final String[] injiArray = KNJ_EditEdit.get_token(compData._condition, 100, 20);
            if (null != injiArray) {
                for (int i = 0; i < injiArray.length; i++) {
                    if (null == injiArray[i] || injiArray[i].length() == 0) {
                        break;
                    }
                    svf.VrsOut("SUBCLASSCD_MASK", compData._groupCd);
                    svf.VrsOut("GRADE_MASK", compData._grade);
                    svf.VrsOut("DETAIL", injiArray[i]);
                    svf.VrEndRecord();
                }
            }
            befDiv = compData._conditionDiv;
            _hasData = true;
        }
    }

    private void printSentaku(final Vrw32alp svf, final List printDataList, final String key) {
        setHead(svf, key);
        for (final Iterator itData = printDataList.iterator(); itData.hasNext();) {
            final SubclassCompSelectData compSelectData = (SubclassCompSelectData) itData.next();
            svf.VrsOut("SELECT_GROUPNO", compSelectData._groupCd);
            svf.VrsOut("SELECT_GROUPNAME", compSelectData._name);
            svf.VrsOut("GRADE", compSelectData._gradeName);
            svf.VrsOut("COURSENAME", compSelectData._courseName);
            svf.VrsOut("MAX_SUBCLASSES", compSelectData._jougen);
            svf.VrsOut("MIN_SUBCLASSES", compSelectData._kagen);
            svf.VrsOut("SELECT_GROUPNO_MASK", compSelectData._groupCd);
            svf.VrsOut("GRADE_MASK", compSelectData._grade);
            for (final Iterator iter = compSelectData._subclassList.iterator(); iter.hasNext();) {
                final SubclassData subclassData = (SubclassData) iter.next();
                svf.VrsOut("SELECT_GROUPNO_MASK", compSelectData._groupCd);
                svf.VrsOut("GRADE_MASK", compSelectData._grade);
                svf.VrsOut("SUBCLASSCD", subclassData._key);
                svf.VrsOut("SUBCLASSNAME", subclassData._name);
                svf.VrEndRecord();
            }
            _hasData = true;
        }
    }

    private void printHituRisyu(final Vrw32alp svf, final List printDataList, final String key) {
        setHead(svf, key);
        for (final Iterator itData = printDataList.iterator(); itData.hasNext();) {
            final SubclassCompGroupData compGroupData = (SubclassCompGroupData) itData.next();
            svf.VrsOut("REQUIREDCD", compGroupData._groupCd);
            svf.VrsOut("REQUIREDNAME", compGroupData._groupName);
            svf.VrsOut("GRADE", compGroupData._gradeName);
            svf.VrsOut("SUBCLASSCD_MASK", compGroupData._groupCd);
            svf.VrsOut("GRADE_MASK", compGroupData._grade);
            final String[] injiArray = KNJ_EditEdit.get_token(compGroupData._condition, 100, 20);
            if (null != injiArray) {
                for (int i = 0; i < injiArray.length; i++) {
                    if (null == injiArray[i] || injiArray[i].length() == 0) {
                        break;
                    }
                    svf.VrsOut("REQUIREDCD_MASK", compGroupData._groupCd);
                    svf.VrsOut("GRADE_MASK", compGroupData._grade);
                    svf.VrsOut("DETAIL", injiArray[i]);
                    svf.VrEndRecord();
                }
            }
            _hasData = true;
        }
    }

    private void setHead(final Vrw32alp svf, final String key) {
        final String formName = JOUKEN_RISYU.equals(key) ? "KNJB0200.frm" : JOUKEN_SETTEI.equals(key) ? "KNJZ239.frm" : "KNJB0210.frm";
        svf.VrSetForm(formName, 4);
        svf.VrsOut("NENDO", _param._year + "年度");
        svf.VrsOut("DATE", StringUtils.replace(_param._ctrlDate, "-", "/"));
    }

    private Map getPrintMap(final DB2UDB db2) throws SQLException {
        final Map retMap = new TreeMap();
        if (JOUKEN_RISYU.equals(_param._kubun)) {
            retMap.put(JOUKEN_RISYU, getRisyu(db2));
        } else if (JOUKEN_SETTEI.equals(_param._kubun)) {
            retMap.put(JOUKEN_SETTEI, getSentaku(db2));
        } else if (JOUKEN_HITURISYU.equals(_param._kubun)) {
            retMap.put(JOUKEN_HITURISYU, getHituRisyu(db2));
        } else if (JOUKEN_ALL.equals(_param._kubun)) {
            retMap.put(JOUKEN_RISYU, getRisyu(db2));
            retMap.put(JOUKEN_SETTEI, getSentaku(db2));
            retMap.put(JOUKEN_HITURISYU, getHituRisyu(db2));
        }
        return retMap;
    }

    private List getRisyu(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        final Map subclassData = getSubclassData(db2, JOUKEN_RISYU);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getRisyuSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            String befSubCd = "";
            SubclassCompGroupData compData = null;
            String compSep = "";
            String condition = "";
            while (rs.next()) {
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String subclassName = rs.getString("SUBCLASSNAME");
                final String conditionDiv = rs.getString("CONDITION_DIV");
                final String grade = rs.getString("GRADE");
                final String gradeName = rs.getString("GRADE_NAME1");
                final String compKey = subclassCd + "-" + conditionDiv + "-" + grade;
                if (!"".equals(befSubCd) && !befSubCd.equals(compKey)) {
                    retList.add(compData);
                    compSep = "";
                    condition = "";
                }
                condition = compSep + "(" + rs.getString("CONDITION") + ")";

                for (final Iterator iter = subclassData.keySet().iterator(); iter.hasNext();) {
                    final String key = (String) iter.next();
                    final Map dataMap = (Map) subclassData.get(key);
                    if (compKey.equals(key)) {
                        for (final Iterator itSubD = dataMap.keySet().iterator(); itSubD.hasNext();) {
                            final String subKey = (String) itSubD.next();
                            final SubclassData data = (SubclassData) dataMap.get(subKey);
                            condition = StringUtils.replace(condition, "'" + data._conSub + "'", data._name);
                        }
                    }
                }

                if (!befSubCd.equals(compKey)) {
                    compData = new SubclassCompGroupData(subclassCd, subclassName, conditionDiv, grade, gradeName, condition);
                } else {
                    compData.setCondition(condition);
                }
                befSubCd = compKey;
                compSep = " OR ";
            }
            if (null != compData) {
                retList.add(compData);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retList;
    }

    private String getRisyuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     L1.SUBCLASSNAME, ");
        stb.append("     T1.CONDITION_DIV, ");
        stb.append("     T1.CONDITION_SEQ, ");
        stb.append("     T1.GRADE, ");
        stb.append("     GD.GRADE_NAME1, ");
        stb.append("     T1.CONDITION ");
        stb.append(" FROM ");
        stb.append("     COMP_CONDITION_DAT T1 ");
        stb.append("     LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
        stb.append("          AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
        stb.append("          AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
        stb.append("          AND T1.SUBCLASSCD = L1.SUBCLASSCD ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GD ON T1.YEAR = GD.YEAR ");
        stb.append("          AND T1.GRADE = GD.GRADE ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND) && !StringUtils.isBlank(_param._SCHOOLCD)) {
            stb.append("   AND T1.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
        }
        if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
            stb.append("       AND GD.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.CONDITION_DIV, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     L1.SUBCLASSNAME, ");
        stb.append("     T1.CONDITION_SEQ ");
        return stb.toString();
    }

    private List getSentaku(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSentakuSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String gradeName = rs.getString("GRADE_NAME1");
                final String course = rs.getString("COURSE");
                final String courseName = rs.getString("COURSENAME");
                final String groupCd = rs.getString("GROUPCD");
                final String name = rs.getString("NAME");
                final String credits = rs.getString("CREDITS");
                final String jougen = rs.getString("JOUGEN");
                final String kagen = rs.getString("KAGEN");

                final SubclassCompSelectData compSelectData = new SubclassCompSelectData(grade, gradeName, course, courseName, groupCd, name, credits, jougen, kagen);
                compSelectData.setSubclass(db2);
                retList.add(compSelectData);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retList;
    }

    private String getSentakuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.GRADE, ");
        stb.append("     GD.GRADE_NAME1, ");
        stb.append("     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE AS COURSE, ");
        stb.append("     CM.COURSENAME || MM.MAJORNAME || CCM.COURSECODENAME AS COURSENAME, ");
        stb.append("     T1.GROUPCD, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.CREDITS, ");
        stb.append("     T1.JOUGEN, ");
        stb.append("     T1.KAGEN ");
        stb.append(" FROM ");
        stb.append("     SUBCLASS_COMP_SELECT_MST T1 ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GD ON T1.YEAR = GD.YEAR ");
        stb.append("          AND T1.GRADE = GD.GRADE ");
        stb.append("     LEFT JOIN COURSE_MST CM ON T1.COURSECD = CM.COURSECD ");
        stb.append("     LEFT JOIN MAJOR_MST MM ON T1.COURSECD = MM.COURSECD ");
        stb.append("          AND T1.MAJORCD = MM.MAJORCD ");
        stb.append("     LEFT JOIN COURSECODE_MST CCM ON T1.COURSECODE = CCM.COURSECODE ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
            stb.append("       AND GD.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.GROUPCD ");
        return stb.toString();
    }

    private List getHituRisyu(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        final Map subclassData = getSubclassData(db2, JOUKEN_HITURISYU);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getHituRisyuSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            String befSubCd = "";
            SubclassCompGroupData compData = null;
            String compSep = "";
            String condition = "";
            while (rs.next()) {
                final String groupCd = rs.getString("GROUP_CD");
                final String groupName = rs.getString("GROUP_NAME");
                final String conditionDiv = rs.getString("CONDITION_DIV");
                final String grade = rs.getString("GRADE");
                final String gradeName = rs.getString("GRADE_NAME1");
                final String compKey = groupCd + "-" + conditionDiv + "-" + grade;
                if (!"".equals(befSubCd) && !befSubCd.equals(compKey)) {
                    retList.add(compData);
                    compSep = "";
                    condition = "";
                }
                condition = compSep + "(" + rs.getString("CONDITION") + ")";

                for (final Iterator iter = subclassData.keySet().iterator(); iter.hasNext();) {
                    final String key = (String) iter.next();
                    final Map dataMap = (Map) subclassData.get(key);
                    if (compKey.equals(key)) {
                        for (final Iterator itSubD = dataMap.keySet().iterator(); itSubD.hasNext();) {
                            final String subKey = (String) itSubD.next();
                            final SubclassData data = (SubclassData) dataMap.get(subKey);
                            condition = StringUtils.replace(condition, "'" + data._conSub + "'", data._name);
                        }
                    }
                }

                if (!befSubCd.equals(compKey)) {
                    compData = new SubclassCompGroupData(groupCd, groupName, conditionDiv, grade, gradeName, condition);
                } else {
                    compData.setCondition(condition);
                }
                befSubCd = compKey;
                compSep = " OR ";
            }
            if (null != compData) {
                retList.add(compData);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retList;
    }

    private String getHituRisyuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.GROUP_CD, ");
        stb.append("     L1.GROUP_NAME, ");
        stb.append("     T1.CONDITION_DIV, ");
        stb.append("     T1.CONDITION_SEQ, ");
        stb.append("     T1.GRADE, ");
        stb.append("     GD.GRADE_NAME1, ");
        stb.append("     T1.CONDITION ");
        stb.append(" FROM ");
        stb.append("     COMP_CONDITION_GROUP_DAT T1 ");
        stb.append("     LEFT JOIN COMP_CONDITION_GROUP_MST L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.GROUP_CD = L1.GROUP_CD ");
        stb.append("          AND T1.GRADE = L1.GRADE ");
        stb.append("          AND T1.HR_CLASS = L1.HR_CLASS ");
        stb.append("          AND T1.COURSECD = L1.COURSECD ");
        stb.append("          AND T1.MAJORCD = L1.MAJORCD ");
        stb.append("          AND T1.COURSECODE = L1.COURSECODE ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GD ON T1.YEAR = GD.YEAR ");
        stb.append("          AND T1.GRADE = GD.GRADE ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
            stb.append("       AND GD.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.CONDITION_DIV, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.GROUP_CD, ");
        stb.append("     L1.GROUP_NAME, ");
        stb.append("     T1.CONDITION_SEQ ");
        return stb.toString();
    }

    private class SubclassCompData {
        private final String _subclassCd;
        private final String _subclassName;
        private final String _conditionDiv;
        private final String _grade;
        private final String _gradeName;
        private String _condition;

        SubclassCompData(
            final String subclassCd,
            final String subclassName,
            final String conditionDiv,
            final String grade,
            final String gradeName,
            final String condition
        ) {
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _conditionDiv = conditionDiv;
            _grade = grade;
            _gradeName = gradeName;
            _condition = condition;
        }

        private void setCondition (final String condition) {
            _condition = _condition + condition;
        }
    }

    private class SubclassCompSelectData {
        private final String _grade;
        private final String _gradeName;
        private final String _course;
        private final String _courseName;
        private final String _groupCd;
        private final String _name;
        private final String _credits;
        private final String _jougen;
        private final String _kagen;
        private final List _subclassList;

        SubclassCompSelectData(
            final String grade,
            final String gradeName,
            final String course,
            final String courseName,
            final String groupCd,
            final String name,
            final String credits,
            final String jougen,
            final String kagen
        ) {
            _grade = grade;
            _gradeName = gradeName;
            _course = course;
            _courseName = courseName;
            _groupCd = groupCd;
            _name = name;
            _credits = credits;
            _jougen = jougen;
            _kagen = kagen;
            _subclassList = new ArrayList();
        }

        private void setSubclass (final DB2UDB db2) throws SQLException {
            final String sql = getSubclassSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final SubclassData subclassData = new SubclassData(rs.getString("KEY"), "", "", rs.getString("SUBCLASSNAME"), "");
                    _subclassList.add(subclassData);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String getSubclassSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS KEY, ");
            stb.append("     L1.SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("     SUBCLASS_COMP_SELECT_DAT T1 ");
            stb.append("     LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD = L1.CLASSCD || L1.SCHOOL_KIND || L1.CURRICULUM_CD || L1.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.GRADE = '" + _grade + "' ");
            stb.append("     AND T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE = '" + _course + "' ");
            stb.append("     AND T1.GROUPCD = '" + _groupCd + "' ");
            if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
                stb.append("       AND T1.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     KEY ");
            return stb.toString();
        }
    }

    private class SubclassCompGroupData {
        private final String _groupCd;
        private final String _groupName;
        private final String _conditionDiv;
        private final String _grade;
        private final String _gradeName;
        private String _condition;

        SubclassCompGroupData(
            final String groupCd,
            final String groupName,
            final String conditionDiv,
            final String grade,
            final String gradeName,
            final String condition
        ) {
            _groupCd = groupCd;
            _groupName = groupName;
            _conditionDiv = conditionDiv;
            _grade = grade;
            _gradeName = gradeName;
            _condition = condition;
        }

        private void setCondition (final String condition) {
            _condition = _condition + condition;
        }
    }

    private Map getSubclassData(final DB2UDB db2, final String div) throws SQLException {
        final Map retMap = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = JOUKEN_RISYU.equals(div) ? getSubclassName() : getSubclassGroupName();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            String befSub = "";
            while (rs.next()) {
                Map subMap = new HashMap();
                final SubclassData subclassData = new SubclassData(rs.getString("KEY"), rs.getString("HEIKOU_FLG"), rs.getString("CREDITS"), rs.getString("SUBCLASSNAME"), rs.getString("CONSUB"));
                if (befSub.equals(rs.getString("KEY"))) {
                    subMap = (Map) retMap.get(rs.getString("KEY"));
                }
                if ("".equals(befSub) || befSub.equals(rs.getString("KEY"))) {
                    subMap.put(rs.getString("CONSUB"), subclassData);
                } else {
                    subMap.put(rs.getString("CONSUB"), subclassData);
                }
                retMap.put(rs.getString("KEY"), subMap);
                befSub = rs.getString("KEY");
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;
    }

    private String getSubclassName() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || '-' || T1.CONDITION_DIV || '-' || T1.GRADE AS KEY, ");
        stb.append("     T1.CON_CLASSCD || '-' || T1.CON_SCHOOL_KIND || '-' || T1.CON_CURRICULUM_CD || '-' || T1.CON_SUBCLASSCD AS CONSUB, ");
        stb.append("     T1.HEIKOU_FLG, ");
        stb.append("     T1.CREDITS, ");
        stb.append("     L1.SUBCLASSNAME ");
        stb.append(" FROM ");
        stb.append("     COMP_CONDITION_SUBCLASS_DAT T1 ");
        stb.append("     LEFT JOIN SUBCLASS_MST L1 ON T1.CON_CLASSCD || T1.CON_SCHOOL_KIND || T1.CON_CURRICULUM_CD || T1.CON_SUBCLASSCD = L1.CLASSCD || L1.SCHOOL_KIND || L1.CURRICULUM_CD || L1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
            stb.append("       AND T1.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
            stb.append("       AND T1.CON_SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     KEY, ");
        stb.append("     CONSUB ");
        return stb.toString();
    }

    private String getSubclassGroupName() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.GROUP_CD || '-' || T1.CONDITION_DIV || '-' || T1.GRADE AS KEY, ");
        stb.append("     T1.CON_CLASSCD || '-' || T1.CON_SCHOOL_KIND || '-' || T1.CON_CURRICULUM_CD || '-' || T1.CON_SUBCLASSCD AS CONSUB, ");
        stb.append("     T1.HEIKOU_FLG, ");
        stb.append("     T1.CREDITS, ");
        stb.append("     L1.SUBCLASSNAME ");
        stb.append(" FROM ");
        stb.append("     COMP_CONDITION_SUBCLASS_GROUP_DAT T1 ");
        stb.append("     LEFT JOIN SUBCLASS_MST L1 ON T1.CON_CLASSCD || T1.CON_SCHOOL_KIND || T1.CON_CURRICULUM_CD || T1.CON_SUBCLASSCD = L1.CLASSCD || L1.SCHOOL_KIND || L1.CURRICULUM_CD || L1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
            stb.append("       AND T1.CON_SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     KEY, ");
        stb.append("     CONSUB ");
        return stb.toString();
    }

    private class SubclassData {
        private final String _key;
        private final String _conSub;
        private final String _heikouFlg;
        private final String _credits;
        private final String _name;

        SubclassData(final String key, final String heikouFlg, final String credits, final String name, final String consub) {
            _key = key;
            _conSub = consub;
            _heikouFlg = heikouFlg;
            _credits = credits;
            _name = name + ("1".equals(_heikouFlg) ? "[並行履修可]" : "");
        }

    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 66053 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _kubun;
        private final String _useSchool_KindField;
        private final String _SCHOOLCD;
        private final String _SCHOOLKIND;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _kubun = request.getParameter("KUBUN");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD= request.getParameter("SCHOOLCD");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
        }

    }
}

// eof
