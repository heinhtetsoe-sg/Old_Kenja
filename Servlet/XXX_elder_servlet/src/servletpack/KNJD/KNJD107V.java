// kanji=漢字
/*
 * $Id: 8d62720cc9b31d00475364a1f0d00878a6eeb696 $
 *
 * 作成日: 2008/05/07 13:50:53 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2008-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * @version $Id: 8d62720cc9b31d00475364a1f0d00878a6eeb696 $
 */
public class KNJD107V {

    private static final String SEMEALL = "9";
    private static final String _990008 = "990008";
    private static final String _990009 = "990009";

    private static final String SUBCLASSALL3 = "333333";
    private static final String SUBCLASSALL5 = "555555";
    private static final String SUBCLASSALL = "999999";

    private static final Log log = LogFactory.getLog(KNJD107V.class);

    private Param _param;

    private static final String SSEMESTER = "1";
    private boolean _hasData = false;

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) {
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null; //Databaseクラスを継承したクラス
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);
            
            
            final List students = Student.load(db2, _param);
            
            final Map classcdSubclasscdMap = getClasscdSubclasscdMap(students);

            if ("1".equals(_param._csv)) {
                outputCsv(response, students, classcdSubclasscdMap);

            } else {
                response.setContentType("application/pdf");

                svf.VrInit();                             //クラスの初期化
                svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定
                //SVF出力
                printMain(svf, students, classcdSubclasscdMap);
            }
        } catch (final Exception e) {
            log.error("exception!", e);
        } finally {
            if ("1".equals(_param._csv)) {
            } else {
                svf.VrQuit();
            }
            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }

    }// doGetの括り

    private void outputCsv(final HttpServletResponse response, final List students, final Map classcdSubclasscdMap) {
        final String nendo = nao_package.KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度";
        final String title;
        String output;
        if ("2".equals(_param._outputDiv)) {
            title = nendo + "クラス別出欠状況一覧（欠課・欠席）";
            output = output2(students, classcdSubclasscdMap, title);
        } else {
            title = nendo + "クラス別成績推移一覧表";
            output = output1(students, classcdSubclasscdMap, title);
        }
        OutputStream os = null;
        try {
            final byte[] data = output.getBytes("MS932");
            final String filename = title + ".csv";
            response.setContentType("text/octet-stream");
            response.setHeader("Accept-Ranges", "none");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + new String(filename.getBytes("MS932"), "ISO8859-1") + "\"");
            response.setHeader("Content-Transfer-Encoding", "binary");
            response.setHeader("Content-Length", String.valueOf(data.length));

            os = new BufferedOutputStream(response.getOutputStream());
            os.write(data);
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            try {
                if (null != os) {
                    os.close();
                }
            } catch (Exception ex) {
            }
        }
    }
    
    private String join(final List columns, final String spl1) {
        final StringBuffer stb = new StringBuffer();
        String spl = "";
        for (final Iterator it = columns.iterator(); it.hasNext();) {
            final String s = (String) it.next();
            stb.append(spl).append(StringUtils.defaultString(s));
            spl = spl1;
        }
        return stb.toString();
    }
    
    private String columnListListToData(final List columnListList) {
        final List rtn = new ArrayList();
        for (int i = 0; i < columnListList.size(); i++) {
            final List columnList = (List) columnListList.get(i);
            rtn.add(join(columnList, ","));
        }
        return join(rtn, "\n");
    }

    private String output1(final List studentList, final Map classcdSubclasscdMap, final String title) {
        final List lines = new ArrayList();
        lines.add(Arrays.asList(new String[] {"", title})); // 年度
        lines.add(Arrays.asList(new String[] {getThisYearHrname(studentList, _param._year), "", "担任:", getThisYearStaffname(studentList, _param._year)})); // 担任

        List colsClass = new ArrayList();
        List colsSubclass = new ArrayList();
        colsClass.addAll(Arrays.asList(new String[] {"", "", ""}));
        colsSubclass.addAll(Arrays.asList(new String[] {"", "", ""}));
        for (final Iterator it = classcdSubclasscdMap.keySet().iterator(); it.hasNext();) {
            final String classcd = (String) it.next();
            final List subclasscdList = (List) classcdSubclasscdMap.get(classcd);
            for (int si = 0; si < subclasscdList.size(); si++) {
                final String subclasscd = (String) subclasscdList.get(si);
                final String subclassname = (String) _param._subclassNameMap.get(subclasscd);
                String classname = null;
                if (si == 0) {
                    classname = (String) _param._classNameMap.get(classcd);
                }
                colsClass.add(classname); // 教科
                colsSubclass.add(subclassname); // 科目
            }
        }
        colsSubclass.addAll(Arrays.asList(new String[] {"総点", "平均", "序列"}));
        lines.add(colsClass);
        lines.add(colsSubclass);
        
        for (int i = 0; i < studentList.size(); i++) {
            final Student student = (Student) studentList.get(i);

            for (int j = 0; j < student._regd.size(); j++) {
                final Student.Regd regd = (Student.Regd) student._regd.get(j);

                final List lineSeiseki = new ArrayList();
                final List lineHyotei = new ArrayList();

                for (int k = 0; k < 2; k++) {
                    Map rankMap = Collections.EMPTY_MAP;
                    List line = null;
                    if (k == 0) {
                        line = lineSeiseki;
                        String name = null;
                        if (j == 0) {
                            name = student._name; // 生徒名
                        }
                        line.add(name);
                        line.add(KenjaProperties.gengou(Integer.parseInt(regd._year)) + "年度　" + StringUtils.defaultString(regd._hrNameAttendno) + " " + StringUtils.defaultString(regd._staffname));
                        line.add("学年成績"); // 成績名称
                        rankMap = regd._rankVDat;
                    } else if (k == 1) {
                        line = lineHyotei;
                        line.add(null);
                        line.add(null);
                        line.add("評定"); // 成績名称
                        rankMap = regd._rankDat;
                    }
                    
                    for (final Iterator it = classcdSubclasscdMap.keySet().iterator(); it.hasNext();) {
                        final String classcd = (String) it.next();
                        final List subclasscdList = (List) classcdSubclasscdMap.get(classcd);
                        for (int si = 0; si < subclasscdList.size(); si++) {
                            final String subclasscd = (String) subclasscdList.get(si);
                            
                            String ss = null;
                            if (null != rankMap.get(subclasscd)) {
                                final Rank r = (Rank) rankMap.get(subclasscd);
                                ss = r._score; // 成績
                            }
                            line.add(ss);
                        }
                    }
                    
                    String ss = null, savg = null, sr = null;
                    if (null != rankMap.get(SUBCLASSALL)) {
                        final Rank r = (Rank) rankMap.get(SUBCLASSALL);
                        ss = r._score; // 総点
                        savg = r.avgStr(); // 平均
                        sr = r.rank(_param); // 序列
                    }
                    line.add(ss);
                    line.add(savg);
                    line.add(sr);
                    lines.add(line);
                }
            }
        }
        return columnListListToData(lines);
    }

    private String output2(final List studentList, final Map classcdSubclasscdMap, final String title) {
        
        final List lines = new ArrayList();
        lines.add(Arrays.asList(new String[] {"", title})); // 年度
        lines.add(Arrays.asList(new String[] {getThisYearHrname(studentList, _param._year), "", "担任:", getThisYearStaffname(studentList, _param._year)})); // 担任
        
        final List colsClass = new ArrayList();
        final List colsSubclass = new ArrayList();
        colsClass.addAll(Arrays.asList(new String[] {"", ""}));
        colsSubclass.addAll(Arrays.asList(new String[] {"", ""}));
        for (final Iterator it = classcdSubclasscdMap.keySet().iterator(); it.hasNext();) {
            final String classcd = (String) it.next();
            final List subclasscdList = (List) classcdSubclasscdMap.get(classcd);
            for (int si = 0; si < subclasscdList.size(); si++) {
                final String subclasscd = (String) subclasscdList.get(si);
                final String subclassname = (String) _param._subclassNameMap.get(subclasscd);
                String classname = null;
                if (si == 0) {
                    classname = (String) _param._classNameMap.get(classcd); // 教科
                }
                colsClass.add(classname);
                colsSubclass.add(subclassname); // 科目
            }
        }
        colsSubclass.addAll(Arrays.asList(new String[] {"授業日数", "出停・忌引", "留学日数", "出席すべき日数", "欠席日数", "出席日数", "備考"}));
        lines.add(colsClass);
        lines.add(colsSubclass);
        
        for (int i = 0; i < studentList.size(); i++) {
            final Student student = (Student) studentList.get(i);

            for (int j = 0; j < student._regd.size(); j++) {
                final Student.Regd regd = (Student.Regd) student._regd.get(j);

                final List line = new ArrayList();

                String name = null;
                if (j == 0) {
                    name = student._name; // 生徒名
                }
                line.add(name);
                line.add(KenjaProperties.gengou(Integer.parseInt(regd._year)) + "年度　" + StringUtils.defaultString(regd._hrNameAttendno) + " " + StringUtils.defaultString(regd._staffname));
                for (final Iterator it = classcdSubclasscdMap.keySet().iterator(); it.hasNext();) {
                    final String classcd = (String) it.next();
                    final List subclasscdList = (List) classcdSubclasscdMap.get(classcd);
                    for (int si = 0; si < subclasscdList.size(); si++) {
                        final String subclasscd = (String) subclasscdList.get(si);
                        
                        String ss = null;
                        if (null != regd._attendSubclass.get(subclasscd)) {
                            final String sick = (String) regd._attendSubclass.get(subclasscd);
                            ss = sick; // 欠課
                        }
                        line.add(ss);
                    }
                }
                
                String v1 = null, v2 = null, v3 = null, v4 = null, v5 = null, v6 = null, v7 = null;
                if (null != regd._attendRec.get(regd._year)) {
                    final AttendRec ar = (AttendRec) regd._attendRec.get(regd._year);
                    
                    v1 = ar._attend1; // 授業日数
                    v2 = ar._suspMour; // 出停・忌引
                    v3 = ar._abroad; // 留学目的
                    v4 = ar._requirepresent; // 出席すべき日数
                    v5 = ar._attend6; // 欠席日数
                    v6 = ar._present; // 出席日数
                    v7 = regd._attendRemark; // 備考
                }
                line.addAll(Arrays.asList(new String[] {v1, v2, v3, v4, v5, v6, v7}));
                
                lines.add(line);
            }
        }
        return columnListListToData(lines);
    }

    private void printMain(final Vrw32alp svf, final List students, final Map classcdSubclasscdMap) {
        final int studentLines = "2".equals(_param._outputDiv) ? 5 : 3;
        final List pageList = getPageList(students, studentLines);
        for (final Iterator it = pageList.iterator(); it.hasNext();) {
            final List pageStudents = (List) it.next();
            if ("2".equals(_param._outputDiv)) {
                printPage2(svf, pageStudents, classcdSubclasscdMap);
            } else {
                printPage1(svf, pageStudents, classcdSubclasscdMap);
            }
        }
    }

    private Map getClasscdSubclasscdMap(final List students) {
        final Set subclassSet = new TreeSet();
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            for (final Iterator itr = student._regd.iterator(); itr.hasNext();) {
                final Student.Regd regd = (Student.Regd) itr.next();
                subclassSet.addAll(regd._rankDat.keySet());
                subclassSet.addAll(regd._rankVDat.keySet());
            }
        }
        subclassSet.remove(SUBCLASSALL);
        
        final Map classcdSubclasscdMap = new TreeMap();
        for (final Iterator it = subclassSet.iterator(); it.hasNext();) {
            final String subclasscd = (String) it.next();
            final String classcd;
            if ("1".equals(_param._useCurriculumcd)) {
                final String[] split = StringUtils.split(subclasscd, "-");
                classcd = split[0] + "-" + split[1];
            } else {
                classcd = subclasscd.substring(0, 2);
            }
            if (null == classcdSubclasscdMap.get(classcd)) {
                classcdSubclasscdMap.put(classcd, new ArrayList());
            }
            ((List) classcdSubclasscdMap.get(classcd)).add(subclasscd);
        }
        return classcdSubclasscdMap;
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
    
    /**
     *  指定バイト数の部分文字列
     */
    private static String substringWithMS932Byte(final String str, final int b) {
        if (null != str) {
            int buf = 0;
            for (int i = 0; i < str.length(); i++) {
                final String s = str.substring(i, i + 1);
                buf += getMS932ByteLength(s);
                if (buf > b) {
                    return str.substring(i);
                }
            }
        }
        return null;
    }
    
    /**
     *  文字数を取得
     */
    private static List splitWithMS932Byte(final String str, final int len) {
        final List list = new ArrayList();
        int buf = 0;
        int start = 0;
        if (null != str) {
            for (int i = 0; i < str.length(); i++) {
                final String s = str.substring(i, i + 1);
                buf += getMS932ByteLength(s);
                if (buf > len) {
                    list.add(str.substring(start, i));
                    start = i;
                    buf = 0;
                }
            }
        }
        if (0 != buf) {
            list.add(str.substring(start));
        }
        return list;
    }
    
    private static List getPageList(final List studentList, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }
    
    private static String getThisYearHrname(final List studentList, final String year) {
        for (int i = 0; i < studentList.size(); i++) {
            final Student student = (Student) studentList.get(i);
            for (int j = 0; j < student._regd.size(); j++) {
                final Student.Regd regd = (Student.Regd) student._regd.get(j);
                if (regd._year.equals(year)) {
                    return regd._hrName;
                }
            }
        }
        return null;
    }
    
    private static String getThisYearStaffname(final List studentList, final String year) {
        for (int i = 0; i < studentList.size(); i++) {
            final Student student = (Student) studentList.get(i);
            for (int j = 0; j < student._regd.size(); j++) {
                final Student.Regd regd = (Student.Regd) student._regd.get(j);
                if (regd._year.equals(year)) {
                    return regd._staffname;
                }
            }
        }
        return null;
    }
    
    public void printPage1(final Vrw32alp svf, final List studentList, final Map classcdSubclasscdMap) {
        final String form = "KNJD107V_1.frm";
        svf.VrSetForm(form, 4);
        
        final String nendo = nao_package.KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度";
        svf.VrsOut("SCHOOL_NAME", "クラス別成績推移一覧表"); // 学校名
        svf.VrsOut("YEAR", nendo); // 年度
        svf.VrsOut("HR_NAME_1", getThisYearHrname(studentList, _param._year)); // 年組
        svf.VrsOut("TEACHER", getThisYearStaffname(studentList, _param._year)); // 担任
        
        final String SEISEKI = "1";
        final String HYOTEI = "2";

        for (int i = 0; i < studentList.size(); i++) {
            final Student student = (Student) studentList.get(i);
            
            final int line = i + 1;
            final int namelen = getMS932ByteLength(student._name);
            svf.VrsOutn("NAME" + (namelen > 30 ? "3" : namelen > 20 ? "2" : "1"), line, student._name); // 生徒名

            for (int j = 0; j < student._regd.size(); j++) {
                final Student.Regd regd = (Student.Regd) student._regd.get(j);

                final int line2 = i * 4 + j + 1;
                svf.VrsOutn("NENDO", line2, KenjaProperties.gengou(Integer.parseInt(regd._year)) + "年度"); // 年組番号
                svf.VrsOutn("HR_NAME", line2, regd._hrNameAttendno); // 年組番号
                svf.VrsOutn("TEACHER" + (namelen > 30 ? "3" : namelen > 20 ? "2" : "1"), line2, regd._staffname); // 担任

                String k;
                k = SEISEKI;
                svf.VrsOutn("REC_NAME" + k, line2, "学年成績"); // 成績名称
                if (null != regd._rankVDat.get(SUBCLASSALL)) {
                    final Rank r = (Rank) regd._rankVDat.get(SUBCLASSALL);
                    svf.VrsOutn("TOTAL" + k, line2, r._score); // 総点
                    svf.VrsOutn("AVERAGE" + k, line2, r.avgStr()); // 平均
                    svf.VrsOutn("RANK" + k, line2, r.rank(_param)); // 序列
                }
                k = HYOTEI;
                svf.VrsOutn("REC_NAME" + k, line2, "評定"); // 成績名称
                if (null != regd._rankDat.get(SUBCLASSALL)) {
                    final Rank r = (Rank) regd._rankDat.get(SUBCLASSALL);
                    svf.VrsOutn("TOTAL" + k, line2, r._score); // 総点
                    svf.VrsOutn("AVERAGE" + k, line2, r.avgStr()); // 平均
                    svf.VrsOutn("RANK" + k, line2, r.rank(_param)); // 序列
                }
            }
        }

        int subline = 0;
        for (final Iterator it = classcdSubclasscdMap.keySet().iterator(); it.hasNext();) {
            final String classcd = (String) it.next();
            final List subclasscdList = (List) classcdSubclasscdMap.get(classcd);
            final String classname = (String) _param._classNameMap.get(classcd);
            final int classnamelen = getMS932ByteLength(classname);
            for (int si = 0; si < subclasscdList.size(); si++) {
                final String subclasscd = (String) subclasscdList.get(si);
                svf.VrsOut("CLASSCD1", classcd); // 教科コード（マスク）
                if (classnamelen > si * 2) {
                    svf.VrsOut("CLASS_1_1", substringWithMS932Byte(classname, si * 2)); // 教科
                }
                final String subclassname = (String) _param._subclassNameMap.get(subclasscd);
                if (null != subclassname) {
                    if (subclassname.length() <= 5) {
                        svf.VrsOut("SUBCLASS1_1", subclassname); // 科目
                    } else if (subclassname.length() <= 8) {
                        svf.VrsOut("SUBCLASS1_2", subclassname); // 科目
                    } else {
                        svf.VrsOut("SUBCLASS2_1", subclassname.substring(0, 8)); // 科目
                        svf.VrsOut("SUBCLASS2_2", subclassname.substring(8)); // 科目
                    }
                }
                
                for (int i = 0; i < studentList.size(); i++) {
                    final Student student = (Student) studentList.get(i);
                    
                    for (int j = 0; j < student._regd.size(); j++) {
                        final Student.Regd regd = (Student.Regd) student._regd.get(j);
                        final int line2 = i * 4 + j + 1;
                        if (null != regd._rankVDat.get(subclasscd)) {
                            final Rank r = (Rank) regd._rankVDat.get(subclasscd);
                            svf.VrsOutn("RESULT1_" + SEISEKI + (getMS932ByteLength(r._score) > 2 ? "_2" : ""), line2, r._score); // 成績
                        }
                        if (null != regd._rankDat.get(subclasscd)) {
                            final Rank r = (Rank) regd._rankDat.get(subclasscd);
                            svf.VrsOutn("RESULT1_" + HYOTEI + (getMS932ByteLength(r._score) > 2 ? "_2" : ""), line2, r._score); // 成績
                        }
                    }
                }

                svf.VrEndRecord();
                subline += 1;
            }
        }
        final int maxSubclassLine = 45;
        for (; subline < maxSubclassLine; subline += 1) {
            svf.VrsOut("CLASSCD1", String.valueOf(subline)); // 教科コード（マスク）
            svf.VrEndRecord();
        }
        _hasData = true;
    }
    
    public void printPage2(final Vrw32alp svf, final List studentList, final Map classcdSubclasscdMap) {
        final String form = "KNJD107V_2.frm";
        svf.VrSetForm(form, 4);
        
        final String nendo = nao_package.KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度";
        svf.VrsOut("SCHOOL_NAME", "クラス別出欠状況一覧（欠課・欠席）"); // 学校名
        svf.VrsOut("YEAR", nendo); // 年度
        svf.VrsOut("HR_NAME_1", getThisYearHrname(studentList, _param._year)); // 年組
        svf.VrsOut("TEACHER", getThisYearStaffname(studentList, _param._year)); // 担任
        
        for (int i = 0; i < studentList.size(); i++) {
            final Student student = (Student) studentList.get(i);
            
            final int line = i + 1;
            final int namelen = getMS932ByteLength(student._name);
            svf.VrsOutn("NAME" + (namelen > 30 ? "3" : namelen > 20 ? "2" : "1"), line, student._name); // 生徒名

            for (int j = 0; j < student._regd.size(); j++) {
                final Student.Regd regd = (Student.Regd) student._regd.get(j);

                final int line2 = i * 4 + j + 1;
                svf.VrsOutn("HR_NAME", line2, KenjaProperties.gengou(Integer.parseInt(regd._year)) + "年度" + StringUtils.defaultString(regd._hrNameAttendno)); // 年組番号
                svf.VrsOutn("TEACHER" + (namelen > 30 ? "3" : namelen > 20 ? "2" : "1"), line2, regd._staffname); // 担任

                if (null != regd._attendRec.get(regd._year)) {
                    final AttendRec ar = (AttendRec) regd._attendRec.get(regd._year);
                    
                    svf.VrsOutn("LESSON1", line2, ar._attend1); // 授業日数
                    svf.VrsOutn("SUSPEND1", line2, ar._suspMour); // 出停・忌引
                    svf.VrsOutn("BROAD1", line2, ar._abroad); // 留学目的
                    svf.VrsOutn("REC_PRESENT1", line2, ar._requirepresent); // 出席すべき日数
                    svf.VrsOutn("ABSENCE1", line2, ar._attend6); // 欠席日数
                    svf.VrsOutn("PRESENT1", line2, ar._present); // 出席日数
                    
                    final List rem = splitWithMS932Byte(regd._attendRemark, 40);
                    for (int k = 0; k < rem.size(); k++) {
                        svf.VrsOutn("REM" + String.valueOf(k + 1), line2, (String) rem.get(k)); // 備考
                    }
                }
            }
        }

        int subline = 0;
        for (final Iterator it = classcdSubclasscdMap.keySet().iterator(); it.hasNext();) {
            final String classcd = (String) it.next();
            final List subclasscdList = (List) classcdSubclasscdMap.get(classcd);
            final String classname = (String) _param._classNameMap.get(classcd);
            final int classnamelen = getMS932ByteLength(classname);
            for (int si = 0; si < subclasscdList.size(); si++) {
                final String subclasscd = (String) subclasscdList.get(si);
                svf.VrsOut("CLASSCD1", classcd); // 教科コード（マスク）
                if (classnamelen > si * 2) {
                    svf.VrsOut("CLASS_1_1", substringWithMS932Byte(classname, si * 2)); // 教科
                }
                final String subclassname = (String) _param._subclassNameMap.get(subclasscd);
                if (null != subclassname) {
                    if (subclassname.length() <= 5) {
                        svf.VrsOut("SUBCLASS1_1", subclassname); // 科目
                    } else if (subclassname.length() <= 8) {
                        svf.VrsOut("SUBCLASS1_2", subclassname); // 科目
                    } else {
                        svf.VrsOut("SUBCLASS2_1", subclassname.substring(0, 8)); // 科目
                        svf.VrsOut("SUBCLASS2_2", subclassname.substring(8)); // 科目
                    }
                }
                
                for (int i = 0; i < studentList.size(); i++) {
                    final Student student = (Student) studentList.get(i);
                    
                    for (int j = 0; j < student._regd.size(); j++) {
                        final Student.Regd regd = (Student.Regd) student._regd.get(j);
                        final int line2 = i * 4 + j + 1;
                        if (null != regd._attendSubclass.get(subclasscd)) {
                            final String sick = (String) regd._attendSubclass.get(subclasscd);
                            svf.VrsOutn("RESULT1_1" + (getMS932ByteLength(sick) > 2 ? "_2" : ""), line2, sick); // 欠課
                        }
                    }
                }

                svf.VrEndRecord();
                subline += 1;
            }
        }
        final int maxSubclassLine = 45;
        for (; subline < maxSubclassLine; subline += 1) {
            svf.VrsOut("CLASSCD1", String.valueOf(subline)); // 教科コード（マスク）
            svf.VrEndRecord();
        }
    }

    private static class Student {
        final String _schregno;
        final String _name;
        final List _regd = new ArrayList();
        
        Student(
            final String schregno,
            final String name
        ) {
            _schregno = schregno;
            _name = name;
        }
        
        private Regd getRegd(final String year) {
            for (final Iterator it = _regd.iterator(); it.hasNext();) {
                Regd regd = (Regd) it.next();
                if (regd._year.equals(year)) {
                    return regd;
                }
            }
            return null;
        }
        
        private static class Regd {
            final String _year;
            final String _semester;
            final String _hrName;
            final String _hrNameAttendno;
            final String _staffname;
            final Map _rankDat = new HashMap();
            final Map _rankVDat = new HashMap();
            final Map _attendSubclass = new HashMap();
            final Map _attendRec = new HashMap();
            String _attendRemark;
            public Regd(String year, String semester, String hrName, String hrNameAttendno, String staffname) {
                _year = year;
                _semester = semester;
                _hrName = hrName;
                _hrNameAttendno = hrNameAttendno;
                _staffname = staffname;
            }
        }

        static Student getStudent(final String schregno, final List list) {
            for (final Iterator it = list.iterator(); it.hasNext();) {
                Student student = (Student) it.next();
                if (student._schregno.equals(schregno)) {
                    return student;
                }
            }
            return null;
        }
        
        public static void setScore(final DB2UDB db2, final Param param, final List students) {
            final String sql1 = " SELECT * FROM RECORD_RANK_SDIV_DAT WHERE SEMESTER = '" + SEMEALL + "' AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _990009 + "' AND SCHREGNO = ? AND SUBCLASSCD NOT IN ('333333','555555','99999A','99999B') ";
            final String sql2 = " SELECT * FROM RECORD_RANK_SDIV_DAT WHERE SEMESTER = '" + SEMEALL + "' AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _990008 + "' AND SCHREGNO = ? AND SUBCLASSCD NOT IN ('333333','555555','99999A','99999B') ";
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql1);
                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String key = param.getSubclassKey(rs);
                        final Regd regd = student.getRegd(rs.getString("YEAR"));
                        if (null == regd) {
                            continue;
                        }
                        regd._rankDat.put(key, new Rank(rs, param));
                    }
                    DbUtils.closeQuietly(rs);
                }
                DbUtils.closeQuietly(ps);

                ps = db2.prepareStatement(sql2);
                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String key = param.getSubclassKey(rs);
                        final Regd regd = student.getRegd(rs.getString("YEAR"));
                        if (null == regd) {
                            continue;
                        }
                        regd._rankVDat.put(key, new Rank(rs, param));
                    }
                    DbUtils.closeQuietly(rs);
                }
                DbUtils.closeQuietly(ps);
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private static void setAttendSubclass(
                final DB2UDB db2,
                final Param param,
                final List students
        ) {
            
            KNJSchoolMst knjSchoolMst = null;
            try {
                knjSchoolMst = new KNJSchoolMst(db2, param._year);
            } catch (SQLException e) {
                log.warn("exception!", e);
            }

            
            String z010 = param.setNameMst(db2, "Z010", "00", "NAME1");
            KNJDefineCode definecode = new KNJDefineCode();
            definecode.defineCode(db2, param._year);
            KNJDefineSchool defineSchoolCode = new KNJDefineSchool();
            defineSchoolCode.defineCode(db2, param._year);         //各学校における定数等設定

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final Map attendSemAllMap = AttendAccumulate.getAttendSemesMap(db2, z010, param._year);
                final String periodInState = AttendAccumulate.getPeiodValue(db2, definecode, param._year, SSEMESTER, param._semester);

                final Map yearDate = param.getYearDate(db2, param._year);
                final String yearSdate = (String) yearDate.get("SDATE");
                
                final Map hasuuMap = AttendAccumulate.getHasuuMap(attendSemAllMap, yearSdate, param._date);
                final boolean semesFlg = ((Boolean) hasuuMap.get("semesFlg")).booleanValue();
                
                final String sql = AttendAccumulate.getAttendSubclassSql(
                        semesFlg,
                        defineSchoolCode,
                        knjSchoolMst,
                        param._year,
                        SSEMESTER,
                        param._semester,
                        (String) hasuuMap.get("attendSemesInState"),
                        periodInState,
                        (String) hasuuMap.get("befDayFrom"),
                        (String) hasuuMap.get("befDayTo"),
                        (String) hasuuMap.get("aftDayFrom"),
                        (String) hasuuMap.get("aftDayTo"),
                        param._grade,
                        param._hrClass,
                        "?",
                        param._useCurriculumcd,
                        param._useVirus,
                        param._useKoudome);
                
                ps = db2.prepareStatement(sql);
                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    
                    for (final Iterator itr = student._regd.iterator(); itr.hasNext();) {
                        final Regd regd = (Regd) itr.next();
                        if (!regd._year.equals(param._year)) {
                            continue;
                        }
                        ps.setString(1, student._schregno);
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                                continue;
                            }
                            regd._attendSubclass.put(rs.getString("SUBCLASSCD"), rs.getString("SICK2"));
                        }
                    }
                }
                
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            try {
                final Set years = new HashSet();
                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    for (final Iterator itr = student._regd.iterator(); itr.hasNext();) {
                        final Regd regd = (Regd) itr.next();
                        if (regd._year.equals(param._year)) {
                            continue;
                        }
                        years.add(regd._year);
                    }
                }
                
                for (final Iterator ity = years.iterator(); ity.hasNext();) {
                    final String year = (String) ity.next();
                    log.fatal(" year = " + year);
                    
                    definecode.defineCode(db2, year);
                    final Map attendSemAllMap = AttendAccumulate.getAttendSemesMap(db2, z010, year);

                    final Map yearDate = param.getYearDate(db2, year);
                    final String yearSdate = (String) yearDate.get("SDATE");
                    final String yearEdate = (String) yearDate.get("EDATE");
                    
                    final Map hasuuMap = AttendAccumulate.getHasuuMap(attendSemAllMap, yearSdate, yearEdate);
                    
                    final String sql = AttendAccumulate.getAttendSubclassSql(
                            true,
                            defineSchoolCode,
                            knjSchoolMst,
                            year,
                            SSEMESTER,
                            SEMEALL,
                            (String) hasuuMap.get("attendSemesInState"),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            "?",
                            param._useCurriculumcd,
                            param._useVirus,
                            param._useKoudome);
                    
                    ps = db2.prepareStatement(sql);
                    for (final Iterator it = students.iterator(); it.hasNext();) {
                        final Student student = (Student) it.next();
                        
                        for (final Iterator itr = student._regd.iterator(); itr.hasNext();) {
                            final Regd regd = (Regd) itr.next();
                            if (!regd._year.equals(year)) {
                                continue;
                            }
                            ps.setString(1, student._schregno);
                            rs = ps.executeQuery();
                            while (rs.next()) {
                                if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                                    continue;
                                }
                                regd._attendSubclass.put(rs.getString("SUBCLASSCD"), rs.getString("SICK2"));
                            }
                        }
                    }
                }

            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT  T1.YEAR, ANNUAL");
                stb.append("       , VALUE(CLASSDAYS,0) AS CLASSDAYS"); // 授業日数
                stb.append("       , CASE WHEN S1.SEM_OFFDAYS = '1' ");
                stb.append("              THEN VALUE(CLASSDAYS,0) ");
                stb.append("              ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) ");
                stb.append("              END AS ATTEND_1"); // 授業日数 - (休学日数) [- 留学日数]
                stb.append("       , VALUE(SUSPEND,0) + VALUE(MOURNING,0) AS SUSP_MOUR"); // 出停・忌引
                stb.append("       , VALUE(SUSPEND,0) AS SUSPEND"); // 出停:2
                stb.append("       , VALUE(MOURNING,0) AS MOURNING"); // 忌引:3
                stb.append("       , VALUE(ABROAD,0) AS ABROAD"); // 留学:4
                stb.append("       , CASE WHEN S1.SEM_OFFDAYS = '1' ");
                stb.append("              THEN VALUE(REQUIREPRESENT,0) + VALUE(OFFDAYS,0) ");
                stb.append("              ELSE VALUE(REQUIREPRESENT,0) ");
                stb.append("              END AS REQUIREPRESENT"); // 要出席日数:5
                stb.append("       , CASE WHEN S1.SEM_OFFDAYS = '1' ");
                stb.append("              THEN VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) + VALUE(OFFDAYS,0) ");
                stb.append("              ELSE VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) ");
                stb.append("              END AS ATTEND_6"); // 病欠＋事故欠（届・無）:6
                stb.append("       , VALUE(PRESENT,0) AS PRESENT"); // 出席日数:7
                stb.append(" FROM(");
                stb.append("      SELECT  YEAR, ANNUAL");
                stb.append("            , SUM(CLASSDAYS) AS CLASSDAYS");
                stb.append("            , SUM(OFFDAYS) AS OFFDAYS");
                stb.append("            , SUM(ABSENT) AS ABSENT");
                stb.append("            , SUM(SUSPEND) AS SUSPEND");
                stb.append("            , SUM(MOURNING) AS MOURNING");
                stb.append("            , SUM(ABROAD) AS ABROAD");
                stb.append("            , SUM(REQUIREPRESENT) AS REQUIREPRESENT");
                stb.append("            , SUM(SICK) AS SICK");
                stb.append("            , SUM(ACCIDENTNOTICE) AS ACCIDENTNOTICE");
                stb.append("            , SUM(NOACCIDENTNOTICE) AS NOACCIDENTNOTICE");
                stb.append("            , SUM(PRESENT) AS PRESENT");
                stb.append("       FROM   SCHREG_ATTENDREC_DAT");
                stb.append("       WHERE  SCHREGNO = ? AND YEAR = ? AND SCHOOLCD <> '1' ");
                stb.append("       GROUP BY YEAR, ANNUAL");
                stb.append("     )T1 ");
                stb.append("     LEFT JOIN SCHOOL_MST S1 ON S1.YEAR = T1.YEAR ");
                
                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);
                    
                    for (final Iterator itr = student._regd.iterator(); itr.hasNext();) {
                        final Regd regd = (Regd) itr.next();
                        ps.setString(2, regd._year);

                        rs = ps.executeQuery();
                        while (rs.next()) {
                            final String year = rs.getString("YEAR");
                            final String annual = rs.getString("ANNUAL");
                            final String classdays = rs.getString("CLASSDAYS");
                            final String attend1 = rs.getString("ATTEND_1");
                            final String suspMour = rs.getString("SUSP_MOUR");
                            final String suspend = rs.getString("SUSPEND");
                            final String mourning = rs.getString("MOURNING");
                            final String abroad = rs.getString("ABROAD");
                            final String requirepresent = rs.getString("REQUIREPRESENT");
                            final String attend6 = rs.getString("ATTEND_6");
                            final String present = rs.getString("PRESENT");
                            final AttendRec attendrec = new AttendRec(year, annual, classdays, attend1, suspMour, suspend, mourning, abroad, requirepresent, attend6, present);
                            regd._attendRec.put(year, attendrec);
                        }
                    }
                }
                
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            try {
                final String sql;
                sql = "SELECT  YEAR, ATTENDREC_REMARK"
                    + " FROM HTRAINREMARK_DAT"
                    + " WHERE SCHREGNO = ? "
                    + " AND YEAR = ? "
                    ;

                ps = db2.prepareStatement(sql);
                
                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);
                    
                    for (final Iterator itr = student._regd.iterator(); itr.hasNext();) {
                        final Regd regd = (Regd) itr.next();
                        ps.setString(2, regd._year);

                        rs = ps.executeQuery();

                        if (rs.next()) {
                            regd._attendRemark = rs.getString("ATTENDREC_REMARK");
                        }
                        DbUtils.closeQuietly(rs);
                    }
                }
            } catch (final Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        public static List load(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    if (null == getStudent(schregno, list)) {
                        final String name = rs.getString("NAME");
                        Student student = new Student(schregno, name);
                        list.add(student);
                    }
                    Student student = getStudent(schregno, list);

                    final String year = rs.getString("YEAR");
                    final String semester = rs.getString("SEMESTER");
                    final String attendno = rs.getString("ATTENDNO");
                    final String hrName = StringUtils.defaultString(rs.getString("HR_NAME"));
                    final String hrNameAttendno = hrName + (NumberUtils.isDigits(attendno) ? String.valueOf(Integer.parseInt(attendno)) + "番" : StringUtils.defaultString(attendno));
                    final String staffname = rs.getString("STAFFNAME");
                    student._regd.add(new Student.Regd(year, semester, hrName, hrNameAttendno, staffname));
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (final Iterator it = list.iterator(); it.hasNext();) { // 在籍のリストが4を超える場合、前のデータを削除しておく
                final Student student = (Student) it.next();
                if (student._regd.size() > 4) {
                    for (int i = 0; i < student._regd.size() - 4; i++) {
                        student._regd.remove(0);
                    }
                }
            }
            setScore(db2, param, list);
            if ("2".equals(param._outputDiv)) {
                setAttendSubclass(db2, param, list);
            }
            return list;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHREGNOS AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, T1.YEAR, T1.SEMESTER, T1.ATTENDNO AS REGD_ATTENDNO ");
            stb.append("     FROM SCHREG_REGD_DAT T1 ");
            stb.append("     WHERE ");
            stb.append("         T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append("         AND T1.YEAR = '" + param._year + "' ");
            stb.append("         AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("         AND T1.GRADE = '" + param._grade + "' ");
            stb.append("         AND T1.HR_CLASS = '" + param._hrClass + "' ");
            stb.append(" ), REGD AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, T1.YEAR, T1.SEMESTER ");
            stb.append("     FROM SCHREGNOS T1 ");
            stb.append("     UNION ALL ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, T2.YEAR, MAX(T2.SEMESTER) AS SEMESTER ");
            stb.append("     FROM SCHREGNOS T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T2.YEAR < '" + param._year + "' ");
            stb.append("     GROUP BY ");
            stb.append("         T1.SCHREGNO, T2.YEAR ");
            stb.append(" ) ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T2.NAME, ");
            stb.append("         T0.YEAR, ");
            stb.append("         T0.SEMESTER, ");
            stb.append("         T4.ATTENDNO, ");
            stb.append("         T5.HR_NAME, ");
            stb.append("         T6.STAFFNAME ");
            stb.append("     FROM REGD T0 ");
            stb.append("     INNER JOIN SCHREGNOS T1 ON T1.SCHREGNO = T0.SCHREGNO ");
            stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T0.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT T4 ON T4.SCHREGNO = T0.SCHREGNO ");
            stb.append("         AND T4.YEAR = T0.YEAR ");
            stb.append("         AND T4.SEMESTER = T0.SEMESTER ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT T5 ON T5.YEAR = T4.YEAR ");
            stb.append("         AND T5.SEMESTER = T4.SEMESTER ");
            stb.append("         AND T5.GRADE = T4.GRADE ");
            stb.append("         AND T5.HR_CLASS = T4.HR_CLASS ");
            stb.append("     LEFT JOIN STAFF_MST T6 ON T6.STAFFCD = T5.TR_CD1 ");
            stb.append(" ORDER BY ");
            stb.append("     T1.REGD_ATTENDNO, T0.YEAR ");
            return stb.toString();
        }
    }
    
    private static class Rank {
        final String _year;
        final String _semester;
        final String _testkindcd;
        final String _testitemcd;
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _schregno;
        final String _score;
        final String _avg;
        final String _gradeRank;
        final String _gradeAvgRank;
        final String _gradeDeviation;
        final String _gradeDeviationRank;
        final String _classRank;
        final String _classAvgRank;
        final String _classDeviation;
        final String _classDeviationRank;
        final String _courseRank;
        final String _courseAvgRank;
        final String _courseDeviation;
        final String _courseDeviationRank;
        final String _majorRank;
        final String _majorAvgRank;
        final String _majorDeviation;
        final String _majorDeviationRank;

        Rank(final ResultSet rs, final Param param) throws SQLException {
            _year = rs.getString("YEAR");
            _semester = rs.getString("SEMESTER");
            _testkindcd = rs.getString("TESTKINDCD");
            _testitemcd = rs.getString("TESTITEMCD");
            if ("1".equals(param._useCurriculumcd)) {
                _classcd = rs.getString("CLASSCD");
                _schoolKind = rs.getString("SCHOOL_KIND");
                _curriculumCd = rs.getString("CURRICULUM_CD");
            } else {
                _classcd = null;
                _schoolKind = null;
                _curriculumCd = null;
            }
            _subclasscd = rs.getString("SUBCLASSCD");
            _schregno = rs.getString("SCHREGNO");
            _score = rs.getString("SCORE");
            _avg = rs.getString("AVG");
            _gradeRank = rs.getString("GRADE_RANK");
            _gradeAvgRank = rs.getString("GRADE_AVG_RANK");
            _gradeDeviation = rs.getString("GRADE_DEVIATION");
            _gradeDeviationRank = rs.getString("GRADE_DEVIATION_RANK");
            _classRank = rs.getString("CLASS_RANK");
            _classAvgRank = rs.getString("CLASS_AVG_RANK");
            _classDeviation = rs.getString("CLASS_DEVIATION");
            _classDeviationRank = rs.getString("CLASS_DEVIATION_RANK");
            _courseRank = rs.getString("COURSE_RANK");
            _courseAvgRank = rs.getString("COURSE_AVG_RANK");
            _courseDeviation = rs.getString("COURSE_DEVIATION");
            _courseDeviationRank = rs.getString("COURSE_DEVIATION_RANK");
            _majorRank = rs.getString("MAJOR_RANK");
            _majorAvgRank = rs.getString("MAJOR_AVG_RANK");
            _majorDeviation = rs.getString("MAJOR_DEVIATION");
            _majorDeviationRank = rs.getString("MAJOR_DEVIATION_RANK");
        }
        
        public String rank(final Param param) {
            String rtn = null;
            if ("1".equals(param._groupDiv)) {
                if ("1".equals(param._rankDiv)) {
                    rtn = _gradeRank;
                } else if ("2".equals(param._rankDiv)) {
                    rtn = _gradeAvgRank;
                } else if ("3".equals(param._rankDiv)) {
                    rtn = _gradeDeviationRank;
                }
            } else if ("3".equals(param._groupDiv)) {
                if ("1".equals(param._rankDiv)) {
                    rtn = _courseRank;
                } else if ("2".equals(param._rankDiv)) {
                    rtn = _courseAvgRank;
                } else if ("3".equals(param._rankDiv)) {
                    rtn = _courseDeviationRank;
                }
            }
            return rtn;
        }

        public String avgStr() {
            if (null == _avg) {
                return null;
            }
            return new BigDecimal(_avg).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }
        
        public String getSubclassKey(final Param param) {
            if (SUBCLASSALL.equals(_subclasscd)) {
                return _subclasscd;
            }
            if ("1".equals(param._useCurriculumcd)) {
                return _classcd + "-" + _schoolKind + "-" + _curriculumCd + "-" + _subclasscd;
            }
            return _subclasscd;
        }
    }
    
    private static class AttendRec {
        final String _year;
        final String _annual;
        final String _classdays;
        final String _attend1;
        final String _suspMour;
        final String _suspend;
        final String _mourning;
        final String _abroad;
        final String _requirepresent;
        final String _attend6;
        final String _present;

        AttendRec(
            final String year,
            final String annual,
            final String classdays,
            final String attend1,
            final String suspMour,
            final String suspend,
            final String mourning,
            final String abroad,
            final String requirepresent,
            final String attend6,
            final String present
        ) {
            _year = year;
            _annual = annual;
            _classdays = classdays;
            _attend1 = attend1;
            _suspMour = suspMour;
            _suspend = suspend;
            _mourning = mourning;
            _abroad = abroad;
            _requirepresent = requirepresent;
            _attend6 = attend6;
            _present = present;
        }
    }
    
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 59916 $"); // CVSキーワードの取り扱いに注意
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _grade;
        final String _hrClass;
        final String _date;
        final String _outputDiv; // 1:クラス別成績推移一覧 2:クラス別出欠状況一覧（欠課・欠席）
        final String _groupDiv; // 序列 1:学年 3:コース
        final String _rankDiv; // 順位の基準点 1:総合点 2:平均点 3:偏差値
        final String[] _categorySelected;
        final String _csv;
        final Map _classNameMap;
        final Map _subclassNameMap;
        
        /** 教育課程コードを使用するか */
        private final String _useCurriculumcd;
        private final String _useVirus;
        private final String _useKoudome;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _date = request.getParameter("DATE").replace('/', '-');
            _grade = request.getParameter("GRADE_HR_CLASS").substring(0, 2);
            _hrClass = request.getParameter("GRADE_HR_CLASS").substring(2);
            _outputDiv = request.getParameter("OUTPUT_DIV");
            _groupDiv = request.getParameter("GROUP_DIV");
            _rankDiv = request.getParameter("RANK_DIV");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _categorySelected = request.getParameterValues("category_selected");
            _csv = request.getParameter("csv");
            _classNameMap = setClassMst(db2);
            _subclassNameMap = setSubclassMst(db2);
        }

        private Map getYearDate(final DB2UDB db2, final String year) {
            Map rtn = new HashMap();
            final String sql = " SELECT "
                    + "     SDATE, EDATE "
                    + " FROM "
                    + "     SEMESTER_MST "
                    + " WHERE "
                    + "     YEAR = '" + year + "' "
                    + "     AND SEMESTER = '" + SEMEALL + "' ";

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put("SDATE", rs.getString("SDATE"));
                    rtn.put("EDATE", rs.getString("EDATE"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }
        
        private String setNameMst(final DB2UDB db2, final String namecd1, final String namecd2, final String field) {
            String rtn = "";
            final String sql = " SELECT "
                    + "     * "
                    + " FROM "
                    + "     V_NAME_MST "
                    + " WHERE "
                    + "     YEAR = '" + _year + "' "
                    + "     AND NAMECD1 = '" + namecd1 + "' "
                    + (null != namecd2 ? ("     AND NAMECD2 = '" + namecd2 + "'") : "")
                    + " ORDER BY "
                    + "     NAMECD2 ";

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }
        
        
        private Map setClassMst(final DB2UDB db2) {
            Map rtn = new HashMap();
            final String sql = " SELECT "
                    + "     * "
                    + " FROM "
                    + "     CLASS_MST ";

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String key;
                    if ("1".equals(_useCurriculumcd)) {
                        key = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND");
                    } else {
                        key = rs.getString("CLASSCD");
                    }
                    rtn.put(key, rs.getString("CLASSNAME"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }
        
        private Map setSubclassMst(final DB2UDB db2) {
            Map rtn = new HashMap();
            final String sql = " SELECT "
                    + "     * "
                    + " FROM "
                    + "     SUBCLASS_MST ";

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(getSubclassKey(rs), rs.getString("SUBCLASSNAME"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String getSubclassKey(final ResultSet rs) throws SQLException {
            if (SUBCLASSALL.equals(rs.getString("SUBCLASSCD"))) {
                return rs.getString("SUBCLASSCD");
            }
            final String key;
            if ("1".equals(_useCurriculumcd)) {
                key = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
            } else {
                key = rs.getString("SUBCLASSCD");
            }
            return key;
        }
    }

}
 // KNJD107V

// eof
