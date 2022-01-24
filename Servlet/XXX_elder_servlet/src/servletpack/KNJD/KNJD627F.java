/*
 * $Id$
 *
 * 作成日: 2021/01/22
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

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

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 駿台甲府中学　再試結果
 */
public class KNJD627F {

    private static final Log log = LogFactory.getLog(KNJD627F.class);
    private static final String SEMEALL = "9";
    private static final String CLSFULL = "ZZ";
    private static final int DANGERCREDITLINE = 74;
    private static final int DANGERSOUTEN_ONEYEARLINE = 1800;
    private static final int DANGERSOUTENLINE = DANGERSOUTEN_ONEYEARLINE * 3;
    private static final int DANGERENGTESTLINE = 200;
    private boolean _hasData;

    private static final String GET_TESTCD = "9-990008";

    private static final int FUSOKU_FAIL_BIT = 0x200;
    private static final int ENGLISH_FAIL_BIT = 0x100;
    private static final int SOUTEN_NULL_BIT = 0x080;
    private static final int D1_NULL_BIT = 0x040;
    private static final int D2_NULL_BIT = 0x020;
    private static final int D3_NULL_BIT = 0x010;
    private static final int SOUTEN_FAIL_BIT = 0x008;
    private static final int D1_FAIL_BIT = 0x004;
    private static final int D2_FAIL_BIT = 0x002;
    private static final int D3_FAIL_BIT = 0x001;

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
        if ("2".equals(_param._printTargetKind)) {
            printMain2(db2, svf);
        } else {
            printMain1(db2, svf);
        }
    }
    private void printMain2(final DB2UDB db2, final Vrw32alp svf) {
        final String form = "KNJD627F_2.frm";
        final Map studentsMap = getStudent(db2);
        final Map creditSoutenMap = getSouten(db2);

        for (Iterator ite = studentsMap.keySet().iterator();ite.hasNext();) {
            boolean fstPrtFlg = false;
            final String grHrCd = (String)ite.next();
            final Map students = (Map)studentsMap.get(grHrCd);
            for (Iterator its = students.keySet().iterator();its.hasNext();) {
                final String schregno = (String)its.next();
                final Souten souten = creditSoutenMap.containsKey(schregno) ? (Souten)creditSoutenMap.get(schregno) : null;
                final Student stu = (Student)students.get(schregno);
                final int jdgSuisen = judgeSuisen(souten);
                if (jdgSuisen <= 0) {
                    continue;
                }
                if (!fstPrtFlg) {
                    svf.VrSetForm(form, 4);
                    setTitle(db2, svf, null);
                    fstPrtFlg = true;
                }
                //番号
                svf.VrsOut("NO", stu._hr_Class_Name1 + stu._attendno);
                //氏名
                final int nlen = KNJ_EditEdit.getMS932ByteLength(stu._name);
                final String nfield = nlen > 30 ? "3" : nlen > 20 ? "2" : "1";
                svf.VrsOut("NAME" + nfield, stu._name);
                //コース名
                final int clen = KNJ_EditEdit.getMS932ByteLength(stu._courseAbbv);
                final String cfield = clen > 14 ? "3" : clen > 10 ? "2" : "1";
                svf.VrsOut("COURSE_NAME" + cfield, stu._courseAbbv);
                if (souten != null) {
                    String chkStr = "";
                    //総計
                    chkStr = (jdgSuisen & SOUTEN_NULL_BIT) > 0 ? "" : (jdgSuisen & SOUTEN_FAIL_BIT) > 0 ? "*" : "";
                    svf.VrsOut("TOTAL_SCORE", chkStr + StringUtils.defaultString(souten._souten, ""));
                    //不足点
                    chkStr = (jdgSuisen & SOUTEN_NULL_BIT) > 0 ? "" : (jdgSuisen & FUSOKU_FAIL_BIT) > 0 ? "*" : "";
                    svf.VrsOut("LACK", chkStr + StringUtils.defaultString(souten._fusokuten, ""));
                    //1年
                    chkStr = Integer.parseInt(souten._d1Val) < 0 ? "*" : "";
                    svf.VrsOut("SCORE1", chkStr + StringUtils.defaultString(souten._d1Val, ""));
                    //2年
                    chkStr = Integer.parseInt(souten._d2Val) < 0 ? "*" : "";
                    svf.VrsOut("SCORE2", chkStr + StringUtils.defaultString(souten._d2Val, ""));
                    //3年
                    chkStr = Integer.parseInt(souten._d3Val) < 0 ? "*" : "";
                    svf.VrsOut("SCORE3", chkStr + StringUtils.defaultString(souten._d3Val, ""));
                    //英語判定
                    svf.VrsOut("ENGLISH", StringUtils.defaultString(souten._rank, ""));
                    //未履修科目(1年)
                    final int nplen1 = KNJ_EditEdit.getMS932ByteLength(souten._npRemark1);
                    final String npfield1 = nplen1 > 40 ? "2" : "1";
                    svf.VrsOutn("FAULT_SUBCLASS_NAME" + npfield1, 1, StringUtils.defaultString(souten._npRemark1, ""));
                    //未履修科目(2年)
                    final int nplen2 = KNJ_EditEdit.getMS932ByteLength(souten._npRemark2);
                    final String npfield2 = nplen2 > 40 ? "2" : "1";
                    svf.VrsOutn("FAULT_SUBCLASS_NAME" + npfield2, 2, StringUtils.defaultString(souten._npRemark2, ""));
                    //未履修科目(3年)
                    final int nplen3 = KNJ_EditEdit.getMS932ByteLength(souten._npRemark3);
                    final String npfield3 = nplen3 > 40 ? "2" : "1";
                    svf.VrsOutn("FAULT_SUBCLASS_NAME" + npfield3, 3, StringUtils.defaultString(souten._npRemark3, ""));
                }
                svf.VrEndRecord();
                _hasData = true;
            }
            if (fstPrtFlg) {
                svf.VrEndPage();
            }
        }

    }

    private int judgeSuisen(final Souten souten) {
        int retVal = -1;  //soutenがnullなら-1で返してcontinueさせる。
        if (souten != null) {
            retVal = 0;
            if ("".equals(StringUtils.defaultString(souten._souten, ""))) {
                retVal += SOUTEN_NULL_BIT;
            } else if (DANGERSOUTENLINE >= Integer.parseInt(souten._souten)) {
                retVal += SOUTEN_FAIL_BIT;
            }
            if ((retVal & SOUTEN_NULL_BIT) <= 0) {
                if (Integer.parseInt(souten._fusokuten) < 0) {
                    retVal += FUSOKU_FAIL_BIT;
                }
            }
            if (!"".equals(souten._rank) && !"1".equals(souten._judge)) {
                retVal += ENGLISH_FAIL_BIT;
            }
        }
        return retVal;
    }
    private void printMain1(final DB2UDB db2, final Vrw32alp svf) {
        final String form = "KNJD627F_1.frm";
        final Map studentsMap = getStudent(db2);
        final Map creditSoutenMap = getCreditSouten(db2);

        for (Iterator ite = studentsMap.keySet().iterator();ite.hasNext();) {
            final String grHrCd = (String)ite.next();
            final Map students = (Map)studentsMap.get(grHrCd);
            final Map clsCdMap = getSammaryClsCd(students, creditSoutenMap);
            boolean fstPrtFlg = false;
            for (Iterator its = students.keySet().iterator();its.hasNext();) {
                final String schregno = (String)its.next();
                final Map crdSouMap = creditSoutenMap.containsKey(schregno) ? (Map)creditSoutenMap.get(schregno) : null;
                final Student stu = (Student)students.get(schregno);
                if (!chkDangerLine(clsCdMap, crdSouMap)) { //出力対象生徒か、チェックする。_chkClsCdMap
                    continue;
                }
                if (!fstPrtFlg) {
                    svf.VrSetForm(form, 4);
                    setTitle(db2, svf, clsCdMap);
                    fstPrtFlg = true;
                }
                //番号
                svf.VrsOut("NO", stu._hr_Class_Name1 + stu._attendno);
                //氏名
                final int nlen = KNJ_EditEdit.getMS932ByteLength(stu._name);
                final String nfield = nlen > 30 ? "3" : nlen > 20 ? "2" : "1";
                svf.VrsOut("NAME" + nfield, stu._name);
                //コース名
                final int clen = KNJ_EditEdit.getMS932ByteLength(stu._courseAbbv);
                final String cfield = clen > 14 ? "3" : clen > 10 ? "2" : "1";
                svf.VrsOut("COURSE_NAME" + cfield, stu._courseAbbv);
                if (crdSouMap != null) {
                    int clsIdx = 0;
                    for (Iterator itc = clsCdMap.keySet().iterator();itc.hasNext();) {
                        final String clsCd = (String)itc.next();
                        clsIdx++;
                        CreditSouten prtWk = crdSouMap.containsKey(clsCd) ? (CreditSouten)crdSouMap.get(clsCd) : null;
                        if (prtWk != null) {
                            //単位
                            svf.VrsOutn("SCORE", clsIdx, prtWk._credits);
                        }
                    }
                    if (crdSouMap.containsKey(CLSFULL)) {
                        CreditSouten prtWk = (CreditSouten)crdSouMap.get(CLSFULL);
                        //修得単位
                        final String chkStr = "".equals(StringUtils.defaultString(prtWk._credits, "")) ? "" : DANGERCREDITLINE >= Integer.parseInt(prtWk._credits) ? "*" : "";
                        svf.VrsOut("CREDIT", chkStr + prtWk._credits);
                        //備考
                        final int nplen1 = KNJ_EditEdit.getMS932ByteLength(prtWk._npRemark1);
                        final String npfield1 = nplen1 > 40 ? "2" : "1";
                        svf.VrsOutn("FAULT_SUBCLASS_NAME" + npfield1, 1, prtWk._npRemark1);
                        final int nplen2 = KNJ_EditEdit.getMS932ByteLength(prtWk._npRemark2);
                        final String npfield2 = nplen2 > 40 ? "2" : "1";
                        svf.VrsOutn("FAULT_SUBCLASS_NAME" + npfield2, 2, prtWk._npRemark2);
                        final int nplen3 = KNJ_EditEdit.getMS932ByteLength(prtWk._npRemark3);
                        final String npfield3 = nplen3 > 40 ? "2" : "1";
                        svf.VrsOutn("FAULT_SUBCLASS_NAME" + npfield3, 3, prtWk._npRemark3);
                        //総点
                        svf.VrsOut("TOTAL_SCORE", prtWk._souten);
                    }
                }
                svf.VrEndRecord();
                _hasData = true;
            }
            svf.VrEndPage();
        }
    }

    private boolean chkDangerLine(final Map clsCdMap, final Map crdSouMap) {
        boolean retBl = false;
        if (crdSouMap != null) {
            for (Iterator itc = clsCdMap.keySet().iterator();itc.hasNext();) {
                final String clsCd = (String)itc.next();
                CreditSouten prtWk = crdSouMap.containsKey(clsCd) ? (CreditSouten)crdSouMap.get(clsCd) : null;
                if (prtWk != null) {
                    //単位
                    if ("1".equals(prtWk._chkStar)) {
                        retBl = true;
                    }
                }
            }
            if (crdSouMap.containsKey(CLSFULL)) {
                CreditSouten prtWk = (CreditSouten)crdSouMap.get(CLSFULL);
                if (DANGERCREDITLINE > Integer.parseInt(prtWk._credits)) {
                    retBl = true;
                }
            }
        }
        return retBl;
    }
    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final Map clsCdMap) {
        //年度
        svf.VrsOut("NENDO", KNJ_EditDate.h_format_Seireki_N(_param._ctrlDate)+"度卒業生");
        //年生
        svf.VrsOut("GRADE_NAME", "高校" + StringUtils.defaultString(_param._gradeName, ""));
        //科目タイトル

        if ("1".equals(_param._printTargetKind) && clsCdMap != null) {
            int clsIdx = 0;
            for (Iterator itc = clsCdMap.keySet().iterator();itc.hasNext();) {
                final String clsCd = (String)itc.next();
                clsIdx++;
                final String clsAbbv = (String)clsCdMap.get(clsCd);
                final int clen = KNJ_EditEdit.getMS932ByteLength(clsAbbv);
                final String cfield = clen > 4 ? "2" : "1";
                svf.VrsOutn("SUBCLASS_NAME" + cfield, clsIdx, StringUtils.defaultString(clsAbbv));
            }
        }
    }

    private Map getSammaryClsCd(final Map students, final Map creditSoutenMap) {
        final Map retMap = new TreeMap();
        for (Iterator ite = students.keySet().iterator();ite.hasNext();) {
            final String schregno = (String)ite.next();
            if (creditSoutenMap.containsKey(schregno)) {
                final Map clsChkObj = (Map)creditSoutenMap.get(schregno);
                for (Iterator itc = clsChkObj.keySet().iterator();itc.hasNext();) {
                    final String clsCd = (String)itc.next();
                    if (!retMap.containsKey(clsCd) && !CLSFULL.equals(clsCd)) {
                        retMap.put(clsCd, (_param._clsCdMap.containsKey(clsCd) ? (String)_param._clsCdMap.get(clsCd) : ""));
                    }
                }
            }
        }
        return retMap;
    }

    private Map getStudent(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        Map subMap = new LinkedMap();
        final String sql = getStudentSql();
        log.debug(" sql = " + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String hr_Class = rs.getString("HR_CLASS");
                final String hr_Class_Name1 = rs.getString("HR_CLASS_NAME1");
                final String courseCode = rs.getString("COURSECODE");
                final String courseAbbv = rs.getString("COURSECODEABBV1");
                final String attendno = rs.getString("ATTENDNO");
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final Student addWk = new Student(grade, hr_Class, hr_Class_Name1, courseCode, courseAbbv, attendno, schregno, name);
                final String fstKey = grade + hr_Class;
                if (!retMap.containsKey(fstKey)) {
                    subMap = new LinkedMap();
                    retMap.put(fstKey, subMap);
                } else {
                    subMap = (Map)retMap.get(fstKey);
                }
                subMap.put(schregno, addWk);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T3.HR_CLASS_NAME1, ");
        stb.append("     T1.COURSECODE, ");
        stb.append("     TC.COURSECODEABBV1, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T2.NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST T2 ");
        stb.append("       ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT T3 ");
        stb.append("       ON T3.YEAR = T1.YEAR ");
        stb.append("      AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("      AND T3.GRADE = T1.GRADE ");
        stb.append("      AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append("     LEFT JOIN V_COURSECODE_MST  TC ");
        stb.append("       ON TC.YEAR = T1.YEAR ");
        stb.append("      AND TC.COURSECODE = T1.COURSECODE ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' AND ");
        stb.append("     T1.SEMESTER = '" + _param._semester  + "' AND ");
        stb.append("     T1.GRADE = '" + _param._grade + "' ");
        return stb.toString();
    }

    private class Student {
        final String _grade;
        final String _hr_Class;
        final String _hr_Class_Name1;
        final String _courseCode;
        final String _courseAbbv;
        final String _attendno;
        final String _schregno;
        final String _name;
        public Student (final String grade, final String hr_Class, final String hr_Class_Name1, final String courseCode, final String courseAbbv, final String attendno, final String schregno, final String name)
        {
            _grade = grade;
            _hr_Class = hr_Class;
            _hr_Class_Name1 = hr_Class_Name1;
            _courseCode = courseCode;
            _courseAbbv = courseAbbv;
            _attendno = attendno;
            _schregno = schregno;
            _name = name;
        }
    }

    private Map getCreditSouten(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        Map subMap = new LinkedMap();
        final String sql = getCreditSoutenSql();
        log.debug(" sql = " + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String classcd = rs.getString("CLASSCD");
                final String schregno = rs.getString("SCHREGNO");
                final String school_Kind = rs.getString("SCHOOL_KIND");
                final String souten = rs.getString("SOUTEN");
                final String credits = rs.getString("T_CREDITS");
                final String chkStar = rs.getString("CHK_STAR");
                final String npRemark1 = rs.getString("NPREMARK1");
                final String npRemark2 = rs.getString("NPREMARK2");
                final String npRemark3 = rs.getString("NPREMARK3");
                final CreditSouten addWk = new CreditSouten(classcd, schregno, school_Kind, souten, credits, chkStar, npRemark1, npRemark2, npRemark3);
                final String fstKey = schregno;
                if (!retMap.containsKey(fstKey)) {
                    subMap = new LinkedMap();
                    retMap.put(fstKey, subMap);
                } else {
                    subMap = (Map)retMap.get(fstKey);
                }
                subMap.put(classcd, addWk);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getCreditSoutenSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH CALC_PASSFLG AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T3.CLASSCD, ");
        stb.append("     T3.SCHOOL_KIND, ");
        stb.append("     T3.CURRICULUM_CD, ");
        stb.append("     T3.SUBCLASSCD, ");
        stb.append("     T3.SCORE, ");
        stb.append("     CASE WHEN T4.PASS_SCORE <= T3.SCORE THEN '1' ELSE '0' END AS PASSFLG, ");
        stb.append("     VALUE(T5.CREDITS, 0) AS CREDITS ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT T2 ");
        stb.append("       ON T2.YEAR = T1.YEAR ");
        stb.append("      AND T2.GRADE = T1.GRADE ");
        stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT T3 ");
        stb.append("       ON T3.YEAR = T1.YEAR ");
        stb.append("      AND T3.SEMESTER || '-' || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV = '" + GET_TESTCD + "' ");
        stb.append("      AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN PERFECT_RECORD_SDIV_DAT T4 ");
        stb.append("       ON T4.YEAR = T3.YEAR ");
        stb.append("      AND T4.SEMESTER = T3.SEMESTER ");
        stb.append("      AND T4.TESTKINDCD = T3.TESTKINDCD ");
        stb.append("      AND T4.TESTITEMCD = T3.TESTITEMCD ");
        stb.append("      AND T4.SCORE_DIV = T3.SCORE_DIV ");
        stb.append("      AND T4.CLASSCD = T3.CLASSCD ");
        stb.append("      AND T4.SCHOOL_KIND =T3.SCHOOL_KIND ");
        stb.append("      AND T4.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("      AND T4.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append("      AND DIV = '01' "); //DIVの値に合わせて結合は変化するので注意。
        stb.append("     LEFT JOIN CREDIT_MST T5 ");
        stb.append("       ON T5.YEAR = T1.YEAR ");
        stb.append("      AND T5.COURSECD = T1.COURSECD ");
        stb.append("      AND T5.MAJORCD = T1.MAJORCD ");
        stb.append("      AND T5.COURSECODE =  T1.COURSECODE ");
        stb.append("      AND T5.GRADE = T1.GRADE ");
        stb.append("      AND T5.CLASSCD = T3.CLASSCD ");
        stb.append("      AND T5.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("      AND T5.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("      AND T5.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR BETWEEN '" + String.valueOf(Integer.parseInt(_param._year) - 2) + "' AND '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T2.GRADE_CD BETWEEN '01' AND '03' ");
        stb.append("     AND T2.SCHOOL_KIND = 'H' ");
        stb.append("     AND T3.SUBCLASSCD IS NOT NULL  AND T4.PASS_SCORE IS NOT NULL ");
        stb.append("     AND T3.SUBCLASSCD NOT IN ('333333', '555555', '999999', '99999A', '99999B') ");  //合計科目コードは除外
        stb.append(" ), NOPASS_SUBCLS AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     SM.SUBCLASSABBV ");
        stb.append(" FROM ");
        stb.append("     CALC_PASSFLG T1 ");
        stb.append("     LEFT JOIN V_SUBCLASS_MST SM ");
        stb.append("       ON SM.YEAR = T1.YEAR ");
        stb.append("      AND SM.CLASSCD = T1.CLASSCD ");
        stb.append("      AND SM.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("      AND SM.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("      AND SM.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.PASSFLG = '0' ");  //単位取得できなかった物を抽出
        stb.append(" ), MAKE_REMARKS AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     LISTAGG(NS.SUBCLASSABBV, ',') AS REMARKS ");
        stb.append(" FROM ");
        stb.append("     CALC_PASSFLG T1 ");
        stb.append("     LEFT JOIN V_CLASS_MST VC ");
        stb.append("       ON VC.YEAR = T1.YEAR ");
        stb.append("      AND VC.CLASSCD = T1.CLASSCD ");
        stb.append("      AND VC.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("     LEFT JOIN NOPASS_SUBCLS NS ");
        stb.append("       ON NS.SCHREGNO = T1.SCHREGNO ");
        stb.append("      AND NS.CLASSCD = T1.CLASSCD ");
        stb.append("      AND NS.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("      AND NS.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("      AND NS.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.PASSFLG = '0' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO ");
        // 教科別の判定をするために、一度利用データを確定させる。
        stb.append(" ), RETBASE_DATA AS (");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     SUM(VALUE(T1.SCORE, 0) * VALUE(T1.CREDITS, 0)) AS SOUTEN, ");
        stb.append("     SUM(CASE WHEN T1.PASSFLG = '1' THEN VALUE(T1.CREDITS, 0) ELSE 0 END) AS T_CREDITS, ");
        stb.append("     '' AS NPREMARK1, ");
        stb.append("     '' AS NPREMARK2, ");
        stb.append("     '' AS NPREMARK3 ");
        stb.append(" FROM ");
        stb.append("     CALC_PASSFLG T1 ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     '" + CLSFULL + "' AS CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     SUM(VALUE(T1.SCORE, 0) * VALUE(T1.CREDITS, 0)) AS SOUTEN, ");
        stb.append("     SUM(CASE WHEN T1.PASSFLG = '1' THEN VALUE(T1.CREDITS, 0) ELSE 0 END) AS T_CREDITS, ");
        stb.append("     MR1.REMARKS AS NPREMARK1, ");
        stb.append("     MR2.REMARKS AS NPREMARK2, ");
        stb.append("     MR3.REMARKS AS NPREMARK3 ");
        stb.append(" FROM ");
        stb.append("     CALC_PASSFLG T1 ");
        stb.append("     LEFT JOIN MAKE_REMARKS MR1 ");
        stb.append("       ON MR1.SCHREGNO = T1.SCHREGNO ");
        stb.append("      AND MR1.YEAR = '" + String.valueOf(Integer.parseInt(_param._year) - 2) + "' ");
        stb.append("     LEFT JOIN MAKE_REMARKS MR2 ");
        stb.append("       ON MR2.SCHREGNO = T1.SCHREGNO ");
        stb.append("      AND MR2.YEAR = '" + String.valueOf(Integer.parseInt(_param._year) - 1) + "' ");
        stb.append("     LEFT JOIN MAKE_REMARKS MR3 ");
        stb.append("       ON MR3.SCHREGNO = T1.SCHREGNO ");
        stb.append("      AND MR3.YEAR = '" + _param._year + "' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     MR1.REMARKS, ");
        stb.append("     MR2.REMARKS, ");
        stb.append("     MR3.REMARKS ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T1.*, ");
        stb.append("   CASE WHEN (D115.NAMECD1 IS NOT NULL AND VALUE(D115.NAME2, 0) > VALUE(T1.T_CREDITS, 0)) THEN 1 ELSE 0 END AS CHK_STAR ");
        stb.append(" FROM ");
        stb.append("   RETBASE_DATA T1");
        stb.append("   LEFT JOIN V_NAME_MST D115 ON D115.YEAR = '" + _param._year + "' AND D115.NAMECD1 = 'D115' AND D115.NAME1 = T1.CLASSCD || '-' || T1.SCHOOL_KIND ");
        stb.append(" ORDER BY ");
        stb.append("     SCHREGNO, ");
        stb.append("     CLASSCD ");
        return stb.toString();
    }

    private class CreditSouten {
        final String _classcd;
        final String _schregno;
        final String _school_Kind;
        final String _souten;
        final String _credits;
        final String _chkStar;
        final String _npRemark1;
        final String _npRemark2;
        final String _npRemark3;
        public CreditSouten (final String classcd, final String schregno, final String school_Kind, final String souten, final String credits, final String chkStar, final String npRemark1, final String npRemark2, final String npRemark3)
        {
            _classcd = classcd;
            _schregno = schregno;
            _school_Kind = school_Kind;
            _souten = souten;
            _credits = credits;
            _chkStar = chkStar;
            _npRemark1 = npRemark1;
            _npRemark2 = npRemark2;
            _npRemark3 = npRemark3;
        }
    }

    private Map getNotPassSubcls(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        List subList = null;
        final String sql = getNotPassSubclsSql();
        log.debug(" sql = " + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String year = rs.getString("YEAR");
                final String schregno = rs.getString("SCHREGNO");
                final String remark = rs.getString("REMARK");
                final NotPassSubcls addWk = new NotPassSubcls(year, schregno, remark);
                final String fstKey = schregno;
                if (!retMap.containsKey(fstKey)) {
                    subList = new ArrayList();
                    retMap.put(fstKey, subList);
                } else {
                    subList = (List)retMap.get(fstKey);
                }
                subList.add(addWk);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getNotPassSubclsSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH CALC_PASSFLG AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T3.CLASSCD, ");
        stb.append("     T3.SCHOOL_KIND, ");
        stb.append("     T3.CURRICULUM_CD, ");
        stb.append("     T3.SUBCLASSCD, ");
        stb.append("     T3.SCORE, ");
        stb.append("     CASE WHEN T4.PASS_SCORE <= T3.SCORE THEN '1' ELSE '0' END AS PASSFLG, ");
        stb.append("     VALUE(T5.CREDITS, 0) AS CREDITS ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT T2 ");
        stb.append("       ON T2.YEAR = T1.YEAR ");
        stb.append("      AND T2.GRADE = T1.GRADE ");
        stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT T3 ");
        stb.append("       ON T3.YEAR = T1.YEAR ");
        stb.append("      AND T3.SEMESTER || '-' || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV = '" + GET_TESTCD + "' ");
        stb.append("      AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN PERFECT_RECORD_SDIV_DAT T4 ");
        stb.append("       ON T4.YEAR = T3.YEAR ");
        stb.append("      AND T4.SEMESTER = T3.SEMESTER ");
        stb.append("      AND T4.TESTKINDCD = T3.TESTKINDCD ");
        stb.append("      AND T4.TESTITEMCD = T3.TESTITEMCD ");
        stb.append("      AND T4.SCORE_DIV = T3.SCORE_DIV ");
        stb.append("      AND T4.CLASSCD = T3.CLASSCD ");
        stb.append("      AND T4.SCHOOL_KIND =T3.SCHOOL_KIND ");
        stb.append("      AND T4.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("      AND T4.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append("      AND DIV = '1' "); //DIVの値に合わせて結合は変化するので注意。
        stb.append("     LEFT JOIN CREDIT_MST T5 ");
        stb.append("       ON T5.YEAR = T1.YEAR ");
        stb.append("      AND T5.COURSECD = T1.COURSECD ");
        stb.append("      AND T5.MAJORCD = T1.MAJORCD ");
        stb.append("      AND T5.COURSECODE =  T1.COURSECODE ");
        stb.append("      AND T5.GRADE = T1.GRADE ");
        stb.append("      AND T5.CLASSCD = T3.CLASSCD ");
        stb.append("      AND T5.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("      AND T5.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("      AND T5.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR BETWEEN '" + String.valueOf(Integer.parseInt(_param._year) - 2) + "' AND '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = (SELECT MAX(SEMESTER) FROM SEMESTER_MST TS WHERE TS.YEAR = T1.YEAR AND SEMESTER < '" + SEMEALL + "') ");
        stb.append("     AND T2.GRADE_CD BETWEEN '01' AND '03' ");
        stb.append("     AND T2.SCHOOL_KIND = 'H' ");
        stb.append("     AND T3.SUBCLASSCD IS NOT NULL AND T4.PASS_SCORE IS NOT NULL ");
        stb.append(" ORDER BY ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T3.CLASSCD, ");
        stb.append("     T3.SCHOOL_KIND, ");
        stb.append("     T3.CURRICULUM_CD, ");
        stb.append("     T3.SUBCLASSCD ");
        stb.append(" ), NOPASS_SUBCLS AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     SM.SUBCLASSABBV ");
        stb.append(" FROM ");
        stb.append("     CALC_PASSFLG T1 ");
        stb.append("     LEFT JOIN V_SUBCLASS_MST SM ");
        stb.append("       ON SM.YEAR = T1.YEAR ");
        stb.append("      AND SM.CLASSCD = T1.CLASSCD ");
        stb.append("      AND SM.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("      AND SM.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("      AND SM.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.PASSFLG = '0' ");  //単位取得できなかった物を抽出
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     LISTAGG(NS.SUBCLASSABBV, ',') AS REMARKS ");
        stb.append(" FROM ");
        stb.append("     CALC_PASSFLG T1 ");
        stb.append("     LEFT JOIN V_CLASS_MST VC ");
        stb.append("       ON VC.YEAR = T1.YEAR ");
        stb.append("      AND VC.CLASSCD = T1.CLASSCD ");
        stb.append("      AND VC.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("     LEFT JOIN NOPASS_SUBCLS NS ");
        stb.append("       ON NS.SCHREGNO = T1.SCHREGNO ");
        stb.append("      AND NS.CLASSCD = T1.CLASSCD ");
        stb.append("      AND NS.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("      AND NS.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("      AND NS.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.PASSFLG = '0' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO ");
        return stb.toString();
    }

    private class NotPassSubcls {
        final String _year;
        final String _schregno;
        final String _remark;
        public NotPassSubcls (final String year, final String schregno, final String remark)
        {
            _year = year;
            _schregno = schregno;
            _remark = remark;
        }
    }

    private Map getSouten(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        final String sql = getSoutenSql();
        log.debug(" sql = " + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String souten = rs.getString("SOUTEN");
                final String fusokuten = rs.getString("FUSOKUTEN");
                final String d1Val = rs.getString("D1Val");
                final String d2Val = rs.getString("D2Val");
                final String d3Val = rs.getString("D3Val");
                final String engTestScore = rs.getString("ENG_TEST_SCORE");
                final String rank = rs.getString("RANK");
                final String judge = rs.getString("JUDGE");
                final String npRemark1 = rs.getString("NPREMARK1");
                final String npRemark2 = rs.getString("NPREMARK2");
                final String npRemark3 = rs.getString("NPREMARK3");
                final Souten addWk = new Souten(schregno, souten, fusokuten, d1Val, d2Val, d3Val, engTestScore, rank, judge, npRemark1, npRemark2, npRemark3);
                retMap.put(schregno, addWk);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }
    private String getSoutenSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH CALC_PASSFLG AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T3.CLASSCD, ");
        stb.append("   T3.SCHOOL_KIND, ");
        stb.append("   T3.CURRICULUM_CD, ");
        stb.append("   T3.SUBCLASSCD, ");
        stb.append("   T3.SCORE, ");
        stb.append("   T4.PASS_SCORE, ");
        stb.append("   CASE WHEN T4.PASS_SCORE <= T3.SCORE THEN '1' ELSE '0' END AS PASSFLG, ");
        stb.append("   VALUE(T5.CREDITS, 0) AS CREDITS ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.GRADE = T1.GRADE ");
        stb.append("   LEFT JOIN RECORD_RANK_SDIV_DAT T3 ");
        stb.append("     ON T3.YEAR = T1.YEAR ");
        stb.append("    AND T3.SEMESTER || '-' || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV = '" + GET_TESTCD + "' ");
        stb.append("    AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN PERFECT_RECORD_SDIV_DAT T4 ");
        stb.append("     ON T4.YEAR = T3.YEAR ");
        stb.append("    AND T4.SEMESTER = T3.SEMESTER ");
        stb.append("    AND T4.TESTKINDCD = T3.TESTKINDCD ");
        stb.append("    AND T4.TESTITEMCD = T3.TESTITEMCD ");
        stb.append("    AND T4.SCORE_DIV = T3.SCORE_DIV ");
        stb.append("    AND T4.CLASSCD = T3.CLASSCD ");
        stb.append("    AND T4.SCHOOL_KIND =T3.SCHOOL_KIND ");
        stb.append("    AND T4.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("    AND T4.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append("    AND DIV = '01' ");
        stb.append("   LEFT JOIN CREDIT_MST T5 ");
        stb.append("     ON T5.YEAR = T1.YEAR ");
        stb.append("    AND T5.COURSECD = T1.COURSECD ");
        stb.append("    AND T5.MAJORCD = T1.MAJORCD ");
        stb.append("    AND T5.COURSECODE =  T1.COURSECODE ");
        stb.append("    AND T5.GRADE = T1.GRADE ");
        stb.append("    AND T5.CLASSCD = T3.CLASSCD ");
        stb.append("    AND T5.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("    AND T5.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("    AND T5.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR BETWEEN '" + String.valueOf(Integer.parseInt(_param._year) - 2) + "' AND '" + _param._year + "' AND ");
        stb.append("   T1.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("   T2.GRADE_CD BETWEEN '01' AND '03' AND ");
        stb.append("   T2.SCHOOL_KIND = 'H' AND ");
        stb.append("   T3.SUBCLASSCD IS NOT NULL AND ");
        stb.append("   T3.SUBCLASSCD NOT IN ('333333', '555555', '999999', '99999A', '99999B') ");
        //未修得科目名を取得
        stb.append(" ), NOPASS_SUBCLS AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.CLASSCD, ");
        stb.append("   T1.SCHOOL_KIND, ");
        stb.append("   T1.CURRICULUM_CD, ");
        stb.append("   T1.SUBCLASSCD, ");
        stb.append("   SM.SUBCLASSABBV ");
        stb.append(" FROM ");
        stb.append("   CALC_PASSFLG T1 ");
        stb.append("   LEFT JOIN V_SUBCLASS_MST SM ");
        stb.append("     ON SM.YEAR = T1.YEAR ");
        stb.append("    AND SM.CLASSCD = T1.CLASSCD ");
        stb.append("    AND SM.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("    AND SM.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("    AND SM.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("   T1.PASSFLG = '0' ");
        //未修得科目を年度、生徒単位に集約
        stb.append(" ), MAKE_REMARKS AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   LISTAGG(NS.SUBCLASSABBV, ',') AS REMARKS ");
        stb.append(" FROM ");
        stb.append("   CALC_PASSFLG T1 ");
        stb.append("   LEFT JOIN V_CLASS_MST VC ");
        stb.append("     ON VC.YEAR = T1.YEAR ");
        stb.append("    AND VC.CLASSCD = T1.CLASSCD ");
        stb.append("    AND VC.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("   LEFT JOIN NOPASS_SUBCLS NS ");
        stb.append("     ON NS.YEAR = T1.YEAR ");
        stb.append("    AND NS.SCHREGNO = T1.SCHREGNO ");
        stb.append("    AND NS.CLASSCD = T1.CLASSCD ");
        stb.append("    AND NS.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("    AND NS.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("    AND NS.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("   T1.PASSFLG = '0' ");
        stb.append(" GROUP BY ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.SCHREGNO ");
        //不足点の元となるD1～D3と総点を算出
        stb.append(" ), CALC_D AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   SUM(VALUE(T1.SCORE, 0) * VALUE(T1.CREDITS, 0)) AS SOUTEN, ");
        stb.append("   SUM(CASE WHEN T1.YEAR = '" + String.valueOf(Integer.parseInt(_param._year) - 2) + "' THEN VALUE(T1.SCORE, 0) * VALUE(T1.CREDITS, 0) ELSE 0 END) - " + DANGERSOUTEN_ONEYEARLINE + " AS D1, ");
        stb.append("   SUM(CASE WHEN T1.YEAR = '" + String.valueOf(Integer.parseInt(_param._year) - 1) + "' THEN VALUE(T1.SCORE, 0) * VALUE(T1.CREDITS, 0) ELSE 0 END) - " + DANGERSOUTEN_ONEYEARLINE + " AS D2, ");
        stb.append("   SUM(CASE WHEN T1.YEAR = '" + _param._year + "' THEN VALUE(T1.SCORE, 0) * VALUE(T1.CREDITS, 0) ELSE 0 END) - " + DANGERSOUTEN_ONEYEARLINE + " AS D3 ");
        stb.append(" FROM ");
        stb.append("   CALC_PASSFLG T1 ");
        stb.append(" GROUP BY ");
        stb.append("   T1.SCHREGNO ");
        //不足点算出
        //不足店について：過年度が悪くて当年度が良ければ超過分で前年度の補填ができるが、過年度が良くて当年度が悪ければ、過年度分で補填できない。
        //                要は、「"過去"は補えるが、"今"は補えない仕組み。」それをCASE WHEN でパターン分けしている。
        //D1～D3について：単位*評価の合計がDANGERSOUTEN_ONEYEARLINEを超えているか超えていないかを、各学年でチェックする。
        stb.append(" ), CALC_FUSOKU AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.SOUTEN, ");
        stb.append("   T1.D1, ");
        stb.append("   T1.D2, ");
        stb.append("   T1.D3, ");
        stb.append("   CASE WHEN T1.D2 < 0 AND T1.D1 >= 0 THEN T1.D2 + T1.D3 ");
        stb.append("        WHEN T1.D3 < 0 AND T1.D2 >= 0 AND T1.D1 + T1.D2 >= 0 THEN T1.D3 ");
        stb.append("        ELSE T1.D1 + T1.D2 + T1.D3 ");
        stb.append("        END AS FUSOKUTEN ");
        stb.append(" FROM ");
        stb.append("   CALC_D T1 ");
        //英語の試験コードを取得(複数科目あったら、コードの小さい方を取得。そもそも英語しか登録が無い前提。)
        stb.append(" ), PROFICIENCY_ONEDAT AS ( ");
        stb.append(" SELECT ");
        stb.append("   YEAR, ");
        stb.append("   SEMESTER, ");
        stb.append("   PROFICIENCYDIV, ");
        stb.append("   PROFICIENCYCD, ");
        stb.append("   SCHREGNO, ");
        stb.append("   MIN(PROFICIENCY_SUBCLASS_CD) AS PROFICIENCY_SUBCLASS_CD ");
        stb.append(" FROM ");
        stb.append("   PROFICIENCY_DAT ");
        stb.append(" GROUP BY ");
        stb.append("   YEAR, ");
        stb.append("   SEMESTER, ");
        stb.append("   PROFICIENCYDIV, ");
        stb.append("   PROFICIENCYCD, ");
        stb.append("   SCHREGNO ");
        //英語の試験結果を取得
        stb.append(" ), PROFICIENCY_MAKEDAT AS ( ");
        stb.append(" SELECT ");
        stb.append("   T4.* ");
        stb.append(" FROM ");
        stb.append("   PROFICIENCY_DAT T4 ");
        stb.append("   INNER JOIN PROFICIENCY_ONEDAT T6 ");
        stb.append("     ON T6.YEAR = T4.YEAR ");
        stb.append("    AND T6.SEMESTER = T4.SEMESTER ");
        stb.append("    AND T6.PROFICIENCYDIV = T4.PROFICIENCYDIV ");
        stb.append("    AND T6.PROFICIENCYCD = T4.PROFICIENCYCD ");
        stb.append("    AND T6.SCHREGNO = T4.SCHREGNO ");
        stb.append("    AND T6.PROFICIENCY_SUBCLASS_CD = T4.PROFICIENCY_SUBCLASS_CD ");
        //英語力判定試験基準(固定値)テーブル(範囲開始,範囲終了,ランク文字列,合否(合格は1))
        stb.append(" ), ENG_TESTTBL(RANGE_S, RANGE_E, RANK, JUDGE) AS ( ");
        stb.append("   VALUES(350, 400, 'A', 1) ");
        stb.append("   UNION ");
        stb.append("   VALUES(300, 349, 'B', 1) ");
        stb.append("   UNION ");
        stb.append("   VALUES(250, 299, 'C', 1) ");
        stb.append("   UNION ");
        stb.append("   VALUES(200, 249, 'D', 1) ");
        stb.append("   UNION ");
        stb.append("   VALUES(150, 199, 'E', 0) ");
        stb.append("   UNION ");
        stb.append("   VALUES(0, 149, 'F', 0) ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.SOUTEN, ");
        stb.append("   T1.FUSOKUTEN, ");
        stb.append("   " + DANGERSOUTEN_ONEYEARLINE + " + T1.D1 AS D1Val, ");
        stb.append("   " + DANGERSOUTEN_ONEYEARLINE + " + T1.D2 AS D2Val, ");
        stb.append("   " + DANGERSOUTEN_ONEYEARLINE + " + T1.D3 AS D3Val, ");
        stb.append("   T4.SCORE AS ENG_TEST_SCORE, ");
        stb.append("   CASE WHEN T4.SCORE IS NULL THEN NULL ELSE T5.RANK END AS RANK, ");
        stb.append("   CASE WHEN T4.SCORE IS NULL THEN NULL ELSE T5.JUDGE END AS JUDGE, ");
        stb.append("   MR1.REMARKS AS NPREMARK1, ");
        stb.append("   MR2.REMARKS AS NPREMARK2, ");
        stb.append("   MR3.REMARKS AS NPREMARK3 ");
        stb.append(" FROM ");
        stb.append("   CALC_FUSOKU T1 ");
        stb.append("   LEFT JOIN PROFICIENCY_YMST T2 ");
        stb.append("     ON T2.YEAR = '" + _param._year + "' ");
        stb.append("    AND T2.SEMESTER = '" + _param._semester + "' ");
        stb.append("    AND T2.PROFICIENCYDIV = '" + _param._proficiencyDiv + "' ");
        stb.append("    AND T2.PROFICIENCYCD = '" + _param._proficiencyCd + "' ");
        stb.append("    AND T2.GRADE = '" + _param._grade + "' ");
        stb.append("   LEFT JOIN PROFICIENCY_MST T3 ");
        stb.append("     ON T3.PROFICIENCYDIV = T2.PROFICIENCYDIV ");
        stb.append("    AND T3.PROFICIENCYCD = T2.PROFICIENCYCD ");
        stb.append("   LEFT JOIN MAKE_REMARKS MR1 ");
        stb.append("     ON MR1.SCHREGNO = T1.SCHREGNO ");
        stb.append("    AND MR1.YEAR = '" + String.valueOf(Integer.parseInt(_param._year) - 2) + "' ");
        stb.append("   LEFT JOIN MAKE_REMARKS MR2 ");
        stb.append("     ON MR2.SCHREGNO = T1.SCHREGNO ");
        stb.append("    AND MR2.YEAR = '" + String.valueOf(Integer.parseInt(_param._year) - 1) + "' ");
        stb.append("   LEFT JOIN MAKE_REMARKS MR3 ");
        stb.append("     ON MR3.SCHREGNO = T1.SCHREGNO ");
        stb.append("    AND MR3.YEAR = '" + _param._year + "' ");
        stb.append("   LEFT JOIN PROFICIENCY_MAKEDAT T4 ");
        stb.append("     ON T4.YEAR = T2.YEAR ");
        stb.append("    AND T4.SEMESTER = T2.SEMESTER ");
        stb.append("    AND T4.PROFICIENCYDIV = T2.PROFICIENCYDIV ");
        stb.append("    AND T4.PROFICIENCYCD = T2.PROFICIENCYCD ");
        stb.append("    AND T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN ENG_TESTTBL T5 ");
        stb.append("     ON VALUE(T4.SCORE, 0) BETWEEN T5.RANGE_S AND T5.RANGE_E ");
        stb.append(" ORDER BY ");
        stb.append("   SCHREGNO ");
        return stb.toString();
    }

    private class Souten {
        final String _schregno;
        final String _souten;
        final String _fusokuten;
        final String _d1Val;
        final String _d2Val;
        final String _d3Val;
        final String _engTestScore;
        final String _rank;
        final String _judge;
        final String _npRemark1;
        final String _npRemark2;
        final String _npRemark3;
        public Souten (final String schregno, final String souten, final String fusokuten, final String d1Val, final String d2Val, final String d3Val, final String engTestScore, final String rank, final String judge, final String npRemark1, final String npRemark2, final String npRemark3)
        {
            _schregno = schregno;
            _souten = souten;
            _fusokuten = fusokuten;
            _d1Val = d1Val;
            _d2Val = d2Val;
            _d3Val = d3Val;
            _engTestScore = engTestScore;
            _rank = rank;
            _judge = judge;
            _npRemark1 = npRemark1;
            _npRemark2 = npRemark2;
            _npRemark3 = npRemark3;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 62833 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlDate;
        final String _lastSemester;
        final String _printTargetKind;
        final String _proficiencyCd;
        final String _proficiencyDiv;

        final String _grade;
        final String _gradeName;
        final Map _clsCdMap;
        final Map _chkClsCdMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("HID_YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlDate = request.getParameter("LOGIN_DATE");
            _printTargetKind = request.getParameter("PRINT_TARGET_KIND");
            _proficiencyDiv = request.getParameter("PROFICIENCYDIV");
            _proficiencyCd = request.getParameter("PROFICIENCYCD");
            _grade = getGradeVal(db2);
            _gradeName = getGradeName(db2);
            _lastSemester = getLastSemester(db2);
            _clsCdMap = getClsCdMap(db2);
            _chkClsCdMap = getChkClsCdMap(db2);
        }

        private Map getClsCdMap(final DB2UDB db2) {
            final Map retMap = new HashMap();
            final String sql = " SELECT CLASSCD, CLASSABBV FROM V_CLASS_MST WHERE YEAR = '" + _year + "' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retMap.put(rs.getString("CLASSCD"), rs.getString("CLASSABBV"));
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }
        private Map getChkClsCdMap(final DB2UDB db2) {
            final Map retMap = new HashMap();
            final String sql = " SELECT NAME1 AS CLASSCD_A, NAME2 AS CHKVAL FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D115' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retMap.put(rs.getString("CLASSCD_A"), rs.getString("CHKVAL"));
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }
        private String getGradeVal(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT GRADE FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE_CD = '03' AND SCHOOL_KIND = 'H' "));
        }
        private String getGradeName(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE_CD = '03' AND SCHOOL_KIND = 'H' "));
        }
        private String getLastSemester(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT MAX(SEMESTER) FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + _year + "' AND SEMESTER < '" + SEMEALL + "' AND GRADE = '" + _grade + "' "));
        }
    }
}

// eof

