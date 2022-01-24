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
 * @version $Id: e2fb38e4ac8d7f9f5c65b96c917e5e759d5e2a3f $
 */
public class KNJL439I {

    private static final Log log = LogFactory.getLog("KNJL439I.class");

    /** 1ページ 40明細 */
    private static final int MAXLINE = 40;

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

		final Map districtMap = getDistrictMap(db2); // 地区Map

		if (districtMap.isEmpty()) {
			return false;
		}

		final Map schoolHyouteiMap = getSchoolHyouteiMap(db2); // 学校評定Map
		final Map totalHyouteiMap = getTotalHyouteiMap(db2); // 地区評定Map

		int page_line[] = {0,1}; // ページ数、印字行数
		int grpcd = 1; // グループコード
		for (Iterator ite = districtMap.keySet().iterator(); ite.hasNext();) {
			final String Key = (String) ite.next();
			final District district = (District) districtMap.get(Key);

			page_line = checkLine(svf,page_line);
			svf.VrsOut("PREF_NAME", " " + district._districtName); // 地区名称
			svf.VrEndRecord();
			page_line[1]++;
			page_line = checkLine(svf,page_line);

			// 学校毎のレコード
			for (Iterator ite2 = district._schoolMap.keySet().iterator(); ite2.hasNext();) {
				final String Key2 = (String) ite2.next();
				if("ZZZ".equals(Key2)) continue;
				final School school = (School) district._schoolMap.get(Key2);
				final String field = getFieldName(school._finschool_Name);
				svf.VrsOut("FINSCHOOL_NAME" + field, school._finschool_Name);
				if(!"2".equals(_param._outputDiv)) {
					printRecord(svf, school,"1",schoolHyouteiMap,grpcd); // 男子計レコード
					page_line[1]++;
					page_line = checkLine(svf,page_line);
				}
				if(!"1".equals(_param._outputDiv)) {
					printRecord(svf, school,"2",schoolHyouteiMap,grpcd); // 女子計レコード
					page_line[1]++;
					page_line = checkLine(svf,page_line);
				}
				printRecord(svf, school,"ZZZ",schoolHyouteiMap,grpcd); // 学校計レコード
				page_line[1]++;
				page_line = checkLine(svf,page_line);
				grpcd++;
				if(grpcd > 99) grpcd = 1;
			}

			final School totalCount= (School)district._schoolMap.get("ZZZ");

			// 合計 男子
			if(!"2".equals(_param._outputDiv)) {
				svf.VrsOut("GRPCD2", "B");
				svf.VrsOut("TOTAL_NAME", "　男子計");
				svf.VrsOut("TOTAL_APPLICANT", printScore(totalCount._shutsugan_Sex1)); // 出願者数
				svf.VrsOut("TOTAL_HOPE", "2".equals(_param._testDiv) ? printScore(totalCount._shigan_Sex1) : printScore(totalCount._shutsugan_Sex1)); // 志願者数
				svf.VrsOut("TOTAL_EXAM", printScore(totalCount._juken_Sex1)); // 受験者数
				svf.VrsOut("TOTAL_PASS", printScore(totalCount._goukaku_Sex1)); // 合格者数
				printTotalHyoutei(svf,district._districtCd,totalHyouteiMap,"1");
				page_line[1]++;
				page_line = checkLine(svf,page_line);
			}

			// 合計 女子
			if(!"1".equals(_param._outputDiv)) {
				svf.VrsOut("GRPCD2", "B");
				svf.VrsOut("TOTAL_NAME", "　女子計");
				svf.VrsOut("TOTAL_APPLICANT", printScore(totalCount._shutsugan_Sex2)); // 出願者数
				svf.VrsOut("TOTAL_HOPE", "2".equals(_param._testDiv) ? printScore(totalCount._shigan_Sex2) : printScore(totalCount._shutsugan_Sex2)); // 志願者数
				svf.VrsOut("TOTAL_EXAM", printScore(totalCount._juken_Sex2)); // 受験者数
				svf.VrsOut("TOTAL_PASS", printScore(totalCount._goukaku_Sex2)); // 合格者数
				printTotalHyoutei(svf,district._districtCd,totalHyouteiMap,"2");
				page_line[1]++;
				page_line = checkLine(svf,page_line);
			}

			// 合計 男女
			svf.VrsOut("GRPCD2", "B");
			svf.VrsOut("TOTAL_NAME", "合　　計");
			svf.VrsOut("TOTAL_APPLICANT", printScore(totalCount._shutsugan)); // 出願者数
			svf.VrsOut("TOTAL_HOPE", "2".equals(_param._testDiv) ? printScore(totalCount._shigan) : printScore(totalCount._shutsugan)); // 志願者数
			svf.VrsOut("TOTAL_EXAM", printScore(totalCount._juken)); // 受験者数
			svf.VrsOut("TOTAL_PASS", printScore(totalCount._goukaku)); // 合格者数
			svf.VrsOut("TOTAL_DIV_AVE", printScore(totalCount._hyouteiall)); // 評定全て
			svf.VrsOut("TOTAL_DIV_PASS", printScore(totalCount._hyouteiok)); // 評定合格
			svf.VrsOut("TOTAL_DIV_FAIL", printScore(totalCount._hyouteing)); // 評定不合格
			printTotalHyoutei(svf,district._districtCd,totalHyouteiMap,"ZZZ");
			page_line[1]++;
		}

		page_line = checkLine(svf,page_line);

		//総合計
		if(!"2".equals(_param._outputDiv)) {
			svf.VrsOut("GRPCD2", "C");
			svf.VrsOut("TOTAL_NAME", "男子総計");
			svf.VrsOut("TOTAL_APPLICANT", printScore(_param._sougou._shutsugan_Sex1)); // 出願者数
			svf.VrsOut("TOTAL_HOPE", "2".equals(_param._testDiv) ? printScore(_param._sougou._shigan_Sex1) : printScore(_param._sougou._shutsugan_Sex1)); // 志願者数
			svf.VrsOut("TOTAL_EXAM", printScore(_param._sougou._juken_Sex1)); // 受験者数
			svf.VrsOut("TOTAL_PASS", printScore(_param._sougou._goukaku_Sex1)); // 合格者数
			printTotalHyoutei(svf,"ZZZ",totalHyouteiMap,"1");
			page_line[1]++;
			page_line = checkLine(svf,page_line);
		}

