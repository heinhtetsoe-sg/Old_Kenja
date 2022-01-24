/*
 * $Id: 848fa308d9dee3341c9499a519a68e0ebf26f125 $
 *
 * 作成日: 2018/05/17
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJD132T {

    private static final Log log = LogFactory.getLog(KNJD132T.class);

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
        final int maxLine = 35;
        int line = 1;

        // 学習・特別活動の記録１
        svf.VrSetForm("KNJD132T_1.frm", 1);
        final List printList = getList(db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();

            if (line > maxLine) {
                svf.VrEndPage();
                svf.VrSetForm("KNJD132T_1.frm", 1);
                line = 1;
            }

            svf.VrsOut("TITLE", _param._ctrlYear + "年度　" + _param._semesterName + "　学習・特別活動等の記録１"); // タイトル
            svf.VrsOut("HR_NAME", String.valueOf(_param._hrClassInfo.get("HR_NAME")));         // 年組
            final String staffName = String.valueOf(_param._hrClassInfo.get("STAFFNAME"));
            final String staffNameField = KNJ_EditEdit.getMS932ByteLength(staffName) > 30 ? "3": KNJ_EditEdit.getMS932ByteLength(staffName) > 20 ? "2": "1";
            svf.VrsOut("TEACHER_NAME" + staffNameField, staffName); // 担任

            svf.VrsOutn("NO", line, printData._attendNo); // 出席番号
            final String nameField = KNJ_EditEdit.getMS932ByteLength(printData._name) > 20 ? "3": KNJ_EditEdit.getMS932ByteLength(printData._name) > 14 ? "2": "1";
            svf.VrsOutn("NAME" + nameField, line, printData._name); // 氏名

            // 総合的な学習の時間
            final String tTField   = KNJ_EditEdit.getMS932ByteLength(printData._totalstudyTime) > 24 ? "3": KNJ_EditEdit.getMS932ByteLength(printData._totalstudyTime) > 16 ? "2": "1";
            final String viewField = KNJ_EditEdit.getMS932ByteLength(printData._viewPoint) > 60 ? "5": KNJ_EditEdit.getMS932ByteLength(printData._viewPoint) > 30 ? "4": KNJ_EditEdit.getMS932ByteLength(printData._viewPoint) > 24 ? "3": KNJ_EditEdit.getMS932ByteLength(printData._viewPoint) > 16 ? "2": "1";
            final String aRField   = KNJ_EditEdit.getMS932ByteLength(printData._specialactRemark) > 110 ? "3_1": KNJ_EditEdit.getMS932ByteLength(printData._specialactRemark) > 80 ? "2": "1";
            svf.VrsOutn("TOTAL_STUDY" + tTField, line, printData._totalstudyTime); // 学習活動
            svf.VrsOutn("VIEW" + viewField, line, printData._viewPoint);           // 観点
            svf.VrsOutn("TOTAL_ACT" + aRField, line, printData._specialactRemark); // 活動の様子

            // 特別活動の記録
            final String cAField   = KNJ_EditEdit.getMS932ByteLength(printData._classAct) > 22 ? "4_1": KNJ_EditEdit.getMS932ByteLength(printData._classAct) > 18 ? "3": KNJ_EditEdit.getMS932ByteLength(printData._classAct) > 12 ? "2": "1";
            final String sCField   = KNJ_EditEdit.getMS932ByteLength(printData._schCouncil) > 22 ? "4_1": KNJ_EditEdit.getMS932ByteLength(printData._schCouncil) > 18 ? "3": KNJ_EditEdit.getMS932ByteLength(printData._schCouncil) > 12 ? "2": "1";
            final String sEField   = KNJ_EditEdit.getMS932ByteLength(printData._schoolEvent) > 44 ? "4_1": KNJ_EditEdit.getMS932ByteLength(printData._schoolEvent) > 32 ? "3": KNJ_EditEdit.getMS932ByteLength(printData._schoolEvent) > 22 ? "2": "1";
            svf.VrsOutn("HR_STUDY" + cAField, line, printData._classAct);        // 学級活動
            svf.VrsOutn("COMMITTEE" + sCField, line, printData._schCouncil);     // 生徒会活動
            svf.VrsOutn("SCHOOL_EVENT" + sEField, line, printData._schoolEvent); // 学校行事

            final String cNField   = KNJ_EditEdit.getMS932ByteLength(printData._clubName) > 24 ? "3": KNJ_EditEdit.getMS932ByteLength(printData._clubName) > 16 ? "2": "1";
            final String cMField   = KNJ_EditEdit.getMS932ByteLength(printData._clubMemo) > 30 ? "3_1": KNJ_EditEdit.getMS932ByteLength(printData._clubMemo) > 20 ? "2": "1";
            svf.VrsOutn("CLUB" + cNField, line, printData._clubName);     // 部活動名
            svf.VrsOutn("CLUB_REC" + cMField, line, printData._clubMemo); // 部活動の記録

            line++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }

        // 学習・特別活動の記録２
        boolean _hasDataUra = false;
        line = 1;
        svf.VrSetForm("KNJD132T_2.frm", 1);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();

            if (line > maxLine) {
                svf.VrEndPage();
                svf.VrSetForm("KNJD132T_2.frm", 1);
                line = 1;
            }

            svf.VrsOut("TITLE", _param._ctrlYear + "年度　" + _param._semesterName + "　学習・特別活動等の記録２"); // タイトル
            svf.VrsOut("HR_NAME", String.valueOf(_param._hrClassInfo.get("HR_NAME"))); // 年組
            final String staffName = String.valueOf(_param._hrClassInfo.get("STAFFNAME"));
            final String staffNameField = KNJ_EditEdit.getMS932ByteLength(staffName) > 30 ? "3": KNJ_EditEdit.getMS932ByteLength(staffName) > 20 ? "2": "1";
            svf.VrsOut("TEACHER_NAME" + staffNameField, staffName); // 担任

            svf.VrsOutn("NO", line, printData._attendNo); // 出席番号
            final String nameField = KNJ_EditEdit.getMS932ByteLength(printData._name) > 20 ? "3": KNJ_EditEdit.getMS932ByteLength(printData._name) > 14 ? "2": "1";
            svf.VrsOutn("NAME" + nameField, line, printData._name); // 氏名

            final String oAField   = KNJ_EditEdit.getMS932ByteLength(printData._otherAct) > 52 ? "4_1": KNJ_EditEdit.getMS932ByteLength(printData._otherAct) > 40 ? "3": KNJ_EditEdit.getMS932ByteLength(printData._otherAct) > 26 ? "2": "1";
            final String cMField   = KNJ_EditEdit.getMS932ByteLength(printData._communication) > 184 ? "3_1": KNJ_EditEdit.getMS932ByteLength(printData._communication) > 108 ? "2": "1";
            svf.VrsOutn("OTHER_ACT" + oAField, line, printData._otherAct); // その他の活動

            final String mFStr = StringUtils.defaultString(printData._otherMoral, "");
            final int mFLen = KNJ_EditEdit.getMS932ByteLength(mFStr);
            final String mFField = mFLen > 50 ? "3_1" : mFLen > 26 ? "2" : "1";
            svf.VrsOutn("MORAL"+mFField, line, mFStr);  //道徳欄

            svf.VrsOutn("COMM" + cMField, line, printData._communication); // 通信欄

            //出欠
            final Attendance att = Attendance.load(db2, _param, printData._schregNo);
            if (null != att) {
                svf.VrsOutn("LESSON", line, String.valueOf(att._lesson));           // 授業日
                svf.VrsOutn("SUSPEND", line, String.valueOf(att._suspendMourning)); // 出停等
                svf.VrsOutn("MUST", line, String.valueOf(att._youSyusseki));        // 要出席
                svf.VrsOutn("NOTICE", line, String.valueOf(att._kesseki));          // 欠席
                svf.VrsOutn("ATTEND", line, String.valueOf(att._syusseki));         // 出席
                svf.VrsOutn("LATE", line, String.valueOf(att._late));               // 遅刻
                svf.VrsOutn("EARLY", line, String.valueOf(att._early));             // 早退
            }

            line++;
            _hasDataUra = true;
        }
        if (_hasDataUra) {
            svf.VrEndPage();
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregNo           = rs.getString("SCHREGNO");
                final String attendNo           = rs.getString("ATTENDNO");
                final String name               = rs.getString("NAME");
                final String totalstudyTime     = rs.getString("TOTALSTUDYTIME");
                final String viewPoint          = rs.getString("VIEWPOINT");
                final String specialactRemark   = rs.getString("SPECIALACTREMARK");
                final String classAct           = rs.getString("CLASS_ACT");
                final String schCouncil         = rs.getString("SCH_COUNCIL");
                final String schoolEvent        = rs.getString("SCHOOL_EVENT");
                final String clubName           = rs.getString("CLUB_NAME");
                final String clubMemo           = rs.getString("CLUB_MEMO");
                final String otherAct           = rs.getString("OTHER_ACT");
                final String otherMoral         = rs.getString("OTHER_MORAL");
                final String communication      = rs.getString("COMMUNICATION");

                final PrintData printData = new PrintData(schregNo, attendNo, name, totalstudyTime, viewPoint, specialactRemark, classAct, schCouncil, schoolEvent, clubName, clubMemo, otherAct, otherMoral, communication);
                retList.add(printData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private class PrintData {
        final String _schregNo;
        final String _attendNo;
        final String _name;
        final String _totalstudyTime;
        final String _viewPoint;
        final String _specialactRemark;
        final String _classAct;
        final String _schCouncil;
        final String _schoolEvent;
        final String _clubName;
        final String _clubMemo;
        final String _otherAct;
        final String _otherMoral;
        final String _communication;
        public PrintData(
                final String schregNo,
                final String attendNo,
                final String name,
                final String totalstudyTime,
                final String viewPoint,
                final String specialactRemark,
                final String classAct,
                final String schCouncil,
                final String schoolEvent,
                final String clubName,
                final String clubMemo,
                final String otherAct,
                final String otherMoral,
                final String communication

        ) {
            _schregNo           = schregNo;
            _attendNo           = attendNo;
            _name               = name;
            _totalstudyTime     = totalstudyTime;
            _viewPoint          = viewPoint;
            _specialactRemark   = specialactRemark;
            _classAct           = classAct;
            _schCouncil         = schCouncil;
            _schoolEvent        = schoolEvent;
            _clubName           = clubName;
            _clubMemo           = clubMemo;
            _otherAct           = otherAct;
            _otherMoral         = otherMoral;
            _communication      = communication;
        }
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH HREPORTREMARKS AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         TOTALSTUDYTIME, ");       //学習活動
        stb.append("         REMARK1 AS VIEWPOINT, "); //観点
        stb.append("         SPECIALACTREMARK, ");     //活動の様子
        stb.append("         COMMUNICATION ");         //通信欄
        stb.append("     FROM ");
        stb.append("         HREPORTREMARK_DAT ");
        stb.append("     WHERE ");
        stb.append("             YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("         AND SEMESTER = '" + _param._semester + "' ");
        stb.append(" ), SCHINFO AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T2.NAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT T1, ");
        stb.append("         SCHREG_BASE_MST T2 ");
        stb.append("     WHERE ");
        stb.append("             T1.YEAR                 = '" + _param._ctrlYear + "' ");
        stb.append("         AND T1.SEMESTER             = '" + _param._semester + "' ");
        stb.append("         AND T1.GRADE || T1.HR_CLASS = '" + _param._gradeHrClass + "' ");
        stb.append("         AND T1.SCHREGNO             = T2.SCHREGNO ");
        stb.append(" ), SPECIAL_ACT AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         REMARK1 AS CLASS_ACT, ");   //学級活動
        stb.append("         REMARK2 AS SCH_COUNCIL, "); //生徒会活動
        stb.append("         REMARK3 AS SCHOOL_EVENT "); //学校行事
        stb.append("     FROM ");
        stb.append("         HREPORTREMARK_DETAIL_DAT ");
        stb.append("     WHERE ");
        stb.append("             YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("         AND SEMESTER = '" + _param._semester + "' ");
        stb.append("         AND DIV      = '01' ");
        stb.append("         AND CODE     = '01' ");
        stb.append(" ), CLUB_INFO AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         REMARK1 AS CLUB_NAME, "); //部活動名
        stb.append("         REMARK2 AS CLUB_MEMO ");  //部活動の記録
        stb.append("     FROM ");
        stb.append("         HREPORTREMARK_DETAIL_DAT ");
        stb.append("     WHERE ");
        stb.append("             YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("         AND SEMESTER = '" + _param._semester + "' ");
        stb.append("         AND DIV      = '02' ");
        stb.append("         AND CODE     = '01' ");
        stb.append(" ), OTHER_ACTION AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         REMARK1 AS OTHER_ACT "); //その他の活動
        stb.append("     FROM ");
        stb.append("         HREPORTREMARK_DETAIL_DAT ");
        stb.append("     WHERE ");
        stb.append("             YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("         AND SEMESTER = '" + _param._semester + "' ");
        stb.append("         AND DIV      = '03' ");
        stb.append("         AND CODE     = '01' ");
        stb.append(" ), OTHER_MRL AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         REMARK1 AS OTHER_MORAL "); //その他の活動
        stb.append("     FROM ");
        stb.append("         HREPORTREMARK_DETAIL_DAT ");
        stb.append("     WHERE ");
        stb.append("             YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("         AND SEMESTER = '" + _param._semester + "' ");
        stb.append("         AND DIV      = '04' ");
        stb.append("         AND CODE     = '01' ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     SCHI.SCHREGNO, ");
        stb.append("     SCHI.ATTENDNO, ");
        stb.append("     SCHI.NAME, ");
        stb.append("     HREP.TOTALSTUDYTIME, ");  //学習活動
        stb.append("     HREP.VIEWPOINT, ");       //観点
        stb.append("     HREP.SPECIALACTREMARK, ");//活動の様子
        stb.append("     SPCL.CLASS_ACT, ");       //学級活動
        stb.append("     SPCL.SCH_COUNCIL, ");     //生徒会活動
        stb.append("     SPCL.SCHOOL_EVENT, ");    //学校行事
        stb.append("     CLUB.CLUB_NAME, ");       //部活動名
        stb.append("     CLUB.CLUB_MEMO, ");       //部活動の記録
        stb.append("     OTHR.OTHER_ACT, ");       //その他の活動
        stb.append("     OTHM.OTHER_MORAL, ");     //道徳欄
        stb.append("     HREP.COMMUNICATION ");    //通信欄
        stb.append(" FROM ");
        stb.append("     SCHINFO SCHI ");
        stb.append("     LEFT JOIN HREPORTREMARKS HREP ON SCHI.SCHREGNO = HREP.SCHREGNO ");
        stb.append("     LEFT JOIN SPECIAL_ACT    SPCL ON SCHI.SCHREGNO = SPCL.SCHREGNO ");
        stb.append("     LEFT JOIN CLUB_INFO      CLUB ON SCHI.SCHREGNO = CLUB.SCHREGNO ");
        stb.append("     LEFT JOIN OTHER_ACTION   OTHR ON SCHI.SCHREGNO = OTHR.SCHREGNO ");
        stb.append("     LEFT JOIN OTHER_MRL      OTHM ON SCHI.SCHREGNO = OTHM.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("     SCHI.ATTENDNO ");

        return stb.toString();
    }

    /**
     * 出欠
     */
    private static class Attendance {
        int _lesson;
        int _suspendMourning;
        int _youSyusseki;
        int _kesseki;
        int _syusseki;
        int _late;
        int _early;

        public static Attendance load(final DB2UDB db2, final Param param, final String schregno) {
            final Attendance att = new Attendance();
            boolean nullChk = false;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     VALUE(LESSON, 0) - VALUE(ABROAD, 0) ");
                if (!"1".equals(param._schoolMst._semOffDays)) {
                    stb.append("     - VALUE(OFFDAYS, 0) ");
                }
                stb.append("     AS LESSON, ");
                stb.append("     VALUE(ABSENT, 0) AS ABSENT, ");
                stb.append("     VALUE(SUSPEND, 0) AS SUSPEND, ");
                if ("true".equals(param._useVirus)) {
                    stb.append("     VALUE(VIRUS, 0) AS VIRUS, ");
                } else {
                    stb.append("     0 AS VIRUS, ");
                }
                if ("true".equals(param._useKoudome)) {
                    stb.append("     VALUE(KOUDOME, 0) AS KOUDOME, ");
                } else {
                    stb.append("     0 AS KOUDOME, ");
                }
                stb.append("     VALUE(MOURNING, 0) AS MOURNING, ");
                stb.append("     VALUE(SICK, 0) + VALUE(NOTICE, 0) + VALUE(NONOTICE, 0) ");
                if ("1".equals(param._schoolMst._semOffDays)) {
                    stb.append("      + VALUE(OFFDAYS, 0) ");
                }
                stb.append("     AS SICK, ");
                stb.append("     VALUE(LATE, 0) AS LATE, ");
                stb.append("     VALUE(EARLY, 0) AS EARLY ");
                stb.append(" FROM ");
                stb.append("     ATTEND_SEMES_DAT ");
                stb.append(" WHERE ");
                stb.append("         YEAR      = '" + param._ctrlYear + "' ");
                stb.append("     AND SCHREGNO  = '" + schregno + "' ");
                stb.append("     AND SEMESTER <= '" + param._semester + "' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                log.debug(" sql =" + stb.toString());
                while (rs.next()) {
                    att._lesson          += rs.getInt("LESSON");
                    att._suspendMourning += rs.getInt("SUSPEND") + rs.getInt("MOURNING") + ("true".equals(param._useVirus) ? rs.getInt("VIRUS") : 0) + ("true".equals(param._useKoudome) ? rs.getInt("KOUDOME") : 0);
                    final int setNum = rs.getInt("LESSON") - (rs.getInt("SUSPEND") + rs.getInt("MOURNING") + ("true".equals(param._useVirus) ? rs.getInt("VIRUS") : 0) + ("true".equals(param._useKoudome) ? rs.getInt("KOUDOME") : 0));
                    att._youSyusseki     += setNum;
                    att._kesseki         += rs.getInt("SICK");
                    att._syusseki        += setNum - rs.getInt("SICK");
                    att._late            += rs.getInt("LATE");
                    att._early           += rs.getInt("EARLY");
                    nullChk = true;
                }
            } catch (Exception e) {
                log.error("attendance exception!", e);
            }
            if (nullChk) {
                return att;
            } else {
                return null;
            }
        }
    }

    /**
     * 学校マスタ
     */
    private class SchoolMst {
        final String _semOffDays;
        public SchoolMst(
                final String semOffDays

        ) {
            _semOffDays           = semOffDays;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72924 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _semester;
        private final String _semesterName;
        private final String _gradeHrClass;
        private final Map _hrClassInfo;
        private final String _ctrlYear;
        private final String _schoolCd;
        private final String _schoolKind;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final SchoolMst _schoolMst;

        private final String _useVirus;
        private final String _useKoudome;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear       = request.getParameter("CTRL_YEAR");
            _semester       = request.getParameter("SEMESTER");
            _semesterName   = getSemeName(db2, _ctrlYear, _semester);
            _gradeHrClass   = request.getParameter("GRADE_HR_CLASS");
            _hrClassInfo    = getHrClassInfoMap(db2, _ctrlYear, _semester, _gradeHrClass);
            _schoolCd       = request.getParameter("SCHOOLCD");
            _schoolKind     = request.getParameter("SCHOOLKIND");
            _ctrlSemester   = request.getParameter("CTRL_SEMESTER");
            _ctrlDate       = request.getParameter("CTRL_DATE");
            _schoolMst      = getSchoolMst(db2, _ctrlYear, _schoolCd, _schoolKind);

            _useVirus       = request.getParameter("useVirus");
            _useKoudome     = request.getParameter("useKoudome");
        }

        private SchoolMst getSchoolMst(final DB2UDB db2, final String year, final String schoolCd, final String schoolKind) {
            SchoolMst schoolMst = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     SEM_OFFDAYS ");
                stb.append(" FROM ");
                stb.append("     SCHOOL_MST ");
                stb.append(" WHERE ");
                stb.append("         YEAR        = '" + year + "' ");
                stb.append("     AND SCHOOLCD    = '" + schoolCd + "' ");
                stb.append("     AND SCHOOL_KIND = '" + schoolKind + "' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                log.debug(" sql =" + stb.toString());
                if (rs.next()) {
                    final String semOffDays = rs.getString("SEM_OFFDAYS");
                    schoolMst = new SchoolMst(semOffDays);
                }
            } catch (SQLException ex) {
                log.debug("getScoolMst exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolMst;
        }

        private String getSemeName(final DB2UDB db2, final String year, final String semester) {
            String retSemeName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    retSemeName = rs.getString("SEMESTERNAME");
                }
            } catch (SQLException ex) {
                log.debug("getSemeName exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSemeName;
        }

        private Map getHrClassInfoMap(final DB2UDB db2, final String year, final String semester, final String gradeHrClass) {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     HDAT.HR_NAMEABBV, ");
                stb.append("     STFF.STAFFNAME ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_HDAT HDAT ");
                stb.append("     LEFT JOIN STAFF_MST STFF ON STFF.STAFFCD = HDAT.TR_CD1 ");
                stb.append(" WHERE ");
                stb.append("         HDAT.YEAR     = '" + year + "' ");
                stb.append("     AND HDAT.SEMESTER = '" + semester + "' ");
                stb.append("     AND HDAT.GRADE || HDAT.HR_CLASS = '" + gradeHrClass + "' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retMap.put("HR_NAME", rs.getString("HR_NAMEABBV"));
                    retMap.put("STAFFNAME", rs.getString("STAFFNAME"));
                }
            } catch (SQLException ex) {
                log.debug("getsTaffName exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }
    }
}

// eof
