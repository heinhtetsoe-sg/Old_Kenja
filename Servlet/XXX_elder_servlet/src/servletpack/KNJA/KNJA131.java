// kanji=漢字
/*
 * $Id: 31df8f86ffa0e677748ed52a296b08556312b0a1 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJA;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;

/**
 * 学校教育システム 賢者 [学籍管理] 生徒指導要録 中等教育学校用（千代田区立九段） 2005/12/27 Build yamashiro
 * 2006/03/18 yamashiro ○印鑑の出力処理を追加 --NO001 2006/04/26 yamashiro
 * ○一括出力(学年指定かつHR組単位)処理を追加 --NO002
 */

public class KNJA131 {
    private static final Log log = LogFactory.getLog(KNJA131.class);

    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2(); // 帳票におけるＳＶＦおよびＤＢ２の設定

    private ResultSet rs; // NO002

    /**
     * KNJA.classから最初に起動されるクラス
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        Map paramap = new HashMap();
        boolean nonedata = false;

        // パラメータの取得
        getParam(request, paramap);

        // print svf設定
        sd.setSvfInit(request, response, svf);

        // ＤＢ接続
        db2 = sd.setDb(request);
        if (sd.openDb(db2)) {
            log.error("db open error! ");
            return;
        }

        // 印刷処理
        nonedata = printSvf(request, db2, svf, paramap);

        // 終了処理
        sd.closeSvf(svf, nonedata);
        sd.closeDb(db2);
    }

    /**
     * get parameter doGet()パラメータ受け取り
     */
    private void getParam(HttpServletRequest request, Map paramap) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        try {
            paramap.put("YEAR", request.getParameter("YEAR")); // 年度
            paramap.put("GAKKI", request.getParameter("GAKKI")); // 学期
            paramap.put("GRADE_HR_CLASS", request.getParameter("GRADE_HR_CLASS")); // 学年・組

            if (request.getParameter("simei") != null) {
                paramap.put("KANJI_OUT", request.getParameter("simei")); // 漢字名出力
            }

            if (request.getParameter("inei") != null) {
                paramap.put("INNEI_OUT", request.getParameter("inei")); // 陰影出力
            }

            if (request.getParameter("DOCUMENTROOT") != null) {
                paramap.put("DOCUMENTROOT", request.getParameter("DOCUMENTROOT")); // 陰影保管場所
                                                                                    // NO001
            }
            
            paramap.put("useCurriculumcd", request.getParameter("useCurriculumcd"));

            log.debug("paramap=" + paramap);
        } catch (Exception ex) {
            log.error("request.getParameter error!", ex);
        }
    }

    /**
     * 印刷処理 NO002 Modify
     */
    private boolean printSvf(HttpServletRequest request, DB2UDB db2, Vrw32alp svf, Map paramap) {
        KNJDefineCode definecode = new KNJDefineCode(); // 各学校における定数等設定
        definecode.setSchoolCode(db2, (String) paramap.get("YEAR"));
        definecode.schooldiv = "0";
        boolean nonedata = false;
        PreparedStatement ps = null;
        KNJA131BASE knj131 = null; // 帳票作成クラス
        List knjobj = new ArrayList(4); // 帳票作成JAVAクラスを格納
        List selectlist = new ArrayList(); // 出力対象学籍番号を格納
        String selected[] = null; // 出力対象HR組を格納

        try {
            if (request.getParameter("OUTPUT") != null && request.getParameter("OUTPUT").equals("2")) {
                selected = request.getParameterValues("category_selected"); // HR組を格納
                ps = db2.prepareStatement(prestatSchno(paramap)); // 任意のHR組の学籍番号取得用
            } else
                selected = request.getParameterValues("GRADE_HR_CLASS"); // HR組を格納

            setKnj131List(request, knjobj); // 帳票作成JAVAクラスをＬＩＳＴへ格納

            for (int i = 0; i < selected.length; i++) { // HR組の繰り返し
                if (request.getParameter("OUTPUT") != null && request.getParameter("OUTPUT").equals("2"))
                    setSchnoList(ps, selected[i], selectlist);
                else
                    setSchnoList(request, selectlist);

                for (Iterator t = selectlist.iterator(); t.hasNext();) { // --学籍番号の繰り返し
                    paramap.put("SCHNO", (String) (t.next()));

                    for (Iterator r = knjobj.iterator(); r.hasNext();) { // --帳票作成JAVAクラスの繰り返し
                        knj131 = (KNJA131BASE) (r.next());
                        knj131.prepareSqlState(db2, paramap, definecode); // PrepareStatement作成処理
                        knj131.setSvfForm(svf, paramap); // SVF-FORM設定
                        if (knj131.printSvf(db2, svf, paramap))
                            nonedata = true; // 印刷処理
                        if (i == selected.length - 1 && selectlist.size() == 1) // --最後のHR組、かつ最後の学籍番号の場合
                            knj131.closePrepareState(db2, paramap); // PrepareStatement
                                                                    // CLOSE処理
                    }

                    removeParamap(paramap); // 処理済み要素をMAPから削除
                    t.remove(); // 処理済み学籍番号をLISTから削除
                }

            }
        } catch (Exception ex) {
            log.error("error! ", ex);
        }
        return nonedata;
    }

    /**
     * 帳票作成JAVAクラスをＬＩＳＴへ格納 NO002 Build
     */
    private void setKnj131List(HttpServletRequest request, List knjobj) {
        try {
            if (request.getParameter("seito") != null)
                knjobj.add(new KNJA131FORM1()); // 様式１（学籍に関する記録）
            if (request.getParameter("tani") != null)
                knjobj.add(new KNJA131FORM2()); // 様式１の裏（修得単位の記録）
            if (request.getParameter("gakushu1") != null)
                knjobj.add(new KNJA131FORM3()); // 様式２（指導に関する記録）前期課程
            if (request.getParameter("gakushu2") != null)
                knjobj.add(new KNJA131FORM4()); // 様式２（指導に関する記録）後期課程
            if (request.getParameter("katsudo") != null)
                knjobj.add(new KNJA131FORM5()); // 様式３
        } catch (Exception ex) {
            log.error("error! ", ex);
        }
    }

    /**
     * 組単位の学籍番号をＬＩＳＴへ格納 NO002 Build
     */
    private void setSchnoList(PreparedStatement ps, String selected, List selectlist) {
        try {
            ps.setString(1, selected); // 組
            rs = ps.executeQuery();
            while (rs.next()) {
                selectlist.add(rs.getString("SCHREGNO"));
            }
            if (rs != null) {
                rs.close();
            }
        } catch (Exception ex) {
            log.error("error! ", ex);
        }
    }

    /**
     * 学籍番号をＬＩＳＴへ格納 NO002 Build
     */
    private void setSchnoList(HttpServletRequest request, List selectlist) {
        try {
            final String s[] = request.getParameterValues("category_selected"); // 学籍番号
            for (int i = 0; i < s.length; i++) {
                selectlist.add(s[i]);
            }
        } catch (Exception ex) {
            log.error("error! ", ex);
        }
    }

    /**
     * 組単位の学籍番号取得用ＳＱＬステートメント NO002 Build
     */
    private String prestatSchno(Map paramap) {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT SCHREGNO FROM SCHREG_REGD_DAT ");
            stb.append("WHERE YEAR = '" + (String) paramap.get("YEAR") + "' ");
            stb.append("AND SEMESTER = '" + (String) paramap.get("GAKKI") + "' ");
            stb.append("AND GRADE || HR_CLASS = ? ");
            stb.append("ORDER BY ATTENDNO ");
        } catch (Exception ex) {
            log.error("error! ", ex);
        }
        return stb.toString();
    }

    /**
     * 引数用マップより不要マップを削除
     */
    private void removeParamap(Map paramap) {
        try {
            if (paramap.containsKey("SCHNO")) {
                paramap.remove("SCHNO");
            }
            if (paramap.containsKey("SCHNAME")) {
                paramap.remove("SCHNAME");
            }
        } catch (Exception ex) {
            log.error("error! ", ex);
        }
    }

}
