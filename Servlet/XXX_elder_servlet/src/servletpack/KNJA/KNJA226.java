/*
 * $Id: 172c1ec4c249ad7b806df4fe6db51d32985ecfcd $
 *
 * 作成日: 2011/09/22
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *  学校教育システム 賢者 [小学校プログラム] 全校生徒名簿
 *
 */
public class KNJA226 {

    private static final Log log = LogFactory.getLog(KNJA226.class);

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

        svf.VrSetForm("KNJA226.frm", 1);

        final String nendo;
    	if (_param._isSeireki) {
    		nendo = StringUtils.defaultString(_param._ctrlYear) + "年度";
    	} else {
    		nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear)) + "年度";
    	}
        svf.VrsOut("NENDO", nendo + "　全校生徒名簿（" + ("2".equals(_param._hrClassType) ? "複式" : "法定") + "）");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._ctrlDate));

        final List abListAll = new ArrayList();
        final List cdeListAll = new ArrayList();

        for (int gi = 0; gi < _param._gradeMapList.size(); gi++) {
            final Map gradeMap = (Map) _param._gradeMapList.get(gi);
            final String grade = (String) gradeMap.get("GRADE");
            boolean isCde;
            isCde = false;
            abListAll.add(printHrclass(db2, svf, isCde, gi * 2 + 1, new String[] {grade + "001"})); // 1組の生徒
            abListAll.add(printHrclass(db2, svf, isCde, gi * 2 + 2, new String[] {grade + "002"})); // 2組の生徒
            isCde = true;
            cdeListAll.add(printHrclass(db2, svf, isCde, gi + 1, new String[] {grade + "003", grade + "004", grade + "005"})); // 3、4、5組の生徒
            _hasData = true;
        }

        final List ab = toStudentList(abListAll);
        final List cde = toStudentList(cdeListAll);
        final List all = new ArrayList();
        all.addAll(ab);
        all.addAll(cde);
        svf.VrsOut("AB_SUM", String.valueOf(ab.size()));
        svf.VrsOut("AB_SUM_BOY", String.valueOf(studentCountSex("1", ab).size()));
        svf.VrsOut("AB_SUM_GIRL", String.valueOf(studentCountSex("2", ab).size()));
        svf.VrsOut("CDE_SUM", String.valueOf(cde.size()));
        svf.VrsOut("CDE_SUM_BOY", String.valueOf(studentCountSex("1", cde).size()));
        svf.VrsOut("CDE_SUM_GIRL", String.valueOf(studentCountSex("2", cde).size()));
        svf.VrsOut("SUM_BOY", String.valueOf(studentCountSex("1", all).size()));
        svf.VrsOut("SUM_GIRL", String.valueOf(studentCountSex("2", all).size()));

        svf.VrEndPage();
    }

    private List toStudentList(final List all) {
        final List rtn = new ArrayList();
        for (final Iterator it = all.iterator(); it.hasNext();) {
            final List[] studentList = (List[]) it.next();
            for (int i = 0; i < studentList.length; i++) {
                if (null != studentList[i]) {
                    rtn.addAll(studentList[i]);
                }
            }
        }
        return rtn;
    }

    public List studentCountSex(final String sex, final List studentList) {
        final List list = new ArrayList();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Map student = (Map) it.next();
            if (sex.equals(student.get("SEX"))) {
                list.add(student);
            }
        }
        return list;
    }

    public List[] printHrclass(final DB2UDB db2, final Vrw32alp svf, final boolean isCDE, final int retu, final String[] gradeHrClasses) {
        final String us = isCDE ? "2" : "1";
        final List[] studentLists = new List[gradeHrClasses.length];
        int studentCount = 0;
        int studentCountSex1 = 0;
        int studentCountSex2 = 0;
        for (int j = 0; j < gradeHrClasses.length; j++) {
            final String sj = isCDE ? String.valueOf(j + 2) : "1";
            svf.VrsOutn("HR_NAME" + sj, retu, getHrClassFiDatData(gradeHrClasses[j], "HR_NAME"));
            svf.VrsOutn("TEACHER" + sj + "_1", retu, getHrClassFiDatData(gradeHrClasses[j], "STAFFNAME1"));
            svf.VrsOutn("TEACHER" + sj + "_2", retu, getHrClassFiDatData(gradeHrClasses[j], "STAFFNAME2"));
            studentLists[j] = getSchregRegdFiDatList(db2, gradeHrClasses[j]);

            for (int i = 0; i < studentLists[j].size(); i++) {
                final Map student = (Map) studentLists[j].get(i);
//                log.debug(" student " + student);
                final String name = (String) student.get("NAME");
                final String gyo = sj + "_" + String.valueOf(i + 1);
                svf.VrsOutn("NAME" + gyo, retu, name);
                svf.VrsOutn("NO" + gyo, retu, getAttendno((String) student.get("ATTENDNO")));
                svf.VrsOutn("LEAGAL_HR" + gyo, retu, (String) student.get("REGDH_HR_CLASS_NAME1"));
            }

            studentCount += studentLists[j].size();
            studentCountSex1 += studentCountSex("1", studentLists[j]).size();
            studentCountSex2 += studentCountSex("2", studentLists[j]).size();
        }
        svf.VrsOutn("TOTAL" + us, retu, String.valueOf(studentCount));
        svf.VrsOutn("SUB_TOTAL" + us + "_1", retu, String.valueOf(studentCountSex1));
        svf.VrsOutn("SUB_TOTAL" + us + "_2", retu, String.valueOf(studentCountSex2));
        return studentLists;
    }

    private String getHrClassFiDatData(final String gradeHrClass, final String field) {
        final Map map = (Map) _param._hrClassFiDatMap.get(gradeHrClass);
        if (null == map) {
            return "";
        }
        return StringUtils.defaultString((String) map.get(field));
    }

    private String getAttendno(final String s) {
        if (NumberUtils.isDigits(s)) {
            return String.valueOf(Integer.parseInt(s));
        }
        return s;
    }

    private List getSchregRegdFiDatList(final DB2UDB db2, final String gradeHrClass) {
        final List schregRegdDatList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT T1.ATTENDNO, T2.SEX, T2.NAME, REGD.HR_CLASS AS REGD_HR_CLASS, REGDH.HR_CLASS_NAME1 AS REGDH_HR_CLASS_NAME1 ");
            if ("2".equals(_param._hrClassType)) {
                sql.append(" FROM SCHREG_REGD_FI_DAT T1 ");
            } else {
                sql.append(" FROM SCHREG_REGD_DAT T1 ");
            }
            sql.append(" INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            sql.append(" LEFT JOIN ");
            if ("2".equals(_param._hrClassType)) {
                sql.append(" SCHREG_REGD_DAT ");
            } else {
                sql.append(" SCHREG_REGD_FI_DAT ");
            }
            sql.append(" REGD ON REGD.SCHREGNO = T1.SCHREGNO ");
            sql.append("   AND REGD.YEAR = T1.YEAR ");
            sql.append("   AND REGD.SEMESTER = T1.SEMESTER ");
            sql.append(" LEFT JOIN ");
            if ("2".equals(_param._hrClassType)) {
                sql.append(" SCHREG_REGD_HDAT ");
            } else {
                sql.append(" SCHREG_REGD_FI_HDAT ");
            }
            sql.append(" REGDH ON REGDH.YEAR = REGD.YEAR ");
            sql.append("   AND REGDH.SEMESTER = REGD.SEMESTER ");
            sql.append("   AND REGDH.GRADE = REGD.GRADE ");
            sql.append("   AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            sql.append(" WHERE T1.YEAR = '" + _param._ctrlYear + "' ");
            sql.append("   AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
            sql.append("   AND T1.GRADE || T1.HR_CLASS = '" + gradeHrClass + "' ");
            sql.append("  ORDER BY T1.ATTENDNO ");

            ps = db2.prepareStatement(sql.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final Map map = new HashMap();
                map.put("NAME", rs.getString("NAME"));
                map.put("ATTENDNO", rs.getString("ATTENDNO"));
                map.put("SEX", rs.getString("SEX"));
                map.put("REGDH_HR_CLASS_NAME1", rs.getString("REGDH_HR_CLASS_NAME1"));
                schregRegdDatList.add(map);
            }

        } catch (Exception e) {
            log.fatal("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return schregRegdDatList;
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 65164 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _hrClassType;
        final List _gradeMapList;
        final Map _hrClassFiDatMap;
        final String _useSchool_KindField;
        final String _SCHOOLKIND;
        final boolean _isSeireki;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _hrClassType = request.getParameter("HR_CLASS_TYPE");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _isSeireki = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' ")));
            _gradeMapList = getGradeMapList(db2);
//            log.debug(" print grade = " + _gradeMapList);
            _hrClassFiDatMap = getHrClassFiDatMap(db2);
        }

        private List getGradeMapList(final DB2UDB db2) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT GRADE ");
                sql.append(" FROM SCHREG_REGD_GDAT ");
                sql.append(" WHERE YEAR = '" + _ctrlYear + "' ");
                if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                    sql.append("   AND SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
                }
                sql.append(" ORDER BY GRADE ");

                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map m = new HashMap();
                    m.put("GRADE", rs.getString("GRADE"));
                    list.add(m);
                }

            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private Map getHrClassFiDatMap(final DB2UDB db2) {
            final Map map = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT T1.GRADE || T1.HR_CLASS AS GRADE_HR_CLASS, T1.HR_NAME ");
                sql.append("   , T2.STAFFNAME AS STAFFNAME1 ");
                sql.append("   , T3.STAFFNAME AS STAFFNAME2 ");
                sql.append("   , T4.STAFFNAME AS STAFFNAME3 ");
                sql.append("   , T5.STAFFNAME AS STAFFNAME4 ");
                sql.append("   , T6.STAFFNAME AS STAFFNAME5 ");
                sql.append("   , T7.STAFFNAME AS STAFFNAME6 ");
                if ("2".equals(_hrClassType)) {
                    sql.append(" FROM SCHREG_REGD_FI_HDAT T1 ");
                } else {
                    sql.append(" FROM SCHREG_REGD_HDAT T1 ");
                }
                sql.append(" LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.TR_CD1 ");
                sql.append(" LEFT JOIN STAFF_MST T3 ON T3.STAFFCD = T1.TR_CD2 ");
                sql.append(" LEFT JOIN STAFF_MST T4 ON T4.STAFFCD = T1.TR_CD3 ");
                sql.append(" LEFT JOIN STAFF_MST T5 ON T5.STAFFCD = T1.SUBTR_CD1 ");
                sql.append(" LEFT JOIN STAFF_MST T6 ON T6.STAFFCD = T1.SUBTR_CD2 ");
                sql.append(" LEFT JOIN STAFF_MST T7 ON T7.STAFFCD = T1.SUBTR_CD3 ");
                sql.append(" WHERE T1.YEAR = '" + _ctrlYear + "' ");
                sql.append("   AND T1.SEMESTER = '" + _ctrlSemester + "' ");

                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map m = new HashMap();
                    m.put("HR_NAME", rs.getString("HR_NAME"));
                    int c = 1;
                    for (int i = 1; i <= 6; i++) {
                        final String staffname = rs.getString("STAFFNAME" + String.valueOf(i));
                        if (null != staffname) {
                            m.put("STAFFNAME" + String.valueOf(c), staffname);
                            c += 1;
                            if (c > 2) {
                                break;
                            }
                        }
                    }
                    map.put(rs.getString("GRADE_HR_CLASS"), m);
                }

            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
        }
    }
}

// eof