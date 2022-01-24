// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2010/04/12 10:12:08 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.StaffInfo;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD134 {

    private static final Log log = LogFactory.getLog("KNJD134.class");

    private boolean _hasData;

    private Param _param;

    private static final String FROM_TO_MARK = " \uFF5E ";

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
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            for (int h = 0; h < _param._hrclass.length; h++) {
                printMain(db2, svf, _param._hrclass[h]);
            }

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final String hrclass) throws SQLException {
        svf.VrSetForm(_param.getFormId(), 1);
        log.debug("フォーム：" + _param.getFormId());

        int kyuCnt = 0;
        int ryuCnt = 0;
        String kyugaku = "";
        String ryugaku = "";
        String kyuSeq = "";
        String ryuSeq = "";
        final List printStudents = getPrintStudent(db2, hrclass);
        int maxpage = 1;
        List subClsCdArry = new ArrayList();
        for (final Iterator it = printStudents.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            for (final Iterator ite = student._subclassList.iterator();ite.hasNext();) {
                Subclass chkObj = (Subclass)ite.next();
                if (!_param._isSubclsSummary) {
                    if (chkObj._classcd != null && !subClsCdArry.contains(chkObj._classcd)) {
                        subClsCdArry.add(chkObj._classcd);
                    }
                } else {
                    if (chkObj._subclasscd != null && !subClsCdArry.contains(chkObj._subclasscd)) {
                        subClsCdArry.add(chkObj._subclasscd);
                    }
                }
            }
        }
        if (subClsCdArry.size() == 0) return;
        Collections.sort(subClsCdArry);
        maxpage = Math.max(maxpage, subClsCdArry.size());

        for (int pi = 0; pi < maxpage; pi++) {
            String chkCd = (String)subClsCdArry.get(pi);
            int gyo = 0;
            for (final Iterator it = printStudents.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();

                final String studentName = StringUtils.defaultString(_param._staffInfo.getStrEngOrJp(student._name, student._nameEng));

                svf.VrsOut("YEAR", _param.getNendo(db2));
                svf.VrsOut("TITLE", "　成績一覧表（文章評価）");
                svf.VrsOut("YMD1", _param.getCtrlDate(db2));
                svf.VrsOut("DATE", _param.getAttendTerm(db2));
                svf.VrsOut("TEACHER1", student._staffName);
                svf.VrsOut("HR_NAME", student._hrName);
                svf.VrsOut("ABSENCE_NAME", "1".equals(_param._chikokuHyoujiFlg) ? "欠時" : "欠課");
                if (0 < student.getKyugaku().length() && kyuCnt < 5 && pi == 0) {
                    kyugaku = kyugaku + kyuSeq + studentName + student.getKyugaku();
                    kyuSeq = ", ";
                    kyuCnt++;
                }
                if (0 < student.getRyugaku().length() && ryuCnt < 5 && pi == 0) {
                    ryugaku = ryugaku + ryuSeq + studentName + student.getRyugaku();
                    ryuSeq = ", ";
                    ryuCnt++;
                }

                gyo++;
                svf.VrsOutn("NO", gyo, student.getAttendno());
                svf.VrsOutn("SEX", gyo, student.getSex());
                if ("1".equals(_param._use_SchregNo_hyoji)) {
                    svf.VrsOutn("SCHREGNO", gyo, student._schregno);
                    svf.VrsOutn("NAME2", gyo, studentName);
                } else  {
                    svf.VrsOutn("NAME", gyo, studentName);
                }

                int cnt = 0;
                String clsName = "";
                String spConent = "";
                String spCdelim = "";
                String spEva = "";
                String spEdelim = "";
                for (cnt = 0;cnt < student._subclassList.size();cnt++) {
                    final Subclass subclass = (Subclass) student._subclassList.get(cnt);
                    final String chkWkCd;
                    //科目で集約しない(_param._isSubclsSummary = false)->キーとなるコードは教科コード、出力は教科名
                    //科目で集約する  (_param._isSubclsSummary = true )->キーとなるコードは科目コード、出力は科目名
                    if (!_param._isSubclsSummary) {
                        chkWkCd = subclass._classcd;
                    } else {
                        chkWkCd = subclass._subclasscd;
                    }
                    if (chkWkCd == null) continue;
                    if (chkWkCd.equals(chkCd)) {
                        if (!_param._isSubclsSummary) {
                            if (!"".equals(subclass._classname)) clsName = subclass._classname;
                        } else {
                            if (!"".equals(subclass._subclassname)) clsName = subclass._subclassname;
                        }
                        if (!"".equals(subclass.getTotalStudyAct())) {
                            spConent += (spCdelim + subclass.getTotalStudyAct());
                            spCdelim = ",";
                        }
                        if (!"".equals(subclass.getTotalStudyTime())) {
                            spEva += (spEdelim + subclass.getTotalStudyTime());
                            spEdelim = ",";
                        }
                    }
                }
                final String attkey;
                if (!_param._isSubclsSummary) {
                    attkey = chkCd;
                } else {
                    attkey = StringUtils.substring(chkCd, 0,2);
                }
                if (student._attendMap.containsKey(attkey)) {
                    AttDat att = null;
                    AttDat attLast = null;
                    //出欠情報から、該当するデータを取得する。
                    //出欠情報はリスト形式で保持していて、
                    //科目で集約する  (_param._isSubclsSummary = true )->科目コード一致をチェック
                    //科目で集約しない(_param._isSubclsSummary = false)->科目コード=""のデータを取得(クラスコード集約)
                    final List attList = (List)student._attendMap.get(attkey);
                    for (Iterator its = attList.iterator();its.hasNext();) {
                        final AttDat attchk = (AttDat)its.next();
                        if (_param._isSubclsSummary) {
                            if (attchk._subclasscd.equals(chkCd)) {
                                att = attchk;
                            }
                        }
                        if (attchk._subclasscd.equals("")) {
                            attLast = attchk;
                        }
                    }
                    if (!_param._isSubclsSummary && att == null) {
                        att = attLast;
                    }
                    if (att != null) {
                        svf.VrsOutn("LATE", gyo, student.getAttend(att._lateEarly));
                        svf.VrsOutn("PUB_ABSENCE", gyo, student.getAttend(att._koketsu));
                        svf.VrsOutn("MOURNING", gyo, student.getAttend(att._mourningSuspend));
                        if (att.compIsOver()) {
                            svf.VrAttributen("ABSENCE", gyo,  "Paint=(1,40,1),Bold=1");
                        } else if (att.getIsOver()) {
                            svf.VrAttributen("ABSENCE", gyo,  "Paint=(1,70,1),Bold=1");
                        }
                        svf.VrsOutn("ABSENCE", gyo, student.getAttend(att._absent));
                        if (att.compIsOver() || att.getIsOver()) {
                            svf.VrAttributen("ABSENCE", gyo,  "Paint=(0,0,0),Bold=0");
                        }
                    }
                }
                if (!"".equals(clsName)) {
                    svf.VrsOut("SUBCLASS_NAME", clsName);
                }
                svf.VrsOutn("SP_CONENT", gyo, spConent);
                svf.VrsOutn("SP_EVA", gyo, spEva);
            }
            if (0 < gyo) {
                printCredits(db2, svf, hrclass);
                if (0 < kyuCnt) svf.VrsOut("NOTE1", "休学者：" + kyugaku);
                if (0 < ryuCnt) svf.VrsOut("NOTE2", "留学者：" + ryugaku);

                svf.VrEndPage();
                _hasData = true;
            }
        }
    }

    private void printCredits(final DB2UDB db2, final Vrw32alp svf, final String hrclass) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getCreditsSql(hrclass);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final int cnt = rs.getInt("CNT");
                final String maxCredits = rs.getString("MAX_CREDITS");
                final String minCredits = rs.getString("MIN_CREDITS");
                final String staffName = rs.getString("STAFFNAME");

                if (0 == cnt) continue;
                svf.VrsOut("TEACHER2", staffName);

                if (null == maxCredits || null == minCredits) continue;
                if (maxCredits.equals(minCredits)) {
                    svf.VrsOut("CREDIT", maxCredits);
                } else {
                    svf.VrsOut("CREDIT", minCredits + FROM_TO_MARK + maxCredits);
                }
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getCreditsSql(final String hrclass) {
        final String stfCdMax = "ZZZZZZZZZZ";
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHNO AS ( ");
        stb.append("     SELECT ");
        stb.append("        * ");
        stb.append("     FROM ");
        stb.append("        SCHREG_REGD_DAT ");
        stb.append("     WHERE ");
        stb.append("        YEAR = '" + _param._year + "' ");
        if ("9".equals(_param._semester)) {
            stb.append("    AND SEMESTER = '" + _param._ctrlSemester + "' ");
        } else {
            stb.append("    AND SEMESTER = '" + _param._semester + "' ");
        }
        stb.append("        AND GRADE || HR_CLASS = '" + hrclass + "' ");
        //CHAIR_STF_DATの正担任/副担任を確定
        stb.append(" ), CSTF_DAT1 AS ( ");
        stb.append(" SELECT ");
        stb.append("     YEAR, ");
        stb.append("     SEMESTER, ");
        stb.append("     CHAIRCD, ");
        stb.append("     MIN(CASE WHEN VALUE(CHARGEDIV, '0') = '1' THEN STAFFCD ELSE '" + stfCdMax + "' END) AS SEITAN, ");
        stb.append("     MIN(CASE WHEN VALUE(CHARGEDIV, '0') = '0' THEN STAFFCD ELSE '" + stfCdMax + "' END) AS FUKUTAN ");
        stb.append(" FROM ");
        stb.append("     CHAIR_STF_DAT ");
        stb.append(" GROUP BY ");
        stb.append("     YEAR, ");
        stb.append("     SEMESTER, ");
        stb.append("     CHAIRCD ");
        //課程科目コード
        stb.append(" ), T_CHAIR AS ( ");
        stb.append("     SELECT ");
        stb.append("         W4.COURSECD, ");
        stb.append("         W4.MAJORCD, ");
        stb.append("         W4.GRADE, ");
        stb.append("         W4.COURSECODE, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || ");
        }
        stb.append("         W2.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("         CASE WHEN VALUE(W3.SEITAN, '" + stfCdMax + "') <> '" + stfCdMax + "' THEN W3.SEITAN WHEN VALUE(W3.FUKUTAN, '" + stfCdMax + "') <> '" + stfCdMax + "' THEN W3.FUKUTAN ELSE '' END AS STAFFCD ");
        stb.append("     FROM ");
        stb.append("         CHAIR_STD_DAT W1 ");
        stb.append("         INNER JOIN CHAIR_DAT W2 ON W1.YEAR = W2.YEAR AND W1.SEMESTER = W2.SEMESTER AND W1.CHAIRCD = W2.CHAIRCD AND W2.SUBCLASSCD LIKE '90%' ");
        stb.append("         LEFT JOIN CSTF_DAT1 W3 ON W1.YEAR = W3.YEAR AND W1.SEMESTER = W3.SEMESTER AND W1.CHAIRCD = W3.CHAIRCD ");
        stb.append("         INNER JOIN SCHNO W4 ON W1.SCHREGNO = W4.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("         W1.YEAR = '" + _param._year + "'  AND ");
        stb.append("         W1.SEMESTER <= '" + _param._semester + "' ");
        stb.append("     GROUP BY ");
        stb.append("         W4.COURSECD, ");
        stb.append("         W4.MAJORCD, ");
        stb.append("         W4.GRADE, ");
        stb.append("         W4.COURSECODE, ");
        stb.append("         W3.SEITAN, ");
        stb.append("         W3.FUKUTAN, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || ");
        }
        stb.append("         W2.SUBCLASSCD ");
        stb.append("  ) ");
        //課程科目コードに単位を紐づけ
        stb.append(" , T_CREDITS AS ( ");
        stb.append("     SELECT ");
        stb.append("        COUNT(*) AS CNT, ");
        stb.append("        MAX(W1.CREDITS) AS MAX_CREDITS, ");
        stb.append("        MIN(W1.CREDITS) AS MIN_CREDITS, ");
        stb.append("        W2.STAFFCD ");
        stb.append("     FROM ");
        stb.append("        T_CHAIR W2 ");
        stb.append("        LEFT JOIN CREDIT_MST W1 ON ");
        stb.append("            W1.YEAR = '" + _param._year + "' ");
        stb.append("        AND W1.CLASSCD = '90' ");
        stb.append("        AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
        }
        stb.append("            W1.SUBCLASSCD = W2.SUBCLASSCD ");
        stb.append("        AND W1.GRADE = W2.GRADE ");
        stb.append("        AND W1.COURSECD = W2.COURSECD ");
        stb.append("        AND W1.MAJORCD = W2.MAJORCD ");
        stb.append("        AND W1.COURSECODE = W2.COURSECODE ");
        stb.append("     GROUP BY ");
        stb.append("        W2.STAFFCD ");
        stb.append("  ) ");

        stb.append("  SELECT ");
        stb.append("     T1.CNT, ");
        stb.append("     T1.MAX_CREDITS, ");
        stb.append("     T1.MIN_CREDITS, ");
        stb.append("     T1.STAFFCD, ");
        stb.append("     L1.STAFFNAME ");
        stb.append("  FROM ");
        stb.append("     T_CREDITS T1 ");
        stb.append("     LEFT JOIN STAFF_MST L1 ON L1.STAFFCD=T1.STAFFCD ");
        return stb.toString();
    }

    private List getPrintStudent(final DB2UDB db2, final String hrclass) throws SQLException {
        final List rtnList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getStudentSql(hrclass);
        // log.debug("sql = " + sql);
        String oldschregno = null;
        Student student = null;

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String attendno = rs.getString("ATTENDNO");
                final String sex = rs.getString("SEX");
                final String name = rs.getString("NAME");
                final String nameEng = rs.getString("NAME_ENG");
                final String hrName = rs.getString("HR_NAME");
                final String staffName = rs.getString("STAFFNAME");
                final String transferCd1 = rs.getString("TRANSFERCD1");
                final String transferSdate1 = rs.getString("TRANSFER_SDATE1");
                final String transferEdate1 = rs.getString("TRANSFER_EDATE1");
                final String transferCd2 = rs.getString("TRANSFERCD2");
                final String transferSdate2 = rs.getString("TRANSFER_SDATE2");
                final String transferEdate2 = rs.getString("TRANSFER_EDATE2");

                final String classcd = rs.getString("CLASSCD");
                final String classname = rs.getString("CLASSNAME");
                final String subclasscd = rs.getString("SUBCLASSCD");
                final String subclassname = rs.getString("SUBCLASSNAME");
                final String totalStudyTime = rs.getString("TOTALSTUDYTIME");
                final String totalStudyAct = rs.getString("TOTALSTUDYACT");
                final Subclass subclass = new Subclass(classcd, classname, subclasscd, subclassname, totalStudyTime, totalStudyAct);

                if (null == oldschregno || !oldschregno.equals(schregno)) {
                    final Map attend = getAttendMap(db2, hrclass, schregno);
                    student = new Student(
                            schregno,
                            attendno,
                            sex,
                            name,
                            nameEng,
                            hrName,
                            staffName,
                            transferCd1,
                            transferSdate1,
                            transferEdate1,
                            transferCd2,
                            transferSdate2,
                            transferEdate2,
                            attend
                            );
                    rtnList.add(student);
                    oldschregno = schregno;
                }
                student._subclassList.add(subclass);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getStudentSql(final String hrclass) {
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH SCHNO AS ( ");
        stb.append("     SELECT ");
        stb.append("        * ");
        stb.append("     FROM ");
        stb.append("        SCHREG_REGD_DAT ");
        stb.append("     WHERE ");
        stb.append("        YEAR = '" + _param._year + "' ");
        if ("9".equals(_param._semester)) {
            stb.append("    AND SEMESTER = '" + _param._ctrlSemester + "' ");
        } else {
            stb.append("    AND SEMESTER = '" + _param._semester + "' ");
        }
        stb.append("        AND GRADE || HR_CLASS = '" + hrclass + "' ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T2.SEX, ");
        stb.append("     T2.NAME, ");
        stb.append("     T2.NAME_ENG, ");
        stb.append("     T3.HR_NAME, ");
        stb.append("     L1.STAFFNAME, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     L2.CLASSCD, ");
            stb.append("     L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || ");
        } else {
            stb.append("     SUBSTR(L2.SUBCLASSCD, 1, 2) AS CLASSCD, ");
        }
        stb.append("     L2.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     CM1.CLASSNAME, ");
        stb.append("     SM1.SUBCLASSNAME, ");
        stb.append("     L2.TOTALSTUDYTIME, ");
        stb.append("     L2.TOTALSTUDYACT, ");
        stb.append("     S1.TRANSFERCD as TRANSFERCD1, ");//1:留学
        stb.append("     S1.TRANSFER_SDATE as TRANSFER_SDATE1, ");
        stb.append("     S1.TRANSFER_EDATE as TRANSFER_EDATE1, ");
        stb.append("     S2.TRANSFERCD as TRANSFERCD2, ");//2:休学
        stb.append("     S2.TRANSFER_SDATE as TRANSFER_SDATE2, ");
        stb.append("     S2.TRANSFER_EDATE as TRANSFER_EDATE2 ");
        stb.append(" FROM ");
        stb.append("     SCHNO T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO=T1.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR=T1.YEAR AND T3.SEMESTER=T1.SEMESTER AND T3.GRADE=T1.GRADE AND T3.HR_CLASS=T1.HR_CLASS ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT T7 ON T7.YEAR=T1.YEAR AND T7.GRADE=T1.GRADE ");
        stb.append("     LEFT JOIN STAFF_MST L1 ON L1.STAFFCD = T3.TR_CD1 ");
        if ("1".equals(_param._knjd134PrintEachSemester)) {
            stb.append("     LEFT JOIN RECORD_TOTALSTUDYTIME_DAT L2 ON L2.YEAR=T1.YEAR AND L2.SEMESTER='" + _param._semester + "' AND L2.SCHREGNO=T1.SCHREGNO ");
        } else {
            stb.append("     LEFT JOIN RECORD_TOTALSTUDYTIME_DAT L2 ON L2.YEAR=T1.YEAR AND L2.SEMESTER='9' AND L2.SCHREGNO=T1.SCHREGNO ");
        }
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(" AND L2.SCHOOL_KIND = T7.SCHOOL_KIND ");
        }
        stb.append("     INNER JOIN SEMESTER_MST T4 ON T4.YEAR = '" + _param._year + "' AND T4.SEMESTER = '" + _param._semester + "' ");
        stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT S1 ON S1.SCHREGNO = T1.SCHREGNO AND S1.TRANSFERCD = '1' AND T4.EDATE BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE ");
        stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT S2 ON S2.SCHREGNO = T1.SCHREGNO AND S2.TRANSFERCD = '2' AND T4.EDATE BETWEEN S2.TRANSFER_SDATE AND S2.TRANSFER_EDATE ");
        stb.append("     LEFT JOIN CLASS_MST CM1 ON ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     CM1.SCHOOL_KIND = L2.SCHOOL_KIND AND ");
            stb.append("     CM1.CLASSCD = L2.CLASSCD ");
        } else {
            stb.append("     CM1.CLASSCD = SUBSTR(L2.SUBCLASSCD, 1, 2) ");
        }
        stb.append("     LEFT JOIN SUBCLASS_MST SM1 ON ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     SM1.CLASSCD = L2.CLASSCD AND ");
            stb.append("     SM1.SCHOOL_KIND = L2.SCHOOL_KIND AND ");
            stb.append("     SM1.CURRICULUM_CD = L2.CURRICULUM_CD AND ");
        }
        stb.append("         SM1.SUBCLASSCD = L2.SUBCLASSCD ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    private Map getAttendMap(final DB2UDB db2, final String hrclass, final String schregno) throws SQLException {
        Map retMap = new LinkedMap();

        final String sql = getAttendSql(hrclass, schregno);
        // log.debug("sql = " + sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        List AddList = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String classcd = rs.getString("CLASSCD");
                final int absent;
                final int lateEarly;
                if ("1".equals(_param._chikokuHyoujiFlg)) {
                    absent = rs.getInt("ABSENT0");
                    lateEarly = rs.getInt("LATE_EARLY0");
                } else {
                    absent = rs.getInt("ABSENT");
                    lateEarly = rs.getInt("LATE_EARLY");
                }
                final int koketsu = rs.getInt("KOKETSU");
                final int mourningSuspend = rs.getInt("MOURNING") + rs.getInt("SUSPEND")
                        + ("true".equals(_param._useVirus) ? rs.getInt("VIRUS") : 0)
                        + ("true".equals(_param._useKoudome) ? rs.getInt("KOUDOME") : 0);
                final int compAbsenceHigh = rs.getInt("COMP_ABSENCE_HIGH");
                final int getAbsenceHigh = rs.getInt("GET_ABSENCE_HIGH");
                final AttDat addObj;
                //科目で集約する  (_param._isSubclsSummary = true )->キーとなるコードは科目コード、出力は科目名
                //科目で集約しない(_param._isSubclsSummary = false)->キーとなるコードは教科コード(科目コードは"")、出力は教科名
                if (_param._isSubclsSummary) {
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    addObj = new AttDat(subclasscd, absent, lateEarly, koketsu, mourningSuspend, compAbsenceHigh, getAbsenceHigh);
                } else {
                    addObj = new AttDat("", absent, lateEarly, koketsu, mourningSuspend, compAbsenceHigh, getAbsenceHigh);
                }
                if (!retMap.containsKey(classcd)) {
                    AddList = new ArrayList();
                    retMap.put(classcd, AddList);
                } else {
                    AddList = (List)retMap.get(classcd);
                }
                AddList.add(addObj);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getAttendSql(final String hrclass, final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHNO AS ( ");
        stb.append("     SELECT ");
        stb.append("        * ");
        stb.append("     FROM ");
        stb.append("        SCHREG_REGD_DAT ");
        stb.append("     WHERE ");
        stb.append("        YEAR = '" + _param._year + "' ");
        if ("9".equals(_param._semester)) {
            stb.append("    AND SEMESTER = '" + _param._ctrlSemester + "' ");
        } else {
            stb.append("    AND SEMESTER = '" + _param._semester + "' ");
        }
        stb.append("        AND SCHREGNO = '" + schregno + "' ");
        stb.append(" ) ");
        //対象講座の表
        stb.append(" , CHAIR_A AS ( ");
        stb.append(     "SELECT W1.SCHREGNO, W2.CHAIRCD, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
        }
        stb.append(             "W2.SUBCLASSCD, W2.SEMESTER, W1.APPDATE, W1.APPENDDATE ");
        stb.append(     "FROM   CHAIR_STD_DAT W1 ");
        stb.append(           " INNER JOIN CHAIR_DAT W2 ON W1.YEAR = W2.YEAR AND W1.SEMESTER = W2.SEMESTER AND W1.CHAIRCD = W2.CHAIRCD ");
        stb.append(     "WHERE  W1.YEAR = '" + _param._year + "' ");
        stb.append(        "AND W1.SEMESTER <= '" + _param._semester + "' ");
        stb.append(        "AND EXISTS(SELECT 'X' FROM SCHNO W3 WHERE W3.SCHREGNO = W1.SCHREGNO)");
        stb.append(     "GROUP BY ");
        stb.append(        "W1.SCHREGNO, W2.CHAIRCD, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
        }
        stb.append(        "W2.SUBCLASSCD, W2.SEMESTER, W1.APPDATE, W1.APPENDDATE ");
        stb.append(     ")");
        // テスト項目マスタの集計フラグ
        stb.append(" , TEST_COUNTFLG AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.EXECUTEDATE, ");
        stb.append("         T1.PERIODCD, ");
        stb.append("         T1.CHAIRCD, ");
        stb.append("         '2' AS DATADIV ");
        stb.append("     FROM ");
        stb.append("         SCH_CHR_TEST T1, ");
        if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(_param._useTestCountflg)) {
            stb.append("         TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ");
        } else {
            stb.append("         TESTITEM_MST_COUNTFLG_NEW T2 ");
        }
        stb.append("     WHERE ");
        stb.append("         T2.YEAR       = T1.YEAR ");
        stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
        stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
        stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
        stb.append("         AND T2.COUNTFLG   = '0' ");
        if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(_param._useTestCountflg)) {
            stb.append("         AND T2.SCORE_DIV = '01' ");
        }
        stb.append(" ) ");
        //欠課数Ａの表
        final String semesInState   = (String) _param._hasuuMap.get("attendSemesInState");
        final String befDayFrom     = (String) _param._hasuuMap.get("befDayFrom");
        final String befDayTo       = (String) _param._hasuuMap.get("befDayTo");
        final String aftDayFrom     = (String) _param._hasuuMap.get("aftDayFrom");
        final String aftDayTo       = (String) _param._hasuuMap.get("aftDayTo");
        stb.append(",ATTEND_A AS(");
        if (befDayFrom != null || aftDayFrom != null) {
            //出欠データより集計
            stb.append(          "SELECT S1.SCHREGNO, SUBCLASSCD, SEMESTER,");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         S1.CLASSCD, S1.SCHOOL_KIND, S1.CURRICULUM_CD, ");
            }
            stb.append(                 "COUNT(*) ");
            if (!"1".equals(_param._knjSchoolMst._subOffDays)) {
                stb.append(             "- COUNT(IS_OFFDAYS) ");
            }
            stb.append(                 "- SUM(CASE WHEN ATDD.REP_DI_CD IN('' ");
            if ("1".equals(_param._knjSchoolMst._subSuspend)) {
                stb.append(                                     ",'2','9'");
            }
            if ("1".equals(_param._knjSchoolMst._subMourning)) {
                stb.append(                                     ",'3','10'");
            }
            if ("true".equals(_param._useKoudome)) {
                if ("1".equals(_param._knjSchoolMst._subKoudome)) {
                    stb.append(                                     ",'25','26'");
                }
            }
            if ("true".equals(_param._useVirus)) {
                if ("1".equals(_param._knjSchoolMst._subVirus)) {
                    stb.append(                                     ",'19','20'");
                }
            }
            stb.append(                 " )  THEN 1 ELSE 0 END) ");
            stb.append(                 "AS JISU, ");
            stb.append(                 "SUM(CASE WHEN ATDD.REP_DI_CD IN('1','8') THEN 1 ELSE 0 END)AS KOKETSU, ");
            stb.append(                 "SUM(CASE WHEN ATDD.REP_DI_CD IN('2','9') THEN 1 ELSE 0 END)AS SUSPEND, ");
            stb.append(                 "SUM(CASE WHEN ATDD.REP_DI_CD IN('3','10') THEN 1 ELSE 0 END)AS MOURNING, ");
            if ("true".equals(_param._useKoudome)) {
                stb.append(                  "SUM(CASE WHEN ATDD.REP_DI_CD IN('25','26') THEN 1 ELSE 0 END)AS KOUDOME, ");
            } else {
                stb.append(                  "0 AS KOUDOME, ");
            }
            if ("true".equals(_param._useVirus)) {
                stb.append(                  "SUM(CASE WHEN ATDD.REP_DI_CD IN('19','20') THEN 1 ELSE 0 END)AS VIRUS, ");
            } else {
                stb.append(                  "0 AS VIRUS, ");
            }
            stb.append(                 "SUM(CASE WHEN (CASE WHEN ATDD.REP_DI_CD IN ('29','30','31') THEN VALUE(ATDD.ATSUB_REPL_DI_CD, ATDD.REP_DI_CD) ELSE ATDD.REP_DI_CD END) IN('4','5','6','14','11','12','13'");
            if ("1".equals(_param._knjSchoolMst._subAbsent)) {
                stb.append(                                     ",'1','8'");
            }
            if ("1".equals(_param._knjSchoolMst._subSuspend)) {
                stb.append(                                     ",'2','9'");
            }
            if ("1".equals(_param._knjSchoolMst._subMourning)) {
                stb.append(                                     ",'3','10'");
            }
            if ("1".equals(_param._knjSchoolMst._subVirus)) {
                stb.append(                                     ",'19','20'");
            }
            stb.append(                                       ") ");
            if ("1".equals(_param._knjSchoolMst._subOffDays)) {
                stb.append(                                   "OR (IS_OFFDAYS IS NOT NULL)");
            }
            stb.append(                 " THEN 1 ELSE 0 END)AS ABSENT1, ");
            stb.append(                 "SUM(CASE WHEN ATDD.REP_DI_CD IN('15','16','23','24') THEN SMALLINT(VALUE(ATDD.MULTIPLY, '1')) ELSE 0 END)AS LATE_EARLY ");
            stb.append(          "FROM ( SELECT T2.SCHREGNO, T2.SUBCLASSCD, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T5.DI_CD, T3.SCHREGNO AS IS_OFFDAYS ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         , T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD ");
            }
            stb.append(                 "FROM   SCH_CHR_DAT T1 ");
            stb.append(                        "INNER JOIN CHAIR_A T2 ON T1.SEMESTER = T2.SEMESTER ");
            stb.append(                             "AND T1.CHAIRCD = T2.CHAIRCD ");
            stb.append(                        "LEFT JOIN SCHREG_TRANSFER_DAT T3 ON T3.TRANSFERCD = '2' ");
            stb.append(                             "AND T2.SCHREGNO = T3.SCHREGNO ");
            stb.append(                             "AND T1.EXECUTEDATE BETWEEN T3.TRANSFER_SDATE AND T3.TRANSFER_EDATE ");
            stb.append(                        "LEFT JOIN ATTEND_DAT T5 ON T2.SCHREGNO = T5.SCHREGNO ");
            stb.append(                             "AND T1.EXECUTEDATE = T5.ATTENDDATE ");
            stb.append(                             "AND T1.PERIODCD = T5.PERIODCD ");
            stb.append(                  "WHERE T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            if (befDayFrom != null && aftDayFrom != null) {
                stb.append(                "AND (T1.EXECUTEDATE BETWEEN '" + befDayFrom + "' AND '" + befDayTo + "' ");
                stb.append(                  "OR T1.EXECUTEDATE BETWEEN '" + aftDayFrom + "' AND '" + aftDayTo + "') ");
            } else if (befDayFrom != null) {
                stb.append(                "AND T1.EXECUTEDATE BETWEEN '" + befDayFrom + "' AND '" + befDayTo + "' ");
            } else if (aftDayFrom != null) {
                stb.append(                "AND T1.EXECUTEDATE BETWEEN '" + aftDayFrom + "' AND '" + aftDayTo + "' ");
            }
            stb.append(                    "AND T1.YEAR = '" + _param._year + "' ");
            stb.append(                    "AND T1.SEMESTER <= '" + _param._semester + "' ");
            stb.append(                    "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T4 ");  //NO025
            stb.append(                                   "WHERE   T4.SCHREGNO = T2.SCHREGNO ");  //NO025
            stb.append(                                       "AND (( ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE ) ");  //NO025
            stb.append(                                         "OR ( GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE )) ) ");  //NO025
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + _param._year + "' ");
            stb.append("                             AND ATDD.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND ATDD.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
            stb.append("                  ) ");
            // 勤怠コード'28'は時間割にカウントしない
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + _param._year + "' ");
            stb.append("                             AND ATDD.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND ATDD.REP_DI_CD = '28' ");
            stb.append("                  ) ");
            stb.append(                    "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");  //NO025
            stb.append(                                   "WHERE   T4.SCHREGNO = T2.SCHREGNO ");  //NO025
            stb.append(                                       "AND TRANSFERCD IN('1') AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ) ");  //NO025
            if (_param._definecode.useschchrcountflg) {
                stb.append(                "AND NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T4 ");
                stb.append(                               "WHERE   T4.EXECUTEDATE = T1.EXECUTEDATE AND ");
                stb.append(                                       "T4.PERIODCD = T1.PERIODCD AND ");
                stb.append(                                       "T4.CHAIRCD = T1.CHAIRCD AND ");
                stb.append(                                       "T1.DATADIV IN ('0', '1') AND ");
                stb.append(                                       "T4.GRADE||T4.HR_CLASS = '" + hrclass + "' AND ");
                stb.append(                                       "T4.COUNTFLG = '0') ");
                stb.append(                "AND NOT EXISTS(SELECT  'X' FROM TEST_COUNTFLG TEST ");
                stb.append(                               "WHERE ");
                stb.append(                                       "TEST.EXECUTEDATE  = T1.EXECUTEDATE ");
                stb.append(                                       "AND TEST.PERIODCD = T1.PERIODCD ");
                stb.append(                                       "AND TEST.CHAIRCD  = T1.CHAIRCD ");
                stb.append(                                       "AND TEST.DATADIV  = T1.DATADIV) ");
            }
            stb.append(                 "GROUP BY T2.SCHREGNO, T2.SUBCLASSCD, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T5.DI_CD, T3.SCHREGNO ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         , T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD ");
            }
            stb.append(               ")S1 ");
            stb.append("               LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + _param._year + "' ");
            stb.append("                             AND ATDD.DI_CD = S1.DI_CD ");
            stb.append(          "GROUP BY S1.SCHREGNO, S1.SUBCLASSCD, S1.SEMESTER ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         , S1.CLASSCD, S1.SCHOOL_KIND, S1.CURRICULUM_CD ");
            }
        }
        if (_param._semesFlg) {
            //月別科目別出欠集計データより欠課を取得
            if (befDayFrom != null || aftDayFrom != null) {
                stb.append(      "UNION ALL ");
            }
            stb.append(          "SELECT  W1.SCHREGNO, W1.SUBCLASSCD, W1.SEMESTER, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD, ");
            }
            stb.append(                  "SUM(VALUE(LESSON,0) ");
            if (!"1".equals(_param._knjSchoolMst._subOffDays)) {
                stb.append(               " - VALUE(OFFDAYS,0)");
            }
            if (!"1".equals(_param._knjSchoolMst._subSuspend)) {
                stb.append(               " - VALUE(SUSPEND,0)");
            }
            if (!"1".equals(_param._knjSchoolMst._subMourning)) {
                stb.append(              "  - VALUE(MOURNING,0)");
            }
            if ("true".equals(_param._useKoudome)) {
                if (!"1".equals(_param._knjSchoolMst._subKoudome)) {
                    stb.append(              "  - VALUE(KOUDOME,0)");
                }
            }
            if ("true".equals(_param._useVirus)) {
                if (!"1".equals(_param._knjSchoolMst._subVirus)) {
                    stb.append(              "  - VALUE(VIRUS,0)");
                }
            }
            stb.append(                   " - VALUE(ABROAD,0) ) AS JISU, ");
            stb.append(                  "SUM(VALUE(ABSENT,0)) AS KOKETSU, ");
            stb.append(                  "SUM(VALUE(SUSPEND,0)) AS SUSPEND, ");
            stb.append(                  "SUM(VALUE(MOURNING,0)) AS MOURNING, ");
            if ("true".equals(_param._useKoudome)) {
                stb.append(                  "SUM(VALUE(KOUDOME,0)) AS KOUDOME, ");
            } else {
                stb.append(                  "0 AS KOUDOME, ");
            }
            if ("true".equals(_param._useVirus)) {
                stb.append(                  "SUM(VALUE(VIRUS,0)) AS VIRUS, ");
            } else {
                stb.append(                  "0 AS VIRUS, ");
            }
            stb.append(                  "SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF,0)");
            if ("1".equals(_param._knjSchoolMst._subAbsent)) {
                stb.append(              "+ VALUE(ABSENT,0)");
            }
            if ("1".equals(_param._knjSchoolMst._subSuspend)) {
                stb.append(              "+ VALUE(SUSPEND,0)");
            }
            if ("1".equals(_param._knjSchoolMst._subMourning)) {
                stb.append(              "+ VALUE(MOURNING,0)");
            }
            if ("1".equals(_param._knjSchoolMst._subOffDays)) {
                stb.append(              "+ VALUE(OFFDAYS,0)");
            }
            if ("1".equals(_param._knjSchoolMst._subVirus)) {
                stb.append(              "+ VALUE(VIRUS,0)");
            }
            if ("1".equals(_param._knjSchoolMst._subKoudome)) {
                stb.append(              "+ VALUE(KOUDOME,0)");
            }
            stb.append(                  ") AS ABSENT1, ");
            stb.append(                  "SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY ");
            stb.append(          "FROM    ATTEND_SUBCLASS_DAT W1, SCHNO W2 ");
            stb.append(          "WHERE   W1.YEAR = '" + _param._year + "' AND ");
            stb.append(              "W1.SEMESTER <= '" + _param._semester + "' AND ");
            stb.append(              "W1.SEMESTER || W1.MONTH IN " + semesInState + " AND ");
            stb.append(              "W1.SCHREGNO = W2.SCHREGNO ");
            stb.append(          "GROUP BY W1.SCHREGNO, W1.SUBCLASSCD, W1.SEMESTER ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         , W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD ");
            }
        }
        stb.append(     ") ");
        //欠課数Ｂの表
        stb.append(",ATTEND_B AS(");
        stb.append(         "SELECT  SCHREGNO, SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         , W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD ");
        }
        if ("1".equals(_param._knjSchoolMst._absentCov) || "3".equals(_param._knjSchoolMst._absentCov) ) {
            //遅刻・早退を学期で欠課換算する
            stb.append(           ", SUM(ABSENT0)AS ABSENT0 ");
            stb.append(           ", SUM(LATE_EARLY)AS LATE_EARLY0 ");
            stb.append(           ", SUM(ABSENT1)AS ABSENT1 ");
            stb.append(           ", SUM(VALUE(LATE_EARLY,0)) - SUM(VALUE(LATE_EARLY,0)) / " + _param._knjSchoolMst._absentCovLate + " * " + _param._knjSchoolMst._absentCovLate + " AS LATE_EARLY1 ");
            stb.append(           ", SUM(JISU)AS JISU ");
            stb.append(           ", SUM(KOKETSU) AS KOKETSU ");
            stb.append(           ", SUM(MOURNING) AS MOURNING ");
            stb.append(           ", SUM(SUSPEND) AS SUSPEND ");
            stb.append(           ", SUM(KOUDOME) AS KOUDOME ");
            stb.append(           ", SUM(VIRUS) AS VIRUS ");
            stb.append(     "FROM   (SELECT  SCHREGNO, SUBCLASSCD, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ");
            }
            stb.append(                    " SUM(JISU)AS JISU, SUM(ABSENT1) AS ABSENT0, SUM(LATE_EARLY) AS LATE_EARLY, ");
            stb.append(                    " SUM(VALUE(KOKETSU,0)) AS KOKETSU, SUM(VALUE(MOURNING,0)) AS MOURNING, SUM(VALUE(SUSPEND,0)) AS SUSPEND, SUM(VALUE(KOUDOME,0)) AS KOUDOME, SUM(VALUE(VIRUS,0)) AS VIRUS, ");
            if ("1".equals(_param._knjSchoolMst._absentCov) || _param._semester.equals("9")) {
                stb.append(                 "VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + _param._knjSchoolMst._absentCovLate + " AS ABSENT1 ");   //05/06/20Modify
            } else {
                stb.append(                 "FLOAT(DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(FLOAT(LATE_EARLY)),0) / " + _param._knjSchoolMst._absentCovLate + ",5,1)) AS ABSENT1 ");   //05/06/20Modify
            }
            stb.append(             "FROM    ATTEND_A ");
            stb.append(             "GROUP BY SCHREGNO, SUBCLASSCD, SEMESTER ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         , CLASSCD, SCHOOL_KIND, CURRICULUM_CD ");
            }
            stb.append(             ")W1 ");
            stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         , W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD ");
            }
        } else if ("2".equals(_param._knjSchoolMst._absentCov)  || "4".equals(_param._knjSchoolMst._absentCov) ) {
            //遅刻・早退を年間で欠課換算する
            stb.append(           ", SUM(ABSENT1)AS ABSENT0 ");
            stb.append(           ", SUM(VALUE(LATE_EARLY,0))AS LATE_EARLY0 ");
            if ("2".equals(_param._knjSchoolMst._absentCov) ) {
                stb.append(       ", VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + _param._knjSchoolMst._absentCovLate + " AS ABSENT1 ");   //05/06/20Modify
            } else {
                stb.append(       ", FLOAT(DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(FLOAT(LATE_EARLY)),0) / " + _param._knjSchoolMst._absentCovLate + ",5,1)) AS ABSENT1 ");   //05/06/20Modify
            }
            stb.append(           ", SUM(VALUE(LATE_EARLY,0)) - SUM(VALUE(LATE_EARLY,0)) / " + _param._knjSchoolMst._absentCovLate + " * " + _param._knjSchoolMst._absentCovLate + " AS LATE_EARLY1 ");
            stb.append(           ", SUM(JISU)AS JISU ");
            stb.append(           ", SUM(KOKETSU) AS KOKETSU ");
            stb.append(           ", SUM(MOURNING) AS MOURNING ");
            stb.append(           ", SUM(SUSPEND) AS SUSPEND ");
            stb.append(           ", SUM(KOUDOME) AS KOUDOME ");
            stb.append(           ", SUM(VIRUS) AS VIRUS ");
            stb.append(     "FROM    ATTEND_A W1 ");
            stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         , W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD ");
            }
        } else if ("5".equals(_param._knjSchoolMst._absentCov)) {
            //遅刻・早退を年間で欠課換算する
            stb.append(           ", SUM(ABSENT1)AS ABSENT0 ");
            stb.append(           ", SUM(VALUE(LATE_EARLY,0)) AS LATE_EARLY0 ");
            stb.append(           ", SUM(VALUE(ABSENT1,0)) + SUM(VALUE(LATE_EARLY,0)) / " + _param._knjSchoolMst._absentCovLate + " ");
            stb.append(           "     +  (CASE WHEN MOD(SUM(VALUE(LATE_EARLY,0)) , " + _param._knjSchoolMst._absentCovLate + ") >= " + _param._knjSchoolMst._amariKuriage + " THEN 1 ELSE 0 END)AS ABSENT1 ");
            stb.append(           ", SUM(VALUE(LATE_EARLY,0)) - SUM(VALUE(LATE_EARLY,0)) / " + _param._knjSchoolMst._absentCovLate + " * " + _param._knjSchoolMst._absentCovLate + " AS LATE_EARLY1 ");
            stb.append(           ", SUM(JISU)AS JISU ");
            stb.append(           ", SUM(KOKETSU) AS KOKETSU ");
            stb.append(           ", SUM(MOURNING) AS MOURNING ");
            stb.append(           ", SUM(SUSPEND) AS SUSPEND ");
            stb.append(           ", SUM(KOUDOME) AS KOUDOME ");
            stb.append(           ", SUM(VIRUS) AS VIRUS ");
            stb.append(     "FROM    ATTEND_A W1 ");
            stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         , W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD ");
            }
        } else {
            //遅刻・早退を欠課換算しない
            stb.append(           ", SUM(ABSENT1)AS ABSENT0 ");
            stb.append(           ", SUM(VALUE(LATE_EARLY,0)) AS LATE_EARLY0 ");
            stb.append(           ", SUM(VALUE(LATE_EARLY,0)) AS LATE_EARLY1 ");
            stb.append(           ", SUM(ABSENT1) AS ABSENT1 ");
            stb.append(           ", SUM(JISU)AS JISU ");
            stb.append(           ", SUM(KOKETSU) AS KOKETSU ");
            stb.append(           ", SUM(MOURNING) AS MOURNING ");
            stb.append(           ", SUM(SUSPEND) AS SUSPEND ");
            stb.append(           ", SUM(KOUDOME) AS KOUDOME ");
            stb.append(           ", SUM(VIRUS) AS VIRUS ");
            stb.append(     "FROM    ATTEND_A W1 ");
            stb.append(     "GROUP BY SCHREGNO, SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("         , W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD ");
            }
        }
        stb.append(     ") ");
        // 上限値の表
        stb.append(",T_ABSENCE_HIGH AS(");
        if (_param._knjSchoolMst.isJitu()) {
            stb.append(    "SELECT  T2.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ");
            }
            stb.append("         T1.SUBCLASSCD, ");
            stb.append(           " VALUE(T1.COMP_ABSENCE_HIGH, 0) AS COMP_ABSENCE_HIGH, ");
            stb.append(           " VALUE(T1.GET_ABSENCE_HIGH, 0) AS GET_ABSENCE_HIGH ");
            stb.append(    "FROM SCHREG_ABSENCE_HIGH_DAT T1 ");
            stb.append(         "INNER JOIN SCHNO T2 ON T1.SCHREGNO = T2.SCHREGNO ");
            stb.append(    "WHERE T1.YEAR = '" + _param._year + "' ");
            stb.append(      "AND T1.DIV = '2' ");
        } else {
            stb.append(    "SELECT  T2.SCHREGNO, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ");
            }
            stb.append("         T1.SUBCLASSCD, ");
            stb.append("            VALUE(T1.ABSENCE_HIGH, 0) ");
            if (_param._printJougenTyuui) {
                if (_param._absenceWarnIsUnitCount) {
                    final String sem = "1".equals(_param._attendEndDateSemester) ? "" : _param._attendEndDateSemester;
                    stb.append("      - VALUE(T1.ABSENCE_WARN" + sem + ", 0) ");
                } else {
                    stb.append("      - VALUE(T1.ABSENCE_WARN_RISHU_SEM" + _param._attendEndDateSemester + ", 0) ");
                }
            }
            stb.append("            AS COMP_ABSENCE_HIGH, ");
            stb.append("            VALUE(T1.GET_ABSENCE_HIGH, 0) ");
            if (_param._printJougenTyuui) {
                if (_param._absenceWarnIsUnitCount) {
                    final String sem = "1".equals(_param._attendEndDateSemester) ? "" : _param._attendEndDateSemester;
                    stb.append("      - VALUE(T1.ABSENCE_WARN" + sem + ", 0) ");
                } else {
                    stb.append("      - VALUE(T1.ABSENCE_WARN_RISHU_SEM" + _param._attendEndDateSemester + ", 0) ");
                }
            }
            stb.append("            AS GET_ABSENCE_HIGH ");
            stb.append(    "FROM V_CREDIT_MST T1 ");
            stb.append(         "INNER JOIN SCHNO T2 ON ");
            stb.append(              "T1.GRADE = T2.GRADE ");
            stb.append(          "AND T1.COURSECD = T2.COURSECD ");
            stb.append(          "AND T1.MAJORCD = T2.MAJORCD ");
            stb.append(          "AND T1.COURSECODE = T2.COURSECODE ");
            stb.append(    "WHERE T1.YEAR = '" + _param._year + "' ");
            stb.append(          "AND EXISTS(SELECT 'X' FROM CHAIR_A T3 WHERE T3.SUBCLASSCD = T1.SUBCLASSCD AND T3.SCHREGNO = T2.SCHREGNO ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(      "AND T3.CLASSCD = T1.CLASSCD AND T3.SCHOOL_KIND = T1.SCHOOL_KIND AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(          "          )");
        }
        stb.append(") ");

        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T5.CLASSCD, ");
        //科目で集約する  (_param._isSubclsSummary = true )->科目コードを利用。それ以外は不要。
        if (_param._isSubclsSummary) {
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     T5.CLASSCD || '-' || T5.SCHOOL_KIND || '-' || T5.CURRICULUM_CD || '-' || ");
            }
            stb.append("     T5.SUBCLASSCD AS SUBCLASSCD, ");
        }
        stb.append("     VALUE(T5.ABSENT0,0) AS ABSENT0 ");
        stb.append("     ,VALUE(T5.ABSENT1,0) AS ABSENT ");
        stb.append("     ,VALUE(T5.JISU,0) AS JISU");
        stb.append("     ,VALUE(T5.KOKETSU,0) AS KOKETSU ");
        stb.append("     ,VALUE(T5.MOURNING,0) AS MOURNING ");
        stb.append("     ,VALUE(T5.SUSPEND,0) AS SUSPEND ");
        stb.append("     ,VALUE(T5.KOUDOME,0) AS KOUDOME ");
        stb.append("     ,VALUE(T5.VIRUS,0) AS VIRUS ");
        stb.append("     ,VALUE(T5.LATE_EARLY0,0) AS LATE_EARLY0 ");
        stb.append("     ,VALUE(T5.LATE_EARLY1,0) AS LATE_EARLY ");
        stb.append("     ,VALUE(T6.COMP_ABSENCE_HIGH,0) AS COMP_ABSENCE_HIGH ");
        stb.append("     ,VALUE(T6.GET_ABSENCE_HIGH,0) AS GET_ABSENCE_HIGH ");
        stb.append(" FROM ");
        stb.append("     SCHNO T1 ");
        //欠課数の表
        stb.append(" LEFT JOIN ( ");
        stb.append("   SELECT SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     CLASSCD, ");
        } else {
            stb.append("     SUBSTR(SUBCLASSCD, 1, 2) AS CLASSCD, ");
        }
        if (_param._isSubclsSummary) {
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("          SCHOOL_KIND, CURRICULUM_CD, ");
            }
            stb.append("          SUBCLASSCD, ");
        }
        stb.append("          SUM(ABSENT0) AS ABSENT0, ");
        stb.append("          SUM(ABSENT1) AS ABSENT1, ");
        stb.append("          SUM(JISU) AS JISU, ");
        stb.append("          SUM(KOKETSU) AS KOKETSU, ");
        stb.append("          SUM(MOURNING) AS MOURNING, ");
        stb.append("          SUM(SUSPEND) AS SUSPEND, ");
        stb.append("          SUM(KOUDOME) AS KOUDOME, ");
        stb.append("          SUM(VIRUS) AS VIRUS, ");
        stb.append("          SUM(LATE_EARLY0) AS LATE_EARLY0, ");
        stb.append("          SUM(LATE_EARLY1) AS LATE_EARLY1 ");
        stb.append("   FROM   ATTEND_B ");
        stb.append("   GROUP BY SCHREGNO ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     ,CLASSCD ");
        } else {
            stb.append("     ,SUBSTR(SUBCLASSCD, 1, 2) ");
        }
        if (_param._isSubclsSummary) {
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("          ,SCHOOL_KIND, CURRICULUM_CD ");
            }
            stb.append("          ,SUBCLASSCD ");
        }
        stb.append(" ) T5 ON T5.SCHREGNO = T1.SCHREGNO");
        //上限値の表
        stb.append(" LEFT JOIN ( ");
        stb.append("   SELECT SCHREGNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     CLASSCD, ");
        } else {
            stb.append("     SUBSTR(SUBCLASSCD, 1, 2) AS CLASSCD, ");
        }
        //科目で集約する  (_param._isSubclsSummary = true )->科目コードを利用。それ以外は不要。
        if (_param._isSubclsSummary) {
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("          SCHOOL_KIND, CURRICULUM_CD, ");
            }
            stb.append("          SUBCLASSCD, ");
        }
        stb.append("          SUM(COMP_ABSENCE_HIGH) AS COMP_ABSENCE_HIGH, ");
        stb.append("          SUM(GET_ABSENCE_HIGH) AS GET_ABSENCE_HIGH ");
        stb.append("   FROM   T_ABSENCE_HIGH ");
        stb.append("   GROUP BY SCHREGNO ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     ,CLASSCD ");
        } else {
            stb.append("     ,SUBSTR(SUBCLASSCD, 1, 2) ");
        }
        if (_param._isSubclsSummary) {
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("          ,SCHOOL_KIND, CURRICULUM_CD ");
            }
            stb.append("          ,SUBCLASSCD ");
        }
        stb.append(" ) T6 ON T6.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T6.CLASSCD = T5.CLASSCD ");
        if (_param._isSubclsSummary) {
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     AND T6.SCHOOL_KIND = T5.SCHOOL_KIND ");
                stb.append("     AND T6.CURRICULUM_CD = T5.CURRICULUM_CD ");
            }
            stb.append("     AND T6.SUBCLASSCD = T5.SUBCLASSCD ");
        }
        stb.append("  WHERE T1.SCHREGNO = '" + schregno + "' ");

        return stb.toString();
    }

    private class Student {
        final String _schregno;
        final String _attendno;
        final String _sex;
        final String _name;
        final String _nameEng;
        final String _hrName;
        final String _staffName;

        final String _transferCd1;
        final String _transferSdate1;
        final String _transferEdate1;
        final String _transferCd2;
        final String _transferSdate2;
        final String _transferEdate2;

        final List _subclassList = new ArrayList();
        final Map _attendMap;

        Student(final String schregno,
                final String attendno,
                final String sex,
                final String name,
                final String nameEng,
                final String hrName,
                final String staffName,
                final String transferCd1,
                final String transferSdate1,
                final String transferEdate1,
                final String transferCd2,
                final String transferSdate2,
                final String transferEdate2,
                final Map attendMap
        ) {
            _schregno = schregno;
            _attendno = attendno;
            _sex = sex;
            _name = name;
            _nameEng = nameEng;
            _hrName = hrName;
            _staffName = staffName;
            _transferCd1 = transferCd1;
            _transferSdate1 = transferSdate1;
            _transferEdate1 = transferEdate1;
            _transferCd2 = transferCd2;
            _transferSdate2 = transferSdate2;
            _transferEdate2 = transferEdate2;
            _attendMap = attendMap;
        }

        private String getAttendno() {
            return (null != _attendno) ? String.valueOf(Integer.parseInt(_attendno)) : "";
        }

        private String getSex() {
            return "2".equals(_sex) ? "*" : "";
        }

        private String getRyugaku() {
            if (null == _transferCd1) return "";
            String sdate = (null == _transferSdate1) ? "" : _transferSdate1.replace('-', '/');
            String edate = (null == _transferEdate1) ? "" : _transferEdate1.replace('-', '/');
            return "(" + sdate + FROM_TO_MARK + edate + ")";
        }

        private String getKyugaku() {
            if (null == _transferCd2) return "";
            String sdate = (null == _transferSdate2) ? "" : _transferSdate2.replace('-', '/');
            String edate = (null == _transferEdate2) ? "" : _transferEdate2.replace('-', '/');
            return "(" + sdate + FROM_TO_MARK + edate + ")";
        }

        private String getAttend(int attend) {
            return (0 < attend) ? String.valueOf(attend) : "";
        }
    }

    private static class AttDat {
        final String _subclasscd;  //_param._subclsSummaryフラグの値に応じてコードが設定/非設定になるので注意。
        final int _absent;
        final int _lateEarly;
        final int _koketsu;
        final int _mourningSuspend;
        final int _compAbsenceHigh;
        final int _getAbsenceHigh;
        public AttDat(
                final String subclasscd,
                final int absent,
                final int lateEarly,
                final int koketsu,
                final int mourningSuspend,
                final int compAbsenceHigh,
                final int getAbsenceHigh
                ) {
            _subclasscd = subclasscd;
            _absent = absent;
            _lateEarly = lateEarly;
            _koketsu = koketsu;
            _mourningSuspend = mourningSuspend;
            _compAbsenceHigh = compAbsenceHigh;
            _getAbsenceHigh = getAbsenceHigh;
        }
        // 履修上限値オーバー
        public boolean compIsOver() {
            return judgeOver(_absent, _compAbsenceHigh);
        }

        // 修得上限値オーバー
        public boolean getIsOver() {
            return judgeOver(_absent, _getAbsenceHigh);
        }

        private boolean judgeOver(final int absent, final int absenceHigh) {
            if (absenceHigh < absent) {
                return true;
            }
            return false;
        }
    }
    private static class Subclass {
        final String _classcd;
        final String _classname;
        final String _subclasscd;
        final String _subclassname;
        final String _totalStudyTime;
        final String _totalStudyAct;
        public Subclass(
                final String classcd,
                final String classname,
                final String subclasscd,
                final String subclassname,
                final String totalStudyTime,
                final String totalStudyAct
                ) {
            _classcd = classcd;
            _classname = classname;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _totalStudyTime = totalStudyTime;
            _totalStudyAct = totalStudyAct;
        }

        private String getTotalStudyTime() {
            if (null == _totalStudyTime) return "";
            String[] rtnArr = KNJ_EditEdit.get_token(_totalStudyTime, 50, 3);
            if (null == rtnArr) return "";
            String rtnStr = "";
            for (int i = 0; i < rtnArr.length; i++) {
                String str = rtnArr[i];
                if (null == str) continue;
                if (str.charAt(str.length() - 1) == '\r') str = str.substring(0, str.length() - 1);
                rtnStr = rtnStr.concat(str);
            }
            return rtnStr;
        }

        private String getTotalStudyAct() {
            if (null == _totalStudyAct) return "";
            String[] rtnArr = KNJ_EditEdit.get_token(_totalStudyAct, 50, 2);
            if (null == rtnArr) return "";
            String rtnStr = "";
            for (int i = 0; i < rtnArr.length; i++) {
                String str = rtnArr[i];
                if (null == str) continue;
                if (str.charAt(str.length() - 1) == '\r') str = str.substring(0, str.length() - 1);
                rtnStr = rtnStr.concat(str);
            }
            return rtnStr;
        }

    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72715 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _year;
        private final String _semester;
        private final String[] _hrclass;
        private final String _useCurriculumcd;
        private final String _useVirus;
        private final String _useKoudome;
        private final String _chikokuHyoujiFlg;
        private final String _useTestCountflg;

        private final boolean _printForm45;
        private final boolean _printForm50;
        private final boolean _printJougenTyuui;
        private final boolean _printJougenTyouka;
        private final boolean _printAttendRuikei;
        private final boolean _printAttendGakki;

        private final String _sDate; //出欠集計開始日付
        private final String _eDate; //出欠集計終了日付

        private KNJSchoolMst _knjSchoolMst;
        private KNJDefineSchool _definecode;
        private Map _attendSemesMap;
        private Map _hasuuMap;
        private boolean _semesFlg;

        private boolean _absenceWarnIsUnitCount;
        private String _attendEndDateSemester;

        /** 氏名欄に学籍番号の表示/非表示 1:表示 それ以外:非表示 */
        final String _use_SchregNo_hyoji;

        /** 所見は学期ごとに入力、印刷する */
        final String _knjd134PrintEachSemester;

        private final StaffInfo _staffInfo;
        private final boolean _isSubclsSummary = false;  //true:科目コードで出欠を集約。科目名称で出力。　false:教科コードで出欠を集約。教科名称で出力。学習内容などは結合して出力。

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException, Exception {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _hrclass = request.getParameterValues("CLASS_SELECTED");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _chikokuHyoujiFlg = request.getParameter("chikokuHyoujiFlg");
            _useTestCountflg = request.getParameter("useTestCountflg");

            final String formSelect = request.getParameter("FORM_SELECT");
            _printForm45 = "1".equals(formSelect);
            _printForm50 = "2".equals(formSelect);
            final String tyuuiTyouka = request.getParameter("TYUI_TYOUKA");
            _printJougenTyuui  = "1".equals(tyuuiTyouka);
            _printJougenTyouka = "2".equals(tyuuiTyouka);
            final String dateDiv = request.getParameter("DATE_DIV");
            _printAttendRuikei = "1".equals(dateDiv);
            _printAttendGakki  = "2".equals(dateDiv);

            final String sDate = request.getParameter("SDATE");
            _sDate = sDate.replace('/', '-');
            final String eDate = request.getParameter("EDATE");
            _eDate = eDate.replace('/', '-');

            _use_SchregNo_hyoji = request.getParameter("use_SchregNo_hyoji");
            _knjd134PrintEachSemester = request.getParameter("knjd134PrintEachSemester");

            _knjSchoolMst = new KNJSchoolMst(db2, _year);
            _definecode = new KNJDefineSchool();
            _definecode.defineCode(db2, _year);
            _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, null, _year);
            _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap, _sDate, _eDate);
            _semesFlg = ((Boolean) _hasuuMap.get("semesFlg")).booleanValue();

            loadNameMstC042(db2);
            loadAttendEdateSemester(db2);

            final String printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _staffInfo = new StaffInfo(db2, printLogStaffcd);
        }

        private String getFormId() {
            return (_param._printForm50) ? "KNJD134_1.frm" : "KNJD134_2.frm";
        }

        private String getNendo(final DB2UDB db2) {
            return (KNJ_EditDate.isSeireki(db2) ? _param._year : KNJ_EditDate.gengou(db2, Integer.parseInt(_year))) + "年度";
        }

        private String getCtrlDate(final DB2UDB db2) {
            return KNJ_EditDate.isSeireki(db2) ? KNJ_EditDate.h_format_SeirekiJP(_ctrlDate) : KNJ_EditDate.h_format_JP(db2, _ctrlDate);
        }

        private String getAttendTerm(final DB2UDB db2) {
            final String sDStr = KNJ_EditDate.isSeireki(db2) ? KNJ_EditDate.h_format_SeirekiJP(_sDate) : KNJ_EditDate.h_format_JP(db2, _sDate);
            final String eDStr = KNJ_EditDate.isSeireki(db2) ? KNJ_EditDate.h_format_SeirekiJP(_eDate) : KNJ_EditDate.h_format_JP(db2, _eDate);
            return sDStr + FROM_TO_MARK + eDStr;
        }

        /**
         * 単位マスタの警告数は単位が回数か
         * @param db2
         * @throws SQLException
         */
        private void loadNameMstC042(final DB2UDB db2) throws SQLException {
            final String sql = "SELECT NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'C042' AND NAMECD2 = '01' ";
            final PreparedStatement ps = db2.prepareStatement(sql);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                _absenceWarnIsUnitCount = "1".equals(rs.getString("NAMESPARE1"));
                log.debug("(名称マスタ C042) =" + _absenceWarnIsUnitCount);
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        private String loadAttendEdateSemester(DB2UDB db2) throws SQLException {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT T1.YEAR, T1.SEMESTER, T1.SDATE, T1.EDATE, T2.SEMESTER AS NEXT_SEMESTER, T2.SDATE AS NEXT_SDATE ");
            stb.append(" FROM SEMESTER_MST T1 ");
            stb.append(" LEFT JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND INT(T2.SEMESTER) = INT(T1.SEMESTER) + 1 ");
            stb.append(" WHERE T1.YEAR = '" + _year + "' ");
            stb.append("     AND T1.SEMESTER <> '9' ");
            stb.append("     AND (('" + _eDate + "' BETWEEN T1.SDATE AND T1.EDATE) ");
            stb.append("          OR ('" + _eDate + "' BETWEEN T1.EDATE AND VALUE(T2.SDATE, '9999-12-30'))) ");

            PreparedStatement ps = db2.prepareStatement(stb.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                _attendEndDateSemester = rs.getString("SEMESTER");
            }
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
            return _attendEndDateSemester;
        }
    }
}

// eof
