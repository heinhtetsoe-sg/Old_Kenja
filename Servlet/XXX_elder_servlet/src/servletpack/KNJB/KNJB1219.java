/*
 * $Id: 6850354bf82a9058843ef06ef302186ea4d8e9e5 $
 *
 * 作成日: 2019/04/30
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJB;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * 教科書購入票
 */
public class KNJB1219 {

    private static final Log log = LogFactory.getLog(KNJB1219.class);

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

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {

        if ("K".equals(_param._schoolKind)) { //フォーマットは小学校から、なので、幼稚園はNG。大小関係にも影響するので、ここでチェック。
            return;
        }

        final Map studentMap = getStudentMap(db2);
        log.debug("P1");
        //選択した生徒(=取得データの生徒)分ループ
        for (Iterator itStd = studentMap.keySet().iterator(); itStd.hasNext();) {
            String kstr = (String) itStd.next();
            final Student student = (Student) studentMap.get(kstr);
            //教科書区分ごとに出力
            for (Iterator itSub = student._BookDivMap.keySet().iterator();itSub.hasNext();) {
                String bookdiv = (String)itSub.next();
                StudentSubInfo studentSInfo = (StudentSubInfo)student._BookDivMap.get(bookdiv);
                boolean printPJ_Flg = false;
                if ("P".compareTo(_param._schoolKind) >= 0 && studentSInfo._schKindEntryList.contains("P")) {
                    svf.VrSetForm("KNJB1219_1.frm", 1);
                    setTitle(db2, svf, student, bookdiv);
                    printPJ_Flg = true;
                    printTextBookInfo_P(db2, svf, student._schregNo, student, bookdiv);
                    _hasData = true;
                }
                if ("J".compareTo(_param._schoolKind) >= 0 && studentSInfo._schKindEntryList.contains("J")) {
                    if (!printPJ_Flg) {
                        svf.VrSetForm("KNJB1219_1.frm", 1);
                        setTitle(db2, svf, student, bookdiv);
                        printPJ_Flg = true;
                    }
                    printTextBookInfo_J(db2, svf, student._schregNo, student, bookdiv);
                    _hasData = true;
                }
                boolean printH_Flg = false;
                if ("H".compareTo(_param._schoolKind) >= 0 && studentSInfo._schKindEntryList.contains("H")) {
                    if (printPJ_Flg) {
                        svf.VrEndPage();
                    }
                    svf.VrSetForm("KNJB1219_2.frm", 1);
                    log.debug("P2");
                    setTitle(db2, svf, student, bookdiv);
                    printH_Flg = true;
                    log.debug("P3");
                    printTextBookInfo_H(db2, svf, student._schregNo, student, bookdiv);
                    _hasData = true;
                }
                //何かを出力しているのであれば、改ページ
                if (printPJ_Flg || printH_Flg) {
                    svf.VrEndPage();
                }
            }
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final Student student, final String bookDiv) {
        final String dateStr = KNJ_EditDate.h_format_JP(db2, _param._ctrlDate);
        final String yearStr = KNJ_EditDate.h_format_JP_N(db2, _param._ctrlDate);
        svf.VrsOut("DATE", dateStr);
        svf.VrsOut("TITLE", yearStr + "度 " + "教科書等記録個人表");
        final String outputStr = (String)_param._textBookDivMap.get(bookDiv);
        svf.VrsOut("TEXTBOOK_TITLE", outputStr);
        svf.VrsOut("NAME", _param._schoolKindName + " " + student._nowHrName + "　氏名：" + student._name);
        svf.VrsOut("SCHOOL_NAME", _param._schoolName);
    }

    private void printTextBookInfo_P(final DB2UDB db2, final Vrw32alp svf, final String schregno, final Student student, final String bookdiv) throws SQLException {
        //小中はほぼ同じ処理。高校だけが違う。
        final StudentSubInfo studentSInfo = (StudentSubInfo)student._BookDivMap.get(bookdiv);
        String schCtlStr = "P";
        //"P"のデータが無い場合は、すぐに終了。
        if (!studentSInfo._schKindEntryList.contains(schCtlStr)) {
            return;
        }
        Map subclassMap = getSubclassMap(db2, schCtlStr, schregno, student, bookdiv, "");
        setSubjectTitle(svf, schCtlStr, subclassMap, "");
        //学年の列を出力する
        for (Iterator itska = student._gradeInfoMap.keySet().iterator();itska.hasNext();) {
            String kStr = (String)itska.next();
            GradeInfo outwk = (GradeInfo)student._gradeInfoMap.get(kStr);
            if (!outwk._schoolKind.equals(schCtlStr)) {
                continue;
            }
            svf.VrsOutn("NENDO1", Integer.parseInt(outwk._gradeCd), outwk._year);
            svf.VrsOutn("GRADE1", Integer.parseInt(outwk._gradeCd), outwk._gradeName);

        }

        for (Iterator its = studentSInfo._schKindArry.keySet().iterator();its.hasNext();) {
            String kstr = (String) its.next();
            pastInfo outInfo = (pastInfo)studentSInfo._schKindArry.get(kstr);
            if (!outInfo._schoolKind.equals(schCtlStr)) {
                continue;
            }
            for (Iterator itr = outInfo._textBookMap.keySet().iterator();itr.hasNext();) {
                String ksStr = (String)itr.next();
                TextBookInf tbwk = (TextBookInf)outInfo._textBookMap.get(ksStr);

                //出力行/出力列を特定する
                String kSubClsStr = tbwk._classCd + "-" + tbwk._curriculumCd + "-" + tbwk._subclassCd;
                SubjectInfo subclsinfo = (SubjectInfo)subclassMap.get(kSubClsStr);

                //学年で行を特定する。
                svf.VrsOutn("TEXT_CD1_"           + subclsinfo._idx,        Integer.parseInt(outInfo._gradeCd), tbwk._textbookMsCd);     ///記号数字(本マスタ)
                svf.VrsOutn("TEXT_NAME1_"         + subclsinfo._idx + "_1", Integer.parseInt(outInfo._gradeCd), tbwk._textbookName);     //本名
                svf.VrsOutn("TEXT_COMPANY_CD1_"   + subclsinfo._idx,        Integer.parseInt(outInfo._gradeCd), tbwk._issueCompanyCd);   //出版社CD
                svf.VrsOutn("TEXT_COMPANY_NAME1_" + subclsinfo._idx + "_1", Integer.parseInt(outInfo._gradeCd), tbwk._issueCompanyAbbv); //出版社
            }
        }
    }

    private void printTextBookInfo_J(final DB2UDB db2, final Vrw32alp svf, final String schregno, final Student student, final String bookdiv) throws SQLException {
        //小中はほぼ同じ処理。高校だけが違う。
        final StudentSubInfo studentSInfo = (StudentSubInfo)student._BookDivMap.get(bookdiv);
        String schCtlStr = "J";
        //"J"のデータが無い場合は、すぐに終了。
        if (!studentSInfo._schKindEntryList.contains(schCtlStr)) {
            return;
        }
        Map subclassMap = getSubclassMap(db2, schCtlStr, schregno, student, bookdiv, "");
        setSubjectTitle(svf, schCtlStr, subclassMap, "");
        //学年の列を在籍情報として出力する(在籍していれば本が割り当たらなくても、NULLにしない)
        for (Iterator itska = student._gradeInfoMap.keySet().iterator();itska.hasNext();) {
            String kStr = (String)itska.next();
            GradeInfo outwk = (GradeInfo)student._gradeInfoMap.get(kStr);
            if (!outwk._schoolKind.equals(schCtlStr)) {
                continue;
            }
            svf.VrsOutn("NENDO2", Integer.parseInt(outwk._gradeCd), outwk._year);
            svf.VrsOutn("GRADE2", Integer.parseInt(outwk._gradeCd), outwk._gradeName);

        }

        for (Iterator its = studentSInfo._schKindArry.keySet().iterator();its.hasNext();) {
            String kstr = (String) its.next();
            pastInfo outInfo = (pastInfo)studentSInfo._schKindArry.get(kstr);
            if (!outInfo._schoolKind.equals(schCtlStr)) {
                continue;
            }
            for (Iterator itr = outInfo._textBookMap.keySet().iterator();itr.hasNext();) {
                String ksStr = (String)itr.next();
                TextBookInf tbwk = (TextBookInf)outInfo._textBookMap.get(ksStr);

                //出力行/出力列を特定する
                String kSubClsStr = tbwk._classCd + "-" + tbwk._curriculumCd + "-" + tbwk._subclassCd;
                SubjectInfo subclsinfo = (SubjectInfo)subclassMap.get(kSubClsStr);

                //学年で行を特定する。
                svf.VrsOutn("TEXT_CD2_"           + subclsinfo._idx,        Integer.parseInt(outInfo._gradeCd), tbwk._textbookMsCd);     //記号数字(本マスタ)
                svf.VrsOutn("TEXT_NAME2_"         + subclsinfo._idx + "_1", Integer.parseInt(outInfo._gradeCd), tbwk._textbookName);     //本名
                svf.VrsOutn("TEXT_COMPANY_CD2_"   + subclsinfo._idx,        Integer.parseInt(outInfo._gradeCd), tbwk._issueCompanyCd);   //出版社CD
                svf.VrsOutn("TEXT_COMPANY_NAME2_" + subclsinfo._idx + "_1", Integer.parseInt(outInfo._gradeCd), tbwk._issueCompanyAbbv); //出版社
            }
        }
    }

    private void printTextBookInfo_H(final DB2UDB db2, final Vrw32alp svf, final String schregno, final Student student, final String bookdiv) throws SQLException {
        //小中はほぼ同じ処理。高校だけが違う。
        final StudentSubInfo studentSInfo = (StudentSubInfo)student._BookDivMap.get(bookdiv);
        String schCtlStr = "H";
        //"H"のデータが無い場合は、すぐに終了。
        if (!studentSInfo._schKindEntryList.contains(schCtlStr)) {
            return;
        }

        for (Iterator itw = studentSInfo._schGradeEntryMap.keySet().iterator();itw.hasNext();) {
            String grdStr = (String)itw.next();
            String grdCdStr = String.valueOf(Integer.parseInt((String)studentSInfo._schGradeEntryMap.get(grdStr)));

            Map subclassMap = getSubclassMap(db2, schCtlStr, schregno, student, bookdiv, grdStr);
            setSubjectTitle(svf, schCtlStr, subclassMap, grdCdStr);
            //学年の列を在籍情報として出力する(在籍していれば本が割り当たらなくても、NULLにしない)
            for (Iterator itska = student._gradeInfoMap.keySet().iterator();itska.hasNext();) {
                String kStr = (String)itska.next();
                GradeInfo outwk = (GradeInfo)student._gradeInfoMap.get(kStr);
                if (!outwk._schoolKind.equals(schCtlStr)) {
                    continue;
                }
                int ngIdx = Integer.parseInt(outwk._gradeCd);
                svf.VrsOut("NENDO" + ngIdx, outwk._year);
                svf.VrsOut("GRADE" + ngIdx, outwk._gradeName);

            }

            for (Iterator its = studentSInfo._schKindArry.keySet().iterator();its.hasNext();) {
                String kstr = (String) its.next();
                pastInfo outInfo = (pastInfo)studentSInfo._schKindArry.get(kstr);
                if (!outInfo._schoolKind.equals(schCtlStr)) {
                    continue;
                }
                if (!grdStr.equals(outInfo._grade) ) {
                    continue;
                }
                for (Iterator itr = outInfo._textBookMap.keySet().iterator();itr.hasNext();) {
                    String ksStr = (String)itr.next();
                    TextBookInf tbwk = (TextBookInf)outInfo._textBookMap.get(ksStr);

                    //出力行/出力列を特定する
                    String kSubClsStr = tbwk._classCd + "-" + tbwk._curriculumCd + "-" + tbwk._subclassCd;
                    SubjectInfo subclsinfo = (SubjectInfo)subclassMap.get(kSubClsStr);

                    //学年で行を特定する。
                    int outlineCnt = 1;
                    int subidx = subclsinfo._idx;
                    if (subclsinfo._idx > 10) {
                        outlineCnt++;
                        subidx = subclsinfo._idx - 10;
                    }
                    svf.VrsOutn("TEXT_CD"           + grdCdStr + "_" + subidx,        outlineCnt, tbwk._textbookMsCd);     //記号数字(本マスタ)
                    svf.VrsOutn("TEXT_NAME"         + grdCdStr + "_" + subidx + "_1", outlineCnt, tbwk._textbookName);     //本名
                    svf.VrsOutn("TEXT_COMPANY_CD"   + grdCdStr + "_" + subidx,        outlineCnt, tbwk._issueCompanyCd);   //出版社CD
                    svf.VrsOutn("TEXT_COMPANY_NAME" + grdCdStr + "_" + subidx + "_1", outlineCnt, tbwk._issueCompanyAbbv); //出版社
                }
            }
        }
    }

    private void setSubjectTitle(final Vrw32alp svf, final String schCtlStr, final Map subclassMap, final String grdCdStr) {
        int ii = 1;
        int outline = 1;
        for (Iterator its = subclassMap.keySet().iterator();its.hasNext();) {
            String kstr = (String)its.next();
            SubjectInfo outinfo = (SubjectInfo)subclassMap.get(kstr);
            if ("P".equals(schCtlStr)) {
                final int nlen = KNJ_EditEdit.getMS932ByteLength(outinfo._classAbbv);
                final String nfield = nlen > 16 ? "3" : (nlen > 12 ? "2" : "1");
                svf.VrsOut("CLASS_NAME1_"+ nfield + "_"+ii, outinfo._classAbbv);
            }
            if ("J".equals(schCtlStr)) {
                final int nlen = KNJ_EditEdit.getMS932ByteLength(outinfo._classAbbv);
                final String nfield = nlen > 16 ? "3" : (nlen > 12 ? "2" : "1");
                svf.VrsOut("CLASS_NAME2_"+ nfield + "_"+ii, outinfo._classAbbv);
            }
            if ("H".equals(schCtlStr)) {
                final int nlen = KNJ_EditEdit.getMS932ByteLength(outinfo._classAbbv);
                final String nfield = nlen > 16 ? "3" : (nlen > 12 ? "2" : "1");
                if (ii > 10) {
                    outline++;
                     ii = 1;
                }
                svf.VrsOutn("CLASS_NAME" + grdCdStr + "_"+ nfield + "_"+ii, outline, outinfo._subclassAbbv);
            }
            ii++;
        }
    }

    /**
     * 生徒データ取得処理
     * @param db2           ＤＢ接続オブジェクト
     * @return              帳票出力対象データリスト
     * @throws Exception
     */
    private Map getStudentMap(final DB2UDB db2) throws SQLException {

        final Map rtnMap = new LinkedMap();
        final String sql = getStudentInfoSql();
        log.debug(" getStudentMap sql = " + sql);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String textBookDiv = rs.getString("TEXTBOOKDIV");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String year = rs.getString("YEAR");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String attendNo = rs.getString("ATTENDNO");
                final String gradeCd = rs.getString("GRADE_CD");
                final String gradeName = rs.getString("GRADE_NAME1");
                final String nowHrName = rs.getString("NOW_HR_NAME");
                final String name = rs.getString("NAME");
                final String subclassAbbv = rs.getString("SUBCLASSABBV");
                final String classCd = rs.getString("CLASSCD");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String textbookCd = rs.getString("TEXTBOOKCD");
                final String textbookMsCd = rs.getString("TEXTBOOKMS");
                final String textbookName = rs.getString("TEXTBOOKNAME");
                final String issueCompanyCd = rs.getString("ISSUECOMPANYCD");
                final String issueCompanyAbbv = rs.getString("ISSUECOMPANYABBV");
                final Student studentInfo;
                if (!rtnMap.containsKey(schregNo)) {
                    studentInfo = new Student(schregNo, nowHrName, name);
                    rtnMap.put(schregNo, studentInfo);
                } else {
                    studentInfo = (Student)rtnMap.get(schregNo);
                }
                studentInfo.addInfo(textBookDiv, schoolKind, year, grade, hrClass, attendNo, gradeCd, gradeName,  subclassAbbv, classCd, curriculumCd, subclassCd, textbookCd, textbookMsCd, textbookName, issueCompanyCd, issueCompanyAbbv);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnMap;
    }

    private String getStudentInfoSql() {
        final StringBuffer stb = new StringBuffer();
        //本CDがNULLのデータも含まれるが、在籍情報として内部で利用(左端の年度/学年を出力)するためにNULLのデータも取得しているので注意。
        stb.append(" select distinct ");
        stb.append("  T0.SCHREGNO, ");
        stb.append("  T2.NAME, ");
        stb.append("  T3_0.HR_NAME AS NOW_HR_NAME, ");
        stb.append("  T3G.SCHOOL_KIND, ");
        stb.append("  T5.TEXTBOOKDIV, ");
        stb.append("  T1.YEAR, ");
        stb.append("  T1.GRADE, ");
        stb.append("  T1.HR_CLASS, ");
        stb.append("  T1.ATTENDNO, ");
        stb.append("  T8.GRADE_CD, ");
        stb.append("  T8.GRADE_NAME1, ");
        stb.append("  T7.SUBCLASSABBV, ");
        stb.append("  T4.CLASSCD, ");
        stb.append("  T4.CURRICULUM_CD, ");
        stb.append("  T4.SUBCLASSCD, ");
        stb.append("  T5.TEXTBOOKCD, ");
        stb.append("  T5.TEXTBOOKMS, ");
        stb.append("  T5.TEXTBOOKNAME, ");
        stb.append("  T6.ISSUECOMPANYCD, ");
        stb.append("  T6.ISSUECOMPANYABBV ");
        stb.append(" FROM ");
        stb.append("  SCHREG_REGD_DAT T0 ");
        stb.append("  LEFT JOIN SCHREG_REGD_DAT T1 ");
        stb.append("    ON T1.SCHREGNO = T0.SCHREGNO ");
        stb.append("  INNER JOIN SCHREG_BASE_MST T2 ");
        stb.append("     ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("  LEFT JOIN SCHREG_REGD_HDAT T3_0 ");
        stb.append("     ON T3_0.YEAR = T0.YEAR ");
        stb.append("    AND T3_0.SEMESTER = T0.SEMESTER ");
        stb.append("    AND T3_0.GRADE = T0.GRADE ");
        stb.append("    AND T3_0.HR_CLASS = T0.HR_CLASS ");
        stb.append("  LEFT JOIN SCHREG_REGD_GDAT T3G ");
        stb.append("     ON T3G.YEAR = T1.YEAR ");
        stb.append("    AND T3G.GRADE = T1.GRADE ");
        stb.append("  LEFT JOIN SCHREG_TEXTBOOK_SUBCLASS_DAT T4 ");
        stb.append("    ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("   AND T4.YEAR = T1.YEAR ");
        stb.append("  LEFT JOIN TEXTBOOK_MST T5 ");
        stb.append("    ON T5.TEXTBOOKCD = T4.TEXTBOOKCD ");
        stb.append("  LEFT JOIN ISSUECOMPANY_MST T6 ");
        stb.append("    ON T6.ISSUECOMPANYCD = T5.ISSUECOMPANYCD ");
        stb.append("  LEFT JOIN SUBCLASS_MST T7 ");
        stb.append("    ON T7.CLASSCD = T4.CLASSCD ");
        stb.append("   AND T7.CURRICULUM_CD = T4.CURRICULUM_CD ");
        stb.append("   AND T7.SCHOOL_KIND = T4.SCHOOL_KIND ");
        stb.append("   AND T7.SUBCLASSCD = T4.SUBCLASSCD ");
        stb.append("  LEFT JOIN SCHREG_REGD_GDAT T8 ");
        stb.append("    ON T8.YEAR = T1.YEAR ");
        stb.append("   AND T8.GRADE = T1.GRADE ");
        stb.append(" WHERE ");
        stb.append("  T0.YEAR = '" + _param._year + "' ");
        stb.append("  AND T0.SEMESTER = '" + _param._semester + "' ");
        stb.append("  AND T0.SCHREGNO IN " + _param._studentInState + " ");
        stb.append("  AND T1.YEAR <= '" + _param._year + "' ");
        stb.append(" ORDER BY ");
        stb.append("  T0.SCHREGNO, ");
        stb.append("  T5.TEXTBOOKDIV, ");
        stb.append("  T3G.SCHOOL_KIND DESC, ");
        stb.append("  T1.YEAR, ");
        stb.append("  T1.GRADE, ");
        stb.append("  T1.HR_CLASS, ");
        stb.append("  T1.ATTENDNO, ");
        stb.append("  T4.CLASSCD, ");
        stb.append("  T4.CURRICULUM_CD, ");
        stb.append("  T4.SUBCLASSCD ");

        return stb.toString();
    }

    private Map getSubclassMap(final DB2UDB db2, final String getSchKind, final String schregno, final Student student, final String bookdiv, final String grade) throws SQLException {
        Map retMap = new LinkedMap();
        final String sql = getSubclassInfoSql(schregno, getSchKind, bookdiv, grade);
        log.debug(" getSubclassMap sql = " + sql);
        db2.query(sql);
        final ResultSet rs = db2.getResultSet();
        try {
            int nidx = 1;
            while (rs.next()) {
                final String classCd = rs.getString("CLASSCD");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String classAbbv = rs.getString("CLASSABBV");
                final String subclassAbbv = rs.getString("SUBCLASSABBV");
                SubjectInfo addwk = new SubjectInfo(nidx, classCd, curriculumCd, subclassCd, classAbbv, subclassAbbv);
                String kstr = classCd + "-" + curriculumCd + "-" + subclassCd;
                retMap.put(kstr, addwk);
                nidx++;
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }

        return retMap;
    }

    private String getSubclassInfoSql(final String schregno, final String getSchKind, final String bookdiv, final String grade) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" select distinct ");
        stb.append("  T4.CLASSCD, ");
        stb.append("  T4.CURRICULUM_CD, ");
        stb.append("  T4.SUBCLASSCD, ");
        stb.append("  T9.CLASSABBV, ");
        stb.append("  T7.SUBCLASSABBV ");
        stb.append(" FROM ");
        stb.append("  SCHREG_REGD_DAT T1 ");
        stb.append("   LEFT JOIN SCHREG_TEXTBOOK_SUBCLASS_DAT T4 ");
        stb.append("    ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("   AND T4.YEAR = T1.YEAR ");
        stb.append("  LEFT JOIN TEXTBOOK_MST T5 ");
        stb.append("    ON T5.TEXTBOOKCD = T4.TEXTBOOKCD ");
        stb.append("  LEFT JOIN SUBCLASS_MST T7 ");
        stb.append("    ON T7.CLASSCD = T4.CLASSCD ");
        stb.append("   AND T7.CURRICULUM_CD = T4.CURRICULUM_CD ");
        stb.append("   AND T7.SCHOOL_KIND = T4.SCHOOL_KIND ");
        stb.append("   AND T7.SUBCLASSCD = T4.SUBCLASSCD ");
        stb.append("  LEFT JOIN CLASS_MST T9 ");
        stb.append("    ON T9.CLASSCD = T4.CLASSCD ");
        stb.append("   AND T9.SCHOOL_KIND = T4.SCHOOL_KIND ");
        stb.append(" WHERE ");
        stb.append("  T1.SCHREGNO = '" + schregno + "' ");
        if (!"".equals(grade)) {
            stb.append(" AND T1.GRADE = '" + grade + "' ");
        }
        stb.append("  AND T4.SCHOOL_KIND = '" + getSchKind + "' ");
        stb.append("  AND T5.TEXTBOOKDIV = '" + bookdiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("  T4.CLASSCD, ");
        stb.append("  T4.CURRICULUM_CD, ");
        stb.append("  T4.SUBCLASSCD ");
        return stb.toString();
    }

    /** 生徒クラス */
    private class Student {
        final String _schregNo;
        final String _nowHrName;
        final String _name;

        Map _gradeInfoMap; //Mapは(grade, Gradeinfo)
        Map _BookDivMap;   // Mapは(textBookDiv, StudentSubInfo)。帳票は、schregno/textBookDiv/小中・高別に出力。(現在の)年組でソート。

        private Student(final String schregNo, final String nowHrName, final String name) {
            _schregNo = schregNo;
            _nowHrName = nowHrName;
            _name = name;
            _BookDivMap = new LinkedMap();
            _gradeInfoMap = new HashMap();
        }

        private void addGradeMap(final String grade, final String gradeCd, final String schoolKind, final String year, final String gradeName) {
            final GradeInfo addwk;
            if (!_gradeInfoMap.containsKey(grade)) {
                addwk = new GradeInfo(grade, gradeCd, schoolKind, year, gradeName);
                _gradeInfoMap.put(grade, addwk);
            }

        }

        private void addInfo(final String textBookDiv, final String schoolKind, final String year, final String grade,
            final String hrClass, final String attendNo, final String gradeCd, final String gradeName,
            final String subclassAbbv, final String classCd, final String curriculumCd, final String subclassCd,
            final String textbookCd, final String textbookMsCd, final String textbookName, final String issueCompanyCd, final String issueCompanyAbbv) {
            addGradeMap(grade, gradeCd, schoolKind, year, gradeName);
            if (textBookDiv != null) {
                StudentSubInfo addwk;
                if (!_BookDivMap.containsKey(textBookDiv)) {
                    addwk = new StudentSubInfo();
                    _BookDivMap.put(textBookDiv, addwk);
                } else {
                    addwk = (StudentSubInfo)_BookDivMap.get(textBookDiv);
                }
                addwk.addInfo(schoolKind, year, grade, hrClass, attendNo, gradeCd, gradeName, subclassAbbv, classCd, curriculumCd, subclassCd, textbookCd, textbookMsCd, textbookName, issueCompanyCd, issueCompanyAbbv);
            }

        }
    }

    private class GradeInfo {
        private final String _grade;
        private final String _gradeCd;
        private final String _year;
        private final String _schoolKind;
        private final String _gradeName;
        private GradeInfo (
                final String grade,
                final String gradeCd,
                final String schoolKind,
                final String year,
                final String gradeName
                ) {
            _grade = grade;
            _gradeCd = gradeCd;
            _schoolKind = schoolKind;
            _year = year;
            _gradeName = gradeName;
        }
    }

    private class StudentSubInfo {
        Map _schKindArry;        // ※(schoolKind + "-" + year + "-" + grade + "-" + hrClass, pastInfo)))
        List _schKindEntryList;  //在籍した校種を保持。どこを出力するか、チェックする。
        Map _schGradeEntryMap;   //在籍した学年コードを保持。高校の時に利用。
        private StudentSubInfo() {
            _schKindArry = new LinkedMap();
            _schKindEntryList = new ArrayList();
            _schGradeEntryMap = new HashMap();
        }
        private void addInfo(final String schoolKind, final String year, final String grade,
              final String hrClass, final String attendNo, final String gradeCd, final String gradeName,
               final String subclassAbbv, final String classCd, final String curriculumCd, final String subclassCd,
               final String textbookCd, final String textbookMsCd, final String textbookName, final String issueCompanyCd, final String issueCompanyAbbv) {
               if (!_schKindEntryList.contains(schoolKind)) {
                   _schKindEntryList.add(schoolKind);
               }
               if (!_schGradeEntryMap.containsKey(grade)) {
                   _schGradeEntryMap.put(grade, gradeCd);
               }
               String kstr = schoolKind + "-" + year + "-" + grade + "-" + hrClass;
               pastInfo addwk;
               if (!_schKindArry.containsKey(kstr)) {
                   addwk = new pastInfo(schoolKind, year, grade, hrClass, attendNo, gradeCd, gradeName);
                   _schKindArry.put(kstr, addwk);
               } else {
                   addwk = (pastInfo)_schKindArry.get(kstr);
               }
               if (classCd != null && curriculumCd != null && subclassCd != null && textbookCd != null) {
                   String bookkey = classCd + "-" + curriculumCd + "-" + subclassCd + "-" + textbookCd;
                   TextBookInf bookInfo;
                if (!addwk._textBookMap.containsKey(bookkey)) {
                    bookInfo = new TextBookInf(classCd, curriculumCd, subclassCd, textbookCd, textbookMsCd,subclassAbbv, textbookName, issueCompanyCd, issueCompanyAbbv);
                    addwk._textBookMap.put(bookkey, bookInfo);
                   }
               }
        }
    }


    private class pastInfo {
        final String _schoolKind; //K
        final String _year;       //K
        final String _grade;      //K
        final String _hrClass;    //K
        final String _attendNo;   //K
        final String _gradeCd;
        final String _gradeName;
        Map _textBookMap;

        private pastInfo(
                final String schoolKind,
                final String year,
                final String grade,
                final String hrClass,
                final String attendNo,
                final String gradeCd,
                final String gradeName
                ) {
            _schoolKind = schoolKind;
            _year = year;
            _grade = grade;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _gradeCd = gradeCd;
            _gradeName = gradeName;
            _textBookMap = new LinkedMap();
        }
    }

    private class TextBookInf {
        final String _classCd;        //k
        final String _curriculumCd;   //k
        final String _subclassCd;     //k
        final String _textbookCd;     //k
        final String _textbookMsCd;
        final String _subclassAbbv;
        final String _textbookName;
        final String _issueCompanyCd;
        final String _issueCompanyAbbv;
        private TextBookInf (
                final String classCd,
                final String curriculumCd,
                final String subclassCd,
                final String textbookCd,
                final String textbookMsCd,
                final String subclassAbbv,
                final String textbookName,
                final String issueCompanyCd,
                final String issueCompanyAbbv
                ) {
            _classCd = classCd;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _textbookCd = textbookCd;
            _textbookMsCd = textbookMsCd;
            _textbookName = textbookName;
            _subclassAbbv = subclassAbbv;
            _issueCompanyCd = issueCompanyCd;
            _issueCompanyAbbv = issueCompanyAbbv;
        }
    }
    private class SubjectInfo {
        final int _idx;
        final String _classCd;       //K
        final String _curriculumCd;  //K
        final String _subclassCd;    //K
        final String _classAbbv;
        final String _subclassAbbv;
        private SubjectInfo (final int idx, final String classCd, final String curriculumCd,
                              final String subclassCd, final String classAbbv, final String subclassAbbv) {
            _idx = idx;
            _classCd = classCd;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _classAbbv = classAbbv;
            _subclassAbbv = subclassAbbv;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 67284 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _schoolKind;
        private final String _fukusikiFlg;
        private final String _ghrCd;

        List _selSchregNo = new ArrayList();

        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolCd;
        private final String _useCurriculumCd;
        private final String _schoolName;
        private final String _schoolKindName;

        final String _studentInState;
        Map _textBookDivMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _fukusikiFlg = request.getParameter("FUKUSIKI_RADIO");
            _ghrCd = request.getParameter("GHR_CD");

            String[] selSchregNo = request.getParameterValues("CATEGORY_SELECTED");     //学年・組
            if (selSchregNo.length > 0) {
                _selSchregNo = Arrays.asList(selSchregNo);
            }

            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");

            _schoolCd = request.getParameter("SCHOOLCD");
            _useCurriculumCd = request.getParameter("useCurriculumcd");

            String studentInstate = "(";
            String sep = "";
            for (int i = 0; i < selSchregNo.length; i++) {
                studentInstate += sep + "'" + selSchregNo[i] + "'";
                sep = ",";
            }
            studentInstate += ")";

            _studentInState = studentInstate;
            _textBookDivMap = getNameMst(db2, "M004");
            _schoolName = getSchoolName(db2);
            _schoolKindName = getSchoolKindName(db2);
        }

        private String getSchoolKindName(final DB2UDB db2) {
            String retStr = "";
              PreparedStatement ps = null;
              ResultSet rs = null;
              try {
                  ps = db2.prepareStatement(" SELECT ABBV1 from NAME_MST WHERE NAMECD1 = 'A023' AND NAME1 = '" + _schoolKind + "' ");
                  rs = ps.executeQuery();
                  if (rs.next()) {
                      retStr = rs.getString("ABBV1");
                  }
              } catch (SQLException ex) {
                  log.debug("getM004 exception!", ex);
              } finally {
                  DbUtils.closeQuietly(null, ps, rs);
                  db2.commit();
              }
              return retStr;
        }

        private String getSchoolName(final DB2UDB db2) {
            String retStr = "";
              PreparedStatement ps = null;
              ResultSet rs = null;
              try {
                  ps = db2.prepareStatement(" SELECT SCHOOLNAME1 from SCHOOL_MST WHERE YEAR = '" + _year + "' AND SCHOOL_KIND = '" + _schoolKind + "' ");
                  rs = ps.executeQuery();
                  if (rs.next()) {
                      retStr = rs.getString("SCHOOLNAME1");
                  }
              } catch (SQLException ex) {
                  log.debug("getM004 exception!", ex);
              } finally {
                  DbUtils.closeQuietly(null, ps, rs);
                  db2.commit();
              }
              return retStr;
        }

        private Map getNameMst(final DB2UDB db2, final String ncd1) {
            Map retMap = new LinkedMap();
              PreparedStatement ps = null;
              ResultSet rs = null;
              try {
                  ps = db2.prepareStatement(" select NAMECD2, NAME1 from NAME_MST WHERE NAMECD1 = '" + ncd1 + "' ");
                  rs = ps.executeQuery();
                  while (rs.next()) {
                      retMap.put(rs.getString("NAMECD2"), rs.getString("NAME1"));
                  }
              } catch (SQLException ex) {
                  log.debug("getM004 exception!", ex);
              } finally {
                  DbUtils.closeQuietly(null, ps, rs);
                  db2.commit();
              }
              return retMap;
        }

    }
}

// eof

