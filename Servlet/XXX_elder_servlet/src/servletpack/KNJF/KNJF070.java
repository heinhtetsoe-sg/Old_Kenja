package servletpack.KNJF;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Schoolinfo_2;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *    学校教育システム 賢者 [保健管理]
 *
 *                    ＜ＫＮＪＦ０７０＞  統計資料
 *
 *  2003/02/07:歯科検査他の統計SVF出力で項目名をname_mstより取得して表示
 *  2003/02/10:身体測定平均値以外を出力するように変更
 * 2003/11/12 nakamoto 和暦変換に対応
 * 2003/11/14 nakamoto 学校区分に対応
 * 2003/11/27 nakamoto 再検査者を追加(set_detail2)
 * 2006/06/02 nakamoto 尿検査の統計：陽性者数、蛋白、潜血、糖に、「01='−'」「05='±'」は含まない
 */

public class KNJF070 {

    private static final Log log = LogFactory.getLog(KNJF070.class);

    private Vrw32alp svf = new Vrw32alp(); //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    private DB2UDB    db2;        // Databaseクラスを継承したクラス
    private boolean nonedata;     //該当データなしフラグ


    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws  SQLException, ServletException, IOException
    {
        // print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

        // svf設定
        svf.VrInit();                           //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);   //PDFファイル名の設定

        // ＤＢ接続
        db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
        }

        log.fatal("$Revision: 69257 $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);

        /*-----------------------------------------------------------------------------
        ＳＶＦ作成処理
         -----------------------------------------------------------------------------*/
        nonedata = false;

        //if (param._check1.equalsIgnoreCase("on"))        set_detail1(param);        //身体測定平均値
        if (param._check1.equalsIgnoreCase("on"))        set_detail2(param);        //視力検査の統計
        if (param._check2.equalsIgnoreCase("on"))        set_detail3(param);        //尿検査の統計
        if (param._check3.equalsIgnoreCase("on"))        set_detail4(param);        //貧血検査の統計
        if (param._check4.equalsIgnoreCase("on"))        set_detail5(param);        //歯科検査の統計
        if (param._check5.equalsIgnoreCase("on"))        set_detail6(param);        //歯科検査他の統計
        if (param._check6.equalsIgnoreCase("on"))     set_detail7(param);     //身体測定の統計

        if (nonedata == false) {
            /*該当データ無し*/
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }

        // 終了処理
        db2.close();        // DBを閉じる
        svf.VrQuit();
        outstrm.close();    // ストリームを閉じる

    }    //doGetの括り



//    /*------------------------------------*
//     * 身体測定平均値　SVF出力               *
//     *------------------------------------*/
//    private void set_detail1(final Param param)
//                     throws ServletException, IOException
//    {
//        boolean nonedata2 = false;
//
//        PreparedStatement ps = null;
//        ResultSet rs = null;
//        try {
//            String sql = new String();
//            sql = "SELECT "
//                    + "CASE T1.DISTRICT WHEN '1' THEN '3' when '2' then '2' WHEN '3' THEN '1' "
//                                            + "ELSE '0' END AS DISTRICT, "
//                    + "T1.MEASUREMENT, "
//                    + "DECIMAL(T1.BOYSTUDENT1,5,1), "
//                    + "DECIMAL(T1.BOYSTUDENT2,5,1), "
//                    + "DECIMAL(T1.BOYSTUDENT3,5,1), "
//                    + "DECIMAL(T1.GIRLSTUDENT1,5,1), "
//                    + "DECIMAL(T1.GIRLSTUDENT2,5,1), "
//                    + "DECIMAL(T1.GIRLSTUDENT3,5,1)  "
//                + "FROM    BODYMEASURED_AVG_DAT T1 "
//                + "WHERE   T1.YEAR = '" + param._year + "' "
//                + "ORDER BY "
//                    + "T1.DISTRICT, "
//                    + "T1.MEASUREMENT";
//
//            //log.debug("set_detail1 sql="+sql);
//            ps = db2.prepareStatement(sql);
//            rs = ps.executeQuery();
//
//                /** SVF-formへデータを出力 **/
//            svf.VrSetForm("KNJF070.frm", 1);        //svf-form
//            svf.VrsOut("NENDO"      , param._year);
//            svf.VrsOut("YEAR"      , param._year);
//            //svf.VrsOut("TODAY"      , param._ctrlDate);
//
//            while (rs.next()) {
//                int ia = rs.getInt("DISTRICT");        //1全国  2沖縄  3本校
//                int ib = rs.getInt("MEASUREMENT");    //1身長　2体重　3座高
//                if (ia<1 || ia>3)    continue;
//                if (ib<1 || ib>3)    continue;
//                String strx = new String();
//                if (ib==1)    strx = "HEIGHT" + ia;
//                if (ib==2)    strx = "WEIGHT" + ia;
//                if (ib==3)    strx = "SIT_HEIGHT" + ia;
//                svf.VrsOutn(strx    , 1, rs.getString(3));    //中学男子１年
//                svf.VrsOutn(strx    , 2, rs.getString(4));    //中学男子２年
//                svf.VrsOutn(strx    , 3, rs.getString(5));    //中学男子３年
//                svf.VrsOutn(strx    , 7, rs.getString(6));    //中学女子１年
//                svf.VrsOutn(strx    , 8, rs.getString(7));    //中学女子２年
//                svf.VrsOutn(strx    , 9, rs.getString(8));    //中学女子３年
//                nonedata2 = true;
//            }
//        } catch (Exception ex) {
//            log.error("set_detail1 read error!", ex);
//        } finally {
//            DbUtils.closeQuietly(null, ps, rs);
//            db2.commit();
//        }
//
//        if (nonedata2 == true) {
//            svf.VrEndRecord();
//            svf.VrEndPage();
//            nonedata = true;
//        }
//           //log.debug("set_detail1 path!");
//    }  //set_detail1の括り

    private static Map resultSetToMap(final ResultSet rs) throws SQLException {
        final ResultSetMetaData meta = rs.getMetaData();
        final Map map = new HashMap();
        for (int i = 0; i < meta.getColumnCount(); i++) {
            final String columnName = meta.getColumnName(i + 1);
            final String val = rs.getString(columnName);
            map.put(columnName, val);
            map.put(new Integer(i + 1), val);
        }
        return map;
    }

    /*------------------------------------*
     * 視力検査の統計　SVF出力               *
     *------------------------------------*/
    private void set_detail2(final Param param)
                     throws ServletException, IOException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("          GRADE,");
            sql.append("          SEX,");
            sql.append("          CNTSAIKEN,");
            sql.append("          ALL_CNT,");
            sql.append("          CNT1A,");
            sql.append("          CNT1B,");
            sql.append("          CNT1C,");
            sql.append("          CNT1D,");
            sql.append("          CNT2A,");
            sql.append("          CNT2B,");
            sql.append("          CNT2C,");
            sql.append("          CNT2D,");
            sql.append("          CNT3A,");
            sql.append("          CNT3B,");
            sql.append("          CNT3C,");
            sql.append("          CNT3D,");
            sql.append("          CASE WHEN CNT1A+CNT1B+CNT1C+CNT1D > 0 THEN DECIMAL(ROUND(FLOAT(CNT1A)/FLOAT(CNT1A+CNT1B+CNT1C+CNT1D)*100,1),5,1) ELSE 0 END AS CNT1AP,");
            sql.append("          CASE WHEN CNT2A+CNT2B+CNT2C+CNT2D > 0 THEN DECIMAL(ROUND(FLOAT(CNT2A)/FLOAT(CNT2A+CNT2B+CNT2C+CNT2D)*100,1),5,1) ELSE 0 END AS CNT2AP,");
            sql.append("          CASE WHEN CNT3A+CNT3B+CNT3C+CNT3D > 0 THEN DECIMAL(ROUND(FLOAT(CNT3A)/FLOAT(CNT3A+CNT3B+CNT3C+CNT3D)*100,1),5,1) ELSE 0 END AS CNT3AP,");
            sql.append("          CASE WHEN CNT1A+CNT1B+CNT1C+CNT1D > 0 THEN DECIMAL(ROUND(FLOAT(CNT1B+CNT1C+CNT1D)/FLOAT(CNT1A+CNT1B+CNT1C+CNT1D)*100,1),5,1) ELSE 0 END AS CNT1BCDP,");
            sql.append("          CASE WHEN CNT2A+CNT2B+CNT2C+CNT2D > 0 THEN DECIMAL(ROUND(FLOAT(CNT2B+CNT2C+CNT2D)/FLOAT(CNT2A+CNT2B+CNT2C+CNT2D)*100,1),5,1) ELSE 0 END AS CNT2BCDP,");
            sql.append("          CASE WHEN CNT3A+CNT3B+CNT3C+CNT3D > 0 THEN DECIMAL(ROUND(FLOAT(CNT3B+CNT3C+CNT3D)/FLOAT(CNT3A+CNT3B+CNT3C+CNT3D)*100,1),5,1) ELSE 0 END AS CNT3BCDP,");
            sql.append("          CNT1B+CNT1C+CNT1D AS CNT1BCD,");
            sql.append("          CNT2B+CNT2C+CNT2D AS CNT2BCD,");
            sql.append("          CNT3B+CNT3C+CNT3D AS CNT3BCD,");
            sql.append("          CASE WHEN ALL_CNT > 0 THEN DECIMAL(ROUND(FLOAT(CNT2A+CNT2B+CNT2C+CNT2D)/ALL_CNT*100,1),5,1) ELSE 0 END AS ALL_CNT2P,");
            sql.append("          CASE WHEN ALL_CNT > 0 THEN DECIMAL(ROUND(FLOAT(CNT3A+CNT3B+CNT3C+CNT3D)/ALL_CNT*100,1),5,1) ELSE 0 END AS ALL_CNT3P,");
            sql.append("          CNT2A+CNT2B+CNT2C+CNT2D AS ALL_CNT2, ");
            sql.append("          CNT3A+CNT3B+CNT3C+CNT3D AS ALL_CNT3 ");
            sql.append("      FROM ");
            sql.append("          (");
            sql.append("              SELECT ");
            sql.append("                  VALUE(T2.GRADE,'9999') AS GRADE,");
            sql.append("                  VALUE(T3.SEX,'3') AS SEX,");
            sql.append("                  SUM(CASE WHEN T1.REEXAMINE='02' THEN 1 ELSE 0 END) AS CNTSAIKEN,");
            sql.append("                  SUM(CASE WHEN T1.BAREVISION<>'X' THEN 1 WHEN T1.LIVINGVISION<>'X' THEN 1 WHEN T1.VISION<>'X' THEN 1 ELSE 0 END) AS ALL_CNT,");
            sql.append("                  SUM(CASE WHEN T1.BAREVISION='A' THEN 1 ELSE 0 END) AS CNT1A,");
            sql.append("                  SUM(CASE WHEN T1.BAREVISION='B' THEN 1 ELSE 0 END) AS CNT1B,");
            sql.append("                  SUM(CASE WHEN T1.BAREVISION='C' THEN 1 ELSE 0 END) AS CNT1C,");
            sql.append("                  SUM(CASE WHEN T1.BAREVISION='D' THEN 1 ELSE 0 END) AS CNT1D,");
            sql.append("                  SUM(CASE WHEN T1.VISION='A' THEN 1 ELSE 0 END) AS CNT2A,");
            sql.append("                  SUM(CASE WHEN T1.VISION='B' THEN 1 ELSE 0 END) AS CNT2B,");
            sql.append("                  SUM(CASE WHEN T1.VISION='C' THEN 1 ELSE 0 END) AS CNT2C,");
            sql.append("                  SUM(CASE WHEN T1.VISION='D' THEN 1 ELSE 0 END) AS CNT2D, ");
            sql.append("                  SUM(CASE WHEN T1.LIVINGVISION='A' THEN 1 ELSE 0 END) AS CNT3A,");
            sql.append("                  SUM(CASE WHEN T1.LIVINGVISION='B' THEN 1 ELSE 0 END) AS CNT3B,");
            sql.append("                  SUM(CASE WHEN T1.LIVINGVISION='C' THEN 1 ELSE 0 END) AS CNT3C,");
            sql.append("                  SUM(CASE WHEN T1.LIVINGVISION='D' THEN 1 ELSE 0 END) AS CNT3D ");
            sql.append("              FROM ");
            sql.append("                  (");
            sql.append("                      SELECT ");
            sql.append("                          SCHREGNO,");
            sql.append("                          CASE WHEN VALUE(EYEDISEASECD,'00') >= '02' THEN '02' ");
            sql.append("                                  ELSE '00' END AS REEXAMINE,");
            if ("1".equals(param._eyesight)) {
                sql.append("                                 CASE WHEN R_BAREVISION_MARK IN ('A')             AND L_BAREVISION_MARK IN ('A')             THEN 'A' ");
                sql.append("                                      WHEN R_BAREVISION_MARK IN ('A','B')         AND L_BAREVISION_MARK IN ('A','B')         THEN 'B' ");
                sql.append("                                      WHEN R_BAREVISION_MARK IN ('A','B','C')     AND L_BAREVISION_MARK IN ('A','B','C')     THEN 'C' ");
                sql.append("                                      WHEN R_BAREVISION_MARK IN ('A','B','C','D') AND L_BAREVISION_MARK IN ('A','B','C','D') THEN 'D' ");
                sql.append("                                      ELSE 'X' END ");
            } else {
                sql.append("                                 CASE WHEN R_BAREVISION >= '1.0' AND L_BAREVISION >= '1.0' THEN 'A' ");
                sql.append("                                      WHEN R_BAREVISION >= '0.7' AND L_BAREVISION >= '0.7' THEN 'B' ");
                sql.append("                                      WHEN R_BAREVISION >= '0.3' AND L_BAREVISION >= '0.3' THEN 'C' ");
                sql.append("                                      WHEN R_BAREVISION >  '0.0' AND L_BAREVISION >  '0.0' THEN 'D' ");
                sql.append("                                      ELSE 'X' END ");
            }
            sql.append("                                AS BAREVISION, ");
            if ("1".equals(param._eyesight)) {
                sql.append("                             CASE WHEN VALUE(R_VISION_MARK, L_VISION_MARK) IS NOT NULL THEN ");
                sql.append("                                       CASE WHEN VALUE(R_VISION_MARK, L_VISION_MARK) IN ('A')                AND VALUE(L_VISION_MARK, R_VISION_MARK) IN ('A')                THEN 'A' ");
                sql.append("                                            WHEN VALUE(R_VISION_MARK, L_VISION_MARK) IN ('A', 'B')           AND VALUE(L_VISION_MARK, R_VISION_MARK) IN ('A', 'B')           THEN 'B' ");
                sql.append("                                            WHEN VALUE(R_VISION_MARK, L_VISION_MARK) IN ('A', 'B', 'C')      AND VALUE(L_VISION_MARK, R_VISION_MARK) IN ('A', 'B', 'C')      THEN 'C' ");
                sql.append("                                            WHEN VALUE(R_VISION_MARK, L_VISION_MARK) IN ('A', 'B', 'C', 'D') AND VALUE(L_VISION_MARK, R_VISION_MARK) IN ('A', 'B', 'C', 'D') THEN 'D' ");
                sql.append("                                            ELSE 'X' END ");
                sql.append("                                  WHEN VALUE(R_BAREVISION_MARK, L_BAREVISION_MARK) IS NOT NULL THEN ");
                sql.append("                                       CASE WHEN VALUE(R_BAREVISION_MARK, L_BAREVISION_MARK) IN ('A')                AND VALUE(L_BAREVISION_MARK, R_BAREVISION_MARK) IN ('A')                THEN 'A' ");
                sql.append("                                            WHEN VALUE(R_BAREVISION_MARK, L_BAREVISION_MARK) IN ('A', 'B')           AND VALUE(L_BAREVISION_MARK, R_BAREVISION_MARK) IN ('A', 'B')           THEN 'B' ");
                sql.append("                                            WHEN VALUE(R_BAREVISION_MARK, L_BAREVISION_MARK) IN ('A', 'B', 'C')      AND VALUE(L_BAREVISION_MARK, R_BAREVISION_MARK) IN ('A', 'B', 'C')      THEN 'C' ");
                sql.append("                                            WHEN VALUE(R_BAREVISION_MARK, L_BAREVISION_MARK) IN ('A', 'B', 'C', 'D') AND VALUE(L_BAREVISION_MARK, R_BAREVISION_MARK) IN ('A', 'B', 'C', 'D') THEN 'D' ");
                sql.append("                                            ELSE 'X' END ");
                sql.append("                                  ELSE 'X' END ");
            } else {
                sql.append("                             CASE WHEN VALUE(R_VISION, L_VISION) IS NOT NULL THEN ");
                sql.append("                                       CASE WHEN VALUE(R_VISION, L_VISION) >= '1.0' AND VALUE(L_VISION, R_VISION) >= '1.0' THEN 'A' ");
                sql.append("                                            WHEN VALUE(R_VISION, L_VISION) >= '0.7' AND VALUE(L_VISION, R_VISION) >= '0.7' THEN 'B' ");
                sql.append("                                            WHEN VALUE(R_VISION, L_VISION) >= '0.3' AND VALUE(L_VISION, R_VISION) >= '0.3' THEN 'C' ");
                sql.append("                                            WHEN VALUE(R_VISION, L_VISION) >  '0.0' AND VALUE(L_VISION, R_VISION) >  '0.0' THEN 'D' ");
                sql.append("                                            ELSE 'X' END ");
                sql.append("                                  WHEN VALUE(R_BAREVISION, L_BAREVISION) IS NOT NULL THEN ");
                sql.append("                                       CASE WHEN VALUE(R_BAREVISION, L_BAREVISION) >= '1.0' AND VALUE(L_BAREVISION, R_BAREVISION) >= '1.0' THEN 'A' ");
                sql.append("                                            WHEN VALUE(R_BAREVISION, L_BAREVISION) >= '0.7' AND VALUE(L_BAREVISION, R_BAREVISION) >= '0.7' THEN 'B' ");
                sql.append("                                            WHEN VALUE(R_BAREVISION, L_BAREVISION) >= '0.3' AND VALUE(L_BAREVISION, R_BAREVISION) >= '0.3' THEN 'C' ");
                sql.append("                                            WHEN VALUE(R_BAREVISION, L_BAREVISION) >  '0.0' AND VALUE(L_BAREVISION, R_BAREVISION) >  '0.0' THEN 'D' ");
                sql.append("                                            ELSE 'X' END ");
                sql.append("                                  ELSE 'X' END ");
            }
            sql.append("                                AS LIVINGVISION, ");

