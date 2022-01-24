/**
 *
 *  学校教育システム 賢者 [事務管理]  証明書交付台帳
 *
 *  2005/12/09 yamashiro 証明書別の発行台帳出力を追加 --NO001
 *  2006/02/10 yamashiro 証明書別の発行台帳出力を追加(05/12/09の続き) --NO001
 *                       学籍基礎データの対象に'GRD_BASE_MST'を含める --NO002
 *                       ページ初期値を(プログラムで)自動設定する処理を追加 --NO002
 *  2006/02/22 yamashiro 在校生を選択した場合印刷されない不具合を修正 --NO003
 */

package servletpack.KNJG;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_SchoolinfoSql;
import servletpack.KNJZ.detail.KnjDbUtils;



public class KNJG020 {

    private static final Log log = LogFactory.getLog(KNJG020.class);

    /**
     *  KNJD.classから最初に起動されるクラス
     *
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        Vrw32alp svf = new Vrw32alp();      //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                  //Databaseクラスを継承したクラス
        KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();    //帳票におけるＳＶＦおよびＤＢ２の設定

        // ＤＢ接続
        db2 = sd.setDb(request);
        if (sd.openDb(db2)) {
            log.error("db open error");
            return;
        }

        // パラメータの取得
        final Param param = getParam(db2, request);

        // print svf設定
        sd.setSvfInit(request, response, svf);

        // 印刷処理
        boolean hasdata = printSvf(db2, svf, param);

        // 終了処理
        sd.closeSvf(svf, hasdata);
        sd.closeDb(db2);
    }


    /**
     *  get parameter doGet()パラメータ受け取り
     *
     */
    private Param getParam(final DB2UDB db2, final HttpServletRequest request) {
        log.fatal("$Revision: 76377 $ $Date: 2020-09-03 13:51:51 +0900 (木, 03 9 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(db2, request);
    }


    /**
     *  印刷処理
     *
     */
    private boolean printSvf(final DB2UDB db2, final Vrw32alp svf, final Param param) {
        boolean hasdata = false;                               //該当データなしフラグ
        final String sql = prestatDetail(param);
        log.info(" sql = " + sql);

        final boolean isKindai = "KINDAI".equals(param._z010) || "KINJUNIOR".equals(param._z010);
        final boolean isKeisho = "Keisho".equals(param._z010);
        final String form = isKindai ? "KNJG020_KIN.frm" : "KNJG020.frm";
        final String nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(param._year)) + "年度";

        final List<List<Map<String, String>>> divList = getDivList(KnjDbUtils.query(db2, sql), param);

        int totalPage = 0;
        for (final List<Map<String, String>> rowList : divList) {

        	svf.VrSetForm(form, 4);
        	svf.VrsOut("NENDO", nendo);  //年度
        	svf.VrsOut("GRADUATE_FLG", param._radio);  //既卒区分
        	if (isKeisho) {
        		svf.VrsOut("scollname2",  param._schoolKindSchoolname1Map.get("J"));
        		svf.VrsOut("scollname" ,  param._schoolKindSchoolname1Map.get("H"));
        	} else {
        		svf.VrsOut("scollname",  param._schoolname);  //学校名称
        	}
        	if (!"1".equals(param._knjg020CertifDivNewPage) && !param._knjg020CertifKindcdNewPage) {
        		if (param._page != null) {  //--NO002 により条件追加
        			svf.VrSetPageCount(Integer.parseInt(param._page), 1);  //ページ初期値
        		}
        	}
        	for (int i = 0; i < rowList.size(); i++) {
        		final Map row = rowList.get(i);
            	if ("1".equals(param._knjg020CertifDivNewPage) || param._knjg020CertifKindcdNewPage) {
            		int page = 1 + totalPage + i / 20;
            		svf.VrAttribute("PAGE", "Edit=,Hensyu=3"); // 編集式カット
    				svf.VrsOut("PAGE", String.valueOf(page));
            	} else {
            		if (!hasdata) {
            			if (param._page != null) {
            				svf.VrSetPageCount(Integer.parseInt(KnjDbUtils.getString(row, "PAGENUM")), 1);  //ページ初期値
            			}
            		}
            	}
        		svf.VrsOut("CERTIF_KIND", Integer.parseInt(param._certifKind) == 0 ? "( 全て )" : "(" + KnjDbUtils.getString(row, "KINDNAME") + ")");  //証明書種別

        		svf.VrsOut("bango", KnjDbUtils.getString(row, "CERTIF_NO")); //発行番号
        		svf.VrsOut("date", KNJ_EditDate.getAutoFormatDate(db2, KnjDbUtils.getString(row, "ISSUEDATE"))); //発行年月日
        		svf.VrsOut("syurui", KnjDbUtils.getString(row, "KINDNAME")); //証明書の種類
        		if (null != KnjDbUtils.getString(row, "PRINT_REAL_NAME") && null != KnjDbUtils.getString(row, "REAL_NAME")) {
        			if ("1".equals(KnjDbUtils.getString(row, "NAME_OUTPUT_FLG")) && null != KnjDbUtils.getString(row, "NAME")) {
        				svf.VrsOut("name2_1", KnjDbUtils.getString(row, "REAL_NAME")); //戸籍氏名
        				final String[] name0 = KNJ_EditEdit.get_token(KnjDbUtils.getString(row, "NAME"), 20 - 4, 1);
        				svf.VrsOut("name2_2", "（" + name0[0] + "）"); //氏名
        			} else {
        				svf.VrsOut("name", KnjDbUtils.getString(row, "REAL_NAME")); //戸籍氏名
        			}
        		} else {
        			svf.VrsOut("name", KnjDbUtils.getString(row, "NAME")); //氏名
        		}
        		svf.VrsOut("sotuki", KnjDbUtils.getString(row, "GRADU")); //卒業期

        		if (!isKindai && "1".equals(KnjDbUtils.getString(row, "REMARK15"))) {
        			svf.VrAttribute("CANCEL_LINE", "UnderLine=(0,3,5), Keta=98"); // キャンセルは打ち消し線
        		}

        		svf.VrEndRecord();
        		hasdata = true;
        	}
        	totalPage += rowList.size() / 20 + (rowList.size() % 20 == 0 ? 0 : 1);
        }

        return hasdata;
    }

