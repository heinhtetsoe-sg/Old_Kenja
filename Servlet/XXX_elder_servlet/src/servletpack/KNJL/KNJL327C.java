package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;


/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 *      ＜ＫＮＪＬ３２７Ｈ＞
 *      ＜１＞  合格通知書
 *      ＜２＞  入学許可通知書
 *      ＜３＞  移行合格通知書
 *      ＜４＞  追加合格通知書
 *      ＜５＞　補欠合格通知書
 *
 *    2008/11/07 takara 作成日
 *
 **/

public class KNJL327C {
    private static final Log log = LogFactory.getLog(KNJL327C.class);

    boolean nonedata;            //対象となるデータがあるかのフラグ(『true』の時は中身が空の意)
    Param _param;


    /**
     * 処理もメイン
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        Vrw32alp svf     = new Vrw32alp();   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2       = null;             //Databaseクラスを継承したクラス

        //■■■print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        //■■■svf設定
        svf.VrInit();    //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定

        //■■■ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }
        _param = new Param(db2, request);

        //■■■SVF出力
        nonedata = setSvfMain(db2, svf); //帳票出力のメソッド

        //■■■該当データ無し(nonedate=falseで該当データなし)
        if( nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }

        //■■■終了処理
        svf.VrQuit();
        db2.commit();
        db2.close();       //DBを閉じる
        outstrm.close();   //ストリームを閉じる
    }


    /**
     * フォームの出力
     * @param db2
     * @param svf
     * @return
     */
    private boolean setSvfMain(final DB2UDB db2, Vrw32alp svf) {
        boolean nonedata = true;            //対象となるデータがあるかのフラグ(『true』の時は中身が空の意)
        PreparedStatement sqlMain = null;
        PreparedStatement sqlGetKoutyou = null;
        ResultSet rsMain = null;
        ResultSet rsGetKoutyou = null;

        final String date = gethiduke(db2, _param._print_date);
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度";
        final String getKoutyou = getKoutyou();
        final String dateSingaku = gethiduke(db2, _param._singakuDate);

        try {
            final String formID = getFormId();
            svf.VrSetForm(formID, 4);
            final String sql = getTuuchiSql();
            //log.info(" sql = " + sql);
            sqlMain = db2.prepareStatement(sql);
            sqlGetKoutyou = db2.prepareStatement(getKoutyou);
            rsMain = sqlMain.executeQuery();
            rsGetKoutyou = sqlGetKoutyou.executeQuery();
            String principalName = "";
            String jobName = "";
            String schoolName = "";
            while(rsGetKoutyou.next()){
                principalName = rsGetKoutyou.getString("PRINCIPAL_NAME");
                jobName = rsGetKoutyou.getString("JOB_NAME");
                schoolName = rsGetKoutyou.getString("SCHOOL_NAME");
            }
            while(rsMain.next()){
                svf.VrsOut("NENDO"      , nendo);
                svf.VrsOut("DATE"       , date);
                svf.VrsOut("STAFFNAME"  , principalName);
                svf.VrsOut("JOBNAME"    , jobName);
                svf.VrsOut("SCHOOLNAME" , schoolName);
                svf.VrsOut("EXAMNO"     , rsMain.getString("EXAMNO"));
                svf.VrsOut("NAME"       , rsMain.getString("NAME"));
                if (_param.isCollege() && "1".equals(_param._outputL)) { // カレッジ合格者
                    printCollegeGokaku(db2, svf, schoolName, rsMain.getString("JUDGEDIV"), rsMain.getString("SHDIV"));
                } else
                if (_param.isGojoOrCollege() && "1".equals(_param._outputL)) { // 五條合格者
                    printGojoGokaku(db2, svf, rsMain.getString("TESTDIV"), rsMain.getString("TESTDIVNAME"), rsMain.getString("SHDIV"), rsMain.getString("SHDIVNAME"), rsMain.getString("JUDGEDIV"));
                } else
                if (_param.isWakayama() && ("1".equals(_param._outputL) || "4".equals(_param._outputL))) { // 和歌山合格者＆追加合格者
                    printWakayamaGokaku(svf, rsMain.getString("JUDGEDIV"), rsMain.getString("SPECIAL_MEASURES"));
                }
                if (_param.isCollege() && "2".equals(_param._outputL)) { // カレッジ入学者
                    printCollegeNyugaku(db2, svf);
                    svf.VrsOut("BIRTHDAY"   , KNJ_EditDate.h_format_JP_Bth(db2, rsMain.getString("BIRTHDAY")));
                } else
                if (!_param.isGojoOrCollege() && "2".equals(_param._outputL) && "6".equals(_param._testdiv)) { // 和歌山入学者
                    printWakayamaNyugaku(svf);
                    svf.VrsOut("BIRTHDAY"   , KNJ_EditDate.h_format_JP_Bth(db2, rsMain.getString("BIRTHDAY")));
                } else
                if (_param.isGojoOrCollege() && "2".equals(_param._outputL)) { // 五條入学者
                    printGojoNyugaku(db2, svf);
                }
                if ((_param.isGojoOrCollege() || _param.isWakayama()) && "6".equals(_param._outputL)) { // 五條:進学通知書(入学手続者)
                    printGojoSingaku(svf, dateSingaku);
                    svf.VrsOut("KANA"       , rsMain.getString("NAME_KANA"));
                    svf.VrsOut("SEX"        , rsMain.getString("SEX_NAME"));
                    svf.VrsOut("BIRTHDAY"   , KNJ_EditDate.h_format_JP_Bth(db2, rsMain.getString("BIRTHDAY")));
                    svf.VrsOut("GRD_NAME"   , rsMain.getString("GNAME"));
                    svf.VrsOut("ADDR1_" + (50 < getMS932count(rsMain.getString("ADDRESS1")) ? "2" : "1"), rsMain.getString("ADDRESS1"));
                    svf.VrsOut("ADDR2_" + (50 < getMS932count(rsMain.getString("ADDRESS2")) ? "2" : "1"), rsMain.getString("ADDRESS2"));
                    svf.VrsOut("FIN_SCHOOL" , rsMain.getString("FINSCHOOL_NAME"));
                }
                if ("3".equals(_param._outputL) && "2".equals(_param._applicantdiv)) {
                    svf.VrsOut("COURSE"     , _param._shiftCourse);
                }
                if (_param.isCollege() && "4".equals(_param._outputL)) { // カレッジ追加合格通知書
                    printCollegeTuikaGokaku(db2, svf, schoolName);
                }
                if ("5".equals(_param._outputL)) {
                    log.debug("dateVisitSchool="+_param._dateVisitSchool);
                    String dayCome = KNJ_EditDate.h_format_JP_MD(_param._dateVisitSchool);
                    String dayOfWeek = "("+KNJ_EditDate.h_format_W(_param._dateVisitSchool)+")";
                    String timeCome = null;
                    if (_param._visitHour.intValue() < 12) {
                        timeCome = "午前"+_param._visitHour+"時";
                    } else {
                        timeCome = "午後"+(_param._visitHour.intValue()-12)+"時";
                    }
                    if (_param._visitMinute.intValue() != 0 ) {
                        timeCome += _param._visitMinute+"分";
                    }
                    log.debug(dayCome+dayOfWeek+timeCome);
                    svf.VrsOut("VISIT_DATE", dayCome+dayOfWeek+timeCome);
                }

                svf.VrEndRecord();
                nonedata = false;            //対象となるデータがあるかのフラグ(『true』の時は中身が空の意)
            }
            rsMain.close();
            sqlMain.close();
        } catch (Exception e) {
            log.debug("クエリのセットのところでエラー", e);
        }
        return nonedata;
    }

