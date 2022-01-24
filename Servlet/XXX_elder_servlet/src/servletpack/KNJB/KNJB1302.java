/*
 * $Id: 80524f8e619e3e184315117ffe711bc9594db30f $
 *
 * 作成日: 2009/10/17
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJB;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
/**
 *  学校教育システム 賢者 [時間割管理]
 *
 *                  ＜ＫＮＪＢ１０２＞  生徒別選択科目一覧
 */
public class KNJB1302 {

    private static final Log log = LogFactory.getLog(KNJB1302.class);

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
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final List homerooms = getHomeRoomList(db2);

        // ホームルームごと出力
        for (final Iterator it = homerooms.iterator(); it.hasNext();) {
            final HomeRoom hr = (HomeRoom) it.next();
            if ("2".equals(_param._formSelect)) {
                printHomeRoomKotei(svf, hr);
            } else {
                printHomeRoomKahen(svf, hr);
            }
        }
    }

    private String getString(final String name, final int length) {
        if (null == name || name.length() <= length) {
            return name;
        }
        return name.substring(0, length);
    }

    private void printHomeRoomKotei(final Vrw32alp svf, final HomeRoom hr) {

        final Map subclassMap = new TreeMap();
        for (final Iterator its = hr._students.iterator(); its.hasNext();) {
            final Student student = (Student) its.next();

            for (final Iterator itsub = student._subclassList.iterator(); itsub.hasNext();) {
                final Subclass subclass = (Subclass) itsub.next();

                if (null == subclassMap.get(subclass._subclasscd)) {
                    subclassMap.put(subclass._subclasscd, new Subclass(subclass._subclasscd, null, subclass._classname, subclass._subclassname, subclass._subclassabbv, false, null));
                }
                if (null != subclass._credits) {
                    final Subclass s = (Subclass) subclassMap.get(subclass._subclasscd);
                    s._creditSet.add(subclass._credits);
                }
            }
        }

        final int MAX_STUDENTS = 40;
        final int MAX_SUBCLASS = 44;
        final List subclassPageList = getPageList(new ArrayList(subclassMap.values()), MAX_SUBCLASS);
        final List studentPageList = getPageList(hr._students, MAX_STUDENTS);

        for (int stpi = 0; stpi < studentPageList.size(); stpi++) {

            final List studentList = (List) studentPageList.get(stpi);

            for (int sbpi = 0; sbpi < subclassPageList.size(); sbpi++) {
                final List subclassList = (List) subclassPageList.get(sbpi);
                final int page = stpi * subclassPageList.size() + sbpi + 1;

                svf.VrSetForm("KNJB1302_2.frm", 4);

                svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度"); // 年度
                svf.VrsOut("TITLE", "履修登録一覧"); // タイトル
                svf.VrsOut("SELECT", _param.TABLE_IS_SUBCLASS_STD_DAT.equals(_param._selectTable) ? "履修登録名簿" : "講座名簿");
                //svf.VrsOut("TIME", null); // 時間
                svf.VrsOut("COURSE_NAME", hr._hrName);
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); // 作成日
                svf.VrsOut("PAGE", String.valueOf(page)); // ページ

                for (int sti = 0; sti < studentList.size(); sti++) {
                    final Student student = (Student) studentList.get(sti);
                    final int line = sti + 1;
                    svf.VrsOutn("ATTENDNO", line, NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : student._attendno); // 番号
                    svf.VrsOutn("SCHREGNO", line, student._schregno); // 学籍番号
                    svf.VrsOutn("NAME_SHOW", line, student._name); // 氏名
                }

                for (int sbi = 0; sbi < subclassList.size(); sbi++) {
                    final Subclass subclass = (Subclass) subclassList.get(sbi);

                    svf.VrsOut("course1", getString(subclass._classname, 7)); // 教科
                    if (StringUtils.defaultString(subclass._subclassname).length() > 10) {
                        svf.VrsOut("subject2", getString(subclass._subclassname, 15)); // 科目
                    } else {
                        svf.VrsOut("subject1", subclass._subclassname); // 科目
                    }
                    if (!subclass._creditSet.isEmpty()) {
                        svf.VrsOut("CREDIT", String.valueOf(subclass._creditSet.last())); // 単位
                    }
                    //svf.VrsOut("GROUP", null); // 群

                    int count = 0;
                    for (int sti = 0; sti < studentList.size(); sti++) {
                        final Student student = (Student) studentList.get(sti);
                        final int line = sti + 1;
                        if (null != Subclass.getSubclass(student._subclassList, subclass._subclasscd)) {
                            svf.VrsOut("TAKE" + String.valueOf(line), "○"); // 履修1
                            count += 1;
                        }
                    }
                    svf.VrsOut("TAKE_NUM" + (String.valueOf(count).length() <= 2 ? "1" : "2"), String.valueOf(count)); // 履修者数

                    svf.VrEndRecord();
                }

                for (int sbi = subclassList.size(); sbi < MAX_SUBCLASS; sbi++) {
                    svf.VrsOut("DUMMY", "1"); // 空行
                    svf.VrEndRecord();
                }

                svf.VrsOut("CREDIT_NAME", "履修登録科目数"); // 履修登録単位数表示名
                if (subclassPageList.size() - 1 == sbpi) {
                    for (int sti = 0; sti < studentList.size(); sti++) {
                        final Student student = (Student) studentList.get(sti);
                        final int line = sti + 1;
                        svf.VrsOut("CREDIT" + line, String.valueOf(student._subclassList.size())); // 科目数
                    }
                }
                svf.VrEndRecord();
                svf.VrsOut("CREDIT_NAME", "履修登録単位数"); // 履修登録単位数表示名
                if (subclassPageList.size() - 1 == sbpi) {
                    for (int sti = 0; sti < studentList.size(); sti++) {
                        final Student student = (Student) studentList.get(sti);
                        final int line = sti + 1;
                        svf.VrsOut("CREDIT" + line, String.valueOf(student._totalCredit)); // 単位
                    }
                }
                svf.VrEndRecord();
                _hasData = true;
            }
        }
    }

    private List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }


    private void printHomeRoomKahen(final Vrw32alp svf, final HomeRoom hr) {
        final int MAX_STUDENTS = 40;
        final int MAX_SUBCLASS = 12;
        svf.VrSetForm("KNJB1302.frm", 4);

        svf.VrsOut("COURSE_NAME", StringUtils.defaultString(hr._majorName) + "　" + StringUtils.defaultString(hr._courseCodeName) + "　　" + StringUtils.defaultString(hr._hrName) + "　　担任：" + StringUtils.defaultString(hr._tr1Name));

        int cline = 1;
        final Map displayLists = new HashMap();
        final Map studentMap = new HashMap();
        final Map studentDisplayListMap = new HashMap();

        for (final Iterator its = hr._students.iterator(); its.hasNext();) {
            final Student student = (Student) its.next();

            final List studentDisplayLists = Student.setDisplayList(student, _param, MAX_SUBCLASS);
            studentDisplayListMap.put(student._schregno, studentDisplayLists);

            studentMap.put(new Integer(cline), student);

            boolean hasSubclass = false;
            for (final Iterator itsub = studentDisplayLists.iterator(); itsub.hasNext();) {
                final Map subclassMap = (Map) itsub.next();
                displayLists.put(new Integer(cline), subclassMap);
                hasSubclass = true;
                cline += 1;
            }
            if (!hasSubclass) {
                cline += 1; // 科目が無くても1行プラス。
            }
        }
        final int totalline = cline - 1;
        final int studentPage = totalline / MAX_STUDENTS + (totalline % MAX_STUDENTS == 0 ? 0 : 1); // ページ数
        final int groupPage;
        if ("1".equals(_param._kijun1)) {
            groupPage = page(_param.getSubclassCourseCompSelectDataMap(hr.getCourseKey()).values(), MAX_SUBCLASS);
        } else if ("1".equals(_param._kijun2)) {
            groupPage = page(hr._gunData.values(), MAX_SUBCLASS);
        } else {
            groupPage = 1;
        }

        for (int sp = 0; sp < studentPage; sp++) {

            svf.VrsOut("NENDO", KNJ_EditDate.h_format_JP_N(_param._year + "-01-01") + "度");
            svf.VrsOut("TITLE", "生徒別選択科目群一覧");
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));
            svf.VrsOut("GRADE", _param._gradeName);
            svf.VrsOut("TIME", _param._loginHour + "時" + _param._loginMinutes + "分");
            svf.VrsOut("STAFFNAME_SHOW", _param._loginStaffName);
            svf.VrsOut("SELECT", _param.TABLE_IS_SUBCLASS_STD_DAT.equals(_param._selectTable) ? "履修登録名簿" : "講座名簿");
            svf.VrsOut("SCHOOLNAME", _param._schoolName);

            for (int gp = 0; gp < groupPage; gp++) {
                /** 生徒の情報を出力 */
                for (int line = 1; line <= MAX_STUDENTS; line++) {
                    Integer keyLine = new Integer(sp * MAX_STUDENTS + line);

                    // クリア処理
                    svf.VrsOutn("ATTENDNO", line, "");
                    svf.VrsOutn("SCHREGNO", line, "");
                    svf.VrsOutn("NAME_SHOW", line, "");
                    svf.VrsOutn("SEX", line, "");
                    svf.VrsOutn("TOTAL_CREDIT", line, "");

                    final Student student = (Student) studentMap.get(keyLine);
                    if (student == null) {
                        continue;
                    }
                    log.debug("student => " + line + " , " + student._attendno + " , " + student._name + " : " + studentDisplayListMap.get(student._schregno));

                    svf.VrsOutn("HR_NAME", line, hr._hrName);
                    svf.VrsOutn("ATTENDNO", line, NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : StringUtils.defaultString(student._attendno));
                    svf.VrsOutn("SCHREGNO", line, student._schregno);
                    svf.VrsOutn("NAME_SHOW", line, student._name);
                    svf.VrsOutn("SEX", line, student._sex);
                    svf.VrsOutn("TOTAL_CREDIT", line, String.valueOf(student._totalCredit));
                }

                /** 科目名と単位を出力 */
                boolean isendrecord = false;
                for (int column = gp * MAX_SUBCLASS + 1; column <= (gp + 1) * MAX_SUBCLASS; column++) {
                    final String groupName = getGroupName(hr, column);
                    svf.VrsOut("GROUPNAME1", groupName);
                    for (int line = 1; line <= MAX_STUDENTS; line++) {
                        final Integer keyLine = new Integer(sp * MAX_STUDENTS + line);

                        final Map map = (Map) displayLists.get(keyLine);
                        if (map == null || map.isEmpty()) {
                            continue;
                        }

                        final Subclass subclass = (Subclass) map.get(new Integer(column));
                        if (subclass == null) {
                            continue;
                        }

                        if (!"1".equals(_param._kijun2) && subclass._isChohuku) {  // 科目が重複している場合に網掛け表示する。
                            log.debug("重複科目 = " + studentMap.get(keyLine) + " , " + subclass._subclassabbv + " ("+ line + "," + column + " )");
                            svf.VrAttribute("SEL_SUB" + line, "Paint=(1,50,1),Bold=1");
                        }

                        svf.VrsOut("SEL_SUB" + line, subclass._subclassabbv);
                        svf.VrsOut("CREDIT" + line, subclass._credits);
                    }
                    svf.VrEndRecord();
                    isendrecord = true;
                    _hasData = true;
                }
                if (!isendrecord) {
                    for (int i = 0; i < MAX_SUBCLASS; i++) {
                        svf.VrsOut("SEL_SUB1", "");
                        svf.VrEndRecord();
                    }
                }
            }

        } // ページごと出力
    }

    private String getGroupName(final HomeRoom hr, int column) {
        final String groupName;
        if ("1".equals(_param._kijun1)) {
            groupName = _param.getSubclassCompSelectDatName(hr.getCourseKey(), column);
        } else if ("1".equals(_param._kijun2)) {
            groupName = HomeRoom.getGunName(hr, column);
        } else {
            groupName = "　選択　科目　";
        }
        return groupName;
    }

    /**
     * 出力する情報を取得したホームルームのリストを得る
     * @param db2
     * @return
     */
    private List getHomeRoomList(final DB2UDB db2) {

        PreparedStatement ps = null;
        ResultSet rs = null;
        final List homerooms = new ArrayList();

        try {
            final String sql = sqlSubclassSelect(_param);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {

                final String coursecd = rs.getString("COURSECD");
                final String majorcd = rs.getString("MAJORCD");
                final String coursecode = rs.getString("COURSECODE");
                HomeRoom hr = HomeRoom.getHomeRoom(homerooms, rs.getString("HR_CLASS"), coursecd, majorcd, coursecode);
                if (hr == null) {
                    hr = new HomeRoom(rs.getString("HR_CLASS"), rs.getString("HR_NAME"), rs.getString("TR1_NAME"), db2, coursecd, majorcd, coursecode, rs.getString("MAJORNAME"), rs.getString("COURSECODENAME"));
                    if ("1".equals(_param._kijun2)) {
                        hr._gunData = HomeRoom.getGunData(db2, hr, _param);
                    }
                    homerooms.add(hr);
                }

                final String schregno = rs.getString("SCHREGNO");
                Student student = Student.getStudent(schregno, hr._students);
                if (student == null) {
                    student = new Student(schregno, rs.getString("ATTENDNO"), rs.getString("NAME"), rs.getString("SEX"), hr);
                    hr._students.add(student);
                }

                Subclass subclass = null;
                if ("1".equals(_param._kijun2)) {
                    subclass = Subclass.getSubclass(student._subclassList, rs.getString("SUBCLASSCD"));
                }

                if (subclass == null) {
                    final String groupCd = "1".equals(_param._kijun1) && _param.TABLE_IS_SUBCLASS_STD_DAT.equals(_param._selectTable) ? rs.getString("GROUPCD") : null;
                    final String classname = rs.getString("CLASSNAME");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String subclassabbv = rs.getString("SUBCLASSABBV");
                    final boolean isChohuku = "1".equals(rs.getString("CHOHUKU"));
                    final String credits = rs.getString("CREDITS");
                    subclass = new Subclass(rs.getString("SUBCLASSCD"), groupCd, classname, subclassname, subclassabbv, isChohuku, credits);
                    student._subclassList.add(subclass);
                    student._totalCredit += null != credits ? Integer.parseInt(credits) : 0;
                }

                if ("1".equals(_param._kijun2)) {
                    subclass._chairCds.add(rs.getString("CHAIRCD"));
                }
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return homerooms;
    }

    /** 生徒別科目一覧 取得SQL */
    private static String sqlSubclassSelect(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SUBCLASS_SELECT AS ( ");
        if (param.TABLE_IS_SUBCLASS_STD_DAT.equals(param._selectTable)) {
            // 科目履修名簿
            stb.append(" SELECT ");
            stb.append("     T1.YEAR ");
            stb.append("     ,T1.SCHREGNO ");
            stb.append("     ,T1.GROUPCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     ,T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD ");
            }
            stb.append("     ,T1.SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("     SUBCLASS_STD_SELECT_RIREKI_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.RIREKI_CODE = '" + param._rirekiCode + "' ");
            if ("1".equals(param._use_prg_schoolkind)) {
                if (!StringUtils.isBlank(param._selectSchoolKind)) {
                    stb.append("   AND T1.SCHOOL_KIND IN (" + param._selectSchoolKindIn + ") ");
                }
            } else if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOL_KIND) && !StringUtils.isBlank(param._SCHOOLCD)) {
                stb.append("   AND T1.SCHOOL_KIND = '" + param._SCHOOL_KIND + "' ");
            }
        } else if (param.TABLE_IS_CHAIR_STD_DAT.equals(param._selectTable)) {
            stb.append(" SELECT DISTINCT ");
            stb.append("     T0.YEAR ");
            stb.append("     ,T0.SCHREGNO ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     ,T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD ");
            }
            stb.append("     ,T1.SUBCLASSCD ");
            if ("1".equals(param._kijun2)) {
                // 群設定された講座名簿
                stb.append("     ,T1.GROUPCD ");
                stb.append("     ,T1.CHAIRCD ");
            } else {
                stb.append("     ,CAST(NULL AS VARCHAR(1)) AS GROUPCD ");
            }
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT T0 ");
            stb.append("     INNER JOIN CHAIR_DAT T1 ON ");
            stb.append("         T0.YEAR = T1.YEAR ");
            stb.append("         AND T0.SEMESTER = T1.SEMESTER ");
            stb.append("         AND T0.CHAIRCD = T1.CHAIRCD ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON ");
            stb.append("         T1.YEAR = T2.YEAR ");
            stb.append("         AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("         AND T0.SCHREGNO = T2.SCHREGNO ");
            if ("1".equals(param._use_prg_schoolkind)) {
                if (!StringUtils.isBlank(param._selectSchoolKind)) {
                    stb.append("   INNER JOIN SCHREG_REGD_GDAT T5 ON T5.YEAR = T2.YEAR AND T5.GRADE = T2.GRADE AND T5.SCHOOL_KIND IN (" + param._selectSchoolKindIn + ") ");
                }
            } else if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOL_KIND) && !StringUtils.isBlank(param._SCHOOLCD)) {
                stb.append("   INNER JOIN SCHREG_REGD_GDAT T5 ON T5.YEAR = T2.YEAR AND T5.GRADE = T2.GRADE AND T5.SCHOOL_KIND = '" + param._SCHOOL_KIND + "' ");
            }
            if ("1".equals(param._kijun2)) {
                // 群設定された講座名簿
                stb.append("     INNER JOIN V_ELECTCLASS_MST T3 ON ");
                stb.append("         T3.YEAR = T1.YEAR ");
                stb.append("         AND T3.GROUPCD = T1.GROUPCD ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND '" + param._date + "' BETWEEN T0.APPDATE AND T0.APPENDDATE ");
            if ("1".equals(param._use_prg_schoolkind)) {
                if (!StringUtils.isBlank(param._selectSchoolKind)) {
                    stb.append("   AND T1.SCHOOL_KIND IN (" + param._selectSchoolKindIn + ") ");
                }
            } else if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOL_KIND) && !StringUtils.isBlank(param._SCHOOLCD)) {
                stb.append("   AND T1.SCHOOL_KIND = '" + param._SCHOOL_KIND + "' ");
            }
        }
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T2.GRADE ");
        stb.append("     ,T2.HR_CLASS ");
        stb.append("     ,T6.HR_NAME ");
        stb.append("     ,T2.ATTENDNO ");
        stb.append("     ,T2.SCHREGNO ");
        stb.append("     ,T2.COURSECD ");
        stb.append("     ,T2.MAJORCD ");
        stb.append("     ,CM_M.MAJORNAME ");
        stb.append("     ,T2.COURSECODE ");
        stb.append("     ,COURSE_M.COURSECODENAME ");
        stb.append("     ,T3.NAME ");
        stb.append("     ,T5.NAME2 AS SEX ");
        stb.append("     ,T1.GROUPCD ");
        stb.append("     , ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append("      T1.SUBCLASSCD AS SUBCLASSCD ");
        stb.append("     ,T5.CLASSNAME ");
        stb.append("     ,T4.SUBCLASSNAME ");
        stb.append("     ,T4.SUBCLASSABBV  ");
        if (param.TABLE_IS_CHAIR_STD_DAT.equals(param._selectTable) && "1".equals(param._kijun2)) {
            stb.append("     ,T1.CHAIRCD ");
        }
        stb.append("     ,CM.CREDITS ");
        stb.append("     ,CASE WHEN SS.SUBCLASSCD IS NOT NULL THEN '1' END AS CHOHUKU ");
        stb.append("     ,VALUE(T7.STAFFNAME, '') AS TR1_NAME ");
        stb.append(" FROM SCHREG_REGD_DAT T2 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T3 ON ");
        stb.append("         T3.SCHREGNO = T2.SCHREGNO ");
        stb.append("     LEFT JOIN SUBCLASS_SELECT T1 ON ");
        stb.append("         T1.YEAR = T2.YEAR ");
        stb.append("         AND T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T6 ON ");
        stb.append("         T6.GRADE = T2.GRADE ");
        stb.append("         AND T6.HR_CLASS = T2.HR_CLASS ");
        stb.append("         AND T6.YEAR = T1.YEAR ");
        stb.append("         AND T6.SEMESTER = T2.SEMESTER ");
        if ("1".equals(param._use_prg_schoolkind)) {
            if (!StringUtils.isBlank(param._selectSchoolKind)) {
                stb.append("   INNER JOIN SCHREG_REGD_GDAT T8 ON T8.YEAR = T2.YEAR AND T8.GRADE = T2.GRADE AND T8.SCHOOL_KIND IN (" + param._selectSchoolKindIn + ") ");
            }
        } else if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOL_KIND) && !StringUtils.isBlank(param._SCHOOLCD)) {
            stb.append("   INNER JOIN SCHREG_REGD_GDAT T8 ON T8.YEAR = T2.YEAR AND T8.GRADE = T2.GRADE AND T8.SCHOOL_KIND = '" + param._SCHOOL_KIND + "' ");
        }
        stb.append("     LEFT JOIN V_COURSE_MAJOR_MST CM_M ON ");
        stb.append("         CM_M.YEAR = T2.YEAR ");
        stb.append("         AND CM_M.COURSECD = T2.COURSECD ");
        stb.append("         AND CM_M.MAJORCD = T2.MAJORCD ");
        stb.append("     LEFT JOIN V_COURSECODE_MST COURSE_M ON ");
        stb.append("         COURSE_M.YEAR = T2.YEAR ");
        stb.append("         AND COURSE_M.COURSECODE = T2.COURSECODE ");
        stb.append("     INNER JOIN SUBCLASS_MST T4 ON ");
        stb.append("         T4.SUBCLASSCD = T1.SUBCLASSCD ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("         AND T4.CLASSCD = T1.CLASSCD AND T4.SCHOOL_KIND = T1.SCHOOL_KIND AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("     INNER JOIN CLASS_MST T5 ON ");
        stb.append("         T5.CLASSCD = T1.CLASSCD ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("         AND T5.SCHOOL_KIND = T1.SCHOOL_KIND ");
        }
        stb.append("     LEFT JOIN CREDIT_MST CM ON ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("         CM.CLASSCD = T1.CLASSCD AND CM.SCHOOL_KIND = T1.SCHOOL_KIND AND CM.CURRICULUM_CD = T1.CURRICULUM_CD AND ");
        }
        stb.append("         CM.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND CM.YEAR = T2.YEAR ");
        stb.append("         AND CM.COURSECD = T2.COURSECD ");
        stb.append("         AND CM.MAJORCD = T2.MAJORCD ");
        stb.append("         AND CM.GRADE = T2.GRADE ");
        stb.append("         AND CM.COURSECODE = T2.COURSECODE ");
        stb.append("     LEFT JOIN  (SELECT SCHREGNO, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("         CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD, MIN(GROUPCD) AS MIN_GROUPCD, MAX(GROUPCD) AS MAX_GROUPCD ");
        stb.append("     FROM SUBCLASS_SELECT ");
        stb.append("     GROUP BY ");
        stb.append("         SCHREGNO, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("         CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ");
        }
        stb.append("         SUBCLASSCD ");
        stb.append("     ) SS ON ");
        stb.append("         T1.SCHREGNO = SS.SCHREGNO ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("         AND T1.CLASSCD = SS.CLASSCD AND T1.SCHOOL_KIND = SS.SCHOOL_KIND AND T1.CURRICULUM_CD = SS.CURRICULUM_CD ");
        }
        stb.append("         AND T1.SUBCLASSCD = SS.SUBCLASSCD ");
        stb.append("         AND (T1.GROUPCD <> SS.MIN_GROUPCD OR T1.GROUPCD <> SS.MAX_GROUPCD) ");
        stb.append("     LEFT JOIN NAME_MST T5 ON ");
        stb.append("         T5.NAMECD1 = 'Z002' ");
        stb.append("         AND T5.NAMECD2 = T3.SEX ");
        stb.append("     LEFT JOIN STAFF_MST T7 ON ");
        stb.append("         T7.STAFFCD = T6.TR_CD1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + param._year + "' ");
        stb.append("     AND T2.SEMESTER = '" + param._semester + "' ");
        stb.append("     AND T2.GRADE|| T2.HR_CLASS IN " + SQLUtils.whereIn(true, param._hrClass) + " ");
        stb.append("     AND ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD ");
        } else {
            stb.append("     SUBSTR(T1.SUBCLASSCD,1,2) ");
        }
        stb.append("             BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
        if ("1".equals(param._kijun3)) {
            stb.append("     AND CM.REQUIRE_FLG IN ('3', '4') ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T2.GRADE, ");
        stb.append("     T2.HR_CLASS, ");
        stb.append("     T2.COURSECD, ");
        stb.append("     T2.MAJORCD, ");
        stb.append("     T2.COURSECODE, ");
        stb.append("     T2.ATTENDNO, ");
        stb.append("     T1.SCHREGNO, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append("     T1.SUBCLASSCD ");
        return stb.toString();
    }

    public static int page(final Collection col, final int max) {
        return col.size() / max + (col.size() % max == 0 ? 0 : 1);
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 61597 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** ホームルーム */
    private static class HomeRoom {
        final String _hrClass;
        final String _hrName;
        final String _tr1Name;
        final String _courseCd;
        final String _majorCd;
        final String _courseCode;
        final String _majorName;
        final String _courseCodeName;
        final List _students = new ArrayList(); // 生徒リスト
        Map _gunData = Collections.EMPTY_MAP;

        HomeRoom(final String hrClass, final String hrName, final String tr1Name, final DB2UDB db2, final String courseCd, final String majorCd, final String courseCode, final String majorName, final String courseCodeName) {
            _hrClass = hrClass;
            _hrName = hrName;
            _tr1Name = tr1Name;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _majorName = majorName;
            _courseCodeName = courseCodeName;
        }

        private String getCourseKey() {
            return _courseCd + _majorCd + _courseCode;
        }

        private static HomeRoom getHomeRoom(final List homerooms, final String hrClass, final String courseCd, final String majorCd, final String courseCode) {
            for (final Iterator it = homerooms.iterator(); it.hasNext();) {
                final HomeRoom hr = (HomeRoom) it.next();
                final String hrkey = hr._hrClass + hr.getCourseKey();
                final String hrCheckKey = hrClass + courseCd + majorCd + courseCode;
                if (hrkey.equals(hrCheckKey)) {
                    return hr;
                }
            }
            return null;
        }

        /** 群の設定を取得 */
        private static Map getGunData(final DB2UDB db2, final HomeRoom hr, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map gunData = new HashMap();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("    T1.GROUPCD, T1.GROUPABBV, T2.CHAIRCD ");
                stb.append(" FROM ");
                stb.append("    V_ELECTCLASS_MST T1 ");
                stb.append(" INNER JOIN CHAIR_DAT T2 ON T2.YEAR = '" + param._year + "' ");
                stb.append("    AND T2.SEMESTER = '" + param._semester + "' ");
                stb.append("    AND T2.GROUPCD = T1.GROUPCD ");
                stb.append("    AND T2.YEAR = T1.YEAR ");
                if ("1".equals(param._use_prg_schoolkind)) {
                    if (!StringUtils.isBlank(param._selectSchoolKind)) {
                        stb.append("   AND SCHOOL_KIND IN (" + param._selectSchoolKindIn + ") ");
                    }
                } else if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOL_KIND) && !StringUtils.isBlank(param._SCHOOLCD)) {
                    stb.append("   AND SCHOOL_KIND = '" + param._SCHOOL_KIND + "' ");
                }
                stb.append(" INNER JOIN CHAIR_STD_DAT T3 ON T3.YEAR = T2.YEAR ");
                stb.append("    AND T3.SEMESTER = T2.SEMESTER ");
                stb.append("    AND T3.CHAIRCD = T2.CHAIRCD ");
                stb.append("    AND '" + param._date + "' BETWEEN T3.APPDATE AND T3.APPENDDATE ");
                stb.append(" INNER JOIN SCHREG_REGD_DAT T4 ON T4.YEAR = T2.YEAR ");
                stb.append("    AND T4.SEMESTER = T2.SEMESTER ");
                stb.append("    AND T4.GRADE = '" + param._grade + "' ");
                stb.append("    AND T4.HR_CLASS = '" + hr._hrClass + "' ");
                stb.append("    AND T4.SCHREGNO = T3.SCHREGNO ");
                stb.append("    AND T4.COURSECD = '" + hr._courseCd + "' ");
                stb.append("    AND T4.MAJORCD = '" + hr._majorCd + "' ");
                stb.append("    AND T4.COURSECODE = '" + hr._courseCode + "' ");
                if ("1".equals(param._use_prg_schoolkind)) {
                    if (!StringUtils.isBlank(param._selectSchoolKind)) {
                        stb.append("   INNER JOIN SCHREG_REGD_GDAT T5 ON T5.YEAR = T4.YEAR AND T5.GRADE = T4.GRADE AND T5.SCHOOL_KIND IN (" + param._selectSchoolKindIn + ") ");
                    }
                } else if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOL_KIND) && !StringUtils.isBlank(param._SCHOOLCD)) {
                    stb.append("   INNER JOIN SCHREG_REGD_GDAT T5 ON T5.YEAR = T4.YEAR AND T5.GRADE = T4.GRADE AND T5.SCHOOL_KIND = '" + param._SCHOOL_KIND + "' ");
                }
                stb.append(" WHERE ");
                stb.append("    T1.YEAR = '" + param._year + "' ");
                stb.append("    AND ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("    T2.CLASSCD ");
                } else {
                    stb.append("    SUBSTR(T2.SUBCLASSCD,1,2) ");
                }
                stb.append("        BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
                stb.append(" GROUP BY T1.GROUPCD, T1.GROUPABBV, T2.CHAIRCD ");
                stb.append(" ORDER BY T1.GROUPCD ");

                final String sql = stb.toString();
                log.debug(" sql V_ELECTCLASS_MST = " + sql);

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                int column = 1;
                Gun gun = null;

                while (rs.next()) {
                    final String groupCd = rs.getString("GROUPCD");
                    if (!gunData.containsKey(groupCd)) {
                        final String abbv = rs.getString("GROUPABBV");
                        gun = new Gun(new Integer(column), groupCd, abbv);
                        gunData.put(groupCd, gun);
                        column += 1;
                    }
                    gun._chaircds.add(rs.getString("CHAIRCD"));
                }

            } catch (SQLException e) {
                log.error("Exception:", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return gunData;
        }

        private static String getGunName(final HomeRoom hr, final int column) {
            for (final Iterator it = hr._gunData.values().iterator(); it.hasNext();) {
                final Gun gun = (Gun) it.next();
                if (gun._column.intValue() == column) {
                    return StringUtils.defaultString(gun._abbv);
                }
            }
            return "";
        }
    }

    /** 生徒 */
    private static class Student {
        final String _schregno;
        final String _attendno;
        final String _name;
        final String _sex;
        final HomeRoom _hr;
        int _totalCredit;

        final List _subclassList = new ArrayList(); // 科目リスト

        Student(final String schregno, final String attendno, final String name, final String sex, final HomeRoom hr) {

            _schregno = schregno;
            _attendno = attendno;
            _name = name;
            _sex = sex;
            _hr = hr;
            _totalCredit = 0;
        }

        /** 単位合計を得る */
        public String getTotalCredits() {
            int rtn = 0;
            for (final Iterator it = _subclassList.iterator(); it.hasNext();) {
                final Subclass subclass = (Subclass) it.next();
                if (subclass._credits != null) {
                    rtn += Integer.parseInt(subclass._credits);
                }
            }
            return String.valueOf(rtn);
        }

        private static Student getStudent(final String schregno, final Collection students) {
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (student._schregno.equals(schregno)) {
                    return student;
                }
            }
            return null;
        }

        private static List createList(final Map map, final String key) {
            if (!map.containsKey(key)) {
                map.put(key, new ArrayList());
            }
            return (List) map.get(key);
        }

        /**
         * 表示用に科目リスト行のリストを設定する。
         * @param maxColumn 1行あたりの科目数(列数)
         */
        public static List setDisplayList(final Student student, final Param param, final int maxColumn) {
            List _displayLists = new ArrayList();

            if ("1".equals(param._kijun1)) {

                // 各生徒の科目を科目グループごとに(縦の)列に並べる。
                final Map subclassGroupListMap = new HashMap(); // 科目グループコードとその科目リストのマップ

                // 科目グループごとに科目リストを設定する
                for (final Iterator it = student._subclassList.iterator(); it.hasNext();) {
                    final Subclass subclass = (Subclass) it.next();

                    if (param.TABLE_IS_SUBCLASS_STD_DAT.equals(param._selectTable)) {
                        // その科目が属しているグループのグループ列に表示する。
                        // その科目がどのグループにも属していなければその科目は表示しない。
                        if (null == subclass._groupCd) {
                            continue;
                        }
                        final SubclassCompSelectDat compSelectDat = (SubclassCompSelectDat) param.getSubclassCourseCompSelectDataMap(student._hr.getCourseKey()).get(subclass._groupCd);
                        if (null != compSelectDat && compSelectDat._subclasses.contains(subclass._subclasscd)) {
                            final List subclassList = createList(subclassGroupListMap, compSelectDat._groupCd);
                            subclassList.add(subclass); // 各科目グループの科目リストに科目を追加する
                        }
                    } else {
                        // その科目が複数のグループに属しているなら複数のグループ列に表示する。
                        // その科目がどのグループにも属していなければその科目は表示しない。
                        for (final Iterator it2 = param.getSubclassCourseCompSelectDataMap(student._hr.getCourseKey()).values().iterator(); it2.hasNext();) {
                            final SubclassCompSelectDat compSelectDat = (SubclassCompSelectDat) it2.next();
                            if (compSelectDat._subclasses.contains(subclass._subclasscd)) {
                                final List subclassList = createList(subclassGroupListMap, compSelectDat._groupCd);
                                subclassList.add(subclass); // 各科目グループの科目リストに科目を追加する
                            }
                        }
                    }
                }

                // この生徒が表示する科目グループの最大行を設定する
                int linemax = 0;
                for (final Iterator it = subclassGroupListMap.keySet().iterator(); it.hasNext();) {
                    final String groupCd = (String) it.next();
                    final List subclassList = (List) subclassGroupListMap.get(groupCd);
                    linemax = Math.max(linemax, subclassList.size());
                }

                // 科目グループの最大行分の表示科目行リストを設定する
                for (int line = 0; line < linemax; line++) {
                    final Map map = new HashMap();
                    _displayLists.add(map);

                    for (final Iterator it = subclassGroupListMap.keySet().iterator(); it.hasNext();) {
                        final String groupCd = (String) it.next();
                        final List subclassList = (List) subclassGroupListMap.get(groupCd);
                        if (subclassList == null || subclassList.size() - 1 < line) {
                            continue;
                        }
                        final Subclass subclass = (Subclass) subclassList.get(line);
                        final SubclassCompSelectDat compSelectDat = (SubclassCompSelectDat) param.getSubclassCourseCompSelectDataMap(student._hr.getCourseKey()).get(groupCd);
                        map.put(compSelectDat._column, subclass); // 科目選択グループの列に科目を追加する
                    }
                }
            } else if ("1".equals(param._kijun2)) {

                // 各生徒の科目を群の講座の科目ごとに(縦の)列に並べる。
                // その科目がどのグループにも属していなければその科目は表示しない。

                final Map gunListMap = new HashMap(); // 群ごとの科目リスト

                // 群ごとに科目リストを設定する
                for (final Iterator it = student._subclassList.iterator(); it.hasNext();) {
                    final Subclass subclass = (Subclass) it.next();

                    for (final Iterator itg = student._hr._gunData.values().iterator(); itg.hasNext();) {
                        final Gun gun = (Gun) itg.next(); // 群の講座グループ
                        if (gun.containsChairCd(subclass._chairCds)) {
                            final List subclassList = createList(gunListMap, gun._groupCd);
                            subclassList.add(subclass); // 群の科目リストに科目を追加する
                        }
                    }
                }

                // 群の最大行を設定する
                int linemax = 0;
                for (final Iterator it = gunListMap.keySet().iterator(); it.hasNext();) {
                    final String groupCd = (String) it.next();
                    final List subclassList = (List) gunListMap.get(groupCd);
                    linemax = Math.max(linemax, subclassList.size());
                }

                // 群の最大行分の表示科目行リストを設定する
                for (int line = 0; line < linemax; line++) {
                    final Map map = new HashMap();
                    _displayLists.add(map);

                    for (final Iterator it = gunListMap.keySet().iterator(); it.hasNext();) {
                        final String groupCd = (String) it.next();
                        final List subclassList = (List) gunListMap.get(groupCd);
                        if (subclassList == null || subclassList.size() - 1 < line) {
                            continue;
                        }
                        final Subclass subclass = (Subclass) subclassList.get(line);
                        final Gun gun = (Gun) student._hr._gunData.get(groupCd);
                        map.put(gun._column, subclass); // 群の列に科目を追加する
                    }
                }

            } else {
                // 各生徒の科目を(横の)行に並べて表示する。
                // 端まで表示したら次の行に折り返して表示する。

                int column = 1;
                Map map = new HashMap(); // 1行のインデクスと科目のマップ
                _displayLists.add(map);

                for (final Iterator it = student._subclassList.iterator(); it.hasNext();) {
                    final Subclass sc = (Subclass) it.next();
                    if (maxColumn < column) {
                        map = new HashMap();
                        _displayLists.add(map);
                        column -= maxColumn;
                    }
                    map.put(new Integer(column), sc);
                    column += 1;
                }
            }
            return _displayLists;
        }

        public String toString() {
            return "(" + _attendno + ":" + _name + ")";
        }
    }

    /** 科目 */
    private static class Subclass {
        final String _subclasscd;
        final String _groupCd;
        final String _classname;
        final String _subclassname;
        final String _subclassabbv;
        final boolean _isChohuku;
        final String _credits;
        final Set _chairCds;
        final TreeSet _creditSet;

        Subclass(final String subclasscd, final String groupCd, final String classname, final String subclassname, final String subclassabbv, final boolean isChohuku, final String credit) {
            _subclasscd = subclasscd;
            _groupCd = groupCd;
            _classname = classname;
            _subclassname = subclassname;
            _subclassabbv = subclassabbv;
            _isChohuku = isChohuku;
            _credits = credit;
            _chairCds = new HashSet();
            _creditSet = new TreeSet();
        }

        private static Subclass getSubclass(final Collection subclasses, final String subclasscd) {
            for (final Iterator it = subclasses.iterator(); it.hasNext();) {
                final Subclass subclass = (Subclass) it.next();
                if (subclass._subclasscd.equals(subclasscd)) {
                    return subclass;
                }
            }
            return null;
        }

        public String toString() {
            return "[SUBCLASS " + _subclasscd + ":" + _subclassabbv + "(" + _groupCd + ") ]";
        }
    }


    /** 科目グループ */
    private static class SubclassCompSelectDat {
        final Integer _column; // 表示する列
        final String _groupCd;
        final String _groupName;
        final Set _subclasses;

        SubclassCompSelectDat(final Integer column, final String groupCd, final String groupName) {
            _column = column;
            _groupCd = groupCd;
            _groupName = groupName;
            _subclasses = new HashSet();
        }
        public String toString() {
            return _groupCd + " : " + _groupName + " " + _subclasses;
        }
    }

    /** 群 */
    private static class Gun {
        final Integer _column; // 表示する列
        final String _groupCd;
        final String _abbv;
        final Set _chaircds;

        Gun(final Integer column, final String groupCd, final String abbv) {
            _column = column;
            _groupCd = groupCd;
            _abbv = abbv;
            _chaircds = new HashSet();
        }

        public boolean containsChairCd(final Collection chairCds) {
            for (final Iterator it = chairCds.iterator(); it.hasNext();) {
                final String chaircd = (String) it.next();
                if (_chaircds.contains(chaircd)) {
                    return true;
                }
            }
            return false;
        }

        public String toString() {
            return _groupCd + " : " + _abbv + " " + _chaircds;
        }
    }

    /** パラメータクラス */
    private static class Param {
        public final String TABLE_IS_CHAIR_STD_DAT = "1"; // 講座名簿を参照する
        public final String TABLE_IS_SUBCLASS_STD_DAT = "2"; // 科目履修名簿を参照する
        final String _year;
        final String _semester;
        final String _date;
        final String _selectTable;
        final String _loginDate;
        final String _loginHour;
        final String _loginMinutes;
        final String _loginStaffCd;
        final String _kijun1; // グループ設定をもとにする
        final String _kijun2; // 講座の群設定をもとにする (講座名簿参照のみ可)
        final String _kijun3; // 必履修区分をもとにする（単位マスタの必履修区分が3:選択、4:選択必履修のみ）
        final String _formSelect;  // フォーム選択 1:可変 2:固定
        final String _useCurriculumcd;
        private final String _rirekiCode;
        private final String _useSchool_KindField;
        private final String _SCHOOLCD;
        private final String _SCHOOL_KIND;

        private final String _use_prg_schoolkind;
        private final String _selectSchoolKind;

        final String _grade;
        final String[] _hrClass;

        String _gradeName = null;
        String _schoolName = null;
        String _loginStaffName = null;
        Map _subclassCourseCompSelectData = null;

        String _selectSchoolKindIn = "";

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _date = request.getParameter("DATE").replace('/', '-');
            _selectTable = request.getParameter("TAISYOU_MEIBO");
            _loginDate = request.getParameter("CTRL_DATE");
            _loginStaffCd = request.getParameter("STAFFCD");
            _grade = request.getParameter("GRADE");
            _hrClass = request.getParameterValues("CATEGORY_SELECTED");
            _kijun1 = request.getParameter("KIJUN1");
            _kijun2 = request.getParameter("KIJUN2");
            _kijun3 = request.getParameter("KIJUN3");
            _formSelect = request.getParameter("FORM_SELECT");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _rirekiCode = request.getParameter("RIREKI_CODE");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD= request.getParameter("SCHOOLCD");
            _SCHOOL_KIND = request.getParameter("SCHOOL_KIND");

            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _selectSchoolKind = request.getParameter("selectSchoolKind");

            final Calendar cal = Calendar.getInstance();
            _loginHour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
            _loginMinutes = String.valueOf(cal.get(Calendar.MINUTE));

            if (!StringUtils.isBlank(_selectSchoolKind)) {
                String[] split = StringUtils.split(_selectSchoolKind, ":");
                String comma = "";
                _selectSchoolKindIn = "";
                for (int i = 0; i < split.length; i++) {
                	_selectSchoolKindIn += comma + "'" + split[i] + "'";
                    comma = ",";
                }
                log.info(" selectSchoolKindIn = " + _selectSchoolKindIn);
            }

            if ("1".equals(_kijun1)) {
                setSubclassCompSelectData(db2);
            }

            setSchoolName(db2);
            setGradeName(db2);
            setLoginStaffName(db2);
        }

        /** スタッフ名取得 */
        private void setLoginStaffName(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _loginStaffName = null;
            try {
                String sql = "SELECT STAFFNAME FROM STAFF_MST WHERE STAFFCD = '" + _loginStaffCd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    _loginStaffName = rs.getString("STAFFNAME");
                }

            } catch (SQLException e) {
                log.error("Exception:", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /** 学年名取得 */
        private void setGradeName(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _gradeName = null;
            try {
                String sql = "SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";
                if ("1".equals(_use_prg_schoolkind)) {
                    if (!StringUtils.isBlank(_selectSchoolKind)) {
                        sql += ("   AND SCHOOL_KIND IN (" + _selectSchoolKindIn + ") ");
                    }
                } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOL_KIND) && !StringUtils.isBlank(_SCHOOLCD)) {
                    sql += ("   AND SCHOOL_KIND = '" + _SCHOOL_KIND + "' ");
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    _gradeName = rs.getString("GRADE_NAME1") != null ? rs.getString("GRADE_NAME1") : "";
                }

            } catch (SQLException e) {
                log.error("Exception:", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /** 学校名取得 */
        private void setSchoolName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _schoolName = null;
            try {
                String sql = "SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _year + "' ";
                if ("1".equals(_use_prg_schoolkind)) {
                    if (!StringUtils.isBlank(_selectSchoolKind)) {
                        sql += ("   AND SCHOOL_KIND IN (" + _selectSchoolKindIn + ") ");
                    }
                } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOL_KIND) && !StringUtils.isBlank(_SCHOOLCD)) {
                    sql += ("   AND SCHOOL_KIND = '" + _SCHOOL_KIND + "' ");
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    _schoolName = rs.getString("SCHOOLNAME1");
                }

            } catch (SQLException e) {
                log.error("Exception:", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /** 科目選択データ取得 */
        private void setSubclassCompSelectData(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final StringBuffer sql = new StringBuffer();
            sql.append("SELECT T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE_KEY, T1.GROUPCD AS GROUPCD, T2.NAME, ");
            if ("1".equals(_useCurriculumcd)) {
                sql.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            sql.append(" T1.SUBCLASSCD AS SUBCLASSCD ");
            sql.append("FROM SUBCLASS_COMP_SELECT_DAT T1 ");
            sql.append("INNER JOIN SUBCLASS_COMP_SELECT_MST T2 ON T2.YEAR = T1.YEAR ");
            sql.append("    AND T2.GRADE = T1.GRADE ");
            sql.append("    AND T2.GROUPCD = T1.GROUPCD ");
            sql.append("    AND T2.COURSECD = T1.COURSECD ");
            sql.append("    AND T2.MAJORCD = T1.MAJORCD ");
            sql.append("    AND T2.COURSECODE = T1.COURSECODE ");
            sql.append("WHERE T1.YEAR = '" + _year + "' AND T1.GRADE = '" + _grade + "' ");
            if ("1".equals(_use_prg_schoolkind)) {
                if (!StringUtils.isBlank(_selectSchoolKind)) {
                    sql.append("   AND T1.SCHOOL_KIND IN (" + _selectSchoolKindIn + ") ");
                }
            } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOL_KIND) && !StringUtils.isBlank(_SCHOOLCD)) {
                sql.append("   AND T1.SCHOOL_KIND = '" + _SCHOOL_KIND + "' ");
            }
            sql.append("ORDER BY GROUPCD ");

            _subclassCourseCompSelectData = new HashMap();
            try {
                log.debug(" sql SUBCLASS_COMP_SELECT_DAT = " + sql);

                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();

                SubclassCompSelectDat scsd = null;

                while (rs.next()) {
                    final String courseKey = rs.getString("COURSE_KEY");
                    final Map subclassCompSelectData = (Map) getSubclassCourseCompSelectDataMap(courseKey);
                    final String groupCd = rs.getString("GROUPCD");
                    final String name = rs.getString("NAME");
                    final String subclassCd = rs.getString("SUBCLASSCD");

                    if (!subclassCompSelectData.containsKey(groupCd)) {
                        final int column = subclassCompSelectData.size() + 1;
                        subclassCompSelectData.put(groupCd, new SubclassCompSelectDat(new Integer(column), groupCd, name));
                    }
                    scsd = (SubclassCompSelectDat) subclassCompSelectData.get(groupCd);
                    scsd._subclasses.add(subclassCd);
                }

            } catch (SQLException e) {
                log.error("Exception:", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String getSubclassCompSelectDatName(final String courseKey, final int column) {
            for (final Iterator it = getSubclassCourseCompSelectDataMap(courseKey).values().iterator(); it.hasNext();) {
                final SubclassCompSelectDat obj = (SubclassCompSelectDat) it.next();
                if (obj._column.intValue() == column) {
                    return StringUtils.defaultString(obj._groupName);
                }
            }
            return "";
        }

        private Map getSubclassCourseCompSelectDataMap(final String courseKey) {
            if (null == _subclassCourseCompSelectData.get(courseKey)) {
                _subclassCourseCompSelectData.put(courseKey, new TreeMap());
            }
            return (Map) _subclassCourseCompSelectData.get(courseKey);
        }
    }
}

// eof

