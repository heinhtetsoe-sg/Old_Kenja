package servletpack.KNJZ;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJ_ClassCode;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
*
*  学校教育システム 賢者 [マスタ管理]
*
*                  ＜ＫＮＪＺ３４０＞  職員一覧表
*
* 2003/11/12 nakamoto 和暦変換に対応
* 2004/03/29 yamashiro・教科コード仕様の変更に伴う修正
* 2004/08/19 nakamoto CHAIR_CLS_DATの抽出条件変更
*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJZ340A implements KNJ_ClassCode {

    private static Log log = LogFactory.getLog(KNJZ340A.class);

    private Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    private DB2UDB db2;                     // Databaseクラスを継承したクラス
    private String dbname;
    private boolean nonedata;               // 該当データなしフラグ

    private String _useCurriculumcd;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

                    /*
                        0   YEAR    2002
                        2   GAKKI
                        [1]作成日
                    */

        // パラメータの取得
        String param[] = new String[3];
        try {
            param[0] = request.getParameter("YEAR");            // 年度
            param[2] = request.getParameter("GAKKI");           // 学期
            _useCurriculumcd = request.getParameter("useCurriculumcd");
        } catch (Exception ex) {
            log.error("[KNJZ340]parameter error!", ex);
        }
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意


        // print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

        // svf設定
        svf.VrInit();                           //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);      //PDFファイル名の設定
        svf.VrSetForm("KNJZ340A.frm", 4);        //SuperVisualFormadeで設計したレイアウト定義態の設定

        // ＤＢ接続
        db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch (Exception ex) {
            log.error("[KNJZ340]DB2 open error!", ex);
        }

        //  作成日(現在処理日)の取得
        try {
            KNJ_Control control = new KNJ_Control();                            //クラスのインスタンス作成
            KNJ_Control.ReturnVal returnval = control.Control(db2);
            param[1] = returnval.val3;                                          //現在処理日
        } catch (Exception e) {
            log.error("[KNJC040]ctrl_date get error!", e);
        }

        for(int ia = 0; ia < param.length; ia++) log.info("[KNJZ340]param[" + ia + "]=" + param[ia]);

        //  ＳＶＦ作成処理
        nonedata = false;       // 該当データなしフラグ(MES001.frm出力用)

        set_detail(param);

        //  該当データ無し
        if (nonedata == false) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }

        // 終了処理
        svf.VrQuit();
        db2.close();        // DBを閉じる
        outstrm.close();    // ストリームを閉じる

    }//svf_outの括り


    /*----------------------------*
     * ＳＶＦ出力
     *----------------------------*/
    private void set_detail(String param[])
    {
        try {
            final StringBuffer sql = new StringBuffer();
            sql.append(     "SELECT ");
            sql.append(           "T1.STAFFCD,");
            sql.append(           "T1.SECTIONCD,");
            sql.append(           "CHAR(T4.SECTIONABBV,6) AS SECTIONABBV,");
            sql.append(           "T1.JOBCD,");
            sql.append(           "VALUE(T5.JOBNAME,'') AS JOBNAME,");
            sql.append(           "T1.STAFFNAME,");
            sql.append(           "VALUE(T6.GRADE,'') AS GRADE,");
            sql.append(           "VALUE(T6.CLASSNAME,'') AS CLASSNAME,");
            sql.append(           "T1.DUTYSHARECD,");
            sql.append(           "VALUE(T2.SHARENAME,'') AS SHARENAME ");
            sql.append(         "FROM ");
            sql.append(           "STAFF_MST T1 ");
            sql.append(           "INNER JOIN STAFF_YDAT T7 ON T7.STAFFCD = T1.STAFFCD ");
            sql.append(           "LEFT JOIN STAFF_DETAIL_MST DET ON DET.YEAR = '" + param[0] + "' ");
            sql.append(               " AND DET.STAFFCD = T1.STAFFCD ");
            sql.append(               " AND DET.STAFF_SEQ = '003' ");
            sql.append(           "LEFT JOIN DUTYSHARE_MST T2 ON T2.DUTYSHARECD = DET.FIELD1 ");
            sql.append(           "LEFT JOIN SECTION_MST T4 ON T4.SECTIONCD = T1.SECTIONCD ");
            sql.append(           "LEFT JOIN JOB_MST T5 ON T5.JOBCD = T1.JOBCD ");
            sql.append(           "LEFT JOIN (");
            sql.append(             "SELECT ");
            sql.append(               "W2.STAFFCD,");
            sql.append(               "W3.TRGTGRADE AS GRADE,");
            sql.append(               "W4.CLASSCD,");
            sql.append(               "W4.CLASSNAME ");
            sql.append(             "FROM ");
            sql.append(               "CHAIR_DAT W1,");
            sql.append(               "CHAIR_STF_DAT W2,");
            sql.append(               "CHAIR_CLS_DAT W3,");
            sql.append(               "CLASS_MST W4 ");
            if ("1".equals(_useCurriculumcd)) {
                sql.append(               ",SUBCLASS_MST W6 ");
            }
            sql.append(             "WHERE ");
            sql.append(                 "W1.YEAR = W2.YEAR ");
            sql.append(               "AND W1.SEMESTER = W2.SEMESTER ");
            sql.append(               "AND W1.CHAIRCD = W2.CHAIRCD ");
            sql.append(               "AND W1.YEAR = W3.YEAR ");
            sql.append(               "AND W1.SEMESTER = W3.SEMESTER ");
            //sql.append(             "AND W1.CHAIRCD = W3.CHAIRCD ");
            sql.append(               "AND (W3.CHAIRCD = '0000000' OR W1.CHAIRCD = W3.CHAIRCD) ");  //2004/08/19
            sql.append(               "AND W1.GROUPCD = W3.GROUPCD ");                //2004/08/19
            if ("1".equals(_useCurriculumcd)) {
                sql.append(               "AND W6.CLASSCD = W4.CLASSCD ");
                sql.append(               "AND W6.SCHOOL_KIND = W4.SCHOOL_KIND ");
                sql.append(               "AND W1.SCHOOL_KIND = W6.SCHOOL_KIND ");
                sql.append(               "AND W1.CURRICULUM_CD = W6.CURRICULUM_CD ");
                sql.append(               "AND W1.SUBCLASSCD = W6.SUBCLASSCD ");
            } else {
                sql.append(               "AND SUBSTR(W1.SUBCLASSCD,1,2) = W4.CLASSCD ");
            }
            sql.append(               "AND W1.YEAR = '" + param[0] + "' ");
            sql.append(               "AND W1.SEMESTER = '" + param[2] + "' ");
            sql.append(               "AND W4.CLASSCD BETWEEN '"+subject_D+"' AND '"+subject_U+"' ");
            sql.append(             "GROUP BY ");
            sql.append(               "W2.STAFFCD,");
            sql.append(               "W3.TRGTGRADE,");
            sql.append(               "W4.CLASSCD,");
            sql.append(               "W4.CLASSNAME ");
            sql.append(           ") T6 ON (T1.STAFFCD = T6.STAFFCD) ");
            sql.append(         "WHERE ");
            sql.append(           "T7.YEAR = '" + param[0] + "' ");
            sql.append(         "ORDER BY ");
            sql.append(           "T1.JOBCD,");
            sql.append(           "T1.SECTIONCD,");
            sql.append(           "T1.STAFFCD");


            log.info("[KNJZ340]set_detail sql="+sql);
            PreparedStatement ps = db2.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            log.info("[KNJZ340]set_detail sql ok!");

           /** 照会結果の取得とsvf_formへ出力 **/
            svf.VrsOut("nendo"    , KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度");
            svf.VrsOut("date"       , "(" + KNJ_EditDate.h_format_JP(param[1]) + "現在" + ")");

            String staffcd = "000000";
            int ia = 0;
            while (rs.next()) {
                if(ia == 31){
                    staffcd = "000000";
                    ia = 0;
                }
                final String hrStaff = getHrStaff(param, rs.getString("STAFFCD"));
                final String hrField = getMS932ByteLength(hrStaff) > 16 ? "2" : "1";
                final String clubStaff = getClubStaff(param, rs.getString("STAFFCD"));
                final String clubField = getMS932ByteLength(clubStaff) > 20 ? "2" : "1";
                svf.VrsOut("STAFFCD1"  , rs.getString("STAFFCD"));      //職員コード
                svf.VrsOut("STAFFCD2"  , rs.getString("STAFFCD"));      //職員コード
                svf.VrsOut("STAFFCD3"  , rs.getString("STAFFCD"));      //職員コード
                svf.VrsOut("STAFFCD4"  , rs.getString("STAFFCD"));      //職員コード
                svf.VrsOut("BELONG"      , rs.getString("SECTIONABBV"));    //所属
                if(rs.getString("STAFFCD").equals(staffcd) == false){
                    svf.VrsOut("field1_1"  , rs.getString("JOBNAME"));      //職名
                    svf.VrsOut("field2_1"  , rs.getString("STAFFNAME"));        //氏名
                    svf.VrsOut("field5_1"  , rs.getString("SHARENAME"));        //校務分掌
                    svf.VrsOut("HR_NAME" + hrField , hrStaff);
                    svf.VrsOut("CLUB_NAME" + clubField  , clubStaff);
                    staffcd = rs.getString("STAFFCD");
                }
                svf.VrsOut("field3"    , rs.getString("GRADE"));            //学年
                svf.VrsOut("field4_1"  , rs.getString("CLASSNAME"));        //教科
                svf.VrEndRecord();
                ia++;
                nonedata = true; //該当データなしフラグ
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
            log.info("[KNJZ340]set_detail read ok!");
        } catch (Exception ex) {
            log.error("[KNJZ340]set_detail read error!", ex);
        } finally {
            
        }

    }//set_detailの括り

    private String getHrStaff(final String param[], final String staffCd) {
        String retStr = "";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     VALUE(HR_NAMEABBV, '') AS HR_NAMEABBV, ");
            stb.append("     CASE WHEN TR_CD1 = '" + staffCd + "' OR TR_CD2 = '" + staffCd + "' OR TR_CD3 = '" + staffCd + "' ");
            stb.append("          THEN '担任' ");
            stb.append("          ELSE '副担任' ");
            stb.append("     END AS STAFFNAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param[0] + "' ");
            stb.append("     AND T1.SEMESTER = '" + param[2] + "' ");
            stb.append("     AND (   T1.TR_CD1 = '" + staffCd + "' ");
            stb.append("          OR T1.TR_CD2 = '" + staffCd + "' ");
            stb.append("          OR T1.TR_CD3 = '" + staffCd + "' ");
            stb.append("          OR T1.SUBTR_CD1 = '" + staffCd + "' ");
            stb.append("          OR T1.SUBTR_CD2 = '" + staffCd + "' ");
            stb.append("          OR T1.SUBTR_CD3 = '" + staffCd + "') ");

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            String sep = "";
            while (rs.next()) {
                retStr = retStr + sep + rs.getString("HR_NAMEABBV") + rs.getString("STAFFNAME");
                sep = "、";
            }

        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retStr;
    }

    private String getClubStaff(final String param[], final String staffCd) {
        String retStr = "";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     VALUE(L1.CLUBNAME, '') AS CLUBNAME ");
            stb.append(" FROM ");
            stb.append("     CLUB_ADVISER_DAT T1 ");
            stb.append("     LEFT JOIN CLUB_MST L1 ON T1.CLUBCD = L1.CLUBCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param[0] + "' ");
            stb.append("     AND T1.ADVISER = '" + staffCd + "' ");

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            String sep = "";
            while (rs.next()) {
                retStr = retStr + sep + rs.getString("CLUBNAME");
                sep = "、";
            }

        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retStr;
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str)
    {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

}//クラスの括り
