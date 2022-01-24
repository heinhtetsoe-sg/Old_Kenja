// kanji=漢字
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: d6078756a4cb122bb3c2618be9646a035ca88a76 $
 */
public class KNJL380I {

    private static final Log log = LogFactory.getLog("KNJL380I.class");

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

            _hasData = printMain(db2, svf);
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

    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {

    	final Map hall_Map = getApplicantMap(db2); //志願者Map

    	if(hall_Map.isEmpty()) {
        	return false;
        }

    	if("1".equals(_param._formDiv)) {
    		printInterview(svf, hall_Map); //面接班名簿
    	} else {
    		if(_param._className.size() == 0) {
        		log.error("テスト教科がありません。");
        		return false;
        	}
    		printAttend(svf, hall_Map); //出欠調査表
    	}


    return true;
    }

    //面接班名簿
    private void printInterview(final Vrw32alp svf, Map hall_Map) {
    	final int MaxLine = 30;
    	int page = 0; // ページ
    	for (Iterator ite = hall_Map.keySet().iterator(); ite.hasNext();) {
    		int line = 0; // 印字行
        	final String hallKey = (String)ite.next();
        	final Hall printHall = (Hall)hall_Map.get(hallKey);
        	for (Iterator hallite = printHall._applicantMap.keySet().iterator(); hallite.hasNext();) {
        		final String getKey = (String)hallite.next();
        		final Applicant applicant = (Applicant)printHall._applicantMap.get(getKey);
				if (line > MaxLine || line == 0) {
					if (line > MaxLine) svf.VrEndPage();
					svf.VrSetForm("KNJL380I_1.frm", 1);
					line = 1;
					page++;
					svf.VrsOut("PAGE", String.valueOf(page) + "頁"); //ページ
					final String date = _param._date != null ? _param._date.replace("-", "/") : "";
					svf.VrsOut("DATE", "作成日時：" + date + " " + _param._time); //作成日時
					svf.VrsOut("TITLE", "  " + _param._year + "年度入学試験" + "　" + printHall._testAbbv + "　面接班名簿《 " + printHall._examhall_Name + " 》"); //タイトル
					svf.VrsOut("SCHOOL_NAME", _param._schoolName); //学校名
				}
				svf.VrsOutn("EXAM_NO1", line, applicant._receptno); //受験番号
				final String nameField = getFieldName(applicant._name);
				svf.VrsOutn("NAME" + nameField, line, applicant._name); //氏名
				final String kanaField = getFieldName(applicant._name_Kana);
				svf.VrsOutn("KANA" + kanaField, line, applicant._name_Kana); //フリガナ
				final String schoolField = getFieldName(applicant._finschool_Name);
				svf.VrsOutn("FINSCHOOL_NAME" + schoolField, line, applicant._finschool_Name); //出身学校
				svf.VrsOutn("EXAM_NO2", line, applicant._recom_Examno); //他方式受験番号
            	line++;
        	}
        	svf.VrEndPage();
		}
    }

    //出欠調査表
    private void printAttend(final Vrw32alp svf, Map hall_Map) {
    	final int MaxCnt = 80;
    	final int MaxLine = 20;
    	for (Iterator ite = hall_Map.keySet().iterator(); ite.hasNext();) {
        	final String hallKey = (String)ite.next();
        	final Hall printHall = (Hall)hall_Map.get(hallKey);

        	for(String className : _param._className) {
        		int line = 0; //印字行
        		int col = 1; //列
        		int cnt = 1; //印字回数
            	for (Iterator hallite = printHall._applicantMap.keySet().iterator(); hallite.hasNext();) {
            		final String getKey = (String)hallite.next();
            		final Applicant applicant = (Applicant)printHall._applicantMap.get(getKey);


            		if(line > MaxLine) {
            			col++;
            			line = 1;
            		}

    				if (cnt > MaxCnt || line == 0) {
    					if (cnt > MaxCnt) svf.VrEndPage();
    					svf.VrSetForm("KNJL380I_2.frm", 1);
    					line = 1;
    					col = 1;
    					cnt = 1;
    					svf.VrsOut("TITLE", _param._year + "年度入学試験" + " " + printHall._testAbbv + " 出欠調査表"); //タイトル
    					svf.VrsOut("GROUP_NAME", printHall._examhall_Name); //班名
    					svf.VrsOut("CLASS_NAME", "   " + className); //教科名
    					svf.VrsOut("GROUP_EXAM_NO1", printHall._s_Receptno); //受験番号始
    					svf.VrsOut("GROUP_EXAM_NO2", printHall._e_Receptno); //受験番号至
    				}
    				svf.VrsOutn("EXAM_NO" + col, line, applicant._receptno); //受験番号
                	line++;
                	cnt++;
            	}
            	svf.VrEndPage();
        	}
		}
    }

    private String getFieldName(final String str) {
    	final int keta = KNJ_EditEdit.getMS932ByteLength(str);
    	return keta <= 20 ? "1" : keta <= 30 ? "2" : "3" ;
    }

