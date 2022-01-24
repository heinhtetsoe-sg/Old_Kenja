package servletpack.KNJL;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Iterator;
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
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *    学校教育システム 賢者 [SATシステム] 個人成績表
 *
 **/

public class KNJL392Q {

    private static final Log log = LogFactory.getLog(KNJL392Q.class);

    private boolean _hasData;
    private Param _param;

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf     = new Vrw32alp();   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2       = null;             //Databaseクラスを継承したクラス

        //print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        //svf設定
        svf.VrInit();    //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定

        //ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }

        try {
            log.fatal("$Revision: 69948 $ $Date: 2019-09-30 20:50:30 +0900 (月, 30 9 2019) $"); // CVSキーワードの取り扱いに注意
            KNJServletUtils.debugParam(request, log);
            _param = new Param(db2, request);

            //SVF出力
            printMain(db2, svf); //帳票出力のメソッド

        } catch (Exception ex) {
            log.error("exception!", ex);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }

            //終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();       //DBを閉じる
            outstrm.close();   //ストリームを閉じる
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {


        final String ATTRIBUTE_ELECTDIV = "Paint=(14,0,2),Bold=1";

        final List list = getList(db2, sql(_param));
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Map row = (Map) it.next();

            final String form = "KNJL392Q.frm";
            svf.VrSetForm(form, 1);

            for (final String field : Arrays.asList("EXAM_NO", "NAME", "HOMETOWN1", "ATTEND_SCHOOL_NAME1", "GROUP_NAME")) {
                svf.VrAttribute(field, ATTRIBUTE_ELECTDIV); // タイトル
            }
            for (int i = 1; i <= 4; i++) {
                for (final String field : Arrays.asList("SCORE", "AVERAGE1", "AVERAGE2", "DEV1", "DEV2", "RANK1", "RANK2", "GUIDELINE1")) {
                    svf.VrAttributen(field, i, ATTRIBUTE_ELECTDIV); // タイトル
                }
            }
            
            if (null != _param._whitespaceImagepath) {
            	svf.VrsOut("LINE_BMP", _param._whitespaceImagepath);
            	svf.VrsOut("LINE_BMP2", _param._whitespaceImagepath);
            }

            svf.VrsOut("TITLE", "駿台甲府高校実戦模試個人成績表"); // タイトル
            svf.VrsOut("DATE", formatDate(KnjDbUtils.getString(row, "EXAM_DATE"))); //
            svf.VrsOut("EXAM_NO", KnjDbUtils.getString(row, "SAT_NO")); // 受験番号
            svf.VrsOut("NAME", KnjDbUtils.getString(row, "NAME1")); // 氏名
            final String atSchoolName = KnjDbUtils.getString(row, "FINSCHOOL_NAME");
            final String atSchoolField = KNJ_EditEdit.getMS932ByteLength(atSchoolName) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(atSchoolName) > 24 ? "2" : "1";
            svf.VrsOut("ATTEND_SCHOOL_NAME" + atSchoolField, atSchoolName); // 在籍中学
            svf.VrsOut("HOMETOWN1", KnjDbUtils.getString(row, "AREA_NAME")); // 出身県
            svf.VrsOut("HOMETOWN2", KnjDbUtils.getString(row, "AREA_NAME")); // 出身県
            svf.VrsOut("GROUP_NAME", KnjDbUtils.getString(row, "GROUPNAME")); // 団体名
            if (!"1".equals(_param._STUDENT)) {
                svf.VrsOut("ALL_NAME", "全　体"); // 全体名称
            }

            final int lineEigo = 1;
            svf.VrsOutn("SCORE", lineEigo, KnjDbUtils.getString(row, "SCORE_ENGLISH")); // 点数
            if (!"1".equals(_param._STUDENT)) {
                svf.VrsOutn("AVERAGE1", lineEigo, KnjDbUtils.getString(row, "ALL_ENGLISH_AVG")); // 平均点
                svf.VrsOutn("DEV1", lineEigo, KnjDbUtils.getString(row, "ALL_ENGLISH_SD")); // 偏差値
                svf.VrsOutn("RANK1", lineEigo, KnjDbUtils.getString(row, "ALL_RANK_ENGLISH")); // 順位
                svf.VrsOutn("ALL1", lineEigo, KnjDbUtils.getString(row, "ALL_ENGLISH_COUNT")); // 総人数
            }
            svf.VrsOutn("AVERAGE2", lineEigo, KnjDbUtils.getString(row, "ENGLISH_AVG")); // 平均点
            svf.VrsOutn("DEV2", lineEigo, KnjDbUtils.getString(row, "ENGLISH_SD")); // 偏差値
            svf.VrsOutn("RANK2", lineEigo, KnjDbUtils.getString(row, "AREA_RANK_ENGLISH")); // 順位
            svf.VrsOutn("ALL2", lineEigo, KnjDbUtils.getString(row, "ENGLISH_COUNT")); // 総人数

            final int lineSugaku = 2;
            svf.VrsOutn("SCORE", lineSugaku, KnjDbUtils.getString(row, "SCORE_MATH")); // 点数
            if (!"1".equals(_param._STUDENT)) {
                svf.VrsOutn("AVERAGE1", lineSugaku, KnjDbUtils.getString(row, "ALL_MATH_AVG")); // 平均点
                svf.VrsOutn("DEV1", lineSugaku, KnjDbUtils.getString(row, "ALL_MATH_SD")); // 偏差値
                svf.VrsOutn("RANK1", lineSugaku, KnjDbUtils.getString(row, "ALL_RANK_MATH")); // 順位
                svf.VrsOutn("ALL1", lineSugaku, KnjDbUtils.getString(row, "ALL_MATH_COUNT")); // 総人数
            }
            svf.VrsOutn("AVERAGE2", lineSugaku, KnjDbUtils.getString(row, "MATH_AVG")); // 平均点
            svf.VrsOutn("DEV2", lineSugaku, KnjDbUtils.getString(row, "MATH_SD")); // 偏差値
            svf.VrsOutn("RANK2", lineSugaku, KnjDbUtils.getString(row, "AREA_RANK_MATH")); // 順位
            svf.VrsOutn("ALL2", lineSugaku, KnjDbUtils.getString(row, "MATH_COUNT")); // 総人数

            final int lineKokugo = 3;
            svf.VrsOutn("SCORE", lineKokugo, KnjDbUtils.getString(row, "SCORE_JAPANESE")); // 点数
            if (!"1".equals(_param._STUDENT)) {
                svf.VrsOutn("AVERAGE1", lineKokugo, KnjDbUtils.getString(row, "ALL_JAPANESE_AVG")); // 平均点
                svf.VrsOutn("DEV1", lineKokugo, KnjDbUtils.getString(row, "ALL_JAPANESE_SD")); // 偏差値
                svf.VrsOutn("RANK1", lineKokugo, KnjDbUtils.getString(row, "ALL_RANK_JAPANESE")); // 順位
                svf.VrsOutn("ALL1", lineKokugo, KnjDbUtils.getString(row, "ALL_JAPANESE_COUNT")); // 総人数
            }
            svf.VrsOutn("AVERAGE2", lineKokugo, KnjDbUtils.getString(row, "JAPANESE_AVG")); // 平均点
            svf.VrsOutn("DEV2", lineKokugo, KnjDbUtils.getString(row, "JAPANESE_SD")); // 偏差値
            svf.VrsOutn("RANK2", lineKokugo, KnjDbUtils.getString(row, "AREA_RANK_JAPANESE")); // 順位
            svf.VrsOutn("ALL2", lineKokugo, KnjDbUtils.getString(row, "JAPANESE_COUNT")); // 総人数

            final int lineTotal = 4;
            svf.VrsOutn("SCORE", lineTotal, KnjDbUtils.getString(row, "SCORE_TOTAL")); // 点数
            if (!"1".equals(_param._STUDENT)) {
                svf.VrsOutn("AVERAGE1", lineTotal, KnjDbUtils.getString(row, "ALL_TOTAL_AVG")); // 平均点
                svf.VrsOutn("DEV1", lineTotal, KnjDbUtils.getString(row, "ALL_TOTAL_SD")); // 偏差値
                svf.VrsOutn("RANK1", lineTotal, KnjDbUtils.getString(row, "ALL_RANK_TOTAL")); // 順位
                svf.VrsOutn("ALL1", lineTotal, KnjDbUtils.getString(row, "ALL_TOTAL_COUNT")); // 総人数
            }
            svf.VrsOutn("AVERAGE2", lineTotal, KnjDbUtils.getString(row, "TOTAL_AVG")); // 平均点
            svf.VrsOutn("DEV2", lineTotal, KnjDbUtils.getString(row, "TOTAL_SD")); // 偏差値
            svf.VrsOutn("RANK2", lineTotal, KnjDbUtils.getString(row, "AREA_RANK_TOTAL")); // 順位
            svf.VrsOutn("ALL2", lineTotal, KnjDbUtils.getString(row, "TOTAL_COUNT")); // 総人数

            svf.VrsOutn("GUIDELINE1", 1, KnjDbUtils.getString(row, "ENG_COMMENT_1")); // 学習指針
            svf.VrsOutn("GUIDELINE2", 1, KnjDbUtils.getString(row, "ENG_COMMENT_2")); // 学習指針
            svf.VrsOutn("GUIDELINE1", 2, KnjDbUtils.getString(row, "MATH_COMMENT_1")); // 学習指針
            svf.VrsOutn("GUIDELINE2", 2, KnjDbUtils.getString(row, "MATH_COMMENT_2")); // 学習指針
            svf.VrsOutn("GUIDELINE1", 3, KnjDbUtils.getString(row, "JAP_COMMENT_1")); // 学習指針
            svf.VrsOutn("GUIDELINE2", 3, KnjDbUtils.getString(row, "JAP_COMMENT_2")); // 学習指針

            /*その他選択時*/
            if ("2".equals(_param._STUDENT)) {
                if (null != KnjDbUtils.getString(row, "JUDGE")) {
                    svf.VrsOut("JUDGE_NAME", "= 判定 ="); // 判定名称
                    svf.VrsOut("JUDGE", KnjDbUtils.getString(row, "JUDGE")); // 判定
                }

                svf.VrsOut("ASSESSMENT1", KnjDbUtils.getString(row, "JUDGE_COMMENT1")); // 総合評価
                svf.VrsOut("ASSESSMENT2", KnjDbUtils.getString(row, "JUDGE_COMMENT2")); // 総合評価
                svf.VrsOut("ASSESSMENT3", KnjDbUtils.getString(row, "JUDGE_COMMENT3")); // 総合評価
                svf.VrsOut("ASSESSMENT4", KnjDbUtils.getString(row, "JUDGE_COMMENT4")); // 総合評価
            }

            svf.VrsOut("VIEW_TITLE1", "成績表"); // 成績表の見方
            svf.VrsOut("VIEW_TITLE2", "の見方"); // 成績表の見方

            svf.VrsOut("VIEW1", "1.「標準偏差」とは、得点のばらつき具合を示す数値です。"); // 成績表の見方
            /*その他選択時*/
            if ("2".equals(_param._STUDENT)) {
                svf.VrsOut("VIEW2", "2.「地域」は、次に示す各地域別に処理されたデータです。[県内:山梨県内　県外:山梨県外・海外]"); // 成績表の見方
                if (null != KnjDbUtils.getString(row, "JUDGE")) {
                    svf.VrsOut("VIEW3", "3.「= 判定 =」とは、駿台甲府高校の学力適正の判定です。"); // 成績表の見方
                }
            }

            svf.VrEndPage();
            _hasData = true;
        }

    }

    private String sql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        //データ取得
        stb.append(" SELECT ");
        stb.append("     t1.YEAR, ");                                    //年度
        stb.append("     t5.EXAM_DATE, ");                               //実施日
        stb.append("     t1.SAT_NO, ");                                  //受験番号
        stb.append("     t1.NAME1, ");                                   //氏名
        stb.append("     t2.AREA, ");                                    //
        stb.append("     t8.NAME1 as AREA_NAME, ");                      //地域名称(名前のところと順位表の地域名称に使用)
        stb.append("     t1.SCHOOLCD, ");                                //
        stb.append("     t6.FINSCHOOL_NAME, ");                     //中学校名
        stb.append("     t1.GROUPCD, ");                                 //
        stb.append("     t7.GROUPNAME, ");                               //団体名

        stb.append("     t2.SCORE_ENGLISH, ");                           //英語得点
        stb.append("     t2.SCORE_MATH, ");                              //数学得点
        stb.append("     t2.SCORE_JAPANESE, ");                          //国語得点
        stb.append("     t2.SCORE_TOTAL, ");                             //三教科得点

        stb.append("     t14.NAME1 as ALL_NAME, ");                      //'全体'
        stb.append("     t4.ENGLISH_AVG as ALL_ENGLISH_AVG, ");          //全体英語平均点
        stb.append("     t4.ENGLISH_SD as ALL_ENGLISH_SD, ");            //全体英語標準偏差
        stb.append("     t2.ALL_RANK_ENGLISH, ");                        //全体英語順位
        stb.append("     t4.ENGLISH_COUNT as ALL_ENGLISH_COUNT, ");      //全体英語人数

        stb.append("     t4.MATH_AVG as ALL_MATH_AVG, ");                //全体数学平均点
        stb.append("     t4.MATH_SD as ALL_MATH_SD, ");                  //全体数学標準偏差
        stb.append("     t2.ALL_RANK_MATH, ");                           //全体数学順位
        stb.append("     t4.MATH_COUNT as ALL_MATH_COUNT, ");            //全体数学人数

        stb.append("     t4.JAPANESE_AVG as ALL_JAPANESE_AVG, ");        //全体国語平均点
        stb.append("     t4.JAPANESE_SD as ALL_JAPANESE_SD, ");          //全体国語標準偏差
        stb.append("     t2.ALL_RANK_JAPANESE, ");                       //全体国語順位
        stb.append("     t4.JAPANESE_COUNT as ALL_JAPANESE_COUNT, ");    //全体国語人数

        stb.append("     t4.TOTAL_AVG as ALL_TOTAL_AVG, ");              //全体三教科平均点
        stb.append("     t4.TOTAL_SD as ALL_TOTAL_SD, ");                //全体三教科標準偏差
        stb.append("     t2.ALL_RANK_TOTAL, ");                          //全体三教科順位
        stb.append("     t4.TOTAL_COUNT as ALL_TOTAL_COUNT, ");          //全体三教科人数

        stb.append("     t3.ENGLISH_AVG, ");                             //地域英語平均点
        stb.append("     t3.ENGLISH_SD, ");                              //地域英語標準偏差
        stb.append("     t2.AREA_RANK_ENGLISH, ");                       //地域英語順位
        stb.append("     t3.ENGLISH_COUNT, ");                           //地域英語人数

        stb.append("     t3.MATH_AVG, ");                                //地域数学平均点
        stb.append("     t3.MATH_SD, ");                                 //地域数学標準偏差
        stb.append("     t2.AREA_RANK_MATH, ");                          //地域数学順位
        stb.append("     t3.MATH_COUNT, ");                              //地域数学人数

        stb.append("     t3.JAPANESE_AVG, ");                            //地域国語平均点
        stb.append("     t3.JAPANESE_SD, ");                             //地域国語標準偏差
        stb.append("     t2.AREA_RANK_JAPANESE, ");                      //地域国語順位
        stb.append("     t3.JAPANESE_COUNT, ");                          //地域国語人数

        stb.append("     t3.TOTAL_AVG, ");                               //地域三教科平均点
        stb.append("     t3.TOTAL_SD, ");                                //地域三教科標準偏差
        stb.append("     t2.AREA_RANK_TOTAL, ");                         //地域三教科順位
        stb.append("     t3.TOTAL_COUNT, ");                             //地域三教科人数

        stb.append("     t2.COMMENT_ENGLISH, ");                         //
        stb.append("     t10.COMMENT1 as ENG_COMMENT_1, ");              //学習指針英語1行目
        stb.append("     t10.COMMENT2 as ENG_COMMENT_2, ");              //学習指針英語2行目
        stb.append("     t2.COMMENT_MATH, ");                            //
        stb.append("     t11.COMMENT1 as MATH_COMMENT_1, ");             //学習指針数学1行目
        stb.append("     t11.COMMENT2 as MATH_COMMENT_2, ");             //学習指針数学2行目
        stb.append("     t2.COMMENT_JAPANESE, ");                        //
        stb.append("     t12.COMMENT1 as JAP_COMMENT_1, ");              //学習指針国語1行目
        stb.append("     t12.COMMENT2 as JAP_COMMENT_2, ");              //学習指針国語2行目

        stb.append("     t2.JUDGE_SAT, ");                               //
        stb.append("     t9.NAME1 as JUDGE, ");                          //判定名称

        stb.append("     t2.COMMENTNO, ");                               //
        stb.append("     t13.COMMENT1 as JUDGE_COMMENT1, ");             //総合評価1行目
        stb.append("     t13.COMMENT2 as JUDGE_COMMENT2, ");             //総合評価2行目
        stb.append("     t13.COMMENT3 as JUDGE_COMMENT3, ");             //総合評価3行目
        stb.append("     t13.COMMENT4 as JUDGE_COMMENT4 ");              //総合評価4行目

        stb.append(" FROM ");
        stb.append("     (SELECT ");
        stb.append("         YEAR, ");
        stb.append("         SAT_NO, ");
        stb.append("         NAME1, ");
        stb.append("         SCHOOLCD, ");
        stb.append("         GROUPCD ");
        stb.append("     FROM ");
        stb.append("         SAT_APP_FORM_MST ");
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._CTRL_YEAR + "' AND ");
        stb.append("         ABSENCE != 0 ");
        if ("1".equals(_param._STUDENT)) {
            /*駿中生選択時*/
            stb.append("     AND ");
            stb.append("         PLACECD = '80' ");
        } else {
            /*その他選択時*/
            stb.append("     AND ");
            stb.append("         PLACECD <> '80' ");
        }
        if ("2".equals(_param._CHOICE)) {
            /*試験会場指定時*/
            stb.append("     AND ");
            stb.append("         PLACECD = '" + _param._PLACE_COMB + "' ");
        }
        if ("3".equals(_param._CHOICE)) {
            /*受験番号指定時*/
            stb.append("     AND ");
            if (!StringUtils.isBlank(_param._EXAM_FROM) && !StringUtils.isBlank(_param._EXAM_TO)) {
                stb.append("         SAT_NO BETWEEN '" + _param._EXAM_FROM + "' AND '" + _param._EXAM_TO + "' ");  //FROM・TO両方入力時
            } else {
                stb.append("         SAT_NO = '" + _param._EXAM_FROM + "' ");                    //FROMのみ入力時
            }
        }
        stb.append("     ) t1 ");
        stb.append("     left join SAT_EXAM_DAT t2 on t1.SAT_NO = t2.SAT_NO and t1.YEAR = t2.YEAR ");
        stb.append("     left join SAT_AREA_RECORD_DAT t3 on t1.YEAR = t3.YEAR and t2.AREA = t3.AREA ");
        stb.append("     left join SAT_AREA_RECORD_DAT t4 on t1.YEAR = t4.YEAR and t4.AREA = '9' ");

        stb.append("     left join SAT_INFO_MST t5 on t1.YEAR = t5.YEAR ");
        stb.append("     left join FINSCHOOL_MST t6 on t1.SCHOOLCD = t6.FINSCHOOLCD and t6.FINSCHOOL_TYPE = '3' ");
        stb.append("     left join SAT_GROUP_DAT t7 on t1.YEAR = t7.YEAR and t1.GROUPCD = t7.GROUPCD ");
        stb.append("     left join NAME_MST t8 on t2.AREA = t8.NAMECD2 and t8.NAMECD1 = 'L204' ");
        stb.append("     left join NAME_MST t9 on t2.JUDGE_SAT = t9.NAMECD2 and t9.NAMECD1 = 'L200' ");

        stb.append("     left join SAT_COMMENT_ENGLISH_DAT t10 on t2.COMMENT_ENGLISH = t10.COMMENT_NO and t1.YEAR = t10.YEAR ");
        stb.append("     left join SAT_COMMENT_MATH_DAT t11 on t2.COMMENT_MATH = t11.COMMENT_NO and t1.YEAR = t11.YEAR ");
        stb.append("     left join SAT_COMMENT_JAPANESE_DAT t12 on t2.COMMENT_JAPANESE = t12.COMMENT_NO and t1.YEAR = t12.YEAR ");
        stb.append("     left join SAT_COMMENT_JUDGE_DAT t13 on t2.COMMENTNO = t13.COMMENTNO and t1.YEAR = t13.YEAR ");

        stb.append("     left join NAME_MST t14 on t4.AREA = t14.NAMECD2 and t14.NAMECD1 = 'L204' ");
        stb.append(" ORDER BY ");
        stb.append("     SAT_NO ");
        return stb.toString();
    }

    private List getList(final DB2UDB db2, final String sql) {
        log.info(" sql = " + sql);
        return KnjDbUtils.query(db2, sql);
    }

    private static String formatDate(final String date) {
        if (null == date) {
            return "";
        }
        final String nen = new SimpleDateFormat("yyyy年").format(Date.valueOf(date));
        final String retDate = nen + KNJ_EditDate.h_format_JP_MD(date);
        return KNJ_EditEdit.convertZenkakuSuuji(retDate);
    }

    private static class Param {
//        final String _AREA_FROM;
//        final String _AREA_TO;
//        final String _CHECK_CNT;
        final String _CHOICE; // 1: すべて 2:試験会場 3:受験番号
        final String _CTRL_DATE;
        final String _CTRL_SEMESTER;
        final String _CTRL_YEAR;
        final String _EXAM_FROM; // 受験番号開始
        final String _EXAM_TO; // 受験番号終了
        final String _PLACE_COMB; // 試験会場
        final String _PRGID;
        final String _STUDENT; // 1:駿中生 2:その他
        final String _documentroot;
        final String _imagepath;
        final String _cmd;
        String _whitespaceImagepath;
        private Param(final DB2UDB db2, final HttpServletRequest request) {
//            _AREA_FROM = request.getParameter("AREA_FROM");
//            _AREA_TO = request.getParameter("AREA_TO");
//            _CHECK_CNT = request.getParameter("CHECK_CNT");
            _CHOICE = request.getParameter("CHOICE");
            _CTRL_DATE = request.getParameter("CTRL_DATE");
            _CTRL_SEMESTER = request.getParameter("CTRL_SEMESTER");
            _CTRL_YEAR = request.getParameter("CTRL_YEAR");
            _EXAM_FROM = request.getParameter("EXAM_FROM");
            _EXAM_TO = request.getParameter("EXAM_TO");
            _PLACE_COMB = request.getParameter("PLACE_COMB");
            _PRGID = request.getParameter("PRGID");
            _STUDENT = request.getParameter("STUDENT");
            _documentroot = request.getParameter("DOCUMENTROOT");
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' "));
            final String path = _documentroot + "/" + _imagepath + "/whitespace.png";
            if (!new File(path).exists()) {
            	log.warn(" no image : " + path);
            } else {
            	_whitespaceImagepath = path;
            }
            log.info(" _whitespaceImagepath = " + _whitespaceImagepath);
            _cmd = request.getParameter("cmd");
        }
    }
}//クラスの括り