    private List<List<Map<String, String>>> getDivList(final List<Map<String, String>> rowList, final Param param) {
    	final List<List<Map<String, String>>> rtn = new ArrayList<List<Map<String, String>>>();
    	List<Map<String, String>> current = null;
    	if ("1".equals(param._knjg020CertifDivNewPage)) {
    		for (final Map<String, String> row : rowList) {
    			if (null == current) {
    				current = new ArrayList<Map<String, String>>();
    				rtn.add(current);
    			} else {
    				final Map<String, String> last = current.get(current.size() - 1);
    				final String oldCertifDiv = KnjDbUtils.getString(last, "CERTIF_DIV");
    				final String certifDiv = KnjDbUtils.getString(row, "CERTIF_DIV");
    				if (null != oldCertifDiv && !oldCertifDiv.equals(certifDiv)) {
    					current = new ArrayList<Map<String, String>>();
    					rtn.add(current);
    				}
    			}
    			current.add(row);
    		}
    	} else if(param._knjg020CertifKindcdNewPage) {
    		for (final Map<String, String> row : rowList) {
    			if (null == current) {
    				current = new ArrayList<Map<String, String>>();
    				rtn.add(current);
    			} else {
    				final Map<String, String> last = current.get(current.size() - 1);
    				final String oldCertifKindcd = KnjDbUtils.getString(last, "CERTIF_KINDCD");
    				final String certifKindcd = KnjDbUtils.getString(row, "CERTIF_KINDCD");
    				if (null != oldCertifKindcd && !oldCertifKindcd.equals(certifKindcd)) {
    					current = new ArrayList<Map<String, String>>();
    					rtn.add(current);
    				}
    			}
    			current.add(row);
    		}
    	} else {
			current = new ArrayList<Map<String, String>>();
			rtn.add(current);
			current.addAll(rowList);
    	}
		return rtn;
	}