    private int getMS932count(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }

    private int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
                ret = str.length() * 2;
            }
        }
        return ret;
    }

    private void printGojoSingaku(final Vrw32alp svf, final String dateSingaku) {
        svf.VrsOut("TEXT1", "　上記のものは本校入学の諸手続を完了し、");
        svf.VrsOut("TEXT2", dateSingaku + "より本校第１学年に入学予定");
        svf.VrsOut("TEXT3", "であります。");
    }

    private void printGojoNyugaku(final DB2UDB db2, final Vrw32alp svf) {
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year));
//        if ("1".equals(_param._applicantdiv)) {
//            svf.VrsOut("TEXT1", "あなたは、先に合格と決定され、このたび");
//            svf.VrsOut("TEXT2", "手続きを完了されましたので、" + nendo + "年度");
//            svf.VrsOut("TEXT3", "第１学年に入学することを許可します。");
//        } else if ("2".equals(_param._applicantdiv)) {
//            svf.VrsOut("TEXT1", "あなたは、先に合格と決定され、このたび");
//            svf.VrsOut("TEXT2", "手続きを完了されましたので、" + nendo + "年度");
//            final String text3 = testdivName + "第１学年に入学することを許可します。";
//            final String[] text3arr = KNJ_EditEdit.get_token(text3, 38, 2);
//            if (null != text3arr) {
//                for (int i = 0; i < text3arr.length; i++) {
//                    svf.VrsOut("TEXT" + String.valueOf(3 + i), text3arr[i]);
//                }
//            }
//        }
        svf.VrsOut("TEXT1", "　あなたは、本校の" + nendo + "年度入学試験に");
        svf.VrsOut("TEXT2", "合格し所定の手続きを完了されましたので");
        svf.VrsOut("TEXT3", "第１学年に入学することを許可します。");
    }


    private void printGojoGokaku(final DB2UDB db2, final Vrw32alp svf, final String testdiv, final String testdivName, final String shdiv, final String shdivName, final String judgediv) {
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year));
        if ("2".equals(_param._applicantdiv)) { // 高校入試
            final String text1;
            if ("3".equals(testdiv) && "1".equals(shdiv)) {
                text1 = "　" + nendo + "年度推薦入試選考の結果、あなたは";
            } else {
                text1 = "　" + nendo + "年度入学試験の結果、あなたは";
            }
            final String text2 = testdivName + shdivName + "合格と決定しましたので通知します。";
            final int len = getMS932ByteLength(text1);
            final String[] text2arr = KNJ_EditEdit.get_token(text2, len, 2);
            String[] textarr = new String[1 + text2arr.length];
            textarr[0] = text1;
            for (int i = 0; i < text2arr.length; i++) {
                textarr[1 + i] = text2arr[i];
            }
            textarr = spaced(textarr, 44, len);
            for (int i = 0; i < textarr.length; i++) {
                svf.VrsOut("TEXT" + String.valueOf(1 + i), textarr[i]);
            }
        } else {
            svf.VrsOut("TEXT1", "　あなたは、本校の" + nendo + "年度入学試験の結果、");
            if ("8".equals(judgediv)) {
                svf.VrsOut("TEXT2", "合格（Ｓ特別選抜）と決定しましたので通知いた");
                svf.VrsOut("TEXT3", "します。");
            } else if ("9".equals(judgediv)) {
                svf.VrsOut("TEXT2", "合格（AB総合選抜）と決定しましたので通知いた");
                svf.VrsOut("TEXT3", "します。");
            } else {
                svf.VrsOut("TEXT2", "合格と決定しましたので通知いたします。");
            }
        }
    }

    private void printWakayamaGokaku(final Vrw32alp svf, final String judgediv, final String specialMeasures) {
        if ("8".equals(judgediv)) {
            svf.VrsOut("TEXT1", "Ｓ選抜クラス合格と決定しましたので通知いた");
            svf.VrsOut("TEXT2", "します。");
        } else if ("9".equals(judgediv) || specialMeasures != null) {
            svf.VrsOut("TEXT1", "総合選抜クラス合格と決定しましたので通知いた");
            svf.VrsOut("TEXT2", "します。");
        } else {
            svf.VrsOut("TEXT1", "合格と決定しましたので通知いたします。");
        }
    }

    private void printCollegeGokaku(final DB2UDB db2, final Vrw32alp svf, final String schoolName, final String judgediv, final String shdiv) {
        final String siken = "2".equals(_param._applicantdiv) ? "編入" : "入学";
//        final String field2 = "2".equals(_param._applicantdiv) ? "_2" : "";
//        final int len2 = "2".equals(_param._applicantdiv) ? 44 : 36;
        final String field2 = "_2";
        final int len2 = 44;
        final String sucCouse;

        if ("2".equals(_param._applicantdiv)) {
            if ("2".equals(_param._outputR)) { //志願者全員
                if ("3".equals(shdiv) || "5".equals(shdiv)) {
                    sucCouse = "ＥＭＡコース";
                } else if ("4".equals(shdiv)) {
                    sucCouse = "ＥＭＳコース";
                } else {
                    sucCouse = "";
                }
            } else {
                if ("A".equals(judgediv)) {
                    sucCouse = "ＥＭＡコース";
                } else if ("B".equals(judgediv)) {
                    sucCouse = "ＥＭＳコース";
                } else {
                    sucCouse = "";
                }
            }
        } else {
            if ("2".equals(_param._outputR)) { //志願者全員
                if ("7".equals(shdiv) || "A".equals(shdiv) || ("8".equals(shdiv) || "B".equals(shdiv)) && "1".equals(_param._sgClassA)) {
                    sucCouse = "Ｓ選抜クラス";
                } else if ("6".equals(shdiv) || "9".equals(shdiv) || ("8".equals(shdiv) || "B".equals(shdiv)) && "2".equals(_param._sgClassA)) {
                    sucCouse = "総合選抜クラス";
                } else {
                    sucCouse = "";
                }
            } else {
                if ("8".equals(judgediv)) {
                    sucCouse = "Ｓ選抜クラス";
                } else if ("9".equals(judgediv)) {
                    sucCouse = "総合選抜クラス";
                } else {
                    sucCouse = "";
                }
            }
        }

        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year));
        final String text = "　あなたは、" + convertSuujiHanToZen(nendo) + "年度" + schoolName + siken + "試験の結果、" + sucCouse + "合格と決定しましたのでここに通知します。";
        final String[] textarr = KNJ_EditEdit.get_token(text, len2, 3);
        for (int i = 0; i < textarr.length; i++) {
            svf.VrsOut("TEXT" + String.valueOf(1 + i) + field2, textarr[i]);
        }
    }

    private void printCollegeNyugaku(final DB2UDB db2, final Vrw32alp svf) {
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year));
        //6:内部日程・・・中学内部生用
        if ("6".equals(_param._testdiv)) {
            svf.VrsOut("TEXT1", "　あなたは、本学園の小学校課程を修了");
            svf.VrsOut("TEXT2", "し、所定の手続きを完了されましたので、");
            svf.VrsOut("TEXT3", "中学部第１学年に入学することを許可しま");
            svf.VrsOut("TEXT4", "す。");
        } else {
            svf.VrsOut("TEXT1", "　あなたは、このたび手続きを完了されまし");
            svf.VrsOut("TEXT2", "たので、" + convertSuujiHanToZen(nendo) + "年度第１学年に入学するこ");
            svf.VrsOut("TEXT3", "とを許可します。");
        }
    }

    private void printWakayamaNyugaku(final Vrw32alp svf) {
        //6:内部日程・・・中学内部生用
        if ("6".equals(_param._testdiv)) {
            svf.VrsOut("TEXT1", "　あなたは、本学園の小学校課程を修了");
            svf.VrsOut("TEXT2", "し、所定の手続きを完了されましたので、");
            svf.VrsOut("TEXT3", "中学校第１学年に入学することを許可しま");
            svf.VrsOut("TEXT4", "す。");
        }
    }

    private void printCollegeTuikaGokaku(final DB2UDB db2, final Vrw32alp svf, final String schoolName) {
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year));
        final String text = "　あなたは、" + convertSuujiHanToZen(nendo) + "年度" + schoolName + "入学試験の結果、追加合格と決定しましたのでここに通知します。";
        final String[] textarr = KNJ_EditEdit.get_token(text, 36, 3);
        for (int i = 0; i < textarr.length; i++) {
            svf.VrsOut("TEXT" + String.valueOf(1 + i), textarr[i]);
        }
    }

    /**
     *  svf print 半角数字を全角数字へ変換(文字単位)
     */
    private String convertSuujiHanToZen(final String suuji) {
        final String arrayZensuuji[] = {"０","１","２","３","４","５","６","７","８","９"};
        final StringBuffer stb = new StringBuffer();
        for (int i = 0; i < suuji.length(); i++) {
            if (Character.isDigit(suuji.charAt(i))) {
                stb.append(arrayZensuuji[Integer.parseInt(suuji.substring(i, i + 1))]);
            } else {
                stb.append(suuji.substring(i, i + 1));
            }
        }
        stb.append("");
        return stb.toString();
    }

    /**
     * 中央寄せに表示するため文字列左にスペースを挿入する
     * @param arr 各行の文字列の配列
     * @param fieldSize 行ごとの文字サイズ
     * @param len 文字列の長さ
     * @return 左にスペースを挿入した各行の文字列の配列
     */
    private String[] spaced(final String[] arr, final int fieldSize, final int len) {
        String spc = "";
        for (int i = 0; i < (fieldSize - len) / 2; i++) {
            spc = " " + spc;
        }
        final String[] rtn = new String[arr.length];
        for (int i = 0; i < arr.length; i++) {
            rtn[i] = spc + arr[i];
        }
        return rtn;
    }


    private String getNameMst(DB2UDB db2, String namecd1,String namecd2) {

        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT NAME1 ");
        sql.append(" FROM NAME_MST ");
        sql.append(" WHERE NAMECD1 = '"+namecd1+"' AND NAMECD2 = '"+namecd2+"' ");
        String name = null;
        try{
            PreparedStatement ps = db2.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
               name = rs.getString("NAME1");
            }
        } catch(SQLException ex) {
            log.debug(ex);
        }

        return name;
    }

    private String getKoutyou() {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME FROM CERTIF_SCHOOL_DAT WHERE CERTIF_KINDCD = ");
        if (_param._applicantdiv.equals("1")) {
            stb.append("'105'");
        } else {
            stb.append("'106'");
        }
        stb.append(" AND YEAR = '"+_param._year+"'");
        return stb.toString();
    }


    private String gethiduke(DB2UDB db2, String inputDate) {
        // 西暦か和暦はフラグで判断
        boolean _seirekiFlg = getSeirekiFlg(db2);
        String date;
        if (null != inputDate) {
            if (_seirekiFlg) {
                //2008年3月3日の形
                date = inputDate.toString().substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(inputDate);
            } else {
                //平成14年10月27日の形
                date = KNJ_EditDate.h_format_JP(db2, inputDate);
            }
            return date;
        }
        return null;
    }
    /* 西暦表示にするのかのフラグ  */
    private boolean getSeirekiFlg(DB2UDB db2) {
        boolean seirekiFlg = false;
        try {
            String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if ( rs.next() ) {
                if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
            }
            ps.close();
            rs.close();
        } catch (Exception e) {
            log.error("getSeirekiFlg Exception", e);
        }
        return seirekiFlg;
    }

    /**
     * 使うフォームの選択
     */
    private String getFormId() {
        String retVal = "";

        if (_param._outputL.equals("1")) { // 合格者
            if (_param.isCollege()) {
                retVal = "KNJL327C_1C.frm";
            } else if (_param.isGojoOrCollege()) {
                retVal = "KNJL327C_1G.frm";
            } else {
                retVal = "KNJL327C_1.frm";
            }
        } else if (_param._outputL.equals("2")) { // 入学者
            if (_param.isCollege()) {
                retVal = "6".equals(_param._testdiv) ? "KNJL327C_2C_2.frm" : "KNJL327C_2C.frm";
            } else if (_param.isGojoOrCollege()) {
                retVal = "KNJL327C_2G.frm";
            } else {
                retVal = "6".equals(_param._testdiv) ? "KNJL327C_2C_2.frm" : "KNJL327C_2.frm";
            }
        } else if (_param._outputL.equals("3")) { // 移行合格
            if (_param._applicantdiv.equals("1")) {  // (中学校フォーム)
                if ("1".equals(_param._shiftSend)) { // [移行先フォーム]
                    retVal = _param.isCollege() ? "KNJL327C_3C_2.frm" : _param.isGojoOrCollege() ? "KNJL327C_3G_2.frm" : _param.isWakayama() ? "KNJL327C_3_2.frm" : "KNJL327C_3_2.frm";
                } else {
                    retVal = _param.isCollege() ? "KNJL327C_3C.frm" : _param.isGojoOrCollege() ? "KNJL327C_3G.frm" : _param.isWakayama() ? "KNJL327C_3.frm" : "KNJL327C_3.frm";
                }
            } else if (_param._applicantdiv.equals("2")) {  // (高校フォーム)
                if ("1".equals(_param._shiftSend)) { // [移行先フォーム]
                    retVal = _param.isGojoOrCollege() ? "KNJL327C_4G_2.frm" : _param.isWakayama() ? "KNJL327C_4_2.frm" : "KNJL327C_4_2.frm";
                } else {
                    retVal = _param.isGojoOrCollege() ? "KNJL327C_4G.frm" : _param.isWakayama() ? "KNJL327C_4.frm" : "KNJL327C_4.frm";
                }
            }
        } else if (_param._outputL.equals("4")) { // 追加合格
            retVal = _param.isCollege() ? "KNJL327C_5C.frm" : _param.isGojoOrCollege() ? "KNJL327C_5G.frm" : _param.isWakayama() ? "KNJL327C_1.frm" : "KNJL327C_5.frm";
        } else if (_param._outputL.equals("5")) { // 補欠合格
            retVal = _param.isCollege() ? "KNJL327C_6C.frm" : _param.isGojoOrCollege() ? "KNJL327C_6G.frm" : _param.isWakayama() ? "KNJL327C_6.frm" : "KNJL327C_6.frm";
        } else if (_param._outputL.equals("6")) { // 五條:進学通知書(入学手続者)
            retVal = _param.isCollege() ? "KNJL327C_7C.frm" : _param.isGojoOrCollege() ? "KNJL327C_7G.frm" : _param.isWakayama() ? "KNJL327C_7.frm" : "KNJL327C_7.frm";
        }

        log.debug("formId = " + retVal);
        return retVal;
    }

    /**
     * SQLの作成
     */
    private String getTuuchiSql() {
        StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     T1.SHDIV, ");
        stb.append("     VALUE(L1.NAME1, '') AS TESTDIVNAME, ");
        stb.append("     '（' || L2.NAME1 || '）' AS SHDIVNAME ");
        stb.append("     ,T1.NAME_KANA ");
        stb.append("     ,L3.ABBV1 AS SEX_NAME ");
        stb.append("     ,T1.BIRTHDAY ");
        stb.append("     ,L4.FINSCHOOL_NAME ");
        stb.append("     ,T3.ADDRESS1 ");
        stb.append("     ,T3.ADDRESS2 ");
        stb.append("     ,T3.GNAME ");
        stb.append("     ,T2.JUDGEDIV ");
        stb.append("     ,T1.SPECIAL_MEASURES ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT T2 ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("                                    AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("                                    AND T2.TESTDIV      = T1.TESTDIV ");
        stb.append("                                    AND T2.EXAMNO       = T1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR AND T3.EXAMNO = T1.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'L004' ");
        stb.append("                              AND L1.NAMECD2 = T1.TESTDIV ");
        stb.append("     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'L006' ");
        stb.append("                              AND L2.NAMECD2 = T1.SHDIV ");
        stb.append("     LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'Z002' AND L3.NAMECD2 = T1.SEX ");
        stb.append("     LEFT JOIN FINSCHOOL_MST L4 ON L4.FINSCHOOLCD = T1.FS_CD ");
        stb.append(" WHERE ");
        stb.append("         T1.ENTEXAMYEAR = '"+ _param._year +"' ");
        stb.append("     AND T1.APPLICANTDIV = '"+ _param._applicantdiv +"' ");
        stb.append("     AND T1.TESTDIV in "+ SQLUtils.whereIn(true, _param._testdivArray) +" ");
        if (!"9".equals(_param._testdiv) && !(_param.isGojoOrCollege() && "1".equals(_param._applicantdiv)) && !(_param.isCollege() && "2".equals(_param._applicantdiv))) {
            stb.append("     AND T1.SHDIV = '"+ _param._shdiv +"' ");
        }
        if (_param._outputL.equals("1")) {                                 //合格通知書
            if ("4".equals(_param._outputR)) { //和歌山中学のS合格者全員
                stb.append(" AND T2.JUDGEDIV = '8' ");
            } else if ("5".equals(_param._outputR)) { //和歌山中学のG合格者全員
                stb.append(" AND T2.JUDGEDIV = '9' ");
            } else if (!"2".equals(_param._outputR)) {
                stb.append(" AND T1.JUDGEMENT = '1' ");
            }
        } else if (_param._outputL.equals("2")) {                         //入学許可書
            if (_param._outputR.equals("4")) { // 入学手続者全員
                stb.append(" AND T1.JUDGEMENT = '1' ");
                stb.append(" AND T1.PROCEDUREDIV = '1' ");
                stb.append(" AND T1.ENTDIV = '1' ");
            } else if (!_param._outputR.equals("2")) {
                stb.append(" AND T1.JUDGEMENT = '1' ");
            }
        }  else if (_param._outputL.equals("3")) {                        //移行希望通知書
            stb.append(" AND T1.SHIFT_DESIRE_FLG  = '1' ");
        } else if (_param._outputL.equals("4")) {                         //追加合格通知書
            stb.append(" AND T1.JUDGEMENT = '1' ");
            stb.append(" AND T1.SPECIAL_MEASURES is not null ");
        } else if (_param._outputL.equals("5")) {                         // 補欠合格者
            stb.append(" AND T2.JUDGEDIV = '3' ");
        } else if (_param._outputL.equals("6")) {                         //五條:進学通知書(入学手続者)
            stb.append(" AND T1.JUDGEMENT = '1' ");
            stb.append(" AND T1.PROCEDUREDIV = '1' ");
            stb.append(" AND T1.ENTDIV = '1' ");
        }
        if (null != _param._examno) {
            stb.append(" AND T1.EXAMNO = '"+ _param._examno +"' ");
        }
        /***
        // 特併合格者は、特進文系コース併願の合格者と一緒に出力する。
        if ((_param.isGojo() && "2".equals(_param._applicantdiv) && "9".equals(_param._testdiv) ||
             _param.isGojo() && "2".equals(_param._applicantdiv) && "7".equals(_param._testdiv) && "2".equals(_param._shdiv)) &&
            (_param._outputL.equals("1") && !_param._outputR.equals("2") ||
             _param._outputL.equals("2") && !_param._outputR.equals("2") ||
             _param._outputL.equals("6"))
        ) {
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T1.NAME, ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     '7' AS TESTDIV, ");
            stb.append("     T1.SHDIV, ");
            stb.append("     VALUE(L1.NAME1, '') AS TESTDIVNAME, ");
            stb.append("     '（' || L2.NAME1 || '）' AS SHDIVNAME ");
            stb.append("     ,T1.NAME_KANA ");
            stb.append("     ,L3.ABBV1 AS SEX_NAME ");
            stb.append("     ,T1.BIRTHDAY ");
            stb.append("     ,L4.FINSCHOOL_NAME ");
            stb.append("     ,T3.ADDRESS1 ");
            stb.append("     ,T3.ADDRESS2 ");
            stb.append("     ,T3.GNAME ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR AND T3.EXAMNO = T1.EXAMNO ");
            stb.append("     LEFT JOIN V_NAME_MST L1 ON  L1.YEAR    = T1.ENTEXAMYEAR ");
            stb.append("                             AND L1.NAMECD1 = 'L004' ");
            stb.append("                             AND L1.NAMECD2 = '7' ");
            stb.append("     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'L006' AND L2.NAMECD2 = T1.SHDIV ");
            stb.append("     LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'Z002' AND L3.NAMECD2 = T1.SEX ");
            stb.append("     LEFT JOIN FINSCHOOL_MST L4 ON L4.FINSCHOOLCD = T1.FS_CD ");
            stb.append(" WHERE ");
            stb.append("         T1.ENTEXAMYEAR = '"+ _param._year +"' ");
            stb.append("     AND T1.APPLICANTDIV = '"+ _param._applicantdiv +"' ");
            stb.append("     AND T1.TESTDIV = '3' ");
            stb.append("     AND T1.JUDGEMENT = '7' ");
            if (_param._outputL.equals("6")) {
                stb.append(" AND T1.PROCEDUREDIV = '1' ");
                stb.append(" AND T1.ENTDIV = '1' ");
            }
            if (null != _param._examno) {
                stb.append(" AND T1.EXAMNO = '"+ _param._examno +"' ");
            }
        }
        ***/
        stb.append(" ORDER BY EXAMNO ");

        log.debug("SQL = " + stb.toString());
        return stb.toString();
    }


    /**
     * パラメータを受け取るクラス
     * <<クラスの説明>>。
     * @author takara
     * @version $Id$
     */
    private class Param {
        private final String _prgid;
        private final String _dbname;
        private final String _year;
        private final String _login_date;
        private final String _applicantdiv;
        private final String _shdiv;
        private final String[] _testdivArray;
        private final String _outputL; // 1:合格通知書 2:入学許可書 3:移行合格通知書 4:追加合格通知書 5:補欠合格通知書
        private final String _outputR;
        private final String _examno;
        private final String _print_date;
        private final String _shiftSend;
        private String _shiftCourse;
        private final String _testdiv;
        private final String _sgClassA; //1:Sクラス 2:Gクラス S/G志願者はコンボで指定したクラスを合格通知書に表示

        private final String _dateVisitSchool;
        private final Integer _visitHour;
        private final Integer _visitMinute;
        private final String _z010SchoolCode;
        private final String _singakuDate;

        Param(
                final DB2UDB db2,
                final HttpServletRequest request
        ) {
            _prgid        = request.getParameter("PRGID");
            _dbname       = request.getParameter("DBNAME");
            _year         = request.getParameter("YEAR");
            _login_date   = request.getParameter("LOGIN_DATE");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _shdiv = request.getParameter("SHDIV");
            _z010SchoolCode = getSchoolCode(db2);
            _testdiv = request.getParameter("TESTDIV");
            if ("9".equals(_testdiv)) {
                if("1".equals(_applicantdiv)) {
                    if (isGojo()) {
                        _testdivArray = new String[]{"1","2","7"};
                    } else {
                        _testdivArray = new String[]{"1","2"};
                    }
                } else if("2".equals(_applicantdiv)) {
                    if (isGojoOrCollege()) {
                        _testdivArray = new String[]{"3","4","5","7"};
                    } else {
                        _testdivArray = new String[]{"3","4","5"};
                    }
                } else {
                    _testdivArray = null;
                }
            } else {
                _testdivArray = new String[]{_testdiv};
            }
            _print_date   = request.getParameter("PRINT_DATE");
            _outputL      = request.getParameter("CHECK_OUTPUT");

            _sgClassA = request.getParameter("SG_CLASS_A");

            /**
             * Paramの初期化、
             * 画面左側のボタンの押された場所などで
             * 微妙に変わってくるので注意
             */
            if (_outputL.equals("1")) { // 合格者
                _outputR      = request.getParameter("CHECK_OUTPUTA");
                if (_outputR.equals("3"))
                {
                    _examno = request.getParameter("EXAMNOA");
                } else {
                    _examno = null;
                }

            } else if (_outputL.equals("2")) { // 入学者
                _outputR      = request.getParameter("CHECK_OUTPUTD");
                if (_outputR.equals("3"))
                {
                    _examno = request.getParameter("EXAMNOD");
                } else {
                    _examno = null;
                }

            } else if (_outputL.equals("3")) { // 移行合格者

                _outputR      = request.getParameter("CHECK_OUTPUTB");
                if (_outputR.equals("2")) {
                    _examno = request.getParameter("EXAMNOB");
                } else {
                    _examno = null;
                }

            } else if (_outputL.equals("4")) { // 追加合格者

                _outputR      = request.getParameter("CHECK_OUTPUTC");
                if (_outputR.equals("2")) {
                    _examno = request.getParameter("EXAMNOC");
                } else {
                    _examno = null;
                }
            } else if (_outputL.equals("5")) { // 補欠合格者
                _outputR     = request.getParameter("CHECK_OUTPUTE");
                if (_outputR.equals("2")) {
                    _examno = request.getParameter("EXAMNOE");
                } else {
                    _examno = null;
                }
            } else if (_outputL.equals("6")) { // 五條:進学通知書(入学手続者)
                _outputR     = request.getParameter("CHECK_OUTPUTF");
                if (_outputR.equals("2")) {
                    _examno = request.getParameter("EXAMNOF");
                } else {
                    _examno = null;
                }
            } else {
                _outputR = null;
                _examno = null;
            }

            _shiftSend = request.getParameter("SHIFT_SEND");
            try{
                String shifuCourse = request.getParameter("SHIFT_COURSE");
                if (shifuCourse != null) {
                    _shiftCourse = new String(shifuCourse.getBytes("Shift_JIS"));
                }
            }catch(Exception ex) {
                log.debug(ex);
            }
            if (_outputL.equals("5")) {
                _dateVisitSchool = request.getParameter("VISIT_DATE");
                _visitHour = Integer.valueOf(request.getParameter("VISIT_HOUR"));
                _visitMinute = Integer.valueOf(request.getParameter("VISIT_MINUTE"));
            } else {
                _dateVisitSchool = null;
                _visitHour = null;
                _visitMinute = null;
            }
            if (_outputL.equals("6")) {
                _singakuDate = request.getParameter("SINGAKU_DATE");
            } else {
                _singakuDate = null;
            }
        }


        private String getSchoolCode(DB2UDB db2) {
            String schoolCode = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   CASE WHEN NAME1 = 'CHIBEN' THEN NAME2 ELSE NULL END AS SCHOOLCODE ");
                stb.append(" FROM ");
                stb.append("   NAME_MST T1 ");
                stb.append(" WHERE ");
                stb.append("   T1.NAMECD1 = 'Z010' ");
                stb.append("   AND T1.NAMECD2 = '00' ");

                PreparedStatement ps = db2.prepareStatement(stb.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    schoolCode = rs.getString("SCHOOLCODE");
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("getSchoolCode Exception", e);
            }
            return schoolCode;
        }

        boolean isGojo() {
            return "30290053001".equals(_z010SchoolCode);
        }

        boolean isGojoOrCollege() {
            return isGojo() || isCollege();
        }

        boolean isWakayama() {
            return "30300049001".equals(_z010SchoolCode);
        }

        boolean isCollege() {
            return "30290086001".equals(_z010SchoolCode);
        }
    }
}//クラスの括り
