/*
 * $Id: 9950f2a3dc5a409be40ae24e6429b145f47630c5 $
 *
 * 作成日: 2016/08/03
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJJ;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJJ142 {

    private static final Log log = LogFactory.getLog(KNJJ142.class);

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
        for (Iterator iterator21 = _param._committeeList.iterator(); iterator21.hasNext();) {
            svf.VrSetForm("KNJJ142.frm", 4);
            svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　委員会人数一覧表(" + _param._committeeSemesterName + ")");
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._date));
            svf.VrsOut("CLUB_TITLE", "委員会名");

            List pageList = (List) iterator21.next();
            for (Iterator iterator = _param._regdList.iterator(); iterator.hasNext();) {
                Map pageMap = (Map) iterator.next();

                for (Iterator iterator2 = pageMap.keySet().iterator(); iterator2.hasNext();) {
                    final String regdKey = (String) iterator2.next();
                    RegdHdat regdHdat = (RegdHdat) pageMap.get(regdKey);
                    final String hrField = "999".equals(regdHdat._hrClass) ? "2" : "1";
                    final String cntField = "999".equals(regdHdat._hrClass) ? "TOTAL_" : "";
                    svf.VrsOut("HR_NAME" + hrField, regdHdat._hrName);
                    int setFieldCnt = 1;
                    for (Iterator iterator211 = pageList.iterator(); iterator211.hasNext();) {
                        Committee committee = (Committee) iterator211.next();
                        svf.VrsOut("SUBTITLE", committee._cFlgName);
                        final String setCommitteeTitleField = getMS932ByteLength(committee._name) > 10 ? "2" : "1";
                        svf.VrsOutn("CLUB_NAME" + setCommitteeTitleField, setFieldCnt, committee._name);
                        final PrintData printDataMan = (PrintData) _param._committeeData.get(regdHdat._grade + regdHdat._hrClass + "1" + committee._schoolcd + committee._schoolKind + committee._cd);
                        final PrintData printDataWoMan = (PrintData) _param._committeeData.get(regdHdat._grade + regdHdat._hrClass + "2" + committee._schoolcd + committee._schoolKind + committee._cd);
                        if (null != printDataMan) {
                            svf.VrsOutn(cntField + "MALE", setFieldCnt, printDataMan._cnt);
                        } else {
                            svf.VrsOutn(cntField + "MALE", setFieldCnt, "0");
                        }
                        if (null != printDataWoMan) {
                            svf.VrsOutn(cntField + "FEMALE", setFieldCnt, printDataWoMan._cnt);
                        } else {
                            svf.VrsOutn(cntField + "FEMALE", setFieldCnt, "0");
                        }
                        setFieldCnt++;
                    }
                    svf.VrEndRecord();
                    _hasData = true;

                }
            }
        }
    }

    private static int getMS932ByteLength(final String s) {
        if (null != s) {
            try {
                return s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return 0;
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 62654 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _committeeFlg;
        private final String _committeeSemester;
        private final String _committeeSemesterName;
        private final String _year;
        private final String _semester;
        private final String _date;
        private final List _committeeList;
        private final List _regdList;
        private final Map _committeeData;
        private final String _useSchool_KindField;
        private final String _SCHOOLCD;
        private final String _SCHOOLKIND;
        private String use_prg_schoolkind;
        private String selectSchoolKind;
        private String selectSchoolKindSql;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _date = request.getParameter("CTRL_DATE");
            _committeeFlg = request.getParameter("COMMITTEE_FLG");
            _committeeSemester = request.getParameter("SEMESTER");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD= request.getParameter("SCHOOLCD");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
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
            _committeeSemesterName = getCommitteeSemeName(db2);
            _committeeList = getCommitteeList(db2);
            _regdList = getRegdList(db2);
            _committeeData = getPrintData(db2);
        }

        private String getCommitteeSemeName(final DB2UDB db2) throws SQLException {
            String retStr = "";
            final String semesterSql = "SELECT * FROM NAME_MST WHERE NAMECD1 = 'J004' AND NAMECD2 = '" + _committeeSemester + "'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(semesterSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = rs.getString("NAME1");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }

            return retStr;
        }

        private List getCommitteeList(final DB2UDB db2) throws SQLException {
            final List retList = new ArrayList();
            final String committeeSql = getCommitteeSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(committeeSql);
                rs = ps.executeQuery();
                final int maxCnt = 50;
                int cnt = 1;
                List pageList = new ArrayList();
                String befCFlg = "";
                while (rs.next()) {
                    String schoolcd = "";
                    String schoolKind = "";
                    if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                        schoolcd = rs.getString("SCHOOLCD");
                        schoolKind = rs.getString("SCHOOL_KIND");
                    }
                    final String cd = rs.getString("COMMITTEECD");
                    final String name = rs.getString("COMMITTEENAME");
                    final String cFlg = rs.getString("COMMITTEE_FLG");
                    final String cFlgName = rs.getString("NAME1");

                    final Committee committee = new Committee(schoolcd, schoolKind, cd, name, cFlg, cFlgName);
                    if (cnt > maxCnt) {
                        retList.add(pageList);
                        pageList = new ArrayList();
                        cnt = 1;
                    } else if (!"".equals(befCFlg) && !befCFlg.equals(cFlg)) {
                        retList.add(pageList);
                        pageList = new ArrayList();
                        cnt = 1;
                    }
                    pageList.add(committee);

                    befCFlg = cFlg;
                    cnt++;
                }
                if (cnt > 1) {
                    retList.add(pageList);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retList;
        }

        private String getCommitteeSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                stb.append("     T1.SCHOOLCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
            }
            stb.append("     T1.COMMITTEE_FLG, ");
            stb.append("     T1.COMMITTEECD, ");
            stb.append("     T2.COMMITTEENAME, ");
            stb.append("     L1.NAME1 ");
            stb.append(" FROM ");
            stb.append("     COMMITTEE_YDAT T1, ");
            stb.append("     COMMITTEE_MST T2 ");
            stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'J003' ");
            stb.append("          AND T2.COMMITTEE_FLG = L1.NAMECD2 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _year + "' ");
            stb.append("     AND T1.COMMITTEE_FLG = T2.COMMITTEE_FLG ");
            stb.append("     AND T1.COMMITTEECD   = T2.COMMITTEECD ");
            if (null != _committeeFlg && !"".equals(_committeeFlg)) {
                stb.append("     AND T1.COMMITTEE_FLG = '" + _committeeFlg + "' ");
            }
            if ("1".equals(use_prg_schoolkind)) {
                stb.append("   AND T2.SCHOOLCD = T1.SCHOOLCD ");
                stb.append("   AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                if (!StringUtils.isBlank(selectSchoolKindSql)) {
                    stb.append("   AND T1.SCHOOLCD = '" + _SCHOOLCD + "' ");
                    stb.append("   AND T1.SCHOOL_KIND IN " + selectSchoolKindSql + " ");
                }
            } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND) && !StringUtils.isBlank(_SCHOOLCD)) {
                stb.append("   AND T1.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("   AND T1.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
                stb.append("   AND T2.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("   AND T2.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            }
            stb.append(" ORDER BY ");
            if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                stb.append("     T1.SCHOOLCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
            }
            stb.append("     T1.COMMITTEE_FLG, ");
            stb.append("     T1.COMMITTEECD ");
            return stb.toString();
        }

        private List getRegdList(final DB2UDB db2) throws SQLException {
            final List retList = new ArrayList();
            final String committeeSql = getRegdHdat();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(committeeSql);
                rs = ps.executeQuery();
                final int maxCnt = 25;
                int cnt = 1;
                Map pageMap = new TreeMap();
                String befGrade = "";
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");

                    final RegdHdat hdat = new RegdHdat(grade, hrClass, hrName);
                    if (cnt > maxCnt) {
                        retList.add(pageMap);
                        pageMap = new TreeMap();
                        cnt = 1;
                    }
                    if (!"".equals(befGrade) && !befGrade.equals(grade)) {
                        final RegdHdat hdatAll = new RegdHdat(befGrade, "999", befGrade + "学年合計");
                        pageMap.put(befGrade + "999", hdatAll);
                        cnt++;
                    }
                    if (cnt > maxCnt) {
                        retList.add(pageMap);
                        pageMap = new TreeMap();
                        cnt = 1;
                    }
                    pageMap.put(grade + hrClass, hdat);

                    befGrade = grade;
                    cnt++;
                }
                if (cnt > maxCnt) {
                    retList.add(pageMap);
                    pageMap = new TreeMap();
                    cnt = 1;
                }
                if (cnt > 1) {
                    final RegdHdat hdatAll = new RegdHdat(befGrade, "999", befGrade + "学年合計");
                    pageMap.put(befGrade + "999", hdatAll);
                    retList.add(pageMap);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retList;
        }

        private String getRegdHdat() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REGDH.* ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT REGDH ");
            if ("1".equals(use_prg_schoolkind)) {
                if (!StringUtils.isBlank(selectSchoolKindSql)) {
                    stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGDH.YEAR AND GDAT.GRADE = REGDH.GRADE ");
                    stb.append("   AND GDAT.SCHOOL_KIND IN " + selectSchoolKindSql + " ");
                }
            } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                stb.append("     INNER JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGDH.YEAR AND REGDG.GRADE = REGDH.GRADE ");
                stb.append("       AND REGDG.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            }
            stb.append(" WHERE ");
            stb.append("     REGDH.YEAR = '" + _year + "' ");
            stb.append("     AND REGDH.SEMESTER = '" + _semester + "' ");
            stb.append(" ORDER BY ");
            stb.append("     REGDH.GRADE, ");
            stb.append("     REGDH.HR_CLASS ");
            return stb.toString();
        }

        private Map getPrintData(final DB2UDB db2) throws SQLException {
            final Map retMap = new HashMap();
            final String committeeSql = getCommitteeDataSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(committeeSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");
                    final String sex = rs.getString("SEX");
                    final String cFlg = rs.getString("COMMITTEE_FLG");
                    final String cCd = rs.getString("COMMITTEECD");
                    final String cnt = rs.getString("CNT");
                    String schoolcd = "";
                    String schoolKind = "";
                    if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                        schoolcd = rs.getString("SCHOOLCD");
                        schoolKind = rs.getString("SCHOOL_KIND");
                    }

                    final PrintData printData = new PrintData(grade, hrClass, hrName, sex, cFlg, cCd, cnt);
                    retMap.put(grade + hrClass + sex + schoolcd + schoolKind + cCd, printData);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }

            return retMap;
        }

        private String getCommitteeDataSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD_H AS ( ");
            stb.append(" SELECT ");
            stb.append("     REGDH.*, ");
            stb.append("     N1.NAMECD2 AS SEX, ");
            stb.append("     N1.NAME1 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT REGDH ");
            if ("1".equals(use_prg_schoolkind)) {
                if (!StringUtils.isBlank(selectSchoolKindSql)) {
                    stb.append(" INNER JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGDH.YEAR AND REGDG.GRADE = REGDH.GRADE ");
                    stb.append("   AND REGDG.SCHOOL_KIND IN " + selectSchoolKindSql + " ");
                }
            } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                stb.append("     INNER JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGDH.YEAR AND REGDG.GRADE = REGDH.GRADE ");
                stb.append("       AND REGDG.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            }
            stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' ");
            stb.append(" WHERE ");
            stb.append("     REGDH.YEAR = '" + _year + "' ");
            stb.append("     AND REGDH.SEMESTER = '" + _semester + "' ");
            stb.append(" ), REGD_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     REGD.*, ");
            stb.append("     BASE.SEX ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     , ");
            stb.append("     (SELECT ");
            stb.append("          T2.SCHREGNO, ");
            stb.append("          MAX(T2.SEMESTER) AS SEMESTER ");
            stb.append("      FROM ");
            stb.append("          SCHREG_REGD_DAT T2 ");
            stb.append("      WHERE ");
            stb.append("          T2.YEAR = '" + _year + "' ");
            stb.append("      GROUP BY ");
            stb.append("          T2.SCHREGNO ");
            stb.append("     ) MAXSEME ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + _year + "' ");
            stb.append("     AND REGD.SCHREGNO = MAXSEME.SCHREGNO ");
            stb.append("     AND REGD.SEMESTER = MAXSEME.SEMESTER ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     REGD_H.GRADE, ");
            stb.append("     REGD_H.HR_CLASS, ");
            stb.append("     REGD_H.HR_NAME, ");
            stb.append("     REGD_H.SEX, ");
            if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                stb.append("     COMMITTEE.SCHOOLCD, ");
                stb.append("     COMMITTEE.SCHOOL_KIND, ");
            }
            stb.append("     COMMITTEE.COMMITTEE_FLG, ");
            stb.append("     COMMITTEE.COMMITTEECD, ");
            stb.append("     SUM(CASE WHEN COMMITTEE.COMMITTEECD IS NOT NULL THEN 1 ELSE 0 END) AS CNT ");
            stb.append(" FROM ");
            stb.append("     REGD_H ");
            stb.append("     LEFT JOIN REGD_T ON REGD_H.GRADE = REGD_T.GRADE ");
            stb.append("          AND REGD_H.HR_CLASS = REGD_T.HR_CLASS ");
            stb.append("          AND REGD_H.SEX = REGD_T.SEX ");
            stb.append("     INNER JOIN SCHREG_COMMITTEE_HIST_DAT COMMITTEE ON COMMITTEE.YEAR = '" + _year + "' ");
            stb.append("          AND COMMITTEE.SEMESTER = '" + _committeeSemester + "' ");
            stb.append("          AND REGD_T.SCHREGNO = COMMITTEE.SCHREGNO ");
            if ("1".equals(use_prg_schoolkind)) {
                if (!StringUtils.isBlank(selectSchoolKindSql)) {
                    stb.append("   AND COMMITTEE.SCHOOLCD = '" + _SCHOOLCD + "' ");
                    stb.append("   AND COMMITTEE.SCHOOL_KIND IN " + selectSchoolKindSql + " ");
                }
            } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND) && !StringUtils.isBlank(_SCHOOLCD)) {
                stb.append("   AND COMMITTEE.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("   AND COMMITTEE.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            }
            stb.append(" GROUP BY ");
            stb.append("     REGD_H.GRADE, ");
            stb.append("     REGD_H.HR_CLASS, ");
            stb.append("     REGD_H.HR_NAME, ");
            stb.append("     REGD_H.SEX, ");
            if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                stb.append("     COMMITTEE.SCHOOLCD, ");
                stb.append("     COMMITTEE.SCHOOL_KIND, ");
            }
            stb.append("     COMMITTEE.COMMITTEE_FLG, ");
            stb.append("     COMMITTEE.COMMITTEECD ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     REGD_H.GRADE, ");
            stb.append("     '999' AS HR_CLASS, ");
            stb.append("     '学年' AS HR_NAME, ");
            stb.append("     REGD_H.SEX, ");
            if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                stb.append("     COMMITTEE.SCHOOLCD, ");
                stb.append("     COMMITTEE.SCHOOL_KIND, ");
            }
            stb.append("     COMMITTEE.COMMITTEE_FLG, ");
            stb.append("     COMMITTEE.COMMITTEECD, ");
            stb.append("     SUM(CASE WHEN COMMITTEE.COMMITTEECD IS NOT NULL THEN 1 ELSE 0 END) AS CNT ");
            stb.append(" FROM ");
            stb.append("     REGD_H ");
            stb.append("     LEFT JOIN REGD_T ON REGD_H.GRADE = REGD_T.GRADE ");
            stb.append("          AND REGD_H.HR_CLASS = REGD_T.HR_CLASS ");
            stb.append("          AND REGD_H.SEX = REGD_T.SEX ");
            stb.append("     INNER JOIN SCHREG_COMMITTEE_HIST_DAT COMMITTEE ON COMMITTEE.YEAR = '" + _year + "' ");
            stb.append("          AND COMMITTEE.SEMESTER = '" + _committeeSemester + "' ");
            stb.append("          AND REGD_T.SCHREGNO = COMMITTEE.SCHREGNO ");
            if ("1".equals(use_prg_schoolkind)) {
                if (!StringUtils.isBlank(selectSchoolKindSql)) {
                    stb.append("   AND COMMITTEE.SCHOOLCD = '" + _SCHOOLCD + "' ");
                    stb.append("   AND COMMITTEE.SCHOOL_KIND IN " + selectSchoolKindSql + " ");
                }
            } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND) && !StringUtils.isBlank(_SCHOOLCD)) {
                stb.append("   AND COMMITTEE.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("   AND COMMITTEE.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            }
            stb.append(" GROUP BY ");
            stb.append("     REGD_H.GRADE, ");
            stb.append("     REGD_H.SEX, ");
            if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                stb.append("     COMMITTEE.SCHOOLCD, ");
                stb.append("     COMMITTEE.SCHOOL_KIND, ");
            }
            stb.append("     COMMITTEE.COMMITTEE_FLG, ");
            stb.append("     COMMITTEE.COMMITTEECD ");
            stb.append(" ORDER BY ");
            stb.append("     GRADE, ");
            stb.append("     HR_CLASS, ");
            stb.append("     SEX, ");
            if ("1".equals(use_prg_schoolkind) || "1".equals(_useSchool_KindField)) {
                stb.append("     SCHOOLCD, ");
                stb.append("     SCHOOL_KIND, ");
            }
            stb.append("     COMMITTEE_FLG, ");
            stb.append("     COMMITTEECD ");

            return stb.toString();
        }

    }

    /** 委員会 */
    private class Committee {
        private final String _schoolcd;
        private final String _schoolKind;
        private final String _cd;
        private final String _name;
        private final String _cFlg;
        private final String _cFlgName;

        public Committee(
                final String schoolcd,
                final String schoolKind,
                final String cd,
                final String name,
                final String cFlg,
                final String cFlgName
                ) {
            _schoolcd = schoolcd;
            _schoolKind = schoolKind;
            _cd = cd;
            _name = name;
            _cFlg = cFlg;
            _cFlgName = cFlgName;
        }
    }

    /** REGDH */
    private class RegdHdat {
        private final String _grade;
        private final String _hrClass;
        private final String _hrName;
        private final Map _manMap;
        private final Map _womanMap;

        public RegdHdat(
                final String grade,
                final String hrClass,
                final String hrName
                ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _manMap = new HashMap();
            _womanMap = new HashMap();
        }
    }

    /** 印字データ */
    private class PrintData {
        private final String _grade;
        private final String _hrClass;
        private final String _hrName;
        private final String _sex;
        private final String _cFlg;
        private final String _cCd;
        private final String _cnt;

        public PrintData(
                final String grade,
                final String hrClass,
                final String hrName,
                final String sex,
                final String cFlg,
                final String cCd,
                final String cnt
                ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _sex = sex;
            _cFlg = cFlg;
            _cCd = cCd;
            _cnt = cnt;
        }
    }
}

// eof
