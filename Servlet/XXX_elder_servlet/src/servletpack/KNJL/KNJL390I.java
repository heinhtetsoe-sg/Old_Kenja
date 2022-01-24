// kanji=漢字
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 5ae09b507432b66589f8e427975062bedc85f9ef $
 */
public class KNJL390I {

    private static final Log log = LogFactory.getLog("KNJL390I.class");

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

    	final Map applicantMap = getApplicantMap(db2); //志願者Map

    	if(applicantMap.isEmpty()) {
        	return false;
        }

    	final int maxLine = 50;
    	int page = 0; // ページ
    	int line = 1; //印字行

    	for (Iterator ite = applicantMap.keySet().iterator(); ite.hasNext();) {
        	final String Key = (String)ite.next();
        	final Applicant applicant = (Applicant)applicantMap.get(Key);

        	if (line > maxLine || page == 0) {
        		if(line > maxLine) svf.VrEndPage();
				page++;
				line = 1;
		    	svf.VrSetForm("KNJL390I.frm", 1);
				svf.VrsOut("PAGE", String.valueOf(page) + "頁"); //ページ
				final String date =  h_format_Seireki_MD(_param._date);
				svf.VrsOut("DATE", date + " " + _param._time); //作成日時
				final String div = "1".equals(_param._disp) ? "男女共" : "2".equals(_param._disp) ? "男子のみ" : "女子のみ";
				svf.VrsOut("TITLE", _param._year + "年度入学試験　" + _param._testAbbv + "　調査書等資料確認表  (" + div + ")" ); //タイトル
				svf.VrsOut("SCHOOL_NAME", _param._schoolName); //学校名
				svf.VrsOut("CLASS_NAME1_1", "資１"); //資料1
				svf.VrsOut("CLASS_NAME2_1", "資２"); //資料2
				svf.VrsOut("CLASS_NAME3_1", "資３"); //資料3
				svf.VrsOut("CLASS_NAME4_1", "資４"); //資料4
				svf.VrsOut("CLASS_NAME5_1", "資５"); //資料5
				svf.VrsOut("CLASS_NAME6_1", "資６"); //資料6
				svf.VrsOut("CLASS_NAME7_1", "資７"); //資料7
        	}

        	svf.VrsOutn("EXAM_NO1", line, applicant._examno); //受験番号
        	final String fieldName = getFieldName(applicant._name);
        	svf.VrsOutn("NAME" + fieldName, line, applicant._name); //氏名
        	svf.VrsOutn("SCORE1", line, applicant._remark1); //資料1
        	svf.VrsOutn("SCORE2", line, applicant._remark2); //資料2
        	svf.VrsOutn("SCORE3", line, applicant._remark3); //資料3
        	svf.VrsOutn("SCORE4", line, applicant._remark4); //資料4
        	svf.VrsOutn("SCORE5", line, applicant._remark5); //資料5
        	svf.VrsOutn("SCORE6", line, applicant._remark6); //資料6
        	svf.VrsOutn("SCORE7", line, applicant._remark7); //資料7

            line++;
		}
    	svf.VrEndPage();

    return true;
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

