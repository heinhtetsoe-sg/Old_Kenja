// kanji=漢字
/*
 * $Id: be56bab010c7e9ff296083a25140ec1deaa9ac8a $
 *
 */
package servletpack.KNJC;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

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
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *  学校教育システム 賢者 [出欠管理] クラス名簿（立志舎用）
 */
public class KNJC070 {

    private static final Log log = LogFactory.getLog(KNJC070.class);
    private static String FROM_TO_MARK = "\uFF5E";

    private boolean _hasData = false;

    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        final Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）

        try {
            response.setContentType("application/pdf");
            svf.VrInit();                             //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定
        } catch (IOException ex) {
            log.error("svf instancing exception! ", ex);
        }

        DB2UDB db2 = null;
        Param param = null;
        try {
            // ＤＢ接続
            try {
                db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
                db2.open();
            } catch(Exception ex) {
                log.error("db2 instancing exception! ", ex);
                return;
            }
            KNJServletUtils.debugParam(request, log);
            param = new Param(request, db2);

            // 印刷処理
            printMain(db2, param, svf);

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            if (null != db2) {
                db2.close();
            }
            // 終了処理
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }

    private void printMain(final DB2UDB db2, final Param param, final Vrw32alp svf) {
        final String form = "KNJC070.frm";

        final String loginDateString = param.getDateString(db2, param._loginDate);
        final String nendo = param.getNendoString(db2);

        final List<HomeRoom> homeRoomList = HomeRoom.getHomeRoomList(db2, param);

        for (final HomeRoom homeRoom : homeRoomList) {

            for (final String date : param._dateList) {
                if (param._isOutputDebug) {
                    log.info(" date = " + date);
                }

                final HomeRoomSchedule schedule = homeRoom._dateHomeRoomScheduleMap.get(date);
//    			if (null == schedule) {
//    				if (param._isOutputDebug) {
//    					log.info(" 0 schedules. skip.");
//    				}
//    				continue; // 時間割がない
//    			}

                final TreeSet<String> coursecdSet = new TreeSet<String>();
                final int maxStudent = 45;
                int male = 0;
                int female = 0;
                final List<Student> allStudentList = homeRoom.getStudentList(date);
                for (final Student student : allStudentList) {
                    if ("1".equals(student._sex)) {
                        male += 1;
                    } else if ("2".equals(student._sex)) {
                        female += 1;
                    }
                }
                final List<List<Student>> studentPageList = getPageList(allStudentList, maxStudent);
                for (final List<Student> studentList : studentPageList) {

                    svf.VrSetForm(form, 1);

                    svf.VrsOut("SUB_TITLE", param.getDateString(db2, date)); // サブタイトル
                    svf.VrsOut("DATE", loginDateString); // 作成日
                    svf.VrsOut("NENDO", nendo); // 年度
                    svf.VrsOut("HR_NAME", homeRoom._hrname); // 学級
                    svf.VrsOut("TR_NAME", homeRoom._staffname); // 担任名

                    for (int sti = 0; sti < studentList.size(); sti++) {
                        final int line = sti + 1;
                        final Student student = studentList.get(sti);

                        if (null != student._name) {
                            final int spaceIndex = getSpaceIndex(student._name);
                            String familyName = null;
                            String ownName = null;
                            if (0 <= spaceIndex) {
                                familyName = student._name.substring(0, spaceIndex);
                                ownName = student._name.substring(spaceIndex + 1);
                            }
                            if (spaceIndex == -1 || (KNJ_EditEdit.getMS932ByteLength(familyName) > 8 || KNJ_EditEdit.getMS932ByteLength(ownName) > 8)) {
                                svf.VrsOutn("NAME", line, student._name); // 生徒氏名
                            } else {
                                if (familyName.length() == 1) {
                                    svf.VrsOutn("LNAME_2", line, familyName); // 氏名（姓）
                                } else {
                                    svf.VrsOutn("LNAME_1", line, familyName); // 氏名（姓）
                                }
                                if (ownName.length() == 1) {
                                    svf.VrsOutn("FNAME_2", line, ownName); // 氏名（名）
                                } else {
                                    svf.VrsOutn("FNAME_1", line, ownName); // 氏名（名）
                                }
                            }
                        }

                        final int ketaKana = KNJ_EditEdit.getMS932ByteLength(student._nameKana);
                        svf.VrsOutn("KANA" + (ketaKana <= 12 ? "1" : ketaKana <= 16 ? "2" : "3"), line, student._nameKana); // ふりがな

                        svf.VrsOutn("ATTENDNO", line, NumberUtils.isDigits(student._attendNo) ? String.valueOf(Integer.parseInt(student._attendNo)) : student._attendNo); // 出席番号

                        if ("1".equals(student._sex)) {
                        } else if ("2".equals(student._sex)) {
                            svf.VrsOutn("MARK", line, "*"); // 性別区分
                        }
                        svf.VrsOutn("SCHREGNO", line, student._schregno); // 学籍番号
                        if (null != student._courseCd) {
                            coursecdSet.add(student._courseCd);
                        }
                    }

                    svf.VrsOut("MALE", String.valueOf(male)); // 男
                    svf.VrsOut("FEMALE", String.valueOf(female)); // 女
                    svf.VrsOut("TOTAL", String.valueOf(male + female)); // 合計

                    final List<Map<String, String>> periodList = param.getPeriodList(coursecdSet);
                    for (int pi = 0; pi < Math.min(periodList.size(), 10); pi++) {
                        final int line = pi + 1;
                        final Map period = periodList.get(pi);
                        final String periodcd = KnjDbUtils.getString(period, "NAMECD2");
                        svf.VrsOutn("PERIOD", line, StringUtils.defaultString(KnjDbUtils.getString(period, "ABBV1"), KnjDbUtils.getString(period, "NAME1"))); // 校時

                        if (null != schedule) {
                            final TreeMap<String, HomeRoomSchedule.Chair> chairMap = getMappedTreeMap(schedule._periodChairMap, periodcd);

                            if (!chairMap.isEmpty()) {
                                final HomeRoomSchedule.Chair chair = chairMap.get(chairMap.firstKey()); // 最小の講座コードの講座
                                if (param._isOutputDebug && chairMap.size() > 1 || param._isOutputDebugAll) {
                                    log.info(" period " + period + " has chairs " + chairMap.size());
                                    for (final Map.Entry<String, HomeRoomSchedule.Chair> e : chairMap.entrySet()) {
                                        final String clsOrderChaircd = e.getKey();
                                        final HomeRoomSchedule.Chair chair1 = e.getValue();
                                        log.info("  " + clsOrderChaircd + " = " + chair1 + " / student (size = " + chair1._gradeHrclassAttendnoSet.size() + ") " + (chair1._gradeHrclassAttendnoSet.size() < 10 ? "= " + chair1._gradeHrclassAttendnoSet.toString() : ""));
                                    }
                                }

                                if (null != chair) {
                                    svf.VrsOutn("SUBCLASS_NAME" + (StringUtils.defaultString(chair._subclassname).length() <= 3 ? "1" : "2"), line, chair._subclassname); // 科目名

                                    if (!chair._staffList.isEmpty()) {
                                        final Map<String, String> staff = chair._staffList.get(0);
                                        String staffname = KnjDbUtils.getString(staff, "STAFFNAME");
                                        if (param._isOutputDebug && chair._staffList.size() > 1 || param._isOutputDebugAll) {
                                            log.info(" period " + period + " chair " + chair._chaircd + ":" + chair._chairname + " has staffs " + chair._staffList.size());
                                            for (final Map staff1 : chair._staffList) {
                                                log.info("  staff = " + staff1);
                                            }
                                        }
                                        final int spaceIndex = getSpaceIndex(staffname);
                                        if (-1 < spaceIndex) {
                                            staffname = staffname.substring(0, spaceIndex);
                                        }
                                        svf.VrsOutn("SUBCLASS_TEACHER", line, staffname); // 科目担任 (講座担任の最小職員コード)
                                    }
                                }
                            }
                        }
                    }
                    _hasData = true;

                    svf.VrEndPage();
                }
            }
        }
    }

