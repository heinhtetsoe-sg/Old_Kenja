/*
 * $Id:$
 *
 * 作成日: 2021/01/07
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJC;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJC161C {

    private static final Log log = LogFactory.getLog(KNJC161C.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List hrList = getList(db2);  //クラス情報(一括)
        final Map attendMap = getAttendMap(db2);  //出欠席情報(一括)

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat( "yyyy/MM/dd HH:mm" );
        final String nowDate = format.format(cal.getTime());  //現在時刻を取得

        int page = 1;
        for (Iterator itHr = hrList.iterator(); itHr.hasNext();) {
            HrClass hrClass = (HrClass) itHr.next();
            final Map stuMap = getStudent(db2, hrClass._hr_Class);
            //生徒情報(1クラス単位)
            svf.VrSetForm("KNJC161C.frm", 1);
            setTitle(db2, svf, nowDate, page);
            //年度校種課程年組担任名
            svf.VrsOut("SUBTITLE", KNJ_EditDate.h_format_Seireki_N(_param._date) + "度 " + StringUtils.defaultString(_param._schKindName) + " " + StringUtils.defaultString(hrClass._majorName) + " " + StringUtils.defaultString(hrClass._hr_Name) + " (" + StringUtils.defaultString(hrClass._staffName) + ")");
            for (int wCnt = 0;wCnt < _param._prtDayList.size();wCnt++) {  //曜日(横)
                final String wdStr = (String)_param._prtDayList.get(wCnt);
                //日付
                svf.VrsOut("DAY" + (wCnt + 1), KNJ_EditDate.h_format_JP_MD(wdStr));
            }
            for (int wCnt = 0;wCnt < _param._prtDayList.size();wCnt++) {  //曜日(横)
                final String wdStr = (String)_param._prtDayList.get(wCnt);
                int lineCnt = 1; //★stuMapとセットで異動
                for (Iterator itr = stuMap.keySet().iterator();itr.hasNext();) {  //生徒(縦)
                    final String fKey = (String)itr.next();
                    final Student stu = (Student)stuMap.get(fKey);
                    //番号
                    svf.VrsOutn("ATTENDNO", lineCnt, String.valueOf(Integer.parseInt(stu._attendno)));
                    final int nlen = KNJ_EditEdit.getMS932ByteLength(stu._name);
                    final String nfield = nlen > 30 ? "3" : nlen > 20 ? "2" : "1";
                    svf.VrsOutn("NAME" + nfield, lineCnt, stu._name);
                    //生徒氏名
                    final String detKey = fKey + "-" + wdStr;
                    if (attendMap.containsKey(detKey)) {
                        //詳細
                        final List subList = (List)attendMap.get(detKey);
                        String putStr = "";
                        for (Iterator its = subList.iterator();its.hasNext();) {
                            final AttendInfo att = (AttendInfo)its.next();
                            putStr += StringUtils.defaultString(att._status);
                        }
                        if (!"".equals(putStr)) {
                            svf.VrsOutn("REASON" + (wCnt + 1), lineCnt, putStr);
                        }
                    }
                    lineCnt++; //★stuMapとセットで異動
                }
            }
            _hasData = true;
            page++;
            svf.VrEndPage();
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final String nowDate, final int page) {
        //タイトル
        svf.VrsOut("TITLE", "□ 週間出欠チェックリスト □");
        //日付
        svf.VrsOut("PRINT_DATE", nowDate);
        //ページ
        svf.VrsOut("PAGE", String.format("%3s", page + "ページ"));
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = hrSql();
        log.debug(" sql =" + sql);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String grade_Name1 = rs.getString("GRADE_NAME1");
                final String hr_Class = rs.getString("HR_CLASS");
                final String hr_Name = rs.getString("HR_NAME");
                final String hr_NameAbbv = rs.getString("HR_NAMEABBV");
                final String tr_Cd1 = rs.getString("TR_CD1");
                final String staffName = rs.getString("STAFFNAME");
                final String majorName = rs.getString("MAJORNAME");

                final HrClass hrClass = new HrClass(grade, grade_Name1, hr_Class, hr_Name, hr_NameAbbv, tr_Cd1, staffName, majorName);
                retList.add(hrClass);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }


    private String hrSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH HR_MAJORCD AS (");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     MIN(T1.majorcd) AS MAJORCD ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SEMESTER_MST SM ");
        stb.append("       ON SM.YEAR = T1.YEAR ");
        stb.append("      AND SM.SEMESTER = T1.SEMESTER ");
        stb.append(" GROUP BY ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.COURSECD ");
        stb.append(" ) ");
        stb.append(" select ");
        stb.append("   T2.GRADE, ");
        stb.append("   T2.GRADE_NAME1, ");
        stb.append("   T1.HR_CLASS, ");
        stb.append("   T1.HR_NAME, ");
        stb.append("   T1.HR_NAMEABBV, ");
        stb.append("   T1.TR_CD1, ");
        stb.append("   STM.STAFFNAME, ");
        stb.append("   MM.MAJORNAME ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_HDAT T1 ");
        stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.GRADE = T1.GRADE ");
        stb.append("   LEFT JOIN SEMESTER_MST SM ");
        stb.append("     ON SM.YEAR = T1.YEAR ");
        stb.append("    AND SM.SEMESTER = T1.SEMESTER ");
        stb.append("   LEFT JOIN STAFF_MST STM ");
        stb.append("     ON STM.STAFFCD = T1.TR_CD1 ");
        stb.append("   LEFT JOIN HR_MAJORCD HM ");
        stb.append("     ON HM.YEAR = T1.YEAR ");
        stb.append("    AND HM.SEMESTER = T1.SEMESTER ");
        stb.append("    AND HM.GRADE = T1.GRADE ");
        stb.append("    AND HM.HR_CLASS = T1.HR_CLASS ");
        stb.append("   LEFT JOIN MAJOR_MST MM ");
        stb.append("     ON MM.COURSECD = HM.COURSECD ");
        stb.append("    AND MM.MAJORCD = HM.MAJORCD ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("   AND T1.HR_CLASS IN " + SQLUtils.whereIn(false, _param._categorySelected) + " ");
        stb.append("   AND '" + _param._date + "' BETWEEN SM.SDATE AND SM.EDATE ");
        stb.append(" ORDER BY ");
        stb.append("   T1.GRADE, ");
        stb.append("   T1.HR_CLASS ");
        return stb.toString();
    }

    private class HrClass {
        final String _grade;
        final String _grade_Name1;
        final String _hr_Class;
        final String _hr_Name;
        final String _hr_NameAbbv;
        final String _tr_Cd1;
        final String _staffName;
        final String _majorName;
        public HrClass (final String grade, final String grade_Name1, final String hr_Class, final String hr_Name, final String hr_NameAbbv, final String tr_Cd1, final String staffName, final String majorName)
        {
            _grade = grade;
            _grade_Name1 = grade_Name1;
            _hr_Class = hr_Class;
            _hr_Name = hr_Name;
            _hr_NameAbbv = hr_NameAbbv;
            _tr_Cd1 = tr_Cd1;
            _staffName = staffName;
            _majorName = majorName;
        }
    }

    private Map getStudent(final DB2UDB db2, final String hrClass) {
        final Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = studentSql(hrClass);
        log.debug(" sql =" + sql);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String hr_Class = rs.getString("HR_CLASS");
                final String attendno = rs.getString("ATTENDNO");
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");

                final Student addWk = new Student(grade, hr_Class, attendno, schregno, name);
                retMap.put(schregno, addWk);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String studentSql(final String hrClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" select ");
        stb.append("   T1.GRADE, ");
        stb.append("   T1.HR_CLASS, ");
        stb.append("   T1.ATTENDNO, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T2.NAME ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append("   LEFT JOIN SCHREG_BASE_MST T2 ");
        stb.append("     ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN SEMESTER_MST SM ");
        stb.append("     ON SM.YEAR = T1.YEAR ");
        stb.append("    AND SM.SEMESTER = T1.SEMESTER ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("   AND T1.HR_CLASS = '" + hrClass + "' ");
        stb.append("   AND '" + _param._date + "' BETWEEN SM.SDATE AND SM.EDATE ");
        stb.append(" ORDER BY ");
        stb.append("   T1.ATTENDNO ");
        return stb.toString();
    }

    private class Student {
        final String _grade;
        final String _hr_Class;
        final String _attendno;
        final String _schregno;
        final String _name;
        public Student (final String grade, final String hr_Class, final String attendno, final String schregno, final String name)
        {
            _grade = grade;
            _hr_Class = hr_Class;
            _attendno = attendno;
            _schregno = schregno;
            _name = name;
        }
    }

    private Map getAttendMap(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        List subList = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = attendInfoSql();
        log.debug(" sql =" + sql);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String attenddate = rs.getString("ATTENDDATE");
                final String di_Cd = rs.getString("DI_CD");
                final String status = rs.getString("STATUS");

                final AttendInfo addWk = new AttendInfo(schregno, attenddate, di_Cd, status);
                final String fstKey = schregno + "-" + attenddate;
                if (!retMap.containsKey(fstKey)) {
                    subList = new ArrayList();
                    retMap.put(fstKey,  subList);
                } else {
                    subList = (List)retMap.get(fstKey);
                }
                subList.add(addWk);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String attendInfoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" select ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T2.ATTENDDATE, ");
        stb.append("   T2.DI_CD, ");
        stb.append("   C41.NAME1 AS STATUS ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append("   LEFT JOIN SEMESTER_MST SM ");
        stb.append("     ON SM.YEAR = T1.YEAR ");
        stb.append("    AND SM.SEMESTER = T1.SEMESTER ");
        stb.append("   LEFT JOIN ATTEND_DAY_DAT T2 ");
        stb.append("     ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("    AND (T2.ATTENDDATE BETWEEN '" + _param._sdate + "' AND '" + _param._edate + "') ");
        stb.append("    AND T2.YEAR = T1.YEAR ");
        stb.append("   LEFT JOIN SCHREG_REGD_GDAT GDAT ");
        stb.append("     ON GDAT.YEAR = T1.YEAR ");
        stb.append("    AND GDAT.GRADE = T1.GRADE ");
        stb.append("   LEFT JOIN NAME_MST C41 ");
        stb.append("     ON C41.NAMECD1 = 'C' || GDAT.SCHOOL_KIND || '41' ");
        stb.append("    AND C41.NAMECD2 = T2.DI_CD ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("   AND T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append("   AND '" + _param._date + "' BETWEEN SM.SDATE AND SM.EDATE ");
        stb.append(" ORDER BY ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T2.ATTENDDATE ");
        return stb.toString();
    }

    public String concatAttendStr(final List dList) {
        String retStr = "";
        String dlm = "";
        for (Iterator ite = dList.iterator();ite.hasNext();) {
            final AttendInfo aInf = (AttendInfo)ite.next();
            retStr = retStr + dlm + aInf._status;
            dlm = ",";
        }
        return retStr;
    }

    private class AttendInfo {
        final String _schregno;
        final String _attenddate;
        final String _di_Cd;
        final String _status;
        public AttendInfo (final String schregno, final String attenddate, final String di_Cd, final String status)
        {
            _schregno = schregno;
            _attenddate = attenddate;
            _di_Cd = di_Cd;
            _status = status;
        }
    }

    public Date toDate(final String date) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (null != date) {
            try {
                return sdf.parse(date);
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
            }
        }
        return null;
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _grade;
        private final String[] _categorySelected;
        private final String _date;
        private final String _sdate;
        private final String _edate;

        private final String _schKind;
        private final String _schKindName;
        private final List _prtDayList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _categorySelected = request.getParameterValues("CLASS_SELECTED");
            _date = request.getParameter("DATE").replace('/', '-');
            _sdate = request.getParameter("CALCD_START_DATE").replace('/', '-');
            _edate = request.getParameter("CALCD_END_DATE").replace('/', '-');
            _schKind =
            _schKindName = getSchKindName(db2);
            _prtDayList = getPrtDayList(db2);
        }

        private String getSchKind(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT T1.SCHOOL_KIND FROM SCHREG_REGD_GDAT T1 WHERE T1.YEAR = '" + _year + "' AND T1.GRADE = '" + _grade + "' "));
        }
        private String getSchKindName(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT T2.ABBV1 FROM SCHREG_REGD_GDAT T1 LEFT JOIN V_NAME_MST T2 ON T2.YEAR = T1.YEAR AND T2.NAMECD1 = 'A023' AND T2.NAME1 = T1.SCHOOL_KIND WHERE T1.YEAR = '" + _year + "' AND T1.GRADE = '" + _grade + "' "));
        }
        private List getPrtDayList(final DB2UDB db2) {
            final List retList = new ArrayList();
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            cal.setTime(toDate(_sdate));
            for (int cnt = 0;cnt < 7;cnt++) {
                retList.add(sdf.format(cal.getTime()));
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
            return retList;
        }
    }
}

// eof

