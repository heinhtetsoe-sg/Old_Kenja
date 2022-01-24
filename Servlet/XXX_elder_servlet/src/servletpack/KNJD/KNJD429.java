/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 8bdec3c34b8de1ae46e6d117dad0efa85d36b21c $
 *
 * 作成日: 2019/04/10
 * 作成者: yamashiro
 *
 * Copyright(C) 2019-2024 ALP Okinawa Co.,Ltd. All rights reserved.
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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD429 {

    private static final Log log = LogFactory.getLog(KNJD429.class);

    private static final String SEMEALL = "9";

    private boolean _hasData;
    private final String HOUTEI = "1";
    private final String JITSU = "2";

    final int FIRST_PAGE_MAXLINE = 36;
    final int PAGE_MAXLINE = 54;
    boolean IS_FIRST_PRINT = false;
    int LINE_CNT = 0;
    final String PRINT_FRM1 = "KNJD426_1.frm";
    final String PRINT_FRM2 = "KNJD426_2.frm";
    String PRINT_FRM_NOW = "";

    private Param _param;

    /**
     * @param request
     *            リクエスト
     * @param response
     *            レスポンス
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

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
        final List studentList = getStudentList(db2);
        //下段の出欠
        for (final Iterator rit = _param._attendRanges.values().iterator(); rit.hasNext();) {
            final DateRange range = (DateRange) rit.next();
            Attendance.load(db2, _param, studentList, range);
        }
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();
            if (_param._isPrintHyosi) {
                printHyoushi(db2, svf, student);
            }

            //各ブロックを印字
            for (Iterator itPrintBlock = student._blockMap.keySet().iterator(); itPrintBlock.hasNext();) {
                final String kindNo = (String) itPrintBlock.next();
                final Block block = (Block) student._blockMap.get(kindNo);
                if (null != block) {
                    block.printOut(svf, student);
                }
            }

            printAttend(svf, student);

            if (_param._isPrintHyosi && !"1".equals(_param._printSize)) {  //A4(裏表紙)
                svf.VrEndPage();
                if (_param._semesterMap.size() == 3) {
                	svf.VrSetForm("KNJD429_1_4.frm", 1);
                } else {
                	svf.VrSetForm("KNJD429_1_5.frm", 1);
                }
            	printUrabyoushi(db2, svf, student);
                svf.VrEndPage();
            }
            _hasData = true;
        }
    }

    private void printHyoushi(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String setForm;
        if ("1".equals(_param._printSize)) {  //A3出力
            if (_param._semesterMap.size() == 3) {
                setForm = "KNJD429_1_1.frm";
            } else {
                setForm = "KNJD429_1_2.frm";
            }
        } else {
        	setForm = "KNJD429_1_3.frm";  //A4(表紙のみ)
        }
        svf.VrSetForm(setForm, 1);
        PRINT_FRM_NOW = setForm;
        if ("1".equals(_param._printSize)) {  //A3出力
            printUrabyoushi(db2, svf, student);
        }
        final String gradeHrName = student._gakubuName + "　" + student._gradeName1;
        final CertifSchool certifSchool = (CertifSchool) _param._certifSchoolMap.get(student._schoolKind);
        if (null != certifSchool) {
            //学校名
            svf.VrsOut("SCHOOL_NAME", certifSchool._schoolName);
            //校長名
            final int principalLen = KNJ_EditEdit.getMS932ByteLength(certifSchool._principalName);
            final String setPriField2 = principalLen > 30 ? "3" : principalLen > 20 ? "2" : "1";
            svf.VrsOut("PRINCIPAL_NAME" + setPriField2, certifSchool._principalName);
        }

        //校章
        svf.VrsOut("SCHOOL_LOGO", _param._schoolLogoImagePath);
        //年度
        svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear)) + "年度");
        //氏名
        int nameLen = KNJ_EditEdit.getMS932ByteLength(student._name);
        final String nameField = nameLen > 30 ? "3" : nameLen > 18 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name);

        //担任
        int setLine = 1;
        setLine = setStaff(svf, student._tannin1, setLine);
        setLine = setStaff(svf, student._tannin2, setLine);
        setLine = setStaff(svf, student._tannin3, setLine);
        setLine = setStaff(svf, student._tannin4, setLine);

        //タイトル
        final Map conditionMap = (Map) _param._hreportConditionMap.get(student._schoolKind);
        if (null != conditionMap) {
            final HreportCondition hreportCondition1 = (HreportCondition) conditionMap.get("101");
            if (null != hreportCondition1) {
                svf.VrsOut("TITLE", hreportCondition1._remark10);
            }
            //クラス名
            final HreportCondition hreportCondition4 = (HreportCondition) conditionMap.get("104");
            if (null != hreportCondition4) {
                final String setHrName = "1".equals(hreportCondition4._remark1) ? student._hrName : "";
                svf.VrsOut("HR_NAME", gradeHrName + "　" + setHrName);
            }
        }
        svf.VrEndPage();
    }

    private void printUrabyoushi(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        //修了文言
        final String gradeHrName = student._gakubuName + "　" + student._gradeName1;
        svf.VrsOut("TEXT", gradeHrName + "の課程を修了したことを証する。");
        //日付フォーマットのみ
        final String[] dateArray = KNJ_EditDate.tate_format4(db2, _param._ctrlDate.replace('/', '-'));
        svf.VrsOut("DATE", dateArray[0] + "　　年　　月　　日");
        final CertifSchool certifSchool = (CertifSchool) _param._certifSchoolMap.get(student._schoolKind);
        if (null != certifSchool) {
        	//学校名
            svf.VrsOut("SCHOOL_NAME2", certifSchool._schoolName);
            //職種
            svf.VrsOut("JOB_NAME", certifSchool._jobName);
            //校長名
            final int principalLen = KNJ_EditEdit.getMS932ByteLength(certifSchool._principalName);
            final String setPriField = principalLen > 20 ? "3" : principalLen > 16 ? "2" : "1";
            svf.VrsOut("PRINCIPAL_NAME2_" + setPriField, certifSchool._principalName);
        }
    }

    private int setStaff(final Vrw32alp svf, final String staffName, final int setLine) {
        int retInt = setLine;
        final int staffLen = KNJ_EditEdit.getMS932ByteLength(staffName);
        final String staffField = staffLen > 30 ? "3" : staffLen > 20 ? "2" : "1";
        svf.VrsOutn("STAFF_NAME" + staffField, setLine, staffName);
        retInt++;
        return retInt;
    }

    // 行動、身体、出欠の記録
    private void printAttend(final Vrw32alp svf, final Student student) {
        String attendFrm = _param.getAttendFrm(student);
        if (StringUtils.isBlank(attendFrm)) {
        	log.error("attendFrm null.");
        	return;
        }
		svf.VrSetForm(attendFrm, 1);
        if (_param._lastSemester.equals(_param._semester)) {
            svf.VrsOut("BLANK", _param._whiteSpaceImagePath);
        }
        final Semester semesterObj = (Semester) _param._semesterMap.get(_param._semester);

        svf.VrsOut("TITLE", semesterObj._semestername + "の記録");
        svf.VrsOut("NAME", "氏名 (" + student._name + ")");

        //行動の記録
        svf.VrsOut("SEMESTER1_1", semesterObj._semestername);
        svf.VrsOut("SEMESTER1_2", semesterObj._semestername);
        int koudouCnt = 1;
        String koudouField = "1";
        for (Iterator itD035 = _param._d035.keySet().iterator(); itD035.hasNext();) {
            final String code = (String) itD035.next();
            final String viewName = (String) _param._d035.get(code);
            final String record = (String) student._behaviorSemeMap.get(code);
            if (koudouCnt > 5) {
                koudouField = "2";
                koudouCnt = 1;
            }
            svf.VrsOutn("VIEW_NAME" + koudouField, koudouCnt, viewName);
            svf.VrsOutn("VIEW" + koudouField, koudouCnt, StringUtils.isEmpty(record) ? "" : record);
            koudouCnt++;
        }

        //身体の記録
        int monthCol = 1;
        for (Iterator itMedexamMonth = student._medexamMonthList.iterator(); itMedexamMonth.hasNext();) {
            final MedexamMonth medexamMonth = (MedexamMonth) itMedexamMonth.next();
            if (Integer.parseInt(medexamMonth._semester) <= Integer.parseInt(_param._semester)) {
                svf.VrsOut("MONTH" + monthCol, Integer.parseInt(medexamMonth._month) + "月");
                svf.VrsOut("HIGHT" + monthCol, medexamMonth._height);
                svf.VrsOut("WEIGHT" + monthCol, medexamMonth._weight);
                svf.VrsOut("EYE" + monthCol + "_1_1", medexamMonth._rBarevisionMark);
                svf.VrsOut("EYE" + monthCol + "_1_2", medexamMonth._rVisionMark);
                svf.VrsOut("EYE" + monthCol + "_2_1", medexamMonth._lBarevisionMark);
                svf.VrsOut("EYE" + monthCol + "_2_2", medexamMonth._lVisionMark);
                monthCol++;
            }
        }
        if (null != student._medexamdet) {
        	svf.VrsOut("EAR1_1", student._medexamdet._rEar);
        	svf.VrsOut("EAR2_1", student._medexamdet._rEarIn);
        	svf.VrsOut("EAR1_2", student._medexamdet._lEar);
        	svf.VrsOut("EAR2_2", student._medexamdet._lEarIn);
        }

        //出欠の記録
        for (final Iterator it = _param._semesterMap.keySet().iterator(); it.hasNext();) {
            final String semester = (String) it.next();
            final Semester semesterObj2 = (Semester) _param._semesterMap.get(semester);
            svf.VrsOut("SEMESTER2_" + semester, semesterObj2._semestername);
            if (!"9".equals(semester) && Integer.parseInt(semester) > Integer.parseInt(_param._semester)) {
                continue;
            }

            int line = 1;
            final Attendance att = (Attendance) student._attendMap.get(semester);
            if (null != att) {
                svf.VrsOutn("ATTEND" + semester, line++, String.valueOf(att._lesson));  // 授業日数
                svf.VrsOutn("ATTEND" + semester, line++, String.valueOf(att._suspend + att._mourning)); // 忌引出停日数
                svf.VrsOutn("ATTEND" + semester, line++, String.valueOf(att._mLesson)); // 出席しなければならない日数
                svf.VrsOutn("ATTEND" + semester, line++, String.valueOf(att._absent));  // 欠席日数
                svf.VrsOutn("ATTEND" + semester, line++, String.valueOf(att._present)); // 出席日数

                final String remarkField = KNJ_EditEdit.getMS932ByteLength(att._remark) > 20 ? "2" : "1";
                svf.VrsOut("ATTEND_REMARK" + semester + "_" + remarkField, att._remark); // 備考
                if (semester.equals(_param._semester)) {
                    if (null != att._communication) {
                        final String[] communicationArray = KNJ_EditEdit.get_token(att._communication, 66, 8);
                        if (null != communicationArray) {
                        	for (int i = 0; i < communicationArray.length; i++) {
                        		final String setText = communicationArray[i];
                        		svf.VrsOutn("FROM_SCHOOL", i + 1, setText); // 学校より
                        	}
                        }
                    }
                }
            }
        }
        svf.VrEndPage();
    }

    private List getStudentList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String studentSql = getStudentSql();
            log.debug(" sql =" + studentSql);
            ps = db2.prepareStatement(studentSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String gradeName1 = rs.getString("GRADE_NAME1");
                final String ghrCd = rs.getString("GHR_CD");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String gakubuName = rs.getString("GAKUBU_NAME");
                final String hrName = rs.getString("HR_NAME");
                final String tannin1 = rs.getString("TANNIN1");
                final String tannin2 = rs.getString("TANNIN2");
                final String tannin3 = rs.getString("TANNIN3");
                final String tannin4 = rs.getString("TANNIN4");
                final String ghrName = rs.getString("GHR_NAME");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String birthDay = rs.getString("BIRTHDAY");
                final String sexName = rs.getString("SEX_NAME");

                final Student student = new Student(schregNo, schoolKind, gradeName1, ghrCd, grade, hrClass, gakubuName, hrName, tannin1, tannin2, tannin3, tannin4, ghrName, name, nameKana, birthDay, sexName);
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        try {
        	for (Iterator it = retList.iterator(); it.hasNext();) {
				Student student = (Student) it.next();
                student.setHreportRemarkDetail(db2);
                student.setBehaviorSeme(db2);
                student.setMedexam(db2);
                student.setSubclassSeqMap(db2);
                student.setBlock8(db2);
			}

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     BASE.SCHREGNO, ");
        stb.append("     GDAT.SCHOOL_KIND, ");
        stb.append("     GDAT.GRADE_NAME1, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     TANNIN1.STAFFNAME AS TANNIN1, ");
        stb.append("     TANNIN2.STAFFNAME AS TANNIN2, ");
        stb.append("     TANNIN3.STAFFNAME AS TANNIN3, ");
        stb.append("     TANNIN4.STAFFNAME AS TANNIN4, ");
        stb.append("     GHR.GHR_CD, ");
        stb.append("     GHRH.GHR_NAME, ");
        stb.append("     A023.ABBV1 AS GAKUBU_NAME, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     BASE.BIRTHDAY, ");
        stb.append("     Z002.NAME2 AS SEX_NAME ");
        stb.append(" FROM ");
        stb.append("     V_SCHREG_BASE_MST BASE ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ");
        stb.append("                                  AND REGD.GRADE = GDAT.GRADE ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
        stb.append("                                  AND REGD.SEMESTER = REGDH.SEMESTER  ");
        stb.append("                                  AND REGD.GRADE || REGD.HR_CLASS = REGDH.GRADE || REGDH.HR_CLASS  ");
        stb.append("     LEFT JOIN STAFF_MST TANNIN1 ON TANNIN1.STAFFCD = REGDH.TR_CD1 ");
        stb.append("     LEFT JOIN STAFF_MST TANNIN2 ON TANNIN2.STAFFCD = REGDH.TR_CD2 ");
        stb.append("     LEFT JOIN STAFF_MST TANNIN3 ON TANNIN3.STAFFCD = REGDH.TR_CD3 ");
        stb.append("     LEFT JOIN STAFF_MST TANNIN4 ON TANNIN4.STAFFCD = REGDH.SUBTR_CD1 ");
        stb.append("     LEFT JOIN SCHREG_REGD_GHR_DAT GHR ON GHR.SCHREGNO = REGD.SCHREGNO ");
        stb.append("                                     AND GHR.YEAR =REGD.YEAR ");
        stb.append("                                     AND GHR.SEMESTER = REGD.SEMESTER ");
        stb.append("     LEFT JOIN SCHREG_REGD_GHR_HDAT GHRH ON GHRH.YEAR = GHR.YEAR ");
        stb.append("                                      AND GHRH.SEMESTER = GHR.SEMESTER ");
        stb.append("                                      AND GHRH.GHR_CD = GHR.GHR_CD ");
        stb.append("     LEFT JOIN NAME_MST A023 ON A023.NAMECD1 = 'A023' ");
        stb.append("                            AND A023.NAME1 = GDAT.SCHOOL_KIND ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("                            AND BASE.SEX = Z002.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     BASE.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append("     AND REGD.YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        stb.append(" ORDER BY ");
        stb.append("     GHR.GHR_CD, ");
        stb.append("     GHR.GHR_ATTENDNO, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");

        return stb.toString();
    }

    private class Student {
        final String _schregNo;
        final String _schoolKind;
        final String _gradeName1;
        final String _ghrCd;
        final String _grade;
        final String _hrClass;
        final String _gakubuName;
        final String _hrName;
        final String _tannin1;
        final String _tannin2;
        final String _tannin3;
        final String _tannin4;
        final String _ghrName;
        final String _name;
        final String _nameKana;
        final String _birthDay;
        final String _sexName;
        final Map _subclassMap;
        final Map _blockMap;
        final Map _attendMap = new TreeMap();
        final List _medexamMonthList = new ArrayList();
        Medexamdet _medexamdet;
        String _hrepRemarkDetailR1;
        String _hrepRemarkDetailR2;
        final Map _behaviorSemeMap;

        public Student(final String schregNo, final String schoolKind, final String gradeName1, final String ghrCd, final String grade,
                final String hrClass, final String gakubuName, final String hrName, final String tannin1, final String tannin2,
                final String tannin3, final String tannin4, final String ghrName, final String name, final String nameKana,
                final String birthDay, final String sexName) {
            _schregNo = schregNo;
            _schoolKind = schoolKind;
            _gradeName1 = gradeName1;
            _ghrCd = StringUtils.isEmpty(ghrCd) ? "00" : ghrCd;
            _grade = grade;
            _hrClass = hrClass;
            _gakubuName = gakubuName;
            _hrName = hrName;
            _tannin1 = StringUtils.defaultString(tannin1);
            _tannin2 = StringUtils.defaultString(tannin2);
            _tannin3 = StringUtils.defaultString(tannin3);
            _tannin4 = StringUtils.defaultString(tannin4);
            _ghrName = ghrName;
            _name = name;
            _nameKana = nameKana;
            _birthDay = birthDay;
            _sexName = sexName;
            _subclassMap = new LinkedMap();
            _blockMap = new TreeMap();
            _behaviorSemeMap = new TreeMap();
        }

        private String getHrName() {
            if (HOUTEI.equals(_param._hukusikiRadio)) {
                return _hrName;
            } else {
                return _ghrName;
            }
        }

        private void setMedexam(final DB2UDB db2) throws SQLException {
            final String medexamMonthSql = getMedexamMonthSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(medexamMonthSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String month = rs.getString("MONTH");
                    final String height = rs.getString("HEIGHT");
                    final String weight = rs.getString("WEIGHT");
                    final String rBarevisionMark = rs.getString("R_BAREVISION_MARK");
                    final String rVisionMark = rs.getString("R_VISION_MARK");
                    final String lBarevisionMark = rs.getString("L_BAREVISION_MARK");
                    final String lVisionMark = rs.getString("L_VISION_MARK");
                    final MedexamMonth medexamMonth = new MedexamMonth(semester, month, height, weight, rBarevisionMark, rVisionMark, lBarevisionMark, lVisionMark);
                    _medexamMonthList.add(medexamMonth);
                }
            } catch (SQLException ex) {
                log.error("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            final String medexamSql = getMedexamDetSql();
            ps = null;
            rs = null;
            try {
                ps = db2.prepareStatement(medexamSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String rEar = rs.getString("R_EAR");
                    final String rEarIn = rs.getString("R_EAR_IN");
                    final String lEar = rs.getString("L_EAR");
                    final String lEarIn = rs.getString("L_EAR_IN");
                    _medexamdet = new Medexamdet(rEar, rEarIn, lEar, lEarIn);
                }
            } catch (SQLException ex) {
                log.error("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

        }

        private void setHreportRemarkDetail(final DB2UDB db2) {
            final String hreportRemarkDetailSql = getHreportRemarkDetailSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            _hrepRemarkDetailR1 = "";
            _hrepRemarkDetailR2 = "";
            try {
                ps = db2.prepareStatement(hreportRemarkDetailSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _hrepRemarkDetailR1 = rs.getString("REMARK1");
                    _hrepRemarkDetailR2 = rs.getString("REMARK2");
                }
            } catch (SQLException ex) {
                log.error("setSubclassList exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getHreportRemarkDetailSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REMARK1, ");
            stb.append("     REMARK2 ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DETAIL_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND SEMESTER = '9' ");
            stb.append("     AND SCHREGNO = '" + _schregNo + "' ");
            stb.append("     AND DIV = '06' ");
            stb.append("     AND CODE = '01' ");

            return stb.toString();
        }

        private void setBehaviorSeme(final DB2UDB db2) {
            final String behaviorSemeSql = getBehaviorSemeSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(behaviorSemeSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("CODE");
                    final String record = rs.getString("RECORD");
                    _behaviorSemeMap.put(code, record);
                }
            } catch (SQLException ex) {
                log.error("setSubclassList exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getBehaviorSemeSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     BEHAVIOR.CODE, ");
            stb.append("     D036.NAMESPARE1 AS RECORD ");
            stb.append(" FROM ");
            stb.append("     BEHAVIOR_SEMES_DAT BEHAVIOR ");
            stb.append("     LEFT JOIN NAME_MST D036 ON D036.NAMECD1 = 'D036' ");
            stb.append("          AND BEHAVIOR.RECORD = D036.NAME1 ");
            stb.append(" WHERE ");
            stb.append("     BEHAVIOR.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND BEHAVIOR.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND BEHAVIOR.SCHREGNO = '" + _schregNo + "' ");
            stb.append(" ORDER BY ");
            stb.append("     BEHAVIOR.CODE ");

            return stb.toString();
        }

        private String getMedexamMonthSql() {
            final Map conditionMap = (Map) _param._hreportConditionMap.get(_schoolKind);
            final HreportCondition hreportCondition5;
            if (null != conditionMap) {
                hreportCondition5 = (HreportCondition) conditionMap.get("105");
            } else {
                hreportCondition5 = new HreportCondition("", "", "", "", "", "", "", "", "", "");
            }
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEMESTER, ");
            stb.append("     MONTH, ");
            stb.append("     HEIGHT, ");
            stb.append("     WEIGHT, ");
            stb.append("     R_BAREVISION_MARK, ");
            stb.append("     R_VISION_MARK, ");
            stb.append("     L_BAREVISION_MARK, ");
            stb.append("     L_VISION_MARK ");
            stb.append(" FROM ");
            stb.append("     MEDEXAM_DET_MONTH_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND SCHREGNO = '" + _schregNo + "' ");
            stb.append("     AND SEMESTER || MONTH IN ('" + hreportCondition5._remark1 + "', '" + hreportCondition5._remark2 + "') ");
            stb.append(" ORDER BY ");
            stb.append("     SEMESTER, INT(MONTH) + CASE WHEN INT(MONTH) < 4 THEN 12 ELSE 0 END ");

            return stb.toString();
        }

        private String getMedexamDetSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     F010R1.NAME1 AS R_EAR, ");
            stb.append("     F010R2.NAME1 AS R_EAR_IN, ");
            stb.append("     F010L1.NAME1 AS L_EAR, ");
            stb.append("     F010L2.NAME1 AS L_EAR_IN ");
            stb.append(" FROM ");
            stb.append("     V_MEDEXAM_DET_DAT MEDEXAM ");
            stb.append("     LEFT JOIN NAME_MST F010R1 ON F010R1.NAMECD1 = 'F010' ");
            stb.append("          AND MEDEXAM.R_EAR = F010R1.NAMECD2 ");
            stb.append("     LEFT JOIN NAME_MST F010R2 ON F010R2.NAMECD1 = 'F010' ");
            stb.append("          AND MEDEXAM.R_EAR_IN = F010R2.NAMECD2 ");
            stb.append("     LEFT JOIN NAME_MST F010L1 ON F010L1.NAMECD1 = 'F010' ");
            stb.append("          AND MEDEXAM.L_EAR = F010L1.NAMECD2 ");
            stb.append("     LEFT JOIN NAME_MST F010L2 ON F010L2.NAMECD1 = 'F010' ");
            stb.append("          AND MEDEXAM.L_EAR_IN = F010L2.NAMECD2 ");
            stb.append(" WHERE ");
            stb.append("     MEDEXAM.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND MEDEXAM.SCHREGNO = '" + _schregNo + "' ");
            stb.append("  ");

            return stb.toString();
        }

        private void setSubclassSeqMap(final DB2UDB db2) {
            final String subclassSql = getSubclassSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(subclassSql);
                rs = ps.executeQuery();
                int setSeq = 1;
                while (rs.next()) {
                    final String classCd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String subclassName = rs.getString("SUBCLASSNAME");
                    final Subclass subclass = new Subclass(classCd, schoolKind, curriculumCd, subclassCd, subclassName, "");
                    _subclassMap.put(String.valueOf(setSeq), subclass);
                    setSeq++;
                }
            } catch (SQLException ex) {
                log.error("setSubclassList exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getSubclassSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T3.CLASSCD, ");
            stb.append("     T3.SCHOOL_KIND, ");
            stb.append("     T3.CURRICULUM_CD, ");
            stb.append("     T3.SUBCLASSCD, ");
            stb.append("     T3.SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("     GRADE_KIND_SCHREG_GROUP_DAT T1 ");
            stb.append("     LEFT JOIN GRADE_KIND_COMP_GROUP_DAT T2 ");
            stb.append("          ON T2.YEAR = T1.YEAR ");
            stb.append("          AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("          AND T2.GAKUBU_SCHOOL_KIND = T1.GAKUBU_SCHOOL_KIND ");
            stb.append("          AND T2.GHR_CD = T1.GHR_CD ");
            stb.append("          AND T2.GRADE = T1.GRADE ");
            stb.append("          AND T2.HR_CLASS = T1.HR_CLASS ");
            stb.append("          AND T2.CONDITION = T1.CONDITION ");
            stb.append("          AND T2.GROUPCD = T1.GROUPCD ");
            stb.append("     LEFT JOIN V_SUBCLASS_MST T3 ");
            stb.append("          ON T3.YEAR = T2.YEAR ");
            stb.append("          AND T3.CLASSCD = T2.CLASSCD ");
            stb.append("          AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("          AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ");
            stb.append("          AND T3.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '9' ");
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + _grade + _hrClass + "' ");
            stb.append("     AND T1.SCHREGNO = '" + _schregNo + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T3.CLASSCD, ");
            stb.append("     T3.SCHOOL_KIND, ");
            stb.append("     T3.CURRICULUM_CD, ");
            stb.append("     T3.SUBCLASSCD ");

            return stb.toString();
        }

        private void setBlock8(final DB2UDB db2) {
            Block8 block8 = null;
            final String aPaternSql = getBlock8PaternSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(aPaternSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String patern = rs.getString("PATERN");
                    final String useSemes = rs.getString("USE_SEMES");
                    final String guidancePattern = rs.getString("GUIDANCE_PATTERN");
                    final String gakubuSchoolKind = rs.getString("GAKUBU_SCHOOL_KIND");
                    final String ghrCd = rs.getString("GHR_CD");
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String condition = rs.getString("CONDITION");
                    final String groupCd = rs.getString("GROUPCD");
                    block8 = new Block8(patern, useSemes, guidancePattern, gakubuSchoolKind, ghrCd, grade, hrClass, condition, groupCd, _schoolKind);
                }
            } catch (SQLException ex) {
                log.error("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if (null != block8) {
                block8.setTitle(db2);
                block8.setSubclassMap(db2, _schregNo);
            }
            _blockMap.put("08", block8);
        }

        private String getBlock8PaternSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     SUBSTR(A035.NAMESPARE1, 2, 1) AS PATERN, ");
            stb.append("     A035.NAMESPARE2 AS USE_SEMES, ");
            stb.append("     L1.GUIDANCE_PATTERN, ");
            stb.append("     L1.GAKUBU_SCHOOL_KIND, ");
            stb.append("     L1.GHR_CD, ");
            stb.append("     L1.GRADE, ");
            stb.append("     L1.HR_CLASS, ");
            stb.append("     L1.CONDITION, ");
            stb.append("     T1.GROUPCD ");
            stb.append(" FROM ");
            stb.append("     GRADE_KIND_SCHREG_GROUP_DAT T1 ");
            stb.append("     INNER JOIN GRADE_KIND_COMP_GROUP_YMST L1 ON L1.YEAR = T1.YEAR ");
            stb.append("          AND T1.SEMESTER    = L1.SEMESTER ");
            stb.append("          AND T1.GAKUBU_SCHOOL_KIND = L1.GAKUBU_SCHOOL_KIND ");
            stb.append("          AND T1.GHR_CD      = L1.GHR_CD ");
            stb.append("          AND T1.GRADE       = L1.GRADE ");
            stb.append("          AND T1.HR_CLASS    = L1.HR_CLASS ");
            stb.append("          AND T1.CONDITION   = L1.CONDITION ");
            stb.append("          AND T1.GROUPCD     = L1.GROUPCD ");
            stb.append("     INNER JOIN NAME_MST A035 ON A035.NAMECD1 = 'A035' ");
            stb.append("           AND L1.GUIDANCE_PATTERN = A035.NAMECD2 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '9' ");
            stb.append("     AND T1.SCHREGNO = '" + _schregNo + "' ");

            return stb.toString();
        }
    }

    private class MedexamMonth {
        final String _semester;
        final String _month;
        final String _height;
        final String _weight;
        final String _rBarevisionMark;
        final String _rVisionMark;
        final String _lBarevisionMark;
        final String _lVisionMark;

        public MedexamMonth(
                final String semester,
                final String month,
                final String height,
                final String weight,
                final String rBarevisionMark,
                final String rVisionMark,
                final String lBarevisionMark,
                final String lVisionMark
        ) {
            _semester = semester;
            _month = month;
            _height = height;
            _weight = weight;
            _rBarevisionMark = rBarevisionMark;
            _rVisionMark = rVisionMark;
            _lBarevisionMark = lBarevisionMark;
            _lVisionMark = lVisionMark;
        }
    }

    private class Medexamdet {
        final String _rEar;
        final String _rEarIn;
        final String _lEar;
        final String _lEarIn;

        public Medexamdet(
                final String rEar,
                final String rEarIn,
                final String lEar,
                final String lEarIn
        ) {
            _rEar = rEar;
            _rEarIn = rEarIn;
            _lEar = lEar;
            _lEarIn = lEarIn;
        }
    }

    abstract private class Block {
        protected boolean _pageChange;
        protected String _kindNo;
        protected String _formId;
        protected int _maxLen;
        protected int _maxLine;
        protected int _pageMaxLine;

        abstract void printOut(final Vrw32alp svf, final Student student);

        /** 改ページブロックなら改ページ */
        final void pageChange(final Vrw32alp svf) {
            if (_pageChange) {
                svf.VrSetForm(_formId, 4);
                PRINT_FRM_NOW = _formId;
                LINE_CNT = 0;
                IS_FIRST_PRINT = true;
            } else if (PRINT_FRM2.equals(_formId) && !PRINT_FRM_NOW.equals(PRINT_FRM1) && !PRINT_FRM_NOW.equals(_formId)) {
                svf.VrSetForm(_formId, 4);
                PRINT_FRM_NOW = _formId;
                LINE_CNT = 0;
                IS_FIRST_PRINT = true;
            }
        }

        /** チェックして改ページ */
        final void checkLineAndPageChange(final Vrw32alp svf, final int cnt) {
            final int checkMaxLine = IS_FIRST_PRINT ? PAGE_MAXLINE : FIRST_PAGE_MAXLINE;
            if (LINE_CNT + cnt > checkMaxLine) {
                svf.VrSetForm(_formId, 4);
                PRINT_FRM_NOW = _formId;
                LINE_CNT = 0;
                IS_FIRST_PRINT = true;
            }
        }

        /** チェックして改ページ */
        final void checkLineAndPageChangeBlock8(final Vrw32alp svf, final int cnt, final String formId, final int maxLine) {
            if (LINE_CNT + cnt > _pageMaxLine) {
                _formId = formId;
                _pageMaxLine = maxLine;
                svf.VrSetForm(_formId, 4);
                PRINT_FRM_NOW = _formId;
                LINE_CNT = 0;
            }
        }
    }

    private class Block8 extends Block {
        final String _patern;
        final String _useSemes;
        final String _guidancePattern;
        final String _gakubuSchoolKind;
        final String _ghrCd;
        final String _grade;
        final String _hrClass;
        final String _condition;
        final String _groupCd;
        String _itemRemark1;
        String _itemRemark2;
        String _itemRemark3;
        String _itemRemark4;
        String _itemRemark5;
        Map _subclassMap;

        public Block8(final String patern, final String useSemes, final String guidancePattern,
                final String gakubuSchoolKind, final String ghrCd, final String grade, final String hrClass,
                final String condition, final String groupCd, final String schoolKind) {
            _patern = patern;
            _useSemes = useSemes;
            _guidancePattern = guidancePattern;
            _gakubuSchoolKind = gakubuSchoolKind;
            _ghrCd = ghrCd;
            _grade = grade;
            _hrClass = hrClass;
            _condition = condition;
            _groupCd = groupCd;
            _subclassMap = new LinkedMap();
            _pageChange = true;
            _kindNo = "08";
            final Map conditionMap = (Map) _param._hreportConditionMap.get(schoolKind);
            final HreportCondition hreportCondition9;
            if (null != conditionMap) {
                hreportCondition9 = (HreportCondition) conditionMap.get("109");
            } else {
                hreportCondition9 = new HreportCondition("", "", "", "", "", "", "", "", "", "");
            }
            if ("A".equals(patern)) {
                _formId = "KNJD429_2_1.frm";
                _pageMaxLine = 41;
            } else if ("B".equals(patern)) {
                _formId = "1".equals(hreportCondition9._remark1) ? "KNJD429_2_2_2.frm" : "KNJD429_2_2.frm";
                _pageMaxLine = "1".equals(hreportCondition9._remark1) ? 51 : 47;
            } else if ("C".equals(patern)) {
                _formId = "1".equals(hreportCondition9._remark1) ? "KNJD429_2_3_2.frm" : "KNJD429_2_3.frm";
                _pageMaxLine = "1".equals(hreportCondition9._remark1) ? 50 : 43;
            } else {
                _formId = "KNJD429_2_4.frm";
                _pageMaxLine = 50;
            }
        }

        public void setTitle(final DB2UDB db2) {
            final String titleSql = getBlock8PaternSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(titleSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _itemRemark1 = rs.getString("ITEM_REMARK1");
                    _itemRemark2 = rs.getString("ITEM_REMARK2");
                    _itemRemark3 = rs.getString("ITEM_REMARK3");
                    _itemRemark4 = rs.getString("ITEM_REMARK4");
                    _itemRemark5 = rs.getString("ITEM_REMARK5");
                }
            } catch (SQLException ex) {
                log.error("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getBlock8PaternSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     TOKU_ITEM.* ");
            stb.append(" FROM ");
            stb.append("     HREPORT_GUIDANCE_ITEM_NAME_DAT TOKU_ITEM ");
            stb.append(" WHERE ");
            stb.append("     TOKU_ITEM.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND TOKU_ITEM.SEMESTER = '9' ");
            stb.append("     AND TOKU_ITEM.GUIDANCE_PATTERN = '" + _guidancePattern + "' ");
            stb.append("     AND TOKU_ITEM.GAKUBU_SCHOOL_KIND = '" + _gakubuSchoolKind + "' ");
            stb.append("     AND TOKU_ITEM.CONDITION = '" + _condition + "' ");

            return stb.toString();
        }

        public void setSubclassMap(final DB2UDB db2, final String schregNo) {
            final String subclassSql = getBlock8SubclassSql(schregNo);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(subclassSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String classCd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String subclassName = rs.getString("SUBCLASSNAME");
                    final String unitCd = rs.getString("UNITCD");
                    final String unitName = rs.getString("UNITNAME");
                    final String guidancePattern = rs.getString("GUIDANCE_PATTERN");
                    final String seq = rs.getString("SEQ");
                    final String remark = rs.getString("REMARK");
                    final Subclass subclass;
                    final String key = classCd + schoolKind + curriculumCd + subclassCd;
                    if (_subclassMap.containsKey(key)) {
                        subclass = (Subclass) _subclassMap.get(key);
                    } else {
                        subclass = new Subclass(classCd, schoolKind, curriculumCd, subclassCd, subclassName, unitCd);
                    }
                    subclass.setSemeUnitMap(semester, unitCd, unitName, guidancePattern, seq, remark);
                    _subclassMap.put(key, subclass);
                }
            } catch (SQLException ex) {
                log.error("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getBlock8SubclassSql(final String schregNo) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TOKU_SUB.CLASSCD, ");
            stb.append("     TOKU_SUB.SCHOOL_KIND, ");
            stb.append("     TOKU_SUB.CURRICULUM_CD, ");
            stb.append("     TOKU_SUB.SUBCLASSCD, ");
            stb.append("     TOKU_SUB.SEMESTER, ");
            stb.append("     TOKU_SUB.UNITCD, ");
            stb.append("     UNIT_M.UNITNAME, ");
            stb.append("     TOKU_SUB.GUIDANCE_PATTERN, ");
            stb.append("     TOKU_SUB.SEQ, ");
            stb.append("     TOKU_SUB.REMARK, ");
            stb.append("     SUBM.SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("     HREPORT_TOKUSHI_SCHREG_SUBCLASS_DAT TOKU_SUB ");
            stb.append("     LEFT JOIN SUBCLASS_MST SUBM ON TOKU_SUB.CLASSCD = SUBM.CLASSCD ");
            stb.append("          AND TOKU_SUB.SCHOOL_KIND = SUBM.SCHOOL_KIND ");
            stb.append("          AND TOKU_SUB.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
            stb.append("          AND TOKU_SUB.SUBCLASSCD = SUBM.SUBCLASSCD ");
            stb.append("     LEFT JOIN GRADE_KIND_UNIT_GROUP_YMST UNIT_M ON TOKU_SUB.YEAR = UNIT_M.YEAR ");
            stb.append("          AND UNIT_M.SEMESTER = '9' ");
            stb.append("          AND UNIT_M.GAKUBU_SCHOOL_KIND = '" + _gakubuSchoolKind + "' ");
            stb.append("          AND UNIT_M.GHR_CD = '" + _ghrCd + "' ");
            stb.append("          AND UNIT_M.GRADE = '" + _grade + "' ");
            stb.append("          AND UNIT_M.HR_CLASS = '" + _hrClass + "' ");
            stb.append("          AND UNIT_M.CONDITION = '" + _condition + "' ");
            stb.append("          AND UNIT_M.GROUPCD = '" + _groupCd + "' ");
            stb.append("          AND TOKU_SUB.UNITCD = UNIT_M.UNITCD ");
            stb.append("          AND TOKU_SUB.CLASSCD = UNIT_M.CLASSCD ");
            stb.append("          AND TOKU_SUB.SCHOOL_KIND = UNIT_M.SCHOOL_KIND ");
            stb.append("          AND TOKU_SUB.CURRICULUM_CD = UNIT_M.CURRICULUM_CD ");
            stb.append("          AND TOKU_SUB.SUBCLASSCD = UNIT_M.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     TOKU_SUB.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND TOKU_SUB.SCHREGNO = '" + schregNo + "' ");
            stb.append("     AND TOKU_SUB.GUIDANCE_PATTERN = '" + _guidancePattern + "' ");
            stb.append(" ORDER BY ");
            stb.append("     TOKU_SUB.CLASSCD, ");
            stb.append("     TOKU_SUB.SCHOOL_KIND, ");
            stb.append("     TOKU_SUB.CURRICULUM_CD, ");
            stb.append("     TOKU_SUB.SUBCLASSCD, ");
            stb.append("     TOKU_SUB.SEMESTER, ");
            stb.append("     TOKU_SUB.UNITCD, ");
            stb.append("     TOKU_SUB.SEQ ");
            return stb.toString();
        }

        void printOut(final Vrw32alp svf, final Student student) {
            pageChange(svf);
            if ("A".equals(_patern)) {
                printOutA(svf, student);
            } else if ("B".equals(_patern)) {
                printOutB(svf, student);
            } else if ("C".equals(_patern)) {
                printOutC(svf, student);
            } else {
                printOutD(svf, student);
            }
        }

        public void printOutA(final Vrw32alp svf, final Student student) {
            pageChange(svf);
            setHeadA(svf, student);

            int grp1 = 1;
            int grp2 = 1;
            int grp3 = 1;
            final Map printSubclass = new HashMap();
            //科目
            for (Iterator itRemark = _subclassMap.keySet().iterator(); itRemark.hasNext();) {
                final String subclassCd = (String) itRemark.next();
                final Subclass subclass = (Subclass) _subclassMap.get(subclassCd);
                final int subclassLen = 2;
                final List setSubclassList = KNJ_EditKinsoku.getTokenList(subclass._subclassName, subclassLen);

                //学期
                for (Iterator itSemeUnit = subclass._semeUnitMap.keySet().iterator(); itSemeUnit.hasNext();) {
                    final String semester = (String) itSemeUnit.next();
                    final Map unitMap = (Map) subclass._semeUnitMap.get(semester);

                    final Map printUnit = new HashMap();
                    if (null != unitMap) {
                        //単元
                        for (Iterator itUnit = unitMap.keySet().iterator(); itUnit.hasNext();) {
                            final String unitCd = (String) itUnit.next();
                            final UnitData unitData = (UnitData) unitMap.get(unitCd);

                            //単元名
                            final List setUnitList = KNJ_EditKinsoku.getTokenList(unitData._unitName, 6);
                            //登録データ1
                            final String remark1 = (String) unitData._unitSeqMap.get("1");
                            final List setRemarkList1 = KNJ_EditKinsoku.getTokenList(remark1, 70);

                            int maxLine = setRemarkList1.size();
                            if (!printSubclass.containsKey(subclassCd) && setSubclassList.size() > maxLine) {
                                maxLine = setSubclassList.size();
                            }
                            if (setUnitList.size() > maxLine) {
                                maxLine = setUnitList.size();
                            }

                            checkLineAndPageChangeBlock8(svf, maxLine, _formId, _pageMaxLine);
                            setHeadA(svf, student);
                            for (int i = 0; i < maxLine; i++) {
                                final String fieldName;
                                if (subclass._isUnit) {
                                    fieldName = "1";
                                    svf.VrsOut("GRP" + fieldName + "_2", String.valueOf(grp2));
                                    grp3 = grp2;
                                    if (!printUnit.containsKey(unitCd) && i < setUnitList.size()) {
                                        svf.VrsOut("UNIT" + fieldName, (String) setUnitList.get(i));
                                    }
                                } else {
                                    fieldName = "2";
                                    grp3 = grp1;
                                }
                                if (!printSubclass.containsKey(subclassCd) && i < setSubclassList.size()) {
                                    svf.VrsOut("CLASS_NAME" + fieldName, (String) setSubclassList.get(i));
                                }
                                svf.VrsOut("GRP" + fieldName + "_1", String.valueOf(grp1));
                                svf.VrsOut("GRP" + fieldName + "_3", String.valueOf(grp3));
                                if (i < setRemarkList1.size()) {
                                    svf.VrsOut("CONTENT" + fieldName, (String) setRemarkList1.get(i));
                                }
                                svf.VrEndRecord();
                                LINE_CNT++;
                            }
                            printUnit.put(unitCd, unitCd);
                            printSubclass.put(subclassCd, subclassCd);
                            grp2++;
                        }
                    }
                    grp3++;
                }
                grp1++;
            }
        }

        private void setHeadA(final Vrw32alp svf, final Student student) {
            final Semester semesterObj = (Semester) _param._semesterMap.get(_param._semester);

            svf.VrsOut("TITLE", semesterObj._semestername + "の記録");
            svf.VrsOut("HEADER_NAME1", _itemRemark1);
            svf.VrsOut("NAME", "氏名 (" + student._name + ")");
        }

        public void printOutB(final Vrw32alp svf, final Student student) {
            pageChange(svf);
            setHeadB(svf, student);

            //年間目標
            printHrepRemarkDetail(svf, student, student._hrepRemarkDetailR1, "YEAR_HOPE", 88);
            int grp1 = 1;
            int grp2 = 1;
            int grp3 = 1;
            final Map printSubclass = new HashMap();
            //科目
            for (Iterator itRemark = _subclassMap.keySet().iterator(); itRemark.hasNext();) {
                final String subclassCd = (String) itRemark.next();
                final Subclass subclass = (Subclass) _subclassMap.get(subclassCd);
                final int subclassLen = subclass._isUnit ? 6 : 12;
                final List setSubclassList = KNJ_EditKinsoku.getTokenList(subclass._subclassName, subclassLen);

                //学期
                final String semester = _param._semester;
                final Map unitMap = (Map) subclass._semeUnitMap.get(semester);

                if (null != unitMap) {
                    //単元
                    for (Iterator itUnit = unitMap.keySet().iterator(); itUnit.hasNext();) {
                        final String unitCd = (String) itUnit.next();
                        final UnitData unitData = (UnitData) unitMap.get(unitCd);

                        //単元名
                        final List setUnitList = KNJ_EditKinsoku.getTokenList(unitData._unitName, 6);
                        //登録データ1
                        final String remark1 = (String) unitData._unitSeqMap.get("1");
                        final List setRemarkList1 = KNJ_EditKinsoku.getTokenList(remark1, 20);
                        //登録データ2
                        final String remark2 = (String) unitData._unitSeqMap.get("2");
                        final List setRemarkList2 = KNJ_EditKinsoku.getTokenList(remark2, 24);
                        //登録データ3
                        final String remark3 = (String) unitData._unitSeqMap.get("3");
                        final List setRemarkList3 = KNJ_EditKinsoku.getTokenList(remark3, 36);

                        int maxLine = setRemarkList1.size();
                        if (setRemarkList2.size() > maxLine) {
                            maxLine = setRemarkList2.size();
                        }
                        if (setRemarkList3.size() > maxLine) {
                            maxLine = setRemarkList3.size();
                        }
                        if (setSubclassList.size() > maxLine) {
                            maxLine = setSubclassList.size();
                        }
                        if (setUnitList.size() > maxLine) {
                            maxLine = setUnitList.size();
                        }

                        checkLineAndPageChangeBlock8(svf, maxLine, "KNJD429_2_2_2.frm", 51);
                        setHeadB(svf, student);
                        for (int i = 0; i < maxLine; i++) {
                            final String fieldName;
                            if (subclass._isUnit) {
                                fieldName = "1";
                                svf.VrsOut("GRP" + fieldName + "_2", String.valueOf(grp2));
                                grp3 = grp2;
                                if (i < setUnitList.size()) {
                                    svf.VrsOut("UNIT" + fieldName, (String) setUnitList.get(i));
                                }
                            } else {
                                fieldName = "2";
                                grp3 = grp1;
                            }
                            if (!printSubclass.containsKey(subclassCd) && i < setSubclassList.size()) {
                                svf.VrsOut("CLASS_NAME" + fieldName, (String) setSubclassList.get(i));
                            }
                            svf.VrsOut("GRP" + fieldName + "_1", String.valueOf(grp1));
                            svf.VrsOut("GRP" + fieldName + "_3", String.valueOf(grp3));
                            svf.VrsOut("GRP" + fieldName + "_4", String.valueOf(grp3));
                            svf.VrsOut("GRP" + fieldName + "_5", String.valueOf(grp3));
                            svf.VrsOut("GRP" + fieldName + "_6", String.valueOf(grp3));
                            if (i < setRemarkList1.size()) {
                                svf.VrsOut("HOPE" + fieldName, (String) setRemarkList1.get(i));
                            }
                            if (i < setRemarkList2.size()) {
                                svf.VrsOut("METHOD" + fieldName, (String) setRemarkList2.get(i));
                            }
                            if (i < setRemarkList3.size()) {
                                svf.VrsOut("VAL" + fieldName, (String) setRemarkList3.get(i));
                            }
                            svf.VrEndRecord();
                            LINE_CNT++;
                        }
                        printSubclass.put(subclassCd, subclassCd);
                        grp2++;
                    }
                }
                grp1++;
            }
        }

        private void setHeadB(final Vrw32alp svf, final Student student) {
            final Semester semesterObj = (Semester) _param._semesterMap.get(_param._semester);

            svf.VrsOut("TITLE", semesterObj._semestername + "の記録");

            svf.VrsOut("HEADER_NAME1", _itemRemark1);
            svf.VrsOut("HEADER_NAME2", _itemRemark2);
            svf.VrsOut("HEADER_NAME3", _itemRemark3);
            svf.VrsOut("NAME", "氏名 (" + student._name + ")");
        }

        private void printHrepRemarkDetail(final Vrw32alp svf, final Student student, final String setText, final String fieldName, final int keta) {
            final List setHopeList = KNJ_EditKinsoku.getTokenList(setText, keta);
            int hopeLine = 1;
            for (Iterator itHope = setHopeList.iterator(); itHope.hasNext();) {
                final String setRemark = (String) itHope.next();
                svf.VrsOutn(fieldName, hopeLine, setRemark);
                hopeLine++;
            }
        }

        public void printOutC(final Vrw32alp svf, final Student student) {
            pageChange(svf);
            final Semester semesterObj = (Semester) _param._semesterMap.get(_param._semester);
            setHeadC(svf, student, semesterObj);

            int grp1 = 1;
            int grp2 = 1;
            int grp3 = 1;
            final Map printSubclass = new HashMap();
            //科目
            for (Iterator itRemark = _subclassMap.keySet().iterator(); itRemark.hasNext();) {
                final String subclassCd = (String) itRemark.next();
                final Subclass subclass = (Subclass) _subclassMap.get(subclassCd);
                final int subclassLen = 6;
                final List setSubclassList = KNJ_EditKinsoku.getTokenList(subclass._subclassName, subclassLen);

                //学期
                for (Iterator itSemeUnit = subclass._semeUnitMap.keySet().iterator(); itSemeUnit.hasNext();) {
                    final String semester = (String) itSemeUnit.next();
                    final Map unitMap = (Map) subclass._semeUnitMap.get(semester);

                    final Map printUnit = new HashMap();
                    if (null != unitMap) {

                        //単元
                        for (Iterator itUnit = unitMap.keySet().iterator(); itUnit.hasNext();) {
                            final String unitCd = (String) itUnit.next();
                            final UnitData unitData = (UnitData) unitMap.get(unitCd);

                            //単元名
                            final List setUnitList = KNJ_EditKinsoku.getTokenList(unitData._unitName, 6);
                            //登録データ1
                            final String remark1 = (String) unitData._unitSeqMap.get("1");
                            final List setRemarkList1 = KNJ_EditKinsoku.getTokenList(remark1, 18);
                            //登録データ2
                            final String remark2 = (String) unitData._unitSeqMap.get("2");
                            final List setRemarkList2 = KNJ_EditKinsoku.getTokenList(remark2, 8);
                            //登録データ3
                            final String remark3 = (String) unitData._unitSeqMap.get("3");
                            final List setRemarkList3 = KNJ_EditKinsoku.getTokenList(remark3, 22);
                            //登録データ4
                            final String remark4 = (String) unitData._unitSeqMap.get("4");
                            final List setRemarkList4 = KNJ_EditKinsoku.getTokenList(remark4, 32);

                            int maxLine = setRemarkList1.size();
                            if (setRemarkList2.size() > maxLine) {
                                maxLine = setRemarkList2.size();
                            }
                            if (setRemarkList3.size() > maxLine) {
                                maxLine = setRemarkList3.size();
                            }
                            if (setRemarkList4.size() > maxLine) {
                                maxLine = setRemarkList4.size();
                            }
                            if (setSubclassList.size() > maxLine) {
                                maxLine = setSubclassList.size();
                            }
                            if (setUnitList.size() > maxLine) {
                                maxLine = setUnitList.size();
                            }

                            checkLineAndPageChangeBlock8(svf, maxLine, "KNJD429_2_3_2.frm", 50);
                            setHeadC(svf, student, semesterObj);
                            for (int i = 0; i < maxLine; i++) {
                                final String fieldName;
                                if (subclass._isUnit) {
                                    fieldName = "1";
                                    svf.VrsOut("GRP" + fieldName + "_2", String.valueOf(grp2));
                                    grp3 = grp2;
                                    if (!printUnit.containsKey(unitCd) && i < setUnitList.size()) {
                                        svf.VrsOut("UNIT" + fieldName, (String) setUnitList.get(i));
                                    }
                                } else {
                                    fieldName = "2";
                                    grp3 = grp1;
                                }
                                if (!printSubclass.containsKey(subclassCd) && i < setSubclassList.size()) {
                                    svf.VrsOut("CLASS_NAME" + fieldName, (String) setSubclassList.get(i));
                                }
                                svf.VrsOut("GRP" + fieldName + "_1", String.valueOf(grp1));
                                svf.VrsOut("GRP" + fieldName + "_3", String.valueOf(grp3));
                                svf.VrsOut("GRP" + fieldName + "_4", String.valueOf(grp3));
                                svf.VrsOut("GRP" + fieldName + "_5", String.valueOf(grp3));
                                svf.VrsOut("GRP" + fieldName + "_6", String.valueOf(grp3));
                                if (i < setRemarkList1.size()) {
                                    svf.VrsOut("HOPE" + fieldName, (String) setRemarkList1.get(i));
                                }
                                if (i < setRemarkList2.size()) {
                                    svf.VrsOut("CAREER" + fieldName, (String) setRemarkList2.get(i));
                                }
                                if (i < setRemarkList3.size()) {
                                    svf.VrsOut("METHOD" + fieldName, (String) setRemarkList3.get(i));
                                }
                                if (i < setRemarkList4.size()) {
                                    svf.VrsOut("VAL" + fieldName, (String) setRemarkList4.get(i));
                                }
                                svf.VrEndRecord();
                                LINE_CNT++;
                            }
                            printUnit.put(unitCd, unitCd);
                            printSubclass.put(subclassCd, subclassCd);
                            grp2++;
                        }
                    }
                    grp3++;
                }
                grp1++;
            }
        }

        private void setHeadC(final Vrw32alp svf, final Student student, final Semester semesterObj) {
            svf.VrsOut("TITLE", semesterObj._semestername + "の記録");
            svf.VrsOut("HEADER_NAME1", _itemRemark1);
            final List setHead2List = KNJ_EditKinsoku.getTokenList(_itemRemark2, 8);
            int head2Cnt = 1;
            for (Iterator itHead2 = setHead2List.iterator(); itHead2.hasNext();) {
                final String setRemark = (String) itHead2.next();
                svf.VrsOut("HEADER_NAME2_" + head2Cnt, setRemark);
                head2Cnt++;
            }
            svf.VrsOut("HEADER_NAME3", _itemRemark3);
            svf.VrsOut("HEADER_NAME4", _itemRemark4);
            svf.VrsOut("NAME", "氏名 (" + student._name + ")");

            //年間目標
            printHrepRemarkDetail(svf, student, student._hrepRemarkDetailR1, "YEAR_HOPE1", 32);
            printHrepRemarkDetail(svf, student, student._hrepRemarkDetailR2, "YEAR_HOPE2", 58);
        }

        public void printOutD(final Vrw32alp svf, final Student student) {
            pageChange(svf);
            setHeadD(svf, student);

            int grp1 = 1;
            int grp2 = 1;
            int grp3 = 1;
            final Map printSubclass = new HashMap();
            //科目
            for (Iterator itRemark = _subclassMap.keySet().iterator(); itRemark.hasNext();) {
                final String subclassCd = (String) itRemark.next();
                final Subclass subclass = (Subclass) _subclassMap.get(subclassCd);
                final int subclassLen = subclass._isUnit? 6 : 12;
                final List setSubclassList = KNJ_EditKinsoku.getTokenList(subclass._subclassName, subclassLen);

                //学期
                for (Iterator itSemeUnit = subclass._semeUnitMap.keySet().iterator(); itSemeUnit.hasNext();) {
                    final String semester = (String) itSemeUnit.next();
                    final Map unitMap = (Map) subclass._semeUnitMap.get(semester);

                    final Map printUnit = new HashMap();
                    if (null != unitMap) {
                        //単元
                        for (Iterator itUnit = unitMap.keySet().iterator(); itUnit.hasNext();) {
                            final String unitCd = (String) itUnit.next();
                            final UnitData unitData = (UnitData) unitMap.get(unitCd);

                            //単元名
                            final List setUnitList = KNJ_EditKinsoku.getTokenList(unitData._unitName, 6);
                            //登録データ1
                            final String remark1 = (String) unitData._unitSeqMap.get("1");
                            final List setRemarkList1 = KNJ_EditKinsoku.getTokenList(remark1, 16);
                            //登録データ2
                            final String remark2 = (String) unitData._unitSeqMap.get("2");
                            final List setRemarkList2 = KNJ_EditKinsoku.getTokenList(remark2, 16);
                            //登録データ3
                            final String remark3 = (String) unitData._unitSeqMap.get("3");
                            final List setRemarkList3 = KNJ_EditKinsoku.getTokenList(remark3, 6);
                            //登録データ4
                            final String remark4 = (String) unitData._unitSeqMap.get("4");
                            final List setRemarkList4 = KNJ_EditKinsoku.getTokenList(remark4, 20);
                            //登録データ5
                            final String remark5 = (String) unitData._unitSeqMap.get("5");
                            final List setRemarkList5 = KNJ_EditKinsoku.getTokenList(remark5, 32);

                            int maxLine = setRemarkList1.size();
                            if (setRemarkList2.size() > maxLine) {
                                maxLine = setRemarkList2.size();
                            }
                            if (setRemarkList3.size() > maxLine) {
                                maxLine = setRemarkList3.size();
                            }
                            if (setRemarkList4.size() > maxLine) {
                                maxLine = setRemarkList4.size();
                            }
                            if (setRemarkList5.size() > maxLine) {
                                maxLine = setRemarkList5.size();
                            }
                            if (setSubclassList.size() > maxLine) {
                                maxLine = setSubclassList.size();
                            }
                            if (setUnitList.size() > maxLine) {
                                maxLine = setUnitList.size();
                            }

                            checkLineAndPageChangeBlock8(svf, maxLine, _formId, _pageMaxLine);
                            setHeadD(svf, student);
                            for (int i = 0; i < maxLine; i++) {
                                final String fieldName;
                                if (subclass._isUnit) {
                                    fieldName = "1";
                                    svf.VrsOut("GRP" + fieldName + "_2", String.valueOf(grp2));
                                    grp3 = grp2;
                                    if (!printUnit.containsKey(unitCd) && i < setUnitList.size()) {
                                        svf.VrsOut("UNIT" + fieldName, (String) setUnitList.get(i));
                                    }
                                } else {
                                    fieldName = "2";
                                    grp3 = grp1;
                                }
                                if (!printSubclass.containsKey(subclassCd) && i < setSubclassList.size()) {
                                    svf.VrsOut("CLASS_NAME" + fieldName, (String) setSubclassList.get(i));
                                }
                                svf.VrsOut("GRP" + fieldName + "_1", String.valueOf(grp1));
                                svf.VrsOut("GRP" + fieldName + "_3", String.valueOf(grp3));
                                svf.VrsOut("GRP" + fieldName + "_4", String.valueOf(grp3));
                                svf.VrsOut("GRP" + fieldName + "_5", String.valueOf(grp3));
                                svf.VrsOut("GRP" + fieldName + "_6", String.valueOf(grp3));
                                svf.VrsOut("GRP" + fieldName + "_7", String.valueOf(grp3));
                                if (i < setRemarkList1.size()) {
                                    svf.VrsOut("HOPE" + fieldName, (String) setRemarkList1.get(i));
                                }
                                if (i < setRemarkList2.size()) {
                                    svf.VrsOut("LEAD_CONTENT" + fieldName, (String) setRemarkList2.get(i));
                                }
                                if (i < setRemarkList3.size()) {
                                    svf.VrsOut("LEAD_FORM" + fieldName, (String) setRemarkList3.get(i));
                                }
                                if (i < setRemarkList4.size()) {
                                    svf.VrsOut("METHOD" + fieldName, (String) setRemarkList4.get(i));
                                }
                                if (i < setRemarkList5.size()) {
                                    svf.VrsOut("VAL" + fieldName, (String) setRemarkList5.get(i));
                                }
                                svf.VrEndRecord();
                                LINE_CNT++;
                            }
                            printUnit.put(unitCd, unitCd);
                            printSubclass.put(subclassCd, subclassCd);
                            grp2++;
                        }
                    }
                    grp3++;
                }
                grp1++;
            }
        }

        private void setHeadD(final Vrw32alp svf, final Student student) {
            final Semester semesterObj = (Semester) _param._semesterMap.get(_param._semester);
            svf.VrsOut("SEMESTER", semesterObj._semestername);

            svf.VrsOut("TITLE", semesterObj._semestername + "の記録");
            svf.VrsOut("HEADER_NAME1", _itemRemark1);
            svf.VrsOut("HEADER_NAME2", _itemRemark2);
            final List setHead3List = KNJ_EditKinsoku.getTokenList(_itemRemark3, 4);
            int head3Cnt = 1;
            for (Iterator itHead3 = setHead3List.iterator(); itHead3.hasNext();) {
                final String setRemark = (String) itHead3.next();
                svf.VrsOut("HEADER_NAME3_" + head3Cnt, setRemark);
                head3Cnt++;
            }
            svf.VrsOut("HEADER_NAME4", _itemRemark4);
            svf.VrsOut("HEADER_NAME5", _itemRemark5);
            svf.VrsOut("NAME", "氏名 (" + student._name + ")");
        }
    }

    private class Subclass {
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclassCd;
        final String _subclassName;
        final boolean _isUnit;
        final Map _semeUnitMap;

        public Subclass(final String classCd, final String schoolKind, final String curriculumCd,
                final String subclassCd, final String subclassName, final String unitCd) {
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _isUnit = !"00".equals(unitCd);
            _semeUnitMap = new LinkedMap();
        }

        public void setSemeUnitMap(final String semester, final String unitCd, final String unitName,
                final String guidancePattern, final String seq, final String remark
        ) {
            UnitData unitData;
            final Map unitDataMap;
            if (_semeUnitMap.containsKey(semester)) {
                unitDataMap = (Map) _semeUnitMap.get(semester);
            } else {
                unitDataMap = new LinkedMap();
                _semeUnitMap.put(semester, unitDataMap);
            }
            if (unitDataMap.containsKey(unitCd)) {
                unitData = (UnitData) unitDataMap.get(unitCd);
            } else {
                unitData = new UnitData(unitCd, unitName, guidancePattern);
                unitDataMap.put(unitCd, unitData);
            }
            unitData._unitSeqMap.put(seq, remark);
        }

    }

    private class UnitData {
        final String _unitCd;
        final String _unitName;
        final String _guidancePattern;
        final Map _unitSeqMap;

        public UnitData(final String unitCd, final String unitName, final String guidancePattern) {
            _unitCd = unitCd;
            _unitName = unitName;
            _guidancePattern = guidancePattern;
            _unitSeqMap = new HashMap();
        }
    }

    private static class Attendance {
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _absent;
        final int _present;
        final int _late;
        final int _early;
        final int _abroad;
        final String _remark;
        final String _communication;
        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int absent,
                final int present,
                final int late,
                final int early,
                final int abroad,
                final String remark,
                final String communication
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
            _abroad = abroad;
            _remark = remark;
            _communication = communication;
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList,
                final DateRange dateRange
        ) {
            log.info(" attendance = " + dateRange);
            final String movedate = param._moveDate.replace('/', '-');
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(movedate) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(movedate) > 0 ? movedate : dateRange._edate;
            PreparedStatement psAtSeme = null;
            ResultSet rsAtSeme = null;
            PreparedStatement psHreportRemark = null;
            ResultSet rsHreportRemark = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._ctrlYear,
                        param._semester,
                        dateRange._sdate,
                        edate,
                        param._attendParamMap
                );
                log.debug(" attend sql = " + sql);
                psAtSeme = db2.prepareStatement(sql);

                final String hreportRemarkSql = getHreportRemarkSql(param, dateRange);
                psHreportRemark = db2.prepareStatement(hreportRemarkSql);
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    psHreportRemark.setString(1, student._schregNo);
                    rsHreportRemark = psHreportRemark.executeQuery();

                    String setRemark = "";
                    String setCommunication = "";
                    while (rsHreportRemark.next()) {
                        setRemark = rsHreportRemark.getString("ATTENDREC_REMARK");
                        setCommunication = rsHreportRemark.getString("COMMUNICATION");
                    }
                    DbUtils.closeQuietly(rsAtSeme);

                    psAtSeme.setString(1, student._schregNo);
                    rsAtSeme = psAtSeme.executeQuery();

                    while (rsAtSeme.next()) {
                        if (!SEMEALL.equals(rsAtSeme.getString("SEMESTER"))) {
                            continue;
                        }

                        final Attendance attendance = new Attendance(
                                rsAtSeme.getInt("LESSON"),
                                rsAtSeme.getInt("MLESSON"),
                                rsAtSeme.getInt("SUSPEND"),
                                rsAtSeme.getInt("MOURNING"),
                                rsAtSeme.getInt("SICK"),
                                rsAtSeme.getInt("PRESENT"),
                                rsAtSeme.getInt("LATE"),
                                rsAtSeme.getInt("EARLY"),
                                rsAtSeme.getInt("TRANSFER_DATE"),
                                setRemark,
                                setCommunication
                        );
                        student._attendMap.put(dateRange._key, attendance);
                    }
                    DbUtils.closeQuietly(rsAtSeme);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(psAtSeme);
                db2.commit();
            }
        }

        private static String getHreportRemarkSql(final Param param, final DateRange dateRange) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TOTALSTUDYTIME, ");
            stb.append("     SPECIALACTREMARK, ");
            stb.append("     COMMUNICATION, ");
            stb.append("     REMARK1, ");
            stb.append("     REMARK2, ");
            stb.append("     REMARK3, ");
            stb.append("     FOREIGNLANGACT, ");
            stb.append("     ATTENDREC_REMARK ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND SEMESTER = '" + dateRange._key + "' ");
            stb.append("     AND SCHREGNO = ? ");

            return stb.toString();
        }

    }

    private static class Semester {
        final String _semester;
        final String _semestername;
        final DateRange _dateRange;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _dateRange = new DateRange(_semester, _semestername, sdate, edate);
        }
    }

    private static class DateRange {
        final String _key;
        final String _name;
        final String _sdate;
        final String _edate;
        public DateRange(final String key, final String name, final String sdate, final String edate) {
            _key = key;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        public String toString() {
            return "DateRange(" + _key + ", " + _sdate + ", " + _edate + ")";
        }
    }

    private class HreportCondition {
        final String _remark1;
        final String _remark2;
        final String _remark3;
        final String _remark4;
        final String _remark5;
        final String _remark6;
        final String _remark7;
        final String _remark8;
        final String _remark9;
        final String _remark10;

        public HreportCondition(
                final String remark1,
                final String remark2,
                final String remark3,
                final String remark4,
                final String remark5,
                final String remark6,
                final String remark7,
                final String remark8,
                final String remark9,
                final String remark10
        ) {
            _remark1  = StringUtils.defaultString(remark1);
            _remark2  = StringUtils.defaultString(remark2);
            _remark3  = StringUtils.defaultString(remark3);
            _remark4  = StringUtils.defaultString(remark4);
            _remark5  = StringUtils.defaultString(remark5);
            _remark6  = StringUtils.defaultString(remark6);
            _remark7  = StringUtils.defaultString(remark7);
            _remark8  = StringUtils.defaultString(remark8);
            _remark9  = StringUtils.defaultString(remark9);
            _remark10 = StringUtils.defaultString(remark10);
        }
    }

    private class CertifSchool {
        final String _schoolName;
        final String _jobName;
        final String _principalName;

        public CertifSchool(
                final String schoolName,
                final String jobName,
                final String principalName
        ) {
            _schoolName  = StringUtils.defaultString(schoolName);
            _jobName  = StringUtils.defaultString(jobName);
            _principalName = StringUtils.defaultString(principalName);
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72620 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _semester;
        final String _hukusikiRadio;
        final String _schoolKind;
        final String _ghrCd;
        final String[] _categorySelected;
        final String _moveDate;
        final boolean _isPrintHyosi;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _prgid;
        final String _documentroot;
        final String _imagepath;
        final String _schoolLogoImagePath;
        final String _whiteSpaceImagePath;
        final String _schoolCd;
        final String _selectGhr;
        final String _printLogStaffcd;
        final String _printLogRemoteIdent;
        final String _recordDate;
        final String _printSize;
        final Map _semesterMap;
        String _lastSemester;
        final Map _hreportConditionMap;
        final Map _certifSchoolMap;

        /** 行動の記録タイトル */
        final Map _d035;

        /** 端数計算共通メソッド引数 */
        private final Map _attendParamMap;
        Map _attendRanges;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _hukusikiRadio = request.getParameter("HUKUSIKI_RADIO");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _ghrCd = request.getParameter("GHR_CD");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _moveDate = request.getParameter("MOVE_DATE");
            _isPrintHyosi = "1".equals(request.getParameter("HYOSI"));
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _prgid = request.getParameter("PRGID");
            _schoolCd = request.getParameter("SCHOOLCD");
            _selectGhr = request.getParameter("SELECT_GHR");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _printLogRemoteIdent = request.getParameter("PRINT_LOG_REMOTE_IDENT");
            _printSize = request.getParameter("PRINTSIZE");
            _recordDate = "9999-03-31";
            _semesterMap = loadSemester(db2);
            _hreportConditionMap = getHreportConditionMap(db2);
            _certifSchoolMap = getCertifSchoolMap(db2);
            _d035 = getVNameMst(db2, "D035");

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");

            _attendRanges = new HashMap();
            for (final Iterator it = _semesterMap.keySet().iterator(); it.hasNext();) {
                final String semester = (String) it.next();
                final Semester oSemester = (Semester) _semesterMap.get(semester);
                _attendRanges.put(semester, oSemester._dateRange);
            }

            _documentroot = request.getParameter("DOCUMENTROOT");
            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));
            _schoolLogoImagePath = getImageFilePath("SCHOOLLOGO.jpg");
            _whiteSpaceImagePath = getImageFilePath("whitespace.png");
        }

        public String getAttendFrm(final Student student) {
            String retFrm = "";
            final Map conditionMap = (Map) _param._hreportConditionMap.get(student._schoolKind);
            if (null == conditionMap) {
            	log.error("conditionMap null : schoolKind = " + student._schoolKind);
            } else {
            	final String semeField = _semesterMap.size() == 3 ? "1" : "2";
        		final String koudouField;
        		final HreportCondition hreportCondition8 = (HreportCondition) conditionMap.get("108");
        		if (null != hreportCondition8 && "1".equals(hreportCondition8._remark1)) {
        			koudouField = "2";
        		} else {
        			koudouField = "1";
        		}
        		final HreportCondition hreportCondition6 = (HreportCondition) conditionMap.get("106");
        		final HreportCondition hreportCondition7 = (HreportCondition) conditionMap.get("107");
        		if ((null == hreportCondition6 || (null != hreportCondition6 && "".equals(hreportCondition6._remark1))) &&
        				(null == hreportCondition7 || (null != hreportCondition7 && "".equals(hreportCondition7._remark1)))
        				) {
        			retFrm = "KNJD429_3_" + koudouField + "_" + semeField + "_1.frm";
        		} else if ((null != hreportCondition6 && "1".equals(hreportCondition6._remark1)) &&
        				(null != hreportCondition7 && "1".equals(hreportCondition7._remark1))
        				) {
        			retFrm = "KNJD429_3_" + koudouField + "_" + semeField + "_4.frm";
        		} else if ((null == hreportCondition6 || (null != hreportCondition6 && "1".equals(hreportCondition6._remark1)))) {
        			retFrm = "KNJD429_3_" + koudouField + "_" + semeField + "_3.frm";
        		} else {
        			retFrm = "KNJD429_3_" + koudouField + "_" + semeField + "_2.frm";
        		}
            }

            return retFrm;
        }

        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2) {
            _lastSemester = _semester;
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new TreeMap();
            try {
                final String sql = "select"
                        + "   SEMESTER,"
                        + "   SEMESTERNAME,"
                        + "   SDATE,"
                        + "   EDATE"
                        + " from"
                        + "   SEMESTER_MST"
                        + " where"
                        + "   YEAR='" + _ctrlYear + "'"
                        + " order by SEMESTER"
                    ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    map.put(semester, new Semester(semester, rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE")));
                    if (!SEMEALL.equals(semester)) {
                        _lastSemester = semester;
                    }
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return map;
        }

        private Map getHreportConditionMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            //SEQ LIKE '1%'について。佐賀は知的障害(1)と知的以外(2)の設定があり、SEQが1××又は、2××と登録される。
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     HREPORT_CONDITION_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND SCHOOLCD = '" + _schoolCd + "' ");
            stb.append("     AND SEQ LIKE '1%' ");
            stb.append(" ORDER BY ");
            stb.append("     SCHOOL_KIND ");

            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String setSchoolKind = rs.getString("SCHOOL_KIND");
                    final String seq = rs.getString("SEQ");
                    final String remark1 = rs.getString("REMARK1");
                    final String remark2 = rs.getString("REMARK2");
                    final String remark3 = rs.getString("REMARK3");
                    final String remark4 = rs.getString("REMARK4");
                    final String remark5 = rs.getString("REMARK5");
                    final String remark6 = rs.getString("REMARK6");
                    final String remark7 = rs.getString("REMARK7");
                    final String remark8 = rs.getString("REMARK8");
                    final String remark9 = rs.getString("REMARK9");
                    final String remark10 = rs.getString("REMARK10");
                    final HreportCondition condition = new HreportCondition(remark1, remark2, remark3, remark4, remark5, remark6, remark7, remark8, remark9, remark10);
                    final Map seqMap;
                    if (retMap.containsKey(setSchoolKind)) {
                        seqMap = (Map) retMap.get(setSchoolKind);
                    } else {
                        seqMap = new HashMap();
                    }
                    seqMap.put(seq, condition);
                    retMap.put(setSchoolKind, seqMap);
                }
            } catch (SQLException ex) {
                log.error("certif_school_dat exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private Map getCertifSchoolMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     CERTIF_KINDCD, ");
            stb.append("     SCHOOL_NAME, ");
            stb.append("     JOB_NAME, ");
            stb.append("     PRINCIPAL_NAME ");
            stb.append(" FROM ");
            stb.append("     CERTIF_SCHOOL_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND CERTIF_KINDCD IN ('103', '104', '117') ");

            final Map retMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String certifKindcd = rs.getString("CERTIF_KINDCD");
                    final String schoolName = rs.getString("SCHOOL_NAME");
                    final String jobName = rs.getString("JOB_NAME");
                    final String principalName = rs.getString("PRINCIPAL_NAME");
                    final CertifSchool certifSchol = new CertifSchool(schoolName, jobName, principalName);
                    final String setSchoolKind;
                    if ("117".equals(certifKindcd)) {
                        setSchoolKind = "P";
                    } else if ("103".equals(certifKindcd)) {
                        setSchoolKind = "J";
                    } else {
                        setSchoolKind = "H";
                    }
                    retMap.put(setSchoolKind, certifSchol);
                }
            } catch (SQLException ex) {
                log.error("certif_school_dat exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private Map getVNameMst(final DB2UDB db2, final String nameCd1) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     NAMECD2, ");
            stb.append("     NAME1 ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND NAMECD1 = '" + nameCd1 + "' ");
            stb.append(" ORDER BY ");
            stb.append("     NAMECD1 ");

            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String namecd2 = rs.getString("NAMECD2");
                    final String name1 = rs.getString("NAME1");
                    retMap.put(namecd2, name1);
                }
            } catch (SQLException ex) {
                log.error("certif_school_dat exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        public String getImageFilePath(final String name) {
            final String path = _documentroot + "/" + (null == _imagepath || "".equals(_imagepath) ? "" : _imagepath + "/") + name;
            final boolean exists = new java.io.File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

    }
}

// eof
