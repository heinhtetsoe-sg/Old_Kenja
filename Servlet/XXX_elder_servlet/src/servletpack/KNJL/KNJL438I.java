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
 * @version $Id: 4012741bde510455489824848c0ccc23d64c0c27 $
 */
public class KNJL438I {

    private static final Log log = LogFactory.getLog("KNJL438I.class");

    /** 1ページ 40明細 */
    private static final int MAXLINE = 40;
    /** 国立*/
    private static final String KOKURITSU = "1";
    /** 私立*/
    private static final String SHIRITSU = "3";

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

		final Map prefectureMap = getPrefectureMap(db2); // 志願者Map

		if (prefectureMap.isEmpty()) {
			return false;
		}

		final Map totalMap = getTotalMap(db2); // 合計Map
		int page_line[] = {0,1}; // ページ数、印字行数

		for (Iterator ite = prefectureMap.keySet().iterator(); ite.hasNext();) {
			final String Key = (String) ite.next();
			final Prefecture prefecture = (Prefecture) prefectureMap.get(Key);

			page_line = checkLine(svf,page_line);
			svf.VrsOut("PREF_NAME", prefecture._prefName); // 都道府県
			svf.VrEndRecord();
			page_line[1]++;
			page_line = checkLine(svf,page_line);

			// DIV1:国立
			final Applicant applicant_Div1= (Applicant)prefecture._divMap.get(KOKURITSU);
			svf.VrsOut("DISTRICT_NAME1", "国立");
			printRecord(svf, applicant_Div1);
			page_line[1]++;
			page_line = checkLine(svf,page_line);

			// DIV2:私立
			final Applicant applicant_Div3= (Applicant)prefecture._divMap.get(SHIRITSU);
			svf.VrsOut("DISTRICT_NAME1", "私立");
			printRecord(svf, applicant_Div3);
			page_line[1]++;
			page_line = checkLine(svf,page_line);

			// DIV3:公立 地区毎
			for (Iterator ite2 = prefecture._districtMap.keySet().iterator(); ite2.hasNext();) {
				final String Key2 = (String) ite2.next();
				final Applicant applicant = (Applicant) prefecture._districtMap.get(Key2);

				final String fieldName = getFieldName(applicant._key_Name);
				svf.VrsOut("DISTRICT_NAME" + fieldName, applicant._key_Name);
				printRecord(svf, applicant);
				page_line[1]++;
				page_line = checkLine(svf,page_line);
			}

			final Applicant totalCount= (Applicant)prefecture._divMap.get("ZZZ");

			// 合計 男子
			if(!"2".equals(_param._outputDiv)) {
				svf.VrsOut("TOTAL_NAME", "　男子計");
				svf.VrsOut("TOTAL_APPLICANT", printScore(totalCount._shutsugan_Sex1)); // 出願者数
				svf.VrsOut("TOTAL_HOPE", "2".equals(_param._testDiv) ? printScore(totalCount._shigan_Sex1) : printScore(totalCount._shutsugan_Sex1)); // 志願者数
				svf.VrsOut("TOTAL_EXAM", printScore(totalCount._juken_Sex1)); // 受験者数
				svf.VrsOut("TOTAL_PASS", printScore(totalCount._goukaku_Sex1)); // 合格者数
				printHyoutei(svf,prefecture._prefCd,totalMap,"1");
				page_line[1]++;
				page_line = checkLine(svf,page_line);
			}

			// 合計 女子
			if(!"1".equals(_param._outputDiv)) {
				svf.VrsOut("TOTAL_NAME", "　女子計");
				svf.VrsOut("TOTAL_APPLICANT", printScore(totalCount._shutsugan_Sex2)); // 出願者数
				svf.VrsOut("TOTAL_HOPE", "2".equals(_param._testDiv) ? printScore(totalCount._shigan_Sex2) : printScore(totalCount._shutsugan_Sex2)); // 志願者数
				svf.VrsOut("TOTAL_EXAM", printScore(totalCount._juken_Sex2)); // 受験者数
				svf.VrsOut("TOTAL_PASS", printScore(totalCount._goukaku_Sex2)); // 合格者数
				printHyoutei(svf,prefecture._prefCd,totalMap,"2");
				page_line[1]++;
				page_line = checkLine(svf,page_line);
			}

			// 合計 男女
			svf.VrsOut("TOTAL_NAME", "合　　計");
			svf.VrsOut("TOTAL_EXAM_SCHOOL", printScore(totalCount._school)); // 受験校数
			svf.VrsOut("TOTAL_APPLICANT", printScore(totalCount._shutsugan)); // 出願者数
			svf.VrsOut("TOTAL_HOPE", "2".equals(_param._testDiv) ? printScore(totalCount._shigan) : printScore(totalCount._shutsugan)); // 志願者数
			svf.VrsOut("TOTAL_EXAM", printScore(totalCount._juken)); // 受験者数
			svf.VrsOut("TOTAL_PASS", printScore(totalCount._goukaku)); // 合格者数
			svf.VrsOut("TOTAL_DIV_AVE", printScore(totalCount._hyouteiall)); // 評定全て
			svf.VrsOut("TOTAL_DIV_PASS", printScore(totalCount._hyouteiok)); // 評定合格
			svf.VrsOut("TOTAL_DIV_FAIL", printScore(totalCount._hyouteing)); // 評定不合格
			printHyoutei(svf,prefecture._prefCd,totalMap,"ZZZ");
			page_line[1]++;
		}

		page_line = checkLine(svf,page_line);

