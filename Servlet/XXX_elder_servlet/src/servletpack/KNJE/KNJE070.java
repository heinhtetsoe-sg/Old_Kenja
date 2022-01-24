// kanji=漢字
/*
 * $Id: 922ae314855615f9739b261d5584a75538684bbd $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJE;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.Vrw32alpWrap;

/*
 *  学校教育システム 賢者 [進路情報管理] 高校用調査書
 *
 *  2004/04/27 yamashiro・学習成績概評の学年人数の出力条件を追加 -> 指示画面より受取
 *  2004/08/17 yamashiro・進学用に関して保健データを表示しない
 *  2004/09/13 yamashiro・所見出力用に印刷指示画面よりＯＳ選択パラメータを追加
 *  2005/11/18〜11/22 yamashiro  「処理日付をブランクで出力する」仕様の追加による修正
 *                          => 年度の算出は、処理日付がブランクの場合は印刷指示画面から受け取る「今年度」、処理日付がある場合は処理日付から割り出した年度とする
 *  2005/12/08 yamashiro・学籍処理日を引数として追加（「卒業日が学籍処理日の後なら卒業見込とする」仕様に伴い）
 *                        但し、現時点では指示画面が未対応のため、処理日を使用する
 */

public class KNJE070 {

	private static final Log log = LogFactory.getLog(KNJE070.class);
	
