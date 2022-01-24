// kanji=漢字
/*
 * $Id: 40a2a3c5274a7b86f06d91d83da9a37390fafcad $
 *
 * 作成日: 2008/04/17 17:25:10 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2008-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJC;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.dao.AttendAccumulate;
;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 40a2a3c5274a7b86f06d91d83da9a37390fafcad $
 */
public class KNJC181K {
    private static final Log log = LogFactory.getLog(KNJC181K.class);

    Param _param;
    KNJDefineCode definecode;       //各学校における定数等設定
    KNJSchoolMst _knjSchoolMst;

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private static final String KINJ = "KINJUNIOR";
    private static final String KINH = "KINDAI";

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws Exception
    {
        Vrw32alp svf = new Vrw32alp(); //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null; //Databaseクラスを継承したクラス
        try {
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            //  ＳＶＦ作成処理
            boolean hasData = false; //該当データなしフラグ

            //SVF出力
            hasData = printMain(db2, svf);

            log.debug("hasData=" + hasData);

            //  該当データ無し
            if (!hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } finally {
            svf.VrQuit();
            db2.commit();
            db2.close();
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
        for (final Iterator iter = param._attendSemAllMap.keySet().iterator(); iter.hasNext();) {
            final String key = (String) iter.next();
            final Map hogeMap = (Map) param._attendSemAllMap.get(key);
            final String sday = (String) hogeMap.get("SDAY");
            final String eday = (String) hogeMap.get("EDAY");
            log.debug(key + " = " + sday + "〜" + eday);
        }
    }
    
    private class Term {
        final String _sdate;
        final String _edate;
        public Term(String sdate, String edate) {
            _sdate = sdate;
            _edate = edate;
        }
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _sdate;
        private final String _edate;
        private final String _sSemester;
        private final String _eSemester;
        private final String _date;
        private final String _week;
        private final String _periodInState;
        private final Map _attendSemAllMap;
        private final Map _hrClassMap = new TreeMap();
        private String _z010 = "";
        private final List _termList;
        /** 教育課程コードを使用するか */
        private final String _useCurriculumcd;
        private final String _useVirus;
        private final String _useKoudome;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            ResultSet rsZ010 = null;
            ResultSet rsHrClass = null;
            try {
                _year = request.getParameter("YEAR");
                _sdate = request.getParameter("SDATE");
                _edate = request.getParameter("EDATE");
                _termList = getTermList(db2, request.getParameter("OUTPUT"), _sdate, _edate);
                _sSemester = request.getParameter("SSEMESTER");
                _eSemester = request.getParameter("ESEMESTER");

                KNJ_Get_Info getinfo = new KNJ_Get_Info();
                KNJ_Get_Info.ReturnVal returnval = null;
                returnval = getinfo.Control(db2);
                _date = KNJ_EditDate.h_format_JP(returnval.val3);
                _week = KNJ_EditDate.h_format_W(returnval.val3) + "曜日";
                getinfo = null;
                returnval = null;
                try {
                    _knjSchoolMst = new KNJSchoolMst(db2, _year);
                } catch (SQLException e) {
                    log.warn("学校マスタ取得でエラー", e);
                }
                _periodInState = AttendAccumulate.getPeiodValue(db2, definecode, _year, _sSemester, _eSemester);

                db2.query(getZ010(_year));
                rsZ010 = db2.getResultSet();
                while (rsZ010.next()) {
                    _z010 = rsZ010.getString("NAME1");
                }
                db2.commit();

                _attendSemAllMap = AttendAccumulate.getAttendSemesMap(db2, _z010, _year);

                db2.query(getHrClass(_year, _eSemester));
                rsHrClass = db2.getResultSet();
                while (rsHrClass.next()) {
                    final Map data = new HashMap();
                    data.put("HR_NAME", rsHrClass.getString("HR_NAME"));
                    data.put("HR_NAMEABBV", rsHrClass.getString("HR_NAMEABBV"));
                    _hrClassMap.put(rsHrClass.getString("GRADE") + rsHrClass.getString("HR_CLASS"), data);
                }
            } finally {
                rsZ010.close();
                db2.commit();
            }
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
        }

        private String getHrClass(final String year, final String semester) {
            final String rtnSql = " SELECT "
                                + "     GRADE, "
                                + "     HR_CLASS, "
                                + "     MAX(HR_NAME) AS HR_NAME, "
                                + "     MAX(HR_NAMEABBV) AS HR_NAMEABBV "
                                + " FROM "
                                + "     SCHREG_REGD_HDAT "
                                + " WHERE "
                                + "     YEAR = '" + year + "' "
                                + "     AND SEMESTER <= '" + semester + "' "
                                + " GROUP BY "
                                + "     GRADE, "
                                + "     HR_CLASS ";

            return rtnSql;
        }

        private String getSemester(String year) {
            return null;
        }

        private String getZ010(final String year) {
            final String rtnSql = " SELECT "
                                + "     * "
                                + " FROM "
                                + "     V_NAME_MST "
                                + " WHERE "
                                + "     YEAR = '" + year + "' "
                                + "     AND NAMECD1 = 'Z010' "
                                + "     AND NAMECD2 = '00'";
            return rtnSql;
        }

        private boolean isKinH() {
            if (_z010.equals(KINH)) {
                return true;
            } else {
                return false;
            }
        }

        private boolean isKinJ() {
            if (_z010.equals(KINJ)) {
                return true;
            } else {
                return false;
            }
        }
        
        private List getTermList(final DB2UDB db2, final String output, final String sdate, final String edate) {
            final List termList = new ArrayList();
            if (!"1".equals(output)) {
                termList.add(new Term(sdate, edate));
            } else {
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    final String sql = " SELECT DISTINCT EXECUTEDATE FROM SCH_CHR_DAT WHERE EXECUTEDATE BETWEEN '" + sdate.replace('/', '-') + "' AND '" + edate.replace('/', '-') + "' ";
                    log.debug(" sql = "+ sql);
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        termList.add(new Term(rs.getString("EXECUTEDATE"), rs.getString("EXECUTEDATE")));
                    }
                } catch (final Exception e) {
                    log.error("exception!", e);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
            }
            return termList;
        }
    }

