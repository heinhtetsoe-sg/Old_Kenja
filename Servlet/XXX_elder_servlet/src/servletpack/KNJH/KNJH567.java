// kanji=漢字
/*
 * $Id: e2232542a460b7edf172a40a8fcedc9d93d3e65d $
 *
 * 作成日: 2007/05/14
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 科目別集計表 (立志舎)
 * @author maesiro
 * @version $Id: e2232542a460b7edf172a40a8fcedc9d93d3e65d $
 */
public class KNJH567 {
    private static final Log log = LogFactory.getLog(KNJH567.class);

    private static final String AVG_DATA_SCORE = "1";

    private static final String RANK_DATA_DIV_SCORE = "01";
    private static final String RANK_DIV_GRADE = "01";

    private static final String AVG_DIV_HR_CLASS = "02";

    private Param _param;
    
    private boolean _hasData;

    /**
     *  KNJH.classから最初に起動されます。
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        log.fatal("$Revision: 59813 $ $Date: 2018-04-21 05:38:34 +0900 (土, 21 4 2018) $"); // CVSキーワードの取り扱いに注意

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");
            
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());
            
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
            
            _param = createParam(request, db2);
            
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

    private int getMS932ByteCount(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return ret;
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final Map hrclassMap = getHrclass(db2);
//      log.debug(" hrclassMap = " + hrclassMap);

//      final Set attendSubclasses = getAttendSubclasses(db2);
//      log.debug(" attendSubclasses = " + attendSubclasses);

        final List subclasscdList = getSubclasscdList(hrclassMap);
        log.info(" subclasscdList = " + subclasscdList);
//      subclasscdList.removeAll(attendSubclasses);
      
        if (subclasscdList.isEmpty()) {
            return;
        }
        
        final String form = "KNJH567.frm";
        svf.VrSetForm(form, 4);
        
        //svf.VrsOut("TITLE", _param.getNendo() + " " + _param._gradename + " " + _param._semestername + " " + _param._proficiencyname1 + "科目別集計表"); // タイトル
        svf.VrsOut("TITLE", _param.getNendo() + " " + _param._proficiencyname1 + "科目別集計表"); // タイトル
        svf.VrsOut("DATE", _param.getDateStr()); // 日付
        
        final List hrclasses = new ArrayList(hrclassMap.values());
        
        for (int subi = 0; subi < subclasscdList.size(); subi++) {
        	final String subclasscd = (String) subclasscdList.get(subi);

            final String subclassname = (String) _param._subclassnames.get(subclasscd);

            log.info(" subclass " + subclasscd + " " + subclassname);

            // 科目名
            svf.VrsOut("SUBCLASS_NAME", subclassname);
            svf.VrEndRecord();

            // ヘッダ
            svf.VrsOut("HEADER", "DUMMY"); 
            svf.VrEndRecord();

            for (int hri = 0; hri < hrclasses.size(); hri++) {
                final Hrclass hrclass = (Hrclass) hrclasses.get(hri);

                final ProficiencyAverageDat average = (ProficiencyAverageDat) hrclass._proficiencyAverageDatMap.get(subclasscd);
                if (null != average) {

                    svf.VrsOut("HR_NAME", hrclass._hrnameAbbv); // 年組名称
                    svf.VrsOut("TEACHER_NAME", hrclass.getStaffsName()); // 担任名
                    svf.VrsOut("AVERAGE", sishagonyu(average._avg)); // 平均点
					final String zaiseki = String.valueOf(hrclass._studentList.size());
                    svf.VrsOut("SCORE", sishagonyu(divide1(average._score, zaiseki))); // 素点
                    svf.VrsOut("TOTAL", average._score); // 合計点
					final String count = average._count;
					svf.VrsOut("PER_ATTEND", append(percentage(divide1(count, zaiseki)), "%")); // 出席率
                    svf.VrsOut("ATTEND", count); // 出席
                    svf.VrsOut("ENROLL", zaiseki); // 在籍 SCHREG_REGD_DAT
                    
                    final List studentList = new ArrayList(hrclass._studentList);

                    for (int si = 0; si < 11; si++) {
                        final String ssi = String.valueOf(si + 1);
                        final int upper = 100 - (si == 0 ? 0 : (si - 1) * 10 + 1); // 100, 99, 89, 79, ... 19, 9
                        final int lower = 100 - (si * 10);  // 100, 90, 80, 70, ... 10, 0
                        //log.info(" range " + upper + " - " + lower);

                        final List inRange = new ArrayList(); 
                        for (final Iterator it = studentList.iterator(); it.hasNext();) {
                        	final Student student = (Student) it.next();
                        	final String score = (String) student._scoreMap.get(subclasscd);
                        	if (NumberUtils.isDigits(score) && lower <= Integer.parseInt(score) && Integer.parseInt(score) <= upper) {
                        		inRange.add(student);
                        		it.remove();
                        	}
                        }
                        
                        svf.VrsOut("DISTRI" + ssi, String.valueOf(inRange.size())); // 分布
                    }
                    svf.VrEndRecord();
                    _hasData = true;
                }
            }
            
            // 空行用
            svf.VrsOut("BLANK", "DUMMY");
            svf.VrEndRecord();
        }
    }
    
    private String append(final String a, final String b) {
    	if (StringUtils.isEmpty(a)) {
    		return "";
    	}
    	return a + b;
    }
    
    private String percentage(final BigDecimal bd) {
    	if (null == bd) {
    		return null;
    	}
    	return sishagonyu(bd.multiply(new BigDecimal(100)));
    }
    
    private BigDecimal divide1(final String num1, final String num2) {
    	if (NumberUtils.isNumber(num1) && NumberUtils.isNumber(num2) && 0.0 != Double.parseDouble(num2)) {
    		return new BigDecimal(num1).divide(new BigDecimal(num2), 6, BigDecimal.ROUND_HALF_UP);
    	}
    	return null;
    }
    
    private String sishagonyu(final BigDecimal rsAvg) {
        return null == rsAvg ? null : rsAvg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }
    
    private List getSubclasscdList(final Map hrclassMap) {
    	final Set subclasscdSet = new HashSet();
    	for (final Iterator it = hrclassMap.values().iterator(); it.hasNext();) {
    		final Hrclass hr = (Hrclass) it.next();
    		subclasscdSet.addAll(hr._proficiencyAverageDatMap.keySet());
    	}
    	final List rtn = new ArrayList(subclasscdSet);
    	Collections.sort(rtn);
        return rtn;
    }
    
//    private Set getAttendSubclasses(final DB2UDB db2, final String selected) {
//        PreparedStatement ps = null;
//        ResultSet rs = null;
//        Set attendSubclass = new TreeSet();
//        try {
//            final String sql = getSubclassReplaceCmbSql(_param, selected);
////            log.debug(" replace_cmb sql = " + sql);
//            ps = db2.prepareStatement(sql);
//            rs = ps.executeQuery();
//            while (rs.next()) {
//                attendSubclass.add(rs.getString("ATTEND_SUBCLASSCD"));
//            }
//        } catch (final Exception e) {
//            log.error("exception!", e);
//        } finally {
//            DbUtils.closeQuietly(null, ps, rs);
//            db2.commit();
//        }
//        return attendSubclass;
//    }
    
//    /** 合併科目取得SQL */
//    private String getSubclassReplaceCmbSql(final Param param, final String selected) {
//        final StringBuffer stb = new StringBuffer();
//        stb.append(" SELECT DISTINCT ");
//        stb.append("     T1.ATTEND_SUBCLASSCD ");
//        stb.append(" FROM ");
//        stb.append("     PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT T1 ");
//        stb.append(" WHERE ");
//        stb.append("     T1.YEAR = '" + _param._year + "' ");
//        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
//        stb.append("     AND T1.PROFICIENCYDIV = '" + _param._proficiencydiv + "' ");
//        stb.append("     AND T1.PROFICIENCYCD = '" + _param._proficiencycd + "' ");
//        stb.append("     AND T1.GRADE = '" + _param._grade +"' ");
//        stb.append(" ORDER BY ");
//        stb.append("     T1.ATTEND_SUBCLASSCD ");
//        return stb.toString();
//    }

