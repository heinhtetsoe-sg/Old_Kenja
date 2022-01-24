// kanji=漢字
/*
 * $Id: 01bcdcee5667d48eed418cea3642e4a82270e2cf $
 *
 * 作成日: 2008/2/26 2:7:28 - JST
 * 作成者: nakasone
 *
 * Copyright(C) 2008-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWD;
import java.util.ArrayList;
import java.util.Enumeration;
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

import servletpack.KNJWE.detail.KNJ_StudyrecSql;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_PersonalinfoSql;


/**
 * 生徒個別単位修得・履修状況
 * 
 * @author nakasone
 *
 */
public class KNJWD720 {

    private static final String CERTIFKIND = "018";

    private static final Log log = LogFactory.getLog(KNJWD720.class);

    Param _param;
    
    /**
     * KNJW.classから呼ばれる処理
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
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
    }

    /**
     * @param response
     * @param db2
     */
    private boolean printMain(final HttpServletResponse response, final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        boolean hasData = false;
        final List studentList = getStudentList(db2);
        final Map noDispSubclass = getNoDispSubclass(db2);

        PreparedStatement psPrint = null;
        // 学校マスタの卒業単位
        final int gradCredit = getGradCredit(db2);
        //  学習記録データ(見込)
        KNJ_StudyrecSql obj_StudyrecSql = new KNJ_StudyrecSql("hyde", "hyde", 1, false, false);
        psPrint = db2.prepareStatement(obj_StudyrecSql.getSchregStudyRec(CERTIFKIND));
        try {
            for (final Iterator iter = studentList.iterator(); iter.hasNext();) {
                
                setHead(svf);

                final Student student = (Student) iter.next();
                log.debug(student);
                final PrintData printData = getDataPrint(db2, student, psPrint, gradCredit, noDispSubclass);
                studentPrint(svf, student);
                dataPrint(db2, svf, printData);
                hasData = true;
            }
        } finally {
            DbUtils.closeQuietly(psPrint);
            db2.commit();
        }

        return hasData;
    }

    private void setHead(final Vrw32alp svf) {
        svf.VrSetForm("KNJWD720.frm", 4);
    }