		if(!"2".equals(_param._outputDiv)) {
			svf.VrsOut("TOTAL_NAME", "男子総計");
			svf.VrsOut("TOTAL_APPLICANT", printScore(_param._sougou._shutsugan_Sex1)); // 出願者数
			svf.VrsOut("TOTAL_HOPE", "2".equals(_param._testDiv) ? printScore(_param._sougou._shigan_Sex1) : printScore(_param._sougou._shutsugan_Sex1)); // 志願者数
			svf.VrsOut("TOTAL_EXAM", printScore(_param._sougou._juken_Sex1)); // 受験者数
			svf.VrsOut("TOTAL_PASS", printScore(_param._sougou._goukaku_Sex1)); // 合格者数
			printHyoutei(svf,"ZZZ",totalMap,"1");
			page_line[1]++;
			page_line = checkLine(svf,page_line);
		}

		if(!"1".equals(_param._outputDiv)) {
			svf.VrsOut("TOTAL_NAME", "女子総計");
			svf.VrsOut("TOTAL_APPLICANT", printScore(_param._sougou._shutsugan_Sex2)); // 出願者数
			svf.VrsOut("TOTAL_HOPE", "2".equals(_param._testDiv) ? printScore(_param._sougou._shigan_Sex2) : printScore(_param._sougou._shutsugan_Sex2)); // 志願者数
			svf.VrsOut("TOTAL_EXAM", printScore(_param._sougou._juken_Sex2)); // 受験者数
			svf.VrsOut("TOTAL_PASS", printScore(_param._sougou._goukaku_Sex2)); // 合格者数
			printHyoutei(svf,"ZZZ",totalMap,"2");
			page_line[1]++;
			page_line = checkLine(svf,page_line);
		}

		svf.VrsOut("TOTAL_NAME", "総　　計");
		svf.VrsOut("TOTAL_EXAM_SCHOOL", printScore(_param._sougou._school)); // 受験校数
		svf.VrsOut("TOTAL_APPLICANT", printScore(_param._sougou._shutsugan)); // 出願者数
		svf.VrsOut("TOTAL_HOPE", "2".equals(_param._testDiv) ? printScore(_param._sougou._shigan) : printScore(_param._sougou._shutsugan)); // 志願者数
		svf.VrsOut("TOTAL_EXAM", printScore(_param._sougou._juken)); // 受験者数
		svf.VrsOut("TOTAL_PASS", printScore(_param._sougou._goukaku)); // 合格者数
		printHyoutei(svf,"ZZZ",totalMap,"ZZZ");
		page_line[1]++;

		svf.VrEndPage();