    private boolean printMain(final DB2UDB db2, Vrw32alp svf) throws ParseException, SQLException {
        boolean hasOneDate = false;
        
        for (final Iterator it = _param._termList.iterator(); it.hasNext();) {
            final Term term = (Term) it.next();
            final String sdate = term._sdate;
            final String edate = term._edate;
            
            log.debug(" 開始日付 = " + sdate + " , 終了日付 = " + edate);
            
            setHeadData(svf, sdate, edate);

            final List printDataList = getPrintData(db2, sdate, edate);
            boolean hasData = printOut(svf, printDataList);
            if (hasData) {
                svf.VrEndPage();
            }
            hasOneDate = hasOneDate || hasData;
        }
        return hasOneDate;
    }

    private void setHeadData(Vrw32alp svf, String sdate, String edate) {
        svf.VrSetForm(getFormName() + ".frm", 1);
        final String nendo = KNJ_EditDate.h_format_JP_N(_param._year+"-04-01") + "度";
        svf.VrsOut("NENDO", nendo);
        svf.VrsOut("DATE", _param._date);
        
        final String printSdate = KNJ_EditDate.h_format_JP(sdate);
        final String printSweek = KNJ_EditDate.h_format_W(sdate);
        final String printEdate = KNJ_EditDate.h_format_JP(edate);
        final String printEweek = KNJ_EditDate.h_format_W(edate);
        
        String printDate = printSdate + "(" + printSweek + ")" + "\uFF5E" + printEdate + "(" + printEweek + ")";
        if (printSdate.equals(printEdate)) {
            printDate = printSdate + "(" + printSweek + ")";
        }
        svf.VrsOut("SDATE", printDate);
    }

    private String getFormName() {
        String rtnForm = "";
        if (_param.isKinH() || _param.isKinJ()) {
            rtnForm = "KNJC181K";
        }
        return rtnForm;
    }

