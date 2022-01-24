// kanji=漢字
package servletpack.KNJX;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.StaffInfo;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/*
 *  学校教育システム 賢者 [出欠管理] 月別出欠統計表
 */

public class KNJX_C162 {

    private static final Log log = LogFactory.getLog(KNJX_C162.class);
    private static String FROM_TO_MARK = "\uFF5E";

    private static final String SPECIAL_ALL = "999";
    private static final String SAKAE = "sakae";
    private static final String csv = "csv";
    private static final String TARGET_ALL = "ALL";

    private Param _param;

    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        DB2UDB db2 = null;
        try {
            // ＤＢ接続
            try {
                db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
                db2.open();
            } catch(Exception ex) {
                log.error("db2 instancing exception! ", ex);
                return;
            }
            log.info(" $Revision: 69240 $ ");
            KNJServletUtils.debugParam(request, log);
            _param = new Param(request, db2);

            final Map csvParam = new HashMap();
            csvParam.put("HttpServletRequest", request);
            // CSV出力処理
            final List outputLines = new ArrayList();
            printMain(db2, outputLines);
            CsvUtils.outputLines(log, response, _param._fName + ".csv", outputLines, csvParam);

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            if (null != _param) {
                DbUtils.closeQuietly(_param._psAttendance);
                DbUtils.closeQuietly(_param._psAttendanceDays);
                DbUtils.closeQuietly(_param._psAttendanceRemark);
                DbUtils.closeQuietly(_param._psSchregAbsenceHigh);
            }
            if (null != db2) {
                db2.close();
            }
        }
    }

    /**
     *  印刷処理
     */
    private boolean printMain(final DB2UDB db2, final List csvlist) {
        final boolean csvFlg = (csvlist != null) ? true : false;

        boolean nonedata = false;

        final List homerooms = Homeroom.getHomeRooms(db2, _param);

        List csvDataList = new ArrayList();

        final String hokenshitsuKekkaMei = _param.getKintaiName("14");
        final String kessekiName = StringUtils.defaultString(_param.getKintaiName("6"), StringUtils.defaultString(_param.getKintaiName("5"), _param.getKintaiName("4")));
        final String nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._year)) + "年度" + (("1".equals(_param._knjc162NenkanAttendance)) ? "　"+_param._targetSemestername : "");

        if (csvFlg) {
            //CSVのヘッダ生成
            _param._fName = nendo + "　クラス別出欠一覧表";

            csvDataList.add(_param._fName + "　（集計範囲：" + _param.getRange(db2) + "）");
            csvlist.add(csvDataList);

            csvDataList = new ArrayList();
            if(_param._header) {
                //ヘッダ有
                String[] header =  {"年組","番","氏名","性別","授業日数","忌引日数","出停停止","留学日数","出席すべき日数",kessekiName,"出席日数","遅刻","早退","授業出席率","LHR","学校行事","生徒会行事","その他","計",hokenshitsuKekkaMei,"備考"};
                for(int idx = 0; idx < header.length; idx++) {
                    csvDataList.add(header[idx]);
                }
                csvlist.add(csvDataList);
            }
        }

        String befGradeHrClass = "";
        for (final Iterator it = homerooms.iterator(); it.hasNext();) {
            final Homeroom homeroom = (Homeroom) it.next();

            // 生徒がいなければ処理をスキップ
            if (homeroom._studentAllList.size() == 0) {
                continue;
            }

            if(!"".equals(befGradeHrClass) && !befGradeHrClass.equals(homeroom._grade + homeroom._hrclass)) {
                //年組が切り替わった際、空行挿入
                List blankList = new ArrayList();
                blankList.add("");
                csvlist.add(blankList);
            }

            final int MAX_LINE_PER_PAGE = 45;
            final List pageList = getPageList(homeroom._studentAllList, MAX_LINE_PER_PAGE);

            for (int pi = 0; pi < pageList.size(); pi++) {
                final List studentList = (List) pageList.get(pi);

                nonedata = true;

                final AttendanceCount sum = new AttendanceCount();

                int lessonDaysMin = Integer.MAX_VALUE; // 授業日数最小
                int lessonDaysMax = Integer.MIN_VALUE; // 授業日数最大

                int specialLessonMin = Integer.MAX_VALUE; // 特別活動時数最小
                int specialLessonMax = Integer.MIN_VALUE; // 特別活動時数最大

                for (int li = 0; li < studentList.size(); li++) {
                    csvDataList = new ArrayList(); //CSV出力用リストの初期化
                    final int line = li + 1;
                    final Student student = (Student) studentList.get(li);

                    int specialLessons = 0;


                    final String showName = _param._staffInfo.getStrEngOrJp(student._name, student._nameEng);

                    final String name = ("1".equals(_param._use_SchregNo_hyoji)) ? student._schregno + " " + showName : showName;
                    csvDataList.add(homeroom._hrname); //年組
                    csvDataList.add(Integer.valueOf(student._attendNo).toString()); //番
                    csvDataList.add(name); //氏名
                    csvDataList.add(student._sexname); //性別

                    if (student._attend != null) {
                        specialLessons = printAttendanceCount(db2, student, line, student._attend, csvDataList, csvFlg);
                    }
                    final int specialLessonTotal = specialLessons;

                    lessonDaysMin = student.getLessonMin(lessonDaysMin);
                    lessonDaysMax = student.getLessonMax(lessonDaysMax);

                    specialLessonMin = Math.min(specialLessonTotal, specialLessonMin);
                    specialLessonMax = Math.max(specialLessonTotal, specialLessonMax);

                    sum.addAttendanceDay(student._attend);

                    String remark = "";
                    if (!SAKAE.equals(_param._z010) && null != student._remark1) {
                        remark = student._remark1.toString();
                    } else if (SAKAE.equals(_param._z010)) {
                        remark = getSchregAttendRemark(db2, student._schregno);
                    }
                    csvDataList.add(remark); //備考

                    //CSVの設定
                    if (csvFlg) {
                        csvlist.add(csvDataList);
                        befGradeHrClass = homeroom._grade + homeroom._hrclass;
                    }
                }
            }
        }

        return nonedata;
    }

    private static List getPageList(final List list, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    /** svfへ出力する
     * @param student 生徒
     * @param svf svf
     * @return 特別活動時数(端数は切捨)
     */
    private int printAttendanceCount(final DB2UDB db2, final Student student, final int line, final AttendanceCount a, List csvDataList, final boolean csvFlg) {
        csvDataList.add(String.valueOf(a._lessonDays)); //授業日数
        csvDataList.add(String.valueOf(a._mourningDays)); //忌引日数
        csvDataList.add(String.valueOf(a._suspendDays)); //出停停止
        csvDataList.add(String.valueOf(a._abroadDays)); //留学日数
        csvDataList.add(String.valueOf(a._mlessonDays)); //出席すべき日数

        KNJSchoolMst knjSchoolMst = null;
        try {
            final Map paramMap = new TreeMap();
            if ("1".equals(_param._useSchool_KindField)) {
                paramMap.put("SCHOOL_KIND", _param._SCHOOLKIND);
                paramMap.put("SCHOOLCD", _param._SCHOOLCD);
            }
            if ("1".equals(_param._use_school_detail_gcm_dat)) {
                paramMap.put("TABLENAME", "V_SCHOOL_GCM_MST");
                paramMap.put("COURSECD", student._courseCd);
                paramMap.put("MAJORCD", student._majorCd);
            }
            final Integer cacheKey = new Integer(paramMap.hashCode());
            if (null == _param._knjschoolMstMap.get(cacheKey)) {
                paramMap.put("outputDebug", "1");
                _param._knjschoolMstMap.put(cacheKey, new KNJSchoolMst(db2, _param._year, paramMap));
            }
            knjSchoolMst = (KNJSchoolMst) _param._knjschoolMstMap.get(cacheKey);
        } catch (Throwable e) {
            log.fatal("exception!", e);
        }
        if (null == knjSchoolMst) {
            knjSchoolMst = _param._knjSchoolMst;
        }
        csvDataList.add(String.valueOf(a._sickDays)); //欠席
        csvDataList.add(String.valueOf(a._attendDays)); //出席日数
        csvDataList.add(String.valueOf(a._lateDays)); //遅刻日数
        csvDataList.add(String.valueOf(a._earlyDays)); //早退日数
        csvDataList.add(a.getAttendancePercentage()); //授業出席率

        // 欠課数の設定
        final BigDecimal spAttendLhr         = getSpecialSubclassGroupHour(a._specialSubClassKekkaMinutes, SpecialSubclass.specialGroupCdLhr, _param, knjSchoolMst);
        final BigDecimal spAttendSchoolEvent = getSpecialSubclassGroupHour(a._specialSubClassKekkaMinutes, SpecialSubclass.specialGroupCdSchoolEvent, _param, knjSchoolMst);
        final BigDecimal spAttendCommittee   = getSpecialSubclassGroupHour(a._specialSubClassKekkaMinutes, SpecialSubclass.specialGroupCdCommittee, _param, knjSchoolMst);
        final BigDecimal spAttendEtc         = getSpecialSubclassGroupHour(a._specialSubClassKekkaMinutes, SpecialSubclass.specialGroupCdEtc, _param, knjSchoolMst);
        final BigDecimal spAttendTotal       = (spAttendLhr.add(spAttendSchoolEvent).add(spAttendCommittee).add(spAttendEtc)).setScale(0, BigDecimal.ROUND_HALF_UP);
        csvDataList.add(String.valueOf(spAttendLhr.setScale(0, BigDecimal.ROUND_HALF_UP))); //LHR
        csvDataList.add(String.valueOf(spAttendSchoolEvent.setScale(0, BigDecimal.ROUND_HALF_UP))); //学校行事
        csvDataList.add(String.valueOf(spAttendCommittee.setScale(0, BigDecimal.ROUND_HALF_UP))); //生徒会行事
        csvDataList.add(String.valueOf(spAttendEtc.setScale(0, BigDecimal.ROUND_HALF_UP))); //その他
        csvDataList.add(String.valueOf(spAttendTotal)); //計
        csvDataList.add(String.valueOf(a._nurseOff)); //保健室欠

        final BigDecimal spLessonLhr         = getSpecialSubclassGroupJisu(a._specialSubClassLessonMinutes, SpecialSubclass.specialGroupCdLhr, _param, knjSchoolMst);
        final BigDecimal spLessonSchoolEvent = getSpecialSubclassGroupJisu(a._specialSubClassLessonMinutes, SpecialSubclass.specialGroupCdSchoolEvent, _param, knjSchoolMst);
        final BigDecimal spLessonCommittee   = getSpecialSubclassGroupJisu(a._specialSubClassLessonMinutes, SpecialSubclass.specialGroupCdCommittee, _param, knjSchoolMst);
        final BigDecimal spLessonEtc         = getSpecialSubclassGroupJisu(a._specialSubClassLessonMinutes, SpecialSubclass.specialGroupCdEtc, _param, knjSchoolMst);
        final BigDecimal spLessonTotal       = (spLessonLhr.add(spLessonSchoolEvent).add(spLessonCommittee).add(spLessonEtc)).setScale(0, BigDecimal.ROUND_HALF_UP);
        return spLessonTotal.intValue();
    }

    private static class Homeroom {
        final String _grade;
        final String _hrname;
        final String _staffname;
        final String _hrclass;
        private List _studentAllList = Collections.EMPTY_LIST;
        public Homeroom(final String grade, final String hrname, final String staffname, final String hrclass) {
            _grade = grade;
            _hrname = hrname;
            _staffname = staffname;
            _hrclass = hrclass;
        }

        private static List getHomeRooms(final DB2UDB db2, final Param param) {
            final List homerooms = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            //SQL作成
            try {
                // HRのSQL
                log.debug("sql = " + sqlSchregRegdHdat(param));
                ps = db2.prepareStatement(sqlSchregRegdHdat(param));
                rs = ps.executeQuery();


                // HRごとに生徒と出欠情報を出力
                while(rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String hrname = rs.getString("HR_NAME");
                    final String hrclass = rs.getString("HR_CLASS");
                    final String staffName = rs.getString("STAFFNAME1");
                    homerooms.add(new Homeroom(grade, hrname, staffName, hrclass));
                }

                for (final Iterator it = homerooms.iterator(); it.hasNext();) {
                    final Homeroom homeroom = (Homeroom) it.next();
                    homeroom._studentAllList = getHrStudentList(db2, param, homeroom._grade, homeroom._hrclass);
                }

            } catch (Exception ex) {
                log.error("svfPrint exception! ", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return homerooms;
        }

        private static List getHrStudentList(final DB2UDB db2, final Param param, final String grade, final String hrClass) throws SQLException {
            // HRの生徒を取得
            final List studentList = Student.getStudentList(db2, param, grade, hrClass);

            if (studentList.size() == 0) {
                return studentList;
            }

            //欠席事由を集計
            Map absmap = getSummaryAbsence(db2, param, grade, hrClass);
            String absencestr;

            ResultSet rs = null;
            // 1日単位
            param._psAttendanceDays.setString(1, grade);
            param._psAttendanceDays.setString(2, hrClass);
            rs = param._psAttendanceDays.executeQuery();
            while(rs.next()) {
                final Student student = Student.getStudent(rs.getString("SCHREGNO"), studentList);
                if (student == null || !"9".equals(rs.getString("SEMESTER"))) {
                    continue;
                }
                if (student._attend == null) {
                    student._attend = new AttendanceCount();
                }
                absencestr = "";
                if (absmap.size() > 0) {
                    if (absmap.containsKey(rs.getString("SCHREGNO")))
                        absencestr = (String)absmap.get(rs.getString("SCHREGNO"));
                }
                student._attend.addAttendanceDay(rs, absencestr);
            }
            DbUtils.closeQuietly(rs);

            // 時数単位
            param._psAttendance.setString(1, grade);
            param._psAttendance.setString(2, hrClass);
            rs = param._psAttendance.executeQuery();
            while(rs.next()) {
                final Student student = Student.getStudent(rs.getString("SCHREGNO"), studentList);
                if (student == null || !"9".equals(rs.getString("SEMESTER"))) {
                    continue;
                }
                if (student._attend == null) {
                    student._attend = new AttendanceCount();
                }
                student._attend.addSubclassAttendance(param, rs);
            }
            DbUtils.closeQuietly(rs);

            if (null != param._psAttendanceRemark) {
                param._psAttendanceRemark.setString(1, grade);
                param._psAttendanceRemark.setString(2, hrClass);
                rs = param._psAttendanceRemark.executeQuery();
                while(rs.next()) {
                    final Student student = Student.getStudent(rs.getString("SCHREGNO"), studentList);
                    if (student == null) {
                        continue;
                    }
                    if (null == student._remark1) {
                        student._remark1 = new StringBuffer();
                    } else {
                        student._remark1.append(" ");
                    }
                    student._remark1.append(rs.getString("REMARK1"));
                }

                DbUtils.closeQuietly(rs);
            }

            param._warnSemester = param.setWarnSemester(db2, grade);
            String sql = "";
            if (param.isHoutei()) {
                sql = param.getHouteiJisuSql(param);
            } else {
                sql = param.getJituJisuSql(param);
            }
            log.debug("get AbsenceHigh sql = " + sql);
            param._psSchregAbsenceHigh = db2.prepareStatement(sql);

            // 特別活動グループコード999の上限値
            param._psSchregAbsenceHigh.setString(1, grade);
            param._psSchregAbsenceHigh.setString(2, hrClass);
            rs = param._psSchregAbsenceHigh.executeQuery();
            while(rs.next()) {
                final Student student = Student.getStudent(rs.getString("SCHREGNO"), studentList);
                if (student == null) {
                    continue;
                }
                if (0.0 != rs.getDouble("ABSENCE_HIGH")) {
                    student._compAbsenceHighSpecial999 = rs.getDouble("ABSENCE_HIGH");
                }
            }
            DbUtils.closeQuietly(rs);

            return studentList;
        }

        /** 学年クラスとクラス名称の列挙を得るSQL */
        private static String sqlSchregRegdHdat(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.HR_NAME, ");
            stb.append("     T2.STAFFNAME AS STAFFNAME1, ");
            stb.append("     T3.STAFFNAME AS STAFFNAME2, ");
            stb.append("     T4.STAFFNAME AS STAFFNAME3 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ");
            stb.append("             ON GDAT.YEAR  = T1.YEAR ");
            stb.append("            AND GDAT.GRADE = T1.GRADE ");
            if(!TARGET_ALL.equals(param._schoolkind)) {
                stb.append("        AND GDAT.SCHOOL_KIND = '"+param._schoolkind+"' ");
            }
            stb.append("     LEFT JOIN STAFF_MST T2 ON T1.TR_CD1 = T2.STAFFCD ");
            stb.append("     LEFT JOIN STAFF_MST T3 ON T1.TR_CD2 = T3.STAFFCD ");
            stb.append("     LEFT JOIN STAFF_MST T4 ON T1.TR_CD3 = T4.STAFFCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '"+param._year+"' ");
            stb.append("     AND T1.SEMESTER = '"+param._semester+"' ");
            if(!TARGET_ALL.equals(param._grade)) {
                if(!TARGET_ALL.equals(param._gradeHrClass)) {
                    stb.append("     AND T1.GRADE || T1.HR_CLASS = '"+param._gradeHrClass+"' ");
                } else {
                    stb.append("     AND T1.GRADE = '"+param._grade+"' ");
                }
            }
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, T1.HR_CLASS ");
            return stb.toString();
        }

        private static Map getSummaryAbsence(final DB2UDB db2, final Param param, final String grade, final String hrClass) throws SQLException {
            Map retmap = new HashMap();
            if (SAKAE.equals(param._z010)) {
                final String sqlabs = sqlSummaryAbsence(param, grade, hrClass);
                PreparedStatement ps3 = db2.prepareStatement(sqlabs);
                ResultSet rs3 = ps3.executeQuery();
                String keystr = "";
                String absencestr = "";
                boolean bfirstflg = true;
                while (rs3.next()) {
                    if (!keystr.equals(rs3.getString("SCHREGNO"))) {
                        if (!bfirstflg) {
                            retmap.put(keystr, absencestr);
                        }
                        absencestr = rs3.getString("NAME1") + rs3.getString("CNT");
                        keystr = rs3.getString("SCHREGNO");
                        bfirstflg = false;
                    } else {
                        absencestr += "、";
                        absencestr += rs3.getString("NAME1") + rs3.getString("CNT");
                    }
                }
                if (!bfirstflg && !"".equals(keystr)) {
                    retmap.put(keystr, absencestr);
                }
                DbUtils.closeQuietly(null, ps3, rs3);
            }
            return retmap;
        }

        private static String sqlSummaryAbsence(final Param param, final String grade, final String hrClass) {

            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH BASEGRPDAT AS ( ");
            stb.append(" SELECT ");
            stb.append("  TA1.YEAR, ");
            stb.append("  TA1.SCHREGNO, ");
            stb.append("  TA1.ATTENDDATE, ");
            stb.append("  TA1.DI_REMARK_CD ");
            stb.append(" FROM ");
            stb.append("  ATTEND_DAT TA1 ");
            stb.append("  LEFT JOIN NAME_MST B001 ON B001.NAMECD2 = TA1.PERIODCD ");
            stb.append("         AND B001.NAMECD1 = 'B001' ");
            stb.append("         AND B001.NAMESPARE1 IS NOT NULL ");
            stb.append(" WHERE ");
            stb.append("  TA1.DI_REMARK_CD IS NOT NULL ");
            stb.append("  AND TA1.DI_REMARK_CD <> '' ");
            stb.append("  AND B001.NAMECD2 IS NOT NULL ");
            stb.append(" GROUP BY ");
            stb.append("  TA1.YEAR, ");
            stb.append("  TA1.SCHREGNO, ");
            stb.append("  TA1.ATTENDDATE, ");
            stb.append("  TA1.DI_REMARK_CD ");
            stb.append(" ), SUMMARYBASEDAT AS ( ");
            stb.append(" SELECT ");
            stb.append("  T1.YEAR, ");
            stb.append("  T1.SCHREGNO, ");
            stb.append("  SRD.GRADE, ");
            stb.append("  SRD.HR_CLASS, ");
            stb.append("  SRD.ATTENDNO, ");
            stb.append("  T1.ATTENDDATE, ");
            stb.append("  1 AS CNT, ");
            stb.append("  T1.DI_REMARK_CD ");
            stb.append(" FROM ");
            stb.append("  BASEGRPDAT T1 ");
            stb.append("  LEFT JOIN SCHREG_REGD_DAT SRD ON SRD.SCHREGNO = T1.SCHREGNO AND SRD.YEAR = T1.YEAR ");
            stb.append("    AND SRD.SEMESTER = '" + param._semester + "' ");
            stb.append(" WHERE ");
            stb.append("  T1.ATTENDDATE between '" + param._attendStartDate + "' AND '" +  param._attendEndDate + "' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("  SBD.GRADE, ");
            stb.append("  SBD.HR_CLASS, ");
            stb.append("  SBD.ATTENDNO, ");
            stb.append("  SBD.SCHREGNO, ");
            stb.append("  SBD.DI_REMARK_CD, ");
            stb.append("  C901.NAME1, ");
            stb.append("  count(SBD.CNT) AS CNT ");
            stb.append(" FROM SUMMARYBASEDAT SBD ");
            stb.append("  LEFT JOIN NAME_MST C901 ON SBD.DI_REMARK_CD = C901.NAMECD2 AND C901.NAMECD1 = 'C901' ");
            stb.append(" WHERE ");
            stb.append("   SBD.YEAR = '" + param._year + "' ");
            stb.append("   AND SBD.GRADE = '" + grade + "' ");
            stb.append("   AND SBD.HR_CLASS = '" + hrClass + "' ");
            stb.append("   AND C901.NAME1 IS NOT NULL ");
            stb.append(" GROUP BY ");
            stb.append("  SBD.GRADE, ");
            stb.append("  SBD.HR_CLASS, ");
            stb.append("  SBD.ATTENDNO, ");
            stb.append("  SBD.SCHREGNO, ");
            stb.append("  SBD.DI_REMARK_CD, ");
            stb.append("  C901.NAME1 ");

            return stb.toString();
        }
    }

    /**
     * 最小値から最大値までの出力用文字列を得る
     * @param min 最小値
     * @param max 最大値
     * @return 最小値から最大値までの出力用文字列
     */
    private String getOutputFromTo(final int min, final int max) {
        if (min == max) {
            return String.valueOf(min);
        }
        return String.valueOf(min) + FROM_TO_MARK + String.valueOf(max);
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _targetSemester;
        final String _targetSemestername;
        final String _attendStartDate;
        final String _attendEndDate;
        final String _loginDate;
        final String _schoolkind;
        final String _grade;
        final String _gradeHrClass;
        final boolean _useAbsenceWarn; // 注意 or 超過
        final boolean _header; // ヘッダ有

        private PreparedStatement _psAttendanceDays; // 1日単位の出欠
        private PreparedStatement _psAttendance; // 時数単位の出欠
        private PreparedStatement _psAttendanceRemark; // 出欠備考
        private PreparedStatement _psSchregAbsenceHigh; // 特活上限値
        private String _z012;
        private String _z010;
        private Map _kintaiNameMap;
        private Map _specialSubcassGroupMap;
        private KNJSchoolMst _knjSchoolMst;
        /** C005：欠課換算法修正 */
        private Map _subClassC005 = new HashMap();
        /** C042：単位マスタの警告数は単位が回数か */
        private boolean _absenceWarnIsUnitCount;
        /** 注意週数学期 */
        private String _warnSemester;
        /** 教育課程コードを使用するか */
        private final String _useCurriculumcd;
        final Map _attendParamMap;

        /** 氏名欄に学籍番号の表示/非表示 1:表示 それ以外:非表示 */
        final String _use_SchregNo_hyoji;

        final String _use_school_detail_gcm_dat;
        Map _attendItemNameMap;
        final String _useSchool_KindField;
        final String _SCHOOLCD;
        final String _SCHOOLKIND;
        final Map _knjschoolMstMap = new HashMap();
        private Map _jobnameMap = Collections.EMPTY_MAP;

        final String _cmd;
        private String _fName;

        /** 生徒氏名（英語・日本語）切替処理用 */
        final String _staffCd;
        final StaffInfo _staffInfo;

        final String _knjc162NenkanAttendance;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("CTRL_YEAR");
            _semester  = request.getParameter("CTRL_SEMESTER");
            _targetSemester = request.getParameter("SEMESTER");
            _targetSemestername = "9".equals(_targetSemester) ? "年間" : getSemesterName(db2, _year, _targetSemester);
            _attendStartDate = request.getParameter("SDATE").replace('/', '-');
            _attendEndDate = request.getParameter("EDATE").replace('/', '-');
            _loginDate = request.getParameter("CTRL_DATE").replace('/', '-');
            _schoolkind = request.getParameter("SCHOOL_KIND");
            _grade = request.getParameter("GRADE");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _useAbsenceWarn = "1".equals(request.getParameter("TYUI_TYOUKA"));
            _header = "on".equals(request.getParameter("HEADER"));
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _use_SchregNo_hyoji = request.getParameter("use_SchregNo_hyoji");
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _use_school_detail_gcm_dat = request.getParameter("use_school_detail_gcm_dat");
            if ("1".equals(_use_school_detail_gcm_dat)) {
                _attendItemNameMap = getAdminCntrlAttendItemNameMap(db2);
            }
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD = request.getParameter("SCHOOLCD");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");

            _cmd = request.getParameter("cmd");
            _fName = "";

            _staffCd = request.getParameter("PRINT_LOG_STAFFCD");
            _staffInfo = new StaffInfo(db2, _staffCd);

            try {
                _z012 = getZ012(db2);
                _z010 = getZ010(db2);
                _kintaiNameMap = getKintaiNameMap(db2);
                _specialSubcassGroupMap = getSpecialSubclassGroupMap(db2);

                try {
                    final Map paramMap = new TreeMap();
                    if ("1".equals(_useSchool_KindField)) {
                        paramMap.put("SCHOOL_KIND", _SCHOOLKIND);
                        paramMap.put("SCHOOLCD", _SCHOOLCD);
                    }
                    final Integer cacheKey = new Integer(paramMap.hashCode());
                    if (null == _knjschoolMstMap.get(cacheKey)) {
                        _knjschoolMstMap.put(cacheKey, new KNJSchoolMst(db2, _year, paramMap));
                    }
                    _knjSchoolMst = (KNJSchoolMst) _knjschoolMstMap.get(cacheKey);
                } catch (Throwable e) {
                    log.fatal("exception!", e);
                }

                loadNameMstC005(db2);
                loadNameMstC042(db2);

                // 1日単位
                _attendParamMap.put("grade", "?");
                _attendParamMap.put("hrClass", "?");
                String sql;
                sql = AttendAccumulate.getAttendSemesSql(
                        _year,
                        "9",
                        _attendStartDate,
                        _attendEndDate,
                        _attendParamMap
                );
                log.debug("get AttendSemes sql = " + sql);
                _psAttendanceDays = db2.prepareStatement(sql);

                // 時数単位
                sql = AttendAccumulate.getAttendSubclassSql(
                        _year,
                        "9",
                        _attendStartDate,
                        _attendEndDate,
                        _attendParamMap
                        );
                log.debug("get AttendSubclass sql = " + sql);
                _psAttendance = db2.prepareStatement(sql);

                final Map hasuuMap = AttendAccumulate.getHasuuMap(db2, _year, _attendStartDate, _attendEndDate);
                final String attendSemesInState = (String) hasuuMap.get("attendSemesInState");
                if (null != attendSemesInState) {
                    sql = getAttendanceRemarkSql(this, attendSemesInState);
                    log.debug(" remark sql = " + sql);
                    _psAttendanceRemark = db2.prepareStatement(sql);
                }

                _jobnameMap = getPrgStampDat(db2);
                if (_jobnameMap.isEmpty()) {
                    _jobnameMap.put("1", "校長");
                    _jobnameMap.put("2", "教頭");
                    _jobnameMap.put("3", "教頭");
                    _jobnameMap.put("4", "教務主任");
                    _jobnameMap.put("5", "学年主任");
                    _jobnameMap.put("6", "担任");
                }
                log.info(" jobnameMap = " + _jobnameMap);

            } catch (Exception ex) {
                log.error("Param load exception!", ex);
            }
            _knjc162NenkanAttendance = request.getParameter("knjc162NenkanAttendance");
        }

        public String getRange(final DB2UDB db2) {
            return KNJ_EditDate.getAutoFormatDate(db2, _attendStartDate) + FROM_TO_MARK + KNJ_EditDate.getAutoFormatDate(db2, _attendEndDate);
        }

        // 印刷日
        private String getPrintDateStr(final DB2UDB db2) {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            DecimalFormat df = new DecimalFormat("00");
            String time = df.format(hour) + ":" + df.format(minute);

            return KNJ_EditDate.getAutoFormatDate(db2, _loginDate) + "（" + KNJ_EditDate.h_format_W(_loginDate) +"）" + time;
        }

        private String getKintaiName(String kintaiCd) {
            return (String) _kintaiNameMap.get(kintaiCd);
        }

        private boolean hasTable(final DB2UDB db2, final String tabname) throws SQLException {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  ");
            stb.append("    T1.TABNAME ");
            stb.append(" FROM SYSCAT.TABLES T1 ");
            stb.append(" WHERE ");
            stb.append("   T1.TABNAME = '" + tabname + "' ");

            boolean hasTable = false;

            PreparedStatement ps = db2.prepareStatement(stb.toString());
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                hasTable = true;
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);

            return hasTable;
        }

        private Map getPrgStampDat(final DB2UDB db2) throws SQLException {

            final Map seqTitleMap = new HashMap();

            if (hasTable(db2, "PRG_STAMP_DAT")) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT  ");
                stb.append("    T1.SEQ ");
                stb.append("  , T1.TITLE ");
                stb.append(" FROM PRG_STAMP_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR = '" + _year + "' ");
                stb.append("   AND T1.SEMESTER = '9' ");
                if ("1".equals(_useSchool_KindField)) {
                    stb.append("   AND T1.SCHOOLCD = '" + _SCHOOLCD + "' ");
                    stb.append("   AND T1.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
                }
                stb.append("   AND T1.PROGRAMID = 'KNJC162' ");

                PreparedStatement ps = db2.prepareStatement(stb.toString());
                final ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    seqTitleMap.put(rs.getString("SEQ"), rs.getString("TITLE"));
                }
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return seqTitleMap;
        }

        //TODOO: ??
        /**
         * 欠課換算法修正
         * @param db2
         * @throws SQLException
         */
        private void loadNameMstC005(final DB2UDB db2) throws SQLException {
            final String sql = "SELECT NAME1 AS SUBCLASSCD, NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'C005'";
            PreparedStatement ps = db2.prepareStatement(sql);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String is = rs.getString("NAMESPARE1");
                log.debug("(名称マスタ C005):科目コード=" + subclassCd);
                _subClassC005.put(subclassCd, is);
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
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

        private String setWarnSemester(final DB2UDB db2, final String grade) throws SQLException {
            String warnSemester = "";
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT T1.YEAR, T1.SEMESTER, T1.SDATE, T1.EDATE, T2.SEMESTER AS NEXT_SEMESTER, T2.SDATE AS NEXT_SDATE ");
            stb.append(" FROM V_SEMESTER_GRADE_MST T1 ");
            stb.append(" LEFT JOIN V_SEMESTER_GRADE_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND INT(T2.SEMESTER) = INT(T1.SEMESTER) + 1 ");
            stb.append("     AND T2.GRADE = T1.GRADE ");
            stb.append(" WHERE T1.YEAR = '" + _year + "' ");
            stb.append("     AND T1.SEMESTER <> '9' ");
            stb.append("     AND T1.GRADE = '" + grade + "' ");
            stb.append("     AND (('" + _attendEndDate + "' BETWEEN T1.SDATE AND T1.EDATE) ");
            stb.append("          OR (T1.EDATE < '" + _attendEndDate + "' AND '" + _attendEndDate + "' < VALUE(T2.SDATE, '9999-12-30'))) ");
            stb.append(" ORDER BY T1.SEMESTER ");

            PreparedStatement ps = db2.prepareStatement(stb.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                warnSemester = StringUtils.defaultString(rs.getString("SEMESTER"));
            }
            DbUtils.closeQuietly(null, ps, rs);
            return warnSemester;
        }

        private String getHouteiJisuSql(final Param parameter) {
            final String tableName = "V_CREDIT_SPECIAL_MST";
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.SCHREGNO, ");
            stb.append("     VALUE(T1.ABSENCE_HIGH, 0) ");
            if (parameter._useAbsenceWarn) {
                if (parameter._absenceWarnIsUnitCount) {
                    final String sem = "1".equals(parameter._warnSemester) ? "" : parameter._warnSemester;
                    stb.append("      - VALUE(ABSENCE_WARN" + sem + ", 0) ");
                } else {
                    stb.append("      - VALUE(ABSENCE_WARN_RISHU_SEM" + parameter._warnSemester + ", 0) ");
                }
            }
            stb.append("       AS ABSENCE_HIGH, ");
            stb.append("     VALUE(T1.GET_ABSENCE_HIGH, 0) ");
            if (parameter._useAbsenceWarn) {
                if (parameter._absenceWarnIsUnitCount) {
                    final String sem = "1".equals(parameter._warnSemester) ? "" : parameter._warnSemester;
                    stb.append("      - VALUE(ABSENCE_WARN" + sem + ", 0) ");
                } else {
                    stb.append("      - VALUE(ABSENCE_WARN_SHUTOKU_SEM" + parameter._warnSemester + ", 0) ");
                }
            }
            stb.append("       AS GET_ABSENCE_HIGH ");
            stb.append(" FROM ");
            stb.append("     " + tableName + " T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON ");
            stb.append("       T2.GRADE = T1.GRADE ");
            stb.append("       AND T2.COURSECD = T1.COURSECD ");
            stb.append("       AND T2.MAJORCD = T1.MAJORCD ");
            stb.append("       AND T2.COURSECODE = T1.COURSECODE ");
            stb.append("       AND T2.YEAR = T1.YEAR ");
            stb.append("       AND T2.SEMESTER = '" + parameter._semester + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + parameter._year + "' ");
            stb.append("     AND T1.SPECIAL_GROUP_CD = '" + SPECIAL_ALL + "' ");
            stb.append("     AND T2.GRADE  = ? AND T2.HR_CLASS = ? ");
            return stb.toString();
        }

        private String getJituJisuSql(final Param parameter) {
            final String tableName = "SCHREG_ABSENCE_HIGH_SPECIAL_DAT";
            final String tableName2 = "V_CREDIT_SPECIAL_MST";
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.SCHREGNO, ");
            stb.append("     VALUE(T1.COMP_ABSENCE_HIGH, 0) ");
            if (parameter._useAbsenceWarn) {
                if (parameter._absenceWarnIsUnitCount) {
                    final String sem = "1".equals(parameter._warnSemester) ? "" : parameter._warnSemester;
                    stb.append("      - VALUE(T3.ABSENCE_WARN" + sem + ", 0) ");
                } else {
                    stb.append("      - VALUE(T3.ABSENCE_WARN_RISHU_SEM" + parameter._warnSemester + ", 0) ");
                }
            }
            stb.append("     AS ABSENCE_HIGH, ");
            stb.append("     VALUE(T1.GET_ABSENCE_HIGH, 0) ");
            if (parameter._useAbsenceWarn) {
                if (parameter._absenceWarnIsUnitCount) {
                    final String sem = "1".equals(parameter._warnSemester) ? "" : parameter._warnSemester;
                    stb.append("      - VALUE(T3.ABSENCE_WARN" + sem + ", 0) ");
                } else {
                    stb.append("      - VALUE(T3.ABSENCE_WARN_SHUTOKU_SEM" + parameter._warnSemester + ", 0) ");
                }
            }
            stb.append("     AS GET_ABSENCE_HIGH ");
            stb.append(" FROM ");
            stb.append("     " + tableName + " T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON ");
            stb.append("       T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("       AND T2.YEAR = T1.YEAR ");
            stb.append("       AND T2.SEMESTER = '" + parameter._semester + "' ");
            stb.append("     LEFT JOIN " + tableName2 + " T3 ON ");
            stb.append("       T3.SPECIAL_GROUP_CD = T1.SPECIAL_GROUP_CD ");
            stb.append("       AND T3.COURSECD = T2.COURSECD ");
            stb.append("       AND T3.MAJORCD = T2.MAJORCD ");
            stb.append("       AND T3.GRADE = T2.GRADE ");
            stb.append("       AND T3.COURSECODE = T2.COURSECODE ");
            stb.append("       AND T3.YEAR = T1.YEAR ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + parameter._year + "' ");
            stb.append("     AND T1.DIV = '2' ");
            stb.append("     AND T1.SPECIAL_GROUP_CD = '" + SPECIAL_ALL + "' ");
            stb.append("     AND T2.GRADE  = ? AND T2.HR_CLASS = ? ");
            return stb.toString();
        }

        public SpecialSubclass getSpecialSubclass(final String subclassCd) {
            for (Iterator iter = _specialSubcassGroupMap.keySet().iterator(); iter.hasNext();) {
                final String specialSubclassGroupCd = (String) iter.next();
                final Map specialSubclassMap = (Map) _specialSubcassGroupMap.get(specialSubclassGroupCd);
                for (Iterator itSubclass = specialSubclassMap.keySet().iterator(); itSubclass.hasNext();) {
                    final String spSubclassCd = (String) itSubclass.next();
                    if (spSubclassCd.equals(subclassCd)) {
                        return (SpecialSubclass) specialSubclassMap.get(spSubclassCd);
                    }
                }
            }
            return null;
        }

        public Map getSpecialSubclassGroup(final String specialSubclassGroupCd) {
            for (Iterator iter = _specialSubcassGroupMap.keySet().iterator(); iter.hasNext();) {
                final String specialSubclassGroupCd1 = (String) iter.next();
                if (specialSubclassGroupCd1.equals(specialSubclassGroupCd)) {
                    return (Map) _specialSubcassGroupMap.get(specialSubclassGroupCd1);
                }
            }
            return null;
        }

        public boolean isSpecialSubclass(final String subclassCd) {
            //log.debug("subclass " + subclassCd + " is special? = " + (getSpecialSubclass(subclassCd) != null));
            return getSpecialSubclass(subclassCd) != null;
        }

        private Map getSpecialSubclassGroupMap(final DB2UDB db2) {
            final Map specialSubclassGroupMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.SPECIAL_GROUP_CD, ");
                stb.append("     T2.SPECIAL_GROUP_NAME, ");
                stb.append("     T2.SPECIAL_GROUP_ABBV, ");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append("     T1.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     T1.MINUTES ");
                stb.append(" FROM ");
                stb.append("     ATTEND_SUBCLASS_SPECIAL_DAT T1 ");
                stb.append("     INNER JOIN ATTEND_SUBCLASS_SPECIAL_MST T2 ON ");
                stb.append("         T1.SPECIAL_GROUP_CD = T2.SPECIAL_GROUP_CD ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _year +  "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String specialGroupCd = rs.getString("SPECIAL_GROUP_CD");

                    if (specialSubclassGroupMap.get(specialGroupCd) == null) {
                        specialSubclassGroupMap.put(specialGroupCd, new TreeMap());
                    }
                    Map specialGroup = (Map) specialSubclassGroupMap.get(specialGroupCd);

                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final int minute = rs.getInt("MINUTES");
                    specialGroup.put(subclassCd, new SpecialSubclass(specialGroupCd, subclassCd, minute));
                }

            } catch (SQLException e) {
                log.error("getSpecialSubClassGroup exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return specialSubclassGroupMap;
        }


        private Map getAdminCntrlAttendItemNameMap(final DB2UDB db2) {
            final Map retItemNameMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH MAIN_T AS ( ");
                stb.append(" SELECT DISTINCT ");
                stb.append("     ATTEND_D.COURSECD, ");
                stb.append("     ATTEND_D.MAJORCD, ");
                stb.append("     CASE WHEN ATTEND_D.ATTEND_ITEM = 'NONOTICE' ");
                stb.append("          THEN '1' ");
                stb.append("          WHEN ATTEND_D.ATTEND_ITEM = 'NOTICE' ");
                stb.append("          THEN '2' ");
                stb.append("          ELSE '3' ");
                stb.append("     END AS SORT, ");
                stb.append("     ATTEND_ITEM.ATTEND_ITEMNAME ");
                stb.append(" FROM ");
                stb.append("     ADMIN_CONTROL_ATTEND_DAT ATTEND_D ");
                stb.append("     LEFT JOIN ADMIN_CONTROL_ATTEND_ITEMNAME_DAT ATTEND_ITEM ON ATTEND_D.YEAR = ATTEND_ITEM.YEAR ");
                stb.append("          AND ATTEND_D.SCHOOL_KIND = ATTEND_ITEM.SCHOOL_KIND ");
                stb.append("          AND ATTEND_D.ATTEND_DIV = ATTEND_ITEM.ATTEND_DIV ");
                stb.append("          AND ATTEND_D.GRADE = ATTEND_ITEM.GRADE ");
                stb.append("          AND ATTEND_D.COURSECD = ATTEND_ITEM.COURSECD ");
                stb.append("          AND ATTEND_D.MAJORCD = ATTEND_ITEM.MAJORCD ");
                stb.append("          AND ATTEND_D.ATTEND_ITEM = ATTEND_ITEM.ATTEND_ITEM ");
                stb.append(" WHERE ");
                stb.append("     ATTEND_D.YEAR = '" + _year + "' ");
                stb.append("     AND ATTEND_D.SCHOOL_KIND IN (SELECT GDAT.SCHOOL_KIND FROM SCHREG_REGD_GDAT GDAT WHERE GDAT.YEAR = '" + _year + "' AND GDAT.GRADE = '" + _grade + "') ");
                stb.append("     AND ATTEND_D.CONTROL_DIV = '1' ");
                stb.append("     AND ATTEND_D.ATTEND_DIV = '1' ");
                stb.append("     AND ATTEND_D.GRADE = '00' ");
                stb.append("     AND ATTEND_D.ATTEND_ITEM IN ('SICK', 'NOTICE', 'NONOTICE') ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("     T1.* ");
                stb.append(" FROM ");
                stb.append("     MAIN_T T1, ");
                stb.append("     (SELECT ");
                stb.append("          COURSECD, ");
                stb.append("          MAJORCD, ");
                stb.append("          MIN(SORT) AS SORT ");
                stb.append("      FROM ");
                stb.append("          MAIN_T ");
                stb.append("      GROUP BY ");
                stb.append("          COURSECD, ");
                stb.append("          MAJORCD ");
                stb.append("     ) T2 ");
                stb.append(" WHERE ");
                stb.append("     T1.COURSECD = T2.COURSECD ");
                stb.append("     AND T1.MAJORCD = T2.MAJORCD ");
                stb.append("     AND T1.SORT = T2.SORT ");
                stb.append("     AND T1.ATTEND_ITEMNAME IS NOT NULL ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String courseCd = rs.getString("COURSECD");
                    final String majorCd = rs.getString("MAJORCD");
                    final String attendItemName = rs.getString("ATTEND_ITEMNAME");
                    retItemNameMap.put(courseCd + majorCd, attendItemName);
                }

            } catch (SQLException e) {
                log.error("getSpecialSubClassGroup exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retItemNameMap;
        }

        private String getStaffNameString(final String staffName1, final String staffName2, final String staffName3) {
            String[] staffNames = {staffName1, staffName2, staffName3};
            StringBuffer stb = new StringBuffer();
            String comma = "";
            for (int i = 0; i < staffNames.length; i++) {
                if (staffNames[i] != null) {
                    stb.append(comma + staffNames[i]);
                    comma = "、";
                }
            }
            return stb.toString();
        }

        private Map getKintaiNameMap(final DB2UDB db2) throws SQLException {

            final Map kintaiNameMap = new HashMap();
            final StringBuffer sql = new StringBuffer();
            sql.append("SELECT ");
            sql.append(" T1.DI_CD AS KINTAI_CD, ");
            sql.append(" T1.DI_NAME1 AS KINTAI_NAME ");
            sql.append("FROM ");
            sql.append("   ATTEND_DI_CD_DAT T1 ");
            sql.append("WHERE ");
            sql.append("      T1.YEAR = '" + _year + "' ");
            sql.append("      AND T1.DI_CD in ('4','5','6','14') ");
            PreparedStatement ps = db2.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String kintaiCd = rs.getString("KINTAI_CD");
                String kintaiName = rs.getString("KINTAI_NAME");
                kintaiNameMap.put(kintaiCd, kintaiName);
            }
            DbUtils.closeQuietly(null, ps, rs);
            return kintaiNameMap;
        }

        private String getZ012(final DB2UDB db2) throws SQLException {
            final StringBuffer sql = new StringBuffer();
            sql.append("SELECT ");
            sql.append(" T2.NAME1 ");
            sql.append("FROM ");
            sql.append("   NAME_YDAT T1 ");
            sql.append("   INNER JOIN NAME_MST T2 ON T1.NAMECD1 = T2.NAMECD1 ");
            sql.append("         AND T1.NAMECD2 = T2.NAMECD2 ");
            sql.append("WHERE ");
            sql.append("      T1.NAMECD1 = 'Z012' ");
            sql.append("      AND T1.YEAR = '" + _year + "' ");
            log.debug("Z012 sql = " + sql);
            PreparedStatement ps = db2.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            String rtn = null;
            while (rs.next()) {
                rtn = rs.getString("NAME1");
                log.debug("Z012 = " + rtn);
            }
            DbUtils.closeQuietly(null, ps, rs);
            return rtn;
        }

        private String getZ010(final DB2UDB db2) throws SQLException {
            final StringBuffer sql = new StringBuffer();
            sql.append("SELECT ");
            sql.append(" T1.NAME1 ");
            sql.append("FROM ");
            sql.append("   NAME_MST T1 ");
            sql.append("WHERE ");
            sql.append("      T1.NAMECD1 = 'Z010' ");
            sql.append("      AND T1.NAMECD2 = '00' ");
            log.debug("Z010 sql = " + sql);
            PreparedStatement ps = db2.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            String rtn = null;
            while (rs.next()) {
                rtn = rs.getString("NAME1");
                log.debug("Z010 = " + rtn);
            }
            DbUtils.closeQuietly(null, ps, rs);
            return rtn;
        }

        private boolean isHoutei() {
            if ("9".equals(_semester) || null != _knjSchoolMst._semesterDiv && _knjSchoolMst._semesterDiv.equals(_semester)) {
                return "3".equals(_knjSchoolMst._jugyouJisuFlg);
            }
            return "1".equals(_knjSchoolMst._jugyouJisuFlg) || null == _knjSchoolMst._jugyouJisuFlg;
        }
    }

    /**
     * 特別活動グループコードに含まれる特別活動科目の時数を得る
     * @param specialSubclassCountMinutesMap 特別活動の科目コードと分数のマップ
     * @param specialSubclassGroupCd 特別活動グループコード
     * @return 特別活動グループコードに含まれる特別活動科目の時数
     */
    private static BigDecimal getSpecialSubclassGroupHour(final Map specialSubclassCountMinutesMap, final String specialSubclassGroupCd, final Param param, final KNJSchoolMst knjSchoolMst) {
        final Map specialSubclassGroup = param.getSpecialSubclassGroup(specialSubclassGroupCd);
        if (specialSubclassGroup == null) {
            return new BigDecimal(0);
        }

        int totaMinutes = 0;

        for (final Iterator it = specialSubclassCountMinutesMap.keySet().iterator(); it.hasNext();) {
            final String subclassCd = (String) it.next();
            final SpecialSubclass ss = (SpecialSubclass) specialSubclassGroup.get(subclassCd);
            if (null == ss) {
                continue;
            }
            Integer minutes = (Integer) specialSubclassCountMinutesMap.get(subclassCd);
            totaMinutes += minutes.intValue();
        }
        final BigDecimal hour = getSpecialAttendExe(totaMinutes, param, knjSchoolMst);
        //log.debug(" 特別活動欠席" + specialSubclassGroupCd + "=" + totaMinutes + "分 (" + hour + "時数)");
        return hour;
    }

    /**
     * 特別活動グループコードに含まれる特別活動科目の時数(単位:時間)を得る
     * @param specialSubclassCountMap 特別活動の科目コードと時数(単位:時間)のマップ
     * @param specialSubclassGroupCd 特別活動グループコード
     * @return 特別活動グループコードに含まれる特別活動科目の時数(単位:時間)
     */
    private static BigDecimal getSpecialSubclassGroupJisu(final Map specialSubclassMinutesMap, final String specialSubclassGroupCd, final Param param, final KNJSchoolMst knjSchoolMst) {
        final Map specialSubclassGroup = param.getSpecialSubclassGroup(specialSubclassGroupCd);
        if (specialSubclassGroup == null) {
            return new BigDecimal(0);
        }

        int minutes = 0;

        for (final Iterator it = specialSubclassMinutesMap.keySet().iterator(); it.hasNext();) {
            final String subclassCd = (String) it.next();
            final Integer subclassMinutes = (Integer) specialSubclassMinutesMap.get(subclassCd);
            final SpecialSubclass ss = (SpecialSubclass) specialSubclassGroup.get(subclassCd);
            if (null == ss) {
                continue;
            }
            //log.debug("  + " + ss._minutes + " (" + ss + ")");
            minutes += subclassMinutes.intValue();
        }
        final BigDecimal hour = getSpecialAttendExe(minutes, param, knjSchoolMst);
        //log.debug(" 特別活動時数" + specialSubclassGroupCd + "=" + minutes + "分  時数=" + hour + ")");
        return hour;
    }

    /**
     * 欠課時分を欠課時数に換算した値を得る
     * @param kekka 欠課時分
     * @return 欠課時分を欠課時数に換算した値
     */
    private static BigDecimal getSpecialAttendExe(final int kekka, final Param param, final KNJSchoolMst knjSchoolMst) {
        final int jituJifun = (knjSchoolMst._jituJifunSpecial == null) ? 50 : Integer.parseInt(knjSchoolMst._jituJifunSpecial);
        final BigDecimal bigD = new BigDecimal(kekka).divide(new BigDecimal(jituJifun), 10, BigDecimal.ROUND_DOWN);
        int hasu = 0;
        final String retSt = bigD.toString();
        final int retIndex = retSt.indexOf(".");
        if (retIndex > 0) {
            hasu = Integer.parseInt(retSt.substring(retIndex + 1, retIndex + 2));
        }
        final BigDecimal rtn;
        if ("1".equals(knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：二捨三入 (五捨六入)
            rtn = bigD.setScale(0, hasu < 6 ? BigDecimal.ROUND_FLOOR : BigDecimal.ROUND_CEILING); // hasu < 6 ? 0 : 1;
        } else if ("2".equals(knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：四捨五入
            rtn = bigD.setScale(0, BigDecimal.ROUND_UP);
        } else if ("3".equals(knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：切り上げ
            rtn = bigD.setScale(0, BigDecimal.ROUND_CEILING);
        } else if ("4".equals(knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：切り下げ
            rtn = bigD.setScale(0, BigDecimal.ROUND_FLOOR);
        } else if ("0".equals(knjSchoolMst._tokubetuKatudoKansan)) { // 換算無し
            rtn = bigD;
        } else {
            rtn = bigD.setScale(0, hasu < 6 ? BigDecimal.ROUND_FLOOR : BigDecimal.ROUND_CEILING); // hasu < 6 ? 0 : 1;
        }
        return rtn;
    }

    private static String getAttendanceRemarkSql(final Param param, final String attendSemesInState) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   T1.SEMESTER, T1.MONTH, T1.SCHREGNO, T1.REMARK1 ");
        stb.append(" FROM ATTEND_SEMES_REMARK_DAT T1 ");
        stb.append(" INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.SEMESTER = '" + param._semester + "' ");
        stb.append("     AND T2.GRADE = ? ");
        stb.append("     AND T2.HR_CLASS = ? ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + param._year + "' ");
        stb.append("   AND T1.SEMESTER || T1.MONTH IN " + attendSemesInState);
        stb.append("   AND T1.REMARK1 IS NOT NULL ");
        stb.append(" ORDER BY ");
        stb.append("   T1.SEMESTER, T1.MONTH, T1.SCHREGNO ");
        return stb.toString();
    }

    private static class Student {
        final String _schregno;
        final String _attendNo;
        final String _courseCd;
        final String _majorCd;
        final String _coursecode;
        final String _name;
        final String _nameEng;
        final String _sex;
        final String _sexname;
        final String _grade;
        public AttendanceCount _attend;
        public double _compAbsenceHighSpecial999;
        public StringBuffer _remark1;

        /**
         * studentList から学籍番号が schregno の生徒を取得
         * @param schregno 生徒の学籍番号
         * @param studentList 生徒のリスト
         * @return 対象の生徒
         */
        public static Student getStudent(final String schregno, final List studentList) {
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (student._schregno.equals(schregno)) {
                    return student;
                }
            }
            return null;
        }

        public Student(
                final String schregno,
                final String attendNo,
                final String courseCd,
                final String majorCd,
                final String name,
                final String nameEng,
                final String sex,
                final String sexname,
                final String grade,
                final String coursecode) {
            _schregno = schregno;
            _attendNo = attendNo;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _coursecode = coursecode;
            _name = name;
            _nameEng = nameEng;
            _sex = sex;
            _sexname = sexname;
            _grade = grade;
        }

        public int getLessonMin(final int lessonDays) {
            if (_attend == null) {
                return lessonDays;
            }
            return Math.min(lessonDays, _attend._lessonDays);
        }
        public int getLessonMax(final int lessonDays) {
            if (_attend == null) {
                return lessonDays;
            }
            return Math.max(lessonDays, _attend._lessonDays);
        }

        public String toString() {
            DecimalFormat df3 = new DecimalFormat("00");
            String attendNo = df3.format(Integer.valueOf(_attendNo).intValue());
            String space = "";
            for (int i=_name.length(); i<7; i++) {
                space += "  ";
            }
            String name = _name + space;
            return attendNo + " , " + name + " , " + _attend;
        }

        private static List getStudentList(final DB2UDB db2, final Param param, final String grade, final String hrClass) throws SQLException {
            final List studentList = new ArrayList();
            final String sqlSchregRegdDat = sqlSchregRegdDat(param, grade, hrClass);
            log.debug("sql student = " + sqlSchregRegdDat);

            PreparedStatement ps = db2.prepareStatement(sqlSchregRegdDat);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                final Student st = new Student(
                        rs.getString("SCHREGNO"),
                        rs.getString("ATTENDNO"),
                        rs.getString("COURSECD"),
                        rs.getString("MAJORCD"),
                        rs.getString("NAME"),
                        rs.getString("NAME_ENG"),
                        rs.getString("SEX"),
                        rs.getString("SEX_NAME"),
                        rs.getString("GRADE"),
                        rs.getString("COURSECODE"));
                studentList.add(st);
            }
            DbUtils.closeQuietly(null, ps, rs);
            return studentList;
        }

        /** 学生を得るSQL */
        private static String sqlSchregRegdDat(final Param param, final String grade, final String hrClass) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T1.COURSECODE, ");
            stb.append("     T2.NAME, ");
            stb.append("     T2.NAME_ENG, ");
            stb.append("     T2.SEX, ");
            stb.append("     NMZ002.ABBV1 AS SEX_NAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1");
            stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = T2.SEX ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.GRADE = '" + grade + "' ");
            stb.append("     AND T1.HR_CLASS = '" + hrClass + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.ATTENDNO ");
            return stb.toString();
        }
    }

    /** 出欠カウント */
    private static class AttendanceCount {
        /** 授業日数 */
        private int _lessonDays;
        /** 忌引日数 */
        private int _mourningDays;
        /** 出停日数 */
        private int _suspendDays;
        /** 留学日数 */
        private int _abroadDays;
        /** 出席すべき日数 */
        private int _mlessonDays;
        /** 欠席日数 */
        private int _sickDays;
        /** 出席日数 */
        private int _attendDays;
        /** 遅刻日数 */
        private int _lateDays;
        /** 早退日数 */
        private int _earlyDays;

        /** 別室登校時数(保健室) */
        private int _nurseOff;

        /** 特活科目の科目コードと講座の時数時間(単位：分)のマップ */
        private Map _specialSubClassLessonMinutes;

        /** 特活科目の科目コードと講座の欠席コマ数のマップ */
        private Map _specialSubClassKekkaMinutes;

        /** 欠席事由 */
        String _absencestr;

        AttendanceCount() {
            _specialSubClassLessonMinutes = new HashMap();
            _specialSubClassKekkaMinutes = new HashMap();
            _absencestr = "";
        }

        /**
         * 1日単位の出欠を得る。
         * @param rs
         * @throws SQLException
         */
        private void addAttendanceDay(final ResultSet rs, final String absencestr) throws SQLException {
            _mourningDays += rs.getInt("MOURNING");
            _suspendDays  += rs.getInt("SUSPEND");
            _suspendDays += rs.getInt("VIRUS");
            _suspendDays += rs.getInt("KOUDOME");
            _abroadDays   += rs.getInt("TRANSFER_DATE");
            _mlessonDays  += rs.getInt("MLESSON");
            _sickDays     += rs.getInt("SICK");
            _lessonDays   += rs.getInt("LESSON");
            _attendDays   += rs.getInt("PRESENT"); // 出席日数 = 出席すべき日数 - 欠席日数
            _lateDays     += rs.getInt("LATE");
            _earlyDays    += rs.getInt("EARLY");
            _absencestr = absencestr;
        }

        private void addAttendanceDay(final AttendanceCount ac) {
            _mourningDays += ac._mourningDays;
            _suspendDays  += ac._suspendDays;
            _abroadDays   += ac._abroadDays;
            _mlessonDays  += ac._mlessonDays;
            _sickDays     += ac._sickDays;
            _lessonDays   += ac._lessonDays;
            _attendDays   += ac._mlessonDays - ac._sickDays; // 出席日数 = 出席すべき日数 - 欠席日数
            _lateDays     += ac._lateDays;
            _earlyDays    += ac._earlyDays;
        }

        /**
         * 科目単位の出欠を得る。
         * @param rs
         * @throws SQLException
         */
        private void addSubclassAttendance(final Param param, final ResultSet rs) throws SQLException {

            final String subclassCd = rs.getString("SUBCLASSCD");
            if (param.isSpecialSubclass(subclassCd)) {
                // 特別活動科目の処理 (授業分数と結果数の加算)
                int lessonMinutes = rs.getInt("SPECIAL_LESSON_MINUTES");
                int kekkaMinutes = 0;

                if (param._subClassC005.containsKey(subclassCd)) {
                    String is = (String) param._subClassC005.get(subclassCd);
                    if ("1".equals(is)) {
                        kekkaMinutes = rs.getInt("SPECIAL_SICK_MINUTES3");
                    } else if ("2".equals(is)) {
                        kekkaMinutes = rs.getInt("SPECIAL_SICK_MINUTES2");
                    }
                } else {
                    kekkaMinutes = rs.getInt("SPECIAL_SICK_MINUTES1");
                }

                //log.debug(name +" , " + specialSubclass._subclassCd + " , " + rs.getInt("LESSON") + " - " + rs.getInt("ABSENT2"));

                // すでにデータがあれば加算する
                if (_specialSubClassLessonMinutes.get(subclassCd) != null ){
                    lessonMinutes += ((Integer) _specialSubClassLessonMinutes.get(subclassCd)).intValue();
                }
                if (_specialSubClassKekkaMinutes.get(subclassCd) != null ){
                    kekkaMinutes += ((Integer) _specialSubClassKekkaMinutes.get(subclassCd)).intValue();
                }
                _specialSubClassLessonMinutes.put(subclassCd, new Integer(lessonMinutes));
                _specialSubClassKekkaMinutes.put(subclassCd, new Integer(kekkaMinutes));

            } else {
                // 通常科目の処理
                _nurseOff += rs.getInt("NURSEOFF"); // 保健室欠課
            }
        }

        /** 出欠の百分率を得る
         *  (除籍区分がある、もしくはデータが無い なら空白)
         */
        public String getAttendancePercentage() {
            if (_mlessonDays <= 0 || _attendDays <= 0) {
                    return "0.0";
            }
            BigDecimal denom = new BigDecimal(_mlessonDays);
            BigDecimal percentage = new BigDecimal(100.0 * (_attendDays)).divide(denom, 1, BigDecimal.ROUND_HALF_UP);
            return new DecimalFormat("0.0").format(percentage);
        }

        public String toString() {
            DecimalFormat df5 = new DecimalFormat("000");
            return
            "LESSON=" + df5.format(_lessonDays)
            + ", MOR=" + df5.format(_mourningDays)
            + ", SSP=" + df5.format(_suspendDays)
            + ", ABR=" + df5.format(_abroadDays)
            + ", MLS=" + df5.format(_mlessonDays)
            + ", SCK=" + df5.format(_sickDays)
            + ", ATE=" + df5.format(_attendDays)
            + ", LAT=" + df5.format(_lateDays )
            + ", EAR=" + df5.format(_earlyDays)
            + ", NRS=" + df5.format(_nurseOff);
        }
    }

    private static class SpecialSubclass {
        /** 特活グループコードLHR */
        static final String specialGroupCdLhr  ="001";
        /** 特活グループコード学校イベント */
        static final String specialGroupCdSchoolEvent  ="002";
        /** 特活グループコード生徒会イベント */
        static final String specialGroupCdCommittee ="003";
        /** 特活グループコードその他 */
        static final String specialGroupCdEtc  ="004";

        final String _specialSubclassGroupCd;
        final String _subclassCd;
        final int _minutes;

        SpecialSubclass(final String specialSubclassGroupCd, final String subclassCd, final int minute) {
            _specialSubclassGroupCd = specialSubclassGroupCd;
            _subclassCd = subclassCd;
            _minutes = minute;
        }

        public String toString() {
            return _subclassCd;
        }
    }

    //対象生徒の出欠備考を取得
    private String getSchregAttendRemark(final DB2UDB db2, final String schregno) {
        String rtnStr = "";
        String conect = "";
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAIN_MST AS ( ");
        stb.append("   SELECT DISTINCT ");
        stb.append("     YEAR, ");
        stb.append("     SCHOOL_KIND, ");
        stb.append("     COLLECTION_CD, ");
        stb.append("     COLLECTION_NAME ");
        stb.append("   FROM  ");
        stb.append("     ATTEND_REASON_COLLECTION_MST ");
        stb.append("   WHERE ");
        stb.append("         YEAR = '"+_param._year+"' ");
        stb.append("     AND SCHOOL_KIND = '"+_param._schoolkind+"' ");
        stb.append("     AND FROM_DATE BETWEEN '"+_param._attendStartDate+"' AND '"+_param._attendEndDate+"' ");
        if (SAKAE.equals(_param._z010) && "9".equals(_param._targetSemester)) {
            stb.append("     AND COLLECTION_CD = '99' ");
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T2.SCHREGNO, ");
        stb.append("   T1.COLLECTION_CD, ");
        stb.append("   T1.COLLECTION_NAME, ");
        stb.append("   T2.ATTEND_REMARK ");
        stb.append(" FROM ");
        stb.append("   MAIN_MST T1 ");
        stb.append("   INNER JOIN ATTEND_REASON_COLLECTION_DAT T2 ");
        stb.append("           ON T2.YEAR          = T1.YEAR ");
        stb.append("          AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
        stb.append("          AND T2.COLLECTION_CD = T1.COLLECTION_CD ");
        stb.append("          AND T2.SCHREGNO = '"+schregno+"' ");
        stb.append(" ORDER BY  ");
        stb.append("   COLLECTION_CD ");

        final String sql =  stb.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String collection = StringUtils.defaultString(rs.getString("COLLECTION_NAME"));
                final String remark = StringUtils.defaultString(rs.getString("ATTEND_REMARK"));
                if(!"".equals(remark)) {
                    final String str = (!"".equals(collection)) ? collection + "：" + remark : remark;
                    rtnStr = rtnStr + conect + str;
                    conect = ".";
                }
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnStr.toString();
    }

    private static String getSemesterName(final DB2UDB db2, final String year, final String semester) {
        final StringBuffer sql = new StringBuffer();
        sql.append(" SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' ");

        return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
    }
}
