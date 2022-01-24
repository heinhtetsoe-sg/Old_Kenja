// kanji=漢字
/*
 * $Id: 04bc3a7c4ea19e71ba3e3a8dcb73230b57c04e67 $
 *
 * 作成日: 2018/07/26 15:46:35 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *  学校教育システム 賢者 [進路管理]
 *
 *  評定平均値一覧表（新賢者）
 *
 */

public class KNJE130J {


    private static final Log log = LogFactory.getLog(KNJE130J.class);

    final static int MAX_LINE = 50;

    private boolean _hasData;

    private Param _param;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        Vrw32alp svf = null;
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            _param = createParam(db2, request);

            if ("csv".equals(_param._cmd)) {
                final List outputLines = new ArrayList();
                final Map csvParam = new HashMap();
                csvParam.put("HttpServletRequest", request);
                setOutputCsvLines(db2, outputLines);
                CsvUtils.outputLines(log, response, "評定平均値一覧.csv", outputLines, csvParam);
            } else {
                svf = new Vrw32alp();
                svf.VrInit();
                svf.VrSetSpoolFileStream(response.getOutputStream());
                response.setContentType("application/pdf");

                _hasData = false;
                printMain(db2, svf);
            }

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }

            if ("csv".equals(_param._cmd)) {
            } else {
                if (!_hasData) {
                    svf.VrSetForm("MES001.frm", 0);
                    svf.VrsOut("note" , "note");
                    svf.VrEndPage();
                }
                svf.VrQuit();
            }
        }

    }

    private void setOutputCsvLines(final DB2UDB db2, final List outputList) throws SQLException {
        final List hrInfoList = getHRInfoList(db2);

        for (final Iterator it = hrInfoList.iterator(); it.hasNext();) {
            final HRInfo hrInfo = (HRInfo) it.next();

            outputCsv(db2, outputList, hrInfo);
        }
    }

    private boolean outputCsv(final DB2UDB db2, final List outputList, final HRInfo hrInfo) {
        boolean hasData = false;

        final List headerLineList = new ArrayList();
        final List header1Line = newLine(headerLineList);
        final String title = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度　評定平均値一覧";
        header1Line.addAll(Arrays.asList(new String[] {"", title}));

        final List header2Line = newLine(headerLineList);
        final String date = KNJ_EditDate.h_format_JP(db2, _param._ctrlDate);
        header2Line.addAll(Arrays.asList(new String[] {hrInfo._hrName, "", "", date}));

        final List header3Line = newLine(headerLineList);
        header3Line.addAll(Arrays.asList(new String[] {"番号", "氏名", "コース"}));
        for (final Iterator it = hrInfo._classInfoList.iterator(); it.hasNext();) {
            final ClassInfo classInfo = (ClassInfo) it.next();
            header3Line.add(StringUtils.defaultString(classInfo._classAbbv));
        }
        header3Line.addAll(Arrays.asList(new String[] {"合計", "科目数計", "平均", "ランク", "学級順位", "学年順位"}));

        final List studentLineList = new ArrayList();
        for (final Iterator it = hrInfo._studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            final List studentLine = newLine(studentLineList);
            studentLine.add(StringUtils.defaultString(student.getPrintAttendno()));
            studentLine.add(StringUtils.defaultString(student._name));
            studentLine.add(StringUtils.defaultString(student._courseCodeName));
            for (final Iterator it2 = hrInfo._classInfoList.iterator(); it2.hasNext();) {
                final ClassInfo classInfo = (ClassInfo) it2.next();
                final String avg = (String) student._classAvgMap.get(classInfo.getKey());
                studentLine.add(StringUtils.defaultString(avg));
            }
            studentLine.add(StringUtils.defaultString(student._score));
            studentLine.add(StringUtils.defaultString(student._count));
            studentLine.add(StringUtils.defaultString(student._avg));
            studentLine.add(StringUtils.defaultString(student._assessMark));
            studentLine.add(StringUtils.defaultString(student._hrRank));
            studentLine.add(StringUtils.defaultString(student._grRank));
            hasData = true;
        }

        final List footerLineList = new ArrayList();
        final List footer1Line = newLine(footerLineList);
        footer1Line.addAll(Arrays.asList(new String[] {"", "クラス平均", ""}));
        for (final Iterator it = hrInfo._classInfoList.iterator(); it.hasNext();) {
            final ClassInfo classInfo = (ClassInfo) it.next();
            footer1Line.add(StringUtils.defaultString(classInfo._hrAvg));
        }
        footer1Line.addAll(Arrays.asList(new String[] {StringUtils.defaultString(hrInfo._hrAvgS), "", StringUtils.defaultString(hrInfo._hrAvgA), "", "", ""}));

        final List footer2Line = newLine(footerLineList);
        footer2Line.addAll(Arrays.asList(new String[] {"", "学年平均", ""}));
        for (final Iterator it = hrInfo._classInfoList.iterator(); it.hasNext();) {
            final ClassInfo classInfo = (ClassInfo) it.next();
            footer2Line.add(StringUtils.defaultString(classInfo._grAvg));
        }
        footer2Line.addAll(Arrays.asList(new String[] {StringUtils.defaultString(hrInfo._grAvgS), "", StringUtils.defaultString(hrInfo._grAvgA), "", "", ""}));

        outputList.addAll(headerLineList);
        outputList.addAll(studentLineList);
        outputList.addAll(footerLineList);
        newLine(outputList); // ブランク
        newLine(outputList); // ブランク

        return hasData;
    }

    private List newLine(final List listList) {
        final List line = line();
        listList.add(line);
        return line;
    }

    private List line() {
        return line(0);
    }

    private List line(final int size) {
        final List line = new ArrayList();
        for (int i = 0; i < size; i++) {
            line.add(null);
        }
        return line;
    }

    /**メイン*/
    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List hrInfoList = getHRInfoList(db2);

        for (final Iterator it = hrInfoList.iterator(); it.hasNext();) {
            final HRInfo hrInfo = (HRInfo) it.next();

            svf.VrSetForm("KNJE130J.frm", 4);
            svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度　評定平均値一覧");
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._ctrlDate)); //作成日
            svf.VrsOut("HR_NAME", hrInfo._hrName); //組名称

            printSchnoAssess(svf, hrInfo);
        }
    }

    /**生徒毎の評定平均値の出力*/
    private void printSchnoAssess(final Vrw32alp svf, final HRInfo hrInfo) {
        //教科略称名
        int classno = 0;
        for (final Iterator it = hrInfo._classInfoList.iterator(); it.hasNext();) {
            final ClassInfo classInfo = (ClassInfo) it.next();
            classno++;
            svf.VrsOutn("SUBCLASS", classno, classInfo._classAbbv); //教科略称名
        }
        //生徒行
        for (final Iterator it = hrInfo._studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            svf.VrsOut("ATTENDNO"       , student.getPrintAttendno()); //出席番号
            svf.VrsOut("NAME"           , student._name); //氏名
            svf.VrsOut("COURSE"         , student._courseCodeName); //コース
            svf.VrsOut("TOTAL1"         , student._score);  //合計
            svf.VrsOut("SUBCLASS_TOTAL1", student._count);  //科目数計
            svf.VrsOut("TOTAL_AVERAGE1" , student._avg);    //平均
            svf.VrsOut("RANK1"          , student._assessMark); //ランク
            svf.VrsOut("CLASS_RANK1"    , student._hrRank); //学級順位
            svf.VrsOut("GRADE_RANK1"    , student._grRank); //学年順位
            //生徒別教科別評定平均値
            int classno2 = 0;
            for (final Iterator it2 = hrInfo._classInfoList.iterator(); it2.hasNext();) {
                final ClassInfo classInfo = (ClassInfo) it2.next();
                classno2++;
                final String avg = (String) student._classAvgMap.get(classInfo.getKey());
                svf.VrsOutn("AVERAGE1", classno2, avg); //評定平均値
            }
            svf.VrEndRecord();
            _hasData = true;
        }
        //空行
        for (int i = hrInfo._studentList.size() + 1; i <= MAX_LINE; i++) {
            svf.VrsOut("ATTENDNO", String.valueOf(i));
            svf.VrAttribute("ATTENDNO", "X=10000");
            svf.VrEndRecord();
        }
        //平均行
        //クラス平均行
        svf.VrsOut("AVERAGE_NAME"    , "クラス平均");
        int classno2 = 0;
        for (final Iterator it2 = hrInfo._classInfoList.iterator(); it2.hasNext();) {
            final ClassInfo classInfo = (ClassInfo) it2.next();
            classno2++;
            svf.VrsOutn("AVERAGE2", classno2, classInfo._hrAvg); //教科別評定平均値
        }
        svf.VrsOut("TOTAL2"         , hrInfo._hrAvgS);  //合計
        svf.VrsOut("TOTAL_AVERAGE2" , hrInfo._hrAvgA);  //平均
        svf.VrEndRecord();
        //学年平均行
        svf.VrsOut("AVERAGE_NAME"    , "学年平均");
        int classno3 = 0;
        for (final Iterator it2 = hrInfo._classInfoList.iterator(); it2.hasNext();) {
            final ClassInfo classInfo = (ClassInfo) it2.next();
            classno3++;
            svf.VrsOutn("AVERAGE2", classno3, classInfo._grAvg); //教科別評定平均値
        }
        svf.VrsOut("TOTAL2"         , hrInfo._grAvgS);  //合計
        svf.VrsOut("TOTAL_AVERAGE2" , hrInfo._grAvgA);  //平均
        svf.VrEndRecord();
    }

    private List getHRInfoList(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH STUDYREC_AVG8 AS ( ");
            stb.append("     SELECT ");
            stb.append("         AVG_DIV, ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         DECIMAL(ROUND(AVG*10,0)/10,4,1) AS AVG ");
            stb.append("     FROM ");
            stb.append("         SCHREG_STUDYREC_AVG_CLASS_DAT ");
            stb.append("     WHERE ");
            stb.append("         YEAR = '" + _param._year + "' ");
            stb.append("         AND CLASS_DIV = '8' "); //8:ALL(合計列)
            stb.append("         AND CLASSCD = '00' "); //00:ALL
            stb.append("         AND SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("         AND COURSECD = '0' ");
            stb.append("         AND MAJORCD = '000' ");
            stb.append("         AND COURSECODE = '0000' ");
            stb.append(" ) ");
            stb.append(" , STUDYREC_AVG9 AS ( ");
            stb.append("     SELECT ");
            stb.append("         AVG_DIV, ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         DECIMAL(ROUND(AVG*10,0)/10,4,1) AS AVG ");
            stb.append("     FROM ");
            stb.append("         SCHREG_STUDYREC_AVG_CLASS_DAT ");
            stb.append("     WHERE ");
            stb.append("         YEAR = '" + _param._year + "' ");
            stb.append("         AND CLASS_DIV = '9' "); //9:ALL(平均列)
            stb.append("         AND CLASSCD = '00' "); //00:ALL
            stb.append("         AND SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("         AND COURSECD = '0' ");
            stb.append("         AND MAJORCD = '000' ");
            stb.append("         AND COURSECODE = '0000' ");
            stb.append(" ) ");

            stb.append(" SELECT ");
            stb.append("     REGDH.GRADE, ");
            stb.append("     REGDH.HR_CLASS, ");
            stb.append("     REGDH.HR_NAME, ");
            stb.append("     AVG1.AVG AS HR_AVG_SCORE, ");
            stb.append("     AVG2.AVG AS HR_AVG_AVG, ");
            stb.append("     AVG3.AVG AS GR_AVG_SCORE, ");
            stb.append("     AVG4.AVG AS GR_AVG_AVG ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT REGDH ");
            stb.append("     LEFT JOIN STUDYREC_AVG8 AVG1 ON AVG1.AVG_DIV = '2' AND AVG1.GRADE = REGDH.GRADE AND AVG1.HR_CLASS = REGDH.HR_CLASS "); //2:学級平均 8:ALL(合計列)
            stb.append("     LEFT JOIN STUDYREC_AVG9 AVG2 ON AVG2.AVG_DIV = '2' AND AVG2.GRADE = REGDH.GRADE AND AVG2.HR_CLASS = REGDH.HR_CLASS "); //2:学級平均 9:ALL(平均列)
            stb.append("     LEFT JOIN STUDYREC_AVG8 AVG3 ON AVG3.AVG_DIV = '1' AND AVG3.GRADE = REGDH.GRADE AND AVG3.HR_CLASS = '000' "); //1:学年平均 8:ALL(合計列)
            stb.append("     LEFT JOIN STUDYREC_AVG9 AVG4 ON AVG4.AVG_DIV = '1' AND AVG4.GRADE = REGDH.GRADE AND AVG4.HR_CLASS = '000' "); //1:学年平均 9:ALL(平均列)
            stb.append(" WHERE ");
            stb.append("     REGDH.YEAR = '" + _param._year + "' ");
            stb.append("     AND REGDH.SEMESTER = '" + _param._ctrlSemester + "' ");
            stb.append("     AND REGDH.GRADE || REGDH.HR_CLASS IN " + SQLUtils.whereIn(true, _param._classSelected));
            stb.append(" ORDER BY ");
            stb.append("     REGDH.GRADE, ");
            stb.append("     REGDH.HR_CLASS ");

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String hrclass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final String hrAvgS = rs.getString("HR_AVG_SCORE");
                final String hrAvgA = rs.getString("HR_AVG_AVG");
                final String grAvgS = rs.getString("GR_AVG_SCORE");
                final String grAvgA = rs.getString("GR_AVG_AVG");

                final HRInfo hrInfo = new HRInfo(grade, hrclass, hrName, hrAvgS, hrAvgA, grAvgS, grAvgA);
                hrInfo.setStudents(db2);
                hrInfo.setClassInfo(db2);
                retList.add(hrInfo);
            }
        } catch (final SQLException e) {
            log.error("クラス情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retList;
    }

    private class HRInfo {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrAvgS;
        final String _hrAvgA;
        final String _grAvgS;
        final String _grAvgA;
        final List _studentList;
        final List _classInfoList;

        public HRInfo(
                final String grade,
                final String hrClass,
                final String hrName,
                final String hrAvgS,
                final String hrAvgA,
                final String grAvgS,
                final String grAvgA
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrAvgS = hrAvgS;
            _hrAvgA = hrAvgA;
            _grAvgS = grAvgS;
            _grAvgA = grAvgA;
            _studentList = new ArrayList();
            _classInfoList = new ArrayList();
        }

        public void setStudents(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH STUDYREC_RANK AS ( ");
                stb.append("     SELECT ");
                stb.append("         SCHREGNO, ");
                stb.append("         RANK_DIV, ");
                stb.append("         DECIMAL(ROUND(SCORE*10,0)/10,4,1) AS SCORE, ");
                stb.append("         COUNT, ");
                stb.append("         DECIMAL(ROUND(AVG*10,0)/10,4,1) AS AVG, ");
                stb.append("         AVG_RANK ");
                stb.append("     FROM ");
                stb.append("         SCHREG_STUDYREC_RANK_CLASS_DAT ");
                stb.append("     WHERE ");
                stb.append("         YEAR = '" + _param._year + "' ");
                stb.append("         AND CLASS_DIV = '9' "); //9:ALL
                stb.append("         AND CLASSCD = '00' "); //00:ALL
                stb.append("         AND SCHOOL_KIND = '" + _param._schoolKind + "' ");
                stb.append(" ) ");

                stb.append(" SELECT ");
                stb.append("     REGD.SCHREGNO, ");
                stb.append("     REGD.ATTENDNO, ");
                stb.append("     BASE.NAME, ");
                stb.append("     REGD.COURSECODE, ");
                stb.append("     CCODE.COURSECODENAME, ");
                stb.append("     RANK2.SCORE, ");
                stb.append("     RANK2.COUNT, ");
                stb.append("     RANK2.AVG, ");
                stb.append("     ASSM.ASSESSMARK, ");
                stb.append("     RANK2.AVG_RANK AS HR_RANK, ");
                stb.append("     RANK1.AVG_RANK AS GR_RANK ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_DAT REGD ");
                stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
                stb.append("     LEFT JOIN COURSECODE_MST CCODE ON CCODE.COURSECODE = REGD.COURSECODE ");
                stb.append("     LEFT JOIN STUDYREC_RANK RANK1 ON RANK1.SCHREGNO = REGD.SCHREGNO AND RANK1.RANK_DIV = '1' "); //1:学年順位
                stb.append("     LEFT JOIN STUDYREC_RANK RANK2 ON RANK2.SCHREGNO = REGD.SCHREGNO AND RANK2.RANK_DIV = '2' "); //2:学級順位
                stb.append("     LEFT JOIN ASSESS_MST ASSM ON ASSM.ASSESSCD = '4' AND RANK2.AVG BETWEEN ASSM.ASSESSLOW AND ASSM.ASSESSHIGH ");
                stb.append(" WHERE ");
                stb.append("     REGD.YEAR = '" + _param._year + "' ");
                stb.append("     AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
                stb.append("     AND REGD.GRADE = '" + _grade + "' ");
                stb.append("     AND REGD.HR_CLASS = '" + _hrClass + "' ");
                stb.append(" ORDER BY ");
                stb.append("     REGD.ATTENDNO ");

//                log.debug(" student sql = " + stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final String attendno = rs.getString("ATTENDNO");
                    final String name = rs.getString("NAME");
                    final String coursecode = rs.getString("COURSECODE");
                    final String courseCodeName = rs.getString("COURSECODENAME");
                    final String score = rs.getString("SCORE");
                    final String count = rs.getString("COUNT");
                    final String avg = rs.getString("AVG");
                    final String assessMark = rs.getString("ASSESSMARK");
                    final String hrRank = rs.getString("HR_RANK");
                    final String grRank = rs.getString("GR_RANK");

                    final Student student = new Student(
                            schregno,
                            attendno,
                            name,
                            coursecode,
                            courseCodeName,
                            score,
                            count,
                            avg,
                            assessMark,
                            hrRank,
                            grRank
                    );
                    student.setClassAvgMap(db2);
                    _studentList.add(student);
                }
            } catch (final SQLException e) {
                log.error("生徒情報取得でエラー", e);
                throw e;
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        public void setClassInfo(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH STUDYREC_AVG1 AS ( ");
                stb.append("     SELECT ");
                stb.append("         CLASSCD, ");
                stb.append("         SCHOOL_KIND, ");
                stb.append("         DECIMAL(ROUND(AVG*10,0)/10,4,1) AS AVG ");
                stb.append("     FROM ");
                stb.append("         SCHREG_STUDYREC_AVG_CLASS_DAT ");
                stb.append("     WHERE ");
                stb.append("         YEAR = '" + _param._year + "' ");
                stb.append("         AND CLASS_DIV = '1' "); //1:教科別
                stb.append("         AND AVG_DIV = '1' "); //1:学年平均
                stb.append("         AND GRADE = '" + _grade + "' ");
                stb.append("         AND HR_CLASS = '000' ");
                stb.append("         AND COURSECD = '0' ");
                stb.append("         AND MAJORCD = '000' ");
                stb.append("         AND COURSECODE = '0000' ");
                stb.append(" ) ");
                stb.append(" , STUDYREC_AVG2 AS ( ");
                stb.append("     SELECT ");
                stb.append("         CLASSCD, ");
                stb.append("         SCHOOL_KIND, ");
                stb.append("         DECIMAL(ROUND(AVG*10,0)/10,4,1) AS AVG ");
                stb.append("     FROM ");
                stb.append("         SCHREG_STUDYREC_AVG_CLASS_DAT ");
                stb.append("     WHERE ");
                stb.append("         YEAR = '" + _param._year + "' ");
                stb.append("         AND CLASS_DIV = '1' "); //1:教科別
                stb.append("         AND AVG_DIV = '2' "); //2:学級平均
                stb.append("         AND GRADE = '" + _grade + "' ");
                stb.append("         AND HR_CLASS = '" + _hrClass + "' ");
                stb.append("         AND COURSECD = '0' ");
                stb.append("         AND MAJORCD = '000' ");
                stb.append("         AND COURSECODE = '0000' ");
                stb.append(" ) ");

                stb.append(" SELECT ");
                stb.append("     CM.CLASSCD, ");
                stb.append("     CM.SCHOOL_KIND, ");
                stb.append("     CM.CLASSABBV, ");
                stb.append("     AVG2.AVG AS HR_AVG, ");
                stb.append("     AVG1.AVG AS GR_AVG ");
                stb.append(" FROM ");
                stb.append("     CLASS_MST CM ");
                stb.append("     INNER JOIN STUDYREC_AVG1 AVG1 ON AVG1.CLASSCD = CM.CLASSCD AND AVG1.SCHOOL_KIND = CM.SCHOOL_KIND "); //1:学年平均
                stb.append("     INNER JOIN STUDYREC_AVG2 AVG2 ON AVG2.CLASSCD = CM.CLASSCD AND AVG2.SCHOOL_KIND = CM.SCHOOL_KIND "); //2:学級平均
                stb.append(" ORDER BY ");
                stb.append("     CM.CLASSCD, ");
                stb.append("     CM.SCHOOL_KIND ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String classcd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String classAbbv = rs.getString("CLASSABBV");
                    final String hrAvg = rs.getString("HR_AVG");
                    final String grAvg = rs.getString("GR_AVG");

                    final ClassInfo classInfo = new ClassInfo(
                            classcd,
                            schoolKind,
                            classAbbv,
                            hrAvg,
                            grAvg
                    );
                    _classInfoList.add(classInfo);
                }
            } catch (final SQLException e) {
                log.error("教科情報取得でエラー", e);
                throw e;
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
    }

    private class Student {
        final String _schregno;
        final String _attendNo;
        final String _name;
        final String _courseCode;
        final String _courseCodeName;
        final String _score;
        final String _count;
        final String _avg;
        final String _assessMark;
        final String _hrRank;
        final String _grRank;
        final Map _classAvgMap;

        public Student(
                final String schregno,
                final String attendNo,
                final String name,
                final String courseCode,
                final String courseCodeName,
                final String score,
                final String count,
                final String avg,
                final String assessMark,
                final String hrRank,
                final String grRank
        ) {
            _schregno = schregno;
            _attendNo = attendNo;
            _name = name;
            _courseCode = courseCode;
            _courseCodeName = courseCodeName;
            _score = score;
            _count = count;
            _avg = avg;
            _assessMark = assessMark;
            _hrRank = hrRank;
            _grRank = grRank;
            _classAvgMap = new HashMap();
        }

        public String getPrintAttendno() {
            return NumberUtils.isDigits(_attendNo) ? String.valueOf(Integer.parseInt(_attendNo)) : _attendNo;
        }

        public void setClassAvgMap(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     CLASSCD, ");
                stb.append("     SCHOOL_KIND, ");
                stb.append("     DECIMAL(ROUND(AVG*10,0)/10,4,1) AS AVG ");
                stb.append(" FROM ");
                stb.append("     SCHREG_STUDYREC_RANK_CLASS_DAT ");
                stb.append(" WHERE ");
                stb.append("     YEAR = '" + _param._year + "' ");
                stb.append("     AND SCHREGNO = '" + _schregno + "' ");
                stb.append("     AND CLASS_DIV = '1' "); //1:教科別
                stb.append("     AND RANK_DIV = '2' "); //2:学級別
                stb.append(" ORDER BY ");
                stb.append("     CLASSCD, ");
                stb.append("     SCHOOL_KIND ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String classcd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String avg = rs.getString("AVG");

                    _classAvgMap.put(classcd + schoolKind, avg);
                }
            } catch (final SQLException e) {
                log.error("生徒別教科別情報取得でエラー", e);
                throw e;
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
    }

    private class ClassInfo {
        final String _classcd;
        final String _schoolKind;
        final String _classAbbv;
        final String _hrAvg;
        final String _grAvg;

        public ClassInfo(
                final String classcd,
                final String schoolKind,
                final String classAbbv,
                final String hrAvg,
                final String grAvg
        ) {
            _classcd = classcd;
            _schoolKind = schoolKind;
            _classAbbv = classAbbv;
            _hrAvg = hrAvg;
            _grAvg = grAvg;
        }

        public String getKey() {
            return _classcd + _schoolKind;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 61568 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class Param {
        final String _year;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _grade;
        final String[] _classSelected;
        final String _useSchool_KindField;
        final String _SCHOOLKIND;
        final String _cmd;
        final String _schoolKind; //校種（学年より取得）

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _grade = request.getParameter("GRADE");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _cmd = request.getParameter("cmd");
            _schoolKind = getSchoolKind(db2);
        }

        private String getSchoolKind(final DB2UDB db2) {
            String schoolKind = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    schoolKind = rs.getString("SCHOOL_KIND");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolKind;
        }
    }

}//クラスの括り
