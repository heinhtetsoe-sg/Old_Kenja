// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2021/04/22
 * 作成者: Nutec
 *
 */
package servletpack.KNJD;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJObjectAbs;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 *
 *  学校教育システム 賢者 [成績管理]
 *
 *                  ＜ＫＮＪⅮ１８３E＞  通知票印刷
 */
public class KNJD183E {

    private static final Log log = LogFactory.getLog(KNJD183E.class);

    private boolean nonedata = false; //該当データなしフラグ
    private Param _param = null;

    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();
    private static final String SEMEALL = "9";
    private static final String AMIKAKE_ATTR = "Paint=(1,80,2),Bold=1";

    final int PARENT_ADDR           = 0;    //保護者住所  表示オプション
    final int PARENT_NAME           = 1;    //保護者氏名  表示オプション
    final int TSUISHIDOU            = 2;    //学期/学年末評価  追指導を参照オプション
    final int HYOUTEI               = 3;    //学年末評定  表示オプション
    final int ZOUKATANI             = 4;    //修得単位数  増加単位を加算するオプション
    final int TOTAL_SCORE           = 5;    //総点  表示オプション
    final int PERSONAL_AVG          = 6;    //個人平均  表示オプション
    final int CLASS_AVG             = 7;    //学級平均  表示オプション
    final int PRINT_RANK            = 8;    //順位  表示オプション
    final int AVG_RANK_TYPE         = 9;    //「順位出力」  表示オプション  1:「クラス」  2:「学年」
    final int AVG_BASE_TYPE         = 10;   //「基準点」  表示オプション    1:「平均点」  2:「合計点」
    final int PRINT_TEIKIKOUSA      = 11;   //定期考査  表示オプション
    final int PRINT_TEIKIKOUSA_RANK = 12;   //順位  表示オプション
    final int TEIKIKOUSA_RANK_TYPE  = 13;   //「順位出力」 表示オプション   1:「クラス」  2:「学年」
    final int TEIKIKOUSA_BASE_TYPE  = 14;   //「基準点」 表示オプション     1:「平均点」  2:「合計点」
    final int NYUURYOKU_PATTERN     = 15;   //入力枠パターン  1～4
    final String RANK_TYPE_CLASS     = "1"; //順位出力オプション  クラス
    final String RANK_TYPE_GRADE     = "2"; //順位出力オプション  学年
    final String BASE_TYPE_AVG       = "1"; //基準点オプション  平均点
    final String BASE_TYPE_TOTAL     = "2"; //基準点オプション  合計点

