// kanji=漢字
/*
 * $Id: 04b91190af8a1e0de9466e10af779d712602f20f $
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * @version $Id: 04b91190af8a1e0de9466e10af779d712602f20f $
 */
public class KNJD659A {

    private static final Log log = LogFactory.getLog("KNJD659A.class");

    private boolean _hasData;
    private static final String FROM_TO_MARK = "\uFF5E";
    private static final String TAISYOUGAI_KYOUKA = "90";

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
            svf.VrQuit();
            if (null != _param) {
                DbUtils.closeQuietly(_param._psStudent);
                DbUtils.closeQuietly(_param._psRecord);
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }
    }

    private static int getMS932ByteLength(final String s) {
        if (null != s) {
            try {
                return s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return 0;
    }

    
    /**
     * 表示文字数幅分均等にスペースを挿入した文字列
     * @param str 元文字列
     * @param keta 表示文字数幅
     * @return 均等にスペースを挿入した文字列
     */
    private static String kintouwari(final String str, final int width) {
        if (null == str) {
            return StringUtils.repeat("　", width);
        }
        final String sps = StringUtils.repeat("　", (width - str.length()) / (str.length() + 1));
        final StringBuffer stb = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            stb.append(sps).append(str.charAt(i));
        }
        stb.append(StringUtils.repeat("　", width - stb.length()));
        return stb.toString();
    }
    
    /**
     * 表示文字数幅分にセンタリングした文字列
     * @param str 元文字列
     * @param keta 表示文字数幅
     * @return センタリングした文字列
     */
    private static String centering(final String str, final int width) {
        if (null == str) {
            return StringUtils.repeat("　", width);
        }
        final String sps = StringUtils.repeat("　", (width - str.length()) / 2);
        final StringBuffer stb = new StringBuffer();
        stb.append(sps);
        for (int i = 0; i < str.length(); i++) {
            stb.append(str.charAt(i));
        }
        stb.append(StringUtils.repeat("　", width - stb.length()));
        return stb.toString();
    }

    private static List getPageList(final List list, final int count) {
        final List rtn = new ArrayList();
        List current = new ArrayList();
        rtn.add(current);
        for (final Iterator it = list.iterator(); it.hasNext();) {
            Object o = it.next();
            if (current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    
    private static List getMappedList(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        
        final Map gradeViewClassListMap = new HashMap();
        
        for (int hri = 0; hri < _param._categorySelected.length; hri++) {
            
            final String gradeHrclass = _param._categorySelected[hri];
            final String grade = gradeHrclass.substring(0, 2);
            log.info(" gradeHrclass = " + gradeHrclass);
            if (null == gradeViewClassListMap.get(grade)) {
                gradeViewClassListMap.put(grade, ViewClass.getViewClassList(db2, _param, grade));
            }
            final List viewClassList = (List) gradeViewClassListMap.get(grade);
            final List columnListAll = new ArrayList();
            for (int i = 0; i < viewClassList.size(); i++) {
                final ViewClass vc = (ViewClass) viewClassList.get(i);
                columnListAll.addAll(vc.getColumnList(String.valueOf(i)));
            }
            
            final int maxLine = 45;
            final List studentListAll = Student.getStudentList(db2, _param, gradeHrclass);
            final List studentPageList = getPageList(studentListAll, maxLine);
            
            String form = null;
            int maxcol = 0;
            if (columnListAll.size() <= 37) {
                maxcol = 37;
                form = "KNJD659A_1.frm";
            } else if (columnListAll.size() <= 44) {
                maxcol = 44;
                form = "KNJD659A_2.frm";
            } else { // if (printColumnList.size() <= 49) {
                maxcol = 49;
                form = "KNJD659A_3.frm";
            }
            
            final List columnPageList = getPageList(columnListAll, maxcol);
            for (int cpi = 0; cpi < columnPageList.size(); cpi++) {
                
                final List columnList = (List) columnPageList.get(cpi);
                
                for (int spi = 0; spi < studentPageList.size(); spi++) {
                    final List studentList = (List) studentPageList.get(spi);

                    svf.VrSetForm(form, 4);
                    svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度　" + StringUtils.defaultString(_param._semestername) + "　単元テスト成績一覧"); // タイトル
                    for (int j = 0; j < studentList.size(); j++) {
                        final Student student = (Student) studentList.get(j);
                        final int line = j + 1;
                        svf.VrsOutn("SCHREG_NO", line, student._schregno); // 学籍番号
                        svf.VrsOutn("HR_NAME", line, student.getHrAttendNo(_param)); // 年組番
                        final int ketaName = getMS932ByteLength(student._name);
                        svf.VrsOutn(ketaName <= 20 ? "NAME1" : ketaName <= 30 ? "NAME2" : "NAME3", line, student._name);
                    }
                    
                    for (int ci = 0; ci < columnList.size(); ci++) {
                        final Map column = (Map) columnList.get(ci);
                        
                        //log.info(" column " + ci + " = " + column);
                        
                        svf.VrsOut("GROUPCD", (String) column.get("GROUPCD")); // グループ用コード
                        
                        final String className = StringUtils.replace((String) column.get("CLASSNAME"), " ", "　");
                        final int ketaClassName = getMS932ByteLength(className);
                        final String field;
                        if (ketaClassName <= 2) {
                            field = "KNJD659A_3.frm".equals(form) ? "CLASS_NAME1" : "CLASS_NAME";
                        } else {
                            field = "CLASS_NAME2";
                            if (className.charAt(0) == '　') {
                                svf.VrAttribute(field, "Hensyu=1"); // 右寄せ
                            }
                        }
                        svf.VrsOut(field, className);
                        
                        final String viewname = (String) column.get("VIEWNAME");
                        if (StringUtils.defaultString(viewname).length() <= 9) {
                            svf.VrsOut("VIEW1_1", viewname); // 観点
                        } else {
                            svf.VrsOut("VIEW2_1", viewname.substring(0, 9)); // 観点
                            svf.VrsOut("VIEW2_2", viewname.substring(9)); // 観点
                        }
                        int validStudentCount = 0;
                        BigDecimal totalScore = null;
                        for (int j = 0; j < studentList.size(); j++) {
                            final Student student = (Student) studentList.get(j);
                            final String line = String.valueOf(j + 1);
//                          svf.VrsOut("MARK" + ssi, null); // 文字評定
                            final String score = student.getScore(_param._semester, (String) column.get("SUBCLASSCD"), getMappedList(column, "VIEWCD_LIST"));
                            svf.VrsOut("SCORE" + line, score); // 点数
                            if (NumberUtils.isNumber(score)) {
                                if (null == totalScore) {
                                    totalScore = new BigDecimal(0);
                                }
                                validStudentCount += 1;
                                totalScore = totalScore.add(new BigDecimal(score));
                            }
                        }
                        if (null != totalScore) {
//                          svf.VrsOut("TOTAL_MARK", null); // 合計文字評定
                            svf.VrsOut("TOTAL_SCORE", totalScore.toString()); // 合計点数
                            if (validStudentCount > 0) {
                                final String avg = totalScore.divide(new BigDecimal(validStudentCount), 1, BigDecimal.ROUND_HALF_UP).toString();
//                              svf.VrsOut("AVE_MARK", null); // 平均文字評定
                                svf.VrsOut("AVE_SCORE", avg.toString()); // 平均点数
                            }
                        }
                        svf.VrEndRecord();
                    }
                    for (int ci = columnList.size(); ci < maxcol; ci++) {
                        svf.VrsOut("GROUPCD", String.valueOf(ci)); // グループ用コード
                        svf.VrAttribute("GROUPUCD", "X=10000");
                        svf.VrEndRecord();
                    }
                    _hasData = true;
                }
            }
        }
    }
    
    /**
     * 生徒
     */
    private static class Student {
        final String _schregno;
        String _name;
        final String _gradeCd;
        final String _gradeName1;
        final String _hrName;
//        final String _staffName;
        final String _grade;
        final String _attendno;
        final String _hrClassName1;
        final Map _attendMap;
//        final Map _attendRemarkMap;
        final Map _subclassMap;
//        final String _entyear;
        String _birthday;
        Map _viewstatRecordDetailMap = new HashMap();
        
        Student(final String schregno, final String gradeCd, final String gradeName1, final String hrName
//                , final String staffName
                , final String attendno, final String grade, final String hrClassName1
//                , final String entyear
                ) {
            _schregno = schregno;
            _gradeCd = gradeCd;
            _gradeName1 = gradeName1;
            _hrName = hrName;
//            _staffName = staffName;
            _attendno = attendno;
            _grade = grade;
            _hrClassName1 = hrClassName1;
//            _entyear = entyear;
            _attendMap = new TreeMap();
//            _attendRemarkMap = new TreeMap();
            _subclassMap = new TreeMap();
        }
        
        private String getHrAttendNo(final Param param) {
            try {
                final String grade = String.valueOf(Integer.parseInt(_gradeCd)) + "年";
                final String hrclass = StringUtils.defaultString(_hrClassName1) + "組";
                final String attendno = String.valueOf(Integer.parseInt(_attendno)) + "番";
                return grade + " " + hrclass + " " + attendno;
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return null;
        }
        
        /**
         * 生徒を取得
         */
        private static List getStudentList(final DB2UDB db2, final Param param, final String gradeHrclass) throws SQLException {
            
            if (null == param._psStudent) {
                final StringBuffer stb = new StringBuffer();
                stb.append("     SELECT  REGD.SCHREGNO");
                stb.append("            ,REGD.SEMESTER ");
                stb.append("            ,BASE.NAME ");
                stb.append("            ,REGDH.HR_NAME ");
                stb.append("            ,REGD.ATTENDNO ");
                stb.append("            ,REGD.GRADE ");
                stb.append("            ,REGDG.GRADE_CD ");
                stb.append("            ,REGDG.GRADE_NAME1 ");
                stb.append("            ,REGDH.HR_CLASS_NAME1 ");
//            stb.append("            ,FISCALYEAR(BASE.ENT_DATE) AS ENT_YEAR ");
//            stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
//            stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
//            stb.append("                  WHEN TRANSF.SCHREGNO IS NOT NULL THEN 1 ");
//            stb.append("                  ELSE 0 END AS LEAVE ");
                stb.append("     FROM    SCHREG_REGD_DAT REGD ");
//            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = '" + param._year + "' AND W2.SEMESTER = REGD.SEMESTER AND W2.GRADE = '" + param._grade + "' ");
//            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
//            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = REGD.SCHREGNO ");
//            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
//            stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + param._edate + "' THEN W2.EDATE ELSE '" + param._edate + "' END ");
//            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
//            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = REGD.SCHREGNO ");
//            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
//            stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + param._edate + "' THEN W2.EDATE ELSE '" + param._edate + "' END ");
//            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
//            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT TRANSF ON TRANSF.SCHREGNO = REGD.SCHREGNO ");
//            stb.append("                  AND TRANSF.TRANSFERCD IN ('1','2') ");
//            stb.append("                  AND CASE WHEN W2.EDATE < '" + param._edate + "' THEN W2.EDATE ELSE '" + param._edate + "' END BETWEEN TRANSF.TRANSFER_SDATE AND TRANSF.TRANSFER_EDATE ");
                stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
                stb.append("                  AND REGDH.SEMESTER = REGD.SEMESTER ");
                stb.append("                  AND REGDH.GRADE = REGD.GRADE ");
                stb.append("                  AND REGDH.HR_CLASS = REGD.HR_CLASS ");
                stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR AND REGDG.GRADE = REGD.GRADE ");
                stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
                stb.append("     WHERE   REGD.YEAR = '" + param._ctrlYear + "' ");
//            if (SEMEALL.equals(param._semester)) {
//                stb.append("     AND REGD.SEMESTER = '" + param._ctrlSemester + "' ");
//            } else {
//                stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
//                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
//                stb.append("                    WHERE S1.SCHREGNO = REGD.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
//            }
                stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
                stb.append("         AND REGD.GRADE || REGD.HR_CLASS = ? ");
                stb.append("     ORDER BY ");
                stb.append("         REGD.ATTENDNO ");
                final String sql = stb.toString();
                log.info(" student sql = " + sql);
                
                param._psStudent = db2.prepareStatement(stb.toString());
            }
            ResultSet rs = null;

            final List studentList = new ArrayList();
            try {
                param._psStudent.setString(1, gradeHrclass);
                rs = param._psStudent.executeQuery();

                while (rs.next()) {
                    final String attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) : rs.getString("ATTENDNO");
                    //final String staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                    final Student student = new Student(rs.getString("SCHREGNO"), rs.getString("GRADE_CD"), rs.getString("GRADE_NAME1"), rs.getString("HR_NAME"), attendno, rs.getString("GRADE"), rs.getString("HR_CLASS_NAME1"));
                    student._name = rs.getString("NAME");
                    studentList.add(student);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            
            setViewstatRecordDetail(db2, param, studentList);
            return studentList;
        }
        
        
        public static void setViewstatRecordDetail(final DB2UDB db2, final Param param, final List studentList) {
            ResultSet rs = null;
            try {
                if (null == param._psRecord) {
                    final String sql = getViewstatRecordDetailSql(param);
                    log.info(" view record sql = "+  sql);
                    param._psRecord = db2.prepareStatement(sql);
                }
                
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    
                    param._psRecord.setString(1, student._schregno);
                    rs = param._psRecord.executeQuery();
                    while (rs.next()) {
                        
                        final String semester = rs.getString("SEMESTER");
                        final String viewcd = rs.getString("VIEWCD");
                        final String subclasscd = rs.getString("SUBCLASSCD");
                        final String remark1 = rs.getString("REMARK1");
                        
                        student._viewstatRecordDetailMap.put(semester + subclasscd + viewcd, remark1);
                    }
                    DbUtils.closeQuietly(rs);
                }
                
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        }
        
        private String getScore(final String semester, final String subclasscd, final List viewcdList) {
            BigDecimal rtn = null;
            for (int i = 0; i < viewcdList.size(); i++) {
                final String viewcd = (String) viewcdList.get(i);
                final String remark1 = getRemark1(semester, subclasscd, viewcd);
                if (NumberUtils.isNumber(remark1)) {
                    if (null == rtn) {
                        rtn = new BigDecimal(0);
                    }
                    rtn = rtn.add(new BigDecimal(remark1));
                }
            }
            return null == rtn ? null : rtn.toString();
        }
        
        private static String getViewstatRecordDetailSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SEMESTER   ");
            stb.append("     , T1.SCHREGNO ");
            if ("1".equals(param._useCurriculumcd)) {
            	stb.append("     , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
            } else {
                stb.append("     , T1.SUBCLASSCD AS SUBCLASSCD ");
            }
            stb.append("     , T1.VIEWCD ");
            stb.append("     , T1.REMARK1 ");
            stb.append(" FROM JVIEWSTAT_RECORD_DETAIL_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");
            if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param.SCHOOLKIND)) {
                stb.append("     AND T1.SCHOOL_KIND = '" + param.SCHOOLKIND + "' ");
            }
            return stb.toString();
        }
        
        public String getRemark1(final String semester, final String subclasscd, final String viewcd) {
            return (String) _viewstatRecordDetailMap.get(semester + subclasscd + viewcd);
        }
    }

    /**
     * 観点の教科
     */
    private static class ViewClass {
        final String _classcd;
        final String _subclasscd;
        final String _classname;
        final List _viewList;
        ViewClass(
                final String classcd,
                final String subclasscd,
                final String classname) {
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classname = classname;
            _viewList = new ArrayList();
        }
        
        private static String safeSubstring(final String str, final int starti, final int mojisu) {
            if (str.length() <= starti) {
                return "";
            }
            if (starti + mojisu > str.length()) {
                return str.substring(starti);
            }
            return str.substring(starti, starti + mojisu);
        }

        public List getColumnList(final String groupCd) {
            int mojisu = 1;
            String classnameColumn = centering(_classname, (getViewSize() + 1) * mojisu);
            if (classnameColumn.length() > getViewSize() + 1) {
                mojisu = 2;
                classnameColumn = centering(_classname, (getViewSize() + 1) * mojisu);
            }
            
            final List columnList = new ArrayList();
            final List totalCdList = new ArrayList();
            for (int i = 0; i < getViewSize(); i++) {
                final Map m = new HashMap((Map) _viewList.get(i));
                m.put("GROUPCD", groupCd);
                m.put("CLASSNAME", safeSubstring(classnameColumn, mojisu * i, mojisu));
                columnList.add(m);
                totalCdList.add(getViewCd(i));
            }
            final Map total = new HashMap();
            total.put("SUBCLASSCD", _subclasscd);
            getMappedList(total, "VIEWCD_LIST").addAll(totalCdList);
            total.put("VIEWNAME", "総合平均点");
            total.put("GROUPCD", groupCd);
            total.put("CLASSNAME", safeSubstring(classnameColumn, mojisu * getViewSize(), mojisu));
            columnList.add(total);
            return columnList;
        }
        
        
        public void addView(final String viewcd, final String viewname) {
            final Map m = new HashMap();
            m.put("SUBCLASSCD", _subclasscd);
            getMappedList(m, "VIEWCD_LIST").add(viewcd);
            m.put("VIEWNAME", viewname);
            _viewList.add(m);
        }
        
        public String getViewCd(final int i) {
            return (String) getMappedList((Map) _viewList.get(i), "VIEWCD_LIST").get(0);
        }
        
        public String getViewName(final int i) {
            return (String) ((Map) _viewList.get(i)).get("VIEWNAME");
        }
        
        public int getViewSize() {
            return _viewList.size();
        }
        
        public static List getViewClassList(final DB2UDB db2, final Param param, final String grade) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewClassSql(param, grade);
                log.info(" view class sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    
                    final String classcd = rs.getString("CLASSCD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String viewcd = rs.getString("VIEWCD");
                    final String viewname = rs.getString("VIEWNAME");
                    
                    ViewClass viewClass = null;
                    for (final Iterator it = list.iterator(); it.hasNext();) {
                        final ViewClass viewClass0 = (ViewClass) it.next();
                        if (viewClass0._subclasscd.equals(subclasscd)) {
                            viewClass = viewClass0;
                            break;
                        }
                    }
                    
                    if (null == viewClass) {
                        final String classname = rs.getString("CLASSNAME");

                        viewClass = new ViewClass(classcd, subclasscd, classname);
                        list.add(viewClass);
                    }
                    
                    viewClass.addView(viewcd, viewname);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        
        private static String getViewClassSql(final Param param, final String grade) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.GRADE, ");
            stb.append("     T3.CLASSCD, ");
            stb.append("     T3.CLASSNAME, ");
            if ("1".equals(param._useCurriculumcd)) {
            	stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
            	stb.append("     T1.SUBCLASSCD AS SUBCLASSCD, ");
            }
            stb.append("     T1.VIEWCD, ");
            stb.append("     T1.VIEWNAME ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR = '" + param._ctrlYear + "' ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            if ("1".equals(param._useCurriculumcd)) {
            	stb.append("         AND T2.CLASSCD = T1.CLASSCD  ");
            	stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            	stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            }
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD  ");
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            if ("1".equals(param._useCurriculumcd)) {
            	stb.append("     INNER JOIN CLASS_MST T3 ON T3.CLASSCD = T1.CLASSCD ");
            	stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            } else {
                stb.append("     INNER JOIN CLASS_MST T3 ON T3.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1, 2) ");
            }
            stb.append("     LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T4.CLASSCD = T1.CLASSCD  ");
                stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND  ");
                stb.append("         AND T4.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.GRADE = '" + grade + "' ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND T1.CLASSCD < '90' ");
            } else {
            	stb.append("     AND SUBSTR(T1.SUBCLASSCD, 1, 2) < '90' ");
            }
            if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param.SCHOOLKIND)) {
                stb.append("     AND T1.SCHOOL_KIND = '" + param.SCHOOLKIND + "' ");
            }
            stb.append(" ORDER BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T3.CLASSCD, ");
            } else {
            	stb.append("     SUBSTR(T1.SUBCLASSCD, 1, 2), ");
            }
            stb.append("     VALUE(T1.SHOWORDER, -1), ");
            stb.append("     T1.VIEWCD ");
            return stb.toString();
        }
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 58839 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _ctrlYear;
        final String _semester;
        final String[] _categorySelected;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _useCurriculumcd;
        final String _useSchool_KindField;
        final String SCHOOLKIND;
        final String _semestername;
        PreparedStatement _psStudent;
        PreparedStatement _psRecord;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            SCHOOLKIND = request.getParameter("SCHOOLKIND");

            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _semestername = getSemestername(db2);
        }

        /**
         * 学期マスタ
         */
        private String getSemestername(final DB2UDB db2) {
            String name = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _ctrlYear +"' AND SEMESTER = '" + _semester + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    name = rs.getString("SEMESTERNAME");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name;
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(DB2UDB db2) {
            String name1 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    name1 = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
        }

        public String getSeme() {
            return "9".equals(_semester) ? _ctrlSemester : _semester;
        }
    }
}

// eof