    private List getPrintData(final DB2UDB db2, String sdate, String edate) throws ParseException, SQLException {
        final List rtnList = new ArrayList();
        final String attendSql = getAttendSql(sdate, edate);
        log.debug(attendSql);
        PreparedStatement psAttend = null;
        ResultSet rsAttend = null;

        try {
            psAttend = db2.prepareStatement(attendSql);
            rsAttend = psAttend.executeQuery();
            while (rsAttend.next()) {
                final String grade = rsAttend.getString("GRADE");
                final String hrClass = rsAttend.getString("HR_CLASS");
                final Map hrMap = (Map) _param._hrClassMap.get(grade + hrClass);
                if (hrMap == null) {
                    log.debug(" 該当データ無し (学年 = " + grade + " , クラス = " + hrClass);
                    continue;
                }
                final String hrName = (String) hrMap.get("HR_NAME");
                final String hrNameAbbv = (String) hrMap.get("HR_NAMEABBV");
                final PrintData printData = new PrintData(grade,
                                                          hrClass,
                                                          hrName,
                                                          hrNameAbbv,
                                                          rsAttend.getInt("SUSPEND") + ("true".equals(_param._useVirus) ? rsAttend.getInt("VIRUS") : 0) + ("true".equals(_param._useKoudome) ? rsAttend.getInt("KOUDOME") : 0),
                                                          rsAttend.getInt("MOURNING"),
                                                          rsAttend.getInt("SICK"),
                                                          rsAttend.getInt("ABSENT"),
                                                          rsAttend.getInt("LATE"),
                                                          rsAttend.getInt("EARLY"));
                rtnList.add(printData);
                log.debug(printData);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, psAttend, rsAttend);
        }
        
        return rtnList;
    }

    private class PrintData {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrNameAbbv;
        int _suspend;
        int _mourning;
        int _absent;
        int _sick;
        int _late;
        int _early;

        private PrintData(
                final String grade,
                final String hrClass,
                final String hrName,
                final String hrNameAbbv,
                int suspend,
                int mourning,
                int sick,
                int absent,
                int late,
                int early
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
            _suspend = suspend;
            _mourning = mourning;
            _sick = sick;
            _absent = absent;
            _late = late;
            _early = early;
        }

        /** クラス略称の頭２桁 */
        private String getClassGroup() {
            return _hrNameAbbv.substring(0, 2);
        }

        public void addData(
                final int suspend,
                final int mourning,
                final int sick,
                final int absent,
                final int late,
                final int early
        ) {
            _suspend += suspend;
            _mourning += mourning;
            _sick += sick;
            _absent += absent;
            _late += late;
            _early += early;
        }

        public String toString() {
            return "学年 = " + _grade
            + " クラス = " + _hrClass
            + " クラス名称 = " + _hrName
            + " グループ = " + getClassGroup();
        }
    }

    private boolean printOut(final Vrw32alp svf, final List printDataList) {
        boolean hasData = false;
        int gyo = 1;
        String befGrade = "";
        String befGroup = "";
        PrintData classTotal = null;
        PrintData gradeTotal = null;
        PrintData allTotal = null;
        for (final Iterator iter = printDataList.iterator(); iter.hasNext();) {
            final PrintData printData = (PrintData) iter.next();

            if (classTotal != null && !befGroup.equals(printData.getClassGroup())) {
                setOutPut(svf, gyo, classTotal);
                gyo = gyo + 2;
                classTotal = null;
            }

            if (gradeTotal != null && !befGrade.equals(printData._grade)) {
                setOutPut(svf, 50, gradeTotal);
                gradeTotal = null;
            }

            gyo = getGyoCnt(gyo, befGrade, printData);

            if (classTotal == null || befGroup.equals(printData.getClassGroup())) {
                classTotal = setPrintTotal(classTotal, printData, "CLASS");
            }

            if (gradeTotal == null || befGrade.equals(printData._grade)) {
                gradeTotal = setPrintTotal(gradeTotal, printData, "GRADE");
            }

            allTotal = setPrintTotal(allTotal, printData, "ALL");

            setOutPut(svf, gyo, printData);

            gyo++;
            befGroup = printData.getClassGroup();
            befGrade = printData._grade;

            hasData = true;
        }

        if (classTotal != null) {
            setOutPut(svf, gyo, classTotal);
        }
        if (gradeTotal != null) {
            setOutPut(svf, 50, gradeTotal);
        }

        svf.VrsOut("TOTAL_SICK", allTotal._sick == 0 ? "" : String.valueOf(allTotal._sick));
        svf.VrsOut("TOTAL_LATE", allTotal._late == 0 ? "" : String.valueOf(allTotal._late));
        svf.VrsOut("TOTAL_EARLY", allTotal._early == 0 ? "" : String.valueOf(allTotal._early));
        svf.VrsOut("TOTAL_SUSPEND", allTotal._suspend == 0 ? "" : String.valueOf(allTotal._suspend));
        svf.VrsOut("TOTAL_MOURNING", allTotal._mourning == 0 ? "" : String.valueOf(allTotal._mourning));
        svf.VrsOut("TOTAL_ABSENT", allTotal._absent == 0 ? "" : String.valueOf(allTotal._absent));

        return hasData;
    }

