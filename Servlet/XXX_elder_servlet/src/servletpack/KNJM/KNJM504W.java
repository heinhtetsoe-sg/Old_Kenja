/*
 * $Id: 7a9b052133305c608817c0d293b5a29ca7770787 $
 *
 * 作成日: 2016/09/16
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJM504W {
    private static final Log log = LogFactory.getLog(servletpack.KNJM.KNJM504W.class);
    private static DecimalFormat zero = new DecimalFormat("00");
    private static DecimalFormat space = new DecimalFormat("##");
    private boolean _hasData;
    private Param _param;

    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Vrw32alp svf;
        DB2UDB db2;
        svf = new Vrw32alp();
        db2 = null;
        try {
            response.setContentType("application/pdf");
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
            _param = createParam(db2, request);
            _hasData = false;
            printMain(db2, svf);
        } catch (Exception e) {
            log.error("Exception:", e);
        } finally {
        	if (!_hasData) {
        		svf.VrSetForm("MES001.frm", 0);
        		svf.VrsOut("note", "note");
        		svf.VrEndPage();
        	}
        	if (db2 != null) {
        		db2.commit();
        		db2.close();
        	}
        	svf.VrQuit();
        }
    }

    private void printMain(DB2UDB db2, Vrw32alp svf) {
        int maxLine = 45;
        String form = "KNJM504W.frm";
        List list = Subclass.getSubclassList(db2, _param);
        for (Iterator it = list.iterator(); it.hasNext();) {
            Subclass subclass = (Subclass) it.next();
            List pageList = getPageList(subclass._studentList, maxLine);
            for (int pi = 0; pi < pageList.size(); pi++) {
                List studentList = (List) pageList.get(pi);
                svf.VrSetForm(form, 1);
                for (int sti = 0; sti < studentList.size(); sti++) {
                    Student student = (Student)studentList.get(sti);
                    int line = sti + 1;
                    String title = (new StringBuilder(String.valueOf(KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year))))).append("年度　　科目別成績進度表").toString();
                    svf.VrsOut("TITLE", title);
                    svf.VrsOut("DATE", (new StringBuilder(String.valueOf(KNJ_EditDate.h_format_JP(db2, _param._kijunDate)))).append("現在").toString());
                    svf.VrsOut("SUBCLASS_NAME", subclass._subclassname);
                    svf.VrsOut("CREDIT", subclass._credits);
                    svf.VrsOutn("HR_NAME", line, (new StringBuilder(String.valueOf(StringUtils.defaultString(student._hrname)))).append(StringUtils.defaultString(NumberUtils.isDigits(student._attendno) ? Integer.valueOf(student._attendno).toString() : student._attendno)).toString());
                    svf.VrsOutn("SCHREGNO", line, student._schregno);
                    svf.VrsOutn("NAME1", line, student._name);
                    for (int repi = 0; repi < student.getSubclassReportList(subclass._subclasscd).size(); repi++) {
                        Report report = (Report)student.getSubclassReportList(subclass._subclasscd).get(repi);
                        if (NumberUtils.isDigits(report._standardSeq)) {
                            int i = Integer.parseInt(report._standardSeq);
                            if (report._repStandardSeq == null) {
                                String hyouka;
                                if (report._standardDate == null) {
                                    hyouka = null;
                                } else if (report._standardDate.compareTo(_param._kijunDate) < 0) {
                                    hyouka = "\327";
                                } else {
                                    hyouka = null;
                                }
                                svf.VrsOutn((new StringBuilder("REP")).append(i).toString(), line, hyouka);
                            } else if (report._gradValue == null || report._gradDate != null && _param._kijunDate.compareTo(report._gradDate) < 0) {
                                svf.VrsOutn((new StringBuilder("REP")).append(i).toString(), line, "受");
                            } else {
                                svf.VrsOutn((new StringBuilder("REP")).append(i).toString(), line, report._gradValueName);
                            }
                        }
                    }

                    svf.VrsOutn("REP13", line, subclass._repSeqAll);
                    svf.VrsOutn("REP14", line, subclass._schSeqMin);
                    List toukouList = student.getAttendList(subclass._subclasscd, false);
                    for (int mensetsui = 0; mensetsui < Math.min(toukouList.size(), 22); mensetsui++) {
                        String date = (String)toukouList.get(mensetsui);
                        svf.VrsOutn((new StringBuilder("INTERVIEW_DATE")).append(String.valueOf(mensetsui + 1)).toString(), line, formatDate(date));
                    }

                    List scoreList = new ArrayList();
                    List testcdList = student.getTestcdList(subclass._subclasscd);
                    for(int sfi = 0; sfi < Math.min(2, testcdList.size()); sfi++) {
                        String testcd = (String)testcdList.get(sfi);
                        String score = (String)student.getSubclassTestScoreMap(subclass._subclasscd).get(testcd);
                        svf.VrsOutn((new StringBuilder("TEST")).append(String.valueOf(sfi + 1)).toString(), line, score);
                        if (NumberUtils.isNumber(score))
                            scoreList.add(new BigDecimal(score));
                    }

                    svf.VrsOutn("TEST_AVE", line, avg(scoreList));
                    svf.VrsOutn("VALUE", line, (String)student.getSubclassTestScoreMap(subclass._subclasscd).get("HYOKA"));
                    svf.VrsOutn("VAL", line, (String)student.getSubclassTestScoreMap(subclass._subclasscd).get("HYOTEI"));
                    svf.VrsOutn("GET_CREDIT", line, (String)student.getSubclassTestScoreMap(subclass._subclasscd).get("GET_CREDIT"));
                    svf.VrsOutn("COMP_CREDIT", line, (String)student.getSubclassTestScoreMap(subclass._subclasscd).get("COMP_CREDIT"));
                    svf.VrsOutn("AUTH_DATE", line, null);
                    svf.VrsOutn("AUTH_PRINT_DATE", line, null);
                    _hasData = true;
                }

                svf.VrEndPage();
            }
        }
    }

    private static class Attend {

        final String _schoolingkindcd;
        final String _namespare1;
        final String _executedate;
        final String _periodcd;
        final BigDecimal _creditTime;

        public Attend(String schoolingkindcd, String namespare1, String executedate, String periodcd, BigDecimal creditTime) {
            _schoolingkindcd = schoolingkindcd;
            _namespare1 = namespare1;
            _executedate = executedate;
            _periodcd = periodcd;
            _creditTime = creditTime;
        }
    }

    private static class Report {

        final String _standardSeq;
        final String _standardDate;
        final String _testcd;
        final String _repStandardSeq;
        final String _namespare1;
        final String _representSeq;
        final String _receiptDate;
        final String _gradDate;
        final String _gradValue;
        final String _gradValueName;

        public Report(String standardSeq, String standardDate, String testcd, String repStandardSeq, String namespare1, String representSeq, String receiptDate, 
                String gradDate, String gradValue, String gradValueName)
        {
            _standardSeq = standardSeq;
            _standardDate = standardDate;
            _testcd = testcd;
            _repStandardSeq = repStandardSeq;
            _namespare1 = namespare1;
            _representSeq = representSeq;
            _receiptDate = receiptDate;
            _gradDate = gradDate;
            _gradValue = gradValue;
            _gradValueName = gradValue != null ? gradValueName : "受";
        }
    }

    private static class Student {

        final String _schregno;
        final String _hrname;
        final String _attendno;
        final String _inoutcd;
        final String _name;
        final String _baseRemark1;
        final String _baseRemark2Name1;
        final Map _subclassReportListMap = new HashMap();
        final Map _subclassAttendListMap = new HashMap();
        final Map _subclassTestScoreMap = new HashMap();

        Student(String schregno, String hrname, String attendno, String inoutcd, String name, String baseRemark1, String thisYearCreditTime, 
                String beforeYearCreditTime, String totalYearCreditTime, String baseRemark2Name1, String beforeYearCredit) {
            _schregno = schregno;
            _hrname = hrname;
            _attendno = attendno;
            _inoutcd = inoutcd;
            _name = name;
            _baseRemark1 = baseRemark1;
            _baseRemark2Name1 = baseRemark2Name1;
        }

        public Map getSubclassTestScoreMap(String subclasscd) {
            if (_subclassTestScoreMap.get(subclasscd) == null)
                _subclassTestScoreMap.put(subclasscd, new HashMap());
            return (Map)_subclassTestScoreMap.get(subclasscd);
        }

        private static List getMappedList(Map m, String key) {
            if (m.get(key) == null)
                m.put(key, new ArrayList());
            return (List)m.get(key);
        }

        private List getSubclassAttendList(String subclasscd) {
            return getMappedList(_subclassAttendListMap, subclasscd);
        }

        private List getSubclassReportList(String subclasscd) {
            return getMappedList(_subclassReportListMap, subclasscd);
        }

        public String getAttendCount(String subclasscd, boolean isHousou) {
            Set set = new HashSet();
            boolean addval = false;
            BigDecimal n = new BigDecimal(0);
            for(Iterator it = getSubclassAttendList(subclasscd).iterator(); it.hasNext();)
            {
                Attend at = (Attend)it.next();
                if (isHousou && "2".equals(at._schoolingkindcd))
                {
                    if (at._creditTime != null)
                    {
                        addval = true;
                        n = n.add(at._creditTime);
                    }
                } else
                if (!isHousou && "1".equals(at._namespare1))
                    if ("1".equals(at._schoolingkindcd))
                        set.add(at._executedate);
                    else
                    if (at._creditTime != null)
                    {
                        addval = true;
                        n = n.add(at._creditTime);
                    }
            }

            if (!set.isEmpty()) {
                addval = true;
                n = n.add(new BigDecimal(set.size()));
            }
            return addval ? getDispNum(n) : isHousou ? null : "0";
        }

        public List getAttendList(String subclasscd, boolean isHousou) {
            TreeSet set = new TreeSet();
            for(Iterator it = getSubclassAttendList(subclasscd).iterator(); it.hasNext();) {
                Attend at = (Attend) it.next();
                if (isHousou && !"1".equals(at._namespare1) && at._executedate != null) {
                    set.add(at._executedate);
                } else if (!isHousou && "1".equals(at._namespare1) && at._executedate != null) {
                    set.add(at._executedate);
                }
            }
            return new ArrayList(set);
        }

        public List getTestcdList(String subclasscd) {
            List list = new ArrayList();
            for(Iterator it = getSubclassReportList(subclasscd).iterator(); it.hasNext();)
            {
                Report rep = (Report)it.next();
                if (rep._testcd != null && !list.contains(rep._testcd))
                    list.add(rep._testcd);
            }

            return list;
        }
    }

    private static class Subclass {

        final String _year;
        final String _chaircd;
        final String _subclasscd;
        final String _subclassname;
        final String _repSeqAll;
        final String _schSeqAll;
        final String _schSeqMin;
        final String _credits;
        final List _studentList = new ArrayList();
        final Set _schregnoSet = new HashSet();

        Subclass(String year, String chaircd, String subclasscd, String subclassname, String repSeqAll, String schSeqAll, String schSeqMin, String credits) {
            _year = year;
            _chaircd = chaircd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _repSeqAll = repSeqAll;
            _schSeqAll = schSeqAll;
            _schSeqMin = schSeqMin;
            _credits = credits;
        }

        private static List getSubclassList(DB2UDB db2, Param param) {
            List list;
            Map subclassMap;
            Map studentMap;
            PreparedStatement ps;
            ResultSet rs;
            list = new ArrayList();
            subclassMap = new HashMap();
            studentMap = new HashMap();
            ps = null;
            rs = null;
            try {
                ps = db2.prepareStatement(sql(param));
                Subclass subclass;
                Student student;
                for (rs = ps.executeQuery(); rs.next(); subclass._studentList.add(student)) {
                    String subclasscd = (new StringBuilder(String.valueOf(rs.getString("CLASSCD")))).append("-").append(rs.getString("SCHOOL_KIND")).append("-").append(rs.getString("CURRICULUM_CD")).append("-").append(rs.getString("SUBCLASSCD")).toString();
                    if (subclassMap.get(subclasscd) == null) {
                        String year = rs.getString("YEAR");
                        String chaircd = rs.getString("CHAIRCD");
                        String subclassname = rs.getString("SUBCLASSNAME");
                        String repSeqAll = rs.getString("REP_SEQ_ALL");
                        String schSeqAll = rs.getString("SCH_SEQ_ALL");
                        String schSeqMin = rs.getString("SCH_SEQ_MIN");
                        String credits = rs.getString("CREDITS");
                        Subclass subclass1 = new Subclass(year, chaircd, subclasscd, subclassname, repSeqAll, schSeqAll, schSeqMin, credits);
                        list.add(subclass1);
                        subclassMap.put(subclasscd, subclass1);
                    }
                    subclass = (Subclass) subclassMap.get(subclasscd);
                    String schregno = rs.getString("SCHREGNO");
                    if (studentMap.get(schregno) == null) {
                        String hrname = rs.getString("HR_NAME");
                        String attendno = rs.getString("ATTENDNO");
                        String inoutcd = rs.getString("INOUTCD");
                        String name = rs.getString("NAME");
                        String baseRemark1 = rs.getString("BASE_REMARK1");
                        String thisYearCreditTime = null;
                        String beforeYearCreditTime = null;
                        String totalYearCreditTime = null;
                        String baseRemark2Name1 = StringUtils.defaultString(rs.getString("BASE_REMARK2_NAME1"));
                        String beforeYearCredit = null;
                        Student student1 = new Student(schregno, hrname, attendno, inoutcd, name, baseRemark1, thisYearCreditTime, beforeYearCreditTime, totalYearCreditTime, baseRemark2Name1, beforeYearCredit);
                        studentMap.put(schregno, student1);
                    }
                    student = (Student)studentMap.get(schregno);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
            	DbUtils.closeQuietly(null, ps, rs);
            	db2.commit();
            }
            try {
                ps = db2.prepareStatement(getSchAttendSql(param));
                for (rs = ps.executeQuery(); rs.next();) {
                    Student student = (Student)studentMap.get(rs.getString("SCHREGNO"));
                    if (student != null) {
                        String subclasscd = (new StringBuilder(String.valueOf(rs.getString("CLASSCD")))).append("-").append(rs.getString("SCHOOL_KIND")).append("-").append(rs.getString("CURRICULUM_CD")).append("-").append(rs.getString("SUBCLASSCD")).toString();
                        student.getSubclassAttendList(subclasscd).add(new Attend(rs.getString("SCHOOLINGKINDCD"), rs.getString("NAMESPARE1"), rs.getString("EXECUTEDATE"), rs.getString("PERIODCD"), rs.getBigDecimal("CREDIT_TIME")));
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
            	DbUtils.closeQuietly(null, ps, rs);
            	db2.commit();
            }
            try {
                String reportSql = getReportSql(param);
                ps = db2.prepareStatement(reportSql);
                for (rs = ps.executeQuery(); rs.next();) {
                    Student student = (Student)studentMap.get(rs.getString("SCHREGNO"));
                    if (student != null) {
                        String subclasscd = (new StringBuilder(String.valueOf(rs.getString("CLASSCD")))).append("-").append(rs.getString("SCHOOL_KIND")).append("-").append(rs.getString("CURRICULUM_CD")).append("-").append(rs.getString("SUBCLASSCD")).toString();
                        student.getSubclassReportList(subclasscd).add(new Report(rs.getString("STANDARD_SEQ"), rs.getString("STANDARD_DATE"), rs.getString("TESTCD"), rs.getString("REP_STANDARD_SEQ"), rs.getString("NAMESPARE1"), rs.getString("REPRESENT_SEQ"), rs.getString("RECEIPT_DATE"), rs.getString("GRAD_DATE"), rs.getString("GRAD_VALUE"), rs.getString("GRAD_VALUE_NAME")));
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
            	DbUtils.closeQuietly(null, ps, rs);
            	db2.commit();
            }
            try {
                String sql = getRecordScoreHistDatSql(param);
                log.info((new StringBuilder(" hyotei sql = ")).append(sql).toString());
                ps = db2.prepareStatement(sql);
                for(rs = ps.executeQuery(); rs.next();) {
                    Student student = (Student)studentMap.get(rs.getString("SCHREGNO"));
                    if (student == null) {
                        log.info((new StringBuilder(" no student = ")).append(rs.getString("SCHREGNO")).toString());
                    } else {
                        String subclasscd = (new StringBuilder(String.valueOf(rs.getString("CLASSCD")))).append("-").append(rs.getString("SCHOOL_KIND")).append("-").append(rs.getString("CURRICULUM_CD")).append("-").append(rs.getString("SUBCLASSCD")).toString();
                        String testcd = (new StringBuilder(String.valueOf(rs.getString("SEMESTER")))).append("-").append(rs.getString("TESTKINDCD")).append("-").append(rs.getString("TESTITEMCD")).append("-").append(rs.getString("SCORE_DIV")).toString();
                        if ("9-99-00-08".equals(testcd)) {
                        	student.getSubclassTestScoreMap(subclasscd).put("HYOKA", rs.getString("SCORE"));
                        } else if ("9-99-00-09".equals(testcd)) {
                            student.getSubclassTestScoreMap(subclasscd).put("GET_CREDIT", rs.getString("GET_CREDIT"));
                            student.getSubclassTestScoreMap(subclasscd).put("COMP_CREDIT", rs.getString("COMP_CREDIT"));
                            student.getSubclassTestScoreMap(subclasscd).put("HYOTEI", rs.getString("SCORE"));
                        }
                        student.getSubclassTestScoreMap(subclasscd).put(testcd, rs.getString("SCORE"));
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
            	DbUtils.closeQuietly(null, ps, rs);
            	db2.commit();
            }
            return list;
        }

        private static String sql(Param param) {
            StringBuffer stb = new StringBuffer();
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.CHAIRCD, ");
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
            stb.append("         T1.SUBCLASSCD, ");
            stb.append("         T8.SUBCLASSNAME, ");
            stb.append("         T2.REP_SEQ_ALL, ");
            stb.append("         T2.SCH_SEQ_ALL, ");
            stb.append("         T2.SCH_SEQ_MIN, ");
            stb.append("         T6.CREDITS, ");
            stb.append("         REGDH.HR_NAME, ");
            stb.append("         T5.GRADE, ");
            stb.append("         T5.HR_CLASS, ");
            stb.append("         T5.ATTENDNO, ");
            stb.append("         T3.SCHREGNO, ");
            stb.append("         T4.INOUTCD, ");
            stb.append("         T4.NAME, ");
            stb.append("         T7.BASE_REMARK1, ");
            stb.append("         T13.NAME1 AS BASE_REMARK2_NAME1 ");
            stb.append("     FROM ");
            stb.append("         CHAIR_DAT T1 ");
            stb.append("     INNER JOIN CHAIR_CORRES_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.CHAIRCD = T1.CHAIRCD ");
            stb.append("     INNER JOIN CHAIR_STD_DAT T3 ON T3.YEAR = T1.YEAR ");
            stb.append("         AND T3.SEMESTER = T1.SEMESTER ");
            stb.append("         AND T3.CHAIRCD = T1.CHAIRCD ");
            stb.append("     INNER JOIN SCHREG_BASE_MST T4 ON T4.SCHREGNO = T3.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT T5 ON T5.SCHREGNO = T3.SCHREGNO ");
            stb.append("         AND T5.YEAR = T1.YEAR ");
            stb.append("         AND T5.SEMESTER = T1.SEMESTER ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = T5.YEAR ");
            stb.append("         AND REGDH.SEMESTER = T5.SEMESTER ");
            stb.append("         AND REGDH.GRADE = T5.GRADE ");
            stb.append("         AND REGDH.HR_CLASS = T5.HR_CLASS ");
            stb.append("     LEFT JOIN CREDIT_MST T6 ON T6.YEAR = T1.YEAR ");
            stb.append("         AND T6.COURSECD = T5.COURSECD ");
            stb.append("         AND T6.GRADE = T5.GRADE ");
            stb.append("         AND T6.MAJORCD = T5.MAJORCD ");
            stb.append("         AND T6.COURSECODE = T5.COURSECODE ");
            stb.append("         AND T6.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T6.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T6.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST T7 ON T7.SCHREGNO = T3.SCHREGNO ");
            stb.append("         AND T7.YEAR = T1.YEAR ");
            stb.append("         AND T7.BASE_SEQ = '001' ");
            stb.append("     LEFT JOIN SUBCLASS_MST T8 ON T8.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T8.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T8.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T8.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     LEFT JOIN SCHREG_BASE_DETAIL_MST T12 ON T12.SCHREGNO = T3.SCHREGNO ");
            stb.append("         AND T12.BASE_SEQ = '004' ");
            stb.append("     LEFT JOIN NAME_MST T13 ON T13.NAMECD1 = 'M013' ");
            stb.append("         AND T13.NAMECD2 = T12.BASE_REMARK2 ");
            stb.append("     WHERE ");
            stb.append((new StringBuilder("         T1.YEAR = '")).append(param._year).append("' ").toString());
            stb.append((new StringBuilder("         AND T1.SEMESTER <= '")).append(param._semester).append("' ").toString());
            stb.append((new StringBuilder("         AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD IN ")).append(SQLUtils.whereIn(true, param._categorySelected)).append(" ").toString());
            stb.append(" ORDER BY ");
            stb.append("     T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T5.GRADE, T5.HR_CLASS, T5.ATTENDNO ");
            return stb.toString();
        }

        private static String getReportSql(Param param) {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAX_REPRESENT_SEQ AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
            stb.append("         T1.SUBCLASSCD, ");
            stb.append("         T1.STANDARD_SEQ, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         MAX(T1.REPRESENT_SEQ) AS REPRESENT_SEQ ");
            stb.append("     FROM ");
            stb.append("         REP_PRESENT_DAT T1 ");
            stb.append("     WHERE ");
            stb.append((new StringBuilder("         T1.YEAR = '")).append(param._year).append("' ").toString());
            stb.append((new StringBuilder("         AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD IN ")).append(SQLUtils.whereIn(true, param._categorySelected)).append(" ").toString());
            stb.append("     GROUP BY ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
            stb.append("         T1.SUBCLASSCD, ");
            stb.append("         T1.STANDARD_SEQ, ");
            stb.append("         T1.SCHREGNO ");
            stb.append(" ), MAX_RECEIPT_DATE AS ( ");
            stb.append("     SELECT  ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
            stb.append("         T1.SUBCLASSCD, ");
            stb.append("         T1.STANDARD_SEQ, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.REPRESENT_SEQ, ");
            stb.append("         MAX(T1.RECEIPT_DATE) AS RECEIPT_DATE ");
            stb.append("     FROM ");
            stb.append("         REP_PRESENT_DAT T1 ");
            stb.append("     INNER JOIN MAX_REPRESENT_SEQ T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T2.REPRESENT_SEQ = T1.REPRESENT_SEQ ");
            stb.append("     GROUP BY ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
            stb.append("         T1.SUBCLASSCD, ");
            stb.append("         T1.STANDARD_SEQ, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.REPRESENT_SEQ ");
            stb.append(" ), PRINT_DATA AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
            stb.append("         T1.SUBCLASSCD, ");
            stb.append("         T1.STANDARD_SEQ, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.REPRESENT_SEQ, ");
            stb.append("         T1.RECEIPT_DATE, ");
            stb.append("         T3.NAMESPARE1, ");
            stb.append("         T1.GRAD_DATE, ");
            stb.append("         T1.GRAD_VALUE, ");
            stb.append("         T3.ABBV1 AS GRAD_VALUE_NAME ");
            stb.append("     FROM ");
            stb.append("         REP_PRESENT_DAT T1 ");
            stb.append("     INNER JOIN MAX_RECEIPT_DATE T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T2.STANDARD_SEQ = T1.STANDARD_SEQ ");
            stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T2.REPRESENT_SEQ = T1.REPRESENT_SEQ ");
            stb.append("         AND T2.RECEIPT_DATE = T1.RECEIPT_DATE ");
            stb.append("     LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'M003' ");
            stb.append("         AND T3.NAMECD2 = T1.GRAD_VALUE ");
            stb.append("     WHERE ");
            stb.append((new StringBuilder("         T2.RECEIPT_DATE <= '")).append(param._kijunDate).append("' ").toString());
            stb.append(" ), SUBCLASS_SCHREGNO AS ( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
            stb.append("         T1.SUBCLASSCD, ");
            stb.append("         T1.SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         SUBCLASS_STD_SELECT_DAT T1 ");
            stb.append("         INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     WHERE ");
            stb.append((new StringBuilder("         T1.YEAR = '")).append(param._year).append("' ").toString());
            stb.append((new StringBuilder("         AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD IN ")).append(SQLUtils.whereIn(true, param._categorySelected)).append(" ").toString());
            stb.append(" ), MAIN AS ( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
            stb.append("         T1.SUBCLASSCD, ");
            stb.append("         T1.STANDARD_SEQ, ");
            stb.append("         T1.STANDARD_DATE, ");
            stb.append("         NMM002.NAMESPARE1 AS TESTCD, ");
            stb.append("         T2.SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         REP_STANDARDDATE_DAT T1 ");
            stb.append("     INNER JOIN SUBCLASS_SCHREGNO T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     LEFT JOIN NAME_MST NMM002 ON NMM002.NAMECD1 = 'M002' AND NMM002.NAMECD2 = T1.REPORTDIV ");
            stb.append(" ) ");
            stb.append("     SELECT ");
            stb.append("         T0.YEAR, ");
            stb.append("         T0.CLASSCD, ");
            stb.append("         T0.SCHOOL_KIND, ");
            stb.append("         T0.CURRICULUM_CD, ");
            stb.append("         T0.SUBCLASSCD, ");
            stb.append("         T0.STANDARD_SEQ, ");
            stb.append("         T0.STANDARD_DATE, ");
            stb.append("         T0.TESTCD, ");
            stb.append("         T0.SCHREGNO, ");
            stb.append("         T1.STANDARD_SEQ AS REP_STANDARD_SEQ, ");
            stb.append("         T1.REPRESENT_SEQ, ");
            stb.append("         T1.RECEIPT_DATE, ");
            stb.append("         T1.NAMESPARE1, ");
            stb.append("         T1.GRAD_DATE, ");
            stb.append("         T1.GRAD_VALUE, ");
            stb.append("         T1.GRAD_VALUE_NAME ");
            stb.append("     FROM ");
            stb.append("         MAIN T0 ");
            stb.append("     LEFT JOIN PRINT_DATA T1 ON T1.YEAR = T0.YEAR ");
            stb.append("         AND T1.CLASSCD = T0.CLASSCD ");
            stb.append("         AND T1.SCHOOL_KIND = T0.SCHOOL_KIND ");
            stb.append("         AND T1.CURRICULUM_CD = T0.CURRICULUM_CD ");
            stb.append("         AND T1.SUBCLASSCD = T0.SUBCLASSCD ");
            stb.append("         AND T1.STANDARD_SEQ = T0.STANDARD_SEQ ");
            stb.append("         AND T1.SCHREGNO = T0.SCHREGNO ");
            stb.append("     ORDER BY ");
            stb.append("         T0.CLASSCD, ");
            stb.append("         T0.SCHOOL_KIND, ");
            stb.append("         T0.CURRICULUM_CD, ");
            stb.append("         T0.SUBCLASSCD, ");
            stb.append("         T0.STANDARD_SEQ, ");
            stb.append("         T1.REPRESENT_SEQ ");
            return stb.toString();
        }

        private static String getSchAttendSql(Param param) {
            StringBuffer stb = new StringBuffer();
            stb.append("     SELECT ");
            stb.append("         T1.YEAR, ");
            stb.append("         T2.SEMESTER, ");
            stb.append("         T3.CLASSCD, ");
            stb.append("         T3.SCHOOL_KIND, ");
            stb.append("         T3.CURRICULUM_CD, ");
            stb.append("         T3.SUBCLASSCD, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.SCHOOLINGKINDCD, ");
            stb.append("         T4.NAMESPARE1, ");
            stb.append("         T1.EXECUTEDATE, ");
            stb.append("         T1.PERIODCD, ");
            stb.append("         T1.CREDIT_TIME ");
            stb.append("     FROM ");
            stb.append("         SCH_ATTEND_DAT T1 ");
            stb.append("         INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("             AND T2.SEMESTER <> '9' ");
            stb.append("             AND T1.EXECUTEDATE BETWEEN T2.SDATE AND T2.EDATE ");
            stb.append("         INNER JOIN CHAIR_DAT T3 ON T3.YEAR = T1.YEAR ");
            stb.append("             AND T3.SEMESTER = T2.SEMESTER ");
            stb.append("             AND T3.CHAIRCD = T1.CHAIRCD ");
            stb.append("         LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'M001' ");
            stb.append("             AND T4.NAMECD2 = T1.SCHOOLINGKINDCD ");
            stb.append("     WHERE ");
            stb.append((new StringBuilder("         T1.YEAR = '")).append(param._year).append("' ").toString());
            stb.append((new StringBuilder("         AND T1.EXECUTEDATE <= '")).append(param._kijunDate).append("' ").toString());
            stb.append((new StringBuilder("         AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD IN ")).append(SQLUtils.whereIn(true, param._categorySelected)).append(" ").toString());
            return stb.toString();
        }

        private static String getRecordScoreHistDatSql(Param param) {
            StringBuffer stb = new StringBuffer();
            stb.append("     SELECT ");
            stb.append("         T1.* ");
            stb.append("     FROM ");
            stb.append("         V_RECORD_SCORE_HIST_DAT T1 ");
            stb.append("     WHERE ");
            stb.append((new StringBuilder("         T1.YEAR = '")).append(param._year).append("' ").toString());
            stb.append((new StringBuilder("         AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD IN ")).append(SQLUtils.whereIn(true, param._categorySelected)).append(" ").toString());
            return stb.toString();
        }
    }


    private static int getMS932ByteLength(String name) {
        int len = 0;
        if (name != null) {
            try {
                len = name.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return len;
    }

    private static String substringMS932(String name, int bytelength) {
        StringBuffer stb = new StringBuffer();
        if (name != null) {
            int maxlen = bytelength;
            try {
                for(int i = 0; i < name.length(); i++) {
                    String sb = name.substring(i, i + 1);
                    maxlen -= sb.getBytes("MS932").length;
                    if (maxlen < 0) {
                        break;
                    }
                    stb.append(sb);
                }

            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return stb.toString();
    }

    private static String subtract(String name, String sub) {
        if (name == null || sub == null || -1 == name.indexOf(sub))
            return null;
        else
            return name.substring(sub.length());
    }

    private String formatDate(String date) {
        if (date == null) {
            return "";
        } else {
            int month = Integer.parseInt(date.substring(5, 7));
            int day = Integer.parseInt(date.substring(8));
            return (new StringBuilder(String.valueOf(space.format(month)))).append("/").append(zero.format(day)).toString();
        }
    }

    private static List getPageList(List list, int count) {
        List rtn = new ArrayList();
        List current = new ArrayList();
        rtn.add(current);
        Object o;
        for(Iterator it = list.iterator(); it.hasNext(); current.add(o))
        {
            o = it.next();
            if (current.size() >= count)
            {
                current = new ArrayList();
                rtn.add(current);
            }
        }

        return rtn;
    }

    private String avg(List scoreList) {
        if (scoreList.isEmpty()) {
            return null;
        }
        BigDecimal sum = new BigDecimal(0);
        for(int i = 0; i < scoreList.size(); i++) {
            sum = sum.add((BigDecimal)scoreList.get(i));
        }

        return sum.divide(new BigDecimal(scoreList.size()), 1, 4).toString();
    }

    private static String getDispNum(BigDecimal bd) {
        if (bd.setScale(0, 0).equals(bd.setScale(0, 1))) {
            return bd.setScale(0).toString();
        } else {
            return bd.setScale(1, 4).toString();
        }
    }

    private Param createParam(DB2UDB db2, HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 76038 $");
        KNJServletUtils.debugParam(request, log);
        Param param = new Param(db2, request);
        return param;
    }

    private class Param {
        final String _year;
        final String _semester;
        final String _categorySelected[];
        final String _loginDate;
        final String _kijunDate;

        Param(DB2UDB db2, HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _categorySelected = request.getParameterValues("category_selected");
            _loginDate = request.getParameter("LOGIN_DATE");
            _kijunDate = request.getParameter("KIJUN") != null ? request.getParameter("KIJUN").replace('/', '-') : null;
        }
    }
}