    // 志願者取得
    private Map getApplicantMap(final DB2UDB db2) throws SQLException {
    	Map retMap = new LinkedMap();
    	PreparedStatement ps = null;
        ResultSet rs = null;
        Hall hall = null;

		try {
			final StringBuffer stb = new StringBuffer();
			stb.append(" SELECT ");
			stb.append("   T1.ENTEXAMYEAR, ");
			stb.append("   T1.APPLICANTDIV, ");
			stb.append("   T1.TESTDIV, ");
			stb.append("   T1.EXAM_TYPE, ");
			stb.append("   T1.EXAMHALLCD, ");
			stb.append("   T1.EXAMHALL_NAME, ");
			stb.append("   T1.S_RECEPTNO, ");
			stb.append("   T1.E_RECEPTNO, ");
			stb.append("   T5.EXAMNO, ");
			stb.append("   T2.NAME, ");
			stb.append("   T2.NAME_KANA, ");
			stb.append("   T2.FS_CD, ");
			stb.append("   T3.FINSCHOOL_NAME, ");
			stb.append("   T2.RECOM_EXAMNO, ");
			stb.append("   T4.TESTDIV_ABBV, ");
			stb.append("   T5.RECEPTNO ");
			stb.append(" FROM ");
			stb.append("   ENTEXAM_HALL_YDAT T1 ");
			stb.append(" LEFT JOIN ");
			stb.append("   ENTEXAM_RECEPT_DAT T5 ON T5.ENTEXAMYEAR = T1.ENTEXAMYEAR AND T5.APPLICANTDIV = T1.APPLICANTDIV AND T5.TESTDIV = T1.TESTDIV AND T5.RECEPTNO BETWEEN T1.S_RECEPTNO AND T1.E_RECEPTNO ");
			stb.append(" LEFT JOIN ");
			stb.append("   ENTEXAM_APPLICANTBASE_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR AND T2.APPLICANTDIV = T1.APPLICANTDIV AND T2.TESTDIV = T1.TESTDIV AND T2.EXAMNO = T5.EXAMNO ");
			stb.append(" LEFT JOIN ");
			stb.append("   FINSCHOOL_MST T3 ON T3.FINSCHOOLCD = T2.FS_CD ");
			stb.append(" LEFT JOIN ");
			stb.append("   ENTEXAM_TESTDIV_MST T4 ON T4.ENTEXAMYEAR = T1.ENTEXAMYEAR AND T4.APPLICANTDIV = T1.APPLICANTDIV AND T4.TESTDIV = T1.TESTDIV ");
			stb.append(" WHERE ");
			stb.append("   T1.ENTEXAMYEAR = '" + _param._year + "' ");
			stb.append("   AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
			stb.append("   AND T1.TESTDIV = '" + _param._testDiv + "' ");
			stb.append("   AND T1.EXAM_TYPE = '1' ");
			if("2".equals(_param._disp)){ //出力対象　班指定
				stb.append("   AND T1.EXAMHALLCD = '" + _param._hallCd + "' ");
			} else { //出力対象全て
				if("1".equals(_param._formDiv)) {
					stb.append("   AND T1.EXAMHALLCD LIKE '2%' "); //面接のみ
				} else {
					stb.append("   AND T1.EXAMHALLCD LIKE '1%' "); //受験のみ
				}
			}
			stb.append(" ORDER BY ");
			stb.append("   T1.EXAMHALLCD,T2.EXAMNO ");

			log.debug(" applicant sql =" + stb.toString());

			ps = db2.prepareStatement(stb.toString());
			rs = ps.executeQuery();

			while (rs.next()) {
				final String entexamyear = rs.getString("ENTEXAMYEAR");
				final String applicantdiv = rs.getString("APPLICANTDIV");
				final String testdiv = rs.getString("TESTDIV");
				final String exam_Type = rs.getString("EXAM_TYPE");
				final String examhallcd = rs.getString("EXAMHALLCD");
				final String examhall_Name = rs.getString("EXAMHALL_NAME");
				final String s_Receptno = rs.getString("S_RECEPTNO");
				final String e_Receptno = rs.getString("E_RECEPTNO");
				final String examno = rs.getString("EXAMNO");
				final String receptno = rs.getString("RECEPTNO");
				final String name = rs.getString("NAME");
				final String name_Kana = rs.getString("NAME_KANA");
				final String fs_Cd = rs.getString("FS_CD");
				final String finschool_Name = rs.getString("FINSCHOOL_NAME");
				final String recom_Examno = rs.getString("RECOM_EXAMNO");
				final String testabbv = rs.getString("TESTDIV_ABBV");

				final Applicant applicant = new Applicant(entexamyear, applicantdiv, testdiv, exam_Type, examhallcd,
						examhall_Name, examno, receptno, name, name_Kana, fs_Cd, finschool_Name,
						recom_Examno);
				if (retMap.containsKey(examhallcd )) {
					hall = (Hall)retMap.get(examhallcd);
				} else {
					hall = new Hall(entexamyear, applicantdiv, testdiv, exam_Type, examhallcd, examhall_Name, s_Receptno, e_Receptno, testabbv);
					retMap.put(examhallcd, hall);
				}

			    if(!hall._applicantMap.containsKey(examno)) {
			    	hall._applicantMap.put(examno, applicant);
			    }
			}
		} catch (final SQLException e) {
			log.error("志願者の基本情報取得でエラー", e);
			throw e;
		} finally {
			db2.commit();
			DbUtils.closeQuietly(null, ps, rs);
		}


    	return retMap;
    }

    private class Applicant {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _exam_Type;
        final String _examhallcd;
        final String _examhall_Name;
        final String _examno;
        final String _receptno;
        final String _name;
        final String _name_Kana;
        final String _fs_Cd;
        final String _finschool_Name;
        final String _recom_Examno;

    	public Applicant(
				final String entexamyear, final String applicantdiv, final String testdiv, final String exam_Type,
				final String examhallcd, final String examhall_Name, final String examno,
				final String receptno, final String name, final String name_Kana, final String fs_Cd,
				final String finschool_Name, final String recom_Examno) {
    	    _entexamyear = entexamyear;
    	    _applicantdiv = applicantdiv;
    	    _testdiv = testdiv;
    	    _exam_Type = exam_Type;
    	    _examhallcd = examhallcd;
    	    _examhall_Name = examhall_Name;
    	    _examno = examno;
    	    _receptno = receptno;
    	    _name = name;
    	    _name_Kana = name_Kana;
    	    _fs_Cd = fs_Cd;
    	    _finschool_Name = finschool_Name;
    	    _recom_Examno = recom_Examno;
    	}
    }

    private class Hall {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _exam_Type;
        final String _examhallcd;
        final String _examhall_Name;
        final String _s_Receptno;
        final String _e_Receptno;
        final Map _applicantMap;
        final String _testAbbv;

		public Hall(final String entexamyear, final String applicantdiv, final String testdiv, final String exam_Type,
				final String examhallcd, final String examhall_Name, final String s_Receptno, final String e_Receptno, final String testAbbv) {
            _entexamyear = entexamyear;
            _applicantdiv = applicantdiv;
            _testdiv = testdiv;
            _exam_Type = exam_Type;
            _examhallcd = examhallcd;
            _examhall_Name = examhall_Name;
            _s_Receptno = s_Receptno;
            _e_Receptno = e_Receptno;
            _testAbbv = testAbbv;
            _applicantMap = new LinkedMap();
        }
    }
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 76037 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _applicantDiv; //入試制度
        final String _testDiv; //入試区分
        final String _formDiv; //帳票タイプ　1:面接班名簿 2:出欠調査表
        final String _disp;  //抽出区分　1:全員 2:合格者のみ
        final String _hallCd; //会場コード
        final String _date;
        final String _time;
        final String _schoolKind;
        final List<String> _className;
        final String _schoolName; //学校名


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _formDiv = request.getParameter("FORMDIV");
            _disp = request.getParameter("DISP");
            _schoolKind = request.getParameter("SCHOOLKIND");
          	_hallCd = ("2".equals(_disp)) ? request.getParameter("EXAMHALLCD") : "";
          	_date = request.getParameter("DATE");
          	_time = request.getParameter("TIME");
          	_className = getClassName(db2);
          	_schoolName = getSchoolName(db2);
        }

