// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2021/03/11
 * 作成者: Nutec
 *
 */
package servletpack.KNJA;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *  学校教育システム 賢者 [学籍管理]
 *
 *                  ＜ＫＮＪＡ２２８Ａ＞  生徒カルテ
 */
public class KNJA228A {

    private static final Log log = LogFactory.getLog(KNJA228A.class);

    private boolean nonedata = false; //該当データなしフラグ

    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final Vrw32alp svf = new Vrw32alp(); //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null; //Databaseクラスを継承したクラス

        PrintWriter outstrm = null;
        try {
            //  print設定
            response.setContentType("application/pdf");
            outstrm = new PrintWriter(response.getOutputStream());

            //  svf設定
            svf.VrInit(); //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定

            sd.setSvfInit(request, response, svf);
            db2 = sd.setDb(request);
            if (sd.openDb(db2)) {
                log.error("db open error! ");
                return;
            }

            final Param param = getParam(db2, request);

            printSvfStudent(db2, svf, param);

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            //  該当データ無し
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            //  終了処理
            svf.VrQuit();
            db2.commit();
            db2.close(); //DBを閉じる
            outstrm.close(); //ストリームを閉じる
        }

    }//doGetの括り

    private void setForm(final Vrw32alp svf, final Param param, final String form, final int data) {
        svf.VrSetForm(form, data);
        if (param._isOutputDebug) {
            log.info(" form = " + form);
        }
    }

    /** 帳票出力 **/
    private void printSvfStudent(
            final DB2UDB db2,
            final Vrw32alp svf,
            final Param param) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            for (int i = 0; i < param._schregnos.length; i++) {
                String schregno[] = param._schregnos[i].split("-");
                String sql = sqlBase(param, schregno[0]);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                String schoolstampPath = null;
                final File schoolstampFile = param.getImageFile("SCHOOLSTAMP_H.bmp");
                if (null != schoolstampFile) {
                    schoolstampPath = schoolstampFile.getAbsolutePath();
                }
                log.info(" schoolstampPath = " + schoolstampPath);

                String schregimgPath = null;
                final File schregimg = param.getImageFile("P" + schregno[0] + "." + param._extension); //写真データ存在チェック用
                if (null != schregimg) {
                    schregimgPath = schregimg.getAbsolutePath();
                    log.info(" schregimg = " + schregimgPath);
                }

                final String form = "KNJA228A_1.xml";
                setForm(svf, param, form, 1);

                svf.VrsOut("PIC", schregimgPath);

                ArrayList yearList = new ArrayList(); //年度を格納するリスト
                ArrayList gradeNameList = new ArrayList(); //学年名を格納するリスト
                String ghaName = ""; //年組番

                //各年度の基本情報
                int count = 1;
                String prevNendo = "9999";
                while (rs.next()) {
                    //生徒毎に帳票1枚目と2枚目の出力を行う
                    ghaName = rs.getString("GRADE_NAME") + rs.getString("HR_CLASS_NAME2") + "組"+ Integer.parseInt(rs.getString("ATTENDNO")) + "番";

                    if (!prevNendo.equals(rs.getString("YEAR"))) {
                        yearList.add(rs.getString("YEAR"));
                        gradeNameList.add(rs.getString("GRADE_NAME"));
                        prevNendo = rs.getString("YEAR");
                    } else {
                        //印字位置を戻して印字(担任を上書きする)
                        count--;
                    }

                    svf.VrsOutn("YEAR1", count,KNJ_EditDate.gengou(db2, Integer.parseInt(rs.getString("YEAR"))) + "年度"); //年度
                    svf.VrsOutn("GRADE1", count, rs.getString("GRADE_NAME")); //学年
                    svf.VrsOutn("HR_CLASS1", count, rs.getString("HR_CLASS_NAME2")); //組
                    int attendno = Integer.parseInt(rs.getString("ATTENDNO"));
                    svf.VrsOutn("ATTENDNO1", count, attendno + ""); //出席番号

                    //学科名
                    {
                        final int majorNameLen = KNJ_EditEdit.getMS932ByteLength(rs.getString("MAJORNAME"));
                        final String majorNameField = (majorNameLen <= 16) ? "1" : "2";
                        svf.VrsOutn("MAJORNAME" + majorNameField, count, rs.getString("MAJORNAME"));
                    }

                    //担任名
                    {
                        final int staffNameLen = KNJ_EditEdit.getMS932ByteLength(rs.getString("STAFFNAME"));
                        final String staffNameField = (staffNameLen <= 14) ? "1" : (staffNameLen <= 20) ? "2" : "3";
                        svf.VrsOutn("STAFFNAME" + staffNameField, count, rs.getString("STAFFNAME"));
                    }

                    count++;
                }

                //生徒基本情報
                sql = "";
                sql = sqlStudentBase(param, schregno[0]);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                String name = "";
                while (rs.next()) {
                    name = rs.getString("NAME");
                    svf.VrsOut("KANA", rs.getString("NAME_KANA")); //生徒氏名かな
                    svf.VrsOut("NAME", rs.getString("NAME")); //生徒氏名
                    svf.VrsOut("BIRTHDAY", rs.getString("BIRTHDAY").replace("-", "/")); //生年月日
                    svf.VrsOut("SCHREGNO", rs.getString("SCHREGNO")); //学籍番号
                    svf.VrsOut("SEX", rs.getString("SEX_NAME")); //性別
                    svf.VrsOut("ADDR1", rs.getString("ADDR1")); //生徒住所1
                    svf.VrsOut("ADDR2", rs.getString("ADDR2")); //生徒住所2
                    svf.VrsOut("AREA_DIV", rs.getString("RESIDENT_NAME")); //住所区分
                    svf.VrsOut("AREA_NAME", rs.getString("AREA_NAME")); //住居地域
                    svf.VrsOut("TELNO", rs.getString("TELNO")); //電話番号(生徒)
                    svf.VrsOut("FAXNO", rs.getString("FAXNO")); //FAX番号(生徒)
                    svf.VrsOut("TELNO2", rs.getString("TELNO2")); //携帯番号(生徒)

                    //生徒メールアドレス
                    {
                        final int emailLen = KNJ_EditEdit.getMS932ByteLength(rs.getString("EMAIL"));
                        final String emailField = (emailLen <= 26) ? "1" : (emailLen <= 32) ? "2" : "3";
                        svf.VrsOut("EMAIL" + emailField, rs.getString("EMAIL"));
                    }

                    svf.VrsOut("COMMUTE_HOURS", rs.getString("COMMUTE_TIME")); //通学時間
                    svf.VrsOut("HOWTOCOMMUTE", rs.getString("HOWTOCOMMUTE_NAME")); //通学方法

                    //出身学校
                    {
                        final int finSchoolNameLen = KNJ_EditEdit.getMS932ByteLength(rs.getString("FINSCHOOL_NAME"));
                        final String finSchoolNameField = (finSchoolNameLen <= 26) ? "1"
                                : (finSchoolNameLen <= 32) ? "2" : "3";
                        svf.VrsOut("FINSCHOOL_NAME" + finSchoolNameField, rs.getString("FINSCHOOL_NAME"));
                    }

                    svf.VrsOut("ENT_DIV", rs.getString("ENT_DIV_NAME")); //入学区分
                    svf.VrsOut("PLANUNIV", rs.getString("PLANUNIV")); //進学希望
                    svf.VrsOut("PLANJOB", rs.getString("PLANJOB")); //就職希望
                }

                //生徒基本情報(留学)
                sql = "";
                sql = sqlStudyAbroad(param, schregno[0]);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                count = 1;
                while (rs.next()) {
                    if (count > 2) {
                        break;
                    }
                    if(rs.getString("TRANSFER_SDATE") != null && rs.getString("TRANSFER_EDATE") != null && rs.getString("TRANSFERPLACE") != null) {
                        svf.VrsOut("TRANSFER" + count, (rs.getString("TRANSFER_SDATE")).replace("-", "/") + "～"
                                + (rs.getString("TRANSFER_EDATE")).replace("-", "/")
                                + " " + rs.getString("TRANSFERPLACE")); //留学1
                        count++;
                    }
                }

                //特別活動の記録(部活動)
                sql = "";
                sql = sqlExtracurricularActivities(param, schregno[0]);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                count = 1;
                String[] clubNames = new String[yearList.size()];
                String[] comitNames = new String[yearList.size()];
                for (int j = 1; j <= clubNames.length; j++) {
                    svf.VrsOutn("YEAR2_1", j,
                            KNJ_EditDate.gengou(db2, Integer.parseInt((String) yearList.get(j - 1))) + "年度");
                    svf.VrsOutn("GRADE2", j, (String) gradeNameList.get(j - 1));
                    clubNames[j - 1] = "";
                    comitNames[j - 1] = "";
                }

                while (rs.next()) {
                    for (int j = 0; j < yearList.size(); j++) {
                        if (((String) yearList.get(j)).equals(rs.getString("YEAR")) == true) {
                            clubNames[j] += rs.getString("CLUBNAME") + " ";

                            if ("".equals(rs.getString("EXECUTIVE_NAME")) == false
                                    && rs.getString("EXECUTIVE_NAME") != null) {
                                clubNames[j] += rs.getString("EXECUTIVE_NAME") + " ";
                            }
                            if ("".equals(rs.getString("EDATE")) == false && rs.getString("EDATE") != null) {
                                clubNames[j] += rs.getString("EDATE").replace("-", "/") + " ";
                            }
                        }
                    }
                }
                for (int j = 1; j <= clubNames.length; j++) {
                    svf.VrsOutn("CLUBNAME1", j, clubNames[j - 1]);
                }

                //特別活動の記録(委員会・生徒会)
                sql = "";
                sql = sqlCommittee(param, schregno[0]);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                count = 1;
                while (rs.next()) {
                    for (int j = 0; j < yearList.size(); j++) {
                        if (((String) yearList.get(j)).equals(rs.getString("YEAR")) == true) {
                            comitNames[j] += rs.getString("COMMITTEENAME") + " ";
                        }
                    }
                }
                for (int j = 1; j <= comitNames.length; j++) {
                    svf.VrsOutn("COMMITEENAME1", j, comitNames[j - 1]);
                }

                //保護者情報1
                sql = "";
                sql = sqlGuardian1Data(param, schregno[0]);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int k = 1;
                while (rs.next()) {
                    //保護者名かな
                    {
                        final int guardKanaLen = KNJ_EditEdit.getMS932ByteLength(rs.getString("GUARD_KANA"));
                        final String guardKanaField = (guardKanaLen <= 40) ? "1" : (guardKanaLen <= 50) ? "2" : "3";
                        svf.VrsOutn("GUARD_KANA" + guardKanaField, k, rs.getString("GUARD_KANA"));
                    }
                    svf.VrsOutn("RELATIONSHIP", k, rs.getString("RELATION_NAME")); //続柄(保護者)
                    svf.VrsOutn("GUARD_NAME", k, rs.getString("GUARD_NAME")); //保護者氏名
                    svf.VrsOutn("GUARD_ADDR1", k, rs.getString("GUARD_ADDR1")); //保護者住所1
                    svf.VrsOutn("GUARD_ADDR2", k, rs.getString("GUARD_ADDR2")); //保護者住所2

                    //保護者メールアドレス
                    {
                        final int guardEMailLen = KNJ_EditEdit.getMS932ByteLength(rs.getString("GUARD_KANA"));
                        final String guardEMailField = (guardEMailLen <= 26) ? "1" : (guardEMailLen <= 32) ? "2" : "3";
                        svf.VrsOutn("GUARD_EMAIL" + guardEMailField, k, rs.getString("GUARD_E_MAIL")); //保護者メールアドレス
                    }
                    svf.VrsOutn("GUARD_TELNO", k, rs.getString("GUARD_TELNO")); //保護者電話番号
                }

                //保護者情報2がここに入る
                sql = "";
                sql = sqlGuardian2Data(param, schregno[0]);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                k = 2;
                while (rs.next()) {
                    svf.VrsOutn("GUARD_KANA1", k, rs.getString("GUARD_KANA")); //保護者名かな
                    svf.VrsOutn("RELATIONSHIP", k, rs.getString("RELATION_NAME")); //続柄(保護者)
                    svf.VrsOutn("GUARD_NAME", k, rs.getString("GUARD_NAME")); //保護者氏名
                    svf.VrsOutn("GUARD_ADDR1", k, rs.getString("GUARD_ADDR1")); //保護者住所1
                    svf.VrsOutn("GUARD_ADDR2", k, rs.getString("GUARD_ADDR2")); //保護者住所1
                    svf.VrsOutn("GUARD_EMAIL1", k, rs.getString("GUARD_E_MAIL")); //保護者メールアドレス1
                    svf.VrsOutn("GUARD_TELNO", k, rs.getString("GUARD_TELNO")); //保護者電話番号
                }

                //身元引受人情報がここにはいる
                sql = "";
                sql = sqlGuaranterData(param, schregno[0]);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                k = 3;
                while (rs.next()) {
                    svf.VrsOutn("GUARD_KANA1", k, rs.getString("GUARANTOR_KANA")); //身元引受人氏名かな
                    svf.VrsOutn("RELATIONSHIP", k, rs.getString("RELATION_NAME")); //続柄(身元引受人)
                    svf.VrsOutn("GUARD_NAME", k, rs.getString("GUARANTOR_NAME")); //身元引受人氏名
                    svf.VrsOutn("GUARD_ADDR1", k, rs.getString("GUARANTOR_ADDR1")); //身元引受人住所1
                    svf.VrsOutn("GUARD_ADDR2", k, rs.getString("GUARANTOR_ADDR2")); //身元引受人住所1
                    svf.VrsOutn("GUARD_TELNO", k, rs.getString("GUARANTOR_TELNO")); //身元引受人電話番号

                    log.info("住所1："    + rs.getString("GUARANTOR_ADDR1"));
                    log.info("住所2："    + rs.getString("GUARANTOR_ADDR2"));
                    log.info("電話番号：" + rs.getString("GUARANTOR_TELNO"));
                }

                //その他の情報がここにはいる
                sql = "";
                sql = sqlSendData(param, schregno[0]);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                k = 4;
                while (rs.next()) {
                    svf.VrsOutn("GUARD_KANA1", k, rs.getString("SEND_KANA")); //その他氏名かな
                    svf.VrsOutn("RELATIONSHIP", k, rs.getString("RELATION_NAME")); //続柄(その他)
                    svf.VrsOutn("GUARD_NAME", k, rs.getString("SEND_NAME")); //その他氏名
                    svf.VrsOutn("GUARD_ADDR1", k, rs.getString("SEND_ADDR1")); //その他住所1
                    svf.VrsOutn("GUARD_ADDR2", k, rs.getString("SEND_ADDR2")); //その他住所1
                    svf.VrsOutn("GUARD_TELNO", k, rs.getString("SEND_TELNO")); //その他電話番号

                    break;
                }

                //緊急連絡先情報
                sql = "";
                sql = sqlEmergencyData(param, schregno[0]);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    //緊急連絡先氏名1
                    {
                        final int emergencyNameLen = KNJ_EditEdit
                                .getMS932ByteLength(rs.getString("EMERGENCYNAME1"));
                        final String emergencyNameField = (emergencyNameLen <= 52) ? "1" : "2";
                        svf.VrsOutn("EMERGENCYNAME" + emergencyNameField, 1, rs.getString("EMERGENCYNAME1"));
                    }

                    //緊急連絡先1
                    {
                        final int emergencyCallLen = KNJ_EditEdit
                                .getMS932ByteLength(rs.getString("EMERGENCYTELNO1"));
                        final String emergencyCallField = (emergencyCallLen <= 52) ? "1" : "2";
                        svf.VrsOutn("EMERGENCYCALL" + emergencyCallField, 1, rs.getString("EMERGENCYTELNO1"));
                    }

                    //緊急連絡先氏名2
                    {
                        final int emergencyNameLen = KNJ_EditEdit
                                .getMS932ByteLength(rs.getString("EMERGENCYNAME2"));
                        final String emergencyNameField = (emergencyNameLen <= 52) ? "1" : "2";
                        svf.VrsOutn("EMERGENCYNAME" + emergencyNameField, 2, rs.getString("EMERGENCYNAME2"));
                    }

                    //緊急連絡先2
                    {
                        final int emergencyCallLen = KNJ_EditEdit
                                .getMS932ByteLength(rs.getString("EMERGENCYTELNO2"));
                        final String emergencyCallField = (emergencyCallLen <= 52) ? "1" : "2";
                        svf.VrsOutn("EMERGENCYCALL" + emergencyCallField, 2, rs.getString("EMERGENCYTELNO2"));
                    }
                }

                //兄弟姉妹情報
                sql = "";
                sql = sqlSiblingsData(param, schregno[0]);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                count = 1;
                while (rs.next()) {
                    if (count > 4) {
                        break;
                    }

                    //兄弟姉妹かな
                    {
                        final int relaKanaLen = KNJ_EditEdit.getMS932ByteLength(rs.getString("RELAKANA"));
                        final String relaKanaField = (relaKanaLen <= 14) ? "1" : (relaKanaLen <= 20) ? "2" : "3";
                        svf.VrsOutn("RELA_KANA" + relaKanaField, count, rs.getString("RELAKANA"));
                    }

                    //兄弟姉妹氏名
                    {
                        final int relaNameLen = KNJ_EditEdit.getMS932ByteLength(rs.getString("RELANAME"));
                        final String relaNameField = (relaNameLen <= 14) ? "1" : (relaNameLen <= 20) ? "2" : "3";
                        svf.VrsOutn("RELA_NAME" + relaNameField, count, rs.getString("RELANAME"));
                    }

                    svf.VrsOutn("RELA_RELATIONSHIP", count, rs.getString("RELATION_NAME")); //兄弟姉妹続柄
                    svf.VrsOutn("RELA_HR_CLASS", count, rs.getString("GRADE_NAME")); //兄弟姉妹年組
                    count++;
                }
                svf.VrEndPage();

                //2ページ目
                final String form2 = "KNJA228A_2.xml";
                setForm(svf, param, form2, 1);

                svf.VrsOut("NAME", name);
                svf.VrsOut("HR_NAME", ghaName);

                //科目コード取得
                sql = "";
                sql = sqlSubClassCd(param, schregno[0]);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ArrayList subClassList = new ArrayList();
                ArrayList scoreYearList = new ArrayList();
                int yearCount = 0;
                while (rs.next()) {
                    //年度が同じとき
                    if (((String) yearList.get(yearCount)).equals(rs.getString("YEAR")) == true) {
                        scoreYearList.add(rs.getString("YEAR"));
                        subClassList.add(rs.getString("SUBCLASSCD"));
                    }
                    //年度が替わったとき
                    else {
                        yearCount++;
                        scoreYearList.add(rs.getString("YEAR"));
                        subClassList.add(rs.getString("SUBCLASSCD"));
                    }
                }

                //成績情報
                sql = "";
                sql = getScoreData(param, schregno[0]);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                int testCount = 0;
                int total = 0;
                double ave = 0.0;
                double num = 1.0;
                String testCd = "";
                int subClassCount = 1;
                yearCount = 0;
                while (rs.next()) {
                    //年度が同じとき
                    if (((String) yearList.get(yearCount)).equals(rs.getString("YEAR")) == true) {
                        svf.VrsOut("YEAR1_" + (yearCount + 1) + "_1",  KNJ_EditDate.gengou(db2, Integer.parseInt((String) yearList.get(yearCount))) + "年度"); //年度
                        svf.VrsOut("GRADE1_" + (yearCount + 1), (String) gradeNameList.get(yearCount));
                        if (testCount == 0) {
                            svf.VrsOutn("TESTITEMNAME" + (yearCount + 1), 1, rs.getString("TESTITEMNAME"));
                            for (int j = 0; j < subClassList.size(); j++) {
                                if (((String) scoreYearList.get(j)).equals(rs.getString("YEAR")) == true) {
                                    final int subClassNameLen = KNJ_EditEdit.getMS932ByteLength(rs.getString("SUBCLASSABBV"));
                                    final String subClassNameField = (subClassNameLen <= 6) ? "1" : "2";
                                    svf.VrsOutn("SUBCLASSABBV" + (yearCount + 1) + subClassNameField, subClassCount,rs.getString("SUBCLASSABBV"));
                                    if (((String) subClassList.get(j)).equals(rs.getString("SUBCLASSCD")) == true) {
                                        svf.VrsOutn("SCORE" + (yearCount + 1) + "_" + 1, subClassCount,rs.getString("SCORE"));
                                        total += rs.getInt("SCORE");
                                    }
                                }
                            }
                            subClassCount++;
                            testCount++;
                        }
                        //テスト種別が同じとき
                        if (testCd.equals(rs.getString("TESTCD")) == true && "".equals(testCd) == false) {
                            for (int j = 0; j < subClassList.size(); j++) {
                                if (((String) scoreYearList.get(j)).equals(rs.getString("YEAR")) == true) {
                                    svf.VrsOutn("SUBCLASSABBV" + (yearCount + 1) + "_1", subClassCount,rs.getString("SUBCLASSABBV"));
                                    if (((String) subClassList.get(j)).equals(rs.getString("SUBCLASSCD")) == true) {
                                        svf.VrsOutn("SCORE" + (yearCount + 1) + "_" + testCount, subClassCount,rs.getString("SCORE"));
                                        total += rs.getInt("SCORE");
                                    }
                                }
                            }
                            subClassCount++;
                        }
                        //テスト種別が変わったとき
                        if (testCd.equals(rs.getString("TESTCD")) == false && "".equals(testCd) == false) {
                            ave = total / (subClassCount * num );
                            svf.VrsOut("TOTAL" + (yearCount + 1) + "_" + testCount, new Integer(total).toString());
                            svf.VrsOut("AVE" + (yearCount + 1) + "_" + testCount, RoundHalfUp(String.valueOf(ave)));
                            testCount++;
                            subClassCount = 1;
                            total = 0;
                            ave = 0.0;
                            svf.VrsOutn("TESTITEMNAME" + (yearCount + 1), testCount, rs.getString("TESTITEMNAME"));
                            for (int j = 0; j < subClassList.size(); j++) {
                                if (((String) scoreYearList.get(j)).equals(rs.getString("YEAR")) == true) {
                                    svf.VrsOutn("SUBCLASSABBV" + (yearCount + 1) + "_1", subClassCount,rs.getString("SUBCLASSABBV"));
                                    if (((String) subClassList.get(j)).equals(rs.getString("SUBCLASSCD")) == true) {
                                        svf.VrsOutn("SCORE" + (yearCount + 1) + "_" + testCount, subClassCount,rs.getString("SCORE"));
                                        total += rs.getInt("SCORE");
                                    }
                                }
                            }
                            subClassCount++;
                        }
                    }
                    //年度が替わったとき
                    else {
                        ave = total / subClassCount;
                        svf.VrsOut("TOTAL" + (yearCount + 1) + "_" + testCount, new Integer(total).toString());
                        svf.VrsOut("AVE" + (yearCount + 1) + "_" + testCount, RoundHalfUp(String.valueOf(ave)));
                        yearCount++;
                        testCount = 0;
                        subClassCount = 1;
                        total = 0;
                        ave = 0.0;
                        svf.VrsOutn("TESTITEMNAME" + (yearCount + 1), 1, rs.getString("TESTITEMNAME"));
                        for (int j = 0; j < subClassList.size(); j++) {
                            if (((String) scoreYearList.get(j)).equals(rs.getString("YEAR")) == true) {
                                svf.VrsOutn("SUBCLASSABBV" + (yearCount + 1) + "_1", subClassCount,rs.getString("SUBCLASSABBV"));
                                if (((String) subClassList.get(j)).equals(rs.getString("SUBCLASSCD")) == true) {
                                    svf.VrsOutn("SCORE" + (yearCount + 1) + "_" + 1, subClassCount,rs.getString("SCORE"));
                                    total += rs.getInt("SCORE");
                                }
                            }
                        }
                        testCount++;
                        subClassCount++;
                    }
                    testCd = rs.getString("TESTCD");
                }
                //合計・平均の最後の表示
                ave = total / (subClassCount * num );
                svf.VrsOut("TOTAL" + (yearCount + 1) + "_" + testCount, new Integer(total).toString());
                svf.VrsOut("AVE" + (yearCount + 1) + "_" + testCount, RoundHalfUp(String.valueOf(ave)));

                //出欠情報取得
                sql = "";
                sql = getAttendanceData(param, schregno[0]);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                yearCount = 0;
                while (rs.next()) {
                    if (((String) yearList.get(yearCount)).equals(rs.getString("YEAR")) == true) {
                        svf.VrsOutn("YEAR2_1"       , (yearCount + 1),  KNJ_EditDate.gengou(db2, Integer.parseInt((String) yearList.get(yearCount))) + "年度"); //年度
                        svf.VrsOutn("GRADE2"        , (yearCount + 1), (String) gradeNameList.get(yearCount));
                        svf.VrsOutn("CLASSDAYS"     , (yearCount + 1), rs.getString("CLASSDAYS"));
                        svf.VrsOutn("MOURNING"      , (yearCount + 1), rs.getString("MOURNING"));
                        svf.VrsOutn("ABROAD"        , (yearCount + 1), rs.getString("ABROAD"));
                        svf.VrsOutn("SICK"          , (yearCount + 1), rs.getString("SICK"));
                        svf.VrsOutn("PRESENT"       , (yearCount + 1), rs.getString("PRESENT"));
                        svf.VrsOutn("LATE"          , (yearCount + 1), rs.getString("LATE"));
                        svf.VrsOutn("EARLY"         , (yearCount + 1), rs.getString("EARLY"));
                        svf.VrsOutn("ATTEND_REMARK1", (yearCount + 1), rs.getString("ATTENDREC_REMARK"));
                        yearCount++;
                    }
                }

                //資格取得
                sql = "";
                sql = getQualifiedData(param, schregno[0]);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                yearCount = 0;
                int sikakuCount = 0;
                ArrayList sikakuGradeList = new ArrayList();
                ArrayList regdDateList = new ArrayList();
                ArrayList sikakuList = new ArrayList();
                while (rs.next()) {
                    if(sikakuCount < 9) {
                        sikakuGradeList.add(rs.getString("GRADE_NAME"));
                        regdDateList.add(rs.getString("REGDDATE").replace('-', '/'));
                        sikakuList.add(rs.getString("SIKAKU"));
                    }
                    sikakuCount++;
                }
                for(int j = (sikakuGradeList.size() -1 ); j >= 0 ; j--) {
                    svf.VrsOutn("GRADE3"         , (yearCount + 1), (String) sikakuGradeList.get(j));
                    svf.VrsOutn("REGDDATE"       , (yearCount + 1), (String) regdDateList.get(j));
                    svf.VrsOutn("QUALIFIED_NAME" , (yearCount + 1), (String) sikakuList.get(j));
                    yearCount++;
                }
                svf.VrEndPage();
                nonedata = true;
            }
        } catch (Exception ex) {
            log.error("setSvfout set error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    /**四捨五入**/
    private String RoundHalfUp(String str) {
        BigDecimal bg = new BigDecimal(str);
        bg = bg.setScale(1, BigDecimal.ROUND_HALF_UP);
        return String.format("%.1f", bg);
    }

    /** 基本情報情報 **/
    private String sqlBase(final Param param, final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SRD AS ( ");
        stb.append("     SELECT SCHREGNO ");
        stb.append("          , YEAR ");
        stb.append("          , SEMESTER ");
        stb.append("          , GRADE ");
        stb.append("          , HR_CLASS ");
        stb.append("          , ATTENDNO ");
        stb.append("          , COURSECD ");
        stb.append("          , MAJORCD ");
        stb.append("       FROM SCHREG_REGD_DAT ");
        stb.append("      WHERE SCHREGNO = '" + schregno + "'");
        stb.append(" ) ");
        stb.append("    SELECT SRD.SCHREGNO "); //学籍番号
        stb.append("         , SRD.YEAR ");     //年度(西暦)
        stb.append("         , SRD.SEMESTER "); //学期
        stb.append("         , CASE WHEN SRD.GRADE = '01' THEN '1年' ");
        stb.append("                WHEN SRD.GRADE = '02' THEN '2年' ");
        stb.append("                WHEN SRD.GRADE = '03' THEN '3年' ");
        stb.append("                ELSE NULL ");
        stb.append("           END AS GRADE_NAME "); //学年
        stb.append("         , SRD.GRADE ");         //学年
        stb.append("         , SRH.HR_CLASS_NAME2 ");//組
        stb.append("         , SRD.ATTENDNO ");      //番号
        stb.append("         , MAJOR_M.MAJORNAME "); //学科
        stb.append("         , STAFF_M.STAFFNAME "); //担任
        stb.append("      FROM SRD ");
        stb.append(" LEFT JOIN SCHREG_REGD_HDAT SRH ");
        stb.append("        ON (SRH.YEAR     = SRD.YEAR ");
        stb.append("       AND  SRH.SEMESTER = SRD.SEMESTER ");
        stb.append("       AND  SRH.GRADE    = SRD.GRADE ");
        stb.append("       AND  SRH.HR_CLASS = SRD.HR_CLASS) ");
        stb.append(" LEFT JOIN MAJOR_MST MAJOR_M ");
        stb.append("        ON (MAJOR_M.COURSECD = SRD.COURSECD ");
        stb.append("       AND  MAJOR_M.MAJORCD  = SRD.MAJORCD) ");
        stb.append(" LEFT JOIN STAFF_CLASS_HIST_DAT SCHD ");
        stb.append("        ON (SCHD.YEAR     = SRD.YEAR ");
        stb.append("       AND  SCHD.SEMESTER = SRD.SEMESTER ");
        stb.append("       AND  SCHD.GRADE    = SRD.GRADE ");
        stb.append("       AND  SCHD.HR_CLASS = SRD.HR_CLASS ");
        stb.append("       AND  SCHD.TR_DIV   = '1') ");
        stb.append(" LEFT JOIN STAFF_MST STAFF_M ");
        stb.append("        ON (STAFF_M.STAFFCD = SCHD.STAFFCD) ");
        stb.append("  GROUP BY SRD.SCHREGNO ");
        stb.append("         , SRD.YEAR ");
        stb.append("         , SRD.SEMESTER ");
        stb.append("         , SRD.GRADE ");
        stb.append("         , SRH.HR_CLASS_NAME2 ");
        stb.append("         , SRD.ATTENDNO ");
        stb.append("         , MAJOR_M.MAJORNAME ");
        stb.append("         , STAFF_M.STAFFNAME ");
        stb.append("  ORDER BY SRD.SCHREGNO ");
        stb.append("         , SRD.YEAR ");
        stb.append("         , SRD.SEMESTER ");

        return stb.toString();
    }

    /** 生徒基本情報情報 **/
    private String sqlStudentBase(final Param param, final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHREG_ADDRESS_LAST AS ( ");
        stb.append("        SELECT SAD.SCHREGNO ");
        stb.append("             , MAX(SAD.ISSUEDATE) AS ISSUEDATE ");
        stb.append("          FROM SCHREG_ADDRESS_DAT SAD ");
        stb.append("         WHERE SAD.SCHREGNO = '" + schregno + "' ");
        stb.append("      GROUP BY SAD.SCHREGNO ");
        stb.append(" ) ");
        stb.append("    SELECT SB_M.NAME_KANA "); //氏名かな
        stb.append("         , SB_M.NAME "); //氏名
        stb.append("         , SB_M.SCHREGNO "); //学籍番号
        stb.append("         , CASE WHEN SB_M.SEX = '1' THEN '男' ");
        stb.append("                WHEN SB_M.SEX = '2' THEN '女' ");
        stb.append("                ELSE NULL ");
        stb.append("           END AS SEX_NAME "); //性別
        stb.append("         , SB_M.BIRTHDAY "); //生年月日
        stb.append("         , SAD.ADDR1 "); //住所1
        stb.append("         , SAD.ADDR2 "); //住所2
        stb.append("         , SED.RESIDENTCD ");
        stb.append("         , NM.NAME1 AS RESIDENT_NAME ");//住居区分
        stb.append("         , NM2.NAME1 AS AREA_NAME "); //居住区域
        stb.append("         , SAD.TELNO "); //電話番号
        stb.append("         , SAD.FAXNO "); //FAX番号
        stb.append("         , SAD.TELNO2 "); //携帯番号
        stb.append("         , SAD.EMAIL "); //メールアドレス
        stb.append("         , CASE WHEN SED.COMMUTE_HOURS IS NULL        THEN '' ");
        stb.append("                WHEN TO_NUMBER(SED.COMMUTE_HOURS) > 0 THEN TO_NUMBER(SED.COMMUTE_HOURS) || '時間' ");
        stb.append("                ELSE '' ");
        stb.append("           END ");
        stb.append("           || ");
        stb.append("           CASE WHEN SED.COMMUTE_MINUTES IS NULL THEN '' ");
        stb.append("                ELSE TO_NUMBER(SED.COMMUTE_MINUTES) || '分' ");
        stb.append("           END AS COMMUTE_TIME ");//通学時間 ");
        stb.append("         , NM3.NAME1 AS HOWTOCOMMUTE_NAME ");//通学方法
        stb.append("         , FM.FINSCHOOL_NAME AS FINSCHOOL_NAME "); //出身校
        stb.append("         , NM4.NAME1 AS ENT_DIV_NAME "); //入学区分
        stb.append("         , SED.PLANUNIV "); //進路希望(進学)
        stb.append("         , SED.PLANJOB "); //進路希望(就職)
        stb.append("      FROM SCHREG_ADDRESS_LAST SAL ");
        stb.append("      JOIN SCHREG_BASE_MST SB_M ");
        stb.append("        ON (SB_M.SCHREGNO  = SAL.SCHREGNO) ");
        stb.append(" LEFT JOIN SCHREG_ADDRESS_DAT SAD ");
        stb.append("        ON (SAD.SCHREGNO  = SAL.SCHREGNO ");
        stb.append("       AND  SAD.ISSUEDATE = SAL.ISSUEDATE) ");
        stb.append(" LEFT JOIN SCHREG_ENVIR_DAT SED ");
        stb.append("        ON (SED.SCHREGNO  = SB_M.SCHREGNO) ");
        stb.append(" LEFT JOIN NAME_MST NM ");
        stb.append("        ON (NM.NAMECD1 = 'H108' ");
        stb.append("       AND  NM.NAMECD2 = SED.RESIDENTCD) ");
        stb.append(" LEFT JOIN NAME_MST NM2 ");
        stb.append("        ON (NM2.NAMECD1 = 'A020' ");
        stb.append("       AND  NM2.NAMECD2 = SAD.AREACD) ");
        stb.append(" LEFT JOIN NAME_MST NM3 ");
        stb.append("        ON (NM3.NAMECD1 = 'H100' ");
        stb.append("       AND  NM3.NAMECD2 = SED.HOWTOCOMMUTECD) ");
        stb.append(" LEFT JOIN NAME_MST NM4 ");
        stb.append("        ON (NM4.NAMECD1 = 'A002' ");
        stb.append("       AND  NM4.NAMECD2 = SB_M.ENT_DIV) ");
        stb.append(" LEFT JOIN FINSCHOOL_MST FM ");
        stb.append("        ON (FM.FINSCHOOLCD = SB_M.FINSCHOOLCD) ");

        return stb.toString();
    }

    /** 生徒基本情報情報(留学) **/
    private String sqlStudyAbroad(final Param param, final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append("    SELECT STD.SCHREGNO "); //学籍番号
        stb.append("         , STD.TRANSFER_SDATE ");//留学開始日
        stb.append("         , STD.TRANSFER_EDATE ");//留学終了日
        stb.append("         , STD.TRANSFERPLACE "); //留学場所
        stb.append("      FROM SCHREG_TRANSFER_DAT STD ");
        stb.append("     WHERE STD.TRANSFERCD = '1' ");
        stb.append("       AND STD.SCHREGNO = '" + schregno + "' ");
        stb.append("  ORDER BY STD.TRANSFER_SDATE DESC ");

        return stb.toString();
    }

    /** 特別活動の記録(部活動) **/
    private String sqlExtracurricularActivities(final Param param, final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH ");
        stb.append(" SRD AS ( ");
        stb.append("       SELECT SCHREGNO ");
        stb.append("            , YEAR ");
        stb.append("            , GRADE ");
        stb.append("         FROM SCHREG_REGD_DAT ");
        stb.append("        WHERE SCHREGNO = '" + schregno + "' ");
        stb.append("     GROUP BY SCHREGNO ");
        stb.append("            , YEAR ");
        stb.append("            , GRADE ");
        stb.append(" ) ");
        stb.append(" , ");
        stb.append(" SCHD AS ( ");
        stb.append("     SELECT CASE WHEN MONTH(SCHREG_CLUB_HIST_DAT.SDATE) < 4 THEN YEAR(SCHREG_CLUB_HIST_DAT.SDATE)-1 ");
        stb.append("                 ELSE YEAR (SCHREG_CLUB_HIST_DAT.SDATE) ");
        stb.append("            END AS SDATE_NENDO ");
        stb.append("          , CASE WHEN MONTH(SCHREG_CLUB_HIST_DAT.EDATE) < 4 THEN YEAR(SCHREG_CLUB_HIST_DAT.EDATE)-1 ");
        stb.append("                 ELSE YEAR (SCHREG_CLUB_HIST_DAT.EDATE) ");
        stb.append("            END AS EDATE_NENDO ");
        stb.append("          , SCHOOLCD ");
        stb.append("          , SCHOOL_KIND ");
        stb.append("          , SCHREGNO ");
        stb.append("          , CLUBCD ");
        stb.append("          , SDATE ");
        stb.append("          , EDATE ");
        stb.append("          , EXECUTIVECD ");
        stb.append("       FROM SCHREG_CLUB_HIST_DAT ");
        stb.append(" ) ");//END WITH
        stb.append("    SELECT SRD.SCHREGNO "); //学籍番号
        stb.append("         , SRD.YEAR "); //年度(西暦)
        stb.append("         , SRD.GRADE "); //学年
        stb.append("         , CASE WHEN SRD.GRADE = '01' THEN '1年' ");
        stb.append("                WHEN SRD.GRADE = '02' THEN '2年' ");
        stb.append("                WHEN SRD.GRADE = '03' THEN '3年' ");
        stb.append("                ELSE NULL ");
        stb.append("           END AS GRADE_NAME ");//学年
        stb.append("         , CM.CLUBNAME "); //部活動(名称)
        stb.append("         , NM.NAME1 AS EXECUTIVE_NAME ");//役職
        stb.append("         , SCHD.SDATE "); //入部日
        stb.append("         , CASE WHEN SRD.YEAR = SCHD.EDATE_NENDO THEN SCHD.EDATE ");
        stb.append("                ELSE NULL ");
        stb.append("           END AS EDATE "); //退部日付
        stb.append("      FROM SRD ");
        stb.append(" LEFT JOIN SCHD ");
        stb.append("        ON (SRD.SCHREGNO = SCHD.SCHREGNO ");
        stb.append("       AND  SCHD.SDATE_NENDO <= SRD.YEAR ");
        stb.append("           ) ");
        stb.append(" LEFT JOIN CLUB_MST CM ");
        stb.append("        ON CM.CLUBCD = SCHD.CLUBCD ");
        stb.append(" LEFT JOIN NAME_MST NM ");
        stb.append("        ON (NM.NAMECD1 = 'J001' ");
        stb.append("       AND  NM.NAMECD2 = SCHD.EXECUTIVECD) ");
        stb.append("     WHERE SCHD.CLUBCD IS NOT NULL ");
        stb.append("  ORDER BY SRD.SCHREGNO ");
        stb.append("         , SRD.YEAR ");
        stb.append("         , SRD.GRADE ");

        return stb.toString();
    }

    /** 特別活動の記録(委員会・生徒会) **/
    private String sqlCommittee(final Param param, final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH ");
        stb.append(" SRD AS ( ");
        stb.append("       SELECT SCHREGNO ");
        stb.append("            , YEAR ");
        stb.append("            , SEMESTER ");
        stb.append("         FROM SCHREG_REGD_DAT ");
        stb.append("        WHERE SCHREGNO = '" + schregno + "' ");
        stb.append(" ) ");//END WITH
        stb.append("    SELECT SCHD.SCHREGNO "); //学籍番号
        stb.append("         , SCHD.YEAR "); //年度
        stb.append("         , CMT_M.COMMITTEENAME ");//委員会・生徒会(名称)
        stb.append("         , NM.NAME1 AS EXECUTIVE_NAME ");//役職
        stb.append("         , SCHD.GRADE "); //学年
        stb.append("      FROM SRD ");
        stb.append("      JOIN SCHREG_COMMITTEE_HIST_DAT SCHD ");
        stb.append("        ON (SCHD.SCHOOLCD    = '" + param._schoolCd + "' ");
        stb.append("       AND  SCHD.SCHOOL_KIND = '" + param._schoolKind + "' ");
        stb.append("       AND  SCHD.YEAR        = SRD.YEAR ");
        stb.append("       AND  SCHD.SCHREGNO    = SRD.SCHREGNO) ");
        stb.append(" LEFT JOIN COMMITTEE_MST CMT_M ");
        stb.append("        ON (CMT_M.SCHOOLCD    = SCHD.SCHOOLCD ");
        stb.append("       AND  CMT_M.SCHOOL_KIND = SCHD.SCHOOL_KIND ");
        stb.append("       AND  CMT_M.COMMITTEECD = SCHD.COMMITTEECD) ");
        stb.append(" LEFT JOIN NAME_MST NM ");
        stb.append("        ON (NM.NAMECD1 = 'J002' ");
        stb.append("       AND  NM.NAMECD2 = SCHD.EXECUTIVECD) ");
        stb.append("  GROUP BY SCHD.SCHREGNO ");
        stb.append("         , SCHD.YEAR ");
        stb.append("         , SCHD.GRADE ");
        stb.append("         , CMT_M.COMMITTEENAME ");
        stb.append("         , SCHD.EXECUTIVECD ");
        stb.append("         , NM.NAME1 ");
        stb.append("  ORDER BY SCHD.SCHREGNO ");
        stb.append("         , SCHD.YEAR ");
        stb.append("         , CMT_M.COMMITTEENAME ");
        stb.append("         , SCHD.EXECUTIVECD ");

        return stb.toString();
    }

    /** 保護者情報1 **/
    private String sqlGuardian1Data(final Param param, final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append("    SELECT GD.SCHREGNO "); //学籍番号
        stb.append("         , GD.GUARD_KANA "); //保護者1(氏名かな)
        stb.append("         , GD.GUARD_NAME "); //保護者1(氏名)
        stb.append("         , NM.NAME1 AS RELATION_NAME ");//保護者1(続柄)
        stb.append("         , GD.GUARD_ADDR1 "); //保護者1(住所1)
        stb.append("         , GD.GUARD_ADDR2 "); //保護者1(住所2)
        stb.append("         , GD.GUARD_TELNO "); //保護者1(電話)
        stb.append("         , GD.GUARD_E_MAIL "); //保護者1(メール)
        stb.append("      FROM GUARDIAN_DAT GD ");
        stb.append(" LEFT JOIN NAME_MST NM ");
        stb.append("        ON (NM.NAMECD1 = 'H201' ");
        stb.append("       AND  NM.NAMECD2 = GD.RELATIONSHIP) ");
        stb.append("  WHERE GD.SCHREGNO = '" + schregno + "' ");

        return stb.toString();
    }

    /** 保護者情報2 **/
    private String sqlGuardian2Data(final Param param, final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append("    SELECT GD2.SCHREGNO "); //学籍番号
        stb.append("         , GD2.GUARD_KANA "); //保護者1(氏名かな)
        stb.append("         , GD2.GUARD_NAME "); //保護者1(氏名)
        stb.append("         , NM.NAME1 AS RELATION_NAME ");//保護者1(続柄)
        stb.append("         , GD2.GUARD_ADDR1 ");//保護者1(住所1)
        stb.append("         , GD2.GUARD_ADDR2 ");//保護者1(住所2)
        stb.append("         , GD2.GUARD_TELNO ");//保護者1(電話)
        stb.append("         , GD2.GUARD_E_MAIL ");//保護者1(メール)
        stb.append("      FROM GUARDIAN2_DAT GD2 ");
        stb.append(" LEFT JOIN NAME_MST NM ");
        stb.append("        ON (NM.NAMECD1 = 'H201' ");
        stb.append("       AND  NM.NAMECD2 = GD2.RELATIONSHIP) ");
        stb.append("  WHERE GD2.SCHREGNO = '" + schregno + "' ");

        return stb.toString();
    }

    /** 身元引受人情報 **/
    private String sqlGuaranterData(final Param param, final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH ");
        stb.append(" GUARANTOR_HIST_LAST AS ( ");
        stb.append("        SELECT GHD.SCHREGNO ");
        stb.append("             , MAX(GHD.ISSUEDATE) AS ISSUEDATE ");
        stb.append("          FROM GUARANTOR_HIST_DAT GHD ");
        stb.append("         WHERE GHD.SCHREGNO = '" + schregno + "' ");
        stb.append("      GROUP BY GHD.SCHREGNO ");
        stb.append(" ) ");//END WITH
        stb.append("    SELECT GHD.SCHREGNO ");
        stb.append("         , GHD.GUARANTOR_KANA "); //身元引受人(氏名かな)
        stb.append("         , GHD.GUARANTOR_NAME "); //身元引受人(氏名)
        stb.append("         , NM.NAME1 AS RELATION_NAME ");//身元引受人(続柄)
        stb.append("         , GAD.GUARANTOR_ADDR1 ");//身元引受人(住所1)
        stb.append("         , GAD.GUARANTOR_ADDR2 ");//身元引受人(住所2)
        stb.append("         , GAD.GUARANTOR_TELNO ");//身元引受人(電話)
        stb.append("      FROM GUARANTOR_HIST_DAT GHD ");
        stb.append("      JOIN GUARANTOR_HIST_LAST GHL ");
        stb.append("        ON (GHL.SCHREGNO  = GHD.SCHREGNO ");
        stb.append("       AND  GHL.ISSUEDATE = GHD.ISSUEDATE) ");
        stb.append(" LEFT JOIN GUARANTOR_ADDRESS_DAT GAD ");
        stb.append("        ON (GAD.SCHREGNO  = GHD.SCHREGNO) ");
        stb.append(" LEFT JOIN NAME_MST NM ");
        stb.append("        ON (NM.NAMECD1 = 'H201' ");
        stb.append("       AND  NM.NAMECD2 = GHD.GUARANTOR_RELATIONSHIP) ");

        return stb.toString();
    }

    /** その他の情報 **/
    private String sqlSendData(final Param param, final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT SSA.SCHREGNO "); //学籍番号
        stb.append("      , SSA.SEND_KANA "); //その他(氏名かな)
        stb.append("      , SSA.SEND_NAME "); //その他(氏名)
        stb.append("      , NM.NAME1 AS RELATION_NAME "); //その他(続柄)
        stb.append("      , SSA.SEND_ADDR1 ");//その他(住所1)
        stb.append("      , SSA.SEND_ADDR2 ");//その他(住所2)
        stb.append("      , SSA.SEND_TELNO ");//その他(電話)
        stb.append("   FROM SCHREG_SEND_ADDRESS_DAT SSA ");
        stb.append(" LEFT JOIN NAME_MST NM ");
        stb.append("        ON (NM.NAMECD1 = 'H201' ");
        stb.append("       AND  NM.NAMECD2 = SSA.SEND_RELATIONSHIP) ");
        stb.append("  WHERE SSA.SCHREGNO = '" + schregno + "' ");
        stb.append(" ORDER BY SSA.DIV ASC ");

        return stb.toString();
    }

    /** 緊急連絡先情報 **/
    private String sqlEmergencyData(final Param param, final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT SBM.SCHREGNO "); //学籍番号
        stb.append("      , SBM.EMERGENCYNAME AS EMERGENCYNAME1 "); //緊急連絡先(緊急1：氏名)
        stb.append("      , SBM.EMERGENCYTELNO AS EMERGENCYTELNO1 "); //緊急連絡先(緊急1：連絡先)
        stb.append("      , SBM.EMERGENCYNAME2 ");//緊急連絡先(緊急2：氏名)
        stb.append("      , SBM.EMERGENCYTELNO2 ");//緊急連絡先(緊急2：連絡先)
        stb.append("   FROM SCHREG_BASE_MST SBM ");
        stb.append("  WHERE SBM.SCHREGNO = '" + schregno + "' ");

        return stb.toString();
    }

    /** 兄弟姉妹情報 **/
    private String sqlSiblingsData(final Param param, final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH ");
        stb.append(" RELA AS ( ");
        stb.append("     SELECT SCHREGNO ");
        stb.append("          , RELAKANA ");
        stb.append("          , RELANAME ");
        stb.append("          , RELATIONSHIP ");
        stb.append("          , RELA_SCHREGNO ");
        stb.append("       FROM SCHREG_RELA_DAT ");
        stb.append("      WHERE RELA_SCHREGNO IS NOT NULL ");
        stb.append("        AND SCHREGNO = '" + schregno + "' ");
        stb.append(" ) ");//END WITH
        stb.append("    SELECT RELA.SCHREGNO ");//学籍番号(※本人)
        stb.append("         , RELA.RELAKANA ");//兄弟姉妹(かな)
        stb.append("         , RELA.RELANAME ");//兄弟姉妹(氏名)
        stb.append("         , NM.NAME1 AS RELATION_NAME ");//兄弟姉妹(続柄)
        stb.append("         , CASE WHEN SRD.GRADE = '01' THEN '1年' ");
        stb.append("                WHEN SRD.GRADE = '02' THEN '2年' ");
        stb.append("                WHEN SRD.GRADE = '03' THEN '3年' ");
        stb.append("                ELSE NULL ");
        stb.append("           END || SRH.HR_CLASS_NAME1 AS GRADE_NAME ");//兄弟姉妹(年組)
        stb.append("      FROM RELA ");
        stb.append(" LEFT JOIN SCHREG_REGD_DAT SRD ");
        stb.append("        ON (SRD.YEAR     = '" + param._year + "' ");
        stb.append("       AND  SRD.SEMESTER = '" + param._gakki + "' ");
        stb.append("       AND  SRD.SCHREGNO = RELA.RELA_SCHREGNO) ");
        stb.append(" LEFT JOIN SCHREG_REGD_HDAT SRH ");
        stb.append("        ON (SRH.YEAR     = SRD.YEAR ");
        stb.append("       AND  SRH.SEMESTER = SRD.SEMESTER ");
        stb.append("       AND  SRH.GRADE    = SRD.GRADE ");
        stb.append("       AND  SRH.HR_CLASS = SRD.HR_CLASS) ");
        stb.append(" LEFT JOIN NAME_MST NM ");
        stb.append("        ON (NM.NAMECD1 = 'H201' ");
        stb.append("       AND  NM.NAMECD2 = RELA.RELATIONSHIP) ");

        return stb.toString();
    }

    /** 科目コード取得 **/
    private String sqlSubClassCd(final Param param, final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SRD AS ( ");
        stb.append("   SELECT ");
        stb.append("     SRD.SCHREGNO ");
        stb.append("     , SRD.YEAR ");
        stb.append("     , SRD.GRADE ");
        stb.append("   FROM ");
        stb.append("     SCHREG_REGD_DAT SRD ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT SRH ");
        stb.append("       ON ( ");
        stb.append("         SRH.YEAR = SRD.YEAR ");
        stb.append("         AND SRH.SEMESTER = SRD.SEMESTER ");
        stb.append("         AND SRH.GRADE = SRD.GRADE ");
        stb.append("         AND SRH.HR_CLASS = SRD.HR_CLASS ");
        stb.append("       ) JOIN SCHREG_BASE_MST SB_M ");
        stb.append("         ON (SB_M.SCHREGNO = SRD.SCHREGNO) ");
        stb.append("   WHERE ");
        stb.append("     SRD.SCHREGNO = '" + schregno + "' ");
        stb.append("   GROUP BY ");
        stb.append("     SRD.SCHREGNO ");
        stb.append("     , SRD.YEAR ");
        stb.append("     , SRD.GRADE ");
        stb.append("     , SRH.HR_CLASS_NAME2 ");
        stb.append("     , SRD.ATTENDNO ");
        stb.append("     , SB_M.NAME ");
        stb.append("   ORDER BY ");
        stb.append("     SRD.SCHREGNO ");
        stb.append("     , SRD.YEAR ");
        stb.append(" ) ");
        stb.append(" , RSD AS ( ");
        stb.append("   SELECT ");
        stb.append("     SCHREGNO ");
        stb.append("     , RECORD_SCORE_DAT.YEAR ");
        stb.append("     , RECORD_SCORE_DAT.SEMESTER ");
        stb.append("     , RECORD_SCORE_DAT.TESTKINDCD ");
        stb.append("     , RECORD_SCORE_DAT.TESTITEMCD ");
        stb.append("     , RECORD_SCORE_DAT.SCORE_DIV ");
        stb.append("     , RECORD_SCORE_DAT.CLASSCD ");
        stb.append("     , RECORD_SCORE_DAT.SCHOOL_KIND ");
        stb.append("     , RECORD_SCORE_DAT.CURRICULUM_CD ");
        stb.append("     , RECORD_SCORE_DAT.SUBCLASSCD ");
        stb.append("     , SUBCLASS_MST.SUBCLASSABBV ");
        stb.append("     , RECORD_SCORE_DAT.SCORE ");
        stb.append("   FROM ");
        stb.append("     RECORD_SCORE_DAT ");
        stb.append("     LEFT JOIN SUBCLASS_MST ");
        stb.append("       ON ( ");
        stb.append("         SUBCLASS_MST.CLASSCD = RECORD_SCORE_DAT.CLASSCD ");
        stb.append("         AND SUBCLASS_MST.SCHOOL_KIND = RECORD_SCORE_DAT.SCHOOL_KIND ");
        stb.append("         AND SUBCLASS_MST.CURRICULUM_CD = RECORD_SCORE_DAT.CURRICULUM_CD ");
        stb.append("         AND SUBCLASS_MST.SUBCLASSCD = RECORD_SCORE_DAT.SUBCLASSCD ");
        stb.append("       ) ");
        stb.append("   WHERE ");
        stb.append("     SCHREGNO = '" + schregno + "' ");
        stb.append("     AND ( ");
        stb.append("       ( ");
        stb.append("         RECORD_SCORE_DAT.SEMESTER IN ('1', '2', '3') ");
        stb.append("         AND RECORD_SCORE_DAT.SCORE_DIV = '01' ");
        stb.append("       ) ");
        stb.append("       OR ( ");
        stb.append("         RECORD_SCORE_DAT.SEMESTER = '9' ");
        stb.append("         AND RECORD_SCORE_DAT.SCORE_DIV = '08' ");
        stb.append("       ) ");
        stb.append("     ) ");
        stb.append(" ) ");
        stb.append(" , TMCNS AS ( ");
        stb.append("   SELECT ");
        stb.append("     * ");
        stb.append("   FROM ");
        stb.append("     TESTITEM_MST_COUNTFLG_NEW_SDIV ");
        stb.append("   WHERE ");
        stb.append("     SEMESTER IN ('1', '2', '3', '9') ");
        stb.append("     AND TESTKINDCD IN ('01', '02', '99') ");
        stb.append("     AND TESTITEMCD IN ('00', '01', '02') ");
        stb.append("     AND SCORE_DIV = '01' ");
        stb.append(" ) "); // END WITH
        stb.append(" SELECT ");
        stb.append("     SRD.YEAR ");
        stb.append("   , RSD.SUBCLASSCD ");
        stb.append("   , RSD.SUBCLASSABBV ");
        stb.append(" FROM ");
        stb.append("   SRD JOIN RSD ");
        stb.append("     ON ( ");
        stb.append("       RSD.SCHREGNO = SRD.SCHREGNO ");
        stb.append("       AND RSD.YEAR = SRD.YEAR ");
        stb.append("     ) JOIN TMCNS ");
        stb.append("       ON ( ");
        stb.append("         TMCNS.YEAR = RSD.YEAR ");
        stb.append("         AND TMCNS.SEMESTER = RSD.SEMESTER ");
        stb.append("         AND TMCNS.TESTKINDCD = RSD.TESTKINDCD ");
        stb.append("         AND TMCNS.TESTITEMCD = RSD.TESTITEMCD ");
        stb.append("         AND TMCNS.SCORE_DIV = RSD.SCORE_DIV ");
        stb.append("       ) ");
        stb.append(" GROUP BY SRD.YEAR ");
        stb.append("        , RSD.SUBCLASSCD ");
        stb.append("        , RSD.SUBCLASSABBV ");
        stb.append(" ORDER BY SRD.YEAR ");
        stb.append("        , RSD.SUBCLASSCD ");

        return stb.toString();
    }

    /** 成績情報 **/
    private String getScoreData(final Param param, final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH ");
        stb.append(" SRD AS ( "); //生徒情報、年度
        stb.append("        SELECT SRD.SCHREGNO "); //学籍番号
        stb.append("             , SRD.YEAR "); //年度(西暦)
        stb.append("             , CASE WHEN SRD.GRADE = '01' THEN '1年' ");
        stb.append("                    WHEN SRD.GRADE = '02' THEN '2年' ");
        stb.append("                    WHEN SRD.GRADE = '03' THEN '3年' ");
        stb.append("                    ELSE NULL ");
        stb.append("               END AS GRADE_NAME "); //学年
        stb.append("             , SRD.GRADE "); //学年CD
        stb.append("          FROM SCHREG_REGD_DAT  SRD ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT SRH ");
        stb.append("            ON (SRH.YEAR     = SRD.YEAR ");
        stb.append("           AND  SRH.SEMESTER = SRD.SEMESTER ");
        stb.append("           AND  SRH.GRADE    = SRD.GRADE ");
        stb.append("           AND  SRH.HR_CLASS = SRD.HR_CLASS) ");
        stb.append("          JOIN SCHREG_BASE_MST SB_M ");
        stb.append("            ON (SB_M.SCHREGNO = SRD.SCHREGNO) ");
        stb.append("         WHERE SRD.SCHREGNO = '" + schregno + "' ");
        stb.append("      GROUP BY SRD.SCHREGNO ");
        stb.append("             , SRD.YEAR ");
        stb.append("             , SRD.GRADE ");
        stb.append("             , SRH.HR_CLASS_NAME2 ");
        stb.append("             , SRD.ATTENDNO ");
        stb.append("             , SB_M.NAME ");
        stb.append("      ORDER BY SRD.SCHREGNO ");
        stb.append("             , SRD.YEAR ");
        stb.append(" ) ");
        stb.append(" , ");
        stb.append(" RSD AS ( "); //特定生徒の成績で絞る
        stb.append("        SELECT SCHREGNO ");
        stb.append("             , RECORD_SCORE_DAT.YEAR ");
        stb.append("             , RECORD_SCORE_DAT.SEMESTER ");
        stb.append("             , RECORD_SCORE_DAT.TESTKINDCD ");
        stb.append("             , RECORD_SCORE_DAT.TESTITEMCD ");
        stb.append("             , RECORD_SCORE_DAT.SCORE_DIV ");
        stb.append("             , RECORD_SCORE_DAT.CLASSCD ");
        stb.append("             , RECORD_SCORE_DAT.SCHOOL_KIND ");
        stb.append("             , RECORD_SCORE_DAT.CURRICULUM_CD ");
        stb.append("             , RECORD_SCORE_DAT.SUBCLASSCD ");
        stb.append("             , SUBCLASS_MST.SUBCLASSABBV ");
        stb.append("             , RECORD_SCORE_DAT.SCORE ");
        stb.append("          FROM RECORD_SCORE_DAT ");
        stb.append("     LEFT JOIN SUBCLASS_MST ");
        stb.append("            ON (SUBCLASS_MST.CLASSCD       = RECORD_SCORE_DAT.CLASSCD ");
        stb.append("           AND  SUBCLASS_MST.SCHOOL_KIND   = RECORD_SCORE_DAT.SCHOOL_KIND ");
        stb.append("           AND  SUBCLASS_MST.CURRICULUM_CD = RECORD_SCORE_DAT.CURRICULUM_CD ");
        stb.append("           AND  SUBCLASS_MST.SUBCLASSCD    = RECORD_SCORE_DAT.SUBCLASSCD) ");
        stb.append("         WHERE SCHREGNO = '" + schregno + "' ");
        stb.append("           AND ( ");
        stb.append(
                "                   (RECORD_SCORE_DAT.SEMESTER IN ('1', '2', '3') AND RECORD_SCORE_DAT.SCORE_DIV = '01') ");
        stb.append(
                "                OR (RECORD_SCORE_DAT.SEMESTER =  '9'             AND RECORD_SCORE_DAT.SCORE_DIV = '08') ");
        stb.append("               ) ");
        stb.append(" ) ");
        stb.append(" , ");
        stb.append(" TMCNS AS ( "); //対象の考査で絞る
        stb.append("     SELECT * ");
        stb.append("       FROM TESTITEM_MST_COUNTFLG_NEW_SDIV ");
        stb.append("      WHERE SEMESTER   IN ('1', '2', '3', '9') ");
        stb.append("        AND TESTKINDCD IN ('01', '02', '99') ");
        stb.append("        AND TESTITEMCD IN ('00', '01', '02') ");
        stb.append("        AND SCORE_DIV  = '01' ");
        stb.append(" ) ");//END WITH
        stb.append("    SELECT SRD.SCHREGNO ");
        stb.append("         , SRD.YEAR "); //年度(西暦)
        stb.append("         , SRD.GRADE_NAME "); //学年
        stb.append("         , SRD.GRADE ");
        stb.append("         , TMCNS.SEMESTER||TMCNS.TESTITEMCD||TMCNS.TESTKINDCD AS TESTCD "); //(学期の識別に使用)
        stb.append("         , TMCNS.TESTITEMNAME "); //学期
        stb.append("         , RSD.SUBCLASSCD "); //(科目の識別に使用)
        stb.append("         , RSD.SUBCLASSABBV "); //科目名
        stb.append("         , RSD.SCORE "); //点数
        stb.append("      FROM SRD ");
        stb.append("      JOIN RSD ");
        stb.append("        ON (RSD.SCHREGNO = SRD.SCHREGNO ");
        stb.append("       AND  RSD.YEAR     = SRD.YEAR) ");
        stb.append("      JOIN TMCNS ");
        stb.append("        ON (TMCNS.YEAR       = RSD.YEAR ");
        stb.append("       AND  TMCNS.SEMESTER   = RSD.SEMESTER ");
        stb.append("       AND  TMCNS.TESTKINDCD = RSD.TESTKINDCD ");
        stb.append("       AND  TMCNS.TESTITEMCD = RSD.TESTITEMCD ");
        stb.append("       AND  TMCNS.SCORE_DIV  = RSD.SCORE_DIV) ");
        stb.append("     WHERE TMCNS.TESTITEMCD <> '99' ");
        stb.append("  ORDER BY SRD.SCHREGNO ");
        stb.append("         , SRD.YEAR ");
        stb.append("         , TMCNS.SEMESTER ");
        stb.append("         , TMCNS.TESTITEMCD ");
        stb.append("         , TMCNS.TESTKINDCD ");
        stb.append("         , RSD.SUBCLASSCD ");

        return stb.toString();
    }
    /** 出欠情報取得 **/
    private String getAttendanceData(final Param param, final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SRD AS ( "); //生徒情報、年度
        stb.append("        SELECT SRD.SCHREGNO ");     //学籍番号
        stb.append("             , SRD.YEAR ");         //年度(西暦)
        stb.append("             , CASE WHEN SRD.GRADE = '01' THEN '1年' ");
        stb.append("                    WHEN SRD.GRADE = '02' THEN '2年' ");
        stb.append("                    WHEN SRD.GRADE = '03' THEN '3年' ");
        stb.append("                    ELSE NULL ");
        stb.append("               END AS GRADE_NAME "); //学年
        stb.append("          FROM SCHREG_REGD_DAT  SRD ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT SRH ");
        stb.append("            ON (SRH.YEAR     = SRD.YEAR ");
        stb.append("           AND  SRH.SEMESTER = SRD.SEMESTER ");
        stb.append("           AND  SRH.GRADE    = SRD.GRADE ");
        stb.append("           AND  SRH.HR_CLASS = SRD.HR_CLASS) ");
        stb.append("          JOIN  SCHREG_BASE_MST SB_M ");
        stb.append("            ON (SB_M.SCHREGNO = SRD.SCHREGNO) ");
        stb.append("         WHERE SRD.SCHREGNO = '" + schregno + "' ");
        stb.append("      GROUP BY SRD.SCHREGNO ");
        stb.append("             , SRD.YEAR ");
        stb.append("             , SRD.GRADE ");
        stb.append("             , SRH.HR_CLASS_NAME2 ");
        stb.append("             , SRD.ATTENDNO ");
        stb.append("             , SB_M.NAME ");
        stb.append("      ORDER BY SRD.SCHREGNO ");
        stb.append("             , SRD.YEAR ");
        stb.append(" ) "); //END WITH
        stb.append("    SELECT SRD.SCHREGNO ");
        stb.append("         , SRD.YEAR ");               //年度(西暦)
        stb.append("         , SRD.GRADE_NAME ");         //学年
        stb.append("         , SAD.CLASSDAYS ");          //授業日数
        stb.append("         , SAD.MOURNING ");           //出停忌引
        stb.append("         , SAD.ABROAD ");             //留学日数
        stb.append("         , (NVL(SAD.SICK, 0)+NVL(SAD.ACCIDENTNOTICE, 0)+NVL(SAD.NOACCIDENTNOTICE, 0)) AS SICK ");//欠席日数
        stb.append("         , SAD.PRESENT ");            //出席日数
        stb.append("         , SUM(ASD.LATE)  AS LATE "); //遅刻日数
        stb.append("         , SUM(ASD.EARLY) AS EARLY ");//早退日数
        stb.append("         , HD.ATTENDREC_REMARK ");    //備考
        stb.append("      FROM SRD ");
        stb.append(" LEFT JOIN SCHREG_ATTENDREC_DAT SAD ");
        stb.append("        ON (SAD.YEAR     = SRD.YEAR ");
        stb.append("       AND  SAD.SCHREGNO = SRD.SCHREGNO) ");
        stb.append(" LEFT JOIN ATTEND_SEMES_DAT ASD ");
        stb.append("        ON (ASD.YEAR     = SRD.YEAR ");
        stb.append("       AND  ASD.SCHREGNO = SRD.SCHREGNO) ");
        stb.append(" LEFT JOIN HTRAINREMARK_DAT HD ");
        stb.append("        ON (HD.YEAR     = SRD.YEAR ");
        stb.append("       AND  HD.SCHREGNO = SRD.SCHREGNO) ");
        stb.append("  GROUP BY SRD.SCHREGNO ");
        stb.append("         , SRD.YEAR ");
        stb.append("         , SRD.GRADE_NAME ");
        stb.append("         , SAD.CLASSDAYS ");
        stb.append("         , SAD.MOURNING ");
        stb.append("         , SAD.ABROAD ");
        stb.append("         , (NVL(SAD.SICK, 0)+NVL(SAD.ACCIDENTNOTICE, 0)+NVL(SAD.NOACCIDENTNOTICE, 0)) ");
        stb.append("         , SAD.PRESENT ");
        stb.append("         , HD.ATTENDREC_REMARK ");
        stb.append(" ORDER BY SRD.SCHREGNO ");
        stb.append("        , SRD.YEAR ");

        return stb.toString();
    }

    /** 資格情報 **/
    private String getQualifiedData(final Param param, final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SQHD AS ( ");
        stb.append("     SELECT SCHREGNO ");
        stb.append("          , REGDDATE ");
        stb.append("          , YEAR ");
        stb.append("          , QUALIFIED_CD ");
        stb.append("          , RANK ");
        stb.append("       FROM SCHREG_QUALIFIED_HOBBY_DAT ");
        stb.append("      WHERE SCHREGNO = '" +  schregno + "' ");
        stb.append(" ) "); //END WITH
        stb.append("    SELECT SQHD.SCHREGNO ");                            //学籍番号
        stb.append("         , CASE WHEN SRD.GRADE = '01' THEN '1年' ");
        stb.append("                WHEN SRD.GRADE = '02' THEN '2年' ");
        stb.append("                WHEN SRD.GRADE = '03' THEN '3年' ");
        stb.append("           ELSE NULL ");
        stb.append("           END AS GRADE_NAME ");                        //学年
        stb.append("         , SQHD.REGDDATE ");                            //取得日
        stb.append("         , QF_M.QUALIFIED_NAME||NM_M.NAME1 AS SIKAKU ");//資格名
        stb.append("      FROM SQHD ");
        stb.append(" LEFT JOIN SCHREG_REGD_DAT SRD ");
        stb.append("        ON (SRD.YEAR     = SQHD.YEAR ");
        stb.append("       AND  SRD.SCHREGNO = SQHD.SCHREGNO) ");
        stb.append(" LEFT JOIN QUALIFIED_MST QF_M ");
        stb.append("        ON (QF_M.QUALIFIED_CD = SQHD.QUALIFIED_CD) ");
        stb.append(" LEFT JOIN NAME_MST NM_M ");
        stb.append("        ON (NM_M.NAMECD1 = 'H312' ");
        stb.append("       AND  NM_M.NAMECD2 = SQHD.RANK) ");
        stb.append("  GROUP BY SQHD.SCHREGNO ");
        stb.append("         , SRD.GRADE ");
        stb.append("         , SQHD.REGDDATE ");
        stb.append("         , QF_M.QUALIFIED_NAME ");
        stb.append("         , NM_M.NAME1 ");
        stb.append("  ORDER BY SQHD.SCHREGNO ");
        stb.append("         , GRADE DESC ");
        stb.append("         , SQHD.REGDDATE DESC ");

        return stb.toString();
    }

    private String getClassSchregnos(String year, String gakki, String[] categorySelected) {

        final StringBuffer stb = new StringBuffer();

        stb.append("   SELECT ");
        stb.append("          SRD.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("          SCHREG_REGD_DAT SRD ");
        stb.append("    WHERE ");
        stb.append("          SRD.YEAR = '" + year + "' ");
        stb.append("      AND SRD.SEMESTER = '" + gakki + "' ");
        stb.append("      AND SRD.GRADE || SRD.HR_CLASS IN " + SQLUtils.whereIn(true, categorySelected));
        stb.append(" ORDER BY ");
        stb.append("          SRD.GRADE ");
        stb.append("        , SRD.HR_CLASS ");
        stb.append("        , SRD.ATTENDNO ");

        return stb.toString();
    }

    private Param getParam(final DB2UDB db2, final HttpServletRequest request) {
        KNJServletUtils.debugParam(request, log);
        log.fatal("$Revision$ $Date$"); // CVSキーワードの取り扱いに注意
        Param param = new Param(db2, request);
        return param;
    }

    private class Param {
        private final String _year;
        private final String _gakki;
        private final String _kubun;
        private final String[] _schregnos;
        private final String[] _categorySelected;
        private final String _date;
        private final String _schoolCd;
        private final String _schoolKind;
        private final String _schoolName;

        private final String _documentRoot;
        private String _imagepath;
        private String _extension;
        final boolean _isOutputDebug;

        public Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR"); //年度
            _gakki = request.getParameter("GAKKI"); //学期
            _kubun = request.getParameter("KUBUN"); //1：個人、2：クラス
            _date = request.getParameter("CTRL_DATE").replace('/', '-'); // ログイン日付
            _schoolCd = request.getParameter("SCHOOLCD");
            _schoolKind = request.getParameter("SCHOOLKIND");
            _schoolName = request.getParameter("SCHOONAME");

            // 学籍番号の指定
            _categorySelected = request.getParameterValues("CLASS_SELECTED"); //学籍番号

            _documentRoot = request.getParameter("DOCUMENTROOT");

            PreparedStatement ps = null;
            ResultSet rs = null;
            String sql = null;


            if ("2".equals(_kubun) == true) {
                String _categorySelectes = "";
                try {
                    sql = getClassSchregnos(_year, _gakki, _categorySelected);
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    int count = 0;
                    while (rs.next()) {
                        if (count == 0) {
                            _categorySelectes = rs.getString("SCHREGNO");
                        } else {
                            _categorySelectes += "," + rs.getString("SCHREGNO");
                        }
                        count++;
                    }
                } catch (SQLException e) {
                    log.error("Param (_kubun==2) error!", e);
                }
                _schregnos = _categorySelectes.split(",");
            } else {
                _schregnos = _categorySelected;
            }

            //  写真データ
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;

            try {
                returnval = getinfo.Control(db2);
                _imagepath = returnval.val4; //格納フォルダ
                _extension = returnval.val5; //拡張子
            } catch (Exception e) {
                log.error("Param getinfo.Control() error!", e);
            } finally {
                getinfo = null;
                returnval = null;
            }
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }

        private String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2,
                    " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJA228A' AND NAME = '" + propName
                            + "' "));
        }

        public File getImageFile(final String name) {
            final File file = new File(_documentRoot + "/" + _imagepath + "/" + name);
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