	private static String PRGID_KNJG081A = "KNJG081A"; 

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws Exception {

    	log.fatal("$Revision: 76366 $ $Date: 2020-09-03 10:09:41 +0900 (木, 03 9 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        final Map paramMap = createParamMap(request); //HttpServletRequestからの引数

        // ＤＢ接続
        final String dbname = request.getParameter("DBNAME");
        final DB2UDB db2 = new DB2UDB(dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2); //Databaseクラスを継承したクラス
        try {
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }

        final Param param = new Param(db2, request, paramMap);
        final boolean isShingakuyou = paramMap.containsKey("OUTPUT_SHINGAKU");
        final boolean isShushokuyou = paramMap.containsKey("OUTPUT_SHUSHOKU");

        Vrw32alpWrap svf = null;
        List csvOutputLines = null;
        boolean hasdata = false;                   //該当データなしフラグ
        try {
            // ＳＶＦ作成
            
            if (param._isCsv) {
            	csvOutputLines = new ArrayList();
            } else {
            	svf = new Vrw32alpWrap();      //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            	
                // ＳＶＦ設定
                response.setContentType("application/pdf");
                svf.VrInit();                         //クラスの初期化
                svf.VrSetSpoolFileStream(response.getOutputStream());    //PDFファイル名の設定
            }

            if (isShingakuyou) {
                hasdata = ent_paper(db2, svf, csvOutputLines, param, paramMap);     //進学用のPAPER作成
            } else if (isShushokuyou) {
                hasdata = emp_paper(db2, svf, csvOutputLines, param, paramMap);     //就職用のPAPER作成
            }

        } catch (Exception e) {
        	log.error("exception!", e);
        } finally {
            // 終了処理
            if (hasdata) {
            	if (param._isCsv) {
            		final Map csvParam = new HashMap();
                    csvParam.put("HttpServletRequest", request);
            		final String filename = "調査書" + (isShingakuyou ? "（進学用）" : isShushokuyou ? "（就職用）" : "") + ".csv";
            		CsvUtils.outputLines(log, response, filename, csvOutputLines, csvParam);
            	} else {
            		svf.VrQuit();
            	}
            }
            db2.commit();
            db2.close();
        }
    }
    
//	private String currentTime() {
//		final Calendar cal = Calendar.getInstance();
//		final DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
//		return df.format(cal.getTime());
//	}
    
    public Map createParamMap(final HttpServletRequest request) {
        final KNJDefineSchool defineSchool = new KNJDefineSchool();  //各学校における定数等設定
    	
        final Map map = new HashMap();        
    	map.put("KNJDefineSchool", defineSchool);

        if ("TOK".equals(defineSchool.schoolmark)) {
            map.put("NUMBER", "");
        }
        final String output = request.getParameter("OUTPUT");
        if ("1h".equals(output) || "2h".equals(output)) {
        	map.put("HANKI_NINTEI", "1"); // 半期認定の評定・単位数のフォーム
        }

        final boolean isShingakuyou = "1".equals(output) || "1h".equals(output);
        final boolean isShushokuyou = "2".equals(output) || "2h".equals(output);
        if (isShingakuyou) {
        	map.put("OUTPUT_SHINGAKU", "1");
        } else if (isShushokuyou) {
        	map.put("OUTPUT_SHUSHOKU", "1");
        }
        
        putParam(map, request, "OS");  //ＯＳ区分
        putParam(map, request, "CTRL_YEAR");  //今年度
        putParam(map, request, "MIRISYU");  // 未履修科目を出力する:1 しない:2
        putParam(map, request, "SONOTAJUUSYO");  // その他住所を優先して表示する。
        putParam(map, request, "HYOTEI");  // 評定の読み替え offの場合はparamapに追加しない。
        putParam(map, request, "FORM6");  // ６年生用フォーム offの場合はparamapに追加しない。
        putParam(map, request, "PRGID");                          // プログラムID
        putParam(map, request, "RISYU");  // 履修のみ科目出力　1:する／2:しない
        putParam(map, request, "useCurriculumcd");
        putParam(map, request, "useClassDetailDat");
        putParam(map, request, "useAddrField2");
        putParam(map, request, "useProvFlg");
        putParam(map, request, "useGakkaSchoolDiv");
        putParamDef(map, request, "TANIPRINT_SOUGOU", "1"); 
        putParamDef(map, request, "TANIPRINT_RYUGAKU", "1"); 
        putParamBl(map, request, "useSyojikou3");
        putParamBl(map, request, "certifNoSyudou");
        putParamBl(map, request, "useCertifSchPrintCnt");
        putParamBl(map, request, "gaihyouGakkaBetu");
        putParamBl(map, request, "train_ref_1_2_3_field_size");
        putParamBl(map, request, "train_ref_1_2_3_gyo_size");
        putParamBl(map, request, "3_or_6_nenYoForm");
        putParamBl(map, request, "tyousasyoSougouHyoukaNentani");
        putParamBl(map, request, "NENYOFORM");
        putParamBl(map, request, "tyousasyoTokuBetuFieldSize");
        putParamBl(map, request, "tyousasyoEMPTokuBetuFieldSize");
        putParamBl(map, request, "tyousasyoKinsokuForm");
        putParamBl(map, request, "tyousasyoNotPrintAnotherAttendrec");
        putParamBl(map, request, "tyousasyoNotPrintAnotherStudyrec");
        putParamReplace(map, request, "tyousasyoAttendrecRemarkFieldSize");
        putParamReplace(map, request, "tyousasyoTotalstudyactFieldSize");
        putParamReplace(map, request, "tyousasyoTotalstudyvalFieldSize");
        putParamReplace(map, request, "tyousasyoSpecialactrecFieldSize");
        putParamWithName(map, "CTRL_DATE", request, "DATE");  //学籍処理日
        putParamWithName(map, "OUTPUT_PRINCIPAL", request, "KOTYO");

        final List<String> containedParam = Arrays.asList("CTRL_DATE", "OUTPUT_PRINCIPAL", "category_selected");
        for (final Enumeration<String> en = request.getParameterNames(); en.hasMoreElements();) {
            final String name = en.nextElement();
            if (map.containsKey(name) || containedParam.contains(name)) {
                continue;
            }
            map.put(name, request.getParameter(name));
        }
        return map;
    }
    
    // パラメータで' 'が'+'に変換されるのでもとに戻す
    private void putParamReplace(final Map paramap, final HttpServletRequest req, final String name) {
        paramap.put(name, StringUtils.replace(StringUtils.defaultString(req.getParameter(name), ""), "+", " "));
    }
    
    // パラメータがnullの場合デフォルト値に置換
    private void putParamDef(final Map paramap, final HttpServletRequest req, final String name, final String defVal) {
        paramap.put(name, StringUtils.defaultString(req.getParameter(name), defVal));
    }
    
    // パラメータがnullの場合""に置換
    private void putParamBl(final Map paramap, final HttpServletRequest req, final String name) {
        putParamDef(paramap, req, name, "");
    }
    
    private void putParam(final Map paramap, final HttpServletRequest req, final String name) {
        paramap.put(name, req.getParameter(name));
    }
    
    private void putParamWithName(final Map paramap, final String paramname, final HttpServletRequest req, final String name) {
        paramap.put(paramname, req.getParameter(name));
    }
    
    /**進学用のPAPER作成**/
    private boolean ent_paper(final DB2UDB db2, final Vrw32alpWrap svf, final List csvOutputLines, final Param param, final Map paramap) throws SQLException {
    	boolean useCertifKindcd058 = false;
    	if (null != paramap.get("tyousasyo2020")) {
    		final boolean hasCertifKindMst058 = KnjDbUtils.query(db2, " SELECT CERTIF_KINDCD AS COUNT FROM CERTIF_KIND_MST WHERE CERTIF_KINDCD = '058' ").size() > 0;
    		final boolean hasCertifSchool058 = KnjDbUtils.query(db2, " SELECT CERTIF_KINDCD FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + param._year + "' AND CERTIF_KINDCD = '058' ").size() > 0;
    		if (hasCertifKindMst058 && hasCertifSchool058) {
    			useCertifKindcd058 = true;
    		}
    	}

    	final KNJDefineSchool definecode = (KNJDefineSchool) paramap.get("KNJDefineSchool");
        final KNJE070_1 pobj;
        if (param._isCsv) {
        	pobj = new KNJE070_1(db2, csvOutputLines, definecode, (String) paramap.get("useSyojikou3"));
        } else {
        	pobj = new KNJE070_1(db2, svf, definecode, (String) paramap.get("useSyojikou3"));
        }
        pobj.pre_stat(param._hyotei, paramap);

        for (final String schregno : param.schregnoArray) {
            final String certifkindcd;
            if (useCertifKindcd058) {
            	certifkindcd = "058";
            } else if (param.useCertifKindGrd(db2, param._year, schregno, param._date)) {
            	certifkindcd = "025";
            } else {
            	certifkindcd = "008";
            }
            paramap.put("CERTIFKIND", certifkindcd);  // 証明書種別
            paramap.put("CERTIFKIND2", certifkindcd);  // 証明書学校データ備考
            final int schPrintCnt = PRGID_KNJG081A.equals(param._prgid) ? ((Integer) paramap.get("PRINT_CNT_" + schregno)).intValue() : paramap.get("useCertifSchPrintCnt").equals("1") ? param.getPrintCnt(db2, "008", schregno) : 1;
            for (int i = 0; i < schPrintCnt; i++) {
                pobj.printSvf(schregno, param._year, param._gakki, param._date, param._staffcd, param._kanji, param._comment, param._os, (String) paramap.get("NUMBER"), paramap);
            }
        }

        pobj.pre_stat_f();
        return pobj.nonedata;
    }

    /**就職用のPAPER作成**/
    private boolean emp_paper(final DB2UDB db2, final Vrw32alpWrap svf, final List csvOutputLines, final Param param, final Map paramap) throws SQLException {
    	final KNJDefineSchool definecode = (KNJDefineSchool) paramap.get("KNJDefineSchool");
        final KNJE070_2 pobj;
        if (param._isCsv) {
        	pobj = new KNJE070_2(db2, csvOutputLines, definecode, (String) paramap.get("useSyojikou3"));
        } else {
        	pobj = new KNJE070_2(db2, svf, definecode, (String) paramap.get("useSyojikou3"));
        }
        pobj.pre_stat(param._hyotei, paramap);

        for (final String schregno : param.schregnoArray) {
            final String certifkindcd;
            if (param.useCertifKindGrd(db2, param._year, schregno, param._date)) {
            	certifkindcd = "026";
            } else {
            	certifkindcd = "009";
            }
            paramap.put("CERTIFKIND", certifkindcd);  // 証明書種別
            paramap.put("CERTIFKIND2", certifkindcd);  // 証明書学校データ備考
            final int schPrintCnt = PRGID_KNJG081A.equals(param._prgid) ? ((Integer) paramap.get("PRINT_CNT_" + schregno)).intValue() : paramap.get("useCertifSchPrintCnt").equals("1") ? param.getPrintCnt(db2, "009", schregno) : 1;
            for (int i = 0; i < schPrintCnt; i++) {
                pobj.printSvf(schregno, param._year, param._gakki, param._date, param._staffcd, param._kanji, param._comment, param._os, (String) paramap.get("NUMBER"), paramap);
            }
        }

        pobj.pre_stat_f();
        return pobj.nonedata;
    }

    private static class Param {
        final String _year;
        final String _gakki;
        final String _staffcd;
        final String _hyotei;
        final String _kanji;
        final String _date;
        final String _comment;
        final String _os;
        final boolean _isCsv;
        final String _prgid;
        final String[] schregnoArray;

        public Param(final DB2UDB db2, final HttpServletRequest request, final Map paramMap) {

            //記載日がブランクの場合桁数０で渡される事に対応
            String param6 = null;
            if (request.getParameter("DATE") != null  &&  3 < request.getParameter("DATE").length()) {
                param6 = request.getParameter("DATE");                            //処理日付
            }

            _year = request.getParameter("YEAR");                            //年度
            _gakki = request.getParameter("GAKKI");                           //学期
            _staffcd = request.getParameter("SEKI");                            //記載責任者
            _hyotei = StringUtils.defaultString(request.getParameter("HYOTEI"), "off");                          //評定の読み替え
            _kanji = request.getParameter("KANJI");                           //漢字出力
            _date = param6;
            _comment = request.getParameter("COMMENT");                         //学習成績概評
            _os = request.getParameter("OS");                              //ＯＳ区分 1:XP 2:WINDOWS2000
            _isCsv = "csv".equals(request.getParameter("cmd"));
            _prgid = request.getParameter("PRGID");
            
            if (PRGID_KNJG081A.equals(_prgid)) {
            	schregnoArray = getKNJG081Aschregno(db2, request, paramMap);
            } else {
            	schregnoArray = request.getParameterValues("category_selected");   // 学籍番号
            }
        }
        
        private String[] getKNJG081Aschregno(final DB2UDB db2, final HttpServletRequest request, final Map paramap) {
        	final List schregnoList = new ArrayList();
        	for (int i = 0; i < 1000; i++) {
        		final String schregno = request.getParameter("SCHREGNO-" + String.valueOf(i));
        		if (StringUtils.isBlank(schregno)) {
        			break;
        		}
        		final String count = request.getParameter("PRINT_CNT-" + String.valueOf(i));
        		if (!NumberUtils.isDigits(count)) {
        			continue;
        		}
        		schregnoList.add(schregno);
        		paramap.put("PRINT_CNT_" + schregno, Integer.valueOf(count));
        		log.info(" " + "PRINT_CNT_" + schregno + " => " + paramap.get("PRINT_CNT_" + schregno));
        	}
        	final String[] schregnos = new String[schregnoList.size()];
        	schregnoList.toArray(schregnos);
        	log.info(" schregnos = " + ArrayUtils.toString(schregnos));
    		return schregnos;
    	}
        
        private int getPrintCnt(final DB2UDB db2, final String certifKind, final String schregNo) throws SQLException {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     MAX(PRINT_CNT) AS PRINT_CNT, ");
            stb.append("     COUNT(PRINT_CNT) AS CNT ");
            stb.append(" FROM ");
            stb.append("     CERTIF_SCH_PRINT_CNT_DAT ");
            stb.append(" WHERE ");
            stb.append("     SCHREGNO = '" + schregNo + "' ");
            stb.append("     AND CERTIF_KINDCD = '" + certifKind + "' ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            int retVal = 1;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    retVal = rs.getInt("CNT") > 0 ? rs.getInt("PRINT_CNT") : 1;
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retVal;
        }
        
        /**
         * 証明書種別は卒業生のものを使用するか
         * @param db2
         * @param string
         * @return
         */
        private boolean useCertifKindGrd(final DB2UDB db2, final String year, final String schregno, final String date) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            boolean boo = false;
            try {
                // 卒業区分が[nullではなく'4'(卒業見込み)以外]なら証明書種別は卒業生のものを使用する。
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH T_SCHOOL_KIND AS ( ");
                stb.append("     SELECT DISTINCT T1.SCHREGNO, T1.YEAR, T2.SCHOOL_KIND ");
                stb.append("     FROM SCHREG_REGD_DAT T1 ");
                stb.append("     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ");
                stb.append("         AND T2.GRADE = T1.GRADE ");
                stb.append("     WHERE ");
                stb.append("         T1.SCHREGNO = '" + schregno + "' ");
                stb.append("         AND T2.YEAR = '" + year + "' ");
                stb.append(" ) ");
                stb.append(" SELECT CASE WHEN (T1.GRD_DIV IS NOT NULL AND T1.GRD_DIV <> '4' ");
                if (null != date) {
                    stb.append("          AND T1.GRD_DATE <= '" + date.replace('/', '-') + "' ");
                }
                stb.append("        ) THEN 1 ELSE 0 END AS DIV ");
                stb.append(" FROM SCHREG_ENT_GRD_HIST_DAT T1 ");
                stb.append(" INNER JOIN T_SCHOOL_KIND T2 ON T2.SCHREGNO = T1.SCHREGNO ");
                stb.append("     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                final String sql = stb.toString();
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    boo = "1".equals(rs.getString("DIV"));
                }
            } catch (Exception e) {
                log.error(e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return boo;
        }

    }
}
