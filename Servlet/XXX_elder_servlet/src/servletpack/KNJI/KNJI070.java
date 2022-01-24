// kanji=漢字
/*
 * $Id: 8bc61053bc57912f35d57d52f8aedbb9b1fd008c $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJE.KNJE080J_1;
import servletpack.KNJE.KNJE080J_2;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KnjDbUtils;

/*
 *  学校教育システム 賢者 [卒業生管理]  学業成績証明書
 *  2005/11/18〜11/22 yamashiro  「処理日付をブランクで出力する」仕様の追加による修正
 *                          => 年度の算出は、処理日付がブランクの場合は印刷指示画面から受け取る「今年度」、処理日付がある場合は処理日付から割り出した年度とする
 */

public class KNJI070 {

	private static final Log log = LogFactory.getLog(KNJI070.class);

    private static final String CERTIF033_KNJE080_1_JUNIOR = "033"; // 成績証明書 中学用
    private static final String CERTIF034_KNJE080_2_JUNIOR = "034"; // 成績証明書 中学用 英語

	private boolean _hasData;                   //該当データなしフラグ

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        log.fatal("$Revision: 74267 $ $Date: 2020-05-14 09:53:36 +0900 (木, 14 5 2020) $");
        KNJServletUtils.debugParam(request, log);

