/*
 * $Id: 9179ce7031e1405ab65471364e73befc1c0b637a $
 *
 * 作成日: 2015/02/26
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJJ;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJJ140 {

    private static final Log log = LogFactory.getLog(KNJJ140.class);

    private boolean _hasData;

    private Param _param;
    private String _useSchool_KindField;
    private String _SCHOOLCD;
    private String _SCHOOLKIND;
    private String _use_prg_schoolkind;
    private String _selectSchoolKind;
    private String _selectSchoolKindSql;
    private String _useClubMultiSchoolKind;

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
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD= request.getParameter("SCHOOLCD");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _selectSchoolKind = request.getParameter("selectSchoolKind");
            _useClubMultiSchoolKind = request.getParameter("useClubMultiSchoolKind");
            if (!StringUtils.isBlank(_selectSchoolKind)) {
                StringBuffer stb = new StringBuffer("('");
                final String[] split = StringUtils.split(_selectSchoolKind, ":");
                if (null != split) {
                    for (int i = 0; i < split.length; i++) {
                        stb.append(split[i] + "', '");
                    }
                }
                _selectSchoolKindSql = stb.append("')").toString();
            }
            _hasData = false;

            for (int i = 0; i < _param._hrClasscd.length; i++) {
                printMain(db2, svf, _param._hrClasscd[i]);
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final String hrClass) {
        svf.VrSetForm("KNJJ140.frm", 4);
        final Map stdMap = getStdMap(db2, hrClass);

        svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._ctrlDate));
        int grpNo = 1;
        for (Iterator itStd = stdMap.keySet().iterator(); itStd.hasNext();) {
            String stdKey = (String) itStd.next();
            Student student = (Student) stdMap.get(stdKey);
            final int addrSize = "on".equals(_param._hogosya) ? max(new int[] {student._guardian._guardName.size(), student._guardian._guardAddr1.size(), student._guardian._guardTelno.size()}) : 0;
            final int maxLine = max(new int[] {student._clubList.size(), student.getCommitteeStringList().size(), addrSize});
            svf.VrsOut("ATTENDNO", student._attendNo);
            svf.VrsOut("NAME", student._name);
            for (int lineCnt = 0; lineCnt < maxLine; lineCnt++) {
                svf.VrsOut("CLASS", student._hrName);
                svf.VrsOut("GRP1", String.valueOf(grpNo));
                svf.VrsOut("GRP2", String.valueOf(grpNo));
                svf.VrsOut("GRP3", String.valueOf(grpNo));
                svf.VrsOut("GRP4", String.valueOf(grpNo));
                svf.VrsOut("GRP5", String.valueOf(grpNo));
                svf.VrsOut("GRP6", String.valueOf(grpNo));
                svf.VrsOut("GRP7", String.valueOf(grpNo));
                if (lineCnt < student._clubList.size()) {
                    final Club club = (Club) student._clubList.get(lineCnt);
                    //svf.VrsOut("CLUBNAME", club._clubname + "(" + club._sdate + club._edate + ")" + club._postname);
                    svf.VrsOut("CLUBNAME", club._clubname + (StringUtils.isBlank(club._postname) ? "" : " （" + club._postname + "）"));
                }
                if (lineCnt < student.getCommitteeStringList().size()) {
                    final String committeeString = (String) student.getCommitteeStringList().get(lineCnt);
                    svf.VrsOut("COMMINAME", committeeString);
                }
                if ("on".equals(_param._hogosya)) {
                    if (lineCnt < student._guardian._guardName.size()) {
                        svf.VrsOut("GUARD_NAME", (String) student._guardian._guardName.get(lineCnt));
                    }
                    if (lineCnt < student._guardian._guardAddr1.size()) {
                        svf.VrsOut("ADDRESS", (String) student._guardian._guardAddr1.get(lineCnt));
                    }
                    if (lineCnt < student._guardian._guardTelno.size()) {
                        svf.VrsOut("TELNO", (String) student._guardian._guardTelno.get(lineCnt));
                    }
                }
                svf.VrEndRecord();
            }
            grpNo++;
            _hasData = true;
        }
    }

    private int max(int[] is) {
        int rtn = Integer.MIN_VALUE;
        for (int i = 0; i < is.length; i++) {
            rtn = Math.max(rtn, is[i]);
        }
        return rtn;
    }

    private Map getStdMap(final DB2UDB db2, final String hrClass) {
        final Map stdMap = new TreeMap();
        PreparedStatement psClub = null;
        ResultSet rsClub = null;
        PreparedStatement psCommittee = null;
        ResultSet rsCommittee = null;
        try {

            //クラブ設定
            final String clubSql = clubSql(hrClass);
            log.debug(" sql =" + clubSql);
            psClub = db2.prepareStatement(clubSql);
            rsClub = psClub.executeQuery();

            while (rsClub.next()) {
                final String schregNo    = rsClub.getString("SCHREGNO");
                final String hrName      = rsClub.getString("HR_NAME");
                final String grade       = rsClub.getString("GRADE");
                final String setHrClass  = rsClub.getString("HR_CLASS");
                final String attendNo    = rsClub.getString("ATTENDNO");
                final String name        = rsClub.getString("NAME");
                final String clubcd      = rsClub.getString("CLUBCD");
                final String clubname    = rsClub.getString("CLUBNAME");
                final String sdate       = rsClub.getString("SDATE");
                final String edate       = null != rsClub.getString("EDATE") ? "\uFF5E" + rsClub.getString("EDATE") : "";
                final String postname    = rsClub.getString("POSTNAME");
                final String remark      = rsClub.getString("REMARK");
                final String guardName   = null != rsClub.getString("GUARD_NAME") ? rsClub.getString("GUARD_NAME") : "";
                final String guardAddr1  = null != rsClub.getString("GUARD_ADDR1") ? rsClub.getString("GUARD_ADDR1") : "";
                final String guardAddr2  = null != rsClub.getString("GUARD_ADDR2") ? rsClub.getString("GUARD_ADDR2") : "";
                final String guardTelno  = null != rsClub.getString("GUARD_TELNO") ? rsClub.getString("GUARD_TELNO") : "";
                Student student = null;
                final String stdKey = attendNo + schregNo;
                if (!stdMap.containsKey(stdKey)) {
                    student = new Student(schregNo, grade, setHrClass, hrName, attendNo, name, guardName, guardAddr1, guardAddr2, guardTelno);
                    stdMap.put(stdKey, student);
                }
                student = (Student) stdMap.get(stdKey);

                final Club club = new Club(clubcd, clubname, sdate, edate, postname, remark);
                student._clubList.add(club);
            }

            //委員会設定
            final String committeeSql = committeeSql(hrClass);
            log.debug(" sql =" + committeeSql);
            psCommittee = db2.prepareStatement(committeeSql);
            rsCommittee = psCommittee.executeQuery();

            while (rsCommittee.next()) {
                final String schregNo       = rsCommittee.getString("SCHREGNO");
                final String hrName         = rsCommittee.getString("HR_NAME");
                final String grade          = rsCommittee.getString("GRADE");
                final String setHrClass     = rsCommittee.getString("HR_CLASS");
                final String attendNo       = rsCommittee.getString("ATTENDNO");
                final String name           = rsCommittee.getString("NAME");
                final String committeeFlg   = rsCommittee.getString("COMMITTEE_FLG");
                final String committeeCd    = rsCommittee.getString("COMMITTEECD");
                final String committeeName  = null != rsCommittee.getString("COMMITTEENAME") ? rsCommittee.getString("COMMITTEENAME") : "";
                final String chargeName     = null != rsCommittee.getString("CHARGENAME") ? " " + rsCommittee.getString("CHARGENAME") : "";
                final String executiveCd    = rsCommittee.getString("EXECUTIVECD");
                final String roleName       = null != rsCommittee.getString("ROLE_NAME") ? " " + rsCommittee.getString("ROLE_NAME") : "";
                final String semester       = rsCommittee.getString("SEMESTER");
                final String guardName      = null != rsCommittee.getString("GUARD_NAME") ? rsCommittee.getString("GUARD_NAME") : "";
                final String guardAddr1     = null != rsCommittee.getString("GUARD_ADDR1") ? rsCommittee.getString("GUARD_ADDR1") : "";
                final String guardAddr2     = null != rsCommittee.getString("GUARD_ADDR2") ? rsCommittee.getString("GUARD_ADDR2") : "";
                final String guardTelno     = null != rsCommittee.getString("GUARD_TELNO") ? rsCommittee.getString("GUARD_TELNO") : "";
                final String semesterName   = null != rsCommittee.getString("SEMESTERNAME") ? rsCommittee.getString("SEMESTERNAME") : "";

                Student student = null;
                final String stdKey = attendNo + schregNo;
                if (!stdMap.containsKey(stdKey)) {
                    student = new Student(schregNo, grade, setHrClass, hrName, attendNo, name, guardName, guardAddr1, guardAddr2, guardTelno);
                    stdMap.put(stdKey, student);
                }
                student = (Student) stdMap.get(stdKey);

                final Committee committee = new Committee(committeeFlg, committeeCd, committeeName, chargeName, executiveCd, roleName, semester, semesterName);
                student._committeeList.add(committee);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, psClub, rsClub);
            DbUtils.closeQuietly(null, psCommittee, rsCommittee);
            db2.commit();
        }
        return stdMap;
    }

    private String clubSql(final String hrClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    T1.SCHREGNO, ");
        stb.append("    T6.HR_NAME, ");
        stb.append("    T1.GRADE, ");
        stb.append("    T1.HR_CLASS, ");
        stb.append("    T1.ATTENDNO, ");
        stb.append("    T2.NAME, ");
        stb.append("    T3.CLUBCD, ");
        stb.append("    T4.CLUBNAME, ");
        stb.append("    T3.SDATE, ");
        stb.append("    T3.EDATE, ");
        stb.append("    VALUE(T5.NAME1,' ') AS POSTNAME, ");
        stb.append("    VALUE(T3.REMARK,' ') AS REMARK, ");
        stb.append("    T7.GUARD_NAME, ");
        stb.append("    T7.GUARD_ADDR1, ");
        stb.append("    T7.GUARD_ADDR2, ");
        stb.append("    T7.GUARD_TELNO ");
        stb.append("FROM ");
        stb.append("    SCHREG_REGD_HDAT T6 ");
        stb.append("    INNER JOIN SCHREG_REGD_DAT T1 ON ");
        stb.append("        T1.YEAR = T6.YEAR AND ");
        stb.append("        T1.SEMESTER = T6.SEMESTER AND ");
        stb.append("        T1.GRADE || T1.HR_CLASS = T6.GRADE || T6.HR_CLASS ");
        stb.append("    INNER JOIN SCHREG_BASE_MST T2 ON ");
        stb.append("        T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("    INNER JOIN SCHREG_CLUB_HIST_DAT T3 ON ");
        stb.append("        T3.SCHREGNO = T1.SCHREGNO ");
        if ("1".equals(_useClubMultiSchoolKind)) {
            stb.append("   AND T3.SCHOOLCD = '" + _SCHOOLCD + "' ");
            stb.append("   AND T3.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
        } else if ("1".equals(_use_prg_schoolkind)) {
            if (!StringUtils.isBlank(_selectSchoolKind)) {
                stb.append("        AND T3.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("        AND T3.SCHOOL_KIND IN " + _selectSchoolKindSql + "  ");
            }
        } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND) && !StringUtils.isBlank(_SCHOOLCD)) {
            stb.append("   AND T3.SCHOOLCD = '" + _SCHOOLCD + "' ");
            stb.append("   AND T3.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
        }
        stb.append("    INNER JOIN CLUB_MST T4 ON ");
        stb.append("        T4.CLUBCD = T3.CLUBCD ");
        if ("1".equals(_useClubMultiSchoolKind)) {
            stb.append("   AND T4.SCHOOLCD = T3.SCHOOLCD ");
            stb.append("   AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ");
        } else if ("1".equals(_useSchool_KindField)) {
            stb.append("   AND T4.SCHOOLCD = T3.SCHOOLCD ");
            stb.append("   AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ");
        }
        stb.append("    LEFT JOIN NAME_MST T5 ON ");
        stb.append("        T5.NAMECD1 = 'J001' AND ");
        stb.append("        T5.NAMECD2 = T3.EXECUTIVECD ");
        stb.append("    LEFT JOIN GUARDIAN_DAT T7 ON ");
        stb.append("        T7.SCHREGNO = T1.SCHREGNO ");
        stb.append("WHERE ");
        stb.append("    T1.YEAR = '" + _param._year + "' AND ");
        stb.append("    T1.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("    T1.GRADE || T1.HR_CLASS = '" + hrClass + "' ");
        stb.append("ORDER BY ");
        stb.append("    T1.GRADE, ");
        stb.append("    T1.HR_CLASS, ");
        stb.append("    T1.ATTENDNO, ");
        stb.append("    T3.CLUBCD ");
        return stb.toString();
    }

    private String committeeSql (final String hrClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT DISTINCT ");
        stb.append("    T1.SCHREGNO, ");
        stb.append("    T6.HR_NAME, ");
        stb.append("    T1.GRADE, ");
        stb.append("    T1.HR_CLASS, ");
        stb.append("    T1.ATTENDNO, ");
        stb.append("    T2.NAME, ");
        stb.append("    T3.COMMITTEE_FLG, ");
        stb.append("    T3.COMMITTEECD, ");
        stb.append("    T4.COMMITTEENAME, ");
        stb.append("    T3.CHARGENAME, ");
        stb.append("    T3.EXECUTIVECD, ");
        stb.append("    NJ002.NAME1 AS ROLE_NAME, ");
        stb.append("    T3.SEMESTER, ");
        stb.append("    T7.GUARD_NAME, ");
        stb.append("    T7.GUARD_ADDR1, ");
        stb.append("    T7.GUARD_ADDR2, ");
        stb.append("    T7.GUARD_TELNO, ");
        stb.append("    NJ004.NAME1 AS SEMESTERNAME ");
        stb.append("FROM ");
        stb.append("    SCHREG_REGD_HDAT T6 ");
        stb.append("    INNER JOIN SCHREG_REGD_DAT T1 ON ");
        stb.append("        T1.YEAR = T6.YEAR AND ");
        stb.append("        T1.SEMESTER = T6.SEMESTER AND ");
        stb.append("        T1.GRADE || T1.HR_CLASS = T6.GRADE || T6.HR_CLASS ");
        stb.append("    INNER JOIN SCHREG_BASE_MST T2 ON ");
        stb.append("        T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("    INNER JOIN SCHREG_COMMITTEE_HIST_DAT T3 ON ");
        stb.append("        T3.YEAR = T1.YEAR ");
        stb.append("        AND (T3.SEMESTER = '9' OR T3.SEMESTER <= T1.SEMESTER) ");
        stb.append("        AND T3.SCHREGNO = T1.SCHREGNO ");
        if ("1".equals(_use_prg_schoolkind)) {
            if (!StringUtils.isBlank(_selectSchoolKind)) {
                stb.append("        AND T3.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("        AND T3.SCHOOL_KIND IN " + _selectSchoolKindSql + "  ");
            }
        } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND) && !StringUtils.isBlank(_SCHOOLCD)) {
            stb.append("   AND T3.SCHOOLCD = '" + _SCHOOLCD + "' ");
            stb.append("   AND T3.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
        }
        stb.append("    INNER JOIN COMMITTEE_MST T4 ON ");
        stb.append("        T4.COMMITTEE_FLG = T3.COMMITTEE_FLG ");
        stb.append("        AND T4.COMMITTEECD = T3.COMMITTEECD ");
        if ("1".equals(_useSchool_KindField)) {
            stb.append("   AND T4.SCHOOLCD = T3.SCHOOLCD ");
            stb.append("   AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ");
        }
        stb.append("    LEFT JOIN NAME_MST NJ002 ON ");
        stb.append("        NJ002.NAMECD1 = 'J002' AND ");
        stb.append("        NJ002.NAMECD2 = T3.EXECUTIVECD ");
        stb.append("    LEFT JOIN GUARDIAN_DAT T7 ON ");
        stb.append("        T7.SCHREGNO = T1.SCHREGNO ");
        stb.append("    LEFT JOIN NAME_MST NJ004 ON NJ004.NAMECD1 = 'J004' ");
        stb.append("         AND T3.SEMESTER = NJ004.NAMECD2 ");
        stb.append("WHERE ");
        stb.append("    T1.YEAR = '" + _param._year + "' AND ");
        stb.append("    T1.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("    T1.GRADE || T1.HR_CLASS = '" + hrClass + "' ");
        stb.append("ORDER BY ");
        stb.append("    T1.GRADE, ");
        stb.append("    T1.HR_CLASS, ");
        stb.append("    T1.ATTENDNO, ");
        stb.append("    T3.COMMITTEE_FLG, ");
        stb.append("    T3.COMMITTEECD, ");
        stb.append("    T3.EXECUTIVECD ");
        return stb.toString();

    }

    private class Student {
        private final String _schregNo;
        private final String _grade;
        private final String _hrClass;
        private final String _hrName;
        private final String _attendNo;
        private final String _name;
        private final Guardian _guardian;
        private final List _clubList;
        private final List _committeeList;
        public Student(
                final String schregNo,
                final String grade,
                final String hrClass,
                final String hrName,
                final String attendNo,
                final String name,
                final String guardName,
                final String guardAddr1,
                final String guardAddr2,
                final String guardTelno
        ) {
            _schregNo = schregNo;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _attendNo = attendNo;
            _name = name;
            _guardian = new Guardian(guardName, guardAddr1, guardAddr2, guardTelno);
            _clubList = new ArrayList();
            _committeeList = new ArrayList();
        }
        public List getCommitteeStringList() {
            final List rtn = new ArrayList();
            final List addList = new ArrayList();
            for (final Iterator it = _committeeList.iterator(); it.hasNext();) {
                final Committee c = (Committee) it.next();
                boolean hasAnotherSemester = false;
                boolean addAnother = false;
                for (final Iterator bit = _committeeList.iterator(); bit.hasNext();) {
                    final Committee bc = (Committee) bit.next();
                    if (c == bc) {
                        continue;
                    }
                    if (null != bc._committeeFlg && bc._committeeFlg.equals(c._committeeFlg) && null != bc._committeeCd && bc._committeeCd.equals(c._committeeCd)) {
                        hasAnotherSemester = true;
                        for (final Iterator yit = addList.iterator(); yit.hasNext();) {
                            final Committee yc = (Committee) yit.next();
                            if (null != c._committeeFlg && c._committeeFlg.equals(yc._committeeFlg) && null != c._committeeCd && c._committeeCd.equals(yc._committeeCd)) {
                                addAnother = true;
                                break;
                            }
                        }
                        break;
                    }
                }
                if (hasAnotherSemester) {
                    if (!addAnother) {
                        rtn.add(c._committeeName + (StringUtils.isBlank(c._chargeName + c._roleName) ? "" : "(" + c._chargeName + c._roleName + ")"));
                    }
                } else {
                    rtn.add(c._semesterName + "(" + c._committeeName + c._chargeName + c._roleName + ")");
                }
                addList.add(c);
            }
            return rtn;
        }
    }

    private static List toList(final String s, final int keta) {
        final String[] token = KNJ_EditEdit.get_token(s, keta, 99);
        if (null == token) {
            return Collections.EMPTY_LIST;
        }
        final List rtn = new ArrayList();
        for (int i = 0; i < token.length; i++) {
            if (!StringUtils.isBlank(token[i])) {
                rtn.add(token[i]);
            }
        }
        return rtn;
    }

    private class Guardian {
        private final List _guardName;
        private final List _guardAddr1;
        private final List _guardTelno;
        public Guardian(
                final String guardName,
                final String guardAddr1,
                final String guardAddr2,
                final String guardTelno
        ) {
            _guardName = toList(guardName, 20);
            _guardAddr1 = toList(guardAddr1 + guardAddr2, 30);
            _guardTelno = toList(guardTelno, 13);
        }
    }

    private class Club {
        private final String _clubcd;
        private final String _clubname;
        private final String _sdate;
        private final String _edate;
        private final String _postname;
        private final String _remark;
        public Club(
                final String clubcd,
                final String clubname,
                final String sdate,
                final String edate,
                final String postname,
                final String remark
        ) {
            _clubcd     = clubcd;
            _clubname   = clubname;
            _sdate      = sdate.replace('-', '/');
            _edate      = edate.replace('-', '/');
            _postname   = postname;
            _remark     = remark;
        }
    }

    private class Committee {
        final String _committeeFlg;
        final String _committeeCd;
        final String _committeeName;
        final String _chargeName;
        final String _executiveCd;
        final String _roleName;
        final String _semester;
        final String _semesterName;
        public Committee(
                final String committeeFlg,
                final String committeeCd,
                final String committeeName,
                final String chargeName,
                final String executiveCd,
                final String roleName,
                final String semester,
                final String semesterName
        ) {
            _committeeFlg   = committeeFlg;
            _committeeCd    = committeeCd;
            _committeeName  = committeeName;
            _chargeName     = chargeName;
            _executiveCd    = executiveCd;
            _roleName       = roleName;
            _semester       = semester;
            _semesterName   = semesterName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 62731 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _ctrlDate;
        private final String _hogosya;
        private final String _useAddrField2;
        private final String[] _hrClasscd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _hogosya = request.getParameter("hogosya");
            _useAddrField2 = request.getParameter("useAddrField2");
            _hrClasscd = request.getParameterValues("CLASS_SELECTED");
        }

    }
}

// eof