		if(!"1".equals(_param._outputDiv)) {
			svf.VrsOut("GRPCD2", "C");
			svf.VrsOut("TOTAL_NAME", "女子総計");
			svf.VrsOut("TOTAL_APPLICANT", printScore(_param._sougou._shutsugan_Sex2)); // 出願者数
			svf.VrsOut("TOTAL_HOPE", "2".equals(_param._testDiv) ? printScore(_param._sougou._shigan_Sex2) : printScore(_param._sougou._shutsugan_Sex2)); // 志願者数
			svf.VrsOut("TOTAL_EXAM", printScore(_param._sougou._juken_Sex2)); // 受験者数
			svf.VrsOut("TOTAL_PASS", printScore(_param._sougou._goukaku_Sex2)); // 合格者数
			printTotalHyoutei(svf,"ZZZ",totalHyouteiMap,"2");
			page_line[1]++;
			page_line = checkLine(svf,page_line);
		}

		svf.VrsOut("GRPCD2", "C");
		svf.VrsOut("TOTAL_NAME", "総　　計");
		svf.VrsOut("TOTAL_APPLICANT", printScore(_param._sougou._shutsugan)); // 出願者数
		svf.VrsOut("TOTAL_HOPE", "2".equals(_param._testDiv) ? printScore(_param._sougou._shigan) : printScore(_param._sougou._shutsugan)); // 志願者数
		svf.VrsOut("TOTAL_EXAM", printScore(_param._sougou._juken)); // 受験者数
		svf.VrsOut("TOTAL_PASS", printScore(_param._sougou._goukaku)); // 合格者数
		printTotalHyoutei(svf,"ZZZ",totalHyouteiMap,"ZZZ");
		page_line[1]++;

		svf.VrEndPage();

