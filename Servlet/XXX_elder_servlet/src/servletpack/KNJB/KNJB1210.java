/*
 * $Id: ce40992b2fc63c9c115ada849aa77b8d584dad8b $
 *
 * 作成日: 2012/10/26
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJB;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 履修登録 点票
 */
public class KNJB1210 {

    private static final Log log = LogFactory.getLog(KNJB1210.class);

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

            if("".equals(_param._schregno)) {
                for (final Iterator it = _param._schregList.iterator(); it.hasNext();) {
                    final String schregno = (String) it.next();
                    printMain(db2, svf, schregno);
                }
            } else {
                printMain(db2, svf, _param._schregno);
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }

    private static int getMS932ByteLength(final String s) {
        if (null != s) {
            try {
                return s.getBytes("MS932").length;
            } catch (final Exception e) {
                log.error("exception!", e);
            }
        }
        return 0;
    }

    private int intval(final String v) {
        return NumberUtils.isNumber(v) ? Integer.parseInt(v) : 0;
    }

    private void printNum(final Vrw32alp svf, final String field, final String v) {
        if (getMS932ByteLength(v) > 5) {
            svf.VrsOut(field + "2", v);
        } else {
            svf.VrsOut(field + "1", v);
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final String schregno) {

        final Student student = new Student(schregno);

        makeStudentInfo(db2, student);
        makeMeisai(db2, student);

        //svf.VrSetForm("KNJB1210.frm", 4);
        svf.VrSetForm("KNJB1210_2.frm", 4);

        svf.VrsOut("SCH_NO", student._schregno);
        svf.VrsOut("NAME", student._name);
        svf.VrsOut("FIELD1", _param._exeYear);

        svf.VrsOut("FREE_TIMES", String.valueOf(student._musyouKaisuu));
        if ("1".equals(student._jikougaiNyuuryoku)) {
            svf.VrsOut("ASSESS_CREDIT_NAME", "前籍校入力完了");
            svf.VrsOut("ASSESS_CREDIT", "");
        } else {
            svf.VrsOut("ASSESS_CREDIT_NAME", "査定単位数：");
            svf.VrsOut("ASSESS_CREDIT", student._sateiTannisuu);
        }

        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._loginDate));

        int A = 0; // 履修登録
        int B = 0; // 履修中
        int C = 0; // 修得単位数.修得単位
        int D = 0; // 修得単位数.自校外
        int E = 0; // 小計 (B + C + D)
        int F = 0; // 高認修得
        int G = 0; // 増単修得
        int H = 0; // 総計
        final int I = intval(student._sateiTannisuu);  // 査定単位数
        int line = 0;
        for (final Iterator itr = student._recordList.iterator(); itr.hasNext();) {
            boolean continueFlg = true;
            final Record record = (Record) itr.next();

            //履修登録、履修中、修得単位、自校外が、null/空白/'0'の場合、出力しない
            if ("1".equals(record._checked)) {
                if(record._credits    != null && !"".equals(record._credits)        && !"0".equals(record._credits))        continueFlg = false;
            }
            if(record._risyutyuCredit != null && !"".equals(record._risyutyuCredit) && !"0".equals(record._risyutyuCredit)) continueFlg = false;
            if(record._recordCredit   != null && !"".equals(record._recordCredit)   && !"0".equals(record._recordCredit))   continueFlg = false;
            if(record._anotherCredit  != null && !"".equals(record._anotherCredit)  && !"0".equals(record._anotherCredit))  continueFlg = false;
            if(continueFlg) continue;

            svf.VrsOut("CLASS" + (getMS932ByteLength(record._classname) > 20 ? "2" : "1"), record._classname);
            svf.VrsOut("SUBJECT" + (getMS932ByteLength(record._subclassname) > 20 ? "2" : "1"), record._subclassname);
            svf.VrsOut("CHAIR" + (getMS932ByteLength(record._chairName) > 20 ? "2" : "1"), record._chairName); //講座名

            // 履修登録
            // svf.VrsOut("REGIST_CREDIT", "1".equals(record._checked) ? "レ" : "");
            if ("1".equals(record._checked)) {
                printNum(svf, "REGIST_CREDIT", record._credits);
                A += intval(record._credits);
            }

            // 履修中
            printNum(svf, "REGD_CREDIT", record._risyutyuCredit);
            B += intval(record._risyutyuCredit);

            // 修得単位
            printNum(svf, "GET_CREDIT", record._recordCredit);
            C += intval(record._recordCredit);

            // 自校外
            printNum(svf, "ANOTHER_CREDIT", record._anotherCredit);
            D += intval(record._anotherCredit);

            // 小計
            printNum(svf, "SUBTOTAL", record._totalComp);
            E += intval(record._totalComp);

            // 認定単位数
            printNum(svf, "NINTEI", record._credits);

            // 必履修
            printNum(svf, "REQUIRE", record._hitsurisyu);

            // 高認
            if ("1".equals(record._kounin)) {
                printNum(svf, "HIGHAUTH_CREDIT", record._credits);
                F += intval(record._credits);
            }

            // 増加単位
            printNum(svf, "ADD_CREDIT", record._zoutan);
            G += intval(record._zoutan);

            svf.VrsOut("REMARK1", record._remark);

            svf.VrEndRecord();
            line += 1;
            _hasData = true;
        }

        if(line == 0) {
            svf.VrEndRecord();
            _hasData = true;
        }

        final int maxline = line / 60.0 <= 1.0 ? 60 : (60 + 62 * ((line - 60) / 60 + ((line - 60) % 60 == 0 ? 0 : 1)));
        for (int l = line; l < maxline; l++) {
            svf.VrsOut("SUBJECT1", "\n");
            svf.VrEndRecord();
        }