        private String getSchoolName(final DB2UDB db2) {
        	return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '105' "));

        }

        private List<String> getClassName(final DB2UDB db2) {
        	final List<String> retList = new LinkedList();

        	if("1".equals(_formDiv)) {
        		return retList;
        	}
        	final StringBuffer stb = new StringBuffer();

        	stb.append(" SELECT ");
        	stb.append("   T2.NAME1 ");
        	stb.append(" FROM  ");
        	stb.append("   ENTEXAM_TESTSUBCLASSCD_DAT T1 ");
        	stb.append(" INNER JOIN ");
        	stb.append("   ENTEXAM_SETTING_MST T2 ON T2.ENTEXAMYEAR = '" + _year + "' AND T2.APPLICANTDIV = '" + _applicantDiv + "' AND T2.SETTING_CD = 'L009' AND T2.SEQ = T1.TESTSUBCLASSCD ");
        	stb.append(" WHERE ");
        	stb.append("   T1.ENTEXAMYEAR = '" + _year + "' ");
        	stb.append("   AND T1.APPLICANTDIV = '" + _applicantDiv + "' ");
        	stb.append("   AND T1.TESTDIV = '" + _testDiv + "' ");
        	stb.append("   AND T1.EXAM_TYPE = '1' ");

        	for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
        		retList.add(KnjDbUtils.getString(row, "NAME1"));
        	}
        	return retList;
        }
    }
}

// eof
