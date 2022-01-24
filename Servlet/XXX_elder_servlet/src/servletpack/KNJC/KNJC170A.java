// kanji=漢字
/*
 * $Id: efd4987c68292f08de07be771811391aad7f836a $
 *
 */
package servletpack.KNJC;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/*
 *  出欠状況表
 */

public class KNJC170A {

    private static final Log log = LogFactory.getLog(KNJC170A.class);
    private static final String TARGET_GRADE = "Paint=(1,90,2),Bold=1";
    private Param _param;
    private boolean _hasdataAll = false;
    private boolean _hasdata = false;
    private int MAX_LINE = 40;
    List _outputcsvdata;

    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        final Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）

        log.debug(" $Id: efd4987c68292f08de07be771811391aad7f836a $ ");
        KNJServletUtils.debugParam(request, log);

        try {
            response.setContentType("application/pdf");
            // ＤＢ接続
            DB2UDB db2 = null;
            try {
                db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
                db2.open();
            } catch (Exception ex) {
                log.error("db2 open error!", ex);
                return;
            }
            _param = new Param(request, db2);

	        svf.VrInit();                             //クラスの初期化
	        svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定
            // 印刷処理
            printMain(svf, db2);

        } catch (Exception ex) {
            log.error("exception!", ex);
        } finally {
	        // 終了処理
	        if (!_hasdataAll) {
	            svf.VrSetForm("MES001.frm", 0);
	            svf.VrsOut("note" , "note");
	            svf.VrEndPage();
	        }
	        svf.VrQuit();

        }
    }

    private void printMain(final Vrw32alp svf, final DB2UDB db2) throws Exception {

    	svf.VrSetForm("KNJC170A.frm", 1);

    	//生徒毎の出欠情報リストを取得
        final List<SchregRow> schregRowList = getSchregRowList(db2);
        //学期と月の対応リストを取得
        final List<String> semeMonthList = getSemeMonthList(db2);
        
        _hasdataAll = false;

	  	Map<String, String> lessonMap = getLessonMap(db2);
        
        //HRクラス単位のループ
        final Map<String, HrClass> hrClassMap = getHrClassMap(db2);
        for (final String gHrclass : hrClassMap.keySet()) {
        	
        	//タイトル
        	svf.VrsOut("TITLE",   String.format("%s年度", _param._ctrlYear) + " " + "出欠状況表");
        	svf.VrsOut("PRINT_DATE", getSystemDateStr());

        	HrClass hrClassObj = hrClassMap.get(gHrclass);
    		svf.VrsOut("HR_NAME", hrClassObj._hrName);
    		svf.VrsOut("TR_NAME", hrClassObj._staffName);

    		//帳票の月ヘッダ部分を表示
    		int col1 = 1;
    		for (final String semeMonth : semeMonthList) {
            	String[] semeAndMonthArray = semeMonth.split("-");		//学期-学期名-月
            	String semester = semeAndMonthArray[0];
            	String sName 	= semeAndMonthArray[1];
            	String month 	= semeAndMonthArray[2];

        	  	final String lesson = lessonMap.get(semester + "-" + month);
        	  	
        	  	final String date_left = "999".equals(month) ? sName : month.replaceFirst("^0+", "") + "月"; //月が999なら学期名を表示、それ以外は月を表示
        	  	final String date_right = StringUtils.defaultIfEmpty(lesson, "  ") + "日";
        	  	svf.VrsOut("DATE" + col1, date_left + "  " + date_right);
        	  	col1++;
    		}

    		//生徒毎の出欠データ部表示
    		_hasdata = false;
            int line = 1;
            int col2 = 1;
    		List<SchregRow> schregRowHrList = hrClassObj._schregRowHrList;
    		for (SchregRow sr: schregRowHrList) {
    			final String fieldName = getMS932ByteLength(sr._name) > 16 ? "NAME2" : "NAME1";
    			svf.VrsOutn(fieldName, line, sr._name);
    			svf.VrsOutn("NO", line, String.valueOf(line));
                for (String semeMonth : semeMonthList) {
                	String[] semeAndMonthArray = semeMonth.split("-");	//学期-学期名-月
                	String semester = semeAndMonthArray[0];
                	String month 	= semeAndMonthArray[2];
                	
            	  	MonthAttendance ma = sr.getMonthAttendance(semester + "-" + month);
            	  	
            	  	if (ma == null) {
            	  		col2++;
            	  		continue;
            	  	}
            	  	
            	  	//表示用文字列に変換
            	  	final String dispSuspend 	= convDispStr(ma._suspend);
            	  	final String dispAbsent 	= convDispStr(ma._kesseki);
            	  	final String dispLate 		= convDispStr(ma._late);
            	  	final String dispEarly 		= convDispStr(ma._early);
            	  	
            	  	svf.VrsOutn("SUSPEND" + col2, line, dispSuspend);
            	  	svf.VrsOutn("ABSENT"  + col2, line, dispAbsent);
            	  	svf.VrsOutn("LATE"    + col2, line, dispLate);
            	  	svf.VrsOutn("EARLY"   + col2, line, dispEarly);
            	  	col2++;
            	  	_hasdata = true;
                }
                col2 = 1;
                line++;                
    		}
    		
    		if (_hasdata) {
    			_hasdataAll = true;
    			svf.VrEndPage();
    		}
    		line = 1;
        }
        
    }
    
    //SQLで取得した出停・欠席・遅刻・早退の数値文字列を表示向けに変換
    private String convDispStr(String num) {
    	//ブランク・Null・0をすべてブランクに置き換え
    	if (num == null) return "";
    	if ("".equals(num)) return "";
    	if (Integer.parseInt(num) < 1) return "";
    	
    	return num;   	
    }
    
    private int getMS932ByteLength(final String str) {
    	return KNJ_EditEdit.getMS932ByteLength(str);
    }

    private String getSystemDateStr() {
    	Date date = new Date();
    	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    	String formattedDate = dateFormat.format(date);
    	    	
    	return formattedDate;
    }
    
    //クラスごとの出席すべき日数を取得
    private Map getLessonMap(final DB2UDB db2) {
    	
    	Map lessonMap = new LinkedHashMap();
    	
    	StringBuffer stb = new StringBuffer();
        stb.append("  SELECT ");
        stb.append("   VALUE(SEMESTER, '999') || '-' || VALUE(MONTH, '999') AS SEME_MONTH, ");
        stb.append("   SUM(LESSON) AS LESSON ");
        stb.append("  FROM ");
        stb.append("    ATTEND_LESSON_MST ");
        stb.append("  WHERE ");
        stb.append("    YEAR = '" + _param._ctrlYear + "' ");
        stb.append("    AND GRADE = '" + _param._grade + "' ");
        stb.append("    AND COURSECD || MAJORCD = '1100' ");	//※課程学科は固定(駒沢用)
        stb.append("    GROUP BY ");
        stb.append("  GROUPING SETS ( ");
        stb.append("  (SEMESTER,MONTH), (SEMESTER),() ");
        stb.append("  ) ");
        
        final String sql = stb.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
        	ps = db2.prepareStatement(sql);
        	rs = ps.executeQuery();
        	while (rs.next()) {
        		final String semeMonth = rs.getString("SEME_MONTH");
        		final String lesson = rs.getString("LESSON");
        		lessonMap.put(semeMonth, lesson);
        	}
        	
        } catch(SQLException ex) {
        	log.debug("exception!", ex);
        } finally {
        	DbUtils.closeQuietly(null, ps, rs);
        }
        
        return lessonMap;
    }
    
    //対象クラス情報
    private Map getHrClassMap(final DB2UDB db2) {
    	Map hrClassMap = new LinkedHashMap();
    	
    	StringBuffer stb = new StringBuffer();
        stb.append("  SELECT ");
        stb.append("    T1.GRADE, ");
        stb.append("    T1.HR_CLASS, ");
        stb.append("    T1.HR_NAME, ");
        stb.append("    T2.STAFFNAME ");
        stb.append("  FROM ");
        stb.append("    SCHREG_REGD_HDAT T1 ");
        stb.append("    LEFT JOIN STAFF_MST T2 ");
        stb.append("      ON T1.TR_CD1 = T2.STAFFCD ");
        stb.append("  WHERE ");
        stb.append("    YEAR = '" + _param._ctrlYear + "' ");
        stb.append("    AND SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("    AND GRADE || '-' ||  HR_CLASS IN " + _param._selectedIn + " ");
        stb.append("   ORDER BY ");
        stb.append("     GRADE, ");
        stb.append("     HR_CLASS ");
        
        final String sql = stb.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
        	ps = db2.prepareStatement(sql);
        	rs = ps.executeQuery();
        	while (rs.next()) {
        		final String grade 		= rs.getString("GRADE");
        		final String hrClass 	= rs.getString("HR_CLASS");
        		final String hrName 	= rs.getString("HR_NAME");
        		final String staffName 	= rs.getString("STAFFNAME");

        		HrClass hrClassObj = new HrClass(grade, hrClass, hrName, staffName);
        		hrClassMap.put(grade + "-" + hrClass, hrClassObj);
        	}
        	
        } catch(SQLException ex) {
        	log.debug("exception!", ex);
        } finally {
        	DbUtils.closeQuietly(null, ps, rs);
        }
        
        //HRクラスごとに所属する生徒情報を振り分ける
        final List<SchregRow> schregRowList = getSchregRowList(db2);
        for(SchregRow sr : schregRowList) {
        	((HrClass)hrClassMap.get(sr._grade + "-" + sr._hrClass)).addSchregRow(sr);        	
        }
        
        return hrClassMap;

    }
    
    private List getSchregRowList(final DB2UDB db2) {
    	
		List schregRowList = new ArrayList();
    	
    	StringBuffer stb = new StringBuffer();
        stb.append("  WITH MAIN_DATA AS ( ");
        stb.append("    SELECT ");
        stb.append("      T1.YEAR, ");
        stb.append("      T2.GRADE, ");
        stb.append("      T2.HR_CLASS, ");
        stb.append("      VALUE (T1.SEMESTER, '999') AS SEMESTER, ");
        stb.append("      VALUE (T1.MONTH, '999') AS MONTH, ");
        stb.append("      T1.SCHREGNO, ");
        stb.append("      T2.ATTENDNO, ");
        stb.append("      VALUE(SUM(T1.SUSPEND), 0) ");
        if ("true".equals(_param._useVirus)) {
        	stb.append("      + VALUE(SUM(T1.VIRUS), 0) ");
        }
        if ("true".equals(_param._useKoudome)) {
        	stb.append("      + VALUE(SUM(KOUDOME), 0)");
        }
        stb.append("      AS SUM_SUSPEND, ");
        stb.append("      VALUE(SUM(T1.SICK), 0) + VALUE(SUM(T1.NOTICE), 0) + VALUE(SUM(NONOTICE), 0) ");
        if ("1".equals(_param._knjSchoolMst._semOffDays)) {
        	stb.append("      + VALUE(SUM(OFFDAYS), 0) ");
        }
        stb.append("      AS SUM_KESSEKI, ");
        stb.append("      SUM(T1.LATE) AS SUM_LATE, ");
        stb.append("      SUM(T1.EARLY) AS SUM_EARLY ");
        stb.append("    FROM ");
        stb.append("      ATTEND_SEMES_DAT T1 ");
        stb.append("      INNER JOIN SCHREG_REGD_DAT T2 ");
        stb.append("        ON T1.YEAR = T2.YEAR ");
        stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
        stb.append("        AND T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("    WHERE ");
        stb.append("      T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("      AND T2.GRADE || '-' || T2.HR_CLASS IN " + _param._selectedIn + " ");
        stb.append("  GROUP BY ");
        stb.append("    GROUPING SETS ( ");
        stb.append("    (T2.GRADE, T2.HR_CLASS, T2.ATTENDNO, T1.YEAR, T1.SEMESTER, T1.MONTH, T1.SCHREGNO), ");	//月計
        stb.append("    (T2.GRADE, T2.HR_CLASS, T2.ATTENDNO, T1.YEAR, T1.SEMESTER, T1.SCHREGNO), ");			//学期計
        stb.append("    (T2.GRADE, T2.HR_CLASS, T2.ATTENDNO, T1.YEAR, T1.SCHREGNO) ");							//年度計
        stb.append("    ) ");
        stb.append("  ) ");
        stb.append("  SELECT ");
        stb.append("    T1.GRADE, ");
        stb.append("    T1.HR_CLASS, ");
        stb.append("    T1.YEAR, ");
        stb.append("    T1.SEMESTER, ");
        stb.append("    T1.MONTH, ");
        stb.append("    T1.SCHREGNO, ");
        stb.append("    T1.SUM_SUSPEND, ");
        stb.append("    T1.SUM_KESSEKI, ");
        stb.append("    T1.SUM_LATE, ");
        stb.append("    T1.SUM_EARLY, ");
        stb.append("    T2.NAME, ");
        stb.append("    T3.HR_NAME, ");
        stb.append("    T4.STAFFNAME ");
        stb.append("  FROM ");
        stb.append("    MAIN_DATA T1 ");
        stb.append("    LEFT JOIN SCHREG_BASE_MST T2 ");
        stb.append("      ON T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("    LEFT JOIN SCHREG_REGD_HDAT T3 ");
        stb.append("       ON T1.YEAR 		= T3.YEAR ");
        stb.append("      AND T1.SEMESTER 	= T3.SEMESTER ");
        stb.append("      AND T1.GRADE 		= T3.GRADE ");
        stb.append("      AND T1.HR_CLASS 	= T3.HR_CLASS ");
        stb.append("    LEFT JOIN STAFF_MST T4 ");
        stb.append("       ON T3.TR_CD1 	= T4.STAFFCD ");
        stb.append("  ORDER BY ");
        stb.append("    T1.GRADE, ");
        stb.append("    T1.HR_CLASS, ");
        stb.append("    T1.ATTENDNO, ");
        stb.append("    T1.SCHREGNO, ");
        stb.append("    T1.SEMESTER, ");
        stb.append("    CASE ");
        stb.append("      WHEN T1.MONTH < '04' ");
        stb.append("      THEN INT (T1.MONTH) + 12 ");
        stb.append("      ELSE T1.MONTH ");
        stb.append("      END ");
    	
    	String sql = stb.toString();
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	try {
    		ps = db2.prepareStatement(sql);
    		rs = ps.executeQuery();
    		
    		String befSchregno = "";
    		SchregRow sr = null;
    		while (rs.next()) {

    			final String schregno 	= rs.getString("SCHREGNO");

    			if ("".equals(befSchregno)|| !schregno.equals(befSchregno)) {
    				//SchregRowインスタンス作成
        			final String grade 		= rs.getString("GRADE");
        			final String hrClass 	= rs.getString("HR_CLASS");
        			final String name 		= rs.getString("NAME");
        			final String hrname 		= rs.getString("HR_NAME");
        			final String staffname = rs.getString("STAFFNAME");

    				sr = new SchregRow(grade, hrClass, schregno, name);
    				sr.setYobiInfo(hrname, staffname);
    				schregRowList.add(sr);
    			}

    			final String semester	= rs.getString("SEMESTER");
    			final String month 		= rs.getString("MONTH");
    			final String suspend 	= rs.getString("SUM_SUSPEND");
    			final String kesseki 	= rs.getString("SUM_KESSEKI");
    			final String late 		= rs.getString("SUM_LATE");
    			final String early 		= rs.getString("SUM_EARLY");
    			
    			MonthAttendance ma = new MonthAttendance(month, suspend, kesseki, late, early);
    			sr.putMonthAttendance(semester + "-" + month, ma);
    			
    			befSchregno = schregno;
    		}
    		
    	} catch(SQLException e) {
    		log.debug("Exception:", e);
    	} finally {
    		DbUtils.closeQuietly(null,ps,rs);
    	}
        
        return schregRowList;
    }

    //学期毎の月情報取得
    private List getSemeMonthList(final DB2UDB db2) {
    	StringBuffer stb = new StringBuffer();
        stb.append("  SELECT ");
        stb.append("    SEMESTER, ");
        stb.append("    SEMESTERNAME, ");
        stb.append("    MONTH(SDATE) AS SMONTH, ");
        stb.append("    MONTH(EDATE) AS EMONTH ");
        stb.append("  FROM ");
        stb.append("    SEMESTER_MST ");
        stb.append("  WHERE ");
        stb.append("    YEAR = '"+_param._ctrlYear+"' ");
        stb.append("    AND SEMESTER <> '9' ");

        String sql = stb.toString();
        List semeMonthList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
	            final String semester = rs.getString("SEMESTER");
	            final String sName = rs.getString("SEMESTERNAME");
	            
	            //forループ用の一時変数
	            int sMonth = Integer.parseInt(StringUtils.defaultIfEmpty(rs.getString("SMONTH"), "99"));
	            sMonth = sMonth < 4 ? sMonth + 12 : sMonth;
	            int eMonth = Integer.parseInt(StringUtils.defaultIfEmpty(rs.getString("EMONTH"), "99"));
	            eMonth = eMonth < 4 ? eMonth + 12 : eMonth;
	            
		          for (int month = sMonth; month <= eMonth; month++) {
		        	  	semeMonthList.add(semester + "-" + sName + "-" + String.format("%02d", (month > 12 ? month - 12 : month)));
		          }
		          
		        //学期計用に設定する仮の月情報　(ここでは月が999のものはその学期の合計月とする)
		          semeMonthList.add(semester + "-" + sName + "-" + "999");
            }

            //学年計用に設定する仮の月情報  (ここでは学期と月が999のものはその学年の合計月とする)
            semeMonthList.add("999-学年計-999");

        } catch (Exception e) {
            log.error("exception!" + sql, e);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return semeMonthList;
    }
    
    //HRクラスのクラス
    private class HrClass {
    	final String _grade;
    	final String _hrClass;
    	final String _hrName;
    	final String _staffName;
    	final List _schregRowHrList;
    	
    	HrClass (final String grade, final String hrClass, final String hrName, final String staffName) {
    		_grade = grade;
    		_hrClass = hrClass;
    		_hrName = hrName;
    		_staffName = staffName;
    		_schregRowHrList = new ArrayList();
    	}
    	
    	public void addSchregRow(SchregRow sr) {
    		_schregRowHrList.add(sr);
    	}
    }
    
    //生徒毎の出席情報
    private class SchregRow {
    	final String _grade;
    	final String _hrClass;
    	final String _schregno;
    	final String _name;
    	String _hrname;
    	String _staffname;
    	final Map<String, MonthAttendance> _monthAttendanceMap;
    	
    	SchregRow(final String grade, final String hrClass, final String schregno, final String name) {
    		_grade = grade;
    		_hrClass = hrClass;
    		_schregno = schregno;
    		_name = name;
    		_monthAttendanceMap = new LinkedHashMap();
    	}
    	public void setYobiInfo(final String hrname, final String staffname) {
    		_hrname = hrname;
    		_staffname = staffname;
    	}
    	
    	public void putMonthAttendance(String month, MonthAttendance ma) {
    		_monthAttendanceMap.put(month, ma);
    	}
    	
    	public MonthAttendance getMonthAttendance(String month) {
    		return _monthAttendanceMap.get(month);
    	}
    }
    
    //生徒毎・月毎の出席情報
    private class MonthAttendance {
    	final String _month;
    	final String _suspend;    //出席停止
    	final String _kesseki;    //欠席
    	final String _late;       //遅刻
    	final String _early;	   //早退
    	
    	MonthAttendance(final String month, final String suspend, final String kesseki, final String late, final String early) {
    		_month = month;
    		_suspend = suspend;
    		_kesseki = kesseki;
    		_late = late;
    		_early = early;
    	}
    }

    private static class Param {
		private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _useVirus;
        private final String _useKoudome;
        private final String _grade;
        private String _selectedIn = "";

        private KNJSchoolMst _knjSchoolMst;


        Param(final HttpServletRequest request, final DB2UDB db2) {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester  = request.getParameter("CTRL_SEMESTER");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _grade = request.getParameter("GRADE");

            _ctrlDate = null == request.getParameter("CTRL_DATE") ? null : request.getParameter("CTRL_DATE").replace('/', '-');
            final String[] categorySelected = request.getParameterValues("CATEGORY_SELECTED"); // 学年＋組
            _selectedIn = "(";
            for (int i = 0; i < categorySelected.length; i++) {
                if (categorySelected[i] == null)
                    break;
                if (i > 0)
                    _selectedIn = _selectedIn + ",";
                _selectedIn = _selectedIn + "'" + categorySelected[i] + "'";
            }
            _selectedIn = _selectedIn + ")";


            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _ctrlYear);
            } catch (Exception ex) {
                log.error("Param load exception!", ex);
            }

        }

    }
}