            if ("1".equals(param._eyesight)) {
                sql.append("                                 CASE WHEN R_VISION_MARK IN ('A')             AND L_VISION_MARK IN ('A')             THEN 'A' ");
                sql.append("                                      WHEN R_VISION_MARK IN ('A','B')         AND L_VISION_MARK IN ('A','B')         THEN 'B' ");
                sql.append("                                      WHEN R_VISION_MARK IN ('A','B','C')     AND L_VISION_MARK IN ('A','B','C')     THEN 'C' ");
                sql.append("                                      WHEN R_VISION_MARK IN ('A','B','C','D') AND L_VISION_MARK IN ('A','B','C','D') THEN 'D' ");
                sql.append("                                      ELSE 'X' END ");
            } else {
                sql.append("                                 CASE WHEN R_VISION >= '1.0' AND L_VISION >= '1.0' THEN 'A' ");
                sql.append("                                      WHEN R_VISION >= '0.7' AND L_VISION >= '0.7' THEN 'B' ");
                sql.append("                                      WHEN R_VISION >= '0.3' AND L_VISION >= '0.3' THEN 'C' ");
                sql.append("                                      WHEN R_VISION >  '0.0' AND L_VISION >  '0.0' THEN 'D' ");
                sql.append("                                      ELSE 'X' END ");
            }
            sql.append("                                AS VISION ");
            sql.append("                      FROM ");
            sql.append("                          MEDEXAM_DET_DAT ");
            sql.append("                      WHERE ");
            sql.append("                              YEAR = '" +  param._year + "' ");
            if ("1".equals(param._eyesight)) {
                sql.append("                                 AND ((VALUE(R_BAREVISION_MARK, '') <> '' OR VALUE(L_BAREVISION_MARK, '') <> '')");
                sql.append("                                   OR (VALUE(R_VISION_MARK,     '') <> '' OR VALUE(L_VISION_MARK,     '') <> '')");
            } else {
                sql.append("                                 AND ((VALUE(R_BAREVISION,      '') <> '' OR VALUE(L_BAREVISION,      '') <> '')");
                sql.append("                                   OR (VALUE(R_VISION,          '') <> '' OR VALUE(L_VISION,          '') <> '')");
            }
            sql.append("                              OR (EYEDISEASECD >= '02'))");
            sql.append("                  ) T1 ");

            sql.append("                  INNER JOIN (");
            sql.append("                      SELECT ");
            sql.append("                          T1.SCHREGNO,");
            sql.append("                          MAX(T1.GRADE) AS GRADE ");
            sql.append("                      FROM ");
            sql.append("                          SCHREG_REGD_DAT T1 ");
            sql.append("                          INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND = '" + param._schoolKind + "' ");
            sql.append("                      WHERE ");
            sql.append("                          T1.YEAR = '" +  param._year + "' ");
            sql.append("                      GROUP BY ");
            sql.append("                          T1.SCHREGNO ");
            sql.append("                  ) T2 ON T1.SCHREGNO = T2.SCHREGNO ");

            sql.append("                  INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");

            sql.append("              GROUP BY ");
            sql.append("              GROUPING SETS ");
            sql.append("                  ((T2.GRADE,T3.SEX),(T3.SEX),())");
            sql.append("          ) T1 ");

            sql.append("      ORDER BY ");
            sql.append("          T1.GRADE,");
            sql.append("          T1.SEX");

            //log.debug("sql2 = " + sql);
            ps = db2.prepareStatement(sql.toString());
            rs = ps.executeQuery();

