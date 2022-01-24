// kanji=漢字
/*
 * $Id: ef8eb0a72eef7657cce70493cdd6a880600b64eb $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJE;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Grade_Hrclass;

/**
 *  学校教育システム 賢者 [進路情報管理] 学業成績証明書
 */
public class KNJE080 {
    private static final Log log = LogFactory.getLog(KNJE080.class);

    private static final String CERTIF006_KNJE080_1 = "006";        // 成績証明書
    private static final String CERTIF007_KNJE080_2 = "007";        // 成績証明書 英語
    private static final String CERTIF027_KNJE080_1_GRD = "027";    // 成績証明書 卒業生用
    private static final String CERTIF033_KNJE080_1_JUNIOR = "033"; // 成績証明書 中学用
    private static final String CERTIF034_KNJE080_2_JUNIOR = "034"; // 成績証明書 中学用 英語
    
    private boolean _hasData;                   //該当データなしフラグ

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

    	//  print設定
        response.setContentType("application/pdf");

        Vrw32alp svf = new Vrw32alp();      //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                         //Databaseクラスを継承したクラス

        // ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception e) {
            log.error("[KNJE080]DB2 open error!", e);
        }
        
        log.fatal("$Revision: 63443 $ $Date: 2018-11-16 22:37:28 +0900 (金, 16 11 2018) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        final Param param = new Param(db2, request);

        try {
            //  ＳＶＦ設定
            svf.VrInit(); //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());    //PDFファイル名の設定

            //  ＳＶＦ作成
            final List schregnoMapList = param.getSchregnoMapList(db2);

            for (int i = 0; i < schregnoMapList.size(); i++) {
            	final Map schregnoMap = (Map) schregnoMapList.get(i);
            	final String schregno = (String) schregnoMap.get("SCHREGNO");
            	final String schoolKind = (String) schregnoMap.get("SCHOOL_KIND");
            	final boolean isJunior = "J".equals(schoolKind);
            	
            	if (Integer.parseInt(param._output) == 1) {
            		if (isJunior) {
            			jpn_paperJr(param, db2, svf, schregno);
            		} else {
            			jpn_paper(param, db2, svf, schregno);
            		}
            	} else {
            		if (isJunior) {
            			eng_paperJr(param, db2, svf, schregno);
            		} else {
            			eng_paper(param, db2, svf, schregno);
            		}
            	}
            }
        } catch (Exception e) {
            log.fatal("exception!", e);
        } finally {
        	if (null != param) {
        		if (null != param._knje080_1) {
        			param._knje080_1.pre_stat_f();
        		}
        		if (null != param._knje080_2) {
        			param._knje080_2.pre_stat_f();
        		}
        		if (null != param._knje080j_1) {
        			param._knje080j_1.pre_stat_f();
        		}
        		if (null != param._knje080j_2) {
        			param._knje080j_2.pre_stat_f();
        		}
        	}
            //  終了処理
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
            db2.commit();
            db2.close();                // DBを閉じる
        }
    }//public void doGetの括り
    

    /**中学用日本語のPAPER作成**/
    public void jpn_paperJr(final Param param, final DB2UDB db2, final Vrw32alp svf, final String schregno) {
        if (null == param._knje080j_1) {
        	param._knje080j_1 = new KNJE080J_1(db2, svf, param._paramap);
        	param._knje080j_1.pre_stat(null);
        }
        param._paramap.put("CERTIFKIND", CERTIF033_KNJE080_1_JUNIOR);  // 証明書種別
        param._knje080j_1.printSvf(param._year, param._gakki, param._date, schregno, param._paramap, param._staffCd, Integer.parseInt((String) param._paramap.get("CERTIFKIND")), null, (String) param._paramap.get("NUMBER"));
        if (param._knje080j_1.nonedata) {
            _hasData = true;
        }
    }

    /**中学用英語のPAPER作成**/
    public void eng_paperJr(final Param param, final DB2UDB db2, final Vrw32alp svf, final String schregno) {
        if (null == param._knje080j_2) {
        	param._knje080j_2 = new KNJE080J_2(db2, svf, param._paramap);
        	param._knje080j_2.pre_stat(null);
        }
        param._paramap.put("CERTIFKIND", CERTIF034_KNJE080_2_JUNIOR);  // 証明書種別
        param._knje080j_2.printSvf(param._year, param._gakki, param._date, schregno, param._paramap, param._staffCd, Integer.parseInt((String) param._paramap.get("CERTIFKIND")), null, (String) param._paramap.get("NUMBER"));
        if (param._knje080j_2.nonedata) {
            _hasData = true;
        }
    }

    /**日本語のPAPER作成**/
    public void jpn_paper(final Param param, final DB2UDB db2, final Vrw32alp svf, final String schregno) {
    	if (null == param._knje080_1) {
    		param._knje080_1 = new KNJE080_1(db2, svf, param._definecode);
    		param._knje080_1.pre_stat(null, param._paramap);
    	}
        final String certifKindcd;
        if (useCertifKindGrd(db2, schregno)) {
            certifKindcd = CERTIF027_KNJE080_1_GRD;
        } else {
            certifKindcd = CERTIF006_KNJE080_1;
        }
        param._paramap.put("CERTIFKIND", certifKindcd);  // 証明書種別
        param._knje080_1.printSvf(param._year, param._gakki, param._date, schregno, param._paramap, param._staffCd, Integer.parseInt((String) param._paramap.get("CERTIFKIND")), null, (String) param._paramap.get("NUMBER"));
        if (param._knje080_1.nonedata) {
            _hasData = true;
        }
    }

    /**英語のPAPER作成**/
    public void eng_paper(final Param param, final DB2UDB db2, final Vrw32alp svf, final String schregno) {
    	if (null == param._knje080_2) {
    		param._knje080_2 = new KNJE080_2(db2, svf, param._definecode);
    		param._knje080_2.pre_stat(null, param._paramap);
    	}
        final String certifKindcd = CERTIF007_KNJE080_2;
        param._paramap.put("CERTIFKIND", certifKindcd);  // 証明書種別
        param._knje080_2.printSvf(param._year, param._gakki, param._date, schregno, param._paramap, param._staffCd, Integer.parseInt((String) param._paramap.get("CERTIFKIND")), null, (String) param._paramap.get("NUMBER"));
        if (param._knje080_2.nonedata) {
            _hasData = true;
        }
    }

    /**
     * 証明書種別は卒業生のものを使用するか
     * @param db22
     * @param string
     * @return
     */
    private boolean useCertifKindGrd(final DB2UDB db2, final String schregno) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean useCertifKindGrd = false;
        try {
            final StringBuffer stb = new StringBuffer();
            // 卒業区分が[nullではなく'4'(卒業見込み)以外]なら証明書種別は卒業生のものを使用する。
            stb.append("SELECT CASE WHEN (GRD_DIV IS NOT NULL AND GRD_DIV <> '4') THEN 1 ELSE 0 END AS DIV ");
            stb.append(" FROM SCHREG_BASE_MST WHERE SCHREGNO = '" + schregno + "' ");
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            if (rs.next()) {
                useCertifKindGrd = "1".equals(rs.getString("DIV"));
            }
        } catch (Exception e) {
            log.error(e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return useCertifKindGrd;
    }
    
    private static class Param {
        final String _year; //年度
        final String _gakki; //学期
        final String _grade; //学年
        final String _staffCd; //記載責任者
        final String _date; //処理日付
        final String _prgId;
        final String _kotyo;
        final String[] _pschregno; //学籍番号
        final String _output;  //出力種別(1:進学 2:就職)
        final Map _paramap = new HashMap();        //HttpServletRequestからの引数
        private final KNJDefineSchool _definecode = new KNJDefineSchool();  //各学校における定数等設定
        
        private KNJE080J_1 _knje080j_1;
        private KNJE080J_2 _knje080j_2;
        private KNJE080_1 _knje080_1;
        private KNJE080_2 _knje080_2;

        public Param(final DB2UDB db2, final HttpServletRequest request) {

            _year = request.getParameter("YEAR");
            _gakki = request.getParameter("GAKKI");
            _output = request.getParameter("LANGUAGE");

            //  '学年＋組'パラメータを分解
            KNJ_Grade_Hrclass gradehrclass = new KNJ_Grade_Hrclass();           //クラスのインスタンス作成
            KNJ_Grade_Hrclass.ReturnVal returnval = gradehrclass.Grade_Hrclass(request.getParameter("GRADE_HR_CLASS"));
            _grade = returnval.val1;

            _staffCd = request.getParameter("SEKI");

            //記載日がブランクの場合桁数０で渡される事に対応 05/11/22 Modify
            if (request.getParameter("DATE") != null && 3 < request.getParameter("DATE").length()) {
                _date = request.getParameter("DATE");
            } else {
                _date = null;
            }

            _prgId = request.getParameter("PRGID");
            _kotyo = request.getParameter("KOTYO");

            _paramap.put( "CTRL_YEAR", request.getParameter("CTRL_YEAR") );  //今年度
            if (!_paramap.containsKey("FORM6")) {
                if ("on".equals(request.getParameter("FORM6")) || "1".equals(request.getParameter("FORM6"))) {
                    _paramap.put("FORM6", "on");  // ６年生用フォーム offの場合はparamapに追加しない。
                }
            }
            _paramap.put("Knje080UseAForm", request.getParameter("Knje080UseAForm"));
            if( _definecode.schoolmark.equals("TOK")) {
                _paramap.put("NUMBER", "");  // 証明書番号セット
            }

            _paramap.put("CTRL_DATE", _date);
            _paramap.put("PRGID", _prgId);
            _paramap.put("OUTPUT_PRINCIPAL", _kotyo);
            _paramap.put("seisekishoumeishoTaniPrintRyugaku", StringUtils.defaultString(request.getParameter("seisekishoumeishoTaniPrintRyugaku"), ""));
            _paramap.put("seisekishoumeishoNotPrintAnotherStudyrec", StringUtils.defaultString(request.getParameter("seisekishoumeishoNotPrintAnotherStudyrec"), ""));
            _paramap.put("useCurriculumcd", request.getParameter("useCurriculumcd"));
            _paramap.put("useGakkaSchoolDiv", request.getParameter("useGakkaSchoolDiv"));
            _paramap.put("useAddrField2", request.getParameter("useAddrField2"));
            for (final Enumeration enums = request.getParameterNames(); enums.hasMoreElements();) {
                final String parameterName = (String) enums.nextElement();
                if (!_paramap.containsKey(parameterName)) {
                    _paramap.put(parameterName, request.getParameter(parameterName));
                }
            }
            _pschregno = request.getParameterValues("category_selected");
        }
        
        /**学籍番号の並べ替え**/
        private List getSchregnoMapList(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List list = new ArrayList();
            try {
                String sql;
                sql = "SELECT "
                        + "T1.ATTENDNO,"
                        + "T1.SCHREGNO, "
                        + "T2.SCHOOL_KIND "
                    + "FROM "
                        + "SCHREG_REGD_DAT T1 "
                        + "LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE "
                    + "WHERE "
                        + "T1.YEAR = '" + _year + "' AND "
                        + "T1.SEMESTER = '" + _gakki + "' AND "
                        + "T1.SCHREGNO IN(";
                sql += "'" + _pschregno[0] + "'";
                for (int len = 1; len < _pschregno.length; len++) {
                    sql += ",'" + _pschregno[len] + "'";
                }
                sql = sql
                    + ") ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SCHREGNO";

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                	final Map map = new HashMap();
                	map.put("SCHREGNO", rs.getString("SCHREGNO"));
                	map.put("ATTENDNO", rs.getString("ATTENDNO"));
                	map.put("SCHOOL_KIND", rs.getString("SCHOOL_KIND"));
                	list.add(map);
                }
            } catch (Exception e) {
                log.error("[KNJE080]getSchregnoList error!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
    }
}//クラスの括り
