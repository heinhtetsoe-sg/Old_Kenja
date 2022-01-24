/*
 * $Id: ec6153847bab9b851b7fbc7946065b6eaaecb17d $
 *
 * 作成日: 2017/05/01
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

public class KNJA227 {

    private static final Log log = LogFactory.getLog(KNJA227.class);

    private boolean _hasData;

    private Param _param;

    private final String SORT_FS_CD = "1";
    private final String SUNDAI_CD_FUTUU = "3008112";
    private final String SUNDAI_CD_SPORT = "3008113";
    private final String SCHOLAR_TOKUBETU = "1";
    private final String SCHOLAR_IPPAN = "2";

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
        final String form = "KNJA227.frm";
        final String nendo = KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度";
        final String title = "塾別生徒一覧";

        for (int i = 0; i < _param._prischoolcds.length; i++) {
            final String[] _cds = StringUtils.split(_param._prischoolcds[i], "-");
            final String priCd = _cds[0];
            final String priClsCd = _cds[1];

            final List regdListJ = getList(db2, sqlRegd(priCd,priClsCd,"J"));
            final List regdListH = getList(db2, sqlRegd(priCd,priClsCd,"H"));
            setClubCommitteeInfo(db2, regdListJ, "J");
            setClubCommitteeInfo(db2, regdListH, "H");
            final List eventListJ = getList(db2, sqlEvent(priCd,priClsCd,"J"));
            final List eventListH = getList(db2, sqlEvent(priCd,priClsCd,"H"));
            if (regdListJ.size() == 0 && regdListH.size() == 0 && eventListJ.size() == 0 && eventListH.size() == 0) continue;

            final List regdListJPageList = getPageList(regdListJ, 12);
            final List regdListHPageList = getPageList(regdListH, 12);
            final List eventListJPageList = getPageList(eventListJ, 15);
            final List eventListHPageList = getPageList(eventListH, 15);

            final int maxPage = Math.max(regdListJPageList.size(), Math.max(regdListHPageList.size(), Math.max(eventListJPageList.size(), Math.max(eventListHPageList.size(), 1))));

            final List priList = getList(db2, sqlPri(priCd,priClsCd));
            for (int dataPageIdx = 0; dataPageIdx < maxPage; dataPageIdx++) {

                svf.VrSetForm(form, 1);
                svf.VrsOut("TITLE", nendo + "　" + title);

                for (int pi = 0; pi < priList.size(); pi++) {
                    final Map pMap = (Map) priList.get(pi);

                    svf.VrsOut("PRISCHOOL_NAME", "0000000".equals(priClsCd) ? getString(pMap, "PRISCHOOL_NAME") : getString(pMap, "PRISCHOOL_NAME") + getString(pMap, "PRISCHOOL_CLASS_NAME"));
                    svf.VrsOut("PRISCHOOL_CD", "0000000".equals(priClsCd) ? getString(pMap, "PRISCHOOLCD") : getString(pMap, "PRISCHOOLCD") + "-" + getString(pMap, "PRISCHOOL_CLASS_CD"));
                    svf.VrsOut("PRISCHOOL_PRINCNAME", getString(pMap, "PRINCNAME"));
                    svf.VrsOut("PRISCHOOL_CHARGE", "");
                    svf.VrsOut("NEAR_STATION", "");
                    svf.VrsOut("PRISCHOOL_TELNO", getString(pMap, "PRISCHOOL_TELNO"));
                    svf.VrsOut("PRISCHOOL_FAXNO", getString(pMap, "PRISCHOOL_FAXNO"));
                    final String setAddr1 = getString(pMap, "PRISCHOOL_ADDR1");
                    final String setAddr2 = getString(pMap, "PRISCHOOL_ADDR2");
                    if (getMS932Bytecount(setAddr2) == 0) {
                        svf.VrsOut("PRISCHOOL_ADDR1" + (getMS932Bytecount(setAddr1) <= 16 ? "_1" : "_2"), setAddr1);
                    } else if (getMS932Bytecount(setAddr1) <= 40 && getMS932Bytecount(setAddr2) <= 40) {
                        svf.VrsOut("PRISCHOOL_ADDR2_1", setAddr1);
                        svf.VrsOut("PRISCHOOL_ADDR2_2", setAddr2);
                    } else {
                        svf.VrsOut("PRISCHOOL_ADDR3_1", setAddr1);
                        svf.VrsOut("PRISCHOOL_ADDR3_2", setAddr2);
                    }
                }

                if (dataPageIdx < regdListJPageList.size()) {
                    final List regdListJ1 = (List) regdListJPageList.get(dataPageIdx);
                    for (int ri = 0; ri < regdListJ1.size(); ri++) {
                        final Map rMap = (Map) regdListJ1.get(ri);

                        int line = ri + 1;
                        final String setAttendno = String.valueOf(Integer.parseInt(getString(rMap, "ATTENDNO")));
                        svf.VrsOutn("HR_NAME1", line, getString(rMap, "HR_NAME") + "-" + setAttendno);
                        final String setName = getString(rMap, "NAME");
                        svf.VrsOutn("J_NAME1" + (getMS932Bytecount(setName) <= 16 ? "_1" : "_2"), line, setName);
                        final String commastr = (!"".equals(getString(rMap, "CLUBNAME")) && !"".equals(getString(rMap, "COMMITTEENAME")) ? "、" : "");
                        final String setEtc = getString(rMap, "CLUBNAME") + commastr + getString(rMap, "COMMITTEENAME");
                        svf.VrsOutn("ETC1" + (getMS932Bytecount(setEtc) <= 22 ? "" : (getMS932Bytecount(setEtc) <= 30) ? "_2" :"_3_1"), line, setEtc);
                    }
                }
                if (dataPageIdx < regdListHPageList.size()) {
                    final List regdListH1 = (List) regdListHPageList.get(dataPageIdx);
                    for (int ri = 0; ri < regdListH1.size(); ri++) {
                        final Map rMap = (Map) regdListH1.get(ri);

                        int line = ri + 1;
                        final String setAttendno = String.valueOf(Integer.parseInt(getString(rMap, "ATTENDNO")));
                        svf.VrsOutn("HR_NAME2", line, getString(rMap, "HR_NAME") + "-" + setAttendno);
                        final String setName = getString(rMap, "NAME");
                        svf.VrsOutn("H_NAME1" + (getMS932Bytecount(setName) <= 16 ? "_1" : "_2"), line, setName);
                        final String setCourseName = getString(rMap, "COURSECODENAME");
                        svf.VrsOutn("COURSE_NAME" + (getMS932Bytecount(setCourseName) <= 10 ? "1" : getMS932Bytecount(setCourseName) <= 12 ? "2" : "3"), line, setCourseName);
                        final String commastr = (!"".equals(getString(rMap, "CLUBNAME")) && !"".equals(getString(rMap, "COMMITTEENAME")) ? "、" : "");
                        final String setEtc = getString(rMap, "CLUBNAME") + commastr + getString(rMap, "COMMITTEENAME");
                        svf.VrsOutn("ETC2" + (getMS932Bytecount(setEtc) <= 22 ? "" : (getMS932Bytecount(setEtc) <= 30) ? "_2" :"_3_1"), line, setEtc);
                    }
                }

                if (dataPageIdx < eventListJPageList.size()) {
                    final List eventListJ1 = (List) eventListJPageList.get(dataPageIdx);
                    for (int ei = 0; ei < eventListJ1.size(); ei++) {
                        final Map eMap = (Map) eventListJ1.get(ei);

                        int line = ei + 1;
                        final String setGrade = getGradeName(getString(eMap, "GRADE"));
                        svf.VrsOutn("J_GRADE2", line, setGrade);
                        final String setName = getString(eMap, "NAME");
                        svf.VrsOutn("J_NAME2" + (getMS932Bytecount(setName) <= 16 ? "_1" : "_2"), line, setName);
                        final String setschName = getString(eMap, "SCHOOLNAME");
                        svf.VrsOutn("J_SCHOOL_NAME" + (getMS932Bytecount(setschName) <= 10 ? "1" : "2"), line, setschName);
                        final String setDate = getString(eMap, "TOUROKU_DATE");
                        svf.VrsOutn("J_DATE", line, StringUtils.defaultString(setDate).replace('-', '/'));
                        final String setEventName = getString(eMap, "EVENT_NAME");
                        svf.VrsOutn("J_EVENT_NAME" + (getMS932Bytecount(setEventName) <= 22 ? "1" : getMS932Bytecount(setEventName) <= 30 ? "2" : "3"), line, setEventName);
                    }
                }
                if (dataPageIdx < eventListHPageList.size()) {
                    final List eventListH1 = (List) eventListHPageList.get(dataPageIdx);
                    for (int ei = 0; ei < eventListH1.size(); ei++) {
                        final Map eMap = (Map) eventListH1.get(ei);

                        int line = ei + 1;
                        final String setGrade = getGradeName(getString(eMap, "GRADE"));
                        svf.VrsOutn("H_GRADE2", line, setGrade);
                        final String setName = getString(eMap, "NAME");
                        svf.VrsOutn("H_NAME2" + (getMS932Bytecount(setName) <= 16 ? "_1" : "_2"), line, setName);
                        final String setschName = getString(eMap, "SCHOOLNAME");
                        svf.VrsOutn("H_SCHOOL_NAME" + (getMS932Bytecount(setschName) <= 10 ? "1" : "2"), line, setschName);
                        final String setDate = getString(eMap, "TOUROKU_DATE");
                        svf.VrsOutn("H_DATE", line, StringUtils.defaultString(setDate).replace('-', '/'));
                        final String setEventName = getString(eMap, "EVENT_NAME");
                        svf.VrsOutn("H_EVENT_NAME" + (getMS932Bytecount(setEventName) <= 32 ? "1" : getMS932Bytecount(setEventName) <= 46 ? "2" : "3"), line, setEventName);
                    }
                }

                svf.VrEndPage();
                _hasData = true;

            }
        }
    }

    private String getGradeName(final String grade) {
        String name = null;
        if ("01".equals(grade)) {
            name = "小１";
        } else if ("02".equals(grade)) {
            name = "小２";
        } else if ("03".equals(grade)) {
            name = "小３";
        } else if ("04".equals(grade)) {
            name = "小４";
        } else if ("05".equals(grade)) {
            name = "小５";
        } else if ("06".equals(grade)) {
            name = "小６";
        } else if ("07".equals(grade)) {
            name = "中１";
        } else if ("08".equals(grade)) {
            name = "中２";
        } else if ("09".equals(grade)) {
            name = "中３";
        }
        return name;
    }

    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private static String getString(final Map m, final String field) {
        if (null == m || m.isEmpty()) {
            return null;
        }
        if (!m.containsKey(field)) {
            return "";
//            throw new IllegalArgumentException("not defined: " + field + " in " + m.keySet());
        }
        return (String) m.get(field);
    }

    private static int getMS932Bytecount(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }

    private void setClubCommitteeInfo(final DB2UDB db2, List regdList, final String schoolKind) {

        for (final Iterator it = regdList.iterator(); it.hasNext();) {
            Map editobj = (Map)it.next();
            String clubstr = getClubStr(db2, (String)editobj.get("SCHREGNO"), schoolKind);
            String committeestr = getCommitteeStr(db2, (String)editobj.get("SCHREGNO"), schoolKind);
            if (!"".equals(clubstr)) {
                editobj.put("CLUBNAME", clubstr);
            }
            if (!"".equals(committeestr)) {
                editobj.put("COMMITTEENAME", committeestr);
            }
        }
    }
    private String getClubStr (final DB2UDB db2, String schregno, final String schoolKind) {
        String retstr = "";
        String sql = sqlClubStr(schregno, schoolKind);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (!"".equals(rs.getString("CLUBNAME"))) {
                    if (!"".equals(retstr)) retstr += "、";
                    retstr += rs.getString("CLUBNAME");
                }
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return retstr;
    }

    private String getCommitteeStr (final DB2UDB db2, String schregno, final String schoolKind) {
        String retstr = "";
        String sql = sqlCommitteeStr(schregno, schoolKind);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (!"".equals(rs.getString("COMMITTEENAME"))) {
                    if (!"".equals(retstr)) retstr += "、";
                    retstr += rs.getString("COMMITTEENAME");
                }
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retstr;
    }

    private List getList(final DB2UDB db2, final String sql) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                final Map m = new HashMap();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    m.put(meta.getColumnLabel(i), rs.getString(meta.getColumnLabel(i)));
                }
                list.add(m);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private String sqlPri(final String prischoolcd, final String prischoolClassCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     P1.PRISCHOOLCD, ");
        stb.append("     P2.PRISCHOOL_CLASS_CD, ");
        stb.append("     P1.PRISCHOOL_NAME, ");
        stb.append("     VALUE(P2.PRISCHOOL_NAME,'') AS PRISCHOOL_CLASS_NAME, ");
        stb.append("     P1.PRINCNAME, ");
        stb.append("     P1.PRISCHOOL_ADDR1, ");
        stb.append("     P1.PRISCHOOL_ADDR2, ");
        stb.append("     P1.PRISCHOOL_TELNO, ");
        stb.append("     P1.PRISCHOOL_FAXNO ");
        stb.append(" FROM ");
        stb.append("     PRISCHOOL_MST P1 ");
        stb.append("     LEFT JOIN PRISCHOOL_CLASS_MST P2 ");
        stb.append("          ON P2.PRISCHOOLCD = P1.PRISCHOOLCD ");
        stb.append("         AND P2.PRISCHOOL_CLASS_CD = '" + prischoolClassCd + "' ");
        stb.append(" WHERE ");
        stb.append("     P1.PRISCHOOLCD = '" + prischoolcd + "' ");
        return stb.toString();
    }

    private String sqlRegd(final String prischoolcd, final String prischoolClassCd, final String schoolKind) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T4.HR_NAME, ");
        stb.append("     T4.HR_NAMEABBV, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.COURSECODE, ");
        stb.append("     T5.COURSECODENAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T4 ON T4.YEAR = T1.YEAR AND T4.SEMESTER = T1.SEMESTER ");
        stb.append("         AND T4.GRADE = T1.GRADE AND T4.HR_CLASS = T1.HR_CLASS ");
        stb.append("     INNER JOIN COURSECODE_MST T5 ON T5.COURSECODE = T1.COURSECODE ");
        stb.append("     LEFT JOIN SCHREG_BASE_DETAIL_MST B1 ON B1.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND B1.BASE_SEQ = '010' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T2.PRISCHOOLCD = '" + prischoolcd + "' ");
        stb.append("     AND VALUE(B1.BASE_REMARK1,'0000000') = '" + prischoolClassCd + "' ");
        stb.append("     AND T3.SCHOOL_KIND = '" + schoolKind + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    private String sqlEvent(final String prischoolcd, final String prischoolClassCd, final String schoolKind) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     R1.RECRUIT_NO, ");
        stb.append("     R1.GRADE, ");
        stb.append("     R1.NAME, ");
        stb.append("     E1.TOUROKU_DATE, ");
        stb.append("     FSM.FINSCHOOL_NAME_ABBV AS SCHOOLNAME, ");
        stb.append("     CASE WHEN E2.EVENT_NAME IS NULL THEN '' WHEN L1.NAME1 IS NULL THEN E2.EVENT_NAME ELSE E2.EVENT_NAME || L1.NAME1 END AS EVENT_NAME ");
        stb.append(" FROM ");
        stb.append("     RECRUIT_DAT R1 ");
        stb.append("     LEFT JOIN RECRUIT_EVENT_DAT E1 ON E1.YEAR = R1.YEAR ");
        stb.append("         AND E1.RECRUIT_NO = R1.RECRUIT_NO ");
        stb.append("     LEFT JOIN RECRUIT_EVENT_YMST E2 ON E2.YEAR = E1.YEAR ");
        stb.append("         AND E2.EVENT_CLASS_CD = E1.EVENT_CLASS_CD ");
        stb.append("         AND E2.EVENT_CD = E1.EVENT_CD ");
        stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'L402' ");
        stb.append("         AND E1.STATE_CD = L1.NAMECD2 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FSM ON FSM.FINSCHOOLCD = R1.FINSCHOOLCD ");
        stb.append(" WHERE ");
        stb.append("     R1.YEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND R1.PRISCHOOLCD = '" + prischoolcd + "' ");
        stb.append("     AND VALUE(R1.PRISCHOOL_CLASS_CD,'0000000') = '" + prischoolClassCd + "' ");
        stb.append("     AND R1.SCHOOL_KIND = '" + schoolKind + "' ");
        stb.append(" ORDER BY ");
        stb.append("     R1.NAME, ");
        stb.append("     E1.TOUROKU_DATE ");
        return stb.toString();
    }

    private String sqlClubStr(final String schregno, final String schoolKind) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("  CASE WHEN CLUB.CLUBNAME IS NULL THEN '' ELSE CLUB.CLUBNAME END AS CLUBNAME ");
        stb.append(" FROM  SCHREG_CLUB_HIST_DAT T3 ");
        stb.append("  LEFT JOIN SCHREG_REGD_DAT T2 ");
        stb.append("    ON T3.SCHREGNO = T2.SCHREGNO ");
        stb.append("       AND T2.YEAR = '" + _param._year + "' ");
        stb.append("       AND T2.SEMESTER IN ('" + _param._semester + "', '9') ");
        stb.append("  LEFT JOIN SCHREG_REGD_GDAT T4 ");
        stb.append("    ON T2.YEAR = T4.YEAR ");
        stb.append("    AND T2.GRADE = T4.GRADE ");
        stb.append("  LEFT JOIN CLUB_MST CLUB ");
        stb.append("    ON T3.CLUBCD = CLUB.CLUBCD ");
        if ("1".equals(_param._useSchool_KindField)) {
            stb.append("    AND CLUB.SCHOOL_KIND = T3.SCHOOL_KIND ");
        }
        stb.append("  LEFT JOIN NAME_MST J001 ON J001.NAMECD1 = 'J001' ");
        stb.append("    AND T3.EXECUTIVECD = J001.NAMECD2 ");
        stb.append(" WHERE  ");
        stb.append("   T3.SCHREGNO = '" + schregno + "' ");
        stb.append("   AND T2.SCHREGNO IS NOT NULL ");
        stb.append("   AND (T3.EDATE IS NULL OR '" + _param._loginDate + "' < T3.EDATE ) ");
        stb.append("   AND T3.SDATE < '" + _param._loginDate + "' ");
        if ("1".equals(_param._useSchool_KindField)) {
            stb.append("   AND T3.SCHOOL_KIND = T4.SCHOOL_KIND ");
        }
        stb.append("   AND T4.SCHOOL_KIND = '" + schoolKind + "' ");
        stb.append(" ORDER BY ");
        stb.append("   T3.SCHREGNO ");
        return stb.toString();
    }

    private String sqlCommitteeStr(final String schregno, final String schoolKind) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("  CMST.COMMITTEENAME AS COMMITTEENAME ");
        stb.append(" FROM ");
        stb.append("  SCHREG_COMMITTEE_HIST_DAT T1 ");
        stb.append("  LEFT JOIN COMMITTEE_MST CMST ");
        stb.append("    ON  T1.COMMITTEE_FLG = CMST.COMMITTEE_FLG ");
        if ("1".equals(_param._useSchool_KindField)) {
            stb.append("    AND T1.SCHOOL_KIND = CMST.SCHOOL_KIND ");
        }
        stb.append("    AND T1.COMMITTEECD = CMST.COMMITTEECD ");
        stb.append("  LEFT JOIN SCHREG_REGD_DAT T2 ");
        stb.append("    ON T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("    AND T1.YEAR = T2.YEAR ");
        stb.append("    AND T1.SEMESTER = T2.SEMESTER ");
        stb.append("    AND T1.GRADE = T2.GRADE ");
        stb.append("  LEFT JOIN SCHREG_REGD_GDAT T4 ");
        stb.append("    ON T1.YEAR = T4.YEAR ");
        stb.append("    AND T1.GRADE = T4.GRADE ");
        if ("1".equals(_param._useSchool_KindField)) {
            stb.append("    AND T1.SCHOOL_KIND = T4.SCHOOL_KIND ");
        }
        stb.append("  LEFT JOIN NAME_MST N1 ");
        stb.append("    ON N1.NAMECD2 = T1.COMMITTEE_FLG ");
        stb.append("    AND N1.NAMECD1 = 'J003' ");
        stb.append("  LEFT JOIN NAME_MST N2 ");
        stb.append("    ON N2.NAMECD2 = T1.EXECUTIVECD ");
        stb.append("    AND N2.NAMECD1 = 'J002' ");
        stb.append(" WHERE  ");
        stb.append("  T1.SCHREGNO = '" + schregno + "' ");
        stb.append("  AND T2.SCHREGNO IS NOT NULL ");
        stb.append("  AND T1.YEAR = '" + _param._year + "' ");
        stb.append("  AND T1.SEMESTER IN ('" + _param._semester + "', '9') ");
        stb.append("  AND T1.SEQ = (SELECT MAX(SEQ) FROM SCHREG_COMMITTEE_HIST_DAT TW1 WHERE TW1.YEAR = T1.YEAR AND TW1.SEMESTER = T1.SEMESTER AND TW1.SCHREGNO = T1.SCHREGNO) ");
        stb.append("  AND T4.SCHOOL_KIND = '" + schoolKind + "' ");
        stb.append(" order by ");
        stb.append("  T1.SCHREGNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 61045 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _year;
        private final String _semester;
        private final String _loginDate;
        private final String[] _prischoolcds;
        private final String _entexamyear;
        private final String _useSchool_KindField;
        private final String _SCHOOLCD;
        private final String _SCHOOL_KIND;
        final String use_prg_schoolkind;
        final String selectSchoolKind;
        String selectSchoolKindSql = null;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _loginDate = request.getParameter("CTRL_DATE");
            _prischoolcds = request.getParameterValues("SCHOOL_SELECTED");
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD= request.getParameter("SCHOOLCD");
            _SCHOOL_KIND = request.getParameter("SCHOOL_KIND");
            use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            selectSchoolKind = request.getParameter("selectSchoolKind");
            if (!StringUtils.isBlank(selectSchoolKind)) {
                final StringBuffer sql = new StringBuffer("('");
                final String[] split = StringUtils.split(selectSchoolKind, ":");
                for (int i = 0; i < split.length; i++) {
                    sql.append(split[i]);
                    if (i < split.length - 1) {
                        sql.append("','");
                    }
                }
                selectSchoolKindSql = sql.append("')").toString();
            }
        }
    }
}

// eof

