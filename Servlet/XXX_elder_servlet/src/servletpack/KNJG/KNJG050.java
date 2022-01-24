/**
 *	学校教育システム 賢者 [事務管理] 卒業証明書出力（クラス別）
 *
 *      2005/01/17 yamashiro
 *      2005/01/19 学科課程の表示に’課程’を入れる
 *      2005/01/27 FormにおいてFieldの変更に伴い修正
 *      2006/01/31 校長名が出力されない不具合を修正 --NO001 => 05/08/31 KNJG010_1 の仕様変更において 当プログラムを忘れていた
 *      2006/02/14 「クラス別」と「個人別」の印刷指示画面をひとつにまとめるに伴い、帳票出力プログムも統合する --NO001
 *                  => KNJG050_BASEを作成し、帳票出力処理はKNJG050_BASEへ移行。
 *      2006/02/21 東京都版の処理追加 --NO002
 */

package servletpack.KNJG;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJG050 {

    private static final Log log = LogFactory.getLog(KNJG050.class);

    private boolean _hasdata;
    private Param _param;

    /**
      *
      *  KNJG.classから最初に起動されるクラス
      *
      **/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス

        // print svf設定
        response.setContentType("application/pdf");
        svf.VrInit();                                         //クラスの初期化
        try {
            svf.VrSetSpoolFileStream(response.getOutputStream());     //PDFファイル名の設定
        } catch (java.io.IOException ex) {
            log.error("db new error:", ex);
        }

        // ＤＢ接続
        db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME") , "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
            db2.open();
        } catch (Exception ex) {
            log.error("db new error:", ex);
            if (db2 != null) {
            	db2.close();
            }
            return;
        }
        try {
            log.fatal(" $Revision: 71788 $ $Date: 2020-01-16 13:41:28 +0900 (木, 16 1 2020) $ ");
            KNJServletUtils.debugParam(request, log);
            _param = new Param(request, db2, svf);

            // 印刷処理
            printSvf(db2);
        } catch (Exception e) {
        	log.error("exception!", e);
        } finally {
            // 終了処理
        	if (null != _param) {
        		_param.close();
        	}
            if (!_hasdata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();

            try {
                db2.commit();
                db2.close();
            } catch (Exception ex) {
                log.error("db close error!", ex);
            }
        }
    }   //doGetの括り


    /**
     *  印刷処理
     */
    private void printSvf(final DB2UDB db2) {

	    final String sql = getSql();
	    // log.debug(" sql = " + sql);
	    final List<Map<String, String>> list = KnjDbUtils.query(db2, sql);

    	final Map<String, String> schoolKindCertifKindMap = new HashMap();
    	schoolKindCertifKindMap.put("P", "023");
    	schoolKindCertifKindMap.put("J", "022");
    	schoolKindCertifKindMap.put("H", "001");

        //フォーム設定
        for (final Map<String, String> row : list) {

        	final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
            final String certifNo = "1".equals(_param._certifNoSyudou) || "1".equals(_param._certif_no_8keta) ? KnjDbUtils.getString(row, "REMARK1") : KnjDbUtils.getString(row, "CERTIF_NO");
            final String schoolKind = KnjDbUtils.getString(row, "SCHOOL_KIND");
            if (_param._isKindai) {
                final KNJG010_1 o = (KNJG010_1) _param._printer;
//                certif1_out(svf, schregno, param._year, param._year, param._gakki, param._noticeday, certifNo);
                o.certif1_out(schregno, _param._year, _param._year, _param._gakki, _param._noticeday, certifNo);
                _hasdata = o.nonedata || _hasdata;
            } else {
                final KNJG010_1T o = (KNJG010_1T) _param._printer;
                //nonedata =  printPrivate(svf, schregno, certifNo, schoolKind) || nonedata;
                final String[] parama = new String[21];
                parama[0] = schregno;
                parama[1] = schoolKindCertifKindMap.get(schoolKind);
                parama[2] = _param._year;
                parama[3] = _param._gakki;
                parama[8] = _param._noticeday;
                parama[9] = certifNo;
                parama[11] = _param._year;
                parama[12] = _param._documentroot;
                parama[16] = null;
                parama[17] = _param._entGrdDateFormat;
                parama[18] = "checkbox".equals(_param._knjg050PrintStamp) ? _param._printStamp : _param._knjg050PrintStamp;
                parama[19] = _param._certifPrintRealName;
                parama[20] = _param._useShuryoShoumeisho;

                _hasdata = o.printSvfMain(parama, _param._year) || _hasdata;
            }
            //log.debug(" nonedata = " + nonedata);
        }
    }

    /**
     *  sql作成 卒業証明書出力
     */
    private String getSql() {

        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH REGD AS (");
        stb.append(" SELECT  T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T1.COURSECD, ");
        stb.append("         T1.MAJORCD, ");
        stb.append("         T2.SCHOOL_KIND ");
        stb.append("         FROM   SCHREG_REGD_DAT T1 ");
        stb.append("         LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("                                      AND T2.GRADE = T1.GRADE ");
        stb.append("         WHERE  T1.YEAR = '" + _param._year + "' ");
        stb.append("            AND T1.SEMESTER = '" + _param._gakki + "' ");
        if ("1".equals(_param._output)) {
            stb.append("            AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._gradeHrclass1)  + " ");
        } else {
            stb.append("            AND T1.GRADE || T1.HR_CLASS = '" + _param._gradeHrclass2 + "' ");
            stb.append("            AND T1.ATTENDNO IN " + SQLUtils.whereIn(true, _param._attendNo2) + " ");
        }
        stb.append("  ) ");
        stb.append(" SELECT  T1.SCHREGNO,");
        stb.append("        T1.SCHOOL_KIND, ");
        if ("1".equals(_param._certif_no_8keta)) {
            stb.append("        T7.REMARK1 ");
        } else {
            stb.append("        T5.CERTIF_NO, ");
            stb.append("        T7.REMARK1 ");
        }
        stb.append("  FROM   REGD T1 ");
        stb.append("  INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("  INNER JOIN SCHREG_ENT_GRD_HIST_DAT T9 ON T9.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("                                       AND T9.SCHREGNO = T1.SCHREGNO ");
        // 卒業見込み出力にチェックが入っていたら、卒業区分がnullの生徒の帳票を出力する。
        if (_param._sotugyoMikomi != null) {
            stb.append("                                   AND T9.GRD_DIV IS NULL ");
        } else {
            stb.append("                                   AND T9.GRD_DIV = '1' ");
        }
        if ("1".equals(_param._certif_no_8keta)) {
        	stb.append("  LEFT JOIN (");
        	stb.append("        SELECT  W1.SCHREGNO, ");
        	stb.append("                MAX(W1.CERTIF_INDEX) AS CERTIF_INDEX ");
        	stb.append("        FROM    CERTIF_ISSUE_DAT W1 ");
        	stb.append("        WHERE   W1.YEAR = '" + _param._year + "' ");
        	stb.append("                AND W1.CERTIF_KINDCD = '001' ");
        	stb.append("        GROUP BY W1.SCHREGNO ");
        	stb.append("  ) T8 ON T8.SCHREGNO = T1.SCHREGNO ");
        } else {
        	stb.append("  LEFT JOIN(");
        	stb.append("        SELECT  SCHREGNO, ");
        	stb.append("                MAX(CERTIF_NO) AS CERTIF_NO ");
        	stb.append("        FROM    CERTIF_ISSUE_DAT W1 ");
        	stb.append("        WHERE   W1.YEAR = '" + _param._year + "' AND ");
        	stb.append("                W1.CERTIF_KINDCD = '001' AND ");
        	stb.append("                W1.SCHREGNO IN (SELECT SCHREGNO FROM REGD) ");
        	stb.append("        GROUP BY W1.SCHREGNO ");
        	stb.append("  ) T5 ON T5.SCHREGNO = T1.SCHREGNO ");

        	stb.append("  LEFT JOIN (");
        	stb.append("        SELECT  SCHREGNO, ");
        	stb.append("                CERTIF_NO, ");
        	stb.append("                CERTIF_INDEX ");
        	stb.append("        FROM    CERTIF_ISSUE_DAT W1 ");
        	stb.append("        WHERE   W1.YEAR = '" + _param._year + "' AND ");
        	stb.append("                W1.CERTIF_KINDCD = '001' AND ");
        	stb.append("                W1.SCHREGNO IN (SELECT SCHREGNO FROM REGD) ");
        	stb.append("  ) T8 ON T8.SCHREGNO = T1.SCHREGNO AND T8.CERTIF_NO = T5.CERTIF_NO ");
        }

        stb.append("  LEFT JOIN (");
        stb.append("        SELECT  SCHREGNO, ");
        stb.append("                CERTIF_INDEX, ");
        stb.append("                REMARK1 ");
        stb.append("        FROM    CERTIF_DETAIL_EACHTYPE_DAT W1 ");
        stb.append("        WHERE   W1.YEAR = '" + _param._year + "' AND ");
        stb.append("                W1.TYPE = '1' AND ");
        stb.append("                W1.SCHREGNO IN (SELECT SCHREGNO FROM REGD) ");
        stb.append(   ") T7 ON T7.SCHREGNO = T1.SCHREGNO AND T7.CERTIF_INDEX = T8.CERTIF_INDEX ");
        stb.append("ORDER BY ");
        stb.append("     T1.GRADE ");
        stb.append("   , T1.HR_CLASS ");
        stb.append("   , T1.ATTENDNO ");
        return stb.toString();
    }

    private static class Param {
        final String _year;
        final String _gakki;
        final String _noticeday;
        final String _output;
        final String _sotugyoMikomi;
        final String _certifNoSyudou;
        final String _certif_no_8keta;
        final String _entGrdDateFormat;
        final String _certifPrintRealName;
        final String _useShuryoShoumeisho;
        final String _printStamp;
        final String _knjg050PrintStamp;
        final String _documentroot;

        private KNJDefineSchool _definecode;
        final boolean _isKindai;

        String[] _gradeHrclass1;
        String _gradeHrclass2;
        String[] _attendNo2;

        private Object _printer;

        Param(final HttpServletRequest request, final DB2UDB db2, final Vrw32alp svf) {
            _year = request.getParameter("YEAR");            //年度
            _gakki = request.getParameter("GAKKI");           //学期
            _noticeday = request.getParameter("NOTICEDAY");       //証明書日付
            if (request.getParameter("OUTPUT") != null) {
                _output = request.getParameter("OUTPUT");  //1:クラス別 2:個人別 NO001
            } else {
                _output = "1";
            }
            _sotugyoMikomi = request.getParameter("SOTUGYO_MIKOMI");       //卒業見込み出力チェックボックス
            _certifNoSyudou = request.getParameter("certifNoSyudou");       //証明書番号は手入力の値を表示するか
            _certif_no_8keta = request.getParameter("certif_no_8keta");
            _entGrdDateFormat = request.getParameter("ENT_GRD_DATE_FORMAT");     //入学・卒業日付は年月で表示する
            _certifPrintRealName = request.getParameter("certifPrintRealName");  // 戸籍名出力設定なしに戸籍名を印字する
            _useShuryoShoumeisho = request.getParameter("useShuryoShoumeisho");     //修了証明書を使用する
            _printStamp = request.getParameter("PRINT_STAMP");     //印影出力するチェックボックス
            _knjg050PrintStamp = request.getParameter("knjg050PrintStamp"); // プロパティー
            _documentroot = request.getParameter("DOCUMENTROOT");

        	final String nameZ010 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
        	_isKindai = "KINDAI".equals(nameZ010) || "KINJUNIOR".equals(nameZ010);

            _definecode = new KNJDefineSchool();  // 各学校における定数等設定
            _definecode.defineCode(db2, _year);  //各学校における定数等設定

            if ("1".equals(_output)) {  //1:クラス別 2:個人別 NO001
                _gradeHrclass1 = request.getParameterValues("CLASS_SELECTED");    //年・組  NO001
            } else {
                _gradeHrclass2 = request.getParameter("CMBCLASS");        //学年・組
                //出席番号をＳＱＬ用に編集
                if (request.getParameter("CLASS_SELECTED") != null) {
                	_attendNo2 = request.getParameterValues("CLASS_SELECTED");    //年・組  NO001:個人別の場合は出席番号
                } else {
                	_attendNo2 = request.getParameterValues("STUDENT_SELECTED");    //出席番号
                }
            }

            if (_isKindai) {
                KNJG010_1 o = new KNJG010_1(db2, svf, _definecode);
                o.pre_stat(null);
                _printer = o;
            } else {
                KNJG010_1T o = new KNJG010_1T(db2, svf, _definecode);
                o.pre_stat(null);
                _printer = o;
            }
        }

        private void close() {
            if (_isKindai) {
                KNJG010_1 o = (KNJG010_1) _printer;
                o.pre_stat_f();
            } else {
                KNJG010_1T o = (KNJG010_1T) _printer;
                o.pre_stat_f();
            }
        }
    }
}