    private Map getNoDispSubclass(final DB2UDB db2) throws SQLException {
        final Map rtnMap = new HashMap();
        final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'W027' ";

        db2.query(sql);
        ResultSet rs = null;
        try {
            rs = db2.getResultSet();
            while (rs.next()) {
                rtnMap.put(rs.getString("NAME1"), rs.getString("NAME1"));
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnMap;
    }

    private int getGradCredit(final DB2UDB db2) throws SQLException {
        int rtnCredit = 0;
        final String sql = "SELECT GRAD_CREDITS FROM SCHOOL_MST WHERE YEAR = '" + _param._year + "' ";

        db2.query(sql);
        ResultSet rs = null;
        try {
            rs = db2.getResultSet();
            while (rs.next()) {
                rtnCredit = rs.getInt("GRAD_CREDITS");
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnCredit;
    }

    private PrintData getDataPrint(
            final DB2UDB db2,
            final Student student,
            final PreparedStatement psPrint,
            final int gradCredit,
            final Map noDispSubclass
    ) throws SQLException {
        final List lineList = new ArrayList();
        ResultSet rs = null;
        int pp = 1;
        psPrint.setString(pp++, student._schregno);
        psPrint.setString(pp++, student._schregno);
        psPrint.setString(pp++, _param._year);
        psPrint.setString(pp++, student._schregno);
        PrintData printData = null;
        try {
            int cnt = 0;

            rs = psPrint.executeQuery();

            int ad_credit = 0;
            int jikougaiCredit = 0;
            int honkouCredit = 0;
            int risyuTyuCredit = 0;
            int yoteiCredit = getYoteiCredit(db2, student);
            int miteiCredit = 0;
            int goukeiCredit = 0;

            while (rs.next()) {
                String subclassName = rs.getString("SUBCLASSNAME");
                String valuation = rs.getString("VALUATION");
                if (null != noDispSubclass && noDispSubclass.containsKey(rs.getString("CLASSCD"))) {
                    valuation = "";
                }
                String credit = rs.getString("GET_CREDIT");
                if (rs.getString("ORDERCD").equals("2")) {
                    credit = "(" + rs.getString("GET_CREDIT") + ")";
                    risyuTyuCredit += rs.getInt("GET_CREDIT");
                } else {
                    if (rs.getString("SCHOOLCD").equals("0")) {
                        honkouCredit += rs.getInt("GET_CREDIT");
                    } else {
                        jikougaiCredit += rs.getInt("GET_CREDIT");
                    }
                }
                final LineData lineData = new LineData(rs.getString("CLASSNAME"),
                                                       getSchoolCd(rs.getString("SCHOOLCD")),
                                                       valuation,
                                                       rs.getString("YEAR"),
                                                       rs.getString("SUBCLASSCD"),
                                                       subclassName,
                                                       credit);
                lineList.add(lineData);
                ad_credit += rs.getInt("GET_CREDIT");

                final int totalCredit = jikougaiCredit + honkouCredit + risyuTyuCredit + yoteiCredit;
                miteiCredit = gradCredit - totalCredit;
                final int miteiShow = miteiCredit <= 0 ? 0 : miteiCredit;
                goukeiCredit = totalCredit + miteiShow;

                cnt++;
            }
            final String miteiShow = miteiCredit <= 0 ? "0" : String.valueOf(miteiCredit);
            PrintTotalData printTotalData = new PrintTotalData(String.valueOf(jikougaiCredit),
                                                               String.valueOf(honkouCredit),
                                                               String.valueOf(risyuTyuCredit),
                                                               String.valueOf(yoteiCredit),
                                                               cnt == 0 ? String.valueOf(gradCredit) : miteiShow,
                                                               String.valueOf(goukeiCredit),
                                                               String.valueOf(ad_credit));
            printData = new PrintData(lineList, printTotalData);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rs);
        }
        return printData;
    }

    private class PrintData {
        final List _lineList;
        final PrintTotalData _printTotalData;

        public PrintData(final List lineList, final PrintTotalData printTotalData) {
            _lineList = lineList;
            _printTotalData = printTotalData;
        }
    }

    private class LineData {
        final String _className;
        final String _getDiv;
        final String _value;
        final String _year;
        final String _subclassCd;
        final String _subclassName;
        final String _credit;
        public LineData(
                final String className,
                final String getDiv,
                final String value,
                final String year,
                final String subclassCd,
                final String subclassName,
                final String credit
        ) {
            _className = className;
            _getDiv = getDiv;
            _value = value;
            _year = year;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _credit = credit;
        }
    }

    private class PrintTotalData {
        final String _jikougaiCredit;
        final String _honkouCredit;
        final String _risyuTyuCredit;
        final String _yoteiCredit;
        final String _miteiCredit;
        final String _goukeiCredit;
        final String _totalCredit;

        public PrintTotalData(
                final String jikougaiCredit,
                final String honkouCredit,
                final String risyuTyuCredit,
                final String yoteiCredit,
                final String miteiCredit,
                final String goukeiCredit,
                final String totalCredit
        ) {
            _jikougaiCredit = jikougaiCredit;
            _honkouCredit = honkouCredit;
            _risyuTyuCredit = risyuTyuCredit;
            _yoteiCredit = yoteiCredit;
            _miteiCredit = miteiCredit;
            _goukeiCredit = goukeiCredit;
            _totalCredit = totalCredit;
        }
    }

    private void studentPrint(final Vrw32alp svf, final Student student) {
        svf.VrsOut("DATE", _param.changePrintDate(_param._date));
        svf.VrsOut("BELONGING_NAME", student._schoolName1);
        svf.VrsOut("HR_NAME", student._hrName);
        svf.VrsOut("CURRICULUM_CD", student._curriculumName);
        svf.VrsOut("SCHREGNO", student._schregno);
        svf.VrsOut("NAME1_1", student._name);
        svf.VrsOut("ENT_DATE", _param.changePrintDate(student._entDate));
        svf.VrsOut("ENT_DIV", student._entName);
        svf.VrsOut("ANNUAL", String.valueOf(student._annual));
    }

    private void setScoolInfo(final Vrw32alp svf, final String fieldName, final String value) {
        if (value != null) {
            svf.VrsOut(fieldName,  value);
        }
    }

    private void dataPrint(
            final DB2UDB db2,
            final Vrw32alp svf,
            final PrintData printData
    ) {
        final PrintTotalData printTotalData = printData._printTotalData;
        int cnt = 1;
        final int lineCnt = printData._lineList.size();
        for (final Iterator iter = printData._lineList.iterator(); iter.hasNext();) {
            final LineData lineData = (LineData) iter.next();
            svf.VrsOut("CLASSNAME", lineData._className);
            svf.VrsOut("GET_DIV", lineData._getDiv);
            if (!_param.isW029(_param._year, lineData._subclassCd)) {
                svf.VrsOut("VALUE", lineData._value);
            }
            svf.VrsOut("NENDO", _param.changePrintYear(lineData._year));
            svf.VrsOut("SUBCLASS", lineData._subclassName);
            svf.VrsOut("CREDIT", lineData._credit);

            if (lineCnt > cnt) {
                cnt++;
                svf.VrEndRecord();
            }
        }
        svf.VrsOut("TOTAL_CREDIT", printTotalData._totalCredit);
        svf.VrsOutn("CREDIT2", 1, printTotalData._jikougaiCredit);
        svf.VrsOutn("CREDIT2", 2, printTotalData._honkouCredit);
        svf.VrsOutn("CREDIT2", 3, printTotalData._risyuTyuCredit);
        svf.VrsOutn("CREDIT2", 4, printTotalData._yoteiCredit);
        svf.VrsOutn("CREDIT2", 5, printTotalData._miteiCredit);
        svf.VrsOutn("CREDIT2", 6, printTotalData._goukeiCredit);
        svf.VrEndRecord();
    }

    private int getYoteiCredit(final DB2UDB db2, final Student student) throws SQLException {
        int rtnCredit = 0;
        final String sql = "SELECT SUM(COMP_CREDIT) AS COMP_CREDIT "
                         + "FROM COMP_CREDIT_DAT "
                         + "WHERE YEAR > '" + _param._year + "' "
                         + "      AND APPLICANTNO = '" + student._applicantNo + "'";
        db2.query(sql);
        ResultSet rs = null;
        try {
            rs = db2.getResultSet();
            while (rs.next()) {
                rtnCredit = rs.getInt("COMP_CREDIT");
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnCredit;
    }

    private String getSchoolCd(final String schoolCd) {
        final Map schoolDisp = new HashMap();
        schoolDisp.put("0", "");
        schoolDisp.put("1", "前");
        schoolDisp.put("2", "大");
        schoolDisp.put("3", "高");
        return (String) schoolDisp.get(schoolCd);
    }

    private List getStudentList(final DB2UDB db2) throws SQLException  {
        final List rtnStudent = new ArrayList();
        PreparedStatement psStudent = null;
        ResultSet rs = null;
        try {
            //  個人データ
            KNJ_PersonalinfoSql obj_Personalinfo = new KNJ_PersonalinfoSql();
            psStudent = db2.prepareStatement(obj_Personalinfo.studentInfoSql(false));

            for (int i = 0; i < _param._schregNoArray.length; i++) {
                int pp = 1;
                psStudent.setString(pp++, _param._schregNoArray[i]);
                psStudent.setString(pp++, _param._year);
                psStudent.setString(pp++, _param._semester);
                rs = psStudent.executeQuery();
                while (rs.next()) {
                    Student student = new Student(rs.getString("SCHREGNO"),
                                                  rs.getString("APPLICANTNO"),
                                                  rs.getString("NAME"),
                                                  rs.getString("ANNUAL"),
                                                  rs.getString("BIRTHDAY"),
                                                  rs.getString("ENT_DATE"),
                                                  rs.getString("ENT_NAME"),
                                                  rs.getString("GRD_DATE"),
                                                  rs.getString("GRD_NAME"),
                                                  rs.getString("CURRICULUM_NAME"),
                                                  rs.getString("SCHOOLNAME1"),
                                                  rs.getString("HR_NAME"),
                                                  rs.getString("HR_NAMEABBV"),
                                                  rs.getString("COURSENAME"),
                                                  rs.getString("MAJORNAME"),
                                                  rs.getString("COURSECODENAME"));
                    rtnStudent.add(student);
                }
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rs);
        }
        return rtnStudent;
    }

    class Student {
        final String _schregno;
        final String _applicantNo;
        final String _name;
        final String _annual;
        final String _birthday;
        final String _entDate;
        final String _entName;
        final String _grdDate;
        final String _grdName;
        final String _curriculumName;
        final String _schoolName1;
        final String _hrName;
        final String _hrNameAbbv;
        final String _courseName;
        final String _majorName;
        final String _courseCodeName;

        /**
         * コンストラクタ。
         */
        public Student(
                final String schregno,
                final String applicantNo,
                final String name,
                final String annual,
                final String birthday,
                final String entDate,
                final String entName,
                final String grdDate,
                final String grdName,
                final String curriculumName,
                final String schoolName1,
                final String hrName,
                final String hrNameAbbv,
                final String courseName,
                final String majorName,
                final String courseCodeName
                ) {
            _schregno = schregno;
            _applicantNo = applicantNo;
            _name = name;
            _annual = annual;
            _birthday = birthday;
            _entDate = entDate;
            _entName = entName;
            _grdDate = grdDate;
            _grdName = grdName;
            _curriculumName = curriculumName;
            _schoolName1 = schoolName1;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
            _courseName = courseName;
            _majorName = majorName;
            _courseCodeName = courseCodeName;
        }

        public String toString() {
            return "学籍：" + _schregno + " 名前：" + _name;
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

    /** パラメータクラス */
    class Param {
        final String _year;
        final String _semester;
        final String _date;
        final String _loginDate;
        final String[] _schregNoArray;
        String _z010 = "";
        String _z012 = "";
        final boolean _isSeireki;
        final Map _w029Map;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            ResultSet rsZ010 = null;
            ResultSet rsZ012 = null;

            try {
                _year = request.getParameter("YEAR");
                _semester = request.getParameter("SEMESTER");
                _date = request.getParameter("DATE");
                _loginDate= request.getParameter("LOGIN_DATE");

                _schregNoArray = request.getParameterValues("CATEGORY_SELECTED");

                _z010 = setNameMst(db2, "Z010", "00");
                _z012 = setNameMst(db2, "Z012", "01");
                _isSeireki = _z012.equals("2") ? true : false;
                _w029Map = getW029Map(db2);

            } finally {
                DbUtils.closeQuietly(rsZ010);
                DbUtils.closeQuietly(rsZ012);
                db2.commit();
            }
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

        public String changePrintDate(final String date) {
            if (null != date) {
                if (_isSeireki) {
                    return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
                } else {
                    return KNJ_EditDate.h_format_JP(date);
                }
            } else {
                return "";
            }
        }

        private Map getW029Map(final DB2UDB db2) throws SQLException {
            final Map retMap = new TreeMap();
            ResultSet rs = null;
            String sql = "SELECT * FROM V_NAME_MST WHERE NAMECD1 = 'W029' ORDER BY YEAR, NAMECD1 ";
            try {
                db2.query(sql);
                rs = db2.getResultSet();
                Map setNamecd2Map = new HashMap();
                String befYear = "";
                while (rs.next()) {
                    if (!befYear.equals(rs.getString("YEAR"))) {
                        setNamecd2Map = new HashMap();
                    }
                    setNamecd2Map.put(rs.getString("NAME1"), rs.getString("NAMECD2"));
                    retMap.put(rs.getString("YEAR"), setNamecd2Map);
                    befYear = rs.getString("YEAR");
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return retMap;
        }

        public String changePrintYear(final String year) {
            if (_isSeireki) {
                return year + "年度";
            } else {
                return nao_package.KenjaProperties.gengou(Integer.parseInt(year)) + "年度";
            }
        }

        private boolean isW029(final String year, final String subclassCd) {
            if (!_w029Map.containsKey(year)) {
                return false;
            }
            final Map setNamecd2Map = (Map) _w029Map.get(year);
            return setNamecd2Map.containsKey(subclassCd);
        }
    }

    class CertifSchool {
        final String _syosyoName;
        final String _syosyoName2;
        final String _schoolName;
        final String _jobName;
        final String _principalName;
        final String _remark1;
        final String _remark2;
        final String _remark3;
        final String _remark4;
        final String _remark5;
        final String _remark6;
        final String _remark7;
        final String _remark8;
        final String _remark9;
        final String _remark10;

        CertifSchool(
                final String syosyoName,
                final String syosyoName2,
                final String schoolName,
                final String jobName,
                final String principalName,
                final String remark1,
                final String remark2,
                final String remark3,
                final String remark4,
                final String remark5,
                final String remark6,
                final String remark7,
                final String remark8,
                final String remark9,
                final String remark10
        ) {
            _syosyoName = syosyoName;
            _syosyoName2 = syosyoName2;
            _schoolName = schoolName;
            _jobName = jobName;
            _principalName = principalName;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
            _remark4 = remark4;
            _remark5 = remark5;
            _remark6 = remark6;
            _remark7 = remark7;
            _remark8 = remark8;
            _remark9 = remark9;
            _remark10 = remark10;
        }
    }
}
