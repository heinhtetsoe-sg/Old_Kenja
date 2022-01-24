// kanji=漢字
/*
 * $Id: 0d9a2e1160c65de0e5330bfe193c723177d6dff9 $
 *
 * 作成日: 2003/02/07 13:24:22 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2003-2007 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.io.IOException;
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
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *  学校教育システム 賢者 [保健管理]
 *
 *                  ＜ＫＮＪＦ０６０＞  統計資料
 *
 *  2003/02/07:歯科検査他の統計SVF出力で項目名をname_mstより取得して表示
 *  2003/02/10:身体測定平均値のみ出力するよう変更
 * 2003/11/12 nakamoto 和暦変換に対応
 * 2003/11/14 nakamoto テーブル変更による修正
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJF060 {
    private static final Log log = LogFactory.getLog("KNJF060.class");

    private boolean _hasData;   //該当データなしフラグ

    private KNJSchoolMst _knjSchoolMst;
    private String _useSchool_KindField;
    private String _SCHOOLKIND;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);

        final String[] param = new String[2];

        // パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");        // 年度
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
        } catch (Exception ex) {
            log.error("[KNJF060]parameter error!", ex);
        }

        // print設定
        response.setContentType("application/pdf");

        final Vrw32alp svf = new Vrw32alp(); //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        // svf設定
        svf.VrInit();                        //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());   //PDFファイル名の設定

        // ＤＢ接続
        DB2UDB db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch (Exception ex) {
            log.error("[KNJF060]DB2 open error!", ex);
        }

        //  学校区分の取得
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            _knjSchoolMst = new KNJSchoolMst(db2, param[0]);
            final String prefNameSql = "SELECT PREF_NAME FROM PREF_MST WHERE PREF_CD = '" + _knjSchoolMst._prefCd + "'";
            ps = db2.prepareStatement(prefNameSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                param[1] = rs.getString("PREF_NAME");
            }
        } catch (Exception e) {
            log.error("[KNJF060]KNJSchoolMst get error!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        /*-----------------------------------------------------------------------------
        ＳＶＦ作成処理       
        -----------------------------------------------------------------------------*/

        try {
            set_detail2(db2, param, svf);     //身体測定平均値
            
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            if (_hasData == false) {
                /*該当データ無し*/
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            
            // 終了処理
            db2.close();        // DBを閉じる
            svf.VrQuit();
        }

    }    //doGetの括り



    /*------------------------------------*
     * 身体測定平均値　SVF出力 2003/11/14 *
     *------------------------------------*/
    private void set_detail2(final DB2UDB db2, final String[] param, final Vrw32alp svf)
                     throws ServletException, IOException
    {
        
        int lineCnt = 1;
        int befGrade = 0;
        int befSex = 0;
        int setField = 1;
        int setManField = 1;
        int setWomanField = 4;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "";
            sql = "SELECT "
                    + "T1.GRADE, "
                    + "T1.SEX, "
                    + "T1.DISTRICT, "
                    + "T1.MEASUREMENT, "
                    + "T2.YEAR AS GDAT_YEAR, "
                    + "T2.GRADE_NAME1, "
                    + "DECIMAL(T1.MEASUREMENT_AVG,5,1) AS MEASUREMENT_AVG "
                + "FROM    BODYMEASURED_AVG_DAT T1 ";
            sql += "   LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            sql += "       AND T2.SCHOOL_KIND = '" + _SCHOOLKIND + "' ";
            sql +="WHERE   T1.YEAR = '" + param[0] + "' "
                + "ORDER BY "
                    + "T1.GRADE, "
                    + "T1.SEX";

            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            /** SVF-formへデータを出力 **/
            svf.VrSetForm("KNJF060.frm", 1);      //svf-form

            while (rs.next()) {
                if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND)) {
                    if (null == rs.getString("GDAT_YEAR")) {
                        continue;
                    }
                }
                svf.VrsOut("NENDO"    , KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度");
                svf.VrsOut("YEAR"     , KenjaProperties.gengou(Integer.parseInt(param[0])) + "年");
                svf.VrsOut("PREF_NAME", param[1]);

                final int district = rs.getInt("DISTRICT");       //1本校  2県内  3全国
                final int measurement = rs.getInt("MEASUREMENT"); //1身長　2体重　3座高
                final int grade = rs.getInt("GRADE");             //学年
                final int sex = rs.getInt("SEX");                 //1男　2女

                if (district < 1 || district > 3) {
                    continue;
                }
                if (measurement < 1 || measurement > 3) {
                    continue;
                }
                if (sex < 1 || sex > 2) {
                    continue;
                }

                if (befGrade > 0 && befSex > 0 && (grade != befGrade || sex != befSex)) {
                    if (sex == 1) {
                        setManField++;
                    } else {
                        setWomanField++;
                    }
                }

                //9列 * 8行で改行
                if (lineCnt > 72) {
                    svf.VrEndPage();  //出力し、改ページする
                    lineCnt = 1;
                    setManField = 1;
                    setWomanField = 4;
                }

                String strx = "";
                if (measurement == 1) {
                    strx = "HEIGHT" + district;
                } else if (measurement == 2) {
                    strx = "WEIGHT" + district;
                } else if (measurement == 3) {
                    strx = "SIT_HEIGHT" + district;
                }

                setField = sex == 1 ? setManField : setWomanField;
                svf.VrsOutn("GRADE"   , setField, rs.getString("GRADE_NAME1"));    //学年
                svf.VrsOutn(strx      , setField, rs.getString("MEASUREMENT_AVG"));                                 //身体測定平均値

                _hasData = true;
                befGrade = grade;
                befSex = sex;
                lineCnt++;
            }
        } catch (Exception ex) {
            log.error("[KNJF060]set_detail2 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        if (lineCnt > 2) {
            svf.VrEndPage();
        }

    }  //set_detail2の括り

}  //クラスの括り