            final Map gradeMap = new HashMap();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                if (null == gradeMap.get(grade)) {
                    gradeMap.put(grade, new HashMap());
                }
                final Map m = (Map) gradeMap.get(grade);
                m.put(rs.getString("SEX"), resultSetToMap(rs));
            }

            svf.VrSetForm("KNJF070_1.frm", 4);
			svf.VrsOut("NENDO"    , param._nendo);
            svf.VrsOut("TODAY"    , param._ctrlDateFormat);
            svf.VrsOut("SCHOOL_KIND_NAME" , param._schoolKindName);
            for (final Iterator it = param._gradeAgeList.iterator(); it.hasNext();) {
                final GradeAge g = (GradeAge) it.next();
                svf.VrsOut("GRADE", g._name);

                final Map keyMap = (Map) gradeMap.get(g._grade);
                if (null != keyMap) {
                    for (final Iterator sit = keyMap.keySet().iterator(); sit.hasNext();) {
                        final String key = (String) sit.next();
                        final Map valueMap = (Map) keyMap.get(key);
                        final String sfx = "1".equals(key) ? "_M" : "2".equals(key) ? "_F" : null;
                        svf.VrsOut("NUMBER" + sfx, (String) valueMap.get("ALL_CNT"));
                        svf.VrsOut("A1" + sfx, (String) valueMap.get("CNT1A"));
                        svf.VrsOut("B1" + sfx, (String) valueMap.get("CNT1B"));
                        svf.VrsOut("C1" + sfx, (String) valueMap.get("CNT1C"));
                        svf.VrsOut("D1" + sfx, (String) valueMap.get("CNT1D"));
                        svf.VrsOut("A2" + sfx, (String) valueMap.get("CNT3A"));
                        svf.VrsOut("B2" + sfx, (String) valueMap.get("CNT3B"));
                        svf.VrsOut("C2" + sfx, (String) valueMap.get("CNT3C"));
                        svf.VrsOut("D2" + sfx, (String) valueMap.get("CNT3D"));
                    }
                }
                svf.VrEndRecord();
            }

            final Map keyMap = (Map) gradeMap.get("9999");
            if (null != keyMap) {
                for (final Iterator sit = keyMap.keySet().iterator(); sit.hasNext();) {
                    final String key = (String) sit.next();
                    final Map valueMap = (Map) keyMap.get(key);
                    final String sfx = "1".equals(key) ? "_M" : "2".equals(key) ? "_F" : "3".equals(key) ? "" : null;
                    svf.VrsOut("TOTAL_NUMBER" + sfx, (String) valueMap.get("ALL_CNT"));
                    svf.VrsOut("TOTAL_A1" + sfx, (String) valueMap.get("CNT1A"));
                    svf.VrsOut("TOTAL_B1" + sfx, (String) valueMap.get("CNT1B"));
                    svf.VrsOut("TOTAL_C1" + sfx, (String) valueMap.get("CNT1C"));
                    svf.VrsOut("TOTAL_D1" + sfx, (String) valueMap.get("CNT1D"));
                    svf.VrsOut("TOTAL_A2" + sfx, (String) valueMap.get("CNT3A"));
                    svf.VrsOut("TOTAL_B2" + sfx, (String) valueMap.get("CNT3B"));
                    svf.VrsOut("TOTAL_C2" + sfx, (String) valueMap.get("CNT3C"));
                    svf.VrsOut("TOTAL_D2" + sfx, (String) valueMap.get("CNT3D"));
                    if ("3".equals(key)) {
                        svf.VrsOut("NORMAL1_1"     , (String) valueMap.get("CNT1AP"));        //正常・裸眼の割合
                        svf.VrsOut("NORMAL1_2"     , (String) valueMap.get("CNT1A"));         //正常・裸眼の人数
                        svf.VrsOut("NORMAL2_1"     , (String) valueMap.get("CNT3AP"));        //正常・生活視力の割合
                        svf.VrsOut("NORMAL2_2"     , (String) valueMap.get("CNT3A"));         //正常・生活視力の人数
                        svf.VrsOut("CAUTIONS1_1"     , (String) valueMap.get("CNT1BCDP"));    //要注意・裸眼の割合
                        svf.VrsOut("CAUTIONS1_2"     , (String) valueMap.get("CNT1BCD"));     //要注意・裸眼の人数
                        svf.VrsOut("CAUTIONS2_1"     , (String) valueMap.get("CNT3BCDP"));    //要注意・生活視力の割合
                        svf.VrsOut("CAUTIONS2_2"     , (String) valueMap.get("CNT3BCD"));     //要注意・生活視力の人数
                        svf.VrsOut("REEXAMINE"     , (String) valueMap.get("CNTSAIKEN"));     //再検査者数 2003/11/27
                        //svf.VrsOut("CAUTIONS3"     , (String) valueMap.get(""));            //要注意視力者数
                        svf.VrsOut("CORRECTED1"     , (String) valueMap.get("ALL_CNT2P"));    //矯正視力者の割合
                        svf.VrsOut("CORRECTED2"     , (String) valueMap.get("ALL_CNT2"));     //矯正視力者数
                    }
                }
                svf.VrEndRecord();
                nonedata = true;
            }
        } catch (Exception ex) {
            log.error("set_detail2 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

    }  //set_detail2の括り

    /*------------------------------------*
     * 尿検査の統計　SVF出力               *
     *------------------------------------*/
    private void set_detail3(final Param param) {
    	final StringBuffer sql = new StringBuffer();
    	sql.append("SELECT ");
    	sql.append(       "GRADE,");
    	sql.append(       "SEX,");
    	sql.append(       "ALL_CNT1,");
    	sql.append(       "ALL_CNT2,");
    	sql.append(       "CASE WHEN ALL_CNT1=0 THEN 0 ELSE ");
    	sql.append(           "DECIMAL(ROUND(FLOAT(ALL_CNT2)/FLOAT(ALL_CNT1)*100,1),5,1) END AS ALL_CNT2P,");
    	sql.append(       "NEGATIVE,");
    	sql.append(       "CASE WHEN ALL_CNT2=0 THEN 0 ELSE ");
    	sql.append(           "DECIMAL(ROUND(FLOAT(NEGATIVE)/FLOAT(ALL_CNT2)*100,1),5,1) END AS NEGATIVEP,");
    	sql.append(       "POSITIVE,");
    	sql.append(       "CASE WHEN ALL_CNT2=0 THEN 0 ELSE ");
    	sql.append(           "DECIMAL(ROUND(FLOAT(POSITIVE)/FLOAT(ALL_CNT2)*100,1),5,1) END AS POSITIVEP,");
    	sql.append(       "POSITIVE_1,");
    	sql.append(       "POSITIVE_2,");
    	sql.append(       "POSITIVE_3,");
    	sql.append(       "ALL_CNT1-ALL_CNT2 AS ALL_CNT3 ");
    	sql.append(   "FROM ");
    	sql.append(       "(");
    	sql.append(           "SELECT ");
    	sql.append(               "VALUE(T2.GRADE,'9999') AS GRADE,");
    	sql.append(               "VALUE(T3.SEX,'3') AS SEX,");
    	sql.append(               "SUM(1) AS ALL_CNT1,");
    	sql.append(               "SUM(CASE WHEN T1.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS ALL_CNT2,");
    	sql.append(               "SUM(CASE WHEN T1.NEGATIVE='TRUE' THEN 1 ELSE 0 END) AS NEGATIVE,");
    	sql.append(               "SUM(CASE WHEN T1.POSITIVE='TRUE' THEN 1 ELSE 0 END) AS POSITIVE,");
    	if (param._isMieken || param._isFukuiken) {
    		sql.append(               "SUM(CASE WHEN T1.ALBUMINURIA1CD >= '04' THEN 1 ELSE 0 END) AS POSITIVE_1,");
    		sql.append(               "SUM(CASE WHEN T1.URICSUGAR1CD   >= '04' THEN 1 ELSE 0 END) AS POSITIVE_2,");
    		sql.append(               "SUM(CASE WHEN T1.URICBLEED1CD   >= '04' THEN 1 ELSE 0 END) AS POSITIVE_3 ");
    	} else if (param._isKumamoto) {
    		sql.append(               "SUM(CASE WHEN T1.ALBUMINURIA1CD >= '04' AND T1.ALBUMINURIA1CD <> '98' THEN 1 ELSE 0 END) AS POSITIVE_1,");
    		sql.append(               "SUM(CASE WHEN T1.URICSUGAR1CD   >= '04' AND T1.URICSUGAR1CD   <> '98' THEN 1 ELSE 0 END) AS POSITIVE_2,");
    		sql.append(               "SUM(CASE WHEN T1.URICBLEED1CD   >= '04' AND T1.URICBLEED1CD   <> '98' THEN 1 ELSE 0 END) AS POSITIVE_3 ");
    	} else {
    		sql.append(               "SUM(CASE WHEN T1.ALBUMINURIA1CD >= '02' AND T1.ALBUMINURIA1CD <> '05' THEN 1 ELSE 0 END) AS POSITIVE_1,");
    		sql.append(               "SUM(CASE WHEN T1.URICSUGAR1CD   >= '02' AND T1.URICSUGAR1CD   <> '05' THEN 1 ELSE 0 END) AS POSITIVE_2,");
    		sql.append(               "SUM(CASE WHEN T1.URICBLEED1CD   >= '02' AND T1.URICBLEED1CD   <> '05' THEN 1 ELSE 0 END) AS POSITIVE_3 ");
    	}
    	sql.append(           "FROM ");
    	sql.append(               "(");
    	sql.append(                   "SELECT ");
    	sql.append(                       "T1.SCHREGNO,");
    	sql.append(                       "MAX(T1.GRADE) AS GRADE ");
    	sql.append(                   "FROM ");
    	sql.append(                       "SCHREG_REGD_DAT T1 ");
    	sql.append(                       "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND = '" + param._schoolKind + "' ");
    	sql.append(                   "WHERE ");
    	sql.append(                       "T1.YEAR = '" +  param._year + "' ");
    	sql.append(                   "GROUP BY ");
    	sql.append(                       "T1.SCHREGNO ");
    	sql.append(               ") T2 ");

    	sql.append(               "INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T2.SCHREGNO ");

    	sql.append(               "LEFT JOIN(");
    	sql.append(                   "SELECT ");
    	sql.append(                       "SCHREGNO,");
    	sql.append(                       "ALBUMINURIA1CD,");
    	sql.append(                       "URICSUGAR1CD,");
    	sql.append(                       "URICBLEED1CD,");
    	if (param._isMieken || param._isFukuiken) {
    		sql.append(                       "CASE WHEN ALBUMINURIA1CD < '04' ");
    		sql.append(                               "AND URICSUGAR1CD < '04' ");
    		sql.append(                               "AND URICBLEED1CD < '04' THEN 'TRUE' ELSE 'FALSE' END AS NEGATIVE,");
    		sql.append(                       "CASE WHEN (ALBUMINURIA1CD >= '04') ");
    		sql.append(                               "OR (URICSUGAR1CD  >= '04') ");
    		sql.append(                               "OR (URICBLEED1CD  >= '04') THEN 'TRUE' ELSE 'FALSE' END AS POSITIVE ");
    	} else if (param._isKumamoto) {
    		sql.append(                       "CASE WHEN ALBUMINURIA1CD < '04' ");
    		sql.append(                               "AND URICSUGAR1CD < '04' ");
    		sql.append(                               "AND URICBLEED1CD < '04' THEN 'TRUE' ELSE 'FALSE' END AS NEGATIVE,");
    		sql.append(                       "CASE WHEN (ALBUMINURIA1CD >= '04' AND ALBUMINURIA1CD <> '98') ");
    		sql.append(                               "OR (URICSUGAR1CD  >= '04' AND URICSUGAR1CD   <> '98') ");
    		sql.append(                               "OR (URICBLEED1CD  >= '04' AND URICBLEED1CD   <> '98') THEN 'TRUE' ELSE 'FALSE' END AS POSITIVE ");
    	} else {
    		sql.append(                       "CASE WHEN (ALBUMINURIA1CD ='01' OR ALBUMINURIA1CD ='05') ");
    		sql.append(                               "AND (URICSUGAR1CD ='01' OR URICSUGAR1CD   ='05') ");
    		sql.append(                               "AND (URICBLEED1CD ='01' OR URICBLEED1CD   ='05') THEN 'TRUE' ELSE 'FALSE' END AS NEGATIVE,");
    		sql.append(                       "CASE WHEN (ALBUMINURIA1CD >= '02' AND ALBUMINURIA1CD <> '05') ");
    		sql.append(                               "OR (URICSUGAR1CD  >= '02' AND URICSUGAR1CD   <> '05') ");
    		sql.append(                               "OR (URICBLEED1CD  >= '02' AND URICBLEED1CD   <> '05') THEN 'TRUE' ELSE 'FALSE' END AS POSITIVE ");
    	}
    	sql.append(                   "FROM ");
    	sql.append(                       "MEDEXAM_DET_DAT ");
    	sql.append(                   "WHERE ");
    	sql.append(                           "YEAR = '" +  param._year + "' ");
    	if (param._isFukuiken) {
    	    sql.append(                       "AND (ALBUMINURIA1CD >= '02' ");
    	    sql.append(                               "OR URICSUGAR1CD >= '02' ");
    	    sql.append(                               "OR URICBLEED1CD >= '02')");
    	} else {
    	    sql.append(                       "AND (ALBUMINURIA1CD >= '01' ");
    	    sql.append(                               "OR URICSUGAR1CD >= '01' ");
    	    sql.append(                               "OR URICBLEED1CD >= '01')");
    	}
    	sql.append(               ") T1  ON T1.SCHREGNO = T2.SCHREGNO ");

    	sql.append(           "GROUP BY ");
    	sql.append(           "GROUPING SETS ");
    	sql.append(               "((T2.GRADE,T3.SEX),(T3.SEX),())");
    	sql.append(       ") T1 ");

    	sql.append(    "ORDER BY ");
    	sql.append(       "T1.GRADE,");
    	sql.append(       "T1.SEX");

    	PreparedStatement ps = null;
        ResultSet rs = null;
        try {

            log.info("set_detail3 sql = " + sql.toString());
            ps = db2.prepareStatement(sql.toString());
            rs = ps.executeQuery();

            final Map gradeMap = new HashMap();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                if (null == gradeMap.get(grade)) {
                    gradeMap.put(grade, new HashMap());
                }
                final Map m = (Map) gradeMap.get(grade);
                m.put(rs.getString("SEX"), resultSetToMap(rs));
            }

            log.info(" gradeMap key = " + gradeMap.keySet());

            /** SVF-formへデータを出力 **/
            svf.VrSetForm("KNJF070_2.frm", 4);        //svf-form
            svf.VrsOut("NENDO"    , param._nendo);
            svf.VrsOut("TODAY"    , param._ctrlDateFormat);
            svf.VrsOut("SCHOOL_KIND_NAME" , param._schoolKindName);

            for (final Iterator it = param._gradeAgeList.iterator(); it.hasNext();) {
                final GradeAge g = (GradeAge) it.next();
                svf.VrsOut("GRADE", g._name);

                final Map keyMap = (Map) gradeMap.get(g._grade);
                if (null != keyMap) {
                    for (final Iterator sit = keyMap.keySet().iterator(); sit.hasNext();) {
                        final String key = (String) sit.next();
                        final Map valueMap = (Map) keyMap.get(key);
                        final String sfx = "1".equals(key) ? "_M" : "2".equals(key) ? "_F" : null;
                        svf.VrsOut("URINALYSIS1" + sfx, (String) valueMap.get("ALL_CNT2"));        //受験者数
                        svf.VrsOut("URINALYSIS2" + sfx, (String) valueMap.get("ALL_CNT2P"));    //受験者率
                        svf.VrsOut("NORMAL1"     + sfx, (String) valueMap.get("NEGATIVE"));        //正常者数
                        svf.VrsOut("NORMAL2"     + sfx, (String) valueMap.get("NEGATIVEP"));    //正常者率
                        svf.VrsOut("POSITIVE1"   + sfx, (String) valueMap.get("POSITIVE"));        //陽性者数
                        svf.VrsOut("POSITIVE2"   + sfx, (String) valueMap.get("POSITIVEP"));    //陽性者率
                        svf.VrsOut("ALBUMIN"     + sfx, (String) valueMap.get("POSITIVE_1"));        //蛋白
                        svf.VrsOut("BLOOD"       + sfx, (String) valueMap.get("POSITIVE_2"));        //潜血
                        svf.VrsOut("SUGER"       + sfx, (String) valueMap.get("POSITIVE_3"));        //糖
                    }
                }
                svf.VrEndRecord();
            }

            final Map keyMap = (Map) gradeMap.get("9999");
            if (null != keyMap) {
                for (final Iterator sit = keyMap.keySet().iterator(); sit.hasNext();) {
                    final String key = (String) sit.next();
                    final Map valueMap = (Map) keyMap.get(key);
                    final String sfx = "1".equals(key) ? "_M" : "2".equals(key) ? "_F" : "3".equals(key) ? "" : null;
                    svf.VrsOut("TOTAL_URINALYSIS1" + sfx, (String) valueMap.get("ALL_CNT2"));        //受験者数
                    svf.VrsOut("TOTAL_URINALYSIS2" + sfx, (String) valueMap.get("ALL_CNT2P"));    //受験者率
                    svf.VrsOut("TOTAL_NORMAL1"     + sfx, (String) valueMap.get("NEGATIVE"));        //正常者数
                    svf.VrsOut("TOTAL_NORMAL2"     + sfx, (String) valueMap.get("NEGATIVEP"));    //正常者率
                    svf.VrsOut("TOTAL_POSITIVE1"   + sfx, (String) valueMap.get("POSITIVE"));        //陽性者数
                    svf.VrsOut("TOTAL_POSITIVE2"   + sfx, (String) valueMap.get("POSITIVEP"));    //陽性者率
                    svf.VrsOut("TOTAL_ALBUMIN"     + sfx, (String) valueMap.get("POSITIVE_1"));        //蛋白
                    svf.VrsOut("TOTAL_BLOOD"       + sfx, (String) valueMap.get("POSITIVE_2"));        //潜血
                    svf.VrsOut("TOTAL_SUGER"       + sfx, (String) valueMap.get("POSITIVE_3"));        //糖

                    if ("3".equals(key)) {
                        svf.VrsOut("URINE_RATE1"   , (String) valueMap.get("ALL_CNT2P"));    //実施率
                        svf.VrsOut("URINE_RATE2"   , (String) valueMap.get("ALL_CNT2"));        //実施人数
                        svf.VrsOut("POSITIVE_RATE1", (String) valueMap.get("POSITIVEP"));    //陽性率
                        svf.VrsOut("POSITIVE_RATE2", (String) valueMap.get("POSITIVE"));        //陽性人数
                        svf.VrsOut("NORMALNO"      , (String) valueMap.get("NEGATIVE"));        //異常なし人数
                        svf.VrsOut("YETNO"         , (String) valueMap.get("ALL_CNT3"));        //未受験者数
                    }
                }
                svf.VrEndRecord();
                nonedata = true;
            }
        } catch (Exception ex) {
            log.error("set_detail3 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

    }  //set_detail3の括り

    /*------------------------------------*
     * 貧血検査の統計　SVF出力               *
     *------------------------------------*/
    private void set_detail4(final Param param)
                     throws ServletException, IOException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = new String();
            sql = "SELECT "
                    + "GRADE,"
                    + "SEX,"
                    + "ALL_CNT1,"
                    + "ALL_CNT2,"
                    + "CASE WHEN ALL_CNT1=0 THEN 0 ELSE "
                        + "DECIMAL(ROUND(FLOAT(ALL_CNT2)/FLOAT(ALL_CNT1)*100,1),5,1) END AS ALL_CNT2P,"
                    + "NORMAL,"
                    + "CASE WHEN ALL_CNT2=0 THEN 0 ELSE "
                        + "DECIMAL(ROUND(FLOAT(NORMAL)/FLOAT(ALL_CNT2)*100,1),5,1) END AS NORMALP,"
                    + "ABNORMAL1,"
                    + "CASE WHEN ALL_CNT2=0 THEN 0 ELSE "
                        + "DECIMAL(ROUND(FLOAT(ABNORMAL1)/FLOAT(ALL_CNT2)*100,1),5,1) END AS ABNORMAL1P,"
                    + "ABNORMAL2,"
                    + "CASE WHEN ALL_CNT2=0 THEN 0 ELSE "
                        + "DECIMAL(ROUND(FLOAT(ABNORMAL2)/FLOAT(ALL_CNT2)*100,1),5,1) END AS ABNORMAL2P,"
                    + "ABNORMAL1+ABNORMAL2 AS ALL_CNT3,"
                    + "CASE WHEN ALL_CNT2=0 THEN 0 ELSE "
                        + "DECIMAL(ROUND(FLOAT(ABNORMAL1+ABNORMAL2)/FLOAT(ALL_CNT2)*100,1),5,1) END AS ALL_CNT3P "
                + "FROM "
                    + "("
                        + "SELECT "
                            + "VALUE(T2.GRADE,'9999') AS GRADE,"
                            + "VALUE(T3.SEX,'3') AS SEX,"
                            + "SUM(1) AS ALL_CNT1,"
                            + "SUM(CASE WHEN T1.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS ALL_CNT2,"
                            + "SUM(CASE WHEN T3.SEX='1' AND T1.HEMOGLOBIN>=13.0 THEN 1 "
                                        + "WHEN T3.SEX='2' AND T1.HEMOGLOBIN>=11.5 THEN 1 "
                                                                                + "ELSE 0 END) AS NORMAL,"
                            + "SUM(CASE WHEN T3.SEX='1' AND T1.HEMOGLOBIN BETWEEN 12.0 AND 12.9 THEN 1 "
                                        + "WHEN T3.SEX='2' AND T1.HEMOGLOBIN BETWEEN 10.5 AND 11.4 THEN 1 "
                                                                                + "ELSE 0 END) AS ABNORMAL1,"
                            + "SUM(CASE WHEN T3.SEX='1' AND T1.HEMOGLOBIN>0 AND T1.HEMOGLOBIN<=11.9 THEN 1 "
                                        + "WHEN T3.SEX='2' AND T1.HEMOGLOBIN>0 AND T1.HEMOGLOBIN<=10.4 THEN 1 "
                                                                                + "ELSE 0 END) AS ABNORMAL2 "
                        + "FROM "
                            + "("
                                + "SELECT "
                                    + "T1.SCHREGNO,"
                                    + "MAX(T1.GRADE) AS GRADE "
                                + "FROM "
                                    + "SCHREG_REGD_DAT T1 "
                                    + "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND = '" + param._schoolKind + "' "
                                + "WHERE "
                                    + "T1.YEAR = '" +  param._year + "' "
                                + "GROUP BY "
                                    + "T1.SCHREGNO "
                            + ") T2 "

                            + "INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T2.SCHREGNO "

                            + "LEFT JOIN("
                                + "SELECT "
                                    + "SCHREGNO,"
                                    + "HEMOGLOBIN "
                                + "FROM "
                                    + "MEDEXAM_DET_DAT "
                                + "WHERE "
                                        + "YEAR = '" +  param._year + "' "
                                    + "AND HEMOGLOBIN > 0 "
                            + ") T1  ON T1.SCHREGNO = T2.SCHREGNO "

                        + "GROUP BY "
                        + "GROUPING SETS "
                            + "((T2.GRADE,T3.SEX),(T3.SEX),())"
                    + ") T1 "

                + "ORDER BY "
                    + "T1.GRADE,"
                    + "T1.SEX";

            //log.debug("set_detail4 sql="+sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final Map gradeMap = new HashMap();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                if (null == gradeMap.get(grade)) {
                    gradeMap.put(grade, new HashMap());
                }
                final Map m = (Map) gradeMap.get(grade);
                m.put(rs.getString("SEX"), resultSetToMap(rs));
            }

            /** SVF-formへデータを出力 **/
            if (param._isFukuiken) {
                svf.VrSetForm("KNJF070_3_2.frm", 4);        //福井県用フォーム
            } else {
                svf.VrSetForm("KNJF070_3.frm", 4);        //svf-form
            }
            svf.VrsOut("NENDO"    , param._nendo);
            svf.VrsOut("TODAY"    , param._ctrlDateFormat);
            svf.VrsOut("SCHOOL_KIND_NAME" , param._schoolKindName);

            for (final Iterator it = param._gradeAgeList.iterator(); it.hasNext();) {
                final GradeAge g = (GradeAge) it.next();
                svf.VrsOut("GRADE", g._name);

                final Map keyMap = (Map) gradeMap.get(g._grade);
                if (null != keyMap) {
                    for (final Iterator sit = keyMap.keySet().iterator(); sit.hasNext();) {
                        final String key = (String) sit.next();
                        final Map valueMap = (Map) keyMap.get(key);
                        final String sfx = "1".equals(key) ? "_M" : "2".equals(key) ? "_F" : null;
                        svf.VrsOut("ANEMIA1"    + sfx, (String) valueMap.get("ALL_CNT2"));        //受験者数
                        svf.VrsOut("ANEMIA2"    + sfx, (String) valueMap.get("ALL_CNT2P"));    //受験者率
                        svf.VrsOut("NORMAL1"    + sfx, (String) valueMap.get("NORMAL"));            //正常者数
                        svf.VrsOut("NORMAL2"    + sfx, (String) valueMap.get("NORMALP"));        //正常者率
                        svf.VrsOut("MEAL1"      + sfx, (String) valueMap.get("ABNORMAL1"));        //要食事指導者数
                        svf.VrsOut("MEAL2"      + sfx, (String) valueMap.get("ABNORMAL1P"));    //要食事指導者率
                        svf.VrsOut("TREATMENT1" + sfx, (String) valueMap.get("ABNORMAL2"));        //要治療者数
                        svf.VrsOut("TREATMENT2" + sfx, (String) valueMap.get("ABNORMAL2P"));    //要治療者率
                    }
                }
                svf.VrEndRecord();
            }

            final Map keyMap = (Map) gradeMap.get("9999");
            if (null != keyMap) {
                for (final Iterator sit = keyMap.keySet().iterator(); sit.hasNext();) {
                    final String key = (String) sit.next();
                    final Map valueMap = (Map) keyMap.get(key);
                    final String sfx = "1".equals(key) ? "_M" : "2".equals(key) ? "_F" : "3".equals(key) ? "" : null;
                    svf.VrsOut("TOTAL_ANEMIA1"    + sfx, (String) valueMap.get("ALL_CNT2"));        //受験者数
                    svf.VrsOut("TOTAL_ANEMIA2"    + sfx, (String) valueMap.get("ALL_CNT2P"));    //受験者率
                    svf.VrsOut("TOTAL_NORMAL1"    + sfx, (String) valueMap.get("NORMAL"));            //正常者数
                    svf.VrsOut("TOTAL_NORMAL2"    + sfx, (String) valueMap.get("NORMALP"));        //正常者率
                    svf.VrsOut("TOTAL_MEAL1"      + sfx, (String) valueMap.get("ABNORMAL1"));        //要食事指導者数
                    svf.VrsOut("TOTAL_MEAL2"      + sfx, (String) valueMap.get("ABNORMAL1P"));    //要食事指導者率
                    svf.VrsOut("TOTAL_TREATMENT1" + sfx, (String) valueMap.get("ABNORMAL2"));        //要治療者数
                    svf.VrsOut("TOTAL_TREATMENT2" + sfx, (String) valueMap.get("ABNORMAL2P"));    //要治療者率

                    if ("3".equals(key)) {
                        svf.VrsOut("POSITIVE2", (String) valueMap.get("ALL_CNT3P"));    //陽性率
                        svf.VrsOut("POSITIVE1", (String) valueMap.get("ALL_CNT3"));        //陽性人数
                    }
                }
                svf.VrEndRecord();
                nonedata = true;
            }
        } catch (Exception ex) {
            log.error("set_detail4 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

    }  //set_detail4の括り

    /*------------------------------------*
     * 歯科検査の統計　SVF出力               *
     *------------------------------------*/
    private void set_detail5(final Param param)
                     throws ServletException, IOException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = new String();
            sql = "SELECT "
                    + "GRADE,"
                    + "SEX,"
                    + "ALL_CNT1,"
                    + "ALL_CNT2,"
                    + "CASE WHEN ALL_CNT1=0 THEN 0 ELSE "
                        + "DECIMAL(ROUND(FLOAT(ALL_CNT2)/FLOAT(ALL_CNT1)*100,1),5,1) END AS ALL_CNT2P,"
                    + "CNT_USI_NASI,"
                        + "CASE WHEN ALL_CNT2=0 THEN 0 ELSE "
                            + "DECIMAL(ROUND(FLOAT(CNT_USI_NASI)/FLOAT(ALL_CNT2)*100,1),5,1) END AS CNT_USI_NASI_P,"
                    + "CNT_USI_ARI,"
                        + "CASE WHEN ALL_CNT2=0 THEN 0 ELSE "
                            + "DECIMAL(ROUND(FLOAT(CNT_USI_ARI)/FLOAT(ALL_CNT2)*100,1),5,1) END AS CNT_USI_ARI_P,"
                    + "CNT_SYOTI_KANRYO,"
                        + "CASE WHEN ALL_CNT2=0 THEN 0 ELSE "
                            + "DECIMAL(ROUND(FLOAT(CNT_SYOTI_KANRYO)/FLOAT(ALL_CNT2)*100,1),5,1) END AS CNT_SYOTI_KANRYO_P,"
                    + "CNT_YOU_TIRYOU,"
                        + "CASE WHEN ALL_CNT2=0 THEN 0 ELSE "
                            + "DECIMAL(ROUND(FLOAT(CNT_YOU_TIRYOU)/FLOAT(ALL_CNT2)*100,1),5,1) END AS CNT_YOU_TIRYOU_P,"
                    + "CNT_YOU_TYUI,"
                        + "CASE WHEN ALL_CNT2=0 THEN 0 ELSE "
                            + "DECIMAL(ROUND(FLOAT(CNT_YOU_TYUI)/FLOAT(ALL_CNT2)*100,1),5,1) END AS CNT_YOU_TYUI_P,"
                    + "CNT_KANSATSU,"
                        + "CASE WHEN ALL_CNT2=0 THEN 0 ELSE "
                            + "DECIMAL(ROUND(FLOAT(CNT_KANSATSU)/FLOAT(ALL_CNT2)*100,1),5,1) END AS CNT_KANSATSU_P,"
                    + "CNT_SOUSITSU,"
                        + "CASE WHEN ALL_CNT2=0 THEN 0 ELSE "
                            + "DECIMAL(ROUND(FLOAT(CNT_SOUSITSU)/FLOAT(ALL_CNT2)*100,1),5,1) END AS CNT_SOUSITSU_P,"

                    + "CNT_OTHERDISEASE,"
                    + "CNT_PLAQUE,"
                    + "CNT_GUM,"
                    + "CNT_JAW,"
                    + "CNT_JAW2,"
                    + "SUM_TREATEDADULT,"
                        + "CASE WHEN ALL_CNT2=0 THEN 0 ELSE "
                            + "DECIMAL(ROUND(FLOAT(SUM_TREATEDADULT)/FLOAT(ALL_CNT2),1),5,1) END AS AVG_TREATEDADULT,"
                    + "SUM_REMAINADULT,"
                        + "CASE WHEN ALL_CNT2=0 THEN 0 ELSE "
                            + "DECIMAL(ROUND(FLOAT(SUM_REMAINADULT)/FLOAT(ALL_CNT2),1),5,1) END AS AVG_REMAINADULT,"
                    + "SUM_BRACKBABY,"
                        + "CASE WHEN ALL_CNT2=0 THEN 0 ELSE "
                            + "DECIMAL(ROUND(FLOAT(SUM_BRACKBABY)/FLOAT(ALL_CNT2),1),5,1) END AS AVG_BRACKBABY,"
                    + "SUM_BRACKADULT,"
                        + "CASE WHEN ALL_CNT2=0 THEN 0 ELSE "
                            + "DECIMAL(ROUND(FLOAT(SUM_BRACKADULT)/FLOAT(ALL_CNT2),1),5,1) END AS AVG_BRACKADULT,"
                    + "SUM_LOST,"
                        + "CASE WHEN ALL_CNT2=0 THEN 0 ELSE "
                            + "DECIMAL(ROUND(FLOAT(SUM_LOST)/FLOAT(ALL_CNT2),1),5,1) END AS AVG_LOST,"
                    + "CNT_OTHERDISEASE + CNT_PLAQUE + CNT_GUM + CNT_JAW + CNT_JAW2 as cnt6789 "
                + "FROM "
                    + "("
                        + "SELECT "
                            + "VALUE(T2.GRADE,'9999') AS GRADE,"
                            + "VALUE(T3.SEX,'3') AS SEX,"
                            + "SUM(1) AS ALL_CNT1,"
                            + "SUM(CASE WHEN T1.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS ALL_CNT2,"
                            + "SUM(CASE WHEN REMAINADULTTOOTH=0 AND TREATEDADULTTOOTH=0 AND REMAINBABYTOOTH=0 AND TREATEDBABYTOOTH=0 THEN 1 "
                                                                                + "ELSE 0 END)         AS CNT_USI_NASI,"
                            + "SUM(CASE WHEN VALUE(REMAINADULTTOOTH,0)>0 OR VALUE(TREATEDADULTTOOTH,0)>0 OR VALUE(REMAINBABYTOOTH,0)>0 OR VALUE(TREATEDBABYTOOTH,0)>0 THEN 1 "
                                                                                + "ELSE 0 END)         AS CNT_USI_ARI,"
                            + "SUM(CASE WHEN VALUE(REMAINADULTTOOTH,0)>0 OR VALUE(REMAINBABYTOOTH,0)>0 THEN 0 "
                                     + "WHEN VALUE(TREATEDADULTTOOTH,0)>0 OR VALUE(TREATEDBABYTOOTH,0)>0 THEN 1 "
                                                                                + "ELSE 0 END)      AS CNT_SYOTI_KANRYO,"
                            + "SUM(CASE WHEN VALUE(REMAINADULTTOOTH,0)>0 OR VALUE(REMAINBABYTOOTH,0)>0 THEN 1 ELSE 0 END) AS CNT_YOU_TIRYOU,"
                            + "SUM(CASE WHEN VALUE(BRACK_BABYTOOTH,0)>0  THEN 1 ELSE 0 END)      AS CNT_YOU_TYUI,"
                            + "SUM(CASE WHEN VALUE(BRACK_ADULTTOOTH,0)>0 THEN 1 ELSE 0 END)      AS CNT_KANSATSU,"
                            + "SUM(CASE WHEN VALUE(LOSTADULTTOOTH,0)>0    THEN 1 ELSE 0 END)      AS CNT_SOUSITSU,"
                            + "SUM(CASE WHEN VALUE(TREATEDADULTTOOTH,0)>0 THEN 1 ELSE 0 END)         AS CNT4,"
                            + "SUM(CASE WHEN VALUE(REMAINADULTTOOTH,0)>0 THEN 1 ELSE 0 END)         AS CNT5,"
                            + "SUM(CASE WHEN VALUE(OTHERDISEASECD,'00')    >'01' THEN 1 ELSE 0 END)     AS CNT_OTHERDISEASE,"
                            + "SUM(CASE WHEN VALUE(PLAQUECD,'00')         >'01' THEN 1 ELSE 0 END)     AS CNT_PLAQUE,"
                            + "SUM(CASE WHEN VALUE(GUMCD,'00')             >'01' THEN 1 ELSE 0 END)     AS CNT_GUM,"
                            + "SUM(CASE WHEN VALUE(JAWS_JOINTCD,'00')    >'01' THEN 1 ELSE 0 END)     AS CNT_JAW,"
                            + "SUM(CASE WHEN VALUE(JAWS_JOINTCD2,'00')  >'01' THEN 1 ELSE 0 END)    AS CNT_JAW2,"
                            + "SUM(FLOAT(VALUE(TREATEDADULTTOOTH,0)))     AS SUM_TREATEDADULT,"
                            + "SUM(FLOAT(VALUE(REMAINADULTTOOTH,0)))     AS SUM_REMAINADULT,"
                            + "SUM(FLOAT(VALUE(BRACK_BABYTOOTH, 0)))    AS SUM_BRACKBABY, "
                            + "SUM(FLOAT(VALUE(BRACK_ADULTTOOTH, 0)))   AS SUM_BRACKADULT, "
                            + "SUM(FLOAT(VALUE(LOSTADULTTOOTH, 0)))     AS SUM_LOST "
                        + "FROM "
                            + "("
                                + "SELECT "
                                    + "T1.SCHREGNO,"
                                    + "MAX(T1.GRADE) AS GRADE "
                                + "FROM "
                                    + "SCHREG_REGD_DAT T1 "
                                    + "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND = '" + param._schoolKind + "' "
                                + "WHERE "
                                    + "T1.YEAR = '" +  param._year + "' "
                                + "GROUP BY "
                                    + "T1.SCHREGNO "
                            + ") T2 "

                            + "INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T2.SCHREGNO "

                            + "LEFT JOIN("
                                + "SELECT "
                                    + "SCHREGNO,"
                                    + "REMAINADULTTOOTH,"
                                    + "REMAINBABYTOOTH,"
                                    + "TREATEDADULTTOOTH,"
                                    + "TREATEDBABYTOOTH,"
                                    + "BRACK_ADULTTOOTH,"
                                    + "BRACK_BABYTOOTH,"
                                    + "OTHERDISEASECD,"
                                    + "PLAQUECD,"
                                    + "GUMCD,"
                                    + "JAWS_JOINTCD,"
                                    + "JAWS_JOINTCD2,"
                                    + "LOSTADULTTOOTH "
                                + "FROM "
                                    + "MEDEXAM_TOOTH_DAT "
                                + "WHERE "
                                        + "YEAR = '" +  param._year + "' "
                                    + "AND (ADULTTOOTH IS NOT NULL "
                                            + "OR REMAINADULTTOOTH IS NOT NULL "
                                            + "OR REMAINBABYTOOTH IS NOT NULL "
                                            + "OR TREATEDADULTTOOTH IS NOT NULL "
                                            + "OR TREATEDBABYTOOTH IS NOT NULL "
                                            + "OR BRACK_ADULTTOOTH IS NOT NULL "
                                            + "OR BRACK_BABYTOOTH IS NOT NULL "
                                            + "OR LOSTADULTTOOTH IS NOT NULL) "
                            + ") T1  ON T1.SCHREGNO = T2.SCHREGNO "

                        + "GROUP BY "
                        + "GROUPING SETS "
                            + "((T2.GRADE,T3.SEX),(T3.SEX),())"
                    + ") T1 "

                + "ORDER BY "
                    + "T1.GRADE,"
                    + "T1.SEX,"
                    + "T1.ALL_CNT1";
            //指定年齢外の人数と合計の列が前後しないよう、人数でソートして合計が最後の列に来るようにしておく。

            //log.debug("set_detail5 sql="+sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final Map gradeMap = new HashMap();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                if (null == gradeMap.get(grade)) {
                    gradeMap.put(grade, new HashMap());
                }
                final Map m = (Map) gradeMap.get(grade);
                m.put(rs.getString("SEX"), resultSetToMap(rs));
            }

            /** SVF-formへデータを出力 **/
            svf.VrSetForm("KNJF070_4.frm", 4);        //svf-form
            svf.VrsOut("NENDO"    , param._nendo);
            svf.VrsOut("TODAY"    , param._ctrlDateFormat);
            svf.VrsOut("SCHOOL_KIND_NAME" , param._schoolKindName);

            for (final Iterator it = param._gradeAgeList.iterator(); it.hasNext();) {
                final GradeAge g = (GradeAge) it.next();
                svf.VrsOut("GRADE", g._name);

                final Map keyMap = (Map) gradeMap.get(g._grade);
                if (null != keyMap) {
                    for (final Iterator sit = keyMap.keySet().iterator(); sit.hasNext();) {
                        final String key = (String) sit.next();
                        final Map valueMap = (Map) keyMap.get(key);
                        final String sfx = "1".equals(key) ? "_M" : "2".equals(key) ? "_F" : null;
                        svf.VrsOut("DENTAL1"           + sfx, (String) valueMap.get("ALL_CNT2"));    //受験者数
                        svf.VrsOut("DENTAL2"           + sfx, (String) valueMap.get("ALL_CNT2P"));//受験者率
                        svf.VrsOut("TEETH1"            + sfx, (String) valueMap.get("CNT_USI_NASI"));        //う歯のない者数
                        svf.VrsOut("TEETH2"            + sfx, (String) valueMap.get("CNT_USI_NASI_P"));    //う歯のない者率
                        svf.VrsOut("DECAYED1"          + sfx, (String) valueMap.get("CNT_USI_ARI"));        //う歯保有者数
                        svf.VrsOut("DECAYED2"          + sfx, (String) valueMap.get("CNT_USI_ARI_P"));    //う歯保有者率
                        svf.VrsOut("BRACK_BABYTOOTH1"  + sfx, (String) valueMap.get("CNT_YOU_TIRYOU"));       //要治療う歯あり
                        svf.VrsOut("BRACK_BABYTOOTH2"  + sfx, (String) valueMap.get("CNT_YOU_TIRYOU_P"));  //要治療う歯あり率
                        svf.VrsOut("TREATEDBABYTOOTH1" + sfx, (String) valueMap.get("CNT_SYOTI_KANRYO"));     //う歯処置完了
                        svf.VrsOut("TREATEDBABYTOOTH2" + sfx, (String) valueMap.get("CNT_SYOTI_KANRYO_P"));//う歯処置完了率
                        svf.VrsOut("BRACK_BABYTOOTH3"  + sfx, (String) valueMap.get("CNT_YOU_TYUI"));         //要注意乳歯あり
                        svf.VrsOut("BRACK_BABYTOOTH4"  + sfx, (String) valueMap.get("CNT_YOU_TYUI_P"));    //要注意乳歯あり率
                        svf.VrsOut("BRACK_ADULTTOOTH1" + sfx, (String) valueMap.get("CNT_KANSATSU"));         //要観察歯あり
                        svf.VrsOut("BRACK_ADULTTOOTH2" + sfx, (String) valueMap.get("CNT_KANSATSU_P"));    //要観察歯あり率
                        svf.VrsOut("LOSTADULTTOOTH1"   + sfx, (String) valueMap.get("CNT_SOUSITSU"));         //喪失歯あり
                        svf.VrsOut("LOSTADULTTOOTH2"   + sfx, (String) valueMap.get("CNT_SOUSITSU_P"));    //喪失歯あり率
                    }
                }
                svf.VrEndRecord();
            }

            final Map keyMap = (Map) gradeMap.get("9999");
            if (null != keyMap) {
                for (final Iterator sit = keyMap.keySet().iterator(); sit.hasNext();) {
                    final String key = (String) sit.next();
                    final Map valueMap = (Map) keyMap.get(key);
                    final String sfx = "1".equals(key) ? "_M" : "2".equals(key) ? "_F" : "3".equals(key) ? "" : null;
                    svf.VrsOut("TOTAL_DENTAL1"           + sfx, (String) valueMap.get("ALL_CNT2"));    //受験者数
                    svf.VrsOut("TOTAL_DENTAL2"           + sfx, (String) valueMap.get("ALL_CNT2P"));//受験者率
                    svf.VrsOut("TOTAL_TEETH1"            + sfx, (String) valueMap.get("CNT_USI_NASI"));        //う歯のない者数
                    svf.VrsOut("TOTAL_TEETH2"            + sfx, (String) valueMap.get("CNT_USI_NASI_P"));    //う歯のない者率
                    svf.VrsOut("TOTAL_DECAYED1"          + sfx, (String) valueMap.get("CNT_USI_ARI"));        //う歯保有者数
                    svf.VrsOut("TOTAL_DECAYED2"          + sfx, (String) valueMap.get("CNT_USI_ARI_P"));    //う歯保有者率
                    svf.VrsOut("TOTAL_BRACK_BABYTOOTH1"  + sfx, (String) valueMap.get("CNT_YOU_TIRYOU"));       //要治療う歯あり
                    svf.VrsOut("TOTAL_BRACK_BABYTOOTH2"  + sfx, (String) valueMap.get("CNT_YOU_TIRYOU_P"));  //要治療う歯あり率
                    svf.VrsOut("TOTAL_TREATEDBABYTOOTH1" + sfx, (String) valueMap.get("CNT_SYOTI_KANRYO"));     //う歯処置完了
                    svf.VrsOut("TOTAL_TREATEDBABYTOOTH2" + sfx, (String) valueMap.get("CNT_SYOTI_KANRYO_P"));//う歯処置完了率
                    svf.VrsOut("TOTAL_BRACK_BABYTOOTH3"  + sfx, (String) valueMap.get("CNT_YOU_TYUI"));         //要注意乳歯あり
                    svf.VrsOut("TOTAL_BRACK_BABYTOOTH4"  + sfx, (String) valueMap.get("CNT_YOU_TYUI_P"));    //要注意乳歯あり率
                    svf.VrsOut("TOTAL_BRACK_ADULTTOOTH1" + sfx, (String) valueMap.get("CNT_KANSATSU"));         //要観察歯あり
                    svf.VrsOut("TOTAL_BRACK_ADULTTOOTH2" + sfx, (String) valueMap.get("CNT_KANSATSU_P"));    //要観察歯あり率
                    svf.VrsOut("TOTAL_LOSTADULTTOOTH1"   + sfx, (String) valueMap.get("CNT_SOUSITSU"));         //喪失歯あり
                    svf.VrsOut("TOTAL_LOSTADULTTOOTH2"   + sfx, (String) valueMap.get("CNT_SOUSITSU_P"));    //喪失歯あり率

                    if ("3".equals(key)) {
                        svf.VrsOut("OTHERS1"         , (String) valueMap.get("CNT_OTHERDISEASE"));  //その他の疾病
                        svf.VrsOut("PLAQUE"         , (String) valueMap.get("CNT_PLAQUE"));           //歯列・咬合
                        svf.VrsOut("GUM"             , (String) valueMap.get("CNT_GUM"));           //顎間接
                        svf.VrsOut("JAWS1"         , (String) valueMap.get("CNT_JAW"));           //歯垢の状態
                        svf.VrsOut("JAWS2"        , (String) valueMap.get("CNT_JAW2"));          //歯肉の状態2
                        svf.VrsOut("OTHERS2"         , (String) valueMap.get("cnt6789"));           //合計
                        svf.VrsOut("AVERAGE1"     , (String) valueMap.get("AVG_REMAINADULT"));   //要治療う歯数
                        svf.VrsOut("AVERAGE2"     , (String) valueMap.get("AVG_TREATEDADULT"));  //処置歯数平均
                        svf.VrsOut("AVERAGE3"     , (String) valueMap.get("AVG_BRACKBABY"));     //要注意乳歯平均
                        svf.VrsOut("AVERAGE4"     , (String) valueMap.get("AVG_BRACKADULT"));    //要観察歯平均
                        svf.VrsOut("AVERAGE5"     , (String) valueMap.get("AVG_LOST"));          //喪失歯数平均

                        svf.VrsOut("TEETH3"      , (String) valueMap.get("CNT_USI_NASI"));               //う歯のない者数
                        svf.VrsOut("TEETH4"      , (String) valueMap.get("CNT_USI_NASI_P"));              //う歯のない者率
                        svf.VrsOut("DECAYED3"   , (String) valueMap.get("CNT_USI_ARI"));                //う歯保有者数
                    }

                }
                svf.VrEndRecord();
                nonedata = true;
            }
        } catch (Exception ex) {
            log.error("set_detail5 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

    }  //set_detail5の括り

    /*------------------------------------*
     * 歯科検査他の統計　SVF出力           *
     *------------------------------------*/
    private void set_detail6(final Param param)
                     throws ServletException, IOException
    {
        String f_name[][] = new String[4][3];

        //項目名称の取得
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String nameCd1 = "F510";
            if (param._isKumamoto) {
                nameCd1 = (2016 > Integer.parseInt(param._year)) ? "F510": "F514";
            }
            String sql = new String();
            sql = "SELECT "
                    + "NAMECD1,"
                    + "NAMECD2,"
                    + "NAME1 "
                + "FROM "
                    + "NAME_MST "
                + "WHERE "
                    + "NAMECD1 = '"+ nameCd1 +"' OR NAMECD1 = 'F511' OR NAMECD1 = 'F513' OR NAMECD1 = 'F520'";

            //log.debug("set_detail6 name_mst sql="+sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                int ia = 0;
                if (rs.getString("NAMECD1").equalsIgnoreCase("F520")) {
                    ia = 1;
                } else if (rs.getString("NAMECD1").equalsIgnoreCase("F511")) {
                    ia = 2;
                } else if (rs.getString("NAMECD1").equalsIgnoreCase("F513")) {
                    ia = 3;
                }
                final int ib = Integer.parseInt(rs.getString("NAMECD2"));
                if (ib > 0 & ib < 4)    {
                    f_name[ia][ib-1] = rs.getString("NAME1");
                }
            }
        } catch (Exception ex) {
            log.error("set_detail6 name_mst read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }


        //データ取得
        try {
            String sql = new String();
            sql = "SELECT "
                    + "VALUE(T2.GRADE,'9999') AS GRADE,"
                    + "VALUE(T3.SEX,'3') AS SEX,"
                    + "SUM(CASE WHEN JAWS_JOINTCD='01' THEN 1 ELSE 0 END)     AS CNT1_1,"
                    + "SUM(CASE WHEN JAWS_JOINTCD='02' THEN 1 ELSE 0 END)     AS CNT1_2,"
                    + "SUM(CASE WHEN JAWS_JOINTCD='03' THEN 1 ELSE 0 END)     AS CNT1_3,"
                    + "SUM(CASE WHEN JAWS_JOINTCD2='01' THEN 1 ELSE 0 END)   AS CNT2_1,"
                    + "SUM(CASE WHEN JAWS_JOINTCD2='02' THEN 1 ELSE 0 END)   AS CNT2_2,"
                    + "SUM(CASE WHEN JAWS_JOINTCD2='03' THEN 1 ELSE 0 END)   AS CNT2_3,"
                    + "SUM(CASE WHEN PLAQUECD='01' THEN 1 ELSE 0 END)     AS CNT3_1,"
                    + "SUM(CASE WHEN PLAQUECD='02' THEN 1 ELSE 0 END)     AS CNT3_2,"
                    + "SUM(CASE WHEN PLAQUECD='03' THEN 1 ELSE 0 END)     AS CNT3_3,"
                    + "SUM(CASE WHEN GUMCD='01' THEN 1 ELSE 0 END)     AS CNT4_1,"
                    + "SUM(CASE WHEN GUMCD='02' THEN 1 ELSE 0 END)     AS CNT4_2,"
                    + "SUM(CASE WHEN GUMCD='03' THEN 1 ELSE 0 END)     AS CNT4_3 "
                + "FROM "
                    + "("
                        + "SELECT "
                            + "T1.SCHREGNO,"
                            + "MAX(T1.GRADE) AS GRADE "
                        + "FROM "
                            + "SCHREG_REGD_DAT T1 "
                            + "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND = '" + param._schoolKind + "' "
                        + "WHERE "
                            + "T1.YEAR = '" +  param._year + "' "
                        + "GROUP BY "
                            + "T1.SCHREGNO "
                    + ") T2 "

                    + "INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T2.SCHREGNO "

                    + "LEFT JOIN("
                        + "SELECT "
                            + "SCHREGNO,"
                            + "JAWS_JOINTCD,"
                            + "JAWS_JOINTCD2,"
                            + "PLAQUECD,"
                            + "GUMCD "
                        + "FROM "
                            + "MEDEXAM_TOOTH_DAT "
                        + "WHERE "
                                + "YEAR = '" +  param._year + "' "
                            + "AND (JAWS_JOINTCD IS NOT NULL "
                                    + "OR JAWS_JOINTCD2 IS NOT NULL "
                                    + "OR PLAQUECD IS NOT NULL "
                                    + "OR GUMCD IS NOT NULL) "
                    + ") T1  ON T1.SCHREGNO = T2.SCHREGNO "

                + "GROUP BY "
                + "GROUPING SETS "
                    + "((T2.GRADE,T3.SEX),(T3.SEX),())"

                + "ORDER BY "
                    + "GRADE,"
                    + "SEX";

            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final Map gradeMap = new HashMap();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                if (null == gradeMap.get(grade)) {
                    gradeMap.put(grade, new HashMap());
                }
                final Map m = (Map) gradeMap.get(grade);
                m.put(rs.getString("SEX"), resultSetToMap(rs));
            }

            /** SVF-formへデータを出力 **/
            svf.VrSetForm("KNJF070_5.frm", 4);        //svf-form
            svf.VrsOut("NENDO"    , param._nendo);
            svf.VrsOut("TODAY"    , param._ctrlDateFormat);
            svf.VrsOut("SCHOOL_KIND_NAME" , param._schoolKindName);
            svf.VrsOut("DENTAL1_1" , f_name[0][0]);
            svf.VrsOut("DENTAL1_2" , f_name[0][1]);
            svf.VrsOut("DENTAL1_3" , f_name[0][2]);
            svf.VrsOut("DENTAL2_1" , f_name[2][0]);
            svf.VrsOut("DENTAL2_2" , f_name[2][1]);
            svf.VrsOut("DENTAL2_3" , f_name[2][2]);
            svf.VrsOut("DENTAL3_1" , f_name[1][0]);
            svf.VrsOut("DENTAL3_2" , f_name[1][1]);
            svf.VrsOut("DENTAL3_3" , f_name[1][2]);
            svf.VrsOut("DENTAL4_1" , f_name[3][0]);
            svf.VrsOut("DENTAL4_2" , f_name[3][1]);
            svf.VrsOut("DENTAL4_3" , f_name[3][2]);

            for (final Iterator it = param._gradeAgeList.iterator(); it.hasNext();) {
                final GradeAge g = (GradeAge) it.next();
                svf.VrsOut("GRADE", g._name);

                final Map keyMap = (Map) gradeMap.get(g._grade);
                if (null != keyMap) {
                    for (final Iterator sit = keyMap.keySet().iterator(); sit.hasNext();) {
                        final String key = (String) sit.next();
                        final Map valueMap = (Map) keyMap.get(key);
                        final String sfx = "1".equals(key) ? "_M" : "2".equals(key) ? "_F" : null;
                        for (int ib = 1; ib <= 4; ib++) {
                            for (int ic = 1; ic <= 3; ic++) {
                                svf.VrsOut("TEETH" + ib + "_" + ic + sfx, (String) valueMap.get("CNT" + ib + "_" + ic));
                            }
                        }
                    }
                }
                svf.VrEndRecord();
            }

            final Map keyMap = (Map) gradeMap.get("9999");
            if (null != keyMap) {
                for (final Iterator sit = keyMap.keySet().iterator(); sit.hasNext();) {
                    final String key = (String) sit.next();
                    final Map valueMap = (Map) keyMap.get(key);
                    final String sfx = "1".equals(key) ? "_M" : "2".equals(key) ? "_F" : "3".equals(key) ? "" : null;
                    for (int ib = 1; ib <= 4; ib++) {
                        for (int ic = 1; ic <= 3; ic++) {
                            svf.VrsOut("TOTAL_TEETH" + ib + "_" + ic + sfx, (String) valueMap.get("CNT" + ib + "_" + ic));
                        }
                    }
                }
                svf.VrEndRecord();
                nonedata = true;
            }
        } catch (Exception ex) {
            log.error("set_detail6 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

    }  //set_detail6の括り


    /**
     * 身体測定の統計
     * @param param
     * @throws ServletException
     * @throws IOException
     */
    private void set_detail7(final Param param)
    throws SQLException, ServletException, IOException {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            StringBuffer stb = new StringBuffer();

            //異年令を除く
            if (param._notAge != null) {
                stb.append(" WITH T_AGE(GRADE, AGE) AS ( ");
                stb.append("     VALUES('9999', 99) ");

                for (final Iterator it = param._gradeAgeList.iterator(); it.hasNext();) {
                    final GradeAge gradeAge = (GradeAge) it.next();

                    stb.append("     UNION ALL VALUES('" + gradeAge._grade + "', " + gradeAge._age + ") ");
                }

                stb.append("     ) ");

                stb.append(" , NOT_AGE AS ( ");
                stb.append("     SELECT ");
                stb.append("         T2.SCHREGNO, ");
                stb.append("         T2.GRADE ");
                stb.append("     FROM ");
                stb.append("        (SELECT ");
                stb.append("             T1.YEAR, ");
                stb.append("             T1.SCHREGNO, ");
                stb.append("             MAX(T1.GRADE) AS GRADE ");
                stb.append("         FROM ");
                stb.append("             SCHREG_REGD_DAT T1 ");
                stb.append("             INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND = '" + param._schoolKind + "' ");
                stb.append("         WHERE ");
                stb.append("             T1.YEAR = '" + param._year + "' ");
                stb.append("         GROUP BY ");
                stb.append("             T1.YEAR, ");
                stb.append("             T1.SCHREGNO ");
                stb.append("         ) T2 ");
                stb.append("         INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T2.SCHREGNO ");
                stb.append("         INNER JOIN T_AGE NM ON NM.GRADE = T2.GRADE ");
                stb.append("     WHERE ");
                stb.append("         T3.BIRTHDAY < DATE(RTRIM(CHAR(SMALLINT(T2.YEAR) - SMALLINT(NM.AGE) - 1)) || '-04-02') OR ");
                stb.append("         T3.BIRTHDAY > DATE(RTRIM(CHAR(SMALLINT(T2.YEAR) - SMALLINT(NM.AGE))) || '-04-01') ");
                stb.append("     ) ");
            }

            if (!param._isKumamoto) {
                if (param._notAge != null) {
                    stb.append(" , MAX_YEAR AS ( ");
                } else {
                    stb.append(" WITH MAX_YEAR AS ( ");
                }
                stb.append("   SELECT ");
                stb.append("       MAX(YEAR) AS YEAR ");
                stb.append("   FROM ");
                stb.append("       HEXAM_PHYSICAL_AVG_DAT T1 ");
                stb.append("   WHERE ");
                stb.append("       T1.YEAR <= '" + param._year + "' ");
                stb.append(" ), MIN_YEAR AS ( ");
                stb.append("   SELECT ");
                stb.append("       MIN(YEAR) AS YEAR ");
                stb.append("   FROM ");
                stb.append("       HEXAM_PHYSICAL_AVG_DAT T1 ");
                stb.append("   WHERE ");
                stb.append("       T1.YEAR >= '" + param._year + "' ");
                stb.append(" ), MAX_MIN_YEAR AS ( ");
                stb.append("   SELECT ");
                stb.append("       MIN(T1.YEAR) AS YEAR ");
                stb.append("   FROM ( ");
                stb.append("       SELECT YEAR FROM MAX_YEAR T1 ");
                stb.append("       UNION ");
                stb.append("       SELECT YEAR FROM MIN_YEAR T1 ");
                stb.append("   ) T1 ");
                stb.append(" ), HEXAM_PHYSICAL_AVG AS ( ");
                stb.append(" SELECT ");
                stb.append("     T1.SEX, ");
                stb.append("     T1.NENREI_YEAR, ");
                stb.append("     T1.NENREI_MONTH, ");
                stb.append("     T1.STD_WEIGHT_KEISU_A, ");
                stb.append("     T1.STD_WEIGHT_KEISU_B ");
                stb.append(" FROM ");
                stb.append("    HEXAM_PHYSICAL_AVG_DAT T1 ");
                stb.append("    INNER JOIN MAX_MIN_YEAR T2 ON T2.YEAR = T1.YEAR ");
                stb.append(" ) ");
            }

            stb.append(       "SELECT " );
            stb.append(           "VALUE(T1.GRADE,'9999') AS GRADE," );
            stb.append(           "VALUE(T1.SEX,'3') AS SEX," );
            stb.append(           "AVG(T1.HEIGHT) AS HEIGHT, " );
            stb.append(           "AVG(T1.WEIGHT) AS WEIGHT, " );
            stb.append(           "AVG(T1.SITHEIGHT) AS SITHEIGHT, " );
            stb.append(           "COUNT(*) AS ALL_CNT, ");
            if (!param._isKumamoto) {
                stb.append(           "SUM(CASE WHEN                  BMI_2 <= -30 THEN 1 ELSE 0 END) AS BMI1, ");
                stb.append(           "SUM(CASE WHEN -30 <  BMI_2 AND BMI_2 <= -20 THEN 1 ELSE 0 END) AS BMI2, ");
                stb.append(           "SUM(CASE WHEN -20 <  BMI_2 AND BMI_2 <   20 THEN 1 ELSE 0 END) AS BMI3, ");
                stb.append(           "SUM(CASE WHEN  20 <= BMI_2 AND BMI_2 <   30 THEN 1 ELSE 0 END) AS BMI4, ");
                stb.append(           "SUM(CASE WHEN  30 <= BMI_2 AND BMI_2 <   50 THEN 1 ELSE 0 END) AS BMI5, ");
                stb.append(           "SUM(CASE WHEN  50 <= BMI_2                  THEN 1 ELSE 0 END) AS BMI6, ");
                stb.append(           "100.0 * SUM(CASE WHEN                  BMI_2 <= -30 THEN 1 ELSE 0 END) / COUNT(*) AS BMI1P, ");
                stb.append(           "100.0 * SUM(CASE WHEN -30 <  BMI_2 AND BMI_2 <= -20 THEN 1 ELSE 0 END) / COUNT(*) AS BMI2P, ");
                stb.append(           "100.0 * SUM(CASE WHEN -20 <  BMI_2 AND BMI_2 <   20 THEN 1 ELSE 0 END) / COUNT(*) AS BMI3P, ");
                stb.append(           "100.0 * SUM(CASE WHEN  20 <= BMI_2 AND BMI_2 <   30 THEN 1 ELSE 0 END) / COUNT(*) AS BMI4P, ");
                stb.append(           "100.0 * SUM(CASE WHEN  30 <= BMI_2 AND BMI_2 <   50 THEN 1 ELSE 0 END) / COUNT(*) AS BMI5P, ");
                stb.append(           "100.0 * SUM(CASE WHEN  50 <= BMI_2                        THEN 1 ELSE 0 END) / COUNT(*) AS BMI6P ");
            } else {
                stb.append(           "SUM(CASE WHEN  0 <= BMI AND BMI < 18 THEN 1 ELSE 0 END) AS BMI1, ");
                stb.append(           "SUM(CASE WHEN 18 <= BMI AND BMI < 20 THEN 1 ELSE 0 END) AS BMI2, ");
                stb.append(           "SUM(CASE WHEN 20 <= BMI AND BMI < 24 THEN 1 ELSE 0 END) AS BMI3, ");
                stb.append(           "SUM(CASE WHEN 24 <= BMI AND BMI < 26 THEN 1 ELSE 0 END) AS BMI4, ");
                stb.append(           "SUM(CASE WHEN 26 <= BMI              THEN 1 ELSE 0 END) AS BMI5, ");
                stb.append(           "100.0 * SUM(CASE WHEN  0 <= BMI AND BMI < 18 THEN 1 ELSE 0 END) / COUNT(*) AS BMI1P, ");
                stb.append(           "100.0 * SUM(CASE WHEN 18 <= BMI AND BMI < 20 THEN 1 ELSE 0 END) / COUNT(*) AS BMI2P, ");
                stb.append(           "100.0 * SUM(CASE WHEN 20 <= BMI AND BMI < 24 THEN 1 ELSE 0 END) / COUNT(*) AS BMI3P, ");
                stb.append(           "100.0 * SUM(CASE WHEN 24 <= BMI AND BMI < 26 THEN 1 ELSE 0 END) / COUNT(*) AS BMI4P, ");
                stb.append(           "100.0 * SUM(CASE WHEN 26 <= BMI              THEN 1 ELSE 0 END) / COUNT(*) AS BMI5P ");
            }
            stb.append(       "FROM (SELECT " );
            stb.append(              "T1.SCHREGNO," );
            stb.append(              "T2.GRADE," );
            stb.append(              "T3.SEX," );
            stb.append(              "T1.HEIGHT, " );
            stb.append(              "T1.WEIGHT, " );
            stb.append(              "T1.SITHEIGHT, " );
            stb.append(              "T1.BMI ");
            if (!param._isKumamoto) {
				stb.append(              ",(CASE WHEN T1.WEIGHT IS NULL OR T1.HEIGHT IS NULL ");
				stb.append(              " OR (HP_MAX2.STD_WEIGHT_KEISU_A IS NULL AND HP_MAX.STD_WEIGHT_KEISU_A IS NULL AND HP.STD_WEIGHT_KEISU_A IS NULL) ");
				stb.append(              " OR (HP_MAX2.STD_WEIGHT_KEISU_B IS NULL AND HP_MAX.STD_WEIGHT_KEISU_B IS NULL AND HP.STD_WEIGHT_KEISU_B IS NULL) THEN NULL ");
				stb.append(              "ELSE DECIMAL(ROUND((T1.WEIGHT-(VALUE(HP.STD_WEIGHT_KEISU_A,HP_MAX.STD_WEIGHT_KEISU_A,HP_MAX2.STD_WEIGHT_KEISU_A)*T1.HEIGHT-VALUE(HP.STD_WEIGHT_KEISU_B,HP_MAX.STD_WEIGHT_KEISU_B,HP_MAX2.STD_WEIGHT_KEISU_B)))/(VALUE(HP.STD_WEIGHT_KEISU_A,HP_MAX.STD_WEIGHT_KEISU_A,HP_MAX2.STD_WEIGHT_KEISU_A)*T1.HEIGHT-VALUE(HP.STD_WEIGHT_KEISU_B,HP_MAX.STD_WEIGHT_KEISU_B,HP_MAX2.STD_WEIGHT_KEISU_B))*100,1),5,1) END) AS BMI_2 ");
            }
            stb.append(       "FROM (SELECT " );
            stb.append(              "T1.SCHREGNO," );
            stb.append(              "T1.HEIGHT, " );
            stb.append(              "T1.WEIGHT, " );
            stb.append(              "T1.SITHEIGHT, " );
            stb.append(              "DECIMAL(ROUND(T1.WEIGHT/T1.HEIGHT/T1.HEIGHT*10000,1),4,1) BMI ");
            stb.append(              "FROM MEDEXAM_DET_DAT T1 " );
            stb.append(             "WHERE " );
            stb.append(                   "YEAR = '" + param._year + "' " );
            stb.append(                   "AND T1.HEIGHT > 0 AND T1.WEIGHT > 0 " );
            stb.append(           ") T1 " );
            stb.append(           "INNER JOIN (" );
            stb.append(               "SELECT " );
            stb.append(                   "T1.YEAR," );
            stb.append(                   "T1.SCHREGNO," );
            stb.append(                   "MAX(T1.GRADE) AS GRADE " );
            stb.append(                "FROM " );
            stb.append(                   "SCHREG_REGD_DAT T1 " );
            stb.append(                   "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND = '" + param._schoolKind + "' ");
            stb.append(                "WHERE " );
            stb.append(                   "T1.YEAR = '" + param._year + "' " );
            stb.append(                "GROUP BY " );
            stb.append(                   "T1.YEAR," );
            stb.append(                    "T1.SCHREGNO " );
            stb.append(           ") T2 ON T1.SCHREGNO = T2.SCHREGNO " );
            stb.append(           "INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO " );

            if (!param._isKumamoto) {
                stb.append("     LEFT JOIN HEXAM_PHYSICAL_AVG HP ON HP.SEX=T3.SEX AND HP.NENREI_YEAR=YEAR(T2.YEAR || '-04-01' - T3.BIRTHDAY) AND HP.NENREI_MONTH=MONTH(T2.YEAR || '-04-01' - T3.BIRTHDAY)");
                stb.append("     LEFT JOIN ( SELECT I1.* FROM HEXAM_PHYSICAL_AVG I1 ");
                stb.append("                 INNER JOIN (SELECT SEX, NENREI_YEAR, MAX(NENREI_MONTH) AS MAX_NENREI_MONTH ");
                stb.append("                             FROM HEXAM_PHYSICAL_AVG ");
                stb.append("                             GROUP BY SEX, NENREI_YEAR");
                stb.append("                            ) I2 ON I2.SEX = I1.SEX ");
                stb.append("                                AND I2.NENREI_YEAR = I1.NENREI_YEAR ");
                stb.append("                                AND I2.MAX_NENREI_MONTH = I1.NENREI_MONTH ");
                stb.append("     ) HP_MAX ON HP_MAX.SEX = T3.SEX AND HP_MAX.NENREI_YEAR = YEAR(T2.YEAR || '-04-01' - T3.BIRTHDAY) ");
                stb.append("     LEFT JOIN ( SELECT I1.* FROM HEXAM_PHYSICAL_AVG I1 ");
                stb.append("                 INNER JOIN (SELECT SEX, MAX(NENREI_YEAR) AS MAX_NENREI_YEAR ");
                stb.append("                             FROM HEXAM_PHYSICAL_AVG ");
                stb.append("                             GROUP BY SEX");
                stb.append("                            ) I2 ON I2.SEX = I1.SEX ");
                stb.append("                                AND I2.MAX_NENREI_YEAR = I1.NENREI_YEAR ");
                stb.append("                 INNER JOIN (SELECT SEX, NENREI_YEAR, MAX(NENREI_MONTH) AS MAX_NENREI_MONTH ");
                stb.append("                             FROM HEXAM_PHYSICAL_AVG ");
                stb.append("                             GROUP BY SEX, NENREI_YEAR");
                stb.append("                            ) I3 ON I3.SEX = I1.SEX ");
                stb.append("                                AND I3.NENREI_YEAR = I1.NENREI_YEAR ");
                stb.append("                                AND I3.MAX_NENREI_MONTH = I1.NENREI_MONTH ");
                stb.append("     ) HP_MAX2 ON HP_MAX2.SEX = T3.SEX ");
            }
            stb.append(" ) T1 " );

            //異年令を除く
            if (param._notAge != null) {
                stb.append(" WHERE ");
                stb.append("     NOT EXISTS(SELECT 'X' FROM NOT_AGE W1 WHERE W1.SCHREGNO = T1.SCHREGNO) ");
            }

            stb.append(           "GROUP BY " );
            stb.append(                 "GROUPING SETS ((T1.GRADE,T1.SEX),(T1.SEX),())" );
            stb.append(         "ORDER BY " );
            stb.append(             "T1.GRADE," );
            stb.append(             "T1.SEX");
            String sql = stb.toString();

            //log.debug("sql7 = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final Map gradeMap = new HashMap();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                if (null == gradeMap.get(grade)) {
                    gradeMap.put(grade, new HashMap());
                }
                final Map m = (Map) gradeMap.get(grade);
                m.put(rs.getString("SEX"), resultSetToMap(rs));
            }

            /** SVF-formへデータを出力 **/
            svf.VrSetForm(!param._isKumamoto ? "KNJF070_6_2.frm" : "KNJF070_6.frm", 4);        //svf-form
            svf.VrsOut("NENDO"    , param._nendo);
            svf.VrsOut("TODAY"    , param._ctrlDateFormat);
            svf.VrsOut("SCHOOL_KIND_NAME" , param._schoolKindName);

            for (final Iterator it = param._gradeAgeList.iterator(); it.hasNext();) {
                final GradeAge g = (GradeAge) it.next();
                svf.VrsOut("GRADE", g._name);

                final Map keyMap = (Map) gradeMap.get(g._grade);
                if (null != keyMap) {
                    for (final Iterator sit = keyMap.keySet().iterator(); sit.hasNext();) {
                        final String key = (String) sit.next();
                        final Map valueMap = (Map) keyMap.get(key);
                        final String sfx = "1".equals(key) ? "_M" : "2".equals(key) ? "_F" : null;
                        svf.VrsOut("NUMBER"       + sfx, (String) valueMap.get("ALL_CNT"));
                        svf.VrsOut("HEIGHT"       + sfx, bg1((String) valueMap.get("HEIGHT")));
                        svf.VrsOut("WEIGHT"       + sfx, bg1((String) valueMap.get("WEIGHT")));
                        svf.VrsOut("SEATEDHEIGHT" + sfx, bg1((String) valueMap.get("SITHEIGHT")));
                        svf.VrsOut("BMI1_1"       + sfx, (String) valueMap.get("BMI1"));
                        svf.VrsOut("BMI2_1"       + sfx, (String) valueMap.get("BMI2"));
                        svf.VrsOut("BMI3_1"       + sfx, (String) valueMap.get("BMI3"));
                        svf.VrsOut("BMI4_1"       + sfx, (String) valueMap.get("BMI4"));
                        svf.VrsOut("BMI5_1"       + sfx, (String) valueMap.get("BMI5"));
                        if (!param._isKumamoto) {
                            svf.VrsOut("BMI6_1"       + sfx, (String) valueMap.get("BMI6"));
                        }

                    }
                }
                svf.VrEndRecord();
            }

            final Map keyMap = (Map) gradeMap.get("9999");
            if (null != keyMap) {
                for (final Iterator sit = keyMap.keySet().iterator(); sit.hasNext();) {
                    final String key = (String) sit.next();
                    final Map valueMap = (Map) keyMap.get(key);
                    final String sfx = "1".equals(key) ? "_M" : "2".equals(key) ? "_F" : "3".equals(key) ? "" : null;
                    svf.VrsOut("TOTAL_NUMBER"       + sfx, (String) valueMap.get("ALL_CNT"));
                    svf.VrsOut("TOTAL_HEIGHT"       + sfx, bg1((String) valueMap.get("HEIGHT")));
                    svf.VrsOut("TOTAL_WEIGHT"       + sfx, bg1((String) valueMap.get("WEIGHT")));
                    svf.VrsOut("TOTAL_SEATEDHEIGHT" + sfx, bg1((String) valueMap.get("SITHEIGHT")));
                    svf.VrsOut("TOTAL_BMI1_1"       + sfx, (String) valueMap.get("BMI1"));
                    svf.VrsOut("TOTAL_BMI2_1"       + sfx, (String) valueMap.get("BMI2"));
                    svf.VrsOut("TOTAL_BMI3_1"       + sfx, (String) valueMap.get("BMI3"));
                    svf.VrsOut("TOTAL_BMI4_1"       + sfx, (String) valueMap.get("BMI4"));
                    svf.VrsOut("TOTAL_BMI5_1"       + sfx, (String) valueMap.get("BMI5"));
                    if (!param._isKumamoto) {
                        svf.VrsOut("TOTAL_BMI6_1"       + sfx, (String) valueMap.get("BMI6"));
                    }

                    if ("3".equals(key)) {
                        svf.VrsOut("BMI1_2"    , bg1((String) valueMap.get("BMI1P")));
                        svf.VrsOut("BMI2_2"    , bg1((String) valueMap.get("BMI2P")));
                        svf.VrsOut("BMI3_2"    , bg1((String) valueMap.get("BMI3P")));
                        svf.VrsOut("BMI4_2"    , bg1((String) valueMap.get("BMI4P")));
                        svf.VrsOut("BMI5_2"    , bg1((String) valueMap.get("BMI5P")));
                        if (!param._isKumamoto) {
                            svf.VrsOut("BMI6_2"    , bg1((String) valueMap.get("BMI6P")));
                        }
                        svf.VrsOut("BMI1_3"    , (String) valueMap.get("BMI1"));
                        svf.VrsOut("BMI2_3"    , (String) valueMap.get("BMI2"));
                        svf.VrsOut("BMI3_3"    , (String) valueMap.get("BMI3"));
                        svf.VrsOut("BMI4_3"    , (String) valueMap.get("BMI4"));
                        svf.VrsOut("BMI5_3"    , (String) valueMap.get("BMI5"));
                        if (!param._isKumamoto) {
                            svf.VrsOut("BMI6_3"    , (String) valueMap.get("BMI6"));
                        }
                    }
                }
                svf.VrEndRecord();

                nonedata = true;
            }

            //異年令を除く
            if (param._notAge != null) {
                svf.VrsOut("COMMENT"    , "（注）異年令を除く");
                svf.VrEndRecord();
            }
        } catch (Exception ex) {
            log.error("set_detail7 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

    }  //set_detail7の括り

    private String bg1(String n) {
        if (n == null) return null;
        try {
            return new BigDecimal(n).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        } catch (Exception e) {
            log.debug(" n = \"" + n + "\"");
        }
        return null;
    }

    private static class GradeAge {
        private final String _grade;
        private final String _age;
        private final String _gradeCd;
        private final String _name;

        public GradeAge(
                final String grade,
                final String age,
                final String gradeCd,
                final String name
        ) {
            _grade = grade;
            _age = age;
            _gradeCd = gradeCd;
            _name = name;
        }
    }

    private static class Param {
        /*
        1   CHECK1 on
        2   CHECK2 on
        3   CHECK3 on
        4   CHECK4 on
        5   CHECK5 on
        6   CHECK6 on
        0   YEAR 2002
            DBNAME          GAKUMUH
                [7]作成日
                [8]視力検査の対象データは1=文字か2=数値か
    */
        final String _year;
        final String _check1;
        final String _check2;
        final String _check3;
        final String _check4;
        final String _check5;
        final String _check6;
        String _ctrlDate;
        final String _eyesight;
        final String _notAge;
        String gname = new String();
        final String _schoolKind;
        final String _schoolKindName;
        final List _gradeAgeList;
        final boolean _isKumamoto;
        final boolean _isMieken;
        final boolean _isFukuiken;
        final String _nendo;
        final String _ctrlDateFormat;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {

            _year = request.getParameter("YEAR");        // 年度

            final String[] param = new String[10];
            for (int ia = 1; ia <= 6 ; ia++) {
                param[ia] = "off";
                if (request.getParameter("CHECK" + ia) != null) {
                    if (request.getParameter("CHECK" + ia).equalsIgnoreCase("on"))
                        param[ia] = "on";
                }
            }
            _check1 = param[1];
            _check2 = param[2];
            _check3 = param[3];
            _check4 = param[4];
            _check5 = param[5];
            _check6 = param[6];

            _eyesight = request.getParameter("EYESIGHT");
            _notAge = request.getParameter("NOT_AGE");

            //  作成日(現在処理日)の取得
            try {
                KNJ_Control control = new KNJ_Control();                            //クラスのインスタンス作成
                KNJ_Control.ReturnVal returnval = control.Control(db2);
                _ctrlDate = returnval.val3;                                          //現在処理日
            } catch( Exception e) {
                log.error("ctrl_date get error!", e);
            }

            //  学校区分の取得
            try {
                KNJ_Schoolinfo_2 schoolinfo = new KNJ_Schoolinfo_2();
                KNJ_Schoolinfo_2.ReturnVal returnval = schoolinfo.Schooldiv(db2, _year);
                String val = returnval.val1;                                            //学校区分
                if ("0".equals(val)) gname = "学年";
                else                 gname = "年次";
            } catch (Exception e) {
                log.error("schooldiv get error!", e);
            }
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _schoolKindName = getSchoolKindName(db2);

            _gradeAgeList = getGradeAgeList(db2, this);

            final String z010 = getNamemstZ010(db2);
            _isKumamoto = "kumamoto".equals(z010);
            _isMieken = "mieken".equals(z010);
            _isFukuiken = "fukuiken".equals(z010);

            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
            _ctrlDateFormat = KNJ_EditDate.h_format_JP(db2, _ctrlDate);
        }

        private String getGradeAgeSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     GRADE, ");
            stb.append("     SCHOOL_KIND, ");
            stb.append("     GRADE_CD, ");
            stb.append("     GRADE_NAME1 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_GDAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SCHOOL_KIND = '" + param._schoolKind + "' ");
            stb.append(" ORDER BY ");
            stb.append("     GRADE ");
            return stb.toString();
        }

        private List getGradeAgeList(final DB2UDB db2, final Param param) throws SQLException  {
            final List rtnList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getGradeAgeSql(param);
                log.debug("getGradeAgeSql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int age = 0;
                if ("P".equals(param._schoolKind)) age = 6;
                if ("J".equals(param._schoolKind)) age = 12;
                if ("H".equals(param._schoolKind)) age = 15;
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String gradeCd = rs.getString("GRADE_CD");

                    if (age == 0) continue;

                    //log.debug("grade = " + grade + ", age = " + age);
                    final String name = (NumberUtils.isDigits(gradeCd) ? String.valueOf(Integer.parseInt(gradeCd)) : gradeCd) + param.gname;
                    final GradeAge gradeAge = new GradeAge(grade, String.valueOf(age), gradeCd, name);
                    rtnList.add(gradeAge);

                    age++;
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtnList;
        }

        private String getNamemstZ010(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' AND NAME1 IS NOT NULL "));
        }

        private String getSchoolKindName(final DB2UDB db2) throws SQLException {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT ABBV1 FROM NAME_MST WHERE NAMECD1 = 'A023' AND NAME1 = '" + _schoolKind + "' "));
        }

    }
}  //クラスの括り
