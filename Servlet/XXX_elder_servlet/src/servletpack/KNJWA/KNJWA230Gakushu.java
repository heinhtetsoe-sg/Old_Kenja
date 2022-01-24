// kanji=漢字
/*
 * $Id: b1259e4db1b1954105a1f73346f8f74ece96723f $
 *
 * 作成日: 2008/06/19 17:23:49 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJWA.KNJWA230.HrClass;
import servletpack.KNJWA.KNJWA230.Param;
import servletpack.KNJWA.KNJWA230.Student;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: b1259e4db1b1954105a1f73346f8f74ece96723f $
 */
public class KNJWA230Gakushu {

    private static final Log log = LogFactory.getLog(KNJWA230Gakushu.class);
    private static final int MAX_HR_CNT = 6;

    private static final String SOUGOU_CLASSCD = "11";
    Param _param;
    Vrw32alp _svf;
    DB2UDB _db2;

    TreeMap _titleMap = new TreeMap();      // 学年（年度）項目名
    TreeMap _pageTitleMap = new TreeMap();  // 学年（年度）項目名(ページ別)
    TreeMap _titleYear = new TreeMap();     // 学年（年度）項目名

    public boolean printData(final DB2UDB db2, final Vrw32alp svf, final Param param, final Student student) throws Exception {
        boolean hasData = false;
        _db2 = db2;
        _param = param;
        _svf = svf;

        final PreparedStatement psTitle = _db2.prepareStatement(_param.getStudyRecSql("TITLE", null));
        try {

            setMaps(student, psTitle);
            //学習データを改ページ単位に分割
            final Map gakushuPrintMap = new HashMap();
            StringBuffer yearInState = new StringBuffer();
            int gakushuCnt = 1;
            int gakushuMapCnt = 1;
            String sep = "";
            for (final Iterator itGakushu = _titleYear.keySet().iterator(); itGakushu.hasNext();) {
                if (gakushuCnt > MAX_HR_CNT) {
                    gakushuPrintMap.put(new Integer(gakushuMapCnt), yearInState.toString());
                    yearInState = new StringBuffer();
                    gakushuMapCnt++;
                    gakushuCnt = 1;
                    sep = "";
                }
                final String gakushuYear = (String) itGakushu.next();
                yearInState.append(sep + "'" + gakushuYear + "'");
                gakushuCnt++;
                sep = ",";
            }
            if (gakushuCnt > 1) {
                gakushuPrintMap.put(new Integer(gakushuMapCnt), yearInState.toString());
            }

            //在籍データを改ページ単位に分割
            final Map regdPrintMap = new HashMap();
            List regdPageList = new ArrayList();
            int hrCnt = 1;
            int hrMapCnt = 1;
            for (final Iterator itRegd = student._regdHist.iterator(); itRegd.hasNext();) {
                if (hrCnt > MAX_HR_CNT) {
                    regdPrintMap.put(new Integer(hrMapCnt), regdPageList);
                    regdPageList = new ArrayList();
                    hrMapCnt++;
                    hrCnt = 1;
                }
                final HrClass hrClass = (HrClass) itRegd.next();
                regdPageList.add(hrClass);
                hrCnt++;
            }
            if (hrCnt > 1) {
                regdPrintMap.put(new Integer(hrMapCnt), regdPageList);
            }

            //roop用
            final int roopCnt = gakushuMapCnt > hrMapCnt ? gakushuMapCnt : hrMapCnt;
            int sortChange = 0;
            for (int i = 1; i <= roopCnt; i++) {
                _svf.VrSetForm("KNJWA230_3.frm", 4);
                final Integer pageCnt = new Integer(i);
                printStudentInfo(student, regdPrintMap, pageCnt, sortChange);

                if (gakushuPrintMap.containsKey(pageCnt)) {
                    final String inState = (String) gakushuPrintMap.get(pageCnt);
                    final PreparedStatement psSelect = _db2.prepareStatement(_param.getStudyRecSql("SELECT", inState));
                    try {
                        printGakushuKiroku(student, psSelect);
                    } finally {
                        DbUtils.close(psSelect);
                    }
                }
                sortChange++;
            }
            if (!hasData) {
                // 学習情報がない場合の処理
                _svf.VrsOut("CLASSCD", "A"); // 教科コード
                _svf.VrEndRecord();
            }

            hasData = true;
        } finally {
            DbUtils.close(psTitle);
            _db2.commit();
        }
        return hasData;
    }

