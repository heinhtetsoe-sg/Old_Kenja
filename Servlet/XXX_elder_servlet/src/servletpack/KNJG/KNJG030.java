// kanji=漢字
/*
 * $Id: 3895b614f15a58a1cc7c5654d60980beea58e301 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJG;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
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

/*
 *
 *  学校教育システム 賢者 [事務管理]  単位修得証明書
 *  2005/11/18〜11/22 yamashiro  「処理日付をブランクで出力する」仕様の追加による修正
 *                          => 年度の算出は、処理日付がブランクの場合は印刷指示画面から受け取る「今年度」、処理日付がある場合は処理日付から割り出した年度とする
 *
 */

public class KNJG030 {
    private static final Log log = LogFactory.getLog(KNJG030.class);  //05/11/18
    private Vrw32alp svf = new Vrw32alp();      //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    private boolean nonedata;                   //該当データなしフラグ
    private final KNJDefineSchool _definecode = new KNJDefineSchool();  //各学校における定数等設定
    
    /*
     * HTTP Get リクエストの処理
     **/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        log.fatal("$Revision: 61807 $ $Date: 2018-08-14 13:36:29 +0900 (火, 14 8 2018) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        //  パラメータの取得
        final String[] pschregno = request.getParameterValues("category_selected");    //学籍番号
        final String[] param = new String[8];
        final Map paramap = new HashMap();
        setParameter(request, param, paramap);

        //  print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

        //  svf設定
        svf.VrInit();                         //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);    //PDFファイル名の設定

        DB2UDB db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch (Exception ex) {
            log.error("[KNJG030]DB2 open error!", ex);
            return;
        }

        //  ＳＶＦ作成
        student(db2, param, pschregno, paramap);     //在校生のPAPER作成

        //  終了処理
        if (nonedata == true) {
            svf.VrQuit();
        }
        db2.commit();
        db2.close();
        outstrm.close();
    }

    private void setParameter(final HttpServletRequest request, final String[] param, final Map paramap) {
        try {
            param[0] = request.getParameter("YEAR");            //年度
            param[1] = request.getParameter("SEMESTER");        //学期

            //記載日がブランクの場合桁数０で渡される事に対応
            if (request.getParameter("DATE") != null && 3 < request.getParameter("DATE").length()) {
                param[6] = request.getParameter("DATE");            //記載日
            }

            paramap.put("CTRL_YEAR", request.getParameter("CTRL_YEAR"));  //今年度
            paramap.put("CTRL_DATE", param[6]);
            paramap.put("tannishutokushoumeishoNotPrintAnotherStudyrec", StringUtils.defaultString(request.getParameter("tannishutokushoumeishoNotPrintAnotherStudyrec"), ""));
            paramap.put("useCurriculumcd", request.getParameter("useCurriculumcd"));
            paramap.put("useGakkaSchoolDiv", request.getParameter("useGakkaSchoolDiv"));
            paramap.put("SEKI", request.getParameter("SEKI"));
            
            final List containedParam = Arrays.asList(new String[] {"DATE"});
            for (final Enumeration en = request.getParameterNames(); en.hasMoreElements();) {
                final String name = (String) en.nextElement();
                if (paramap.containsKey(name) || containedParam.contains(name)) {
                    continue;
                }
                paramap.put(name, request.getParameter(name));
            }
        } catch (Exception ex) {
            log.error("[KNJG030]parameter error!", ex);
        }
        if (_definecode.schoolmark.equals("TOK")) {
//            param[7] = getCertificateNum(db2,param[0]);  // 証明書番号セット
        }
    }

    /**
     * 在校生のPAPER作成
     **/
    public void student(final DB2UDB db2, final String[] param, final String[] pschregno, final Map paramap) {
        _definecode.defineCode(db2, param[0]);  //各学校における定数等設定
        KNJG030_1 pobj = new KNJG030_1(db2, svf, _definecode);                    //ＳＶＦ出力用のインスタンス作成
        pobj.pre_stat(null, paramap);                                        //PrepareStatement作成
        for (int len = 0; len < pschregno.length; len++) {
            if (useCertifKindGrd(db2, pschregno[len])) {
                paramap.put("CERTIFKIND", "029");  // 証明書種別
            } else {
                paramap.put("CERTIFKIND", "011");  // 証明書種別
            }
            pobj.printSvf(param[0], param[1], pschregno[len], param[6], (String) paramap.get("CERTIFKIND"), param[7], paramap);
        }
        if (pobj.nonedata == true) {
            nonedata = true;
        }
        pobj.pre_stat_f();
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
        boolean isGrd = false;
        try {
            final StringBuffer stb = new StringBuffer();
            // 卒業区分が[nullではなく'4'(卒業見込み)以外]なら証明書種別は卒業生のものを使用する。
            stb.append("SELECT CASE WHEN (GRD_DIV IS NOT NULL AND GRD_DIV <> '4') THEN 1 ELSE 0 END AS DIV ");
            stb.append(" FROM SCHREG_BASE_MST WHERE SCHREGNO = '" + schregno + "' ");
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            if (rs.next()) {
                isGrd = "1".equals(rs.getString("DIV"));
            }
        } catch (Exception e) {
            log.error(e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return isGrd;
    }
}