        // 合計単位数
        if (!_param._year.equals(_param._exeYear)) {
            printNum(svf, "SUB_REGIST_CREDIT", String.valueOf(A));
        }
        printNum(svf, "SUB_REGD_CREDIT", String.valueOf(B));
        printNum(svf, "SUB_GET_CREDIT", String.valueOf(C));
        printNum(svf, "SUB_ANOTHER_CREDIT", String.valueOf(D));
        printNum(svf, "SUB_SUBTOTAL", String.valueOf(E));
        printNum(svf, "SUB_HIGHAUTH_CREDIT", String.valueOf(F));
        printNum(svf, "SUB_ADD_CREDIT", String.valueOf(G));
        svf.VrEndRecord();
        // 総計
        if (!_param._year.equals(_param._exeYear)) {
            // 総計 H = A + E + F + G + I
            H = A + E + F + G + I;
            svf.VrsOut("ALLCREDIT", String.valueOf(H));
        } else {
            // 総計 H = E + F + G + I
            H = E + F + G + I;
            svf.VrsOut("ALLCREDIT", String.valueOf(H));
        }
        svf.VrEndRecord();
    }

    private List query(final DB2UDB db2, final String sql) {
        final List rowList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final ResultSetMetaData meta = rs.getMetaData();
            final int colc = meta.getColumnCount();
            while (rs.next()) {
                final Map row = new HashMap();
                for (int i = 1; i <= colc; i++) {
                    final String name = meta.getColumnName(i);
                    row.put(name, rs.getString(name));
                    row.put(String.valueOf(i), rs.getString(name));
                }
                rowList.add(row);
            }

        } catch (final SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rowList;
    }

    private Map getRow(final List list) {
        return (list.isEmpty() ? new HashMap() : (Map) list.get(0));
    }

    private String get(final Map m, final String key) {
        if (null == m || m.isEmpty()) {
            log.fatal(" empty row : key = " + key + ", " + m );
        } else if (!m.containsKey(key)) {
            log.fatal(" row not contain key : key = " + key + ", " + m);
        }
        return (String) m.get(key);
    }

    private String getOne(final Map m) {
        return get(m, "1");
    }

    private void makeStudentInfo(final DB2UDB db2, final Student student) {

        Map info = getRow(query(db2, getStudentInfoData("", student._schregno)));
        String risyuCount = getOne(getRow(query(db2, getRisyuuCnt(student._schregno))));
        student._musyouKaisuu = (intval(get(info, "MUSYOU_KAISU")) < intval(risyuCount)) ? 0 : intval(get(info, "MUSYOU_KAISU")) - intval(risyuCount);
        student._jikougaiNyuuryoku = get(info, "JIKOUGAI_NYUURYOKU");
        student._sateiTannisuu = "1".equals(student._jikougaiNyuuryoku) ? "0" : get(info, "SATEI_TANNI");
        student._name = get(info, "NAME");
    }

    private void makeMeisai(final DB2UDB db2, final Student student) {

        int totalGet = 0;
        int totalComp = 0;
        int totalJikougai = 0;

        final List result = query(db2, getSubclassName(student._schregno));
//        Set s = null;
        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map row = (Map) it.next();
//            if ("1".equals(_param._cmd)) {
//
//            } else {
                if (!"1".equals(get(row, "CHECKED"))) {
//                    foreach ($selectdata as $val) {
//                        if ($val == $chkval) {
//                            $row["CHECKED"] = "1";
//                            $zoutanCheck = "0";
//                            break;
//                        } else {
//                            $row["CHECKED"] = "";
//                        }
//                    }
                } else {
                    if ("1".equals(_param._exeNendoPatern)) {
                        row.put("RISYUTYU_CREDIT", get(row, "SET_CREDIT"));
                    }
                }
//                if ($row["KOUNIN"] != "1") {
//                    foreach ($selectKounin as $val) {
//                        if ($val == $chkval) {
//                            $row["KOUNIN"] = "1";
//                            $zoutanCheck = "0";
//                            break;
//                        } else {
//                            $row["KOUNIN"] = "";
//                        }
//                    }
//                }
//            }
        }

        for (final Iterator it = result.iterator(); it.hasNext();) {
            final Map row = (Map) it.next();

            final Record record = new Record();
            student._recordList.add(record);
            record._classcd = get(row, "CLASSCD");
            record._classcdfull = get(row, "CLASSCD") + "-" + get(row, "SCHOOL_KIND") + "-" + get(row, "CURRICULUM_CD");
            record._subclasscd = get(row, "SUBCLASSCD");
            record._subclasscdfull = get(row, "CLASSCD") + "-" + get(row, "SCHOOL_KIND") + "-" + get(row, "CURRICULUM_CD") + "-" + get(row, "SUBCLASSCD");
            record._classname = get(row, "CLASSNAME");
            record._subclassname = get(row, "SUBCLASSNAME");
            record._chairCd = get(row, "CHAIRCD");
            record._chairName = get(row, "CHAIRNAME");

            record._checked = get(row, "CHECKED");
            record._risyutyuCredit = get(row, "RISYUTYU_CREDIT");
            record._recordCredit = get(row, "RECORD_CREDITS");
            record._anotherCredit = get(row, "ANOTHER_CREDITS");
            record._credits = get(row, "CREDITS");
            record._hitsurisyu = get(row, "HITSURISYU");
            record._kounin = get(row, "KOUNIN");
            record._zoutan = get(row, "ZOUTAN");
            record._remark = get(row, "REMARK");
            String subRityu = "0";
            if ("1".equals(get(row, "CHECKED"))) {
                totalGet += intval(get(row, "SET_CREDIT"));
            } else if ("1".equals(get(row, "KOUNIN"))) {
                totalJikougai += intval(get(row, "SET_CREDIT"));
                subRityu = get(row, "SET_CREDIT");
            } else if (!StringUtils.isBlank(get(row, "ZOUTAN"))) {
                totalJikougai += intval(get(row, "ZOUTAN"));
                subRityu = get(row, "ZOUTAN");
            }
            record._totalComp = String.valueOf(intval(get(row, "RISYUTYU_CREDIT")) + intval(get(row, "ANOTHER_CREDITS")) + intval(get(row, "RECORD_CREDITS")));
            student._totalComp = String.valueOf(intval(student._totalComp) + intval(record._totalComp));
            // s = row.keySet();
        }
        student._totalComp = String.valueOf(totalComp);
        student._totalGet = String.valueOf(totalGet);
        student._totalJikougai = String.valueOf(totalJikougai);
        if ("1".equals(_param._exeNendoPatern)) {
            student._compGet = String.valueOf(intval(student._sateiTannisuu) + totalComp + totalJikougai);
        } else {
            student._compGet = String.valueOf(intval(student._sateiTannisuu) + totalGet + totalComp + totalJikougai);
        }
