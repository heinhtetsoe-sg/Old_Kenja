// kanji=漢字
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id:$
 */
public class KNJL434H {

    private static final Log log = LogFactory.getLog("KNJL332A.class");

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
    	final int maxLine = 50; //最大行
    	final int maxCol = 3; //最大列
    	int page = 0; //ページ数
    	boolean printFlg = false;
    	final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._examYear)) + "年度 ";

    	//専併区分毎のループ
    	for(Iterator shIte = _param._shDivMap.keySet().iterator(); shIte.hasNext(); ) {
    		final String shKey = (String)shIte.next();
    		final String shName = (String)_param._shDivMap.get(shKey);

    		final Map applicantMap = getApplicantMap(db2, shKey); //志願者Map

    		//受験科目毎のループ
    		for(Iterator subclassIte = _param._testSubclassMap.keySet().iterator(); subclassIte.hasNext(); ) {
    			final String subclassKey = (String)subclassIte.next();
    			final String subclassName = (String)_param._testSubclassMap.get(subclassKey);

    			int line = 1; //印字行
    			int col = 1; //印字列
    			boolean first = true; //最初の帳票

    			for(Iterator ite = applicantMap.keySet().iterator(); ite.hasNext(); ) {

    				if(col > maxCol || first) {
    					if(page > 0) svf.VrEndPage();
    					first = false;
    					page++;
    					col = 1;
    					svf.VrSetForm("KNJL434H.frm", 1);
            			svf.VrsOut("PRINT_DATE", "作成日時：" + _param._date.replace("-", "/") + " " + _param._time); //日付
            			svf.VrsOut("TITLE", "入試点数チェックリスト　" + nendo); //タイトル
            			svf.VrsOut("SUBTITLE", _param._examTypeName + shName + "　　　【 " + subclassName + " 】"); //サブタイトル
            			svf.VrsOut("SCHOOL_NAME", _param._schoolName); //学校名
            			svf.VrsOut("PAGE", String.valueOf(page)); //ページ数
    				}

    				final String key = (String)ite.next();
    				final Applicant applicant = (Applicant)applicantMap.get(key);

    				svf.VrsOutn("EXAM_NO" + col, line, applicant._receptno); //受験番号
    				if(applicant._scoreMap.containsKey(subclassKey)) {
    					svf.VrsOutn("SCORE" + col, line, String.valueOf(applicant._scoreMap.get(subclassKey))); //得点
    				}

    				line++;
    				if(line > maxLine) {
    					line = 1;
    					col++;
    				}
    				printFlg = true;
    			}
    			svf.VrEndPage();
    		}
    	}

    	return printFlg;
    }


    private Map getApplicantMap(final DB2UDB db2, final String shdiv) throws SQLException {
    	Map retMap = new LinkedMap();
    	PreparedStatement ps = null;
        ResultSet rs = null;

        try{
        	final StringBuffer stb = new StringBuffer();
        	stb.append(" SELECT ");
        	stb.append("   RECEPT.RECEPTNO, ");
        	stb.append("   BASE.EXAMNO, ");
        	stb.append("   RECEPT.APPLICANTDIV, ");
        	stb.append("   RECEPT.TESTDIV, ");
        	stb.append("   RECEPT.EXAM_TYPE, ");
        	stb.append("   BASE.SHDIV, ");
        	stb.append("   SCORE.TESTSUBCLASSCD, ");
        	stb.append("   SCORE.SCORE ");
        	stb.append(" FROM ");
        	stb.append("   ENTEXAM_RECEPT_DAT RECEPT ");
        	stb.append(" LEFT JOIN ");
        	stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV AND BASE.TESTDIV = RECEPT.TESTDIV AND BASE.EXAMNO = RECEPT.EXAMNO ");
        	stb.append(" LEFT JOIN ");
        	stb.append("   ENTEXAM_SCORE_DAT SCORE ON SCORE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR AND SCORE.APPLICANTDIV = RECEPT.APPLICANTDIV AND SCORE.TESTDIV = RECEPT.TESTDIV AND SCORE.EXAM_TYPE = RECEPT.EXAM_TYPE AND SCORE.RECEPTNO = RECEPT.RECEPTNO ");
        	stb.append(" WHERE ");
        	stb.append("   RECEPT.ENTEXAMYEAR = '" + _param._examYear + "' AND ");
        	stb.append("   RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
        	stb.append("   RECEPT.EXAM_TYPE = '" + _param._examType + "' AND ");
        	stb.append("   BASE.SHDIV = '" + shdiv + "' ");
        	stb.append(" ORDER BY ");
        	stb.append("   RECEPT.RECEPTNO, SCORE.TESTSUBCLASSCD ");


        	log.debug(" applicant sql =" + stb.toString());

			ps = db2.prepareStatement(stb.toString());
			rs = ps.executeQuery();

			Applicant applicant;
			while (rs.next()) {
				final String receptno = rs.getString("RECEPTNO");
				final String cd = rs.getString("TESTSUBCLASSCD");
				final String score = rs.getString("SCORE");

				if(retMap.containsKey(receptno)) {
					applicant = (Applicant)retMap.get(receptno);
					if(!applicant._scoreMap.containsKey(cd)) {
						applicant._scoreMap.put(cd, score);
					}
				} else {
					final Map scoreMap = new LinkedMap();
					scoreMap.put(cd, score);
					applicant = new Applicant(receptno, scoreMap);
					retMap.put(receptno, applicant);
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
        final String _receptno; //受験番号
        final Map _scoreMap; //得点

		public Applicant(final String receptno, final Map scoreMap) {
			_receptno = receptno;
			_scoreMap = scoreMap;
		}
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Id:$");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _examYear; //入試年度
        final String _applicantDiv; //入試制度
        final String _testDiv; //入試区分
        final String _date; //日付
        final String _time; //時間
        final String _testSubclassCd; //受験科目
        final String _shDiv; //専併区分
        final String _examType; //受験型
        final String _examTypeName; //受験型名称
        final String _schoolName; //学校名称
        final Map _shDivMap; //ループ用専併区分
        final Map _testSubclassMap; //ループ用受験科目

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _examYear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _date = request.getParameter("LOGIN_DATE");
            _time = request.getParameter("TIME");
            _testSubclassCd = request.getParameter("TESTSUBCLASSCD");
            _shDiv = request.getParameter("SHDIV");
            _examType = request.getParameter("EXAMTYPE");
            _examTypeName = getExamTypeName(db2);
            _schoolName = getSchoolName(db2);
            _shDivMap = getShDivMap(db2, _shDiv);
            _testSubclassMap = getTestSubclassMap(db2, _testSubclassCd);

            log.info("");
        }

        private String getExamTypeName(final DB2UDB db2) {
        	return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT EXAMTYPE_NAME FROM ENTEXAM_EXAMTYPE_MST WHERE ENTEXAMYEAR = '" + _examYear + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND EXAM_TYPE = '" + _examType + "' "));
        }

        private String getSchoolName(final DB2UDB db2) {
        	final String cd = "1".equals(_applicantDiv) ? "105" : "106";
        	return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _examYear + "' AND CERTIF_KINDCD = '" + cd + "' "));
        }

        private Map getShDivMap(final DB2UDB db2, final String shdiv) {
        	final Map retMap = new LinkedMap();
			final StringBuffer stb = new StringBuffer();
			stb.append(" SELECT ");
			stb.append("     T1.SEQ, ");
			stb.append("     T1.NAME1 ");
			stb.append(" FROM ");
			stb.append("     ENTEXAM_SETTING_MST T1 ");
			stb.append(" WHERE ");
			stb.append("     T1.ENTEXAMYEAR = '" + _examYear + "' ");
			stb.append("     AND T1.APPLICANTDIV = '" + _applicantDiv + "' ");
			stb.append("     AND T1.SETTING_CD = 'L006' ");
			if(!"ALL".equals(shdiv)) {
				stb.append("     AND T1.SEQ = '" + shdiv + "' ");
        	}
			stb.append(" ORDER BY ");
			stb.append("     T1.SEQ ");

			for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
				final String seq = KnjDbUtils.getString(row, "SEQ");
				final String name = KnjDbUtils.getString(row, "NAME1");
				if(!retMap.containsKey(seq)) {
					retMap.put(seq, name);
				}
			}

        	return retMap;
        }

        private Map getTestSubclassMap(final DB2UDB db2, final String cd) {
        	final Map retMap = new LinkedMap();
			final StringBuffer stb = new StringBuffer();
			stb.append(" SELECT ");
			stb.append("     T1.TESTSUBCLASSCD, ");
			stb.append("     SET009.NAME1 ");
			stb.append(" FROM ");
			stb.append("     ENTEXAM_TESTSUBCLASSCD_DAT T1 ");
			stb.append(" LEFT JOIN ");
			stb.append("     ENTEXAM_SETTING_MST SET009 ON SET009.ENTEXAMYEAR = T1.ENTEXAMYEAR AND SET009.APPLICANTDIV = T1.APPLICANTDIV AND SET009.SETTING_CD = 'L009' AND SET009.SEQ = T1.TESTSUBCLASSCD ");
			stb.append(" WHERE ");
			stb.append("     T1.ENTEXAMYEAR = '" + _examYear + "' ");
			stb.append("     AND T1.APPLICANTDIV = '" + _applicantDiv + "' ");
			stb.append("     AND T1.TESTDIV = '" + _testDiv + "' ");
			stb.append("     AND T1.EXAM_TYPE = '" + _examType + "' ");
			if(!"ALL".equals(cd)) {
				stb.append(" AND T1.TESTSUBCLASSCD = '" + cd + "'");
        	}
			stb.append(" ORDER BY ");
			stb.append("     T1.TESTSUBCLASSCD ");

			for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
				final String testSubclassCd = KnjDbUtils.getString(row, "TESTSUBCLASSCD");
				final String testSubclassName = KnjDbUtils.getString(row, "NAME1");

				if(!retMap.containsKey(testSubclassCd)) {
					retMap.put(testSubclassCd, testSubclassName);
				}
			}
        	return retMap;
        }
    }
}

// eof
