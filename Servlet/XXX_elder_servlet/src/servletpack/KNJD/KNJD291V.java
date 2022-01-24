/*
 * $Id: d95da5790df1c335d29ad6c1c88143aed28e3b78 $
 *
 * 作成日: 2013/07/25
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
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

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理]  成績判定会議資料
 */

public class KNJD291V {

    private static final Log log = LogFactory.getLog(KNJD291V.class);

    private static final String SEMEALL = "9";
    private static final String HYOTEI_TESTCD = "990009";

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
        Vrw32alp svf = null;
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
            
            _hasData = false;

            _param = createParam(db2, request);
            
            final List studentList = Student.getStudentList(db2, _param);
            setData(db2, _param, studentList);

            svf = new Vrw32alp();
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());
            
            printMain(svf, studentList);

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            svf.VrQuit();

            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }
    }
    
    private void printMain(final Vrw32alp svf, final List studentList0) {

        final int studentMax = 50;
        
        final String form = "KNJD291V.frm";
        svf.VrSetForm(form, 1);
        
        String title = "";
        title += KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　成績判定会議資料";
        if ("1".equals(_param._output)) {
            title += "（未履修のみ）";
        } else if ("2".equals(_param._output)) {
            title += "（成績不振者のみ）";
        } else if ("3".equals(_param._output)) {
            title += "（未履修および成績不振者）";
        }
        
        final Map subclassStudentListMap = getSubclassStudentListMap(_param, studentList0);
        for (final Iterator it = subclassStudentListMap.entrySet().iterator(); it.hasNext();) {
            final Map.Entry e = (Map.Entry) it.next();
            final Subclass subclass = (Subclass) e.getKey();
            final List allStudentList = (List) e.getValue();
            
            final List pageList = getPageList(allStudentList, studentMax);
            for (int pi = 0; pi < pageList.size(); pi++) {
                final List studentList = (List) pageList.get(pi);
                
                svf.VrsOut("TITLE", title); // タイトル
                svf.VrsOut("SUBCLASS_CD", subclass._subclasscd); // 科目コード
                svf.VrsOut("SUBCLASS_NAME", subclass._subclassname); // 科目名
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate)); // 作成日
                
                for (int j = 0; j < studentList.size(); j++) {
                    final Student student = (Student) studentList.get(j);
                    final int line = j + 1;
                    svf.VrsOutn("HR_NAME", line, student.getHrNameAttendnoCd()); // 年組番
                    svf.VrsOutn("SCHREGNO", line, student._schregno); // 学籍番号
                    svf.VrsOutn("NAME", line, student._name); // 氏名
                    svf.VrsOutn("CREDIT", line, subclass.getCredit(student.course())); // 単位数
                    svf.VrsOutn("GET_CREDIT", line, student.getGetCredit(subclass._subclasscd)); // 修得単位数
                    
                    String hyotei = student.getScore(subclass._subclasscd);
                    if ("0".equals(hyotei)) {
                        hyotei = "未";
                    }
                    svf.VrsOutn("VALUE", line, hyotei); // 
                    svf.VrsOutn("KEKKA", line, student.getKekka(subclass._subclasscd)); // 欠課時数
                }
                _hasData = true;
                svf.VrEndPage();
            }
        }
    }

    private static List getMappedList(final Map map, final Object key) {
        if (null == map.get(key)) {
            map.put(key, new ArrayList());
        }
        return (List) map.get(key);
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
    
    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private static void setData(final DB2UDB db2, final Param param, final List studentList) {

        log.debug(" setData ");
        PreparedStatement ps = null;
        ResultSet rs = null;

        // 科目出欠
        try {
            final String sql = AttendAccumulate.getAttendSubclassSql(
                    param._year,
                    param._semester,
                    null,
                    param._edate,
                    param._attendParamMap);
            
            // log.debug(" attend subclass sql = " + sql);
            ps = db2.prepareStatement(sql);
            
            final List hrClassList = HrClass.getHrClassList(studentList);
            for (final Iterator hIt = hrClassList.iterator(); hIt.hasNext();) {
                final HrClass hrClass = (HrClass) hIt.next();
                log.debug(" set SubclassAttendance " + hrClass);
            
                ps.setString(1, hrClass._grade);
                ps.setString(2, hrClass._hrClass);

                rs = ps.executeQuery();
                
                while (rs.next()) {
                    final Student student = Student.getStudent(rs.getString("SCHREGNO"), hrClass._studentList);
                    if (student == null || !"9".equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    if (null == param._subclassMap.get(subclasscd)) {
                        log.debug(" no subclass " + subclasscd);
                        continue;
                    }
                    final Subclass subclass = (Subclass) param._subclassMap.get(subclasscd);
                    final BigDecimal lesson = rs.getBigDecimal("MLESSON");
                    final BigDecimal sick = subclass._isSaki ? rs.getBigDecimal("REPLACED_SICK") : rs.getBigDecimal("SICK2");
                    final SubclassAttendance sa = new SubclassAttendance((Subclass) param._subclassMap.get(subclasscd), lesson, sick);
                    
                    student._subclassAttendance.put(subclasscd, sa);
//                    log.debug("   schregno = " + student._schregno + " , subclcasscd = " + subclasscd + " , subclass attendance = " + sa);
                }
            }
        } catch (Exception e) {
            log.debug("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        // 単位
        try {
            final String sql = SubclassScore.getSubclassCredit(param);
            //log.debug(" setCredit sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final Student student = Student.getStudent(schregno, studentList);
                if (null == student) {
                    continue;
                }
                final String subclasscd = rs.getString("SUBCLASSCD");
                
                if (!param._subclassMap.containsKey(subclasscd)) {
                    continue;
                }
                final Subclass subclass = (Subclass) param._subclassMap.get(subclasscd);
                
                subclass._coursesMap.put(student.course(), rs.getObject("CREDITS"));
//                getMappedSet(subclass._creditsCourse, rs.getObject("CREDITS")).add(student.course());
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        // 成績
        try {
            final String sql = SubclassScore.getSubclassScoreSql(param);
            //log.debug(" setRecord  sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final Student student = Student.getStudent(schregno, studentList);
                if (null == student) {
                    continue;
                }
                
                final String subclasscd = rs.getString("SUBCLASSCD");
                final String classabbv = rs.getString("CLASSABBV");
                final String subclassname = rs.getString("SUBCLASSNAME");
                final String subclassabbv = rs.getString("SUBCLASSABBV");
                final String score = rs.getString("SCORE");
                final String getCredit = rs.getString("GET_CREDIT");
                
                if (!param._subclassMap.containsKey(subclasscd)) {
                    param._subclassMap.put(subclasscd, new Subclass(subclasscd, classabbv, subclassname, subclassabbv, false, false));
                }
                final Subclass subclass = (Subclass) param._subclassMap.get(subclasscd);
                
                final SubclassScore subclassscore = new SubclassScore(student, subclass, score, getCredit);
                
                student._subclassScore.put(subclasscd, subclassscore);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }
    
    private static Map getSubclassStudentListMap(final Param param, final List studentList) {
        final Map rtn = new TreeMap();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            for (final Iterator sit = student._subclassScore.values().iterator(); sit.hasNext();) {
                final SubclassScore subScore = (SubclassScore) sit.next();
                
                boolean isTgt = false;
                if ("1".equals(param._output)) {
                    if ("0".equals(subScore._score)) {
                        isTgt = true;
                    }
                } else if ("2".equals(param._output)) {
                    if ("1".equals(subScore._score)) {
                        isTgt = true;
                    }
                } else if ("3".equals(param._output)) {
                    if ("0".equals(subScore._score) || "1".equals(subScore._score)) {
                        isTgt = true;
                    }
                }
                if (false == isTgt) {
                    continue;
                }
                if (null == rtn.get(subScore._subclass)) {
                    final Subclass subclass = (Subclass) param._subclassMap.get(subScore._subclass._subclasscd);
                    if (null == subclass) {
                        continue;
                    } else {
                        rtn.put(subclass, new ArrayList());
                    }
                }
                getMappedList(rtn, subScore._subclass).add(student);
            }
        }
        return rtn;
    }

    private static class Student {
        
        final String _grade;
        final String _hrClass;
        final String _hrNameabbv;
        final String _attendno;
        final String _schregno;
        final String _hrName;
        final String _name;
        final String _sex;
        final String _sexName;
        final String _entdiv;
        final String _entdivName;
        final String _entdate;
        final String _grddiv;
        final String _grddivName;
        final String _grddate;
        // 年度開始日時点の異動データ
        final String _transfercd1;
        final String _transfername1;
        final String _transferreason1;
        final String _transferSdate1;
        final String _transferEdate1;
        // パラメータ指定日付時点の異動データ
        final String _transfercd2;
        final String _transfername2;
        final String _transferreason2;
        final String _transferSdate2;
        final String _transferEdate2;
        final String _coursecd;
        final String _majorcd;
        final String _majorname;
        final String _coursecode;
        final String _coursecodename;
        final Map _subclassScore = new TreeMap();
        final Map _subclassAttendance = new TreeMap();

        Student(
            final String grade,
            final String hrClass,
            final String hrNameabbv,
            final String attendno,
            final String schregno,
            final String hrName,
            final String name,
            final String sex,
            final String sexName,
            final String entdiv,
            final String entdivName,
            final String entdate,
            final String grddiv,
            final String grddivName,
            final String grddate,
            final String transfercd1,
            final String transfername1,
            final String transferreason1,
            final String transferSdate1,
            final String transferEdate1,
            final String transfercd2,
            final String transfername2,
            final String transferreason2,
            final String transferSdate2,
            final String transferEdate2,
            final String coursecd,
            final String majorcd,
            final String majorname,
            final String coursecode,
            final String coursecodename
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrNameabbv = hrNameabbv;
            _attendno = attendno;
            _schregno = schregno;
            _hrName = hrName;
            _name = name;
            _sex = sex;
            _sexName = sexName;
            _entdiv = entdiv;
            _entdivName = entdivName;
            _entdate = entdate;
            _grddiv = grddiv;
            _grddivName = grddivName;
            _grddate = grddate;
            _transfercd1 = transfercd1;
            _transfername1 = transfername1;
            _transferreason1 = transferreason1;
            _transferSdate1 = transferSdate1;
            _transferEdate1 = transferEdate1;
            _transfercd2 = transfercd2;
            _transfername2 = transfername2;
            _transferreason2 = transferreason2;
            _transferSdate2 = transferSdate2;
            _transferEdate2 = transferEdate2;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _majorname = majorname;
            _coursecode = coursecode;
            _coursecodename = coursecodename;
        }
        
        public String coursecdMajorcd() {
            return _coursecd + _majorcd;
        }
        
        public String course() {
            return _grade + _coursecd + _majorcd + _coursecode;
        }

        private String getHrNameAttendnoCd() {
            return StringUtils.defaultString(_hrName) + getAttendnoCd();
        }

        private String getAttendnoCd() {
            return StringUtils.defaultString((NumberUtils.isDigits(_attendno)) ? String.valueOf(Integer.parseInt(_attendno)) : _attendno) + "番";
        }

        private String getScore(final String subclasscd) {
            final SubclassScore subScore = (SubclassScore) _subclassScore.get(subclasscd);
            return null == subScore ? null : subScore._score;
        }
        
        private String getGetCredit(final String subclasscd) {
            final SubclassScore subScore;
            subScore = (SubclassScore) _subclassScore.get(subclasscd);
            return null == subScore ? null : subScore._getCredit;
        }
        
        private String getKekka(final String subclasscd) {
            final SubclassAttendance subatt = (SubclassAttendance) _subclassAttendance.get(subclasscd);
            if (null != subatt && null != subatt._sick) {
                return subatt._sick.toString();
            }
            return null;
        }

        public static Student getStudent(final String schregno, final List studentList) {
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                Student student = (Student) it.next();
                if (student._schregno.equals(schregno)) {
                    return student;
                }
            }
            return null;
        }
        
        public String toString() {
            return "Student(" + _schregno + ")";
        }
        
        private static List getStudentList(final DB2UDB db2, final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHREG_TRANSFER1 AS ( ");
            stb.append("   SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     MAX(T1.TRANSFER_SDATE) AS TRANSFER_SDATE ");
            stb.append("   FROM SCHREG_TRANSFER_DAT T1 ");
            stb.append("   LEFT JOIN SEMESTER_MST T2 ON T2.YEAR = '" + param._year + "' AND T2.SEMESTER = '9' ");
            stb.append("   WHERE ");
            stb.append("     T2.SDATE BETWEEN T1.TRANSFER_SDATE AND VALUE(T1.TRANSFER_EDATE, '9999-12-31') ");
            stb.append("   GROUP BY T1.SCHREGNO ");
            stb.append(" ), SCHREG_TRANSFER2 AS ( ");
            stb.append("   SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     MAX(T1.TRANSFER_SDATE) AS TRANSFER_SDATE ");
            stb.append("   FROM SCHREG_TRANSFER_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("     '" + param._edate + "' BETWEEN T1.TRANSFER_SDATE AND VALUE(T1.TRANSFER_EDATE, '9999-12-31') ");
            stb.append("   GROUP BY T1.SCHREGNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T2.HR_NAME, ");
            stb.append("     T2.HR_NAMEABBV, ");
            stb.append("     T3.NAME, ");
            stb.append("     T3.SEX, ");
            stb.append("     NMZ002.NAME2 AS SEX_NAME, ");
            stb.append("     T3.ENT_DATE, ");
            stb.append("     T3.ENT_DIV, ");
            stb.append("     NMA002.NAME1 AS ENT_DIV_NAME, ");
            stb.append("     T3.GRD_DATE, ");
            stb.append("     T3.GRD_DIV, ");
            stb.append("     NMA003.NAME1 AS GRD_DIV_NAME, ");
            stb.append("     T5.TRANSFERCD AS TRANSFERCD1, ");
            stb.append("     NMA004_1.NAME1 AS TRANSFER_NAME1, ");
            stb.append("     T5.TRANSFERREASON AS TRANSFERREASON1, ");
            stb.append("     T5.TRANSFER_SDATE AS TRANSFER_SDATE1, ");
            stb.append("     T5.TRANSFER_EDATE AS TRANSFER_EDATE1, ");
            stb.append("     T7.TRANSFERCD AS TRANSFERCD2, ");
            stb.append("     NMA004_2.NAME1 AS TRANSFER_NAME2, ");
            stb.append("     T7.TRANSFERREASON AS TRANSFERREASON2, ");
            stb.append("     T7.TRANSFER_SDATE AS TRANSFER_SDATE2, ");
            stb.append("     T7.TRANSFER_EDATE AS TRANSFER_EDATE2, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T9.MAJORNAME, ");
            stb.append("     T1.COURSECODE, ");
            stb.append("     T8.COURSECODENAME ");
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
            stb.append(" LEFT JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T2.GRADE = T1.GRADE ");
            stb.append("     AND T2.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN SCHREG_TRANSFER1 T4 ON T4.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN SCHREG_TRANSFER_DAT T5 ON T5.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND T5.TRANSFER_SDATE = T4.TRANSFER_SDATE ");
            stb.append(" LEFT JOIN SCHREG_TRANSFER2 T6 ON T6.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN SCHREG_TRANSFER_DAT T7 ON T7.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND T7.TRANSFER_SDATE = T6.TRANSFER_SDATE ");
            stb.append(" LEFT JOIN COURSECODE_MST T8 ON T8.COURSECODE = T1.COURSECODE ");
            stb.append(" LEFT JOIN MAJOR_MST T9 ON T9.COURSECD = T1.COURSECD AND T9.MAJORCD = T1.MAJORCD ");
            stb.append(" LEFT JOIN NAME_MST NMA004_1 ON NMA004_1.NAMECD1 = 'A004' AND NMA004_1.NAMECD2 = T5.TRANSFERCD ");
            stb.append(" LEFT JOIN NAME_MST NMA004_2 ON NMA004_2.NAMECD1 = 'A004' AND NMA004_2.NAMECD2 = T7.TRANSFERCD ");
            stb.append(" LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = T3.SEX ");
            stb.append(" LEFT JOIN NAME_MST NMA002 ON NMA002.NAMECD1 = 'A002' AND NMA002.NAMECD2 = T3.ENT_DIV ");
            stb.append(" LEFT JOIN NAME_MST NMA003 ON NMA003.NAMECD1 = 'A003' AND NMA003.NAMECD2 = T3.GRD_DIV ");
            stb.append(" WHERE ");
            stb.append(" T1.YEAR = '" + param._year + "' ");
            stb.append(" AND T1.SEMESTER = '" + (SEMEALL.equals(param._semester) ? param._ctrlSemester : param._semester) + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO ");

            final List studentList = new ArrayList();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {

                log.debug(" regd sql = " + stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String hrNameabbv = rs.getString("HR_NAMEABBV");
                    final String attendno = rs.getString("ATTENDNO");
                    final String schregno = rs.getString("SCHREGNO");
                    final String hrName = rs.getString("HR_NAME");
                    final String name = rs.getString("NAME");
                    final String sex = rs.getString("SEX");
                    final String sexName = rs.getString("SEX_NAME");
                    final String entdiv = rs.getString("ENT_DIV");
                    final String entdivName = rs.getString("ENT_DIV_NAME");
                    final String entdate = rs.getString("ENT_DATE");
                    final String grddiv = rs.getString("GRD_DIV");
                    final String grddivName = rs.getString("GRD_DIV_NAME");
                    final String grddate = rs.getString("GRD_DATE");
                    final String transfercd1 = rs.getString("TRANSFERCD1");
                    final String transferName1 = rs.getString("TRANSFER_NAME1");
                    final String transferreason1 = rs.getString("TRANSFERREASON1");
                    final String transferSdate1 = rs.getString("TRANSFER_SDATE1");
                    final String transferEdate1 = rs.getString("TRANSFER_EDATE1");
                    final String transfercd2 = rs.getString("TRANSFERCD2");
                    final String transferName2 = rs.getString("TRANSFER_NAME2");
                    final String transferreason2 = rs.getString("TRANSFERREASON2");
                    final String transferSdate2 = rs.getString("TRANSFER_SDATE2");
                    final String transferEdate2 = rs.getString("TRANSFER_EDATE2");
                    final String coursecd = rs.getString("COURSECD");
                    final String majorcd = rs.getString("MAJORCD");
                    final String majorname = rs.getString("MAJORNAME");
                    final String coursecode = rs.getString("COURSECODE");
                    final String coursecodename = rs.getString("COURSECODENAME");
                    final Student student = new Student(grade, hrClass, hrNameabbv, attendno, schregno, hrName, name, sex, sexName, entdiv, entdivName, entdate, grddiv, grddivName, grddate,
                            transfercd1, transferName1, transferreason1 ,transferSdate1, transferEdate1,
                            transfercd2, transferName2, transferreason2, transferSdate2, transferEdate2,
                            coursecd, majorcd, majorname, coursecode, coursecodename);
                    studentList.add(student);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
                ps = null;
                rs = null;
            }
            return studentList;
        }
    }
    
    private static class HrClass {
        final String _grade;
        final String _hrClass;
        final String _hrNameabbv;
        final List _studentList;
//        String _avgAvg;

        HrClass(
            final String grade,
            final String hrClass,
            final String hrNameabbv
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrNameabbv = hrNameabbv;
            _studentList = new ArrayList();
        }
        
        public String getCode() {
            return _grade + _hrClass;
        }
        
        public static HrClass getHrClass(final String grade, final String hrClass, final List hrClassList) {
            for (final Iterator it = hrClassList.iterator(); it.hasNext();) {
                HrClass hrclass = (HrClass) it.next();
                if (hrclass._grade.equals(grade) && hrclass._hrClass.equals(hrClass)) {
                    return hrclass;
                }
            }
            return null;
        }
        
        public static List getHrClassList(final List studentList) {
            final List list = new ArrayList();
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (null == getHrClass(student._grade, student._hrClass, list)) {
                    list.add(new HrClass(student._grade, student._hrClass, student._hrNameabbv));
                }
                final HrClass hrclass = getHrClass(student._grade, student._hrClass, list);
                hrclass._studentList.add(student);
            }
            return list;
        }
        
        public String toString() {
            return "HrClass(" + _grade + _hrClass + ":" + _hrNameabbv + ")";
        }
    }
    
    private static class Subclass implements Comparable {
        final String _subclasscd;
        final String _classabbv;
        final String _subclassname;
        final String _subclassabbv;
        final boolean _isSaki;
        final boolean _isMoto;
        final HashMap _coursesMap;
        Subclass(
            final String subclasscd,
            final String classabbv,
            final String subclassname,
            final String subclassabbv,
            final boolean isSaki,
            final boolean isMoto
        ) {
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _subclassname = subclassname;
            _subclassabbv = subclassabbv;
            _coursesMap = new HashMap();
            _isSaki = isSaki;
            _isMoto = isMoto;
        }
        
        public int compareTo(final Object o) {
            if (!(o instanceof Subclass)) return -1;
            final Subclass s = (Subclass) o;
            return _subclasscd.compareTo(s._subclasscd);
        }
        
        public String getCredit(final String course) {
            if (null == course || null == _coursesMap.get(course)) {
                return null;
            }
            return _coursesMap.get(course).toString();
        }

        public String toString() {
            return "Subclass(" + _subclasscd + ":" + _subclassname + ")";
        }
    }
    
    /**
     * 生徒の科目の得点
     */
    private static class SubclassScore {
        final Student _student;
        final Subclass _subclass;
        final String _score;
        final String _getCredit;

        SubclassScore(
            final Student student,
            final Subclass subclass,
            final String score,
            final String getCredit
        ) {
            _student = student;
            _subclass = subclass;
            _score = score;
            _getCredit = getCredit;
        }
        
        public static String getSubclassCredit(final Param param) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T20.CLASSCD || '-' || T20.SCHOOL_KIND || '-' || T20.CURRICULUM_CD || '-' || T20.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     T20.CREDITS ");
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
            stb.append(" INNER JOIN CREDIT_MST T20 ON T20.YEAR = T1.YEAR ");
            stb.append("     AND T20.GRADE = T1.GRADE ");
            stb.append("     AND T20.COURSECD= T1.COURSECD ");
            stb.append("     AND T20.MAJORCD = T1.MAJORCD ");
            stb.append("     AND T20.COURSECODE = T1.COURSECODE ");
            stb.append(" WHERE ");
            stb.append(" T1.YEAR = '" + param._year + "' ");
            stb.append(" AND T1.SEMESTER = '" + (SEMEALL.equals(param._semester) ? param._ctrlSemester : param._semester) + "' ");
            stb.append(" AND T20.CLASSCD <= '90' ");
            return stb.toString();
        }

        public static String getSubclassScoreSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("   WITH REGD AS ( ");
            stb.append("     SELECT ");
            stb.append("      T1.SCHREGNO, ");
            stb.append("      T1.GRADE, ");
            stb.append("      T1.HR_CLASS, ");
            stb.append("      T1.COURSECD, ");
            stb.append("      T1.MAJORCD, ");
            stb.append("      T1.COURSECODE ");
            stb.append("     FROM SCHREG_REGD_DAT T1 ");
            stb.append("     WHERE ");
            stb.append("      T1.YEAR = '" + param._year + "' ");
            stb.append("      AND T1.SEMESTER = '" + (SEMEALL.equals(param._semester) ? param._ctrlSemester : param._semester) + "' ");
            stb.append(" ), REC AS ( ");
            stb.append("     SELECT ");
            stb.append("      T1.SCHREGNO, ");
            stb.append("      T1.CLASSCD, ");
            stb.append("      T1.SCHOOL_KIND, ");
            stb.append("      T1.CURRICULUM_CD, ");
            stb.append("      T1.SUBCLASSCD, ");
            stb.append("      T1.SCORE, ");
            stb.append("      T1.GET_CREDIT ");
            stb.append("     FROM RECORD_SCORE_DAT T1 ");
            stb.append("     INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_PROV_FLG_DAT PROV ON PROV.YEAR = T1.YEAR ");
            stb.append("         AND PROV.CLASSCD = T1.CLASSCD ");
            stb.append("         AND PROV.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND PROV.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND PROV.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND PROV.SCHREGNO = T1.SCHREGNO ");
            stb.append("     WHERE ");
            stb.append("      T1.YEAR = '" + param._year + "' ");
            stb.append("      AND T1.SEMESTER = '9' ");
            stb.append("      AND T1.TESTKINDCD = '99' ");
            stb.append("      AND T1.TESTITEMCD = '00' ");
            stb.append("      AND T1.SCORE_DIV = '09' ");
//            if ("1".equals(param._kariHyotei)) {
//                stb.append(" AND PROV.PROV_FLG = '1' ");
//            } else {
//                stb.append(" AND PROV.PROV_FLG IS NULL ");
//            }
            stb.append(" ), REC2 AS ( ");
            stb.append("     SELECT SCHREGNO, CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, SCORE, GET_CREDIT ");
            stb.append("     FROM REC T1 ");
            stb.append("     WHERE SCORE IS NOT NULL ");
            stb.append(" ) ");
            stb.append(" , HYOTEI_DATA AS ( ");
            stb.append(" SELECT  ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T1.SUBCLASSCD, ");
            stb.append("   T1.SCORE, ");
            stb.append("   T1.GET_CREDIT ");
            stb.append(" FROM REC2 T1 ");
            stb.append(" INNER JOIN REGD ON REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T20.CLASSCD || '-' || T20.SCHOOL_KIND || '-' || T20.CURRICULUM_CD || '-' || T20.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     T6.CLASSABBV, ");
            stb.append("     T3.SUBCLASSNAME, ");
            stb.append("     T3.SUBCLASSABBV, ");
            stb.append("     T2.SCORE, ");
            stb.append("     T2.GET_CREDIT ");
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
            stb.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND NOT (VALUE(BASE.ENT_DIV, '') IN ('4', '5') AND VALUE(BASE.ENT_DATE, '1900-01-01') > '" + param._edate + "') ");
            stb.append("     AND NOT (VALUE(BASE.GRD_DIV, '') IN ('1', '2', '3') AND VALUE(BASE.GRD_DATE, '9999-12-31') < '" + param._edate + "') ");
            stb.append(" INNER JOIN RECORD_SCORE_DAT T20 ON T20.YEAR = T1.YEAR ");
            stb.append("     AND T20.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T20.TESTKINDCD || T20.TESTITEMCD || T20.SCORE_DIV = '" + param._testcd + "' ");
            stb.append("     AND T20.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN HYOTEI_DATA T2 ON T2.SUBCLASSCD = T20.CLASSCD || '-' || T20.SCHOOL_KIND || '-' || T20.CURRICULUM_CD || '-' || T20.SUBCLASSCD ");
            stb.append("     AND T2.SCHREGNO = T20.SCHREGNO ");
            stb.append(" LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T20.SUBCLASSCD ");
            stb.append("     AND T3.SCHOOL_KIND = T20.SCHOOL_KIND ");
            stb.append("     AND T3.CURRICULUM_CD = T20.CURRICULUM_CD ");
            stb.append("     AND T3.CLASSCD = T20.CLASSCD ");
            stb.append(" LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T4 ON T4.YEAR = T20.YEAR ");
            stb.append("     AND T4.SEMESTER = T20.SEMESTER ");
            stb.append("     AND T4.TESTKINDCD = T20.TESTKINDCD ");
            stb.append("     AND T4.TESTITEMCD = T20.TESTITEMCD ");
            stb.append("     AND T4.SCORE_DIV = T20.SCORE_DIV ");
            stb.append("  LEFT JOIN CLASS_MST T6 ON T6.CLASSCD = T20.CLASSCD ");
            stb.append("     AND T6.SCHOOL_KIND = T20.SCHOOL_KIND ");
            stb.append(" WHERE ");
            stb.append(" T1.YEAR = '" + param._year + "' ");
            stb.append(" AND T1.SEMESTER = '" + (SEMEALL.equals(param._semester) ? param._ctrlSemester : param._semester) + "' ");
            stb.append(" AND T20.CLASSCD <= '90' ");

            return stb.toString();
        }
    }
    
    /**
     * 科目ごとの出欠データ
     */
    private static class SubclassAttendance {
        final Subclass _subclass;
        final int _mlesson;
        /** 換算後の欠課数 */
        final BigDecimal _sick;
        
        public SubclassAttendance(
                final Subclass subclass,
                final BigDecimal mlesson,
                final BigDecimal sick
                ) {
            _subclass =subclass;
            _mlesson = mlesson.intValue();
            _sick = sick;
        }
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 66415 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _output; // 1:未履修のみ（評定0） 2:成績不振者のみ（評定1） 3:未履修および成績不振者
        final String _testcd;
        final String _edate;
//        final String _kariHyotei;
        
        private Map _subclassMap;

        final Map _attendParamMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _output = request.getParameter("OUTPUT");
            _semester = "9";;
            _testcd = HYOTEI_TESTCD;
            _edate = request.getParameter("DATE").replace('/', '-');
//            _kariHyotei = null; // request.getParameter("KARI_HYOTEI");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _subclassMap = getSubclassMap(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");
            _attendParamMap.put("grade", "?");
            _attendParamMap.put("hrClass", "?");
        }

        private Map getSubclassMap(DB2UDB db2) {
            Map map = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH REPL AS ( ");
                stb.append(" SELECT DISTINCT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' ");
                stb.append(" UNION ");
                stb.append(" SELECT DISTINCT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     T2.CLASSABBV, ");
                stb.append("     T1.SUBCLASSNAME, ");
                stb.append("     T1.SUBCLASSABBV, ");
                stb.append("     CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ");
                stb.append("     CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO ");
                stb.append(" FROM SUBCLASS_MST T1 ");
                stb.append(" LEFT JOIN REPL L1 ON L1.DIV = '1' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ");
                stb.append(" LEFT JOIN REPL L2 ON L2.DIV = '2' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ");
                stb.append(" LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
                stb.append("   AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");

                log.debug(" subclass sql ="  + stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String classabbv = rs.getString("CLASSABBV");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String subclassabbv = rs.getString("SUBCLASSABBV");
                    final boolean isSaki = "1".equals(rs.getString("IS_SAKI"));
                    final boolean isMoto = "1".equals(rs.getString("IS_MOTO"));
                    map.put(subclasscd, new Subclass(subclasscd, classabbv, subclassname, subclassabbv, isSaki, isMoto));
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
        }
    }
}

// eof