		return true;
	}

	private int[] checkLine(final Vrw32alp svf, final int[] page_line) {
		if (page_line[1] > MAXLINE || page_line[0] == 0) {
			if (page_line[1] > MAXLINE) svf.VrEndPage();
			svf.VrSetForm("KNJL438I.frm", 4);
			page_line[0]++;
			page_line[1] = 1;
			svf.VrsOut("PAGE", String.valueOf(page_line[0]) + "頁"); // ページ

			final String date = h_format_Seireki_MD(_param._date);
			svf.VrsOut("DATE", date + " " + _param._time); // 作成日付

			final String div = "1".equals(_param._outputDiv) ? "男子のみ" : "2".equals(_param._outputDiv) ? "女子のみ" : "男女共";
			svf.VrsOut("TITLE", _param._year + "年度 入学試験　" + _param._testAbbv + "　地区別入試状況" + " (" + div + ")"); // タイトル
			svf.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名
		}

		return page_line;
	}

	private void printRecord(final Vrw32alp svf, final Applicant obj) {
		if(obj != null) {
			svf.VrsOut("EXAM_SCHOOL", printScore(obj._school)); // 受験校数
			svf.VrsOut("APPLICANT", printScore(obj._shutsugan)); // 出願者数
			svf.VrsOut("HOPE", "2".equals(_param._testDiv) ? printScore(obj._shigan) : printScore(obj._shutsugan)); // 志願者数
			svf.VrsOut("EXAM", printScore(obj._juken)); // 受験者数
			svf.VrsOut("PASS", printScore(obj._goukaku)); // 合格者数
			svf.VrsOut("DIV_AVE", printScore(obj._hyouteiall)); // 評定全て
			svf.VrsOut("DIV_PASS", printScore(obj._hyouteiok)); // 評定合格
			svf.VrsOut("DIV_FAIL", printScore(obj._hyouteing)); // 評定不合格
		}
		svf.VrEndRecord();
	}

	private void printHyoutei(final Vrw32alp svf, final String cd, final Map hyouteiMap, final String sex) {
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
    	return keta <= 14 ? "1" : "2";
    }

    // 志願者取得
    private Map getPrefectureMap(final DB2UDB db2) throws SQLException {
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
			stb.append("     SCHOOL.FINSCHOOL_DIV, ");
			stb.append("     STG.NAME1 AS DIVNAME, ");
			stb.append("     SCHOOL.FINSCHOOL_NAME, ");
			stb.append("     SCHOOL.FINSCHOOL_PREF_CD, ");
			stb.append("     PREF.PREF_NAME, ");
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
			stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE   ");
			stb.append(" INNER JOIN FINSCHOOL_MST SCHOOL ON SCHOOL.FINSCHOOLCD = BASE.FS_CD   ");
			stb.append(" INNER JOIN PREF_MST PREF ON PREF.PREF_CD = SCHOOL.FINSCHOOL_PREF_CD   ");
			stb.append(" INNER JOIN V_NAME_MST NMST2 ON NMST2.YEAR = '" + _param._year + "' AND NMST2.NAMECD1 = 'Z003' AND NMST2.NAMECD2 = SCHOOL.DISTRICTCD   ");
			stb.append(" INNER JOIN ENTEXAM_SETTING_MST STG ON STG.ENTEXAMYEAR = '" + _param._year + "' AND STG.APPLICANTDIV = '" + _param._applicantDiv + "' AND STG.SETTING_CD = 'L015' AND STG.SEQ = SCHOOL.FINSCHOOL_DIV   ");
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
			stb.append(" ), DIV_A AS (SELECT "); // 国立、私立　受験校数
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     FINSCHOOL_DIV, ");
			stb.append("     COUNT(DISTINCT FS_CD) AS COUNT ");
			stb.append(" FROM ");
			stb.append("     BASEALL ");
			stb.append(" WHERE ");
			stb.append("     FINSCHOOL_DIV IN ('1', '3') ");
			stb.append(" GROUP BY ");
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     FINSCHOOL_DIV ");
			stb.append(" ), DIV_B AS (SELECT "); // 国立、私立　出願者数
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     FINSCHOOL_DIV, ");
			stb.append("     COUNT(EXAMNO) AS COUNT, ");
			stb.append("     COUNT(SEX = '1' OR NULL) AS SEX1, ");
			stb.append("     COUNT(SEX = '2' OR NULL) AS SEX2 ");
			stb.append(" FROM ");
			stb.append("     BASEALL ");
			stb.append(" WHERE ");
			stb.append("     FINSCHOOL_DIV IN ('1', '3') ");
			stb.append(" GROUP BY ");
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     FINSCHOOL_DIV ");
			stb.append(" ), DIV_C AS (SELECT "); // 国立、私立　志願者数
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     FINSCHOOL_DIV, ");
			stb.append("     COUNT(EXAMNO) AS COUNT, ");
			stb.append("     COUNT(SEX = '1' OR NULL) AS SEX1, ");
			stb.append("     COUNT(SEX = '2' OR NULL) AS SEX2 ");
			stb.append(" FROM ");
			stb.append("     BASEALL ");
			stb.append(" WHERE ");
			stb.append("     FINSCHOOL_DIV IN ('1', '3') ");
			stb.append("     AND JUDGEMENT2 <> '1' OR JUDGEMENT2 IS NULL ");
			stb.append(" GROUP BY ");
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     FINSCHOOL_DIV ");
			stb.append(" ), DIV_D AS (SELECT "); // 国立、私立　受験者数
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     FINSCHOOL_DIV, ");
			stb.append("     COUNT(EXAMNO) AS COUNT, ");
			stb.append("     COUNT(SEX = '1' OR NULL) AS SEX1, ");
			stb.append("     COUNT(SEX = '2' OR NULL) AS SEX2 ");
			stb.append(" FROM ");
			stb.append("     BASEALL ");
			stb.append(" WHERE ");
			stb.append("     FINSCHOOL_DIV IN ('1', '3') AND ");
			stb.append("     JUDGEMENT <> '4' ");
			stb.append(" GROUP BY ");
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     FINSCHOOL_DIV ");
			stb.append(" ), DIV_E AS (SELECT "); // 国立、私立　合格者数
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     FINSCHOOL_DIV, ");
			stb.append("     COUNT(EXAMNO) AS COUNT, ");
			stb.append("     COUNT(SEX = '1' OR NULL) AS SEX1, ");
			stb.append("     COUNT(SEX = '2' OR NULL) AS SEX2 ");
			stb.append(" FROM ");
			stb.append("     BASEALL ");
			stb.append(" WHERE ");
			stb.append("     FINSCHOOL_DIV IN ('1', '3') AND ");
			stb.append("     JUDGEMENT = '1' ");
			stb.append(" GROUP BY ");
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     FINSCHOOL_DIV ");
			stb.append(" ), DIV_F AS (SELECT "); // 国立、私立　評定平均全体
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     FINSCHOOL_DIV, ");
			stb.append("     DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG, ");
			stb.append("     COUNT(SEX = '1' OR NULL) AS SEX1, ");
			stb.append("     COUNT(SEX = '2' OR NULL) AS SEX2 ");
			stb.append(" FROM ");
			stb.append("     BASEALL ");
			stb.append(" WHERE ");
			stb.append("     FINSCHOOL_DIV IN ('1', '3') AND ");
			stb.append("     JUDGEMENT <> '4' ");
			stb.append(" GROUP BY ");
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     FINSCHOOL_DIV ");
			stb.append(" ), DIV_G AS (SELECT "); // 国立、私立　評定平均合格
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     FINSCHOOL_DIV, ");
			stb.append("     DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG, ");
			stb.append("     COUNT(SEX = '1' OR NULL) AS SEX1, ");
			stb.append("     COUNT(SEX = '2' OR NULL) AS SEX2 ");
			stb.append(" FROM ");
			stb.append("     BASEALL ");
			stb.append(" WHERE ");
			stb.append("     FINSCHOOL_DIV IN ('1', '3') AND ");
			stb.append("     JUDGEMENT = '1' ");
			stb.append(" GROUP BY ");
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     FINSCHOOL_DIV ");
			stb.append(" ), DIV_H AS (SELECT "); // 国立、私立　評定平均不合格
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     FINSCHOOL_DIV, ");
			stb.append("     DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG, ");
			stb.append("     COUNT(SEX = '1' OR NULL) AS SEX1, ");
			stb.append("     COUNT(SEX = '2' OR NULL) AS SEX2 ");
			stb.append(" FROM ");
			stb.append("     BASEALL ");
			stb.append(" WHERE ");
			stb.append("     FINSCHOOL_DIV IN ('1', '3') AND ");
			stb.append("     JUDGEMENT = '2' ");
			stb.append(" GROUP BY ");
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     FINSCHOOL_DIV ");
			stb.append(" ), DIV_BASE AS (SELECT "); // 国立、私立　ベース表
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     FINSCHOOL_DIV AS KEY_CD, ");
			stb.append("     DIVNAME AS KEY_NAME, ");
			stb.append("     'DIV' AS DIV ");
			stb.append(" FROM ");
			stb.append("     BASEALL ");
			stb.append(" WHERE ");
			stb.append("     FINSCHOOL_DIV IN ('1', '3') ");
			stb.append(" GROUP BY ");
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     FINSCHOOL_DIV, ");
			stb.append("     DIVNAME ");
			stb.append(" ), CHIKU_A AS (SELECT "); // 公立　受験校数
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     DISTRICTCD, ");
			stb.append("     COUNT(DISTINCT FS_CD) AS COUNT ");
			stb.append(" FROM ");
			stb.append("     BASEALL ");
			stb.append(" WHERE ");
			stb.append("     FINSCHOOL_DIV = '2' ");
			stb.append(" GROUP BY ");
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     DISTRICTCD ");
			stb.append(" ), CHIKU_B AS (SELECT "); // 公立　出願者数
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     DISTRICTCD, ");
			stb.append("     COUNT(EXAMNO) AS COUNT, ");
			stb.append("     COUNT(SEX = '1' OR NULL) AS SEX1, ");
			stb.append("     COUNT(SEX = '2' OR NULL) AS SEX2 ");
			stb.append(" FROM ");
			stb.append("     BASEALL ");
			stb.append(" WHERE ");
			stb.append("     FINSCHOOL_DIV = '2' ");
			stb.append(" GROUP BY ");
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     DISTRICTCD ");
			stb.append(" ), CHIKU_C AS (SELECT "); // 公立　志願者数
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     DISTRICTCD, ");
			stb.append("     COUNT(EXAMNO) AS COUNT, ");
			stb.append("     COUNT(SEX = '1' OR NULL) AS SEX1, ");
			stb.append("     COUNT(SEX = '2' OR NULL) AS SEX2 ");
			stb.append(" FROM ");
			stb.append("     BASEALL ");
			stb.append(" WHERE ");
			stb.append("     FINSCHOOL_DIV = '2' ");
			stb.append("     AND JUDGEMENT2 <> '1' OR JUDGEMENT2 IS NULL ");
			stb.append(" GROUP BY ");
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     DISTRICTCD ");
			stb.append(" ), CHIKU_D AS (SELECT "); // 公立　受験者数
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     DISTRICTCD, ");
			stb.append("     COUNT(EXAMNO) AS COUNT, ");
			stb.append("     COUNT(SEX = '1' OR NULL) AS SEX1, ");
			stb.append("     COUNT(SEX = '2' OR NULL) AS SEX2 ");
			stb.append(" FROM ");
			stb.append("     BASEALL ");
			stb.append(" WHERE ");
			stb.append("     FINSCHOOL_DIV = '2' AND ");
			stb.append("     JUDGEMENT <> '4' ");
			stb.append(" GROUP BY ");
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     DISTRICTCD ");
			stb.append(" ), CHIKU_E AS (SELECT "); // 公立　合格者数
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     DISTRICTCD, ");
			stb.append("     COUNT(EXAMNO) AS COUNT, ");
			stb.append("     COUNT(SEX = '1' OR NULL) AS SEX1, ");
			stb.append("     COUNT(SEX = '2' OR NULL) AS SEX2 ");
			stb.append(" FROM ");
			stb.append("     BASEALL ");
			stb.append(" WHERE ");
			stb.append("     FINSCHOOL_DIV = '2' AND ");
			stb.append("     JUDGEMENT = '1' ");
			stb.append(" GROUP BY ");
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     DISTRICTCD ");
			stb.append(" ), CHIKU_F AS (SELECT "); // 公立　評定平均全体
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     DISTRICTCD, ");
			stb.append("     DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG, ");
			stb.append("     COUNT(SEX = '1' OR NULL) AS SEX1, ");
			stb.append("     COUNT(SEX = '2' OR NULL) AS SEX2 ");
			stb.append(" FROM ");
			stb.append("     BASEALL ");
			stb.append(" WHERE ");
			stb.append("     FINSCHOOL_DIV = '2' AND ");
			stb.append("     JUDGEMENT <> '4' ");
			stb.append(" GROUP BY ");
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     DISTRICTCD ");
			stb.append(" ), CHIKU_G AS (SELECT "); // 公立　評定平均合格
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     DISTRICTCD, ");
			stb.append("     DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG, ");
			stb.append("     COUNT(SEX = '1' OR NULL) AS SEX1, ");
			stb.append("     COUNT(SEX = '2' OR NULL) AS SEX2 ");
			stb.append(" FROM ");
			stb.append("     BASEALL ");
			stb.append(" WHERE ");
			stb.append("     FINSCHOOL_DIV = '2' AND ");
			stb.append("     JUDGEMENT = '1' ");
			stb.append(" GROUP BY ");
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     DISTRICTCD ");
			stb.append(" ), CHIKU_H AS (SELECT "); // 公立　評定平均不合格
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     DISTRICTCD, ");
			stb.append("     DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG, ");
			stb.append("     COUNT(SEX = '1' OR NULL) AS SEX1, ");
			stb.append("     COUNT(SEX = '2' OR NULL) AS SEX2 ");
			stb.append(" FROM ");
			stb.append("     BASEALL ");
			stb.append(" WHERE ");
			stb.append("     FINSCHOOL_DIV = '2' AND ");
			stb.append("     JUDGEMENT = '2' ");
			stb.append(" GROUP BY ");
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     DISTRICTCD ");
			stb.append(" ), CHIKU_BASE AS (SELECT "); // 公立　ベース表
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     DISTRICTCD AS KEY_CD, ");
			stb.append("     DISTRICTNAME AS KEY_NAME, ");
			stb.append("     'DISTRICT' AS DIV ");
			stb.append(" FROM ");
			stb.append("     BASEALL ");
			stb.append(" WHERE ");
			stb.append("     FINSCHOOL_DIV = '2' ");
			stb.append(" GROUP BY ");
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     DISTRICTCD, ");
			stb.append("     DISTRICTNAME ");
			stb.append(" ), DIV_KEKKA AS ( ");
			stb.append(" SELECT ");
			stb.append("    'KEY' AS KEY, ");
			stb.append("    BASE.FINSCHOOL_PREF_CD, ");
			stb.append("    BASE.PREF_NAME, ");
			stb.append("    BASE.KEY_CD, ");
			stb.append("    BASE.KEY_NAME, ");
			stb.append("    BASE.DIV, ");
			stb.append("    A.COUNT AS SCHOOL, ");
			stb.append("    B.COUNT AS SHUTSUGAN, ");
			stb.append("    B.SEX1 AS SHUTSUGAN_SEX1, ");
			stb.append("    B.SEX2 AS SHUTSUGAN_SEX2, ");
			stb.append("    C.COUNT AS SHIGAN, ");
			stb.append("    C.SEX1 AS SHIGAN_SEX1, ");
			stb.append("    C.SEX2 AS SHIGAN_SEX2, ");
			stb.append("    D.COUNT AS JUKEN, ");
			stb.append("    D.SEX1 AS JUKEN_SEX1, ");
			stb.append("    D.SEX2 AS JUKEN_SEX2, ");
			stb.append("    E.COUNT AS GOUKAKU, ");
			stb.append("    E.SEX1 AS GOUKAKU_SEX1, ");
			stb.append("    E.SEX2 AS GOUKAKU_SEX2, ");
			stb.append("    F.AVG AS HYOUTEIALL, ");
			stb.append("    F.SEX1 AS HYOUTEIALL_SEX1, ");
			stb.append("    F.SEX2 AS HYOUTEIALL_SEX2, ");
			stb.append("    G.AVG AS HYOUTEIOK, ");
			stb.append("    G.SEX1 AS HYOUTEIOK_SEX1, ");
			stb.append("    G.SEX2 AS HYOUTEIOK_SEX2, ");
			stb.append("    H.AVG AS HYOUTEING, ");
			stb.append("    H.SEX1 AS HYOUTEING_SEX1, ");
			stb.append("    H.SEX2 AS HYOUTEING_SEX2 ");
			stb.append(" FROM ");
			stb.append("     CHIKU_BASE BASE ");
			stb.append(" LEFT JOIN CHIKU_A A ON A.FINSCHOOL_PREF_CD = BASE.FINSCHOOL_PREF_CD AND A.DISTRICTCD = BASE.KEY_CD");
			stb.append(" LEFT JOIN CHIKU_B B ON B.FINSCHOOL_PREF_CD = BASE.FINSCHOOL_PREF_CD AND B.DISTRICTCD = BASE.KEY_CD");
			stb.append(" LEFT JOIN CHIKU_C C ON C.FINSCHOOL_PREF_CD = BASE.FINSCHOOL_PREF_CD AND C.DISTRICTCD = BASE.KEY_CD");
			stb.append(" LEFT JOIN CHIKU_D D ON D.FINSCHOOL_PREF_CD = BASE.FINSCHOOL_PREF_CD AND D.DISTRICTCD = BASE.KEY_CD");
			stb.append(" LEFT JOIN CHIKU_E E ON E.FINSCHOOL_PREF_CD = BASE.FINSCHOOL_PREF_CD AND E.DISTRICTCD = BASE.KEY_CD");
			stb.append(" LEFT JOIN CHIKU_F F ON F.FINSCHOOL_PREF_CD = BASE.FINSCHOOL_PREF_CD AND F.DISTRICTCD = BASE.KEY_CD");
			stb.append(" LEFT JOIN CHIKU_G G ON G.FINSCHOOL_PREF_CD = BASE.FINSCHOOL_PREF_CD AND G.DISTRICTCD = BASE.KEY_CD");
			stb.append(" LEFT JOIN CHIKU_H H ON H.FINSCHOOL_PREF_CD = BASE.FINSCHOOL_PREF_CD AND H.DISTRICTCD = BASE.KEY_CD");
			stb.append(" UNION  ");
			stb.append(" SELECT ");
			stb.append("     'KEY' AS KEY, ");
			stb.append("     BASE2.FINSCHOOL_PREF_CD, ");
			stb.append("     BASE2.PREF_NAME, ");
			stb.append("     BASE2.KEY_CD, ");
			stb.append("     BASE2.KEY_NAME, ");
			stb.append("     BASE2.DIV, ");
			stb.append("     A2.COUNT AS SCHOOL, ");
			stb.append("     B2.COUNT AS SHUTSUGAN, ");
			stb.append("     B2.SEX1 AS SHUTSUGAN_SEX1, ");
			stb.append("     B2.SEX2 AS SHUTSUGAN_SEX2, ");
			stb.append("     C2.COUNT AS SHIGAN, ");
			stb.append("     C2.SEX1 AS SHIGAN_SEX1, ");
			stb.append("     C2.SEX2 AS SHIGAN_SEX2, ");
			stb.append("     D2.COUNT AS JUKEN, ");
			stb.append("     D2.SEX1 AS JUKEN_SEX1, ");
			stb.append("     D2.SEX2 AS JUKEN_SEX2, ");
			stb.append("     E2.COUNT AS GOUKAKU, ");
			stb.append("     E2.SEX1 AS GOUKAKU_SEX1, ");
			stb.append("     E2.SEX2 AS GOUKAKU_SEX2, ");
			stb.append("     F2.AVG AS HYOUTEIALL, ");
			stb.append("     F2.SEX1 AS HYOUTEIALL_SEX1, ");
			stb.append("     F2.SEX2 AS HYOUTEIALL_SEX2, ");
			stb.append("     G2.AVG AS HYOUTEIOK, ");
			stb.append("     G2.SEX1 AS HYOUTEIOK_SEX1, ");
			stb.append("     G2.SEX2 AS HYOUTEIOK_SEX2, ");
			stb.append("     H2.AVG AS HYOUTEING, ");
			stb.append("     H2.SEX1 AS HYOUTEING_SEX1, ");
			stb.append("     H2.SEX2 AS HYOUTEING_SEX2 ");
			stb.append(" FROM ");
			stb.append("     DIV_BASE BASE2 ");
			stb.append(" LEFT JOIN DIV_A A2 ON A2.FINSCHOOL_PREF_CD = BASE2.FINSCHOOL_PREF_CD AND A2.FINSCHOOL_DIV = BASE2.KEY_CD   ");
			stb.append(" LEFT JOIN DIV_B B2 ON B2.FINSCHOOL_PREF_CD = BASE2.FINSCHOOL_PREF_CD AND B2.FINSCHOOL_DIV = BASE2.KEY_CD   ");
			stb.append(" LEFT JOIN DIV_C C2 ON C2.FINSCHOOL_PREF_CD = BASE2.FINSCHOOL_PREF_CD AND C2.FINSCHOOL_DIV = BASE2.KEY_CD   ");
			stb.append(" LEFT JOIN DIV_D D2 ON D2.FINSCHOOL_PREF_CD = BASE2.FINSCHOOL_PREF_CD AND D2.FINSCHOOL_DIV = BASE2.KEY_CD   ");
			stb.append(" LEFT JOIN DIV_E E2 ON E2.FINSCHOOL_PREF_CD = BASE2.FINSCHOOL_PREF_CD AND E2.FINSCHOOL_DIV = BASE2.KEY_CD   ");
			stb.append(" LEFT JOIN DIV_F F2 ON F2.FINSCHOOL_PREF_CD = BASE2.FINSCHOOL_PREF_CD AND F2.FINSCHOOL_DIV = BASE2.KEY_CD   ");
			stb.append(" LEFT JOIN DIV_G G2 ON G2.FINSCHOOL_PREF_CD = BASE2.FINSCHOOL_PREF_CD AND G2.FINSCHOOL_DIV = BASE2.KEY_CD   ");
			stb.append(" LEFT JOIN DIV_H H2 ON H2.FINSCHOOL_PREF_CD = BASE2.FINSCHOOL_PREF_CD AND H2.FINSCHOOL_DIV = BASE2.KEY_CD  ");
			stb.append(" ORDER BY FINSCHOOL_PREF_CD,DIV,KEY_CD ");
			stb.append(" ) ");
			stb.append(" SELECT ");
			stb.append("     KEKKA.* ");
			stb.append(" FROM ");
			stb.append("     DIV_KEKKA KEKKA ");
			stb.append(" UNION  ");
			stb.append(" SELECT ");
			stb.append("     MAX(KEY), ");
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME, ");
			stb.append("     'ZZZ' AS KEY_CD, ");
			stb.append("     'ZZZ' AS KEY_NAME, ");
			stb.append("     'ZZZ' AS DIV, ");
			stb.append("     SUM(SCHOOL) AS SCHOOL, ");
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
			stb.append("     FINSCHOOL_PREF_CD, ");
			stb.append("     PREF_NAME ");
			stb.append(" UNION  ");
			stb.append(" SELECT ");
			stb.append("     KEY, ");
			stb.append("     'ZZZ' AS FINSCHOOL_PREF_CD, ");
			stb.append("     'ZZZ' AS PREF_NAME, ");
			stb.append("     'ZZZ' AS KEY_CD, ");
			stb.append("     'ZZZ' AS KEY_NAME, ");
			stb.append("     'ZZZ' AS DIV, ");
			stb.append("     SUM(SCHOOL) AS SCHOOL, ");
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
			stb.append(" ORDER BY KEY,FINSCHOOL_PREF_CD,DIV,KEY_CD ");

			log.debug(" applicant sql =" + stb.toString());

			ps = db2.prepareStatement(stb.toString());
			rs = ps.executeQuery();

			Prefecture prefecture;
			while (rs.next()) {
				final String finschool_Pref_Cd = rs.getString("FINSCHOOL_PREF_CD");
				final String pref_Name = rs.getString("PREF_NAME");
				final String key_Cd = rs.getString("KEY_CD");
				final String key_Name = rs.getString("KEY_NAME");
				final String div = rs.getString("DIV");
				final String school = rs.getString("SCHOOL");
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
				if("ZZZ".equals(finschool_Pref_Cd)) {
					_param._sougou = new Applicant(key_Cd, key_Name, div, school, shutsugan, shutsugan_Sex1,
							shutsugan_Sex2, shigan, shigan_Sex1, shigan_Sex2, juken, juken_Sex1, juken_Sex2, goukaku,
							goukaku_Sex1, goukaku_Sex2, hyouteiall, hyouteiall_Sex1, hyouteiall_Sex2, hyouteiok,
							hyouteiok_Sex1, hyouteiok_Sex2, hyouteing, hyouteing_Sex1, hyouteing_Sex2);
				}else {
					if(!retMap.containsKey(finschool_Pref_Cd)) {
				    	prefecture = new Prefecture(finschool_Pref_Cd, pref_Name);
				    	retMap.put(finschool_Pref_Cd, prefecture);
				    } else {
				    	prefecture = (Prefecture)retMap.get(finschool_Pref_Cd);
				    }

				    if("DIV".equals(div)) { //国立、私立
				    	if(!prefecture._divMap.containsKey(key_Cd)) {
							final Applicant applicant = new Applicant(key_Cd, key_Name, div, school, shutsugan, shutsugan_Sex1,
									shutsugan_Sex2, shigan, shigan_Sex1, shigan_Sex2, juken, juken_Sex1, juken_Sex2, goukaku,
									goukaku_Sex1, goukaku_Sex2, hyouteiall, hyouteiall_Sex1, hyouteiall_Sex2, hyouteiok,
									hyouteiok_Sex1, hyouteiok_Sex2, hyouteing, hyouteing_Sex1, hyouteing_Sex2);

							prefecture._divMap.put(key_Cd, applicant);
				    	}
				    } else if("ZZZ".equals(div)){
				    	if(!prefecture._divMap.containsKey(key_Cd)) {
							final Applicant applicant = new Applicant(key_Cd, key_Name, div, school, shutsugan, shutsugan_Sex1,
									shutsugan_Sex2, shigan, shigan_Sex1, shigan_Sex2, juken, juken_Sex1, juken_Sex2, goukaku,
									goukaku_Sex1, goukaku_Sex2, hyouteiall, hyouteiall_Sex1, hyouteiall_Sex2, hyouteiok,
									hyouteiok_Sex1, hyouteiok_Sex2, hyouteing, hyouteing_Sex1, hyouteing_Sex2);

							prefecture._divMap.put(key_Cd, applicant);
				    	}
				    } else { //公立 地区毎
				    	if(!prefecture._districtMap.containsKey(key_Cd)) {
							final Applicant applicant = new Applicant(key_Cd, key_Name, div, school, shutsugan, shutsugan_Sex1,
									shutsugan_Sex2, shigan, shigan_Sex1, shigan_Sex2, juken, juken_Sex1, juken_Sex2, goukaku,
									goukaku_Sex1, goukaku_Sex2, hyouteiall, hyouteiall_Sex1, hyouteiall_Sex2, hyouteiok,
									hyouteiok_Sex1, hyouteiok_Sex2, hyouteing, hyouteing_Sex1, hyouteing_Sex2);

							prefecture._districtMap.put(key_Cd, applicant);
				    	}
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
    private Map getTotalMap(final DB2UDB db2) throws SQLException {

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
			stb.append("     SCHOOL.FINSCHOOL_DIV, ");
			stb.append("     STG.NAME1 AS DIVNAME, ");
			stb.append("     SCHOOL.FINSCHOOL_NAME, ");
			stb.append("     SCHOOL.FINSCHOOL_PREF_CD, ");
			stb.append("     PREF.PREF_NAME, ");
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
			stb.append(" INNER JOIN      FINSCHOOL_MST SCHOOL ON SCHOOL.FINSCHOOLCD = BASE.FS_CD     ");
			stb.append(" INNER JOIN       PREF_MST PREF ON PREF.PREF_CD = SCHOOL.FINSCHOOL_PREF_CD     ");
			stb.append(" INNER JOIN      V_NAME_MST NMST2 ON NMST2.YEAR = '" + _param._year
					+ "' AND NMST2.NAMECD1 = 'Z003' AND NMST2.NAMECD2 = SCHOOL.DISTRICTCD   ");
			stb.append(" INNER JOIN ENTEXAM_SETTING_MST STG ON STG.ENTEXAMYEAR = '" + _param._year + "' AND STG.APPLICANTDIV = '" + _param._applicantDiv + "' AND STG.SETTING_CD = 'L015' AND STG.SEQ = SCHOOL.FINSCHOOL_DIV   ");
			stb.append(
					" INNER JOIN      ENTEXAM_APPLICANTCONFRPT_DAT CONF ON CONF.ENTEXAMYEAR = BASE.ENTEXAMYEAR AND CONF.APPLICANTDIV = BASE.APPLICANTDIV AND CONF.EXAMNO = BASE.EXAMNO AND CONF.TOTAL_ALL IS NOT NULL ");
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
			stb.append("  FINSCHOOL_PREF_CD, ");
			stb.append("  PREF_NAME, ");
			stb.append("  SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT = '1' ");
			stb.append(" GROUP BY  ENTEXAMYEAR,FINSCHOOL_PREF_CD, PREF_NAME,SEX ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append("  'OK' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  FINSCHOOL_PREF_CD, ");
			stb.append("  PREF_NAME, ");
			stb.append("  'ZZZ' AS SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT = '1' ");
			stb.append(" GROUP BY  ENTEXAMYEAR, FINSCHOOL_PREF_CD, PREF_NAME ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append("  'OK' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  'ZZZ' AS FINSCHOOL_PREF_CD, ");
			stb.append("  'ZZZ' AS PREF_NAME, ");
			stb.append("  SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT = '1' ");
			stb.append(" GROUP BY  ENTEXAMYEAR, SEX ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append(" 'OK' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  'ZZZ' AS FINSCHOOL_PREF_CD, ");
			stb.append("  'ZZZ' AS PREF_NAME, ");
			stb.append("  'ZZZ' AS SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT = '1' ");
			stb.append(" GROUP BY  ENTEXAMYEAR ");
			stb.append(" ORDER BY FINSCHOOL_PREF_CD,SEX ");
			stb.append(" ) , HYOUTEI_ALL AS ( ");
			stb.append("  SELECT ");
			stb.append("  'ALL' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  FINSCHOOL_PREF_CD, ");
			stb.append("  PREF_NAME, ");
			stb.append("  SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT <> '4' ");
			stb.append(" GROUP BY  ENTEXAMYEAR,FINSCHOOL_PREF_CD, PREF_NAME,SEX ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append("  'ALL' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  FINSCHOOL_PREF_CD, ");
			stb.append("  PREF_NAME, ");
			stb.append("  'ZZZ' AS SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT <> '4' ");
			stb.append(" GROUP BY  ENTEXAMYEAR, FINSCHOOL_PREF_CD, PREF_NAME ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append("  'ALL' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  'ZZZ' AS FINSCHOOL_PREF_CD, ");
			stb.append("  'ZZZ' AS PREF_NAME, ");
			stb.append("  SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT <> '4' ");
			stb.append(" GROUP BY  ENTEXAMYEAR, SEX ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append(" 'ALL' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  'ZZZ' AS FINSCHOOL_PREF_CD, ");
			stb.append("  'ZZZ' AS PREF_NAME, ");
			stb.append("  'ZZZ' AS SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT <> '4' ");
			stb.append(" GROUP BY  ENTEXAMYEAR ");
			stb.append(" ORDER BY FINSCHOOL_PREF_CD,SEX ");
			stb.append(" ) , HYOUTEI_NG AS ( ");
			stb.append("  SELECT ");
			stb.append("  'NG' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  FINSCHOOL_PREF_CD, ");
			stb.append("  PREF_NAME, ");
			stb.append("  SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT = '2' ");
			stb.append(" GROUP BY  ENTEXAMYEAR,FINSCHOOL_PREF_CD, PREF_NAME,SEX ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append("  'NG' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  FINSCHOOL_PREF_CD, ");
			stb.append("  PREF_NAME, ");
			stb.append("  'ZZZ' AS SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT = '2' ");
			stb.append(" GROUP BY  ENTEXAMYEAR, FINSCHOOL_PREF_CD, PREF_NAME ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append("  'NG' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  'ZZZ' AS FINSCHOOL_PREF_CD, ");
			stb.append("  'ZZZ' AS PREF_NAME, ");
			stb.append("  SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT = '2' ");
			stb.append(" GROUP BY  ENTEXAMYEAR, SEX ");
			stb.append(" UNION ");
			stb.append("  SELECT ");
			stb.append(" 'NG' AS KEY, ");
			stb.append("  ENTEXAMYEAR, ");
			stb.append("  'ZZZ' AS FINSCHOOL_PREF_CD, ");
			stb.append("  'ZZZ' AS PREF_NAME, ");
			stb.append("  'ZZZ' AS SEX, ");
			stb.append("  DECIMAL(ROUND(AVG(AVG),2),5,2) AS AVG ");
			stb.append(" FROM BASEALL BASE ");
			stb.append(" WHERE JUDGEMENT = '2' ");
			stb.append(" GROUP BY  ENTEXAMYEAR ");
			stb.append(" ORDER BY FINSCHOOL_PREF_CD,SEX ");
			stb.append(" ) SELECT * FROM HYOUTEI_ALL ");
			stb.append(" UNION SELECT * FROM HYOUTEI_OK ");
			stb.append(" UNION SELECT * FROM HYOUTEI_NG ");
			stb.append(" ORDER BY KEY,FINSCHOOL_PREF_CD,SEX ");

			log.debug("total sql = " + stb);
			ps = db2.prepareStatement(stb.toString());
			rs = ps.executeQuery();

			while (rs.next()) {
				final String key = rs.getString("KEY");
				final String prefCd = rs.getString("FINSCHOOL_PREF_CD");
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

    // 国立、私立、地区ごとの志願者クラス
    private class Applicant {
        final String _key_Cd;
        final String _key_Name;
        final String _key;
        final String _school;
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

		public Applicant(final String key_Cd, final String key_Name, final String key, final String school,
				final String shutsugan, final String shutsugan_Sex1, final String shutsugan_Sex2, final String shigan,
				final String shigan_Sex1, final String shigan_Sex2, final String juken, final String juken_Sex1,
				final String juken_Sex2, final String goukaku, final String goukaku_Sex1, final String goukaku_Sex2,
				final String hyouteiall, final String hyouteiall_Sex1, final String hyouteiall_Sex2,
				final String hyouteiok, final String hyouteiok_Sex1, final String hyouteiok_Sex2,
				final String hyouteing, final String hyouteing_Sex1, final String hyouteing_Sex2) {
			_key_Cd = key_Cd;
			_key_Name = key_Name;
			_key = key;
			_school = school;
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

    private class Prefecture {
    	final String _prefCd;
    	final String _prefName;
    	final Map<String,Applicant> _divMap;
    	final Map<String,Applicant> _districtMap;

    	public Prefecture(final String prefCd, final String prefName) {
    		_prefCd = prefCd;
    		_prefName = prefName;
    		_divMap = new LinkedMap();
    		_districtMap = new LinkedMap();
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
        Applicant _sougou;

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