    private static int GAPPEISAKI = 2;
    private static int GAPPEIMOTO = 1;
    private static int GAPPEINASI = 0;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response)
                     throws ServletException, IOException
    {
        final Vrw32alp svf = new Vrw32alp();   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

        PrintWriter outstrm = null;
        try {
            //  print設定
            response.setContentType("application/pdf");
            outstrm = new PrintWriter (response.getOutputStream());

            //  svf設定
            svf.VrInit();  //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());       //PDFファイル名の設定

            sd.setSvfInit(request, response, svf);
            db2 = sd.setDb(request);
            if (sd.openDb(db2)) {
                log.error("db open error! ");
                return;
            }

            _param = getParam(db2, request);

            printSvfMain(db2, svf);

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            //  該当データ無し
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            //  終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();                //DBを閉じる
            outstrm.close();            //ストリームを閉じる
        }

    }//doGetの括り

    private static int toInt(final String s, final int def) {
        if (!NumberUtils.isDigits(s)) {
            return def;
        }
        return Integer.parseInt(s);
    }

    private void setForm(final Vrw32alp svf, final String form, final int data) {
        svf.VrSetForm(form, data);
        if (_param._isOutputDebug) {
            log.info(" form = " + form);
        }
    }

    /** 帳票出力 **/
    private void printSvfMain(
        final DB2UDB db2,
        final Vrw32alp svf
    ) {

        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql   = null;

        //校章画像
        String schoolstampPath = null;
        final File schoolstampFile = _param.getImageFile("SCHOOLSTAMP_H.bmp");
        if (null != schoolstampFile) {
            schoolstampPath = schoolstampFile.getAbsolutePath();
        }

        //学級平均画像
        String avgstampPath = null;
        final File avgstampFile = _param.getImageFile("HR_AVE.jpg");
        if (null != avgstampFile) {
            avgstampPath = avgstampFile.getAbsolutePath();
        }

        try {

            ArrayList<String> option = new ArrayList<String>();
            ArrayList<ArrayList<String>> fotter    = new ArrayList<ArrayList<String>>();
            ArrayList<String> work;  //一時保管用

            //オプション情報
            sql = sqlOption();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                option.add(rs.getString("REMARK1"));

                if (   "009".equals(rs.getString("SEQ"))
                    || "011".equals(rs.getString("SEQ"))) {
                    option.add(rs.getString("REMARK2"));
                    option.add(rs.getString("REMARK3"));
                }
            }

            //修得上限値
            sql = sqlSyutoku();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            int syutokuBunsi = 0;
            int syutokuBunbo = 0;
            int houteiSyusuSemester1 = 0;
            int houteiSyusuSemester2 = 0;
            int jituSyusu = 0;
            String offdaysFlg  = "0";
            String absentFlg   = "0";
            String suspendFlg  = "0";
            String mourningFlg = "0";
            String virusFlg    = "0";
            while (rs.next()) {
                syutokuBunsi = toInt(rs.getString("SYUTOKU_BUNSI"), 0);
                syutokuBunbo = toInt(rs.getString("SYUTOKU_BUNBO"), 0);
                houteiSyusuSemester1 = toInt(rs.getString("HOUTEI_SYUSU_SEMESTER1"), 0);
                houteiSyusuSemester2 = toInt(rs.getString("HOUTEI_SYUSU_SEMESTER2"), 0);
                jituSyusu   = toInt(rs.getString("JITU_SYUSU"), 0);
                offdaysFlg  = rs.getString("SUB_OFFDAYS");
                absentFlg   = rs.getString("SUB_ABSENT");
                suspendFlg  = rs.getString("SUB_SUSPEND");
                mourningFlg = rs.getString("SUB_MOURNING");
                virusFlg    = rs.getString("SUB_VIRUS");
            }

            //生徒情報
            final List studentList = getList(db2);

            //保護者住所(優先)
            sql = sqlAddr1();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ArrayList<Address1> addr1 = new ArrayList<Address1>();
            while (rs.next()) {
                addr1.add(new Address1(
                        rs.getString("SCHREGNO")    //学籍番号
                      , rs.getString("SEND_ZIPCD")  //保護者郵便番号
                      , rs.getString("SEND_ADDR1")  //保護者住所1
                      , rs.getString("SEND_ADDR2")  //保護者住所2
                      , rs.getString("SEND_NAME")   //保護者氏名
                ));
            }

            //保護者住所(優先じゃない)
            sql = sqlAddr2();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ArrayList<Address2> addr2 = new ArrayList<Address2>();
            while (rs.next()) {
                addr2.add(new Address2(
                        rs.getString("SCHREGNO")     //学籍番号
                      , rs.getString("GUARD_ZIPCD")  //保護者郵便番号
                      , rs.getString("GUARD_ADDR1")  //保護者住所1
                      , rs.getString("GUARD_ADDR2")  //保護者住所2
                      , rs.getString("GUARD_NAME")   //保護者氏名
                ));
            }

            //出欠の記録、備考
            Attendance.load(db2, _param, studentList);

            //フッター、校長名、担任名
            sql = sqlFotter();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                work = new ArrayList<String>();
                work.add(rs.getString("SCHREGNO"));        //学籍番号
                work.add(rs.getString("PRINCIPAL_NAME"));  //校長名
                work.add(rs.getString("STAFFNAME"));       //担任名
                fotter.add(work);
            }

            //帳票に印字
            String schregno   = "";      //学籍番号
            boolean handicap = false;  //特別な支援対象者かどうか
            boolean flg       = false;  //保護者住所に優先項目を入れたかどうか
            final int MAX_SUBCLASS_CNT = 20;  //印字する最大科目数

            for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
                final Student student = (Student) iterator.next();

                //学籍番号を保持
                schregno = student._schregno;
                //SCHREG_BASE_MST.HANDICAP = 002の場合
                if ((student._handicap != null) && ("002".equals(student._handicap))) {
                    handicap = true;
                } else {
                    handicap = false;
                }

                String xmlFileName = getXmlFileName(option, handicap);
                setForm(svf, xmlFileName, 4);

                //校章
                svf.VrsOut("SCHOOL_LOGO", schoolstampPath);

                //網掛け
                svf.VrAttribute("HATCH", AMIKAKE_ATTR);

                //年度
                //日付の和暦変換に使用
                Calendar calendar = Calendar.getInstance();
                calendar.set(toInt(_param._year, 0), 4, 1);
                String dateStr = KNJ_EditDate.gengou(db2, calendar.get(Calendar.YEAR)) + "年度";
                svf.VrsOut("NENDO", dateStr);

                //生徒情報
                String attendno = String.valueOf(toInt(student._attendno, 0)) + "番";

                //学校名
                svf.VrsOut("SCHOOL_NAME", student._schoolName);

                //学科名
                svf.VrsOut("MAJORNAME",   student._majorname);

                //年組番
                svf.VrsOut("HR_NAME",     student._schreg + attendno);

                //氏名
                svf.VrsOut("NAME1",       student._name);

                //保護者住所
                String zipcd = "";
                String parentAddr1 = "";
                String parentAddr2 = "";
                String parentName  = "";
                for (int i = 0; i < addr1.size(); i++) {
                    if (schregno.equals(addr1.get(i)._schregno)) {
                        if (   addr1.get(i)._sendZipcd == null
                            && addr1.get(i)._sendAddr1 == null
                            && addr1.get(i)._sendAddr2 == null) {
                            //優先にデータがない場合
                            flg = false;
                            break;
                        } else {
                            //優先
                            //保護者郵便番号
                            zipcd = addr1.get(i)._sendZipcd;

                            //保護者住所1
                            parentAddr1 = addr1.get(i)._sendAddr1;

                            //保護者住所2
                            parentAddr2 = addr1.get(i)._sendAddr2;

                            //保護者氏名
                            parentName = addr1.get(i)._sendName + "　様";

                            flg = true;
                            break;
                        }
                    }
                }

                if (flg == false) {  //優先データがない場合こっちを入れる
                    for (int i = 0; i < addr2.size(); i++) {
                        if (schregno.equals(addr2.get(i)._schregno)) {
                            //保護者郵便番号
                            zipcd = addr2.get(i)._guardZipcd;

                            //保護者住所1
                            parentAddr1 = addr2.get(i)._guardAddr1;

                            //保護者住所2
                            parentAddr2 = addr2.get(i)._guardAddr2;

                            //保護者氏名
                            parentName = addr2.get(i)._guardName + "　様";

                            break;
                        }
                    }
                }

                if ("1".equals(option.get(PARENT_ADDR))) {  //保護者住所  表示オプション
                    //保護者郵便番号
                    svf.VrsOut("ZIPCD", zipcd);

                    //保護者住所1
                    {
                        final int parentAddr1Len = KNJ_EditEdit.getMS932ByteLength(parentAddr1);
                        final String parentAddr1Field = (parentAddr1Len <= 40)? "_1": (parentAddr1Len <= 50)? "_2": "_3";
                        svf.VrsOut("ADDR1" + parentAddr1Field, parentAddr1);
                    }

                    //保護者住所2
                    {
                        final int parentAddr2Len = KNJ_EditEdit.getMS932ByteLength(parentAddr2);
                        final String parentAddr2Field = (parentAddr2Len <= 40)? "_1": (parentAddr2Len <= 50)? "_2": "_3";
                        svf.VrsOut("ADDR2" + parentAddr2Field, parentAddr2);
                    }
                }
                if ("1".equals(option.get(PARENT_NAME))) {  //保護者氏名  表示オプション
                    //保護者氏名
                    {
                        final int parentNameLen = KNJ_EditEdit.getMS932ByteLength(parentName);
                        final String parentNameField = (parentNameLen <= 40)? "_1": (parentNameLen <= 50)? "_2": "_3";
                        svf.VrsOut("GUARD_NAME1" + parentNameField, parentName);
                    }
                }

                ArrayList<String> subclassOrder = new ArrayList<String>();
                ArrayList<String> subclassAbbvOrder = new ArrayList<String>();
                //学習成績
                //科目毎の評価、欠課
                String grpcd = "";
                int groupcdCnt = 0;
                for (int subCnt = 0; subCnt < student.subclass.size(); subCnt++) {
                    if (MAX_SUBCLASS_CNT <= subCnt) {
                        break;
                    }
                    String key = student.subclass.get(subCnt);
                    final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(key);
                    svf.VrsOut("GRPCD", scoreData._classcd);  //教科CD
                    if (grpcd.equals(scoreData._classcd) == false) {
                        grpcd = scoreData._classcd;
                        groupcdCnt = 0;
                    }

                    //教科名
                    {
                        String[] splitClassName = student._classNameMap.get(grpcd);
                        svf.VrsOut("CLASS_NAME" + student._classNameFieldMap.get(grpcd), splitClassName[groupcdCnt]);
                    }

                    groupcdCnt++;
                    //科目名
                    {
                        String subclassName = scoreData._subclassname;
                        int subclassNameLen = KNJ_EditEdit.getMS932ByteLength(subclassName);

                        final String subclassNameField = (subclassNameLen <= 6 * 2)? "1":
                                                                (subclassNameLen <= 8 * 2)? "2":
                                                                    (subclassNameLen <= 10 * 2)? "3":
                                                                            (subclassNameLen <= 12 * 2)? "4":
                                                                                (subclassNameLen <= 14 * 2)? "5":
                                                                                    (subclassNameLen <= 15 * 2)? "6": "7";
                        svf.VrsOut("SUBCLASS_NAME" + subclassNameField, subclassName);
                        if (subclassOrder.indexOf(scoreData._subclasscd) < 0) {
                            subclassOrder.add(scoreData._subclasscd);        //定期考査の科目の順番に使用
                            subclassAbbvOrder.add(scoreData._subclassabbv);  //定期考査の科目名に使用
                        }
                    }

                    //単位
                    String credits = scoreData._credits;
                    svf.VrsOut("CREDITS", credits);

                    String semester = "";
                    int totalNotice = 0;
                    int totalNurseoff = 0;
                    int totalOffdays  = 0;
                    int totalabsent   = 0;
                    int totalSuspend  = 0;
                    int totalMourning = 0;
                    int totalVirus    = 0;
                    int gappei = toInt(scoreData.gappei(key), 0);
                    for (int semCnt   = 0; semCnt < student.semester.size(); semCnt++) {
                        semester = student.semester.get(semCnt);
                        //科目毎の評価
                        String score = scoreData.score(semester);
                        if ("1".equals(option.get(TSUISHIDOU))) {  //学期/学年末評価  追指導を参照オプション
                            if (scoreData.tuishi(semester).isEmpty() == false) {
                                //追試評価
                                score = scoreData.tuishi(semester);
                            }
                        }
                        double keikokuten = Double.parseDouble(scoreData.keikokuten(semester));
                        if (semester.equals("9")) {
                            boolean noticeAmikake = false;
                            int kekka = totalNotice + totalNurseoff + totalOffdays + totalabsent + totalSuspend
                                       + totalMourning + totalVirus;
                            float resultSyutoku;
                            resultSyutoku = jituSyusu * (toInt(credits, 0) * syutokuBunsi / (float)syutokuBunbo);
                            if (   (syutokuBunsi != 0 && syutokuBunbo != 0 && jituSyusu != 0)
                                && (resultSyutoku < kekka)) {
                                //修得上限値を超えた場合網掛けにする
                                noticeAmikake = true;
                            }

                            //評価、欠課
                            final String scoreFiled = "1_9_1";
                            printHyoukaKekka(svf, scoreFiled, semester, gappei, score, keikokuten, String.valueOf(totalNotice), noticeAmikake);
                        } else {
                            String notice = scoreData.notice(semester);
                            totalNotice += toInt(notice, 0);
                            boolean noticeAmikake = false;
                            int syusu = 0;
                            if (semester.equals("1")) {
                                syusu = houteiSyusuSemester1;
                            } else if (semester.equals("2")) {
                                syusu = houteiSyusuSemester2;
                            }
                            int kekka = toInt(notice, 0) + toInt(scoreData.nurseoff(semester), 0);
                            totalNurseoff += toInt(scoreData.nurseoff(semester), 0);
                            float resultSyutoku;
                            resultSyutoku = syusu * (toInt(credits, 0) * syutokuBunsi / (float)syutokuBunbo);
                            if ("1".equals(offdaysFlg)) {
                                kekka += toInt(scoreData.offdays(semester), 0);
                                totalOffdays += toInt(scoreData.offdays(semester), 0);
                            }
                            if ("1".equals(absentFlg)) {
                                kekka += toInt(scoreData.absent(semester), 0);
                                totalabsent += toInt(scoreData.absent(semester), 0);
                            }
                            if ("1".equals(suspendFlg)) {
                                kekka += toInt(scoreData.suspend(semester), 0);
                                totalSuspend += toInt(scoreData.suspend(semester), 0);
                            }
                            if ("1".equals(mourningFlg)) {
                                kekka += toInt(scoreData.mourning(semester), 0);
                                totalMourning += toInt(scoreData.mourning(semester), 0);
                            }
                            if ("1".equals(virusFlg)) {
                                kekka += toInt(scoreData.virus(semester), 0);
                                totalVirus += toInt(scoreData.virus(semester), 0);
                            }

                            if (   (syutokuBunsi != 0 && syutokuBunbo != 0 && syusu != 0)
                                && (resultSyutoku < kekka )) {
                                //修得上限値を超えた場合網掛けにする
                                noticeAmikake = true;
                            }

                            //評価、欠課
                            final String scoreFiled = "1_" + semester;
                            printHyoukaKekka(svf, scoreFiled, semester, gappei, score, keikokuten, notice, noticeAmikake);
                        }
                    }

                    if (semester.equals("9")) {
                        //評定、修得単位数
                        //評定
                        final SyutokuData syutokueData = (SyutokuData) student._syutokuMap.get(key);
                        if (syutokueData != null) {
                            if ("1".equals(option.get(HYOUTEI))) {  //学年末評定  表示オプション
                                //評定を表示する
                                //合併元科目は印字しない
                                if (gappei != GAPPEIMOTO) {
                                    String hyoutei = syutokueData.score(key);
                                    svf.VrsOut("SCORE1_9_2", hyoutei);
                                    if (hyoutei.equals("1")) {
                                        svf.VrAttribute("SCORE1_9_2", AMIKAKE_ATTR);
                                    }
                                }
                            }
                            //修得単位数
                            String credit = syutokueData.getCredit(key);
                            if ("1".equals(option.get(ZOUKATANI))) {  //修得単位数  増加単位を加算するオプション
                                //増加単位を加算する
                                credit    = String.valueOf(toInt(credit, 0) + toInt(syutokueData.addCredit(key), 0));
                            }
                            svf.VrsOut("GET_CREDIT", credit);
                            if (credit.equals("1")) {
                                svf.VrAttribute("GET_CREDIT", AMIKAKE_ATTR);
                            }
                        }
                    }
                    if (subCnt < (MAX_SUBCLASS_CNT - 1)) {
                        svf.VrEndRecord();
                    }
                }  //subCntの括り

                //総点、平均、順位
                int row = 0;
                for (int semCnt = 0; semCnt < student.semester.size(); semCnt++) {
                    int col = 0;
                    int sem = toInt(student.semester.get(semCnt), 0);
                    if (sem == 3) {
                        continue;  //3学期は印字しない
                    }
                    row++;
                    if ("1".equals(option.get(TOTAL_SCORE))) {  //総点  表示オプション
                        col++;
                        svf.VrsOut("TOTAL_AVE_RANK_TITLE" + col, "総点");
                        //総点
                        svf.VrsOutn("TOTAL_AVE_RANK" + col, row, student._totalScoreMap.get(String.valueOf(sem)));
                    }
                    if (   "1".equals(option.get(PERSONAL_AVG))     //個人平均  表示オプション
                        || "1".equals(option.get(CLASS_AVG))) {   //学級平均  表示オプション
                        col++;
                        svf.VrsOut("TOTAL_AVE_RANK_TITLE" + col, "平均");
                        if ("1".equals(option.get(PERSONAL_AVG))) { //個人平均  表示オプション
                            //平均点
                            String type = "";
                            if (col != 1) {
                                type = "_1";
                            }

                            if (   student._totalAvgMap.get(String.valueOf(sem)) != null
                                && student._totalAvgMap.containsKey((String.valueOf(sem)))) {
                                svf.VrsOutn("TOTAL_AVE_RANK" + col + type, row, RoundHalfUp(student._totalAvgMap.get(String.valueOf(sem)).toString()));
                            }
                        }
                        if ("1".equals(option.get(CLASS_AVG))) {  //学級平均  表示オプション
                            svf.VrsOut("TOTAL_AVE_RANK_TITLE" + col, "平均");
                            //学級平均表示
                            svf.VrsOut("AVG" + col, avgstampPath);

                            //学級平均
                            if (student._classAvgMap.get(String.valueOf(sem)) != null) {
                                svf.VrsOutn("TOTAL_AVE_RANK" + col + "_2", row, "[" + RoundHalfUp(student._classAvgMap.get(String.valueOf(sem)).toString()) + "]");
                            }
                        }
                    }
                    if ("1".equals(option.get(PRINT_RANK))) {  //順位  表示オプション
                        col++;
                        svf.VrsOut("TOTAL_AVE_RANK_TITLE" + col, "順位");
                        String rank  = "";
                        String count = "";
                        Rank AvgRank = null;
                        if (RANK_TYPE_CLASS.equals(option.get(AVG_RANK_TYPE))) {       //「順位出力」で「クラス」  表示オプション
                            if (BASE_TYPE_AVG.equals(option.get(AVG_BASE_TYPE))) {  //「基準点」で「平均点」    表示オプション
                                AvgRank = (Rank) student._classAvgRankMap.get(String.valueOf(sem));
                            } else if (BASE_TYPE_TOTAL.equals(option.get(AVG_BASE_TYPE))) {  //「基準点」で「合計点」  表示オプション
                                AvgRank = (Rank) student._classRankMap.get(String.valueOf(sem));
                            }
                        } else if (RANK_TYPE_GRADE.equals(option.get(AVG_RANK_TYPE))) {  //「順位出力」で「学年」  表示オプション
                            if (BASE_TYPE_AVG.equals(option.get(AVG_BASE_TYPE))) {   //「基準点」で「平均点」  表示オプション
                                AvgRank = (Rank) student._gradeAvgRankMap.get(String.valueOf(sem));
                            } else if (BASE_TYPE_TOTAL.equals(option.get(AVG_BASE_TYPE))) {  //「基準点」で「合計点」  表示オプション
                                AvgRank = (Rank) student._gradeRankMap.get(String.valueOf(sem));
                            }
                        }
                        if (AvgRank != null) {
                            rank  = AvgRank._rank;
                            count = AvgRank._count;
                        }
                        String type = "";
                        if (col != 1) {
                            type = "_1";
                        }

                        //順位(分子)
                        svf.VrsOutn("TOTAL_AVE_RANK" + col + type,     row, rank);

                        svf.VrsOutn("TOTAL_AVE_RANK" + col + "_SLASH", row, "/");

                        //順位(分母)
                        svf.VrsOutn("TOTAL_AVE_RANK" + col + "_4",     row, count);
                    }
                }  //semCntの括り

                ArrayList testTypeCdList = new ArrayList();
                if ("1".equals(option.get(PRINT_TEIKIKOUSA))) {  //定期考査  表示オプション
                    //定期考査科目名表示
                    for (int sub = 0; sub < subclassOrder.size(); sub++) {
                        for (Object subclasscd : student._teikikousaMap.keySet()) {
                            final TeikikousaData teikikousaData = (TeikikousaData) student._teikikousaMap.get(subclasscd);
                            if (teikikousaData == null) {
                                continue;
                            }
                            if (subclasscd.equals(subclassOrder.get(sub))) {
                                //科目名
                                svf.VrsOutn("SUBCLASSNAMEABBV", sub + 1, teikikousaData._subclassname);
                            } else {
                                //科目名
                                svf.VrsOutn("SUBCLASSNAMEABBV", sub + 1, subclassAbbvOrder.get(sub));
                            }
                            Object[] mapkey = teikikousaData._scoreMap.keySet().toArray();
                            Arrays.sort(mapkey);
                            for (int i = 0; i < mapkey.length; i++) {
                                if (testTypeCdList.contains(mapkey[i]) == false) {
                                    testTypeCdList.add(mapkey[i]);
                                }
                            }
                        }
                    }
                    //定期考査の成績
                    for (int sub = 0; sub < subclassOrder.size(); sub++) {
                        for (Object subclasscd : student._teikikousaMap.keySet()) {
                            final TeikikousaData teikikousaData = (TeikikousaData) student._teikikousaMap.get(subclasscd);
                            if (teikikousaData == null) {
                                continue;
                            }
                            int testTypeCount = 1;
                            int count = 1;
                            for (int i = 0; i < testTypeCdList.size(); i++) {
                                String semTest = (String)testTypeCdList.get(i);

                                if (teikikousaData.testName(semTest) != null && "".equals(teikikousaData.testName(semTest)) == false) {
                                    //試験種別名
                                    svf.VrsOut( "TESTITEMNAME" + testTypeCount + "_" + count,  teikikousaData.testName(semTest));
                                }
                                if (   subclasscd != null
                                    && subclasscd.equals(subclassOrder.get(sub))) {
                                    //素点
                                    svf.VrsOutn("SCORE2_" + testTypeCount + "_" + count, sub + 1, teikikousaData.score(semTest));
                                }
                                count++;
                                if (count == 3) {
                                    testTypeCount++;
                                    count = 1;
                                }
                            }
                        }
                    }

                    //定期考査の成績、生徒毎の平均点
                    row = 0;
                    svf.VrsOut("AVE_NAME", "※学級平均");
                    for (Object testType : student._teikiTotalAvgMap.keySet()) {
                        String semTest = String.valueOf(testType);
                        row++;

                        //平均点
                        if (student._teikiTotalAvgMap.get(semTest) != null) {
                            svf.VrsOutn("AVG2_1", row, RoundHalfUp(student._teikiTotalAvgMap.get(semTest).toString()));
                        }

                        if(student._teikiTotalClassAvgMap.get(semTest) != null) {
                            //学級平均
                            svf.VrsOutn("AVG2_2", row, "[" + RoundHalfUp(student._teikiTotalClassAvgMap.get(semTest).toString()) + "]");
                        }
                        Rank AvgRank = null;
                        String rank = "";
                        String count = "";
                        if ("1".equals(option.get(PRINT_TEIKIKOUSA_RANK))) {  //順位  表示オプション
                            if (RANK_TYPE_CLASS.equals(option.get(TEIKIKOUSA_RANK_TYPE))) {  //「順位出力」で「クラス」  表示オプション
                                if (BASE_TYPE_AVG.equals(option.get(TEIKIKOUSA_BASE_TYPE))) {  //「基準点」で「平均点」  表示オプション
                                    AvgRank = (Rank) student._teikiClassAvgRankMap.get(semTest);
                                } else if (BASE_TYPE_TOTAL.equals(option.get(TEIKIKOUSA_BASE_TYPE))) {  //「基準点」で「合計点」  表示オプション
                                    AvgRank = (Rank) student._teikiClassRankMap.get(semTest);
                                }
                            } else if (RANK_TYPE_GRADE.equals(option.get(TEIKIKOUSA_RANK_TYPE))) {  //「順位出力」で「学年」  表示オプション
                                if (BASE_TYPE_AVG.equals(option.get(TEIKIKOUSA_BASE_TYPE))) {  //「基準点」で「平均点」  表示オプション
                                    AvgRank = (Rank) student._teikiGradeAvgRankMap.get(semTest);
                                } else if (BASE_TYPE_TOTAL.equals(option.get(TEIKIKOUSA_BASE_TYPE))) {  //「基準点」で「合計点」  表示オプション
                                    AvgRank = (Rank) student._teikiGradeRankMap.get(semTest);
                                }
                            }
                        }
                        if (AvgRank != null) {
                            rank  = AvgRank._rank;
                            count = AvgRank._count;
                        }
                        //順位
                        svf.VrsOutn("RANK2_1_2", row, rank);

                        //人数
                        svf.VrsOutn("RANK2_1_3", row, count);
                    }
                }

                //自立活動、総合的な探求の時間、取得資格・検定、所見
                String name = student._midashi == null ? "総合的な探究の時間" : student._midashi;
                int width = 0;
                if (handicap == true) {
                    //自立活動(改行して印字)
                    printReport(svf, "ACTIVE",  student._jiritsu, 40, 4);
                }
                if ("1".equals(option.get(NYUURYOKU_PATTERN))) {        //入力枠パターン  「総探のみ使用」オプション
                    if (handicap == false) {
                        width = 110;
                    } else {
                        width = 70;
                    }

                    //総合的な探求の時間(改行して印字)
                    printReport(svf, "TOTAL_HREPORT",  student._totalStudyTime, width, 4);
                } else if ("2".equals(option.get(NYUURYOKU_PATTERN))) {  //入力枠パターン  「資格・検定のみ使用」オプション
                    if (handicap == false) {
                        width = 110;
                    } else {
                        width = 70;
                    }

                    //資格取得・検定(改行して印字)
                    printReport(svf, "TOTAL_HREPORT",  student._shikaku, width, 4);
                    name = "取得資格・検定（今年度）";
                } else if ("3".equals(option.get(NYUURYOKU_PATTERN))) {  //入力枠パターン  「両方使用」オプション
                    if (handicap == false) {
                        width = 50;
                    } else {
                        width = 30;
                    }

                    //総合的な探求の時間(改行して印字)
                    printReport(svf, "TOTALSTUDYTIME",  student._totalStudyTime, width, 4);

                    //資格取得・検定(改行して印字)
                    printReport(svf, "HREPORTREMARK",  student._shikaku, width, 4);
                }
                //帳票のパターンによりフィールド名が違うがどちらにも見出しを入れる
                //みだし
                svf.VrsOut("TOTAL_HREPORT_NAME",  name);

                //みだし
                svf.VrsOut("TOTALSTUDYTIME_NAME", name);

                //所見(改行して印字)
                printReport(svf, "COMMUNICATION",  student._communication, 50, 6);

                //出欠の記録、備考
                int suspend = 0;
                int ketsuji = 0;
                int totalLesson = 0;
                int totalSuspend = 0;
                int totalMust    = 0;
                int totalNotice  = 0;
                int totalPresent = 0;
                int totalLate    = 0;
                int totalEarly   = 0;
                int totalKetsuji = 0;
                int printLesson  = 0;
                int printSuspend = 0;
                int printMust    = 0;
                int printNotice  = 0;
                int printPresent = 0;
                int printLate    = 0;
                int printEarly   = 0;

                for (Object semester : student._attendMap.keySet()) {
                    int sem = toInt(String.valueOf(semester), 0);
                    final Attendance attendance = (Attendance) student._attendMap.get(semester);
                    suspend =  attendance._suspend + attendance._virus + attendance._koudome + attendance._mourning;

                    totalLesson  += attendance._lesson;
                    totalSuspend += suspend;
                    totalMust    += attendance._mLesson;
                    totalNotice  += attendance._sick;
                    totalPresent += attendance._present;
                    totalLate    += attendance._late;
                    totalEarly   += attendance._early;

                    //3行目には1～3学期の合計を学年末として印字する
                    //授業日数
                    printLesson = sem != 3 ? attendance._lesson : totalLesson;
                    svf.VrsOutn("LESSON",  sem, String.valueOf(printLesson));

                    //出停・忌引等の日数
                    printSuspend = sem != 3 ? suspend : totalSuspend;
                    svf.VrsOutn("SUSPEND", sem, String.valueOf(printSuspend));

                    //出席すべき日数
                    printMust = sem != 3 ? attendance._mLesson : totalMust;
                    svf.VrsOutn("MUST",    sem, String.valueOf(printMust));

                    //欠席日数
                    printNotice = sem != 3 ? attendance._sick : totalNotice;
                    svf.VrsOutn("NOTICE",  sem, String.valueOf(printNotice));

                    //出席日数
                    printPresent = sem != 3 ? attendance._present : totalPresent;
                    svf.VrsOutn("PRESENT", sem, String.valueOf(printPresent));

                    //遅刻日数
                    printLate = sem != 3 ? attendance._late : totalLate;
                    svf.VrsOutn("LATE",    sem, String.valueOf(printLate));

                    //早退日数
                    printEarly = sem != 3 ? attendance._early : totalEarly;
                    svf.VrsOutn("EARLY",   sem, String.valueOf(printEarly));

                    //欠時
                    totalKetsuji += toInt(String.valueOf(student._ketsujiMap.get(semester)), 0);
                    ketsuji = sem != 3 ? toInt(String.valueOf(student._ketsujiMap.get(semester)), 0) : totalKetsuji;

                    if (   printNotice == 0
                        && printLate   == 0
                        && printEarly  == 0
                        && ketsuji     == 0) {

                        //備考
                        svf.VrsOutn("ATTEND_REMARK", sem, "皆勤");
                    } else if (   (student._bikouMap.get(semester) != null)
                                && ("".equals(String.valueOf(student._bikouMap.get(semester))) == false)) {
                        //備考
                        svf.VrsOutn("ATTEND_REMARK", sem, String.valueOf(student._bikouMap.get(semester)));
                    }
                }

                //フッター、校長名、担任名
                for (int i = 0; i < fotter.size(); i++) {
                    if (((ArrayList<String>)fotter.get(i)).get(0).equals(schregno)) {
                        //校長名
                        {
                            final String principalName = ((ArrayList<String>)fotter.get(i)).get(1);
                            final int principalNameLen = KNJ_EditEdit.getMS932ByteLength(principalName);
                            final String principalNameField = (principalNameLen <= 20)? "1": (principalNameLen <= 30)? "2": "3";
                            svf.VrsOut("PRINCIPAL_NAME" + principalNameField, principalName);
                        }

                        //担任名
                        {
                            final String staffName = ((ArrayList<String>)fotter.get(i)).get(2);
                            final int staffNameLen = KNJ_EditEdit.getMS932ByteLength(staffName);
                            final String staffNameField = (staffNameLen <= 20)? "1": (staffNameLen <= 30)? "2": "3";
                            svf.VrsOut("STAFFNAME" + staffNameField, staffName);
                        }
                        break;
                    }
                }
                svf.VrEndRecord();
                nonedata = true;
                svf.VrEndPage();
            }//iteratorの括り

            if (nonedata) {
                svf.VrEndPage();
                svf.VrsOut("DUMMY", "　");
                svf.VrEndPage();
            }

        } catch (Exception ex) {
            log.error("printSvfMain set error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        if (nonedata) {
            svf.VrEndPage();
        }
    }

    /**セットするmxlファイル名を作成**/
    private String getXmlFileName(ArrayList<String> option, boolean handicap)
    {
        //帳票の分岐
        String programID = "KNJD183E";
        String xmlFileName = "";
        String p1 = "";
        int p2 = 0;
        String p3 = "";
        String p4 = "";
        int colCnt  = 0;
        if (handicap == false) {
            //Aパターン
            p1 = "A";
        } else {
            //Bパターン
            p1 = "B";
        }

        //学習成績の右列(総点、平均、順位)
        if ("1".equals(option.get(TOTAL_SCORE))) {     //総点  表示オプション
            colCnt++;
        }
        if (   "1".equals(option.get(PERSONAL_AVG))     //個人平均  表示オプション
            || "1".equals(option.get(CLASS_AVG))) {   //学級平均  表示オプション
            colCnt++;
        }
        if ("1".equals(option.get(PRINT_RANK))) {  //順位  表示オプション
            colCnt++;
        }

        switch (colCnt) {
            case 3:
                p2 = 1;
                break;
            case 2:
                p2 = 3;
                break;
            case 1:
                p2 = 5;
                break;
        }
        //定期考査
        if ("1".equals(option.get(PRINT_TEIKIKOUSA))) {      //定期考査  表示オプション
            if ("1".equals(option.get(PRINT_TEIKIKOUSA_RANK))) {  //順位  表示オプション
                p3 = "1";
            } else {
                p3 = "2";
            }
        } else {
            p2++;
            p3 = "1";
        }
        if ("1".equals(option.get(HYOUTEI)) == false) {  //学年末評定  表示オプション
            p2 += 10;
        }
        if (   "1".equals(option.get(NYUURYOKU_PATTERN))     //入力枠パターン  「総探のみ使用」オプション
                || "2".equals(option.get(NYUURYOKU_PATTERN))) {  //入力枠パターン  「資格・検定のみ使用」オプション
            p4 = "1";
        } else if ("3".equals(option.get(NYUURYOKU_PATTERN))) {  //入力枠パターン  「両方使用」オプション
            p4 = "2";
        } else if ("4".equals(option.get(NYUURYOKU_PATTERN))) {  //入力枠パターン  「両方表示しない」オプション
            p4 = "3";
        }
        xmlFileName = programID + "_" + p1 + "_" + p2 + "_" + p3 + "_" + p4 + ".xml";

        return xmlFileName;
    }

    //評価と欠課を印字する
    private void printHyoukaKekka(
            final Vrw32alp svf,
            final String scoreFiled,
            final String noticeFiled,
            final int gappei,
            final String score,
            final double keikokuten,
            final String notice,
            final boolean noticeAmikake) {
        //評価
        //合併元科目のみ印字(合併先科目は空欄)
        if (gappei != GAPPEISAKI) {
            svf.VrsOut("SCORE" + scoreFiled, score);

            //警告点以下の場合、網掛けにする
            if (   ("".equals(score) == false)
                && (toInt(score, 0) <= keikokuten)) {
                svf.VrAttribute("SCORE" + scoreFiled, AMIKAKE_ATTR);
            }
        }

        //欠課
        svf.VrsOut("NOTICE" + noticeFiled,       notice);
        if (noticeAmikake == true) {
            svf.VrAttribute("NOTICE" + noticeFiled, AMIKAKE_ATTR);
        }
    }

    //小数第二位を四捨五入
    private String RoundHalfUp(String str) {
        BigDecimal bg = new BigDecimal(str);
        bg = bg.setScale(1, BigDecimal.ROUND_HALF_UP);
        return String.format("%.1f", bg);
    }

    private String ketaCenter(final String classname, final int keta) {
        final StringBuffer stb = new StringBuffer();
        stb.append(StringUtils.repeat(" ", (keta - KNJ_EditEdit.getMS932ByteLength(classname)) / 2));
        stb.append(StringUtils.defaultString(classname));
        stb.append(StringUtils.repeat(" ", (keta - KNJ_EditEdit.getMS932ByteLength(stb.toString()))));
        final String rtn = StringUtils.replace(stb.toString(), "  ", "　"); // 半角スペース2つを全角スペース1つに置換
        return rtn;
    }

    private static String[] split(final String name, final int keta) {
        int blankIdx = -1;
        String[] token0 = {};
        if (null != name) {
            token0 = KNJ_EditEdit.get_token(name, keta, 99);
            for (int i = token0.length - 1; i >= 0; i--) {
                if (null == token0[i] || "".equals(token0[i])) {
                    blankIdx = i;
                    continue;
                }
                break;
            }
        }
        String[] token;
        if (-1 == blankIdx) {
            token = token0;
        } else if (0 == blankIdx) {
            token = new String[] {};
        } else {
            token = new String[blankIdx];
            for (int i = 0; i < blankIdx; i++) {
                token[i] = token0[i];
            }
        }
        return token;
    }

    /**改行して印字する**/
    private void printReport(
            final Vrw32alp svf,
            final String fieldName,
            final String str,
            final int size,
            final int lineCnt
    ) {
        KNJObjectAbs knjobj = new KNJEditString();
        ArrayList arrlist = knjobj.retDividString(str, size, lineCnt);
        if ( arrlist != null ) {
            for (int i = 0; i < arrlist.size(); i++) {
                svf.VrsOutn(fieldName, i+1,  (String)arrlist.get(i) );
            }
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sqlStudent();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Student student = new Student();

                student._schregno   = rs.getString("SCHREGNO");     //学籍番号
                student._schoolName = rs.getString("SCHOOL_NAME");  //学校名
                student._majorname  = rs.getString("MAJORNAME");    //学科名
                student._schreg     = rs.getString("SCHREG");       //年組
                student._attendno   = rs.getString("ATTENDNO");     //番
                student._name       = rs.getString("NAME");         //氏名
                student._handicap   = rs.getString("HANDICAP");     //002:特別な支援対象者

                student.setData(db2);
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.debug("getList error!:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    /**修得上限値**/
    private String sqlSyutoku()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("  SELECT SYUTOKU_BUNSI ");
        stb.append("       , SYUTOKU_BUNBO ");
        stb.append("       , HOUTEI_SYUSU_SEMESTER1 ");
        stb.append("       , HOUTEI_SYUSU_SEMESTER2 ");
        stb.append("       , JITU_SYUSU ");
        stb.append("       , SUB_OFFDAYS ");
        stb.append("       , SUB_ABSENT ");
        stb.append("       , SUB_SUSPEND ");
        stb.append("       , SUB_MOURNING ");
        stb.append("       , SUB_VIRUS ");
        stb.append("    FROM V_SCHOOL_MST ");
        stb.append("   WHERE YEAR        = '" + _param._year + "' ");
        stb.append("     AND SCHOOLCD    = '000000000000' ");
        stb.append("     AND SCHOOL_KIND = '" + _param._schoolKind + "' ");

        return stb.toString();
    }

    /**オプション情報**/
    private String sqlOption()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("  SELECT SEQ ");
        stb.append("       , REMARK1 ");
        stb.append("       , REMARK2 ");
        stb.append("       , REMARK3 ");
        stb.append("    FROM HREPORT_CONDITION_DAT ");
        stb.append("   WHERE YEAR        = '" + _param._year + "' ");
        stb.append("     AND SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("ORDER BY SEQ");

        return stb.toString();
    }

    /**生徒情報**/
    private String sqlStudent()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("    SELECT SRD.SCHREGNO ");
        stb.append("         , CSD.SCHOOL_NAME ");
        stb.append("         , MJR.MAJORNAME ");
        stb.append("         , CASE SRD.GRADE ");
        stb.append("           WHEN '01' THEN '1' ");
        stb.append("           WHEN '02' THEN '2' ");
        stb.append("           WHEN '03' THEN '3' ");
        stb.append("           WHEN '04' THEN '4' ");
        stb.append("           ELSE  SRD.GRADE ");
        stb.append("           END || '年' ");
        stb.append("           || SRH.HR_CLASS_NAME1 || '組' AS SCHREG ");
        stb.append("         , SRD.ATTENDNO ");
        stb.append("         , SBM.NAME ");
        stb.append("         , SBM.HANDICAP ");
        stb.append("      FROM SCHREG_REGD_DAT SRD ");
        stb.append("INNER JOIN V_SEMESTER_GRADE_MST VSG");
        stb.append("        ON VSG.YEAR     = SRD.YEAR ");
        stb.append("       AND VSG.SEMESTER = SRD.SEMESTER ");
        stb.append("       AND VSG.GRADE    = SRD.GRADE ");
        stb.append(" LEFT JOIN SCHREG_REGD_HDAT SRH ");
        stb.append("        ON SRH.YEAR     = SRD.YEAR ");
        stb.append("       AND SRH.SEMESTER = SRD.SEMESTER ");
        stb.append("       AND SRH.GRADE    = SRD.GRADE ");
        stb.append("       AND SRH.HR_CLASS = SRD.HR_CLASS ");
        //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
        stb.append(" LEFT JOIN SCHREG_BASE_MST SB1 ");
        stb.append("        ON SB1.SCHREGNO = SRD.SCHREGNO ");
        stb.append("       AND SB1.GRD_DIV IN('2','3') ");
        stb.append("       AND SB1.GRD_DATE < CASE ");
        stb.append("                          WHEN VSG.EDATE < '" + _param._date + "'  ");
        stb.append("                          THEN VSG.EDATE ");
        stb.append("                          ELSE '" + _param._date + "' END ");
        //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
        stb.append(" LEFT JOIN SCHREG_BASE_MST SB2 ");
        stb.append("        ON SB2.SCHREGNO = SRD.SCHREGNO ");
        stb.append("       AND SB2.ENT_DIV IN('4','5') ");
        stb.append("       AND SB2.ENT_DATE > CASE ");
        stb.append("                          WHEN VSG.EDATE < '" + _param._date + "' ");
        stb.append("                          THEN VSG.EDATE ");
        stb.append("                          ELSE '" + _param._date + "' END ");
        //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
        stb.append(" LEFT JOIN SCHREG_TRANSFER_DAT STD ");
        stb.append("        ON STD.SCHREGNO = SRD.SCHREGNO ");
        stb.append("       AND STD.TRANSFERCD IN ('1','2') ");
        stb.append("       AND CASE ");
        stb.append("           WHEN VSG.EDATE < '" + _param._date + "' ");
        stb.append("           THEN VSG.EDATE ELSE '" + _param._date + "' END ");
        stb.append("           BETWEEN STD.TRANSFER_SDATE AND STD.TRANSFER_EDATE ");
        stb.append("LEFT JOIN CERTIF_SCHOOL_DAT CSD ");
        stb.append("       ON CSD.YEAR          = SRD.YEAR ");
        stb.append("      AND CSD.CERTIF_KINDCD = '104' ");
        stb.append("LEFT JOIN MAJOR_MST MJR ");
        stb.append("       ON MJR.COURSECD      = SRD.COURSECD ");
        stb.append("      AND MJR.MAJORCD       = SRD.MAJORCD ");
        stb.append("LEFT JOIN SCHREG_BASE_MST SBM ");
        stb.append("       ON SBM.SCHREGNO      = SRD.SCHREGNO ");
        stb.append("    WHERE SRD.YEAR          = '" + _param._year + "' ");
        if ("1".equals(_param._kubun)) { //1:クラス
            stb.append("      AND SRD.GRADE || SRD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._schregnos) + " ");
        } else if ("2".equals(_param._kubun)) { //2:個人
            stb.append("	  AND SRD.GRADE || SRD.HR_CLASS|| SRD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._schregnos) + " ");
        }
        if (SEMEALL.equals(_param._semester)) {
            stb.append("     AND SRD.SEMESTER = '" + _param._ctrlSeme + "' ");
        } else {
            stb.append("     AND SRD.SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND NOT EXISTS(SELECT 'X'  ");
            stb.append("                      FROM SCHREG_BASE_MST S1");
            stb.append("                     WHERE S1.SCHREGNO = SRD.SCHREGNO ");
            stb.append("                       AND S1.GRD_DIV IN('2','3') ");
            stb.append("                       AND S1.GRD_DATE < VSG.SDATE) ");
        }
        stb.append(" ORDER BY SRD.GRADE ");
        stb.append("        , SRD.HR_CLASS ");
        stb.append("        , SRD.ATTENDNO ");

        return stb.toString();
    }

    /**保護者住所(優先)**/
    private String sqlAddr1()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("   WITH SRD AS ( ");
        stb.append("              SELECT SCHREGNO ");
        stb.append("                    , YEAR ");
        stb.append("                    , GRADE ");
        stb.append("                    , HR_CLASS ");
        stb.append("                    , ATTENDNO ");
        stb.append("                 FROM SCHREG_REGD_DAT ");
        stb.append("                WHERE YEAR = '" + _param._year + "' ");
        stb.append("             GROUP BY SCHREGNO ");
        stb.append("                    , YEAR ");
        stb.append("                    , GRADE ");
        stb.append("                    , HR_CLASS ");
        stb.append("                    , ATTENDNO ");
        stb.append("   ) ");
        stb.append("   SELECT SRD.SCHREGNO ");
        stb.append("        , '〒' || SAD.SEND_ZIPCD AS SEND_ZIPCD");  //郵便番号
        stb.append("        , SAD.SEND_ADDR1 ");                       //保護者住所1
        stb.append("        , SAD.SEND_ADDR2 ");                       //保護者住所2
        stb.append("        , SAD.SEND_NAME ");                        //保護者氏名
        stb.append("     FROM SRD ");
        stb.append("LEFT JOIN SCHREG_SEND_ADDRESS_DAT SAD ");
        stb.append("       ON SAD.SCHREGNO = SRD.SCHREGNO ");
        stb.append("    WHERE SRD.YEAR     = '" + _param._year + "' ");
        if ("1".equals(_param._kubun)) { //1:クラス
            stb.append("      AND SRD.GRADE || SRD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._schregnos) + " ");
        } else if ("2".equals(_param._kubun)) { //2:個人
            stb.append("	  AND SRD.GRADE || SRD.HR_CLASS|| SRD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._schregnos) + " ");
        }
        stb.append(" ORDER BY SRD.GRADE ");
        stb.append("        , SRD.HR_CLASS ");
        stb.append("        , SRD.ATTENDNO ");

        return stb.toString();
    }

    /**保護者住所(優先じゃない)**/
    private String sqlAddr2()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("   WITH SRD AS ( ");
        stb.append("              SELECT SCHREGNO ");
        stb.append("                   , YEAR ");
        stb.append("                   , GRADE ");
        stb.append("                   , HR_CLASS ");
        stb.append("                   , ATTENDNO ");
        stb.append("                FROM SCHREG_REGD_DAT ");
        stb.append("               WHERE YEAR = '" + _param._year + "' ");
        stb.append("            GROUP BY SCHREGNO ");
        stb.append("                   , YEAR ");
        stb.append("                   , GRADE ");
        stb.append("                   , HR_CLASS ");
        stb.append("                   , ATTENDNO ");
        stb.append("   ) ");
        stb.append("   SELECT SRD.SCHREGNO ");
        stb.append("        , '〒' || GAD.GUARD_ZIPCD AS GUARD_ZIPCD");  //郵便番号
        stb.append("        , GAD.GUARD_ADDR1 ");                        //保護者住所1
        stb.append("        , GAD.GUARD_ADDR2 ");                        //保護者住所2
        stb.append("        , GRD.GUARD_NAME ");                         //保護者氏名
        stb.append("     FROM SRD ");
        stb.append("LEFT JOIN GUARDIAN_ADDRESS_DAT GAD ");
        stb.append("       ON GAD.SCHREGNO = SRD.SCHREGNO ");
        stb.append("LEFT JOIN GUARDIAN_DAT GRD ");
        stb.append("       ON GRD.SCHREGNO = GAD.SCHREGNO ");
        stb.append("    WHERE SRD.YEAR     = '" + _param._year + "' ");
        if ("1".equals(_param._kubun)) { //1:クラス
            stb.append("      AND SRD.GRADE || SRD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._schregnos) + " ");
        } else if ("2".equals(_param._kubun)) { //2:個人
            stb.append("	  AND SRD.GRADE || SRD.HR_CLASS|| SRD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._schregnos) + " ");
        }
        stb.append(" ORDER BY SRD.GRADE ");
        stb.append("        , SRD.HR_CLASS ");
        stb.append("        , SRD.ATTENDNO ");
        stb.append("        , GAD.ISSUEDATE desc ");

        return stb.toString();
    }

    /**フッター、校長名、担任名**/
    private String sqlFotter()
    {
        final StringBuffer stb = new StringBuffer();
        stb.append("   SELECT SRD.SCHREGNO ");
        stb.append("        , CSD.PRINCIPAL_NAME ");  //校長名
        stb.append("        , STF.STAFFNAME ");       //担任名
        stb.append("     FROM SCHREG_REGD_DAT  SRD ");
        stb.append("LEFT JOIN SCHREG_REGD_HDAT SRH ");
        stb.append("       ON SRH.YEAR          = SRD.YEAR ");
        stb.append("      AND SRH.SEMESTER      = SRD.SEMESTER ");
        stb.append("      AND SRH.GRADE         = SRD.GRADE ");
        stb.append("      AND SRH.HR_CLASS      = SRD.HR_CLASS ");
        stb.append("LEFT JOIN STAFF_MST STF ");
        stb.append("       ON STF.STAFFCD       = SRH.TR_CD1 ");
        stb.append("LEFT JOIN CERTIF_SCHOOL_DAT CSD ");
        stb.append("       ON CSD.YEAR          = SRD.YEAR ");
        stb.append("    WHERE SRD.YEAR          = '" + _param._year + "' ");
        stb.append("      AND CSD.CERTIF_KINDCD = '104' ");
        if (SEMEALL.equals(_param._semester)) {
            stb.append("  AND SRD.SEMESTER      = '" + _param._ctrlSeme + "' ");
        } else {
            stb.append("  AND SRD.SEMESTER      = '" + _param._semester + "' ");
        }
        if ("1".equals(_param._kubun)) { //1:クラス
            stb.append("      AND SRD.GRADE || SRD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._schregnos) + " ");
        } else if ("2".equals(_param._kubun)) { //2:個人
            stb.append("	  AND SRD.GRADE || SRD.HR_CLASS|| SRD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._schregnos) + " ");
        }

        return stb.toString();
    }

    private Param getParam(final DB2UDB db2, final HttpServletRequest request) {
        KNJServletUtils.debugParam(request, log);
        log.fatal("$Revision$ $Date$"); // CVSキーワードの取り扱いに注意
        Param param = new Param(db2, request);
        return param;
    }

    private class Student {
        String _schregno;
        String _name;
        String _staffname;
        String _staffname2;
        String _attendno;
        String _majorname;
        String _coursename;
        String _jiritsu;
        String _midashi;
        String _totalStudyTime;
        String _shikaku;
        String _communication;
        String _schoolName;
        String _schreg;
        String _handicap;
        final Map _attendMap = new TreeMap();
        final Map _printSubclassMap = new TreeMap();
        final Map _syutokuMap = new TreeMap();
        ArrayList<String> subclass = new ArrayList<String>();
        ArrayList<String> semester = new ArrayList<String>();
        final Map _classAvgMap = new TreeMap();
        final Map<String, String> _totalScoreMap = new TreeMap<String, String>();
        final Map _totalAvgMap = new TreeMap();
        final Map _classAvgRankMap = new TreeMap();
        final Map _classRankMap = new TreeMap();
        final Map _gradeAvgRankMap = new TreeMap();
        final Map _gradeRankMap = new TreeMap();
        final Map _teikikousaMap = new TreeMap();
        final Map _teikiTotalAvgMap = new TreeMap();
        final Map _teikiTotalClassAvgMap = new TreeMap();
        final Map _teikiClassAvgRankMap = new TreeMap();
        final Map _teikiClassRankMap = new TreeMap();
        final Map _teikiGradeAvgRankMap = new TreeMap();
        final Map _teikiGradeRankMap = new TreeMap();
        final Map _ketsujiMap = new TreeMap();
        final Map _bikouMap = new TreeMap();
        final Map<String, String[]> _classNameMap = new TreeMap<String, String[]>();
        final Map<String, String> _classNameFieldMap = new TreeMap<String, String>();

        private void setData(final DB2UDB db2) {
            setSubclass(db2);
            setSyutoku(db2);
            setClassAvg(db2);
            setScoreAvgRank(db2);
            setTeikikousa(db2);
            setTeikikousaScoreAvgRank(db2);
            setSyoken(db2);
            setKetsujiBikou(db2);
            setKeikokuten(db2);
        }

        private void setSubclass(final DB2UDB db2) {
            String scoreSql = sqlScore1();
            if (_param._isOutputDebug) {
                log.fatal(" scoreSql = " + scoreSql);
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String oldClasscd    = "";
                String oldSubclasscd = "";
                String oldClassName  = "";
                int classcdCnt       = 1;
                int columnZenkakuMojisu = 3;
                ps = db2.prepareStatement(scoreSql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String classcd      = rs.getString("CLASSCD");
                    final String classname    = rs.getString("CLASSNAME");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String credits       = rs.getString("CREDITS");
                    final String subclassabbv = rs.getString("SUBCLASSABBV");


                    String subclasscd = rs.getString("SUBCLASSCD");
                    final String key = subclasscd;
                    if (!_printSubclassMap.containsKey(key)) {
                        _printSubclassMap.put(key, new ScoreData(classcd, subclasscd, subclassname, credits, subclassabbv));
                        subclass.add(key);
                    }
                    if (null == rs.getString("SEMESTER")) {
                        continue;
                    }
                    if (semester.indexOf(rs.getString("SEMESTER")) < 0) {
                        semester.add(rs.getString("SEMESTER"));
                    }

                    //教科名を中央に印字するために分割する
                    if (oldClasscd.equals(classcd) && !(oldSubclasscd.equals(subclasscd))) {
                        classcdCnt++;
                        oldSubclasscd = subclasscd;
                    } else if (!(oldClasscd.equals(classcd))) {
                        if (!("".equals(oldClasscd))) {
                            float classNameLen = KNJ_EditEdit.getMS932ByteLength(oldClassName);

                            final String classNameField = (classNameLen / classcdCnt <= 3 * 2)? "2":
                                                                 (classNameLen / classcdCnt <= 4 * 2)? "3": "4";

                            columnZenkakuMojisu = (classNameLen / classcdCnt <= 3 * 2)? 3:
                                                      (classNameLen / classcdCnt <= 4 * 2)? 4 : 5;

                            final String[] splittedClassName = split(ketaCenter(oldClassName, classcdCnt * columnZenkakuMojisu * 2), columnZenkakuMojisu * 2);
                            _classNameMap.put(oldClasscd, splittedClassName);
                            _classNameFieldMap.put(oldClasscd, classNameField);
                            classcdCnt = 1;
                        }
                        oldClasscd    = classcd;
                        oldSubclasscd = subclasscd;
                        oldClassName  = classname;
                    }

                    final ScoreData scoreData = (ScoreData) _printSubclassMap.get(key);
                    final String semester     = rs.getString("SEMESTER");
                    final String score        = rs.getString("SCORE");
                    final String tsuishi      = rs.getString("TSUISHI");       //追指導
                    final String noticeCnt    = rs.getString("NOTICE_CNT");
                    final String nurseoff     = rs.getString("NURSEOFF");
                    final String offdays      = rs.getString("OFFDAYS");
                    final String absent       = rs.getString("ABSENT");
                    final String suspend      = rs.getString("SUSPEND");
                    final String mourning     = rs.getString("MOURNING");
                    final String virus        = rs.getString("VIRUS");
                    final String gappei        = rs.getString("REPLACEMOTO");
                    scoreData._scoreMap.put(semester, score);
                    scoreData._tuishiMap.put(semester, tsuishi);
                    scoreData._noteiceCntMap.put(semester, noticeCnt);
                    scoreData._nurseoffMap.put(semester, nurseoff);
                    scoreData._offdaysMap.put(semester, offdays);
                    scoreData._absentMap.put(semester, absent);
                    scoreData._suspendMap.put(semester, suspend);
                    scoreData._mourningMap.put(semester, mourning);
                    scoreData._virusMap.put(semester, virus);
                    scoreData._gappeiMap.put(key, gappei);
                }
                if (rs != null) {
                    float classNameLen = KNJ_EditEdit.getMS932ByteLength(oldClassName);

                    final String classNameField = (classNameLen / classcdCnt <= 3 * 2)? "2":
                                                         (classNameLen / classcdCnt <= 4 * 2)? "3": "4";

                    columnZenkakuMojisu = (classNameLen / classcdCnt <= 3 * 2)? 3:
                                              (classNameLen / classcdCnt <= 4 * 2)? 4 : 5;

                    final String[] splittedClassName = split(ketaCenter(oldClassName, classcdCnt * columnZenkakuMojisu * 2), columnZenkakuMojisu * 2);
                    _classNameMap.put(oldClasscd, splittedClassName);    //分割した教科名
                    _classNameFieldMap.put(oldClasscd, classNameField);  //教科名のフィールド
                }

            } catch (Exception e) {
                log.error("setSubclass error!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private void setSyutoku(final DB2UDB db2) {
            String scoreSql = sqlScore2();
            if (_param._isOutputDebug) {
                log.fatal(" scoreSql = " + scoreSql);
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(scoreSql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String score      = rs.getString("SCORE");       //評定
                    final String getCredit = rs.getString("GET_CREDIT");  //修得単位数
                    final String addCredit = rs.getString("ADD_CREDIT");  //増加単位数

                    String subclasscd = rs.getString("SUBCLASSCD");
                    final String key  = subclasscd;

                    if (!_syutokuMap.containsKey(key)) {
                        _syutokuMap.put(key, new SyutokuData(subclasscd));
                    }

                    if (key != null) {
                        final SyutokuData syutokueData = (SyutokuData) _syutokuMap.get(key);
                        syutokueData._scoreMap.put(key, score);
                        syutokueData._getCreditMap.put(key, getCredit);
                        syutokueData._addCreditMap.put(key, addCredit);
                    }
                }
            } catch (Exception e) {
                log.error("setSyutoku error!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private void setClassAvg(final DB2UDB db2) {
            String scoreSql = sqlScore3();
            if (_param._isOutputDebug) {
                log.fatal(" scoreSql = " + scoreSql);
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(scoreSql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String semester = rs.getString("SEMESTER");
                    final String avg = rs.getString("AVG");

                    if (semester != null) {
                        _classAvgMap.put(semester, avg);
                    }

                }
            } catch (Exception e) {
                log.error("setClassAvg error!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        //学習成績の総点、平均、順位
        private void setScoreAvgRank(final DB2UDB db2) {
            String scoreSql = sqlScore4();
            if (_param._isOutputDebug) {
                log.fatal(" scoreSql = " + scoreSql);
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(scoreSql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String semester = rs.getString("SEMESTER");

                    if (semester != null) {
                        _totalScoreMap.put(semester, rs.getString("SCORE"));  //総点
                        _totalAvgMap.put(semester, rs.getString("AVG"));      //平均点

                        final Rank classAvgRank = new Rank(rs.getString("CLASS_AVG_RANK"), rs.getString("CLASS_COUNT"));  //クラス順位、平均点基準
                        final Rank classRank    = new Rank(rs.getString("CLASS_RANK"), rs.getString("CLASS_COUNT"));      //クラス順位、合計点基準
                        final Rank gradeAvgRank = new Rank(rs.getString("GRADE_AVG_RANK"), rs.getString("GRADE_COUNT"));  //学年順位、  平均点基準
                        final Rank gradeRank    = new Rank(rs.getString("GRADE_RANK"), rs.getString("GRADE_COUNT"));      //学年順位、  合計点基準

                        _classAvgRankMap.put(semester, classAvgRank);
                        _classRankMap.put(semester, classRank);
                        _gradeAvgRankMap.put(semester, gradeAvgRank);
                        _gradeRankMap.put(semester, gradeRank);
                    }
                }
            } catch (Exception e) {
                log.error("setScoreAvgRank error!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        //定期考査の成績
        private void setTeikikousa(final DB2UDB db2) {
            String scoreSql = sqlTest();
            if (_param._isOutputDebug) {
                log.fatal(" scoreSql = " + scoreSql);
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(scoreSql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    String subclasscd   = rs.getString("SUBCLASSCD");
                    String subclassname = rs.getString("SUBCLASSABBV");

                    final String key = subclasscd;
                    if (key != null) {
                        if (!_teikikousaMap.containsKey(key)) {
                            _teikikousaMap.put(key, new TeikikousaData(subclasscd, subclassname));
                        }

                        final TeikikousaData teikikousaData = (TeikikousaData) _teikikousaMap.get(key);
                        final String semester     = rs.getString("SEMESTER");
                        final String testkindcd  = rs.getString("TESTKINDCD");    //試験種別
                        final String testName    = rs.getString("TESTITEMNAME");  //試験種別名
                        final String score        = rs.getString("SCORE");         //素点
                        final String testType     = semester + "-" + testkindcd;
                        teikikousaData._scoreMap.put(testType, score);
                        teikikousaData._testName.put(testType, testName);
                    }
                }
            } catch (Exception e) {
                log.error("setTeikikousa error!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        //定期考査の平均、順位
        private void setTeikikousaScoreAvgRank(final DB2UDB db2) {
            String scoreSql = sqlTest2();
            if (_param._isOutputDebug) {
                log.fatal(" scoreSql = " + scoreSql);
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(scoreSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester     = rs.getString("SEMESTER");
                    final String testkindcd  = rs.getString("TESTKINDCD");    //試験種別
                    final String testType     = semester + "-" + testkindcd;

                    if (semester != null) {
                        final Rank classAvgRank = new Rank(rs.getString("CLASS_AVG_RANK"), rs.getString("CLASS_COUNT"));  //クラス順位、平均点基準
                        final Rank classRank    = new Rank(rs.getString("CLASS_RANK"), rs.getString("CLASS_COUNT"));      //クラス順位、合計点基準
                        final Rank gradeAvgRank = new Rank(rs.getString("GRADE_AVG_RANK"), rs.getString("GRADE_COUNT"));  //学年順位、  平均点基準
                        final Rank gradeRank    = new Rank(rs.getString("GRADE_RANK"), rs.getString("GRADE_COUNT"));       //学年順位、  合計点基準

                        _teikiTotalAvgMap.put(testType, rs.getString("AVG"));      //平均点
                        _teikiTotalClassAvgMap.put(testType, rs.getString("CLASS_AVG")); //学級平均点
                        _teikiClassAvgRankMap.put(testType, classAvgRank);
                        _teikiClassRankMap.put(testType, classRank);
                        _teikiGradeAvgRankMap.put(testType, gradeAvgRank);
                        _teikiGradeRankMap.put(testType, gradeRank);
                    }
                }
            } catch (Exception e) {
                log.error("setTeikikousaScoreAvgRank error!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        //自立活動、総合的な探求の時間、取得資格・検定、所見
        private void setSyoken(final DB2UDB db2) {
            String scoreSql = sqlSyoken();
            if (_param._isOutputDebug) {
                log.fatal(" scoreSql = " + scoreSql);
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(scoreSql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    _jiritsu        = (rs.getString("JIRITSU"));         //自立活動
                    _midashi        = (rs.getString("MIDASHI"));         //みだし
                    _totalStudyTime = (rs.getString("TOTALSTUDYTIME"));  //総合的な探求の時間
                    _shikaku        = (rs.getString("SHIKAKU"));         //資格取得・検定
                    _communication  = (rs.getString("COMMUNICATION"));   //所見
                }
            } catch (Exception e) {
                log.error("setSyoken error!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        //欠時、備考
        private void setKetsujiBikou(final DB2UDB db2) {
            String scoreSql = sqlKetsujiBikou();
            if (_param._isOutputDebug) {
                log.fatal(" scoreSql = " + scoreSql);
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(scoreSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String key = rs.getString("SEMESTER");

                    if (key != null) {
                        _ketsujiMap.put(key, (rs.getString("KEKKA_JISU")));        //欠時
                        _bikouMap.put(key,   (rs.getString("ATTENDREC_REMARK")));  //備考
                    }
                }
            } catch (Exception e) {
                log.error("setKetsujiBikou error!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        //科目毎の警告点
        private void setKeikokuten(final DB2UDB db2) {
            String keikokutenSql = sqlKeikokuten();
            if (_param._isOutputDebug) {
                log.fatal(" keikokutenSql = " + keikokutenSql);
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(keikokutenSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    String subclasscd = rs.getString("SUBCLASSCD");
                    final String key = subclasscd;

                    if (key != null) {
                        final ScoreData scoreData = (ScoreData) _printSubclassMap.get(key);
                        final String semester     = rs.getString("SEMESTER");
                        final String keikokuten   = rs.getString("KEIKOKUTEN");
                        scoreData._keikokutenMap.put(semester, keikokuten);
                    }
                }
            } catch (Exception e) {
                log.error("setKeikokuten error!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        /**学習成績(選択した学期より前のデータを全て取得)**/
        /**学生、学期、科目毎**/
        /**科目名、単位数、評価、欠課**/
        private String sqlScore1()
        {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SRD AS ( ");
            stb.append("      SELECT SCHREGNO ");
            stb.append("           , YEAR ");
            stb.append("           , GRADE ");
            stb.append("           , COURSECD ");
            stb.append("           , MAJORCD ");
            stb.append("           , COURSECODE ");
            stb.append("      FROM SCHREG_REGD_DAT ");
            stb.append("     WHERE SCHREGNO = '" + _schregno + "' ");
            stb.append("       AND YEAR     = '" + _param._year + "' ");
            stb.append("  GROUP BY SCHREGNO ");
            stb.append("         , YEAR ");
            stb.append("         , GRADE ");
            stb.append("         , COURSECD ");
            stb.append("         , MAJORCD ");
            stb.append("         , COURSECODE ");
            stb.append(" ) , NTC AS ( ");
            stb.append("    SELECT SCHREGNO ");
            stb.append("          , SEMESTER ");
            stb.append("          , SUBCLASSCD ");
            stb.append("          ,   SUM(NVL(NOTICE,   0)) ");
            stb.append("            + SUM(NVL(NONOTICE, 0)) ");
            stb.append("            + SUM(NVL(SICK,     0)) AS NOTICE_CNT ");
            stb.append("          , NVL(NURSEOFF, 0) AS NURSEOFF ");
            stb.append("          , NVL(OFFDAYS,  0) AS OFFDAYS");
            stb.append("          , NVL(ABSENT,   0) AS ABSENT ");
            stb.append("          , NVL(SUSPEND,  0) AS SUSPEND ");
            stb.append("          , NVL(MOURNING, 0) AS MOURNING ");
            stb.append("          , NVL(VIRUS,    0) AS VIRUS ");
            stb.append("       FROM ATTEND_SUBCLASS_DAT ");
            stb.append("      WHERE YEAR = '" + _param._year + "' ");
            stb.append("   GROUP BY SEMESTER ");
            stb.append("          , SCHREGNO ");
            stb.append("          , SUBCLASSCD ");
            stb.append("          , NURSEOFF ");
            stb.append("          , OFFDAYS");
            stb.append("          , ABSENT ");
            stb.append("          , SUSPEND ");
            stb.append("          , MOURNING ");
            stb.append("          , VIRUS ");
            stb.append("   ORDER BY SCHREGNO ");
            stb.append("          , SEMESTER ");
            stb.append("          , SUBCLASSCD ");
            stb.append(" ) , COMBINED_A AS ( ");
            stb.append("     SELECT COMBINED_CLASSCD ");
            stb.append("          , COMBINED_SCHOOL_KIND ");
            stb.append("          , COMBINED_CURRICULUM_CD ");
            stb.append("          , COMBINED_SUBCLASSCD ");
            stb.append("       FROM SUBCLASS_REPLACE_COMBINED_DAT ");
            stb.append("      WHERE YEAR = '" + _param._year + "' ");
            stb.append(" ) , COMBINED_B AS ( ");
            stb.append("     SELECT ATTEND_CLASSCD ");
            stb.append("          , ATTEND_SCHOOL_KIND ");
            stb.append("          , ATTEND_CURRICULUM_CD ");
            stb.append("          , ATTEND_SUBCLASSCD ");
            stb.append("       FROM SUBCLASS_REPLACE_COMBINED_DAT ");
            stb.append("      WHERE YEAR = '" + _param._year + "' ");
            stb.append(" ) ");
            stb.append("   SELECT SRD.SCHREGNO ");
            stb.append("        , RS8.SEMESTER ");
            stb.append("        , RS8.SCORE_DIV ");
            stb.append("        , RS8.CLASSCD ");
            stb.append("        , RS8.SUBCLASSCD ");
            stb.append("        , CASE  ");
            stb.append("          WHEN CLS.CLASSABBV IS NULL ");
            stb.append("               THEN CLS.CLASSNAME ");
            stb.append("               ELSE CLS.CLASSABBV ");
            stb.append("          END  AS CLASSNAME ");     //教科名
            stb.append("        , CASE  ");
            stb.append("          WHEN SCL.SUBCLASSORDERNAME2 IS NULL ");
            stb.append("               THEN SCL.SUBCLASSNAME ");
            stb.append("               ELSE SCL.SUBCLASSORDERNAME2 ");
            stb.append("          END  AS SUBCLASSNAME ");
            stb.append("        , SCL.SUBCLASSABBV ");
            stb.append("        , CRD.CREDITS ");           //単位数
            stb.append("        , RS8.SCORE ");             //評価
            stb.append("        , NVL(NTC.NOTICE_CNT, 0) AS NOTICE_CNT ");  //欠課
            stb.append("        , RSSD.SCORE AS TSUISHI ");  //追試の評価
            stb.append("        , NTC.NURSEOFF ");
            stb.append("        , NTC.OFFDAYS");
            stb.append("        , NTC.ABSENT ");
            stb.append("        , NTC.SUSPEND ");
            stb.append("        , NTC.MOURNING ");
            stb.append("        , NTC.VIRUS ");
            stb.append("        , CASE  ");
            stb.append("          WHEN COMBINED_A.COMBINED_CLASSCD IS NOT NULL ");
            stb.append("               THEN " + GAPPEISAKI + " ");
            stb.append("          WHEN COMBINED_B.ATTEND_CLASSCD IS NOT NULL ");
            stb.append("               THEN " + GAPPEIMOTO + " ");
            stb.append("               ELSE " + GAPPEINASI + "  ");
            stb.append("          END  AS REPLACEMOTO ");     //合併科目
            stb.append("     FROM SRD ");
            stb.append("LEFT JOIN RECORD_SCORE_DAT RS8 ");
            stb.append("       ON RS8.YEAR          = SRD.YEAR ");
            stb.append("      AND RS8.SCHREGNO      = SRD.SCHREGNO ");
            stb.append("      AND RS8.TESTKINDCD    = '99' ");
            stb.append("      AND RS8.TESTITEMCD    = '00' ");
            stb.append("      AND RS8.SCORE_DIV     = '08' ");
            stb.append("LEFT JOIN CLASS_MST CLS ");
            stb.append("       ON CLS.CLASSCD       = RS8.CLASSCD ");
            stb.append("      AND CLS.SCHOOL_KIND   = '" + _param._schoolKind + "' ");
            stb.append("LEFT JOIN SUBCLASS_MST SCL ");
            stb.append("       ON SCL.CLASSCD       = RS8.CLASSCD ");
            stb.append("      AND SCL.SCHOOL_KIND   = CLS.SCHOOL_KIND ");
            stb.append("      AND SCL.CURRICULUM_CD = RS8.CURRICULUM_CD ");
            stb.append("      AND SCL.SUBCLASSCD    = RS8.SUBCLASSCD ");
            stb.append("LEFT JOIN CREDIT_MST CRD ");
            stb.append("       ON CRD.YEAR          = SRD.YEAR ");
            stb.append("      AND CRD.COURSECD      = SRD.COURSECD ");
            stb.append("      AND CRD.MAJORCD       = SRD.MAJORCD ");
            stb.append("      AND CRD.GRADE         = SRD.GRADE ");
            stb.append("      AND CRD.COURSECODE    = SRD.COURSECODE ");
            stb.append("      AND CRD.CLASSCD       = RS8.CLASSCD ");
            stb.append("      AND CRD.SCHOOL_KIND   = CLS.SCHOOL_KIND ");
            stb.append("      AND CRD.CURRICULUM_CD = RS8.CURRICULUM_CD ");
            stb.append("      AND CRD.SUBCLASSCD    = RS8.SUBCLASSCD ");
            stb.append("LEFT JOIN RECORD_SLUMP_SDIV_DAT RSSD ");
            stb.append("       ON RSSD.YEAR          = SRD.YEAR ");
            stb.append("      AND RSSD.SEMESTER      = RS8.SEMESTER ");
            stb.append("      AND RSSD.TESTKINDCD    = RS8.TESTKINDCD ");
            stb.append("      AND RSSD.TESTITEMCD    = RS8.TESTITEMCD ");
            stb.append("      AND RSSD.SCORE_DIV     = RS8.SCORE_DIV ");
            stb.append("      AND RSSD.CLASSCD       = RS8.CLASSCD ");
            stb.append("      AND RSSD.SCHOOL_KIND   = CLS.SCHOOL_KIND ");
            stb.append("      AND RSSD.CURRICULUM_CD = RS8.CURRICULUM_CD ");
            stb.append("      AND RSSD.SUBCLASSCD    = RS8.SUBCLASSCD ");
            stb.append("      AND RSSD.SCHREGNO      = SRD.SCHREGNO ");
            stb.append("LEFT JOIN NTC ");
            stb.append("       ON NTC.SCHREGNO      = SRD.SCHREGNO ");
            stb.append("      AND NTC.SEMESTER      = RS8.SEMESTER ");
            stb.append("      AND NTC.SUBCLASSCD    = RS8.SUBCLASSCD ");
            stb.append("LEFT JOIN COMBINED_A ");
            stb.append("       ON COMBINED_A.COMBINED_CLASSCD       = RS8.CLASSCD ");
            stb.append("      AND COMBINED_A.COMBINED_SCHOOL_KIND   = CLS.SCHOOL_KIND ");
            stb.append("      AND COMBINED_A.COMBINED_CURRICULUM_CD = RS8.CURRICULUM_CD ");
            stb.append("      AND COMBINED_A.COMBINED_SUBCLASSCD    = RS8.SUBCLASSCD ");
            stb.append("LEFT JOIN COMBINED_B ");
            stb.append("       ON COMBINED_B.ATTEND_CLASSCD       = RS8.CLASSCD ");
            stb.append("      AND COMBINED_B.ATTEND_SCHOOL_KIND   = CLS.SCHOOL_KIND ");
            stb.append("      AND COMBINED_B.ATTEND_CURRICULUM_CD = RS8.CURRICULUM_CD ");
            stb.append("      AND COMBINED_B.ATTEND_SUBCLASSCD    = RS8.SUBCLASSCD ");
            stb.append("    WHERE RS8.SEMESTER     <= "  + _param._semester);
            stb.append(" ORDER BY RS8.CLASSCD ");
            stb.append("        , RS8.SUBCLASSCD ");
            stb.append("        , RS8.SEMESTER ");

            return stb.toString();
        }

        /**学習成績**/
        /**評定、修得単位数**/
        private String sqlScore2()
        {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SRD AS ( ");
            stb.append("             SELECT YEAR ");
            stb.append("                  , GRADE ");
            stb.append("                  , HR_CLASS ");
            stb.append("                  , SCHREGNO ");
            stb.append("               FROM SCHREG_REGD_DAT ");
            stb.append("           GROUP BY YEAR");
            stb.append("                  , GRADE ");
            stb.append("                  , HR_CLASS ");
            stb.append("                  , SCHREGNO ");
            stb.append(" ) ");
            stb.append("   SELECT SRD.SCHREGNO ");
            stb.append("        , RS9.SUBCLASSCD ");
            stb.append("        , RS9.SCORE ");
            stb.append("        , NVL(RS9.GET_CREDIT, 0) AS GET_CREDIT ");
            stb.append("        , NVL(SQD.CREDITS,    0) AS ADD_CREDIT ");  //増加単位
            stb.append("     FROM SRD ");
            stb.append("LEFT JOIN RECORD_SCORE_DAT RS9  ");
            stb.append("       ON RS9.YEAR        = SRD.YEAR ");
            stb.append("	  AND RS9.SCHREGNO    = SRD.SCHREGNO ");
            stb.append("LEFT JOIN SCHREG_QUALIFIED_DAT SQD ");
            stb.append("       ON SQD.YEAR          = SRD.YEAR ");
            stb.append("	  AND SQD.SCHREGNO      = SRD.SCHREGNO ");
            stb.append("	  AND SQD.CLASSCD       = RS9.CLASSCD ");
            stb.append("	  AND SQD.SCHOOL_KIND   = RS9.SCHOOL_KIND ");
            stb.append("	  AND SQD.CURRICULUM_CD = RS9.CURRICULUM_CD ");
            stb.append("	  AND SQD.SUBCLASSCD    = RS9.SUBCLASSCD ");
            stb.append("	  AND SQD.CONDITION_DIV = '1' ");
            stb.append("    WHERE SRD.YEAR        = '" + _param._year + "' ");
            stb.append("      AND RS9.SEMESTER    = 9 ");
            stb.append("      AND RS9.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("      AND RS9.TESTKINDCD  = '99' ");
            stb.append("      AND RS9.TESTITEMCD  = '00' ");
            stb.append("      AND RS9.SCORE_DIV   = '09' ");
            stb.append("	  AND SRD.SCHREGNO    = '" + _schregno + "' ");
            stb.append(" ORDER BY SRD.SCHREGNO ");
            stb.append("        , RS9.SUBCLASSCD ");

            return stb.toString();
        }

        /**学習成績**/
        /**学級平均**/
        private String sqlScore3()
        {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SRD AS ( ");
            stb.append("             SELECT YEAR ");
            stb.append("                  , GRADE ");
            stb.append("                  , HR_CLASS ");
            stb.append("               FROM SCHREG_REGD_DAT ");
            stb.append("              WHERE YEAR     = '" + _param._year + "' ");
            stb.append("	            AND SCHREGNO = '" + _schregno + "' ");
            stb.append("           GROUP BY YEAR");
            stb.append("                  , GRADE ");
            stb.append("                  , HR_CLASS ");
            stb.append(" ) ");
            stb.append("   SELECT RAC.SEMESTER ");
            stb.append("        , RAC.AVG ");
            stb.append("     FROM SRD ");
            stb.append("LEFT JOIN RECORD_AVERAGE_CLASS_SDIV_DAT RAC ");
            stb.append("       ON RAC.YEAR        = SRD.YEAR ");
            stb.append("      AND RAC.GRADE       = SRD.GRADE ");
            stb.append("      AND RAC.HR_CLASS    = SRD.HR_CLASS ");
            stb.append("      AND RAC.TESTKINDCD  = '99' ");
            stb.append("      AND RAC.TESTITEMCD  = '00' ");
            stb.append("      AND RAC.SCORE_DIV   = '08' ");
            stb.append("      AND RAC.CLASS_DIV   = '9' ");
            stb.append("      AND RAC.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append(" ORDER BY RAC.SEMESTER ");

            return stb.toString();
        }

        /**学習成績**/
        /**生徒毎、学期毎、総点、個人平均、順位**/
        private String sqlScore4()
        {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SRD AS ( ");
            stb.append("             SELECT YEAR ");
            stb.append("                  , SCHREGNO ");
            stb.append("                  , GRADE ");
            stb.append("                  , HR_CLASS ");
            stb.append("                  , COURSECD ");
            stb.append("                  , MAJORCD ");
            stb.append("                  , COURSECODE ");
            stb.append("               FROM SCHREG_REGD_DAT ");
            stb.append("              WHERE YEAR     = '" + _param._year + "' ");
            stb.append("	            AND SCHREGNO = '" + _schregno + "' ");
            stb.append("           GROUP BY YEAR");
            stb.append("                  , SCHREGNO ");
            stb.append("                  , GRADE ");
            stb.append("                  , HR_CLASS ");
            stb.append("                  , COURSECD ");
            stb.append("                  , MAJORCD ");
            stb.append("                  , COURSECODE ");
            stb.append(" ) ");
            stb.append("    SELECT SRD.SCHREGNO ");
            stb.append("         , RRS.SEMESTER ");
            stb.append("         , RRS.SCORE ");   //総点
            stb.append("         , RRS.AVG ");     //平均
            stb.append("         , RRS.CLASS_AVG_RANK ");  //クラス順位、平均点基準
            stb.append("         , RRS.CLASS_RANK ");      //クラス順位、合計点基準
            stb.append("         , RRS.GRADE_AVG_RANK ");  //学年順位、  平均点基準
            stb.append("         , RRS.GRADE_RANK ");      //学年順位、  合計点基準
            stb.append("         , RA1.COUNT AS CLASS_COUNT ");  //クラス人数
            stb.append("         , RA2.COUNT AS GRADE_COUNT ");  //学年人数
            stb.append("      FROM SRD ");
            stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT RRS ");
            stb.append("        ON RRS.YEAR          = SRD.YEAR ");
            stb.append("       AND RRS.SCHREGNO      = SRD.SCHREGNO ");
            stb.append("       AND RRS.TESTKINDCD    = '99' ");
            stb.append("       AND RRS.TESTITEMCD    = '00' ");
            stb.append("       AND RRS.SCORE_DIV     = '08' ");
            stb.append("       AND RRS.SUBCLASSCD    = '999999' ");
            stb.append("       AND RRS.SCHOOL_KIND   = '" + _param._schoolKind + "' ");
            stb.append(" LEFT JOIN RECORD_AVERAGE_SDIV_DAT RA1 ");
            stb.append("        ON RA1.YEAR          = SRD.YEAR ");
            stb.append("       AND RA1.SEMESTER      = RRS.SEMESTER ");
            stb.append("       AND RA1.TESTKINDCD    = RRS.TESTKINDCD ");
            stb.append("       AND RA1.TESTITEMCD    = RRS.TESTITEMCD ");
            stb.append("       AND RA1.SCORE_DIV     = RRS.SCORE_DIV ");
            stb.append("       AND RA1.CLASSCD       = RRS.CLASSCD ");
            stb.append("       AND RA1.SCHOOL_KIND   = RRS.SCHOOL_KIND ");
            stb.append("       AND RA1.CURRICULUM_CD = RRS.CURRICULUM_CD ");
            stb.append("       AND RA1.SUBCLASSCD    = RRS.SUBCLASSCD ");
            stb.append("       AND RA1.GRADE         = SRD.GRADE ");
            stb.append("       AND RA1.HR_CLASS      = SRD.HR_CLASS ");
            stb.append(" LEFT JOIN RECORD_AVERAGE_SDIV_DAT RA2 ");
            stb.append("        ON RA2.YEAR          = SRD.YEAR ");
            stb.append("       AND RA2.SEMESTER      = RRS.SEMESTER ");
            stb.append("       AND RA2.TESTKINDCD    = RRS.TESTKINDCD ");
            stb.append("       AND RA2.TESTITEMCD    = RRS.TESTITEMCD ");
            stb.append("       AND RA2.SCORE_DIV     = RRS.SCORE_DIV ");
            stb.append("       AND RA2.CLASSCD       = RRS.CLASSCD ");
            stb.append("       AND RA2.SCHOOL_KIND   = RRS.SCHOOL_KIND ");
            stb.append("       AND RA2.CURRICULUM_CD = RRS.CURRICULUM_CD ");
            stb.append("       AND RA2.SUBCLASSCD    = RRS.SUBCLASSCD ");
            stb.append("       AND RA2.GRADE         = SRD.GRADE ");
            stb.append("       AND RA2.HR_CLASS      = '000' ");
            stb.append("       AND RA2.AVG_DIV       = '1' ");
            stb.append(" ORDER BY SRD.SCHREGNO");
            stb.append("        , RRS.SEMESTER");

            return stb.toString();
        }

        /**定期試験**/
        /**生徒毎、試験毎、科目毎、素点**/
        private String sqlTest()
        {
            final StringBuffer stb = new StringBuffer();
            stb.append("   SELECT SRD.SCHREGNO ");
            stb.append("        , TCS.TESTITEMNAME ");  //試験種別名
            stb.append("        , TCS.SEMESTER ");      //学期
            stb.append("        , TCS.TESTKINDCD ");    //試験種別
            stb.append("        , SUB.SUBCLASSCD ");    //科目CD
            stb.append("        , SUB.SUBCLASSABBV ");  //科目名
            stb.append("        , RSD.SCORE ");         //素点
            stb.append("     FROM SCHREG_REGD_DAT SRD ");
            stb.append("LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV TCS ");
            stb.append("       ON TCS.YEAR           = SRD.YEAR ");
            stb.append("      AND TCS.SEMESTER       = SRD.SEMESTER ");
            stb.append("      AND TCS.SCORE_DIV      = '01' ");
            stb.append("LEFT JOIN RECORD_SCORE_DAT RSD ");
            stb.append("       ON RSD.YEAR           = SRD.YEAR ");
            stb.append("      AND RSD.SEMESTER       = SRD.SEMESTER ");
            stb.append("      AND RSD.TESTKINDCD     = TCS.TESTKINDCD ");
            stb.append("      AND RSD.TESTITEMCD     = TCS.TESTITEMCD ");
            stb.append("      AND RSD.SCHREGNO       = SRD.SCHREGNO ");
            stb.append("LEFT JOIN SUBCLASS_MST SUB ");
            stb.append("       ON SUB.CLASSCD        = RSD.CLASSCD ");
            stb.append("      AND SUB.SCHOOL_KIND    = RSD.SCHOOL_KIND ");
            stb.append("      AND SUB.CURRICULUM_CD  = RSD.CURRICULUM_CD ");
            stb.append("      AND SUB.SUBCLASSCD     = RSD.SUBCLASSCD ");
            stb.append("    WHERE SRD.YEAR           = '" + _param._year + "' ");
            stb.append("      AND SRD.SEMESTER      <= "  + _param._semester);
            stb.append("      AND RSD.SCHOOL_KIND    = '" + _param._schoolKind + "' ");
            stb.append("	  AND SRD.SCHREGNO       = '" + _schregno + "' ");
            stb.append(" ORDER BY SRD.SCHREGNO");
            stb.append("        , SRD.SEMESTER ");
            stb.append("        , TCS.TESTKINDCD ");
            stb.append("        , RSD.CLASSCD ");
            stb.append("        , RSD.SUBCLASSCD ");

            return stb.toString();
        }

        /**定期試験**/
        /**生徒毎、試験毎、平均点、順位**/
        private String sqlTest2()
        {
            final StringBuffer stb = new StringBuffer();
            stb.append("    SELECT SRD.SCHREGNO ");
            stb.append("         , SRD.SEMESTER ");
            stb.append("         , TCS.TESTKINDCD ");
            stb.append("         , RSD.AVG ");                   //平均点
            stb.append("         , RSD.CLASS_AVG_RANK ");        //クラス順位、平均点基準
            stb.append("         , RSD.CLASS_RANK ");            //クラス順位、合計点基準
            stb.append("         , RSD.GRADE_AVG_RANK ");        //学年順位、  平均点基準
            stb.append("         , RSD.GRADE_RANK ");            //学年順位、  合計点基準
            stb.append("         , RA1.COUNT AS CLASS_COUNT ");  //クラス人数
            stb.append("         , RA2.COUNT AS GRADE_COUNT ");  //学年人数
            stb.append("         , RAC.AVG   AS CLASS_AVG ");    //クラス平均
            stb.append("      FROM SCHREG_REGD_DAT      SRD ");
            stb.append(" LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV TCS ");
            stb.append("        ON TCS.YEAR          = SRD.YEAR ");
            stb.append("       AND TCS.SEMESTER      = SRD.SEMESTER ");
            stb.append("       AND TCS.SCORE_DIV     = '01' ");
            stb.append( "LEFT JOIN RECORD_RANK_SDIV_DAT RSD ");
            stb.append("        ON RSD.YEAR          = SRD.YEAR ");
            stb.append("       AND RSD.SEMESTER      = SRD.SEMESTER ");
            stb.append("       AND RSD.SCHREGNO      = SRD.SCHREGNO ");
            stb.append("       AND RSD.TESTKINDCD    = TCS.TESTKINDCD ");
            stb.append("       AND RSD.TESTITEMCD    = TCS.TESTITEMCD ");
            stb.append("       AND RSD.SCORE_DIV     = '01' ");
            stb.append("       AND RSD.SUBCLASSCD    = '999999' ");
            stb.append("       AND RSD.SCHOOL_KIND   = '" + _param._schoolKind + "' ");
            stb.append(" LEFT JOIN RECORD_AVERAGE_SDIV_DAT RA1 ");
            stb.append("        ON RA1.YEAR          = SRD.YEAR ");
            stb.append("       AND RA1.SEMESTER      = SRD.SEMESTER ");
            stb.append("       AND RA1.TESTKINDCD    = TCS.TESTKINDCD ");
            stb.append("       AND RA1.TESTITEMCD    = TCS.TESTITEMCD ");
            stb.append("       AND RA1.SCORE_DIV     = TCS.SCORE_DIV ");
            stb.append("       AND RA1.CLASSCD       = RSD.CLASSCD ");
            stb.append("       AND RA1.SCHOOL_KIND   = RSD.SCHOOL_KIND ");
            stb.append("       AND RA1.CURRICULUM_CD = RSD.CURRICULUM_CD ");
            stb.append("       AND RA1.SUBCLASSCD    = RSD.SUBCLASSCD ");
            stb.append("       AND RA1.GRADE         = SRD.GRADE ");
            stb.append("       AND RA1.HR_CLASS      = SRD.HR_CLASS ");
            stb.append(" LEFT JOIN RECORD_AVERAGE_SDIV_DAT RA2 ");
            stb.append("        ON RA2.YEAR          = SRD.YEAR ");
            stb.append("       AND RA2.SEMESTER      = SRD.SEMESTER ");
            stb.append("       AND RA2.TESTKINDCD    = TCS.TESTKINDCD ");
            stb.append("       AND RA2.TESTITEMCD    = TCS.TESTITEMCD ");
            stb.append("       AND RA2.SCORE_DIV     = TCS.SCORE_DIV ");
            stb.append("       AND RA2.CLASSCD       = RSD.CLASSCD ");
            stb.append("       AND RA2.SCHOOL_KIND   = RSD.SCHOOL_KIND ");
            stb.append("       AND RA2.CURRICULUM_CD = RSD.CURRICULUM_CD ");
            stb.append("       AND RA2.SUBCLASSCD    = RSD.SUBCLASSCD ");
            stb.append("       AND RA2.GRADE         = SRD.GRADE ");
            stb.append("       AND RA2.HR_CLASS      = '000' ");
            stb.append("       AND RA2.AVG_DIV       = '1' ");
            stb.append(" LEFT JOIN RECORD_AVERAGE_CLASS_SDIV_DAT RAC ");
            stb.append("        ON RAC.YEAR          = SRD.YEAR ");
            stb.append("       AND RAC.SEMESTER      = SRD.SEMESTER ");
            stb.append("       AND RAC.TESTKINDCD    = TCS.TESTKINDCD ");
            stb.append("       AND RAC.TESTITEMCD    = TCS.TESTITEMCD ");
            stb.append("       AND RAC.GRADE         = SRD.GRADE ");
            stb.append("       AND RAC.HR_CLASS      = SRD.HR_CLASS ");
            stb.append("       AND RAC.SCORE_DIV     = '01' ");
            stb.append("       AND RAC.CLASS_DIV     = '9' ");
            stb.append("     WHERE SRD.YEAR          = '" + _param._year + "' ");
            stb.append("       AND SRD.SEMESTER     <= "  + _param._semester);
            stb.append("	   AND SRD.SCHREGNO      = '" + _schregno + "' ");

            return stb.toString();
        }

        /**自立活動、総合的な探求の時間、取得資格・検定、所見**/
        private String sqlSyoken()
        {
            final StringBuffer stb = new StringBuffer();
            stb.append("   SELECT SRD.SCHREGNO ");
            stb.append("        , HDD.REMARK1 AS JIRITSU ");  //自立活動
            stb.append("        , HCD.REMARK1 AS MIDASHI ");  //みだし
            stb.append("        , RTD.TOTALSTUDYTIME ");      //総合的な探求の時間
            stb.append("        , HRP.REMARK1 AS SHIKAKU ");  //資格取得・検定
            stb.append("        , HRP.COMMUNICATION ");       //所見
            stb.append("     FROM SCHREG_REGD_DAT SRD ");
            stb.append("LEFT JOIN HREPORTREMARK_DETAIL_DAT HDD ");
            stb.append("       ON HDD.YEAR         = SRD.YEAR ");
            stb.append("      AND HDD.SEMESTER     = '3' ");
            stb.append("      AND HDD.SCHREGNO     = SRD.SCHREGNO ");
            stb.append("      AND HDD.DIV          = '01' ");
            stb.append("      AND HDD.CODE         = '01' ");
            stb.append("LEFT JOIN HREPORT_CONDITION_DAT HCD ");
            stb.append("       ON HCD.YEAR         = SRD.YEAR ");
            stb.append("      AND HCD.SEQ          = '013' ");
            stb.append("LEFT JOIN RECORD_TOTALSTUDYTIME_DAT RTD ");
            stb.append("       ON RTD.YEAR         = SRD.YEAR ");
            stb.append("      AND RTD.SEMESTER     = HDD.SEMESTER ");
            stb.append("      AND RTD.SCHREGNO     = SRD.SCHREGNO ");
            stb.append("      AND RTD.SCHOOL_KIND  = HCD.SCHOOL_KIND ");
            stb.append("LEFT JOIN HREPORTREMARK_DAT HRP ");
            stb.append("       ON HRP.YEAR         = SRD.YEAR ");
            stb.append("      AND HRP.SEMESTER     = HDD.SEMESTER ");
            stb.append("      AND HRP.SCHREGNO     = SRD.SCHREGNO ");
            stb.append("    WHERE SRD.YEAR         = '" + _param._year + "' ");
            if (SEMEALL.equals(_param._semester)) {
                stb.append("  AND SRD.SEMESTER     = '" + _param._ctrlSeme + "' ");
            } else {
                stb.append("  AND SRD.SEMESTER     = '" + _param._semester + "' ");
            }
            stb.append("      AND HCD.SCHOOLCD     = '000000000000'  ");
            stb.append("      AND HCD.SCHOOL_KIND  = '" + _param._schoolKind + "' ");
            stb.append("	  AND SRD.SCHREGNO     = '" + _schregno + "' ");

            return stb.toString();
        }

        /**欠時、備考**/
        private String sqlKetsujiBikou()
        {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH ASD AS ( ");
            stb.append("              SELECT SCHREGNO ");
            stb.append("                   , SEMESTER ");
            stb.append("                   , SUM(NVL(SICK, 0))       AS SICK ");
            stb.append("                   , SUM(NVL(NOTICE, 0))     AS NOTICE ");
            stb.append("                   , SUM(NVL(NONOTICE, 0))   AS NONOTICE ");
            stb.append("                FROM ATTEND_SUBCLASS_DAT ");
            stb.append("               WHERE YEAR     = '" + _param._year + "' ");
            stb.append("            GROUP BY SCHREGNO ");
            stb.append("                   , SEMESTER ");
            stb.append(" ) ");
            stb.append("   SELECT SRD.SCHREGNO ");
            stb.append("        , SRD.SEMESTER ");
            stb.append("        , ASD.SICK + ASD.NOTICE + ASD.NONOTICE AS KEKKA_JISU");
            stb.append("        , HRP.ATTENDREC_REMARK ");
            stb.append("     FROM SCHREG_REGD_DAT   SRD ");
            stb.append("LEFT JOIN ASD ");
            stb.append("       ON ASD.SCHREGNO        = SRD.SCHREGNO ");
            stb.append("      AND ASD.SEMESTER        = SRD.SEMESTER ");
            stb.append("LEFT JOIN HREPORTREMARK_DAT HRP ");
            stb.append("       ON HRP.SCHREGNO        = SRD.SCHREGNO ");
            stb.append("      AND HRP.YEAR            = SRD.YEAR ");
            stb.append("      AND HRP.SEMESTER        = SRD.SEMESTER ");
            stb.append("    WHERE SRD.YEAR            = '" + _param._year + "' ");
            stb.append("      AND SRD.SEMESTER       <= "  + _param._semester);
            stb.append("	  AND SRD.SCHREGNO        = '" + _schregno + "' ");

            return stb.toString();
        }

        /**科目毎の警告点**/
        private String sqlKeikokuten()
        {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH RSD AS ( ");
            stb.append("      SELECT * ");
            stb.append("        FROM RECORD_SCORE_DAT RSD ");
            stb.append("       WHERE RSD.YEAR       = '" + _param._year + "' ");
            stb.append("         AND RSD.TESTKINDCD = '99' ");
            stb.append("         AND RSD.TESTITEMCD = '00' ");
            stb.append("         AND RSD.SCORE_DIV  = '08' ");
            stb.append("         AND RSD.SCHREGNO   = '" + _schregno + "' ");
            stb.append("         AND RSD.SEMESTER  <= "  + _param._semester);
            stb.append(" ) ");
            stb.append(" , CGSD AS ( ");
            stb.append("       SELECT RSD.YEAR ");
            stb.append("            , RSD.SEMESTER ");
            stb.append("            , RSD.SUBCLASSCD ");
            stb.append("            , RSD.CHAIRCD ");
            stb.append("            , CASE WHEN CGSD_1.CHAIR_GROUP_CD IS NOT NULL ");
            stb.append("                   THEN CGSD_1.CHAIR_GROUP_CD ");
            stb.append("                   WHEN CGSD_2.CHAIR_GROUP_CD IS NOT NULL ");
            stb.append("                   THEN CGSD_2.CHAIR_GROUP_CD ");
            stb.append("                   ELSE '000' ");
            stb.append("              END CHAIR_GROUP_CD ");
            stb.append("         FROM RSD ");
            stb.append("    LEFT JOIN CHAIR_GROUP_SDIV_DAT CGSD_1 ");
            stb.append("           ON CGSD_1.YEAR       = RSD.YEAR ");
            stb.append("          AND CGSD_1.SEMESTER   = RSD.SEMESTER ");
            stb.append("          AND CGSD_1.CHAIRCD    = RSD.CHAIRCD ");
            stb.append("          AND CGSD_1.TESTKINDCD = '00' ");
            stb.append("          AND CGSD_1.TESTITEMCD = '00' ");
            stb.append("          AND CGSD_1.SCORE_DIV  = '00' ");
            stb.append("    LEFT JOIN CHAIR_GROUP_SDIV_DAT CGSD_2 ");
            stb.append("           ON CGSD_2.YEAR       = RSD.YEAR ");
            stb.append("          AND CGSD_2.SEMESTER   = RSD.SEMESTER ");
            stb.append("          AND CGSD_2.CHAIRCD    = RSD.CHAIRCD ");
            stb.append("          AND CGSD_2.TESTKINDCD = RSD.TESTKINDCD ");
            stb.append("          AND CGSD_2.TESTITEMCD = RSD.TESTITEMCD ");
            stb.append("          AND CGSD_2.SCORE_DIV  = RSD.SCORE_DIV ");
            stb.append(" ) ");
            stb.append("       SELECT CGSD.SEMESTER ");
            stb.append("            , CGSD.SUBCLASSCD ");
            stb.append("            , CASE SDD.SCHOOL_REMARK1 ");
            stb.append("                   WHEN '1' THEN SDD.SCHOOL_REMARK2 ");
            stb.append("                   WHEN '2' THEN RASD.AVG * SDD.SCHOOL_REMARK2 / SDD.SCHOOL_REMARK3 ");
            stb.append("                   ELSE AM.ASSESSHIGH ");
            stb.append("              END AS KEIKOKUTEN ");
            stb.append("         FROM CGSD ");
            stb.append("    LEFT JOIN SCHOOL_DETAIL_DAT SDD ");
            stb.append("           ON SDD.YEAR        = CGSD.YEAR ");
            stb.append("          AND SDD.SCHOOLCD    = '000000000000' ");
            stb.append("          AND SDD.SCHOOL_SEQ  = '009' ");
            stb.append("    LEFT JOIN RECORD_AVERAGE_SDIV_DAT RASD ");
            stb.append("           ON RASD.YEAR       = CGSD.YEAR ");
            stb.append("          AND RASD.SEMESTER   = CGSD.SEMESTER ");
            stb.append("          AND RASD.MAJORCD    = CGSD.CHAIR_GROUP_CD ");
            stb.append("          AND RASD.SUBCLASSCD = CGSD.SUBCLASSCD ");
            stb.append("          AND RASD.AVG_DIV    = '6' ");
            stb.append("          AND RASD.GRADE      = '" + _param._grade + "' ");
            stb.append("          AND RASD.TESTKINDCD = '99' ");
            stb.append("          AND RASD.TESTITEMCD = '00' ");
            stb.append("          AND RASD.SCORE_DIV  = '08' ");
            stb.append("        , ASSESS_MST AM ");
            stb.append("    WHERE AM.ASSESSCD         = '2' ");
            stb.append("      AND AM.ASSESSLEVEL      = '1' ");
            stb.append(" ORDER BY CGSD.SEMESTER ");
            stb.append("        , CGSD.CHAIRCD ");

            return stb.toString();
        }
    }

    private static class ScoreData {
        final String _classcd;
        final String _subclasscd;
        final String _subclassname;
        final String _credits;
        final String _subclassabbv;
        final Map _scoreMap      = new HashMap(); // 得点
        final Map _tuishiMap     = new HashMap(); // 追試の評価
        final Map _noteiceCntMap = new HashMap(); // 欠課
        final Map _avgMap        = new HashMap(); // 平均点
        final Map _gradeRankMap  = new HashMap(); // 学年順位
        final Map _hrRankMap     = new HashMap(); // クラス順位
        final Map _courceRankMap = new HashMap(); // コース順位
        final Map _majorRankMap  = new HashMap(); // 専攻順位
        final Map _keikokutenMap = new HashMap(); // 警告点
        final Map _nurseoffMap   = new HashMap();
        final Map _offdaysMap    = new HashMap();
        final Map _absentMap     = new HashMap();
        final Map _suspendMap    = new HashMap();
        final Map _mourningMap   = new HashMap();
        final Map _virusMap      = new HashMap();
        final Map _gappeiMap     = new HashMap(); // 合併科目

        private ScoreData(
                final String classcd,
                final String subclasscd,
                final String subclassname,
                final String credits,
                final String subclassabbv
        ) {
            _classcd = classcd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _credits = credits;
            _subclassabbv = subclassabbv;
        }

        public String score(final String sdiv) {
            return StringUtils.defaultString((String) _scoreMap.get(sdiv), "");
        }

        public String tuishi(final String sdiv) {
            return StringUtils.defaultString((String) _tuishiMap.get(sdiv), "");
        }

        public String notice(final String sdiv) {
            return StringUtils.defaultString((String) _noteiceCntMap.get(sdiv), "0");
        }

        public String avg(final String sdiv) {
            return StringUtils.defaultString((String) _avgMap.get(sdiv), "");
        }

        public String keikokuten(final String sdiv) {
            return StringUtils.defaultString((String) _keikokutenMap.get(sdiv), "0");
        }

        public String toString() {
            return "ScoreData(" + _subclasscd + ":" + _subclassname + ", scoreMap = " + _scoreMap + ")";
        }

        public String nurseoff(final String sdiv) {
            return StringUtils.defaultString((String) _nurseoffMap.get(sdiv), "0");
        }

        public String offdays(final String sdiv) {
            return StringUtils.defaultString((String) _offdaysMap.get(sdiv), "0");
        }

        public String absent(final String sdiv) {
            return StringUtils.defaultString((String) _absentMap.get(sdiv), "0");
        }

        public String suspend(final String sdiv) {
            return StringUtils.defaultString((String) _suspendMap.get(sdiv), "0");
        }

        public String mourning(final String sdiv) {
            return StringUtils.defaultString((String) _mourningMap.get(sdiv), "0");
        }

        public String virus(final String sdiv) {
            return StringUtils.defaultString((String) _virusMap.get(sdiv), "0");
        }

        public String gappei(final String subclasscd) {
            return StringUtils.defaultString((String) _gappeiMap.get(subclasscd), "0");
        }
    }

    private static class SyutokuData {
        final String _subclasscd;
        final Map _scoreMap     = new HashMap(); //評定
        final Map _getCreditMap = new HashMap(); //修得単位数
        final Map _addCreditMap = new HashMap(); //増加単位数

        private SyutokuData(final String subclasscd) {
            _subclasscd = subclasscd;
        }

        public String score(final String sdiv) {
            return StringUtils.defaultString((String) _scoreMap.get(sdiv), "");
        }

        public String getCredit(final String sdiv) {
            return StringUtils.defaultString((String) _getCreditMap.get(sdiv), "");
        }

        public String addCredit(final String sdiv) {
            return StringUtils.defaultString((String) _addCreditMap.get(sdiv), "");
        }
    }

    private static class Rank {
        final String _rank;
        final String _count;
        public Rank(final String rank, final String count) {
            _rank = rank;
            _count = count;
        }
    }

    private static class TeikikousaData {
        final String _subclasscd;
        final String _subclassname;
        final Map _testName = new HashMap(); // テスト種別名
        final Map _scoreMap = new HashMap(); // 得点

        private TeikikousaData(
                final String subclasscd,
                final String subclassname
        ) {
            _subclasscd   = subclasscd;
            _subclassname = subclassname;
        }

        public String testName(final Object testType) {
            return StringUtils.defaultString((String) _testName.get(testType), "");
        }

        public String score(final String sdiv) {
            return StringUtils.defaultString((String) _scoreMap.get(sdiv), "");
        }
    }

    private static class Attendance {
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _sick;
        final int _present;
        final int _late;
        final int _early;
        final int _abroad;
        final int _absent;
        final int _sick_only;
        final int _notice_only;
        final int _nonotice_only;
        final int _virus;
        final int _koudome;

        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int sick,
                final int present,
                final int late,
                final int early,
                final int abroad,
                final int absent,
                final int sick_only,
                final int notice_only,
                final int nonotice_only,
                final int virus,
                final int koudome
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _sick = sick;
            _present = present;
            _late = late;
            _early = early;
            _abroad = abroad;
            _absent = absent;
            _sick_only = sick_only;
            _notice_only = notice_only;
            _nonotice_only = nonotice_only;
            _virus = virus;
            _koudome = koudome;
        }

        private static void load(
                final DB2UDB db2,
                final Param _param,
                final List studentList
        ) {
            final String sdate = _param._year + "-04-01";
            final String edate = (toInt(_param._year, 0) + 1) + "-04-01" ;

            PreparedStatement psAtSeme = null;
            ResultSet rsAtSeme = null;
            String semester = null;
            try {
                _param._attendParamMap.put("schregno", "?");

                String sql = AttendAccumulate.getAttendSemesSql(
                        _param._year,
                        _param._semester,
                        sdate,
                        edate,
                        _param._attendParamMap
                );

                psAtSeme = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    psAtSeme.setString(1, student._schregno);
                    rsAtSeme = psAtSeme.executeQuery();

                    while (rsAtSeme.next()) {
                        if (SEMEALL.equals(rsAtSeme.getString("SEMESTER"))) {
                            continue;
                        }
                        semester = rsAtSeme.getString("SEMESTER");

                        final Attendance attendance = new Attendance(
                                rsAtSeme.getInt("LESSON"),
                                rsAtSeme.getInt("MLESSON"),
                                rsAtSeme.getInt("SUSPEND"),
                                rsAtSeme.getInt("MOURNING"),
                                rsAtSeme.getInt("SICK"),
                                rsAtSeme.getInt("PRESENT"),
                                rsAtSeme.getInt("LATE"),
                                rsAtSeme.getInt("EARLY"),
                                rsAtSeme.getInt("TRANSFER_DATE"),
                                rsAtSeme.getInt("ABSENT"),
                                rsAtSeme.getInt("SICK_ONLY"),
                                rsAtSeme.getInt("NOTICE_ONLY"),
                                rsAtSeme.getInt("NONOTICE_ONLY"),
                                rsAtSeme.getInt("VIRUS"),
                                rsAtSeme.getInt("KOUDOME")
                        );
                        student._attendMap.put(semester, attendance);
                    }
                    DbUtils.closeQuietly(rsAtSeme);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(psAtSeme);
                db2.commit();
            }
        }
    }

    private static class DateRange {
        final String _key;
        final String _name;
        final String _sdate;
        final String _edate;
        public DateRange(final String key, final String name, final String sdate, final String edate) {
            _key = key;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        public String toString() {
            return "DateRange(" + _key + ", " + _sdate + ", " + _edate + ")";
        }
    }

    private static class Address1 {
        final String _schregno;
        final String _sendZipcd;
        final String _sendAddr1;
        final String _sendAddr2;
        final String _sendName;

        public Address1(
                final String schregno
              , final String sendZipcd
              , final String sendAddr1
              , final String sendAddr2
              , final String sendName
                ) {
            _schregno = schregno;
            _sendZipcd = sendZipcd;
            _sendAddr1 = sendAddr1;
            _sendAddr2 = sendAddr2;
            _sendName  = sendName;
        }
    }

    private static class Address2 {
        final String _schregno;
        final String _guardZipcd;
        final String _guardAddr1;
        final String _guardAddr2;
        final String _guardName;

        public Address2(
                final String schregno
              , final String guardZipcd
              , final String guardAddr1
              , final String guardAddr2
              , final String guardName
                ) {
            _schregno = schregno;
            _guardZipcd = guardZipcd;
            _guardAddr1 = guardAddr1;
            _guardAddr2 = guardAddr2;
            _guardName  = guardName;
        }
    }

    private static class Param {
        private final String _year;
        private final String[] _schregnos;

        private final String _documentRoot;
        private String _imagepath;
        private String _extension;
        final boolean _isOutputDebug;
        private final String _schoolKind;
        private final String _kubun;
        private final String _semester;
        private final String _ctrlSeme;
        private final String _date;
        private final String _gradeHrclass;
        private final String _grade;

        /** 端数計算共通メソッド引数 */
        private final Map _attendParamMap;

        public Param(final DB2UDB db2, final HttpServletRequest request) {
            _year         = request.getParameter("YEAR");                    //年度
            _schoolKind   = request.getParameter("SCHOOL_KIND");
            _kubun        = request.getParameter("CATEGORY_IS_CLASS");       //対象一覧区分
            _semester     = request.getParameter("SEMESTER");                //学期
            _ctrlSeme     = request.getParameter("CTRL_SEMESTER");
            _date         = request.getParameter("DATE").replace("/", "-");  //異動対象日付及び出欠集計日付
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            if (_gradeHrclass == null) {
                _grade    = request.getParameter("GRADE");
            } else {
                _grade    = _gradeHrclass.substring(0, 2);
            }

            // 学籍番号・クラスの指定
            _schregnos = request.getParameterValues("CLASS_SELECTED"); //学籍番号・クラス

            _documentRoot = request.getParameter("DOCUMENTROOT");
            //  写真データ
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            try {
                returnval = getinfo.Control(db2);
                _imagepath = returnval.val4;      //格納フォルダ
                _extension = returnval.val5;      //拡張子
            } catch (Exception e) {
                log.error("setHeader set error!", e);
            } finally {
                getinfo = null;
                returnval = null;
            }

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));

        }

        private String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD183E' AND NAME = '" + propName + "' "));
        }

        public File getImageFile(final String name) {
            final File file = new File(_documentRoot + "/" + _imagepath + "/" + name);
            log.info(file);
            if (_isOutputDebug) {
                log.info(" file " + file.getAbsolutePath() + " exists? " + file.exists());
            }
            if (file.exists()) {
                return file;
            }
            return null;
        }
    }

}//クラスの括り