		return true;
	}

	private int[] checkLine(final Vrw32alp svf, final int[] page_line) {
		if (page_line[1] > MAXLINE || page_line[0] == 0) {
			if (page_line[1] > MAXLINE) svf.VrEndPage();
			svf.VrSetForm("KNJL439I.frm", 4);
			page_line[0]++;
			page_line[1] = 1;
			svf.VrsOut("PAGE", String.valueOf(page_line[0]) + "頁"); // ページ

			final String date = h_format_Seireki_MD(_param._date);
			svf.VrsOut("DATE", date + " " + _param._time); // 作成日付

			final String div = "1".equals(_param._outputDiv) ? "男子のみ" : "2".equals(_param._outputDiv) ? "女子のみ" : "男女共";
			svf.VrsOut("TITLE", _param._year + "年度 入学試験  " + _param._testAbbv + " 地区別・中学校別入試状況" + " (" + div + ")"); // タイトル
			svf.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名
		}

		return page_line;
	}

	private void printRecord(final Vrw32alp svf, final School obj, final String sex, final Map hyouteiMap, final int grpcd) {
		svf.VrsOut("GRPCD1", String.valueOf(grpcd));
		final String subName = "1".equals(sex) ? "男子計" : "2".equals(sex) ? "女子計" : "合　計";
		svf.VrsOut("SUB_TOTAL_NAME", subName);
		if(obj != null) {
			if(!"3".equals(sex)) {
				if("1".equals(sex)) {
					svf.VrsOut("APPLICANT", printScore(obj._shutsugan_Sex1)); // 出願者数
					svf.VrsOut("HOPE", "2".equals(_param._testDiv) ? printScore(obj._shigan_Sex1) : printScore(obj._shutsugan_Sex1)); // 志願者数
					svf.VrsOut("EXAM", printScore(obj._juken_Sex1)); // 受験者数
					svf.VrsOut("PASS", printScore(obj._goukaku_Sex1)); // 合格者数
				} else {
					svf.VrsOut("APPLICANT", printScore(obj._shutsugan_Sex2)); // 出願者数
					svf.VrsOut("HOPE", "2".equals(_param._testDiv) ? printScore(obj._shigan_Sex2) : printScore(obj._shutsugan_Sex2)); // 志願者数
					svf.VrsOut("EXAM", printScore(obj._juken_Sex2)); // 受験者数
					svf.VrsOut("PASS", printScore(obj._goukaku_Sex2)); // 合格者数
				}

			} else {
				svf.VrsOut("APPLICANT", printScore(obj._shutsugan)); // 出願者数
				svf.VrsOut("HOPE", "2".equals(_param._testDiv) ? printScore(obj._shigan) : printScore(obj._shutsugan)); // 志願者数
				svf.VrsOut("EXAM", printScore(obj._juken)); // 受験者数
				svf.VrsOut("PASS", printScore(obj._goukaku)); // 合格者数
			}
			// 評定全て
			final String allKey = "ALL" + obj._fs_Cd + sex;
			if (hyouteiMap.containsKey(allKey)) {
				final String hyouteiAll = (String) hyouteiMap.get(allKey);
				svf.VrsOut("DIV_AVE", hyouteiAll);
			}
			// 評定合格
			final String okKey = "OK" + obj._fs_Cd + sex;
			if (hyouteiMap.containsKey(okKey)) {
				final String hyouteiOk = (String) hyouteiMap.get(okKey);
				svf.VrsOut("DIV_PASS", hyouteiOk);
			}
			// 評定不合格
			final String ngKey = "NG" + obj._fs_Cd + sex;
			if (hyouteiMap.containsKey(ngKey)) {
				final String hyouteiNg = (String) hyouteiMap.get(ngKey);
				svf.VrsOut("DIV_FAIL", hyouteiNg);
			}

		}
		svf.VrEndRecord();
	}

	private void printTotalHyoutei(final Vrw32alp svf, final String cd, final Map hyouteiMap, final String sex) {
			// 評定全て
			final String allKey = "ALL" + cd + sex;
			if (hyouteiMap.containsKey(allKey)) {
				final String hyouteiAll = (String) hyouteiMap.get(allKey);
				svf.VrsOut("TOTAL_DIV_AVE", hyouteiAll);
			}

			// 評定合格
			final String okKey = "OK" + cd + sex;
			if (hyouteiMap.containsKey(okKey)) {
				final String hyouteiOk = (String) hyouteiMap.get(okKey);
				svf.VrsOut("TOTAL_DIV_PASS", hyouteiOk);
			}

			// 評定不合格
			final String ngKey = "NG" + cd + sex;
			if (hyouteiMap.containsKey(ngKey)) {
				final String hyouteiNg = (String) hyouteiMap.get(ngKey);
				svf.VrsOut("TOTAL_DIV_FAIL", hyouteiNg);
			}
		svf.VrEndRecord();
	}

	private String printScore(final String str) {
		if("0".equals(str) || str == null) {
			return "";
		}
		return str;
	}

    private String getFieldName(final String str) {
    	final int keta = KNJ_EditEdit.getMS932ByteLength(str);
    	return keta <= 30 ? "1" : "2";
    }

    // 志願者取得
    private Map getDistrictMap(final DB2UDB db2) throws SQLException {
    	Map retMap = new LinkedMap();
    	PreparedStatement ps = null;
        ResultSet rs = null;

		try {
			final StringBuffer stb = new StringBuffer();
			stb.append(" WITH BASEALL AS (SELECT ");
			stb.append("     BASE.ENTEXAMYEAR, ");
			stb.append("     BASE.APPLICANTDIV, ");
			stb.append("     BASE.TESTDIV, ");
			stb.append("     BASE.EXAMNO, ");
			stb.append("     BASE.NAME, ");
			stb.append("     BASE.SEX, ");
			stb.append("     BASE.FS_CD, ");
			stb.append("     SCHOOL.FINSCHOOL_NAME, ");
			stb.append("     SCHOOL.DISTRICTCD, ");
			stb.append("     NMST2.NAME1 AS DISTRICTNAME, ");
			stb.append("     BASE.JUDGEMENT, ");
			stb.append("     BASE.RECOM_EXAMNO, ");
			stb.append("     (SELECT ");
			stb.append("         T1.JUDGEMENT ");
			stb.append("     FROM ");
			stb.append("         ENTEXAM_APPLICANTBASE_DAT T1 ");
			stb.append("     WHERE ");
			stb.append("         T1.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND ");
			stb.append("         T1.APPLICANTDIV = BASE.APPLICANTDIV AND ");
			stb.append("         T1.TESTDIV = '1' AND ");
			stb.append("         T1.EXAMNO = BASE.RECOM_EXAMNO ");
			stb.append("     ) AS JUDGEMENT2, ");
			stb.append("     CONF.TOTAL_ALL, ");
			stb.append("     DECIMAL(ROUND(CAST(CONF.TOTAL_ALL AS double) / 9,2),5,2) AS AVG ");
			stb.append(" FROM ");
			stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE     ");
			stb.append(" INNER JOIN FINSCHOOL_MST SCHOOL ON SCHOOL.FINSCHOOLCD = BASE.FS_CD     ");
			stb.append(" INNER JOIN V_NAME_MST NMST2 ON NMST2.YEAR = '" + _param._year + "' AND NMST2.NAMECD1 = 'Z003' AND NMST2.NAMECD2 = SCHOOL.DISTRICTCD     ");
			stb.append(" INNER JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF ON CONF.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND CONF.APPLICANTDIV = BASE.APPLICANTDIV AND CONF.EXAMNO = BASE.EXAMNO AND CONF.TOTAL_ALL IS NOT NULL ");
			stb.append(" WHERE ");
			stb.append("     BASE.ENTEXAMYEAR = '" + _param._year + "' AND ");
			stb.append("     BASE.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
			stb.append("     BASE.TESTDIV = '" + _param._testDiv + "' ");
			if(!"3".equals(_param._outputDiv)) {
				stb.append("     AND BASE.SEX = '" + _param._outputDiv + "' ");
			}
			stb.append(" ORDER BY ");
			stb.append("     BASE.EXAMNO ");
			stb.append(" ), CHIKU_B AS (SELECT ");
			stb.append("     FS_CD, ");
			stb.append("     DISTRICTCD, ");
			stb.append("     COUNT(EXAMNO) AS COUNT, ");
			stb.append("     COUNT(SEX = '1' OR NULL) AS SEX1, ");
			stb.append("     COUNT(SEX = '2' OR NULL) AS SEX2 ");
			stb.append(" FROM ");
			stb.append("     BASEALL ");
			stb.append(" GROUP BY ");
			stb.append("     FS_CD, ");
			stb.append("     DISTRICTCD ");
			stb.append(" ), CHIKU_C AS (SELECT ");
			stb.append("     FS_CD, ");
			stb.append("     DISTRICTCD, ");
			stb.append("     COUNT(EXAMNO) AS COUNT, ");
			stb.append("     COUNT(SEX = '1' OR NULL) AS SEX1, ");
			stb.append("     COUNT(SEX = '2' OR NULL) AS SEX2 ");
			stb.append(" FROM ");
			stb.append("     BASEALL ");
			stb.append(" WHERE ");
			stb.append("     JUDGEMENT2 <> '1' OR JUDGEMENT2 IS NULL ");
			stb.append(" GROUP BY ");
			stb.append("     FS_CD, ");
			stb.append("     DISTRICTCD ");
			stb.append(" ), CHIKU_D AS (SELECT ");
			stb.append("     FS_CD, ");
			stb.append("     DISTRICTCD, ");
			stb.append("     COUNT(EXAMNO) AS COUNT, ");
			stb.append("     COUNT(SEX = '1' OR NULL) AS SEX1, ");
			stb.append("     COUNT(SEX = '2' OR NULL) AS SEX2 ");
			stb.append(" FROM ");
			stb.append("     BASEALL ");
			stb.append(" WHERE ");
			stb.append("     JUDGEMENT <> '4' ");
			stb.append(" GROUP BY ");
			stb.append("     FS_CD, ");
			stb.append("     DISTRICTCD ");
			stb.append(" ), CHIKU_E AS (SELECT ");
			stb.append("     FS_CD, ");
			stb.append("     DISTRICTCD, ");
			stb.append("     COUNT(EXAMNO) AS COUNT, ");
			stb.append("     COUNT(SEX = '1' OR NULL) AS SEX1, ");
			stb.append("     COUNT(SEX = '2' OR NULL) AS SEX2 ");
			stb.append(" FROM ");
			stb.append("     BASEALL ");
			stb.append(" WHERE ");
			stb.append("     JUDGEMENT = '1' ");
			stb.append(" GROUP BY ");
			stb.append("     FS_CD, ");
			stb.append("     DISTRICTCD ");
			stb.append(" ), CHIKU_F AS (SELECT ");
			stb.append("     FS_CD, ");
			stb.append("     DISTRICTCD, ");
			stb.append("     DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG, ");
			stb.append("     COUNT(SEX = '1' OR NULL) AS SEX1, ");
			stb.append("     COUNT(SEX = '2' OR NULL) AS SEX2 ");
			stb.append(" FROM ");
			stb.append("     BASEALL ");
			stb.append(" WHERE ");
			stb.append("     JUDGEMENT <> '4' ");
			stb.append(" GROUP BY ");
			stb.append("     FS_CD, ");
			stb.append("     DISTRICTCD ");
			stb.append(" ), CHIKU_G AS (SELECT ");
			stb.append("     FS_CD, ");
			stb.append("     DISTRICTCD, ");
			stb.append("     DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG, ");
			stb.append("     COUNT(SEX = '1' OR NULL) AS SEX1, ");
			stb.append("     COUNT(SEX = '2' OR NULL) AS SEX2 ");
			stb.append(" FROM ");
			stb.append("     BASEALL ");
			stb.append(" WHERE ");
			stb.append("     JUDGEMENT = '1' ");
			stb.append(" GROUP BY ");
			stb.append("     FS_CD, ");
			stb.append("     DISTRICTCD ");
			stb.append(" ), CHIKU_H AS (SELECT ");
			stb.append("     FS_CD, ");
			stb.append("     DISTRICTCD, ");
			stb.append("     DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG, ");
			stb.append("     COUNT(SEX = '1' OR NULL) AS SEX1, ");
			stb.append("     COUNT(SEX = '2' OR NULL) AS SEX2 ");
			stb.append(" FROM ");
			stb.append("     BASEALL ");
			stb.append(" WHERE ");
			stb.append("     JUDGEMENT = '2' ");
			stb.append(" GROUP BY ");
			stb.append("     FS_CD, ");
			stb.append("     DISTRICTCD ");
			stb.append(" ), CHIKU_BASE AS (SELECT ");
			stb.append("     FS_CD, ");
			stb.append("     FINSCHOOL_NAME, ");
			stb.append("     DISTRICTCD, ");
			stb.append("     DISTRICTNAME ");
			stb.append(" FROM ");
			stb.append("     BASEALL ");
			stb.append(" GROUP BY ");
			stb.append("     FS_CD, ");
			stb.append("     FINSCHOOL_NAME, ");
			stb.append("     DISTRICTCD, ");
			stb.append("     DISTRICTNAME ");
			stb.append(" ), DIV_KEKKA AS (SELECT ");
			stb.append("     'KEY' AS KEY, ");
			stb.append("     BASE.FS_CD, ");
			stb.append("     BASE.FINSCHOOL_NAME, ");
			stb.append("     BASE.DISTRICTCD, ");
			stb.append("     BASE.DISTRICTNAME, ");
			stb.append("     B.COUNT AS SHUTSUGAN, ");
			stb.append("     B.SEX1 AS SHUTSUGAN_SEX1, ");
			stb.append("     B.SEX2 AS SHUTSUGAN_SEX2, ");
			stb.append("     C.COUNT AS SHIGAN, ");
			stb.append("     C.SEX1 AS SHIGAN_SEX1, ");
			stb.append("     C.SEX2 AS SHIGAN_SEX2, ");
			stb.append("     D.COUNT AS JUKEN, ");
			stb.append("     D.SEX1 AS JUKEN_SEX1, ");
			stb.append("     D.SEX2 AS JUKEN_SEX2, ");
			stb.append("     E.COUNT AS GOUKAKU, ");
			stb.append("     E.SEX1 AS GOUKAKU_SEX1, ");
			stb.append("     E.SEX2 AS GOUKAKU_SEX2, ");
			stb.append("     F.AVG AS HYOUTEIALL, ");
			stb.append("     F.SEX1 AS HYOUTEIALL_SEX1, ");
			stb.append("     F.SEX2 AS HYOUTEIALL_SEX2, ");
			stb.append("     G.AVG AS HYOUTEIOK, ");
			stb.append("     G.SEX1 AS HYOUTEIOK_SEX1, ");
			stb.append("     G.SEX2 AS HYOUTEIOK_SEX2, ");
			stb.append("     H.AVG AS HYOUTEING, ");
			stb.append("     H.SEX1 AS HYOUTEING_SEX1, ");
			stb.append("     H.SEX2 AS HYOUTEING_SEX2 ");
			stb.append(" FROM ");
			stb.append("     CHIKU_BASE BASE   ");
			stb.append(" LEFT JOIN CHIKU_B B ON B.FS_CD = BASE.FS_CD AND B.DISTRICTCD = BASE.DISTRICTCD  ");
			stb.append(" LEFT JOIN CHIKU_C C ON C.FS_CD = BASE.FS_CD AND C.DISTRICTCD = BASE.DISTRICTCD  ");
			stb.append(" LEFT JOIN CHIKU_D D ON D.FS_CD = BASE.FS_CD AND D.DISTRICTCD = BASE.DISTRICTCD  ");
			stb.append(" LEFT JOIN CHIKU_E E ON E.FS_CD = BASE.FS_CD AND E.DISTRICTCD = BASE.DISTRICTCD  ");
			stb.append(" LEFT JOIN CHIKU_F F ON F.FS_CD = BASE.FS_CD AND F.DISTRICTCD = BASE.DISTRICTCD  ");
			stb.append(" LEFT JOIN CHIKU_G G ON G.FS_CD = BASE.FS_CD AND G.DISTRICTCD = BASE.DISTRICTCD  ");
			stb.append(" LEFT JOIN CHIKU_H H ON H.FS_CD = BASE.FS_CD AND H.DISTRICTCD = BASE.DISTRICTCD ");
			stb.append(" ) ");
			stb.append(" SELECT ");
			stb.append("     KEKKA.*  FROM      DIV_KEKKA KEKKA   ");
			stb.append(" UNION  ");
			stb.append(" SELECT ");
			stb.append("     'KEY' AS KEY, ");
			stb.append("     'ZZZ' AS FS_CD, ");
			stb.append("     'ZZZ' AS FINSCHOOL_NAME, ");
			stb.append("     DISTRICTCD, ");
			stb.append("     DISTRICTNAME, ");
			stb.append("     SUM(SHUTSUGAN) AS SHUTSUGAN, ");
			stb.append("     SUM(SHUTSUGAN_SEX1) AS SHUTSUGAN_SEX1, ");
			stb.append("     SUM(SHUTSUGAN_SEX2) AS SHUTSUGAN_SEX2, ");
			stb.append("     SUM(SHIGAN) AS SHIGAN, ");
			stb.append("     SUM(SHIGAN_SEX1) AS SHIGAN_SEX1, ");
			stb.append("     SUM(SHIGAN_SEX2) AS SHIGAN_SEX2, ");
			stb.append("     SUM(JUKEN) AS JUKEN, ");
			stb.append("     SUM(JUKEN_SEX1) AS JUKEN_SEX1, ");
			stb.append("     SUM(JUKEN_SEX2) AS JUKEN_SEX2, ");
			stb.append("     SUM(GOUKAKU) AS GOUKAKU, ");
			stb.append("     SUM(GOUKAKU_SEX1) AS GOUKAKU_SEX1, ");
			stb.append("     SUM(GOUKAKU_SEX2) AS GOUKAKU_SEX2, ");
			stb.append("     AVG(HYOUTEIALL) AS HYOUTEIALL, ");
			stb.append("     AVG(HYOUTEIALL_SEX1) AS HYOUTEIALL_SEX1, ");
			stb.append("     AVG(HYOUTEIALL_SEX2) AS HYOUTEIALL_SEX2, ");
			stb.append("     AVG(HYOUTEIOK) AS HYOUTEIOK, ");
			stb.append("     AVG(HYOUTEIOK_SEX1) AS HYOUTEIOK_SEX1, ");
			stb.append("     AVG(HYOUTEIOK_SEX2) AS HYOUTEIOK_SEX2, ");
			stb.append("     AVG(HYOUTEING) AS HYOUTEING, ");
			stb.append("     AVG(HYOUTEING_SEX1) AS HYOUTEING_SEX1, ");
			stb.append("     AVG(HYOUTEING_SEX2) AS HYOUTEING_SEX2 ");
			stb.append(" FROM ");
			stb.append("     DIV_KEKKA KEKKA ");
			stb.append(" GROUP BY ");
			stb.append("     DISTRICTCD,DISTRICTNAME ");
			stb.append(" UNION  ");
			stb.append(" SELECT ");
			stb.append("     KEY, ");
			stb.append("     'ZZZ' AS FS_CD, ");
			stb.append("     'ZZZ' AS FINSCHOOL_NAME, ");
			stb.append("     'ZZZ' AS DISTRICTCD, ");
			stb.append("     'ZZZ' AS DISTRICTNAME, ");
			stb.append("     SUM(SHUTSUGAN) AS SHUTSUGAN, ");
			stb.append("     SUM(SHUTSUGAN_SEX1) AS SHUTSUGAN_SEX1, ");
			stb.append("     SUM(SHUTSUGAN_SEX2) AS SHUTSUGAN_SEX2, ");
			stb.append("     SUM(SHIGAN) AS SHIGAN, ");
			stb.append("     SUM(SHIGAN_SEX1) AS SHIGAN_SEX1, ");
			stb.append("     SUM(SHIGAN_SEX2) AS SHIGAN_SEX2, ");
			stb.append("     SUM(JUKEN) AS JUKEN, ");
			stb.append("     SUM(JUKEN_SEX1) AS JUKEN_SEX1, ");
			stb.append("     SUM(JUKEN_SEX2) AS JUKEN_SEX2, ");
			stb.append("     SUM(GOUKAKU) AS GOUKAKU, ");
			stb.append("     SUM(GOUKAKU_SEX1) AS GOUKAKU_SEX1, ");
			stb.append("     SUM(GOUKAKU_SEX2) AS GOUKAKU_SEX2, ");
			stb.append("     AVG(HYOUTEIALL) AS HYOUTEIALL, ");
			stb.append("     AVG(HYOUTEIALL_SEX1) AS HYOUTEIALL_SEX1, ");
			stb.append("     AVG(HYOUTEIALL_SEX2) AS HYOUTEIALL_SEX2, ");
			stb.append("     AVG(HYOUTEIOK) AS HYOUTEIOK, ");
			stb.append("     AVG(HYOUTEIOK_SEX1) AS HYOUTEIOK_SEX1, ");
			stb.append("     AVG(HYOUTEIOK_SEX2) AS HYOUTEIOK_SEX2, ");
			stb.append("     AVG(HYOUTEING) AS HYOUTEING, ");
			stb.append("     AVG(HYOUTEING_SEX1) AS HYOUTEING_SEX1, ");
			stb.append("     AVG(HYOUTEING_SEX2) AS HYOUTEING_SEX2 ");
			stb.append(" FROM ");
			stb.append("     DIV_KEKKA KEKKA ");
			stb.append(" GROUP BY ");
			stb.append("     KEY ");
			stb.append(" ORDER BY ");
			stb.append("     DISTRICTCD,FS_CD ");

			log.debug(" applicant sql =" + stb.toString());

			ps = db2.prepareStatement(stb.toString());
			rs = ps.executeQuery();

			District district;
			while (rs.next()) {
				final String fs_Cd = rs.getString("FS_CD");
				final String finschool_Name = rs.getString("FINSCHOOL_NAME");
				final String districtCd = rs.getString("DISTRICTCD");
				final String districtName = rs.getString("DISTRICTNAME");
				final String shutsugan = rs.getString("SHUTSUGAN");
				final String shutsugan_Sex1 = rs.getString("SHUTSUGAN_SEX1");
				final String shutsugan_Sex2 = rs.getString("SHUTSUGAN_SEX2");
				final String shigan = rs.getString("SHIGAN");
				final String shigan_Sex1 = rs.getString("SHIGAN_SEX1");
				final String shigan_Sex2 = rs.getString("SHIGAN_SEX2");
				final String juken = rs.getString("JUKEN");
				final String juken_Sex1 = rs.getString("JUKEN_SEX1");
				final String juken_Sex2 = rs.getString("JUKEN_SEX2");
				final String goukaku = rs.getString("GOUKAKU");
				final String goukaku_Sex1 = rs.getString("GOUKAKU_SEX1");
				final String goukaku_Sex2 = rs.getString("GOUKAKU_SEX2");
				final String hyouteiall = rs.getString("HYOUTEIALL");
				final String hyouteiall_Sex1 = rs.getString("HYOUTEIALL_SEX1");
				final String hyouteiall_Sex2 = rs.getString("HYOUTEIALL_SEX2");
				final String hyouteiok = rs.getString("HYOUTEIOK");
				final String hyouteiok_Sex1 = rs.getString("HYOUTEIOK_SEX1");
				final String hyouteiok_Sex2 = rs.getString("HYOUTEIOK_SEX2");
				final String hyouteing = rs.getString("HYOUTEING");
				final String hyouteing_Sex1 = rs.getString("HYOUTEING_SEX1");
				final String hyouteing_Sex2 = rs.getString("HYOUTEING_SEX2");

				//総合計
				if("ZZZ".equals(districtCd)) {
					_param._sougou = new School(fs_Cd, finschool_Name, districtCd, districtName, shutsugan, shutsugan_Sex1,
							shutsugan_Sex2, shigan, shigan_Sex1, shigan_Sex2, juken, juken_Sex1, juken_Sex2, goukaku,
							goukaku_Sex1, goukaku_Sex2, hyouteiall, hyouteiall_Sex1, hyouteiall_Sex2, hyouteiok,
							hyouteiok_Sex1, hyouteiok_Sex2, hyouteing, hyouteing_Sex1, hyouteing_Sex2);
				}else {
					if(!retMap.containsKey(districtCd)) {
				    	district = new District(districtCd, districtName);
				    	retMap.put(districtCd, district);
				    } else {
				    	district = (District)retMap.get(districtCd);
				    }

					if (!district._schoolMap.containsKey(fs_Cd)) {
						final School school = new School(fs_Cd, finschool_Name, districtCd, districtName, shutsugan, shutsugan_Sex1,
								shutsugan_Sex2, shigan, shigan_Sex1, shigan_Sex2, juken, juken_Sex1, juken_Sex2, goukaku,
								goukaku_Sex1, goukaku_Sex2, hyouteiall, hyouteiall_Sex1, hyouteiall_Sex2, hyouteiok,
								hyouteiok_Sex1, hyouteiok_Sex2, hyouteing, hyouteing_Sex1, hyouteing_Sex2);

						district._schoolMap.put(fs_Cd, school);
					}
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

    // 合計取得
    private Map getTotalHyouteiMap(final DB2UDB db2) throws SQLException {

    	Map retMap = new LinkedMap();
    	PreparedStatement ps = null;
        ResultSet rs = null;

		try {
			final StringBuffer stb = new StringBuffer();
			stb.append(" WITH BASEALL AS (SELECT ");
			stb.append("     BASE.ENTEXAMYEAR, ");
			stb.append("     BASE.APPLICANTDIV, ");
			stb.append("     BASE.TESTDIV, ");
			stb.append("     BASE.EXAMNO, ");
			stb.append("     BASE.NAME, ");
			stb.append("     BASE.SEX, ");
			stb.append("     BASE.FS_CD, ");
			stb.append("     SCHOOL.FINSCHOOL_NAME, ");
			stb.append("     SCHOOL.DISTRICTCD, ");
			stb.append("     NMST2.NAME1 AS DISTRICTNAME, ");
			stb.append("     BASE.JUDGEMENT, ");
			stb.append("     BASE.RECOM_EXAMNO, ");
			stb.append("     (SELECT ");
			stb.append("         T1.JUDGEMENT ");
			stb.append("     FROM ");
			stb.append("         ENTEXAM_APPLICANTBASE_DAT T1 ");
			stb.append("     WHERE ");
			stb.append("         T1.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND ");
			stb.append("         T1.APPLICANTDIV = BASE.APPLICANTDIV AND ");
			stb.append("         T1.TESTDIV = '1' AND ");
			stb.append("         T1.EXAMNO = BASE.RECOM_EXAMNO ");
			stb.append("     ) AS JUDGEMENT2, ");
			stb.append("     CONF.TOTAL_ALL, ");
			stb.append("     DECIMAL(ROUND(CAST(CONF.TOTAL_ALL AS double) / 9,2),5,2) AS AVG ");
			stb.append(" FROM ");
			stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE     ");
			stb.append(" INNER JOIN FINSCHOOL_MST SCHOOL ON SCHOOL.FINSCHOOLCD = BASE.FS_CD     ");
			stb.append(" INNER JOIN V_NAME_MST NMST2 ON NMST2.YEAR = '" + _param._year + "' AND NMST2.NAMECD1 = 'Z003' AND NMST2.NAMECD2 = SCHOOL.DISTRICTCD     ");
			stb.append(" INNER JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF ON CONF.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND CONF.APPLICANTDIV = BASE.APPLICANTDIV AND CONF.EXAMNO = BASE.EXAMNO AND CONF.TOTAL_ALL IS NOT NULL ");
			stb.append(" WHERE ");
			stb.append("     BASE.ENTEXAMYEAR = '" + _param._year + "' AND ");
			stb.append("     BASE.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
			stb.append("     BASE.TESTDIV = '" + _param._testDiv + "' ");
			if(!"3".equals(_param._outputDiv)) {
				stb.append("     AND BASE.SEX = '" + _param._outputDiv + "' ");
			}
			stb.append(" ORDER BY ");
			stb.append("     BASE.EXAMNO ");
			stb.append(" ) , HYOUTEI_OK AS ( ");
			stb.append("  SELECT ");
			stb.append("  'OK' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  DISTRICTCD, ");
			stb.append("  DISTRICTNAME, ");
			stb.append("  SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT = '1' ");
			stb.append(" GROUP BY  ENTEXAMYEAR,DISTRICTCD, DISTRICTNAME,SEX ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append("  'OK' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  DISTRICTCD, ");
			stb.append("  DISTRICTNAME, ");
			stb.append("  'ZZZ' AS SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT = '1' ");
			stb.append(" GROUP BY  ENTEXAMYEAR, DISTRICTCD, DISTRICTNAME ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append("  'OK' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  'ZZZ' AS DISTRICTCD, ");
			stb.append("  'ZZZ' AS DISTRICTNAME, ");
			stb.append("  SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT = '1' ");
			stb.append(" GROUP BY  ENTEXAMYEAR, SEX ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append(" 'OK' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  'ZZZ' AS DISTRICTCD, ");
			stb.append("  'ZZZ' AS DISTRICTNAME, ");
			stb.append("  'ZZZ' AS SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT = '1' ");
			stb.append(" GROUP BY  ENTEXAMYEAR ");
			stb.append(" ORDER BY DISTRICTCD,SEX ");
			stb.append(" ) , HYOUTEI_ALL AS ( ");
			stb.append("  SELECT ");
			stb.append("  'ALL' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  DISTRICTCD, ");
			stb.append("  DISTRICTNAME, ");
			stb.append("  SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT <> '4' ");
			stb.append(" GROUP BY  ENTEXAMYEAR,DISTRICTCD, DISTRICTNAME,SEX ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append("  'ALL' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  DISTRICTCD, ");
			stb.append("  DISTRICTNAME, ");
			stb.append("  'ZZZ' AS SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT <> '4' ");
			stb.append(" GROUP BY  ENTEXAMYEAR, DISTRICTCD, DISTRICTNAME ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append("  'ALL' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  'ZZZ' AS DISTRICTCD, ");
			stb.append("  'ZZZ' AS DISTRICTNAME, ");
			stb.append("  SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT <> '4' ");
			stb.append(" GROUP BY  ENTEXAMYEAR, SEX ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append(" 'ALL' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  'ZZZ' AS DISTRICTCD, ");
			stb.append("  'ZZZ' AS DISTRICTNAME, ");
			stb.append("  'ZZZ' AS SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT <> '4' ");
			stb.append(" GROUP BY  ENTEXAMYEAR ");
			stb.append(" ORDER BY DISTRICTCD,SEX ");
			stb.append(" ) , HYOUTEI_NG AS ( ");
			stb.append("  SELECT ");
			stb.append("  'NG' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  DISTRICTCD, ");
			stb.append("  DISTRICTNAME, ");
			stb.append("  SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT = '2' ");
			stb.append(" GROUP BY  ENTEXAMYEAR,DISTRICTCD, DISTRICTNAME,SEX ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append("  'NG' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  DISTRICTCD, ");
			stb.append("  DISTRICTNAME, ");
			stb.append("  'ZZZ' AS SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT = '2' ");
			stb.append(" GROUP BY  ENTEXAMYEAR, DISTRICTCD, DISTRICTNAME ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append("  'NG' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  'ZZZ' AS DISTRICTCD, ");
			stb.append("  'ZZZ' AS DISTRICTNAME, ");
			stb.append("  SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT = '2' ");
			stb.append(" GROUP BY  ENTEXAMYEAR, SEX ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append(" 'NG' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  'ZZZ' AS DISTRICTCD, ");
			stb.append("  'ZZZ' AS DISTRICTNAME, ");
			stb.append("  'ZZZ' AS SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT = '2' ");
			stb.append(" GROUP BY  ENTEXAMYEAR ");
			stb.append(" ORDER BY DISTRICTCD,SEX ");
			stb.append(" ) SELECT * FROM HYOUTEI_ALL ");
			stb.append(" UNION SELECT * FROM HYOUTEI_OK ");
			stb.append(" UNION SELECT * FROM HYOUTEI_NG ");
			stb.append(" ORDER BY KEY,DISTRICTCD,SEX ");

			log.debug("total sql = " + stb);
			ps = db2.prepareStatement(stb.toString());
			rs = ps.executeQuery();

			while (rs.next()) {
				final String key = rs.getString("KEY");
				final String prefCd = rs.getString("DISTRICTCD");
				final String sex = rs.getString("SEX");
				final String avg = rs.getString("AVG");

				final String keyCd = key + prefCd + sex;

				if(!retMap.containsKey(keyCd)) {
			    	retMap.put(keyCd, avg);
			    }
			}

		} catch (final SQLException e) {
			log.error("合計の基本情報取得でエラー", e);
			throw e;
		} finally {
			db2.commit();
			DbUtils.closeQuietly(null, ps, rs);
		}

    	return retMap;
    }

 // 学校評定取得
    private Map getSchoolHyouteiMap(final DB2UDB db2) throws SQLException {

    	Map retMap = new LinkedMap();
    	PreparedStatement ps = null;
        ResultSet rs = null;

		try {
			final StringBuffer stb = new StringBuffer();
			stb.append(" WITH BASEALL AS (SELECT ");
			stb.append("     BASE.ENTEXAMYEAR, ");
			stb.append("     BASE.APPLICANTDIV, ");
			stb.append("     BASE.TESTDIV, ");
			stb.append("     BASE.EXAMNO, ");
			stb.append("     BASE.NAME, ");
			stb.append("     BASE.SEX, ");
			stb.append("     BASE.FS_CD, ");
			stb.append("     SCHOOL.FINSCHOOL_NAME, ");
			stb.append("     SCHOOL.DISTRICTCD, ");
			stb.append("     NMST2.NAME1 AS DISTRICTNAME, ");
			stb.append("     BASE.JUDGEMENT, ");
			stb.append("     BASE.RECOM_EXAMNO, ");
			stb.append("     (SELECT ");
			stb.append("         T1.JUDGEMENT ");
			stb.append("     FROM ");
			stb.append("         ENTEXAM_APPLICANTBASE_DAT T1 ");
			stb.append("     WHERE ");
			stb.append("         T1.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND ");
			stb.append("         T1.APPLICANTDIV = BASE.APPLICANTDIV AND ");
			stb.append("         T1.TESTDIV = '1' AND ");
			stb.append("         T1.EXAMNO = BASE.RECOM_EXAMNO ");
			stb.append("     ) AS JUDGEMENT2, ");
			stb.append("     CONF.TOTAL_ALL, ");
			stb.append("     DECIMAL(ROUND(CAST(CONF.TOTAL_ALL AS double) / 9,2),5,2) AS AVG ");
			stb.append(" FROM ");
			stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE     ");
			stb.append(" INNER JOIN FINSCHOOL_MST SCHOOL ON SCHOOL.FINSCHOOLCD = BASE.FS_CD     ");
			stb.append(" INNER JOIN V_NAME_MST NMST2 ON NMST2.YEAR = '" + _param._year + "' AND NMST2.NAMECD1 = 'Z003' AND NMST2.NAMECD2 = SCHOOL.DISTRICTCD     ");
			stb.append(" INNER JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF ON CONF.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND CONF.APPLICANTDIV = BASE.APPLICANTDIV AND CONF.EXAMNO = BASE.EXAMNO AND CONF.TOTAL_ALL IS NOT NULL ");
			stb.append(" WHERE ");
			stb.append("     BASE.ENTEXAMYEAR = '" + _param._year + "' AND ");
			stb.append("     BASE.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
			stb.append("     BASE.TESTDIV = '" + _param._testDiv + "' ");
			if(!"3".equals(_param._outputDiv)) {
				stb.append("     AND BASE.SEX = '" + _param._outputDiv + "' ");
			}
			stb.append(" ORDER BY ");
			stb.append("     BASE.EXAMNO ");
			stb.append(" ) , HYOUTEI_OK AS ( ");
			stb.append("  SELECT ");
			stb.append("  'OK' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  FS_CD, ");
			stb.append("  FINSCHOOL_NAME, ");
			stb.append("  SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT = '1' ");
			stb.append(" GROUP BY  ENTEXAMYEAR,FS_CD, FINSCHOOL_NAME,SEX ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append("  'OK' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  FS_CD, ");
			stb.append("  FINSCHOOL_NAME, ");
			stb.append("  'ZZZ' AS SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT = '1' ");
			stb.append(" GROUP BY  ENTEXAMYEAR, FS_CD, FINSCHOOL_NAME ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append("  'OK' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  'ZZZ' AS FS_CD, ");
			stb.append("  'ZZZ' AS FINSCHOOL_NAME, ");
			stb.append("  SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT = '1' ");
			stb.append(" GROUP BY  ENTEXAMYEAR, SEX ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append(" 'OK' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  'ZZZ' AS FS_CD, ");
			stb.append("  'ZZZ' AS FINSCHOOL_NAME, ");
			stb.append("  'ZZZ' AS SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT = '1' ");
			stb.append(" GROUP BY  ENTEXAMYEAR ");
			stb.append(" ORDER BY FS_CD,SEX ");
			stb.append(" ) , HYOUTEI_ALL AS ( ");
			stb.append("  SELECT ");
			stb.append("  'ALL' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  FS_CD, ");
			stb.append("  FINSCHOOL_NAME, ");
			stb.append("  SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT <> '4' ");
			stb.append(" GROUP BY  ENTEXAMYEAR,FS_CD, FINSCHOOL_NAME,SEX ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append("  'ALL' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  FS_CD, ");
			stb.append("  FINSCHOOL_NAME, ");
			stb.append("  'ZZZ' AS SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT <> '4' ");
			stb.append(" GROUP BY  ENTEXAMYEAR, FS_CD, FINSCHOOL_NAME ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append("  'ALL' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  'ZZZ' AS FS_CD, ");
			stb.append("  'ZZZ' AS FINSCHOOL_NAME, ");
			stb.append("  SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT <> '4' ");
			stb.append(" GROUP BY  ENTEXAMYEAR, SEX ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append(" 'ALL' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  'ZZZ' AS FS_CD, ");
			stb.append("  'ZZZ' AS FINSCHOOL_NAME, ");
			stb.append("  'ZZZ' AS SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT <> '4' ");
			stb.append(" GROUP BY  ENTEXAMYEAR ");
			stb.append(" ORDER BY FS_CD,SEX ");
			stb.append(" ) , HYOUTEI_NG AS ( ");
			stb.append("  SELECT ");
			stb.append("  'NG' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  FS_CD, ");
			stb.append("  FINSCHOOL_NAME, ");
			stb.append("  SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT = '2' ");
			stb.append(" GROUP BY  ENTEXAMYEAR,FS_CD, FINSCHOOL_NAME,SEX ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append("  'NG' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  FS_CD, ");
			stb.append("  FINSCHOOL_NAME, ");
			stb.append("  'ZZZ' AS SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT = '2' ");
			stb.append(" GROUP BY  ENTEXAMYEAR, FS_CD, FINSCHOOL_NAME ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append("  'NG' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  'ZZZ' AS FS_CD, ");
			stb.append("  'ZZZ' AS FINSCHOOL_NAME, ");
			stb.append("  SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT = '2' ");
			stb.append(" GROUP BY  ENTEXAMYEAR, SEX ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append(" 'NG' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  'ZZZ' AS FS_CD, ");
			stb.append("  'ZZZ' AS FINSCHOOL_NAME, ");
			stb.append("  'ZZZ' AS SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT = '2' ");
			stb.append(" GROUP BY  ENTEXAMYEAR ");
			stb.append(" ORDER BY FS_CD,SEX ");
			stb.append(" ) SELECT * FROM HYOUTEI_ALL ");
			stb.append(" UNION SELECT * FROM HYOUTEI_OK ");
			stb.append(" UNION SELECT * FROM HYOUTEI_NG ");
			stb.append(" ORDER BY KEY,FS_CD,SEX ");

			log.debug("schoolHyoutei sql = " + stb);
			ps = db2.prepareStatement(stb.toString());
			rs = ps.executeQuery();

			while (rs.next()) {
				final String key = rs.getString("KEY");
				final String prefCd = rs.getString("FS_CD");
				final String sex = rs.getString("SEX");
				final String avg = rs.getString("AVG");

				final String keyCd = key + prefCd + sex;

				if(!retMap.containsKey(keyCd)) {
			    	retMap.put(keyCd, avg);
			    }
			}

		} catch (final SQLException e) {
			log.error("合計の基本情報取得でエラー", e);
			throw e;
		} finally {
			db2.commit();
			DbUtils.closeQuietly(null, ps, rs);
		}

    	return retMap;
    }

    // 学校の志願者集約クラス
    private class School {
        final String _fs_Cd;
        final String _finschool_Name;
        final String _districtCd;
        final String _districtName;
        final String _shutsugan;
        final String _shutsugan_Sex1;
        final String _shutsugan_Sex2;
        final String _shigan;
        final String _shigan_Sex1;
        final String _shigan_Sex2;
        final String _juken;
        final String _juken_Sex1;
        final String _juken_Sex2;
        final String _goukaku;
        final String _goukaku_Sex1;
        final String _goukaku_Sex2;
        final String _hyouteiall;
        final String _hyouteiall_Sex1;
        final String _hyouteiall_Sex2;
        final String _hyouteiok;
        final String _hyouteiok_Sex1;
        final String _hyouteiok_Sex2;
        final String _hyouteing;
        final String _hyouteing_Sex1;
        final String _hyouteing_Sex2;


		public School(final String fs_Cd, final String finschool_Name, final String districtCd,
				final String districtName, final String shutsugan, final String shutsugan_Sex1,
				final String shutsugan_Sex2, final String shigan, final String shigan_Sex1, final String shigan_Sex2,
				final String juken, final String juken_Sex1, final String juken_Sex2, final String goukaku,
				final String goukaku_Sex1, final String goukaku_Sex2, final String hyouteiall,
				final String hyouteiall_Sex1, final String hyouteiall_Sex2, final String hyouteiok,
				final String hyouteiok_Sex1, final String hyouteiok_Sex2, final String hyouteing,
				final String hyouteing_Sex1, final String hyouteing_Sex2) {
			_fs_Cd = fs_Cd;
			_finschool_Name = finschool_Name;
			_districtCd = districtCd;
			_districtName = districtName;
			_shutsugan = shutsugan;
			_shutsugan_Sex1 = shutsugan_Sex1;
			_shutsugan_Sex2 = shutsugan_Sex2;
			_shigan = shigan;
			_shigan_Sex1 = shigan_Sex1;
			_shigan_Sex2 = shigan_Sex2;
			_juken = juken;
			_juken_Sex1 = juken_Sex1;
			_juken_Sex2 = juken_Sex2;
			_goukaku = goukaku;
			_goukaku_Sex1 = goukaku_Sex1;
			_goukaku_Sex2 = goukaku_Sex2;
			_hyouteiall = hyouteiall;
			_hyouteiall_Sex1 = hyouteiall_Sex1;
			_hyouteiall_Sex2 = hyouteiall_Sex2;
			_hyouteiok = hyouteiok;
			_hyouteiok_Sex1 = hyouteiok_Sex1;
			_hyouteiok_Sex2 = hyouteiok_Sex2;
			_hyouteing = hyouteing;
			_hyouteing_Sex1 = hyouteing_Sex1;
			_hyouteing_Sex2 = hyouteing_Sex2;
		}
    }

    private class District {
    	final String _districtCd;
    	final String _districtName;
    	final Map<String,School> _schoolMap;

    	public District(final String districtCd, final String districtName) {
    		_districtCd = districtCd;
    		_districtName = districtName;
    		_schoolMap = new LinkedMap();
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
        final String _schoolName;
        School _sougou;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _outputDiv = request.getParameter("OUTPUT_DIV");
            _schoolKind = request.getParameter("SCHOOLKIND");
          	_date = request.getParameter("CTRL_DATE");
          	_time = request.getParameter("TIME");
          	_testAbbv = getTestDivAbbv(db2);
          	_schoolName = getSchoolName(db2);
        }

        private String getTestDivAbbv(final DB2UDB db2) {
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