    private void setMaps(final Student student, final PreparedStatement psTitle) throws SQLException {
        _titleMap.clear();
        _pageTitleMap.clear();
        _titleYear.clear();
        Map pageTitleMap = new TreeMap();
        int titleCnt = 1;
        int pageCnt = 1;
        ResultSet rs = null;
        try {
            int p = 1;
            psTitle.setString(p++, student._schregno);
            psTitle.setString(p++, student._schregno);
            rs = psTitle.executeQuery();
            while (rs.next()) {
                if (titleCnt > MAX_HR_CNT) {
                    _pageTitleMap.put(new Integer(pageCnt), pageTitleMap);
                    pageTitleMap = new TreeMap();
                    pageCnt++;
                    titleCnt = 1;
                }
                String nendo = nao_package.KenjaProperties.gengou(rs.getInt("YEAR"));
                _titleYear.put(rs.getString("YEAR"), Integer.valueOf(rs.getString("ANNUAL")));
                _titleMap.put(Integer.valueOf(rs.getString("ANNUAL")), nendo);
                pageTitleMap.put(Integer.valueOf(rs.getString("ANNUAL")), nendo);
                titleCnt++;
            }
            if (titleCnt > 1) {
                _pageTitleMap.put(new Integer(pageCnt), pageTitleMap);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            _db2.commit();
        }
    }

    private void printStudentInfo(final Student student, final Map regdPrintMap, final Integer pageCnt, final int sortChange) {

        final String nameField = student._name.length() > 10 ? "2" : "1";
        _svf.VrsOut("NAME" + nameField, student._name);

        final String schoolNameField = _param._schoolName.length() > 11 ? "2" : "1";
        _svf.VrsOut("SCHOOLNAME" + schoolNameField, _param._schoolName);

        if (regdPrintMap.containsKey(pageCnt)) {
            final List regdList = (List) regdPrintMap.get(pageCnt);
            for (final Iterator itRegd = regdList.iterator(); itRegd.hasNext();) {
                final HrClass hrClass = (HrClass) itRegd.next();
                final String nendo = _param.changePrintYear(hrClass._year, false);
                final int sort = Integer.parseInt(hrClass._sort) - (sortChange * 6);
                _svf.VrsOut("NENDO" + sort + "_1", nendo.substring(0, 2));
                _svf.VrsOut("NENDO" + sort + "_2", nendo.substring(2));
                _svf.VrsOut("HR_CLASS" + sort, hrClass._hrNameAbbv);
                _svf.VrsOut("SCHREGNO" + sort, student._schregno);
            }
        }
        if (_pageTitleMap.containsKey(pageCnt)) {
            final TreeMap titleMap = (TreeMap) _pageTitleMap.get(pageCnt);
            for (Iterator t = titleMap.keySet().iterator(); t.hasNext();) {
                Integer value = (Integer) t.next();
                final int sort = value.intValue() - (sortChange * 6);
                String str = (String) titleMap.get(value);
                _svf.VrsOut("YEAR" + sort, str);
            }
        }
    }

    private void printGakushuKiroku(final Student student, final PreparedStatement psSelect) throws SQLException {

        ResultSet rs = null;
        try {
            int pp = 1;
            psSelect.setString(pp++, student._schregno);
            psSelect.setString(pp++, student._schregno);
            rs = psSelect.executeQuery();

            final Map printSougouMap = new TreeMap();
            String sougouRemark = "";
            String befSougouRemark = "";

            final String ryugakuClassCd = "98";
            final Map printRyugakuMap = new TreeMap();
            String ryugakuRemark = "";
            String befRyugakuRemark = "";

            final List printList = new ArrayList();
            final Map printSyouKeiMap = new TreeMap();
            final Map printGoukeiMap = new TreeMap();
            PrintData printData = null;
            final PrintTotalData printTotalData = new PrintTotalData();
            boolean hasData = false;
            String befClassCd = "";
            String befSubclassCd = "";
            LineData lineData = null;
            while (rs.next()) {

                final String strVal = String.valueOf(rs.getInt("VALUATION"));
                final String strMinMiri = String.valueOf(rs.getInt("MIN_MIRI"));
                final int strStudyCnt = rs.getInt("STUDY_CNT");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String valution = getValuation(rs.getString("SCHOOLCD"), strVal);
                final int getCredit = null == rs.getString("GET_CREDIT") || (strMinMiri.equals("1") && strStudyCnt > 0) ? 0 : rs.getInt("GET_CREDIT");
                printTotalData.setTotalCredit(rs.getString("CLASSCD"), getCredit);
                final String annual = rs.getString("ANNUAL");

                if (!printGoukeiMap.containsKey(annual)) {
                    printData = new PrintData(rs.getString("YEAR"), annual, valution, String.valueOf(getCredit), strMinMiri, strStudyCnt, curriculumCd);
                    printGoukeiMap.put(annual, printData);
                } else {
                    printData = (PrintData) printGoukeiMap.get(annual);
                    printData.addCredit(getCredit);
                }

                if (rs.getString("CLASSCD").equals(ryugakuClassCd)) {
                    if (!printRyugakuMap.containsKey(annual)) {
                        printData = new PrintData(rs.getString("YEAR"), annual, valution, String.valueOf(getCredit), strMinMiri, strStudyCnt, curriculumCd);
                        printRyugakuMap.put(annual, printData);
                    } else {
                        printData = (PrintData) printRyugakuMap.get(annual);
                        printData.addCredit(getCredit);
                    }
                    final String remark = rs.getString("FORMER_REG_SCHOOLCD");
                    if (null != remark && remark.length() > 0) {
                        if (!befRyugakuRemark.equals(remark)) {
                            final String sep = ryugakuRemark.length() > 0 ? "," : "";
                            ryugakuRemark = ryugakuRemark + sep + remark;
                        }
                        befRyugakuRemark = remark;
                    }
                    continue;
                }

                if (!printSyouKeiMap.containsKey(annual)) {
                    printData = new PrintData(rs.getString("YEAR"), annual, valution, String.valueOf(getCredit), strMinMiri, strStudyCnt, curriculumCd);
                    printSyouKeiMap.put(annual, printData);
                } else {
                    printData = (PrintData) printSyouKeiMap.get(annual);
                    printData.addCredit(getCredit);
                }

                if (rs.getString("CLASSCD").equals(SOUGOU_CLASSCD)) {
                    if (!printSougouMap.containsKey(annual)) {
                        printData = new PrintData(rs.getString("YEAR"), annual, valution, String.valueOf(getCredit), strMinMiri, strStudyCnt, curriculumCd);
                        printSougouMap.put(annual, printData);
                    } else {
                        printData = (PrintData) printSougouMap.get(annual);
                        printData.addCredit(getCredit);
                    }
                    final String remark = rs.getString("FORMER_REG_SCHOOLCD");
                    if (null != remark && remark.length() > 0) {
                        if (!befSougouRemark.equals(remark)) {
                            final String sep = sougouRemark.length() > 0 ? "," : "";
                            sougouRemark = sougouRemark + sep + remark;
                        }
                        befSougouRemark = remark;
                    }
                    continue;
                }

                if (!hasData || !befClassCd.equals(rs.getString("CLASSCD")) || !befSubclassCd.equals(rs.getString("SUBCLASSCD"))) {
                    if (hasData && (!befClassCd.equals(rs.getString("CLASSCD")) || !befSubclassCd.equals(rs.getString("SUBCLASSCD")))) {
                        printList.add(lineData);
                    }
                    lineData = new LineData(rs.getString("CLASSCD"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSCD"), rs.getString("SUBCLASSNAME"), 0);
                }

                lineData.setPrintData(getCredit, rs.getString("YEAR"), annual, valution, rs.getString("FORMER_REG_SCHOOLCD"), strMinMiri, strStudyCnt, curriculumCd);
                befClassCd = rs.getString("CLASSCD");
                befSubclassCd = rs.getString("SUBCLASSCD");
                hasData = true;
            }
            printNotRecordField(printSougouMap, "TOTALSTUDY_CREDIT", student);
            printRemark(sougouRemark);
            printNotRecordField(printRyugakuMap, "ABROAD_CREDIT", student);
            printNotRecordField(printSyouKeiMap, "SUBTOTAL_CREDIT", student);
            printNotRecordField(printGoukeiMap, "TOTAL_CREDIT", student);
            if (hasData) {
                printList.add(lineData);
                //明細印字
                meisaiPrintOut(printList, printTotalData, student);
            } else {
                // 学習情報がない場合の処理
                _svf.VrsOut("CLASSCD", "A"); // 教科コード
                _svf.VrEndRecord();
            }
        } finally {
            DbUtils.closeQuietly(rs);
        }
    }

    private void printRemark(final String sougouRemark) {
        final int remarkLen = sougouRemark.length();
        if (remarkLen > 0) {
            final String remarkField = remarkLen > 30 ? "3" : remarkLen > 13 ? "2" : "1";
            _svf.VrsOut("TOTALSTUDY_REMARK" + remarkField, sougouRemark);
        }
    }

    private void printNotRecordField(final Map printMap, final String field, final Student student) throws SQLException {
        int total = 0;
        for (final Iterator iter = printMap.keySet().iterator(); iter.hasNext();) {
            final String keyAnnual = (String) iter.next();
            PrintData print = (PrintData) printMap.get(keyAnnual);

            if (field.equals("TOTALSTUDY_CREDIT")) {
                final String mirishuCre = getMirishu(student, print);
                final String printCre = mirishuCre.equals("") ? print._credit : print._credit + "(" + mirishuCre + ")";
                final String fieldNo = mirishuCre.equals("") ? "" : "_2";
                _svf.VrsOut(field + print._annual + fieldNo, printCre);
            } else {
                _svf.VrsOut(field + print._annual, print._credit);
            }
            total += Integer.parseInt(print._credit);
        }
        _svf.VrsOut(field, String.valueOf(total));
    }

    /**
     * 評定データ
     */
    private String getValuation(final String schoolCd, final String valuation) {
        if (schoolCd.equals("0")) {
            return null != valuation ? valuation : "";
        } else if (schoolCd.equals("1")) {
            return null != valuation ? valuation : "";
        } else if (schoolCd.equals("2")) {
            return "大";
        } else if (schoolCd.equals("3")) {
            return "高";
        } else if (schoolCd.equals("4")) {
            return "技";
        } else {
            return "履中";
        }
    }

    private class PrintTotalData {
        final String sougouClassCd = "11";
        int _sougouCredit = 0;
        int _totalCredit = 0;

        public PrintTotalData() {
        }

        private void setTotalCredit(final String classCd, final int credit) {
            if (sougouClassCd.equals(classCd)) {
                _sougouCredit += credit;
            }
            _totalCredit += credit;
        }
    }

    private class LineData {
        final String _classCd;
        final String _className;
        final String _subclassCd;
        final String _subclassName;
        final List _printData;
        Map _printDataTotal = new HashMap();
        int _totalCredit = 0;
        String _remark;
        String _befRemark;

        public LineData(
                final String classCd,
                final String className,
                final String subclassCd,
                final String subclassName,
                final int totalCredit
        ) {
            _classCd = classCd;
            _className = className;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _printData = new ArrayList();
            _totalCredit += totalCredit;
            _remark = "";
            _befRemark = "";
        }

        private void setPrintData(
                final int credit,
                final String year,
                final String annual,
                final String value,
                final String remark,
                final String strMinMiri,
                final int strStudyCnt,
                final String curriculumCd
        ) {
            _totalCredit += credit;
            PrintData printData = new PrintData(year, annual, value, String.valueOf(credit), strMinMiri, strStudyCnt, curriculumCd);
            _printData.add(printData);
            addRemark(remark);
            PrintDataTotal printDataTotal = new PrintDataTotal(credit, value);
            if (_printDataTotal.containsKey(annual)) {
                printDataTotal = (PrintDataTotal) _printDataTotal.get(annual);
                printDataTotal.addData(credit, value);
            }
            _printDataTotal.put(annual, printDataTotal);
        }

        private void addRemark(final String remark) {
            if (null != remark && remark.length() > 0) {
                if (!_befRemark.equals(remark)) {
                    final String sep = _remark.length() > 0 ? "," : "";
                    _remark = _remark + sep + remark;
                }
                _befRemark = remark;
            }
        }
    }

    private class PrintData {
        final String _year;
        final String _annual;
        final String _value;
        final String _curriculumCd;
        String _credit;
        String _minMiri;
        int _studyCnt;

        public PrintData(
                final String year,
                final String annual,
                final String value,
                final String credit,
                final String minMiri,
                final int studyCnt,
                final String curriculumCd
        ) {
            _year = year;
            _annual = annual;
            _value = value;
            _credit = credit;
            _minMiri = minMiri;
            _studyCnt = studyCnt;
            _curriculumCd = curriculumCd;
        }

        public void addCredit(final int credit) {
            final int cre = Integer.parseInt(_credit);
            _credit = String.valueOf(cre + credit);
        }
    }

    private class PrintDataTotal {
        String _value;
        int _credit;

        public PrintDataTotal(final int credit, final String value) {
            _value = value;
            _credit = credit;
        }

        public void addData(final int credit, final String value) {
            final String setValue = null == value || value.equals("") ? "0" : value;
            if (setValue.equals("大") || setValue.equals("高") || setValue.equals("技")) {
                _value = value;
            } else if (!_value.equals("大") && !_value.equals("高") && !_value.equals("技")) {
                _value = Integer.parseInt(_value) < Integer.parseInt(value) ? value : _value;
            }
            _credit += credit;
        }
    }

    private void meisaiPrintOut(final List printList, final PrintTotalData printTotalData, final Student student) throws SQLException {

        int fieldCnt = 1;
        String befClassCd = "";
        for (final Iterator itLine = printList.iterator(); itLine.hasNext();) {
            final LineData lineData = (LineData) itLine.next();
            _svf.VrsOut("CLASSCD", lineData._classCd); // 教科コード
            if (!befClassCd.equals(lineData._classCd)) {
                final String field = lineData._className.length() > 5 ? "2" : "1";
                _svf.VrsOut("CLASSNAME" + field, lineData._className);
            }
            if (null != lineData._subclassName) {
                final String field = lineData._subclassName.length() > 10 ? "2" : "1";
                _svf.VrsOut("SUBCLASSNAME" + field, lineData._subclassName);
            }
            for (final Iterator itPrint = lineData._printData.iterator(); itPrint.hasNext();) {
                final PrintData printData = (PrintData) itPrint.next();
                final PrintDataTotal printDataTotal = (PrintDataTotal) lineData._printDataTotal.get(printData._annual);
                final String printValue = printDataTotal._value;
                String cre = String.valueOf(printDataTotal._credit);
                if (null != printValue && !printValue.equals("大") && !printValue.equals("高") && !printValue.equals("技")) {
                    if (printData._minMiri.equals("1")) {
                        lineData._totalCredit -= printDataTotal._credit;
                        if (printData._studyCnt > 0) {
                            cre = "(" + getRisyuCredit(lineData, printData, student) + ")";
                        } else {
                            cre = "(" + cre + ")";
                        }
                    }
                    if (!_param.isW029(printData._year, lineData._subclassCd) && !printValue.equals("") && Integer.parseInt(printValue) >= 1) {
                        _svf.VrsOut("GRADES" + printData._annual, printValue);
                    } else if (_param.isW029(printData._year, lineData._subclassCd)) {
                        _svf.VrsOut("GRADES" + printData._annual, "-");
                    }
                } else {
                    if (!_param.isW029(printData._year, lineData._subclassCd) && null != printValue) {
                        _svf.VrsOut("GRADES" + printData._annual, printValue);
                    } else if (_param.isW029(printData._year, lineData._subclassCd)) {
                        _svf.VrsOut("GRADES" + printData._annual, "-");
                    }
                }
                _svf.VrsOut("CREDIT" + printData._annual, cre);
            }
            _svf.VrsOut("CREDIT", String.valueOf(lineData._totalCredit));
            String remark = lineData._remark;
            final int remarkLen = remark.length();
            if (remarkLen > 0) {
                final String remarkField = remarkLen > 30 ? "3" : remarkLen > 18 ? "2" : "1";
                _svf.VrsOut("REMARK" + remarkField, remark);
            }
            befClassCd = lineData._classCd;
            fieldCnt++;
            _svf.VrEndRecord();
        }
        for (int i = fieldCnt; i < 50; i++) {
            if (i == fieldCnt) {
                _svf.VrsOut("CLASSCD", ""); // 教科コード
            } else {
                _svf.VrsOut("CLASSCD", String.valueOf(i)); // 教科コード
            }
            _svf.VrEndRecord();
        }
    }

    private String getRisyuCredit(final LineData lineData, final PrintData printData, final Student student) throws SQLException {

        String ret = "";
        ResultSet rs = null;

        final String sql = "SELECT "
                         + "    T1.CREDITS, "
                         + "    L1.SUBCLASSCD2 "
                         + "FROM "
                         + "    SUBCLASS_DETAILS_MST T1 "
                         + "    LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD || T1.SUBCLASSCD || T1.CURRICULUM_CD = L1.CLASSCD || L1.SUBCLASSCD || L1.CURRICULUM_CD "
                         + "WHERE "
                         + "    T1.YEAR = '" + printData._year + "' "
                         + "    AND T1.CLASSCD = '" + lineData._classCd + "' "
                         + "    AND T1.SUBCLASSCD = '" + lineData._subclassCd + "' "
                         + "    AND T1.CURRICULUM_CD = '" + printData._curriculumCd + "' ";
        try {
            _db2.query(sql);
            rs = _db2.getResultSet();
            if (rs.next()) {
                if (rs.getString("SUBCLASSCD2") == null) {
                    ret = rs.getString("CREDITS") == null ? "" : rs.getString("CREDITS");
                    return ret;
                }
            }
        } finally {
            DbUtils.closeQuietly(rs);
            _db2.commit();
        }

        final String sql2 = ""
            + "WITH SUB_T AS ("
            + "SELECT DISTINCT "
            + "    T1.CLASSCD, "
            + "    T1.SUBCLASSCD, "
            + "    T1.CURRICULUM_CD "
            + "FROM "
            + "    SUBCLASS_MST T1 "
            + "    INNER JOIN COMP_REGIST_DAT I1 ON I1.YEAR = '" + printData._year + "' "
            + "          AND I1.SCHREGNO = '" + student._schregno + "' "
            + "          AND T1.CLASSCD || T1.SUBCLASSCD || T1.CURRICULUM_CD = I1.CLASSCD || I1.SUBCLASSCD || I1.CURRICULUM_CD "
            + "WHERE "
            + "    T1.CLASSCD = '" + lineData._classCd + "' "
            + "    AND T1.SUBCLASSCD2 = '" + lineData._subclassCd + "' "
            + "    AND T1.SUBCLASSCD != '" + lineData._subclassCd + "' "
            + "    AND T1.CURRICULUM_CD = '" + printData._curriculumCd + "' "
            + ") "
            + "SELECT "
            + "    SUM(T1.CREDITS) AS CREDITS "
            + "FROM "
            + "    SUBCLASS_DETAILS_MST T1 "
            + "    LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD || T1.SUBCLASSCD || T1.CURRICULUM_CD = L1.CLASSCD || L1.SUBCLASSCD || L1.CURRICULUM_CD "
            + "WHERE "
            + "    T1.YEAR = '" + printData._year + "' "
            + "    AND EXISTS(SELECT "
            + "                   'x' "
            + "               FROM "
            + "                   SUB_T "
            + "               WHERE "
            + "                   T1.CLASSCD = SUB_T.CLASSCD "
            + "                   AND T1.SUBCLASSCD = SUB_T.SUBCLASSCD "
            + "                   AND T1.CURRICULUM_CD = SUB_T.CURRICULUM_CD "
            + "              )";
        try {
            _db2.query(sql2);
            rs = _db2.getResultSet();
            if (rs.next()) {
                ret = rs.getString("CREDITS") == null ? "" : rs.getString("CREDITS");
            }
        } finally {
            DbUtils.closeQuietly(rs);
            _db2.commit();
        }
        return ret;
    }

    private String getMirishu(final Student student, final PrintData print) throws SQLException {

        String ret = "";
        ResultSet rs = null;
        final String sql = getMirisyuSql(print._year, SOUGOU_CLASSCD, student._schregno);

        try {
            _db2.query(sql);
            rs = _db2.getResultSet();

            if (rs.next()) {
                ret = rs.getString("CREDITS") == null ? "" : rs.getString("CREDITS");
            }
        } finally {
            DbUtils.closeQuietly(rs);
            _db2.commit();
        }

        return ret;
    }

    /**
     * 未履修科目取得
     * ?の値
     * YEAR, CLASSCD, SCHREGNO, YEAR, CLASSCD, SCHREGNO, YEAR, SCHREGNO, CLASSCD
     * 
     * @return
     */
    private String getMirisyuSql(final String year, final String classCd, final String schregno) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH STUDY_REC AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD ");
        stb.append(" FROM ");
        stb.append("     SCHREG_STUDYREC_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND T1.CLASSCD = '" + classCd + "' ");
        stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
        stb.append("     AND VALUE(T1.GET_CREDIT, 0) > 0 ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     L3.SCHREGNO, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD ");
        stb.append(" FROM ");
        stb.append("     ANOTHER_SCHOOL_GETCREDITS_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST L3 ON T1.APPLICANTNO = L3.APPLICANTNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND T1.CLASSCD = '" + classCd + "' ");
        stb.append("     AND L3.SCHREGNO = '" + schregno + "' ");
        stb.append("     AND ( ");
        stb.append("          (CAST(CAST(T1.GET_METHOD AS SMALLINT) + 1 AS CHAR(1)) = '2' ");
        stb.append("           OR ");
        stb.append("           CAST(CAST(T1.GET_METHOD AS SMALLINT) + 1 AS CHAR(1)) = '3' ");
        stb.append("          ) ");
        stb.append("          OR ");
        stb.append("          ( ");
        stb.append("           VALUE(T1.GET_CREDIT, 0) > 0 ");
        stb.append("          ) ");
        stb.append("         ) ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     SUM(I1.CREDITS) AS CREDITS ");
        stb.append(" FROM ");
        stb.append("     COMP_REGIST_DAT T1 ");
        stb.append("     INNER JOIN SUBCLASS_DETAILS_MST I1 ON T1.YEAR = I1.YEAR ");
        stb.append("          AND T1.CLASSCD = I1.CLASSCD ");
        stb.append("          AND T1.CURRICULUM_CD = I1.CURRICULUM_CD ");
        stb.append("          AND T1.SUBCLASSCD = I1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
        stb.append("     AND T1.CLASSCD = '" + classCd + "' ");
        stb.append("     AND NOT EXISTS( ");
        stb.append("            SELECT ");
        stb.append("                'x' ");
        stb.append("            FROM ");
        stb.append("                STUDY_REC E1 ");
        stb.append("            WHERE ");
        stb.append("                T1.YEAR = E1.YEAR ");
        stb.append("                AND T1.SCHREGNO = E1.SCHREGNO ");
        stb.append("                AND T1.CLASSCD = E1.CLASSCD ");
        stb.append("                AND T1.CURRICULUM_CD = E1.CURRICULUM_CD ");
        stb.append("                AND T1.SUBCLASSCD = E1.SUBCLASSCD ");
        stb.append("     ) ");

        log.debug(stb);
        return stb.toString();
    }

}
 // KNJWA230Gakushu

// eof