    private List getStudentList(final DB2UDB db2) {

        final Map studentMap = new HashMap();
        final String sql = Student.sqlStudent(_param);
        for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
        	final Map row = (Map) it.next();
            final Student student = new Student(KnjDbUtils.getString(row, "SCHREGNO"), KnjDbUtils.getString(row, "NAME"), KnjDbUtils.getString(row, "HR_CLASS"), KnjDbUtils.getString(row, "ATTENDNO"));
            studentMap.put(KnjDbUtils.getString(row, "SCHREGNO"), student);
        }
        
        final String sqlScore = Student.sqlRank(_param);
//      log.debug(" rank sql = " + sql);
        for (final Iterator it = KnjDbUtils.query(db2, sqlScore).iterator(); it.hasNext();) {
          	final Map row = (Map) it.next();
              
              final Student student = (Student) studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
              if (null == student) {
                  continue;
              }
              final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
              final String score = KnjDbUtils.getString(row, "SCORE");
              student._scoreMap.put(subclasscd, score);
        }
        
        final List rtn = new ArrayList();
        rtn.addAll(studentMap.values());
        
        return rtn;
    }

    private Map getHrclass(final DB2UDB db2) {
        final Map rtn = new TreeMap();
        final String sql = Hrclass.sqlHrclass(_param);
        for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
        	final Map row = (Map) it.next();
        	final List staffnameList = new ArrayList();
        	staffnameList.add(KnjDbUtils.getString(row, "STAFFNAME1"));
        	staffnameList.add(KnjDbUtils.getString(row, "STAFFNAME2"));
        	staffnameList.add(KnjDbUtils.getString(row, "STAFFNAME3"));
            rtn.put(KnjDbUtils.getString(row, "HR_CLASS"), new Hrclass(KnjDbUtils.getString(row, "HR_CLASS"), KnjDbUtils.getString(row, "HR_NAME"), KnjDbUtils.getString(row, "HR_NAMEABBV"), staffnameList));
        }
        
        log.info(" hr = " + rtn.keySet());
        
        final String sqlAverage = ProficiencyAverageDat.sqlAverage(_param);