        // ＤＢ接続
        DB2UDB db2;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception e) {
            log.error("[KNJI070]DB2 open error!", e);
            return;
        }
        final Param param = new Param(request);
        final List<Map<String, String>> studentList = getStudentList(request);

        final Vrw32alp svf = new Vrw32alp();

        response.setContentType("application/pdf");
        //  ＳＶＦ設定
        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());

        try {
            for (final Map<String, String> student : studentList) {
                printStudent(svf, db2, param, student);
            }
        } catch (Exception e) {
        	log.error("exception!", e);
        } finally {
        	//  終了処理
        	if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
        	}
            int ret = svf.VrQuit();
            if (ret < 0) {
            	log.error(" svf.VrQuit ret = " + ret);
            }
        	param.close();
        	db2.commit();
        	db2.close();
        }
    }

    private static List<Map<String, String>> getStudentList(final HttpServletRequest request) {
    	final String schregnoCsv = request.getParameter("SCHREGNO");
    	final String gYearCsv = request.getParameter("G_YEAR");
    	final String gSemesterCsv = request.getParameter("G_SEMESTER");
    	final String gGradeCsv = request.getParameter("G_GRADE");
    	
        final List<Map<String, String>> studentList = new ArrayList<Map<String, String>>();
        final StringTokenizer tschregno = new StringTokenizer(schregnoCsv, ",");      //学籍番号
        final StringTokenizer tyear = new StringTokenizer(gYearCsv, ",");        //卒業年度
        final StringTokenizer tseme = new StringTokenizer(gSemesterCsv, ",");    //卒業学期
        final StringTokenizer tgrade = new StringTokenizer(gGradeCsv, ",");       //卒業学年
        while (tschregno.hasMoreTokens() && tyear.hasMoreTokens() && tseme.hasMoreTokens() && tgrade.hasMoreTokens()) {
            final String schregno = tschregno.nextToken();                            //学籍番号
            final String year = tyear.nextToken();                            //卒業年度
            final String semester = tseme.nextToken();                            //卒業学期
            final String grade = tgrade.nextToken();                            //卒業学年

            final Map<String, String> student = new HashMap<String, String>();
            studentList.add(student);
            student.put("schregno", schregno);
            student.put("year", year);
            student.put("semester", semester);
            student.put("grade", grade);
        }
        return studentList;
    }

    private void printStudent(final Vrw32alp svf, final DB2UDB db2, final Param param, final Map<String, String> student) {
		final String schregno = student.get("schregno"); //学籍番号
		final String year = student.get("year"); //卒業年度
		final String semester = student.get("semester"); //卒業学期
		final String grade = student.get("grade"); //卒業学年
		
		if ("J".equals(param.getSchoolKind(db2, grade))) {
			// 中学成績証明書
		    if ("1".equals(param._output)) {
		    	// 和文
		    	param._paramap.put("CERTIFKIND", CERTIF033_KNJE080_1_JUNIOR);  // 証明書種別
		        if (null == param._knje080j_1) {
		        	param._knje080j_1 = new KNJE080J_1(db2, svf, param._paramap);
		        	param._knje080j_1.pre_stat(null);
		        }
		        param._knje080j_1.printSvf(year, semester, param._date, schregno, param._paramap, param._seki, Integer.parseInt((String) param._paramap.get("CERTIFKIND")), null, (String) param._paramap.get("NUMBER"));
		        if (param._knje080j_1.nonedata) {
		            _hasData = true;
		        }
		    } else { // if ("2".equals(param._output)) {
		    	// 英文
		    	param._paramap.put("CERTIFKIND", CERTIF034_KNJE080_2_JUNIOR);  // 証明書種別
		        if (null == param._knje080j_2) {
		        	param._knje080j_2 = new KNJE080J_2(db2, svf, param._paramap);
		        	param._knje080j_2.pre_stat(null);
		        }
		        param._knje080j_2.printSvf(year, semester, param._date, schregno, param._paramap, param._seki, Integer.parseInt((String) param._paramap.get("CERTIFKIND")), null, (String) param._paramap.get("NUMBER"));
		        if (param._knje080j_2.nonedata) {
		            _hasData = true;
		        }
		    }
		} else {
			// 高校成績証明書
		    if ("1".equals(param._output)) {
		    	// 和文
		    	param._paramap.put("CERTIFKIND", "027");
		    	if (null == param._knji070_1) {
		    		param._knji070_1 = new KNJI070_1(db2, svf, param._definecode);
		    		param._knji070_1.pre_stat(null, param._paramap);
		    	}
		        param._knji070_1.printSvf(year, semester, param._date, schregno, param._paramap, param._seki, Integer.parseInt((String) param._paramap.get("CERTIFKIND")), null, (String)param._paramap.get("NUMBER"));
		        if (param._knji070_1.nonedata) {
		            _hasData = true;
		        }
		    } else { // if ("2".equals(param._output)) {
		    	// 英文
		    	param._paramap.put("CERTIFKIND", "007");
		    	if (null == param._knji070_2) {
		    		param._knji070_2 = new KNJI070_2(db2, svf, param._definecode);
		    		param._knji070_2.pre_stat(null, param._paramap);
		    	}
		        param._knji070_2.printSvf(year, semester, param._date, schregno, param._paramap, param._seki, Integer.parseInt((String) param._paramap.get("CERTIFKIND")), null, (String)param._paramap.get("NUMBER"));
		        if (param._knji070_2.nonedata) {
		            _hasData = true;
		        }
		    }
		}
	}
    
    private static class Param {
    	final String _ctrlYear;
    	final String _output;
    	final String _seki;
        String _date = null;
        final Map _paramap = new HashMap();
        private final KNJDefineSchool _definecode = new KNJDefineSchool();  //各学校における定数等設定
        private final Map<String, String> _gradeSchoolKindMap = new HashMap<String, String>();

        private KNJE080J_1 _knje080j_1;
        private KNJE080J_2 _knje080j_2;
        private KNJI070_1 _knji070_1;
        private KNJI070_2 _knji070_2;

        /*  << request.Parameter >>
        OUTPUT      1               調査書種類 1:和文: 2:英文
    6   DATE        2003/06/05      記載日付
    3   SEKI        999999          記載責任者
    7   SCHREGNO    181010,181241   学籍番号
    0   G_YEAR      2002,2002       卒業時年度
    1   G_SEMESTER  3,3             卒業時学期
    2   G_GRADE     3,3             卒業時学年
         */
    	Param(final HttpServletRequest request) {
            _ctrlYear = request.getParameter("CTRL_YEAR");                            //年度
            _output = request.getParameter("OUTPUT");                        //出力種別(1:日本語 2:英語)
            _seki = request.getParameter("SEKI");                            //記載責任者
            //記載日がブランクの場合桁数０で渡される事に対応
            if (3 < StringUtils.defaultString(request.getParameter("DATE")).length()) {
            	_date = request.getParameter("DATE");                            //処理日付
            }
            
            _paramap.put("CTRL_YEAR", request.getParameter("CTRL_YEAR"));  //今年度
            
            if (_definecode.schoolmark.equals("TOK")) {
                _paramap.put("NUMBER", "");  // 証明書番号セット
            }
            if (!_paramap.containsKey("FORM6")) {
                if ("on".equals(request.getParameter("FORM6"))) {
                    _paramap.put("FORM6", "on");  // ６年生用フォーム offの場合はparamapに追加しない。
                }
            }
            _paramap.put("Knje080UseAForm", request.getParameter("Knje080UseAForm"));
            _paramap.put("seisekishoumeishoTaniPrintRyugaku", StringUtils.defaultString(request.getParameter("seisekishoumeishoTaniPrintRyugaku"), ""));
            _paramap.put("useCurriculumcd", request.getParameter("useCurriculumcd"));
            _paramap.put("useGakkaSchoolDiv", request.getParameter("useGakkaSchoolDiv"));
            _paramap.put("seisekishoumeishoNotPrintAnotherStudyrec", StringUtils.defaultString(request.getParameter("seisekishoumeishoNotPrintAnotherStudyrec"), ""));
            
            for (final Enumeration<String> enums = request.getParameterNames(); enums.hasMoreElements();) {
                final String parameterName = enums.nextElement();
                if (!_paramap.containsKey(parameterName) && !Arrays.asList("SCHREGNO", "G_YEAR", "G_SEMESTER", "G_GRADE").contains(parameterName)) {
                    _paramap.put(parameterName, request.getParameter(parameterName));
                }
            }
            _paramap.put("PRINT_GRD", "1");
    	}
    	
    	public String getSchoolKind(final DB2UDB db2, final String grade) {
    		if (!_gradeSchoolKindMap.containsKey(grade)) {
    			final String schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _ctrlYear + "' AND GRADE = '" + grade + "' "));
    			log.info(" grade = " + grade + ", schoolKind = " + schoolKind);
				_gradeSchoolKindMap.put(grade, schoolKind);
    		}
    		final String schoolKind = _gradeSchoolKindMap.get(grade);
    		return schoolKind;
    	}
    	
    	private void close() {
    		if (null != _knji070_1) {
    		    _knji070_1.pre_stat_f();
    		}
    		if (null != _knji070_2) {
    		    _knji070_2.pre_stat_f();
    		}
    		if (null != _knje080j_1) {
    		    _knje080j_1.pre_stat_f();
    		}
    		if (null != _knje080j_2) {
    		    _knje080j_2.pre_stat_f();
    		}
    	}
    }
}