    private int getSpaceIndex(final String name) {
        if (null == name) {
            return -1;
        }
        int spaceIndex;
        spaceIndex = name.indexOf('　'); // 全角スペース
        if (-1 == spaceIndex) {
            spaceIndex = name.indexOf(' '); // 半角スペース
        }
        return spaceIndex;
    }

    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static <T> List<List<T>> getPageList(final List<T> list, final int max) {
        final List<List<T>> rtn = new ArrayList();
        List<T> current = null;
        for (final T o : list) {
            if (null == current || current.size() >= max) {
                current = new ArrayList<T>();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList<B>());
        }
        return map.get(key1);
    }

    private static <A, B, C> TreeMap<B, C> getMappedTreeMap(final Map<A, TreeMap<B, C>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap<B, C>());
        }
        return map.get(key1);
    }

    private static class HomeRoom {
        final String _hrname;
        final String _staffname;
        final String _hrclass;
        private List<Student> _studentList = Collections.emptyList();
        private Map<String, HomeRoomSchedule> _dateHomeRoomScheduleMap = Collections.EMPTY_MAP;
        public HomeRoom(final String hrname, final String staffname, final String hrclass) {
            _hrname = hrname;
            _staffname = staffname;
            _hrclass = hrclass;
        }

        public List<Student> getStudentList(final String date) {
            final List<Student> rtn = new ArrayList<Student>();
            for (final Student student : _studentList) {
                if ((null == student._entDate || student._entDate.compareTo(date) <= 0) && (null == student._grdDate || date.compareTo(student._grdDate) <= 0)) {
                    rtn.add(student);
                }
            }
            return rtn;
        }

        private static List<HomeRoom> getHomeRoomList(final DB2UDB db2, final Param param) {
            final List<HomeRoom> homeRoomList = new ArrayList();
            final String sql = sql(param);

            for (final Map row : KnjDbUtils.query(db2, sql)) {
                final String hrname = KnjDbUtils.getString(row, "HR_NAME");
                final String hrclass = KnjDbUtils.getString(row, "HR_CLASS");
                final String staffName = KnjDbUtils.getString(row, "STAFFNAME1");
                HomeRoom homeRoom = new HomeRoom(hrname, staffName, hrclass);
                homeRoomList.add(homeRoom);

                homeRoom._studentList = Student.getStudentList(db2, param, homeRoom._hrclass);
            }

            HomeRoomSchedule.setHomeRoomScheduleMap(db2, param, homeRoomList);

            return homeRoomList;
        }

        /** 学年クラスとクラス名称の列挙を得るSQL */
        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.HR_NAME, ");
            stb.append("     T2.STAFFNAME AS STAFFNAME1, ");
            stb.append("     T3.STAFFNAME AS STAFFNAME2, ");
            stb.append("     T4.STAFFNAME AS STAFFNAME3 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT T1 ");
            stb.append("     LEFT JOIN STAFF_MST T2 ON T1.TR_CD1 = T2.STAFFCD ");
            stb.append("     LEFT JOIN STAFF_MST T3 ON T1.TR_CD2 = T3.STAFFCD ");
            stb.append("     LEFT JOIN STAFF_MST T4 ON T1.TR_CD3 = T4.STAFFCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T1.GRADE || T1.HR_CLASS in "+ SQLUtils.whereIn(true, param._gradeHrclasses));
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, T1.HR_CLASS ");
            return stb.toString();
        }

    }

    private static class Student {
        final String _schregno;
        final String _attendNo;
        final String _courseCd;
        final String _majorCd;
        final String _coursecode;
        final String _name;
        final String _nameKana;
        final String _sex;
        final String _sexname;
        final String _grade;
        final String _entDate;
        final String _grdDate;

        public Student(
                final String schregno,
                final String attendNo,
                final String courseCd,
                final String majorCd,
                final String name,
                final String nameKana,
                final String sex,
                final String sexname,
                final String grade,
                final String coursecode,
                final String entDate,
                final String grdDate) {
            _schregno = schregno;
            _attendNo = attendNo;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _name = name;
            _nameKana = nameKana;
            _sex = sex;
            _sexname = sexname;
            _grade = grade;
            _coursecode = coursecode;
            _entDate = entDate;
            _grdDate = grdDate;
        }

        private static List getStudentList(final DB2UDB db2, final Param param, final String hrClass) {
            final List studentList = new ArrayList();
            final String sql = sql(param, hrClass);

            for (final Map row : KnjDbUtils.query(db2, sql)) {
                final Student st = new Student(
                        KnjDbUtils.getString(row, "SCHREGNO"),
                        KnjDbUtils.getString(row, "ATTENDNO"),
                        KnjDbUtils.getString(row, "COURSECD"),
                        KnjDbUtils.getString(row, "MAJORCD"),
                        KnjDbUtils.getString(row, "NAME"),
                        KnjDbUtils.getString(row, "NAME_KANA"),
                        KnjDbUtils.getString(row, "SEX"),
                        KnjDbUtils.getString(row, "SEX_NAME"),
                        KnjDbUtils.getString(row, "GRADE"),
                        KnjDbUtils.getString(row, "COURSECODE"),
                        KnjDbUtils.getString(row, "ENT_DATE"),
                        KnjDbUtils.getString(row, "GRD_DATE"));
                studentList.add(st);
            }
            return studentList;
        }

        /** 学生を得るSQL */
        private static String sql(final Param param, final String hrClass) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T1.COURSECODE, ");
            stb.append("     T2.NAME, ");
            stb.append("     T2.NAME_KANA, ");
            stb.append("     T2.SEX, ");
            stb.append("     NMZ002.ABBV1 AS SEX_NAME, ");
            stb.append("     ENTGRD.ENT_DATE, ");
            stb.append("     ENTGRD.GRD_DATE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1");
            stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = T2.SEX ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON T1.GRADE = GDAT.GRADE ");
            stb.append("         AND T1.YEAR = GDAT.YEAR ");
            stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND ENTGRD.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T1.HR_CLASS = '" + hrClass + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.ATTENDNO ");
            return stb.toString();
        }
    }

    private static class HomeRoomSchedule {
        final String _executedate;
        final Map<String, TreeMap<String, HomeRoomSchedule.Chair>> _periodChairMap = new HashMap();

        HomeRoomSchedule(final String executedate) {
            _executedate = executedate;
        }

        private static class Chair {
            final String _subclasscd;
            final String _subclassname;
            final String _subclassabbv;
            final String _chaircd;
            final String _chairname;
            final String _clsOrder;
            final List<Map<String, String>> _staffList = new ArrayList();
            final TreeSet _gradeHrclassAttendnoSet;

            Chair(
                    final String periodcd,
                    final String subclasscd,
                    final String subclassname,
                    final String subclassabbv,
                    final String chaircd,
                    final String chairname,
                    final String clsOrder
                    ) {
                _subclasscd = subclasscd;
                _subclassname = subclassname;
                _subclassabbv = subclassabbv;
                _chaircd = chaircd;
                _chairname = chairname;
                _clsOrder = clsOrder;
                _gradeHrclassAttendnoSet = new TreeSet();
            }

            public String toString() {
                return "Chair(" + _subclasscd + ", " + _chaircd + ":" + _chairname +", clsOrder = " + _clsOrder + ")";
            }
        }

        public static void setHomeRoomScheduleMap(final DB2UDB db2, final Param param, final List<HomeRoom> homeRoomList) {
            final String sql = sql(param);
            if (param._isOutputDebug) {
                log.info(" sql = " + sql);
            }
            PreparedStatement ps = null;

            try {
                ps = db2.prepareStatement(sql);

                for (final HomeRoom homeRoom : homeRoomList) {

                    homeRoom._dateHomeRoomScheduleMap = new HashMap();

                    for (final Map<String, String> row : KnjDbUtils.query(db2, ps, new Object[] { param._grade, homeRoom._hrclass, param._grade, homeRoom._hrclass})) {
                        final String executedate = KnjDbUtils.getString(row, "EXECUTEDATE");
                        if (null == homeRoom._dateHomeRoomScheduleMap.get(executedate)) {
                            homeRoom._dateHomeRoomScheduleMap.put(executedate, new HomeRoomSchedule(executedate));
                        }
                        final HomeRoomSchedule sche = homeRoom._dateHomeRoomScheduleMap.get(executedate);
                        final String periodcd = KnjDbUtils.getString(row, "PERIODCD");
                        final String clsOrder = KnjDbUtils.getString(row, "CLS_ORDER");
                        final String chaircd = KnjDbUtils.getString(row, "CHAIRCD");
                        final String clsOrderChaircd = clsOrder + chaircd;
                        if (!getMappedTreeMap(sche._periodChairMap, periodcd).containsKey(clsOrderChaircd)) {
                            final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                            final String subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");
                            final String subclassabbv = KnjDbUtils.getString(row, "SUBCLASSABBV");
                            final String chairname = KnjDbUtils.getString(row, "CHAIRNAME");
                            final Chair chair = new Chair(periodcd, subclasscd, subclassname, subclassabbv, chaircd, chairname, clsOrder);
                            getMappedTreeMap(sche._periodChairMap, periodcd).put(clsOrderChaircd, chair);
                        }
                        final Chair chair = getMappedTreeMap(sche._periodChairMap, periodcd).get(clsOrderChaircd);
                        final String staffcd = KnjDbUtils.getString(row, "STAFFCD");
                        if (null != staffcd) {
                            boolean addBefore = false;
                            for (final Iterator stfit = chair._staffList.iterator(); stfit.hasNext();) {
                                final Map staff = (Map) stfit.next();
                                final String staffStaffcd = (String) staff.get("STAFFCD");
                                if (staffStaffcd.equals(staffcd)) {
                                    addBefore = true;
                                    break;
                                }
                            }
                            if (!addBefore) {
                                final Map staff = new HashMap();
                                staff.put("STAFFCD", staffcd);
                                staff.put("STAFFNAME", KnjDbUtils.getString(row, "STAFFNAME"));
                                chair._staffList.add(staff);
                            }
                        }
                        chair._gradeHrclassAttendnoSet.add(KnjDbUtils.getString(row, "GRADE_HR_CLASS_ATTENDNO_SCHREGNO"));
                    }
                }

            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        public static String sql(final Param param) {
            final String q = "?";
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     SCHED.EXECUTEDATE, ");
            stb.append("     SCHED.PERIODCD, ");
            stb.append("     CHR.CLASSCD || '-' || CHR.SCHOOL_KIND || '-' || CHR.CURRICULUM_CD || '-' || CHR.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     SUBM.SUBCLASSNAME, ");
            stb.append("     SUBM.SUBCLASSABBV, ");
            stb.append("     SCHED.CHAIRCD, ");
            stb.append("     CHR.CHAIRNAME, ");
            stb.append("     '1_CHAIR_STF' AS STAFF_KIND, ");
            stb.append("     CASE WHEN CHCLS.CHAIRCD IS NOT NULL THEN '1' ELSE '2' END AS CLS_ORDER, "); // 指定のクラスが講座クラスに設定されていれば1、それ以外は2 (TreeMapのキーに含めてソートするため)
            stb.append("     CHSTF.STAFFCD, ");
            stb.append("     STF.STAFFNAME, ");
            stb.append("     REGD.GRADE || REGD.HR_CLASS || '-' || REGD.ATTENDNO || '-' || STD.SCHREGNO AS GRADE_HR_CLASS_ATTENDNO_SCHREGNO ");
            stb.append(" FROM CHAIR_DAT CHR ");
            stb.append(" INNER JOIN CHAIR_STD_DAT STD ON STD.YEAR = CHR.YEAR ");
            stb.append("     AND STD.SEMESTER = CHR.SEMESTER ");
            stb.append("     AND STD.CHAIRCD = CHR.CHAIRCD ");
            stb.append(" LEFT JOIN CHAIR_STF_DAT CHSTF ON CHSTF.YEAR = CHR.YEAR ");
            stb.append("     AND CHSTF.SEMESTER = CHR.SEMESTER ");
            stb.append("     AND CHSTF.CHAIRCD = CHR.CHAIRCD ");
            stb.append("     AND CHSTF.CHARGEDIV = 1 ");
            stb.append(" INNER JOIN SCH_CHR_DAT SCHED ON SCHED.EXECUTEDATE BETWEEN STD.APPDATE AND STD.APPENDDATE ");
            stb.append("     AND SCHED.CHAIRCD = CHR.CHAIRCD ");
            stb.append(" INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = STD.SCHREGNO ");
            stb.append("     AND REGD.YEAR = CHR.YEAR  ");
            stb.append("     AND REGD.SEMESTER = STD.SEMESTER ");
            stb.append("     AND REGD.GRADE = " + q + " ");
            stb.append("     AND REGD.HR_CLASS = " + q + " ");
            stb.append(" INNER JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = CHR.CLASSCD ");
            stb.append("     AND SUBM.SCHOOL_KIND = CHR.SCHOOL_KIND ");
            stb.append("     AND SUBM.CURRICULUM_CD = CHR.CURRICULUM_CD ");
            stb.append("     AND SUBM.SUBCLASSCD = CHR.SUBCLASSCD ");
            stb.append(" LEFT JOIN STAFF_MST STF ON STF.STAFFCD = CHSTF.STAFFCD ");
            stb.append(" LEFT JOIN CHAIR_CLS_DAT CHCLS ON CHCLS.YEAR = CHR.YEAR ");
            stb.append("     AND CHCLS.SEMESTER = CHR.SEMESTER ");
            stb.append("     AND (CHCLS.CHAIRCD = CHR.CHAIRCD AND CHCLS.GROUPCD = '0000' ");
            stb.append("       OR CHCLS.CHAIRCD = '0000000' AND CHCLS.GROUPCD = CHR.GROUPCD) ");
            stb.append("     AND CHCLS.TRGTGRADE = REGD.GRADE ");
            stb.append("     AND CHCLS.TRGTCLASS = REGD.HR_CLASS ");
            stb.append(" WHERE ");
            stb.append(" CHR.YEAR = '" + param._year + "' ");
            stb.append(" AND CHR.SEMESTER = '" + param._semester + "' ");
            stb.append(" AND SCHED.EXECUTEDATE BETWEEN '" + param._sdate + "' AND '" + param._edate + "' ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     SCHED.EXECUTEDATE, ");
            stb.append("     SCHED.PERIODCD, ");
            stb.append("     CHR.CLASSCD || '-' || CHR.SCHOOL_KIND || '-' || CHR.CURRICULUM_CD || '-' || CHR.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     SUBM.SUBCLASSNAME, ");
            stb.append("     SUBM.SUBCLASSABBV, ");
            stb.append("     SCHED.CHAIRCD, ");
            stb.append("     CHR.CHAIRNAME, ");
            stb.append("     '0_SCH_STF' AS STAFF_KIND, ");
            stb.append("     CASE WHEN CHCLS.CHAIRCD IS NOT NULL THEN '1' ELSE '2' END AS CLS_ORDER, "); // 指定のクラスが講座クラスに設定されていれば1、それ以外は2 (TreeMapのキーに含めてソートするため)
            stb.append("     SCHSTF.STAFFCD, ");
            stb.append("     STF.STAFFNAME, ");
            stb.append("     REGD.GRADE || REGD.HR_CLASS || '-' || REGD.ATTENDNO || '-' || STD.SCHREGNO AS GRADE_HR_CLASS_ATTENDNO_SCHREGNO ");
            stb.append(" FROM CHAIR_DAT CHR ");
            stb.append(" INNER JOIN CHAIR_STD_DAT STD ON STD.YEAR = CHR.YEAR ");
            stb.append("     AND STD.SEMESTER = CHR.SEMESTER ");
            stb.append("     AND STD.CHAIRCD = CHR.CHAIRCD ");
            stb.append(" INNER JOIN SCH_CHR_DAT SCHED ON SCHED.EXECUTEDATE BETWEEN STD.APPDATE AND STD.APPENDDATE ");
            stb.append("     AND SCHED.CHAIRCD = CHR.CHAIRCD ");
            stb.append(" INNER JOIN SCH_STF_DAT SCHSTF ON SCHSTF.EXECUTEDATE = SCHED.EXECUTEDATE ");
            stb.append("     AND SCHSTF.PERIODCD = SCHED.PERIODCD ");
            stb.append("     AND SCHSTF.CHAIRCD = SCHED.CHAIRCD ");
            stb.append(" INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = STD.SCHREGNO ");
            stb.append("     AND REGD.YEAR = CHR.YEAR  ");
            stb.append("     AND REGD.SEMESTER = STD.SEMESTER ");
            stb.append("     AND REGD.GRADE = " + q + " ");
            stb.append("     AND REGD.HR_CLASS = " + q + " ");
            stb.append(" INNER JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = CHR.CLASSCD ");
            stb.append("     AND SUBM.SCHOOL_KIND = CHR.SCHOOL_KIND ");
            stb.append("     AND SUBM.CURRICULUM_CD = CHR.CURRICULUM_CD ");
            stb.append("     AND SUBM.SUBCLASSCD = CHR.SUBCLASSCD ");
            stb.append(" INNER JOIN STAFF_MST STF ON STF.STAFFCD = SCHSTF.STAFFCD ");
            stb.append(" LEFT JOIN CHAIR_CLS_DAT CHCLS ON CHCLS.YEAR = CHR.YEAR ");
            stb.append("     AND CHCLS.SEMESTER = CHR.SEMESTER ");
            stb.append("     AND (CHCLS.CHAIRCD = CHR.CHAIRCD AND CHCLS.GROUPCD = '0000' ");
            stb.append("       OR CHCLS.CHAIRCD = '0000000' AND CHCLS.GROUPCD = CHR.GROUPCD) ");
            stb.append("     AND CHCLS.TRGTGRADE = REGD.GRADE ");
            stb.append("     AND CHCLS.TRGTCLASS = REGD.HR_CLASS ");
            stb.append(" WHERE ");
            stb.append(" CHR.YEAR = '" + param._year + "' ");
            stb.append(" AND CHR.SEMESTER = '" + param._semester + "' ");
            stb.append(" AND SCHED.EXECUTEDATE BETWEEN '" + param._sdate + "' AND '" + param._edate + "' ");
            stb.append(" ORDER BY ");
            stb.append("    EXECUTEDATE, ");
            stb.append("    PERIODCD, ");
            stb.append("    SUBCLASSCD, ");
            stb.append("    SUBCLASSNAME, ");
            stb.append("    SUBCLASSABBV, ");
            stb.append("    STAFF_KIND, ");
            stb.append("    CHAIRCD, ");
            stb.append("    STAFFCD ");
            return stb.toString();
        }
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _sdate;
        final String _edate;
        final String _loginDate;
        final String _grade;
        final String[] _gradeHrclasses;
        final List<String> _dateList;
        final Map<String, List<Map<String, String>>> _coursecdPeriodListMap;

        private boolean _isSeireki;
        private KNJSchoolMst _knjSchoolMst;

        final String _useSchool_KindField;
        final String SCHOOLCD;
        final String SCHOOLKIND;
        final boolean _isOutputDebug;
        final boolean _isOutputDebugAll;
        final Map _knjschoolMstMap = new HashMap();

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("CTRL_YEAR");
            _semester  = request.getParameter("CTRL_SEMESTER");
            _sdate = request.getParameter("SDATE").replace('/', '-');
            _edate = request.getParameter("EDATE").replace('/', '-');
            _loginDate = request.getParameter("CTRL_DATE").replace('/', '-');
            _dateList = getDateList(_sdate, _edate);
            _coursecdPeriodListMap = getCoursecdPeriodListMap(db2);

            _grade = request.getParameter("GRADE");
            _gradeHrclasses = request.getParameterValues("CATEGORY_SELECTED");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            SCHOOLCD = request.getParameter("SCHOOLCD");
            SCHOOLKIND = request.getParameter("SCHOOLKIND");
            final String outputDebug = getDbPrginfoProperties(db2, "outputDebug");
            _isOutputDebugAll = "all".equals(outputDebug);
            _isOutputDebug = _isOutputDebugAll || "1".equals(outputDebug);

            try {
                _isSeireki = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'Z012' AND NAMECD2 = '00' ")));

                try {
                    final Map paramMap = new TreeMap();
                    if ("1".equals(_useSchool_KindField)) {
                        paramMap.put("SCHOOL_KIND", SCHOOLKIND);
                        paramMap.put("SCHOOLCD", SCHOOLCD);
                    }
                    final Integer cacheKey = new Integer(paramMap.hashCode());
                    if (null == _knjschoolMstMap.get(cacheKey)) {
                        _knjschoolMstMap.put(cacheKey, new KNJSchoolMst(db2, _year, paramMap));
                    }
                    _knjSchoolMst = (KNJSchoolMst) _knjschoolMstMap.get(cacheKey);
                } catch (Throwable e) {
                    log.fatal("exception!", e);
                }

            } catch (Exception ex) {
                log.error("Param load exception!", ex);
            }
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJC070' AND NAME = '" + propName + "' "));
        }

        private List<Map<String, String>> getPeriodList(final TreeSet<String> coursecdSet) {
            if (!coursecdSet.isEmpty()) {
                return getMappedList(_coursecdPeriodListMap, coursecdSet.first());
            }
            return getMappedList(_coursecdPeriodListMap, "NO_COURSE");
        }

        private Map<String, List<Map<String, String>>> getCoursecdPeriodListMap(final DB2UDB db2) {
            final Map<String, List<Map<String, String>>> coursecdPeriodcdListMap = new HashMap();

            final StringBuffer sql = new StringBuffer();
            sql.append("SELECT ");
            sql.append("  T1.COURSECD, ");
            sql.append("  T2.NAMECD2, ");
            sql.append("  T2.NAME1, ");
            sql.append("  T2.ABBV1 ");
            sql.append(" FROM COURSE_MST T1 ");
            sql.append("  INNER JOIN V_NAME_MST T2 ON T2.NAMECD2 BETWEEN S_PERIODCD AND E_PERIODCD ");
            sql.append(" WHERE T2.YEAR = '" + _year + "' ");
            sql.append("       AND T2.NAMECD1 = 'B001' ");
            sql.append(" UNION ALL ");
            sql.append("SELECT ");
            sql.append("  'NO_COURSE' AS COURSECD, "); // コースなし
            sql.append("  T2.NAMECD2, ");
            sql.append("  T2.NAME1, ");
            sql.append("  T2.ABBV1 ");
            sql.append(" FROM V_NAME_MST T2 ");
            sql.append(" WHERE T2.YEAR = '" + _year + "' ");
            sql.append("       AND T2.NAMECD1 = 'B001' ");
            sql.append(" ORDER BY COURSECD ");
            sql.append("        , NAMECD2 ");

            for (final Map row : KnjDbUtils.query(db2, sql.toString())) {
                final Map period = new HashMap();
                period.put("NAMECD2", KnjDbUtils.getString(row, "NAMECD2"));
                period.put("NAME1", KnjDbUtils.getString(row, "NAME1"));
                period.put("ABBV1", KnjDbUtils.getString(row, "ABBV1"));
                getMappedList(coursecdPeriodcdListMap, KnjDbUtils.getString(row, "COURSECD")).add(period);
            }

            return coursecdPeriodcdListMap;
        }

        private static List<String> getDateList(final String sdate, final String edate) {
            final List list = new ArrayList();
            final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            final Calendar scal = Calendar.getInstance();
            scal.setTime(java.sql.Date.valueOf(sdate));
            final Calendar ecal = Calendar.getInstance();
            ecal.setTime(java.sql.Date.valueOf(edate));
            while (scal.before(ecal) || scal.equals(ecal)) {
                list.add(df.format(scal.getTime()));
                scal.add(Calendar.DAY_OF_MONTH, 1);
            }
            return list;
        }

//		// 印刷日
//        private String getPrintDateStr() {
//            Calendar cal = Calendar.getInstance();
//            int hour = cal.get(Calendar.HOUR_OF_DAY);
//            int minute = cal.get(Calendar.MINUTE);
//            DecimalFormat df = new DecimalFormat("00");
//            String time = df.format(hour) + ":" + df.format(minute);
//
//            return getDateString(_loginDate) + "（" + KNJ_EditDate.h_format_W(_loginDate) +"）" + time;
//        }

        private String getNendoString(final DB2UDB db2) {
            if (_isSeireki) {
                return _year + "年度";
            }
            return KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_year)) + "年度";
        }

        private String getDateString(final DB2UDB db2, final String date) {
            if (_isSeireki) {
                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
            }
            return KNJ_EditDate.h_format_JP(db2, date); // デフォルトは和暦
        }
    }
}
