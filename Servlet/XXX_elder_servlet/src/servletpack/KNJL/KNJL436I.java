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
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: ec6e39fa27623064bcc929cf12f95cabf543b412 $
 */
public class KNJL436I {

    private static final Log log = LogFactory.getLog("KNJL436I.class");

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

		final Map applicantMap = getApplicantMap(db2); // 志願者Map

		if (applicantMap.isEmpty()) {
			return false;
		}

		log.debug(applicantMap.size());
		final int maxLine = 13; // 最大印字行
		final int maxCol = 8; // 最大印字列
		int line = 0; // 印字行
		int col = 1; //印字列

		for (Iterator ite = applicantMap.keySet().iterator(); ite.hasNext();) {
			final String Key = (String) ite.next();
			final Applicant applicant = (Applicant) applicantMap.get(Key);

			if (line > maxLine || line == 0) {
				if (line > maxLine) {
					svf.VrEndPage();
				}
				line = 1;
				svf.VrSetForm("KNJL436I.frm", 4);
				final String date = h_format_Seireki_MD(_param._date);
				svf.VrsOut("DATE", date + " " + _param._time); // 作成日付

				final String div = "1".equals(_param._outputDiv) ? "男子のみ" : "2".equals(_param._outputDiv) ? "女子のみ" : "男女共";
				svf.VrsOut("TITLE", _param._year + "年度　" + _param._testAbbv + "　　入学試験　合格者受験番号一覧表"); // タイトル
				svf.VrsOut("SELECT_DIV", "(" + div + ")"); // 抽出区分
			}

			svf.VrsOut("EXAM_NO" + col, applicant._examno); // 受験番号1

			col++;
			if(col > maxCol) {
				svf.VrEndRecord();
				line++; // 印字行
				col = 1;
			}
		}
		svf.VrsOut("TOTAL_COUNT", "以上" + applicantMap.size() + "名"); // 人数
		svf.VrEndRecord();
		svf.VrEndPage();

		return true;
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
			stb.append("   BASE.APPLICANTDIV, ");
			stb.append("   BASE.TESTDIV, ");
			stb.append("   BASE.EXAMNO, ");
			stb.append("   BASE.JUDGEMENT ");
			stb.append(" FROM ");
			stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ");
			stb.append(" WHERE ");
			stb.append("   BASE.ENTEXAMYEAR = '" + _param._year + "' AND ");
			stb.append("   BASE.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
			stb.append("   BASE.TESTDIV = '" + _param._testDiv + "' AND ");
			if(!"3".equals(_param._outputDiv)) {
				stb.append(" BASE.SEX = '" + _param._outputDiv + "' AND ");
			}
			stb.append("   BASE.JUDGEMENT = '1' ");
			stb.append(" ORDER BY ");
			stb.append("  BASE.EXAMNO ");

			log.debug(" applicant sql =" + stb.toString());

			ps = db2.prepareStatement(stb.toString());
			rs = ps.executeQuery();

			while (rs.next()) {
				final String entexamyear = rs.getString("ENTEXAMYEAR");
				final String applicantdiv = rs.getString("APPLICANTDIV");
				final String testdiv = rs.getString("TESTDIV");
				final String examno = rs.getString("EXAMNO");
				final String judgement = rs.getString("JUDGEMENT");

				final Applicant applicant = new Applicant(entexamyear, applicantdiv, testdiv, examno, judgement);

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
    	final String _judgement;


		public Applicant(final String entexamyear, final String applicantdiv, final String testdiv, final String examno,
				final String judgement) {
		    _entexamyear = entexamyear;
		    _applicantdiv = applicantdiv;
		    _testdiv = testdiv;
		    _examno = examno;
		    _judgement = judgement;
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
        final String _outputDiv;
        final String _testAbbv;
        final String _date;
        final String _time;
        final String _schoolKind;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _outputDiv = request.getParameter("OUTPUT_DIV");
            _schoolKind = request.getParameter("SCHOOLKIND");
          	_date = request.getParameter("CTRL_DATE");
          	_time = request.getParameter("TIME");
          	_testAbbv = getTestDivAbbv(db2);

        }

        private String getTestDivAbbv(final DB2UDB db2) {
        	return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT TESTDIV_ABBV FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '" + _year + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND TESTDIV = '" + _testDiv + "' "));
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
