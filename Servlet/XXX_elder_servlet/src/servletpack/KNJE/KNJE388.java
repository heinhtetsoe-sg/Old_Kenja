// kanji=漢字
/*
 * $Id: caccfa41ecb3b736a07b6c378217b05cc1aac75e $
 *
 * 作成日: 2019/12/25 18:00:00 - JST
 * 作成者: yogi
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

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
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: caccfa41ecb3b736a07b6c378217b05cc1aac75e $
 */
public class KNJE388 {

    private static final Log log = LogFactory.getLog("KNJE388.class");

    private boolean _hasData;

    Param _param;

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
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        Map schregMap = getSchregMap(db2);
        int maxline = 50;

        int outline = 1;
        svf.VrSetForm("KNJE388.frm", 1);
        setTitle(db2, svf);
        for (Iterator ite = schregMap.keySet().iterator();ite.hasNext();) {
        	String kStr = (String)ite.next();
        	Student outwk = (Student)schregMap.get(kStr);
        	if (maxline < outline) {
        		svf.VrEndPage();
                setTitle(db2, svf);
        		outline = 1;
        	}
        	svf.VrsOutn("HR_NAME", outline, outwk._hrName + Integer.parseInt(outwk._attendNo) + "番");
        	final int nlen = KNJ_EditEdit.getMS932ByteLength(outwk._name);
        	final String nfield = nlen > 30 ? "3" : nlen > 20 ? "2" : "1";
        	svf.VrsOutn("NAME" + nfield, outline, outwk._name);
        	svf.VrsOutn("BEST_SCORE", outline, outwk._mScore);

        	int colCnt = 1;
        	for (Iterator its = outwk._detailMap.keySet().iterator();its.hasNext();) {
        		String kkstr = (String)its.next();
        		ScoreDetail subwk = (ScoreDetail)outwk._detailMap.get(kkstr);
        		if (outline == 1) {
        		    svf.VrsOut("YEAR"+colCnt, subwk._testDate.substring(0,4));
        		    svf.VrsOut("DATE"+colCnt, subwk._testDate.substring(5).replace('-', '/'));
        		}
        		svf.VrsOutn("SCORE"+colCnt, outline, subwk._score);
        		colCnt++;
        	}
        	_hasData = true;
        	outline++;
        }
		svf.VrEndPage();
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf) {
    	final String nendo = KNJ_EditDate.getAutoFormatYearNen(db2, _param._ctrlDate);
    	svf.VrsOut("TITLE", nendo+"度  " + _param._gradeName + "  トーフルスコア一覧表 (" + _param._date.replace('-', '.') + ")");

    }
    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }
    private Map getSchregMap(final DB2UDB db2) {
        final Map rtnMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String singakuSql = getSchregSql();
        log.debug(" sql = " + singakuSql);
        try {
            ps = db2.prepareStatement(singakuSql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String schregNo = rs.getString("SCHREGNO");
            	final String grade = rs.getString("GRADE");
            	final String hrClass = rs.getString("HR_CLASS");
            	final String hrName = rs.getString("HR_NAME");
            	final String attendNo = rs.getString("ATTENDNO");
            	final String name = rs.getString("NAME");
            	final String mScore = rs.getString("M_SCORE");
            	Student addwk = new Student(schregNo, grade, hrClass, hrName, attendNo, name, mScore);
            	rtnMap.put(schregNo, addwk);
            }
        } catch (SQLException e) {
            log.error("exception!!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        if (rtnMap.size() > 0) {
        	getSchregMap(db2, rtnMap);
        }
        return rtnMap;
    }

    private String getSchregSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        //stb.append(" row_number() over() AS RNUM "); //動作確認用。出力行
        stb.append("  T1.SCHREGNO, ");
        stb.append("  T1.GRADE, ");
        stb.append("  T1.HR_CLASS, ");
        stb.append("  T3.HR_NAME, ");
        stb.append("  T1.ATTENDNO, ");
        stb.append("  BM.NAME, ");
        stb.append("  max(T2.SCORE) AS M_SCORE ");
        stb.append(" FROM ");
        stb.append("  SCHREG_REGD_DAT T1 ");
        stb.append("  LEFT JOIN SCHREG_BASE_MST BM ");
        stb.append("    ON BM.SCHREGNO = T1.SCHREGNO ");
        stb.append("  LEFT JOIN AFT_TOTAL_STUDY_TOEFL_DAT T2 ");
        stb.append("    ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("   AND T2.TEST_DATE <= '" + _param._date + "' ");
        stb.append("   AND T2.TEST_DATE >= '" + _param._sDate + "' ");
        stb.append("  LEFT JOIN SCHREG_REGD_HDAT T3 ");
        stb.append("    ON T3.YEAR = T1.YEAR ");
        stb.append("   AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("   AND T3.GRADE = T1.GRADE ");
        stb.append("   AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("   AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("   AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._classSelected) + " ");
        stb.append(" GROUP BY ");
        stb.append("  T1.SCHREGNO, ");
        stb.append("  T1.GRADE, ");
        stb.append("  T1.HR_CLASS, ");
        stb.append("  T3.HR_NAME, ");
        stb.append("  T1.ATTENDNO, ");
        stb.append("  BM.NAME ");
        stb.append(" ORDER BY ");
        if ("1".equals(_param._sort_order)) {
            stb.append("  VALUE(M_SCORE, 0) DESC, ");
        }
        stb.append("  T1.GRADE, ");
        stb.append("  T1.HR_CLASS, ");
        stb.append("  T1.ATTENDNO ");

        return stb.toString();
    }

    private void getSchregMap(final DB2UDB db2, final Map rtnMap) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String singakuSql = getDetailSql();
        log.debug(" sql = " + singakuSql);
        try {
            ps = db2.prepareStatement(singakuSql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String schregNo = rs.getString("SCHREGNO");
            	final String testDate = rs.getString("TEST_DATE");
            	final String score = StringUtils.defaultString(rs.getString("SCORE"), "");
            	ScoreDetail addwk = new ScoreDetail(testDate, score);
            	if (rtnMap.containsKey(schregNo)) {
            		Student insWk = (Student)rtnMap.get(schregNo);
            		insWk._detailMap.put(testDate, addwk);
            	}
            }
        } catch (SQLException e) {
            log.error("exception!!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return;
    }

    private String getDetailSql() {
        final StringBuffer stb = new StringBuffer();

    	stb.append(" WITH DATE_BASE AS ( ");
    	stb.append(" SELECT DISTINCT ");
    	stb.append("   T2.TEST_DATE ");
    	stb.append(" FROM ");
    	stb.append("  SCHREG_REGD_DAT T1 ");
    	stb.append("  INNER JOIN AFT_TOTAL_STUDY_TOEFL_DAT T2 ");
    	stb.append("    ON T2.SCHREGNO = T1.SCHREGNO ");
    	stb.append("   AND T2.TEST_DATE <= '" + _param._date + "' ");
    	stb.append("   AND T2.TEST_DATE >= '" + _param._sDate + "' ");
    	stb.append(" WHERE ");
    	stb.append("   T1.YEAR = '" + _param._ctrlYear + "' ");
    	stb.append("   AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
    	stb.append("   AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._classSelected) + " ");  //年組で制限しているが、トーフル実施対象日全部なら、ここを外して"誰か受けてるはず"で対処。
    	stb.append(" ORDER BY ");
    	stb.append("   T2.TEST_DATE ");
    	stb.append(" ) ");
    	stb.append(" SELECT ");
    	stb.append("  T1.SCHREGNO, ");
    	stb.append("  T2.TEST_DATE, ");
    	stb.append("  T3.SCORE ");
    	stb.append(" FROM ");
    	stb.append("  SCHREG_REGD_DAT T1, ");
    	stb.append("  DATE_BASE T2 ");
    	stb.append("  LEFT JOIN AFT_TOTAL_STUDY_TOEFL_DAT T3 ");
    	stb.append("    ON T3.SCHREGNO = T1.SCHREGNO ");
    	stb.append("   AND T3.TEST_DATE = T2.TEST_DATE ");
    	stb.append(" WHERE ");
    	stb.append("   T1.YEAR = '" + _param._ctrlYear + "' ");
    	stb.append("   AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
    	stb.append("   AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._classSelected) + " ");
    	stb.append(" ORDER BY ");
    	stb.append("  T1.SCHREGNO, ");
    	stb.append("  T2.TEST_DATE ");

        return stb.toString();
    }

    private class Student {
        private final String _schregNo;
        private final String _name;
        private final String _grade;
        private final String _hrClass;
        private final String _hrName;
        private final String _attendNo;
        private final String _mScore;
        private final Map _detailMap;
        Student (final String schregNo, final String grade, final String hrClass, final String hrName, final String attendNo, final String name, final String mScore)
        {
        	_schregNo = schregNo;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _attendNo = attendNo;
            _name = name;
            _mScore = mScore;
            _detailMap = new LinkedMap();
        }
    }

    private class ScoreDetail {
        final String _testDate;
        final String _score;
        public ScoreDetail (final String testDate, final String score)
        {
            _testDate = testDate;
            _score = score;
        }
    }
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71450 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    public class Param {
    	public final String _ctrlYear;
    	public final String _ctrlSemester;
    	public final String _ctrlDate;
    	public final String[] _classSelected;

        private boolean _isSeireki;

        private final String _sort_order;
        private final String _sDate;
    	private final String _date;
    	private final String _grade;
    	private final String _gradeName;
//    	private final String _useCurriculumcd;
//    	private final String _use_prg_schoolkind;
//    	private final String _selectSchoolKind;
//    	private final String _useSchool_KindField;
//    	private final String _schoolCd;
//    	private final String _schoolKind;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {

            _sort_order = request.getParameter("SORT_ORDER");
            _date = request.getParameter("EDATE").replace('/', '-');
            _grade = request.getParameter("GRADE");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE").replace('/', '-');
            _classSelected = request.getParameterValues("CATEGORY_SELECTED");

//            _useCurriculumcd = request.getParameter("useCurriculumcd");
//            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
//            _selectSchoolKind = request.getParameter("selectSchoolKind");
//            _useSchool_KindField = request.getParameter("useSchool_KindField");
//            _schoolCd = request.getParameter("SCHOOLCD");
//            _schoolKind = request.getParameter("SCHOOLKIND");

            setSeirekiFlg(db2);
            _gradeName = getGradeName(db2);
            _sDate = get2YearsAgo();
        }

        private String get2YearsAgo() {
        	String retStr = "";
        	try {
        	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date convWk = sdf.parse(_date);
                Calendar cal = Calendar.getInstance();
        	    cal.setTime(convWk);
        	    cal.add(Calendar.YEAR, -2);
        	    cal.add(Calendar.DAY_OF_MONTH, -1);
        	    final String oginai = (cal.get(Calendar.MONTH)+1) > 9 ? "" : "0";
        	    retStr = cal.get(Calendar.YEAR) + "-" + oginai + (cal.get(Calendar.MONTH)+1) + "-" + cal.get(Calendar.DAY_OF_MONTH);
        	} catch (ParseException e) {
                log.error("exception!", e);
        	}
        	return retStr;
        }
//        private String getClassSelectedIn(final String[] classSelected) {
//            StringBuffer stb = new StringBuffer();
//            stb.append("(");
//            for (int i = 0; i < classSelected.length; i++) {
//                if (0 < i) stb.append(",");
//                stb.append("'" + classSelected[i] + "'");
////                if (KISOTSU.equals(classSelected[i])) {
////                    _isKisotsu = true;
////                }
//            }
//            stb.append(")");
//            return stb.toString();
//        }

//        private String getGouhiKubunName(final DB2UDB db2, String cd1, String cd2, final String field) {
//            if ("ALL".equals(cd2)) {
//                return "全て";
//            }
//            String rtnName = "";
//            try {
//                String sql = "SELECT " + field + " FROM NAME_MST WHERE NAMECD1='" + cd1 + "' AND NAMECD2='" + cd2 + "' ";
//                PreparedStatement ps = db2.prepareStatement(sql);
//                ResultSet rs = ps.executeQuery();
//                while( rs.next() ){
//                    rtnName = rs.getString(field);
//                }
//                ps.close();
//                rs.close();
//            } catch (Exception e) {
//                log.error("Exception", e);
//            } finally {
//                db2.commit();
//            }
//            return rtnName;
//        }

        private void setSeirekiFlg(final DB2UDB db2) {
            try {
                _isSeireki = false;
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while( rs.next() ){
                    if (rs.getString("NAME1").equals("2")) _isSeireki = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
        }
        private String getGradeName(final DB2UDB db2) {
        	return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _ctrlYear + "' AND GRADE = '" + _grade + "' "));
        }

//        public String changePrintDate(final String date) {
//            if (null == date) {
//                return "";
//            }
//            if (_isSeireki) {
//                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
//            } else {
//                return KNJ_EditDate.h_format_JP(date);
//            }
//        }

//        public String changePrintYear(final String year) {
//            if (null == year) {
//                return "";
//            }
//            if (_isSeireki) {
//                return year + "年度";
//            } else {
//                return nao_package.KenjaProperties.gengou(Integer.parseInt(year)) + "年度";
//            }
//        }

//        public boolean isNamecdE012(final String cd1) {
//            return "E012".equals(cd1);
//        }
//
//        public boolean isNamecdE005(final String cd1) {
//            return "E005".equals(cd1);
//        }
//
//        public boolean isNamecdE006(final String cd1) {
//            return "E006".equals(cd1);
//        }

    }
}

// eof