	/**
     *   PrepareStatement作成
     */
    private String prestatDetail(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH ");
        //証明書交付台帳の表
        stb.append("CERTIF_DATA AS(");
        stb.append(    "SELECT  T1.CERTIF_INDEX ");
        if ("1".equals(param._certifNoSyudou) || "1".equals(param._certif_no_8keta)) {
            stb.append(       ",INT(T2.REMARK1) AS CERTIF_NO ");
        } else {
            stb.append(       ",T1.CERTIF_NO ");
        }
        stb.append(           ",T1.CERTIF_KINDCD ");
        stb.append(           ",T1.ISSUEDATE ");
        stb.append(           ",T1.SCHREGNO ");
        stb.append(           ",T1.GRADUATE_FLG ");
        stb.append(           ",T3.CERTIF_DIV ");
        stb.append(           ",T3.KINDNAME ");
        stb.append(    "FROM    CERTIF_ISSUE_DAT T1 ");
        if ("1".equals(param._certifNoSyudou) || "1".equals(param._certif_no_8keta)) {
            stb.append(        "INNER JOIN CERTIF_DETAIL_EACHTYPE_DAT T2 ");
            stb.append(             " ON T2.YEAR = T1.YEAR ");
            stb.append(             "AND T2.CERTIF_INDEX = T1.CERTIF_INDEX ");
            stb.append(             "AND T2.TYPE = '1' ");
        }
        stb.append("    INNER JOIN CERTIF_KIND_MST T3 ON T3.CERTIF_KINDCD = T1.CERTIF_KINDCD ");
        stb.append(    "WHERE   T1.ISSUECD = '1' ");
        stb.append(        "AND T1.YEAR ='" + param._year2 + "' ");
        if ("2".equals(param._radio)) {              //--->在学生
            stb.append(    "AND T1.GRADUATE_FLG = '0' ");
        } else if ("1".equals(param._radio)) {              //--->卒業生
            stb.append(    "AND T1.GRADUATE_FLG = '1' ");
        }
        if (Integer.parseInt( param._certifKind) != 0) {
            stb.append(    "AND T1.CERTIF_KINDCD = '" + param._certifKind + "' ");
        }
//        if ("1".equals(param._certifNoSyudou)) {
//            stb.append(    "AND T2.REMARK1 is not null ");
//        } else {
//            stb.append(    "AND T1.CERTIF_NO is not null ");
//        }
        stb.append(") ");

        //学籍の表
        stb.append(    ",SCHREG_DATA AS(");
        stb.append(       "SELECT  SCHREGNO, NAME, REAL_NAME, GRD_DATE FROM SCHREG_BASE_MST W1 ");
        stb.append(       "WHERE   EXISTS(SELECT 'X' FROM CERTIF_DATA W2 WHERE W1.SCHREGNO = W2.SCHREGNO GROUP BY W2.SCHREGNO) ");
        if (!"2".equals(param._radio)) {         //--->在学生のみではない場合
            //過去の卒業生
            stb.append(   "UNION ");
            stb.append(   "SELECT  SCHREGNO, NAME, REAL_NAME, GRD_DATE FROM GRD_BASE_MST W1 ");
            stb.append(   "WHERE   EXISTS(SELECT 'X' FROM CERTIF_DATA W2 WHERE W1.SCHREGNO = W2.SCHREGNO GROUP BY W2.SCHREGNO) ");
        }
        stb.append(") ");

        //対象学籍の証明書交付台帳の表
        stb.append(",DATA AS(");
        stb.append("SELECT ");
        stb.append("        ROW_NUMBER() OVER (ORDER BY ");
        if ("1".equals(param._knjg020CertifDivNewPage)) {
        	stb.append("        W1.CERTIF_DIV, ");
        }
        if(param._knjg020CertifKindcdNewPage) {
        	stb.append("        W1.CERTIF_KINDCD, ");
        }
        if (!"2".equals(param._output)) {
        	stb.append("        W1.ISSUEDATE, ");
        }
        stb.append("            W1.CERTIF_NO ");
        stb.append("            ) AS NUMBER ");
        stb.append(           ",W1.CERTIF_INDEX ");
        stb.append(           ",W1.CERTIF_NO ");
        stb.append(           ",W1.CERTIF_KINDCD ");
        stb.append(           ",W1.ISSUEDATE ");
        stb.append(           ",W1.SCHREGNO ");
        stb.append(           ",W1.GRADUATE_FLG ");
    	stb.append("           ,W1.CERTIF_DIV ");
    	stb.append("           ,W1.KINDNAME ");
        stb.append(    "FROM    CERTIF_DATA W1 ");
        stb.append(    "WHERE   SCHREGNO IN (SELECT SCHREGNO FROM SCHREG_DATA) ");
        stb.append(") ");

        //メイン表
        stb.append("SELECT ");
        stb.append("        T1.CERTIF_NO ");
        stb.append(       ",T1.CERTIF_KINDCD ");
        stb.append(       ",T1.ISSUEDATE ");
        stb.append(       ",T1.CERTIF_DIV ");
        stb.append(       ",T1.KINDNAME ");
        stb.append(       ",T2.NAME ");
        stb.append(       ",T2.REAL_NAME ");
        stb.append(       ",T4.SCHREGNO AS PRINT_REAL_NAME ");
        stb.append(       ",T4.NAME_OUTPUT_FLG ");
        stb.append(       ",T1.SCHREGNO ");
        stb.append(       ",CASE WHEN VALUE(T1.GRADUATE_FLG,'0') = '0' THEN NULL ");
        stb.append(       "      WHEN MONTH(T2.GRD_DATE) < 4 THEN ");
        stb.append(       "           INT(FISCALYEAR(T2.GRD_DATE)) - " + Integer.parseInt( param._foundedyear) + " - 1 ");
        stb.append(       "      ELSE INT(FISCALYEAR(T2.GRD_DATE)) - " + Integer.parseInt( param._foundedyear) + " - 2 ");
        stb.append(       " END AS GRADU ");
        stb.append(       ",T1.NUMBER/20 + CASE WHEN MOD(T1.NUMBER, 20) = 0 THEN 0 ELSE 1 END PAGENUM ");
        stb.append(       ",T22.REMARK15 "); // キャンセル
        stb.append("FROM    DATA T1 ");
        stb.append("LEFT JOIN CERTIF_DETAIL_EACHTYPE_DAT T22 ON T22.YEAR = '" + param._year + "' ");
        stb.append(             "AND T22.CERTIF_INDEX = T1.CERTIF_INDEX ");
        stb.append("INNER JOIN SCHREG_DATA T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("LEFT  JOIN CERTIF_KIND_MST T3 ON T3.CERTIF_KINDCD = T1.CERTIF_KINDCD ");
        stb.append("LEFT  JOIN SCHREG_NAME_SETUP_DAT T4 ON T4.DIV = '07' AND T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("WHERE   T1.NUMBER > (SELECT  MIN(I1.NUMBER) - CASE WHEN MOD(MIN(I1.NUMBER),20) = 0 THEN 20 ELSE MOD(MIN(I1.NUMBER),20) END ");
        stb.append(                     "FROM    DATA I1 ");
        stb.append(                     "WHERE   CHAR(I1.ISSUEDATE) >= '" + param._date + "') ");
        stb.append("ORDER BY T1.NUMBER ");
        return stb.toString();
    }

    private static class Param {

        final String _year;  //年度
        final String _semester;  //学期
        final String _radio;  //既卒区分
        final String _date;  //証明書発行日付
        final String _output;  //出力順 1発行日 2発行番号
        final String _year2;  //年度
        final String _certifKind;  //証明書種別
        final String _pageformat;
        String _page;
        final String _certifNoSyudou;
        final String _certif_no_8keta;
        final String _z010;
        final String _knjg020CertifDivNewPage;
        final boolean _knjg020CertifKindcdNewPage;

        String _schoolname;
        String _foundedyear;
        Map<String, String> _schoolKindSchoolname1Map = new HashMap<String, String>();

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _radio = request.getParameter("RADIO");
            _date = KNJ_EditDate.h_format_sec_2(request.getParameter("DATE"));
            _output = request.getParameter("OUTPUT");
            _year2 = KNJ_EditDate.b_year(request.getParameter("DATE"));
            _certifKind = StringUtils.defaultString(request.getParameter("CERTIF_KIND"), "0");  //証明書種別;
            _pageformat = StringUtils.defaultString(request.getParameter("OUTPUT2"), "1");
            if ("2".equals(_pageformat) && request.getParameter("PAGE") != null) {
                _page = request.getParameter("PAGE");
            }
            _certifNoSyudou = request.getParameter("certifNoSyudou");
            _certif_no_8keta = request.getParameter("certif_no_8keta");
            _knjg020CertifDivNewPage = request.getParameter("knjg020CertifDivNewPage");
            _knjg020CertifKindcdNewPage = ("1".equals(request.getParameter("knjg020CertifKindcdNewPage"))) ? true : false;
            _z010 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' AND NAME1 IS NOT NULL "));
            setSchoolInfo(db2);
        }

        /**
         *  SVF-FORM 見出し設定
         */
        private void setSchoolInfo(final DB2UDB db2) {
        	final Map schoolinfoParam = new HashMap();
        	final boolean hasSchoolMstSchoolKind = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");
			if (hasSchoolMstSchoolKind) {
        		final List<String> schoolKinds = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' ORDER BY GRADE "), "SCHOOL_KIND");
        		if (schoolKinds.contains("H")) {
        			schoolinfoParam.put("SCHOOL_KIND", "H");
        		} else if (schoolKinds.contains("J")) {
        			schoolinfoParam.put("SCHOOL_KIND", "J");
        		} else if (!schoolKinds.isEmpty()) {
        			schoolinfoParam.put("SCHOOL_KIND", schoolKinds.get(schoolKinds.size() - 1));
        		}
        	}
        	final String sql = new KNJ_SchoolinfoSql("10000").pre_sql(schoolinfoParam);
        	final Map<String, String> row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql, new Object[] {_year, _year}));
            if (!row.isEmpty()) {
                if (KnjDbUtils.getString(row, "FOUNDEDYEAR") != null) { _foundedyear = KnjDbUtils.getString(row, "FOUNDEDYEAR"); } //創立期
                if (KnjDbUtils.getString(row, "SCHOOLNAME1") != null) { _schoolname =  KnjDbUtils.getString(row, "SCHOOLNAME1"); } //学校名
            }
            if (null == _foundedyear) {
            	_foundedyear = "0"; //創立期
            }
            if (hasSchoolMstSchoolKind) {
            	_schoolKindSchoolname1Map.putAll(KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT SCHOOL_KIND, SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _year + "' "), "SCHOOL_KIND", "SCHOOLNAME1"));
            }
        }
    }

}//クラスの括り