    private PrintData setPrintTotal(final PrintData classTotal, final PrintData printData, final String div) {
        if (classTotal == null) {
            String abbv = div == "CLASS" ? printData.getClassGroup() + "計" : "計";
            abbv = div == "ALL" ? "全体計" : abbv;
            return new PrintData(printData._grade,
                                  printData._hrClass,
                                  printData._hrClass,
                                  abbv,
                                  printData._suspend,
                                  printData._mourning,
                                  printData._sick,
                                  printData._absent,
                                  printData._late,
                                  printData._early);
        } else {
            classTotal.addData(printData._suspend,
                               printData._mourning,
                               printData._sick,
                               printData._absent,
                               printData._late,
                               printData._early);
        }
        return classTotal;
    }

    private int getGyoCnt(final int gyo, final String grade, final PrintData printData) {
        int rtngyo = gyo;
        if (!grade.equals(printData._grade)) {
            rtngyo = 1;
        }
        return rtngyo;
    }

    private void setOutPut(final Vrw32alp svf, int gyo, final PrintData printData) {
        svf.VrsOutn("HR_NAME" + Integer.parseInt(printData._grade), gyo, printData._hrNameAbbv);
        svf.VrsOutn("SICK" + Integer.parseInt(printData._grade), gyo, printData._sick == 0 ? "" : String.valueOf(printData._sick));
        svf.VrsOutn("LATE" + Integer.parseInt(printData._grade), gyo, printData._late == 0 ? "" : String.valueOf(printData._late));
        svf.VrsOutn("EARLY" + Integer.parseInt(printData._grade), gyo, printData._early == 0 ? "" : String.valueOf(printData._early));
        svf.VrsOutn("SUSPEND" + Integer.parseInt(printData._grade), gyo, printData._suspend == 0 ? "" : String.valueOf(printData._suspend));
        svf.VrsOutn("MOURNING" + Integer.parseInt(printData._grade), gyo, printData._mourning == 0 ? "" : String.valueOf(printData._mourning));
        svf.VrsOutn("ABSENT" + Integer.parseInt(printData._grade), gyo, printData._absent == 0 ? "" : String.valueOf(printData._absent));
    }

    private String getAttendSql(final String sdate, final String edate) throws ParseException {
        final Map hasuuMap = AttendAccumulate.getHasuuMap(_param._attendSemAllMap, sdate, edate);
        final boolean semesFlg = ((Boolean) hasuuMap.get("semesFlg")).booleanValue();
        final String rtnSql = AttendAccumulate.getAttendSemesSql(
                                                semesFlg,
                                                definecode,
                                                _knjSchoolMst,
                                                _param._year,
                                                _param._sSemester,
                                                _param._eSemester,
                                                (String) hasuuMap.get("attendSemesInState"),
                                                _param._periodInState,
                                                (String) hasuuMap.get("befDayFrom"),
                                                (String) hasuuMap.get("befDayTo"),
                                                (String) hasuuMap.get("aftDayFrom"),
                                                (String) hasuuMap.get("aftDayTo"),
                                                null,
                                                null,
                                                null,
                                                "HR_CLASS",
                                                _param._useCurriculumcd,
                                                _param._useVirus,
                                                _param._useKoudome);
        return rtnSql;
    }
}
 // KNJC181K

// eof