		try {
			final StringBuffer stb = new StringBuffer();
			stb.append(" SELECT ");
			stb.append("   BASE.ENTEXAMYEAR, ");
			stb.append("   BASE.EXAMNO, ");
			stb.append("   BASE.NAME, ");
			stb.append("   BASE.APPLICANTDIV, ");
			stb.append("   BASE.TESTDIV, ");
			stb.append("   T1.REMARK1 AS REMARK1, ");
			stb.append("   T2.REMARK1 AS REMARK2, ");
			stb.append("   T3.REMARK1 AS REMARK3, ");
			stb.append("   T4.REMARK1 AS REMARK4, ");
			stb.append("   T5.REMARK1 AS REMARK5, ");
			stb.append("   T6.REMARK1 AS REMARK6, ");
			stb.append("   T7.REMARK1 AS REMARK7 ");
			stb.append(" FROM ");
			stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ");
			stb.append(" LEFT JOIN ");
			stb.append("   ENTEXAM_DOCUMENT_VIEW_DAT T1 ON T1.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND T1.APPLICANTDIV = BASE.APPLICANTDIV AND T1.TESTDIV = BASE.TESTDIV AND T1.EXAMNO = BASE.EXAMNO AND T1.SEQ = '001' ");
			stb.append(" LEFT JOIN ");
			stb.append("   ENTEXAM_DOCUMENT_VIEW_DAT T2 ON T2.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND T2.APPLICANTDIV = BASE.APPLICANTDIV AND T2.TESTDIV = BASE.TESTDIV AND T2.EXAMNO = BASE.EXAMNO AND T2.SEQ = '002' ");
			stb.append(" LEFT JOIN ");
			stb.append("   ENTEXAM_DOCUMENT_VIEW_DAT T3 ON T3.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND T3.APPLICANTDIV = BASE.APPLICANTDIV AND T3.TESTDIV = BASE.TESTDIV AND T3.EXAMNO = BASE.EXAMNO AND T3.SEQ = '003' ");
			stb.append(" LEFT JOIN ");
			stb.append("   ENTEXAM_DOCUMENT_VIEW_DAT T4 ON T4.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND T4.APPLICANTDIV = BASE.APPLICANTDIV AND T4.TESTDIV = BASE.TESTDIV AND T4.EXAMNO = BASE.EXAMNO AND T4.SEQ = '004' ");
			stb.append(" LEFT JOIN ");
			stb.append("   ENTEXAM_DOCUMENT_VIEW_DAT T5 ON T5.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND T5.APPLICANTDIV = BASE.APPLICANTDIV AND T5.TESTDIV = BASE.TESTDIV AND T5.EXAMNO = BASE.EXAMNO AND T5.SEQ = '005' ");
			stb.append(" LEFT JOIN ");
			stb.append("   ENTEXAM_DOCUMENT_VIEW_DAT T6 ON T6.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND T6.APPLICANTDIV = BASE.APPLICANTDIV AND T6.TESTDIV = BASE.TESTDIV AND T6.EXAMNO = BASE.EXAMNO AND T6.SEQ = '006' ");
			stb.append(" LEFT JOIN ");
			stb.append("   ENTEXAM_DOCUMENT_VIEW_DAT T7 ON T7.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND T7.APPLICANTDIV = BASE.APPLICANTDIV AND T7.TESTDIV = BASE.TESTDIV AND T7.EXAMNO = BASE.EXAMNO AND T7.SEQ = '007' ");
			stb.append(" WHERE ");
			stb.append("     BASE.ENTEXAMYEAR = '" + _param._year + "' AND ");
			stb.append("     BASE.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
			stb.append("     BASE.TESTDIV = '" + _param._testDiv + "' ");
			if("2".equals(_param._disp)) {
				stb.append("     AND BASE.SEX = '1' ");
			} else if("3".equals(_param._disp)) {
				stb.append("     AND BASE.SEX = '2' ");
			}
			stb.append(" ORDER BY BASE.EXAMNO");

			log.debug(" applicant sql =" + stb.toString());

			ps = db2.prepareStatement(stb.toString());
			rs = ps.executeQuery();

			while (rs.next()) {
				final String entexamyear = rs.getString("ENTEXAMYEAR");
				final String examno = rs.getString("EXAMNO");
				final String name = rs.getString("NAME");
				final String applicantdiv = rs.getString("APPLICANTDIV");
				final String testdiv = rs.getString("TESTDIV");
				final String remark1 = rs.getString("REMARK1");
				final String remark2 = rs.getString("REMARK2");
				final String remark3 = rs.getString("REMARK3");
				final String remark4 = rs.getString("REMARK4");
				final String remark5 = rs.getString("REMARK5");
				final String remark6 = rs.getString("REMARK6");
				final String remark7 = rs.getString("REMARK7");

				final Applicant applicant = new Applicant(entexamyear, applicantdiv, testdiv, examno, name, remark1, remark2, remark3, remark4, remark5, remark6, remark7);

			    if(!retMap.containsKey(examno)) {
			    	retMap.put(examno, applicant);
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
        final String _examno;
        final String _name;
        final String _remark1;
        final String _remark2;
        final String _remark3;
        final String _remark4;
        final String _remark5;
        final String _remark6;
        final String _remark7;

    	public Applicant(
				final String entexamyear, final String applicantdiv, final String testdiv,final String examno,
				final String name, final String remark1, final String remark2, final String remark3,
				final String remark4, final String remark5, final String remark6, final String remark7) {
    	    _entexamyear = entexamyear;
    	    _applicantdiv = applicantdiv;
    	    _testdiv = testdiv;
    	    _examno = examno;
    	    _name = name;
    	    _remark1 = remark1;
    	    _remark2 = remark2;
    	    _remark3 = remark3;
    	    _remark4 = remark4;
    	    _remark5 = remark5;
    	    _remark6 = remark6;
    	    _remark7 = remark7;
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
        final String _disp;  //抽出区分　1:全員 2:男子のみ 3:女子のみ
        final String _testAbbv;
        final String _date;
        final String _schoolKind;
        final String _schoolName;
        final String _time;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _disp = request.getParameter("OUTPUT_DIV");
            _schoolKind = request.getParameter("SCHOOLKIND");
          	_date = request.getParameter("CTRL_DATE");
          	_testAbbv = getTestAbbv(db2);
          	_schoolName = getSchoolName(db2);
          	_time = request.getParameter("TIME");

        }
        private String getTestAbbv(final DB2UDB db2) {
        	return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT TESTDIV_ABBV FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '" + _year + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND TESTDIV = '" + _testDiv + "' "));
        }

        private String getSchoolName(final DB2UDB db2) {
        	final String kindcd = "1".equals(_applicantDiv) ? "105" : "106";
        	return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '" + kindcd + "' "));

        }
    }

    public static String h_format_Seireki_MD(final String date) {
        if (null == date) {
            return date;
        }
        SimpleDateFormat sdf = new SimpleDateFormat();
        String retVal = "";
        sdf.applyPattern("yyyy年M月d日");
        retVal = sdf.format(java.sql.Date.valueOf(date));

        return retVal;
    }
}

// eof