//        log.debug(" keys = " + new TreeSet(s));


    }

    private static class Student {
        final String _schregno;
        String _name;
        int _musyouKaisuu;
        String _sateiTannisuu;
        String _jikougaiNyuuryoku;

        String _totalGet;
        String _totalComp;
        String _totalJikougai;
        String _compGet;

        final List _recordList = new ArrayList();

        public Student(final String schregno) {
            _schregno = schregno;
        }
    }

    private static class Record {
        String _classcd;
        String _classcdfull;
        String _subclasscd;
        String _subclasscdfull;
        String _classname;
        String _subclassname;
        String _chairCd;
        String _chairName;

        String _checked;
        String _registCredit;
        String _risyutyuCredit;
        String _recordCredit;
        String _anotherCredit;
        String _totalComp;
        String _credits;
        String _hitsurisyu;
        String _kounin;
        String _zoutan;
        String _remark;
    }

    // ---

    //生徒情報取得
    private String getStudentInfoData(final String div, final String schregno)
    {
        String setYear;
        String seme;
        if ("meisai".equals(div)) {
            setYear = _param._exeYear;
            seme = _param._year.equals(_param._exeYear) ? _param._semester : "1";
        } else {
            setYear = _param._year;
            seme = _param._semester;
        }

        final StringBuffer tableName = new StringBuffer();
        final String tableBetsumei1;
        final String tableBetsumei2;
        if ("1".equals(_param._searchDiv)) {

            tableName.append( "(SELECT ");
            tableName.append( "     F1.*, ");
            tableName.append( "     V1.BASE_REMARK1 AS SATEI_TANNI, ");
            tableName.append( "     V1.BASE_REMARK5 AS JIKOUGAI_NYUURYOKU, ");
            tableName.append( "     V1.BASE_REMARK2 AS TOKKATU_JISU, ");
            tableName.append( "     V1.BASE_REMARK4 AS MUSYOU_KAISU ");
            tableName.append( " FROM ");
            tableName.append( "     FRESHMAN_DAT F1 ");
            tableName.append( "     LEFT JOIN SCHREG_BASE_DETAIL_MST V1 ON F1.SCHREGNO = V1.SCHREGNO ");
            tableName.append( "          AND V1.BASE_SEQ = '004' ");
            tableName.append( " ) ");
            tableBetsumei1 = "MAIN";
            tableBetsumei2 = "MAIN";
        } else {
            tableName.append("V_SCHREG_BASE_MST");
            tableBetsumei1 = "SC_R";
            tableBetsumei2 = "SC_CTRL";
        }

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     MAIN.SCHREGNO, ");
        if ("1".equals(_param._searchDiv)) {
            stb.append("     '01' AS GRADE, ");
            stb.append("     '01' AS CTRL_GRADE, ");
        } else {
            if (_param._year.equals(_param._exeYear)) {
                stb.append("     SC_R.GRADE, ");
            } else {
                stb.append("     '0' || CAST(CAST(SC_R.GRADE AS SMALLINT) + 1 AS CHAR(1)) AS GRADE, ");
            }
            stb.append("     SC_R.GRADE AS CTRL_GRADE, ");
        }
        stb.append("     C2.COURSENAME AS COURSE, ");
        stb.append("     C2.COURSENAME || M2.MAJORNAME AS MAJOR, ");
        stb.append("     CC2.COURSECODENAME, ");
        stb.append("     SM.STAFFNAME AS STAFFNAME, ");
        stb.append("     SC_R.ANNUAL AS ANNUAL, ");
        stb.append("     MAIN.NAME AS NAME, ");
        if ("1".equals(_param._searchDiv)) {
            stb.append("     MAIN.CURRICULUM_YEAR AS CURRICULUM_YEAR, ");
            stb.append("     MAIN.ENTERYEAR AS ENT_YEAR, ");
            stb.append("     MAIN.ADDR1, ");
            stb.append("     MAIN.ADDR2, ");
        } else {
            stb.append("     SC_H.CURRICULUM_YEAR AS CURRICULUM_YEAR, ");
            stb.append("     MAIN.ENT_DATE, ");
            stb.append("     CASE WHEN MONTH(MAIN.ENT_DATE) < 4 ");
            stb.append("          THEN YEAR(MAIN.ENT_DATE) - 1 ");
            stb.append("          ELSE YEAR(MAIN.ENT_DATE) ");
            stb.append("     END AS ENT_YEAR, ");
            stb.append("     MAIN.GRD_DATE, ");
            stb.append("     ADDR.ADDR1, ");
            stb.append("     ADDR.ADDR2, ");
        }
        stb.append("     SC_YD.BASE_REMARK1 AS GRD_YOTEI, ");
        stb.append("     MAIN.SATEI_TANNI, ");
        stb.append("     MAIN.JIKOUGAI_NYUURYOKU, ");
        stb.append("     TOKKATU.NAME1 AS TOKKATU_JISU, ");
        stb.append("     SCHOOLING.NAME1 AS SCHOOLING_DIV, ");
        stb.append("     MAIN.MUSYOU_KAISU, ");
        stb.append("     AREA.NAME1 AS AREA, ");
        stb.append("     JOB.NAME1 AS JOB, ");
        stb.append("     " + tableBetsumei1 + ".COURSECD, ");
        stb.append("     " + tableBetsumei1 + ".MAJORCD, ");
        stb.append("     " + tableBetsumei1 + ".COURSECODE, ");
        stb.append("     " + tableBetsumei2 + ".COURSECD AS CTRL_COURSECD, ");
        stb.append("     " + tableBetsumei2 + ".MAJORCD AS CTRL_MAJORCD, ");
        stb.append("     " + tableBetsumei2 + ".COURSECODE AS CTRL_COURSECODE ");
        stb.append(" FROM ");
        stb.append("     " + tableName + " MAIN ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT SC_R ON SC_R.SCHREGNO = MAIN.SCHREGNO ");
        stb.append("          AND SC_R.YEAR = '" + setYear + "' ");
        stb.append("          AND SC_R.SEMESTER = '" + seme + "' ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT SC_CTRL ON SC_CTRL.SCHREGNO = MAIN.SCHREGNO ");
        stb.append("          AND SC_CTRL.YEAR = '" + _param._year + "' ");
        stb.append("          AND SC_CTRL.SEMESTER = '" + _param._semester + "' ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT SC_G ON SC_R.YEAR = SC_G.YEAR ");
        stb.append("          AND SC_R.GRADE = SC_G.GRADE ");
        stb.append("     LEFT JOIN COURSE_MST C2 ON C2.COURSECD = " + tableBetsumei1 + ".COURSECD ");
        stb.append("     LEFT JOIN MAJOR_MST M2 ON M2.COURSECD = " + tableBetsumei1 + ".COURSECD ");
        stb.append("          AND M2.MAJORCD = " + tableBetsumei1 + ".MAJORCD ");
        stb.append("     LEFT JOIN COURSECODE_MST CC2 ON CC2.COURSECODE = " + tableBetsumei1 + ".COURSECODE ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT SH ON SH.YEAR = SC_R.YEAR ");
        stb.append("          AND SH.SEMESTER = SC_R.SEMESTER ");
        stb.append("          AND SH.GRADE = SC_R.GRADE ");
        stb.append("          AND SH.HR_CLASS = SC_R.HR_CLASS ");
        stb.append("     LEFT JOIN STAFF_MST SM ON SM.STAFFCD = SH.TR_CD1 ");
        stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT SC_H ON MAIN.SCHREGNO = SC_H.SCHREGNO ");
        stb.append("          AND SC_H.SCHOOL_KIND = SC_G.SCHOOL_KIND ");
        stb.append("     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST SC_YD ON SC_YD.SCHREGNO = MAIN.SCHREGNO ");
        stb.append("          AND SC_YD.YEAR = '" + _param._exeYear + "' ");
        stb.append("          AND SC_YD.BASE_SEQ = '001' ");
        stb.append("     LEFT JOIN SCHREG_SEND_ADDRESS_DAT SCH_SEND ON SCH_SEND.SCHREGNO = MAIN.SCHREGNO ");
        stb.append("          AND SCH_SEND.DIV = '1' ");
        stb.append("     LEFT JOIN NAME_MST AREA ON AREA.NAMECD1 = 'A020' ");
        stb.append("          AND SCH_SEND.SEND_AREACD = AREA.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST JOB ON JOB.NAMECD1 = 'H202' ");
        stb.append("          AND SCH_SEND.SEND_JOBCD = JOB.NAMECD2 ");
        stb.append("     LEFT JOIN ( ");
        stb.append("               SELECT ");
        stb.append("                   T1.* ");
        stb.append("               FROM ");
        stb.append("                   SCHREG_ADDRESS_DAT T1, ");
        stb.append("                   (SELECT ");
        stb.append("                        SCHREGNO, ");
        stb.append("                        MAX(ISSUEDATE) AS ISSUEDATE ");
        stb.append("                    FROM ");
        stb.append("                        SCHREG_ADDRESS_DAT ");
        stb.append("                    GROUP BY ");
        stb.append("                        SCHREGNO ");
        stb.append("                   ) T2 ");
        stb.append("               WHERE ");
        stb.append("                   T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("                   AND T1.ISSUEDATE = T2.ISSUEDATE) ADDR ");
        stb.append("          ON MAIN.SCHREGNO = ADDR.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST Y_DETAIL ON MAIN.SCHREGNO = Y_DETAIL.SCHREGNO ");
        stb.append("          AND Y_DETAIL.YEAR = '" + _param._exeYear + "' ");
        stb.append("          AND Y_DETAIL.BASE_SEQ = '002' ");
        stb.append("     LEFT JOIN NAME_MST TOKKATU ON TOKKATU.NAMECD1 = 'M013' ");
        stb.append("          AND MAIN.TOKKATU_JISU = TOKKATU.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST SCHOOLING ON SCHOOLING.NAMECD1 = 'M014' ");
        stb.append("          AND Y_DETAIL.BASE_REMARK1 = SCHOOLING.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     MAIN.SCHREGNO = '" + schregno + "' ");

        return stb.toString();
    }

    //履修パターン
    private String getRisyuuCnt(final String schregno)
    {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH STD_YEAR AS ( ");
        stb.append(" SELECT ");
        stb.append("     YEAR ");
        stb.append(" FROM ");
        stb.append("     SUBCLASS_STD_SELECT_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR < '" + _param._exeYear + "' ");
        stb.append("     AND SCHREGNO = '" + schregno + "' ");
        stb.append(" GROUP BY ");
        stb.append("     YEAR ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     STD_YEAR ");

        return stb.toString();
    }

    //一覧
    private String getSubclassName(final String schregno)
    {
        final String seme = "1".equals(_param._exeNendoPatern) ? _param._semester : "1";
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCH_MAIN AS ( ");
        stb.append(getStudentInfoData("", schregno));

        //教育課程
        stb.append(" ), CURRICULUM_T AS ( ");
        stb.append(" SELECT ");
        stb.append("    NAMECD2 ");
        stb.append(" FROM ");
        stb.append("    NAME_MST ");
        stb.append(" WHERE ");
        stb.append("    NAMECD1 = 'Z018' ");
        stb.append("    AND '" + _param._exeYear + "' BETWEEN NAMESPARE1 AND NAMESPARE2 ");
        stb.append(" ), ");

        //履修登録済み
        stb.append(" COMP_SUBCLASS_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     '1' AS COMP_EXE_FLG ");
        stb.append(" FROM ");
        stb.append("     SUBCLASS_MST T1 ");
        stb.append("     INNER JOIN SUBCLASS_STD_SELECT_DAT S_STD ON S_STD.YEAR = '" + _param._exeYear + "' ");
        stb.append("           AND S_STD.SEMESTER = '" + seme + "' ");
        stb.append("           AND S_STD.CLASSCD = T1.CLASSCD ");
        stb.append("           AND S_STD.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("           AND S_STD.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("           AND S_STD.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("           AND S_STD.SCHREGNO = '" + schregno + "' ");
        stb.append(" WHERE ");
        stb.append("    T1.CLASSCD < '91' ");
        stb.append(" ), ");

        //履修登録済み()
        stb.append(" COMP_SUBCLASS_CTL_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     '1' AS COMP_EXE_FLG ");
        stb.append(" FROM ");
        stb.append("     SUBCLASS_MST T1 ");
        stb.append("     INNER JOIN SUBCLASS_STD_SELECT_DAT S_STD ON S_STD.YEAR = '" + _param._year + "' ");
        stb.append("           AND S_STD.SEMESTER = '" + _param._semester + "' ");
        stb.append("           AND S_STD.CLASSCD = T1.CLASSCD ");
        stb.append("           AND S_STD.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("           AND S_STD.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("           AND S_STD.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("           AND S_STD.SCHREGNO = '" + schregno + "' ");
        stb.append(" WHERE ");
        stb.append("    T1.CLASSCD < '91' ");
        stb.append(" ), ");

        //自校外
        stb.append(" ANOTHER_SUBCLASS_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.SUBCLASSNAME, ");
        stb.append("     SUM(VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0)) AS GET_CREDIT ");
        stb.append(" FROM ");
        stb.append("    SCHREG_STUDYREC_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     SCHOOLCD = '1' ");
        stb.append("     AND T1.YEAR = '0000' ");
        stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
        stb.append("     AND T1.ANNUAL = '00' ");
        stb.append("     AND T1.CLASSCD < '91' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.SUBCLASSNAME ");
        stb.append(" ), ");

        //成績：今年度RECORD_DAT（STUDYREC除く）＋指定年度以下STUDYREC
        stb.append(" RECORD AS ( ");
        stb.append(" SELECT ");
        stb.append("     'REC' AS REC_DIV, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     '1' AS RISYUTYU_FLG, ");
        stb.append("     SUM(VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0)) AS GET_CREDIT ");
        stb.append(" FROM ");
        stb.append("     RECORD_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO = '" + schregno + "' ");
        stb.append("     AND T1.CLASSCD < '91' ");
        stb.append("     AND T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND NOT EXISTS(SELECT ");
        stb.append("                        'x' ");
        stb.append("                    FROM ");
        stb.append("                       SCHREG_STUDYREC_DAT E1 ");
        stb.append("                    WHERE ");
        stb.append("                        E1.SCHOOLCD = '0' ");
        stb.append("                        AND T1.YEAR = E1.YEAR ");
        stb.append("                        AND T1.SCHREGNO = E1.SCHREGNO ");
        stb.append("                        AND T1.CLASSCD = E1.CLASSCD ");
        stb.append("                        AND T1.SCHOOL_KIND = E1.SCHOOL_KIND ");
        stb.append("                        AND T1.CURRICULUM_CD = E1.CURRICULUM_CD ");
        stb.append("                        AND T1.SUBCLASSCD = E1.SUBCLASSCD ");
        stb.append("     ) ");
        stb.append(" GROUP BY ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     'REC2' AS REC_DIV, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     '0' AS RISYUTYU_FLG, ");
        stb.append("     SUM(VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0)) AS GET_CREDIT ");
        stb.append(" FROM ");
        stb.append("    SCHREG_STUDYREC_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     SCHOOLCD = '0' ");
        stb.append("     AND YEAR = '" + _param._year + "' ");
        stb.append("     AND SCHREGNO = '" + schregno + "' ");
        stb.append("     AND T1.CLASSCD < '91' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     'STUDY' AS REC_DIV, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     '0' AS RISYUTYU_FLG, ");
        stb.append("     SUM(VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0)) AS GET_CREDIT ");
        stb.append(" FROM ");
        stb.append("    SCHREG_STUDYREC_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     SCHOOLCD = '0' ");
        stb.append("     AND YEAR <= '" + _param._exeYear + "' ");
        stb.append("     AND YEAR <> '" + _param._year + "' ");
        stb.append("     AND T1.CLASSCD < '91' ");
        stb.append("     AND SCHREGNO = '" + schregno + "' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD ");
        stb.append(" ), ");

        //出力対象科目（成績）
        stb.append(" RECORD_SUB AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD ");
        stb.append(" FROM ");
        stb.append("     RECORD_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO = '" + schregno + "' ");
        stb.append("     AND T1.YEAR <= '" + _param._exeYear + "' ");
        stb.append("     AND T1.CLASSCD < '91' ");
        stb.append("     AND NOT EXISTS(SELECT ");
        stb.append("                        'x' ");
        stb.append("                    FROM ");
        stb.append("                       SCHREG_STUDYREC_DAT E1 ");
        stb.append("                    WHERE ");
        stb.append("                        E1.SCHOOLCD = '0' ");
        stb.append("                        AND T1.YEAR = E1.YEAR ");
        stb.append("                        AND T1.SCHREGNO = E1.SCHREGNO ");
        stb.append("                        AND T1.CLASSCD = E1.CLASSCD ");
        stb.append("                        AND T1.SCHOOL_KIND = E1.SCHOOL_KIND ");
        stb.append("                        AND T1.CURRICULUM_CD = E1.CURRICULUM_CD ");
        stb.append("                        AND T1.SUBCLASSCD = E1.SUBCLASSCD ");
        stb.append("     ) ");
        stb.append(" GROUP BY ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD ");
        stb.append(" FROM ");
        stb.append("    SCHREG_STUDYREC_DAT T1 ");
        stb.append("    LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
        stb.append("         AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
        stb.append("         AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
        stb.append("         AND T1.SUBCLASSCD = L1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     SCHOOLCD = '0' ");
        stb.append("     AND YEAR <= '" + _param._exeYear + "' ");
        stb.append("     AND T1.CLASSCD < '91' ");
        stb.append("     AND SCHREGNO = '" + schregno + "' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD ");
        stb.append(" ), ");

        //出力対象科目：開講科目＋成績（開講科目除く）＋自校外（開講科目と成績除く）
        stb.append(" SUBCLASS_T AS ( ");
        stb.append(" SELECT ");
        stb.append("    T1.CLASSCD, ");
        stb.append("    T1.SCHOOL_KIND, ");
        stb.append("    T1.CURRICULUM_CD, ");
        stb.append("    T1.SUBCLASSCD, ");
        stb.append("    T1.SUBCLASSNAME ");
        stb.append(" FROM ");
        stb.append("    V_SUBCLASS_MST T1 ");
        stb.append(" WHERE ");
        stb.append("    T1.YEAR = '" + _param._exeYear + "' ");
        stb.append("    AND T1.CLASSCD < '91' ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("    T1.CLASSCD, ");
        stb.append("    T1.SCHOOL_KIND, ");
        stb.append("    T1.CURRICULUM_CD, ");
        stb.append("    T1.SUBCLASSCD, ");
        stb.append("    L1.SUBCLASSNAME ");
        stb.append(" FROM ");
        stb.append("    RECORD_SUB T1 ");
        stb.append("    LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
        stb.append("         AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
        stb.append("         AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
        stb.append("         AND T1.SUBCLASSCD = L1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("    T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ");
        stb.append("         NOT IN (SELECT ");
        stb.append("                     I1.CLASSCD || I1.SCHOOL_KIND || I1.CURRICULUM_CD || I1.SUBCLASSCD ");
        stb.append("                 FROM ");
        stb.append("                     V_SUBCLASS_MST I1 ");
        stb.append("                 WHERE ");
        stb.append("                     I1.YEAR = '" + _param._exeYear + "' ");
        stb.append("                ) ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("    T1.CLASSCD, ");
        stb.append("    T1.SCHOOL_KIND, ");
        stb.append("    T1.CURRICULUM_CD, ");
        stb.append("    T1.SUBCLASSCD, ");
        stb.append("    L1.SUBCLASSNAME ");
        stb.append(" FROM ");
        stb.append("    ANOTHER_SUBCLASS_T T1 ");
        stb.append("    LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
        stb.append("         AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
        stb.append("         AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
        stb.append("         AND T1.SUBCLASSCD = L1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("    T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ");
        stb.append("         NOT IN (SELECT ");
        stb.append("                     I1.CLASSCD || I1.SCHOOL_KIND || I1.CURRICULUM_CD || I1.SUBCLASSCD ");
        stb.append("                 FROM ");
        stb.append("                     V_SUBCLASS_MST I1 ");
        stb.append("                 WHERE ");
        stb.append("                     I1.YEAR = '" + _param._exeYear + "' ");
        stb.append("                ) ");
        stb.append("    AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ");
        stb.append("         NOT IN (SELECT ");
        stb.append("                     I2.CLASSCD || I2.SCHOOL_KIND || I2.CURRICULUM_CD || I2.SUBCLASSCD ");
        stb.append("                 FROM ");
        stb.append("                     RECORD I2 ");
        stb.append("                ) ");
        //講座
        stb.append(" ) , CHAIR_A AS( ");
        stb.append(" SELECT ");
        stb.append("     S1.SCHREGNO, ");
        stb.append("     S2.CLASSCD, ");
        stb.append("     S2.SCHOOL_KIND, ");
        stb.append("     S2.CURRICULUM_CD, ");
        stb.append("     S2.SUBCLASSCD, ");
        stb.append("     S2.CHAIRCD, ");
        stb.append("     S2.CHAIRNAME ");
        stb.append(" FROM ");
        stb.append("     CHAIR_STD_DAT S1, ");
        stb.append("     CHAIR_DAT S2 ");
        stb.append(" WHERE ");
        stb.append("     S1.YEAR         = '" + _param._exeYear + "' ");
        if (!_param._year.equals(_param._exeYear)) {
        	stb.append("     AND S1.SEMESTER = '1' ");
        } else {
        	stb.append("     AND S1.SEMESTER = '" + _param._semester + "' ");
        }
        stb.append("     AND S2.YEAR     = S1.YEAR ");
        stb.append("     AND S2.SEMESTER = S1.SEMESTER ");
        stb.append("     AND S2.CHAIRCD  = S1.CHAIRCD ");
        stb.append("     AND S1.SCHREGNO = '" + schregno + "' ");
        stb.append(" ) ");
        //メイン
        stb.append(" SELECT DISTINCT ");
        if ("1".equals(_param._knjb1210SubclassOrder)) {
        	stb.append("     CASE WHEN VALUE(R2.GET_CREDIT, 0) + VALUE(R3.GET_CREDIT, 0) > 0 THEN 0 ELSE 1 END AS PRINT_ORDER, "); // dummy 
        }
        stb.append("    T1.CLASSCD, ");
        stb.append("    T1.SCHOOL_KIND, ");
        stb.append("    T1.CURRICULUM_CD, ");
        stb.append("    T1.SUBCLASSCD, ");
        stb.append("    T2.CLASSNAME, ");
        stb.append("    T1.SUBCLASSNAME, ");
        stb.append("    CHAIR.CHAIRCD, ");
        stb.append("    CHAIR.CHAIRNAME, ");
        stb.append("    CASE WHEN R3.SUBCLASSCD IS NOT NULL AND R3.GET_CREDIT > 0 ");
        stb.append("         THEN '1' ");
        stb.append("         ELSE '0' ");
        stb.append("    END AS NOT_CHANGE, ");
        if ("1".equals(_param._exeNendoPatern)) {
            stb.append("    CASE WHEN COMP.SUBCLASSCD IS NOT NULL AND R1.RISYUTYU_FLG = '1' ");
            stb.append("         THEN R1.GET_CREDIT ");
            stb.append("         ELSE ");
            stb.append("         CASE WHEN COMP.SUBCLASSCD IS NOT NULL AND R1.SUBCLASSCD IS NULL AND R3.SUBCLASSCD IS NULL ");
            stb.append("              THEN CRE.CREDITS ");
            stb.append("              ELSE NULL ");
            stb.append("         END ");
            stb.append("    END AS RISYUTYU_CREDIT, ");
        } else {
            stb.append("    '' AS RISYUTYU_CREDIT, ");
        }
        stb.append("    CASE WHEN COMP.SUBCLASSCD IS NOT NULL AND R3.SUBCLASSCD IS NOT NULL ");
        stb.append("         THEN R3.GET_CREDIT ");
        stb.append("         ELSE CRECTL.CREDITS ");
        stb.append("    END AS SET_CREDIT, ");
        stb.append("    CRE.CREDITS, ");
        stb.append("    VALUE(R2.GET_CREDIT, 0) + VALUE(R3.GET_CREDIT, 0) AS RECORD_CREDITS, ");
        stb.append("    ANOTHER.GET_CREDIT AS ANOTHER_CREDITS, ");
        stb.append("    CASE WHEN PATTERN.SUBCLASSCD IS NOT NULL ");
        stb.append("         THEN '1' ");
        stb.append("         ELSE ");
        stb.append("         CASE WHEN COMP.SUBCLASSCD IS NOT NULL ");
        stb.append("              THEN '1' ");
        stb.append("              ELSE '0' ");
        stb.append("         END ");
        stb.append("    END AS CHECKED, ");
        stb.append("    CASE WHEN PATTERN.SUBCLASSCD IS NOT NULL ");
        stb.append("         THEN '1' ");
        stb.append("         ELSE ");
        stb.append("         CASE WHEN COMPCTL.SUBCLASSCD IS NOT NULL ");
        stb.append("              THEN '1' ");
        stb.append("              ELSE '0' ");
        stb.append("         END ");
        stb.append("    END AS CTLCHECKED, ");
        stb.append("    CASE WHEN V_SUB.SUBCLASSCD IS NOT NULL ");
        stb.append("         THEN '1' ");
        stb.append("         ELSE '0' ");
        stb.append("    END AS CHECKBOX, ");
        stb.append("    CASE WHEN VALUE(ANOTHER.GET_CREDIT, 0) = 0  ");
        stb.append("         THEN '1' ");
        stb.append("         ELSE '2' ");
        stb.append("    END AS SORT, ");
        stb.append("    COMP.COMP_EXE_FLG, ");
        stb.append("    SCH_COMP.KOUNIN, ");
        stb.append("    SCH_COMP.ADD_CREDIT AS ZOUTAN, ");
        stb.append("    N1.NAME1 AS REMARK, ");
        stb.append("    N2.NAMESPARE1 AS HITSURISYU ");
        stb.append(" FROM ");
        stb.append("    SUBCLASS_T T1 ");
        stb.append("    LEFT JOIN CHAIR_A CHAIR ");
        stb.append("          ON T1.CLASSCD       = CHAIR.CLASSCD ");
        stb.append("         AND T1.SCHOOL_KIND   = CHAIR.SCHOOL_KIND ");
        stb.append("         AND T1.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
        stb.append("         AND T1.SUBCLASSCD    = CHAIR.SUBCLASSCD ");
        stb.append("    LEFT JOIN CLASS_MST T2 ON T1.CLASSCD = T2.CLASSCD ");
        stb.append("         AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append("    LEFT JOIN RECORD R1 ON T1.CLASSCD = R1.CLASSCD ");
        stb.append("         AND T1.SCHOOL_KIND   = R1.SCHOOL_KIND ");
        stb.append("         AND T1.CURRICULUM_CD = R1.CURRICULUM_CD ");
        stb.append("         AND T1.SUBCLASSCD    = R1.SUBCLASSCD ");
        stb.append("         AND R1.REC_DIV       = 'REC' ");
        stb.append("    LEFT JOIN RECORD R2 ON T1.CLASSCD = R2.CLASSCD ");
        stb.append("         AND T1.SCHOOL_KIND   = R2.SCHOOL_KIND ");
        stb.append("         AND T1.CURRICULUM_CD = R2.CURRICULUM_CD ");
        stb.append("         AND T1.SUBCLASSCD    = R2.SUBCLASSCD ");
        stb.append("         AND R2.REC_DIV       = 'STUDY' ");
        stb.append("    LEFT JOIN RECORD R3 ON T1.CLASSCD = R3.CLASSCD ");
        stb.append("         AND T1.SCHOOL_KIND   = R3.SCHOOL_KIND ");
        stb.append("         AND T1.CURRICULUM_CD = R3.CURRICULUM_CD ");
        stb.append("         AND T1.SUBCLASSCD    = R3.SUBCLASSCD ");
        stb.append("         AND R3.REC_DIV       = 'REC2' ");
        stb.append("    LEFT JOIN ANOTHER_SUBCLASS_T ANOTHER ON T1.CLASSCD = ANOTHER.CLASSCD ");
        stb.append("         AND T1.SCHOOL_KIND = ANOTHER.SCHOOL_KIND ");
        stb.append("         AND T1.CURRICULUM_CD = ANOTHER.CURRICULUM_CD ");
        stb.append("         AND T1.SUBCLASSCD    = ANOTHER.SUBCLASSCD ");
        stb.append("    LEFT JOIN COMP_SUBCLASS_T COMP ON T1.CLASSCD       = COMP.CLASSCD ");
        stb.append("         AND T1.SCHOOL_KIND   = COMP.SCHOOL_KIND ");
        stb.append("         AND T1.CURRICULUM_CD = COMP.CURRICULUM_CD ");
        stb.append("         AND T1.SUBCLASSCD    = COMP.SUBCLASSCD ");
        stb.append("    LEFT JOIN COMP_SUBCLASS_CTL_T COMPCTL ON T1.CLASSCD = COMPCTL.CLASSCD ");
        stb.append("         AND T1.SCHOOL_KIND   = COMPCTL.SCHOOL_KIND ");
        stb.append("         AND T1.CURRICULUM_CD = COMPCTL.CURRICULUM_CD ");
        stb.append("         AND T1.SUBCLASSCD    = COMPCTL.SUBCLASSCD ");
        stb.append("    LEFT JOIN COMP_CREDITS_PATTERN_DAT PATTERN ON T1.CLASSCD = PATTERN.CLASSCD ");
        stb.append("         AND T1.SCHOOL_KIND   = PATTERN.SCHOOL_KIND ");
        stb.append("         AND T1.CURRICULUM_CD = PATTERN.CURRICULUM_CD ");
        stb.append("         AND T1.SUBCLASSCD    = PATTERN.SUBCLASSCD ");
//        if ("pattern".equals(_param._cmd)) {
//            stb.append("         AND PATTERN.PATTERN_CD    = '" + _param._pattern_cd + "' ");
//        } else {
            stb.append("         AND PATTERN.PATTERN_CD    = '' ");
//        }
        stb.append("    LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z018' ");
        stb.append("         AND T1.CURRICULUM_CD = N1.NAMECD2 ");
        stb.append("    LEFT JOIN V_SUBCLASS_MST V_SUB ON T1.CLASSCD = V_SUB.CLASSCD ");
        stb.append("         AND T1.SCHOOL_KIND   = V_SUB.SCHOOL_KIND ");
        stb.append("         AND T1.CURRICULUM_CD = V_SUB.CURRICULUM_CD ");
        stb.append("         AND T1.SUBCLASSCD    = V_SUB.SUBCLASSCD ");
        stb.append("         AND V_SUB.YEAR          = '" + _param._exeYear + "' ");
        stb.append("    LEFT JOIN SCH_MAIN I1 ON I1.SCHREGNO = '" + schregno + "' ");
        stb.append("    LEFT JOIN CREDIT_MST CRE ON CRE.YEAR = '" + _param._exeYear + "' ");
        stb.append("         AND CRE.COURSECD || CRE.MAJORCD || CRE.GRADE || CRE.COURSECODE = I1.COURSECD || I1.MAJORCD || I1.GRADE || I1.COURSECODE ");
        stb.append("         AND T1.CLASSCD       = CRE.CLASSCD ");
        stb.append("         AND T1.SCHOOL_KIND   = CRE.SCHOOL_KIND ");
        stb.append("         AND T1.CURRICULUM_CD = CRE.CURRICULUM_CD ");
        stb.append("         AND T1.SUBCLASSCD    = CRE.SUBCLASSCD ");
        stb.append("    LEFT JOIN CREDIT_MST CRECTL ON CRECTL.YEAR = '" + _param._year + "' ");
        stb.append("         AND CRECTL.COURSECD || CRECTL.MAJORCD || CRECTL.GRADE || CRECTL.COURSECODE = I1.CTRL_COURSECD || I1.CTRL_MAJORCD || I1.CTRL_GRADE || I1.CTRL_COURSECODE ");
        stb.append("         AND T1.CLASSCD       = CRECTL.CLASSCD ");
        stb.append("         AND T1.SCHOOL_KIND   = CRECTL.SCHOOL_KIND ");
        stb.append("         AND T1.CURRICULUM_CD = CRECTL.CURRICULUM_CD ");
        stb.append("         AND T1.SUBCLASSCD    = CRECTL.SUBCLASSCD ");
        stb.append("    LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'Z011' ");
        stb.append("         AND CRE.REQUIRE_FLG = N2.NAMECD2 ");
        stb.append("    LEFT JOIN SCH_COMP_DETAIL_DAT SCH_COMP ON SCH_COMP.YEAR = '" + _param._exeYear + "' ");
        stb.append("         AND SCH_COMP.SCHREGNO = '" + schregno + "' ");
        stb.append("         AND T1.CLASSCD       = SCH_COMP.CLASSCD ");
        stb.append("         AND T1.SCHOOL_KIND   = SCH_COMP.SCHOOL_KIND ");
        stb.append("         AND T1.CURRICULUM_CD = SCH_COMP.CURRICULUM_CD ");
        stb.append("         AND T1.SUBCLASSCD    = SCH_COMP.SUBCLASSCD ");

        stb.append(" ORDER BY ");
        if ("1".equals(_param._knjb1210SubclassOrder)) {
        	stb.append("     CASE WHEN VALUE(R2.GET_CREDIT, 0) + VALUE(R3.GET_CREDIT, 0) > 0 THEN 0 ELSE 1 END, ");
        }
        stb.append("     T1.CLASSCD, ");
        stb.append("     T2.CLASSNAME, ");
        stb.append("     CHECKBOX DESC, ");
        stb.append("     SORT, ");
        stb.append("     T1.SUBCLASSCD ");

        return stb.toString();
    }


    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72195 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _exeYear;
        final String _exeNendoPatern;
        final String _loginDate;
        final String _schregno;
        final String _searchDiv;
        final String _gradeHrClass;
        final String _choice; // 1:個人指定 2:クラス指定
        final String _knjb1210SubclassOrder; //
        final String[] _categorySelected;
        final List _schregList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _exeYear = request.getParameter("EXE_YEAR");
            _exeNendoPatern = request.getParameter("EXE_NENDO_PATERN");
            _loginDate = request.getParameter("LOGIN_DATE");
            _schregno = StringUtils.defaultString(request.getParameter("SCHREGNO"));
            _searchDiv = request.getParameter("SEARCH_DIV");
            _knjb1210SubclassOrder = request.getParameter("knjb1210SubclassOrder");

            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _choice = request.getParameter("CHOICE");
            _categorySelected = request.getParameterValues("category_name");
            if(!"".equals(_schregno)) {
                _schregList = null;
            } else {
                _schregList = getSchregList(db2);
            }
            log.info(_schregList);
        }

        private List getSchregList(final DB2UDB db2) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getSchregnoSql();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = StringUtils.defaultString(rs.getString("SCHREGNO"));
                    list.add(schregno);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private String getSchregnoSql() {

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     INNER JOIN SCHREG_BASE_MST T2 ");
            stb.append("            ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT T3 ");
            stb.append("            ON T3.YEAR     = T1.YEAR ");
            stb.append("           AND T3.SEMESTER = T1.SEMESTER ");
            stb.append("           AND T3.GRADE    = T1.GRADE ");
            stb.append("           AND T3.HR_CLASS = T1.HR_CLASS ");
            stb.append(" WHERE ");
            stb.append("      T1.YEAR         = '" + _year + "' ");
            stb.append("      AND T1.SEMESTER = '" + _semester + "' ");
            if("1".equals(_choice)) {
                stb.append("      AND T1.GRADE || T1.HR_CLASS = '" + _gradeHrClass + "' ");
                stb.append("      AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _categorySelected));
            } else {
                stb.append("      AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _categorySelected));
            }
            stb.append(" ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
            return stb.toString();
        }

    }
}

// eof

