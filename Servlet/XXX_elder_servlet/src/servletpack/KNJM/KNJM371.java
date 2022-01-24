/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: b056251b6f12074070c72eb2e2a6b7c750c7669a $
 *
 * 作成日: 2019/01/07
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletException;
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
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJM371 {

    private static final Log log = LogFactory.getLog(KNJM371.class);
    private static final String SEMEALL = "9";

    private boolean _hasData;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        Vrw32alp svf = null;
        try {
            svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            response.setContentType("application/pdf");

            outputPdf(svf, request);

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }

    public void outputPdf(final Vrw32alp svf, final HttpServletRequest request) throws Exception {
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            Param param = createParam(request, db2);

            printMain(db2, svf, param);

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            try {
                db2.commit();
                db2.close();
            } catch (Exception ex) {
                log.error("db close error!", ex);
            }
        }
    }

    protected void printMain(final DB2UDB db2, final Vrw32alp svf, final Param param) {
        final List studentList = Student.getStudentList(db2, param);

        for (int i = 0; i < studentList.size(); i++) {
            final Student student = (Student) studentList.get(i);
            if (param._isOutputDebug) {
                log.info(" student schregno = " + student._schregno);
            }
            svf.VrSetForm("KNJM371.frm", 4);
            if ("1".equals(param._semester)) {
            	if (null != param._whitespaceImagePath) {
            		svf.VrsOut("WHITESPACE", param._whitespaceImagePath);
            	}
            }

            svf.VrsOut("TITLE", "レポート合格・インターネット視聴状況のお知らせ"); // タイトル
            svf.VrsOut("SCHREGNO", student._schregno); // 学籍番号
            svf.VrsOut("HR_NAME", student._hrname); // クラス名
            svf.VrsOut("NAME", student._name); // 氏名
            svf.VrsOut("SCHOOL_NAME", param._knjSchoolMst._schoolName1);
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP_M(db2, param._loginDate));

            final List subclassList = Subclass.getPrintSubclassList(param, student);
            final int record = 17;
            int totalAddRep = 0;
            for (int subi = 0; subi < record; subi++) {
                if (subi < subclassList.size()) {
                    final Subclass subclass = (Subclass) subclassList.get(subi);
                    svf.VrsOut("CLASS_NAME", subclass._mst._classname);
                    svf.VrsOut("SUBCLASS_NAME" + (KNJ_EditEdit.getMS932ByteLength(subclass._mst._subclassname) <= 20 ? "1" : "2"), subclass._mst._subclassname); // 科目名

                    if (param._isOutputDebug) {
                        log.info(" subclass " + subclass._subclasscd + " (zenki,kouki) = (" + (subclass._isZenki ? "1" : "0") + "," + (subclass._isKouki ? "1" : "0") + ") " + subclass._mst._subclassname);
                    }

                    svf.VrsOut("REPORT1", subclass._repoLimitCnt); // レポート規定数
                    svf.VrsOut("REPORT2", subclass._rVal1); // レポート合格数

                    int attendCnt = 0;
                    if (null != subclass._subclassAttendance && null != subclass._subclassAttendance._attend) {
                        attendCnt = subclass._subclassAttendance._attend.intValue();
                    }
                    boolean isPrintTsuikaReport = true;
                    if ("1".equals(param._semester)) {
                    	// 前期は前期科目以外は追加レポート数を出力しない
                    	if (!subclass._isZenki) {
                    		isPrintTsuikaReport = false;
                    	}
                    }
                    if (isPrintTsuikaReport) {
                    	final String setAddRep = getAddRep(param, subclass._repoLimitCnt, subclass._rVal1, subclass._schoolingLimitCnt, attendCnt);
                    	svf.VrsOut("REPORT3", setAddRep); // 追加レポート数
                    	if (!"合格".equals(setAddRep) && !"不合格".equals(setAddRep)) {
                    		totalAddRep += Integer.parseInt(setAddRep);
                    	}
                    }

                    // ネット
                    if ("1".equals(subclass._sVal1)) {
                        svf.VrsOut("NET", "済");
                    } else if ("1".equals(subclass._useMedia1)) {
                        svf.VrsOut("NET", "未");
                    } else {
                        svf.VrsOut("NET", "／");
                    }

                } else {
                    svf.VrsOut("BLANK", String.valueOf(subi));
                    svf.VrsOut("CLASS_NAME", String.valueOf(subi));
                    svf.VrAttribute("CLASS_NAME", "Meido=100");
                }

                svf.VrEndRecord();
                _hasData = true;
            }
            svf.VrsOut("REPORT3_TOTAL", String.valueOf(totalAddRep));
            svf.VrEndRecord();
        }
    }

    private String getAddRep(final Param param, final String repLimit, final String passCnt, final String atendLimit, final int attendCnt) {
        final int rLimit = NumberUtils.isDigits(repLimit) ? Integer.parseInt(repLimit) : 0;
        final int pCnt = NumberUtils.isDigits(passCnt) ? Integer.parseInt(passCnt) : 0;
        final int aLimit = NumberUtils.isDigits(atendLimit) ? Integer.parseInt(atendLimit) : 0;
        final int addRep = rLimit - pCnt;

//        if (aLimit > attendCnt) {
//            return "不合格";
//        }
        if ("1".equals(param._semester) && aLimit > attendCnt) { // 2020/1/17 前期のみとする(とりあえずの対応)
            return "不合格";
        }
        final int rLimitHarf = new BigDecimal(rLimit).divide(new BigDecimal(2), 0, BigDecimal.ROUND_HALF_UP).intValue();
        if (rLimitHarf < addRep) {
            return "不合格";
        }
        if (rLimit <= pCnt) {
            return "合格";
        }
        return String.valueOf(addRep);
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
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST SEMEG ON SEMEG.YEAR = '" + param._year + "' AND SEMEG.SEMESTER = REGD.SEMESTER AND SEMEG.GRADE = REGD.GRADE ");
            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
            stb.append("                  AND W3.GRD_DATE < CASE WHEN SEMEG.EDATE < '" + param._date + "' THEN SEMEG.EDATE ELSE '" + param._date + "' END ");
            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
            stb.append("                  AND W4.ENT_DATE > CASE WHEN SEMEG.EDATE < '" + param._date + "' THEN SEMEG.EDATE ELSE '" + param._date + "' END ");
            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
            stb.append("                  AND CASE WHEN SEMEG.EDATE < '" + param._date + "' THEN SEMEG.EDATE ELSE '" + param._date + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = REGD.YEAR ");
            stb.append("                  AND HDAT.SEMESTER = REGD.SEMESTER ");
            stb.append("                  AND HDAT.GRADE = REGD.GRADE ");
            stb.append("                  AND HDAT.HR_CLASS = REGD.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR ");
            stb.append("                  AND GDAT.GRADE = REGD.GRADE ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN STAFF_MST STFM ON STFM.STAFFCD = HDAT.TR_CD1 ");
            stb.append("     LEFT JOIN STAFF_MST STFM2 ON STFM2.STAFFCD = HDAT.TR_CD2 ");
            stb.append("     LEFT JOIN MAJOR_MST MAJ ON MAJ.COURSECD = REGD.COURSECD ");
            stb.append("                  AND MAJ.MAJORCD = REGD.MAJORCD ");
            stb.append("     LEFT JOIN COURSECODE_MST CCM ON CCM.COURSECODE = REGD.COURSECODE ");
            stb.append("     WHERE   REGD.YEAR = '" + param._year + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
            if ("1".equals(param._output)) {
                stb.append("    AND REGD.GRADE || REGD.HR_CLASS = '" + param._gradeHrclass + "' ");
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
            _repoLimitCnt = repoLimitCnt;
            _schoolingLimitCnt = schoolingLimitCnt;
            _useMedia1 = useMedia1;
            _retestPassScore = retestPassScore;
            _rVal1 = rVal1;
            _tVal1 = tVal1;
            _tVal1Name1 = tVal1Name1;
            _sVal1 = sVal1;
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
            if (param._isOutputDebug) {
                log.info(" subclass sql = " + sql);
            }
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
                    } else if ("2".equals(takeSemes)) {
                        subclass._isKouki = true;
                    }
                }

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
            if (param._notPrintZenki) {
                stb.append("                         AND VALUE(L2.TAKESEMES, '0') <> '1' ");
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
            if ("1".equals(param._printSubclassLastChairStd)) {
                stb.append("     INNER JOIN SEMESTER_MST STDSEME ON STDSEME.YEAR = L1.YEAR ");
                stb.append("       AND STDSEME.SEMESTER = L1.SEMESTER ");
                stb.append("       AND STDSEME.EDATE = L1.APPENDDATE ");
            }
            stb.append(" WHERE ");
            stb.append("    T1.YEAR = '" + param._year + "' ");
            stb.append("    AND T1.SEMESTER = '" + param._semester + "' ");
            if ("1".equals(param._output)) {
                stb.append("    AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrclass + "' ");
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
            stb.append("    AND M006.NAMECD2 = CAST(L5T.VAL_NUMERIC AS VARCHAR(1)) ");
            stb.append(" WHERE ");
            stb.append("   T1.SUBCLASSCD NOT LIKE '8%' AND T1.CLASSCD <= '90' ");
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
                        param._year,
                        SEMEALL,
                        null,
                        param._date,
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
            if (param._notPrintZenki) {
                stb.append("                         AND VALUE(CHR.TAKESEMES, '0') <> '1' ");
            }
            stb.append(" INNER JOIN SUBCLASS_MST SUB_M ON SUB_M.CLASSCD = CHR.CLASSCD ");
            stb.append("                              AND SUB_M.SCHOOL_KIND = CHR.SCHOOL_KIND ");
            stb.append("                              AND SUB_M.CURRICULUM_CD = CHR.CURRICULUM_CD ");
            stb.append("                              AND SUB_M.SUBCLASSCD = CHR.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND SEME.SEMESTER <= '" + param._semester + "' ");
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
                if (param._isOutputDebug) {
                    log.info(" repl sub sql = " + sql);
                }
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
    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        final Param param = new Param(request, db2);
        log.fatal("$Revision: 71813 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }


    private static class Param {
        final String _year;
        final String _semester;
        final String _output;
        final String _loginDate;

        final String _gradeHrclass;
        final String[] _categorySelected;
        /** 出欠集計日付 */
        final String _date;
        final boolean _notPrintZenki;
        /** 講座名簿の終了日付が学期の終了日付と同一 */
        final String _printSubclassLastChairStd;

        final String _documentroot;
        final String _imagePath;
        final String _whitespaceImagePath;
        final boolean _isOutputDebug;

        private Map _subclassMstMap;

        /** 端数計算共通メソッド引数 */
        final Map _attendParamMap;

        private KNJSchoolMst _knjSchoolMst;

        private final DecimalFormat _df;

        final String _nendo;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _output = request.getParameter("OUTPUT");
            _loginDate = request.getParameter("CTRL_DATE");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _categorySelected = request.getParameterValues("category_selected");
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _notPrintZenki = "1".equals(request.getParameter("NOT_PRINT_ZENKI"));
            _printSubclassLastChairStd = request.getParameter("printSubclassLastChairStd");

            _documentroot = request.getParameter("DOCUMENTROOT");
            _imagePath = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' "));
            _whitespaceImagePath = getImagePath("whitespace.png");

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_year));

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }

            _subclassMstMap = SubclassMst.getSubclassMst(db2, this, _year);

            _df = null != _knjSchoolMst && ("3".equals(_knjSchoolMst._absentCov) || "4".equals(_knjSchoolMst._absentCov)) ? new DecimalFormat("0.0") : new DecimalFormat("0");

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJM371' AND NAME = '" + propName + "' "));
        }

        private SubclassMst getSubclassMst(final String subclasscd) {
            if (null == _subclassMstMap.get(subclasscd)) {
                //log.info("科目マスタなし:" + subclasscd);
                return null;
            }
            return (SubclassMst) _subclassMstMap.get(subclasscd);
        }
        
        public String getImagePath(final String filename) {
            final String path = _documentroot + "/" + (null == _imagePath ? "" : _imagePath + "/") + filename;
            final File file = new File(path);
            if (!file.exists() || _isOutputDebug) {
                log.info(" file " + file.getPath() + " exists? " + file.exists());
            }
            if (!file.exists()) {
            	return null;
            }
            return file.getPath();
        }
    }
}

// eof
