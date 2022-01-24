package servletpack.KNJJ;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *  学校教育システム 賢者 [特別活動管理]
 *
 *                      ＜ＫＮＪＪ０７０＞  部クラブ顧問割り当て一覧
 *
 *
 *  2004/11/20 nakamoto 光明版(LFA060M)を近大版(KNJJ070)へプログラムを移行
 *
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJJ070 {

    /* ロギング */
    private static final Log log = LogFactory.getLog(KNJJ070.class);

    private Vrw32alp svf = new Vrw32alp();  //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    private DB2UDB db2;                 //Databaseクラスを継承したクラス
    private String _useSchool_KindField;
    private String _SCHOOLCD;
    private String _SCHOOLKIND;
    private String _schkind;
    private String use_prg_schoolkind;
    private String selectSchoolKind;
    private String selectSchoolKindSql;
    private String useClubMultiSchoolKind;
    private String _simo;
    private String _fuseji;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        log.fatal("$Revision: 62734 $");
        KNJServletUtils.debugParam(request, log);

        final String[] param = new String[2];

        // パラメータの取得
        //String[] classcd = request.getParameterValues("CLUB_SELECTED");  // 対象部クラブコード
        try {
            param[0] = request.getParameter("YEAR");            // 年度
            //param[1] = request.getParameter("GAKKI");         // 学期
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD= request.getParameter("SCHOOLCD");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _schkind = request.getParameter("SCHKIND"); //校種コンボ
            use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            selectSchoolKind = request.getParameter("selectSchoolKind");
            useClubMultiSchoolKind = request.getParameter("useClubMultiSchoolKind");
            if (!StringUtils.isBlank(selectSchoolKind)) {
                final StringBuffer sql = new StringBuffer("('");
                final String[] split = StringUtils.split(selectSchoolKind, ":");
                for (int i = 0; i < split.length; i++) {
                    sql.append(split[i]);
                    if (i < split.length - 1) {
                        sql.append("','");
                    }
                }
                selectSchoolKindSql = sql.append("')").toString();
            }
            _simo = request.getParameter("simo");
            _fuseji = request.getParameter("fuseji");
        } catch (Exception ex ) {
            log.warn("[KNJJ070]parameter error!");
        }

        // print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

        // svf設定
        svf.VrInit();                          //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);   //PDFファイル名の設定
        svf.VrSetForm("KNJJ070.frm", 4);       //SuperVisualFormadeで設計したレイアウト定義態の設定
        //svf.VrAttribute("CLUBCD","FF=1");     //ＳＶＦ属性変更--->改ページ

        // ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("[KNJJ070]DB2 open error!", ex);
            return;
        }

        // ＳＶＦ作成処理
        PreparedStatement ps1 = null;
        boolean nonedata = false;                               //該当データなしフラグ
        Set_Head(param);                                        //作成日のメソッド
        for (int i = 0; i < param.length; i++) {
            log.debug("[KNJJ070]param[" + i + "]=" + param[i]);
        }

        //SQL作成
        try {
            final String sql = Pre_Stat1(param);
            log.debug(" sql = " + sql);
            ps1 = db2.prepareStatement(sql);        //一覧取得ＳＱＬ
        } catch (Exception ex) {
            log.warn("db2.prepareStatement error!", ex);
        }

        //for (int ia = 0; ia < classcd.length; ia++)
        if( svfout(param,ps1) ) nonedata = true;    //ＳＶＦ出力のメソッド

        log.debug("[KNJJ070]nonedata = "+nonedata);
        // 該当データ無し
        if (!nonedata) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }

        // 終了処理
        svf.VrQuit();
        DbUtils.closeQuietly(ps1);
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる

    }    //doGetの括り

    /** 作成日のメソッド **/
    private void Set_Head(final String[] param) {

        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;

        //  年度の取得
        try {
            svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度");
        } catch (Exception e) {
            log.warn("nendo get error!", e);
        }
        //  作成日(現在処理日)の取得
        try {
            returnval = getinfo.Control(db2);
            svf.VrsOut("DATE",KNJ_EditDate.h_format_JP(returnval.val3));        //作成日
        } catch (Exception e ) {
            log.warn("ctrl_date get error!", e);
        }
        getinfo = null;
        returnval = null;

    }//Set_Head()の括り

    private int getMS932ByteLength(final String str) {
        int len = 0;
        if (null != str) {
            try {
                len = str.getBytes("MS932").length;
            } catch (final Exception e) {
                log.debug("exception!", e);
            }
        }
        return len;
    }

    private String getStaffCd(final String staffCd) {
        String retStr = "";
        if (null != _simo && !"".equals(_simo)) {
            int len = staffCd.length();
            final int intSimo = Integer.parseInt(_simo);
            retStr = staffCd.substring(len - intSimo, len);
            for (int umeCnt = 0; umeCnt < len; umeCnt++) {
                retStr = _fuseji + retStr;
            }
            final int retStrLen = retStr.length();
            retStr = retStr.substring(intSimo, retStrLen);
            return retStr;
        } else {
            return staffCd;
        }
    }

    /*----------------------------*
     * ＳＶＦ作成
     *----------------------------*/
    private boolean svfout(final String[] param, final PreparedStatement ps1)
    {
        boolean nonedata = false;
        ResultSet rs = null;
        try {
            //int pp = 0;
            //ps1.setString(++pp,classcd);  //部クラブコード
            rs = ps1.executeQuery();

            // 照会結果の取得
            String staffcd = "00000000";
            int gyo = 0;
            while (rs.next()) {
                //グループ出力
                final String setStaffCd = rs.getString("ADVISER");
                svf.VrsOut("M_STAFFCD"  , setStaffCd);
                svf.VrsOut("M_NAME"         , setStaffCd);
                //職員コードのブレイク
                if (staffcd.equals(setStaffCd) && (gyo < 50)) {
                    svf.VrsOut("STAFFCD"    , "");
                    svf.VrsOut("NAME"   , "");
                } else {
                    svf.VrsOut("STAFFCD"    , getStaffCd(setStaffCd));
                    svf.VrsOut("NAME"   , rs.getString("STAFFNAME"));
                }
                //明細出力
                svf.VrsOut("CLUBCD"     , rs.getString("CLUBCD"));
                if (getMS932ByteLength(rs.getString("CLUBNAME")) > 20) {
                    svf.VrsOut("CLUBNAME2" , rs.getString("CLUBNAME"));
                } else {
                    svf.VrsOut("CLUBNAME" , rs.getString("CLUBNAME"));
                }
                svf.VrsOut("PLACE"  , rs.getString("ACTIVITY_PLACE"));
                svf.VrsOut("ROOM"   , rs.getString("CLUBROOM_ASSIGN"));
                svf.VrEndRecord();
                nonedata = true;
                staffcd = setStaffCd;//職員コードセット
                if (gyo == 50) {
                    gyo = 0;//初期化
                }
                gyo++;
                //初期化
                svf.VrsOut("CLUBCD"     , "");
                svf.VrsOut("CLUBNAME" , "");
                svf.VrsOut("PLACE"  , "");
                svf.VrsOut("ROOM"   , "");
            }
        } catch (Exception ex) {
            log.warn("[KNJJ070]svfout read error!", ex);
        } finally {
            DbUtils.closeQuietly(rs);
        }
        return nonedata;
    }

    /* 部クラブ顧問割り当て一覧
     */
    private String Pre_Stat1(final String[] param) {
        StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    w1.adviser, ");
        stb.append("    w2.staffname, ");
        stb.append("    w1.clubcd, ");
        stb.append("    w3.clubname, ");
        stb.append("    w3.activity_place, ");
        stb.append("    w3.clubroom_assign  ");
        stb.append("FROM ");
        stb.append("    club_adviser_dat w1  ");
        stb.append("    LEFT JOIN staff_mst w2 ON w2.staffcd=w1.adviser  ");
        stb.append("    LEFT JOIN club_mst w3 ON w3.clubcd=w1.clubcd  ");
        if ("1".equals(useClubMultiSchoolKind)) {
            stb.append("   AND w3.SCHOOLCD = W1.SCHOOLCD ");
            stb.append("   AND w3.SCHOOL_KIND = W1.SCHOOL_KIND ");
        } else if ("1".equals(use_prg_schoolkind)) {
            stb.append("   AND w3.SCHOOLCD = W1.SCHOOLCD ");
            stb.append("   AND w3.SCHOOL_KIND = W1.SCHOOL_KIND ");
            if (!StringUtils.isBlank(selectSchoolKindSql)) {
                stb.append("   AND w3.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("   AND w3.SCHOOL_KIND IN " + selectSchoolKindSql + " ");
            }
        } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND) && !StringUtils.isBlank(_SCHOOLCD)) {
            stb.append("   AND w3.SCHOOLCD = W1.SCHOOLCD ");
            stb.append("   AND w3.SCHOOL_KIND = W1.SCHOOL_KIND ");
        }
        stb.append("WHERE ");
        stb.append("    w1.year='"+param[0]+"' ");
        if ("1".equals(useClubMultiSchoolKind)) {
            stb.append("   AND w1.SCHOOLCD = '" + _SCHOOLCD + "' ");
            stb.append("   AND w1.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
        } else if ("1".equals(use_prg_schoolkind)) {
            if (!StringUtils.isBlank(_schkind)) {
                stb.append("   AND w1.SCHOOL_KIND = '" + _schkind + "' ");
            }
            if (!StringUtils.isBlank(selectSchoolKindSql)) {
                stb.append("   AND w1.SCHOOLCD = '" + _SCHOOLCD + "' ");
                stb.append("   AND w1.SCHOOL_KIND IN " + selectSchoolKindSql + " ");
            }
        } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND) && !StringUtils.isBlank(_SCHOOLCD)) {
            stb.append("   AND w1.SCHOOLCD = '" + _SCHOOLCD + "' ");
            stb.append("   AND w1.SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
        }
        stb.append("ORDER BY ");
        stb.append("    w1.adviser, ");
        stb.append("    w1.clubcd ");
        return stb.toString();
    }//Pre_Stat1()の括り

}  //クラスの括り