//      log.debug(sql);
        for (final Iterator it = KnjDbUtils.query(db2, sqlAverage).iterator(); it.hasNext();) {
        	final Map row = (Map) it.next();
          
            Hrclass hrclass = (Hrclass) rtn.get(KnjDbUtils.getString(row, "HR_CLASS"));
            if (null == hrclass) {
                continue;
            }
            
            final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
            final String score = KnjDbUtils.getString(row, "SCORE");
            final String highscore = KnjDbUtils.getString(row, "HIGHSCORE");
            final String lowscore = KnjDbUtils.getString(row, "LOWSCORE");
            final String count = KnjDbUtils.getString(row, "COUNT");
            final BigDecimal avg = null == KnjDbUtils.getString(row, "AVG") ? null : new BigDecimal(KnjDbUtils.getString(row, "AVG"));

            hrclass._proficiencyAverageDatMap.put(subclasscd, new ProficiencyAverageDat(subclasscd, score, highscore, lowscore, count, avg));
        }

        for (final Iterator it = getStudentList(db2).iterator(); it.hasNext();) {
        	final Student student = (Student) it.next();
        	final Hrclass hr = (Hrclass) rtn.get(student._hrclass);
        	if (null != hr) {
        		hr._studentList.add(student);
        	}
        }

        return rtn;
    }

    private static class Hrclass {
        final String _hrclass;
        final String _hrname;
        final String _hrnameAbbv;
        final Map _proficiencyAverageDatMap = new HashMap();
        final List _staffnameList;
        final List _studentList = new ArrayList();
        Hrclass(final String hrclass, final String hrname, final String hrnameAbbv, final List staffnameList) {
            _hrclass = hrclass;
            _hrname = hrname;
            _hrnameAbbv = hrnameAbbv;
            _staffnameList = staffnameList;
        }
        
        public String getStaffsName() {
        	final StringBuffer stb = new StringBuffer();
        	for (int i = 0; i < _staffnameList.size(); i++) {
        		final String staffname = (String) _staffnameList.get(i);
        		if (null == staffname) {
        			continue;
        		}
        		if (stb.length() > 0) {
        			stb.append("・");
        		}
        		if (staffname.indexOf('　') > 0) {
        			stb.append(staffname.substring(0, staffname.indexOf('　')));
        		} else {
        			stb.append(staffname);
        		}
        	}
        	return stb.toString();
        }

        private static String sqlHrclass(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.HR_NAME, ");
            stb.append("     T1.HR_NAMEABBV, ");
            stb.append("     STF1.STAFFNAME AS STAFFNAME1, ");
            stb.append("     STF2.STAFFNAME AS STAFFNAME2, ");
            stb.append("     STF3.STAFFNAME AS STAFFNAME3 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("       AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("       AND T2.GRADE = T1.GRADE ");
            stb.append("       AND T2.HR_CLASS = T1.HR_CLASS ");
            stb.append("     LEFT JOIN STAFF_MST STF1 ON STF1.STAFFCD = T1.TR_CD1 ");
            if (param._staffMstHasSchoolKind) {
                stb.append("     AND STF1.SCHOOL_KIND = '" + param._schoolKind + "' ");
            }
            stb.append("     LEFT JOIN STAFF_MST STF2 ON STF2.STAFFCD = T1.TR_CD2 ");
            if (param._staffMstHasSchoolKind) {
                stb.append("     AND STF2.SCHOOL_KIND = '" + param._schoolKind + "' ");
            }
            stb.append("     LEFT JOIN STAFF_MST STF3 ON STF3.STAFFCD = T1.TR_CD3 ");
            if (param._staffMstHasSchoolKind) {
                stb.append("     AND STF3.SCHOOL_KIND = '" + param._schoolKind + "' ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T2.MAJORCD = '102' "); //
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, T1.HR_CLASS ");
            return stb.toString();
        }
        
        public String toString() {
            return "HrclassAvg(" + _hrclass + " : " + _proficiencyAverageDatMap + ")";
        }
    }

    private static class Student {
        final String _schregno;
        final String _hrclass;
        final String _attendno;
        final String _name;
        final Map _scoreMap = new HashMap();
        public Student(final String schregno, final String name, final String hrclass, final String attendno) {
            _schregno = schregno;
            _name = name;
            _hrclass = hrclass;
            _attendno = attendno;
        }
        
        private static String sqlStudent(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T2.NAME, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     INNER JOIN  SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            return stb.toString();
        }
        
        private static String sqlRank(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD, ");
            stb.append("     T1.SCORE ");
            stb.append(" FROM PROFICIENCY_RANK_DAT T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT t3 on ");
            stb.append("         T1.YEAR = t3.YEAR and ");
            stb.append("         T1.SEMESTER = t3.SEMESTER and ");
            stb.append("         T1.SCHREGNO = t3.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     and T1.PROFICIENCYDIV = '" + param._proficiencydiv + "' ");
            stb.append("     and T1.PROFICIENCYCD = '" + param._proficiencycd + "' ");
            stb.append("     and T1.RANK_DATA_DIV = '" + RANK_DATA_DIV_SCORE + "' ");
            stb.append("     and T1.RANK_DIV = '" + RANK_DIV_GRADE + "' ");
            stb.append("     and T3.GRADE = '" + param._grade + "' ");
            return stb.toString();
        }
    }
    
    private static class ProficiencyAverageDat {
        final String _subclasscd;
        final String _score;
        final String _highscore;
        final String _lowscore;
        final String _count;
        final BigDecimal _avg;
        public ProficiencyAverageDat(final String subclasscd, final String score, final String highscore, final String lowscore, final String count, final BigDecimal avg) {
            _subclasscd = subclasscd;
            _score = score;
            _highscore = highscore;
            _lowscore = lowscore;
            _count = count;
            _avg = avg;
        }
        
        private static String sqlAverage(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.AVG_DIV, ");
            stb.append("     T1.PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.SCORE, ");
            stb.append("     T1.HIGHSCORE, ");
            stb.append("     T1.LOWSCORE, ");
            stb.append("     T1.COUNT, ");
            stb.append("     T1.AVG ");
            stb.append(" FROM ");
            stb.append("     PROFICIENCY_AVERAGE_DAT T1 ");
            stb.append("     INNER JOIN PROFICIENCY_SUBCLASS_MST T2 ON T2.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.PROFICIENCYDIV = '" + param._proficiencydiv + "' ");
            stb.append("     AND T1.PROFICIENCYCD = '" + param._proficiencycd + "' ");
            stb.append("     AND T1.DATA_DIV = '" + AVG_DATA_SCORE + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade +"' ");
            stb.append("     AND T1.AVG_DIV = '" + AVG_DIV_HR_CLASS + "' ");
            stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '00000000' ");
            stb.append(" ORDER BY ");
            stb.append("     AVG_DIV, ");
            stb.append("     SUBCLASSCD, ");
            stb.append("     HR_CLASS ");
            return stb.toString();
        }
    }
    
    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        KNJServletUtils.debugParam(request, log);
        return new Param(request, db2);
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _grade;
        final String _proficiencydiv;
        final String _proficiencycd;
        final String _loginDate;
        final Map _subclassnames;
        final String _proficiencyname1;
        final String _semestername;
        final String _gradename;
        final String _schoolKind;
        final boolean _staffMstHasSchoolKind;
        final boolean _seirekiFlg;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _proficiencydiv = request.getParameter("PROFICIENCYDIV");
            _proficiencycd = request.getParameter("PROFICIENCYCD");
            _loginDate = request.getParameter("LOGIN_DATE");
            _subclassnames = getSubclassname(db2);
        	_proficiencyname1 = StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT PROFICIENCYNAME1 FROM PROFICIENCY_MST WHERE PROFICIENCYDIV = '" + _proficiencydiv + "' AND PROFICIENCYCD = '" + _proficiencycd + "' ")));
        	_semestername = StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ")));
        	_gradename = StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE ='" + _grade + "' ")));
            _schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' "));
            _staffMstHasSchoolKind = KnjDbUtils.query(db2, "SELECT 1 FROM SYSCAT.COLUMNS WHERE TABNAME = 'STAFF_MST' AND COLNAME = 'SCHOOL_KIND' ").size() > 0;
            _seirekiFlg = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' AND NAME1 IS NOT NULL ")));
        }
        
        private Map getSubclassname(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD, ");
            stb.append("     SUBCLASS_NAME AS SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("     PROFICIENCY_SUBCLASS_MST ");
            
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, stb.toString()), "SUBCLASSCD", "SUBCLASSNAME");
        }

        /**
         * 年度の名称を得る
         * @return 年度の名称
         */
        public String getNendo() {
            return _seirekiFlg ? _year + "年度" : KNJ_EditDate.h_format_JP_N(_year + "-01-01") + "度";
        }

        /**
         * 日付の名称を得る
         * @return 日付の名称
         */
        public String getDateStr() {
            return _seirekiFlg ? (_loginDate.substring(0,4) + "年" + KNJ_EditDate.h_format_JP_MD(_loginDate)) : (KNJ_EditDate.h_format_JP(_loginDate));
        }
    }
}
