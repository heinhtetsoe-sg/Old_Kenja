// kanji=漢字
/*
 * $Id: cfb39fb7d5235d1289338db9d63f53fdfb4b5168 $
 *
 * 作成日: 2009/10/23 14:42:55 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: cfb39fb7d5235d1289338db9d63f53fdfb4b5168 $
 */
public class KNJA082A {

    private static final Log log = LogFactory.getLog("KNJA082A.class");

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

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {

        //明細データをセット
        final String form;
        if (_param._printClassOld) {
        	form = "KNJA082A_3.frm";
        } else if (_param._printGojuon) {
        	form = "KNJA082A_2.frm";
        } else {
        	form = ("1".equals(_param._row)) ? "KNJA082A_4.frm" : "KNJA082A_1.frm";
        }

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            svf.VrSetForm(form, 1);

        	//SQL
        	final String sql = sqlClass(_param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            printClass(svf, rs);
        } catch (Exception ex) {
            log.warn("printMain read error!", ex);
        } finally {
        	DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    //ヘッダ印刷
    private void printHead(Vrw32alp svf) {
        svf.VrsOut("DATE", _param._printDate );
    }

    /**クラス別明細データをセット*/
    private void printClass(final Vrw32alp svf, final ResultSet rs) {
    	final int rowMax;
    	final int lineMax;
    	boolean goju = false;
        if (_param._printClassOld) {
    		rowMax = 7;
    		lineMax = 50;
        } else if (_param._printGojuon) {
            goju = true;
    		rowMax = 4;
    		lineMax = 50;
        } else {
    		rowMax = ("1".equals(_param._row)) ? 6 : 8;
    		lineMax = 50;
        }

        int row  = 1;               //列カウンタ MAX rowMax
        int line = 1;               //行カウンタ MAX lineMax
    	int boycnt  = 0;            //男子カウンタ
    	int girlcnt = 0;            //女子カウンタ
        String classchange = "*";   //改列用
        String gradechange = "*";   //改列用
        try {
        	if (goju) {
        		/**五十音別明細データをセット*/
                while (rs.next()) {
                    if (line > lineMax) {
                        line = 1;
                        row++;
                    }
                    if (row > rowMax) {
                        //ヘッダ印刷
                        printHead(svf);
                        svf.VrEndPage();
                        line = 1;
                        row  = 1;
                    }
                    if (!gradechange.equals("*") && !gradechange.equalsIgnoreCase(rs.getString("GRADE"))) {
                        //ヘッダ印刷
                        printHead(svf);
                        svf.VrEndPage();
                        row  = 1;
                        line = 1;
                    }
                    if (classchange.equals("*")) {
                        svf.VrsOutn("SORT"+row        ,line   ,rs.getString("CLASSCHANGE"));
                        line++;
                    } else if (!classchange.equals("*") && !classchange.equalsIgnoreCase(rs.getString("CLASSCHANGE"))) {
                        svf.VrsOutn("SORT"+row        ,line   ,rs.getString("CLASSCHANGE"));
                        line++;
                    }
                    if (line > lineMax) {
                        line = 1;
                        row++;
                    }
                    if (row > rowMax) {
                        //ヘッダ印刷
                        printHead(svf);
                        svf.VrEndPage();
                        line = 1;
                        row  = 1;
                    }
                    if (_param._printFurigana) {
                        if (rs.getString("NAME_KANA") != null ) {
                            if (rs.getString("NAME_KANA").length() <= 10) {
                                svf.VrsOutn("KANA"+row+"_1"       ,line   ,rs.getString("NAME_KANA"));
                            } else {
                                svf.VrsOutn("KANA"+row+"_2"       ,line   ,rs.getString("NAME_KANA"));
                            }
                        }
                    }
                    svf.VrsOutn("NAME"+row        ,line   ,rs.getString("NAME"));
                    svf.VrsOutn("HR_NAME"+row     ,line   ,rs.getString("HR_NAME"));
                    line++;
                    classchange = rs.getString("CLASSCHANGE");
                    gradechange = rs.getString("GRADE");
                    _hasData = true;
                }
                if (line > 1) {
                    //ヘッダ印刷
                    printHead(svf);
                    svf.VrEndPage();
                }
        	} else {
        		while (rs.next()) {
        			if (!classchange.equals("*") && !classchange.equalsIgnoreCase(rs.getString("CLASSCHANGE"))) {
        				svf.VrsOut("BOY"+row      ,String.valueOf(boycnt));
        				svf.VrsOut("GIRL"+row     ,String.valueOf(girlcnt));
        				svf.VrsOut("TOTAL"+row    ,String.valueOf(boycnt + girlcnt));
        				girlcnt = 0;
        				boycnt  = 0;
        				line = 1;
        				row++;
        			}
        			if (!gradechange.equals("*") && !gradechange.equalsIgnoreCase(rs.getString("GRADE"))) {
        				//ヘッダ印刷
        				printHead(svf);
        				svf.VrEndPage();
        				girlcnt = 0;
        				boycnt  = 0;
        				line = 1;
        				row  = 1;
        			}
        			if (line > lineMax) {
        				line = 1;
        				row++;
        			}
        			if (row > rowMax) {
        				//ヘッダ印刷
        				printHead(svf);
        				svf.VrEndPage();
        				line = 1;
        				row = 1;
        			}
        			svf.VrsOut("HR_NAME"+row      ,rs.getString("HR_NAME") );
        			svf.VrsOut("STAFF_NAME"+row   ,rs.getString("STAFFNAME") );
        			if (_param._printFurigana) {
        				svf.VrsOutn("KANA"+row        ,line   , rs.getString("NAME_KANA") );
        			}
        			svf.VrsOutn("NAME"+row        ,line   , rs.getString("NAME") );
        			svf.VrsOutn("ATTENDNO"+row    ,line   , rs.getString("ATTENDNO") );
        			if (_param._printClassOld) {
                        String oldNullCheke = rs.getString("OLD_NULL_CHEKE");
                        svf.VrsOutn("OLD_ATTENDNO"+row    ,line   , (null == oldNullCheke) ? "" : rs.getString("OLD_ATTENDNO"));
        			}
        			if (null != rs.getString("SEX") && rs.getString("SEX").equals("1")) {
        				boycnt++;
        			} else {
        				girlcnt++;
        			}
        			line++;
        			classchange = rs.getString("CLASSCHANGE");
        			gradechange = rs.getString("GRADE");
        			_hasData = true;
        		}
        		if (line > 1) {
        			svf.VrsOut("BOY"+row      ,String.valueOf(boycnt));
        			svf.VrsOut("GIRL"+row     ,String.valueOf(girlcnt));
        			svf.VrsOut("TOTAL"+row    ,String.valueOf(boycnt + girlcnt));
        			//ヘッダ印刷
        			printHead(svf);
        			svf.VrEndPage();
        		}
        	}
        } catch (Exception ex) {
        	log.warn("printMeisai_1 read error!", ex);
        }
    }

    private String sqlClass(final Param param) {
    	boolean isGoju = false;
        if (_param._printClassOld) {
        } else if (_param._printGojuon) {
        	isGoju = true;
        } else {
        }

    	final StringBuffer stb = new StringBuffer();
    	stb.append("WITH MAIN_T AS ( ");
    	stb.append("SELECT ");
        if (isGoju) {
        	// 五十音別明細データを抽出
            stb.append("    VALUE(T3.HR_NAMEABBV,'') AS HR_NAME, ");
            stb.append("    VALUE(T1.ATTENDNO,'000') AS ATTENDNO, ");
            stb.append("    T1.GRADE, ");
            stb.append("    VALUE(CASE WHEN T5.NAME_KANA IS NULL THEN SUBSTR(T2.NAME_KANA,1,3) ELSE SUBSTR(T5.NAME_KANA,1,3) END, '') AS CLASSCHANGE ");

        } else {
        	// クラス別明細データを抽出
        	stb.append("    T3.HR_NAME, ");
        	stb.append("    T4.STAFFNAME, ");
        	stb.append("    T1.ATTENDNO, ");
        	stb.append("    CASE WHEN T5.SEX IS NULL OR T5.SEX = '' THEN T2.SEX ELSE T5.SEX END AS SEX, ");
        	stb.append("    T1.GRADE, ");
        	stb.append("    T1.HR_CLASS, ");
        	stb.append("    VALUE(T1.GRADE,'00') || VALUE(T1.HR_CLASS,'000') AS CLASSCHANGE ");
        	if (_param._printClassOld) {
                stb.append("   ,rtrim(char(smallint(VALUE(T1.OLD_GRADE,'00')))) || '-' || rtrim(char(smallint(VALUE(T1.OLD_HR_CLASS,'000'))))  || '-' || rtrim(char(smallint(VALUE(T1.OLD_ATTENDNO,'000')))) AS OLD_ATTENDNO ");
                stb.append("   ,T1.OLD_GRADE || '-' || T1.OLD_HR_CLASS || '-' || T1.OLD_ATTENDNO AS OLD_NULL_CHEKE ");
        	}
        }
    	stb.append("    , CASE WHEN T5.NAME      IS NULL OR T5.NAME = ''      THEN T2.NAME      ELSE T5.NAME      END AS NAME ");
    	stb.append("    , CASE WHEN T5.NAME_KANA IS NULL OR T5.NAME_KANA = '' THEN T2.NAME_KANA ELSE T5.NAME_KANA END AS NAME_KANA ");
        stb.append("FROM ");
        stb.append("    CLASS_FORMATION_DAT T1 ");
        stb.append("    LEFT JOIN FRESHMAN_DAT T2 ON T2.ENTERYEAR = T1.YEAR ");
        stb.append("      AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("    LEFT JOIN SCHREG_BASE_MST T5 ON T5.SCHREGNO = T1.SCHREGNO ");
        stb.append("    LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("      AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("      AND T3.GRADE = T1.GRADE ");
        stb.append("      AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append("    LEFT JOIN STAFF_MST T4 ON T4.STAFFCD = T3.TR_CD1 ");
        stb.append("    LEFT JOIN SCHREG_REGD_GDAT T6 ON T6.YEAR = T1.YEAR ");
        stb.append("      AND T6.GRADE = T1.GRADE ");
        stb.append("WHERE ");
        stb.append("    T1.YEAR = '"+_param._nextYear+"' ");
        stb.append("    AND T1.SEMESTER = '1' ");
        if ("1".equals(_param._use_prg_schoolkind)) {
            if (!StringUtils.isBlank(_param._selectSchoolKind)) {
                stb.append("        AND T6.SCHOOL_KIND IN " + _param._selectSchoolKindSql + "  ");
            }
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
            stb.append("   AND T6.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
        }
        if (!_param._isGradeAll) {
        	stb.append("    AND T1.GRADE = '"+_param._grade+"' ");
        }
        stb.append(") ");
        stb.append("SELECT ");
        stb.append("* ");
        stb.append("FROM ");
        stb.append("    MAIN_T T1 ");
        if (isGoju) {
            stb.append("ORDER BY ");
            stb.append("    GRADE, ");
            stb.append("    NAME_KANA ");
        } else {
        	stb.append("ORDER BY ");
        	stb.append("    T1.GRADE, ");
        	stb.append("    T1.HR_CLASS, ");
        	stb.append("    T1.ATTENDNO ");
        }
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
    	log.fatal("$Revision: 74747 $");
    	KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _nextYear;
        private final String _grade;
        private final boolean _isGradeAll;
        private final boolean _printClass; // クラス別を出力するか
        private final boolean _printGojuon;// 五十音別を出力するか
        private final boolean _printFurigana;//ふりがなを出力するか
        private final boolean _printClassOld;//旧クラスを出力するか
        private final String _row; //1:6列、2:8列
        final String _useSchool_KindField;
        final String _SCHOOLKIND;
        final String _use_prg_schoolkind;
        final String _selectSchoolKind;
        String _selectSchoolKindSql;

        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _printDate;
        private boolean _isSeireki;

        //private final int _classCount;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _nextYear = request.getParameter("YEAR");
            _grade = request.getParameter("GRADE");
            _isGradeAll = "99".equals(_grade);
            String output = request.getParameter("OUTPUT"); // 1:クラス別, 2:五十音別
            _printClass = "1".equals(output);
            _printGojuon = "2".equals(output);
            String output3 = request.getParameter("OUTPUT3");
            _printFurigana = null != output3;
            String output4 = request.getParameter("OUTPUT4");
            _printClassOld = null != output4;
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _row = request.getParameter("ROW");

            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _selectSchoolKind = request.getParameter("selectSchoolKind");
            if (!StringUtils.isBlank(_selectSchoolKind)) {
                StringBuffer stb = new StringBuffer("('");
                final String[] split = StringUtils.split(_selectSchoolKind, ":");
                if (null != split) {
                    for (int i = 0; i < split.length; i++) {
                        stb.append(split[i] + "', '");
                    }
                }
                _selectSchoolKindSql = stb.append("')").toString();
            }

            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _printDate = printDate(db2, _ctrlDate);
            setSeirekiFlg(db2);

            //_classCount = setClassCnt(db2);
        }

        private void setSeirekiFlg(final DB2UDB db2) {
        	PreparedStatement ps = null;
        	ResultSet rs = null;
            try {
                _isSeireki = false;
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while( rs.next() ){
                    if (rs.getString("NAME1").equals("2")) _isSeireki = true; //西暦
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
            	DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public String printDate(final DB2UDB db2, final String date) {
            if (null == date) {
                return "";
            }
            if (_isSeireki) {
                return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
            } else {
                return KNJ_EditDate.h_format_JP(db2, date);
            }
        }

        public String printYear(final DB2UDB db2, final String year) {
            if (null == year) {
                return "";
            }
            if (_isSeireki) {
                return year + "年度";
            } else {
                return KNJ_EditDate.gengou(db2, Integer.parseInt(year)) + "年度";
            }
        }

        public int setClassCnt(final DB2UDB db2) {
            int rtn = 0;
            if (!_printClassOld && !_printGojuon) {
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    String sql = ClassCountSql();
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while( rs.next() ){
                        rtn = Integer.parseInt(rs.getString("CLASS_COUNT"));
                    }
                } catch (Exception e) {
                    log.error("Exception", e);
                } finally {
                	DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
            }
            return rtn;
        }

        private String ClassCountSql() {

        	final StringBuffer stb = new StringBuffer();
        	stb.append("WITH MAIN_T AS ( ");
        	stb.append("SELECT DISTINCT ");
        	stb.append("    T1.GRADE, ");
        	stb.append("    T1.HR_CLASS ");
            stb.append("FROM ");
            stb.append("    CLASS_FORMATION_DAT T1 ");
            stb.append("    LEFT JOIN SCHREG_REGD_GDAT T6 ON T6.YEAR = T1.YEAR ");
            stb.append("      AND T6.GRADE = T1.GRADE ");
            stb.append("WHERE ");
            stb.append("    T1.YEAR = '"+_nextYear+"' ");
            stb.append("    AND T1.SEMESTER = '1' ");
            if ("1".equals(_use_prg_schoolkind)) {
                if (!StringUtils.isBlank(_selectSchoolKind)) {
                    stb.append("        AND T6.SCHOOL_KIND IN " + _selectSchoolKindSql + "  ");
                }
            } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                stb.append("   AND T6.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            }
            if (!_isGradeAll) {
            	stb.append("    AND T1.GRADE = '"+_grade+"' ");
            }
            stb.append(") ");
            stb.append("SELECT ");
            stb.append("    T1.GRADE, ");
            stb.append("    COUNT(T1.HR_CLASS) AS CLASS_COUNT ");
            stb.append("FROM ");
            stb.append("    MAIN_T T1 ");
            stb.append("GROUP BY T1.GRADE ");
            stb.append("ORDER BY CLASS_COUNT DESC ");
            stb.append("FETCH FIRST 1 ROWS ONLY ");
            return stb.toString();
        }
    }
}

// eof
