// kanji=漢字
/*
 * $Id: 2cc13437c7e1d4d8e3b9b49fd6852e9f1cb063aa $
 *
 * 作成日: 2008/06/19 17:23:37 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWA;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJWA.KNJWA230.Param;
import servletpack.KNJWA.KNJWA230.Student;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 2cc13437c7e1d4d8e3b9b49fd6852e9f1cb063aa $
 */
public class KNJWA230Tani {

    private static final Log log = LogFactory.getLog(KNJWA230Tani.class);

    Param _param;
    Vrw32alp _svf;
    DB2UDB _db2;

    public boolean printData(final DB2UDB db2, final Vrw32alp svf, final Param param, final Student student) throws Exception {
        boolean hasData = false;
        _db2 = db2;
        _param = param;
        _svf = svf;

        final PreparedStatement psSelect = _db2.prepareStatement(_param.getStudyRecSql("SELECT", null));
        final PreparedStatement psTitle = _db2.prepareStatement(_param.getStudyRecSql("TITLE", null));
        try {
            _svf.VrSetForm("KNJWA230_2.frm", 4);

            printStudentInfo(student);
            printGakushuKiroku(student, psSelect);

            hasData = true;
        } finally {
            DbUtils.close(psSelect);
            DbUtils.close(psTitle);
            _db2.commit();
        }
        return hasData;
    }

    private void printStudentInfo(final Student student) {

        final String nameField = student._name.length() > 10 ? "2" : "1";
        _svf.VrsOut("NAME" + nameField, student._name);
    }

    private void printGakushuKiroku(final Student student, final PreparedStatement psSelect) throws SQLException {

        ResultSet rs = null;
        try {
            int pp = 1;
            psSelect.setString(pp++, student._schregno);
            psSelect.setString(pp++, student._schregno);
            rs = psSelect.executeQuery();

            final String sougouClassCd = "11";
            final List printList = new ArrayList();
            final PrintTotalData printTotalData = new PrintTotalData();
            boolean hasData = false;
            String befClassCd = "";
            String befSubclassCd = "";
            LineData lineData = null;
            while (rs.next()) {

                final String valution = getValuation(rs.getString("SCHOOLCD"), rs.getString("VALUATION"));
                int getCredit = null == rs.getString("GET_CREDIT") ? 0 : rs.getInt("GET_CREDIT");
                final String strMinMiri = String.valueOf(rs.getInt("MIN_MIRI"));
                final int strStudyCnt = rs.getInt("STUDY_CNT");
                if (null != valution && !valution.equals("大") && !valution.equals("高") && !valution.equals("技")) {
                    if (strMinMiri.equals("1") &&
                            strStudyCnt > 0
                    ) {
                        getCredit = 0;
                    }
                }
                printTotalData.setTotalCredit(rs.getString("CLASSCD"), getCredit);
                final String annual = rs.getString("ANNUAL");

                if (rs.getString("CLASSCD").equals(sougouClassCd)) {
                    continue;
                }

                if (!hasData || !befClassCd.equals(rs.getString("CLASSCD")) || !befSubclassCd.equals(rs.getString("SUBCLASSCD"))) {
                    if (hasData && (!befClassCd.equals(rs.getString("CLASSCD")) || !befSubclassCd.equals(rs.getString("SUBCLASSCD")))) {
                        printList.add(lineData);
                    }
                    lineData = new LineData(rs.getString("CLASSCD"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSNAME"), 0);
                }

                lineData.setPrintData(getCredit, rs.getString("YEAR"), annual, valution, rs.getString("FORMER_REG_SCHOOLCD"));
                befClassCd = rs.getString("CLASSCD");
                befSubclassCd = rs.getString("SUBCLASSCD");
                hasData = true;
            }

            _svf.VrsOut("TOTALSTUDY_CREDIT", String.valueOf(printTotalData._sougouCredit));
            if (hasData) {
                printList.add(lineData);
                //明細印字
                meisaiPrintOut(printList, printTotalData);
            } else {
                // 学習情報がない場合の処理
                _svf.VrsOut("CLASSCD", "A"); // 教科コード
                _svf.VrEndRecord();
            }
        } finally {
            DbUtils.closeQuietly(rs);
        }
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
        final String _subclassName;
        final List _printData;
        int _totalCredit = 0;
        String _remark;
        String _befRemark;

        public LineData(
                final String classCd,
                final String className,
                final String subclassName,
                final int totalCredit
        ) {
            _classCd = classCd;
            _className = className;
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
                final String remark
        ) {
            _totalCredit += credit;
            PrintData printData = new PrintData(year, annual, value, String.valueOf(credit));
            _printData.add(printData);
            addRemark(remark);
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
        String _credit;

        public PrintData(final String year, final String annual, final String value, final String credit) {
            _year = year;
            _annual = annual;
            _value = value;
            _credit = credit;
        }

        public void addCredit(final int credit) {
            final int cre = Integer.parseInt(_credit);
            _credit = String.valueOf(cre + credit);
        }
    }

    private void meisaiPrintOut(final List printList, final PrintTotalData printTotalData) {

        int fieldCnt = 1;
        String befClassCd = "";
        for (final Iterator itLine = printList.iterator(); itLine.hasNext();) {
            final LineData lineData = (LineData) itLine.next();
            _svf.VrsOut("CLASSCD", lineData._classCd); // 教科コード
            if (!befClassCd.equals(lineData._classCd)) {
                final String field = lineData._className.length() > 6 ? "2" : "1";
                _svf.VrsOut("CLASSNAME" + field, lineData._className);
            }
            if (null != lineData._subclassName) {
                final String field = lineData._subclassName.length() > 13 ? "2" : "1";
                _svf.VrsOut("SUBCLASSNAME" + field, lineData._subclassName);
            }

            _svf.VrsOut("CREDIT", String.valueOf(lineData._totalCredit));

            befClassCd = lineData._classCd;
            fieldCnt++;
            _svf.VrEndRecord();
        }

        for (int i = fieldCnt; i < 79; i++) {
            if (i == fieldCnt) {
                _svf.VrsOut("CLASSCD", ""); // 教科コード
            } else {
                _svf.VrsOut("CLASSCD", String.valueOf(i)); // 教科コード
            }
            _svf.VrEndRecord();
        }
    }

}
 // KNJWA230Tani

// eof
