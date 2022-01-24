package servletpack.KNJA;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * クラス担任用の出欠簿
 *
 * @author nakasone
 *
 */

public class KNJA224G {

    private static final Log log = LogFactory.getLog(KNJA224G.class);

    private boolean _hasData;

    private Param _param;

    private static final int PAGE_MAX_LINE = 45;
    private static final int PAGE_MAX_COL  = 12;

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        svf.VrSetForm("KNJA224G.frm", 1);

        Map<Integer, List<List<Date>>> dateMap = getDateMap();
        Map<String, Hr> syuketubo = getSyuketubo(db2);

        for (Hr hr : syuketubo.values()) {
            for (List<Student> studentList : hr._studentList) {
                for (Integer month : dateMap.keySet()) {
                    List<List<Date>> dateLists = dateMap.get(month);

                    boolean enterFlg = false;

                    for (int i = 0; i < dateLists.size(); i++) {
                        List<Date> dateList = dateLists.get(i);
                        int around = (i % 2) + 1;

                        printTitle(svf, month, hr, around);
                        printHeader(svf, dateList, around);
                        printBody(svf, studentList, around);

                        // 左右書き終えたら改ページする
                        if (around == 2) {
                            svf.VrEndPage();
                            enterFlg = true;
                        } else {
                            enterFlg = false;
                        }
                    }

                    // 左を書き込んで終えた場合は、改ページされていないので改ページする
                    if (!enterFlg) {
                        svf.VrEndPage();
                    }
                }
            }
        }
    }

    private void printTitle(final Vrw32alp svf, final int month, final Hr hr, final int around) {
        svf.VrsOut("MONTH" + around, (month + 1) + "月");
        svf.VrsOut("HR_NAME" + around, "HR" + hr._hrClass + "　出席簿");
    }

    private void printHeader(final Vrw32alp svf, final List<Date> dateList, final int around) {
        int colCnt = 1;

        for (Date date : dateList) {
            SimpleDateFormat dayFormat = new SimpleDateFormat("d");
            svf.VrsOutn("DAY" + around, colCnt, dayFormat.format(date));

            SimpleDateFormat weekdayFormat = new SimpleDateFormat("E");
            String weekday = weekdayFormat.format(date);
            svf.VrsOutn("WEEK" + around, colCnt, weekday);

            colCnt++;
        }
    }

    private void printBody(final Vrw32alp svf, final List<Student> studentList, final int around) {
        int lineCnt = 1;

        for (Student student : studentList) {
            svf.VrsOutn("ATTEND_NO" + around, lineCnt, String.valueOf(Integer.parseInt(student._attendNo)));

            String suffix = student._grdName != null ? "（" + student._grdName + "）" : student._transferName != null ? "（" + student._transferName + "）" : "";
            String name = student._sex + " " + StringUtils.defaultString(student._name) + suffix;
            int nameByte = KNJ_EditEdit.getMS932ByteLength(name);
            String nameField = nameByte >= 30 ? "3" : nameByte >= 20 ? "2" : "1";
            svf.VrsOutn("NAME" + around + "_" + nameField, lineCnt, name);

            _hasData = true;
            lineCnt++;
        }
    }

    private Map<Integer, List<List<Date>>> getDateMap() {
        Map<Integer, List<List<Date>>> monthMap = new LinkedHashMap<Integer, List<List<Date>>>();
        List<List<Date>> dateLists = new ArrayList<List<Date>>();
        List<Date> dateList = null;
        SimpleDateFormat dFormat = new SimpleDateFormat("yyyy/MM/dd");
        Calendar cal = Calendar.getInstance();

        try {
            for (Date date = dFormat.parse(_param._dateFrom); date.compareTo(dFormat.parse(_param._dateTo)) <= 0; date = DateUtils.addDays(date, 1)) {
                cal.setTime(date);
                int month = cal.get(Calendar.MONTH);
                if (monthMap.containsKey(month)) {
                    dateLists = monthMap.get(month);
                } else {
                    dateLists = new ArrayList<List<Date>>();
                    monthMap.put(month, dateLists);
                }

                if (dateLists.size() == 0) {
                    dateList = new ArrayList<Date>();
                    dateLists.add(dateList);
                } else if (PAGE_MAX_COL <= dateLists.get(dateLists.size() - 1).size()) {
                    dateList = new ArrayList<Date>();
                    dateLists.add(dateList);
                } else {
                    dateList = dateLists.get(dateLists.size() - 1);
                }

                dateList.add(date);
            }
        } catch (ParseException e) {
            log.debug("Exception:", e);
        }

        return monthMap;
    }

    private Map<String, Hr> getSyuketubo(final DB2UDB db2) {
        Map<String, Hr> hrMap = new LinkedHashMap<String, Hr>();
        Hr hr = null;
        List<Student> studentList = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String hrSql = getHrSql();
        log.debug(" sql =" + hrSql);

        try {
            ps = db2.prepareStatement(hrSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String hrClass = rs.getString("HR_CLASS");
                final String attendNo = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");
                final String grdName = rs.getString("GRD_NAME");
                final String transferName = rs.getString("TRANSFER_NAME");

                if (hrMap.containsKey(hrClass)) {
                    hr = hrMap.get(hrClass);
                } else {
                    hr = new Hr(hrClass);
                    hrMap.put(hrClass, hr);
                }

                Student student = new Student(attendNo, name, sex, grdName, transferName);

                if (hr._studentList.size() == 0) {
                    studentList = new ArrayList<Student>();
                    hr._studentList.add(studentList);
                } else if (PAGE_MAX_LINE <= hr._studentList.get(hr._studentList.size() - 1).size()) {
                    studentList = new ArrayList<Student>();
                    hr._studentList.add(studentList);
                } else {
                    studentList = hr._studentList.get(hr._studentList.size() - 1);
                }

                hr._studentList.get(hr._studentList.size() - 1).add(student);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return hrMap;
    }

    private String getHrSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH TRF AS ( ");
        stb.append("     SELECT ");
        stb.append("         TRF1.SCHREGNO, ");
        stb.append("         TRF1.TRANSFERCD, ");
        stb.append("         TRF1.TRANSFER_SDATE ");
        stb.append("     FROM ");
        stb.append("         SCHREG_TRANSFER_DAT TRF1 ");
        stb.append("         INNER JOIN ( ");
        stb.append("             SELECT ");
        stb.append("                 SCHREGNO, ");
        stb.append("                 TRANSFERCD, ");
        stb.append("                 MAX(TRANSFER_SDATE) AS TRANSFER_SDATE ");
        stb.append("             FROM ");
        stb.append("                 SCHREG_TRANSFER_DAT ");
        stb.append("             WHERE ");
        stb.append("                 (TO_DATE('" + _param._dateFrom + "', 'YYYY/MM/DD') BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE) ");
        stb.append("             OR  (TRANSFER_SDATE <= TO_DATE('" + _param._dateFrom + "', 'YYYY/MM/DD') AND TRANSFER_EDATE IS NULL) ");
        stb.append("             OR  (TO_DATE('" + _param._dateTo + "', 'YYYY/MM/DD') BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE) ");
        stb.append("             OR  (TRANSFER_SDATE BETWEEN TO_DATE('" + _param._dateFrom + "', 'YYYY/MM/DD') AND TO_DATE('" + _param._dateTo + "', 'YYYY/MM/DD'))");
        stb.append("             GROUP BY ");
        stb.append("                 SCHREGNO, ");
        stb.append("                 TRANSFERCD ");
        stb.append("         ) TRF2 ON ");
        stb.append("             TRF1.SCHREGNO       = TRF2.SCHREGNO ");
        stb.append("         AND TRF1.TRANSFERCD     = TRF2.TRANSFERCD ");
        stb.append("         AND TRF1.TRANSFER_SDATE = TRF2.TRANSFER_SDATE ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     HDAT.HR_CLASS, ");
        stb.append("     DAT.ATTENDNO, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     BASE.NAME, ");
        stb.append("     A003.NAME1 AS GRD_NAME, ");
        stb.append("     A004.NAME1 AS TRANSFER_NAME, ");
        stb.append("     TRF.TRANSFER_SDATE ");

        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_GDAT GDAT ");

        stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT ON ");
        stb.append("               HDAT.YEAR  = GDAT.YEAR ");
        stb.append("           AND HDAT.GRADE = GDAT.GRADE ");

        stb.append("     LEFT JOIN SCHREG_REGD_DAT DAT ON ");
        stb.append("               DAT.YEAR     = HDAT.YEAR ");
        stb.append("           AND DAT.SEMESTER = HDAT.SEMESTER ");
        stb.append("           AND DAT.GRADE    = HDAT.GRADE ");
        stb.append("           AND DAT.HR_CLASS = HDAT.HR_CLASS ");

        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON ");
        stb.append("               BASE.SCHREGNO = DAT.SCHREGNO ");

        stb.append("     LEFT JOIN V_NAME_MST Z002 ON ");
        stb.append("               Z002.YEAR    = DAT.YEAR ");
        stb.append("           AND Z002.NAMECD1 = 'Z002' ");
        stb.append("           AND Z002.NAMECD2 = BASE.SEX ");

        stb.append("     LEFT JOIN V_NAME_MST A003 ON ");
        stb.append("               A003.YEAR    = DAT.YEAR ");
        stb.append("           AND A003.NAMECD1 = 'A003' ");
        stb.append("           AND A003.NAMECD2 = BASE.GRD_DIV ");

        stb.append("     LEFT JOIN TRF ON ");
        stb.append("               TRF.SCHREGNO = BASE.SCHREGNO ");

        stb.append("     LEFT JOIN V_NAME_MST A004 ON ");
        stb.append("               A004.YEAR    = DAT.YEAR ");
        stb.append("           AND A004.NAMECD1 = 'A004' ");
        stb.append("           AND A004.NAMECD2 = TRF.TRANSFERCD ");

        stb.append(" WHERE ");
        stb.append("     GDAT.YEAR     = '" + _param._year + "' AND ");
        stb.append("     DAT.SEMESTER  = '" + _param._semester + "' AND ");
        stb.append("     GDAT.GRADE    = '" + _param._grade + "' AND ");
        stb.append(SQLUtils.whereIn(true, "DAT.HR_CLASS", _param._classSelected));
        stb.append(" ORDER BY ");
        stb.append("     HDAT.HR_CLASS, ");
        stb.append("     DAT.ATTENDNO ");

        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class Hr {
        final String _hrClass;
        final List<List<Student>> _studentList;

        Hr(
            final String hrClass
        ) {
            _hrClass    = hrClass;
            _studentList = new ArrayList<List<Student>>();
        }
    }

    private class Student {
        final String _attendNo;
        final String _name;
        final String _sex;
        final String _grdName;
        final String _transferName;

        Student(
            final String attendNo,
            final String name,
            final String sex,
            final String grdName,
            final String transferName
        ) {
            _attendNo     = attendNo;
            _name         = name;
            _sex          = sex;
            _grdName      = grdName;
            _transferName = transferName;
        }
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _loginDate;
        private final String _grade;
        private final String[] _classSelected;
        private final String _dateFrom;
        private final String _dateTo;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year          = request.getParameter("YEAR");
            _semester      = request.getParameter("SEMESTER");
            _loginDate     = request.getParameter("LOGIN_DATE");
            _grade         = request.getParameter("GRADE");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _dateFrom      = request.getParameter("DATE_FROM");
            _dateTo        = request.getParameter("DATE_TO");
        }
    }

}// クラスの括り
