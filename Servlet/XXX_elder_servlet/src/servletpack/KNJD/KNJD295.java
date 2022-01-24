/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: db91cb9231a6c8ce0f513c6539adaf6977c48fb8 $
 *
 * 作成日: 2019/02/26
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJD295 {

    private static final Log log = LogFactory.getLog(KNJD295.class);

    private boolean _hasData;
    private final int MAX_LINE = 41;

    private Param _param;

    KNJEditString knjobj = new KNJEditString();

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

	//NULLを"0"にし、さらに""も"0"にする
    private String chkdefaultZeroStr(final String chkStr) {
    	String retStr = StringUtils.defaultString(chkStr, "0");
    	retStr = "".equals(retStr) ? "0" : retStr;
    	return retStr;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List printList = getList(db2);

        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            svf.VrSetForm("KNJD295.frm", 4);
            if (!"1".equals(_param._printSign)) {
            	whitespace(svf, "MASK1");
            }
            if (!"1".equals(_param._printVerification)) {
            	whitespace(svf, "MASK2");
            }
            final Student student = (Student) iterator.next();

            //埋め字用
            int lineCnt = 0;

            //タイトルや票の項目名を出力
            setTitle(db2, svf, student);

            //教科・科目の情報を出力
            int totalCredit1 = 0;
            int totalCredit2 = 0;
            for (Iterator itr = student._subclassHoldMap.keySet().iterator();itr.hasNext();) {
            	final String subclassCd = (String) itr.next();
            	final SubClassHold subClassHold = (SubClassHold) student._subclassHoldMap.get(subclassCd);

            	//教科・科目を出力
            	svf.VrsOut("CLASS_NAME", subClassHold._className);
            	final int scnlen = KNJ_EditEdit.getMS932ByteLength(subClassHold._subClassName);
            	final String scnfield = scnlen > 24 ? "3" : (scnlen > 18 ? "2" : "1");
            	svf.VrsOut("SUBCLASS_NAME" + scnfield, subClassHold._subClassName);

            	if (student._studyRecCreditMap.containsKey(subclassCd)) {
                    final StudyRecCredit studyRecCredit = (StudyRecCredit) student._studyRecCreditMap.get(subclassCd);
                    final String setCredit = StringUtils.defaultString(studyRecCredit._credit1);
                    svf.VrsOut("GET_CREDIT1", setCredit);
                    if (!"".equals(setCredit)) {
                        totalCredit1 += Integer.parseInt(setCredit);
                    }

                    final String setOtherCredit = StringUtils.defaultString(studyRecCredit._credit2);
                    svf.VrsOut("GET_CREDIT2", setOtherCredit);
                    if (!"".equals(setOtherCredit)) {
                        totalCredit2 += Integer.parseInt(setOtherCredit);
                    }
            	}

                svf.VrsOut("CREDIT1_" + subClassHold._printSeme1, StringUtils.defaultString(subClassHold._credit1));
                svf.VrsOut("CREDIT2_" + subClassHold._printSeme2, StringUtils.defaultString(subClassHold._credit2));
                svf.VrsOut("CREDIT3_" + subClassHold._printSeme3, StringUtils.defaultString(subClassHold._credit3));

                String setRemark = subClassHold._remark1;
                setRemark = !"".equals(setRemark) ? setRemark : subClassHold._remark2;
                setRemark = !"".equals(setRemark) ? setRemark : subClassHold._remark3;
                svf.VrsOut("FIELD1", setRemark);

                lineCnt++;
                svf.VrEndRecord();
                _hasData = true;
            }

            for (int i = lineCnt; i < (MAX_LINE - 2); i++) {
                svf.VrEndRecord();
            }

            //その他科目
            int otherCredits1 = 0;
            int otherCredits2 = 0;
            for (Iterator itOther = student._studyRecCreditMap.keySet().iterator(); itOther.hasNext();) {
                final String subclassCd = (String) itOther.next();
                if (!student._subclassHoldMap.containsKey(subclassCd)) {
                    final StudyRecCredit studyRecCredit = (StudyRecCredit) student._studyRecCreditMap.get(subclassCd);
                    final String setCredit = StringUtils.defaultString(studyRecCredit._credit1);
                    if (!"".equals(setCredit)) {
                        otherCredits1 += Integer.parseInt(setCredit);
                    }
                    final String setOtherCredit = StringUtils.defaultString(studyRecCredit._credit2);
                    if (!"".equals(setOtherCredit)) {
                        otherCredits2 += Integer.parseInt(setOtherCredit);
                    }
                	log.info(" schregno = " + student._schregNo + ", その他の科目 = " + subclassCd + ", 合計 = " + otherCredits1 + ", 前籍校 = " + otherCredits2);
                }
            }
            svf.VrsOut("SUBCLASS_NAME1", "その他の科目");
            svf.VrsOut("GET_CREDIT1", String.valueOf(otherCredits1));
            svf.VrsOut("GET_CREDIT2", String.valueOf(otherCredits2));
            svf.VrEndRecord();

            //合計
            svf.VrsOut("SUBCLASS_NAME1", "合計");
            svf.VrsOut("GET_CREDIT1", String.valueOf(totalCredit1 + otherCredits1));
            svf.VrsOut("GET_CREDIT2", String.valueOf(totalCredit2 + otherCredits2));
            svf.VrEndRecord();
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear) + 1) + "年度　科目履修届");
        svf.VrsOut("SCHREG_NO", student._schregNo);
        final int nlen = KNJ_EditEdit.getMS932ByteLength(student._name);
        final String nfield = nlen > 30 ? "3" : (nlen > 20 ? "2" : "1");
        svf.VrsOut("NAME" + nfield, student._name);
        svf.VrsOut("GRADE1", "1年次");
        svf.VrsOut("SEMESTER1_1", "前期");
        svf.VrsOut("SEMESTER1_2", "後期");
        svf.VrsOut("GRADE2", "2年次");
        svf.VrsOut("SEMESTER2_1", "前期");
        svf.VrsOut("SEMESTER2_2", "後期");
        svf.VrsOut("GRADE3", "3年次");
        svf.VrsOut("SEMESTER3_1", "前期");
        svf.VrsOut("SEMESTER3_2", "後期");

        svf.VrsOut("SEMESTER9_1", "前期");
        svf.VrsOut("SEMESTER9_2", "後期");
        svf.VrsOut("SEMESTER9_9", "通年");
    }

    /** 空白の画像を表示して欄を非表示 */
    private void whitespace(final Vrw32alp svf, final String field) {
        if (null != _param._whitespaceImagePath) {
            svf.VrsOut(field, _param._whitespaceImagePath);
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String schregNo = rs.getString("SCHREGNO");
                final String courseCd = rs.getString("COURSECD");
                final String majorCd = rs.getString("MAJORCD");
                final String courseCode = rs.getString("COURSECODE");
                final String name = rs.getString("NAME");

                final Student student = new Student(db2, grade, hrClass, schregNo, courseCd, majorCd, courseCode, name);

                student.setScore(db2);
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();

        if (_param._isFreshMan) {
            stb.append(" SELECT ");
            stb.append("     FRESHD.GRADE, ");
            stb.append("     FRESHD.HR_CLASS, ");
            stb.append("     FRESHD.ATTENDNO, ");
            stb.append("     FRESHD.SCHREGNO, ");
            stb.append("     FRESHD.COURSECD, ");
            stb.append("     FRESHD.MAJORCD, ");
            stb.append("     FRESHD.COURSECODE, ");
            stb.append("     FRESHD.NAME ");
            stb.append(" FROM ");
            stb.append("     FRESHMAN_DAT FRESHD ");
            stb.append(" WHERE ");
            stb.append("     FRESHD.ENTERYEAR = '" + (Integer.parseInt(_param._ctrlYear) + 1) + "' ");
            stb.append("     AND FRESHD.GRADE || FRESHD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._classSelected) + " ");
            stb.append(" ORDER BY ");
            stb.append("     FRESHD.GRADE, ");
            stb.append("     FRESHD.HR_CLASS, ");
            stb.append("     FRESHD.ATTENDNO, ");
            stb.append("     FRESHD.SCHREGNO ");
        } else {
            stb.append(" WITH MAX_SEME AS ( ");
            stb.append(" SELECT ");
            stb.append("     REGD.YEAR, ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     MAX(REGD.SEMESTER) AS SEMESTER ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._classSelected) + " ");
            stb.append(" GROUP BY ");
            stb.append("     REGD.YEAR, ");
            stb.append("     REGD.SCHREGNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     REGD.COURSECD, ");
            stb.append("     REGD.MAJORCD, ");
            stb.append("     REGD.COURSECODE, ");
            stb.append("     BASE.NAME ");
            stb.append(" FROM ");
            stb.append("     MAX_SEME ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = MAX_SEME.YEAR ");
            stb.append("           AND REGD.SEMESTER = MAX_SEME.SEMESTER ");
            stb.append("           AND REGD.SCHREGNO = MAX_SEME.SCHREGNO ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = MAX_SEME.SCHREGNO ");
            stb.append(" ORDER BY ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     REGD.SCHREGNO ");
        }

        return stb.toString();
    }

    private class SubClassHold {
    	final String _classCd;
    	final String _schoolKind;
    	final String _curriculumCd;
    	final String _subclassCd;
    	final String _className;
    	final String _subClassName;
        final String _printSeme1;
        final String _printSeme2;
        final String _printSeme3;
        final String _credit1;
        final String _credit2;
        final String _credit3;
        final String _remark1;
        final String _remark2;
        final String _remark3;

    	public SubClassHold(
    	    	final String classCd,
                final String schoolKind,
                final String curriculumCd,
                final String subClassCd,
                final String className,
                final String subClassName,
                final String printSeme1,
                final String printSeme2,
                final String printSeme3,
                final String credit1,
                final String credit2,
                final String credit3,
                final String remark1,
                final String remark2,
                final String remark3
		) {
        	_classCd = classCd;
        	_schoolKind = schoolKind;
        	_curriculumCd = curriculumCd;
        	_subclassCd = subClassCd;
        	_className = className;
        	_subClassName = subClassName;
        	_printSeme1 = printSeme1;
        	_printSeme2 = printSeme2;
        	_printSeme3 = printSeme3;
            _credit1 = credit1;
            _credit2 = credit2;
            _credit3 = credit3;
            _remark1 = StringUtils.defaultString(remark1);
            _remark2 = StringUtils.defaultString(remark2);
            _remark3 = StringUtils.defaultString(remark3);
    	}
    }

    private class Student {
        final String _grade;
        final String _hrClass;
        final String _schregNo;
        final String _courseCd;
        final String _majorCd;
        final String _courseCode;
        final String _name;

        final Map _studyRecCreditMap;
        final Map _subclassHoldMap;

        public Student(
                final DB2UDB db2,
                final String grade,
                final String hrClass,
                final String schregNo,
                final String courseCd,
                final String majorCd,
                final String courseCode,
                final String name
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _schregNo = schregNo;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _name = name;
            _subclassHoldMap = getSubclassHoldMap(db2, schregNo);
            _studyRecCreditMap = new LinkedMap();
        }

        private void setScore(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();

            //全てのデータを取得。
            stb.append(" WITH SREC_TBL AS ( ");
            stb.append(" select ");
            stb.append("  T1.SCHOOLCD, ");
            stb.append("  T1.YEAR, ");
            stb.append("  T1.SCHREGNO, ");
            stb.append("  T1.CLASSCD, ");
            stb.append("  T1.SCHOOL_KIND, ");
            stb.append("  T1.CURRICULUM_CD, ");
            stb.append("  VALUE(SUB_M.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD, ");
            stb.append("  SUM(T1.GET_CREDIT) AS GET_CREDIT ");
            stb.append(" FROM ");
            stb.append("    SCHREG_STUDYREC_DAT T1 ");
            stb.append("    LEFT JOIN SUBCLASS_MST SUB_M ON SUB_M.CLASSCD = T1.CLASSCD ");
            stb.append("        AND SUB_M.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("        AND SUB_M.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("        AND SUB_M.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("    T1.SCHREGNO = '" + _schregNo + "' ");
            stb.append(" GROUP BY ");
            stb.append("  T1.SCHOOLCD, ");
            stb.append("  T1.YEAR, ");
            stb.append("  T1.SCHREGNO, ");
            stb.append("  T1.CLASSCD, ");
            stb.append("  T1.SCHOOL_KIND, ");
            stb.append("  T1.CURRICULUM_CD, ");
            stb.append("  VALUE(SUB_M.SUBCLASSCD2, T1.SUBCLASSCD) ");
            stb.append(" ) ");
            //単位数合計/前籍校合計を算出する。
            stb.append(" select ");
            stb.append("  T2.CLASSCD, ");
            stb.append("  T2.SCHOOL_KIND, ");
            stb.append("  T2.CURRICULUM_CD, ");
            stb.append("  T2.SUBCLASSCD, ");
            stb.append("  SUM(T2.GET_CREDIT) AS GET_CREDIT_1, ");
            stb.append("  SUM(T3.GET_CREDIT) AS GET_CREDIT_2 ");
            stb.append(" FROM ");
            stb.append("  SREC_TBL T2 ");
            stb.append("  LEFT JOIN SREC_TBL T3 ");
            stb.append("    ON T3.SCHOOLCD = '1' "); //前籍校を指定
            stb.append("    AND T3.YEAR = T2.YEAR ");
            stb.append("    AND T3.SCHREGNO = T2.SCHREGNO ");
            stb.append("    AND T3.CLASSCD = T2.CLASSCD ");
            stb.append("    AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("    AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ");
            stb.append("    AND T3.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("  ");
            stb.append(" GROUP BY ");
            stb.append("  T2.CLASSCD, ");
            stb.append("  T2.SCHOOL_KIND, ");
            stb.append("  T2.CURRICULUM_CD, ");
            stb.append("  T2.SUBCLASSCD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String classCd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String credit1 = rs.getString("GET_CREDIT_1");
                    final String credit2 = rs.getString("GET_CREDIT_2");
                    final String kstr = classCd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd;
                    final StudyRecCredit studyRecCredit = new StudyRecCredit(classCd, schoolKind, curriculumCd, subclasscd, credit1, credit2);
                    _studyRecCreditMap.put(kstr, studyRecCredit);
                }
            } catch (SQLException ex) {
                log.debug("setScore exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private Map getSubclassHoldMap(final DB2UDB db2, final String schregNo) {
            Map retMap = new LinkedMap();

            final StringBuffer stb = new StringBuffer();
            String setYear1 = "";
            String setYear2 = "";
            String setYear3 = "";
            if (Integer.parseInt(_grade) > Integer.parseInt(_param._a023._firstGrade)) {
                setYear1 = String.valueOf((Integer.parseInt(_param._ctrlYear) - 1));
                setYear2 = String.valueOf((Integer.parseInt(_param._ctrlYear)));
                setYear3 = String.valueOf((Integer.parseInt(_param._ctrlYear) + 1));
            } else if (!_param._isFreshMan) {
                setYear1 = String.valueOf((Integer.parseInt(_param._ctrlYear)));
                setYear2 = String.valueOf((Integer.parseInt(_param._ctrlYear) + 1));
                setYear3 = String.valueOf((Integer.parseInt(_param._ctrlYear) + 2));
            } else {
                setYear1 = String.valueOf((Integer.parseInt(_param._ctrlYear) + 1));
                setYear2 = String.valueOf((Integer.parseInt(_param._ctrlYear) + 2));
                setYear3 = String.valueOf((Integer.parseInt(_param._ctrlYear) + 3));
            }

            //コード一覧を拾う
            stb.append(" WITH SBCLS_CODE_MSTTBL AS ( ");
            stb.append(" SELECT ");
            stb.append("     HOLD_D.CLASSCD, ");
            stb.append("     HOLD_D.SCHOOL_KIND, ");
            stb.append("     HOLD_D.CURRICULUM_CD, ");
            stb.append("     HOLD_D.SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("     SUBCLASS_HOLD_DAT HOLD_D ");
            stb.append(" WHERE ");
            stb.append("    (   HOLD_D.ENTYEAR = '" + setYear1 + "' ");
            stb.append("     OR HOLD_D.ENTYEAR = '" + setYear2 + "' ");
            stb.append("     OR HOLD_D.ENTYEAR = '" + setYear3 + "') ");
            stb.append("     AND HOLD_D.COURSECD = '" + _courseCd + "' ");
            stb.append("     AND HOLD_D.MAJORCD = '" + _majorCd + "' ");
            stb.append("     AND HOLD_D.SCHOOL_KIND = 'H' ");
            stb.append(" GROUP BY ");
            stb.append("     HOLD_D.CLASSCD, ");
            stb.append("     HOLD_D.SCHOOL_KIND, ");
            stb.append("     HOLD_D.CURRICULUM_CD, ");
            stb.append("     HOLD_D.SUBCLASSCD ");
            stb.append(" ), MAIN_T AS ( ");
            stb.append("  SELECT DISTINCT ");
            stb.append("    CODE_MST.CLASSCD, ");
            stb.append("    CODE_MST.SCHOOL_KIND, ");
            stb.append("    CODE_MST.CURRICULUM_CD, ");
            stb.append("    CODE_MST.SUBCLASSCD, ");
            stb.append("    CASE WHEN SEME_1.SUBCLASS_REMARK1 = '1' THEN 1 ");
            stb.append("         WHEN SEME_1.SUBCLASS_REMARK2 = '1' THEN 2 ");
            stb.append("         ELSE 9 END AS SEME_FLG_1, ");
            stb.append("    CASE WHEN SEME_2.SUBCLASS_REMARK1 = '1' THEN 1 ");
            stb.append("         WHEN SEME_2.SUBCLASS_REMARK2 = '1' THEN 2 ");
            stb.append("         ELSE 9 END AS SEME_FLG_2, ");
            stb.append("    CASE WHEN SEME_3.SUBCLASS_REMARK1 = '1' THEN 1 ");
            stb.append("         WHEN SEME_3.SUBCLASS_REMARK2 = '1' THEN 2 ");
            stb.append("         ELSE 9 END AS SEME_FLG_3, ");

            stb.append("   CASE WHEN HOLD_1.STUDY1 = '1' THEN HOLD_1.CREDITS ELSE cast(null AS smallint) END AS CREDIT1, ");
            stb.append("   CASE WHEN HOLD_2.STUDY2 = '1' THEN HOLD_2.CREDITS ELSE cast(null AS smallint) END AS CREDIT2, ");
            stb.append("   CASE WHEN HOLD_3.STUDY3 = '1' THEN HOLD_3.CREDITS ELSE cast(null AS smallint) END AS CREDIT3, ");

            stb.append("   CASE WHEN HOLD_1.STUDY1 = '1' THEN HOLD_1.REMARK ELSE '' END AS REMARK1, ");
            stb.append("   CASE WHEN HOLD_2.STUDY2 = '1' THEN HOLD_2.REMARK ELSE '' END AS REMARK2, ");
            stb.append("   CASE WHEN HOLD_3.STUDY3 = '1' THEN HOLD_3.REMARK ELSE '' END AS REMARK3 ");
            stb.append("  FROM ");
            stb.append("    SBCLS_CODE_MSTTBL CODE_MST ");
            stb.append("    LEFT JOIN SUBCLASS_DETAIL_DAT SEME_1 ");
            stb.append("      ON SEME_1.YEAR = '" + setYear1 + "' ");
            stb.append("     AND SEME_1.CLASSCD = CODE_MST.CLASSCD ");
            stb.append("     AND SEME_1.SCHOOL_KIND = CODE_MST.SCHOOL_KIND ");
            stb.append("     AND SEME_1.CURRICULUM_CD = CODE_MST.CURRICULUM_CD ");
            stb.append("     AND SEME_1.SUBCLASSCD = CODE_MST.SUBCLASSCD ");
            stb.append("     AND SEME_1.SUBCLASS_SEQ = '012' ");
            stb.append("    LEFT JOIN SUBCLASS_DETAIL_DAT SEME_2 ");
            stb.append("      ON SEME_2.YEAR = '" + setYear2 + "' ");
            stb.append("     AND SEME_2.CLASSCD = CODE_MST.CLASSCD ");
            stb.append("     AND SEME_2.SCHOOL_KIND = CODE_MST.SCHOOL_KIND ");
            stb.append("     AND SEME_2.CURRICULUM_CD = CODE_MST.CURRICULUM_CD ");
            stb.append("     AND SEME_2.SUBCLASSCD = CODE_MST.SUBCLASSCD ");
            stb.append("     AND SEME_2.SUBCLASS_SEQ = '012' ");
            stb.append("    LEFT JOIN SUBCLASS_DETAIL_DAT SEME_3 ");
            stb.append("      ON SEME_3.YEAR = '" + setYear3 + "' ");
            stb.append("     AND SEME_3.CLASSCD = CODE_MST.CLASSCD ");
            stb.append("     AND SEME_3.SCHOOL_KIND = CODE_MST.SCHOOL_KIND ");
            stb.append("     AND SEME_3.CURRICULUM_CD = CODE_MST.CURRICULUM_CD ");
            stb.append("     AND SEME_3.SUBCLASSCD = CODE_MST.SUBCLASSCD ");
            stb.append("     AND SEME_3.SUBCLASS_SEQ = '012' ");

            stb.append("   LEFT JOIN SUBCLASS_HOLD_DAT HOLD_1 ");
            stb.append("     ON HOLD_1.ENTYEAR = '"+ setYear1 + "' ");
            stb.append("    AND HOLD_1.COURSECD = '" + _courseCd + "' ");
            stb.append("    AND HOLD_1.MAJORCD = '" + _majorCd + "' ");
            stb.append("    AND HOLD_1.CLASSCD = CODE_MST.CLASSCD ");
            stb.append("    AND HOLD_1.SCHOOL_KIND = CODE_MST.SCHOOL_KIND ");
            stb.append("    AND HOLD_1.CURRICULUM_CD = CODE_MST.CURRICULUM_CD ");
            stb.append("    AND HOLD_1.SUBCLASSCD = CODE_MST.SUBCLASSCD ");
            stb.append("   LEFT JOIN SUBCLASS_HOLD_DAT HOLD_2 ");
            stb.append("     ON HOLD_2.ENTYEAR = '" + setYear2 + "' ");
            stb.append("    AND HOLD_2.COURSECD = '" + _courseCd + "' ");
            stb.append("    AND HOLD_2.MAJORCD = '" + _majorCd + "' ");
            stb.append("    AND HOLD_2.CLASSCD = CODE_MST.CLASSCD ");
            stb.append("    AND HOLD_2.SCHOOL_KIND = CODE_MST.SCHOOL_KIND ");
            stb.append("    AND HOLD_2.CURRICULUM_CD = CODE_MST.CURRICULUM_CD ");
            stb.append("    AND HOLD_2.SUBCLASSCD = CODE_MST.SUBCLASSCD ");
            stb.append("   LEFT JOIN SUBCLASS_HOLD_DAT HOLD_3 ");
            stb.append("     ON HOLD_3.ENTYEAR = '"+ setYear3 + "' ");
            stb.append("    AND HOLD_3.COURSECD = '" + _courseCd + "' ");
            stb.append("    AND HOLD_3.MAJORCD = '" + _majorCd + "' ");
            stb.append("    AND HOLD_3.CLASSCD = CODE_MST.CLASSCD ");
            stb.append("    AND HOLD_3.SCHOOL_KIND = CODE_MST.SCHOOL_KIND ");
            stb.append("    AND HOLD_3.CURRICULUM_CD = CODE_MST.CURRICULUM_CD ");
            stb.append("    AND HOLD_3.SUBCLASSCD = CODE_MST.SUBCLASSCD ");
            stb.append(" ) ");

            stb.append(" SELECT ");
            stb.append("     MAIN_T.CLASSCD, ");
            stb.append("     MAIN_T.SCHOOL_KIND, ");
            stb.append("     MAIN_T.CURRICULUM_CD, ");
            stb.append("     CASE WHEN SUB_M.SUBCLASSCD2 IS NOT NULL THEN SUB_M.SUBCLASSCD2 ELSE MAIN_T.SUBCLASSCD END AS SUBCLASSCD, ");
            stb.append("     MIN(MAIN_T.SEME_FLG_1) AS SEME_FLG_1, ");
            stb.append("     MIN(MAIN_T.SEME_FLG_2) AS SEME_FLG_2, ");
            stb.append("     MIN(MAIN_T.SEME_FLG_3) AS SEME_FLG_3, ");
            stb.append("     SUM(MAIN_T.CREDIT1) AS CREDIT1, ");
            stb.append("     SUM(MAIN_T.CREDIT2) AS CREDIT2, ");
            stb.append("     SUM(MAIN_T.CREDIT3) AS CREDIT3, ");
            stb.append("     MAX(MAIN_T.REMARK1) AS REMARK1, ");
            stb.append("     MAX(MAIN_T.REMARK2) AS REMARK2, ");
            stb.append("     MAX(MAIN_T.REMARK3) AS REMARK3, ");
            stb.append("     MAX(CLASS_M.CLASSNAME) AS CLASSNAME, ");
            stb.append("     MAX(CASE WHEN SUB_M.SUBCLASSCD2 IS NOT NULL THEN SUB_MG.SUBCLASSNAME ELSE SUB_M.SUBCLASSNAME END) AS SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("     MAIN_T ");
            stb.append("     LEFT JOIN CLASS_MST CLASS_M ON CLASS_M.CLASSCD = MAIN_T.CLASSCD ");
            stb.append("          AND CLASS_M.SCHOOL_KIND = MAIN_T.SCHOOL_KIND ");
            stb.append("     LEFT JOIN SUBCLASS_MST SUB_M ON SUB_M.CLASSCD = MAIN_T.CLASSCD ");
            stb.append("          AND SUB_M.SCHOOL_KIND = MAIN_T.SCHOOL_KIND ");
            stb.append("          AND SUB_M.CURRICULUM_CD = MAIN_T.CURRICULUM_CD ");
            stb.append("          AND SUB_M.SUBCLASSCD = MAIN_T.SUBCLASSCD ");
            stb.append("     LEFT JOIN SUBCLASS_MST SUB_MG ON SUB_MG.CLASSCD = SUB_M.CLASSCD ");
            stb.append("          AND SUB_MG.SCHOOL_KIND = SUB_M.SCHOOL_KIND ");
            stb.append("          AND SUB_MG.CURRICULUM_CD = SUB_M.CURRICULUM_CD ");
            stb.append("          AND SUB_MG.SUBCLASSCD = SUB_M.SUBCLASSCD2 ");
            stb.append(" GROUP BY ");
            stb.append("     MAIN_T.CLASSCD, ");
            stb.append("     MAIN_T.SCHOOL_KIND, ");
            stb.append("     MAIN_T.CURRICULUM_CD, ");
            stb.append("     CASE WHEN SUB_M.SUBCLASSCD2 IS NOT NULL THEN SUB_M.SUBCLASSCD2 ELSE MAIN_T.SUBCLASSCD END ");
            stb.append(" ORDER BY ");
            stb.append("   MAIN_T.CLASSCD, ");
            stb.append("   MAIN_T.SCHOOL_KIND, ");
            stb.append("   MAIN_T.CURRICULUM_CD, ");
            stb.append("   CASE WHEN SUB_M.SUBCLASSCD2 IS NOT NULL THEN SUB_M.SUBCLASSCD2 ELSE MAIN_T.SUBCLASSCD END ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                log.debug("getSubClassMstTbl : "+stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String className = rs.getString("CLASSNAME");
                    final String subClassName = rs.getString("SUBCLASSNAME");

                    final String classCd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subClassCd = rs.getString("SUBCLASSCD");

                    final String printSeme1 = rs.getString("SEME_FLG_1");
                    final String printSeme2 = rs.getString("SEME_FLG_2");
                    final String printSeme3 = rs.getString("SEME_FLG_3");

                    final String credit1 = rs.getString("CREDIT1");
                    final String credit2 = rs.getString("CREDIT2");
                    final String credit3 = rs.getString("CREDIT3");

                    final String remark1 = rs.getString("REMARK1");
                    final String remark2 = rs.getString("REMARK2");
                    final String remark3 = rs.getString("REMARK3");

                    final String kcd = classCd + "-" + schoolKind + "-" + curriculumCd + "-" + subClassCd;
                    final SubClassHold subClassHoldInfo = new SubClassHold(classCd, schoolKind, curriculumCd, subClassCd, className, subClassName, printSeme1, printSeme2, printSeme3, credit1, credit2, credit3, remark1, remark2, remark3);

                    retMap.put(kcd, subClassHoldInfo);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

    }

    private class StudyRecCredit {
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _credit1;
        final String _credit2;
        public StudyRecCredit(
                final String classCd,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String credit1,
                final String credit2
        ) {
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _credit1 = credit1;
            _credit2 = credit2;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65975 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String[] _classSelected;
        final String _ctrlDate;
        final String _ctrlSemester;
        final String _ctrlYear;
        final String _newYear;
        final String _printSign;          // 1: サイン欄出力
        final String _printVerification;  // 1: 検証欄出力
        final String _useCurriculumcd;

        final String _documentRoot;
        final String _imagePath;
        final String _whitespaceImagePath;

        final A023 _a023;
        final boolean _isFreshMan;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _isFreshMan = "0".equals(StringUtils.split(request.getParameter("DTGRADE"), '-')[0]);
            _ctrlDate = request.getParameter("CTRL_DATE");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _newYear = request.getParameter("NEW_YEAR");
            _printSign = request.getParameter("PRINT_SIGN");
            _printVerification = request.getParameter("PRINT_VERIFICATION");
            _useCurriculumcd = request.getParameter("useCurriculumcd");

            _documentRoot = request.getParameter("DOCUMENTROOT");
            _imagePath = request.getParameter("IMAGEPATH");
            _whitespaceImagePath = getImageFilePath("whitespace.png");
            _a023 = getA023(db2);
        }

        private A023 getA023(final DB2UDB db2) {
            A023 retA023 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT * FROM NAME_MST WHERE NAMECD1 = 'A023' AND NAME1 = 'H' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String firstGrade = rs.getString("NAME2");
                    final String finalGrade = rs.getString("NAME3");
                    final String finalGradeRangeStart = rs.getString("NAMESPARE2");
                    final String finalGradeRangeEnd = rs.getString("NAMESPARE3");
                    retA023 = new A023(firstGrade, finalGrade, finalGradeRangeStart, finalGradeRangeEnd);
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retA023;
        }

        public String getImageFilePath(final String name) {
            final String path = _documentRoot + "/" + (null == _imagePath || "".equals(_imagePath) ? "" : _imagePath + "/") + name;
            final boolean exists = new File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }
    }

    /** パラメータクラス */
    private class A023 {
        final String _firstGrade;
        final String _finalGrade;
        final String _finalGradeRangeStart;
        final String _finalGradeRangeEnd;
        public A023(
                final String firstGrade,
                final String finalGrade,
                final String finalGradeRangeStart,
                final String finalGradeRangeEnd
        ) {
            _firstGrade = firstGrade;
            _finalGrade = finalGrade;
            _finalGradeRangeStart = finalGradeRangeStart;
            _finalGradeRangeEnd = finalGradeRangeEnd;
        }
    }
}

// eof
