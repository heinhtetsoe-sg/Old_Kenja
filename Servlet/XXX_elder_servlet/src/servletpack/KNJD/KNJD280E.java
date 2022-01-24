/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 8a94dd44e1cbf11740f194e0b8f8ef7ece91830a $
 *
 * 作成日: 2019/01/29
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD280E {

    private static final Log log = LogFactory.getLog(KNJD280E.class);

    private boolean _hasData;
    private static final String SEMEALL = "9";

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

            printMain(db2, svf, _param);
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

    protected void printMain(final DB2UDB db2, final Vrw32alp svf, final Param param) {
        final List studentList = Student.getStudentList(db2, param);

        final String ZENKI_KIMATSU  = "1020101";
        final String KOUKI_KIMATSU  = "2020101";
        final int tanka = 1000;

        for (int i = 0; i < studentList.size(); i++) {
            final Student student = (Student) studentList.get(i);
            final boolean isSaturdayCourse = "101".equals(student._majorcd); // 土曜コースか

            final List subclassList = Subclass.getPrintSubclassList(param, student);
            final int record = 100;
            final int retestPassScore = 40;
            final List printSubclassList = new ArrayList();
            for (int subi = 0; subi < record; subi++) {
                if (subi < subclassList.size()) {
                    final Subclass subclass = (Subclass) subclassList.get(subi);
                    if (null != subclass._subclassAttendance && null != subclass._subclassAttendance._attend) {
                        if (Integer.parseInt(subclass._schoolingLimitCnt) > subclass._subclassAttendance._attend.intValue()) {
                            continue;
                        }
                    }
                    final int checkRval = null != subclass._rVal1 && !"".equals(subclass._rVal1) ? Integer.parseInt(subclass._rVal1) : 0;
                    if (Integer.parseInt(subclass._repoLimitCnt) > checkRval) {
                        continue;
                    }

                    if (isSaturdayCourse) {
                        if (!"1".equals(subclass._tVal1) && !"2".equals(subclass._tVal1) && !"3".equals(subclass._tVal1) && !"4".equals(subclass._tVal1)) {
                            continue;
                        }
                    } else {
                        if (!"1".equals(subclass._tVal1) && !"2".equals(subclass._tVal1) && !"3".equals(subclass._tVal1)) {
                            continue;
                        }
                    }
                    if ("2".equals(_param._ctrlSemester) && subclass._isZenki) {
                        continue;
                    }

                    if (subclass._isZenki) {
                        final String retest = subclass.getScore(ZENKI_KIMATSU);
                        if (NumberUtils.isDigits(retest)) {
                            if (retestPassScore <= Integer.parseInt(retest)) {
                                continue;
                            }
                        }
                    }

                    if (subclass._isKouki) {
                        final String retest = subclass.getScore(KOUKI_KIMATSU);
                        if (NumberUtils.isDigits(retest)) {
                            if (retestPassScore <= Integer.parseInt(retest)) {
                                continue;
                            }
                        }
                        final String SLUMP = KOUKI_KIMATSU + "_SLUMP";
                        final String retestSlump = subclass.getScore(SLUMP);
                        if (NumberUtils.isDigits(retestSlump)) {
                            if (retestPassScore <= Integer.parseInt(retestSlump)) {
                                continue;
                            }
                        }
                    }

                    printSubclassList.add(subclass._mst._subclassname);
                }
            }

            if (printSubclassList.size() > 0) {

                final String form = "1".equals(_param._moneyPrint) ? "KNJD280E.frm" : "KNJD280E_2.frm";
                svf.VrSetForm(form, 4);

                svf.VrsOut("DATE", KNJ_EditDate.getAutoFormatDate(db2, _param._printDate));
                svf.VrsOut("NAME", student._name + "(" + student._hrname + ")　様");
                svf.VrsOut("NAME2", student._name + "(" + student._hrname + ")");
                svf.VrsOut("NAME3", student._name + "(" + student._hrname + ")");
                svf.VrsOut("GUARD_NAME", student._guardName + "　様");
                svf.VrsOut("COPR_NAME", "学校法人　立志舎");
                svf.VrsOut("SCHOOL_NAME", _param._schoolName);
                svf.VrsOut("EXAM_DATE", KNJ_EditDate.getAutoFormatDate(db2, _param._retestDate) + "(" + KNJ_EditDate.h_format_W(_param._retestDate) + ")");
                final int setMoney = tanka * printSubclassList.size();
                svf.VrsOut("MONEY1", String.valueOf(setMoney));
                svf.VrsOut("MONEY2", String.valueOf(setMoney));

                for (Iterator itSubclass = printSubclassList.iterator(); itSubclass.hasNext();) {
                    final String subclassName = (String) itSubclass.next();
                    svf.VrsOut("SUBCLASS_NAME", subclassName);
                    svf.VrEndRecord();
                }
                _hasData = true;
            }
        }
    }

    /**
     * 生徒
     */
    private static class Student {
        String _schregno;
        String _name;
        String _hrname;
        String _staffname;
        String _staffname2;
        String _grade;
        String _gradeCd;
        String _coursecd;
        String _majorcd;
        String _course;
        String _majorname;
        String _majorabbv;
        String _coursecodename;
        String _attendno;
        String _attendnoZeroSuprpess;
        String _hrClassName1;
        String _guardName;
        final Map _attendMap = new TreeMap();
        final Map _subclassMap = new TreeMap();
        String _entyear;

        Subclass getSubclass(final String subclasscd) {
            return (Subclass) _subclassMap.get(subclasscd);
        }

        /**
         * 生徒を取得
         */
        private static List getStudentList(final DB2UDB db2, final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("     SELECT  REGD.SCHREGNO");
            stb.append("            ,REGD.SEMESTER ");
            stb.append("            ,BASE.NAME ");
            stb.append("            ,GUARD.GUARD_NAME ");
            stb.append("            ,HDAT.HR_NAME ");
            stb.append("            ,STFM.STAFFNAME ");
            stb.append("            ,STFM2.STAFFNAME AS STAFFNAME2 ");
            stb.append("            ,REGD.ATTENDNO ");
            stb.append("            ,REGD.GRADE ");
            stb.append("            ,GDAT.GRADE_CD ");
            stb.append("            ,REGD.COURSECD ");
            stb.append("            ,REGD.MAJORCD ");
            stb.append("            ,REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE");
            stb.append("            ,MAJ.MAJORNAME ");
            stb.append("            ,MAJ.MAJORABBV ");
            stb.append("            ,CCM.COURSECODENAME ");
            stb.append("            ,HDAT.HR_CLASS_NAME1 ");
            stb.append("            ,FISCALYEAR(BASE.ENT_DATE) AS ENT_YEAR ");
            stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  ELSE 0 END AS LEAVE ");
            stb.append("     FROM    SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST SEMEG ON SEMEG.YEAR = '" + param._ctrlYear + "' AND SEMEG.SEMESTER = REGD.SEMESTER AND SEMEG.GRADE = REGD.GRADE ");
            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
            stb.append("                  AND W3.GRD_DATE < CASE WHEN SEMEG.EDATE < '" + param._printDate + "' THEN SEMEG.EDATE ELSE '" + param._printDate + "' END ");
            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
            stb.append("                  AND W4.ENT_DATE > CASE WHEN SEMEG.EDATE < '" + param._printDate + "' THEN SEMEG.EDATE ELSE '" + param._printDate + "' END ");
            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
            stb.append("                  AND CASE WHEN SEMEG.EDATE < '" + param._printDate + "' THEN SEMEG.EDATE ELSE '" + param._printDate + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = REGD.YEAR ");
            stb.append("                  AND HDAT.SEMESTER = REGD.SEMESTER ");
            stb.append("                  AND HDAT.GRADE = REGD.GRADE ");
            stb.append("                  AND HDAT.HR_CLASS = REGD.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR ");
            stb.append("                  AND GDAT.GRADE = REGD.GRADE ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN GUARDIAN_DAT GUARD ON BASE.SCHREGNO = GUARD.SCHREGNO ");
            stb.append("     LEFT JOIN STAFF_MST STFM ON STFM.STAFFCD = HDAT.TR_CD1 ");
            stb.append("     LEFT JOIN STAFF_MST STFM2 ON STFM2.STAFFCD = HDAT.TR_CD2 ");
            stb.append("     LEFT JOIN MAJOR_MST MAJ ON MAJ.COURSECD = REGD.COURSECD ");
            stb.append("                  AND MAJ.MAJORCD = REGD.MAJORCD ");
            stb.append("     LEFT JOIN COURSECODE_MST CCM ON CCM.COURSECODE = REGD.COURSECODE ");
            stb.append("     WHERE   REGD.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._ctrlSemester + "' ");
            if ("1".equals(param._output)) {
                stb.append("    AND REGD.GRADE || REGD.HR_CLASS = '" + param._gradeHrClass + "' ");
                stb.append("    AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected));
            } else if ("2".equals(param._output)) {
                stb.append("    AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, param._categorySelected));
            }
            stb.append("     ORDER BY ");
            stb.append("         REGD.GRADE, ");
            stb.append("         REGD.HR_CLASS, ");
            stb.append("         REGD.ATTENDNO ");
            final String sql = stb.toString();
            log.debug(" student sql = " + sql);

            final List studentList = new ArrayList();

            for (final Iterator it = KnjDbUtils.query(db2, stb.toString()).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final Student student = new Student();
                student._schregno = KnjDbUtils.getString(row, "SCHREGNO");
                student._name = KnjDbUtils.getString(row, "NAME");
                student._hrname = KnjDbUtils.getString(row, "HR_NAME");
                student._staffname = StringUtils.defaultString(KnjDbUtils.getString(row, "STAFFNAME"));
                student._staffname2 = StringUtils.defaultString(KnjDbUtils.getString(row, "STAFFNAME2"));
                student._attendno = NumberUtils.isDigits(KnjDbUtils.getString(row, "ATTENDNO")) ? String.valueOf(Integer.parseInt(KnjDbUtils.getString(row, "ATTENDNO"))) + "番" : KnjDbUtils.getString(row, "ATTENDNO");
                student._attendnoZeroSuprpess = NumberUtils.isDigits(KnjDbUtils.getString(row, "ATTENDNO")) ? String.valueOf(Integer.parseInt(KnjDbUtils.getString(row, "ATTENDNO"))) : KnjDbUtils.getString(row, "ATTENDNO");
                student._grade = KnjDbUtils.getString(row, "GRADE");
                student._gradeCd = KnjDbUtils.getString(row, "GRADE_CD");
                student._coursecd = KnjDbUtils.getString(row, "COURSECD");
                student._majorcd = KnjDbUtils.getString(row, "MAJORCD");
                student._course = KnjDbUtils.getString(row, "COURSE");
                student._majorname = KnjDbUtils.getString(row, "MAJORNAME");
                student._majorabbv = KnjDbUtils.getString(row, "MAJORABBV");
                student._coursecodename = KnjDbUtils.getString(row, "COURSECODENAME");
                student._hrClassName1 = KnjDbUtils.getString(row, "HR_CLASS_NAME1");
                student._entyear = KnjDbUtils.getString(row, "ENT_YEAR");
                student._guardName = KnjDbUtils.getString(row, "GUARD_NAME");
                studentList.add(student);
            }

            final Map studentMap = new HashMap();
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                studentMap.put(student._schregno, student);
            }

            Subclass.setSubclassList(db2, param, studentMap);

            SubclassAttendance.load(db2, param, studentList);

            return studentList;
        }
    }

    private static class Subclass implements Comparable {
        final SubclassMst _mst;
        final String _subclasscd;
        final String _repoLimitCnt;
        final String _schoolingLimitCnt;
        final String _useMedia1;
        final String _retestPassScore;
        final String _rVal1;
        final String _tVal1;
        final String _sVal1;
        final String _tVal1Name1;

        final Map _scoreMap = new HashMap();
        SubclassAttendance _subclassAttendance;
        boolean _isZenki;
        boolean _isKouki;

        Subclass(
            final SubclassMst mst,
            final String subclasscd,
            final String repoLimitCnt,
            final String schoolingLimitCnt,
            final String useMedia1,
            final String retestPassScore,
            final String rVal1,
            final String tVal1,
            final String tVal1Name1,
            final String sVal1
        ) {
            _mst = mst;
            _subclasscd = subclasscd;
            _repoLimitCnt = null == repoLimitCnt || "".equals(repoLimitCnt) ? "0" : repoLimitCnt;
            _schoolingLimitCnt = schoolingLimitCnt;
            _useMedia1 = useMedia1;
            _retestPassScore = retestPassScore;
            _rVal1 = rVal1;
            _tVal1 = tVal1;
            _tVal1Name1 = tVal1Name1;
            _sVal1 = sVal1;
        }

        public String getScore(final String semTestcd) {
            return (String) _scoreMap.get(semTestcd);
        }

        public int compareTo(final Object o) {
            final Subclass os = (Subclass) o;
            int rtn;
            if (null == _mst) {
                return 1;
            }
            if (null == os._mst) {
                return -1;
            }
            if (_isZenki != os._isZenki && _isZenki) {
                return -1;
            }
            if (_isKouki != os._isKouki && _isKouki) {
                if (os._isZenki) {
                    return 1;
                }
                return -1;
            }
            rtn = _mst.compareTo(os._mst);
            return rtn;
        }

        public static List getPrintSubclassList(final Param param, final Student student) {
            final List subclassList = new ArrayList(student._subclassMap.values());
            Collections.sort(subclassList);
            return subclassList;
        }

        public static void setSubclassList(final DB2UDB db2, final Param param, final Map studentMap) {
            final String sql = sql(param);
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                final Student student = (Student) studentMap.get(schregno);
                if (null == student) {
                    continue;
                }

                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                if (null == student.getSubclass(subclasscd)) {
                    final SubclassMst mst = param.getSubclassMst(subclasscd);

                    final String repoLimitCnt = KnjDbUtils.getString(row, "REPO_LIMIT_CNT");
                    final String schoolingLimitCnt = KnjDbUtils.getString(row, "SCHOOLING_LIMIT_CNT");
                    final String useMedia1 = KnjDbUtils.getString(row, "USE_MEDIA1");
                    final String retestPassScore = KnjDbUtils.getString(row, "RETEST_PASS_SCORE");
                    final String rVal1 = KnjDbUtils.getString(row, "R_VAL1");
                    final String tVal1 = KnjDbUtils.getString(row, "T_VAL1");
                    final String tVal1Name1 = KnjDbUtils.getString(row, "T_VAL1_NAME1");
                    final String sVal1 = KnjDbUtils.getString(row, "S_VAL1");
                    final Subclass subclass = new Subclass(mst, subclasscd, repoLimitCnt, schoolingLimitCnt, useMedia1, retestPassScore, rVal1, tVal1, tVal1Name1, sVal1);
                    student._subclassMap.put(subclasscd, subclass);

                    final String takeSemes = KnjDbUtils.getString(row, "TAKESEMES");
                    if ("1".equals(takeSemes)) {
                        subclass._isZenki = true;
                    } else {
                        subclass._isKouki = true;
                    }
                }

                final Subclass subclass = (Subclass) student._subclassMap.get(subclasscd);

                final String semTestcd = KnjDbUtils.getString(row, "SEM_TESTCD");
                final String score = null != KnjDbUtils.getString(row, "VALUE_DI") ? KnjDbUtils.getString(row, "VALUE_DI") : KnjDbUtils.getString(row, "SCORE");
                subclass._scoreMap.put(semTestcd, score);
                subclass._scoreMap.put(semTestcd + "_SLUMP", KnjDbUtils.getString(row, "SLUMP_SCORE"));
            }
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH CHAIR_SUBCLASS AS ( ");
            stb.append(" SELECT ");
            stb.append("    T1.SCHREGNO ");
            stb.append("    , T1.YEAR ");
            stb.append("    , T1.SEMESTER ");
            stb.append("    , T1.GRADE ");
            stb.append("    , T1.HR_CLASS ");
            stb.append("    , T1.ATTENDNO ");
            stb.append("    , CASE WHEN T1.COURSECODE = '1011' THEN '1' ELSE '0' END AS WEEK_SAT ");
            stb.append("    , L2.CLASSCD ");
            stb.append("    , L2.SCHOOL_KIND ");
            stb.append("    , L2.CURRICULUM_CD ");
            stb.append("    , L2.SUBCLASSCD ");
            stb.append("    , MIN(VALUE(L2.TAKESEMES, '0')) AS TAKESEMES ");
            stb.append("    , MAX(L3.REPO_LIMIT_CNT) AS REPO_LIMIT_CNT ");
            stb.append("    , MAX(L3.SCHOOLING_LIMIT_CNT) AS SCHOOLING_LIMIT_CNT ");
            stb.append("    , MAX(L3.USE_MEDIA1) AS USE_MEDIA1 ");
            stb.append("    , MIN(INT(ABBV2)) AS RETEST_PASS_SCORE ");
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
            stb.append(" INNER JOIN CHAIR_STD_DAT L1 ON ");
            stb.append("      L1.YEAR = T1.YEAR ");
            stb.append("    AND L1.SEMESTER = T1.SEMESTER ");
            stb.append("    AND L1.SCHREGNO = T1.SCHREGNO ");
            stb.append(" INNER JOIN CHAIR_DAT L2 ON ");
            stb.append("      L2.YEAR = T1.YEAR ");
            stb.append("    AND L2.SEMESTER = T1.SEMESTER ");
            stb.append("    AND L2.CHAIRCD = L1.CHAIRCD ");
            if ("1".equals(param._printSubclassLastChairStd)) {
                stb.append("     INNER JOIN SEMESTER_MST STDSEME ON STDSEME.YEAR = L1.YEAR ");
                stb.append("       AND STDSEME.SEMESTER = L1.SEMESTER ");
                stb.append("       AND STDSEME.EDATE = L1.APPENDDATE ");
            }
            stb.append(" LEFT JOIN CHAIR_CORRES_SEMES_DAT L3 ON ");
            stb.append("      L3.YEAR = T1.YEAR ");
            stb.append("    AND L3.SEMESTER = T1.SEMESTER ");
            stb.append("    AND L3.CHAIRCD = L1.CHAIRCD ");
            stb.append("    AND L3.CLASSCD = L2.CLASSCD ");
            stb.append("    AND L3.SCHOOL_KIND = L2.SCHOOL_KIND ");
            stb.append("    AND L3.CURRICULUM_CD = L2.CURRICULUM_CD ");
            stb.append("    AND L3.SUBCLASSCD = L2.SUBCLASSCD ");
            stb.append(" LEFT JOIN NAME_MST M024 ON M024.NAMECD1 = 'M024' ");
            stb.append("    AND M024.NAME1 = T1.COURSECD ");
            stb.append("    AND M024.NAME2 = T1.MAJORCD ");
            stb.append("    AND M024.ABBV1 = L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || L2.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("    T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("    AND T1.SEMESTER = '" + param._ctrlSemester + "' ");
            if ("1".equals(param._output)) {
                stb.append("    AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrClass + "' ");
                stb.append("    AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected));
            } else if ("2".equals(param._output)) {
                stb.append("    AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, param._categorySelected));
            }
            stb.append(" GROUP BY ");
            stb.append("    T1.SCHREGNO ");
            stb.append("    , T1.YEAR ");
            stb.append("    , T1.SEMESTER ");
            stb.append("    , T1.GRADE ");
            stb.append("    , T1.HR_CLASS ");
            stb.append("    , T1.ATTENDNO ");
            stb.append("    , T1.COURSECODE ");
            stb.append("    , L2.CLASSCD ");
            stb.append("    , L2.SCHOOL_KIND ");
            stb.append("    , L2.CURRICULUM_CD ");
            stb.append("    , L2.SUBCLASSCD ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("    T1.SCHREGNO ");
            stb.append("    , T1.YEAR ");
            stb.append("    , T1.SEMESTER ");
            stb.append("    , T1.GRADE ");
            stb.append("    , T1.HR_CLASS ");
            stb.append("    , T1.ATTENDNO ");
            stb.append("    , T1.WEEK_SAT ");
            stb.append("    , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("    , T1.TAKESEMES ");
            stb.append("    , T1.REPO_LIMIT_CNT ");
            stb.append("    , T1.SCHOOLING_LIMIT_CNT ");
            stb.append("    , T1.USE_MEDIA1 ");
            stb.append("    , T1.RETEST_PASS_SCORE ");
            stb.append("    , L5R.VAL_NUMERIC AS R_VAL1 ");
            stb.append("    , L5T.VAL_NUMERIC AS T_VAL1 ");
            stb.append("    , L5S.VAL_NUMERIC AS S_VAL1 ");
            stb.append("    , M006.NAME1 AS T_VAL1_NAME1 ");
            stb.append("    , REC.SEMESTER || REC.TESTKINDCD || REC.TESTITEMCD || REC.SCORE_DIV AS SEM_TESTCD ");
            stb.append("    , REC.SCORE ");
            stb.append("    , REC.VALUE_DI ");
            stb.append("    , SLUMP.SCORE AS SLUMP_SCORE ");
            stb.append(" FROM CHAIR_SUBCLASS T1 ");
            stb.append(" LEFT JOIN SUBCLASS_CORRES_RST_SEMES_DAT L5R ON ");
            stb.append("      L5R.YEAR = T1.YEAR ");
            stb.append("    AND L5R.SEMESTER = T1.SEMESTER ");
            stb.append("    AND L5R.CLASSCD = T1.CLASSCD ");
            stb.append("    AND L5R.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("    AND L5R.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND L5R.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("    AND L5R.RST_DIV = 'R' ");
            stb.append("    AND L5R.SEQ = 1 ");
            stb.append("    AND L5R.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN SUBCLASS_CORRES_RST_SEMES_DAT L5T ON ");
            stb.append("      L5T.YEAR = T1.YEAR ");
            stb.append("    AND L5T.SEMESTER = T1.SEMESTER ");
            stb.append("    AND L5T.CLASSCD = T1.CLASSCD ");
            stb.append("    AND L5T.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("    AND L5T.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND L5T.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("    AND L5T.RST_DIV = 'T' ");
            stb.append("    AND L5T.SEQ = 1 ");
            stb.append("    AND L5T.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN SUBCLASS_CORRES_RST_SEMES_DAT L5S ON ");
            stb.append("      L5S.YEAR = T1.YEAR ");
            stb.append("    AND L5S.SEMESTER = T1.SEMESTER ");
            stb.append("    AND L5S.CLASSCD = T1.CLASSCD ");
            stb.append("    AND L5S.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("    AND L5S.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND L5S.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("    AND L5S.RST_DIV = 'S' ");
            stb.append("    AND L5S.SEQ = 1 ");
            stb.append("    AND L5S.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN NAME_MST M006 ON ");
            stb.append("      M006.NAMECD1 = 'M006' ");
            stb.append("    AND M006.NAMECD2 = CAST(L5T.VAL_NUMERIC AS CHAR(1)) ");
            stb.append(" LEFT JOIN RECORD_SCORE_DAT REC ON ");
            stb.append("      REC.YEAR = T1.YEAR ");
            stb.append("    AND REC.CLASSCD = T1.CLASSCD ");
            stb.append("    AND REC.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("    AND REC.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("    AND REC.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("    AND REC.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN RECORD_SLUMP_SDIV_DAT SLUMP ON ");
            stb.append("      SLUMP.YEAR = REC.YEAR ");
            stb.append("    AND SLUMP.SEMESTER = REC.SEMESTER ");
            stb.append("    AND SLUMP.TESTKINDCD = REC.TESTKINDCD ");
            stb.append("    AND SLUMP.TESTITEMCD = REC.TESTITEMCD ");
            stb.append("    AND SLUMP.SCORE_DIV = REC.SCORE_DIV ");
            stb.append("    AND SLUMP.CLASSCD = REC.CLASSCD ");
            stb.append("    AND SLUMP.SCHOOL_KIND = REC.SCHOOL_KIND ");
            stb.append("    AND SLUMP.CURRICULUM_CD = REC.CURRICULUM_CD ");
            stb.append("    AND SLUMP.SUBCLASSCD = REC.SUBCLASSCD ");
            stb.append("    AND SLUMP.SCHREGNO = REC.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("   T1.SUBCLASSCD NOT LIKE '1601%' AND T1.CLASSCD < '90' ");
            stb.append(" ORDER BY ");
            stb.append("    T1.GRADE ");
            stb.append("    , T1.HR_CLASS ");
            stb.append("    , T1.ATTENDNO ");
            stb.append("    , T1.SUBCLASSCD ");
            return stb.toString();
        }
    }

    private static class SubclassAttendance {
        BigDecimal _lesson;
        BigDecimal _attend;
        BigDecimal _sick;

        public SubclassAttendance(final BigDecimal lesson, final BigDecimal attend, final BigDecimal sick) {
            _lesson = lesson;
            _attend = attend;
            _sick = sick;
        }

        private static void load(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            PreparedStatement ps2 = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSubclassSql(
                        param._ctrlYear,
                        SEMEALL,
                        null,
                        param._printDate,
                        param._attendParamMap
                );

                ps = db2.prepareStatement(sql);

                final String sql2 = getSchAttendDatSql(param);
                ps2 = db2.prepareStatement(sql2);

                for (final Iterator stit = studentList.iterator(); stit.hasNext();) {
                    final Student student = (Student) stit.next();

                    final Set logged = new HashSet();

                    for (final Iterator it = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); it.hasNext();) {
                        final Map row = (Map) it.next();
                        if (!SEMEALL.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                            continue;
                        }
                        final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");

                        final SubclassMst mst = (SubclassMst) param._subclassMstMap.get(subclasscd);
                        if (null == mst) {
                            log.warn("no subclass : " + subclasscd);
                            continue;
                        }
                        final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
                        if ((Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && iclasscd <= Integer.parseInt(KNJDefineSchool.subject_U) || iclasscd == Integer.parseInt(KNJDefineSchool.subject_T))) {
                            if (null == student._subclassMap.get(subclasscd)) {
                                final String message = " null chair subclass = " + subclasscd;
                                if (logged.contains(message)) {
                                    log.info(message);
                                    logged.add(message);
                                }
                                continue;
                            }
                            final Subclass subclass = student.getSubclass(subclasscd);

                            final BigDecimal lesson = KnjDbUtils.getBigDecimal(row, "MLESSON", null);
                            final BigDecimal rawSick = KnjDbUtils.getBigDecimal(row, "SICK1", null);
                            final BigDecimal sick = KnjDbUtils.getBigDecimal(row, "SICK2", null);
                            final BigDecimal rawReplacedSick = KnjDbUtils.getBigDecimal(row, "RAW_REPLACED_SICK", null);
                            final BigDecimal replacedSick = KnjDbUtils.getBigDecimal(row, "REPLACED_SICK", null);

                            final BigDecimal sick1 = mst._isSaki ? rawReplacedSick : rawSick;
                            final BigDecimal attend = lesson.subtract(null == sick1 ? BigDecimal.valueOf(0) : sick1);
                            final BigDecimal sick2 = mst._isSaki ? replacedSick : sick;

                            final SubclassAttendance sa = new SubclassAttendance(lesson, attend, sick2);

//                            log.debug(" schregno = " + student._schregno + ", sa = " + subclassAttendance);
                            subclass._subclassAttendance = sa;
                        }
                    }

                    for (final Iterator it = KnjDbUtils.query(db2, ps2, new Object[] {student._schregno}).iterator(); it.hasNext();) {
                        final Map row = (Map) it.next();
                        final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");

                        if (null == student._subclassMap.get(subclasscd)) {
                            continue;
                        }
                        final Subclass subclass = student.getSubclass(subclasscd);

                        BigDecimal creditTime = KnjDbUtils.getBigDecimal(row, "CREDIT_TIME", null);
                        if (null != creditTime && creditTime.scale() > 0 && creditTime.subtract(creditTime.setScale(0, BigDecimal.ROUND_DOWN)).doubleValue() == 0.0) {
                            creditTime = creditTime.setScale(0);
                        }
                        SubclassAttendance sa = new SubclassAttendance(null, creditTime, null);
                        if (null != subclass._subclassAttendance) {
                            sa = subclass._subclassAttendance.add(sa);
                        }
                        subclass._subclassAttendance = sa;
                    }
                }

            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                DbUtils.closeQuietly(ps2);
                db2.commit();
            }
        }

        private SubclassAttendance add(final SubclassAttendance sa) {
            if (null == sa) {
                return this;
            }
            final BigDecimal lesson = null == _lesson ? sa._lesson : null == sa._lesson ? _lesson : _lesson.add(sa._lesson);
            final BigDecimal attend = null == _attend ? sa._attend : null == sa._attend ? _attend : _attend.add(sa._attend);
            final BigDecimal sick = null == _sick ? sa._sick : null == sa._sick ? _sick : _sick.add(sa._sick);
            return new SubclassAttendance(lesson, attend, sick);
        }

        private static String getSchAttendDatSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("       T1.SCHREGNO ");
            stb.append("     , CHR.CLASSCD || '-' || CHR.SCHOOL_KIND || '-' || CHR.CURRICULUM_CD || '-' || CHR.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("     , T1.SCHOOLINGKINDCD ");
            stb.append("     , SUM(T1.CREDIT_TIME) AS CREDIT_TIME ");
            stb.append(" FROM SCH_ATTEND_DAT T1 ");
            stb.append(" INNER JOIN SEMESTER_MST SEME ON SEME.YEAR = T1.YEAR ");
            stb.append("                             AND SEME.SEMESTER <> '9' ");
            stb.append("                             AND T1.EXECUTEDATE BETWEEN SEME.SDATE AND SEME.EDATE ");
            stb.append(" INNER JOIN CHAIR_DAT CHR ON CHR.YEAR = T1.YEAR ");
            stb.append("                         AND CHR.SEMESTER = SEME.SEMESTER ");
            stb.append("                         AND CHR.CHAIRCD = T1.CHAIRCD ");
            stb.append(" INNER JOIN SUBCLASS_MST SUB_M ON SUB_M.CLASSCD = CHR.CLASSCD ");
            stb.append("                              AND SUB_M.SCHOOL_KIND = CHR.SCHOOL_KIND ");
            stb.append("                              AND SUB_M.CURRICULUM_CD = CHR.CURRICULUM_CD ");
            stb.append("                              AND SUB_M.SUBCLASSCD = CHR.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND SEME.SEMESTER <= '" + param._ctrlSemester + "' ");
            stb.append("     AND CHR.CLASSCD <= '90' ");
            stb.append("     AND T1.SCHREGNO = ? ");
            stb.append(" GROUP BY ");
            stb.append("       T1.SCHREGNO ");
            stb.append("     , CHR.CLASSCD || '-' || CHR.SCHOOL_KIND || '-' || CHR.CURRICULUM_CD || '-' || CHR.SUBCLASSCD ");
            stb.append("     , T1.SCHOOLINGKINDCD ");
            stb.append(" HAVING ");
            stb.append("     SUM(T1.CREDIT_TIME) IS NOT NULL ");

            return stb.toString();
        }
    }

    private static class SubclassMst implements Comparable {
        final Param _param;
        final String _specialDiv;
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassname;
        final Integer _electdiv;
        boolean _isSaki;
        boolean _isMoto;
        String _calculateCreditFlg;
        SubclassMst _sakikamoku;
        public SubclassMst(final Param param, final String specialDiv, final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final Integer electdiv) {
            _param = param;
            _specialDiv = specialDiv;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassname = subclassname;
            _electdiv = electdiv;
        }
        public int compareTo(final Object o) {
            final SubclassMst os = (SubclassMst) o;
            int rtn;
            rtn = _subclasscd.compareTo(os._subclasscd);
            return rtn;
        }
        public String toString() {
            return "SubclassMst(subclasscd = " + _subclasscd + ")";
        }

        private static Map getSubclassMst(
                final DB2UDB db2,
                final Param param,
                final String year
        ) {
            Map subclassMstMap = new HashMap();
            try {
                String sql = "";
                sql += " SELECT ";
                sql += "   VALUE(T2.SPECIALDIV, '0') AS SPECIALDIV, ";
                sql += "   T1.CLASSCD, ";
                sql += "   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                sql += "   T2.CLASSABBV, T2.CLASSNAME, T1.SUBCLASSABBV, T1.SUBCLASSNAME, ";
                sql += "   VALUE(T1.ELECTDIV, '0') AS ELECTDIV ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                    final Map row = (Map) it.next();
                    final SubclassMst mst = new SubclassMst(param, KnjDbUtils.getString(row, "SPECIALDIV"), KnjDbUtils.getString(row, "CLASSCD"), KnjDbUtils.getString(row, "SUBCLASSCD"), KnjDbUtils.getString(row, "CLASSABBV"), KnjDbUtils.getString(row, "CLASSNAME")
                            , KnjDbUtils.getString(row, "SUBCLASSABBV")
                            , KnjDbUtils.getString(row, "SUBCLASSNAME")
                            , KnjDbUtils.getInt(row, "ELECTDIV", new Integer(999)));
                    subclassMstMap.put(KnjDbUtils.getString(row, "SUBCLASSCD"), mst);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
            try {
                String sql = "";
                sql += " SELECT ";
                sql += " COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, CALCULATE_CREDIT_FLG,  ";
                sql += " ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ";
                sql += " FROM SUBCLASS_REPLACE_COMBINED_DAT ";
                sql += " WHERE YEAR = '" + year + "' ";
                for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                    final Map row = (Map) it.next();

                    final SubclassMst combined = (SubclassMst) subclassMstMap.get(KnjDbUtils.getString(row, "COMBINED_SUBCLASSCD"));
                    final SubclassMst attend = (SubclassMst) subclassMstMap.get(KnjDbUtils.getString(row, "ATTEND_SUBCLASSCD"));
                    if (null != combined && null != attend) {
                        combined._isSaki = true;
                        attend._isMoto = true;
                        combined._calculateCreditFlg = KnjDbUtils.getString(row, "CALCULATE_CREDIT_FLG");
                        attend._sakikamoku = combined;
                    } else {
                        log.warn(" combined = " + combined + ", attend = " + attend + " in " + row);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
            return subclassMstMap;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 69209 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _output;
        final String _gradeHrClass;
        final String[] _categorySelected;
        final String _printDate;
        final String _retestDate;
        final String _moneyPrint;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _usecurriculumcd;
        final String _selectdata;
        final String _printLogStaffcd;
        final String _printLogRemoteIdent;
        final String _printLogRemoteDddr;
        /** 講座名簿の終了日付が学期の終了日付と同一 */
        final String _printSubclassLastChairStd;
        final String _schoolName;
        private Map _subclassMstMap;

        /** 端数計算共通メソッド引数 */
        final Map _attendParamMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _output = request.getParameter("OUTPUT");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _categorySelected = request.getParameterValues("category_selected");
            _printDate = StringUtils.replace(request.getParameter("PRINT_DATE"), "/", "-");
            _retestDate = StringUtils.replace(request.getParameter("RETEST_DATE"), "/", "-");
            _moneyPrint = request.getParameter("MONEY_PRINT");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _usecurriculumcd = request.getParameter("useCurriculumcd");
            _selectdata = request.getParameter("selectdata");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _printLogRemoteIdent = request.getParameter("PRINT_LOG_REMOTE_IDENT");
            _printLogRemoteDddr = request.getParameter("PRINT_LOG_REMOTE_ADDR");
            _printSubclassLastChairStd = request.getParameter("printSubclassLastChairStd");
            _subclassMstMap = SubclassMst.getSubclassMst(db2, this, _ctrlYear);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");

            _schoolName = getSchoolName(db2, _ctrlYear);
        }

        private String getSchoolName(final DB2UDB db2, final String year) {
            String retSchoolName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + year + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    retSchoolName = rs.getString("SCHOOLNAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSchoolName;
        }

        private SubclassMst getSubclassMst(final String subclasscd) {
            if (null == _subclassMstMap.get(subclasscd)) {
                //log.info("科目マスタなし:" + subclasscd);
                return null;
            }
            return (SubclassMst) _subclassMstMap.get(subclasscd);
        }
    }
}

// eof
